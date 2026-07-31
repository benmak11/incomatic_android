package com.makusha.incomatic.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncButtonVariant
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.insights.CalculationBreakdownCard
import com.makusha.incomatic.net.dto.SavedCalculationDetail
import com.makusha.incomatic.net.dto.SavedCalculationSummary
import com.makusha.incomatic.net.dto.displaySubtitle
import com.makusha.incomatic.net.dto.displayTitle
import com.makusha.incomatic.net.dto.savedAtCompact

/**
 * Detail of a single saved calculation — back/delete header, then the same
 * CalculationBreakdownCard the Insights tab renders, driven by the saved
 * session's own response (no HistoryDetailMapper needed, see the Phase 4 plan).
 */
@Composable
fun SessionDetailView(summary: SavedCalculationSummary, viewModel: HistoryViewModel, onBack: () -> Unit, onDelete: () -> Unit) {
    val colors = incColors()
    var detail by remember { mutableStateOf<SavedCalculationDetail?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(summary.id) {
        loadError = null
        runCatching { viewModel.detail(summary.id) }
            .onSuccess { detail = it }
            .onFailure { loadError = it.message ?: "Couldn't load this session" }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 100.dp)) {
        NavHeader(onBack = onBack, onDelete = onDelete)
        PageHeader(summary)
        when {
            loadError != null -> Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Couldn't load this session", style = IncType.body.copy(fontWeight = FontWeight.SemiBold), color = colors.text)
                Text(loadError.orEmpty(), style = IncType.secondary, color = colors.textDim)
            }
            detail == null -> Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.sage)
            }
            else -> Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                CalculationBreakdownCard(detail!!.response)
                IncButton(text = "Delete from history", onClick = onDelete, variant = IncButtonVariant.DANGER)
            }
        }
    }
}

@Composable
private fun NavHeader(onBack: () -> Unit, onDelete: () -> Unit) {
    val colors = incColors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onBack),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                val w = size.width
                val h = size.height
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.7f, h * 0.15f)
                    lineTo(w * 0.3f, h * 0.5f)
                    lineTo(w * 0.7f, h * 0.85f)
                }
                drawPath(p, colors.sage, style = Stroke(w * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            Text("History", style = IncType.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp), color = colors.sage)
        }
        Text(
            "Delete",
            style = IncType.secondary.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textDim,
            modifier = Modifier.clickable(onClick = onDelete),
        )
    }
}

@Composable
private fun PageHeader(summary: SavedCalculationSummary) {
    val colors = incColors()
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 8.dp, bottom = 14.dp)) {
        Text(
            "SAVED ${summary.savedAtCompact.uppercase()} · ${summary.displaySubtitle.uppercase()}",
            style = IncType.overline.copy(fontSize = 11.sp),
            color = colors.sage,
            maxLines = 1,
        )
        Spacer(Modifier.size(4.dp))
        Text(summary.displayTitle, style = IncType.pageTitle.copy(fontSize = 32.sp), color = colors.text)
    }
}
