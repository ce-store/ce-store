package com.ibm.ets.ita.ce.store.conversation.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.stripDelimitingQuotesFrom;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.processor.SentenceProcessor;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class ProcessedWord extends GeneralItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_ENTCON = "entity concept";
	private static final String CON_PROPCON = "property concept";
	private static final String CON_PROWORD = "processed word";
	private static final String CON_UNMWORD = "unmatched word";
	private static final String CON_LINGTHING = "linguistic thing";
	private static final String CON_MAN = "man";
	private static final String CON_WOMAN = "woman";
	private static final String PROP_PROPNAME = "property name";
	private static final String PROP_PLURAL = "plural form";
	private static final String PROP_EXPBY = "is expressed by";
	private static final String PROP_COMMWORD = "common word";
	private static final String PROP_NEGWORD = "negation word";
	private static final String PROP_REPLY = "is in reply to";
	private static final String PROP_ABOUT = "is about";
	private static final String DET_A = "a";
	private static final String DET_AN = "an";
	private static final String DET_THE = "the";
	private static final String WORD_HERE = "here";
	private static final String Q_WHAT = "what";
	private static final String Q_WHO = "who";
	private static final String Q_WHERE = "where";
	private static final String Q_WHICH = "which";
	private static final String Q_WHY = "why";
	private static final String Q_COUNT = "count";
	private static final String Q_LIST = "list";
	private static final String Q_SUMM1 = "summarise";
	private static final String Q_SUMM2 = "summarize";
	private static final String Q_ALL = "all";
	private static final String AR_HE = "he";
	private static final String AR_SHE = "she";
	private static final String AR_THEY = "they";
	private static final String AR_IT = "it";

	private ConvWord convWord = null;
	private ArrayList<ExtractedItem> extractedItems = null;
	private String lcText = null;

	//Matched (directly, by the name)
	private CeConcept matchingConcept = null;
	private TreeMap<String, CeProperty> matchingRelations = null;
	private CeInstance matchingInstance = null;

	//Referred (indirectly
	private TreeMap<String, CeConcept> referredExactConcepts = null;
	private TreeMap<String, CeProperty> referredExactRelations = null;
	private TreeMap<String, CeInstance> referredExactInstances = null;
	private TreeMap<String, CeConcept> referredExactConceptsPlural = null;
	private TreeMap<String, CeInstance> referredExactInstancesPlural = null;

	//Types of words
	private boolean isStandardWord = false;
	private boolean isNegationWord = false;
	private boolean isValueWord = false;
	private boolean isNumberWord = false;
	private boolean isQuestionWord = false;
	private boolean isAnaphoricReference = false;
	private boolean confirmRequired = false;

	private boolean partialStartWord = false;
	private boolean partialConceptReference = false;
	private boolean partialRelationReference = false;
	private boolean partialInstanceReference = false;

	private CeInstance chosenInstance = null;

	private ProcessedWord(ConvWord pConvWord, SentenceProcessor pSp) {
		this.id = pConvWord.getId();
		this.convWord = pConvWord;

		this.lcText = declutter(pConvWord.getWordText().toLowerCase());

		try {
			//Attempt to create a double
			Double.parseDouble(getDeclutteredText());
			this.isNumberWord = true;
		} catch (NumberFormatException e) {
			//There was a number format exception so this is not a number word
			this.isNumberWord = false;
		}

		if (this.lcText.equals(WORD_HERE)) {
			doSpecialHereProcessing(pSp);
		}
	}

	private void doSpecialHereProcessing(SentenceProcessor pSp) {
		CeInstance locInst = pSp.getLocationInstance();

		if (locInst != null) {
			this.matchingInstance = locInst;
		}
	}

	public CeInstance getChosenInstance() {
		return this.chosenInstance;
	}

	public String getConceptName() {
		String result = null;

		if (isUnmatchedWord()) {
			result = CON_UNMWORD;
		} else {
			result = CON_PROWORD;
		}

		return result;
	}

	public String getDeterminer() {
		String result = null;

		if (isUnmatchedWord()) {
			result = DET_AN;
		} else {
			result = DET_A;
		}

		return result;
	}

	public void setChosenInstance(CeInstance pInst) {
		this.chosenInstance = pInst;
	}

	public static ProcessedWord createFrom(ConvWord pConvWord, SentenceProcessor pSp) {
		ProcessedWord newGw = new ProcessedWord(pConvWord, pSp);

		pConvWord.setProcessedWord(newGw);

		return newGw;
	}

	public ConvWord getConvWord() {
		return this.convWord;
	}

	public String getWordText() {
		return this.convWord.getWordText();
	}

	public CeConcept getMatchingConcept() {
		return this.matchingConcept;
	}

	public CeInstance getMatchingInstance() {
		return this.matchingInstance;
	}

	public boolean matchesToConcept() {
		return this.matchingConcept != null;
	}

	public boolean matchesToRelations() {
		return (this.matchingRelations != null) && !this.matchingRelations.isEmpty();
	}

	public boolean matchesToInstance() {
		return this.matchingInstance != null;
	}

	public boolean refersToConceptsExactly() {
		return (this.referredExactConcepts != null) && !this.referredExactConcepts.isEmpty();
	}

	public boolean refersToInstanceOfConceptNamed(ActionContext pAc, String pConName) {
		boolean result = false;

		if (this.matchingInstance != null) {
			result = this.matchingInstance.isConceptNamed(pAc, pConName);
		}

		return result;
	}

	public boolean refersToPluralConceptsExactly() {
		return (this.referredExactConceptsPlural != null) && !this.referredExactConceptsPlural.isEmpty();
	}

	public boolean refersToPluralInstancesExactly() {
		return (this.referredExactInstancesPlural != null) && !this.referredExactInstancesPlural.isEmpty();
	}

	public boolean refersToRelationsExactly() {
		return (this.referredExactRelations != null) && !this.referredExactRelations.isEmpty();
	}

	public boolean refersToInstancesExactly() {
		return (this.referredExactInstances != null) && !this.referredExactInstances.isEmpty();
	}

	public ArrayList<ExtractedItem> getExtractedItems() {
		return this.extractedItems;
	}

	public void addExtractedItem(ExtractedItem pEi) {
		if (this.extractedItems == null) {
			this.extractedItems = new ArrayList<ExtractedItem>();
		}

		this.extractedItems.add(pEi);
	}

	public String getLcWordText() {
		return this.lcText;
	}

	public boolean isStandardWord() {
		return this.isStandardWord;
	}

	public boolean isNegationWord() {
		return this.isNegationWord;
	}

	public boolean isPureNegation() {
		return this.isNegationWord && (!isGrounded() && !this.partialStartWord);
	}

	public boolean isWhat() {
		return this.lcText.equals(Q_WHAT);
	}

	public boolean isWho() {
		return this.lcText.equals(Q_WHO);
	}

	public boolean isWhere() {
		return this.lcText.equals(Q_WHERE);
	}

	public boolean isWhich() {
		return this.lcText.equals(Q_WHICH);
	}

	public boolean isWhy() {
		return this.lcText.equals(Q_WHY);
	}

	public boolean isCount() {
		return this.lcText.equals(Q_COUNT);
	}

	public boolean isList() {
		return this.lcText.equals(Q_LIST);
	}

	public boolean isSummarise() {
		return this.lcText.equals(Q_SUMM1) || this.lcText.equals(Q_SUMM2);
	}

	public boolean isAll() {
		return this.lcText.equals(Q_ALL);
	}

	public boolean isValueWord() {
		return this.isValueWord;
	}

	public boolean isNumberWord() {
		return this.isNumberWord;
	}

