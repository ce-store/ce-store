package com.ibm.ets.ita.ce.store.conversation.trigger.nl;

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ConvSentence;
import com.ibm.ets.ita.ce.store.conversation.model.ConvWord;
import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Concept;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Word;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class NlSentenceProcessor {

    private ActionContext ac = null;
    private CeInstance cardInstance = null;

    public NlSentenceProcessor(ActionContext ac, CeInstance cardInst) {
        this.ac = ac;
        this.cardInstance = cardInst;
    }

    // Process words in sentence to decipher meaning
    public ArrayList<ProcessedWord> process(ConvSentence sentence) {

        ArrayList<ProcessedWord> words = prepareWords(sentence);
        classify(words);

        return words;
    }

    // Create the initial ProcessedWord list
    private ArrayList<ProcessedWord> prepareWords(ConvSentence sentence) {
        ArrayList<ProcessedWord> processedWords = new ArrayList<ProcessedWord>();
        ArrayList<ConvWord> words = sentence.getAllWords();

        for (ConvWord word : words) {
            ProcessedWord processedWord = new ProcessedWord(word);
            processedWords.add(processedWord);
            markQuestionWord(processedWord);
        }

        return processedWords;
    }

    // If question word, mark ProcessedWord
    private void markQuestionWord(ProcessedWord word) {
        if ((word.getLcWordText().equals(Word.WHO.toString())) ||
            (word.getLcWordText().equals(Word.WHERE.toString())) ||
            (word.getLcWordText().equals(Word.WHEN.toString())) ||
            (word.getLcWordText().equals(Word.WHAT.toString())) ||
            (word.getLcWordText().equals(Word.WHICH.toString())) ||
            (word.getLcWordText().equals(Word.WHY.toString())) ||
            (word.getLcWordText().equals(Word.LIST.toString())) ||
            (word.getLcWordText().equals(Word.SUMMARISE.toString())) ||
            (word.getLcWordText().equals(Word.SUMMARIZE.toString())) ||
            (word.getLcWordText().equals(Word.COUNT.toString()))) {
            word.markAsQuestionWord();
        }
    }

    // Classify all ProcessedWords
    private void classify(ArrayList<ProcessedWord> words) {
        ArrayList<CeInstance> commonWords = ac.getModelBuilder().getAllInstancesForConceptNamed(ac, Concept.COMMON_WORDS.toString());
        ArrayList<CeInstance> negationWords = ac.getModelBuilder().getAllInstancesForConceptNamed(ac, Concept.NEGATION_WORDS.toString());

        for (ProcessedWord word : words) {
            word.classify(ac, commonWords, negationWords, cardInstance);
        }
    }

    // Find entities matching words in sentence and create an ExtractedItem for each
    public void extractMatchingEntities(ConvSentence sentence, ArrayList<ProcessedWord> words) {
        ExtractedItem lastExtracted = null;

        for (ProcessedWord word : words) {
            if (word.isGroundedOnConcept()) {
                if (!word.isLaterPartOfPartial()) {
                    extractConcept(word, lastExtracted);
                } else {
                    reportDebug("Ignoring later partial for concept: " + word.getWordText(), ac);
                }
            } else if (word.isGroundedOnProperty()) {
                if (!word.isLaterPartOfPartial()) {
                    extractProperty(word, lastExtracted);
                } else {
                    reportDebug("Ignoring later partial for property: " + word.getWordText(), ac);
                }
            } else if (word.isGroundedOnInstance()) {
                extractInstance(word, lastExtracted);
            } else {
                reportDebug("Word is ungrounded: " + word.toString(), ac);
            }
        }
    }

    // Create ExtractedItem for matching concept
    private void extractConcept(ProcessedWord word, ExtractedItem lastExtracted) {
        CeConcept concept = retrieveSingleConceptFrom(word.listGroundedConcepts(), word);

        if (concept != null) {
            reportDebug("I got an object concept (" + concept.getConceptName()  + "), from word: " + word.toString(), ac);
            ExtractedItem extractedConcept = new ExtractedItem(word, concept);

            if (lastExtracted != null) {
                extractedConcept.setPreviousItem(lastExtracted);
            }

            word.addConnectedWordsTo(extractedConcept);
            lastExtracted = extractedConcept;
        } else {
            reportDebug("No object concept found for word: " + word.toString(), ac);
        }
    }

    // Create ExtractedItem for matching property
    private void extractProperty(ProcessedWord word, ExtractedItem lastExtracted) {
        ArrayList<CeProperty> propertyList = word.listGroundedProperties();

        if (!propertyList.isEmpty()) {
            ExtractedItem extProp = new ExtractedItem(word, propertyList);

            if (lastExtracted != null) {
                extProp.setPreviousItem(lastExtracted);
            }

            word.addConnectedWordsTo(extProp);
            lastExtracted = extProp;
        } else {
            reportWarning("No object property found for word: " + word.toString(), ac);
        }
    }

    // Create ExtractedItem for each matching instance
    private void extractInstance(ProcessedWord word, ExtractedItem lastExtracted) {
        ArrayList<CeInstance> instances = word.listGroundedInstances();

        if (word.confirmRequired() || word.correctionRequired()) {
            for (CeInstance instance : instances) {
                ExtractedItem extractedInstance = new ExtractedItem(word, instance);

                if (lastExtracted != null) {
                    extractedInstance.setPreviousItem(lastExtracted);
                }

                lastExtracted = extractedInstance;
            }
        } else {
            CeInstance instance = retrieveSingleInstanceFrom(instances, word);

            if (instance != null) {
                word.setChosenInstance(instance);
                reportDebug("I got an object instance (" + instance.getInstanceName()  + "), from word: " + word.toString(), ac);
                ExtractedItem extractedInstance = new ExtractedItem(word, instance);

                if (lastExtracted != null) {
                    extractedInstance.setPreviousItem(lastExtracted);
                }

                word.addConnectedWordsTo(extractedInstance);
                lastExtracted = extractedInstance;
            }  else {
                 reportWarning("No object instance found for word: " + word.toString(), ac);
            }
        }
    }

    // Get most likely matching concept
    private CeConcept retrieveSingleConceptFrom(ArrayList<CeConcept> concepts, ProcessedWord word) {
        CeConcept result = null;

        if (concepts.isEmpty()) {
            reportError("No grounded concept found for word: " + word.toString(), ac);
        } else if (concepts.size() > 1) {
            reportError("Too many (" + concepts.size() + ") grounded concepts found for word: "  + word.toString(), ac);
        } else {
            result = concepts.get(0);
        }

        return result;
    }

    // Get most likely matching instance
    private CeInstance retrieveSingleInstanceFrom(ArrayList<CeInstance> instances, ProcessedWord word) {
        CeInstance result = null;

        if (instances.isEmpty()) {
            reportError("No grounded instance found for word: " + word.toString(), ac);
        } else if (instances.size() > 1) {
            reportDebug("Too many (" + instances.size() + ") grounded instances found for word: "  + word.toString(), ac);
            result = chooseLongestNameFrom(instances);
        } else {
            result = instances.get(0);
        }

        return result;
    }

    // Choose longest name from instance list
    private static CeInstance chooseLongestNameFrom(ArrayList<CeInstance> instances) {
        CeInstance result = null;

        for (CeInstance inst : instances) {
            int currLen = -1;

            if (result != null) {
                currLen = result.getInstanceName().length();
            }

            if (inst.getInstanceName().length() > currLen) {
                result = inst;
            }
        }

        return result;
    }
}
