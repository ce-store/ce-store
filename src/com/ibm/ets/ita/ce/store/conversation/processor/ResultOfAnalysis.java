package com.ibm.ets.ita.ce.store.conversation.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.model.CeInstance;

public class ResultOfAnalysis {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_NLCARD = "NL card";
	private static final String CON_CONFCARD = "confirm card";
	private static final String CON_GISTCONFCARD = "gist-confirm card";
	private static final String CON_GISTCARD = "gist card";
	private static final String CON_TELLCARD = "tell card";
	private static final String PROP_IRT = "is in reply to";

	private String ceText = null;
	private String gistText = null;
	private long scoreVal = 0;
	private String scoreExplanation = "";
	private String scoreType = "";
	private String responseCardType = null;
	private String infoMessage = null;
	private boolean isQuestionResult = false;
	private ArrayList<ResultOfAnalysis> childResults = new ArrayList<ResultOfAnalysis>();
	private TreeMap<String, CeInstance> matchedInstances = new TreeMap<String, CeInstance>();
	private ArrayList<String> referencedIds = new ArrayList<String>();

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
		return ResultOfAnalysis.createWithInfoMessage("Thank you for your message");
	}

	public static ResultOfAnalysis msgThankyouForCe(CeInstance pScoreInst) {
		ResultOfAnalysis result =  ResultOfAnalysis.createWithInfoMessage("I have saved that to the knowledge base");
		
		if (pScoreInst != null) {
			long scoreVal = new Long(pScoreInst.getSingleValueFromPropertyNamed("score value")).longValue();
			String scoreExp = pScoreInst.getSingleValueFromPropertyNamed("score explanation");

			result.addScoreDetails(scoreVal, scoreExp, "actual");
		}
		
		return result;
	}

	public static ResultOfAnalysis msgTempHvt() {
		return ResultOfAnalysis.createWithInfoMessage("The vehicle with plate ABC 123 is linked to a High Value Target");
	}

	public static ResultOfAnalysis msgLexicallyProcessed() {
		return ResultOfAnalysis.createWithInfoMessage("Your sentence has been lexically processed");
	}

	public static ResultOfAnalysis msgDeclined() {
		return ResultOfAnalysis.createWithInfoMessage("I'm sorry but I can't help you.  You don't have the right authorisation.");
	}

	public static ResultOfAnalysis msgUserNotAuthorised(String pUserName) {
		ResultOfAnalysis result = null;

		if (pUserName == null) {
			result = ResultOfAnalysis.createWithInfoMessage("You must log in before you can interact with the system");
		} else {
			result = ResultOfAnalysis.createWithInfoMessage("I'm sorry but the user named '" + pUserName + "' is not authorised to interact with the system.  Please log in as an authorised user.");
		}

		return result;
	}

	public static ResultOfAnalysis msgActNotAuthorised(String pUserName, String pActName) {
		return ResultOfAnalysis.createWithInfoMessage("I cannot respond to that request.\nThe '" + pActName + "' speech act is not authorised for user '" + pUserName + "'");
	}

	public static ResultOfAnalysis msgNotUnderstood() {
		return ResultOfAnalysis.createWithInfoMessage("I wasn't able to understand any of that, sorry.");
	}

	public static ResultOfAnalysis msgEmptyText() {
		return ResultOfAnalysis.createWithInfoMessage("I didn't do anything because I don't think you said anything");
	}

	public static ResultOfAnalysis msgBeenSaidAlready() {
		return ResultOfAnalysis.createWithInfoMessage("You've said that already!");
	}

	public static ResultOfAnalysis msgCantHandleNegations() {
		return ResultOfAnalysis.createWithInfoMessage("I'm not able to handle negative statements such as 'no', 'not' and 'doesn't'");
	}

	public static ResultOfAnalysis msgNothingElseToSay() {
		return ResultOfAnalysis.createWithInfoMessage("Sorry, I don't have anything else to tell you");
	}

	public static ResultOfAnalysis msgNoFurtherExplanation() {
		return ResultOfAnalysis.createWithInfoMessage("Sorry, I don't have any further explanation for that");
	}

	public static ResultOfAnalysis msgExpandNotSpecified() {
		return ResultOfAnalysis.createWithInfoMessage("You didn't tell me what to expand on");
	}

	public static ResultOfAnalysis msgCommandHandlingNotYetImplemented() {
		return ResultOfAnalysis.createWithInfoMessage("Command handling is not yet implemented");
	}

	public static ResultOfAnalysis msgUnsupportedCardType(CeInstance pConvInst) {
		String msgText = "Processing for this card type is not yet implemented: " + pConvInst.getInstanceName() + " (" + pConvInst.getFirstLeafConceptName() + ")";

		return ResultOfAnalysis.createWithInfoMessage(msgText);
	}

	public static ResultOfAnalysis msgOriginalCardNotFound(CeInstance pConvInst) {
		String msgText = "Error: Unable to locate card '" + pConvInst.getSingleValueFromPropertyNamed(PROP_IRT) + "' (" + PROP_IRT + ") when processing confirm card '" + pConvInst.getInstanceName() + "'";

		return ResultOfAnalysis.createWithInfoMessage(msgText);
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
				//No existing CE text
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
				//No existing GIST text
				this.gistText = pRoa.getGistText();
			}
		}
	}

	public long getScoreVal() {
		return this.scoreVal;
	}

	private void addScoreValFrom(ResultOfAnalysis pRoa) {
		this.scoreVal += pRoa.getScoreVal();
	}

	public String getScoreExplanation() {
		return this.scoreExplanation;
	}

	public boolean hasScoreExplanation() {
		return (this.scoreExplanation != null) && (!this.scoreExplanation.isEmpty());
	}

	private void appendScoreExplanationFrom(ResultOfAnalysis pRoa) {
		if (pRoa.hasScoreExplanation()) {
			this.scoreExplanation += pRoa.getScoreExplanation();
		}
	}

	public String getScoreType() {
		return this.scoreType;
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

	public void addScoreDetails(long pScoreVal, String pScoreExp, String pScoreType) {
		this.scoreVal = pScoreVal;
		this.scoreExplanation = pScoreExp;
		this.scoreType = pScoreType;
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
		addScoreValFrom(pChildRoa);
		appendScoreExplanationFrom(pChildRoa);
		appendMatchedInstancesFrom(pChildRoa);
		this.scoreType = pChildRoa.getScoreType();
		this.isQuestionResult = pChildRoa.isQuestionResult();
		appendReferencedIdsFrom(pChildRoa);
	}
	
	private void transferResponseCardTypeFrom(ResultOfAnalysis pChildRoa) {
		if (pChildRoa.hasResponseCardType()) {
			//TODO: Add test and warning for mismatch here
			this.responseCardType = pChildRoa.getResponseCardType();
		}
	}

	public String calculatePrimaryContent() {
		String result = null;

		if (!isInfoMessage()) {
			if (hasGistText()) {
				//For GIST results the primary content is the GIST text
				 result = getGistText();
			} else {
				//For non-GIST results the primary content is the CE text
				result = getCeText();
			}
		} else {
			//For info messages the primary content is the message text
			result = getInfoMessage();
		}

		return result;
	}

	public String calculateSecondaryContent() {
		//Only GIST results have secondary content (the CE text that derived the GIST)
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
	
			if (this.scoreVal != 0) {
				result += sepChar + "score=" + new Long(this.scoreVal).toString();
				result += " (" + this.scoreExplanation + ")";
				sepChar = "\n";
			}
		} else {
			result = "Info msg: " + this.infoMessage;
		}

		return result;
	}

}