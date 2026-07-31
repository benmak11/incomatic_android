package com.makusha.incomatic.design

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException

/**
 * Shared predictive-back preview: a subtle scale/alpha dip tracking the
 * in-progress swipe gesture, reset if the gesture is cancelled. One
 * implementation reused by every back-handled screen (shell tab switch,
 * onboarding step-back, the grants dialog's step-back) instead of each
 * wiring PredictiveBackHandler + graphicsLayer itself.
 */
@Composable
fun rememberPredictiveBackScale(enabled: Boolean = true, onBack: () -> Unit): Modifier {
    var progress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = enabled) { events ->
        try {
            events.collect { event -> progress = event.progress }
            onBack()
        } catch (e: CancellationException) {
            // Gesture cancelled — fall through to reset progress below.
        } finally {
            progress = 0f
        }
    }

    return Modifier.graphicsLayer {
        val scale = 1f - progress * 0.08f
        scaleX = scale
        scaleY = scale
        alpha = 1f - progress * 0.15f
    }
}
