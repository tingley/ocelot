package com.vistatec.ocelot.events;

import com.vistatec.ocelot.segment.model.OcelotSegment;
import com.vistatec.ocelot.xliff.XLIFFDocument;

public class SegmentTargetExitEvent extends SegmentEvent {
    public SegmentTargetExitEvent(XLIFFDocument xliff, OcelotSegment segment) {
        super(xliff, segment);
    }
}
