package com.ibm.ets.ita.ce.store.query;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.NL;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeClause;
import com.ibm.ets.ita.ce.store.model.CeConcatenatedValue;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerQueryResult;

public class CeGeneratorConclusion {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CONNECTOR_THAT = "that";
	private static final String CONNECTOR_AND = "and";

	private ActionContext ac = null;
	private ContainerCeResult ceResult = null;
	private CeRule ceRule = null;

	private TreeMap<String, Integer> hdrIndexes = null;
	private TreeMap<String, String> uniqueTgtVars = null;
	private boolean generateRationale = true;
	private boolean doubleRationale = false;

	public CeGeneratorConclusion(ActionContext pAc, CeRule pRule, ContainerCeResult pResult, boolean pGenRationale, boolean pDoubleRationale) {
		this.ac = pAc;
		this.ceRule = pRule;
		this.ceResult = pResult;

		this.generateRationale = pGenRationale;
		this.doubleRationale = pDoubleRationale;
	}

	private static String ceNormalDefinition(CeConcept pCon, String pInstId) {
		String ceTemplate = "there is %CONQUAL% %CONCEPT% named '%ID%'";

		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%CONQUAL%", pCon.conceptQualifier());
		ceParms.put("%CONCEPT%", pCon.getConceptName());
		ceParms.put("%ID%", pInstId);

		return substituteCeParameters(ceTemplate, ceParms);
	}

	private static String ceSecondaryDefinition(String pMainConName, String pSecConName, String pInstId) {
		String ceTemplate = "the %MAIN_CON% '%ID%' is a %SEC_CON%";

		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%MAIN_CON%", pMainConName);
		ceParms.put("%SEC_CON%", pSecConName);
		ceParms.put("%ID%", pInstId);

		return substituteCeParameters(ceTemplate, ceParms);
	}

	private static String ceStartingDefinition(String pConName, String pInstId) {
		String ceTemplate = "the %CONCEPT% '%ID%'";

		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%CONCEPT%", pConName);
		ceParms.put("%ID%", pInstId);

		return substituteCeParameters(ceTemplate, ceParms);
	}

	private static String ceFnProperty(String pRange, String pValue, String pPropName) {
		String ceTemplate = "";

		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%VAL%", pValue);
		ceParms.put("%PROP%", pPropName);

		if (pRange.isEmpty()) {
			ceTemplate = "  has '%VAL%' as %PROP%";
		} else {
			ceParms.put("%RANGE%", pRange);
			ceTemplate = "  has %RANGE% '%VAL%' as %PROP%";
		}

		return substituteCeParameters(ceTemplate, ceParms);
	}

	private static String ceVsProperty(String pRange, String pValue, String pPropName) {
		String ceTemplate = "  %PROP% %RANGE% '%VAL%'";

		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%RANGE%", pRange);
		ceParms.put("%VAL%", pValue);
		ceParms.put("%PROP%", pPropName);

		return substituteCeParameters(ceTemplate, ceParms);
	}

	private static String calculateFormattedRange(CePropertyInstance pPi) {
		String formattedRange = "";
		String rName = pPi.getSingleOrFirstRangeName();
		
		if (rName != null) {
			if (rName != CeProperty.RANGE_VALUE) {
				formattedRange = "the " + pPi.getSingleOrFirstRangeName();
			}
		}
		
		return formattedRange;
	}

