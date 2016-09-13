package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.RestNames.REST_HELPER;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_EXECUTOR;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_INTERPRETER;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_ANSWERER;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_RESET;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_STATUS;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_DIR_LIST;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_DIR_LOAD;

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
		String qt = getTextFromRequest();
		
		boolean plainText = false;

		if (isPost()) {
			if (command.equals(REST_HELPER)) {
				result = processHelperRequest(qt, st);
			} else if (command.equals(REST_EXECUTOR)) {
				result = processExecutorRequest(qt, st);
			} else if (command.equals(REST_INTERPRETER)) {
				result = processInterpreterRequest(qt, st);
			} else if (command.equals(REST_ANSWERER)) {
				result = processAnswererRequest(qt, st);
				plainText = true;
			} else if(command.equals(REST_DIR_LOAD)){
				result = processLoadDirectoryModel(st,getParameterNamed("model"));
			} else {
				reportUnhandledUrl();
			}
		} else if (isGet()) {
			if (command.equals(REST_RESET)) {
				//TODO: /reset should probably be implemented as a POST rather than GET
				result = processResetRequest(st);
			} else if (command.equals(REST_STATUS)) {
				result = processStatusRequest(st);
			} else if(command.equals(REST_DIR_LIST)){
				result = processListDirectoryModels(st);
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

	private CeStoreJsonObject processHelperRequest(String pQuestionText, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionHelpHandler qh = new QuestionHelpHandler(this.wc, pQuestionText, pStartTime);
		result = qh.processQuestion();

		return result;
	}

	private CeStoreJsonObject processExecutorRequest(String pQuestionText, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		ServletStateManager.getHudsonManager(this.wc).logQuestionText(this.wc, pQuestionText);

		QuestionExecutionHandler qe = new QuestionExecutionHandler(this.wc, pQuestionText, pStartTime);
		result = qe.processQuestion();

		return result;
	}

	private CeStoreJsonObject processInterpreterRequest(String pQuestionText, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionInterpreterHandler qh = new QuestionInterpreterHandler(this.wc, pQuestionText, pStartTime);
		result = qh.processQuestion();

		return result;
	}

	private CeStoreJsonObject processAnswererRequest(String pQuestionText, long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionAnswererHandler qh = new QuestionAnswererHandler(this.wc, pQuestionText, pStartTime);
		result = qh.processInterpretation();

		return result;
	}

	private CeStoreJsonObject processResetRequest(long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionManagementHandler qh = new QuestionManagementHandler(this.wc, pStartTime);
		result = qh.handleReset();

		return result;
	}
	
	private CeStoreJsonObject processListDirectoryModels(long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();
		ModelDirectoryHandler mdh = new ModelDirectoryHandler(this.wc, pStartTime);
		result = mdh.handleList();
		return result;
	}
	
	
	private CeStoreJsonObject processLoadDirectoryModel(long pStartTime, String modelName) {
		CeStoreJsonObject result = new CeStoreJsonObject();
		ModelDirectoryHandler mdh = new ModelDirectoryHandler(this.wc, pStartTime);
		result = mdh.handleLoad(modelName);
		return result;
	}

	private CeStoreJsonObject processStatusRequest(long pStartTime) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		QuestionManagementHandler qh = new QuestionManagementHandler(this.wc, pStartTime);
		result = qh.handleStatus();

		return result;
	}

}
