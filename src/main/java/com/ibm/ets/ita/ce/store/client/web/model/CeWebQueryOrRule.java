package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeClause;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSpecialProperty;

public class CeWebQueryOrRule extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//JSON Response Keys
	private static final String KEY_A_QUERIES = "queries";
	private static final String KEY_A_RULES = "rules";

//	private static final String KEY_SQL = "sql";

	private static final String KEY_S_CETEXT = "ce_text";
	private static final String KEY_A_CONCEPTS = "concepts";
	private static final String KEY_A_ATTRIBUTES = "attributes";
	private static final String KEY_A_RELATIONSHIPS = "relationships";

	private static final String KEY_S_CE = "ce";	
	private static final String KEY_S_QR_TYPE = "qr_type";
	private static final String KEY_S_QUERY_NAME = "query_name";
	private static final String KEY_L_QUERY_TIME = "query_time";
	private static final String KEY_S_RULE_NAME = "rule_name";
	private static final String KEY_L_RULE_TIME = "rule_time";

	private static final String KEY_S_PROPNAME = "property_name";
	private static final String KEY_S_PROPFORMAT = "property_format";
	private static final String KEY_S_PREM_OR_CONC = "premise_or_conclusion";
	private static final String KEY_S_SRCVAR = "source_variable";
	private static final String KEY_S_TGTVAR = "target_variable";
	private static final String KEY_S_CONNAME = "concept_name";
	
	private static final String KEY_S_VALUE = "value";
	private static final String KEY_S_OPERATOR = "operator";
	private static final String KEY_S_VARID = "variable_id";
	private static final String KEY_B_INCLUDED = "included";
	private static final String KEY_B_NEGATED_DOM = "negated_domain";
	private static final String KEY_B_NEGATED_RNG = "negated_range";
