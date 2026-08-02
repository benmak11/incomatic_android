package com.makusha.incomatic.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makusha.incomatic.account.AccountManager
import com.makusha.incomatic.account.AccountSheet
import com.makusha.incomatic.calculator.CalculatorTab
import com.makusha.incomatic.calculator.CalculatorViewModel
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.design.rememberPredictiveBackScale
import com.makusha.incomatic.equity.EquityViewModel
import com.makusha.incomatic.equity.GrantsDialog
import com.makusha.incomatic.history.HistoryTab
import com.makusha.incomatic.history.HistoryViewModel
import com.makusha.incomatic.insights.InsightsScreen
import com.makusha.incomatic.nav.AccountGlyph
import com.makusha.incomatic.nav.AppPillNav
import com.makusha.incomatic.nav.MainTab

/**
 * Tabs are plain state, not a NavHost — matching iOS and the design doc's
 * explicit choice. Every tab is real as of Phase 4. [viewModel] is hoisted
 * to [com.makusha.incomatic.shell.AppRoot] so Onboarding and the Shell share
 * one instance; [startTab] lets Onboarding hand off straight into Insights
 * after a first calculation. [equityViewModel]/[accountManager]/
 * [historyViewModel] are hoisted the same way — each is needed by more than
 * one tab (Equity card + Yearly Outlook; account glyph + both sign-in CTAs;
 * History tab + its own detail screen).
 */
@Composable
fun IncomaticShell(
    viewModel: CalculatorViewModel,
    equityViewModel: EquityViewModel,
    accountManager: AccountManager,
    historyViewModel: HistoryViewModel,
    startTab: MainTab = MainTab.Calculator,
) {
    var tab by remember { mutableStateOf(startTab) }
    var compact by remember { mutableStateOf(false) }
    var showGrantsDialog by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    val colors = incColors()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by accountManager.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) { equityViewModel.load(currentUser != null) }

    val backScale = rememberPredictiveBackScale(enabled = tab != MainTab.Calculator) {
        tab = MainTab.Calculator
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg).then(backScale)) {
        when (tab) {
            MainTab.Calculator -> CalculatorTab(
                uiState = uiState,
                equity = equityViewModel,
                signedIn = currentUser != null,
                currentTab = tab,
                onTabSelected = { tab = it; compact = false },
                onFormUpdate = viewModel::updateForm,
                onSectionChange = viewModel::setSection,
                onCalculate = { viewModel.calculate(equityViewModel.vestingThisYear) { tab = MainTab.Insights } },
                onOpenGrants = { showGrantsDialog = true },
                onShowAccount = { showAccountSheet = true },
            )
            MainTab.Insights -> InsightsScreen(
                uiState = uiState,
                grants = equityViewModel.grants.collectAsStateWithLifecycle().value,
                onOpenCalculator = { tab = MainTab.Calculator },
                onCompactChange = { compact = it },
            )
            MainTab.History -> HistoryTab(
                accountManager = accountManager,
                viewModel = historyViewModel,
                onCompactChange = { compact = it },
            )
        }

        AccountGlyph(
            signedIn = currentUser != null,
            initials = currentUser?.initials ?: "?",
            onClick = { showAccountSheet = true },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 18.dp),
        )

        // Calculator embeds its own copy of AppPillNav inside
        // CalculatorBottomDock (combined with the CTA, to fix the overlap
        // this floating copy used to cause there); this floating instance
        // now only serves Insights/History.
        if (tab != MainTab.Calculator) {
            AppPillNav(
                tab = tab,
                onTabSelected = { tab = it; compact = false },
                compact = compact,
                onExpand = { compact = false },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    if (showGrantsDialog) {
        GrantsDialog(equity = equityViewModel, onDismiss = { showGrantsDialog = false })
    }
    if (showAccountSheet) {
        val savedCount by historyViewModel.sessions.collectAsStateWithLifecycle()
        AccountSheet(accountManager = accountManager, savedCount = savedCount.size, onClose = { showAccountSheet = false })
    }
}
