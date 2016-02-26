package com.ibm.ets.ita.ce.store.conversation.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.generator.AnswerGenerator;
import com.ibm.ets.ita.ce.store.conversation.model.ConvSentence;
import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.FinalItem;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;
import com.ibm.ets.ita.ce.store.model.CeSentence;

public class QuestionProcessor {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final boolean USE_FORMAL_NAMES = true;
	private static final boolean SHOW_RANGES = false;

	private static final String CON_CONFIGCON = "configuration concept";
	private static final String CON_SPATIAL = "spatial thing";
	private static final String CON_CONVTHING = "conv thing";
	private static final String CON_AGENT = "agent";
	private static final String CON_MAN = "man";
	private static final String CON_WOMAN = "woman";
	private static final String CON_UNINTPROP = "uninteresting property";
	private static final String PROP_SINGQUAL = "single qualifier";
	private static final String PROP_IRT = "is in reply to";
	private static final String PROP_ABOUT = "is about";
	private static final String PROP_ISFROM = "is from";
	private static final String PROP_PLURAL = "plural form";
	private static final String PROP_LAT = "latitude";
	private static final String PROP_LON = "longitude";

	private ActionContext ac = null;
	private ArrayList<ProcessedWord> allWords = null;
	private ArrayList<FinalItem> finalItems = null;
	private ArrayList<FinalItem> optionItems = null;
	private ArrayList<String> matchedIds = null;
	private CeInstance cardInstance = null;
	private ConvSentence convSen = null;
	private CeConcept configCon = null;
	private boolean isCeResponse = false;
	private boolean isOptResponse = false;
	private AnswerGenerator ag = null;

	private static String computeInstanceNameFrom(CeInstance pTgtInst, ExtractedItem pEi) {
		String result = null;

		if (USE_FORMAL_NAMES) {
			result = pTgtInst.getInstanceName();
		} else {
			result = pEi.getOriginalDescription();
		}

		return result;
	}

	public QuestionProcessor(ActionContext pAc, ArrayList<ProcessedWord> pWords, ConvSentence pConvSen, CeInstance pCardInst) {
		this.ac = pAc;
		this.allWords = pWords;
		this.convSen = pConvSen;
		this.cardInstance = pCardInst;

		initialiseFinalItems(computeAllExtractedItems());

		this.matchedIds = new ArrayList<String>();
		this.configCon = this.ac.getModelBuilder().getConceptNamed(this.ac, CON_CONFIGCON);
		this.ag = new AnswerGenerator(this.ac, this);
	}

	private ArrayList<ExtractedItem> computeAllExtractedItems() {
		ArrayList<ExtractedItem> result = new ArrayList<ExtractedItem>();
		ArrayList<ExtractedItem> optionsResult = new ArrayList<ExtractedItem>();

		for (ProcessedWord pw : this.allWords) {
			if (pw.getExtractedItems() != null) {
				for (ExtractedItem ei : pw.getExtractedItems()) {
					if (!result.contains(ei)) {
						if (ei.isDominantInterpretation()) {
							result.add(ei);
						} else if (pw.confirmRequired()) {
							isOptResponse = true;
							optionsResult.add(ei);
						}
					}
				}
			}
		}

		if (isOptResponse) {
			FinalItem thisFi = null;
			optionItems = new ArrayList<FinalItem>();

			for (ExtractedItem option : optionsResult) {
				if (option.isInstanceItem()) {
					if (thisFi != null) {
						if (thisFi.isInstanceItem()) {
							thisFi.addExtractedItem(option);
						} else {
							thisFi = null;
						}
					}
				} else {
					thisFi = null;
				}

				if (thisFi == null) {
					thisFi = new FinalItem(option);
					optionItems.add(thisFi);
				}
			}
		}

		return result;
	}

	private void initialiseFinalItems(ArrayList<ExtractedItem> pExList) {
		FinalItem thisFi = null;
		this.finalItems = new ArrayList<FinalItem>();

		for (ExtractedItem thisEi : pExList) {
			if (thisEi.isInstanceItem()) {
				if (thisFi != null) {
					if (thisFi.isInstanceItem()) {
						thisFi.addExtractedItem(thisEi);
					} else {
						thisFi = null;
					}
				}
			} else {
				thisFi = null;
			}

			if (thisFi == null) {
				thisFi = new FinalItem(thisEi);
				this.finalItems.add(thisFi);
			}
		}
	}

	public ResultOfAnalysis processQuestion() {
		ResultOfAnalysis result = null;
		CeInstance fromUser = this.cardInstance.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);
		String fromName = null;

		if (fromUser == null) {
			fromName = "(none)";
		} else {
			fromName = fromUser.getInstanceName();
		}

