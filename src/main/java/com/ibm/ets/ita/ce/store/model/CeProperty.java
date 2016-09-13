package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_PASTTENSE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_PLURAL;
import static com.ibm.ets.ita.ce.store.names.CeNames.RANGE_VALUE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COLON;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.encodeForCe;

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;

public class CeProperty extends CeModelEntity {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	// TODO: Need to link property to sentence
	private String idKey = null;
	private String rangeConceptName = "";
	protected int ceStyle = -1;
	protected int ceCardinality = -1;
	private boolean statedProperty = false;
	private boolean inferredProperty = false;
	private CeConcept domainConcept = null;
	private CeConcept assertedDomainConcept = null;
	private CeConcept rangeConcept = null;
	private CeProperty statedSourceProperty = null;
	private CeProperty[] inferredPropertyList = null;

	public static final int STYLE_FN = 0;
	public static final int STYLE_VS = 1;
	private static final String STYLENAME_FN = "functional noun";
	private static final String STYLENAME_VS = "verb singular";
	private static final String TYPENAME_DT = "datatype";
	private static final String TYPENAME_OBJ = "object";

	public static final int RANGETYPE_DATATYPE = 0;
	public static final int RANGETYPE_OBJECT = 1;

	public static final int CARDINALITY_SINGLE = 0;
	public static final int CARDINALITY_SINGLE_EXACT = 1;
	public static final int CARDINALITY_MULTI = 2;

	protected CeProperty() {
		// This is private to ensure that new instances can only be created via
		// the various static methods
	}

	@Override
	public String identityKey() {
		return this.idKey;
	}

	@Override
	public String toString() {
		return "CeProperty (" + formattedFullPropertyName() + ")";
	}

	public static String calculateFullPropertyNameFor(String pDomainName, String pPropertyName, String pRangeName) {
		String rangeName;

		if ((pRangeName == null) || (pRangeName.isEmpty())) {
			rangeName = RANGE_VALUE;
		} else {
			rangeName = pRangeName;
		}

		return pDomainName + TOKEN_COLON + pPropertyName + TOKEN_COLON + rangeName;
	}

	public static CeProperty createOrRetrieveFnDatatypeProperty(ActionContext pAc, CeConcept pConcept, String pPropName,
			int pCeCardinality) {
		return createOrRetrieveProperty(pAc, pConcept, pPropName, STYLE_FN, pCeCardinality, null);
	}

	public static CeProperty createOrRetrieveVsDatatypeProperty(ActionContext pAc, CeConcept pConcept, String pPropName,
			int pCeCardinality) {
		return createOrRetrieveProperty(pAc, pConcept, pPropName, STYLE_VS, pCeCardinality, null);
	}

	public static CeProperty createOrRetrieveFnObjectProperty(ActionContext pAc, CeConcept pConcept, String pPropName,
			int pCeCardinality, CeConcept pRangeConcept) {
		return createOrRetrieveProperty(pAc, pConcept, pPropName, STYLE_FN, pCeCardinality, pRangeConcept);
	}

	public static CeProperty createOrRetrieveVsObjectProperty(ActionContext pAc, CeConcept pConcept, String pPropName,
			int pCeCardinality, CeConcept pRangeConcept) {
		return createOrRetrieveProperty(pAc, pConcept, pPropName, STYLE_VS, pCeCardinality, pRangeConcept);
	}

	private static CeProperty createOrRetrieveProperty(ActionContext pAc, CeConcept pConcept, String pPropName,
			int pCeStyle, int pCeCardinality, CeConcept pRangeConcept) {
		ModelBuilder mb = pAc.getModelBuilder();
		String rangeName = null;

		if (pRangeConcept == null) {
			rangeName = RANGE_VALUE;
		} else {
			rangeName = pRangeConcept.getConceptName();
		}

		String fullPropName = calculateFullPropertyNameFor(pConcept.getConceptName(), pPropName, rangeName);
		CeProperty result = mb.getPropertyFullyNamed(fullPropName);

		if (result == null) {
			// Create the new property
			result = createNewProperty(pAc, pConcept, pConcept, pPropName, pCeStyle, pCeCardinality, pRangeConcept,
					null);

			if (result != null) {
				// Add the property to the concept
				pConcept.attachDirectProperty(pAc, result);
			}
		}

		return result;
	}

