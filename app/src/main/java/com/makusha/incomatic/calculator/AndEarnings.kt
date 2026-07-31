package com.makusha.incomatic.calculator

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncCardHeader
import com.makusha.incomatic.design.IncMoneyField
import com.makusha.incomatic.design.IncSegmented
import com.makusha.incomatic.design.IncPicker
import com.makusha.incomatic.design.IncSwitch
import com.makusha.incomatic.design.IncTextField
import com.makusha.incomatic.design.SegmentOption
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.equity.EquityCard
import com.makusha.incomatic.equity.EquityViewModel

@Composable
fun AndEarnings(
    form: CalculatorState,
    update: ((CalculatorState) -> CalculatorState) -> Unit,
    equity: EquityViewModel,
    signedIn: Boolean,
    onOpenGrants: () -> Unit,
    onShowAccount: () -> Unit,
) {
    val colors = incColors()
    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        IncCard {
            IncCardHeader(
                icon = { CalcCardIcon(CalcIcon.WALLET, colors.sage) },
                title = "How you're paid",
                subtitle = "Sets the period every figure is quoted in.",
            )
            IncPicker(
                label = "Pay frequency",
                value = form.payFrequency,
                options = PayFrequencyOption.entries,
                labelOf = { it.label },
                onChange = { v -> update { it.copy(payFrequency = v) } },
            )
            IncSegmented(
                label = "Income type",
                value = form.incomeType,
                options = listOf(
                    SegmentOption(IncomeType.SALARY, "Salary"),
                    SegmentOption(IncomeType.HOURLY, "Hourly"),
                ),
                onChange = { v -> update { it.copy(incomeType = v) } },
            )
            if (form.incomeType == IncomeType.SALARY) {
                IncMoneyField(label = "Base salary", value = form.salary, onChange = { v -> update { it.copy(salary = v) } }, suffix = "Per year")
            } else {
                IncMoneyField(label = "Hourly rate", value = form.hourlyRate, onChange = { v -> update { it.copy(hourlyRate = v) } })
                IncMoneyField(label = "Regular hours / period", value = form.regularHours, onChange = { v -> update { it.copy(regularHours = v) } })
                IncMoneyField(label = "Overtime hours / period", value = form.overtimeHours, onChange = { v -> update { it.copy(overtimeHours = v) } })
            }
        }
        IncCard {
            IncCardHeader(
                icon = { CalcCardIcon(CalcIcon.CHART, colors.sage) },
                title = "Extras",
                subtitle = "Bonus and commission are withheld at the supplemental rate.",
            )
            IncMoneyField(label = "Annual bonus", value = form.bonus, onChange = { v -> update { it.copy(bonus = v) } })
            IncTextField(
                label = "Expected date (YYYY-MM-DD, optional)",
                value = form.bonusDate,
                onChange = { v -> update { it.copy(bonusDate = v) } },
                placeholder = "This year",
                keyboardType = KeyboardType.Number,
            )
            IncSwitch(
                label = "Repeats yearly",
                sub = "Same amount every year from the start date onward.",
                checked = form.bonusRecurring,
                onChange = { v -> update { it.copy(bonusRecurring = v) } },
            )
            IncMoneyField(label = "Commission (annual)", value = form.commission, onChange = { v -> update { it.copy(commission = v) } })
        }
        EquityCard(form = form, update = update, equity = equity, signedIn = signedIn, onOpenGrants = onOpenGrants, onShowAccount = onShowAccount)
    }
}
