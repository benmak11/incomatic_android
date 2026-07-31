package com.makusha.incomatic.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Every token from inc-theme.css, ported verbatim (same file backing the
 * iOS and web work) so the palette can't drift between platforms.
 */
data class IncColors(
    val bg: Color,
    val surface: Color,
    val surfaceWarm: Color,
    val text: Color,
    val textDim: Color,
    val textMute: Color,
    val sage: Color,
    val sageDeep: Color,
    val sageSoft: Color,
    val sageBg: Color,
    val blush: Color,
    val blushBg: Color,
    val gold: Color,
    val red: Color,
    val redBg: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val cardBorder: Color,
    val track: Color,
    val disabled: Color,
    val donutTrack: Color,
    val barBg: Color,
    val btnSolid: Color,
    val btnSolidText: Color,
    val toastBg: Color,
    val toastText: Color,
)

val IncLightColors = IncColors(
    bg = Color(0xFFF5F1EA),
    surface = Color(0xFFFFFFFF),
    surfaceWarm = Color(0xFFFBF6EE),
    text = Color(0xFF1F2A2A),
    textDim = Color(0xFF5A6868),
    textMute = Color(0xFF94A09E),
    sage = Color(0xFF5F8C7C),
    sageDeep = Color(0xFF3F6B5C),
    sageSoft = Color(0xFFD7E4DE),
    sageBg = Color(0xFFEEF5F1),
    blush = Color(0xFFE89B7D),
    blushBg = Color(0xFFFBE9DE),
    gold = Color(0xFFE4C77A),
    red = Color(0xFFD93A3A),
    redBg = Color(0xFFFBEAEA),
    hairline = Color(0x141F2A2A),
    hairlineStrong = Color(0x241F2A2A),
    cardBorder = Color(0x001F2A2A),
    track = Color(0xFFE2E5E3),
    disabled = Color(0xFFC4CCCA),
    donutTrack = Color(0x0D000000),
    barBg = Color(0xE0F5F1EA),
    btnSolid = Color(0xFF1F2A2A),
    btnSolidText = Color(0xFFFFFFFF),
    toastBg = Color(0xFF1F2A2A),
    toastText = Color(0xFFFFFFFF),
)

val IncDarkColors = IncColors(
    bg = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    surfaceWarm = Color(0xFF242426),
    text = Color(0xFFFFFFFF),
    textDim = Color(0x99EBEBF5),
    textMute = Color(0x6BEBEBF5),
    sage = Color(0xFF7FB29F),
    sageDeep = Color(0xFFA6D8C5),
    sageSoft = Color(0x667FB29F),
    sageBg = Color(0x267FB29F),
    blush = Color(0xFFEBA890),
    blushBg = Color(0x26E89B7D),
    gold = Color(0xFFE8CF86),
    red = Color(0xFFFF6961),
    redBg = Color(0x26FF6961),
    hairline = Color(0x17FFFFFF),
    hairlineStrong = Color(0x2EFFFFFF),
    cardBorder = Color(0x12FFFFFF),
    track = Color(0xFF39393D),
    disabled = Color(0xFF2C2C2E),
    donutTrack = Color(0x14FFFFFF),
    barBg = Color(0xCC121214),
    btnSolid = Color(0xFF2C2C2E),
    btnSolidText = Color(0xFFFFFFFF),
    toastBg = Color(0xFF2C2C2E),
    toastText = Color(0xFFFFFFFF),
)

val LocalIncColors = staticCompositionLocalOf { IncLightColors }
