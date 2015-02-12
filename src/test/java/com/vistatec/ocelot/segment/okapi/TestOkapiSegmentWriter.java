package com.vistatec.ocelot.segment.okapi;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.ITSProvenanceAnnotations;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.*;

import com.vistatec.ocelot.config.ProvenanceConfig;
import com.vistatec.ocelot.its.Provenance;
import com.vistatec.ocelot.its.ProvenanceFactory;

import static org.junit.Assert.*;

public class TestOkapiSegmentWriter {

    private OkapiXLIFF12Segment emptySegment() {
        return new OkapiXLIFF12Segment(1, new TextUnit(), LocaleId.ENGLISH, LocaleId.FRENCH);
    }

    @Test
    public void testMissingProvenance() {
        OkapiXLIFF12Segment seg = emptySegment();
        seg.addProvenance(ProvenanceFactory.fromOkapiXLIFF12Annotation(new GenericAnnotation(GenericAnnotationType.PROV,
                GenericAnnotationType.PROV_REVORG, "S",
                GenericAnnotationType.PROV_REVPERSON, "T",
                GenericAnnotationType.PROV_PROVREF, "X")));
        // pass empty provenance properties
        ProvenanceConfig config = new TestProvenanceConfig(null, null, null);
        Provenance prov = config.getUserProvenance();
        // OC-16: make sure this doesn't crash
        seg.addProvenance(prov);
        ITSProvenanceAnnotations provAnns = seg.getProvenanceAnnotations("RW1");
        // We shouldn't add a second annotation record for our empty user provenance
        assertEquals(1, provAnns.getAnnotations("its-prov").size());

        // Do it again, make sure it doesn't crash
        seg.addProvenance(prov);
        ITSProvenanceAnnotations provAnns2 = seg.getProvenanceAnnotations("RW1");
        assertEquals(1, provAnns2.getAnnotations("its-prov").size());
    }

    @Test
    public void testDontAddRedundantProvenance() throws Exception {
        OkapiXLIFF12Segment seg = emptySegment();
        seg.addProvenance(ProvenanceFactory.fromOkapiXLIFF12Annotation(new GenericAnnotation(GenericAnnotationType.PROV,
                GenericAnnotationType.PROV_REVORG, "S",
                GenericAnnotationType.PROV_REVPERSON, "T",
                GenericAnnotationType.PROV_PROVREF, "X")));
        ProvenanceConfig config = new TestProvenanceConfig("T", "S", "X");
        seg.addProvenance(config.getUserProvenance());
        ITSProvenanceAnnotations provAnns = seg.getProvenanceAnnotations("RW1");
        assertEquals(1, provAnns.getAnnotations("its-prov").size());
    }

    @Test
    public void testAddUserProvenance() throws Exception {
        OkapiXLIFF12Segment seg = emptySegment();
        seg.addProvenance(ProvenanceFactory.fromOkapiXLIFF12Annotation(new GenericAnnotation(GenericAnnotationType.PROV,
                GenericAnnotationType.PROV_REVORG, "S",
                GenericAnnotationType.PROV_REVPERSON, "T",
                GenericAnnotationType.PROV_PROVREF, "X")));
        ProvenanceConfig config = new TestProvenanceConfig("A", "B", "C");
        seg.addProvenance(config.getUserProvenance());
        ITSProvenanceAnnotations provAnns = seg.getProvenanceAnnotations("RW1");
        assertEquals(2, provAnns.getAnnotations("its-prov").size());
        GenericAnnotation origAnno = provAnns.getAnnotations("its-prov").get(0);
        assertEquals("T", origAnno.getString(GenericAnnotationType.PROV_REVPERSON));
        assertEquals("S", origAnno.getString(GenericAnnotationType.PROV_REVORG));
        assertEquals("X", origAnno.getString(GenericAnnotationType.PROV_PROVREF));
        GenericAnnotation userAnno = provAnns.getAnnotations("its-prov").get(1);
        assertEquals("A", userAnno.getString(GenericAnnotationType.PROV_REVPERSON));
        assertEquals("B", userAnno.getString(GenericAnnotationType.PROV_REVORG));
        assertEquals("C", userAnno.getString(GenericAnnotationType.PROV_PROVREF));
    }

    class TestProvenanceConfig extends ProvenanceConfig {
        private String revPerson, revOrg, extRef;
        public TestProvenanceConfig(String revPerson, String revOrg, String extRef) {
            super();
            this.revPerson = revPerson;
            this.revOrg = revOrg;
            this.extRef = extRef;
        }
        @Override
        protected String getRevPerson() {
            return revPerson;
        }
        @Override
        protected String getRevOrg() {
            return revOrg;
        }
        @Override
        protected String getExternalReference() {
            return extRef;
        }
    }
}
