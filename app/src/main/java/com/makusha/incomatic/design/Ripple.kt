package com.makusha.incomatic.design

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
@ReadOnlyComposable
fun incColors(): IncColors = LocalIncColors.current

/**
 * Sage-tinted ripple, replacing iOS's opacity-press. `IncSubtleRippleScope`
 * lowers the alpha further for large tappable surfaces (cards) where a full
 * ripple would read as noise, matching the design doc's `subtle` variant.
 */
@Composable
fun IncRippleScope(content: @Composable () -> Unit) {
    val sage = incColors().sage
    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            color = sage,
            rippleAlpha = RippleAlpha(
                draggedAlpha = 0.10f,
                focusedAlpha = 0.10f,
                hoveredAlpha = 0.06f,
                pressedAlpha = 0.18f,
            ),
        ),
        content = content,
    )
}

@Composable
fun IncSubtleRippleScope(content: @Composable () -> Unit) {
    val sage = incColors().sage
    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            color = sage,
            rippleAlpha = RippleAlpha(
                draggedAlpha = 0.06f,
                focusedAlpha = 0.06f,
                hoveredAlpha = 0.03f,
                pressedAlpha = 0.10f,
            ),
        ),
        content = content,
    )
}
