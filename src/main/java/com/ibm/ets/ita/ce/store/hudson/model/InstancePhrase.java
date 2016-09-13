package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class InstancePhrase extends InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<CeInstance> instances = new ArrayList<CeInstance>();

	public InstancePhrase(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonArray jEnts = pJo.getJsonArray("entities");

		if (jEnts != null) {
			for (Object thisObj : jEnts.items()) {
				CeStoreJsonObject jEnt = (CeStoreJsonObject)thisObj;
				
				String instId = jEnt.getString("_id");
				CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc, instId);
				
				if (thisInst != null) {
					this.instances.add(thisInst);
				}
			}
		}
	}

	public ArrayList<CeInstance> getInstances() {
		return this.instances;
	}

}
