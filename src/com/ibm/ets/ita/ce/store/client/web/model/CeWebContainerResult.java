package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
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
	private static final String KEY_SEARCHCON = "search_concept";
	private static final String KEY_SEARCHPROP = "search_property";
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
	public static CeStoreJsonObject generateNormalCeQueryResultFrom(ActionContext pAc, ContainerCeResult pCeResult, boolean pReturnInstances) {
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
				createInstanceResultsFor(pAc, jObj, KEY_INSTANCES, queryResults, pCeResult.getTypes());
			}
		} else {
			reportError("Unexpected null container CE result encountered during details JSON rendering", pAc);
		}

		return jObj;
	}
	
	private static void createInstanceResultsFor(ActionContext pAc, CeStoreJsonObject pObj, String pKey, ArrayList<ArrayList<String>> pResults, ArrayList<String> pTypes) {
		CeStoreJsonObject jInsts = new CeStoreJsonObject();
		
		//TODO: numSteps should be respected here too
		//TODO: full vs summary should be respected here too

		for (ArrayList<String> thisRow : pResults) {
			for (int i = 0; i < pTypes.size(); i++) {
				String thisType = pTypes.get(i);
				String thisId = thisRow.get(i);
				
				if (thisType.equals("O")) {					
					if (!thisId.isEmpty()) {
						CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc,  thisId);
						
						CeWebInstance webInst = new CeWebInstance(pAc);
						CeStoreJsonObject jInst = webInst.generateSummaryDetailsJsonFor(thisInst, 0, false, false, null);
						
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
	//	KEY_SEARCHCON
	//	KEY_SEARCHPROP
	//	KEY_SEARCHRESULTS[]
	//		KEY_DOMAIN_NAME
	//		KEY_INSTANCE_NAME
	//		KEY_PROP_NAME
	//		KEY_PROP_VAL
	//		KEY_PROP_TYPE
	public static CeStoreJsonObject generateKeywordSearchResultFrom(ActionContext pAc, ArrayList<ContainerSearchResult> pResults, String pSearchTerms, String pConceptName, String pPropertyName) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		
		if (pResults != null) {
			if (!pResults.isEmpty()) {
				putStringValueIn(jObj, KEY_SEARCHTERMS, pSearchTerms);
				putStringValueIn(jObj, KEY_SEARCHCON, pConceptName);
				putStringValueIn(jObj, KEY_SEARCHPROP, pPropertyName);
				putArrayValueIn(jObj, KEY_SEARCHRESULTS, processKeywordSearchRows(pAc, pResults));
			} else {
				reportEmptyKeywordSearchResult(pAc, pSearchTerms, pConceptName, pPropertyName);
			}
		} else {
			reportError("Unexpected null keyword search result encountered during details JSON rendering", pAc);
		}
		
		return jObj;
	}

	private static CeStoreJsonArray processKeywordSearchRows(ActionContext pAc, ArrayList<ContainerSearchResult> pResults) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		for (ContainerSearchResult thisRes : pResults) {
			CeStoreJsonObject jObj = new CeStoreJsonObject();
			
			CeInstance resInst = pAc.getModelBuilder().getInstanceNamed(pAc, thisRes.getInstanceName());
			
			putStringValueIn(jObj, KEY_DOMAIN_NAME, thisRes.getConceptName());
			putStringValueIn(jObj, KEY_INSTANCE_NAME, thisRes.getInstanceName());
			putStringValueIn(jObj, KEY_INSTANCE_LABEL, resInst.calculateLabel(pAc));
			putStringValueIn(jObj, KEY_PROP_NAME, thisRes.getPropertyName());
			putStringValueIn(jObj, KEY_PROP_VAL, thisRes.getPropertyValue());
			putStringValueIn(jObj, KEY_PROP_TYPE, thisRes.getPropertyType());
			
			jArr.add(jObj);
		}
		
		return jArr;
	}

	private static void reportEmptyKeywordSearchResult(ActionContext pAc, String pSearchTerms, String pConName, String pPropName) {
		String extraBit = "";
		
		if ((pConName != null) && (pConName.isEmpty())) {
			extraBit = " for concept named " + pConName;
		}

		if ((pPropName != null) && (pPropName.isEmpty())) {
			extraBit = " and property named " + pPropName;
		}

		pAc.getActionResponse().addLineToMessage("Nothing matched your search term of '" + pSearchTerms + "'" + extraBit);
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