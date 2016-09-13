package com.ibm.ets.ita.ce.store.conversation.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_BADACT;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_CARDTYPE_NOTSUPP;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_DECLINED;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_LOCCARD;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_LOGIN;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NEGATION;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOCMD;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOEXPAND;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOFURTHER;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOTAUTH;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOTHINGDONE;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOTHINGELSE;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOTUNDERSTOOD;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_REPEATED;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_SAVED;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_THANKYOU;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONFCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_GISTCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_GISTCONFCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_NLCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_TELLCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ISINREPLYTO;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteNormalParameters;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.model.CeInstance;

public class ResultOfAnalysis {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String ceText = null;
	private String gistText = null;
	private String responseCardType = null;
	private String infoMessage = null;
	private boolean isQuestionResult = false;
	private ArrayList<ResultOfAnalysis> childResults = new ArrayList<ResultOfAnalysis>();
	private TreeMap<String, CeInstance> matchedInstances = new TreeMap<String, CeInstance>();
	private ArrayList<String> referencedIds = new ArrayList<String>();

	private static ResultOfAnalysis createWithInfoMessage(String pMsg, TreeMap<String, String> pParms) {
		String msgText = substituteNormalParameters(pMsg, pParms);

		return createWithInfoMessage(msgText);
	}

	private static ResultOfAnalysis createWithInfoMessage(String pMsg) {
		ResultOfAnalysis result = new ResultOfAnalysis();

		result.responseCardType = CON_NLCARD;
		result.infoMessage = pMsg;

		return result;
	}

	public static ResultOfAnalysis createConfirmationFor(String pCeText) {
		ResultOfAnalysis result = new ResultOfAnalysis();

		result.ceText = pCeText;
		result.responseCardType = CON_CONFCARD;

		return result;
	}

	public static ResultOfAnalysis createTellFor(String pCeText) {
		ResultOfAnalysis result = new ResultOfAnalysis();

		result.ceText = pCeText;
		result.responseCardType = CON_TELLCARD;

		return result;
	}

	public static ResultOfAnalysis createWithGistAndCe(String pGistText, String pCeText) {
		ResultOfAnalysis result = new ResultOfAnalysis();

		result.gistText = pGistText;
		result.ceText = pCeText;

		return result;
	}

	public static ResultOfAnalysis createQuestionResponseWithGistAndCe(String pGistText, String pCeText) {
		ResultOfAnalysis result = new ResultOfAnalysis();

		result.gistText = pGistText;
		result.ceText = pCeText;
		result.isQuestionResult = true;
		result.responseCardType = CON_GISTCARD;

		return result;
	}

	public static ResultOfAnalysis createQuestionResponseWithGistOnly(String pGistText) {
		ResultOfAnalysis result = new ResultOfAnalysis();

		result.gistText = pGistText;
		result.ceText = null;
		result.isQuestionResult = true;
		result.responseCardType = CON_GISTCARD;

		return result;
	}

	public boolean isQuestionResult() {
		return this.isQuestionResult;
	}

	public TreeMap<String, CeInstance> getMatchedInstances() {
		return this.matchedInstances;
	}

	public void addMatchedInstance(CeInstance pInst) {
		this.matchedInstances.put(pInst.getInstanceName(), pInst);
	}

	public ArrayList<String> getReferencedIds() {
		return this.referencedIds;
	}

	public void addReferencedId(String pId) {
		this.referencedIds.add(pId);
	}

