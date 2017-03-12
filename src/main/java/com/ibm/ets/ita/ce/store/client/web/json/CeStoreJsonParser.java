package com.ibm.ets.ita.ce.store.client.web.json;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_BS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_CLOSEBRA;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_CLPAR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_COLON;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_COMMA;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_DQ;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_FQ;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_OPENBRA;
import static com.ibm.ets.ita.ce.store.names.ParseNames.CHAR_OPPAR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_FALSE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NULL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_TRUE;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.bufferedReaderFromString;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class CeStoreJsonParser {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CLASS_NAME = CeStoreJsonParser.class.getName();
	private static final String PACKAGE_NAME = CeStoreJsonParser.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final int STATE_DEFAULT = 1;
	private static final int STATE_ELEMVAL = 2;
	private static final int STATE_ARRAYVAL = 3;

	private static final String CLASS_ARRAY = "CeStoreJsonArray";
	private static final String CLASS_OBJECT = "CeStoreJsonObject";

	private ActionContext ac = null;
	private String jsonText = null;
	private char currentChar = 0;
	private ArrayList<Object> stack = null;

	private boolean inQuote = false;
	private boolean wasQuoted = false;
	private boolean lastCharBs = false;
	private int state = 0;
	private StringBuilder currentItem = null;
	private String elemName = null;

	public CeStoreJsonParser(ActionContext pAc, String pJsonText) {
		this.ac = pAc;
		this.jsonText = pJsonText;
		this.stack = new ArrayList<Object>();
		this.currentItem = new StringBuilder();
	}

	public void parse() {
		final String METHOD_NAME = "doParsing";

		BufferedReader r = bufferedReaderFromString(this.jsonText);

		try {
			int thisInt = -1;
			while ((thisInt = r.read()) != -1) {
				this.currentChar = (char) thisInt;
				processCharacter();
			}
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	public boolean hasRootJsonObject() {
		return getRootJsonObject() != null;
	}

	public boolean hasRootJsonArray() {
		return getRootJsonArray() != null;
	}

	public CeStoreJsonObject getRootJsonObject() {
		CeStoreJsonObject result = null;

		if (!this.stack.isEmpty()) {
			Object rawObj = getStackHead();
			String className = rawObj.getClass().getSimpleName();

			if (className.equals(CLASS_OBJECT)) {
				result = (CeStoreJsonObject) rawObj;
			}
		}

		return result;
	}

	private Object getStackHead() {
		return this.stack.get(0);
	}

	private Object getStackTail() {
		return this.stack.get(this.stack.size() - 1);
	}

	private void pushStack(Object pObj) {
		this.stack.add(pObj);
	}

	private void popStack() {
		// Don't ever pop the last (root) item from the stack
		if (this.stack.size() > 1) {
			this.stack.remove(this.stack.size() - 1);
		}
	}

	public CeStoreJsonArray getRootJsonArray() {
		CeStoreJsonArray result = null;

		if (!this.stack.isEmpty()) {
			Object rawObj = getStackHead();
			String className = rawObj.getClass().getSimpleName();

			if (className.equals(CLASS_ARRAY)) {
				result = (CeStoreJsonArray) rawObj;
			}
		}

		return result;
	}

	private void processCharacter() {
		if (this.lastCharBs) {
			processEscapedCharacter();
		} else {
			processNormalCharacter();
		}
	}

	private void processEscapedCharacter() {
		// This character occurs directly after an escape character (backslash)
		switch (this.currentChar) {
		case CHAR_BS:
			// A backslash character
			doEscapedBackslash();
		case CHAR_DQ:
			// A quote character
			doEscapedQuote();
			break;
		case CHAR_FQ:
			// A forward quote character
			doEscapedForwardSlash();
			break;
		default:
			// Anything else should not occur in this position
			reportError("Unexpected character (" + this.currentChar + ") following backslash", this.ac);
			break;
		}

		this.lastCharBs = false;
	}

	private void processNormalCharacter() {
		switch (this.currentChar) {
		case CHAR_OPENBRA:
			doOpenBrace();
			break;
		case CHAR_CLOSEBRA:
			doCloseBrace();
			break;
		case CHAR_OPPAR:
			doOpenParenthesis();
			break;
		case CHAR_CLPAR:
			doCloseParenthesis();
			break;
		case CHAR_BS:
			doBackslash();
			break;
		case CHAR_DQ:
			doQuote();
			break;
		case CHAR_COMMA:
			doComma();
			break;
		case CHAR_COLON:
			doColon();
			break;
		default:
			// Some other character
			doDefault();
			break;
		}
	}

	private void doEscapedBackslash() {
		// Simply add any escaped backslash to currentItem
		doDefault();
	}

	private void doEscapedQuote() {
		// Simply add any escaped quote to currentItem
		doDefault();
	}

	private void doEscapedForwardSlash() {
		// Simply add any escaped forward slash to currentItem
		doDefault();
	}

	private void doOpenBrace() {
		if (this.inQuote) {
			doDefault();
		} else {
			startObject();
		}
	}

	private void doCloseBrace() {
		if (this.inQuote) {
			doDefault();
		} else {
			endObject();
		}
	}

	private void doOpenParenthesis() {
		if (this.inQuote) {
			doDefault();
		} else {
			startArray();
		}
	}

	private void doCloseParenthesis() {
		if (this.inQuote) {
			doDefault();
		} else {
			endArray();
		}
	}

	private boolean hasRoot() {
		return !this.stack.isEmpty();
	}

	private void startObject() {
		resetState();

		if (!hasRoot()) {
			pushStack(new CeStoreJsonObject());
		} else {
			CeStoreJsonObject newObj = new CeStoreJsonObject();

			Object lastObj = getStackTail();
			String className = lastObj.getClass().getSimpleName();

			if (className.equals(CLASS_ARRAY)) {
				((CeStoreJsonArray) lastObj).add(newObj);
			} else if (className.equals(CLASS_OBJECT)) {
				((CeStoreJsonObject) lastObj).put(this.elemName, newObj);
			}

			pushStack(newObj);
		}
	}

	private void endObject() {
		if (this.currentItem != null) {
			saveCurrentItem();
		}

		resetState();
		popStack();
	}

	private void startArray() {
		if (!hasRoot()) {
			pushStack(new CeStoreJsonArray());
		} else {
			CeStoreJsonArray newArr = new CeStoreJsonArray();

			Object lastObj = getStackTail();
			String className = lastObj.getClass().getSimpleName();

			if (className.equals(CLASS_ARRAY)) {
				((CeStoreJsonArray) lastObj).add(newArr);
			} else if (className.equals(CLASS_OBJECT)) {
				((CeStoreJsonObject) lastObj).put(this.elemName, newArr);
			}

			pushStack(newArr);
		}

		this.state = STATE_ARRAYVAL;
	}

	private void endArray() {
		if (this.currentItem != null) {
			saveCurrentItem();
		}

		resetState();
		popStack();
	}

	private void doBackslash() {
		this.lastCharBs = !this.lastCharBs;
	}

	private void doQuote() {
		this.inQuote = !this.inQuote;

		if (this.inQuote) {
			doEnterQuote();
		} else {
			doExitQuote();
		}
	}

	private void doEnterQuote() {
		// Nothing is needed
	}

	private void doExitQuote() {
		//It was an empty quote, so should be an empty string rather than null
		if (this.currentItem == null) {
			this.currentItem = new StringBuilder();
		}

		this.wasQuoted = true;
		saveCurrentItem();
		this.wasQuoted = false;
	}

	private void doComma() {
		if (this.inQuote) {
			doDefault();
		} else {
			saveCurrentItem();
		}
	}

	private void doColon() {
		if (this.inQuote) {
			doDefault();
		} else {
			saveCurrentItem();
		}
	}

	private void doDefault() {
		if (this.currentItem == null) {
			this.currentItem = new StringBuilder();
		}

		this.currentItem.append(this.currentChar);
	}

	private void resetState() {
		this.state = STATE_DEFAULT;
	}

	private void saveCurrentItem() {
		if (this.currentItem != null) {
			switch (this.state) {
			case STATE_DEFAULT:
				saveElementName();
				break;
			case STATE_ELEMVAL:
				saveElementValue();
				break;
			case STATE_ARRAYVAL:
				saveArrayValue();
				break;
			default:
				reportError("Unknown state (" + this.state + ") in saveCurrentItem()", this.ac);
				break;
			}

			this.currentItem = null;
		}
	}

	private void saveElementName() {
		this.elemName = this.currentItem.toString();
		this.state = STATE_ELEMVAL;
	}

	private void saveElementValue() {
		if (this.wasQuoted) {
			String elemVal = this.currentItem.toString();
			((CeStoreJsonObject) getStackTail()).put(this.elemName, elemVal);
		} else {
			CeStoreJsonObject jObj = ((CeStoreJsonObject) getStackTail());
			String elemVal = this.currentItem.toString();

			if (elemVal.equalsIgnoreCase(TOKEN_NULL)) {
				jObj.put(this.elemName, (Object) null);
			} else if (elemVal.equalsIgnoreCase(TOKEN_TRUE)) {
				jObj.put(this.elemName, true);
			} else if (elemVal.equalsIgnoreCase(TOKEN_FALSE)) {
				jObj.put(this.elemName, false);
			} else {
				jObj.put(this.elemName, new Float(elemVal).floatValue());
			}
		}

		this.state = STATE_DEFAULT;
	}

	private void saveArrayValue() {
		if (this.wasQuoted) {
			String arrayValue = this.currentItem.toString();
			((CeStoreJsonArray) getStackTail()).add(arrayValue);
		} else {
			CeStoreJsonArray jArr = ((CeStoreJsonArray) getStackTail());
			String elemVal = this.currentItem.toString();

			if (elemVal.equalsIgnoreCase(TOKEN_NULL)) {
				jArr.add((Object) null);
			} else if (elemVal.equalsIgnoreCase(TOKEN_TRUE)) {
				jArr.add(true);
			} else if (elemVal.equalsIgnoreCase(TOKEN_FALSE)) {
				jArr.add(false);
			} else {
				jArr.add(new Float(elemVal).floatValue());
			}
		}
	}

}
