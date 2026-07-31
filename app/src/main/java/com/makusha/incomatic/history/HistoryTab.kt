package com.makusha.incomatic.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makusha.incomatic.account.AccountManager
import com.makusha.incomatic.calculator.CalcIcon
import com.makusha.incomatic.calculator.CalcCardIcon
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncButtonVariant
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.nav.AppSectionHeader
import com.makusha.incomatic.net.dto.SavedCalculationSummary
import kotlin.math.abs
import kotlinx.coroutines.launch

/**
 * Three states: signed-out (sign-in CTA card), signed-in empty (prompt to run a
 * calculation), signed-in with sessions (list). Tapping a row shows SessionDetailView.
 * Mirrors HistoryTab.swift.
 */
@Composable
fun HistoryTab(accountManager: AccountManager, viewModel: HistoryViewModel, onCompactChange: (Boolean) -> Unit) {
    val colors = incColors()
    var selected by remember { mutableStateOf<SavedCalculationSummary?>(null) }
    val isSignedIn = accountManager.isSignedIn

    LaunchedEffect(isSignedIn) { viewModel.load(isSignedIn) }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        val current = selected
        if (current != null) {
            SessionDetailView(
                summary = current,
                viewModel = viewModel,
                onBack = { selected = null },
                onDelete = { viewModel.delete(current.id) { selected = null } },
            )
        } else {
            HistoryList(accountManager, viewModel, onCompactChange, onSelect = { selected = it })
        }
    }
}

@Composable
private fun HistoryList(
    accountManager: AccountManager,
    viewModel: HistoryViewModel,
    onCompactChange: (Boolean) -> Unit,
    onSelect: (SavedCalculationSummary) -> Unit,
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

    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 132.dp)) {
        AppSectionHeader<Unit>(title = "History")
        Text(
            if (accountManager.isSignedIn) "Your saved take-home projections, synced to your account." else "Keep a record of every projection you run.",
            style = IncType.body,
            color = colors.textDim,
            modifier = Modifier.padding(horizontal = 22.dp).padding(bottom = 14.dp),
        )
        when {
            !accountManager.isSignedIn -> SignedOutCard(accountManager)
            isLoading && sessions.isEmpty() -> Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.sage)
            }
            sessions.isEmpty() -> SignedInEmpty()
            else -> SessionsList(sessions, onSelect)
        }
        if (errorMessage != null) {
            Text(errorMessage.orEmpty(), style = IncType.secondary, color = colors.red, modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp))
        }
    }
}

@Composable
private fun SignedOutCard(accountManager: AccountManager) {
    val colors = incColors()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isSigningIn by accountManager.isSigningIn.collectAsStateWithLifecycle()
    val errorMessage by accountManager.errorMessage.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Box(modifier = Modifier.size(76.dp).clip(CircleShape).background(colors.sageBg), contentAlignment = Alignment.Center) {
            CalcCardIcon(CalcIcon.WALLET, colors.sage)
        }
        Spacer(Modifier.size(18.dp))
        Text(
            "Sign in to save and\nview past calculations",
            style = IncType.sheetTitle.copy(fontSize = 24.sp),
            color = colors.text,
        )
        Spacer(Modifier.size(10.dp))
        Text(
            "Every calculation you run is saved privately to your account and synced across devices.",
            style = IncType.secondary,
            color = colors.textDim,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.size(20.dp))
        IncButton(
            text = if (isSigningIn) "Signing in…" else "Continue with Google",
            onClick = { scope.launch { accountManager.signInWithGoogle(context) } },
            variant = IncButtonVariant.OUTLINED,
            enabled = !isSigningIn,
        )
        if (errorMessage != null) {
            Spacer(Modifier.size(10.dp))
            Text(errorMessage.orEmpty(), style = IncType.secondary, color = colors.red)
        }
    }
}

@Composable
private fun SignedInEmpty() {
    val colors = incColors()
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(colors.sageBg), contentAlignment = Alignment.Center) {
            CalcCardIcon(CalcIcon.CHART, colors.sage)
        }
        Spacer(Modifier.size(14.dp))
        Text("No saved calculations yet", style = IncType.sheetTitle, color = colors.text)
        Spacer(Modifier.size(6.dp))
        Text(
            "Run a projection from the Calculator tab and it will be saved here automatically.",
            style = IncType.body,
            color = colors.textDim,
            modifier = Modifier.padding(horizontal = 34.dp),
        )
    }
}

@Composable
private fun SessionsList(sessions: List<SavedCalculationSummary>, onSelect: (SavedCalculationSummary) -> Unit) {
    val colors = incColors()
    Column {
        Text(
            "${sessions.size} SAVED",
            style = IncType.overline,
            color = colors.textMute,
            modifier = Modifier.padding(horizontal = 22.dp).padding(bottom = 6.dp),
        )
        IncCard(modifier = Modifier.padding(horizontal = 16.dp), pad = 2.dp) {
            sessions.forEachIndexed { idx, summary ->
                SessionRow(summary = summary, showDivider = idx != sessions.lastIndex, onClick = { onSelect(summary) })
            }
        }
    }
}
