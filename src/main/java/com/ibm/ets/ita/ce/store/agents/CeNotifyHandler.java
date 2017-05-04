package com.ibm.ets.ita.ce.store.agents;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public abstract class CeNotifyHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public abstract void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName,
			String pSourceId, String pRuleOrQuery, String pRuleOrQueryName);

	public static ContainerSentenceLoadResult saveCeText(ActionContext pAc, String pCeText, CeSource pSrc) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);

		return sa.saveCeText(pCeText, pSrc, false);
	}

	public static ContainerSentenceLoadResult saveCeText(ActionContext pAc, String pCeText) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);

		return sa.saveCeText(pCeText, null, false);
	}

}
