package com.ibm.ets.ita.ce.store.conversation.generator;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.FinalItem;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.conversation.processor.QuestionProcessor;
import com.ibm.ets.ita.ce.store.generation.GeneralGenerator;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;

public class AnswerGenerator extends GeneralGenerator {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_THING = "thing";
	private static final String CON_VALUE = "value";
	private static final String PROP_DEFDET = "default determiner";
	private static final String INDENT = "  ";

	private QuestionProcessor qp = null;
	private StringBuilder sb = null;

	public AnswerGenerator(ActionContext pAc, QuestionProcessor pQp) {
		super(pAc);

		this.qp = pQp;
		this.ac = pAc;
		this.sb = new StringBuilder();
	}

	public boolean isEmpty() {
		return this.sb.length() == 0;
	}

	public String extractAnswerText() {
		return this.sb.toString();
	}

	public void msgNotKnown() {
		appendToSbNoNl(this.sb, "I'm sorry, I don't know anything about '");
		appendToSbNoNl(this.sb, this.qp.computeQualifierText());
		appendToSbNoNl(this.sb, "'");
	}

	public void msgUnhandledQuestion(ArrayList<FinalItem> pFinalItems) {
		msgUnhandledQuestion("more than four", pFinalItems);
	}

	public void msgUnhandledOneTermSimpleQuestion(ArrayList<FinalItem> pFinalItems) {
		msgUnhandledQuestion("that one", pFinalItems);
	}

	public void msgUnhandledTwoTermQuestion(ArrayList<FinalItem> pFinalItems) {
		msgUnhandledQuestion("those two", pFinalItems);
	}

	public void msgUnhandledThreeTermQuestion(ArrayList<FinalItem> pFinalItems) {
		msgUnhandledQuestion("those three", pFinalItems);
	}

	public void msgUnhandledFourTermQuestion(ArrayList<FinalItem> pFinalItems) {
		msgUnhandledQuestion("those four", pFinalItems);
	}

	public void msgNotYetSupported(String pContext, ArrayList<FinalItem> pFinalItems) {
		appendToSb(this.sb, pContext + " - needs to be completed");
		appendUnhandledTermSummary(pFinalItems);
	}

	private void msgUnhandledQuestion(String pContext, ArrayList<FinalItem> pFinalItems) {
		appendToSb(this.sb, "I cannot answer complex standard questions with " + pContext + " terms at the moment");
		appendUnhandledTermSummary(pFinalItems);
	}

	public void msgNothingUnderstood() {
		appendToSb(this.sb, "I didn't manage to understand any of that, sorry");
	}

	public void msgStandaloneWhy() {
		appendToSb(this.sb, "I can only answer why questions in the context of some previous information");
	}

	public void msgMakesNoSense(String pContext) {
		appendToSb(this.sb, "That question makes no sense! " + pContext);
	}

	public void msgNoReasonFound() {
		appendToSb(this.sb, "I can't find any reason for that I'm afraid");
	}

	public void appendMultipleItemsHeader(int pSize) {
		appendToSb(this.sb, "You asked " + pSize + " things.  The answers are:");
	}

	public void appendMultipleItemMarker(int pNum) {
		appendToSb(this.sb, "");
		appendToSbNoNl(this.sb, new Integer(pNum).toString());
		appendToSbNoNl(this.sb, ") ");
	}

	public void appendRationaleCeText(String pCeText) {
		appendToSb(this.sb, pCeText);
	}

