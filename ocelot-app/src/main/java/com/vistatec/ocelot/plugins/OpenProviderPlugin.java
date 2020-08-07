package com.vistatec.ocelot.plugins;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import com.vistatec.ocelot.OcelotApp;
import com.vistatec.ocelot.OcelotApp.ErrorAlertException;
import com.vistatec.ocelot.config.OcelotJsonConfigService;

public interface OpenProviderPlugin extends Plugin {

	JMenuItem getMenuItem();

	File handleOpen(OcelotJsonConfigService configService, OcelotApp ocelotApp, JFrame parentFrame)
			throws ErrorAlertException;
}
