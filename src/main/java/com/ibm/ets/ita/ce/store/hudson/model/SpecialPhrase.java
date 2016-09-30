package com.ibm.ets.ita.ce.store.hudson.model;

import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_COLLECTION;
import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_ENUMCON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_LINKEDINST;
import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_MATCHTRIP;
import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_MULTIMATCH;
import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_NUMBER;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpCollection;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpEnumeratedConcept;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpLinkedInstance;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpMatchedTriple;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpMultiMatch;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpNumber;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpThing;

public class SpecialPhrase extends InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String type = null;
	private SpThing special = null;

	public SpecialPhrase(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		this.type = pJo.getString("type");

		if (this.type.equals(SPEC_COLLECTION)) {
			this.special = new SpCollection(pJo);
		} else if (this.type.equals(SPEC_ENUMCON)) {
			this.special = new SpEnumeratedConcept(pJo);
		} else if (this.type.equals(SPEC_LINKEDINST)) {
			this.special = new SpLinkedInstance(pJo);
		} else if (this.type.equals(SPEC_MATCHTRIP)) {
			this.special = new SpMatchedTriple(pAc, pJo);
		} else if (this.type.equals(SPEC_MULTIMATCH)) {
			this.special = new SpMultiMatch(pAc, pJo);
		} else if (this.type.equals(SPEC_NUMBER)) {
			this.special = new SpNumber(pJo);
		} else {
			// TODO: Proper error reporting needed
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
		return this.type.equals(SPEC_COLLECTION);
	}

	public boolean isEnumeratedConcept() {
		return this.type.equals(SPEC_ENUMCON);
	}

	public boolean isLinkedInstance() {
		return this.type.equals(SPEC_LINKEDINST);
	}

	public boolean isMatchedTriple() {
		return this.type.equals(SPEC_MATCHTRIP);
	}

	public boolean isMultiMatch() {
		return this.type.equals(SPEC_MULTIMATCH);
	}

	public boolean isNumber() {
		return this.type.equals(SPEC_NUMBER);
	}

}