	public void doRuleCeProcessing() {
		calculateUniqueTargetVariables();

		initialiseHeaders();

		if (hasCountVariable()) {
			collapseRowsForCounting();
		} else if (hasSumVariable()) {
			collapseRowsForSumming();
		}

		//Calculate the correct CE results for the rule - based on the conclusion clauses
		for (ArrayList<String> queryRow : this.ceResult.getResultRows()) {
			CeConclusionRow thisRow = new CeConclusionRow(this, queryRow);
			String conclusionCe = "";

			for (String tgtVar : this.uniqueTgtVars.keySet()) {
				String newCe = generateRuleOutputCeForResultRow(tgtVar, thisRow);

				conclusionCe = ContainerCeResult.appendAdditionalSentence(conclusionCe, newCe);
			}

			replaceCeInResultRow(thisRow, conclusionCe);
		}

		if (this.ceResult.hasCountHeader() || this.ceResult.hasSumHeader()) {
			ArrayList<String> hdrsToRemove = new ArrayList<String>();

			//To suppress the rendering of non-relevant columns simply remove them from the hdrIndexes list
			for (String thisHdr : this.hdrIndexes.keySet()) {
				if ((!thisHdr.startsWith(ContainerQueryResult.COUNT_INDICATOR)) && (!thisHdr.startsWith(ContainerQueryResult.SUM_INDICATOR))) {
					if (!thisHdr.equals(ContainerCeResult.HDR_CE)) {
						if (!this.uniqueTgtVars.containsKey(thisHdr)) {
							hdrsToRemove.add(thisHdr);
						}
					}
				}
			}

			for (String thisHdr : hdrsToRemove) {
				this.hdrIndexes.remove(thisHdr);
				this.ceResult.removeColumn(thisHdr);
			}
		}
	}

	private boolean hasCountVariable() {
		boolean result = false;

		for (String thisHdr : this.ceResult.getHeaders()) {
			if (thisHdr.startsWith(ContainerQueryResult.COUNT_INDICATOR)) {
				result = true;
				break;
			}
		}

		return result;
	}

	private boolean hasSumVariable() {
		boolean result = false;

		for (String thisHdr : this.ceResult.getHeaders()) {
			if (thisHdr.startsWith(ContainerQueryResult.SUM_INDICATOR)) {
				result = true;
				break;
			}
		}

		return result;
	}

	private void collapseRowsForCounting() {
		TreeMap<String, ArrayList<String>> newRowMap = new TreeMap<String, ArrayList<String>>();
		TreeMap<String, Integer> countMap = new TreeMap<String, Integer>();
		int ceIndex = this.ceResult.getIndexForHeader("CE");
		int countIndex = -1;

		for (ArrayList<String> thisRow : this.ceResult.getResultRows()) {
			StringBuilder sbMain = new StringBuilder();

			for (String thisHdr : this.ceResult.getHeaders()) {
				int hdrIndex = this.ceResult.getIndexForHeader(thisHdr);

				if (thisHdr.startsWith(ContainerQueryResult.COUNT_INDICATOR)) {
					//This is the variable to be counted
					countIndex = hdrIndex;
				} else {
					if (!thisHdr.equals(ContainerCeResult.HDR_CE)) {
						if (this.uniqueTgtVars.containsKey(thisHdr)) {
							String thisVal = thisRow.get(hdrIndex);

							if (sbMain.length() > 0) {
								sbMain.append("|");
							}

							sbMain.append(thisVal);
						}
					}
				}
			}

			int rowCount = 0;
			String thisKey = sbMain.toString();
			StringBuilder sbRat = new StringBuilder();
			boolean newRowCreated = false;

			if (!newRowMap.containsKey(thisKey)) {
				newRowMap.put(thisKey, thisRow);
				newRowCreated = true;
			}

			ArrayList<String> newRow = newRowMap.get(thisKey);

			if (!newRowCreated) {
				sbRat.append(newRow.get(ceIndex));
				sbRat.append(" and\n");
			}

			sbRat.append(thisRow.get(ceIndex));

			newRow.set(ceIndex, sbRat.toString());

			if (countMap.containsKey(thisKey)) {
				rowCount = countMap.get(thisKey).intValue();
			}

			countMap.put(thisKey, new Integer(++rowCount));
		}

		for (String thisKey : newRowMap.keySet()) {
			ArrayList<String> thisRow = newRowMap.get(thisKey);

			thisRow.set(countIndex, countMap.get(thisKey).toString());
		}

		this.ceResult.replaceResultRowsWith(newRowMap.values());
	}

