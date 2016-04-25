package com.ibm.ets.ita.ce.store.conversation.trigger.nl;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.FinalItem;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.utilities.Tuple;

public class NlQuestionProcessor {

    // Create list of final items from extracted items
    public ArrayList<FinalItem> getFinalItems(ArrayList<ProcessedWord> words) {
        ArrayList<ExtractedItem> finalExtractedItems = computeFinalExtractedItems(words);
        ArrayList<FinalItem> finalItems = null;

        if (finalExtractedItems != null) {
            finalItems = initialiseFinalItems(finalExtractedItems);
            findPropertiesRelatingOrReferringToInstances(finalItems);
        }

        return finalItems;
    }

    // Create list of optional items from extracted items
    public ArrayList<FinalItem> getOptionItems(ArrayList<ProcessedWord> words) {
        ArrayList<ExtractedItem> optionExtractedItems = computeOptionExtractedItems(words);
        ArrayList<FinalItem> optionalFinalItems = null;

        if (optionExtractedItems != null) {
            optionalFinalItems = initialiseOptionItems(optionExtractedItems);
        }

        return optionalFinalItems;
    }

    // Create list of optional items from extracted items
    public ArrayList<FinalItem> getMaybeItems(ArrayList<ProcessedWord> words) {
        ArrayList<ExtractedItem> maybeExtractedItems = computeMaybeExtractedItems(words);
        ArrayList<FinalItem> maybeFinalItems = null;

        if (maybeExtractedItems != null) {
            maybeFinalItems = initialiseOptionItems(maybeExtractedItems);
        }

        return maybeFinalItems;
    }

    // Check extracted items aren't repeated and are the dominant interpretation before adding to list
    private ArrayList<ExtractedItem> computeFinalExtractedItems(ArrayList<ProcessedWord> words) {
        ArrayList<ExtractedItem> items = new ArrayList<ExtractedItem>();

        for (ProcessedWord word : words) {
            ArrayList<ExtractedItem> wordItems = word.getExtractedItems();

            if (wordItems != null) {
                for (ExtractedItem item : wordItems) {
                    if (!items.contains(item)) {
                        // TODO: Look into dominant interpretation
                        if (item.isDominantInterpretation()) {
                            items.add(item);
                        }
                    }
                }
            }
        }

        return items;
    }

    // Check extracted items aren't repeated and are optional before adding to list
    private ArrayList<ExtractedItem> computeOptionExtractedItems(ArrayList<ProcessedWord> words) {
        ArrayList<ExtractedItem> items = new ArrayList<ExtractedItem>();

        for (ProcessedWord word : words) {
            ArrayList<ExtractedItem> wordItems = word.getExtractedItems();

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

    // Check extracted items aren't repeated and require correction before adding to list
    private ArrayList<ExtractedItem> computeMaybeExtractedItems(ArrayList<ProcessedWord> words) {
        ArrayList<ExtractedItem> items = new ArrayList<ExtractedItem>();

        for (ProcessedWord word : words) {
            ArrayList<ExtractedItem> wordItems = word.getExtractedItems();

            if (wordItems != null) {
                for (ExtractedItem item : wordItems) {
                    if (!items.contains(item)) {
                        if (word.correctionRequired()) {
                            System.out.println("Correction required - adding " + item);
                            items.add(item);
                        }
                    }
                }
            }
        }

        return items;
    }

    // Create FinalItems list of final items
    private ArrayList<FinalItem> initialiseFinalItems(ArrayList<ExtractedItem> extractedItems) {
        ArrayList<FinalItem> finalItems = new ArrayList<FinalItem>();

        for (ExtractedItem item : extractedItems) {
            FinalItem finalItem = new FinalItem(item);
            finalItems.add(finalItem);
        }

        return finalItems;
    }

    // Create FinalItems list of option items
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

    // Find final properties that exist on final instances
    private void findPropertiesRelatingOrReferringToInstances(ArrayList<FinalItem> finalItems) {
        ArrayList<FinalItem> toRemoveItems = new ArrayList<FinalItem>();

        // Initial parse to look for connected instances and properties
        for (FinalItem propertyItem : finalItems) {
            if (propertyItem.isPropertyItem()) {
                for (FinalItem instanceItem : finalItems) {
                    System.out.println("  " + instanceItem + " " + instanceItem.isInstanceItem() + " " + !toRemoveItems.contains(instanceItem) + " " + !toRemoveItems.contains(propertyItem));
                    if (instanceItem.isInstanceItem() && !toRemoveItems.contains(instanceItem) && !toRemoveItems.contains(propertyItem)) {
                        Tuple<CeInstance, CeProperty> matchingConceptProperty = instanceHasOrIsReferredToByProperty(instanceItem, propertyItem);

                        if (matchingConceptProperty != null) {
                            System.out.println("Instance has property");
                            // Instance has this property. Append results
                            toRemoveItems.add(instanceItem);

                            ExtractedItem extractedInstance = instanceItem.getFirstExtractedItem();
                            ProcessedWord word = extractedInstance.getStartWord();
                            System.out.println("Word: " + word);

                            ExtractedItem extractedItem = new ExtractedItem(word, matchingConceptProperty.x);
                            propertyItem.addExtractedItem(extractedItem);
                        }
                    }
                }
            }
        }

        // Remove processed items
        for (FinalItem item : toRemoveItems) {
            finalItems.remove(item);
        }
    }

    // If instance has property then return the result of that property
    private Tuple<CeInstance, CeProperty> instanceHasOrIsReferredToByProperty(FinalItem instanceItem, FinalItem propertyItem) {
        ExtractedItem extractedProperty = propertyItem.getFirstExtractedItem();
        ArrayList<CeProperty> properties = extractedProperty.getPropertyList();

        System.out.println("\nProperty: " + extractedProperty);
        System.out.println(properties);

        ExtractedItem extractedInstance = instanceItem.getFirstExtractedItem();
        CeInstance instance = extractedInstance.getInstance();
        CeConcept[] instanceConcepts = instance.getDirectConcepts();

        System.out.println("\nInstance: " + extractedInstance);
        System.out.println(instance);
        System.out.println(instanceConcepts);

        Tuple<CeInstance, CeProperty> matchingConceptProperty = null;

        for (CeProperty property : properties) {
            CeConcept propertyDomain = property.getDomainConcept();
            CeConcept propertyRange = property.getRangeConcept();

            if (matchingConceptProperty == null) {
                for (CeConcept instanceConcept : instanceConcepts) {
                    System.out.println("\nConcept: " + instanceConcept);
                    if (instanceConcept.equalsOrHasParent(propertyDomain)) {
                        System.out.println("Domain match!!");
                        matchingConceptProperty = new Tuple<CeInstance, CeProperty>(instance, property);
                        break;
                    }

                    if (instanceConcept.equalsOrHasParent(propertyRange)) {
                        System.out.println("Range match!!");
                        matchingConceptProperty = new Tuple<CeInstance, CeProperty>(instance, property);
                        break;
                    }
                }
            }
        }

        return matchingConceptProperty;
    }

    // Does question start with 'who'?
    public boolean isWhoQuestion(ArrayList<ProcessedWord> words) {
        ProcessedWord word = getFirstQuestionWord(words);

        return (word != null) && word.isWho();
    }

    // Get first question word
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
