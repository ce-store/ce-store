package com.ibm.ets.ita.ce.store.query;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.RANGE_VALUE;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportExecutionTiming;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeClause;
import com.ibm.ets.ita.ce.store.model.CeConcatenatedValue;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;

public class QueryExecutionManagerMem extends QueryExecutionManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = QueryExecutionManagerMem.class.getName();

	private static final String MARKER_SPECIALOP_START = "(";

	private static final String SPECIALVAL_EQUALS = "(=)";
	private static final String SPECIALVAL_CONTAINS = "(contains)";
	private static final String SPECIALVAL_NOTCONTAINS = "(not-contains)";
	private static final String SPECIALVAL_MATCHES = "(matches)";
	private static final String SPECIALVAL_STARTS = "(starts-with)";
	private static final String SPECIALVAL_NOTSTARTS = "(not-starts-with)";
	private static final String SPECIALVAL_ENDS = "(ends-with)";
	private static final String SPECIALVAL_NEG = "(!=)";
	private static final String SPECIALVAL_GT = "(>)";
	private static final String SPECIALVAL_GTE = "(>=)";
	private static final String SPECIALVAL_LT = "(<)";
	private static final String SPECIALVAL_LTE = "(<=)";

	private TreeMap<String, MatchedClauseList> mcls = new TreeMap<String, MatchedClauseList>();
	
	protected QueryExecutionManagerMem(ActionContext pAc) {
		super(pAc);
	}
	
	private static String calculateMclId(String pSrcVar, String pTgtVar, String pPropName) {
		String newId = "";
		
		if ((pPropName == null) && (pTgtVar == null)) {
			//This is a simple property (not a relationship) so just use the source variable id
			newId = pSrcVar;
		} else {
			//This is a normal relationship property so concatenate the three parts together
			newId = pSrcVar + ":" + pPropName + ":" + pTgtVar;
		}
		
		return newId;
	}
	
	private TreeMap<String, MatchedClauseList> getMcls() {
		return this.mcls;
	}
	
	private void addToMcls(String pKey, MatchedClauseList pMcl) {
		this.mcls.put(pKey, pMcl);
	}
	
	@Override
	protected void resetQueryExecutionManager() {
		resetCommonVariables();
		this.mcls = new TreeMap<String, MatchedClauseList>();
	}

	@Override
	protected ContainerCeResult performQueryExecution(CeQuery pQuery) {
		final String METHOD_NAME = "performQueryExecution";
		
		long sTime = System.currentTimeMillis();

		ContainerCeResult result = null;
		boolean isEmpty = doClauseProcessing(pQuery);

		if (!isEmpty) {
			QueryResultProcessorMem qrp = new QueryResultProcessorMem(this.ac, this.mcls, pQuery);
			result = qrp.generateResultFromMcls();
		}

		//Ensure that a null result is not returned
		if (result == null) {
			result = new ContainerCeResult();
			result.setTargetQuery(pQuery);
			result.setQuery(pQuery.getCeText());
		}
		
		reportExecutionTiming(this.ac, sTime, "[1] performQueryExecution", CLASS_NAME, METHOD_NAME);
//System.out.println("QueryExecution for " + pQuery.getQueryName() + ": " + (System.currentTimeMillis() - sTime));
		return result;
	}

	private boolean doClauseProcessing(CeQuery pQuery) {
		final String METHOD_NAME = "doClauseProcessing";

		long sTime = System.currentTimeMillis();
		boolean isEmpty = false;

		//Process all the clauses
		isEmpty = processAllClauses(pQuery);

		//An empty result for a clause means that no further processing is needed
		if (!isEmpty) {
			//This must be done separately after all of the normal clauses since the lists that need to be checked are lists of values, not
			//lists of instances and therefore cannot be simply requested from the model builder
			isEmpty = processAllDatatypeClauses(pQuery);
			if (!isEmpty) {
				isEmpty = processAllSpecialObjectClauses(pQuery);
			}
		}

		reportExecutionTiming(this.ac, sTime, "[2] doClauseProcessing", CLASS_NAME, METHOD_NAME);

		return isEmpty;
	}

	private boolean processAllClauses(CeQuery pQuery) {
		final String METHOD_NAME = "processAllClauses";

		long sTime = System.currentTimeMillis();

		boolean isEmpty = false;
		TreeMap<Integer, ArrayList<CeClause>> sortedClauses = new TreeMap<Integer, ArrayList<CeClause>>();
		ArrayList<CeClause> valueClauses = new ArrayList<CeClause>();

		//First sort the clauses based on the number of instances
		for (CeClause thisClause : pQuery.listAllChildPremiseClauses()) {
			CeConcept domainConcept = thisClause.getTargetConcept();

			if (domainConcept != null) {
				Integer icInt = new Integer(getModelBuilder().countAllInstancesForConcept(domainConcept));

				ArrayList<CeClause> thisClauseList = sortedClauses.get(icInt);
				if (thisClauseList == null) {
					thisClauseList = new ArrayList<CeClause>();
					sortedClauses.put(icInt, thisClauseList);
				}
				thisClauseList.add(thisClause);
			} else {
				valueClauses.add(thisClause);
			}
		}

		//Now iterate through the non-value clauses from the lowest count to the highest
		for (Integer thisKey : sortedClauses.keySet()) {
			if (!isEmpty) {
				ArrayList<CeClause> thisClauseList = sortedClauses.get(thisKey);

				for (CeClause thisClause : thisClauseList) {
					//Added by DSB 09/09/2012 - If any clause is empty then there is no need to continue as the result must be empty
					if (!isEmpty) {
						CeConcept domainConcept = thisClause.getTargetConcept();
						
						ArrayList<CeInstance> latestInsts = getLatestInstsForVariable(thisClause.getTargetVariable(), domainConcept, thisClause.targetVariableWasQuoted());					
						
						isEmpty = processThisClause(thisClause, latestInsts, thisClause.getTargetVariable());
					}
				}
			}
		}

		//Now do the value clauses
		if (!isEmpty) {
			for (CeClause thisClause : valueClauses) {
				isEmpty = processThisClause(thisClause, null, thisClause.getTargetVariable());
			}
		}

		reportExecutionTiming(this.ac, sTime, "[3] processAllClauses", CLASS_NAME, METHOD_NAME);

		return isEmpty;
	}
	
	private boolean processAllSpecialObjectClauses(CeQuery pQuery) {
		final String METHOD_NAME = "processAllSpecialObjectClauses";

		long sTime = System.currentTimeMillis();

		boolean isEmpty = false;
		TreeMap<Integer, ArrayList<CeClause>> sortedClauses = new TreeMap<Integer, ArrayList<CeClause>>();

		//First sort the clauses based on the number of instances
		for (CeClause thisClause : pQuery.listAllChildPremiseClauses()) {
			CeConcept domainConcept = thisClause.getTargetConcept();
			
			Integer icInt = new Integer(getModelBuilder().countAllInstancesForConcept(domainConcept));

			ArrayList<CeClause> thisClauseList = sortedClauses.get(icInt);
			if (thisClauseList == null) {
				thisClauseList = new ArrayList<CeClause>();
				sortedClauses.put(icInt, thisClauseList);
			}
			thisClauseList.add(thisClause);			
		}

		//Finally process the special properties
		for (Integer thisKey : sortedClauses.keySet()) {
			if (!isEmpty) {
				ArrayList<CeClause> thisClauseList = sortedClauses.get(thisKey);
				
				for (CeClause thisClause : thisClauseList) {
					//Added by DSB 09/09/2012 - If any clause is empty then there is no need to continue as the result must be empty
					if (!isEmpty) {
						CeConcept domainConcept = thisClause.getTargetConcept();
						
						ArrayList<CeInstance> latestInsts = getLatestInstsForVariable(thisClause.getTargetVariable(), domainConcept, thisClause.targetVariableWasQuoted());					
						
						isEmpty = processSpecialPropertiesForThisClause(thisClause, latestInsts, thisClause.getTargetVariable(), pQuery);
					}
				}
			}
		}

		reportExecutionTiming(this.ac, sTime, "[3] processAllSpecialObjectClauses", CLASS_NAME, METHOD_NAME);

		return isEmpty;
	}

	private ArrayList<CeInstance> getLatestInstsForVariable(String pVariable, CeConcept pTargetConcept, boolean pIsQuotedVal) {
		ArrayList<CeInstance> result = null;
		ArrayList<CeInstance> allInsts = null;
		int allSize = 0;

		if (pIsQuotedVal) {
			allInsts = new ArrayList<CeInstance>();
			CeInstance thisInst = getModelBuilder().getInstanceNamed(this.ac, pVariable);
			
			if (thisInst != null) {
				if (thisInst.isConcept(pTargetConcept)) {
					allInsts.add(thisInst);
				}
			}
		} else {
			allInsts = getModelBuilder().retrieveAllInstancesForConcept(pTargetConcept);
		}
		allSize = allInsts.size();
		
		for (MatchedClauseList thisMcl : this.mcls.values()) {
			if (thisMcl.getSrcVarId().equals(pVariable)) {
				int thisSize = thisMcl.getMatchedPairs().values().size();
				if (thisSize < allSize) {
					if (result == null) {
						result = new ArrayList<CeInstance>();
					}
					if (isReportMicroDebug()) {
						reportMicroDebug("Found (source) for " + pVariable + ": " + thisSize + " vs (all): " + allSize, this.ac);
					}
					updateInstListWith(result, thisMcl.getSourceInstances(this.ac));
				}
			} else if (thisMcl.getSrcVarId().equals(pVariable)) {
				int thisSize = thisMcl.getMatchedPairs().size();
				if (thisSize < allSize) {
					if (result == null) {
						result = new ArrayList<CeInstance>();
					}
					if (isReportMicroDebug()) {
						reportMicroDebug("Found (target) for " + pVariable + ": " + thisSize + " vs (all): " + allSize, this.ac);
					}
					updateInstListWith(result, thisMcl.getTargetInstances(this.ac));
				}
			}
		}

		//If nothing has been set then use all of the instances
		if (result == null) {
			result = allInsts;
		}
		
		return result;
	}
	
	private static void updateInstListWith(ArrayList<CeInstance> pMainList, ArrayList<CeInstance> pNewList) {
		ArrayList<CeInstance> keepList = new ArrayList<CeInstance>();
		
		if (pMainList.isEmpty()) {
			pMainList.addAll(pNewList);				
		} else {
			for (CeInstance thisInst : pMainList) {
				if (pNewList.contains(thisInst)) {
					keepList.add(thisInst);
				}
			}
			pMainList.clear();
			pMainList.addAll(keepList);
		}
	}
	
	private CeInstance getInstForVariable(String pVariable) {
		CeInstance result = null;
		
		result = getModelBuilder().getInstanceNamed(this.ac, pVariable);

		return result;
	}
	
	private boolean processAllDatatypeClauses(CeQuery pQuery) {
		final String METHOD_NAME = "processAllDatatypeClauses";

		long sTime = System.currentTimeMillis();
		boolean isEmpty = false;
		
		for (CeClause thisClause : pQuery.listAllChildPremiseClauses()) {
			ArrayList<CePropertyInstance> dataProps = thisClause.getDatatypeProperties();

			for (CePropertyInstance datPi : dataProps) {
				if (datPi.isSpecialOperatorPropertyInstance()) {
					if (!isEmpty) {
						ArrayList<String> latestInsts = getAllMatchingValuesForVariable(datPi.getClauseVariableId(), pQuery);
						isEmpty = secondaryProcessForDatatypeProperty(latestInsts, thisClause.getTargetVariable(), datPi, pQuery);
					}
				}
			}
		}
		
		reportExecutionTiming(this.ac, sTime, "[3] processAllDatatypeClauses", CLASS_NAME, METHOD_NAME);

		return isEmpty;
	}

	private ArrayList<String> getAllMatchingValuesForVariable(String pVarId, CeQuery pQuery) {
		ArrayList<String> result = new ArrayList<String>();
		
		if (!getMcls().isEmpty()) {
			boolean foundVar = false;
			
			for (String mclKey : getMcls().keySet()) {
				MatchedClauseList thisMcl = getMcls().get(mclKey);	
				
				if (thisMcl.getSrcVarId().equals(pVarId)) {
					for (ArrayList<String> matchedPair : thisMcl.getMatchedPairs().values()) {
						foundVar = true;
						result.add(matchedPair.get(0));
					}
				}
	
				if (thisMcl.getTgtVarId().equals(pVarId)) {
					for (ArrayList<String> matchedPair : thisMcl.getMatchedPairs().values()) {
						foundVar = true;
						result.add(matchedPair.get(1));
					}
				}
			}
			
			if (!foundVar) {
				reportMicroDebug("No variable named '" + pVarId + "' was located in the query... should it be a constant value instead?  (If so enclose it in single quotes), for query: " + pQuery.getCeText(), this.ac);
			}
		}
		
		return result;
	}
	
	private boolean processThisClause(CeClause pClause, ArrayList<CeInstance> pInsts, String pVarId) {
		int startingMclCount = getMcls().size();
		boolean wasFilterProp = false;
		boolean hadProp = false;
		boolean result = false;
		ArrayList<CePropertyInstance> objProps = pClause.getObjectProperties();
		ArrayList<CePropertyInstance> dataProps = pClause.getDatatypeProperties();
		ArrayList<CeConcatenatedValue> concatVals = pClause.getConcatenatedValues();

		addHeader(pVarId);
		
		if (objProps.isEmpty() && dataProps.isEmpty() && concatVals.isEmpty()) {
			result = processForEmptyProperties(pClause, pInsts, pVarId);
		} else {		
			//Normal processing - object and/or datatype properties are not empty
			for (CePropertyInstance thisObjPi : objProps) {
				if (!thisObjPi.isSpecialOperatorPropertyInstance()) {
					processForObjectProperty(pClause, pInsts, pVarId, thisObjPi);
					hadProp = true;
				}
			}
			
			for (CePropertyInstance thisDatPi : dataProps) {
				hadProp = true;
				if (isDatatypeVariable(thisDatPi)) {
					processForObjectProperty(pClause, pInsts, pVarId, thisDatPi);
				} else {
					initialProcessForDatatypeProperty(pClause, pVarId, thisDatPi);
					wasFilterProp = true;
				}
			}
			
			for (CeConcatenatedValue thisConVal : concatVals) {
				processForConcatenatedValue(pVarId, thisConVal);
			}
		}
		
		if (hadProp) {
			if (wasFilterProp) {
				//Must always be false if this was a filter property (as no new mcls are added)
				result = false;
			} else {
				//If the number of mcls has not increased then there were no instances returned for this clause
				result = (getMcls().size() == startingMclCount);
			}
//		} else {
//			//Must always be false if no properties were processed
//			result = false;
		}
		
		return result;
	}
	
	private boolean processSpecialPropertiesForThisClause(CeClause pClause, ArrayList<CeInstance> pInsts, String pVarId, CeQuery pQuery) {
		boolean result = false;
		ArrayList<CePropertyInstance> objProps = pClause.getObjectProperties();

		addHeader(pVarId);

		//Normal processing - object and/or datatype properties are not empty
		for (CePropertyInstance thisObjPi : objProps) {
			if (thisObjPi.isSpecialOperatorPropertyInstance()) {
				result = processForSpecialObjectProperty(pClause, pInsts, pVarId, thisObjPi, pQuery);
			}
		}
		
//		//Result is always false for special properties
//		return false;
		return result;
	}

	private boolean processForEmptyProperties(CeClause pClause, ArrayList<CeInstance> pInsts, String pTargetHeader) {
		boolean result = true;

		if (pInsts != null) {
			//Object and datatype properties are empty, so this is a simple "list all instances" query
			for (CeInstance thisInst : pInsts) {
				if (thisInst.isInTimestampRange(this.startTs, this.endTs)) {
					boolean keepInst = true;
					
					break_position:
					for (CeConcept thisSecCon : pClause.getSecondaryConceptsNormal()) {
						if (!thisInst.isConcept(thisSecCon)) {
							keepInst = false;
							break break_position;
						}
					}
					if (keepInst) {
						break_position:
						for (CeConcept thisSecConNeg : pClause.getSecondaryConceptsNegated()) {
							if (thisInst.isConcept(thisSecConNeg)) {
								keepInst = false;
								break break_position;
							}
						}
					}
					
					if (keepInst) {
						saveSimpleInstance(pTargetHeader, thisInst);
						result = false;
					} else {
						removeSimpleInstance(pTargetHeader, thisInst);
					}
				}
			}
		}

		CeConcept tgtCon = pClause.getTargetConcept();
		
		if (tgtCon != null) {
			if (pClause.getSecondaryConceptsNormal().isEmpty()) {
				//Add this information to the CE template
				addToCeTemplateSimple(tgtCon, pTargetHeader);
			} else {
				for (CeConcept secCon : pClause.getSecondaryConceptsNormal()) {
					//Add this information to the CE template
					addToCeTemplateSecondary(tgtCon, pTargetHeader, secCon);
				}
			}
		} else {
			reportError("Unexpected null target concept for query/rule clause: " + pClause.toString(), this.ac);
		}

		return result;
	}

	private void processForObjectProperty(CeClause pClause, ArrayList<CeInstance> pInsts, String pSrcVarId, CePropertyInstance pObjPi) {
		String propName = pObjPi.getRelatedProperty().getPropertyName();
		String tgtVarId = pObjPi.getSingleOrFirstValue();
		String encTgtVarId = "";
		
		if (pObjPi.hadQuotesOriginally(this.ac)) {
			//This is a filter statement (since there were quotes specified) so try to match on the specified value
			encTgtVarId = tgtVarId;
			doFilterProcessing(pSrcVarId, propName, pInsts, pObjPi);
		} else {
			//This is a normal variable statement
			encTgtVarId = encodeVariable(tgtVarId);
			
			if (pClause.isTargetConceptNegated()) {
				doNegatedTargetProcessing(pSrcVarId, tgtVarId, propName, pObjPi);
			} else {
				doNormalProcessing(pSrcVarId, tgtVarId, propName, pInsts, pObjPi);
			}
		}

		//Add this information to the CE template
		addToCeTemplate(pClause, pSrcVarId, encTgtVarId, propName, pObjPi);
	}
	
	private boolean processForSpecialObjectProperty(CeClause pClause, ArrayList<CeInstance> pInsts, String pSrcVarId, CePropertyInstance pObjPi, CeQuery pQuery) {
		boolean result = false;
		String propName = pObjPi.getRelatedProperty().getPropertyName();
		String tgtVarId = pObjPi.getSingleOrFirstValue();
		String encTgtVarId = "";
		
		if (pObjPi.hadQuotesOriginally(this.ac)) {
			//This is a filter statement (since there were quotes specified) so try to match on the specified value
			encTgtVarId = tgtVarId;
			result = doSpecialFilterProcessing(pSrcVarId, propName, pInsts, pObjPi);
		} else {
			//This is a normal variable statement
			encTgtVarId = encodeVariable(tgtVarId);
			doSpecialPropertyProcessing(pSrcVarId, tgtVarId, propName, pInsts, pObjPi, pQuery);
		}

		//Add this information to the CE template
		addToCeTemplate(pClause, pSrcVarId, encTgtVarId, propName, pObjPi);
		
		return result;
	}

	private void processForConcatenatedValue(String pVarId, CeConcatenatedValue pConVal) {
		for (String concatVar : pConVal.getAllVarNames()) {
			for (String tgtVal : getLatestValuesForVariable(concatVar)) {
				String concatVal = pConVal.doConcatenationWith(concatVar, tgtVal);

				saveThisMatchedConcatValue(pVarId, concatVar, concatVal, tgtVal);
				if (isReportMicroDebug()) {
					reportMicroDebug("Successfully concatenated value '" + concatVar + "'->'" + pConVal.getRawConcatText() + "' (" + concatVal + " -> " + tgtVal + ")", this.ac);
				}
			}
		}
	}
	
	private HashSet<String> getLatestValuesForVariable(String pVarName) {
		HashSet<String> result = new HashSet<String>();
		
		for (MatchedClauseList thisMcl : this.mcls.values()) {
			if (pVarName.equals(thisMcl.getSrcVarId())) {
				result.addAll(thisMcl.computeSourceValues());
			}
			if (pVarName.equals(thisMcl.getTgtVarId())) {
				//Target variable matches
				result.addAll(thisMcl.computeTargetValues());
			}
		}
		
		return result;
	}

	private boolean doFilterProcessing(String pSrcVarId, String pPropName, ArrayList<CeInstance> pInsts, CePropertyInstance pObjPi) {
		boolean result = true;
		//This is a filter statement (since there were quotes specified) so try to match on the specified value
		String targetName = retrieveTargetValueFromFilterProperty(pObjPi);
		
		for (CeInstance thisInst : pInsts) {
			CePropertyInstance foundPi = thisInst.getPropertyInstanceNamed(pPropName);
			
			if (foundPi != null) {
				for (String thisInstIdOrValue : retrieveValuesFor(foundPi)) {
					CeInstance matchedInst = getInstForVariable(thisInstIdOrValue);
					if (matchedInst.getInstanceName().equals(targetName)) {
						saveThisMatchedInstance(pSrcVarId, thisInst, pObjPi.getSingleOrFirstValue(), matchedInst, pPropName);
						result = false;	//A match has been made, so this is not an empty result
					}
				}
			}				
		}

		return result;
	}
	
	private boolean doSpecialFilterProcessing(String pSrcVarId, String pPropName, ArrayList<CeInstance> pInsts, CePropertyInstance pObjPi) {
		//This is currently the same as normal filter processing
		return doFilterProcessing(pSrcVarId, pPropName, pInsts, pObjPi);
	}

	private void doNormalProcessing(String pSrcVarId, String pTgtVarId, String pPropName, ArrayList<CeInstance> pInsts, CePropertyInstance pObjPi) {
		addHeader(pTgtVarId);
		
		String rangeConceptId = pObjPi.getSingleOrFirstRangeName();
		CeConcept targetRange = getModelBuilder().getConceptNamed(this.ac, rangeConceptId);
		
		for (CeInstance thisInst : pInsts) {
			//DB 12/09/2012 - Added special operator handling
			if (!CeProperty.isSpecialPropertyName(pPropName, rangeConceptId)) {
				//Normal processing
				CePropertyInstance foundPi = thisInst.getPropertyInstanceNamed(pPropName);

				if (foundPi != null) {
					processFoundPi(foundPi, targetRange, pSrcVarId, pTgtVarId, pPropName, thisInst, pObjPi);
				} else {
					//No property instance is found
					if (pObjPi.isNegated(this.ac)) {
						//This is a negated match - so only save the cases where there is no value
						if (pObjPi.getRelatedProperty().isObjectProperty()) {
							saveThisMatchedInstance(pSrcVarId, thisInst, pObjPi.getSingleOrFirstValue(), null, pPropName);
						} else {
							saveThisMatchedNormalValue(pSrcVarId, thisInst, pObjPi.getSingleOrFirstValue(), "", pPropName);
						}
					}
				}
			} else {
				//Special operator processing - do nothing... it is done in "doSpecialPropertyProcessing()"
			}
		}	
	}

	private void doNegatedTargetProcessing(String pSrcVarId, String pTgtVarId, String pPropName, CePropertyInstance pObjPi) {
		addHeader(pTgtVarId);

		String rangeConceptName = pObjPi.getSingleOrFirstRangeName();
		
		ArrayList<CeInstance> candidates = this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, rangeConceptName);
				
		for (CeInstance thisCandidate : candidates) {
			//DB 12/09/2012 - Added special operator handling
			if (!CeProperty.isSpecialPropertyName(pPropName, rangeConceptName)) {
				
				//Normal negated processing
				boolean matched = false;

				HashSet<CePropertyInstance> refPis = thisCandidate.listAllReferringPropertyInstances(this.ac, pPropName);

				for (CePropertyInstance refPi : refPis) {
					if (refPi.getPropertyName().equals(pObjPi.getPropertyName())) {
						if (pObjPi.getRelatedProperty().getDomainConcept().equals(refPi.getRelatedProperty().getDomainConcept())) {
							for (CeInstance refInst : refPi.getValueInstanceList(this.ac)) {
								if (thisCandidate.equals(refInst)) {
									matched = true;
								}
							}
						}
					}
				}

				if (!matched) {
					//The candidate references via the target property instance.  This means it is a match as the target was negated
					saveThisMatchedInstance(pSrcVarId, null, pObjPi.getSingleOrFirstValue(), thisCandidate, pPropName);
				}
			} else {
				//Special operator processing - do nothing... it is done in "doSpecialPropertyProcessing()"
			}
		}	
	}

	private void doSpecialPropertyProcessing(String pSrcVarId, String pTgtVarId, String pPropName, ArrayList<CeInstance> pInsts, CePropertyInstance pObjPi, CeQuery pQuery) {
		addHeader(pTgtVarId);
		
		String rangeConceptId = pObjPi.getSingleOrFirstRangeName();
		
		for (CeInstance thisInst : pInsts) {
			//DB 12/09/2012 - Added special operator handling
			if (CeProperty.isSpecialPropertyName(pPropName, rangeConceptId)) {
				//Special operator processing
				if (clauseTestOnDirectMatch(this.ac, pObjPi)) {
					processSpecialEquals(pSrcVarId, pTgtVarId, pPropName, thisInst, pObjPi, pQuery);
				} else if (clauseTestOnPartialMatchContains(pObjPi)) {
					reportWarning("(contains) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnPartialMatchNotContains(pObjPi)) {
					reportWarning("(not contains) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnPartialMatchMatches(pObjPi)) {
					reportWarning("(matches) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnPartialMatchStartsWith(pObjPi)) {
					reportWarning("(startsWith) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnPartialMatchNotStartsWith(pObjPi)) {
					reportWarning("(not startsWith) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnPartialMatchEndsWith(pObjPi)) {
					reportWarning("(endsWith) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnNegativeMatch(pObjPi)) {
					//Nothing should be done here as this is a double variable != match and will be handled in later processing
				} else if (clauseTestOnGreaterThanMatch(pObjPi)) {
					reportWarning("(>) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnGreaterThanOrEqualsMatch(pObjPi)) {
					reportWarning("(>=) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnLessThanMatch(pObjPi)) {
					reportWarning("(<) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else if (clauseTestOnLessThanOrEqualsMatch(pObjPi)) {
					reportWarning("(<=) Special operator processing not yet implemented, for: " + pObjPi.toString(), this.ac);
				} else {
					reportError("Unable to match on datatype clause due to unknown comparison operator (" + pObjPi.getPropertyName() + ")", this.ac);
				}
			} else {
				//Do nothing - normal processing has already been done
			}
		}	
	}

	private void processSpecialEquals(String pSrcVarId, String pTgtVarId, String pPropName, CeInstance pInst, CePropertyInstance pObjPi, CeQuery pQuery) {
		ArrayList<String> matchedInstNames = getAllMatchingValuesForVariable(pTgtVarId, pQuery);
		
		for (String thisInstName : matchedInstNames) {
			addHeader(pTgtVarId);

			CeInstance matchedInst = getModelBuilder().getInstanceNamed(this.ac, thisInstName);
			
			if (matchedInst == pInst) {
				saveThisMatchedInstance(pSrcVarId, pInst, pObjPi.getSingleOrFirstValue(), matchedInst, pPropName);
			}
		}
	}
	
	private void processFoundPi(CePropertyInstance pFoundPi, CeConcept pTargetRange, String pSrcVarId, String pTgtVarId, String pPropName, CeInstance pInst, CePropertyInstance pObjPi) {
		CeConcept tempRange = getModelBuilder().getConceptNamed(this.ac, pFoundPi.getSingleOrFirstRangeName());
		
		//If both range concepts are null then this is a match
		boolean matchedConstantRanges = (tempRange == null) && (pTargetRange == null);
		boolean matchedNormalRanges = false;
		
		if (matchedConstantRanges) {
			//This is a datatype (constant) match
			//Do nothing here (see later)
		} else {
			//This is an object (normal) match
			matchedNormalRanges = testForNormalRangeMatch(pFoundPi, pTargetRange, pSrcVarId, pTgtVarId, pObjPi);
		}
		
		if (matchedConstantRanges || matchedNormalRanges) {
			//We have a match
			if (pObjPi.isNegated(this.ac)) {
				//This is a negated match - so only save the cases where there is no value
				String targetValue = pFoundPi.getSingleOrFirstValue();
				if ((targetValue == null) || (targetValue.isEmpty())) {
					if (matchedNormalRanges) {
						addHeader(pTgtVarId);
						saveThisMatchedInstance(pSrcVarId, pInst, pObjPi.getSingleOrFirstValue(), null, pPropName);
					} else {
						saveThisMatchedNormalValue(pSrcVarId, pInst, pObjPi.getSingleOrFirstValue(), "", pPropName);
					}
				}
			} else {
				//This is a normal match
				for (String thisInstIdOrValue : retrieveValuesFor(pFoundPi)) {
					//Only try to get the matching instance if this is a normal (not constant) match
					if (matchedNormalRanges) {
						addHeader(pTgtVarId);

						CeInstance matchedInst = getModelBuilder().getInstanceNamed(this.ac, thisInstIdOrValue);
						if (doesRangeConceptMatchTarget(matchedInst, pTargetRange)) {
							saveThisMatchedInstance(pSrcVarId, pInst, pObjPi.getSingleOrFirstValue(), matchedInst, pPropName);
						}
					} else {
						saveThisMatchedNormalValue(pSrcVarId, pInst, pObjPi.getSingleOrFirstValue(), thisInstIdOrValue, pPropName);
					}
				}
			}
		}
	}
	
	private boolean testForNormalRangeMatch(CePropertyInstance pFoundPi, CeConcept pTargetRange, String pSrcVarId, String pTgtVarId, CePropertyInstance pObjPi) {
		boolean result = false;
		
		if (pObjPi.isNegated(this.ac)) {
			//A negated property
			String targetValue = pFoundPi.getSingleOrFirstValue();
			if ((targetValue == null) || (targetValue.isEmpty())) {
				result = true;
			}
		} else {
			//A normal property
			for (CeInstance relInst : pFoundPi.getValueInstanceList(this.ac)) {
				if (isPotentialMatch(relInst, pSrcVarId, pTgtVarId)) {
					result = result || doesRangeConceptMatchTarget(relInst, pTargetRange);
				}
			}
		}
		
		return result;
	}
	
	private static boolean doesRangeConceptMatchTarget(CeInstance pInst, CeConcept pTargetRange) {
		boolean result = false;
		
		if (pInst != null) {
			for (CeConcept relCon : pInst.listAllConcepts()) {
				if (!result) {
					result = relCon.equalsOrHasParent(pTargetRange);
				}
			}
		}

		return result;
	}
	
	private void addToCeTemplate(CeClause pClause, String pSrcVarId, String pEncTgtVarId, String pPropName, CePropertyInstance pObjPi) {
		CeProperty relProp = pObjPi.getRelatedProperty();
		boolean isRangeNegated = pObjPi.isNegated(this.ac);
		boolean isDomainNegated = pClause.isTargetConceptNegated();

		if (relProp != null) {
			if (relProp.isFunctionalNoun()) {
				addToCeTemplateNormalFn(pClause.getTargetConcept().getConceptName(), pPropName, pObjPi.getSingleOrFirstRangeName(), encodeVariable(pSrcVarId), pEncTgtVarId, isRangeNegated, isDomainNegated);
			} else {
				addToCeTemplateNormalVs(pClause.getTargetConcept().getConceptName(), pPropName, pObjPi.getSingleOrFirstRangeName(), encodeVariable(pSrcVarId), pEncTgtVarId, isRangeNegated, isDomainNegated);
			}
		} else {
			reportError("CE template cannot be generated for query clause as related property (" + pObjPi.toString() + ") could not be found: " + pClause.toString(), this.ac);
		}
	}

	private HashSet<String> retrieveValuesFor(CePropertyInstance pPi) {
		return pPi.getValueListInTimestampRange(this.startTs, this.endTs);
	}
	
	private static String retrieveTargetValueFromFilterProperty(CePropertyInstance pPi) {
		return pPi.getSingleOrFirstValue();
	}
	
	private boolean isPotentialMatch(CeInstance pInst, String pSrcVar, String pTgtVar) {
		boolean result = true;
		
		for (MatchedClauseList thisMcl : this.mcls.values()) {
			//Do not attempt to check the MCL which is currently being processed
			if (!thisMcl.hasVariables(pSrcVar, pTgtVar)) {
				//Only try the test if we have not already concluded this is NOT a match
				if (result) {
					if (thisMcl.getSrcVarId().equals(pTgtVar)) {
						result = thisMcl.containsSourceId(pInst.getInstanceName());
					} else if (thisMcl.getTgtVarId().equals(pTgtVar)) {
						result = thisMcl.containsTargetId(pInst.getInstanceName());
					}
				}
			}
		}
	
		return result;
	}

	private void initialProcessForDatatypeProperty(CeClause pClause, String pSrcVarId, CePropertyInstance pDatPi) {
		String dHdrName = pDatPi.getSingleOrFirstValue();		
		CeProperty datProp = pDatPi.getRelatedProperty();
		String propName = datProp.getPropertyName();
		String conceptName = "";
		
		if (!pDatPi.isSpecialOperatorPropertyInstance()) {
			addHeader(dHdrName);
		}
		
		if (pClause.getTargetConcept() != null) {
			conceptName = pClause.getTargetConcept().getConceptName();
		} else {
			conceptName = RANGE_VALUE;
		}

		//Add this information to the CE template
		CeProperty relProp = pDatPi.getRelatedProperty();
		boolean isRangeNegated = pDatPi.isNegated(this.ac);
		boolean isDomainNegated = pClause.isTargetConceptNegated();
		
		if (relProp != null) {
			if (relProp.isFunctionalNoun()) {
				if (pDatPi.hadQuotesOriginally(this.ac)) {
					addToCeTemplateNormalFn(conceptName, propName, pDatPi.getSingleOrFirstRangeName(), encodeVariable(pSrcVarId), dHdrName, isRangeNegated, isDomainNegated);
				} else {
					addToCeTemplateNormalFn(conceptName, propName, pDatPi.getSingleOrFirstRangeName(), encodeVariable(pSrcVarId), encodeVariable(pDatPi.getSingleOrFirstValue()), isRangeNegated, isDomainNegated);
				}
			} else {
				if (pDatPi.hadQuotesOriginally(this.ac)) {
					addToCeTemplateNormalVs(conceptName, propName, pDatPi.getSingleOrFirstRangeName(), encodeVariable(pSrcVarId), dHdrName, isRangeNegated, isDomainNegated);
				} else {
					addToCeTemplateNormalVs(conceptName, propName, pDatPi.getSingleOrFirstRangeName(), encodeVariable(pSrcVarId), encodeVariable(pDatPi.getSingleOrFirstValue()), isRangeNegated, isDomainNegated);
				}
			}
		} else {
			reportError("CE template cannot be generated for query clause as related property (" + pDatPi.toString() + ") could not be found: " + pClause.toString(), this.ac);
		}
	}
	
	private boolean secondaryProcessForDatatypeProperty(ArrayList<String> pCandidateVals, String pSrcVarId, CePropertyInstance pDatPi, CeQuery pQuery) {
		ArrayList<String> targetVals = null;
		CeProperty datProp = pDatPi.getRelatedProperty();
		String propName = datProp.getPropertyName();
		
		//DSB 09/09/2012 - Changed to handle comparisons against variables
		if (pDatPi.hadQuotesOriginally(this.ac)) {
			//This was a quoted value so just add the specified value to the target values list
			targetVals = new ArrayList<String>();
			targetVals.add(pDatPi.getSingleOrFirstValue());
		} else {
			//This was not a quote value and is therefore a variable, so all possible matching values must be tested
			targetVals = getAllMatchingValuesForVariable(pDatPi.getSingleOrFirstValue(), pQuery);
		}

		int matches = 0;
		
		for (String cVal : pCandidateVals) {
			if (clauseTestOnDirectMatch(this.ac, pDatPi)) {
				for (String thisTval : targetVals) {
					if (cVal.equals(thisTval)) {
						//We have a match
						String tgtVarId = "";
						if (pDatPi.hadQuotesOriginally(this.ac)) {
							tgtVarId = SPECIALVAL_EQUALS + pSrcVarId;
						} else {
							tgtVarId = pDatPi.getSingleOrFirstValue();
						}
						saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
						matches++;
					}
				}
			} else if (clauseTestOnPartialMatchContains(pDatPi)) {
				for (String thisTval : targetVals) {
					//DSB 01/05/2015 (#1098)
					boolean result = false;
					
					if (this.ac.getCeConfig().isCaseSensitive()) {
						result = cVal.contains(thisTval);
					} else {
						result = cVal.toLowerCase().contains(thisTval.toLowerCase());
					}
					
					if (result) {
						//We have a match
						String tgtVarId = "";
						if (pDatPi.hadQuotesOriginally(this.ac)) {
							tgtVarId = SPECIALVAL_CONTAINS + pSrcVarId;
						} else {
							tgtVarId = pDatPi.getSingleOrFirstValue();
						}
						saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
						matches++;
					}
				}
			} else if (clauseTestOnPartialMatchNotContains(pDatPi)) {
				for (String thisTval : targetVals) {
					//DSB 01/05/2015 (#1098)
					boolean result = false;
					
					if (this.ac.getCeConfig().isCaseSensitive()) {
						result = cVal.contains(thisTval);
					} else {
						result = cVal.toLowerCase().contains(thisTval.toLowerCase());
					}
					
					if (!result) {
						//We have a match
						String tgtVarId = "";
						if (pDatPi.hadQuotesOriginally(this.ac)) {
							tgtVarId = SPECIALVAL_NOTCONTAINS + pSrcVarId;
						} else {
							tgtVarId = pDatPi.getSingleOrFirstValue();
						}
						saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
						matches++;
					}
				}
			} else if (clauseTestOnPartialMatchMatches(pDatPi)) {
				for (String thisTval : targetVals) {
					//DSB 01/05/2015 (#1098)
					boolean result = false;
					
					if (this.ac.getCeConfig().isCaseSensitive()) {
						result = cVal.matches(thisTval);
					} else {
						result = cVal.toLowerCase().matches(thisTval.toLowerCase());
					}
					
					if (result) {
						//We have a match
						String tgtVarId = "";
						if (pDatPi.hadQuotesOriginally(this.ac)) {
							tgtVarId = SPECIALVAL_MATCHES + pSrcVarId;
						} else {
							tgtVarId = pDatPi.getSingleOrFirstValue();
						}
						saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
						matches++;
					}
				}
			} else if (clauseTestOnPartialMatchStartsWith(pDatPi)) {
				for (String thisTval : targetVals) {
					//DSB 01/05/2015 (#1098)
					boolean result = false;
					
					if (this.ac.getCeConfig().isCaseSensitive()) {
						result = cVal.startsWith(thisTval);
					} else {
						result = cVal.toLowerCase().startsWith(thisTval.toLowerCase());
					}
					
					if (result) {
						//We have a match
						String tgtVarId = "";
						if (pDatPi.hadQuotesOriginally(this.ac)) {
							tgtVarId = SPECIALVAL_STARTS + pSrcVarId;
						} else {
							tgtVarId = pDatPi.getSingleOrFirstValue();
						}
						saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
						matches++;
					}
				}
			} else if (clauseTestOnPartialMatchNotStartsWith(pDatPi)) {
				for (String thisTval : targetVals) {
					//DSB 01/05/2015 (#1098)
					boolean result = false;
					
					if (this.ac.getCeConfig().isCaseSensitive()) {
						result = cVal.startsWith(thisTval);
					} else {
						result = cVal.toLowerCase().startsWith(thisTval.toLowerCase());
					}
					
					if (!result) {
						//We have a match
						String tgtVarId = "";
						if (pDatPi.hadQuotesOriginally(this.ac)) {
							tgtVarId = SPECIALVAL_NOTSTARTS + pSrcVarId;
						} else {
							tgtVarId = pDatPi.getSingleOrFirstValue();
						}
						saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
						matches++;
					}
				}
			} else if (clauseTestOnPartialMatchEndsWith(pDatPi)) {
				for (String thisTval : targetVals) {
					//DSB 01/05/2015 (#1098)
					boolean result = false;
					
					if (this.ac.getCeConfig().isCaseSensitive()) {
						result = cVal.endsWith(thisTval);
					} else {
						result = cVal.toLowerCase().endsWith(thisTval.toLowerCase());
					}
					
					if (result) {
						//We have a match
						String tgtVarId = "";
						if (pDatPi.hadQuotesOriginally(this.ac)) {
							tgtVarId = SPECIALVAL_ENDS + pSrcVarId;
						} else {
							tgtVarId = pDatPi.getSingleOrFirstValue();
						}
						saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
						matches++;
					}
				}
			} else if (clauseTestOnNegativeMatch(pDatPi)) {
				for (String thisTval : targetVals) {
					//DSB 01/05/2015 (#1098)
					boolean result = false;
					
					if (this.ac.getCeConfig().isCaseSensitive()) {
						result = cVal.equals(thisTval);
					} else {
						result = cVal.toLowerCase().equals(thisTval.toLowerCase());
					}
					
					if (!result) {
						//We have a match
						String tgtVarId = "";
						if (pDatPi.hadQuotesOriginally(this.ac)) {
							tgtVarId = SPECIALVAL_NEG + pSrcVarId;
						} else {
							tgtVarId = pDatPi.getSingleOrFirstValue();
						}
						saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
						matches++;
					}
				}
			} else if (clauseTestOnGreaterThanMatch(pDatPi)) {
				for (String thisTval : targetVals) {
					try {
						double cDbl = new Double(cVal).doubleValue();
						double tDbl = new Double(thisTval).doubleValue();
						if (cDbl > tDbl) {
							//We have a match
							String tgtVarId = "";
							if (pDatPi.hadQuotesOriginally(this.ac)) {
								tgtVarId = SPECIALVAL_GT + pSrcVarId;
							} else {
								tgtVarId = pDatPi.getSingleOrFirstValue();
							}
							saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
							matches++;
						}
					} catch (NumberFormatException e) {
						reportError("Query could not be executed because the datatype being compared in a numeric operation is not a valid numeric value (" + cVal + ") and (" + thisTval + ")", this.ac);
					}
				}
			} else if (clauseTestOnGreaterThanOrEqualsMatch(pDatPi)) {
				for (String thisTval : targetVals) {
					try {
						double cDbl = new Double(cVal).doubleValue();
						double tDbl = new Double(thisTval).doubleValue();
						if (cDbl >= tDbl) {
							//We have a match
							String tgtVarId = "";
							if (pDatPi.hadQuotesOriginally(this.ac)) {
								tgtVarId = SPECIALVAL_GTE + pSrcVarId;
							} else {
								tgtVarId = pDatPi.getSingleOrFirstValue();
							}
							saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
							matches++;
						}
					} catch (NumberFormatException e) {
						reportError("Query could not be executed because the datatype being compared in a numeric operation is not a valid numeric value (" + cVal + ") and (" + thisTval + ")", this.ac);
					}
				}
			} else if (clauseTestOnLessThanMatch(pDatPi)) {
				for (String thisTval : targetVals) {
					try {
						double cDbl = new Double(cVal).doubleValue();
						double tDbl = new Double(thisTval).doubleValue();
						if (cDbl < tDbl) {
							//We have a match
							String tgtVarId = "";
							if (pDatPi.hadQuotesOriginally(this.ac)) {
								tgtVarId = SPECIALVAL_LT + pSrcVarId;
							} else {
								tgtVarId = pDatPi.getSingleOrFirstValue();
							}
							saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
							matches++;
						}
					} catch (NumberFormatException e) {
						reportError("Query could not be executed because the datatype being compared in a numeric operation is not a valid numeric value (" + cVal + ") and (" + thisTval + ")", this.ac);
					}
				}
			} else if (clauseTestOnLessThanOrEqualsMatch(pDatPi)) {
				for (String thisTval : targetVals) {
					try {
						double cDbl = new Double(cVal).doubleValue();
						double tDbl = new Double(thisTval).doubleValue();
						if (cDbl <= tDbl) {
							//We have a match
							String tgtVarId = "";
							if (pDatPi.hadQuotesOriginally(this.ac)) {
								tgtVarId = SPECIALVAL_LTE + pSrcVarId;
							} else {
								tgtVarId = pDatPi.getSingleOrFirstValue();
							}
							saveThisMatchedSpecialValue(pSrcVarId, tgtVarId, cVal, thisTval, propName);
							matches++;
						}
					} catch (NumberFormatException e) {
						reportError("Query could not be executed because the datatype being compared in a numeric operation is not a valid numeric value (" + cVal + ") and (" + thisTval + ")", this.ac);
					}
				}
			} else {
				reportError("Unable to match on datatype clause due to unknown comparison operator (" + pDatPi.getPropertyName() + ")", this.ac);
			}
		}

		//Return whether there were zero matches or not
		return (matches == 0);
	}

	private void saveSimpleInstance(String pSrcVar, CeInstance pSrcInst) {
		MatchedClauseList mcl = null;

		String mclId = pSrcVar;
		
		if (getMcls().containsKey(mclId)) {
			mcl = getMcls().get(mclId);
		} else {
			mcl = new MatchedClauseList();

			mcl.setSrcVarId(pSrcVar);
		}
		
		ArrayList<String> thisPair = new ArrayList<String>();
		thisPair.add(pSrcInst.getInstanceName());
		thisPair.add("");

		mcl.addMatchedPair(this.ac, thisPair);
		
		addToMcls(mclId, mcl);
	}

	private void removeSimpleInstance(String pSrcVar, CeInstance pSrcInst) {
		MatchedClauseList mcl = null;

		String mclId = pSrcVar;
		
		if (getMcls().containsKey(mclId)) {
			ArrayList<String> thisPair = new ArrayList<String>();
			thisPair.add(pSrcInst.getInstanceName());
			thisPair.add("");

			mcl = getMcls().get(mclId);
			mcl.removeMatchedPairFor(thisPair);
		}		
	}

	private void saveThisMatchedInstance(String pSrcVar, CeInstance pSrcInst, String pTgtVar, CeInstance pTgtInst, String pPropName) {
		MatchedClauseList mcl = null;

		String mclId = calculateMclId(pSrcVar, pTgtVar, pPropName);
		
		if (getMcls().containsKey(mclId)) {
			mcl = getMcls().get(mclId);
		} else {
			mcl = new MatchedClauseList();

			mcl.setSrcVarId(pSrcVar);
			mcl.setTgtVarId(pTgtVar);
			mcl.setPropertyName(pPropName);
		}
		
		ArrayList<String> thisPair = new ArrayList<String>();

		if (pSrcInst == null) {
			thisPair.add("");
		} else {
			thisPair.add(pSrcInst.getInstanceName());
		}
		
		if (pTgtInst == null) {
			thisPair.add("");
		} else {
			thisPair.add(pTgtInst.getInstanceName());
		}

		mcl.addMatchedPair(this.ac, thisPair);
		
		addToMcls(mclId, mcl);
	}
	
	private void saveThisMatchedNormalValue(String pSrcVar, CeInstance pSrcInst, String pTgtVar, String pTgtVal, String pPropName) {
		ArrayList<String> thisPair = new ArrayList<String>();
		//TODO: One of these can be deleted (if the method is called in the correct context)
		thisPair.add(pSrcInst.getInstanceName());			// For source value based matches the first item in the pair isn't needed
		thisPair.add(pTgtVal);

		saveCommonMatchedValues(pSrcVar, pTgtVar, pPropName, thisPair, false);
	}
	
	private void saveThisMatchedSpecialValue(String pSrcVar, String pTgtVar, String pSrcVal, String pTgtVal, String pPropName) {
		ArrayList<String> thisPair = new ArrayList<String>();
		thisPair.add(pSrcVal);
		//DSB 09/09/2012 - Previously the target value was not saved, but this must be in cases where both source and target are variables
		//		thisPair.add("");
		thisPair.add(pTgtVal);

		saveCommonMatchedValues(pSrcVar, pTgtVar, pPropName, thisPair, true);
	}

	private void saveThisMatchedConcatValue(String pSrcVar, String pTgtVar, String pSrcVal, String pTgtVal) {
		ArrayList<String> thisPair = new ArrayList<String>();
		thisPair.add(pSrcVal);
		thisPair.add(pTgtVal);

		saveCommonMatchedValues(pSrcVar, pTgtVar, "(concat)", thisPair, true);
	}

	private void saveCommonMatchedValues(String pSrcVar, String pTgtVar, String pPropName, ArrayList<String> pPair, boolean pIsSpecial) {
		MatchedClauseList mcl = null;
		String mclId = calculateMclId(pSrcVar, pTgtVar, pPropName);
		
		if (getMcls().containsKey(mclId)) {
			mcl = getMcls().get(mclId);
		} else {
			mcl = new MatchedClauseList();

			mcl.setSrcVarId(pSrcVar);
			mcl.setTgtVarId(pTgtVar);
			mcl.setPropertyName(pPropName);
			
			if (pIsSpecial) {
				mcl.markAsSpecialOperatorMcl();
				if (!pTgtVar.startsWith(MARKER_SPECIALOP_START)) {
					mcl.markAsDoubleVariable();
				}
			}
		}

		mcl.addMatchedPair(this.ac, pPair);
		addToMcls(mclId, mcl);
	}

}
