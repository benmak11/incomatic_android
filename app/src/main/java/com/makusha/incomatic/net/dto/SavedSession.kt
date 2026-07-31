package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Denormalized row data for the History list — mirrors SavedCalculationSummary.java. */
@Serializable
data class SavedCalculationSummary(
    val id: String,
    val savedAt: String,
    val country: String? = null,
    val taxYear: Int? = null,
    val cadence: String? = null,
    val stateCode: String? = null,
    val stateName: String? = null,
    val currency: String? = null,
    val takeHomePerPeriod: Double? = null,
    val taxesPerPeriod: Double? = null,
    val benefitsPerPeriod: Double? = null,
    val grossPerPeriod: Double? = null,
    val takeHomePct: Double? = null,
)

/** Mirrors CalculationListResponse.java. */
@Serializable
data class CalculationListResponse(
    val items: List<SavedCalculationSummary> = emptyList(),
    val nextCursor: String? = null,
)

/** Mirrors SavedCalculationDetail.java — request/response reuse the existing CalculateRequest/CalculateResponse types. */
@Serializable
data class SavedCalculationDetail(
    val summary: SavedCalculationSummary,
    val request: CalculateRequest,
    val response: CalculateResponse,
)

/** Local display helpers, ported from SavedSession.swift's SavedCalculationSummary extension. */

/** State name when US, else the state/country code. */
val SavedCalculationSummary.displayTitle: String
    get() = stateName?.takeIf { it.isNotEmpty() }
        ?: stateCode?.takeIf { it.isNotEmpty() }
        ?: country
        ?: "Calculation"

/** "United States · Bi-weekly" style subtitle. */
val SavedCalculationSummary.displaySubtitle: String
    get() {
        val countryLabel = if (country == "US") "United States" else country.orEmpty()
        val cadenceLabel = cadenceFriendly
        return listOf(countryLabel, cadenceLabel).filter { it.isNotEmpty() }.joinToString(" · ")
    }

/** Compact "Jun 7" / "Jun 7, 2026" label; falls back to the raw ISO string when parsing fails. */
val SavedCalculationSummary.savedAtCompact: String
    get() {
        val parsed = runCatching { OffsetDateTime.parse(savedAt) }.getOrNull() ?: return savedAt
        val date = parsed.atZoneSameInstant(ZoneId.systemDefault())
        val sameYear = date.year == OffsetDateTime.now().year
        val pattern = if (sameYear) "MMM d" else "MMM d, yyyy"
        return date.format(DateTimeFormatter.ofPattern(pattern))
    }

private val SavedCalculationSummary.cadenceFriendly: String
    get() = when (cadence?.uppercase()) {
        "DAILY" -> "Daily"
        "WEEKLY" -> "Weekly"
        "BIWEEKLY" -> "Bi-weekly"
        "SEMIMONTHLY" -> "Semi-monthly"
        "MONTHLY" -> "Monthly"
        "QUARTERLY" -> "Quarterly"
        "SEMIANNUAL" -> "Semi-annual"
        "ANNUAL" -> "Annual"
        else -> cadence?.lowercase()?.replaceFirstChar { it.uppercase() }.orEmpty()
    }
