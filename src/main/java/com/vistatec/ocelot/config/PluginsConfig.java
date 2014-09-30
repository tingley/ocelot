package com.vistatec.ocelot.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.vistatec.ocelot.plugins.Plugin;

@XmlRootElement
public class PluginsConfig {
    @XmlElement (name = "plugin")
    List<PluginConfig> plugins = new ArrayList<PluginConfig>();

    public void enablePlugin(Plugin plugin, boolean enabled) {
        PluginConfig pcfg = findPluginConfig(plugin);
        pcfg.setEnabled(enabled);
    }

   private PluginConfig findPluginConfig(Plugin plugin) {
        PluginConfig foundPluginConfig = null;
        for (PluginConfig pcfg : plugins) {
            if (pcfg.matches(plugin)) {
                foundPluginConfig = pcfg;
            }
        }
        if (foundPluginConfig == null) {
            foundPluginConfig = new PluginConfig(plugin, false);
            addPluginConfig(foundPluginConfig);
        }
        return foundPluginConfig;
    }

    public boolean isPluginEnabled(Plugin plugin) {
        PluginConfig pcfg = findPluginConfig(plugin);
        return pcfg.getEnabled();
    }

    public void addPluginConfig(PluginConfig pluginConfig) {
        plugins.add(pluginConfig);
    }
}
