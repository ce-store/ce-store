package com.ibm.ets.ita.ce.store.conversation.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;

public class ConvText extends ConvItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_NAME = "conv text";

	private static final String CON_ASSERTION = "conv assertion";
	private static final String CON_QUESTION = "conv question";

	private static final int TYPE_ASSERTION = 1;
	private static final int TYPE_QUESTION = 2;

	private int textType = TYPE_ASSERTION;

	private ArrayList<ConvWord> allWords = null;
	private ArrayList<ConvSentence> childSentences = new ArrayList<ConvSentence>();

	private String[] qsmList = null;	//Question start markers
	private String[] qemList = null;	//Question end markers

	public static ConvText createNewText(ActionContext pAc, String pText) {
		ConvText result = new ConvText(pAc, pText);
		result.initialise();
		result.parse(pAc);

		return result;
	}

	private ConvText(ActionContext pAc, String pText) {
		super(pAc, CON_NAME, pText);
	}

	public String getConceptName() {
		String result = "";

		if (isQuestion()) {
			result = CON_QUESTION;
		} else {
			result = CON_ASSERTION;
		}

		return result;
	}

	public String getText() {
		return this.itemText;
	}

	public ArrayList<ConvSentence> getChildSentences() {
		return this.childSentences;
	}

	public void addChildSentence(ConvSentence pSentence) {
		this.childSentences.add(pSentence);
	}

	public ArrayList<ConvWord> getAllWords() {
		return this.allWords;
	}

	public void addWord(ConvWord pWord) {
		this.allWords.add(pWord);
	}

	@Override
	protected void initialise() {
		this.allWords = new ArrayList<ConvWord>();

		initialiseDelimiterList();
		initialiseQuestionStartMarkers();
		initialiseQuestionEndMarkers();
	}

	private void initialiseDelimiterList() {
		//TODO: Make this more dynamic
		this.delimiterList = new String[3];
		this.delimiterList[0] = "\\.";
		this.delimiterList[1] = "\\!";
		this.delimiterList[2] = "\\?";
	}

	private void initialiseQuestionStartMarkers() {
		//TODO: Make this more dynamic
		this.qsmList = new String[10];
		this.qsmList[0] = "what";
		this.qsmList[1] = "who";
		this.qsmList[2] = "why";
		this.qsmList[3] = "where";
		this.qsmList[4] = "which";
		this.qsmList[5] = "count";
		this.qsmList[6] = "list";
		this.qsmList[7] = "is";
		this.qsmList[8] = "summarise";
		this.qsmList[9] = "summarize";
	}

	private void initialiseQuestionEndMarkers() {
		//TODO: Make this more dynamic
		this.qemList = new String[1];
		this.qemList[0] = "?";
	}

	public boolean isAssertion() {
		return this.textType == TYPE_ASSERTION;
	}

	public boolean isQuestion() {
		return (this.textType == TYPE_QUESTION) || this.firstWordIsQuestion();
	}

	public ProcessedWord getFirstProcessedWord() {
		ProcessedWord result = null;

		if ((this.allWords != null) && (this.allWords.isEmpty())) {
			result = this.allWords.get(0).getProcessedWord();
		}

		return result;
	}

	public boolean firstWordIsQuestion() {
		boolean result = false;
		ProcessedWord fpw = getFirstProcessedWord();

		if (fpw != null) {
			result = fpw.isQuestionWord();
		}

		return result;
	}

	@Override
	protected void parse(ActionContext pAc) {
		ArrayList<String> splitText = new ArrayList<String>();

		if (!getText().isEmpty()) {
			splitText.add(getText());
		}

		//TODO: Make the splitting of sentences configurable via the agent.
		//For now it is disabled to prevent issues with sentences like "Capt. Scarlett likes apples"
//		for (String thisDelim : this.delimiterList) {
//			splitText = splitUsing(thisDelim, splitText);
//		}

		for (String thisSen : splitText) {
			ConvSentence.createNewSentence(pAc, thisSen, this);
		}

		classifyText();
	}

	private void classifyText() {
		String lcPt = getText().toLowerCase();

		this.textType = TYPE_ASSERTION;

		for (String qem : this.qemList) {
			if (lcPt.endsWith(qem)) {
				this.textType = TYPE_QUESTION;
				break;
			}
		}

		for (String qsm : this.qsmList) {
			if (lcPt.startsWith(qsm)) {
				this.textType = TYPE_QUESTION;
				break;
			}
		}
	}

}