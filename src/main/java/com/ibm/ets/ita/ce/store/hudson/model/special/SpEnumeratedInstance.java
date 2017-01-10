package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_NUM;
import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_ENUMINST;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ProcessedWord;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class SpEnumeratedInstance extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private String numberWordText = null;
	private ArrayList<MatchedItem> instanceItems = null;

	public SpEnumeratedInstance(CeStoreJsonObject pJo) {
		super(pJo);
		// TODO: Complete this
	}

	public SpEnumeratedInstance(String pPhraseText, int pEndPos, ProcessedWord pNumWord,
			ArrayList<MatchedItem> pInstItems) {
		super(pPhraseText, pNumWord.getWordPos(), pEndPos);

		this.numberWordText = pNumWord.getWordText();
		this.instanceItems = pInstItems;
	}

	public boolean isEnumeratedInstance() {
		return true;
	}

	public String getNumberWordText() {
		return this.numberWordText;
	}

	public ArrayList<MatchedItem> getInstanceItems() {
		return this.instanceItems;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonArray jInstList = new CeStoreJsonArray();

		for (MatchedItem thisInstItem : getInstanceItems()) {
			CeInstance thisInst = thisInstItem.getInstance();

			jInstList.add(QuestionInterpreterHandler.jsonFor(pAc, thisInst, thisInstItem));
		}

		addStandardFields(jResult, SPEC_ENUMINST);

		jResult.put(JSON_NUM, getNumberWordText());
		jResult.put(JSON_INSTS, jInstList);

		return jResult;
	}

}