//	private static final String KEY_D_XPOS = "x_pos";
//	private static final String KEY_D_YPOS = "y_pos";
	
	private static final String QR_TYPE_RULE = "RULE";
	private static final String QR_TYPE_QUERY = "QUERY";

	private static final String PREFIX_QUOTED_OPERATOR = "T_";
	
	public CeWebQueryOrRule(ActionContext pAc) {
		super(pAc);
	}

	
	public static CeStoreJsonArray generateQuerySummaryListFrom(Collection<CeQuery> pQueryList) {
		//TODO: Decide what this should be
		return generateQueryListFrom(pQueryList);
	}

	public static CeStoreJsonArray generateQueryMinimalListFrom(Collection<CeQuery> pQueryList) {
		//TODO: Decide what this should be
		return generateQueryListFrom(pQueryList);
	}

	public static CeStoreJsonArray generateQueryFullListFrom(Collection<CeQuery> pQueryList) {
		//TODO: Decide what this should be
		return generateQueryListFrom(pQueryList);
	}

	//Query List response structure:
	//	[]
	//	KEY_QR_TYPE
	//	KEY_QUERY_NAME
	//	KEY_QUERY_TIME
	//	KEY_CE
	public static CeStoreJsonArray generateQueryListFrom(Collection<CeQuery> pQueryList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		if (pQueryList != null) {
			Collections.sort(new ArrayList<CeQuery>(pQueryList));
			
			for (CeQuery thisQuery : pQueryList) {
				CeStoreJsonObject jObj = new CeStoreJsonObject();
				
				putStringValueIn(jObj, KEY_S_QR_TYPE, QR_TYPE_QUERY);
				putStringValueIn(jObj, KEY_S_QUERY_NAME, thisQuery.getQueryName());
				putLongValueIn(jObj, KEY_L_QUERY_TIME, thisQuery.getCreationDate());
				putStringValueIn(jObj, KEY_S_CE, thisQuery.getCeText());

				jArr.add(jObj);
			}
		}
		
		return jArr;
	}

	public static CeStoreJsonArray generateRuleSummaryListFrom(Collection<CeRule> pRuleList) {
		//TODO: Decide what this should be
		return generateRuleListFrom(pRuleList);
	}

	public static CeStoreJsonArray generateRuleMinimalListFrom(Collection<CeRule> pRuleList) {
		//TODO: Decide what this should be
		return generateRuleListFrom(pRuleList);
	}

	public static CeStoreJsonArray generateRuleFullListFrom(Collection<CeRule> pRuleList) {
		//TODO: Decide what this should be
		return generateRuleListFrom(pRuleList);
	}

	//Query List response structure:
	//	[]
	//	KEY_QR_TYPE
	//	KEY_RULE_NAME
	//	KEY_RULE_TIME
	//	KEY_CE
	public static CeStoreJsonArray generateRuleListFrom(Collection<CeRule> pRuleList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		if (pRuleList != null) {
			Collections.sort(new ArrayList<CeRule>(pRuleList));
			
			for (CeRule thisRule : pRuleList) {
				CeStoreJsonObject jObj = new CeStoreJsonObject();
				
				putStringValueIn(jObj, KEY_S_QR_TYPE, QR_TYPE_RULE);
				putStringValueIn(jObj, KEY_S_RULE_NAME, thisRule.getRuleName());
				putLongValueIn(jObj, KEY_L_RULE_TIME, thisRule.getCreationDate());
				putStringValueIn(jObj, KEY_S_CE, thisRule.getCeText());

				jArr.add(jObj);
			}
		}
		
		return jArr;
	}

	public static CeStoreJsonObject generatePatternSummaryListFrom(Collection<CeQuery> pQueryList, Collection<CeRule> pRuleList) {
		//TODO: Decide what this should be
		return generateRuleAndQueryListFrom(pQueryList, pRuleList);
	}

	public static CeStoreJsonObject generatePatternMinimalListFrom(Collection<CeQuery> pQueryList, Collection<CeRule> pRuleList) {
		//TODO: Decide what this should be
		return generateRuleAndQueryListFrom(pQueryList, pRuleList);
	}

	public static CeStoreJsonObject generatePatternFullListFrom(Collection<CeQuery> pQueryList, Collection<CeRule> pRuleList) {
		//TODO: Decide what this should be
		return generateRuleAndQueryListFrom(pQueryList, pRuleList);
	}

	//Query List response structure:
	//	KEY_QUERIES[]
	//		See generateQueryListFrom()
	//	KEY_RULES[]
	//		See generateRuleListFrom()
	public static CeStoreJsonObject generateRuleAndQueryListFrom(Collection<CeQuery> pQueryList, Collection<CeRule> pRuleList) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		
		putArrayValueIn(jObj, KEY_A_QUERIES, generateQueryListFrom(pQueryList));
		putArrayValueIn(jObj, KEY_A_RULES, generateRuleListFrom(pRuleList));

		return jObj;
	}
	
	public CeStoreJsonObject generateQueryFullDetailsFrom(CeQuery pQuery) {
		//TODO: Decide what to do here
		return generateDetailsFrom(pQuery);
	}
	
	public CeStoreJsonObject generateQuerySummaryDetailsFrom(CeQuery pQuery) {
		//TODO: Decide what to do here
		return generateDetailsFrom(pQuery);
	}
	
	public CeStoreJsonObject generateRuleFullDetailsFrom(CeRule pRule) {
		//TODO: Decide what to do here
		return generateDetailsFrom(pRule);
	}
	
	public CeStoreJsonObject generateRuleSummaryDetailsFrom(CeRule pRule) {
		//TODO: Decide what to do here
		return generateDetailsFrom(pRule);
	}
	
	public CeStoreJsonObject generatePatternFullDetailsFrom(CeQuery pPattern) {
		//TODO: Decide what to do here
		return generateDetailsFrom(pPattern);
	}
	
	public CeStoreJsonObject generatePatternSummaryDetailsFrom(CeQuery pPattern) {
		//TODO: Decide what to do here
		return generateDetailsFrom(pPattern);
	}
	
	//Query Details response structure:
	//	KEY_NAME
	//	KEY_CETEXT
	//	KEY_CONCEPTS[]
	//		See processConcepts()
	//	KEY_ATTRIBUTES[]
	//		See processAttributes()
	//	KEY_RELATIONSHIPS[]
	//		See processRelationships()
	public CeStoreJsonObject generateDetailsFrom(CeQuery pQueryOrRule) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		
		putStringValueIn(jObj, KEY_ID, pQueryOrRule.getQueryName());
		putStringValueIn(jObj, KEY_S_CETEXT, pQueryOrRule.getCeText());
		putArrayValueIn(jObj, KEY_A_CONCEPTS, processConcepts(pQueryOrRule));
		putArrayValueIn(jObj, KEY_A_ATTRIBUTES, processAttributes(pQueryOrRule));
		putArrayValueIn(jObj, KEY_A_RELATIONSHIPS, processRelationships(pQueryOrRule));

