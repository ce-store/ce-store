package com.ibm.ets.ita.ce.store.client.web.json;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.io.IOException;
import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeStoreJsonArray extends CeStoreJsonProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

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
			String commaVal= "";
			sb.append("[");

			for (Object thisVal : this.innerList) {
				sb.append(commaVal);
				if (thisVal instanceof CeStoreJsonObject) {
					sb.append(((CeStoreJsonObject)thisVal).serialize(pAc));
				} else if (thisVal instanceof CeStoreJsonArray) {
					sb.append(((CeStoreJsonArray)thisVal).serialize(pAc));
				} else {
					sb.append(encodeJsonValue((String)thisVal));
				}
				commaVal = ",";
			}

			sb.append("]");
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