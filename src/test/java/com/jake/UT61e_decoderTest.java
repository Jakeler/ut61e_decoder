package com.jake;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test different inputs
 */
public class UT61e_decoderTest {
    @Test
    public void parseVoltageDC() throws Exception {
        UT61e_decoder data = new UT61e_decoder();
        byte[] testInput = {52, 50, 50, -75, 56, -80, 59, 49, -80, -80, 56, -80, 13, -118};
        assertEquals(data.parse(testInput), true);
        assertEquals(data.getValue(), 225.8, 0.0001);
        assertEquals(data.isOL(), true);
        assertEquals(data.getMode(), data.MODE_VOLTAGE);
        assertEquals(data.isDC(), true);
        assertEquals(data.isFreq(), false);
        assertEquals(data.isDuty(), false);
    }

    @Test
    public void parseCapacity() throws Exception {
        UT61e_decoder data = new UT61e_decoder();
        byte[] testInput = {-80, -80, -80, -77, 50, -80, -74, -80, -80, -80, 50, -80, 13, -118};
        assertEquals(data.parse(testInput), true);
        assertEquals(data.getValue(), 0.320, 0.0001);
        assertEquals(data.isOL(), false);
        assertEquals(data.getMode(), data.MODE_CAPACITANCE);
        assertEquals(data.isDC(), false);
        assertEquals(data.isFreq(), false);
        assertEquals(data.isDuty(), false);
    }

}