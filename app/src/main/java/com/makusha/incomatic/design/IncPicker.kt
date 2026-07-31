package com.makusha.incomatic.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * M3 exposed-dropdown-style picker — the Android answer to iOS's wheel/menu
 * picker. Generic over T so callers pass plain values (pay frequency
 * strings, filing-status enum, state codes) with a label function.
 */
@Composable
fun <T> IncPicker(
    label: String,
    value: T,
    options: List<T>,
    onChange: (T) -> Unit,
    labelOf: (T) -> String = { it.toString() },
) {
    val colors = incColors()
    var open by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        IncLabel(label)
        IncSubtleRippleScope {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { open = !open }
                    .padding(bottom = 8.dp)
                    .drawBehind {
                        drawLine(
                            color = if (open) colors.sage else colors.hairline,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2.dp.toPx(),
                        )
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(labelOf(value), style = IncType.title.copy(fontSize = 18.sp), color = colors.text)
                Canvas(
                    modifier = Modifier.size(14.dp).graphicsLayer { rotationZ = if (open) 180f else 0f },
                ) {
                    val w = size.width
                    val h = size.height
                    val sw = w * 0.16f
                    val chevron = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.2f, h * 0.35f)
                        lineTo(w * 0.5f, h * 0.65f)
                        lineTo(w * 0.8f, h * 0.35f)
                    }
                    drawPath(chevron, if (open) colors.sage else colors.textMute, style = Stroke(sw, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                }
            }
        }
        if (open) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.hairline, RoundedCornerShape(12.dp)),
            ) {
                Column(modifier = Modifier.heightIn(max = 232.dp).verticalScroll(rememberScrollState())) {
                    IncSubtleRippleScope {
                        options.forEach { opt ->
                            val sel = opt == value
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChange(opt); open = false }
                                    .let { m -> if (sel) m.background(colors.sageBg) else m }
                                    .padding(horizontal = 14.dp, vertical = 13.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    if (sel) {
                                        val w = size.width
                                        val h = size.height
                                        val check = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(w * 0.15f, h * 0.5f)
                                            lineTo(w * 0.4f, h * 0.75f)
                                            lineTo(w * 0.85f, h * 0.2f)
                                        }
                                        drawPath(check, colors.sage, style = Stroke(w * 0.15f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                                    }
                                }
                                Text(
                                    labelOf(opt),
                                    style = if (sel) IncType.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else IncType.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                                    color = if (sel) colors.sageDeep else colors.text,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
