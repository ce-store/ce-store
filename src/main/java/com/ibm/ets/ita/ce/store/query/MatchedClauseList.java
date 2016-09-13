package com.ibm.ets.ita.ce.store.query;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSpecialProperty;

public class MatchedClauseList {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String srcVarId = "";
	private String tgtVarId = "";
	private String propertyName = "";
	private TreeMap<String, ArrayList<String>> matchedPairs = new TreeMap<String, ArrayList<String>>();
	private ArrayList<MatchedClauseList> linkedMcls = new ArrayList<MatchedClauseList>();
	private TreeMap<String, String> sourceVals = new TreeMap<String, String>();
	private TreeMap<String, String> targetVals = new TreeMap<String, String>();
	private boolean specialOperatorMcl = false;
	private boolean doubleVariableMcl = false;

	//DSB 29/10/2015 - Added for new result set processing
	private TreeMap<String, ArrayList<String>> srcToTgtList = null;
	private TreeMap<String, ArrayList<String>> tgtToSrcList = null;

	public String getSrcVarId() {
		return this.srcVarId;
	}

	public void setSrcVarId(String pSrcVarId) {
		this.srcVarId = pSrcVarId;
	}

	public String getTgtVarId() {
		return this.tgtVarId;
	}

	public void setTgtVarId(String pTgtVarId) {
		this.tgtVarId = pTgtVarId;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	public void setPropertyName(String pPropName) {
		this.propertyName = pPropName;
	}

	public TreeMap<String, ArrayList<String>> getMatchedPairs() {
		return this.matchedPairs;
	}

	public void addMatchedPair(ActionContext pAc, ArrayList<String> pMatchedPair) {
		if (validMatchedPair(pMatchedPair)) {
			this.matchedPairs.put(calculateMpKeyFor(pMatchedPair), pMatchedPair);
		} else {
			reportError("Invalid matched pair encountered [" + pMatchedPair.get(0) + ", " + pMatchedPair.get(1) + "]", pAc);
		}
	}

	public void removeMatchedPairFor(ArrayList<String> pMatchedPair) {
		String mpKey = calculateMpKeyFor(pMatchedPair);
		this.matchedPairs.remove(mpKey);
	}

	private static boolean validMatchedPair(ArrayList<String> pMatchedPair) {
		return (pMatchedPair.get(0) != null) && (pMatchedPair.get(1) != null);
	}

	private static String calculateMpKeyFor(ArrayList<String> pMatchedPair) {
		String calcKey = "";
		calcKey += "0=" + pMatchedPair.get(0) + ", "; 
		calcKey += "1=" + pMatchedPair.get(1); 

		return calcKey;
	}

	public ArrayList<MatchedClauseList> getLinkedMcls() {
		return this.linkedMcls;
	}

	public ArrayList<MatchedClauseList> getLinkedMclsForSourceVarExcluding(ArrayList<MatchedClauseList> pDoneMcls) {
		ArrayList<MatchedClauseList> result = new ArrayList<MatchedClauseList>();

		for (MatchedClauseList thisMcl : this.linkedMcls) {
			if (thisMcl.getSrcVarId().equals(this.srcVarId)) {
				if (!pDoneMcls.contains(thisMcl)) {
					//Match on source
					result.add(thisMcl);
				}
			} else if (thisMcl.getTgtVarId().equals(this.srcVarId)) {
				if (!pDoneMcls.contains(thisMcl)) {
					//Match on target
					result.add(thisMcl);
				}
			}
		}

		return result;
	}

	public ArrayList<MatchedClauseList> getLinkedMclsForTargetVarExcluding(ArrayList<MatchedClauseList> pDoneMcls) {
		ArrayList<MatchedClauseList> result = new ArrayList<MatchedClauseList>();

		for (MatchedClauseList thisMcl : this.linkedMcls) {
			if (thisMcl.getSrcVarId().equals(this.tgtVarId)) {
				if (!pDoneMcls.contains(thisMcl)) {
					//Match on source
					result.add(thisMcl);
				}
			} else if (thisMcl.getTgtVarId().equals(this.tgtVarId)) {
				if (!pDoneMcls.contains(thisMcl)) {
					//Match on target
					result.add(thisMcl);
				}
			}
		}

		return result;
	}

	public void addLinkedMcl(MatchedClauseList pMcl) {
		if (!this.linkedMcls.contains(pMcl)) {
			this.linkedMcls.add(pMcl);
			pMcl.addLinkedMcl(this);
		}
	}

	public TreeMap<String, String> getSourceVals() {
		return this.sourceVals;
	}

	public TreeMap<String, String> getTargetVals() {
		return this.targetVals;
	}

	public boolean matches(MatchedClauseList pMcl) {
		boolean result = false;

		if (this.equals(pMcl)) {
			result = false;
		} else {
			result = (pMcl.getSrcVarId().equals(this.srcVarId)) ||
					(pMcl.getSrcVarId().equals(this.tgtVarId)) ||
					(pMcl.getTgtVarId().equals(this.srcVarId)) ||
					(pMcl.getTgtVarId().equals(this.tgtVarId));
		}

		return result;
	}
	
//	public String debugString() {
//		String result = "";
//		String NL = StaticFunctionsFile.NL;
//		
//		result += "srcVarId: '" + this.srcVarId + "'" + NL;
//		result += "tgtVarId: '" + this.tgtVarId + "'" + NL;
//		result += "propertyNamw: '" + this.propertyName + "'" + NL;
//		result += "matchedPairs: '" + this.matchedPairs.toString() + "'" + NL;
//		result += "sourceVals: '" + this.sourceVals.toString() + "'" + NL;
//		result += "targetVals: '" + this.targetVals.toString() + "'" + NL + NL;
//		
//		return result;
//	}
	
	public void calculateSourceAndTargetVals() {
		this.sourceVals = new TreeMap<String, String>();		
		this.targetVals = new TreeMap<String, String>();

		for(ArrayList<String> thisPair : this.matchedPairs.values()) {
			String srcVal = thisPair.get(0);
			String tgtVal = thisPair.get(1);
			
			this.sourceVals.put(srcVal, srcVal);
			this.targetVals.put(tgtVal, tgtVal);
		}
	}
	
	public ArrayList<String> computeSourceValues() {
		ArrayList<String> result = new ArrayList<String>();
		
		for(ArrayList<String> thisPair : this.matchedPairs.values()) {
			String srcVal = thisPair.get(0);
			
			result.add(srcVal);
		}
		
		return result;
	}

	public ArrayList<String> computeTargetValues() {
		ArrayList<String> result = new ArrayList<String>();
		
		for(ArrayList<String> thisPair : this.matchedPairs.values()) {
			String tgtVal = thisPair.get(1);
			
			result.add(tgtVal);
		}
		
		return result;
	}

	public int eliminateNonMatchingPairs() {
		int result = 0;
		
		for (MatchedClauseList relMcl : this.linkedMcls) {
			//Check the source variable
			if (this.srcVarId.equals(relMcl.getSrcVarId())) {
				//Source to source match
				result = eliminateNonMatchingSourceToSource(relMcl);
			} else if (this.srcVarId.equals(relMcl.getTgtVarId())) {
				//Source to target match
				result = eliminateNonMatchingSourceToTarget(relMcl);
			}
			
			//Check the target variable
			if (this.tgtVarId.equals(relMcl.getSrcVarId())) {
				//Target to source match
				result = eliminateNonMatchingTargetToSource(relMcl);
			} else if (this.tgtVarId.equals(relMcl.getTgtVarId())) {
				//Target to target match
				result = eliminateNonMatchingTargetToTarget(relMcl);
			}
		}
		
		return result;
	}
	
	private int eliminateNonMatchingSourceToTarget(MatchedClauseList pMcl) {
		TreeMap<String, String> relTargetList = pMcl.getTargetVals();
		
		return eliminate(this.sourceVals, relTargetList, 0);
	}
	
	private int eliminateNonMatchingSourceToSource(MatchedClauseList pMcl) {
		TreeMap<String, String> relSourceList = pMcl.getSourceVals();
		
		return eliminate(this.sourceVals, relSourceList, 0);
	}
	
	private int eliminateNonMatchingTargetToSource(MatchedClauseList pMcl) {
		TreeMap<String, String> relSourceList = pMcl.getSourceVals();
		
		return eliminate(this.targetVals, relSourceList, 1);
	}

	private int eliminateNonMatchingTargetToTarget(MatchedClauseList pMcl) {
		TreeMap<String, String> relTargetList = pMcl.getTargetVals();
		
		return eliminate(this.targetVals, relTargetList, 1);
	}

	private int eliminate(TreeMap<String, String> pSrcVals, TreeMap<String, String> pTgtVals, int pIndex) {
		TreeMap<String, String> eliminationList = new TreeMap<String, String>();
		TreeMap<String, ArrayList<String>> uneliminatedPairs = new TreeMap<String, ArrayList<String>>();
		int counter = 0;
		
		for (String srcVal : pSrcVals.values()) {
			String tgtVal = pTgtVals.get(srcVal);
			if (tgtVal == null) {
				eliminationList.put(srcVal, srcVal);
			}
			counter++;
		}
		
		for (String tgtVal : pTgtVals.values()) {
			String srcVal = pSrcVals.get(tgtVal);
			if (srcVal == null) {
				eliminationList.put(tgtVal, tgtVal);
			}
			counter++;
		}

		for (ArrayList<String> thisPair : this.matchedPairs.values()) {
			String thisSource = thisPair.get(pIndex);
			if (eliminationList.get(thisSource) == null) {
				uneliminatedPairs.put(calculateMpKeyFor(thisPair), thisPair);
			}
			counter++;
		}

		this.matchedPairs = uneliminatedPairs;
		
		//Now rebuild the source and target vals based on the new list of matched pairs
		calculateSourceAndTargetVals();
		
		return counter;
	}
	
	public ArrayList<TreeMap<String, String>> processPairs(ArrayList<String> pHdrs) {
		ArrayList<TreeMap<String, String>> result = new ArrayList<TreeMap<String, String>>();
		
		for (ArrayList<String> thisPair : this.matchedPairs.values()) {
			TreeMap<String, String> thisRow = new TreeMap<String, String>();
			String thisSrcVal = thisPair.get(0);
			String thisTgtVal = thisPair.get(1);
			
			for (String thisHdr : pHdrs) {
				if (thisHdr.equals(this.srcVarId)) {
					//The source variable matches this header
					thisRow.put(thisHdr, thisSrcVal);
				} else if (thisHdr.equals(this.tgtVarId)) {
					//The target variable matches this header
					thisRow.put(thisHdr, thisTgtVal);
				} else {
					//Nothing matches this header
					thisRow.put(thisHdr, "");
				}
			}
			
			result.add(thisRow);
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		String result = "";
		String mpCount = "";
		
		if(this.matchedPairs != null) {
			mpCount = Integer.toString(this.matchedPairs.size());
		} else {
			mpCount = "[null]";
		}
		
		//Improve this check for existence / normal
		if (isExistenceMcl()) {
//			result = "Existence clause - " + "sourceVar='" + this.srcVarId + "' (count=" + mpCount + ")";
			result = "(e) " + this.srcVarId + " (count=" + mpCount + ")";
		} else {
//			result = "Normal clause - " + "sourceVar='" + this.srcVarId + "':propName='" + this.propertyName + "':targetVar'" + this.tgtVarId + "' (count=" + mpCount + ")";
			result = this.srcVarId + ":" + this.propertyName + ":" + this.tgtVarId + " (count=" + mpCount + ")";
		}
		
		return result;
	}
	
	public boolean isExistenceMcl() {
		//This is an existence MCL if the property name is empty
		return this.propertyName.isEmpty();
	}
	
	public boolean isUnlinkedExistenceMcl() {
		return isExistenceMcl() && this.linkedMcls.isEmpty();
	}
	
	public boolean isNormalMcl() {
		return !isExistenceMcl() && !isSpecialOperatorMcl() && !isDoubleVariableMcl() && !hasSpecialPropertyName();
	}
	
	public boolean hasSpecialPropertyName() {
		return CeSpecialProperty.isSpecialUniversalOperator(this.propertyName);
	}

	public boolean containsSourceId(String pId) {
		boolean result = false;
		
		for (ArrayList<String> thisPair : this.matchedPairs.values()) {
			if (!result) {
				result = thisPair.get(0).equals(pId);

				if (result) {
					break;
				}
			}
		}
		
		return result;
	}
	
	public boolean containsTargetId(String pId) {
		boolean result = false;
		
		for (ArrayList<String> thisPair : this.matchedPairs.values()) {
			if (!result) {
				result = thisPair.get(1).equals(pId);
				
				if (result) {
					break;
				}
			}
		}
		
		return result;
	}
	
	public boolean hasVariables(String pSrcVar, String pTgtVar) {
		//if the source and target variables match those passed in then this MCL has those variables
		return (this.srcVarId.equals(pSrcVar) && this.tgtVarId.equals(pTgtVar));
	}
	
	public void markAsSpecialOperatorMcl() {
		this.specialOperatorMcl = true;
	}
	
	public void markAsDoubleVariable() {
		this.doubleVariableMcl = true;
	}

	public boolean isSpecialOperatorMcl() {
		return this.specialOperatorMcl;
	}
	
	public boolean isSimpleSpecialOperatorMcl() {
		return isSpecialOperatorMcl() && !isDoubleVariableMcl();
	}
	
	public boolean isDoubleVariableMcl() {
		return this.doubleVariableMcl;
	}

	public ArrayList<CeInstance> getSourceInstances(ActionContext pAc) {
		return getInstancesForIndex(pAc, 0);
	}
	
	public ArrayList<CeInstance> getTargetInstances(ActionContext pAc) {
		return getInstancesForIndex(pAc, 1);
	}
	
	private ArrayList<CeInstance> getInstancesForIndex(ActionContext pAc, int pIndex) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		
		for (ArrayList<String> thisPair : this.matchedPairs.values()) {
			String srcId = thisPair.get(pIndex);
			
			CeInstance thisInst = pAc.getModelBuilder().getInstanceNamed(pAc, srcId);
			
			if (thisInst != null) {
				result.add(thisInst);
			}
		}
		
		return result;
	}
	
	public String getLinkedSourceVariableForSpecialOperatorSource() {
		String result = "";
		
		for (MatchedClauseList thisLm : this.linkedMcls) {
			if (thisLm.getTgtVarId().equals(getSrcVarId())) {
				result = thisLm.getSrcVarId();
			}
		}
		
		return result;
	}
	
	public String getLinkedSourceVariableForSpecialOperatorTarget() {
		String result = "";
		
		for (MatchedClauseList thisLm : this.linkedMcls) {
			if (thisLm.getTgtVarId().equals(getTgtVarId())) {
				result = thisLm.getSrcVarId();
			}
		}
		
		return result;
	}
	
	public ArrayList<ArrayList<String>> rowsMatchingSourceValue(String pSrcValue) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		for (ArrayList<String> thisPair : this.matchedPairs.values()) {
			if (thisPair.get(0).equals(pSrcValue)) {
				//DSB 24/09/2013 - Only add the row if the target is not empty
				if (!thisPair.get(1).isEmpty()) {
					result.add(thisPair);
				}
			}
		}
		
		return result;
	}

