package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSpecial;

public class CeStoreRestApiSpecialConfig extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public CeStoreRestApiSpecialConfig(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	public boolean processRequest() {
		if (this.restParts.size() == 2) {
			processTwoElementConfigRequest();
		} else {
			reportUnhandledUrl();
		}

		return false;
	}

	private void processTwoElementConfigRequest() {
		//URL = special/config
		//Show store config
		handleShowStoreConfiguration();
	}

	private void handleShowStoreConfiguration() {
		if (isJsonRequest()) {
			jsonShowStoreConfiguration();
		} else if (isTextRequest()) {
			textShowStoreConfiguration();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonShowStoreConfiguration() {
		setStoreConfigAsStructuredResult();
	}

	private void textShowStoreConfiguration() {
		//TODO: Implement this
		reportNotYetImplementedError();
	}

	private void setStoreConfigAsStructuredResult() {
		getWebActionResponse().setStructuredResult(CeWebSpecial.generateStoreConfigListFrom(this.wc));
	}

}
