package com.makusha.incomatic.equity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.util.formatMoney
import com.makusha.incomatic.util.formatShares

/**
 * Year-level vest distribution for one grant — direct port of iOS's
 * VestTimelineView. A 4-year monthly schedule is 37 events, so rows roll up
 * per year ("2026 · 250 sh ≈ $58,035 (4 vests)"). The current tax year is
 * highlighted in sage with a text caption (the label, not the color, is the
 * accessible channel); a cliff year gets a gold marker.
 */
@Composable
fun VestTimeline(grant: RsuGrant) {
    val colors = incColors()
    val groups = VestMath.yearGroups(grant)

    if (groups.isEmpty()) {
        Text(
            "Set shares, price, and a grant date to preview vesting.",
            style = IncType.secondary,
            color = colors.textMute,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        return
    }

    Column {
        groups.forEach { group ->
            val isCurrent = group.year == AppConfig.TAX_YEAR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .let { m -> if (isCurrent) m.background(colors.sageBg) else m }
                    .padding(horizontal = if (isCurrent) 10.dp else 0.dp, vertical = 7.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val dotSize = if (group.hasCliff) 12.dp else 8.dp
                    val dotColor = if (isCurrent) colors.sage else if (group.hasCliff) colors.gold else colors.hairlineStrong
                    Box(modifier = Modifier.size(dotSize).clip(CircleShape).background(dotColor))
                    Text(
                        group.year.toString(),
                        style = IncType.body.copy(fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold),
                        color = colors.text,
                    )
                    Text(
                        "${formatShares(group.shares)} sh ≈ ${formatMoney(group.value, cents = false)}",
                        style = IncType.secondary.copy(fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal),
                        color = if (isCurrent) colors.text else colors.textDim,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "(${group.vestCount} ${if (group.vestCount == 1) "vest" else "vests"})",
                        style = IncType.secondary.copy(fontSize = 11.sp),
                        color = colors.textMute,
                    )
                }
                if (isCurrent) {
                    Text(
                        "Counts toward your ${AppConfig.TAX_YEAR} paycheck",
                        style = IncType.secondary.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.sage,
                        modifier = Modifier.padding(start = 18.dp),
                    )
                }
            }
        }
    }
}
