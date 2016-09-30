package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_MULTIMATCH;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.hudson.model.InstancePhrase;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class SpMultiMatch extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_MI = "multi-match instance";
	private static final String JSON_INSTS = "instances";

	private InstancePhrase inst1 = null;
	private InstancePhrase inst2 = null;
	private CeInstance matchedInst = null;

	public SpMultiMatch(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonObject jMmi = pJo.getJsonObject(JSON_MI);
		String id = jMmi.getString(JSON_ID);
		this.matchedInst = pAc.getModelBuilder().getInstanceNamed(pAc, id);

		CeStoreJsonArray jArr = pJo.getJsonArray(JSON_INSTS);

		int pos = 0;
		for (Object thisItem : jArr.items()) {
			CeStoreJsonObject jInst = (CeStoreJsonObject) thisItem;

			if (pos == 0) {
				this.inst1 = new InstancePhrase(pAc, jInst);
			} else if (pos == 1) {
				this.inst2 = new InstancePhrase(pAc, jInst);
			}
		}
	}

	public SpMultiMatch(String pPhraseText, int pStartPos, int pEndPos, MatchedItem pInst1, MatchedItem pInst2,
			CeInstance pMatchedInst) {
		super(pPhraseText, pStartPos, pEndPos);

		this.inst1 = new InstancePhrase(pInst1);
		this.inst2 = new InstancePhrase(pInst2);
		this.matchedInst = pMatchedInst;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonObject jInst1 = this.inst1.toJson(pAc);
		CeStoreJsonObject jInst2 = this.inst2.toJson(pAc);
		CeStoreJsonObject jMi = QuestionInterpreterHandler.jsonFor(pAc, this.matchedInst);
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		jArr.add(jInst1);
		jArr.add(jInst2);

		addStandardFields(jResult, SPEC_MULTIMATCH);

		jResult.put(JSON_INSTS, jArr);
		jResult.put(JSON_MI, jMi);

		return jResult;
	}

	public boolean isMultiMatch() {
		return true;
	}

	public CeInstance getMatchedInstance() {
		return this.matchedInst;
	}

}