//	public boolean isCommonWord() {
//		return this.isCommonWord;
//	}

	public boolean isQuestionWord() {
		return this.isQuestionWord;
	}

//	public void markAsCommonWord() {
//		this.isCommonWord = true;
//	}

	public void markAsQuestionWord() {
		this.isQuestionWord = true;
	}

	public boolean confirmRequired() {
		return this.confirmRequired;
	}

	public boolean isDeterminer() {
		String decText = getDeclutteredText();

		//TODO: This should not be hardcoded
		return
			(decText.equals(DET_A)) ||
			(decText.equals(DET_AN)) ||
			(decText.equals(DET_THE));
	}

	public boolean isUnmatchedWord() {

		return
			(this.matchingConcept == null) &&
			!matchesToRelations() &&
			(this.matchingInstance == null) &&
			!refersToConceptsExactly() &&
			!refersToPluralConceptsExactly() &&
			!refersToRelationsExactly() &&
			!refersToInstancesExactly() &&
			!refersToPluralInstancesExactly() &&
			(!this.isStandardWord) &&
			(!this.isNegationWord) &&
			(!this.isValueWord) &&
			(!this.isNumberWord) &&
			(!this.partialConceptReference) &&
			(!this.partialRelationReference) &&
			(!this.partialInstanceReference) &&
			(!isDeterminer()) &&
			(!isQuestionWord());
	}

	public boolean isLaterPartOfPartial() {
		return
			(this.partialConceptReference ||
					this.partialRelationReference ||
					this.partialInstanceReference) &&
			!this.partialStartWord;
	}

	private void checkForAnaphoricReference(ActionContext pAc, CeInstance pCardInstance) {
		String strippedWord = getDeclutteredText();

		if (strippedWord.equals(AR_HE)
				|| strippedWord.equals(AR_SHE)
				|| strippedWord.equals(AR_THEY)
				|| strippedWord.equals(AR_IT)) {
			isAnaphoricReference = true;

			CeInstance prevCard = pCardInstance.getSingleInstanceFromPropertyNamed(pAc, PROP_REPLY);

			// arbitrary limit of 10 cards deep
			for (int i = 0; i < 10 && this.matchingInstance == null && prevCard != null && this.confirmRequired == false; ++i) {
				ArrayList<CeInstance> prevCardAbouts = prevCard.getInstanceListFromPropertyNamed(pAc, PROP_ABOUT);

				// try and match all instances that previous card is about
				ArrayList<CeInstance> matchingInstances = new ArrayList<CeInstance>();
				for (CeInstance prevCardAbout : prevCardAbouts) {
					if (strippedWord.equals(AR_HE) && prevCardAbout.isConceptNamed(pAc, CON_MAN)) {
						// word == "he" and last talked about instance was a man
						matchingInstances.add(prevCardAbout);
					} else if (strippedWord.equals(AR_SHE) && prevCardAbout.isConceptNamed(pAc, CON_WOMAN)) {
						// word == "she" and last talked about instance was a woman
						matchingInstances.add(prevCardAbout);
					}

				}


				if (matchingInstances.size() == 1) {
					// if only one match in previous card, assume correct
					this.matchingInstance = matchingInstances.get(0);
				} else if (matchingInstances.size() > 1) {
					// if more than one match, ask confirm card
					this.confirmRequired = true;
				}

				// get previous card
				prevCard = prevCard.getSingleInstanceFromPropertyNamed(pAc, PROP_REPLY);
			}


		}
	}

	private void checkForMatchingConcept(ActionContext pAc) {
		String strippedWord = getDeclutteredText();
		CeConcept possCon = pAc.getModelBuilder().getConceptNamed(pAc, strippedWord);

		this.matchingConcept = possCon;
	}

	private void checkForMatchingInstances(ActionContext pAc) {
		CeInstance tempInst = pAc.getIndexedEntityAccessor().getInstanceNamedOrIdentifiedAs(pAc, getDeclutteredText());

		if (isValidMatchingInstance(tempInst)) {
			this.matchingInstance = tempInst;
		}
	}

	private static boolean isValidMatchingInstance(CeInstance pInst) {
		boolean result = false;

		if (pInst != null) {
			result = !pInst.isOnlyMetaModelInstance();
		}

		return result;
	}

	private String getDeclutteredText() {
		return this.lcText;
	}

	private static String declutter(String pRawText) {
		String result = stripDelimitingQuotesFrom(pRawText);

		if (result.startsWith("@")) {
			result = result.substring(1, result.length());
		}

		if (result.startsWith("#")) {
			result = result.substring(1, result.length());
		}

		//TODO: Is the declutter phase the best place for this?
		if (result.endsWith("'s")) {
			result = result.substring(0, result.length() - 2);
		}

		//TODO: Is the declutter phase the best place for this?
		if (result.endsWith("’s")) {
			result = result.substring(0, result.length() - 2);
		}

		return result;
	}

	public void classify(ActionContext pAc, ArrayList<CeInstance> pComWordLists, ArrayList<CeInstance> pNegWordLists, CeInstance pCardInstance) {
		checkForMatchingConcept(pAc);
		checkForMatchingRelation(pAc);
		checkForMatchingInstances(pAc);

		checkForReferringConcepts(pAc);
		checkForReferringRelations(pAc);
		checkForReferringInstances(pAc);

		checkForStandardWords(pComWordLists, pNegWordLists);
		checkForAnaphoricReference(pAc, pCardInstance);

		String decText = getDeclutteredText();

		checkForPartialMatchingConcepts(pAc, decText);
		checkForPartialMatchingRelations(pAc, decText);
		checkForPartialMatchingInstances(pAc, decText);
	}

	private void checkForReferringConcepts(ActionContext pAc) {
		for (CeInstance thisInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, CON_ENTCON)) {
			checkForConceptByExpressedBy(pAc, thisInst);
			checkForConceptByPluralForm(pAc, thisInst);
		}
	}

	private void checkForConceptByExpressedBy(ActionContext pAc, CeInstance pEntConInst) {
		String conName = pEntConInst.getInstanceName();
		String decText = getDeclutteredText().toLowerCase();
		ArrayList<String> expList = pEntConInst.getValueListFromPropertyNamed(PROP_EXPBY);
		ArrayList<String> lcExpList = new ArrayList<String>();

		for (String thisExp : expList) {
			lcExpList.add(thisExp.toLowerCase());
		}

		for (String expVal : lcExpList) {
			boolean match = expVal.equals(decText);
			boolean exact = false;

			if (match) {
				exact = true;
			} else {
				exact = false;
				match = expVal.startsWith(decText);
			}

			CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, conName);

			if (match) {
				if (exact) {
					if (this.matchingConcept != tgtCon) {
						addReferredExactConcept(pAc, conName, tgtCon);
					}
				} else {
					if (testForFullConceptReferenceWithLaterWords(pAc, decText, lcExpList, 1)) {
						reportMicroDebug("Found multiple word concept reference at: " + decText, pAc);
						this.partialStartWord = true;
						this.addReferredExactConcept(pAc, conName, tgtCon);
					}
				}
			}
		}
	}

	private void checkForInstanceByExpressedBy(ActionContext pAc, CeInstance pPossInst) {
		String decText = getDeclutteredText().toLowerCase();

		ArrayList<String> expList = pPossInst.getValueListFromPropertyNamed(PROP_EXPBY);
		ArrayList<String> lcExpList = new ArrayList<String>();

		for (String thisExp : expList) {
			lcExpList.add(thisExp.toLowerCase());
		}

		for (String expVal : lcExpList) {
			boolean match = expVal.equals(decText);
			boolean exact = false;

			if (match) {
				exact = true;
			} else {
				exact = false;
				match = expVal.startsWith(decText);
			}

			if (match) {
				if (exact) {
					//This is an exact match so just save the referenced instance
					this.addReferredExactInstance(pPossInst);
				} else {
					if (testForFullInstanceReferenceWithLaterWords(pAc, decText, lcExpList, 1)) {
						reportMicroDebug("Found multiple word instance reference at: " + decText, pAc);
						this.partialStartWord = true;
						this.addReferredExactInstance(pPossInst);
					}
				}
			}
		}
	}

	private void checkForConceptByPluralForm(ActionContext pAc, CeInstance pEntConInst) {
		String conName = pEntConInst.getInstanceName();
		String decText = getDeclutteredText();

		ArrayList<String> expList = pEntConInst.getValueListFromPropertyNamed(PROP_PLURAL);
		ArrayList<String> lcExpList = new ArrayList<String>();

		for (String thisExp : expList) {
			lcExpList.add(thisExp.toLowerCase());
		}

		for (String expVal : lcExpList) {
			boolean match = expVal.equals(decText);
			boolean exact = false;

			if (match) {
				exact = true;
			} else {
				exact = false;
				match = expVal.startsWith(decText);
			}

			CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, conName);

			if (match) {
				if (exact) {
					if (this.matchingConcept != tgtCon) {
						addReferredExactConceptPlural(pAc, conName, tgtCon);
					}
				} else {
					if (testForFullConceptReferenceWithLaterWords(pAc, decText, lcExpList, 1)) {
						reportMicroDebug("Found multiple word concept reference at: " + decText, pAc);
						this.partialStartWord = true;
						this.addReferredExactConceptPlural(pAc, conName, tgtCon);
					}
				}
			}
		}
	}

	private void addMatchingRelation(CeProperty pProp) {
		if (this.matchingRelations == null) {
			this.matchingRelations = new TreeMap<String, CeProperty>();
		}

		this.matchingRelations.put(pProp.formattedFullPropertyName(), pProp);
	}

	public Collection<CeProperty> listMatchingRelations() {
		Collection<CeProperty> result = null;

		if (this.matchingRelations != null) {
			result = this.matchingRelations.values();
		} else {
			result = new ArrayList<CeProperty>();
		}

		return result;
	}

	private void checkForMatchingRelation(ActionContext pAc) {
		String decText = getDeclutteredText();

		for (CeInstance thisInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, CON_PROPCON)) {
			String propFullName = thisInst.getInstanceName();

			for (String expVal : thisInst.getValueListFromPropertyNamed(PROP_PROPNAME)) {
				boolean match = expVal.equals(decText);

				if (match) {
					CeProperty tgtProp = pAc.getModelBuilder().getPropertyFullyNamed(propFullName);
					addMatchingRelation(tgtProp);
				}
			}
		}
	}

	private boolean alreadyRefersToExactRelation(CeProperty pProp) {
		boolean result = false;

		if (refersToRelationsExactly()) {
			result = this.referredExactRelations.containsValue(pProp);
		} else {
			result = false;
		}

		return result;
	}

	private void checkForReferringRelations(ActionContext pAc) {
		for (CeInstance thisInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, CON_PROPCON)) {
			String propFullName = thisInst.getInstanceName();
			String decText = getDeclutteredText();

			ArrayList<String> expList = thisInst.getValueListFromPropertyNamed(PROP_EXPBY);

			if (!expList.isEmpty()) {
				ArrayList<String> lcExpList = new ArrayList<String>();

				for (String thisExp : expList) {
					lcExpList.add(thisExp.toLowerCase());
				}

				for (String expVal : lcExpList) {
					boolean match = expVal.equals(decText);
					boolean exact = false;

					if (match) {
						exact = true;
					} else {
						exact = false;
						match = expVal.startsWith(decText);
					}

					if (match) {
						if (exact) {
							CeProperty tgtProp = pAc.getModelBuilder().getPropertyFullyNamed(propFullName);
							if (!alreadyMatchesRelation(tgtProp)) {
								addReferredExactRelation(pAc, propFullName, tgtProp);
							}
						} else {
							CeProperty tgtProp = pAc.getModelBuilder().getPropertyFullyNamed(propFullName);
							if (tgtProp != null) {
								if (!alreadyMatchesRelation(tgtProp)) {
									if (!alreadyRefersToExactRelation(tgtProp)) {
										if (testForFullRelationReferenceWithLaterWords(pAc, decText, lcExpList, 1)) {
											this.partialStartWord = true;
											reportMicroDebug("Found multiple word relation reference at: " + decText, pAc);
											addReferredExactRelation(pAc, tgtProp.formattedFullPropertyName(), tgtProp);
										}
									}
								}
							} else {
								//TODO: Do this properly
								if (propFullName.equals("special:inheritance")) {
									reportMicroDebug("I found the special inheritance relationship", pAc);
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean testForFullRelationReferenceWithLaterWords(ActionContext pAc, String pLcWordText, ArrayList<String> pExpList, int pDepth) {
		boolean result = false;
		int depth = pDepth;
		ProcessedWord nextWord = getNextProcessedWord();

		if (nextWord != null) {
			String concatText = pLcWordText + " " + nextWord.getDeclutteredText();

			if (pExpList.contains(concatText)) {
				reportMicroDebug("Matched '" + concatText + "' at depth " + pDepth, pAc);

				nextWord.markWordsAsRelationReferenceMatchedToDepth(pDepth);
				result = true;
			} else {
				result = nextWord.testForFullRelationReferenceWithLaterWords(pAc, concatText, pExpList, ++depth);
			}
		} else {
			result = false;
		}

		return result;
	}

	private boolean testForFullConceptReferenceWithLaterWords(ActionContext pAc, String pLcWordText, ArrayList<String> pExpList, int pDepth) {
		boolean result = false;
		int depth = pDepth;
		ProcessedWord nextWord = getNextProcessedWord();

		if (nextWord != null) {
			String concatText = pLcWordText + " " + nextWord.getDeclutteredText();

			if (pExpList.contains(concatText)) {
				reportMicroDebug("Matched '" + concatText + "' at depth " + pDepth, pAc);

				nextWord.markWordsAsConceptReferenceMatchedToDepth(pDepth);
				result = true;
			} else {
				result = nextWord.testForFullConceptReferenceWithLaterWords(pAc, concatText, pExpList, ++depth);
			}
		} else {
			result = false;
		}

		return result;
	}

	private boolean testForFullInstanceReferenceWithLaterWords(ActionContext pAc, String pLcWordText, ArrayList<String> pExpList, int pDepth) {
		boolean result = false;
		int depth = pDepth;
		ProcessedWord nextWord = getNextProcessedWord();

		if (nextWord != null) {
			String concatText = pLcWordText + " " + nextWord.getDeclutteredText();

			if (pExpList.contains(concatText)) {
				reportMicroDebug("Matched '" + concatText + "' at depth " + pDepth, pAc);

				nextWord.markWordsAsInstanceReferenceMatchedToDepth(pDepth);
				result = true;
			} else {
				result = nextWord.testForFullInstanceReferenceWithLaterWords(pAc, concatText, pExpList, ++depth);
			}
		} else {
			result = false;
		}

		return result;
	}

	public void markWordsAsRelationReferenceMatchedToDepth(int pDepth) {
		if (pDepth > 0) {
			int depth = pDepth;
			this.partialRelationReference = true;
			ProcessedWord prevWord = getPreviousProcessedWord();

			if (prevWord != null) {
				prevWord.markWordsAsRelationReferenceMatchedToDepth(--depth);
			}
		}
	}

	public void markWordsAsConceptReferenceMatchedToDepth(int pDepth) {
		if (pDepth > 0) {
			int depth = pDepth;
			this.partialConceptReference = true;
			ProcessedWord prevWord = getPreviousProcessedWord();

			if (prevWord != null) {
				prevWord.markWordsAsConceptReferenceMatchedToDepth(--depth);
			}
		}
	}

	public void markWordsAsInstanceReferenceMatchedToDepth(int pDepth) {
		if (pDepth > 0) {
			int depth = pDepth;
			this.partialInstanceReference = true;
			ProcessedWord prevWord = getPreviousProcessedWord();

			if (prevWord != null) {
				prevWord.markWordsAsInstanceReferenceMatchedToDepth(--depth);
			}
		}
	}

	public void markWordAsValue() {
		this.isValueWord = true;
	}

	public void markWordAsNumber() {
		this.isNumberWord = true;
	}

	private void checkForReferringInstances(ActionContext pAc) {
		checkForPluralMatchingInstance(pAc, getDeclutteredText());

		for (CeInstance thisInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, CON_LINGTHING)) {
			if (!thisInst.isMetaModelInstance()) {
				checkForInstanceByExpressedBy(pAc, thisInst);
			}
		}
	}

	private void addReferredExactConcept(ActionContext pAc, String pConName, CeConcept pTgtCon) {
		if (pTgtCon != null) {
			if (this.referredExactConcepts == null) {
				this.referredExactConcepts = new TreeMap<String, CeConcept>();
			}

			this.referredExactConcepts.put(pConName, pTgtCon);
		} else {
			reportError("Unable to add exact concept '" + pConName + "' as it could not be located", pAc);
		}
	}

	public Collection<CeConcept> listReferredExactConcepts() {
		Collection<CeConcept> result = null;

		if (this.referredExactConcepts != null) {
			result = this.referredExactConcepts.values();
		} else {
			result = new ArrayList<CeConcept>();
		}

		return result;
	}

	private void addReferredExactConceptPlural(ActionContext pAc, String pConName, CeConcept pTgtCon) {
		if (pTgtCon != null) {
			if (this.referredExactConceptsPlural == null) {
				this.referredExactConceptsPlural = new TreeMap<String, CeConcept>();
			}

			this.referredExactConceptsPlural.put(pConName, pTgtCon);
		} else {
			reportError("Unable to add exact concept plural '" + pConName + "' as it could not be located", pAc);
		}
	}

	public Collection<CeConcept> listReferredExactConceptsPlural() {
		Collection<CeConcept> result = null;

		if (this.referredExactConceptsPlural != null) {
			result = this.referredExactConceptsPlural.values();
		} else {
			result = new ArrayList<CeConcept>();
		}

		return result;
	}

	private void addReferredExactInstancesPlural(ActionContext pAc, String pInstName, CeInstance pTgtInst) {
		if (pTgtInst != null) {
			if (this.referredExactInstancesPlural == null) {
				this.referredExactInstancesPlural = new TreeMap<String, CeInstance>();
			}

			this.referredExactInstancesPlural.put(pInstName, pTgtInst);
		} else {
			reportError("Unable to add exact instance plural '" + pInstName + "' as it could not be located", pAc);
		}
	}

	public Collection<CeInstance> listReferredExactInstancesPlural() {
		Collection<CeInstance> result = null;

		if (this.referredExactInstancesPlural != null) {
			result = this.referredExactInstancesPlural.values();
		} else {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	private void addReferredExactRelation(ActionContext pAc, String pPropFullName, CeProperty pTgtProp) {
		if (pTgtProp != null) {
			if (this.referredExactRelations == null) {
				this.referredExactRelations = new TreeMap<String, CeProperty>();
			}

			this.referredExactRelations.put(pPropFullName, pTgtProp);
		} else {
			reportError("Unable to add exact relation '" + pPropFullName + "' as it could not be located", pAc);
		}
	}

	public Collection<CeProperty> listReferredExactRelations() {
		Collection<CeProperty> result = null;

		if (this.referredExactRelations != null) {
			result = this.referredExactRelations.values();
		} else {
			result = new ArrayList<CeProperty>();
		}

		return result;
	}

	private void checkForStandardWords(ArrayList<CeInstance> pComWordLists, ArrayList<CeInstance> pNegWordLists) {
		String myText = getDeclutteredText();

		for (CeInstance thisCwl : pComWordLists) {
			for (String thisCw : thisCwl.getValueListFromPropertyNamed(PROP_COMMWORD)) {
				String cwText = thisCw.trim().toLowerCase();

				if (myText.equals(cwText)) {
					this.isStandardWord = true;
					break;
				}
			}
		}

		for (CeInstance thisNwl : pNegWordLists) {
			for (String thisNw : thisNwl.getValueListFromPropertyNamed(PROP_NEGWORD)) {
				String nwText = thisNw.trim().toLowerCase();

				if (myText.equals(nwText)) {
					this.isNegationWord = true;
					break;
				}
			}
		}
	}

	private void checkForPartialMatchingConcepts(ActionContext pAc, String pLcWordText) {
		CeConcept result = checkForPartialMatchingConcept(pAc, pLcWordText, true);

		if (result != null) {
			if (result != this.matchingConcept) {
				reportMicroDebug("Partial matched concept (" + result.getConceptName() + ") found for " + getWordText(), pAc);
				this.partialStartWord = true;
				addReferredExactConcept(pAc, result.getConceptName(), result);
			}
		}
	}

	private CeConcept checkForPartialMatchingConcept(ActionContext pAc, String pLcWordText, boolean pFirstTime) {
		CeConcept result = null;
		String strippedWord = stripDelimitingQuotesFrom(pLcWordText);

		result = tryForPartialConceptMatchUsing(pAc, strippedWord, pFirstTime);

		if ((result == null) && (strippedWord.endsWith("s"))) {
			//Strip the trailing "s" and try again (in case it was pluralised)
			strippedWord = strippedWord.substring(0, (strippedWord.length() - 1));
			result = tryForPartialConceptMatchUsing(pAc, strippedWord, pFirstTime);
		}

		return result;
	}

	private CeConcept tryForPartialConceptMatchUsing(ActionContext pAc, String pPossibleName, boolean pFirstTime) {
		CeConcept result = null;

		if (pAc.getModelBuilder().isThereAConceptNameStartingButNotExactly(pAc, pPossibleName)) {
			ProcessedWord nextWord = getNextProcessedWord();

			reportMicroDebug("Partial match for concept '" + pPossibleName + "'", pAc);

			if (nextWord != null) {
				String concatLcText = pPossibleName + " " + getNextProcessedWord().getDeclutteredText();
				result = nextWord.checkForPartialMatchingConcept(pAc, concatLcText, false);
			}

			if (result != null) {
				nextWord.partialConceptReference = true;
			}
		} else {
			result = pAc.getModelBuilder().getConceptNamed(pAc, pPossibleName);

			if (!pFirstTime && (result == null)) {
				reportMicroDebug("No further partial match for concept '" + pPossibleName + "'", pAc);
			}
		}

		//If the test failed retry with just the passed name
		if (result == null) {
			result = pAc.getModelBuilder().getConceptNamed(pAc, pPossibleName);
		}

		return result;
	}

	private boolean alreadyMatchesRelation(CeProperty pProp) {
		boolean result = false;

		if (matchesToRelations()) {
			result = this.matchingRelations.containsValue(pProp);
		} else {
			result = false;
		}

		return result;
	}

	private void checkForPartialMatchingRelations(ActionContext pAc, String pLcWordText) {
		ArrayList<CeProperty> result = checkForPartialMatchingRelation(pAc, pLcWordText, true);

		if (result != null) {
			for (CeProperty thisProp : result) {
				if (!alreadyMatchesRelation(thisProp)) {
					reportMicroDebug("Partial matched relation (" + thisProp.getPropertyName() + ") found for " + getWordText(), pAc);
					this.partialStartWord = true;
					addReferredExactRelation(pAc, thisProp.formattedFullPropertyName(), thisProp);
				}
			}
		}
	}

	private ArrayList<CeProperty> checkForPartialMatchingRelation(ActionContext pAc, String pLcWordText, boolean pFirstTime) {
		ArrayList<CeProperty> result = null;

		if (pAc.getModelBuilder().isThereADefinedPropertyNameStartingButNotExactly(pLcWordText)) {
			ProcessedWord nextWord = getNextProcessedWord();

			reportMicroDebug("Partial match for relation '" + pLcWordText + "'", pAc);

			if (nextWord != null) {
				String concatLcText = pLcWordText + " " + getNextProcessedWord().getDeclutteredText();
				result = nextWord.checkForPartialMatchingRelation(pAc, concatLcText, false);
			}
		} else {
			result = pAc.getModelBuilder().getPropertiesNamed(pLcWordText);

			if (!pFirstTime && (result == null)) {
				reportMicroDebug("No further partial match for property '" + pLcWordText + "'", pAc);
			}
		}

		//If the test failed retry with just the passed name
		if ((result == null) || (result.isEmpty())) {
			result = pAc.getModelBuilder().getPropertiesNamed(pLcWordText);
		}

		if ((result != null) && (!result.isEmpty()) && (result.size() > 0)) {
			this.partialRelationReference = true;
		}

		return result;
	}

	private void addReferredExactInstance(CeInstance pInst) {
		if (this.referredExactInstances == null) {
			this.referredExactInstances = new TreeMap<String, CeInstance>();
		}

		this.referredExactInstances.put(pInst.getInstanceName(), pInst);
	}

	public Collection<CeInstance> listReferredExactInstances() {
		Collection<CeInstance> result = null;

		if (this.referredExactInstances != null) {
			result = this.referredExactInstances.values();
		} else {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	private void checkForPartialMatchingInstances(ActionContext pAc, String pLcWordText) {
		CeInstance result = checkForPartialMatchingInstance(pAc, pLcWordText, true);

		if (result != null) {
			if (result != this.matchingInstance) {
				reportMicroDebug("Partial matched instance (" + result.getInstanceName() + ") found for " + getWordText(), pAc);
				this.partialInstanceReference = true;
				this.partialStartWord = true;
				addReferredExactInstance(result);
			}
		}
	}

	private CeInstance checkForPartialMatchingInstance(ActionContext pAc, String pLcWordText, boolean pFirstTime) {
		CeInstance result = null;
		String strippedWord = stripDelimitingQuotesFrom(pLcWordText);

		ArrayList<CeInstance> possInsts = pAc.getIndexedEntityAccessor().calculateInstancesWithNameStarting(pAc, strippedWord + " ");

		if (!possInsts.isEmpty()) {
			ProcessedWord nextWord = getNextProcessedWord();

			reportMicroDebug("Partial match for instance '" + strippedWord + "'", pAc);

			if (nextWord != null) {
				String concatLcText = pLcWordText + " " + getNextProcessedWord().getDeclutteredText();
				result = nextWord.checkForPartialMatchingInstance(pAc, concatLcText, false);

				if (result != null) {
					nextWord.partialInstanceReference = true;
					nextWord.partialStartWord = false;
				}
			}
		} else {
			result = pAc.getIndexedEntityAccessor().getInstanceNamedOrIdentifiedAs(pAc, strippedWord);

			if (!pFirstTime && (result == null)) {
				reportMicroDebug("No further partial match for instance '" + strippedWord + "'", pAc);
			}
		}

		//If it turns out the match was in the meta-model then remove it
		if (!isValidMatchingInstance(result)) {
			result = null;
		}

		return result;
	}

	private void checkForPluralMatchingInstance(ActionContext pAc, String pLcWordText) {
		String strippedWord = stripDelimitingQuotesFrom(pLcWordText);

		//First check for the simple "extra s" case
		if (strippedWord.endsWith("s")) {
			String singForm = strippedWord.substring(0, (strippedWord.length() - 1));
			CeInstance matchedInst = pAc.getModelBuilder().getInstanceNamed(pAc, singForm);

			if (matchedInst != null) {
				addReferredExactInstancesPlural(pAc, singForm, matchedInst);
			}
		}

		//Next check for any explicit "plural form" statements
		for (CeInstance lingInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, CON_LINGTHING)) {
			for (String synonym : lingInst.getValueListFromPropertyNamed(PROP_PLURAL)) {
				if (synonym.equals(strippedWord)) {
					addReferredExactInstancesPlural(pAc, strippedWord, lingInst);
				}
			}
		}

	}

	public ProcessedWord getNextProcessedWord() {
		ProcessedWord result = null;
		ConvWord nextConvWord = this.convWord.getNextWord();

		if (nextConvWord != null) {
			result = nextConvWord.getProcessedWord();
		}

		return result;
	}

	public ProcessedWord getPreviousProcessedWord() {
		ProcessedWord result = null;
		ConvWord prevConvWord = this.convWord.getPreviousWord();

		if (prevConvWord != null) {
			result = prevConvWord.getProcessedWord();
		}

		return result;
	}

	public boolean isGrounded() {
		return
			isGroundedOnConcept() ||
			isGroundedOnProperty() ||
			isGroundedOnInstance();
	}

	public boolean isValidSubjectWord(ActionContext pAc) {
		boolean result = false;

		//Exclude any words that only have attributional thing instances linked
		if (isGroundedOnInstance()) {
			for (CeInstance thisInst : listGroundedInstances()) {
				if (thisInst.getDirectConcepts().length > 1) {
					result = true;
				} else {
					//TODO: Remove this hardcoded name
					if (!thisInst.isConceptNamed(pAc, "attributional thing")) {
						result = true;
					}
				}
			}
		} else {
			result = true;
		}

		return result;
	}

	public boolean isGroundedOnConcept() {
		return
				matchesToConcept() ||
				refersToConceptsExactly() ||
				refersToPluralConceptsExactly() ||
				this.partialConceptReference;
	}

	public boolean isGroundedOnProperty() {
		return
			matchesToRelations() ||
			refersToRelationsExactly() ||
			this.partialRelationReference;
	}

	public boolean isGroundedOnInstance() {
		return
			(matchesToInstance() && !isLaterPartOfPartial()) ||
			refersToInstancesExactly() ||
			refersToPluralInstancesExactly() ||
			(this.partialInstanceReference && this.partialStartWord);
	}

	public ArrayList<CeConcept> listGroundedConcepts() {
		ArrayList<CeConcept> result = null;

		if (this.partialConceptReference) {
			result = getPreviousProcessedWord().listGroundedConcepts();
		} else {
			HashSet<CeConcept> set = new HashSet<CeConcept>();

			if (this.matchingConcept != null) {
				set.add(this.matchingConcept);
			}

			if (refersToConceptsExactly()) {
				set.addAll(this.referredExactConcepts.values());
			}

			if (refersToPluralConceptsExactly()) {
				set.addAll(this.referredExactConceptsPlural.values());
			}

			result = winnowConcepts(set);
		}

		return result;
	}

	public ArrayList<CeConcept> listGroundedConceptsNoPlural() {
		ArrayList<CeConcept> result = null;

		if (this.partialConceptReference) {
			result = getPreviousProcessedWord().listGroundedConcepts();
		} else {
			HashSet<CeConcept> set = new HashSet<CeConcept>();

			if (this.matchingConcept != null) {
				set.add(this.matchingConcept);
			}

			if (refersToConceptsExactly()) {
				set.addAll(this.referredExactConcepts.values());
			}

			result = winnowConcepts(set);
		}

		return result;
	}

	public ArrayList<CeProperty> listGroundedProperties() {
		HashSet<CeProperty> result = new HashSet<CeProperty>();

		if (this.matchingRelations != null) {
			result.addAll(this.matchingRelations.values());
		}

		if (refersToRelationsExactly()) {
			result.addAll(this.referredExactRelations.values());
		}

		return winnowProperties(result);
	}

	public ArrayList<CeInstance> listGroundedInstances() {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		if (this.partialConceptReference) {
			result = getPreviousProcessedWord().listGroundedInstances();
		} else {
			HashSet<CeInstance> set = new HashSet<CeInstance>();

			if (!this.partialInstanceReference) {
				if (this.matchingInstance != null) {
					set.add(this.matchingInstance);
				}
			}

			if (refersToInstancesExactly()) {
				set.addAll(this.referredExactInstances.values());
			}

			if (refersToPluralInstancesExactly()) {
				set.addAll(this.referredExactInstancesPlural.values());
			}

			result = winnowInstances(set);
		}

		return result;
	}

	private static ArrayList<CeConcept> winnowConcepts(HashSet<CeConcept> pCons) {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

		for (CeConcept thisCon : pCons) {
			if (!hasChildConceptIn(thisCon, pCons)) {
				result.add(thisCon);
			}
		}

		return result;
	}

	private static ArrayList<CeProperty> winnowProperties(HashSet<CeProperty> pProps) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		//TODO: Implement this
		for (CeProperty thisProp : pProps) {
			result.add(thisProp);
		}

		return result;
	}

	private static ArrayList<CeInstance> winnowInstances(HashSet<CeInstance> pInsts) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		//Instances cannot be winnowed
		for (CeInstance thisInst : pInsts) {
			result.add(thisInst);
		}

		return result;
	}

	private static boolean hasChildConceptIn(CeConcept tgtCon, HashSet<CeConcept> pCandidates) {
		for (CeConcept thisCand : pCandidates) {
			if (tgtCon.isParentOf(thisCand)) {
				return true;
			}
		}

		return false;
	}

	public void addConnectedWordsTo(ExtractedItem pExtItem) {
		if (this.partialStartWord) {
			ProcessedWord nw = getNextProcessedWord();

			if ((nw != null) && (nw.partialConceptReference)) {
				pExtItem.addOtherWord(nw);
				nw.addConnectedWordsTo(pExtItem);
			}

			if ((nw != null) && (nw.partialRelationReference)) {
				pExtItem.addOtherWord(nw);
				nw.addConnectedWordsTo(pExtItem);
			}

			if ((nw != null) && (nw.partialInstanceReference)) {
				pExtItem.addOtherWord(nw);
				nw.addConnectedWordsTo(pExtItem);
			}
		}
	}

	public ArrayList<ProcessedWord> getFollowingUnprocessedWords() {
		ArrayList<ProcessedWord> result = new ArrayList<ProcessedWord>();

		ProcessedWord nw = getNextProcessedWord();

		if (nw != null) {
			if (nw.isUnmatchedWord()) {
				result.add(nw);
				result.addAll(nw.getFollowingUnprocessedWords());
			}
		}

		return result;
	}

	@Override
	public String toString() {
		String result = getWordText();

		if (matchesToConcept()) {
			result += " (corresponds to concept: " + this.matchingConcept.getConceptName() + ")";
		}

		if (matchesToRelations()) {
			for (CeProperty thisProp : this.matchingRelations.values()) {
				result += " (corresponds to relation: " + thisProp.formattedFullPropertyName() + ")";
			}
		}

		if (matchesToInstance()) {
			result += " (corresponds to instance of: " + this.matchingInstance.formattedDirectConceptNames() + ")";
		}

		if (refersToConceptsExactly()) {
			String refConText = "";
			for (CeConcept refCon : this.referredExactConcepts.values()) {
				if (!refConText.isEmpty()) {
					refConText += ", ";
				}
				refConText += refCon.getConceptName();
			}

			refConText = " (refers to concept: " + refConText + ")";
			result += refConText;
		}

		if (refersToPluralConceptsExactly()) {
			String refConText = "";
			for (CeConcept refCon : this.referredExactConceptsPlural.values()) {
				if (!refConText.isEmpty()) {
					refConText += ", ";
				}
				refConText += refCon.getConceptName();
			}

			refConText = " (refers to concept plural: " + refConText + ")";
			result += refConText;
		}

		if (refersToRelationsExactly()) {
			String refPropText = "";
			for (CeProperty refProp : this.referredExactRelations.values()) {
				if (!refPropText.isEmpty()) {
					refPropText += ", ";
				}
				refPropText += refProp.getPropertyName();
			}

			refPropText = " (refers to relation: " + refPropText + ")";
			result += refPropText;
		}

		if (refersToInstancesExactly()) {
			String refInstText = "";
			for (CeInstance refInst : this.referredExactInstances.values()) {
				if (!refInstText.isEmpty()) {
					refInstText += ", ";
				}
				refInstText += refInst.getInstanceName();
			}

			refInstText = " (refers to instance: " + refInstText + ")";
			result += refInstText;
		}

		if (refersToPluralInstancesExactly()) {
			String refInstText = "";
			for (CeInstance refInst : this.referredExactInstancesPlural.values()) {
				if (!refInstText.isEmpty()) {
					refInstText += ", ";
				}
				refInstText += refInst.getInstanceName();
			}

			refInstText = " (refers to instance plural: " + refInstText + ")";
			result += refInstText;
		}

		if (this.isQuestionWord) {
			result += " (question word)";
		}

		if (this.isStandardWord) {
			result += " (standard word)";
		}

		if (this.isNegationWord) {
			result += " (negation word)";
		}

		if (this.isValueWord) {
			result += " (value word)";
		}

		if (this.isNumberWord) {
			result += " (number word)";
		}

		if (this.partialConceptReference) {
			result += " (partial concept reference)";
		}

		if (this.partialRelationReference) {
			result += " (partial relation reference)";
		}

		return result;
	}

}