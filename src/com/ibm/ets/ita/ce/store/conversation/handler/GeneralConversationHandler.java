package com.ibm.ets.ita.ce.store.conversation.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.getBooleanValueFrom;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.agent.CeNotifyHandler;
import com.ibm.ets.ita.ce.store.conversation.generator.AnswerCeGenerator;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFactNormal;

public abstract class GeneralConversationHandler extends CeNotifyHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = GeneralConversationHandler.class.getName();
	private static final String PACKAGE_NAME = GeneralConversationHandler.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	protected static final String TYPE_CON = "CONCEPT";
	protected static final String TYPE_PROP = "PROPERTY";
	private static final String FORM_CONVFACT = "conv_facts";
	private static final String UID_PREFIX = "msg_";
	private static final long DEFAULT_DELAY = 0;

	protected static final String CON_TELLCARD = "tell card";
	private static final String CON_SERVICE = "service";
	private static final String CON_QUES = "question";
	private static final String PROP_FROMCON = "from concept";
	private static final String PROP_FROMINST = "from instance";
	private static final String PROP_MSDELAY = "milliseconds delay";
	private static final String BROKER_NAME = "Moira";
	protected static final String PROP_DEBUG = "debug";
	protected static final String PROP_AFF = "affiliation";
	protected static final String PROP_CHECKNAT = "check nationalities";
	protected static final String PROP_CHECKUSERS = "check authorised users";
	protected static final String PROP_GENCONVCE = "generate conversation ce";
	protected static final String PROP_RUNRULESONSAVE = "run rules on save";
	protected static final String PROP_USEDEFAULTSCORING = "use default scoring";
	protected static final String PROP_ISFROM = "is from";
	protected static final String PROP_SUBJ = "subject";
	protected static final String PROP_PRED = "predicate";

	protected ActionContext ac = null;
	protected CeInstance trigInst = null;
	private boolean oldDebug = false;
	protected CeInstance fromInst = null;
	protected CeInstance fromAffiliation = null;
	protected String fromConName = "";
	protected String fromInstName = "";
	private long waitTime = DEFAULT_DELAY;
	protected boolean isCheckingNationalities = false;
	protected boolean isCheckingForAuthorisedUsers = false;
	protected boolean isGeneratingConvCe = false;
	private boolean runRulesOnSave = false;
	private boolean useDefaultScoring = false;
	private ArrayList<CeInstance> allQuestions = null;

	public boolean isGeneratingConvCe() {
		return this.isGeneratingConvCe;
	}

	public boolean isUsingDefaultScoring() {
		return this.useDefaultScoring;
	}

	public boolean isCheckingNationalities() {
		return this.isCheckingNationalities;
	}

	public boolean isCheckingForAuthorisedUsers() {
		return this.isCheckingForAuthorisedUsers;
	}

	public String getFromConName() {
		return this.fromConName;
	}

	public String getFromInstName() {
		return this.fromInstName;
	}

	public CeInstance getFromAffiliation() {
		return this.fromAffiliation;
	}

	protected void initialise(ActionContext pAc) {
		this.ac = pAc;

		this.oldDebug = this.ac.getCeConfig().isDebug();
	}
	
	protected void cleanup() {
		this.ac.getCeConfig().setDebug(this.oldDebug);
	}

	private String getConfigOptionalSingleValueNamed(String pPropName, String pDefaultValue) {
		String result = "";

		if (this.trigInst != null) {
			CePropertyInstance thisInst = this.trigInst.getPropertyInstanceNamed(pPropName);
			
			if (thisInst != null) { 
				result = thisInst.getSingleOrFirstValue();
			} else {
				result = pDefaultValue;
			}
		} else {
			reportError("No trigger instance found", this.ac);
		}
		
		return result;
	}

	protected void extractTriggerDetailsUsing(String pTrigInstName) {
		this.trigInst = getModelBuilder().getInstanceNamed(this.ac, pTrigInstName);

		if (this.trigInst != null) {
			boolean triggerDebug = getBooleanValueFrom(getConfigOptionalSingleValueNamed(PROP_DEBUG, "false"));

			if (triggerDebug) {
				this.ac.getCeConfig().setDebug(triggerDebug); 
			}

			String msDelayText = this.trigInst.getSingleValueFromPropertyNamed(PROP_MSDELAY);

			if (!msDelayText.isEmpty()) {
				this.waitTime = new Long(msDelayText).longValue();
			}

			this.fromConName = this.trigInst.getSingleValueFromPropertyNamed(PROP_FROMCON);
			if (this.fromConName.isEmpty()) {
				this.fromConName = CON_SERVICE;
			}

			this.fromInst = this.trigInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_FROMINST);
			if (this.fromInst != null) {
				this.fromInstName = this.fromInst.getInstanceName();
				
				this.fromAffiliation = this.fromInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_AFF);
			} else {
				this.fromInstName = BROKER_NAME;
			}

			this.isCheckingNationalities = new Boolean(this.trigInst.getSingleValueFromPropertyNamed(PROP_CHECKNAT)).booleanValue();
			this.isCheckingForAuthorisedUsers = new Boolean(this.trigInst.getSingleValueFromPropertyNamed(PROP_CHECKUSERS)).booleanValue();
			this.isGeneratingConvCe = new Boolean(this.trigInst.getSingleValueFromPropertyNamed(PROP_GENCONVCE)).booleanValue();
			String rr = this.trigInst.getSingleValueFromPropertyNamed(PROP_RUNRULESONSAVE);
			
			if ((rr == null) || (rr.isEmpty())) {
				this.runRulesOnSave = false;
			} else {
				this.runRulesOnSave = new Boolean(rr).booleanValue();
			}

			String uds = this.trigInst.getSingleValueFromPropertyNamed(PROP_USEDEFAULTSCORING);
			
			if ((uds == null) || (uds.isEmpty())) {
				this.useDefaultScoring = true;
			} else {
				this.useDefaultScoring = new Boolean(uds).booleanValue();
			}
		} else {
			reportError("Unable to get trigger details for: " + pTrigInstName, this.ac);
		}
	}

	protected ModelBuilder getModelBuilder() {
		return this.ac.getModelBuilder();
	}

	public String generateNewUid() {
		return this.ac.getModelBuilder().getNextUid(this.ac, UID_PREFIX);
	}

	protected ArrayList<CeInstance> retrieveAllInstancesForConceptNamed(String pTgtConName, String pIgnoreConName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		ArrayList<CeInstance> allInstances = new ArrayList<CeInstance>();
		CeConcept trigCon = getModelBuilder().getConceptNamed(this.ac, pTgtConName);

		//Add any new instances created in this unit of work
		if (this.ac.getSessionCreations().getNewInstances() != null) {
			for (CeInstance thisInst : this.ac.getSessionCreations().getNewInstances()) {
				if (thisInst.isConcept(trigCon)) {
					allInstances.add(thisInst);
				}
			}
		}

		//Also add in all the existing instances in case they were created in a previous step
		allInstances.addAll(getModelBuilder().getAllInstancesForConceptNamed(this.ac, pTgtConName));

		//Now remove any instances that match the concept that is to be ignored
		if (pIgnoreConName != null) {
			CeConcept ignoreCon = getModelBuilder().getConceptNamed(this.ac, pIgnoreConName);
			for (CeInstance possInst : allInstances) {
				if (!possInst.isDirectConcept(ignoreCon)) {
					result.add(possInst);
				}
			}
		} else {
			result = allInstances;
		}

		return result;
	}

	public ContainerSentenceLoadResult saveCeText(String pCeText, CeInstance pConvInst) {
		String userName = pConvInst.getSingleValueFromPropertyNamed(PROP_ISFROM);

		validateCeText(pCeText, userName);

		return saveCeTextWithFlag(pCeText, FORM_CONVFACT, this.runRulesOnSave);
	}

	protected ContainerSentenceLoadResult saveCeCardText(String pCeText, String pSrcName) {
		//No need to call all the rules and triggers if we are just saving CE card sentences
		return saveCeTextWithFlag(pCeText, pSrcName, false);
	}

	private ContainerSentenceLoadResult saveCeTextWithFlag(String pCeText, String pSrcName, boolean pRunRules) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);

		boolean oldExecRules = this.ac.isAutoExecutingRules();
		this.ac.markAsAutoExecuteRules(pRunRules);

		CeSource tgtSrc = CeSource.createNewFormSource(this.ac, pSrcName, pSrcName);
		ContainerSentenceLoadResult result = sa.saveCeText(pCeText, tgtSrc);

		this.ac.markAsAutoExecuteRules(oldExecRules);

		return result;
	}

	private ContainerSentenceLoadResult validateCeText(String pCeText, String pUserName) {
		StringBuilder sb = new StringBuilder();
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);
		ContainerSentenceLoadResult result = sa.validateCeSentence(pCeText);

		if ((result != null) && (result.getValidatedSentences() != null)) {
			this.allQuestions = this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, CON_QUES);

			for (BuilderSentence bfSen : result.getValidatedSentences()) {
				generateAnswerCeFor(sb, bfSen, pUserName);
			}

			saveCeTextWithFlag(sb.toString(), "src_answers", false);
		}

		return result;
	}

	private void generateAnswerCeFor(StringBuilder pSb, BuilderSentence pSen, String pUserName) {
		//TODO: Move this elsewhere

		if (pSen.isFactSentenceNormal()) {
			AnswerCeGenerator acg = new AnswerCeGenerator(this.ac, pSb);
			BuilderSentenceFactNormal fSen = (BuilderSentenceFactNormal)pSen;

			if (fSen.getTargetConcept() != null) {
				String domCon = fSen.getTargetConcept().getConceptName();
				
				String domName = fSen.getInstanceName();
	
				for (CePropertyInstance pi : fSen.getObjectProperties()) {
					String propName = pi.getPropertyName();
					String fullPropName = pi.getRelatedProperty().formattedFullPropertyName();
					
					int i = 0;
					for (String thisVal : pi.getValueList()) {
						String rangeCon = pi.getRangeNameList().get(i++);
						String answerId = domName + "_" + propName + "_" + thisVal;
	
						acg.generateCeForAnswer(answerId, domCon, domName, rangeCon, thisVal, fullPropName, pUserName);
						
						for (CeInstance ques : findAllQuestionsMatching(domName, fullPropName, thisVal)) {
							acg.generateCeLinkingQuestionToAnswer(ques.getInstanceName(), answerId);
						}
					}
				}
				
	//			for (CePropertyInstance pi : fSen.getDatatypeProperties()) {
	//				String propName = pi.getPropertyName();
	//				String fullPropName = pi.getRelatedProperty().formattedFullPropertyName();
	//				
	//				for (String thisVal : pi.getValueList()) {
	//					String answerId = domName + "_" + propName + "_" + thisVal;
	//
	//					acg.generateCeForAnswer(answerId, domCon, domName, null, thisVal, fullPropName, pUserName);
	//					
	//					for (CeInstance ques : findAllQuestionsMatching(domName, fullPropName, thisVal)) {
	//						acg.generateCeLinkingQuestionToAnswer(ques.getInstanceName(), answerId);
	//					}
	//				}
	//			}

				//TODO: Add support for datatype properties
			}
		}
	}

	private ArrayList<CeInstance> findAllQuestionsMatching(String pDomName, String pFullPropName, String pRangeName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		for (CeInstance thisQues : this.allQuestions) {
			String qSub = thisQues.getSingleValueFromPropertyNamed(PROP_SUBJ);
			String qPred = thisQues.getSingleValueFromPropertyNamed(PROP_PRED);
			
			if ((qSub.equals(pDomName)) || (qSub.equals(pRangeName))) {
				if (qPred.equals(pFullPropName)) {
					result.add(thisQues);
				}
			}			
		}

		return result;
	}

	protected void waitForSomeTime() {
		final String METHOD_NAME = "waitForSomeTime";

		if (this.waitTime > 0) {
			try {
				synchronized (this) {
					this.wait(this.waitTime);
				}
			} catch(InterruptedException e) {
				reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
			}
		}
	}

	public void saveCeForNotifiedInstance(CeInstance pTgtInst, String pNewConName, String pSrcName) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "the %TGT_CON% '%TGT_INST%' is a %NEW_CON%.");

		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%TGT_CON%", pTgtInst.getFirstLeafConceptName());
		ceParms.put("%TGT_INST%", pTgtInst.getInstanceName());
		ceParms.put("%NEW_CON%", pNewConName);

		String ceSentence = substituteCeParameters(sb.toString(), ceParms);
		saveCeCardText(ceSentence, pSrcName);
	}

	public void saveCeForConversationCard(String pTgtConName, String pTgtInstName, String pIrtConName, String pIrtInstName, String pFromConName, String pFromInstName, String pToConName, ArrayList<String> pToInstNames, String pPriCon, String pSecCon, long pScoreVal, String pScoreExp, String pScoreType, String pSrcName, ArrayList<String> pAboutIds) {
		if (pTgtConName != null) {
			StringBuilder sb = new StringBuilder();
			TreeMap<String, String> ceParms = new TreeMap<String, String>();

			appendToSb(sb, "there is a %NEW_CON% named '%NEW_INST%' that");
			appendToSb(sb, "  has the timestamp '{now}' as timestamp and");

			if (pIrtConName != null) {
				appendToSb(sb, "  is in reply to the %IRT_CON% '%IRT_INST%' and");
			}

			if (pFromInstName != null) {
				appendToSb(sb, "  is from the %FROM_CON% '%FROM_INST%' and");
			}

			if (pToConName != null) {
				int ctr = 1;
				if (pToInstNames != null) {
					for (String thisToInst : pToInstNames) {
						String toId = "%TO_INST_" + ctr++ + "%";
						appendToSb(sb, "  is to the %TO_CON% '" + toId + "' and");

						ceParms.put(toId, thisToInst);
						ceParms.put("%TO_CON%", pToConName);
					}
				}
			}

			if (pAboutIds != null) {
				int anCtr = 1;
				for (String thisAn : pAboutIds) {
					String anId = "%ABOUT_" + anCtr++ + "%";
					appendToSb(sb, "  is about the thing '" + anId + "' and");
					ceParms.put(anId, thisAn);
				}
			}

			if (pSecCon != null) {
				appendToSb(sb, "  has '%PRI_CON%' as content and");
				appendToSb(sb, "  has '%SEC_CON%' as secondary content.");
			} else {
				appendToSb(sb, "  has '%PRI_CON%' as content.");				
			}

			if (pScoreVal != 0) {
				appendToSb(sb, "");
				appendToSb(sb, "there is a %SCORE_TYPE% score named 'score_%NEW_INST%' that");
				appendToSb(sb, "  is obtained from the %NEW_CON% '%NEW_INST%' and");
				appendToSb(sb, "  has '%SCORE_VAL%' as score value and");
				appendToSb(sb, "  has '%SCORE_EXP%' as score explanation.");
				appendToSb(sb, "");
				appendToSb(sb, "the %NEW_CON% '%NEW_INST%' is awarded the %SCORE_TYPE% score 'score_%NEW_INST%'.");

				ceParms.put("%SCORE_TYPE%", pScoreType);
				ceParms.put("%SCORE_VAL%", new Long(pScoreVal).toString());
				ceParms.put("%SCORE_EXP%", pScoreExp);
			}

			ceParms.put("%NEW_CON%", pTgtConName);
			ceParms.put("%NEW_INST%", pTgtInstName);
			ceParms.put("%IRT_CON%", pIrtConName);
			ceParms.put("%IRT_INST%", pIrtInstName);

			if (pFromInstName != null) {
				ceParms.put("%FROM_CON%", pFromConName);
				ceParms.put("%FROM_INST%", pFromInstName);
			}

			ceParms.put("%PRI_CON%", pPriCon);
			ceParms.put("%SEC_CON%", pSecCon);

			String ceSentence = substituteCeParameters(sb.toString(), ceParms);
			saveCeCardText(ceSentence, pSrcName);
		} else {
			reportError("Cannot save conversation card as no card type is specified, in response to: " + pIrtInstName, this.ac);
		}
	}

}