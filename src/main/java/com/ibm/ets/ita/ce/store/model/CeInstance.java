package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.NL;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.encodeForCe;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;

public class CeInstance extends CeModelEntity {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private CeSentence[] secondarySentences = new CeSentence[0];
	private CeConcept[] directConcepts = new CeConcept[0];
	private CePropertyInstance[] propertyInstances = new CePropertyInstance[0];
	private CePropertyInstance[] referringPropertyInstances = new CePropertyInstance[0];

	private CeProperty[] cachedAllProperties = new CeProperty[0];

	private CeInstance() {
		//This is private to ensure that new instances can only be created via the static methods
	}

	public static CeInstance createInstanceNamed(ActionContext pAc, String pInstName) {
		CeInstance newInst = new CeInstance();

		if (pInstName.trim().isEmpty()) {
			reportWarning("Attempt to create instance with empty name", pAc);
		}

		newInst.name = pAc.getModelBuilder().getCachedStringValueLevel1(pInstName);
		
		pAc.getIndexedEntityAccessor().clearInstanceNameCache();

		return newInst;
	}

	public String getInstanceName() {
		return this.name;
	}

	public String ceEncodedInstanceName() {
		return encodeForCe(this.name);
	}

	public HashSet<CeConcept> listAllConcepts() {
		HashSet<CeConcept> result = new HashSet<CeConcept>();

		for (CeConcept dirCon : this.directConcepts) {
			result.add(dirCon);
		}

		result.addAll(getInheritedConcepts());

		return result;
	}

	public ArrayList<CeProperty> retrieveAllProperties(ActionContext pAc) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		if (pAc.getCeConfig().cacheInstanceProperties()) {
			if (this.cachedAllProperties.length == 0) {
				ArrayList<CeProperty> allProps = calculateAllProperties();

				if (!allProps.isEmpty()) {
					this.cachedAllProperties = new CeProperty[allProps.size()];

					int ctr = 0;
					for (CeProperty thisProp : allProps) {
						this.cachedAllProperties[ctr++] = thisProp;
					}
				}
			}

			result = this.calculateAllProperties();
		} else {
			result = calculateAllProperties();
		}

