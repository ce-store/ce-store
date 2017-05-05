package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;

public class CeStoreRestApiRestore extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CLASS_NAME = CeStoreRestApi.class.getName();
	private static final String PACKAGE_NAME = CeStoreRestApi.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	public CeStoreRestApiRestore(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests: /restore
	 */

	public boolean processRequest() {
		final String METHOD_NAME = "processRequest";

		if (isPut()) {
			try {
				//reportInfo("Received model to restore",this.wc);
				InputStream is = getBinaryFromRequest();
				GZIPInputStream gz = new GZIPInputStream(is);
				ObjectInputStream oin = new ObjectInputStream(gz) ;
				ModelBuilder tgtMb = (ModelBuilder) oin.readObject();
				tgtMb.setCeStoreName(this.wc.getModelBuilder().getCeStoreName());
				ServletStateManager.getServletStateManager().getModelBuilderMap().put(tgtMb.getCeStoreName(), tgtMb);
				oin.close();
				gz.close();
			} catch (IOException e) {
				reportException(e, "Unhandled IOException", this.wc, logger, CLASS_NAME, METHOD_NAME);
			} catch (ClassNotFoundException e) {
				reportException(e, "Unhandled ClassNotFoundException", this.wc, logger, CLASS_NAME, METHOD_NAME);
			}
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

}
