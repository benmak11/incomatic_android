package com.makusha.incomatic.calculator

import com.makusha.incomatic.net.dto.FilingStatus
import com.makusha.incomatic.net.dto.PayCadence

enum class IncomeType { SALARY, HOURLY }

enum class CalculatorSection(val label: String) {
    EARNINGS("Earnings"), FEDERAL("Federal"), STATE("State"), BENEFITS("Benefits")
}

enum class PayFrequencyOption(val label: String, val cadence: PayCadence) {
    WEEKLY("Weekly", PayCadence.WEEKLY),
    BIWEEKLY("Bi-weekly", PayCadence.BIWEEKLY),
    SEMIMONTHLY("Semi-monthly", PayCadence.SEMIMONTHLY),
    MONTHLY("Monthly", PayCadence.MONTHLY),
    ANNUAL("Annual", PayCadence.ANNUAL),
}

/** Shared between the Calculator's Federal section and Onboarding's filing-status step. */
fun filingStatusLabel(status: FilingStatus): String = when (status) {
    FilingStatus.SINGLE -> "Single or married filing separately"
    FilingStatus.MARRIED -> "Married filing jointly"
    FilingStatus.HEAD_OF_HOUSEHOLD -> "Head of household"
}

/**
 * Form state for the Calculator's four sections. Field set mirrors the
 * design's AND_FORM seed. Meant to be shared with Onboarding (Phase 3) once
 * that exists, same as on iOS — kept as a plain data class rather than
 * folded directly into the ViewModel so it can be lifted there later without
 * restructuring.
 */
data class CalculatorState(
    val payFrequency: PayFrequencyOption = PayFrequencyOption.BIWEEKLY,
    val incomeType: IncomeType = IncomeType.SALARY,
    val salary: String = "85000",
    val hourlyRate: String = "",
    val regularHours: String = "80",
    val overtimeHours: String = "",
    val bonus: String = "",
    val bonusDate: String = "",
    val commission: String = "",
    val filingStatus: FilingStatus = FilingStatus.SINGLE,
    val useOldW4: Boolean = false,
    val dependents: String = "",
    val otherIncome: String = "",
    val extraWithholding: String = "",
    val stateCode: String = "CA",
    val livesElsewhere: Boolean = false,
    val medical: String = "",
    val dental: String = "",
    val vision: String = "",
    val fsa: String = "",
    val t401k: Int = 6,
    val roth: Int = 0,
    val rsuOverride: String = "",
)
