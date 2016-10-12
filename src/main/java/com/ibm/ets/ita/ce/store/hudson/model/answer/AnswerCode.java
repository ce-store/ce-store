package com.ibm.ets.ita.ce.store.hudson.model.answer;

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_CO_CODE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_CO_TEXT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RESCODE;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class AnswerCode extends Answer {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String code = null;
	private String text = null;

	public AnswerCode(String pCode, String pText, int pConf) {
		super(pConf);

		this.code = pCode;
		this.text = pText;
	}

	public String getCode() {
		return this.code;
	}

	public String getText() {
		return this.text;
	}

	@Override
	public String toString() {
		return "AnswerCode: " + this.code + " [" + this.text + "]";
	}

	@Override
	public CeStoreJsonObject specificJson() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		CeStoreJsonObject jRes = new CeStoreJsonObject();

		jRes.put(JSON_A_CO_CODE, this.code);
		jRes.put(JSON_A_CO_TEXT, this.text);

		result.put(JSON_A_RESCODE, jRes);

		return result;
	}

}
