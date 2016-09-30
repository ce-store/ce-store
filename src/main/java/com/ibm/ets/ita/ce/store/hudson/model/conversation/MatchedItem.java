package com.ibm.ets.ita.ce.store.hudson.model.conversation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class MatchedItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CONTEXT_MATCHCON = "matched-concept";
	private static final String CONTEXT_MATCHPROP = "matched-property";
	private static final String CONTEXT_MATCHINST = "matched-instance";
	private static final String CONTEXT_REFCON_EXACT = "referred-concept-exact";
	private static final String CONTEXT_REFCON_PLURAL = "referred-concept-plural";
	private static final String CONTEXT_REFCON_PAST = "referred-concept-past";
	private static final String CONTEXT_REFPROP_EXACT = "referred-property-exact";
	private static final String CONTEXT_REFINST_EXACT = "referred-instance-exact";
	private static final String CONTEXT_REFINST_PLURAL = "referred-instance-plural";

	private ProcessedWord firstWord = null;
	private ArrayList<ProcessedWord> otherWords = null;
	private CeInstance instance = null;
	private CeConcept concept = null;
	private CeProperty property = null;

	private String context = null;
	private String phraseText = null;

	private MatchedItem(ProcessedWord pWord, String pContext) {
		// Private constructor to force use of static methods to create
		this.context = pContext;
		this.firstWord = pWord;
	}

	public static MatchedItem createForMatchedConcept(ProcessedWord pWord, CeConcept pCon) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_MATCHCON);

		mi.concept = pCon;
		mi.phraseText = pWord.getLcWordText();

		return mi;
	}

	public static MatchedItem createForReferredConceptExact(ProcessedWord pWord, CeConcept pCon, String pPhraseText,
			ArrayList<ProcessedWord> pOtherWords) {
		MatchedItem mi = createForReferredConceptExact(pWord, pCon, pPhraseText);

		mi.setOtherWords(pOtherWords);

		return mi;
	}

	public static MatchedItem createForReferredConceptExact(ProcessedWord pWord, CeConcept pCon, String pPhraseText) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_REFCON_EXACT);

		mi.concept = pCon;
		mi.phraseText = pPhraseText;

		return mi;
	}

	public static MatchedItem createForReferredConceptPlural(ProcessedWord pWord, CeConcept pCon, String pPhraseText,
			ArrayList<ProcessedWord> pOtherWords) {
		MatchedItem mi = createForReferredConceptPlural(pWord, pCon, pPhraseText);

		mi.setOtherWords(pOtherWords);

		return mi;
	}

	public static MatchedItem createForReferredConceptPlural(ProcessedWord pWord, CeConcept pCon, String pPhraseText) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_REFCON_PLURAL);

		mi.concept = pCon;
		mi.phraseText = pPhraseText;

		return mi;
	}

	public static MatchedItem createForReferredConceptPastTense(ProcessedWord pWord, CeConcept pCon, String pPhraseText,
			ArrayList<ProcessedWord> pOtherWords) {
		MatchedItem mi = createForReferredConceptPastTense(pWord, pCon, pPhraseText);

		mi.setOtherWords(pOtherWords);

		return mi;
	}

	public static MatchedItem createForReferredConceptPastTense(ProcessedWord pWord, CeConcept pCon,
			String pPhraseText) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_REFCON_PAST);

		mi.concept = pCon;
		mi.phraseText = pPhraseText;

		return mi;
	}

	public static MatchedItem createForMatchedProperty(ProcessedWord pWord, CeProperty pProp) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_MATCHPROP);

		mi.property = pProp;
		mi.phraseText = pWord.getLcWordText();

		return mi;
	}

	public static MatchedItem createForReferredPropertyExact(ProcessedWord pWord, CeProperty pProp, String pPhraseText,
			ArrayList<ProcessedWord> pOtherWords) {
		MatchedItem mi = createForReferredPropertyExact(pWord, pProp, pPhraseText);

		mi.setOtherWords(pOtherWords);

		return mi;
	}

	public static MatchedItem createForReferredPropertyExact(ProcessedWord pWord, CeProperty pProp,
			String pPhraseText) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_REFPROP_EXACT);

		mi.property = pProp;
		mi.phraseText = pPhraseText;

		return mi;
	}

	public static MatchedItem createForMatchedInstance(ProcessedWord pWord, CeInstance pInst) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_MATCHINST);

		mi.instance = pInst;
		mi.phraseText = pWord.getLcWordText();

		return mi;
	}

	public static MatchedItem createForReferredInstanceExact(ProcessedWord pWord, CeInstance pInst, String pPhraseText,
			ArrayList<ProcessedWord> pOtherWords) {
		MatchedItem mi = createForReferredInstanceExact(pWord, pInst, pPhraseText);

		mi.setOtherWords(pOtherWords);

		return mi;
	}

	public static MatchedItem createForReferredInstanceExact(ProcessedWord pWord, CeInstance pInst,
			String pPhraseText) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_REFINST_EXACT);

		mi.instance = pInst;
		mi.phraseText = pPhraseText;

		return mi;
	}

	public static MatchedItem createForReferredInstancePlural(ProcessedWord pWord, CeInstance pInst,
			String pPhraseText) {
		MatchedItem mi = new MatchedItem(pWord, CONTEXT_REFINST_PLURAL);

		mi.instance = pInst;
		mi.phraseText = pPhraseText;

		return mi;
	}

	public String getPhraseText() {
		return this.phraseText;
	}

	public int getStartPos() {
		return getFirstWord().getWordPos();
	}

	public int getEndPos() {
		return getLastWord().getWordPos();
	}

	public CeInstance getInstance() {
		return this.instance;
	}

	public CeConcept getConcept() {
		return this.concept;
	}

	public CeProperty getProperty() {
		return this.property;
	}

	public ProcessedWord getFirstWord() {
		return this.firstWord;
	}

	public ProcessedWord getLastWord() {
		ProcessedWord lastWord = this.firstWord;

		if (this.otherWords != null) {
			for (ProcessedWord thisWord : this.otherWords) {
				if (thisWord.getWordPos() > lastWord.getWordPos()) {
					lastWord = thisWord;
				}
			}
		}

		return lastWord;
	}

	public ArrayList<ProcessedWord> getOtherWords() {
		return this.otherWords;
	}

	public void setOtherWords(ArrayList<ProcessedWord> pWords) {
		this.otherWords = pWords;

		for (ProcessedWord thisWord : this.otherWords) {
			thisWord.addOtherMatchedItem(this);
		}
	}

	public boolean hasConcept() {
		return this.concept != null;
	}

	public boolean hasProperty() {
		return this.property != null;
	}

	public boolean hasInstance() {
		return this.instance != null;
	}

	public boolean isMatchedConcept() {
		return this.context.equals(CONTEXT_MATCHCON);
	}

	public boolean isMatchedProperty() {
		return this.context.equals(CONTEXT_MATCHPROP);
	}

	public boolean isMatchedInstance() {
		return this.context.equals(CONTEXT_MATCHINST);
	}

	public boolean isReferredConceptExact() {
		return this.context.equals(CONTEXT_REFCON_EXACT);
	}

	public boolean isReferredConceptPlural() {
		return this.context.equals(CONTEXT_REFCON_PLURAL);
	}

	public boolean isReferredConceptPastTense() {
		return this.context.equals(CONTEXT_REFCON_PAST);
	}

	public boolean isReferredPropertyExact() {
		return this.context.equals(CONTEXT_REFPROP_EXACT);
	}

	public boolean isReferredInstanceExact() {
		return this.context.equals(CONTEXT_REFINST_EXACT);
	}

	public boolean isReferredInstancePlural() {
		return this.context.equals(CONTEXT_REFINST_PLURAL);
	}

}
