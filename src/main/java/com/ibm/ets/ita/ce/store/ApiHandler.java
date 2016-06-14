package com.ibm.ets.ita.ce.store;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/


public abstract class ApiHandler {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public abstract boolean isDefaultStyle();
	public abstract boolean isFullStyle();
	public abstract boolean isSummaryStyle();
	public abstract boolean isMinimalStyle();

}
