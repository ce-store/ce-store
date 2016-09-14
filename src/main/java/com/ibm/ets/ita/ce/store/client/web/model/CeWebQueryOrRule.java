package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_EQUALS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_ATTRIBUTES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_CONCEPTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_QUERIES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RELATIONSHIPS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RULES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_B_INCLUDED;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_B_NEGATED_DOM;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_B_NEGATED_RNG;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_L_QUERY_TIME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_L_RULE_TIME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_CE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_CETEXT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_CONNAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_OPERATOR;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_PREM_OR_CONC;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_PROPFORMAT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_PROPNAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_QR_TYPE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_QUERY_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_RULE_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_SRCVAR;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_TGTVAR;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_VALUE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_S_VARID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.PREFIX_QUOTED_OPERATOR;
import static com.ibm.ets.ita.ce.store.names.JsonNames.QR_TYPE_QUERY;
import static com.ibm.ets.ita.ce.store.names.JsonNames.QR_TYPE_RULE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SQ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeClause;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;

public class CeWebQueryOrRule extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public CeWebQueryOrRule(ActionContext pAc) {
		super(pAc);
	}

	public static CeStoreJsonArray generateQuerySummaryListFrom(Collection<CeQuery> pQueryList) {
		// TODO: Decide what this should be
		return generateQueryListFrom(pQueryList);
	}

	public static CeStoreJsonArray generateQueryMinimalListFrom(Collection<CeQuery> pQueryList) {
		// TODO: Decide what this should be
		return generateQueryListFrom(pQueryList);
	}

	public static CeStoreJsonArray generateQueryNormalisedListFrom(Collection<CeQuery> pQueryList) {
		// TODO: Decide what this should be
		return generateQueryListFrom(pQueryList);
	}

	public static CeStoreJsonArray generateQueryFullListFrom(Collection<CeQuery> pQueryList) {
		// TODO: Decide what this should be
		return generateQueryListFrom(pQueryList);
	}

	public static CeStoreJsonArray generateQueryListFrom(Collection<CeQuery> pQueryList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		if (pQueryList != null) {
			Collections.sort(new ArrayList<CeQuery>(pQueryList));

			for (CeQuery thisQuery : pQueryList) {
				CeStoreJsonObject jObj = new CeStoreJsonObject();

				putStringValueIn(jObj, JSON_S_QR_TYPE, QR_TYPE_QUERY);
				putStringValueIn(jObj, JSON_S_QUERY_NAME, thisQuery.getQueryName());
				putLongValueIn(jObj, JSON_L_QUERY_TIME, thisQuery.getCreationDate());
				putStringValueIn(jObj, JSON_S_CE, thisQuery.getCeText());

				jArr.add(jObj);
			}
		}

		return jArr;
	}

	public static CeStoreJsonArray generateRuleSummaryListFrom(Collection<CeRule> pRuleList) {
		// TODO: Decide what this should be
		return generateRuleListFrom(pRuleList);
	}

	public static CeStoreJsonArray generateRuleMinimalListFrom(Collection<CeRule> pRuleList) {
		// TODO: Decide what this should be
		return generateRuleListFrom(pRuleList);
	}

	public static CeStoreJsonArray generateRuleNormalisedListFrom(Collection<CeRule> pRuleList) {
		// TODO: Decide what this should be
		return generateRuleListFrom(pRuleList);
	}

	public static CeStoreJsonArray generateRuleFullListFrom(Collection<CeRule> pRuleList) {
		// TODO: Decide what this should be
		return generateRuleListFrom(pRuleList);
	}

	public static CeStoreJsonArray generateRuleListFrom(Collection<CeRule> pRuleList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		if (pRuleList != null) {
			Collections.sort(new ArrayList<CeRule>(pRuleList));

			for (CeRule thisRule : pRuleList) {
				CeStoreJsonObject jObj = new CeStoreJsonObject();

				putStringValueIn(jObj, JSON_S_QR_TYPE, QR_TYPE_RULE);
				putStringValueIn(jObj, JSON_S_RULE_NAME, thisRule.getRuleName());
				putLongValueIn(jObj, JSON_L_RULE_TIME, thisRule.getCreationDate());
				putStringValueIn(jObj, JSON_S_CE, thisRule.getCeText());

				jArr.add(jObj);
			}
		}

		return jArr;
	}

	public static CeStoreJsonObject generatePatternSummaryListFrom(Collection<CeQuery> pQueryList,
			Collection<CeRule> pRuleList) {
		// TODO: Decide what this should be
		return generateRuleAndQueryListFrom(pQueryList, pRuleList);
	}

	public static CeStoreJsonObject generatePatternMinimalListFrom(Collection<CeQuery> pQueryList,
			Collection<CeRule> pRuleList) {
		// TODO: Decide what this should be
		return generateRuleAndQueryListFrom(pQueryList, pRuleList);
	}

	public static CeStoreJsonObject generatePatternNormalisedListFrom(Collection<CeQuery> pQueryList,
			Collection<CeRule> pRuleList) {
		// TODO: Decide what this should be
		return generateRuleAndQueryListFrom(pQueryList, pRuleList);
	}

	public static CeStoreJsonObject generatePatternFullListFrom(Collection<CeQuery> pQueryList,
			Collection<CeRule> pRuleList) {
		// TODO: Decide what this should be
		return generateRuleAndQueryListFrom(pQueryList, pRuleList);
	}

	public static CeStoreJsonObject generateRuleAndQueryListFrom(Collection<CeQuery> pQueryList,
			Collection<CeRule> pRuleList) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putArrayValueIn(jObj, JSON_A_QUERIES, generateQueryListFrom(pQueryList));
		putArrayValueIn(jObj, JSON_A_RULES, generateRuleListFrom(pRuleList));

		return jObj;
	}

	public CeStoreJsonObject generateQueryFullDetailsFrom(CeQuery pQuery) {
		// TODO: Decide what to do here
		return generateDetailsFrom(pQuery);
	}

	public CeStoreJsonObject generateQuerySummaryDetailsFrom(CeQuery pQuery) {
		// TODO: Decide what to do here
		return generateDetailsFrom(pQuery);
	}

	public CeStoreJsonObject generateRuleFullDetailsFrom(CeRule pRule) {
		// TODO: Decide what to do here
		return generateDetailsFrom(pRule);
	}

	public CeStoreJsonObject generateRuleSummaryDetailsFrom(CeRule pRule) {
		// TODO: Decide what to do here
		return generateDetailsFrom(pRule);
	}

	public CeStoreJsonObject generatePatternFullDetailsFrom(CeQuery pPattern) {
		// TODO: Decide what to do here
		return generateDetailsFrom(pPattern);
	}

	public CeStoreJsonObject generatePatternSummaryDetailsFrom(CeQuery pPattern) {
		// TODO: Decide what to do here
		return generateDetailsFrom(pPattern);
	}

	public CeStoreJsonObject generateDetailsFrom(CeQuery pQueryOrRule) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_ID, pQueryOrRule.getQueryName());
		putStringValueIn(jObj, JSON_S_CETEXT, pQueryOrRule.getCeText());
		putArrayValueIn(jObj, JSON_A_CONCEPTS, processConcepts(pQueryOrRule));
		putArrayValueIn(jObj, JSON_A_ATTRIBUTES, processAttributes(pQueryOrRule));
		putArrayValueIn(jObj, JSON_A_RELATIONSHIPS, processRelationships(pQueryOrRule));

		return jObj;
	}

	private CeStoreJsonArray processConcepts(CeQuery pQueryOrRule) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		TreeMap<String, CeStoreJsonObject> allConcepts = new TreeMap<String, CeStoreJsonObject>();

		for (CeClause outerClause : pQueryOrRule.getAllDirectClauses()) {
			for (CeClause thisClause : outerClause.getChildClauses()) {
				// Conclusion clauses are treated separately when listing
				// concepts
				if (thisClause.isPremiseClause()) {
					processConceptPremiseClause(pQueryOrRule, thisClause, allConcepts);
				} else {
					String varId = thisClause.getTargetVariable();
					CeStoreJsonObject jObj = processConceptConclusionClause(pQueryOrRule, thisClause, varId);

					if (jObj != null) {
						allConcepts.put(varId, jObj);
					}
				}
			}
		}

		return addAllValuesTo(jArr, allConcepts);
	}

	private void processConceptPremiseClause(CeQuery pQueryOrRule, CeClause pClause,
			TreeMap<String, CeStoreJsonObject> pConMap) {
		String varId = ES;
		CeStoreJsonObject jObj = null;

		if ((pClause.isSimpleClause()) || (pClause.hasNoRelationships())) {
			varId = pClause.getTargetVariable();
			jObj = processConceptSimplePremiseClause(pQueryOrRule, pClause, varId);
			if (jObj != null) {
				pConMap.put(varId, jObj);
			}
		} else {
			for (CePropertyInstance thisPi : pClause.getAllProperties()) {
				if (!thisPi.isSpecialOperatorPropertyInstance()) {
					varId = thisPi.getClauseVariableId();
					jObj = processConceptNormalPremiseClauseDomainVariable(pQueryOrRule, pClause, thisPi, varId);
					if (jObj != null) {
						pConMap.put(varId, jObj);
					}

					varId = thisPi.getSingleOrFirstValue();
					jObj = processConceptNormalPremiseClauseRangeVariable(pQueryOrRule, pClause, thisPi, varId);
					if (jObj != null) {
						pConMap.put(varId, jObj);
					}
				}
			}
		}
	}

	private static CeStoreJsonObject processConceptSimplePremiseClause(CeQuery pQueryOrRule, CeClause pClause,
			String pVarId) {
		CeStoreJsonObject jObj = null;

		if (pClause.getTargetConcept() != null) {
			jObj = new CeStoreJsonObject();
			putStringValueIn(jObj, JSON_S_VARID, pVarId);
			putStringValueIn(jObj, JSON_S_CONNAME, pClause.getTargetConcept().getConceptName());
			putBooleanValueIn(jObj, JSON_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
			putStringValueIn(jObj, JSON_S_PREM_OR_CONC, pClause.formattedClauseType());
		}

		return jObj;
	}

	private static CeStoreJsonObject processConceptNormalPremiseClauseDomainVariable(CeQuery pQueryOrRule,
			CeClause pClause, CePropertyInstance pPi, String pVarId) {
		CeStoreJsonObject jObj = null;

		if (!pPi.getRelatedProperty().isDatatypeProperty()) {
			String encVarId = pVarId;
			if (pClause.targetVariableWasQuoted()) {
				encVarId = TOKEN_SQ + pVarId + TOKEN_SQ;
			}

			jObj = new CeStoreJsonObject();
			putStringValueIn(jObj, JSON_S_VARID, encVarId);
			putStringValueIn(jObj, JSON_S_CONNAME, pPi.getRelatedProperty().calculateDomainConceptName());
			putBooleanValueIn(jObj, JSON_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
			putStringValueIn(jObj, JSON_S_PREM_OR_CONC, pClause.formattedClauseType());
		}

		return jObj;
	}

	private CeStoreJsonObject processConceptNormalPremiseClauseRangeVariable(CeQuery pQueryOrRule, CeClause pClause,
			CePropertyInstance pPi, String pVarId) {
		CeStoreJsonObject jObj = null;

		if (!pPi.getRelatedProperty().isDatatypeProperty()) {
			String encVarId = pVarId;
			if (pPi.hadQuotesOriginally(this.ac)) {
				encVarId = TOKEN_SQ + pVarId + TOKEN_SQ;
			}

			jObj = new CeStoreJsonObject();
			putStringValueIn(jObj, JSON_S_VARID, encVarId);
			putStringValueIn(jObj, JSON_S_CONNAME, pPi.getSingleOrFirstRangeName());
			putBooleanValueIn(jObj, JSON_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
			putStringValueIn(jObj, JSON_S_PREM_OR_CONC, pClause.formattedClauseType());
		}

		return jObj;
	}

	private static CeStoreJsonObject processConceptConclusionClause(CeQuery pQueryOrRule, CeClause pClause,
			String pVarId) {
		CeStoreJsonObject jObj = null;

		if (pClause.isSimpleClause()) {
			if (pClause.getTargetConcept() != null) {
				jObj = new CeStoreJsonObject();
				String encVarId = pVarId;
				if (pClause.targetVariableWasQuoted()) {
					encVarId = TOKEN_SQ + pVarId + TOKEN_SQ;
				}

				putStringValueIn(jObj, JSON_S_VARID, encVarId);
				putStringValueIn(jObj, JSON_S_CONNAME, pClause.getTargetConcept().getConceptName());
				putBooleanValueIn(jObj, JSON_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
				putStringValueIn(jObj, JSON_S_PREM_OR_CONC, pClause.formattedClauseType());
			}
		}

		return jObj;
	}

	private CeStoreJsonArray processAttributes(CeQuery pQueryOrRule) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		TreeMap<String, CeStoreJsonObject> allAttributes = new TreeMap<String, CeStoreJsonObject>();
		int ctr = 1; // A counter for incrementing variable names if needed

		// Attributes (datatype properties)
		for (CeClause outerClause : pQueryOrRule.getAllDirectClauses()) {
			for (CeClause thisClause : outerClause.getChildClauses()) {
				for (CePropertyInstance thisPi : thisClause.getDatatypeProperties()) {
					String varId = ES;
					CeStoreJsonObject jObj = null;

					if (thisPi.isSpecialOperatorPropertyInstance()) {
						if (thisPi.hadQuotesOriginally(this.ac)) {
							varId = PREFIX_QUOTED_OPERATOR + Integer.toString(ctr++);
							jObj = extractJsonFrom(allAttributes, varId);
							processAttributeSpecialOperatorWithQuotes(jObj, pQueryOrRule, thisClause, thisPi, varId);
						} else {
							varId = thisPi.getClauseVariableId();
							jObj = extractJsonFrom(allAttributes, varId);
							processAttributeSpecialOperatorNoQuotes(jObj, thisPi);
						}
					} else {
						varId = thisPi.getSingleOrFirstValue();
						jObj = extractJsonFrom(allAttributes, varId);
						processAttributeNormal(jObj, pQueryOrRule, thisClause, thisPi, varId);
					}
				}
			}
		}

		return addAllValuesTo(jArr, allAttributes);
	}

	private void processAttributeSpecialOperatorWithQuotes(CeStoreJsonObject pJsonObj, CeQuery pQueryOrRule,
			CeClause pClause, CePropertyInstance pPi, String pVarId) {
		putStringValueIn(pJsonObj, JSON_S_VARID, pVarId);
		putStringValueIn(pJsonObj, JSON_S_SRCVAR, pPi.getClauseVariableId());
		putStringValueIn(pJsonObj, JSON_S_PROPNAME, pPi.getPropertyName());
		putStringValueIn(pJsonObj, JSON_S_PROPFORMAT, pPi.getRelatedProperty().formattedCeStyle());
		putStringValueIn(pJsonObj, JSON_S_PREM_OR_CONC, pClause.formattedClauseType());
		putStringValueIn(pJsonObj, JSON_S_OPERATOR, SPECIALNAME_EQUALS);
		putStringValueIn(pJsonObj, JSON_S_VALUE, pPi.getSingleOrFirstValue());
		putBooleanValueIn(pJsonObj, JSON_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
		putBooleanValueIn(pJsonObj, JSON_B_NEGATED_DOM, pClause.isTargetConceptNegated());
		putBooleanValueIn(pJsonObj, JSON_B_NEGATED_RNG, pPi.isNegated(this.ac));
	}

	private static void processAttributeSpecialOperatorNoQuotes(CeStoreJsonObject pJsonObj, CePropertyInstance pPi) {
		putStringValueIn(pJsonObj, JSON_S_OPERATOR, pPi.getPropertyName());
		putStringValueIn(pJsonObj, JSON_S_VALUE, pPi.getSingleOrFirstValue());
	}

	private void processAttributeNormal(CeStoreJsonObject pJsonObj, CeQuery pQueryOrRule, CeClause pClause,
			CePropertyInstance pPi, String pVarId) {
		String encVarId = pVarId;
		if (pPi.hadQuotesOriginally(this.ac)) {
			encVarId = TOKEN_SQ + pVarId + TOKEN_SQ;
		}

		putStringValueIn(pJsonObj, JSON_S_VARID, encVarId);
		putStringValueIn(pJsonObj, JSON_S_SRCVAR, pPi.getClauseVariableId());
		putStringValueIn(pJsonObj, JSON_S_PROPNAME, pPi.getPropertyName());
		putStringValueIn(pJsonObj, JSON_S_PROPFORMAT, pPi.getRelatedProperty().formattedCeStyle());
		putStringValueIn(pJsonObj, JSON_S_PREM_OR_CONC, pClause.formattedClauseType());
		putBooleanValueIn(pJsonObj, JSON_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
		putBooleanValueIn(pJsonObj, JSON_B_NEGATED_DOM, pClause.isTargetConceptNegated());
		putBooleanValueIn(pJsonObj, JSON_B_NEGATED_RNG, pPi.isNegated(this.ac));
	}

	private CeStoreJsonArray processRelationships(CeQuery pQueryOrRule) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeClause outerClause : pQueryOrRule.getAllDirectClauses()) {
			for (CeClause thisClause : outerClause.getChildClauses()) {
				for (CePropertyInstance thisPi : thisClause.getObjectProperties()) {
					CeStoreJsonObject jObj = new CeStoreJsonObject();

					String encSrcVarId = thisPi.getClauseVariableId();
					if (thisClause.targetVariableWasQuoted()) {
						encSrcVarId = TOKEN_SQ + thisPi.getClauseVariableId() + TOKEN_SQ;
					}

					String encTgtVarId = thisPi.getSingleOrFirstValue();
					if (thisPi.hadQuotesOriginally(this.ac)) {
						encTgtVarId = TOKEN_SQ + thisPi.getSingleOrFirstValue() + TOKEN_SQ;
					}

					putStringValueIn(jObj, JSON_S_SRCVAR, encSrcVarId);
					putStringValueIn(jObj, JSON_S_TGTVAR, encTgtVarId);
					putStringValueIn(jObj, JSON_S_PROPNAME, thisPi.getPropertyName());
					putStringValueIn(jObj, JSON_S_PROPFORMAT, thisPi.getRelatedProperty().formattedCeStyle());
					putStringValueIn(jObj, JSON_S_PREM_OR_CONC, thisClause.formattedClauseType());
					putBooleanValueIn(jObj, JSON_B_NEGATED_DOM, thisClause.isTargetConceptNegated());
					putBooleanValueIn(jObj, JSON_B_NEGATED_RNG, thisPi.isNegated(this.ac));
					jArr.add(jObj);
				}
			}
		}

		return jArr;
	}

	private static CeStoreJsonObject extractJsonFrom(TreeMap<String, CeStoreJsonObject> pList, String pKey) {
		CeStoreJsonObject result = pList.get(pKey);

		if (result == null) {
			result = new CeStoreJsonObject();
			pList.put(pKey, result);
		}

		return result;
	}

	private static CeStoreJsonArray addAllValuesTo(CeStoreJsonArray pJsonArray,
			TreeMap<String, CeStoreJsonObject> pValueTree) {
		// Now add all the attributes to the returned JSON array
		for (CeStoreJsonObject thisJatt : pValueTree.values()) {
			addObjectValueTo(pJsonArray, thisJatt);
		}

		return pJsonArray;
	}

}
