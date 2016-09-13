package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.RANGE_VALUE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NO_TS;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class CePropertyInstance implements Comparable<CePropertyInstance> {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String id = null;
	private CeInstance relatedInstance = null;
	private CeProperty relatedProperty = null;
	private CePropertyValue[] propertyValues = new CePropertyValue[0];

	//TODO: remove the need for this
	private String clauseVariableId = null;

	private CePropertyInstance(CeProperty pProperty, CeInstance pRelInst) {
		this.relatedProperty = pProperty;
		this.relatedInstance = pRelInst;
	}

	public static CePropertyInstance createDatatypeProperty(ActionContext pAc, CeProperty pProperty, String pValue, CeInstance pRelInst, CeSentence pSen, boolean pHadQuotes, boolean pIsNegated) {
		CePropertyInstance newPi = new CePropertyInstance(pProperty, pRelInst);

		newPi.setInitialValue(pAc, pValue, RANGE_VALUE, pHadQuotes, pIsNegated, pSen);

		return newPi;
	}

	public static CePropertyInstance createObjectProperty(ActionContext pAc, CeProperty pProperty, String pRangeName, String pValue, String pSentenceText, CeInstance pRelInst, CeSentence pSen, boolean pHadQuotes, boolean pIsNegated) {
		CePropertyInstance newPi = new CePropertyInstance(pProperty, pRelInst);

		newPi.setInitialValue(pAc, pValue, pRangeName, pHadQuotes, pIsNegated, pSen);
		
		if (!pProperty.isSpecialOperatorProperty()) {
			newPi.testRangeMatch(pAc, pRangeName, pSentenceText);
		}
		
		return newPi;
	}
	
	public String identityKey() {
		return getId();
	}
	
	public String getId() {
		//Lazy initialised
		if (this.id == null) {
			this.id = retrieveInstanceName() + ":" + this.relatedProperty.identityKey();
		}
		
		return this.id;
	}
	
	public CeInstance getRelatedInstance() {
		return this.relatedInstance;
	}
	
	public void setRelatedInstance(ActionContext pAc, CeInstance pRelInst) {
		if (this.relatedInstance == null) {
			this.relatedInstance = pRelInst;
		} else {
			reportError("Attempt to set related instance (" + pRelInst + ") when an existing related instance (" + this.relatedInstance.toString() + ") is already specified (for '" + toString() + "')", pAc);
		}
	}

	public String retrieveInstanceName() {
		String result = "";
		
		if (this.relatedInstance != null) {
			result = this.relatedInstance.getInstanceName();
		}
		
		return result;
	}
	
	public String getClauseVariableId() {
		return this.clauseVariableId;
	}
	
	public void setClauseVariableId(String pVarId) {
		this.clauseVariableId = pVarId;
	}
	
	public CeProperty getRelatedProperty() {
		return this.relatedProperty;
	}
	
	public String getPropertyName() {
		String result = "";
		
		if (this.relatedProperty != null) {
			result = this.relatedProperty.getPropertyName();
		} else {
			result = "<unknown property name - no related property>";
		}
		
		return result;
	}
	
	private CeConcept getPropertyRangeConcept() {
		CeConcept result = null;
		
		if (this.relatedProperty != null) {
			result = this.relatedProperty.getRangeConcept();
		}
		
		return result;
	}
	
	public boolean isSingleCardinality() {
		return this.relatedProperty.isSingleCardinality();
	}
	
	public boolean isSpecialOperatorPropertyInstance() {
		boolean result = false;
		
		if (this.relatedProperty != null) {
			result = this.relatedProperty.isSpecialOperatorProperty();
		}
		
		//Finally, check whether the value originally had quotes... if so then it is an equals property in the shorthand form
		if (!result) {
			if (hasSingleValue()) {
				CePropertyValue singlePv = getFirstPropertyValue();
				if (singlePv != null) {
					result = singlePv.hadQuotesOriginally();
				}
			}
		}
		
		return result;
	}
	
	public boolean hadQuotesOriginally(ActionContext pAc) {
		//TODO: This code is not ideal and should be replaced with a better version
		boolean result = false;
		
		if (hasSingleValue()) {
			CePropertyValue singPv = getFirstPropertyValue();
			result = singPv.hadQuotesOriginally();
		} else {
			reportWarning("Unable to determine default 'hadQuotesOriginally' value as more than one value for property instance '" + this.identityKey() + "' is specified (therefore 'false' is returned as default)", pAc);
			result = false;
		}
		
		return result;
	}

	public boolean isNegated(ActionContext pAc) {
		//TODO: Migrate this onto the property value
		boolean result = false;
		
		if (hasSingleValue()) {
			CePropertyValue singPv = getFirstPropertyValue();
			result = singPv.isNegated();
		} else {
			if (isReportDebug()) {
				reportDebug("Unable to determine default 'isNegated' value as more than one value for property instance '" + this.identityKey() + "' is specified (therefore 'false' is returned as default)", pAc);
			}
			result = false;
		}
		
		return result;
	}

	public CePropertyValue[] getPropertyValues() {
		return this.propertyValues;
	}
	
	public Collection<CePropertyValue> getUniquePropertyValues() {
		TreeMap<String, CePropertyValue> uniqueList = new TreeMap<String, CePropertyValue>();
		
		for (CePropertyValue thisPv : this.propertyValues) {
			uniqueList.put(thisPv.getRangeName() + ":" + thisPv.getValue(), thisPv);
		}
		
		return uniqueList.values();
	}

	public int countPropertyValues() {
		return this.propertyValues.length;
	}
	
	public int countUniquePropertyValues() {
		return getUniquePropertyValues().size();
	}

	public CePropertyValue getFirstPropertyValue() {
		CePropertyValue result = null;
		
		if (hasAnyValues()) {
			result = this.propertyValues[0];
		}
			
		return result;
	}
	
	public boolean alreadyHasValue(CePropertyValue pPv) {
		boolean result = false;
		
		for (CePropertyValue thisPv : this.propertyValues) {
			if (thisPv.getValue().equals(pPv.getValue())) {
				result = true;
			}
		}
		
		return result;
	}
	
	public void addPropertyValue(ActionContext pAc, CePropertyValue pPv) {
		int currLen = 0;
		
		if (this.relatedProperty.isSingleCardinality()) {
			if (hasAnyValues()) {
				if (!alreadyHasValue(pPv)) {
					reportError("Attempting to add a new value (" + pPv.getValue() + ") to a single cardinality property (" + getPropertyName() + ") that already has value '" + getSingleOrFirstValue() + "' for instance named '" + retrieveInstanceName() + "'", pAc);
				}
			}
		}
		
		currLen = this.propertyValues.length;
		CePropertyValue[] newArray = new CePropertyValue[currLen + 1];
		System.arraycopy(this.propertyValues, 0, newArray, 0, currLen);
		this.propertyValues = newArray;

		this.propertyValues[currLen] = pPv;
	}

	private void setInitialValue(ActionContext pAc, String pValue, String pRangeName, boolean pHadQuotes, boolean pIsNegated, CeSentence pSen) {
		CePropertyValue propVal = CePropertyValue.createUsing(pAc, pValue, pRangeName, pHadQuotes, pIsNegated, pSen);
		addPropertyValue(pAc, propVal);
	}
	
	public String getSingleOrFirstValue() {
		String result =  "";
		
		CePropertyValue tgtPropVal = getFirstPropertyValue();
		
		if (tgtPropVal != null) {
			result = tgtPropVal.getValue();
		}
		
		return result;
	}

	public void setSingleValue(ActionContext pAc, String pValue) {
		CePropertyValue tgtVal = getFirstPropertyValue();
		
		if (tgtVal != null) {
			tgtVal.setValue(pAc, pValue);
		}
	}

	public String getSingleOrFirstRangeName() {
		String result =  "";
		
		CePropertyValue tgtPropVal = getFirstPropertyValue();
		
		if (tgtPropVal != null) {
			result = tgtPropVal.getRangeName();
		}
		
		return result;
	}

	public CeConcept getSingleOrFirstRangeConcept(ActionContext pAc) {
		return pAc.getModelBuilder().getConceptNamed(pAc, getSingleOrFirstRangeName());
	}
	
	public long getSingleOrFirstCreationDate() {
		long result = NO_TS;
		
		CePropertyValue tgtPropVal = getFirstPropertyValue();
		
		if (tgtPropVal != null) {
			result = tgtPropVal.getCreationDate();
		}
		
		return result;
	}

	public ArrayList<String> getValueListWithDuplicates() {
		ArrayList<String> result = null;
		
		if (hasAnyValues()) {
			result = new ArrayList<String>();
			
			for (CePropertyValue thisPv : this.propertyValues) {
				result.add(thisPv.getValue());
			}
		}
		
		return result;
	}

	public HashSet<CeInstance> getValueInstanceList(ActionContext pAc) {
		HashSet<CeInstance> result = null;
		
		if (hasAnyValues()) {
			result = new HashSet<CeInstance>();
			
			for (CePropertyValue thisPv : this.propertyValues) {
				CeInstance thisInst = thisPv.getValueInstance(pAc);
				
				if (thisInst != null) {
					result.add(thisInst);
				} else {
					reportWarning("Could not locate instance named '" + thisPv.getValue() + "' when processing " + this.toString(), pAc);
				}
			}
		}

		return result;
	}

	public ArrayList<CeInstance> getValueInstanceListWithDuplicates(ActionContext pAc) {
		ArrayList<CeInstance> result = null;

		if (hasAnyValues()) {
			result = new ArrayList<CeInstance>();

			for (CePropertyValue thisPv : this.propertyValues) {
				result.add(thisPv.getValueInstance(pAc));
			}
		}

		return result;
	}

	public HashSet<String> getValueListInTimestampRange(long pStartTs, long pEndTs) {
		HashSet<String> result = null;
		
		if (hasAnyValues()) {
			result = new HashSet<String>();
			
			for (CePropertyValue thisPv : this.propertyValues) {
				if (thisPv.isInTimestampRange(pStartTs, pEndTs)) {
					result.add(thisPv.getValue());
				}
			}
		}
		
		return result;
	}
	
	public HashSet<String> getValueList() {
		//Return only unique values (i.e. filter out duplicates)
		HashSet<String> result = new HashSet<String>();

		if (hasAnyValues()) {
			for (CePropertyValue thisPv : this.propertyValues) {
				result.add(thisPv.getValue());
			}
		}
		
		return result;
	}
	
	public ArrayList<CeSentence> getSentenceList() {
		ArrayList<CeSentence> senList = new ArrayList<CeSentence>();
		
		for (CePropertyValue thisVal : this.propertyValues) {
			senList.add(thisVal.getSentence());
		}
		
		return senList;
	}
	
	public boolean hasAnyValues() {
		return (this.propertyValues.length > 0);
	}
	
	public boolean hasSingleValue() {
		return (this.propertyValues.length == 1);
	}
	
	public boolean hasMoreThanOneValue() {
		return (this.propertyValues.length > 1);
	}

	private String formattedValueText() {
		String result = "";
		
		if (hasMoreThanOneValue()) {
			String commaSep = "";
			result += "{";
			for (String thisVal : getValueList()) {
				result += commaSep + "'" + thisVal + "'";
				commaSep = ", ";
			}
			result += "}";
		} else {
			result = "'" + getSingleOrFirstValue() + "'";
		}

		return result;
	}
	
	public String getSingleRangeName() {
		String result = "";
		CePropertyValue tgtVal = getFirstPropertyValue();
		
		if (tgtVal != null) {
			result = tgtVal.getRangeName();
		}
		
		return result;
	}
	
	public ArrayList<String> getRangeNameList() {
		ArrayList<String> result = null;
		
		if (hasAnyValues()) {
			result = new ArrayList<String>();
			
			for (CePropertyValue thisPv : this.propertyValues) {
				result.add(thisPv.getRangeName());
			}
		}
		
		return result;
	}

	public long getSingleCreationDate() {
		long result = NO_TS;
		CePropertyValue tgtVal = getFirstPropertyValue();
		
		if (tgtVal != null) {
			result = tgtVal.getCreationDate();
		}
		
		return result;
	}
	
	private void testRangeMatch(ActionContext pAc, String pRangeName, String pSentenceText) {
		//Check that the range for this instance is a valid concept against what is defined in the model
		//i.e. the range for this property instance must match or be a child of the range for the property
		//definition in the model
		CeConcept modelRangeConcept = getPropertyRangeConcept();
		CeConcept instanceRangeConcept = pAc.getModelBuilder().getConceptNamed(pAc, pRangeName);

		if ((modelRangeConcept != null) && (instanceRangeConcept != null)) {
			if (!instanceRangeConcept.equalsOrHasParent(modelRangeConcept)) {
				reportError("Range mismatch detected. '" + modelRangeConcept.getConceptName() + "' (required) vs '" + instanceRangeConcept.getConceptName() + "' (stated) in sentence: " + pSentenceText, pAc);
			}
		} else {
			reportError("Unable to perform range test as model and/or instance concept cannot be located.  For sentence: " + pSentenceText, pAc);
		}
	}
	
	public boolean hasValue(String pVal) {
		boolean result = false;
		
		break_position:
		for (CePropertyValue thisPv : this.propertyValues) {
			result = thisPv.getValue().equals(pVal);
			if (result) {
				break break_position;
			}
		}
		
		return result;
	}

	@Override
	public int hashCode() {
		return identityKey().hashCode();
	}

	@Override
	public int compareTo(CePropertyInstance pOtherPi) {
		return identityKey().compareTo(pOtherPi.identityKey());
	}

	@Override
	public String toString() {
		String result = "";
		
		if (this.relatedProperty != null) {
			result = "property:{" + this.relatedProperty.toString() + "}";
		} else {
			result = "property:{null}";
		}
		
		result += ", range='" + getPropertyRangeConcept() + "'";

		if (this.relatedProperty.isSingleCardinality()) {
			result += ", value=" + formattedValueText();
		} else {
			result += ", values=" + formattedValueText();
		}
		
		return result;
	}

}