	private static CeProperty createNewProperty(ActionContext pAc, CeConcept pDomainConcept, CeConcept pAssertedConcept,
			String pPropName, int pCeStyle, int pCeCardinality, CeConcept pRangeConcept,
			CeProperty pStatedSourceProperty) {
		CeProperty newProp = new CeProperty();
		String thisRangeName = "";

		if (pRangeConcept == null) {
			thisRangeName = RANGE_VALUE;
		} else {
			thisRangeName = pRangeConcept.getConceptName();
		}

		newProp.domainConcept = pDomainConcept;
		newProp.assertedDomainConcept = pAssertedConcept;
		newProp.name = pPropName;
		newProp.rangeConceptName = thisRangeName;
		newProp.ceStyle = pCeStyle;
		newProp.ceCardinality = pCeCardinality;
		newProp.rangeConcept = pRangeConcept;

		newProp.inferredPropertyList = null;

		newProp.calculateFullPropertyName();

		if (pStatedSourceProperty == null) {
			// This is a stated property since there is no stated source
			// property which this has been inferred from
			newProp.statedProperty = true;
			newProp.inferredProperty = false;
			newProp.statedSourceProperty = null;
		} else {
			// This is an inferred property
			newProp.statedProperty = false;
			newProp.inferredProperty = true;
			newProp.statedSourceProperty = pStatedSourceProperty;
			newProp.statedSourceProperty.addInferredProperty(newProp);
		}

		// Added by Dave Braines - 26/05/2015 - The property must not be saved
		// if validating only
		if (!pAc.isValidatingOnly()) {
			pAc.getModelBuilder().addProperty(newProp);

			// Ensure the property name cache is cleared
			pAc.getIndexedEntityAccessor().clearPropertyNameCaches();
		}

		return newProp;
	}

	public static boolean isSpecialPropertyName(String pPropName, String pConName) {
		boolean result = false;

		// Dave Braines 02/07/2014 - Due to fixes elsewhere the pConName may now
		// be null (which requires the same interpretation as RANGE_VALUE)
		if ((pConName == null) || (pConName.equals(RANGE_VALUE))) {
			result = CeSpecialProperty.isSpecialValueOperator(pPropName);
		}

		// Dave Braines 02/07/2014 - Always test for universal operators, even
		// when the range is value
		if (!result) {
			result = CeSpecialProperty.isSpecialUniversalOperator(pPropName);
		}

		return result;
	}

	public static boolean isDatatypeRangeName(String pRangeName) {
		return (pRangeName == null) || (pRangeName.isEmpty()) || pRangeName.equals(RANGE_VALUE);
	}

	public CeProperty cloneToGetInferredProperty(ActionContext pAc, CeConcept pDomainConcept) {
		// Create a new instance of CeProperty with most of the same properties
		// as this
		// instance but specifically override some using the specified passed
		// values
		CeProperty newProperty = CeProperty.createNewProperty(pAc, pDomainConcept, this.assertedDomainConcept,
				getPropertyName(), this.ceStyle, this.ceCardinality, this.rangeConcept, this);

		return newProperty;
	}

	public boolean isProperty() {
		return true;
	}

	public boolean isShadow() {
		return (getPrimarySentences().length == 0);
	}

	@SuppressWarnings("static-method")
	public boolean isSpecialOperatorProperty() {
		// This must not be a static method since it is overridden by the
		// subclass implementation
		// TODO: Improve this as it is confusing
		return false;
	}

	@Override
	public HashSet<CeSentence> listAllSentences() {
		HashSet<CeSentence> result = new HashSet<CeSentence>();

		for (CeSentence priSen : getPrimarySentences()) {
			result.add(priSen);
		}

		return result;
	}

	@Override
	public ArrayList<CeSentence> listSecondarySentences() {
		return new ArrayList<CeSentence>();
	}

