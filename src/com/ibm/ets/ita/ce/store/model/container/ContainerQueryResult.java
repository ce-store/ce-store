package com.ibm.ets.ita.ce.store.model.container;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

public abstract class ContainerQueryResult extends ContainerResult {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";
	
	private static final String COUNT_INDICATOR = "#";	//DSB 01/05/2015 #1096
	private static final String TYPE_COUNT = "C";	//DSB 01/05/2015 #1096

	private String query = "";
	private ArrayList<String> headers = new ArrayList<String>();
	private ArrayList<String> types = new ArrayList<String>();
	protected ArrayList<ArrayList<String>> resultRows = new ArrayList<ArrayList<String>>();

	//TODO: This is inefficient.  Must get rid of it and use a single TreeMap (for result/all rows)
	protected ArrayList<ArrayList<String>> allRows = new ArrayList<ArrayList<String>>();

	@Override
	public boolean isCeResult() {
		return false;
	}

	@Override
	public boolean isStatistics() {
		return false;
	}

	public void setQuery(String pQuery) {
		this.query = pQuery;
	}

	public String getQuery() {
		return this.query;
	}

	public ArrayList<String> getHeaders() {
		return this.headers;
	}

	public int getIndexForHeader(String pHeader) {
		int result = -1;
		int hdrIndex = 0;

		for (String thisHdr : getHeaders()) {
			if (thisHdr.equals(pHeader)) {
				result = hdrIndex;
			}
			++hdrIndex;
		}

		return result;
	}

	public void addHeader(String pHeader, String pType) {
		if (!this.headers.contains(pHeader)) {
			//DSB 01/05/2015 #1096
			if (pHeader.startsWith(COUNT_INDICATOR)) {
				this.headers.add(pHeader);
				this.types.add(TYPE_COUNT);
			} else {
				this.headers.add(pHeader);
				this.types.add(pType);
			}
		}
	}

	public ArrayList<String> getTypes() {
		return this.types;
	}

	public ArrayList<ArrayList<String>> getResultRows() {
		return this.resultRows;
	}

	public void addResultRow(ArrayList<String> pRow) {
		this.resultRows.add(pRow);
	}

	public ArrayList<ArrayList<String>> getAllRows() {
		return this.allRows;
	}

	public void addAllRow(ArrayList<String> pRow) {
		this.allRows.add(pRow);
	}

	//DSB 01/05/2015 #1096
	public boolean hasCountHeader() {
		return this.types.contains(TYPE_COUNT);
	}

}