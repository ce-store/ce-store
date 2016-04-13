package com.ibm.ets.ita.ce.store.conversation.trigger.interesting;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralProcessor;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Property;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class InterestingProcessor extends GeneralProcessor {

    private InterestingTriggerHandler th = null;

    public InterestingProcessor(ActionContext ac, InterestingTriggerHandler th) {
        this.ac = ac;
        this.th = th;
        cg = new CardGenerator(ac);
    }

    // Process interesting thing
    public void process(CeInstance inst) {
        ArrayList<CeInstance> interestedParties = inst.getInstanceListFromPropertyNamed(ac, Property.INTERESTED_PARTY.toString());
        ArrayList<CeInstance> uninterestedParties = inst.getInstanceListFromPropertyNamed(ac, Property.UNINTERESTED_PARTY.toString());

        for (CeInstance interestedParty : interestedParties) {
            boolean sendFact = true;

            String interestedUser = interestedParty.getSingleValueFromPropertyNamed("user");
            String interestedTimestamp = interestedParty.getSingleValueFromPropertyNamed("timestamp");

            for (CeInstance uninterestedParty : uninterestedParties) {
                String uninterestedUser = uninterestedParty.getSingleValueFromPropertyNamed("user");

                // Check if user has registered uninterest more recently than interest
                if (interestedUser.equals(uninterestedUser)) {
                    String uninterestedTimestamp = uninterestedParty.getSingleValueFromPropertyNamed("timestamp");

                    if (Long.parseLong(interestedTimestamp, 10) < Long.parseLong(uninterestedTimestamp, 10)) {
                        sendFact = false;
                    }
                }
            }

            if (sendFact) {
                cg.generateInterestingFactCard(inst, th.getTriggerName(), interestedUser);
            }
        }
    }
}
