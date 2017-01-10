package com.ibm.ets.ita.ce.store.parsing.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.ANNO_TOKEN_NOTE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_BOM;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_CLBR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_CLPAR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_COMMA;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_CR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_DOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_NL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_OPBR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_OPPAR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_SPACE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_TAB;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_DASH;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COLON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;

//TODO: Add test (and warning) for non-standard acsii character outside of CE sentence

public class SentenceParserCe extends SentenceParser {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private ProcessorCe proc = null;

	private boolean lastCharDash = false;
	private boolean lastCharNlOrCr = false;
	private boolean inComment = false;
	private boolean firstTokenEndsWithColon = false;

	public SentenceParserCe(ActionContext pAc, ProcessorCe pProc) {		
		super(pAc);

		this.proc = pProc;
	}

	private ProcessorCe getProcessor() {
		return this.proc;
	}

	private boolean inAnnotationSentence() {
		return this.firstTokenEndsWithColon;
	}

	private boolean isNoteAnnotation() {
		boolean result = false;

		if (this.firstToken != null) {
			result = this.firstToken.equals(ANNO_TOKEN_NOTE);
		}

		return result;
	}

	@Override
	protected void processCharacterExtra() {
		//Test for comment characters
		if (this.currentChar == CHAR_DASH) {
			handleDash();
		}
	}

	@Override
	protected void saveTokenExtra(String pToken) {
		if (this.firstToken == null) {
			this.firstToken = pToken;
			this.firstTokenEndsWithColon = this.firstToken.endsWith(TOKEN_COLON);
		}
	}

	@Override
	protected void saveSentenceExtra() {
		BuilderSentence thisCeSentence = BuilderSentence.createForSentenceText(this.ac, this.thisSentence.toString(), this.tokens);

		if (thisCeSentence != null) {
			thisCeSentence.categoriseSentence();
			resetFlags();

			//Do the other sentence related processes
			if (isValidatingOnly()) {
				getProcessor().validateCeSentence(thisCeSentence);
			} else {
				getProcessor().processCeSentence(thisCeSentence);
			}
		} else {
			if (!this.ac.isValidatingOnly()) {
				reportParsingError("Unable to continue sentence processing due to null BuilderSentence");
			}
			getProcessor().addToInvalidCount();
		}
	}

	@Override
	protected void recordLastCharExtra() {
		//Record whether this character is a dash
		this.lastCharDash = (this.currentChar == CHAR_DASH);

		//Record whether this character is a newline
		if ((this.currentChar != CHAR_CR) && (this.currentChar != CHAR_SPACE) && (this.currentChar != CHAR_TAB)) {
			this.lastCharNlOrCr = (this.currentChar == CHAR_NL);
		}
	}

	@Override
	protected void recordChar() {
		//CR characters are always ignored
		if (this.currentChar != CHAR_CR) {
			if (this.inComment) {
				addCharToComment();
			} else {
				if (charIsTokenDelimiter()) {
					if (inAnnotationSentence()) {
						saveToken();
						saveSentence();
					} else {
						//A token delimiter, so add to sentence only
						if (this.thisSentence.length() == 0) {
							//The sentence is empty so only add if not a standard delimiter
							if (!isClearDelimiter()) {
								addCharToSentenceOnly();
							}
						} else {
							//Only automatically add if the sentence is not empty
							addCharToSentenceOnly();
						}
					}
				} else {
					addCharToTokenAndSentence();
				}
			}
		}
	}

	@Override
	protected boolean isInClearText() {
		return !this.inSingleQuotes && !this.inDoubleQuotes && !this.inComment;
	}

	@Override
	protected void handleDot() {
		if (isInClearText()) {
			// Dot is not a sentence delimiter for annotation sentences
			if (!inAnnotationSentence()) {
				saveToken();
				saveSentence();
			}
		}
	}

	@Override
	protected void handleNlOrCr() {
		if (this.inComment) {
			//Nl or Cr is a termination character for a comment
			saveComment();
		} else {
			if (!inAnnotationSentence()) {
				if (isInClearText()) {
					saveToken();
				}
			}
		}
	}

	@Override
	protected void handleSpace() {
		if (!inAnnotationSentence()) {
			//If this is not inside a quoted string or comment it may be a token
			if (isInClearText()) {
				saveToken();
			}
		}
	}

	@Override
	protected void handleComma() {
		processSpecialToken();
	}

	@Override
	protected void handleOpenBracket() {
		processSpecialToken();
	}

	@Override
	protected void handleCloseBracket() {
		processSpecialToken();
	}

	@Override
	protected void handleOpenParenthesis() {
		processSpecialToken();
	}

