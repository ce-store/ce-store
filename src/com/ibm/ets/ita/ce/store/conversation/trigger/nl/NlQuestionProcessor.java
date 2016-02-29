package com.ibm.ets.ita.ce.store.conversation.trigger.nl;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.FinalItem;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;

public class NlQuestionProcessor {

//    private ActionContext ac = null;
//    private CeInstance cardInstance = null;
//    private NlAnswerGenerator ag = null;

    public NlQuestionProcessor() {
//        this.ac = ac;
//        this.cardInstance = cardInst;
//        ag = new NlAnswerGenerator(ac);
    }

    public ArrayList<FinalItem> getFinalItems(ArrayList<ProcessedWord> words) {
        System.out.println("\nGet final items...");
        ArrayList<ExtractedItem> finalExtractedItems = computeFinalExtractedItems(words);
        ArrayList<FinalItem> finalItems = null;

        if (finalExtractedItems != null) {
            System.out.println("Final results available: " + finalExtractedItems);
            finalItems = initialiseFinalItems(finalExtractedItems);
        }

        return finalItems;
    }

    public ArrayList<FinalItem> getOptionItems(ArrayList<ProcessedWord> words) {
        System.out.println("\nGet option items...");
        ArrayList<ExtractedItem> optionExtractedItems = computeOptionExtractedItems(words);
        ArrayList<FinalItem> optionalFinalItems = null;

        if (optionExtractedItems != null) {
            System.out.println("Options available: " + optionExtractedItems);
            optionalFinalItems = initialiseOptionItems(optionExtractedItems);
        }

        return optionalFinalItems;
    }

//    public ArrayList<FinalItem> process(ArrayList<ProcessedWord> words) {
//        ArrayList<ExtractedItem> finalExtractedItems = computeFinalExtractedItems(words);
//        ArrayList<ExtractedItem> optionExtractedItems = computeOptionExtractedItems(words);
//
//        if (finalExtractedItems != null) {
//            System.out.println("Final results available: " + finalExtractedItems);
//            ArrayList<FinalItem> finalItems = initialiseFinalItems(finalExtractedItems);
//
//            if (isWhoQuestion(words)) {
//                ag.answerStandardQuestion(finalItems, Word.WHO);
//            } else {
//                ag.answerStandardQuestion(finalItems, null);
//            }
//
//            return finalItems;
//        } else if (optionExtractedItems != null) {
//            System.out.println("Options available: " + optionExtractedItems);
//            ArrayList<FinalItem> optionalFinalItems = initialiseOptionItems(optionExtractedItems);
//
//            return optionalFinalItems;
//        } else {
//            return null;
//        }
//    }

    private ArrayList<ExtractedItem> computeFinalExtractedItems(ArrayList<ProcessedWord> words) {
        ArrayList<ExtractedItem> items = new ArrayList<ExtractedItem>();

        for (ProcessedWord word : words) {
            ArrayList<ExtractedItem> wordItems = word.getExtractedItems();

            if (wordItems != null) {
                for (ExtractedItem item : wordItems) {
                    if (!items.contains(item)) {
                        if (item.isDominantInterpretation()) {
                            items.add(item);
                        }
                    }
                }
            }
        }

        return items;
    }

    private ArrayList<ExtractedItem> computeOptionExtractedItems(ArrayList<ProcessedWord> words) {
        ArrayList<ExtractedItem> items = new ArrayList<ExtractedItem>();

        for (ProcessedWord word : words) {
            ArrayList<ExtractedItem> wordItems = word.getExtractedItems();
            System.out.println("Extracted items: " + wordItems);

            if (wordItems != null) {
                for (ExtractedItem item : wordItems) {
                    if (!items.contains(item)) {
                        if (word.confirmRequired()) {
                            items.add(item);
                        }
                    }
                }
            }
        }

        return items;
    }

    private ArrayList<FinalItem> initialiseFinalItems(ArrayList<ExtractedItem> extractedItems) {
        FinalItem finalItem = null;
        ArrayList<FinalItem> finalItems = new ArrayList<FinalItem>();

        for (ExtractedItem item : extractedItems) {
            if (item.isInstanceItem()) {
                if (finalItem != null) {
                    if (finalItem.isInstanceItem()) {
                        finalItem.addExtractedItem(item);
                    } else {
                        finalItem = null;
                    }
                }
            } else {
                finalItem = null;
            }

            if (finalItem == null) {
                finalItem = new FinalItem(item);
                finalItems.add(finalItem);
            }
        }

        return finalItems;
    }

    private ArrayList<FinalItem> initialiseOptionItems(ArrayList<ExtractedItem> extractedItems) {
        FinalItem finalItem = null;
        ArrayList<FinalItem> optionItems = new ArrayList<FinalItem>();

        for (ExtractedItem option : extractedItems) {
            if (option.isInstanceItem()) {
                if (finalItem != null) {
                    if (finalItem.isInstanceItem()) {
                        finalItem.addExtractedItem(option);
                    } else {
                        finalItem = null;
                    }
                }
            } else {
                finalItem = null;
            }

            if (finalItem == null) {
                finalItem = new FinalItem(option);
                optionItems.add(finalItem);
            }
        }

        return optionItems;
    }

    public boolean isWhoQuestion(ArrayList<ProcessedWord> words) {
        ProcessedWord word = getFirstQuestionWord(words);

        return (word != null) && word.isWho();
    }

    private ProcessedWord getFirstQuestionWord(ArrayList<ProcessedWord> words) {
        ProcessedWord result = null;

        for (ProcessedWord pw : words) {
            if (pw.isQuestionWord()) {
                result = pw;
                break;
            }
        }

        return result;
    }
}
