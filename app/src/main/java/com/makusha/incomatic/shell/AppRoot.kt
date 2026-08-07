package com.makusha.incomatic.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.makusha.incomatic.account.AccountManager
import com.makusha.incomatic.calculator.CalculatorViewModel
import com.makusha.incomatic.data.OnboardingPrefs
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.equity.EquityViewModel
import com.makusha.incomatic.history.HistoryViewModel
import com.makusha.incomatic.nav.MainTab
import com.makusha.incomatic.net.UpgradeGate
import com.makusha.incomatic.onboarding.OnboardingFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Owns the single [CalculatorViewModel] shared by Onboarding and the Shell,
 * and gates between them on the DataStore-backed first-run flag. Renders
 * nothing while the flag is loading (first frame or two) rather than
 * flashing Onboarding then swapping to the Shell.
 */
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val prefs = remember { OnboardingPrefs(context) }
    val scope = rememberCoroutineScope()
    val nullableOnboardingFlag = remember(prefs) { prefs.hasCompletedOnboarding.map<Boolean, Boolean?> { it } }
    val hasCompletedOnboarding by nullableOnboardingFlag.collectAsStateWithLifecycle(initialValue = null)
    var forceShell by remember { mutableStateOf(false) }
    var startTab by remember { mutableStateOf(MainTab.Calculator) }

    val viewModel: CalculatorViewModel = viewModel()
    val equityViewModel: EquityViewModel = viewModel()
    val accountManager: AccountManager = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val colors = incColors()

    // Wins over every other branch, including onboarding: once the backend has
    // refused this build, nothing below here can complete a request.
    val upgradeRequirement by UpgradeGate.requirement.collectAsStateWithLifecycle()
    upgradeRequirement?.let {
        UpgradeRequiredScreen(requirement = it)
        return
    }

    when {
        hasCompletedOnboarding == null -> Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {}
        forceShell || hasCompletedOnboarding == true ->
            IncomaticShell(
                viewModel = viewModel,
                equityViewModel = equityViewModel,
                accountManager = accountManager,
                historyViewModel = historyViewModel,
                startTab = startTab,
            )
        else -> OnboardingFlow(
            viewModel = viewModel,
            onComplete = {
                scope.launch { prefs.setCompleted() }
                startTab = MainTab.Insights
                forceShell = true
            },
        )
    }
}
