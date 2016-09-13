package com.ibm.ets.ita.ce.store.client.rest;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation 2011, 2016 All Rights Reserved
 *******************************************************************************/

public abstract class ApiHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public abstract boolean isDefaultStyle();

	public abstract boolean isFullStyle();

	public abstract boolean isSummaryStyle();

	public abstract boolean isMinimalStyle();

	public abstract boolean isNormalisedStyle();

}
