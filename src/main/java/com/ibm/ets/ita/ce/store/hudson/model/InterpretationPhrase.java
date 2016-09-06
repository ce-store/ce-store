package com.ibm.ets.ita.ce.store.hudson.model;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public abstract class InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String phraseText = null;
	private int startPos = -1;
	private int endPos = -1;

	public InterpretationPhrase(CeStoreJsonObject pJo) {
		this.phraseText = pJo.getString("phrase");
		this.startPos = pJo.getInt("start position");
		this.endPos = pJo.getInt("end position");
	}

	public String getPhraseText() {
		return this.phraseText;
	}

	public int getStartPos() {
		return this.startPos;
	}

	public int getEndPos() {
		return this.endPos;
	}

}
