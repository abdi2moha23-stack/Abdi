package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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

/**
 * Glossy multi-stop gradient button with tactile spring scale, subtle elevation, and rich icon styling.
 */
@Composable
fun VibrantGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradient: List<Color> = GradientRoyalBlue,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    fontSize: Int = 13
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_scale"
    )
    val effectiveColors = if (enabled) gradient else listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1))
    val shadowColor = effectiveColors.first().copy(alpha = 0.38f)

    Box(
        modifier = modifier
            .scale(scale)
            .heightIn(min = height)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(effectiveColors))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Luminous soft-tinted action button with colored border & crisp icon.
 */
@Composable
fun VibrantTintedButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 40.dp,
    fontSize: Int = 12
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tinted_btn_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .heightIn(min = height)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = color),
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.11f),
        border = BorderStroke(1.2.dp, color.copy(alpha = 0.42f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = color,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Compact circular tinted icon action button (for Edit, Delete, etc.) meeting 48dp touch target friendliness.
 */
@Composable
fun VibrantIconActionButton(
    icon: ImageVector,
    contentDescription: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(38.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.3f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    subtitle: String? = null,
    accentColor: Color = NavyLight,
    icon: ImageVector? = null,
    actionContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.72f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column {
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (actionContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            actionContent()
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    accentColor: Color = BlueSecondary
) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SchoolTopBar(
    title: String,
    subtitle: String? = null,
    currentLang: String,
    onToggleLang: () -> Unit,
    onSignOut: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(GradientCobaltDeep))
            .drawBehind {
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                    radius = size.height * 1.15f,
                    center = Offset(size.width * 0.92f, size.height * 0.1f)
                )
                drawCircle(
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    radius = size.height * 0.7f,
                    center = Offset(size.width * 0.18f, size.height * 1.1f)
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (onBack != null) {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable(onClick = onBack)
                            .testTag("back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                    Text(
                        text = title,
                        fontFamily = OutfitFontFamily,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = Color(0xFFBAE6FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = onToggleLang,
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("language_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Language",
                            tint = AmberWarning,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (currentLang == "so") "SO | EN" else "EN | SO",
                            color = Color.White,
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RedDanger.copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFFCA5A5).copy(alpha = 0.45f), CircleShape)
                        .clickable(onClick = onSignOut)
                        .testTag("sign_out_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFFECACA),
                        modifier = Modifier.size(18.dp)
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
            ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else color.copy(alpha = 0.24f)
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            color.copy(alpha = if (isSelected) 0.14f else 0.08f),
                            Color.White
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vibrant left vertical color indicator strip
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(color, color.copy(alpha = 0.55f))
                            )
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(color, color.copy(alpha = 0.78f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value,
                        fontFamily = OutfitFontFamily,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                if (onClick != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.13f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, lang: String = "so") {
    val (label, bg, fg) = when (status.uppercase()) {
        "PRESENT", "JOOGA" -> Triple(AppStrings.get("present", lang), GreenBackground, GreenAccent)
        "ABSENT", "MAQAN" -> Triple(AppStrings.get("absent", lang), RedBackground, RedDanger)
        "LATE", "SOO DAAHAY" -> Triple(AppStrings.get("late", lang), AmberBackground, AmberDark)
        "EXCUSED", "CUDURDAAR" -> Triple(AppStrings.get("excused", lang), BlueSurface, BlueSecondary)
        "PAID", "DHAMEYSTIRAN" -> Triple(AppStrings.get("status_paid", lang), GreenBackground, GreenAccent)
        "PARTIAL", "PARTIALLY_PAID" -> Triple(AppStrings.get("status_partial", lang), AmberBackground, AmberDark)
        "UNPAID" -> Triple(AppStrings.get("status_unpaid", lang), RedBackground, RedDanger)
        "PUBLISHED" -> Triple(AppStrings.get("status_published", lang), GreenBackground, GreenAccent)
        "APPROVED" -> Triple(AppStrings.get("status_approved", lang), BlueSurface, NavyLight)
        "SUBMITTED" -> Triple(AppStrings.get("status_submitted", lang), VioletBackground, VioletAccent)
        "LOCKED" -> Triple(AppStrings.get("status_locked", lang), Color(0xFFF1F5F9), Color(0xFF475569))
        else -> Triple(status, BlueSurface, NavyPrimary)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
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
 * Polished Visual Report Card with vibrant jewel headers & clear Subject Marks & Grade table.
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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, NavyLight.copy(alpha = 0.25f)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Vibrant Cobalt-to-Cerulean Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(GradientCobaltDeep))
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                            radius = size.height * 0.9f,
                            center = Offset(size.width * 0.92f, size.height * 0.2f)
                        )
                    }
                    .padding(16.dp)
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
                            size = 50.dp,
                            accentColor = AmberWarning
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = school.name.uppercase(Locale.ROOT),
                                color = AmberWarning,
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.9.sp
                            )
                            Text(
                                text = student.fullName,
                                fontFamily = OutfitFontFamily,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID: ${student.studentId} • ${student.className} • ${exam.academicYear}",
                                color = Color(0xFFBAE6FD),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AmberWarning,
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "#${result.position}",
                            fontFamily = OutfitFontFamily,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Tri-color Rainbow Academic Stripe (Blue -> Emerald -> Amber)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(NavyLight, GreenLight, AmberWarning)
                        )
                    )
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
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavyPrimary
                        )
                        if (exam.folderName.isNotBlank()) {
                            Text(
                                text = exam.folderName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BlueSecondary
                            )
                        }
                    }
                    StatusBadge(status = exam.status, lang = lang)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Colorful Summary Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(BlueSurface, GreenBackground.copy(alpha = 0.7f))
                            )
                        )
                        .border(1.dp, BlueSecondary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .padding(vertical = 11.dp, horizontal = 8.dp),
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
                        color = AmberDark
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Marks & Grade Table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BlueSecondary.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(NavyPrimary, NavyLight)))
                            .padding(horizontal = 10.dp, vertical = 9.dp),
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
                            color = Color(0xFFBAE6FD),
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
                            color = TextSecondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        rows.forEachIndexed { idx, (subName, maxM, obtM) ->
                            val pct = if (maxM > 0) (obtM / maxM) * 100.0 else 0.0
                            val subGrade = SchoolRepository.calculateGrade(pct, school.gradingScale)
                            val gradeColor = when (subGrade) {
                                "A+", "A" -> GreenAccent
                                "B", "C" -> BlueSecondary
                                "D" -> AmberDark
                                else -> RedDanger
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (idx % 2 == 0) Color.White else Color(0xFFF5F8FF))
                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(2.2f)
                                )
                                Text(
                                    text = "${maxM.toInt()}",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = if (obtM % 1.0 == 0.0) "${obtM.toInt()}" else String.format(Locale.US, "%.1f", obtM),
                                    fontFamily = MonoFontFamily,
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
                                        shape = RoundedCornerShape(8.dp),
                                        color = gradeColor.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, gradeColor.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = subGrade,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = gradeColor,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BlueSurface)
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "so") "WADARTA GUUD" else "TOTAL",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary,
                                modifier = Modifier.weight(2.2f)
                            )
                            Text(
                                text = "${result.totalMax.toInt()}",
                                fontFamily = MonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                text = "${result.totalObtained.toInt()}",
                                fontFamily = MonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                text = result.grade,
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (result.grade == "F") RedDanger else GreenAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }
                }

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
                    shape = RoundedCornerShape(12.dp),
                    color = AmberBackground,
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(11.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AmberWarning.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AmberDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppStrings.get("teacher_remarks", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = remarks,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = TextPrimary
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
        Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        Text(value, fontFamily = OutfitFontFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
