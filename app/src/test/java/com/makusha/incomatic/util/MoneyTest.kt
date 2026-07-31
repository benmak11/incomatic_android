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
}
