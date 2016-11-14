package com.vistatec.ocelot.tm.okapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.okapi.tm.pensieve.seeker.PensieveSeeker;
import net.sf.okapi.tm.pensieve.writer.PensieveWriter;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vistatec.ocelot.config.ConfigService;
import com.vistatec.ocelot.config.ConfigTransferService;
import com.vistatec.ocelot.config.ConfigTransferService.TransferException;
import com.vistatec.ocelot.config.xml.TmManagement;
import com.vistatec.ocelot.config.xml.TmManagement.TmConfig;
import com.vistatec.ocelot.tm.TmManager;
import com.vistatec.ocelot.tm.TmTmxWriter;

/**
 * Use Okapi Pensieve to perform functionality expected of a {@link TmManager}.
 */
public class OkapiTmManager implements TmManager {
	private static final Logger LOG = LoggerFactory
	        .getLogger(OkapiTmManager.class);
	private final File tmRootDir;
	private final ConfigService cfgService;
	private final TmTmxWriter tmxWriter;

	@Inject
	public OkapiTmManager(@Named("tmDir") File tmDir, ConfigService cfgService,
	        TmTmxWriter tmxWriter) throws IOException,
	        ConfigTransferService.TransferException {
		this.tmRootDir = tmDir;
		this.cfgService = cfgService;
		this.tmxWriter = tmxWriter;
		discover();
	}

	private void discover() throws IOException,
	        ConfigTransferService.TransferException {
		Set<String> configuredTms = new HashSet<>();

		List<TmConfig> tms = new ArrayList<TmManagement.TmConfig>(this.cfgService.getTms());
		for (TmManagement.TmConfig tm : tms) {
			if (verifyExistingTm(tm)) {
				configuredTms.add(tm.getTmName());
			}
		}

		for (String tmName : this.tmRootDir.list()) {
			if (!configuredTms.contains(tmName)) {
				initializeNewTm(tmName, new File(
				        constructDefaultTmDataDir(tmName)));
			}
		}
	}

	/**
	 * If TM config points to a non-existent directory for the root or data,
	 * delete the configuration; otherwise generate Pensieve index if missing.
	 */
	private boolean verifyExistingTm(TmManagement.TmConfig tmConfig)
	        throws IOException, ConfigTransferService.TransferException {
		File tmDir = new File(this.tmRootDir, tmConfig.getTmName());
		if (!tmDir.exists()) {
			removeTmConfig(tmConfig);
			return false;
		}

		// File tmDataDir = new File(tmConfig.getTmDataDir());
		// if (!tmDataDir.exists() || !tmDataDir.isDirectory() ||
		// tmDataDir.listFiles().length <= 0) {
//		if (!checkTmExists(tmConfig)) {
//			deleteTm(tmConfig.getTmName());
//			return false;
//
//		} else {
//			File pensieve = getDefaultPensieveDir(tmConfig.getTmName());
//			if (!pensieve.exists()) {
//				regenerateTm(tmConfig.getTmName());
//			}
//			return true;
//		}
		File tmDataDir = new File(tmConfig.getTmDataDir());
        if (!tmDataDir.exists() || !tmDataDir.isDirectory() || tmDataDir.listFiles().length <= 0) {
            deleteTm(tmConfig.getTmName());
            return false;

        } else {
            File pensieve = getDefaultPensieveDir(tmConfig.getTmName());
            if (!pensieve.exists()) {
                regenerateTm(tmConfig.getTmName());
            }
            return true;
        }
	}

	private boolean checkTmExists(TmConfig tm) {

		boolean exists = true;
		File tmDir = new File(tm.getTmDataDir());
		if (tmDir.exists() && tm.getTmxFiles() != null
		        && tm.getTmxFiles().getTmxFile() != null && !tm.getTmxFiles().getTmxFile().isEmpty()) {
			File tmxFile = null;
			for (String tmxFileName : tm.getTmxFiles().getTmxFile()) {
				tmxFile = new File(tm.getTmDataDir(), tmxFileName);
				if (!tmxFile.exists()) {
					exists = false;
					break;
				}
			}
		} else {
			exists = false;
		}
		return exists;
	}

