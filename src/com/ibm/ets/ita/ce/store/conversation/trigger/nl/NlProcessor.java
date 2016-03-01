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
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

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
                processTellResponse(cardInst, nlText);
            } else {
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

            // TODO: Shouldn't this be if sentence is question? - Text currently isn't being split into sentences
            if (convText.isQuestion()) {
                sp.extractMatchingEntities(sentence, words);
                System.out.println("\nDone matching");

                ArrayList<FinalItem> finalItems = qp.getFinalItems(words);
                ArrayList<FinalItem> optionItems = qp.getOptionItems(words);

                if (finalItems != null) {
                    allFinalItems.addAll(finalItems);

                    // TODO: Compute who/what/where answers differently
                    if (qp.isWhoQuestion(words)) {
                        sb.append(ag.answerStandardQuestion(finalItems));
                    } else {
                        sb.append(ag.answerStandardQuestion(finalItems));
                    }
                }

                if (optionItems != null) {
                    if (!sb.toString().isEmpty()) {
                        sb.append("\n");
                    }

                    allOptionItems.addAll(optionItems);
                    sb.append(ag.answerOptionQuestion(optionItems));
                }

            } else {
                // TODO: Something else - what would these be?
            }
        }

        // If string builder is empty, then nothing has been understood
        if (sb.toString().isEmpty()) {
            sb.append(ag.nothingUnderstood());
        }

        ArrayList<String> referencedItems = new ArrayList<String>();
        ArrayList<CeInstance> referencedInsts = new ArrayList<CeInstance>();

        extractReferencedItems(allFinalItems, referencedItems, referencedInsts);

        // Look for interesting things
        InterestingThingsProcessor interestingThings = new InterestingThingsProcessor(ac);
        String interestingAnswer = interestingThings.generate(referencedInsts);
        System.out.println("Interesting things: " + interestingAnswer);

        // Generate NL Card with reply
        String humanAgent = findHumanAgent(cardInst);
        cg.generateNLCard(cardInst, sb.toString(), th.getTriggerName(), humanAgent, referencedItems);
    }

    // Extract referenced items to set 'about' property in card
    private void extractReferencedItems(ArrayList<FinalItem> allFinalItems, ArrayList<String> referencedItems,
            ArrayList<CeInstance> referencedInsts) {
        for (FinalItem item : allFinalItems) {
            ArrayList<ExtractedItem> extractedItems = item.getExtractedItems();

            if (item.isPropertyInstanceItem()) {
                System.out.println("Is property instance item");

                CeInstance instance = null;
                CeProperty property = null;
                ArrayList<CeProperty> properties = null;

                // Extract instance and property
                for (ExtractedItem extractedItem : extractedItems) {
                    if (extractedItem.isInstanceItem()) {
                        instance = extractedItem.getInstance();
                        referencedItems.add(instance.getInstanceName());
                    } else if (extractedItem.isPropertyItem()) {
                        properties = extractedItem.getPropertyList();
                    }
                }

                CeConcept[] instanceConcepts = instance.getDirectConcepts();

                // Find property that matches instance
                for (CeProperty prop : properties) {
                    CeConcept propertyConcept = prop.getDomainConcept();

                    for (CeConcept instanceConcept : instanceConcepts) {
                        if (instanceConcept.equals(propertyConcept)) {
                            property = prop;
                            break;
                        }
                    }
                }

                // Add value from property instance
                CePropertyInstance propertyInstance = instance.getPropertyInstanceForProperty(property);

                if (propertyInstance != null) {
                    String value = propertyInstance.getFirstPropertyValue().getValue();
                    System.out.println("Value: " + value);
                    referencedItems.add(value);
                }
            } else {
                System.out.println("Other item");
                for (ExtractedItem extractedItem : extractedItems) {
                    if (extractedItem.isInstanceItem()) {
                        System.out.println("Adding instance to reference");
                        System.out.println(extractedItem.getInstance().getInstanceName());
                        referencedItems.add(extractedItem.getInstance().getInstanceName());
                    } else {
                        System.out.println("Other type of extracted item");
                        // TODO: do something with concepts and properties
                    }
                }
            }
        }
    }

    // Pass on Tell agent's message to human agent
    private void processTellResponse(CeInstance cardInst, String convText) {
        if (convText.equals(Reply.SAVED.message())) {
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
