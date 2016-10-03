package com.ibm.ets.ita.ce.store.parsing.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.core.StaticCeRepository.ceMetamodelConceptualModel;
import static com.ibm.ets.ita.ce.store.core.StaticCeRepository.ceMetamodelConceptualModelDefinesConcept;
import static com.ibm.ets.ita.ce.store.core.StaticCeRepository.ceMetamodelEntityConceptAnnotation;
import static com.ibm.ets.ita.ce.store.core.StaticCeRepository.ceMetamodelEntityConceptChild;
import static com.ibm.ets.ita.ce.store.core.StaticCeRepository.ceMetamodelEntityConceptMain;
import static com.ibm.ets.ita.ce.store.core.StaticCeRepository.ceMetamodelProperty;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_ATTCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CTE;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_DTPROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_OBPROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_RELCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_PROPNAME;
import static com.ibm.ets.ita.ce.store.names.MiscNames.SENMODE_NORMAL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.SENMODE_VALIDATE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.SRCID_POPMODEL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.STEPNAME_POPMODEL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_CONNECTOR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_RQNAME;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_RQSTART;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AND;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CLOSESQBR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_OPENSQBR;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.bufferedReaderFromString;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.formattedDuration;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.reportExecutionTiming;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteNormalParameters;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.agents.CeNotifyHandler;
import com.ibm.ets.ita.ce.store.agents.NotifyActionContext;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeAnnotation;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.HelperConcept;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleConclusion;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationalePremise;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceCommand;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFact;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceRuleOrQuery;
import com.ibm.ets.ita.ce.store.parsing.saver.SentenceSaver;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerSentence;

public class ProcessorCe {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = ProcessorCe.class.getName();
	private static final String PACKAGE_NAME = ProcessorCe.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

//	private String auditFilename = null;
	protected ActionContext ac = null;
	private ContainerSentenceLoadResult senStats = null;

	private BuilderSentence lastSentence = null;
	private boolean rationaleProcessing = false;
	private boolean suppressMessages = true;

	private ArrayList<BuilderSentence> validatedSentences = null;

	protected SentenceParserCe sp = null; 

	public ProcessorCe(ActionContext pAc, boolean pSuppressMessages, ContainerSentenceLoadResult pSenStats) {
		this.ac = pAc;		
		this.sp = new SentenceParserCe(this.ac, this);
		this.suppressMessages = pSuppressMessages;
		this.senStats = pSenStats;
	}
	
	public ContainerSentenceLoadResult getSentenceStats() {
		return this.senStats;
	}

	public void addToInvalidCount() {
		this.senStats.incrementInvalidSentenceCount(1);
	}

	public BuilderSentence getLastSentence() {
		return this.lastSentence;
	}

	public void markAsRationaleProcessing() {
		this.rationaleProcessing = true;
	}

	public boolean isRationaleProcessing() {
		return this.rationaleProcessing;
	}

	public ArrayList<BuilderSentence> getValidatedSentences() {
		return this.validatedSentences;
	}

	private void addValidatedSentence(BuilderSentence pSen) {
		if (this.validatedSentences == null) {
			this.validatedSentences = new ArrayList<BuilderSentence>();
		}

		this.validatedSentences.add(pSen);
	}

	protected void validateCeSentence(BuilderSentence pSentence) {
		if (!pSentence.hasErrors()) {
			TokenizerSentence.doTokenizingFor(this.ac, pSentence, false);
			addValidatedSentence(pSentence);

			if (pSentence.hasErrors()) {
				reportWarning("Sentence has errors, for sentence: '" + pSentence.getSentenceText() + "'", this.ac);
			}
		} else {
			reportWarning("Sentence not sent to tokenizer due to errors, for sentence: '" + pSentence.getSentenceText() + "'", this.ac);
		}

		if (pSentence.isValid()) {
			this.senStats.addValidatedSentence(pSentence);
		} else {
			this.senStats.incrementInvalidSentenceCount(1);
		}
	}

	protected void processCeSentence(BuilderSentence pSentence) {
		if (!pSentence.isCommandSentence()) {
			if (isRationaleProcessing()) {
				pSentence.markAsRationaleProcessing();
			}

			tokenizeSentence(this.ac, pSentence, this.lastSentence, this.suppressMessages, this.senStats);
//			logFinalSentence(pSentence.getConvertedSentence());

			if (!isRationaleProcessing()) {
				if (pSentence.isValid()) {
					this.senStats.addValidatedSentence(pSentence);
				} else {
					this.senStats.incrementInvalidSentenceCount(1);
				}
			}
		} else {
			BuilderSentenceCommand cmdSen = (BuilderSentenceCommand)pSentence;
			cmdSen.executeCommand(this.ac, this.senStats);
		}

		//If this sentence is not an annotation sentence then store it as the last sentence processed
		if (!pSentence.isAnnotationSentence()) {
			this.lastSentence = pSentence;
		}
	}

	public static void tokenizeSentence(ActionContext pAc, BuilderSentence pSentence, BuilderSentence pLastSentence, boolean pSuppressMessages, ContainerSentenceLoadResult pSenStats) {
		if (!pSentence.hasErrors()) {
			TokenizerSentence.doTokenizingFor(pAc, pSentence, pSuppressMessages);

			if (sentenceShouldBeSaved(pAc, pSentence)) {
				if (!pSentence.hasErrors()) {
					//Don't save the sentence if it is part of the rationale of another sentence
					if (!pSentence.isRationaleProcessing()) {
						SentenceSaver.saveValidSentence(pAc, pSentence, pLastSentence);
					}

					if (pSentence.hasRationale()) {
						//If the sentence has rationale then it must be a normal fact sentence
						doRationaleTokenizingFor(pAc, (BuilderSentenceFact)pSentence, pSenStats);
					}
				} else {
					SentenceSaver.saveInvalidSentence(pAc, pSentence);
					reportError("Sentence not sent to CE Store due to errors, for sentence: '" + pSentence.getSentenceText() + "'", pAc);
				}
			}
		} else {
			reportWarning("Sentence not sent to tokenizer due to errors, for sentence: '" + pSentence.getSentenceText() + "'", pAc);
		}
	}

