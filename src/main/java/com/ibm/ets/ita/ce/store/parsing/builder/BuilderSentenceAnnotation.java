package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class BuilderSentenceAnnotation extends BuilderSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public BuilderSentenceAnnotation(String pSenText) {
		super(pSenText);
	}

	@Override
	protected void propogateRationaleValues() {
		//Do nothing - rationale does not apply to annotation sentences
	}

	@Override
	public boolean hasRationale() {
		//Rationale does not apply to annotation sentences
		return false;
	}

}
