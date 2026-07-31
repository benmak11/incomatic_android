package com.makusha.incomatic.equity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.calculator.CalcCardIcon
import com.makusha.incomatic.calculator.CalcIcon
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncCardHeader
import com.makusha.incomatic.design.IncMoneyField
import com.makusha.incomatic.design.IncPicker
import com.makusha.incomatic.design.IncTextField
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.util.formatShares

/**
 * Add/edit grant form — trimmed from iOS's GrantFormView: no ticker
 * search/quote (that endpoint 401s without auth), so company/ticker is
 * always a free-text field and price is always entered manually.
 */
@Composable
fun GrantForm(existing: RsuGrant?, onSave: (RsuGrant) -> Unit) {
    val colors = incColors()
    var companyText by remember { mutableStateOf(existing?.company ?: existing?.ticker ?: "") }
    var priceText by remember { mutableStateOf(existing?.pricePerShare?.toString() ?: "") }
    var sharesText by remember { mutableStateOf(existing?.sharesTotal?.let { formatShares(it) } ?: "") }
    var dateText by remember { mutableStateOf(existing?.grantDate ?: "") }
    var preset by remember {
        mutableStateOf(
            VestingPreset.fromPresetId(existing?.schedule?.presetId)
                ?: if (existing != null) VestingPreset.CUSTOM else VestingPreset.MONTHLY1CLIFF,
        )
    }
    var customTotal by remember { mutableStateOf((existing?.schedule?.totalMonths ?: 48).toString()) }
    var customCliff by remember { mutableStateOf((existing?.schedule?.cliffMonths ?: 12).toString()) }
    var customFreq by remember { mutableStateOf((existing?.schedule?.freqMonths ?: 1).toString()) }

    val schedule = preset.terms?.let { (total, cliff, freq) ->
        RsuGrant.VestingSchedule(preset.presetId, total, cliff, freq)
    } ?: run {
        val total = customTotal.toIntOrNull() ?: 48
        RsuGrant.VestingSchedule(VestingPreset.CUSTOM.presetId, total, (customCliff.toIntOrNull() ?: 12).coerceAtMost(total), customFreq.toIntOrNull() ?: 1)
    }

    val shares = sharesText.toDoubleOrNull()
    val price = priceText.toDoubleOrNull()
    val draft: RsuGrant? = if (shares != null && shares > 0 && price != null && price > 0 && dateText.isNotBlank() && companyText.isNotBlank()) {
        RsuGrant(
            id = existing?.id,
            ticker = companyText,
            company = companyText,
            manualPrice = true,
            sharesTotal = shares,
            pricePerShare = price,
            grantDate = dateText,
            schedule = schedule,
        )
    } else null

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
            IncCard {
                IncCardHeader(icon = { CalcCardIcon(CalcIcon.FLAG, colors.sage) }, title = "Company")
                IncTextField(label = "Company or ticker", value = companyText, onChange = { companyText = it }, placeholder = "AAPL, Acme Corp…")
            }
            IncCard {
                IncCardHeader(icon = { CalcCardIcon(CalcIcon.WALLET, colors.sage) }, title = "Grant terms")
                IncMoneyField(label = "Price per share", value = priceText, onChange = { priceText = it })
                IncTextField(label = "Total shares", value = sharesText, onChange = { sharesText = it }, placeholder = "400", keyboardType = KeyboardType.Decimal)
                IncTextField(label = "Grant date (YYYY-MM-DD)", value = dateText, onChange = { dateText = it }, placeholder = "2025-03-15", keyboardType = KeyboardType.Number)
            }
            IncCard {
                IncCardHeader(icon = { CalcCardIcon(CalcIcon.SEED, colors.sage) }, title = "Vesting schedule")
                IncPicker(label = "Preset", value = preset, options = VestingPreset.entries, labelOf = { it.displayName }, onChange = { preset = it })
                if (preset == VestingPreset.CUSTOM) {
                    IncTextField(label = "Duration (months)", value = customTotal, onChange = { customTotal = it }, keyboardType = KeyboardType.Number)
                    IncTextField(label = "Cliff (months)", value = customCliff, onChange = { customCliff = it }, keyboardType = KeyboardType.Number)
                    IncTextField(label = "Vest every (months)", value = customFreq, onChange = { customFreq = it }, keyboardType = KeyboardType.Number)
                }
            }
            IncCard {
                IncCardHeader(icon = { CalcCardIcon(CalcIcon.CHART, colors.sage) }, title = "Vest distribution")
                if (draft != null) {
                    VestTimeline(draft)
                } else {
                    Text(
                        "Set shares, price, and a grant date to preview vesting.",
                        style = IncType.secondary,
                        color = colors.textMute,
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            IncButton(
                text = if (existing == null) "Save grant" else "Save changes",
                onClick = { draft?.let(onSave) },
                enabled = draft != null,
            )
        }
    }
}
