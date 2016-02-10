package com.ibm.ets.ita.ce.store.client.web;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreConfig;

class StoreConfigServlet extends StoreConfig {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";
	
	private static final String CLASS_NAME = StoreConfigServlet.class.getName();
	private static final String PACKAGE_NAME = StoreConfigServlet.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);
	private static final String DEFAULT_ROOT = "/opt/ibm/cestore/var/icnlpe/";

	private static final String JNDI_CONTEXT_TOMCAT = "java:/comp/env";
	private static final String JNDI_CONTEXT_LIBERTY = "cestore";

	@Override
	protected void initialise() {
		//Do nothing here... this will be called from the constructor but we
		//need an ActionContext instance in order to report any errors so the
		//method initialiseManually() is called instead, passing in the required
		//ActionContext instance
	}
	
	protected void initialiseManually(ActionContext pAc) {
		final String METHOD_NAME = "initialiseManually";
		
		InitialContext initialContext = null;
		Context envContext = null;

		try {
			initialContext = new InitialContext();
		} catch(NamingException e) {
			reportException(e, "On initialContext creation", pAc, logger, CLASS_NAME, METHOD_NAME);
		}

		if (initialContext != null) {
			try {
				envContext = (Context) initialContext.lookup(JNDI_CONTEXT_TOMCAT);
			} catch (NamingException e) {
				//The first naming context failed so try the liberty one instead
				try {
					envContext = (Context) initialContext.lookup(JNDI_CONTEXT_LIBERTY);
				} catch (NamingException e1) {
					//This is not an error.  Defaults will be used instead
				}
			}
			if (envContext != null) {
				initialiseFromJndiContext(envContext);
			} else {
				setRootFolder(DEFAULT_ROOT);
				setDefaultCeRootUrl(ServletStateManager.getDefaultRootUrl());
				setDefaultCeCurrentUrl(ServletStateManager.getDefaultCurrentUrl());
			}
		}

	}

	private void initialiseFromJndiContext(Context pContext) {
		//Fixed
		String rootFolder = contextLookup(pContext, GENKEY_ROOT, null);
		
		if ((rootFolder == null) || (rootFolder.isEmpty())) {
			setRootFolder(DEFAULT_ROOT);
		} else {
			setRootFolder(rootFolder);
		}

		String serverName = contextLookup(pContext, ECKEY_DEFCESVR, "");
		if ((serverName == null) || (serverName.isEmpty())) {
			setDefaultCeRootUrl(ServletStateManager.getDefaultRootUrl());
			setDefaultCeCurrentUrl(ServletStateManager.getDefaultCurrentUrl());
		} else {
			setDefaultCeRootUrl(serverName);
			setDefaultCeCurrentUrl(ServletStateManager.getDefaultCurrentUrl());	//TODO: Is this right?
		}

		String logFile = contextLookup(pContext, GENKEY_LOGFILE, null); 
		setLogfile(logFile);

		//Modifiable
		setDebug(contextLookup(pContext, ECKEY_DEBUG, new Boolean(isDebug())).booleanValue());
		setLoggingCeToFiles(contextLookup(pContext, ECKEY_LOGCE, new Boolean(isLoggingCeToFiles())).booleanValue());
		setLogQueries(contextLookup(pContext, ECKEY_LOGQUERIES, new Boolean(isLoggingQueries())).booleanValue());
		setAutogenerateFolders(contextLookup(pContext, ECKEY_GENFOLDERS, new Boolean(isAutogeneratingFolders())).booleanValue());
		setMaxSentences(contextLookup(pContext, ECKEY_MAXSENS, new Integer(getMaxSentences())).intValue());
		setLogJsonToFiles(contextLookup(pContext, ECKEY_LOGJSON, new Boolean(logJsonToFiles())).booleanValue());
		setCatchAgentErrors(contextLookup(pContext, ECKEY_CATCHERRS, new Boolean(isCatchingAgentErrors())).booleanValue());
		setUidClassName(contextLookup(pContext, ECKEY_UIDCLASSNAME, getUidClassName()));
		setUidFilename(contextLookup(pContext, ECKEY_UIDFILENAME, getUidFilename()));
		setUidValues(contextLookup(pContext, ECKEY_UIDVALUES, getUidValues()));
		setIsAllowingUidPrefixes(contextLookup(pContext, ECKEY_UIDPREFIXES, new Boolean(isAllowingUidPrefixes())).booleanValue());
		setUidBatchSize(contextLookup(pContext, ECKEY_UIDBATCH, new Long(getUidBatchSize())).longValue());
	}

	@SuppressWarnings("unchecked")
	private static <T> T contextLookup(Context pContext, String pKey, T pDefaultValue){
		try {
			return (T)pContext.lookup(pKey);
		} catch (NamingException ex) {
			return pDefaultValue;
		}
	}

}