	@Override
	public void initializeNewTm(String tmName, File tmDataDir)
	        throws IOException, ConfigTransferService.TransferException {
		LOG.debug("Creating new TM '{}' with data directory '{}'", tmName,
		        tmDataDir.getAbsolutePath());
		if (cfgService.getTmConfig(tmName) != null) {
			String errorMsg = "TM '"
			        + tmName
			        + "' already exists, cannot create a new TM with data dir '"
			        + tmDataDir.getAbsolutePath() + "'";
			LOG.error(errorMsg);
			throw new IOException(errorMsg);
		}

		if (tmDataDir.exists()) {
			File[] tmxFiles = tmDataDir.listFiles();
			if (tmxFiles != null && tmxFiles.length > 0) {
				cfgService.createNewTmConfig(tmName, true,
				        tmDataDir.getAbsolutePath());
				for (File tmx : tmxFiles) {
					indexTmx(tmName, tmx);
				}
			} else {
				LOG.error("No files in given directory '{}'",
				        tmDataDir.getAbsolutePath());
				throw new IOException("No files in given directory '"
				        + tmDataDir.getAbsolutePath() + "'");
			}
		} else {
			LOG.error("Data directory '{}' does not exist",
			        tmDataDir.getAbsolutePath());
			throw new IOException("Data directory '"
			        + tmDataDir.getAbsolutePath() + "' does not exist");
		}
	}

	@Override
	public void initializeNewTm(String tmName, File[] tmxFiles)
	        throws IOException, TransferException {

		if (tmxFiles != null && tmxFiles.length > 0) {
			File tmDataDir = new File(tmxFiles[0].getParent());
			LOG.debug("Creating new TM '{}' with data directory '{}'", tmName,
			        tmDataDir.getAbsolutePath());
			if (cfgService.getTmConfig(tmName) != null) {
				String errorMsg = "TM '"
				        + tmName
				        + "' already exists, cannot create a new TM with data dir '"
				        + tmDataDir.getAbsolutePath() + "'";
				LOG.error(errorMsg);
				throw new IOException(errorMsg);
			}
			if (tmDataDir.exists()) {
				List<String> configTmxFiles = new ArrayList<String>();
				for(File tmxFile: tmxFiles){
					configTmxFiles.add(tmxFile.getName());
				}
				cfgService.createNewTmConfig(tmName, true,
				        tmDataDir.getAbsolutePath(), configTmxFiles);
				for (File tmx : tmxFiles) {
					indexTmx(tmName, tmx);
				}
			} else {
				LOG.error("Data directory '{}' does not exist",
				        tmDataDir.getAbsolutePath());
				throw new IOException("Data directory '"
				        + tmDataDir.getAbsolutePath() + "' does not exist");
			}
//		} else {
			// LOG.error("No files in given directory '{}'",
			// tmDataDir.getAbsolutePath());
			// throw new
			// IOException("No files in given directory '"+tmDataDir.getAbsolutePath()+"'");
		}

	}

	// private void indexTmx(String tmName, File[] tmxFiles){
	//
	// }

	@Override
	public void deleteTm(String tmName) throws IOException,
	        ConfigTransferService.TransferException {
		TmManagement.TmConfig config = cfgService.getTmConfig(tmName);
		if (config == null) {
			throw new IOException("'" + tmName + "' missing TM configuration!");
		}

		String defaultTmDataDir = constructDefaultTmDataDir(tmName);
		if (defaultTmDataDir.equals(config.getTmDataDir())) {
			deleteDefaultTmDataDirectory(tmName);
		}

		deletePensieveIndex(tmName);
		deleteFileDirectory(new File(tmRootDir, tmName));
		removeTmConfig(config);
	}

	@Override
	public void saveOpenFileAsTmx(File tmx) throws IOException {
		this.tmxWriter.exportTmx(tmx);
	}