	private void collapseRowsForSumming() {
		TreeMap<String, ArrayList<String>> newRowMap = new TreeMap<String, ArrayList<String>>();
		TreeMap<String, Long> sumMap = new TreeMap<String, Long>();
		int ceIndex = this.ceResult.getIndexForHeader("CE");
		int sumIndex = -1;
		String sumText = null;
		long sumVal = 0;

		for (ArrayList<String> thisRow : this.ceResult.getResultRows()) {
			StringBuilder sbMain = new StringBuilder();

			for (String thisHdr : this.ceResult.getHeaders()) {
				int hdrIndex = this.ceResult.getIndexForHeader(thisHdr);

				if (thisHdr.startsWith(ContainerQueryResult.SUM_INDICATOR)) {
					//This is the variable to be summed
					sumIndex = hdrIndex;
					sumText = thisRow.get(sumIndex);

					try {
						sumVal = new Long(sumText).longValue();
					} catch (NumberFormatException e) {
						reportWarning("Unable to sum '" + sumText + "' during rule processing", this.ac);
					}
				} else {
					if (!thisHdr.equals(ContainerCeResult.HDR_CE)) {
						if (this.uniqueTgtVars.containsKey(thisHdr)) {
							String thisVal = thisRow.get(hdrIndex);

							if (sbMain.length() > 0) {
								sbMain.append("|");
							}

							sbMain.append(thisVal);
						}
					}
				}
			}

			long rowSum = 0;
			String thisKey = sbMain.toString();
			StringBuilder sbRat = new StringBuilder();
			boolean newRowCreated = false;

			if (!newRowMap.containsKey(thisKey)) {
				newRowMap.put(thisKey, thisRow);
				newRowCreated = true;
			}

			ArrayList<String> newRow = newRowMap.get(thisKey);

			if (!newRowCreated) {
				sbRat.append(newRow.get(ceIndex));
				sbRat.append(" and\n");
			}

			sbRat.append(thisRow.get(ceIndex));

			newRow.set(ceIndex, sbRat.toString());

			if (sumMap.containsKey(thisKey)) {
				rowSum = sumMap.get(thisKey).longValue();
			}

			sumMap.put(thisKey, new Long(rowSum + sumVal));
		}

		for (String thisKey : newRowMap.keySet()) {
			ArrayList<String> thisRow = newRowMap.get(thisKey);

			thisRow.set(sumIndex, sumMap.get(thisKey).toString());
		}

		this.ceResult.replaceResultRowsWith(newRowMap.values());
	}

	private void initialiseHeaders() {
		int ctr = 0;
		
		this.hdrIndexes = new TreeMap<String, Integer>();
		
		for (String thisHdr : this.ceResult.getHeaders()) {
			Integer idx = new Integer(ctr++);
			this.hdrIndexes.put(thisHdr, idx);
		}
	}
	
	private String generateRuleOutputCeForResultRow(String pTgtVar, CeConclusionRow pRow) {
		StringBuilder sb = new StringBuilder();

		processClauses(sb, pTgtVar, pRow);

		return sb.toString();
	}
	
