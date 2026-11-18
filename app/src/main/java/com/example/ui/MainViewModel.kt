package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.SchoolRepository
import com.example.localization.AppStrings
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = SchoolRepository(application)
    private val prefs = application.getSharedPreferences("iftin_school_prefs", Context.MODE_PRIVATE)

    // Language state: default "so" (Somali), alternative "en" (English)
    private val _language = MutableStateFlow(prefs.getString("app_language", "so") ?: "so")
    val language: StateFlow<String> = _language.asStateFlow()

    // Authentication & Role State
    private val _currentUser = MutableStateFlow(Firebase.auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Portal Selection: "HOME", "ADMIN", "TEACHER", "PARENT"
    private val _currentPortal = MutableStateFlow("HOME")
    val currentPortal: StateFlow<String> = _currentPortal.asStateFlow()

    // Selected Sub-Screen / Tab within Portal
    private val _currentTab = MutableStateFlow("DASHBOARD")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // School Config
    private val _schoolConfig = MutableStateFlow(SchoolConfig())
    val schoolConfig: StateFlow<SchoolConfig> = _schoolConfig.asStateFlow()

    // Realtime Lists
    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    private val _classes = MutableStateFlow<List<ClassRoom>>(emptyList())
    val classes: StateFlow<List<ClassRoom>> = _classes.asStateFlow()

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _examFolders = MutableStateFlow<List<ExamFolder>>(emptyList())
    val examFolders: StateFlow<List<ExamFolder>> = _examFolders.asStateFlow()

    private val _examinations = MutableStateFlow<List<Examination>>(emptyList())
    val examinations: StateFlow<List<Examination>> = _examinations.asStateFlow()

    private val _allAttendance = MutableStateFlow<List<DailyAttendance>>(emptyList())
    val allAttendance: StateFlow<List<DailyAttendance>> = _allAttendance.asStateFlow()

    private val _fees = MutableStateFlow<List<Fee>>(emptyList())
    val fees: StateFlow<List<Fee>> = _fees.asStateFlow()

    private val _feePayments = MutableStateFlow<List<FeePayment>>(emptyList())
    val feePayments: StateFlow<List<FeePayment>> = _feePayments.asStateFlow()

    private val _notifications = MutableStateFlow<List<SchoolNotification>>(emptyList())
    val notifications: StateFlow<List<SchoolNotification>> = _notifications.asStateFlow()

    // Teacher specific selected class & password-unlocked class IDs
    private val _selectedClass = MutableStateFlow<ClassRoom?>(null)
    val selectedClass: StateFlow<ClassRoom?> = _selectedClass.asStateFlow()

    private val _unlockedClassIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedClassIds: StateFlow<Set<String>> = _unlockedClassIds.asStateFlow()

    // Parent specific linked students and selected child
    private val _parentProfile = MutableStateFlow<ParentProfile?>(null)
    val parentProfile: StateFlow<ParentProfile?> = _parentProfile.asStateFlow()

    private val _linkedStudents = MutableStateFlow<List<Student>>(emptyList())
    val linkedStudents: StateFlow<List<Student>> = _linkedStudents.asStateFlow()

    private val _selectedChild = MutableStateFlow<Student?>(null)
    val selectedChild: StateFlow<Student?> = _selectedChild.asStateFlow()

    // Status / Message toast
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val dataObservationJobs = mutableListOf<Job>()
    private var parentProfileJob: Job? = null
    private var observedUid: String? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        _currentUser.value = user
        if (user != null) {
            if (observedUid != user.uid) {
                observedUid = user.uid
                loadSchoolConfig()
                startObservingData()
            }
            loadUserProfileAndParent(user.uid)
        } else {
            observedUid = null
            stopObservingData()
            _userProfile.value = null
            _parentProfile.value = null
            _linkedStudents.value = emptyList()
            _selectedChild.value = null
        }
    }

    init {
        Firebase.auth.addAuthStateListener(authStateListener)
        Firebase.auth.currentUser?.let { user ->
            observedUid = user.uid
            loadSchoolConfig()
            startObservingData()
            loadUserProfileAndParent(user.uid)
        }
    }

    override fun onCleared() {
        Firebase.auth.removeAuthStateListener(authStateListener)
        stopObservingData()
        super.onCleared()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.edit().putString("app_language", lang).apply()
    }

    fun selectPortal(portal: String) {
        _currentPortal.value = portal
        _currentTab.value = "DASHBOARD"
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun selectTeacherClass(classRoom: ClassRoom?) {
        _selectedClass.value = classRoom
    }

    fun unlockClass(classRoom: ClassRoom, enteredPassword: String): Boolean {
        return if (classRoom.accessCode.trim() == enteredPassword.trim() && enteredPassword.isNotBlank()) {
            _unlockedClassIds.value = _unlockedClassIds.value + classRoom.classId
            _selectedClass.value = classRoom
            true
        } else {
            false
        }
    }

    fun selectChild(student: Student?) {
        _selectedChild.value = student
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun hasCreatedParentRoom(): Boolean {
        val uid = Firebase.auth.currentUser?.uid ?: return false
        if (prefs.getBoolean("parent_room_$uid", false)) return true
        if (_userProfile.value?.role == "PARENT") return true
        if (_parentProfile.value != null) return true
        return false
    }

    private fun loadSchoolConfig() {
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            try {
                val cfg = repository.getSchoolConfig()
                _schoolConfig.value = cfg
                val db = FirebaseFirestore.getInstance(
                    getApplication<Application>().getString(com.example.R.string.firestore_database_id)
                )
                com.example.data.SampleDataSeeder.seedInitialDataIfEmpty(repository, db)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("MainViewModel", "Failed to load school config: ${e.message}", e)
            }
        }
    }

    fun refreshAuthUser() {
        val user = Firebase.auth.currentUser
        _currentUser.value = user
        if (user != null) {
            if (observedUid != user.uid) {
                observedUid = user.uid
                loadSchoolConfig()
                startObservingData()
            }
            loadUserProfileAndParent(user.uid)
        } else {
            observedUid = null
            stopObservingData()
            _userProfile.value = null
            _parentProfile.value = null
            _linkedStudents.value = emptyList()
            _selectedChild.value = null
        }
    }

    private fun loadUserProfileAndParent(uid: String) {
        viewModelScope.launch {
            try {
                val prof = repository.getUserProfile(uid)
                _userProfile.value = prof
                if (prof?.role == "PARENT" || prefs.getBoolean("parent_room_$uid", false)) {
                    prefs.edit().putBoolean("parent_room_$uid", true).apply()
                    loadParentProfile(uid)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("MainViewModel", "Failed to load user profile: ${e.message}", e)
            }
        }
    }

    private fun stopObservingData() {
        dataObservationJobs.forEach { it.cancel() }
        dataObservationJobs.clear()
        parentProfileJob?.cancel()
        parentProfileJob = null
    }

    private fun startObservingData() {
        if (Firebase.auth.currentUser == null) return
        stopObservingData()

        dataObservationJobs += viewModelScope.launch {
            repository.observeTeachers()
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeTeachers error: ${e.message}")
                }
                .collectLatest { _teachers.value = it }
        }
        dataObservationJobs += viewModelScope.launch {
            repository.observeClasses()
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeClasses error: ${e.message}")
                }
                .collectLatest { list ->
                    _classes.value = list
                    val currentId = _selectedClass.value?.classId
                    if (currentId != null) {
                        _selectedClass.value = list.firstOrNull { it.classId == currentId }
                    }
                }
        }
        dataObservationJobs += viewModelScope.launch {
            repository.observeStudents()
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeStudents error: ${e.message}")
                }
                .collectLatest { _students.value = it }
        }
        dataObservationJobs += viewModelScope.launch {
            repository.observeExamFolders()
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeExamFolders error: ${e.message}")
                }
                .collectLatest { _examFolders.value = it }
        }
        dataObservationJobs += viewModelScope.launch {
            repository.observeExaminations()
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeExaminations error: ${e.message}")
                }
                .collectLatest { _examinations.value = it }
        }
        dataObservationJobs += viewModelScope.launch {
            repository.observeAllAttendance()
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeAllAttendance error: ${e.message}")
                }
                .collectLatest { _allAttendance.value = it }
        }
        dataObservationJobs += viewModelScope.launch {
            repository.observeFees()
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeFees error: ${e.message}")
                }
                .collectLatest { _fees.value = it }
        }
        dataObservationJobs += viewModelScope.launch {
            repository.observeFeePayments()
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeFeePayments error: ${e.message}")
                }
                .collectLatest { _feePayments.value = it }
        }
        dataObservationJobs += viewModelScope.launch {
            repository.observeNotifications("ALL")
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeNotifications error: ${e.message}")
                }
                .collectLatest { _notifications.value = it }
        }
    }

    fun loadParentProfile(parentId: String) {
        parentProfileJob?.cancel()
        parentProfileJob = viewModelScope.launch {
            repository.observeParentProfile(parentId)
                .catch { e ->
                    if (e is CancellationException && e.cause == null) throw e
                    Log.w("MainViewModel", "observeParentProfile error: ${e.message}")
                }
                .collectLatest { profile ->
                    _parentProfile.value = profile
                    if (profile != null) {
                        prefs.edit().putBoolean("parent_room_$parentId", true).apply()
                        try {
                            val kids = repository.getStudentsByIds(profile.linkedStudentIds)
                            _linkedStudents.value = kids
                            val selectedId = _selectedChild.value?.studentId
                            _selectedChild.value = if (selectedId != null) {
                                kids.firstOrNull { it.studentId == selectedId } ?: kids.firstOrNull()
                            } else {
                                kids.firstOrNull()
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.w("MainViewModel", "Failed to load linked students: ${e.message}")
                        }
                    }
                }
        }
    }

    // --- Actions ---

    fun verifyAdminPasscode(enteredPasscode: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val master = _schoolConfig.value.adminPasscode
        val user = Firebase.auth.currentUser
        if (user != null && (enteredPasscode.trim() == master || _userProfile.value?.role == "ADMIN")) {
            viewModelScope.launch {
                val profile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: "Admin",
                    role = "ADMIN",
                    schoolId = FIXED_SCHOOL_ID,
                    language = _language.value
                )
                repository.saveUserProfile(profile)
                _userProfile.value = profile
                onSuccess()
            }
        } else {
            onError()
        }
    }

    fun createParentRoom(onSuccess: () -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                prefs.edit().putBoolean("parent_room_${user.uid}", true).apply()
                val profile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: "Parent",
                    role = "PARENT",
                    schoolId = FIXED_SCHOOL_ID,
                    parentId = user.uid,
                    language = _language.value
                )
                repository.saveUserProfile(profile)
                _userProfile.value = profile
                loadParentProfile(user.uid)
                onSuccess()
            } catch (e: Exception) {
                _toastMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun linkChild(studentId: String, linkingCode: String, onResult: (Boolean) -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val found = repository.findStudentByLinkingCode(studentId, linkingCode)
                if (found != null) {
                    repository.linkChildToParent(user.uid, found.studentId)
                    prefs.edit().putBoolean("parent_room_${user.uid}", true).apply()
                    loadParentProfile(user.uid)
                    _toastMessage.value = if (_language.value == "so")
                        "Ardayga ${found.fullName} si guul leh ayaa loogu xiriiriyay!"
                    else
                        "Student ${found.fullName} linked successfully!"
                    onResult(true)
                } else {
                    _toastMessage.value = AppStrings.get("child_not_found", _language.value)
                    onResult(false)
                }
            } catch (e: Exception) {
                _toastMessage.value = e.localizedMessage
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Teacher Management
    fun addTeacher(
        fullName: String,
        phone: String,
        email: String,
        classes: List<String> = emptyList(),
        photoUrl: String = ""
    ) {
        viewModelScope.launch {
            val t = Teacher(
                fullName = fullName,
                phone = phone,
                email = email,
                assignedClasses = classes,
                photoUrl = photoUrl
            )
            repository.addTeacher(t)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun updateTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.updateTeacher(teacher)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun deleteTeacher(teacherId: String) {
        viewModelScope.launch {
            repository.deleteTeacher(teacherId)
            _toastMessage.value = AppStrings.get("success_deleted", _language.value)
        }
    }

    // Class Management
    fun addClass(
        name: String,
        teacherId: String = "",
        teacherName: String,
        year: String,
        customAccessCode: String = ""
    ) {
        viewModelScope.launch {
            val accessCode = customAccessCode.trim().ifBlank {
                (1000 + (Math.random() * 9000).toInt()).toString()
            }
            val c = ClassRoom(
                name = name,
                classTeacherId = teacherId,
                classTeacherName = teacherName,
                subjects = listOf("Somali", "English", "Mathematics", "Science", "Islamic Studies"),
                academicYear = year,
                accessCode = accessCode
            )
            repository.addClass(c)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun updateClass(classRoom: ClassRoom) {
        viewModelScope.launch {
            repository.updateClass(classRoom)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun deleteClass(classId: String) {
        viewModelScope.launch {
            repository.deleteClass(classId)
            _toastMessage.value = AppStrings.get("success_deleted", _language.value)
        }
    }

    // Student Management
    fun addStudent(
        fullName: String,
        fatherName: String,
        motherName: String,
        phone: String,
        gender: String,
        classId: String,
        className: String,
        admissionDate: String,
        photoUrl: String = ""
    ) {
        viewModelScope.launch {
            val s = Student(
                fullName = fullName,
                fatherName = fatherName,
                motherName = motherName,
                parentPhone = phone,
                gender = gender,
                classId = classId,
                className = className,
                admissionDate = admissionDate,
                photoUrl = photoUrl
            )
            repository.addStudent(s)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            _toastMessage.value = AppStrings.get("success_deleted", _language.value)
        }
    }

    // Attendance Management
    fun markAttendance(classId: String, className: String, date: String, records: List<AttendanceRecord>) {
        viewModelScope.launch {
            val present = records.count { it.status == "PRESENT" }.toLong()
            val absent = records.count { it.status == "ABSENT" }.toLong()
            val late = records.count { it.status == "LATE" }.toLong()
            val excused = records.count { it.status == "EXCUSED" }.toLong()

            val teacherName = _userProfile.value?.name ?: "Teacher"
            val attendance = DailyAttendance(
                classId = classId,
                className = className,
                date = date,
                teacherId = _currentUser.value?.uid ?: "",
                teacherName = teacherName,
                records = records,
                presentCount = present,
                absentCount = absent,
                lateCount = late,
                excusedCount = excused
            )
            repository.saveAttendance(attendance)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    // Exam Folders Management
    fun createExamFolder(name: String, examType: String, academicYear: String, description: String) {
        viewModelScope.launch {
            val folder = ExamFolder(
                name = name,
                examType = examType,
                academicYear = academicYear,
                description = description
            )
            repository.saveExamFolder(folder)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun deleteExamFolder(folderId: String) {
        viewModelScope.launch {
            repository.deleteExamFolder(folderId)
            _toastMessage.value = AppStrings.get("success_deleted", _language.value)
        }
    }

    // Examination Management
    fun createExamination(
        name: String,
        type: String,
        classId: String,
        className: String,
        subjects: List<ExamSubject>,
        startDate: String,
        endDate: String,
        desc: String,
        folderId: String = "",
        folderName: String = ""
    ) {
        viewModelScope.launch {
            val exam = Examination(
                folderId = folderId,
                folderName = folderName,
                name = name,
                examType = type,
                classId = classId,
                className = className,
                subjects = subjects,
                startDate = startDate,
                endDate = endDate,
                description = desc,
                status = "DRAFT",
                createdByTeacherId = _currentUser.value?.uid ?: ""
            )
            repository.saveExamination(exam)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun updateExamStatus(examId: String, status: String) {
        viewModelScope.launch {
            repository.updateExamStatus(examId, status)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun submitExamResults(exam: Examination, results: List<ExamResult>) {
        viewModelScope.launch {
            repository.saveExamResultsBatch(results)
            repository.updateExamStatus(exam.examId, "SUBMITTED")
            _toastMessage.value = if (_language.value == "so")
                "Natiijooyinka si guul leh ayaa loogu gudbiyay maamulka!"
            else
                "Examination results submitted successfully!"
        }
    }

    // Fees Management
    fun addFee(feeName: String, classId: String, className: String, amount: Double, dueDate: String, desc: String) {
        viewModelScope.launch {
            val studentsInClass = _students.value.filter { it.classId == classId }
            val fee = Fee(
                feeName = feeName,
                classId = classId,
                className = className,
                amount = amount,
                dueDate = dueDate,
                description = desc,
                isPublished = false
            )
            repository.addFee(fee, studentsInClass)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun publishFeeFolder(fee: Fee) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val studentsInClass = _students.value.filter { it.classId == fee.classId }
            repository.publishFeeToParents(fee, studentsInClass, dateStr)
            _toastMessage.value = AppStrings.get("fee_submitted_notice", _language.value)
        }
    }

    fun recordFeePayment(paymentId: String, amount: Double, receiptNo: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.recordPayment(paymentId, amount, receiptNo, dateStr)
            _toastMessage.value = AppStrings.get("receipt_sent_notice", _language.value)
        }
    }

    // Timetable Management
    fun addTimetableSlot(classId: String, className: String, day: String, subject: String, teacher: String, start: String, end: String, room: String) {
        viewModelScope.launch {
            val slot = TimetableSlot(
                classId = classId,
                className = className,
                day = day,
                subject = subject,
                teacherName = teacher,
                startTime = start,
                endTime = end,
                roomNo = room
            )
            repository.addTimetableSlot(slot)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun deleteTimetableSlot(slotId: String) {
        viewModelScope.launch {
            repository.deleteTimetableSlot(slotId)
            _toastMessage.value = AppStrings.get("success_deleted", _language.value)
        }
    }

    // Remarks Management
    fun addRemark(studentId: String, studentName: String, classId: String, category: String, message: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val remark = StudentRemark(
                studentId = studentId,
                studentName = studentName,
                classId = classId,
                teacherId = _currentUser.value?.uid ?: "",
                teacherName = _userProfile.value?.name ?: "Teacher",
                date = dateStr,
                category = category,
                message = message
            )
            repository.addRemark(remark)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    // Notifications
    fun sendNotification(title: String, message: String, targetRole: String, targetStudentId: String = "") {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val notif = SchoolNotification(
                targetRole = targetRole,
                targetStudentId = targetStudentId,
                title = title,
                message = message,
                date = dateStr
            )
            repository.addNotification(notif)
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    // Update School Settings
    fun updateSchoolSettings(name: String, address: String, phone: String, email: String, year: String, passcode: String) {
        viewModelScope.launch {
            val current = _schoolConfig.value
            val updated = current.copy(
                name = name,
                address = address,
                phone = phone,
                email = email,
                academicYear = year,
                adminPasscode = if (passcode.isNotBlank()) passcode else current.adminPasscode
            )
            repository.updateSchoolConfig(updated)
            _schoolConfig.value = updated
            _toastMessage.value = AppStrings.get("success_saved", _language.value)
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        stopObservingData()
        observedUid = null
        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    CredentialManager.create(getApplication()).clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w("Auth", "Failed to clear credential state: ${e.message}")
                } finally {
                    Firebase.auth.signOut()
                    _currentUser.value = null
                    _userProfile.value = null
                    _parentProfile.value = null
                    _linkedStudents.value = emptyList()
                    _selectedChild.value = null
                    _unlockedClassIds.value = emptySet()
                    _selectedClass.value = null
                    _currentPortal.value = "HOME"
                    onSignedOut()
                }
            }
        }
    }
}
