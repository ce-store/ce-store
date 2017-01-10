package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ANSWERS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RESTEXT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ALERTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_AL_ERRORS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_AL_WARNINGS;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ActionResponse;
import com.ibm.ets.ita.ce.store.hudson.model.Question;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ConvPhrase;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ConvWord;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ProcessedWord;

public abstract class QuestionHandler extends GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	protected Question question = null;
	protected ConvPhrase phrase = null;
	protected ArrayList<ProcessedWord> allWords = null;

	protected abstract CeStoreJsonObject handleQuestion();

	public QuestionHandler(ActionContext pAc, Question pQuestion, long pStartTime) {
		super(pAc, pStartTime);

		this.question = pQuestion;
		this.allWords = new ArrayList<ProcessedWord>();
	}

	protected String getQuestionText() {
		return this.question.getQuestionText();
	}

	protected void interpretQuestion() {
		this.phrase = ConvPhrase.createNewPhrase(this.ac, getQuestionText());

		prepareAndClassifyWords(this.phrase);
	}

	private void prepareAndClassifyWords(ConvPhrase pCp) {
		int wordPos = 0;

		//First create all the processed word instances
		for (ConvWord thisWord : pCp.getAllWords()) {
			ProcessedWord newPw = ProcessedWord.createFrom(thisWord, wordPos++);
			this.allWords.add(newPw);
		}

		//Then classify the processed words
		for (ProcessedWord thisWord : this.allWords) {
			thisWord.classify(this.ac, getConvConfig());
		}
	}

	public CeStoreJsonObject processQuestion() {
		CeStoreJsonObject jResult = handleQuestion();

		createJsonAlerts(jResult);

		return jResult;
	}

	public String processQuestionAndReturnAnswerText() {
		String result = null;
		CeStoreJsonObject jResponse = processQuestion();
		CeStoreJsonArray jAnswers = jResponse.getJsonArray(JSON_ANSWERS);

		if (!jAnswers.isEmpty()) {
			CeStoreJsonObject jFirstAns = (CeStoreJsonObject)jAnswers.get(0);

			if (jFirstAns != null) {
				result = jFirstAns.getString(JSON_A_RESTEXT);
			}
		}

		return result;
	}

	private void createJsonAlerts(CeStoreJsonObject pResult) {
		ActionResponse ar = this.ac.getActionResponse();
		CeStoreJsonArray jErrs = new CeStoreJsonArray();
		CeStoreJsonArray jWarns = new CeStoreJsonArray();

		if (ar.hasErrors()) {
			jErrs.addAll(ar.getErrors());
		}

		if (ar.hasWarnings()) {
			jWarns.addAll(ar.getWarnings());			
		}

		if (!jErrs.isEmpty() && !jWarns.isEmpty()) {
			CeStoreJsonObject jAlerts = new CeStoreJsonObject();

			jAlerts.put(JSON_AL_ERRORS, jErrs);
			jAlerts.put(JSON_AL_WARNINGS, jWarns);

			pResult.put(JSON_ALERTS, jAlerts);
		}
	}

}
