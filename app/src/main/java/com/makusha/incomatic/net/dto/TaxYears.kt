package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable

/**
 * Port of incomatic (iOS)'s `TaxYearsResponse`.
 *
 * [defaultTaxYear] is nullable: the backend returns null when it has no rule
 * pack for the country at all, which must not be mistaken for "use year zero".
 */
@Serializable
data class TaxYearsResponse(
    val country: String? = null,
    val supportedTaxYears: List<Int> = emptyList(),
    val defaultTaxYear: Int? = null,
)
