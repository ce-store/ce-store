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
        System.out.println("\nProcess sentence");
        ArrayList<ProcessedWord> words = prepareWords(sentence);
        classify(words);

//        if (sentence.getParentPhrase().isQuestion()) {
//            // Question sentence
//            extractMatchingEntities(sentence, words);
//            return words;
//        } else {
//            // TODO: Something else
//
//        }

        return words;
    }

    // Create the initial ProcessedWord list
    private ArrayList<ProcessedWord> prepareWords(ConvSentence sentence) {
        System.out.println("Prepare words");
        ArrayList<ProcessedWord> processedWords = new ArrayList<ProcessedWord>();
        ArrayList<ConvWord> words = sentence.getAllWords();
        System.out.println("Words: " + words);

        for (ConvWord word : words) {
            ProcessedWord processedWord = new ProcessedWord(word);
            processedWords.add(processedWord);
            System.out.println("Processed word: " + processedWord);

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

    // Find entities matching words in sentence and create ExtractedItems for them
    public void extractMatchingEntities(ConvSentence sentence, ArrayList<ProcessedWord> words) {
        System.out.println("\nMatch entities...");
        ProcessedWord subject = seekSubject(sentence, words);
        seekOthers(sentence, words, subject);
    }

    private ProcessedWord seekSubject(ConvSentence sentence, ArrayList<ProcessedWord> words) {
        ProcessedWord subjectWord = seekSubjectWord(words);

        if (subjectWord != null) {
            seekSubjectEntity(subjectWord);
        } else {
            reportDebug("No subject word found for sentence: " + sentence.getSentenceText(), ac);
        }

        return subjectWord;
    }

    // Find subject word
    private ProcessedWord seekSubjectWord(ArrayList<ProcessedWord> words) {
        ProcessedWord foundWord = null;

        if (foundWord == null) {
            // Now try everything
            for (ProcessedWord word : words) {
                if (foundWord == null) {
                    if (word.isGrounded()) {
                        if (word.isValidSubjectWord(ac)) {
                            foundWord = word;
                        }
                    }
                }
            }
        }

        return foundWord;
    }

    // Find entity relating to subject word
    private void seekSubjectEntity(ProcessedWord subjectWord) {
        ExtractedItem lastExtracted = null;

        if (subjectWord.isGroundedOnConcept()) {
            // Concept
            ArrayList<CeConcept> subCons = subjectWord.listGroundedConcepts();
            CeConcept subjectConcept = retrieveSingleConceptFrom(subCons, subjectWord, "subject");
            lastExtracted = new ExtractedItem(subjectWord, subjectConcept);
        } else if (subjectWord.isGroundedOnProperty()) {
            // Property
            ArrayList<CeProperty> subProps = subjectWord.listGroundedProperties();
            lastExtracted = new ExtractedItem(subjectWord, subProps);
        } else if (subjectWord.isGroundedOnInstance()) {
            // Instance
            ArrayList<CeInstance> subInsts = subjectWord.listGroundedInstances();
            boolean confirmRequired = subjectWord.confirmRequired();

            if (confirmRequired) {
                for (CeInstance subInst : subInsts) {
                    ExtractedItem extInst = new ExtractedItem(subjectWord, subInst);

                    if (lastExtracted != null) {
                        extInst.setPreviousItem(lastExtracted);
                    }

                    lastExtracted = extInst;
                }
            } else {
                CeInstance subjectInstance = retrieveSingleInstanceFrom(subInsts, subjectWord, "subject");

                if (subjectInstance != null) {
                    subjectWord.setChosenInstance(subjectInstance);
                }

                lastExtracted = new ExtractedItem(subjectWord, subjectInstance);
            }
        } else {
            //Unknown
            reportWarning("Unable to detect grounding type for subject word: " + subjectWord.toString(), ac);
        }

        if (lastExtracted != null) {
            subjectWord.addConnectedWordsTo(lastExtracted);
        }
    }

    // Find entities matching other words
    private void seekOthers(ConvSentence sentence, ArrayList<ProcessedWord> words, ProcessedWord subject) {
        ExtractedItem lastExtracted = null;

        for (ProcessedWord word : words) {
            if (word != subject) {
                if (word.isGroundedOnConcept()) {
                    if (!word.isLaterPartOfPartial()) {
                        CeConcept tgtCon = retrieveSingleConceptFrom(word.listGroundedConcepts(), word, "object");

                        if (tgtCon != null) {
                            reportDebug("I got an object concept (" + tgtCon.getConceptName()  + "), from word: " + word.toString(), ac);
                            ExtractedItem extCon = new ExtractedItem(word, tgtCon);

                            if (lastExtracted != null) {
                                extCon.setPreviousItem(lastExtracted);
                            }

                            word.addConnectedWordsTo(extCon);
//                            this.otherConcepts.add(extCon);
                            lastExtracted = extCon;
                        } else {
                            reportDebug("No object concept found for word: " + word.toString(), ac);
                        }
                    } else {
                        reportDebug("Ignoring later partial for concept: " + word.getWordText(), ac);
                    }
                } else if (word.isGroundedOnProperty()) {
                    if (!word.isLaterPartOfPartial()) {
//
                        ArrayList<CeProperty> propList = word.listGroundedProperties();
                        if (!propList.isEmpty()) {
                            ExtractedItem extProp = new ExtractedItem(word, propList);
                            if (lastExtracted != null) {
                                extProp.setPreviousItem(lastExtracted);
                            }
                            word.addConnectedWordsTo(extProp);
//                            this.otherProperties.add(extProp);

                            lastExtracted = extProp;
                        } else {
                            reportWarning("No object property found for word: " + word.toString(), ac);
                        }
                    } else {
                        reportDebug("Ignoring later partial for property: " + word.getWordText(), ac);
                    }
                } else if (word.isGroundedOnInstance()) {
                    CeInstance tgtInst = retrieveSingleInstanceFrom(word.listGroundedInstances(), word, "object");

                    if (tgtInst != null) {
                        word.setChosenInstance(tgtInst);
                        reportDebug("I got an object instance (" + tgtInst.getInstanceName()  + "), from word: " + word.toString(), ac);
                        ExtractedItem extInst = new ExtractedItem(word, tgtInst);
                        if (lastExtracted != null) {
                            extInst.setPreviousItem(lastExtracted);
                        }
                        word.addConnectedWordsTo(extInst);
//                        this.otherInstances.add(extInst);

                        lastExtracted = extInst;
                    } else {
                        reportWarning("No object instance found for word: " + word.toString(), ac);
                    }
                } else {
//                    this.ungroundedWords.add(word);
                }
            }
        }
    }

    // Get most likely matching concept
    private CeConcept retrieveSingleConceptFrom(ArrayList<CeConcept> concepts, ProcessedWord word, String context) {
        CeConcept result = null;

        if (concepts.isEmpty()) {
            reportError("No grounded concept found for " + context + " word: " + word.toString(), this.ac);
        } else if (concepts.size() > 1) {
            reportError("Too many (" + concepts.size() + ") grounded concepts found for " + context + " word: "  + word.toString(), this.ac);
        } else {
            result = concepts.get(0);
        }

        return result;
    }

    // Get most likely matching instance
    private CeInstance retrieveSingleInstanceFrom(ArrayList<CeInstance> instances, ProcessedWord word, String context) {
        CeInstance result = null;

        if (instances.isEmpty()) {
            reportError("No grounded instance found for " + context + " word: " + word.toString(), this.ac);
        } else if (instances.size() > 1) {
            reportDebug("Too many (" + instances.size() + ") grounded instances found for " + context + " word: "  + word.toString(), this.ac);
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
