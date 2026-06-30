package com.latido.app.obd

import com.latido.app.data.obd.DtcDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DtcDecoderTest {

    @Test
    fun `decodes powertrain codes`() {
        assertEquals("P0300", DtcDecoder.decode(0x03, 0x00))
        assertEquals("P0133", DtcDecoder.decode(0x01, 0x33))
        assertEquals("P0455", DtcDecoder.decode(0x04, 0x55))
        assertEquals("P0335", DtcDecoder.decode(0x03, 0x35))
        assertEquals("P0171", DtcDecoder.decode(0x01, 0x71))
    }

    @Test
    fun `decodes the four system letters from the top two bits`() {
        assertEquals("C0300", DtcDecoder.decode(0x43, 0x00)) // 01 -> C
        assertEquals("B0001", DtcDecoder.decode(0x80, 0x01)) // 10 -> B
        assertEquals("U0100", DtcDecoder.decode(0xC1, 0x00)) // 11 -> U
    }

    @Test
    fun `recognises all-zero padding`() {
        assertTrue(DtcDecoder.isPadding(0x00, 0x00))
        assertFalse(DtcDecoder.isPadding(0x03, 0x00))
    }
}
