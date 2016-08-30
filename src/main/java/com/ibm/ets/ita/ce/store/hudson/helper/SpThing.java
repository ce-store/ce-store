package com.ibm.ets.ita.ce.store.hudson.helper;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public abstract class SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected static final String JSON_TYPE = "type";
	protected static final String JSON_NAME = "name";
	protected static final String JSON_POS = "position";
	protected static final String JSON_CEID = "_id";

	protected String label = null;
	protected int position = -1;

	protected void extractStandardFieldsFromJson(CeStoreJsonObject pJo) {
		this.label = pJo.getString(JSON_NAME);
		this.position = pJo.getInt(JSON_POS);
	}

	public String getLabel() {
		return this.label;
	}

	public int getPosition() {
		return this.position;
	}

	public boolean isNumber() {
		return false;
	}

	public boolean isMatchedTriple() {
		return false;
	}

	public boolean isLinkedInstance() {
		return false;
	}

	public boolean isCollection() {
		return false;
	}

	public boolean isEnumeratedConcept() {
		return false;
	}


}
