package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PHRASE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STARTPOS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ENDPOS;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;

public abstract class InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String phraseText = null;
	private int startPos = -1;
	private int endPos = -1;

	protected InterpretationPhrase(CeStoreJsonObject pJo) {
		this.phraseText = pJo.getString(JSON_PHRASE);
		this.startPos = pJo.getInt(JSON_STARTPOS);
		this.endPos = pJo.getInt(JSON_ENDPOS);
	}

	protected InterpretationPhrase(MatchedItem pMi) {
		this.phraseText = pMi.getPhraseText();
		this.startPos = pMi.getStartPos();
		this.endPos = pMi.getEndPos();
	}

	protected void toJsonUsing(CeStoreJsonObject pJo) {
		pJo.put(JSON_PHRASE, this.phraseText);
		pJo.put(JSON_STARTPOS, this.startPos);
		pJo.put(JSON_ENDPOS, this.endPos);
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
