package com.makusha.incomatic.net.dto

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedSessionTest {

    private fun summary(
        stateName: String? = null,
        stateCode: String? = null,
        country: String? = null,
        cadence: String? = null,
        savedAt: String = "2025-06-07T12:00:00Z",
    ) = SavedCalculationSummary(
        id = "id",
        savedAt = savedAt,
        country = country,
        stateCode = stateCode,
        stateName = stateName,
        cadence = cadence,
    )

    @Test
    fun `displayTitle prefers stateName, then stateCode, then country, then a fallback`() {
        assertEquals("California", summary(stateName = "California", stateCode = "CA").displayTitle)
        assertEquals("CA", summary(stateCode = "CA", country = "US").displayTitle)
        assertEquals("US", summary(country = "US").displayTitle)
        assertEquals("Calculation", summary().displayTitle)
    }

    @Test
    fun `displaySubtitle expands US and joins with the middle dot only when both parts exist`() {
        assertEquals("United States · Bi-weekly", summary(country = "US", cadence = "BIWEEKLY").displaySubtitle)
        assertEquals("United States", summary(country = "US", cadence = null).displaySubtitle)
        assertEquals("Monthly", summary(country = null, cadence = "MONTHLY").displaySubtitle)
    }

    @Test
    fun `displaySubtitle title-cases an unrecognized cadence as a fallback`() {
        assertEquals("Foo", summary(cadence = "FOO").displaySubtitle)
    }

    @Test
    fun `savedAtCompact omits the year for a same-calendar-year timestamp`() {
        val now = OffsetDateTime.now()
        val expected = now.atZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d"))
        assertEquals(expected, summary(savedAt = now.toString()).savedAtCompact)
    }

    @Test
    fun `savedAtCompact includes the year for a prior-calendar-year timestamp`() {
        val lastYear = OffsetDateTime.now().minusYears(1)
        val expected = lastYear.atZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        assertEquals(expected, summary(savedAt = lastYear.toString()).savedAtCompact)
    }

    @Test
    fun `savedAtCompact falls back to the raw string when it can't be parsed`() {
        assertEquals("not-a-date", summary(savedAt = "not-a-date").savedAtCompact)
    }
}
