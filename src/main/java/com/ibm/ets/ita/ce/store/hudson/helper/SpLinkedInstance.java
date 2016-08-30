package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class SpLinkedInstance extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TYPE_NAME = "linked-instance";
	private static final String JSON_MINST = "matched-instance";
	private static final String JSON_LINST = "linked-instance";
	private static final String JSON_PROP = "property";

	private CeInstance matchedInstance = null;
	private CeProperty linkingProperty = null;
	private ArrayList<CeInstance> linkedInstances = null;

	public SpLinkedInstance(CeInstance pMatchedInstance, CeProperty pLinkingProperty, ArrayList<CeInstance> pLinkedInstances, String pLabel) {
		this.matchedInstance = pMatchedInstance;
		this.linkingProperty = pLinkingProperty;
		this.linkedInstances = pLinkedInstances;
		this.label = pLabel;
	}

	public static SpLinkedInstance createFromJson(ActionContext pAc, CeStoreJsonObject pJo) {
		SpLinkedInstance result = new SpLinkedInstance(null, null, null, "");

		result.extractStandardFieldsFromJson(pJo);

		//TODO: Complete this
		return result;
	}
	
	public boolean isLinkedInstance() {
		return true;
	}
	
	public CeInstance getMatchedInstance() {
		return this.matchedInstance;
	}

	public CeProperty getLinkingProperty() {
		return this.linkingProperty;
	}

	public ArrayList<CeInstance> getLinkedInstances() {
		return this.linkedInstances;
	}

	public CeStoreJsonObject toJson(ActionContext pAc, int pCtr) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();

		jResult.put(JSON_TYPE, TYPE_NAME);
		jResult.put(JSON_NAME, getLabel());
		jResult.put(JSON_POS, pCtr);
		jResult.put(JSON_MINST, QuestionInterpreterHandler.jsonFor(pAc, getMatchedInstance()));
		jResult.put(JSON_LINST, QuestionInterpreterHandler.jsonFor(pAc, getLinkedInstances()));
		jResult.put(JSON_PROP, QuestionInterpreterHandler.jsonFor(pAc, getLinkingProperty()));

		return jResult;
	}

}
