package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ActionResponse;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.conversation.model.ConvPhrase;
import com.ibm.ets.ita.ce.store.conversation.model.ConvWord;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public abstract class QuestionHandler extends GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected static final String JSON_DEBUG = "debug";
	protected static final String JSON_ET = "execution_time_ms";
	protected static final String JSON_ALERTS = "alerts";
	protected static final String JSON_ERRORS = "errors";
	protected static final String JSON_WARNINGS = "warnings";
	protected static final String JSON_DEBUGS = "debugs";

	protected Question question = null;
	protected ConvPhrase phrase = null;
	protected ArrayList<ProcessedWord> allWords = null;
	protected String unitName = null;

	public abstract CeStoreJsonObject handleQuestion();

	public QuestionHandler(ActionContext pAc, boolean pDebug, Question pQuestion, long pStartTime) {
		super(pAc, pDebug, pStartTime);

		this.question = pQuestion;
		this.allWords = new ArrayList<ProcessedWord>();
	}

	protected String getQuestionText() {
		return this.question.getQuestionText();
	}

	protected void interpretQuestion() {
		//This is the original CE processing
		this.phrase = originalCePhraseProcessing();
		originalCeWordClassifying(this.phrase);

		extractUnitName();
	}

	private void extractUnitName() {
		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isGroundedOnConcept()) {
				for (CeConcept thisCon : thisWord.listGroundedConcepts()) {
					CeInstance mmInst = thisCon.retrieveMetaModelInstance(this.ac);

					if (mmInst != null) {
						if (mmInst.isConceptNamed(this.ac, CON_QUALCON)) {
							if (this.unitName == null) {
								this.unitName = mmInst.getSingleValueFromPropertyNamed(PROP_UNITNAME);
							}
						}
					}
				}
			}
		}
	}

	private ConvPhrase originalCePhraseProcessing() {
		ConvPhrase cp = ConvPhrase.createNewPhrase(this.ac, getQuestionText());

		return cp;
	}

	private void originalCeWordClassifying(ConvPhrase pCp) {
		prepareAndClassifyWords(pCp);
	}

	private void prepareAndClassifyWords(ConvPhrase pCp) {
		int wordPos = 0;

		//First create all the processed word instances
		for (ConvWord thisWord : pCp.getAllWords()) {
			ProcessedWord newPw = ProcessedWord.createFrom(thisWord, wordPos++);
			this.allWords.add(newPw);
		}

		markQuestionWords();

		//Then classify the processed words
		for (ProcessedWord thisWord : this.allWords) {
			thisWord.classify(this.ac, getConvConfig());
		}
	}

	private void markQuestionWords() {
		ArrayList<String> qsws = null;

		if (getConvConfig() != null) {
			qsws = getConvConfig().getQuestionStartMarkers();

			for (ProcessedWord thisPw : this.allWords) {
				for (String thisQsw : qsws) {
					if (thisPw.getLcWordText().equals(thisQsw)) {
						thisPw.markAsQuestionWord();
					}
				}
			}
		}
	}
	
	protected void createJsonDebugs(CeStoreJsonObject pResult) {
		ActionResponse ar = this.ac.getActionResponse();
		
		if (!ar.getDebug().isEmpty()) {
			CeStoreJsonArray jDebugs = new CeStoreJsonArray();
			jDebugs.addAll(ar.getDebug());

			pResult.put(JSON_DEBUGS, jDebugs);
		}
	}

	protected void createJsonAlerts(CeStoreJsonObject pResult) {
		ActionResponse ar = this.ac.getActionResponse();
		
		if (ar.hasErrors()) {
			CeStoreJsonArray jErrs = new CeStoreJsonArray();
			jErrs.addAll(ar.getErrors());
			
			pResult.put(JSON_ERRORS, jErrs);
		}

		if (ar.hasWarnings()) {
			CeStoreJsonArray jWarns = new CeStoreJsonArray();
			jWarns.addAll(ar.getWarnings());
			
			pResult.put(JSON_WARNINGS, jWarns);
		}
	}

}