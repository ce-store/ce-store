package com.ibm.ets.ita.ce.store.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSentenceQualified;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.HelperConcept;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerCommonValues;
import com.ibm.ets.ita.ce.store.model.container.ContainerSearchResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.processor.ProcessorCe;
import com.ibm.ets.ita.ce.store.query.QueryExecutionManager;

public class QueryHandler extends RequestHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//Mode parameter values
	private static final String MODE_ALL = "all";
	private static final String MODE_ROOT = "root";

	public QueryHandler(ActionContext pAc) {
		super(pAc);
	}

	private static ArrayList<CeProperty> getAllObjectPropertiesBetween(CeConcept pDomConcept, CeConcept pRangeConcept) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		for (CeProperty thisProp : pDomConcept.calculateAllProperties().values()) {
			if (thisProp.isObjectProperty()) {
				//Only process the object properties
				CeConcept propRange = thisProp.getRangeConcept();

				if ((pRangeConcept.equalsOrHasParent(propRange))) {
					//If this property has a range which equals or is a parent of the passed range then include it
					result.add(thisProp);
				}
			}
		}
		
		return result;
	}
	
	private static ArrayList<CeProperty> getAllDatatypePropertiesFor(CeConcept pConcept) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();
		
		for (CeProperty thisProp : pConcept.calculateAllProperties().values()) {
			if (thisProp.isDatatypeProperty()) {
				result.add(thisProp);
			}
		}
		
		return result;
	}

