/*
 * Copyright (C) 2014, VistaTEC or third-party contributors as indicated
 * by the @author tags or express copyright attribution statements applied by
 * the authors. All third-party contributions are distributed under license by
 * VistaTEC.
 *
 * This file is part of Ocelot.
 *
 * Ocelot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ocelot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, write to:
 *
 *     Free Software Foundation, Inc.
 *     51 Franklin Street, Fifth Floor
 *     Boston, MA 02110-1301
 *     USA
 *
 * Also, see the full LGPL text here: <http://www.gnu.org/copyleft/lesser.html>
 */
package com.vistatec.ocelot.config;

import java.io.IOException;
import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vistatec.ocelot.events.ConfigurationChangedEvent;

/**
 * Ocelot application configuration preferences persistence class.
 * Handles the marshalling and unmarshalling of configuration data to/from the
 * XML config file.
 *
 * The configuration will save itself to disk upon receiving
 * {@link ConfigurationChangedEvent}.
 */
public class AppConfig {
    private Logger LOG = LoggerFactory.getLogger(AppConfig.class);
    protected Configs configs;
    protected JAXBContext jaxb;
    protected RootConfig config = new RootConfig();

    /**
     * Create an empty configuration.
     */
    public AppConfig() {}

    @Subscribe
    public void handleConfigurationChange(ConfigurationChangedEvent e) {
        save();
    }

    /**
     * Create a configuration based on the specified files.
     * @param configs
     */
    public AppConfig(EventBus eventBus, Configs configs) {
        eventBus.register(this);
        this.configure(configs);
    }

    /**
     * Get the current columns configuration. 
     * @return columns configuration
     */
    public ColumnsConfig getColumnsConfig() {
        return config.getDisplay().getColumns();
    }

    /**
     * Get the current plugins configuration.
     * @return plugins configuration
     */
    public PluginsConfig getPluginsConfig() {
        return config.getPlugins();
    }

    public void configure(Configs configs) {
        this.configs = configs;
        try {
            jaxb = JAXBContext.newInstance(RootConfig.class);
            Reader r = configs.getOcelotReader();
            if (r == null) {
                config = new RootConfig();
                config.marshal(jaxb, configs.getOcelotWriter());
            } else {
                config = RootConfig.unmarshal(jaxb, r);
            }
        } catch (JAXBException ex) {
            LOG.error("Exception handling JAXB content", ex);
        } catch (IOException ex) {
            LOG.error("Failed to create config file", ex);
        }
    }

    public void save() {
        try {
            config.marshal(jaxb, configs.getOcelotWriter());
        } catch (Exception ex) {
            LOG.error("Failed to save configuration", ex);
        }
    }
}
