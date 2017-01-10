package com.ibm.ets.ita.ce.store.conversation.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_NLCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_TELLCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_GISTCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_USER;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_PERSON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CEUSER;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONVTHING;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_AUTHUSER;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_EXPANDCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONFCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_WHYCARD;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ISFROM;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_SECCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CANASK;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CANTELL;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CANWHY;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ISINREPLYTO;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CONTENT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_AFFILIATION;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CANEXP;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CANCONF;
import static com.ibm.ets.ita.ce.store.names.CeNames.SRC_CONV_PREFIX;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ACT_CONFIRM;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ACT_EXPAND;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ACT_TELL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.CMD_CONFIRM;
import static com.ibm.ets.ita.ce.store.names.MiscNames.CMD_EXPAND;
import static com.ibm.ets.ita.ce.store.names.MiscNames.CMD_EXPLAIN;
import static com.ibm.ets.ita.ce.store.names.MiscNames.CMD_OK;
import static com.ibm.ets.ita.ce.store.names.MiscNames.CMD_YES;
import static com.ibm.ets.ita.ce.store.names.MiscNames.FORM_CONVINIT;
import static com.ibm.ets.ita.ce.store.names.MiscNames.UNKNOWN_USER;
import static com.ibm.ets.ita.ce.store.names.MiscNames.URL_CONV_INITIALISE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.conversation.agents.ConversationTriggerHandler;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public class ConversationProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private ActionContext ac = null;
	private ConversationTriggerHandler triggerHandler = null;
	private String senderName = null;

	public ConversationProcessor(ActionContext pAc, ConversationTriggerHandler pTh) {
		this.ac = pAc;
		this.triggerHandler = pTh;
		this.senderName = this.triggerHandler.getFromInstName();
	}

	private String getSourceName() {
		return SRC_CONV_PREFIX + this.senderName;
	}

	public void dealWithThisInstance(CeInstance pConvInst) {
		if (isUserAuthorised(pConvInst)) {
			if (pConvInst.isConceptNamed(this.ac, CON_NLCARD)) {
				//Normal NL card processing
				processNlNormal(pConvInst);
			} else if (pConvInst.isConceptNamed(this.ac, CON_CONFCARD)) {
				//Confirm card processing
				processCeConfirm(pConvInst);
			} else if (pConvInst.isConceptNamed(this.ac, CON_TELLCARD)) {
				//Tell card processing
				processCeTell(pConvInst);
			} else if (pConvInst.isConceptNamed(this.ac, CON_EXPANDCARD)) {
				//Expand card processing
				processCeExpand(pConvInst);
			} else if (pConvInst.isConceptNamed(this.ac, CON_WHYCARD)) {
				//Why card processing
				processNlNormal(pConvInst);
			} else {
				//An unexpected card type was encountered.
				if (isAllowedToSeeResponse(pConvInst)) {
					generateCeConversationCard(pConvInst, ResultOfAnalysis.msgUnsupportedCardType(pConvInst));
				} else {
					msgRequestDeclined(pConvInst);
				}
			}
		} else {
			msgUserNotAuthorised(pConvInst, getFromUserName(pConvInst));
		}
	}

	private String getFromUserName(CeInstance pConvInst) {
		String result = null;

		CeInstance fromUser = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);

		if (fromUser != null) {
			result = fromUser.getInstanceName();
		}

		return result;
	}

	private boolean isUserAuthorised(CeInstance pConvInst) {
		boolean result = false;

		if (this.triggerHandler.isCheckingForAuthorisedUsers()) {
			CeInstance fromUser = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);

			if (fromUser != null) {
				result = fromUser.isConceptNamed(this.ac, CON_AUTHUSER);
			}
		} else {
			//Not checking for authorised users, so always allow
			result = true;
		}

		return result;
	}

	private void doStandardInitialisation() {
		//If the conversation request concept is not defined then request that the relevant lexical models are loaded
		//TODO: A better test would be to see if the relevant sources are loaded
		if (this.ac.getModelBuilder().getConceptNamed(this.ac, CON_CONVTHING) == null) {
			StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);

			sa.loadSentencesFromUrl(URL_CONV_INITIALISE, FORM_CONVINIT);
		}
	}

	private void processCeConfirm(CeInstance pConvInst) {
		if (isAllowedToSeeResponse(pConvInst)) {
			CeInstance fromInst = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);

			if (isAuthorisedForConfirm(fromInst)) {
				CeInstance irtInst = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISINREPLYTO);

				//CE confirm cards must be in response to a card that contains CE
				if (irtInst != null) {
					String ceText = null;

					if (!irtInst.isConceptNamed(this.ac, CON_GISTCARD)) {
						//Normal (non-gist) so the CE should be in the content
						ceText = irtInst.getSingleValueFromPropertyNamed(PROP_CONTENT);
					} else {
						//Gist - the CE should be in the secondary content
						ceText = irtInst.getSingleValueFromPropertyNamed(PROP_SECCON);
					}

					if (!ceText.isEmpty()) {
						generateCeConversationCard(pConvInst, ResultOfAnalysis.msgThankyouForCe());

						//Save the actual CE text
						this.triggerHandler.saveCeText(ceText, pConvInst);
					} else {
						generateCeConversationCard(pConvInst, ResultOfAnalysis.msgEmptyText());
					}
				} else {
					generateCeConversationCard(pConvInst, ResultOfAnalysis.msgOriginalCardNotFound(pConvInst));
				}
			} else {
				msgActNotAuthorised(pConvInst, fromInst.getInstanceName(), ACT_CONFIRM);
			}
		} else {
			msgRequestDeclined(pConvInst);
		}
	}

	private void processCeTell(CeInstance pConvInst) {
		CeInstance fromInst = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);

		if (isAuthorisedForTell(fromInst)) {
			String ceText = pConvInst.getSingleValueFromPropertyNamed(PROP_CONTENT);

			if (!ceText.isEmpty()) {
				//TODO: Need to handle cases where this is a 'raw' tell, e.g. not in response to
				//a confirm that came from NL
				generateCeConversationCard(pConvInst, ResultOfAnalysis.msgThankyouForCe());

				//Save the actual CE
				this.triggerHandler.saveCeText(ceText, pConvInst);

				String intText = InterestingThingsProcessor.generateInterestingThingsFromCe(this.ac, ceText);
				generateInterestingThingsCard(pConvInst, intText);
			} else {
				generateCeConversationCard(pConvInst, ResultOfAnalysis.msgEmptyText());
			}
		} else {
			String fromName = null;
			
			if (fromInst != null) {
				fromName = fromInst.getInstanceName();
			} else {
				fromName = UNKNOWN_USER;
			}
			
			msgActNotAuthorised(pConvInst, fromName, ACT_TELL);
		}
	}

	private static boolean isAuthorisedForConfirm(CeInstance pFromUser) {
		return checkAuthorisationFor(pFromUser, PROP_CANCONF);
	}

	private static boolean isAuthorisedForTell(CeInstance pFromUser) {
		return checkAuthorisationFor(pFromUser, PROP_CANTELL);
	}

	private static boolean isAuthorisedForExpand(CeInstance pFromUser) {
		return checkAuthorisationFor(pFromUser, PROP_CANEXP);
	}

	public static boolean isAuthorisedForWhy(CeInstance pFromUser) {
		return checkAuthorisationFor(pFromUser, PROP_CANWHY);
	}

	public static boolean isAuthorisedForAsk(CeInstance pFromUser) {
		return checkAuthorisationFor(pFromUser, PROP_CANASK);
	}

	private static boolean checkAuthorisationFor(CeInstance pFromUser, String pPropName) {
		boolean result = false;

		if (pFromUser != null) {
			String propVal = pFromUser.getLatestValueFromPropertyNamed(pPropName);

			if (propVal.isEmpty()) {
				//No value means the user is authorised by default
				result = true;
			} else {
				result = new Boolean(propVal).booleanValue();
			}
		}

		return result;
	}

	private void processCeExpand(CeInstance pConvInst) {
		if (isAllowedToSeeResponse(pConvInst)) {
			CeInstance fromInst = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);

			if (isAuthorisedForExpand(fromInst)) {
				CeInstance irtInst = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISINREPLYTO);

				if (irtInst != null) {
					String ceText = irtInst.getSingleValueFromPropertyNamed(PROP_SECCON);

					if (!ceText.isEmpty()) {
						generateCeConversationCard(pConvInst, ResultOfAnalysis.createConfirmationFor(ceText));
					} else {
						generateCeConversationCard(pConvInst, ResultOfAnalysis.msgNothingElseToSay());
					}
				} else {
					generateCeConversationCard(pConvInst, ResultOfAnalysis.msgExpandNotSpecified());
				}
			} else {
				msgActNotAuthorised(pConvInst, fromInst.getInstanceName(), ACT_EXPAND);
			}
		} else {
			msgRequestDeclined(pConvInst);
		}
	}

	private void processNlNormal(CeInstance pConvInst) {
		doStandardInitialisation();

		String convText = pConvInst.getSingleValueFromPropertyNamed(PROP_CONTENT);
		String modConvText = appendDotIfNeeded(convText);

		//First test to see if this is valid CE
		if (isValidCe(modConvText)) {
			//Valid CE
			generateCeConversationCard(pConvInst, ResultOfAnalysis.createConfirmationFor(modConvText));
		} else {
			if (isConfirmation(convText)) {
				processCeConfirm(pConvInst);
			} else {
				if (isExpansion(convText)) {
					processCeExpand(pConvInst);
				} else {
					if (!convText.isEmpty()) {
						//Process the specified text
						RawLexicalProcessor rlh = new RawLexicalProcessor(this.ac);

						dealWithConversationResult(pConvInst, rlh.processConversationText(convText));
					} else {
						//Report empty text
						generateCeConversationCard(pConvInst, ResultOfAnalysis.msgEmptyText());
					}
				}
			}
		}
	}

	private static boolean isConfirmation(String pConvText) {
		String lcConvText = pConvText.toLowerCase();

		return
			(lcConvText.equals(CMD_CONFIRM)) ||
			(lcConvText.equals(CMD_YES)) ||
			(lcConvText.equals(CMD_OK));
	}

	private static boolean isExpansion(String pConvText) {
		String lcConvText = pConvText.toLowerCase();

		return
			(lcConvText.equals(CMD_EXPAND)) ||
			(lcConvText.equals(CMD_EXPLAIN));
	}

	private static String appendDotIfNeeded(String pText) {
		String result = pText.trim();

		if (!result.endsWith(TOKEN_DOT)) {
			result += TOKEN_DOT;
		}

		return result;
	}

	private boolean isValidCe(String pText) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);

		ContainerSentenceLoadResult result = sa.validateCeSentence(pText);

		return (result.getInvalidSentenceCount() == 0) && (result.getValidSentenceCount() > 0);
	}

	private boolean isAllowedToSeeResponse(CeInstance pConvInst) {
		boolean result = false;

		if (this.triggerHandler.isCheckingNationalities()) {
			result = checkForMatchingNationalities(pConvInst);
		} else {
			result = true;
		}

		return result;
	}

	private boolean checkForMatchingNationalities(CeInstance pConvInst) {
		boolean result = false;

		if (pConvInst != null) {
			CeInstance msgSender = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);

			if (msgSender != null) {
				CeInstance affSender = msgSender.getSingleInstanceFromPropertyNamed(this.ac, PROP_AFFILIATION);

				if (affSender != null) {
					result = (affSender == this.triggerHandler.getFromAffiliation());
				}
			}
		}

		return result;
	}

	private void msgRequestDeclined(CeInstance pConvInst) {
		generateCeConversationCard(pConvInst, ResultOfAnalysis.msgDeclined());
	}

	private void msgUserNotAuthorised(CeInstance pConvInst, String pUserName) {
		generateCeConversationCard(pConvInst, ResultOfAnalysis.msgUserNotAuthorised(pUserName));
	}

	private void msgActNotAuthorised(CeInstance pConvInst, String pUserName, String pActName) {
		generateCeConversationCard(pConvInst, ResultOfAnalysis.msgActNotAuthorised(pUserName, pActName));
	}

	private void dealWithConversationResult(CeInstance pConvInst, ResultOfAnalysis pResult) {
		ResultOfAnalysis result = pResult;

		if (!result.isInfoMessage()) {
			//Policy point
			if (isAllowedToSeeResponse(pConvInst)) {
				if (!result.hasResponseCardType()) {
					if (result.hasGistText()) {
						//This is a GIST confirm result
						result.markAsGistConfirmResponse();
					} else {
						//This is a normal confirm response
						if (result.hasCeText()) {
							result.markAsConfirmResponse();
						}
					}
				}
			} else {
				//User is not authorised, so replace with a generic message instead
				result = ResultOfAnalysis.msgThankyou();
			}
		}

		generateCeConversationCard(pConvInst, result);

		String intText = InterestingThingsProcessor.generateInterestingThingsFromNl(this.ac, result);
		generateInterestingThingsCard(pConvInst, intText);
	}

	private void generateInterestingThingsCard(CeInstance pConvInst, String pInterestingText) {
		if (pInterestingText != null) {
			CeInstance irtSenderInst = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);
			String sdrName = null;

			if (irtSenderInst != null) {
				sdrName = irtSenderInst.getInstanceName();
			}

			ArrayList<String> sdrList = new ArrayList<String>();
			sdrList.add(sdrName);

			this.triggerHandler.saveCeForConversationCard(
				CON_GISTCARD,
				this.triggerHandler.generateNewUid(),
				pConvInst.getFirstLeafConceptName(),
				pConvInst.getInstanceName(),
				this.triggerHandler.getFromConName(),
				this.triggerHandler.getFromInstName(),
				conceptNameForSender(irtSenderInst),
				sdrList,
				pInterestingText,
				null,
				getSourceName(),
				null
			);
		}
	}

	private void generateCeConversationCard(CeInstance pConvInst, ResultOfAnalysis pResult) {
		CeInstance irtSenderInst = pConvInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_ISFROM);
		String sdrName = null;

		if (irtSenderInst != null) {
			sdrName = irtSenderInst.getInstanceName();
		}

		ArrayList<String> sdrList = new ArrayList<String>();
		sdrList.add(sdrName);

		this.triggerHandler.saveCeForConversationCard(
			pResult.getResponseCardType(),
			this.triggerHandler.generateNewUid(),
			pConvInst.getFirstLeafConceptName(),
			pConvInst.getInstanceName(),
			this.triggerHandler.getFromConName(),
			this.triggerHandler.getFromInstName(),
			conceptNameForSender(irtSenderInst),
			sdrList,
			pResult.calculatePrimaryContent(),
			pResult.calculateSecondaryContent(),
			getSourceName(),
			pResult.getReferencedIds()
		);
	}

	private String conceptNameForSender(CeInstance pInst) {
		String result = null;

		if (pInst != null) {
			CeConcept conUser = this.ac.getModelBuilder().getConceptNamed(this.ac, CON_USER);
			CeConcept leafCon = pInst.getFirstLeafConceptExcludingConcept(conUser);

			if (leafCon != null) {
				result = leafCon.getConceptName();
			}

			//TODO: Think of a better way
			if (result.equals(CON_CEUSER)) {
				result = CON_PERSON;
			}
		}

		return result;
	}

}
