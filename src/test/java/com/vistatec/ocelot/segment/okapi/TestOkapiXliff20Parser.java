package com.vistatec.ocelot.segment.okapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.vistatec.ocelot.segment.CodeAtom;
import com.vistatec.ocelot.segment.Segment;
import com.vistatec.ocelot.segment.SegmentAtom;
import com.vistatec.ocelot.segment.SegmentVariant;
import com.vistatec.ocelot.segment.SimpleSegmentVariant;
import com.vistatec.ocelot.segment.TextAtom;

public class TestOkapiXliff20Parser {

    @Test
    public void testParser() throws URISyntaxException, IOException {
        File testFile = new File(TestOkapiXliff20Parser.class.getResource(
                "XLIFF2.0_example.xlf").toURI());
        OkapiXLIFF20Parser parser = new OkapiXLIFF20Parser();
        List<Segment> testSegments = parser.parse(testFile);
        List<Segment> goalSegments = getGoalSegments();
        assertTrue(testSegments.size() > 0);
        compareSegmentsIgnoringWhitespace(testSegments, goalSegments);
    }

    @Test
    public void testTagParser() throws URISyntaxException, IOException {
        File testFile = new File(TestOkapiXliff20Parser.class.getResource(
                "LQE_xliff_2.0.xlf").toURI());
        OkapiXLIFF20Parser parser = new OkapiXLIFF20Parser();
        List<Segment> testSegments = parser.parse(testFile);
        List<Segment> goalSegments = getTagGoalSegments();
        compareSegmentsIgnoringWhitespace(testSegments, goalSegments);
    }

    public void compareSegmentsIgnoringWhitespace(List<Segment> testSegs, List<Segment> goalSegs) {
        Iterator<Segment> testIter = testSegs.iterator();
        Iterator<Segment> goalIter = goalSegs.iterator();
        while (testIter.hasNext()) {
            Segment testSeg = testIter.next();
            Segment goalSeg = goalIter.next();
            assertEquals(goalSeg.getSource().getDisplayText().replaceAll("\\s", ""),
                    testSeg.getSource().getDisplayText().replaceAll("\\s", ""));
            assertEquals(goalSeg.getTarget().getDisplayText().replaceAll("\\s", ""),
                    testSeg.getTarget().getDisplayText().replaceAll("\\s", ""));
        }
        assertFalse(goalIter.hasNext());
    }

    public List<Segment> getGoalSegments() {
        List<Segment> segs = new ArrayList<>();
        segs.add(new Segment(1, 1, 1,
                new SimpleSegmentVariant("Sentence 1. Sentence 2."),
                new SimpleSegmentVariant(""),
                new SimpleSegmentVariant("")));
        segs.add(new Segment(2, 2, 2,
                new SimpleSegmentVariant("Sentence 3 (no-trans). Sentence 4 (no-trans)."),
                new SimpleSegmentVariant(""),
                new SimpleSegmentVariant("")));
        segs.add(new Segment(3, 3, 3,
                new SimpleSegmentVariant("Sentence 5."),
                new SimpleSegmentVariant(""),
                new SimpleSegmentVariant("")));
        segs.add(new Segment(4, 4, 4,
                new SimpleSegmentVariant("Sentence 6 (no-trans)."),
                new SimpleSegmentVariant(""),
                new SimpleSegmentVariant("")));
        segs.add(new Segment(5, 5, 5,
                new SimpleSegmentVariant("Sentence 7. Sentence 8. "),
                new SimpleSegmentVariant(""),
                new SimpleSegmentVariant("")));
        segs.add(new Segment(6, 6, 6,
                new SimpleSegmentVariant("Sentence with A. Sentence with <cp hex=\"0001\"/>. "),
                new SimpleSegmentVariant("Sentence with A. Sentence with <cp hex=\"0001\"/>. "),
                new SimpleSegmentVariant("")));
        return segs;
    }

