package com.ibm.ets.ita.ce.store.parsing.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;

public abstract class SentenceParser {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = SentenceParser.class.getName();
	private static final String PACKAGE_NAME = SentenceParser.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	protected static final char CHAR_CR = '\r';
	protected static final char CHAR_NL = '\n';
	protected static final char CHAR_SPACE = ' ';
	protected static final char CHAR_COMMA = ',';
	protected static final char CHAR_OPBR = '(';
	protected static final char CHAR_CLBR = ')';
	protected static final char CHAR_OPPAR = '[';
	protected static final char CHAR_CLPAR = ']';
	protected static final char CHAR_TAB = '	';
	protected static final char CHAR_DOT = '.';
	protected static final char CHAR_SQ = '\'';
	protected static final char CHAR_DQ = '\"';
	protected static final char CHAR_BS = '\\';
	protected static final char CHAR_BOM = (char)65279;	//This is the Byte Order Marker Character inserted when editing in notepad

	//DSB 30/07/2014 - Added handling for special quote characters
	protected static final char CHAR_SQ1 = '‘';
	protected static final char CHAR_SQ2 = '’';
	protected static final char CHAR_DQ1 = '“';
	protected static final char CHAR_DQ2 = '”';

	private static final int SBSIZE_TOKEN = 100;
	private static final int SBSIZE_SENTENCE = 1000;	

	protected ActionContext ac = null;
	protected ArrayList<String> tokens = new ArrayList<String>();

	protected boolean inSingleQuotes = false;
	protected boolean inDoubleQuotes = false;
	protected boolean lastCharBackslash = false;
	protected char currentChar = 0;
	protected String firstToken = null;

	protected StringBuilder thisToken = new StringBuilder(SBSIZE_TOKEN);
	protected StringBuilder thisSentence = new StringBuilder(SBSIZE_SENTENCE);

	private int mode = 0;

	protected abstract void processCharacterExtra();
	protected abstract void recordLastCharExtra();
	protected abstract void saveSentenceExtra();
	protected abstract void saveTokenExtra(String pToken);
	protected abstract void recordChar();
	protected abstract boolean isInClearText();
	protected abstract void handleDot();
	protected abstract void handleNlOrCr();
	protected abstract void handleSpace();
	protected abstract void handleComma();
	protected abstract void handleOpenBracket();
	protected abstract void handleCloseBracket();
	protected abstract void handleOpenParenthesis();
	protected abstract void handleCloseParenthesis();
	protected abstract void handleTab();
	protected abstract void handleSingleQuote();
	protected abstract void handleDoubleQuote();
	protected abstract void testForOpenSingleQuotes();
	protected abstract void testForOpenDoubleQuotes();
	protected abstract void testForTrailingText();
	protected abstract void testForSpecificThings();

	public SentenceParser(ActionContext pAc) {
		this.ac = pAc;
	}

	protected boolean isValidatingOnly() {
		return this.mode == StoreActions.MODE_VALIDATE;
	}

	protected void doParsing(BufferedReader pReader, int pMode) {
		final String METHOD_NAME = "doParsing";

		this.mode = pMode;

		try {
			int thisInt = -1;
			while ((thisInt = pReader.read()) != -1) {
				this.currentChar = (char) thisInt;
				processCharacter();
			}
			testForEndOfProcessingErrors();
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	protected void doParsing(StringBuilder pSb, int pMode) {
		this.mode = pMode;
		int sbLen = pSb.length();

		int thisPos = 0;
		while (thisPos < sbLen) {
			this.currentChar = pSb.charAt(thisPos++);
			processCharacter();
		}
		testForEndOfProcessingErrors();
	}

	protected void saveToken() {
		//This is a token delimiter so add the token to the list if it contains any text
		if (this.thisToken.length() > 0) {
			String thisTokenString = this.thisToken.toString();
			if (!thisTokenString.trim().isEmpty()) {
				this.tokens.add(thisTokenString);

				saveTokenExtra(thisTokenString);

				if (this.firstToken == null) {
					this.firstToken = thisTokenString;
				}
			}
		}
		this.thisToken = new StringBuilder(SBSIZE_TOKEN);
	}

	protected void saveSentence() {
		if (this.thisSentence.length() > 0) {
			saveSentenceExtra();
			this.tokens = new ArrayList<String>();
			this.thisSentence = new StringBuilder(SBSIZE_SENTENCE);
			this.firstToken = null;
		}
	}

	private void processCharacter() {
		recordChar();

		if (this.currentChar == CHAR_SPACE) {
			handleSpace();
		} else if (isNlOrCr()) {
			handleNlOrCr();
		} else if (this.currentChar == CHAR_DOT) {
			handleDot();
		} else if (this.currentChar == CHAR_TAB) {
			handleTab();
//DSB 30/07/2014 - Added handling for special quote characters
//		} else if ((this.currentChar == CHAR_SQ)) {
		} else if ((this.currentChar == CHAR_SQ) || (this.currentChar == CHAR_SQ1) || (this.currentChar == CHAR_SQ2)) {
			handleSingleQuote();
//DSB 30/07/2014 - Added handling for special quote characters
//		} else if ((this.currentChar == CHAR_DQ)) {
		} else if ((this.currentChar == CHAR_DQ) || (this.currentChar == CHAR_DQ1) || (this.currentChar == CHAR_DQ2)) {
			handleDoubleQuote();
		} else if (this.currentChar == CHAR_OPBR) {
			handleOpenBracket();
		} else if (this.currentChar == CHAR_CLBR) {
			handleCloseBracket();
		} else if (this.currentChar == CHAR_OPPAR) {
			handleOpenParenthesis();
		} else if (this.currentChar == CHAR_CLPAR) {
			handleCloseParenthesis();
		} else if (this.currentChar == CHAR_COMMA) {
			handleComma();
		}

		//Further character testing by the subclass (if required)
		processCharacterExtra();
		
		recordLastChar();
	}

	protected void recordLastChar() {
		//Record whether this character is a backslash
		if (this.lastCharBackslash == true && (this.currentChar == CHAR_BS)) {
			//This is a double backslash so ignore it
			this.lastCharBackslash = false;
		} else {
			this.lastCharBackslash = (this.currentChar == CHAR_BS);
		}
		
		//Further character recording by the subclass (if required)
		recordLastCharExtra();
	}

	private void testForEndOfProcessingErrors() {
		testForOpenSingleQuotes();
		testForOpenDoubleQuotes();
		testForTrailingText();

		//Further testing by the subclass (if required)
		testForSpecificThings();
	}

	protected void addCharToTokenAndSentence() {
		this.thisToken.append(this.currentChar);
		this.thisSentence.append(this.currentChar);
	}

	protected void addCharToTokenOnly() {
		this.thisToken.append(this.currentChar);
	}

	protected void addCharToSentenceOnly() {
		this.thisSentence.append(this.currentChar);
	}

	protected boolean isNlOrCr() {
		return (this.currentChar == CHAR_NL) || (this.currentChar == CHAR_CR);
	}

	protected boolean isClearDelimiter() {
		return (this.currentChar == CHAR_NL) || (this.currentChar == CHAR_CR) || (this.currentChar == CHAR_TAB) || (this.currentChar == CHAR_SPACE) || (this.currentChar == CHAR_BOM);
	}

}