package com.ibm.ets.ita.ce.store.client.web;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.CESTORENAME_DEFAULT;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.hudson.helper.HudsonManager;

public class ServletStateManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	// Static variables
	private static ServletStateManager pm = null;
	private static HudsonManager hm = null;
	private static String defaultRootUrl = null;
	private static String defaultCurrentUrl = null;
	// defaultUrl: to avoid the need to manually specify the hostname in the
	// server
	// config we detect in from the request and store it in the
	// ServletStateManager

	// Local variables
	private ConcurrentHashMap<String, ModelBuilder> mbs = null;

	private ServletStateManager() {
		this.mbs = new ConcurrentHashMap<String, ModelBuilder>();
	}

	public static String getDefaultRootUrl() {
		return defaultRootUrl;
	}

	public static String getDefaultCurrentUrl() {
		return defaultCurrentUrl;
	}

	public static void retainDefaultUrl(HttpServletRequest pRequest) {
		if (defaultRootUrl == null) {
			String scheme = pRequest.getScheme();
			String serverName = pRequest.getServerName();
			int portName = pRequest.getServerPort();
			String path = pRequest.getContextPath();

			defaultRootUrl = scheme + "://" + serverName + ":" + portName;
			// TODO: Anonymise these

			if ((path != null) && (!path.isEmpty())) {
				defaultCurrentUrl = defaultRootUrl + path + "/";
			} else {
				defaultCurrentUrl = defaultRootUrl + "/";
			}

			defaultRootUrl += "/";
		}
	}

	private static String keyForCeStoreName(String pCeStoreName) {
		String result = null;

		if ((pCeStoreName != null) && (!pCeStoreName.trim().isEmpty())) {
			result = pCeStoreName.trim();
		} else {
			result = CESTORENAME_DEFAULT;
		}

		return result;
	}

	public static ServletStateManager getServletStateManager() {
		if (pm == null) {
			pm = new ServletStateManager();
		}

		return pm;
	}

	public static HudsonManager getHudsonManager() {
		if (hm == null) {
			hm = new HudsonManager();
		}

		return hm;
	}

	public TreeMap<String, ModelBuilder> getAllModelBuilders() {
		return new TreeMap<String, ModelBuilder>(this.mbs);
	}

	public ModelBuilder getModelBuilder(String pCeStoreName) {
		String csnKey = keyForCeStoreName(pCeStoreName);
		ModelBuilder result = this.mbs.get(csnKey);

		return result;
	}

	public ModelBuilder getDefaultModelBuilder(ActionContext pAc) {
		ModelBuilder result = getModelBuilder(CESTORENAME_DEFAULT);

		if (result == null) {
			result = createModelBuilder(pAc, CESTORENAME_DEFAULT);
		}

		return result;
	}

	public synchronized ModelBuilder createModelBuilder(ActionContext pAc, String pCeStoreName) {
		ModelBuilder newMb = ModelBuilder.createNew(pAc, pCeStoreName);
		this.mbs.put(newMb.getCeStoreName(), newMb);
		pAc.setModelBuilderAndCeStoreName(newMb);

		StoreActions.initialiseEnvironment(pAc);

		// Ensure that the CE Store is reset if it is empty.
		// ...thus enabling users to use the CE store without being forced to
		// issue
		// the command "perform reset store.".
		StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
		sa.resetStore("1", false); // TODO: Anonymise this

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
