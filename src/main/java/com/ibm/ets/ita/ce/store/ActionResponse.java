package com.ibm.ets.ita.ce.store;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public abstract class ActionResponse {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	// The structure of this response class is:
	// payload
	//   message
	//   alerts
	//     debugs
	//     infos
	//     warnings
	//     errors
	//  query_response
	//  structured_response
	//  stats
	//    transation_name
	//    sentence_count
	//    instance_count
	//    duration

	private static final String KEY_MESSAGE = "message";
	private static final String KEY_ALERTS = "alerts";
	private static final String KEY_DEBUG = "debugs";
	private static final String KEY_INFOS = "infos";
	private static final String KEY_WARNINGS = "warnings";
	private static final String KEY_ERRORS = "errors";
	protected static final String KEY_QUERY_STRUCTURED = "structured_response";
	private static final String KEY_STATS = "stats";
	private static final String KEY_DURATION = "duration";
	private static final String KEY_TXN_CODEVERSION = "code_version";
	private static final String KEY_TXN_NAME = "transaction_name";
	private static final String KEY_SERVER_TIME = "server_time";
	private static final String KEY_SEN_COUNT = "sentence_count";
	private static final String KEY_INST_COUNT = "instance_count";

	private ArrayList<String> msg = null;
	protected LinkedHashMap<String, ArrayList<String>> alerts = null;
	private ArrayList<String> debugs = null;
	private ArrayList<String> infos = null;
	private ArrayList<String> warnings = null;
	private ArrayList<String> errors = null;
	private long timeStart = -1;
	private String txnName = "";
	private boolean plainResponse = false;

	public ActionResponse() {
		initialiseFields();
	}

	private void initialiseFields() {
		this.timeStart = System.currentTimeMillis();
		this.alerts = new LinkedHashMap<String, ArrayList<String>>();
		this.msg = new ArrayList<String>();
		this.debugs = new ArrayList<String>();
		this.infos = new ArrayList<String>();
		this.warnings = new ArrayList<String>();
		this.errors = new ArrayList<String>();

		this.alerts.put(KEY_DEBUG, this.debugs);
		this.alerts.put(KEY_INFOS, this.infos);
		this.alerts.put(KEY_WARNINGS, this.warnings);
		this.alerts.put(KEY_ERRORS, this.errors);		
	}
	
	protected abstract void addMessagesToPayload(ActionContext pAc, String pKey, ArrayList<String> pValue);
	protected abstract void addStatsToPayload(ActionContext pAc, String pKey, LinkedHashMap<String, String> pValue);
	protected abstract void addAlertsToPayload(ActionContext pAc, String pKey, LinkedHashMap<String, ArrayList<String>> pValue);
	protected abstract void saveStructuredResult(ActionContext pAc);
	public abstract void setPlainTextPayload(ActionContext pAc, String pText);

	public abstract void convertAndRespond(ActionContext pAc, PrintWriter pOut, boolean pWrapResponse);
	public abstract void plainTextResponse(PrintWriter pOut);

	public boolean isPlainTextResponse() {
		return this.plainResponse;
	}

	public void setIsPlainTextResponse(boolean pValue) {
		this.plainResponse = pValue;
	}

	public String getTxnName() {
		return this.txnName;
	}
	
	public void setTxnName(String pName) {
		this.txnName = pName;
	}
	
	public void appendToTxnName(String pAddendum) {
		this.txnName += " " + pAddendum;
	}

	public ArrayList<String> getMessageLines() {
		return this.msg;
	}

	public void addLineToMessage(String pText) {
		this.msg.add(pText);
	}
	
	public ArrayList<String> getDebug() {
		return this.debugs;
	}
	
	private String calculateExecutionTime() {
		return Double.toString((System.currentTimeMillis() - this.timeStart) / 1000.00);
	}
	
	public void addErrorMessage(String pMsg) {
		this.errors.add(pMsg);
	}

	public void addWarningMessage(String pMsg) {
		this.warnings.add(pMsg);
	}

	public void addInfoMessage(String pMsg) {
		this.infos.add(pMsg);
	}

	public void addDebugMessage(String pMsg) {
		this.debugs.add(pMsg);
	}

	protected void populateStandardFields(ActionContext pAc) {
		LinkedHashMap<String, String> stats = new LinkedHashMap<String, String>();

		stats.put(KEY_TXN_NAME, this.txnName);
		stats.put(KEY_TXN_CODEVERSION, StoreConfig.VERSION);
		stats.put(KEY_SERVER_TIME, new Long(timestampNow()).toString());
		stats.put(KEY_DURATION, calculateExecutionTime());
		
		if (pAc.hasModelBuilder()) {
			stats.put(KEY_SEN_COUNT, Long.toString(pAc.getModelBuilder().countSentences()));
			stats.put(KEY_INST_COUNT, Long.toString(pAc.getModelBuilder().getTotalInstanceCount()));
		}
		
		addAlertsToPayload(pAc, KEY_ALERTS, this.alerts);
		addMessagesToPayload(pAc, KEY_MESSAGE, this.msg);
		addStatsToPayload(pAc, KEY_STATS, stats);
		saveStructuredResult(pAc);
	}
	
	public boolean hasErrors() {
		return (this.errors != null) && (!this.errors.isEmpty());
	}

	public ArrayList<String> getErrors() {
		return this.errors;
	}

	public boolean hasWarnings() {
		return (this.warnings != null) && (!this.warnings.isEmpty());
	}

	public ArrayList<String> getWarnings() {
		return this.warnings;
	}

	public String convertErrorsAndWarningsToHtml() {
		StringBuilder result = new StringBuilder();
		
		if (hasErrors()) {			
			for (String thisError : this.errors) {
				result.append(thisError + "(error)\n");
			}
		}
		
		if (hasWarnings()) {
			for (String thisWarning : this.warnings) {
				result.append(thisWarning + " (warning)\n");
			}
		}

		return result.toString();
	}

	public void returnInResponse(ActionContext pAc, PrintWriter pOut, boolean pWrapResponse) {
		if (isPlainTextResponse()) {
			//This is a special "plain" response so no JSON encoding is required
			plainTextResponse(pOut);
		} else { 
			//This is a normal (JSON) response
			convertAndRespond(pAc, pOut, pWrapResponse);
		}
	}

}