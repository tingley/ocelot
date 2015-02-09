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
    private boolean addedProvenance = false, setOriginalTarget = false;
    private String lqiID, provID;
    private List<LanguageQualityIssue> lqiList = new LinkedList<LanguageQualityIssue>();
    private List<Provenance> provList = new LinkedList<Provenance>();
    private List<OtherITSMetadata> otherITSList = new LinkedList<OtherITSMetadata>();
    private SegmentController segmentListener;
    private SegmentVariant originalTarget;
    private ArrayList<String> targetDiff = new ArrayList<String>();

    public OcelotSegment(int segNum, SegmentVariant source, SegmentVariant target,
                         SegmentVariant originalTarget) {
        this.segmentNumber = segNum;
        this.source = source;
        this.target = target;
        if (originalTarget != null) {
            this.originalTarget = originalTarget;
            setOriginalTarget = true;

            this.targetDiff = EditDistance.styleTextDifferences(target, originalTarget);
        } else {
            this.originalTarget = target.createEmptyTarget();
        }
    }

    public void setSegmentListener(SegmentController segController) {
        this.segmentListener = segController;
    }

    public int getSegmentNumber() {
        return segmentNumber;
    }

    public SegmentVariant getSource() {
        return this.source;
    }

    public SegmentVariant getTarget() {
        return this.target;
    }

    public SegmentVariant getOriginalTarget() {
        return this.originalTarget;
    }

    public void setOriginalTarget(SegmentVariant oriTgt) {
        if (!this.setOriginalTarget) {
            this.originalTarget = oriTgt;
        }
        this.setOriginalTarget = true;
    }

    public boolean hasOriginalTarget() {
        return this.setOriginalTarget;
    }

    /**
     * Sets the new target for this segment.  Sets an original target value
     * if none exists.  Updates target diff value.
     * @param updatedTarget
     */
    public void updateTarget(SegmentVariant updatedTarget) {
        if (!updatedTarget.getDisplayText().equals(target.getDisplayText())) {
            if (!hasOriginalTarget()) {
                setOriginalTarget(target);
            }
            target = updatedTarget;
            editDistance = NO_EDIT_DISTANCE;
            setTargetDiff(EditDistance.styleTextDifferences(getTarget(),
                          getOriginalTarget()));
            if (segmentListener != null) {
                segmentListener.notifyUpdateSegment(this);
            }
        }
    }

    public void resetTarget() {
        if (setOriginalTarget) {
            updateTarget(getOriginalTarget());
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

    /**
     * XLIFF specific fields.
     */
    public abstract boolean isEditable();

    // Currently unused
    public abstract String getTransUnitId();

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

    public List<Provenance> getProv() {
        return provList;
    }

    public void setProv(List<Provenance> provList) {
        this.provList = provList;
    }

    public void addProvenance(Provenance prov) {
        provList.add(prov);
        if (segmentListener != null) {
            segmentListener.notifyAddedProv(prov);
        }
    }

    public boolean addedRWProvenance() {
        return addedProvenance;
    }

    public void setAddedRWProvenance(boolean flag) {
        addedProvenance = flag;
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
    }

    public void editedLQI(LanguageQualityIssue lqi) {
        if (segmentListener != null) {
            segmentListener.notifyModifiedLQI(lqi, this);
        }
    }

    public void removeLQI(LanguageQualityIssue removeLQI) {
        lqiList.remove(removeLQI);
        if (segmentListener != null) {
            segmentListener.notifyRemovedLQI(removeLQI, this);
        }
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
