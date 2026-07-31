package com.makusha.incomatic.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.calculator.CalculatorState
import com.makusha.incomatic.calculator.IncomeType
import com.makusha.incomatic.calculator.PayFrequencyOption
import com.makusha.incomatic.calculator.filingStatusLabel
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncMoneyField
import com.makusha.incomatic.design.IncPicker
import com.makusha.incomatic.design.IncSegmented
import com.makusha.incomatic.design.IncSlider
import com.makusha.incomatic.design.IncSubtleRippleScope
import com.makusha.incomatic.design.IncTextField
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.SegmentOption
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.FilingStatus
import com.makusha.incomatic.net.dto.UsState

/**
 * One composable per [OnbStep] kind, each wrapped in a bare [IncCard] (no
 * header — android-onboarding.jsx's `wrap()` helper doesn't add one either,
 * unlike the Calculator's section cards).
 */
@Composable
fun OnbWageBody(form: CalculatorState, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    IncCard {
        IncSegmented(
            value = form.incomeType,
            options = listOf(SegmentOption(IncomeType.SALARY, "Salary"), SegmentOption(IncomeType.HOURLY, "Hourly")),
            onChange = { v -> update { it.copy(incomeType = v) } },
        )
        if (form.incomeType == IncomeType.SALARY) {
            IncMoneyField(label = "Annual gross", value = form.salary, onChange = { v -> update { it.copy(salary = v) } })
        } else {
            IncMoneyField(label = "Hourly rate", value = form.hourlyRate, onChange = { v -> update { it.copy(hourlyRate = v) } })
        }
    }
}

@Composable
fun OnbPayFrequencyBody(form: CalculatorState, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    IncCard {
        Column {
            PayFrequencyOption.entries.forEach { opt ->
                OnbRadioRow(label = opt.label, selected = form.payFrequency == opt) {
                    update { it.copy(payFrequency = opt) }
                }
            }
        }
    }
}

@Composable
fun OnbFilingStatusBody(form: CalculatorState, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    IncCard {
        Column {
            FilingStatus.entries.forEach { opt ->
                OnbRadioRow(label = filingStatusLabel(opt), selected = form.filingStatus == opt) {
                    update { it.copy(filingStatus = opt) }
                }
            }
        }
    }
}

/**
 * Shared body for the Bonus/Commission steps. `wants` is local onboarding-only
 * UI state (not persisted on [CalculatorState] — matches useOnboardingSteps
 * treating it as a branch flag, not form data). Answering "No" clears the
 * backing field(s) so a changed mind doesn't leave stale amounts behind.
 */
@Composable
fun OnbYesNoAmountBody(
    wants: Boolean?,
    onWantsChange: (Boolean) -> Unit,
    amountLabel: String,
    amountValue: String,
    onAmountChange: (String) -> Unit,
    dateValue: String? = null,
    onDateChange: ((String) -> Unit)? = null,
) {
    IncCard {
        IncSegmented(
            value = when (wants) { true -> "yes"; false -> "no"; null -> "" },
            options = listOf(SegmentOption("yes", "Yes"), SegmentOption("no", "No")),
            onChange = { v -> onWantsChange(v == "yes") },
        )
        if (wants == true) {
            IncMoneyField(label = amountLabel, value = amountValue, onChange = onAmountChange)
            if (dateValue != null && onDateChange != null) {
                IncTextField(
                    label = "Expected date (YYYY-MM-DD, optional)",
                    value = dateValue,
                    onChange = onDateChange,
                    placeholder = "This year",
                    keyboardType = KeyboardType.Number,
                )
            }
        }
    }
}

@Composable
fun OnbStatePickerBody(form: CalculatorState, usStates: List<UsState>, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    val colors = incColors()
    IncCard {
        if (usStates.isNotEmpty()) {
            IncPicker(
                label = "Work state",
                value = usStates.find { it.code == form.stateCode } ?: usStates.first(),
                options = usStates,
                labelOf = { it.name },
                onChange = { v -> update { it.copy(stateCode = v.code) } },
            )
        } else {
            Text("Loading states…", style = IncType.secondary, color = colors.textDim)
        }
    }
}

@Composable
fun OnbBenefitsBody(form: CalculatorState, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    IncCard {
        IncMoneyField(label = "Medical", value = form.medical, onChange = { v -> update { it.copy(medical = v) } })
        IncMoneyField(label = "Dental", value = form.dental, onChange = { v -> update { it.copy(dental = v) } })
        IncMoneyField(label = "Vision", value = form.vision, onChange = { v -> update { it.copy(vision = v) } })
        IncMoneyField(label = "Healthcare FSA", value = form.fsa, onChange = { v -> update { it.copy(fsa = v) } })
    }
}

@Composable
fun OnbRetirementBody(form: CalculatorState, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    IncCard {
        IncSlider(label = "Traditional 401(k)", value = form.t401k, onChange = { v -> update { it.copy(t401k = v) } })
        IncSlider(label = "Roth 401(k)", value = form.roth, onChange = { v -> update { it.copy(roth = v) } })
    }
}

/** Radio-style tappable row shared by the payFreq/filing "choice" steps. */
@Composable
fun OnbRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = incColors()
    val shape: Shape = RoundedCornerShape(12.dp)
    IncSubtleRippleScope {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(onClick = onClick)
                .let { m -> if (selected) m.background(colors.sageBg) else m }
                .padding(vertical = 13.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .border(2.dp, if (selected) colors.sage else colors.hairlineStrong, RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(999.dp)).background(colors.sage))
                }
            }
            Text(
                label,
                style = IncType.body.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                color = if (selected) colors.sageDeep else colors.text,
            )
        }
    }
}

@Composable
fun OnbOptionalHint() {
    val colors = incColors()
    Text(
        "Optional — skip if you're not sure.",
        style = IncType.secondary.copy(fontSize = 12.sp),
        color = colors.textMute,
        modifier = Modifier.padding(horizontal = 22.dp),
    )
}
