package com.vistatec.ocelot.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.vistatec.ocelot.SegmentViewColumn;

public class ColumnsConfig {

    @XmlElement(name = "column")
    List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

    public void setColumnEnabled(SegmentViewColumn column, boolean enabled) {
        ColumnConfig config = findConfig(column);
        if (config == null) {
            config = new ColumnConfig(column, enabled);
            columns.add(config);
        }
        config.setEnabled(enabled);
    }

    public EnumMap<SegmentViewColumn, Boolean> getColumnMap() {
        EnumMap<SegmentViewColumn, Boolean> map =
                new EnumMap<SegmentViewColumn, Boolean>(SegmentViewColumn.class);
        for (SegmentViewColumn c : SegmentViewColumn.values()) {
            map.put(c, isEnabled(c));
        }
        return map;
    }

    private ColumnConfig findConfig(SegmentViewColumn col) {
        for (ColumnConfig config : columns) {
            if (config.getColumn().equals(col)) {
                return config;
            }
        }
        return null;
    }

    /**
     * Return the enabled state of the given column.  If the configuration
     * contains no state for the column, return the default enabled value
     * for that column.
     * @param column
     * @return
     */
    public boolean isEnabled(SegmentViewColumn column) {
        for (ColumnConfig cc : columns) {
            if (cc.getColumn().equals(column)) {
                return cc.isEnabled();
            }
        }
        return column.isVisibleByDefaut();
    }
}
