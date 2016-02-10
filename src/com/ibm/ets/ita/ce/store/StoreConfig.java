package com.ibm.ets.ita.ce.store;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isDebugOn;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.setDebugOff;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.setDebugOn;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class StoreConfig {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = StoreConfig.class.getName(); 
	private static final String PACKAGE_NAME = StoreConfig.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

//	private static final String REVISION = "$Revision$";
//	public  static final String VERSION = "1.2." + REVISION.substring(11, REVISION.length() - 2);
	public  static final String VERSION = "1.3.0001";

	// Environment Context (Modifiable) Keys
	protected static final String ECKEY_DEBUG = "debug";
	protected static final String ECKEY_LOGCE = "logCeToFiles";
	protected static final String ECKEY_LOGQUERIES = "logQueries";
	protected static final String ECKEY_GENFOLDERS = "autogenerateFolders";
	protected static final String ECKEY_MAXSENS = "maxSentences";
	protected static final String ECKEY_LOGJSON = "logJsonToFiles";
	protected static final String ECKEY_CATCHERRS = "catchAgentErrors";
	protected static final String ECKEY_UIDCLASSNAME = "uidClassName";
	protected static final String ECKEY_UIDFILENAME = "uidFilename";
	protected static final String ECKEY_UIDVALUES = "uidValues";
	protected static final String ECKEY_UIDPREFIXES = "uidAllowPrefixes";
	protected static final String ECKEY_UIDBATCH = "uidBatchSize";
	private static final String ECKEY_CACHEPROPS = "cacheInstanceProperties";
	private static final String ECKEY_CACHECE = "cacheCeText";
	private static final String ECKEY_CASESEN = "caseSensitive";
	protected static final String ECKEY_DEFCESVR = "defaultCeServer";

	// General (Fixed) Keys
	protected static final String GENKEY_ROOT = "rootFolder";
	protected static final String GENKEY_LOGFILE = "logfile";
	private   static final String GENKEY_LOGPATH = "logPath";
	private   static final String GENKEY_TEMPPATH = "tempPath";
	private   static final String GENKEY_GENPATH = "genPath";
	private   static final String GENKEY_PERSISTPATH = "persistPath";
	private   static final String GENKEY_VERSION = "version";

	// Property name values
	private static final String PROP_DEBUG = "debug";
	private static final String PROP_LOGCETOFILE = "logCeToFiles";
	private static final String PROP_LOGQUERIES = "logQueries";
	private static final String PROP_GENFOLDERS = "autogenerateFolders";
	private static final String PROP_MAXSENS = "maxSentences";
	private static final String PROP_LOGJSON = "logJsonToFiles";
	private static final String PROP_CATCHERRS = "catchAgentErrors";
	private static final String PROP_UIDCLASSNAME = "uidClassName";
	private static final String PROP_UIDFILENAME = "uidFilename";
	private static final String PROP_UIDVALUES = "uidValues";
	private static final String PROP_UIDPREFIXES = "uidAllowPrefixes";
	private static final String PROP_UIDBATCH = "uidBatchSize";
	private static final String PROP_CACHEPROPS = "cacheInstanceProperties";
	private static final String PROP_CACHECE = "cacheCeText";
	private static final String PROP_CASESEN = "caseSensitive";
	private static final String PROP_DEFCESVR = "defaultCeServer";

	// Performance related
	private boolean cacheInstanceProperties = true;
	private boolean cacheCeText = true;
	private boolean saveCeSentences = true;
	private boolean caseSensitive = false;
	
	// UID related
	private static final String DEFAULT_UIDCLASSNAME = "com.ibm.ets.ita.ce.store.UidManagerDefault";
	
	private boolean uidAllowPrefixes = true;
	private long uidBatchSize = -1;
	private String uidClassName = DEFAULT_UIDCLASSNAME;
	private String uidFilename = "";
	private String uidValues = "";
	
	// Debug and logging related
	private boolean logCeToFiles = false;
	private boolean logJsonToFiles = false;
	private boolean logQueries = false;
	
	// Paths
	private static final String SUB_FOLDER_LOGGING = "logging/";
	private static final String SUB_FOLDER_TEMP = "temp/";
	private static final String SUB_FOLDER_GENERATED = "generated/";
	private static final String SUB_FOLDER_PERSIST = "persist/";
	public  static final String FILENAME_QUERYLOG_CE = "queries_ce.log";

	private String rootFolder = null;
	private String logfile = null;
	private String logPath = null;
	private String tempPath = null;
	private String genPath = null;
	private String persistPath = null;

	// Other
	private boolean autogenerateFolders = false;
	private int maxSentences = -1;
	private boolean catchAgentErrors = false;	
	private String defaultCeRootUrl = null;
	private String defaultCeCurrentUrl = null;
	
	protected abstract void initialise();	
	
	protected StoreConfig() {
		final String METHOD_NAME = "StoreConfig";
		initialise();
		if (logger.isLoggable(Level.FINER)) {
			logger.logp(Level.FINER, CLASS_NAME, METHOD_NAME, getAllGeneralProperties().toString());
			logger.logp(Level.FINER, CLASS_NAME, METHOD_NAME, getAllEnvironmentProperties().toString());
		}
	}
	
	/*
	 * Return the state of the debug mode.
	 * 
	 * See ReportingUtilities.isDebugOn() for details.
	 */
	@SuppressWarnings("static-method")
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
			String logPathAndFile = null;
			if (this.logPath != null && this.logfile != null) {
				logPathAndFile = this.logPath + this.logfile;
			}
			success = setDebugOn(logPathAndFile);
		} else {
			success = setDebugOff();
		}
		return success;
	}

	public void setDebug(boolean pDebug, ActionContext pAc) {
		if (setDebug(pDebug)) {
			pAc.getActionResponse().addLineToMessage("debug updated with value '" + pDebug + "'");
		} else {
			pAc.getActionResponse().addLineToMessage("debug could not be updated with value '" + pDebug + "' and remains set to " + Boolean.toString(this.isDebug()));					
		}
	}
	
	public String getRootFolder() {
		return this.rootFolder;
	}

	public void setRootFolder(String pRootFolder) {
		if (pRootFolder==null || pRootFolder.isEmpty()) {
			this.rootFolder = null;
			this.logPath = null;
			this.tempPath = null;
			this.genPath = null;
			this.persistPath = null;
		} else {
			// using "/" and concatenation works on all required platforms, all these
			// paths have a trailing "/" or File.separator. 
			if (!pRootFolder.endsWith("/") && !pRootFolder.endsWith(File.separator)) {
				this.rootFolder = pRootFolder + "/";
			} else {
				this.rootFolder = pRootFolder;
			}
			this.logPath = this.rootFolder + SUB_FOLDER_LOGGING;
			this.tempPath = this.rootFolder + SUB_FOLDER_TEMP;
			this.genPath = this.rootFolder + SUB_FOLDER_GENERATED;
			this.persistPath = this.rootFolder + SUB_FOLDER_PERSIST;
		}
	}

	public String getLogfile() {
		return this.logfile;
	}

	public void setLogfile(String pLogfile) {
		this.logfile = pLogfile;
	}

	public boolean isLoggingCeToFiles() {
		return this.logCeToFiles;
	}

	protected void setLoggingCeToFiles(boolean pLogCeToFiles) {
		this.logCeToFiles = pLogCeToFiles;
	}

	protected void setLogQueries(boolean pLogQueries) {
		this.logQueries = pLogQueries;
	}

	public boolean isLoggingQueries() {
		return this.logQueries;
	}

	public boolean isAutogeneratingFolders() {
		return this.autogenerateFolders;
	}

	protected void setAutogenerateFolders(boolean pAutogenFolders) {
		this.autogenerateFolders = pAutogenFolders;
	}

	public int getMaxSentences() {
		return this.maxSentences;
	}

	protected void setMaxSentences(int pMaxSentences) {
		this.maxSentences = pMaxSentences;
	}

	public boolean isAllowingUidPrefixes() {
		return this.uidAllowPrefixes;
	}

	protected void setIsAllowingUidPrefixes(boolean pIsAllowingPrefixes) {
		this.uidAllowPrefixes = pIsAllowingPrefixes;
	}

	public long getUidBatchSize() {
		return this.uidBatchSize;
	}

	protected void setUidBatchSize(long pUidBatchSize) {
		this.uidBatchSize = pUidBatchSize;
	}

	public String getUidClassName() {
		return this.uidClassName;
	}

	protected void setUidClassName(String pUidClassName) {
		this.uidClassName = pUidClassName;
	}

	public String getUidFilename() {
		return this.uidFilename;
	}

	protected void setUidFilename(String pUidFilename) {
		this.uidFilename = pUidFilename;
	}

	public String getUidValues() {
		return this.uidValues;
	}

	protected void setUidValues(String pUidValues) {
		this.uidValues = pUidValues;
	}

	public String getDefaultCeRootUrl() {
		return this.defaultCeRootUrl;
	}

	public void setDefaultCeRootUrl(String pDefCeRootUrl) {
		if (pDefCeRootUrl != null) {
			this.defaultCeRootUrl = pDefCeRootUrl;
			
			//Ensure that the specified value always ends with a /
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
			
			//Ensure that the specified value always ends with a /
			if (!this.defaultCeCurrentUrl.endsWith("/")) {
				this.defaultCeCurrentUrl += "/";
			}
		}
	}

	public void useDefaultCeServerIfNeeded(String pDefCeRootUrl, String pDefCeCurrentUrl, ActionContext pAc) {
		//Only use the specified default CE Server if there is not one specified already
		if (this.defaultCeRootUrl == null) {
			this.setDefaultCeRootUrl(pDefCeRootUrl);

			if (pAc.getCeConfig().isDebug()) {
				reportDebug("Using this (default) root url for REST requests: " + this.defaultCeRootUrl, pAc);
			}
		}

		//Only use the specified default CE Server if there is not one specified already
		if (this.defaultCeCurrentUrl == null) {
			this.setDefaultCeCurrentUrl(pDefCeCurrentUrl);

			if (pAc.getCeConfig().isDebug()) {
				reportDebug("Using this (default) current url for REST requests: " + this.defaultCeCurrentUrl, pAc);
			}
		}
	}
	
	public boolean logJsonToFiles() {
		return this.logJsonToFiles;
	}

	protected void setLogJsonToFiles(boolean pLogJsonToFiles) {
		this.logJsonToFiles = pLogJsonToFiles;
	}

	public boolean isCatchingAgentErrors() {
		return this.catchAgentErrors;
	}

	protected void setCatchAgentErrors(boolean pCatchAgentErrors) {
		this.catchAgentErrors = pCatchAgentErrors;
	}

	public String getLogPath() {
		return this.logPath;
	}

	public String getTempPath() {
		return this.tempPath;
	}

	public String getGenPath() {
		return this.genPath;
	}

	public String getPersistPath() {
		return this.persistPath;
	}

	public boolean cacheInstanceProperties() {
		return this.cacheInstanceProperties;
	}
	
	public boolean cacheCeText() {
		return this.cacheCeText;
	}
	
	public boolean isSavingCeSentences() {
		return this.saveCeSentences;
	}

	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}

	protected void setPropertyByName(ActionContext pAc, String pPropName, String pPropVal) {
		if (pPropName != null) {
			if (pPropName.equals(PROP_DEBUG)) {
				setDebug(new Boolean(pPropVal).booleanValue(), pAc);
			} else if (pPropName.equals(PROP_LOGCETOFILE)) {
				this.logCeToFiles = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage("logCeToFiles updated with value '" + pPropVal + "' to become " + Boolean.toString(this.logCeToFiles));
			} else if (pPropName.equals(PROP_LOGQUERIES)) {
				this.logQueries = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage("logQueries updated with value '" + pPropVal + "' to become " + Boolean.toString(this.logQueries));
			} else if (pPropName.equals(PROP_GENFOLDERS)) {
				this.autogenerateFolders = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage("autogenerateFolders updated with value '" + pPropVal + "' to become " + Boolean.toString(this.autogenerateFolders));
			} else if (pPropName.equals(PROP_MAXSENS)) {
				this.maxSentences = new Integer(pPropVal).intValue();
				pAc.getActionResponse().addLineToMessage("maxSentences updated with value '" + pPropVal + "' to become " + Integer.toString(this.maxSentences));
			} else if (pPropName.equals(PROP_LOGJSON)) {
				this.logJsonToFiles = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage("logJsonToFiles updated with value '" + pPropVal + "' to become " + Boolean.toString(this.logJsonToFiles));
			} else if (pPropName.equals(PROP_CATCHERRS)) {
				this.catchAgentErrors = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage("catchAgentErrors updated with value '" + pPropVal + "' to become " + Boolean.toString(this.catchAgentErrors));
			} else if (pPropName.equals(PROP_UIDCLASSNAME)) {
				this.uidClassName = pPropVal;
				pAc.getActionResponse().addLineToMessage("uidClassName updated with value '" + pPropVal + "' to become " + this.uidClassName);
			} else if (pPropName.equals(PROP_UIDFILENAME)) {
				this.uidFilename = pPropVal;
				pAc.getActionResponse().addLineToMessage("uidFilename updated with value '" + pPropVal + "' to become " + this.uidFilename);
			} else if (pPropName.equals(PROP_UIDVALUES)) {
				this.uidValues = pPropVal;
				pAc.getActionResponse().addLineToMessage("uidValues updated with value '" + pPropVal + "' to become " + this.uidValues);
			} else if (pPropName.equals(PROP_UIDPREFIXES)) {
				this.uidAllowPrefixes = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage("uidAllowPrefixes updated with value '" + pPropVal + "' to become " + Boolean.toString(this.uidAllowPrefixes));
			} else if (pPropName.equals(PROP_UIDBATCH)) {
				this.uidBatchSize = new Integer(pPropVal).intValue();
				pAc.getActionResponse().addLineToMessage("uidBatchSize updated with value '" + pPropVal + "' to become " + Long.toString(this.uidBatchSize));
			} else if (pPropName.equals(PROP_CACHEPROPS)) {
				this.cacheInstanceProperties = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage("cacheInstanceProperties updated with value '" + pPropVal + "' to become " + Boolean.toString(this.cacheInstanceProperties));
			} else if (pPropName.equals(PROP_CACHECE)) {
				this.cacheCeText = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage("cacheCeText updated with value '" + pPropVal + "' to become " + Boolean.toString(this.cacheCeText));
			} else if (pPropName.equals(PROP_CASESEN)) {
				this.caseSensitive = new Boolean(pPropVal).booleanValue();
				pAc.getActionResponse().addLineToMessage(pPropName + " updated with value '" + pPropVal + "' to become " + Boolean.toString(this.caseSensitive));
			} else if (pPropName.equals(PROP_DEFCESVR)) {
				this.defaultCeRootUrl = pPropVal;
				pAc.getActionResponse().addLineToMessage("defaultCeServer updated with value '" + pPropVal + "' to become " + this.defaultCeRootUrl);
			} else {
				reportError("Cannot update property value (" + pPropVal + ") as the property name '" + pPropName + "' is unknown", pAc);
			}
		} else {
			reportError("Cannot update property value (" + pPropVal + ") as no property name is specified", pAc);
		}
	}

	public void setCaseInsensitive() {
		this.caseSensitive = false;
	}

	public Object getPropertyByName(ActionContext pAc, String pPropName) {
		Object result = null;

		if (pPropName != null) {
			if (pPropName.equals(PROP_DEBUG)) {
				result = new Boolean(this.isDebug());
			} else if (pPropName.equals(PROP_LOGCETOFILE)) {
				result = new Boolean(this.logCeToFiles);
			} else if (pPropName.equals(PROP_LOGQUERIES)) {
				result = new Boolean(this.logQueries);
			} else if (pPropName.equals(PROP_GENFOLDERS)) {
				result = new Boolean(this.autogenerateFolders);
			} else if (pPropName.equals(PROP_MAXSENS)) {
				result = new Integer(this.maxSentences);
			} else if (pPropName.equals(PROP_LOGJSON)) {
				result = new Boolean(this.logJsonToFiles);
			} else if (pPropName.equals(PROP_CATCHERRS)) {
				result = new Boolean(this.catchAgentErrors);
			} else if (pPropName.equals(PROP_UIDCLASSNAME)) {
				result = this.uidClassName;
			} else if (pPropName.equals(PROP_UIDFILENAME)) {
				result = this.uidFilename;
			} else if (pPropName.equals(PROP_UIDVALUES)) {
				result = this.uidValues;
			} else if (pPropName.equals(PROP_UIDPREFIXES)) {
				result = new Boolean(this.uidAllowPrefixes);
			} else if (pPropName.equals(PROP_UIDBATCH)) {
				result = new Long(this.uidBatchSize);
			} else if (pPropName.equals(PROP_CACHEPROPS)) {
				result = new Boolean(this.cacheInstanceProperties);
			} else if (pPropName.equals(PROP_CACHECE)) {
				result = new Boolean(this.cacheCeText);
			} else if (pPropName.equals(PROP_CASESEN)) {
				result = new Boolean(this.caseSensitive);
			} else if (pPropName.equals(PROP_DEFCESVR)) {
				result = this.defaultCeRootUrl;
			} else {
				reportError("Cannot get property value (" + pPropName + ") as it is unknown", pAc);
			}
		} else {
			reportError("Cannot get property value as no property name is specified", pAc);
		}

		return result;
	}

	public LinkedHashMap<String, String> getAllGeneralProperties() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		
		result.put(GENKEY_VERSION, VERSION);
		result.put(GENKEY_ROOT, this.rootFolder);
		result.put(GENKEY_LOGPATH, this.logPath);
		result.put(GENKEY_TEMPPATH, this.tempPath);
		result.put(GENKEY_GENPATH, this.genPath);
		result.put(GENKEY_PERSISTPATH, this.persistPath);
		result.put(GENKEY_LOGFILE, getLogfile());

		return result;
	}
	
	public LinkedHashMap<String, String> getAllEnvironmentProperties() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

		result.put(ECKEY_DEBUG, Boolean.toString(this.isDebug()));
		result.put(ECKEY_LOGCE, Boolean.toString(this.logCeToFiles));
		result.put(ECKEY_UIDCLASSNAME, this.uidClassName);
		result.put(ECKEY_UIDFILENAME, this.uidFilename);
		result.put(ECKEY_UIDVALUES, this.uidValues);
		result.put(ECKEY_UIDPREFIXES, Boolean.toString(this.uidAllowPrefixes));
		result.put(ECKEY_UIDBATCH, Long.toString(this.uidBatchSize));
		result.put(ECKEY_CACHEPROPS, Boolean.toString(this.cacheInstanceProperties));
		result.put(ECKEY_CACHECE, Boolean.toString(this.cacheCeText));
		result.put(ECKEY_CASESEN, Boolean.toString(this.caseSensitive));
		result.put(ECKEY_DEFCESVR, this.defaultCeRootUrl);

		return result;
	}
	
}