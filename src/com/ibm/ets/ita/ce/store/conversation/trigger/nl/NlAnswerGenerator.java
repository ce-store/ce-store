package com.ibm.ets.ita.ce.store.conversation.trigger.nl;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.FinalItem;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Concept;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Property;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.Word;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;

public class NlAnswerGenerator {

    private ActionContext ac = null;

    public NlAnswerGenerator(ActionContext ac) {
        this.ac = ac;
    }

    // Build answer for standard question
    protected String answerStandardQuestion(ArrayList<FinalItem> finalItems, Word questionType) {
        StringBuilder sb = new StringBuilder();

        if (!finalItems.isEmpty()) {
            int numFinalItems = finalItems.size();

            for (int i = 0; i < numFinalItems; ++i) {
                FinalItem item = finalItems.get(i);

                if (item.isConceptItem()) {
                    sb.append(conceptAnswer(item));
                } else if (item.isInstanceItem()) {
                    sb.append(instanceAnswer(item, questionType));
                } else if (item.isPropertyItem()) {
                    sb.append(propertyAnswer(item));
                }

                if (i < numFinalItems - 1) {
                    sb.append("\n\n");
                }
            }
        }

        return sb.toString();
    }

    // Build answer with options
    public Object answerOptionQuestion(ArrayList<FinalItem> optionItems) {
        StringBuilder sb = new StringBuilder();

        if (!optionItems.isEmpty()) {
            for (FinalItem item : optionItems) {
                if (item.isConceptItem()) {
                    // TODO
                } else if (item.isInstanceItem()) {
                    ProcessedWord uncertainWord = null;
                    String options = "";

                    ArrayList<ExtractedItem> extractedItems = item.getExtractedItems();
                    for (int i = 0; i < extractedItems.size(); ++i) {
                        ExtractedItem ei = extractedItems.get(i);
                        if (uncertainWord == null) {
                            uncertainWord = ei.getStartWord();
                        }

                        CeInstance tgtInst = ei.getInstance();
                        options += tgtInst.getInstanceName();

                        if (i < extractedItems.size() - 3) {
                            options += ", ";
                        } else if (i == extractedItems.size() - 2) {
                            options += " or ";
                        }
                    }

                    appendToSb(sb, "'" + uncertainWord.getWordText() + "' could mean " + options + ".");
                    appendToSb(sb, "Please re-ask your question with a specific option.");
                } else if (item.isPropertyItem()) {
                    // TODO
                }
            }
        }

        return sb.toString();
    }

    // Generate a reply detailing a concept
    private String conceptAnswer(FinalItem item) {
        StringBuilder sb = new StringBuilder();
        ExtractedItem extractedItem = item.getFirstExtractedItem();
        CeConcept concept = extractedItem.getConcept();

        appendToSbNoNl(sb, concept.getConceptName());
        appendToSb(sb, " is a concept.");
        appendToSbNoNl(sb, "It has ");
        appendToSbNoNl(sb, new Integer(ac.getModelBuilder().countAllInstancesForConcept(concept)).toString());
        appendToSb(sb, " instances.");

        return sb.toString();
    }

    // Generate a reply detailing an instance
    private String instanceAnswer(FinalItem item, Word questionType) {
        StringBuilder sb = new StringBuilder();
        ArrayList<ExtractedItem> extractedItems = item.getExtractedItems();

        int numExtractedItems = extractedItems.size();
        boolean multipleAnswers = numExtractedItems > 1;

        if (multipleAnswers) {
            appendToSb(sb, "You asked " + numExtractedItems + " things.  The answers are:");
        }

        for (int i = 0; i < numExtractedItems; ++i) {
            ExtractedItem extractedItem = extractedItems.get(i);
            CeInstance instance = extractedItem.getInstance();

            if (multipleAnswers) {
                appendToSb(sb, "");
                sb.append(i);
                sb.append(") ");
            }

            ArrayList<CeInstance> processedInsts = new ArrayList<CeInstance>();

            sb.append(instanceType(instance));
            sb.append(instanceProperties(instance, processedInsts, questionType));
            sb.append(instanceReferences(instance, processedInsts));
        }

        return sb.toString();
    }

