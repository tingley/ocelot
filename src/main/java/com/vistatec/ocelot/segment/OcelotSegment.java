/*
 * Copyright (C) 2013, VistaTEC or third-party contributors as indicated
 * by the @author tags or express copyright attribution statements applied by
 * the authors. All third-party contributions are distributed under license by
 * VistaTEC.
 *
 * This file is part of Ocelot.
 *
 * Ocelot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ocelot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, write to:
 *
 *     Free Software Foundation, Inc.
 *     51 Franklin Street, Fifth Floor
 *     Boston, MA 02110-1301
 *     USA
 *
 * Also, see the full LGPL text here: <http://www.gnu.org/copyleft/lesser.html>
 */
package com.vistatec.ocelot.segment;

import com.vistatec.ocelot.config.ProvenanceConfig;
import com.vistatec.ocelot.its.ITSMetadata;
import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.its.OtherITSMetadata;
import com.vistatec.ocelot.its.Provenance;
import com.vistatec.ocelot.rules.StateQualifier;
import com.vistatec.ocelot.segment.editdistance.EditDistance;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents source, target segments with ITS metadata
 */
public abstract class OcelotSegment {
    private int segmentNumber;
    private SegmentVariant source, target;
    private StateQualifier state_qualifier;
    private boolean setOriginalTarget = false;
    private String lqiID, provID;
    private List<LanguageQualityIssue> lqiList = new LinkedList<LanguageQualityIssue>();
    private List<Provenance> provList = new LinkedList<Provenance>();
    private List<OtherITSMetadata> otherITSList = new LinkedList<OtherITSMetadata>();
    private SegmentController segmentListener;
    private SegmentVariant originalTarget;
    private ArrayList<String> targetDiff = new ArrayList<String>();

    public OcelotSegment(int segNum) {
        this.segmentNumber = segNum;
    }

    public SegmentController getSegmentController() {
        return segmentListener;
    }

    public void setSegmentController(SegmentController segController) {
        this.segmentListener = segController;
    }

    public int getSegmentNumber() {
        return segmentNumber;
    }

    public SegmentVariant getSource() {
        if (source == null) {
            source = getSourceVariant();
        }
        return this.source;
    }

    public SegmentVariant getTarget() {
        if (target == null) {
            target = getTargetVariant();
        }
        return this.target;
    }

    public SegmentVariant getOriginalTarget() {
        if (originalTarget == null) {
            originalTarget = getOriginalTargetVariant();
            if (originalTarget != null) {
                setOriginalTarget = true;
                this.targetDiff = EditDistance.styleTextDifferences(getTarget(), originalTarget);
            }
            else {
                originalTarget = new BaseSegmentVariant();
            }
        }
        return this.originalTarget;
    }

    public void setOriginalTarget(SegmentVariant oriTgt) {
        if (!this.setOriginalTarget) {
            originalTarget = oriTgt;
            setOriginalTargetVariant(oriTgt);
        }
        this.setOriginalTarget = true;
    }

    public boolean hasOriginalTarget() {
        return this.setOriginalTarget;
    }

    /**
     * Return true if this segment is editable.  This may depend
     * on format-specific information.
     * @return
     */
    public abstract boolean isEditable();

    /**
     * Get a SegmentVariant representation of current source content.
     * @return
     */
    protected abstract SegmentVariant getSourceVariant();

    /**
     * Get a SegmentVariant representation of current target content.
     * @return
     */
    protected abstract SegmentVariant getTargetVariant();

    /**
     * Get a SegmentVariant representation of original target content,
     * or null if there is no original target.
     * @return
     */
    protected abstract SegmentVariant getOriginalTargetVariant();

    /**
     * Update the native representation for the target content based on
     * the contents of the specified SegmentVariant.
     * @return
     */
    protected abstract void setTargetVariant(SegmentVariant target);

    /**
     * Update the native representation for the original target content based on
     * the contents of the specified SegmentVariant.
     * @return
     */
    // Argument may not be needed
    protected abstract void setOriginalTargetVariant(SegmentVariant originalTarget);

    // Currently unused
    public abstract String getTransUnitId();

    /**
     * Attach a provenance object to the underlying native representation.
     * This will be called whenever a new provenance item is added ot the
     * segment.
     */
    protected abstract void addNativeProvenance(Provenance prov);

