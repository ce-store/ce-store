package com.ibm.ets.ita.ce.store.conversation.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.stripDelimitingQuotesFrom;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.hudson.handler.GenericHandler;
import com.ibm.ets.ita.ce.store.hudson.helper.ConvConfig;
import com.ibm.ets.ita.ce.store.hudson.helper.HudsonManager;
import com.ibm.ets.ita.ce.store.hudson.helper.WordCheckerCache;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class ProcessedWord extends GeneralItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CON_ENTCON = "entity concept";
	private static final String CON_PROPCON = "property concept";
	private static final String CON_PROWORD = "processed word";
	private static final String CON_UNMWORD = "unmatched word";
	private static final String PROP_PLURAL = "plural form";
	private static final String PROP_PAST = "past tense";
	private static final String PROP_EXPBY = "is expressed by";
	private static final String DET_A = "a";
	private static final String DET_AN = "an";
	private static final String DET_THE = "the";
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

	private ConvWord convWord = null;
	private ArrayList<ExtractedItem> extractedItems = null;
	private String lcText = null;
	private int wordPos = -1;

	// Matched (directly, by the name)
	private CeConcept matchingConcept = null;
	private TreeMap<String, CeProperty> matchingRelations = null;
	private ArrayList<CeInstance> matchingInstances = null;

	// Referred (indirectly
	private TreeMap<String, CeConcept> referredExactConcepts = null;
	private TreeMap<String, CeProperty> referredExactRelations = null;
	private TreeMap<String, CeInstance> referredExactInstances = null;
	private TreeMap<String, CeConcept> referredExactConceptsPlural = null;
	private TreeMap<String, CeConcept> referredExactConceptsPastTense = null;
	private TreeMap<String, CeInstance> referredExactInstancesPlural = null;

	// Types of words
	private boolean isStandardWord = false;
	private boolean isNegationWord = false;
	private boolean isValueWord = false;
	private boolean isNumberWord = false;
	private boolean isQuestionWord = false;

	private boolean partialStartWord = false;
	private boolean partialConceptReference = false;
	private boolean partialRelationReference = false;
	private boolean partialInstanceReference = false;

	private CeInstance chosenInstance = null;

	private ProcessedWord(ConvWord pConvWord, int pWordPos) {
		this.id = pConvWord.getId();
		this.convWord = pConvWord;
		this.wordPos = pWordPos;

		this.lcText = declutter(pConvWord.getWordText().toLowerCase());

		try {
			// Attempt to create a double
			Double.parseDouble(getDeclutteredText());
			this.isNumberWord = true;
		} catch (NumberFormatException e) {
			// There was a number format exception so this is not a number word
			this.isNumberWord = false;
		}
	}

	public int getWordPos() {
		return this.wordPos;
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

	public static ProcessedWord createFrom(ConvWord pConvWord, int pWordPos) {
		ProcessedWord newGw = new ProcessedWord(pConvWord, pWordPos);

		pConvWord.setProcessedWord(newGw);

		return newGw;
	}

	public ConvWord getConvWord() {
		return this.convWord;
	}

	public String getWordText() {
		return this.convWord.getWordText();
	}

	public String getWholePhraseText() {
		String result = null;

		result = getWordText();

		if (this.partialStartWord) {
			ProcessedWord nextWord = getNextProcessedWord();

			if (nextWord != null) {
				result += nextWord.getFollowingPartialText();
			}
		}

		return result;
	}

	public String getFollowingPartialText() {
		String result = null;

		if (this.isLaterPartOfPartial()) {
			result = " " + getWordText();

			ProcessedWord nextWord = getNextProcessedWord();

			if (nextWord != null) {
				result += nextWord.getFollowingPartialText();
			}
		} else {
			result = "";
		}

		return result;
	}

	public CeConcept getMatchingConcept() {
		return this.matchingConcept;
	}

	public void setMatchingConcept(CeConcept pCon) {
		this.matchingConcept = pCon;
	}

	public ArrayList<CeInstance> getMatchingInstances() {
		return this.matchingInstances;
	}

	public CeInstance getFirstMatchingInstance() {
		CeInstance result = null;

		if (this.matchingInstances != null) {
			if (!this.matchingInstances.isEmpty()) {
				result = this.matchingInstances.get(0);
			}
		}

		return result;
	}

	public void setMatchingInstances(ArrayList<CeInstance> pInsts) {
		this.matchingInstances = pInsts;
	}

	public boolean matchesToConcept() {
		return this.matchingConcept != null;
	}

	public boolean matchesToRelations() {
		return (this.matchingRelations != null) && !this.matchingRelations.isEmpty();
	}

	public boolean matchesToInstance() {
		return this.matchingInstances != null;
	}

	public boolean refersToConceptsExactly() {
		return (this.referredExactConcepts != null) && !this.referredExactConcepts.isEmpty();
	}

	public boolean refersToInstanceOfConceptNamed(ActionContext pAc, String pConName) {
		boolean result = false;

		if (this.matchingInstances != null) {
			for (CeInstance thisInst : this.matchingInstances) {
				if (thisInst.isConceptNamed(pAc, pConName)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	public boolean refersToPluralConceptsExactly() {
		return (this.referredExactConceptsPlural != null) && !this.referredExactConceptsPlural.isEmpty();
	}

	public boolean refersToPastTenseConceptsExactly() {
		return (this.referredExactConceptsPastTense != null) && !this.referredExactConceptsPastTense.isEmpty();
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

	public boolean isQuestionWord() {
		return this.isQuestionWord;
	}

	public void markAsQuestionWord() {
		this.isQuestionWord = true;
	}

	public boolean isDeterminer() {
		String decText = getDeclutteredText();

		// TODO: This should not be hardcoded
		return (decText.equals(DET_A)) || (decText.equals(DET_AN)) || (decText.equals(DET_THE));
	}

	public boolean isUnmatchedWord() {

		return (this.matchingConcept == null) && !matchesToRelations() && (this.matchingInstances == null)
				&& !refersToConceptsExactly() && !refersToPluralConceptsExactly() && !refersToRelationsExactly()
				&& !refersToInstancesExactly() && !refersToPluralInstancesExactly() && (!this.isStandardWord)
				&& (!this.isNegationWord) && (!this.isValueWord) && (!this.isNumberWord)
				&& (!this.partialConceptReference) && (!this.partialRelationReference)
				&& (!this.partialInstanceReference) && (!isDeterminer()) && (!isQuestionWord());
	}

	public boolean isLaterPartOfPartial() {
		return (this.partialConceptReference || this.partialRelationReference || this.partialInstanceReference)
				&& !this.partialStartWord;
	}

	public String getDeclutteredText() {
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

		// DSB 21/09/2015 - Simply remove apostrophes from the text.
		// TODO: Is the declutter phase the best place for this?
		if (result.contains("'")) {
			result = result.replaceAll("'", "");
		}

		if (result.contains("’")) {
			result = result.replaceAll("’", "");
		}

		// //DSB 21/09/2015 - Simply remove dashes from the text.
		// if (result.contains("-")) {
		// result = result.replaceAll("-", " ");
		// }

		// //TODO: Is the declutter phase the best place for this?
		// if (result.endsWith("'s")) {
		// //DSB 21/09/2015 - Added trailing "s" to the word (so the apostrophe
		// is simply removed)
		// result = result.substring(0, result.length() - 2) + "s";
		// }
		//
		// //TODO: Is the declutter phase the best place for this?
		// if (result.endsWith("’s")) {
		// //DSB 21/09/2015 - Added trailing "s" to the word (so the apostrophe
		// is simply removed)
		// result = result.substring(0, result.length() - 2) + "s";
		// }

		return result;
	}

	public void classify(ActionContext pAc, ConvConfig pCc) {
		WordCheckerCache wcc = ServletStateManager.getHudsonManager(pAc).getWordCheckerCache();

		wcc.checkForMatchingConcept(pAc, this);
		wcc.checkForMatchingRelation(pAc, this);
		wcc.checkForMatchingInstances(pAc, this);

		checkForReferringConcepts(pAc);
		checkForReferringRelations(pAc);
		checkForReferringInstances(pAc, wcc);

		checkForStandardWords(pCc, wcc, pAc);

		String decText = getDeclutteredText();

		checkForPartialMatchingConcepts(pAc, decText);
		checkForPartialMatchingRelations(pAc, decText);
		checkForPartialMatchingInstances(pAc, decText);
	}

	private void checkForReferringConcepts(ActionContext pAc) {
		for (CeInstance thisInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, CON_ENTCON)) {
			checkForConceptByExpressedBy(pAc, thisInst);
			checkForConceptByPluralForm(pAc, thisInst);
			checkForConceptByPastTense(pAc, thisInst);
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
					// This is an exact match so just save the referenced
					// instance
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

	private void checkForConceptByPastTense(ActionContext pAc, CeInstance pEntConInst) {
		String conName = pEntConInst.getInstanceName();
		String decText = getDeclutteredText();

		ArrayList<String> expList = pEntConInst.getValueListFromPropertyNamed(PROP_PAST);
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
						addReferredExactConceptPastTense(pAc, conName, tgtCon);
					}
				} else {
					if (testForFullConceptReferenceWithLaterWords(pAc, decText, lcExpList, 1)) {
						reportMicroDebug("Found multiple word concept reference at: " + decText, pAc);
						this.partialStartWord = true;
						this.addReferredExactConceptPastTense(pAc, conName, tgtCon);
					}
				}
			}
		}
	}

	public void setMatchingRelations(TreeMap<String, CeProperty> pProps) {
		this.matchingRelations = pProps;
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
											reportMicroDebug("Found multiple word relation reference at: " + decText,
													pAc);
											addReferredExactRelation(pAc, tgtProp.formattedFullPropertyName(), tgtProp);
										}
									}
								}
							} else {
								// TODO: Do this properly
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

	private boolean testForFullRelationReferenceWithLaterWords(ActionContext pAc, String pLcWordText,
			ArrayList<String> pExpList, int pDepth) {
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

	private boolean testForFullConceptReferenceWithLaterWords(ActionContext pAc, String pLcWordText,
			ArrayList<String> pExpList, int pDepth) {
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

	private boolean testForFullInstanceReferenceWithLaterWords(ActionContext pAc, String pLcWordText,
			ArrayList<String> pExpList, int pDepth) {
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

	private void checkForReferringInstances(ActionContext pAc, WordCheckerCache pWcc) {
		checkForPluralMatchingInstance(pAc, getDeclutteredText(), pWcc);

		for (CeInstance thisInst : pWcc.getLingThingInstances(pAc)) {
			checkForInstanceByExpressedBy(pAc, thisInst);
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

	// public void setReferredExactConcepts(TreeMap<String, CeConcept> pCons) {
	// this.referredExactConcepts = pCons;
	// }

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

	private void addReferredExactConceptPastTense(ActionContext pAc, String pConName, CeConcept pTgtCon) {
		if (pTgtCon != null) {
			if (this.referredExactConceptsPastTense == null) {
				this.referredExactConceptsPastTense = new TreeMap<String, CeConcept>();
			}

			this.referredExactConceptsPastTense.put(pConName, pTgtCon);
		} else {
			reportError("Unable to add exact concept past tense '" + pConName + "' as it could not be located", pAc);
		}
	}

	public Collection<CeConcept> listReferredExactConceptsPastTense() {
		Collection<CeConcept> result = null;

		if (this.referredExactConceptsPastTense != null) {
			result = this.referredExactConceptsPastTense.values();
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

			// DSB 23/10/2015 - Metamodel instances should not be used
			if (!WordCheckerCache.isOnlyConfigCon(pAc, pTgtInst)) {
				this.referredExactInstancesPlural.put(pInstName, pTgtInst);
			}
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

	private void checkForStandardWords(ConvConfig pCc, WordCheckerCache pWcc, ActionContext pAc) {
		String myText = getDeclutteredText();

		ArrayList<String> cWords = pWcc.getCommonWords(pCc, pAc);
		ArrayList<String> nWords = pWcc.getNegationWords(pCc, pAc);

		if (cWords.contains(myText)) {
			this.isStandardWord = true;
		}

		if (nWords.contains(myText)) {
			this.isNegationWord = true;
		}
	}

	private void checkForPartialMatchingConcepts(ActionContext pAc, String pLcWordText) {
		CeConcept result = checkForPartialMatchingConcept(pAc, pLcWordText, true);

		if (result != null) {
			if (result != this.matchingConcept) {
				reportMicroDebug("Partial matched concept (" + result.getConceptName() + ") found for " + getWordText(),
						pAc);
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
			// Strip the trailing "s" and try again (in case it was pluralised)
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

		// If the test failed retry with just the passed name
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
					reportMicroDebug(
							"Partial matched relation (" + thisProp.getPropertyName() + ") found for " + getWordText(),
							pAc);
					this.partialStartWord = true;
					addReferredExactRelation(pAc, thisProp.formattedFullPropertyName(), thisProp);
				}
			}
		}
	}

	private ArrayList<CeProperty> checkForPartialMatchingRelation(ActionContext pAc, String pLcWordText,
			boolean pFirstTime) {
		ArrayList<CeProperty> result = null;

		if (pAc.getModelBuilder().isThereADefinedPropertyNameStartingButNotExactly(pLcWordText)) {
			ProcessedWord nextWord = getNextProcessedWord();

			reportMicroDebug("Partial match for relation '" + pLcWordText + "'", pAc);

			if (nextWord != null) {
				String concatLcText = pLcWordText + " " + getNextProcessedWord().getDeclutteredText();
				result = nextWord.checkForPartialMatchingRelation(pAc, concatLcText, false);
			}
		} else {
			if (!pFirstTime) {
				// DSB 23/10/2015 - Improved to only consider non-inferred
				// properties
				ArrayList<CeProperty> tempResult = pAc.getModelBuilder().getPropertiesNamed(pLcWordText);

				for (CeProperty thisProp : tempResult) {
					CeProperty rootProp = null;

					if (thisProp.isInferredProperty()) {
						rootProp = thisProp.getStatedSourceProperty();
					} else {
						rootProp = thisProp;
					}

					if (result == null) {
						result = new ArrayList<CeProperty>();
					}

					if (!result.contains(rootProp)) {
						result.add(rootProp);
					}
				}

				if (result == null) {
					reportMicroDebug("No further partial match for property '" + pLcWordText + "'", pAc);
				}
			}
		}

		if (!pFirstTime) {
			// If the test failed retry with just the passed name
			if ((result == null) || (result.isEmpty())) {
				result = pAc.getModelBuilder().getPropertiesNamed(pLcWordText);
			}
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
		ArrayList<CeInstance> result = checkForPartialMatchingInstanceList(pAc, pLcWordText, true);
		// boolean matched = false;

		if (result != null) {
			// DSB 21/10/2015 - Corrected very poor previous implementation
			// if (this.matchingInstances != null) {
			// for (CeInstance thisInst : this.matchingInstances) {
			// for (CeInstance resInst : result) {
			// if (resInst == thisInst) {
			// matched = true;
			// break;
			// }
			// }
			// }
			// }

			// if (!matched) {
			for (CeInstance thisInst : result) {
				if ((this.matchingInstances == null) || (!this.matchingInstances.contains(thisInst))) {
					reportMicroDebug(
							"Partial matched instance (" + thisInst.getInstanceName() + ") found for " + getWordText(),
							pAc);
					this.partialInstanceReference = true;
					this.partialStartWord = true;

					addReferredExactInstance(thisInst);
				}
			}
		}
	}
	// }

	private ArrayList<CeInstance> checkForPartialMatchingInstanceList(ActionContext pAc, String pLcWordText,
			boolean pFirstTime) {
		ArrayList<CeInstance> result = null;
		HudsonManager hm = ServletStateManager.getHudsonManager(pAc);
		String strippedWord = stripDelimitingQuotesFrom(pLcWordText);

		ArrayList<CeInstance> possInsts = hm.getIndexedEntityAccessor(pAc.getModelBuilder())
				.calculateInstancesWithNameStarting(pAc, strippedWord + " ");

		if (!possInsts.isEmpty()) {
			ProcessedWord nextWord = getNextProcessedWord();

			reportMicroDebug("Partial match for instance '" + strippedWord + "'", pAc);

			if (nextWord != null) {
				String concatLcText = pLcWordText + " " + getNextProcessedWord().getDeclutteredText();

				// Quick and dirty depluralisation (to stop the need to specify
				// lots of singular variants of the
				// word as synonyms. Won't work for complex plurals like
				// "people"...
				if (concatLcText.endsWith("s")) {
					ArrayList<String> variants = new ArrayList<String>();
					variants.add(concatLcText);
					variants.add(concatLcText.substring(0, (concatLcText.length() - 1)));

					result = new ArrayList<CeInstance>();

					for (String thisVariant : variants) {
						ArrayList<CeInstance> instList = nextWord.checkForPartialMatchingInstanceList(pAc, thisVariant,
								false);

						if (instList != null) {
							result.addAll(instList);
						}
					}
				} else {
					result = nextWord.checkForPartialMatchingInstanceList(pAc, concatLcText, false);
				}

				if ((result != null) && (!result.isEmpty())) {
					nextWord.partialInstanceReference = true;
					nextWord.partialStartWord = false;
				}
			}
		}

		if (result == null) {
			result = new ArrayList<CeInstance>();
		}

		// DSB 20/10/2015 - Moved this processing out so it is always done (even
		// when there
		// are partial matches
		ArrayList<CeInstance> extraResult = hm.getWordCheckerCache().checkForMatchingInstances(pAc, strippedWord);

		if (!extraResult.isEmpty()) {
			result.addAll(extraResult);
		}

		if (!pFirstTime && (result.isEmpty())) {
			reportMicroDebug("No further partial match for instance '" + strippedWord + "'", pAc);
		}

		return result;
	}

	private void checkForPluralMatchingInstance(ActionContext pAc, String pLcWordText, WordCheckerCache pWcc) {
		String strippedWord = stripDelimitingQuotesFrom(pLcWordText);

		// First check for the simple "extra s" case
		if (strippedWord.endsWith("s")) {
			String singForm = strippedWord.substring(0, (strippedWord.length() - 1));
			CeInstance matchedInst = pAc.getModelBuilder().getInstanceNamed(pAc, singForm);

			if (matchedInst != null) {
				addReferredExactInstancesPlural(pAc, singForm, matchedInst);
			}
		}

		for (CeInstance lingInst : pWcc.getLingThingPluralForms(pAc, strippedWord)) {
			addReferredExactInstancesPlural(pAc, strippedWord, lingInst);
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
		return isGroundedOnConcept() || isGroundedOnProperty() || isGroundedOnInstance();
	}

	public boolean isValidSubjectWord(ActionContext pAc) {
		boolean result = false;

		// Exclude any words that only have attributional thing instances linked
		if (isGroundedOnInstance()) {
			for (CeInstance thisInst : listGroundedInstances()) {
				if (thisInst.getDirectConcepts().length > 1) {
					result = true;
				} else {
					// TODO: Remove this hardcoded name
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
		return matchesToConcept() || refersToConceptsExactly() || refersToPluralConceptsExactly()
				|| this.partialConceptReference;
	}

	public boolean isGroundedOnProperty() {
		return matchesToRelations() || refersToRelationsExactly() || this.partialRelationReference;
	}

	public boolean isGroundedOnInstance() {
		return (matchesToInstance() && !isLaterPartOfPartial()) || refersToInstancesExactly()
				|| refersToPluralInstancesExactly() || (this.partialInstanceReference && this.partialStartWord);
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

			if (refersToPastTenseConceptsExactly()) {
				set.addAll(this.referredExactConceptsPastTense.values());
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
				if (this.matchingInstances != null) {
					set.addAll(this.matchingInstances);
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

		// TODO: Implement this
		for (CeProperty thisProp : pProps) {
			result.add(thisProp);
		}

		return result;
	}

	private static ArrayList<CeInstance> winnowInstances(HashSet<CeInstance> pInsts) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		// Instances cannot be winnowed
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

	public boolean isModifier(ActionContext pAc) {
		boolean result = false;

		for (CeInstance thisInst : listGroundedInstances()) {
			if (thisInst.isConceptNamed(pAc, GenericHandler.CON_MODIFIER)) {
				result = true;
				break;
			}
		}

		return result;
	}

	public boolean isSearchModifier(ActionContext pAc) {
		boolean result = false;

		for (CeInstance thisInst : listGroundedInstances()) {
			if (thisInst.isConceptNamed(pAc, GenericHandler.CON_SRCHMOD)) {
				result = true;
				break;
			}
		}

		return result;
	}

	public boolean isFilterModifier(ActionContext pAc) {
		boolean result = false;

		for (CeInstance thisInst : listGroundedInstances()) {
			if (thisInst.isConceptNamed(pAc, GenericHandler.CON_FILTMOD)) {
				result = true;
				break;
			}
		}

		return result;
	}

	public boolean isFunctionModifier(ActionContext pAc) {
		boolean result = false;

		for (CeInstance thisInst : listGroundedInstances()) {
			if (thisInst.isConceptNamed(pAc, GenericHandler.CON_FUNCMOD)) {
				result = true;
				break;
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
			boolean firstTime = true;
			result += " (corresponds to relation: ";

			for (CeProperty thisProp : this.matchingRelations.values()) {
				if (!firstTime) {
					result += ", ";
				}

				result += thisProp.formattedFullPropertyName();
				firstTime = false;
			}

			result += ")";
		}

		if (matchesToInstance()) {
			boolean firstTime = true;
			result += " (corresponds to instance of: ";

			for (CeInstance thisInst : this.matchingInstances) {
				if (!firstTime) {
					result += ", ";
				}

				result += thisInst.formattedDirectConceptNames();
				firstTime = false;
			}

			result += ")";
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

		if (refersToPastTenseConceptsExactly()) {
			String refConText = "";
			for (CeConcept refCon : this.referredExactConceptsPastTense.values()) {
				if (!refConText.isEmpty()) {
					refConText += ", ";
				}
				refConText += refCon.getConceptName();
			}

			refConText = " (refers to concept past tense: " + refConText + ")";
			result += refConText;
		}

		if (refersToRelationsExactly()) {
			String refPropText = "";
			for (CeProperty refProp : this.referredExactRelations.values()) {
				if (!refPropText.isEmpty()) {
					refPropText += ", ";
				}
				refPropText += refProp.formattedFullPropertyName();
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

		if (this.partialInstanceReference) {
			result += " (partial instance reference)";
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