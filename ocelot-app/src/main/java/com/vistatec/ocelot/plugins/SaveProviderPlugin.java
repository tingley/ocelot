package com.vistatec.ocelot.plugins;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import com.vistatec.ocelot.OcelotApp;
import com.vistatec.ocelot.OcelotApp.ErrorAlertException;
import com.vistatec.ocelot.config.OcelotJsonConfigService;

/**
 * Plugin to register a custom handler in the "Save to.." menu and
 * handle its selection.
 */
public interface SaveProviderPlugin extends Plugin {

	JMenuItem getSaveMenuItem();

	void handleSave(OcelotJsonConfigService configService, OcelotApp ocelotApp, JFrame parentFrame) throws ErrorAlertException;
}
