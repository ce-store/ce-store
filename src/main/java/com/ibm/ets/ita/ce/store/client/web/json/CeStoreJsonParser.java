package com.ibm.ets.ita.ce.store.client.web.json;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.bufferedReaderFromString;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeStoreJsonParser {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = CeStoreJsonParser.class.getName();
	private static final String PACKAGE_NAME = CeStoreJsonParser.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final int STATE_DEFAULT = 1;
	private static final int STATE_ELEMVAL = 2;
	private static final int STATE_ARRAYVAL = 3;

	private static final String CLASS_ARRAY = "CeStoreJsonArray";
	private static final String CLASS_OBJECT = "CeStoreJsonObject";
	private static final String CLASS_STRING = "String";
	private static final String INDENT = "  ";
	private static final String NL = "\n";

	private ActionContext ac = null;
	private String jsonText = null;
	private char currentChar = 0;
	private ArrayList<Object> stack = new ArrayList<Object>();

	private boolean inQuote = false;
	private boolean wasQuoted = false;
	private boolean lastCharBs = false;
	private int state = 0;
	private StringBuilder currentItem = new StringBuilder();
	private String elemName = null;

	public CeStoreJsonParser(ActionContext pAc, String pJsonText) {
		this.ac = pAc;
		this.jsonText = pJsonText;
	}

	public void parse() {
		final String METHOD_NAME = "doParsing";

//System.out.println(this.jsonText);

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

		if (this.ac.getCeConfig().isDebug()) {
			debugSummary();
		}
	}

	private void debugSummary() {
		StringBuilder sb = new StringBuilder();

		CeStoreJsonObject jObj = getRootJsonObject();

		if (jObj != null) {
			summariseObject(jObj, INDENT, sb);
		}

		CeStoreJsonArray jArr = getRootJsonArray();

		if (jArr != null) {
			summariseArray(jArr, INDENT, sb);
		}

		System.out.println(sb.toString());
	}

	private void summariseObject(CeStoreJsonObject pObj, String pIndent, StringBuilder pSb) {
		boolean firstTime = true;
		StringBuilder thisSb = new StringBuilder();

		for (String thisKey : pObj.keySet()) {
			Object rawObj = pObj.get(this.ac, thisKey);

			if (!firstTime) {
				thisSb.append(NL);
			}

			thisSb.append(pIndent + thisKey + ": ");
			summariseRawObject(rawObj, pIndent, thisSb);

			firstTime = false;
		}

		if (thisSb.length() > 0) {
			if (pSb.length() > 0) {
				pSb.append(NL);
			}
			pSb.append(thisSb);
		}
	}

	private void summariseArray(CeStoreJsonArray pArr, String pIndent, StringBuilder pSb) {
		boolean firstTime = true;

		for (int i = 0; i < pArr.length(); i++) {
			Object thisObj = pArr.get(i);

			if (!firstTime) {
				pSb.append(", ");
			}

			summariseRawObject(thisObj, pIndent + INDENT, pSb);
			firstTime = false;
		}
	}

	private void summariseRawObject(Object pObj, String pIndent, StringBuilder pSb) {
		String className = pObj.getClass().getSimpleName();

		if (className.equals(CLASS_ARRAY)) {
			summariseArray((CeStoreJsonArray)pObj, pIndent + INDENT, pSb);
		} else if (className.equals(CLASS_OBJECT)){
			summariseObject((CeStoreJsonObject)pObj, pIndent + INDENT, pSb);
		} else if (className.equals(CLASS_STRING)){
			pSb.append(pObj.toString());
		} else {
			reportError("Unexpected class (" + className + ") in summariseRawObject()", this.ac);
			pSb.append("???");
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
				result = (CeStoreJsonObject)rawObj;
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
		//Don't ever pop the last (root) item from the stack
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
				result = (CeStoreJsonArray)rawObj;
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
		//This character occurs directly after an escape character (backslash)
		switch (this.currentChar) {
		case '\\':
			// A backslash character
			doEscapedBackslash();
		case '"':
			// A quote character
			doEscapedQuote();
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
		case '{':
			doOpenBrace();
			break;
		case '}':
			doCloseBrace();
			break;
		case '[':
			doOpenParenthesis();
			break;
		case ']':
			doCloseParenthesis();
			break;
		case '\\':
			doBackslash();
			break;
		case '"':
			doQuote();
			break;
		case ',':
			doComma();
			break;
		case ':':
			doColon();
			break;
		default:
			// Some other character
			doDefault();
			break;
		}
	}

	private void doEscapedBackslash() {
//System.out.println("doEscapedBackslash: " + this.currentChar);
		//Simply add any escaped backslash to currentItem
		doDefault();
	}

	private void doEscapedQuote() {
//System.out.println("doEscapedQuote: " + this.currentChar);
		//Simply add any escaped quote to currentItem
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
//System.out.println("startObject: " + this.currentChar);
		resetState();

		if (!hasRoot()) {
			pushStack(new CeStoreJsonObject());
		} else {
			CeStoreJsonObject newObj = new CeStoreJsonObject();

			Object lastObj = getStackTail();
			String className = lastObj.getClass().getSimpleName();

			if (className.equals(CLASS_ARRAY)) {
				((CeStoreJsonArray)lastObj).add(newObj);
			} else if (className.equals(CLASS_OBJECT)){
				((CeStoreJsonObject)lastObj).put(this.elemName, newObj);
			}

			pushStack(newObj);
		}
	}

	private void endObject() {
//System.out.println("endObject: " + this.currentChar + " - elemName:" + this.elemName);

		if (this.currentItem != null) {
			saveCurrentItem();
		}

		resetState();
		popStack();
	}

	private void startArray() {
//System.out.println("startArray: " + this.currentChar);
		if (!hasRoot()) {
			pushStack(new CeStoreJsonArray());
		} else {
			CeStoreJsonArray newArr = new CeStoreJsonArray();

			Object lastObj = getStackTail();
			String className = lastObj.getClass().getSimpleName();

			if (className.equals(CLASS_ARRAY)) {
				((CeStoreJsonArray)lastObj).add(newArr);
			} else if (className.equals(CLASS_OBJECT)){
				((CeStoreJsonObject)lastObj).put(this.elemName, newArr);
			}

			pushStack(newArr);
		}

		this.state = STATE_ARRAYVAL;
	}

	private void endArray() {
//System.out.println("endArray: " + this.currentChar + " - elemName:" + this.elemName);
		if (this.currentItem != null) {
			saveCurrentItem();
		}

		resetState();
		popStack();
	}

	private void doBackslash() {
//System.out.println("doBackslash: " + this.currentChar);
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
		//Nothing is needed
//System.out.println("doEnterQuote: " + this.currentChar);
	}

	private void doExitQuote() {
//System.out.println("doExitQuote: " + this.currentChar);
		this.wasQuoted = true;
		saveCurrentItem();
		this.wasQuoted = false;
	}

	private void doComma() {
//System.out.println("doComma: " + this.currentChar);
		if (this.inQuote) {
			doDefault();
		} else {
			saveCurrentItem();
		}
	}

	private void doColon() {
//System.out.println("doColon: " + this.currentChar);
		if (this.inQuote) {
			doDefault();
		} else {
			saveCurrentItem();
		}
	}

	private void doDefault() {
//System.out.println("doDefault: " + this.currentChar);
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
//System.out.println("saveElementName: " + this.elemName);
		this.state = STATE_ELEMVAL;
	}

	private void saveElementValue() {
		if (this.wasQuoted) {
			String elemVal = this.currentItem.toString();
			((CeStoreJsonObject)getStackTail()).put(this.elemName, elemVal);
		} else {
			CeStoreJsonObject jObj = ((CeStoreJsonObject)getStackTail());
			String elemVal = this.currentItem.toString();

			if (elemVal.equalsIgnoreCase("null")) {
				jObj.put(this.elemName, (Object)null);
			} else if (elemVal.equalsIgnoreCase("true")) {
				jObj.put(this.elemName, true);
			} else if (elemVal.equalsIgnoreCase("false")) {
				jObj.put(this.elemName, false);
			} else {
				jObj.put(this.elemName, new Integer(elemVal).intValue());
			}
		}

		this.state = STATE_DEFAULT;
	}

	private void saveArrayValue() {
		if (this.wasQuoted) {
			String arrayValue = this.currentItem.toString();
			((CeStoreJsonArray)getStackTail()).add(arrayValue);
		} else {
			CeStoreJsonArray jArr = ((CeStoreJsonArray)getStackTail());
			String elemVal = this.currentItem.toString();

			if (elemVal.equalsIgnoreCase("null")) {
				jArr.add((Object)null);
			} else if (elemVal.equalsIgnoreCase("true")) {
				jArr.add(true);
			} else if (elemVal.equalsIgnoreCase("false")) {
				jArr.add(false);
			} else {
				jArr.add(new Integer(elemVal).intValue());
			}
		}
	}

}