//	public ArrayList<CeConcept> listAllConcepts() {
//		ArrayList<CeConcept> result = new ArrayList<CeConcept>();
//		
//		if (!this.mb.hasNoConcepts()) {
//			CeConcept thingCon = HelperConcept.thingConcept(this.ac);
//			
//			result.add(thingCon);
//			result.addAll(thingCon.retrieveAllChildren(false));
//		}
//		
//		return result;
//	}
	
	public ArrayList<CeConcept> listAllChildConceptsFor(String pConceptName) {
		return listConceptsOfType(pConceptName, MODE_ALL);
	}

	public ArrayList<CeConcept> listDirectChildConceptsFor(String pConceptName) {
		return listConceptsOfType(pConceptName, MODE_ROOT);
	}

	public ArrayList<CeConcept> listAllParentConceptsFor(String pConceptName) {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();
		CeConcept tgtCon = this.mb.getConceptNamed(this.ac, pConceptName);
		
		if (tgtCon != null) {
			result = tgtCon.retrieveAllParents(false);
		}
		
		return result;
	}

	public ArrayList<CeConcept> listDirectParentConceptsFor(String pConceptName) {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();
		CeConcept tgtCon = this.mb.getConceptNamed(this.ac, pConceptName);
		
		if (tgtCon != null) {
			for (CeConcept thisCon : tgtCon.getDirectParents()) {
				result.add(thisCon);
			}
		}
		
		return result;
	}

	public CeConcept getConceptDetails(String pConceptName) {		
		CeConcept thisConcept = null;
		
		if (pConceptName != null) {
			thisConcept = this.mb.getConceptNamed(this.ac, pConceptName);
			
			if (thisConcept == null) {
				reportWarning("Concept details cannot be located for unknown concept '" + pConceptName + "'", this.ac);
			}
		} else {
			// No concept specified
			reportWarning("No concept specified for showing concept details", this.ac);
		}
		
		return thisConcept;
	}

	public CeConceptualModel getConceptualModelDetails(String pCmName) {		
		CeConceptualModel thisCm = null;
		
		if (pCmName != null) {
			thisCm = this.mb.getConceptualModel(pCmName);
			
			if (thisCm == null) {
				reportWarning("Concept model details cannot be located for unknown conceptual model '" + pCmName + "'", this.ac);
			}
		} else {
			// No model specified
			reportWarning("No conceptual model specified for showing conceptual model details", this.ac);
		}
		
		return thisCm;
	}

	public ArrayList<CeProperty> listProperties(String pDomainName, String pRangeName) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();
		
		CeConcept domConcept = this.mb.getConceptNamed(this.ac, pDomainName);
		
		if (domConcept != null) {
			if (CeProperty.isDatatypeRangeName(pRangeName)) {
				//Get all datatype properties
				result = getAllDatatypePropertiesFor(domConcept);
			} else {
				CeConcept rangeConcept = this.mb.getConceptNamed(this.ac, pRangeName);
				
				if (rangeConcept != null) {
					//Get all object properties
					result = getAllObjectPropertiesBetween(domConcept, rangeConcept);
				} else {
					reportError("Unable to locate range concept named '" + pRangeName + "' during list property processing", this.ac);
				}
			}
		} else {
			reportError("Unable to locate domain concept named '" + pDomainName + "' during list property processing", this.ac);
		}
				
		return result;
	}
	
	public LinkedHashMap<String, CeQuery> listAllQueries() {
		//TODO: Add the additional VQB information here
		return this.mb.getAllQueries();
	}

	public LinkedHashMap<String, CeRule> listAllRules() {
		//TODO: Add the additional VQB information here
		return this.mb.getAllRules();
	}

	public ArrayList<ArrayList<String>> listAllReferencesFor(String pInstName) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		TreeMap<String, String> uidKeys = new TreeMap<String, String>();
		CeConcept conThing = HelperConcept.thingConcept(this.ac);
		int iterationCounter = 0;
				
		if ((pInstName != null) && (!pInstName.isEmpty())) {
			CeInstance targetInst = this.mb.getInstanceNamed(this.ac,pInstName);
			
			if (targetInst != null) {
				if (isReportMicroDebug()) {
					reportMicroDebug("There are " + new Integer(this.mb.countAllInstancesForConcept(conThing)).toString() + " instances to be checked", this.ac);
				}
				
				ArrayList<CeInstance> allInsts = this.mb.listAllInstancesForConcept(conThing);
				
				//Now iterate through all instances looking for matching properties
				for (CeInstance thisInst : allInsts) {
					for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
						if (thisPi.getRelatedProperty().isObjectProperty()) {
							for (String thisVal : thisPi.getValueList()) {
								++iterationCounter;
								if (thisVal.equals(pInstName)) {
									CeProperty thisProp = thisPi.getRelatedProperty();
									String subjectName = thisInst.getInstanceName();
									String propertyName = thisProp.getPropertyName();
									String uidKey = subjectName + ":" + propertyName;

									//Ensure that only a single property is added
									if (uidKeys.get(uidKey) == null) {
										ArrayList<String> thisRow = new ArrayList<String>();
										
										thisRow.add(subjectName);
										thisRow.add(propertyName);
										
										result.add(thisRow);
										uidKeys.put(uidKey, uidKey);
									}
								}
							}
						}
					}
				}					
			} else {
				reportError("Unable to locate instance named '" + pInstName + "' during listReferences processing", this.ac);
			}
		} else {
			result = new ArrayList<ArrayList<String>>();
			reportError("No instance name specified - list of references cannot be calculated", this.ac);
		}
		
		reportMicroDebug("Iteration counter = '" + new Integer(iterationCounter).toString() + "'", this.ac);
		
		return result;
	}

	public TreeMap<String, HashSet<CeInstance>> listAllInstanceReferencesFor(CeInstance pInst) {
		TreeMap<String, HashSet<CeInstance>> result = new TreeMap<String, HashSet<CeInstance>>();

		for (CeInstance tgtInst : this.mb.listAllInstances()) {
			for (CePropertyInstance thisPi : tgtInst.getObjectPropertyInstances()) {
				for (CeInstance relInst : thisPi.getValueInstanceList(this.ac)) {
					if (relInst == pInst) {
						String propName = thisPi.getPropertyName();
						HashSet<CeInstance> thisList = null;
								
						if (result.containsKey(propName)) {
							thisList = result.get(propName);
						} else {
							thisList = new HashSet<CeInstance>();
							result.put(propName, thisList);
						}
						thisList.add(tgtInst);
					}
				}
			}
		}					

		return result;
	}

	public CeQuery getQueryDetails(String pQueryName) {
		CeQuery targetQuery = null;
		
		if (pQueryName != null) {
			targetQuery = this.mb.getQueryNamed(pQueryName);
			
			if (targetQuery == null) {
				reportError("Unable to locate query named '" + pQueryName + "' when getting query details", this.ac);
			}			
		} else {
			// No query name specified
			reportWarning("No query name specified for getting details", this.ac);
		}
		
		return targetQuery;
	}

	public CeRule getRuleDetails(String pRuleName) {
		CeRule targetRule = null;
		
		if (pRuleName != null) {
			targetRule = this.mb.getRuleNamed(pRuleName);
			
			if (targetRule == null) {
				reportError("Unable to locate rule named '" + pRuleName + "' when getting rule details", this.ac);
			}			
		} else {
			// No rule name specified
			reportWarning("No rule name specified for getting details", this.ac);
		}
		
		return targetRule;
	}

	public ArrayList<CeSource> listAllSources() {
		ArrayList<CeSource> result = new ArrayList<CeSource>();
		
		if (this.mb != null) {			
			result.addAll(this.mb.getAllSources().values());
		} else {
			reportError("Unexpected null ModelBuilder in QueryHandler:listAllSources()", this.ac);
		}
		
		return result;
	}

	public CeSource getSourceDetailsFor(String pSrcId) {
		CeSource result = null;
		
		if (this.mb != null) {			
			result = this.mb.getSourceById(pSrcId);
		} else {
			reportError("Unexpected null ModelBuilder in QueryHandler:getSourceDetailsFor()", this.ac);
		}
		
		return result;
	}

	public ArrayList<CeConceptualModel> listAllConceptualModels() {
		ArrayList<CeConceptualModel> result = new ArrayList<CeConceptualModel>();
		
		if (this.mb != null) {			
			result.addAll(this.mb.getAllConceptualModels().values());
		} else {
			reportError("Unexpected null ModelBuilder in QueryHandler:listAllConceptualModels()", this.ac);
		}
		
		return result;
	}

	public ArrayList<CeSentence> listAllSentences() {
		return this.mb.listAllSentences();
	}

	public ArrayList<CeSentence> listAllModelSentences() {
		return listAllSentencesOfType(BuilderSentence.SENTYPE_MODEL);
	}
	
	public ArrayList<CeSentence> listAllNormalFactSentences() {
		return listAllSentencesOfType(BuilderSentence.SENTYPE_FACT_NORMAL);
	}
	
	public ArrayList<CeSentence> listAllQualifiedFactSentences() {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();
		
		for (CeSentence thisSen : this.mb.getAllValidSentences()) {
			if (thisSen.hasQualifiedSentences()) {
				for (CeSentenceQualified thisQs : thisSen.getQualifiedSentences()) {
					result.add(thisQs);
				}
			}
		}
		
		for (CeSentence thisSen : this.mb.getAllInvalidSentences()) {
			if (thisSen.hasQualifiedSentences()) {
				for (CeSentenceQualified thisQs : thisSen.getQualifiedSentences()) {
					result.add(thisQs);
				}
			}
		}

		return result;
	}

	public ArrayList<CeSentence> listAllRuleSentences() {
		return listAllSentencesOfType(BuilderSentence.SENTYPE_RULE);
	}
	
	public ArrayList<CeSentence> listAllQuerySentences() {
		return listAllSentencesOfType(BuilderSentence.SENTYPE_QUERY);
	}
	
	public ArrayList<CeSentence> listAllAnnotationSentences() {
		return listAllSentencesOfType(BuilderSentence.SENTYPE_ANNO);
	}

	public ArrayList<CeSentence> listAllCommandSentences() {
		return listAllSentencesOfType(BuilderSentence.SENTYPE_CMD);
	}

	public ArrayList<CeSentence> listAllValidSentences() {
		return this.mb.listAllValidSentences();
	}

	public ArrayList<CeSentence> listAllInvalidSentences() {
    return this.mb.listAllInvalidSentences();
	}

	public ArrayList<CeSentence> listAllSentencesForSource(String pSourceId) {
		ArrayList<CeSentence> senList = null;
		
		if (pSourceId != null) {
			CeSource thisSource = this.mb.getSourceById(pSourceId);
			if (thisSource != null) {
				senList = new ArrayList<CeSentence>(thisSource.listAllSentences());
			} else {
				senList = new ArrayList<CeSentence>();
				reportError("Unable to locate source '" + pSourceId + "'", this.ac);
			}
		} else {
			// No source id specified
			senList = new ArrayList<CeSentence>();
			reportWarning("No source id specified for listing sentences", this.ac);
		}

		return senList;
	}

	public ArrayList<CeSentence> listPrimarySentencesForConcept(String pConceptName) {
		ArrayList<CeSentence> senList = null;

		if (pConceptName != null) {
			CeConcept targetConcept = this.mb.getConceptNamed(this.ac, pConceptName);
			
			if (targetConcept != null) {
				senList = new ArrayList<CeSentence>();
				for (CeSentence priSen : targetConcept.getPrimarySentences()) {
					senList.add(priSen);
				}
			} else {
				senList = new ArrayList<CeSentence>();
				reportError("Unable to locate concept '" + pConceptName + "' when listing primary sentences", this.ac);
			}
		} else {
			// No concept name specified
			senList = new ArrayList<CeSentence>();
			reportWarning("No concept name specified for listing primary sentences", this.ac);
		}
		
		return senList;
	}

	public ArrayList<CeSentence> listSecondarySentencesForConcept(String pConceptName) {
		ArrayList<CeSentence> senList = new ArrayList<CeSentence>();

		if (pConceptName != null) {
			CeConcept targetConcept = this.mb.getConceptNamed(this.ac, pConceptName);
			
			if (targetConcept != null) {
		        senList.addAll(targetConcept.listSecondarySentences());
			} else {
				reportError("Unable to locate concept '" + pConceptName + "' when listing secondary sentences", this.ac);
			}
		} else {
			// No concept name specified
			reportWarning("No concept name specified for listing secondary sentences", this.ac);
		}

		return senList;
	}
	
	public ArrayList<CeSentence> listPrimarySentencesForInstance(String pInstanceName) {
		ArrayList<CeSentence> senList = null;

		if (pInstanceName != null) {
			CeInstance thisInst = this.mb.getInstanceNamed(this.ac, pInstanceName);

			if (thisInst != null) {
				senList = new ArrayList<CeSentence>();
				for (CeSentence thisSen : thisInst.getPrimarySentences()) {
					senList.add(thisSen);
				}
			} else {
				senList = new ArrayList<CeSentence>();
				reportError("Unable to locate instance '" + pInstanceName + "' when listing primary sentences", this.ac);
			}
		} else {
			// No instance name specified
			senList = new ArrayList<CeSentence>();
			reportWarning("No instance name specified for listing primary sentences", this.ac);
		}

		return senList;
	}

	public ArrayList<CeSentence> listSecondarySentencesForInstance(String pInstanceName) {
		ArrayList<CeSentence> senList = null;

		if (pInstanceName != null) {
			CeInstance thisInst = this.mb.getInstanceNamed(this.ac, pInstanceName);

			if (thisInst != null) {
				senList = new ArrayList<CeSentence>();
				for (CeSentence secSen : thisInst.getSecondarySentences()) {
					senList.add(secSen);
				}
			} else {
				senList = new ArrayList<CeSentence>();
				reportError("Unable to locate instance '" + pInstanceName + "' when listing secondary sentences", this.ac);
			}
		} else {
			// No instance name specified
			senList = new ArrayList<CeSentence>();
			reportWarning("No instance name specified for listing secondary sentences", this.ac);
		}

		return senList;
	}

	public ArrayList<CeConcept> listShadowConcepts() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();
		
		for (CeConcept thisConcept : this.mb.listAllConcepts()) {
			if (thisConcept.isShadowEntity()) {
				result.add(thisConcept);
			}
		}
		
		return result;
	}

	public ArrayList<CeInstance> listShadowInstances() {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		for (CeConcept thisConcept : this.mb.listAllConcepts()) {
			for (CeInstance thisInst : this.mb.retrieveAllInstancesForConcept(thisConcept)) {
				if (thisInst.isShadowEntity()) {
					if (thisInst.isDirectConcept(thisConcept)) {
						//Only report the shadow instances for the directly asserted concepts
						//Otherwise all inherited supertypes are also output here
						result.add(thisInst);
					}
				}
			}
		}

		return result;
	}

	public ArrayList<CeInstance> listUnreferencedInstances(boolean pIgnoreMetaModel) {
		HashSet<CeInstance> instSet = new HashSet<CeInstance>();
		ArrayList<CeInstance> result = null;

		for (CeConcept thisConcept : this.mb.listAllConcepts()) {
			for (CeInstance thisInst : this.mb.retrieveAllInstancesForConcept(thisConcept)) {
				if (!thisInst.hasReferringPropertyInstances()) {
					if (!pIgnoreMetaModel || (!thisInst.isMetaModelInstance())) {
						instSet.add(thisInst);
					}
				}
			}
		}

		result = new ArrayList<CeInstance>(instSet);
		Collections.sort(result);

		return result;
	}

	public TreeMap<CeInstance, ArrayList<CeConcept>> listDiverseConceptInstances() {
		TreeMap<CeInstance, ArrayList<CeConcept>> result = new TreeMap<CeInstance, ArrayList<CeConcept>>();
		
		for (CeInstance thisInst : this.mb.retrieveAllInstances()) {
			ArrayList<CeConcept> leafCons = thisInst.getAllLeafConcepts();
			
			if (leafCons.size() > 1) {
				ArrayList<CeConcept> divCons = new ArrayList<CeConcept>();

				for (CeConcept thisCon : leafCons) {
					if (isDiverseConceptFrom(thisCon, leafCons)) {
						divCons.add(thisCon);
					}
				}

				if (!divCons.isEmpty()) {
					result.put(thisInst, divCons);
				}
			}
		}
		
		return result;
	}

	private static boolean isDiverseConceptFrom(CeConcept pMainCon, ArrayList<CeConcept> pOtherCons) {
		boolean result = false;

		for (CeConcept otherCon : pOtherCons) {
			if (pMainCon != otherCon) {
				if (!pMainCon.hasCommonParentWith(otherCon)) {
					result = true;
				}
			}
		}

		return result;
	}

	public int countInstances(String pConceptName) {
		int result = 0;
		
		if (pConceptName != null) {
			CeConcept thisConcept = this.mb.getConceptNamed(this.ac, pConceptName);
			if (thisConcept != null) {
				result = this.ac.getModelBuilder().getInstanceCountForConcept(thisConcept);
			} else {
				reportError("Unable to count instances as the concept '" + pConceptName + "' could not be located", this.ac);
			}

		} else {
			reportError("Unable to count instances as concept has not been specified", this.ac);
		}

		return result;
	}

	public ContainerCeResult executeUserSpecifiedCeQuery(String pCeQuery, String pStartTs, String pEndTs) {
		return executeUserSpecifiedCeQueryWith(pCeQuery, QueryExecutionManager.FLAG_RESP_IDS, pStartTs, pEndTs, false);
	}
	
	public ContainerCeResult executeUserSpecifiedCeQuery(String pCeQuery, boolean pSuppressCeColumn, String pStartTs, String pEndTs) {
		return executeUserSpecifiedCeQueryWith(pCeQuery, QueryExecutionManager.FLAG_RESP_IDS, pStartTs, pEndTs, pSuppressCeColumn);
	}
	
	public ContainerCeResult executeUserSpecifiedCeQueryByName(String pCeQueryName, String pStartTs, String pEndTs) {
		return executeUserSpecifiedCeQueryByNameWithFlag(pCeQueryName, QueryExecutionManager.FLAG_RESP_IDS, false, pStartTs, pEndTs);
	}

	public ContainerCeResult executeUserSpecifiedCeQueryByName(String pCeQueryName, boolean pSuppressCeColumn, String pStartTs, String pEndTs) {
		return executeUserSpecifiedCeQueryByNameWithFlag(pCeQueryName, QueryExecutionManager.FLAG_RESP_IDS, pSuppressCeColumn, pStartTs, pEndTs);
	}
	
	private ContainerCeResult executeUserSpecifiedCeQueryByNameWithFlag(String pCeQueryName, int pFlag, boolean pSuppressCeColumn, String pStartTs, String pEndTs) {
		long sTime = System.currentTimeMillis();
		ContainerCeResult result = null;
		String ceQueryText = "";
		CeQuery targetQuery = this.mb.getQueryNamed(pCeQueryName);
		
		if (targetQuery == null) {
			//There is no query of this named, but the user may be asking to run a rule in query mode so now check all rules
			targetQuery = this.mb.getRuleNamed(pCeQueryName);
		}
		
		if (targetQuery != null) {
			ceQueryText = targetQuery.getCeText();
			result = executeUserSpecifiedCeQueryWith(ceQueryText, pFlag, pStartTs, pEndTs, pSuppressCeColumn);
		} else {
			reportError("Query (or rule) named '" + pCeQueryName + "' could not be located", this.ac);
			result = new ContainerCeResult();
		}
		
		result.setExecutionTime(System.currentTimeMillis() - sTime);
		
		return result;
	}

	public ContainerCeResult executeUserSpecifiedCeRule(String pCeRule, String pStartTs, String pEndTs, boolean pSuppressCeColumn) {
		return executeUserSpecifiedCeRuleWith(pCeRule, pStartTs, pEndTs, pSuppressCeColumn);
	}
	
	public ContainerCeResult executeUserSpecifiedCeRuleByName(String pCeRuleName, String pStartTs, String pEndTs, boolean pSuppressCeColumn) {
		long sTime = System.currentTimeMillis();
		ContainerCeResult result = null;
		String ceRuleText = "";
		CeRule targetRule = this.mb.getRuleNamed(pCeRuleName);
		
		if (targetRule != null) {
			ceRuleText = targetRule.getCeText();
			result = executeUserSpecifiedCeRuleWith(ceRuleText, pStartTs, pEndTs, pSuppressCeColumn);
		} else {
			reportError("Rule named '" + pCeRuleName + "' could not be located", this.ac);
			result = new ContainerCeResult();
		}
		
		result.setExecutionTime(System.currentTimeMillis() - sTime);

		return result;
	}

	public ContainerCeResult executeUserSpecifiedCeQueryForInstances(String pCeQuery, boolean pSuppressCeColumn, String pStartTs, String pEndTs) {
		return executeUserSpecifiedCeQueryWith(pCeQuery, QueryExecutionManager.FLAG_RESP_INSTS, pStartTs, pEndTs, pSuppressCeColumn);
	}
	
	public ContainerCeResult executeUserSpecifiedCeQueryByNameForInstances(String pCeQueryName, boolean pSuppressCeColumn, String pStartTs, String pEndTs) {
		return executeUserSpecifiedCeQueryByNameWithFlag(pCeQueryName, QueryExecutionManager.FLAG_RESP_INSTS, pSuppressCeColumn, pStartTs, pEndTs);
	}

	private ContainerCeResult executeUserSpecifiedCeQueryWith(String pCeQueryText, int pRespType, String pStartTs, String pEndTs, boolean pSuppressCeColumn) {
		long sTime = System.currentTimeMillis();
		ContainerCeResult result = null;

		this.ac.markAsExecutingQueryOrRule(true);

		if (pCeQueryText != null) {
			if (!pCeQueryText.isEmpty()) {
				ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("executeUserSpecifiedCeQueryWith");
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				CeQuery thisQuery = procCe.processQuerySentence(pCeQueryText, sTime);

				if (thisQuery != null) {
					if (!thisQuery.hasErrors()) {
						QueryExecutionManager qem = QueryExecutionManager.createUsing(this.ac, pSuppressCeColumn, pStartTs, pEndTs);
						
						result = qem.executeQuery(thisQuery, pRespType);
					} else {
						reportError("Query '" + thisQuery.getQueryName() + "' has errors and was not executed", this.ac);
						result = new ContainerCeResult();
					}
				} else {
					reportError("Error during parsing of CE query sentence (" + pCeQueryText + ")", this.ac);
					result = new ContainerCeResult();
				}
			} else {
				reportError("No CE query found in supplied string (empty string)", this.ac);
				result = new ContainerCeResult();
			}
		} else {
			reportError("No CE query found in supplied string (null)", this.ac);
			result = new ContainerCeResult();
		}
		
		result.setExecutionTime(System.currentTimeMillis() - sTime);
		
		return result;
	}
	
	private ContainerCeResult executeUserSpecifiedCeRuleWith(String pCeRuleText, String pStartTs, String pEndTs, boolean pSuppressCeColumn) {
		long sTime = System.currentTimeMillis();
		ContainerCeResult result = null;
		long startTime = System.currentTimeMillis();

		this.ac.markAsExecutingQueryOrRule(true);

		if (pCeRuleText != null) {
			if (!pCeRuleText.isEmpty()) {
				ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("executeUserSpecifiedCeRuleWith");
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				CeRule thisRule = procCe.processRuleSentence(pCeRuleText, startTime);
				
				if (thisRule != null) {
					if (!thisRule.hasErrors()) {
						QueryExecutionManager qem = QueryExecutionManager.createUsing(this.ac, pSuppressCeColumn, pStartTs, pEndTs);
						
						//TODO: Decide if rationale can be switched off in this mode
						result = qem.executeRule(thisRule, true, false);
					} else {
						reportError("Rule '" + thisRule.getRuleName() + "' has errors and was not executed", this.ac);
						result = new ContainerCeResult();
					}
				} else {
					reportError("Error during parsing of CE rule sentence (" + pCeRuleText + ")", this.ac);
					result = new ContainerCeResult();
				}
			} else {
				reportError("No CE rule found in supplied string (empty string)", this.ac);
				result = new ContainerCeResult();
			}
		} else {
			reportError("No CE rule found in supplied string (null)", this.ac);
			result = new ContainerCeResult();
		}
		
		result.setExecutionTime(System.currentTimeMillis() - sTime);

		return result;
	}

	public ArrayList<ContainerSearchResult> keywordSearch(String pTerms, String pConceptName, String pPropertyName, boolean pCaseSensitive) {
		ArrayList<ContainerSearchResult> searchArray = null;

		searchArray = executeMemorySearch(pTerms, pConceptName, pPropertyName, pCaseSensitive);

		return searchArray;
	}

	private ArrayList<ContainerSearchResult> executeMemorySearch(String pTerms, String pConceptName, String pPropertyName, boolean pCaseSensitive) {
		HashSet<ContainerSearchResult> result = new HashSet<ContainerSearchResult>();
		ArrayList<CeInstance> instsToSearch = null;

		if ((pConceptName != null) && (!pConceptName.isEmpty())) {
			CeConcept targetConcept = this.mb.getConceptNamed(this.ac, pConceptName);
			if (targetConcept != null) {
				instsToSearch = this.mb.retrieveAllInstancesForConcept(targetConcept);
			} else {
				reportWarning("Specified concept '" + pConceptName + "' could not be located, so search was carried out against all concepts", this.ac);
				instsToSearch = this.mb.retrieveAllInstances();
			}
		} else {
			instsToSearch = this.mb.retrieveAllInstances();
		}

		if (!instsToSearch.isEmpty()) {
			for (CeInstance thisInst : instsToSearch) {
				if ((pPropertyName == null) || (pPropertyName.isEmpty())) {
					for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
						searchPropertyInstanceFor(thisInst, thisPi, pTerms, result, pCaseSensitive);
					}
				} else {
					CePropertyInstance tgtPi = thisInst.getPropertyInstanceNamed(pPropertyName);

					if (tgtPi != null) {
						searchPropertyInstanceFor(thisInst, tgtPi, pTerms, result, pCaseSensitive);
					}
				}
			}
		} else {
			reportWarning("No searchable instances were located (no instances with searchable properties defined)", this.ac);
		}

		ArrayList<ContainerSearchResult>resArray = new ArrayList<ContainerSearchResult>();

		resArray.addAll(result);

		return resArray;
	}

	private static void searchPropertyInstanceFor(CeInstance pInst, CePropertyInstance pPi, String pTerms, HashSet<ContainerSearchResult> pResult, boolean pCaseSensitive) {
		HashSet<String> uvList = pPi.getValueList();

		for (String thisVal : uvList) {
			String srcVal = null;
			String tgtVal = null;

			if (pCaseSensitive) {
				srcVal = thisVal;
				tgtVal = pTerms;
			} else {
				srcVal = thisVal.toLowerCase();
				tgtVal = pTerms.toLowerCase();
			}

			if (srcVal.contains(tgtVal)) {
				for (CeConcept instDirConcept : pInst.getDirectConcepts()) {
					ContainerSearchResult csr = new ContainerSearchResult();
					csr.setConceptName(instDirConcept.getConceptName());
					csr.setInstanceName(pInst.getInstanceName());
					csr.setPropertyName(pPi.getPropertyName());
					csr.setPropertyValue(thisVal);

					if (pPi.getRelatedProperty().isDatatypeProperty()) {
						csr.setPropertyType("value");
					} else {
						csr.setPropertyType("instance");
					}

					pResult.add(csr);
				}
			}
		}
	}

	public ArrayList<ContainerCommonValues> listCommonPropertyValues(CeProperty pProp) {
		ArrayList<ContainerCommonValues> commonVals = new ArrayList<ContainerCommonValues>();
		
		TreeMap<String, Long> result = new TreeMap<String, Long>();
		
		for (CeInstance thisInst : this.mb.listAllInstancesForConcept(pProp.getDomainConcept())) {
			CePropertyInstance thisPi = thisInst.getPropertyInstanceForProperty(pProp);
			
			if (thisPi != null) {
				for (String thisVal : thisPi.getValueList()) {
					long countVal = 0;
					Long existingCount = null;
					
					existingCount = result.get(thisVal);
					
					if (existingCount != null) {
						countVal = existingCount.longValue();
					}
					
					result.put(thisVal, new Long(++countVal));
				}
			}
		}
		
		for (String thisKey : result.keySet()) {
			Long countVal = result.get(thisKey);
			ContainerCommonValues thisCcv = new ContainerCommonValues();
			thisCcv.setName(thisKey);
			thisCcv.setCount(countVal.toString());
			commonVals.add(thisCcv);
		}

		return commonVals;
	}

	public ArrayList<CeInstance> listAllInstanceDetails(CeConcept pConcept) {
		return this.mb.retrieveAllInstancesForConcept(pConcept);
	}
	
	public ArrayList<CeInstance> listAllExactInstanceDetails(CeConcept pConcept) {
		return this.mb.retrieveAllExactInstancesForConcept(pConcept);
	}

	private ArrayList<CeSentence> listAllSentencesOfType(int pSenType) {
		return this.mb.listAllSentencesOfType(this.ac, pSenType);
	}

	private ArrayList<CeConcept> listConceptsOfType(String pConceptName, String pMode) {
		ArrayList<CeConcept> listOfConcepts = new ArrayList<CeConcept>();
		String targetMode = "";

		if (pMode != null) {
			targetMode = pMode;
		} else {
			targetMode = MODE_ALL;
		}

		CeConcept targetConcept = null;
		//If a concept name is specified try to return the children of that concept, otherwise return all concepts
		if (pConceptName != null) {
			targetConcept = this.mb.getConceptNamed(this.ac, pConceptName);
		}

		if (targetConcept == null) {
			if (pConceptName != null) {
				reportWarning("Specified concept '" + pConceptName + "' could not be located so listConcepts has returned all 'thing' concepts rather than child concepts", this.ac);
			}
		}

		if (targetConcept != null) {
			if (targetMode.equals(MODE_ALL)) {
				listOfConcepts = targetConcept.retrieveAllChildren(true);
			} else {
				listOfConcepts = new ArrayList<CeConcept>();
				for (CeConcept dirChild : targetConcept.getDirectChildren()) {
					listOfConcepts.add(dirChild);
				}
			}
		}

		return listOfConcepts;
	}

}