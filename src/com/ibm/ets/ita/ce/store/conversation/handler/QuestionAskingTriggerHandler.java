package com.ibm.ets.ita.ce.store.conversation.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.encodeAndEncloseInQuotesIfNeeded;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class QuestionAskingTriggerHandler extends GeneralConversationHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String SRC_QUES = "conv_ques";
	private static final String CON_NLCARD = "NL card";
	private static final String CON_QUES = "question";
	private static final String CON_ASKCARD = "ask card";
	private static final String CON_AUTHUSER = "authorised user";
	private static final String PROP_CONTENT = "content";
	private static final String PROP_ISTO = "is to";
	private static final String PROP_IRT = "is in reply to";
	private static final String PROP_WASASKED = "was asked";
	private static final String PROP_QTEXT = "question text";
	private static final String PROP_ANSWER = "answer";
	private static final String PROP_ISGIVENBY = "is given by";
	private static final String PROP_CANASK = "can ask";
	
	private static final String PARM_CUTOFF = "cutoff period";
	private static final String PARM_TGTPHR = "target phrase";
	private static final String PARM_PERCENT = "percent chance";
	private static final String PARM_MAXQS = "max questions per user";

	private long cutoffDuration = -1;
	private int percentChance = -1;
	private int maxQuestions = -1;
	private String targetPhrase = null;

	@Override
	public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId, String pRuleOrQuery, String pRuleOrQueryName) {
		initialise(pAc);
		extractMyTriggerDetailsUsing(pTriggerName);

		//Only property matched triggers are handled
		if (pThingType.equals(TYPE_PROP)) {
			handlePropertyTrigger();
		} else {
			reportWarning("Unexpected trigger type (" + pThingType + ") for conversation trigger handler", this.ac);
		}
		
		cleanup();
	}

	private boolean extractMyTriggerDetailsUsing(String pTriggerName) {
		boolean result = true;

		extractTriggerDetailsUsing(pTriggerName);

		this.targetPhrase = this.trigInst.getSingleValueFromPropertyNamed(PARM_TGTPHR);

		if ((this.targetPhrase != null) && (!this.targetPhrase.isEmpty())) {
			String propVal = null;

			propVal = this.trigInst.getSingleValueFromPropertyNamed(PARM_CUTOFF);

			if (!propVal.isEmpty()) {
				try {
					this.cutoffDuration = new Long(propVal).longValue();
				} catch (NumberFormatException e) {
					reportError("Invalid number (long) specified for '" + PARM_CUTOFF + "' property", this.ac);
				}
			}

			propVal = this.trigInst.getSingleValueFromPropertyNamed(PARM_PERCENT);

			if (!propVal.isEmpty()) {
				try {
					this.percentChance = new Integer(propVal).intValue();
				} catch (NumberFormatException e) {
					reportError("Invalid number (int) specified for '" + PARM_PERCENT + "' property", this.ac);
				}
			}

			propVal = this.trigInst.getSingleValueFromPropertyNamed(PARM_MAXQS);

			if (!propVal.isEmpty()) {
				try {
					this.maxQuestions = new Integer(propVal).intValue();
				} catch (NumberFormatException e) {
					reportError("Invalid number (int) specified for '" + PARM_MAXQS + "' property", this.ac);
				}
			}
		} else {
			reportError("Cannot do question answer processing as no target phrase is identified", this.ac);
			result = false;
		}

		return result;
	}

	private void handlePropertyTrigger() {
		//TODO: Review whether this CopyOnWriteArrayList is needed here
		CopyOnWriteArrayList<CeInstance> copyList = new CopyOnWriteArrayList<CeInstance>(this.ac.getSessionCreations().getNewInstances());
		for (CeInstance thisInst : copyList) {
			if (thisInst.isConceptNamed(this.ac, CON_NLCARD)) {
				String isFrom = thisInst.getSingleValueFromPropertyNamed(PROP_ISFROM);

				if (isFrom.equals(this.fromInstName)) {
					if (isCardInTimeRange(thisInst)) {
						if (!hasBeenRepliedToAlready(thisInst)) {
							doMessageProcessing(thisInst);
						}
					} else {
						reportDebug("QuestionAskingTriggerHandler: Not processing card '" + thisInst.getInstanceName() + "' because it is not in the specified time range", this.ac);
					}
				}
			}
		}
	}
	
	private boolean isCardInTimeRange(CeInstance pCardInst) {
		boolean result = false;

		if (this.cutoffDuration == -1) {
			//There is no cutoff so it must be in range
			result = true;
		} else {
			long cutoffTime = System.currentTimeMillis() - (this.cutoffDuration * 1000);

			if (pCardInst.getCreationDate() > cutoffTime) {
				result = true;
			}
		}

		return result;
	}

	private void doMessageProcessing(CeInstance pCardInst) {
		String targetUserName = pCardInst.getSingleValueFromPropertyNamed(PROP_ISTO);
		CeInstance userInstance = this.ac.getIndexedEntityAccessor().getInstanceNamedOrIdentifiedAs(this.ac, targetUserName);

		if (userInstance != null) {
			if (canUserBeAskedQuestions(userInstance)) {
				String content = pCardInst.getSingleValueFromPropertyNamed(PROP_CONTENT);
				
				if (content.equals(this.targetPhrase)) {
					if (isSelectedAtRandom()) {
						if (!maxQuestionsExceededFor(userInstance)) {
							waitForSomeTime();	//The duration to wait is specified in the trigger CE
	
							askQuestionOfUser(userInstance, pCardInst);
						} else {
							reportDebug("User '" + targetUserName + "' has already been asked the maximum number of questions", this.ac);
						}
					} else {
						reportDebug("User '" + targetUserName + "' was not selected in random processing", this.ac);
					}
				}
			} else {
				reportDebug("User '" + targetUserName + "' cannot be asked questions", this.ac);
			}
		} else {
			reportError("Could not identify user instance named '" + targetUserName + "'", this.ac);
		}
	}
	
	private static boolean canUserBeAskedQuestions(CeInstance pUser) {
		boolean result = false;
		
		String propVal = pUser.getLatestValueFromPropertyNamed(PROP_CANASK);
		
		if (propVal.isEmpty()) {
			//No value means the user is authorised by default
			result = true;
		} else {
			result = new Boolean(propVal).booleanValue();
		}

		return result;
	}
	
	private void askQuestionOfUser(CeInstance pUser, CeInstance pCardInst) {
		CeInstance tgtQuestion = selectQuestionForUser(pUser);
		
		if (tgtQuestion != null) {
			String encUserName = encodeAndEncloseInQuotesIfNeeded(pUser.getInstanceName());
			String encQuesName = encodeAndEncloseInQuotesIfNeeded(tgtQuestion.getInstanceName());
			ArrayList<String> toList = new ArrayList<String>();
			toList.add(pUser.getInstanceName());
			
			//Save the CE for the new "ask" card
			saveCeForConversationCard(
					CON_ASKCARD,
					generateNewUid(),
					pCardInst.getFirstLeafConceptName(),
					pCardInst.getInstanceName(),
					this.fromConName,
					this.fromInstName,
					CON_AUTHUSER,
					toList,
					tgtQuestion.getSingleValueFromPropertyNamed(PROP_QTEXT),
					null,		//pSecCon
					0,			//pScoreVal
					null,		//pScoreExp
					null,		//pScoreType
					SRC_QUES,
					null		//pAboutIds
			);

			//Link the question to the user it was asked of
			saveCeCardText("the authorised user " + encUserName + " was asked the question " + encQuesName + ".", SRC_QUES);
		}
	}
	
	private CeInstance selectQuestionForUser(CeInstance pUser) {
		CeInstance result = null;

		//TODO: Review whether this CopyOnWriteArrayList is needed here
		CopyOnWriteArrayList<CeInstance> questionsList = new CopyOnWriteArrayList<CeInstance>(this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, CON_QUES));

		Collections.shuffle(questionsList, new Random(System.nanoTime()));

		for (CeInstance thisQues : questionsList) {
			if (result == null) {
				if (!hasUserAlreadyBeenAskedThisQuestion(thisQues, pUser)) {
					if (!hasUserAlreadyAnsweredThisQuestion(thisQues, pUser)) {
						result = thisQues;
						break;
					}
				} else {
					reportDebug("Ignoring question '" + thisQues.getInstanceName() + "' as it has already been answered by this user", this.ac);
				}
			} else {
				reportDebug("Ignoring question '" + thisQues.getInstanceName() + "' as it has already been asked of this user", this.ac);
			}
		}

		return result;
	}
	
	private boolean hasUserAlreadyBeenAskedThisQuestion(CeInstance pQuestion, CeInstance pUser) {
		boolean result = false;

		//TODO: Review whether this CopyOnWriteArrayList is needed here
		CopyOnWriteArrayList<CeInstance> existingQuestions = new CopyOnWriteArrayList<CeInstance>(pUser.getInstanceListFromPropertyNamed(this.ac, PROP_WASASKED));

		if (existingQuestions != null) {
			result = existingQuestions.contains(pQuestion);
		}
		
		return result;
	}

	private boolean hasUserAlreadyAnsweredThisQuestion(CeInstance pQuestion, CeInstance pUser) {
		boolean result = false;

		//TODO: Review whether this CopyOnWriteArrayList is needed here
		CopyOnWriteArrayList<CeInstance> answerList = new CopyOnWriteArrayList<CeInstance>(pQuestion.getInstanceListFromPropertyNamed(this.ac, PROP_ANSWER));

		for (CeInstance thisAnswer: answerList) {
			for (CeInstance answerer : thisAnswer.getInstanceListFromPropertyNamed(this.ac, PROP_ISGIVENBY)) {
				if (answerer.equals(pUser)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	private boolean hasBeenRepliedToAlready(CeInstance pCardInst) {
		//The passed card instance is already processed if it has another card linked via
		//the "is in reply to" property, and that card has the user name of this agent
		//in the "is from" property.
		
		//More succinctly:
		//This card has already been processed if any other card from this agent is already
		//in reply to it.
		boolean result = false;

		CePropertyInstance irtPi = pCardInst.getReferringPropertyInstanceNamed(PROP_IRT);

		if (irtPi != null) {
			CeInstance replier = irtPi.getRelatedInstance().getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);

			if (replier != null) {
				result = replier.equals(this.fromInst);
			}
		}

		return result;
	}

	private boolean maxQuestionsExceededFor(CeInstance pUserInstance) {
		boolean result = false;
		
		ArrayList<String> questionsAsked = pUserInstance.getValueListFromPropertyNamed(PROP_WASASKED);
		
		if (questionsAsked != null) {
			result = (questionsAsked.size() >= this.maxQuestions);
		}
		
		return result;
	}

	private boolean isSelectedAtRandom() {
		int randNum = new Random().nextInt(100);

		return (randNum <= this.percentChance);
	}

}