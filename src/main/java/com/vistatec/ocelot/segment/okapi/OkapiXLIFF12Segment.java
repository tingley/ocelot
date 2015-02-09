package com.vistatec.ocelot.segment.okapi;

import net.sf.okapi.common.resource.ITextUnit;

import com.vistatec.ocelot.segment.OcelotSegment;
import com.vistatec.ocelot.segment.SegmentVariant;

public class OkapiXLIFF12Segment extends OcelotSegment {
    private ITextUnit textUnit;
    private String phase_name;

    public OkapiXLIFF12Segment(int segNum, SegmentVariant source, 
            SegmentVariant target, SegmentVariant originalTarget,
            ITextUnit textUnit) {
        super(segNum, source, target, originalTarget);
        this.textUnit = textUnit;
    }

    @Override
    public boolean isEditable() {
        return !"Rebuttal".equalsIgnoreCase(getPhaseName()) &&
                !"Translator approval".equalsIgnoreCase(getPhaseName());
    }

    @Override
    public String getTransUnitId() {
        return textUnit.getId();
    }

    public ITextUnit getTextUnit() {
        return textUnit;
    }

    public String getPhaseName() {
        return this.phase_name;
    }

    public void setPhaseName(String phaseName) {
        this.phase_name = phaseName;
    }

}
