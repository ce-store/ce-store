package com.ibm.ets.ita.ce.store.hudson.model.conversation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONVPHRASE;
import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class ConvPhrase extends ConvItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<ConvWord> allWords = null;
	private ArrayList<ConvSentence> childSentences = new ArrayList<ConvSentence>();
	
	public static ConvPhrase createNewPhrase(ActionContext pAc, String pPhraseText) {
		ConvPhrase result = new ConvPhrase(pAc, pPhraseText);
		result.initialise();
		result.parse(pAc);

		return result;
	}

	public boolean isAssertion() {
		//TODO: Resurrect this
		return false;
	}

	public boolean isQuestion() {
		//TODO: Resurrect this
		return true;
	}

	private ConvPhrase(ActionContext pAc, String pPhraseText) {
		super(pAc, CON_CONVPHRASE, pPhraseText);
	}

	public String getPhraseText() {
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
	}

	private void initialiseDelimiterList() {
		//TODO: Make this more dynamic
		this.delimiterList = new String[3];
		this.delimiterList[0] = "\\.";
		this.delimiterList[1] = "\\!";
		this.delimiterList[2] = "\\?";
	}

	public ProcessedWord getFirstProcessedWord() {
		ProcessedWord result = null;
		
		if ((this.allWords != null) && (this.allWords.isEmpty())) {
			result = this.allWords.get(0).getProcessedWord();
		}

		return result;
	}

	@Override
	protected void parse(ActionContext pAc) {
		ArrayList<String> splitPhrases = new ArrayList<String>();

		if (!getPhraseText().isEmpty()) {
			splitPhrases.add(getPhraseText());
		}

		//TODO: Make the splitting of phrases configurable via the agent.
		//For now it is disabled to prevent issues with phrases like "Capt. Scarlett likes apples"
		for (String thisSen : splitPhrases) {
			ConvSentence.createNewSentence(pAc, thisSen, this);
		}
	}

}
