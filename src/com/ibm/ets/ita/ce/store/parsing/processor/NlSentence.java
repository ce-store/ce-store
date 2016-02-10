package com.ibm.ets.ita.ce.store.parsing.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

public class NlSentence {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private String sentenceText = "";
	private ArrayList<String> words = null;
	private boolean hasUnevenQuotes = false;

	public NlSentence(String pText) {
		this.sentenceText = pText;
		this.words = new ArrayList<String>();
	}

	public String getSentenceText() {
		return this.sentenceText;
	}

	public void setSentenceText(String pText) {
		this.sentenceText = pText;
	}

	public void appendSentenceText(char pChar) {
		this.sentenceText += pChar;
	}

	public void appendSentenceText(String pText) {
		this.sentenceText += pText;
	}

	public ArrayList<String> getWords() {
		return this.words;
	}

	public String getLastWord() {
		String result = "";

		if (this.words.size() > 0) {
			result = this.words.get(this.words.size() - 1);
		} else {
			result = "";
		}

		return result;
	}

	public void setWords(ArrayList<String> pWords) {
		this.words = pWords;
	}

	public void appendWords(ArrayList<String> pWords) {
		if (!pWords.isEmpty()) {
			if (this.words == null) {
				this.words = pWords;
			} else {
				String firstWord = pWords.get(0);
				pWords.remove(0);
				appendToLastWord(firstWord);

				if (!pWords.isEmpty()) {
					this.words.addAll(pWords);
				}
			}
		}
	}

	public boolean hasUnevenQuotes() {
		return this.hasUnevenQuotes;
	}

	public void setHasUnevenQuotes(boolean pValue) {
		this.hasUnevenQuotes = pValue;
	}

	public boolean isOnlyQuoteCharacters() {
		String temp = this.sentenceText;

		//TODO: Do I need to test for special characters here?
		temp = temp.replace(SentenceParser.CHAR_SQ, ' ');
		temp = temp.replace(SentenceParser.CHAR_DQ, ' ');		
		temp = temp.trim();

		return temp.isEmpty();
	}

	public void appendToLastWord(char pChar) {
		int wPos = this.words.size() - 1;

		if (wPos >= 0) {
			String lastWord = this.words.get(wPos);
			lastWord += pChar;
			
			this.words.remove(wPos);
			this.words.add(lastWord);
		} else {
			//TODO: Is this right? - if there is no existing word should this character be added as one?
			String tempString = "" + pChar;
			this.words.add(tempString);
		}
	}

	public void appendToLastWord(String pWord) {
		int wPos = this.words.size() - 1;

		String lastWord = this.words.get(wPos);
		lastWord += pWord;

		this.words.remove(wPos);
		this.words.add(lastWord);
	}

	@Override
	public String toString() {
		return getSentenceText();
	}

}