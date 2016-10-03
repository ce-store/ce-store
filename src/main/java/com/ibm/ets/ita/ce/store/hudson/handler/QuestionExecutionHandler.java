package com.ibm.ets.ita.ce.store.hudson.handler;

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ALERTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_AL_ERRORS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_AL_WARNINGS;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.io.IOException;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.model.Question;

public class QuestionExecutionHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = QuestionHandler.class.getName();
	private static final String PACKAGE_NAME = QuestionHandler.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private boolean returnInterpretation = false;
	private boolean returnInstances = false;
	
	public QuestionExecutionHandler(ActionContext pAc, String pQt, boolean pRetInt, boolean pRetInsts, long pStartTime) {
		super(pAc, Question.create(pQt), pStartTime);

		this.returnInterpretation = pRetInt;
		this.returnInstances = pRetInsts;
	}

	@Override
	protected CeStoreJsonObject handleQuestion() {
		String METHOD_NAME = "handleQuestion";

		QuestionInterpreterHandler qih = new QuestionInterpreterHandler(this.ac, this.question.getQuestionText(), System.currentTimeMillis());
		CeStoreJsonObject intResult = qih.handleQuestion();
		CeStoreJsonObject ansResult = null;
		String jsonText = null;

		try {
			jsonText = intResult.serialize(this.ac);
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}

		if (jsonText != null) {
			QuestionAnswererHandler qah = new QuestionAnswererHandler(this.ac, jsonText, this.returnInterpretation, this.returnInstances, System.currentTimeMillis());
			ansResult = qah.processInterpretation();
			
			mergeAlerts(ansResult, intResult);
		} else {
			ansResult = new CeStoreJsonObject();
		}
		
		return ansResult;
	}
	
	private void mergeAlerts(CeStoreJsonObject pAnsJson, CeStoreJsonObject pIntJson) {
		CeStoreJsonObject ansAlerts = pAnsJson.getJsonObject(JSON_ALERTS);
		CeStoreJsonObject intAlerts = pAnsJson.getJsonObject(JSON_ALERTS);
		
		if ((ansAlerts != null) && !ansAlerts.isEmpty()) {
			if ((intAlerts != null) && !intAlerts.isEmpty()) {
				mergeArrays(ansAlerts.getJsonArray(JSON_AL_ERRORS), intAlerts.getJsonArray(JSON_AL_ERRORS));
				mergeArrays(ansAlerts.getJsonArray(JSON_AL_WARNINGS), intAlerts.getJsonArray(JSON_AL_WARNINGS));
			}			
		}
	}
	
	private void mergeArrays(CeStoreJsonArray pArr1, CeStoreJsonArray pArr2) {
		if ((pArr1 != null) && !pArr1.isEmpty()) {
			if ((pArr2 != null) && !pArr2.isEmpty()) {
				for (Object thisObj : pArr2.items()) {
					pArr1.add(thisObj);
				}
			}
		}
	}

}
