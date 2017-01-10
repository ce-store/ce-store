package com.ibm.ets.ita.ce.store.query;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ResultSetWrapper {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private HashSet<HashMap<String, String>> rs = null;
	private HashSet<HashMap<String, String>> newRows = null;
	
	public ResultSetWrapper() {
		this.rs = new HashSet<HashMap<String, String>>();
		this.newRows = new HashSet<HashMap<String, String>>();
	}

	private void rowCreate(HashMap<String, String> pRow) {
		this.rs.add(pRow);
	}

	private void rowAddPair(HashMap<String, String> pRow, String pKey, String pVal) {
		pRow.put(pKey,  pVal);
	}

	public ArrayList<HashMap<String, String>> listCompleteRows(int pNumVars) {
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

		for (HashMap<String, String> thisRow : this.rs) {
			if (thisRow.keySet().size() == pNumVars) {
				result.add(thisRow);
			}
		}

		return result;
	}
	
	private HashMap<String, String> duplicateRow(HashMap<String, String> pRow) {
		return new HashMap<String, String>(pRow);
	}
	
	public boolean processingForAdd(String pSrcVarId, String pSrcVal, String pTgtVarId, String pTgtVal, boolean pFoundMatch) {
		boolean foundMatch = pFoundMatch;

		foundMatch = processAdd(foundMatch, this.rs, pSrcVarId, pSrcVal, pTgtVarId, pTgtVal);

		return foundMatch;
	}

	private boolean processAdd(boolean pFoundMatch, HashSet<HashMap<String, String>> pRows, String pSrcVarId, String pSrcVal, String pTgtVarId, String pTgtVal) {
		boolean foundMatch = pFoundMatch;

		for (HashMap<String, String> thisRow : this.rs) {
			boolean srcFound = false;
			boolean srcMatched = false;
			boolean tgtFound = false;
			boolean tgtMatched = false;

			String existingSrcVal = thisRow.get(pSrcVarId);
			if (existingSrcVal != null) {
				srcFound = true;
				if (existingSrcVal.equals(pSrcVal)) {
					srcMatched = true;
				}
			}

			String existingTgtVal = thisRow.get(pTgtVarId);
			if (existingTgtVal != null) {
				tgtFound = true;
				if (existingTgtVal.equals(pTgtVal)) {
					tgtMatched = true;
				}
			}

			//Source matches exactly, but no target is found
			if (srcMatched && !tgtFound) {
				foundMatch = true;
				//Add the target to the row
				rowAddPair(thisRow, pTgtVarId, pTgtVal);
			}

			//Target matches exactly, but no source is found
			if (tgtMatched && !srcFound) {
				foundMatch = true;
				//Add the source to the row
				rowAddPair(thisRow, pSrcVarId, pSrcVal);
			}
			
//			//Neither source nor target are found
//			if (!srcFound && !tgtFound) {
//				foundMatch = true;
//				rowAddPair(thisRow, pSrcVarId, pSrcVal);
//				rowAddPair(thisRow, pTgtVarId, pTgtVal);
//			}

			//Both source and target are matched
			if (srcMatched && tgtMatched) {
				//Nothing is needed - but mark this as a match
				foundMatch = true;
			}
		}

		return foundMatch;
	}
	
	public boolean processingForDuplicate(boolean pFoundMatch, String pSrcVarId, String pSrcVal, String pTgtVarId, String pTgtVal) {
		boolean foundMatch = pFoundMatch;

		foundMatch = processDuplicate(foundMatch, this.rs, pSrcVarId, pSrcVal, pTgtVarId, pTgtVal);

		return foundMatch;
	}
	
	private boolean processDuplicate(boolean pFoundMatch, HashSet<HashMap<String, String>> pRows, String pSrcVarId, String pSrcVal, String pTgtVarId, String pTgtVal) {
		boolean foundMatch = pFoundMatch;

		for (HashMap<String, String> thisRow : this.rs) {
			boolean srcFound = false;
			boolean srcMatched = false;
			boolean tgtFound = false;
			boolean tgtMatched = false;

			String existingSrcVal = thisRow.get(pSrcVarId);
			if (existingSrcVal != null) {
				srcFound = true;
				if (existingSrcVal.equals(pSrcVal)) {
					srcMatched = true;
				}
			}

			String existingTgtVal = thisRow.get(pTgtVarId);
			if (existingTgtVal != null) {
				tgtFound = true;
				if (existingTgtVal.equals(pTgtVal)) {
					tgtMatched = true;
				}
			}

			if (srcMatched && tgtMatched) {
				//Nothing is needed - an existing match
				foundMatch = true;
			} else if (!srcMatched && !tgtMatched) {
				//Nothing is needed - unexpected case where neither match
			} else {
				if (srcFound && !srcMatched) {
					//Source found but not matched
					rowAddPair(thisRow, pTgtVarId, pTgtVal);

					HashMap<String, String> newRow = duplicateRow(thisRow);
					rowAddPair(newRow, pSrcVarId, pSrcVal);
					this.newRows.add(newRow);
					foundMatch = true;
				} else if (tgtFound && !tgtMatched) {
					//Target found but not matched
					rowAddPair(thisRow, pSrcVarId, pSrcVal);

					HashMap<String, String> newRow = duplicateRow(thisRow);
					rowAddPair(newRow, pTgtVarId, pTgtVal);
					this.newRows.add(newRow);
					foundMatch = true;
				} else {
					//Not expected to get here
				}
			}
		}
		
		return foundMatch;
	}

	public void saveNewRows() {
		if (!this.newRows.isEmpty()) {
			this.rs.addAll(this.newRows);

			this.newRows = new HashSet<HashMap<String, String>>();
		}
	}
	
	public boolean isEmpty() {
		return this.rs.isEmpty();
	}
	
	public void saveTheseRows(HashSet<HashMap<String, String>> pRows) {
		this.rs.addAll(pRows);
	}

	public void processOneSidedClause(String pKey, String pVal) {
		HashMap<String, String> thisRow = new HashMap<String, String>();

		thisRow.put(pKey, pVal);
		rowCreate(thisRow);
	}

	public void processSimpleRowAdd(String pKey1, String pVal1, String pKey2, String pVal2) {
		HashMap<String, String> thisRow = new HashMap<String, String>();

		thisRow.put(pKey1, pVal1);
		thisRow.put(pKey2, pVal2);
		rowCreate(thisRow);
	}

}
