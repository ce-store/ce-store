package com.ibm.ets.ita.ce.store.hudson.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

public class SpNumber extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TYPE_NAME = "number";

	public SpNumber(String pPhraseText, int pWordPos) {
		super(pPhraseText, pWordPos, pWordPos);
	}

	public SpNumber(CeStoreJsonObject pJo) {
		super(pJo);
	}

	public boolean isNumber() {
		return true;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();

		addStandardFields(jResult, TYPE_NAME);

		return jResult;
	}

}
