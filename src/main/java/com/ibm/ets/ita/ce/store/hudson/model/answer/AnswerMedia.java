package com.ibm.ets.ita.ce.store.hudson.model.answer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_M_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_M_CREDIT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_M_URL;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RESMEDIA;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

public class AnswerMedia extends Answer {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private String id = null;
	private String url = null;
	private String credit = null;

	public AnswerMedia(String pId, String pUrl, String pCredit, int pConf) {
		super (pConf);

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

	@Override
	public CeStoreJsonObject specificJson() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		CeStoreJsonObject jRes = new CeStoreJsonObject();

		jRes.put(JSON_A_M_ID, this.id);
		jRes.put(JSON_A_M_URL, this.url);
		jRes.put(JSON_A_M_CREDIT, this.credit);

		result.put(JSON_A_RESMEDIA, jRes);

		return result;
	}

}
