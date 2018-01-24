package com.ibm.ets.ita.ce.store.query;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_EQUALS;
import static com.ibm.ets.ita.ce.store.names.CeNames.SPECIALNAME_NOTEQUALS;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.MiscNames.HDR_COUNT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COUNT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SUM;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_UNDERSCORE;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportExecutionTiming;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeClause;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;

public class QueryResultProcessorMem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CLASS_NAME = QueryResultProcessorMem.class.getName();

	private static final String SPECIALVAL_START = "(";

	private ActionContext ac = null;
	private CeQuery targetQuery = null;
	private TreeMap<String, MatchedClauseList> mcls = null;

	private int numberOfVariables = -1;

	private int itCounter = 0;

	public QueryResultProcessorMem(ActionContext pAc, TreeMap<String, MatchedClauseList> pMcls, CeQuery pQuery) {
		this.ac = pAc;
		this.targetQuery = pQuery;
		this.mcls = pMcls;

		this.itCounter = 0;
	}

	private void setNumberOfVariables(int pNum) {
		this.numberOfVariables = pNum;
	}
	
	private MatchedClauseList largestNormalMcl() {
		//Return the largest normal MCL
		MatchedClauseList result = null;
		
		for (MatchedClauseList thisMcl : this.mcls.values()) {
			if (thisMcl.isNormalMcl()) {
				if ((result == null) || (thisMcl.getMatchedPairs().size() > result.getMatchedPairs().size())) {
					result = thisMcl;
				}
			}
		}
		
		return result;
	}

	private MatchedClauseList firstMclFor(String pHdrName) {
		//Return the first MCL with a source or target with the specified name
		MatchedClauseList result = null;
		
		for (MatchedClauseList thisMcl : this.mcls.values()) {
			if (result == null) {
				if ((thisMcl.getSrcVarId().equals(pHdrName)) || (thisMcl.getTgtVarId().equals(pHdrName))) {
					result = thisMcl;
				}
			}
		}
		
		return result;
	}

	private MatchedClauseList firstMcl() {
		//Return the first normal MCL
		MatchedClauseList result = null;
		
		for (MatchedClauseList thisMcl : this.mcls.values()) {
			if (result == null) {
				result = thisMcl;
			}
		}
		
		return result;
	}

	public ContainerCeResult generateResultFromMcls() {
		final String METHOD_NAME = "generateResultFromMcls";

		long sTime = System.currentTimeMillis();

		prepareMcls();

		ArrayList<TreeMap<String, String>> resultRows = calculateResultRows();

		reportExecutionTiming(this.ac, sTime, "[2.a] calculateResultRows", CLASS_NAME, METHOD_NAME);
		ContainerCeResult result = generateResultContainerUsing(resultRows);

		reportExecutionTiming(this.ac, sTime, "[2.b] generateResultContainerUsing", CLASS_NAME, METHOD_NAME);

		return result;
	}
	
	private ArrayList<TreeMap<String, String>> calculateResultRows() {
		ArrayList<TreeMap<String, String>> resultRows = new ArrayList<TreeMap<String, String>>();
		
		boolean noMatches = processHeaders();

		if (!noMatches) {
			MatchedClauseList tgtMcl = largestNormalMcl();
			
			if (tgtMcl == null) {
				tgtMcl = firstMcl();
			}
			
			if (tgtMcl != null) {
				if (isSingleGraph()) {
					resultRows = newProcessAllMclsWith(tgtMcl, calculateGraphHeaders());
				} else {
					reportDebug("This is not a single graph and is therefore not supported (" + this.targetQuery.getQueryName() + ")", this.ac);
					resultRows = newProcessAllMclsFromMultipleGraphs();
				}

				doPostProcessing(resultRows);
				reportDebugStats(resultRows);
			}
		}
		
		return resultRows;
	}

	private void prepareMcls() {
		linkMcls();
		truncateMcls();
	}

	private void linkMcls() {
		for (String key1 : this.mcls.keySet()) {
			MatchedClauseList mcl1 = this.mcls.get(key1);
			//Also calculate the source and target values
			mcl1.calculateSourceAndTargetVals();

			for (String key2 : this.mcls.keySet()) {
				MatchedClauseList mcl2 = this.mcls.get(key2);

				if (!mcl1 .equals(mcl2)) {
					if (mcl1.matches(mcl2)) {
						mcl1.addLinkedMcl(mcl2);
					}
				}
			}
		}
	}

	private void truncateMcls() {
		final String METHOD_NAME = "truncateMcls";

		long sTime = System.currentTimeMillis();

		boolean removalsMade = true;
		while (removalsMade) {
			removalsMade = doTruncation();
		}

		reportExecutionTiming(this.ac, sTime, "[4] truncateMcls", CLASS_NAME, METHOD_NAME);
	}
	
	private boolean doTruncation() {
		boolean removalsMade = false;
		
		for (String key : this.mcls.keySet()) {
			MatchedClauseList thisMcl = this.mcls.get(key);
			int mpCount = thisMcl.getMatchedPairs().size();
			this.itCounter += thisMcl.eliminateNonMatchingPairs();
			
			if(thisMcl.getMatchedPairs().size() != mpCount) {
				removalsMade = true;
			}
		}
		
		return removalsMade;
	}

	private boolean processHeaders() {
		boolean result = false;
		
		ArrayList<TreeMap<String, String>> allRows = new ArrayList<TreeMap<String, String>>();
		ArrayList<TreeMap<String, String>> existenceRows = new ArrayList<TreeMap<String, String>>();
		ArrayList<String> existenceHeaders = new ArrayList<String>();
		ArrayList<String> nonExistenceHeaders = new ArrayList<String>();
		
		for (String key : this.mcls.keySet()) {
			MatchedClauseList srcMcl = this.mcls.get(key);
			if (srcMcl.isExistenceMcl()) {
				String srcVarId = srcMcl.getSrcVarId();
				
				//The special header of "(none)" must not be added
				if (!srcVarId.startsWith(SPECIALVAL_START)) {
					existenceHeaders.add(srcVarId);
				}
			} else {
				String srcVarId = srcMcl.getSrcVarId();
				String tgtVarId = srcMcl.getTgtVarId();
				if ((srcVarId != null) && (!srcVarId.startsWith(SPECIALVAL_START))) {
					if (!nonExistenceHeaders.contains(srcVarId)) {
						nonExistenceHeaders.add(srcVarId);
					}
				}
				if ((tgtVarId != null) && (!tgtVarId.startsWith(SPECIALVAL_START))) {
					if (!nonExistenceHeaders.contains(tgtVarId)) {
						nonExistenceHeaders.add(tgtVarId);
					}
				}
			}
		}
		
		//Process all the normal (non existence) headers
		for (String thisHdr : nonExistenceHeaders) {
			for (String key : this.mcls.keySet()) {
				MatchedClauseList srcMcl = this.mcls.get(key);
				if (srcMcl.getSrcVarId().equals(thisHdr)) {
					ArrayList<TreeMap<String, String>> matchedRows = srcMcl.processPairs(nonExistenceHeaders);
					
					if (!matchedRows.isEmpty()) {
						allRows.addAll(matchedRows);
					} else {
						result = true;
					}
				}
			}
		}
		
		//Process all the existence headers
		for (String thisHdr : existenceHeaders) {
			for (String key : this.mcls.keySet()) {
				MatchedClauseList srcMcl = this.mcls.get(key);
				//Existence MCLs should not be added into the results...  they will never match with anything and will be concatenated at the end
				if (srcMcl.getSrcVarId().equals(thisHdr)) {
					ArrayList<TreeMap<String, String>> matchedRows = srcMcl.processPairs(existenceHeaders);
					
					if (!matchedRows.isEmpty()) {
						existenceRows.addAll(matchedRows);
					} else {
						result = true;
					}
				}
			}
		}
		
		return result;
	}
		
	private ContainerCeResult generateResultContainerUsing(ArrayList<TreeMap<String, String>> pResultRows) {
		ContainerCeResult result = new ContainerCeResult();
		result.setTargetQuery(this.targetQuery);
		result.setQuery(this.targetQuery.getCeText());

		if (this.targetQuery.isCountQuery()) {
			reportCountResults(pResultRows, result);
		} else {
			reportNormalResults(this.targetQuery, pResultRows, result);
		}

		return result;
	}

	private boolean isSingleGraph() {
		return calculateGraphHeaderList(true).size() == 1;
	}

	private void reportDebugStats(ArrayList<TreeMap<String, String>> pResult) {
		if (isReportMicroDebug()) {
			reportMicroDebug("Iteration counter = " + new Integer(this.itCounter).toString() + " (Truncation steps)", this.ac);
			reportMicroDebug("Result = " + new Integer(pResult.size()).toString(), this.ac);
		}
	}

	private void doPostProcessing(ArrayList<TreeMap<String, String>> pResult) {
		removeEqualsAndNotEquals(pResult);
	}

	private static ArrayList<TreeMap<String, String>> calculateResultPermutations(ArrayList<ArrayList<TreeMap<String, String>>> pResList) {
		ArrayList<TreeMap<String, String>> combinedRes = new ArrayList<TreeMap<String, String>>();

		for (ArrayList<TreeMap<String, String>> tgtRes : pResList) {
			combinedRes = mergeInto(combinedRes, tgtRes);
		}
		
		return combinedRes;
	}

	private static ArrayList<TreeMap<String, String>> mergeInto(ArrayList<TreeMap<String, String>> pCombinedRes, ArrayList<TreeMap<String, String>> pTargetRes) {
		ArrayList<TreeMap<String, String>> result = new ArrayList<TreeMap<String, String>>();

		if (pCombinedRes.isEmpty()) {
			//Simple case - empty combined result so just add target result
			result.addAll(pTargetRes);
		} else {
			//Complex case - combined result already exists so create permutations with target result
			
			for (TreeMap<String, String> combinedRow : pCombinedRes) {
				for (TreeMap<String, String> targetRow : pTargetRes) {
					TreeMap<String, String> newRow = new TreeMap<String, String>();
					newRow.putAll(combinedRow);
					result.add(newRow);
					for (String tgtKey : targetRow.keySet()) {
						String tgtVal = targetRow.get(tgtKey);
						newRow.put(tgtKey,  tgtVal);
					}
				}
			}
		}
		
		return result;
	}
	
	private ArrayList<TreeSet<String>> calculateGraphHeaderList(boolean pSilent) {
		ArrayList<TreeSet<String>> rawResult = new ArrayList<TreeSet<String>>();
		ArrayList<TreeSet<String>> finalResult = new ArrayList<TreeSet<String>>();

		for (MatchedClauseList thisMcl : this.mcls.values()) {
			rawResult.add(thisMcl.calculateAllLinkedVarIds());
		}
		
		finalResult =  eliminateDuplicatesFrom(rawResult);
		
		if (isReportMicroDebug()) {
			if (!pSilent && (finalResult.size() > 1)) {
				for (TreeSet<String> thisRow : finalResult) {
					reportMicroDebug("Graph Header -> " + thisRow.toString(), this.ac);
				}
			}
		}
		
		return finalResult;
	}
	
	private static ArrayList<TreeSet<String>> eliminateDuplicatesFrom(ArrayList<TreeSet<String>> pRawList) {
		ArrayList<TreeSet<String>> result = new ArrayList<TreeSet<String>>();
		HashMap<String, TreeSet<String>> tempMap = new HashMap<String, TreeSet<String>>();
		
		for (TreeSet<String> thisSet : pRawList) {
			String uniqueKey = thisSet.toString();
			tempMap.put(uniqueKey, thisSet);
		}
		
		for (TreeSet<String> finalVal : tempMap.values()) {
			result.add(finalVal);
		}
		
		return result;
	}
	
	private ArrayList<TreeMap<String, String>> newProcessAllMclsWith(MatchedClauseList pSeedMcl, TreeSet<String> pGraphHeaders) {
		setNumberOfVariables(pGraphHeaders.size());		
		buildMclLists(pSeedMcl, null);
		
		ResultSetWrapper rsw = new ResultSetWrapper();		
		computeRows(pSeedMcl, null, rsw);
		
		ArrayList<TreeMap<String, String>> result = new ArrayList<TreeMap<String, String>>();
		
		ArrayList<HashMap<String, String>> tempResult = rsw.listCompleteRows(this.numberOfVariables);

		for (HashMap<String, String> thisMap : tempResult) {
			TreeMap<String, String> treeMap = new TreeMap<String, String>(thisMap);
			result.add(treeMap);
		}

		return result;
	}

	private ArrayList<TreeMap<String, String>> newProcessAllMclsFromMultipleGraphs() {
		ArrayList<ArrayList<TreeMap<String, String>>> resultList = new ArrayList<ArrayList<TreeMap<String, String>>>();
		ArrayList<TreeSet<String>> graphHeaderList = calculateGraphHeaderList(false);

		for (TreeSet<String> graphHeaders : graphHeaderList) {
			String firstGraphHeader = graphHeaders.iterator().next();
			MatchedClauseList seedMcl = firstMclFor(firstGraphHeader);
			resultList.add(newProcessAllMclsWith(seedMcl, graphHeaders));
		}

		return calculateResultPermutations(resultList);
	}

	private ArrayList<MatchedClauseList> doDoneMclProcessing(ArrayList<MatchedClauseList> pDoneMcls, MatchedClauseList pMcl) {
		ArrayList<MatchedClauseList> result = null;

		if (pDoneMcls == null) {
			result = new ArrayList<MatchedClauseList>();
		} else{
			result = pDoneMcls;
		}

		if (!result.contains(pMcl)) {
			result.add(pMcl);
		}

		return result;
	}
	
	private void computeRows(MatchedClauseList pMcl, ArrayList<MatchedClauseList> pDoneMcls, ResultSetWrapper pRsw) {
		ArrayList<MatchedClauseList> doneMcls = doDoneMclProcessing(pDoneMcls, pMcl);
		
		if (pMcl.getPropertyName().equals(SPECIALNAME_NOTEQUALS)) {
			//Ignore not-equals operators - they are processed later
			reportDebug("Ignoring '!=' MCL for '" + pMcl.toString() + "'", this.ac);
		} else {
			if (pRsw.isEmpty()) {
				computeRowsForFirstMcl(pMcl, pDoneMcls, pRsw);
			} else {
				computeRowsNormal(pMcl, pDoneMcls, pRsw);
			}
		}

		pRsw.saveNewRows();

		for (MatchedClauseList srcLinkedMcl : pMcl.getLinkedMclsForSourceVarExcluding(doneMcls)) {
			if (!srcLinkedMcl.isSimpleSpecialOperatorMcl()) {
				computeRows(srcLinkedMcl, doneMcls, pRsw);
			}
		}

		for (MatchedClauseList tgtLinkedMcl : pMcl.getLinkedMclsForTargetVarExcluding(doneMcls)) {
			if (!tgtLinkedMcl.isSimpleSpecialOperatorMcl()) {
				computeRows(tgtLinkedMcl, doneMcls, pRsw);
			}
		}
	}

	private void computeRowsForFirstMcl(MatchedClauseList pMcl, ArrayList<MatchedClauseList> pDoneMcls, ResultSetWrapper pRsw) {
		String tgtVarId = pMcl.getTgtVarId();
		String srcVarId = pMcl.getSrcVarId();
		Set<String> srcKeys = pMcl.getSrcToTgtList().keySet();
		HashSet<HashMap<String, String>> newRows = new HashSet<HashMap<String, String>>();

		for (String srcVal : srcKeys) {
			if (tgtVarId.isEmpty()) {
				//Special case for one-sided clauses
				HashMap<String, String> newRow = new HashMap<String, String>();
				newRow.put(srcVarId, srcVal);

				newRows.add(newRow);
			} else {
				//Normal cases for two-sided clauses
				ArrayList<String> tgtVals = pMcl.getSrcToTgtList().get(srcVal);

				for (String tgtVal : tgtVals) {
					HashMap<String, String> newRow = new HashMap<String, String>();
					newRow.put(srcVarId, srcVal);
					newRow.put(tgtVarId, tgtVal);
					
					newRows.add(newRow);
				}
			}
		}
		
		if (!newRows.isEmpty()) {
			pRsw.saveTheseRows(newRows);
		}
	}

	private void computeRowsNormal(MatchedClauseList pMcl, ArrayList<MatchedClauseList> pDoneMcls, ResultSetWrapper pRsw) {
		String srcVarId = pMcl.getSrcVarId();
		String tgtVarId = pMcl.getTgtVarId();
		Set<String> srcKeys = pMcl.getSrcToTgtList().keySet();

		for (String srcVal : srcKeys) {
			if (tgtVarId.isEmpty()) {
				//Special case for one-sided clauses
				pRsw.processOneSidedClause(srcVarId, srcVal);
			} else {
				//Normal cases for two-sided clauses
				ArrayList<String> tgtVals = pMcl.getSrcToTgtList().get(srcVal);

				for (String tgtVal : tgtVals) {
					boolean foundRow = false;

					//Look for existing rows to be added to
					foundRow = pRsw.processingForAdd(srcVarId, srcVal, tgtVarId, tgtVal, foundRow);

					//Look for rows that need to be duplicated
					if (!foundRow) {
						foundRow = pRsw.processingForDuplicate(foundRow, srcVarId, srcVal, tgtVarId, tgtVal);
						
						//Nothing was found so create a new row
						if (!foundRow) {
							pRsw.processSimpleRowAdd(srcVarId, srcVal, tgtVarId, tgtVal);
						}
					}
				}
			}
		}
	}

	private void buildMclLists(MatchedClauseList pMcl, ArrayList<MatchedClauseList> pDoneMcls) {
		ArrayList<MatchedClauseList> doneMcls = doDoneMclProcessing(pDoneMcls, pMcl);

		TreeMap<String, ArrayList<String>> srcToTgtList = new TreeMap<String, ArrayList<String>>();
		TreeMap<String, ArrayList<String>> tgtToSrcList = new TreeMap<String, ArrayList<String>>();

		for (ArrayList<String> thisPair : pMcl.getMatchedPairs().values()) {
			String srcVal = thisPair.get(0);
			String tgtVal = thisPair.get(1);
			
			listProcessing(srcToTgtList, srcVal, tgtVal);
			listProcessing(tgtToSrcList, tgtVal, srcVal);
		}

		pMcl.setSrcToTgtList(srcToTgtList);
		pMcl.setTgtToSrcList(tgtToSrcList);

		for (MatchedClauseList linkedMcl : pMcl.getLinkedMcls()) {
			if (!doneMcls.contains(linkedMcl)) {
				buildMclLists(linkedMcl, doneMcls);
			}
		}
	}
	
	private void listProcessing(TreeMap<String, ArrayList<String>> pList, String pFirstVal, String pSecondVal) {
		ArrayList<String> tgtList = null;
		
		if (pList.containsKey(pFirstVal)) {
			tgtList = pList.get(pFirstVal);
		} else {
			tgtList = new ArrayList<String>();
			pList.put(pFirstVal, tgtList);
		}

		tgtList.add(pSecondVal);
	}

	private void removeEqualsAndNotEquals(ArrayList<TreeMap<String, String>> pResult) {
		for (CeClause thisClause : this.targetQuery.getDirectPremiseClauses()) {
			testEqualsAndNotEqualsForClause(thisClause, pResult);			
		}
	}

	private void testEqualsAndNotEqualsForClause(CeClause pClause, ArrayList<TreeMap<String, String>> pResult) {
		for (CePropertyInstance thisPi : pClause.getAllProperties()) {
			if (thisPi.isSpecialOperatorPropertyInstance()) {
				String srcVarId = thisPi.getClauseVariableId();

				if (thisPi.hadQuotesOriginally(this.ac)) {
					//This is a fixed value
					String tgtVal = thisPi.getSingleOrFirstValue();
					if (thisPi.getRelatedProperty().getPropertyName().equals(SPECIALNAME_NOTEQUALS)) {
						deleteMatchingRowsForValueFrom(pResult, srcVarId, tgtVal);
					} else if (thisPi.getRelatedProperty().getPropertyName().equals(SPECIALNAME_EQUALS)) {
						deleteUnmatchingRowsForValueFrom(pResult, srcVarId, tgtVal);
					}
				} else {
					//This is a variable
					String tgtVarId = thisPi.getSingleOrFirstValue();
					if (thisPi.getRelatedProperty().getPropertyName().equals(SPECIALNAME_NOTEQUALS)) {
						deleteMatchingRowsForVariableIdFrom(pResult, srcVarId, tgtVarId);
					} else if (thisPi.getRelatedProperty().getPropertyName().equals(SPECIALNAME_EQUALS)) {
						deleteUnmatchingRowsForVariableIdFrom(pResult, srcVarId, tgtVarId);
					}
				}
			}
		}
	}

	private static void deleteMatchingRowsForValueFrom(ArrayList<TreeMap<String, String>> pResult, String pSrcVarId, String pTgtVal) {
		ArrayList<TreeMap<String, String>> copyRows = new ArrayList<TreeMap<String, String>>();
		copyRows.addAll(pResult);

		for (TreeMap<String, String> thisRow : copyRows) {
			String srcVal = thisRow.get(pSrcVarId);
			
			if (srcVal.equals(pTgtVal)) {
				pResult.remove(thisRow);
			}
		}
	}

	private static void deleteUnmatchingRowsForValueFrom(ArrayList<TreeMap<String, String>> pResult, String pSrcVarId, String pTgtVal) {
		ArrayList<TreeMap<String, String>> copyRows = new ArrayList<TreeMap<String, String>>();
		copyRows.addAll(pResult);

		for (TreeMap<String, String> thisRow : copyRows) {
			String srcVal = thisRow.get(pSrcVarId);
			
			if (!srcVal.equals(pTgtVal)) {
				pResult.remove(thisRow);
			}
		}
	}

	private static void deleteMatchingRowsForVariableIdFrom(ArrayList<TreeMap<String, String>> pResult, String pSrcVarId, String pTgtVarId) {
		ArrayList<TreeMap<String, String>> copyRows = new ArrayList<TreeMap<String, String>>();
		copyRows.addAll(pResult);

		for (TreeMap<String, String> thisRow : copyRows) {
			String srcVal = thisRow.get(pSrcVarId);
			String tgtVal = thisRow.get(pTgtVarId);
			
			if (srcVal.equals(tgtVal)) {
				pResult.remove(thisRow);
			}
		}
	}

	private static void deleteUnmatchingRowsForVariableIdFrom(ArrayList<TreeMap<String, String>> pResult, String pSrcVarId, String pTgtVarId) {
		ArrayList<TreeMap<String, String>> copyRows = new ArrayList<TreeMap<String, String>>();
		copyRows.addAll(pResult);

		for (TreeMap<String, String> thisRow : copyRows) {
			String srcVal = thisRow.get(pSrcVarId);
			String tgtVal = thisRow.get(pTgtVarId);
			
			if (!srcVal.equals(tgtVal)) {
				pResult.remove(thisRow);
			}
		}
	}

	private TreeSet<String> calculateGraphHeaders() {
		TreeSet<String> tempSet = new TreeSet<String>();

		for (MatchedClauseList thisMcl : this.mcls.values()) {
			if (!thisMcl.isSimpleSpecialOperatorMcl()) {
				String srcVar = thisMcl.getSrcVarId();
				String tgtVar = thisMcl.getTgtVarId();

				if ((srcVar != null) && (!srcVar.isEmpty())) {
					tempSet.add(srcVar);
				}

				if ((tgtVar != null) && (!tgtVar.isEmpty())) {
					tempSet.add(tgtVar);
				}
			}
		}

		return tempSet;
	}

	private static void reportCountResults(ArrayList<TreeMap<String, String>> pRows, ContainerCeResult pResult) {
		ArrayList<String> countResLine = new ArrayList<String>();
		int rowSize = 0;

		if (pRows != null) {
			rowSize = pRows.size();
		}

		countResLine.add(Integer.toString(rowSize));

		pResult.addHeader(HDR_COUNT, HDR_COUNT);
		pResult.addResultRow(countResLine);		
	}

	private void reportNormalResults(CeQuery pQuery, ArrayList<TreeMap<String, String>> pRows, ContainerCeResult pResult) {
		//Add each header
		for (String thisHdr : pQuery.getResponseVariableIds()) {
			if (!pQuery.hasNegatedHeader(this.ac, thisHdr)) {
				pResult.addHeader(thisHdr, pQuery.getTypeForHeader(thisHdr));
			}
		}

		ArrayList<TreeMap<String, String>> processedRows = null;

		if ((!pQuery.isRule()) && (pResult.hasCountHeader())) {
			processedRows = processRowsForCounts(pRows, pResult);
		} else if ((!pQuery.isRule()) && (pResult.hasSumHeader())) {
			processedRows = processRowsForSums(pRows, pResult);
		} else {
			processedRows = pRows;
		}

		if (pRows != null) {
			if (pQuery.needsSorting()) {
				addSortedRows(pQuery, processedRows, pResult);
			} else {
				addUnsortedRows(pQuery, processedRows, pResult);
			}
		}
	}

	private ArrayList<TreeMap<String, String>> processRowsForCounts(ArrayList<TreeMap<String, String>> pRows, ContainerCeResult pResult) {
		ArrayList<TreeMap<String, String>> result = new ArrayList<TreeMap<String, String>>();
		boolean doneCountVariable = false;
		
		for (String thisHdr : pResult.getHeaders()) {
			if (CeQuery.isCountHeader(thisHdr)) {
				String rawHdr = thisHdr.replace(TOKEN_COUNT, ES);

				if (!doneCountVariable) {
					TreeMap<String, TreeMap<String, String>> tempMap = new TreeMap<String, TreeMap<String,String>>();
					doneCountVariable = true;

					for (TreeMap<String, String> thisRow : pRows) {
						TreeMap<String, String> newRow = new TreeMap<String, String>();
						String newKey = ES;
						TreeMap<String, String> existingRow = tempMap.get(newKey);

						for (String rowHdr : thisRow.keySet()) {
							if (pResult.getHeaders().contains(rowHdr)) {
								if (!rawHdr.equals(rowHdr)) {
									String thisVal = thisRow.get(rowHdr);

									newRow.put(rowHdr, thisVal);
									if (!newKey.isEmpty()) {
										newKey += ", ";
									}
									newKey += rowHdr + "=" + thisVal;
								}
							}
						}
						
						existingRow = tempMap.get(newKey);

						if (existingRow == null) {
							newRow.put(thisHdr, "1");	//TODO: Abstract this
							tempMap.put(newKey, newRow);
						} else {
							int currCount = new Integer(existingRow.get(thisHdr)).intValue();
							String newCountVal = new Integer(++currCount).toString();
							existingRow.put(thisHdr, newCountVal);
						}
					}
					
					for (TreeMap<String, String> newRow : tempMap.values()) {
						result.add(newRow);
					}
				} else {
					reportWarning("More than one count variable is not yet supported.  The request to count '" + thisHdr + "' will be ignored", this.ac);
				}
			}
		}

		return result;
	}

	private ArrayList<TreeMap<String, String>> processRowsForSums(ArrayList<TreeMap<String, String>> pRows, ContainerCeResult pResult) {
		ArrayList<TreeMap<String, String>> result = new ArrayList<TreeMap<String, String>>();
		boolean doneSumVariable = false;

		for (String thisHdr : pResult.getHeaders()) {
			if (CeQuery.isSumHeader(thisHdr)) {
				String rawHdr = thisHdr.replace(TOKEN_SUM, ES);

				if (!doneSumVariable) {
					TreeMap<String, TreeMap<String, String>> tempMap = new TreeMap<String, TreeMap<String,String>>();
					doneSumVariable = true;

					for (TreeMap<String, String> thisRow : pRows) {
						TreeMap<String, String> newRow = new TreeMap<String, String>();
						String newKey = ES;
						TreeMap<String, String> existingRow = tempMap.get(newKey);

						for (String rowHdr : thisRow.keySet()) {
							if (pResult.getHeaders().contains(rowHdr)) {
								if (!rawHdr.equals(rowHdr)) {
									String thisVal = thisRow.get(rowHdr);

									newRow.put(rowHdr, thisVal);
									if (!newKey.isEmpty()) {
										newKey += ", ";
									}
									newKey += rowHdr + "=" + thisVal;
								}
							}
						}

						existingRow = tempMap.get(newKey);
						String txtVal = thisRow.get(rawHdr);
						long thisVal = 0;
						long currSum = 0;

						try {
							thisVal = new Long(txtVal).longValue();
						} catch(NumberFormatException e) {
							reportWarning("Unable to convert '" + txtVal + "' to number for summing", this.ac);
							thisVal = 0;
						}

						if (existingRow == null) {
							existingRow = newRow;
							tempMap.put(newKey, existingRow);
							currSum = 0;
						} else {
							currSum = new Long(existingRow.get(thisHdr)).longValue();
						}

						String newSumVal = new Long(currSum + thisVal).toString();
						existingRow.put(thisHdr, newSumVal);
					}

					for (TreeMap<String, String> newRow : tempMap.values()) {
						result.add(newRow);
					}
				} else {
					reportWarning("More than one sum variable is not yet supported.  The request to sum '" + thisHdr + "' will be ignored", this.ac);
				}
			}
		}

		return result;
	}

	private void addSortedRows(CeQuery pQuery, ArrayList<TreeMap<String, String>> pRows, ContainerCeResult pResult) {
		ArrayList<TreeMap<String, String>> sortedRows = null;
		TreeMap<String, TreeMap<String, String>> tempRows = new TreeMap<String, TreeMap<String,String>>();

		int ctr = 0;
		for (TreeMap<String, String> thisRow : pRows) {
			String sortedKey = ES;
			for (String sortHdr : pQuery.getOrderTokens()) {
				try {
					Integer countNum = new Integer(thisRow.get(sortHdr));
					sortedKey += String.format("%12d", countNum);	//TODO: Abstract this
					//TODO: Improve this to handle numbers larger than 999999999999
				} catch (NumberFormatException e) {
					sortedKey += thisRow.get(sortHdr);
				}
			}

			//Add a counter to the end to ensure keys are unique
			sortedKey += TOKEN_UNDERSCORE + ctr++;

			tempRows.put(sortedKey, thisRow);
		}

		sortedRows = new ArrayList<TreeMap<String, String>>(tempRows.values());

		if (!pQuery.isSortOrderAscending()) {
			Collections.reverse(sortedRows);
		}

		//Now just add the rows in the usual way
		addUnsortedRows(pQuery, sortedRows, pResult);
	}

	private static void addUnsortedRows(CeQuery pQuery, ArrayList<TreeMap<String, String>> pRows, ContainerCeResult pResult) {
		//Add each row
		for (TreeMap<String, String> thisRow : pRows) {
			ArrayList<String> thisResLine = new ArrayList<String>();
			for (String thisHdr : pQuery.getResponseVariableIds()) {
				if (pResult.getHeaders().contains(thisHdr)) {
					String thisVal = thisRow.get(thisHdr);
					thisResLine.add(thisVal);
				}
			}
			pResult.addResultRow(thisResLine);

			ArrayList<String> thisAllLine = new ArrayList<String>();
			for (String thisHdr : pQuery.getAllVariableIds()) {
				String thisVal = thisRow.get(thisHdr);
				thisAllLine.add(thisVal);
			}
			pResult.addAllRow(thisAllLine);
		}
	}

}
