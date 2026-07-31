package com.makusha.incomatic.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncCardHeader
import com.makusha.incomatic.design.IncPicker
import com.makusha.incomatic.design.IncSwitch
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.UsState

@Composable
fun AndState(form: CalculatorState, usStates: List<UsState>, update: ((CalculatorState) -> CalculatorState) -> Unit) {
    val colors = incColors()
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        IncCard {
            IncCardHeader(
                icon = { CalcCardIcon(CalcIcon.PIN, colors.sage) },
                title = "Where you work",
                subtitle = "State income tax, plus county where it applies.",
            )
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
            Column(
                modifier = Modifier.drawBehind {
                    drawLine(colors.hairline, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                }.padding(top = 6.dp),
            ) {
                IncSwitch(
                    label = "I live in a different state",
                    sub = "Reciprocity and non-resident rules may apply.",
                    checked = form.livesElsewhere,
                    onChange = { v -> update { it.copy(livesElsewhere = v) } },
                )
            }
        }
        Text(
            if (form.stateCode == "CA") "California has no local income tax on wages." else "Local income tax is applied where the state levies one.",
            style = IncType.secondary,
            color = colors.textMute,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
    }
}
