package com.ibm.ets.ita.ce.store.parsing.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.ActionContext;

public class SentenceParserNl extends SentenceParser {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	protected ProcessorNl proc = null;

	private boolean trailingSentence = false;
	private boolean lastCharTokenDelim = false;
	private boolean lastCharSentenceDelim = false;
	private boolean lookingForSentenceSeparator = false;
	private boolean ignoreCrs = true;
	private boolean trackSingleQuotes = true;
	private boolean trackDoubleQuotes = true;
	private boolean trackSpecialQuotes = true;

	private boolean inQuotes = false;
	private char lastChar = 0;
	private char[] sentenceDelimiters = null;
	private char[] specialQuotes = null;
	private HashSet<String> acronymList = null;

	private NlSentence tentativeSentence = null;

	public SentenceParserNl(ActionContext pAc, ProcessorNl pProc, ArrayList<String> pAcronymList, char[] pSenDelims, char[] pSpecialQuotes, boolean pIgnoreCrs, boolean pTrackSingle, boolean pTrackDouble, boolean pTrackSpecial) {		
		super(pAc);

		this.proc = pProc;
		this.sentenceDelimiters = pSenDelims;
		this.specialQuotes = pSpecialQuotes;
		this.ignoreCrs = pIgnoreCrs;
		this.trackSingleQuotes = pTrackSingle;
		this.trackDoubleQuotes = pTrackDouble;
		this.trackSpecialQuotes = pTrackSpecial;

		initialiseAcronymList(pAcronymList);
	}

	private void initialiseAcronymList(ArrayList<String> pAcronymList) {
		//TODO: Efficiency - pass in a HashSet instead of converting it
		this.acronymList = new HashSet<String>(pAcronymList);		
	}

	private ProcessorNl getProcessor() {
		return this.proc;
	}

	@Override
	protected void processCharacterExtra() {
		if (this.trackSpecialQuotes) {
			//Handle any special quotes
			for (char thisChar : this.specialQuotes) {
				if (thisChar == this.currentChar) {
					handleSpecialQuote();
				}
			}
		}

		//Handle any sentence delimiters
		for (char thisChar : this.sentenceDelimiters) {
			if (thisChar == this.currentChar) {
				handleDot();
			}
		}
	}

	@Override
	protected void recordLastCharExtra() {
		this.lastChar = this.currentChar;
		this.lastCharTokenDelim = charIsTokenDelimiterIncludingInsideQuotes();
		this.lastCharSentenceDelim = charIsSentenceDelimiterIncludingInsideQuotes();
	}

	@Override
	protected void saveSentenceExtra() {
		NlSentence targetSentence = null;

		if (this.tentativeSentence == null) {
			targetSentence = new NlSentence(this.thisSentence.toString());
			targetSentence.setWords(this.tokens);
		} else {
			targetSentence = this.tentativeSentence;
			targetSentence.appendSentenceText(this.thisSentence.toString());
			targetSentence.appendWords(this.tokens);
		}

		//Tentatively save this sentence.  This allows the next token to be processed and if it is a delimiter then
		//the sentence will be really saved, otherwise it will be concatenated to as the dot is inside an acronym or
		//number
		tentativeSaveOfSentence(targetSentence);

		if (this.trailingSentence) {
			//The tentative sentence can be saved (and cleared) - Notify the processor of this new sentence
			getProcessor().processNlSentence(this.tentativeSentence);
			this.tentativeSentence = null;
		}
	}

	private void tentativeSaveOfSentence(NlSentence pSen) {
		this.tentativeSentence = pSen;
		this.lookingForSentenceSeparator = true;
	}

	@Override
	protected void saveTokenExtra(String pToken) {
		//No special processing required
	}

	@Override
	protected void recordChar() {
		checkForTentativeSentenceCompletion();

		//Only ignore this character if it is a CR and the ignoreCRs flag is set
		boolean ignoreThisChar = this.ignoreCrs && (this.currentChar == CHAR_CR);

		if (!ignoreThisChar) {
			if (charIsTokenDelimiter()) {
				if ((this.thisSentence.length() == 0) && (charIsBlankCharacter())) {
					//Do not add if the sentence is empty and this is a blank character (the equivalent of left trimming)
				} else {
					addCharToSentenceOnly();
				}
			} else {
				addCharToTokenAndSentence();
			}
		}
	}

	private void checkForTentativeSentenceCompletion() {
		if (this.lookingForSentenceSeparator) {
			if (this.tentativeSentence != null) {
				if (charIsTokenDelimiter()) {
					if (!lastTokenIsAcronym()) {
						//This test added (to handle ellipses etc) by DB 07/09/2012
						if ((this.lastCharSentenceDelim) && charIsSentenceDelimiter()) {
							//The last and current characters are sentence delimiters, so carry on
						} else {
							//This is a token delimiter so the tentative sentence can be saved (and cleared) - Notify the processor of this new sentence
							getProcessor().processNlSentence(this.tentativeSentence);
							this.tentativeSentence = null;
						}
					} else {
						//Otherwise carry on and keep the sentence as tentative since the last token was an acronym
						this.tentativeSentence.appendToLastWord(this.lastChar);
						this.tentativeSentence.appendSentenceText(this.currentChar);
					}
				} else {
					//Otherwise carry on and keep the sentence as tentative since it was not followed by a token delimiter
					this.tentativeSentence.appendToLastWord(this.lastChar);
				}
			}
		}

		this.lookingForSentenceSeparator = false;
	}

