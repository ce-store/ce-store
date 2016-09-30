package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_ENUMCON;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ProcessedWord;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class SpEnumeratedConcept extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_NUM = "number";
	private static final String JSON_CONS = "concepts";

	private String numberWordText = null;
	private ArrayList<MatchedItem> conceptItems = null;

	public SpEnumeratedConcept(CeStoreJsonObject pJo) {
		super(pJo);
		// TODO: Complete this
	}

	public SpEnumeratedConcept(String pPhraseText, int pEndPos, ProcessedWord pNumWord,
			ArrayList<MatchedItem> pConItems) {
		super(pPhraseText, pNumWord.getWordPos(), pEndPos);

		this.numberWordText = pNumWord.getWordText();
		this.conceptItems = pConItems;
	}

	public boolean isEnumeratedConcept() {
		return true;
	}

	public String getNumberWordText() {
		return this.numberWordText;
	}

	public ArrayList<MatchedItem> getConceptItems() {
		return this.conceptItems;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonArray jConList = new CeStoreJsonArray();

		for (MatchedItem thisConItem : getConceptItems()) {
			CeConcept thisCon = thisConItem.getConcept();
			CeInstance mmInst = thisCon.retrieveMetaModelInstance(pAc);

			jConList.add(QuestionInterpreterHandler.jsonFor(pAc, mmInst, thisConItem));
		}

		addStandardFields(jResult, SPEC_ENUMCON);

		jResult.put(JSON_NUM, getNumberWordText());
		jResult.put(JSON_CONS, jConList);

		return jResult;
	}
}
