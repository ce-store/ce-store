package com.ibm.ets.ita.ce.store.conversation.trigger.nl;

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.formattedDuration;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.concurrent.CopyOnWriteArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Card;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralTriggerHandler;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class NlTriggerHandler extends GeneralTriggerHandler {

    @Override
    public void notify(ActionContext ac, String thingType, String thingName, String triggerName, String sourceId,
            String ruleOrQuery, String ruleOrQueryName) {
        System.out.println("NL trigger notification received");

        initialise(ac);
        extractTriggerDetailsUsing(triggerName);

        // Only property matched triggers are handled
        if (thingType.equals(TYPE_PROP)) {
            handlePropertyTrigger();
        } else {
            reportWarning("Unexpected trigger type (" + thingType + ") for conversation trigger handler", ac);
        }
    }

    // Check new instances are NL cards
    private void handlePropertyTrigger() {
        if (ac.getSessionCreations().getNewInstances() != null) {
            //TODO: Review whether this CopyOnWriteArrayList is needed here
            CopyOnWriteArrayList<CeInstance> copyList = new CopyOnWriteArrayList<CeInstance>(ac.getSessionCreations().getNewInstances());

            for (CeInstance thisInst : copyList) {
                if (thisInst.isConceptNamed(ac, Card.NL.toString())) {
                    handleCardInstance(thisInst);
                }
            }
        }
    }

    // Check card hasn't already been processed and is for this agent
    private void handleCardInstance(CeInstance cardInst) {
        if (!isCardAlreadyProcessed(cardInst)) {
            if (isThisCardForMe(cardInst)) {
                doMessageProcessing(cardInst);
            } else {
                reportDebug("Ignoring message '" + cardInst.getInstanceName() + "' as it was not directed to me", ac);
            }
        }
    }

    // Process NL message
    protected void doMessageProcessing(CeInstance cardInst) {
        long startTime = System.currentTimeMillis();

        NlProcessor nlp = new NlProcessor(ac, this);
        nlp.process(cardInst);

        reportDebug("Conversation processing took " + formattedDuration(startTime) + " seconds", ac);
    }
}
