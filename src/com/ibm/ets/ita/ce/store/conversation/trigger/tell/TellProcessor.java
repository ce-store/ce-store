package com.ibm.ets.ita.ce.store.conversation.trigger.tell;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.ConvCeGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralProcessor;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Property;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class TellProcessor extends GeneralProcessor {

    private TellTriggerHandler th = null;
    private ConvCeGenerator ce;

    public TellProcessor(ActionContext ac, TellTriggerHandler th) {
        this.ac = ac;
        this.th = th;
        cg = new CardGenerator(ac);
        ce = new ConvCeGenerator(ac);
    }

    // Process Tell card
    public void process(CeInstance cardInst) {
        String tellText = cardInst.getSingleValueFromPropertyNamed(Property.CONTENT.toString());
        String fromService = cardInst.getSingleValueFromPropertyNamed(Property.IS_FROM.toString());
        System.out.println("Tell text: " + tellText);

        if (isValidCe(tellText)) {
            // Valid CE - accept and add to store
            System.out.println("Save valid CE");
            String source = ce.generateSrcName(th.getTriggerName());
            ce.save(tellText, source);
            cg.generateTellReplyCard(cardInst, tellText, th.getTriggerName(), fromService);
        } else {
            // Not valid CE - reject card
            System.out.println("Not valid CE");
        }
    }
}
