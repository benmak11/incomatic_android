package com.makusha.incomatic.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.calculator.CalculatorUiState
import com.makusha.incomatic.design.AndroidDonut
import com.makusha.incomatic.design.DonutWedge
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncOverline
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.CalculateResponse
import com.makusha.incomatic.net.dto.LineItem
import com.makusha.incomatic.net.dto.LineItemCategory
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.nav.AppSectionHeader
import com.makusha.incomatic.util.formatMoney
import kotlin.math.abs

/**
 * Loading -> result -> empty router. Result view is driven by the real
 * CalculateResponse from POST /v1/calculate — donut wedges + breakdown rows
 * grouped by LineItemCategory, not mocked math. Yearly Outlook is out of
 * scope here (needs RSU grant data — Phase 5); this shows the current-period
 * breakdown only.
 */
@Composable
fun InsightsScreen(
    uiState: CalculatorUiState,
    grants: List<RsuGrant>,
    onOpenCalculator: () -> Unit,
    onCompactChange: (Boolean) -> Unit,
) {
    when {
        uiState.isLoading -> InsightsLoading()
        uiState.result != null -> InsightsResult(uiState.form, uiState.result, grants, uiState.errorMessage, onCompactChange)
        else -> InsightsEmpty(uiState.errorMessage, onOpenCalculator)
    }
}

@Composable
private fun InsightsLoading() {
    val colors = incColors()
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Calculating…", style = IncType.title, color = colors.textDim)
    }
}

@Composable
private fun InsightsEmpty(errorMessage: String?, onOpenCalculator: () -> Unit) {
    val colors = incColors()
    Column(modifier = Modifier.fillMaxSize()) {
        AppSectionHeader<Unit>(title = "Insights")
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("No results yet", style = IncType.sheetTitle, color = colors.text)
            Spacer(Modifier.size(8.dp))
            Text(
                errorMessage ?: "Run a projection and the full breakdown of your paycheck shows up here.",
                style = IncType.secondary,
                color = if (errorMessage != null) colors.red else colors.textDim,
            )
            Spacer(Modifier.size(20.dp))
            IncButton(text = "Open Calculator", onClick = onOpenCalculator, fullWidth = false)
        }
    }
}

private val TAX_CATEGORIES = setOf(LineItemCategory.TAX_FEDERAL, LineItemCategory.TAX_FICA, LineItemCategory.TAX_STATE)

@Composable
private fun InsightsResult(
    form: com.makusha.incomatic.calculator.CalculatorState,
    result: CalculateResponse,
    grants: List<RsuGrant>,
    errorMessage: String?,
    onCompactChange: (Boolean) -> Unit,
) {
    val colors = incColors()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 40.dp.toPx() }
    var lastScroll by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState.value) {
        val dy = scrollState.value - lastScroll
        if (abs(dy) > 6) onCompactChange(dy > 0 && scrollState.value > thresholdPx)
        lastScroll = scrollState.value
    }

    val net = result.netPerCadence ?: 0.0
    val taxesTotal = result.lineItems.filter { it.category in TAX_CATEGORIES }.sumOf { it.amount }
    val benefitsTotal = result.lineItems.filter { it.category == LineItemCategory.PRE_TAX_BENEFIT }.sumOf { it.amount }
    val retirementTotal = result.lineItems.filter { it.category == LineItemCategory.RETIREMENT }.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 132.dp)) {
        AppSectionHeader<Unit>(title = "Insights")
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (errorMessage != null) {
                IncCard {
                    Text(errorMessage, style = IncType.secondary, color = colors.red)
                }
            }
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
        YearlyOutlook(form = form, result = result, grants = grants)
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

