package com.ibm.ets.ita.ce.store.hudson.model.answer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

public class AnswerResultSet {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String title = null;
	private ArrayList<String> headers = new ArrayList<String>();
	private ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();

	public AnswerResultSet(ArrayList<String> pHdrs) {
		this.headers = pHdrs;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void applyTitle(String pTitleSingle, String pTitlePlural, String pFilterText) {
		int rowCount = this.rows.size();
		
		if (rowCount == 1) {
			this.title = pTitleSingle;
		} else {
			if (pTitlePlural != null) {
				this.title = pTitlePlural.replace("%COUNT%", new Integer(rowCount).toString());
			}
		}
		
		if (this.title != null) {
			this.title = this.title.replace("%FILTER%", pFilterText);
		}
	}
	
	public ArrayList<String> getHeaders() {
		return this.headers;
	}
	
	public void addHeader(String pHdr) {
		this.headers.add(pHdr);
	}
	
	public ArrayList<ArrayList<String>> getRows() {
		return this.rows;
	}

	public void setRows(ArrayList<ArrayList<String>> pRows) {
		this.rows = pRows;
	}

	public void addRow(ArrayList<String> pRow) {
		this.rows.add(pRow);
	}
	
	public boolean hasSingleValue() {
		boolean result = false;
		
		if (this.rows.size() == 1) {
			if (this.rows.get(0).size() == 1) {
				result = true;
			}
		}
		
		return result;
	}
	
	public String getSingleValue() {
		String result = null;
		
		if (hasSingleValue()) {
			result = this.rows.get(0).get(0);
		}
		
		return result;
	}

	@Override
	public String toString() {
		String result = null;
		
		result = this.headers + ", " + this.rows.size() + " rows";
		
		return result;
	}

}
