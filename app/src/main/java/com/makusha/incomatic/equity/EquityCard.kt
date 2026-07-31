package com.makusha.incomatic.equity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.calculator.CalcCardIcon
import com.makusha.incomatic.calculator.CalcIcon
import com.makusha.incomatic.calculator.CalculatorState
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncCardHeader
import com.makusha.incomatic.design.IncMoneyField
import com.makusha.incomatic.design.IncSubtleRippleScope
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.util.formatMoney

/**
 * "Equity / RSUs" card in the Earnings section — port of iOS's
 * EquityCardView, now with the same three states: signed out (plain
 * annual value field + sign-in hint), signed in without grants (add CTA),
 * signed in with grants (vesting-this-year summary). An explicit manual
 * override always wins over the grant-derived total, same as iOS.
 */
@Composable
fun EquityCard(
    form: CalculatorState,
    update: ((CalculatorState) -> CalculatorState) -> Unit,
    equity: EquityViewModel,
    signedIn: Boolean,
    onOpenGrants: () -> Unit,
    onShowAccount: () -> Unit,
) {
    val colors = incColors()
    val grants by equity.grants.collectAsStateWithLifecycle()
    val override = form.rsuOverride.toDoubleOrNull() ?: 0.0
    var showOverrideField by remember { mutableStateOf(false) }

    IncCard {
        IncCardHeader(
            icon = { CalcCardIcon(CalcIcon.CHART, colors.sage) },
            title = "Equity / RSUs",
            subtitle = "Vests count as supplemental income",
        )
        if (!signedIn) {
            SignedOutBody(form = form, update = update, onShowAccount = onShowAccount)
            return@IncCard
        }
        if (grants.isEmpty()) {
            AddGrantsRow(onClick = onOpenGrants)
        } else {
            VestingSummaryRow(equity = equity, grants = grants, dimmed = override > 0, onClick = onOpenGrants)
        }
        if (showOverrideField || override > 0) {
            Spacer(Modifier.size(10.dp))
            IncMoneyField(
                label = "Override amount (annual)",
                value = form.rsuOverride,
                onChange = { v -> update { it.copy(rsuOverride = v) } },
            )
            if (override > 0) {
                Text(
                    "Using your override, clear it to value grants automatically.",
                    style = IncType.secondary.copy(fontSize = 12.sp),
                    color = colors.textDim,
                )
            }
        } else {
            Text(
                if (grants.isEmpty()) "or enter an amount directly" else "Override amount",
                style = IncType.secondary.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                color = colors.sage,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { showOverrideField = true },
            )
        }
    }
}

@Composable
private fun SignedOutBody(
    form: CalculatorState,
    update: ((CalculatorState) -> CalculatorState) -> Unit,
    onShowAccount: () -> Unit,
) {
    val colors = incColors()
    Column {
        IncMoneyField(
            label = "RSU value vesting this year (annual)",
            value = form.rsuOverride,
            onChange = { v -> update { it.copy(rsuOverride = v) } },
        )
        Row(
            modifier = Modifier.clickable(onClick = onShowAccount),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "Sign in to model grants and vesting schedules",
                style = IncType.secondary.copy(fontSize = 12.sp),
                color = colors.textDim,
            )
        }
    }
}

@Composable
private fun AddGrantsRow(onClick: () -> Unit) {
    val colors = incColors()
    IncSubtleRippleScope {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(colors.sageBg),
                contentAlignment = Alignment.Center,
            ) {
                Text("+", style = IncType.title.copy(fontSize = 18.sp), color = colors.sage)
            }
            Text("Add your RSU grants", style = IncType.body.copy(fontWeight = FontWeight.SemiBold), color = colors.text, modifier = Modifier.weight(1f))
            Chevron()
        }
    }
}

@Composable
private fun VestingSummaryRow(equity: EquityViewModel, grants: List<com.makusha.incomatic.net.dto.RsuGrant>, dimmed: Boolean, onClick: () -> Unit) {
    val colors = incColors()
    IncSubtleRippleScope {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "RSUs vesting in ${AppConfig.TAX_YEAR} · ~${formatMoney(equity.vestingThisYear, cents = false)}",
                    style = IncType.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = if (dimmed) colors.textMute else colors.text,
                )
                Text(
                    "${grants.size} ${if (grants.size == 1) "grant" else "grants"} · ${equity.tickerSummary}",
                    style = IncType.secondary.copy(fontSize = 12.5.sp),
                    color = colors.textDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Chevron()
        }
    }
}

/** Decorative — sits inside an already-clickable row whose own text is the accessible label. */
@Composable
private fun Chevron() {
    val colors = incColors()
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(13.dp).semantics { hideFromAccessibility() },
    ) {
        val w = size.width
        val h = size.height
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.3f, h * 0.15f)
            lineTo(w * 0.7f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.85f)
        }
        drawPath(
            p,
            colors.textMute,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                w * 0.16f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}
