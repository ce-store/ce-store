package com.ibm.ets.ita.ce.store.hudson.model;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpCollection;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpEnumeratedConcept;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpLinkedInstance;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpMatchedTriple;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpMultiMatch;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpNumber;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpThing;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class SpecialPhrase extends InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TYPE_COLLECTION = "collection";
	private static final String TYPE_ENUMCON = "enumerated-concept";
	private static final String TYPE_LINKEDINST = "linked-instance";
	private static final String TYPE_MATCHTRIP = "matched-triple";
	private static final String TYPE_MULTIMATCH = "multi-match";
	private static final String TYPE_NUMBER = "number";

	private String type = null;
	private SpThing special = null;

	public SpecialPhrase(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		this.type = pJo.getString("type");

		if (this.type.equals(TYPE_COLLECTION)) {
			this.special = new SpCollection(pJo);
		} else if (this.type.equals(TYPE_ENUMCON)) {
			this.special = new SpEnumeratedConcept(pJo);
		} else if (this.type.equals(TYPE_LINKEDINST)) {
			this.special = new SpLinkedInstance(pJo);
		} else if (this.type.equals(TYPE_MATCHTRIP)) {
			this.special = new SpMatchedTriple(pJo);
		} else if (this.type.equals(TYPE_MULTIMATCH)) {
			this.special = new SpMultiMatch(pAc, pJo);
		} else if (this.type.equals(TYPE_NUMBER)) {
			this.special = new SpNumber(pJo);
		} else {
			//TODO: Proper error reporting needed
			System.out.println("Unexpected special type: " + this.type);
		}
	}

	public String getType() {
		return this.type;
	}

	public SpThing getSpecial() {
		return this.special;
	}

	public boolean isCollection() {
		return this.type.equals(TYPE_COLLECTION);
	}

	public boolean isEnumeratedConcept() {
		return this.type.equals(TYPE_ENUMCON);
	}

	public boolean isLinkedInstance() {
		return this.type.equals(TYPE_LINKEDINST);
	}

	public boolean isMatchedTriple() {
		return this.type.equals(TYPE_MATCHTRIP);
	}

	public boolean isMultiMatch() {
		return this.type.equals(TYPE_MULTIMATCH);
	}

	public boolean isNumber() {
		return this.type.equals(TYPE_NUMBER);
	}

}
