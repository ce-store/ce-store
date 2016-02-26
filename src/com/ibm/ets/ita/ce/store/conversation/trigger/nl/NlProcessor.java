package com.ibm.ets.ita.ce.store.conversation.trigger.nl;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralProcessor;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Property;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Reply;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class NlProcessor extends GeneralProcessor {

    private NlTriggerHandler th = null;

    public NlProcessor(ActionContext ac, NlTriggerHandler th) {
        this.ac = ac;
        this.th = th;
        cg = new CardGenerator(ac);
    }

    // Process NL card
    public void process(CeInstance cardInst) {
        String convText = cardInst.getSingleValueFromPropertyNamed(Property.CONTENT.toString());
        String modConvText = appendDotIfNeeded(convText);
        System.out.println("Conv text: " + modConvText);

        // Test for valid CE
        if (isValidCe(modConvText)) {
            // Valid CE -> generate Tell card
            System.out.println("Valid CE");
            cg.generateTellCard(cardInst, modConvText, th.getFromInstName(), th.getTellServiceName());
        } else {
            // NL -> determine meaning
            System.out.println("Not valid CE");
            if (fromTellService(cardInst)) {
                System.out.println("From tell service");
                handleTellResponse(cardInst, convText);
            }
        }
    }

    private void handleTellResponse(CeInstance cardInst, String convText) {
        if (convText.equals(Reply.SAVED.toString())) {
            System.out.println("Is saved response");
            String humanAgent = findHumanAgent(cardInst);
            cg.generateNLCard(cardInst, convText, th.getFromInstName(), humanAgent);
        }
    }

    private String findHumanAgent(CeInstance cardInst) {
        System.out.println("\nLook for human agent...");
        CeInstance prevCard = cardInst.getSingleInstanceFromPropertyNamed(ac, Property.IN_REPLY_TO.toString());

        while (fromTellService(prevCard) || fromNLService(prevCard)) {
            prevCard = prevCard.getSingleInstanceFromPropertyNamed(ac, Property.IN_REPLY_TO.toString());
            System.out.println(prevCard);

            if (prevCard == null) {
                return null;
            }
        }

        String humanAgent = prevCard.getSingleValueFromPropertyNamed(Property.IS_FROM.toString());
        System.out.println("Human agent: " + humanAgent);
        return humanAgent;
    }

    private boolean fromNLService(CeInstance cardInst) {
        String fromService = cardInst.getSingleValueFromPropertyNamed(Property.IS_FROM.toString());
        return fromService.equals(th.getFromInstName());
    }

    private boolean fromTellService(CeInstance cardInst) {
        String fromService = cardInst.getSingleValueFromPropertyNamed(Property.IS_FROM.toString());
        return fromService.equals(th.getTellServiceName());
    }

    // Trim leading and trailing whitespace and append full stop if needed
    private String appendDotIfNeeded(String text) {
        String result = text.trim();

        if (!result.endsWith(".")) {
            result += ".";
        }

        return result;
    }
}
