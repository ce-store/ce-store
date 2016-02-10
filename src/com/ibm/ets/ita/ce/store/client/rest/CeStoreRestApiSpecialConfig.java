package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportInfo;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSpecial;

public class CeStoreRestApiSpecialConfig extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";


	private static final String PARM_VALUE = "value";

	public CeStoreRestApiSpecialConfig(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	public boolean processRequest() {
		if (this.restParts.size() == 2) {
			processTwoElementConfigRequest();
		} else if (this.restParts.size() == 3) {
			processThreeElementConfigRequest();
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

	private void processThreeElementConfigRequest() {
		String keyName = this.restParts.get(2);
//		String keyName = StaticFunctionsGeneral.decodeFromUrl(this.wc, rawKeyName);

		if (isGet()) {
			//URL = special/config/{key}
			//Get config value
			handleShowStoreConfigValue(keyName);
		} else if (isPut()) {
			//URL = special/config/{key}
			//Update config value
			handleUpdateStoreConfigValue(keyName);
		} else {
			reportUnsupportedMethodError();
		}
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

	private void handleShowStoreConfigValue(String pKeyName) {
		if (isJsonRequest()) {
			jsonShowStoreConfigValue(pKeyName);
		} else if (isTextRequest()) {
			textShowStoreConfigValue(pKeyName);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonShowStoreConfigValue(String pPropName) {
		Object thisVal = this.wc.getCeConfig().getPropertyByName(this.wc, pPropName);

		setSingleValueAsStructuredResult(thisVal);
	}

	private void textShowStoreConfigValue(String pKeyName) {
		//TODO: Implement this
		reportNotYetImplementedError("show store config value for '" + pKeyName + "'");
	}

	private void handleUpdateStoreConfigValue(String pKeyName) {
		String newVal = getUrlParameterValueNamed(PARM_VALUE);

		if ((newVal != null) && (!newVal.isEmpty())) {
			if (isJsonRequest()) {
				jsonShowUpdateConfigValue(pKeyName, newVal);
			} else if (isTextRequest()) {
				textShowUpdateConfigValue(pKeyName, newVal);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportMissingUrlParameterError(PARM_VALUE);
		}
	}

	private void jsonShowUpdateConfigValue(String pKeyName, String pValue) {
		actionUpdateConfigValue(pKeyName, pValue);
	}

	private void textShowUpdateConfigValue(String pKeyName, String pValue) {
		actionUpdateConfigValue(pKeyName, pValue);
	}

	private void actionUpdateConfigValue(String pKeyName, String pValue) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		sa.updateConfig(pKeyName, pValue);
		reportInfo("Config property '" + pKeyName + "' was set to '" + pValue + "'", this.wc);
	}

	private void setStoreConfigAsStructuredResult() {
		getWebActionResponse().setStructuredResult(CeWebSpecial.generateStoreConfigListFrom(this.wc));
	}

}