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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun TeacherDashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    val classes by viewModel.classes.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val unlockedClassIds by viewModel.unlockedClassIds.collectAsState()
    val allStudents by viewModel.students.collectAsState()
    val examFolders by viewModel.examFolders.collectAsState()
    val allExams by viewModel.examinations.collectAsState()
    val allFees by viewModel.fees.collectAsState()
    val allFeePayments by viewModel.feePayments.collectAsState()

    val isCurrentClassUnlocked = selectedClass != null && selectedClass!!.classId in unlockedClassIds

    // Filter by selected class
    val classStudents = remember(allStudents, selectedClass) {
        if (selectedClass != null) allStudents.filter { it.classId == selectedClass?.classId } else emptyList()
    }
    val classExams = remember(allExams, selectedClass) {
        if (selectedClass != null) allExams.filter { it.classId == selectedClass?.classId } else emptyList()
    }
    val classFees = remember(allFees, selectedClass) {
        if (selectedClass != null) allFees.filter { it.classId == selectedClass?.classId } else emptyList()
    }
    val classFeePayments = remember(allFeePayments, selectedClass) {
        if (selectedClass != null) allFeePayments.filter { it.classId == selectedClass?.classId } else emptyList()
    }

    // Dialog States
    var showRegisterStudentDialog by remember { mutableStateOf(false) }
    var editStudentTarget by remember { mutableStateOf<Student?>(null) }
    var showCreateExamDialog by remember { mutableStateOf(false) }
    var showEnterMarksExam by remember { mutableStateOf<Examination?>(null) }
    var showAllReportCardsExam by remember { mutableStateOf<Examination?>(null) }
    var showCreateFeeDialog by remember { mutableStateOf(false) }
    var openedFeeFolder by remember { mutableStateOf<Fee?>(null) }
    var showRemarkDialogStudent by remember { mutableStateOf<Student?>(null) }

    // Class switch password prompt state
    var pendingUnlockClass by remember { mutableStateOf<ClassRoom?>(null) }

    Scaffold(
        topBar = {
            Column {
                SchoolTopBar(
                    title = schoolConfig.name,
                    subtitle = AppStrings.get("teacher_portal", lang),
                    currentLang = lang,
                    onToggleLang = { viewModel.setLanguage(if (lang == "so") "en" else "so") },
                    onSignOut = { viewModel.signOut { onBack() } },
                    onBack = onBack
                )

                // Class Selector Bar (with Lock/Unlock indicator)
                Surface(
                    color = BlueSurface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${AppStrings.get("class", lang)}: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary
                        )

                        if (classes.isEmpty()) {
                            Text(if (lang == "so") "Weli fasal lama abuurin" else "No classes created yet", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(classes) { c ->
                                    val unlocked = c.classId in unlockedClassIds
                                    FilterChip(
                                        selected = selectedClass?.classId == c.classId && unlocked,
                                        onClick = {
                                            if (unlocked) {
                                                viewModel.selectTeacherClass(c)
                                            } else {
                                                pendingUnlockClass = c
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (unlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        },
                                        label = { Text(c.name, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NavyPrimary,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = AmberWarning
                                        ),
                                        modifier = Modifier.testTag("teacher_class_chip_${c.classId}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (isCurrentClassUnlocked) {
                TeacherBottomNavigation(
                    currentTab = currentTab,
                    lang = lang,
                    onSelectTab = { viewModel.selectTab(it) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
        ) {
            if (!isCurrentClassUnlocked) {
                // MANDATORY Class Selection + Password Gate
                ClassPasswordGateScreen(
                    lang = lang,
                    classes = classes,
                    unlockedClassIds = unlockedClassIds,
                    preselectedClass = pendingUnlockClass ?: selectedClass,
                    onUnlockSuccess = { c ->
                        pendingUnlockClass = null
                        viewModel.selectTeacherClass(c)
                    },
                    onVerifyPassword = { c, pw ->
                        viewModel.unlockClass(c, pw)
                    }
                )
            } else {
                when (currentTab) {
                    "DASHBOARD" -> TeacherOverviewContent(
                        lang = lang,
                        selectedClass = selectedClass,
                        studentsCount = classStudents.size,
                        examsCount = classExams.size,
                        feesCount = classFees.size,
                        onOpenAttendance = { viewModel.selectTab("ATTENDANCE") },
                        onOpenRegisterStudent = {
                            editStudentTarget = null
                            showRegisterStudentDialog = true
                        },
                        onOpenCreateExam = { showCreateExamDialog = true },
                        onOpenFees = { viewModel.selectTab("FEES") }
                    )
                    "STUDENTS" -> TeacherStudentsContent(
                        lang = lang,
                        students = classStudents,
                        onRegisterStudent = {
                            editStudentTarget = null
                            showRegisterStudentDialog = true
                        },
                        onEditStudent = {
                            editStudentTarget = it
                            showRegisterStudentDialog = true
                        },
                        onWriteRemark = { showRemarkDialogStudent = it }
                    )
                    "ATTENDANCE" -> TeacherAttendanceContent(
                        lang = lang,
                        selectedClass = selectedClass,
                        students = classStudents,
                        viewModel = viewModel
                    )
                    "EXAMS" -> TeacherExamsContent(
                        lang = lang,
                        exams = classExams,
                        students = classStudents,
                        onCreateExam = { showCreateExamDialog = true },
                        onEnterMarks = { showEnterMarksExam = it },
                        onViewAllReportCards = { exam ->
                            showAllReportCardsExam = exam
                        }
                    )
                    "FEES" -> TeacherFeesContent(
                        lang = lang,
                        fees = classFees,
                        students = classStudents,
                        feePayments = classFeePayments,
                        onCreateFee = { showCreateFeeDialog = true },
                        onOpenFeeFolder = { openedFeeFolder = it }
                    )
                    else -> TeacherOverviewContent(
                        lang = lang,
                        selectedClass = selectedClass,
                        studentsCount = classStudents.size,
                        examsCount = classExams.size,
                        feesCount = classFees.size,
                        onOpenAttendance = { viewModel.selectTab("ATTENDANCE") },
                        onOpenRegisterStudent = {
                            editStudentTarget = null
                            showRegisterStudentDialog = true
                        },
                        onOpenCreateExam = { showCreateExamDialog = true },
                        onOpenFees = { viewModel.selectTab("FEES") }
                    )
                }
            }
        }
    }

    // Class Switch Password Dialog when clicking a locked chip in top bar
    if (pendingUnlockClass != null && isCurrentClassUnlocked) {
        val target = pendingUnlockClass!!
        var enteredPw by remember(target) { mutableStateOf("") }
        var pwError by remember(target) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { pendingUnlockClass = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${target.name} • ${AppStrings.get("class_access_code", lang)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        if (lang == "so") "Geli furaha sirta ee fasalka ${target.name}:" else "Enter the access password for ${target.name}:",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPw,
                        onValueChange = {
                            enteredPw = it
                            pwError = false
                        },
                        label = { Text(AppStrings.get("class_access_code", lang)) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pwError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pwError) {
                        Text(AppStrings.get("invalid_passcode", lang), color = RedDanger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.unlockClass(target, enteredPw)) {
                            pendingUnlockClass = null
                        } else {
                            pwError = true
                        }
                    }
                ) {
                    Text(AppStrings.get("unlock_class", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUnlockClass = null }) { Text(AppStrings.get("cancel", lang)) }
            }
        )
    }

    // Register / Edit Student Dialog (Optional Photo via Gallery or Camera)
    if (showRegisterStudentDialog && selectedClass != null) {
        TeacherRegisterStudentDialog(
            lang = lang,
            classRoom = selectedClass!!,
            existingStudent = editStudentTarget,
            onDismiss = {
                showRegisterStudentDialog = false
                editStudentTarget = null
            },
            onConfirm = { name, father, mother, phone, gender, admDate, photoBase64 ->
                if (editStudentTarget != null) {
                    viewModel.updateStudent(
                        editStudentTarget!!.copy(
                            fullName = name,
                            fatherName = father,
                            motherName = mother,
                            parentPhone = phone,
                            gender = gender,
                            admissionDate = admDate,
                            photoUrl = photoBase64
                        )
                    )
                } else {
                    viewModel.addStudent(
                        fullName = name,
                        fatherName = father,
                        motherName = mother,
                        phone = phone,
                        gender = gender,
                        classId = selectedClass!!.classId,
                        className = selectedClass!!.name,
                        admissionDate = admDate,
                        photoUrl = photoBase64
                    )
                }
                showRegisterStudentDialog = false
                editStudentTarget = null
            }
        )
    }

    // Create Exam Dialog (with Admin ExamFolder picker)
    if (showCreateExamDialog && selectedClass != null) {
        TeacherCreateExamDialog(
            lang = lang,
            classRoom = selectedClass!!,
            folders = examFolders,
            onDismiss = { showCreateExamDialog = false },
            onConfirm = { name, type, subs, start, end, desc, folderId, folderName ->
                viewModel.createExamination(
                    name = name,
                    type = type,
                    classId = selectedClass!!.classId,
                    className = selectedClass!!.name,
                    subjects = subs,
                    startDate = start,
                    endDate = end,
                    desc = desc,
                    folderId = folderId,
                    folderName = folderName
                )
                showCreateExamDialog = false
            }
        )
    }

    // Enter Marks Dialog (includes per-student teacherRemarks explaining what the student deserves)
    if (showEnterMarksExam != null) {
        TeacherEnterMarksDialog(
            exam = showEnterMarksExam!!,
            students = classStudents,
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showEnterMarksExam = null }
        )
    }

    // All-Students Report Cards Dialog (Beautiful Table UI + PDF Download for EVERY student)
    if (showAllReportCardsExam != null) {
        TeacherAllReportCardsDialog(
            exam = showAllReportCardsExam!!,
            students = classStudents,
            school = schoolConfig,
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showAllReportCardsExam = null }
        )
    }

    // Create Class Fee Folder Dialog
    if (showCreateFeeDialog && selectedClass != null) {
        TeacherCreateFeeDialog(
            lang = lang,
            classRoom = selectedClass!!,
            onDismiss = { showCreateFeeDialog = false },
            onConfirm = { name, amount, dueDate, desc ->
                viewModel.addFee(
                    feeName = name,
                    classId = selectedClass!!.classId,
                    className = selectedClass!!.name,
                    amount = amount,
                    dueDate = dueDate,
                    desc = desc
                )
                showCreateFeeDialog = false
            }
        )
    }

    // Opened Fee Folder Dialog ("Gudbi" button in top corner + every student with "Bixiyay" button)
    if (openedFeeFolder != null) {
        val currentFee = classFees.firstOrNull { it.feeId == openedFeeFolder!!.feeId } ?: openedFeeFolder!!
        TeacherFeeFolderDialog(
            fee = currentFee,
            students = classStudents,
            payments = classFeePayments.filter { it.feeId == currentFee.feeId },
            lang = lang,
            onDismiss = { openedFeeFolder = null },
            onSubmitFeeToParents = {
                viewModel.publishFeeFolder(currentFee)
            },
            onMarkStudentPaid = { paymentId, amount, receiptNo ->
                viewModel.recordFeePayment(paymentId, amount, receiptNo)
            }
        )
    }

    // Write Remark Dialog
    if (showRemarkDialogStudent != null) {
        val st = showRemarkDialogStudent!!
        TeacherWriteRemarkDialog(
            student = st,
            lang = lang,
            onDismiss = { showRemarkDialogStudent = null },
            onConfirm = { cat, msg ->
                viewModel.addRemark(st.studentId, st.fullName, st.classId, cat, msg)
                showRemarkDialogStudent = null
            }
        )
    }
}

@Composable
fun ClassPasswordGateScreen(
    lang: String,
    classes: List<ClassRoom>,
    unlockedClassIds: Set<String>,
    preselectedClass: ClassRoom?,
    onUnlockSuccess: (ClassRoom) -> Unit,
    onVerifyPassword: (ClassRoom, String) -> Boolean
) {
    var pickedClass by remember(preselectedClass, classes) {
        mutableStateOf(preselectedClass ?: classes.firstOrNull())
    }
    var password by remember(pickedClass) { mutableStateOf("") }
    var showError by remember(pickedClass) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(NavyPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LockPerson, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(38.dp))
            }
        }

        item {
            Text(
                text = AppStrings.get("enter_class_password", lang),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = if (lang == "so")
                    "Macallin kasta waa inuu doortaa fasalkiisa kadibna uu geliyaa furaha sirta ah ee fasalkaas si uu u galo."
                else
                    "Select your assigned class and enter its class password to access attendance, students, exams, and fees.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (lang == "so") "1. Dooro Fasalka:" else "1. Select Class:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyPrimary
                    )

                    if (classes.isEmpty()) {
                        Text(
                            if (lang == "so") "Maamulaha weli ma abuurin fasallo." else "No classes available yet. Contact Admin.",
                            fontSize = 13.sp,
                            color = RedDanger
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(classes) { c ->
                                val isSelected = pickedClass?.classId == c.classId
                                val unlocked = c.classId in unlockedClassIds
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        pickedClass = c
                                        showError = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (unlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(c.name, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }
                    }

                    if (pickedClass != null) {
                        Text(
                            "${AppStrings.get("class_teacher", lang)}: ${pickedClass!!.classTeacherName.ifBlank { "-" }}",
                            fontSize = 12.sp,
                            color = BlueSecondary
                        )

                        Text(
                            if (lang == "so") "2. Geli Furaha Sirta Fasalka (${pickedClass!!.name}):" else "2. Enter Password for ${pickedClass!!.name}:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyPrimary
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                showError = false
                            },
                            label = { Text(AppStrings.get("class_access_code", lang)) },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = showError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_class_password_input")
                        )

                        if (showError) {
                            Text(
                                AppStrings.get("invalid_passcode", lang),
                                color = RedDanger,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                val target = pickedClass ?: return@Button
                                if (onVerifyPassword(target, password)) {
                                    onUnlockSuccess(target)
                                } else {
                                    showError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("teacher_unlock_class_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(AppStrings.get("unlock_class", lang), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherBottomNavigation(
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
            modifier = Modifier.testTag("teacher_nav_dashboard")
        )
        NavigationBarItem(
            selected = currentTab == "STUDENTS",
            onClick = { onSelectTab("STUDENTS") },
            icon = { Icon(Icons.Default.School, contentDescription = null) },
            label = { Text(AppStrings.get("students", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("teacher_nav_students")
        )
        NavigationBarItem(
            selected = currentTab == "ATTENDANCE",
            onClick = { onSelectTab("ATTENDANCE") },
            icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
            label = { Text(AppStrings.get("attendance", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("teacher_nav_attendance")
        )
        NavigationBarItem(
            selected = currentTab == "EXAMS",
            onClick = { onSelectTab("EXAMS") },
            icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
            label = { Text(AppStrings.get("exams", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("teacher_nav_exams")
        )
        NavigationBarItem(
            selected = currentTab == "FEES",
            onClick = { onSelectTab("FEES") },
            icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            label = { Text(AppStrings.get("fees", lang), fontSize = 10.sp) },
            modifier = Modifier.testTag("teacher_nav_fees")
        )
    }
}

// Teacher Overview (NO Homework)
@Composable
fun TeacherOverviewContent(
    lang: String,
    selectedClass: ClassRoom?,
    studentsCount: Int,
    examsCount: Int,
    feesCount: Int,
    onOpenAttendance: () -> Unit,
    onOpenRegisterStudent: () -> Unit,
    onOpenCreateExam: () -> Unit,
    onOpenFees: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = selectedClass?.name ?: "No Class Selected",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${AppStrings.get("academic_year", lang)}: ${selectedClass?.academicYear ?: "2025-2026"}",
                        fontSize = 12.sp,
                        color = BlueLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$studentsCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(AppStrings.get("total_students", lang), fontSize = 11.sp, color = BlueLight)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$examsCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                            Text(AppStrings.get("exams", lang), fontSize = 11.sp, color = BlueLight)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$feesCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GreenLight)
                            Text(AppStrings.get("fees", lang), fontSize = 11.sp, color = BlueLight)
                        }
                    }
                }
            }
        }

        item {
            Text(
                AppStrings.get("quick_actions", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenAttendance,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.get("attendance", lang), fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenRegisterStudent,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.get("add", lang), fontSize = 12.sp)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenCreateExam,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (lang == "so") "Abuur Imtixaan" else "Create Exam", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenFees,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.get("fee_folders", lang), fontSize = 12.sp)
                }
            }
        }
    }
}

// Teacher Students List (with Avatar photo & Edit button)
@Composable
fun TeacherStudentsContent(
    lang: String,
    students: List<Student>,
    onRegisterStudent: () -> Unit,
    onEditStudent: (Student) -> Unit,
    onWriteRemark: (Student) -> Unit
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
            Text("${AppStrings.get("students", lang)} (${students.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onRegisterStudent,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.get("add", lang))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(students) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarImage(
                                    photoBase64 = student.photoUrl,
                                    name = student.fullName,
                                    size = 44.dp,
                                    accentColor = BlueSecondary
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("ID: ${student.studentId} • ${student.parentPhone}", fontSize = 12.sp, color = Color.Gray)
                                }

                                IconButton(onClick = { onEditStudent(student) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Student", tint = BlueSecondary)
                                }

                                Button(
                                    onClick = { onWriteRemark(student) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.get("remarks", lang), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEFCBF)
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
                    }
                }
            }
        }
    }
}

// Attendance Screen
@Composable
fun TeacherAttendanceContent(
    lang: String,
    selectedClass: ClassRoom?,
    students: List<Student>,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    val attendanceMap = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(selectedClass, selectedDate, students) {
        if (selectedClass != null) {
            val existing = viewModel.repository.getAttendance(selectedClass.classId, selectedDate)
            attendanceMap.clear()
            if (existing != null) {
                existing.records.forEach { attendanceMap[it.studentId] = it.status }
            } else {
                students.forEach { attendanceMap[it.studentId] = "PRESENT" }
            }
        }
    }

    val presentCount = attendanceMap.values.count { it == "PRESENT" }
    val absentCount = attendanceMap.values.count { it == "ABSENT" }
    val lateCount = attendanceMap.values.count { it == "LATE" }
    val excusedCount = attendanceMap.values.count { it == "EXCUSED" }
    val totalCount = students.size
    val rate = if (totalCount > 0) (presentCount.toDouble() / totalCount * 100) else 0.0

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
            Column {
                Text(AppStrings.get("attendance", lang), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${AppStrings.get("date", lang)}: ${formatDayAndDate(selectedDate, lang)}", fontSize = 12.sp, color = Color.Gray)
            }

            Button(
                onClick = { students.forEach { attendanceMap[it.studentId] = "PRESENT" } },
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("mark_all_present_button")
            ) {
                Text(AppStrings.get("mark_all_present", lang), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("${AppStrings.get("present", lang)}: $presentCount", color = GreenAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${AppStrings.get("absent", lang)}: $absentCount", color = RedDanger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${AppStrings.get("late", lang)}: $lateCount", color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${AppStrings.get("excused", lang)}: $excusedCount", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(String.format(Locale.US, "%.0f%%", rate), color = NavyPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students) { student ->
                val currentStatus = attendanceMap[student.studentId] ?: "PRESENT"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(student.studentId, fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AttendanceStatusButton("P", currentStatus == "PRESENT", GreenAccent) {
                                attendanceMap[student.studentId] = "PRESENT"
                            }
                            AttendanceStatusButton("A", currentStatus == "ABSENT", RedDanger) {
                                attendanceMap[student.studentId] = "ABSENT"
                            }
                            AttendanceStatusButton("L", currentStatus == "LATE", AmberWarning) {
                                attendanceMap[student.studentId] = "LATE"
                            }
                            AttendanceStatusButton("E", currentStatus == "EXCUSED", Color.Gray) {
                                attendanceMap[student.studentId] = "EXCUSED"
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (selectedClass != null) {
                    val records = students.map { s ->
                        AttendanceRecord(
                            studentId = s.studentId,
                            studentName = s.fullName,
                            status = attendanceMap[s.studentId] ?: "PRESENT"
                        )
                    }
                    viewModel.markAttendance(selectedClass.classId, selectedClass.name, selectedDate, records)
                    Toast.makeText(context, AppStrings.get("success_saved", lang), Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_attendance_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(AppStrings.get("save", lang), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttendanceStatusButton(
    shortLabel: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) color else Color(0xFFEDF2F7),
        modifier = Modifier.size(34.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                shortLabel,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// Teacher Exams Tab (Report Cards button opens ALL students in class, not just 1!)
@Composable
fun TeacherExamsContent(
    lang: String,
    exams: List<Examination>,
    students: List<Student>,
    onCreateExam: () -> Unit,
    onEnterMarks: (Examination) -> Unit,
    onViewAllReportCards: (Examination) -> Unit
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
            Text(AppStrings.get("exams", lang), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onCreateExam,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.get("add", lang))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (exams.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(exams) { exam ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exam.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                                    if (exam.folderName.isNotBlank()) {
                                        Text("Folder: ${exam.folderName}", fontSize = 11.sp, color = BlueSecondary)
                                    }
                                }
                                StatusBadge(status = exam.status, lang = lang)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "${exam.examType} • ${exam.subjects.size} ${AppStrings.get("subjects", lang)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onEnterMarks(exam) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.get("enter_marks", lang), fontSize = 11.sp)
                                }

                                if (students.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { onViewAllReportCards(exam) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "${AppStrings.get("view_report_card", lang)} (${students.size})",
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Teacher Fees Content: Fee Folders -> Open Folder -> Top "Gudbi" button + per-student "Bixiyay" button
@Composable
fun TeacherFeesContent(
    lang: String,
    fees: List<Fee>,
    students: List<Student>,
    feePayments: List<FeePayment>,
    onCreateFee: () -> Unit,
    onOpenFeeFolder: (Fee) -> Unit
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
            Column(modifier = Modifier.weight(1f)) {
                Text(AppStrings.get("fee_folders", lang), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (lang == "so") "Fur folder-ka si aad ardayda ula socoto una gudbiso" else "Tap a fee folder to submit to parents & mark payments",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Button(
                onClick = onCreateFee,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (lang == "so") "Fii Cusub" else "New Fee")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (fees.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(fees) { fee ->
                    val folderPayments = feePayments.filter { it.feeId == fee.feeId }
                    val paidCount = folderPayments.count { it.status == "PAID" }
                    val totalStudentsCount = maxOf(students.size, folderPayments.size)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFeeFolder(fee) }
                            .testTag("fee_folder_${fee.feeId}"),
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
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (fee.isPublished) GreenAccent.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (fee.isPublished) GreenAccent else Color(0xFFB7791F),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        fee.feeName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = NavyPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (fee.isPublished) Color(0xFFC6F6D5) else Color(0xFFFEEBC8)
                                    ) {
                                        Text(
                                            text = if (fee.isPublished) AppStrings.get("submitted", lang) else if (lang == "so") "Qabyo" else "Draft",
                                            color = if (fee.isPublished) Color(0xFF22543D) else Color(0xFF7B341E),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "$${fee.amount} • ${AppStrings.get("due_date", lang)}: ${fee.dueDate}",
                                    fontSize = 12.sp,
                                    color = BlueSecondary
                                )
                                Text(
                                    if (lang == "so")
                                        "Bixiyay: $paidCount / $totalStudentsCount arday"
                                    else
                                        "Paid: $paidCount / $totalStudentsCount students",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NavyPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherFeeFolderDialog(
    fee: Fee,
    students: List<Student>,
    payments: List<FeePayment>,
    lang: String,
    onDismiss: () -> Unit,
    onSubmitFeeToParents: () -> Unit,
    onMarkStudentPaid: (String, Double, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(fee.feeName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                    Text("$${fee.amount} • ${fee.className}", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Top-right corner "Gudbi" (Submit) button to publish fee to every parent room
                Button(
                    onClick = onSubmitFeeToParents,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (fee.isPublished) GreenAccent else NavyPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("fee_folder_submit_button")
                ) {
                    Icon(
                        if (fee.isPublished) Icons.Default.Check else Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (fee.isPublished) AppStrings.get("submitted", lang) else AppStrings.get("submit_to_parents", lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            val rows = remember(students, payments, fee) {
                if (students.isNotEmpty()) {
                    students.map { st ->
                        payments.firstOrNull { it.studentId == st.studentId } ?: FeePayment(
                            paymentId = "${fee.feeId}_${st.studentId}",
                            feeId = fee.feeId,
                            feeName = fee.feeName,
                            studentId = st.studentId,
                            studentName = st.fullName,
                            classId = st.classId,
                            className = st.className,
                            amount = fee.amount,
                            amountPaid = 0.0,
                            balance = fee.amount,
                            status = "UNPAID"
                        )
                    }
                } else {
                    payments
                }
            }

            if (rows.isEmpty()) {
                Text(AppStrings.get("no_data", lang), color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rows) { p ->
                        val st = students.firstOrNull { it.studentId == p.studentId }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    AvatarImage(
                                        photoBase64 = st?.photoUrl ?: "",
                                        name = p.studentName,
                                        size = 36.dp,
                                        accentColor = NavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(p.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            if (p.balance > 0) "${AppStrings.get("balance", lang)}: $${p.balance}"
                                            else "${AppStrings.get("receipt_no", lang)}: ${p.receiptNo}",
                                            fontSize = 11.sp,
                                            color = if (p.balance > 0) RedDanger else GreenAccent
                                        )
                                    }
                                }

                                if (p.balance > 0) {
                                    Button(
                                        onClick = {
                                            val rcpt = "RCP-${System.currentTimeMillis() % 100000}"
                                            onMarkStudentPaid(p.paymentId, p.balance, rcpt)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp).testTag("pay_btn_${p.studentId}")
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(AppStrings.get("paid_btn", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    StatusBadge(status = "PAID", lang = lang)
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

// Dialog: Register / Edit Student in Class (Photo is OPTIONAL via Gallery or Camera)
@Composable
fun TeacherRegisterStudentDialog(
    lang: String,
    classRoom: ClassRoom,
    existingStudent: Student?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit
) {
    var name by remember(existingStudent) { mutableStateOf(existingStudent?.fullName ?: "") }
    var father by remember(existingStudent) { mutableStateOf(existingStudent?.fatherName ?: "") }
    var mother by remember(existingStudent) { mutableStateOf(existingStudent?.motherName ?: "") }
    var phone by remember(existingStudent) { mutableStateOf(existingStudent?.parentPhone ?: "") }
    var gender by remember(existingStudent) { mutableStateOf(existingStudent?.gender ?: "M") }
    var admDate by remember(existingStudent) { mutableStateOf(existingStudent?.admissionDate?.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) } ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var photoBase64 by remember(existingStudent) { mutableStateOf(existingStudent?.photoUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${AppStrings.get("students", lang)} - ${classRoom.name}") },
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
                        FilterChip(selected = gender == "M", onClick = { gender = "M" }, label = { Text(AppStrings.get("male", lang)) })
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = gender == "F", onClick = { gender = "F" }, label = { Text(AppStrings.get("female", lang)) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        // Photo is completely optional; empty string passes cleanly!
                        onConfirm(name.trim(), father.trim(), mother.trim(), phone.trim(), gender, admDate, photoBase64)
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

// Dialog: Create Exam (linked to an Admin ExamFolder)
@Composable
fun TeacherCreateExamDialog(
    lang: String,
    classRoom: ClassRoom,
    folders: List<ExamFolder>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<ExamSubject>, String, String, String, String, String) -> Unit
) {
    var selectedFolder by remember(folders) { mutableStateOf(folders.firstOrNull()) }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(selectedFolder?.examType ?: "MONTHLY") }
    var subjectsStr by remember { mutableStateOf(classRoom.subjects.joinToString(", ")) }
    var startDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var endDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (lang == "so") "Abuur Imtixaan Cusub" else "Create Examination") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (folders.isNotEmpty()) {
                    item {
                        Text(AppStrings.get("select_folder", lang), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(folders) { folder ->
                                FilterChip(
                                    selected = selectedFolder?.folderId == folder.folderId,
                                    onClick = {
                                        selectedFolder = folder
                                        type = folder.examType
                                    },
                                    label = { Text(folder.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(AppStrings.get("exam_name", lang)) },
                        placeholder = { Text("e.g. Midterm Term 1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(AppStrings.get("exam_type", lang), fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(selected = type == "MONTHLY", onClick = { type = "MONTHLY" }, label = { Text("Monthly") })
                        FilterChip(selected = type == "MIDTERM", onClick = { type = "MIDTERM" }, label = { Text("Midterm") })
                        FilterChip(selected = type == "FINAL", onClick = { type = "FINAL" }, label = { Text("Final") })
                    }
                }
                item {
                    OutlinedTextField(
                        value = subjectsStr,
                        onValueChange = { subjectsStr = it },
                        label = { Text(AppStrings.get("subjects", lang) + " (Comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(AppStrings.get("description", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val subs = subjectsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            .map { ExamSubject(name = it, maxMarks = 100.0) }
                        onConfirm(
                            name.trim(),
                            type,
                            subs,
                            startDate,
                            endDate,
                            desc.trim(),
                            selectedFolder?.folderId ?: "",
                            selectedFolder?.name ?: ""
                        )
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

// Dialog: Enter Student Marks + Teacher Deserving Remarks per student
@Composable
fun TeacherEnterMarksDialog(
    exam: Examination,
    students: List<Student>,
    lang: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val marksMap = remember { mutableStateMapOf<String, MutableMap<String, Double>>() }
    val remarksMap = remember { mutableStateMapOf<String, String>() }

    val existingResults by viewModel.repository.observeExamResults(exam.examId).collectAsState(initial = emptyList())

    LaunchedEffect(existingResults) {
        existingResults.forEach { res ->
            marksMap[res.studentId] = res.subjectMarks.toMutableMap()
            if (res.teacherRemarks.isNotBlank()) {
                remarksMap[res.studentId] = res.teacherRemarks
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(exam.name, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text("${AppStrings.get("enter_marks", lang)} • ${exam.className}", fontSize = 12.sp, color = Color.Gray)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(students) { student ->
                    val studentMarks = marksMap.getOrPut(student.studentId) { mutableMapOf() }
                    var commentStr by remember(existingResults, student.studentId) {
                        mutableStateOf(remarksMap[student.studentId] ?: "")
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarImage(photoBase64 = student.photoUrl, name = student.fullName, size = 32.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("ID: ${student.studentId}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            exam.subjects.forEach { sub ->
                                var markStr by remember(existingResults, student.studentId, sub.name) {
                                    mutableStateOf(
                                        studentMarks[sub.name]?.let {
                                            if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
                                        } ?: ""
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${sub.name} (Max ${sub.maxMarks.toInt()})", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    OutlinedTextField(
                                        value = markStr,
                                        onValueChange = {
                                            markStr = it
                                            val num = it.toDoubleOrNull() ?: 0.0
                                            studentMarks[sub.name] = num.coerceIn(0.0, sub.maxMarks)
                                        },
                                        modifier = Modifier.width(80.dp),
                                        singleLine = true
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = commentStr,
                                onValueChange = {
                                    commentStr = it
                                    remarksMap[student.studentId] = it
                                },
                                label = { Text(AppStrings.get("teacher_remarks", lang), fontSize = 11.sp) },
                                placeholder = {
                                    Text(
                                        if (lang == "so") "Qor waxa ardaygu u qalmo / qiimeynta..." else "Write what this student deserves...",
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resultsList = students.map { s ->
                        val subMarks = marksMap[s.studentId] ?: emptyMap()
                        val totalObt = subMarks.values.sum()
                        val totalMax = exam.subjects.sumOf { it.maxMarks }
                        val pct = if (totalMax > 0) (totalObt / totalMax * 100) else 0.0
                        val avg = if (exam.subjects.isNotEmpty()) totalObt / exam.subjects.size else 0.0
                        val customRemark = remarksMap[s.studentId]?.trim().orEmpty()
                        val autoRemark = when {
                            pct >= 90 -> if (lang == "so") "Aad u wanaagsan! Waxqabad heer sare ah." else "Excellent work! Truly outstanding performance."
                            pct >= 75 -> if (lang == "so") "Wanaagsan, dadaalkaaga sii wad." else "Very good performance, keep up the great effort."
                            pct >= 50 -> if (lang == "so") "Waa fiican yahay, balse dadaal dheeri ah ayaa loo baahan yahay." else "Satisfactory, more revision is encouraged."
                            else -> if (lang == "so") "Waxaad u baahan tahay caawinaad iyo dadaal xooggan." else "Needs dedicated focus and additional support."
                        }

                        ExamResult(
                            examId = exam.examId,
                            examName = exam.name,
                            studentId = s.studentId,
                            studentName = s.fullName,
                            classId = s.classId,
                            className = s.className,
                            subjectMarks = subMarks,
                            totalObtained = totalObt,
                            totalMax = totalMax,
                            percentage = pct,
                            average = avg,
                            teacherRemarks = customRemark.ifBlank { autoRemark }
                        )
                    }
                    viewModel.submitExamResults(exam, resultsList)
                    Toast.makeText(context, AppStrings.get("success_saved", lang), Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text(AppStrings.get("submit", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang)) }
        }
    )
}

// Dialog: ALL-Students Report Cards Browser + PDF Export
@Composable
fun TeacherAllReportCardsDialog(
    exam: Examination,
    students: List<Student>,
    school: SchoolConfig,
    lang: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val results by viewModel.repository.observeExamResults(exam.examId).collectAsState(initial = emptyList())
    var selectedStudentFilter by remember { mutableStateOf<String?>(null) } // null = All students

    val displayList = remember(students, results, selectedStudentFilter) {
        val base = students.map { st ->
            val res = results.firstOrNull { it.studentId == st.studentId } ?: ExamResult(
                examId = exam.examId,
                examName = exam.name,
                studentId = st.studentId,
                studentName = st.fullName,
                classId = st.classId,
                className = st.className,
                subjectMarks = exam.subjects.associate { it.name to 0.0 },
                totalObtained = 0.0,
                totalMax = exam.subjects.sumOf { it.maxMarks },
                percentage = 0.0,
                grade = "-",
                position = 0L
            )
            st to res
        }.sortedBy { if (it.second.position > 0) it.second.position else Long.MAX_VALUE }

        if (selectedStudentFilter != null) base.filter { it.first.studentId == selectedStudentFilter } else base
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("${AppStrings.get("view_report_card", lang)} • ${exam.className}", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 15.sp)
                Text("${exam.name} (${students.size} ${AppStrings.get("students", lang)})", fontSize = 11.sp, color = Color.Gray)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedStudentFilter == null,
                            onClick = { selectedStudentFilter = null },
                            label = { Text("${AppStrings.get("all_students", lang)} (${students.size})", fontSize = 11.sp) }
                        )
                    }
                    items(students) { st ->
                        FilterChip(
                            selected = selectedStudentFilter == st.studentId,
                            onClick = { selectedStudentFilter = st.studentId },
                            label = { Text(st.fullName.substringBefore(" "), fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayList) { (st, res) ->
                        Column {
                            BeautifulReportCardView(
                                school = school,
                                student = st,
                                exam = exam,
                                result = res,
                                lang = lang
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    val pdfFile = ReportCardPdfGenerator.generateReportCard(
                                        context = context,
                                        school = school,
                                        student = st,
                                        exam = exam,
                                        result = res,
                                        language = lang
                                    )
                                    if (pdfFile != null) {
                                        ReportCardPdfGenerator.shareReportCardPdf(context, pdfFile)
                                    } else {
                                        Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${AppStrings.get("download_pdf", lang)} (${st.fullName})", fontSize = 11.sp)
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

// Dialog: Create Class Fee Folder
@Composable
fun TeacherCreateFeeDialog(
    lang: String,
    classRoom: ClassRoom,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String) -> Unit
) {
    var feeName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("20.0") }
    var dueDate by remember { mutableStateOf("2026-12-05") }
    var desc by remember { mutableStateOf("Monthly school tuition fee") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${AppStrings.get("fee_folders", lang)} - ${classRoom.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = feeName,
                    onValueChange = { feeName = it },
                    label = { Text(AppStrings.get("fee_name", lang)) },
                    placeholder = { Text("e.g. December Tuition Fee") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(AppStrings.get("amount", lang)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text(AppStrings.get("due_date", lang)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (feeName.isNotBlank() && amt > 0) {
                        onConfirm(feeName.trim(), amt, dueDate.trim(), desc)
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

// Dialog: Write Student Remark
@Composable
fun TeacherWriteRemarkDialog(
    student: Student,
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var category by remember { mutableStateOf("GOOD_PERFORMANCE") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${AppStrings.get("remarks", lang)} - ${student.fullName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item { FilterChip(selected = category == "GOOD_PERFORMANCE", onClick = { category = "GOOD_PERFORMANCE" }, label = { Text("Good Performance") }) }
                    item { FilterChip(selected = category == "EXCELLENT_BEHAVIOR", onClick = { category = "EXCELLENT_BEHAVIOR" }, label = { Text("Excellent Behavior") }) }
                    item { FilterChip(selected = category == "NEEDS_IMPROVEMENT", onClick = { category = "NEEDS_IMPROVEMENT" }, label = { Text("Needs Improvement") }) }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(AppStrings.get("message", lang)) },
                    placeholder = { Text("Enter detailed feedback for parent...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (message.isNotBlank()) onConfirm(category, message.trim()) }) {
                Text(AppStrings.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang)) }
        }
    )
}
