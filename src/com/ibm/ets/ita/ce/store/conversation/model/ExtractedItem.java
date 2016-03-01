package com.ibm.ets.ita.ce.store.conversation.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class ExtractedItem extends GeneralItem {
    public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

    private static final String CON_GROUP = "group";
    private static final String PROP_EXPBY = "is expressed by";

    private ProcessedWord determinerWord = null;
    private ProcessedWord startWord = null;
    private ArrayList<ProcessedWord> otherWords = new ArrayList<ProcessedWord>();
    private CeConcept concept = null;
    private ArrayList<CeProperty> propList = null;
    private CeInstance instance = null;
    private String newInstanceId = null;
    private ArrayList<MatchedTriple> matchedTriples = new ArrayList<MatchedTriple>();

    private ExtractedItem previousItem = null;
    private ExtractedItem nextItem = null;

    public ExtractedItem(ProcessedWord pWord, CeConcept pConcept) {
        pWord.addExtractedItem(this);

        this.id = "ei_" + pWord.getId();
        this.startWord = pWord;
        this.concept = pConcept;

        ProcessedWord possDet = this.startWord.getPreviousProcessedWord();

        if (possDet != null) {
            if (possDet.isDeterminer()) {
                this.determinerWord = possDet;
                possDet.addExtractedItem(this);
            }
        }
    }

    public ExtractedItem(ProcessedWord pWord, ArrayList<CeProperty> pPropList) {
        pWord.addExtractedItem(this);

        this.id = "ei_" + pWord.getId();
        this.startWord = pWord;
        this.propList = pPropList;

        ProcessedWord possDet = this.startWord.getPreviousProcessedWord();

        if (possDet != null) {
            if (possDet.isDeterminer()) {
                this.determinerWord = possDet;
                possDet.addExtractedItem(this);
            }
        }
    }

    public ExtractedItem(ProcessedWord pWord, CeInstance pInstance) {
        pWord.addExtractedItem(this);

        this.id = "ei_" + pWord.getId();
        this.startWord = pWord;
        this.instance = pInstance;

        ProcessedWord possDet = this.startWord.getPreviousProcessedWord();

        if (possDet != null) {
            if (possDet.isDeterminer()) {
                this.determinerWord = possDet;
                possDet.addExtractedItem(this);
            }
        }
    }

    public ProcessedWord getDeterminerWord() {
        return this.determinerWord;
    }

    public ProcessedWord getStartWord() {
        return this.startWord;
    }

    public ArrayList<ProcessedWord> getOtherWords() {
        return this.otherWords;
    }

    public void addOtherWord(ProcessedWord pWord) {
        this.otherWords.add(pWord);
        pWord.addExtractedItem(this);
    }

    public CeConcept getConcept() {
        return this.concept;
    }

    public ArrayList<CeProperty> getPropertyList() {
        return this.propList;
    }

    public CeProperty getFirstProperty() {
        CeProperty result = null;

        //TODO: This needs to be more intelligent
        if ((this.propList != null) && (!this.propList.isEmpty())) {
            result = this.propList.get(0);
        }

        return result;
    }

    public CeProperty getPropertyWithDomain(ActionContext pAc, CeConcept pDomCon) {
        CeProperty result = null;

        if (this.propList != null) {
            for (CeProperty thisProp : this.propList) {
                if (pDomCon.equalsOrHasParent(thisProp.getDomainConcept())) {
                    if (result != null) {
                        reportWarning("More than one property with domain concept '" + pDomCon.getConceptName() + "' found for: " + this.toString(), pAc);
                    }
                    result = thisProp;
                }
            }
        }

        return result;
    }

    public CeInstance getInstance() {
        return this.instance;
    }

    public String getNewInstanceId() {
        return this.newInstanceId;
    }

    public void setNewInstanceId(String pVal) {
        this.newInstanceId = pVal;
    }

    public void setPreviousItem(ExtractedItem pEi) {
        this.previousItem = pEi;
        pEi.setNextItem(this);
    }

    public void setNextItem(ExtractedItem pEi) {
        this.nextItem = pEi;
    }

    public ArrayList<MatchedTriple> getMatchedTriples() {
        return this.matchedTriples;
    }

    public void addMatchedTriple(MatchedTriple pMt) {
        this.matchedTriples.add(pMt);
    }

    public boolean isConceptItem() {
        return this.concept != null;
    }

    public boolean isPropertyItem() {
        return this.propList != null;
    }

    public boolean isInstanceItem() {
        return this.instance != null;
    }

    public boolean isPluralConceptMatch() {
        return this.startWord.refersToPluralConceptsExactly();
    }

    public boolean isPluralOrGroup() {
        return isPluralConceptMatch() || isGroupConcept();
    }

    public boolean isGroupConcept() {
        boolean result = false;

        if (isConceptItem()) {
            this.concept.getConceptName().equals(CON_GROUP);
        }

        return result;
    }

    public ProcessedWord getLastWord() {
        ProcessedWord result = null;

        if (this.otherWords.isEmpty()) {
            result = this.startWord;
        } else {
            result = this.otherWords.get(this.otherWords.size() - 1);
        }

        return result;
    }

    public String getOriginalDescription() {
        return formattedAllWords();
    }

    public String getOriginalDescriptionForPreviousItem() {
        String result = null;

        if (this.previousItem != null) {
            result = this.previousItem.getOriginalDescription();
        } else {
            result = getOriginalDescription();
        }

        return result;
    }

    public String getOriginalDescriptionForNextItem() {
        String result = null;

        if (this.nextItem != null) {
            result = this.nextItem.getOriginalDescription();
        } else {
//			result = getOriginalDescription();
            //TODO: Even if the word is not matched we should create an extracted item
            //or similar to allow us to get to the unhandled words.
            result = "???";
        }

        return result;
    }

    public String formattedAllWords() {
        String result = "";

        if (this.determinerWord != null) {
            result += this.determinerWord.getWordText() + " ";
        }

        result += this.startWord.getWordText();

        for (ProcessedWord gw : this.otherWords) {
            result += " " + gw.getWordText();
        }

        return result;
    }
    public String formattedType() {
        String result = "";

        if (isConceptItem()) {
            result = "concept";
        } else if (isPropertyItem()) {
            result = "property";
        } else if (isInstanceItem()) {
            result = "instance";
        } else {
            result = "unknown";
        }

        return result;
    }

    public String formattedItemName() {
        String result = "";

        if (isConceptItem()) {
            result = this.concept.getConceptName();
        } else if (isPropertyItem()) {
            if (!this.propList.isEmpty()) {
                result = this.propList.get(0).formattedFullPropertyName();
            }
        } else if (isInstanceItem()) {
            result = this.instance.getInstanceName();
        } else {
            result = "unknown";
        }

        return result;
    }

    public boolean hasMeaningfulDescription(ActionContext pAc) {
        boolean result = true;

        if (isConceptItem()) {
            String lcConName = this.concept.getConceptName().toLowerCase();
            String lcDesc = getOriginalDescription().toLowerCase();
            lcDesc = removeDeterminersFrom(lcDesc);

            result = !lcConName.equals(lcDesc);

            if (result) {
                CeInstance mm = this.concept.retrieveMetaModelInstance(pAc);

                for (String synonym : mm.getValueListFromPropertyNamed(PROP_EXPBY)) {
                    if (synonym.toLowerCase().equals(lcDesc)) {
                        result = false;
                        break;
                    }
                }
            }
        }

        if (isInstanceItem()) {
            String lcInstName = this.instance.getInstanceName().toLowerCase();
            String lcDesc = getOriginalDescription().toLowerCase();

            result = !lcInstName.equals(lcDesc);

            if (result) {
                for (String synonym : this.instance.getValueListFromPropertyNamed(PROP_EXPBY)) {
                    if (synonym.toLowerCase().equals(lcDesc)) {
                        result = false;
                        break;
                    }
                }
            }
        }

        //TODO: Handle properties here

        return result;
    }

    private static String removeDeterminersFrom(String pLcText) {
        String result = pLcText;

        //TODO: This should not be hardcoded
        if (result.startsWith("a ")) {
            result = result.substring(2, result.length());
        }
        if (result.startsWith("an ")) {
            result = result.substring(3, result.length());
        }
        if (result.startsWith("the ")) {
            result = result.substring(4, result.length());
        }

        return result;
    }

    public String calculateItemText() {
        String result = "";

        if (this.determinerWord != null) {
            result += this.determinerWord.getWordText() + " ";
        }

        result += this.startWord.getWordText();

        for (ProcessedWord otherWord : this.otherWords) {
            result += " " + otherWord.getWordText();
        }

        return result;
    }

    public boolean isDominantInterpretation() {
        boolean result = false;

        //TODO: This should be a much richer test
        //Currently any extracted item that starts with a word that has multiple meanings is not a dominant interpretation
        result = this.getStartWord().getExtractedItems().size() == 1;

        return result;
    }

    @Override
    public String toString() {
        String result = "";
        String owText = "";

        for (ProcessedWord oWord : this.otherWords) {
            if (owText.isEmpty()) {
                owText = ", otherWords=";
            } else {
                owText += ", ";
            }
            owText += oWord.getWordText();
        }

        result = "[Extracted " + formattedType() + "] startWord=" + this.startWord.getWordText() + owText;

        return result;
    }

}
