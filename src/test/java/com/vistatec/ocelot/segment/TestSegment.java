package com.vistatec.ocelot.segment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.vistatec.ocelot.config.ConfigsForProvTesting;
import com.vistatec.ocelot.config.ProvenanceConfig;
import com.vistatec.ocelot.its.stats.ITSDocStats;
import com.vistatec.ocelot.segment.okapi.OkapiXLIFFFactory;

/**
 * Other tests in SegmentController are also relevant to Segment
 * functionality.
 */
public class TestSegment {
    private static SegmentController segController;
    @BeforeClass
    public static void setup() {
        segController = new SegmentController(
                new OkapiXLIFFFactory(), new EventBus(), new ITSDocStats(),
                new ProvenanceConfig(new ConfigsForProvTesting("revPerson=q", null)));
    }

    @Test
    public void testMultipleSegmentUpdates() throws Exception {
        OcelotSegment seg = newSegment();
        SegmentVariant originalTarget = seg.getTarget();
        SegmentVariant newTarget1 = textVariant("update1");
        seg.updateTarget(newTarget1);
        assertEquals(newTarget1, seg.getTarget());
        assertEquals(originalTarget, seg.getOriginalTarget());
        SegmentVariant newTarget2 = textVariant("update2");
        seg.updateTarget(newTarget2);
        assertEquals(newTarget2, seg.getTarget());
        // Original target is still the -original- target.
        assertEquals(originalTarget, seg.getOriginalTarget());
    }

    @Test
    public void testResetTarget() {
        OcelotSegment seg = newSegment();
        seg.updateTarget(textVariant("update"));
        assertTrue(seg.hasOriginalTarget());
        assertEquals("update", seg.getTarget().getDisplayText());
        assertNotNull(seg.getOriginalTarget());
        assertEquals("target", seg.getOriginalTarget().getDisplayText());
        seg.resetTarget();
        assertEquals("target", seg.getTarget().getDisplayText());
        assertTrue(seg.hasOriginalTarget());
        assertNotNull(seg.getOriginalTarget());
        assertEquals("target", seg.getTarget().getDisplayText());
        // Make sure the target diff got reset. XXX This has an ugly
        // dependency on the diff language.
        assertEquals(Lists.newArrayList("target", "regular"), seg.getTargetDiff());
    }

    @Test
    public void testResetWithNoOriginalTarget() {
        OcelotSegment seg = newSegment();
        seg.resetTarget();
        assertEquals("target", seg.getTarget().getDisplayText());
    }

    @Test
    public void testTargetChangesAffectEditDistance() {
        OcelotSegment seg = newSegment();
        seg.updateTarget(textVariant("targetA"));
        assertEquals(1, seg.getEditDistance());
        seg.updateTarget(textVariant("targetAB"));
        assertEquals(2, seg.getEditDistance());
        seg.resetTarget();
        assertEquals(0, seg.getEditDistance());
    }

    private static int nextSegmentId = 1;
    public static OcelotSegment newSegment() {
        int id = nextSegmentId++;
        OcelotSegment seg = new SimpleSegment(id, textVariant("source"),
                textVariant("target"), null);
        seg.setSegmentController(segController);
        return seg;
    }

    private static BaseSegmentVariant textVariant(String text) {
        return new BaseSegmentVariant(Lists.newArrayList((SegmentAtom)new TextAtom(text)));
    }
}
