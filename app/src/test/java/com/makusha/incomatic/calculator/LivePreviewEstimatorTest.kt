package com.makusha.incomatic.calculator

import java.time.Year
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LivePreviewEstimatorTest {

    private fun base() = CalculatorState(
        payFrequency = PayFrequencyOption.ANNUAL,
        salary = "",
        bonus = "",
        commission = "",
        medical = "",
        dental = "",
        vision = "",
        fsa = "",
        t401k = 0,
        roth = 0,
    )

    @Test
    fun `estimate returns null when there is no annual income and no bonus`() {
        assertNull(LivePreviewEstimator.estimate(base()))
    }

    @Test
    fun `estimate applies marginal federal brackets and the known CA state rate`() {
        val state = base().copy(salary = "100000", stateCode = "CA")
        val result = LivePreviewEstimator.estimate(state)!!

        // taxable = 100000 - 14600 standard deduction = 85400, crossing the 10/12/22% brackets.
        // fed = 11600*.10 + 35550*.12 + 38250*.22 = 1160 + 4266 + 8415 = 13841
        // state (CA .06) = 85400*.06 = 5124; ss = 100000*.062 = 6200; medicare = 100000*.0145 = 1450
        // net = 100000 - (13841+5124+6200+1450) = 73385
        assertEquals(73385.0, result.perPeriod, 0.01)
        assertEquals(73.385, result.takeHomePct, 0.01)
    }

    @Test
    fun `estimate falls back to the default state rate for an unrecognized code`() {
        val known = LivePreviewEstimator.estimate(base().copy(salary = "100000", stateCode = "CA"))!!
        val unknown = LivePreviewEstimator.estimate(base().copy(salary = "100000", stateCode = "ZZ"))!!

        // Default rate (.04) is lower than CA's (.06), so the unknown-code estimate nets more.
        assertTrue(unknown.perPeriod > known.perPeriod)
        // unknown state tax = 85400*.04 = 3416; net = 100000 - (13841+3416+6200+1450) = 75093
        assertEquals(75093.0, unknown.perPeriod, 0.01)
    }

    @Test
    fun `estimate excludes a bonus dated a future year from annual income`() {
        val nextYear = Year.now().value + 1
        val state = base().copy(salary = "5000", bonus = "10000", bonusDate = "$nextYear-06-15")
        val result = LivePreviewEstimator.estimate(state)!!

        // annual should be just the 5000 salary, not 15000 with the bonus included.
        // ss = 5000*.062 = 310; medicare = 5000*.0145 = 72.5; taxable is 0 (below standard deduction) so fed/state = 0.
        assertEquals(5000.0 - 310.0 - 72.5, result.perPeriod, 0.01)
    }

    @Test
    fun `estimate treats a blank bonusDate as the current tax year`() {
        val state = base().copy(salary = "", bonus = "10000", bonusDate = "")
        val result = LivePreviewEstimator.estimate(state)!!

        // annual = 10000 (bonus only); taxable = max(0, 10000-14600) = 0, so fed/state = 0.
        // ss = 10000*.062 = 620; medicare = 10000*.0145 = 145.
        assertEquals(10000.0 - 620.0 - 145.0, result.perPeriod, 0.01)
    }

    @Test
    fun `estimate reduces net pay when pretax benefits are present`() {
        val withoutBenefits = LivePreviewEstimator.estimate(base().copy(salary = "50000"))!!
        val withBenefits = LivePreviewEstimator.estimate(
            base().copy(salary = "50000", medical = "200", t401k = 10),
        )!!

        assertTrue(withBenefits.perPeriod < withoutBenefits.perPeriod)
    }
}
