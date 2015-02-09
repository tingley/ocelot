package com.vistatec.ocelot.config;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.*;

import com.vistatec.ocelot.its.Provenance;

import static org.junit.Assert.*;

public class TestProvenanceConfig {

    @Test
    public void testEmptyProvenance() {
        ProvenanceConfig config = new ProvenanceConfig(new ConfigsForProvTesting(null, null));
        assertTrue(config.isEmpty());
        assertTrue(config.getUserProvenance().isEmpty());
        
        config = new ProvenanceConfig(new ConfigsForProvTesting(null, null));
        assertTrue(config.isEmpty());
        assertTrue(config.getUserProvenance().isEmpty());
    }

    @Test
    public void testLoadProvenance() {
        ProvenanceConfig config = new ProvenanceConfig(new ConfigsForProvTesting(
                "externalReference=C\nrevOrganization=B\nrevPerson=A", null));
        assertFalse(config.isEmpty());
        Provenance prov = config.getUserProvenance();
        assertNotNull(prov);
        assertFalse(prov.isEmpty());
        assertEquals("A", prov.getRevPerson());
        assertEquals("B", prov.getRevOrg());
        assertEquals("C", prov.getProvRef());
    }

    @Test
    public void testSaveProvenance() throws IOException {
        StringWriter sw = new StringWriter();
        ProvenanceConfig config = new ProvenanceConfig(new ConfigsForProvTesting("", sw));
        Provenance prov = config.getUserProvenance();
        prov.setProvRef("D");
        prov.setRevPerson("E");
        prov.setRevOrg("F");
        config.save(prov);

        config = new ProvenanceConfig(new ConfigsForProvTesting(sw.toString(), null));
        assertFalse(config.isEmpty());
        Provenance roundtrip = config.getUserProvenance();
        assertEquals("D", roundtrip.getProvRef());
        assertEquals("E", roundtrip.getRevPerson());
        assertEquals("F", roundtrip.getRevOrg());
    }
}
