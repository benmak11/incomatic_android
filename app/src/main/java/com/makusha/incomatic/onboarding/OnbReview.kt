package com.makusha.incomatic.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.calculator.CalculatorState
import com.makusha.incomatic.calculator.IncomeType
import com.makusha.incomatic.calculator.LivePreviewEstimator
import com.makusha.incomatic.calculator.filingStatusLabel
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncOverline
import com.makusha.incomatic.design.IncSubtleRippleScope
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.UsState
import com.makusha.incomatic.util.formatMoney

private data class ReviewRow(val stepId: String, val label: String, val value: String)

/** Ported from AndOnbReview — live headline number + tappable rows that jump back to the originating step. */
@Composable
fun OnbReview(form: CalculatorState, usStates: List<UsState>, onJumpTo: (String) -> Unit) {
    val colors = incColors()
    val live = LivePreviewEstimator.estimate(form)

    val rows = listOf(
        ReviewRow(
            "wage",
            if (form.incomeType == IncomeType.SALARY) "Annual salary" else "Hourly rate",
            formatMoney((if (form.incomeType == IncomeType.SALARY) form.salary else form.hourlyRate).toDoubleOrNull() ?: 0.0, cents = false),
        ),
        ReviewRow("payfreq", "Pay frequency", form.payFrequency.label),
        ReviewRow("bonus", "Bonus", if (form.bonus.isNotBlank()) formatMoney(form.bonus.toDoubleOrNull() ?: 0.0, cents = false) else "None"),
        ReviewRow("commission", "Commission", if (form.commission.isNotBlank()) formatMoney(form.commission.toDoubleOrNull() ?: 0.0, cents = false) else "None"),
        ReviewRow("filing", "Filing status", filingStatusLabel(form.filingStatus)),
        ReviewRow("state", "Work state", usStates.find { it.code == form.stateCode }?.name ?: form.stateCode),
        ReviewRow("retirement", "Retirement", "${form.t401k}% 401(k) · ${form.roth}% Roth"),
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp).padding(bottom = 20.dp)) {
            IncOverline("Your take-home, roughly")
            Text(
                if (live != null) formatMoney(live.perPeriod, cents = false) else "—",
                style = IncType.heroMoney,
                color = colors.text,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                "per ${form.payFrequency.label.lowercase()} paycheck · edit anything below",
                style = IncType.secondary,
                color = colors.textDim,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        IncCard(pad = 8.dp) {
            Column {
                rows.forEachIndexed { i, row ->
                    OnbReviewRow(row.label, row.value, showTopDivider = i > 0) { onJumpTo(row.stepId) }
                }
            }
        }
    }
}

@Composable
private fun OnbReviewRow(label: String, value: String, showTopDivider: Boolean, onClick: () -> Unit) {
    val colors = incColors()
    IncSubtleRippleScope {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .let { m ->
                    if (showTopDivider) {
                        m.drawBehind { drawLine(colors.hairline, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
                    } else m
                }
                .padding(vertical = 13.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(label, style = IncType.secondary.copy(fontSize = 13.5.sp), color = colors.textDim, modifier = Modifier.weight(1f))
            Text(value, style = IncType.body.copy(fontWeight = FontWeight.Bold), color = colors.text)
            Canvas(modifier = Modifier.size(15.dp)) {
                val w = size.width
                val h = size.height
                val chevron = Path().apply {
                    moveTo(w * 0.35f, h * 0.2f)
                    lineTo(w * 0.65f, h * 0.5f)
                    lineTo(w * 0.35f, h * 0.8f)
                }
                drawPath(chevron, colors.textMute, style = Stroke(w * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}
