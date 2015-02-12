package com.vistatec.ocelot.segment.okapi;

import java.util.List;

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
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.its.Provenance;
import com.vistatec.ocelot.segment.BaseSegmentVariant;
import com.vistatec.ocelot.segment.CodeAtom;
import com.vistatec.ocelot.segment.OcelotSegment;
import com.vistatec.ocelot.segment.SegmentAtom;
import com.vistatec.ocelot.segment.SegmentVariant;

public class OkapiXLIFF12Segment extends OcelotSegment {
    private Logger LOG = LoggerFactory.getLogger(OkapiXLIFF12Segment.class);

    private ITextUnit textUnit;
    private LocaleId srcLocale, tgtLocale;
    private String phase_name;

    public OkapiXLIFF12Segment(int segNum, ITextUnit textUnit,
                               LocaleId srcLocale, LocaleId tgtLocale) {
        super(segNum);
        this.textUnit = textUnit;
        this.srcLocale = srcLocale;
        this.tgtLocale = tgtLocale;
    }

    private String getITSRef() {
        return "RW" + getSegmentNumber();
    }

    protected SegmentVariant getSourceVariant() {
        return new OkapiXLIFF12VariantHelper().createVariant(textUnit.getSource());
    }

    protected SegmentVariant getTargetVariant() {
        return new OkapiXLIFF12VariantHelper().createVariant(
                        OkapiXLIFF12SegmentHelper.getTargetTextContainer(textUnit, tgtLocale));
    }
    protected SegmentVariant getOriginalTargetVariant() {
        TextContainer oriTgtTu = new OkapiXLIFF12SegmentHelper(tgtLocale)
                .retrieveOriginalTarget(OkapiXLIFF12SegmentHelper.getTargetTextContainer(textUnit, tgtLocale));
        return oriTgtTu != null ?
                new OkapiXLIFF12VariantHelper().createVariant(oriTgtTu) : null;
    }
    protected void setTargetVariant(SegmentVariant target) {
        setAtomsForTextContainer(
                OkapiXLIFF12SegmentHelper.getTargetTextContainer(textUnit, tgtLocale),
                ((BaseSegmentVariant)target).getAtoms());
    }
    protected void setOriginalTargetVariant(SegmentVariant originalTarget) {
        // I'm ignoring the argument here and just copying
        // the existing target to original target.
        // XXX Is the argument needed?
        // The Okapi representation of the target hasn't been changed yet, so I can
        // do this safely (I think)
        TextContainer segTarget = textUnit.getTarget(tgtLocale); // XXX can this crash?
        TextContainer segSource = textUnit.getSource();
        TextContainer oriTarget = new OkapiXLIFF12SegmentHelper(tgtLocale).retrieveOriginalTarget(segTarget);
        if (oriTarget == null) {
            // XXX Better way to get source locale ID?
            AltTranslation rwbAltTrans = new AltTranslation(srcLocale, tgtLocale, null,
                    segSource.getUnSegmentedContentCopy(), segTarget.getUnSegmentedContentCopy(),
                    MatchType.EXACT, 100, "Ocelot");
            XLIFFTool rwbAltTool = new XLIFFTool("Ocelot", "Ocelot");
            rwbAltTrans.setTool(rwbAltTool);
            AltTranslationsAnnotation altTrans = segTarget.getAnnotation(AltTranslationsAnnotation.class);
            altTrans = altTrans == null ? new AltTranslationsAnnotation() : altTrans;
            altTrans.add(rwbAltTrans);
            segTarget.setAnnotation(altTrans);
        }
    }

    @Override
    protected void addNativeLQI(LanguageQualityIssue addedLQI) {
        updateITSLQIAnnotations(getITSRef());
    }

    @Override
    protected void modifyNativeLQI(LanguageQualityIssue modifiedLQI) {
        updateITSLQIAnnotations(getITSRef());
    }

    @Override
    protected void removeNativeLQI(LanguageQualityIssue removedLQI) {
        updateITSLQIAnnotations(getITSRef());
    }

    @Override
    public void addNativeProvenance(Provenance addedProv) {
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

    private void setAtomsForTextContainer(TextContainer tc, List<SegmentAtom> atoms) {
        // Unfortunately, TextContainers can't view all of the codes
        // they contain.
        List<Code> tcCodes = tc.getUnSegmentedContentCopy().getCodes();
        TextFragment frag = new TextFragment();
        for (SegmentAtom atom : atoms) {
            if (atom instanceof CodeAtom) {
                CodeAtom codeAtom = (CodeAtom) atom;
                Code c = tcCodes.get( Integer.parseInt(codeAtom.getId()) );
                frag.append(c);
            }
            else {
                frag.append(atom.getData());
            }
        }
        tc.setContent(frag);
    }

    /**
     * This is a bit crude, but there are a few ways it can go wrong.  This
     * re-synchronizes the LQI issue list to the Okapi representation.  The main
     * reason to do it this way is that we need to gather up all the LQIs so
     * we don't write them out in multiple different places.
     */
    private void updateITSLQIAnnotations(String rwRef) {
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

        // Remove any ITS annotations that might exist on the source or
        // target level.
        removeTextContainerITSAnnotations(textUnit.getSource());
        removeTextContainerITSAnnotations(textUnit.getTarget(tgtLocale));
    }

    void removeTextContainerITSAnnotations(TextContainer tc) {
        tc.setProperty(new Property(Property.ITS_LQI, ""));
        tc.setAnnotation(null);
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
