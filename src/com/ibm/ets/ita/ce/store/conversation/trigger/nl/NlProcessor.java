package com.ibm.ets.ita.ce.store.conversation.trigger.nl;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ConvSentence;
import com.ibm.ets.ita.ce.store.conversation.model.ConvText;
import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.FinalItem;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.conversation.processor.InterestingThingsProcessor;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralProcessor;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Property;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Reply;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Word;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class NlProcessor extends GeneralProcessor {

    private ConvText convText;
    private NlTriggerHandler th;

    public NlProcessor(ActionContext ac, NlTriggerHandler th) {
        this.ac = ac;
        this.th = th;
        cg = new CardGenerator(ac);
    }

    // Process NL card
    public void process(CeInstance cardInst) {
        String nlText = cardInst.getSingleValueFromPropertyNamed(Property.CONTENT.toString());
        String modNlText = appendDotIfNeeded(nlText);
        System.out.println("Conv text: " + modNlText);

        // Test for valid CE
        if (isValidCe(modNlText)) {
            // Valid CE
            System.out.println("Valid CE");
            cg.generateTellCard(cardInst, modNlText, th.getTriggerName(), th.getTellServiceName());
        } else {
            // NL
            System.out.println("Not valid CE");
            if (fromTellService(cardInst)) {
                // Process Tell response
                processTellResponse(cardInst, nlText);
            } else {
                // Process NL response
                processNLResponse(cardInst, modNlText);
            }
        }
    }

    private void processNLResponse(CeInstance cardInst, String nlText) {
        // TODO: Convert NL questions into CE queries and pass to Ask agent
        convText = ConvText.createNewText(ac, nlText);
        System.out.println("Conv text: " + convText);

        NlSentenceProcessor sp = new NlSentenceProcessor(ac, cardInst);
        NlQuestionProcessor qp = new NlQuestionProcessor();
        NlAnswerGenerator ag = new NlAnswerGenerator(ac);

        StringBuilder sb = new StringBuilder();

        ArrayList<FinalItem> allFinalItems = new ArrayList<FinalItem>();
        ArrayList<FinalItem> allOptionItems = new ArrayList<FinalItem>();

        for (ConvSentence sentence : convText.getChildSentences()) {
            ArrayList<ProcessedWord> words = sp.process(sentence);

            // TODO: Shouldn't this be if sentence is question?
            if (convText.isQuestion()) {
                sp.extractMatchingEntities(sentence, words);

                ArrayList<FinalItem> finalItems = qp.getFinalItems(words);
                ArrayList<FinalItem> optionItems = qp.getOptionItems(words);

                if (finalItems != null) {
                    allFinalItems.addAll(finalItems);

                    // TODO: Compute who/what/where answers differently
//                    if (qp.isWhoQuestion(words)) {
                        sb.append(ag.answerStandardQuestion(finalItems, Word.WHO));
//                    } else {
//
//                    }
                }

                if (optionItems != null) {
                    allOptionItems.addAll(optionItems);
                    sb.append(ag.answerOptionQuestion(optionItems));
                }

            } else {
                // TODO: Something else - what would these be?
            }
        }

        // If string builder is empty, then nothing has been understood
        if (sb.toString().isEmpty()) {
            sb.append(ag.nothingtUnderstood());
        }

        // Extract referenced items to send to InterestingThingsProcessor and to set 'about' property in card
        ArrayList<String> referencedItems = new ArrayList<String>();
        ArrayList<CeInstance> referencedInsts = new ArrayList<CeInstance>();

        for (FinalItem item : allFinalItems) {
            ArrayList<ExtractedItem> extractedItems = item.getExtractedItems();

            for (ExtractedItem extractedItem : extractedItems) {
                referencedItems.add(extractedItem.getInstance().getInstanceName());
            }
        }

        InterestingThingsProcessor interestingThings = new InterestingThingsProcessor(ac);
        String interestingAnswer = interestingThings.generate(referencedInsts);
        System.out.println("Interesting things: " + interestingAnswer);

        // Generate NL Card with reply
        String humanAgent = findHumanAgent(cardInst);
        cg.generateNLCard(cardInst, sb.toString(), th.getTriggerName(), humanAgent, referencedItems);
    }

    // Pass on Tell agent's message to human agent
    private void processTellResponse(CeInstance cardInst, String convText) {
        if (convText.equals(Reply.SAVED.toString())) {
            System.out.println("Is saved response");
            String humanAgent = findHumanAgent(cardInst);
            cg.generateNLCard(cardInst, convText, th.getTriggerName(), humanAgent, null);
        }
    }

    // Find the last spoke to human agent from earlier in the conversation
    private String findHumanAgent(CeInstance cardInst) {
        System.out.println("\nLook for human agent...");
        CeInstance prevCard = cardInst;

        while (fromTellService(prevCard) || fromNLService(prevCard)) {
            if (prevCard == null) {
                return null;
            }

            prevCard = prevCard.getSingleInstanceFromPropertyNamed(ac, Property.IN_REPLY_TO.toString());
        }

        String humanAgent = prevCard.getSingleValueFromPropertyNamed(Property.IS_FROM.toString());
        System.out.println("Human agent: " + humanAgent);
        return humanAgent;
    }

    private boolean fromNLService(CeInstance cardInst) {
        String fromService = cardInst.getSingleValueFromPropertyNamed(Property.IS_FROM.toString());
        return fromService.equals(th.getTriggerName());
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
