package com.makusha.incomatic.equity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.calculator.CalcCardIcon
import com.makusha.incomatic.calculator.CalcIcon
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncCard
import com.makusha.incomatic.design.IncCardHeader
import com.makusha.incomatic.design.IncMoneyField
import com.makusha.incomatic.design.IncPicker
import com.makusha.incomatic.design.IncSubtleRippleScope
import com.makusha.incomatic.design.IncSwitch
import com.makusha.incomatic.design.IncTextField
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.ApiClient
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.net.dto.StockQuote
import com.makusha.incomatic.net.dto.StockSymbol
import com.makusha.incomatic.util.formatMoney
import com.makusha.incomatic.util.formatShares
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** Add/edit grant form — ticker search + live quote (Company card), grant terms, vesting presets, live vest preview. */
@Composable
fun GrantForm(existing: RsuGrant?, equity: EquityViewModel, onSaved: (RsuGrant) -> Unit) {
    val colors = incColors()
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    val editingSearchMode = existing != null && existing.manualPrice != true && existing.ticker != null

    var manualMode by remember { mutableStateOf(existing != null && !editingSearchMode) }
    var searchQuery by remember { mutableStateOf(if (editingSearchMode) existing?.ticker ?: "" else "") }
    var searchResults by remember { mutableStateOf<List<StockSymbol>>(emptyList()) }
    var selectedSymbol by remember {
        mutableStateOf(if (editingSearchMode) StockSymbol(existing!!.ticker!!, existing.company ?: existing.ticker!!) else null)
    }
    var quote by remember { mutableStateOf<StockQuote?>(null) }
    var quoteLoading by remember { mutableStateOf(false) }
    var lookupUnavailable by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf(if (!editingSearchMode) existing?.company ?: "" else "") }

    var priceText by remember { mutableStateOf(existing?.pricePerShare?.toString() ?: "") }
    var sharesText by remember { mutableStateOf(existing?.sharesTotal?.let { formatShares(it) } ?: "") }
    var dateText by remember { mutableStateOf(existing?.grantDate ?: "") }
    var preset by remember {
        mutableStateOf(
            VestingPreset.fromPresetId(existing?.schedule?.presetId)
                ?: if (existing != null) VestingPreset.CUSTOM else VestingPreset.MONTHLY1CLIFF,
        )
    }
    var customTotal by remember { mutableStateOf((existing?.schedule?.totalMonths ?: 48).toString()) }
    var customCliff by remember { mutableStateOf((existing?.schedule?.cliffMonths ?: 12).toString()) }
    var customFreq by remember { mutableStateOf((existing?.schedule?.freqMonths ?: 1).toString()) }

    LaunchedEffect(searchQuery, manualMode) {
        val query = searchQuery.trim()
        if (manualMode || query.length < 2 || query == selectedSymbol?.symbol) {
            searchResults = emptyList()
            return@LaunchedEffect
        }
        delay(300)
        try {
            searchResults = ApiClient.salaryCalculatorService.searchStocks(query)
            lookupUnavailable = false
        } catch (e: HttpException) {
            searchResults = emptyList()
            if (e.code() == 503) {
                lookupUnavailable = true
                manualMode = true
            }
        } catch (e: Exception) {
            searchResults = emptyList()
        }
    }

    LaunchedEffect(selectedSymbol) {
        val symbol = selectedSymbol ?: return@LaunchedEffect
        quoteLoading = true
        try {
            quote = ApiClient.salaryCalculatorService.quoteStock(symbol.symbol)
            lookupUnavailable = false
        } catch (e: HttpException) {
            quote = null
            if (e.code() == 503) {
                lookupUnavailable = true
                manualMode = true
            }
        } catch (e: Exception) {
            quote = null
        } finally {
            quoteLoading = false
        }
    }

    val schedule = preset.terms?.let { (total, cliff, freq) ->
        RsuGrant.VestingSchedule(preset.presetId, total, cliff, freq)
    } ?: run {
        val total = customTotal.toIntOrNull() ?: 48
        RsuGrant.VestingSchedule(VestingPreset.CUSTOM.presetId, total, (customCliff.toIntOrNull() ?: 12).coerceAtMost(total), customFreq.toIntOrNull() ?: 1)
    }

    val currentPrice = if (manualMode) {
        priceText.toDoubleOrNull()
    } else {
        quote?.price ?: existing?.pricePerShare?.takeIf { existing.manualPrice != true }
    }
    val hasCompany = if (manualMode) companyName.isNotBlank() else selectedSymbol != null

    val shares = sharesText.toDoubleOrNull()
    val draft: RsuGrant? = if (shares != null && shares > 0 && currentPrice != null && currentPrice > 0 && dateText.isNotBlank() && hasCompany) {
        RsuGrant(
            id = existing?.id,
            ticker = if (manualMode) null else selectedSymbol?.symbol,
            company = if (manualMode) companyName else selectedSymbol?.name,
            manualPrice = manualMode,
            sharesTotal = shares,
            pricePerShare = currentPrice,
            grantDate = dateText,
            schedule = schedule,
        )
    } else null

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
            IncCard {
                IncCardHeader(icon = { CalcCardIcon(CalcIcon.FLAG, colors.sage) }, title = "Company")
                if (!manualMode) {
                    IncTextField(label = "Ticker or company", value = searchQuery, onChange = { searchQuery = it }, placeholder = "Search: AAPL, Apple…")
                    if (lookupUnavailable) {
                        Text(
                            "Stock lookup unavailable. Enter price manually",
                            style = IncType.secondary,
                            color = colors.red,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                    }
                    searchResults.forEach { result ->
                        StockResultRow(
                            result = result,
                            onClick = {
                                selectedSymbol = result
                                searchResults = emptyList()
                                searchQuery = result.symbol
                            },
                        )
                    }
                    if (selectedSymbol != null) {
                        QuotePill(
                            symbol = selectedSymbol!!,
                            quote = quote,
                            loading = quoteLoading,
                            onClear = {
                                selectedSymbol = null
                                quote = null
                                searchQuery = ""
                            },
                        )
                    }
                } else {
                    IncTextField(label = "Company name", value = companyName, onChange = { companyName = it }, placeholder = "Acme Corp")
                    IncMoneyField(label = "Price per share", value = priceText, onChange = { priceText = it })
                }
                IncSwitch(
                    checked = manualMode,
                    onChange = { checked ->
                        manualMode = checked
                        if (checked) {
                            selectedSymbol = null
                            quote = null
                        }
                    },
                    label = "Company not listed? Enter price manually",
                )
            }
            IncCard {
                IncCardHeader(icon = { CalcCardIcon(CalcIcon.WALLET, colors.sage) }, title = "Grant terms")
                IncTextField(label = "Total shares", value = sharesText, onChange = { sharesText = it }, placeholder = "400", keyboardType = KeyboardType.Decimal)
                IncTextField(label = "Grant date (YYYY-MM-DD)", value = dateText, onChange = { dateText = it }, placeholder = "2025-03-15", keyboardType = KeyboardType.Number)
            }
            IncCard {
                IncCardHeader(icon = { CalcCardIcon(CalcIcon.SEED, colors.sage) }, title = "Vesting schedule")
                IncPicker(label = "Preset", value = preset, options = VestingPreset.entries, labelOf = { it.displayName }, onChange = { preset = it })
                if (preset == VestingPreset.CUSTOM) {
                    IncTextField(label = "Duration (months)", value = customTotal, onChange = { customTotal = it }, keyboardType = KeyboardType.Number)
                    IncTextField(label = "Cliff (months)", value = customCliff, onChange = { customCliff = it }, keyboardType = KeyboardType.Number)
                    IncTextField(label = "Vest every (months)", value = customFreq, onChange = { customFreq = it }, keyboardType = KeyboardType.Number)
                }
            }
            IncCard {
                IncCardHeader(icon = { CalcCardIcon(CalcIcon.CHART, colors.sage) }, title = "Vest distribution")
                if (draft != null) {
                    VestTimeline(draft)
                } else {
                    Text(
                        "Set shares, price, and a grant date to preview vesting.",
                        style = IncType.secondary,
                        color = colors.textMute,
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            IncButton(
                text = if (saving) "Saving…" else if (existing == null) "Save grant" else "Save changes",
                onClick = {
                    draft?.let { d ->
                        scope.launch {
                            saving = true
                            val result = equity.save(d)
                            saving = false
                            result?.let(onSaved)
                        }
                    }
                },
                enabled = draft != null && !saving,
            )
        }
    }
}

@Composable
private fun StockResultRow(result: StockSymbol, onClick: () -> Unit) {
    val colors = incColors()
    IncSubtleRippleScope {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(result.symbol, style = IncType.secondary.copy(fontWeight = FontWeight.Bold), color = colors.text)
            Spacer(Modifier.size(8.dp))
            Text(result.name, style = IncType.secondary, color = colors.textDim, maxLines = 1, modifier = Modifier.weight(1f))
        }
    }
}

