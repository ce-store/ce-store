package com.ibm.ets.ita.ce.store;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.StaticCeRepository.ceMetamodel;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.bufferedReaderFromUrlWithApplicationTextHeader;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.calculateFullFilenameFrom;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.createFolderOnStartupIfNeeded;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.deleteFile;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.readTextFromFile;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.createFullUrlForRelativePath;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.doGarbageCollection;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.formattedDuration;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.formattedStats;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.isRelativePath;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportExecutionTiming;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportGeneralCountInformation;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportLatestUidValue;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportMemoryUsage;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.agent.CeAgent;
import com.ibm.ets.ita.ce.store.api.CEStore;
import com.ibm.ets.ita.ce.store.handler.QueryHandler;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerCommonValues;
import com.ibm.ets.ita.ce.store.model.container.ContainerSearchResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;
import com.ibm.ets.ita.ce.store.parsing.processor.ProcessorCe;

//TODO: sen_ceprop: what about rules?
//TODO: Review and set all property overrides

public class StoreActions implements CEStore {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = StoreActions.class.getName();
	private static final String PACKAGE_NAME = StoreActions.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final String SRCID_CONCMETAMODEL = "src_cm";
	private static final String STEPNAME_CONCMETAMODEL = "conceptualiseMetamodel";

	private static final int ACTION_RESET = 1;
	private static final int ACTION_EMPTY = 2;

	public static final int MODE_NORMAL = 1;
	public static final int MODE_VALIDATE = 2;

	private StoreConfig conf = null;
	private ActionContext ac = null;

	private StoreActions() {
		//This is private to ensure that new instances can only be created via the various static methods
	}

	public static StoreActions createUsingDefaultConfig(ActionContext pAc) {
		StoreActions newSa = new StoreActions();

		newSa.ac = pAc;
		newSa.conf = pAc.getCeConfig();

		return newSa;
	}

	public static StoreActions createUsingSpecificConfig(StoreConfig pCc, ActionContext pAc) {
		StoreActions newSa = new StoreActions();

		newSa.conf = pCc;
		newSa.ac = pAc;

		return newSa;
	}

	public static void initialiseEnvironment(ActionContext pAc) {
		initialiseFolders(pAc);
	}

	private static void initialiseFolders(ActionContext pAc) {
		StoreConfig conf = pAc.getCeConfig();
		String rootFolder = conf.getRootFolder();

		if ((rootFolder == null) || (rootFolder.isEmpty())) {
			reportError("Unable to initialise CE Store as the mandatory 'rootFolder' property has not been specified", pAc);
		} else {
			// Create all of the required folders (if not already created, and if authorised)
			createFolderOnStartupIfNeeded(pAc, rootFolder);
			createFolderOnStartupIfNeeded(pAc, conf.getLogPath());
			createFolderOnStartupIfNeeded(pAc, conf.getTempPath());
			createFolderOnStartupIfNeeded(pAc, conf.getGenPath());
			createFolderOnStartupIfNeeded(pAc, conf.getPersistPath());
		}
	}

	@Override
	public ContainerSentenceLoadResult resetStore(String pStartingUid) {
		ContainerSentenceLoadResult result = null;
		CeSource lastCurrent = this.ac.getCurrentSource();

		this.ac.getModelBuilder().resetUids(this.ac, pStartingUid);
		result = performAndReportAction(ACTION_RESET);

		if (lastCurrent != null) {
			renumberExistingSources(lastCurrent);
		}

		return result;
	}
	
	private void renumberExistingSources(CeSource pLastSource) {
		ArrayList<CeSource> listToRenumber = new ArrayList<CeSource>();
		
		addParentSourcesTo(pLastSource, listToRenumber);
		
		for (CeSource thisSrc : listToRenumber) {
			CeSource.renumberThisSource(thisSrc);
			
			//Also save the renumbered source to the list of all sources
			this.ac.getModelBuilder().saveSource(thisSrc);
		}
	}

	private void addParentSourcesTo(CeSource pSrc, ArrayList<CeSource> pSrcList) {
		CeSource parSrc = pSrc.getParentSource();

		if (parSrc != null) {
			addParentSourcesTo(parSrc, pSrcList);
		}
		
		pSrcList.add(pSrc);
	}

	@Override
	public ContainerSentenceLoadResult emptyInstances() {
		return performAndReportAction(ACTION_EMPTY);
	}

	public ContainerSentenceLoadResult runAgent(String pAgentName, String pAgentConName) {
		final String METHOD_NAME = "runAgent";
		
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("runAgent:" + pAgentName);
		CeAgent agentInstance = null;

		CeInstance configInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, pAgentName);

