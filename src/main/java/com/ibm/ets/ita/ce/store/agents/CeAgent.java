package com.ibm.ets.ita.ce.store.agents;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CEFILENAME;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CLASSNAME;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DEBUG;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DOESNOTGENERATECE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DUBRAT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_GENRAT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_MAXITS;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_MAXSENS;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_RESTATE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_SAVEINDIVIDUAL;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_SAVETOFILE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_SENDTOSTORE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_SRC_CONCEPT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_SRC_PROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_SRC_RANGE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_TGT_CONCEPT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_TGT_PROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_TGT_RANGE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_TGT_SOURCE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.CEVALUE_UNLIMITED;
import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_NOVAL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.SENMODE_NORMAL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.SUFFIX_CE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_BECAUSE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_FALSE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_TRUE;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendNewLineToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.calculateFullFilenameFrom;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.getFolderValueFor;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.joinFolderAndFilename;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.writeToFile;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.getBooleanValueFrom;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.getIntValueFrom;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportExecutionTiming;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteNormalParameters;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.core.QueryHandler;
import com.ibm.ets.ita.ce.store.core.StoreConfig;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.parsing.processor.ProcessorCe;
import com.ibm.ets.ita.ce.store.utilities.ReportingUtilities;

public abstract class CeAgent {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = CeAgent.class.getName();
	private static final String PACKAGE_NAME = CeAgent.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private ActionContext ac = null;
	private ModelBuilder mb = null;
	private CeInstance configInstance = null;
	private StringBuilder outputCe = null;
	private int sentenceCounter = 0;
	private int flushCounter = 0;
	private boolean sentenceLimitExceeded = false;

	private String className = null;
	private boolean sendCeToStore = true;
	private boolean saveCeToFile = false;
	private boolean saveCeIndividually = false;
	private boolean doesNotGenerateCe = false;
	protected boolean generateRationale = false;
	protected boolean doubleRationaleSentences = false; // A temporary fix to
														// allow prolog
														// integration (which
														// requires double
														// sentences)
	private boolean failedToGetMandatoryParameter = false;
	private String ceFilename = null;
	private int maxSentences = -1;
	private boolean restateExistingSentences = true;
	private String targetSourceName = null;

	private int iterationCounter = 0;
	private int maxIterations = -1;

	private int validSentenceCount = 0;
	private int invalidSentenceCount = 0;

	private String sourceConceptName = null;
	private String sourcePropertyName = null;
	private String sourceRangeName = null;
	private String targetConceptName = null;
	private String targetPropertyName = null;
	private String targetRangeName = null;

	private CeConcept sourceConcept = null;
	private CeProperty sourceProperty = null;
	private CeConcept targetConcept = null;
	private CeProperty targetProperty = null;

	private CeSource existingSource = null;

	private boolean agentDebug = false;

	public abstract String getAgentName();

	public abstract String getAgentVersion();

	protected abstract void executeAgentProcessing();

	protected abstract void loadAgentParameters();

	protected StoreConfig getCeConfig() {
		return this.ac.getCeConfig();
	}

	private void loadGeneralParameters() {
		String thisVal = null;
		String defaultVal = null;

		thisVal = getConfigSingleValueNamed(PROP_CLASSNAME);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.className = thisVal; // Class name can never be empty, but this
										// test is here for consistency
		} else {
			this.className = ES;
		}

