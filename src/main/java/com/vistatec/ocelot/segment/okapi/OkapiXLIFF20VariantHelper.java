package com.vistatec.ocelot.segment.okapi;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.PCont;
import net.sf.okapi.lib.xliff2.core.Tag;
import net.sf.okapi.lib.xliff2.renderer.IFragmentObject;
import net.sf.okapi.lib.xliff2.renderer.XLIFFFragmentRenderer;

import com.vistatec.ocelot.segment.BaseSegmentVariant;
import com.vistatec.ocelot.segment.CodeAtom;
import com.vistatec.ocelot.segment.SegmentAtom;
import com.vistatec.ocelot.segment.TextAtom;

public class OkapiXLIFF20VariantHelper {
    private final Logger LOG = LoggerFactory.getLogger(OkapiXLIFF20VariantHelper.class);
    private int protectedContentId = 0;

    public BaseSegmentVariant createVariant(Fragment frag) {
        List<SegmentAtom> parsedSegmentAtoms = new ArrayList<SegmentAtom>();
        XLIFFFragmentRenderer fragmentRenderer = new XLIFFFragmentRenderer(frag, null);
        for (IFragmentObject fragPart : fragmentRenderer) {
            Object textObject = fragPart.getObject();
            if (textObject instanceof String) {
                parsedSegmentAtoms.add(new TextAtom(fragPart.render()));

            } else if (textObject instanceof Tag) {
                Tag tag = (Tag) textObject;
                parsedSegmentAtoms.add(convertToCodeAtom(fragPart, tag));

            } else if (textObject instanceof PCont) {
                //TODO: Verify usage
                parsedSegmentAtoms.add(convertToCodeAtom(fragPart));

            } else {
                // TODO: More descriptive error
                LOG.error("Unrecognized object type in Fragment");
                System.exit(1);
            }
        }
        return new BaseSegmentVariant(parsedSegmentAtoms);
    }

    private CodeAtom convertToCodeAtom(IFragmentObject fragPart, Tag tag) {
        String detailedTag = fragPart.render();
        String basicTag = getBasicTag(detailedTag);
        return new TaggedCodeAtom(tag, basicTag, detailedTag);
    }

    private CodeAtom convertToCodeAtom(IFragmentObject fragPart) {
        String detailedTag = fragPart.render();
        String basicTag = getBasicTag(detailedTag);
        return new CodeAtom("PC"+protectedContentId++, basicTag, detailedTag);
    }

    private String getBasicTag(String detailedTag) {
        int tagEndCaratPos = detailedTag.indexOf(">");
        if (tagEndCaratPos < 0) {
            // TODO: Handle this case
            LOG.warn("Could not find tag end character '>' in '"+detailedTag+"'");
            System.exit(1);
        }
        if (detailedTag.charAt(tagEndCaratPos-1) == '/') {
            tagEndCaratPos--;
        }
        int beginTagAttrPos = detailedTag.indexOf(" ");
        return "<"+detailedTag.substring(1, beginTagAttrPos >= 0 ? beginTagAttrPos : tagEndCaratPos)
                +detailedTag.substring(tagEndCaratPos, detailedTag.length());
    }

}
