package com.vistatec.ocelot.its;

import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;

import org.junit.*;

import static org.junit.Assert.*;

public class TestProvenance {

    @Test
    public void testNewProvenanceIsEmpty() {
        assertTrue(new Provenance().isEmpty());
    }

    @Test
    public void testOkapiXLIFF12ProvIsNotEmpty() {
        Provenance prov = ProvenanceFactory.fromOkapiXLIFF12Annotation(
                new GenericAnnotation(GenericAnnotationType.PROV,
                GenericAnnotationType.PROV_REVORG, "S",
                GenericAnnotationType.PROV_REVPERSON, "T",
                GenericAnnotationType.PROV_PROVREF, "X"));
        assertFalse(prov.isEmpty());
    }

    @Test
    public void testUserProvIsNotEmpty() {
        assertFalse(ProvenanceFactory.fromUserFields("A", "B", "C").isEmpty());
    }

    @Test
    public void testUserProvIsEmpty() {
        assertTrue(ProvenanceFactory.fromUserFields(null, null, null).isEmpty());
    }
}