    // Generate reply detailing top matching properties
    private String propertyAnswer(FinalItem item) {
        StringBuilder sb = new StringBuilder();
        ExtractedItem extractedItem = item.getFirstExtractedItem();

        ArrayList<CeProperty> properties = extractedItem.getPropertyList();
        ArrayList<CeProperty> topLevelProperties = new ArrayList<CeProperty>();

        // Find top level property
        for (CeProperty prop : properties) {
            boolean addNewProp = false;
            ArrayList<CeProperty> propertiesToRemove = new ArrayList<CeProperty>();

            if (topLevelProperties.isEmpty()) {
                addNewProp = true;
            } else {
                for (CeProperty topLevelProp : topLevelProperties) {
                    CeConcept topDomainConcept = topLevelProp.getDomainConcept();
                    CeConcept propDomainConcept = prop.getDomainConcept();

                    if (propDomainConcept.isParentOf(topDomainConcept)) {
                        // New prop is parent of current top property
                        addNewProp = true;
                        propertiesToRemove.add(topLevelProp);
                    } else if (!propDomainConcept.hasParent(topDomainConcept)) {
                        // New prop is not a child of current top property
                        addNewProp = true;
                    }
                }
            }

            if (addNewProp) {
                topLevelProperties.add(prop);
                for (CeProperty removeProp : propertiesToRemove) {
                    topLevelProperties.remove(removeProp);
                }
            }
        }

        for (CeProperty property : topLevelProperties) {

            appendToSbNoNl(sb, "'");
            appendToSbNoNl(sb, property.getPropertyName());
            appendToSbNoNl(sb, "'");

            if (property.isObjectProperty()) {
                appendToSb(sb, " is a relationship.");
                appendToSbNoNl(sb, "It links ");
                appendToSbNoNl(sb, property.getDomainConcept().getConceptName());
                appendToSbNoNl(sb, " to ");
                appendToSbNoNl(sb, property.getRangeConceptName());
                appendToSb(sb, ".");
            } else {
                appendToSbNoNl(sb, " is an attribute on ");
                appendToSbNoNl(sb, property.getDomainConcept().getConceptName());
                appendToSb(sb, ".");
            }
        }

        return sb.toString();
    }

    // Generate reply detailing type of instance
    private String instanceType(CeInstance instance) {
        StringBuilder sb = new StringBuilder();
        boolean foundConceptType = false;

        appendToSbNoNl(sb, instance.getInstanceName());

        for (CeConcept leafCon : instance.getAllLeafConcepts()) {
            if (!isConfigConcept(leafCon)) {
                if (!foundConceptType) {
                    appendToSbNoNl(sb, " is ");
                    appendToSbNoNl(sb, leafCon.conceptQualifier());
                    appendToSbNoNl(sb, " ");
                    appendToSbNoNl(sb, leafCon.getConceptName());
                } else {
                    appendToSbNoNl(sb, " and ");
                    appendToSbNoNl(sb, leafCon.conceptQualifier());
                    appendToSbNoNl(sb, " ");
                    appendToSbNoNl(sb, leafCon.getConceptName());
                }

                foundConceptType = true;
            }
        }

        if (!foundConceptType) {
            appendToSbNoNl(sb, " is part of the CE store configuration (");
            appendToSbNoNl(sb, instance.getFirstLeafConceptName());
            appendToSbNoNl(sb, ")");
        }

        appendToSb(sb, ".");

        return sb.toString();
    }

