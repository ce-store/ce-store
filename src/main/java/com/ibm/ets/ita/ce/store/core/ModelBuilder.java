package com.ibm.ets.ita.ce.store.core;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.NO_TS;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendNewLineToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.isQuoteDelimited;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.model.CeAnnotation;
import com.ibm.ets.ita.ce.store.model.CeClause;
import com.ibm.ets.ita.ce.store.model.CeConcatenatedValue;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeModelEntity;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSequence;
import com.ibm.ets.ita.ce.store.model.CeSequenceClause;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleConclusion;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationalePart;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationalePremise;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.query.QueryExecutionManager;
import com.ibm.ets.ita.ce.store.uid.UidManager;
import com.ibm.ets.ita.ce.store.uid.UidManagerDefault;

public class ModelBuilder {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	//String caching levels (0 = No caching at all)
	private static final int CACHELEVEL_1 = 1;	//Caching of heavily repetitive strings
	private static final int CACHELEVEL_2 = 2;	//Caching of level 2 plus CE sentence text
	private static final int CACHELEVEL_3 = 3;	//Caching of all strings
	private int cacheLevel = CACHELEVEL_1;

	private String ceStoreName = null;
	private long creationTime = NO_TS; // time of initial creation

	private TreeMap<String, CeConcept> allConcepts = null;
	private HashMap<String, CeSource> allSources = null;
	private HashMap<String, CeConceptualModel> allConceptualModels = null;
	private HashSet<CeProperty> allProperties = null;
	private SentenceList allValidSentences = new SentenceList();
	private SentenceList allInvalidSentences = new SentenceList();
	private InstanceRepository instanceRepository = null;
	private LinkedHashMap<String, CeRule> allRules = null;
	private LinkedHashMap<String, CeQuery> allQueries = null;
	private CeRationaleReasoningStep[] allReasoningSteps = new CeRationaleReasoningStep[0];
	private HashMap<String, String> allValues = new HashMap<String, String>();
	
	private HashSet<String> cachedConceptFragmentNames = new HashSet<String>();
	private HashSet<String> cachedPropertyFragmentNames = new HashSet<String>();
	private HashSet<String> cachedInstanceFragmentNames = new HashSet<String>();
	
	//TODO: Need to remove or rename this
	private ArrayList<String> tempWarnings = null;

	private UidManager uidMgr = null;

	private ModelBuilder(ActionContext pAc, String pCeStoreName) {
		// private so new instances can only be created via the static method
		this.ceStoreName = pCeStoreName;
		this.creationTime = timestampNow();
		reset(pAc);
	}

	public static ModelBuilder createNew(ActionContext pAc, String pCeStoreName) {
		ModelBuilder newMb = new ModelBuilder(pAc, pCeStoreName);
		return newMb;
	}

	public void reset(ActionContext pAc) {
		// Create empty collections for each of the relevant properties
		if (pAc.getCeConfig().isCaseSensitive()) {
			this.allConcepts = new TreeMap<String, CeConcept>();
		} else {
			this.allConcepts = new TreeMap<String, CeConcept>(String.CASE_INSENSITIVE_ORDER);
		}
		this.allProperties = new HashSet<CeProperty>();
		this.allSources = new LinkedHashMap<String, CeSource>();
		this.allConceptualModels = new HashMap<String, CeConceptualModel>();
		this.allValidSentences = new SentenceList();
		this.allInvalidSentences = new SentenceList();
		this.allRules = new LinkedHashMap<String, CeRule>();
		this.allQueries = new LinkedHashMap<String, CeQuery>();
		this.allReasoningSteps = new CeRationaleReasoningStep[0];
		this.instanceRepository = null;

		// This cannot be included in the constructor due to a recursive issue
		// with the DbActions constructor calling the ModelBuilder constructor
		this.instanceRepository = new InstanceRepository();

		this.allValues = new HashMap<String, String>();

		this.cachedConceptFragmentNames = new HashSet<String>();
		this.cachedPropertyFragmentNames = new HashSet<String>();
		this.cachedInstanceFragmentNames = new HashSet<String>();
		
		// Reset all of the counters that are used when allocating objects
		CeRationalePart.resetCounter();
		CeAnnotation.resetCounter();
		CeClause.resetCounter();
		CeConcatenatedValue.resetCounter();
		CeModelEntity.resetCounter();
		CePropertyValue.resetCounter();
		CeQuery.resetCounter();
		CeSentence.resetCounter();
		CeSequence.resetCounter();
		CeSequenceClause.resetCounter();
		CeSource.resetCounter();
	}

