package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable

@Serializable
data class CalculateResponse(
    val grossPerCadence: Double? = null,
    val baseSalaryPerCadence: Double? = null,
    val bonusPerCadence: Double? = null,
    val netPerCadence: Double? = null,
    val currency: String? = null,
    val supplemental: SupplementalBreakdown? = null,
    val lineItems: List<LineItem> = emptyList(),
    val explanation: List<Explanation> = emptyList(),
    val calculationId: String? = null,
    val rulePackVersion: String? = null,
)

@Serializable
data class LineItem(
    val name: String,
    val amount: Double,
    val category: LineItemCategory? = null,
)

@Serializable
data class Explanation(
    val id: String,
    val text: String,
)

/**
 * Server-truth annual tax slice for supplemental income (bonus + commission
 * + RSU vesting). Not per-cadence like the rest of [CalculateResponse] —
 * supplemental income is lump-sum-shaped. See CLAUDE.md in salary-calculator
 * for the full explanation of why this is annual, not per-period.
 */
@Serializable
data class SupplementalBreakdown(
    val bonusGross: Double = 0.0,
    val commissionGross: Double = 0.0,
    val rsuGross: Double = 0.0,
    val federalTax: Double = 0.0,
    val socialSecurity: Double = 0.0,
    val medicare: Double = 0.0,
    val net: Double = 0.0,
)
