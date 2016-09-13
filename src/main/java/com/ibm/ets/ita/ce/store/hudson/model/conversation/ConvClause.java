package com.ibm.ets.ita.ce.store.hudson.model.conversation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONVCLAUSE;
import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class ConvClause extends ConvItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ConvSentence parentSentence = null;
	private ArrayList<ConvWord> childWords = null;
	private String[] punctuationList = null;
	
	private ConvClause(ActionContext pAc, String pClauseText, ConvSentence pParent) {
		super(pAc, CON_CONVCLAUSE, pClauseText);

		this.parentSentence = pParent;
		this.parentSentence.addChildClause(this);
	}

	public static ConvClause createNewClause(ActionContext pAc, String pClauseText, ConvSentence pParent) {
		ConvClause newClause = new ConvClause(pAc, pClauseText, pParent);
		newClause.initialise();
		newClause.parse(pAc);

		return newClause;
	}

	public static String getConceptName() {
		return CON_CONVCLAUSE;
	}

	public String getClauseText() {
		return this.itemText;
	}

	public ConvSentence getParentSentence() {
		return this.parentSentence;
	}

	public ArrayList<ConvWord> getChildWords() {
		return this.childWords;
	}

	public void addChildWord(ConvWord pWord) {
		this.childWords.add(pWord);
		this.parentSentence.addWord(pWord);
	}

	public ConvWord getWordWithIndex(int pIndex) {
		for (ConvWord thisWord : this.childWords) {
			if (thisWord.getWordIndex() == pIndex) {
				return thisWord;
			}
		}

		return null;
	}

	@Override
	protected void initialise() {
		this.childWords = new ArrayList<ConvWord>();

		initialiseDelimiterList();
		initialisePunctuationList();
	}

	private void initialiseDelimiterList() {
		//TODO: Make this dynamic
		this.delimiterList = new String[2];
		this.delimiterList[0] = " ";	//Whitespace
		this.delimiterList[1] = "	";	//Tab
	}
	
	private void initialisePunctuationList() {
		//TODO: Make this dynamic
		this.punctuationList = new String[4];
		this.punctuationList[0] = ".";
		this.punctuationList[1] = ",";
		this.punctuationList[2] = ";";
		this.punctuationList[3] = ":";
	}
	
	@Override
	protected void parse(ActionContext pAc) {
		int wordCtr = 0;
		ArrayList<String> rawWords = new ArrayList<String>();
		rawWords.add(getClauseText());

		for (String thisDelim : this.delimiterList) {
			rawWords = splitUsing(thisDelim, rawWords);
		}

		ArrayList<String> finalWords = new ArrayList<String>();
		for (String thisWord : rawWords) {
			finalWords.add(removePunctuationFrom(thisWord));
		}

		for (String thisWord : finalWords) {
			ConvWord.createNewWord(pAc, thisWord, ++wordCtr, this);
		}
	}

	private String removePunctuationFrom(String pWordText) {
		String result = null;

		if (endsWithPunctuationChar(pWordText)) {
			if (pWordText.length() > 1) {
				result = pWordText.substring(0, pWordText.length() - 1);
			} else {
				result = "";
			}
		} else {
			result = pWordText;
		}

		return result;
	}

	private boolean endsWithPunctuationChar(String pWordText) {
		boolean result = false;

		for (String puncVal : this.punctuationList) {
			if (pWordText.endsWith(puncVal)) {
				result = true;
				break;
			}
		}

		return result;
	}

}
