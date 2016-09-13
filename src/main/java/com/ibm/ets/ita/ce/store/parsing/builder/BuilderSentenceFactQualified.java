package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class BuilderSentenceFactQualified extends BuilderSentenceFactNormal {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public BuilderSentenceFactQualified(String pSenText) {
		//Simply call the super constructor
		super(pSenText);
	}

	@Override
	public boolean isQualified() {
		return true;
	}

}
