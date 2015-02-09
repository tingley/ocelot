package com.vistatec.ocelot.segment.okapi;

import net.sf.okapi.lib.xliff2.core.Segment;

import com.vistatec.ocelot.segment.OcelotSegment;
import com.vistatec.ocelot.segment.SegmentVariant;

public class OkapiXLIFF20Segment extends OcelotSegment {
    private Segment segment;

    public OkapiXLIFF20Segment(int segNum, SegmentVariant source,
            SegmentVariant target, SegmentVariant originalTarget,
            Segment segment) {
        super(segNum, source, target, originalTarget);
        this.segment = segment;
    }

    public Segment getSegment() {
        return segment;
    }

    @Override
    public boolean isEditable() {
        // Currently we have no phase-qualifier info for these
        return true;
    }

    @Override
    public String getTransUnitId() {
        return segment.getId();
    }
}
