package com.ibm.ets.ita.ce.store.conversation.trigger.interesting;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CeGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Concept;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralProcessor;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Property;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class InterestingProcessor extends GeneralProcessor {

    private InterestingTriggerHandler th = null;
    private CeGenerator ce;

    public InterestingProcessor(ActionContext ac, InterestingTriggerHandler th) {
        this.ac = ac;
        this.th = th;
        cg = new CardGenerator(ac);
        ce = new CeGenerator(ac);
    }

    // Process interesting thing
    public void process(CeInstance inst) {
        String interestedParty = inst.getSingleValueFromPropertyNamed(Property.INTERESTED_PARTY.toString());
        System.out.println("\nInterested party: " + interestedParty);

        if (interestedParty == null || interestedParty.isEmpty()) {
            System.out.println("No interested party");
            ArrayList<CeInstance> interestingInstances = ac.getModelBuilder().getAllInstancesForConceptNamed(ac, Concept.INTERESTING.toString());
            ArrayList<String> interestedParties = new ArrayList<String>();

            for (CeInstance interestingInst : interestingInstances) {
                String user = interestingInst.getSingleValueFromPropertyNamed(Property.INTERESTED_PARTY.toString());
                System.out.println("Interested party: " + user);

                if (user != null && !user.isEmpty()) {
                    interestedParties.add(user);
                    cg.generatePotentialInterestingThingCard(inst, th.getTriggerName(), user);
                }
            }
        } else {
            cg.generateInterestingFactCard(inst, th.getTriggerName(), interestedParty);
        }
    }
}
