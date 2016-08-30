package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.NL;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeRule extends CeQuery {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private CeSentence[] inferredSentences = new CeSentence[0];
	private ArrayList<CeClause> directConclusionClauses = new ArrayList<CeClause>();

	//For performance purposes only - when determining if this rule has already inferred a particular sentence
	private HashSet<String> sentenceLookup = null;

	private CeRule(String pRuleCe, String pRuleName) {
		//This is private to ensure that new instances can only be created via the various static methods
		super(pRuleCe, pRuleName);

		//Rules are always "normal" queries... they can never be count queries
		markAsNormalQuery();
	}

	public static CeRule createNew(String pRuleCe, String pRuleName) {
		return new CeRule(pRuleCe, pRuleName);
	}

	public static boolean isTemporaryRuleName(String pRuleName) {
		return pRuleName.startsWith("temp_");
	}

	@Override
	public String identityKey() {
		return this.name;
	}
	
	@Override
	public boolean isRule() {
		return true;
	}
	
	@Override
	public boolean isQuery() {
		return false;
	}
	
	public String getRuleName() {
		return this.name;
	}
	
	public ArrayList<CeClause> getDirectConclusionClauses() {
		return this.directConclusionClauses;
	}
	
	public void addDirectConclusionClause(CeClause pClause) {
		this.directConclusionClauses.add(pClause);
	}
	
	@Override
	public ArrayList<CeClause> getAllDirectClauses() {
		ArrayList<CeClause> result = new ArrayList<CeClause>();
		
		result.addAll(getDirectPremiseClauses());
		result.addAll(getDirectConclusionClauses());
		
		return result;
	}
	
	public CeSentence[] getInferredSentences() {
		return this.inferredSentences;
	}

	public void markAsTemporary() {
		//this is a temporary rule so add the prefix to the rule name
		this.name = "temp_" + this.name;
	}
	
	public void addInferredSentence(CeSentence pSen) {
		int currLen = 0;

		currLen = this.inferredSentences.length;
		CeSentence[] newArray = new CeSentence[currLen + 1];
		System.arraycopy(this.inferredSentences, 0, newArray, 0, currLen);
		this.inferredSentences = newArray;

		this.inferredSentences[currLen] = pSen;
	}

	public void createSentenceLookup(ActionContext pAc) {
		this.sentenceLookup = new HashSet<String>();
		
		for (CeSentence thisSen : this.inferredSentences) {
			this.sentenceLookup.add(thisSen.getCeText(pAc));
		}
	}

	public void clearSentenceLookup() {
		this.sentenceLookup = new HashSet<String>();
	}

	public boolean doesSentenceTextAlreadyExist(ActionContext pAc, String pCeText) {
		boolean result = this.sentenceLookup.contains(pCeText);

		return result;
	}

	@Override
	protected String calcNextSeqId() {
		return Integer.toString(++this.lastSeqId);
	}

	@Override
	public String toString() {
		String result = "";

		result = "CeRule (" + getRuleName() + "):" + NL;
		result += "  Premises:" + NL;
		for (CeClause thisClause : listAllChildPremiseClauses()) {
			result += "    " + thisClause.toString() + NL;
		}
		result += "  Conclusions:" + NL;
		for (CeClause thisClause : listAllChildConclusionClauses()) {
			result += "    " + thisClause.toString() + NL;
		}

		return result;
	}

	@Override
	public ArrayList<CeClause> listAllChildPremiseClauses() {
		ArrayList<CeClause> result = new ArrayList<CeClause>();
		
		for (CeClause directClause : getDirectPremiseClauses()) {
			result.addAll(directClause.getChildClauses());
		}
		
		return result;
	}

	public ArrayList<CeClause> listAllChildConclusionClauses() {
		ArrayList<CeClause> result = new ArrayList<CeClause>();
		
		for (CeClause directClause : getDirectConclusionClauses()) {
			result.addAll(directClause.getChildClauses());
		}
		
		return result;
	}

	@Override
	public boolean isIncludedVarId(String pVarId) {
		//This is a rule, so every clause is always included
		return true;
	}
	
	public HashSet<CeConcept> listAllPremiseConcepts() {
		HashSet<CeConcept> result = new HashSet<CeConcept>();

		for (CeClause premClause : listAllChildPremiseClauses()) {
			if (premClause.getTargetConcept() != null) {
				result.add(premClause.getTargetConcept());
			}
		}

		return result;
	}

	public HashSet<CeProperty> listAllPremiseProperties() {
		HashSet<CeProperty> result = new HashSet<CeProperty>();

		for (CeClause premClause : listAllChildPremiseClauses()) {
			for (CePropertyInstance thisPi : premClause.getAllProperties()) {
				//Ignore special operator properties
				if (!thisPi.isSpecialOperatorPropertyInstance()) {
					result.add(thisPi.getRelatedProperty());
				}
			}
		}

		return result;
	}

	public boolean hasAnyUnboundedCreationConclusionClauses() {
		boolean result = false;

		for (CeClause thisClause : listAllChildConclusionClauses()) {
			if (thisClause.isUnboundedCreationClause()) {
				result = true;
			}
		}

		return result;
	}

	@Override
	public void testIntegrity(ActionContext pAc) {
		super.testIntegrity(pAc);

		//Test for duplicate conclusion clauses
		HashSet<String> clauseTexts = new HashSet<String>();

		for (CeClause thisClause : this.directConclusionClauses) {
			String thisClauseText = thisClause.calculateRawText();

			if (clauseTexts.contains(thisClauseText)) {
				reportWarning("Duplicate conculsion clause detected in rule '" + this.getQueryName() + "' (" + thisClauseText + ")", pAc);
			} else {
				clauseTexts.add(thisClauseText);
			}
		}
	}

}
