package com.ibm.ets.ita.ce.store.hudson.model.answer;

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_CONF;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTS;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public abstract class Answer implements Comparable<Answer> {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected int confidence = -1;
	protected ArrayList<CeInstance> instances = null;

	public abstract CeStoreJsonObject specificJson();

	public Answer(int pConf) {
		this.confidence = pConf;
		this.instances = new ArrayList<CeInstance>();
	}
	public Answer(int pConf, ArrayList<CeInstance> pInstList) {
		this.confidence = pConf;
		this.instances = pInstList;
	}

	@Override
	public int compareTo(Answer pOtherAnswer) {
		return pOtherAnswer.getConfidence() - this.confidence;
	}
	
	public int getConfidence() {
		return this.confidence;
	}
	
	public void addInstance(CeInstance pInst) {
		this.instances.add(pInst);
	}

	public CeStoreJsonObject toJson(ActionContext pAc, boolean pRetInsts) {
		CeStoreJsonObject result = specificJson();

		result.put(JSON_A_CONF, this.confidence);

		if (pRetInsts) {
			jsonAddAnswerInstances(pAc, result);
		}

		return result;
	}
	
	protected void jsonAddAnswerInstances(ActionContext pAc, CeStoreJsonObject pJo) {
		if (!this.instances.isEmpty()) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();
			pJo.put(JSON_INSTS, jArr);

			for (CeInstance thisInst : this.instances) {
				CeWebInstance webInst = new CeWebInstance(pAc);
				CeStoreJsonObject jInst = webInst.generateNormalisedDetailsJsonFor(thisInst, null, 0, false, false, null);
	
				jArr.add(jInst);
			}
		}
	}

}
