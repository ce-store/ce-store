package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_A;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AN;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COLON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_PASTTENSE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_PLURAL;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_THING;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.encodeForCe;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class CeConcept extends CeModelEntity {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private boolean qualifiedWithAn = false;
	private boolean isThing = false;

	//Sentence relationships
	private CeSentence[] secondarySentences = new CeSentence[0];

	//Direct model relationships
	private CeConcept[] directParents = new CeConcept[0];
	private CeConcept[] directChildren = new CeConcept[0];
	private CeProperty[] directProperties = new CeProperty[0];
	private CeConceptualModel[] conceptualModels = new CeConceptualModel[0];

	//Cached information
	private CeConcept[] cachedAllParents = new CeConcept[0];
	private CeConcept[] cachedAllChildren = new CeConcept[0];
	private CeProperty[] cachedInferredProperties = new CeProperty[0];
	private CeProperty[] cachedAllNamedProperties = new CeProperty[0];

	private CeConcept() {
		//This is private to ensure that new instances can only be created via the various static methods
	}

	public static CeConcept createOrRetrieveConceptNamed(ActionContext pAc, String pConceptName) {
		CeConcept result = pAc.getModelBuilder().getConceptNamed(pAc, pConceptName);

		if (result == null) {
			//Create the new concept
			result = createNewConceptNamed(pAc, pConceptName);
		}

		return result;
	}

	private static CeConcept createNewConceptNamed(ActionContext pAc, String pConceptName) {
		CeConcept newConcept = new CeConcept();

		newConcept.name = pAc.getModelBuilder().getCachedStringValueLevel1(pConceptName);	
		newConcept.isThing = (newConcept.name.equals(CON_THING));

		if (HelperConcept.hasReservedName(newConcept)) {
			reportError("Attempting to use reserved concept name '" + pConceptName + "'", pAc);
			newConcept = null;
		} else {
			if (!pAc.isValidatingOnly()) {
				//Save the concept
				pAc.getModelBuilder().saveConcept(newConcept);
			}
			
			//Ensure the concept name cache is cleared
			pAc.getIndexedEntityAccessor().clearConceptNameCache();
		}
		
		return newConcept;
	}

	public boolean isConcept() {
		return true;
	}

	public String getConceptName() {
		return this.name;
	}

	public String ceEncodedConceptName() {
		return encodeForCe(this.name);
	}

	public boolean isQualifiedWithAn() {
		return this.qualifiedWithAn;
	}
	
	public void markAsQualifiedWithAn() {
		this.qualifiedWithAn = true;
	}

	public String conceptQualifier() {
		String result = null;

		if (this.qualifiedWithAn) {
			result = TOKEN_AN;
		} else {
			result = TOKEN_A;
		}

		return result;
	}

	public CeSentence[] getSecondarySentences() {
		return this.secondarySentences;
	}

	public void addSecondarySentence(CeSentence pSen) {
		if (!hasSecondarySentence(pSen)) {
			int currLen = 0;

			currLen = this.secondarySentences.length;
			CeSentence[] newArray = new CeSentence[currLen + 1];
			System.arraycopy(this.secondarySentences, 0, newArray, 0, currLen);
			this.secondarySentences = newArray;

			this.secondarySentences[currLen] = pSen;
		}
	}

	public void removeSecondarySentence(CeSentence pSen) {
		if (hasSecondarySentence(pSen)) {
			CeSentence[] newArray = new CeSentence[this.secondarySentences.length - 1];

			int ctr = 0;
			for (CeSentence secSen : this.secondarySentences) {
				if (secSen != pSen) {
					newArray[ctr++] = secSen;
				}
			}

			this.secondarySentences = newArray;
		}
	}

	public boolean hasSecondarySentence(CeSentence pSen) {
		boolean result = false;

		for (CeSentence secSen : this.secondarySentences) {
			if (!result) {
				if (secSen == pSen) {
					result = true;
				}
			}
		}

		return result;
	}

	public CeConcept[] getDirectParents() {
		return this.directParents;
	}

	public ArrayList<CeConcept> listDirectParents() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

		for (CeConcept thisParent : getDirectParents()) {
			result.add(thisParent);
		}

		return result;
	}

	private void addDirectParent(CeConcept pParent) {
		if (!hasDirectParent(pParent)) {
			int currLen = 0;

			currLen = this.directParents.length;
			CeConcept[] newArray = new CeConcept[currLen + 1];
			System.arraycopy(this.directParents, 0, newArray, 0, currLen);
			this.directParents = newArray;

			this.directParents[currLen] = pParent;
		}
	}

	public boolean hasDirectParents() {
		return (this.directParents != null) && (this.directParents.length > 0);
	}

	public boolean hasDirectParent(CeConcept pParent) {
		boolean result = false;

		for (CeConcept dirParent : this.directParents) {
			if (!result) {
				if (dirParent == pParent) {
					result = true;
				}
			}
		}

		return result;
	}

	public CeConcept[] getDirectChildren() {
		return this.directChildren;
	}

	public ArrayList<CeConcept> listDirectChildren() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

		for (CeConcept thisChild : getDirectChildren()) {
			result.add(thisChild);
		}

		return result;
	}

	private void addDirectChild(CeConcept pChild) {
		if (!hasDirectChild(pChild)) {
			int currLen = 0;

			currLen = this.directChildren.length;
			CeConcept[] newArray = new CeConcept[currLen + 1];
			System.arraycopy(this.directChildren, 0, newArray, 0, currLen);
			this.directChildren = newArray;
	
			this.directChildren[currLen] = pChild;
		}
	}

	public boolean hasDirectChildren() {
		return (this.directChildren != null) && (this.directChildren.length > 0);
	}

	public boolean hasDirectChild(CeConcept pChild) {
		boolean result = false;

		for (CeConcept dirChild : this.directChildren) {
			if (!result) {
				if (dirChild == pChild) {
					result = true;
				}
			}
		}

		return result;
	}

	private void removeDirectChild(CeConcept pChild) {
		if (hasDirectChild(pChild)) {
			CeConcept[] newArray = new CeConcept[this.directChildren.length - 1];

			int ctr = 0;
			for (CeConcept dirChild : this.directChildren) {
				if (dirChild != pChild) {
					newArray[ctr++] = dirChild;
				}
			}

			this.directChildren = newArray;
		}
	}

	public CeProperty[] getDirectProperties() {
		return this.directProperties;
	}

	public boolean hasDirectProperties() {
		return (this.directProperties != null) && (this.directProperties.length > 0);
	}

	private void addDirectProperty(CeProperty pProperty) {
		if (!hasDirectProperty(pProperty)) {
			int currLen = 0;

			currLen = this.directProperties.length;
			CeProperty[] newArray = new CeProperty[currLen + 1];
			System.arraycopy(this.directProperties, 0, newArray, 0, currLen);
			this.directProperties = newArray;

			this.directProperties[currLen] = pProperty;
		}
	}

	private void removeDirectProperty(CeProperty pProp) {
		if (hasDirectProperty(pProp)) {
			CeProperty[] newArray = new CeProperty[this.directProperties.length - 1];

			int ctr = 0;
			for (CeProperty dirProp : this.directProperties) {
				if (dirProp != pProp) {
					newArray[ctr++] = dirProp;
				}
			}

			this.directProperties = newArray;
		}
	}

	public boolean hasDirectProperty(CeProperty pProp) {
		boolean result = false;

		for (CeProperty dirProp : this.directProperties) {
			if (!result) {
				if (dirProp == pProp) {
					result = true;
				}
			}
		}

		return result;
	}

	public CeConceptualModel[] getConceptualModels() {
		return this.conceptualModels;
	}

	public ArrayList<CeConceptualModel> listConceptualModels() {
	  ArrayList<CeConceptualModel> result = new ArrayList<CeConceptualModel>();

	  for (CeConceptualModel thisModel : getConceptualModels()) {
	    result.add(thisModel);
	  }

	  return result;
	}

	public void addConceptualModel(CeConceptualModel pCm) {
		if (!hasConceptualModel(pCm)) {
			int currLen = 0;

			currLen = this.conceptualModels.length;
			CeConceptualModel[] newArray = new CeConceptualModel[currLen + 1];
			System.arraycopy(this.conceptualModels, 0, newArray, 0, currLen);
			this.conceptualModels = newArray;

			this.conceptualModels[currLen] = pCm;
		}
	}

