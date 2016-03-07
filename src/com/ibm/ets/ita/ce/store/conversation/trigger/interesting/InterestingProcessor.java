package com.ibm.ets.ita.ce.store.conversation.trigger.interesting;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CeGenerator;
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

    // Process Tell card
    public void process(CeInstance cardInst) {
        String interestingText = cardInst.getSingleValueFromPropertyNamed(Property.CONTENT.toString());
        String fromService = cardInst.getSingleValueFromPropertyNamed(Property.IS_FROM.toString());
        System.out.println("Interesting text: " + interestingText);

//        if (isValidCe(interestingText)) {
//            // Valid CE - accept and add to store
//            System.out.println("Save valid CE");
//            String source = ce.generateSrcName(th.getTriggerName());
//            ce.save(interestingText, source);
//            cg.generateTellReplyCard(cardInst, interestingText, th.getTriggerName(), fromService);
//        } else {
//            // Not valid CE - reject card
//            System.out.println("Not valid CE");
//        }
    }
}
