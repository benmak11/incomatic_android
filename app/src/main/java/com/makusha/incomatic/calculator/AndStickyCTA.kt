package com.makusha.incomatic.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncButtonVariant
import com.makusha.incomatic.design.IncOverline
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.util.formatMoney

/**
 * Live projection ribbon (from [LivePreviewEstimator], instant/local) over
 * the Continue/Calculate button row, pinned above the pill nav. Ribbon
 * opacity tracks scroll [progress] (0.45 -> 1.0), matching the design.
 */
@Composable
fun AndStickyCTA(
    form: CalculatorState,
    section: CalculatorSection,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    progress: Float = 1f,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = incColors()
    val sections = CalculatorSection.entries
    val idx = sections.indexOf(section)
    val isLast = idx == sections.lastIndex
    val nextLabel = sections.getOrNull(idx + 1)?.label
    val live = LivePreviewEstimator.estimate(form)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp)),
    ) {
        Column {
            if (live != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.sageBg.copy(alpha = colors.sageBg.alpha * (0.45f + 0.55f * progress)))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        IncOverline("Projected · per paycheck", color = colors.sage)
                        Text(formatMoney(live.perPeriod), style = IncType.cardMoney.copy(color = colors.sageDeep))
                    }
                    Text("${live.takeHomePct.toInt()}% of gross", style = IncType.secondary.copy(fontWeight = FontWeight.Bold), color = colors.sage)
                }
            }
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
}