	private void processClauses(StringBuilder pSb, String pTgtVar, CeConclusionRow pRow) {
		StringBuilder sb = new StringBuilder();
		String connectorText = "";
		boolean generatedStart = false;
		boolean splitSentence = false;
		CeConcept lastTgtCon = null;

		for (CeClause thisClause : getAllChildConclusionClauses()) {
			String clauseTv = getVariableStubFrom(thisClause.getTargetVariable());
			if (clauseTv.equals(pTgtVar)) {
				CeConcept thisTgtCon = thisClause.getTargetConcept();

				if ((lastTgtCon != null) && (!lastTgtCon.equalsOrHasParent(thisTgtCon))) {
					//The concept is not compatible with the previous so a new sentence must be started
					generateRationaleAndCloseSentence(sb, pRow);
					sb.append(ContainerCeResult.CESEN_SEPARATOR);
					connectorText = "";
					generatedStart = false;
					splitSentence = true;
				}

				lastTgtCon = thisTgtCon;

				if (thisClause.isSimpleClause()) {
					connectorText = generateCeConclusionForSimpleClause(sb, thisClause, pRow, connectorText);
					generatedStart = true;
				} else if (thisClause.isTargetConceptNegated()) {
					reportWarning("Rendering of conclusions for negated target concept not yet implemented", this.ac);
				} else {
					connectorText = generateCeConclusionForNormalClause(sb, thisClause, pRow, connectorText, generatedStart);
					generatedStart = true;
				}
			}
		}

		generateRationaleAndCloseSentence(sb, pRow);
		
		if (splitSentence) {
			pSb.append(ContainerCeResult.CESEN_SEPARATOR);
		}
		
		pSb.append(sb);
	}

	private String generateCeConclusionForSimpleClause(StringBuilder pSb, CeClause pClause, CeConclusionRow pRow, String pConnectorText) {
		String connectorText = pConnectorText;
		CeConcept tgtCon = pClause.getTargetConcept();
		
		if (tgtCon != null) {
			if (pClause.getSecondaryConceptsNormal().isEmpty()) {
				if (pClause.getConceptVariableTokens().isEmpty()) {
					if (pClause.getConcatenatedValues().isEmpty()) {
						String thisNewInstId = "";
						String tgtVar = pClause.getTargetVariable();
						if (pClause.targetVariableWasQuoted()) {
							thisNewInstId = tgtVar;
						} else {
							thisNewInstId = getValueForHeader(tgtVar, pRow);
						}
						String ceText = ceNormalDefinition(tgtCon, thisNewInstId);
						appendToSbNoNl(pSb, ceText);
						connectorText = " " + CONNECTOR_THAT;
					} else {
						String ceText = null;

						for (CeConcatenatedValue thisCv : pClause.getConcatenatedValues()) {
							String thisNewInstId = "";
							String tgtVar = pClause.getTargetVariable();
							if (pClause.targetVariableWasQuoted()) {
								thisNewInstId = tgtVar;
							} else {
								thisNewInstId = getValueForHeader(tgtVar, pRow);
							}

							appendToSbNoNl(pSb, ceStartingDefinition(tgtCon.getConceptName(), thisNewInstId));

							String concatVar = thisCv.getFirstVarName();
							String concatVal = thisCv.doConcatenationWith(concatVar, getValueForHeader(concatVar, pRow));
							ceText = ceFnProperty("the value", concatVal, thisCv.getName());
						}
						appendToSbNoNl(pSb, ceText);
						connectorText = " " + CONNECTOR_THAT;
					}
				} else {
					for (String thisCvt : pClause.getConceptVariableTokens()) {
						//A special "is a" with a variable
						String tgtInstId = getValueForHeader(pClause.getTargetVariable(), pRow);
						String tgtSecCon = getValueForHeader(trimHashFrom(thisCvt), pRow);
						String ceText = ceSecondaryDefinition(tgtCon.getConceptName(), tgtSecCon, tgtInstId);
						appendToSbNoNl(pSb, ceText);
					}
					connectorText = " " + CONNECTOR_AND;
				}
			} else {
				for (CeConcept thisSecCon : pClause.getSecondaryConceptsNormal()) {
					String tgtInstId = getValueForHeader(pClause.getTargetVariable(), pRow);
					String ceText = ceSecondaryDefinition(tgtCon.getConceptName(), thisSecCon.getConceptName(), tgtInstId);
					appendToSbNoNl(pSb, ceText);
				}
				connectorText = " " + CONNECTOR_AND;
			}
		} else {
			reportError("Unexpected null target concept in generateCeConclusionForSimpleClause()", this.ac);
		}
		
		return connectorText;
	}
	
