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
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class PropertyPhrase extends InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<CeProperty> properties = new ArrayList<CeProperty>();

	public PropertyPhrase(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonArray jEnts = pJo.getJsonArray(JSON_ENTITIES);

		if (jEnts != null) {
			for (Object thisObj : jEnts.items()) {
				CeStoreJsonObject jEnt = (CeStoreJsonObject) thisObj;

				String propName = jEnt.getString(JSON_ID);
				CeProperty thisProp = pAc.getModelBuilder().getPropertyFullyNamed(propName);

				if (thisProp != null) {
					this.properties.add(thisProp);
				}
			}
		}
	}

	public PropertyPhrase(MatchedItem pMi) {
		super(pMi);

		CeProperty miProp = pMi.getProperty();

		if (miProp != null) {
			this.properties.add(miProp);
		}
	}

	public ArrayList<CeProperty> getProperties() {
		return this.properties;
	}

	public CeProperty getFirstProperty() {
		CeProperty result = null;

		if ((this.properties != null) && (this.properties.size() > 0)) {
			result = this.properties.get(0);
		}

		return result;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		super.toJsonUsing(result);

		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeProperty thisProp : this.properties) {
			CeInstance mmInst = thisProp.getMetaModelInstance(pAc);

			if (mmInst != null) {
				CeWebInstance webInst = new CeWebInstance(pAc);
				CeStoreJsonObject jInst = webInst.generateNormalisedDetailsJsonFor(mmInst, null, 0, false, false, null);

				jArr.add(jInst);
			}
		}

		result.put(JSON_ENTITIES, jArr);

		return result;
	}

}
