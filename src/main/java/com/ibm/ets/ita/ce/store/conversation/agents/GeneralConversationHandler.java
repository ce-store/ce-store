package com.ibm.ets.ita.ce.store.conversation.agents;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_CANNOTSAVE;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOTRIGDETAILS;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOTRIGINST;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_SERVICE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_AFFILIATION;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CHECKAUTHORISEDUSERS;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CHECKNATIONALITIES;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DEBUG;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_FROMCONCONCEPT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_FROMINSTANCE;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_MILLISECONDSDELAY;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_RUNRULESONSAVE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_DELAY;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.MiscNames.FORM_CONVFACT;
import static com.ibm.ets.ita.ce.store.names.MiscNames.UID_PREFIX;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_FALSE;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.getBooleanValueFrom;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.agents.CeNotifyHandler;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public abstract class GeneralConversationHandler extends CeNotifyHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CLASS_NAME = GeneralConversationHandler.class.getName();
	private static final String PACKAGE_NAME = GeneralConversationHandler.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	protected ActionContext ac = null;
	protected CeInstance trigInst = null;
	private boolean oldDebug = false;
	protected CeInstance fromInst = null;
	protected CeInstance fromAffiliation = null;
	protected String fromConName = null;
	protected String fromInstName = null;
	private long waitTime = DEFAULT_DELAY;
	protected boolean isCheckingNationalities = false;
	protected boolean isCheckingForAuthorisedUsers = false;
	private boolean runRulesOnSave = false;

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
		// Cannot be done in constructore as this is a trigger class and is
		// generically instantiated by ce-store
		this.ac = pAc;
		this.oldDebug = this.ac.getCeConfig().isDebug();
	}

	protected void cleanup() {
		this.ac.getCeConfig().setDebug(this.oldDebug);
	}

	private String getConfigOptionalSingleValueNamed(String pPropName, String pDefaultValue) {
		String result = null;

		if (this.trigInst != null) {
			CePropertyInstance thisInst = this.trigInst.getPropertyInstanceNamed(pPropName);

			if (thisInst != null) {
				result = thisInst.getSingleOrFirstValue();
			} else {
				result = pDefaultValue;
			}
		} else {
			reportError(MSG_NOTRIGINST, this.ac);
			result = ES;
		}

		return result;
	}

	protected void extractTriggerDetailsUsing(String pTrigInstName) {
		this.trigInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, pTrigInstName);

		if (this.trigInst != null) {
			boolean triggerDebug = getBooleanValueFrom(getConfigOptionalSingleValueNamed(PROP_DEBUG, TOKEN_FALSE));

			if (triggerDebug) {
				this.ac.getCeConfig().setDebug(triggerDebug);
			}

			String msDelayText = this.trigInst.getSingleValueFromPropertyNamed(PROP_MILLISECONDSDELAY);

			if (!msDelayText.isEmpty()) {
				this.waitTime = new Long(msDelayText).longValue();
			}

			this.fromConName = this.trigInst.getSingleValueFromPropertyNamed(PROP_FROMCONCONCEPT);
			if (this.fromConName.isEmpty()) {
				this.fromConName = CON_SERVICE;
			}

			this.fromInst = this.trigInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_FROMINSTANCE);
			if (this.fromInst != null) {
				this.fromInstName = this.fromInst.getInstanceName();
				this.fromAffiliation = this.fromInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_AFFILIATION);
			}

			this.isCheckingNationalities = new Boolean(
					this.trigInst.getSingleValueFromPropertyNamed(PROP_CHECKNATIONALITIES)).booleanValue();
			this.isCheckingForAuthorisedUsers = new Boolean(
					this.trigInst.getSingleValueFromPropertyNamed(PROP_CHECKAUTHORISEDUSERS)).booleanValue();
			String rr = this.trigInst.getSingleValueFromPropertyNamed(PROP_RUNRULESONSAVE);

			if ((rr == null) || (rr.isEmpty())) {
				this.runRulesOnSave = false;
			} else {
				this.runRulesOnSave = new Boolean(rr).booleanValue();
			}
		} else {
			TreeMap<String, String> parms = new TreeMap<String, String>();
			parms.put("%01", pTrigInstName);

			reportError(MSG_NOTRIGDETAILS, parms, this.ac);
		}
	}

	public String generateNewUid() {
		return this.ac.getModelBuilder().getNextUid(this.ac, UID_PREFIX);
	}

	protected ArrayList<CeInstance> retrieveAllInstancesForConceptNamed(String pTgtConName, String pIgnoreConName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		ArrayList<CeInstance> allInstances = new ArrayList<CeInstance>();
		ModelBuilder mb = this.ac.getModelBuilder();
		CeConcept trigCon = mb.getConceptNamed(this.ac, pTgtConName);

		// Add any new instances created in this unit of work
		if (this.ac.getSessionCreations().getNewInstances() != null) {
			for (CeInstance thisInst : this.ac.getSessionCreations().getNewInstances()) {
				if (thisInst.isConcept(trigCon)) {
					allInstances.add(thisInst);
				}
			}
		}

		// Also add in all the existing instances in case they were created in a
		// previous step
		allInstances.addAll(mb.getAllInstancesForConceptNamed(this.ac, pTgtConName));

		// Now remove any instances that match the concept that is to be ignored
		if (pIgnoreConName != null) {
			CeConcept ignoreCon = mb.getConceptNamed(this.ac, pIgnoreConName);
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
		return saveCeTextWithFlag(pCeText, FORM_CONVFACT, this.runRulesOnSave, false);
	}

	protected ContainerSentenceLoadResult saveCeCardText(String pCeText, String pSrcName) {
		// No need to call all the rules and triggers if we are just saving CE
		// card sentences
		return saveCeTextWithFlag(pCeText, pSrcName, false, false);
	}

	private ContainerSentenceLoadResult saveCeTextWithFlag(String pCeText, String pSrcName, boolean pRunRules, boolean pRetInsts) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);

		boolean oldExecRules = this.ac.isAutoExecutingRules();
		this.ac.markAsAutoExecuteRules(pRunRules);

		CeSource tgtSrc = CeSource.createNewFormSource(this.ac, pSrcName, pSrcName);
		ContainerSentenceLoadResult result = sa.saveCeText(pCeText, tgtSrc, pRetInsts);

		this.ac.markAsAutoExecuteRules(oldExecRules);

		return result;
	}

	protected void waitForSomeTime() {
		final String METHOD_NAME = "waitForSomeTime";

		if (this.waitTime > 0) {
			try {
				synchronized (this) {
					this.wait(this.waitTime);
				}
			} catch (InterruptedException e) {
				reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
			}
		}
	}

	public void saveCeForNotifiedInstance(CeInstance pTgtInst, String pNewConName, String pSrcName) {
		StringBuilder sb = new StringBuilder();

		// TODO: Abstract these values?
		appendToSb(sb, "the %TGT_CON% '%TGT_INST%' is a %NEW_CON%.");

		TreeMap<String, String> ceParms = new TreeMap<String, String>();
		ceParms.put("%TGT_CON%", pTgtInst.getFirstLeafConceptName());
		ceParms.put("%TGT_INST%", pTgtInst.getInstanceName());
		ceParms.put("%NEW_CON%", pNewConName);

		String ceSentence = substituteCeParameters(sb.toString(), ceParms);
		saveCeCardText(ceSentence, pSrcName);
	}

	public void saveCeForConversationCard(String pTgtConName, String pTgtInstName, String pIrtConName,
			String pIrtInstName, String pFromConName, String pFromInstName, String pToConName,
			ArrayList<String> pToInstNames, String pPriCon, String pSecCon, String pSrcName,
			ArrayList<String> pAboutIds) {
		// TODO: Abstract these values?
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
			TreeMap<String, String> parms = new TreeMap<String, String>();
			parms.put("%01", pIrtInstName);

			reportError(MSG_CANNOTSAVE, parms, this.ac);
		}
	}

}
