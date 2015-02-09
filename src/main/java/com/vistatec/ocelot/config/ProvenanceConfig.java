package com.vistatec.ocelot.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vistatec.ocelot.its.Provenance;
import com.vistatec.ocelot.its.ProvenanceFactory;

/**
 * Class that manages user-configured provenance information.
 */
public class ProvenanceConfig {
    private Logger LOG = LoggerFactory.getLogger(ProvenanceConfig.class);

    private Configs configs;
    private Properties p = new Properties();

    protected ProvenanceConfig() { }

    public ProvenanceConfig(Configs configs) {
        this.configs = configs;
        reload();
    }

    public void reload() {
        p = new Properties();
        try {
            Reader r = configs.getProvenanceReader();
            if (r != null) {
                p.load(r);
                r.close();
            }
        }
        catch (IOException e) {
            LOG.warn("Failed to load user provenance information", e);
        }
    }

    public boolean isEmpty() {
        return getUserProvenance().isEmpty();
    }

    protected String getRevPerson() {
        return p.getProperty("revPerson");
    }

    protected String getRevOrg() {
        return p.getProperty("revOrganization");
    }

    protected String getExternalReference() {
        return p.getProperty("externalReference");
    }

    public Provenance getUserProvenance() {
        return ProvenanceFactory.fromUserFields(getRevPerson(),
                                                getRevOrg(),
                                                getExternalReference());
    }

    public void save(Provenance prov) throws IOException {
        p.setProperty("revPerson", prov.getRevPerson());
        p.setProperty("revOrganization", prov.getRevOrg());
        p.setProperty("externalReference", prov.getProvRef());
        Writer w = configs.getProvenanceWriter();
        p.store(w, null);
        w.close();
    }
}
