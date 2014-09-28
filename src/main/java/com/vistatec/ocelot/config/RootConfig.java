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
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML Root Ocelot configuration element.
 */
@XmlRootElement(name = "ocelot")
public class RootConfig {
    @XmlElement
    protected PluginsConfig plugins = new PluginsConfig();
    @XmlElement
    protected DisplayConfig display = new DisplayConfig();

    public RootConfig() {
    }

    public PluginsConfig getPlugins() {
        return plugins;
    }

    public DisplayConfig getDisplay() {
        return display;
    }

    static RootConfig unmarshal(JAXBContext jaxb, Reader r) throws JAXBException {
        Unmarshaller unmarshal = jaxb.createUnmarshaller();
        return (RootConfig) unmarshal.unmarshal(r);
    }

    void marshal(JAXBContext jaxb, Writer w) throws JAXBException, IOException {
        Marshaller marshaller = jaxb.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(this, w);
        w.close();
    }
}
