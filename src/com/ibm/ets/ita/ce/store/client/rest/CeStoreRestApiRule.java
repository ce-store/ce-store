package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebContainerResult;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebQueryOrRule;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeStoreRestApiRule extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String TYPE_RULE = "rule";
	private static final String PARM_SUPPCE = "suppressCe";

	public CeStoreRestApiRule(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/rules
	 * 		/rules/{name}
	 * 		/rules/{name}/execute
	 * 		/rules/{name}/rationale
	*/
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			statsInResponse = processOneElementRequest();
		} else {
			String ruleName = this.restParts.get(1);
//			String ruleName = StaticFunctionsGeneral.decodeFromUrl(this.wc, rawRuleName);

			CeRule tgtRule = getModelBuilder().getRuleNamed(ruleName);

			if (tgtRule != null) {
				if (this.restParts.size() == 2) {
					statsInResponse = processTwoElementRequest(tgtRule);
				} else {				
					if (this.restParts.size() == 3) {
						statsInResponse = processThreeElementRequest(tgtRule);
					} else {
						reportUnhandledUrl();
					}
				}
			} else {
				reportNotFoundError(ruleName);
			}
		}

		return statsInResponse;
	}

	private boolean processOneElementRequest() {
		if (isGet()) {
			//URL = /rules
			//List all rules
			handleListRules();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processTwoElementRequest(CeRule pTgtRule) {
		boolean statsInResponse = false;

		//URL = /rules/{name}
		if (isGet()) {
			//GET rule details
			handleGetRuleDetails(pTgtRule);
		} else if (isDelete()) {
			//DELETE rule
			handleDeleteRule(pTgtRule);
			statsInResponse = true;
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private boolean processThreeElementRequest(CeRule pTgtRule) {
		boolean statsInResponse = false;
		String qualifier = this.restParts.get(2);

		if (qualifier.equals(REST_EXECUTE)) {
			statsInResponse = processThreeElementExecuteRequest(pTgtRule);
		} else if (qualifier.equals(REST_RATIONALE)) {
			processThreeElementRationaleRequest(pTgtRule);
		} else {
			reportUnhandledUrl();
		}

		return statsInResponse;
	}

	private boolean processThreeElementExecuteRequest(CeRule pTgtRule) {
		boolean statsInResponse = false;

		//URL = /rules/{name}/execute
		if (isGet()) {
			//Execute rule as query
			handleExecuteRuleAsQuery(pTgtRule);
		} else if (isPost()) {
			//Execute rule as rule (possibly inserting CE sentences into store)
			handleExecuteRule(pTgtRule);
			statsInResponse = true;
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private void processThreeElementRationaleRequest(CeRule pTgtRule) {
		//URL = /rules/{name}/rationale
		//List rationale for rule
		if (isGet()) {
			handleListAllRationaleForRule(pTgtRule);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void handleListRules() {
		if (isJsonRequest()) {
			jsonListRules();
		} else if (isTextRequest()) {
			textListRules();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListRules() {
		setRuleListAsStructuredResult(getModelBuilder().getAllRules().values());
	}

	private void textListRules() {
		//TODO: Complete this
		reportNotYetImplementedError();
	}

	private void handleGetRuleDetails(CeRule pTgtRule) {
		if (isJsonRequest()) {
			jsonGetRuleDetails(pTgtRule);
		} else if (isTextRequest()) {
			textGetRuleDetails(pTgtRule);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void handleDeleteRule(CeRule pTgtRule) {
		//TODO: Implement this
		reportNotYetImplementedError("delete rule named '" + pTgtRule.getRuleName() + "'");
	}

	private void jsonGetRuleDetails(CeRule pTgtRule) {
		setRuleDetailsAsStructuredResult(pTgtRule);
	}

	private void textGetRuleDetails(CeRule pTgtRule) {
		//TODO: Complete this
		reportNotYetImplementedError("get rule details for '" + pTgtRule.getRuleName() + "'");
	}

	private void handleExecuteRuleAsQuery(CeRule pTgtRule) {
		if (isJsonRequest()) {
			jsonExecuteRuleAsQuery(pTgtRule);
		} else if (isTextRequest()) {
			textExecuteRuleAsQuery(pTgtRule);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonExecuteRuleAsQuery(CeRule pTgtRule) {
		ContainerCeResult result = actionExecuteRuleAsQuery(pTgtRule);
		boolean returnInstances = getBooleanUrlParameterValueNamed(PARM_RETINSTS, false);

		setCeResultAsStructuredResult(result, returnInstances);
	}

	private void textExecuteRuleAsQuery(CeRule pTgtRule) {
		ContainerCeResult result = actionExecuteRuleAsQuery(pTgtRule);

		getWebActionResponse().setPlainTextPayload(this.wc, CeWebContainerResult.generateCeOnlyCeQueryResultFrom(this.wc, result));
	}

	private ContainerCeResult actionExecuteRuleAsQuery(CeRule pTgtRule) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		String startTs = getUrlParameterValueNamed(PARM_STARTTS);
		String endTs = getUrlParameterValueNamed(PARM_ENDTS);
		boolean suppressCE = getBooleanUrlParameterValueNamed(PARM_SUPPCE, false);
		ContainerCeResult result = sa.executeUserSpecifiedCeQueryByName(pTgtRule.getRuleName(), suppressCE, startTs, endTs);

		return result;
	}

	private void handleExecuteRule(CeRule pTgtRule) {
		if (isJsonRequest()) {
			jsonExecuteRule(pTgtRule);
		} else if (isTextRequest()) {
			textExecuteRule(pTgtRule);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonExecuteRule(CeRule pTgtRule) {
		ContainerCeResult result = actionExecuteRule(pTgtRule);
		boolean returnInstances = getBooleanUrlParameterValueNamed(PARM_RETINSTS, false);

		setCeResultAsStructuredResult(result, returnInstances);
	}

	private void textExecuteRule(CeRule pTgtRule) {
		ContainerCeResult result = actionExecuteRule(pTgtRule);

		getWebActionResponse().setPlainTextPayload(this.wc, CeWebContainerResult.generateCeOnlyCeQueryResultFrom(this.wc, result));
	}

	private ContainerCeResult actionExecuteRule(CeRule pTgtRule) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		String startTs = getUrlParameterValueNamed(PARM_STARTTS);
		String endTs = getUrlParameterValueNamed(PARM_ENDTS);
		ContainerCeResult result = sa.executeUserSpecifiedCeRuleByName(pTgtRule.getRuleName(), "", startTs, endTs);

		return result;
	}

	private void handleListAllRationaleForRule(CeRule pRule) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonGetRuleRationale(pRule);
			} else if (isTextRequest()) {
				textGetRuleRationale(pRule);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonGetRuleRationale(CeRule pRule) {
		ArrayList<CeRationaleReasoningStep> ratList = getModelBuilder().getReasoningStepsForRule(pRule.getRuleName());
		setRationaleListAsStructuredResult(ratList);
	}

	private void textGetRuleRationale(CeRule pRule) {
		//TODO: Implement this
		reportNotYetImplementedError("get rule rationale for rule named '" + pRule.getRuleName() + "'");
	}

	private void reportNotFoundError(String pRuleName) {
		reportNotFoundError(TYPE_RULE, pRuleName);
	}

	private void setRuleListAsStructuredResult(Collection<CeRule> pRuleList) {
		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(CeWebQueryOrRule.generateRuleSummaryListFrom(pRuleList));
		} else {
			getWebActionResponse().setStructuredResult(CeWebQueryOrRule.generateRuleFullListFrom(pRuleList));
		}
	}

	private void setRuleDetailsAsStructuredResult(CeRule pRule) {
		CeWebQueryOrRule pattWeb = new CeWebQueryOrRule(this.wc);

		if (isDefaultStyle() || isFullStyle()) {
			getWebActionResponse().setStructuredResult(pattWeb.generateRuleFullDetailsFrom(pRule));
		} else {
			getWebActionResponse().setStructuredResult(pattWeb.generateRuleSummaryDetailsFrom(pRule));
		}
	}

}