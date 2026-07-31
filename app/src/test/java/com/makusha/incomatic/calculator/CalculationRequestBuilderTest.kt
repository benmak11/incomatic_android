package com.makusha.incomatic.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class CalculationRequestBuilderTest {

    @Test
    fun `salary income type populates salary and leaves hourly null`() {
        val earnings = CalculatorState(incomeType = IncomeType.SALARY, salary = "85000").toCalculateRequest().earnings!!
        assertNotNull(earnings.salary)
        assertNull(earnings.hourly)
        assertEquals(85000.0, earnings.salary!!.amount, 0.0)
    }

    @Test
    fun `hourly income type populates hourly and leaves salary null`() {
        val earnings = CalculatorState(
            incomeType = IncomeType.HOURLY,
            hourlyRate = "25",
            regularHours = "80",
            overtimeHours = "5",
        ).toCalculateRequest().earnings!!

        assertNull(earnings.salary)
        assertNotNull(earnings.hourly)
        assertEquals(25.0, earnings.hourly!!.rate, 0.0)
        assertEquals(80.0, earnings.hourly!!.regularHours, 0.0)
        assertEquals(5.0, earnings.hourly!!.overtimeHours, 0.0)
    }

    @Test
    fun `blank numeric fields map to zero rather than crashing`() {
        val earnings = CalculatorState(bonus = "", commission = "").toCalculateRequest().earnings!!
        assertEquals(0.0, earnings.bonus!!, 0.0)
        assertEquals(0.0, earnings.commission!!, 0.0)
    }

    @Test
    fun `explicit rsuOverride wins over the grant-derived value`() {
        val earnings = CalculatorState(rsuOverride = "5000").toCalculateRequest(grantsRsuValue = 12345.0).earnings!!
        assertEquals(5000.0, earnings.rsuVesting!!, 0.0)
    }

    @Test
    fun `blank rsuOverride falls back to the grant-derived value`() {
        val earnings = CalculatorState(rsuOverride = "").toCalculateRequest(grantsRsuValue = 12345.0).earnings!!
        assertEquals(12345.0, earnings.rsuVesting!!, 0.0)
    }

    @Test
    fun `401k and roth percents are divided by 100`() {
        val request = CalculatorState(t401k = 6, roth = 25).toCalculateRequest()
        assertEquals(0.06, request.pretax!!.pensionPercent, 0.0001)
        assertEquals(0.25, request.posttax!!.roth401kPercent, 0.0001)
    }

    @Test
    fun `per-period benefit fields are annualized by the pay cadence`() {
        val pretax = CalculatorState(
            payFrequency = PayFrequencyOption.BIWEEKLY,
            medical = "100",
            dental = "20",
            vision = "10",
            fsa = "50",
        ).toCalculateRequest().pretax!!

        assertEquals(100.0 * 26, pretax.medical, 0.0)
        assertEquals(20.0 * 26, pretax.dental, 0.0)
        assertEquals(10.0 * 26, pretax.vision, 0.0)
        assertEquals(50.0 * 26, pretax.healthcareFsa, 0.0)
    }

    @Test
    fun `blank bonusDate maps to null`() {
        val blank = CalculatorState(bonusDate = "").toCalculateRequest().earnings!!
        assertNull(blank.bonusDate)

        val explicit = CalculatorState(bonusDate = "2025-03-15").toCalculateRequest().earnings!!
        assertEquals("2025-03-15", explicit.bonusDate)
    }
}