		thisVal = getConfigOptionalSingleValueNamed(PROP_DOESNOTGENERATECE, TOKEN_FALSE);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.doesNotGenerateCe = thisVal.equals(TOKEN_TRUE);
		}

		defaultVal = TOKEN_TRUE;
		thisVal = getConfigOptionalSingleValueNamed(PROP_SENDTOSTORE, defaultVal);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			if (doesNotGenerateCe() && (thisVal != defaultVal)) {
				reportInconsistentParmError(PROP_SENDTOSTORE);
			} else {
				this.sendCeToStore = thisVal.equals(TOKEN_TRUE);
			}
		}

		defaultVal = TOKEN_FALSE;
		thisVal = getConfigOptionalSingleValueNamed(PROP_SAVETOFILE, defaultVal);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			if (doesNotGenerateCe() && (thisVal != defaultVal)) {
				reportInconsistentParmError(PROP_SAVETOFILE);
			} else {
				this.saveCeToFile = thisVal.equals(TOKEN_TRUE);
			}
		}

		defaultVal = TOKEN_TRUE;
		thisVal = getConfigOptionalSingleValueNamed(PROP_RESTATE, defaultVal);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			if (!getCeConfig().cacheCeText() && (thisVal != defaultVal)) {
				reportInconsistentParmError(PROP_RESTATE);
			} else {
				this.restateExistingSentences = thisVal.equals(TOKEN_TRUE);
			}
		}

		defaultVal = TOKEN_FALSE;
		thisVal = getConfigOptionalSingleValueNamed(PROP_SAVEINDIVIDUAL, defaultVal);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			if (doesNotGenerateCe() && (thisVal != defaultVal)) {
				reportInconsistentParmError(PROP_SAVEINDIVIDUAL);
			} else {
				this.saveCeIndividually = thisVal.equals(TOKEN_TRUE);
			}
		}

		defaultVal = ES;
		thisVal = getConfigOptionalSingleFileNamed(PROP_CEFILENAME, defaultVal);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			if (doesNotGenerateCe() && (thisVal != defaultVal)) {
				reportInconsistentParmError(PROP_CEFILENAME);
			} else {
				this.ceFilename = thisVal;
			}
		} else {
			this.ceFilename = ES;
		}

		defaultVal = Integer.toString(CEVALUE_UNLIMITED);
		thisVal = getConfigOptionalSingleValueNamed(PROP_MAXSENS, defaultVal);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			if (doesNotGenerateCe() && (thisVal != defaultVal)) {
				reportInconsistentParmError(PROP_MAXSENS);
			} else {
				this.maxSentences = new Integer(thisVal).intValue();
			}
		}

		thisVal = getConfigOptionalSingleValueNamed(PROP_SRC_CONCEPT, ES);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.sourceConceptName = thisVal;
		} else {
			this.sourceConceptName = ES;
		}

		thisVal = getConfigOptionalSingleValueNamed(PROP_SRC_PROP, ES);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.sourcePropertyName = thisVal;
		} else {
			this.sourcePropertyName = ES;
		}

		thisVal = getConfigOptionalSingleValueNamed(PROP_SRC_RANGE, ES);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.sourceRangeName = thisVal;
		} else {
			this.sourceRangeName = ES;
		}

		thisVal = getConfigOptionalSingleValueNamed(PROP_TGT_CONCEPT, ES);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.targetConceptName = thisVal;
		} else {
			this.targetConceptName = ES;
		}

		thisVal = getConfigOptionalSingleValueNamed(PROP_TGT_PROP, ES);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.targetPropertyName = thisVal;
		} else {
			this.targetPropertyName = ES;
		}

		thisVal = getConfigOptionalSingleValueNamed(PROP_TGT_RANGE, ES);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.targetRangeName = thisVal;
		} else {
			this.targetRangeName = ES;
		}

		thisVal = getConfigOptionalSingleValueNamed(PROP_TGT_SOURCE, ES);
		if ((thisVal != null) && (!thisVal.isEmpty())) {
			this.targetSourceName = thisVal;
		} else {
			this.targetSourceName = ES;
		}

		this.agentDebug = getBooleanValueFrom(getConfigOptionalSingleValueNamed(PROP_DEBUG, TOKEN_FALSE));
		this.generateRationale = getBooleanValueFrom(getConfigOptionalSingleValueNamed(PROP_GENRAT, TOKEN_FALSE));
		this.doubleRationaleSentences = getBooleanValueFrom(
				getConfigOptionalSingleValueNamed(PROP_DUBRAT, TOKEN_FALSE));

		this.maxIterations = getIntValueFrom(getConfigOptionalSingleValueNamed(PROP_MAXITS, DEFAULT_NOVAL));
	}

	protected void initialise(ActionContext pAc, CeInstance pConfigInstance) {
		this.ac = pAc;
		this.mb = this.ac.getModelBuilder();
		this.configInstance = pConfigInstance;
		this.outputCe = new StringBuilder();
		this.sentenceLimitExceeded = false;
		this.sentenceCounter = 0;
		this.flushCounter = 0;
		loadGeneralParameters();
		loadAgentParameters();
	}

	public String getConfigInstanceName() {
		return this.configInstance.getInstanceName();
	}

	public CeConcept[] getConfigInstanceDirectConcepts() {
		return this.configInstance.getDirectConcepts();
	}

	// TODO - needs extra processing in case there is more than one direct
	// concept
	public CeConcept getConfigInstanceConcept() {
		CeConcept result = null;

		for (CeConcept thisCon : getConfigInstanceDirectConcepts()) {
			if (result == null) {
				result = thisCon;
			}
		}

		return result;
	}

	public String getConfigInstanceConceptName() {
		String result = null;
		CeConcept confCon = getConfigInstanceConcept();

		if (confCon != null) {
			result = confCon.getConceptName();
		} else {
			result = ES;
		}

		return result;
	}

	public String getConfigInstanceConceptQualifier() {
		String result = null;
		CeConcept confCon = getConfigInstanceConcept();

		if (confCon != null) {
			result = confCon.conceptQualifier();
		} else {
			result = ES;
		}

		return result;
	}

	public ArrayList<CeInstance> getAllInstancesForConceptNamed(String pConceptName) {
		ArrayList<CeInstance> result = null;

		if (this.mb != null) {
			result = this.mb.getAllInstancesForConceptNamed(this.ac, pConceptName);
		} else {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	public ArrayList<CeInstance> getAllInstancesForConcept(CeConcept pConcept) {
		ArrayList<CeInstance> result = null;

		if (this.mb != null) {
			result = this.mb.retrieveAllInstancesForConcept(pConcept);
		} else {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	public ArrayList<CeInstance> getAllInstancesForSourceConcept() {
		ArrayList<CeInstance> result = null;
		CeConcept srcCon = getSourceConcept();

		if (srcCon != null) {
			if (this.mb != null) {
				result = this.mb.retrieveAllInstancesForConcept(srcCon);
			} else {
				result = new ArrayList<CeInstance>();
			}
		} else {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	public CeInstance getInstanceNamed(String pName) {
		CeInstance result = null;

		if (this.mb != null) {
			result = this.mb.getInstanceNamed(this.ac, pName);
		}

		return result;
	}

	public String getQualifiedAgentNameAndVersion() {
		return getAgentName() + " (" + this.className + ") " + getAgentVersion();
	}

	public boolean isSendingCeToStore() {
		return this.sendCeToStore;
	}

	public boolean isSavingCeIndividually() {
		return this.saveCeIndividually;
	}

	public boolean doesNotGenerateCe() {
		return this.doesNotGenerateCe;
	}

	public boolean generateRationale() {
		return this.generateRationale;
	}

	public boolean isSavingCeToFile() {
		return this.saveCeToFile;
	}

	public boolean isSentenceLimitExceeded() {
		return this.sentenceLimitExceeded;
	}

	public String getCeFilename() {
		return this.ceFilename;
	}

	public int getMaxSentences() {
		return this.maxSentences;
	}

	public boolean hasNoSentenceLimit() {
		return (this.maxSentences == CEVALUE_UNLIMITED);
	}

	public int getSentenceCounter() {
		return this.sentenceCounter;
	}

	public int rejectedSentenceCount() {
		return (this.sentenceCounter - this.maxSentences);
	}

	public int getValidSentenceCount() {
		return this.validSentenceCount;
	}

	public int getInvalidSentenceCount() {
		return this.invalidSentenceCount;
	}

	protected String getSourceConceptName() {
		return this.sourceConceptName;
	}

	protected void setSourceConceptName(String pSrcConName) {
		this.sourceConceptName = pSrcConName;
	}

	protected CeConcept getSourceConcept() {
		if (this.sourceConcept == null) {
			if ((this.sourceConceptName != null) && (!this.sourceConceptName.isEmpty())) {
				this.sourceConcept = getConceptNamed(this.sourceConceptName);
				if (this.sourceConcept == null) {
					reportError("Unable to locate specified source concept named '" + this.sourceConceptName + "'");
				}
			} else {
				reportError("No source concept specified");
			}
		}

		return this.sourceConcept;
	}

	protected String getTargetConceptName() {
		return this.targetConceptName;
	}

	protected void setTargetConceptName(String pTgtConName) {
		this.targetConceptName = pTgtConName;
	}

	protected CeConcept getTargetConcept() {
		if (this.targetConcept == null) {
			if (this.targetConceptName != null) {
				this.targetConcept = getConceptNamed(this.targetConceptName);
				if (this.targetConcept == null) {
					reportError("Unable to locate specified target concept named '" + this.targetConceptName + "'");
				}
			} else {
				reportError("No target concept specified");
			}
		}

		return this.targetConcept;
	}

	protected String getSourcePropertyName() {
		return this.sourcePropertyName;
	}

	protected void setSourcePropertyName(String pSrcPropName) {
		this.sourcePropertyName = pSrcPropName;
	}

	protected CeProperty getSourceProperty() {
		if (this.sourceProperty == null) {
			if (getSourceConcept() != null) {
				if (this.sourcePropertyName != null) {
					String fullPropName = CeProperty.calculateFullPropertyNameFor(getSourceConcept().getConceptName(),
							this.sourcePropertyName, this.sourceRangeName);
					this.sourceProperty = getSourceConcept().retrievePropertyFullyNamed(fullPropName);
					if (this.sourceProperty == null) {
						reportWarning("Unable to locate specified source property '" + this.sourcePropertyName + ":"
								+ this.sourceRangeName + "'");
					}
				} else {
					reportError("No source property specified");
				}
			}
		}

		return this.sourceProperty;
	}

	protected ArrayList<CePropertyInstance> getAllSourcePropertyInstances() {
		ArrayList<CePropertyInstance> result = new ArrayList<CePropertyInstance>();

		CeConcept srcConcept = getSourceConcept();
		String srcPropName = getSourcePropertyName();

		if (this.mb != null) {
			if ((srcPropName != null) && (!srcPropName.isEmpty())) {
				if (srcConcept != null) {
					for (CeInstance thisInst : this.mb.retrieveAllInstancesForConcept(srcConcept)) {
						CePropertyInstance thisPi = thisInst.getPropertyInstanceNamed(srcPropName);

						if (thisPi != null) {
							result.add(thisPi);
						}
					}
				} else {
					reportError(
							"Attempt to list all source property instances failed because the 'source concept' could not be located for this agent instance");
				}
			} else {
				reportError(
						"Attempt to list all source property instances failed because the 'source property' value is not specified for this agent instance");
			}
		}

		return result;
	}

	protected String getTargetPropertyName() {
		return this.targetPropertyName;
	}

	protected void setTargetPropertyName(String pTgtPropName) {
		this.targetPropertyName = pTgtPropName;
	}

	protected CeProperty getTargetProperty() {
		if (this.targetProperty == null) {
			if (getTargetConcept() != null) {
				if (this.targetPropertyName != null) {
					String fullPropName = CeProperty.calculateFullPropertyNameFor(getSourceConcept().getConceptName(),
							this.targetPropertyName, this.targetRangeName);
					this.targetProperty = getTargetConcept().retrievePropertyFullyNamed(fullPropName);
					if (this.targetProperty == null) {
						reportWarning("Unable to locate specified target property '" + this.targetPropertyName + ":"
								+ this.targetRangeName + "'");
					}
				} else {
					reportError("No target property specified");
				}
			}
		}

		return this.targetProperty;
	}

	protected CeConcept getConceptNamed(String pConceptName) {
		CeConcept result = null;

		if (this.mb != null) {
			result = this.mb.getConceptNamed(this.ac, pConceptName);
		}

		return result;
	}

	protected ArrayList<CeInstance> retrieveAllInstancesForConceptNamed(String pConceptName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		CeConcept tgtConcept = getConceptNamed(pConceptName);

		if (tgtConcept != null) {
			result = this.mb.retrieveAllInstancesForConcept(tgtConcept);
		} else {
			reportError("Unable to retrieve concept named '" + pConceptName + "'");
		}

		return result;
	}

	public StringBuilder getAndClearGeneratedCeSentenceText() {
		StringBuilder result = this.outputCe;
		this.outputCe = new StringBuilder();

		return result;
	}

	public int getSizeOfGeneratedCeText() {
		return this.outputCe.length();
	}

	public void runAgent(ActionContext pAc, CeInstance pConfigInstance) {
		initialise(pAc, pConfigInstance);
		boolean oldDebug = this.ac.getCeConfig().isDebug();

		if (this.agentDebug) {
			this.ac.getCeConfig().setDebug(this.agentDebug);
		}

		if (!this.failedToGetMandatoryParameter) {
			// Just run the agent execution with no attempt to catch errors
			executeAgentProcessing();
		} else {
			reportError("Not all mandatory parameters were specified, so this agent will not be executed");
		}

		if (!doesNotGenerateCe()) {
			// Only deal with generated sentences if this agent is not barred
			// from generating any!
			if (this.flushCounter > 0) {
				// Increment the flush counter if it has already been
				// incremented
				++this.flushCounter;
			}

			dealWithGeneratedSentences();
		}

		agentProcessingCompleted();

		this.ac.getCeConfig().setDebug(oldDebug);
	}

	public void flushSentences() {
		incrementFlushCounter();
		dealWithGeneratedSentences();

		// After a flush ensure the created sentences and instances are saved
		if (isReportDebug()) {
			reportDebug("Updating created things following CE agent flush (" + getAgentName() + ", " + getAgentVersion()
					+ ")");
		}
		this.ac.getModelBuilder().updateCreatedThingsFrom(this.ac);
	}

	private void incrementFlushCounter() {
		++this.flushCounter;
	}

	private void dealWithGeneratedSentences() {
		StringBuilder finalCe = getAndClearGeneratedCeSentenceText();

		if (finalCe.length() != 0) {
			finalCe.insert(0, ceAgentAnnotationPrefix());
			if (isSentenceLimitExceeded()) {
				reportWarning("Sentence limit of " + getMaxSentences() + " was exceeded for agent '"
						+ getQualifiedAgentNameAndVersion() + "' (" + Integer.toString(rejectedSentenceCount())
						+ " sentences were generated but not stored)");
			}
			if (isSendingCeToStore()) {
				// Only send the sentences to the store if they are not being
				// saved individually
				if (!isSavingCeIndividually()) {
					sendSentencesToStore(finalCe);
				}
			} else {
				if (isReportDebug()) {
					reportDebug("Output from agent '" + getAgentName() + "' (" + getSentenceCounter()
							+ " sentences) not sent to store due to configuration setting [send CE to store=false]");
				}
			}
			if (isSavingCeToFile()) {
				// The CE filename is expected to NOT be fully qualified, and
				// the standard "generation" sub-folder will be used
				String targetFilename = null;

				if (getCeFilename().isEmpty()) {
					targetFilename = getAgentName() + SUFFIX_CE;
				} else {
					targetFilename = getCeFilename();
				}

				targetFilename = calculateFilenameWithFlush(targetFilename);

				targetFilename = joinFolderAndFilename(this.ac, getCeConfig().getGenPath(), targetFilename);

				writeToFile(this.ac, finalCe, targetFilename);
				if (isReportDebug()) {
					reportDebug("Output from agent '" + getAgentName() + "' has been logged to file '" + targetFilename
							+ "'");
				}
			}
		}
	}

	private String calculateFilenameWithFlush(String pFilename) {
		String result = pFilename;
		// TODO: Abstract these values
		if (this.flushCounter > 0) {
			String flushbit = "_flush_" + Integer.toString(this.flushCounter);

			// TODO: Improve this to handle other extensions too
			if (result.endsWith(".ce")) {
				result = result.substring(0, (result.length() - 3)) + flushbit + ".ce";
			} else if (result.endsWith(".CE")) {
				result = result.substring(0, (result.length() - 3)) + flushbit + ".CE";
			}
		}

		return result;
	}

	private void sendSentencesToStore(StringBuilder pSb) {
		final String METHOD_NAME = "sendSentencesToStore";

		long startTime = System.currentTimeMillis();

		if (pSb != null) {
			if (pSb.length() != 0) {
				ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult
						.createWithZeroValues("sendSentencesToStore()");
				ProcessorCe procCe = new ProcessorCe(this.ac, false, senStats);
				CeSource oldCurrSource = this.ac.getCurrentSource();

				// If the agent config states that a named source should be used
				// then
				// ensure that this is the case
				if ((this.targetSourceName != null) && (!this.targetSourceName.isEmpty())) {
					this.existingSource = this.mb.getSourceById(this.targetSourceName);

					if (this.existingSource == null) {
						this.existingSource = CeSource.createNewAgentSource(this.ac, getQualifiedAgentNameAndVersion(),
								getConfigInstanceName(), this.targetSourceName);
					}
				}

				if (this.existingSource != null) {
					procCe.processNormalSentencesFromSbWithExistingSource(pSb, CeSource.SRCTYPE_ID_AGENT,
							getQualifiedAgentNameAndVersion(), getConfigInstanceName(), startTime, SENMODE_NORMAL,
							this.existingSource);
				} else {
					this.existingSource = procCe.processNormalSentencesFromSb(pSb, CeSource.SRCTYPE_ID_AGENT,
							getQualifiedAgentNameAndVersion(), getConfigInstanceName(), startTime, SENMODE_NORMAL);
				}

				this.existingSource.setParentSource(this.ac, oldCurrSource);
				this.ac.setCurrentSource(oldCurrSource);

				this.validSentenceCount += senStats.getValidSentenceCount();
				this.invalidSentenceCount += senStats.getInvalidSentenceCount();

				reportExecutionTiming(getActionContext(), startTime,
						"[ag] sendSentencesToStore, agent=" + getAgentName(), CLASS_NAME, METHOD_NAME);
			} else {
				// No need to report an error or warning if an agent doesn't
				// generate any sentences
				if (isReportDebug()) {
					reportDebug("No CE sentences found in supplied string (empty string) ["
							+ getQualifiedAgentNameAndVersion() + "]");
				}
			}
		} else {
			// The agent should return an empty string rather than null, so log
			// a warning here
			reportWarning("No CE sentences generated by the agent '" + getQualifiedAgentNameAndVersion() + "' ("
					+ getConfigInstanceName() + ")");
		}
	}

	protected void closeWithStandardRationale(StringBuilder pSb) {
		if (generateRationale()) {
			// TODO: Need to add proper rationale
			appendNewLineToSb(pSb);
			appendToSbNoNl(pSb, TOKEN_BECAUSE);
			appendToSbNoNl(pSb, " ");
		}

		appendToSb(pSb, TOKEN_DOT);
	}

	private String ceAgentAnnotationPrefix() {
		// TODO: Anonymise this
		return "Note: All sentences generated by CE Agent " + getAgentName() + " (Version " + getAgentVersion()
				+ ") using agent instance '" + getConfigInstanceName() + "'." + NL + NL;
	}

	protected void agentProcessingCompleted() {
		// Subclass this method if you wish to do anything once the processing
		// is completed
	}

	public ActionContext getActionContext() {
		return this.ac;
	}

	protected ModelBuilder getModelBuilder() {
		if (this.mb == null) {
			this.mb = this.ac.getModelBuilder();
		}

		return this.mb;
	}

	public String getConfigSingleValueNamed(String pPropName) {
		String result = ES;

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result = thisInst.getSingleOrFirstValue();
			} else {
				this.failedToGetMandatoryParameter = true;
				reportError("Unable to locate config property named '" + pPropName + "'");
			}
		} else {
			this.failedToGetMandatoryParameter = true;
			reportError("No config instance loaded");
		}

		return result;
	}

	public String getConfigSingleFileNamed(String pPropName) {
		String result = ES;

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result = calculateFullFilenameFrom(getActionContext(), thisInst.getSingleOrFirstValue());
			} else {
				this.failedToGetMandatoryParameter = true;
				reportError("Unable to locate config property named '" + pPropName + "'");
			}
		} else {
			this.failedToGetMandatoryParameter = true;
			reportError("No config instance loaded");
		}

		return result;
	}

	public String getConfigSingleFolderNamed(String pPropName) {
		String result = ES;

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result = getFolderValueFor(getActionContext(), thisInst.getSingleOrFirstValue());
			} else {
				this.failedToGetMandatoryParameter = true;
				reportError("Unable to locate config property named '" + pPropName + "'");
			}
		} else {
			this.failedToGetMandatoryParameter = true;
			reportError("No config instance loaded");
		}

		return result;
	}

	public boolean isPropertySpecified(String pPropName) {
		boolean result = false;

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			result = (thisInst != null);
		} else {
			reportError("No config instance loaded");
		}

		return result;
	}

	public String getConfigOptionalSingleValueNamed(String pPropName, String pDefaultValue) {
		String result = ES;

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result = thisInst.getSingleOrFirstValue();
			} else {
				result = pDefaultValue;
			}
		} else {
			reportError("No config instance loaded");
		}

		return result;
	}

	public String getConfigOptionalSingleFileNamed(String pPropName, String pDefaultValue) {
		String result = ES;

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result = calculateFullFilenameFrom(getActionContext(), thisInst.getSingleOrFirstValue());
			} else {
				result = pDefaultValue;
			}
		} else {
			reportError("No config instance loaded");
		}

		return result;
	}

	public String getConfigOptionalSingleFolderNamed(String pPropName, String pDefaultValue) {
		String result = ES;

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result = getFolderValueFor(getActionContext(), thisInst.getSingleOrFirstValue());
			} else {
				result = pDefaultValue;
			}
		} else {
			reportError("No config instance loaded");
		}

		return result;
	}

	public ArrayList<String> getConfigValueListNamed(String pPropName) {
		ArrayList<String> result = new ArrayList<String>();

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result.addAll(thisInst.getValueList());
			} else {
				this.failedToGetMandatoryParameter = true;
				reportError("Unable to locate config property named '" + pPropName + "'");
			}
		} else {
			this.failedToGetMandatoryParameter = true;
			reportError("No config instance loaded");
		}

		return result;
	}

	public ArrayList<String> getConfigFileListNamed(String pPropName) {
		ArrayList<String> result = new ArrayList<String>();

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				for (String rawVal : thisInst.getValueList()) {
					result.add(calculateFullFilenameFrom(getActionContext(), rawVal));
				}
			} else {
				this.failedToGetMandatoryParameter = true;
				reportError("Unable to locate config property named '" + pPropName + "'");
			}
		} else {
			this.failedToGetMandatoryParameter = true;
			reportError("No config instance loaded");
		}

		return result;
	}

	public ArrayList<String> getConfigFolderListNamed(String pPropName) {
		ArrayList<String> result = new ArrayList<String>();

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				for (String rawVal : thisInst.getValueList()) {
					result.add(getFolderValueFor(getActionContext(), rawVal));
				}
			} else {
				this.failedToGetMandatoryParameter = true;
				reportError("Unable to locate config property named '" + pPropName + "'");
			}
		} else {
			this.failedToGetMandatoryParameter = true;
			reportError("No config instance loaded");
		}

		return result;
	}

	public ArrayList<CeInstance> getConfigInstanceListNamed(String pPropName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		for (String thisInstName : getConfigValueListNamed(pPropName)) {
			CeInstance thisInst = getInstanceNamed(thisInstName);

			if (thisInst != null) {
				result.add(thisInst);
			}
		}

		return result;
	}

	public ArrayList<String> getConfigOptionalValueListNamed(String pPropName) {
		ArrayList<String> result = new ArrayList<String>();

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result.addAll(thisInst.getValueList());
			}
		} else {
			reportError("No config instance loaded");
		}

		return result;
	}

	public ArrayList<String> getConfigOptionalFileListNamed(String pPropName) {
		ArrayList<String> result = new ArrayList<String>();

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				for (String rawVal : thisInst.getValueList()) {
					result.add(calculateFullFilenameFrom(getActionContext(), rawVal));
				}
			}
		} else {
			reportError("No config instance loaded");
		}

		return result;
	}

	public ArrayList<String> getConfigOptionalFolderListNamed(String pPropName) {
		ArrayList<String> result = new ArrayList<String>();

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				for (String rawVal : thisInst.getValueList()) {
					result.add(getFolderValueFor(getActionContext(), rawVal));
				}
			}
		} else {
			reportError("No config instance loaded");
		}

		return result;
	}

	public ArrayList<CeInstance> getConfigOptionalValueInstanceListNamed(String pPropName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		if (this.configInstance != null) {
			CePropertyInstance thisInst = this.configInstance.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result = new ArrayList<CeInstance>(thisInst.getValueInstanceList(this.ac));
			}
		} else {
			reportError("No config instance loaded");
		}

		return result;
	}

	protected void addRationaleSentenceCe(String pCeText, boolean pNoAppend) {
		if (!generateRationale()) {
			reportWarning("Generated CE sentence (" + pCeText
					+ ") submitted with rationale when rationale generation is switched off");
		}

		String ceText = pCeText;
		if (!pNoAppend) {
			int lastDotIdx = ceText.lastIndexOf(TOKEN_DOT);

			if (lastDotIdx > 0) {
				ceText = ceText.substring(0, lastDotIdx);
				ceText += " [ agent_" + getAgentName() + " ]."; // TODO:
																// Anonymise
																// this
			} else {
				reportWarning("Sentence with no dot detected during rationale processing: |" + ceText + "|");
			}
		}

		addActualSentenceCe(ceText);
	}

	protected void addSentenceCe(String pCeText) {
		if (generateRationale()) {
			reportWarning("Generated CE sentence (" + pCeText
					+ ") submitted without rationale when rationale generation is switched on");
		}

		addActualSentenceCe(pCeText);
	}

	private void addActualSentenceCe(String pCeText) {
		if (doesNotGenerateCe()) {
			reportError(
					"Attempt to add sentence when agent is defined as not generating CE.  Sentence='" + pCeText + "'");
		} else {
			if (hasNoSentenceLimit() || (this.sentenceCounter <= this.maxSentences)) {
				if (isSentenceToBeSaved(pCeText)) {
					if (isSavingCeIndividually()) {
						// Send the CE straight to the store
						// (sentences are being saved individually)
						StringBuilder sbText = new StringBuilder(pCeText);
						sbText.append(NL);
						sendSentencesToStore(sbText);
					} else {
						// Append the CE to the current cache of CE
						// (sentences will be submitted collectively at the end
						// of agent execution)
						this.outputCe.append(pCeText);
						this.outputCe.append(NL);
					}
				}
			} else {
				this.sentenceLimitExceeded = true;
			}

			this.sentenceCounter++;
		}
	}

	private boolean isSentenceToBeSaved(String pCeText) {
		boolean result = true;

		if (!this.restateExistingSentences) {
			result = !this.ac.getModelBuilder().doesSentenceTextAlreadyExist(this.ac, pCeText);
		}

		return result;
	}

	public String addSentenceCe(String pCeTemplate, TreeMap<String, String> pValues) {
		String ceSentence = substituteCeParameters(pCeTemplate, pValues);

		addSentenceCe(ceSentence);

		return ceSentence;
	}

	public void addRationaleSentenceCe(String pCeTemplate, TreeMap<String, String> pValues) {
		String ceSentence = substituteCeParameters(pCeTemplate, pValues);

		addRationaleSentenceCe(ceSentence, false);
	}

	public void addRationaleSentenceCeWithNoAgentName(String pCeText) {
		addRationaleSentenceCe(pCeText, true);
	}

	public void addSentenceCeNoEncoding(String pCeTemplate, TreeMap<String, String> pValues) {
		String ceSentence = substituteNormalParameters(pCeTemplate, pValues);

		addSentenceCe(ceSentence);
	}

	protected ContainerCeResult executeCeQueryForInstances(String pCeQuery) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.executeUserSpecifiedCeQueryForInstances(pCeQuery, false, null, null);
	}

	public void addSentenceCeWithOrWithoutRationale(String pCeTemplate, TreeMap<String, String> pCeParms) {
		if (generateRationale()) {
			// Temporary - if requested, double up the rationale sentences by
			// generating two sentences (one with and one without rationale)
			// This is to allow integration with Prolog
			if (this.doubleRationaleSentences) {
				addSentenceCe(pCeTemplate, pCeParms);
			}

			addRationaleSentenceCe(pCeTemplate, pCeParms);
		} else {
			addSentenceCe(pCeTemplate, pCeParms);
		}
	}

	protected ArrayList<CeInstance> executeCeQueryForInstancesWithSingleColumnResult(String pCeQuery, String pColName) {
		ContainerCeResult ceResult = executeCeQueryForInstances(pCeQuery);

		ArrayList<CeInstance> result = ceResult.getColumnOfInstancesFor(this.ac, pColName);

		return result;
	}

	protected ContainerCeResult executeNamedCeQueryForInstances(String pQueryName, String pStartTs, String pEndTs) {
		QueryHandler qh = new QueryHandler(this.ac);
		return qh.executeUserSpecifiedCeQueryByNameForInstances(pQueryName, false, pStartTs, pEndTs);
	}

	protected void reportError(String pError) {
		ReportingUtilities.reportError(pError + agentMessageSuffix(), this.ac);
	}

	protected void reportWarning(String pWarning) {
		ReportingUtilities.reportWarning(pWarning + agentMessageSuffix(), this.ac);
	}

	protected void reportDebug(String pDebug) {
		ReportingUtilities.reportDebug(pDebug + agentMessageSuffix(), this.ac);
	}

	public void reportUnhandledException(Exception pE, String pMethod, String pExtra) {
		reportException(pE, pExtra, this.ac, logger, CLASS_NAME, pMethod); // calling
																			// class
																			// unknown
																			// so
																			// use
																			// this
																			// class
																			// name
	}

	private void reportInconsistentParmError(String pParmName) {
		reportError("Attempt to set inconsistent property '" + pParmName
				+ "' (Agent is stated as not generating CE sentences)");
	}

	private String agentMessageSuffix() {
		return " [Agent=" + getAgentName() + ", config=" + getConfigInstanceName() + "]"; // TODO:
																							// Anonymise
																							// this
	}

	public String getNextUid(String pOptionalPrefix) {
		return this.mb.getNextUid(this.ac, pOptionalPrefix);
	}

	public Properties getBatchOfUids(long pBatchSize) {
		return this.mb.getBatchOfUids(this.ac, pBatchSize);
	}

	public boolean hasRequiredParameters() {
		return !this.failedToGetMandatoryParameter;
	}

	protected void incrementIterationCounter() {
		this.iterationCounter++;
	}

	protected boolean hasExceededMaxIterationCount() {
		boolean result = (this.maxIterations != -1) && (this.iterationCounter > this.maxIterations);

		if (result) {
			reportWarning("Max iterations (" + this.maxIterations + ") have been exceeded for agent " + getAgentName());
		}

		return result;
	}

}
