package com.makusha.incomatic.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.design.IncSerifFamily
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors

/** Ported from android-onboarding.jsx's AndOnbTopBar — logo mark + wordmark, no back/close affordance. */
@Composable
fun AndOnbTopBar() {
    val colors = incColors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(999.dp)).background(colors.sage),
            contentAlignment = Alignment.Center,
        ) {
            Text("i", color = Color.White, fontFamily = IncSerifFamily, fontStyle = FontStyle.Italic, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
        Text("incomatic", style = IncType.title.copy(fontSize = 15.sp, letterSpacing = (-0.2).sp), color = colors.text)
    }
}

/** Ported from AndOnbRail — thin progress bar, (index+1)/total. */
@Composable
fun AndOnbRail(index: Int, total: Int) {
    val colors = incColors()
    val progress = ((index + 1).toFloat() / total).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 20.dp)
            .height(2.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.hairline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(colors.sage),
        )
    }
}

/** Ported from AndOnbBubble — sage speech bubble carrying the step's question. */
@Composable
fun AndOnbBubble(text: String) {
    val colors = incColors()
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 0.dp).padding(bottom = 18.dp)) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp))
                .background(colors.sageBg)
                .border(1.dp, colors.sageSoft, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp))
                .padding(PaddingValues(horizontal = 18.dp, vertical = 14.dp)),
        ) {
            Text(
                text,
                fontFamily = IncSerifFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 21.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.4).sp,
                color = colors.text,
            )
        }
    }
}
