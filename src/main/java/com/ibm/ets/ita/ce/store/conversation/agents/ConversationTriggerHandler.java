package com.ibm.ets.ita.ce.store.conversation.agents;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_EXECTIME;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_IGNORINGMSG;
import static com.ibm.ets.ita.ce.store.messages.ConversationMessages.MSG_UNEXPECTEDTRIGGER;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ISFROM;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ISINREPLYTO;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ISTO;
import static com.ibm.ets.ita.ce.store.names.MiscNames.TYPE_PROP;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.formattedDuration;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ibm.ets.ita.ce.store.conversation.processor.ConversationProcessor;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class ConversationTriggerHandler extends GeneralConversationHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	@Override
	public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId,
			String pRuleOrQuery, String pRuleOrQueryName) {
		initialise(pAc);
		extractTriggerDetailsUsing(pTriggerName);

		// Only property matched triggers are handled
		if (pThingType.equals(TYPE_PROP)) {
			handlePropertyTrigger();
		} else {
			TreeMap<String, String> parms = new TreeMap<String, String>();
			parms.put("%01", pThingType);
			parms.put("%02", "conversation trigger handler");

			reportWarning(MSG_UNEXPECTEDTRIGGER, parms, this.ac);
		}

		cleanup();
	}

	private void handlePropertyTrigger() {
		if (this.ac.getSessionCreations().getNewInstances() != null) {
			// TODO: Review whether this CopyOnWriteArrayList is needed here
			CopyOnWriteArrayList<CeInstance> copyList = new CopyOnWriteArrayList<CeInstance>(
					this.ac.getSessionCreations().getNewInstances());

			for (CeInstance thisInst : copyList) {
				if (thisInst.isConceptNamed(this.ac, CON_CARD)) {
					handleCardInstance(thisInst);
				}
			}
		}
	}

	private void handleCardInstance(CeInstance pCardInst) {
		if (!isCardAlreadyProcessed(pCardInst)) {
			if (isThisCardForMe(pCardInst)) {
				doMessageProcessing(pCardInst);
			} else {
				TreeMap<String, String> parms = new TreeMap<String, String>();
				parms.put("%01", pCardInst.getInstanceName());

				reportDebug(MSG_IGNORINGMSG, parms, this.ac);
			}
		}
	}

	private boolean isCardAlreadyProcessed(CeInstance pCardInst) {
		// The passed card instance is already processed if it has another card
		// linked via
		// the "is in reply to" property, and that card has the user name of
		// this agent
		// in the "is from" property.

		// More succinctly:
		// This card has already been processed if any other card from this
		// agent is already
		// in reply to it.
		boolean result = false;

		CePropertyInstance irtPi = pCardInst.getReferringPropertyInstanceNamed(PROP_ISINREPLYTO);

		if (irtPi != null) {
			CeInstance replier = irtPi.getRelatedInstance().getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);

			if (replier != null) {
				result = replier.equals(this.fromInst);
			}
		}

		return result;
	}

	private boolean isThisCardForMe(CeInstance pCardInst) {
		// This card is for "me" (this agent) if the card has the name of this
		// agent in the "is to" property.
		CePropertyInstance tgtPi = pCardInst.getPropertyInstanceNamed(PROP_ISTO);
		boolean result = false;

		if (tgtPi != null) {
			result = tgtPi.hasValue(this.fromInstName);
		}

		return result;
	}

	private void doMessageProcessing(CeInstance pCardInst) {
		long startTime = System.currentTimeMillis();

		waitForSomeTime(); // The duration to wait is specified in the trigger
							// CE

		ConversationProcessor cp = new ConversationProcessor(this.ac, this);
		cp.dealWithThisInstance(pCardInst);

		TreeMap<String, String> parms = new TreeMap<String, String>();
		parms.put("%01", formattedDuration(startTime));

		reportDebug(MSG_EXECTIME, parms, this.ac);
	}

}
