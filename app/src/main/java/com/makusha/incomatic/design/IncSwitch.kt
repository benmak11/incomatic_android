package com.makusha.incomatic.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** M3 switch — 52x32 track, thumb grows 16->24dp when on. */
@Composable
fun IncSwitch(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    label: String,
    sub: String? = null,
) {
    val colors = incColors()
    val haptics = LocalHapticFeedback.current
    val trackColor by animateColorAsState(if (checked) colors.sage else colors.track, label = "switchTrack")
    val thumbColor by animateColorAsState(if (checked) androidx.compose.ui.graphics.Color.White else colors.textMute, label = "switchThumb")
    val thumbSize by animateDpAsState(if (checked) 24.dp else 16.dp, label = "switchThumbSize")

    IncSubtleRippleScope {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Switch) { newValue ->
                    haptics.performHapticFeedback(if (newValue) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                    onChange(newValue)
                }
                .padding(vertical = 10.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(label, style = IncType.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.text)
                if (sub != null) Text(sub, style = IncType.secondary, color = colors.textDim)
            }
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 32.dp)
                    .clip(CircleShape)
                    .background(trackColor)
                    .let { m -> if (!checked) m.border(2.dp, colors.hairlineStrong, CircleShape) else m },
                contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = if (checked) 4.dp else 6.dp)
                        .size(thumbSize)
                        .clip(CircleShape)
                        .background(thumbColor),
                )
            }
        }
    }
}
