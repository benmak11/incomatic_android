package com.makusha.incomatic.shell

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makusha.incomatic.calculator.CalculatorTab
import com.makusha.incomatic.calculator.CalculatorViewModel
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.equity.EquityViewModel
import com.makusha.incomatic.equity.GrantsDialog
import com.makusha.incomatic.insights.InsightsScreen
import com.makusha.incomatic.nav.AccountGlyph
import com.makusha.incomatic.nav.AppPillNav
import com.makusha.incomatic.nav.AppSectionHeader
import com.makusha.incomatic.nav.MainTab
import kotlin.math.abs

/**
 * Tabs are plain state, not a NavHost — matching iOS and the design doc's
 * explicit choice. Calculator and Insights are real (Phase 2); History
 * stays a placeholder until auth exists (Phase 4). [viewModel] is hoisted
 * to [com.makusha.incomatic.shell.AppRoot] (Phase 3) so Onboarding and the
 * Shell share one instance; [startTab] lets Onboarding hand off straight
 * into Insights after a first calculation. [equityViewModel] (Phase 5) is
 * hoisted the same way — both Calculator's Equity card and Insights'
 * Yearly Outlook need the same on-device grants.
 */
@Composable
fun IncomaticShell(viewModel: CalculatorViewModel, equityViewModel: EquityViewModel, startTab: MainTab = MainTab.Calculator) {
    var tab by remember { mutableStateOf(startTab) }
    var compact by remember { mutableStateOf(false) }
    var showGrantsDialog by remember { mutableStateOf(false) }
    val colors = incColors()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = tab != MainTab.Calculator) {
        tab = MainTab.Calculator
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        when (tab) {
            MainTab.Calculator -> CalculatorTab(
                uiState = uiState,
                equity = equityViewModel,
                onFormUpdate = viewModel::updateForm,
                onSectionChange = viewModel::setSection,
                onCalculate = { viewModel.calculate(equityViewModel.vestingThisYear) { tab = MainTab.Insights } },
                onCompactChange = { compact = it },
                onOpenGrants = { showGrantsDialog = true },
            )
            MainTab.Insights -> InsightsScreen(
                uiState = uiState,
                grants = equityViewModel.grants.collectAsStateWithLifecycle().value,
                onOpenCalculator = { tab = MainTab.Calculator },
                onCompactChange = { compact = it },
            )
            MainTab.History -> PlaceholderScreen(
                title = "History",
                tagline = "Signed-out CTA → empty → list → detail — lands in Phase 4, once /v1/auth/google exists.",
                onCompactChange = { compact = it },
            )
        }

        AccountGlyph(
            signedIn = false,
            onClick = { /* wired in Phase 4 */ },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 18.dp),
        )

        AppPillNav(
            tab = tab,
            onTabSelected = { tab = it; compact = false },
            compact = compact,
            onExpand = { compact = false },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showGrantsDialog) {
        GrantsDialog(equity = equityViewModel, onDismiss = { showGrantsDialog = false })
    }
}

/**
 * Scroll-driven pill-nav collapse: scrolling down past a 40dp threshold
 * collapses the pill; scrolling up (or being back near the top) re-expands
 * it. Mirrors the design doc's `dy > 0 && scrollTop > 40` rule using
 * [rememberScrollState]'s absolute offset instead of a raw DOM scrollTop.
 * Still used by the History tab, which remains a placeholder until Phase 4.
 */
@Composable
private fun PlaceholderScreen(title: String, tagline: String, onCompactChange: (Boolean) -> Unit) {
    val colors = incColors()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 40.dp.toPx() }
    var lastScroll by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState.value) {
        val dy = scrollState.value - lastScroll
        if (abs(dy) > 6) {
            onCompactChange(dy > 0 && scrollState.value > thresholdPx)
        }
        lastScroll = scrollState.value
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 132.dp),
    ) {
        AppSectionHeader<Unit>(title = title)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            IncCard {
                Text(tagline, style = IncType.body, color = colors.textDim)
            }
            repeat(5) { i ->
                IncCard {
                    Text("Placeholder card ${i + 1}", style = IncType.title, color = colors.text)
                    Text("Scroll to see the pill nav collapse into the bottom-left handle, then scroll up to bring it back.", style = IncType.secondary, color = colors.textDim)
                }
            }
        }
    }
}
