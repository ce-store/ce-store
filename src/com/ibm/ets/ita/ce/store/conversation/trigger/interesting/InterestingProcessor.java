package com.ibm.ets.ita.ce.store.conversation.trigger.interesting;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Card;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Concept;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralProcessor;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Property;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Reply;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;

public class InterestingProcessor extends GeneralProcessor {

    private InterestingTriggerHandler th = null;

    public InterestingProcessor(ActionContext ac, InterestingTriggerHandler th) {
        this.ac = ac;
        this.th = th;
        cg = new CardGenerator(ac);
    }

    // Process interesting thing
    public void process(CeInstance inst) {
        ArrayList<CeInstance> interestedParties = inst.getInstanceListFromPropertyNamed(ac, Property.INTERESTED_PARTY.toString());
        ArrayList<CeInstance> uninterestedParties = inst.getInstanceListFromPropertyNamed(ac, Property.UNINTERESTED_PARTY.toString());

        for (CeInstance interestedParty : interestedParties) {
            boolean sendFact = true;

            String interestedUser = interestedParty.getSingleValueFromPropertyNamed("user");
            String interestedTimestamp = interestedParty.getSingleValueFromPropertyNamed("timestamp");

            for (CeInstance uninterestedParty : uninterestedParties) {
                String uninterestedUser = uninterestedParty.getSingleValueFromPropertyNamed("user");

                // Check if user has registered uninterest more recently than interest
                if (interestedUser.equals(uninterestedUser)) {
                    String uninterestedTimestamp = uninterestedParty.getSingleValueFromPropertyNamed("timestamp");

                    if (Long.parseLong(interestedTimestamp, 10) < Long.parseLong(uninterestedTimestamp, 10)) {
                        sendFact = false;
                    }
                }
            }

            if (sendFact) {
                // Get latest fact sentence
                CeSentence[] sentences = inst.getPrimarySentences();
                CeSentence[] secondarySentences = inst.getSecondarySentences();
                String sentenceText = null;

                if (sentences.length > 0) {
                    CeSentence lastPrimarySentence = sentences[sentences.length - 1];

                    if (secondarySentences.length > 0) {
                        CeSentence lastSecondarySentence = secondarySentences[secondarySentences.length - 1];

                        if (lastPrimarySentence.getCreationDate() > lastSecondarySentence.getCreationDate()) {
                            sentenceText = lastPrimarySentence.calculateCeTextWithoutRationale();
                        } else {
                            // Check secondary sentences for uninteresting rules to prevent duplicate notifications
                            ArrayList<CeInstance> uninterestingRules = ac.getModelBuilder().getAllInstancesForConceptNamed(ac, Concept.UNINTERESTING_RULE.toString());
                            String ruleName = lastSecondarySentence.getRationaleRuleName();

                            boolean isUninterestingRule = false;

                            for (CeInstance uninterestingRule : uninterestingRules) {
                                isUninterestingRule = isUninterestingRule || uninterestingRule.getInstanceName().equals(ruleName);
                            }

                            if (!isUninterestingRule) {
                                sentenceText = lastSecondarySentence.calculateCeTextWithoutRationale();
                            }
                        }
                    } else {
                        sentenceText = lastPrimarySentence.calculateCeTextWithoutRationale();
                    }
                }

                if (sentenceText != null && !sentenceText.contains(Property.INTERESTED_PARTY.toString()) && !sentenceText.contains(Card.GENERAL.toString())) {
                    StringBuilder sb = new StringBuilder();

                    appendToSbNoNl(sb, Reply.NEW_INFORMATION.toString());
                    appendToSbNoNl(sb, inst.getInstanceName());
                    appendToSb(sb, ":");

                    appendToSb(sb, sentenceText);

                    String content = sb.toString();
                    cg.generateCard(Card.GIST.toString(), content, th.getTriggerName(), interestedUser, null, null);
                }
            }
        }
    }
}
