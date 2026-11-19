package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.localization.AppStrings
import com.example.pdf.ReportCardPdfGenerator
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.utils.AvatarImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentDashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val linkedStudents by viewModel.linkedStudents.collectAsState()
    val selectedChild by viewModel.selectedChild.collectAsState()
    val allExams by viewModel.examinations.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val allFeePayments by viewModel.feePayments.collectAsState()

    var showAddChildDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("RESULTS") }

    Scaffold(
        topBar = {
            SchoolTopBar(
                title = schoolConfig.name,
                subtitle = AppStrings.get("parent_portal", lang) + ": " + (userProfile?.name ?: "Parent"),
                currentLang = lang,
                onToggleLang = { viewModel.setLanguage(if (lang == "so") "en" else "so") },
                onSignOut = { viewModel.signOut { onBack() } },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
        ) {
            // Children Selector Bar & Colorful Add Child Button
            Surface(
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(BlueSurface.copy(alpha = 0.45f), Color.White)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Brush.linearGradient(GradientEmerald)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "so") "Carruurtaada:" else "Your Children:",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = NavyPrimary
                            )
                        }

                        VibrantGradientButton(
                            text = AppStrings.get("add_child", lang),
                            icon = Icons.Default.Add,
                            gradient = GradientEmerald,
                            height = 38.dp,
                            fontSize = 12,
                            onClick = { showAddChildDialog = true },
                            modifier = Modifier.testTag("parent_add_child_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (linkedStudents.isEmpty()) {
                        Surface(
                            color = AmberBackground,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AmberDark)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    if (lang == "so")
                                        "Weli ma aadan xiriirin arday. Fadlan riix '+ Ku Dar Arday' adigoo isticmaalaya ID-ga iyo Furaha Sirta ah."
                                    else
                                        "No children linked yet. Tap '+ Add Child' using the Student ID and Linking Code provided by school.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(linkedStudents) { student ->
                                val isSelected = selectedChild?.studentId == student.studentId
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) Color.Transparent else Color.White,
                                    shadowElevation = if (isSelected) 5.dp else 1.dp,
                                    border = if (isSelected)
                                        BorderStroke(1.5.dp, AmberWarning)
                                    else
                                        BorderStroke(1.dp, BorderLight),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { viewModel.selectChild(student) }
                                        .testTag("child_selector_${student.studentId}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .then(
                                                if (isSelected)
                                                    Modifier.background(Brush.horizontalGradient(GradientRoyalBlue))
                                                else
                                                    Modifier.background(Color.White)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarImage(
                                            photoBase64 = student.photoUrl,
                                            name = student.fullName,
                                            size = 36.dp,
                                            accentColor = if (isSelected) AmberWarning else NavyLight
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                student.fullName,
                                                fontFamily = OutfitFontFamily,
                                                color = if (isSelected) Color.White else NavyPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                student.className,
                                                color = if (isSelected) Color(0xFFBAE6FD) else BlueSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedChild != null) {
                val child = selectedChild!!

                // Vibrant Color-Coded 4-Tab Pill Strip (Results, Attendance, Fees, Remarks)
                val tabs = listOf(
                    Triple("RESULTS", AppStrings.get("exams", lang), NavyLight),
                    Triple("ATTENDANCE", AppStrings.get("attendance", lang), GreenAccent),
                    Triple("FEES", AppStrings.get("fees", lang), RedDanger),
                    Triple("REMARKS", AppStrings.get("remarks", lang), VioletAccent)
                )

                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tabs.forEach { (key, label, tabColor) ->
                            val isSelected = selectedTab == key
                            Surface(
                                onClick = { selectedTab = key },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) tabColor else tabColor.copy(alpha = 0.09f),
                                border = BorderStroke(1.dp, if (isSelected) tabColor else tabColor.copy(alpha = 0.25f)),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                                    Text(
                                        text = label,
                                        fontFamily = OutfitFontFamily,
                                        color = if (isSelected) Color.White else tabColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        "RESULTS" -> ParentChildResultsTab(
                            student = child,
                            allExams = allExams,
                            school = schoolConfig,
                            lang = lang,
                            viewModel = viewModel
                        )
                        "ATTENDANCE" -> ParentChildAttendanceTab(
                            student = child,
                            allAttendance = allAttendance,
                            lang = lang
                        )
                        "FEES" -> ParentChildFeesTab(
                            student = child,
                            allFeePayments = allFeePayments.filter { it.studentId == child.studentId && (it.isPublished || it.amountPaid > 0) },
                            viewModel = viewModel,
                            lang = lang
                        )
                        "REMARKS" -> ParentChildRemarksTab(
                            student = child,
                            viewModel = viewModel,
                            lang = lang
                        )
                    }
                }
            } else {
                EmptyStateCard(
                    icon = Icons.Default.ChildCare,
                    message = if (lang == "so")
                        "Fadlan ku dar ama dooro ilmahaaga si aad u aragto xogta."
                    else
                        "Please link or select your child to view their records.",
                    accentColor = GreenAccent
                )
            }
        }
    }

    // Add Child Modal Dialog
    if (showAddChildDialog) {
        ParentAddChildDialog(
            lang = lang,
            onDismiss = { showAddChildDialog = false },
            onLinkChild = { studentId, linkingCode ->
                viewModel.linkChild(studentId, linkingCode) { success ->
                    if (success) {
                        showAddChildDialog = false
                    }
                }
            }
        )
    }
}