		generateQuestionAnalysis();

		if (isStandardQuestion()) {
			if (ConversationProcessor.isAuthorisedForAsk(fromUser)) {
				answerStandardQuestion();
			} else {
				result = ResultOfAnalysis.msgActNotAuthorised(fromName, "ask");
			}
		} else if (isWhyQuestion()) {
			if (ConversationProcessor.isAuthorisedForWhy(fromUser)) {
				answerWhyQuestion();
			} else {
				result = ResultOfAnalysis.msgActNotAuthorised(fromName, "why");
			}
		} else if (isAggregationQuestion()) {
			if (ConversationProcessor.isAuthorisedForAsk(fromUser)) {
				answerAggregationQuestion();
			} else {
				result = ResultOfAnalysis.msgActNotAuthorised(fromName, "ask");
			}
		} else {
			reportError("Unexpected question type: " + extractQuestionPhrase(), this.ac);
		}

		if (result == null) {
			result = composeResult();
		}

		return result;
	}

	private void generateQuestionAnalysis() {
		StringBuilder sb = new StringBuilder();

		if (finalItems != null) {
			for (FinalItem thisFi : this.finalItems) {
				if (thisFi.isConceptItem()) {
					ExtractedItem thisEi = thisFi.getFirstExtractedItem();
					CeConcept tgtCon = thisEi.getConcept();

					sb.append("[con: ");
					sb.append(tgtCon.getConceptName());
					sb.append("] ");
				} else if (thisFi.isPropertyItem()) {
					ExtractedItem thisEi = thisFi.getFirstExtractedItem();
					CeProperty tgtProp = thisEi.getFirstProperty();

					sb.append("[prop: ");
					sb.append(tgtProp.getPropertyName());
					sb.append("] ");
				} else if (thisFi.isInstanceItem()) {
					String connector = "";

					sb.append("[inst: ");

					for (ExtractedItem thisEi : thisFi.getExtractedItems()) {
						CeInstance tgtInst = thisEi.getInstance();
						System.out.println("tgtInst: " + tgtInst);
						sb.append(connector);
						sb.append(tgtInst.getInstanceName());
						connector = ", ";
					}

					sb.append("] ");
				} else {
					ExtractedItem thisEi = thisFi.getFirstExtractedItem();

					sb.append("[???: ");
					sb.append(thisEi.toString());
					sb.append("] ");
				}
			}
		}

		if (optionItems != null) {
			for (FinalItem option : optionItems) {
				if (option.isConceptItem()) {
					// TODO
				} else if (option.isPropertyItem()) {
					// TODO
				} else if (option.isInstanceItem()) {
					String connector = "";

					sb.append("[opt inst: ");

					for (ExtractedItem thisEi : option.getExtractedItems()) {
						CeInstance tgtInst = thisEi.getInstance();
						sb.append(connector);
						sb.append(tgtInst.getInstanceName());
						connector = ", ";
					}

					sb.append("] ");
				} else {
					ExtractedItem thisEi = option.getFirstExtractedItem();

					sb.append("[???: ");
					sb.append(thisEi.toString());
					sb.append("] ");
				}
			}
		}

		this.convSen.setAnalysisText(sb.toString());
	}

	private ResultOfAnalysis composeResult() {
		ResultOfAnalysis result = null;

		if (this.isCeResponse) {
			String ceText = this.ag.extractAnswerText();
			result = ResultOfAnalysis.createTellFor(ceText);
		} else if (isOptResponse) {
			String optText = this.ag.extractAnswerText();
			result = ResultOfAnalysis.createOptionResponse(optText);
		} else {
			String gistText = this.ag.extractAnswerText();
			result = ResultOfAnalysis.createQuestionResponseWithGistAndCe(gistText, "");
		}

		for (String thisId : this.matchedIds) {
			result.addReferencedId(thisId);
		}

		return result;
	}

	private CeInstance findOriginalCard() {
		//The original card is the one (if any) that this card is in reply to
		CeInstance result = null;

		if (this.cardInstance != null) {
			result = this.cardInstance.getSingleInstanceFromPropertyNamed(this.ac, PROP_IRT);
		}

		return result;
	}

	private ProcessedWord getFirstQuestionWord() {
		ProcessedWord result = null;

		for (ProcessedWord pw : this.allWords) {
			if (pw.isQuestionWord()) {
				result = pw;
				break;
			}
		}

		return result;
	}

	private ProcessedWord getFirstPostQuestionWord() {
		ProcessedWord result = null;
		boolean foundQuestion = false;

		for (ProcessedWord pw : this.allWords) {
			if (foundQuestion) {
				result = pw;
				break;
			}

			if (pw.isQuestionWord()) {
				foundQuestion = true;
			}
		}

		return result;
	}

