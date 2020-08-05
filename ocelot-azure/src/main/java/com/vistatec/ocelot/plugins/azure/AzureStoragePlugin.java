package com.vistatec.ocelot.plugins.azure;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vistatec.ocelot.OcelotApp;
import com.vistatec.ocelot.Version;
import com.vistatec.ocelot.OcelotApp.ErrorAlertException;
import com.vistatec.ocelot.config.OcelotJsonConfigService;
import com.vistatec.ocelot.config.json.OcelotAzureConfig;
import com.vistatec.ocelot.plugins.SaveProviderPlugin;
import com.vistatec.ocelot.storage.model.PostUploadRequest;

public class AzureStoragePlugin implements SaveProviderPlugin {
	private static Logger LOG = LoggerFactory.getLogger(AzureStoragePlugin.class);
	private JMenuItem menuItem;

	public String getPluginName() {
		return "Azure Storage";
	}

	public String getPluginVersion() {
		return Version.BANNER;
	}

	public JMenuItem getSaveMenuItem() {
		if (menuItem == null) {
			menuItem = new JMenuItem("Save to Azure");
			menuItem.setEnabled(false);
		}
		return menuItem;
	}

	@Override
	public void handleSave(OcelotJsonConfigService configService, OcelotApp ocelotApp, JFrame parentFrame) throws ErrorAlertException {

		OcelotAzureConfig ocelotAzureConfiguration = configService.getOcelotAzureConfiguration();

		if (ocelotAzureConfiguration != null) {

			File tempFile = null;
			try {
				if (ocelotAzureConfiguration.isComplete()) {
					LOG.debug("Checking if this file has already been saved to Azure...");

					tempFile = File.createTempFile("ocelot", "azure");
					ocelotApp.saveFile(tempFile);

					AzureStorageService storageService = new AzureStorageService(ocelotAzureConfiguration.getSas(),
							ocelotAzureConfiguration.getBlobEndpoint(),
							ocelotAzureConfiguration.getQueueEndpoint());

					String fileId = UUID.randomUUID().toString();
					boolean uploadedFileToBlobStorage = storageService.uploadFileToBlobStorage(
							tempFile.getAbsolutePath(), "unprocessed", fileId, ocelotApp.getDefaultFileName());
					if (uploadedFileToBlobStorage) {
						LOG.debug("File with id " + fileId + " was uploaded to blob storage");

						PostUploadRequest postUploadRequest = Util.getPostUploadRequest(fileId);
						String json = Util.serializeToJson(postUploadRequest);
						LOG.debug("Post Upload Request for Storage Queue in json format is " + json);
						boolean messageSent = storageService.sendMessageToPostUploadQueue(json);
						if (!messageSent) {
							LOG.error("No message sent to Storage queue.");
						} else {
							LOG.info("Sent message to Storage queue.");
						}
						JOptionPane.showMessageDialog(parentFrame, "File successfully saved to Azure.",
								"Save to Azure", JOptionPane.INFORMATION_MESSAGE);
					} else {
						LOG.error("File with id " + fileId + " was not uploaded to blob storage");
						JOptionPane.showMessageDialog(parentFrame,
								"An error has occurred while saving the document to Azure. Please, try again.");
					}
				}
			} catch (IOException e) {
				LOG.error("Error while saving the document.", e);
				JOptionPane.showMessageDialog(parentFrame, "An error has occurred while saving the document.");
			} finally {
				if (tempFile != null) {
					tempFile.delete();
				}
			}
		}
	}
}
