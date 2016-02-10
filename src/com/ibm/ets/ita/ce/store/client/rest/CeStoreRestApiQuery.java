package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebContainerResult;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebQueryOrRule;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;

public class CeStoreRestApiQuery extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String TYPE_QUERY = "query";
	private static final String PARM_SUPPCE = "suppressCe";

	public CeStoreRestApiQuery(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/queries
	 * 		/queries/{name}
	 * 		/queries/{name}/execute
	*/
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			processOneElementRequest();
		} else {
			String queryName = this.restParts.get(1);

			CeQuery tgtQuery = getModelBuilder().getQueryNamed(queryName);

			if (tgtQuery != null) {
				if (this.restParts.size() == 2) {
					statsInResponse = processTwoElementRequest(tgtQuery);
				} else {				
					if (this.restParts.size() == 3) {
						processThreeElementRequest(tgtQuery);
					} else {
						reportUnhandledUrl();
					}
				}
			} else {
				reportNotFoundError(queryName);
			}
		}

		return statsInResponse;
	}

	private void processOneElementRequest() {
		if (isGet()) {
			//URL = /queries
			//List all queries
			handleListQueries();
		} else {
			reportUnsupportedMethodError();
		}
	}

	private boolean processTwoElementRequest(CeQuery pTgtQuery) {
		boolean statsInResponse = false;

		//URL = /queries/{name}
		if (isGet()) {
			//GET query details
			handleGetQueryDetails(pTgtQuery);
		} else if (isDelete()) {
			//DELETE query
			handleDeleteQuery(pTgtQuery);
			statsInResponse = true;
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private void processThreeElementRequest(CeQuery pTgtQuery) {
		String qualifier = this.restParts.get(2);

		if (isGet()) {
			if (qualifier.equals(REST_EXECUTE)) {
				handleExecuteQuery(pTgtQuery);
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void handleListQueries() {
		if (isJsonRequest()) {
			jsonListQueries();
		} else if (isTextRequest()) {
			textListQueries();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListQueries() {
		setQueryListAsStructuredResult(getModelBuilder().getAllQueries().values());
	}

	private void textListQueries() {
		//TODO: Complete this
		reportNotYetImplementedError();
	}

	private void handleGetQueryDetails(CeQuery pTgtQuery) {
		if (isJsonRequest()) {
			jsonGetQueryDetails(pTgtQuery);
		} else if (isTextRequest()) {
			textGetQueryDetails(pTgtQuery);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetQueryDetails(CeQuery pTgtQuery) {
		setQueryDetailsAsStructuredResult(pTgtQuery);
	}

	private void textGetQueryDetails(CeQuery pTgtQuery) {
		//TODO: Complete this
		reportNotYetImplementedError("get query details for '" + pTgtQuery.getQueryName() + "'");
	}

	private void handleDeleteQuery(CeQuery pQuery) {
		if (isJsonRequest()) {
			jsonDeleteQuery(pQuery);
		} else if (isTextRequest()) {
			textDeleteQuery(pQuery);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteQuery(CeQuery pQuery) {
		setActionOutcomeAsStructuredResult(actionDeleteQuery(pQuery));
	}

	private void textDeleteQuery(CeQuery pQuery) {
		String summaryResult = actionDeleteQuery(pQuery);

		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteQuery(CeQuery pQuery) {
		ModelBuilder.deleteQuery(this.wc, pQuery);

		return "Query '" + pQuery.getQueryName() + "' has been deleted";
	}

	private void handleExecuteQuery(CeQuery pTgtQuery) {
		//URL = /queries/{name}/execute
		//Execute query
		if (isJsonRequest()) {
			jsonExecuteQuery(pTgtQuery);
		} else if (isTextRequest()) {
			textExecuteQuery(pTgtQuery);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonExecuteQuery(CeQuery pTgtQuery) {
		boolean returnInstances = getBooleanUrlParameterValueNamed(PARM_RETINSTS, false);
		ContainerCeResult result = actionExecuteQuery(pTgtQuery);

		setCeResultAsStructuredResult(result, returnInstances);
	}

	private void textExecuteQuery(CeQuery pTgtQuery) {
		ContainerCeResult result = actionExecuteQuery(pTgtQuery);

		getWebActionResponse().setPlainTextPayload(this.wc, CeWebContainerResult.generateCeOnlyCeQueryResultFrom(this.wc, result));
	}

	private ContainerCeResult actionExecuteQuery(CeQuery pTgtQuery) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		String startTs = getUrlParameterValueNamed(PARM_STARTTS);
		String endTs = getUrlParameterValueNamed(PARM_ENDTS);
		boolean suppressCe = getBooleanUrlParameterValueNamed(PARM_SUPPCE, false);
		ContainerCeResult result = sa.executeUserSpecifiedCeQueryByName(pTgtQuery.getQueryName(), suppressCe, startTs, endTs);

		return result;
	}

	private void reportNotFoundError(String pQueryName) {
		reportNotFoundError(TYPE_QUERY, pQueryName);
	}

	private void setQueryListAsStructuredResult(Collection<CeQuery> pQueryList) {
		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(CeWebQueryOrRule.generateQuerySummaryListFrom(pQueryList));
		} else {
			getWebActionResponse().setStructuredResult(CeWebQueryOrRule.generateQueryFullListFrom(pQueryList));
		}
	}

	private void setQueryDetailsAsStructuredResult(CeQuery pQuery) {
		CeWebQueryOrRule pattWeb = new CeWebQueryOrRule(this.wc);

		if (isDefaultStyle() || isFullStyle()) {
			getWebActionResponse().setStructuredResult(pattWeb.generateQueryFullDetailsFrom(pQuery));
		} else {
			getWebActionResponse().setStructuredResult(pattWeb.generateQuerySummaryDetailsFrom(pQuery));
		}
	}

}