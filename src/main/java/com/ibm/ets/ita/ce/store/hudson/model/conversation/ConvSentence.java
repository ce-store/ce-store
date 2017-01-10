package com.ibm.ets.ita.ce.store.hudson.model.conversation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONVSEN;
import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class ConvSentence extends ConvItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private ConvPhrase parentPhrase = null;
	private ArrayList<ConvClause> childClauses = null;
	private ArrayList<ConvWord> allWords = null;
	private String analysisText = null;

	public static ConvSentence createNewSentence(ActionContext pAc, String pSentenceText, ConvPhrase pParent) {
		ConvSentence result = new ConvSentence(pAc, pSentenceText, pParent);
		result.initialise();
		result.parse(pAc);

		return result;
	}

	private ConvSentence(ActionContext pAc, String pSentenceText, ConvPhrase pParent) {
		super(pAc, CON_CONVSEN, pSentenceText);

		this.parentPhrase = pParent;
		this.parentPhrase.addChildSentence(this);
	}

	@Override
	protected void initialise() {
		this.childClauses = new ArrayList<ConvClause>();
		this.allWords = new ArrayList<ConvWord>();

		initialiseDelimiterList();
	}
	
	private void initialiseDelimiterList() {
		//TODO: This should be dynamic
		this.delimiterList = new String[5];
		this.delimiterList[0] = ":";
		this.delimiterList[1] = ";";
		this.delimiterList[2] = ",";
		this.delimiterList[3] = "\\!";
		this.delimiterList[4] = "\\?";
//		this.delimiterList[x] = "\\.";
	}

	public static String getConceptName() {
		return CON_CONVSEN;
	}

	public String getSentenceText() {
		return this.itemText;
	}

	public ConvPhrase getParentPhrase() {
		return this.parentPhrase;
	}

	public ArrayList<ConvClause> getChildClauses() {
		return this.childClauses;
	}

	public void addChildClause(ConvClause pClause) {
		this.childClauses.add(pClause);
	}

	public ArrayList<ConvWord> getAllWords() {
		return this.allWords;
	}

	public void addWord(ConvWord pWord) {
		this.allWords.add(pWord);
		this.parentPhrase.addWord(pWord);
	}

	public String getAnalysisText() {
		return this.analysisText;
	}

	public void setAnalysisText(String pText) {
		this.analysisText = pText;
	}

	@Override
	public void parse(ActionContext pAc) {
		ArrayList<String> allClauseTexts = new ArrayList<String>();

		allClauseTexts.add(getSentenceText());

		for (String thisDelim : this.delimiterList) {
			allClauseTexts = splitUsing(thisDelim, allClauseTexts);
		}

		for (String clauseText : allClauseTexts) {
			ConvClause.createNewClause(pAc, clauseText, this);
		}
	}

}
