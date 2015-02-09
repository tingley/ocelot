package com.vistatec.ocelot.segment.okapi;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.ITSLQIAnnotations;
import net.sf.okapi.common.annotation.ITSProvenanceAnnotations;
import net.sf.okapi.common.annotation.XLIFFTool;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;

import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.its.Provenance;
import com.vistatec.ocelot.segment.OcelotSegment;
import com.vistatec.ocelot.segment.SegmentController;
import com.vistatec.ocelot.segment.SegmentVariant;

public class OkapiXLIFF12Segment extends OcelotSegment {
    private Logger LOG = LoggerFactory.getLogger(OkapiXLIFF12Segment.class);

    private ITextUnit textUnit;
    private LocaleId tgtLocale;
    private String phase_name;

    public OkapiXLIFF12Segment(int segNum, SegmentVariant source, 
            SegmentVariant target, SegmentVariant originalTarget,
            ITextUnit textUnit, LocaleId tgtLocale) {
        super(segNum, source, target, originalTarget);
        this.textUnit = textUnit;
        this.tgtLocale = tgtLocale;
    }

    private String getITSRef() {
        return "RW" + getSegmentNumber();
    }

    @Override
    public void updateSegment() {
        updateITSLQIAnnotations(getITSRef());

        if (hasOriginalTarget()) {
            // Make sure the Okapi Event is aware that the target has changed.
            textUnit.setTarget(tgtLocale, unwrap(getTarget()));
            updateOriginalTarget(getSegmentController());
        }
    }

    @Override
    public void addNativeProvenance(Provenance addedProv) {
        // XXX: I want to move the code that resets is stuff into the writer.  But I can't, because
        // the writer doesn't know about the Ocelot model.  So for now, I let the common
        // code update the provenance list, and I synchronize it back to Okapi here.
        String ref = getITSRef();
        textUnit.setProperty(new Property(Property.ITS_PROV, " its:provenanceRecordsRef=\"#" + ref + "\""));
        textUnit.setAnnotation(getProvenanceAnnotations(ref));
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

    void updateITSLQIAnnotations(String rwRef) {
        ITSLQIAnnotations lqiAnns = new ITSLQIAnnotations();
        for (LanguageQualityIssue lqi : getLQI()) {
            GenericAnnotation ga = new GenericAnnotation(GenericAnnotationType.LQI,
                    GenericAnnotationType.LQI_TYPE, lqi.getType(),
                    GenericAnnotationType.LQI_COMMENT, lqi.getComment(),
                    GenericAnnotationType.LQI_SEVERITY, lqi.getSeverity(),
                    GenericAnnotationType.LQI_ENABLED, lqi.isEnabled());
            lqiAnns.add(ga);
        }

        if (lqiAnns.size() > 0) {
            textUnit.setProperty(new Property(Property.ITS_LQI, " its:locQualityIssuesRef=\"#"+rwRef+"\""));
            textUnit.setAnnotation(lqiAnns);
        } else {
            textUnit.setProperty(new Property(Property.ITS_LQI, ""));
            textUnit.setAnnotation(null);
        }
        lqiAnns.setData(rwRef);

        removeITSLQITextUnitSourceAnnotations();
        removeITSLQITextUnitTargetAnnotations();
    }

    void removeITSLQITextUnitSourceAnnotations() {
        TextContainer tc = unwrap(getSource());
        tc.setProperty(new Property(Property.ITS_LQI, ""));
        tc.setAnnotation(null);
        textUnit.setSource(tc);
    }

    void removeITSLQITextUnitTargetAnnotations() {
        Set<LocaleId> targetLocales = textUnit.getTargetLocales();
        if (targetLocales.isEmpty()) {
            // Unclear if this ever happens
            textUnit.setTarget(tgtLocale, unwrap(getTarget()));
        }
        else {
            TextContainer tgtTC = textUnit.getTarget(tgtLocale);
            tgtTC.setProperty(new Property(Property.ITS_LQI, ""));
            tgtTC.setAnnotation(null);
            textUnit.setTarget(tgtLocale, tgtTC);
        }
    }

    /**
     * Add an alt-trans containing the original target if one from this tool
     * doesn't exist already.
     * @param seg - Segment edited
     * @param segController
     */
    void updateOriginalTarget(SegmentController segController) {
        TextContainer segTarget = unwrap(getTarget());
        TextContainer segSource = unwrap(getSource());
        TextContainer segOriTarget = unwrap(getOriginalTarget());
        TextContainer oriTarget = new OkapiXLIFF12SegmentHelper(tgtLocale).retrieveOriginalTarget(segTarget);
        if (oriTarget == null) {
            // XXX Better way to get source locale ID?
            AltTranslation rwbAltTrans = new AltTranslation(LocaleId.fromString(segController.getFileSourceLang()),
                    tgtLocale, null,
                    segSource.getUnSegmentedContentCopy(), segOriTarget.getUnSegmentedContentCopy(),
                    MatchType.EXACT, 100, "Ocelot");
            XLIFFTool rwbAltTool = new XLIFFTool("Ocelot", "Ocelot");
            rwbAltTrans.setTool(rwbAltTool);
            AltTranslationsAnnotation altTrans = segTarget.getAnnotation(AltTranslationsAnnotation.class);
            altTrans = altTrans == null ? new AltTranslationsAnnotation() : altTrans;
            altTrans.add(rwbAltTrans);
            segTarget.setAnnotation(altTrans);
        }
    }

    private TextContainer unwrap(SegmentVariant v) {
        return ((TextContainerVariant)v).getTextContainer();
    }

    /**
     * Returns the current set of provenance objects as Okapi XLIFF 1.2
     * annotation objects.
     * @return
     */
    ITSProvenanceAnnotations getProvenanceAnnotations(String provRef) {
        ITSProvenanceAnnotations provAnns = new ITSProvenanceAnnotations();
        for (Provenance prov : getProvenance()) {
            GenericAnnotation ga = new GenericAnnotation(GenericAnnotationType.PROV,
                    GenericAnnotationType.PROV_PERSON, prov.getPerson(),
                    GenericAnnotationType.PROV_ORG, prov.getOrg(),
                    GenericAnnotationType.PROV_TOOL, prov.getTool(),
                    GenericAnnotationType.PROV_REVPERSON, prov.getRevPerson(),
                    GenericAnnotationType.PROV_REVORG, prov.getRevOrg(),
                    GenericAnnotationType.PROV_REVTOOL, prov.getRevTool(),
                    GenericAnnotationType.PROV_PROVREF, prov.getProvRef());
            provAnns.add(ga);
        }
        provAnns.setData(provRef);
        return provAnns;
    }
}
