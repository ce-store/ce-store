package com.ibm.ets.ita.ce.store.hudson.model;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

public class Question {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private String questionText = null;
	private ArrayList<String> words = null;
	private String interpretationText = null;
	private int interpretationConfidence = -1;
	private String interpretationConfidenceExplanation = null;
	private int abilityToAnswerConfidence = -1;

	public Question(CeStoreJsonObject pJo) {
		this.questionText = pJo.getString("text");
		this.words = new ArrayList<String>();

		for (Object thisObj : pJo.getJsonArray("words").items()) {
			String thisWord = (String)thisObj;

			this.words.add(thisWord);
		}
	}
	
	private Question(String pQt) {
		//Private to enforce use of creator methods
		this.questionText = pQt;
	}

	public static Question create(String pQt) {
		return new Question(pQt);
	}

	public String getQuestionText() {
		return this.questionText;
	}

	public void setInterpretationText(String pText) {
		this.interpretationText = pText;
	}

	public String getInterpretationText() {
		return this.interpretationText;
	}

	public int getAbilityToAnswerConfidence() {
		return this.abilityToAnswerConfidence;
	}

	public void setAbilityToAnswerConfidence(int pVal) {
		this.abilityToAnswerConfidence = pVal;
	}

	public int getInterpretationConfidence() {
		return this.interpretationConfidence;
	}

	public void setInterpretationConfidence(int pVal) {
		this.interpretationConfidence = pVal;
	}

	public String getInterpretationConfidenceExplanation() {
		return this.interpretationConfidenceExplanation;
	}

	public void setInterpretationConfidenceExplanation(String pVal) {
		this.interpretationConfidenceExplanation = pVal;
	}

	public boolean endsWithPunctuation() {
		//TODO: Make this more dynamic
		return this.questionText.endsWith("?");
	}

	@Override
	public String toString() {
		return "Question: " + this.questionText;
	}

}