	public ArrayList<ArrayList<String>> rowsMatchingTargetValue(String pSrcValue) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		for (ArrayList<String> thisPair : this.matchedPairs.values()) {
			if (thisPair.get(1).equals(pSrcValue)) {
				//DSB 24/09/2013 - Only add the row if the source is not empty
				if (!thisPair.get(0).isEmpty()) {
					result.add(thisPair);
				}
			}
		}
		
		return result;
	}

	public void getAllLinkedMcls(HashSet<MatchedClauseList> pList) {
		if (!pList.contains(this)) {
			pList.add(this);
		}

		for (MatchedClauseList thisMcl : getLinkedMcls()) {
			if (!pList.contains(thisMcl)) {
				pList.add(thisMcl);
				thisMcl.getAllLinkedMcls(pList);
			}
		}
	}

	protected TreeSet<String> calculateAllLinkedVarIds() {
		TreeSet<String> result = new TreeSet<String>();
		HashSet<MatchedClauseList> allMcls = new HashSet<MatchedClauseList>();
		
		getAllLinkedMcls(allMcls);
		
		for (MatchedClauseList thisMcl : allMcls) {
//			if (!thisMcl.isSpecialOperatorMcl()) {
				if (!thisMcl.getSrcVarId().isEmpty()) {
					result.add(thisMcl.getSrcVarId());
				}
				if (!thisMcl.getTgtVarId().isEmpty()) {
					result.add(thisMcl.getTgtVarId());
				}
//			}
		}
		
		return result;
	}
	
	public TreeMap<String, ArrayList<String>> getSrcToTgtList() {
		return this.srcToTgtList;
	}
	
	public void setSrcToTgtList(TreeMap<String, ArrayList<String>> pList) {
		this.srcToTgtList = pList;
	}

	public TreeMap<String, ArrayList<String>> getTgtToSrcList() {
		return this.tgtToSrcList;
	}
	
	public void setTgtToSrcList(TreeMap<String, ArrayList<String>> pList) {
		this.tgtToSrcList = pList;
	}

}
