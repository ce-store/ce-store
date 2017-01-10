package com.ibm.ets.ita.ce.store.hudson.model.answer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RESSET;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RS_TITLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RS_FTR;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RS_HDRS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RS_ROWS;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class AnswerResultSet extends Answer {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private String title = null;
	private String footer = null;
	private ArrayList<String> headers = new ArrayList<String>();
	private ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();

	public AnswerResultSet(ArrayList<String> pHdrs, int pConf) {
		super(pConf);

		this.headers = pHdrs;
	}

	public AnswerResultSet(ArrayList<String> pHdrs, ArrayList<ArrayList<String>> pRows, ArrayList<CeInstance> pInsts, int pConf) {
		super(pConf, pInsts);

		this.headers = pHdrs;
		this.rows = pRows;
	}

	public String getTitle() {
		return this.title;
	}
	
	public String getFooter() {
		return this.footer;
	}

	public void setFooter(String pVal) {
		this.footer = pVal;
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

	@Override
	public CeStoreJsonObject specificJson() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		CeStoreJsonObject jRes = new CeStoreJsonObject();
		CeStoreJsonArray jHdrs = new CeStoreJsonArray();
		CeStoreJsonArray jRows = new CeStoreJsonArray();

		jHdrs.addAll(this.headers);
		jRows.addAllArrays(this.rows);
		
		if (this.title != null) {
			jRes.put(JSON_A_RS_TITLE, this.title);
		}

		jRes.put(JSON_A_RS_HDRS, jHdrs);
		jRes.put(JSON_A_RS_ROWS, jRows);
		
		if (this.footer != null) {
			jRes.put(JSON_A_RS_FTR, this.footer);
		}

		result.put(JSON_A_RESSET, jRes);

		return result;
	}

}