	private String generateCeConclusionForNormalClause(StringBuilder pSb, CeClause pClause, CeConclusionRow pRow, String pConnectorText, boolean pGeneratedStart) {
		String connectorText = pConnectorText;
		boolean generatedStart = pGeneratedStart;
		
		for (CePropertyInstance thisPi : pClause.getAllProperties()) {			
			//Output the start of the CE sentence if necessary
			if (!generatedStart) {
				String tgtConName = thisPi.getRelatedProperty().getDomainConcept().getConceptName();
				String instId = null;

				//DSB 25/02/2015 - Added extra test to handle quoted target variables (i.e. constant values) in conclusions
				if (pClause.targetVariableWasQuoted()) {
					instId = pClause.getTargetVariable();
				} else {
					instId = calculateInstName(thisPi, pRow);
				}

				appendToSbNoNl(pSb, ceStartingDefinition(tgtConName, instId));
				generatedStart = true;
			}
			
			//Output (and set) the connector
			appendToSb(pSb, connectorText);
			connectorText = " " + CONNECTOR_AND;

			//Process the property
			appendToSbNoNl(pSb, generatePropertyCe(thisPi, pRow));
		}
		
		return connectorText;
	}
	
	private String generatePropertyCe(CePropertyInstance pPi, CeConclusionRow pRow) {
		String ceText = "";
		String piVal = calculatePiVal(pPi, pRow);
		String propName = pPi.getRelatedProperty().getPropertyName();
		String formattedRange = calculateFormattedRange(pPi);
		
		if (pPi.getRelatedProperty().isFunctionalNoun()) {
			ceText = ceFnProperty(formattedRange, piVal, propName);
		} else {
			ceText = ceVsProperty(formattedRange, piVal, propName);
		}
		
		return ceText;
	}
	
	private void generateRationaleAndCloseSentence(StringBuilder pSb, CeConclusionRow pRow) {
		if (pSb.length() > 0) {
			if (this.generateRationale) {
				
				//If rationale is being doubled insert a copy of the sentence so far at the beginning of the text
				if (this.doubleRationale) {
					pSb.insert(0, pSb.toString() + ".");
				}
				
				appendToSbNoNl(pSb, generateRationaleFor(pRow));
			} else {
				//Finish the CE sentence if no rationale is being generated
				appendToSb(pSb, ".");
			}
		}
	}
	
	private ArrayList<CeClause> getAllChildConclusionClauses() {
		ArrayList<CeClause> result = new ArrayList<CeClause>();
		
		for (CeClause dcClause : this.ceRule.getDirectConclusionClauses()) {
			result.addAll(dcClause.getChildClauses());
		}
		
		return result;
	}
	
	public String getValueForHeader(String pTgtVar, CeConclusionRow pRow) {
		String targetName = "";
		Integer tgtIdx = this.hdrIndexes.get(pTgtVar);

		if (tgtIdx != null) {
			targetName = pRow.getQueryRow(tgtIdx.intValue());
		} else {
			//This is not in the result so get it from the newInstances list instead
			targetName = getNewInstanceIdFor(pTgtVar, pRow);
		}
		
		return targetName;
	}
	
	private String getNewInstanceIdFor(String pTgtVar, CeConclusionRow pRow) {
		String result = "";
		String newIdxTemplate = this.uniqueTgtVars.get(pTgtVar);
		
		if (newIdxTemplate != null) {
			result = pRow.getNewValueFor(this.ac, newIdxTemplate);
		} else {
			reportError("Unable to locate new index template for variable '" + pTgtVar + "' in rule: " + this.ceRule.getRuleName(), this.ac);
		}
		
		//TODO: Complete this
		return result;
	}
	
