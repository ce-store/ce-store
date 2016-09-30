package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_LINKEDINST;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class SpLinkedInstance extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_MINST = "matched instance";
	private static final String JSON_LINST = "linked instances";
	private static final String JSON_PROP = "property";

	private MatchedItem matchedInstance = null;
	private CeProperty linkingProperty = null;
	private ArrayList<CeInstance> linkedInstances = null;

	public SpLinkedInstance(CeStoreJsonObject pJo) {
		super(pJo);
		// TODO: Complete this
	}

	public SpLinkedInstance(String pPhraseText, MatchedItem pMatchedInstance, CeProperty pLinkingProperty,
			ArrayList<CeInstance> pLinkedInstances) {
		super(pPhraseText, pMatchedInstance.getStartPos(), pMatchedInstance.getEndPos());

		this.matchedInstance = pMatchedInstance;
		this.linkingProperty = pLinkingProperty;
		this.linkedInstances = pLinkedInstances;
	}

	public boolean isLinkedInstance() {
		return true;
	}

	public MatchedItem getMatchedInstance() {
		return this.matchedInstance;
	}

	public CeProperty getLinkingProperty() {
		return this.linkingProperty;
	}

	public ArrayList<CeInstance> getLinkedInstances() {
		return this.linkedInstances;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();

		addStandardFields(jResult, SPEC_LINKEDINST);

		jResult.put(JSON_MINST, QuestionInterpreterHandler.jsonFor(pAc, getMatchedInstance().getInstance()));
		jResult.put(JSON_LINST, QuestionInterpreterHandler.jsonFor(pAc, getLinkedInstances()));
		jResult.put(JSON_PROP, QuestionInterpreterHandler.jsonFor(pAc, getLinkingProperty()));

		return jResult;
	}

}
