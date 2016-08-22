/*
 * Copyright (C) 2015, VistaTEC or third-party contributors as indicated
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
package com.vistatec.ocelot.segment.model.okapi;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.*;

import com.google.common.collect.Lists;
import com.vistatec.ocelot.segment.model.CodeAtom;
import com.vistatec.ocelot.segment.model.SegmentAtom;
import com.vistatec.ocelot.segment.model.SegmentVariantSelection;
import com.vistatec.ocelot.segment.view.SegmentTextCell;
import com.vistatec.ocelot.segment.model.TextAtom;

import static org.junit.Assert.*;

public class TestTextContainerSegmentVariant {
    private TextContainerVariant tcv, plainTextTCV, plainCodeTCV;

    @Before
    public void beforeTest() {
        tcv = sampleText();
        plainTextTCV = plainText();
        plainCodeTCV = plainCode();
    }

    private TextContainerVariant sampleText() {
        TextContainer tc = new TextContainer();
        TextFragment tf = tc.getFirstContent();
        tf.append("A");
        tf.append(new Code(TagType.OPENING, "b", "<b id=\"1\">"));
        tf.append("B");
        tf.append(new Code(TagType.CLOSING, "b", "</b>"));
        
        return new TextContainerVariant(tc);
    }

    private TextContainerVariant plainText() {
        TextContainer tc = new TextContainer();
        TextFragment tf = tc.getFirstContent();
        tf.append("Plain text");
        return new TextContainerVariant(tc);
    }

    private TextContainerVariant plainCode() {
        TextContainer tc = new TextContainer();
        TextFragment tf = tc.getFirstContent();
        tf.append(new Code(TagType.OPENING, "b", "<b id=\"1\">"));
        tf.append(new Code(TagType.CLOSING, "b", "</b>"));
        return new TextContainerVariant(tc);
    }

    @Test
    public void testGetAtoms() {
        assertEquals(Lists.newArrayList(new TextAtom("A"), new CodeAtom("0", "<b>", "<b id=\"1\">"),
                                        new TextAtom("B"), new CodeAtom("1", "</b>", "</b>")),
                tcv.getAtoms());
    }

    @Test
    public void testSetAtoms() {
        List<SegmentAtom> atoms = tcv.getAtoms();
        atoms.add(new TextAtom("X"));
        tcv.setAtoms(atoms);
        assertEquals(Lists.newArrayList(new TextAtom("A"), new CodeAtom("0", "<b>", "<b id=\"1\">"),
                new TextAtom("B"), new CodeAtom("1", "</b>", "</b>"), new TextAtom("X")),
                tcv.getAtoms());
    }

    @Test
    public void testReplaceSelection() {
        // A<b>B</b>
        // replace <b>B with B<b>
        TextContainer tc = new TextContainer();
        TextFragment tf = tc.getFirstContent();
        tf.append("AB");
        tf.append(new Code(TagType.OPENING, "b", "<b id=\"1\">"));
        tf.append(new Code(TagType.CLOSING, "b", "</b>"));
        TextContainerVariant replacement = new TextContainerVariant(tc);

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

    @Test
    public void testDoesThisCrash() {
        List<String> l = Lists.newArrayList("A", "B", "C");
        assertEquals(Lists.newArrayList("C"), l.subList(2, 3));
        assertEquals(Lists.newArrayList(), l.subList(3, 3));
    }
}
