package com.vistatec.ocelot.config;

import java.util.EnumMap;

import org.junit.*;

import com.vistatec.ocelot.SegmentViewColumn;

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

    @Test
    public void testGetColumnsMap() {
        ColumnsConfig columns = new ColumnsConfig();
        EnumMap<SegmentViewColumn, Boolean> map = columns.getColumnMap();
        assertNotNull(map);
        for (SegmentViewColumn c : SegmentViewColumn.values()) {
            assertTrue(map.containsKey(c));
            assertEquals(c.isVisibleByDefaut(), map.get(c));
        }
    }
}
