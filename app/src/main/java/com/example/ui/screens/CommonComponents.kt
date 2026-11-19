package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.models.ExamResult
import com.example.data.models.Examination
import com.example.data.models.SchoolConfig
import com.example.data.models.Student
import com.example.data.repository.SchoolRepository
import com.example.localization.AppStrings
import com.example.ui.theme.*
import com.example.utils.AvatarImage
import com.example.utils.ImageUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

fun formatOrdinalPosition(position: Long): String {
    if (position <= 0L) return "-"
    val mod100 = (position % 100L).toInt()
    val suffix = if (mod100 in 11..13) {
        "th"
    } else {
        when ((position % 10L).toInt()) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
    return "$position$suffix"
}

/**
 * First-stage Result Summary Card shown before tapping Preview to open the full certificate report.
 */
@Composable
fun StudentResultSummaryCard(
    student: Student,
    exam: Examination,
    result: ExamResult,
    lang: String,
    onPreview: () -> Unit,
    onDownloadPdf: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val gradeAccent = when (result.grade) {
        "A+", "A" -> GreenAccent
        "B", "C" -> BlueSecondary
        "D" -> AmberDark
        else -> RedDanger
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPreview),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, NavyLight.copy(alpha = 0.24f)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0A2472), Color(0xFF1D4ED8))))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
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
                            size = 44.dp,
                            accentColor = AmberWarning
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = exam.name,
                                fontFamily = OutfitFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${student.fullName} • ${student.className} • ${student.studentId}",
                                color = Color(0xFFBAE6FD),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AmberWarning,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = formatOrdinalPosition(result.position),
                            fontFamily = OutfitFontFamily,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Brush.horizontalGradient(listOf(AmberWarning, GreenLight, BlueLight)))
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BlueSurface.copy(alpha = 0.75f))
                        .border(1.dp, BlueSecondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ReportSummaryPill(
                        label = if (lang == "so") "Wadarta" else "Total Marks",
                        value = "${result.totalObtained.toInt()}/${result.totalMax.toInt()}",
                        color = NavyPrimary
                    )
                    ReportSummaryPill(
                        label = if (lang == "so") "Celcelis" else "Average",
                        value = String.format(Locale.US, "%.1f%%", result.percentage),
                        color = BlueSecondary
                    )
                    ReportSummaryPill(
                        label = if (lang == "so") "Darajada" else "Grade",
                        value = result.grade,
                        color = gradeAccent
                    )
                    ReportSummaryPill(
                        label = if (lang == "so") "Kaalinta" else "Position",
                        value = formatOrdinalPosition(result.position),
                        color = VioletAccent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VibrantGradientButton(
                        text = AppStrings.get("preview_report", lang),
                        icon = Icons.Default.Visibility,
                        gradient = GradientRoyalBlue,
                        height = 42.dp,
                        fontSize = 12,
                        onClick = onPreview,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_report_button_${exam.examId}_${student.studentId}")
                    )

                    if (onDownloadPdf != null) {
                        VibrantTintedButton(
                            text = "PDF",
                            color = GreenAccent,
                            icon = Icons.Default.Download,
                            height = 42.dp,
                            fontSize = 12,
                            onClick = onDownloadPdf
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-width Certificate Preview Dialog triggered when tapping Preview on any student's result.
 */
@Composable
fun StudentReportPreviewDialog(
    student: Student,
    exam: Examination,
    result: ExamResult,
    school: SchoolConfig,
    lang: String,
    classTeacherName: String = "",
    onDownloadPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFF1F5FF),
            shadowElevation = 14.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(GradientCobaltDeep))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.get("preview_report", lang),
                            fontFamily = OutfitFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = onDownloadPdf,
                            shape = RoundedCornerShape(50),
                            color = GreenAccent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", color = Color.White, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp)
                ) {
                    BeautifulReportCardView(
                        school = school,
                        student = student,
                        exam = exam,
                        result = result,
                        lang = lang,
                        classTeacherName = classTeacherName
                    )
                }
            }
        }
    }
}

