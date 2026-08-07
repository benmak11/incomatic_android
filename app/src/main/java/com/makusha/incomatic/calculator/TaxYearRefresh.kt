package com.makusha.incomatic.calculator

import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.data.TaxYearPrefs
import com.makusha.incomatic.net.ApiClient

/**
 * Port of incomatic (iOS)'s `TaxYearRefresh.swift`. Asks the backend which tax
 * year it considers current and caches the answer, so a newly published rule
 * pack takes effect without an app update.
 *
 * Every failure path keeps whatever year we already had. A backend that is down
 * or a payload we cannot read must not strand the calculator on a year with no
 * rule pack.
 */
suspend fun refreshTaxYear(prefs: TaxYearPrefs) {
    runCatching { ApiClient.salaryCalculatorService.getTaxYears() }
        .getOrNull()
        ?.defaultTaxYear
        ?.takeIf { it > 0 }
        ?.let { latest ->
            AppConfig.cacheTaxYear(latest)
            prefs.setTaxYear(latest)
        }
}
