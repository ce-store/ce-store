package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionAnalysisHandler;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionExecutionHandler;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionHelpHandler;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionManagementHandler;

public class CeStoreRestApiSpecialHudson extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";
	
	private static final String REST_HELPER = "helper";
	private static final String REST_EXECUTOR = "executor";
	private static final String REST_ANALYSER = "analyser";
	private static final String REST_RESET = "reset";
	private static final String REST_STATUS = "status";

	private static final String PARM_DEBUG = "debug";

	public CeStoreRestApiSpecialHudson(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/special/hudson/helper
	 * 		/special/hudson/executor
	 * 		/special/hudson/analyser
	 * 		/special/hudson/reset
	 * 		/special/hudson/status
	 */
	public boolean processRequest() {
		if (this.restParts.size() == 3) {
			processThreeElementHudsonRequest();
		} else {
			reportUnhandledUrl();
		}

		return false;
	}

	private void processThreeElementHudsonRequest() {
		CeStoreJsonObject result = null;
		long st = System.currentTimeMillis();
		String command = this.restParts.get(2);
		boolean debug = getBooleanParameterNamed(PARM_DEBUG, false);
		String qt = getTextFromRequest();

		if (isPost()) {
			if (command.equals(REST_HELPER)) {
				result = processHelperRequest(qt, debug, st);
			} else if (command.equals(REST_EXECUTOR)) {
				result = processExecutorRequest(qt, debug, st);
			} else if (command.equals(REST_ANALYSER)) {
				result = processAnalyserRequest(qt, debug, st);
			} else {
				reportUnhandledUrl();
			}
		} else if (isGet()) {
			if (command.equals(REST_RESET)) {
				//TODO: /reset should probably be implemented as a POST rather than GET
				result = processResetRequest(debug, st);
			} else if (command.equals(REST_STATUS)) {
				result = processStatusRequest(debug, st);
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}

		if (result != null) {
			getWebActionResponse().setPayloadTo(result);
		}
	}

	private CeStoreJsonObject processHelperRequest(String pQuestionText, boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionHelpHandler qh = new QuestionHelpHandler(this.wc, pDebug, pQuestionText, pStartTime);
		result = qh.handleQuestion();

		return result;
	}

	private CeStoreJsonObject processExecutorRequest(String pQuestionText, boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		ServletStateManager.getHudsonManager(this.wc).logQuestionText(this.wc, pQuestionText);

		QuestionExecutionHandler qe = new QuestionExecutionHandler(this.wc, pDebug, pQuestionText, pStartTime);
		result = qe.handleQuestion();

		return result;
	}

	private CeStoreJsonObject processAnalyserRequest(String pQuestionText, boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionAnalysisHandler qh = new QuestionAnalysisHandler(this.wc, pDebug, pQuestionText, pStartTime);
		result = qh.handleQuestion();

		return result;
	}

	private CeStoreJsonObject processResetRequest(boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionManagementHandler qh = new QuestionManagementHandler(this.wc, pDebug, pStartTime);
		result = qh.handleReset();

		return result;
	}

	private CeStoreJsonObject processStatusRequest(boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionManagementHandler qh = new QuestionManagementHandler(this.wc, pDebug, pStartTime);
		result = qh.handleStatus();

		return result;
	}

}
