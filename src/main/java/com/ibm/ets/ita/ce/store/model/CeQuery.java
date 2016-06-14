package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.NL;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.container.ContainerQueryResult;

public class CeQuery extends CeModelEntity {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public static final String RULENAME_START = "[";
	public static final String RULENAME_END = "]";

	private static AtomicLong patternIdVal = new AtomicLong(0);

	private String ceText = "";
	private int type = -1;
	private ArrayList<String> allVariableIds = new ArrayList<String>();
	private TreeMap<String, String> allVariableTypes = new TreeMap<String, String>();
	private ArrayList<String> responseVariableIds = new ArrayList<String>();
	private ArrayList<CeClause> directPremiseClauses = new ArrayList<CeClause>();
	private ArrayList<String> orderTokens = new ArrayList<String>();	//DSB 01/05/2015 #1095
	private boolean orderAscending = true;		//DSB 01/05/2015 #1095
	protected int lastSeqId = 0;
	private boolean hasErrors = false;
	private int rowLimit = -1;

	private static final int TYPE_NORMAL = 1;
	private static final int TYPE_COUNT = 2;

	protected CeQuery(String pCe, String pName) {
		//This is protected to ensure that new instances can only be created via the various static methods
		this.name = pName;
		this.ceText = pCe;
	}

	public static void resetCounter() {
		patternIdVal = new AtomicLong(0);
	}

	public static CeQuery createNew(String pQueryCe, String pQueryName) {
		return new CeQuery(pQueryCe, pQueryName);
	}

	public static long nextPatternId() {
		return patternIdVal.incrementAndGet();
	}

	private static void addClauseConcepts(CeClause pClause, ArrayList<CeConcept> pResult) {
		if (!pResult.contains(pClause.getTargetConcept())) {
			pResult.add(pClause.getTargetConcept());
		}
		for (CeConcept thisConcept : pClause.getSecondaryConcepts()) {
			if (!pResult.contains(thisConcept)) {
				pResult.add(thisConcept);
			}
		}
	}

	private static void addClauseProperties(CeClause pClause, ArrayList<CeProperty> pResult) {
		for (CePropertyInstance thisPi : pClause.getAllProperties()) {
			CeProperty thisProp = thisPi.getRelatedProperty();

			if (thisProp != null) {
				if (!pResult.contains(thisProp)) {
					pResult.add(thisProp);
				}
			}
		}
	}

	@SuppressWarnings("static-method")
	public boolean isRule() {
		//This must not become a static method
		return false;
	}

	public String getQueryName() {
		return this.name;
	}

	public String getCeText() {
		return this.ceText;
	}

	public int getQueryType() {
		return this.type;
	}
	
	public boolean isCountQuery() {
		return (this.type == TYPE_COUNT);
	}

	public boolean isNormalQuery() {
		return (this.type == TYPE_NORMAL);
	}

	public void markAsCountQuery() {
		this.type = TYPE_COUNT;
	}

	public void markAsNormalQuery() {
		this.type = TYPE_NORMAL;
	}

	public ArrayList<String> getAllVariableIds() {
		return this.allVariableIds;
	}

	public TreeMap<String, String> getAllVariableTypes() {
		return this.allVariableTypes;
	}

	public void addAllVariableId(String pVarId, String pType) {
		if (pVarId != null) {
			if (!this.allVariableIds.contains(pVarId)) {
				this.allVariableIds.add(pVarId);
				this.allVariableTypes.put(pVarId, pType);
			}
	
			//Also add to the response variable id list if this is a rule
			if (isRule()) {
				addResponseVariableId(pVarId);
			}
		}
	}
	
	public String getTypeForHeader(String pVarId) {
		return this.allVariableTypes.get(pVarId);
	}

	public ArrayList<String> getResponseVariableIds() {
		return this.responseVariableIds;
	}

	public void addResponseVariableId(String pVarId) {
		if (!this.responseVariableIds.contains(pVarId)) {
			this.responseVariableIds.add(pVarId);
		}
	}