	@Override
	public ArrayList<ArrayList<CeSentence>> listAllSentencesAsPair() {
		ArrayList<ArrayList<CeSentence>> senListPair = new ArrayList<ArrayList<CeSentence>>();

		senListPair.add(listPrimarySentences());
		senListPair.add(listSecondarySentences());

		return senListPair;
	}

	public void addInferredProperty(CeProperty pProperty) {
		if (!hasInferredProperty(pProperty)) {
			int currLen = 0;

			if (this.inferredPropertyList == null) {
				this.inferredPropertyList = new CeProperty[1];
				currLen = 0;
			} else {
				currLen = this.inferredPropertyList.length;
				CeProperty[] newArray = new CeProperty[currLen + 1];
				System.arraycopy(this.inferredPropertyList, 0, newArray, 0, currLen);

				this.inferredPropertyList = newArray;
			}

			this.inferredPropertyList[currLen] = pProperty;
		}
	}

	public boolean hasInferredProperty(CeProperty pProp) {
		boolean result = false;

		if (this.inferredPropertyList != null) {
			for (CeProperty thisProp : this.inferredPropertyList) {
				if (!result) {
					if (thisProp == pProp) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	public boolean hasInferredProperties() {
		return (getInferredProperties() != null) && (getInferredProperties().length > 0);
	}

	public boolean isStatedProperty() {
		return this.statedProperty;
	}

	public boolean isInferredProperty() {
		return this.inferredProperty;
	}

	public CeProperty[] getInferredProperties() {
		return this.inferredPropertyList;
	}

	public CeProperty getStatedSourceProperty() {
		return this.statedSourceProperty;
	}

	public String getPropertyName() {
		return this.name;
	}

	public String getRangeConceptName() {
		return this.rangeConceptName;
	}

	public int getCeStyle() {
		return this.ceStyle;
	}

	public String formattedCeStyle() {
		String result = null;

		if (this.ceStyle == STYLE_FN) {
			result = STYLENAME_FN;
		} else if (this.ceStyle == STYLE_VS) {
			result = STYLENAME_VS;
		} else {
			result = "Unknown CE style (" + Integer.toString(this.ceStyle) + ")";
		}

		return result;
	}

	public String formattedCeType() {
		String result = null;

		if (this.isDatatypeProperty()) {
			result = TYPENAME_DT;
		} else {
			result = TYPENAME_OBJ;
		}

		return result;
	}

	public int getCeCardinality() {
		return this.ceCardinality;
	}

	public CeConcept getDomainConcept() {
		return this.domainConcept;
	}

	public String calculateDomainConceptName() {
		String result = null;

		if (this.domainConcept != null) {
			result = this.domainConcept.getConceptName();
		} else {
			result = "";
		}

		return result;
	}

	public CeConcept getAssertedDomainConcept() {
		return this.assertedDomainConcept;
	}

	public String calculateAssertedDomainConceptName() {
		String result = null;

		if (this.assertedDomainConcept != null) {
			result = this.assertedDomainConcept.getConceptName();
		} else {
			result = "";
		}

		return result;
	}

	public CeConcept getRangeConcept() {
		return this.rangeConcept;
	}

	public boolean isObjectProperty() {
		return (this.rangeConcept != null);
	}

	public boolean isDatatypeProperty() {
		return !isObjectProperty();
	}

	public boolean isFunctionalNoun() {
		return this.ceStyle == STYLE_FN;
	}

	public boolean isVerbSingular() {
		return this.ceStyle == STYLE_VS;
	}

	public boolean isMultipleCardinality() {
		return this.ceCardinality == CARDINALITY_MULTI;
	}

	public boolean isSingleCardinality() {
		return (this.ceCardinality == CARDINALITY_SINGLE) || (this.ceCardinality == CARDINALITY_SINGLE_EXACT);
	}

	private String formattedRangeName() {
		String result = null;

		if (this.rangeConcept == null) {
			result = RANGE_VALUE;
		} else {
			result = this.rangeConcept.getConceptName();
		}

		return result;
	}

	public String formattedFullPropertyName() {
		return this.idKey;
	}

	private void calculateFullPropertyName() {
		String cName = null;

		if (this.domainConcept != null) {
			cName = this.domainConcept.getConceptName();
		} else {
			cName = RANGE_VALUE;
		}

		this.idKey = cName + TOKEN_COLON + getPropertyName() + TOKEN_COLON + formattedRangeName();
	}

	public String formattedAssertedPropertyName() {
		String cName = null;

		if (this.assertedDomainConcept != null) {
			cName = this.assertedDomainConcept.getConceptName();
		} else {
			cName = RANGE_VALUE;
		}

		return cName + TOKEN_COLON + getPropertyName() + TOKEN_COLON + formattedRangeName();
	}

	public String ceEncodedPropertyName() {
		return encodeForCe(formattedFullPropertyName());
	}

	public boolean hasSentence(CeSentence pSen) {
		boolean result = false;

		if (hasPrimarySentence(pSen)) {
			result = true;
		}

		return result;
	}

	public boolean hasOtherSentences(CeSentence pSen) {
		boolean result = false;

		for (CeSentence thisSen : listAllSentences()) {
			if (!result) {
				if (thisSen != pSen) {
					result = true;
				}
			}
		}

		return result;
	}

	public void deleteSentence(CeSentence pSen) {
		if (hasPrimarySentence(pSen)) {
			removePrimarySentence(pSen);
		}
	}

	public boolean equalsOrHasInferredProperty(CeProperty pProp) {
		boolean result = false;
		if (this.equals(pProp)) {
			result = true;
		} else {
			if (getInferredProperties() != null) {
				for (CeProperty infProp : getInferredProperties()) {
					if (infProp.equals(pProp)) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}

	public String pluralFormName(ActionContext pAc) {
		String result = null;
		CeProperty tgtProp = null;

		if (isInferredProperty()) {
			tgtProp = getStatedSourceProperty();
		} else {
			tgtProp = this;
		}

		CeInstance mmInst = pAc.getModelBuilder().getInstanceNamed(pAc, tgtProp.formattedFullPropertyName());

		if (mmInst != null) {
			result = mmInst.getSingleValueFromPropertyNamed(PROP_PLURAL);

			if ((result == null) || result.isEmpty()) {
				result = defaultPluralFormFor(tgtProp);
				// result = tgtProp.getPropertyName();
			}
		} else {
			// There is no meta-model instance, so return the normal name
			result = defaultPluralFormFor(tgtProp);
		}

		return result;
	}

	public String pastTenseName(ActionContext pAc) {
		String result = null;
		CeProperty tgtProp = null;

		if (isInferredProperty()) {
			tgtProp = getStatedSourceProperty();
		} else {
			tgtProp = this;
		}

		CeInstance mmInst = pAc.getModelBuilder().getInstanceNamed(pAc, tgtProp.formattedFullPropertyName());

		if (mmInst != null) {
			result = mmInst.getSingleValueFromPropertyNamed(PROP_PASTTENSE);

			if ((result == null) || result.isEmpty()) {
				result = tgtProp.getPropertyName();
			} else {
				result = defaultPastTenseFor(tgtProp);
			}
		} else {
			// There is no meta-model instance, so return the normal name
			result = defaultPastTenseFor(tgtProp);
		}

		return result;
	}

	private static String defaultPluralFormFor(CeProperty pProp) {
		return pProp.getPropertyName() + "s"; // TODO: Abstract this
	}

	private static String defaultPastTenseFor(CeProperty pProp) {
		String result = pProp.getPropertyName();

		if (result.endsWith("e")) { // TODO: Abstract these
			result += "d";
		} else {
			result += "ed";
		}

		return result;
	}

	public CeInstance getMetaModelInstance(ActionContext pAc) {
		CeInstance result = pAc.getModelBuilder().getInstanceNamed(pAc, this.formattedFullPropertyName());

		if (result == null) {
			result = pAc.getModelBuilder().getInstanceNamed(pAc, this.formattedAssertedPropertyName());
		}

		return result;
	}

}