	public String getCeStoreName() {
		return this.ceStoreName;
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public InstanceRepository getInstanceRepository() {
		return this.instanceRepository;
	}
	
	protected void resetInstanceRepository(ActionContext pAc) {
		this.instanceRepository.removeAllNonMetamodelInstances(pAc);
	}

	public ArrayList<CeInstance> retrieveAllInstancesForConcept(CeConcept pConcept) {
		return getInstanceRepository().getAllInstancesForConcept(pConcept);
	}
	
	public ArrayList<CeInstance> retrieveInstancesForConceptCreatedSince(CeConcept pConcept, long pSinceTs) {
		return getInstanceRepository().getAllInstancesForConceptCreatedSince(pConcept, pSinceTs);
	}

	public ArrayList<CeInstance> retrieveAllExactInstancesForConcept(CeConcept pConcept) {
		return getAllExactInstancesForConcept(pConcept);
	}
	
	public ArrayList<CeInstance> retrieveAllInstances() {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		
		for (CeConcept thisConcept : listAllConcepts()) {
			result.addAll(retrieveAllInstancesForConcept(thisConcept));
		}
		
		return result;
	}
	
	private ArrayList<CeInstance> getAllExactInstancesForConcept(CeConcept pConcept) {
		return getInstanceRepository().getAllExactInstancesForConcept(pConcept);
	}
	
	public ArrayList<CeInstance> getAllInstancesForConceptNamed(ActionContext pAc, String pConceptName) {
		CeConcept tgtConcept = getConceptNamed(pAc, pConceptName);
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		
		if (tgtConcept != null) {
			result = getInstanceRepository().getAllInstancesForConcept(tgtConcept);
		} else {
			reportError("Unable to locate concept named '" + pConceptName + "'", pAc);
		}
		
		return result;
	}

	public CeInstance getInstanceNamed(ActionContext pAc, String pInstanceName) {
		InstanceRepository ir = getInstanceRepository();
		CeInstance result = null;
		
		if (ir != null) {
			result = ir.getInstanceNamed(pAc, pInstanceName);
		}
		
		return result;
	}
	
	public CeInstance getOrCreateInstanceNamed(ActionContext pAc, String pInstanceName) {
		CeInstance result = getInstanceNamed(pAc, pInstanceName);
		
		if (result == null) {
			//Existing instance not found, so create a new one and save it
			result = CeInstance.createInstanceNamed(pAc, pInstanceName);
			saveInstance(pAc, result);
		}
		
		return result;
	}

	public long getTotalInstanceCount() {
		long result = -1;
		
		result = getInstanceRepository().getTotalInstanceCount();
		
		return result;
	}
	
	public int getInstanceCountForConcept(CeConcept pConcept) {
		return getInstanceRepository().getInstanceCountForConcept(pConcept);
	}

	public void saveInstance(ActionContext pAc, CeInstance pInstance) {
		//Clear the instance fragment names cache
		this.cachedInstanceFragmentNames = new HashSet<String>();

		InstanceRepository ir = getInstanceRepository();
		
		if (ir != null) {
			ir.saveInstance(pAc, pInstance);
		} else {
			reportError("Unable to save instance (" + pInstance.toString() + ") as instance repository is null", pAc);
		}
	}

	public void removeInstance(ActionContext pAc, CeInstance pInstance) {
		//Clear the instance fragment names cache
		this.cachedInstanceFragmentNames = new HashSet<String>();

		InstanceRepository ir = getInstanceRepository();
		
		if (ir != null) {
			ir.removeInstance(pInstance);
		} else {
			reportError("Unable to remove instance (" + pInstance.toString() + ") as instance repository is null", pAc);
		}
	}

	public Collection<CeConcept> listAllConcepts() {
		return this.allConcepts.values();
	}
	
	public void saveConcept(CeConcept pConcept) {
		//Clear the concept fragment cache
		this.cachedConceptFragmentNames = new HashSet<String>();

		this.allConcepts.put(pConcept.identityKey(), pConcept);
	}
	
	private void clearSources() {
		this.allSources = new LinkedHashMap<String, CeSource>();
	}

	public void saveSource(CeSource pSource) {
		if (pSource != null) {
			this.allSources.put(pSource.getId(), pSource);
		}
	}

	private void saveSentence(ActionContext pAc, CeSentence pSentence) {
		if (pSentence.isValid()) {
			saveValidSentence(pAc, pSentence);
		} else {
			saveInvalidSentence(pAc, pSentence);
		}
	}
	
	private void saveValidSentence(ActionContext pAc, CeSentence pSentence) {
		if (pSentence != null) {
			if (pAc.getCeConfig().isSavingCeSentences()) {
				addValidSentence(pSentence);
			}
		}
	}

	private void saveInvalidSentence(ActionContext pAc, CeSentence pSentence) {
		if (pSentence != null) {
			if (pAc.getCeConfig().isSavingCeSentences()) {
				addInvalidSentence(pSentence);
			}
		}
	}

	public void updateCreatedThingsFrom(ActionContext pAc) {
		updateSentenceListsFrom(pAc);
		updateInstanceListFrom(pAc);
	}

	private void updateSentenceListsFrom(ActionContext pAc) {
		SessionCreations sc = pAc.getSessionCreations();

		if (pAc.getCeConfig().isSavingCeSentences()) {
			addValidSentences(sc.getValidSentencesCreated());
			addInvalidSentences(sc.getInvalidSentencesCreated());
		}

		//Save the inferred sentences against the rule that inferred them
		if (sc.getValidSentencesCreated() != null) {
			for (CeSentence thisSen : sc.getValidSentencesCreated()) {
				String ruleName = thisSen.getRationaleRuleName();

				if ((ruleName != null) && (!ruleName.isEmpty())) {
					CeRule tgtRule = getRuleNamed(ruleName);

					if (tgtRule != null) {
						tgtRule.addInferredSentence(thisSen);
					} else {
						if (CeRule.isTemporaryRuleName(ruleName)) {
							//This is expected behaviour.  Temporary rules are not saved
							reportDebug("Not saving sentences to temporary rule named '" + ruleName + "'", pAc);
						} else {
							//This is a real rule name, so this is an error that should be reported
							reportDebug("Unable to find rule named '" + ruleName + "' when saving inferred sentences", pAc);
						}
					}
				} else {
					//TODO: Should be able to detect inferred sentences that are missing
					//the rule name and output an error here
				}
			}
		}

		sc.clearSessionSentences(pAc);
	}

	private static synchronized void updateInstanceListFrom(ActionContext pAc) {
		SessionCreations sc = pAc.getSessionCreations();
		
		//Update the concept instance lists for all new instances in this request
		pAc.getModelBuilder().getInstanceRepository().updateConceptInstanceListsFor(sc.getNewInstances());
		sc.clearSessionInstances();
	}

	public HashMap<String, CeSource> getAllSources() {
		return this.allSources;
	}
	
	public HashSet<CeProperty> getAllProperties() {
		return this.allProperties;
	}

	public HashMap<String, CeConceptualModel> getAllConceptualModels() {
		return this.allConceptualModels;
	}
	
	public ArrayList<CeConceptualModel> listAllConceptualModels() {
		ArrayList<CeConceptualModel> result = new ArrayList<CeConceptualModel>();
		
		for (CeConceptualModel thisCm : this.allConceptualModels.values()) {
			result.add(thisCm);
		}
		
		return result;
	}

	public CeConceptualModel getConceptualModel(String pCmName) {
		return this.allConceptualModels.get(pCmName);
	}
	
	public void saveConceptualModel(CeConceptualModel pCm) {
		if (pCm != null) {
			this.allConceptualModels.put(pCm.getModelName(), pCm);
		}
	}

	public int countAllProperties() {
		return this.allProperties.size();
	}

	public void addProperty(CeProperty pProp) {
		//Clear the property fragment cache
		this.cachedPropertyFragmentNames = new HashSet<String>();

		this.allProperties.add(pProp);
	}

	public void removeProperty(CeProperty pProp) {
		//Clear the property fragment cache
		this.cachedPropertyFragmentNames = new HashSet<String>();

		this.allProperties.remove(pProp);
	}

	public CeSource getSourceById(String pId) {
		CeSource result = null;

		break_position:
		for (CeSource thisSource : this.allSources.values()) {
			if (thisSource.getId().equals(pId)) {
				result = thisSource;
				break break_position;
			}
		}
		
		return result;
	}

	public ArrayList<CeSource> getSourcesByDetail(String pDetail) {
		ArrayList<CeSource> result = new ArrayList<CeSource>();

		for (CeSource thisSource : this.allSources.values()) {
			if (thisSource.getDetail().equals(pDetail)) {
				result.add(thisSource);
			}
		}

		return result;
	}

	public synchronized CeSource getOrCreateSourceByDetail(ActionContext pAc, String pDetail) {
		CeSource result = null;
		ArrayList<CeSource> srcList = getSourcesByDetail(pDetail);

		if (srcList.isEmpty()) {
			result = CeSource.createNewFormSource(pAc, pDetail, null);
		} else {
			result = srcList.get(0);
		}

		return result;
	}

	public synchronized CeSource getOrCreateSourceById(ActionContext pAc, String pId, String pDetail) {
		CeSource result = getSourceById(pId);

		if (result == null) {
			result = CeSource.createNewUrlSource(pAc, pDetail, pId);
		}

		return result;
	}

	public ArrayList<CeSource> getSourcesByAgentInstanceName(String pAgentInstName) {
		ArrayList<CeSource> result = new ArrayList<CeSource>();

		for (CeSource thisSource : this.allSources.values()) {
			String thisAiName = thisSource.getAgentInstanceName();
			
			if (thisAiName != null) {
				if (thisAiName.equals(pAgentInstName)) {
					result.add(thisSource);
				}
			}
		}
		
		return result;
	}

	public CeSentence[] getAllValidSentences() {
		return this.allValidSentences.getSentences();
	}

	public int countAllValidSentences() {
		return this.allValidSentences.getSentenceCount();
	}

	public boolean hasValidSentence(String pSenId) {
		return this.allValidSentences.containsSentence(pSenId);
	}

	public void addValidSentence(CeSentence pSen) {
		this.allValidSentences.addSentence(pSen, null);
	}

	private void addValidSentences(List<CeSentence> pSenList) {
		this.allValidSentences.addSentences(pSenList, null);
	}

	private void removeValidSentenceWithId(String pSenId) {
		this.allValidSentences.removeSentence(pSenId);
	}

	public CeSentence[] getAllInvalidSentences() {
		return this.allInvalidSentences.getSentences();
	}

	public int countAllInvalidSentences() {
		return this.allInvalidSentences.getSentenceCount();
	}

	public boolean hasInvalidSentence(String pSenId) {
		return this.allInvalidSentences.containsSentence(pSenId);
	}

	private void addInvalidSentence(CeSentence pSen) {
		this.allInvalidSentences.addSentence(pSen, null);
	}

	private void addInvalidSentences(List<CeSentence> pSenList) {
		this.allInvalidSentences.addSentences(pSenList, null);
	}

	private void removeInvalidSentenceWithId(String pSenId) {
		this.allInvalidSentences.removeSentence(pSenId);
	}

	public ArrayList<CeSentence> listAllSentences() {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();
		result.addAll(this.allValidSentences.getSentencesAsList());
		result.addAll(this.allInvalidSentences.getSentencesAsList());
		return result;
	}
	
	public ArrayList<CeSentence> listAllSentencesInSourcesNamed(String pSrcName) {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();
		
		for (CeSource thisSrc : getSourcesByDetail(pSrcName)) {
			result.addAll(thisSrc.listAllSentences());
		}
		
		return result;
	}

	public CeConcept getConceptNamed(ActionContext pAc, String pName) {
		CeConcept result = null;

		if (isQuoteDelimited(pName)) {
			reportWarning("Concept names should not be quote delimited: " + pName, pAc);
		}

		result = this.allConcepts.get(pName);

		return result;
	}

	public CeProperty getPropertyFullyNamed(String pFullName) {
		for (CeProperty thisProp : this.allProperties) {
			if (thisProp.identityKey().equals(pFullName)) {
				return thisProp;
			}
		}
		
		return null;
	}

	public ArrayList<CeProperty> listAllPropertiesReferringTo(CeConcept pConcept) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();
		
		for (CeProperty thisProp : this.allProperties) {
			CeConcept thisRange = thisProp.getRangeConcept();
			if (thisRange != null) {
				if ((thisRange.equals(pConcept)) || (thisRange.hasDirectChild(pConcept))) {
					result.add(thisProp);
				}
			}
		}
		
		return result;
	}

