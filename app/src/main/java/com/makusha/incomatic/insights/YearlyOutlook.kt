package com.makusha.incomatic.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.calculator.CalcCardIcon
import com.makusha.incomatic.calculator.CalcIcon
import com.makusha.incomatic.calculator.CalculatorState
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.equity.VestMath
import com.makusha.incomatic.net.dto.CalculateResponse
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.util.formatMoney
import kotlin.math.roundToInt

private data class YearEntry(val year: Int, val base: Double, val bonus: Double, val rsu: Double) {
    val total: Double get() = base + bonus + rsu
    val nonBasePct: Double get() = if (total > 0) (bonus + rsu) / total * 100 else 0.0
}

private const val VISIBLE_YEAR_CAP = 6

/**
 * Yearly earnings outlook — direct port of iOS's YearlyOutlookView.
 * Collapsed pill -> expanded year rows with stacked base(sage)/bonus(blush)/
 * RSU(gold) bars. All math is local: base held flat, one-time bonus in its
 * payout year / recurring from its start year onward, RSU vests at today's
 * price. GROSS figures, labeled so. Clamped to the current tax year — past
 * vest years never appear (a gap the design prototype had; iOS already
 * fixed it, this carries the fix forward).
 */
@Composable
fun YearlyOutlook(form: CalculatorState, result: CalculateResponse, grants: List<RsuGrant>) {
    val colors = incColors()
    var expanded by remember { mutableStateOf(false) }
    var showAllYears by remember { mutableStateOf(false) }

    val baseAnnual = (result.baseSalaryPerCadence ?: 0.0) * form.payFrequency.cadence.periodsPerYear
    val bonusAmount = form.bonus.toDoubleOrNull() ?: 0.0
    val bonusStartYear = VestMath.parseDate(form.bonusDate)?.year ?: AppConfig.taxYear

    fun bonusInYear(year: Int): Double {
        if (bonusAmount <= 0) return 0.0
        val applies = if (form.bonusRecurring) year >= bonusStartYear else year == bonusStartYear
        return if (applies) bonusAmount else 0.0
    }

    fun rsuInYear(year: Int): Double =
        if (year == AppConfig.taxYear) result.supplemental?.rsuGross ?: 0.0 else VestMath.value(year, grants)

    val firstYear = AppConfig.taxYear
    val lastYear = maxOf(
        VestMath.finalVestYear(grants) ?: firstYear,
        if (bonusAmount > 0) bonusStartYear else firstYear,
        firstYear + 1,
    )
    val entries = (firstYear..lastYear).map { year -> YearEntry(year, baseAnnual, bonusInYear(year), rsuInYear(year)) }
    val visibleEntries = if (showAllYears) entries else entries.take(VISIBLE_YEAR_CAP)
    val highlightEntry = entries.filter { it.bonus + it.rsu > 0 }.maxByOrNull { it.nonBasePct }
    val hasBonusAnywhere = entries.any { it.bonus > 0 }
    val hasRsuAnywhere = entries.any { it.rsu > 0 }
    val nonBaseLabel = when {
        hasBonusAnywhere && hasRsuAnywhere -> "Bonus + RSUs add up to"
        hasBonusAnywhere -> "Bonus adds up to"
        else -> "RSUs add up to"
    }
    val maxTotal = maxOf(entries.maxOfOrNull { it.total } ?: 1.0, 1.0)

    IncCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(colors.sageBg),
                contentAlignment = Alignment.Center,
            ) {
                CalcCardIcon(CalcIcon.CHART, colors.sage)
            }
            Spacer(Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Yearly earnings outlook · ${entries.first().year}–${entries.last().year}",
                    style = IncType.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                Text(
                    if (highlightEntry != null) {
                        "$nonBaseLabel ${highlightEntry.nonBasePct.roundToInt()}% in ${highlightEntry.year} · gross"
                    } else {
                        "Gross earnings, before taxes and deductions"
                    },
                    style = IncType.secondary.copy(fontSize = 12.sp),
                    color = colors.textDim,
                )
            }
            ExpandChevron(expanded = expanded)
        }

        if (expanded) {
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                visibleEntries.forEach { entry -> YearRow(entry, maxTotal) }
            }
            if (entries.size > VISIBLE_YEAR_CAP && !showAllYears) {
                Text(
                    "+${entries.size - VISIBLE_YEAR_CAP} more years",
                    style = IncType.secondary.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.sage,
                    modifier = Modifier.padding(top = 8.dp).clickable { showAllYears = true },
                )
            }
            Text(
                assumptionsCaption(baseAnnual, hasBonusAnywhere, form.bonusRecurring, hasRsuAnywhere),
                style = IncType.secondary.copy(fontSize = 11.5.sp),
                color = colors.textDim,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun YearRow(entry: YearEntry, maxTotal: Double) {
    val colors = incColors()
    val isCurrent = entry.year == AppConfig.taxYear
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.year.toString(),
                style = IncType.secondary.copy(fontSize = 13.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold),
                color = colors.text,
            )
            if (isCurrent) {
                Spacer(Modifier.size(6.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(colors.sageBg).padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text("THIS YEAR'S PAYCHECK CALC", style = IncType.overline.copy(fontSize = 9.sp), color = colors.sageDeep)
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                formatMoney(entry.total, cents = false),
                style = IncType.secondary.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
        }
        Spacer(Modifier.size(4.dp))
        Row(modifier = Modifier.fillMaxWidth((entry.total / maxTotal).toFloat()).height(8.dp)) {
            if (entry.base > 0) SegmentBar(colors.sage, (entry.base / entry.total).toFloat())
            if (entry.bonus > 0) SegmentBar(colors.blush, (entry.bonus / entry.total).toFloat())
            if (entry.rsu > 0) SegmentBar(colors.gold, (entry.rsu / entry.total).toFloat())
        }
        Spacer(Modifier.size(4.dp))
        Text(captionFor(entry), style = IncType.secondary.copy(fontSize = 11.sp), color = colors.textDim)
    }
}

private fun captionFor(entry: YearEntry): String {
    val parts = mutableListOf("Base ${formatMoney(entry.base, cents = false)}")
    if (entry.bonus > 0) parts += "+Bonus ${formatMoney(entry.bonus, cents = false)}"
    if (entry.rsu > 0) parts += "+RSU ${formatMoney(entry.rsu, cents = false)}"
    val pct = if (entry.bonus + entry.rsu > 0) " (${entry.nonBasePct.roundToInt()}%)" else ""
    return parts.joinToString(" · ") + pct
}

private fun assumptionsCaption(baseAnnual: Double, hasBonus: Boolean, bonusRecurring: Boolean, hasRsu: Boolean): String {
    val clauses = mutableListOf("Base pay held flat at ${formatMoney(baseAnnual, cents = false)}/yr.")
    if (hasBonus) {
        clauses += if (bonusRecurring) "Bonus repeats every year at the same amount." else "Bonus shown once, in its payout year."
    }
    if (hasRsu) clauses += "RSU value uses today's price for every future vest. Actual value will differ."
    clauses += "All figures gross, before taxes and deductions."
    return clauses.joinToString(" ")
}

@Composable
private fun ExpandChevron(expanded: Boolean) {
    val colors = incColors()
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(13.dp).graphicsLayer { rotationZ = if (expanded) 180f else 0f },
    ) {
        val w = size.width
        val h = size.height
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.85f, h * 0.35f)
        }
        drawPath(
            p,
            colors.textMute,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                w * 0.16f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}

/** Decorative — the caption row below each bar already states every figure in words. */
@Composable
private fun RowScope.SegmentBar(color: Color, fraction: Float) {
    Box(
        modifier = Modifier
            .weight(fraction.coerceAtLeast(0.02f))
            .fillMaxHeight()
            .padding(end = 1.5.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
            .semantics { hideFromAccessibility() },
    )
}
