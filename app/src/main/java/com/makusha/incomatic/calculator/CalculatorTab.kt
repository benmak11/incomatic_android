package com.makusha.incomatic.calculator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.equity.EquityViewModel
import com.makusha.incomatic.nav.AppSectionHeader
import kotlin.math.abs

/**
 * The Calculator tab's own content: section-switching (hairline tabs), the
 * four section bodies, and the sticky CTA overlaying the bottom. Scroll
 * drives both the pill-nav collapse (via [onCompactChange]) and the sticky
 * CTA's live-ribbon opacity.
 */
@Composable
fun CalculatorTab(
    uiState: CalculatorUiState,
    equity: EquityViewModel,
    signedIn: Boolean,
    onFormUpdate: ((CalculatorState) -> CalculatorState) -> Unit,
    onSectionChange: (CalculatorSection) -> Unit,
    onCalculate: () -> Unit,
    onCompactChange: (Boolean) -> Unit,
    onOpenGrants: () -> Unit,
    onShowAccount: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 40.dp.toPx() }
    var lastScroll by remember { mutableIntStateOf(0) }
    var ribbonProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(scrollState.value) {
        val max = scrollState.maxValue
        ribbonProgress = if (max > 0) (scrollState.value.toFloat() / max).coerceIn(0f, 1f) else 0f
        val dy = scrollState.value - lastScroll
        if (abs(dy) > 6) onCompactChange(dy > 0 && scrollState.value > thresholdPx)
        lastScroll = scrollState.value
    }

    val sections = CalculatorSection.entries
    val currentIdx = sections.indexOf(uiState.section)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 230.dp),
        ) {
            AppSectionHeader(
                title = "Calculator",
                sections = sections,
                selected = uiState.section,
                labelOf = { it.label },
                onSelect = onSectionChange,
            )
            when (uiState.section) {
                CalculatorSection.EARNINGS -> AndEarnings(uiState.form, onFormUpdate, equity, signedIn, onOpenGrants, onShowAccount)
                CalculatorSection.FEDERAL -> AndFederal(uiState.form, onFormUpdate)
                CalculatorSection.STATE -> AndState(uiState.form, uiState.usStates, onFormUpdate)
                CalculatorSection.BENEFITS -> AndBenefits(uiState.form, onFormUpdate)
            }
        }

        AndStickyCTA(
            form = uiState.form,
            section = uiState.section,
            onNext = {
                if (currentIdx == sections.lastIndex) onCalculate() else onSectionChange(sections[currentIdx + 1])
            },
            onSkip = onCalculate,
            progress = ribbonProgress,
            isLoading = uiState.isLoading,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp),
        )
    }
}
