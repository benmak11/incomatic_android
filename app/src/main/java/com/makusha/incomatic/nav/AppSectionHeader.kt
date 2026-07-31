package com.makusha.incomatic.nav

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.design.IncSubtleRippleScope
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors

/**
 * Serif page title (34sp, -1 tracking — the Android stand-in for iOS's New
 * York title) + optional hairline section tabs with a 2dp sage underline.
 * Insights/History pass no sections and just show the title.
 */
@Composable
fun <T> AppSectionHeader(
    title: String,
    sections: List<T> = emptyList(),
    selected: T? = null,
    labelOf: (T) -> String = { it.toString() },
    onSelect: (T) -> Unit = {},
) {
    val colors = incColors()
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 26.dp, bottom = 18.dp)) {
        Text(title, style = IncType.pageTitle, color = colors.text)
        if (sections.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .drawBehind {
                        drawLine(
                            color = colors.hairline,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                IncSubtleRippleScope {
                    sections.forEach { section ->
                        val active = section == selected
                        Text(
                            labelOf(section),
                            style = IncType.title,
                            color = if (active) colors.text else colors.textMute,
                            modifier = Modifier
                                .clickable { onSelect(section) }
                                .padding(bottom = 10.dp)
                                .drawBehind {
                                    if (active) {
                                        drawLine(
                                            color = colors.sage,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 2.dp.toPx(),
                                        )
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}
