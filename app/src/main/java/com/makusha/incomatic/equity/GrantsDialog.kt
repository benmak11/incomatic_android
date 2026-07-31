package com.makusha.incomatic.equity

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.calculator.CalcCardIcon
import com.makusha.incomatic.calculator.CalcIcon
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncSubtleRippleScope
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.util.formatMoney
import com.makusha.incomatic.util.formatShares
import java.time.format.DateTimeFormatter
import java.util.Locale

private sealed class EquityScreen {
    object List : EquityScreen()
    data class Form(val existing: RsuGrant?) : EquityScreen()
    data class Detail(val grant: RsuGrant) : EquityScreen()
}

/**
 * Fullscreen grants flow (List / Form / Detail) — the app has no NavHost
 * anywhere (see shell/IncomaticShell.kt's own doc comment), so this owns a
 * tiny local step state instead, same "plain state" philosophy as the shell
 * and onboarding. [BackHandler] pops one level instead of dismissing.
 */
@Composable
fun GrantsDialog(equity: EquityViewModel, onDismiss: () -> Unit) {
    var screen by remember { mutableStateOf<EquityScreen>(EquityScreen.List) }
    val grants by equity.grants.collectAsStateWithLifecycle()
    val colors = incColors()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BackHandler {
            screen = when (val s = screen) {
                is EquityScreen.List -> { onDismiss(); EquityScreen.List }
                is EquityScreen.Form -> s.existing?.let { EquityScreen.Detail(it) } ?: EquityScreen.List
                is EquityScreen.Detail -> EquityScreen.List
            }
        }
        Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            when (val s = screen) {
                is EquityScreen.List -> {
                    EquityTopBar(title = "RSU grants", leadingLabel = "Done", onLeading = onDismiss) {
                        Text(
                            "+",
                            style = IncType.title.copy(fontSize = 20.sp),
                            color = colors.sage,
                            modifier = Modifier.clickable { screen = EquityScreen.Form(null) },
                        )
                    }
                    GrantsList(
                        grants = grants,
                        onSelect = { g -> screen = EquityScreen.Detail(g) },
                        onAdd = { screen = EquityScreen.Form(null) },
                    )
                }
                is EquityScreen.Form -> {
                    EquityTopBar(
                        title = if (s.existing == null) "Add grant" else "Edit grant",
                        leadingLabel = "Cancel",
                        onLeading = { screen = s.existing?.let { EquityScreen.Detail(it) } ?: EquityScreen.List },
                    )
                    GrantForm(
                        existing = s.existing,
                        onSave = { saved -> equity.save(saved); screen = EquityScreen.Detail(saved) },
                    )
                }
                is EquityScreen.Detail -> {
                    val current = grants.find { it.id == s.grant.id } ?: s.grant
                    EquityTopBar(title = current.company ?: current.ticker ?: "Grant", leadingLabel = "Back", onLeading = { screen = EquityScreen.List }) {
                        Text(
                            "Edit",
                            style = IncType.secondary.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.sage,
                            modifier = Modifier.clickable { screen = EquityScreen.Form(current) },
                        )
                    }
                    GrantDetail(
                        grant = current,
                        onDelete = { current.id?.let(equity::delete); screen = EquityScreen.List },
                    )
                }
            }
        }
    }
}

@Composable
private fun EquityTopBar(title: String, leadingLabel: String, onLeading: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    val colors = incColors()
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(
            leadingLabel,
            style = IncType.secondary.copy(fontWeight = FontWeight.SemiBold),
            color = colors.text,
            modifier = Modifier.align(Alignment.CenterStart).clickable(onClick = onLeading),
        )
        Text(title, style = IncType.title, color = colors.text, modifier = Modifier.align(Alignment.Center))
        if (trailing != null) Box(modifier = Modifier.align(Alignment.CenterEnd)) { trailing() }
    }
}

@Composable
private fun GrantsList(grants: List<RsuGrant>, onSelect: (RsuGrant) -> Unit, onAdd: () -> Unit) {
    val colors = incColors()
    if (grants.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(colors.sageBg),
                contentAlignment = Alignment.Center,
            ) {
                CalcCardIcon(CalcIcon.CHART, colors.sage, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(14.dp))
            Text("No grants yet", style = IncType.sheetTitle.copy(fontSize = 17.sp), color = colors.text)
            Spacer(Modifier.size(6.dp))
            Text(
                "Add an RSU grant to see how shares vest and what lands in this year's paycheck.",
                style = IncType.secondary,
                color = colors.textDim,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.size(16.dp))
            IncButton(text = "+ Add grant", onClick = onAdd, fullWidth = false)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
        grants.forEach { grant -> GrantRow(grant, onClick = { onSelect(grant) }) }
        Text(
            "Estimates value all ${AppConfig.TAX_YEAR} vests at today's price. Actual tax withholding happens at each vest at that day's price.",
            style = IncType.secondary.copy(fontSize = 11.5.sp),
            color = colors.textDim,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
    }
}

private val NEXT_VEST_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

@Composable
private fun GrantRow(grant: RsuGrant, onClick: () -> Unit) {
    val colors = incColors()
    val vestingThisYear = VestMath.value(AppConfig.TAX_YEAR, listOf(grant))
    val next = VestMath.nextVest(grant)

    IncSubtleRippleScope {
        Column(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(grant.ticker ?: grant.company ?: "—", style = IncType.body.copy(fontWeight = FontWeight.Bold), color = colors.text)
                Spacer(Modifier.size(8.dp))
                Text("${formatShares(grant.sharesTotal)} sh", style = IncType.secondary.copy(fontWeight = FontWeight.SemiBold), color = colors.textDim, modifier = Modifier.weight(1f))
            }
            Text(grant.schedule.label(), style = IncType.secondary.copy(fontSize = 12.sp), color = colors.textMute)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Vesting in ${AppConfig.TAX_YEAR}: ${formatMoney(vestingThisYear, cents = false)}",
                    style = IncType.secondary.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.sage,
                )
                if (next != null) {
                    Text("Next vest ${next.date.format(NEXT_VEST_FORMATTER)}", style = IncType.secondary.copy(fontSize = 11.5.sp), color = colors.textMute)
                }
            }
        }
    }
}
