package com.makusha.incomatic.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncCardHeader
import com.makusha.incomatic.design.IncMoneyField
import com.makusha.incomatic.design.IncPicker
import com.makusha.incomatic.design.IncSwitch
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.FilingStatus

private fun filingStatusLabel(status: FilingStatus): String = when (status) {
    FilingStatus.SINGLE -> "Single or married filing separately"
    FilingStatus.MARRIED -> "Married filing jointly"
    FilingStatus.HEAD_OF_HOUSEHOLD -> "Head of household"
}

@Composable
fun AndFederal(form: CalculatorState, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    val colors = incColors()
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        IncCard {
            IncCardHeader(
                icon = { CalcCardIcon(CalcIcon.FLAG, colors.sage) },
                title = "Filing status",
                subtitle = "Drives the bracket table and standard deduction.",
            )
            IncPicker(
                label = "Status",
                value = form.filingStatus,
                options = FilingStatus.entries,
                labelOf = ::filingStatusLabel,
                onChange = { v -> update { it.copy(filingStatus = v) } },
            )
            Column(
                modifier = Modifier.drawBehind {
                    drawLine(colors.hairline, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                }.padding(top = 6.dp),
            ) {
                IncSwitch(
                    label = "Use pre-2020 W-4",
                    sub = "Withholding allowances instead of dependents and adjustments.",
                    checked = form.useOldW4,
                    onChange = { v -> update { it.copy(useOldW4 = v) } },
                )
            }
        }
        IncCard {
            IncCardHeader(
                icon = { CalcCardIcon(CalcIcon.SEED, colors.sage) },
                title = "Adjustments",
                subtitle = "Optional — W-4 steps 3 and 4.",
            )
            IncMoneyField(label = "Dependent credits", value = form.dependents, onChange = { v -> update { it.copy(dependents = v) } })
            IncMoneyField(label = "Other income", value = form.otherIncome, onChange = { v -> update { it.copy(otherIncome = v) } })
            IncMoneyField(label = "Extra withholding", value = form.extraWithholding, onChange = { v -> update { it.copy(extraWithholding = v) } }, suffix = "Per paycheck")
        }
    }
}