	public void appendCountForConcept(String pOrigTerm, int pCount) {
		appendToSbNoNl(this.sb, "There are ");
		appendToSbNoNl(this.sb, new Integer(pCount).toString());
		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, pOrigTerm);
		appendToSb(this.sb, "defined in the system");
	}

	public void appendListForNoneFound(String pPluralForm) {
		appendToSbNoNl(this.sb, "No ");
		appendToSbNoNl(this.sb, pPluralForm);
		appendToSbNoNl(this.sb, " are defined in the system yet.");
	}

	public void appendListForLimitExceeded(String pPluralForm, int pCount) {
		appendToSbNoNl(this.sb, "There are ");
		appendToSbNoNl(this.sb, new Integer(pCount).toString());
		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, pPluralForm);
		appendToSbNoNl(this.sb, " defined in the system.  Please be more specific.");
	}

	public void appendListFor(CeConcept pTgtCon, String pPluralForm, int pCount, boolean pSummary) {
		appendToSbNoNl(this.sb, "The following ");
		appendToSbNoNl(this.sb, new Integer(pCount).toString());
		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, pPluralForm);
		appendToSbNoNl(this.sb, " are defined in the system:");

		for (CeInstance thisInst : this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, pTgtCon.getConceptName())) {
			if (pSummary) {
				appendToSb(this.sb, "");
				appendSummaryTextFor(thisInst);
			} else {
				appendToSb(this.sb, "");
				appendToSbNoNl(this.sb, INDENT);
				appendToSbNoNl(this.sb, thisInst.getInstanceName());
			}
		}
	}

	private void appendSummaryTextFor(CeInstance pInst) {
		appendToSbNoNl(this.sb, INDENT);
		appendToSbNoNl(this.sb, pInst.getInstanceName());
		appendToSb(this.sb, ": ");

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			appendToSbNoNl(this.sb, INDENT);
			appendToSbNoNl(this.sb, INDENT);
			appendToSbNoNl(this.sb, thisPi.getPropertyName());
			appendToSbNoNl(this.sb, "=");

			String sep = "";
			for (CePropertyValue thisPv : thisPi.getUniquePropertyValues()) {
				appendToSbNoNl(this.sb, sep);
				sep = ", ";

				if (thisPv.getValue().length() > 20) {
					appendToSbNoNl(this.sb, thisPv.getValue().substring(0, 16));
					appendToSbNoNl(this.sb, "...");
				} else {
					appendToSbNoNl(this.sb, thisPv.getValue());
				}
			}

			appendToSb(this.sb, "");
		}
	}

	public void appendTypeTextFor(CeInstance pTgtInst) {
		boolean foundType = false;

		appendToSbNoNl(this.sb, pTgtInst.getInstanceName());

		for (CeConcept leafCon : pTgtInst.getAllLeafConcepts()) {
			if (!this.qp.isConfigCon(leafCon)) {
				if (!foundType) {
					appendToSbNoNl(this.sb, " is ");
					appendToSbNoNl(this.sb, leafCon.conceptQualifier());
					appendToSbNoNl(this.sb, " ");
					appendToSbNoNl(this.sb, leafCon.getConceptName());
				} else {
					appendToSbNoNl(this.sb, " and ");
					appendToSbNoNl(this.sb, leafCon.conceptQualifier());
					appendToSbNoNl(this.sb, " ");
					appendToSbNoNl(this.sb, leafCon.getConceptName());
				}

				foundType = true;
			}
		}

		if (!foundType) {
			appendToSbNoNl(this.sb, " is part of the CE store configuration (");
			appendToSbNoNl(this.sb, pTgtInst.getFirstLeafConceptName());
			appendToSbNoNl(this.sb, ")");
		}

		appendToSb(this.sb, ".");
	}

	public void appendPropertiesTextFor(CeInstance pTgtInst, HashSet<CeInstance> pProcessedInsts, boolean pAllowConfigCons) {
		boolean foundProp = false;

		for (CePropertyInstance pi : pTgtInst.getPropertyInstances()) {
			if (!this.qp.isPropertyToBeIgnored(pi.getRelatedProperty())) {
				String propName = pi.getPropertyName();
				CeConcept domCon = pi.getRelatedProperty().getDomainConcept();

				if ((!this.qp.isConfigCon(domCon)) || pAllowConfigCons) {
					if (!this.qp.isConceptToBeIgnored(domCon)) {
						for (CePropertyValue thisPv : pi.getUniquePropertyValues()) {
							String rangeName = thisPv.getRangeName();
							String value = thisPv.getValue();
							String fullRangeName = null;

							if (rangeName.equals(CON_VALUE)) {
								fullRangeName = "";
							} else {
								fullRangeName = " the " + rangeName;
								CeInstance refInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, value);

								if (refInst != null) {
									pProcessedInsts.add(refInst);
								}
							}

							if (!foundProp) {
								String qualifier = this.qp.computeQualifierFor(pTgtInst, pAllowConfigCons);
								appendToSbNoNl(this.sb, qualifier);
								appendToSbNoNl(this.sb, " ");
							} else {
								appendToSb(this.sb, " and");
							}
							foundProp = true;

							if (pi.getRelatedProperty().isVerbSingular()) {
								appendToSbNoNl(this.sb, propName);
								if (this.qp.isShowingRanges()) {
									appendToSbNoNl(this.sb, fullRangeName);
								}
								appendToSbNoNl(this.sb, " ");
								appendToSbNoNl(this.sb, value);
							} else {
								appendToSbNoNl(this.sb, "has");
								if (this.qp.isShowingRanges()) {
									appendToSbNoNl(this.sb, fullRangeName);
								}
								appendToSbNoNl(this.sb, " ");
								appendToSbNoNl(this.sb, value);
								appendToSbNoNl(this.sb, " as ");
								appendToSbNoNl(this.sb, propName);
							}
						}
					}
				}
			}
		}

		if (foundProp) {
			appendToSbNoNl(this.sb, ".");
		}
	}

	public void appendReferencesTextFor(CeInstance pTgtInst, HashSet<CeInstance> pProcessedInsts, boolean pAllowConfigCons) {
		CePropertyInstance[] refInsts = pTgtInst.getReferringPropertyInstances();
		boolean foundRef = false;

		for (CePropertyInstance thisPi : refInsts) {
			if (!this.qp.isPropertyToBeIgnored(thisPi.getRelatedProperty())) {
				CeConcept domCon = thisPi.getRelatedProperty().getDomainConcept();

				if ((!this.qp.isConfigCon(domCon)) || pAllowConfigCons) {
					if (!this.qp.isConceptToBeIgnored(domCon)) {
						CeInstance thisInst = thisPi.getRelatedInstance();

						if (!pProcessedInsts.contains(thisInst)) {
							foundRef = true;
							appendToSb(this.sb, "");

							if (this.qp.isShowingRanges()) {
								appendMainTypeTextFor(thisInst, pAllowConfigCons);
								appendToSbNoNl(this.sb, " ");
							}

							appendToSbNoNl(this.sb, thisInst.getInstanceName());

							if (thisPi.getRelatedProperty().isVerbSingular()) {
								appendToSbNoNl(this.sb, " ");
								appendToSbNoNl(this.sb, thisPi.getPropertyName());
								appendToSbNoNl(this.sb, " ");
								appendToSbNoNl(this.sb, pTgtInst.getInstanceName());
							} else {
								appendToSbNoNl(this.sb, " has ");
								appendToSbNoNl(this.sb, pTgtInst.getInstanceName());
								appendToSbNoNl(this.sb, " as ");
								appendToSbNoNl(this.sb, thisPi.getPropertyName());
							}
						}
					}
				}
			}
		}

		if (foundRef) {
			appendToSbNoNl(this.sb, ".");
		}
	}

	private void appendMainTypeTextFor(CeInstance pTgtInst, boolean pAllowConfigCons) {
		boolean foundCon = false;

		appendToSbNoNl(this.sb, "the ");

		for (CeConcept leafCon : pTgtInst.getAllLeafConcepts()) {
			if ((!this.qp.isConfigCon(leafCon)) || pAllowConfigCons) {
				appendToSbNoNl(this.sb, leafCon.getConceptName());
				foundCon = true;
				break;
			}
		}

		if (!foundCon) {
			appendToSbNoNl(this.sb, CON_THING);
		}
	}

	public void appendSpatialUnspecifiedCoordinates(String pInstName) {
		appendToSbNoNl(this.sb, pInstName);
		appendToSbNoNl(this.sb, " is a location but does not have specific coordinates.");
	}

	public void appendNoSpatialAnswer(String pInstName) {
		appendToSbNoNl(this.sb, pInstName);
		appendToSbNoNl(this.sb, " does not have a known location.");
	}

	public void appendSpatialSpecifiedCoordinates(String pInstName, String pLat, String pLon) {
		appendToSbNoNl(this.sb, "the location of ");
		appendToSbNoNl(this.sb, pInstName);
		appendToSbNoNl(this.sb, " is (");
		appendToSbNoNl(this.sb, pLat);
		appendToSbNoNl(this.sb, ", ");
		appendToSbNoNl(this.sb, pLon);
		appendToSbNoNl(this.sb, ").");
	}

	public void appendSpatialRelationshipDetails(String pInstName, CeInstance pRelInst, CePropertyInstance pPi, String pLat, String pLon, String pConnector, boolean pAllowConfigCons) {

		appendToSbNoNl(this.sb, pConnector);

		if (pPi.getRelatedProperty().isVerbSingular()) {
			appendToSbNoNl(this.sb, pInstName);
			appendToSbNoNl(this.sb, " ");
			appendToSbNoNl(this.sb, pPi.getPropertyName());
			appendToSbNoNl(this.sb, " ");
			if (this.qp.isShowingRanges()) {
				appendMainTypeTextFor(pRelInst, pAllowConfigCons);
				appendToSbNoNl(this.sb, " ");
			}
			appendToSbNoNl(this.sb, pRelInst.getInstanceName());
		} else {
			appendToSbNoNl(this.sb, pInstName);
			appendToSbNoNl(this.sb, " has ");
			if (this.qp.isShowingRanges()) {
				appendMainTypeTextFor(pRelInst, pAllowConfigCons);
				appendToSbNoNl(this.sb, " ");
			}
			appendToSbNoNl(this.sb, pRelInst.getInstanceName());
			appendToSbNoNl(this.sb, " as ");
			appendToSbNoNl(this.sb, pPi.getPropertyName());
		}

		if (pLat != null) {
			appendToSbNoNl(this.sb, " which has the coordinates (");
			appendToSbNoNl(this.sb, pLat);
			appendToSbNoNl(this.sb, ", ");
			appendToSbNoNl(this.sb, pLon);
			appendToSbNoNl(this.sb, ")");
		}

		appendToSbNoNl(this.sb, ".");
	}

	private void appendUnhandledTermSummary(ArrayList<FinalItem> pFinalItems) {
		appendToSb(this.sb, "");
		appendToSb(this.sb, "");

		for (FinalItem thisFi : pFinalItems) {
			for (ExtractedItem thisEi : thisFi.getExtractedItems()) {
				appendToSb(this.sb, thisEi.toString());
			}
		}
	}

	public void appendConPropInstAnswer(ArrayList<CeInstance> pAnswer, CeProperty pProp, CeInstance pTgtInst) {
		String connector = "";
		String propName = null;

		if (!pAnswer.isEmpty()) {
			if (pAnswer.size() > 1) {
				propName = pProp.pluralFormName(this.ac);
			} else {
				propName = pProp.getPropertyName();
			}

			for (CeInstance answerInst : pAnswer) {
				appendToSbNoNl(this.sb, connector);

				appendDefaultDeterminer(answerInst);
				appendToSbNoNl(this.sb, answerInst.getInstanceName());

				connector = " and";
			}

			appendToSbNoNl(this.sb, " ");
			appendToSbNoNl(this.sb, propName);
			appendToSbNoNl(this.sb, " ");
			appendToSbNoNl(this.sb, pTgtInst.getInstanceName());
		} else {
			propName = pProp.getPropertyName();

			if (this.qp.isWhoQuestion()) {
				appendToSbNoNl(this.sb, "no one");
			} else {
				appendToSbNoNl(this.sb, "nothing");
			}
		}

		appendToSbNoNl(this.sb, ".");
	}

	public void appendConInstPropAnswer(ArrayList<CeInstance> pAnswer, CeProperty pProp, CeInstance pTgtInst) {
		String connector = " ";
		String propName = null;

		if (!pAnswer.isEmpty()) {
			if (pAnswer.size() > 1) {
				propName = pProp.pluralFormName(this.ac);
			} else {
				propName = pProp.getPropertyName();
			}

			appendToSbNoNl(this.sb, pTgtInst.getInstanceName());
			appendToSbNoNl(this.sb, " ");
			appendToSbNoNl(this.sb, propName);

			for (CeInstance answerInst : pAnswer) {
				appendToSbNoNl(this.sb, connector);

				appendDefaultDeterminer(answerInst);
				appendToSbNoNl(this.sb, answerInst.getInstanceName());

				connector = " and ";
			}
		} else {
			propName = pProp.getPropertyName();

			if (this.qp.isWhoQuestion()) {
				appendToSbNoNl(this.sb, "no one");
			} else {
				appendToSbNoNl(this.sb, "nothing");
			}
		}

		appendToSbNoNl(this.sb, ".");
	}

	public void appendPropInstAnswer(ArrayList<CeInstance> pAnswer, CeProperty pProp, CeInstance pTgtInst) {
		String connector = "";
		String propName = null;

		if (!pAnswer.isEmpty()) {
			if (pAnswer.size() > 1) {
				propName = pProp.pluralFormName(this.ac);
			} else {
				propName = pProp.getPropertyName();
			}

			for (CeInstance answerInst : pAnswer) {
				appendToSbNoNl(this.sb, connector);

				appendDefaultDeterminer(answerInst);
				appendToSbNoNl(this.sb, answerInst.getInstanceName());

				connector = " and ";
			}
		} else {
			propName = pProp.getPropertyName();

			if (this.qp.isWhoQuestion()) {
				appendToSbNoNl(this.sb, "no one");
			} else {
				appendToSbNoNl(this.sb, "nothing");
			}
		}

		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, propName);
		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, pTgtInst.getInstanceName());

		appendToSbNoNl(this.sb, ".");
	}

	public void appendInstPropAnswer(ArrayList<CeInstance> pAnswer, CeInstance pTgtInst, CeProperty pProp) {
		String connector = " ";
		String propName = null;

		if (!pAnswer.isEmpty()) {
			if (pAnswer.size() > 1) {
				propName = pProp.pluralFormName(this.ac);
			} else {
				propName = pProp.getPropertyName();
			}

			appendToSbNoNl(this.sb, pTgtInst.getInstanceName());
			appendToSbNoNl(this.sb, " ");
			appendToSbNoNl(this.sb, propName);

			for (CeInstance answerInst : pAnswer) {
				appendToSbNoNl(this.sb, connector);

				appendDefaultDeterminer(answerInst);
				appendToSbNoNl(this.sb, answerInst.getInstanceName());

				connector = " and";
			}
		} else {
			propName = pProp.getPropertyName();

			if (this.qp.isWhoQuestion()) {
				appendToSbNoNl(this.sb, "no one");
			} else {
				appendToSbNoNl(this.sb, "nothing");
			}
		}

		appendToSbNoNl(this.sb, ".");
	}

	private void appendDefaultDeterminer(CeInstance pInst) {
		String determiner = null;

		for (CeConcept thisCon : pInst.listAllConcepts()) {
			CeInstance mm = thisCon.retrieveMetadataInstance(this.ac);

			if (mm != null) {
				if (mm.hasPropertyInstanceForPropertyNamed(PROP_DEFDET)) {
					determiner = mm.getSingleValueFromPropertyNamed(PROP_DEFDET);
					break;
				}
			}
		}

		if (determiner != null) {
			appendToSbNoNl(this.sb, determiner);
			appendToSbNoNl(this.sb, " ");
		}
	}