		return result;
	}

	private ArrayList<CeProperty> calculateAllProperties() {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		for (CeConcept thisConcept : listAllConcepts()) {
			for (CeProperty thisProp : thisConcept.calculateAllProperties().values()) {
				if (!result.contains(thisProp)) {
					result.add(thisProp);
				}
			}
		}

		return result;
	}

	public void addConceptAndParents(CeConcept pConcept) {
		addDirectConcept(pConcept);
	}

	public CeConcept[] getDirectConcepts() {
		return this.directConcepts;
	}

	public int countDirectConcepts() {
		return this.directConcepts.length;
	}

	public ArrayList<String> calculateAllDirectConceptNames() {
		ArrayList<String> result = new ArrayList<String>();

		for (CeConcept thisConcept : getDirectConcepts()) {
			result.add(thisConcept.getConceptName());
		}

		return result;
	}

	public ArrayList<String> calculateAllInheritedConceptNames(ArrayList<String> pExclusions) {
		ArrayList<String> result = calculateAllInheritedConceptNames();

		if (pExclusions != null) {
			for (String exCon : pExclusions) {
				result.remove(exCon);
			}
		}

		return result;
	}

	public ArrayList<String> calculateAllInheritedConceptNames() {
		ArrayList<String> result = new ArrayList<String>();

		if (!getInheritedConcepts().isEmpty()) {
			for (CeConcept thisConcept : getInheritedConcepts()) {
				result.add(thisConcept.getConceptName());
			}
		}

		return result;
	}

	private void addDirectConcept(CeConcept pConcept) {
		if (!isDirectConcept(pConcept)) {
			int currLen = 0;

			currLen = this.directConcepts.length;
			CeConcept[] newArray = new CeConcept[currLen + 1];
			System.arraycopy(this.directConcepts, 0, newArray, 0, currLen);

			this.directConcepts = newArray;
			this.directConcepts[currLen] = pConcept;
		}
	}

	public HashSet<CeConcept> getInheritedConcepts() {
		HashSet<CeConcept> result = new HashSet<CeConcept>();

		for (CeConcept dirCon : getDirectConcepts()) {
			for (CeConcept inhCon : dirCon.retrieveAllParents(false)) {
				result.add(inhCon);
			}
		}

		return result;
	}

	public int countInheritedConcepts() {
		return getInheritedConcepts().size();
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

	public CePropertyInstance[] getPropertyInstances() {
		return this.propertyInstances;
	}

	public ArrayList<CePropertyInstance> getObjectPropertyInstances() {
		ArrayList<CePropertyInstance> result = new ArrayList<CePropertyInstance>();

		for (CePropertyInstance thisPi : this.propertyInstances) {
			if (thisPi.getRelatedProperty().isObjectProperty()) {
				result.add(thisPi);
			}
		}

		return result;
	}

	public int countPropertyInstances() {
		return this.propertyInstances.length;
	}

	public CePropertyInstance getPropertyInstanceNamed(String pName) {
		CePropertyInstance result = null;

		for (CePropertyInstance thisPi : this.propertyInstances) {
			if (result == null) {
				if (thisPi.getPropertyName().equals(pName)) {
					result = thisPi;
				}
			}
		}

		return result;
	}

	public CePropertyInstance getPropertyInstanceForProperty(CeProperty pProp) {
		return getPropertyInstanceNamed(pProp.getPropertyName());
	}

	public void addPropertyInstance(CePropertyInstance pPi) {
		if (!hasPropertyInstance(pPi)) {
			int currLen = 0;

			currLen = this.propertyInstances.length;
			CePropertyInstance[] newArray = new CePropertyInstance[currLen + 1];
			System.arraycopy(this.propertyInstances, 0, newArray, 0, currLen);
			this.propertyInstances = newArray;

			this.propertyInstances[currLen] = pPi;
		}
	}

	public boolean hasPropertyInstance(CePropertyInstance pPi) {
		boolean result = false;

		break_position:
		for (CePropertyInstance thisPi : this.propertyInstances) {
			if (thisPi == pPi) {
				result = true;
				break break_position;
			}
		}

		return result;
	}

	public boolean hasPropertyInstanceForPropertyNamed(String pPropName) {
		boolean result = false;

		break_position:
		for (CePropertyInstance thisPi : this.propertyInstances) {
			if (thisPi.getPropertyName().equals(pPropName)) {
				result = true;
				break break_position;
			}
		}

		return result;
	}

	public CePropertyInstance[] getReferringPropertyInstances() {
		return this.referringPropertyInstances;
	}

	public int countReferringPropertyInstances() {
		return this.referringPropertyInstances.length;
	}

	public boolean hasReferringPropertyInstances() {
		return this.referringPropertyInstances.length > 0;
	}

	public CePropertyInstance getReferringPropertyInstanceNamed(String pName) {
    CePropertyInstance result = null;

    for (CePropertyInstance thisPi : this.referringPropertyInstances) {
      if (thisPi.getPropertyName().equals(pName)) {
        result = thisPi;
        break;
      }
    }

    return result;
  }

  public CePropertyInstance[] getReferringPropertyInstancesNamed(String pName) {
    ArrayList<CePropertyInstance> result = new ArrayList<CePropertyInstance>();

    for (CePropertyInstance thisPi : this.referringPropertyInstances) {
      if (thisPi.getPropertyName().equals(pName)) {
        result.add(thisPi);
      }
    }

    return result.toArray(new CePropertyInstance[]{});
  }

	public CePropertyInstance getReferringPropertyInstanceForProperty(CeProperty pProp) {
		return getReferringPropertyInstanceNamed(pProp.getPropertyName());
	}

	public void addReferringPropertyInstance(CePropertyInstance pPi) {
		if (!hasReferringPropertyInstance(pPi)) {
			int currLen = 0;

			currLen = this.referringPropertyInstances.length;
			CePropertyInstance[] newArray = new CePropertyInstance[currLen + 1];
			System.arraycopy(this.referringPropertyInstances, 0, newArray, 0, currLen);
			this.referringPropertyInstances = newArray;

			this.referringPropertyInstances[currLen] = pPi;
		}
	}

	public boolean hasReferringPropertyInstance(CePropertyInstance pPi) {
		boolean result = false;

		break_position:
		for (CePropertyInstance thisPi : this.referringPropertyInstances) {
			if (thisPi == pPi) {
				result = true;
				break break_position;
			}
		}

		return result;
	}

	public boolean hasReferringPropertyInstanceForPropertyNamed(String pPropName) {
		boolean result = false;

		break_position:
		for (CePropertyInstance thisPi : this.referringPropertyInstances) {
			if (thisPi.getPropertyName().equals(pPropName)) {
				result = true;
				break break_position;
			}
		}

		return result;
	}

	public int countSecondarySentences() {
		return this.secondarySentences.length;
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

		return result;
	}

	@Override
	public ArrayList<CeSentence> listSecondarySentences() {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();

		for (CeSentence thisSen : getSecondarySentences()) {
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

	public boolean isConcept(CeConcept pConcept) {
		//TODO: Improve this
		return listAllConcepts().contains(pConcept);
	}

	public boolean isDirectConcept(CeConcept pConcept) {
		for (CeConcept dirCon : this.directConcepts) {
			if (dirCon == pConcept) {
				return true;
			}
		}

		return false;
	}

	public boolean isConceptNamed(ActionContext pAc, String pConName) {
		boolean result = false;
		CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, pConName);

		if (tgtCon != null) {
			result = isConcept(tgtCon);
		} else {
			reportWarning("Concept '" + pConName + "' could not be located [isConceptNamed]", pAc);
			result = false;
		}

		return result;
	}
	
	//DSB 21/09/2015 - Added
	//Return true only if there is just one direct concept and that concept (or a parent)
	//matches the specified concept name.
	public boolean isOnlyConceptNamed(ActionContext pAc, String pConName) {
		boolean result = false;
		
		if (getDirectConcepts().length == 1) {
			result = isConceptNamed(pAc, pConName);
		}
		
		return result;
	}

	public CeInstance getSingleInstanceFromPropertyNamed(ActionContext pAc, String pPropName) {
		CeInstance result = null;
		CePropertyInstance targetPi = getPropertyInstanceNamed(pPropName);

		if (targetPi != null) {
			String instName = targetPi.getSingleOrFirstValue();

			result = pAc.getModelBuilder().getInstanceNamed(pAc, instName);

			if (result == null) {
				reportWarning("Unable to locate instance named '" + instName + "' from '" + targetPi.getPropertyName() + "' during sentence classification", pAc);
			}
		}

		return result;
	}

	public String getSingleValueFromPropertyNamed(String pPropName) {
		String result = "";
		CePropertyInstance targetPi = getPropertyInstanceNamed(pPropName);

		if (targetPi != null) {
			result = targetPi.getSingleOrFirstValue();
		}

		return result;
	}

	public String getLatestValueFromPropertyNamed(String pPropName) {
		String result = "";
		long latestDate = 0;
		CePropertyInstance targetPi = getPropertyInstanceNamed(pPropName);

		if (targetPi != null) {
			for (CePropertyValue thisVal : targetPi.getPropertyValues()) {
				if (thisVal.getCreationDate() > latestDate) {
					result = thisVal.getValue();
					latestDate = thisVal.getCreationDate();
				}
			}
		}

		return result;
	}

	public ArrayList<CeInstance> getInstanceListFromPropertyNamed(ActionContext pAc, String pPropName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		CePropertyInstance targetPi = getPropertyInstanceNamed(pPropName);

		if (targetPi != null) {
			if (targetPi.getRelatedProperty().isObjectProperty()) {
				result = new ArrayList<CeInstance>(targetPi.getValueInstanceList(pAc));
			}
		}

		return result;
	}

	public ArrayList<String> getValueListFromPropertyNamed(String pPropName) {
		ArrayList<String> result = null;
		CePropertyInstance targetPi = getPropertyInstanceNamed(pPropName);

		if (targetPi != null) {
			result = new ArrayList<String>(targetPi.getValueList());
		} else {
			result = new ArrayList<String>();
		}

		return result;
	}

	public HashSet<CeInstance> getAllRelatedInstances(ActionContext pAc) {
		HashSet<CeInstance> result = new HashSet<CeInstance>();

		for (CePropertyInstance thisPi : getObjectPropertyInstances()) {
			result.addAll(thisPi.getValueInstanceList(pAc));
		}

		return result;
	}

	public HashSet<CeInstance> getAllRelatedInstances(ActionContext pAc, String[] pLimRels) {
		HashSet<CeInstance> result = new HashSet<CeInstance>();
		
		ArrayList<String> relNames = new ArrayList<String>();
		
		if ((pLimRels != null) && (pLimRels.length > 0)) {
			for (String thisRel : pLimRels) {
				relNames.add(thisRel);
			}
		}

		for (CePropertyInstance thisPi : getObjectPropertyInstances()) {
			String relName = thisPi.getPropertyName();

			if (relNames.isEmpty() || relNames.contains(relName)) {
				result.addAll(thisPi.getValueInstanceList(pAc));
			}
		}

		return result;
	}

	public void renameInstanceTo(ActionContext pAc, String pNewName) {
		String origInstName = getInstanceName();
		//First set the instance name property
		this.name = pAc.getModelBuilder().getCachedStringValueLevel1(pNewName);

		//Finally iterate through all instances that could have related properties with this reference
		ArrayList<CeProperty> refProps = new ArrayList<CeProperty>();
		for (CeConcept thisConcept : getDirectConcepts()) {
			for (CeProperty thisProp : pAc.getModelBuilder().listAllPropertiesReferringTo(thisConcept)) {
				refProps.add(thisProp);
			}
		}

		for (CeProperty refProp : refProps) {
			CeConcept domCon = refProp.getDomainConcept();
			for (CeInstance possInst : pAc.getModelBuilder().listAllInstancesForConcept(domCon)) {
				CePropertyInstance possPi = possInst.getPropertyInstanceForProperty(refProp);
				for (CePropertyValue thisPv : possPi.getPropertyValues()) {
					String thisVal = thisPv.getValue();
					if (thisVal.equals(origInstName)) {
						thisPv.setValue(pAc, pNewName);
					}
				}
			}
		}
	}

	public String generateCe(ActionContext pAc) {
		String result = "";
		String connector = " that" + NL;
		ArrayList<String> doneProps = new ArrayList<String>();

		String encInstName = encodeForCe(getInstanceName());

		//First process the direct concepts
		for (CeConcept thisConcept : getDirectConcepts()) {
			String encConceptName = encodeForCe(thisConcept.getConceptName());
			if (result.isEmpty()) {
				//The first concept
				result += "there is " + thisConcept.conceptQualifier() + " " + encConceptName + " named '" + encInstName + "'";
			} else {
				result += connector + "  is " + thisConcept.conceptQualifier() + " " + encConceptName;
				connector = " and" + NL;
			}
		}

		//Now process the properties
		for (CeProperty thisProp : retrieveAllProperties(pAc)) {
			//Ensure that each property is processed only once
			//This is done by property name being the UID, and will ensure that any inherited instances
			//of the same property don't have their values written more than once.
			if (!doneProps.contains(thisProp.getPropertyName())) {
				String encPropName = encodeForCe(thisProp.getPropertyName());
				String encRangeName = encodeForCe(thisProp.getRangeConceptName());
				String rangeText = "";

				if (thisProp.isObjectProperty()) {
					rangeText = "the " + encRangeName;
				}

				CePropertyInstance thisPi = getPropertyInstanceForProperty(thisProp);

				if (thisPi != null) {
					doneProps.add(thisProp.getPropertyName());
					for (String thisVal : thisPi.getValueList()) {
						String encVal = encodeForCe(thisVal);
						if (thisProp.isVerbSingular()) {
							result += connector + "  " + encPropName + " " + rangeText + " '" + encVal + "'";
						} else {
							result += connector + "  has " + rangeText + "'" + encVal + "' as " + encPropName;
						}
						connector = " and" + NL;
					}
				}
			}
		}

		//Now process any annotations
		//TODO: Annotations not yet implemented for instances

		result += "." + NL + NL;

		return result;
	}

	public ArrayList<CeConcept> getAllLeafConcepts() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();
		CeConcept[] dirCons = getDirectConcepts();
		
		for (CeConcept thisCon : dirCons) {
			if (!thisCon.hasAnyChildren()) {
				//If a concept has no children then it is a leaf concept
				result.add(thisCon);
			} else {
				//If a concept has children but none of them are directly
				//stated concepts then it is a leaf concept
				boolean keep = true;

				for (CeConcept kid : thisCon.retrieveAllChildren(false)) {
					for (CeConcept dirCon : dirCons) {
						if (kid.equals(dirCon)) {
							keep = false;
							break;
						}
					}
				}
				
				if (keep) {
					result.add(thisCon);
				}
			}
		}

		return result;
	}

	public String getFirstLeafConceptName() {
		ArrayList<CeConcept> leafCons = getAllLeafConcepts();

		if (!leafCons.isEmpty()) {
			return leafCons.get(0).getConceptName();
		}

		return "";
	}

	public String getFirstLeafConceptNameExcluding(String pExcConName) {
		ArrayList<CeConcept> leafCons = getAllLeafConcepts();
		String result = "";

		if (!leafCons.isEmpty()) {
			break_position:
			for (CeConcept thisCon : leafCons) {
				if (!thisCon.getConceptName().equals(pExcConName)) {
					result = thisCon.getConceptName();
					break break_position;
				}
			}
		}

		return result;
	}

	public CeConcept getFirstLeafConceptExcludingConcept(CeConcept pExCon) {
		ArrayList<CeConcept> leafCons = getAllLeafConcepts();
		CeConcept result = null;

		if (!leafCons.isEmpty()) {
			break_position:
			for (CeConcept thisCon : leafCons) {
				if (!thisCon.equalsOrHasParent(pExCon)) {
					result = thisCon;
					break break_position;
				}
			}
		}

		return result;
	}

	@Override
	public boolean equals(Object pObj) {
		boolean result = false;

		if (pObj!=null && pObj.hashCode()==hashCode()) {
			//The hash codes match so these objects MAY be the same.  Now check on the strings
			CeInstance pInst = (CeInstance)pObj;
			result = (identityKey().equals(pInst.identityKey()));
		}

		return result;
	}

	@Override
	public int hashCode() {
		return identityKey().hashCode();
	}

	// renderable concept/thing properties 

	private String getLabelPropertyName(ActionContext pAc){
		return getRenderablePropertyValue(pAc, HelperConcept.PROP_LAPN);
	}

	private String getIconPropertyName(ActionContext pAc){
		return getRenderablePropertyValue(pAc, HelperConcept.PROP_IPN);
	}

	private String getIconFileName(ActionContext pAc){
		return getRenderablePropertyValue(pAc, HelperConcept.PROP_IFN);
	}

	private String getRenderablePropertyValue(ActionContext pAc, String propertyName){
		ArrayList<String> resultList = getRenderablePropertyValues(pAc, propertyName);
		String result = null;

		if (resultList != null && resultList.size() > 0){
			result = resultList.get(0);
		}
		
		return result;
	}

	private ArrayList<String> getRenderablePropertyValues(ActionContext pAc, String propertyName){
		// try to get the instance property first
		CePropertyInstance property = getPropertyInstanceNamed(propertyName);
		if (property != null){
			ArrayList<String> valueList = new ArrayList<String>(property.getValueList());
			if (valueList != null && valueList.size() > 0){
				return valueList;
			}
		}

		// if no instance property try the meta-model by looking at all
		// concepts for this instance. take the value from the 
		// first concept that has it
		// TODO - what happens if more than one concept has the values
		//		set and they conflict? Really need to take the most
		//		derived concept values first so here am looking
		//		at derived concepts first. 
		for (CeConcept concept : this.directConcepts){
			CeInstance conceptMetaModel = concept.retrieveMetaModelInstance(pAc);

			if ( conceptMetaModel != null){
				property = conceptMetaModel.getPropertyInstanceNamed(propertyName);
				
				if (property != null){
					ArrayList<String> valueList = new ArrayList<String>(property.getValueList());
					if (valueList != null && valueList.size() > 0){
						return valueList;
					}
				}
			}
		}

		for (CeConcept concept : getInheritedConcepts()){
			CeInstance conceptMetaModel = concept.retrieveMetaModelInstance(pAc);
			
			if ( conceptMetaModel != null){
				property = conceptMetaModel.getPropertyInstanceNamed(propertyName);
				
				if (property != null){
					ArrayList<String> valueList = new ArrayList<String>(property.getValueList());
					if (valueList != null && valueList.size() > 0){
						return valueList;
					}
				}
			}
		}

		return null;
	}

	public boolean isInTimestampRange(long pStartTs, long pEndTs) {
		boolean result = true;

		if (pStartTs != ModelBuilder.NO_TS) {
			result = getCreationDate() >= pStartTs;
		}

		if (result) {
			if (pEndTs != ModelBuilder.NO_TS) {
				result = getCreationDate() <= pEndTs;
			}
		}

		return result;
	}

	public boolean deleteSentence(CeSentence pSen) {
		boolean result = false;

		if (hasPrimarySentence(pSen)) {
			result = true;
			removePrimarySentence(pSen);
		}

		if (hasSecondarySentence(pSen)) {
			result = true;
			removeSecondarySentence(pSen);
		}

		return result;
	}

	public String calculateLabel(ActionContext pAc) {
		String result = "";
		String labelPropName = getLabelPropertyName(pAc);

		if ((labelPropName != null) && (!labelPropName.isEmpty())) {
			result = getSingleValueFromPropertyNamed(labelPropName);
		}

		if (result.isEmpty()) {
			result = getInstanceName();
		}

		return result;
	}

	public String calculateIconFilename(ActionContext pAc) {
		String result = "";
		String iconPropName = getIconPropertyName(pAc);

		if ((iconPropName != null) && (!iconPropName.isEmpty())) {
			result = getSingleValueFromPropertyNamed(iconPropName);
		} else {
			//If the icon property name was not set try a hardcoded icon filename for the instance
			result = getIconFileName(pAc);
		}

		return result;
	}

	public boolean isMetaModelInstance() {
		//Returns true if any concept that this instance is is a meta-model concept
		for (CeConcept thisCon : this.listAllConcepts()) {
			if (thisCon.isMetaModelConcept()) {
				return true;
			}
		}

		return false;
	}

	public boolean isOnlyMetaModelInstance() {
		//Returns true if all concepts that this instance is are meta-model concepts
		for (CeConcept thisCon : this.directConcepts) {
			if (!thisCon.isMetaModelConcept()) {
				return false;
			}
		}

		return true;
	}

	public String formattedDirectConceptNames() {
		String result = "";

		for (CeConcept thisCon : getDirectConcepts()) {
			if (!result.isEmpty()) {
				result += ", ";
			}

			result += thisCon.getConceptName();
		}
		
		return result;
	}

	public HashSet<CePropertyInstance> listAllReferringPropertyInstances(ActionContext pAc, String pPropName) {
		//TODO: Major performance issue... I must improve this
		//Maybe by tracking references of the instance directly?
		HashSet<CePropertyInstance> result = new HashSet<CePropertyInstance>();

		if (!this.getInstanceName().isEmpty()) {
			for (CeInstance thisInst : pAc.getModelBuilder().listAllInstances()) {
				CePropertyInstance thisPi = thisInst.getPropertyInstanceNamed(pPropName);

				if (thisPi != null) {
					if (thisPi.getRelatedProperty().isObjectProperty()) {
						if (thisPi.getValueInstanceList(pAc).contains(this)) {
							result.add(thisPi);
						}
					}
				}
			}
		}

		return result;
	}

	public HashSet<CeSource> listAllDefiningSources() {
		HashSet<CeSource> result = new HashSet<CeSource>();

		for (CeSentence thisSen : getPrimarySentences()) {
			result.add(thisSen.getSource());
		}

		return result;
	}
	
	public String getFirstInstanceIdentifier(ActionContext pAc) {
		String result = null;
		ArrayList<String> ids = getInstanceIdentifiers(pAc);

		if (!ids.isEmpty()) {
			result = ids.get(0);
		}

		if (result == null) {
			result = getInstanceName();
		}

		return result;
	}

	public ArrayList<String> getInstanceIdentifiers(ActionContext pAc) {
		ArrayList<String> result = new ArrayList<String>();
		
		//Only add the instance name if no other identifiers found
		if (!isSeparatelyIdentified(pAc)) {
			result.add(getInstanceName());
		}

		for (CePropertyInstance thisPi : getPropertyInstances()) {
			CeProperty relProp = thisPi.getRelatedProperty();
			CeInstance mmInst = relProp.getMetaModelInstance(pAc);
			
			//TODO: Anonymise this property name
			if (mmInst.isConceptNamed(pAc, "identification property concept")) {
				for (String thisVal : thisPi.getValueList()) {
					result.add(thisVal);
				}
			}
		}

		//If there are no identifiers found then add the instance name
		if (result.isEmpty()) {
			result.add(getInstanceName());
		}
		
		return result;
	}
	
	private boolean isSeparatelyIdentified(ActionContext pAc) {
		boolean result = false;

		for (CeConcept dirCon : getDirectConcepts()) {
			ArrayList<CeInstance> mmList = dirCon.retrieveMetaModelInstances(pAc, null);
			
			for (CeInstance mmInst : mmList) {
				//TODO: Anonymise this concept name
				if (mmInst.isConceptNamed(pAc, "separately identified concept")) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	public String formattedInstanceIdentifier(ActionContext pAc) {
		String firstId = getFirstInstanceIdentifier(pAc);
		String instName = getInstanceName();
		String result = null;

		if (!firstId.equals(instName)) {
			result = firstId + " (" + instName + ")";
		} else {
			result = instName;
		}

		return result;
	}

	public boolean hasSameValuesAs(ActionContext pAc, CeInstance pOtherInst, ArrayList<String> pPropNames) {
		boolean result = true;

		for (CePropertyInstance thisPi : this.propertyInstances) {
			CeProperty relProp = thisPi.getRelatedProperty();
			boolean carryOn = true;

			if ((pPropNames != null) && (!pPropNames.isEmpty())) {
				if (!pPropNames.contains(relProp.getPropertyName())) {
					carryOn = false;
				}
			}
			
			if (carryOn) {
				if (relProp.isDatatypeProperty()) {
					ArrayList<String> localVals = new ArrayList<String>(thisPi.getValueList());
					ArrayList<String> otherVals = pOtherInst.getValueListFromPropertyNamed(thisPi.getPropertyName());
					
					if (localVals.size() == otherVals.size()) {
						if (localVals.containsAll(otherVals) && localVals.containsAll(otherVals)) {
							//A match
						} else {
							result = false;
							break;
						}
					} else {
						result = false;
						break;					
					}
				} else {
					ArrayList<CeInstance> localInsts = new ArrayList<CeInstance>(thisPi.getValueInstanceList(pAc));
					ArrayList<CeInstance> otherInsts = pOtherInst.getInstanceListFromPropertyNamed(pAc, thisPi.getPropertyName());
					
					//TODO: Consider a more thorough test where the values of different
					//properties are just tested rather than an exact match on the whole
					//CeInstance?
					if (!localInsts.containsAll(otherInsts) || !localInsts.containsAll(otherInsts)) {
						result = false;
						break;
					}
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return "CeInstance (" + this.name + ")";
	}

}