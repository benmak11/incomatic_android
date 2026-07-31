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
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.nav.AccountGlyph
import com.makusha.incomatic.nav.AppPillNav
import com.makusha.incomatic.nav.AppSectionHeader
import com.makusha.incomatic.nav.MainTab
import kotlin.math.abs

/**
 * Tabs are plain state, not a NavHost — matching iOS and the design doc's
 * explicit choice. Each tab's content is a placeholder for this pass (real
 * Calculator/Insights/History screens land in later phases); the point here
 * is proving the chrome (tokens, cards, pill nav, scroll-collapse, system
 * back) against the design-canvas screenshots.
 */
@Composable
fun IncomaticShell() {
    var tab by remember { mutableStateOf(MainTab.Calculator) }
    var compact by remember { mutableStateOf(false) }
    val colors = incColors()

    BackHandler(enabled = tab != MainTab.Calculator) {
        tab = MainTab.Calculator
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        when (tab) {
            MainTab.Calculator -> PlaceholderScreen(
                title = "Calculator",
                tagline = "Earnings · Federal · State · Benefits behind the hairline tabs — lands in Phase 2.",
                onCompactChange = { compact = it },
            )
            MainTab.Insights -> PlaceholderScreen(
                title = "Insights",
                tagline = "Donut breakdown + yearly outlook — lands in Phase 2.",
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
}

/**
 * Scroll-driven pill-nav collapse: scrolling down past a 40dp threshold
 * collapses the pill; scrolling up (or being back near the top) re-expands
 * it. Mirrors the design doc's `dy > 0 && scrollTop > 40` rule using
 * [rememberScrollState]'s absolute offset instead of a raw DOM scrollTop.
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
