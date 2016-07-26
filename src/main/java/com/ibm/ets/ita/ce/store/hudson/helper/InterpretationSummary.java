package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;

public class InterpretationSummary {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<ProcessedWord> processedWords = null;
	private ArrayList<Interpretation> interpretations = null;

	private InterpretationSummary(ArrayList<ProcessedWord> pWords) {
		//private to enforce use of creator method
		this.processedWords = pWords;
		this.interpretations = new ArrayList<Interpretation>();

		for (ProcessedWord thisPw : this.processedWords) {
			this.interpretations.add(Interpretation.createUsing(thisPw));
		}
	}

	public static ArrayList<Interpretation> generateInterpretations(ArrayList<ProcessedWord> pWords) {
		InterpretationSummary is = new InterpretationSummary(pWords);

		return is.getInterpretations();
	}

	public ArrayList<Interpretation> getInterpretations() {

		return this.interpretations;
	}

}
