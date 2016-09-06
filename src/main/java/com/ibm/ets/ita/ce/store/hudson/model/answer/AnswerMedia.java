package com.ibm.ets.ita.ce.store.hudson.model.answer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class AnswerMedia {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String id = null;
	private String url = null;
	private String credit = null;

	public AnswerMedia(String pId, String pUrl, String pCredit) {
		this.id = pId;
		this.url = pUrl;
		this.credit = pCredit;
	}

	public String getId() {
		return this.id;
	}

	public String getUrl() {
		return this.url;
	}

	public String getCredit() {
		return this.credit;
	}

	@Override
	public String toString() {
		String result = null;
		
		result = this.url + ", " + this.credit + " (" + this.id + ")";
		
		return result;
	}

}