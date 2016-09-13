package com.ibm.ets.ita.ce.store.model.container;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.CESEN_SEPARATOR;
import static com.ibm.ets.ita.ce.store.names.MiscNames.HDR_CE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import java.util.regex.Pattern;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;

public class ContainerCeResult extends ContainerQueryResult {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private CeQuery targetQuery = null;
	private ArrayList<ArrayList<CeInstance>> instanceRows = new ArrayList<ArrayList<CeInstance>>();

	@Override
	public boolean isCeResult() {
		return true;
	}

	@Override
	public boolean isStatistics() {
		return false;
	}

	public ArrayList<ArrayList<CeInstance>> getInstanceRows() {
		return this.instanceRows;
	}

	public void addInstanceRow(ArrayList<CeInstance> pRow) {
		this.instanceRows.add(pRow);
	}

	public CeQuery getTargetQuery() {
		return this.targetQuery;
	}

	public void setTargetQuery(CeQuery pQuery) {
		this.targetQuery = pQuery;
	}

	public String getTargetQueryName() {
		String result = "";

		if (this.targetQuery != null) {
			result = this.targetQuery.getQueryName();
		} else {
			result = "";
		}

		return result;
	}

	public void populateInstances(ActionContext pAc) {
		ModelBuilder mb = pAc.getModelBuilder();

		for (ArrayList<String> thisRow : getResultRows()) {
			ArrayList<CeInstance> instRow = new ArrayList<CeInstance>();

			for (int i = 0; i < thisRow.size(); i++) {
				String instName = thisRow.get(i);
				String hdrName = getHeaders().get(i);

				if (!hdrName.equals(HDR_CE)) {
					CeInstance thisInst = mb.getInstanceNamed(pAc, instName);

					if (thisInst != null) {
						instRow.add(thisInst);
					} else {
						reportError("Unable to locate instance named '" + instName + "' during CE Query processing", pAc);
					}
				}
			}

			addInstanceRow(instRow);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		int ctr = 0;

		if ((this.instanceRows != null) && (!this.instanceRows.isEmpty())) {		
			for (ArrayList<CeInstance> thisInstRow : this.instanceRows) {
				ctr = 0;
				for (CeInstance thisInst : thisInstRow) {
					result.append(getHeaders().get(ctr++) + "=");
					result.append(thisInst.toString() + " ");
				}
				result.append(NL);
			}
		} else {
			result.append("ResultRows=" + this.resultRows.size());
		}

		return result.toString();
	}

	public String getAllGeneratedCeText() {
		StringBuilder sb = new StringBuilder();

		for (String thisCe : getCeResults()) {
			appendToSb(sb, thisCe);
		}

		return sb.toString();
	}

	public void trimToContainOnlyCe() {
		//Iterate through each row and remove everything except for the CE
		int ceIndex = getHeaders().indexOf(HDR_CE);

		for (ArrayList<String> thisRow : this.resultRows) {
			String thisCe = thisRow.get(ceIndex);

			thisRow.clear();
			thisRow.add(thisCe);
		}

		ArrayList<String> hdrs = getHeaders();
		hdrs.clear();
		hdrs.add(HDR_CE);

		ArrayList<String> types = getTypes();
		types.clear();
		types.add(HDR_CE);
	}

	public void trimToRemoveCe() {
		//Iterate through each row and remove the CE value
		int ceIndex = getHeaders().indexOf(HDR_CE);

		for (ArrayList<String> thisRow : this.resultRows) {
			thisRow.remove(ceIndex);
		}

		ArrayList<String> hdrs = getHeaders();
		hdrs.remove(HDR_CE);
	}

	public ArrayList<String> getCeResults() {
		ArrayList<String> result = new ArrayList<String>();

		int ceHdrIndex = getHeaders().indexOf(HDR_CE);

		for (ArrayList<String> thisRow : this.resultRows) {
			result.add(thisRow.get(ceHdrIndex));
		}

		return result;
	}

	public ArrayList<CeInstance> getColumnOfInstancesFor(ActionContext pAc, String pColName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		int hdrIndex = getIndexForHeader(pColName);

		if (hdrIndex > -1) {
			for (ArrayList<CeInstance> thisRow : getInstanceRows()) {
				CeInstance thisInst = thisRow.get(hdrIndex);
				result.add(thisInst);
			}
		} else {
			reportError("No such column '" + pColName + "' in result set", pAc);
		}

		return result;
	}

	@Override
	public long getCreationDate() {
		long result = 0;

		if (this.targetQuery != null) {
			result = this.targetQuery.getCreationDate();
		} else {
			result = timestampNow();
		}

		return result;
	}

	public static boolean isMultipleSentences(String pSenText) {
		return pSenText.startsWith(CESEN_SEPARATOR);
	}

	public static String[] splitSentences(String pSenText) {
		return pSenText.split(Pattern.quote(CESEN_SEPARATOR));
	}
	
	public static String appendAdditionalSentence(String pOriginalCe, String pNewCe) {
		String result = "";

		if (!pNewCe.isEmpty()) {
			if (!pOriginalCe.isEmpty()) {
				//First ensure that any original CE has the marker prepended
				if (!pOriginalCe.startsWith(CESEN_SEPARATOR)) {
					result = CESEN_SEPARATOR + pOriginalCe;
				} else {
					result = pOriginalCe;
				}

				//Now concatenate the additional CE
				result += CESEN_SEPARATOR + pNewCe;
			} else {
				//The original CE is empty so just return the new CE
				result = pNewCe;
			}
		} else {
			//The new CE is empty so just return the original
			result = pOriginalCe;
		}

		return result;
	}

	public void trimToLimit(CeQuery pQuery) {
		if (pQuery.hasRowLimit()) {
			int limit = pQuery.getRowLimit();

			if (this.instanceRows.size() > limit) {
				this.instanceRows.subList(limit, this.instanceRows.size()).clear();
			}

			super.trimToLimit(pQuery);
		}
	}

	public void removeColumn(String pColName) {
		int colIdx = getIndexForHeader(pColName);

		if (colIdx > -1) {
			this.headers.remove(colIdx);
			this.types.remove(colIdx);

			for (ArrayList<String> thisRow : this.allRows) {
				thisRow.remove(colIdx);
			}

			if (this.allRows != this.resultRows) {
				for (ArrayList<String> thisRow : this.resultRows) {
					thisRow.remove(colIdx);
				}
			}

			for (ArrayList<CeInstance> thisRow : this.instanceRows) {
				thisRow.remove(colIdx);
			}

		}
	}

}