/**
 * Certificate-grade Exam Result Report matching the Royal Blue & Gold Wave academic design.
 */
@Composable
fun BeautifulReportCardView(
    school: SchoolConfig,
    student: Student,
    exam: Examination,
    result: ExamResult,
    lang: String,
    classTeacherName: String = "",
    modifier: Modifier = Modifier
) {
    val reportDateStr = remember {
        SimpleDateFormat("dd / MM / yyyy", Locale.US).format(Date())
    }
    val ordinalRank = formatOrdinalPosition(result.position)
    val resolvedTeacherName = classTeacherName.ifBlank {
        if (lang == "so") "Macallinka Fasalka" else "Class Teacher"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF0A2472)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, Color(0xFF1E40AF).copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. TOP CURVED ROYAL BLUE + GOLD WAVE HEADER & FLOATING TITLE BANNER
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .drawBehind {
                            val w = size.width
                            val h = size.height
                            // Gold sweeping wave underneath
                            val goldPath = Path().apply {
                                moveTo(0f, h * 0.72f)
                                cubicTo(
                                    w * 0.30f, h * 1.04f,
                                    w * 0.72f, h * 0.86f,
                                    w, h * 0.44f
                                )
                                lineTo(w, h * 0.78f)
                                cubicTo(
                                    w * 0.70f, h * 1.02f,
                                    w * 0.28f, h * 1.06f,
                                    0f, h * 0.90f
                                )
                                close()
                            }
                            drawPath(
                                path = goldPath,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFD97706), Color(0xFFFACC15), Color(0xFFF59E0B))
                                )
                            )

                            // Deep Royal Blue main curved banner
                            val navyPath = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(w, 0f)
                                lineTo(w, h * 0.46f)
                                cubicTo(
                                    w * 0.72f, h * 0.85f,
                                    w * 0.28f, h * 1.00f,
                                    0f, h * 0.76f
                                )
                                close()
                            }
                            drawPath(
                                path = navyPath,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF071B54), Color(0xFF0D2E8A), Color(0xFF1849C6))
                                )
                            )
                            drawCircle(
                                color = Color(0xFF38BDF8).copy(alpha = 0.14f),
                                radius = h * 0.75f,
                                center = Offset(w * 0.85f, h * 0.18f)
                            )
                        }
                        .padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 36.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gold-Ringed School Crest Logo
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .shadow(6.dp, CircleShape, ambientColor = AmberWarning)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFD97706)))
                                )
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.school_logo),
                                contentDescription = school.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = school.name,
                                fontFamily = OutfitFontFamily,
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = school.motto.ifBlank { "Learn • Excel • Succeed" },
                                    color = Color(0xFFE0F2FE),
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Floating Navy + Gold Bordered Pill Banner ("EXAM RESULT REPORT")
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF0A2472),
                    shadowElevation = 6.dp,
                    border = BorderStroke(
                        2.dp,
                        Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFFDE047), Color(0xFFD97706)))
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.get("exam_result_report", lang),
                            fontFamily = OutfitFontFamily,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.7.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 2. THREE-COLUMN STUDENT PHOTO + INFO TABLE + GOLD POSITION ROSETTE MEDAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Student Photo with Gold Border & Royal Blue Backdrop
                    StudentCertificatePhoto(
                        photoBase64 = student.photoUrl,
                        name = student.fullName
                    )

                    // Student Details Ice-Blue Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFF93C5FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CertificateInfoRow(
                                icon = Icons.Default.Person,
                                label = AppStrings.get("student_name_label", lang),
                                value = student.fullName
                            )
                            CertificateInfoRow(
                                icon = Icons.Default.Badge,
                                label = "ID",
                                value = student.studentId
                            )
                            CertificateInfoRow(
                                icon = Icons.Default.Groups,
                                label = AppStrings.get("class", lang),
                                value = student.className
                            )
                            CertificateInfoRow(
                                icon = Icons.Default.DateRange,
                                label = AppStrings.get("term_label", lang),
                                value = exam.name
                            )
                            CertificateInfoRow(
                                icon = Icons.Default.CalendarMonth,
                                label = AppStrings.get("academic_year", lang),
                                value = exam.academicYear
                            )
                        }
                    }

                    // Gold Scalloped Rosette Medal with Red Ribbons
                    PositionRosetteMedal(
                        ordinalPosition = ordinalRank,
                        label = if (lang == "so") "Kaalinta" else "Position"
                    )
                }

                // 3. SUBJECT MARKS TABLE (No. | Subject | Marks [95 / 100] | Grade Pill)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.3.dp, Color(0xFF1E40AF), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0A2472), Color(0xFF1D4ED8))))
                            .padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("no_col", lang),
                            color = Color.White,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.45f)
                        )
                        Text(
                            text = if (lang == "so") "Maaddada" else "Subject",
                            color = Color.White,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1.9f)
                        )
                        Text(
                            text = if (lang == "so") "Dhibcaha" else "Marks",
                            color = Color.White,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.15f)
                        )
                        Text(
                            text = if (lang == "so") "Darajo" else "Grade",
                            color = Color(0xFFFDE047),
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.75f)
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
                            val (pillBg, pillFg) = when (subGrade) {
                                "A+", "A" -> Color(0xFFBBF7D0) to Color(0xFF14532D)
                                "B", "C" -> Color(0xFFDBEAFE) to Color(0xFF1E3A8A)
                                "D" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
                                else -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                            }
                            val scoreText = if (obtM % 1.0 == 0.0) "${obtM.toInt()}" else String.format(Locale.US, "%.1f", obtM)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (idx % 2 == 0) Color.White else Color(0xFFF3F7FF))
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.45f)
                                )
                                Text(
                                    text = subName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1.9f)
                                )
                                Row(
                                    modifier = Modifier.weight(1.15f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFF93C5FD))
                                    ) {
                                        Text(
                                            text = scoreText,
                                            fontFamily = MonoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NavyPrimary,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = " / ${maxM.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier.weight(0.75f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = pillBg,
                                        border = BorderStroke(1.dp, pillFg.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            text = subGrade,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = pillFg,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            if (idx < rows.lastIndex) {
                                HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.6.dp)
                            }
                        }
                    }
                }

                // 4. THREE COLORFUL METRIC CARDS (Total Marks | Average | Position)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    ReportBannerMetricCard(
                        icon = Icons.Default.EmojiEvents,
                        iconTint = Color(0xFFFDE047),
                        label = if (lang == "so") "Wadarta" else "Total Marks",
                        value = "${result.totalObtained.toInt()} / ${result.totalMax.toInt()}",
                        gradient = listOf(Color(0xFF0E3399), Color(0xFF2563EB)),
                        modifier = Modifier.weight(1f)
                    )
                    ReportBannerMetricCard(
                        icon = Icons.Default.TrendingUp,
                        iconTint = Color(0xFFFDE047),
                        label = if (lang == "so") "Celcelis" else "Average",
                        value = String.format(Locale.US, "%.2f%%", result.percentage),
                        gradient = listOf(Color(0xFF047857), Color(0xFF16A34A)),
                        modifier = Modifier.weight(1f)
                    )
                    ReportBannerMetricCard(
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFFFDE047),
                        label = if (lang == "so") "Kaalinta" else "Position",
                        value = ordinalRank,
                        gradient = listOf(Color(0xFF5B21B6), Color(0xFF7C3AED)),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 5. TEACHER'S REPLY & SIGNATURE CARD
                val remarks = result.teacherRemarks.ifBlank {
                    if (result.percentage >= 85) {
                        if (lang == "so") "Waxqabad aad u wanaagsan! Sii wad dadaalkaaga oo guul weyn gaar."
                        else "Excellent performance! Keep up the hard work and strive for greater success."
                    } else if (result.percentage >= 65) {
                        if (lang == "so") "Waxqabad wanaagsan! Sii wad dadaalkaaga joogtada ah."
                        else "Good performance! Continue working hard for even better results."
                    } else {
                        if (lang == "so") "Dadaal dheeri ah ayaa loo baahan yahay. Waad gaari kartaa guul."
                        else "Needs focused revision and extra practice. Keep striving!"
                    }
                }
                val highlightTag = if (result.percentage >= 65) {
                    AppStrings.get("well_done", lang)
                } else {
                    AppStrings.get("keep_striving", lang)
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0F6FF),
                        border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 22.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Remark & "Well Done!" gold swoosh
                            Column(
                                modifier = Modifier.weight(1.45f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = remarks,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = NavyPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = highlightTag,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF0A2472)
                                )
                                Canvas(
                                    modifier = Modifier
                                        .width(86.dp)
                                        .height(7.dp)
                                ) {
                                    val swoosh = Path().apply {
                                        moveTo(0f, size.height * 0.8f)
                                        quadraticTo(size.width * 0.5f, 0f, size.width, size.height * 0.45f)
                                    }
                                    drawPath(
                                        path = swoosh,
                                        color = Color(0xFFF59E0B),
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .width(1.dp)
                                    .height(64.dp)
                                    .background(Color(0xFF93C5FD))
                            )

                            // Right: Teacher Cursive Signature + Name + Title
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(26.dp)
                                ) {
                                    val sig = Path().apply {
                                        moveTo(size.width * 0.08f, size.height * 0.78f)
                                        cubicTo(
                                            size.width * 0.25f, size.height * 0.08f,
                                            size.width * 0.38f, size.height * 0.05f,
                                            size.width * 0.34f, size.height * 0.75f
                                        )
                                        cubicTo(
                                            size.width * 0.44f, size.height * 0.42f,
                                            size.width * 0.56f, size.height * 0.25f,
                                            size.width * 0.58f, size.height * 0.72f
                                        )
                                        cubicTo(
                                            size.width * 0.68f, size.height * 0.35f,
                                            size.width * 0.78f, size.height * 0.48f,
                                            size.width * 0.94f, size.height * 0.58f
                                        )
                                    }
                                    drawPath(
                                        path = sig,
                                        color = Color(0xFF0D2E8A),
                                        style = Stroke(
                                            width = 2.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                                HorizontalDivider(
                                    color = Color(0xFF64748B),
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Text(
                                    text = resolvedTeacherName,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = AppStrings.get("class_teacher", lang),
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Overlapping "Teacher's Reply" Navy Pill Tab
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF0A2472),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("teacher_reply", lang),
                                fontFamily = OutfitFontFamily,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 6. BOTTOM CURVED GOLD & NAVY WAVE FOOTER + REPORT DATE + OFFICIAL STAMP
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .drawBehind {
                            val w = size.width
                            val h = size.height
                            val goldWave = Path().apply {
                                moveTo(0f, h * 0.14f)
                                cubicTo(
                                    w * 0.34f, h * 0.38f,
                                    w * 0.72f, h * 0.08f,
                                    w, 0f
                                )
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(
                                path = goldWave,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFD97706), Color(0xFFFACC15), Color(0xFFF59E0B))
                                )
                            )

                            val navyWave = Path().apply {
                                moveTo(0f, h * 0.34f)
                                cubicTo(
                                    w * 0.34f, h * 0.56f,
                                    w * 0.72f, h * 0.25f,
                                    w, h * 0.14f
                                )
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(
                                path = navyWave,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF0D2E8A), Color(0xFF071B54))
                                )
                            )
                        }
                        .padding(start = 14.dp, end = 14.dp, top = 28.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.get("build_brighter_future", lang),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OfficialSchoolStamp(schoolName = school.name)
                    }
                }

                // Floating Report Date Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF0A2472),
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${AppStrings.get("report_date", lang)}: $reportDateStr",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCertificatePhoto(
    photoBase64: String,
    name: String
) {
    val imageBitmap = remember(photoBase64) { ImageUtils.base64ToImageBitmap(photoBase64) }
    Box(
        modifier = Modifier
            .width(74.dp)
            .height(94.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF0EA5E9))))
            .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = name,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(34.dp)
                )
                Text(
                    text = name.trim().take(2).uppercase(Locale.ROOT).ifBlank { "ST" },
                    fontFamily = OutfitFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CertificateInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF1E3A8A),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label:",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(62.dp)
        )
        Surface(
            shape = RoundedCornerShape(5.dp),
            color = Color.White,
            border = BorderStroke(0.8.dp, Color(0xFF93C5FD)),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = value,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun PositionRosetteMedal(
    ordinalPosition: String,
    label: String
) {
    Box(
        modifier = Modifier
            .width(68.dp)
            .height(92.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.width / 2f + 2.dp.toPx()
            val outerR = size.width * 0.46f

            // Dual Red Ribbons hanging beneath seal
            val leftRibbon = Path().apply {
                moveTo(cx - 12.dp.toPx(), cy + outerR * 0.45f)
                lineTo(cx - 20.dp.toPx(), size.height * 0.96f)
                lineTo(cx - 10.dp.toPx(), size.height * 0.85f)
                lineTo(cx - 2.dp.toPx(), size.height * 0.96f)
                lineTo(cx, cy + outerR * 0.55f)
                close()
            }
            val rightRibbon = Path().apply {
                moveTo(cx + 12.dp.toPx(), cy + outerR * 0.45f)
                lineTo(cx + 20.dp.toPx(), size.height * 0.96f)
                lineTo(cx + 10.dp.toPx(), size.height * 0.85f)
                lineTo(cx + 2.dp.toPx(), size.height * 0.96f)
                lineTo(cx, cy + outerR * 0.55f)
                close()
            }
            drawPath(leftRibbon, Color(0xFFDC2626))
            drawPath(rightRibbon, Color(0xFFB91C1C))

            // 16-point Scalloped Golden Rosette
            val points = 32
            val rosette = Path()
            for (i in 0 until points) {
                val angle = (2.0 * PI * i) / points
                val r = if (i % 2 == 0) outerR else outerR * 0.84f
                val px = cx + (r * cos(angle)).toFloat()
                val py = cy + (r * sin(angle)).toFloat()
                if (i == 0) rosette.moveTo(px, py) else rosette.lineTo(px, py)
            }
            rosette.close()
            drawPath(
                path = rosette,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFB45309)),
                    center = Offset(cx, cy),
                    radius = outerR
                )
            )

            // Inner Dark Royal Navy Circle
            drawCircle(
                color = Color(0xFF0A2472),
                radius = outerR * 0.72f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFFFDE047),
                radius = outerR * 0.66f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        Column(
            modifier = Modifier.padding(top = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color(0xFFE0F2FE),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = ordinalPosition,
                fontFamily = OutfitFontFamily,
                color = Color(0xFFFDE047),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ReportBannerMetricCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = gradient.first()),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradient))
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    fontFamily = OutfitFontFamily,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OfficialSchoolStamp(schoolName: String) {
    val shortTitle = remember(schoolName) {
        schoolName.trim().split(" ").take(2).joinToString(" ").uppercase(Locale.ROOT)
    }
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            drawCircle(
                color = Color(0xFF1D4ED8),
                radius = r - 1.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF1D4ED8),
                radius = r - 9.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = shortTitle,
                color = Color(0xFF1D4ED8),
                fontSize = 5.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                tint = Color(0xFF1D4ED8),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "★ OFFICIAL ★",
                color = Color(0xFF1D4ED8),
                fontSize = 5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ReportSummaryPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        Text(value, fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
