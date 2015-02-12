package com.vistatec.ocelot.segment;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;

public class TestBaseSegmentVariant {
    BaseSegmentVariant simpleSv, complexSv;
    BaseSegmentVariant tcv, plainTextTCV, plainCodeTCV;


    @Before
    public void setup() {
        List<SegmentAtom> atoms = Lists.newArrayList(
                new TextAtom("A"),
                new CodeAtom("1", "<b>", "<b>"),
                new TextAtom("B"),
                new CodeAtom("2", "</b>", "</b>")
        );

        simpleSv = new BaseSegmentVariant(atoms);

        complexSv = new BaseSegmentVariant(Lists.newArrayList(
                new TextAtom("ABC"),
                new CodeAtom("1", "<b>", "<b>"),
                new TextAtom("DEF"),
                new CodeAtom("1", "</b>", "</b>")
        ));

        tcv = sampleText();
        plainTextTCV = plainText();
        plainCodeTCV = plainCode();
    }

    public static BaseSegmentVariant sampleText() {
        List<SegmentAtom> atoms = new ArrayList<SegmentAtom>();
        atoms.add(new TextAtom("A"));
        atoms.add(new CodeAtom("0", "<b>", "<b id=\"1\">"));
        atoms.add(new TextAtom("B"));
        atoms.add(new CodeAtom("1", "</b>", "</b>"));
        return new BaseSegmentVariant(atoms);
    }

    public static BaseSegmentVariant plainText() {
        return new BaseSegmentVariant(Lists.newArrayList((SegmentAtom)new TextAtom("Plain text")));
    }

    public static BaseSegmentVariant plainCode() {
        List<SegmentAtom> atoms = new ArrayList<SegmentAtom>();
        atoms.add(new CodeAtom("1", "<b>", "<b id=\"1\">"));
        atoms.add(new CodeAtom("2", "</b>", "</b>"));
        return new BaseSegmentVariant(atoms);
    }

    @Test
    public void testGetAtomsForRange() {
        // A < b > B < / B >
        // 0 1 2 3 4 5 6 7 8
        assertEquals((List<SegmentAtom>)new ArrayList<SegmentAtom>(),
                     simpleSv.getAtomsForRange(0, 0));
        assertEquals(Lists.newArrayList(new TextAtom("A")),
                simpleSv.getAtomsForRange(0, 1));
        assertEquals(Lists.newArrayList(new TextAtom("A"), new CodeAtom("1", "<b>", "<b>")),
                simpleSv.getAtomsForRange(0,  2));
        assertEquals(Lists.newArrayList(new TextAtom("A"), new CodeAtom("1", "<b>", "<b>")),
                simpleSv.getAtomsForRange(0,  3));
        assertEquals(Lists.newArrayList(new TextAtom("A"), new CodeAtom("1", "<b>", "<b>")),
                simpleSv.getAtomsForRange(0,  4));
        assertEquals(Lists.newArrayList(new TextAtom("A"), new CodeAtom("1", "<b>", "<b>"), new TextAtom("B")),
                simpleSv.getAtomsForRange(0,  5));
        assertEquals(Lists.newArrayList(new CodeAtom("1", "<b>", "<b>"), new TextAtom("B")),
                simpleSv.getAtomsForRange(1, 4));
        assertEquals(Lists.newArrayList(new CodeAtom("1", "<b>", "<b>"), new TextAtom("B")),
                simpleSv.getAtomsForRange(2, 3));
        assertEquals(Lists.newArrayList(new CodeAtom("1", "<b>", "<b>"), new TextAtom("B")),
                simpleSv.getAtomsForRange(3, 2));
        assertEquals(Lists.newArrayList(new TextAtom("A"), new CodeAtom("1", "<b>", "<b>"), 
                                        new TextAtom("B"), new CodeAtom("2", "</b>", "</b>")),
                simpleSv.getAtomsForRange(0, 8));

        // A B C < b > D E F < / b >
        // 0 1 2 3 4 5 6 7 8 9 0 1 2
        assertEquals(Lists.newArrayList(new TextAtom("C"), new CodeAtom("1", "<b>", "<b>")),
                complexSv.getAtomsForRange(2, 4));
        assertEquals(Lists.newArrayList(new TextAtom("C"), new CodeAtom("1", "<b>", "<b>"), new TextAtom("D")),
                complexSv.getAtomsForRange(2, 5));
        assertEquals(Lists.newArrayList(new CodeAtom("1", "<b>", "<b>"), new TextAtom("D")),
                complexSv.getAtomsForRange(3, 4));
        assertEquals(Lists.newArrayList(new CodeAtom("1", "<b>", "<b>"), new TextAtom("DE")),
                complexSv.getAtomsForRange(3, 5));
    }

