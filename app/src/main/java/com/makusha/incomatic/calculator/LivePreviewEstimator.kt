package com.makusha.incomatic.calculator

data class LivePreviewResult(val perPeriod: Double, val takeHomePct: Double)

/**
 * Local, instant estimate for the sticky-CTA ribbon — Kotlin port of
 * shared.jsx's mockCalculate(). Deliberately NOT the real tax engine: it
 * exists purely so the ribbon updates on every keystroke without a network
 * round-trip. The real number comes back from POST /v1/calculate.
 */
object LivePreviewEstimator {
    private val FEDERAL_BRACKETS = listOf(
        11_600.0 to 0.10, 47_150.0 to 0.12, 100_525.0 to 0.22, 191_950.0 to 0.24,
        243_725.0 to 0.32, 609_350.0 to 0.35, Double.MAX_VALUE to 0.37,
    )
    private val STATE_RATES = mapOf(
        "CA" to 0.06, "NY" to 0.055, "TX" to 0.0, "FL" to 0.0,
        "MD" to 0.0475, "WA" to 0.0, "IL" to 0.0495, "MA" to 0.05,
    )
    private const val DEFAULT_STATE_RATE = 0.04
    private const val STANDARD_DEDUCTION = 14_600.0
    private const val SS_WAGE_BASE = 168_600.0
    private const val SS_RATE = 0.062
    private const val MEDICARE_RATE = 0.0145

    fun estimate(state: CalculatorState): LivePreviewResult? {
        val bonusAmt = state.bonus.toDoubleOrNull() ?: 0.0
        val bonusInCurrentYear = state.bonusDate.isBlank() ||
            runCatching { state.bonusDate.substring(0, 4).toInt() }.getOrNull() == java.time.Year.now().value
        val bonusForYear = if (bonusInCurrentYear) bonusAmt else 0.0

        val salaryAmt = if (state.incomeType == IncomeType.SALARY) state.salary.toDoubleOrNull() ?: 0.0 else 0.0
        val annual = salaryAmt + bonusForYear + (state.commission.toDoubleOrNull() ?: 0.0)
        if (annual <= 0.0 && bonusAmt <= 0.0) return null

        val periods = state.payFrequency.cadence.periodsPerYear
        val t401k = state.t401k / 100.0
        val roth = state.roth / 100.0

        val med = (state.medical.toDoubleOrNull() ?: 0.0) * periods
        val dent = (state.dental.toDoubleOrNull() ?: 0.0) * periods
        val vis = (state.vision.toDoubleOrNull() ?: 0.0) * periods
        val fsaAmt = (state.fsa.toDoubleOrNull() ?: 0.0) * periods
        val preTaxBenefits = med + dent + vis + fsaAmt + (annual * t401k)

        val taxable = maxOf(0.0, annual - preTaxBenefits - STANDARD_DEDUCTION)
        var fed = 0.0
        var prev = 0.0
        var remaining = taxable
        for ((cap, rate) in FEDERAL_BRACKETS) {
            val slice = minOf(remaining, cap - prev)
            if (slice <= 0.0) break
            fed += slice * rate
            remaining -= slice
            prev = cap
            if (remaining <= 0.0) break
        }

        val stateTax = taxable * (STATE_RATES[state.stateCode] ?: DEFAULT_STATE_RATE)
        val ss = minOf(annual, SS_WAGE_BASE) * SS_RATE
        val medicare = annual * MEDICARE_RATE
        val rothAmt = annual * roth
        val totalTax = fed + stateTax + ss + medicare
        val netAnnual = annual - totalTax - preTaxBenefits - rothAmt

        return LivePreviewResult(
            perPeriod = netAnnual / periods,
            takeHomePct = (netAnnual / annual) * 100.0,
        )
    }
}
