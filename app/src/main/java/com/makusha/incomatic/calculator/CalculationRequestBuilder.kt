package com.makusha.incomatic.calculator

import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.net.dto.CalculateRequest
import com.makusha.incomatic.net.dto.Country
import com.makusha.incomatic.net.dto.CountryOptions
import com.makusha.incomatic.net.dto.CountryOptionsUS
import com.makusha.incomatic.net.dto.Earnings
import com.makusha.incomatic.net.dto.Hourly
import com.makusha.incomatic.net.dto.Posttax
import com.makusha.incomatic.net.dto.Pretax
import com.makusha.incomatic.net.dto.Salary
import com.makusha.incomatic.net.dto.SalaryBasis
import com.makusha.incomatic.net.dto.W4

/**
 * Maps [CalculatorState] to the real [CalculateRequest] contract, field for
 * field with modules/common/src/main/java/app/salary/common/dto/. The
 * benefits fields (medical/dental/vision/fsa) are entered per pay period in
 * the UI but the backend's Pretax DTO expects annual figures — multiply by
 * periodsPerYear, mirroring what the design's own mock estimator does.
 *
 * [grantsRsuValue] is the grant-derived RSU value vesting this tax year
 * (from EquityViewModel.vestingThisYear); an explicit [rsuOverride] always
 * wins over it, matching iOS's effectiveRsuAnnual.
 */
fun CalculatorState.toCalculateRequest(grantsRsuValue: Double = 0.0): CalculateRequest {
    val periods = payFrequency.cadence.periodsPerYear

    val earnings = Earnings(
        salary = if (incomeType == IncomeType.SALARY) {
            Salary(amount = salary.toDoubleOrNull() ?: 0.0, basis = SalaryBasis.PER_YEAR)
        } else null,
        hourly = if (incomeType == IncomeType.HOURLY) {
            Hourly(
                rate = hourlyRate.toDoubleOrNull() ?: 0.0,
                regularHours = regularHours.toDoubleOrNull() ?: 0.0,
                overtimeHours = overtimeHours.toDoubleOrNull() ?: 0.0,
            )
        } else null,
        bonus = bonus.toDoubleOrNull() ?: 0.0,
        bonusDate = bonusDate.ifBlank { null },
        bonusRecurring = bonusRecurring,
        commission = commission.toDoubleOrNull() ?: 0.0,
        rsuVesting = rsuOverride.toDoubleOrNull() ?: grantsRsuValue,
    )

    val pretax = Pretax(
        pensionPercent = t401k / 100.0,
        medical = (medical.toDoubleOrNull() ?: 0.0) * periods,
        dental = (dental.toDoubleOrNull() ?: 0.0) * periods,
        vision = (vision.toDoubleOrNull() ?: 0.0) * periods,
        healthcareFsa = (fsa.toDoubleOrNull() ?: 0.0) * periods,
    )

    val posttax = Posttax(roth401kPercent = roth / 100.0)

    val w4 = W4(
        useOldW4 = useOldW4,
        dependentsAmount = dependents.toDoubleOrNull() ?: 0.0,
        otherIncome = otherIncome.toDoubleOrNull() ?: 0.0,
        additionalWithholding = extraWithholding.toDoubleOrNull() ?: 0.0,
    )

    val countryOptionsUS = CountryOptionsUS(
        state = stateCode,
        filingStatus = filingStatus,
        w4 = w4,
    )

    return CalculateRequest(
        country = Country.US,
        taxYear = AppConfig.taxYear,
        earnings = earnings,
        cadence = payFrequency.cadence,
        pretax = pretax,
        posttax = posttax,
        countryOptions = CountryOptions(us = countryOptionsUS),
    )
}
