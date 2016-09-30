package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_COLLECTION;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CONS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROPS;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;

public class SpCollection extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_CONNS = "connectors";

	private ArrayList<MatchedItem> connectors = null;
	private ArrayList<MatchedItem> items = null;

	public SpCollection(String pPhraseText, int pStartPos, int pEndPos, ArrayList<MatchedItem> pConns,
			ArrayList<MatchedItem> pItems) {
		super(pPhraseText, pStartPos, pEndPos);

		this.connectors = pConns;
		this.items = pItems;
	}

	public SpCollection(CeStoreJsonObject pJo) {
		super(pJo);
		// TODO: Complete this
	}

	public boolean isCollection() {
		return true;
	}

	public ArrayList<MatchedItem> getConnectors() {
		return this.connectors;
	}

	public ArrayList<MatchedItem> getItems() {
		return this.items;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonArray jConnectors = new CeStoreJsonArray();
		CeStoreJsonArray jInsts = new CeStoreJsonArray();
		CeStoreJsonArray jCons = new CeStoreJsonArray();
		CeStoreJsonArray jProps = new CeStoreJsonArray();

		addStandardFields(jResult, SPEC_COLLECTION);

		for (MatchedItem mi : getConnectors()) {
			CeStoreJsonArray mmArr = new CeStoreJsonArray();
			mmArr.add(QuestionInterpreterHandler.jsonFor(pAc, mi.getInstance()));

			jConnectors.add(QuestionInterpreterHandler.jsonForMatchedItem(mi, mmArr));
		}

		for (MatchedItem mi : getInstanceMatches()) {
			CeStoreJsonArray mmArr = new CeStoreJsonArray();
			mmArr.add(QuestionInterpreterHandler.jsonFor(pAc, mi.getInstance()));

			jInsts.add(QuestionInterpreterHandler.jsonForMatchedItem(mi, mmArr));
		}

		for (MatchedItem mi : getConceptMatches()) {
			CeStoreJsonArray mmArr = new CeStoreJsonArray();
			mmArr.add(QuestionInterpreterHandler.jsonFor(pAc, mi.getConcept()));

			jCons.add(QuestionInterpreterHandler.jsonForMatchedItem(mi, mmArr));
		}

		for (MatchedItem mi : getPropertyMatches()) {
			CeStoreJsonArray mmArr = new CeStoreJsonArray();
			mmArr.add(QuestionInterpreterHandler.jsonFor(pAc, mi.getProperty()));

			jProps.add(QuestionInterpreterHandler.jsonForMatchedItem(mi, mmArr));
		}

		if (!jConnectors.isEmpty()) {
			jResult.put(JSON_CONNS, jConnectors);
		}

		if (!jInsts.isEmpty()) {
			jResult.put(JSON_INSTS, jInsts);
		}

		if (!jCons.isEmpty()) {
			jResult.put(JSON_CONS, jCons);
		}

		if (!jProps.isEmpty()) {
			jResult.put(JSON_PROPS, jProps);
		}

		return jResult;
	}

	private ArrayList<MatchedItem> getInstanceMatches() {
		ArrayList<MatchedItem> result = new ArrayList<MatchedItem>();

		for (MatchedItem thisMi : this.items) {
			if (thisMi.hasInstance()) {
				result.add(thisMi);
			}
		}

		return result;
	}

	private ArrayList<MatchedItem> getConceptMatches() {
		ArrayList<MatchedItem> result = new ArrayList<MatchedItem>();

		for (MatchedItem thisMi : this.items) {
			if (thisMi.hasConcept()) {
				result.add(thisMi);
			}
		}

		return result;
	}

	private ArrayList<MatchedItem> getPropertyMatches() {
		ArrayList<MatchedItem> result = new ArrayList<MatchedItem>();

		for (MatchedItem thisMi : this.items) {
			if (thisMi.hasProperty()) {
				result.add(thisMi);
			}
		}

		return result;
	}

}
