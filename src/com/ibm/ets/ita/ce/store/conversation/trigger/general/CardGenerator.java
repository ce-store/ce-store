package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;

import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class CardGenerator {

    private CeGenerator ce;

    public CardGenerator(ActionContext ac) {
        ce = new CeGenerator(ac);
    }

    // Tell cards are used to add valid CE to the store
    public void generateTellCard(CeInstance cardInst, String convText, String fromService, String toService) {
        System.out.println("\nGenerate tell card:");
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "there is a %CARD_TYPE% named '%CARD_NAME%' that");
        appendToSb(sb, "  has the timestamp '{now}' as timestamp and");
        appendToSb(sb, "  has '%CONV_TEXT%' as content and");
        appendToSb(sb, "  is from the service '%FROM_SERV%' and");
        appendToSb(sb, "  is to the service '%TO_SERV%' and");
        appendToSb(sb, "  is in reply to the card '%PREV_CARD%'.");

        ceParms.put("%CARD_TYPE%", Card.TELL.toString());
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

    // Reply to service that sent the Tell card if its card has been accepted or not
    public void generateTellReplyCard(CeInstance cardInst, String tellText, String fromService,
            String toService) {
        System.out.println("\nGenerate reply tell card:");
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "there is a %CARD_TYPE% named '%CARD_NAME%' that");
        appendToSb(sb, "  has the timestamp '{now}' as timestamp and");
        appendToSb(sb, "  has '%CONV_TEXT%' as content and");
        appendToSb(sb, "  is from the service '%FROM_SERV%' and");
        appendToSb(sb, "  is to the service '%TO_SERV%' and");
        appendToSb(sb, "  is in reply to the card '%PREV_CARD%'.");

        ceParms.put("%CARD_TYPE%", Card.NL.toString());
        ceParms.put("%CARD_NAME%", ce.generateNewUid());
        ceParms.put("%CONV_TEXT%", Reply.SAVED.toString());
        ceParms.put("%FROM_SERV%", fromService);
        ceParms.put("%TO_SERV%", toService);
        ceParms.put("%PREV_CARD%", cardInst.getInstanceName());

        String ceSentence = substituteCeParameters(sb.toString(), ceParms);
        String source = ce.generateSrcName(fromService);
        System.out.println(ceSentence);
        ce.save(ceSentence, source);
    }

    public void generateNLCard(CeInstance cardInst, String convText, String fromService, String toService) {
        System.out.println("\nGenerate reply tell card:");
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "there is a %CARD_TYPE% named '%CARD_NAME%' that");
        appendToSb(sb, "  has the timestamp '{now}' as timestamp and");
        appendToSb(sb, "  has '%CONV_TEXT%' as content and");
        appendToSb(sb, "  is from the service '%FROM_SERV%' and");
        appendToSb(sb, "  is to the service '%TO_SERV%' and");
        appendToSb(sb, "  is in reply to the card '%PREV_CARD%'.");

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
}
