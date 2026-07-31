package com.makusha.incomatic.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.calculator.CalculatorUiState
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.CalculateResponse
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.nav.AppSectionHeader
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

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 132.dp)) {
        AppSectionHeader<Unit>(title = "Insights")
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (errorMessage != null) {
                IncCard {
                    Text(errorMessage, style = IncType.secondary, color = colors.red)
                }
            }
            CalculationBreakdownCard(result)
        }
        YearlyOutlook(form = form, result = result, grants = grants)
    }
}

