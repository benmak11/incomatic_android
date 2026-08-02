package com.makusha.incomatic.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun `formatMoney keeps two decimals by default`() {
        assertEquals("$1,234.50", formatMoney(1234.5))
    }

    @Test
    fun `formatMoney with cents false rounds to whole dollars`() {
        assertEquals("$1,235", formatMoney(1234.5, cents = false))
    }

    @Test
    fun `formatMoney handles zero`() {
        assertEquals("$0.00", formatMoney(0.0))
    }

    @Test
    fun `formatMoney adds thousands separators`() {
        assertEquals("$12,345.67", formatMoney(12345.67))
    }

    @Test
    fun `formatShares renders an integral value with no decimal`() {
        assertEquals("400", formatShares(400.0))
    }

    @Test
    fun `formatShares renders a fractional value with one decimal`() {
        assertEquals("37.5", formatShares(37.5))
    }

    @Test
    fun `sanitizeCurrency strips non-digit non-dot characters`() {
        assertEquals("120000", sanitizeCurrency("$120,000"))
        assertEquals("120000", sanitizeCurrency("120abc000"))
    }

    @Test
    fun `sanitizeCurrency drops a second decimal point but keeps digits after it`() {
        assertEquals("1.56", sanitizeCurrency("1.5.6"))
    }

    @Test
    fun `sanitizeCurrency caps fractional digits at two`() {
        assertEquals("1.23", sanitizeCurrency("1.2345"))
    }

    @Test
    fun `sanitizeCurrency preserves a trailing dot mid-typing`() {
        assertEquals("120.", sanitizeCurrency("120."))
    }

    @Test
    fun `groupThousands inserts separators crossing multiple groups`() {
        assertEquals("120,000", groupThousands("120000"))
        assertEquals("1,234,567", groupThousands("1234567"))
    }

    @Test
    fun `groupThousands leaves short values ungrouped`() {
        assertEquals("120", groupThousands("120"))
    }

    @Test
    fun `groupThousands preserves a trailing dot and partial decimals mid-typing`() {
        assertEquals("1,234.", groupThousands("1234."))
        assertEquals("1,234.5", groupThousands("1234.5"))
    }

    @Test
    fun `groupThousands handles empty input`() {
        assertEquals("", groupThousands(""))
    }
}