	private static boolean sentenceShouldBeSaved(ActionContext pAc, BuilderSentence pSentence) {
		boolean result = true;

		//Query and rule sentences must not be saved via this route if they are being executed...
		if (pAc.isExecutingQueryOrRule()) {
			if ((pSentence.isQuerySentence()) || (pSentence.isRuleSentence())) {
				result = false;
			}
		}

		return result;
	}

	public static void doRationaleTokenizingFor(ActionContext pAc, BuilderSentenceFact pSenBldr, ContainerSentenceLoadResult pSenStats) {
		String ruleName = pSenBldr.getRationaleRuleName();
		CeSentence actualSen = pSenBldr.getConvertedSentence();
		String ratText = pSenBldr.getSentenceText();
		CeRationaleReasoningStep ratRs = CeRationaleReasoningStep.createNew(pAc, actualSen, ruleName, ratText);
		generateRationaleConclusions(pAc, ratRs, pSenBldr);

		ArrayList<String> ratFrags = pSenBldr.rationaleFragments();

		boolean firstTime = true;
		for (String thisRatText : ratFrags) {
			ArrayList<String> ratTokens = new ArrayList<String>();

			if (!firstTime) {
				ratTokens.add(SCELABEL_CONNECTOR);
				ratTokens.add(TOKEN_AND);
			}

			ProcessorCe procCe = new ProcessorCe(pAc, true, pSenStats);
			procCe.markAsRationaleProcessing();
			procCe.processRationaleSentence(thisRatText);
			BuilderSentenceFact ratSentence = (BuilderSentenceFact)procCe.getLastSentence();

			if (ratSentence != null) {
				ratTokens.addAll(ratSentence.getStructuredCeTokens());

				if (!ratSentence.hasErrors()) {
					generateRationalePremises(pAc, ratRs, ratSentence);
				} else {
					reportWarning("Errors were generated during rationale processing, for:" + ratText, pAc);
				}

				firstTime = false;
			} else {
				reportWarning("No rationale sentence was extracted from: " + ratText, pAc);
			}
		}

		ArrayList<String> closingTokens = new ArrayList<String>();
		closingTokens.add(SCELABEL_RQSTART);
		closingTokens.add(TOKEN_OPENSQBR);
		closingTokens.add(SCELABEL_RQNAME);
		closingTokens.add(ratRs.getRuleName());
		closingTokens.add(TOKEN_CLOSESQBR);
		closingTokens.add(TOKEN_DOT);

		actualSen.addStructuredRationaleTokens(closingTokens);
		actualSen.setRationaleReasoningStep(ratRs);
	}

	private static void generateRationalePremises(ActionContext pAc, CeRationaleReasoningStep pRs, BuilderSentenceFact pRatSen) {
		String instName = pRatSen.getInstanceName();
		String conName = "";
		boolean conIsNegated = false;
		boolean premiseAdded = false;

		if (pRatSen.getTargetConceptNormal() != null) {
			conIsNegated = false;
			conName = pRatSen.getTargetConceptNormal().getConceptName();
		}
		if (pRatSen.getTargetConceptNegated() != null) {
			conIsNegated = false;
			conName = pRatSen.getTargetConceptNegated().getConceptName();
		}

		//Process datatype properties
		for (CePropertyInstance thisDpi : pRatSen.getDatatypeProperties()) {
			boolean propIsNegated = thisDpi.isNegated(pAc);

			for (CePropertyValue thisPv : thisDpi.getPropertyValues()) {
				String propName = thisDpi.getPropertyName();
				String propVal = thisPv.getValue();
				String rangeName = thisPv.getRangeName();

				CeRationalePremise ratPrem = CeRationalePremise.createNew(conIsNegated, conName, instName, propIsNegated, propName, rangeName, propVal);
				pRs.addPremise(ratPrem);
				premiseAdded = true;
			}
		}

		//Process object properties
		for (CePropertyInstance thisOpi : pRatSen.getObjectProperties()) {
			boolean propIsNegated = thisOpi.isNegated(pAc);

			for (CePropertyValue thisPv : thisOpi.getPropertyValues()) {
				String propName = thisOpi.getPropertyName();
				String propVal = thisPv.getValue();
				String rangeName = thisPv.getRangeName();

				CeRationalePremise ratPrem = CeRationalePremise.createNew(conIsNegated, conName, instName, propIsNegated, propName, rangeName, propVal);
				pRs.addPremise(ratPrem);
				premiseAdded = true;
			}
		}

		//Cater for such cases as "there is a situation named th_cestore0000111" where
		//the premise relies on a primary concept. Here we just look for the case
		//where no premise has been identified so far and assume that that
		//it's because it's a primary concept premise
		if (premiseAdded == false){
			CeRationalePremise ratPrem = CeRationalePremise.createNew(conIsNegated, conName, instName, false, "[is a]", "", "");
			pRs.addPremise(ratPrem);
		}
	}

