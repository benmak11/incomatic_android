package com.makusha.incomatic.nav

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncRippleScope
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors

/**
 * 38dp overlay, top-right of the shell. Visual-only in this pass — real
 * sign-in state comes with the Account/History phase.
 */
@Composable
fun AccountGlyph(
    signedIn: Boolean,
    initials: String = "BM",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = incColors()
    IncRippleScope {
        Box(
            modifier = modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (signedIn) colors.sage else colors.surface)
                .let { m -> if (signedIn) m else m.border(1.dp, colors.hairlineStrong, CircleShape) }
                .semantics { contentDescription = if (signedIn) "Account, signed in as $initials" else "Account, signed out" }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (signedIn) {
                Text(initials, style = IncType.secondary.copy(fontWeight = FontWeight.Bold), color = colors.btnSolidText)
            } else {
                val strokeColor = colors.textDim
                Canvas(modifier = Modifier.size(18.dp)) {
                    val strokeWidth = 1.9.dp.toPx()
                    drawCircle(strokeColor, radius = size.width * 0.19f, center = Offset(size.width / 2f, size.height * 0.32f), style = Stroke(strokeWidth))
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.92f)
                        cubicTo(
                            size.width * 0.35f, size.height * 0.6f,
                            size.width * 0.65f, size.height * 0.6f,
                            size.width * 0.8f, size.height * 0.92f,
                        )
                    }
                    drawPath(path, strokeColor, style = Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
            }
        }
    }
}
