package com.vistatec.ocelot.config;

import com.vistatec.ocelot.plugins.Plugin;

public class TestPlugin implements Plugin {

    @Override
    public String getPluginName() {
        return "Test Plugin";
    }

    @Override
    public String getPluginVersion() {
        return "1.0";
    }

}