    // Generate properties for instance
    private String instanceProperties(CeInstance instance, ArrayList<CeInstance> processedInsts, Word questionType) {
        StringBuilder sb = new StringBuilder();
        String CON_VALUE = "value";

        boolean foundProp = false;
        boolean allowConfigConcepts = allLeafConceptsAreConfigConcepts(instance);

        for (CePropertyInstance propertyInst : instance.getPropertyInstances()) {
            if (!ignoreProperty(propertyInst.getRelatedProperty())) {
                String propertyName = propertyInst.getPropertyName();
                CeConcept domainConcept = propertyInst.getRelatedProperty().getDomainConcept();

                if ((!isConfigConcept(domainConcept)) || allowConfigConcepts) {
                    if (!ignoreConcept(domainConcept)) {
                        for (CePropertyValue propertyValue : propertyInst.getUniquePropertyValues()) {
                            String rangeName = propertyValue.getRangeName();
                            String value = propertyValue.getValue();
//                            String fullRangeName = null;

                            if (rangeName.equals(CON_VALUE)) {
//                                fullRangeName = "";
                            } else {
//                                fullRangeName = " the " + rangeName;
                                CeInstance referringInst = ac.getModelBuilder().getInstanceNamed(ac, value);

                                if (referringInst != null) {
                                    processedInsts.add(referringInst);
                                }
                            }

                            if (!foundProp) {
                                String qualifier = computeQualifierFor(instance, allowConfigConcepts, questionType);
                                appendToSbNoNl(sb, qualifier);
                                appendToSbNoNl(sb, " ");
                            } else {
                                appendToSb(sb, " and");
                            }
                            foundProp = true;

                            if (propertyInst.getRelatedProperty().isVerbSingular()) {
                                appendToSbNoNl(sb, propertyName);
//                                if (isShowingRanges()) {
//                                    appendToSbNoNl(sb, fullRangeName);
//                                }
                                appendToSbNoNl(sb, " ");
                                appendToSbNoNl(sb, value);
                            } else {
                                appendToSbNoNl(sb, "has");
//                                if (isShowingRanges()) {
//                                    appendToSbNoNl(sb, fullRangeName);
//                                }
                                appendToSbNoNl(sb, " ");
                                appendToSbNoNl(sb, value);
                                appendToSbNoNl(sb, " as ");
                                appendToSbNoNl(sb, propertyName);
                            }
                        }
                    }
                }
            }
        }

        if (foundProp) {
            appendToSbNoNl(sb, ".");
        }

        return sb.toString();
    }

    // Generate references for instance
    private String instanceReferences(CeInstance instance, ArrayList<CeInstance> processedInsts) {
        StringBuilder sb = new StringBuilder();

        CePropertyInstance[] referringPIs = instance.getReferringPropertyInstances();
        boolean allowConfigConcepts = allLeafConceptsAreConfigConcepts(instance);
        boolean foundRef = false;

        for (CePropertyInstance propertyInst : referringPIs) {
            if (!ignoreProperty(propertyInst.getRelatedProperty())) {
                CeConcept domainCon = propertyInst.getRelatedProperty().getDomainConcept();

                if ((!isConfigConcept(domainCon)) || allowConfigConcepts) {
                    if (!ignoreConcept(domainCon)) {
                        CeInstance relatedInst = propertyInst.getRelatedInstance();

                        if (!processedInsts.contains(relatedInst)) {
                            foundRef = true;
                            appendToSb(sb, "");

//                            if (this.qp.isShowingRanges()) {
//                                appendMainTypeTextFor(thisInst, pAllowConfigCons);
//                                appendToSbNoNl(sb, " ");
//                            }

                            appendToSbNoNl(sb, relatedInst.getInstanceName());

                            if (propertyInst.getRelatedProperty().isVerbSingular()) {
                                appendToSbNoNl(sb, " ");
                                appendToSbNoNl(sb, propertyInst.getPropertyName());
                                appendToSbNoNl(sb, " ");
                                appendToSbNoNl(sb, instance.getInstanceName());
                            } else {
                                appendToSbNoNl(sb, " has ");
                                appendToSbNoNl(sb, instance.getInstanceName());
                                appendToSbNoNl(sb, " as ");
                                appendToSbNoNl(sb, propertyInst.getPropertyName());
                            }
                        }
                    }
                }
            }
        }

        if (foundRef) {
            appendToSbNoNl(sb, ".");
        }

        return sb.toString();
    }