	public ArrayList<CeClause> getDirectPremiseClauses() {
		return this.directPremiseClauses;
	}

	public void addDirectPremiseClause(CeClause pClause) {
		this.directPremiseClauses.add(pClause);
	}

	public ArrayList<CeClause> getAllDirectClauses() {
		return getDirectPremiseClauses();
	}

	//DSB 01/05/2015 #1095
	public ArrayList<String> getOrderTokens() {
		return this.orderTokens;
	}

	//DSB 01/05/2015 #1095
	public void addOrderToken(String pToken) {
		this.orderTokens.add(pToken);
	}
	
	//DSB 01/05/2015 #1095
	public boolean needsSorting() {
		return !this.orderTokens.isEmpty();
	}

	//DSB 01/05/2015 #1095
	public boolean isSortOrderAscending() {
		return this.orderAscending;
	}

	//DSB 01/05/2015 #1095
	public void setSortOrderDescending() {
		this.orderAscending = false;
	}

	public boolean hasRowLimit() {
		return this.rowLimit != -1;
	}

	public int getRowLimit() {
		return this.rowLimit;
	}

	public void setRowLimit(String pLimit) {
		try {
			int intVal = new Integer(pLimit).intValue();

			if (intVal > 0) {
				this.rowLimit = intVal;
			}
		} catch(NumberFormatException e) {
			//Nothing needed here - just ignore
		}
	}

	public ArrayList<CeClause> getAllConcatenationClauses() {
		ArrayList<CeClause> result = new ArrayList<CeClause>();

		for (CeClause outerClause : getAllDirectClauses()) {
			for (CeClause thisClause : outerClause.getChildClauses()) {
				if (thisClause.isConcatenationClause()) {
					result.add(thisClause);
				}
			}
		}

		return result;
	}

	public ArrayList<CeConcept> getAllReferencedPremiseConcepts() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

		for (CeClause outerClause : getDirectPremiseClauses()) {
			for (CeClause innerClause : outerClause.getChildClauses()) {
				addClauseConcepts(innerClause, result);
			}
			addClauseConcepts(outerClause, result);
		}

