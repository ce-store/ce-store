package com.ibm.ets.ita.ce.store.client.web;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;

public class WebActionContext extends ActionContext {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	protected WebActionContext(String pUserName, WebActionResponse pAr) {
		super(pUserName, pAr);
	}

	public WebActionResponse getWebActionResponse() {
		return (WebActionResponse) this.ar;
	}

	public void switchToStoreNamed(String pStoreName) {
		ModelBuilder tgtMb = ServletStateManager.getServletStateManager().getModelBuilder(pStoreName);

		// The CE Store in question will be created if it does not already exist
		if (tgtMb == null) {
			tgtMb = ServletStateManager.getServletStateManager().createModelBuilder(this, pStoreName);
		}

		setModelBuilderAndCeStoreName(tgtMb);
	}

}
