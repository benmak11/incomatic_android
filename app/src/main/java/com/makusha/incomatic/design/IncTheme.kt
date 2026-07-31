package com.makusha.incomatic.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Wraps MaterialTheme with the Incomatic token set. Material You / dynamic
 * color is deliberately never used — the sage system is the brand, not a
 * per-device wallpaper extraction. M3 supplies ripple, ModalBottomSheet
 * mechanics and accessibility semantics only; every visible color comes
 * from [IncColors].
 */
@Composable
fun IncomaticTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val incColors = if (darkTheme) IncDarkColors else IncLightColors
    val materialScheme = if (darkTheme) {
        darkColorScheme(
            primary = incColors.sage,
            onPrimary = incColors.btnSolidText,
            background = incColors.bg,
            onBackground = incColors.text,
            surface = incColors.surface,
            onSurface = incColors.text,
            error = incColors.red,
        )
    } else {
        lightColorScheme(
            primary = incColors.sage,
            onPrimary = incColors.btnSolidText,
            background = incColors.bg,
            onBackground = incColors.text,
            surface = incColors.surface,
            onSurface = incColors.text,
            error = incColors.red,
        )
    }

    CompositionLocalProvider(LocalIncColors provides incColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = incMaterialTypography(),
        ) {
            IncRippleScope(content = content)
        }
    }
}
