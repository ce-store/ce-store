package com.ibm.ets.ita.ce.store.conversation.trigger.interesting;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CeGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralProcessor;
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
        String interestedParty = inst.getSingleValueFromPropertyNamed("interested party");

        cg.generateInterestingFactCard(inst, th.getTriggerName(), interestedParty);
    }
}
