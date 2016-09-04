package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class Answer implements Comparable<Answer> {
	private int answerConfidence = -1;

	@Override
	public int compareTo(Answer pOtherAnswer) {
		return pOtherAnswer.getAnswerConfidence() - this.answerConfidence;
	}
	
	public int getAnswerConfidence() {
	return this.answerConfidence;
}

//	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";
//
//	private static final String CEPROP_URL = "url";
//	
//	private String key = null;
//	private String standardAnswerText = null;
//	private String chattyAnswerText = null;
//	private AnswerResultSet answerSet = null;
//	private AnswerMedia answerMedia = null;
//	private AnswerCoords answerCoords = null;
//	private String answerCode = null;
//	private Source source = null;
//	private ArrayList<ChosenWord> chosenWords = null;
//	private boolean isWhere = false;
//	private boolean isCountAnswer = false;
//
//	private Answer(String pKey, ArrayList<ChosenWord> pChosenWords, int pConf) {
//		this.key = pKey;
//		this.answerConfidence = pConf;
//		this.chosenWords = pChosenWords;
//	}
//	
//	public static Answer create(String pKey, ArrayList<ChosenWord> pChosenWords, int pConf) {
//		Answer ans = new Answer(pKey, pChosenWords, pConf);
//
//		return ans;
//	}
//
//	public static Answer create(String pKey, ArrayList<ChosenWord> pChosenWords, int pConf, Source pSource) {
//		Answer ans = new Answer(pKey, pChosenWords, pConf);
//
//		ans.source = pSource;
//
//		return ans;
//	}
//	
//	public void markAsCountAnswer() {
//		this.isCountAnswer = true;
//	}
//
//	public static Answer create(String pKey, ArrayList<ChosenWord> pChosenWords, String pAnswerText, int pConf) {
//		Answer ans = create(pKey, pChosenWords, pConf);
//
//		ans.standardAnswerText = pAnswerText;
//		
//		return ans;
//	}
//
//	public static Answer create(String pKey, ArrayList<ChosenWord> pChosenWords, String pAnswerText, int pConf, Source pSource) {
//		Answer ans = create(pKey, pChosenWords, pAnswerText, pConf);
//
//		ans.standardAnswerText = pAnswerText;
//		ans.source = pSource;
//
//		return ans;
//	}
//
//	public static Answer create(String pKey, ArrayList<ChosenWord> pChosenWords, AnswerResultSet pAnswerSet, int pConf) {
//		Answer ans = new Answer(pKey, pChosenWords, pConf);
//
//		ans.setAnswerSet(pAnswerSet);
//		
//		return ans;
//	}
//
//	public static Answer create(String pKey, ArrayList<ChosenWord> pChosenWords, AnswerMedia pAnswerMedia, int pConf, Source pSource) {
//		Answer ans = new Answer(pKey, pChosenWords, pConf);
//
//		ans.answerMedia = pAnswerMedia;
//		ans.source = pSource;
//		
//		return ans;
//	}
//
//	public static Answer create(String pKey, ArrayList<ChosenWord> pChosenWords, AnswerCoords pAnswerCoords, int pConf, Source pSource) {
//		Answer ans = new Answer(pKey, pChosenWords, pConf);
//
//		ans.answerCoords = pAnswerCoords;
//		ans.source = pSource;
//		
//		return ans;
//	}
//
//	public static Answer create(String pKey, ArrayList<ChosenWord> pChosenWords, AnswerResultSet pAnswerSet, int pConf, Source pSource) {
//		Answer ans = create(pKey, pChosenWords, pAnswerSet, pConf);
//
//		ans.source = pSource;
//		
//		return ans;
//	}
//
//	public static Answer createError(String pKey, int pConf) {
//		Answer ans = create(pKey, null, pConf);
//
//		return ans;
//	}
//
//	public String getKey() {
//		return this.key;
//	}
//
//	public String getStandardAnswerText() {
//		return this.standardAnswerText;
//	}
//
//	public boolean hasStandardAnswerText() {
//		return (this.standardAnswerText != null) && (!this.standardAnswerText.isEmpty());
//	}
//
//	public void setStandardAnswerText(String pText) {
//		this.standardAnswerText = pText;
//	}
//
//	public String getChattyAnswerText() {
//		return this.chattyAnswerText;
//	}
//	
//	public boolean hasChattyAnswerText() {
//		return (this.chattyAnswerText != null) && (!this.chattyAnswerText.isEmpty());
//	}
//
//	public void setChattyAnswerText(String pText) {
//		this.chattyAnswerText = pText;
//	}
//	
//	public AnswerResultSet getAnswerSet() {
//		return this.answerSet;
//	}
//	
//	public boolean hasAnswerSet() {
//		return this.answerSet != null;
//	}
//
//	public void setAnswerSet(AnswerResultSet pAnswerSet) {
//		this.answerSet = pAnswerSet;
//
//		if (this.answerSet.getHeaders().contains("count")) {
//			this.isCountAnswer = true;
//		}
//	}
//
//	public AnswerMedia getAnswerMedia() {
//		return this.answerMedia;
//	}
//	
//	public boolean hasAnswerMedia() {
//		return this.answerMedia != null;
//	}
//
//	public void setAnswerMedia(AnswerMedia pAnswerMedia) {
//		this.answerMedia = pAnswerMedia;
//	}
//
//	public AnswerCoords getAnswerCoords() {
//		return this.answerCoords;
//	}
//	
//	public boolean hasAnswerCoords() {
//		return this.answerCoords != null;
//	}
//
//	public void setAnswerCoords(AnswerCoords pAnswerCoords) {
//		this.answerCoords = pAnswerCoords;
//	}
//
//	public String getAnswerCode() {
//		return this.answerCode;
//	}
//	
//	public boolean hasAnswerCode() {
//		return (this.answerCode != null) && (!this.answerCode.isEmpty());
//	}
//
//	public void setAnswerCode(String pAnswerCode) {
//		this.answerCode = pAnswerCode;
//	}
//
//	public Source getSource() {
//		return this.source;
//	}
//
//	public boolean hasSource() {
//		return this.source != null;
//	}
//
//	public void addSourceUsing(CeInstance pSrcInst) {
//		String srcName = pSrcInst.getInstanceName();
//		String srcUrl =  pSrcInst.getSingleValueFromPropertyNamed(CEPROP_URL);
//
//		this.source = Source.create(srcName, srcUrl);
//	}
//
////	public String getQuestionInterpretation() {
////		return this.questionInterpretation;
////	}
//
//	public void replaceAnswerSetWithAnswerText(String pUnitName) {
//		String tgtVal = this.answerSet.getSingleValue();
//		
//		if ((pUnitName != null) && (!pUnitName.isEmpty())) {
//			tgtVal += " " + pUnitName;
//		}
//		
//		if (tgtVal != null) {
//			setStandardAnswerText(tgtVal);
//			this.answerSet = null;
//		}
//	}
//
//	@Override
//	public String toString() {
//		String result = "Answer ";
//
//		if (this.answerCode != null) {
//			result += "(code): " + this.answerCode;			
//		}
//
//		if (this.standardAnswerText != null) {
//			result += "(text): " + this.standardAnswerText;			
//		}
//
//		if (this.answerCoords != null) {
//			result += "(coords): " + this.answerCoords.toString();
//		}
//
//		if (this.answerMedia != null) {
//			result += "(media): " + this.answerMedia.toString();
//		}
//
//		if (this.answerSet != null) {
//			result += "(set): " + this.answerSet.toString();
//		}
//
//		if (this.chattyAnswerText != null) {
//			result += " [" + this.chattyAnswerText + "]";
//		}
//
//		result += " {" + this.answerConfidence + "}";			
//
//		if (this.source != null) {
//			result += ", source= " + this.source.toString();
//		} else {
//			result += ", no source";
//		}
//
//		return result;
//	}
//
//	public String computeInterpretation(ActionContext pAc, ArrayList<ProcessedWord> pQuesWords) {
//		InterpretationCalculator ic = new InterpretationCalculator(pAc, this.chosenWords, pQuesWords, this);
//		String result = ic.computeInterpretation();
//
//		createChattyAnswerUsing(result);
//
//		return result;
//	}
//
//	private void createChattyAnswerUsing(String pInterpretationText) {
//		String chattyText = pInterpretationText.toLowerCase();
//		chattyText = chattyText.replace("?", "");
//
//		if (hasStandardAnswerText() && !hasChattyAnswerText()) {
//			if (chattyText.startsWith("how many")) {
//				chattyText = chattyText.replace("how many", this.standardAnswerText);
//				setChattyAnswerText(chattyText);
//			} else if (chattyText.startsWith("who")) {
//				chattyText = chattyText.replace("who", this.standardAnswerText);
//				setChattyAnswerText(chattyText);
//			} else if (chattyText.startsWith("what")) {
//				chattyText = chattyText.replace("what", this.standardAnswerText);
//				setChattyAnswerText(chattyText);
//			} else {
//				chattyText += " is " + this.standardAnswerText;
//				setChattyAnswerText(chattyText);
//			}
//		}
//	}
//	
//	public boolean isWhereAnswer() {
//		return this.isWhere || this.hasAnswerCoords();
//	}
//	
//	public boolean isCountAnswer() {
//		return this.isCountAnswer;
//	}
//	
//	public void markAsWhere() {
//		this.isWhere = true;
//	}
//
}