package com.makusha.incomatic.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.design.AndroidDonut
import com.makusha.incomatic.design.DonutWedge
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncOverline
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.CalculateResponse
import com.makusha.incomatic.net.dto.LineItem
import com.makusha.incomatic.net.dto.LineItemCategory
import com.makusha.incomatic.util.formatMoney

private val TAX_CATEGORIES = setOf(LineItemCategory.TAX_FEDERAL, LineItemCategory.TAX_FICA, LineItemCategory.TAX_STATE)

/**
 * Donut + legend + per-paycheck breakdown rows, driven by any [CalculateResponse] —
 * the live Insights result or a saved History session's response, same rendering
 * either way. Extracted out of InsightsScreen.kt's InsightsResult so History's
 * SessionDetailView can reuse it without a mapper layer (unlike iOS, Android's
 * Insights already renders directly off CalculateResponse, no intermediate
 * ViewFriendlyResponse type to bridge into).
 */
@Composable
fun CalculationBreakdownCard(result: CalculateResponse) {
    val colors = incColors()
    val net = result.netPerCadence ?: 0.0
    val taxesTotal = result.lineItems.filter { it.category in TAX_CATEGORIES }.sumOf { it.amount }
    val benefitsTotal = result.lineItems.filter { it.category == LineItemCategory.PRE_TAX_BENEFIT }.sumOf { it.amount }
    val retirementTotal = result.lineItems.filter { it.category == LineItemCategory.RETIREMENT }.sumOf { it.amount }

    IncCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            AndroidDonut(
                wedges = listOf(
                    DonutWedge(net, colors.sage),
                    DonutWedge(taxesTotal, colors.blush),
                    DonutWedge(benefitsTotal, colors.gold),
                    DonutWedge(retirementTotal, colors.sageSoft),
                ),
                size = 186.dp,
                thickness = 22.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IncOverline("Take-home")
                    Text(formatMoney(net), style = IncType.pageTitle.copy(fontSize = 25.sp), color = colors.text)
                }
            }
            Spacer(Modifier.size(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendDot(colors.sage, "Take-home")
                LegendDot(colors.blush, "Taxes")
                LegendDot(colors.gold, "Benefits")
                LegendDot(colors.sageSoft, "Retirement")
            }
        }
    }
    IncCard {
        IncOverline("Per paycheck")
        Spacer(Modifier.size(4.dp))
        BreakdownRow("Gross pay", formatMoney(result.grossPerCadence ?: 0.0), strong = true)
        result.lineItems.forEach { item -> BreakdownRow(item.name, signedAmount(item)) }
        Column(
            modifier = Modifier
                .padding(top = 6.dp)
                .drawBehind {
                    drawLine(colors.hairlineStrong, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx())
                }
                .padding(top = 4.dp),
        ) {
            BreakdownRow("Take-home", formatMoney(net), strong = true)
        }
    }
}

private fun signedAmount(item: LineItem): String {
    val negative = item.category != null && item.category != LineItemCategory.EARNINGS && item.category != LineItemCategory.NET
    return if (negative) "−" + formatMoney(item.amount) else formatMoney(item.amount)
}

@Composable
private fun BreakdownRow(label: String, value: String, strong: Boolean = false) {
    val colors = incColors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = if (strong) IncType.title else IncType.body, color = colors.text)
        Text(value, style = (if (strong) IncType.title else IncType.body).copy(fontWeight = if (strong) FontWeight.Bold else FontWeight.Medium), color = colors.text)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val colors = incColors()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(modifier = Modifier.size(8.dp).clip(CircleShape)) {
            Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(color) }
        }
        Text(label, style = IncType.secondary.copy(fontWeight = FontWeight.SemiBold), color = colors.textDim)
    }
}
