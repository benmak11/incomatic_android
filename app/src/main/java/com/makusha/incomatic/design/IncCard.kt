package com.makusha.incomatic.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CardShape = RoundedCornerShape(22.dp)

/**
 * Explicit low-alpha shadow pair instead of stock M3 elevation — elevation
 * glows in dark mode; this pair plus [IncColors.cardBorder] gives the card
 * definition without it.
 */
private fun Modifier.incCardShadow(): Modifier = this
    .shadow(elevation = 22.dp, shape = CardShape, ambientColor = Color.Black.copy(alpha = 0.04f), spotColor = Color.Black.copy(alpha = 0.04f))
    .shadow(elevation = 2.dp, shape = CardShape, ambientColor = Color.Black.copy(alpha = 0.02f), spotColor = Color.Black.copy(alpha = 0.02f))

@Composable
fun IncCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    pad: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = incColors()
    val base = modifier
        .fillMaxWidth()
        .padding(bottom = 14.dp)
        .incCardShadow()
        .clip(CardShape)
        .background(colors.surface)
        .border(1.dp, colors.cardBorder, CardShape)

    if (onClick != null) {
        IncSubtleRippleScope {
            Box(base.clickable(onClick = onClick).padding(pad)) {
                Column(content = content)
            }
        }
    } else {
        Box(base.padding(pad)) {
            Column(content = content)
        }
    }
}

@Composable
fun IncCardHeader(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = incColors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(colors.sageBg),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = IncType.title, color = colors.text)
            if (subtitle != null) {
                Text(subtitle, style = IncType.secondary, color = colors.textDim, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun IncOverline(text: String, color: Color? = null) {
    val colors = incColors()
    Text(text.uppercase(), style = IncType.overline, color = color ?: colors.textMute)
}

@Composable
fun IncLabel(text: String, suffix: String? = null) {
    val colors = incColors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        IncOverline(text)
        if (suffix != null) Text(suffix, style = IncType.overline.copy(color = colors.sage))
    }
}
