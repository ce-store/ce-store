package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSentence;

public class BuilderSentenceRuleOrQuery extends BuilderSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private CeQuery query = null;
	private CeRule rule = null;

	public BuilderSentenceRuleOrQuery(String pSenText) {
		super(pSenText);
	}

	@Override
	protected void propogateRationaleValues() {
		//Do nothing - rationale does not apply to rule or query sentences
	}

	@Override
	public boolean hasRationale() {
		//Rationale does not apply to rule or query sentences
		return false;
	}

	public CeQuery getQuery() {
		return this.query;
	}

	public void setQuery(CeQuery pQuery) {
		this.query = pQuery;
	}

	public CeRule getRule() {
		return this.rule;
	}

	public void setRule(CeRule pRule) {
		this.rule = pRule;
	}

	@Override
	public CeSentence convertToSentence(ActionContext pAc) {
		if (pAc.isExecutingQueryOrRule()) {
			//A rule or query is being executed, so this sentence should not be saved
			//(It is being processed purely to extract details for execution)
			if (this.convertedSentence == null) {
				this.convertedSentence = CeSentence.createNewRuleOrQuerySentenceWithoutSaving(pAc, this.type, this.validity, getSentenceText());			
			}
		} else {
			//A rule or query is not being executed, so this sentence is being saved.
			//The normal processing (on the parent) can be carried out
			this.convertedSentence = super.convertToSentence(pAc);
		}

		return this.convertedSentence;
	}
}