package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;

public class SpEnumeratedConcept extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String label = null;
	private ProcessedWord numberWord = null;
	private ProcessedWord conceptWord = null;

	public SpEnumeratedConcept(ProcessedWord pNumWord, ProcessedWord pConWord, String pLabel) {
		this.label = pLabel;
		this.numberWord = pNumWord;
		this.conceptWord = pConWord;
	}

	public String getLabel() {
		return this.label;
	}

	public ProcessedWord getNumberWord() {
		return this.numberWord;
	}

	public ProcessedWord getConceptWord() {
		return this.conceptWord;
	}

}