	private void removeTmConfig(TmManagement.TmConfig config)
	        throws ConfigTransferService.TransferException {
		List<TmManagement.TmConfig> configs = cfgService.getTms();
		configs.remove(config);
		cfgService.saveConfig();
	}

	private void deleteDefaultTmDataDirectory(String tmName) throws IOException {
		File dataDir = new File(constructDefaultTmDataDir(tmName));
		try {
			deleteFileDirectory(dataDir);
		} catch (IOException e) {
			LOG.error("Failed to delete TM '" + tmName + "' data directory", e);
			throw e;
		}
	}

	private void deletePensieveIndex(String tmName) throws IOException {
		File pensieveIndex = getDefaultPensieveDir(tmName);
		try {
			deleteFileDirectory(pensieveIndex);
		} catch (IOException e) {
			LOG.error(
			        "Failed to delete TM '" + tmName + "' pensieve directory",
			        e);
			throw e;
		}
	}

	/**
	 * Delete directory and contents only if the contents underneath it are
	 * files.
	 */
	private void deleteFileDirectory(File dir) throws IOException {
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new IOException("'" + dir.getAbsolutePath()
				        + "' is not a directory!");
			}
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					file.delete();
				} else {
					throw new IOException("'" + file.getAbsolutePath()
					        + "' is not a file, cannot delete directory '"
					        + dir.getAbsolutePath() + "'");
				}
			}
			dir.delete();
		}
	}

	@Override
	public void regenerateTm(String tmName) throws IOException {
		deletePensieveIndex(tmName);
		TmManagement.TmConfig config = cfgService.getTmConfig(tmName);
		File tmDataDir = new File(config.getTmDataDir());
		for (File tmx : tmDataDir.listFiles()) {
			indexTmx(tmName, tmx);
		}
	}

	private String constructDefaultTmDataDir(String tmName) {
		String fileSeparator = System.getProperty("file.separator");
		return this.tmRootDir.getAbsolutePath() + fileSeparator + tmName
		        + fileSeparator + "tmx";
	}

	private File getDefaultPensieveDir(String tmName) {
		File fileTm = new File(tmRootDir, tmName);
		if (!fileTm.exists()) {
			fileTm.mkdirs();
		}

		return new File(fileTm, "pensieve");
	}

	@Override
	public void changeTmDataDir(String tmName, File tmDataDir)
	        throws IOException, ConfigTransferService.TransferException {
		TmManagement.TmConfig tmConfig = cfgService.getTmConfig(tmName);
		if (tmConfig == null) {
			throw new IOException("TM '" + tmName + "' does not exist!");
		}

		if (!tmConfig.getTmDataDir().equals(tmDataDir.getAbsolutePath())) {
			LOG.debug("Set TM data directory from '{}' to '{}'",
			        tmConfig.getTmDataDir(), tmDataDir.getAbsolutePath());

			for (File tmxFile : tmDataDir.listFiles()) {
				indexTmx(tmName, tmxFile);
			}
			cfgService.saveTmDataDir(tmConfig, tmDataDir.getAbsolutePath());
		}
		LOG.debug(
		        "Setting TM data directory to itself '{} to {}', doing nothing",
		        tmConfig.getTmDataDir(), tmDataDir.getAbsolutePath());
	}

	@Override
	public void importTmx(String tmName, File tmx) throws IOException {
		if (!tmx.exists()) {
			throw new IOException("File '" + tmx.getAbsolutePath()
			        + "' does not exist");
		}

		TmManagement.TmConfig config = cfgService.getTmConfig(tmName);
		File tmDataDir = new File(
		        (config == null) ? constructDefaultTmDataDir(tmName)
		                : config.getTmDataDir());
		if (tmDataDir.exists()) {
			for (String file : tmDataDir.list()) {
				if (file.equals(tmx.getName())) {
					String errMsg = "File with the same name '" + tmx.getName()
					        + "' already exists in "
					        + tmDataDir.getAbsolutePath();
					LOG.warn(errMsg);
					throw new IOException(errMsg);
				}
			}
		} else {
			tmDataDir.mkdirs();
		}

		ByteSource tmxSource = Files.asByteSource(tmx);
		tmxSource.copyTo(new FileOutputStream(new File(tmDataDir, tmName)));

		if (config == null) {
			try {
				cfgService.createNewTmConfig(tmName, true,
				        tmDataDir.getAbsolutePath());
			} catch (ConfigTransferService.TransferException e) {
				String errorMsg = "Failed to create new TM configuration for '"
				        + tmName + "' when importing file '"
				        + tmx.getAbsolutePath() + "'";
				LOG.error(errorMsg, e);
				throw new IOException(errorMsg, e);
			}
		}
		indexTmx(tmName, tmx);
	}

	private void indexTmx(String tmName, File tmx) throws IOException {
		DirectoryWrapper luceneIndex;
		try {
			luceneIndex = loadTm(tmName);
		} catch (ConfigTransferService.TransferException e) {
			LOG.error(
			        "Failed to save new TM configuration for '{}' when importing '{}'",
			        tmName, tmx.getAbsolutePath());
			throw new IOException(e);
		}

		PensieveWriter writer = new PensieveWriter(luceneIndex.luceneDir,
		        !luceneIndex.hasPensieveIndex);
		OkapiTmTmxImporter parser = new OkapiTmTmxImporter();
		parser.parse(tmx, writer);
		writer.close();
	}

	/**
	 * Ensure the given TM is visible by the TM manager
	 */
	private DirectoryWrapper loadTm(String tmName) throws IOException,
	        ConfigTransferService.TransferException {
		if (cfgService.getTmConfig(tmName) == null) {
			throw new IOException(tmName + " is missing a TM configuration!");
		}

		File pensieveIndex = getDefaultPensieveDir(tmName);
		return new DirectoryWrapper(FSDirectory.open(pensieveIndex),
		        pensieveIndex.exists());
	}

	/**
	 * Return searchable TMs, in the TmConfig list order.
	 */
	Iterator<TmPair> getSeekers() throws IOException {
		List<TmPair> seekers = new ArrayList<>();
		for (TmManagement.TmConfig tm : this.cfgService.getTms()) {
			try {
				DirectoryWrapper luceneIndex = loadTm(tm.getTmName());
				seekers.add(new TmPair(tm.getTmName(), new PensieveSeeker(
				        luceneIndex.luceneDir)));
			} catch (ConfigTransferService.TransferException e) {
				LOG.error("Failed to create TM config for '" + tm.getTmName()
				        + "'", e);
			}
		}
		return seekers.iterator();
	}

	@Override
	public List<TmManagement.TmConfig> fetchTms() {
		return cfgService.getTms();
	}

	@Override
	public TmManagement.TmConfig fetchTm(String tmName) {
		return cfgService.getTmConfig(tmName);
	}

	@Override
	public void saveTmOrdering(List<TmManagement.TmConfig> orderedTms)
	        throws ConfigTransferService.TransferException {
		cfgService.saveTms(orderedTms);
	}

	/**
	 * Mapping between the TM name and the PensieveSeeker used to search the
	 * Lucence index generated by Pensieve.
	 */
	static class TmPair {
		private final String tmOrigin;
		private final PensieveSeeker seeker;

		public TmPair(String tmOrigin, PensieveSeeker seeker) {
			this.tmOrigin = tmOrigin;
			this.seeker = seeker;
		}

		public String getTmOrigin() {
			return tmOrigin;
		}

		public PensieveSeeker getSeeker() {
			return seeker;
		}

	}

	/**
	 * Wrapper around a Lucene Directory that indicates whether Pensieve needs
	 * to create a new TM index.
	 */
	static class DirectoryWrapper {
		private final Directory luceneDir;
		private final boolean hasPensieveIndex;

		public DirectoryWrapper(Directory luceneDir, boolean hasPensieveIndex) {
			this.luceneDir = luceneDir;
			this.hasPensieveIndex = hasPensieveIndex;
		}
	}

}
