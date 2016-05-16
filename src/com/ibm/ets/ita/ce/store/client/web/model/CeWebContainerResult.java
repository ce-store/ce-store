package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.rest.CeStoreRestApiSpecial;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerCommonValues;
import com.ibm.ets.ita.ce.store.model.container.ContainerSearchResult;

public class CeWebContainerResult extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//JSON Response Keys
	private static final String KEY_HEADERS = "headers";
	private static final String KEY_RESULTS = "results";
	private static final String KEY_ROWS = "rows";
	private static final String KEY_INSTANCES = "instances";
	private static final String KEY_TYPES = "types";

	private static final String KEY_QUERY_TEXT = "query";
	private static final String KEY_QUERY_TIME = "query_time";

	private static final String KEY_DOMAIN_NAME = "domain_name";
	private static final String KEY_INSTANCE_NAME = "instance_name";
	private static final String KEY_INSTANCE_LABEL = "instance_label";
	private static final String KEY_PROP_NAME = "property_name";
	private static final String KEY_PROP_VAL = "property_value";
	private static final String KEY_PROP_TYPE = "property_type";
	private static final String KEY_SEARCHTERMS = "search_terms";
	private static final String KEY_SEARCHCONS = "search_concepts";
	private static final String KEY_SEARCHPROPS = "search_properties";
	private static final String KEY_SEARCHRESULTS = "search_results";
	private static final String KEY_COUNT = "count";
	
	public CeWebContainerResult(ActionContext pAc) {
		super(pAc);
	}

	//CE Query response structure:
	//	KEY_QUERY_TEXT
	//	KEY_QUERY_TIME
	//	KEY_HEADERS[]
	//	KEY_TYPES[]
	//	KEY_RESULTS[[]]
	public static CeStoreJsonObject generateNormalCeQueryResultFrom(ActionContext pAc, ContainerCeResult pCeResult, boolean pReturnInstances, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
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

			putStringValueIn(jObj, KEY_QUERY_TEXT, pCeResult.getQuery());
			putLongValueIn(jObj, KEY_QUERY_TIME, pCeResult.getExecutionTime());
			putAllStringValuesIn(jObj, KEY_HEADERS, pCeResult.getHeaders());
			putAllStringValuesIn(jObj, KEY_TYPES, pCeResult.getTypes());
			
			putAllArrayStringValuesIn(jObj, KEY_RESULTS, queryResults);	

			if (pReturnInstances) {
				createInstanceResultsFor(pAc, jObj, KEY_INSTANCES, queryResults, pCeResult.getTypes(), pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes);
			}
		} else {
			reportError("Unexpected null container CE result encountered during details JSON rendering", pAc);
		}

		return jObj;
	}

	private static void createInstanceResultsFor(ActionContext pAc, CeStoreJsonObject pObj, String pKey, ArrayList<ArrayList<String>> pResults, ArrayList<String> pTypes, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jInsts = new CeStoreJsonObject();

		for (ArrayList<String> thisRow : pResults) {
			for (int i = 0; i < pTypes.size(); i++) {
				String thisType = pTypes.get(i);
				String thisId = thisRow.get(i);

				if (thisType.equals("O")) {
					if (!thisId.isEmpty()) {
						CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc,  thisId);

						CeWebInstance webInst = new CeWebInstance(pAc);
						CeStoreJsonObject jInst = webInst.generateSummaryDetailsJsonFor(thisInst, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes);

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

	// Keyword search response:
	//	KEY_SEARCHTERMS
	//	KEY_SEARCHCONS
	//	KEY_SEARCHPROPS
	//	KEY_SEARCHRESULTS[]
	//		KEY_DOMAIN_NAME
	//		KEY_INSTANCE_NAME
	//		KEY_PROP_NAME
	//		KEY_PROP_VAL
	//		KEY_PROP_TYPE
	public static CeStoreJsonObject generateKeywordSearchResultFrom(ActionContext pAc, ArrayList<ContainerSearchResult> pResults, ArrayList<String> pSearchTerms, String[] pConceptNames, String[] pPropertyNames, boolean pRetInsts, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (pResults != null) {
			if (!pResults.isEmpty()) {
				CeStoreJsonObject jObjResult = processKeywordSearchRows(pAc, pResults, pRetInsts, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes);

				putAllStringValuesIn(jObj, KEY_SEARCHTERMS, pSearchTerms);
				putAllStringValuesIn(jObj, KEY_SEARCHCONS, pConceptNames);
				putAllStringValuesIn(jObj, KEY_SEARCHPROPS, pPropertyNames);
				putArrayValueIn(jObj, KEY_SEARCHRESULTS, (CeStoreJsonArray)jObjResult.get(pAc, KEY_ROWS));

				if (pRetInsts) {
					putObjectValueIn(jObj, KEY_INSTANCES, (CeStoreJsonObject)jObjResult.get(pAc, KEY_INSTANCES));
				}

			} else {
				reportEmptyKeywordSearchResult(pAc, pSearchTerms, pConceptNames, pPropertyNames);
			}
		} else {
			reportError("Unexpected null keyword search result encountered during details JSON rendering", pAc);
		}

		return jObj;
	}

	private static CeStoreJsonObject processKeywordSearchRows(ActionContext pAc, ArrayList<ContainerSearchResult> pResults, boolean pRetInsts, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		CeStoreJsonObject jObjMain = new CeStoreJsonObject();
		CeStoreJsonObject jObjInsts = new CeStoreJsonObject();
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (ContainerSearchResult thisRes : pResults) {
			CeStoreJsonObject jObjRow = new CeStoreJsonObject();
			CeInstance resInst = pAc.getModelBuilder().getInstanceNamed(pAc, thisRes.getInstanceName());

			putStringValueIn(jObjRow, KEY_DOMAIN_NAME, thisRes.getConceptName());
			putStringValueIn(jObjRow, KEY_INSTANCE_NAME, thisRes.getInstanceName());
			putStringValueIn(jObjRow, KEY_INSTANCE_LABEL, resInst.calculateLabel(pAc));
			putStringValueIn(jObjRow, KEY_PROP_NAME, thisRes.getPropertyName());
			putStringValueIn(jObjRow, KEY_PROP_VAL, thisRes.getPropertyValue());
			putStringValueIn(jObjRow, KEY_PROP_TYPE, thisRes.getPropertyType());

			if (pRetInsts) {
				CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc, thisRes.getInstanceName());

				if (thisInst != null) {
					CeWebInstance cwi = new CeWebInstance(pAc);

					//TODO: Get the values for these defaulted parameters from command line parameters
					jObjInsts.put(thisInst.getInstanceName(), cwi.generateSummaryDetailsJsonFor(thisInst, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes));
				}
			}

			jArr.add(jObjRow);
		}

		jObjMain.put(KEY_ROWS, jArr);
		jObjMain.put(KEY_INSTANCES, jObjInsts);

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
	//		KEY_PROP_VAL
	//		KEY_COUNT
	public static CeStoreJsonArray generateCommonValuesResultFrom(ActionContext pAc, ArrayList<ContainerCommonValues> pResults) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		if (!pResults.isEmpty()) {
			for (ContainerCommonValues thisRes : pResults) {
				CeStoreJsonObject jObj = new CeStoreJsonObject();

				putStringValueIn(jObj, KEY_PROP_VAL, thisRes.getName());
				putStringValueIn(jObj, KEY_COUNT, thisRes.getCount());
				
				jArr.add(jObj);
			}
		} else {
			reportError("Unexpected null common values search result encountered during details JSON rendering", pAc);
		}
		
		return jArr;
	}
	
}