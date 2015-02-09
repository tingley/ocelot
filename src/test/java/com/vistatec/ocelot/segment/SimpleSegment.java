package com.vistatec.ocelot.segment;

import net.sf.okapi.common.resource.TextContainer;

import com.vistatec.ocelot.segment.OcelotSegment;
import com.vistatec.ocelot.segment.SegmentVariant;
import com.vistatec.ocelot.segment.okapi.TextContainerVariant;

public class SimpleSegment extends OcelotSegment {

    public SimpleSegment(int segNum, SegmentVariant source,
            SegmentVariant target, SegmentVariant originalTarget) {
        super(segNum, source, target, originalTarget);
    }

    public SimpleSegment() {
        this(1);
    }

    public SimpleSegment(int segNum) {
        super(segNum, emptyVariant(), emptyVariant(), emptyVariant());
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    static SegmentVariant emptyVariant() {
        return new TextContainerVariant(new TextContainer());
    }

    @Override
    public String getTransUnitId() {
        return "dummy";
    }
}