    // Find out if concept a configuration concept
    private boolean isConfigConcept(CeConcept concept) {
        String CON_CONFIGCON = "configuration concept";
        CeConcept configCon = ac.getModelBuilder().getConceptNamed(ac, CON_CONFIGCON);

        return concept.equalsOrHasParent(configCon);
    }

    // Find out if all leaf concepts configuration concepts
    private boolean allLeafConceptsAreConfigConcepts(CeInstance instance) {
        boolean result = true;

        for (CeConcept concept : instance.getAllLeafConcepts()) {
            if (!isConfigConcept(concept)) {
                result = false;
                break;
            }
        }

        return result;
    }

    // Compute qualifier for instance
    public String computeQualifierFor(CeInstance instance, boolean allowConfigConcepts, Word questionType) {
        String qualifier = null;

        // First try each of the leaf concepts
        for (CeConcept leafCon : instance.getAllLeafConcepts()) {
            if ((!isConfigConcept(leafCon)) || allowConfigConcepts) {
                CeInstance mm = leafCon.retrieveMetaModelInstance(ac);
                qualifier = mm.getSingleValueFromPropertyNamed(Property.SINGLE_QUAL.toString());

                if (!qualifier.isEmpty()) {
                    break;
                }
            }
        }

        if ((qualifier == null) || (qualifier.isEmpty())) {
            // Now try parents
            for (CeConcept leafCon : instance.getAllLeafConcepts()) {
                if (!isConfigConcept(leafCon)) {
                    qualifier = getSingleQualifierFor(leafCon);

                    if (!qualifier.isEmpty()) {
                        break;
                    }
                }
            }
        }

        if ((qualifier == null) || (qualifier.isEmpty())) {
            // TODO: Dynamically define gender concepts in CE
            if (instance.isConceptNamed(ac, Concept.MAN.toString())) {
                qualifier = "He";
            } else if (instance.isConceptNamed(ac, Concept.WOMAN.toString())) {
                qualifier = "She";
            } else if (instance.isConceptNamed(ac, Concept.WOMAN.toString())) {
                qualifier = "He or she";
            } else {
                qualifier = "It";
            }
        }

        return qualifier;
    }

    // Get qualifier for concept
    private String getSingleQualifierFor(CeConcept concept) {
        String qualifier = null;
        CeInstance mm = concept.retrieveMetaModelInstance(ac);

        if (mm != null) {
            qualifier = mm.getSingleValueFromPropertyNamed(Property.SINGLE_QUAL.toString());

            if (qualifier.isEmpty()) {
                for (CeConcept parCon : concept.getDirectParents()) {
                    qualifier = getSingleQualifierFor(parCon);

                    if (!qualifier.isEmpty()) {
                        break;
                    }
                }
            }
        }

        return qualifier;
    }

    // Should this concept be ignored
    public boolean ignoreConcept(CeConcept concept) {
        ArrayList<String> excludeList = new ArrayList<String>();
        excludeList.add(Concept.CONV_THING.toString());

        return concept.equalsOrHasParentNamed(ac, excludeList);
    }

    // Should this property be ignored
    public boolean ignoreProperty(CeProperty property) {
        boolean result = false;

        CeInstance metaModel = property.getMetaModelInstance(ac);

        if (metaModel != null) {
            result = metaModel.isConceptNamed(ac, Concept.UNINTERESTING.toString());
        }

        return result;
    }

    public String nothingtUnderstood() {
        return "I didn't manage to understand any of that, sorry";
    }
}
