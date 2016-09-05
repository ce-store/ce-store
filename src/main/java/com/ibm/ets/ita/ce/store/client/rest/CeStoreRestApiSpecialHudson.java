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
import com.ibm.ets.ita.ce.store.hudson.handler.ModelDirectoryHandler;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionAnswererHandler;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionExecutionHandler;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionHelpHandler;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionManagementHandler;

public class CeStoreRestApiSpecialHudson extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";
	
	private static final String REST_HELPER = "helper";
	private static final String REST_EXECUTOR = "executor";
	private static final String REST_INTERPRETER = "interpreter";
	private static final String REST_ANSWERER = "answerer";
	private static final String REST_RESET = "reset";
	private static final String REST_STATUS = "status";
	
	private static final String REST_DIR_LIST = "directory_list";
	private static final String REST_DIR_LOAD = "directory_load";

	private static final String PARM_DEBUG = "debug";

	public CeStoreRestApiSpecialHudson(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/special/hudson/helper
	 * 		/special/hudson/executor
	 * 		/special/hudson/interpreter
	 * 		/special/hudson/answerer
	 * 		/special/hudson/reset
	 * 		/special/hudson/status
	 * 		/special/hudson/directory_list
	 * 		/special/hudson/directory_load
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
		
		boolean plainText = false;

		if (isPost()) {
			if (command.equals(REST_HELPER)) {
				result = processHelperRequest(qt, debug, st);
			} else if (command.equals(REST_EXECUTOR)) {
				result = processExecutorRequest(qt, debug, st);
			} else if (command.equals(REST_INTERPRETER)) {
				result = processInterpreterRequest(qt, debug, st);
			} else if (command.equals(REST_ANSWERER)) {
				result = processAnswererRequest(qt, debug, st);
				plainText = true;
			} else if(command.equals(REST_DIR_LOAD)){
				result = processLoadDirectoryModel(debug,st,getParameterNamed("model"));
			} else {
				reportUnhandledUrl();
			}
		} else if (isGet()) {
			if (command.equals(REST_RESET)) {
				//TODO: /reset should probably be implemented as a POST rather than GET
				result = processResetRequest(debug, st);
			} else if (command.equals(REST_STATUS)) {
				result = processStatusRequest(debug, st);
			} else if(command.equals(REST_DIR_LIST)){
				result = processListDirectoryModels(debug,st);
			}else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}

		if (result != null) {
			if (plainText) {
				getWebActionResponse().setIsPlainTextResponse(true);
				getWebActionResponse().setPlainTextPayload(this.wc, (String)result.get(this.wc, "answer"));
			} else {
				getWebActionResponse().setPayloadTo(result);
			}
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

	private CeStoreJsonObject processInterpreterRequest(String pQuestionText, boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionInterpreterHandler qh = new QuestionInterpreterHandler(this.wc, pDebug, pQuestionText, pStartTime);
		result = qh.handleQuestion();

		return result;
	}

	private CeStoreJsonObject processAnswererRequest(String pQuestionText, boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionAnswererHandler qh = new QuestionAnswererHandler(this.wc, pDebug, pQuestionText, pStartTime);
		result = qh.handleQuestion();

		return result;
	}

	private CeStoreJsonObject processResetRequest(boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionManagementHandler qh = new QuestionManagementHandler(this.wc, pDebug, pStartTime);
		result = qh.handleReset();

		return result;
	}
	
	private CeStoreJsonObject processListDirectoryModels(boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();
		ModelDirectoryHandler mdh = new ModelDirectoryHandler(this.wc, pDebug, pStartTime);
		result = mdh.handleList();
		return result;
	}
	
	
	private CeStoreJsonObject processLoadDirectoryModel(boolean pDebug, long pStartTime, String modelName) {
		CeStoreJsonObject result = new CeStoreJsonObject();
		ModelDirectoryHandler mdh = new ModelDirectoryHandler(this.wc, pDebug, pStartTime);
		result = mdh.handleLoad(modelName);
		return result;
	}

	private CeStoreJsonObject processStatusRequest(boolean pDebug, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionManagementHandler qh = new QuestionManagementHandler(this.wc, pDebug, pStartTime);
		result = qh.handleStatus();

		return result;
	}

}
