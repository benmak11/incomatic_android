package com.makusha.incomatic.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.design.AndroidDonut
import com.makusha.incomatic.design.DonutWedge
import com.makusha.incomatic.design.IncSubtleRippleScope
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.SavedCalculationSummary
import com.makusha.incomatic.net.dto.displaySubtitle
import com.makusha.incomatic.net.dto.displayTitle
import com.makusha.incomatic.net.dto.savedAtCompact
import com.makusha.incomatic.util.formatMoney
import kotlin.math.roundToInt

/** Single row in the History list — mini donut, state name + freq, savedAt/amount/percent, chevron. */
@Composable
fun SessionRow(summary: SavedCalculationSummary, showDivider: Boolean, onClick: () -> Unit) {
    val colors = incColors()
    IncSubtleRippleScope {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .let { m ->
                    if (showDivider) {
                        m.drawBehind {
                            drawLine(colors.hairline, Offset(58.dp.toPx(), size.height), Offset(size.width, size.height), 1.dp.toPx())
                        }
                    } else m
                }
                .padding(vertical = 16.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AndroidDonut(
                wedges = listOf(
                    DonutWedge(summary.takeHomePerPeriod ?: 0.0, colors.sage),
                    DonutWedge(summary.taxesPerPeriod ?: 0.0, colors.blush),
                    DonutWedge(summary.benefitsPerPeriod ?: 0.0, colors.gold),
                ),
                size = 40.dp,
                thickness = 9.dp,
                animate = false,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    summary.displayTitle,
                    style = IncType.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.5.sp),
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(summary.displaySubtitle, style = IncType.secondary.copy(fontSize = 12.sp), color = colors.textMute)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    summary.savedAtCompact.uppercase(),
                    style = IncType.overline.copy(fontSize = 10.5.sp),
                    color = colors.textMute,
                )
                Text(
                    formatMoney(summary.takeHomePerPeriod ?: 0.0, cents = false),
                    style = IncType.body.copy(fontWeight = FontWeight.Bold, fontSize = 16.5.sp),
                    color = colors.text,
                )
                Text(
                    "${(summary.takeHomePct ?: 0.0).roundToInt()}% of ${formatMoney(summary.grossPerPeriod ?: 0.0, cents = false)}",
                    style = IncType.secondary.copy(fontSize = 11.5.sp),
                    color = colors.textDim,
                )
            }
            Chevron()
        }
    }
}

@Composable
private fun Chevron() {
    val colors = incColors()
    Canvas(modifier = Modifier.size(11.dp)) {
        val w = size.width
        val h = size.height
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.25f, h * 0.1f)
            lineTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.9f)
        }
        drawPath(p, colors.textMute, style = Stroke(w * 0.18f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
