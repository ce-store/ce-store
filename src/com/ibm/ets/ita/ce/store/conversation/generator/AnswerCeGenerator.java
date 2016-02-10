package com.ibm.ets.ita.ce.store.conversation.generator;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.generation.CeGenerator;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

public class AnswerCeGenerator extends CeGenerator {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public AnswerCeGenerator(ActionContext pAc, StringBuilder pSb) {
		super(pAc, pSb);
	}

	public void generateCeForAnswer(String pAnswerId, String pDomCon, String pDomName, String pRangeCon, String pRangeName, String pFullPropName, String pUserName) {
		ceDeclarationLong("an", "answer", pAnswerId);
		ceAddFnProperty("subject", pDomCon, pDomName);
		ceAddFnProperty("predicate", "relation concept", pFullPropName);
		ceAddFnProperty("object", pRangeCon, pRangeName);
		ceAddVsProperty("is given by", "CE user", pUserName);
		ceEndSentence();
	}

	public void generateCeLinkingQuestionToAnswer(String pQuestionId, String pAnswerId) {
		ceDeclarationShort("question", pQuestionId);
		ceAddFnProperty("answer", "answer", pAnswerId);
		ceEndSentence();
	}

}