//		String sqlText = calculateSqlFor(pQueryOrRule);
//
//		if (sqlText != null) {
//			putStringValueIn(jObj, KEY_SQL, sqlText);
//		}

		return jObj;
	}

//	private String calculateSqlFor(CeQuery pQueryOrRule) {
//		String result = null;
//
//		if (pQueryOrRule.isDatabaseBacked(this.ac)) {
//			result = pQueryOrRule.calculateSql(this.ac);
//		}
//
//		return result;
//	}

	private CeStoreJsonArray processConcepts(CeQuery pQueryOrRule) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		TreeMap<String, CeStoreJsonObject> allConcepts = new TreeMap<String, CeStoreJsonObject>();

		for (CeClause outerClause : pQueryOrRule.getAllDirectClauses()) {
			for (CeClause thisClause : outerClause.getChildClauses()) {
				//Conclusion clauses are treated separately when listing concepts
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
	
	private void processConceptPremiseClause(CeQuery pQueryOrRule, CeClause pClause, TreeMap<String, CeStoreJsonObject> pConMap) {
		String varId = "";
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

	private static CeStoreJsonObject processConceptSimplePremiseClause(CeQuery pQueryOrRule, CeClause pClause, String pVarId) {
		CeStoreJsonObject jObj = null;

		if (pClause.getTargetConcept() != null) {
			jObj = new CeStoreJsonObject();
			putStringValueIn(jObj, KEY_S_VARID, pVarId);
			putStringValueIn(jObj, KEY_S_CONNAME, pClause.getTargetConcept().getConceptName());
			putBooleanValueIn(jObj, KEY_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
			putStringValueIn(jObj, KEY_S_PREM_OR_CONC, pClause.formattedClauseType());
//			putDoubleValueIn(jObj, KEY_D_XPOS, pQueryOrRule.getXPosFor(pVarId));
//			putDoubleValueIn(jObj, KEY_D_YPOS, pQueryOrRule.getYPosFor(pVarId));
		}

		return jObj;
	}

	private static CeStoreJsonObject processConceptNormalPremiseClauseDomainVariable(CeQuery pQueryOrRule, CeClause pClause, CePropertyInstance pPi, String pVarId) {
		CeStoreJsonObject jObj = null;

		if (!pPi.getRelatedProperty().isDatatypeProperty()) {
			String encVarId = pVarId;
			if (pClause.targetVariableWasQuoted()) {
				encVarId = "'" + pVarId + "'";
			}
			
			jObj = new CeStoreJsonObject();			
			putStringValueIn(jObj, KEY_S_VARID, encVarId);
			putStringValueIn(jObj, KEY_S_CONNAME, pPi.getRelatedProperty().calculateDomainConceptName());
			putBooleanValueIn(jObj, KEY_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
			putStringValueIn(jObj, KEY_S_PREM_OR_CONC, pClause.formattedClauseType());
//			putDoubleValueIn(jObj, KEY_D_XPOS, pQueryOrRule.getXPosFor(pVarId));
//			putDoubleValueIn(jObj, KEY_D_YPOS, pQueryOrRule.getYPosFor(pVarId));
		}

		return jObj;
	}

	private CeStoreJsonObject processConceptNormalPremiseClauseRangeVariable(CeQuery pQueryOrRule, CeClause pClause, CePropertyInstance pPi, String pVarId) {
		CeStoreJsonObject jObj = null;

		if (!pPi.getRelatedProperty().isDatatypeProperty()) {
			String encVarId = pVarId;
			if (pPi.hadQuotesOriginally(this.ac)) {
				encVarId = "'" + pVarId + "'";
			}

			jObj = new CeStoreJsonObject();
			putStringValueIn(jObj, KEY_S_VARID, encVarId);
			putStringValueIn(jObj, KEY_S_CONNAME, pPi.getSingleOrFirstRangeName());
			putBooleanValueIn(jObj, KEY_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
			putStringValueIn(jObj, KEY_S_PREM_OR_CONC, pClause.formattedClauseType());
//			putDoubleValueIn(jObj, KEY_D_XPOS, pQueryOrRule.getXPosFor(pVarId));
//			putDoubleValueIn(jObj, KEY_D_YPOS, pQueryOrRule.getYPosFor(pVarId));
		}

		return jObj;
	}

	private static CeStoreJsonObject processConceptConclusionClause(CeQuery pQueryOrRule, CeClause pClause, String pVarId) {
		CeStoreJsonObject jObj = null;

		if (pClause.isSimpleClause()) {
			if (pClause.getTargetConcept() != null) {
				jObj = new CeStoreJsonObject();
				String encVarId = pVarId;
				if (pClause.targetVariableWasQuoted()) {
					encVarId = "'" + pVarId + "'";
				}

				putStringValueIn(jObj, KEY_S_VARID, encVarId);
				putStringValueIn(jObj, KEY_S_CONNAME, pClause.getTargetConcept().getConceptName());
				putBooleanValueIn(jObj, KEY_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
				putStringValueIn(jObj, KEY_S_PREM_OR_CONC, pClause.formattedClauseType());
//				putDoubleValueIn(jObj, KEY_D_XPOS, pQueryOrRule.getXPosFor(pVarId));
//				putDoubleValueIn(jObj, KEY_D_YPOS, pQueryOrRule.getYPosFor(pVarId));
			}
		}
		
		return jObj;
	}

	private CeStoreJsonArray processAttributes(CeQuery pQueryOrRule) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		TreeMap<String, CeStoreJsonObject> allAttributes = new TreeMap<String, CeStoreJsonObject>();
		int ctr = 1;  //A counter for incrementing variable names if needed
		
		//Attributes (datatype properties)
		for (CeClause outerClause : pQueryOrRule.getAllDirectClauses()) {
			for (CeClause thisClause : outerClause.getChildClauses()) {
				for (CePropertyInstance thisPi : thisClause.getDatatypeProperties()) {
					String varId = "";
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
	
	private void processAttributeSpecialOperatorWithQuotes(CeStoreJsonObject pJsonObj, CeQuery pQueryOrRule, CeClause pClause, CePropertyInstance pPi, String pVarId) {
		putStringValueIn(pJsonObj, KEY_S_VARID, pVarId);
		putStringValueIn(pJsonObj, KEY_S_SRCVAR, pPi.getClauseVariableId());
		putStringValueIn(pJsonObj, KEY_S_PROPNAME, pPi.getPropertyName());
		putStringValueIn(pJsonObj, KEY_S_PROPFORMAT, pPi.getRelatedProperty().formattedCeStyle());
		putStringValueIn(pJsonObj, KEY_S_PREM_OR_CONC, pClause.formattedClauseType());
		putStringValueIn(pJsonObj, KEY_S_OPERATOR, CeSpecialProperty.SPECIALNAME_EQUALS);
		putStringValueIn(pJsonObj, KEY_S_VALUE, pPi.getSingleOrFirstValue());
		putBooleanValueIn(pJsonObj, KEY_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
		putBooleanValueIn(pJsonObj, KEY_B_NEGATED_DOM, pClause.isTargetConceptNegated());
		putBooleanValueIn(pJsonObj, KEY_B_NEGATED_RNG, pPi.isNegated(this.ac));
//		putDoubleValueIn(pJsonObj, KEY_D_XPOS, pQueryOrRule.getXPosFor(pVarId));
//		putDoubleValueIn(pJsonObj, KEY_D_YPOS, pQueryOrRule.getYPosFor(pVarId));
	}
	
	private static void processAttributeSpecialOperatorNoQuotes(CeStoreJsonObject pJsonObj, CePropertyInstance pPi) {
		putStringValueIn(pJsonObj, KEY_S_OPERATOR, pPi.getPropertyName());
		putStringValueIn(pJsonObj, KEY_S_VALUE, pPi.getSingleOrFirstValue());
	}

	private void processAttributeNormal(CeStoreJsonObject pJsonObj, CeQuery pQueryOrRule, CeClause pClause, CePropertyInstance pPi, String pVarId) {		
		String encVarId = pVarId;
		if (pPi.hadQuotesOriginally(this.ac)) {
			encVarId = "'" + pVarId + "'";
		}

		putStringValueIn(pJsonObj, KEY_S_VARID, encVarId);
		putStringValueIn(pJsonObj, KEY_S_SRCVAR, pPi.getClauseVariableId());
		putStringValueIn(pJsonObj, KEY_S_PROPNAME, pPi.getPropertyName());
		putStringValueIn(pJsonObj, KEY_S_PROPFORMAT, pPi.getRelatedProperty().formattedCeStyle());
		putStringValueIn(pJsonObj, KEY_S_PREM_OR_CONC, pClause.formattedClauseType());
		putBooleanValueIn(pJsonObj, KEY_B_INCLUDED, pQueryOrRule.isIncludedVarId(pVarId));
		putBooleanValueIn(pJsonObj, KEY_B_NEGATED_DOM, pClause.isTargetConceptNegated());
		putBooleanValueIn(pJsonObj, KEY_B_NEGATED_RNG, pPi.isNegated(this.ac));
//		putDoubleValueIn(pJsonObj, KEY_D_XPOS, pQueryOrRule.getXPosFor(pVarId));
//		putDoubleValueIn(pJsonObj, KEY_D_YPOS, pQueryOrRule.getYPosFor(pVarId));
	}
	
	//Relationship fragment response structure:
	//	[]
	//	KEY_PROPNAME
	//	KEY_PROPFORMAT
	//	KEY_PREM_OR_CONC
	//	KEY_SRCVAR
	//	KEY_TGTVAR
	private CeStoreJsonArray processRelationships(CeQuery pQueryOrRule) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		for (CeClause outerClause : pQueryOrRule.getAllDirectClauses()) {
			for (CeClause thisClause : outerClause.getChildClauses()) {
				for (CePropertyInstance thisPi : thisClause.getObjectProperties()) {
					CeStoreJsonObject jObj = new CeStoreJsonObject();
					
					String encSrcVarId = thisPi.getClauseVariableId();
					if (thisClause.targetVariableWasQuoted()) {
						encSrcVarId = "'" + thisPi.getClauseVariableId() + "'";
					}
					
					String encTgtVarId = thisPi.getSingleOrFirstValue();
					if (thisPi.hadQuotesOriginally(this.ac)) {
						encTgtVarId = "'" + thisPi.getSingleOrFirstValue() + "'";
					}

					putStringValueIn(jObj, KEY_S_SRCVAR, encSrcVarId);
					putStringValueIn(jObj, KEY_S_TGTVAR, encTgtVarId);
					putStringValueIn(jObj, KEY_S_PROPNAME, thisPi.getPropertyName());
					putStringValueIn(jObj, KEY_S_PROPFORMAT, thisPi.getRelatedProperty().formattedCeStyle());
					putStringValueIn(jObj, KEY_S_PREM_OR_CONC, thisClause.formattedClauseType());
					putBooleanValueIn(jObj, KEY_B_NEGATED_DOM, thisClause.isTargetConceptNegated());
					putBooleanValueIn(jObj, KEY_B_NEGATED_RNG, thisPi.isNegated(this.ac));
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

	private static CeStoreJsonArray addAllValuesTo(CeStoreJsonArray pJsonArray, TreeMap<String, CeStoreJsonObject> pValueTree) {
		//Now add all the attributes to the returned JSON array
		for (CeStoreJsonObject thisJatt : pValueTree.values()) {
			addObjectValueTo(pJsonArray, thisJatt);
		}
		
		return pJsonArray;
	}

}