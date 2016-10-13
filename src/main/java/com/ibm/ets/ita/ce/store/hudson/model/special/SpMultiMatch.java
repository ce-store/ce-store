package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_MI;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_MATCHES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INST;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROPNAME;
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

	private InstancePhrase inst1 = null;
	private InstancePhrase inst2 = null;
	private String propName1 = null;
	private String propName2 = null;
	private CeInstance matchedInst = null;

	public SpMultiMatch(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonObject jMmi = pJo.getJsonObject(JSON_MI);
		String id = jMmi.getString(JSON_ID);
		this.matchedInst = pAc.getModelBuilder().getInstanceNamed(pAc, id);

		CeStoreJsonArray jArr = pJo.getJsonArray(JSON_MATCHES);

		int pos = 0;
		for (Object thisItem : jArr.items()) {
			CeStoreJsonObject jMatch = (CeStoreJsonObject) thisItem;
			String propName = jMatch.getString(JSON_PROPNAME);
			CeStoreJsonObject jInst = jMatch.getJsonObject(JSON_INST);

			if (pos == 0) {
				this.inst1 = new InstancePhrase(pAc, jInst);
				this.propName1 = propName;
			} else if (pos == 1) {
				this.inst2 = new InstancePhrase(pAc, jInst);
				this.propName2 = propName;
			}

			++pos;
		}
	}

	public SpMultiMatch(String pPhraseText, int pStartPos, int pEndPos, MatchedItem pInst1, MatchedItem pInst2,
			String pPropName1, String pPropName2, CeInstance pMatchedInst) {
		super(pPhraseText, pStartPos, pEndPos);

		this.inst1 = new InstancePhrase(pInst1);
		this.inst2 = new InstancePhrase(pInst2);
		this.propName1 = pPropName1;
		this.propName2 = pPropName2;
		this.matchedInst = pMatchedInst;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonObject jInst1 = this.inst1.toJson(pAc);
		CeStoreJsonObject jInst2 = this.inst2.toJson(pAc);
		CeStoreJsonObject jMi = QuestionInterpreterHandler.jsonFor(pAc, this.matchedInst);
		CeStoreJsonArray jArrMatches = new CeStoreJsonArray();
		CeStoreJsonObject jMatch1 = new CeStoreJsonObject();
		CeStoreJsonObject jMatch2 = new CeStoreJsonObject();

		jMatch1.put(JSON_INST, jInst1);
		jMatch1.put(JSON_PROPNAME, this.propName1);

		jMatch2.put(JSON_INST, jInst2);
		jMatch2.put(JSON_PROPNAME, this.propName2);

		jArrMatches.add(jMatch1);
		jArrMatches.add(jMatch2);

		addStandardFields(jResult, SPEC_MULTIMATCH);

		jResult.put(JSON_MATCHES, jArrMatches);
		jResult.put(JSON_MI, jMi);

		return jResult;
	}

	public boolean isMultiMatch() {
		return true;
	}

	public CeInstance getMatchedInstance() {
		return this.matchedInst;
	}

	public InstancePhrase getInstPhrase1() {
		return this.inst1;
	}

	public InstancePhrase getInstPhrase2() {
		return this.inst2;
	}

	public String getPropertyName1() {
		return this.propName1;
	}

	public String getPropertyName2() {
		return this.propName2;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("SpMultiMatch: ");

		sb.append("(");
		if (this.matchedInst != null) {
			sb.append(this.matchedInst.getInstanceName());
			
		}
		sb.append(") ");

		sb.append(" inst1={");
		if (this.inst1 != null) {
			sb.append(this.inst1.toString());
		}

		sb.append("}, inst2={");
		if (this.inst2 != null) {
			sb.append(this.inst2.toString());
		}
		sb.append("}");

		return sb.toString();
	}

}
