package com.ibm.ets.ita.ce.store.client.rest;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_STORE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.CESTORENAME_DEFAULT;

public class CeStoreRestApiBackup extends CeStoreRestApi {

	public CeStoreRestApiBackup(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
		// TODO Auto-generated constructor stub
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
