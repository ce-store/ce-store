package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.RANGE_VALUE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AND;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NEW;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NO;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SPACE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SQ;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THE;

import java.util.ArrayList;

public class CeClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final int MODE_PREMISE = 1;
	private static final int MODE_CONCLUSION = 2;

//	private static AtomicLong clauseIdVal = new AtomicLong(0);

//	private String id = null;
	private String seqId = null;
	private int clauseMode = 0; // Whether this is a premise or conclusion
	private ArrayList<String> rawTokens = new ArrayList<String>();
	private CeConcept targetConcept = null;
	private boolean targetConceptNegated = false;
	private boolean targetVariableWasQuoted = false;
	private String targetVariable = "";
	private ArrayList<CePropertyInstance> datatypeProperties = new ArrayList<CePropertyInstance>();
	private ArrayList<CePropertyInstance> objectProperties = new ArrayList<CePropertyInstance>();
	private ArrayList<CeConcatenatedValue> concatVals = new ArrayList<CeConcatenatedValue>();
	private ArrayList<CeConcept> secondaryConceptsNormal = new ArrayList<CeConcept>();
	private ArrayList<CeConcept> secondaryConceptsNegated = new ArrayList<CeConcept>();
	private ArrayList<CeClause> childClauses = new ArrayList<CeClause>();
	private ArrayList<String> conceptVariableTokens = new ArrayList<String>();

	private CeClause(String pSeqId, int pMode) {
		// This is private to ensure that new instances can only be created via
		// the various static methods
//		this.id = PREFIX_CLAUSE + nextClauseId();
		this.seqId = pSeqId;
		this.clauseMode = pMode;
	}

//	public static void resetCounter() {
//		clauseIdVal = new AtomicLong(0);
//	}

	public static CeClause createNewPremiseClauseFor(CeQuery pQuery) {
		String seqId = pQuery.calcNextSeqId();

		return new CeClause(seqId, MODE_PREMISE);
	}

	public static CeClause createNewConclusionClauseFor(CeRule pRule) {
		String seqId = pRule.calcNextSeqId();

		return new CeClause(seqId, MODE_CONCLUSION);
	}

	public CeClause cloneFromStemUsing(String pSeqId) {
		CeClause newClause = null;

		if (isPremiseClause()) {
			newClause = new CeClause(pSeqId, MODE_PREMISE);
		} else {
			newClause = new CeClause(pSeqId, MODE_CONCLUSION);
		}

		newClause.setTargetConcept(getTargetConcept());
		newClause.setTargetConceptNegated(isTargetConceptNegated());
		if (targetVariableWasQuoted()) {
			newClause.markTargetVariableAsQuoted();
		}
		newClause.setTargetVariable(getTargetVariable());

		return newClause;
	}

//	private static long nextClauseId() {
//		return clauseIdVal.incrementAndGet();
//	}

