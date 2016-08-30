package com.ibm.ets.ita.ce.store.hudson.helper;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class SpEnumeratedConcept extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TYPE_NAME = "enumerated-concept";
	private static final String JSON_NUM = "number";
	private static final String JSON_CONS = "concepts";

	private String numberWordText = null;
	private String conceptWordText = null;
	private ArrayList<CeConcept> concepts = null;

	public SpEnumeratedConcept(String pNumWordText, String pConWordText, ArrayList<CeConcept> pCons, String pLabel) {
		this.label = pLabel;
		this.numberWordText = pNumWordText;
		this.conceptWordText = pConWordText;
		this.concepts = pCons;
	}

	public static SpEnumeratedConcept createFromJson(ActionContext pAc, CeStoreJsonObject pJo) {
		SpEnumeratedConcept result = new SpEnumeratedConcept("", "", null, "");

		result.extractStandardFieldsFromJson(pJo);

		//TODO: Complete this
		return result;
	}

	public boolean isEnumeratedConcept() {
		return true;
	}

	public String getNumberWordText() {
		return this.numberWordText;
	}

	public String getConceptWordText() {
		return this.conceptWordText;
	}

	public ArrayList<CeConcept> getConcepts() {
		return this.concepts;
	}

	public CeStoreJsonObject toJson(ActionContext pAc, int pCtr) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonArray jConList = new CeStoreJsonArray();

		for (CeConcept thisCon : getConcepts()) {
			CeInstance mmInst = thisCon.retrieveMetaModelInstance(pAc);

			jConList.add(QuestionInterpreterHandler.jsonFor(pAc, mmInst));
		}

		jResult.put(JSON_TYPE, TYPE_NAME);
		jResult.put(JSON_NAME, getLabel());
		jResult.put(JSON_POS, pCtr);
		jResult.put(JSON_NUM, getNumberWordText());
		jResult.put(JSON_CONS, jConList);

		return jResult;
	}
}
