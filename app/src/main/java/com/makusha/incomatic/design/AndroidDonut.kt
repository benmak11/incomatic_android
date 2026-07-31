package com.makusha.incomatic.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutWedge(val value: Double, val color: Color)

/**
 * Canvas-drawn donut chart — port of the design's SVG stroke-dasharray
 * animation. One shared progress animatable sweeps every wedge in together
 * rather than the JS's per-wedge staggered delay (0.07s * index) — a minor
 * simplification, not worth the extra Animatable-per-wedge bookkeeping for
 * a chart with at most 4 wedges.
 */
@Composable
fun AndroidDonut(
    wedges: List<DonutWedge>,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    thickness: Dp = 22.dp,
    animate: Boolean = true,
    center: @Composable () -> Unit = {},
) {
    val colors = incColors()
    val total = wedges.sumOf { it.value }.let { if (it <= 0.0) 1.0 else it }
    val progress = remember { Animatable(if (animate) 0f else 1f) }

    LaunchedEffect(wedges, animate) {
        if (animate) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(700, delayMillis = 60, easing = FastOutSlowInEasing))
        } else {
            progress.snapTo(1f)
        }
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        // Decorative — center() (e.g. the take-home total) carries the actual figures.
        Canvas(modifier = Modifier.size(size).semantics { hideFromAccessibility() }) {
            val strokePx = thickness.toPx()
            val diameter = this.size.minDimension - strokePx
            val topLeft = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = colors.donutTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokePx),
            )

            var startAngle = -90f
            wedges.forEach { wedge ->
                val sweep = (wedge.value / total).toFloat() * 360f * progress.value
                if (sweep > 0f) {
                    drawArc(
                        color = wedge.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokePx, cap = StrokeCap.Butt),
                    )
                }
                startAngle += sweep
            }
        }
        center()
    }
}
