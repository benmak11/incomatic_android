package com.makusha.incomatic.calculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Hand-drawn Canvas icons approximating the design's card-header SVGs. */
enum class CalcIcon { WALLET, FLAG, PIN, HEART, CHART, SEED }

/** Decorative — the adjacent IncCardHeader title already carries the same information. */
@Composable
fun CalcCardIcon(icon: CalcIcon, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(17.dp).semantics { hideFromAccessibility() }) { drawCalcIcon(icon, color) }
}

private fun DrawScope.drawCalcIcon(icon: CalcIcon, color: Color) {
    val w = size.width
    val h = size.height
    val sw = w * 0.1f
    val stroke = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (icon) {
        CalcIcon.WALLET -> {
            drawRoundRect(color, topLeft = Offset(w * 0.1f, h * 0.25f), size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.55f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f), style = stroke)
            drawLine(color, Offset(w * 0.1f, h * 0.42f), Offset(w * 0.9f, h * 0.42f), sw, cap = StrokeCap.Round)
            drawCircle(color, radius = w * 0.06f, center = Offset(w * 0.7f, h * 0.58f))
        }
        CalcIcon.FLAG -> {
            val p = Path().apply {
                moveTo(w * 0.2f, h * 0.9f); lineTo(w * 0.2f, h * 0.15f)
                lineTo(w * 0.8f, h * 0.15f); lineTo(w * 0.65f, h * 0.4f)
                lineTo(w * 0.8f, h * 0.65f); lineTo(w * 0.2f, h * 0.65f)
            }
            drawPath(p, color, style = stroke)
        }
        CalcIcon.PIN -> {
            drawArc(color, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(w * 0.14f, h * 0.06f), size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.72f), style = stroke)
            val p = Path().apply {
                moveTo(w * 0.18f, h * 0.5f)
                lineTo(w * 0.5f, h * 0.92f)
                lineTo(w * 0.82f, h * 0.5f)
            }
            drawPath(p, color, style = stroke)
            drawCircle(color, radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.42f), style = stroke)
        }
        CalcIcon.HEART -> {
            val p = Path().apply {
                moveTo(w * 0.5f, h * 0.85f)
                cubicTo(w * 0.1f, h * 0.55f, w * 0.15f, h * 0.15f, w * 0.5f, h * 0.35f)
                cubicTo(w * 0.85f, h * 0.15f, w * 0.9f, h * 0.55f, w * 0.5f, h * 0.85f)
                close()
            }
            drawPath(p, color, style = stroke)
        }
        CalcIcon.CHART -> {
            val p = Path().apply {
                moveTo(w * 0.12f, h * 0.7f); lineTo(w * 0.38f, h * 0.44f)
                lineTo(w * 0.55f, h * 0.6f); lineTo(w * 0.88f, h * 0.22f)
            }
            drawPath(p, color, style = stroke)
            val p2 = Path().apply { moveTo(w * 0.62f, h * 0.22f); lineTo(w * 0.88f, h * 0.22f); lineTo(w * 0.88f, h * 0.46f) }
            drawPath(p2, color, style = stroke)
        }
        CalcIcon.SEED -> {
            drawLine(color, Offset(w * 0.5f, h * 0.9f), Offset(w * 0.5f, h * 0.35f), sw, cap = StrokeCap.Round)
            drawArc(color, startAngle = 180f, sweepAngle = -90f, useCenter = false, topLeft = Offset(w * 0.5f, h * 0.1f), size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.4f), style = stroke)
            drawArc(color, startAngle = 0f, sweepAngle = 90f, useCenter = false, topLeft = Offset(w * 0.1f, h * 0.1f), size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.4f), style = stroke)
        }
    }
}
