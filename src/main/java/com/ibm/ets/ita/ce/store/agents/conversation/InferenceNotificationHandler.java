package com.ibm.ets.ita.ce.store.agents.conversation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.NL;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;

public class InferenceNotificationHandler extends GeneralConversationHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String SRC_INF = "conv_inf";

	private static final String PROP_ISABOUT = "is about";
	private static final String PARM_IGNORECON = "ignore concept name";
	private static final String PARM_TOCON = "to concept";
	private static final String PARM_TOINST = "to instance";

	private String toConName = "";
	private ArrayList<String> toInstNames = null;
	private ArrayList<String> ignoreConNames = null;

	@Override
	public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId, String pRuleOrQuery, String pRuleOrQueryName) {
		initialise(pAc);
		getTriggerDetailsFrom(pTriggerName);

		//Only concept matched triggers are handled
		if (pThingType.equals(TYPE_CON)) {
			handleConceptTrigger(pThingName);
		} else {
			reportWarning("Unexpected trigger type (" + pThingType + ") for conversation trigger handler", this.ac);
		}

		cleanup();
	}

	private void getTriggerDetailsFrom(String pTrigInstName) {
		super.extractTriggerDetailsUsing(pTrigInstName);

		if (this.trigInst != null) {
			this.toConName = this.trigInst.getSingleValueFromPropertyNamed(PARM_TOCON);
			this.toInstNames = this.trigInst.getValueListFromPropertyNamed(PARM_TOINST);
			this.ignoreConNames = this.trigInst.getValueListFromPropertyNamed(PARM_IGNORECON);
		} else {
			reportError("Unable to get trigger details for: " + pTrigInstName, this.ac);
		}
	}

	private void handleConceptTrigger(String pMatchedConName) {
		for (CeInstance thisInst : retrieveAllInstancesForConceptNamed(pMatchedConName, null)) {
			if ((!shouldBeIgnored(thisInst)) && (!isAlreadyNotified(thisInst))) {
				waitForSomeTime();
				notifySentence(thisInst, pMatchedConName);
			}
		}
	}

	private boolean shouldBeIgnored(CeInstance pInst) {
		boolean result = false;

		for (String igConName : this.ignoreConNames) {
			if (pInst.isConceptNamed(this.ac, igConName)) {
				result = true;
				break;
			}
		}

		return result;
	}

	private boolean isAlreadyNotified(CeInstance pInst) {
		return !pInst.listAllReferringPropertyInstances(this.ac, PROP_ISABOUT).isEmpty();
	}

	private void notifySentence(CeInstance pInst, String pMatchedConName) {
		String importantSenText = "";
		String importantFullSenText = "";
		String secondarySenText = "";
		String secondaryFullSenText = "";

		for (CeSentence thisSen : pInst.getPrimarySentences()) {
			String ceText = thisSen.getCeTextWithoutRationale(this.ac);
			String fullCeText = thisSen.getCeText(this.ac);

			if (ceText.contains(pMatchedConName)) {
				if (!importantSenText.isEmpty()) {
					importantSenText += NL + NL;
				}
				importantSenText += ceText;

				if (!importantFullSenText.isEmpty()) {
					importantFullSenText += NL + NL;
				}
				importantFullSenText += fullCeText;
			} else {
				if (!secondarySenText.isEmpty()) {
					secondarySenText += NL + NL;
				}
				secondarySenText += ceText;

				if (!secondaryFullSenText.isEmpty()) {
					secondaryFullSenText += NL + NL;
				}
				secondaryFullSenText += fullCeText;
			}
		}

		if ((!importantSenText.isEmpty()) && (!secondarySenText.isEmpty())) {
			importantSenText += NL + NL;
		}

		if ((!importantFullSenText.isEmpty()) && (!secondaryFullSenText.isEmpty())) {
			importantFullSenText += NL + NL;
		}

		ArrayList<String> aboutInstNames = new ArrayList<String>();
		if (pInst != null) {
			aboutInstNames.add(pInst.getInstanceName());
		}

		saveCeForConversationCard(
			CON_TELLCARD,
			generateNewUid(),
			null,
			null,
			this.fromConName,
			this.fromInstName,
			this.toConName,
			this.toInstNames,
			(importantSenText + secondarySenText),
			(importantFullSenText + secondaryFullSenText),
			0,
			null,
			null,
			SRC_INF,
			aboutInstNames);
	}

}