package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.stripDelimitingQuotesFrom;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class CeConcatenatedValue {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TOKEN_CONCAT = "<>";

	private static AtomicLong concatIdVal = new AtomicLong(0);

	private String id = null;
	private String name = "";
	private String rawConcatText = "";
	private String parts[] = null;

	private CeConcatenatedValue() {
		//This is private to ensure that new instances can only be created via the various static methods
	}

	public static CeConcatenatedValue createNewConcatenatedValue(String pName, String pConcatText) {
		CeConcatenatedValue result = new CeConcatenatedValue();
		
		result.id = "ccv_" + nextConcatId();
		result.name = pName;
		result.rawConcatText = pConcatText;
		result.calculateParts();
		
		return result;
	}
	
	public static void resetCounter() {
		concatIdVal = new AtomicLong(0);
	}

	private static long nextConcatId() {
		return concatIdVal.incrementAndGet();
	}

	public String getId() {
		return this.id;
	}
	
	private void calculateParts() {
		this.parts = this.rawConcatText.split(TOKEN_CONCAT);

		//Now trim the parts
		//TODO: Think about other whitespace characters like tab, newline etc
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
		
		//The variable "parts" are those which do not start with a string delimiter (')
		for (String thisPart : this.parts) {
			if (!thisPart.startsWith("'")) {
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

	public String doConcatenationWith(String pVarId, String pValue) {
		String result = "";
		
		for (String thisPart : this.parts) {
			if (thisPart.equals(pVarId)) {
				//This is the variable to be replaced so substitute that
				result += pValue;
			} else {
				//Remove the stand and end quote characters when concatenating
				result += stripDelimitingQuotesFrom(thisPart);
			}
		}
		
		return result;
	}
	
}
