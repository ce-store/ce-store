package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class Question {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String questionText = null;
	private String interpretationText = null;
	private int interpretationConfidence = -1;
	private int abilityToAnswerConfidence = -1;
	
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

}