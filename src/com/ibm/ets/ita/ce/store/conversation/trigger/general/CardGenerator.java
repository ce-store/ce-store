package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;

public class CardGenerator {

    private CeGenerator ce;
    private ActionContext ac;

    public CardGenerator(ActionContext ac) {
        ce = new CeGenerator(ac);
        this.ac = ac;
    }

    // Tell cards are used to add valid CE to the store
    public void generateTellCard(CeInstance cardInst, String convText, String fromService, String toService) {
        generateCard(Card.TELL.toString(), convText, fromService, toService, cardInst.getInstanceName(), null);
    }

    // Reply to service that sent the Tell card if its card has been accepted or not
    public void generateTellReplyCard(CeInstance cardInst, String tellText, String fromService,
            String toService) {
        generateCard(Card.NL.toString(), Reply.SAVED.message(), fromService, toService, cardInst.getInstanceName(), null);
    }

    // Generate NL card
    public void generateNLCard(CeInstance cardInst, String convText, String fromService, String toService, ArrayList<String> referencedInsts) {
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
        ce.save(ceSentence, source);
    }

    // User has declared their interest in something - create new interesting thing
    public void generateInterestingThingCard(CeInstance cardInst, String content, String fromService,
            String toService) {
        generateCard(Card.TELL.toString(), content, fromService, toService, cardInst.getInstanceName(), null);
    }

    public void generatePotentialInterestingThingCard(CeInstance inst, String fromService, String toService) {
        StringBuilder sb = new StringBuilder();

        appendToSb(sb, Reply.NEW_INTERESTING.toString());
        appendToSbNoNl(sb, Reply.STATE_INTEREST.toString());
        appendToSbNoNl(sb, inst.getInstanceName());
        appendToSb(sb, ".");

        String content = sb.toString();
        System.out.println("Content: " + content);
        generateCard(Card.NL.toString(), content, fromService, toService, null, null);
    }

    // Found an interesting thing - send to interested user
    public void generateInterestingFactCard(CeInstance inst, String fromService, String toService) {
        System.out.println("\nGenerate interesting thing found card:\n");

        CeSentence[] sentences = inst.getPrimarySentences();
        CeSentence lastSentence = sentences[sentences.length - 1];
        String sentenceText = lastSentence.getCeText(ac);

        if (!sentenceText.contains(Property.INTERESTED_PARTY.toString())) {
            StringBuilder sb = new StringBuilder();

            appendToSbNoNl(sb, Reply.NEW_INFORMATION.toString());
            appendToSbNoNl(sb, inst.getInstanceName());
            appendToSb(sb, ":");

            appendToSb(sb, sentenceText);

            String content = sb.toString();
            System.out.println("Content: " + content);
            generateCard(Card.NL.toString(), content, fromService, toService, null, null);
        }
    }

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
        System.out.println(ceSentence);
        ce.save(ceSentence, source);
    }
}
