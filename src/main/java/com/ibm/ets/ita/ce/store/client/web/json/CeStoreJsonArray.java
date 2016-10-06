package com.ibm.ets.ita.ce.store.client.web.json;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CLOSESQBR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COMMA;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_OPENSQBR;

import java.io.IOException;
import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class CeStoreJsonArray extends CeStoreJsonProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<Object> innerList = new ArrayList<Object>();

	public void add(String pValue) {
		this.innerList.add(pValue);
	}

	public void add(Object pValue) {
		this.innerList.add(pValue);
	}

	public void add(boolean pValue) {
		this.innerList.add(pValue);
	}

	public void add(int pValue) {
		this.innerList.add(pValue);
	}

	public void add(CeStoreJsonObject pValue) {
		this.innerList.add(pValue);
	}

	public void add(CeStoreJsonArray pValue) {
		this.innerList.add(pValue);
	}

	public void addAll(ArrayList<String> pValues) {
		this.innerList.addAll(pValues);
	}

	public void addAllArrays(ArrayList<ArrayList<String>> pValues) {
		for (ArrayList<String> thisRow : pValues) {
			CeStoreJsonArray jRow = new CeStoreJsonArray();
			jRow.addAll(thisRow);
			add(jRow);
		}
	}

	public Object get(int index) {
		return this.innerList.get(index);
	}

	public int length() {
		return this.innerList.size();
	}

	public ArrayList<Object> items() {
		return this.innerList;
	}

	public boolean isEmpty() {
		return this.innerList.isEmpty();
	}

	@Override
	public StringBuilder serializeToSb(ActionContext pAc) throws IOException {
		StringBuilder sb = new StringBuilder();

		if (this.jsonText == null) {
			String commaVal = ES;
			sb.append(TOKEN_OPENSQBR);

			for (Object thisVal : this.innerList) {
				sb.append(commaVal);
				if (thisVal instanceof CeStoreJsonObject) {
					sb.append(((CeStoreJsonObject) thisVal).serialize(pAc));
				} else if (thisVal instanceof CeStoreJsonArray) {
					sb.append(((CeStoreJsonArray) thisVal).serialize(pAc));
				} else {
					sb.append(encodeJsonValue((String) thisVal));
				}
				commaVal = TOKEN_COMMA;
			}

			sb.append(TOKEN_CLOSESQBR);
		} else {
			sb.append(this.jsonText);
		}

		return sb;
	}

	@Override
	public String serialize(ActionContext pAc) throws IOException {
		return serializeToSb(pAc).toString();
	}

	@Override
	public String toString() {
		return "(CeStoreJsonArray): " + this.innerList.toString();
	}

}
