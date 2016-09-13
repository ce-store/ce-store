package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.CESRC_NAME;

public class Source {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String name = null;
	private String url = null;

	private Source(String pSrcName, String pSrcUrl) {
		this.name = pSrcName;
		this.url = pSrcUrl;
	}

	public static Source create(String pSrcName, String pSrcUrl) {
		return new Source(pSrcName, pSrcUrl);
	}
	
	public static Source ceSource() {
		return create(CESRC_NAME, null);
	}

	public String getName() {
		return this.name;
	}

	public String getUrl() {
		return this.url;
	}

	@Override
	public String toString() {
		String result = null;
		
		result = this.name + ", " + this.url;
		
		return result;
	}
}
