package com.vistatec.ocelot.config;

import javax.xml.bind.annotation.XmlAttribute;

import com.vistatec.ocelot.SegmentViewColumn;

public class ColumnConfig {
    private SegmentViewColumn column;

    private boolean enabled;

    public ColumnConfig() { }

    public ColumnConfig(SegmentViewColumn column, boolean enabled) {
        this.column = column;
        this.enabled = enabled;
    }
    
    @XmlAttribute(name = "original")
    public SegmentViewColumn getColumn() {
        return column;
    }

    public void setColumn(SegmentViewColumn column) {
        this.column = column;
    }

    @XmlAttribute(name = "enabled")
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
