package com.ibm.ets.ita.ce.store.hudson.model.special;

import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_MULTIMATCH;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class SpMultiMatch extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_MI = "multi-match instance";
	private static final String JSON_INSTS = "instances";

	private MatchedItem inst1 = null;
	private MatchedItem inst2 = null;
	private CeInstance matchedInst = null;

	public SpMultiMatch(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonObject jMm = pJo.getJsonObject("multi-match instance");

		//TODO: extract the instances too...

		if (jMm != null) {
			String id = jMm.getString("_id");
			CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc, id);

			if (thisInst != null) {
				this.matchedInst = thisInst;
			}
		}

	}

	public SpMultiMatch(String pPhraseText, int pStartPos, int pEndPos, MatchedItem pInst1, MatchedItem pInst2, CeInstance pMatchedInst) {
		super(pPhraseText, pStartPos, pEndPos);
		
		this.inst1 = pInst1;
		this.inst2 = pInst2;
		this.matchedInst = pMatchedInst;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonObject jInst1 = QuestionInterpreterHandler.jsonForMatchedItemInstance(pAc, this.inst1);
		CeStoreJsonObject jInst2 = QuestionInterpreterHandler.jsonForMatchedItemInstance(pAc, this.inst2);
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
