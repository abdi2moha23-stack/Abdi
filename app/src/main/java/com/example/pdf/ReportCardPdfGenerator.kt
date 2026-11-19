package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.models.ExamResult
import com.example.data.models.Examination
import com.example.data.models.SchoolConfig
import com.example.data.models.Student
import com.example.data.repository.SchoolRepository
import com.example.localization.AppStrings
import com.example.ui.screens.formatOrdinalPosition
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ReportCardPdfGenerator {

    fun generateReportCard(
        context: Context,
        school: SchoolConfig,
        student: Student,
        exam: Examination,
        result: ExamResult,
        language: String = "so",
        classTeacherName: String = ""
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        val deepNavy = Color.rgb(10, 36, 114)
        val royalBlue = Color.rgb(24, 73, 198)
        val goldColor = Color.rgb(245, 158, 11)
        val goldBright = Color.rgb(250, 204, 21)
        val iceBlue = Color.rgb(239, 246, 255)
        val iceBorder = Color.rgb(147, 197, 253)
        val emerald = Color.rgb(4, 120, 87)
        val emeraldLight = Color.rgb(187, 247, 208)
        val emeraldDark = Color.rgb(20, 83, 45)
        val purpleColor = Color.rgb(91, 33, 182)
        val darkText = Color.rgb(15, 23, 42)
        val subtleText = Color.rgb(100, 116, 139)

        // 1. TOP CURVED GOLD + ROYAL BLUE HEADER
        val goldWave = Path().apply {
            moveTo(0f, 88f)
            cubicTo(180f, 128f, 430f, 106f, 595f, 58f)
            lineTo(595f, 96f)
            cubicTo(425f, 125f, 175f, 134f, 0f, 108f)
            close()
        }
        paint.color = goldColor
        canvas.drawPath(goldWave, paint)

        val navyHeader = Path().apply {
            moveTo(0f, 0f)
            lineTo(595f, 0f)
            lineTo(595f, 60f)
            cubicTo(430f, 104f, 175f, 122f, 0f, 92f)
            close()
        }
        paint.color = deepNavy
        canvas.drawPath(navyHeader, paint)

        // School Logo
        try {
            val logoBmp = BitmapFactory.decodeResource(context.resources, R.drawable.school_logo)
            if (logoBmp != null) {
                paint.color = goldBright
                canvas.drawCircle(72f, 48f, 32f, paint)
                val scaledLogo = Bitmap.createScaledBitmap(logoBmp, 56, 56, true)
                val clipPath = Path().apply { addCircle(72f, 48f, 28f, Path.Direction.CW) }
                canvas.save()
                canvas.clipPath(clipPath)
                canvas.drawBitmap(scaledLogo, 44f, 20f, paint)
                canvas.restore()
            }
        } catch (_: Exception) {
        }

        // School Name & Motto
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 23f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(school.name, 315f, 42f, paint)

        paint.textSize = 11f
        paint.color = Color.rgb(224, 242, 254)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(school.motto.ifBlank { "Learn • Excel • Succeed" }, 315f, 62f, paint)

        // Floating Pill Title ("EXAM RESULT REPORT")
        val titleRect = RectF(145f, 88f, 450f, 122f)
        paint.color = deepNavy
        canvas.drawRoundRect(titleRect, 17f, 17f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = goldColor
        paint.strokeWidth = 2.2f
        canvas.drawRoundRect(titleRect, 17f, 17f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.WHITE
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(AppStrings.get("exam_result_report", language), 297.5f, 110f, paint)

        // 2. THREE-COLUMN STUDENT PROFILE + INFO TABLE + GOLD ROSETTE MEDAL
        val topSectionY = 138f

        // Left: Student Photo Box
        val photoRect = RectF(36f, topSectionY, 122f, topSectionY + 112f)
        paint.color = royalBlue
        canvas.drawRoundRect(photoRect, 12f, 12f, paint)
        val studentBmp = decodeBase64Bitmap(student.photoUrl)
        if (studentBmp != null) {
            val scaledSt = Bitmap.createScaledBitmap(studentBmp, 86, 112, true)
            val clipRect = Path().apply { addRoundRect(photoRect, 12f, 12f, Path.Direction.CW) }
            canvas.save()
            canvas.clipPath(clipRect)
            canvas.drawBitmap(scaledSt, 36f, topSectionY, paint)
            canvas.restore()
        } else {
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(student.fullName.trim().take(2).uppercase(Locale.ROOT).ifBlank { "ST" }, 79f, topSectionY + 65f, paint)
        }
        paint.style = Paint.Style.STROKE
        paint.color = goldColor
        paint.strokeWidth = 2.5f
        canvas.drawRoundRect(photoRect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        // Middle: Info Table Box
        val infoRect = RectF(134f, topSectionY, 474f, topSectionY + 112f)
        paint.color = iceBlue
        canvas.drawRoundRect(infoRect, 12f, 12f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = iceBorder
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(infoRect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        val infoRows = listOf(
            AppStrings.get("student_name_label", language) to student.fullName,
            "Student ID" to student.studentId,
            AppStrings.get("class", language) to student.className,
            AppStrings.get("term_label", language) to exam.name,
            AppStrings.get("academic_year", language) to exam.academicYear
        )
        infoRows.forEachIndexed { idx, (lbl, valStr) ->
            val ry = topSectionY + 18f + idx * 20f
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 9.5f
            paint.color = darkText
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("$lbl:", 146f, ry, paint)

            val valBox = RectF(240f, ry - 12f, 464f, ry + 5f)
            paint.color = Color.WHITE
            canvas.drawRoundRect(valBox, 5f, 5f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = iceBorder
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(valBox, 5f, 5f, paint)
            paint.style = Paint.Style.FILL

            paint.color = deepNavy
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(valStr.take(34), 248f, ry - 0.5f, paint)
        }

        // Right: Gold Rosette Medal + Red Ribbons
        val cx = 524f
        val cy = topSectionY + 42f
        val outerR = 36f

        val leftRibbon = Path().apply {
            moveTo(cx - 12f, cy + 16f)
            lineTo(cx - 22f, topSectionY + 112f)
            lineTo(cx - 10f, topSectionY + 98f)
            lineTo(cx, topSectionY + 112f)
            lineTo(cx, cy + 20f)
            close()
        }
        val rightRibbon = Path().apply {
            moveTo(cx + 12f, cy + 16f)
            lineTo(cx + 22f, topSectionY + 112f)
            lineTo(cx + 10f, topSectionY + 98f)
            lineTo(cx, topSectionY + 112f)
            lineTo(cx, cy + 20f)
            close()
        }
        paint.color = Color.rgb(220, 38, 38)
        canvas.drawPath(leftRibbon, paint)
        paint.color = Color.rgb(185, 28, 28)
        canvas.drawPath(rightRibbon, paint)

        val rosette = Path()
        for (i in 0 until 32) {
            val angle = (2.0 * PI * i) / 32.0
            val r = if (i % 2 == 0) outerR else outerR * 0.84f
            val px = cx + (r * cos(angle)).toFloat()
            val py = cy + (r * sin(angle)).toFloat()
            if (i == 0) rosette.moveTo(px, py) else rosette.lineTo(px, py)
        }
        rosette.close()
        paint.color = goldColor
        canvas.drawPath(rosette, paint)

        paint.color = deepNavy
        canvas.drawCircle(cx, cy, outerR * 0.72f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = goldBright
        paint.strokeWidth = 1.5f
        canvas.drawCircle(cx, cy, outerR * 0.66f, paint)
        paint.style = Paint.Style.FILL

        val ordinalPos = formatOrdinalPosition(result.position)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.rgb(224, 242, 254)
        paint.textSize = 8.5f
        canvas.drawText(if (language == "so") "Kaalinta" else "Position", cx, cy - 8f, paint)
        paint.color = goldBright
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(ordinalPos, cx, cy + 12f, paint)

        // 3. SUBJECT MARKS TABLE (No. | Subject | Marks | Grade)
        val tableTop = 268f
        val rowsData = if (exam.subjects.isNotEmpty()) {
            exam.subjects.map { Triple(it.name, it.maxMarks, result.subjectMarks[it.name] ?: 0.0) }
        } else {
            result.subjectMarks.entries.map { Triple(it.key, 100.0, it.value) }
        }
        val rowHeight = 26f

        paint.color = deepNavy
        canvas.drawRoundRect(RectF(36f, tableTop, 559f, tableTop + 28f), 8f, 8f, paint)
        paint.color = Color.WHITE
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(AppStrings.get("no_col", language), 60f, tableTop + 18f, paint)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(if (language == "so") "Maaddada (Subject)" else "Subject", 94f, tableTop + 18f, paint)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(if (language == "so") "Dhibcaha (Marks)" else "Marks", 380f, tableTop + 18f, paint)
        paint.color = goldBright
        canvas.drawText(if (language == "so") "Darajo (Grade)" else "Grade", 506f, tableTop + 18f, paint)

        var currentY = tableTop + 28f
        for (i in rowsData.indices) {
            val (subName, maxM, obtM) = rowsData[i]
            val pct = if (maxM > 0) (obtM / maxM) * 100.0 else 0.0
            val subGrade = SchoolRepository.calculateGrade(pct, school.gradingScale)

            if (i % 2 == 1) {
                paint.color = Color.rgb(243, 247, 255)
                canvas.drawRect(36f, currentY, 559f, currentY + rowHeight, paint)
            }

            paint.color = deepNavy
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${i + 1}", 60f, currentY + 17f, paint)

            paint.color = darkText
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(subName, 94f, currentY + 17f, paint)

            // Marks pill box
            val markBox = RectF(332f, currentY + 4f, 376f, currentY + 21f)
            paint.color = Color.WHITE
            canvas.drawRoundRect(markBox, 5f, 5f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = iceBorder
            paint.strokeWidth = 0.9f
            canvas.drawRoundRect(markBox, 5f, 5f, paint)
            paint.style = Paint.Style.FILL

            val scoreTxt = if (obtM % 1.0 == 0.0) "${obtM.toInt()}" else String.format(Locale.US, "%.1f", obtM)
            paint.color = deepNavy
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(scoreTxt, 354f, currentY + 16f, paint)
            paint.color = subtleText
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("/ ${maxM.toInt()}", 382f, currentY + 16f, paint)

            // Grade Pill
            val gradeRect = RectF(476f, currentY + 4f, 536f, currentY + 21f)
            paint.color = when (subGrade) {
                "A+", "A" -> emeraldLight
                "B", "C" -> Color.rgb(219, 234, 254)
                "D" -> Color.rgb(254, 243, 199)
                else -> Color.rgb(254, 226, 226)
            }
            canvas.drawRoundRect(gradeRect, 9f, 9f, paint)
            paint.color = when (subGrade) {
                "A+", "A" -> emeraldDark
                "B", "C" -> deepNavy
                "D" -> Color.rgb(146, 64, 14)
                else -> Color.rgb(153, 27, 27)
            }
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(subGrade, 506f, currentY + 16f, paint)

            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 0.6f
            canvas.drawLine(36f, currentY + rowHeight, 559f, currentY + rowHeight, paint)
            currentY += rowHeight
        }

        paint.style = Paint.Style.STROKE
        paint.color = royalBlue
        paint.strokeWidth = 1.4f
        canvas.drawRoundRect(RectF(36f, tableTop, 559f, currentY), 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // 4. THREE COLORFUL METRIC CARDS
        val cardsY = (currentY + 18f).coerceAtMost(600f)
        val cardW = 166f
        val gap = 12f
        val metrics = listOf(
            Triple(if (language == "so") "Wadarta" else "Total Marks", "${result.totalObtained.toInt()} / ${result.totalMax.toInt()}", royalBlue),
            Triple(if (language == "so") "Celcelis" else "Average", String.format(Locale.US, "%.2f%%", result.percentage), emerald),
            Triple(if (language == "so") "Kaalinta" else "Position", ordinalPos, purpleColor)
        )
        metrics.forEachIndexed { idx, (lbl, valTxt, bgColor) ->
            val x1 = 36f + idx * (cardW + gap)
            val rect = RectF(x1, cardsY, x1 + cardW, cardsY + 54f)
            paint.color = bgColor
            canvas.drawRoundRect(rect, 10f, 10f, paint)
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(lbl, x1 + cardW / 2f, cardsY + 20f, paint)
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(valTxt, x1 + cardW / 2f, cardsY + 42f, paint)
        }

        // 5. TEACHER'S REPLY & SIGNATURE CARD
        val replyY = cardsY + 72f
        val replyBox = RectF(36f, replyY + 10f, 559f, replyY + 94f)
        paint.color = iceBlue
        canvas.drawRoundRect(replyBox, 12f, 12f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = iceBorder
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(replyBox, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        val tabRect = RectF(44f, replyY - 4f, 185f, replyY + 20f)
        paint.color = deepNavy
        canvas.drawRoundRect(tabRect, 12f, 12f, paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(AppStrings.get("teacher_reply", language), 114.5f, replyY + 12f, paint)

        val remarksText = result.teacherRemarks.ifBlank {
            if (language == "so") "Waxqabad aad u wanaagsan! Sii wad dadaalkaaga." else "Excellent performance! Keep up the hard work."
        }
        paint.color = deepNavy
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(remarksText.take(54), 210f, replyY + 46f, paint)

        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        canvas.drawText(
            if (result.percentage >= 65) AppStrings.get("well_done", language) else AppStrings.get("keep_striving", language),
            210f,
            replyY + 72f,
            paint
        )

        paint.color = iceBorder
        paint.strokeWidth = 1f
        canvas.drawLine(390f, replyY + 22f, 390f, replyY + 84f, paint)

        val teacherDisplay = classTeacherName.ifBlank { AppStrings.get("class_teacher", language) }
        paint.color = deepNavy
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(teacherDisplay, 474f, replyY + 66f, paint)
        paint.color = subtleText
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(AppStrings.get("class_teacher", language), 474f, replyY + 80f, paint)

        // 6. BOTTOM WAVE + REPORT DATE + OFFICIAL STAMP
        val bottomGold = Path().apply {
            moveTo(0f, 766f)
            cubicTo(190f, 796f, 410f, 756f, 595f, 740f)
            lineTo(595f, 842f)
            lineTo(0f, 842f)
            close()
        }
        paint.color = goldColor
        canvas.drawPath(bottomGold, paint)

        val bottomNavy = Path().apply {
            moveTo(0f, 782f)
            cubicTo(195f, 812f, 415f, 772f, 595f, 758f)
            lineTo(595f, 842f)
            lineTo(0f, 842f)
            close()
        }
        paint.color = deepNavy
        canvas.drawPath(bottomNavy, paint)

        val formattedDate = SimpleDateFormat("dd / MM / yyyy", Locale.US).format(Date())
        val datePill = RectF(200f, 746f, 395f, 772f)
        paint.color = deepNavy
        canvas.drawRoundRect(datePill, 13f, 13f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = goldColor
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(datePill, 13f, 13f, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${AppStrings.get("report_date", language)}: $formattedDate", 297.5f, 763f, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(AppStrings.get("build_brighter_future", language), 40f, 822f, paint)

        // Circular Official Stamp Seal on bottom right
        paint.color = Color.WHITE
        canvas.drawCircle(526f, 792f, 36f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = royalBlue
        paint.strokeWidth = 2.2f
        canvas.drawCircle(526f, 792f, 33f, paint)
        paint.strokeWidth = 1f
        canvas.drawCircle(526f, 792f, 24f, paint)
        paint.style = Paint.Style.FILL
        paint.color = royalBlue
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(school.name.uppercase(Locale.ROOT).take(14), 526f, 788f, paint)
        canvas.drawText("★ OFFICIAL ★", 526f, 801f, paint)

        pdfDocument.finishPage(page)

        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "ReportCard_${student.studentId}_${exam.name.replace(" ", "_")}.pdf"
        val outputFile = File(reportsDir, fileName)

        return try {
            FileOutputStream(outputFile).use { fos -> pdfDocument.writeTo(fos) }
            pdfDocument.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun decodeBase64Bitmap(base64: String): Bitmap? {
        if (base64.isBlank()) return null
        return try {
            val cleaned = base64.substringAfter("base64,", base64)
            val bytes = Base64.decode(cleaned, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    fun shareReportCardPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Report Card"))
    }
}
