package com.vistatec.ocelot.di;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.vistatec.ocelot.DefaultPlatformSupport;
import com.vistatec.ocelot.OSXPlatformSupport;
import com.vistatec.ocelot.OcelotApp;
import com.vistatec.ocelot.PlatformSupport;
import com.vistatec.ocelot.config.ConfigService;
import com.vistatec.ocelot.config.ConfigTransferService;
import com.vistatec.ocelot.config.ConfigTransferService.TransferException;
import com.vistatec.ocelot.config.Configs;
import com.vistatec.ocelot.config.DirectoryBasedConfigs;
import com.vistatec.ocelot.config.LQIXmlConfigTransferService;
import com.vistatec.ocelot.config.LqiConfigService;
import com.vistatec.ocelot.config.OcelotConfigService;
import com.vistatec.ocelot.config.OcelotXmlConfigTransferService;
import com.vistatec.ocelot.events.api.EventBusWrapper;
import com.vistatec.ocelot.events.api.OcelotEventQueue;
import com.vistatec.ocelot.findrep.FindAndReplaceController;
import com.vistatec.ocelot.lqi.LQIGridController;
import com.vistatec.ocelot.lqi.constants.LQIConstants;
import com.vistatec.ocelot.plugins.PluginManager;
import com.vistatec.ocelot.rules.RuleConfiguration;
import com.vistatec.ocelot.rules.RulesParser;
import com.vistatec.ocelot.services.ITSDocStatsService;
import com.vistatec.ocelot.services.OkapiXliffService;
import com.vistatec.ocelot.services.ProvenanceService;
import com.vistatec.ocelot.services.SegmentService;
import com.vistatec.ocelot.services.SegmentServiceImpl;
import com.vistatec.ocelot.services.XliffService;
import com.vistatec.ocelot.tm.TmManager;
import com.vistatec.ocelot.tm.TmPenalizer;
import com.vistatec.ocelot.tm.TmService;
import com.vistatec.ocelot.tm.TmTmxWriter;
import com.vistatec.ocelot.tm.gui.TmGuiManager;
import com.vistatec.ocelot.tm.okapi.OkapiTmManager;
import com.vistatec.ocelot.tm.okapi.OkapiTmService;
import com.vistatec.ocelot.tm.okapi.OkapiTmxWriter;
import com.vistatec.ocelot.tm.penalty.SimpleTmPenalizer;

/**
 * Main Ocelot object dependency context module.
 */
public class OcelotModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(OcelotModule.class);

    @Override
    protected void configure() {
        bind(OcelotEventQueue.class).toInstance(new EventBusWrapper(new EventBus()));
        bind(PlatformSupport.class).toInstance(getPlatformSupport());

        try {
            File ocelotDir = configureDirectories();
            Configs configs = new DirectoryBasedConfigs(ocelotDir);

            bind(ConfigService.class).toInstance(setupConfigService(ocelotDir));
			bind(LqiConfigService.class).toInstance(setupLQIConfigService(ocelotDir));
			bind(RuleConfiguration.class).toInstance(new RulesParser().loadConfig(configs.getRulesReader()));

            bind(SegmentService.class).to(SegmentServiceImpl.class).in(Scopes.SINGLETON);

            
            bind(TmManager.class).to(OkapiTmManager.class).in(Scopes.SINGLETON);
            bind(TmPenalizer.class).to(SimpleTmPenalizer.class);
            bind(TmService.class).to(OkapiTmService.class);
            bind(TmTmxWriter.class).to(OkapiTmxWriter.class);
            bind(TmGuiManager.class).in(Scopes.SINGLETON);
            bind(LQIGridController.class);
            
        } catch (IOException | JAXBException | ConfigTransferService.TransferException ex) {
            LOG.error("Failed to initialize configuration", ex);
            System.exit(1);
        }

        bind(XliffService.class).to(OkapiXliffService.class).in(Scopes.SINGLETON);
        bind(PluginManager.class).in(Scopes.SINGLETON);
        bind(ProvenanceService.class).in(Scopes.SINGLETON);
        bind(ITSDocStatsService.class).in(Scopes.SINGLETON);
        bind(FindAndReplaceController.class).in(Scopes.SINGLETON);

        bind(OcelotApp.class).in(Scopes.SINGLETON);
    }

    private File configureDirectories() {
        File ocelotDir = new File(System.getProperty("user.home"), ".ocelot");
        bindNamedFile(ocelotDir, "ocelotDir");
        bindNamedFile(new File(ocelotDir, "plugins"), "pluginDir");
        bindNamedFile(new File(ocelotDir, "tm"), "tmDir");
        return ocelotDir;
    }

    private File bindNamedFile(File file, String name) {
        file.mkdirs();
        bind(File.class).annotatedWith(Names.named(name)).toInstance(file);
        return file;
    }

    private PlatformSupport getPlatformSupport() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Mac")) {
            return new OSXPlatformSupport();
        }
        return new DefaultPlatformSupport();
    }

    private OcelotConfigService setupConfigService(File ocelotDir) throws ConfigTransferService.TransferException, JAXBException {
        File configFile = new File(ocelotDir, "ocelot_cfg.xml");
        ByteSource configSource = !configFile.exists() ?
                ByteSource.empty() :
                Files.asByteSource(configFile);

        CharSink configSink = Files.asCharSink(configFile,
                Charset.forName("UTF-8"));
		return new OcelotConfigService(new OcelotXmlConfigTransferService(
                configSource, configSink));
    }

	private LqiConfigService setupLQIConfigService(File ocelotDir)
	        throws TransferException, JAXBException {

		File configFile = new File(ocelotDir, "lqi_cfg.xml");
		ByteSource configSource = !configFile.exists() ? ByteSource.empty()
		        : Files.asByteSource(configFile);

		CharSink configSink = Files.asCharSink(configFile,
		        Charset.forName("UTF-8"));
		LqiConfigService service = new LqiConfigService(new LQIXmlConfigTransferService(
		        configSource, configSink));
		// If the config file doesn't exist, initialize a default configuration.
		if (!configFile.exists()) {
		    LOG.info("Writing default LQI Grid configuration to " + configFile);
		    service.saveLQIConfig(LQIConstants.getDefaultLQIGrid());
		}
		return service;
	}
}