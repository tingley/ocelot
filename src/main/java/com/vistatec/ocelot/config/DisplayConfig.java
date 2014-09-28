package com.vistatec.ocelot.config;

import javax.xml.bind.annotation.XmlElement;

public class DisplayConfig {

    private ColumnsConfig columns = new ColumnsConfig();

    public DisplayConfig() { }

    @XmlElement
    public ColumnsConfig getColumns() {
        return columns;
    }

    public void setColumns(ColumnsConfig config) {
        this.columns = config;
    }
}
