package com.vistatec.ocelot.segment.okapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.ITSLQIAnnotations;
import net.sf.okapi.common.annotation.ITSProvenanceAnnotations;
import net.sf.okapi.common.annotation.XLIFFPhase;
import net.sf.okapi.common.annotation.XLIFFPhaseAnnotation;
import net.sf.okapi.common.annotation.XLIFFTool;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;

import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.its.OtherITSMetadata;
import com.vistatec.ocelot.its.Provenance;
import com.vistatec.ocelot.its.ProvenanceFactory;
import com.vistatec.ocelot.rules.DataCategoryField;
import com.vistatec.ocelot.rules.StateQualifier;
import com.vistatec.ocelot.segment.OcelotSegment;

public class OkapiXLIFF12SegmentHelper {
    private static Logger LOG = LoggerFactory.getLogger(OkapiXLIFF12SegmentHelper.class);

    private LocaleId tgtLocale;

    public OkapiXLIFF12SegmentHelper(LocaleId tgtLocale) {
        this.tgtLocale = tgtLocale;
    }

    public void setTargetLocale(LocaleId tgtLocale) {
        this.tgtLocale = tgtLocale;
    }

    public LocaleId getTargetLocale() {
        return tgtLocale;
    }

    public static TextContainer getTargetTextContainer(ITextUnit tu, LocaleId tgtLocale) {
        TextContainer tgtTu = tu.getTarget(tgtLocale);
        if (tgtTu == null) {
            tgtTu = new TextContainer();
            tu.setTarget(tgtLocale, tgtTu);
        }
        return tgtTu;
    }

    // XXX Maybe move this back to the parser
    public OkapiXLIFF12Segment convertTextUnitToSegment(ITextUnit tu, int documentSegmentNum,
                                                        LocaleId srcLocale) {
        TextContainer tgtTu = getTargetTextContainer(tu, tgtLocale);

        OkapiXLIFF12Segment seg = new OkapiXLIFF12Segment(documentSegmentNum, tu,
                                        srcLocale, tgtLocale);
        Property stateQualifier = tgtTu.getProperty("state-qualifier");
        if (stateQualifier != null) {
            StateQualifier sq = StateQualifier.get(stateQualifier.getValue());
            if (sq != null) {
                seg.setStateQualifier(sq);
            }
            else {
                LOG.info("Ignoring state-qualifier value '" + 
                         stateQualifier.getValue() + "'");
            }
        }
        XLIFFPhaseAnnotation phaseAnn = tu.getAnnotation(XLIFFPhaseAnnotation.class);
        if (phaseAnn != null) {
            XLIFFPhase refPhase = phaseAnn.getReferencedPhase();
            seg.setPhaseName(refPhase.getPhaseName());
        }
        attachITSDataToSegment(seg, tu, tu.getSource(), tgtTu);
        return seg;
    }

    public TextContainer retrieveOriginalTarget(TextContainer target) {
        AltTranslationsAnnotation altTrans = target.getAnnotation(AltTranslationsAnnotation.class);
        if (altTrans != null) {
            Iterator<AltTranslation> iterAltTrans = altTrans.iterator();
            while (iterAltTrans.hasNext()) {
                AltTranslation altTran = iterAltTrans.next();
                // Check if alt-trans is Ocelot generated.
                XLIFFTool altTool = altTran.getTool();
                if (altTool != null && altTool.getName().equals("Ocelot")) {
                    // We should be able to replace this with |return altTrans.getTarget;|
                    // once an issue with the XLIFF reader is fixed (Okapi 412).
                    ITextUnit tu = altTran.getEntry();
                    for ( LocaleId trg : tu.getTargetLocales() ) {
                        return altTran.getTarget(); // If there is a target return it
                    }
                    // No target: create one empty
                    return tu.createTarget(getTargetLocale(), true, IResource.CREATE_EMPTY);
                }
            }
        }
        return null;
    }

