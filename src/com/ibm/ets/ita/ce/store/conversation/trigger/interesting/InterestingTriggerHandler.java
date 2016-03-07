package com.ibm.ets.ita.ce.store.conversation.trigger.interesting;

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.HashSet;

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

        System.out.println("thingType: " + thingType);
        System.out.println("thingName: " + thingName);
        System.out.println("triggerName: " + triggerName);
        System.out.println("sourceId: " + sourceId);
        System.out.println("ruleOrQuery: " + ruleOrQuery);
        System.out.println("ruleOrQueryName: " + ruleOrQueryName);

        if (thingType.toLowerCase().equals(Type.PROPERTY.toString())) {
            // Triggered on property
            handlePropertyTrigger();
        } else if (thingType.toLowerCase().equals(Type.CONCEPT.toString())) {
            // Triggered on concept
            handleConceptTrigger();
        } else {
            reportWarning("Unexpected trigger type (" + thingType + ") for conversation trigger handler", ac);
        }
    }

    // Handle new properties
    private void handlePropertyTrigger() {
        HashSet<CeInstance> newInstances = ac.getSessionCreations().getNewInstances();

        if (newInstances != null) {
            InterestingProcessor ip = new InterestingProcessor(ac, this);

            for (CeInstance inst : newInstances) {
                // TODO: Fix for instances with parent concepts of interesting thing
                if (inst.isConceptNamed(ac, Concept.INTERESTING.toString())) {
                    System.out.println("New interesting thing: ");
                    System.out.println(inst);
                    ip.process(inst);
                }
            }
        }
    }

    // Handle concepts
    private void handleConceptTrigger() {
        // TODO Auto-generated method stub

    }
}
