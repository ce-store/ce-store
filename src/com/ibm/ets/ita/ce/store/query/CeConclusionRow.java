package com.ibm.ets.ita.ce.store.query;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeConclusionRow {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String TOKEN_NEW = "{{NEW}}";
	private static final String TOKEN_VARIABLE = "$";
	private static final String TOKEN_CONSTANT = "#";

	private CeGeneratorConclusion parent = null;
	private ArrayList<String> queryRow = null;
	private TreeMap<String, String> newValues = null;

	public CeConclusionRow(CeGeneratorConclusion pParent, ArrayList<String> pQueryRow) {
		this.parent = pParent;
		this.queryRow = pQueryRow;
		this.newValues = new TreeMap<String, String>();
	}

	public ArrayList<String> getQueryRow() {
		return this.queryRow;
	}

	public int getQueryRowSize() {
		return this.queryRow.size();
	}

	public String getQueryRow(int pIdx) {
		return this.queryRow.get(pIdx);
	}

	public String getNewValueFor(ActionContext pAc, String pTgtVar) {
		String repTokens[] = pTgtVar.split("_");
		String newValKey = "";

		for (String thisToken : repTokens) {
			if (!newValKey.isEmpty()) {
				newValKey += '_';
			}

			if (thisToken.startsWith(TOKEN_VARIABLE)) {
				String rawToken = thisToken.replace(TOKEN_VARIABLE, "");
				if (rawToken.equals(TOKEN_NEW)) {
					newValKey += rawToken;
				} else {
					//This token should be replaced with the value with that variable name in this result row
					newValKey += this.parent.getValueForHeader(rawToken, this);
				}
			} else if (thisToken.startsWith(TOKEN_CONSTANT)) {
				//This token should be left exactly as the specified text (after removing the marker)
				newValKey += thisToken.replace(TOKEN_CONSTANT, "");
			} else {
				//This is the core part of the token and can be ignored
			}
		}

		String result = this.newValues.get(newValKey);

		if (result == null) {
			result = newValKey.replace(TOKEN_NEW, pAc.getModelBuilder().getNextUid(pAc, ""));
			this.newValues.put(newValKey, result);
		}

		return result;
	}

}