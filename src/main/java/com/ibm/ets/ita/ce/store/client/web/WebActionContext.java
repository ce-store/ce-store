package com.ibm.ets.ita.ce.store.client.web;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.StoreConfig;

public class WebActionContext extends ActionContext {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	protected WebActionContext(StoreConfig pCc, String pUserName, WebActionResponse pAr) {
		super(pCc, pUserName, pAr);
	}
	
	public WebActionResponse getWebActionResponse() {
		return (WebActionResponse)this.ar;
	}

	public void switchToStoreNamed(String pStoreName) {
		ModelBuilder tgtMb = ServletStateManager.getServletStateManager().getModelBuilder(pStoreName);
		
		//the CE Store in question will be created if it does not already exist
		if (tgtMb == null) {
			tgtMb = ServletStateManager.getServletStateManager().createModelBuilder(this, pStoreName);
		}
		
		setModelBuilderAndCeStoreName(tgtMb);
	}

}