    private void attachITSDataToSegment(OcelotSegment seg, ITextUnit tu, TextContainer srcTu, TextContainer tgtTu) {
        ITSLQIAnnotations lqiAnns = retrieveITSLQIAnnotations(tu, srcTu, tgtTu);
        List<LanguageQualityIssue> lqiList = new ArrayList<LanguageQualityIssue>();
        for (GenericAnnotation ga : lqiAnns.getAnnotations(GenericAnnotationType.LQI)) {
            lqiList.add(new LanguageQualityIssue(ga));
            seg.setLQIID(lqiAnns.getData());
        }
        seg.setLQI(lqiList);

        ITSProvenanceAnnotations provAnns = retrieveITSProvAnnotations(tu, srcTu, tgtTu);
        List<GenericAnnotation> provAnnList = provAnns.getAnnotations(GenericAnnotationType.PROV);
        if (provAnnList != null) {
            List<Provenance> provList = new ArrayList<Provenance>();
            for (GenericAnnotation ga : provAnnList) {
                provList.add(ProvenanceFactory.fromOkapiXLIFF12Annotation(ga));
                seg.setProvID(provAnns.getData());
            }
            seg.setProv(provList);
        }

        if (tgtTu != null) {
            List<OtherITSMetadata> otherList = new ArrayList<OtherITSMetadata>();
            for (GenericAnnotation mtAnn : retrieveITSMTConfidenceAnnotations(tgtTu)) {
                otherList.add(new OtherITSMetadata(DataCategoryField.MT_CONFIDENCE,
                        mtAnn.getDouble(GenericAnnotationType.MTCONFIDENCE_VALUE)));
            }
            seg.setOtherITSMetadata(otherList);
        }
    }

    private ITSLQIAnnotations retrieveITSLQIAnnotations(ITextUnit tu, TextContainer srcTu, TextContainer tgtTu) {
        ITSLQIAnnotations lqiAnns = tu.getAnnotation(ITSLQIAnnotations.class);
        lqiAnns = lqiAnns == null ? new ITSLQIAnnotations() : lqiAnns;

        ITSLQIAnnotations srcLQIAnns = srcTu.getAnnotation(ITSLQIAnnotations.class);
        if (srcLQIAnns != null) {
            lqiAnns.addAll(srcLQIAnns);
        }
        if (tgtTu != null) {
            ITSLQIAnnotations tgtLQIAnns = tgtTu.getAnnotation(ITSLQIAnnotations.class);
            if (tgtLQIAnns != null) {
                lqiAnns.addAll(tgtLQIAnns);
            }
        }
        return lqiAnns;
    }

    private ITSProvenanceAnnotations retrieveITSProvAnnotations(ITextUnit tu, TextContainer srcTu, TextContainer tgtTu) {
        ITSProvenanceAnnotations provAnns = tu.getAnnotation(ITSProvenanceAnnotations.class);
        provAnns = provAnns == null ? new ITSProvenanceAnnotations() : provAnns;

        ITSProvenanceAnnotations srcProvAnns = srcTu.getAnnotation(ITSProvenanceAnnotations.class);
        if (srcProvAnns != null) {
            provAnns.addAll(srcProvAnns);
        }

        if (tgtTu != null) {
            ITSProvenanceAnnotations tgtProvAnns = tgtTu.getAnnotation(ITSProvenanceAnnotations.class);
            if (tgtProvAnns != null) {
                provAnns.addAll(tgtProvAnns);
            }
        }
        return provAnns;
    }

    private List<GenericAnnotation> retrieveITSMTConfidenceAnnotations(TextContainer tgtTu) {
        GenericAnnotations tgtAnns = tgtTu.getAnnotation(GenericAnnotations.class);
        List<GenericAnnotation> mtAnns = new LinkedList<GenericAnnotation>();
        if (tgtAnns != null) {
            mtAnns = tgtAnns.getAnnotations(GenericAnnotationType.MTCONFIDENCE);
        }
        return mtAnns;
    }
}