    public List<Segment> getTagGoalSegments() {
        List<Segment> segs = new ArrayList<>();

        SegmentBuilder seg1 = new SegmentBuilder();
        seg1.source().text("Sentence 1.").code("1", "<mrk>", "<mrk id=\"1\" type=\"its:its\" translate=\"no\">")
                .text("LQI").code("1", "</mrk>", "</mrk>").text(" Sentence 2.");
        seg1.target().text("Sentence 1.").code("1", "<mrk>", "<mrk id=\"1\" type=\"its:its\" translate=\"no\">")
                .text("Prov").code("1", "</mrk>", "</mrk>").text(" Sentence 2.");
        seg1.originalTarget();
        segs.add(seg1.build());

        SegmentBuilder seg2 = new SegmentBuilder();
        seg2.source().text("Sentence with A. Sentence with <cp hex=\"0001\"/>. ");
        seg2.target().text("Sentence with A. Sentence with <cp hex=\"0001\"/>. ");
        seg2.originalTarget();
        segs.add(seg2.build());

        SegmentBuilder seg3 = new SegmentBuilder();
        seg3.source().text("Ph element ").code("1", "<ph/>", "<ph id=\"ph1\"/>").text(" #1.");
        seg3.target().text("Ph element ").code("1", "<ph/>", "<ph id=\"ph1\"/>").text(" #1.");
        seg3.originalTarget();
        segs.add(seg3.build());

        SegmentBuilder seg4 = new SegmentBuilder();
        seg4.source().text("Pc element ").code("1", "<pc>", "<pc id=\"pc1\">")
                .text("Important").code("1", "</pc>", "</pc>").text(" #1.");
        seg4.target().text("Pc element ").code("1", "<pc>", "<pc id=\"pc1\">")
                .text("Important").code("1", "</pc>", "</pc>").text(" #1.");
        seg4.originalTarget();
        segs.add(seg4.build());

        SegmentBuilder seg5 = new SegmentBuilder();
        seg5.source().text("Text in ").code("1", "<sc/>", "<sc id=\"sc1\"")
                .text("bold ").code("1", "<pc>", "<sc id=\"sc2\"/>")
                .text("and").code("1", "<ec/>", "<ec startRef=\"sc1\"/>")
                .text(" italics").code("1", "</pc>", "<ec startRef=\"sc2\"/>")
                .text(".");
        seg5.target().text("Text in ").code("1", "<sc/>", "<sc id=\"sc1\"/>")
                .text("bold ").code("1", "<pc>", "<sc id=\"sc2\"/>")
                .text("and").code("1", "<ec/>", "<ec startRef=\"sc1\"/>")
                .text(" italics").code("1", "</pc>", "<ec startRef=\"sc2\"/>")
                .text(".");
        seg5.originalTarget();
        segs.add(seg5.build());

        SegmentBuilder seg6 = new SegmentBuilder();
        seg6.source().text("Mrk element ").code("1", "<mrk>", "<mrk id=\"mrk1\" translate=\"yes\">")
                .text("Important").code("1", "</mrk>", "</mrk>").text(" #1.");
        seg6.target().text("Mrk element ").code("1", "<mrk>", "<mrk id=\"mrk1\" translate=\"yes\">")
                .text("Important").code("1", "</mrk>", "</mrk>").text(" #1.");
        seg6.originalTarget();
        segs.add(seg6.build());

        SegmentBuilder seg7 = new SegmentBuilder();
        seg7.source().text("Sm split element ").code("1", "<sm/>", "<sm id=\"sm1\" translate=\"no\"/>")
                .text(" #1.");
        seg7.target().text("Sm split element ").code("1", "<sm/>", "<sm id=\"sm1\" translate=\"no\"/>")
                .text(" #1.");
        seg7.originalTarget();
        segs.add(seg7.build());

        SegmentBuilder seg8 = new SegmentBuilder();
        seg8.source().text("Em split element ").code("1", "<em/>", "<em startRef=\"sm1\"/>")
                .text(" #1.");
        seg8.target().text("Em split element ").code("1", "<em/>", "<em startRef=\"sm1\"/>")
                .text(" #1.");
        seg8.originalTarget();
        segs.add(seg8.build());
        return segs;
    }

    public class SegmentBuilder {
        private int segNum, srcEventNum, tgtEventNum;
        private SegmentVariantBuilder source, target, originalTarget;

        public SegmentBuilder segmentNumber(int segNum) {
            this.segNum = segNum;
            return this;
        }

        public SegmentBuilder sourceEventNumber(int srcEventNum) {
            this.srcEventNum = srcEventNum;
            return this;
        }

        public SegmentBuilder targetEventNumber(int tgtEventNum) {
            this.tgtEventNum = tgtEventNum;
            return this;
        }

        public SegmentVariantBuilder source() {
            this.source = new SegmentVariantBuilder();
            return this.source;
        }

        public SegmentVariantBuilder target() {
            this.target = new SegmentVariantBuilder();
            return this.target;
        }

        public SegmentVariantBuilder originalTarget() {
            this.originalTarget = new SegmentVariantBuilder();
            return this.originalTarget;
        }

        public Segment build() {
            return new Segment(segNum, srcEventNum, tgtEventNum, source.build(),
                    target.build(), originalTarget.build());
        }
    }

    public class SegmentVariantBuilder {
        List<SegmentAtom> segAtoms = new ArrayList<>();

        public SegmentVariantBuilder text(String text) {
            segAtoms.add(new TextAtom(text));
            return this;
        }

        public SegmentVariantBuilder code(String id, String basic, String verbose) {
            segAtoms.add(new CodeAtom(id, basic, verbose));
            return this;
        }

        public SegmentVariant build() {
            return new SimpleSegmentVariant(segAtoms);
        }
    }
}
