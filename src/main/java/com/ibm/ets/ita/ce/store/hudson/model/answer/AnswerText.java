package com.ibm.ets.ita.ce.store.hudson.model.answer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RESTEXT;

import java.util.ArrayList;

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_CHATTEXT;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class AnswerText extends Answer {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private String answerText = null;
	private String chattyText = null;
	
	public AnswerText(String pAnswerText, String pChattyText, int pConf) {
		super(pConf);

		this.answerText = pAnswerText;
		this.chattyText = pChattyText;
	}
	
	public AnswerText(String pAnswerText, String pChattyText, int pConf, ArrayList<CeInstance> pInstList) {
		super(pConf, pInstList);

		this.answerText = pAnswerText;
		this.chattyText = pChattyText;
	}
	
	public String getAnswerText() {
		return this.answerText;
	}

	public String getChattyText() {
		return this.chattyText;
	}

	@Override
	public CeStoreJsonObject specificJson() {
		CeStoreJsonObject result = new CeStoreJsonObject();

		result.put(JSON_A_RESTEXT, this.answerText);

		if (this.chattyText != null) {
			result.put(JSON_A_CHATTEXT, this.chattyText);
		}

		return result;
	}

}
