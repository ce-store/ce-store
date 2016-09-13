package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;

public class BuilderSentenceFactTemporary extends BuilderSentenceFactNormal {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String originalText = null;
	private ArrayList<String> removedTokens = null;

	public BuilderSentenceFactTemporary(String pOrigSenText) {
		//Simply call the super constructor
		super(pOrigSenText);

		this.removedTokens = new ArrayList<String>();
		this.originalText = pOrigSenText;
		this.sentenceText = trimmedSentenceText(pOrigSenText);
	}

	private static String trimmedSentenceText(String pOrigSenText) {
		String result = pOrigSenText;

		result.replace("it is true that", "");	//TODO: Abstract these
		result.replace("it is false that", "");

		return result.trim();
	}

	@Override
	public void setRawTokens(ArrayList<String> pOrigTokens) {
		super.setRawTokens(trimmedRawTokens(pOrigTokens));
	}

	@Override
	public CeSentence convertToSentence(ActionContext pAc) {
		CeSource lastSrc = pAc.getLastSource();

		if (this.convertedSentence == null) {
			this.convertedSentence = CeSentence.createNewSentence(pAc, this.type, this.validity, this.originalText, getStructuredCeTokens(), lastSrc);
		}

		propogateRationaleValues();

		return this.convertedSentence;
	}

	@Override
	public ArrayList<String> getStructuredCeTokens() {
		ArrayList<String> upgradedToks = new ArrayList<String>();
		ArrayList<String> actualToks = this.structuredCeTokens;

		upgradedToks.addAll(this.removedTokens);
		upgradedToks.addAll(actualToks);

		return upgradedToks;
	}

	private ArrayList<String> trimmedRawTokens(ArrayList<String> pTokens) {
		ArrayList<String> result = new ArrayList<String>();

		//Simply remove the first 4 tokens
		for (int i = 0; i < pTokens.size(); i++) {
			String thisTok = pTokens.get(i);

			if (i >= 4) {
				result.add(thisTok);
			} else {
				this.removedTokens.add(thisTok);
			}
		}

		return result;
	}

}
