package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class BuilderSentenceModel extends BuilderSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private CeConcept newConcept = null;
	private ArrayList<CeConcept> newParents = null;
	private ArrayList<CeProperty> newProperties = null;

	protected BuilderSentenceModel(String pSenText) {
		super(pSenText);

		this.newParents = new ArrayList<CeConcept>();
		this.newProperties = new ArrayList<CeProperty>();
	}

	@Override
	protected void propogateRationaleValues() {
		//Do nothing - rationale does not apply to model sentences
	}

	@Override
	public boolean hasRationale() {
		//Rationale does not apply to model sentences
		return false;
	}

	public void setNewConcept(CeConcept pConcept) {
		this.newConcept = pConcept;
	}

	public CeConcept getNewConcept() {
		return this.newConcept;
	}

	public ArrayList<CeConcept> getNewParents() {
		return this.newParents;
	}

	public void addNewParent(ActionContext pAc, CeConcept pParentConcept) {
		if (!this.newParents.contains(pParentConcept)) {
			this.newParents.add(pParentConcept);

			//Also add the parent to the target concept so that the in-memory model is kept updated
			getTargetConcept().createParent(pAc, pParentConcept);
		}
	}

	public ArrayList<CeProperty> getNewProperties() {
		return this.newProperties;
	}

	public void addNewProperty(CeProperty pProperty) {
		if (!this.newProperties.contains(pProperty)) {
			this.newProperties.add(pProperty);
		}
	}

}
