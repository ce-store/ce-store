package com.ibm.ets.ita.ce.store.conversation.trigger.interesting;

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Concept;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralTriggerHandler;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Type;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class InterestingTriggerHandler extends GeneralTriggerHandler {

    @Override
    public void notify(ActionContext ac, String thingType, String thingName, String triggerName, String sourceId,
            String ruleOrQuery, String ruleOrQueryName) {
        System.out.println("Interesting thing trigger notification received");

        initialise(ac);
        extractTriggerDetailsUsing(triggerName);

        if (thingType.toLowerCase().equals(Type.CONCEPT.toString())) {
            if (thingName.equals(Concept.INTERESTING.toString())) {
                // Triggered on concept
                handleConceptTrigger();
            }
        } else {
            reportWarning("Unexpected trigger type (" + thingType + ") for conversation trigger handler", ac);
        }
    }

    // Handle concepts
    private void handleConceptTrigger() {
        HashSet<CeInstance> newInstances = ac.getSessionCreations().getNewInstances();

        if (newInstances != null) {
            CopyOnWriteArrayList<CeInstance> copiedInstances = new CopyOnWriteArrayList<CeInstance>(ac.getSessionCreations().getNewInstances());

            InterestingProcessor ip = new InterestingProcessor(ac, this);

            for (CeInstance inst : copiedInstances) {
                // TODO: Fix for instances with parent concepts of interesting thing
                if (inst.isConceptNamed(ac, Concept.INTERESTING.toString())) {
                    System.out.println("Interesting thing mentioned: ");
                    System.out.println(inst);
                    ip.process(inst);
                }
            }
        }
    }
}
