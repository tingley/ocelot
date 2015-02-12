package com.vistatec.ocelot.segment.okapi;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.lib.xliff2.core.Directionality;
import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.IWithStore;
import net.sf.okapi.lib.xliff2.core.Store;
import net.sf.okapi.lib.xliff2.core.TagType;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.vistatec.ocelot.segment.BaseSegmentVariant;
import com.vistatec.ocelot.segment.CodeAtom;
import com.vistatec.ocelot.segment.SegmentAtom;
import com.vistatec.ocelot.segment.TestBaseSegmentVariant;
import com.vistatec.ocelot.segment.TextAtom;

public class TestOkapiXLIFF20Segment {
    private Fragment sampleFV, plainTextFV, plainCodeFV;

    @Before
    public void beforeTest() {
        sampleFV = sampleText(false);
        plainTextFV = plainText();
        plainCodeFV = plainCode();
    }

    @Test
    public void testSampleTextConversion() {
        BaseSegmentVariant sv = new OkapiXLIFF20VariantHelper().createVariant(sampleFV);
        List<SegmentAtom> atoms = new ArrayList<SegmentAtom>();
        atoms.add(new TextAtom("A"));
        atoms.add(new CodeAtom("id1", "<pc>", "<pc id=\"id1\">"));
        atoms.add(new TextAtom("B"));
        atoms.add(new CodeAtom("id1", "</pc>", "</pc>"));
        BaseSegmentVariant expected = new BaseSegmentVariant(atoms);
        assertEquals(expected, sv);
    }

    @Test
    public void testPlainTextConversion() {
        BaseSegmentVariant sv = new OkapiXLIFF20VariantHelper().createVariant(plainTextFV);
        List<SegmentAtom> atoms = new ArrayList<SegmentAtom>();
        atoms.add(new TextAtom("Plain Text"));
        BaseSegmentVariant expected = new BaseSegmentVariant(atoms);
        assertEquals(expected, sv);
    }

    @Test
    public void testPlainCodeConversion() {
        BaseSegmentVariant sv = new OkapiXLIFF20VariantHelper().createVariant(plainCodeFV);
        List<SegmentAtom> atoms = new ArrayList<SegmentAtom>();
        atoms.add(new CodeAtom("id1", "<pc>", "<pc id=\"id1\">"));
        atoms.add(new CodeAtom("id1", "</pc>", "</pc>"));
        BaseSegmentVariant expected = new BaseSegmentVariant(atoms);
        assertEquals(expected, sv);
    }

    private Fragment sampleText(boolean isTarget) {
        Store store = new Store(new DummyWithStore());
        Fragment fragment = new Fragment(store, isTarget);
        fragment.append("A");
        fragment.append(TagType.OPENING, "id1", "<b>", false);
        fragment.append("B");
        fragment.append(TagType.CLOSING, "id1", "</b>", false);
        return fragment;
    }

    private Fragment plainText() {
        Store store = new Store(new DummyWithStore());
        Fragment fragment = new Fragment(store, false);
        fragment.append("Plain Text");
        return fragment;
    }

    private Fragment plainCode() {
        Store store = new Store(new DummyWithStore());
        Fragment fragment = new Fragment(store, false);
        fragment.append(TagType.OPENING, "id1", "<b>", false);
        fragment.append(TagType.CLOSING, "id1", "</b>", false);
        return fragment;
    }

    class DummyWithStore implements IWithStore {
        @Override
        public Directionality getSourceDir() {
            return Directionality.AUTO;
        }

        @Override
        public Directionality getTargetDir() {
            return Directionality.AUTO;
        }

        @Override
        public boolean isIdUsed(String id) {
            return false;
        }

        @Override
        public void setSourceDir(Directionality arg0) {
        }

        @Override
        public void setTargetDir(Directionality arg0) {
        }
        
    }
}
