package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_SVP;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_INST;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CONCEPT_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CREATED;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_DIR_CONCEPT_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ICON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INH_CONCEPT_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTANCE_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_LABEL;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_MAININST;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_NORM_CONCEPTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PRISEN_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROPRAT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROPTYPES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROPVALS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_REFINSTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RELINSTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SECSEN_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SHADOW;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STYLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_TYPE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.PROPTYPE_DATATYPE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.PROPTYPE_OBJECT;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_FULL;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_SUMMARY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.QueryHandler;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeWebInstance extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public CeWebInstance(ActionContext pAc) {
		super(pAc);
	}

	public CeStoreJsonObject generateFullDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, int pNumSteps,
			boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes, boolean pIsSmartMode) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();

		if (pNumSteps <= 0) {
			mainObj = normalFullDetailsJsonFor(pInst, pOnlyProps, pSuppPropTypes, pIsSmartMode);
		} else {
			mainObj.put(JSON_MAININST, normalFullDetailsJsonFor(pInst, pOnlyProps, pSuppPropTypes, pIsSmartMode));

			if (pRelInsts) {
				CeStoreJsonObject relInsts = new CeStoreJsonObject();
				relatedInstancesJsonFor(pInst, pNumSteps, 1, relInsts, pLimRels, pSuppPropTypes, pIsSmartMode);
				mainObj.put(JSON_RELINSTS, relInsts);
			}

			if (pRefInsts) {
				CeStoreJsonObject refInsts = new CeStoreJsonObject();
				referringInstancesJsonFor(pInst, pNumSteps, 1, refInsts, pLimRels, pSuppPropTypes, pIsSmartMode);
				mainObj.put(JSON_REFINSTS, refInsts);
			}
		}

		return mainObj;
	}

	private void relatedInstancesJsonFor(CeInstance pInst, int pNumSteps, int pDepth, CeStoreJsonObject pResult,
			String[] pLimRels, boolean pSuppPropTypes, boolean pIsSmartMode) {
		int thisDepth = pDepth + 1;

		for (CeInstance relInst : pInst.getAllRelatedInstances(this.ac, pLimRels)) {
			pResult.put(relInst.getInstanceName(), normalSummaryDetailsJsonFor(relInst, null, pSuppPropTypes, pIsSmartMode, null));

			if (thisDepth <= pNumSteps) {
				relatedInstancesJsonFor(relInst, pNumSteps, thisDepth, pResult, pLimRels, pSuppPropTypes, pIsSmartMode);
			}
		}
	}

	private void relatedInstancesMinimalJsonFor(CeInstance pInst, int pNumSteps, int pDepth, CeStoreJsonObject pResult,
			String[] pLimRels, String[] pOnlyProps, boolean pIsSmartMode) {
		int thisDepth = pDepth + 1;

		for (CeInstance relInst : pInst.getAllRelatedInstances(this.ac, pLimRels)) {
			pResult.put(relInst.getInstanceName(), normalMinimalDetailsJsonFor(relInst, pOnlyProps, pIsSmartMode));

			if (thisDepth <= pNumSteps) {
				relatedInstancesMinimalJsonFor(relInst, pNumSteps, thisDepth, pResult, pLimRels, pOnlyProps, pIsSmartMode);
			}
		}
	}

	private void relatedInstancesNormalisedJsonFor(CeInstance pInst, int pNumSteps, int pDepth,
			CeStoreJsonObject pResult, String[] pLimRels, String[] pOnlyProps, boolean pIsSmartMode) {
		int thisDepth = pDepth + 1;

		for (CeInstance relInst : pInst.getAllRelatedInstances(this.ac, pLimRels)) {
			pResult.put(relInst.getInstanceName(), normalNormalisedDetailsJsonFor(relInst, pOnlyProps, pIsSmartMode));

			if (thisDepth <= pNumSteps) {
				relatedInstancesNormalisedJsonFor(relInst, pNumSteps, thisDepth, pResult, pLimRels, pOnlyProps, pIsSmartMode);
			}
		}
	}

	private void referringInstancesJsonFor(CeInstance pInst, int pNumSteps, int pDepth, CeStoreJsonObject pResult,
			String[] pLimRels, boolean pSuppPropTypes, boolean pIsSmartMode) {
		int thisDepth = pDepth + 1;
		QueryHandler qh = new QueryHandler(this.ac);
		TreeMap<String, HashSet<CeInstance>> refResult = qh.listAllInstanceReferencesFor(pInst);
		ArrayList<String> relNames = new ArrayList<String>();

		if ((pLimRels != null) && (pLimRels.length > 0)) {
			for (String thisRel : pLimRels) {
				relNames.add(thisRel);
			}
		}

		for (String relPropName : refResult.keySet()) {
			if (relNames.isEmpty() || (relNames.contains(relPropName))) {
				HashSet<CeInstance> refInsts = refResult.get(relPropName);
				CeStoreJsonArray refArray = (CeStoreJsonArray) pResult.get(this.ac, relPropName);

				if (refArray == null) {
					refArray = new CeStoreJsonArray();
				}

				for (CeInstance refInst : refInsts) {
					refArray.add(normalSummaryDetailsJsonFor(refInst, null, pSuppPropTypes, pIsSmartMode, null));

					if (thisDepth <= pNumSteps) {
						referringInstancesJsonFor(refInst, pNumSteps, thisDepth, pResult, pLimRels, pSuppPropTypes, pIsSmartMode);
					}
				}

				pResult.put(relPropName, refArray);
			}
		}
	}

	private void referringInstancesMinimalJsonFor(CeInstance pInst, int pNumSteps, int pDepth,
			CeStoreJsonObject pResult, String[] pLimRels, String[] pOnlyProps, boolean pIsSmartMode) {
		int thisDepth = pDepth + 1;
		QueryHandler qh = new QueryHandler(this.ac);
		TreeMap<String, HashSet<CeInstance>> refResult = qh.listAllInstanceReferencesFor(pInst);
		ArrayList<String> relNames = new ArrayList<String>();

		if ((pLimRels != null) && (pLimRels.length > 0)) {
			for (String thisRel : pLimRels) {
				relNames.add(thisRel);
			}
		}

		for (String relPropName : refResult.keySet()) {
			if (relNames.isEmpty() || (relNames.contains(relPropName))) {
				HashSet<CeInstance> refInsts = refResult.get(relPropName);
				CeStoreJsonArray refArray = (CeStoreJsonArray) pResult.get(this.ac, relPropName);

				if (refArray == null) {
					refArray = new CeStoreJsonArray();
				}

				for (CeInstance refInst : refInsts) {
					refArray.add(normalMinimalDetailsJsonFor(refInst, null, pIsSmartMode));

					if (thisDepth <= pNumSteps) {
						referringInstancesMinimalJsonFor(refInst, pNumSteps, thisDepth, pResult, pLimRels, pOnlyProps, pIsSmartMode);
					}
				}

				pResult.put(relPropName, refArray);
			}
		}
	}

	private void referringInstancesNormalisedJsonFor(CeInstance pInst, int pNumSteps, int pDepth,
			CeStoreJsonObject pResult, String[] pLimRels, String[] pOnlyProps, boolean pIsSmartMode) {
		int thisDepth = pDepth + 1;
		QueryHandler qh = new QueryHandler(this.ac);
		TreeMap<String, HashSet<CeInstance>> refResult = qh.listAllInstanceReferencesFor(pInst);
		ArrayList<String> relNames = new ArrayList<String>();

		if ((pLimRels != null) && (pLimRels.length > 0)) {
			for (String thisRel : pLimRels) {
				relNames.add(thisRel);
			}
		}

		for (String relPropName : refResult.keySet()) {
			if (relNames.isEmpty() || (relNames.contains(relPropName))) {
				HashSet<CeInstance> refInsts = refResult.get(relPropName);
				CeStoreJsonArray refArray = (CeStoreJsonArray) pResult.get(this.ac, relPropName);

				if (refArray == null) {
					refArray = new CeStoreJsonArray();
				}

				for (CeInstance refInst : refInsts) {
					refArray.add(normalNormalisedDetailsJsonFor(refInst, null, pIsSmartMode));

					if (thisDepth <= pNumSteps) {
						referringInstancesNormalisedJsonFor(refInst, pNumSteps, thisDepth, pResult, pLimRels,
								pOnlyProps, pIsSmartMode);
					}
				}

				pResult.put(relPropName, refArray);
			}
		}
	}

	private CeStoreJsonObject normalFullDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, boolean pSuppPropTypes, boolean pIsSmartMode) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();
		ArrayList<String> onlyPropList = null;

		if (pOnlyProps != null) {
			onlyPropList = new ArrayList<String>(Arrays.asList(pOnlyProps));
		} else {
			onlyPropList = new ArrayList<String>();
		}

		putStringValueIn(mainObj, JSON_TYPE, JSONTYPE_INST);
		putStringValueIn(mainObj, JSON_STYLE, STYLE_FULL);
		putStringValueIn(mainObj, JSON_ID, pInst.getInstanceName());
		putLongValueIn(mainObj, JSON_CREATED, pInst.getCreationDate());
		putBooleanValueIn(mainObj, JSON_SHADOW, pInst.isShadowEntity());
		putStringValueIn(mainObj, JSON_LABEL, pInst.calculateLabel(this.ac));

		processAnnotations(pInst, mainObj);

		putStringValueIn(mainObj, JSON_ICON, pInst.calculateIconFilename(this.ac, null));

		ArrayList<String> dirConNames = pInst.calculateAllDirectConceptNames();
		putAllStringValuesIn(mainObj, JSON_DIR_CONCEPT_NAMES, dirConNames);
		putAllStringValuesIn(mainObj, JSON_INH_CONCEPT_NAMES, pInst.calculateAllInheritedConceptNames(dirConNames));

		CeStoreJsonObject propValsObj = new CeStoreJsonObject();
		CeStoreJsonObject propTypesObj = new CeStoreJsonObject();
		CeStoreJsonObject propRatObj = new CeStoreJsonObject();

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			if (isValidProperty(thisPi, onlyPropList)) {
				String keyPropName = thisPi.getRelatedProperty().getPropertyName();
				if (thisPi.isSingleCardinality()) {
					putAppropriateValueIn(this.ac, thisPi, propValsObj, keyPropName, thisPi.getSingleOrFirstValue(), pIsSmartMode);
				} else {
					CeProperty thisProp = thisPi.getRelatedProperty();
					HashSet<String> uvList = thisPi.getValueList();
					boolean doneProp = false;

					if (pIsSmartMode) {
						CeInstance mmInst = thisProp.getMetaModelInstance(this.ac);
						if (mmInst.isConceptNamed(this.ac, CON_SVP)) {
							String latestVal = pInst.getLatestValueFromPropertyNamed(thisProp.getPropertyName());
							putAppropriateValueIn(this.ac, thisPi, mainObj, keyPropName, latestVal, pIsSmartMode);
							doneProp = true;
						}
					}

					if (!doneProp) {
						putAllAppropriateValuesIn(this.ac, thisPi, propValsObj, keyPropName, uvList, pIsSmartMode);
					}

					CeStoreJsonObject valRatObj = new CeStoreJsonObject();

					for (String thisVal : uvList) {
						ArrayList<CeRationaleReasoningStep> ratList = this.ac.getModelBuilder()
								.getReasoningStepsForPropertyValue(pInst.getInstanceName(), thisPi.getPropertyName(),
										thisVal, false);

						if (!ratList.isEmpty()) {
							CeWebRationaleReasoningStep webRs = new CeWebRationaleReasoningStep(this.ac);
							valRatObj.put(thisVal, webRs.generateListFrom(ratList));
						}
					}

					if (!valRatObj.isEmpty()) {
						propRatObj.put(thisPi.getPropertyName(), valRatObj);
					}
				}

				if (thisPi.getRelatedProperty().isDatatypeProperty()) {
					putStringValueIn(propTypesObj, keyPropName, PROPTYPE_DATATYPE);
				} else {
					putStringValueIn(propTypesObj, keyPropName, PROPTYPE_OBJECT);
				}
			}
		}

		putObjectValueIn(mainObj, JSON_PROPVALS, propValsObj);

		if (!pSuppPropTypes) {
			putObjectValueIn(mainObj, JSON_PROPTYPES, propTypesObj);
		}

		if (!propRatObj.isEmpty()) {
			putObjectValueIn(mainObj, JSON_PROPRAT, propRatObj);
		}

		putIntValueIn(mainObj, JSON_PRISEN_COUNT, pInst.countPrimarySentences());
		putIntValueIn(mainObj, JSON_SECSEN_COUNT, pInst.countSecondarySentences());

		return mainObj;
	}

	public CeStoreJsonObject generateSummaryDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, int pNumSteps,
			boolean pRelInsts, boolean pRefInsts, String[] pLimInsts, boolean pSuppPropTypes, boolean pIsSmartMode) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();

		if (pNumSteps <= 0) {
			mainObj = normalSummaryDetailsJsonFor(pInst, pOnlyProps, pSuppPropTypes, pIsSmartMode, null);
		} else {
			mainObj.put(JSON_MAININST, normalSummaryDetailsJsonFor(pInst, pOnlyProps, pSuppPropTypes, pIsSmartMode, null));

			if (pRelInsts) {
				CeStoreJsonObject relInsts = new CeStoreJsonObject();
				relatedInstancesJsonFor(pInst, pNumSteps, 1, relInsts, pLimInsts, pSuppPropTypes, pIsSmartMode);
				mainObj.put(JSON_RELINSTS, relInsts);
			}

			if (pRefInsts) {
				CeStoreJsonObject refInsts = new CeStoreJsonObject();
				referringInstancesJsonFor(pInst, pNumSteps, 1, refInsts, pLimInsts, pSuppPropTypes, pIsSmartMode);
				mainObj.put(JSON_REFINSTS, refInsts);
			}
		}

		return mainObj;
	}

	public CeStoreJsonObject generateMinimalDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, int pNumSteps,
			boolean pRelInsts, boolean pRefInsts, String[] pLimInsts, boolean pIsSmartMode) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();

		if (pNumSteps <= 0) {
			mainObj = normalMinimalDetailsJsonFor(pInst, pOnlyProps, pIsSmartMode);
		} else {
			mainObj.put(JSON_MAININST, normalMinimalDetailsJsonFor(pInst, pOnlyProps, pIsSmartMode));

			if (pRelInsts) {
				CeStoreJsonObject relInsts = new CeStoreJsonObject();
				relatedInstancesMinimalJsonFor(pInst, pNumSteps, 1, relInsts, pLimInsts, pOnlyProps, pIsSmartMode);
				mainObj.put(JSON_RELINSTS, relInsts);
			}

			if (pRefInsts) {
				CeStoreJsonObject refInsts = new CeStoreJsonObject();
				referringInstancesMinimalJsonFor(pInst, pNumSteps, 1, refInsts, pLimInsts, pOnlyProps, pIsSmartMode);
				mainObj.put(JSON_REFINSTS, refInsts);
			}
		}

		return mainObj;
	}

	public CeStoreJsonObject generateNormalisedDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, int pNumSteps,
			boolean pRelInsts, boolean pRefInsts, String[] pLimInsts, boolean pIsSmartMode) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();

		if (pNumSteps <= 0) {
			mainObj = normalNormalisedDetailsJsonFor(pInst, pOnlyProps, pIsSmartMode);
		} else {
			mainObj.put(JSON_MAININST, normalNormalisedDetailsJsonFor(pInst, pOnlyProps, pIsSmartMode));

			if (pRelInsts) {
				CeStoreJsonObject relInsts = new CeStoreJsonObject();
				relatedInstancesNormalisedJsonFor(pInst, pNumSteps, 1, relInsts, pLimInsts, pOnlyProps, pIsSmartMode);
				mainObj.put(JSON_RELINSTS, relInsts);
			}

			if (pRefInsts) {
				CeStoreJsonObject refInsts = new CeStoreJsonObject();
				referringInstancesNormalisedJsonFor(pInst, pNumSteps, 1, refInsts, pLimInsts, pOnlyProps, pIsSmartMode);
				mainObj.put(JSON_REFINSTS, refInsts);
			}
		}

		return mainObj;
	}

	private CeStoreJsonObject normalSummaryDetailsJsonFor(CeInstance pInst, String[] pOnlyProps,
			boolean pSuppPropTypes, boolean pIsSmartMode, CeConcept pCon) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();
		ArrayList<String> onlyPropList = null;

		if (pOnlyProps != null) {
			onlyPropList = new ArrayList<String>(Arrays.asList(pOnlyProps));
		} else {
			onlyPropList = new ArrayList<String>();
		}

		putStringValueIn(mainObj, JSON_TYPE, JSONTYPE_INST);
		putStringValueIn(mainObj, JSON_STYLE, STYLE_SUMMARY);
		putStringValueIn(mainObj, JSON_ID, pInst.getInstanceName());
		putLongValueIn(mainObj, JSON_CREATED, pInst.getCreationDate());
		putBooleanValueIn(mainObj, JSON_SHADOW, pInst.isShadowEntity());
		putStringValueIn(mainObj, JSON_LABEL, pInst.calculateLabel(this.ac));

		processAnnotations(pInst, mainObj);

		putStringValueIn(mainObj, JSON_ICON, pInst.calculateIconFilename(this.ac, pCon));

		ArrayList<String> dirConNames = pInst.calculateAllDirectConceptNames();

		putAllStringValuesIn(mainObj, JSON_DIR_CONCEPT_NAMES, dirConNames);
		putAllStringValuesIn(mainObj, JSON_INH_CONCEPT_NAMES, pInst.calculateAllInheritedConceptNames(dirConNames));

		CeStoreJsonObject propValsObj = new CeStoreJsonObject();
		CeStoreJsonObject propTypesObj = new CeStoreJsonObject();

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			if (isValidProperty(thisPi, onlyPropList)) {
				String keyPropName = thisPi.getRelatedProperty().getPropertyName();
				if (thisPi.isSingleCardinality()) {
					putAppropriateValueIn(this.ac, thisPi, propValsObj, keyPropName, thisPi.getSingleOrFirstValue(), pIsSmartMode);
				} else {
					CeProperty thisProp = thisPi.getRelatedProperty();
					boolean doneProp = false;

					if (pIsSmartMode) {
						CeInstance mmInst = thisProp.getMetaModelInstance(this.ac);
						if (mmInst.isConceptNamed(this.ac, CON_SVP)) {
							String latestVal = pInst.getLatestValueFromPropertyNamed(thisProp.getPropertyName());
							putAppropriateValueIn(this.ac, thisPi, propValsObj, keyPropName, latestVal, pIsSmartMode);
							doneProp = true;
						}
					}

					if (!doneProp) {
						putAllAppropriateValuesIn(this.ac, thisPi, propValsObj, keyPropName, thisPi.getValueList(), pIsSmartMode);
					}
				}

				if (thisPi.getRelatedProperty().isDatatypeProperty()) {
					putStringValueIn(propTypesObj, keyPropName, PROPTYPE_DATATYPE);
				} else {
					putStringValueIn(propTypesObj, keyPropName, PROPTYPE_OBJECT);
				}
			}
		}

		putObjectValueIn(mainObj, JSON_PROPVALS, propValsObj);

		if (!pSuppPropTypes) {
			putObjectValueIn(mainObj, JSON_PROPTYPES, propTypesObj);
		}

		putIntValueIn(mainObj, JSON_PRISEN_COUNT, pInst.countPrimarySentences());
		putIntValueIn(mainObj, JSON_SECSEN_COUNT, pInst.countSecondarySentences());

		return mainObj;
	}

	private CeStoreJsonObject normalMinimalDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, boolean pIsSmartMode) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();
		ArrayList<String> onlyPropList = null;

		if (pOnlyProps != null) {
			onlyPropList = new ArrayList<String>(Arrays.asList(pOnlyProps));
		} else {
			onlyPropList = new ArrayList<String>();
		}

		putStringValueIn(mainObj, JSON_ID, pInst.getInstanceName());

		ArrayList<String> conNames = new ArrayList<String>();

		for (CeConcept thisCon : pInst.listAllConcepts()) {
			if (!thisCon.isThing()) {
				conNames.add(thisCon.getConceptName());
			}
		}

		putAllStringValuesIn(mainObj, JSON_CONCEPT_NAMES, conNames);

		CeStoreJsonObject propValsObj = new CeStoreJsonObject();

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			if (isValidProperty(thisPi, onlyPropList)) {
				String keyPropName = thisPi.getRelatedProperty().getPropertyName();
				if (thisPi.isSingleCardinality()) {
					putAppropriateValueIn(this.ac, thisPi, propValsObj, keyPropName, thisPi.getSingleOrFirstValue(), pIsSmartMode);
				} else {
					CeProperty thisProp = thisPi.getRelatedProperty();
					boolean doneProp = false;

					if (pIsSmartMode) {
						CeInstance mmInst = thisProp.getMetaModelInstance(this.ac);
						if (mmInst.isConceptNamed(this.ac, CON_SVP)) {
							String latestVal = pInst.getLatestValueFromPropertyNamed(thisProp.getPropertyName());
							putAppropriateValueIn(this.ac, thisPi, propValsObj, keyPropName, latestVal, pIsSmartMode);
							doneProp = true;
						}
					}

					if (!doneProp) {
						putAllAppropriateValuesIn(this.ac, thisPi, propValsObj, keyPropName, thisPi.getValueList(), pIsSmartMode);
					}
				}
			}
		}

		putObjectValueIn(mainObj, JSON_PROPVALS, propValsObj);

		return mainObj;
	}

	private CeStoreJsonObject normalNormalisedDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, boolean pIsSmartMode) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();
		ArrayList<String> onlyPropList = null;

		if (pOnlyProps != null) {
			onlyPropList = new ArrayList<String>(Arrays.asList(pOnlyProps));
		} else {
			onlyPropList = new ArrayList<String>();
		}

		putStringValueIn(mainObj, JSON_ID, pInst.getInstanceName());

		ArrayList<String> conNames = new ArrayList<String>();

		for (CeConcept thisCon : pInst.listAllConcepts()) {
			if (!thisCon.isThing()) {
				conNames.add(thisCon.getConceptName());
			}
		}

		putAllStringValuesIn(mainObj, JSON_NORM_CONCEPTS, conNames);

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			if (isValidProperty(thisPi, onlyPropList)) {
				CeProperty thisProp = thisPi.getRelatedProperty();
				String keyPropName = thisPi.getRelatedProperty().getPropertyName();
				boolean doneProp = false;

				if (pIsSmartMode) {
					CeInstance mmInst = thisProp.getMetaModelInstance(this.ac);
					if (mmInst.isConceptNamed(this.ac, CON_SVP)) {
						String latestVal = pInst.getLatestValueFromPropertyNamed(thisProp.getPropertyName());
						putAppropriateValueIn(this.ac, thisPi, mainObj, keyPropName, latestVal, pIsSmartMode);
						doneProp = true;
					}
				}

				if (!doneProp) {
					HashSet<String> vals = thisPi.getValueList();

					if (!vals.isEmpty()) {
						if (vals.size() == 1) {
							putAppropriateValueIn(this.ac, thisPi, mainObj, keyPropName, vals.iterator().next(), pIsSmartMode);
						} else {
							putAllAppropriateValuesIn(this.ac, thisPi, mainObj, keyPropName, vals, pIsSmartMode);
						}
					}
				}
			}
		}

		return mainObj;
	}

	public boolean isValidProperty(CePropertyInstance pPi, ArrayList<String> pOnlyProps) {
		boolean result = true;

		if ((pOnlyProps != null) && (!pOnlyProps.isEmpty())) {
			result = pOnlyProps.contains(pPi.getPropertyName());
		}

		return result;
	}

	public CeStoreJsonArray generateFullListJsonFor(Collection<CeInstance> pInstList, String[] pOnlyProps,
			boolean pSuppPropTypes, boolean pIsSmartMode) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		for (CeInstance thisInst : pInstList) {
			jInsts.add(normalFullDetailsJsonFor(thisInst, pOnlyProps, pSuppPropTypes, pIsSmartMode));
		}

		return jInsts;
	}

	public CeStoreJsonArray generateSummaryListJsonFor(Collection<CeInstance> pInstList, String[] pOnlyProps,
			boolean pSuppPropTypes, boolean pIsSmartMode, CeConcept pCon) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		for (CeInstance thisInst : pInstList) {
			jInsts.add(normalSummaryDetailsJsonFor(thisInst, pOnlyProps, pSuppPropTypes, pIsSmartMode, pCon));
		}

		return jInsts;
	}

	public CeStoreJsonArray generateMinimalListJsonFor(Collection<CeInstance> pInstList, String[] pOnlyProps, boolean pIsSmartMode) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		for (CeInstance thisInst : pInstList) {
			jInsts.add(normalMinimalDetailsJsonFor(thisInst, pOnlyProps, pIsSmartMode));
		}

		return jInsts;
	}

	public CeStoreJsonArray generateNormalisedListJsonFor(Collection<CeInstance> pInstList, String[] pOnlyProps, boolean pIsSmartMode) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		for (CeInstance thisInst : pInstList) {
			jInsts.add(normalNormalisedDetailsJsonFor(thisInst, pOnlyProps, pIsSmartMode));
		}

		return jInsts;
	}

	public static CeStoreJsonObject generateDiverseConceptDetailsJson(CeInstance pInst, ArrayList<CeConcept> pConList) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_INSTANCE_NAME, pInst.getInstanceName());
		putAllStringValuesIn(jObj, JSON_CONCEPT_NAMES, calculateConceptNamesFrom(pConList));

		return jObj;
	}

	private static ArrayList<String> calculateConceptNamesFrom(ArrayList<CeConcept> pConList) {
		ArrayList<String> result = new ArrayList<String>();

		for (CeConcept thisCon : pConList) {
			result.add(thisCon.getConceptName());
		}

		return result;
	}

}
