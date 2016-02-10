package com.ibm.ets.ita.ce.store.generation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;

public abstract class GeneralGenerator {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	protected ActionContext ac = null;

	protected GeneralGenerator(ActionContext pAc) {
		this.ac = pAc;
	}

}