    protected abstract void addNativeLQI(LanguageQualityIssue addedLQI);

    protected abstract void modifyNativeLQI(LanguageQualityIssue modifiedLQI);

    protected abstract void removeNativeLQI(LanguageQualityIssue removedLQI);

    public void markWithUserProvenance() {
        addProvenance(getSegmentController().getProvenanceConfig().getUserProvenance());
    }

    /**
     * Sets the new target for this segment.  Sets an original target value
     * if none exists.  Updates target diff value.
     * @param updatedTarget
     */
    public void updateTarget(SegmentVariant updatedTarget) {
        if (!updatedTarget.getDisplayText().equals(getTarget().getDisplayText())) {
            if (!hasOriginalTarget()) {
                setOriginalTarget(target);
            }
            target = updatedTarget;
            setTargetVariant(target);
            editDistance = NO_EDIT_DISTANCE;
            setTargetDiff(EditDistance.styleTextDifferences(getTarget(),
                          getOriginalTarget()));
            markWithUserProvenance();
            if (segmentListener != null) {
                segmentListener.notifyUpdateSegment(this); // needed for edit events
            }
        }
    }

    public void resetTarget() {
        if (setOriginalTarget) {
            updateTarget(getOriginalTarget());
            markWithUserProvenance();
            if (segmentListener != null) {
                segmentListener.notifyResetTarget(this);
            }
        }
    }

    public ArrayList<String> getTargetDiff() {
        return this.targetDiff;
    }

    private void setTargetDiff(ArrayList<String> targetDiff) {
        this.targetDiff = targetDiff;
    }

    public StateQualifier getStateQualifier() {
        return this.state_qualifier;
    }

    public void setStateQualifier(StateQualifier state) {
        this.state_qualifier = state;
    }

    public String getProvID() {
        return this.provID;
    }

    public void setProvID(String provId) {
        this.provID = provId;
    }

    public List<Provenance> getProvenance() {
        return provList;
    }

    public void setProv(List<Provenance> provList) {
        this.provList = provList;
    }

    public void addProvenance(Provenance prov) {
        if (!prov.isEmpty() && !provList.contains(prov)) {
            provList.add(prov);
            addNativeProvenance(prov);
            if (segmentListener != null) {
                segmentListener.notifyAddedProv(prov);
            }
        }
    }

    public String getLQIID() {
        return this.lqiID;
    }

    public void setLQIID(String id) {
        this.lqiID = id;
    }

    public boolean containsLQI() {
        return lqiList.size() > 0;
    }

    public List<LanguageQualityIssue> getLQI() {
        return lqiList;
    }

    public void setLQI(List<LanguageQualityIssue> lqi) {
        this.lqiList = lqi;
    }

    public void addLQI(LanguageQualityIssue lqi) {
        lqiList.add(lqi);
        if (segmentListener != null) {
            segmentListener.notifyModifiedLQI(lqi, this);
        }
        addNativeLQI(lqi);
    }

    public void editedLQI(LanguageQualityIssue lqi) {
        if (segmentListener != null) {
            segmentListener.notifyModifiedLQI(lqi, this);
        }
        modifyNativeLQI(lqi);
    }

    public void removeLQI(LanguageQualityIssue removeLQI) {
        lqiList.remove(removeLQI);
        if (segmentListener != null) {
            segmentListener.notifyRemovedLQI(removeLQI, this);
        }
        removeNativeLQI(removeLQI);
    }

    public List<OtherITSMetadata> getOtherITSMetadata() {
        return this.otherITSList;
    }

    public void setOtherITSMetadata(List<OtherITSMetadata> otherList) {
        this.otherITSList = otherList;
    }

    public List<ITSMetadata> getAllITSMetadata() {
        List<ITSMetadata> its = new ArrayList<ITSMetadata>();
        its.addAll(lqiList);
        its.addAll(provList);
        its.addAll(otherITSList);
        return its;
    }

    private static final int NO_EDIT_DISTANCE = -1;
    private int editDistance = NO_EDIT_DISTANCE;
    public int getEditDistance() {
        if (NO_EDIT_DISTANCE == editDistance) {
            editDistance = hasOriginalTarget() ? EditDistance.calcEditDistance(getTarget(), getOriginalTarget()) : 0;
        }
        return editDistance;
    }
}
