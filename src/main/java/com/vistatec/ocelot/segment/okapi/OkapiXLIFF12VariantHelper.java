package com.vistatec.ocelot.segment.okapi;

import java.util.List;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

import com.google.common.collect.Lists;
import com.vistatec.ocelot.segment.BaseSegmentVariant;
import com.vistatec.ocelot.segment.CodeAtom;
import com.vistatec.ocelot.segment.SegmentAtom;
import com.vistatec.ocelot.segment.TextAtom;

class OkapiXLIFF12VariantHelper {

    // XXX problem - in paired tags, they both have the same IDs.
    // IDs aren't unique!
    public BaseSegmentVariant createVariant(TextContainer tc) {
        List<SegmentAtom> atoms = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        TextFragment tf = tc.getUnSegmentedContentCopy();
        for (int i = 0; i < tf.length(); i++) {
            char tfChar = tf.charAt(i);
            if (TextFragment.isMarker(tfChar)) {
                if (sb.length() > 0) {
                    // Flush as text
                    atoms.add(new TextAtom(sb.toString()));
                    sb.setLength(0);
                }
                char codeMarker = tf.charAt(++i);
                int codeIndex = TextFragment.toIndex(codeMarker);
                Code code = tf.getCode(codeIndex);
                atoms.add(new CodeAtom(codeIndex+"", getCodeText(code, false),
                                       getCodeText(code, true)));
            }
            else {
                sb.append(tfChar);
            }
        }
        // Flush trailing markup
        if (sb.length() > 0) {
            atoms.add(new TextAtom(sb.toString()));
        }

        return new BaseSegmentVariant(atoms);
    }

    private String getCodeText(Code code, boolean verbose) {
        if (verbose) {
            return code.hasOuterData() ? code.getOuterData() : code.getData();
        }
        switch (code.getTagType()) {
        case OPENING:
            return "<" + code.getType() + ">"; 
        case CLOSING:
            return "</" + code.getType() + ">";
        case PLACEHOLDER:
            return "<" + code.getType() + "/>";
        }
        throw new IllegalStateException();
    }
}
