package com.makusha.incomatic.net.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalculateRequest(
    val country: Country,
    val taxYear: Int,
    val earnings: Earnings? = null,
    val payDate: String? = null,
    val cadence: PayCadence = PayCadence.ANNUAL,
    val pretax: Pretax? = null,
    val posttax: Posttax? = null,
    val countryOptions: CountryOptions? = null,
)

@Serializable
data class Earnings(
    val salary: Salary? = null,
    val hourly: Hourly? = null,
    val bonus: Double? = 0.0,
    val bonusDate: String? = null,
    val bonusRecurring: Boolean? = false,
    val commission: Double? = 0.0,
    val rsuVesting: Double? = 0.0,
)

@Serializable
enum class SalaryBasis {
    PER_YEAR, PER_PERIOD
}

@Serializable
data class Salary(
    val amount: Double,
    val basis: SalaryBasis = SalaryBasis.PER_YEAR,
)

@Serializable
data class Hourly(
    val rate: Double,
    val regularHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val overtimeMultiplier: Double = 1.5,
    val doubleTimeHours: Double = 0.0,
    val doubleTimeMultiplier: Double = 2.0,
)

@Serializable
data class CountryOptions(
    @SerialName("US") val us: CountryOptionsUS? = null,
)

@Serializable
data class CountryOptionsUS(
    val state: String,
    val filingStatus: FilingStatus,
    val allowances: Int = 0,
    val w4: W4? = null,
)

@Serializable
data class W4(
    val useOldW4: Boolean = false,
    val dependentsAmount: Double = 0.0,
    val otherIncome: Double = 0.0,
    val itemizedDeductions: Double = 0.0,
    val additionalWithholding: Double = 0.0,
    val exemptFederal: Boolean = false,
    val exemptSocialSecurity: Boolean = false,
    val exemptMedicare: Boolean = false,
)

@Serializable
data class Pretax(
    val percent: Double = 0.0,
    val fixed: Double = 0.0,
    val hsa: Double = 0.0,
    val pensionPercent: Double = 0.0,
    val medical: Double = 0.0,
    val dental: Double = 0.0,
    val vision: Double = 0.0,
    val healthcareFsa: Double = 0.0,
    val dependentCareFsa: Double = 0.0,
)

@Serializable
data class Posttax(
    val fixed: Double = 0.0,
    val roth401kPercent: Double = 0.0,
)
