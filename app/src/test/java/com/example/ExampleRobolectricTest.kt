package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.localization.AppStrings
import com.example.ui.screens.formatOrdinalPosition
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Iftin School", appName)
  }

  @Test
  fun `ordinal rank formatting works for 1st 2nd 3rd 4th 11th 21st`() {
    assertEquals("-", formatOrdinalPosition(0L))
    assertEquals("1st", formatOrdinalPosition(1L))
    assertEquals("2nd", formatOrdinalPosition(2L))
    assertEquals("3rd", formatOrdinalPosition(3L))
    assertEquals("4th", formatOrdinalPosition(4L))
    assertEquals("11th", formatOrdinalPosition(11L))
    assertEquals("12th", formatOrdinalPosition(12L))
    assertEquals("13th", formatOrdinalPosition(13L))
    assertEquals("21st", formatOrdinalPosition(21L))
  }

  @Test
  fun `report card bilingual strings resolve`() {
    assertEquals("Fiiri Report-ka", AppStrings.get("preview_report", "so"))
    assertEquals("Preview Report", AppStrings.get("preview_report", "en"))
    assertEquals("EXAM RESULT REPORT", AppStrings.get("exam_result_report", "en"))
  }
}