private val ASOF_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())

private fun formatAsOf(iso: String): String = runCatching { ASOF_FORMATTER.format(Instant.parse(iso)) }.getOrDefault("")

@Composable
private fun QuotePill(symbol: StockSymbol, quote: StockQuote?, loading: Boolean, onClear: () -> Unit) {
    val colors = incColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.sageBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(symbol.symbol, style = IncType.secondary.copy(fontWeight = FontWeight.Bold), color = colors.sage)
        Spacer(Modifier.size(8.dp))
        if (loading) {
            CircularProgressIndicator(color = colors.sage, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        } else if (quote != null) {
            Text("· ${formatMoney(quote.price)}", style = IncType.secondary.copy(fontWeight = FontWeight.SemiBold), color = colors.sage)
            Spacer(Modifier.size(6.dp))
            Text("as of ${formatAsOf(quote.asOf)}", style = IncType.secondary.copy(fontSize = 11.sp), color = colors.textDim)
        }
        Spacer(Modifier.weight(1f))
        Canvas(modifier = Modifier.size(15.dp).clickable(onClick = onClear)) {
            val w = size.width
            val h = size.height
            val inset = w * 0.2f
            val stroke = Stroke(w * 0.12f, cap = StrokeCap.Round)
            drawLine(colors.textMute, Offset(inset, inset), Offset(w - inset, h - inset), stroke.width, cap = stroke.cap)
            drawLine(colors.textMute, Offset(w - inset, inset), Offset(inset, h - inset), stroke.width, cap = stroke.cap)
        }
    }
}