	private boolean lastTokenIsAcronym() {
		String lastToken = this.tentativeSentence.getLastWord() + this.lastChar;
		
		return this.acronymList.contains(lastToken);
	}

	@Override
	protected void handleDot() {
		if (isInClearText()) {
			saveToken();
			saveSentence();
		}
	}

	@Override
	protected void handleSpace() {
		//If this is not inside a quoted string or comment it may be a token
		if (isInClearText()) {
			saveToken();
		}
	}

	@Override
	protected void handleComma() {
		//If this is not inside a quoted string or comment it may be a token
		if (isInClearText()) {
			saveToken();
		}
	}

	@Override
	protected void handleOpenBracket() {
		//If this is not inside a quoted string or comment it may be a token
		if (isInClearText()) {
			saveToken();
		}
	}

	@Override
	protected void handleCloseBracket() {
		//If this is not inside a quoted string or comment it may be a token
		if (isInClearText()) {
			saveToken();
		}
	}

	@Override
	protected void handleOpenParenthesis() {
		//If this is not inside a quoted string or comment it may be a token
		if (isInClearText()) {
			saveToken();
		}
	}

	@Override
	protected void handleCloseParenthesis() {
		//If this is not inside a quoted string or comment it may be a token
		if (isInClearText()) {
			saveToken();
		}
	}

	@Override
	protected void handleTab() {
		//Treat the same as a space
		handleSpace();
	}

	@Override
	protected void handleSingleQuote() {
		//Subclassed - Need to account for special quotes and to look for apostrophes

		if (this.trackSingleQuotes) {
			boolean ignoreThisQuote = false;

			if (!this.inSingleQuotes) {
				if (!this.lastCharTokenDelim) {
					//We are not in single quotes and the last character was not a word delimiter...
					//This is therefore probably an apostrophe and should be ignored
					ignoreThisQuote = true;
				}
			}

			if (!ignoreThisQuote) {
				this.inQuotes = !this.inQuotes;
			}

			//If no longer in single quotes this indicates the end of a sentence if the last char was a sentence delimiter
			if (!ignoreThisQuote && !this.inSingleQuotes) {
				testForQuotedEndOfSentence();
			}
		}
	}

	@Override
	protected void handleDoubleQuote() {
		if (this.trackDoubleQuotes) {
			this.inQuotes = !this.inQuotes;

			//If no longer in double quotes this indicates the end of a sentence if the last char was a sentence delimiter
			if (!this.inQuotes) {
				testForQuotedEndOfSentence();
			}
		}
	}

	protected void handleSpecialQuote() {
		if (this.trackSpecialQuotes) {
			this.inQuotes = !this.inQuotes;

			//If no longer in special quotes this indicates the end of a sentence if the last char was a sentence delimiter
			if (!this.inQuotes) {
				testForQuotedEndOfSentence();
			}
		}
	}

	private void testForQuotedEndOfSentence() {
		if (this.lastCharSentenceDelim) {
			saveToken();
			saveSentence();
		}
	}

	@Override
	protected void handleNlOrCr() {
		if (isInClearText()) {
			saveToken();
		}
	}

	@Override
	protected void testForSpecificThings() {
		testForOpenQuotes();
	}

	protected void testForOpenQuotes() {
		//Instead of reporting an error, instead mark the sentence as having uneven quotes
		if (this.inQuotes) {
			getProcessor().recordUnevenQuotes();
		}
	}

	@Override
	protected void testForOpenSingleQuotes() {
		//Do nothing - quote management is merged for NL sentences
	}

	@Override
	protected void testForOpenDoubleQuotes() {
		//Do nothing - quote management is merged for NL sentences
	}

	@Override
	protected void testForTrailingText() {
		//If there are any extra characters report them as an invalid fragment
		if (this.thisSentence.length() > 0) {
			this.trailingSentence = true;
			saveToken();
			saveSentence();
		} else {
			//The tentative sentence must be saved (and cleared) - Notify the processor of this new sentence
			getProcessor().processNlSentence(this.tentativeSentence);
			this.tentativeSentence = null;
		}
	}

	@Override
	protected boolean isInClearText() {
		//Modified to account for special quotes
		return !this.inQuotes;
	}

	protected boolean charIsTokenDelimiter() {
		boolean result = false;

		if (isInClearText()) {
			result = charIsSentenceDelimiter() || charIsTokenDelimiterIncludingInsideQuotes();
		}

		return result;
	}

	protected boolean charIsSentenceDelimiter() {
		boolean result = false;

		if (isInClearText()) {
			result = charIsSentenceDelimiterIncludingInsideQuotes();
		}

		return result;
	}

	protected boolean charIsTokenDelimiterIncludingInsideQuotes() {
		boolean result = false;

		result = charIsSentenceDelimiter() || charIsBlankCharacter();

		return result;
	}

	protected boolean charIsBlankCharacter() {
		return (this.currentChar == CHAR_SPACE) ||
				(this.currentChar == CHAR_TAB) ||
				(this.currentChar == CHAR_NL) ||
				(this.currentChar == CHAR_CR);
	}

	protected boolean charIsSentenceDelimiterIncludingInsideQuotes() {
		boolean specialDelimMatch = false;
		boolean result = false;

		for (char thisChar : this.sentenceDelimiters) {
			if (thisChar == this.currentChar) {
				specialDelimMatch = true;
			}
		}

		result = specialDelimMatch || (this.currentChar == CHAR_DOT);

		return result;
	}

}