package com.vistatec.ocelot.events;

import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.segment.OcelotSegment;

public class LQIModificationEvent {
    private LanguageQualityIssue lqi;
    private OcelotSegment segment;

    public LQIModificationEvent(LanguageQualityIssue lqi, OcelotSegment segment) {
        this.lqi = lqi;
        this.segment = segment;
    }

    public OcelotSegment getSegment() {
        return segment;
    }

    public LanguageQualityIssue getLQI() {
        return lqi;
    }
}
