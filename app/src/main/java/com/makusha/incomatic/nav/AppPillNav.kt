package com.makusha.incomatic.nav

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.design.IncRippleScope
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors

private fun DrawScope.tabIcon(tab: MainTab, color: Color) {
    val w = size.width
    val h = size.height
    val sw = w * 0.09f
    when (tab) {
        MainTab.Calculator -> {
            drawRoundRect(color, topLeft = Offset(w * 0.15f, h * 0.1f), size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f), style = Stroke(sw))
            drawLine(color, Offset(w * 0.3f, h * 0.32f), Offset(w * 0.7f, h * 0.32f), sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.3f, h * 0.5f), Offset(w * 0.45f, h * 0.5f), sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.3f, h * 0.68f), Offset(w * 0.45f, h * 0.68f), sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.62f, h * 0.48f), Offset(w * 0.62f, h * 0.7f), sw, cap = StrokeCap.Round)
        }
        MainTab.Insights -> {
            drawLine(color, Offset(w * 0.15f, h * 0.85f), Offset(w * 0.85f, h * 0.85f), sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.3f, h * 0.85f), Offset(w * 0.3f, h * 0.35f), sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.5f, h * 0.85f), Offset(w * 0.5f, h * 0.15f), sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.7f, h * 0.85f), Offset(w * 0.7f, h * 0.5f), sw, cap = StrokeCap.Round)
        }
        MainTab.History -> {
            drawCircle(color, radius = w * 0.38f, center = Offset(w / 2f, h / 2f), style = Stroke(sw))
            drawLine(color, Offset(w / 2f, h * 0.32f), Offset(w / 2f, h * 0.5f), sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w / 2f, h * 0.5f), Offset(w * 0.65f, h * 0.6f), sw, cap = StrokeCap.Round)
        }
    }
}

private val PillSpring = spring<Float>(dampingRatio = 0.85f)

@Composable
private fun CompactHandle(tab: MainTab, onExpand: () -> Unit) {
    val colors = incColors()
    val haptics = LocalHapticFeedback.current
    Box(modifier = Modifier.padding(start = 20.dp, bottom = 22.dp)) {
        IncRippleScope {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(6.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.16f), spotColor = Color.Black.copy(alpha = 0.16f))
                    .clip(CircleShape)
                    .background(colors.surface)
                    .border(1.dp, colors.hairline, CircleShape)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onExpand()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(20.dp)) { tabIcon(tab, colors.sageDeep) }
            }
        }
    }
}

@Composable
private fun ExpandedTrack(tab: MainTab, onTabSelected: (MainTab) -> Unit) {
    val colors = incColors()
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 22.dp)
            .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(14.dp))
            .background(colors.sageBg)
            .padding(4.dp),
    ) {
        IncRippleScope {
            MainTab.entries.forEach { t ->
                val on = t == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .let { m -> if (on) m.background(colors.surface) else m }
                        .clickable {
                            if (!on) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onTabSelected(t)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(15.dp)) { tabIcon(t, if (on) colors.sageDeep else colors.textDim) }
                        Spacer(Modifier.size(7.dp))
                        Text(t.label, style = IncType.secondary.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp), color = if (on) colors.sageDeep else colors.textDim)
                    }
                }
            }
        }
    }
}

/**
 * Floating pill nav. Expanded: sage-tinted track, white active chip.
 * Collapsed (scrolling down): a 48dp circular handle bottom-left showing
 * the active tab's icon — tap or scroll up to re-expand. The
 * expanded/collapsed swap animates with a spring (0.85 damping), the
 * Compose analog of the design doc's scroll-collapse spec.
 */
@Composable
fun AppPillNav(
    tab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    compact: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = compact,
        transitionSpec = {
            (fadeIn(PillSpring) + scaleIn(initialScale = 0.88f, animationSpec = PillSpring))
                .togetherWith(fadeOut(PillSpring) + scaleOut(targetScale = 0.88f, animationSpec = PillSpring))
        },
        modifier = modifier,
        label = "pillNav",
    ) { isCompact ->
        if (isCompact) CompactHandle(tab, onExpand) else ExpandedTrack(tab, onTabSelected)
    }
}
