package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class CardGenerator {

    private CeGenerator ce;

    public CardGenerator(ActionContext ac) {
        ce = new CeGenerator(ac);
    }

    // Tell cards are used to add valid CE to the store
    public void generateTellCard(CeInstance cardInst, String convText, String fromService, String toService) {
        System.out.println("\nGenerate tell card:");

        generateCard(Card.TELL.toString(), convText, fromService, toService, cardInst.getInstanceName());
    }

    // Reply to service that sent the Tell card if its card has been accepted or not
    public void generateTellReplyCard(CeInstance cardInst, String tellText, String fromService,
            String toService) {
        System.out.println("\nGenerate reply tell card:");

        generateCard(Card.NL.toString(), Reply.SAVED.message(), fromService, toService, cardInst.getInstanceName());
    }

    // Generate NL card
    public void generateNLCard(CeInstance cardInst, String convText, String fromService, String toService, ArrayList<String> referencedInsts) {
        System.out.println("\nGenerate nl card:");
        System.out.println("Matched instances: " + referencedInsts);
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "there is a %CARD_TYPE% named '%CARD_NAME%' that");
        appendToSb(sb, "  has the timestamp '{now}' as timestamp and");
        appendToSb(sb, "  has '%CONV_TEXT%' as content and");
        appendToSb(sb, "  is from the service '%FROM_SERV%' and");
        appendToSb(sb, "  is to the service '%TO_SERV%' and");

        if (referencedInsts != null) {
            int numReferences = referencedInsts.size();

            if (numReferences > 0) {
                appendToSb(sb, "  is in reply to the card '%PREV_CARD%' and");

                for (int i = 0; i < numReferences; ++i) {
                    String inst = referencedInsts.get(i);
                    appendToSbNoNl(sb, "  is about the thing '");
                    appendToSbNoNl(sb, inst);

                    if (i < numReferences - 1) {
                        appendToSb(sb, "' and");
                    } else {
                        appendToSbNoNl(sb, "'.");
                    }
                }
            } else {
                appendToSb(sb, "  is in reply to the card '%PREV_CARD%'.");
            }
        } else {
            appendToSb(sb, "  is in reply to the card '%PREV_CARD%'.");
        }

        ceParms.put("%CARD_TYPE%", Card.NL.toString());
        ceParms.put("%CARD_NAME%", ce.generateNewUid());
        ceParms.put("%CONV_TEXT%", convText);
        ceParms.put("%FROM_SERV%", fromService);
        ceParms.put("%TO_SERV%", toService);
        ceParms.put("%PREV_CARD%", cardInst.getInstanceName());

        String ceSentence = substituteCeParameters(sb.toString(), ceParms);
        String source = ce.generateSrcName(fromService);
        System.out.println(ceSentence);
        ce.save(ceSentence, source);
    }

    // User has declared their interest in something - create new interesting thing
    public void generateInterestingCard(CeInstance cardInst, CeConcept concept, String fromService,
            String toService) {
        System.out.println("\nGenerate interesting things card:");

        String content = ce.generateInterestingThing(concept);
        generateCard(Card.TELL.toString(), content, fromService, toService, cardInst.getInstanceName());
    }

    // Found an interesting thing - send to interested user
    public void generateInterestingFactCard(CeInstance inst, String fromService, String toService) {
        System.out.println("\nGenerate interesting thing found card:");

        String content = "I've found new information on " + inst.getInstanceName() + ".";
        generateCard(Card.NL.toString(), content, fromService, toService, null);
    }

    // Generate generic card
    private void generateCard(String cardType, String content, String fromService, String toService, String prevCard) {
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "there is a %CARD_TYPE% named '%CARD_NAME%' that");
        appendToSb(sb, "  has the timestamp '{now}' as timestamp and");
        appendToSb(sb, "  has '%CONV_TEXT%' as content and");
        appendToSb(sb, "  is from the service '%FROM_SERV%' and");

        if (prevCard != null) {
            appendToSb(sb, "  is to the service '%TO_SERV%' and");
            appendToSb(sb, "  is in reply to the card '%PREV_CARD%'.");
        } else {
            appendToSb(sb, "  is to the service '%TO_SERV%'.");
        }

        ceParms.put("%CARD_TYPE%", cardType);
        ceParms.put("%CARD_NAME%", ce.generateNewUid());
        ceParms.put("%CONV_TEXT%", content);
        ceParms.put("%FROM_SERV%", fromService);
        ceParms.put("%TO_SERV%", toService);

        if (prevCard != null) {
            ceParms.put("%PREV_CARD%", prevCard);
        }

        String ceSentence = substituteCeParameters(sb.toString(), ceParms);
        String source = ce.generateSrcName(fromService);
        System.out.println(ceSentence);
        ce.save(ceSentence, source);
    }
}
