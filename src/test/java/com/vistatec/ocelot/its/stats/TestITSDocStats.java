package com.vistatec.ocelot.its.stats;

import java.util.Arrays;
import java.util.Collections;

import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;

import org.junit.*;

import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.its.ProvenanceFactory;
import com.vistatec.ocelot.its.stats.ProvenanceStats.Type;

import static org.junit.Assert.*;

public class TestITSDocStats {

    @Test
    public void testAddLQI() {
        ITSDocStats docStats = new ITSDocStats();
        docStats.addLQIStats(getLQI("omission", 50));
        assertEquals(Collections.singletonList(getLQIStats(1, "omission", 50)), docStats.getStats());
        assertEquals((Integer)1, docStats.getStats().get(0).getCount());
        docStats.addLQIStats(getLQI("omission", 70));
        assertEquals(Collections.singletonList(getLQIStats(2, "omission", 50, 70)), docStats.getStats());
        assertEquals((Integer)2, docStats.getStats().get(0).getCount());
        docStats.addLQIStats(getLQI("omission", 30));
        assertEquals(Collections.singletonList(getLQIStats(3, "omission", 30, 70)), docStats.getStats());
        docStats.addLQIStats(getLQI("mistranslation", 80));
        assertEquals(Arrays.asList(getLQIStats(3, "omission", 30, 70), getLQIStats(1, "mistranslation", 80)), docStats.getStats());
    }

    @Test
    public void testAddProvenance() {
        ITSDocStats docStats = new ITSDocStats();
        // XXX Bit of a cheat here, I'm assuming the order that the
        // stats are added to the docStats object
        docStats.addProvenanceStats(ProvenanceFactory.fromOkapiXLIFF12Annotation(
                new GenericAnnotation(GenericAnnotationType.PROV,
                GenericAnnotationType.PROV_PERSON, "testPerson",
                GenericAnnotationType.PROV_ORG, "testOrg",
                GenericAnnotationType.PROV_TOOL, "testTool")));
        assertEquals(Arrays.asList(getProvStats(Type.person, "testPerson", 1),
                                   getProvStats(Type.org, "testOrg", 1),
                                   getProvStats(Type.tool, "testTool", 1)),
                     docStats.getStats());
        docStats.addProvenanceStats(ProvenanceFactory.fromOkapiXLIFF12Annotation(
                new GenericAnnotation(GenericAnnotationType.PROV,
                GenericAnnotationType.PROV_PERSON, "testPerson",
                GenericAnnotationType.PROV_ORG, "testOrg",
                GenericAnnotationType.PROV_TOOL, "testTool",
                GenericAnnotationType.PROV_REVPERSON, "testRevPerson",
                GenericAnnotationType.PROV_REVORG, "testRevOrg",
                GenericAnnotationType.PROV_REVTOOL, "testRevTool")));
        assertEquals(Arrays.asList(
                            getProvStats(Type.person, "testPerson", 2),
                            getProvStats(Type.org, "testOrg", 2),
                            getProvStats(Type.tool, "testTool", 2),
                            getProvStats(Type.revPerson, "testRevPerson", 1),
                            getProvStats(Type.revOrg, "testRevOrg", 1),
                            getProvStats(Type.revTool, "testRevTool", 1)),
                     docStats.getStats());
    }

    @Test
    public void testClearStats() {
        ITSDocStats docStats = new ITSDocStats();
        docStats.addLQIStats(getLQI("omission", 50));
        assertEquals(Collections.singletonList(getLQIStats(1, "omission", 50)), docStats.getStats());
        docStats.clear();
        assertEquals(Collections.emptyList(), docStats.getStats());
    }

    private LanguageQualityIssue getLQI(String type, int severity) {
        LanguageQualityIssue lqi = new LanguageQualityIssue();
        lqi.setType(type);
        lqi.setSeverity(severity);
        return lqi;
    }

    private LanguageQualityIssueStats getLQIStats(int count, String type, double severity) {
        LanguageQualityIssueStats lqiStats = new LanguageQualityIssueStats();
        lqiStats.setType(type);
        lqiStats.setRange(severity);
        lqiStats.setCount(count);
        return lqiStats;
    }

    private LanguageQualityIssueStats getLQIStats(int count, String type, double min, double max) {
        LanguageQualityIssueStats lqiStats = new LanguageQualityIssueStats();
        lqiStats.setType(type);
        lqiStats.setRange(min);
        lqiStats.setRange(max);
        lqiStats.setCount(count);
        return lqiStats;
    }

    private ProvenanceStats getProvStats(Type type, String value, int count) {
        ProvenanceStats stats = new ProvenanceStats(type, value);
        stats.setCount(count);
        return stats;
    }
}
