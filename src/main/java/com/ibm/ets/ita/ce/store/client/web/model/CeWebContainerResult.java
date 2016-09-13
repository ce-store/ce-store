package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_HEADERS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RESULTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ROWS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTANCES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_TYPES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_NUMROWS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_QUERY_TEXT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_QUERY_TIME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CONCEPT_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTANCE_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTANCE_LABEL;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROP_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROP_TYPE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROP_VAL;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEARCHTERMS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEARCHCONS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEARCHPROPS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEARCHRESULTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_COUNT;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.rest.CeStoreRestApiSpecial;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerCommonValues;
import com.ibm.ets.ita.ce.store.model.container.ContainerSearchResult;

public class CeWebContainerResult extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public CeWebContainerResult(ActionContext pAc) {
		super(pAc);
	}

	//CE Query response structure:
	//	JSON_QUERY_TEXT
	//	JSON_QUERY_TIME
	//	JSON_HEADERS[]
	//	JSON_TYPES[]
	//	JSON_RESULTS[[]]
	public static CeStoreJsonObject generateNormalCeQueryResultFrom(ActionContext pAc, ContainerCeResult pCeResult, boolean pSuppressResult, boolean pReturnInstances, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (pCeResult != null) {
			ArrayList<ArrayList<String>> queryResults = pCeResult.getResultRows();
			int ceIndex = pCeResult.getIndexForHeader("CE");

			if (ceIndex > -1) {
				for (ArrayList<String> thisRow : queryResults) {
					String oldCe = thisRow.get(ceIndex);

					if (ContainerCeResult.isMultipleSentences(oldCe)) {
						String newCe = "";
						for (String thisPart : ContainerCeResult.splitSentences(oldCe)) {
							if (!thisPart.isEmpty()) {
								if (!newCe.isEmpty()) {
									newCe += "\n";
								}
								newCe += thisPart;

								if (!thisPart.endsWith("\n")) {
									newCe += "\n";
								}
							}
						}
						thisRow.set(ceIndex, newCe);
					}
				}
			}

			putStringValueIn(jObj, JSON_QUERY_TEXT, pCeResult.getQuery());
			putLongValueIn(jObj, JSON_QUERY_TIME, pCeResult.getExecutionTime());
			putLongValueIn(jObj, JSON_NUMROWS, queryResults.size());

			if (!pSuppressResult) {
				putAllStringValuesIn(jObj, JSON_HEADERS, pCeResult.getHeaders());
				putAllStringValuesIn(jObj, JSON_TYPES, pCeResult.getTypes());
				putAllArrayStringValuesIn(jObj, JSON_RESULTS, queryResults);	
			}

			if (pReturnInstances) {
				createNormalInstanceResultsFor(pAc, jObj, JSON_INSTANCES, queryResults, pCeResult.getTypes(), pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes);
			}
		} else {
			reportError("Unexpected null container CE result encountered during details JSON rendering", pAc);
		}

		return jObj;
	}

	public static CeStoreJsonObject generateMinimalCeQueryResultFrom(ActionContext pAc, ContainerCeResult pCeResult, boolean pSuppressResult, boolean pReturnInstances, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (pCeResult != null) {
			ArrayList<ArrayList<String>> queryResults = pCeResult.getResultRows();
			int ceIndex = pCeResult.getIndexForHeader("CE");

			if (ceIndex > -1) {
				for (ArrayList<String> thisRow : queryResults) {
					String oldCe = thisRow.get(ceIndex);

					if (ContainerCeResult.isMultipleSentences(oldCe)) {
						String newCe = "";
						for (String thisPart : ContainerCeResult.splitSentences(oldCe)) {
							if (!thisPart.isEmpty()) {
								if (!newCe.isEmpty()) {
									newCe += "\n";
								}
								newCe += thisPart;

								if (!thisPart.endsWith("\n")) {
									newCe += "\n";
								}
							}
						}
						thisRow.set(ceIndex, newCe);
					}
				}
			}

			putStringValueIn(jObj, JSON_QUERY_TEXT, pCeResult.getQuery());
			putLongValueIn(jObj, JSON_QUERY_TIME, pCeResult.getExecutionTime());
			putLongValueIn(jObj, JSON_NUMROWS, queryResults.size());

			if (!pSuppressResult) {
				putAllStringValuesIn(jObj, JSON_HEADERS, pCeResult.getHeaders());
				putAllStringValuesIn(jObj, JSON_TYPES, pCeResult.getTypes());
				putAllArrayStringValuesIn(jObj, JSON_RESULTS, queryResults);	
			}

			if (pReturnInstances) {
				createMinimalInstanceResultsFor(pAc, jObj, JSON_INSTANCES, queryResults, pCeResult.getTypes(), pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes);
			}
		} else {
			reportError("Unexpected null container CE result encountered during details JSON rendering", pAc);
		}

		return jObj;
	}

	public static CeStoreJsonObject generateNormalisedCeQueryResultFrom(ActionContext pAc, ContainerCeResult pCeResult, boolean pSuppressResult, boolean pReturnInstances, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (pCeResult != null) {
			ArrayList<ArrayList<String>> queryResults = pCeResult.getResultRows();
			int ceIndex = pCeResult.getIndexForHeader("CE");

			if (ceIndex > -1) {
				for (ArrayList<String> thisRow : queryResults) {
					String oldCe = thisRow.get(ceIndex);

					if (ContainerCeResult.isMultipleSentences(oldCe)) {
						String newCe = "";
						for (String thisPart : ContainerCeResult.splitSentences(oldCe)) {
							if (!thisPart.isEmpty()) {
								if (!newCe.isEmpty()) {
									newCe += "\n";
								}
								newCe += thisPart;

								if (!thisPart.endsWith("\n")) {
									newCe += "\n";
								}
							}
						}
						thisRow.set(ceIndex, newCe);
					}
				}
			}

			putStringValueIn(jObj, JSON_QUERY_TEXT, pCeResult.getQuery());
			putLongValueIn(jObj, JSON_QUERY_TIME, pCeResult.getExecutionTime());
			putLongValueIn(jObj, JSON_NUMROWS, queryResults.size());

			if (!pSuppressResult) {
				putAllStringValuesIn(jObj, JSON_HEADERS, pCeResult.getHeaders());
				putAllStringValuesIn(jObj, JSON_TYPES, pCeResult.getTypes());
				putAllArrayStringValuesIn(jObj, JSON_RESULTS, queryResults);	
			}

			if (pReturnInstances) {
				createNormalisedInstanceResultsFor(pAc, jObj, JSON_INSTANCES, queryResults, pCeResult.getTypes(), pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes);
			}
		} else {
			reportError("Unexpected null container CE result encountered during details JSON rendering", pAc);
		}

		return jObj;
	}

	private static void createNormalInstanceResultsFor(ActionContext pAc, CeStoreJsonObject pObj, String pKey, ArrayList<ArrayList<String>> pResults, ArrayList<String> pTypes, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jInsts = new CeStoreJsonObject();

		for (ArrayList<String> thisRow : pResults) {
			for (int i = 0; i < pTypes.size(); i++) {
				String thisType = pTypes.get(i);
				String thisId = thisRow.get(i);

				if (thisType.equals("O")) {
					if (!thisId.isEmpty()) {
						CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc,  thisId);

						CeWebInstance webInst = new CeWebInstance(pAc);
						CeStoreJsonObject jInst = webInst.generateSummaryDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes);

						jInsts.put(thisInst.getInstanceName(), jInst);
					}
				}
			}
		}

		pObj.put(pKey, jInsts);
	}

	private static void createMinimalInstanceResultsFor(ActionContext pAc, CeStoreJsonObject pObj, String pKey, ArrayList<ArrayList<String>> pResults, ArrayList<String> pTypes, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jInsts = new CeStoreJsonObject();

		for (ArrayList<String> thisRow : pResults) {
			for (int i = 0; i < pTypes.size(); i++) {
				String thisType = pTypes.get(i);
				String thisId = thisRow.get(i);

				if (thisType.equals("O")) {
					if (!thisId.isEmpty()) {
						CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc,  thisId);

						CeWebInstance webInst = new CeWebInstance(pAc);
						CeStoreJsonObject jInst = webInst.generateMinimalDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels);

						jInsts.put(thisInst.getInstanceName(), jInst);
					}
				}
			}
		}

		pObj.put(pKey, jInsts);
	}

	private static void createNormalisedInstanceResultsFor(ActionContext pAc, CeStoreJsonObject pObj, String pKey, ArrayList<ArrayList<String>> pResults, ArrayList<String> pTypes, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jInsts = new CeStoreJsonObject();

		for (ArrayList<String> thisRow : pResults) {
			for (int i = 0; i < pTypes.size(); i++) {
				String thisType = pTypes.get(i);
				String thisId = thisRow.get(i);

				if (thisType.equals("O")) {
					if (!thisId.isEmpty()) {
						CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc,  thisId);

						CeWebInstance webInst = new CeWebInstance(pAc);
						CeStoreJsonObject jInst = webInst.generateNormalisedDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels);

						jInsts.put(thisInst.getInstanceName(), jInst);
					}
				}
			}
		}

		pObj.put(pKey, jInsts);
	}

	public static String generateCeOnlyCeQueryResultFrom(ActionContext pAc, ContainerCeResult pCeResult) {
		StringBuilder sb = new StringBuilder();

		if (pCeResult != null) {
			for (String thisCeSen : pCeResult.getCeResults()) {
				appendToSb(sb, thisCeSen);
			}
		} else {
			reportError("Unexpected null container CE result encountered during details JSON rendering", pAc);
		}

		return sb.toString();
	}

	public static CeStoreJsonObject generateKeywordSearchSummaryResultFrom(ActionContext pAc, ArrayList<ContainerSearchResult> pResults, ArrayList<String> pSearchTerms, String[] pConceptNames, String[] pPropertyNames, boolean pRetInsts, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		return generateKeywordSearchResultFrom(pAc, pResults, pSearchTerms, pConceptNames, pPropertyNames, pRetInsts, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes, false);
	}

	public static CeStoreJsonObject generateKeywordSearchMinimalResultFrom(ActionContext pAc, ArrayList<ContainerSearchResult> pResults, ArrayList<String> pSearchTerms, String[] pConceptNames, String[] pPropertyNames, boolean pRetInsts, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		return generateKeywordSearchResultFrom(pAc, pResults, pSearchTerms, pConceptNames, pPropertyNames, pRetInsts, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes, true);
	}

	// Keyword search response:
	//	JSON_SEARCHTERMS
	//	JSON_SEARCHCONS
	//	JSON_SEARCHPROPS
	//	JSON_SEARCHRESULTS[]
	//		JSON_DOMAIN_NAME
	//		JSON_INSTANCE_NAME
	//		JSON_PROP_NAME
	//		JSON_PROP_VAL
	//		JSON_PROP_TYPE
	public static CeStoreJsonObject generateKeywordSearchResultFrom(ActionContext pAc, ArrayList<ContainerSearchResult> pResults, ArrayList<String> pSearchTerms, String[] pConceptNames, String[] pPropertyNames, boolean pRetInsts, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes, boolean pMinimal) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (pResults != null) {
			if (!pResults.isEmpty()) {
				CeStoreJsonObject jObjResult = processKeywordSearchRows(pAc, pResults, pRetInsts, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes, pMinimal);

				putAllStringValuesIn(jObj, JSON_SEARCHTERMS, pSearchTerms);
				putAllStringValuesIn(jObj, JSON_SEARCHCONS, pConceptNames);
				putAllStringValuesIn(jObj, JSON_SEARCHPROPS, pPropertyNames);
				putArrayValueIn(jObj, JSON_SEARCHRESULTS, (CeStoreJsonArray)jObjResult.get(pAc, JSON_ROWS));

				if (pRetInsts) {
					putObjectValueIn(jObj, JSON_INSTANCES, (CeStoreJsonObject)jObjResult.get(pAc, JSON_INSTANCES));
				}
			} else {
				reportEmptyKeywordSearchResult(pAc, pSearchTerms, pConceptNames, pPropertyNames);
			}
		} else {
			reportError("Unexpected null keyword search result encountered during details JSON rendering", pAc);
		}

		return jObj;
	}

	private static CeStoreJsonObject processKeywordSearchRows(ActionContext pAc, ArrayList<ContainerSearchResult> pResults, boolean pRetInsts, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes, boolean pMinimal) {
		CeStoreJsonObject jObjMain = new CeStoreJsonObject();
		CeStoreJsonObject jObjInsts = new CeStoreJsonObject();
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (ContainerSearchResult thisRes : pResults) {
			CeStoreJsonObject jObjRow = new CeStoreJsonObject();
			CeInstance resInst = pAc.getModelBuilder().getInstanceNamed(pAc, thisRes.getInstanceName());

			putAllStringValuesIn(jObjRow, JSON_CONCEPT_NAMES, thisRes.getConceptNames());
			putStringValueIn(jObjRow, JSON_INSTANCE_NAME, thisRes.getInstanceName());
			putStringValueIn(jObjRow, JSON_INSTANCE_LABEL, resInst.calculateLabel(pAc));
			putStringValueIn(jObjRow, JSON_PROP_NAME, thisRes.getPropertyName());
			putStringValueIn(jObjRow, JSON_PROP_VAL, thisRes.getPropertyValue());
			putStringValueIn(jObjRow, JSON_PROP_TYPE, thisRes.getPropertyType());

			if (pRetInsts) {
				CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc, thisRes.getInstanceName());

				if (thisInst != null) {
					CeWebInstance cwi = new CeWebInstance(pAc);

					//TODO: Get the values for these defaulted parameters from command line parameters
					//TODO: Need to handle normalised form here too
					if (pMinimal) {
						jObjInsts.put(thisInst.getInstanceName(), cwi.generateMinimalDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels));
					} else {
						jObjInsts.put(thisInst.getInstanceName(), cwi.generateSummaryDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes));
					}
				}
			}

			jArr.add(jObjRow);
		}

		jObjMain.put(JSON_ROWS, jArr);
		jObjMain.put(JSON_INSTANCES, jObjInsts);

		return jObjMain;
	}

	private static void reportEmptyKeywordSearchResult(ActionContext pAc, ArrayList<String> pSearchTerms, String[] pConNames, String[] pPropNames) {
		String extraBit = "";
		
		if ((pConNames != null) && (pConNames.length == 0)) {
			String conNameList = "";
			String sep = "";

			for (String conName : pConNames) {
				conNameList += sep + conName;
				sep = ", ";
			}

			extraBit = " for concepts named " + conNameList;
		}

		if ((pPropNames != null) && (pPropNames.length == 0)) {
			String propNameList = "";
			String sep = "";

			for (String propName : pPropNames) {
				propNameList += sep + propName;
				sep = ", ";
			}

			extraBit = " and property named " + propNameList;
		}

		pAc.getActionResponse().addLineToMessage("Nothing matched your search term of '" + CeStoreRestApiSpecial.generateSearchTermSummaryFrom(pSearchTerms) + "'" + extraBit);
	}

	//Common Values response:
	//	[]
	//		JSON_PROP_VAL
	//		JSON_COUNT
	public static CeStoreJsonArray generateCommonValuesResultFrom(ActionContext pAc, ArrayList<ContainerCommonValues> pResults) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		if (!pResults.isEmpty()) {
			for (ContainerCommonValues thisRes : pResults) {
				CeStoreJsonObject jObj = new CeStoreJsonObject();

				putStringValueIn(jObj, JSON_PROP_VAL, thisRes.getName());
				putStringValueIn(jObj, JSON_COUNT, thisRes.getCount());
				
				jArr.add(jObj);
			}
		} else {
			reportError("Unexpected null common values search result encountered during details JSON rendering", pAc);
		}
		
		return jArr;
	}
	
}