	@Override
	protected void handleCloseParenthesis() {
		processSpecialToken();
	}

	@Override
	protected void handleTab() {
		if (!inAnnotationSentence()) {
			//Treat the same as a space
			handleSpace();
		}
	}

	protected void handleDash() {
		if (!inAnnotationSentence()) {
			//If the last char was a dash then this is a comment
			if (isInClearText()) {
				this.inComment = this.lastCharDash;
				if (this.inComment) {
					recordCommentStart();
				}
			}
		}
	}

	@Override
	protected void handleSingleQuote() {
		if (!inAnnotationSentence()) {
			if ((!this.lastCharBackslash) && (!this.inComment) && (!this.inDoubleQuotes)) {
				this.inSingleQuotes = !this.inSingleQuotes;
			}
		}
	}

	@Override
	protected void handleDoubleQuote() {
		if (!inAnnotationSentence()) {
			if ((!this.lastCharBackslash) && (!this.inComment) && (!this.inSingleQuotes)) {
				this.inDoubleQuotes = !this.inDoubleQuotes;
			}
		}
	}

	@Override
	protected void testForSpecificThings() {
		//No specific tests needed
	}

	private void processSpecialToken() {
		if (!inAnnotationSentence()) {
			//If this is not inside a quoted string or comment it should be processed
			if (isInClearText()) {
				//A special token separator ends the previous token, but must be treated
				//as a separate token in it's own right

				saveToken();
				addCharToTokenOnly();
				saveToken();
			}
		}
	}

	@Override
	protected void testForTrailingText() {
		//If there are any extra characters report them as an invalid fragment (unless it's an annotation sentence)
		if (this.thisSentence.length() > 0) {
			if (inAnnotationSentence()) {
				//Trailing text for an annotation sentence simply means that it needs to be saved
				saveToken();
				saveSentence();
			} else {
				String trimmedText = this.thisSentence.toString().replace(CHAR_NL, ' ').replace(CHAR_CR, ' ').trim();

				if (!trimmedText.isEmpty()) {
					//Trailing text outside of an annotation sentence is an error
					reportParsingError("Trailing sentence fragment detected: '" + trimmedText + "'");
				}
			}
		}
	}

	@Override
	protected void testForOpenSingleQuotes() {
		if (this.inSingleQuotes) {
			reportParsingError("Unclosed single quote string detected, for sentence: " + this.thisSentence);
		}
	}

	@Override
	protected void testForOpenDoubleQuotes() {
		if (this.inDoubleQuotes) {
			reportParsingError("Unclosed double quote string detected, for sentence: " + this.thisSentence);
		}
	}

	private void recordCommentStart() {
		//Remove the last dash character from the token and sentence if they were added
		int len = this.thisToken.length();
		this.thisToken.delete((len - 2), len);

		len = this.thisSentence.length();
		this.thisSentence.delete((len - 2), len);
	}

	private void saveComment() {
		//Do nothing - comments are not processed further
		resetFlags();
	}

	private void resetFlags() {
		this.firstTokenEndsWithColon = false;
		this.inComment = false;
		this.lastCharDash = false;
		this.lastCharNlOrCr = false;
	}

	private void addCharToComment() {
		//Do nothing - comments are not processed further
	}

	protected boolean charIsTokenDelimiter() {
		boolean result = false;

		//An annotation sentence only has two tokens:
		//  the first word which is the colon-terminated annotation indicator token
		//  the entire remainder of the annotation sentence as a single token
		//This allows any characters (such as single quotes as apostrophes) to be used in annotation sentences
		if (!inAnnotationSentence()) {
			if (isInClearText()) {
				result = (this.currentChar == CHAR_SPACE) ||
							(this.currentChar == CHAR_DOT) ||
							(this.currentChar == CHAR_NL) ||
							(this.currentChar == CHAR_TAB) ||
							(this.currentChar == CHAR_COMMA) ||
							(this.currentChar == CHAR_OPBR) ||
							(this.currentChar == CHAR_CLBR) ||
							(this.currentChar == CHAR_OPPAR) ||
							(this.currentChar == CHAR_CLPAR) ||
							(this.currentChar == CHAR_BOM);
			}
		} else {
			//Annotation sentence delimiters are double NL characters
			if (isNoteAnnotation()) {
				//Note annotations are delimited by double newline characters
				result = ((this.currentChar == CHAR_NL) && (this.lastCharNlOrCr));
			} else {
				//All other annotations are single newline delimited
				result = (this.currentChar == CHAR_NL);
			}
		}

		return result;
	}

	private void reportParsingError(String pText) {
		if (!this.proc.isSuppressingMessages()) {
			reportError(pText, this.ac);
		}
	}

}
