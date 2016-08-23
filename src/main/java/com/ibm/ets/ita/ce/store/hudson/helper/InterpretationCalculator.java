package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.handler.GenericHandler;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class InterpretationCalculator {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String START_CON_WHAT = "What";
	private static final String START_CON_WHERE = "Where";
	private static final String START_CON_WHO = "Who";
	private static final String START_INST_WHAT = "What is";
	private static final String START_INST_WHERE = "Where is";
	private static final String START_INST_WHO = "Who is";

	ActionContext ac = null;
	ArrayList<ChosenWord> chosenWords = null;
	ArrayList<ProcessedWord> questionWords = null;
	Answer answer = null;
	ChosenWord tgtConWord = null;
	ChosenWord tgtInstWord = null;
	CeConcept tgtCon = null;
	CeInstance tgtInst = null;
	ArrayList<ChosenWord> qualifyingConWords = new ArrayList<ChosenWord>();

	public InterpretationCalculator(ActionContext pAc, ArrayList<ChosenWord> pChosWords, ArrayList<ProcessedWord> pQuesWords, Answer pAns) {
		this.ac = pAc;
		this.questionWords = pQuesWords;
		this.answer = pAns;

		this.chosenWords = new ArrayList<ChosenWord>();

		if (pChosWords != null) {
			//TODO: This is nasty - replace with a proper sort
			TreeMap<Integer, ArrayList<ChosenWord>> tempMap = new TreeMap<Integer, ArrayList<ChosenWord>>();

			for (ChosenWord thisCw : pChosWords) {
				Integer wordPos = new Integer(thisCw.originalWordPos());

				ArrayList<ChosenWord> tgtArray = tempMap.get(wordPos);

				if (tgtArray == null) {
					tgtArray = new ArrayList<ChosenWord>();
					tempMap.put(wordPos, tgtArray);
				}

				tgtArray.add(thisCw);
			}

			for (Integer thisKey : tempMap.keySet()) {
				ArrayList<ChosenWord> wordList = tempMap.get(thisKey);

				for (ChosenWord thisWord : wordList) {
					this.chosenWords.add(thisWord);
				}
			}
		}
	}

	public String computeInterpretation() {
		StringBuilder sb = new StringBuilder();

		if ((this.chosenWords != null) && (!this.chosenWords.isEmpty())) {
			findTargets();

			computeStartInterpretation(sb);

			if (isVerbSingular()) {
				computePropertyInterpretations(sb);
				computeConceptInterpretations(sb);
			} else {
				computeConceptInterpretations(sb);
				computePropertyInterpretations(sb);
			}

			computeGrouperInterpretations(sb);
			computeOrdererInterpretations(sb);
			computeEndInterpretation(sb);
		}

		return sb.toString();
	}

	private void computeStartInterpretation(StringBuilder pSb) {
		if (this.tgtCon != null) {
			if (isWhereAnswer()) {
				lookForQuestionWords(pSb, START_CON_WHERE);
			} else if (isWhoAnswer()) {
				lookForQuestionWords(pSb, START_CON_WHO);
			} else {
				lookForQuestionWords(pSb, START_CON_WHAT);
			}
		} else {
			if (isVerbSingular()) {
				if (isWhereAnswer()) {
					lookForQuestionWords(pSb, START_CON_WHERE);
				} else if (isWhoAnswer()) {
					lookForQuestionWords(pSb, START_CON_WHO);
				} else {
					lookForQuestionWords(pSb, START_CON_WHAT);
				}
			} else {
				if (isWhereAnswer()) {
					lookForQuestionWords(pSb, START_INST_WHERE);
				} else if (isWhoAnswer()) {
					lookForQuestionWords(pSb, START_INST_WHO);
				} else {
					lookForQuestionWords(pSb, START_INST_WHAT);
				}
			}
		}
	}

	private boolean isWhereAnswer() {
		boolean result = this.answer.isWhereAnswer();
		
		if (!result) {
			for (ProcessedWord thisWord : this.questionWords) {
				if (thisWord.isWhere()) {
					result = true;
				}
			}
		}

		return result;
	}
	
	private boolean isWhoAnswer() {
		boolean result = false;

		for (ProcessedWord thisWord : this.questionWords) {
			if (thisWord.isWho()) {
				result = true;
			}
		}

		return result;
	}

	private static void computeEndInterpretation(StringBuilder pSb) {
		pSb.append("?");
	}

	private void computeConceptInterpretations(StringBuilder pSb) {
		computeQualifiers(pSb);

		for (ChosenWord thisCw : this.chosenWords) {
			if (thisCw.isCeConcept()){
				if (this.answer.isCountAnswer()) {
					pSb.append(" " + thisCw.getConceptPluralName(this.ac));
				} else {
					pSb.append(" " + thisCw.getConceptName());
				}
			} else if (thisCw.isCeInstance()) {
				computeLocalConceptInterpretation(pSb, thisCw);
			} else if (thisCw.isEndModifier()) {
				pSb.append(" " + thisCw.getConceptName());
			}
		}
	}

	private void computeLocalConceptInterpretation(StringBuilder pSb, ChosenWord pCw) {
		pSb.append(" ");
		pSb.append(pCw.interpretationText(this.ac, pluraliseTerms()));
	}

	private boolean isVerbSingular() {
		boolean result = false;

		//Find the CE properties
		if (this.tgtInst != null) {
			for (ChosenWord thisCw : this.chosenWords) {
				if (thisCw.isCeProperty()) {
					if (thisCw.cePropertyAppliesTo(this.ac, this.tgtInst)) {
						result = thisCw.getCeProperty(this.ac).isVerbSingular();
					}
				}
			}
		}

		return result;
	}

	private void computePropertyInterpretations(StringBuilder pSb) {
		//Find the CE properties
		if (this.tgtInst != null) {
			for (ChosenWord thisCw : this.chosenWords) {
				if (thisCw.isCeProperty()) {
					if (thisCw.cePropertyAppliesTo(this.ac, this.tgtInst)) {
						pSb.append(" " + thisCw.cePropertyText());
					}
				}
			}
		}
	}

	private void computeGrouperInterpretations(StringBuilder pSb) {
		for (ChosenWord thisCw : this.chosenWords) {
			if (thisCw.isGrouper()) {
				pSb.append(" " + thisCw.referredInstanceText());
			}
		}
	}

	private void computeOrdererInterpretations(StringBuilder pSb) {
		for (ChosenWord thisCw : this.chosenWords) {
			if (thisCw.isOrderer()) {
				pSb.append(" " + thisCw.referredInstanceText());
			}
		}
	}

	private void findQualifiers() {
		CeInstance mmInst = this.tgtCon.retrieveMetaModelInstance(this.ac);

		if (mmInst != null) {
			for (String qualConName : mmInst.getValueListFromPropertyNamed(GenericHandler.PROP_IQB)) {

				if (qualConName != null) {
					CeConcept thisQualCon = this.ac.getModelBuilder().getConceptNamed(this.ac, qualConName);

					if (thisQualCon != null) {
						for (ChosenWord thisCw : this.chosenWords) {
							CeConcept thisCon = thisCw.getConcept(this.ac);

							if (thisCon != null) {
								if (thisCon.equals(thisQualCon)) {
									this.qualifyingConWords.add(thisCw);
								}
							}
						}
					}
				}
			}
		}
	}

	private void computeQualifiers(StringBuilder pSb) {
		if (this.tgtConWord != null) {
			String qualConText = "";

			for (ChosenWord thisQw : this.qualifyingConWords) {
				qualConText += " " + thisQw.interpretationText(this.ac, pluraliseTerms());
			}

			pSb.append(qualConText + " " + this.tgtConWord.interpretationText(this.ac, pluraliseTerms()));
		}
	}

	private boolean pluraliseTerms() {
		boolean pluralise = this.answer.isCountAnswer();

		if (!pluralise) {
			if (this.answer.hasAnswerSet()) {
				pluralise = this.answer.getAnswerSet().getRows().size() > 1;
			}
		}

		return pluralise;
	}

	private void findTargets() {
		findTargetConcept();
		findTargetInstance();

		if (this.tgtCon != null) {
			findQualifiers();
		}
	}

	private void findTargetConcept() {
		for (ChosenWord thisCw : this.chosenWords) {
			if (this.tgtCon == null) {
				if (thisCw.isNamedConcept()) {
					this.tgtConWord = thisCw;
					this.tgtCon = thisCw.getConcept(this.ac);
				}
			}
		}
	}

	private void findTargetInstance() {
		//Find the target instance
		for (ChosenWord thisCw : this.chosenWords) {
			if (this.tgtInst == null) {
				if (thisCw.isCeInstance()) {
					this.tgtInstWord = thisCw;
					this.tgtInst = thisCw.ceInstance();
				}
			}
		}
	}

	private void lookForQuestionWords(StringBuilder pSb, String pDefPhrase) {
		boolean foundWord = false;

		for (ChosenWord thisCw : this.chosenWords) {
			if (thisCw.isStartModifier()) {
				if (foundWord) {
					pSb.append(" ");
				}

				pSb.append(thisCw.referredInstanceText());
				foundWord = true;
			}
		}

		if (!foundWord) {
			pSb.append(pDefPhrase);

		}
	}
}
