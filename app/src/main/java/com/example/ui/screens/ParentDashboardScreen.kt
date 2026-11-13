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
import androidx.compose.ui.text.style.TextAlign
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
    // ONLY 4 tabs: RESULTS, ATTENDANCE, FEES, REMARKS (Homework & Timetable completely removed)
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
            // Children Selector Bar & Add Child Button
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "so") "Carruurtaada:" else "Your Children:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyPrimary
                        )

                        Button(
                            onClick = { showAddChildDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("parent_add_child_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.get("add_child", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (linkedStudents.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCBF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF744210))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (lang == "so")
                                        "Weli ma aadan xiriirin arday. Fadlan riix '+ Ku Dar Arday' adigoo isticmaalaya ID-ga iyo Furaha Sirta ah."
                                    else
                                        "No children linked yet. Tap '+ Add Child' using the Student ID and Linking Code provided by school.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF744210)
                                )
                            }
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(linkedStudents) { student ->
                                val isSelected = selectedChild?.studentId == student.studentId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) NavyPrimary else Color(0xFFF0F4F8),
                                    border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder,
                                    modifier = Modifier
                                        .clickable { viewModel.selectChild(student) }
                                        .testTag("child_selector_${student.studentId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarImage(
                                            photoBase64 = student.photoUrl,
                                            name = student.fullName,
                                            size = 34.dp,
                                            accentColor = if (isSelected) AmberWarning else NavyPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                student.fullName,
                                                color = if (isSelected) Color.White else NavyPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                student.className,
                                                color = if (isSelected) BlueLight else Color.Gray,
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

            if (selectedChild != null) {
                val child = selectedChild!!

                // 4 Focused Parent Tabs (Homework & Timetable Removed)
                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        "RESULTS" -> 0
                        "ATTENDANCE" -> 1
                        "FEES" -> 2
                        "REMARKS" -> 3
                        else -> 0
                    },
                    containerColor = Color.White,
                    contentColor = NavyPrimary
                ) {
                    Tab(
                        selected = selectedTab == "RESULTS",
                        onClick = { selectedTab = "RESULTS" },
                        text = { Text(AppStrings.get("exams", lang), fontSize = 12.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTab == "ATTENDANCE",
                        onClick = { selectedTab = "ATTENDANCE" },
                        text = { Text(AppStrings.get("attendance", lang), fontSize = 12.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTab == "FEES",
                        onClick = { selectedTab = "FEES" },
                        text = { Text(AppStrings.get("fees", lang), fontSize = 12.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTab == "REMARKS",
                        onClick = { selectedTab = "REMARKS" },
                        text = { Text(AppStrings.get("remarks", lang), fontSize = 12.sp, maxLines = 1) }
                    )
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (lang == "so") "Fadlan ku dar ama dooro ilmahaaga si aad u aragto xogta." else "Please link or select your child to view their records.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
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

// 1. Parent Results Tab (Beautiful Visual Report Card with Subject Marks & Grade Table)
@Composable
fun ParentChildResultsTab(
    student: Student,
    allExams: List<Examination>,
    school: SchoolConfig,
    lang: String,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val publishedExams = allExams.filter { it.classId == student.classId && it.status == "PUBLISHED" }
    val resultsState by viewModel.repository.observeStudentResults(student.studentId).collectAsState(initial = emptyList())

    if (publishedExams.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    if (lang == "so")
                        "Ma jiraan natiijooyin imtixaan oo weli la daabacay. Maamulka dugsiga ayaa soo daabici doona."
                    else
                        "No examination results have been published yet. The school administration will publish them soon.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

                Column {
                    BeautifulReportCardView(
                        school = school,
                        student = student,
                        exam = exam,
                        result = result,
                        lang = lang
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                                Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_download_report_card")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppStrings.get("download_pdf", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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
        "LATE" -> AmberWarning
        "EXCUSED" -> Color.Gray
        else -> NavyPrimary
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            if (lang == "so") "Xaadiriska Maanta (${formatDayAndDate(todayStr, lang)})" else "Today (${formatDayAndDate(todayStr, lang)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(student.fullName, fontSize = 12.sp, color = Color.Gray)
                    }
                    StatusBadge(status = todayStatus, lang = lang)
                }
            }
        }

        item {
            Text(
                AppStrings.get("tap_to_view_dates", lang),
                fontSize = 12.sp,
                color = BlueSecondary,
                fontWeight = FontWeight.SemiBold
            )
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
                    color = Color.Gray,
                    modifier = Modifier.weight(1f).testTag("parent_att_excused"),
                    isSelected = selectedStatusFilter == "EXCUSED",
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == "EXCUSED") null else "EXCUSED" }
                )
            }
        }

        // Numbered Day + Date Breakdown List when tapped!
        if (selectedStatusFilter != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, selectedColor.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$selectedLabel (${selectedDatesList.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = selectedColor
                            )
                            TextButton(onClick = { selectedStatusFilter = null }) {
                                Text(if (lang == "so") "Xir" else "Close", fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 8.dp))

                        if (selectedDatesList.isEmpty()) {
                            Text(
                                text = if (lang == "so") "Ma jirto maalin xaaladdan leh." else "No dates recorded for this status.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        } else {
                            selectedDatesList.forEachIndexed { idx, dateStr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = selectedColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${idx + 1}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = selectedColor
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${idx + 1}. ${formatDayAndDate(dateStr, lang)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                    border = BorderStroke(1.dp, RedDanger),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RedDanger)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            if (lang == "so")
                                "Waalid qaddarin leh, waxaa ilmahaaga lagu leeyahay fiiga dugsiga oo dhan $$totalBalance."
                            else
                                "Dear Parent, your child's outstanding school fee balance of $$totalBalance is due.",
                            fontSize = 13.sp,
                            color = RedDanger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(if (lang == "so") "Wadarta Fiiga" else "Total Fee", "$$totalBilled", Icons.Default.AttachMoney, NavyPrimary, Modifier.weight(1f))
                StatCard(AppStrings.get("paid_btn", lang), "$$totalPaid", Icons.Default.Check, GreenAccent, Modifier.weight(1f))
            }
        }

        item {
            StatCard(AppStrings.get("balance", lang), "$$totalBalance", Icons.Default.HourglassBottom, if (totalBalance > 0) RedDanger else GreenAccent)
        }

        if (childFeeNotifs.isNotEmpty()) {
            item {
                Text(
                    if (lang == "so") "Ogeysiisyada & Rasiidyada Fiiga" else "Fee Notifications & Receipts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavyPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(childFeeNotifs) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (notif.receiptNo.isNotBlank()) Color(0xFFF0FFF4) else Color(0xFFEBF8FF)
                    ),
                    border = BorderStroke(1.dp, if (notif.receiptNo.isNotBlank()) GreenAccent.copy(alpha = 0.4f) else BlueSecondary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                            if (notif.receiptNo.isNotBlank()) {
                                Surface(shape = RoundedCornerShape(4.dp), color = GreenAccent) {
                                    Text(
                                        notif.receiptNo,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notif.message, fontSize = 12.sp, color = Color.DarkGray)
                        Text(notif.date, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        item {
            Text(
                if (lang == "so") "Biilasha & Lacag Bixinta" else "Fee Invoices & Payments",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (allFeePayments.isEmpty()) {
            item {
                Text(AppStrings.get("no_data", lang), color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            items(allFeePayments) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.feeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            StatusBadge(status = p.status, lang = lang)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${AppStrings.get("amount", lang)}: $${p.amount} • ${AppStrings.get("amount_paid", lang)}: $${p.amountPaid} • ${AppStrings.get("balance", lang)}: $${p.balance}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        if (p.paymentDate.isNotBlank()) {
                            Text(
                                "${AppStrings.get("date", lang)}: ${p.paymentDate} • ${AppStrings.get("receipt_no", lang)}: ${p.receiptNo}",
                                fontSize = 11.sp,
                                color = GreenAccent,
                                fontWeight = FontWeight.SemiBold
                            )
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(AppStrings.get("no_data", lang), color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(remarksList) { remark ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(remark.category.replace("_", " "), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GreenAccent)
                            Text(remark.date, fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(remark.message, fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Teacher: ${remark.teacherName}", fontSize = 11.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
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
        title = { Text(AppStrings.get("add_child", lang), fontWeight = FontWeight.Bold, color = NavyPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (lang == "so")
                        "Fadlan geli Aqoonsiga Ardayga (Student ID) iyo Furaha Sirta ah (Linking Code) ee dugsigu ku siiyay."
                    else
                        "Enter the Student ID and confidential Parent Linking Code provided by the school administration.",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text(AppStrings.get("student_id", lang)) },
                    placeholder = { Text("e.g. IFT-101") },
                    modifier = Modifier.fillMaxWidth().testTag("parent_child_id_input")
                )

                OutlinedTextField(
                    value = linkingCode,
                    onValueChange = { linkingCode = it },
                    label = { Text(AppStrings.get("linking_code", lang)) },
                    placeholder = { Text("e.g. 749201") },
                    modifier = Modifier.fillMaxWidth().testTag("parent_child_linking_code_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (studentId.isNotBlank() && linkingCode.isNotBlank()) {
                        onLinkChild(studentId.trim(), linkingCode.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                modifier = Modifier.testTag("parent_confirm_link_child")
            ) {
                Text(AppStrings.get("link_student", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.get("cancel", lang)) }
        }
    )
}
