package com.ibm.ets.ita.ce.store.client.web;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.writeToFile;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.formattedTimeForFilenames;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ActionResponse;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonProcessor;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebObject;

public class WebActionResponse extends ActionResponse {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = WebActionResponse.class.getName();
	private static final String PACKAGE_NAME = WebActionResponse.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final String FN_JSON_SUFFIX = ".json";
	private static final String FN_JSON_PREFIX = "JSON";

	private CeStoreJsonObject jsonPayload = null;
	private StringBuilder sbPayload = null;
	private CeStoreJsonProcessor structuredResult = null;

	private int httpErrorCode = -1;
	private String httpErrorMessage = null;

	public int getHttpErrorCode() {
		return this.httpErrorCode;
	}

	public void setHttpErrorCode(int pErrCode) {
		this.httpErrorCode = pErrCode;
	}

	public boolean hasHttpError() {
		return this.httpErrorCode != -1;
	}

	public String getHttpErrorMessage() {
		return this.httpErrorMessage;
	}

	public void setHttpErrorMessage(String pErrMsg) {
		this.httpErrorMessage = pErrMsg;
	}

	private void initialiseJsonPayload(ActionContext pAc, String pKey) {
		if (this.jsonPayload == null) {
			this.jsonPayload = new CeStoreJsonObject();
		}

		if (this.jsonPayload.get(pAc, pKey) != null) {
			reportWarning("Payload (JSON) has already been populated with data for key '" + pKey + "'", pAc);
		}
	}

	private void initialiseSbPayload(ActionContext pAc) {
		if (this.sbPayload != null) {
			reportWarning("Payload (plain) has already been initialised", pAc);
		}

		if (this.sbPayload == null) {
			this.sbPayload = new StringBuilder();
		}
	}

	public void setStructuredResult(CeStoreJsonProcessor pJson) {
		if (pJson != null) {
			this.structuredResult = pJson;
		}
	}

	@Override
	public void saveStructuredResult(ActionContext pAc) {
		addToPayload(pAc, KEY_QUERY_STRUCTURED, this.structuredResult);
	}

	public void setPayloadTo(CeStoreJsonObject pJsonObj) {
		this.jsonPayload = pJsonObj;
	}

	private void addToPayload(ActionContext pAc, String pKey, CeStoreJsonProcessor pJson) {
		initialiseJsonPayload(pAc, pKey);
		this.jsonPayload.put(pKey, pJson);
	}

	@Override
	public void setPlainTextPayload(ActionContext pAc, String pText) {
		pAc.getActionResponse().setIsPlainTextResponse(true);
		initialiseSbPayload(pAc);
		this.sbPayload.append(pText);
	}

	@Override
	public void plainTextResponse(PrintWriter pOut) {
		//This is the special response type which is plain text
		pOut.append(this.sbPayload.toString());
	}

	@Override
	public void convertAndRespond(ActionContext pAc, PrintWriter pOut, boolean pWrapResponse) {
		final String METHOD_NAME = "convertAndRespond";

		if (pWrapResponse) {
			populateStandardFields(pAc);
		}

		//Since this is a web action response the conversion is to json
		try {
			StringBuilder jsonSb = null;

			if (pWrapResponse) {
				jsonSb = this.jsonPayload.serializeToSb(pAc);
			} else {
				if (this.structuredResult != null) {
					jsonSb = this.structuredResult.serializeToSb(pAc);
				} else {
					jsonSb = CeWebObject.generateStandardAlertsFrom(this.alerts).serializeToSb(pAc);
				}
			}

			pOut.append(jsonSb);

			//Log to file if needed
			if (pAc.getCeConfig().logJsonToFiles()) {
				String jsonFilename = pAc.getCeConfig().getTempPath() + FN_JSON_PREFIX + "_" + this.getTxnName() + "_" + formattedTimeForFilenames() + FN_JSON_SUFFIX;
				writeToFile(pAc, jsonSb, jsonFilename);
			}
		} catch (IOException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}		
	}

	@Override
	protected void addMessagesToPayload(ActionContext pAc, String pKey, ArrayList<String> pMessages) {		
		addToPayload(pAc, pKey, CeWebObject.generateStandardMessagesFrom(pMessages));
	}

	@Override
	protected void addStatsToPayload(ActionContext pAc, String pKey, LinkedHashMap<String, String> pStats) {
		addToPayload(pAc, pKey, CeWebObject.generateStandardStatsFrom(pStats));
	}

	@Override
	protected void addAlertsToPayload(ActionContext pAc, String pKey, LinkedHashMap<String, ArrayList<String>> pAlerts) {
		addToPayload(pAc, pKey, CeWebObject.generateStandardAlertsFrom(pAlerts));
	}

}