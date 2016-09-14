package com.ibm.ets.ita.ce.store.query;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CONSTANT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NEW;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_UNDERSCORE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_VARIABLE;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class CeConclusionRow {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

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
		String repTokens[] = pTgtVar.split(TOKEN_UNDERSCORE);
		String newValKey = ES;

		for (String thisToken : repTokens) {
			if (thisToken.startsWith(TOKEN_VARIABLE)) {
				String rawToken = thisToken.replace(TOKEN_VARIABLE, ES);
				if (rawToken.equals(TOKEN_NEW)) {
					newValKey += rawToken;
				} else {
					// This token should be replaced with the value with that
					// variable name in this result row
					newValKey += this.parent.getValueForHeader(rawToken, this);
				}
			} else if (thisToken.startsWith(TOKEN_CONSTANT)) {
				// This token should be left exactly as the specified text
				// (after removing the marker)
				newValKey += thisToken.replace(TOKEN_CONSTANT, ES);
			} else {
				// This is the core part of the token and can be ignored
			}
		}

		String result = this.newValues.get(newValKey);

		if (result == null) {
			result = newValKey.replace(TOKEN_NEW, pAc.getModelBuilder().getNextUid(pAc, ES));
			this.newValues.put(newValKey, result);
		}

		return result;
	}

}
