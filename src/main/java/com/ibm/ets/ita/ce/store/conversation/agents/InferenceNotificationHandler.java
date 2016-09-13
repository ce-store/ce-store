package com.ibm.ets.ita.ce.store.conversation.agents;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_NOTRIGDETAILS;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_UNEXPECTEDTRIGGER;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_TELLCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_IGNORECON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ISABOUT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_TOCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_TOINST;
import static com.ibm.ets.ita.ce.store.names.CeNames.SRC_INF;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.TYPE_CON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;

public class InferenceNotificationHandler extends GeneralConversationHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String toConName = null;
	private ArrayList<String> toInstNames = null;
	private ArrayList<String> ignoreConNames = null;

	@Override
	public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId,
			String pRuleOrQuery, String pRuleOrQueryName) {
		initialise(pAc);
		getTriggerDetailsFrom(pTriggerName);

		// Only concept matched triggers are handled
		if (pThingType.equals(TYPE_CON)) {
			handleConceptTrigger(pThingName);
		} else {
			TreeMap<String, String> parms = new TreeMap<String, String>();
			parms.put("%01", pThingType);
			parms.put("%02", "inference notification handler");

			reportWarning(MSG_UNEXPECTEDTRIGGER, parms, this.ac);
		}

		cleanup();
	}

	private void getTriggerDetailsFrom(String pTrigInstName) {
		super.extractTriggerDetailsUsing(pTrigInstName);

		if (this.trigInst != null) {
			this.toConName = this.trigInst.getSingleValueFromPropertyNamed(PROP_TOCON);
			this.toInstNames = this.trigInst.getValueListFromPropertyNamed(PROP_TOINST);
			this.ignoreConNames = this.trigInst.getValueListFromPropertyNamed(PROP_IGNORECON);
		} else {
			TreeMap<String, String> parms = new TreeMap<String, String>();
			parms.put("%01", pTrigInstName);

			reportError(MSG_NOTRIGDETAILS, this.ac);
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

		saveCeForConversationCard(CON_TELLCARD, generateNewUid(), null, null, this.fromConName, this.fromInstName,
				this.toConName, this.toInstNames, (importantSenText + secondarySenText),
				(importantFullSenText + secondaryFullSenText), SRC_INF, aboutInstNames);
	}

}
