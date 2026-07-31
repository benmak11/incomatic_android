package com.makusha.incomatic.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncCardHeader
import com.makusha.incomatic.design.IncMoneyField
import com.makusha.incomatic.design.IncSlider
import com.makusha.incomatic.design.incColors

@Composable
fun AndBenefits(form: CalculatorState, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    val colors = incColors()
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        IncCard {
            IncCardHeader(
                icon = { CalcCardIcon(CalcIcon.HEART, colors.sage) },
                title = "Pre-tax benefits",
                subtitle = "Per paycheck. Lowers taxable income.",
            )
            IncMoneyField(label = "Medical", value = form.medical, onChange = { v -> update { it.copy(medical = v) } })
            IncMoneyField(label = "Dental", value = form.dental, onChange = { v -> update { it.copy(dental = v) } })
            IncMoneyField(label = "Vision", value = form.vision, onChange = { v -> update { it.copy(vision = v) } })
            IncMoneyField(label = "FSA / HSA", value = form.fsa, onChange = { v -> update { it.copy(fsa = v) } })
        }
        IncCard {
            IncCardHeader(
                icon = { CalcCardIcon(CalcIcon.SEED, colors.sage) },
                title = "Retirement",
                subtitle = "Traditional lowers taxable income now; Roth doesn't.",
            )
            IncSlider(label = "Traditional 401(k)", value = form.t401k, onChange = { v -> update { it.copy(t401k = v) } })
            IncSlider(label = "Roth 401(k)", value = form.roth, onChange = { v -> update { it.copy(roth = v) } })
        }
    }
}
