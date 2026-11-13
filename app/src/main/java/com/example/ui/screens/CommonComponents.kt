package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ExamResult
import com.example.data.models.Examination
import com.example.data.models.SchoolConfig
import com.example.data.models.Student
import com.example.data.repository.SchoolRepository
import com.example.localization.AppStrings
import com.example.ui.theme.*
import com.example.utils.AvatarImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SchoolTopBar(
    title: String,
    subtitle: String? = null,
    currentLang: String,
    onToggleLang: () -> Unit,
    onSignOut: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Surface(
        color = NavyPrimary,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = BlueLight,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = onToggleLang,
                    label = {
                        Text(
                            text = if (currentLang == "so") "SO | EN" else "EN | SO",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Language",
                            tint = AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = NavyDark
                    ),
                    border = null,
                    modifier = Modifier.testTag("language_toggle_button")
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFFC8181)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .then(
                if (isSelected) Modifier.border(2.dp, color, RoundedCornerShape(12.dp)) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, lang: String = "so") {
    val (label, bg, fg) = when (status.uppercase()) {
        "PRESENT", "JOOGA" -> Triple(AppStrings.get("present", lang), Color(0xFFC6F6D5), Color(0xFF22543D))
        "ABSENT", "MAQAN" -> Triple(AppStrings.get("absent", lang), Color(0xFFFED7D7), Color(0xFF742A2A))
        "LATE", "SOO DAAHAY" -> Triple(AppStrings.get("late", lang), Color(0xFFFEEBC8), Color(0xFF7B341E))
        "EXCUSED", "CUDURDAAR" -> Triple(AppStrings.get("excused", lang), Color(0xFFE2E8F0), Color(0xFF2D3748))
        "PAID", "DHAMEYSTIRAN" -> Triple(AppStrings.get("status_paid", lang), Color(0xFFC6F6D5), Color(0xFF22543D))
        "PARTIAL", "PARTIALLY_PAID" -> Triple(AppStrings.get("status_partial", lang), Color(0xFFFEEBC8), Color(0xFF7B341E))
        "UNPAID" -> Triple(AppStrings.get("status_unpaid", lang), Color(0xFFFED7D7), Color(0xFF742A2A))
        "PUBLISHED" -> Triple(AppStrings.get("status_published", lang), Color(0xFFC6F6D5), Color(0xFF22543D))
        "APPROVED" -> Triple(AppStrings.get("status_approved", lang), Color(0xFFBEE3F8), Color(0xFF2A4365))
        "SUBMITTED" -> Triple(AppStrings.get("status_submitted", lang), Color(0xFFE9D8FD), Color(0xFF44337A))
        "LOCKED" -> Triple(AppStrings.get("status_locked", lang), Color(0xFFEDF2F7), Color(0xFF4A5568))
        else -> Triple(status, Color(0xFFE2E8F0), Color(0xFF2D3748))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Formats "YYYY-MM-DD" into "Sabti 20/02/2026" (Somali) or "Saturday 20/02/2026" (English).
 */
fun formatDayAndDate(dateStr: String, lang: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsed = parser.parse(dateStr.trim()) ?: return dateStr
        val cal = Calendar.getInstance().apply { time = parsed }
        val dayName = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> if (lang == "so") "Sabti" else "Saturday"
            Calendar.SUNDAY -> if (lang == "so") "Axad" else "Sunday"
            Calendar.MONDAY -> if (lang == "so") "Isniin" else "Monday"
            Calendar.TUESDAY -> if (lang == "so") "Talaado" else "Tuesday"
            Calendar.WEDNESDAY -> if (lang == "so") "Arbaco" else "Wednesday"
            Calendar.THURSDAY -> if (lang == "so") "Khamiis" else "Thursday"
            Calendar.FRIDAY -> if (lang == "so") "Jimce" else "Friday"
            else -> ""
        }
        val dmy = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(parsed)
        "$dayName $dmy"
    } catch (_: Exception) {
        dateStr
    }
}

/**
 * Polished Visual Report Card with clear Subject Marks & Grade table for Parents, Teachers & Admin.
 */
@Composable
fun BeautifulReportCardView(
    school: SchoolConfig,
    student: Student,
    exam: Examination,
    result: ExamResult,
    lang: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // School Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(NavyPrimary, NavyDark)))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        AvatarImage(
                            photoBase64 = student.photoUrl,
                            name = student.fullName,
                            size = 48.dp,
                            accentColor = AmberWarning
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = school.name.uppercase(Locale.ROOT),
                                color = AmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = student.fullName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID: ${student.studentId} • ${student.className} • ${exam.academicYear}",
                                color = BlueLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberWarning
                    ) {
                        Text(
                            text = "#${result.position}",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Gold accent stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(AmberWarning)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exam.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyPrimary
                        )
                        if (exam.folderName.isNotBlank()) {
                            Text(
                                text = exam.folderName,
                                fontSize = 11.sp,
                                color = BlueSecondary
                            )
                        }
                    }
                    StatusBadge(status = exam.status, lang = lang)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Strip (Total, Percentage, Grade, Position)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F4F8))
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ReportSummaryPill(
                        label = if (lang == "so") "Wadarta" else "Total",
                        value = "${result.totalObtained.toInt()}/${result.totalMax.toInt()}",
                        color = NavyPrimary
                    )
                    ReportSummaryPill(
                        label = if (lang == "so") "Boqolkiiba" else "Percent",
                        value = String.format(Locale.US, "%.1f%%", result.percentage),
                        color = BlueSecondary
                    )
                    ReportSummaryPill(
                        label = if (lang == "so") "Darajada" else "Grade",
                        value = result.grade,
                        color = if (result.grade == "F") RedDanger else GreenAccent
                    )
                    ReportSummaryPill(
                        label = if (lang == "so") "Kaalinta" else "Rank",
                        value = "#${result.position}",
                        color = Color(0xFFB7791F)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Marks & Grade Table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyPrimary)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "so") "Maaddada" else "Subject",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(2.2f)
                        )
                        Text(
                            text = if (lang == "so") "Ugu Badan" else "Max",
                            color = BlueLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.9f)
                        )
                        Text(
                            text = if (lang == "so") "Buundada" else "Marks",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.9f)
                        )
                        Text(
                            text = if (lang == "so") "Darajo" else "Grade",
                            color = AmberWarning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    // Table Rows
                    val rows = if (exam.subjects.isNotEmpty()) {
                        exam.subjects.map { sub ->
                            val obt = result.subjectMarks[sub.name] ?: 0.0
                            Triple(sub.name, sub.maxMarks, obt)
                        }
                    } else {
                        result.subjectMarks.entries.map { (subName, obt) ->
                            Triple(subName, 100.0, obt)
                        }
                    }

                    if (rows.isEmpty()) {
                        Text(
                            text = AppStrings.get("no_data", lang),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        rows.forEachIndexed { idx, (subName, maxM, obtM) ->
                            val pct = if (maxM > 0) (obtM / maxM) * 100.0 else 0.0
                            val subGrade = SchoolRepository.calculateGrade(pct, school.gradingScale)
                            val gradeColor = when (subGrade) {
                                "A+", "A" -> GreenAccent
                                "B", "C" -> BlueSecondary
                                "D" -> AmberWarning
                                else -> RedDanger
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (idx % 2 == 0) Color.White else Color(0xFFF8FAFC))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.weight(2.2f)
                                )
                                Text(
                                    text = "${maxM.toInt()}",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = if (obtM % 1.0 == 0.0) "${obtM.toInt()}" else String.format(Locale.US, "%.1f", obtM),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Box(
                                    modifier = Modifier.weight(0.8f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = gradeColor.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = subGrade,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = gradeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        }

                        // Total Footer Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEBF8FF))
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "so") "WADARTA GUUD" else "TOTAL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary,
                                modifier = Modifier.weight(2.2f)
                            )
                            Text(
                                text = "${result.totalMax.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                text = "${result.totalObtained.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                text = result.grade,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (result.grade == "F") RedDanger else GreenAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }
                }

                // Teacher Evaluation / Deserving Remarks Box
                val remarks = result.teacherRemarks.ifBlank {
                    if (result.percentage >= 85) {
                        if (lang == "so") "Arday aad u dadaal badan, waxqabadkiisuna uu heer sare yahay." else "Outstanding performance and exemplary dedication."
                    } else if (result.percentage >= 65) {
                        if (lang == "so") "Waxqabad wanaagsan, sii wad dadaalkaaga." else "Good effort and steady progress."
                    } else {
                        if (lang == "so") "Waxaad u baahan tahay dadaal iyo dib-u-eegis dheeri ah." else "Needs additional support and focused revision."
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFFBEB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFB7791F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = AppStrings.get("teacher_remarks", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF744210)
                            )
                            Text(
                                text = remarks,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF4A5568)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
