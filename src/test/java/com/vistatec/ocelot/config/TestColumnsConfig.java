package com.vistatec.ocelot.config;

import org.junit.*;

import static com.vistatec.ocelot.SegmentViewColumn.*;

import static org.junit.Assert.*;

public class TestColumnsConfig {

    @Test
    public void testSetEnabled() {
        ColumnsConfig columns = new ColumnsConfig();
        assertEquals(EditDistance.isVisibleByDefaut(), columns.isEnabled(EditDistance));
        columns.setColumnEnabled(EditDistance, true);
        assertTrue(columns.isEnabled(EditDistance));
        columns.setColumnEnabled(EditDistance, false);
        assertFalse(columns.isEnabled(EditDistance));
    }
}