		return result;
	}

	public ArrayList<CeProperty> getAllReferencedPremiseProperties() {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		for (CeClause outerClause : getDirectPremiseClauses()) {
			for (CeClause innerClause : outerClause.getChildClauses()) {
				addClauseProperties(innerClause, result);
			}
			addClauseProperties(outerClause, result);
		}

		return result;
	}

	public int getLastSeqId() {
		return this.lastSeqId;
	}

	protected String calcNextSeqId() {
		return Integer.toString(++this.lastSeqId);
	}

	public ArrayList<CeClause> listAllChildPremiseClauses() {
		ArrayList<CeClause> result = new ArrayList<CeClause>();

		for (CeClause directClause : getDirectPremiseClauses()) {
			result.addAll(directClause.getChildClauses());
		}

		return result;
	}

	public void calculateAllVariableIds() {
		for (CeClause thisClause : getDirectPremiseClauses()) {
			if (thisClause.isSimpleClause()) {
				addAllVariableId(thisClause.getTargetVariable(), "O");
			} else {
				for (CePropertyInstance thisPi : thisClause.getAllProperties()) {
					if (thisPi.getClauseVariableId() != null) {
						addAllVariableId(thisPi.getClauseVariableId(), "O");
					}

					if (!thisPi.isSpecialOperatorPropertyInstance()) {
						for (String thisVarId : thisPi.getValueList()) {
							if (thisPi.getRelatedProperty().isObjectProperty()) {
								addAllVariableId(thisVarId, "O");
							} else {
								addAllVariableId(thisVarId, "D");
							}
						}
					}
				}
			}
		}
	}

	public boolean isIncludedVarId(String pVarId) {
		return getResponseVariableIds().contains(pVarId);
	}

	@Override
	public String toString() {
		String result = "";

		result = "CeQuery (" + this.ceText + "):" + NL;
		result += "  Premises:" + NL;
		for (CeClause thisClause : listAllChildPremiseClauses()) {
			result += "    " + thisClause.toString() + NL;
		}

		return result;
	}

	@Override
	public HashSet<CeSentence> listAllSentences() {
		HashSet<CeSentence> result = new HashSet<CeSentence>();

		for (CeSentence priSen : getPrimarySentences()) {
			result.add(priSen);
		}

		return result;
	}

	@Override
	public ArrayList<CeSentence> listSecondarySentences() {
		return new ArrayList<CeSentence>();
	}

	@Override
	public ArrayList<ArrayList<CeSentence>> listAllSentencesAsPair() {
		ArrayList<ArrayList<CeSentence>> senListPair = new ArrayList<ArrayList<CeSentence>>();

		senListPair.add(listPrimarySentences());
		senListPair.add(listSecondarySentences());

		return senListPair;
	}

	public boolean hasNegatedHeader(ActionContext pAc, String pHdr) {
		boolean result = false;

		for (CeClause thisClause : getDirectPremiseClauses()) {
			for (CePropertyInstance thisPi : thisClause.getAllProperties()) {
				String propVal = thisPi.getSingleOrFirstValue();
				if (propVal.equals(pHdr)) {
					result = thisPi.isNegated(pAc);
				}
			}
		}

		return result;
	}

	public boolean hasErrors() {
		return this.hasErrors;
	}

	public void markAsHavingErrors() {
		this.hasErrors = true;
	}

	public void testIntegrity(ActionContext pAc) {
		//Test for duplicate premise clauses
		HashSet<String> clauseTexts = new HashSet<String>();

		for (CeClause thisClause : this.directPremiseClauses) {
			String thisClauseText = thisClause.calculateRawText();

			if (clauseTexts.contains(thisClauseText)) {
				reportWarning("Duplicate premise clause detected in query/rule '" + this.getQueryName() + "' (" + thisClauseText + ")", pAc);
			} else {
				clauseTexts.add(thisClauseText);
			}
		}
	}

	public boolean isDatabaseBacked(ActionContext pAc) {
		boolean result = false;

		for (CeConcept thisCon : this.getAllReferencedPremiseConcepts()) {
			CeInstance mdInst = thisCon.retrieveMetadataInstance(pAc);

			if (mdInst != null) {
				//TODO: Remove hardcoded name and also raise to higher general database concept
				result = mdInst.isConceptNamed(pAc, "gaian concept");
			}
		}

		return result;
	}

	public String calculateSql(ActionContext pAc) {
		String result = "";

		for (CeConcept thisCon : this.getAllReferencedPremiseConcepts()) {
			CeInstance mdInst = thisCon.retrieveMetadataInstance(pAc);

			if (mdInst != null) {
				//TODO: Remove hardcoded name and also raise to higher general database concept
				if (mdInst.isConceptNamed(pAc, "gaian concept")) {
					//TODO: Remove this hardcoded value
					String tablename = mdInst.getSingleValueFromPropertyNamed("table name");
					result += "select * from triple_store_s where body_type = '" + tablename + "'";
				}
			}
		}

		return result;
	}

	public static boolean isCountHeader(String pHdr) {
		return pHdr.startsWith(ContainerQueryResult.COUNT_INDICATOR);
	}

	public static boolean isSumHeader(String pHdr) {
		return pHdr.startsWith(ContainerQueryResult.SUM_INDICATOR);
	}

	public boolean hasCountHeader() {
		boolean result = false;

		for (String thisHdr : this.responseVariableIds) {
			if (thisHdr.startsWith(ContainerQueryResult.COUNT_INDICATOR)) {
				result = true;
			}
		}

		return result;
	}

	public boolean hasSumHeader() {
		boolean result = false;

		for (String thisHdr : this.responseVariableIds) {
			if (thisHdr.startsWith(ContainerQueryResult.SUM_INDICATOR)) {
				result = true;
			}
		}

		return result;
	}

}