	private void calculateUniqueTargetVariables() {
		this.uniqueTgtVars = new TreeMap<String, String>();
		
		for (CeClause cClause : getAllChildConclusionClauses()) {
			String tgtVar = cClause.getTargetVariable();
			String trimmedTgtVar = getVariableStubFrom(tgtVar);
			
			insertUvKey(trimmedTgtVar, tgtVar);
			insertUvKey(tgtVar, tgtVar);
		}
		
//		if (this.uniqueTgtVars.size() > 1) {
//			if (isReportDebug()) {
//				reportDebug("More than one unique target variable " + this.uniqueTgtVars.toString() + " for rule conclusions, for: " + this.ceRule.getCeText(), this.ac);
//			}
//		}
		
//		reportDebug("Processing rule: " + this.ceRule.getRuleName());
		
//		for (String utvKey : this.uniqueTgtVars.keySet()) {
//			reportDebug("Final Unique target variable: " + utvKey + " -> " + this.uniqueTgtVars.get(utvKey));
//		}
	}
	
	private void insertUvKey(String pKey, String pVal) {
		//First with the trimmed key
		if (this.uniqueTgtVars.containsKey(pKey)) {
			if (pKey.equals(pVal)) {
				//Both values are the same and there is already a value present so ignore
			} else {
				String existingVal = this.uniqueTgtVars.get(pKey);
				if (!existingVal.equals(pKey)) {
					//There is an existing value which does not match the trimmed value
					//...and that value is not the same as this so an error must be reported
					reportError("Two conflicted values (" + pVal + " and " + existingVal + ") detected during rule execution for variable " + pKey + " in rule: " + this.ceRule.getRuleName(), this.ac);
				} else {
					//There is no conflict so just overwite the value
					this.uniqueTgtVars.put(pKey, pVal);
				}
			}
		} else {
			//Not already present so just insert
			this.uniqueTgtVars.put(pKey, pVal);
		}
	}

	private static String getVariableStubFrom(String pTgtVar) {
		String result = "";
		String parts[] = pTgtVar.split("_");
		
		for (String thisPart : parts) {
			if (!thisPart.startsWith(CeConclusionRow.TOKEN_VARIABLE) && !thisPart.startsWith(CeConclusionRow.TOKEN_CONSTANT)) {
				if (!result.isEmpty()) {
					result += "_";
				}
				result += thisPart;
			}
		}

		return result;
	}

	private static String trimHashFrom(String pCvt) {
		return pCvt.replaceFirst(CeConclusionRow.TOKEN_CONSTANT, "");
	}

	private String calculatePiVal(CePropertyInstance pPi, CeConclusionRow pRow) {
		String piVal = "";
		
		if (!pPi.hadQuotesOriginally(this.ac)) {
			//This is a variable, so needs to be substituted
			String varName = pPi.getSingleOrFirstValue();
			piVal = getValueForHeader(varName, pRow);
		} else {
			piVal = pPi.getSingleOrFirstValue();
		}
		
		return piVal;
	}
	
	private String calculateInstName(CePropertyInstance pPi, CeConclusionRow pRow) {
		//TODO: Need to handle case when inst name is quoted?
		String varName = pPi.getClauseVariableId();
	
		return getValueForHeader(varName, pRow);
	}
	
	private static void replaceCeInResultRow(CeConclusionRow pRow, String pReplacementCe) {
		ArrayList<String> qRow = pRow.getQueryRow();
		int lastIndex = qRow.size() - 1;
		
		//The CE will always be the last item in the row, so it can be simply removed and re-added
		qRow.remove(lastIndex);
		qRow.add(pReplacementCe);
	}
	
	private String generateRationaleFor(CeConclusionRow pRow) {
		int lastIndex = pRow.getQueryRowSize() - 1;
		
		String existingCe = pRow.getQueryRow(lastIndex);
		
//		return NL + "because" + NL + existingCe + NL + "[ " + this.ceRule.getRuleName() + " ]." + NL + NL;
		return NL + "because" + NL + existingCe + NL + "[ " + this.ceRule.getRuleName() + " ].";
	}
	
}
