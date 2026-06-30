package com.latido.app.obd

import com.latido.app.data.obd.VinDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VinDecoderTest {

    @Test
    fun `decodes make and year from a Renault VIN`() {
        val id = VinDecoder.decode("VF1RFB000G1234567")
        assertEquals("Renault", id.make)
        assertEquals(2016, id.year)
    }

    @Test
    fun `decodes a Volkswagen VIN year code A as 2010`() {
        val id = VinDecoder.decode("WVWZZZ1KZAW123456")
        assertEquals("Volkswagen", id.make)
        assertEquals(2010, id.year)
    }

    @Test
    fun `returns nulls for a missing or malformed VIN`() {
        val none = VinDecoder.decode(null)
        assertNull(none.make)
        assertNull(none.year)
        assertNull(VinDecoder.decode("TOO-SHORT").year)
    }
}
