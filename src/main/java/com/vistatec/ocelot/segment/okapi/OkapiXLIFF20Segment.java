package com.vistatec.ocelot.segment.okapi;

import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.Segment;
import net.sf.okapi.lib.xliff2.its.ITSWriter;
import net.sf.okapi.lib.xliff2.its.Provenances;

import com.vistatec.ocelot.its.Provenance;
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

    @Override
    protected void updateSegment() {
        //TODO: set ori target
        if (hasOriginalTarget()) {
            FragmentVariant targetFrag = (FragmentVariant)getTarget();
            Fragment updatedOkapiFragment = targetFrag.getUpdatedOkapiFragment(segment.getTarget());
            segment.setTarget(updatedOkapiFragment);
        }

        FragmentVariant source = (FragmentVariant)getSource();
        source.updateSegmentAtoms(segment);

        FragmentVariant target = (FragmentVariant)getTarget();
        target.updateSegmentAtoms(segment);
    }

    @Override
    public void addNativeProvenance(Provenance addedProv) {
        // TODO Refactor with 1.2 getITSRef()
        String ocelotProvId = "OcelotProv" + getSegmentNumber();
        Provenances okapiProvGroup = new Provenances(ocelotProvId);
        net.sf.okapi.lib.xliff2.its.Provenance okapiProv
                = new net.sf.okapi.lib.xliff2.its.Provenance();
        okapiProv.setRevPerson(addedProv.getRevPerson());
        okapiProv.setRevOrg(addedProv.getRevOrg());
        // TODO this should always be set, not just for XLIFF 2.0
        okapiProv.setRevTool("http://open.vistatec.com/ocelot");
        okapiProv.setProvRef(addedProv.getProvRef());
        okapiProvGroup.getList().add(okapiProv);
        ITSWriter.annotate(segment.getTarget(), 0, -1, okapiProvGroup, null);
    }
}