	private static void generateRationaleConclusions(ActionContext pAc, CeRationaleReasoningStep pRs, BuilderSentenceFact pOrigSen) {
		String instName = pOrigSen.getInstanceName();
		String conName = "";
		boolean conIsNegated = false;

		if (pOrigSen.getTargetConceptNormal() != null) {
			conIsNegated = false;
			conName = pOrigSen.getTargetConceptNormal().getConceptName();
		}
		if (pOrigSen.getTargetConceptNegated() != null) {
			conIsNegated = false;
			conName = pOrigSen.getTargetConceptNegated().getConceptName();
		}

		//Secondary concepts
		for (CeConcept secCon : pOrigSen.getAllSecondaryConcepts()) {
			String rangeName = secCon.getConceptName();
			CeRationaleConclusion ratConc = CeRationaleConclusion.createNew(conIsNegated, conName, instName, false, "[is a]", rangeName, "");
			pRs.addConclusion(ratConc);
		}

		//Process datatype properties
		for (CePropertyInstance thisDpi : pOrigSen.getDatatypeProperties()) {
			boolean propIsNegated = thisDpi.isNegated(pAc);

			for (CePropertyValue thisPv : thisDpi.getPropertyValues()) {
				String propName = thisDpi.getPropertyName();
				String propVal = thisPv.getValue();
				String rangeName = thisPv.getRangeName();

				CeRationaleConclusion ratConc = CeRationaleConclusion.createNew(conIsNegated, conName, instName, propIsNegated, propName, rangeName, propVal);
				pRs.addConclusion(ratConc);
			}
		}

		//Process object properties
		for (CePropertyInstance thisOpi : pOrigSen.getObjectProperties()) {
			boolean propIsNegated = thisOpi.isNegated(pAc);

			for (CePropertyValue thisPv : thisOpi.getPropertyValues()) {
				String propName = thisOpi.getPropertyName();
				String propVal = thisPv.getValue();
				String rangeName = thisPv.getRangeName();

				CeRationaleConclusion ratConc = CeRationaleConclusion.createNew(conIsNegated, conName, instName, propIsNegated, propName, rangeName, propVal);
				pRs.addConclusion(ratConc);
			}
		}

		//Cater for such cases as "there is a situation named th_cestore0000111" where
		//the conclusion relies on a primary concept. Here we assume that there is 
		//always a primary concept conclusion. I.e. the following will result in the
		//conclusion that an instance of the primary concept has been created:
		//  there is a sentence from syncoin message named 'sen_cestore0000006'
		//  there is a sentence from syncoin message named 'sen_cestore0000006' that has 'The caller ..' as sentence text
		CeRationaleConclusion ratConc = CeRationaleConclusion.createNew(conIsNegated, conName, instName, false, "[is a]", "", "");
		pRs.addConclusion(ratConc);
	}

	public CeSource processNormalSentencesFromString(String pText, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime, int pMode) {
		final String METHOD_NAME = "processNormalSentencesFromString";

		BufferedReader br = bufferedReaderFromString(pText);
		CeSource newSrc = processSentencesForSource(br, pSourceType, pTxnName, pExtraInfo, pStartTime, pMode, null);

		try {
			br.close();
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}

		if (pMode != SENMODE_VALIDATE) {
			doPostSourceLoadActions(newSrc);
		}
		
		return newSrc;
	}

	public CeSource processNormalSentencesFromStringForSpecifiedSource(String pText, CeSource pTgtSrc, String pExtraInfo, long pStartTime, int pMode) {
		final String METHOD_NAME = "processNormalSentencesFromStringForSpecifiedSource";

		BufferedReader br = bufferedReaderFromString(pText);
		processSentencesForSource(br, CeSource.SRCTYPE_ID_FORM, "", pExtraInfo, pStartTime, pMode, pTgtSrc);

		try {
			br.close();
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}

		if (pMode != SENMODE_VALIDATE) {
			//Since this is a reused source will this now duplicate if model changes are present?
			doPostSourceLoadActions(pTgtSrc);
		}

		return pTgtSrc;
	}

	public CeSource processNormalSentencesFromSb(StringBuilder pSb, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime, int pMode) {
		CeSource newSrc = processSentencesForSource(pSb, pSourceType, pTxnName, pExtraInfo, pStartTime, pMode, null);
		doPostSourceLoadActions(newSrc);

		return newSrc;
	}

	public CeSource processNormalSentencesFromSb(StringBuilder pSb, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime) {
		CeSource newSrc = processSentencesForSource(pSb, pSourceType, pTxnName, pExtraInfo, pStartTime, SENMODE_NORMAL, null);
		doPostSourceLoadActions(newSrc);

		return newSrc;
	}

	public void processNormalSentencesFromSbWithExistingSource(StringBuilder pSb, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime, int pMode, CeSource pSource) {
		processSentencesForExistingSource(pSb, pSourceType, pTxnName, pExtraInfo, pStartTime, pMode, pSource);
		doPostSourceLoadActions(pSource);
	}

	public CeSource processNormalSentencesFromReader(BufferedReader pReader, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime) {
		CeSource newSrc = processSentencesForSource(pReader, pSourceType, pTxnName, pExtraInfo, pStartTime, SENMODE_NORMAL, null);
		doPostSourceLoadActions(newSrc);

		return newSrc;
	}

	public void processNormalSentencesFromReaderWithExistingSource(BufferedReader pReader, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime, int pMode, CeSource pSource) {
		processSentencesForExistingSource(pReader, pSourceType, pTxnName, pExtraInfo, pStartTime, pMode, pSource);
		doPostSourceLoadActions(pSource);
	}

