package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.RANGE_VALUE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.decodeForCe;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcatenatedValue;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSequence;
import com.ibm.ets.ita.ce.store.model.CeSpecialProperty;

public class BuilderSentenceFactNormal extends BuilderSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	//TODO: Complete sequence implementation
	private String instanceName = null;
	private ArrayList<CeConcept> secondaryConceptsNormal = null;
	private ArrayList<CeConcept> secondaryConceptsNegated = null;
	private ArrayList<CePropertyInstance> datatypeProperties = null;
	private ArrayList<CePropertyInstance> objectProperties = null;
	private ArrayList<CeConcatenatedValue> concatValues = null;
	private ArrayList<CeSequence> sequences = null;
	private String rationaleText = null;
	private String rationaleRuleName = null;
	private ArrayList<String> rationaleTokens = null;
	private boolean isClause = false;
	private boolean rationaleProcessing = false;
	private boolean quotedInstanceName = false;
	private ArrayList<String> conceptVariableTokens = null;

	public BuilderSentenceFactNormal(String pSenText) {
		super(pSenText);

		this.secondaryConceptsNormal = new ArrayList<CeConcept>();
		this.secondaryConceptsNegated = new ArrayList<CeConcept>();
		this.datatypeProperties = new ArrayList<CePropertyInstance>();
		this.objectProperties = new ArrayList<CePropertyInstance>();
		this.concatValues = new ArrayList<CeConcatenatedValue>();
		this.sequences = new ArrayList<CeSequence>();
		this.rationaleTokens = new ArrayList<String>();
		this.conceptVariableTokens = new ArrayList<String>();
	}

	public void markAsClause() {
		this.isClause = true;
	}

	public void addRationaleToken(String pToken) {
		this.rationaleTokens.add(pToken);

		//TODO: The various tokens that need to be prepended should be added here
		//this.structuredCeTokens.add(pToken);
	}

	public boolean isClause() {
		return this.isClause;
	}

	@Override
	protected void propogateRationaleValues() {
		getConvertedSentence().setRationaleText(this.rationaleText);
		getConvertedSentence().setRationaleRuleName(this.rationaleRuleName);
	}

	public String getInstanceName() {
		return this.instanceName;
	}

	public void setInstanceName(String pInstanceName) {
		this.instanceName = pInstanceName;
	}

	public String getRationaleText() {
		return this.rationaleText;
	}

	public void setRationaleText(String pRationaleText) {
		this.rationaleText = pRationaleText;
	}

	@Override
	public boolean hasRationale() {
		return !((this.rationaleText == null) || (this.rationaleText.isEmpty()));
	}

	public String getRationaleRuleName() {
		return this.rationaleRuleName;
	}

	public void setRationaleRuleName(String pRationaleRuleName) {
		this.rationaleRuleName = pRationaleRuleName;
	}

	public ArrayList<CeConcept> getAllSecondaryConcepts() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

		result.addAll(this.secondaryConceptsNormal);
		result.addAll(this.secondaryConceptsNegated);

		return result;
	}

	public ArrayList<CeConcept> getSecondaryConceptsNormal() {
		return this.secondaryConceptsNormal;
	}

	public void addSecondaryConceptNormal(CeConcept pConcept) {
		this.secondaryConceptsNormal.add(pConcept);
	}

	public ArrayList<String> getConceptVariableTokens() {
		return this.conceptVariableTokens;
	}

	public void addConceptVariableToken(String pCvToken) {
		this.conceptVariableTokens.add(pCvToken);
	}

	public ArrayList<CeConcept> getSecondaryConceptsNegated() {
		return this.secondaryConceptsNegated;
	}

	public void addSecondaryConceptNegated(CeConcept pConcept) {
		this.secondaryConceptsNegated.add(pConcept);
	}

	public ArrayList<CePropertyInstance> getDatatypeProperties() {
		return this.datatypeProperties;
	}

	public void addDatatypeProperty(ActionContext pAc, String pName, String pValue, CeProperty pProp, boolean pHadQuotes, boolean pIsNegated) {
		String decValue = decodeForCe(pValue);

		//If this is a special operator property just save it, otherwise do the normal processing
		if ((pProp != null) && (pProp.isSpecialOperatorProperty())) {
			CePropertyInstance propInst = CePropertyInstance.createDatatypeProperty(pAc, pProp, decValue, null, null, pHadQuotes, pIsNegated);
			this.datatypeProperties.add(propInst);
		} else {
			if (hasTargetConcept()) {
				String fullPropName = CeProperty.calculateFullPropertyNameFor(getTargetConcept().getConceptName(), pName, RANGE_VALUE);			
				CeProperty targetProperty = getTargetConcept().retrievePropertyFullyNamed(fullPropName);

				if (targetProperty != null) {
					CePropertyInstance propInst = CePropertyInstance.createDatatypeProperty(pAc, targetProperty, decValue, null, null, pHadQuotes, pIsNegated);
					this.datatypeProperties.add(propInst);
				} else {
					hasError(pAc, "Unable to find property named '" + pName + "' and concept named '" + getTargetConcept().getConceptName() + "' when adding datatype property");
				}
			} else {
				hasError(pAc, "No target concept for sentence: " + getSentenceText());
			}
		}
	}

	public ArrayList<CePropertyInstance> getObjectProperties() {
		return this.objectProperties;
	}

	public ArrayList<CeConcatenatedValue> getConcatenatedValues() {
		return this.concatValues;
	}

	public ArrayList<CeSequence> getSequences() {
		return this.sequences;
	}

	@Override
	public boolean isRationaleProcessing() {
		return this.rationaleProcessing;
	}

	@Override
	public void markAsRationaleProcessing() {
		this.rationaleProcessing = true;
	}

	public void addObjectProperty(ActionContext pAc, String pName, String pRange, String pValue, boolean pHadQuotes, boolean pIsNegated) {
		if (hasTargetConcept()) {
			String decValue = decodeForCe(pValue);
			String fullPropName = CeProperty.calculateFullPropertyNameFor(getTargetConcept().getConceptName(), pName, pRange);
			CeProperty targetProperty = null;

			if (CeProperty.isSpecialPropertyName(pName, getTargetConcept().getConceptName())) {
				targetProperty = CeSpecialProperty.getSpecialOperatorPropertyNamed(pAc, pName);
			} else {
				if (pRange.equals(RANGE_VALUE)) {
					targetProperty = getTargetConcept().retrievePropertyFullyNamed(fullPropName);
				} else {
					targetProperty = getTargetConcept().calculatePropertyNamedWithInheritance(pAc, pName, pRange);
				}
			}

			if (targetProperty != null) {
				CePropertyInstance propInst = CePropertyInstance.createObjectProperty(pAc, targetProperty, pRange, decValue, getSentenceText(), null, null, pHadQuotes, pIsNegated);
				this.objectProperties.add(propInst);
			} else {
				hasError(pAc, "Unable to find property named '" + fullPropName + "' when adding object property");
			}
		} else {
			hasError(pAc, "No target concept for sentence: " + getSentenceText());
		}
	}

	public void addConcatenatedValue(String pName, String pConcatText) {
		this.concatValues.add(CeConcatenatedValue.createNewConcatenatedValue(pName, pConcatText));
	}

	@Override
	public CeSentence convertToSentence(ActionContext pAc) {
		CeSentence cSen = super.convertToSentence(pAc);

		if (pAc.getCeConfig().isSavingCeSentences()) {
			for (CePropertyInstance thisPi : this.objectProperties) {
				for (CePropertyValue thisPv : thisPi.getPropertyValues()) {
					thisPv.setSentence(cSen);
				}
			}

			for (CePropertyInstance thisPi : this.datatypeProperties) {
				for (CePropertyValue thisPv : thisPi.getPropertyValues()) {
					thisPv.setSentence(cSen);
				}
			}
		}

		return cSen;
	}

	public void addSequence(ActionContext pAc, CeSequence pSeq) {
		if (hasTargetConcept()) {
			if (pSeq != null) {
				this.sequences.add(pSeq);
			} else {
				hasError(pAc, "No sequence specified");
			}
		} else {
			hasError(pAc, "No target concept for sentence: " + getSentenceText());
		}
	}

	public ArrayList<CePropertyInstance> retrieveAllProperties() {
		ArrayList<CePropertyInstance> result = new ArrayList<CePropertyInstance>();

		result.addAll(getDatatypeProperties());
		result.addAll(getObjectProperties());

		return result;
	}

	public ArrayList<CeProperty> retrievePropertiesNamed(String pName) {
		ArrayList<CeProperty> result = null;

		if (hasTargetConcept()) {
			//TODO: Need to work out how the range can be passed here
			result = getTargetConcept().calculatePropertiesNamed(pName);
		} else {
			result = new ArrayList<CeProperty>();
		}

		return result;
	}

	public ArrayList<CeProperty> retrievePropertiesStartingWith(String pName) {
		ArrayList<CeProperty> result = null;

		if (hasTargetConcept()) {
			//TODO: Need to work out how the range can be passed here
			result = getTargetConcept().calculatePropertiesStartingWith(pName);
		} else {
			result = new ArrayList<CeProperty>();
		}

		return result;
	}

	public void testForMultipleSingleCardinalityProperties(ActionContext pAc) {
		ArrayList<String> tempProps = new ArrayList<String>();

		for (CePropertyInstance thisDpi : this.datatypeProperties) {
			CeProperty thisProp = thisDpi.getRelatedProperty();
			if (thisProp != null) {
				if (thisProp.isSingleCardinality()) {
					String propName = thisProp.getPropertyName();
					if (tempProps.contains(propName)) {
						hasError(pAc, "More than one occurrence of the single cardinality datatype property '" + propName + "' is specified in sentence: " + getSentenceText());
					}
					tempProps.add(propName);
				}
			}
		}

		for (CePropertyInstance thisDpi : this.objectProperties) {
			CeProperty thisProp = thisDpi.getRelatedProperty();
			if (thisProp != null) {
				if (thisProp.isSingleCardinality()) {
					String propName = thisProp.getPropertyName();
					if (tempProps.contains(propName)) {
						hasError(pAc, "More than one occurrence of the single cardinality object property '" + propName + "' is specified in sentence: " + getSentenceText());
					}
					tempProps.add(propName);
				}
			}
		}
	}

	public ArrayList<String> rationaleFragments() {
		ArrayList<String> result = new ArrayList<String>();
		String thisFrag = "";

		for (String thisToken : this.rationaleTokens) {
			if (thisToken.equals("and")) {
				if (!thisFrag.isEmpty()) {
					result.add(thisFrag + TOKEN_DOT);
					thisFrag = "";
				}
			} else {
				//A normal token
				thisFrag += " " + thisToken;
			}
		}

		if (!thisFrag.isEmpty()) {
			result.add(thisFrag + TOKEN_DOT);
		}

		return result;
	}

	public void markInstanceNameAsQuoted() {
		this.quotedInstanceName = true;
	}

	public boolean hadQuotedInstanceName() {
		return this.quotedInstanceName;
	}

}