		if (configInst != null) {
			if (!configInst.isConceptNamed(this.ac, pAgentConName)) {
				reportWarning("Request to run agent '" + pAgentName + "' will be carried out, but an incompatible agent concept named '" + pAgentConName + "' was specified", this.ac);
			}

			CePropertyInstance cnProp = configInst.getPropertyInstanceNamed(CeAgent.PNAME_CLASSNAME);
			if (cnProp != null) {
				String agentClassName = cnProp.getSingleOrFirstValue();
				try {
					@SuppressWarnings("unchecked")
					// The user must specify a valid subclass of CeAgent
					Class<CeAgent> agentClass = (Class<CeAgent>) Class.forName(agentClassName);

					try {
						agentInstance = agentClass.newInstance();
						agentInstance.runAgent(this.ac, configInst);
						senStats.updateFrom(createSentenceStatsFrom(agentInstance));
					} catch (IllegalAccessException e) {
						reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
					} catch (InstantiationException e) {
						reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
					}
				} catch (ClassNotFoundException e) {
					reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
				}
			} else {
				reportError("Class name property not found for agent configuration named '" + pAgentName + "'", this.ac);
			}
		} else {
			reportError("Unable to locate agent configuration (instance) named '" + pAgentName + "'", this.ac);
		}

		return senStats;
	}

	@Override
	public ContainerSentenceLoadResult loadSentencesFromUrl(String pUrl, String pSrcName) {
		final String METHOD_NAME = "loadSentencesFromUrl";

		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("loadSentencesFromUrl:" + pUrl);
		String tgtUrl = "";

		if (isRelativePath(pUrl)) {
			tgtUrl = createFullUrlForRelativePath(this.ac, pUrl);

			if (isReportMicroDebug()) {
				reportMicroDebug("Converting relative path (" + pUrl + ") to full path using default location: " + tgtUrl, this.ac);
			}
		} else {
			tgtUrl = pUrl;
		}

		long startTime = System.currentTimeMillis();
		BufferedReader br = bufferedReaderFromUrlWithApplicationTextHeader(this.ac, tgtUrl);

		if (br != null) {
			if (isReportMicroDebug()) {
				  reportMicroDebug("CE text retrieved from URL '" + tgtUrl + "'", this.ac);
			}

			ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
			CeSource oldCurr = this.ac.getCurrentSource();				
			CeSource tgtSrc = null;

			if (pSrcName != null) {
				tgtSrc = this.ac.getModelBuilder().getOrCreateSourceById(this.ac, pSrcName, pSrcName);
			} else {
				tgtSrc = CeSource.createNewUrlSource(this.ac, pUrl, null);
			}

			tgtSrc.setParentSource(this.ac, oldCurr);
			procCe.processNormalSentencesFromReaderWithExistingSource(br, CeSource.SRCTYPE_ID_URL, tgtUrl, "", startTime, MODE_NORMAL, tgtSrc);
			this.ac.setCurrentSource(oldCurr);
		
			try {
				br.close();
			} catch (IOException e) {
				reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
			}
		} else {
			reportWarning("No CE sentences returned from URL  (buffered reader is null) '" + tgtUrl + "'", this.ac);
		}

		return senStats;
	}

	@Override
	public ContainerSentenceLoadResult loadSentencesFromForm(String pCeText, String pFormName, int pMode) {
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("loadSentencesFromForm:" + pFormName);
		long startTime = System.currentTimeMillis();

		if (pCeText != null) {
			if (!pCeText.isEmpty()) {
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				procCe.processNormalSentencesFromString(pCeText, CeSource.SRCTYPE_ID_FORM, pFormName, "", startTime, pMode);
			} else {
				reportError("No CE sentences found in supplied string (empty string) [" + pFormName + "]", this.ac);
			}
		} else {
			reportError("No CE sentences found in supplied string (null) [" + pFormName + "]", this.ac);
		}

		return senStats;
	}

	public ProcessorCe parseSentencesFromForm(String pCeText, String pFormName, int pMode) {
		ProcessorCe procCe = null;

		long startTime = System.currentTimeMillis();

		if (pCeText != null) {
			if (!pCeText.isEmpty()) {
				ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("parseSentencesFromForm");
				procCe = new ProcessorCe(this.ac, false, senStats);
				procCe.processNormalSentencesFromString(pCeText, CeSource.SRCTYPE_ID_FORM, pFormName, "", startTime, pMode);
			} else {
				reportError("No CE sentences found in supplied string (empty string) [" + pFormName + "]", this.ac);
			}
		} else {
			reportError("No CE sentences found in supplied string (null) [" + pFormName + "]", this.ac);
		}

		return procCe;
	}

	@Override
	public ContainerSentenceLoadResult loadSentencesFromFormForSpecifiedSource(String pCeText, CeSource pTgtSrc, int pMode) {
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("loadSentencesFromFormForSpecifiedSource(" + pMode + "):" + pTgtSrc.getId());
		long startTime = System.currentTimeMillis();

		if (pCeText != null) {
			if (!pCeText.isEmpty()) {
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				procCe.processNormalSentencesFromStringForSpecifiedSource(pCeText, pTgtSrc, "", startTime, pMode);
			} else {
				reportError("No CE sentences found in supplied string (empty string) [" + pTgtSrc.getId() + "]", this.ac);
			}
		} else {
			reportError("No CE sentences found in supplied string (null) [" + pTgtSrc.getId() + "]", this.ac);
		}

		return senStats;
	}

	@Override
	public ContainerSentenceLoadResult validateCeSentence(String pCeText) {
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("validateCeSentence");
		this.ac.markAsValidatingOnly();

		if (pCeText != null) {
			if (!pCeText.isEmpty()) {
				senStats.updateFrom(loadSentencesFromForm(pCeText, "validate", MODE_VALIDATE));
			} else {
				reportError("Unable to validate CE sentence (empty string)", this.ac);
			}
		} else {
			reportError("Unable to validate CE sentence (null)", this.ac);
		}

		//DSB 04/03/2016
		//Important to reset the validate flag on the action context otherwise
		//it is still set when saving the sentence so nothing is saved
		this.ac.markAsNotValidating();

		return senStats;
	}

	public ProcessorCe parseCeSentence(String pCeText) {
		ProcessorCe result = null;

		if (pCeText != null) {
			if (!pCeText.isEmpty()) {
				result = parseSentencesFromForm(pCeText, "validate", MODE_VALIDATE);				
			} else {
				reportError("Unable to validate CE sentence (empty string)", this.ac);
			}
		} else {
			reportError("Unable to validate CE sentence (null)", this.ac);
		}

		return result;
	}

	@Override
	public ContainerSentenceLoadResult loadSentencesFromFile(String pFullyQualifiedFilename) {
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("loadSentencesFromFile:" + pFullyQualifiedFilename);
		String correctFilename = calculateFullFilenameFrom(this.ac, pFullyQualifiedFilename);

		long startTime = System.currentTimeMillis();
		String ceText = readTextFromFile(this.ac, correctFilename);

		if (ceText != null) {
			if (!ceText.isEmpty()) {
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				procCe.processNormalSentencesFromString(ceText, CeSource.SRCTYPE_ID_FILE, correctFilename, "", startTime, StoreActions.MODE_NORMAL);
			} else {
				reportError("No CE sentences found in supplied string (empty string) [" + correctFilename + "]", this.ac);
			}
		} else {
			reportError("No CE sentences found in supplied string (null) [" + correctFilename + "]", this.ac);
		}

		return senStats;
	}

	public ContainerSentenceLoadResult loadSentencesFromQuery(String pQueryOrQueryName) {
		long startTime = System.currentTimeMillis();		
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("loadSentencesFromQuery:" + pQueryOrQueryName);
		ContainerCeResult ceResult = null;
		CeQuery tgtQuery = this.ac.getModelBuilder().getQueryNamed(pQueryOrQueryName);

		if (tgtQuery == null) {
			//No query was found with that name so see if this looks like a query and try to run it
			ceResult = executeUserSpecifiedCeQuery(pQueryOrQueryName, false, null, null);
		} else {
			ceResult = executeUserSpecifiedCeQueryByName(pQueryOrQueryName, false, null, null);
		}

		if (ceResult != null) {
			String ceText = "";

			ceResult.trimToRemoveCe();

			int ctr = 0;
			for (ArrayList<String> thisRow : ceResult.getResultRows()) {
				ctr++;
				if (thisRow.size() == 1) {
					ceText += thisRow.get(0);
				} else {
					reportError("Unexpected number of columns (" + Integer.toString(thisRow.size()) + ") in result row " + Integer.toString(ctr) + " for query: " + pQueryOrQueryName, this.ac);
				}
			}

			if (!ceText.isEmpty()) {
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				procCe.processNormalSentencesFromString(ceText, CeSource.SRCTYPE_ID_QUERY, pQueryOrQueryName, "", startTime, StoreActions.MODE_NORMAL);
			} else {
				reportError("No CE sentences found after running query (empty string) [" + pQueryOrQueryName + "]", this.ac);
			}
		} else {
			reportWarning("No results returned for CE query '" + pQueryOrQueryName + "'", this.ac);
		}

		return senStats;
	}

	ContainerSentenceLoadResult loadSentencesFromInternal(StringBuilder pSb, String pStepName) {
		long startTime = System.currentTimeMillis();
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("loadSentencesFromInternal:" + pStepName);

		if (pSb != null) {
			if (pSb.length() > 0) {
				CeSource tempSource = this.ac.getCurrentSource();
				CeSource mmSrc = CeSource.createNewInternalSource(this.ac, STEPNAME_CONCMETAMODEL, SRCID_CONCMETAMODEL);
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				procCe.processNormalSentencesFromSbWithExistingSource(pSb, CeSource.SRCTYPE_ID_INTERNAL, pStepName, "", startTime, StoreActions.MODE_NORMAL, mmSrc);
				mmSrc.setParentSource(this.ac, tempSource);
				this.ac.setCurrentSource(tempSource);
			} else {
				reportError("No CE sentences found in supplied string (empty string) [" + pStepName + "]", this.ac);
			}
		} else {
			reportError("No CE sentences found in supplied string (null) [" + pStepName + "]", this.ac);
		}

		return senStats;
	}

	ContainerSentenceLoadResult loadSentencesFromAgent(StringBuilder pSb, String pDetails, String pExtraInfo) {
		long startTime = System.currentTimeMillis();
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("loadSentencesFromAgent:" + pDetails);

		if (pSb != null) {
			if (pSb.length() > 0) {
				CeSource oldCurrentSource = this.ac.getCurrentSource();
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				procCe.processNormalSentencesFromSb(pSb, CeSource.SRCTYPE_ID_AGENT, pDetails, pExtraInfo, startTime, StoreActions.MODE_NORMAL);
				this.ac.getCurrentSource().setParentSource(this.ac, oldCurrentSource);
				this.ac.setCurrentSource(oldCurrentSource);
			} else {
				reportError("No CE sentences found in supplied string (empty string) [" + pDetails + "]", this.ac);
			}
		} else {
			reportError("No CE sentences found in supplied string (null) [" + pDetails + "]", this.ac);
		}

		return senStats;
	}

	//@Override
	public void updateConfig(String pConfigName, String pConfigVal) {
		String targetVal = "";

		if (pConfigVal != null) {
			targetVal = pConfigVal;
		} else {
			targetVal = "";
		}

		if (pConfigName != null) {
			this.conf.setPropertyByName(this.ac, pConfigName, pConfigVal);
		} else {
			reportError("Cannot update config value (" + targetVal	+ ") as no config property name is specified", this.ac);
		}
	}

	@Override
	public ArrayList<CeConcept> listAllConcepts() {
		return new ArrayList<CeConcept>(this.ac.getModelBuilder().listAllConcepts());
	}

	@Override
	public ArrayList<CeConcept> listAllChildConceptsFor(String pConceptName) {
		ArrayList<CeConcept> childConcepts = null;

		if ((pConceptName == null) || (pConceptName.isEmpty())) {
			reportError("No concept name specified when requesting all child concepts", this.ac);
			childConcepts = new ArrayList<CeConcept>();
		} else {
			QueryHandler qh = new QueryHandler(this.ac);
			childConcepts = qh.listAllChildConceptsFor(pConceptName);
		}

		return childConcepts;
	}

	@Override
	public ArrayList<CeConcept> listDirectChildConceptsFor(String pConceptName) {
		ArrayList<CeConcept> childConcepts = null;

		if ((pConceptName == null) || (pConceptName.isEmpty())) {
			reportError("No concept name specified when requesting direct child concepts", this.ac);
			childConcepts = new ArrayList<CeConcept>();
		} else {
			QueryHandler qh = new QueryHandler(this.ac);
			childConcepts = qh.listDirectChildConceptsFor(pConceptName);
		}

		return childConcepts;
	}

	@Override
	public ArrayList<CeConcept> listAllParentConceptsFor(String pConceptName) {
		ArrayList<CeConcept> parentConcepts = null;

		if ((pConceptName == null) || (pConceptName.isEmpty())) {
			reportError("No concept name specified when requesting all parent concepts", this.ac);
			parentConcepts = new ArrayList<CeConcept>();
		} else {
			QueryHandler qh = new QueryHandler(this.ac);
			parentConcepts = qh.listAllParentConceptsFor(pConceptName);
		}

		return parentConcepts;
	}

	@Override
	public ArrayList<CeConcept> listDirectParentConceptsFor(String pConceptName) {
		ArrayList<CeConcept> parentConcepts = null;

		if ((pConceptName == null) || (pConceptName.isEmpty())) {
			reportError("No concept name specified when requesting direct parent concepts", this.ac);
			parentConcepts = new ArrayList<CeConcept>();
		} else {
			QueryHandler qh = new QueryHandler(this.ac);
			parentConcepts = qh.listDirectParentConceptsFor(pConceptName);
		}

		return parentConcepts;
	}

	@Override
	public int countInstances(String pConceptName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.countInstances(pConceptName);
	}

	@Override
	public ArrayList<CeInstance> listInstances(String pConceptName) {
		return this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, pConceptName);
	}

	@Override
	public ArrayList<CeSource> listAllSources() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllSources();
	}

	@Override
	public CeSource getSourceDetailsFor(String pSrcId) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.getSourceDetailsFor(pSrcId);
	}

	@Override
	public ArrayList<CeConceptualModel> listAllConceptualModels() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllConceptualModels();
	}

	@Override
	public ArrayList<CeSentence> listAllSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllSentences();
	}

	@Override
	public ArrayList<CeSentence> listAllModelSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllModelSentences();
	}

	@Override
	public ArrayList<CeSentence> listAllNormalFactSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllNormalFactSentences();
	}

	@Override
	public ArrayList<CeSentence> listAllQualifiedFactSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllQualifiedFactSentences();
	}

	@Override
	public ArrayList<CeSentence> listAllRuleSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllRuleSentences();
	}

	@Override
	public ArrayList<CeSentence> listAllQuerySentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllQuerySentences();
	}

	@Override
	public ArrayList<CeSentence> listAllAnnotationSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllAnnotationSentences();
	}

	@Override
	public ArrayList<CeSentence> listAllCommandSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllCommandSentences();
	}

	@Override
	public ArrayList<CeSentence> listAllValidSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllValidSentences();
	}

	@Override
	public ArrayList<CeSentence> listAllInvalidSentences() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllInvalidSentences();
	}

	@Override
	public ArrayList<CeSentence> listPrimarySentencesForInstance(String pInstName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listPrimarySentencesForInstance(pInstName);
	}

	@Override
	public ArrayList<CeSentence> listSecondarySentencesForInstance(String pInstName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listSecondarySentencesForInstance(pInstName);
	}

	@Override
	public ArrayList<ArrayList<CeSentence>> listAllSentencesForInstance(String pInstName) {
		QueryHandler qh = new QueryHandler(this.ac);
		ArrayList<ArrayList<CeSentence>> overallList = new ArrayList<ArrayList<CeSentence>>();

		ArrayList<CeSentence> priSenList = qh.listPrimarySentencesForInstance(pInstName);
		ArrayList<CeSentence> secSenList = qh.listSecondarySentencesForInstance(pInstName);

		overallList.add(priSenList);
		overallList.add(secSenList);

		return overallList;
	}

	@Override
	public ArrayList<CeSentence> listPrimarySentencesForConcept(String pConceptName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listPrimarySentencesForConcept(pConceptName);
	}

	@Override
	public ArrayList<CeSentence> listSecondarySentencesForConcept(String pConceptName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listSecondarySentencesForConcept(pConceptName);
	}

	@Override
	public ArrayList<ArrayList<CeSentence>> listAllSentencesForConcept(String pConceptName) {
		QueryHandler qh = new QueryHandler(this.ac);
		ArrayList<ArrayList<CeSentence>> overallList = new ArrayList<ArrayList<CeSentence>>();

		ArrayList<CeSentence> priSenList = qh.listPrimarySentencesForConcept(pConceptName);
		ArrayList<CeSentence> secSenList = qh.listSecondarySentencesForConcept(pConceptName);

		overallList.add(priSenList);
		overallList.add(secSenList);

		return overallList;
	}

	@Override
	public ArrayList<CeSentence> listAllSentencesForSource(String pSourceId) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllSentencesForSource(pSourceId);
	}

	@Override
	public ArrayList<CeRationaleReasoningStep> listAllRationale() {
		return this.ac.getModelBuilder().getAllReasoningSteps();
	}

	@Override
	public ArrayList<CeRationaleReasoningStep> listRationaleForRule(String pRuleName) {
		return this.ac.getModelBuilder().getReasoningStepsForRule(pRuleName);
	}

	@Override
	public ArrayList<CeRationaleReasoningStep> listRationaleForSentence(CeSentence pSen) {
		return this.ac.getModelBuilder().getReasoningStepsForSentence(pSen);
	}

	@Override
	public ArrayList<CeRationaleReasoningStep> listRationaleForConcept(String pConName, boolean pCheckPremise) {
		return this.ac.getModelBuilder().getReasoningStepsForConcept(pConName, pCheckPremise);
	}

	@Override
	public ArrayList<CeRationaleReasoningStep> listRationaleForProperty(String pPropName, boolean pCheckPremise) {
		return this.ac.getModelBuilder().getReasoningStepsForProperty(pPropName, pCheckPremise);
	}

	@Override
	public ArrayList<CeRationaleReasoningStep> listRationaleForInstance(String pInstName, boolean pCheckPremise) {
		return this.ac.getModelBuilder().getReasoningStepsForInstance(pInstName, pCheckPremise);
	}

	@Override
	public ArrayList<CeRationaleReasoningStep> listRationaleForPropertyValue(String pInstName, String pPropName, String pValue, boolean pCheckPremise) {
		return this.ac.getModelBuilder().getReasoningStepsForPropertyValue(pInstName, pPropName, pValue, pCheckPremise);
	}

	@Override
	public CeQuery getQueryDetails(String pName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.getQueryDetails(pName);
	}

	@Override
	public CeRule getRuleDetails(String pName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.getRuleDetails(pName);
	}

	@Override
	public ArrayList<CeInstance> listAllInstanceDetails(CeConcept pTargetConcept) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllInstanceDetails(pTargetConcept);
	}

	@Override
	public ArrayList<CeInstance> listAllExactInstanceDetails(CeConcept pTargetConcept) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllExactInstanceDetails(pTargetConcept);
	}

	@Override
	public CeConcept getConceptDetails(String pConceptName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.getConceptDetails(pConceptName);
	}

	@Override
	public CeConceptualModel getConceptualModelDetails(String pModelName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.getConceptualModelDetails(pModelName);
	}

	@Override
	public ArrayList<CeProperty> listProperties(String pDomainName, String pRangeName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listProperties(pDomainName, pRangeName);
	}

	@Override
	public ArrayList<CeQuery> listAllQueries() {
		QueryHandler qh = new QueryHandler(this.ac);
		return new ArrayList<CeQuery>(qh.listAllQueries().values());
	}

	@Override
	public ArrayList<CeRule> listAllRules() {
		QueryHandler qh = new QueryHandler(this.ac);
		return new ArrayList<CeRule>(qh.listAllRules().values());
	}

	@Override
	public ArrayList<ArrayList<String>> listReferences(String pInstName) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listAllReferencesFor(pInstName);
	}

	@Override
	public ArrayList<ContainerSearchResult> keywordSearch(String pTerms, String[] pConceptNames, String[] pPropertyNames, boolean pCaseSensitive) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.keywordSearch(pTerms, pConceptNames, pPropertyNames, pCaseSensitive);
	}

	//@Override
	public ArrayList<ContainerCommonValues> commonPropertyValues(CeProperty pProp) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listCommonPropertyValues(pProp);
	}

	@Override
	public ContainerCeResult executeUserSpecifiedCeQuery(String pCeQuery, String pStartTs, String pEndTs) {
		this.ac.markAsExecutingQueryOrRule(true);
		
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.executeUserSpecifiedCeQuery(pCeQuery, false, pStartTs, pEndTs);
	}

	@Override
	public ContainerCeResult executeUserSpecifiedCeQuery(String pCeQuery, boolean pSuppressCeColumn, String pStartTs, String pEndTs) {
		this.ac.markAsExecutingQueryOrRule(true);
		
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.executeUserSpecifiedCeQuery(pCeQuery, pSuppressCeColumn, pStartTs, pEndTs);
	}

	@Override
	public ContainerCeResult executeUserSpecifiedCeQueryByName(String pCeQueryName, String pStartTs, String pEndTs) {
		this.ac.markAsExecutingQueryOrRule(true);
		
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.executeUserSpecifiedCeQueryByName(pCeQueryName, pStartTs, pEndTs);
	}

	@Override
	public ContainerCeResult executeUserSpecifiedCeQueryByName(String pCeQueryName, boolean pSuppressCeColumn, String pStartTs, String pEndTs) {
		this.ac.markAsExecutingQueryOrRule(true);
		
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.executeUserSpecifiedCeQueryByName(pCeQueryName, pSuppressCeColumn, pStartTs, pEndTs);
	}

	@Override
	public ContainerCeResult executeUserSpecifiedCeRule(String pCeRule, String pStartTs, String pEndTs) {
		this.ac.markAsExecutingQueryOrRule(true);

		QueryHandler qh = new QueryHandler(this.ac);
		ContainerCeResult result = qh.executeUserSpecifiedCeRule(pCeRule, pStartTs, pEndTs, false);

		String genCe = result.getAllGeneratedCeText();
		
		if (ContainerCeResult.isMultipleSentences(genCe)) {
			String newCe = "";

			for (String thisSen : ContainerCeResult.splitSentences(genCe)) {
				newCe += thisSen;
			}
			genCe = newCe;
		}

		if ((genCe != null) && (!genCe.isEmpty())) {
			loadSentencesFromForm(genCe, "User specified rule: " + result.getTargetQueryName(), StoreActions.MODE_NORMAL);
		}

		return result;
	}

	@Override
	public ContainerCeResult executeUserSpecifiedCeRuleByName(String pCeRuleName, String pFormat, String pStartTs, String pEndTs) {
		final String METHOD_NAME = "executeUserSpecifiedCeRuleByName";

		long sTime = System.currentTimeMillis();
		this.ac.markAsExecutingQueryOrRule(true);

		QueryHandler qh = new QueryHandler(this.ac);
		ContainerCeResult result = qh.executeUserSpecifiedCeRuleByName(pCeRuleName, pStartTs, pEndTs, false);

		reportExecutionTiming(this.ac, sTime, "[-2.a] executeUserSpecifiedCeRuleByName", CLASS_NAME, METHOD_NAME);

		String genCe = result.getAllGeneratedCeText();
		
		if (ContainerCeResult.isMultipleSentences(genCe)) {
			String newCe = "";

			for (String thisSen : ContainerCeResult.splitSentences(genCe)) {
				newCe += thisSen;
			}
			genCe = newCe;
		}

		reportExecutionTiming(this.ac, sTime, "[-2.b] executeUserSpecifiedCeRuleByName", CLASS_NAME, METHOD_NAME);

		if ((genCe != null) && (!genCe.isEmpty())) {
			loadSentencesFromForm(genCe, "User specified rule (by name): " + result.getTargetQueryName(), StoreActions.MODE_NORMAL);
		}

		reportExecutionTiming(this.ac, sTime, "[-2.c] executeUserSpecifiedCeRuleByName", CLASS_NAME, METHOD_NAME);

		return result;
	}

	@Override
	public ContainerSentenceLoadResult executeInferenceRules() {
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("executeInferenceRules");

		this.ac.markAsExecutingQueryOrRule(true);
		long sTime = System.currentTimeMillis();
		int newSentences = 0;
		int lastSentences = -1;
		int totalSentences = 0;
		int maxIterations = 10;
		int iterationCounter = 0;

		//Iterate through the rules and only stop if the max iterations is reached
		//or there are no new sentences generated
		while ((iterationCounter < maxIterations) && (newSentences > lastSentences)) {
			lastSentences = newSentences;
			newSentences = 0;
			QueryHandler qh = new QueryHandler(this.ac);
			String genCe = "";

			for (CeRule thisRule : this.ac.getModelBuilder().getAllRules().values()) {
				ContainerCeResult result = qh.executeUserSpecifiedCeRuleByName(thisRule.getQueryName(), null, null, false);
				//TODO: Ensure that only new sentences are saved, and that only one source is created
				genCe += result.getAllGeneratedCeText();
				newSentences += result.getAllRows().size();
			}

			if (newSentences > lastSentences) {
				if (!genCe.isEmpty()) {
					senStats.updateFrom(loadSentencesFromForm(genCe, "Execute all rules (manual)", StoreActions.MODE_NORMAL));
				}
			}
			totalSentences += (newSentences - lastSentences);
			++iterationCounter;
		}

		if (isReportMicroDebug()) {
			reportMicroDebug("Total of " + Integer.toString(totalSentences) + " new sentences generated by " + Integer.toString(iterationCounter) + " iterations of the rules", this.ac);
		}

		senStats.setExecutionTime(System.currentTimeMillis() - sTime);
		
		return senStats;
	}

	@Override
	public ArrayList<CeConcept> listShadowConcepts() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listShadowConcepts();
	}

	@Override
	public ArrayList<CeInstance> listShadowInstances() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listShadowInstances();
	}

	@Override
	public ArrayList<CeInstance> listUnreferencedInstances(boolean pIgnoreMetaModel) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listUnreferencedInstances(pIgnoreMetaModel);
	}

	//@Override
	public TreeMap<CeInstance, ArrayList<CeConcept>> listDiverseConceptInstances() {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.listDiverseConceptInstances();
	}

	@Override
	public void showStoreStatistics() {
		reportLatestUidValue(this.ac);
		reportMemoryUsage(this.ac);
		reportGeneralCountInformation(this.ac);
	}

	@Override
	public void runStoreStatistics() {
		//Do garbage collection
		doGarbageCollection(this.ac);

		reportLatestUidValue(this.ac);
		reportMemoryUsage(this.ac);
		reportGeneralCountInformation(this.ac);
	}

	@Override
	public void deleteAllInstancesForConceptNamed(String pConceptName) {
		CeConcept tgtConcept = this.ac.getModelBuilder().getConceptNamed(this.ac, pConceptName);

		if (tgtConcept != null) {
			this.ac.getModelBuilder().deleteAllInstancesForConcept(this.ac, tgtConcept);
		} else {
			reportError("Unable to locate concept named '" + pConceptName + "', so no instances have been deleted", this.ac);
		}
	}

	@Override
	public void deleteInstanceNamed(String pInstName) {
		CeInstance tgtInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, pInstName);
		//TODO: What about sentences etc?  Should they be deleted too?

		if (tgtInst != null) {
			this.ac.getModelBuilder().deleteInstance(this.ac, tgtInst);
		} else {
			reportError("Unable to locate instance named '" + pInstName + "', so it has not been deleted", this.ac);
		}
	}

	@Override
	public ContainerSentenceLoadResult deleteSentencesFromSourceById(String pSrcId) {
		this.ac.getModelBuilder().deleteSource(this.ac, pSrcId);

		//TODO: Should I return senStats (perhaps negative?) for this call?
		return ContainerSentenceLoadResult.createWithZeroValues("deleteSentencesFromSourceById:" + pSrcId);
	}

	@Override
	public ContainerSentenceLoadResult deleteSentencesFromSourceByAgentId(String pSrcAgentId) {
		ArrayList<CeSource> tgtSrcs = this.ac.getModelBuilder().getSourcesByAgentInstanceName(pSrcAgentId);

		if ((tgtSrcs != null) && (!tgtSrcs.isEmpty())) {
			if (tgtSrcs.size() > 1) {
				if (isReportDebug()) {
					reportDebug("More than one source (" + tgtSrcs.size() + ") was created by agent id '" + pSrcAgentId + "'.  All have been deleted.", this.ac);
				}
			}

			for (CeSource thisSrc : tgtSrcs) {
				this.ac.getModelBuilder().deleteSource(this.ac, thisSrc.getId());
			}
		} else {
			reportError("No source with agent id '" + pSrcAgentId + "' could not be located, so nothing has been deleted", this.ac);
		}

		//TODO: Should I return senStats (perhaps negative?) for this call?
		return ContainerSentenceLoadResult.createWithZeroValues("deleteSentencesFromSourceByAgentId:" + pSrcAgentId);
	}

	@Override
	public ContainerSentenceLoadResult deleteSentencesFromSourceByName(String pSrcName) {
		ArrayList<CeSource> tgtSrcs = this.ac.getModelBuilder().getSourcesByDetail(pSrcName);

		if ((tgtSrcs != null) && (!tgtSrcs.isEmpty())) {
			if (tgtSrcs.size() > 1) {
				if (isReportDebug()) {
					reportDebug("More than one source (" + tgtSrcs.size() + ") was found with the name '" + pSrcName + "'.  All have been deleted.", this.ac);
				}
			}

			for (CeSource thisSrc : tgtSrcs) {
				this.ac.getModelBuilder().deleteSource(this.ac, thisSrc.getId());
			}
		} else {
			reportError("No source with name '" + pSrcName + "' could not be located, so nothing has been deleted", this.ac);
		}

		//TODO: Should I return senStats (perhaps negative?) for this call?
		return ContainerSentenceLoadResult.createWithZeroValues("deleteSentencesFromSourceByName:" + pSrcName);
	}

	@Override
	public void deleteSentence(String pSenId) {
		this.ac.getModelBuilder().deleteSingleSentence(this.ac, pSenId);
	}

	@Override
	public String getUidSingle() {
		return this.ac.getModelBuilder().getNextUid(this.ac, "");
	}

	public String showNextUidWithoutIncrementing() {
		return this.ac.getModelBuilder().showNextUidWithoutIncrementing(this.ac);
	}

	@Override
	public Properties getUidBatch(long pBatchSize) {
		return this.ac.getModelBuilder().getBatchOfUids(this.ac, pBatchSize);
	}

	@Override
	public void resetUids() {
		this.ac.getModelBuilder().resetUids(this.ac, "");
	}

	private ContainerSentenceLoadResult performAndReportAction(int pActionType) {
		ContainerSentenceLoadResult senStats = null;
		String formattedExecTime = "";
		String formattedStats = "";
		String resultLabel = "";
		long startTime = System.currentTimeMillis();

		switch (pActionType) {
			case ACTION_RESET:
				senStats = performStoreReset();
				resultLabel = "Sentence store reset - ";
				break;
			case ACTION_EMPTY:
				senStats = performEmptyInstances();
				resultLabel = "Empty instances - ";
				break;
			default:
				reportError("Unexpected action type encountered (" + Integer.toString(pActionType) + ") in StoreActions:performAndReportAction()", this.ac);
				break;
		}

		formattedExecTime = formattedDuration(startTime);
		formattedStats = formattedStats(formattedExecTime, 0);

		if (isReportMicroDebug()) {
			reportMicroDebug(resultLabel + formattedStats, this.ac);
		}

		return senStats;
	}

	private ContainerSentenceLoadResult performStoreReset() {
		ContainerSentenceLoadResult result = null;

		this.ac.getModelBuilder().reset(this.ac);

		if (!this.ac.isCachedCeLoading()) {
			deleteFile(this.ac, this.ac.getCeConfig().getTempPath() + this.ac.getModelBuilder().calculateCeLoggingFilename());
			result = conceptualiseMetamodel(); // Load the core meta model concepts
		} else {
			//Do not load the meta-model if this is cached CE loading (since the meta-model will be in the cached CE)
			result = ContainerSentenceLoadResult.createWithZeroValues("performStoreReset");
		}

		return result;
	}

	private ContainerSentenceLoadResult performEmptyInstances() {
		//Then delete the appropriate sentences and instances from the memory model
		this.ac.getModelBuilder().resetInstanceRepository(this.ac);
		this.ac.getModelBuilder().removeUnusedSentencesAndSources(this.ac);

		//TODO: Should I return negative values here?
		return ContainerSentenceLoadResult.createWithZeroValues("performEmptyInstances");
	}

	private ContainerSentenceLoadResult conceptualiseMetamodel() {
		StringBuilder sb = new StringBuilder();

		ceMetamodel(sb);

		return loadSentencesFromInternal(sb, STEPNAME_CONCMETAMODEL);
	}

	private static ContainerSentenceLoadResult createSentenceStatsFrom(CeAgent pAgent) {
		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("createSentenceStatsFrom(sa-agent):" + pAgent.getConfigInstanceName());

		senStats.setValidSentenceCount(pAgent.getValidSentenceCount());
		senStats.setInvalidSentenceCount(pAgent.getInvalidSentenceCount());

		return senStats;
	}	


	public ContainerSentenceLoadResult saveCeText(String pCeText, CeSource pTgtSrc) {
		final String METHOD_NAME = "saveCeText";

		long startTime = System.currentTimeMillis();
		ContainerSentenceLoadResult senStats = null;

		if (pCeText != null && (!pCeText.isEmpty())) {
			if (pTgtSrc != null) {
				senStats = loadSentencesFromFormForSpecifiedSource(pCeText, pTgtSrc, StoreActions.MODE_NORMAL);		
			} else {
				senStats = loadSentencesFromForm(pCeText, "", StoreActions.MODE_NORMAL);		
			}
		} else {
			reportWarning("No CE text was specified for saving to the specified source", this.ac);
			senStats = ContainerSentenceLoadResult.createWithZeroValues("saveCeText:" + pTgtSrc.getId());
		}

		if (this.ac.getSessionCreations().getNewInstances() != null) {
			senStats.setNewInstances(this.ac.getSessionCreations().getNewInstances());
		}

		String srcName = "";
		
		if (pTgtSrc != null) {
			srcName = pTgtSrc.getId();
		} else {
			srcName = "n/a";
		}
		reportExecutionTiming(this.ac, startTime, "[lo] saveCeText, src=" + srcName, CLASS_NAME, METHOD_NAME);

		return senStats;
	}

}