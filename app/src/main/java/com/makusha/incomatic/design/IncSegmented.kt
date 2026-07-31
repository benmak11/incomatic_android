package com.makusha.incomatic.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SegmentOption<T>(val value: T, val label: String)

/**
 * M3 SegmentedButtonRow — outlined cells with a leading check on the
 * selection, rather than iOS's sliding filled capsule.
 */
@Composable
fun <T> IncSegmented(
    value: T,
    options: List<SegmentOption<T>>,
    onChange: (T) -> Unit,
    label: String? = null,
) {
    val colors = incColors()
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        if (label != null) IncLabel(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .border(1.dp, colors.hairlineStrong, RoundedCornerShape(999.dp)),
        ) {
            IncSubtleRippleScope {
                options.forEachIndexed { i, opt ->
                    val sel = opt.value == value
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onChange(opt.value) }
                            .let { m -> if (sel) m.background(colors.sageBg) else m }
                            .let { m ->
                                if (i > 0) {
                                    m.drawBehind {
                                        drawLine(colors.hairlineStrong, Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx())
                                    }
                                } else m
                            }
                            .padding(vertical = 11.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (sel) {
                            Canvas(modifier = Modifier.size(14.dp)) {
                                val w = size.width
                                val h = size.height
                                val check = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w * 0.15f, h * 0.5f)
                                    lineTo(w * 0.4f, h * 0.75f)
                                    lineTo(w * 0.85f, h * 0.2f)
                                }
                                drawPath(check, colors.sageDeep, style = Stroke(w * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                            androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                        }
                        Text(opt.label, style = IncType.secondary.copy(fontWeight = FontWeight.Bold), color = if (sel) colors.sageDeep else colors.textDim)
                    }
                }
            }
        }
    }
}
