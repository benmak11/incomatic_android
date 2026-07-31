package com.makusha.incomatic.equity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makusha.incomatic.calculator.CalcCardIcon
import com.makusha.incomatic.calculator.CalcIcon
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncButtonVariant
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncCardHeader
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.util.formatMoney
import com.makusha.incomatic.util.formatShares

/** Grant detail — facts + full vest timeline + Delete (confirmed). Edit re-opens the form pre-filled. */
@Composable
fun GrantDetail(grant: RsuGrant, onDelete: () -> Unit) {
    val colors = incColors()
    var confirmDelete by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
        IncCard {
            IncCardHeader(
                icon = { CalcCardIcon(CalcIcon.FLAG, colors.sage) },
                title = grant.company ?: grant.ticker ?: "Grant",
            )
            FactRow("Total shares", formatShares(grant.sharesTotal))
            FactRow("Price per share", formatMoney(grant.pricePerShare))
            FactRow("Grant value", formatMoney(grant.sharesTotal * grant.pricePerShare, cents = false))
            FactRow("Grant date", grant.grantDate)
            FactRow("Schedule", grant.schedule.label())
        }
        IncCard {
            IncCardHeader(icon = { CalcCardIcon(CalcIcon.CHART, colors.sage) }, title = "Vest distribution")
            VestTimeline(grant)
        }
        IncButton(text = "Delete grant", onClick = { confirmDelete = true }, variant = IncButtonVariant.DANGER)
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this grant?", style = IncType.title, color = colors.text) },
            text = { Text("Its vesting value is removed from this year's calculation.", style = IncType.secondary, color = colors.textDim) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("Delete grant", color = colors.red)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = colors.text) }
            },
        )
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    val colors = incColors()
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, style = IncType.secondary, color = colors.textDim, modifier = Modifier.weight(1f))
        Text(value, style = IncType.secondary.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.text)
    }
}
