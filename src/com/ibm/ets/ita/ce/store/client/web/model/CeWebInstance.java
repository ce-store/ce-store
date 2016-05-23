package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.handler.QueryHandler;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeWebInstance extends CeWebObject {
    public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

    // JSON Response Keys
    private static final String KEY_PROPVALS = "property_values";
    private static final String KEY_PROPTYPES = "property_types";
    private static final String KEY_PROPRAT = "property_rationale";

    private static final String KEY_PRISEN_COUNT = "primary_sentence_count";
    private static final String KEY_SECSEN_COUNT = "secondary_sentence_count";
    private static final String KEY_INSTANCE_NAME = "instance_name";
    private static final String KEY_DIR_CONCEPT_NAMES = "direct_concept_names";
    private static final String KEY_INH_CONCEPT_NAMES = "inherited_concept_names";
    private static final String KEY_CONCEPT_NAMES = "concept_names";
    private static final String KEY_ICON = "icon";

    private static final String TYPE_INST = "instance";
    private static final String PROPTYPE_DATATYPE = "D";
    private static final String PROPTYPE_OBJECT = "O";

    public CeWebInstance(ActionContext pAc) {
        super(pAc);
    }

    public CeStoreJsonObject generateFullDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
        CeStoreJsonObject mainObj = new CeStoreJsonObject();

        if (pNumSteps <= 0) {
            mainObj = normalFullDetailsJsonFor(pInst, pOnlyProps, pSuppPropTypes);
        } else {
            mainObj.put("main_instance", normalFullDetailsJsonFor(pInst, pOnlyProps, pSuppPropTypes));

            if (pRelInsts) {
            	CeStoreJsonObject relInsts = new CeStoreJsonObject();
                relatedInstancesJsonFor(pInst, pNumSteps, 1, relInsts, pLimRels, pSuppPropTypes);
                mainObj.put("related_instances", relInsts);
            }

            if (pRefInsts) {
	            CeStoreJsonObject refInsts = new CeStoreJsonObject();
	            referringInstancesJsonFor(pInst, pNumSteps, 1, refInsts, pLimRels, pSuppPropTypes);
	            mainObj.put("referring_instances", refInsts);
            }
        }

        return mainObj;
    }

	private void relatedInstancesJsonFor(CeInstance pInst, int pNumSteps, int pDepth, CeStoreJsonObject pResult, String[] pLimRels, boolean pSuppPropTypes) {
		int thisDepth = pDepth + 1;

		for (CeInstance relInst : pInst.getAllRelatedInstances(this.ac, pLimRels)) {
			pResult.put(relInst.getInstanceName(), normalSummaryDetailsJsonFor(relInst, null, pSuppPropTypes));

			if (thisDepth <= pNumSteps) {
				relatedInstancesJsonFor(relInst, pNumSteps, thisDepth, pResult, pLimRels, pSuppPropTypes);
			}
		}
	}

	private void relatedInstancesMinimalJsonFor(CeInstance pInst, int pNumSteps, int pDepth, CeStoreJsonObject pResult, String[] pLimRels) {
		int thisDepth = pDepth + 1;

		for (CeInstance relInst : pInst.getAllRelatedInstances(this.ac, pLimRels)) {
			pResult.put(relInst.getInstanceName(), normalMinimalDetailsJsonFor(relInst, null));

			if (thisDepth <= pNumSteps) {
				relatedInstancesMinimalJsonFor(relInst, pNumSteps, thisDepth, pResult, pLimRels);
			}
		}
	}

	private void referringInstancesJsonFor(CeInstance pInst, int pNumSteps, int pDepth, CeStoreJsonObject pResult, String[] pLimRels, boolean pSuppPropTypes) {
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
					refArray.add(normalSummaryDetailsJsonFor(refInst, null, pSuppPropTypes));

					if (thisDepth <= pNumSteps) {
						referringInstancesJsonFor(refInst, pNumSteps, thisDepth, pResult, pLimRels, pSuppPropTypes);
					}
				}

				pResult.put(relPropName, refArray);
			}
		}
	}

	private void referringInstancesMinimalJsonFor(CeInstance pInst, int pNumSteps, int pDepth, CeStoreJsonObject pResult, String[] pLimRels) {
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
					refArray.add(normalMinimalDetailsJsonFor(refInst, null));

					if (thisDepth <= pNumSteps) {
						referringInstancesMinimalJsonFor(refInst, pNumSteps, thisDepth, pResult, pLimRels);
					}
				}

				pResult.put(relPropName, refArray);
			}
		}
	}

    private CeStoreJsonObject normalFullDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, boolean pSuppPropTypes) {
        CeStoreJsonObject mainObj = new CeStoreJsonObject();
        ArrayList<String> onlyPropList = null;

        if (pOnlyProps != null) {
        	onlyPropList = new ArrayList<String>(Arrays.asList(pOnlyProps));
        } else {
        	onlyPropList = new ArrayList<String>();
        }

        putStringValueIn(mainObj, KEY_TYPE, TYPE_INST);
        putStringValueIn(mainObj, KEY_STYLE, STYLE_FULL);
        putStringValueIn(mainObj, KEY_ID, pInst.getInstanceName());
        putLongValueIn(mainObj, KEY_CREATED, pInst.getCreationDate());
        putBooleanValueIn(mainObj, KEY_SHADOW, pInst.isShadowEntity());
        putStringValueIn(mainObj, KEY_LABEL, pInst.calculateLabel(this.ac));

        processAnnotations(pInst, mainObj);

        putStringValueIn(mainObj, KEY_ICON, pInst.calculateIconFilename(this.ac));

        ArrayList<String> dirConNames = pInst.calculateAllDirectConceptNames();
        putAllStringValuesIn(mainObj, KEY_DIR_CONCEPT_NAMES, dirConNames);
        putAllStringValuesIn(mainObj, KEY_INH_CONCEPT_NAMES, pInst.calculateAllInheritedConceptNames(dirConNames));

        CeStoreJsonObject propValsObj = new CeStoreJsonObject();
        CeStoreJsonObject propTypesObj = new CeStoreJsonObject();
        CeStoreJsonObject propRatObj = new CeStoreJsonObject();

        for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
        	if (isValidProperty(thisPi, onlyPropList)) {
	            String keyPropName = encodedPropertyName(thisPi.getRelatedProperty());
	            if (thisPi.isSingleCardinality()) {
	                putStringValueIn(propValsObj, keyPropName, thisPi.getSingleOrFirstValue());
	            } else {
	                HashSet<String> uvList = thisPi.getValueList();
	                putAllStringValuesIn(propValsObj, keyPropName, uvList);
	
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

        putObjectValueIn(mainObj, KEY_PROPVALS, propValsObj);

        if (!pSuppPropTypes) {
            putObjectValueIn(mainObj, KEY_PROPTYPES, propTypesObj);
        }

        if (!propRatObj.isEmpty()) {
            putObjectValueIn(mainObj, KEY_PROPRAT, propRatObj);
        }

        putIntValueIn(mainObj, KEY_PRISEN_COUNT, pInst.countPrimarySentences());
        putIntValueIn(mainObj, KEY_SECSEN_COUNT, pInst.countSecondarySentences());

        return mainObj;
    }

	public CeStoreJsonObject generateSummaryDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimInsts, boolean pSuppPropTypes) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();

		if (pNumSteps <= 0) {
			mainObj = normalSummaryDetailsJsonFor(pInst, pOnlyProps, pSuppPropTypes);
		} else {
			mainObj.put("main_instance", normalSummaryDetailsJsonFor(pInst, pOnlyProps, pSuppPropTypes));

			if (pRelInsts) {
				CeStoreJsonObject relInsts = new CeStoreJsonObject();
				relatedInstancesJsonFor(pInst, pNumSteps, 1, relInsts, pLimInsts, pSuppPropTypes);
				mainObj.put("related_instances", relInsts);
			}

			if (pRefInsts) {
				CeStoreJsonObject refInsts = new CeStoreJsonObject();
				referringInstancesJsonFor(pInst, pNumSteps, 1, refInsts, pLimInsts, pSuppPropTypes);
				mainObj.put("referring_instances", refInsts);
			}
		}

		return mainObj;
	}

	public CeStoreJsonObject generateMinimalDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimInsts) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();

		if (pNumSteps <= 0) {
			mainObj = normalMinimalDetailsJsonFor(pInst, pOnlyProps);
		} else {
			mainObj.put("main_instance", normalMinimalDetailsJsonFor(pInst, pOnlyProps));

			if (pRelInsts) {
				CeStoreJsonObject relInsts = new CeStoreJsonObject();
				relatedInstancesMinimalJsonFor(pInst, pNumSteps, 1, relInsts, pLimInsts);
				mainObj.put("related_instances", relInsts);
			}

			if (pRefInsts) {
				CeStoreJsonObject refInsts = new CeStoreJsonObject();
				referringInstancesMinimalJsonFor(pInst, pNumSteps, 1, refInsts, pLimInsts);
				mainObj.put("referring_instances", refInsts);
			}
		}

		return mainObj;
	}

	private CeStoreJsonObject normalSummaryDetailsJsonFor(CeInstance pInst, String[] pOnlyProps, boolean pSuppPropTypes) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();
		ArrayList<String> onlyPropList = null;

		if (pOnlyProps != null) {
			onlyPropList = new ArrayList<String>(Arrays.asList(pOnlyProps));
		} else {
			onlyPropList = new ArrayList<String>();
		}

		putStringValueIn(mainObj, KEY_TYPE, TYPE_INST);
		putStringValueIn(mainObj, KEY_STYLE, STYLE_SUMMARY);
		putStringValueIn(mainObj, KEY_ID, pInst.getInstanceName());
		putLongValueIn(mainObj, KEY_CREATED, pInst.getCreationDate());
		putBooleanValueIn(mainObj, KEY_SHADOW, pInst.isShadowEntity());
		putStringValueIn(mainObj, KEY_LABEL, pInst.calculateLabel(this.ac));

		processAnnotations(pInst, mainObj);

		putStringValueIn(mainObj, KEY_ICON, pInst.calculateIconFilename(this.ac));

		ArrayList<String> dirConNames = pInst.calculateAllDirectConceptNames();

		putAllStringValuesIn(mainObj, KEY_DIR_CONCEPT_NAMES, dirConNames);
		putAllStringValuesIn(mainObj, KEY_INH_CONCEPT_NAMES, pInst.calculateAllInheritedConceptNames(dirConNames));

		CeStoreJsonObject propValsObj = new CeStoreJsonObject();
		CeStoreJsonObject propTypesObj = new CeStoreJsonObject();

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			if (isValidProperty(thisPi, onlyPropList)) {
				String keyPropName = encodedPropertyName(thisPi.getRelatedProperty());
				if (thisPi.isSingleCardinality()) {
					putStringValueIn(propValsObj, keyPropName, thisPi.getSingleOrFirstValue());
				} else {
					putAllStringValuesIn(propValsObj, keyPropName, thisPi.getValueList());
				}

				if (thisPi.getRelatedProperty().isDatatypeProperty()) {
					putStringValueIn(propTypesObj, keyPropName, PROPTYPE_DATATYPE);
				} else {
					putStringValueIn(propTypesObj, keyPropName, PROPTYPE_OBJECT);
				}
			}
		}

		putObjectValueIn(mainObj, KEY_PROPVALS, propValsObj);

		if (!pSuppPropTypes) {
			putObjectValueIn(mainObj, KEY_PROPTYPES, propTypesObj);
		}

		putIntValueIn(mainObj, KEY_PRISEN_COUNT, pInst.countPrimarySentences());
		putIntValueIn(mainObj, KEY_SECSEN_COUNT, pInst.countSecondarySentences());

		return mainObj;
	}

	private CeStoreJsonObject normalMinimalDetailsJsonFor(CeInstance pInst, String[] pOnlyProps) {
		CeStoreJsonObject mainObj = new CeStoreJsonObject();
		ArrayList<String> onlyPropList = null;

		if (pOnlyProps != null) {
			onlyPropList = new ArrayList<String>(Arrays.asList(pOnlyProps));
		} else {
			onlyPropList = new ArrayList<String>();
		}

		putStringValueIn(mainObj, KEY_ID, pInst.getInstanceName());

		ArrayList<String> conNames = new ArrayList<String>();

		for (CeConcept thisCon : pInst.listAllConcepts()) {
			if (!thisCon.isThing()) {
				conNames.add(thisCon.getConceptName());
			}
		}

		putAllStringValuesIn(mainObj, KEY_CONCEPT_NAMES, conNames);

		CeStoreJsonObject propValsObj = new CeStoreJsonObject();
		CeStoreJsonObject propTypesObj = new CeStoreJsonObject();

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			if (isValidProperty(thisPi, onlyPropList)) {
				String keyPropName = encodedPropertyName(thisPi.getRelatedProperty());
				if (thisPi.isSingleCardinality()) {
					putStringValueIn(propValsObj, keyPropName, thisPi.getSingleOrFirstValue());
				} else {
					putAllStringValuesIn(propValsObj, keyPropName, thisPi.getValueList());
				}

				if (thisPi.getRelatedProperty().isDatatypeProperty()) {
					putStringValueIn(propTypesObj, keyPropName, PROPTYPE_DATATYPE);
				} else {
					putStringValueIn(propTypesObj, keyPropName, PROPTYPE_OBJECT);
				}
			}
		}

		putObjectValueIn(mainObj, KEY_PROPVALS, propValsObj);

		return mainObj;
	}

	public boolean isValidProperty(CePropertyInstance pPi, ArrayList<String> pOnlyProps) {
		boolean result = true;

		if ((pOnlyProps != null) && (!pOnlyProps.isEmpty())) {
			result = pOnlyProps.contains(pPi.getPropertyName());
		}

		return result;
	}

    public CeStoreJsonArray generateFullListJsonFor(Collection<CeInstance> pInstList, String[] pOnlyProps, boolean pSuppPropTypes) {
        CeStoreJsonArray jInsts = new CeStoreJsonArray();

        for (CeInstance thisInst : pInstList) {
            jInsts.add(normalFullDetailsJsonFor(thisInst, pOnlyProps, pSuppPropTypes));
        }

        return jInsts;
    }

	public CeStoreJsonArray generateSummaryListJsonFor(Collection<CeInstance> pInstList, String[] pOnlyProps, boolean pSuppPropTypes) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		for (CeInstance thisInst : pInstList) {
			jInsts.add(normalSummaryDetailsJsonFor(thisInst, pOnlyProps, pSuppPropTypes));
		}

		return jInsts;
	}

	public CeStoreJsonArray generateMinimalListJsonFor(Collection<CeInstance> pInstList, String[] pOnlyProps) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		for (CeInstance thisInst : pInstList) {
			jInsts.add(normalMinimalDetailsJsonFor(thisInst, pOnlyProps));
		}

		return jInsts;
	}

    // Diverse concept instance details response:
    // KEY_INSTANCE_NAME
    // KEY_CONCEPT_NAMES[]
    public static CeStoreJsonObject generateDiverseConceptDetailsJson(CeInstance pInst, ArrayList<CeConcept> pConList) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putStringValueIn(jObj, KEY_INSTANCE_NAME, pInst.getInstanceName());
        putAllStringValuesIn(jObj, KEY_CONCEPT_NAMES, calculateConceptNamesFrom(pConList));

        return jObj;
    }

    private static ArrayList<String> calculateConceptNamesFrom(ArrayList<CeConcept> pConList) {
        ArrayList<String> result = new ArrayList<String>();

        for (CeConcept thisCon : pConList) {
            result.add(thisCon.getConceptName());
        }

        return result;
    }

    private static String encodedPropertyName(CeProperty pProp) {
        return pProp.getPropertyName();
    }

}
