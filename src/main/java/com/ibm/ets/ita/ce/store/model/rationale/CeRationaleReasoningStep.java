package com.ibm.ets.ita.ce.store.model.rationale;

import java.io.Serializable;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeSentence;

public class CeRationaleReasoningStep implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String PREFIX_RS = "rs_";

	private CeSentence sourceSentence = null;
	private String id = "";
	private String ruleName = "";
	private String rationaleCe = "";
	private ArrayList<CeRationalePremise> premises = new ArrayList<CeRationalePremise>();
	private ArrayList<CeRationaleConclusion> conclusions = new ArrayList<CeRationaleConclusion>();

	private CeRationaleReasoningStep() {
		// This is private to ensure that new instances can only be created via
		// the various static methods
	}

	public static CeRationaleReasoningStep createNew(ActionContext pAc, CeSentence pSen, String pRuleName, String pRatText) {
		CeRationaleReasoningStep newInst = new CeRationaleReasoningStep();

		newInst.sourceSentence = pSen;
		newInst.id = PREFIX_RS + pSen.getId();
		newInst.ruleName = pRuleName;
		newInst.rationaleCe = pRatText;

		//Save the reasoning step
		pAc.getModelBuilder().addReasoningStep(newInst);

		return newInst;
	}

	public CeSentence getSourceSentence() {
		return this.sourceSentence;
	}

	public String getId() {
		return this.id;
	}

	public String getRuleName() {
		return this.ruleName;
	}

	public String getRationaleCe() {
		return this.rationaleCe;
	}

	public ArrayList<CeRationalePremise> getPremises() {
		return this.premises;
	}

	public void addPremise(CeRationalePremise pPrem) {
		this.premises.add(pPrem);
	}

	public ArrayList<CeRationaleConclusion> getConclusions() {
		return this.conclusions;
	}

	public void addConclusion(CeRationaleConclusion pConc) {
		this.conclusions.add(pConc);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("ruleName='");
		sb.append(this.ruleName);
		sb.append("', premises=");
		sb.append(this.premises.size());
		sb.append(", conclusions=");
		sb.append(this.conclusions.size());
		sb.append(" (id=");
		sb.append(this.id);
		sb.append(")");
		
		return sb.toString();
	}

}
