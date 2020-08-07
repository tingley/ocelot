package com.vistatec.ocelot.plugins.lingotek;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import com.vistatec.ocelot.OcelotApp;
import com.vistatec.ocelot.OcelotApp.ErrorAlertException;
import com.vistatec.ocelot.Version;
import com.vistatec.ocelot.config.OcelotJsonConfigService;
import com.vistatec.ocelot.plugins.OpenProviderPlugin;

public class LingoTekPlugin implements OpenProviderPlugin {
	private JMenuItem menuItem;

	@Override
	public String getPluginName() {
		return "LingoTek";
	}

	@Override
	public String getPluginVersion() {
		return Version.BANNER;
	}

	public JMenuItem getMenuItem() {
		if (menuItem == null) {
			menuItem = new JMenuItem("Open from LingoTek");
			menuItem.setEnabled(false);
		}
		return menuItem;
	}

	@Override
	public File handleOpen(OcelotJsonConfigService configService, OcelotApp ocelotApp, JFrame parentFrame)
			throws ErrorAlertException {
		LingoTekManager manager = new LingoTekManager(configService.getLingoTekConfigurationParams());
		return manager.downloadFile(parentFrame, configService.getUserProvenance().getLangCode());
	}
}
