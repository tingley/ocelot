package com.vistatec.ocelot.its;

import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;

public class ProvenanceFactory {

    public static Provenance fromOkapiXLIFF12Annotation(GenericAnnotation ga) {
        Provenance prov = new Provenance();
        if (ga.getString(GenericAnnotationType.PROV_RECSREF) != null) {
            prov.setRecsRef(ga.getString(GenericAnnotationType.PROV_RECSREF));
        }
        if (ga.getString(GenericAnnotationType.PROV_PERSON) != null) {
            prov.setPerson(ga.getString(GenericAnnotationType.PROV_PERSON));
        }
        if (ga.getString(GenericAnnotationType.PROV_ORG) != null) {
            prov.setOrg(ga.getString(GenericAnnotationType.PROV_ORG));
        }
        if (ga.getString(GenericAnnotationType.PROV_TOOL) != null) {
            prov.setTool(ga.getString(GenericAnnotationType.PROV_TOOL));
        }
        if (ga.getString(GenericAnnotationType.PROV_REVPERSON) != null) {
            prov.setRevPerson(ga.getString(GenericAnnotationType.PROV_REVPERSON));
        }
        if (ga.getString(GenericAnnotationType.PROV_REVORG) != null) {
            prov.setRevOrg(ga.getString(GenericAnnotationType.PROV_REVORG));
        }
        if (ga.getString(GenericAnnotationType.PROV_REVTOOL) != null) {
            prov.setRevTool(ga.getString(GenericAnnotationType.PROV_REVTOOL));
        }
        if (ga.getString(GenericAnnotationType.PROV_PROVREF) != null) {
            prov.setProvRef(ga.getString(GenericAnnotationType.PROV_PROVREF));
        }
        return prov;
    }

    public static Provenance fromOkapiXLIFF20Provenance(net.sf.okapi.lib.xliff2.its.Provenance p) {
        Provenance prov = new Provenance();
        if (p.getPerson() != null) {
            prov.setPerson(p.getPerson());
        }
        if (p.getOrg() != null) {
            prov.setOrg(p.getOrg());
        }
        if (p.getTool() != null) {
            prov.setTool(p.getTool());
        }
        if (p.getRevPerson() != null) {
            prov.setRevPerson(p.getRevPerson());
        }
        if (p.getRevOrg() != null) {
            prov.setRevOrg(p.getRevOrg());
        }
        if (p.getRevTool() != null) {
            prov.setRevTool(p.getRevTool());
        }
        if (p.getProvRef() != null) {
            prov.setProvRef(p.getProvRef());
        }
        return prov;
    }

    public static Provenance fromUserFields(String revPerson, String revOrg, String externalRef) {
        Provenance prov = new Provenance();
        prov.setRevPerson(revPerson);
        prov.setRevOrg(revOrg);
        prov.setProvRef(externalRef);
        return prov;
    }
}
