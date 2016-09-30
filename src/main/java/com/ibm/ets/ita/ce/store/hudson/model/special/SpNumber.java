package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_NUMBER;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;

public class SpNumber extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public SpNumber(String pPhraseText, int pWordPos) {
		super(pPhraseText, pWordPos, pWordPos);
	}

	public SpNumber(CeStoreJsonObject pJo) {
		super(pJo);
		// Nothing else needed
	}

	public boolean isNumber() {
		return true;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();

		addStandardFields(jResult, SPEC_NUMBER);

		return jResult;
	}

}