	public boolean isThereAConceptNameStarting(ActionContext pAc, String pName) {
		String tgtName = null;

		if (pAc.getCeConfig().isCaseSensitive()) {
			tgtName = pName;
		} else {
			tgtName = pName.toLowerCase();
		}

		boolean result = this.cachedConceptFragmentNames.contains(tgtName);

		if (!result) {
			for (CeConcept thisConcept : this.allConcepts.values()) {
				String conName = null;

				if (pAc.getCeConfig().isCaseSensitive()) {
					conName = thisConcept.getConceptName();
				} else {
					conName = thisConcept.getConceptName().toLowerCase();
				}

				if (conName.startsWith(tgtName)) {
					this.cachedConceptFragmentNames.add(tgtName);
					return true;
				}
			}
		}

		return result;
	}

	public boolean isThereAnInstanceNameStarting(ActionContext pAc, String pName) {
		boolean result = this.cachedInstanceFragmentNames.contains(pName);
		
		if (!result) {
			for (CeInstance thisInstance : listAllInstances()) {
				String instName = null;
				String tgtName = null;

				if (pAc.getCeConfig().isCaseSensitive()) {
					instName = thisInstance.getInstanceName();
					tgtName = pName;
				} else {
					instName = thisInstance.getInstanceName().toLowerCase();
					tgtName = pName.toLowerCase();
				}

				if (instName.startsWith(tgtName)) {
					this.cachedInstanceFragmentNames.add(pName);
					return true;
				}
			}
		}
		
		return result;
	}

	public boolean isThereAPropertyNameStarting(String pName) {
		boolean result = this.cachedPropertyFragmentNames.contains(pName);
		
		if (!result) {
			for (CeProperty thisProperty : this.allProperties) {
				if (thisProperty.getPropertyName().startsWith(pName)) {
					this.cachedPropertyFragmentNames.add(pName);
					return true;
				}
			}
		}
		
		return result;
	}

	public boolean isThereAConceptNameStartingButNotExactly(ActionContext pAc, String pName) {
		return (getConceptNamed(pAc, pName) == null) && isThereAConceptNameStarting(pAc, pName);
	}
	
