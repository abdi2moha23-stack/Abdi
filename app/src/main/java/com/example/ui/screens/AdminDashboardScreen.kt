package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.localization.AppStrings
import com.example.pdf.ReportCardPdfGenerator
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.utils.AvatarImage
import com.example.utils.PhotoPickerSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    val teachers by viewModel.teachers.collectAsState()
    val classes by viewModel.classes.collectAsState()
    val students by viewModel.students.collectAsState()
    val examFolders by viewModel.examFolders.collectAsState()
    val exams by viewModel.examinations.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val feePayments by viewModel.feePayments.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    // Dialog States
    var teacherFormTarget by remember { mutableStateOf<Teacher?>(null) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }

    var classFormTarget by remember { mutableStateOf<ClassRoom?>(null) }
    var showAddClassDialog by remember { mutableStateOf(false) }
    var previewClassTarget by remember { mutableStateOf<ClassRoom?>(null) }

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var editStudentTarget by remember { mutableStateOf<Student?>(null) }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var openedExamFolder by remember { mutableStateOf<ExamFolder?>(null) }
    var openedExamForStudents by remember { mutableStateOf<Examination?>(null) }
    var selectedReportPreview by remember { mutableStateOf<Triple<Student, Examination, ExamResult>?>(null) }

    var todayAttendancePopupStatus by remember { mutableStateOf<String?>(null) } // "PRESENT" or "ABSENT"
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Today's attendance calculation
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayAttendance = allAttendance.filter { it.date == todayStr }
    val presentToday = todayAttendance.sumOf { it.presentCount }
    val absentToday = todayAttendance.sumOf { it.absentCount }

    // Published exams & unpaid fees
    val publishedExamsCount = exams.count { it.status == "PUBLISHED" }
    val unpaidFeesTotal = feePayments.filter { it.status != "PAID" }.sumOf { it.balance }

    Scaffold(
        topBar = {
            SchoolTopBar(
                title = schoolConfig.name,
                subtitle = AppStrings.get("admin_portal", lang),
                currentLang = lang,
                onToggleLang = { viewModel.setLanguage(if (lang == "so") "en" else "so") },
                onSignOut = { viewModel.signOut { onBack() } },
                onBack = onBack
            )
        },
        bottomBar = {
            AdminBottomNavigation(
                currentTab = currentTab,
                lang = lang,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
        ) {
            when (currentTab) {
                "DASHBOARD" -> AdminOverviewContent(
                    lang = lang,
                    studentsCount = students.size,
                    teachersCount = teachers.size,
                    classesCount = classes.size,
                    presentToday = presentToday,
                    absentToday = absentToday,
                    publishedExams = publishedExamsCount,
                    unpaidFees = unpaidFeesTotal,
                    notifications = notifications,
                    onOpenTeachers = { viewModel.selectTab("TEACHERS") },
                    onOpenClasses = { viewModel.selectTab("CLASSES") },
                    onOpenStudents = { viewModel.selectTab("STUDENTS") },
                    onOpenExams = { viewModel.selectTab("EXAMS") },
                    onOpenFees = { viewModel.selectTab("FEES") },
                    onOpenPresentToday = { todayAttendancePopupStatus = "PRESENT" },
                    onOpenAbsentToday = { todayAttendancePopupStatus = "ABSENT" },
                    onOpenSettings = { showSettingsDialog = true }
                )
                "TEACHERS" -> AdminTeachersContent(
                    lang = lang,
                    teachers = teachers,
                    onAddTeacher = {
                        teacherFormTarget = null
                        showAddTeacherDialog = true
                    },
                    onEditTeacher = {
                        teacherFormTarget = it
                        showAddTeacherDialog = true
                    },
                    onDeleteTeacher = { viewModel.deleteTeacher(it) }
                )
                "CLASSES" -> AdminClassesContent(
                    lang = lang,
                    classes = classes,
                    students = students,
                    onAddClass = {
                        classFormTarget = null
                        showAddClassDialog = true
                    },
                    onPreviewClass = { previewClassTarget = it },
                    onEditClass = {
                        classFormTarget = it
                        showAddClassDialog = true
                    },
                    onDeleteClass = { viewModel.deleteClass(it) }
                )
                "STUDENTS" -> AdminStudentsContent(
                    lang = lang,
                    students = students,
                    classes = classes,
                    onAddStudent = {
                        editStudentTarget = null
                        showAddStudentDialog = true
                    },
                    onEditStudent = {
                        editStudentTarget = it
                        showAddStudentDialog = true
                    },
                    onDeleteStudent = { viewModel.deleteStudent(it) }
                )
                "EXAMS" -> AdminExamsContent(
                    lang = lang,
                    folders = examFolders,
                    exams = exams,
                    onCreateFolder = { showCreateFolderDialog = true },
                    onDeleteFolder = { viewModel.deleteExamFolder(it) },
                    onOpenFolder = { openedExamFolder = it },
                    onOpenExamStudents = { openedExamForStudents = it }
                )
                "FEES" -> AdminFeesContent(
                    lang = lang,
                    feePayments = feePayments,
                    onRecordPayment = { paymentId, amt, rcpt ->
                        viewModel.recordFeePayment(paymentId, amt, rcpt)
                    }
                )
                "REPORTS" -> AdminReportsContent(
                    lang = lang,
                    school = schoolConfig,
                    students = students,
                    teachers = teachers,
                    classes = classes,
                    exams = exams,
                    feePayments = feePayments
                )
                else -> AdminOverviewContent(
                    lang = lang,
                    studentsCount = students.size,
                    teachersCount = teachers.size,
                    classesCount = classes.size,
                    presentToday = presentToday,
                    absentToday = absentToday,
                    publishedExams = publishedExamsCount,
                    unpaidFees = unpaidFeesTotal,
                    notifications = notifications,
                    onOpenTeachers = { viewModel.selectTab("TEACHERS") },
                    onOpenClasses = { viewModel.selectTab("CLASSES") },
                    onOpenStudents = { viewModel.selectTab("STUDENTS") },
                    onOpenExams = { viewModel.selectTab("EXAMS") },
                    onOpenFees = { viewModel.selectTab("FEES") },
                    onOpenPresentToday = { todayAttendancePopupStatus = "PRESENT" },
                    onOpenAbsentToday = { todayAttendancePopupStatus = "ABSENT" },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }
        }
    }

    // Today's Present / Absent Student List Dialog
    if (todayAttendancePopupStatus != null) {
        val targetStatus = todayAttendancePopupStatus!!
        TodayAttendanceDetailDialog(
            status = targetStatus,
            todayStr = todayStr,
            todayAttendance = todayAttendance,
            lang = lang,
            onDismiss = { todayAttendancePopupStatus = null }
        )
    }

    // Add / Edit Teacher Dialog (NO subjects input; WITH Gallery+Camera photo support)
    if (showAddTeacherDialog) {
        TeacherFormDialog(
            lang = lang,
            existingTeacher = teacherFormTarget,
            onDismiss = {
                showAddTeacherDialog = false
                teacherFormTarget = null
            },
            onConfirm = { name, phone, email, photoBase64 ->
                if (teacherFormTarget != null) {
                    viewModel.updateTeacher(
                        teacherFormTarget!!.copy(
                            fullName = name,
                            phone = phone,
                            email = email,
                            photoUrl = photoBase64
                        )
                    )
                } else {
                    viewModel.addTeacher(
                        fullName = name,
                        phone = phone,
                        email = email,
                        photoUrl = photoBase64
                    )
                }
                showAddTeacherDialog = false
                teacherFormTarget = null
            }
        )
    }

    // Add / Edit Class Dialog (NO subjects input; select from registered teachers)
    if (showAddClassDialog) {
        ClassFormDialog(
            lang = lang,
            teachers = teachers,
            existingClass = classFormTarget,
            onDismiss = {
                showAddClassDialog = false
                classFormTarget = null
            },
            onConfirm = { name, teacherId, teacherName, year, accessCode ->
                if (classFormTarget != null) {
                    viewModel.updateClass(
                        classFormTarget!!.copy(
                            name = name,
                            classTeacherId = teacherId,
                            classTeacherName = teacherName,
                            academicYear = year,
                            accessCode = accessCode.ifBlank { classFormTarget!!.accessCode }
                        )
                    )
                } else {
                    viewModel.addClass(
                        name = name,
                        teacherId = teacherId,
                        teacherName = teacherName,
                        year = year,
                        customAccessCode = accessCode
                    )
                }
                showAddClassDialog = false
                classFormTarget = null
            }
        )
    }

    // Class Preview Dialog (shows class info & enrolled student names)
    if (previewClassTarget != null) {
        val c = previewClassTarget!!
        val classStudents = students.filter { it.classId == c.classId }
        ClassPreviewDialog(
            classRoom = c,
            students = classStudents,
            lang = lang,
            onDismiss = { previewClassTarget = null }
        )
    }

    // Add / Edit Student Dialog
    if (showAddStudentDialog) {
        StudentFormDialog(
            lang = lang,
            classes = classes,
            existingStudent = editStudentTarget,
            onDismiss = {
                showAddStudentDialog = false
                editStudentTarget = null
            },
            onConfirm = { fullName, father, mother, phone, gender, classId, className, admDate, photoUrl ->
                if (editStudentTarget != null) {
                    viewModel.updateStudent(
                        editStudentTarget!!.copy(
                            fullName = fullName,
                            fatherName = father,
                            motherName = mother,
                            parentPhone = phone,
                            gender = gender,
                            classId = classId,
                            className = className,
                            admissionDate = admDate,
                            photoUrl = photoUrl
                        )
                    )
                } else {
                    viewModel.addStudent(
                        fullName = fullName,
                        fatherName = father,
                        motherName = mother,
                        phone = phone,
                        gender = gender,
                        classId = classId,
                        className = className,
                        admissionDate = admDate,
                        photoUrl = photoUrl
                    )
                }
                showAddStudentDialog = false
                editStudentTarget = null
            }
        )
    }

    // Create Exam Folder Dialog
    if (showCreateFolderDialog) {
        CreateExamFolderDialog(
            lang = lang,
            academicYear = schoolConfig.academicYear,
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name, type, year, desc ->
                viewModel.createExamFolder(name, type, year, desc)
                showCreateFolderDialog = false
            }
        )
    }

    // Opened Exam Folder -> shows all classes' exams inside that folder
    if (openedExamFolder != null) {
        val folder = openedExamFolder!!
        val folderExams = exams.filter { it.folderId == folder.folderId }
        AdminExamFolderDetailDialog(
            folder = folder,
            folderExams = folderExams,
            lang = lang,
            viewModel = viewModel,
            onDismiss = { openedExamFolder = null },
            onInspectExamStudents = { exam ->
                openedExamForStudents = exam
            }
        )
    }

    // Inspect every student & report inside an Exam
    if (openedExamForStudents != null) {
        val exam = openedExamForStudents!!
        val classStudents = students.filter { it.classId == exam.classId }
        AdminClassExamStudentsDialog(
            exam = exam,
            students = classStudents,
            school = schoolConfig,
            lang = lang,
            viewModel = viewModel,
            onDismiss = { openedExamForStudents = null },
            onOpenStudentReport = { st, ex, res ->
                selectedReportPreview = Triple(st, ex, res)
            }
        )
    }

    // Full Individual Student Report Card Dialog (from Admin Exam Folder)
    if (selectedReportPreview != null) {
        val (st, ex, res) = selectedReportPreview!!
        AdminStudentReportDialog(
            student = st,
            exam = ex,
            result = res,
            school = schoolConfig,
            lang = lang,
            onDismiss = { selectedReportPreview = null }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AdminSettingsDialog(
            school = schoolConfig,
            lang = lang,
            onDismiss = { showSettingsDialog = false },
            onSave = { name, address, phone, email, year, passcode ->
                viewModel.updateSchoolSettings(name, address, phone, email, year, passcode)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun AdminBottomNavigation(
    currentTab: String,
    lang: String,
    onSelectTab: (String) -> Unit
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = currentTab == "DASHBOARD",
            onClick = { onSelectTab("DASHBOARD") },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text(AppStrings.get("dashboard", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("admin_nav_dashboard")
        )
        NavigationBarItem(
            selected = currentTab == "TEACHERS",
            onClick = { onSelectTab("TEACHERS") },
            icon = { Icon(Icons.Default.People, contentDescription = null) },
            label = { Text(AppStrings.get("teachers", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("admin_nav_teachers")
        )
        NavigationBarItem(
            selected = currentTab == "CLASSES",
            onClick = { onSelectTab("CLASSES") },
            icon = { Icon(Icons.Default.Class, contentDescription = null) },
            label = { Text(AppStrings.get("classes", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("admin_nav_classes")
        )
        NavigationBarItem(
            selected = currentTab == "STUDENTS",
            onClick = { onSelectTab("STUDENTS") },
            icon = { Icon(Icons.Default.School, contentDescription = null) },
            label = { Text(AppStrings.get("students", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("admin_nav_students")
        )
        NavigationBarItem(
            selected = currentTab == "EXAMS",
            onClick = { onSelectTab("EXAMS") },
            icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null) },
            label = { Text(AppStrings.get("exams", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("admin_nav_exams")
        )
        NavigationBarItem(
            selected = currentTab == "FEES",
            onClick = { onSelectTab("FEES") },
            icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            label = { Text(AppStrings.get("fees", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("admin_nav_fees")
        )
    }
}

@Composable
fun AdminOverviewContent(
    lang: String,
    studentsCount: Int,
    teachersCount: Int,
    classesCount: Int,
    presentToday: Long,
    absentToday: Long,
    publishedExams: Int,
    unpaidFees: Double,
    notifications: List<SchoolNotification>,
    onOpenTeachers: () -> Unit,
    onOpenClasses: () -> Unit,
    onOpenStudents: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenFees: () -> Unit,
    onOpenPresentToday: () -> Unit,
    onOpenAbsentToday: () -> Unit,
    onOpenSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        AppStrings.get("single_school_notice", lang),
                        fontSize = 12.sp,
                        color = NavyPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Column {
                Text(
                    AppStrings.get("dashboard", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (lang == "so") "Taabo kaar walbo si aad u aragto xogta uu u taagan yahay" else "Tap any stat card to open its full breakdown",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // Clickable Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = AppStrings.get("total_students", lang),
                        value = "$studentsCount",
                        icon = Icons.Default.School,
                        color = NavyPrimary,
                        modifier = Modifier.weight(1f).testTag("stat_total_students"),
                        onClick = onOpenStudents
                    )
                    StatCard(
                        title = AppStrings.get("total_teachers", lang),
                        value = "$teachersCount",
                        icon = Icons.Default.People,
                        color = BlueSecondary,
                        modifier = Modifier.weight(1f).testTag("stat_total_teachers"),
                        onClick = onOpenTeachers
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = AppStrings.get("total_classes", lang),
                        value = "$classesCount",
                        icon = Icons.Default.Class,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f).testTag("stat_total_classes"),
                        onClick = onOpenClasses
                    )
                    StatCard(
                        title = AppStrings.get("published_exams", lang),
                        value = "$publishedExams",
                        icon = Icons.Default.AssignmentTurnedIn,
                        color = GreenAccent,
                        modifier = Modifier.weight(1f).testTag("stat_published_exams"),
                        onClick = onOpenExams
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = AppStrings.get("present_today", lang),
                        value = "$presentToday",
                        icon = Icons.Default.CheckCircle,
                        color = GreenAccent,
                        modifier = Modifier.weight(1f).testTag("stat_present_today"),
                        onClick = onOpenPresentToday
                    )
                    StatCard(
                        title = AppStrings.get("absent_today", lang),
                        value = "$absentToday",
                        icon = Icons.Default.Cancel,
                        color = RedDanger,
                        modifier = Modifier.weight(1f).testTag("stat_absent_today"),
                        onClick = onOpenAbsentToday
                    )
                }

                StatCard(
                    title = AppStrings.get("unpaid_fees", lang),
                    value = String.format(Locale.US, "$%.2f", unpaidFees),
                    icon = Icons.Default.AttachMoney,
                    color = AmberWarning,
                    modifier = Modifier.testTag("stat_unpaid_fees"),
                    onClick = onOpenFees
                )
            }
        }

        // Quick Actions
        item {
            Text(
                AppStrings.get("quick_actions", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenTeachers,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("admin_action_teachers")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.get("teachers", lang), fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenClasses,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("admin_action_classes")
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.get("classes", lang), fontSize = 12.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenExams,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("admin_action_publish_exams")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.get("exam_folders", lang), fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("admin_action_settings")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.get("settings", lang), fontSize = 12.sp)
                }
            }
        }

        // Recent Activity Feed
        item {
            Text(
                AppStrings.get("recent_activities", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (notifications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        AppStrings.get("no_data", lang),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(notifications.take(5)) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(notif.message, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayAttendanceDetailDialog(
    status: String,
    todayStr: String,
    todayAttendance: List<DailyAttendance>,
    lang: String,
    onDismiss: () -> Unit
) {
    val entries = remember(todayAttendance, status) {
        todayAttendance.flatMap { att ->
            att.records.filter { it.status == status }.map { rec ->
                Triple(rec.studentName, rec.studentId, att.className)
            }
        }
    }
    val title = if (status == "PRESENT") AppStrings.get("today_present_list", lang) else AppStrings.get("today_absent_list", lang)
    val accent = if (status == "PRESENT") GreenAccent else RedDanger

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = accent)
                Text("${AppStrings.get("date", lang)}: $todayStr (${entries.size})", fontSize = 12.sp, color = Color.Gray)
            }
        },
        text = {
            if (entries.isEmpty()) {
                Text(
                    if (lang == "so") "Maanta weli lama diiwaangelin arday xaaladdan leh." else "No students recorded with this status today yet.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries.size) { index ->
                        val (name, id, className) = entries[index]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${index + 1}. $name", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("$id • $className", fontSize = 11.sp, color = Color.Gray)
                                }
                                StatusBadge(status = status, lang = lang)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

// Teachers Tab (NO subjects; Photo + Edit + Delete)
@Composable
fun AdminTeachersContent(
    lang: String,
    teachers: List<Teacher>,
    onAddTeacher: () -> Unit,
    onEditTeacher: (Teacher) -> Unit,
    onDeleteTeacher: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                AppStrings.get("teachers", lang),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddTeacher,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_add_teacher_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.get("add", lang))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (teachers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(teachers) { teacher ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                photoBase64 = teacher.photoUrl,
                                name = teacher.fullName,
                                size = 48.dp,
                                accentColor = NavyPrimary
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    teacher.fullName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${teacher.phone} • ${teacher.email.ifBlank { "No email" }}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            IconButton(
                                onClick = { onEditTeacher(teacher) },
                                modifier = Modifier.testTag("edit_teacher_${teacher.teacherId}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = AppStrings.get("edit", lang), tint = BlueSecondary)
                            }

                            IconButton(
                                onClick = { onDeleteTeacher(teacher.teacherId) },
                                modifier = Modifier.testTag("delete_teacher_${teacher.teacherId}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Classes Tab (NO subjects; Registered teacher selection; Preview with students + Edit + Delete)
@Composable
fun AdminClassesContent(
    lang: String,
    classes: List<ClassRoom>,
    students: List<Student>,
    onAddClass: () -> Unit,
    onPreviewClass: (ClassRoom) -> Unit,
    onEditClass: (ClassRoom) -> Unit,
    onDeleteClass: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                AppStrings.get("classes", lang),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddClass,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_add_class_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.get("add", lang))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (classes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(classes) { classRoom ->
                    val studentCount = students.count { it.classId == classRoom.classId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPreviewClass(classRoom) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    classRoom.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BlueSurface
                                ) {
                                    Text(
                                        "${if (lang == "so") "Furaha" else "Password"}: ${classRoom.accessCode}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                "${AppStrings.get("class_teacher", lang)}: ${classRoom.classTeacherName.ifBlank { if (lang == "so") "Lama Xilsaarin" else "Not Assigned" }}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                "${AppStrings.get("total_students", lang)}: $studentCount • ${classRoom.academicYear}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onPreviewClass(classRoom) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).testTag("preview_class_${classRoom.classId}")
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.get("preview", lang), fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { onEditClass(classRoom) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).testTag("edit_class_${classRoom.classId}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = BlueSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.get("edit", lang), fontSize = 11.sp, color = BlueSecondary)
                                }

                                IconButton(
                                    onClick = { onDeleteClass(classRoom.classId) },
                                    modifier = Modifier.testTag("delete_class_${classRoom.classId}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedDanger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassPreviewDialog(
    classRoom: ClassRoom,
    students: List<Student>,
    lang: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("${AppStrings.get("class_preview", lang)}: ${classRoom.name}", fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text(
                    "${AppStrings.get("class_teacher", lang)}: ${classRoom.classTeacherName.ifBlank { "-" }} • Password: ${classRoom.accessCode}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column {
                Text(
                    "${AppStrings.get("students", lang)} (${students.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (students.isEmpty()) {
                    Text(
                        if (lang == "so") "Fasalakan weli arday laguma darin." else "No students enrolled in this class yet.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(students.size) { index ->
                            val st = students[index]
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarImage(photoBase64 = st.photoUrl, name = st.fullName, size = 34.dp, accentColor = GreenAccent)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${index + 1}. ${st.fullName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("ID: ${st.studentId} • Code: ${st.linkingCode}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

// Students Tab
@Composable
fun AdminStudentsContent(
    lang: String,
    students: List<Student>,
    classes: List<ClassRoom>,
    onAddStudent: () -> Unit,
    onEditStudent: (Student) -> Unit,
    onDeleteStudent: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf<String?>(null) }

    val filtered = students.filter { s ->
        (selectedClassFilter == null || s.classId == selectedClassFilter) &&
                (searchQuery.isBlank() || s.fullName.contains(searchQuery, ignoreCase = true) || s.studentId.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                AppStrings.get("students", lang),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddStudent,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_add_student_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.get("add", lang))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text(AppStrings.get("search", lang)) },
            modifier = Modifier.fillMaxWidth().testTag("admin_student_search"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedClassFilter == null,
                    onClick = { selectedClassFilter = null },
                    label = { Text(if (lang == "so") "Dhammaan" else "All") }
                )
            }
            items(classes) { c ->
                FilterChip(
                    selected = selectedClassFilter == c.classId,
                    onClick = { selectedClassFilter = c.classId },
                    label = { Text(c.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                photoBase64 = student.photoUrl,
                                name = student.fullName,
                                size = 44.dp,
                                accentColor = GreenAccent
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    student.fullName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "ID: ${student.studentId} • ${student.className}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFEFCBF),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        "Linking Code: ${student.linkingCode}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF744210),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(onClick = { onEditStudent(student) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BlueSecondary)
                            }
                            IconButton(
                                onClick = { onDeleteStudent(student.studentId) },
                                modifier = Modifier.testTag("delete_student_${student.studentId}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Examinations Tab: Admin Creates Exam Folders -> All Class Exams Collect Inside -> Inspect Each Student & Report Card
@Composable
fun AdminExamsContent(
    lang: String,
    folders: List<ExamFolder>,
    exams: List<Examination>,
    onCreateFolder: () -> Unit,
    onDeleteFolder: (String) -> Unit,
    onOpenFolder: (ExamFolder) -> Unit,
    onOpenExamStudents: (Examination) -> Unit
) {
    val unassignedExams = remember(folders, exams) {
        val folderIds = folders.map { it.folderId }.toSet()
        exams.filter { it.folderId.isBlank() || it.folderId !in folderIds }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    AppStrings.get("exam_folders", lang),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (lang == "so")
                        "Abuur folder-ka imtixaanka; fasallada oo dhan gudaha ayay ku soo aruurayaan"
                    else
                        "Create exam folders where all class submissions & student reports gather",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onCreateFolder,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_create_exam_folder_button")
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.get("add", lang))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (folders.isEmpty() && unassignedExams.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(folders) { folder ->
                    val folderExams = exams.filter { it.folderId == folder.folderId }
                    val submittedCount = folderExams.count { it.status == "SUBMITTED" || it.status == "PUBLISHED" || it.status == "LOCKED" }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFolder(folder) }
                            .testTag("exam_folder_${folder.folderId}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmberWarning.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFB7791F), modifier = Modifier.size(30.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(folder.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                Text(
                                    "${folder.examType} • ${folder.academicYear}",
                                    fontSize = 12.sp,
                                    color = BlueSecondary
                                )
                                Text(
                                    if (lang == "so")
                                        "Fasallada: ${folderExams.size} (${submittedCount} la soo gudbiyay)"
                                    else
                                        "Classes: ${folderExams.size} (${submittedCount} submitted)",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            IconButton(onClick = { onDeleteFolder(folder.folderId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Folder", tint = RedDanger)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NavyPrimary)
                        }
                    }
                }

                if (unassignedExams.isNotEmpty()) {
                    item {
                        Text(
                            if (lang == "so") "Imtixaannada Aan Folder Lahayn" else "Other Class Examinations",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(unassignedExams) { exam ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenExamStudents(exam) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exam.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                                    Text("${exam.className} • ${exam.academicYear}", fontSize = 12.sp, color = Color.Gray)
                                }
                                StatusBadge(status = exam.status, lang = lang)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminExamFolderDetailDialog(
    folder: ExamFolder,
    folderExams: List<Examination>,
    lang: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onInspectExamStudents: (Examination) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFFB7791F))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(folder.name, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 16.sp)
                    Text("${folder.examType} • ${folder.academicYear}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        },
        text = {
            if (folderExams.isEmpty()) {
                Text(
                    if (lang == "so")
                        "Weli macallimiintu fasal imtixaan uma soo dirin folder-kan."
                    else
                        "No class exams have been submitted into this folder yet.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(folderExams) { exam ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exam.className, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = NavyPrimary)
                                        Text(exam.name, fontSize = 12.sp, color = Color.DarkGray)
                                    }
                                    StatusBadge(status = exam.status, lang = lang)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onInspectExamStudents(exam) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (lang == "so") "Ardayda & Report" else "Students & Reports",
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (exam.status != "PUBLISHED") {
                                        Button(
                                            onClick = { viewModel.updateExamStatus(exam.examId, "PUBLISHED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(0.8f)
                                        ) {
                                            Text(AppStrings.get("publish", lang), fontSize = 11.sp)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { viewModel.updateExamStatus(exam.examId, "LOCKED") },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(0.8f)
                                        ) {
                                            Text(AppStrings.get("lock", lang), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
fun AdminClassExamStudentsDialog(
    exam: Examination,
    students: List<Student>,
    school: SchoolConfig,
    lang: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onOpenStudentReport: (Student, Examination, ExamResult) -> Unit
) {
    val resultsState by viewModel.repository.observeExamResults(exam.examId).collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("${exam.className} • ${exam.name}", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 15.sp)
                Text(
                    if (lang == "so") "Taabo arday kasta si aad u aragto report kiisa" else "Tap any student to view their full report card",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            val studentEntries = if (resultsState.isNotEmpty()) {
                resultsState.sortedBy { it.position }.map { res ->
                    val st = students.firstOrNull { it.studentId == res.studentId }
                        ?: Student(studentId = res.studentId, fullName = res.studentName, classId = res.classId, className = res.className)
                    st to res
                }
            } else {
                students.mapIndexed { i, st ->
                    st to ExamResult(
                        examId = exam.examId,
                        examName = exam.name,
                        studentId = st.studentId,
                        studentName = st.fullName,
                        classId = st.classId,
                        className = st.className,
                        position = (i + 1).toLong()
                    )
                }
            }

            if (studentEntries.isEmpty()) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray, fontSize = 13.sp)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(studentEntries) { (st, res) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenStudentReport(st, exam, res) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AmberWarning.copy(alpha = 0.2f),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("#${res.position}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFB7791F))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                AvatarImage(photoBase64 = st.photoUrl, name = st.fullName, size = 34.dp, accentColor = NavyPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "${res.totalObtained.toInt()}/${res.totalMax.toInt()} (${String.format(Locale.US, "%.1f%%", res.percentage)}) • Grade ${res.grade}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Icon(Icons.Default.Assessment, contentDescription = "View Report", tint = BlueSecondary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
fun AdminStudentReportDialog(
    student: Student,
    exam: Examination,
    result: ExamResult,
    school: SchoolConfig,
    lang: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(AppStrings.get("view_report_card", lang), fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 460.dp)) {
                item {
                    BeautifulReportCardView(
                        school = school,
                        student = student,
                        exam = exam,
                        result = result,
                        lang = lang
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val pdfFile = ReportCardPdfGenerator.generateReportCard(
                                context = context,
                                school = school,
                                student = student,
                                exam = exam,
                                result = result,
                                language = lang
                            )
                            if (pdfFile != null) {
                                ReportCardPdfGenerator.shareReportCardPdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppStrings.get("download_pdf", lang))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

// Fees Content
@Composable
fun AdminFeesContent(
    lang: String,
    feePayments: List<FeePayment>,
    onRecordPayment: (String, Double, String) -> Unit
) {
    var showRecordPaymentDialog by remember { mutableStateOf<FeePayment?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            AppStrings.get("fees", lang),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (feePayments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(feePayments) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(p.studentName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                StatusBadge(status = p.status, lang = lang)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("${p.feeName} • ${p.className}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${AppStrings.get("amount", lang)}: $${p.amount}", fontSize = 12.sp, color = Color.Gray)
                                Text("${AppStrings.get("amount_paid", lang)}: $${p.amountPaid}", fontSize = 12.sp, color = GreenAccent, fontWeight = FontWeight.Bold)
                                Text("${AppStrings.get("balance", lang)}: $${p.balance}", fontSize = 12.sp, color = if (p.balance > 0) RedDanger else Color.Gray, fontWeight = FontWeight.Bold)
                            }

                            if (p.balance > 0) {
                                Button(
                                    onClick = { showRecordPaymentDialog = p },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End).padding(top = 6.dp)
                                ) {
                                    Text(AppStrings.get("record_payment", lang), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRecordPaymentDialog != null) {
        val p = showRecordPaymentDialog!!
        var amtStr by remember { mutableStateOf(p.balance.toString()) }
        var receiptStr by remember { mutableStateOf("RCP-${System.currentTimeMillis() % 100000}") }

        AlertDialog(
            onDismissRequest = { showRecordPaymentDialog = null },
            title = { Text(AppStrings.get("record_payment", lang)) },
            text = {
                Column {
                    Text("${p.studentName} • ${p.feeName}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amtStr,
                        onValueChange = { amtStr = it },
                        label = { Text(AppStrings.get("amount", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = receiptStr,
                        onValueChange = { receiptStr = it },
                        label = { Text(AppStrings.get("receipt_no", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amtStr.toDoubleOrNull() ?: p.balance
                        onRecordPayment(p.paymentId, amt, receiptStr)
                        showRecordPaymentDialog = null
                    }
                ) {
                    Text(AppStrings.get("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordPaymentDialog = null }) { Text(AppStrings.get("cancel", lang)) }
            }
        )
    }
}

// Reports Tab
@Composable
fun AdminReportsContent(
    lang: String,
    school: SchoolConfig,
    students: List<Student>,
    teachers: List<Teacher>,
    classes: List<ClassRoom>,
    exams: List<Examination>,
    feePayments: List<FeePayment>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(AppStrings.get("reports", lang), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("${school.name} • ${school.academicYear}", fontSize = 12.sp, color = Color.Gray)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (lang == "so") "Warbixinta Guud ee Dugsiga" else "Executive Summary Report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ReportMetricRow("Total Enrolled Students", "${students.size}")
                    ReportMetricRow("Total Employed Teachers", "${teachers.size}")
                    ReportMetricRow("Total Active Classes", "${classes.size}")
                    ReportMetricRow("Total Examinations Created", "${exams.size}")
                    val totalFeeBilled = feePayments.sumOf { it.amount }
                    val totalFeeCollected = feePayments.sumOf { it.amountPaid }
                    val totalFeeOutstanding = feePayments.sumOf { it.balance }
                    ReportMetricRow("Total Fees Billed", "$$totalFeeBilled")
                    ReportMetricRow("Total Fees Collected", "$$totalFeeCollected")
                    ReportMetricRow("Total Outstanding Balance", "$$totalFeeOutstanding")
                }
            }
        }
    }
}

@Composable
fun ReportMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Dialog: Add / Edit Teacher (NO subjects field; WITH Gallery+Camera photo picker)
@Composable
fun TeacherFormDialog(
    lang: String,
    existingTeacher: Teacher?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember(existingTeacher) { mutableStateOf(existingTeacher?.fullName ?: "") }
    var phone by remember(existingTeacher) { mutableStateOf(existingTeacher?.phone ?: "") }
    var email by remember(existingTeacher) { mutableStateOf(existingTeacher?.email ?: "") }
    var photoBase64 by remember(existingTeacher) { mutableStateOf(existingTeacher?.photoUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                AppStrings.get("teachers", lang) + " - " +
                    AppStrings.get(if (existingTeacher == null) "add" else "edit", lang)
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PhotoPickerSection(
                        photoBase64 = photoBase64,
                        onPhotoChanged = { photoBase64 = it },
                        label = AppStrings.get("teacher_photo", lang),
                        lang = lang
                    )
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(AppStrings.get("full_name", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(AppStrings.get("phone", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(AppStrings.get("email", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), phone.trim(), email.trim(), photoBase64)
                    }
                }
            ) {
                Text(AppStrings.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang)) }
        }
    )
}

// Dialog: Add / Edit Class (NO subjects field; select from registered teachers)
@Composable
fun ClassFormDialog(
    lang: String,
    teachers: List<Teacher>,
    existingClass: ClassRoom?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var className by remember(existingClass) { mutableStateOf(existingClass?.name ?: "") }
    var selectedTeacherId by remember(existingClass) { mutableStateOf(existingClass?.classTeacherId ?: "") }
    var selectedTeacherName by remember(existingClass) { mutableStateOf(existingClass?.classTeacherName ?: "") }
    var academicYear by remember(existingClass) { mutableStateOf(existingClass?.academicYear ?: "2025-2026") }
    var accessCode by remember(existingClass) { mutableStateOf(existingClass?.accessCode ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                AppStrings.get("classes", lang) + " - " +
                    AppStrings.get(if (existingClass == null) "add" else "edit", lang)
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text(AppStrings.get("class_name", lang)) },
                        placeholder = { Text("e.g. Form 1A, Grade 8") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        AppStrings.get("select_teacher", lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (teachers.isEmpty()) {
                        Text(
                            if (lang == "so") "Fadlan marka hore diiwaangeli macallimiinta." else "Please register teachers first.",
                            fontSize = 12.sp,
                            color = RedDanger
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(teachers) { teacher ->
                                val isSelected = selectedTeacherId == teacher.teacherId ||
                                    (selectedTeacherId.isBlank() && selectedTeacherName == teacher.fullName)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTeacherId = teacher.teacherId
                                        selectedTeacherName = teacher.fullName
                                    },
                                    leadingIcon = {
                                        AvatarImage(photoBase64 = teacher.photoUrl, name = teacher.fullName, size = 22.dp)
                                    },
                                    label = { Text(teacher.fullName, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text(AppStrings.get("academic_year", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = { accessCode = it },
                        label = { Text(AppStrings.get("class_access_code", lang)) },
                        placeholder = { Text(if (lang == "so") "Tusaale: 4819 (ikhtiyaari)" else "e.g. 4819 (auto if blank)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (className.isNotBlank()) {
                        onConfirm(className.trim(), selectedTeacherId, selectedTeacherName, academicYear.trim(), accessCode.trim())
                    }
                }
            ) {
                Text(AppStrings.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang)) }
        }
    )
}

// Dialog: Add / Edit Student (Optional photo via Gallery or Camera)
@Composable
fun StudentFormDialog(
    lang: String,
    classes: List<ClassRoom>,
    existingStudent: Student?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember(existingStudent) { mutableStateOf(existingStudent?.fullName ?: "") }
    var father by remember(existingStudent) { mutableStateOf(existingStudent?.fatherName ?: "") }
    var mother by remember(existingStudent) { mutableStateOf(existingStudent?.motherName ?: "") }
    var phone by remember(existingStudent) { mutableStateOf(existingStudent?.parentPhone ?: "") }
    var gender by remember(existingStudent) { mutableStateOf(existingStudent?.gender ?: "M") }
    var selectedClassId by remember(existingStudent) { mutableStateOf(existingStudent?.classId ?: classes.firstOrNull()?.classId ?: "") }
    var selectedClassName by remember(existingStudent) { mutableStateOf(existingStudent?.className ?: classes.firstOrNull()?.name ?: "") }
    var admDate by remember(existingStudent) { mutableStateOf(existingStudent?.admissionDate?.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) } ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var photoBase64 by remember(existingStudent) { mutableStateOf(existingStudent?.photoUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("students", lang) + " - " + AppStrings.get(if (existingStudent == null) "add" else "edit", lang)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PhotoPickerSection(
                        photoBase64 = photoBase64,
                        onPhotoChanged = { photoBase64 = it },
                        label = AppStrings.get("photo_optional", lang),
                        lang = lang
                    )
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(AppStrings.get("full_name", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = father,
                        onValueChange = { father = it },
                        label = { Text(AppStrings.get("father_name", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = mother,
                        onValueChange = { mother = it },
                        label = { Text(AppStrings.get("mother_name", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(AppStrings.get("parent_phone", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(AppStrings.get("gender", lang), fontSize = 12.sp)
                    Row {
                        FilterChip(
                            selected = gender == "M",
                            onClick = { gender = "M" },
                            label = { Text(AppStrings.get("male", lang)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = gender == "F",
                            onClick = { gender = "F" },
                            label = { Text(AppStrings.get("female", lang)) }
                        )
                    }
                }
                item {
                    Text(AppStrings.get("class", lang), fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(classes) { c ->
                            FilterChip(
                                selected = selectedClassId == c.classId,
                                onClick = {
                                    selectedClassId = c.classId
                                    selectedClassName = c.name
                                },
                                label = { Text(c.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), father.trim(), mother.trim(), phone.trim(), gender, selectedClassId, selectedClassName, admDate, photoBase64)
                    }
                }
            ) {
                Text(AppStrings.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang)) }
        }
    )
}

// Dialog: Create Exam Folder
@Composable
fun CreateExamFolderDialog(
    lang: String,
    academicYear: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MIDTERM") }
    var year by remember { mutableStateOf(academicYear) }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("create_exam_folder", lang), fontWeight = FontWeight.Bold, color = NavyPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text(AppStrings.get("folder_name", lang)) },
                    placeholder = { Text("e.g. Midterm Term 1 / Final 2026") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(AppStrings.get("exam_type", lang), fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item { FilterChip(selected = type == "MONTHLY", onClick = { type = "MONTHLY" }, label = { Text("Monthly") }) }
                    item { FilterChip(selected = type == "MIDTERM", onClick = { type = "MIDTERM" }, label = { Text("Midterm") }) }
                    item { FilterChip(selected = type == "FINAL", onClick = { type = "FINAL" }, label = { Text("Final") }) }
                }
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text(AppStrings.get("academic_year", lang)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(AppStrings.get("description", lang)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName.trim(), type, year.trim(), desc.trim())
                    }
                }
            ) {
                Text(AppStrings.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang)) }
        }
    )
}

// Dialog: Settings
@Composable
fun AdminSettingsDialog(
    school: SchoolConfig,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(school.name) }
    var address by remember { mutableStateOf(school.address) }
    var phone by remember { mutableStateOf(school.phone) }
    var email by remember { mutableStateOf(school.email) }
    var year by remember { mutableStateOf(school.academicYear) }
    var passcode by remember { mutableStateOf(school.adminPasscode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("settings", lang)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("School Name") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Academic Year") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = passcode, onValueChange = { passcode = it }, label = { Text("Master Admin Passcode") }, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, address, phone, email, year, passcode) }) {
                Text(AppStrings.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang)) }
        }
    )
}