//	public void appendConPropInstAnswer2(ArrayList<CeInstance> pAnswer, CeConcept pCon, CeProperty pProp, CeInstance pTgtInst) {
//		String connector = "";
//		String propName = null;
//
//		if (!pAnswer.isEmpty()) {
//			if (pAnswer.size() > 1) {
//				propName = pProp.pluralFormName(this.ac);
//			} else {
//				propName = pProp.getPropertyName();
//			}
//
//			appendToSbNoNl(pTgtInst.getInstanceName());
//			appendToSbNoNl(this.sb, " ");
//			appendToSbNoNl(propName);
//			appendToSbNoNl(this.sb, " ");
//
//			for (CeInstance answerInst : pAnswer) {
//				appendToSbNoNl(connector);
//				appendToSbNoNl(answerInst.getInstanceName());
//
//				if (pCon == null) {
//					if (!this.qp.isWhoQuestion()) {
//						//The type can be suppressed for who questions
//						appendToSbNoNl(this.sb, " (");
//						appendToSbNoNl(answerInst.getFirstLeafConceptName());
//						appendToSbNoNl(this.sb, ")");
//					}
//				}
//
//				connector = " and ";
//			}
//		} else {
//			propName = pProp.getPropertyName();
//
//			if (this.qp.isWhoQuestion()) {
//				appendToSbNoNl(this.sb, "no one");
//			} else {
//				appendToSbNoNl(this.sb, "nothing");
//			}
//		}
//
//		appendToSbNoNl(this.sb, ".");
//	}

	public void appendConInstPropAnswer(ArrayList<CeInstance> pAnswer, CeConcept pCon, CeInstance pTgtInst, CeProperty pProp) {
		String connector = "";
		String propName = null;

		if (!pAnswer.isEmpty()) {
			if (pAnswer.size() > 1) {
				propName = pProp.pluralFormName(this.ac);
			} else {
				propName = pProp.getPropertyName();
			}
		} else {
			propName = pProp.getPropertyName();
		}

		appendToSbNoNl(this.sb, pTgtInst.getInstanceName());
		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, propName);
		appendToSbNoNl(this.sb, " ");

		if (!pAnswer.isEmpty()) {

			if (pCon != null) {
				appendToSbNoNl(this.sb, "the ");
			}

			for (CeInstance answerInst : pAnswer) {
				appendToSbNoNl(this.sb, connector);
				appendToSbNoNl(this.sb, answerInst.getInstanceName());

				connector = " and ";
			}
		} else {
			if (this.qp.isWhoQuestion()) {
				appendToSbNoNl(this.sb, "no one");
			} else {
				appendToSbNoNl(this.sb, "nothing");
			}
		}

		appendToSbNoNl(this.sb, ".");
	}

	public void appendConAnswer(FinalItem pFi) {
		ExtractedItem ei = pFi.getFirstExtractedItem();
		CeConcept tgtCon = ei.getConcept();

		appendToSbNoNl(this.sb, tgtCon.getConceptName());
		appendToSb(this.sb, " is a concept.");
		appendToSbNoNl(this.sb, "It has ");
		appendToSbNoNl(this.sb, new Integer(this.ac.getModelBuilder().countAllInstancesForConcept(tgtCon)).toString());
		appendToSb(this.sb, " instances.");
	}

	public void appendPropAnswer(FinalItem pFi) {
		ExtractedItem ei = pFi.getFirstExtractedItem();
		CeProperty tgtProp = ei.getFirstProperty();

		appendToSbNoNl(this.sb, tgtProp.getPropertyName());

		if (tgtProp.isObjectProperty()) {
			appendToSb(this.sb, " is a relationship.");
			appendToSbNoNl(this.sb, "It links ");
			appendToSbNoNl(this.sb, tgtProp.getDomainConcept().getConceptName());
			appendToSbNoNl(this.sb, " to ");
			appendToSbNoNl(this.sb, tgtProp.getRangeConceptName());
			appendToSb(this.sb, ".");
		} else {
			appendToSbNoNl(this.sb, " is an attribute on ");
			appendToSbNoNl(this.sb, tgtProp.getDomainConcept().getConceptName());
			appendToSb(this.sb, ".");
		}

	}

	public void appendConInstOutgoingAnswer(ArrayList<CeInstance> pAnswer, CeConcept pCon, CeInstance pTgtInst) {
		String connector = "";
		String separator = "";
		String conName = null;

		if (!pAnswer.isEmpty()) {
			if (pAnswer.size() > 1) {
				conName = pCon.pluralFormName(this.ac);
				connector = " are ";
			} else {
				conName = pCon.getConceptName();
				connector = " is ";
			}

			appendToSbNoNl(this.sb, "the ");
			appendToSbNoNl(this.sb, conName);
			appendToSbNoNl(this.sb, " of ");
			appendToSbNoNl(this.sb, pTgtInst.getInstanceName());
			appendToSbNoNl(this.sb, connector);

			for (CeInstance answerInst : pAnswer) {
				appendToSbNoNl(this.sb, separator);
				appendToSbNoNl(this.sb, answerInst.getInstanceName());

				separator = ", ";
			}
		} else {
			conName = pCon.getConceptName();

			appendToSbNoNl(this.sb, pTgtInst.getInstanceName());
			appendToSbNoNl(this.sb, " has no ");
			appendToSbNoNl(this.sb, conName);
		}

		appendToSbNoNl(this.sb, ".");
	}

	public void appendOptAnswer(FinalItem pOpt) {
		ProcessedWord uncertainWord = null;
		String options = "";

		ArrayList<ExtractedItem> extractedItems = pOpt.getExtractedItems();
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
		appendToSb(sb, "Please specify an option.");
	}

}