// 1. Parent Results Tab (First shows Student Result Summary Card; tapping Preview opens full certificate Report Card)
@Composable
fun ParentChildResultsTab(
    student: Student,
    allExams: List<Examination>,
    school: SchoolConfig,
    lang: String,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val classes by viewModel.classes.collectAsState()
    val publishedExams = allExams.filter { it.classId == student.classId && it.status == "PUBLISHED" }
    val resultsState by viewModel.repository.observeStudentResults(student.studentId).collectAsState(initial = emptyList())
    val classTeacherName = remember(classes, student.classId) {
        classes.firstOrNull { it.classId == student.classId }?.classTeacherName.orEmpty()
    }

    var previewExamAndResult by remember { mutableStateOf<Pair<Examination, ExamResult>?>(null) }

    if (publishedExams.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Default.HourglassEmpty,
            message = if (lang == "so")
                "Ma jiraan natiijooyin imtixaan oo weli la daabacay. Maamulka dugsiga ayaa soo daabici doona."
            else
                "No examination results have been published yet. The school administration will publish them soon.",
            accentColor = NavyLight
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(publishedExams) { exam ->
                val result = resultsState.firstOrNull { it.examId == exam.examId } ?: ExamResult(
                    examId = exam.examId,
                    examName = exam.name,
                    studentId = student.studentId,
                    studentName = student.fullName,
                    classId = student.classId,
                    className = student.className,
                    subjectMarks = exam.subjects.associate { it.name to 0.0 },
                    totalObtained = 0.0,
                    totalMax = exam.subjects.sumOf { it.maxMarks },
                    percentage = 0.0,
                    grade = "-",
                    position = 0L
                )

                StudentResultSummaryCard(
                    student = student,
                    exam = exam,
                    result = result,
                    lang = lang,
                    onPreview = { previewExamAndResult = exam to result },
                    onDownloadPdf = {
                        val pdfFile = ReportCardPdfGenerator.generateReportCard(
                            context = context,
                            school = school,
                            student = student,
                            exam = exam,
                            result = result,
                            language = lang,
                            classTeacherName = classTeacherName
                        )
                        if (pdfFile != null) {
                            ReportCardPdfGenerator.shareReportCardPdf(context, pdfFile)
                        } else {
                            Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("parent_download_report_card")
                )
            }
        }
    }

    if (previewExamAndResult != null) {
        val (exam, result) = previewExamAndResult!!
        StudentReportPreviewDialog(
            student = student,
            exam = exam,
            result = result,
            school = school,
            lang = lang,
            classTeacherName = classTeacherName,
            onDownloadPdf = {
                val pdfFile = ReportCardPdfGenerator.generateReportCard(
                    context = context,
                    school = school,
                    student = student,
                    exam = exam,
                    result = result,
                    language = lang,
                    classTeacherName = classTeacherName
                )
                if (pdfFile != null) {
                    ReportCardPdfGenerator.shareReportCardPdf(context, pdfFile)
                } else {
                    Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { previewExamAndResult = null }
        )
    }
}

// 2. Parent Attendance Tab (Clickable Stat Cards showing numbered Day + Date breakdown!)
@Composable
fun ParentChildAttendanceTab(
    student: Student,
    allAttendance: List<DailyAttendance>,
    lang: String
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val classAttendance = remember(allAttendance, student.classId) {
        allAttendance.filter { it.classId == student.classId }.sortedBy { it.date }
    }

    val todayRecord = classAttendance.firstOrNull { it.date == todayStr }?.records?.firstOrNull { it.studentId == student.studentId }
    val todayStatus = todayRecord?.status ?: "PENDING"

    val presentDates = remember(classAttendance, student.studentId) {
        classAttendance.filter { att -> att.records.any { it.studentId == student.studentId && it.status == "PRESENT" } }.map { it.date }
    }
    val absentDates = remember(classAttendance, student.studentId) {
        classAttendance.filter { att -> att.records.any { it.studentId == student.studentId && it.status == "ABSENT" } }.map { it.date }
    }
    val lateDates = remember(classAttendance, student.studentId) {
        classAttendance.filter { att -> att.records.any { it.studentId == student.studentId && it.status == "LATE" } }.map { it.date }
    }
    val excusedDates = remember(classAttendance, student.studentId) {
        classAttendance.filter { att -> att.records.any { it.studentId == student.studentId && it.status == "EXCUSED" } }.map { it.date }
    }

    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    val selectedDatesList = when (selectedStatusFilter) {
        "PRESENT" -> presentDates
        "ABSENT" -> absentDates
        "LATE" -> lateDates
        "EXCUSED" -> excusedDates
        else -> emptyList()
    }

    val selectedLabel = when (selectedStatusFilter) {
        "PRESENT" -> AppStrings.get("present", lang)
        "ABSENT" -> AppStrings.get("absent", lang)
        "LATE" -> AppStrings.get("late", lang)
        "EXCUSED" -> AppStrings.get("excused", lang)
        else -> ""
    }

    val selectedColor = when (selectedStatusFilter) {
        "PRESENT" -> GreenAccent
        "ABSENT" -> RedDanger
        "LATE" -> AmberDark
        "EXCUSED" -> BlueSecondary
        else -> NavyPrimary
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today Status Gradient Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.28f)),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(GreenBackground, Color.White)))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (lang == "so") "Xaadiriska Maanta (${formatDayAndDate(todayStr, lang)})" else "Today (${formatDayAndDate(todayStr, lang)})",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyPrimary
                        )
                        Text(student.fullName, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                    StatusBadge(status = todayStatus, lang = lang)
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BlueSurface,
                border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = BlueSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        AppStrings.get("tap_to_view_dates", lang),
                        fontSize = 12.sp,
                        color = NavyPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Clickable Summary Counters
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = AppStrings.get("present", lang),
                    value = "${presentDates.size}",
                    icon = Icons.Default.CheckCircle,
                    color = GreenAccent,
                    modifier = Modifier.weight(1f).testTag("parent_att_present"),
                    isSelected = selectedStatusFilter == "PRESENT",
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == "PRESENT") null else "PRESENT" }
                )
                StatCard(
                    title = AppStrings.get("absent", lang),
                    value = "${absentDates.size}",
                    icon = Icons.Default.Cancel,
                    color = RedDanger,
                    modifier = Modifier.weight(1f).testTag("parent_att_absent"),
                    isSelected = selectedStatusFilter == "ABSENT",
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == "ABSENT") null else "ABSENT" }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = AppStrings.get("late", lang),
                    value = "${lateDates.size}",
                    icon = Icons.Default.AccessTime,
                    color = AmberWarning,
                    modifier = Modifier.weight(1f).testTag("parent_att_late"),
                    isSelected = selectedStatusFilter == "LATE",
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == "LATE") null else "LATE" }
                )
                StatCard(
                    title = AppStrings.get("excused", lang),
                    value = "${excusedDates.size}",
                    icon = Icons.Default.HelpOutline,
                    color = BlueSecondary,
                    modifier = Modifier.weight(1f).testTag("parent_att_excused"),
                    isSelected = selectedStatusFilter == "EXCUSED",
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == "EXCUSED") null else "EXCUSED" }
                )
            }
        }

        // Numbered Day + Date Breakdown List when tapped!
        item {
            AnimatedVisibility(
                visible = selectedStatusFilter != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (selectedStatusFilter != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, selectedColor.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$selectedLabel (${selectedDatesList.size})",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = selectedColor
                                )
                                VibrantTintedButton(
                                    text = if (lang == "so") "Xir" else "Close",
                                    color = selectedColor,
                                    height = 32.dp,
                                    fontSize = 11,
                                    onClick = { selectedStatusFilter = null }
                                )
                            }

                            HorizontalDivider(color = selectedColor.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                            if (selectedDatesList.isEmpty()) {
                                Text(
                                    text = if (lang == "so") "Ma jirto maalin xaaladdan leh." else "No dates recorded for this status.",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    selectedDatesList.forEachIndexed { idx, dateStr ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = selectedColor.copy(alpha = 0.07f),
                                            border = BorderStroke(1.dp, selectedColor.copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(selectedColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${idx + 1}",
                                                        fontFamily = OutfitFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.White
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "${idx + 1}. ${formatDayAndDate(dateStr, lang)}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NavyPrimary
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
    }
}

// 3. Parent Fees Tab (Published Fees + Payment Receipts & Notifications)
@Composable
fun ParentChildFeesTab(
    student: Student,
    allFeePayments: List<FeePayment>,
    viewModel: MainViewModel,
    lang: String
) {
    val parentNotifications by viewModel.repository
        .observeNotifications("PARENT", listOf(student.studentId))
        .collectAsState(initial = emptyList())

    val childFeeNotifs = remember(parentNotifications, student.studentId) {
        parentNotifications.filter { it.targetStudentId == student.studentId }
    }

    val totalBilled = allFeePayments.sumOf { it.amount }
    val totalPaid = allFeePayments.sumOf { it.amountPaid }
    val totalBalance = allFeePayments.sumOf { it.balance }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (totalBalance > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedBackground),
                    border = BorderStroke(1.5.dp, RedDanger.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(RedDanger),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (lang == "so")
                                "Waalid qaddarin leh, waxaa ilmahaaga lagu leeyahay fiiga dugsiga oo dhan $$totalBalance."
                            else
                                "Dear Parent, your child's outstanding school fee balance of $$totalBalance is due.",
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(if (lang == "so") "Wadarta Fiiga" else "Total Fee", "$$totalBilled", Icons.Default.AttachMoney, NavyLight, Modifier.weight(1f))
                StatCard(AppStrings.get("paid_btn", lang), "$$totalPaid", Icons.Default.Check, GreenAccent, Modifier.weight(1f))
            }
        }

        item {
            StatCard(AppStrings.get("balance", lang), "$$totalBalance", Icons.Default.HourglassBottom, if (totalBalance > 0) RedDanger else GreenAccent)
        }

        if (childFeeNotifs.isNotEmpty()) {
            item {
                SectionHeaderRow(
                    title = if (lang == "so") "Ogeysiisyada & Rasiidyada Fiiga" else "Fee Notifications & Receipts",
                    accentColor = GreenAccent,
                    icon = Icons.Default.ReceiptLong
                )
            }
            items(childFeeNotifs) { notif ->
                val hasReceipt = notif.receiptNo.isNotBlank()
                val accent = if (hasReceipt) GreenAccent else BlueSecondary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasReceipt) GreenBackground else BlueSurface
                    ),
                    border = BorderStroke(1.2.dp, accent.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(notif.title, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                            if (hasReceipt) {
                                Surface(shape = RoundedCornerShape(50), color = GreenAccent) {
                                    Text(
                                        notif.receiptNo,
                                        fontFamily = MonoFontFamily,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(notif.message, fontSize = 12.sp, color = TextPrimary)
                        Text(notif.date, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        item {
            SectionHeaderRow(
                title = if (lang == "so") "Biilasha & Lacag Bixinta" else "Fee Invoices & Payments",
                accentColor = NavyLight,
                icon = Icons.Default.AccountBalanceWallet
            )
        }

        if (allFeePayments.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Receipt,
                    message = AppStrings.get("no_data", lang),
                    accentColor = BlueSecondary
                )
            }
        } else {
            items(allFeePayments) { p ->
                val cardAccent = if (p.balance > 0) RedDanger else GreenAccent
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, cardAccent.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.feeName, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                            StatusBadge(status = p.status, lang = lang)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${AppStrings.get("amount", lang)}: $${p.amount} • ${AppStrings.get("amount_paid", lang)}: $${p.amountPaid} • ${AppStrings.get("balance", lang)}: $${p.balance}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        if (p.paymentDate.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GreenBackground
                            ) {
                                Text(
                                    "${AppStrings.get("date", lang)}: ${p.paymentDate} • ${AppStrings.get("receipt_no", lang)}: ${p.receiptNo}",
                                    fontFamily = MonoFontFamily,
                                    fontSize = 11.sp,
                                    color = GreenAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. Parent Remarks Tab
@Composable
fun ParentChildRemarksTab(
    student: Student,
    viewModel: MainViewModel,
    lang: String
) {
    val remarksList by viewModel.repository.observeRemarksForStudent(student.studentId).collectAsState(initial = emptyList())

    if (remarksList.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Default.MarkChatRead,
            message = AppStrings.get("no_data", lang),
            accentColor = VioletAccent
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(remarksList) { remark ->
                val catColor = when (remark.category) {
                    "NEEDS_IMPROVEMENT" -> AmberDark
                    "EXCELLENT_BEHAVIOR" -> BlueSecondary
                    else -> GreenAccent
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, catColor.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = catColor.copy(alpha = 0.13f)
                            ) {
                                Text(
                                    remark.category.replace("_", " "),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = catColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(remark.date, fontSize = 11.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(remark.message, fontSize = 13.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Teacher: ${remark.teacherName}", fontSize = 11.sp, color = VioletAccent, fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// Modal: Add Child (+)
@Composable
fun ParentAddChildDialog(
    lang: String,
    onDismiss: () -> Unit,
    onLinkChild: (String, String) -> Unit
) {
    var studentId by remember { mutableStateOf("") }
    var linkingCode by remember { mutableStateOf("") }

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
                        .background(Brush.linearGradient(GradientEmerald)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(AppStrings.get("add_child", lang), fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (lang == "so")
                        "Fadlan geli Aqoonsiga Ardayga (Student ID) iyo Furaha Sirta ah (Linking Code) ee dugsigu ku siiyay."
                    else
                        "Enter the Student ID and confidential Parent Linking Code provided by the school administration.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text(AppStrings.get("student_id", lang)) },
                    placeholder = { Text("e.g. IFT-101") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("parent_child_id_input")
                )

                OutlinedTextField(
                    value = linkingCode,
                    onValueChange = { linkingCode = it },
                    label = { Text(AppStrings.get("linking_code", lang)) },
                    placeholder = { Text("e.g. 749201") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("parent_child_linking_code_input")
                )
            }
        },
        confirmButton = {
            VibrantGradientButton(
                text = AppStrings.get("link_student", lang),
                icon = Icons.Default.Link,
                gradient = GradientEmerald,
                height = 42.dp,
                onClick = {
                    if (studentId.isNotBlank() && linkingCode.isNotBlank()) {
                        onLinkChild(studentId.trim(), linkingCode.trim())
                    }
                },
                modifier = Modifier.testTag("parent_confirm_link_child")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang), color = RedDanger, fontWeight = FontWeight.SemiBold) }
        }
    )
}
