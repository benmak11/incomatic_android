package com.makusha.incomatic.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Slider for 401(k)/Roth percentages. Uses the stock M3 Slider (not a
 * pixel-exact port of the design's custom 20dp thumb) — the custom
 * thumb/track composable API has shifted across Compose Material3 releases,
 * not worth the fragility for a Phase 2 control. Sage-tinted via colors.
 */
@Composable
fun IncSlider(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    max: Int = 20,
    suffix: String = "%",
) {
    val colors = incColors()
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            IncOverline(label)
            Text("$value$suffix", style = IncType.title.copy(color = colors.sageDeep))
        }
        Spacer(Modifier.height(10.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = 0f..max.toFloat(),
            steps = (max - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = colors.sage,
                activeTrackColor = colors.sage,
                inactiveTrackColor = colors.track,
            ),
        )
    }
}