//	public String getId() {
//		return this.id;
//	}

	public String getSeqId() {
		return this.seqId;
	}

	public void setSeqId(String pSeqId) {
		this.seqId = pSeqId;
	}

	public String formattedClauseType() {
		String result = null;

		if (isPremiseClause()) {
			result = "premise";
		} else {
			result = "conclusion";
		}

		return result;
	}

	public int getClauseMode() {
		return this.clauseMode;
	}

	public boolean isSimpleClause() {
		// A clause that is just a statement of being (with no properties)
		// e.g. 'there is a person V1'
		return (getObjectProperties().isEmpty()) && (getDatatypeProperties().isEmpty());
	}

	public boolean isCreationClause() {
		// A conclusion clause used to create a new instance
		return (isSimpleClause()) && (getSecondaryConceptsNormal().isEmpty()) && (getConceptVariableTokens().isEmpty());
	}

	public boolean isUnboundedCreationClause() {
		// A conclusion clause that contains the token {{NEW}}
		return isCreationClause() && getTargetVariable().contains(TOKEN_NEW);
	}

	public boolean isPremiseClause() {
		return this.clauseMode == MODE_PREMISE;
	}

	public boolean isConclusionClause() {
		return !isPremiseClause();
	}

	public boolean isConcatenationClause() {
		return !this.concatVals.isEmpty();
	}

	public boolean isDirectClause() {
		return this.childClauses.size() > 0;
	}

	public boolean hasNoRelationships() {
		return this.objectProperties.isEmpty();
	}

	public ArrayList<String> getRawTokens() {
		return this.rawTokens;
	}

	public void addRawToken(String pRawToken) {
		this.rawTokens.add(pRawToken);
	}

	public CeConcept getTargetConcept() {
		return this.targetConcept;
	}

	public void setTargetConcept(CeConcept pConcept) {
		this.targetConcept = pConcept;
	}

	public boolean isTargetConceptNegated() {
		return this.targetConceptNegated;
	}

	public void setTargetConceptNegated(boolean pBool) {
		this.targetConceptNegated = pBool;
	}

	public boolean targetVariableWasQuoted() {
		return this.targetVariableWasQuoted;
	}

	public void markTargetVariableAsQuoted() {
		this.targetVariableWasQuoted = true;
	}

	public String getTargetVariable() {
		return this.targetVariable;
	}

	public void setTargetVariable(String pTargetVariable) {
		this.targetVariable = pTargetVariable;
	}

	public ArrayList<CePropertyInstance> getDatatypeProperties() {
		return this.datatypeProperties;
	}

	public void addDatatypeProperty(CePropertyInstance pPropInst) {
		this.datatypeProperties.add(pPropInst);
	}

	public ArrayList<CePropertyInstance> getObjectProperties() {
		return this.objectProperties;
	}

	public void addObjectProperty(CePropertyInstance pPropInst) {
		this.objectProperties.add(pPropInst);
	}

	public ArrayList<CeConcatenatedValue> getConcatenatedValues() {
		return this.concatVals;
	}

	public void addConcatenatedValue(CeConcatenatedValue pConcatVal) {
		this.concatVals.add(pConcatVal);
	}

	public ArrayList<CeConcept> getSecondaryConceptsNormal() {
		return this.secondaryConceptsNormal;
	}

	public void addSecondaryConceptNormal(CeConcept pSecConcept) {
		this.secondaryConceptsNormal.add(pSecConcept);
	}

	public ArrayList<CeConcept> getSecondaryConceptsNegated() {
		return this.secondaryConceptsNegated;
	}

	public void addSecondaryConceptNegated(CeConcept pSecConcept) {
		this.secondaryConceptsNegated.add(pSecConcept);
	}

	public ArrayList<String> getConceptVariableTokens() {
		return this.conceptVariableTokens;
	}

	public void addConceptVariableToken(String pCvt) {
		this.conceptVariableTokens.add(pCvt);
	}

	public ArrayList<CeConcept> getSecondaryConcepts() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

		result.addAll(getSecondaryConceptsNormal());
		result.addAll(getSecondaryConceptsNegated());

		return result;
	}

	public ArrayList<CeClause> getChildClauses() {
		return this.childClauses;
	}

	public void addChildClause(CeClause pClause) {
		this.childClauses.add(pClause);
	}

	public ArrayList<CePropertyInstance> getAllProperties() {
		ArrayList<CePropertyInstance> allProps = new ArrayList<CePropertyInstance>();

		allProps.addAll(this.datatypeProperties);
		allProps.addAll(this.objectProperties);

		return allProps;
	}

	private String calculateClauseText() {
		String result = ES;
		String sepWord = ES;

		for (String thisToken : this.rawTokens) {
			result += sepWord + thisToken;
			sepWord = TOKEN_SPACE;
		}

		return result;
	}

	public String terminatedClauseText() {
		String result = calculateClauseText();

		if (!result.isEmpty()) {
			result += TOKEN_DOT;
		}

		return result;
	}

	public String formattedDirectClauseString() {
		String result = ES;
		String sepWord = ES;

		result += formattedClauseType() + TOKEN_SPACE;
		result += " direct (" + getSeqId() + ") - ";
		result += this.rawTokens.toString() + NL;
		result += formattedTargetConcept() + " " + formattedTargetVariable();

		for (CeConcept secCon : getSecondaryConceptsNormal()) {
			result += sepWord;
			result += " is a " + secCon.getConceptName() + TOKEN_SPACE;
			sepWord = TOKEN_AND;
		}

		for (CeConcept secCon : getSecondaryConceptsNegated()) {
			result += sepWord;
			result += " is not a " + secCon.getConceptName() + TOKEN_SPACE;
			sepWord = TOKEN_AND;
		}

		for (CePropertyInstance thisProp : this.datatypeProperties) {
			result += sepWord;
			if (thisProp.getRelatedProperty().isVerbSingular()) {
				// The verb singular form for datatype properties is rarely used
				result += TOKEN_SPACE + thisProp.getRelatedProperty().getPropertyName() + " the value '"
						+ thisProp.getSingleOrFirstValue() + "' ";
			} else {
				CePropertyValue thisPv = thisProp.getFirstPropertyValue();
				String qs = null;
				if (thisPv.hadQuotesOriginally()) {
					qs = TOKEN_SQ;
				} else {
					qs = ES;
				}

				result += " has the [value] " + qs + thisPv.getValue() + qs + " as "
						+ thisProp.getRelatedProperty().getPropertyName() + TOKEN_SPACE;
			}
			sepWord = TOKEN_AND;
		}

		for (CePropertyInstance thisProp : this.objectProperties) {
			result += sepWord;
			if (thisProp.getRelatedProperty().isVerbSingular()) {
				result += " " + thisProp.getRelatedProperty().getPropertyName() + " the "
						+ thisProp.getSingleOrFirstRangeName() + TOKEN_SPACE + thisProp.getSingleOrFirstValue() + " ";
			} else {
				result += " has the " + thisProp.getSingleOrFirstRangeName() + TOKEN_SPACE
						+ thisProp.getSingleOrFirstValue() + " as " + thisProp.getRelatedProperty().getPropertyName()
						+ " ";
			}
			sepWord = TOKEN_AND;
		}

		result += NL + "(with " + Integer.toString(this.childClauses.size()) + " child clauses)";

		return result;
	}

	public String formattedChildClauseString() {
		String result = ES;
		String sepWord = ES;

		result += formattedClauseType() + TOKEN_SPACE;
		result += " child (" + getSeqId() + ") - ";
		result += formattedTargetConcept() + TOKEN_SPACE + formattedTargetVariable();

		for (CeConcept secCon : getSecondaryConceptsNormal()) {
			result += sepWord;
			result += " is a " + secCon.getConceptName() + TOKEN_SPACE;
			sepWord = TOKEN_AND;
		}

		for (CeConcept secCon : getSecondaryConceptsNegated()) {
			result += sepWord;
			result += " is-not-a " + secCon.getConceptName() + TOKEN_SPACE;
			sepWord = TOKEN_AND;
		}

		for (CePropertyInstance thisProp : this.datatypeProperties) {
			result += sepWord;
			if (thisProp.getRelatedProperty().isVerbSingular()) {
				// The verb singular form for datatype properties is rarely used
				result += TOKEN_SPACE + thisProp.getRelatedProperty().getPropertyName() + " the value '"
						+ thisProp.getSingleOrFirstValue() + "' ";
			} else {
				CePropertyValue thisPv = thisProp.getFirstPropertyValue();
				String qs = null;

				if (thisPv.hadQuotesOriginally()) {
					qs = TOKEN_SQ;
				} else {
					qs = ES;
				}

				result += " has the [value] " + qs + thisPv.getValue() + qs + " as "
						+ thisProp.getRelatedProperty().getPropertyName() + TOKEN_SPACE;
			}
			sepWord = TOKEN_AND;
		}

		for (CePropertyInstance thisProp : this.objectProperties) {
			result += sepWord;
			if (thisProp.getRelatedProperty().isVerbSingular()) {
				result += TOKEN_SPACE + thisProp.getRelatedProperty().getPropertyName() + " the "
						+ thisProp.getSingleOrFirstRangeName() + TOKEN_SPACE + thisProp.getSingleOrFirstValue()
						+ TOKEN_SPACE;
			} else {
				result += " has the " + thisProp.getSingleOrFirstRangeName() + TOKEN_SPACE
						+ thisProp.getSingleOrFirstValue() + " as " + thisProp.getRelatedProperty().getPropertyName()
						+ TOKEN_SPACE;
			}
			sepWord = TOKEN_AND;
		}

		// TODO: Add concatenated values here

		return result;
	}

	private String formattedTargetConcept() {
		String result = null;

		if (isTargetConceptNegated()) {
			result = TOKEN_NO + TOKEN_SPACE;
		} else {
			result = TOKEN_THE + TOKEN_SPACE;
		}

		if (getTargetConcept() == null) {
			result += RANGE_VALUE;
		} else {
			result += getTargetConcept().getConceptName();
		}

		return result;
	}

	private String formattedTargetVariable() {
		String result = null;

		if (getTargetVariable().isEmpty()) {
			result = "(null targetVariable)";
		} else {
			result = getTargetVariable();
		}

		if (targetVariableWasQuoted()) {
			result = TOKEN_SQ + result + TOKEN_SQ;
		}

		return result;
	}

	@Override
	public String toString() {
		String result = null;

		if (isDirectClause()) {
			result = formattedDirectClauseString();
		} else {
			result = formattedChildClauseString();
		}

		return result;
	}

	public String calculateRawText() {
		String result = null;

		for (String thisToken : this.rawTokens) {
			if (result == null) {
				result = thisToken;
			} else {
				result += TOKEN_SPACE + thisToken;
			}
		}

		return result;
	}

}
