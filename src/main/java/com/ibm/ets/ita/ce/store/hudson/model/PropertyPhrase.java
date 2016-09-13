package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class PropertyPhrase extends InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<CeProperty> properties = new ArrayList<CeProperty>();

	public PropertyPhrase(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonArray jEnts = pJo.getJsonArray("entities");

		if (jEnts != null) {
			for (Object thisObj : jEnts.items()) {
				CeStoreJsonObject jEnt = (CeStoreJsonObject)thisObj;
				
				String propName = jEnt.getString("_id");
				CeProperty thisProp = pAc.getModelBuilder().getPropertyFullyNamed(propName);
				
				if (thisProp != null) {
					this.properties.add(thisProp);
				}
			}
		}
	}

	public ArrayList<CeProperty> getProperties() {
		return this.properties;
	}

}
