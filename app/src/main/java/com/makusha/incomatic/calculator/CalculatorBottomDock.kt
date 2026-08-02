package com.makusha.incomatic.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.nav.AppPillNav
import com.makusha.incomatic.nav.MainTab

/**
 * Bottom chrome for the Calculator tab: the CTA button row and the
 * tab-switcher pill, stacked as one measured unit instead of two
 * independently-floating overlays each tuned with unrelated hardcoded
 * offsets (the old `AndStickyCTA`/`AppPillNav` pairing, which could
 * overlap scroll content that ran past either one's guessed clearance).
 * The call site measures this composable's real height via
 * `onGloballyPositioned` and feeds that back into the scroll content's
 * bottom padding, so there's no magic number to keep in sync.
 */
@Composable
fun CalculatorBottomDock(
    section: CalculatorSection,
    tab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AndStickyCTA(
            section = section,
            onNext = onNext,
            onSkip = onSkip,
            isLoading = isLoading,
        )
        AppPillNav(
            tab = tab,
            onTabSelected = onTabSelected,
            compact = false,
            onExpand = {},
        )
    }
}
