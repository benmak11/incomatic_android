package com.makusha.incomatic.calculator

import com.makusha.incomatic.net.dto.FilingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorStateTest {

    @Test
    fun `filingStatusLabel maps every status to its expected copy`() {
        assertEquals("Single or married filing separately", filingStatusLabel(FilingStatus.SINGLE))
        assertEquals("Married filing jointly", filingStatusLabel(FilingStatus.MARRIED))
        assertEquals("Head of household", filingStatusLabel(FilingStatus.HEAD_OF_HOUSEHOLD))
    }
}
