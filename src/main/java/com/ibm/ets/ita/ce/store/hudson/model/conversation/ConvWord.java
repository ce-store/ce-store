package com.ibm.ets.ita.ce.store.hudson.model.conversation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONVWORD;
import com.ibm.ets.ita.ce.store.core.ActionContext;

public class ConvWord extends ConvItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private int wordIndex = -1;		//Relative to parent clause
	private ConvClause parentClause = null;
	private ProcessedWord processedWord = null;

	public static ConvWord createNewWord(ActionContext pAc, String pWordText, int pIndex, ConvClause pParent) {
		ConvWord result = new ConvWord(pAc, pWordText, pIndex, pParent);
		result.initialise();
		result.parse(pAc);

		return result;
	}

	private ConvWord(ActionContext pAc, String pWordText, int pIndex, ConvClause pParent) {
		super(pAc, CON_CONVWORD, pWordText);

		this.wordIndex = pIndex;
		this.parentClause = pParent;
		this.parentClause.addChildWord(this);
	}

	public static String getConceptName() {
		return CON_CONVWORD;
	}

	public String getWordText() {
		return this.itemText;
	}

	public int getWordIndex() {
		return this.wordIndex;
	}

	public ConvClause getParentClause() {
		return this.parentClause;
	}

	public ProcessedWord getProcessedWord() {
		return this.processedWord;
	}

	public void setProcessedWord(ProcessedWord pPw) {
		this.processedWord = pPw;
	}

	@Override
	protected void initialise() {
		//No special initialisation required for conversation words
	}

	@Override
	protected void parse(ActionContext pAc) {
		//Words do not need to be parsed as they are the lowest entity and cannot be decomposed
	}

	public ConvWord getNextWord() {
		return this.parentClause.getWordWithIndex(this.wordIndex + 1);
	}

	public ConvWord getPreviousWord() {
		return this.parentClause.getWordWithIndex(this.wordIndex - 1);
	}

}