    @Test
    public void testFindSelectionStart() {
        // A < b > B < / B >
        // 0 1 2 3 4 5 6 7 8
        assertEquals(0, simpleSv.findSelectionStart(0)); // unchanged
        assertEquals(1, simpleSv.findSelectionStart(1)); // unchanged
        assertEquals(1, simpleSv.findSelectionStart(2));
        assertEquals(1, simpleSv.findSelectionStart(3));
        assertEquals(4, simpleSv.findSelectionStart(4)); // unchanged
        assertEquals(5, simpleSv.findSelectionStart(5)); // unchanged
        assertEquals(5, simpleSv.findSelectionStart(6));
        assertEquals(5, simpleSv.findSelectionStart(7));
        assertEquals(5, simpleSv.findSelectionStart(8));
    }

    @Test
    public void testFindSelectionEnd() {
        // A < b > B < / B >
        // 0 1 2 3 4 5 6 7 8
        assertEquals(0, simpleSv.findSelectionEnd(0)); // unchanged
        assertEquals(1, simpleSv.findSelectionEnd(1));
        assertEquals(4, simpleSv.findSelectionEnd(2));
        assertEquals(4, simpleSv.findSelectionEnd(3));
        assertEquals(4, simpleSv.findSelectionEnd(4));
        assertEquals(5, simpleSv.findSelectionEnd(5));
        assertEquals(9, simpleSv.findSelectionEnd(6));
        assertEquals(9, simpleSv.findSelectionEnd(7));
        assertEquals(9, simpleSv.findSelectionEnd(8));
    }

    @Test
    public void testGetAtoms() {
        assertEquals(Lists.newArrayList(new TextAtom("A"), new CodeAtom("0", "<b>", "<b id=\"1\">"),
                                        new TextAtom("B"), new CodeAtom("1", "</b>", "</b>")),
                tcv.getAtoms());
    }

    @Test
    public void testReplaceSelection() {
        // A<b>B</b>
        // replace <b>B with B<b>
        List<SegmentAtom> atoms = new ArrayList<SegmentAtom>();
        atoms.add(new TextAtom("AB"));
        atoms.add(new CodeAtom("0", "<b>", "<b id=\"1\">"));
        atoms.add(new CodeAtom("1", "</b>", "</b>"));
        BaseSegmentVariant replacement = new BaseSegmentVariant(atoms);

        // A < b > B < / b >
        // 0 1 2 3 4 5 6 7 8
        //      - - - 
        // A B < b > < / b >
        // 0 1 2 3 4 5 6 7 8
        tcv.replaceSelection(1, 5, new SegmentVariantSelection(0, replacement, 1, 5));
        assertEquals("AB<b></b>", tcv.getDisplayText());
    }

    @Test
    public void testGetDisplayText() {
        assertEquals("A<b>B</b>", tcv.getDisplayText());
    }

    @Test
    public void testGetStyleData() {
        List<String> expected = new ArrayList<String>();
        expected.add("A");
        expected.add(SegmentTextCell.regularStyle);
        expected.add("<b>");
        expected.add(SegmentTextCell.tagStyle);
        expected.add("B");
        expected.add(SegmentTextCell.regularStyle);
        expected.add("</b>");
        expected.add(SegmentTextCell.tagStyle);
        assertEquals(expected, tcv.getStyleData(false));
    }

    @Test
    public void testGetStyleDataVerbose() {
        List<String> expected = new ArrayList<String>();
        expected.add("A");
        expected.add(SegmentTextCell.regularStyle);
        expected.add("<b id=\"1\">");
        expected.add(SegmentTextCell.tagStyle);
        expected.add("B");
        expected.add(SegmentTextCell.regularStyle);
        expected.add("</b>");
        expected.add(SegmentTextCell.tagStyle);
        assertEquals(expected, tcv.getStyleData(true));
    }

    @Test
    public void testCanInsertAt() {
        // A < b > B < / b >
        // 0 1 2 3 4 5 6 7 8
        // Y Y N N Y Y N N N
        assertTrue(tcv.canInsertAt(0));
        assertTrue(tcv.canInsertAt(1));
        assertFalse(tcv.canInsertAt(2));
        assertFalse(tcv.canInsertAt(3));
        assertTrue(tcv.canInsertAt(4));
        assertTrue(tcv.canInsertAt(5));
        assertFalse(tcv.canInsertAt(6));
        assertFalse(tcv.canInsertAt(7));
        assertFalse(tcv.canInsertAt(8));
    }