//	private void removeConceptualModel(CeConceptualModel pCm) {
//		if (hasConceptualModel(pCm)) {
//			CeConceptualModel[] newArray = new CeConceptualModel[this.conceptualModels.length - 1];
//
//			int ctr = 0;
//			for (CeConceptualModel thisCm : this.conceptualModels) {
//				if (thisCm != pCm) {
//					newArray[ctr++] = pCm;
//				}
//			}
//
//			this.conceptualModels = newArray;
//		}
//	}

	public boolean hasConceptualModel(CeConceptualModel pCm) {
		boolean result = false;

		for (CeConceptualModel thisCm : this.conceptualModels) {
			if (!result) {
				if (thisCm == pCm) {
					result = true;
				}
			}
		}

		return result;
	}

	public ArrayList<CeConcept> retrieveAllParents(boolean pUseCache) {
		ArrayList<CeConcept> result = null;

		if (!pUseCache) {
			//Don't use the cache, so ensure it is refreshed
			clearParentCache();
			result = calculateAllParentsWithNoCache();
		} else {
			//Use the cache
			if (this.cachedAllParents.length == 0) {
				result = calculateAllParentsWithNoCache();

				if ((result != null) && (!result.isEmpty())) {
					this.cachedAllParents = new CeConcept[result.size()];

					int ctr = 0;
					for (CeConcept thisParent : result) {
						this.cachedAllParents[ctr++] = thisParent;
					}
				}
			} else {
				result = new ArrayList<CeConcept>();

				for (CeConcept thisCon : this.cachedAllParents) {
					result.add(thisCon);
				}
			}
		}

		return result;
	}

	private void clearParentCache() {
		this.cachedAllParents = new CeConcept[0];
	}

	public ArrayList<CeConcept> retrieveAllChildren(boolean pUseCache) {
		ArrayList<CeConcept> result = null;

		if (!pUseCache) {
			//Don't use the cache, so ensure it is refreshed
			clearChildCache();
			result = calculateAllChildrenWithNoCache();
		} else {
			//Use the cache
			if (this.cachedAllChildren.length == 0) {
				result = calculateAllChildrenWithNoCache();

				if ((result != null) && (!result.isEmpty())) {
					this.cachedAllChildren = new CeConcept[result.size()];

					int ctr = 0;
					for (CeConcept thisChild : result) {
						this.cachedAllChildren[ctr++] = thisChild;
					}
				}
			} else {
				result = new ArrayList<CeConcept>();

				for (CeConcept thisCon : this.cachedAllChildren) {
					result.add(thisCon);
				}
			}
		}

		return result;
	}

	private void clearChildCache() {
		this.cachedAllChildren = new CeConcept[0];
	}

	public CeProperty[] retrieveInferredProperties() {
		//TODO: This should be cached properly
		return this.cachedInferredProperties;
	}

	public boolean hasInferredProperties() {
		return this.cachedInferredProperties.length > 0;
	}

	private void addInferredProperty(CeProperty pProp) {
		if (!hasInferredProperty(pProp)) {
			int currLen = 0;

			currLen = this.cachedInferredProperties.length;
			CeProperty[] newArray = new CeProperty[currLen + 1];
			System.arraycopy(this.cachedInferredProperties, 0, newArray, 0, currLen);
			this.cachedInferredProperties = newArray;

			this.cachedInferredProperties[currLen] = pProp;
		}
	}

	public boolean hasInferredProperty(CeProperty pProp) {
		boolean result = false;

		for (CeProperty thisProp : this.cachedInferredProperties) {
			if (!result) {
				if (thisProp.formattedFullPropertyName().equals(pProp.formattedFullPropertyName())) {
					result = true;
				}
			}
		}

		return result;
	}

	public void removeInferredProperty(CeProperty pProp) {
		if (hasInferredProperty(pProp)) {
			CeProperty[] newArray = new CeProperty[this.cachedInferredProperties.length - 1];

			int ctr = 0;
			for (CeProperty infProp : this.cachedInferredProperties) {
				if (infProp != pProp) {
					newArray[ctr++] = infProp;
				}
			}

			this.cachedInferredProperties = newArray;
		}
	}

	public CeProperty[] retrieveAllNamedProperties() {
		//TODO: This should be cached properly
		return this.cachedAllNamedProperties;
	}

	private void addNamedProperty(CeProperty pProp) {
		if (!hasNamedProperty(pProp)) {
			int currLen = 0;

			currLen = this.cachedAllNamedProperties.length;
			CeProperty[] newArray = new CeProperty[currLen + 1];
			System.arraycopy(this.cachedAllNamedProperties, 0, newArray, 0, currLen);
			this.cachedAllNamedProperties = newArray;

			this.cachedAllNamedProperties[currLen] = pProp;
		}
	}

	public boolean hasNamedProperty(CeProperty pProp) {
		boolean result = false;

		for (CeProperty thisProp : this.cachedAllNamedProperties) {
			if (!result) {
				if (thisProp.formattedFullPropertyName().equals(pProp.formattedFullPropertyName())) {
					result = true;
				}
			}
		}

		return result;
	}

	public void removeNamedProperty(CeProperty pProp) {
		if (hasNamedProperty(pProp)) {
			CeProperty[] newArray = new CeProperty[this.cachedAllNamedProperties.length - 1];

			int ctr = 0;
			for (CeProperty namedProp : this.cachedAllNamedProperties) {
				if (namedProp != pProp) {
					newArray[ctr++] = namedProp;
				}
			}

			this.cachedAllNamedProperties = newArray;
		}
	}

	@Override
	public HashSet<CeSentence> listAllSentences() {
		HashSet<CeSentence> result = new HashSet<CeSentence>();

		for (CeSentence priSen : getPrimarySentences()) {
			result.add(priSen);
		}

		for (CeSentence secSen : getSecondarySentences()) {
			result.add(secSen);
		}

		result.addAll(calculateAnnotationSentences());

		return result;
	}

	private ArrayList<CeConcept> calculateAllParentsWithNoCache() {
		HashSet<CeConcept> resultSet = new HashSet<CeConcept>();

		//TODO: Is caching still needed?
		for (CeConcept thisParent : this.directParents) {
			resultSet.add(thisParent);

			for (CeConcept thisGrandparent : thisParent.calculateAllParentsWithNoCache()) {
				resultSet.add(thisGrandparent);
			}
		}

		return new ArrayList<CeConcept>(resultSet);
	}

	private ArrayList<CeConcept> calculateAllChildrenWithNoCache() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

		//TODO: Is caching still needed?
		for (CeConcept thisChild : this.directChildren) {
			if (!result.contains(thisChild)) {
				result.add(thisChild);
			}

			for (CeConcept thisGrandchild : thisChild.calculateAllChildrenWithNoCache()) {
				if (!result.contains(thisGrandchild)) {
					result.add(thisGrandchild);
				}
			}
		}

		return result;
	}

	public boolean isParentOf(CeConcept pCandidate) {
		//TODO: Improve this
		return pCandidate.retrieveAllParents(false).contains(this);
	}

	public boolean isMetaModelConcept() {
		//TODO: Come up with a better solution... create a new parent to contain all metamodel concepts?
		return HelperConcept.metaModelConceptNames().contains(getConceptName());
	}

	public boolean hasAnyParents() {
		return (this.directParents.length > 0);
	}

	public boolean hasAnyChildren() {
		return (this.directChildren.length > 0);
	}

	private void checkForChildUsesOfDirectProperty(ActionContext pAc, CeProperty pProperty) {
		ArrayList<CeProperty> propsToRemove = new ArrayList<CeProperty>();

		for (CeConcept thisChild : retrieveAllChildren(false)) {
			for (CeProperty childProp : thisChild.getDirectProperties()) {
				if (childProp.getPropertyName().equals(pProperty.getPropertyName())) {
					if (childProp.getRangeConceptName().equals(pProperty.getRangeConceptName())) {
						//Remove the existing direct property...
						//A new inferred one will be created since this direct property exists on the parent concept
						String propAndRange = pProperty.getPropertyName() + TOKEN_COLON + pProperty.getRangeConceptName();
						reportWarning("Found existing child definition of property named '" + propAndRange + "' on concept named '" + thisChild.getConceptName() + "' is a duplication of the existing property definition of the same name on the parent concept named '" + getConceptName() + "'", pAc);
						propsToRemove.add(childProp);
					}
				}
			}
		}

		//Separately remove the properties to avoid timing issues
		for (CeProperty thisProp : propsToRemove) {
			thisProp.getDomainConcept().removeDirectProperty(thisProp);
		}
	}

	private boolean checkForExistingParentUseOfDirectProperty(ActionContext pAc, CeProperty pProperty) {
		boolean result = false;

		for (CeConcept thisParent : retrieveAllParents(false)) {
			for (CeProperty parentProp : thisParent.getDirectProperties()) {
				if (parentProp.getPropertyName().equals(pProperty.getPropertyName())) {
					if (parentProp.getRangeConceptName().equals(pProperty.getRangeConceptName())) {
						String propAndRange = pProperty.getPropertyName() + TOKEN_COLON + pProperty.getRangeConceptName();
						reportWarning("Definition of property named '" + propAndRange + "' on concept named '" + getConceptName() + "' is a duplication of the existing property definition of the same name on the parent concept '" + thisParent.getConceptName() + "'", pAc);
						result = true;
					}
				}
			}
		}

		return result;
	}

	public ArrayList<CeProperty> calculateAllDirectProperties(boolean pUseCache) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		//TODO: Cache for performance?
		for (CeProperty thisProp : this.directProperties) {
			result.add(thisProp);
		}

		for (CeConcept thisParent : retrieveAllParents(pUseCache)) {
			for (CeProperty thisProp : thisParent.getDirectProperties()) {
				result.add(thisProp);
			}
		}

		return result;
	}

	public TreeMap<String, CeProperty> calculateAllProperties() {
		//TODO: Cache for performance?
		TreeMap<String, CeProperty> result = new TreeMap<String, CeProperty>();

		for (CeProperty thisProp : this.directProperties) {
			result.put(thisProp.identityKey(), thisProp);
		}

		for (CeProperty thisProp : this.cachedInferredProperties) {
			result.put(thisProp.identityKey(), thisProp);
		}

		return result;
	}

	public ArrayList<CeProperty> calculatePropertiesNamed(String pName) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		for (CeProperty thisProp : retrieveAllNamedProperties()) {
			if (thisProp.getPropertyName().equals(pName)) {
				result.add(thisProp);
			}
		}

		return result;
	}

	public ArrayList<CeProperty> calculatePropertiesStartingWith(String pName) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		for (CeProperty thisProp : retrieveAllNamedProperties()) {
			if (thisProp.getPropertyName().startsWith(pName)) {
				result.add(thisProp);
			}
		}

		return result;
	}

	public CeProperty calculatePropertyNamedWithInheritance(ActionContext pAc, String pPropName, String pRangeName) {
		String fullPropName = CeProperty.calculateFullPropertyNameFor(getConceptName(), pPropName, pRangeName);
		CeProperty targetProperty = null;
		CeConcept rangeConcept = pAc.getModelBuilder().getConceptNamed(pAc, pRangeName);

		if (rangeConcept != null) {
			targetProperty = retrievePropertyFullyNamed(fullPropName);

			if (targetProperty == null) {
				break_position:
				for (CeConcept thisParent : rangeConcept.retrieveAllParents(false)) {
					fullPropName = CeProperty.calculateFullPropertyNameFor(getConceptName(), pPropName, thisParent.getConceptName());
					targetProperty = retrievePropertyFullyNamed(fullPropName);

					if (targetProperty != null) {
						break break_position;
					}
				}
			}
		} else {
			reportError("Unable to locate range concept named '" + pRangeName + "' when getting property with inheritance", pAc);
		}

		return targetProperty;
	}

	public CeProperty retrievePropertyFullyNamed(String pFullName) {
		CeProperty result = null;

		break_position:
		for (CeProperty thisProp : retrieveAllNamedProperties()) {
			if (thisProp.identityKey().equals(pFullName)) {
				result = thisProp;
				break break_position;
			}
		}

		return result;
	}

	public void createParent(ActionContext pAc, CeConcept pParent) {
		if (pParent != null) {
			if (!pParent.equals(this)) {
				if (hasParent(pParent)) {
					if (!HelperConcept.isThing(pParent)) {
						if (!hasDirectParent(pParent)) {
							//Re-statements of direct parents are allowed, but giving a concept a parent that it already has higher in the existing
							//hierarchy is questionable (unless it is a core concept in which case it will be a default)
							reportWarning("Unnecessary parent specified (" + pParent.getConceptName() + ") - is already a parent at a higher level, for concept named '" + getConceptName() + "'", pAc);
						}
					}
				}

				if (!retrieveAllChildren(false).contains(pParent)) {
					//Update the model to reflect the parent/child relationship
					createParentChildRelationship(pAc, pParent, this);
				} else {
					reportError("Attempt to create circular hierarchy!  The parent '" + pParent.getConceptName() + "' cannot be specified for the concept '" + getConceptName() + "' as the proposed parent is already a child.  This relationship has been ignored.", pAc);
				}
			} else {
				reportError("Invalid parent specified - is the same as itself, for concept named '" + getConceptName() + "'", pAc);
			}
		} else {
			reportError("Unexpected null parent encountered during createParent processing", pAc);
		}
	}

	protected void populateInheritedInferredPropertiesFor(ActionContext pAc, CeConcept pParent) {
		//Clone each parent property for this concept (it will store the property automatically)
		for (CeProperty thisProperty : pParent.calculateAllDirectProperties(false)) {
			cloneAndAddProperty(pAc, thisProperty);
		}

		//Also perform this activity for all current children
		for (CeConcept thisChild : this.directChildren) {
			thisChild.populateInheritedInferredPropertiesFor(pAc, this);
		}
	}

	private void cloneAndAddProperty(ActionContext pAc, CeProperty pProperty) {
		CeProperty newProp = pProperty.cloneToGetInferredProperty(pAc, this);
		attachInferredProperty(newProp);
	}

	public boolean hasParent(CeConcept pConcept) {
		boolean result = false;

		if (pConcept != null) {
			break_position:
			for (CeConcept thisParent : retrieveAllParents(false)) {
				if (thisParent.equals(pConcept)) {
					result = true;
					break break_position;
				}
			}
		}

		return result;
	}

	public boolean hasParentNamed(String pConName) {
		boolean result = false;

		break_position:
		for (CeConcept thisParent : retrieveAllParents(false)) {
			if (thisParent.getConceptName().equals(pConName)) {
				result = true;
				break break_position;
			}
		}

		return result;
	}

	public boolean equalsOrHasParent(CeConcept pConcept) {
		return this.equals(pConcept) || hasParent(pConcept);
	}
	
	public boolean hasCommonParentWith(CeConcept pConcept) {
		boolean result = false;

		for (CeConcept parCon : retrieveAllParents(true)) {
			if (!parCon.isThing) {
				if (pConcept.equalsOrHasParent(parCon)) {
					result = true;
				}
				if (parCon.equalsOrHasParent(pConcept)) {
					result = true;
				}
			}
		}

		return result;
	}

	public boolean equalsOrHasParentNamed(ActionContext pAc, String pConName) {
		ArrayList<String> conNames = new ArrayList<String>();
		conNames.add(pConName);
		
		return equalsOrHasParentNamed(pAc, conNames);
	}

	public boolean equalsOrHasParentNamed(ActionContext pAc, ArrayList<String> pConNames) {
		boolean result = false;

		for (String thisConName : pConNames) {
			CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, thisConName);

			if (tgtCon != null) {
				result = this.equalsOrHasParent(tgtCon);

				if (result) {
					break;
				}
			}
		}

		return result;
	}

	public boolean hasAnyChildrenWithSentencesOtherThan(CeSentence pSen) {
		boolean result = false;

		for (CeConcept thisCon : this.directChildren) {
			if (!result) {
				result = result || thisCon.hasOtherSentences(pSen);
			}
		}

		return result;
	}

	public boolean hasAnyInstances(ActionContext pAc) {
		return (pAc.getModelBuilder().getInstanceCountForConcept(this) > 0);
	}

	public boolean hasSentence(CeSentence pSen) {
		boolean result = false;

		if (hasPrimarySentence(pSen)) {
			result = true;
		}

		if (hasSecondarySentence(pSen)) {
			result = true;
		}

		if (calculateAnnotationSentences().contains(pSen)) {
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

		deleteSecondarySentence(pSen);
	}

	public void deleteSecondarySentence(CeSentence pSen) {
		if (calculateAnnotationSentences().contains(pSen)) {
			removeAnnotationUsingSentence(pSen);
		}

		if (hasSecondarySentence(pSen)) {
			removeSecondarySentence(pSen);
		}

	}

	public void deleteProperty(CeProperty pProp) {
		removeNamedProperty(pProp);
		removeDirectProperty(pProp);
		removeInferredProperty(pProp);
	}

	public CeInstance retrieveMetaModelInstance(ActionContext pAc){
		return pAc.getModelBuilder().getInstanceNamed(pAc, getConceptName());
	}

	public ArrayList<CeInstance> retrieveMetaModelInstances(ActionContext pAc, String pConName){
		ArrayList<CeInstance> mmInsts = new ArrayList<CeInstance>();

		CeInstance mmInst = retrieveMetaModelInstance(pAc);
		
		if ((pConName != null) && (!pConName.isEmpty())) {
			if (mmInst.isConceptNamed(pAc, pConName)) {
				mmInsts.add(mmInst);
			}
		} else {
			mmInsts.add(mmInst);
		}
		
		for (CeConcept parentCon : calculateAllParentsWithNoCache()) {
			CeInstance mmParentInst = parentCon.retrieveMetaModelInstance(pAc);
			
			if ((pConName != null) && (!pConName.isEmpty())) {
				if (mmParentInst.isConceptNamed(pAc, pConName)) {
					mmInsts.add(mmParentInst);
				}
			} else {
				mmInsts.add(mmParentInst);
			}
		}

		return mmInsts;
	}

	@Override
	public ArrayList<CeSentence> listSecondarySentences() {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();

		for (CeSentence thisSen : this.secondarySentences) {
			result.add(thisSen);
		}

		return result;
	}

	@Override
	public ArrayList<ArrayList<CeSentence>> listAllSentencesAsPair() {
		ArrayList<ArrayList<CeSentence>> senListPair = new ArrayList<ArrayList<CeSentence>>();

		senListPair.add(listPrimarySentences());
		senListPair.add(listSecondarySentences());

		return senListPair;
	}

	public int countSecondarySentences() {
		return this.secondarySentences.length;
}

	public void attachDirectParent(CeConcept pParent) {
		addDirectParent(pParent);

		//Also clear the cache
		clearParentCache();

		for (CeConcept thisChild : retrieveAllChildren(false)) {
			thisChild.clearParentCache();
		}
	}

	public void attachDirectChild(CeConcept pChild) {
		addDirectChild(pChild);

		//Also clear the cache
		clearChildCache();

		for (CeConcept thisParent : retrieveAllParents(false)) {
			thisParent.clearChildCache();
		}
	}

	public void deleteDirectChild(CeConcept pChild) {
		removeDirectChild(pChild);

		//Also clear the cache
		clearChildCache();

		for (CeConcept pCon : this.retrieveAllParents(false)) {
			pCon.clearChildCache();
		}
	}

	private void createParentChildRelationship(ActionContext pAc, CeConcept pParent, CeConcept pChild) {
		pParent.attachDirectChild(pChild);
		pChild.attachDirectParent(pParent);
		populateInheritedInferredPropertiesFor(pAc, pParent);
	}

	public ArrayList<String> calculateDirectParentNames() {
		ArrayList<String> result = new ArrayList<String>();

		for (CeConcept thisParent : getDirectParents()) {
			result.add(thisParent.getConceptName());
		}

		return result;
	}

	public ArrayList<String> calculateAllParentNames() {
		ArrayList<String> result = new ArrayList<String>();

		for (CeConcept thisParent : retrieveAllParents(false)) {
			result.add(thisParent.getConceptName());
		}

		return result;
	}

	public ArrayList<String> calculateDirectChildNames() {
		ArrayList<String> result = new ArrayList<String>();

		for (CeConcept thisChild : this.directChildren) {
			result.add(thisChild.getConceptName());
		}

		return result;
	}

	public ArrayList<String> calculateAllChildNames() {
		ArrayList<String> result = new ArrayList<String>();

		for (CeConcept thisChild : retrieveAllChildren(false)) {
			result.add(thisChild.getConceptName());
		}

		return result;
	}

	public ArrayList<String> calculateDirectPropertyNames() {
		ArrayList<String> result = new ArrayList<String>();

		for (CeProperty thisProp : this.directProperties) {
			result.add(thisProp.getPropertyName());
		}

		return result;
	}

	public ArrayList<String> calculateInheritedPropertyNames() {
		ArrayList<String> result = new ArrayList<String>();

		for (CeProperty thisProp : retrieveInferredProperties()) {
			result.add(thisProp.getPropertyName());
		}

		return result;
	}

	protected void attachDirectProperty(ActionContext pAc, CeProperty pProperty) {
		boolean alreadyExists = checkForExistingParentUseOfDirectProperty(pAc, pProperty);

		//Check for (and delete) any child uses of this direct property
		//Note that this must be done even if the direct property exists on a parent and
		//therefore won't be saved here... This can occur when a grand-parent has a property
		//and a child has that property, but the parent does not
		checkForChildUsesOfDirectProperty(pAc, pProperty);

		if (!alreadyExists) {
			addDirectProperty(pProperty);

			//Also add to allNamedProperties
			addNamedProperty(pProperty);

			//Ensure that all children have this new property added as inferred
			for (CeConcept thisChild : retrieveAllChildren(false)) {
				thisChild.cloneAndAddProperty(pAc, pProperty);
			}
		}
	}

	private void attachInferredProperty(CeProperty pProperty) {
		//TODO: This needs to be properly cached
		if (!hasDirectProperty(pProperty)) {
			//The inferred property is only added if it is not a primary key (id) and
			//is not an existing direct property
			addInferredProperty(pProperty);

			//Also add to allNamedProperties
			addNamedProperty(pProperty);
		}
	}

	public boolean isThing() {
		return this.isThing;
	}

	public CeInstance retrieveMetadataInstance(ActionContext pAc) {
		return pAc.getModelBuilder().getInstanceNamed(pAc, getConceptName());
	}

	public String pluralFormName(ActionContext pAc) {
		String result = null;
		CeInstance mmInst = pAc.getModelBuilder().getInstanceNamed(pAc, getConceptName());

		if (mmInst != null) {
			result = mmInst.getSingleValueFromPropertyNamed(PROP_PLURAL);

			if ((result == null) || result.isEmpty()) {
				result = defaultPlural();
			}
		} else {
			//There is no meta-model instance, so return the normal name
			result = defaultPlural();
		}

		return result;
	}

	public String pastTenseName(ActionContext pAc) {
		String result = null;
		CeInstance mmInst = pAc.getModelBuilder().getInstanceNamed(pAc, getConceptName());

		if (mmInst != null) {
			result = mmInst.getSingleValueFromPropertyNamed(PROP_PASTTENSE);

			if ((result == null) || result.isEmpty()) {
				result = defaultPastTense();
			}
		} else {
			//There is no meta-model instance, so return the normal name
			result = defaultPastTense();
		}

		return result;
	}

	private String defaultPlural() {
		return this.name + "s";	//TODO: Abstract this
	}

	private String defaultPastTense() {
		String result = null;
		
		if (this.name.endsWith("e")) {	//TODO: Abstract these
			result = this.name + "d";
		} else {
			result = this.name + "ed";
		}

		return result;
	}

	@Override
	public String toString() {
		return "CeConcept (" + this.name + ")";
	}

}
