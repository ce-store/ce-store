package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class CardGenerator {

    private ConvCeGenerator ce;

    public CardGenerator(ActionContext ac) {
        ce = new ConvCeGenerator(ac);
    }

    // Tell cards are used to add valid CE to the store
    public void generateTellCard(CeInstance cardInst, String convText, String fromService, String toService) {
        generateCard(Card.TELL.toString(), convText, fromService, toService, cardInst.getInstanceName(), null);
    }

    // Reply to service that sent the Tell card if its card has been accepted or not
    public void generateTellReplyCard(CeInstance cardInst, String tellText, String fromService,
            String toService) {
        generateCard(Card.GIST.toString(), Reply.SAVED.message(), fromService, toService, cardInst.getInstanceName(), null);
    }

    // User has declared their interest in something - create new interesting thing
    public void generateInterestingThingCard(CeInstance cardInst, String content, String fromService,
            String toService) {
        generateCard(Card.TELL.toString(), content, fromService, toService, cardInst.getInstanceName(), null);
    }

    // Add referenced instances to the 'about' property on a card
    private String addAbout(ArrayList<String> referencedInsts) {
        StringBuilder sb = new StringBuilder();

    	if (referencedInsts != null) {
	        int numReferences = referencedInsts.size();

	        for (int i = 0; i < numReferences; ++i) {
	            String inst = referencedInsts.get(i);
	            appendToSbNoNl(sb, "  is about the thing '");
	            appendToSbNoNl(sb, inst);
                appendToSb(sb, "' and");
	        }
    	}

    	return sb.toString();
    }

    // Generate generic card
    public void generateCard(String cardType, String content, String fromService, String toService, String prevCard, ArrayList<String> referencedInsts) {
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "there is a %CARD_TYPE% named '%CARD_NAME%' that");
        appendToSb(sb, "  has the timestamp '{now}' as timestamp and");
        appendToSb(sb, "  has '%CONV_TEXT%' as content and");
        appendToSb(sb, "  is from the service '%FROM_SERV%' and");

        appendToSbNoNl(sb, addAbout(referencedInsts));

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
        ce.save(ceSentence, source);
    }
}
