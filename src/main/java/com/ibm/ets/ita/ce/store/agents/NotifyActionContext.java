package com.ibm.ets.ita.ce.store.agents;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation 2011, 2016 All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class NotifyActionContext extends ActionContext {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public NotifyActionContext(ActionContext pAc, String pUserName) {
		super(pUserName, pAc.getActionResponse());

		setModelBuilderAndCeStoreName(pAc.getModelBuilder());
		markAsAutoExecuteRules(pAc.isAutoExecutingRules());
		markAsExecutingQueryOrRule(pAc.isExecutingQueryOrRule());
		setCredentials(pAc.getCredentials());
		getSessionCreations().setNewInstances(pAc.getSessionCreations().getNewInstances());
	}

	public void switchToStoreNamed(String pStoreName) {
		// TODO: Implement this
	}

}
