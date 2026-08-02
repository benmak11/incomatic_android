package com.makusha.incomatic.calculator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncOverline
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.equity.EquityViewModel
import com.makusha.incomatic.nav.AppSectionHeader
import com.makusha.incomatic.nav.MainTab
import com.makusha.incomatic.util.formatMoney

/**
 * The Calculator tab's own content: a sticky header (title + section tabs +
 * live per-paycheck figure), the four section bodies, and a bottom-docked
 * CTA + tab nav. The header sits outside the scroll area (not its first
 * child) so it stays visible while scrolling; the dock's own rendered
 * height is measured and fed back into the scroll content's bottom padding,
 * so there's no hardcoded clearance guess to keep in sync with the dock's
 * actual size.
 */
@Composable
fun CalculatorTab(
    uiState: CalculatorUiState,
    equity: EquityViewModel,
    signedIn: Boolean,
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onFormUpdate: ((CalculatorState) -> CalculatorState) -> Unit,
    onSectionChange: (CalculatorSection) -> Unit,
    onCalculate: () -> Unit,
    onOpenGrants: () -> Unit,
    onShowAccount: () -> Unit,
) {
    val colors = incColors()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var dockHeightPx by remember { mutableIntStateOf(0) }
    val dockHeightDp = with(density) { dockHeightPx.toDp() }

    val sections = CalculatorSection.entries
    val currentIdx = sections.indexOf(uiState.section)
    val live = LivePreviewEstimator.estimate(uiState.form)

    Column(modifier = Modifier.fillMaxSize()) {
        AppSectionHeader(
            title = "Calculator",
            sections = sections,
            selected = uiState.section,
            labelOf = { it.label },
            onSelect = onSectionChange,
            trailing = {
                Column(horizontalAlignment = Alignment.End) {
                    IncOverline("Per ${uiState.form.payFrequency.label}", color = colors.textMute)
                    Text(
                        if (live != null) formatMoney(live.perPeriod) else "—",
                        style = IncType.cardMoney,
                        color = colors.sageDeep,
                    )
                }
            },
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = dockHeightDp),
            ) {
                when (uiState.section) {
                    CalculatorSection.EARNINGS -> AndEarnings(uiState.form, onFormUpdate, equity, signedIn, onOpenGrants, onShowAccount)
                    CalculatorSection.FEDERAL -> AndFederal(uiState.form, onFormUpdate)
                    CalculatorSection.STATE -> AndState(uiState.form, uiState.usStates, onFormUpdate)
                    CalculatorSection.BENEFITS -> AndBenefits(uiState.form, onFormUpdate)
                }
            }

            CalculatorBottomDock(
                section = uiState.section,
                tab = currentTab,
                onTabSelected = onTabSelected,
                onNext = {
                    if (currentIdx == sections.lastIndex) onCalculate() else onSectionChange(sections[currentIdx + 1])
                },
                onSkip = onCalculate,
                isLoading = uiState.isLoading,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned { dockHeightPx = it.size.height },
            )
        }
    }
}