	public static ResultOfAnalysis msgThankyou() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_THANKYOU);
	}

	public static ResultOfAnalysis msgThankyouForCe() {
		ResultOfAnalysis result = ResultOfAnalysis.createWithInfoMessage(MSG_SAVED);

		return result;
	}

	public static ResultOfAnalysis msgDeclined() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_DECLINED);
	}

	public static ResultOfAnalysis msgUserNotAuthorised(String pUserName) {
		ResultOfAnalysis result = null;

		if (pUserName == null) {
			result = ResultOfAnalysis.createWithInfoMessage(MSG_LOGIN);
		} else {
			TreeMap<String, String> parms = new TreeMap<String, String>();
			parms.put("%01", pUserName);

			result = ResultOfAnalysis.createWithInfoMessage(MSG_NOTAUTH, parms);
		}

		return result;
	}

	public static ResultOfAnalysis msgActNotAuthorised(String pUserName, String pActName) {
		TreeMap<String, String> parms = new TreeMap<String, String>();
		parms.put("%01", pActName);
		parms.put("%02", pUserName);

		return ResultOfAnalysis.createWithInfoMessage(MSG_BADACT, parms);
	}

	public static ResultOfAnalysis msgNotUnderstood() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_NOTUNDERSTOOD);
	}

	public static ResultOfAnalysis msgEmptyText() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_NOTHINGDONE);
	}

	public static ResultOfAnalysis msgBeenSaidAlready() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_REPEATED);
	}

	public static ResultOfAnalysis msgCantHandleNegations() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_NEGATION);
	}

	public static ResultOfAnalysis msgNothingElseToSay() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_NOTHINGELSE);
	}

	public static ResultOfAnalysis msgNoFurtherExplanation() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_NOFURTHER);
	}

	public static ResultOfAnalysis msgExpandNotSpecified() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_NOEXPAND);
	}

	public static ResultOfAnalysis msgCommandHandlingNotYetImplemented() {
		return ResultOfAnalysis.createWithInfoMessage(MSG_NOCMD);
	}

	public static ResultOfAnalysis msgUnsupportedCardType(CeInstance pConvInst) {
		TreeMap<String, String> parms = new TreeMap<String, String>();
		parms.put("%01", pConvInst.getInstanceName());
		parms.put("%02", pConvInst.getFirstLeafConceptName());

		return ResultOfAnalysis.createWithInfoMessage(MSG_CARDTYPE_NOTSUPP, parms);
	}

	public static ResultOfAnalysis msgOriginalCardNotFound(CeInstance pConvInst) {
		TreeMap<String, String> parms = new TreeMap<String, String>();
		parms.put("%01", pConvInst.getSingleValueFromPropertyNamed(PROP_ISINREPLYTO));
		parms.put("%02", PROP_ISINREPLYTO);
		parms.put("%03", pConvInst.getInstanceName());

		return ResultOfAnalysis.createWithInfoMessage(MSG_LOCCARD, parms);
	}

	public String getCeText() {
		return this.ceText;
	}

	public boolean hasCeText() {
		return (this.ceText != null) && (!this.ceText.isEmpty());
	}

	private void appendCeTextFrom(ResultOfAnalysis pRoa) {
		if (pRoa.hasCeText()) {
			if (this.hasCeText()) {
				this.ceText += pRoa.getCeText();
			} else {
				// No existing CE text
				this.ceText = pRoa.getCeText();
			}
		}
	}

	public String getGistText() {
		return this.gistText;
	}

	public boolean hasGistText() {
		return (this.gistText != null) && (!this.gistText.isEmpty());
	}

	private void appendGistTextFrom(ResultOfAnalysis pRoa) {
		if (pRoa.hasGistText()) {
			if (this.hasGistText()) {
				this.gistText += pRoa.getGistText();
			} else {
				// No existing GIST text
				this.gistText = pRoa.getGistText();
			}
		}
	}

	private void appendMatchedInstancesFrom(ResultOfAnalysis pRoa) {
		for (CeInstance thisInst : pRoa.getMatchedInstances().values()) {
			addMatchedInstance(thisInst);
		}
	}

	private void appendReferencedIdsFrom(ResultOfAnalysis pRoa) {
		for (String thisId : pRoa.getReferencedIds()) {
			addReferencedId(thisId);
		}
	}

	public String getResponseCardType() {
		return this.responseCardType;
	}

	public void markAsGistConfirmResponse() {
		this.responseCardType = CON_GISTCONFCARD;
	}

	public void markAsConfirmResponse() {
		this.responseCardType = CON_CONFCARD;
	}

	public String getInfoMessage() {
		return this.infoMessage;
	}

	public boolean isInfoMessage() {
		return this.infoMessage != null;
	}

	public boolean hasResponseCardType() {
		return (this.responseCardType != null) && (!this.responseCardType.isEmpty());
	}

	public boolean isConfirmCard() {
		boolean result = false;

		if (this.responseCardType != null) {
			result = this.responseCardType.equals(CON_CONFCARD);
		}

		return result;
	}

	public boolean isTellCard() {
		boolean result = false;

		if (this.responseCardType != null) {
			result = this.responseCardType.equals(CON_TELLCARD);
		}

		return result;
	}

	public ArrayList<ResultOfAnalysis> getChildResults() {
		return this.childResults;
	}

	public void incorporate(ResultOfAnalysis pChildRoa) {
		this.childResults.add(pChildRoa);

		transferResponseCardTypeFrom(pChildRoa);
		appendCeTextFrom(pChildRoa);
		appendGistTextFrom(pChildRoa);
		appendMatchedInstancesFrom(pChildRoa);
		this.isQuestionResult = pChildRoa.isQuestionResult();
		appendReferencedIdsFrom(pChildRoa);
	}

	private void transferResponseCardTypeFrom(ResultOfAnalysis pChildRoa) {
		if (pChildRoa.hasResponseCardType()) {
			// TODO: Add test and warning for mismatch here
			this.responseCardType = pChildRoa.getResponseCardType();
		}
	}

	public String calculatePrimaryContent() {
		String result = null;

		if (!isInfoMessage()) {
			if (hasGistText()) {
				// For GIST results the primary content is the GIST text
				result = getGistText();
			} else {
				// For non-GIST results the primary content is the CE text
				result = getCeText();
			}
		} else {
			// For info messages the primary content is the message text
			result = getInfoMessage();
		}

		return result;
	}

	public String calculateSecondaryContent() {
		// Only GIST results have secondary content (the CE text that derived
		// the GIST)
		String result = null;

		if (hasGistText()) {
			result = getCeText();
		}

		return result;
	}

	@Override
	public String toString() {
		String result = "";
		String sepChar = "";

		if (!isInfoMessage()) {
			if (hasCeText()) {
				result += sepChar + "ceText=" + this.ceText;
				sepChar = "\n";
			}

			if (hasGistText()) {
				result += sepChar + "gistText=" + this.gistText;
				sepChar = "\n";
			}
		} else {
			result = "Info msg: " + this.infoMessage;
		}

		return result;
	}

}