	public boolean isThereADefinedPropertyNameStartingButNotExactly(String pName) {
		for (CeProperty thisProp : getAllProperties()) {
			if (!thisProp.isInferredProperty()) {
				String lcPropName = thisProp.getPropertyName().toLowerCase();
				if (lcPropName.startsWith(pName)) {
					if (!lcPropName.equals(pName)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	public boolean isThereAnInstanceNameStartingButNotExactly(ActionContext pAc, String pName) {
		return isThereAnInstanceNameStarting(pAc, pName + " ");
	}

	public ArrayList<CeProperty> getPropertiesNamed(String pPropName) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();
		
		for (CeProperty thisProp : this.allProperties) {
			if (thisProp.getPropertyName().equals(pPropName)) {
				result.add(thisProp);
			}
		}
		
		return result;
	}

	public LinkedHashMap<String, CeRule> getAllRules() {
		if (this.allRules == null) {
			this.allRules = new LinkedHashMap<String, CeRule>();
		}
		
		return this.allRules;
	}

	public CeRule getRuleNamed(String pRuleName) {
		//Lazy initialisation
		if ((this.allRules == null) || (this.allRules.isEmpty())) {
			getAllRules();
		}

		CeRule result = null;

		for (CeRule thisRule : this.allRules.values()) {
			String thisRuleName = thisRule.getQueryName();

			if ((thisRuleName != null) && (thisRuleName.equals(pRuleName))) {
				result = thisRule;
			}
		}

		return result;
	}

	public void addRule(CeRule pRule) {
		if (this.allRules == null) {
			this.allRules = new LinkedHashMap<String, CeRule>();
		}

		//Now the new rule can be added
		this.allRules.put(pRule.getRuleName(), pRule);
	}
	
	public LinkedHashMap<String, CeQuery> getAllQueries() {
		if (this.allQueries == null) {
			this.allQueries = new LinkedHashMap<String, CeQuery>();
		}
		
		return this.allQueries;
	}

	public CeQuery getQueryNamed(String pQueryName) {		
		//Lazy initialisation
		if ((this.allQueries == null) || (this.allQueries.isEmpty())) {
			getAllQueries();
		}
		
		CeQuery result = null;
		
		for (CeQuery thisQuery : this.allQueries.values()) {
			String thisQueryName = thisQuery.getQueryName();
			
			if ((thisQueryName != null) && (thisQueryName.equals(pQueryName))) {
				result = thisQuery;
			}
		}
		
		return result;
	}
	
	public void addQuery(CeQuery pQuery) {
		if (this.allQueries == null) {
			this.allQueries = new LinkedHashMap<String, CeQuery>();
		}
		
		//Now the new query can be added
		this.allQueries.put(pQuery.getQueryName(), pQuery);
	}

	public ArrayList<CeRationaleReasoningStep> getAllReasoningSteps() {
		ArrayList<CeRationaleReasoningStep> ratList = new ArrayList<CeRationaleReasoningStep>();

		for (CeRationaleReasoningStep thisRs : this.allReasoningSteps) {
			ratList.add(thisRs);
		}

		return ratList;
	}
	
	public void addReasoningStep(CeRationaleReasoningStep pRs) {
		int currLen = 0;

		currLen = this.allReasoningSteps.length;
		CeRationaleReasoningStep[] newArray = new CeRationaleReasoningStep[currLen + 1];
		System.arraycopy(this.allReasoningSteps, 0, newArray, 0, currLen);
		this.allReasoningSteps = newArray;

		this.allReasoningSteps[currLen] = pRs;			
	}

	public ArrayList<CeRationaleReasoningStep> getReasoningStepsForRule(String pRuleName) {
		ArrayList<CeRationaleReasoningStep> result = new ArrayList<CeRationaleReasoningStep>();
		
		for (CeRationaleReasoningStep thisRs : this.allReasoningSteps) {
			if (thisRs.getRuleName().equals(pRuleName)) {
				result.add(thisRs);
			}
		}
		
		return result;
	}

	public ArrayList<CeRationaleReasoningStep> getReasoningStepsForSentence(CeSentence pSen) {
		ArrayList<CeRationaleReasoningStep> result = new ArrayList<CeRationaleReasoningStep>();

		for (CeRationaleReasoningStep thisRs : this.allReasoningSteps) {
			if (thisRs.getSourceSentence().equals(pSen)) {
				result.add(thisRs);
			}
		}
		
		return result;
	}

	public ArrayList<CeRationaleReasoningStep> getReasoningStepsForConcept(String pConName, boolean pCheckPremise) {
		ArrayList<CeRationaleReasoningStep> result = new ArrayList<CeRationaleReasoningStep>();

		for (CeRationaleReasoningStep thisRs : this.allReasoningSteps) {
			if (pCheckPremise == false){
				for (CeRationaleConclusion thisConc : thisRs.getConclusions()) {
					if (thisConc.getConceptName().equals(pConName)) {
						result.add(thisRs);
					}
				}
			} else {
				for (CeRationalePremise thisPremise : thisRs.getPremises()) {
					if (thisPremise.getConceptName().equals(pConName)) {
						result.add(thisRs);
					}
				}
			}
		}
		
		return result;
	}

	public ArrayList<CeRationaleReasoningStep> getReasoningStepsForProperty(String pPropName, boolean pCheckPremise) {
		ArrayList<CeRationaleReasoningStep> result = new ArrayList<CeRationaleReasoningStep>();
		
		for (CeRationaleReasoningStep thisRs : this.allReasoningSteps) {
			if (pCheckPremise == false){
				for (CeRationaleConclusion thisConc : thisRs.getConclusions()) {
					if ((thisConc.getPropertyName().equals(pPropName))) {
						result.add(thisRs);
					}
				}
			} else {
				for (CeRationalePremise thisPremise : thisRs.getPremises()) {
					if (thisPremise.getPropertyName().equals(pPropName)) {
						result.add(thisRs);
					}
				}
			}
		}
		
		return result;
	}

	public ArrayList<CeRationaleReasoningStep> getReasoningStepsForInstance(String pInstName, boolean pCheckPremise) {
		ArrayList<CeRationaleReasoningStep> result = new ArrayList<CeRationaleReasoningStep>();

		for (CeRationaleReasoningStep thisRs : this.allReasoningSteps) {
			if (pCheckPremise == false) {
				for (CeRationaleConclusion thisConc : thisRs.getConclusions()) {
					if (thisConc.getInstanceName().equals(pInstName)) {
						result.add(thisRs);
					}
				}
			} else {
				for (CeRationalePremise thisPremise : thisRs.getPremises()) {
					if (thisPremise.getInstanceName().equals(pInstName)) {
						result.add(thisRs);
					}
				}
			}
		}
		
		return result;
	}

	public ArrayList<CeRationaleReasoningStep> getReasoningStepsForPropertyValue(String pInstName, String pPropName, String pValue, boolean pCheckPremise) {
		ArrayList<CeRationaleReasoningStep> result = new ArrayList<CeRationaleReasoningStep>();
		
		for (CeRationaleReasoningStep thisRs : this.allReasoningSteps) {
			if (pCheckPremise == false){
				for (CeRationaleConclusion thisConc : thisRs.getConclusions()) {
					if ((thisConc.getInstanceName().equals(pInstName)) && (thisConc.getPropertyName().equals(pPropName)) && (thisConc.getValue().equals(pValue))) {
						result.add(thisRs);
					}
				}
			} else {
				for (CeRationalePremise thisPremise : thisRs.getPremises()) {
					if ((thisPremise.getInstanceName().equals(pInstName)) && (thisPremise.getPropertyName().equals(pPropName)) && (thisPremise.getValue().equals(pValue))) {
						result.add(thisRs);
					}
				}
			}
		}
		
		return result;
	}

	public long countSentences() {
		int valCount = countAllValidSentences();
		int invalCount = countAllInvalidSentences();

		return valCount + invalCount;
	}
	
	public ArrayList<CeSentence> listAllSentencesOfType(ActionContext pAc, int pSenType) {
		ArrayList<CeSentence> result = null;
		
		switch (pSenType) {
		case BuilderSentence.SENTYPE_FACT:
			result = listAllFactSentences();
			break;
		case BuilderSentence.SENTYPE_MODEL:
			result = listAllModelSentences();
			break;
		case BuilderSentence.SENTYPE_RULE:
			result = listAllRuleSentences();
			break;
		case BuilderSentence.SENTYPE_QUERY:
			result = listAllQuerySentences();
			break;
		case BuilderSentence.SENTYPE_ANNO:
			result = listAllAnnotationSentences();
			break;
		case BuilderSentence.SENTYPE_CMD:
			result = listAllCommandSentences();
			break;
		default:
			reportError("Unexpected sentence type '" + pSenType + "' encountered in ModelBuilder:getSentencesOfType()", pAc);
			break;
		}
		
		return result;
	}

	public ArrayList<CeSentence> listAllValidSentences() {
		return this.allValidSentences.getSentencesAsArrayList();
	}

	public ArrayList<CeSentence> listAllInvalidSentences() {
		return this.allInvalidSentences.getSentencesAsArrayList();
	}

	public ArrayList<CeSentence> listAllModelSentences() {
		return listAllSentencesOfTypeWith(BuilderSentence.SENTYPE_MODEL);
	}
	
	public ArrayList<CeSentence> listAllFactSentences() {
		return listAllSentencesOfTypeWith(BuilderSentence.SENTYPE_FACT);		
	}

	public ArrayList<CeSentence> listAllRuleSentences() {
		return listAllSentencesOfTypeWith(BuilderSentence.SENTYPE_RULE);		
	}
	
	public ArrayList<CeSentence> listAllQuerySentences() {
		return listAllSentencesOfTypeWith(BuilderSentence.SENTYPE_QUERY);		
	}
	
	public ArrayList<CeSentence> listAllRuleOrQuerySentences() {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();
			
		result.addAll(listAllSentencesOfTypeWith(BuilderSentence.SENTYPE_RULE));
		result.addAll(listAllSentencesOfTypeWith(BuilderSentence.SENTYPE_QUERY));
		
		return result;
	}

	public ArrayList<CeSentence> listAllAnnotationSentences() {
		return listAllSentencesOfTypeWith(BuilderSentence.SENTYPE_ANNO);
	}

	public ArrayList<CeSentence> listAllCommandSentences() {
		return listAllSentencesOfTypeWith(BuilderSentence.SENTYPE_CMD);
	}

	private ArrayList<CeSentence> listAllSentencesOfTypeWith(int pType) {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();

		result.addAll(this.allValidSentences.getSentencesOfTypeAsList(pType));
		result.addAll(this.allInvalidSentences.getSentencesOfTypeAsList(pType));

		return result;
	}

	public ArrayList<CeInstance> listAllInstancesForConcept(CeConcept pConcept) {
		return getInstanceRepository().getAllInstancesForConcept(pConcept);
	}
	
	public ArrayList<CeInstance> listAllInstances() {
		return getInstanceRepository().listAllInstances();
	}
		
	public int countAllInstancesForConcept(CeConcept pConcept) {
		return getInstanceRepository().getInstanceCountForConcept(pConcept);
	}

	public ArrayList<CePropertyValue> getPropertyValuesFromPremise(ActionContext pAc, CeRationalePremise pPrem) {
		ArrayList<CePropertyValue> result = new ArrayList<CePropertyValue>();
		
		if (pPrem.hasPropertyDetails()) {
			String instName = pPrem.getInstanceName();
			CeInstance thisInst = getInstanceNamed(pAc, instName);
			
			String propName = pPrem.getPropertyName();
			
			if (thisInst != null) {
				CePropertyInstance thisPi = thisInst.getPropertyInstanceNamed(propName);
				
				String tgtVal = pPrem.getValue();
				
				if (thisPi != null) {
					for (CePropertyValue thisPv : thisPi.getPropertyValues()) {
						if (thisPv.getValue().equals(tgtVal)) {
							result.add(thisPv);
						}
					}
				}
			} else {
				reportWarning("Unable to get instance '" + instName + "' during premise processing for rationale", pAc);
			}
		}
		
		return result;
	}
	
	protected void removeUnusedSentencesAndSources(ActionContext pAc) {
		TreeMap<Integer, CeSentence> newSens = new TreeMap<Integer, CeSentence>();

		//First delete all the fact sentences and sources
		deleteAllFactAndAnnotationSentences();
		deleteAllSources();
		
		//Next recreate all fact sentences that are still referenced by any concept, instance or property
		for (CeConcept thisConcept : listAllConcepts()) {
			//Process sentences directly related to this concept
			for (CeSentence thisConcSen : thisConcept.listAllSentences()) {
				if ((thisConcSen.isFactSentence()) || (thisConcSen.isAnnotationSentence())) {
					newSens.put(new Integer(thisConcSen.getId()), thisConcSen);
				}
			}

			//Process properties and related sentences for this concept
			for (CeProperty thisProp : thisConcept.calculateAllProperties().values()) {
				for (CeSentence thisPropSen : thisProp.listAllSentences()) {
					if (thisPropSen.isFactSentence()) {
						newSens.put(new Integer(thisPropSen.getId()), thisPropSen);
					}
				}
			}			
		}

		//Process instances and related sentences
		for (CeInstance thisInst : getInstanceRepository().getAllInstances().values()) {
			for (CeSentence thisInstSen : thisInst.listAllSentences()) {
				if (thisInstSen.isFactSentence()) {
					CeSource relSrc = thisInstSen.getSource();
					if (relSrc.isInternalSource()) {
						//Only sentences for metamodel instances from internal sources should be saved
						newSens.put(new Integer(thisInstSen.getId()), thisInstSen);
					}
				}
			}
		}

		for (CeSentence newSen : newSens.values()) {
			saveSentence(pAc, newSen);
		}

		//Next recreate any sources that are referenced by any remaining sentence
		for (CeSentence thisSen : listAllSentences()) {
			CeSource thisSource = thisSen.getSource();
			saveSource(thisSource);
		}
		
		//Finally recreate any sentences that define source annotations
		//TODO: Need to decide how to do this
		for (CeSource thisSource : getAllSources().values()) {
			if (thisSource.listAnnotationSentences() != null) {
				for (CeSentence thisSen : thisSource.listAnnotationSentences()) {
					saveSentence(pAc, thisSen);
				}
			}
		}
	}
	
	private void deleteAllFactAndAnnotationSentences() {
		int[] typesToRemove;

		typesToRemove = new int[] {BuilderSentence.SENTYPE_FACT};
		this.allValidSentences.removeSentencesOfTypes(typesToRemove);

		typesToRemove = new int[] {BuilderSentence.SENTYPE_FACT, BuilderSentence.SENTYPE_ANNO};
		this.allInvalidSentences.removeSentencesOfTypes(typesToRemove);
	}

	private void deleteAllSources() {
		clearSources();
	}

	public void deleteAllInstancesForConcept(ActionContext pAc, CeConcept pConcept) {
		//Propagate on to the instance repository
		getInstanceRepository().deleteAllInstancesForConcept(pAc, pConcept);
	}

	public static void deleteExactInstancesForConcept(ActionContext pAc, CeConcept pConcept) {
		//Propogate on to the instance repository
		InstanceRepository.deleteExactInstancesForConcept(pAc, pConcept);
	}

	public void deleteInstance(ActionContext pAc, CeInstance pInst) {
		//Propogate on to the instance repository
		getInstanceRepository().deleteInstance(pInst);
		
		if (isReportDebug()) {
			reportDebug("Instance '" + pInst.getInstanceName() + "' has been deleted", pAc);
		}
	}

	public static void deleteConceptualModel(ActionContext pAc, CeConceptualModel pCm) {
		//TODO: Implement this
		reportError("Conceptual model deletion is not yet implemented (for conceptual model '" + pCm.getModelName() + "')", pAc);
	}

	public static void deleteConcept(ActionContext pAc, CeConcept pCon) {
		//TODO: Implement this
		reportError("Concept deletion is not yet implemented (for concept '" + pCon.getConceptName() + "')", pAc);
	}

	public static void deleteProperty(ActionContext pAc, CeProperty pProp) {
		//TODO: Implement this
		reportError("Property deletion is not yet implemented (for property '" + pProp.formattedFullPropertyName() + "')", pAc);
	}

	public static void deleteQuery(ActionContext pAc, CeQuery pQuery) {
		//TODO: Implement this
		reportError("Query deletion is not yet implemented (for query '" + pQuery.getQueryName() + "')", pAc);
	}

	public static void deleteRule(ActionContext pAc, CeRule pRule) {
		//TODO: Implement this
		reportError("Rule deletion is not yet implemented (for rule '" + pRule.getRuleName() + "')", pAc);
	}

	private void deleteSentence(ActionContext pAc, String pSenId) {
		//TODO: Improve the performance of this (by storing links to relevant concepts / instances on the sentence?)
		boolean anyFailures = false;
		boolean dontDelete = false;
		
		ArrayList<CeConcept> possibleCons = new ArrayList<CeConcept>();
		ArrayList<CeProperty> possibleProps = new ArrayList<CeProperty>();
		
		CeSentence tgtSen = getSentence(pSenId);
		
		if (tgtSen != null) {			
			//Now try to remove from each concept
			ArrayList<CeConcept> allCons = new ArrayList<CeConcept>();
			allCons.addAll(this.allConcepts.values());
			
			for (CeConcept thisCon : allCons) {
				if (thisCon.hasSentence(tgtSen)) {
					if (thisCon.hasPrimarySentence(tgtSen)) {
						if (!thisCon.hasOtherSentences(tgtSen)) {
							anyFailures = anyFailures || !deleteConcept(pAc, thisCon, tgtSen);
						} else {
							possibleCons.add(thisCon);
							dontDelete = true;
						}
					} else {
						thisCon.deleteSecondarySentence(tgtSen);
						possibleCons.add(thisCon);
						dontDelete = true;
					}
				}
			}
			
			//Now try to remove from each instance
			ArrayList<CeInstance> allInsts = new ArrayList<CeInstance>();
			allInsts.addAll(getInstanceRepository().getAllInstances().values());
			
			for (CeInstance thisInst : allInsts) {
				if (thisInst.deleteSentence(tgtSen)) {
					if (!thisInst.hasAnySentences()) {
						//Instances cannot fail to be deleted, so no need to affect anyFailures
						deleteInstance(pAc, thisInst);
					}
				}
			}
			
			//Now try to remove from each property
			ArrayList<CeProperty> allProps = new ArrayList<CeProperty>();
			for (CeProperty thisProp : this.allProperties) {
				allProps.add(thisProp);
			}
			
			for (CeProperty thisProp : allProps) {
				if (thisProp.hasSentence(tgtSen)) {
					if (!thisProp.hasOtherSentences(tgtSen)) {
						anyFailures = anyFailures || !deleteProperty(pAc, thisProp, tgtSen);
					} else {
						possibleProps.add(thisProp);
						dontDelete = true;
					}
				}
			}
			
			if (anyFailures) {
				tempWarning("Sentence '" + tgtSen.getId() + "' has not been deleted as associated concept/property/instance(s) could not be deleted");
			} else {
				if (dontDelete) {
					//There were no errors, but the sentence was not deleted from the property and/or concept but this can be safely done now
					for (CeConcept thisCon : possibleCons) {
						thisCon.deleteSentence(tgtSen);
					}

					for (CeProperty thisProp : possibleProps) {
						thisProp.deleteSentence(tgtSen);
					}
				}
				
				//Now remove from the source
				CeSource relSrc = tgtSen.getSource();
				
				if (relSrc != null) {
					relSrc.removePrimarySentence(tgtSen);
				}
				//Note that sources are not deleted if they have no remaining sentences (so no need to test and delete here)

				if (hasInvalidSentence(pSenId)) {
					removeInvalidSentenceWithId(pSenId);
				}
				if (hasValidSentence(pSenId)) {
					removeValidSentenceWithId(pSenId);
				}

				if (isReportDebug()) {
					reportDebug("Sentence '" + tgtSen.getId() + "' has been deleted", pAc);
				}
			}
		} else {
			reportError("No sentence with id '" + pSenId + "' was found, so nothing was deleted", pAc);
		}
	}

	public void deleteSingleSentence(ActionContext pAc, String pSenId) {
		clearTempWarnings();

		deleteSentence(pAc, pSenId);
		
		copyTempWarnings(pAc);
	}
	
	public void deleteSource(ActionContext pAc, String pSrcId) {
		CeSource tgtSrc = getSourceById(pSrcId);
		
		if (tgtSrc != null) {
			int beforeCount = -1;
			int afterCount = 0;
						
			while (beforeCount != afterCount) {
				clearTempWarnings();

				beforeCount = tgtSrc.listAllSentences().size();
				//Create a new ArrayList to prevent concurrentModification exceptions
				ArrayList<CeSentence> allSens = new ArrayList<CeSentence>();
				allSens.addAll(tgtSrc.listAllSentences());
				
				for (CeSentence thisSen : allSens) {
					deleteSentence(pAc, thisSen.formattedId());
				}
				
				afterCount = tgtSrc.listAllSentences().size();
			}
			
			copyTempWarnings(pAc);
			
			if (tgtSrc.listAllSentences().isEmpty()) {
				this.allSources.remove(tgtSrc.getId());
				if (isReportDebug()) {
					reportDebug("Source '" + tgtSrc.getId() + "' has been deleted", pAc);
				}
			} else {
				tempWarning("Source '" + pSrcId + "' was not deleted because it still contains sentences");
			}
		} else {
			reportError("No source with id '" + pSrcId + "' was found, so nothing was deleted", pAc);
		}		
	}
	
	private boolean deleteConcept(ActionContext pAc, CeConcept pCon, CeSentence pSen) {
		boolean result = false;
		
		//Clear the concept fragment names cache
		this.cachedConceptFragmentNames = new HashSet<String>();

		//Only delete the concept if there are no children and no instances
		if (pCon.hasAnyChildrenWithSentencesOtherThan(pSen)) {
			tempWarning("Concept '" + pCon.getConceptName() + "' was not deleted because it has children");
		} else if (pCon.hasAnyInstances(pAc)) {
			tempWarning("Concept '" + pCon.getConceptName() + "' was not deleted because there are instances of this concept");
		} else {
			result = true;
			pCon.deleteSentence(pSen);
			
			for (CeConcept parCon : pCon.getDirectParents()) {
				parCon.deleteDirectChild(pCon);
			}
			
			this.allConcepts.remove(pCon.identityKey());

			if (isReportDebug()) {
				reportDebug("Concept '" + pCon.getConceptName() + "' has been deleted", pAc);
			}
		}
		
		return result;
	}

	private boolean deleteProperty(ActionContext pAc, CeProperty pProp, CeSentence pSen) {
		//Only delete this property if there are no instances that use it
		boolean hasPropertyValues = false;
		boolean result = false;
		
		for (CeInstance thisInst : getInstanceRepository().getAllInstancesForConcept(pProp.getDomainConcept())) {
			if (!hasPropertyValues) {
				CePropertyInstance mainPi = thisInst.getPropertyInstanceForProperty(pProp);	
				hasPropertyValues = (mainPi != null);

				for (CeProperty thisProp : pProp.getInferredProperties()) {
					if (!hasPropertyValues) {
						CePropertyInstance infPi = thisInst.getPropertyInstanceForProperty(thisProp);	
						hasPropertyValues = (infPi != null);
					}
				}
			}
		}
		
		if (!hasPropertyValues) {
			//There are no property instances so delete the property
			result = true;
			pProp.deleteSentence(pSen);
			
			CeConcept domConcept = pProp.getDomainConcept();
			domConcept.deleteProperty(pProp);
			
			removeProperty(pProp);
			if (isReportDebug()) {
				reportDebug("Property '" + pProp.identityKey() + "' has been deleted", pAc);
			}

			for (CeProperty thisProp : pProp.getInferredProperties()) {
				domConcept = thisProp.getDomainConcept();
				domConcept.deleteProperty(thisProp);
				
				removeProperty(thisProp);
				if (isReportDebug()) {
					reportDebug("Inferred property '" + thisProp.identityKey() + "' has been deleted", pAc);
				}
			}
		} else {
			tempWarning("Property '" + pProp.identityKey() + "' was not deleted because there are instances that use this property");
		}
		
		return result;
	}
	
	private void clearTempWarnings() {
		this.tempWarnings = new ArrayList<String>();
	}
	
	private void tempWarning(String pText) {
		this.tempWarnings.add(pText);
	}
	
	private void copyTempWarnings(ActionContext pAc) {
		for (String thisWarning : this.tempWarnings) {
			reportWarning(thisWarning, pAc);
		}
	}

	public String getNextUid(ActionContext pAc, String pPrefix) {
		//Lazy initialisation
		if (this.uidMgr == null) {
			initialiseUidManager(pAc);
		}
		
		return sendUidRequest(pAc, pPrefix);
	}
	
	public String showNextUidWithoutIncrementing(ActionContext pAc) {
		//Lazy initialisation
		if (this.uidMgr == null) {
			initialiseUidManager(pAc);
		}
		
		return showNextUid();
	}

	public void setNextUidValueTo(ActionContext pAc, long pUidVal) {
		//Lazy initialisation
		if (this.uidMgr == null) {
			initialiseUidManager(pAc);
		}
		
		setNextUidTo(pUidVal);
	}

	public Properties getBatchOfUids(ActionContext pAc, long pBatchSize) {
		Properties result = null;
		
		//Lazy initialisation
		if (this.uidMgr == null) {
			initialiseUidManager(pAc);
		}
		
		if (this.uidMgr != null) {
			result = this.uidMgr.getBatchOfUids(pBatchSize);
		}
		
		return result;
	}
	
	public void resetUids(ActionContext pAc, String pStartingUid) {
		//Lazy initialisation - this does need to be done, otherwise any file based UID managers will not have their files deleted
		if (this.uidMgr == null) {
			initialiseUidManager(pAc);
		}
		
		this.uidMgr.resetUidCounter(pStartingUid);
	}
	
	private void initialiseUidManager(ActionContext pAc) {
		this.uidMgr = new UidManagerDefault();
		this.uidMgr.initialiseUidManager(isReportDebug(), isReportMicroDebug(), "", true, -1);
		this.uidMgr.initialiseUids(-1);

		handleMessagesFrom(pAc, this.uidMgr);
	}
	
	private String sendUidRequest(ActionContext pAc, String pPrefix) {
		String result = this.uidMgr.getNextUid(pPrefix);

		handleMessagesFrom(pAc, this.uidMgr);

		return result;
	}
	
	private String showNextUid() {
		return this.uidMgr.showNextUid();
	}

	private void setNextUidTo(long pUidVal) {
		this.uidMgr.setNextUidTo(pUidVal);
	}

	private static void handleMessagesFrom(ActionContext pAc, UidManager pUm) {
		String extraBit = " (generated on UID Manager)";

		//Process errors
		for (String thisErr : pUm.getErrors()) {
			reportError(thisErr + extraBit, pAc);
		}

		//Process warnings
		for (String thisWarn : pUm.getWarnings()) {
			reportWarning(thisWarn + extraBit, pAc);
		}

		//Process debugs
		if (isReportDebug()) {
			for (String thisDebug : pUm.getDebugs()) {
				reportDebug(thisDebug + extraBit, pAc);
			}
		}
		
		//Process micro debugs
		if (isReportMicroDebug()) {
			for (String thisDebug : pUm.getMicroDebugs()) {
				reportMicroDebug(thisDebug + extraBit, pAc);
			}
		}

		pUm.clearMessages();
	}
		
	public String getCachedStringValueLevel1(String pValue) {
		return getCachedStringValue(pValue, CACHELEVEL_1);
	}

	public String getCachedStringValueLevel2(String pValue) {
		return getCachedStringValue(pValue, CACHELEVEL_2);
	}

	public String getCachedStringValueLevel3(String pValue) {
		return getCachedStringValue(pValue, CACHELEVEL_3);
	}

	private String getCachedStringValue(String pValue, int pCacheLevel) {
		//This method has been tested against using string interning and
		//found to be just as effective from a memory usage and performance
		//perspective.  Given the issues with .intern() using PermGen space
		//in v6 (limited to 64MB by default) we have chosen to stick with this
		//custom implementation for now.
		String result = null;
		
		if (pCacheLevel <= this.cacheLevel) {
			result = this.allValues.get(pValue);

			if (result == null) {
				this.allValues.put(pValue, pValue);
				result = pValue;
			}
		} else {
			result = pValue;
		}
				
		return result;
	}

	public int getCachedStringCount() {
		return this.allValues.size();
	}

	public boolean doesSentenceTextAlreadyExist(ActionContext pAc, String pCeText) {
		boolean result = this.allValidSentences.containsText(pCeText, pAc);
		if (!result) {
			result = this.allInvalidSentences.containsText(pCeText, pAc);
		}
		if (result && isReportMicroDebug()) {
			reportMicroDebug("Ignoring sentence due to duplication: " + pCeText, pAc);
		}
		return result;
	}

	public CeSentence getSentence(String pSenId) {
		CeSentence result = this.allValidSentences.getSentence(pSenId);
		if (result == null) {
			result = this.allInvalidSentences.getSentence(pSenId);
		}
		return result;
	}

	public void executeTheseRules(ActionContext pAc, ArrayList<CeRule> pRules, int pMaxIterations, CeSource pSrc) {
		if (!pRules.isEmpty()) {
			String srcId = null;
			String srcDetail = null;
			
			if (pSrc == null) {
				srcId = "(none)";	//TODO: Abstract this
				srcDetail = "";
			} else {
				srcId = pSrc.getId();
				srcDetail = pSrc.getDetail();
			}
			
			updateCreatedThingsFrom(pAc);

			if (isReportMicroDebug()) {
				reportMicroDebug("Attempting to execute " + pRules.size() + " rules for Source " + srcId + " (" + srcDetail + ")", pAc);
			}

			int itCtr = 0;
			int newSens = -1;
//			int totalSens = 0;
			
			HashSet<String> genSentences = new HashSet<String>();
			StringBuilder sb = new StringBuilder();

			//Iterate through the rules and only stop if the max iterations is reached
			//or there are no new sentences generated
			while ((itCtr < pMaxIterations) && (newSens != 0)) {
				newSens = executeSpecifiedRules(pAc, genSentences, pRules, itCtr, true, false, false);
				
				++itCtr;

				//Save the sentences
				if (!genSentences.isEmpty()) {
					for (String thisSen : genSentences) {
						appendToSb(sb, thisSen);
						appendNewLineToSb(sb);
					}
				}
			}
			
			if (sb.length() > 1) {
				//Save the new sentences
				StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
				sa.loadSentencesFromAgent(sb, "ruleExecution for source " + srcId, "autoRunRules");	
	
				genSentences = new HashSet<String>();
			}
		}
	}
	
	private static int executeSpecifiedRules(ActionContext pAc, HashSet<String> pGenSentences, ArrayList<CeRule> pRules, int pIterationCounter, boolean pGenRat, boolean pDblRatSens, boolean pRestateSens) {
		int newOverallSentences = 0;
		int totalSentences = 0;
		int ruleCtr = 0;
		long startTime = System.currentTimeMillis();
		QueryExecutionManager qm = QueryExecutionManager.createUsing(pAc, false, null, null);

		for (CeRule thisRule : pRules) {
			int newSentencesForThisRule = 0;
			if (!thisRule.hasErrors()) {
				++ruleCtr;
				long ruleStart = System.currentTimeMillis();
				if (isReportMicroDebug()) {
					reportMicroDebug("[Start] Executing rule " + thisRule.getRuleName() + " (iteration=" + pIterationCounter + ")", pAc);
				}
				ContainerCeResult result = qm.executeRule(thisRule, pGenRat, pDblRatSens);

				ArrayList<String> allResults = result.getCeResults();
				totalSentences += allResults.size();

				//Initialise the sentence cache (huge performance improvement)
				thisRule.createSentenceLookup(pAc);

				for (String thisSen : allResults) {
					if (ContainerCeResult.isMultipleSentences(thisSen)) {
						for (String innerSen : ContainerCeResult.splitSentences(thisSen)) {
							if ((pRestateSens) || (!thisRule.doesSentenceTextAlreadyExist(pAc, innerSen))) {
								int beforeSize = pGenSentences.size();
								pGenSentences.add(innerSen + NL + NL);
								
								if (pGenSentences.size() > beforeSize) {
									++newSentencesForThisRule;
								}
							}
						}
					} else {
						if ((pRestateSens) || (!thisRule.doesSentenceTextAlreadyExist(pAc, thisSen))) {
							int beforeSize = pGenSentences.size();
							pGenSentences.add(thisSen + NL + NL);
							
							if (pGenSentences.size() > beforeSize) {
								++newSentencesForThisRule;
							}
						}
					}
				}

				//Clear the sentence cache
				thisRule.clearSentenceLookup();

				newOverallSentences += newSentencesForThisRule;
				long duration = System.currentTimeMillis() - ruleStart;

				if ((duration > 2000) || (isReportDebug())) {
					String ruleMsg = "[End] Executing rule " + thisRule.getRuleName() + " (results=" + result.getCeResults().size() + ", new=" + newSentencesForThisRule + ") time=" + duration;

					if (duration > 2000) {
						reportWarning("SLOW RULE: " + ruleMsg, pAc);
					} else {
						reportMicroDebug(ruleMsg, pAc);
					}
				}
			} else {
				reportError("Rule '" + thisRule.getRuleName() + "' has errors and was not executed", pAc);
			}
		}

		if (isReportMicroDebug()) {
			String summaryText = "";
			
			summaryText += "matched rules: " + ruleCtr + ", ";
			summaryText += "total sentences: " + totalSentences + ", ";
			summaryText += "new sentences: " + newOverallSentences + ", ";
			summaryText += "execution time: " + (System.currentTimeMillis() - startTime);
			reportMicroDebug(summaryText, pAc);
		}

		return newOverallSentences;
	}

	public static boolean isThisCeValid(ActionContext pAc, String pCeText) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
		ContainerSentenceLoadResult senStats = sa.validateCeSentence(pCeText);

		return (senStats.getInvalidSentenceCount() == 0);
	}

	public boolean isCeStoreEmpty() {
		return this.allConcepts.isEmpty();
	}

}
