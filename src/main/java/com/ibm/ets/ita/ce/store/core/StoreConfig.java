package com.ibm.ets.ita.ce.store.core;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_ROOT;
import static com.ibm.ets.ita.ce.store.names.MiscNames.SUB_FOLDER_GENERATED;
import static com.ibm.ets.ita.ce.store.names.MiscNames.URL_MODELDIR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_ARR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_CACHECE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_CASESEN;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_DEBUG;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_DEFCECURRENT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_DEFCEROOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_GENPATH;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_MDU;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_ROOTFOLDER;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_SAVESENS;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isDebugOn;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.setDebugOff;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.setDebugOn;

import java.io.File;
import java.util.LinkedHashMap;

public class StoreConfig {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private boolean cacheCeText = true;
	// Whether the CE text for a sentence is cached or computed from parts

	private boolean saveCeSentences = true;
	// Whether CE sentences are saved or discarded

	private boolean caseSensitive = false;
	// Whether case sensitivity for entity ids is enabled

	private boolean autoRunRules = false;
	// Whether to automatically run all rules on CE ingestion

	private String rootFolder = null;
	// The local folder where files will be written (if needed)

	private String genPath = null;
	//The sub-folder within rootFolder where generated CE will be written

	private String defaultCeRootUrl = null;
	//The default 'root' CE url (used when '.' is specified in a relative CE path)
	
	private String defaultCeCurrentUrl = null;
	//The default 'current' CE url (used when '/' is specified in a relative CE path)

	private String modelDirectoryUrl = URL_MODELDIR;

	public StoreConfig() {
		setRootFolder(DEFAULT_ROOT);
		setGenPath(SUB_FOLDER_GENERATED);
	}

	/*
	 * Return the state of the debug mode.
	 * 
	 * See ReportingUtilities.isDebugOn() for details.
	 */
	public boolean isDebug() {
		return isDebugOn();
	}

	/*
	 * Set debug mode on or off, and return whether the request was successful
	 * or not.
	 * 
	 * See ReportingUtilities.setDebugOn() and setDebugOff() for details.
	 */
	public boolean setDebug(boolean pDebug) {
		boolean success;

		if (pDebug) {
			success = setDebugOn(null);
		} else {
			success = setDebugOff();
		}

		return success;
	}

	public String getRootFolder() {
		return this.rootFolder;
	}

	public void setRootFolder(String pRootFolder) {
		if (pRootFolder == null || pRootFolder.isEmpty()) {
			this.rootFolder = null;
		} else {
			// using '/' and concatenation works on all required platforms, all
			// these paths have a trailing '/' or File.separator.
			if (!pRootFolder.endsWith("/") && !pRootFolder.endsWith(File.separator)) {
				this.rootFolder = pRootFolder + "/";
			} else {
				this.rootFolder = pRootFolder;
			}
		}
	}

	public String getModelDirectoryUrl() {
		return this.modelDirectoryUrl;
	}

	public void setModelDirectoryUrl(String pVal) {
		this.modelDirectoryUrl = pVal;
	}

	public boolean getAutoRunRules() {
		return this.autoRunRules;
	}

	public void setAutoRunRules(boolean pVal) {
		this.autoRunRules = pVal;
	}

	public String getDefaultCeRootUrl() {
		return this.defaultCeRootUrl;
	}

	public void setDefaultCeRootUrl(String pDefCeRootUrl) {
		if (pDefCeRootUrl != null) {
			this.defaultCeRootUrl = pDefCeRootUrl;

			// Ensure that the specified value always ends with a /
			if (!this.defaultCeRootUrl.endsWith("/")) {
				this.defaultCeRootUrl += "/";
			}
		}
	}

	public String getDefaultCeCurrentUrl() {
		return this.defaultCeCurrentUrl;
	}

	public void setDefaultCeCurrentUrl(String pDefCeCurrentUrl) {
		if (pDefCeCurrentUrl != null) {
			this.defaultCeCurrentUrl = pDefCeCurrentUrl;

			// Ensure that the specified value always ends with a /
			if (!this.defaultCeCurrentUrl.endsWith("/")) {
				this.defaultCeCurrentUrl += "/";
			}
		}
	}

	public void useDefaultCeServerIfNeeded(String pDefCeRootUrl, String pDefCeCurrentUrl, ActionContext pAc) {
		// Only use the specified default CE Server if there is not one
		// specified already
		if (this.defaultCeRootUrl == null) {
			this.setDefaultCeRootUrl(pDefCeRootUrl);

			if (pAc.getCeConfig().isDebug()) {
				reportDebug("Using this (default) root url for REST requests: " + this.defaultCeRootUrl, pAc);
			}
		}

		// Only use the specified default CE Server if there is not one
		// specified already
		if (this.defaultCeCurrentUrl == null) {
			this.setDefaultCeCurrentUrl(pDefCeCurrentUrl);

			if (pAc.getCeConfig().isDebug()) {
				reportDebug("Using this (default) current url for REST requests: " + this.defaultCeCurrentUrl, pAc);
			}
		}
	}

	public String getRawPath() {
		return this.genPath;
	}

	public String getGenPath() {
		return this.rootFolder + this.genPath;
	}

	public void setGenPath(String pVal) {
		this.genPath = pVal;
	}

	public boolean cacheCeText() {
		return this.cacheCeText;
	}

	public void setCacheCeText(boolean pVal) {
		this.cacheCeText = pVal;
	}

	public boolean isSavingCeSentences() {
		return this.saveCeSentences;
	}

	public void setSaveCeSentences(boolean pVal) {
		this.saveCeSentences = pVal;
	}

	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}

	public void setCaseSensitive(boolean pVal) {
		this.caseSensitive = pVal;
	}

	public LinkedHashMap<String, String> getAllProperties() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

		result.put(VARNAME_DEBUG, new Boolean(this.isDebug()).toString());
		result.put(VARNAME_CACHECE, new Boolean(this.cacheCeText).toString());
		result.put(VARNAME_CASESEN, new Boolean(this.caseSensitive).toString());
		result.put(VARNAME_SAVESENS, new Boolean(this.saveCeSentences).toString());
		result.put(VARNAME_ARR, new Boolean(this.autoRunRules).toString());
		result.put(VARNAME_ROOTFOLDER, this.rootFolder);
		result.put(VARNAME_GENPATH, this.genPath);
		result.put(VARNAME_DEFCEROOT, this.defaultCeRootUrl);
		result.put(VARNAME_DEFCECURRENT, this.defaultCeCurrentUrl);
		result.put(VARNAME_MDU, this.modelDirectoryUrl);

		return result;
	}
}
