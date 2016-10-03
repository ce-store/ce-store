package com.ibm.ets.ita.ce.store.query;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.RANGE_VALUE;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_CONTAINS;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_ENDSWITH;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_EQUALS;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_GREATER;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_GREATEROREQUAL;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_LESS;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_LESSOREQUAL;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_MATCHES;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_NOTCONTAINS;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_NOTEQUALS;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_NOTSTARTSWITH;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_STARTSWITH;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.MiscNames.HDR_CE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.HDR_COUNT;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NO_TS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AND;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_PERCENT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SPACE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_ZERO;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportExecutionTiming;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSpecialProperty;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;

public abstract class QueryExecutionManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = QueryExecutionManager.class.getName();

	public static final int FLAG_RESP_IDS = 1;
	public static final int FLAG_RESP_INSTS = 2;

	protected ActionContext ac = null;
	private CeQuery targetQuery = null;
	protected long startTs = 0;
	protected long endTs = 0;
	private ArrayList<String> hdrList = new ArrayList<String>();
	private ArrayList<String> ceTemplateList = new ArrayList<String>();
	private boolean generateRationale = false;
	private boolean doubleRationale = false;
	private boolean suppressCeColumn = false;

	protected abstract ContainerCeResult performQueryExecution(CeQuery pQuery);
	protected abstract void resetQueryExecutionManager();

	protected QueryExecutionManager(ActionContext pAc) {
		this.ac = pAc;
	}

	public static QueryExecutionManager createUsing(ActionContext pAc, boolean pSuppressColumn, String pStartTs, String pEndTs) {
		QueryExecutionManager result = null;
		result = new QueryExecutionManagerMem(pAc);

		//Process the timestamps
		result.startTs = extractTimestampFrom(pAc, pStartTs);
		result.endTs = extractTimestampFrom(pAc, pEndTs);
		result.suppressCeColumn = pSuppressColumn;

		return result;
	}

	protected static boolean clauseTestOnDirectMatch(ActionContext pAc, CePropertyInstance pPi) {
		//A direct match is either explicitly expressed with '=', or via the shorthand syntax of 'has x as blah...' 
		return pPi.getPropertyName().equals(SPECIALNAME_EQUALS) || clauseTestOnDirectMatchWithoutSpecialOperator(pAc, pPi);
	}

	private static boolean clauseTestOnDirectMatchWithoutSpecialOperator(ActionContext pAc, CePropertyInstance pPi) {
		//A direct match is either explicitly expressed with '=', or via the shorthand syntax of 'has x as blah...'
		//So the test here is also whether the original value had quotes (which indicates the shorthand)
		return (!CeSpecialProperty.isSpecialValueOperator(pPi.getPropertyName())) && pPi.hadQuotesOriginally(pAc);
	}

	protected static boolean clauseTestOnPartialMatchContains(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_CONTAINS);
	}

	protected static boolean clauseTestOnPartialMatchNotContains(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_NOTCONTAINS);
	}

	protected static boolean clauseTestOnPartialMatchMatches(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_MATCHES);
	}

	protected static boolean clauseTestOnPartialMatchStartsWith(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_STARTSWITH);
	}

	protected static boolean clauseTestOnPartialMatchNotStartsWith(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_NOTSTARTSWITH);
	}

	protected static boolean clauseTestOnPartialMatchEndsWith(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_ENDSWITH);
	}

	protected static boolean clauseTestOnNegativeMatch(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_NOTEQUALS);
	}

	protected static boolean clauseTestOnGreaterThanMatch(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_GREATER);
	}

	protected static boolean clauseTestOnGreaterThanOrEqualsMatch(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_GREATEROREQUAL);
	}

	protected static boolean clauseTestOnLessThanMatch(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_LESS);
	}

	protected static boolean clauseTestOnLessThanOrEqualsMatch(CePropertyInstance pPi) {
		return pPi.getPropertyName().equals(SPECIALNAME_LESSOREQUAL);
	}

	protected static boolean isDatatypeVariable(CePropertyInstance pPi) {
		boolean result = false;
		
		if (pPi != null) {
			result = !pPi.isSpecialOperatorPropertyInstance();
		}
		
		return result;
	}
	
	protected static String encodeVariable(String pVariable) {
		return TOKEN_PERCENT + pVariable + TOKEN_PERCENT;
	}

	private static long extractTimestampFrom(ActionContext pAc, String pTsText) {
		long ts = NO_TS;
		
		if ((pTsText != null) && (!pTsText.isEmpty())) {
			try {
				ts = new Long(pTsText).longValue();
			} catch (Exception e) {
				ts = NO_TS;
				reportError("Timestamp could not be processed (must be a long numeric value, was '" + pTsText + "')", pAc);
			}
		} else {
			ts = NO_TS;
		}
		
		return ts;
	}

	protected ModelBuilder getModelBuilder() {
		return this.ac.getModelBuilder();
	}
	
	protected void addHeader(String pHdr) {
		if (!this.hdrList.contains(pHdr)) {
			this.hdrList.add(pHdr);
		}
	}

	protected void resetCommonVariables() {
		this.hdrList = new ArrayList<String>();
		this.ceTemplateList = new ArrayList<String>();
	}
	
	public ContainerCeResult executeQuery(CeQuery pQuery, int pRespType) {
		ContainerCeResult result = doStandardExecutionProcessing(pQuery);
				
		//Retrieve the returned instances (if required)
		if (pRespType == FLAG_RESP_INSTS) {
			result.populateInstances(this.ac);
		}

		result.trimToLimit(pQuery);

		return result;
	}
	
	public ContainerCeResult executeRule(CeRule pRule, boolean pGenRationale, boolean pDoubleRationale) {
		final String METHOD_NAME = "executeRule";
		
		long sTime = System.currentTimeMillis();
		this.generateRationale = pGenRationale;
		this.doubleRationale = pDoubleRationale;
		this.suppressCeColumn = false;
		ContainerCeResult result = doStandardExecutionProcessing(pRule);

		//Rules don't return normal resultset values - only the CE column
		result.trimToContainOnlyCe();
		
		reportExecutionTiming(this.ac, sTime, "[-1] executeRule", CLASS_NAME, METHOD_NAME);

		return result;		
	}
	
	private ContainerCeResult doStandardExecutionProcessing(CeQuery pQueryOrRule) {
		final String METHOD_NAME = "doStandardExecutionProcessing";
		
		long sTime = System.currentTimeMillis();

		//Ensure that the query manager is reset (it might be used for multiple queries/rules)
		resetQueryExecutionManager();
		this.targetQuery = pQueryOrRule;

		//Execute this query or rule (in both cases they are executed as a query)
		ContainerCeResult result = executeQueryForIds(pQueryOrRule);

		//This may be a rule being execute in query mode, in which case the correct
		//Rule CE needs to be generated
		if (pQueryOrRule.isRule()) {
			CeGeneratorConclusion cgc = new CeGeneratorConclusion(this.ac, (CeRule)pQueryOrRule, result, this.generateRationale, this.doubleRationale);

			cgc.doRuleCeProcessing();
		}

		reportExecutionTiming(this.ac, sTime, "[0] doStandardExecutionProcessing", CLASS_NAME, METHOD_NAME);

		return result;
	}

	private ContainerCeResult executeQueryForIds(CeQuery pQuery) {
		final String METHOD_NAME = "executeQueryForIds";

		long sTime = System.currentTimeMillis();
		ContainerCeResult result = null;

		if (!pQuery.listAllChildPremiseClauses().isEmpty()) {
			result = performQueryExecution(pQuery);

			if (!this.suppressCeColumn) {
				if (pQuery.isNormalQuery()) {
					if ((pQuery.isRule()) || !((pQuery.hasCountHeader()) || (pQuery.hasSumHeader()))) {
						//The CE only needs to be added if this is a normal (not count or sum) query
						addCeToResultSet(result, pQuery);
					}
				}
			}
		} else {
			reportError("No clauses located for query named '" + pQuery.getQueryName() + "'", this.ac);
			result = new ContainerCeResult();
			result.setTargetQuery(pQuery);
		}

		//Now set the headers for this result...
		//If this is a query: Only process the header ids that were requested in the query
		//If this is a rule: Always process all header ids
		if (pQuery.isRule()) {
			for (String thisHdr : this.hdrList) {
				if (!pQuery.hasNegatedHeader(this.ac, thisHdr)) {
					result.addHeader(thisHdr, pQuery.getTypeForHeader(thisHdr));
				}
			}
		} else {
			if (!pQuery.isCountQuery()) {
				for (String thisHdr : pQuery.getResponseVariableIds()) {
					result.addHeader(thisHdr, pQuery.getTypeForHeader(thisHdr));
				}
			} else {
				//If the result is empty then there were no matches so put in an empty count row
				if (!result.getHeaders().contains(HDR_COUNT)) {
					result.addHeader(HDR_COUNT, HDR_COUNT);
					ArrayList<String> zeroRow = new ArrayList<String>();
					zeroRow.add(TOKEN_ZERO);
					result.addResultRow(zeroRow);
				}
			}
		}

		reportExecutionTiming(this.ac, sTime, "[0] executeQueryForIds", CLASS_NAME, METHOD_NAME);

		return result;
	}
	
	protected void addToCeTemplateSimple(CeConcept pSrcCon, String pSrcVarId) {
		String prefix = cePrefix();
		String postfix = cePostfix();
		String srcConceptName = pSrcCon.getConceptName();
		
		String thisTemplate = prefix + "there is %CONQUAL% %CONNAME% named '%INSTNAME%'" + postfix;
		
		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%CONQUAL%", pSrcCon.conceptQualifier());
		ceParms.put("%CONNAME%", srcConceptName);
		ceParms.put("%INSTNAME%", encodeVariable(pSrcVarId));
		
		String ceText = substituteCeParameters(thisTemplate, ceParms);
		
		this.ceTemplateList.add(ceText);
	}

	protected void addToCeTemplateSecondary(CeConcept pSrcCon, String pSrcVarId, CeConcept pTgtCon) {
		String prefix = cePrefix();
		String postfix = cePostfix();
		
		String thisTemplate = prefix + "the %SRCCONNAME% '%INSTNAME%' is %TGTCONQUAL% %TGTCONNAME%" + postfix;
		
		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%SRCCONNAME%", pSrcCon.getConceptName());
		ceParms.put("%INSTNAME%", encodeVariable(pSrcVarId));
		ceParms.put("%TGTCONQUAL%", pTgtCon.conceptQualifier());
		ceParms.put("%TGTCONNAME%", pTgtCon.getConceptName());
		
		String ceText = substituteCeParameters(thisTemplate, ceParms);
		
		this.ceTemplateList.add(ceText);
	}

	protected void addToCeTemplateNormalFn(String pSrcConceptName, String pPropName, String pTgtConceptName, String pSrcVarId, String pTgtVarId, boolean pIsRangeNegated, boolean pIsDomainNegated) {
		String thisTemplate = ES;
		String prefix = cePrefix();
		String postfix = cePostfix();
		
		if (pTgtConceptName.equals(RANGE_VALUE)) {
			if (this.targetQuery.isRule()) {
				if (CeSpecialProperty.isSpecialValueOperator(pPropName)) {
					thisTemplate = prefix + "the %01 '%03' has '%04' as %02" + postfix;
				} else {
					if (pIsRangeNegated) {
						thisTemplate = prefix + "the %01 '%03' has no value as %02" + postfix;
					} else {
						if (pIsDomainNegated) {
							thisTemplate = prefix + "no %01 has '%04' as %02" + postfix;
						} else {
							thisTemplate = prefix + "the %01 '%03' has '%04' as %02" + postfix;
						}
					}
				}
			} else {
				if (pIsRangeNegated) {
					thisTemplate = prefix + "the %01 '%03' has no value as %02" + postfix;
				} else {
					if (pIsDomainNegated) {
						thisTemplate = prefix + "no %01 has '%04' as %02" + postfix;
					} else {
						thisTemplate = prefix + "the %01 '%03' has '%04' as %02" + postfix;
					}
				}
			}
		} else {
			if (pIsRangeNegated) {
				thisTemplate = prefix + "the %01 '%03' has no %05 as %02" + postfix;
			} else {
				if (pIsDomainNegated) {
					thisTemplate = prefix + "no %01 has the %05 '%04' as %02" + postfix;
				} else {
					thisTemplate = prefix + "the %01 '%03' has the %05 '%04' as %02" + postfix;
				}
			}
		}
		
		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%01", pSrcConceptName);
		ceParms.put("%02", pPropName);
		ceParms.put("%03", pSrcVarId);
		ceParms.put("%04", pTgtVarId);
		ceParms.put("%05", pTgtConceptName);

		this.ceTemplateList.add(substituteCeParameters(thisTemplate, ceParms));
	}

	protected void addToCeTemplateNormalVs(String pSrcConceptName, String pPropName, String pTgtConceptName, String pSrcVarId, String pTgtVarId, boolean pIsRangeNegated, boolean pIsDomainNegated) {
		String thisTemplate = ES;
		String encSrcVar = ES;
		String encTgtVar = ES;
		
		String prefix = cePrefix();
		String postfix = cePostfix();

		if (pTgtConceptName.equals(RANGE_VALUE)) {
			if (this.targetQuery.isRule()) {
				if (CeSpecialProperty.isSpecialValueOperator(pPropName)) {
					thisTemplate = prefix + "the %01 '%03' %02 '%04'" + postfix;
				} else {
					if (pIsRangeNegated) {
						thisTemplate = prefix + "the %01 '%03' %02 no value" + postfix;
					} else {
						if (pIsDomainNegated) {
							thisTemplate = prefix + "no %01 %02 '%04'" + postfix;
						} else {
							thisTemplate = prefix + "the %01 '%03' %02 '%04'" + postfix;
						}
					}
				}
			} else {
				if (pIsRangeNegated) {
					thisTemplate = prefix + "the %01 '%03' %02 no value" + postfix;
				} else {
					if (pIsDomainNegated) {
						thisTemplate = prefix + "no %01 %02 '%04'" + postfix;
					} else {
						thisTemplate = prefix + "the %01 '%03' %02 '%04'" + postfix;
					}
				}
			}
		} else {
			if (pIsRangeNegated) {
				thisTemplate = prefix + "the %01 '%03' %02 no %05" + postfix;
			} else {
				if (pIsDomainNegated) {
					thisTemplate = prefix + "no %01 %02 the %05 '%04'" + postfix;
				} else {
					thisTemplate = prefix + "the %01 '%03' %02 the %05 '%04'" + postfix;
				}
			}
		}
		
		encTgtVar = pTgtVarId;		
		encSrcVar = pSrcVarId;

		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%01", pSrcConceptName);
		ceParms.put("%02", pPropName);
		ceParms.put("%03", encSrcVar);
		ceParms.put("%04", encTgtVar);
		ceParms.put("%05", pTgtConceptName);

		this.ceTemplateList.add(substituteCeParameters(thisTemplate, ceParms));
	}
	
	private String cePrefix() {
		String result = null;
		
		if (this.targetQuery.isRule()) {
			result = TOKEN_SPACE + TOKEN_SPACE;
		} else {
			result = ES;
		}
		
		return result;
	}
	
	private String cePostfix() {
		String result = null;
		
		if (this.targetQuery.isRule()) {
			result = ES;
		} else {
			result = TOKEN_DOT;
		}
		
		return result;
	}

	private String ceConcatenator() {
		String result = ES;
		
		if (this.targetQuery.isRule()) {
			result = TOKEN_SPACE + TOKEN_AND + NL;
		} else {
			result = NL;
		}
		
		return result;
	}

	private void addCeToResultSet(ContainerCeResult pResult, CeQuery pQuery) {
		pResult.addHeader(HDR_CE, HDR_CE);

		for (int i = 0; i < pResult.getResultRows().size(); i++) {
			ArrayList<String> thisRow = pResult.getResultRows().get(i);
			ArrayList<String> allRow = pResult.getAllRows().get(i);
			TreeMap<String, String> resRow = new TreeMap<String, String>();
			int posCtr = 0;

			//Iterate through the corresponding 'all' row, rather than just the res row (which contains only the filtered results)
			//This is because the CE generation requires all the values
			for (String thisVal : allRow) {
				//Get the header from the list of query variable ids, and only add it to the result if it is required in the result
				String thisVarName = encodeVariable(pQuery.getAllVariableIds().get(posCtr++));
				resRow.put(thisVarName, thisVal);
			}
			String ceText = generateCeTextFor(resRow);
			thisRow.add(ceText);
		}
	}

	private String generateCeTextFor(TreeMap<String, String> pRows) {
		String result = ES;
		String sep = ES;
		String concat = ceConcatenator();
		
		if (this.ceTemplateList.size() == 1) {
			result = this.ceTemplateList.get(0);
		} else {
			for (String thisTplFrag : this.ceTemplateList) {
				result += sep + thisTplFrag;
				sep = concat;
			}
		}
		
		result = substituteCeParameters(result, pRows);

		return result;
	}

}
