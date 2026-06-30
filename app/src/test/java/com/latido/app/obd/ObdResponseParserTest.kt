package com.latido.app.obd

import com.latido.app.data.obd.ObdResponseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdResponseParserTest {

    @Test
    fun `parses stored DTCs from a spaced response`() {
        val codes = ObdResponseParser.parseDtcs("43 03 00 04 55 03 35\r>", 0x43)
        assertEquals(listOf("P0300", "P0455", "P0335"), codes)
    }

    @Test
    fun `parses DTCs with spaces off (ATS0)`() {
        val codes = ObdResponseParser.parseDtcs("43030004550335\r>", 0x43)
        assertEquals(listOf("P0300", "P0455", "P0335"), codes)
    }

    @Test
    fun `drops a leading count byte and ignores padding`() {
        // CAN single frame: 0x43, count(01), DTC 0133, then 0x00 padding.
        val codes = ObdResponseParser.parseDtcs("43 01 01 33 00 00 00 00\r>", 0x43)
        assertEquals(listOf("P0133"), codes)
    }

    @Test
    fun `strips a CAN header when headers are on (ATH1)`() {
        val codes = ObdResponseParser.parseDtcs("7E8 06 43 01 33 00 00 00 00\r>", 0x43)
        assertEquals(listOf("P0133"), codes)
    }

    @Test
    fun `ignores SEARCHING noise`() {
        val codes = ObdResponseParser.parseDtcs("SEARCHING...\r43 01 33\r>", 0x43)
        assertEquals(listOf("P0133"), codes)
    }

    @Test
    fun `treats NO DATA and question mark as no codes`() {
        assertTrue(ObdResponseParser.parseDtcs("NO DATA\r>", 0x43).isEmpty())
        assertTrue(ObdResponseParser.parseDtcs("?\r>", 0x43).isEmpty())
    }

    @Test
    fun `parses pending DTCs with service 0x47`() {
        assertEquals(listOf("P0171"), ObdResponseParser.parseDtcs("47 01 71\r>", 0x47))
    }

    @Test
    fun `parses a multi-frame VIN`() {
        val raw = buildString {
            append("014\r")
            append("0: 49 02 01 56 46 31\r")
            append("1: 52 46 42 30 30 30 47\r")
            append("2: 31 32 33 34 35 36 37\r\r>")
        }
        assertEquals("VF1RFB000G1234567", ObdResponseParser.parseVin(raw))
    }

    @Test
    fun `returns null VIN on error`() {
        assertNull(ObdResponseParser.parseVin("NO DATA\r>"))
    }

    @Test
    fun `extracts PID data for a freeze-frame RPM read`() {
        val data = ObdResponseParser.parsePidData("42 0C 0C D0\r>", 0x42, 0x0C)
        assertEquals(listOf(0x0C, 0xD0), data)
        // RPM = ((A*256)+B)/4
        val rpm = ((data!![0] * 256) + data[1]) / 4
        assertEquals(820, rpm)
    }
}
