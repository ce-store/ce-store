package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ENTITIES;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class InstancePhrase extends InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<CeInstance> instances = new ArrayList<CeInstance>();

	public InstancePhrase(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonArray jEnts = pJo.getJsonArray(JSON_ENTITIES);

		if (jEnts != null) {
			for (Object thisObj : jEnts.items()) {
				CeStoreJsonObject jEnt = (CeStoreJsonObject) thisObj;

				String instId = jEnt.getString(JSON_ID);
				CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc, instId);

				if (thisInst != null) {
					this.instances.add(thisInst);
				}
			}
		}
	}

	public InstancePhrase(MatchedItem pMi) {
		super(pMi);

		CeInstance miInst = pMi.getInstance();

		if (miInst != null) {
			this.instances.add(miInst);
		}
	}

	public static ArrayList<InstancePhrase> createListFrom(ArrayList<MatchedItem> pMis) {
		ArrayList<InstancePhrase> result = new ArrayList<InstancePhrase>();

		for (MatchedItem thisMi : pMis) {
			result.add(new InstancePhrase(thisMi));
		}

		return result;
	}

	public ArrayList<CeInstance> getInstances() {
		return this.instances;
	}

	public CeInstance getFirstInstance() {
		CeInstance result = null;

		if ((this.instances != null) && (this.instances.size() > 0)) {
			result = this.instances.get(0);
		}

		return result;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		super.toJsonUsing(result);

		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeInstance thisInst : this.instances) {
			CeWebInstance webInst = new CeWebInstance(pAc);
			CeStoreJsonObject jInst = webInst.generateNormalisedDetailsJsonFor(thisInst, null, 0, false, false, null);

			jArr.add(jInst);
		}

		result.put(JSON_ENTITIES, jArr);

		return result;
	}

}
