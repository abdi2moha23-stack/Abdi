package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayAttendance = allAttendance.filter { it.date == todayStr }
    val presentToday = todayAttendance.sumOf { it.presentCount }
    val absentToday = todayAttendance.sumOf { it.absentCount }

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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyLight,
                    selectedTextColor = NavyLight,
                    indicatorColor = BlueSurface
                ),
                modifier = Modifier.testTag("admin_nav_dashboard")
            )
            NavigationBarItem(
                selected = currentTab == "TEACHERS",
                onClick = { onSelectTab("TEACHERS") },
                icon = { Icon(Icons.Default.People, contentDescription = null) },
                label = { Text(AppStrings.get("teachers", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BlueSecondary,
                    selectedTextColor = BlueSecondary,
                    indicatorColor = BlueSurface
                ),
                modifier = Modifier.testTag("admin_nav_teachers")
            )
            NavigationBarItem(
                selected = currentTab == "CLASSES",
                onClick = { onSelectTab("CLASSES") },
                icon = { Icon(Icons.Default.Class, contentDescription = null) },
                label = { Text(AppStrings.get("classes", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VioletAccent,
                    selectedTextColor = VioletAccent,
                    indicatorColor = VioletBackground
                ),
                modifier = Modifier.testTag("admin_nav_classes")
            )
            NavigationBarItem(
                selected = currentTab == "STUDENTS",
                onClick = { onSelectTab("STUDENTS") },
                icon = { Icon(Icons.Default.School, contentDescription = null) },
                label = { Text(AppStrings.get("students", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenAccent,
                    selectedTextColor = GreenAccent,
                    indicatorColor = GreenBackground
                ),
                modifier = Modifier.testTag("admin_nav_students")
            )
            NavigationBarItem(
                selected = currentTab == "EXAMS",
                onClick = { onSelectTab("EXAMS") },
                icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null) },
                label = { Text(AppStrings.get("exams", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AmberDark,
                    selectedTextColor = AmberDark,
                    indicatorColor = AmberBackground
                ),
                modifier = Modifier.testTag("admin_nav_exams")
            )
            NavigationBarItem(
                selected = currentTab == "FEES",
                onClick = { onSelectTab("FEES") },
                icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                label = { Text(AppStrings.get("fees", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RedDanger,
                    selectedTextColor = RedDanger,
                    indicatorColor = RedBackground
                ),
                modifier = Modifier.testTag("admin_nav_fees")
            )
        }
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
        // Luminous Cobalt-to-Cerulean Hero Banner
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
                                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                radius = size.height * 0.9f,
                                center = Offset(size.width * 0.92f, size.height * 0.15f)
                            )
                            drawCircle(
                                color = Color(0xFF10B981).copy(alpha = 0.16f),
                                radius = size.height * 0.65f,
                                center = Offset(size.width * 0.1f, size.height * 0.95f)
                            )
                        }
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(AmberWarning.copy(alpha = 0.22f))
                                    .border(1.dp, AmberWarning.copy(alpha = 0.6f), RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    AppStrings.get("dashboard", lang),
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    AppStrings.get("single_school_notice", lang),
                                    fontSize = 11.sp,
                                    color = Color(0xFFBAE6FD)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = if (lang == "so") "Taabo kaar walbo si aad u aragto xogta uu u taagan yahay" else "Tap any stat card to open its full breakdown",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Clickable Vibrant Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = AppStrings.get("total_students", lang),
                        value = "$studentsCount",
                        icon = Icons.Default.School,
                        color = NavyLight,
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
                        color = VioletAccent,
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
                        color = GreenLight,
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
                    color = AmberDark,
                    modifier = Modifier.testTag("stat_unpaid_fees"),
                    onClick = onOpenFees
                )
            }
        }

        // Quick Actions (Vibrant Multi-Color Gradient Buttons)
        item {
            SectionHeaderRow(
                title = AppStrings.get("quick_actions", lang),
                accentColor = NavyLight,
                icon = Icons.Default.Bolt
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VibrantGradientButton(
                    text = AppStrings.get("teachers", lang),
                    icon = Icons.Default.PersonAdd,
                    gradient = GradientRoyalBlue,
                    onClick = onOpenTeachers,
                    modifier = Modifier.weight(1f).testTag("admin_action_teachers")
                )

                VibrantGradientButton(
                    text = AppStrings.get("classes", lang),
                    icon = Icons.Default.AddBusiness,
                    gradient = GradientViolet,
                    onClick = onOpenClasses,
                    modifier = Modifier.weight(1f).testTag("admin_action_classes")
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VibrantGradientButton(
                    text = AppStrings.get("exam_folders", lang),
                    icon = Icons.Default.FolderOpen,
                    gradient = GradientEmerald,
                    onClick = onOpenExams,
                    modifier = Modifier.weight(1f).testTag("admin_action_publish_exams")
                )

                VibrantGradientButton(
                    text = AppStrings.get("settings", lang),
                    icon = Icons.Default.Settings,
                    gradient = GradientCrimson,
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f).testTag("admin_action_settings")
                )
            }
        }

        // Recent Activity Feed
        item {
            SectionHeaderRow(
                title = AppStrings.get("recent_activities", lang),
                accentColor = GreenAccent,
                icon = Icons.Default.NotificationsActive
            )
        }

        if (notifications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Text(
                        AppStrings.get("no_data", lang),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(notifications.take(5)) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Brush.linearGradient(GradientRoyalBlue)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(notif.title, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                            Text(notif.message, fontSize = 12.sp, color = TextSecondary)
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
    val gradient = if (status == "PRESENT") GradientEmerald else GradientCrimson

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (status == "PRESENT") Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = accent, fontSize = 16.sp)
                    Text("${AppStrings.get("date", lang)}: $todayStr (${entries.size})", fontSize = 12.sp, color = TextSecondary)
                }
            }
        },
        text = {
            if (entries.isEmpty()) {
                Text(
                    if (lang == "so") "Maanta weli lama diiwaangelin arday xaaladdan leh." else "No students recorded with this status today yet.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries.size) { index ->
                        val (name, id, className) = entries[index]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.06f)),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${index + 1}. $name", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                    Text("$id • $className", fontSize = 11.sp, color = TextSecondary)
                                }
                                StatusBadge(status = status, lang = lang)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            VibrantGradientButton(text = "OK", gradient = gradient, height = 40.dp, onClick = onDismiss)
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
        SectionHeaderRow(
            title = AppStrings.get("teachers", lang),
            subtitle = "${teachers.size}",
            accentColor = BlueSecondary,
            icon = Icons.Default.People
        ) {
            VibrantGradientButton(
                text = AppStrings.get("add", lang),
                icon = Icons.Default.Add,
                gradient = GradientRoyalBlue,
                height = 42.dp,
                onClick = onAddTeacher,
                modifier = Modifier.testTag("admin_add_teacher_button")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (teachers.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.PeopleOutline,
                message = AppStrings.get("no_data", lang),
                accentColor = BlueSecondary
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(teachers) { teacher ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.22f)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                photoBase64 = teacher.photoUrl,
                                name = teacher.fullName,
                                size = 50.dp,
                                accentColor = BlueSecondary
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    teacher.fullName,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    "${teacher.phone} • ${teacher.email.ifBlank { "No email" }}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            VibrantIconActionButton(
                                icon = Icons.Default.Edit,
                                contentDescription = AppStrings.get("edit", lang),
                                color = BlueSecondary,
                                onClick = { onEditTeacher(teacher) },
                                modifier = Modifier.testTag("edit_teacher_${teacher.teacherId}")
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            VibrantIconActionButton(
                                icon = Icons.Default.Delete,
                                contentDescription = "Delete",
                                color = RedDanger,
                                onClick = { onDeleteTeacher(teacher.teacherId) },
                                modifier = Modifier.testTag("delete_teacher_${teacher.teacherId}")
                            )
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
        SectionHeaderRow(
            title = AppStrings.get("classes", lang),
            subtitle = "${classes.size}",
            accentColor = VioletAccent,
            icon = Icons.Default.Class
        ) {
            VibrantGradientButton(
                text = AppStrings.get("add", lang),
                icon = Icons.Default.Add,
                gradient = GradientViolet,
                height = 42.dp,
                onClick = onAddClass,
                modifier = Modifier.testTag("admin_add_class_button")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (classes.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.MeetingRoom,
                message = AppStrings.get("no_data", lang),
                accentColor = VioletAccent
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(classes) { classRoom ->
                    val studentCount = students.count { it.classId == classRoom.classId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPreviewClass(classRoom) },
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(Brush.linearGradient(GradientViolet)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Class, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        classRoom.name,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NavyPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = BlueSurface,
                                    border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        "${if (lang == "so") "Furaha" else "Password"}: ${classRoom.accessCode}",
                                        fontFamily = MonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "${AppStrings.get("class_teacher", lang)}: ${classRoom.classTeacherName.ifBlank { if (lang == "so") "Lama Xilsaarin" else "Not Assigned" }}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Text(
                                "${AppStrings.get("total_students", lang)}: $studentCount • ${classRoom.academicYear}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VibrantTintedButton(
                                    text = AppStrings.get("preview", lang),
                                    color = GreenAccent,
                                    icon = Icons.Default.Visibility,
                                    onClick = { onPreviewClass(classRoom) },
                                    modifier = Modifier.weight(1f).testTag("preview_class_${classRoom.classId}")
                                )

                                VibrantTintedButton(
                                    text = AppStrings.get("edit", lang),
                                    color = BlueSecondary,
                                    icon = Icons.Default.Edit,
                                    onClick = { onEditClass(classRoom) },
                                    modifier = Modifier.weight(1f).testTag("edit_class_${classRoom.classId}")
                                )

                                VibrantIconActionButton(
                                    icon = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    color = RedDanger,
                                    onClick = { onDeleteClass(classRoom.classId) },
                                    modifier = Modifier.testTag("delete_class_${classRoom.classId}")
                                )
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text("${AppStrings.get("class_preview", lang)}: ${classRoom.name}", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text(
                    "${AppStrings.get("class_teacher", lang)}: ${classRoom.classTeacherName.ifBlank { "-" }} • Password: ${classRoom.accessCode}",
                    fontSize = 12.sp,
                    color = BlueSecondary,
                    fontWeight = FontWeight.SemiBold
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
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(students.size) { index ->
                            val st = students[index]
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BlueSurface.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.22f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarImage(photoBase64 = st.photoUrl, name = st.fullName, size = 36.dp, accentColor = GreenAccent)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${index + 1}. ${st.fullName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                        Text("ID: ${st.studentId} • Code: ${st.linkingCode}", fontFamily = MonoFontFamily, fontSize = 11.sp, color = TextSecondary)
                                    }
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
        SectionHeaderRow(
            title = AppStrings.get("students", lang),
            subtitle = "${students.size}",
            accentColor = GreenAccent,
            icon = Icons.Default.School
        ) {
            VibrantGradientButton(
                text = AppStrings.get("add", lang),
                icon = Icons.Default.Add,
                gradient = GradientEmerald,
                height = 42.dp,
                onClick = onAddStudent,
                modifier = Modifier.testTag("admin_add_student_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyLight) },
            placeholder = { Text(AppStrings.get("search", lang)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = NavyLight,
                unfocusedBorderColor = BorderLight
            ),
            modifier = Modifier.fillMaxWidth().testTag("admin_student_search"),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedClassFilter == null,
                    onClick = { selectedClassFilter = null },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyLight,
                        selectedLabelColor = Color.White
                    ),
                    label = { Text(if (lang == "so") "Dhammaan" else "All", fontWeight = FontWeight.Bold) }
                )
            }
            items(classes) { c ->
                FilterChip(
                    selected = selectedClassFilter == c.classId,
                    onClick = { selectedClassFilter = c.classId },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyLight,
                        selectedLabelColor = Color.White
                    ),
                    label = { Text(c.name, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.PersonSearch,
                message = AppStrings.get("no_data", lang),
                accentColor = GreenAccent
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.22f)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                photoBase64 = student.photoUrl,
                                name = student.fullName,
                                size = 48.dp,
                                accentColor = GreenAccent
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    student.fullName,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    "ID: ${student.studentId} • ${student.className}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = AmberBackground,
                                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.45f)),
                                    modifier = Modifier.padding(top = 5.dp)
                                ) {
                                    Text(
                                        "Linking Code: ${student.linkingCode}",
                                        fontFamily = MonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            VibrantIconActionButton(
                                icon = Icons.Default.Edit,
                                contentDescription = "Edit",
                                color = BlueSecondary,
                                onClick = { onEditStudent(student) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            VibrantIconActionButton(
                                icon = Icons.Default.Delete,
                                contentDescription = "Delete",
                                color = RedDanger,
                                onClick = { onDeleteStudent(student.studentId) },
                                modifier = Modifier.testTag("delete_student_${student.studentId}")
                            )
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
        SectionHeaderRow(
            title = AppStrings.get("exam_folders", lang),
            subtitle = if (lang == "so")
                "Abuur folder-ka imtixaanka; fasallada oo dhan gudaha ayay ku soo aruurayaan"
            else
                "Create exam folders where all class submissions & student reports gather",
            accentColor = AmberDark,
            icon = Icons.Default.FolderSpecial
        ) {
            VibrantGradientButton(
                text = AppStrings.get("add", lang),
                icon = Icons.Default.CreateNewFolder,
                gradient = GradientAmber,
                height = 42.dp,
                onClick = onCreateFolder,
                modifier = Modifier.testTag("admin_create_exam_folder_button")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (folders.isEmpty() && unassignedExams.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.FolderOpen,
                message = AppStrings.get("no_data", lang),
                accentColor = AmberDark
            )
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
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(GradientAmber)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(folder.name, fontFamily = OutfitFontFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = NavyPrimary)
                                Text(
                                    "${folder.examType} • ${folder.academicYear}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BlueSecondary
                                )
                                Text(
                                    if (lang == "so")
                                        "Fasallada: ${folderExams.size} ($submittedCount la soo gudbiyay)"
                                    else
                                        "Classes: ${folderExams.size} ($submittedCount submitted)",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            VibrantIconActionButton(
                                icon = Icons.Default.Delete,
                                contentDescription = "Delete Folder",
                                color = RedDanger,
                                onClick = { onDeleteFolder(folder.folderId) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NavyPrimary)
                        }
                    }
                }

                if (unassignedExams.isNotEmpty()) {
                    item {
                        Text(
                            if (lang == "so") "Imtixaannada Aan Folder Lahayn" else "Other Class Examinations",
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(unassignedExams) { exam ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenExamStudents(exam) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exam.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                                    Text("${exam.className} • ${exam.academicYear}", fontSize = 12.sp, color = TextSecondary)
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(GradientAmber)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(folder.name, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 16.sp)
                    Text("${folder.examType} • ${folder.academicYear}", fontSize = 11.sp, color = TextSecondary)
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
                    color = TextSecondary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(folderExams) { exam ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BlueSurface.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.28f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exam.className, fontFamily = OutfitFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = NavyPrimary)
                                        Text(exam.name, fontSize = 12.sp, color = TextPrimary)
                                    }
                                    StatusBadge(status = exam.status, lang = lang)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VibrantGradientButton(
                                        text = if (lang == "so") "Ardayda & Report" else "Students & Reports",
                                        icon = Icons.Default.People,
                                        gradient = GradientRoyalBlue,
                                        height = 38.dp,
                                        fontSize = 11,
                                        onClick = { onInspectExamStudents(exam) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (exam.status != "PUBLISHED") {
                                        VibrantGradientButton(
                                            text = AppStrings.get("publish", lang),
                                            gradient = GradientEmerald,
                                            height = 38.dp,
                                            fontSize = 11,
                                            onClick = { viewModel.updateExamStatus(exam.examId, "PUBLISHED") },
                                            modifier = Modifier.weight(0.75f)
                                        )
                                    } else {
                                        VibrantTintedButton(
                                            text = AppStrings.get("lock", lang),
                                            color = RedDanger,
                                            height = 38.dp,
                                            fontSize = 11,
                                            onClick = { viewModel.updateExamStatus(exam.examId, "LOCKED") },
                                            modifier = Modifier.weight(0.75f)
                                        )
                                    }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text("${exam.className} • ${exam.name}", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 15.sp)
                Text(
                    if (lang == "so") "Taabo arday kasta si aad u aragto report kiisa" else "Tap any student to view their full report card",
                    fontSize = 11.sp,
                    color = BlueSecondary
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
                Text(AppStrings.get("no_data", lang), color = TextSecondary, fontSize = 13.sp)
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
                            colors = CardDefaults.cardColors(containerColor = BlueSurface.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(AmberWarning),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("#${res.position}", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF0F172A))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                AvatarImage(photoBase64 = st.photoUrl, name = st.fullName, size = 34.dp, accentColor = NavyLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                    Text(
                                        "${res.totalObtained.toInt()}/${res.totalMax.toInt()} (${String.format(Locale.US, "%.1f%%", res.percentage)}) • Grade ${res.grade}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
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
            VibrantGradientButton(text = "OK", gradient = GradientRoyalBlue, height = 40.dp, onClick = onDismiss)
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(AppStrings.get("view_report_card", lang), fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
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
                    VibrantGradientButton(
                        text = AppStrings.get("download_pdf", lang),
                        icon = Icons.Default.Download,
                        gradient = GradientRoyalBlue,
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            VibrantGradientButton(text = "OK", gradient = GradientEmerald, height = 40.dp, onClick = onDismiss)
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
        SectionHeaderRow(
            title = AppStrings.get("fees", lang),
            subtitle = "${feePayments.size}",
            accentColor = RedDanger,
            icon = Icons.Default.AccountBalanceWallet
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (feePayments.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Payments,
                message = AppStrings.get("no_data", lang),
                accentColor = RedDanger
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(feePayments) { p ->
                    val cardAccent = if (p.balance > 0) RedDanger else GreenAccent
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, cardAccent.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(p.studentName, fontFamily = OutfitFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                StatusBadge(status = p.status, lang = lang)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("${p.feeName} • ${p.className}", fontSize = 12.sp, color = TextSecondary)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${AppStrings.get("amount", lang)}: $${p.amount}", fontSize = 12.sp, color = TextSecondary)
                                Text("${AppStrings.get("amount_paid", lang)}: $${p.amountPaid}", fontSize = 12.sp, color = GreenAccent, fontWeight = FontWeight.Bold)
                                Text("${AppStrings.get("balance", lang)}: $${p.balance}", fontSize = 12.sp, color = if (p.balance > 0) RedDanger else GreenAccent, fontWeight = FontWeight.Bold)
                            }

                            if (p.balance > 0) {
                                VibrantGradientButton(
                                    text = AppStrings.get("record_payment", lang),
                                    icon = Icons.Default.CheckCircle,
                                    gradient = GradientEmerald,
                                    height = 38.dp,
                                    fontSize = 11,
                                    onClick = { showRecordPaymentDialog = p },
                                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                                )
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
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = { Text(AppStrings.get("record_payment", lang), fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = {
                Column {
                    Text("${p.studentName} • ${p.feeName}", fontWeight = FontWeight.Bold, color = BlueSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amtStr,
                        onValueChange = { amtStr = it },
                        label = { Text(AppStrings.get("amount", lang)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = receiptStr,
                        onValueChange = { receiptStr = it },
                        label = { Text(AppStrings.get("receipt_no", lang)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                VibrantGradientButton(
                    text = AppStrings.get("save", lang),
                    icon = Icons.Default.Check,
                    gradient = GradientEmerald,
                    height = 42.dp,
                    onClick = {
                        val amt = amtStr.toDoubleOrNull() ?: p.balance
                        onRecordPayment(p.paymentId, amt, receiptStr)
                        showRecordPaymentDialog = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRecordPaymentDialog = null }) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
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
            SectionHeaderRow(
                title = AppStrings.get("reports", lang),
                subtitle = "${school.name} • ${school.academicYear}",
                accentColor = NavyLight,
                icon = Icons.Default.Analytics
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NavyLight.copy(alpha = 0.25f)),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (lang == "so") "Warbixinta Guud ee Dugsiga" else "Executive Summary Report",
                        fontFamily = OutfitFontFamily,
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                AppStrings.get("teachers", lang) + " - " +
                    AppStrings.get(if (existingTeacher == null) "add" else "edit", lang),
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(AppStrings.get("phone", lang)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(AppStrings.get("email", lang)) },
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
                gradient = GradientRoyalBlue,
                height = 42.dp,
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), phone.trim(), email.trim(), photoBase64)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                AppStrings.get("classes", lang) + " - " +
                    AppStrings.get(if (existingClass == null) "add" else "edit", lang),
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = VioletAccent
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        AppStrings.get("select_teacher", lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
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
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VioletAccent,
                                        selectedLabelColor = Color.White
                                    ),
                                    leadingIcon = {
                                        AvatarImage(photoBase64 = teacher.photoUrl, name = teacher.fullName, size = 22.dp)
                                    },
                                    label = { Text(teacher.fullName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = { accessCode = it },
                        label = { Text(AppStrings.get("class_access_code", lang)) },
                        placeholder = { Text(if (lang == "so") "Tusaale: 4819 (ikhtiyaari)" else "e.g. 4819 (auto if blank)") },
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
                    if (className.isNotBlank()) {
                        onConfirm(className.trim(), selectedTeacherId, selectedTeacherName, academicYear.trim(), accessCode.trim())
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                AppStrings.get("students", lang) + " - " + AppStrings.get(if (existingStudent == null) "add" else "edit", lang),
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = GreenAccent
            )
        },
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
                item {
                    Text(AppStrings.get("class", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(classes) { c ->
                            FilterChip(
                                selected = selectedClassId == c.classId,
                                onClick = {
                                    selectedClassId = c.classId
                                    selectedClassName = c.name
                                },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenAccent, selectedLabelColor = Color.White),
                                label = { Text(c.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("save", lang),
                icon = Icons.Default.Check,
                gradient = GradientEmerald,
                height = 42.dp,
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), father.trim(), mother.trim(), phone.trim(), gender, selectedClassId, selectedClassName, admDate, photoBase64)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = { Text(AppStrings.get("create_exam_folder", lang), fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = AmberDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text(AppStrings.get("folder_name", lang)) },
                    placeholder = { Text("e.g. Midterm Term 1 / Final 2026") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(AppStrings.get("exam_type", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = type == "MONTHLY",
                            onClick = { type = "MONTHLY" },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyLight, selectedLabelColor = Color.White),
                            label = { Text("Monthly") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = type == "MIDTERM",
                            onClick = { type = "MIDTERM" },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AmberDark, selectedLabelColor = Color.White),
                            label = { Text("Midterm") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = type == "FINAL",
                            onClick = { type = "FINAL" },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenAccent, selectedLabelColor = Color.White),
                            label = { Text("Final") }
                        )
                    }
                }
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text(AppStrings.get("academic_year", lang)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(AppStrings.get("description", lang)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("save", lang),
                icon = Icons.Default.CreateNewFolder,
                gradient = GradientAmber,
                height = 42.dp,
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName.trim(), type, year.trim(), desc.trim())
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
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
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = { Text(AppStrings.get("settings", lang), fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("School Name") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Academic Year") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = passcode, onValueChange = { passcode = it }, label = { Text("Master Admin Passcode") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("save", lang),
                icon = Icons.Default.Check,
                gradient = GradientRoyalBlue,
                height = 42.dp,
                onClick = { onSave(name, address, phone, email, year, passcode) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger) }
        }
    )
}
