package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CONCAT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DQ;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SQ;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.stripDelimitingQuotesFrom;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.query.CeConclusionRow;
import com.ibm.ets.ita.ce.store.query.CeGeneratorConclusion;

public class CeConcatenatedValue {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private String name = null;
	private String rawConcatText = null;
	private String parts[] = null;

	private CeConcatenatedValue() {
		// This is private to ensure that new instances can only be created via
		// the various static methods
	}

	public static CeConcatenatedValue createNewConcatenatedValue(String pName, String pConcatText) {
		CeConcatenatedValue result = new CeConcatenatedValue();

		result.name = pName;
		result.rawConcatText = pConcatText;
		result.calculateParts();

		return result;
	}

	private void calculateParts() {
		this.parts = this.rawConcatText.split(TOKEN_CONCAT);

		// Now trim the parts
		// TODO: Think about other whitespace characters like tab, newline etc
		for (int i = 0; i < this.parts.length; i++) {
			String trimmedPart = this.parts[i].trim();
			this.parts[i] = trimmedPart;
		}
	}

	public String getName() {
		return this.name;
	}

	public String getRawConcatText() {
		return this.rawConcatText;
	}

	public String[] getParts() {
		return this.parts;
	}

	public ArrayList<String> getAllVarNames() {
		ArrayList<String> result = new ArrayList<String>();

		// The variable "parts" are those which do not start with a string
		// delimiter (' or ")
		for (String thisPart : this.parts) {
			if (!(thisPart.startsWith(TOKEN_SQ) || thisPart.startsWith(TOKEN_DQ))) {
				result.add(thisPart);
			}
		}

		return result;
	}

	public String getFirstVarName() {
		String result = null;

		ArrayList<String> allVarNames = getAllVarNames();

		if (!allVarNames.isEmpty()) {
			result = allVarNames.get(0);
		}

		return result;
	}

	public String doConcatenationWith(CeConclusionRow pRow, CeGeneratorConclusion pCgc) {
		String result = ES;

		for (String thisPart : this.parts) {
			String strippedVal = stripDelimitingQuotesFrom(thisPart);

			if (strippedVal.equals(thisPart)) {
				//This is a variable (no quotes were stripped)
				result += pCgc.getValueForHeader(strippedVal, pRow);
			} else {
				//This is a fixed value (quotes were stripped)
				result += strippedVal;
			}
		}

		return result;
	}

}
