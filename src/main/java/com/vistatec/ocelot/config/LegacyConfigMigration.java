package com.vistatec.ocelot.config;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class LegacyConfigMigration {
    private Configs configs;

    public LegacyConfigMigration(Configs configs) {
        this.configs = configs;
    }

    // XXX I need to refactor this so that I can pass in the reader/writer
    // and run unittest on the transform.
    // I should also probably -always- be running this, or something.
    public RootConfig loadLegacyConfiguration() throws IOException, TransformerException {
        RootConfig root = new RootConfig();

        StreamSource stylesource = new StreamSource(getMigrationXSL());
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(stylesource);

        Source xmlSource = new StreamSource(configs.getOcelotReader());
        Result outputTarget = new StreamResult(configs.getOcelotWriter());
        transformer.transform(xmlSource, outputTarget);

        return root;
    }

    private InputStream getMigrationXSL() throws IOException {
        return getClass().getResourceAsStream("/migrate-legacy-config.xsl");
    }

}
