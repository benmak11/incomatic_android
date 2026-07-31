package com.makusha.incomatic.design

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Roboto is the system default on every Android device — no explicit
// FontFamily needed, FontFamily.Default already resolves to it.
val IncSansFamily = FontFamily.Default

// Source Serif 4 Medium is meant to stand in for New York on the 34sp page
// titles and the italic "i" wordmark glyph (per the design doc). Wiring the
// real font needs either a bundled res/font/*.ttf (no binary font asset was
// fetchable in this session) or Compose's Downloadable Fonts via the Google
// Fonts provider, which requires a specific certificate array that isn't
// safe to hand-type from memory — a wrong value fails silently at runtime.
// Using the platform's generic serif (Noto Serif on most devices) as an
// honest placeholder until one of those is wired up properly.
val IncSerifFamily = FontFamily.Serif

// Type scale — dp/sp values mirror the design doc's table (page title 34sp,
// card money 19-22sp bold, body 14sp, secondary 12.5-13sp, overline
// 10-10.5sp bold uppercase +0.6 tracking).
object IncType {
    val pageTitle = TextStyle(fontFamily = IncSerifFamily, fontWeight = FontWeight.Medium, fontSize = 34.sp, letterSpacing = (-1).sp)
    val sheetTitle = TextStyle(fontFamily = IncSerifFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, letterSpacing = (-0.6).sp)
    val heroMoney = TextStyle(fontFamily = IncSerifFamily, fontWeight = FontWeight.Medium, fontSize = 44.sp, letterSpacing = (-1.6).sp)
    val cardMoney = TextStyle(fontFamily = IncSansFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = (-0.4).sp)
    val title = TextStyle(fontFamily = IncSansFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    val body = TextStyle(fontFamily = IncSansFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp)
    val secondary = TextStyle(fontFamily = IncSansFamily, fontWeight = FontWeight.Normal, fontSize = 12.5.sp)
    val overline = TextStyle(fontFamily = IncSansFamily, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, letterSpacing = 0.6.sp)
}

@Composable
fun incMaterialTypography(): Typography = Typography(
    headlineMedium = IncType.pageTitle,
    titleLarge = IncType.sheetTitle,
    titleMedium = IncType.title,
    bodyLarge = IncType.body,
    bodySmall = IncType.secondary,
    labelSmall = IncType.overline,
)