	public void processRationaleSentence(String pRationaleText) {
		final String METHOD_NAME = "processRationaleSentence";

		BufferedReader br = bufferedReaderFromString(pRationaleText);
		this.sp.doParsing(br, SENMODE_NORMAL);

		try {
			br.close();
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	public CeRule processRuleSentence(String pCeRuleText, long pStartTime) {
		CeRule result = null;

		CeQuery tgtQuery = processQuerySentence(pCeRuleText, pStartTime);

		if (tgtQuery instanceof CeRule) {
			result = (CeRule)tgtQuery;

//			//Before returning the rule, mark it with a temporary rule name prefix
//			result.markAsTemporary();
		} else {
			reportError("Unable to execute as rule since it is a query: " + tgtQuery.getCeText(), this.ac);
		}

		return result;
	}

	public CeQuery processQuerySentence(String pCeQueryText, long pStartTime) {
		final String METHOD_NAME = "processQuerySentence";

		CeQuery result = null;
		BufferedReader br = bufferedReaderFromString(pCeQueryText);
		this.sp.doParsing(br, SENMODE_NORMAL);
		
		try {
			br.close();
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}

		recordSentenceAddingResults(pStartTime, 0, "query sentence processing complete");

		if (this.lastSentence != null) {
			if (this.lastSentence.isQuerySentence()) {
				BuilderSentenceRuleOrQuery bSen = (BuilderSentenceRuleOrQuery)this.lastSentence;
				result = bSen.getQuery();
			} else if (this.lastSentence.isRuleSentence()) {
				BuilderSentenceRuleOrQuery bSen = (BuilderSentenceRuleOrQuery)this.lastSentence;
				result = bSen.getRule();
			} else {
				reportError("CE cannot be executed as query since it is not a query or rule sentence.  For sentence (" + this.lastSentence.getConvertedSentence().formattedSentenceType() + "): " + this.lastSentence.getSentenceText(), this.ac);
			}

			if (this.lastSentence.hasErrors()) {
				if (result != null) {
					result.markAsHavingErrors();
				}
			}
		} else {
			reportError("The CE query is not valid: " + pCeQueryText, this.ac);
		}

		return result;
	}

	private void processSentencesForExistingSource(StringBuilder pSb, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime, int pMode, CeSource pSource) {
		processSentencesForSource(pSb, pSourceType, pTxnName, pExtraInfo, pStartTime, pMode, pSource);
	}

	private void processSentencesForExistingSource(BufferedReader pBr, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime, int pMode, CeSource pSource) {
		processSentencesForSource(pBr, pSourceType, pTxnName, pExtraInfo, pStartTime, pMode, pSource);
	}

	private CeSource processSentencesForSource(BufferedReader pReader, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime, int pMode, CeSource pSource) {
		CeSource thisSrc = null;
		String txnName = null;
		
		if (pTxnName.isEmpty()) {
			txnName = "";
		} else {
			txnName = pTxnName + " ";
		}

		if (pMode == SENMODE_NORMAL) {
			if (pSource != null) {
				thisSrc = pSource;
				this.ac.setCurrentSource(pSource);

				this.ac.getModelBuilder().saveSource(thisSrc);
			} else {
				thisSrc = createCeSource(pSourceType, pTxnName, pExtraInfo);
			}
			if (thisSrc != null) {
				this.ac.setLastSource(thisSrc);
				this.sp.doParsing(pReader, pMode);
				recordSentenceAddingResults(pStartTime, 0, txnName + "sentence processing complete");
			} else {
				reportError("Failed to create CeSource.  All sentences from " + pSourceType + " '" + pTxnName + "' have been ignored.", this.ac);
			}
		} else if (pMode == SENMODE_VALIDATE) {
			this.sp.doParsing(pReader, pMode);
			recordSentenceAddingResults(pStartTime, 0, txnName + "sentence processing complete");
		} else {
			reportError("Unexpected mode specified during sentence processing (" + new Integer(pMode).toString() + ")", this.ac);
		}

		return thisSrc;
	}

	private CeSource processSentencesForSource(StringBuilder pSb, int pSourceType, String pTxnName, String pExtraInfo, long pStartTime, int pMode, CeSource pSource) {
		CeSource thisSrc = null;
		String txnName = null;
		
		if (pTxnName.isEmpty()) {
			txnName = "";
		} else {
			txnName = pTxnName + " ";
		}

		if (pMode == SENMODE_NORMAL) {
			if (pSource != null) {
				thisSrc = pSource;
				this.ac.getModelBuilder().saveSource(thisSrc);
			} else {
				thisSrc = createCeSource(pSourceType, pTxnName, pExtraInfo);
			}
			if (thisSrc != null) {
				this.ac.setLastSource(thisSrc);
				this.sp.doParsing(pSb, pMode);
				recordSentenceAddingResults(pStartTime, 0, txnName + "sentence processing complete");
			} else {
				reportError("Failed to create CeSource.  All sentences from " + pSourceType + " '" + pTxnName + "' have been ignored.", this.ac);
			}
		} else if (pMode == SENMODE_VALIDATE) {
			this.sp.doParsing(pSb, pMode);
			recordSentenceAddingResults(pStartTime, 0, txnName + "sentence processing complete");
		} else {
			reportError("Unexpected mode specified during sentence processing (" + new Integer(pMode).toString() + ")", this.ac);
		}

		return thisSrc;
	}

	private CeSource createCeSource(int pSourceType, String pDetail, String pExtraInfo) {
		CeSource thisSource = setupEnvironment(pDetail, pSourceType, pExtraInfo);

		if (thisSource != null) {
			this.ac.getModelBuilder().saveSource(thisSource);
		}

		return thisSource;
	}

	private void recordSentenceAddingResults(long pStartTime, int pTxns, String pTxnName) {
		if (isReportMicroDebug()) {
			// Summarise the sentences, transactions and execution time
			String msgTemplate = "%01 - %02 valid, %03 invalid, %04 total txns, in %05 secs.";

			TreeMap<String, String> ceParms = new TreeMap<String, String>();
			ceParms.put("%01", pTxnName);
			ceParms.put("%02", Integer.toString(this.senStats.getValidSentenceCount()));
			ceParms.put("%03", Integer.toString(this.senStats.getInvalidSentenceCount()));
			ceParms.put("%04", Integer.toString(pTxns));
			ceParms.put("%05", formattedDuration(pStartTime));

			String actualMsg = substituteNormalParameters(msgTemplate, ceParms);

			reportMicroDebug(actualMsg, this.ac);
		}
	}

	private CeSource setupEnvironment(String pDetail, int pSourceType, String pExtraInfo) {
		CeSource thisSource = null;

		switch (pSourceType) {
			case CeSource.SRCTYPE_ID_URL:
				thisSource = CeSource.createNewUrlSource(this.ac, pDetail, null);
				break;
			case CeSource.SRCTYPE_ID_FILE:
				thisSource = CeSource.createNewFileSource(this.ac, pDetail, null);
				break;
			case CeSource.SRCTYPE_ID_FORM:
				thisSource = CeSource.createNewFormSource(this.ac, pDetail, null);
				break;
			case CeSource.SRCTYPE_ID_AGENT:
				thisSource = CeSource.createNewAgentSource(this.ac, pDetail, pExtraInfo, null);
				break;
			case CeSource.SRCTYPE_ID_INTERNAL:
				thisSource = CeSource.createNewInternalSource(this.ac, pDetail, null);
				break;
			case CeSource.SRCTYPE_ID_RULE:
				thisSource = CeSource.createNewRuleSource(this.ac, pDetail, null);
				break;
			case CeSource.SRCTYPE_ID_QUERY:
				thisSource = CeSource.createNewQuerySource(this.ac, pDetail, null);
				break;
			default:
				reportError("Unexpected sourceType encountered (" + Integer.toString(pSourceType) + ").  CeSource could not be created.", this.ac);
				break;
		}

		return thisSource;
	}

//	private void logFinalSentence(CeSentence pSentence) {
//		if (this.ac.getCeConfig().isLoggingCeToFiles()) {
//			//Rationale processing is handling the clauses in the conclusion so these sentences should not be saved
//			if (!this.isRationaleProcessing()) {
//				String prefixText = "";
//
//				if (pSentence.isCommandSentence()) {
//					//Command sentences must be commented out otherwise a reload will attempt to run those commands again
//					prefixText = "-- ";
//				}
//
//				appendToFileWithNewlines(this.ac, getAuditFilename(), prefixText + pSentence.getActualCeTextWithTokenSubstitutions());
//			}
//		}
//	}

//	private String getAuditFilename() {
//		if (this.auditFilename == null) {
//			this.auditFilename = this.ac.getCeConfig().getTempPath() + this.ac.getModelBuilder().calculateCeLoggingFilename();
//		}
//
//		return this.auditFilename;
//	}

	public boolean isSuppressingMessages() {
		return this.suppressMessages;
	}

	private void doPostSourceLoadActions(CeSource pSource) {
	  final String METHOD_NAME = "doPostSourceLoadActions";
	  logger.entering(CLASS_NAME, METHOD_NAME, pSource);
	  
		//Generate any specific conceptual models defined by sources
		StringBuilder sb = new StringBuilder();
		metamodelProcessAnnotationsForSource(pSource, sb);
		
		linkSourceToConcepts(pSource, sb);
		linkSourceToProperties(pSource, sb);

		giveParentsToFloatingConcepts();
		generateMetamodelSentencesFor(pSource, sb);

		//Send any notifications (by executing triggers)
		sendNotifications(pSource);

		//This is required, otherwise entities created by triggers don't get saved
		this.ac.getModelBuilder().updateCreatedThingsFrom(this.ac);
		
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	private void sendNotifications(CeSource pSource) {
		final String METHOD_NAME = "sendNotifications";
		
		CeConcept trigConcept = this.ac.getModelBuilder().getConceptNamed(this.ac, CON_CTE);

		if (trigConcept != null) {
			//TODO: A more efficient way of storing these (will issue a query to retrieve them every time currently)

			//Check for matches against each trigger instance
			for (CeInstance trigInst : this.ac.getModelBuilder().retrieveAllInstancesForConcept(trigConcept)) {
				//Check for concept matches
				sendNotificationsForConceptOrPropertyMatches(trigInst, pSource);

				//Check for rule matches
				sendNotificationsForRuleMatches(trigInst, pSource);

				//Check for query matches
				sendNotificationsForQueryMatches(trigInst, pSource);
			}
		}

		if (this.ac.isAutoExecutingRules()) {
			long sTime = System.currentTimeMillis();
			detectAllMatchedRules(pSource);
			reportExecutionTiming(this.ac, sTime, "[ru] executeAllMatchedRules, src=" + pSource.getId(), CLASS_NAME, METHOD_NAME);
		}

		//The matches and triggers have been processed so clear the notifications
		pSource.clearNotifications();
	}

	private void detectAllMatchedRules(CeSource pSource) {
		HashSet<CeRule> rulesToRun = new HashSet<CeRule>();

		for (CeRule newRule : pSource.getAffectedRules()) {
			if (newRule.hasAnyUnboundedCreationConclusionClauses()) {
				reportWarning("New rule '" + newRule.getRuleName() + "' will not be included in auto execution of rules as it creates new instances in the conclusion and would over-generate instances", this.ac);
			} else {
				rulesToRun.add(newRule);
			}
		}

		for (CeRule thisRule : this.ac.getModelBuilder().getAllRules().values()) {
			if (thisRule.hasAnyUnboundedCreationConclusionClauses()) {
				reportWarning("Rule '" + thisRule.getRuleName() + "' will not be included in auto execution of rules as it creates new instances in the conclusion and would over-generate instances", this.ac);
			} else {
				HashSet<CeProperty> premProps = thisRule.listAllPremiseProperties();

				//Check at concept level
				for (CeConcept premCon : thisRule.listAllPremiseConcepts()) {
					CopyOnWriteArrayList<CeConcept> affCons = new CopyOnWriteArrayList<CeConcept>(pSource.getAffectedConceptsPlusParents());

					for (CeConcept affCon : affCons) {
						if (affCon.equalsOrHasParent(premCon)) {
							if (isReportMicroDebug()) {
								reportMicroDebug(thisRule.getRuleName() + " is matched against premise concept " + premCon.getConceptName() + ", for source " + pSource.getId(), this.ac);
							}
							rulesToRun.add(thisRule);
						}
					}
				}

				if (!premProps.isEmpty()) {
					//Check at the property level
					for (CeProperty premProp : premProps) {
						//First check for range matches
						for (CeConcept affCon : pSource.getAffectedConcepts()) {
							CeConcept premRangeCon = premProp.getRangeConcept();

							if (premRangeCon != null) {
								if (affCon.equalsOrHasParent(premRangeCon)) {
									if (isReportMicroDebug()) {
										reportMicroDebug(thisRule.getRuleName() + " is matched against premise range concept " + premRangeCon.getConceptName() + ", for source " + pSource.getId(), this.ac);
									}
									rulesToRun.add(thisRule);
								}
							}
						}

						//Now check for property matches
						for (CeProperty affProp : pSource.getAffectedProperties()) {
							//First check for property matches
							if (premProp == affProp) {
								if (isReportMicroDebug()) {
									reportMicroDebug(thisRule.getRuleName() + " is directly matched against premise property " + premProp.getPropertyName() + ", for source " + pSource.getId(), this.ac);
								}
								rulesToRun.add(thisRule);
							} else {
								if (premProp.getInferredProperties() != null) {
									for (CeProperty infProp : premProp.getInferredProperties()) {
										if (infProp == affProp) {
											if (isReportMicroDebug()) {
												reportMicroDebug(thisRule.getRuleName() + " is indirectly matched (via " + infProp.getPropertyName() + ") against premise property " + premProp.getPropertyName() + ", for source " + pSource.getId(), this.ac);
											}
											rulesToRun.add(thisRule);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		//The rules only need to be executed once as they will automatically iterate
		this.ac.getModelBuilder().executeTheseRules(this.ac, new ArrayList<CeRule>(rulesToRun), 1, pSource);
	}

	private void sendNotificationsForConceptOrPropertyMatches(CeInstance pTrigInst, CeSource pSource) {
		CePropertyInstance tcPi = pTrigInst.getPropertyInstanceNamed("concept name");

		if (tcPi != null) {
			CeConcept targetConcept = null;

			for (String tcName : tcPi.getValueList()) {
				targetConcept = this.ac.getModelBuilder().getConceptNamed(this.ac, tcName);

				//Check for property matches (only if a target concept has been identified
				if (targetConcept != null) {
					CePropertyInstance tpPi = pTrigInst.getPropertyInstanceNamed(PROP_PROPNAME);

					if (tpPi != null) {
						//Check against each property
						for (String tpName : tpPi.getValueList()) {
							checkForPropertyTriggerMatches(targetConcept.calculatePropertiesNamed(tpName), pTrigInst, pSource, "", "");
						}
					} else {
						//No properties listed, so just check against the concept
						checkForConceptTriggerMatch(targetConcept, pTrigInst, pSource, "", "");
					}
				}
			}
		}
	}

	private void sendNotificationsForRuleMatches(CeInstance pTrigInst, CeSource pSource) {
		CePropertyInstance trPi = pTrigInst.getPropertyInstanceNamed("affected rule name");
		if (trPi != null) {
			for (String ruleName : trPi.getValueList()) {
				CeRule targetRule = this.ac.getModelBuilder().getRuleNamed(ruleName);

				if(targetRule != null) {
					ArrayList<CeConcept> refCons = targetRule.getAllReferencedPremiseConcepts();
					checkForConceptTriggerMatches(refCons, pTrigInst, pSource, "RULE", targetRule.getRuleName());

					ArrayList<CeProperty> refProps = targetRule.getAllReferencedPremiseProperties();
					checkForPropertyTriggerMatches(refProps, pTrigInst, pSource, "RULE", targetRule.getRuleName());
				} else {
					reportWarning("Rule named '" + ruleName + "' could not be located during trigger execution for trigger event '" + pTrigInst.getInstanceName() + "'", this.ac);
				}
			}
		}
	}

	private void sendNotificationsForQueryMatches(CeInstance pTrigInst, CeSource pSource) {
		CePropertyInstance tqPi = pTrigInst.getPropertyInstanceNamed("affected query name");
		if (tqPi != null) {
			for (String queryName : tqPi.getValueList()) {
				CeQuery targetQuery = this.ac.getModelBuilder().getQueryNamed(queryName);

				if(targetQuery != null) {
					ArrayList<CeConcept> refCons = targetQuery.getAllReferencedPremiseConcepts();
					checkForConceptTriggerMatches(refCons, pTrigInst, pSource, "QUERY", targetQuery.getQueryName());

					ArrayList<CeProperty> refProps = targetQuery.getAllReferencedPremiseProperties();
					checkForPropertyTriggerMatches(refProps, pTrigInst, pSource, "QUERY", targetQuery.getQueryName());					
				} else {
					reportWarning("Query named '" + queryName + "' could not be located during trigger execution for trigger event '" + pTrigInst.getInstanceName() + "'", this.ac);
				}
			}
		}
	}

	private void checkForConceptTriggerMatches(ArrayList<CeConcept> pTargetConcepts, CeInstance pTrigInst, CeSource pSource, String pExtraType, String pExtraName) {
		for (CeConcept thisCon : pTargetConcepts) {
			checkForConceptTriggerMatch(thisCon, pTrigInst, pSource, pExtraType, pExtraName);
		}
	}

	private void checkForConceptTriggerMatch(CeConcept pTargetConcept, CeInstance pTrigInst, CeSource pSource, String pExtraType, String pExtraName) {
		ArrayList<CeConcept> copyCons = new ArrayList<CeConcept>(pSource.getAffectedConceptsPlusParents());

		for (CeConcept candidateCon : copyCons) {
			if (candidateCon.equalsOrHasParent(pTargetConcept)) {
				notifyTriggerMatch(pTrigInst, "CONCEPT", candidateCon.getConceptName(), pSource.getId(), pExtraType, pExtraName);
			}
		}
	}

	private void checkForPropertyTriggerMatches(ArrayList<CeProperty> pTargetProps, CeInstance pTrigInst, CeSource pSource, String pExtraType, String pExtraName) {
		for (CeProperty targetProp : pTargetProps) {
			CopyOnWriteArrayList<CeProperty> copyList = new CopyOnWriteArrayList<CeProperty>(pSource.getAffectedProperties());

			boolean processedTrigger = false;

			for (CeProperty affProp : copyList) {
				if (!processedTrigger) {
					if (targetProp.equalsOrHasInferredProperty(affProp)) {
						notifyTriggerMatch(pTrigInst, "PROPERTY", targetProp.getPropertyName(), pSource.getId(), pExtraType, pExtraName);
						processedTrigger = true;
					}
				}
			}
		}
	}

	private void notifyTriggerMatch(CeInstance pTrigInst, String pType, String pName, String pSource, String pExtraType, String pExtraName) {
		final String METHOD_NAME = "notifyTriggerMatch";
		String notifyClassName = pTrigInst.getSingleValueFromPropertyNamed("class to notify");

		if (!notifyClassName.isEmpty()) {
			try {
				@SuppressWarnings("unchecked")
				// The user must specify a valid subclass of CeNotifyHandler
				Class<CeNotifyHandler> notifyClass = (Class<CeNotifyHandler>)Class.forName(notifyClassName);

				try {
					//Create a new action context for this notify
					NotifyActionContext nAc = new NotifyActionContext(this.ac, "(Notify Trigger)");

					CeNotifyHandler notifyInst = notifyClass.newInstance();
					notifyInst.notify(nAc, pType, pName, pTrigInst.getInstanceName(), pSource, pExtraType, pExtraName);
				} catch (IllegalAccessException e) {
					reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
				} catch (InstantiationException e) {
					reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
				}
			} catch (ClassNotFoundException e) {
				reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
			}
		} else {
			reportError("Class name was not specified for trigger '" + pTrigInst.getInstanceName() + "', so no notification could be sent.", this.ac);
		}
	}

	private void linkSourceToConcepts(CeSource pSource, StringBuilder pSb) {
		for (CeConcept thisCon : this.ac.getModelBuilder().listAllConcepts()) {
			for (CeSentence thisSen : thisCon.getPrimarySentences()) {
				if (pSource.equals(thisSen.getSource())) {
					//If the source defines a concept but has
					//no conceptual model then add the default one
					if (!pSource.hasAnyDefinedModel()) {
						CeConceptualModel defCm = pSource.addDefaultConceptualModel(this.ac);

						if (!defCm.metaModelHasBeenGenerated()) {
							ceMetamodelConceptualModel(pSb, defCm.getModelName());
							defCm.markAsMetaModelGenerated();
						}
					}

					for (CeConceptualModel thisCm : pSource.getDefinedModels()) {
						thisCm.addDefinedConcept(thisCon);
					}

				}
			}
		}
	}

	private void linkSourceToProperties(CeSource pSource, StringBuilder pSb) {
		for (CeProperty thisProp : this.ac.getModelBuilder().getAllProperties()) {
			for (CeSentence thisSen : thisProp.getPrimarySentences()) {
				if (pSource.equals(thisSen.getSource())) {
					for (CeConceptualModel thisCm : pSource.getDefinedModels()) {
						thisCm.addDefinedProperty(thisProp);
					}
				}
			}
		}
	}

	private void giveParentsToFloatingConcepts() {
		CeConcept defaultParent = HelperConcept.thingConcept(this.ac);

		for (CeConcept thisConcept : this.ac.getModelBuilder().listAllConcepts()) {
			if ((!HelperConcept.isThing(thisConcept)) && (!thisConcept.hasAnyParents())) {
				thisConcept.createParent(this.ac, defaultParent);
				//TODO: Should a new CE sentence be generated to communicate this assumption?
			}
		}
	}

	private void generateMetamodelSentencesFor(CeSource pSource, StringBuilder pSb) {
	  final String METHOD_NAME = "generateMetamodelSentencesFor";
	  logger.entering(CLASS_NAME, METHOD_NAME, new String[]{pSource.toString(), Integer.toString(pSb.length())});
	  
		long sTime = System.currentTimeMillis();

		if (pSb.length() > 0) {
			pSb.insert(0, "Note: These meta-model sentences are generated from the source '" + pSource.getDetail() + "' (" + pSource.getId() + ")\n\n");
		}

		for (CeConceptualModel thisCm : pSource.getDefinedModels()) {
			for (CeConcept thisCon : thisCm.getDefinedConcepts()) {
				metamodelProcessConceptToModelCe(pSource, thisCon, pSb);
			}
			for (CeProperty thisProp : thisCm.getDefinedProperties()) {
				metamodelProcessPropertyCe(thisProp, pSb);
			}
		}

		//Only save the CE if there was any CE actually created
		if (pSb.length() > 0) {
			CeSource mmSrc = this.ac.getModelBuilder().getSourceById(SRCID_POPMODEL);
			if (mmSrc == null) {
				CeSource tempCurrentSource = this.ac.getCurrentSource();
				mmSrc = CeSource.createNewInternalSource(this.ac, STEPNAME_POPMODEL, SRCID_POPMODEL);
				mmSrc.setParentSource(this.ac, tempCurrentSource);
			}
			
			CeSource otherSource = this.ac.getCurrentSource();

			//To prevent issues with the source being overwritten and the various affected properties and
			//concepts being lost this saving of meta-model CE is not done in the same way as trigger
			//notifications, but creating a new separate instance of ActionContext to perform the updates.
			NotifyActionContext nAc = new NotifyActionContext(this.ac, "(Notify Trigger)");

			nAc.setCurrentSource(mmSrc);
			nAc.setLastSource(mmSrc);
			ProcessorCe procCe = new ProcessorCe(nAc, false, this.senStats);

			procCe.processNormalSentencesFromSbWithExistingSource(pSb, CeSource.SRCTYPE_ID_INTERNAL, STEPNAME_POPMODEL, "", sTime, SENMODE_NORMAL, mmSrc);
			this.ac.setCurrentSource(otherSource);
		}
		
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/*
	 * This will return all models mentioned by annotations associated with ce
	 * processed by the source to date, regardless of when they were seen.
	 */
	private void metamodelProcessAnnotationsForSource(CeSource pSource, StringBuilder pSb) {
	  for (CeAnnotation thisAnno : pSource.getAnnotations()) {
		  if (thisAnno.isModelAnnotation() && !thisAnno.metaModelHasBeenGenerated()) {
			  thisAnno.markAsMetaModelGenerated();

			  createConceptualModel(thisAnno.getText(), pSource, pSb);
		  }
		}
	}

	private CeConceptualModel createConceptualModel(String pCmName, CeSource pSrc, StringBuilder pSb) {
		CeConceptualModel tgtCm = this.ac.getModelBuilder().getConceptualModel(pCmName);

		if (tgtCm == null) {
			tgtCm = CeConceptualModel.createConceptualModel(this.ac, pCmName, pSrc);
		} else {
			pSrc.addDefinedModel(tgtCm);
			tgtCm.addSource(pSrc);
		}
	
		if (!tgtCm.metaModelHasBeenGenerated()) {
			ceMetamodelConceptualModel(pSb, pCmName);
			tgtCm.markAsMetaModelGenerated();
		}
	
		return tgtCm;
	}

	private void metamodelProcessConceptToModelCe(CeSource pSrc, CeConcept pCon, StringBuilder pSb) {
		String conName = pCon.getConceptName();
		
		for (CeConceptualModel thisCm : pSrc.getDefinedModels()) {
			String cmName = thisCm.getModelName();
			ceMetamodelConceptualModelDefinesConcept(pSb, cmName, conName);
			thisCm.addDefinedConceptByName(this.ac, pCon.getConceptName());
		}

		//Only generate the concept details meta-CE if not already done
		if (!pCon.metaModelHasBeenGenerated()) {					
			ceMetamodelEntityConceptMain(pSb, conName);
			
			for (CeAnnotation thisAnno : pCon.getAnnotations()) {
				ceMetamodelEntityConceptAnnotation(pSb, conName, thisAnno.getText());
			}

			for (CeConcept thisParent : pCon.getDirectParents()) {
				ceMetamodelEntityConceptChild(pSb, thisParent.getConceptName(), conName);
			}
		}

		pCon.markAsMetaModelGenerated();
	}

	private static void metamodelProcessPropertyCe(CeProperty pProp, StringBuilder pSb) {
		if (!pProp.metaModelHasBeenGenerated()) {
			String propName = pProp.identityKey();
			String anAttOrRelConcept = pProp.isFunctionalNoun() ? "an " + CON_ATTCON : "a " + CON_RELCON;
			String aDataOrObjProp = pProp.isDatatypeProperty() ? "a " + CON_DTPROP : "an " + CON_OBPROP;
			String domainName = pProp.getDomainConcept().getConceptName();
			String rangeName = pProp.getRangeConceptName();
			String propertyName = pProp.getPropertyName();
			ceMetamodelProperty(pSb, pProp.isObjectProperty(), propName, anAttOrRelConcept, aDataOrObjProp, domainName, rangeName, propertyName);
		}

		pProp.markAsMetaModelGenerated();
	}

}
