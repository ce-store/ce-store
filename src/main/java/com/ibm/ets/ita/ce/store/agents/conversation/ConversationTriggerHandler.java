package com.ibm.ets.ita.ce.store.agents.conversation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.formattedDuration;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.concurrent.CopyOnWriteArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class ConversationTriggerHandler extends GeneralConversationHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_CARD = "card";
	private static final String PROP_ISTO = "is to";
	private static final String PROP_IRT = "is in reply to";
	private static final String PROP_FROM = "is from";

	@Override
	public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId, String pRuleOrQuery, String pRuleOrQueryName) {
		initialise(pAc);
		extractTriggerDetailsUsing(pTriggerName);

		//Only property matched triggers are handled
		if (pThingType.equals(TYPE_PROP)) {
			handlePropertyTrigger();
		} else {
			reportWarning("Unexpected trigger type (" + pThingType + ") for conversation trigger handler", this.ac);
		}

		cleanup();
	}

	private void handlePropertyTrigger() {
		if (this.ac.getSessionCreations().getNewInstances() != null) {
			//TODO: Review whether this CopyOnWriteArrayList is needed here
			CopyOnWriteArrayList<CeInstance> copyList = new CopyOnWriteArrayList<CeInstance>(this.ac.getSessionCreations().getNewInstances());

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
				reportDebug("Ignoring message '" + pCardInst.getInstanceName() + "' as it was not directed to me", this.ac);
			}
		}
	}

	private boolean isCardAlreadyProcessed(CeInstance pCardInst) {
		//The passed card instance is already processed if it has another card linked via
		//the "is in reply to" property, and that card has the user name of this agent
		//in the "is from" property.
		
		//More succinctly:
		//This card has already been processed if any other card from this agent is already
		//in reply to it.
		boolean result = false;

		CePropertyInstance irtPi = pCardInst.getReferringPropertyInstanceNamed(PROP_IRT);

		if (irtPi != null) {
			CeInstance replier = irtPi.getRelatedInstance().getSingleInstanceFromPropertyNamed(this.ac, PROP_FROM);

			if (replier != null) {
				result = replier.equals(this.fromInst);
			}
		}

		return result;
	}

	private boolean isThisCardForMe(CeInstance pCardInst) {
		//This card is for "me" (this agent) if the card has the name of this
		//agent in the "is to" property.
		CePropertyInstance tgtPi = pCardInst.getPropertyInstanceNamed(PROP_ISTO);
		boolean result = false;

		if (tgtPi != null) {
			result = tgtPi.hasValue(this.fromInstName);
		}

		return result;
	}

	protected void doMessageProcessing(CeInstance pCardInst) {
		long startTime = System.currentTimeMillis();

		waitForSomeTime();	//The duration to wait is specified in the trigger CE

		//TODO: Convert this over to the new Hudson implementation
//		ConversationProcessor cp = new ConversationProcessor(this.ac, this);
//		cp.dealWithThisInstance(pCardInst);

		reportDebug("Conversation processing took " + formattedDuration(startTime) + " seconds", this.ac);
	}

}