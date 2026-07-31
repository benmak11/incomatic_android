package com.makusha.incomatic.equity

import com.makusha.incomatic.net.dto.RsuGrant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VestingPresetTest {

    @Test
    fun `fromPresetId round-trips every preset's own id`() {
        for (preset in VestingPreset.entries) {
            assertEquals(preset, VestingPreset.fromPresetId(preset.presetId))
        }
    }

    @Test
    fun `fromPresetId returns null for unknown or null ids`() {
        assertNull(VestingPreset.fromPresetId(null))
        assertNull(VestingPreset.fromPresetId("not-a-real-preset"))
    }

    @Test
    fun `label formats years, monthly cadence, and a year cliff`() {
        val schedule = RsuGrant.VestingSchedule(null, totalMonths = 48, cliffMonths = 12, freqMonths = 1)
        assertEquals("4-yr monthly · 1-yr cliff", schedule.label())
    }

    @Test
    fun `label formats non-year totals in months and omits an absent cliff`() {
        val schedule = RsuGrant.VestingSchedule(null, totalMonths = 50, cliffMonths = 0, freqMonths = 3)
        assertEquals("50-mo quarterly", schedule.label())
    }

    @Test
    fun `label formats an annual cadence`() {
        val schedule = RsuGrant.VestingSchedule(null, totalMonths = 48, cliffMonths = 0, freqMonths = 12)
        assertEquals("4-yr annual", schedule.label())
    }

    @Test
    fun `label falls back to a raw month count for a non-standard frequency and cliff`() {
        val schedule = RsuGrant.VestingSchedule(null, totalMonths = 48, cliffMonths = 18, freqMonths = 5)
        assertEquals("4-yr every 5 mo · 18-mo cliff", schedule.label())
    }
}
