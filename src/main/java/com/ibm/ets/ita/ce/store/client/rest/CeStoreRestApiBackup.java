package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;

public class CeStoreRestApiBackup extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public CeStoreRestApiBackup(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests: /backup[?timestamp=true]
	 */

	public boolean processRequest() {
		if (isGet()) {
			this.wc.getActionResponse().setIsGzipResponse(true);
		} else {
			reportUnsupportedMethodError();
		}
		return false;

	}

	protected boolean isJsonRequest() {
		return false;
	}

	protected boolean isTextRequest() {
		return false;
	}

}
