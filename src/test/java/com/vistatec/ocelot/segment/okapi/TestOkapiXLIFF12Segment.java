package com.vistatec.ocelot.segment.okapi;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.*;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.vistatec.ocelot.config.ConfigsForProvTesting;
import com.vistatec.ocelot.config.ProvenanceConfig;
import com.vistatec.ocelot.its.stats.ITSDocStats;
import com.vistatec.ocelot.segment.BaseSegmentVariant;
import com.vistatec.ocelot.segment.CodeAtom;
import com.vistatec.ocelot.segment.SegmentAtom;
import com.vistatec.ocelot.segment.SegmentController;
import com.vistatec.ocelot.segment.TextAtom;

import static org.junit.Assert.*;

public class TestOkapiXLIFF12Segment {
    private static SegmentController segController;
    @BeforeClass
    public static void setup() {
        segController = new SegmentController(
                new OkapiXLIFFFactory(), new EventBus(), new ITSDocStats(),
                new ProvenanceConfig(new ConfigsForProvTesting("revPerson=q", null)));
    }

    private TextContainer sampleTextTC() {
        TextContainer tc = new TextContainer();
        TextFragment tf = tc.getFirstContent();
        tf.append("A");
        tf.append(new Code(TagType.OPENING, "b", "<b id=\"1\">"));
        tf.append("B");
        tf.append(new Code(TagType.CLOSING, "b", "</b>"));
        return tc;
    }

    private TextContainer plainTextTC() {
        TextContainer tc = new TextContainer();
        TextFragment tf = tc.getFirstContent();
        tf.append("Plain text");
        return tc;
    }


    @Test
    public void testUpdateTarget() {
        TextUnit tu = new TextUnit("tu1");
        tu.setSource(plainTextTC());
        tu.setTarget(LocaleId.FRENCH, plainTextTC());
        OkapiXLIFF12Segment segment = new OkapiXLIFF12Segment(1, tu,
                                LocaleId.ENGLISH, LocaleId.FRENCH);
        segment.setSegmentController(segController);
        segment.updateTarget(new BaseSegmentVariant(
                Lists.newArrayList((SegmentAtom)new TextAtom("hello"))));
        TextContainer tcTarget = tu.getTarget(LocaleId.FRENCH);
        assertEquals("hello", tcTarget.toString());
        assertEquals("hello", segment.getTarget().toString());
        assertEquals("Plain text", segment.getOriginalTarget().toString());
        TextContainer tcOrigTarget = new OkapiXLIFF12SegmentHelper(LocaleId.FRENCH)
                    .retrieveOriginalTarget(tcTarget);
        assertEquals("Plain text", tcOrigTarget.toString());
    }

    @Test
    public void testUpdateTargetWithCodes() {
        TextUnit tu = new TextUnit("tu1");
        tu.setSource(sampleTextTC());
        tu.setTarget(LocaleId.FRENCH, sampleTextTC());
        OkapiXLIFF12Segment segment = new OkapiXLIFF12Segment(1, tu,
                                LocaleId.ENGLISH, LocaleId.FRENCH);
        segment.setSegmentController(segController);

        List<SegmentAtom> updatedAtoms = new ArrayList<SegmentAtom>();
        updatedAtoms.add(new CodeAtom("0", "<b>", "<b id=\"1\">"));
        updatedAtoms.add(new TextAtom("C"));
        updatedAtoms.add(new CodeAtom("1", "</b>", "</b>"));
        updatedAtoms.add(new TextAtom("D"));
        segment.updateTarget(new BaseSegmentVariant(updatedAtoms));
        TextContainer tcTarget = tu.getTarget(LocaleId.FRENCH);
        assertEquals("<b id=\"1\">C</b>D", tcTarget.toString());
        assertEquals("<b>C</b>D", segment.getTarget().toString());
        assertEquals("A<b>B</b>", segment.getOriginalTarget().toString());
        TextContainer tcOrigTarget = new OkapiXLIFF12SegmentHelper(LocaleId.FRENCH)
                    .retrieveOriginalTarget(tcTarget);
        assertEquals("A<b id=\"1\">B</b>", tcOrigTarget.toString());
    }
}
