package com.makusha.incomatic.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class IncButtonVariant { FILLED, SOLID, OUTLINED, TEXT, DANGER }

@Composable
fun IncButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: IncButtonVariant = IncButtonVariant.FILLED,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
) {
    val colors = incColors()
    val shape = RoundedCornerShape(14.dp)
    val haptics = LocalHapticFeedback.current
    val (bg, fg, borderColor) = when (variant) {
        IncButtonVariant.FILLED -> Triple(if (!enabled) colors.disabled else colors.sage, Color.White, null)
        IncButtonVariant.SOLID -> Triple(colors.btnSolid, colors.btnSolidText, null)
        IncButtonVariant.OUTLINED -> Triple(Color.Transparent, colors.text, colors.hairlineStrong)
        IncButtonVariant.TEXT -> Triple(Color.Transparent, colors.sageDeep, null)
        IncButtonVariant.DANGER -> Triple(colors.redBg, colors.red, null)
    }

    IncRippleScope {
        Box(
            modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
                .defaultMinSize(minHeight = 48.dp)
                .clip(shape)
                .background(bg)
                .let { m -> if (borderColor != null) m.border(1.dp, borderColor, shape) else m }
                .let { m ->
                    if (enabled) {
                        m.clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onClick()
                        }
                    } else m
                }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = IncType.secondary.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold), color = fg)
        }
    }
}
