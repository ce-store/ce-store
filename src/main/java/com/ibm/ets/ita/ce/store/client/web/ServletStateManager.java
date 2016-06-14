package com.ibm.ets.ita.ce.store.client.web;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.StoreConfig;

public class ServletStateManager {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	// Static variables
	private static ServletStateManager pm = null;
	private static String defaultRootUrl = null;
	private static String defaultCurrentUrl = null;
	//defaultUrl: to avoid the need to manually specify the hostname in the server
	//config we detect in from the request and store it in the ServletStateManager

	// Local variables
	private StoreConfig cc = null;
	private ConcurrentHashMap<String, ModelBuilder> mbs = new ConcurrentHashMap<String, ModelBuilder>();

	//TODO: StoreConfig should probably apply to each CE Store rather than the CE Server
	
	private ServletStateManager(ActionContext pAc) {
		this.cc = pAc.getCeConfig();
	}

	protected static WebActionContext createWebActionContext(HttpServletRequest pRequest, String pUserName) {
		WebActionContext wc = null;
		WebActionResponse wr = new WebActionResponse();

		if (pm == null) {
			// The ServletStateManager does not exist so must be created.
			// Creating the WebActionContext with the default constructor
			// will create all required resources since they are not
			// available to be reused.
			StoreConfigServlet newCc = new StoreConfigServlet();
			wc = new WebActionContext(newCc, pUserName, wr);
			newCc.initialiseManually(wc);
			StoreActions.initialiseEnvironment(wc);
			pm = new ServletStateManager(wc);

			reportDebug("New ServletStateManager successfully created", wc);
		} else {
			// The ServletStateManager exists from a previous request so create
			// a new WebActionContext but reuse the various pre-existing resources
			// from the ServletStateManager

			wc = new WebActionContext(pm.getCeConfig(), pUserName, wr);

			reportDebug("Existing ServletStateManager reused", wc);
		}

		retainDefaultUrl(pRequest, wc);

		return wc;
	}
	
	protected static String getDefaultRootUrl() {
		return defaultRootUrl;
	}

	protected static String getDefaultCurrentUrl() {
		return defaultCurrentUrl;
	}

	private static void retainDefaultUrl(HttpServletRequest pRequest, ActionContext pAc) {
		if (defaultRootUrl == null) {
			String scheme = pRequest.getScheme();
			String serverName = pRequest.getServerName();
			int portName = pRequest.getServerPort();
			String path = pRequest.getContextPath();

			defaultRootUrl = scheme + "://" + serverName + ":" + portName;

			if ((path != null) && (!path.isEmpty())) {
				defaultCurrentUrl = defaultRootUrl + path + "/";
			} else {
				defaultCurrentUrl = defaultRootUrl + "/";
			}

			defaultRootUrl += "/";
			
			//The action context has been created already and may need to have the
			//default CE server updated
			pAc.getCeConfig().useDefaultCeServerIfNeeded(defaultRootUrl, defaultCurrentUrl, pAc);
		}
	}

	private static String keyForCeStoreName(String pCeStoreName) {
		String result = ModelBuilder.CESTORENAME_DEFAULT;
		
		if ((pCeStoreName != null) && (!pCeStoreName.trim().isEmpty())) {
			result = pCeStoreName.trim();
		}
		
		return result;
	}

	public static ServletStateManager getServletStateManager() {
		return pm;
	}

	private StoreConfig getCeConfig() {
		return this.cc;
	}

	public TreeMap<String, ModelBuilder> getAllModelBuilders() {
		return new TreeMap<String, ModelBuilder>(this.mbs);
	}

	public ModelBuilder getModelBuilder(String pCeStoreName) {
		String csnKey = keyForCeStoreName(pCeStoreName);
		ModelBuilder result = this.mbs.get(csnKey);

		return result;
	}

	public synchronized ModelBuilder createModelBuilder(ActionContext pAc, String pCeStoreName) {
		ModelBuilder newMb = ModelBuilder.createNew(pAc, pCeStoreName);
		this.mbs.put(newMb.getCeStoreName(), newMb);
		pAc.setModelBuilderAndCeStoreName(newMb);

		//Added by DSB 20/06/2014
		//Ensure that the CE Store is reset if it is empty.
		//...thus enabling users to use the CE store without being forced to issue
		//the command "perform reset store.".
		StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
		sa.resetStore("1");

		return newMb;
	}

	public void deleteModelBuilder(ActionContext pAc, ModelBuilder pMb) {
		String storeName = pMb.getCeStoreName();

		if (this.mbs.containsKey(storeName)) {
			this.mbs.remove(storeName);
		} else {
			reportError("Could not find CE Store named '" + storeName + "'.  Nothing has been deleted", pAc);
		}
	}

}