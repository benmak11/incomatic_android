package com.makusha.incomatic.equity

import com.makusha.incomatic.net.dto.RsuGrant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VestMathTest {

    private fun grant(
        sharesTotal: Double,
        pricePerShare: Double = 10.0,
        grantDate: String = "2025-01-01",
        totalMonths: Int,
        cliffMonths: Int,
        freqMonths: Int,
        presetId: String? = null,
    ) = RsuGrant(
        sharesTotal = sharesTotal,
        pricePerShare = pricePerShare,
        grantDate = grantDate,
        schedule = RsuGrant.VestingSchedule(presetId, totalMonths, cliffMonths, freqMonths),
    )

    @Test
    fun `vestEvents cliff slice releases cliffMonths worth of shares at once`() {
        val g = grant(sharesTotal = 480.0, totalMonths = 48, cliffMonths = 12, freqMonths = 1, presetId = "monthly1cliff")
        val events = VestMath.vestEvents(g)

        val cliff = events.first()
        assertTrue(cliff.isCliff)
        assertEquals(LocalDate.parse("2025-01-01").plusMonths(12), cliff.date)
        assertEquals(120.0, cliff.shares, 0.0)

        // 36 monthly slices of 10 shares each follow the cliff.
        val monthly = events.drop(1)
        assertEquals(36, monthly.size)
        assertTrue(monthly.none { it.isCliff })
        assertTrue(monthly.all { it.shares == 10.0 })
        assertEquals(LocalDate.parse("2025-01-01").plusMonths(48), monthly.last().date)
    }

    @Test
    fun `vestEvents with no cliff produces equal annual slices`() {
        val g = grant(sharesTotal = 480.0, totalMonths = 48, cliffMonths = 0, freqMonths = 12, presetId = "annual4")
        val events = VestMath.vestEvents(g)

        assertEquals(4, events.size)
        assertTrue(events.none { it.isCliff })
        assertTrue(events.all { it.shares == 120.0 })
        assertEquals(
            listOf(12L, 24L, 36L, 48L).map { LocalDate.parse("2025-01-01").plusMonths(it) },
            events.map { it.date },
        )
    }

    @Test
    fun `vestEvents last slice is a partial remainder when freq doesn't evenly divide`() {
        // total=50, cliff=10, freq=12: after the cliff, 12/12/12/4 remain (46->50 is only 4 months).
        val g = grant(sharesTotal = 500.0, totalMonths = 50, cliffMonths = 10, freqMonths = 12)
        val events = VestMath.vestEvents(g)

        assertEquals(100.0, events[0].shares, 0.0) // cliff: 10 months * 10 shares/mo
        assertEquals(120.0, events[1].shares, 0.0)
        assertEquals(120.0, events[2].shares, 0.0)
        assertEquals(120.0, events[3].shares, 0.0)
        assertEquals(40.0, events[4].shares, 0.0) // partial remainder: only 4 months left
        assertEquals(500.0, events.sumOf { it.shares }, 0.0001)
    }

    @Test
    fun `vestEvents returns empty list for an unparseable grant date`() {
        val g = grant(sharesTotal = 100.0, grantDate = "not-a-date", totalMonths = 12, cliffMonths = 0, freqMonths = 1)
        assertEquals(emptyList<VestMath.VestEvent>(), VestMath.vestEvents(g))
    }

    @Test
    fun `yearGroups groups events spanning a calendar year boundary`() {
        val g = grant(sharesTotal = 40.0, pricePerShare = 5.0, grantDate = "2025-11-01", totalMonths = 4, cliffMonths = 0, freqMonths = 1)
        val groups = VestMath.yearGroups(g)

        assertEquals(2, groups.size)
        val y2025 = groups.first { it.year == 2025 }
        assertEquals(10.0, y2025.shares, 0.0)
        assertEquals(1, y2025.vestCount)
        assertEquals(50.0, y2025.value, 0.0) // 10 shares * $5
        assertTrue(!y2025.hasCliff)

        val y2026 = groups.first { it.year == 2026 }
        assertEquals(30.0, y2026.shares, 0.0)
        assertEquals(3, y2026.vestCount)
    }

    @Test
    fun `yearGroups marks the cliff year as hasCliff`() {
        val g = grant(sharesTotal = 480.0, totalMonths = 48, cliffMonths = 12, freqMonths = 1, presetId = "monthly1cliff")
        val cliffYear = VestMath.yearGroups(g).first { it.year == 2026 }
        assertTrue(cliffYear.hasCliff)
    }

    @Test
    fun `value sums across multiple grants and ignores grants with no vests in the year`() {
        // Vests entirely in 2026.
        val vestingIn2026 = grant(sharesTotal = 40.0, pricePerShare = 5.0, grantDate = "2025-11-01", totalMonths = 4, cliffMonths = 0, freqMonths = 1)
        // Fully vested before 2026 — contributes nothing to value(2026, ...).
        val vestedBefore = grant(sharesTotal = 12.0, pricePerShare = 100.0, grantDate = "2020-01-01", totalMonths = 12, cliffMonths = 0, freqMonths = 1)

        val value = VestMath.value(2026, listOf(vestingIn2026, vestedBefore))
        assertEquals(30.0 * 5.0, value, 0.0) // only the 3 shares vesting in 2026 from the first grant
    }

    @Test
    fun `nextVest returns the first event strictly after the given date, else null`() {
        val g = grant(sharesTotal = 480.0, totalMonths = 48, cliffMonths = 0, freqMonths = 12, presetId = "annual4")
        val afterFirst = VestMath.nextVest(g, after = LocalDate.parse("2025-06-01"))
        assertEquals(LocalDate.parse("2025-01-01").plusMonths(12), afterFirst?.date)

        val afterAll = VestMath.nextVest(g, after = LocalDate.parse("2025-01-01").plusMonths(48))
        assertNull(afterAll)
    }

    @Test
    fun `finalVestYear returns the max last-vest year across grants, null when empty`() {
        val earlier = grant(sharesTotal = 12.0, grantDate = "2024-01-01", totalMonths = 12, cliffMonths = 0, freqMonths = 1)
        val later = grant(sharesTotal = 480.0, grantDate = "2025-01-01", totalMonths = 48, cliffMonths = 0, freqMonths = 12)

        assertEquals(2029, VestMath.finalVestYear(listOf(earlier, later)))
        assertNull(VestMath.finalVestYear(emptyList()))
    }
}
