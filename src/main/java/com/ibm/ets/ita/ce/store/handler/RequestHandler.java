package com.ibm.ets.ita.ce.store.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;

public class RequestHandler {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	protected ActionContext ac = null;
	protected ModelBuilder mb = null;

	public RequestHandler(ActionContext pAc) {
		this.ac = pAc;
		this.mb = this.ac.getModelBuilder();
	}

}