    @Test
    public void testContainsTag() {
        // A < b > B < / b >
        // 0 1 2 3 4 5 6 7 8
        assertFalse(tcv.containsTag(0, 1));
        assertTrue(tcv.containsTag(0, 2));
        assertTrue(tcv.containsTag(1, 2));
        assertTrue(tcv.containsTag(2, 2));
        assertTrue(tcv.containsTag(3, 2));
        assertFalse(tcv.containsTag(4, 1));
        assertTrue(tcv.containsTag(4, 2));
        assertTrue(tcv.containsTag(5, 2));
        assertTrue(tcv.containsTag(6, 2));
    }

    @Test
    public void testModifyChars() {
        // A < b > B < / b >
        // 0 1 2 3 4 5 6 7 8
        tcv.modifyChars(0, 0, "X");
        assertEquals("XA<b>B</b>", tcv.getDisplayText());
        tcv = sampleText();
        tcv.modifyChars(1, 0, "X");
        assertEquals("AX<b>B</b>", tcv.getDisplayText());
        tcv = sampleText();
        tcv.modifyChars(4, 0, "X");
        assertEquals("A<b>XB</b>", tcv.getDisplayText());
        tcv.modifyChars(5, 0, "N");
        assertEquals("A<b>XNB</b>", tcv.getDisplayText());
        tcv = sampleText();
        tcv.modifyChars(5, 0, "X");
        assertEquals("A<b>BX</b>", tcv.getDisplayText());
        tcv = sampleText();
        tcv.modifyChars(0,  1, "X");
        assertEquals("X<b>B</b>", tcv.getDisplayText());
        tcv = sampleText();
        tcv.modifyChars(4,  1, "X");
        assertEquals("A<b>X</b>", tcv.getDisplayText());
        tcv = sampleText();
        tcv.modifyChars(0, 1, null); // delete
        assertEquals("<b>B</b>", tcv.getDisplayText());
        tcv = sampleText();
        tcv.modifyChars(9, 0, "X");
        assertEquals("A<b>B</b>X", tcv.getDisplayText());
    }

    @Test
    public void testInsertPlainCode() {
        plainCodeTCV.modifyChars(0, 0, "ABC");
        assertEquals("ABC<b></b>", plainCodeTCV.getDisplayText());

        plainCodeTCV = plainCode();
        plainCodeTCV.modifyChars(7, 0, "ABC");
        assertEquals("<b></b>ABC", plainCodeTCV.getDisplayText());

        plainCodeTCV = plainCode();
        plainCodeTCV.modifyChars(3, 0, "ABC");
        assertEquals("<b>ABC</b>", plainCodeTCV.getDisplayText());

        // These test calls should never be called in real usage, as modifyChars
        // should not be called unless it has already been verified that you are
        // not modifying within a CodeAtom.
        plainCodeTCV = plainCode();
        plainCodeTCV.modifyChars(1, 0, "ABC");
        assertEquals("ABC<b></b>", plainCodeTCV.getDisplayText());

        plainCodeTCV = plainCode();
        plainCodeTCV.modifyChars(2, 0, "ABC");
        assertEquals("ABC<b></b>", plainCodeTCV.getDisplayText());

        plainCodeTCV = plainCode();
        plainCodeTCV.modifyChars(4, 0, "ABC");
        assertEquals("<b>ABC</b>", plainCodeTCV.getDisplayText());

        plainCodeTCV = plainCode();
        plainCodeTCV.modifyChars(5, 0, "ABC");
        assertEquals("<b>ABC</b>", plainCodeTCV.getDisplayText());

        plainCodeTCV = plainCode();
        plainCodeTCV.modifyChars(6, 0, "ABC");
        assertEquals("<b>ABC</b>", plainCodeTCV.getDisplayText());
    }

    @Test
    public void testRemoveCharsFromPlainText() {
        plainTextTCV.modifyChars(5, 5, null);
        assertEquals("Plain", plainTextTCV.getDisplayText());

        plainTextTCV = plainText();
        plainTextTCV.modifyChars(5, 1, null);
        assertEquals("Plaintext", plainTextTCV.getDisplayText());

        plainTextTCV = plainText();
        plainTextTCV.modifyChars(0, 6, null);
        assertEquals("text", plainTextTCV.getDisplayText());
    }

}
