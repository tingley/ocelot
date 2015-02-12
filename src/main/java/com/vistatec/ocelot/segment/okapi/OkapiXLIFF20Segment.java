package com.vistatec.ocelot.segment.okapi;

import java.util.List;

import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.MTag;
import net.sf.okapi.lib.xliff2.core.Part;
import net.sf.okapi.lib.xliff2.core.Segment;
import net.sf.okapi.lib.xliff2.core.Tag;
import net.sf.okapi.lib.xliff2.core.TagType;
import net.sf.okapi.lib.xliff2.core.Tags;
import net.sf.okapi.lib.xliff2.its.IITSItem;
import net.sf.okapi.lib.xliff2.its.ITSItems;
import net.sf.okapi.lib.xliff2.its.ITSWriter;
import net.sf.okapi.lib.xliff2.its.LocQualityIssue;
import net.sf.okapi.lib.xliff2.its.LocQualityIssues;
import net.sf.okapi.lib.xliff2.its.Provenances;

import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.its.Provenance;
import com.vistatec.ocelot.segment.BaseSegmentVariant;
import com.vistatec.ocelot.segment.CodeAtom;
import com.vistatec.ocelot.segment.OcelotSegment;
import com.vistatec.ocelot.segment.SegmentAtom;
import com.vistatec.ocelot.segment.SegmentVariant;

public class OkapiXLIFF20Segment extends OcelotSegment {
    private Segment segment;
    private SegmentVariant originalTarget;

    public OkapiXLIFF20Segment(int segNum, Segment segment) {
        super(segNum);
        this.segment = segment;
    }

    public Segment getSegment() {
        return segment;
    }

    @Override
    protected SegmentVariant getSourceVariant() {
        return new OkapiXLIFF20VariantHelper().createVariant(segment.getSource());
    }

    @Override
    protected SegmentVariant getTargetVariant() {
        return new OkapiXLIFF20VariantHelper().createVariant(segment.getTarget());
    }

    @Override
    protected SegmentVariant getOriginalTargetVariant() {
        // XLIFF 2.0 doesn't currently have a representation for this, so
        // we only have one if it's been set in an editing session
        return originalTarget;
    }

    @Override
    protected void setTargetVariant(SegmentVariant target) {
        segment.setTarget(updateFragment(segment.getTarget(), (BaseSegmentVariant)target));
    }

    @Override
    protected void setOriginalTargetVariant(SegmentVariant originalTarget) {
        this.originalTarget = originalTarget;
    }

    @Override
    protected void addNativeLQI(LanguageQualityIssue addedLQI) {
        updateITSLQIAnnotations();
    }

    @Override
    protected void modifyNativeLQI(LanguageQualityIssue modifiedLQI) {
        updateITSLQIAnnotations();
    }

    @Override
    protected void removeNativeLQI(LanguageQualityIssue removedLQI) {
        updateITSLQIAnnotations();
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

    // XXX Is it possible to edit the fragment in place?
    private Fragment updateFragment(Fragment fragment, BaseSegmentVariant variant) {
        Fragment updatedFragment = new Fragment(fragment.getStore(), fragment.isTarget(), "");
        Tags tags = fragment.getTags();
        for (SegmentAtom atom : variant.getAtoms()) {
            if (atom instanceof TaggedCodeAtom) {
                TaggedCodeAtom codeAtom = (TaggedCodeAtom) atom;
                Tag tag = codeAtom.getTag();
                if (tag != null) {
                    updatedFragment.append(Fragment.toChar1(tags.getKey(tag)))
                            .append(Fragment.toChar2(tags.getKey(tag)));
                }
            } else if (atom instanceof CodeAtom) {
                CodeAtom codeAtom = (CodeAtom) atom;
                Tag tag = fetchTag(codeAtom.getId(), fragment);
                if (tag != null) {
                    updatedFragment.append(Fragment.toChar1(tags.getKey(tag)))
                            .append(Fragment.toChar2(tags.getKey(tag)));
                }
            } else {
                updatedFragment.append(atom.getData());
            }
        }
        return updatedFragment;
    }

    private Tag fetchTag(String tagId, Fragment fragment) {
        Tags tags = fragment.getTags();
        for (Tag tag : tags) {
            if (tagId.equals(tag.getId())) {
                return tag;
            }
        }
        return null;
    }

    /**
     * Update LQI annotations on the segment. TODO: separate from non-LQI updates
     * @param unitPart - Okapi representation of the segment
     * @param seg - Ocelot segment
     */
    public void updateITSLQIAnnotations() {
        removeExistingLqiAnnotationsFromSegment(segment);

        if (containsLQI()) {
            String ocelotLqiId = "OcelotLQI" + getSegmentNumber();
            LocQualityIssues newOkapiLqiGroup = convertOcelotToOkapiLqi(
                    getLQI(), ocelotLqiId);
            ITSWriter.annotate(segment.getTarget(), 0, -1, newOkapiLqiGroup);
        }
    }

    private void removeExistingLqiAnnotationsFromSegment(Part unitPart) {
        List<Tag> sourceTags = unitPart.getSource().getOwnTags();
        List<Tag> targetTags = unitPart.getTarget().getOwnTags();

        removeExistingLqiAnnotations(unitPart, false, sourceTags);
        removeExistingLqiAnnotations(unitPart, true, targetTags);
    }

    private void removeExistingLqiAnnotations(Part unitPart, boolean isTarget, List<Tag> tags) {
        for (Tag tag : tags) {
            if (tag.isMarker()) {
                MTag mtag = (MTag) tag;
                if (mtag.hasITSItem()) {
                    ITSItems items = mtag.getITSItems();
                    IITSItem itsLqiItem = items.get(LocQualityIssue.class);
                    if (itsLqiItem != null) {
                        // Don't delete the LQI issues for opening tags so we
                        // can find the corresponding closing tag and delete it as well
                        if (mtag.getTagType() == TagType.CLOSING ||
                                mtag.getTagType() == TagType.STANDALONE) {
                            items.remove(itsLqiItem);
                        }
                        // TODO: Assumes MTag is only used for ITS metadata
                        if (items.size() <= 1) {
                            Fragment frag = isTarget ?
                                    unitPart.getTarget() : unitPart.getSource();
                            frag.remove(mtag);
                        }
                    }
                }
            }
        }
    }

    private LocQualityIssues convertOcelotToOkapiLqi(List<LanguageQualityIssue> ocelotLqi, String ocelotLqiId) {
        LocQualityIssues newLqiGroup = new LocQualityIssues(ocelotLqiId);
        for (LanguageQualityIssue lqi : ocelotLqi) {
            LocQualityIssue newLqi = new LocQualityIssue();
            newLqi.setType(lqi.getType());
            newLqi.setComment(lqi.getComment());
            newLqi.setSeverity(lqi.getSeverity());

            if (lqi.getProfileReference() != null) {
                newLqi.setProfileRef(lqi.getProfileReference().toString());
            }

            newLqi.setEnabled(lqi.isEnabled());
            newLqiGroup.getList().add(newLqi);
        }
        return newLqiGroup;
    }
}
