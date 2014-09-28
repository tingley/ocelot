package com.vistatec.ocelot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.vistatec.ocelot.SegmentViewColumn;

public class TestRootConfig {
    JAXBContext jaxb;
    File temp;
    Writer w;

    @Before
    public void setup() throws Exception {
        jaxb = JAXBContext.newInstance(RootConfig.class);
        temp = File.createTempFile("ocelot", ".xml");
        w = new OutputStreamWriter(new FileOutputStream(temp), "UTF-8");
    }

    @After
    public void cleanup() throws Exception {
        temp.delete();
    }

    @Test
    public void testMarshallAndUnmarshall() throws Exception {

        RootConfig config = new RootConfig();
        config.getPlugins().addPluginConfig(new PluginConfig(new TestPlugin(), true));
        config.getDisplay().getColumns().setColumnEnabled(SegmentViewColumn.Original, false);

        RootConfig roundtrip = roundtrip(config);

        PluginsConfig pConfig = roundtrip.getPlugins();
        assertNotNull(pConfig);
        assertEquals(1, pConfig.plugins.size());
        assertEquals(TestPlugin.class.getName(), pConfig.plugins.get(0).getClassName());
        assertEquals(true, pConfig.plugins.get(0).getEnabled());

        DisplayConfig dConfig = roundtrip.getDisplay();
        ColumnsConfig cConfig = dConfig.getColumns();
        assertEquals(1, cConfig.columns.size());
        assertEquals(SegmentViewColumn.Original, cConfig.columns.get(0).getColumn());
        assertEquals(false, cConfig.columns.get(0).isEnabled());

        // Entries we didn't serialize should use their default values
        for (SegmentViewColumn col : SegmentViewColumn.values()) {
            if (col.equals(SegmentViewColumn.Original)) continue;
            assertEquals(col.isVisibleByDefaut(), cConfig.isEnabled(col));
        }
        
        assertTrue(temp.delete());
    }

    @Test
    public void testRoundtripDefaults() throws Exception {
        RootConfig roundtrip = roundtrip(new RootConfig());
        for (SegmentViewColumn col : SegmentViewColumn.values()) {
            assertEquals(col.isVisibleByDefaut(), roundtrip.getDisplay().getColumns().isEnabled(col));
        }
    }

    @Test
    public void testRoundtripColumnsEnabled() throws Exception {
        RootConfig config = new RootConfig();
        for (SegmentViewColumn col : SegmentViewColumn.values()) {
            config.getDisplay().getColumns().setColumnEnabled(col, true);
        }
        RootConfig roundtrip = roundtrip(config);
        for (SegmentViewColumn col : SegmentViewColumn.values()) {
            assertTrue(roundtrip.getDisplay().getColumns().isEnabled(col));
        }
    }

    @Test
    public void testRoundtripColumnsDisabled() throws Exception {
        RootConfig config = new RootConfig();
        for (SegmentViewColumn col : SegmentViewColumn.values()) {
            config.getDisplay().getColumns().setColumnEnabled(col, false);
        }
        RootConfig roundtrip = roundtrip(config);
        for (SegmentViewColumn col : SegmentViewColumn.values()) {
            assertFalse(roundtrip.getDisplay().getColumns().isEnabled(col));
        }
    }

    @Test
    public void testLoadColumnsEnabled() throws Exception {
        Reader r = getReader("/configs/enabled_columns.xml");
        RootConfig config = RootConfig.unmarshal(jaxb, r);
        for (SegmentViewColumn col : SegmentViewColumn.values()) {
            assertTrue(config.getDisplay().getColumns().isEnabled(col));
        }
        r.close();
    }

    @Test
    public void testLoadColumnsDisabled() throws Exception {
        Reader r = getReader("/configs/disabled_columns.xml");
        RootConfig config = RootConfig.unmarshal(jaxb, r);
        for (SegmentViewColumn col : SegmentViewColumn.values()) {
            assertFalse(config.getDisplay().getColumns().isEnabled(col));
        }
        r.close();
    }

    private Reader getReader(String resourceName) throws IOException {
        return new InputStreamReader(getClass().getResourceAsStream(resourceName), "UTF-8");
    }

    RootConfig roundtrip(RootConfig original) throws Exception {
        original.marshal(jaxb, w);
        Reader r = new InputStreamReader(new FileInputStream(temp), "UTF-8");
        RootConfig roundtrip = RootConfig.unmarshal(jaxb, r);
        r.close();
        return roundtrip;
    }
}
