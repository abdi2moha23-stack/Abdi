package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

                // Vibrant Class Selector Strip
                Surface(
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(BlueSurface.copy(alpha = 0.65f), GreenBackground.copy(alpha = 0.45f))))
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${AppStrings.get("class", lang)}: ",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = NavyPrimary
                        )

                        if (classes.isEmpty()) {
                            Text(if (lang == "so") "Weli fasal lama abuurin" else "No classes created yet", fontSize = 12.sp, color = TextSecondary)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(classes) { c ->
                                    val unlocked = c.classId in unlockedClassIds
                                    val active = selectedClass?.classId == c.classId && unlocked
                                    FilterChip(
                                        selected = active,
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
                                                tint = if (active) AmberWarning else if (unlocked) GreenAccent else AmberDark,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        },
                                        label = { Text(c.name, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color.White,
                                            labelColor = NavyPrimary,
                                            selectedContainerColor = NavyLight,
                                            selectedLabelColor = Color.White
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

    if (pendingUnlockClass != null && isCurrentClassUnlocked) {
        val target = pendingUnlockClass!!
        var enteredPw by remember(target) { mutableStateOf("") }
        var pwError by remember(target) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { pendingUnlockClass = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(GradientRoyalBlue)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("${target.name} • ${AppStrings.get("class_access_code", lang)}", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        if (lang == "so") "Geli furaha sirta ee fasalka ${target.name}:" else "Enter the access password for ${target.name}:",
                        fontSize = 13.sp,
                        color = TextSecondary
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pwError) {
                        Text(AppStrings.get("invalid_passcode", lang), color = RedDanger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                VibrantGradientButton(
                    text = AppStrings.get("unlock_class", lang),
                    icon = Icons.Default.LockOpen,
                    gradient = GradientRoyalBlue,
                    height = 42.dp,
                    onClick = {
                        if (viewModel.unlockClass(target, enteredPw)) {
                            pendingUnlockClass = null
                        } else {
                            pwError = true
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { pendingUnlockClass = null }) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
            }
        )
    }

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

    if (showEnterMarksExam != null) {
        TeacherEnterMarksDialog(
            exam = showEnterMarksExam!!,
            students = classStudents,
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showEnterMarksExam = null }
        )
    }

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
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientRoyalBlue))
                    .border(3.dp, AmberWarning, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LockPerson, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = AppStrings.get("enter_class_password", lang),
                    fontFamily = OutfitFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (lang == "so")
                        "Macallin kasta waa inuu doortaa fasalkiisa kadibna uu geliyaa furaha sirta ah ee fasalkaas si uu u galo."
                    else
                        "Select your assigned class and enter its class password to access attendance, students, exams, and fees.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, NavyLight.copy(alpha = 0.25f)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (lang == "so") "1. Dooro Fasalka:" else "1. Select Class:",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyPrimary
                    )

                    if (classes.isEmpty()) {
                        Text(
                            if (lang == "so") "Maamulaha weli ma abuurin fasallo." else "No classes available yet. Contact Admin.",
                            fontSize = 13.sp,
                            color = RedDanger,
                            fontWeight = FontWeight.SemiBold
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
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyLight,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = AmberWarning
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            if (unlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(c.name, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    if (pickedClass != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BlueSurface
                        ) {
                            Text(
                                "${AppStrings.get("class_teacher", lang)}: ${pickedClass!!.classTeacherName.ifBlank { "-" }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Text(
                            if (lang == "so") "2. Geli Furaha Sirta Fasalka (${pickedClass!!.name}):" else "2. Enter Password for ${pickedClass!!.name}:",
                            fontFamily = OutfitFontFamily,
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_class_password_input")
                        )

                        if (showError) {
                            Text(
                                AppStrings.get("invalid_passcode", lang),
                                color = RedDanger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        VibrantGradientButton(
                            text = AppStrings.get("unlock_class", lang),
                            icon = Icons.Default.LockOpen,
                            gradient = GradientRoyalBlue,
                            height = 50.dp,
                            fontSize = 14,
                            onClick = {
                                val target = pickedClass ?: return@VibrantGradientButton
                                if (onVerifyPassword(target, password)) {
                                    onUnlockSuccess(target)
                                } else {
                                    showError = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_unlock_class_button")
                        )
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
    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.6f))
    ) {
        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
            NavigationBarItem(
                selected = currentTab == "DASHBOARD",
                onClick = { onSelectTab("DASHBOARD") },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                label = { Text(AppStrings.get("dashboard", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = NavyLight, selectedTextColor = NavyLight, indicatorColor = BlueSurface),
                modifier = Modifier.testTag("teacher_nav_dashboard")
            )
            NavigationBarItem(
                selected = currentTab == "STUDENTS",
                onClick = { onSelectTab("STUDENTS") },
                icon = { Icon(Icons.Default.School, contentDescription = null) },
                label = { Text(AppStrings.get("students", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = BlueSecondary, selectedTextColor = BlueSecondary, indicatorColor = BlueSurface),
                modifier = Modifier.testTag("teacher_nav_students")
            )
            NavigationBarItem(
                selected = currentTab == "ATTENDANCE",
                onClick = { onSelectTab("ATTENDANCE") },
                icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                label = { Text(AppStrings.get("attendance", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenAccent, selectedTextColor = GreenAccent, indicatorColor = GreenBackground),
                modifier = Modifier.testTag("teacher_nav_attendance")
            )
            NavigationBarItem(
                selected = currentTab == "EXAMS",
                onClick = { onSelectTab("EXAMS") },
                icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                label = { Text(AppStrings.get("exams", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = VioletAccent, selectedTextColor = VioletAccent, indicatorColor = VioletBackground),
                modifier = Modifier.testTag("teacher_nav_exams")
            )
            NavigationBarItem(
                selected = currentTab == "FEES",
                onClick = { onSelectTab("FEES") },
                icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                label = { Text(AppStrings.get("fees", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = RedDanger, selectedTextColor = RedDanger, indicatorColor = RedBackground),
                modifier = Modifier.testTag("teacher_nav_fees")
            )
        }
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(GradientCobaltDeep))
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF10B981).copy(alpha = 0.22f),
                                radius = size.height * 0.85f,
                                center = Offset(size.width * 0.9f, size.height * 0.15f)
                            )
                        }
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = selectedClass?.name ?: "No Class Selected",
                            fontFamily = OutfitFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${AppStrings.get("academic_year", lang)}: ${selectedClass?.academicYear ?: "2025-2026"}",
                            fontSize = 12.sp,
                            color = Color(0xFFBAE6FD)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TeacherHeroStatOrb(
                                value = "$studentsCount",
                                label = AppStrings.get("total_students", lang),
                                accent = BlueLight,
                                modifier = Modifier.weight(1f)
                            )
                            TeacherHeroStatOrb(
                                value = "$examsCount",
                                label = AppStrings.get("exams", lang),
                                accent = AmberWarning,
                                modifier = Modifier.weight(1f)
                            )
                            TeacherHeroStatOrb(
                                value = "$feesCount",
                                label = AppStrings.get("fees", lang),
                                accent = GreenLight,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeaderRow(
                title = AppStrings.get("quick_actions", lang),
                accentColor = NavyLight,
                icon = Icons.Default.Bolt
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VibrantGradientButton(
                    text = AppStrings.get("attendance", lang),
                    icon = Icons.Default.Checklist,
                    gradient = GradientEmerald,
                    onClick = onOpenAttendance,
                    modifier = Modifier.weight(1f)
                )

                VibrantGradientButton(
                    text = AppStrings.get("add", lang),
                    icon = Icons.Default.PersonAdd,
                    gradient = GradientRoyalBlue,
                    onClick = onOpenRegisterStudent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VibrantGradientButton(
                    text = if (lang == "so") "Abuur Imtixaan" else "Create Exam",
                    icon = Icons.Default.Assignment,
                    gradient = GradientViolet,
                    onClick = onOpenCreateExam,
                    modifier = Modifier.weight(1f)
                )

                VibrantGradientButton(
                    text = AppStrings.get("fee_folders", lang),
                    icon = Icons.Default.Folder,
                    gradient = GradientAmber,
                    onClick = onOpenFees,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TeacherHeroStatOrb(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontFamily = OutfitFontFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = accent)
            Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
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
        SectionHeaderRow(
            title = "${AppStrings.get("students", lang)} (${students.size})",
            accentColor = BlueSecondary,
            icon = Icons.Default.School
        ) {
            VibrantGradientButton(
                text = AppStrings.get("add", lang),
                icon = Icons.Default.Add,
                gradient = GradientRoyalBlue,
                height = 42.dp,
                onClick = onRegisterStudent
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (students.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.School,
                message = AppStrings.get("no_data", lang),
                accentColor = BlueSecondary
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(students) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.22f)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarImage(
                                    photoBase64 = student.photoUrl,
                                    name = student.fullName,
                                    size = 48.dp,
                                    accentColor = BlueSecondary
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.fullName, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                                    Text("ID: ${student.studentId} • ${student.parentPhone}", fontSize = 12.sp, color = TextSecondary)
                                }

                                VibrantIconActionButton(
                                    icon = Icons.Default.Edit,
                                    contentDescription = "Edit Student",
                                    color = BlueSecondary,
                                    onClick = { onEditStudent(student) }
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                VibrantGradientButton(
                                    text = AppStrings.get("remarks", lang),
                                    icon = Icons.Default.Comment,
                                    gradient = GradientAmber,
                                    contentColor = Color(0xFF0F172A),
                                    height = 36.dp,
                                    fontSize = 11,
                                    onClick = { onWriteRemark(student) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = AmberBackground,
                                border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    "Linking Code: ${student.linkingCode}",
                                    fontFamily = MonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
            Column(modifier = Modifier.weight(1f)) {
                Text(AppStrings.get("attendance", lang), fontFamily = OutfitFontFamily, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = NavyPrimary)
                Text("${AppStrings.get("date", lang)}: ${formatDayAndDate(selectedDate, lang)}", fontSize = 12.sp, color = BlueSecondary, fontWeight = FontWeight.SemiBold)
            }

            VibrantGradientButton(
                text = AppStrings.get("mark_all_present", lang),
                icon = Icons.Default.DoneAll,
                gradient = GradientEmerald,
                height = 40.dp,
                fontSize = 11,
                onClick = { students.forEach { attendanceMap[it.studentId] = "PRESENT" } },
                modifier = Modifier.testTag("mark_all_present_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("${AppStrings.get("present", lang)}: $presentCount", color = GreenAccent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                Text("${AppStrings.get("absent", lang)}: $absentCount", color = RedDanger, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                Text("${AppStrings.get("late", lang)}: $lateCount", color = AmberDark, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                Text("${AppStrings.get("excused", lang)}: $excusedCount", color = BlueSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                Text(String.format(Locale.US, "%.0f%%", rate), fontFamily = OutfitFontFamily, color = NavyPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
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
                    border = BorderStroke(1.dp, BorderLight),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                            Text(student.studentId, fontFamily = MonoFontFamily, fontSize = 11.sp, color = TextSecondary)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            AttendanceStatusButton("P", currentStatus == "PRESENT", GreenAccent) {
                                attendanceMap[student.studentId] = "PRESENT"
                            }
                            AttendanceStatusButton("A", currentStatus == "ABSENT", RedDanger) {
                                attendanceMap[student.studentId] = "ABSENT"
                            }
                            AttendanceStatusButton("L", currentStatus == "LATE", AmberDark) {
                                attendanceMap[student.studentId] = "LATE"
                            }
                            AttendanceStatusButton("E", currentStatus == "EXCUSED", BlueSecondary) {
                                attendanceMap[student.studentId] = "EXCUSED"
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        VibrantGradientButton(
            text = AppStrings.get("save", lang),
            icon = Icons.Default.Save,
            gradient = GradientRoyalBlue,
            height = 50.dp,
            fontSize = 15,
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
            modifier = Modifier.fillMaxWidth().testTag("save_attendance_button")
        )
    }
}

@Composable
fun AttendanceStatusButton(
    shortLabel: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "att_scale"
    )
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color else color.copy(alpha = 0.09f),
        border = BorderStroke(1.2.dp, if (isSelected) color else color.copy(alpha = 0.35f)),
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = Modifier.scale(scale).size(36.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                shortLabel,
                fontFamily = OutfitFontFamily,
                color = if (isSelected) Color.White else color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
        }
    }
}

// Teacher Exams Tab (Report Cards button opens ALL students in class)
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
        SectionHeaderRow(
            title = AppStrings.get("exams", lang),
            subtitle = "${exams.size}",
            accentColor = VioletAccent,
            icon = Icons.Default.Assignment
        ) {
            VibrantGradientButton(
                text = AppStrings.get("add", lang),
                icon = Icons.Default.Add,
                gradient = GradientViolet,
                height = 42.dp,
                onClick = onCreateExam
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (exams.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Assignment,
                message = AppStrings.get("no_data", lang),
                accentColor = VioletAccent
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(exams) { exam ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, VioletAccent.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exam.name, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                                    if (exam.folderName.isNotBlank()) {
                                        Text("Folder: ${exam.folderName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VioletAccent)
                                    }
                                }
                                StatusBadge(status = exam.status, lang = lang)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "${exam.examType} • ${exam.subjects.size} ${AppStrings.get("subjects", lang)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VibrantGradientButton(
                                    text = AppStrings.get("enter_marks", lang),
                                    icon = Icons.Default.Edit,
                                    gradient = GradientRoyalBlue,
                                    height = 40.dp,
                                    fontSize = 11,
                                    onClick = { onEnterMarks(exam) },
                                    modifier = Modifier.weight(1f)
                                )

                                if (students.isNotEmpty()) {
                                    VibrantTintedButton(
                                        text = "${AppStrings.get("view_report_card", lang)} (${students.size})",
                                        color = GreenAccent,
                                        icon = Icons.Default.Assessment,
                                        height = 40.dp,
                                        fontSize = 11,
                                        onClick = { onViewAllReportCards(exam) },
                                        modifier = Modifier.weight(1f)
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
        SectionHeaderRow(
            title = AppStrings.get("fee_folders", lang),
            subtitle = if (lang == "so") "Fur folder-ka si aad ardayda ula socoto una gudbiso" else "Tap a fee folder to submit to parents & mark payments",
            accentColor = RedDanger,
            icon = Icons.Default.FolderShared
        ) {
            VibrantGradientButton(
                text = if (lang == "so") "Fii Cusub" else "New Fee",
                icon = Icons.Default.CreateNewFolder,
                gradient = GradientCrimson,
                height = 42.dp,
                fontSize = 12,
                onClick = onCreateFee
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (fees.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.FolderOff,
                message = AppStrings.get("no_data", lang),
                accentColor = RedDanger
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(fees) { fee ->
                    val folderPayments = feePayments.filter { it.feeId == fee.feeId }
                    val paidCount = folderPayments.count { it.status == "PAID" }
                    val totalStudentsCount = maxOf(students.size, folderPayments.size)
                    val folderGrad = if (fee.isPublished) GradientEmerald else GradientAmber

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFeeFolder(fee) }
                            .testTag("fee_folder_${fee.feeId}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, (if (fee.isPublished) GreenAccent else AmberWarning).copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(folderGrad)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
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
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = NavyPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatusBadge(
                                        status = if (fee.isPublished) "SUBMITTED" else "UNPAID",
                                        lang = lang
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "$${fee.amount} • ${AppStrings.get("due_date", lang)}: ${fee.dueDate}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BlueSecondary
                                )
                                Text(
                                    if (lang == "so")
                                        "Bixiyay: $paidCount / $totalStudentsCount arday"
                                    else
                                        "Paid: $paidCount / $totalStudentsCount students",
                                    fontSize = 12.sp,
                                    color = TextSecondary
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(fee.feeName, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                    Text("$${fee.amount} • ${fee.className}", fontSize = 11.sp, color = TextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                VibrantGradientButton(
                    text = if (fee.isPublished) AppStrings.get("submitted", lang) else AppStrings.get("submit_to_parents", lang),
                    icon = if (fee.isPublished) Icons.Default.Check else Icons.Default.Send,
                    gradient = if (fee.isPublished) GradientEmerald else GradientViolet,
                    height = 38.dp,
                    fontSize = 12,
                    onClick = onSubmitFeeToParents,
                    modifier = Modifier.testTag("fee_folder_submit_button")
                )
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
                Text(AppStrings.get("no_data", lang), color = TextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rows) { p ->
                        val st = students.firstOrNull { it.studentId == p.studentId }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (p.balance > 0) RedBackground.copy(alpha = 0.45f) else GreenBackground.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, (if (p.balance > 0) RedDanger else GreenAccent).copy(alpha = 0.25f))
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
                                        accentColor = if (p.balance > 0) RedDanger else GreenAccent
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(p.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                        Text(
                                            if (p.balance > 0) "${AppStrings.get("balance", lang)}: $${p.balance}"
                                            else "${AppStrings.get("receipt_no", lang)}: ${p.receiptNo}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (p.balance > 0) RedDanger else GreenAccent
                                        )
                                    }
                                }

                                if (p.balance > 0) {
                                    VibrantGradientButton(
                                        text = AppStrings.get("paid_btn", lang),
                                        icon = Icons.Default.CheckCircle,
                                        gradient = GradientEmerald,
                                        height = 36.dp,
                                        fontSize = 11,
                                        onClick = {
                                            val rcpt = "RCP-${System.currentTimeMillis() % 100000}"
                                            onMarkStudentPaid(p.paymentId, p.balance, rcpt)
                                        },
                                        modifier = Modifier.testTag("pay_btn_${p.studentId}")
                                    )
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
            VibrantGradientButton(text = "OK", gradient = GradientRoyalBlue, height = 40.dp, onClick = onDismiss)
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = { Text("${AppStrings.get("students", lang)} - ${classRoom.name}", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary) },
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = father,
                        onValueChange = { father = it },
                        label = { Text(AppStrings.get("father_name", lang)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = mother,
                        onValueChange = { mother = it },
                        label = { Text(AppStrings.get("mother_name", lang)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(AppStrings.get("parent_phone", lang)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(AppStrings.get("gender", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    Row {
                        FilterChip(
                            selected = gender == "M",
                            onClick = { gender = "M" },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyLight, selectedLabelColor = Color.White),
                            label = { Text(AppStrings.get("male", lang)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = gender == "F",
                            onClick = { gender = "F" },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VioletAccent, selectedLabelColor = Color.White),
                            label = { Text(AppStrings.get("female", lang)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("save", lang),
                icon = Icons.Default.Check,
                gradient = GradientRoyalBlue,
                height = 42.dp,
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), father.trim(), mother.trim(), phone.trim(), gender, admDate, photoBase64)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = { Text(if (lang == "so") "Abuur Imtixaan Cusub" else "Create Examination", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = VioletAccent) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (folders.isNotEmpty()) {
                    item {
                        Text(AppStrings.get("select_folder", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(folders) { folder ->
                                FilterChip(
                                    selected = selectedFolder?.folderId == folder.folderId,
                                    onClick = {
                                        selectedFolder = folder
                                        type = folder.examType
                                    },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AmberDark, selectedLabelColor = Color.White),
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(AppStrings.get("exam_type", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(AppStrings.get("description", lang)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("save", lang),
                icon = Icons.Default.Check,
                gradient = GradientViolet,
                height = 42.dp,
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
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text(exam.name, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text("${AppStrings.get("enter_marks", lang)} • ${exam.className}", fontSize = 12.sp, color = BlueSecondary)
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
                        colors = CardDefaults.cardColors(containerColor = BlueSurface.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarImage(photoBase64 = student.photoUrl, name = student.fullName, size = 34.dp, accentColor = NavyLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                                    Text("ID: ${student.studentId}", fontFamily = MonoFontFamily, fontSize = 11.sp, color = TextSecondary)
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
                                    Text("${sub.name} (Max ${sub.maxMarks.toInt()})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    OutlinedTextField(
                                        value = markStr,
                                        onValueChange = {
                                            markStr = it
                                            val num = it.toDoubleOrNull() ?: 0.0
                                            studentMarks[sub.name] = num.coerceIn(0.0, sub.maxMarks)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.width(84.dp),
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("submit", lang),
                icon = Icons.Default.Check,
                gradient = GradientEmerald,
                height = 42.dp,
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
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
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
    val classes by viewModel.classes.collectAsState()
    val classTeacherName = remember(classes, exam.classId) {
        classes.firstOrNull { it.classId == exam.classId }?.classTeacherName.orEmpty()
    }
    val results by viewModel.repository.observeExamResults(exam.examId).collectAsState(initial = emptyList())
    var selectedStudentFilter by remember { mutableStateOf<String?>(null) }
    var previewPair by remember { mutableStateOf<Pair<Student, ExamResult>?>(null) }

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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text("${AppStrings.get("view_report_card", lang)} • ${exam.className}", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 15.sp)
                Text("${exam.name} (${students.size} ${AppStrings.get("students", lang)})", fontSize = 11.sp, color = BlueSecondary)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedStudentFilter == null,
                            onClick = { selectedStudentFilter = null },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyLight, selectedLabelColor = Color.White),
                            label = { Text("${AppStrings.get("all_students", lang)} (${students.size})", fontSize = 11.sp) }
                        )
                    }
                    items(students) { st ->
                        FilterChip(
                            selected = selectedStudentFilter == st.studentId,
                            onClick = { selectedStudentFilter = st.studentId },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenAccent, selectedLabelColor = Color.White),
                            label = { Text(st.fullName.substringBefore(" "), fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayList) { (st, res) ->
                        StudentResultSummaryCard(
                            student = st,
                            exam = exam,
                            result = res,
                            lang = lang,
                            onPreview = { previewPair = st to res },
                            onDownloadPdf = {
                                val pdfFile = ReportCardPdfGenerator.generateReportCard(
                                    context = context,
                                    school = school,
                                    student = st,
                                    exam = exam,
                                    result = res,
                                    language = lang,
                                    classTeacherName = classTeacherName
                                )
                                if (pdfFile != null) {
                                    ReportCardPdfGenerator.shareReportCardPdf(context, pdfFile)
                                } else {
                                    Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            VibrantGradientButton(text = "OK", gradient = GradientEmerald, height = 40.dp, onClick = onDismiss)
        }
    )

    if (previewPair != null) {
        val (st, res) = previewPair!!
        StudentReportPreviewDialog(
            student = st,
            exam = exam,
            result = res,
            school = school,
            lang = lang,
            classTeacherName = classTeacherName,
            onDownloadPdf = {
                val pdfFile = ReportCardPdfGenerator.generateReportCard(
                    context = context,
                    school = school,
                    student = st,
                    exam = exam,
                    result = res,
                    language = lang,
                    classTeacherName = classTeacherName
                )
                if (pdfFile != null) {
                    ReportCardPdfGenerator.shareReportCardPdf(context, pdfFile)
                } else {
                    Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { previewPair = null }
        )
    }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = { Text("${AppStrings.get("fee_folders", lang)} - ${classRoom.name}", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = RedDanger) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = feeName,
                    onValueChange = { feeName = it },
                    label = { Text(AppStrings.get("fee_name", lang)) },
                    placeholder = { Text("e.g. December Tuition Fee") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(AppStrings.get("amount", lang)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text(AppStrings.get("due_date", lang)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("save", lang),
                icon = Icons.Default.Check,
                gradient = GradientCrimson,
                height = 42.dp,
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (feeName.isNotBlank() && amt > 0) {
                        onConfirm(feeName.trim(), amt, dueDate.trim(), desc)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = TextSecondary) }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = { Text("${AppStrings.get("remarks", lang)} - ${student.fullName}", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = AmberDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = category == "GOOD_PERFORMANCE",
                            onClick = { category = "GOOD_PERFORMANCE" },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenAccent, selectedLabelColor = Color.White),
                            label = { Text("Good Performance") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = category == "EXCELLENT_BEHAVIOR",
                            onClick = { category = "EXCELLENT_BEHAVIOR" },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BlueSecondary, selectedLabelColor = Color.White),
                            label = { Text("Excellent Behavior") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = category == "NEEDS_IMPROVEMENT",
                            onClick = { category = "NEEDS_IMPROVEMENT" },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedDanger, selectedLabelColor = Color.White),
                            label = { Text("Needs Improvement") }
                        )
                    }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(AppStrings.get("message", lang)) },
                    placeholder = { Text("Enter detailed feedback for parent...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("save", lang),
                icon = Icons.Default.Send,
                gradient = GradientAmber,
                contentColor = Color(0xFF0F172A),
                height = 42.dp,
                onClick = { if (message.isNotBlank()) onConfirm(category, message.trim()) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
        }
    )
}
