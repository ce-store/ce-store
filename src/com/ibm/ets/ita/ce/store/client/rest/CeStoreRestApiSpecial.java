package com.ibm.ets.ita.ce.store.client.rest;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.urlDecode;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebContainerResult;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebQueryOrRule;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSpecial;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.container.ContainerSearchResult;

public class CeStoreRestApiSpecial extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String REST_STATISTICS = "statistics";
	private static final String REST_PATTERN = "patterns";
	private static final String REST_CONFIG = "config";
	private static final String REST_UID = "uid";
	private static final String REST_SEARCH = "keyword-search";
	private static final String REST_SHADCON = "shadow-concepts";
	private static final String REST_SHADINST = "shadow-instances";
	private static final String REST_UNREFINST = "unreferenced-instances";
	private static final String REST_DIVCONINST = "diverse-concept-instances";
	private static final String REST_MULTINSTS = "instances-for-multiple-concepts";

	private static final String PARM_SEARCHTERMS = "keywords";
	private static final String PARM_CASESEN = "caseSensitive";
	private static final String PARM_RESTRICTCONNAMES = "restrictToConcepts";
	private static final String PARM_RESTRICTPROPNAMES = "restrictToProperties";
	private static final String PARM_CONNAMES = "conceptNames";
	private static final String PARM_IGMETMOD = "ignoreMetaModel";

	private CeStoreRestApiSpecial(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/special/statistics
	 * 		/special/patterns
	 * 		/special/rationale
	 * 		/special/config
	 * 		/special/config/{key}
	 * 		/special/uid
	 * 		/special/uid/batch
	 * 		/special/uid/reset
	 * 		/special/keyword-search
	 * 		/special/shadow-concepts
	 * 		/special/shadow-instances
	 * 		/special/diverse-concept-instances
	 * 		/special/data
	 */
	public static boolean processRequest(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		boolean statsInResponse = false;
		CeStoreRestApiSpecial handler = new CeStoreRestApiSpecial(pWc, pRestParts, pRequest);

		if (pRestParts.size() > 1) {
			String specialType = pRestParts.get(1);

			if (specialType.equals(REST_STATISTICS)) {
				handler.processStatisticsRequest();
				statsInResponse = true;
			} else if (specialType.equals(REST_PATTERN)) {
				handler.processPatternRequest();
			} else if (specialType.equals(REST_RATIONALE)) {
				handler.processRationaleRequest();
			} else if (specialType.equals(REST_CONFIG)) {
				CeStoreRestApiSpecialConfig configHandler = new CeStoreRestApiSpecialConfig(pWc, pRestParts, pRequest);
				configHandler.processRequest();
			} else if (specialType.equals(REST_UID)) {
				CeStoreRestApiSpecialUid uidHandler = new CeStoreRestApiSpecialUid(pWc, pRestParts, pRequest);
				uidHandler.processRequest();
			} else if (specialType.equals(REST_SEARCH)) {
				handler.processSearchRequest();
			} else if (specialType.equals(REST_SHADCON)) {
				handler.processShadowConceptRequest();
			} else if (specialType.equals(REST_SHADINST)) {
				handler.processShadowInstanceRequest();
			} else if (specialType.equals(REST_UNREFINST)) {
				String immParm = urlDecode(pWc, pRequest.getParameter(PARM_IGMETMOD));
				boolean igMetaModel = false;

				if (immParm.equals("true")) {
					igMetaModel = true;
				}

				handler.processUnreferencedInstanceRequest(igMetaModel);
			} else if (specialType.equals(REST_DIVCONINST)) {
				handler.processDiverseConceptInstanceRequest();
			} else if (specialType.equals(REST_MULTINSTS)) {
				handler.processListInstancesForMultipleConceptsRequest();
			} else {
				handler.reportUnhandledUrl(pRestParts);
			}
		} else {
			handler.reportUnhandledUrl(pRestParts);
		}

		return statsInResponse;
	}

	private boolean processStatisticsRequest() {
		//URL = /special/statistics
		if (isGet()) {
			handleShowStatistics();
		} else if (isPost()) {
			handleRunStatistics();
		} else {
			reportUnsupportedMethodError();
		}

		return true;
	}

	private boolean processPatternRequest() {
		//URL = /special/patterns
		if (isGet()) {
			handleListPatterns();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processRationaleRequest() {
		//URL = /special/rationale
		if (isGet()) {
			handleListRationale();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processSearchRequest() {
		//URL = /special/keyword-search
		if (isGet()) {
			handleKeywordSearch();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processShadowConceptRequest() {
		//URL = /special/shadow-concepts
		if (isGet()) {
			//List shadow concepts
			handleListShadowConcepts();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processShadowInstanceRequest() {
		//URL = /special/shadow-instances
		if (isGet()) {
			//List shadow instances
			handleListShadowInstances();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processUnreferencedInstanceRequest(boolean pIgnoreMetaModel) {
		//URL = /special/unreferenced-instances
		if (isGet()) {
			//List unreferenced instances
			handleListUnreferencedInstances(pIgnoreMetaModel);
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processDiverseConceptInstanceRequest() {
		//URL = /special/diverse-concept-instances
		if (isGet()) {
			//List diverse concept instances
			handleListDiverseConceptInstances();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processListInstancesForMultipleConceptsRequest() {
		//URL = /special/instances-for-multiple-concepts
		if (isGet()) {
			handleListInstancesForMultipleConcepts();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private void handleShowStatistics() {
		if (isJsonRequest()) {
			jsonShowStatistics();
		} else if (isTextRequest()) {
			textShowStatistics();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonShowStatistics() {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		sa.showStoreStatistics();
	}

	private void textShowStatistics() {
		reportNotYetImplementedError();
	}

	private void handleRunStatistics() {
		if (isJsonRequest()) {
			jsonRunStatistics();
		} else if (isTextRequest()) {
			textRunStatistics();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonRunStatistics() {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		sa.runStoreStatistics();
	}

	private void textRunStatistics() {
		reportNotYetImplementedError();
	}

	private void handleListPatterns() {
		if (isJsonRequest()) {
			jsonListPatterns();
		} else if (isTextRequest()) {
			textListPatterns();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListPatterns() {
		ArrayList<CeQuery> queryList = new ArrayList<CeQuery>(getModelBuilder().getAllQueries().values());
		ArrayList<CeRule> ruleList = new ArrayList<CeRule>(getModelBuilder().getAllRules().values());

		setPatternListAsStructuredResult(queryList, ruleList);
	}

	private void textListPatterns() {
		//TODO: Complete this
		reportNotYetImplementedError();
	}

	private void handleListRationale() {
		if (isJsonRequest()) {
			jsonListRationale();
		} else if (isTextRequest()) {
			textListRationale();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListRationale() {
		setRationaleListAsStructuredResult(getModelBuilder().getAllReasoningSteps());
	}

	private void textListRationale() {
		//TODO: Implement this
		reportNotYetImplementedError();
	}

	private void handleKeywordSearch() {
		String rawSearchTerms = getUrlParameterValueNamed(PARM_SEARCHTERMS);
		String[] conNames = getListParameterNamed(PARM_RESTRICTCONNAMES);
		String[] propNames = getListParameterNamed(PARM_RESTRICTPROPNAMES);
		int numSteps = getNumericUrlParameterValueNamed(CeStoreRestApiInstance.PARM_STEPS, -1);
		boolean relInsts = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_RELINSTS, true);
		boolean refInsts = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_REFINSTS, true);
		boolean suppPropTypes = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_SPTS, false);
		String[] limRels = getListParameterNamed(CeStoreRestApiInstance.PARM_LIMRELS);

		boolean retInsts = getBooleanParameterNamed(PARM_RETINSTS);
		boolean caseSen = getBooleanParameterNamed(PARM_CASESEN, this.wc.getCeConfig().isCaseSensitive());

		ArrayList<String> searchTerms = extractKeywordListFrom(rawSearchTerms);

		if (!searchTerms.isEmpty()) {
			if (isJsonRequest()) {
				jsonKeywordSearch(searchTerms, conNames, propNames, retInsts, caseSen, numSteps, relInsts, refInsts, limRels, suppPropTypes);
			} else if (isTextRequest()) {
				textKeywordSearch(searchTerms, conNames, propNames, retInsts, caseSen);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportMissingUrlParameterError(PARM_SEARCHTERMS);
		}
	}

	private void jsonKeywordSearch(ArrayList<String> pSearchTerms, String[] pConNames, String[] pPropNames, boolean pRetInsts, boolean pCaseSensitive, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		ArrayList<ContainerSearchResult> resList = sa.keywordSearch(pSearchTerms, pConNames, pPropNames, pCaseSensitive);
		setSearchListAsStructuredResult(resList, pSearchTerms, pConNames, pPropNames, pRetInsts, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes);
	}

	private void textKeywordSearch(ArrayList<String> pSearchTerms, String[] pConNames, String[] pPropNames, boolean pRetInsts, boolean pCaseSen) {
		//TODO: Implement this
		String searchTermSummary = generateSearchTermSummaryFrom(pSearchTerms);
		reportNotYetImplementedError("keyword search using '" + searchTermSummary + "'");
	}

	private ArrayList<String> extractKeywordListFrom(String pSearchTerms) {
		ArrayList<String> result = new ArrayList<String>();

		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(pSearchTerms);
		while (m.find()) {
			result.add(m.group(1).replace("\"", ""));
		}

		return result;
	}

	public static String generateSearchTermSummaryFrom(ArrayList<String> pSearchTerms) {
		String result = "";
		String sep = "";

		for (String thisTerm : pSearchTerms) {
			if (thisTerm.contains(" ")) {
				result += sep + "\"" + thisTerm + "\"";
			} else {
				result += sep + thisTerm;
			}

			sep = ", ";
		}
		return result;
	}

	private void handleListShadowConcepts() {
		if (isJsonRequest()) {
			jsonListShadowConcepts();
		} else if (isTextRequest()) {
			textListShadowConcepts();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListShadowConcepts() {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		ArrayList<CeConcept> shadowConcepts = sa.listShadowConcepts();

		setConceptListAsStructuredResult(shadowConcepts);
	}

	private void textListShadowConcepts() {
		reportNotYetImplementedError();
	}

	private void handleListShadowInstances() {	
		if (isJsonRequest()) {
			jsonListShadowInstances();
		} else if (isTextRequest()) {
			textListShadowInstances();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void handleListUnreferencedInstances(boolean pIgnoreMetaModel) {
		if (isJsonRequest()) {
			jsonListUnreferencedInstances(pIgnoreMetaModel);
		} else if (isTextRequest()) {
			textListUnreferencedInstances();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListShadowInstances() {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		ArrayList<CeInstance> shadowInstances = sa.listShadowInstances();

		setInstanceListAsStructuredResult(shadowInstances);
	}

	private void jsonListUnreferencedInstances(boolean pIgnoreMetaModel) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		ArrayList<CeInstance> unrefInstances = sa.listUnreferencedInstances(pIgnoreMetaModel);

		setInstanceListAsStructuredResult(unrefInstances);
	}

	private void textListShadowInstances() {
		reportNotYetImplementedError();
	}

	private void textListUnreferencedInstances() {
		reportNotYetImplementedError();
	}

	private void handleListDiverseConceptInstances() {
		if (isJsonRequest()) {
			jsonListDiverseConceptInstances();
		} else if (isTextRequest()) {
			textListDiverseConceptInstances();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void handleListInstancesForMultipleConcepts() {
		String conNames = getUrlParameterValueNamed(PARM_CONNAMES);
		String since = getUrlParameterValueNamed(CeStoreRestApiConcept.PARM_SINCE);
		int numSteps = getNumericUrlParameterValueNamed(CeStoreRestApiInstance.PARM_STEPS, -1);
		boolean relInsts = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_RELINSTS, true);
		boolean refInsts = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_REFINSTS, true);
		String[] limRels = getListParameterNamed(CeStoreRestApiInstance.PARM_LIMRELS);

		if (isJsonRequest()) {
			jsonListInstancesForMultipleConcepts(conNames, since, numSteps, relInsts, refInsts, limRels);
		} else if (isTextRequest()) {
			textListInstancesForMultipleConcepts(conNames, since, numSteps, relInsts, refInsts, limRels);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListDiverseConceptInstances() {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		TreeMap<CeInstance, ArrayList<CeConcept>> dcInstances = sa.listDiverseConceptInstances();

		setDiverseConceptInstanceListAsStructuredResult(dcInstances);
	}

	private void textListDiverseConceptInstances() {
		reportNotYetImplementedError();
	}

	private void jsonListInstancesForMultipleConcepts(String pConNameList, String pSince, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels) {
		TreeMap<String, ArrayList<CeInstance>> result = new TreeMap<String, ArrayList<CeInstance>>();
		String[] conNames = pConNameList.split(",");

		for (String thisConName : conNames) {
			CeConcept tgtCon = this.wc.getModelBuilder().getConceptNamed(this.wc, thisConName);
			ArrayList<CeInstance> instList = null;

			if (tgtCon != null) {
				instList = CeStoreRestApiConcept.makeInstanceListRequestFor(this.wc, tgtCon, pSince);
			}

			result.put(thisConName, instList);
		}

		setMultipleConceptInstanceListAsStructuredResult(result, pNumSteps, pRelInsts, pRefInsts, pLimRels);
	}

	private void textListInstancesForMultipleConcepts(String pConNameList, String pSince, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels) {
		reportNotYetImplementedError("list instances for multiple concepts '" + pConNameList + "' (since=" + pSince + ",numSteps=" + pNumSteps + "");
	}

	private void setPatternListAsStructuredResult(Collection<CeQuery> pQueryList, Collection<CeRule> pRuleList) {
		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(CeWebQueryOrRule.generatePatternSummaryListFrom(pQueryList, pRuleList));
		} else {
			getWebActionResponse().setStructuredResult(CeWebQueryOrRule.generatePatternFullListFrom(pQueryList, pRuleList));
		}
	}

	private void setSearchListAsStructuredResult(ArrayList<ContainerSearchResult> pResults, ArrayList<String> pSearchTerms, String[] pConceptNames, String[] pPropertyNames, boolean pRetInsts, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String []pLimRels, boolean pSuppPropTypes) {
		getWebActionResponse().setStructuredResult(CeWebContainerResult.generateKeywordSearchResultFrom(this.wc, pResults, pSearchTerms, pConceptNames, pPropertyNames, pRetInsts, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes));
	}

	private void setMultipleConceptInstanceListAsStructuredResult(TreeMap<String, ArrayList<CeInstance>> pList, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels) {
		boolean suppPropTypes = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_SPTS, false);

		getWebActionResponse().setStructuredResult(CeWebSpecial.generateMultipleConceptInstanceListFrom(this.wc, this, pList, pNumSteps, pRelInsts, pRefInsts, pLimRels, suppPropTypes));
	}

	private void setDiverseConceptInstanceListAsStructuredResult(TreeMap<CeInstance, ArrayList<CeConcept>> pInstList) {
		getWebActionResponse().setStructuredResult(CeWebSpecial.generateDiverseConceptInstanceListFrom(pInstList));
	}
	
}