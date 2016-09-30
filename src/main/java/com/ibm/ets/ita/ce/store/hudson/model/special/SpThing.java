package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;

public abstract class SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected static final String JSON_TYPE = "type";
	protected static final String JSON_PHRASE = "phrase";
	protected static final String JSON_STARTPOS = "start position";
	protected static final String JSON_ENDPOS = "end position";
	protected static final String JSON_CEID = "_id";

	protected String phraseText = null;
	protected int startPos = -1;
	protected int endPos = -1;

	protected SpThing(CeStoreJsonObject pJo) {
		this.phraseText = pJo.getString(JSON_PHRASE);
		this.startPos = pJo.getInt(JSON_STARTPOS);
		this.endPos = pJo.getInt(JSON_ENDPOS);
	}

	protected SpThing(MatchedItem pMi) {
		this.phraseText = pMi.getPhraseText();
		this.startPos = pMi.getStartPos();
		this.endPos = pMi.getEndPos();
	}

	protected SpThing(String pPhraseText, int pStartPos, int pEndPos) {
		this.phraseText = pPhraseText;
		this.startPos = pStartPos;
		this.endPos = pEndPos;
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

	protected void addStandardFields(CeStoreJsonObject pJo, String pTypeName) {
		pJo.put(JSON_TYPE, pTypeName);
		pJo.put(JSON_PHRASE, getPhraseText());
		pJo.put(JSON_STARTPOS, getStartPos());
		pJo.put(JSON_ENDPOS, getEndPos());
	}

	public boolean isNumber() {
		return false;
	}

	public boolean isMatchedTriple() {
		return false;
	}

	public boolean isMultiMatch() {
		return false;
	}

	public boolean isLinkedInstance() {
		return false;
	}

	public boolean isCollection() {
		return false;
	}

	public boolean isEnumeratedConcept() {
		return false;
	}

}
