package com.makusha.incomatic.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncButtonVariant
import com.makusha.incomatic.design.incColors

/**
 * Continue/Calculate button row for the Calculator tab's bottom dock. The
 * live-projection figure that used to live in this composable's own ribbon
 * now lives in the sticky [com.makusha.incomatic.nav.AppSectionHeader]
 * instead (see CalculatorBottomDock.kt).
 */
@Composable
fun AndStickyCTA(
    section: CalculatorSection,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = incColors()
    val sections = CalculatorSection.entries
    val idx = sections.indexOf(section)
    val isLast = idx == sections.lastIndex
    val nextLabel = sections.getOrNull(idx + 1)?.label

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp)),
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
            if (!isLast) {
                IncButton(
                    text = "Skip to calc",
                    onClick = onSkip,
                    variant = IncButtonVariant.OUTLINED,
                    fullWidth = false,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                )
            }
            IncButton(
                text = if (isLast) (if (isLoading) "Calculating…" else "Calculate detailed projection") else "Continue to $nextLabel",
                onClick = onNext,
                fullWidth = false,
                enabled = !isLoading,
                modifier = Modifier.weight(if (isLast) 1f else 1.5f),
            )
        }
    }
}