//	private boolean isAllInstances() {
//		boolean result = (this.finalItems != null) && (!this.finalItems.isEmpty());
//
//		for (FinalItem thisFi : this.finalItems) {
//			if (!thisFi.isInstanceItem()) {
//				result = false;
//				break;
//			}
//		}
//
//		return result;
//	}

	private void answerStandardQuestion() {
		if (finalItems != null && !this.finalItems.isEmpty()) {
			if (this.finalItems.size() == 1) {
				FinalItem firstFi = this.finalItems.get(0);

				handle1ElementQuestion(firstFi);
			} else if (this.finalItems.size() == 2) {
				FinalItem firstFi = this.finalItems.get(0);
				FinalItem secondFi = this.finalItems.get(1);

				handle2ElementQuestion(firstFi, secondFi, true);
			} else if (this.finalItems.size() == 3) {
				FinalItem firstFi = this.finalItems.get(0);
				FinalItem secondFi = this.finalItems.get(1);
				FinalItem thirdFi = this.finalItems.get(2);

				handle3ElementQuestion(firstFi, secondFi, thirdFi, true);
			} else if (this.finalItems.size() == 4) {
				FinalItem firstFi = this.finalItems.get(0);
				FinalItem secondFi = this.finalItems.get(1);
				FinalItem thirdFi = this.finalItems.get(2);
				FinalItem fourthFi = this.finalItems.get(3);

				handle4ElementQuestion(firstFi, secondFi, thirdFi, fourthFi, true);
			} else {
				this.ag.msgUnhandledQuestion(this.finalItems);
			}
		} else if (optionItems != null && !optionItems.isEmpty()) {
			for (FinalItem option : optionItems) {
				this.ag.appendOptAnswer(option);
			}
		} else {
			this.ag.msgNothingUnderstood();
		}
	}

	private void handle1ElementQuestion(FinalItem pFirstFi) {
		if (pFirstFi.isConceptItem()) {
			//Concept
			handle1wayConceptQuestion(pFirstFi);
		} else if (pFirstFi.isPropertyItem()) {
			//Property
			handle1wayPropertyQuestion(pFirstFi);
		} else if (pFirstFi.isInstanceItem()) {
			//Instance
			handle1wayInstanceQuestion(pFirstFi);
		} else {
			this.ag.msgUnhandledOneTermSimpleQuestion(this.finalItems);
		}
	}

	private void handle1wayConceptQuestion(FinalItem pFi) {
		this.ag.appendConAnswer(pFi);
	}

	private void handle1wayPropertyQuestion(FinalItem pFi) {
		this.ag.appendPropAnswer(pFi);
	}

	private void handle1wayInstanceQuestion(FinalItem pFi) {
		ArrayList<ExtractedItem> instItems = pFi.getExtractedItems();

		if (instItems.isEmpty()) {
			this.ag.msgNotKnown();
		} else {
			boolean multipleMode = false;

			if (instItems.size() > 1) {
				multipleMode = true;
				this.ag.appendMultipleItemsHeader(instItems.size());
			}

			int ctr = 0;
			for (ExtractedItem thisEi : instItems) {
				CeInstance tgtInst = thisEi.getInstance();

				if (multipleMode) {
					this.ag.appendMultipleItemMarker(++ctr);
				}

				computeQuestionAnswerForInstance(tgtInst, thisEi);
			}
		}
	}

	private ArrayList<CeInstance> handle2ElementQuestion(FinalItem pFirstFi, FinalItem pSecondFi, boolean pFinal) {
		ArrayList<CeInstance> answer = null;

		if (pFirstFi.isConceptItem() && pSecondFi.isConceptItem()) {
			//Concept Concept
			answer = handle2wayConConQuestion(pFirstFi, pSecondFi, pFinal);
		} else if (pFirstFi.isConceptItem() && pSecondFi.isPropertyItem()) {
			//Concept Property
			answer = handle2wayConPropQuestion(pFirstFi, pSecondFi, pFinal);
		} else if (pFirstFi.isConceptItem() && pSecondFi.isInstanceItem()) {
			//Concept Instance
			answer = handle2wayConInstQuestion(pFirstFi, pSecondFi, pFinal);
		} else if (pFirstFi.isPropertyItem() && pSecondFi.isConceptItem()) {
			//Property Concept
			answer = handle2wayPropConQuestion(pFirstFi, pSecondFi, pFinal);
		} else if (pFirstFi.isPropertyItem() && pSecondFi.isPropertyItem()) {
			//Property Property
			answer = handle2wayPropPropQuestion();
		} else if (pFirstFi.isPropertyItem() && pSecondFi.isInstanceItem()) {
			//Property Instance
			answer = handle2wayPropInstQuestion(pFirstFi, pSecondFi, pFinal);
		} else if (pFirstFi.isInstanceItem() && pSecondFi.isConceptItem()) {
			//Instance Concept
			answer = handle2wayInstConQuestion(pFirstFi, pSecondFi, pFinal);
		} else if (pFirstFi.isInstanceItem() && pSecondFi.isPropertyItem()) {
			//Instance Property
			answer = handle2wayInstPropQuestion(pFirstFi, pSecondFi, pFinal);
		} else if (pFirstFi.isInstanceItem() && pSecondFi.isInstanceItem()) {
			//Instance Instance
			//(should not be possible)
			this.ag.msgUnhandledTwoTermQuestion(this.finalItems);
		} else {
			this.ag.msgUnhandledTwoTermQuestion(this.finalItems);
		}

		return answer;
	}

	private ArrayList<CeInstance> handle2wayConConQuestion(FinalItem pFirstFi, FinalItem pSecondFi, boolean pFinal) {
		//TODO: Complete this
		this.ag.msgNotYetSupported("handleConceptConceptQuestion()", this.finalItems);

		return null;
	}

	private ArrayList<CeInstance> handle2wayConPropQuestion(FinalItem pFirstFi, FinalItem pSecondFi, boolean pFinal) {
		//TODO: Complete this
		this.ag.msgNotYetSupported("handleConceptPropertyQuestion()", this.finalItems);

		return null;
	}

	private ArrayList<CeInstance> handle2wayConInstQuestion(FinalItem pFirstFi, FinalItem pSecondFi, boolean pFinal) {
		ArrayList<CeInstance> answer = new ArrayList<CeInstance>();
		ExtractedItem firstEi = pFirstFi.getFirstExtractedItem();
		ExtractedItem secondEi = pSecondFi.getFirstExtractedItem();

		CeConcept tgtCon = firstEi.getConcept();
		CeInstance tgtInst = secondEi.getInstance();

		//First check for outgoing relationships
		for (CePropertyInstance thisPi : tgtInst.getPropertyInstances()) {
			if (thisPi.getRelatedProperty().isObjectProperty()) {
				for (CeInstance relInst : thisPi.getValueInstanceList(this.ac)) {
					if (relInst.isConcept(tgtCon)) {
						if (!answer.contains(relInst)) {
							answer.add(relInst);
						}
					}
				}
			}
		}

		if (pFinal) {
			this.ag.appendConInstOutgoingAnswer(answer, tgtCon, tgtInst);
		}

		return answer;
	}

	private ArrayList<CeInstance> handle2wayPropConQuestion(FinalItem pFirstFi, FinalItem pSecondFi, boolean pFinal) {
		//TODO: Complete this
		this.ag.msgNotYetSupported("handlePropertyConceptQuestion()", this.finalItems);

		return null;
	}

	private ArrayList<CeInstance> handle2wayPropPropQuestion() {
		this.ag.msgMakesNoSense("[property, property]");

		return null;
	}

	private ArrayList<CeInstance> handle2wayPropInstQuestion(FinalItem pFirstFi, FinalItem pSecondFi, boolean pFinal) {
		ArrayList<CeInstance> answer = new ArrayList<CeInstance>();
		ExtractedItem firstEi = pFirstFi.getFirstExtractedItem();
		ExtractedItem secondEi = null;
		CeProperty tgtProp = null;
		CeInstance tgtInst = null;
		boolean forwardMode = false;

		if (pSecondFi.getExtractedItems().size() > 1) {
			secondEi = pSecondFi.getLastExtractedItem();

			tgtProp = firstEi.getFirstProperty();
			tgtInst = secondEi.getInstance();

			//There is more than one instance, so assume we need to check outgoing relationships
			forwardMode = true;
			CePropertyInstance thisPi = tgtInst.getPropertyInstanceForProperty(tgtProp);

			for (CeInstance matchedInst : thisPi.getValueInstanceList(this.ac)) {
				if (isWhoQuestion()) {
					if (matchedInst.isConceptNamed(this.ac, CON_AGENT)) {
						if (!answer.contains(matchedInst)) {
							answer.add(matchedInst);
						}
					}
				} else {
					if (!answer.contains(matchedInst)) {
						answer.add(matchedInst);
					}
				}
			}
		} else {
			secondEi = pSecondFi.getLastExtractedItem();

			tgtProp = firstEi.getFirstProperty();
			tgtInst = secondEi.getInstance();

			//There is exactly one instance, so assume we need to check incoming relationships
			for (CePropertyInstance thisPi : tgtInst.getReferringPropertyInstances()) {
				if (thisPi.getPropertyName().equals(tgtProp.getPropertyName())) {
					CeInstance matchedInst = thisPi.getRelatedInstance();

					if (isWhoQuestion()) {
						if (matchedInst.isConceptNamed(this.ac, CON_AGENT)) {
							if (!answer.contains(matchedInst)) {
								answer.add(matchedInst);
							}
						}
					} else {
						if (!answer.contains(matchedInst)) {
							answer.add(matchedInst);
						}
					}
				}
			}
		}


		if (pFinal) {
			if (forwardMode) {
				this.ag.appendInstPropAnswer(answer, tgtInst, tgtProp);
			} else {
				this.ag.appendPropInstAnswer(answer, tgtProp, tgtInst);
			}
		}

		return answer;
	}

	private ArrayList<CeInstance> handle2wayInstConQuestion(FinalItem pFirstFi, FinalItem pSecondFi, boolean pFinal) {
		//TODO: Complete this
		this.ag.msgNotYetSupported("handleInstanceConceptQuestion()", this.finalItems);

		return null;
	}

	private ArrayList<CeInstance> handle2wayInstPropQuestion(FinalItem pFirstFi, FinalItem pSecondFi, boolean pFinal) {
		ArrayList<CeInstance> answer = new ArrayList<CeInstance>();
		ExtractedItem firstEi = pFirstFi.getFirstExtractedItem();
		ExtractedItem secondEi = pSecondFi.getFirstExtractedItem();

		CeInstance tgtInst = firstEi.getInstance();
		CeProperty tgtProp = secondEi.getFirstProperty();

		for (CePropertyInstance thisPi : tgtInst.getPropertyInstances()) {
			if (thisPi.getPropertyName().equals(tgtProp.getPropertyName())) {
				//TODO: This should handle values too
				for (CeInstance matchedInst : thisPi.getValueInstanceList(this.ac)) {

					if (isWhoQuestion()) {
						if (matchedInst.isConceptNamed(this.ac, CON_AGENT)) {
							if (!answer.contains(matchedInst)) {
								answer.add(matchedInst);
							}
						}
					} else {
						if (!answer.contains(matchedInst)) {
							answer.add(matchedInst);
						}
					}
				}
			}
		}

		if (pFinal) {
			this.ag.appendConInstPropAnswer(answer, null, tgtInst, tgtProp);
		}

		return answer;
	}

	private ArrayList<CeInstance> handle3ElementQuestion(FinalItem pFirstFi, FinalItem pSecondFi, FinalItem pThirdFi, boolean pFinal) {
		ArrayList<CeInstance> answer = null;

		//TODO: Handle more forms here?
		answer = handle2ElementQuestion(pSecondFi, pThirdFi, false);

		if (pFirstFi.isConceptItem()) {
			//Concept
			answer = handle3wayConQuestion(pFirstFi, pSecondFi, pThirdFi, answer, pFinal);
		} else {
			this.ag.msgUnhandledThreeTermQuestion(this.finalItems);
		}

		return answer;
	}

	private ArrayList<CeInstance> handle3wayConQuestion(FinalItem pFirstFi, FinalItem pSecondFi, FinalItem pThirdFi, ArrayList<CeInstance> pAnswer, boolean pFinal) {
		ArrayList<CeInstance> finalAnswer = new ArrayList<CeInstance>();

		if (pAnswer != null) {
			ExtractedItem firstEi = pFirstFi.getFirstExtractedItem();

			CeConcept tgtCon = firstEi.getConcept();

			for (CeInstance answerInst : pAnswer) {
				if (answerInst.isConcept(tgtCon)) {
					finalAnswer.add(answerInst);
				}
			}

			if (pFinal) {
				if (pSecondFi.isPropertyItem() && pThirdFi.isInstanceItem()) {
					this.ag.appendConPropInstAnswer(finalAnswer, getFirstProperty(), getFirstInstance());
				} else if (pSecondFi.isInstanceItem() && pThirdFi.isPropertyItem()) {
					this.ag.appendConInstPropAnswer(finalAnswer, getFirstProperty(), getFirstInstance());
				} else {
					this.ag.msgNotYetSupported("handle3wayConQuestion()", this.finalItems);
				}
			}
		}

		return finalAnswer;
	}

	private void handle4ElementQuestion(FinalItem pFirstFi, FinalItem pSecondFi, FinalItem pThirdFi, FinalItem pFourthFi, boolean pFinal) {
		if (pFirstFi.isConceptItem() && pSecondFi.isConceptItem() && pThirdFi.isInstanceItem() && pFourthFi.isPropertyItem()) {
			//Concept Concept Instance Property
			handle4wayConConInstPropQuestion(pFirstFi, pSecondFi, pThirdFi, pFourthFi);
		} else {
			this.ag.msgUnhandledFourTermQuestion(this.finalItems);
		}
	}

	private void handle4wayConConInstPropQuestion(FinalItem pFirstFi, FinalItem pSecondFi, FinalItem pThirdFi, FinalItem pFourthFi) {
		//TODO: Implement this properly
		handle3ElementQuestion(pSecondFi, pThirdFi, pFourthFi, true);
	}

	private CeProperty getFirstProperty() {
		CeProperty result = null;

		for (FinalItem thisFi : this.finalItems) {
			if (thisFi.isPropertyItem()) {
				//TODO: Better method than just getting the first property
				ExtractedItem thisEi = thisFi.getFirstExtractedItem();
				result = thisEi.getFirstProperty();
				break;
			}
		}

		return result;
	}

	private CeInstance getFirstInstance() {
		CeInstance result = null;

		for (FinalItem thisFi : this.finalItems) {
			if (thisFi.isInstanceItem()) {
				ExtractedItem thisEi = thisFi.getFirstExtractedItem();
				result = thisEi.getInstance();
				break;
			}
		}

		return result;
	}

	public boolean isConfigCon(CeConcept pCon) {
		return pCon.equalsOrHasParent(this.configCon);
	}

	private void answerWhyQuestion() {
		CeInstance originalCard = findOriginalCard();

		if (originalCard == null) {
			//TODO: Relax this constraint.  You should be able to ask "why is Fred married"
			this.ag.msgStandaloneWhy();
		} else {
			computeRationaleFor(originalCard);
		}
	}

	private void computeRationaleFor(CeInstance pOrigCard) {
		String qualifierText = computeQualifierText();
		ArrayList<String> existingCeText = new ArrayList<String>();
		boolean foundExplanation = false;

		for (CeInstance thisInst : pOrigCard.getInstanceListFromPropertyNamed(this.ac, PROP_ABOUT)) {
			for (CeSentence thisSen : thisInst.getPrimarySentences()) {
				if (thisSen.hasRationale()) {
					String lcCeText = thisSen.getCeTextWithoutRationale(this.ac).toLowerCase();

					if (qualifierText.isEmpty() || lcCeText.contains(qualifierText)) {
						String thisCeText = thisSen.getCeText(this.ac);

						if (!existingCeText.contains(thisCeText)) {
							this.ag.appendRationaleCeText(thisCeText);
							foundExplanation = true;
						}
					}
				}
			}
		}

		if (foundExplanation) {
			this.isCeResponse = true;
		} else {
			this.ag.msgNoReasonFound();
		}
	}

	private void answerAggregationQuestion() {
		ArrayList<ExtractedItem> conItems = listAllExtractedItemConcepts();

		if (conItems.isEmpty()) {
			this.ag.msgNotKnown();
		} else {
			for (ExtractedItem thisEi : conItems) {
				String origDesc = thisEi.getOriginalDescription();
				CeConcept thisCon = thisEi.getConcept();

				this.matchedIds.add(thisCon.getConceptName());

				if (isCountQuestion()) {
					int count = this.ac.getModelBuilder().getInstanceCountForConcept(thisCon);
					this.ag.appendCountForConcept(origDesc, count);
				} else if (isListQuestion()) {
					computeListForConcept(thisCon, origDesc, false);
				} else if (isSummariseQuestion()) {
					computeListForConcept(thisCon, origDesc, true);
				} else {
					reportError("Unexpected aggregation operator:" + extractQuestionPhrase(), this.ac);
				}
			}
		}
	}

	private void computeListForConcept(CeConcept pTgtCon, String pOrigTerm, boolean pSummary) {
		boolean isUnlimited = isUnlimitedListRequest();
		String pluralForm = statedPluralFor(pTgtCon);
		int instCount = this.ac.getModelBuilder().getInstanceCountForConcept(pTgtCon);

		if (pluralForm == null) {
			if (pOrigTerm.equals(pTgtCon.getConceptName())) {
				pluralForm = pOrigTerm + "s";
			} else {
				pluralForm = pOrigTerm;
			}
		}

		if (instCount == 0) {
			this.ag.appendListForNoneFound(pluralForm);
		} else {
			if ((instCount > 50) && !isUnlimited) {
				this.ag.appendListForLimitExceeded(pluralForm, instCount);
			} else {
				this.ag.appendListFor(pTgtCon, pluralForm, instCount, pSummary);
			}
		}
	}

	private String statedPluralFor(CeConcept pTgtCon) {
		String result = null;
		CeInstance mm = pTgtCon.retrieveMetaModelInstance(this.ac);

		if (mm != null) {
			result = mm.getSingleValueFromPropertyNamed(PROP_PLURAL);

			if (result.isEmpty()) {
				result = null;
			}
		}

		return result;
	}

	public String computeQualifierText() {
		String result = null;

		for (ProcessedWord pw : this.allWords) {
			if (!pw.isQuestionWord()) {
				if (result == null) {
					result = "";
				} else {
					result += " ";
				}

				result += pw.getWordText();
			}
		}

		return result;
	}

	private ArrayList<ExtractedItem> listAllExtractedItemConcepts() {
		ArrayList<ExtractedItem> result = new ArrayList<ExtractedItem>();

		for (FinalItem thisFi : this.finalItems) {
			if (thisFi.isConceptItem()) {
				ExtractedItem thisEi = thisFi.getFirstExtractedItem();
				result.add(thisEi);
			}
		}

		return result;
	}

	private String extractQuestionPhrase() {
		String result = null;

		for (ProcessedWord thisPw : this.allWords) {
			if (thisPw.isQuestionWord()) {
				if (result != null) {
					result += " ";
				}

				result += thisPw.getLcWordText();
			}
		}

		return result;
	}

	private void computeQuestionAnswerForInstance(CeInstance pTgtInst, ExtractedItem pEi) {
		boolean allowConfigCons = isOnlyConfig(pTgtInst);

		this.matchedIds.add(pTgtInst.getInstanceName());

		if (isWhatQuestion() || isWhoQuestion()) {
			computeDetailsForInstance(pTgtInst, allowConfigCons);
		} else if (isWhereQuestion()) {
			computeLocationForInstance(pTgtInst, pEi, allowConfigCons);
		}

		if (this.ag.isEmpty()) {
			this.ag.appendNoSpatialAnswer(pTgtInst.getInstanceName());
		}
	}

	private void computeDetailsForInstance(CeInstance pTgtInst, boolean pAllowConfigCons) {
		HashSet<CeInstance> processedInsts = new HashSet<CeInstance>();

		this.ag.appendTypeTextFor(pTgtInst);
		this.ag.appendPropertiesTextFor(pTgtInst, processedInsts, pAllowConfigCons);
		this.ag.appendReferencesTextFor(pTgtInst, processedInsts, pAllowConfigCons);
	}

	private void computeLocationForInstance(CeInstance pTgtInst, ExtractedItem pEi, boolean pAllowConfigCons) {
		computeDirectLocationFor(pTgtInst, pEi);
		computeReferencedLocationFor(pTgtInst, pEi, pAllowConfigCons);
	}

	public String computeQualifierFor(CeInstance pTgtInst, boolean pAllowConfigCons) {
		String qualifier = null;

		//First try each of the leaf concepts
		for (CeConcept leafCon : pTgtInst.getAllLeafConcepts()) {
			if ((!leafCon.equalsOrHasParent(this.configCon)) || pAllowConfigCons) {
				CeInstance mm = leafCon.retrieveMetaModelInstance(this.ac);
				qualifier = mm.getSingleValueFromPropertyNamed(PROP_SINGQUAL);

				if (!qualifier.isEmpty()) {
					break;
				}
			}
		}

		if ((qualifier == null) || (qualifier.isEmpty())) {
			//Now try parents
			for (CeConcept leafCon : pTgtInst.getAllLeafConcepts()) {
				if (!leafCon.equalsOrHasParent(this.configCon)) {
					qualifier = getSingleQualifierFor(leafCon);

					if (!qualifier.isEmpty()) {
						break;
					}
				}
			}
		}

		if ((qualifier == null) || (qualifier.isEmpty())) {
			if (isWhoQuestion()) {
				if (pTgtInst.isConceptNamed(ac, CON_MAN)) {
					qualifier = "he";
				} else if (pTgtInst.isConceptNamed(ac, CON_WOMAN)) {
					qualifier = "she";
				} else {
					qualifier = "he or she";
				}
			} else {
				qualifier = "it";
			}
		}

		return qualifier;
	}

	private String getSingleQualifierFor(CeConcept pTgtCon) {
		String qualifier = null;
		CeInstance mm = pTgtCon.retrieveMetaModelInstance(this.ac);

		if (mm != null) {
			qualifier = mm.getSingleValueFromPropertyNamed(PROP_SINGQUAL);

			if (qualifier.isEmpty()) {
				for (CeConcept parCon : pTgtCon.getDirectParents()) {
					qualifier = getSingleQualifierFor(parCon);

					if (!qualifier.isEmpty()) {
						break;
					}
				}
			}
		}

		return qualifier;
	}

	private void computeDirectLocationFor(CeInstance pTgtInst, ExtractedItem pEi) {
		if (pTgtInst.isConceptNamed(this.ac, CON_SPATIAL)) {
			String instName = computeInstanceNameFrom(pTgtInst, pEi);
			if (pTgtInst.getSingleValueFromPropertyNamed(PROP_LAT).isEmpty()) {
				this.ag.appendSpatialUnspecifiedCoordinates(instName);
			} else {
				String lat = pTgtInst.getSingleValueFromPropertyNamed(PROP_LAT);
				String lon = pTgtInst.getSingleValueFromPropertyNamed(PROP_LON);

				this.ag.appendSpatialSpecifiedCoordinates(instName, lat, lon);
			}
		}
	}

	private void computeReferencedLocationFor(CeInstance pTgtInst, ExtractedItem pEi, boolean pAllowConfigCons) {
		String connector = "";

		for (CePropertyInstance pi : pTgtInst.getPropertyInstances()) {
			if (pi.getRelatedProperty().isObjectProperty()) {
				for (CePropertyValue pv : pi.getUniquePropertyValues()) {
					CeInstance relInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, pv.getValue());

					if (relInst != null) {
						if (relInst.isConceptNamed(this.ac, CON_SPATIAL)) {
							String instName = computeInstanceNameFrom(pTgtInst, pEi);
							String lat = null;
							String lon = null;

							if (!relInst.getSingleValueFromPropertyNamed(PROP_LAT).isEmpty()) {
								lat = relInst.getSingleValueFromPropertyNamed(PROP_LAT);
								lon = relInst.getSingleValueFromPropertyNamed(PROP_LON);
							}

							this.ag.appendSpatialRelationshipDetails(instName, relInst, pi, lat, lon, connector, pAllowConfigCons);
							connector = " and\n";
						}
					}
				}
			}
		}
	}

	private boolean isWhyQuestion() {
		ProcessedWord fqw = getFirstQuestionWord();

		return (fqw != null) && fqw.isWhy();
	}

	private boolean isWhatQuestion() {
		ProcessedWord fqw = getFirstQuestionWord();

		return (fqw != null) && fqw.isWhat();
	}

	public boolean isWhoQuestion() {
		ProcessedWord fqw = getFirstQuestionWord();

		return (fqw != null) && fqw.isWho();
	}

	private boolean isWhereQuestion() {
		ProcessedWord fqw = getFirstQuestionWord();

		return (fqw != null) && fqw.isWhere();
	}

	private boolean isWhichQuestion() {
		ProcessedWord fqw = getFirstQuestionWord();

		return (fqw != null) && fqw.isWhich();
	}

	private boolean isCountQuestion() {
		ProcessedWord fqw = getFirstQuestionWord();

		return (fqw != null) && fqw.isCount();
	}

	private boolean isListQuestion() {
		ProcessedWord fqw = getFirstQuestionWord();

		return (fqw != null) && fqw.isList();
	}

	private boolean isSummariseQuestion() {
		ProcessedWord fqw = getFirstQuestionWord();

		return (fqw != null) && fqw.isSummarise();
	}

	private boolean isStandardQuestion() {
		return
			isWhatQuestion() ||
			isWhoQuestion() ||
			isWhereQuestion() ||
			isWhichQuestion();
	}

	private boolean isAggregationQuestion() {
		return
			isCountQuestion() ||
			isListQuestion() ||
			isSummariseQuestion();
	}

	private boolean isUnlimitedListRequest() {
		ProcessedWord fpqw = getFirstPostQuestionWord();

		return (fpqw != null) && fpqw.isAll();
	}

	public boolean isConceptToBeIgnored(CeConcept pCon) {
		ArrayList<String> excludeList = new ArrayList<String>();
		excludeList.add(CON_CONVTHING);

		return pCon.equalsOrHasParentNamed(this.ac, excludeList);
	}

	public boolean isPropertyToBeIgnored(CeProperty pProp) {
		boolean result = false;

		CeInstance mm = pProp.getMetaModelInstance(this.ac);

		if (mm != null) {
			result = mm.isConceptNamed(this.ac, CON_UNINTPROP);
		}

		return result;
	}

	private boolean isOnlyConfig(CeInstance pInst) {
		boolean result = true;

		for (CeConcept thisCon : pInst.getAllLeafConcepts()) {
			if (!thisCon.equalsOrHasParent(this.configCon)) {
				result = false;
				break;
			}
		}

		return result;
	}

	@SuppressWarnings("static-method")
	public boolean isShowingRanges() {
		return SHOW_RANGES;
	}

}