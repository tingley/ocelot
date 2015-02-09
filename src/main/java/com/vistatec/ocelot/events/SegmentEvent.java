package com.vistatec.ocelot.events;

import com.vistatec.ocelot.segment.OcelotSegment;

public abstract class SegmentEvent {
    private OcelotSegment segment;

    protected SegmentEvent(OcelotSegment segment) {
        this.segment = segment;
    }

    public OcelotSegment getSegment() {
        return segment;
    }
}
