package com.makusha.incomatic

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The tax year was a hardcoded `const val 2025` until 2026-08-06, which left
 * Android a full year behind the deployed rule packs with nothing to catch it.
 */
class AppConfigTaxYearTest {

    /** [AppConfig] is a singleton, so a test that moves the year must put it back. */
    @After
    fun restoreDefault() {
        AppConfig.cacheTaxYear(AppConfig.FALLBACK_TAX_YEAR)
    }

    @Test
    fun `adopts a year resolved from the backend`() {
        AppConfig.cacheTaxYear(2027)
        assertEquals(2027, AppConfig.taxYear)
    }

    @Test
    fun `ignores a non-positive year rather than wiping a good one`() {
        AppConfig.cacheTaxYear(2027)
        AppConfig.cacheTaxYear(0)
        AppConfig.cacheTaxYear(-1)
        assertEquals(2027, AppConfig.taxYear)
    }

    @Test
    fun `falls back to a year that has a published rule pack`() {
        // Never derived from the current date: rolling forward on January 1 would
        // ask the backend for a year it has no pack for and break every calculation.
        assertEquals(2026, AppConfig.FALLBACK_TAX_YEAR)
        assertEquals(AppConfig.FALLBACK_TAX_YEAR, AppConfig.taxYear)
    }
}
