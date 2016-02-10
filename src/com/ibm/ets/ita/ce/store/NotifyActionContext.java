package com.ibm.ets.ita.ce.store;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

public class NotifyActionContext extends ActionContext {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public NotifyActionContext(ActionContext pAc, String pUserName) {
		super(pAc.getCeConfig(), pUserName, pAc.getActionResponse());

		this.setModelBuilderAndCeStoreName(pAc.getModelBuilder());
		this.markAsAutoExecuteRules(pAc.isAutoExecutingRules());
		this.markAsExecutingQueryOrRule(pAc.isExecutingQueryOrRule());
		this.setCredentials(pAc.getCredentials());
		this.getSessionCreations().setNewInstances(pAc.getSessionCreations().getNewInstances());
	}

	public void switchToStoreNamed(String pStoreName) {
		//TODO: Implement this
	}

}
