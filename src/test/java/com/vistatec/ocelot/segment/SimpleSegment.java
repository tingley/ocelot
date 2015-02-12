package com.vistatec.ocelot.segment;

import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.its.Provenance;
import com.vistatec.ocelot.segment.OcelotSegment;
import com.vistatec.ocelot.segment.SegmentVariant;

public class SimpleSegment extends OcelotSegment {
    private SegmentVariant source = new BaseSegmentVariant(),
                    target = new BaseSegmentVariant(), originalTarget;

    public SimpleSegment(int segNum, SegmentVariant source,
            SegmentVariant target, SegmentVariant originalTarget) {
        super(segNum);
        this.source = source;
        this.target = target;
        this.originalTarget = originalTarget;
    }

    public SimpleSegment() {
        this(1);
    }

    public SimpleSegment(int segNum) {
        super(segNum);
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public String getTransUnitId() {
        return "dummy";
    }

    @Override
    protected void addNativeProvenance(Provenance prov) {
    }

    @Override
    protected SegmentVariant getSourceVariant() {
        return source;
    }

    @Override
    protected SegmentVariant getTargetVariant() {
        return target;
    }

    @Override
    protected SegmentVariant getOriginalTargetVariant() {
        return originalTarget;
    }

    @Override
    protected void setTargetVariant(SegmentVariant target) {
        this.target = target;
    }

    @Override
    protected void setOriginalTargetVariant(SegmentVariant originalTarget) {
        this.originalTarget = originalTarget;
    }

    @Override
    protected void addNativeLQI(LanguageQualityIssue addedLQI) {
    }

    @Override
    protected void modifyNativeLQI(LanguageQualityIssue modifiedLQI) {
    }

    @Override
    protected void removeNativeLQI(LanguageQualityIssue removedLQI) {
    }
}
