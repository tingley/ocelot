package com.vistatec.ocelot.plugins;

import javax.swing.JMenuItem;

/**
 * Plugin to register a custom handler in the "Save to.." menu and
 * handle its selection.
 */
public interface SaveProviderPlugin extends Plugin {

	JMenuItem getSaveMenuItem();

}
