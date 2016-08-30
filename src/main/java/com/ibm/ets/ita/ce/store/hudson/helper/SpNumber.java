package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

public class SpNumber extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TYPE_NAME = "number";

	private String label = null;

	public SpNumber(String pLabel) {
		this.label = pLabel;
	}

	public static SpNumber createFromJson(ActionContext pAc, CeStoreJsonObject pJo) {
		SpNumber result = new SpNumber("");

		result.extractStandardFieldsFromJson(pJo);

		return result;
	}
	
	public boolean isNumber() {
		return true;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public CeStoreJsonObject toJson(ActionContext pAc, int pCtr) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();

		jResult.put(JSON_TYPE, TYPE_NAME);
		jResult.put(JSON_NAME, getLabel());
		jResult.put(JSON_POS, pCtr);

		return jResult;
	}

}
