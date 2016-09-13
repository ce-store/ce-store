package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_SEN;
import static com.ibm.ets.ita.ce.store.names.MiscNames.PREFIX_SEN;
import static com.ibm.ets.ita.ce.store.names.RestNames.ACTION_EXEC_Q;
import static com.ibm.ets.ita.ce.store.names.RestNames.ACTION_EXEC_R;
import static com.ibm.ets.ita.ce.store.names.RestNames.ACTION_PARSE;
import static com.ibm.ets.ita.ce.store.names.RestNames.ACTION_SAVE;
import static com.ibm.ets.ita.ce.store.names.RestNames.ACTION_VALIDATE;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_TYPE;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_VALIDITY;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_ENDTS;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_ACTION;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_RUNRULES;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_SUPPCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_RETCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_RETINSTS;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_STARTTS;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_RATIONALE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_ALL_TYPES;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_ALL_VALIDITIES;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_ANNOTATION;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_COMMAND;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_FACT;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_MODEL;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_NORMAL;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_PATTERN;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_QUALIFIED;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_QUERY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_RULE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_VALID;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SEN_INVALID;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendNewLineToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebContainerResult;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSentence;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeStoreRestApiSentence extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static HashSet<String> listUnknownTypeQualifiers(Set<String> pTypeQualifiers) {
		HashSet<String> result = new HashSet<String>();

		for (String thisType : pTypeQualifiers) {
			boolean matched = false;

			for (String knownType : REST_SEN_ALL_TYPES) {
				if (thisType.equals(knownType)) {
					matched = true;
				}
			}
			
			if (!matched) {
				result.add(thisType);
			}
		}

		return result;
	}

	private static HashSet<String> listUnknownValidityQualifiers(Set<String> pTypeQualifiers) {
		HashSet<String> result = new HashSet<String>();

		for (String thisType : pTypeQualifiers) {
			boolean matched = false;

			for (String knownType : REST_SEN_ALL_VALIDITIES) {
				if (thisType.equals(knownType)) {
					matched = true;
				}
			}
			
			if (!matched) {
				result.add(thisType);
			}
		}

		return result;
	}
	
	//Constructor
	public CeStoreRestApiSentence(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/sentences
	 *      /sentences?type=t&validity=v...
	 * 		/sentences/{id}
	 * 		/sentences/{id}/rationale
	 */
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			Set<String> sentenceTypes = getUrlParameterValuesNamed(PARM_TYPE);
			Set<String> sentenceValidities = getUrlParameterValuesNamed(PARM_VALIDITY);

			if (sentenceTypes.isEmpty() && sentenceValidities.isEmpty()) {
				statsInResponse = processOneElementRequest();
			} else {
				processOneElementRequestWithQualifiers(sentenceTypes, sentenceValidities);
			}
		} else {
			String senId = extractSentenceId();

			if (senId != null) {
				//This is a sentence details request of some kind
				CeSentence tgtSen = getModelBuilder().getSentence(senId);

				if (tgtSen != null) {
					if (this.restParts.size() == 2) {
						statsInResponse = processTwoElementDetailsRequest(tgtSen);
					} else if (this.restParts.size() == 3) {
						processThreeElementDetailsRequest(tgtSen);
					} else {
						reportUnhandledUrl();
					}
				} else {
					reportNotFoundError(senId);
				}
			} else {
				reportUnhandledUrl();
			}
		}

		return statsInResponse;
	}

	private boolean processOneElementRequest() {
		boolean statsInResponse = false;

		//URL = /sentences
		if (isGet()) {
			//GET - List all sentences
			handleListAllSentences();
		} else if (isPost()) {
			//POST - Add sentences
			handleAddSentences();
			statsInResponse = true;
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private boolean processTwoElementDetailsRequest(CeSentence pTgtSen) {
		boolean statsInResponse = false;

		//URL = /sentences/{id}
		if (isGet()) {
			//GET - Return sentence details
			handleGetSentenceDetails(pTgtSen);
		} else if (isDelete()) {
			//DELETE - Delete this sentence
			handleDeleteSentence(pTgtSen);
			statsInResponse = true;
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private void processThreeElementDetailsRequest(CeSentence pTgtSen) {   	
		String qualifier = this.restParts.get(2);

		if (qualifier.equals(REST_RATIONALE)) {
			//URL = /sentences/{id}/rationale
			if (isGet()) {
				//GET - return rationale details
				handleGetSentenceRationale(pTgtSen);
			} else {
				reportUnsupportedMethodError();
			}
		} else {
			reportUnhandledUrl();
		}
	}

	private String extractSentenceId() {
		String possibleSenId = this.restParts.get(1);
		return possibleSenId.startsWith(PREFIX_SEN) ? possibleSenId : null;
	}

	private void processOneElementRequestWithQualifiers(Set<String> typeQualifiers, Set<String> validityQualifiers) {
		if (isGet()) {
			//URL = /sentences?type=t&validity=v...

			HashSet<String> unknownTypeQualifiers = listUnknownTypeQualifiers(typeQualifiers);
			HashSet<String> unknownValidityQualifiers = listUnknownValidityQualifiers(validityQualifiers);

			if (unknownTypeQualifiers.size() > 0 || unknownValidityQualifiers.size() > 0) {
				reportUnexpectedQualifierError(unknownTypeQualifiers, unknownValidityQualifiers);				
			}

			ArrayList<String> qualifiers = new ArrayList<String>();
			qualifiers.addAll(typeQualifiers);
			qualifiers.addAll(validityQualifiers);

			ArrayList<CeSentence> result = getSentencesWithQualifiers(qualifiers);
			handleListSpecificSentences(result, qualifiers);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private ArrayList<CeSentence> getSentencesWithQualifiers(List<String> pQualifiers) {
		ArrayList<CeSentence> result = null; // will eventually always set to some non-null value

		if (pQualifiers.isEmpty()) {
			result = new ArrayList<CeSentence>(0);
		} else {
			String qualifier = pQualifiers.get(0);
			if (qualifier.equals(REST_SEN_MODEL)) {
				result = getModelBuilder().listAllModelSentences();
			} else if (qualifier.equals(REST_SEN_FACT)) {
				result = getModelBuilder().listAllFactSentences();
			} else if (qualifier.equals(REST_SEN_NORMAL)) {
				result = getModelBuilder().listAllNormalFactSentences();
			} else if (qualifier.equals(REST_SEN_QUALIFIED)) {
				result = getModelBuilder().listAllQualifiedFactSentences();
			} else if (qualifier.equals(REST_SEN_PATTERN)) {
				result = getModelBuilder().listAllRuleOrQuerySentences();
			} else if (qualifier.equals(REST_SEN_RULE)) {
				result = getModelBuilder().listAllRuleSentences();
			} else if (qualifier.equals(REST_SEN_QUERY)) {
				result = getModelBuilder().listAllQuerySentences();
			} else if (qualifier.equals(REST_SEN_ANNOTATION)) {
				result = getModelBuilder().listAllAnnotationSentences();
			} else if (qualifier.equals(REST_SEN_COMMAND)) {
				result = getModelBuilder().listAllCommandSentences();
			} else if (qualifier.equals(REST_SEN_VALID)) {
				result = getModelBuilder().listAllValidSentences();
			} else if (qualifier.equals(REST_SEN_INVALID)) {
				result = getModelBuilder().listAllInvalidSentences();
			}
		}
		
		for(int i=1; i<pQualifiers.size(); i++) {
			String qualifier = pQualifiers.get(i);
			for(Iterator<CeSentence> iterator = result.iterator(); iterator.hasNext();) {
				CeSentence thisSen = iterator.next();
				if (qualifier.equals(REST_SEN_MODEL)) {
					if (!thisSen.isModelSentence()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_FACT)) {
					if (!thisSen.isFactSentence()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_NORMAL)) {
					if (!thisSen.isFactSentenceNormal()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_QUALIFIED)) {
					if (!thisSen.isFactSentenceQualified()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_PATTERN)) {
					if (!thisSen.isPatternSentence()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_RULE)) {
					if (!thisSen.isRuleSentence()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_QUERY)) {
					if (!thisSen.isQuerySentence()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_ANNOTATION)) {
					if (!thisSen.isAnnotationSentence()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_COMMAND)) {
					if (!thisSen.isCommandSentence()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_VALID)) {
					if (!thisSen.isValid()) {
						iterator.remove();
					}
				} else if (qualifier.equals(REST_SEN_INVALID)) {
					if (thisSen.isValid()) {
						iterator.remove();
					}					
				}
			}
		}
		
		return result;
	}
					
	private void handleListAllSentences() {
		if (isJsonRequest()) {
			jsonListAllSentences();
		} else if (isTextRequest()) {
			textListAllSentences();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllSentences() {
		setSentenceListAsStructuredResult(getModelBuilder().listAllSentences());
	}

	private void textListAllSentences() {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences");
		appendNewLineToSb(sb);

		for (CeSentence thisSen : getModelBuilder().listAllSentences()) {
			appendToSb(sb, thisSen.getCeText(this.wc));
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleAddSentences() {
		if (isJsonRequest()) {
			jsonAddSentences();
		} else if (isTextRequest()) {
			textAddSentences();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonAddSentences() {
		ContainerResult result = actionAddSentences();

		if (result.isCeResult()) {
			boolean returnInstances = getBooleanUrlParameterValueNamed(PARM_RETINSTS, false);

			setCeResultAsStructuredResult((ContainerCeResult)result, false, returnInstances);
		} else if (result.isStatistics()) {
			setSentenceLoadResults((ContainerSentenceLoadResult)result);
		} else {
			reportGeneralError("Unexpected result type encountered");
		}
	}

	private void textAddSentences() {
		ContainerResult result = actionAddSentences();

		if (result.isStatistics()) {
			getWebActionResponse().setPlainTextPayload(this.wc, ((ContainerSentenceLoadResult)result).convertToText());
		} else if (result.isCeResult()) {
			getWebActionResponse().setPlainTextPayload(this.wc, CeWebContainerResult.generateCeOnlyCeQueryResultFrom(this.wc, (ContainerCeResult)result));
		} else {
			reportGeneralError("Unexpected result type encountered");
		}
	}

	private ContainerResult actionAddSentences() {
		ContainerResult result = null;
		String actionParm = getUrlParameterValueNamed(PARM_ACTION);
		String ceText = getCeTextFromRequest();
		String startTs = getUrlParameterValueNamed(PARM_STARTTS);
		String endTs = getUrlParameterValueNamed(PARM_ENDTS);
		boolean returnCe = getBooleanUrlParameterValueNamed(PARM_RETCE, false);

		if (returnCe) {
			this.wc.markAsKeepingSentences();
		}

		if ((actionParm == null) || (actionParm.isEmpty()) || (actionParm.equals(ACTION_SAVE))) {
			//Create a new source
			CeSource newSrc = CeSource.createNewFormSource(this.wc, "add sentences", null);

			//Save sentences
			StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);

			boolean runRules = getBooleanUrlParameterValueNamed(PARM_RUNRULES, this.wc.getCeConfig().getAutoRunRules());
			this.wc.markAsAutoExecuteRules(runRules);

			result = sa.saveCeText(ceText, newSrc);
		} else if (actionParm.equals(ACTION_VALIDATE)) {
			//Validate sentences
			StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
			result = sa.validateCeSentence(ceText);
		} else if (actionParm.equals(ACTION_PARSE)) {
			//Parse sentences

			//TODO: Implement this
//				StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
//				HashSet<String> instNames = sa.ParseCeSentence(ceText);

			reportNotYetImplementedError();
			result = ContainerSentenceLoadResult.createWithZeroValues("actionAddSentences(1)");
		} else if (actionParm.equals(ACTION_EXEC_Q)) {
			//Execute query sentence
			boolean suppressCE = getBooleanUrlParameterValueNamed(PARM_SUPPCE, false);

			StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
			result = sa.executeUserSpecifiedCeQuery(ceText, suppressCE, startTs, endTs);
		} else if (actionParm.equals(ACTION_EXEC_R)) {
			//Execute rule sentence
			StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
			result = sa.executeUserSpecifiedCeRule(ceText, startTs, endTs);
		} else {
			reportGeneralError("Unknown action '" + actionParm + "' when saving CE sentences");
			result = ContainerSentenceLoadResult.createWithZeroValues("actionAddSentences(2)");
		}

		if (returnCe) {
			result.setCreatedSentences(this.wc.getSessionCreations().getAllValidSessionSentences());
		}

		return result;
	}

	private void handleListSpecificSentences(ArrayList<CeSentence> pSenList, ArrayList<String> pQualifiers) {
		if (isJsonRequest()) {
			jsonListSpecificSentences(pSenList);
		} else if (isTextRequest()) {
			textListSpecificSentences(pSenList, pQualifiers);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListSpecificSentences(ArrayList<CeSentence> pSenList) {
		setSentenceListAsStructuredResult(pSenList);
	}

	private void textListSpecificSentences(ArrayList<CeSentence> pSenList, ArrayList<String> pQualifiers) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences matching filters: " + calculateFilterTextFrom(pQualifiers));
		appendNewLineToSb(sb);

		for (CeSentence thisSen : pSenList) {
			appendToSb(sb, thisSen.getCeText(this.wc));
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private static String calculateFilterTextFrom(ArrayList<String> pQualifiers) {
		String result = "";
		String sepVal = "";

		for (String thisFilter : pQualifiers) {
			result += sepVal + thisFilter;
			sepVal = ", ";
		}

		return result;
	}

	private void handleGetSentenceDetails(CeSentence pSen) {
		//URL = /sentences/{id}
		//Get sentence details
		if (isJsonRequest()) {
			jsonGetSentenceDetails(pSen);
		} else if (isTextRequest()) {
			textGetSentenceDetails(pSen);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetSentenceDetails(CeSentence pSen) {
		setSentenceDetailsAsStructuredResult(pSen);
	}

	private void textGetSentenceDetails(CeSentence pSen) {
		StringBuilder sb = new StringBuilder();

		generateTextForSentence(this.wc, sb, pSen, isFullStyle());

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleDeleteSentence(CeSentence pSen) {
		//URL = /sentences/{id}
		//DELETE sentence
		if (isJsonRequest()) {
			jsonDeleteSentence(pSen);
		} else if (isTextRequest()) {
			textDeleteSentence(pSen);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteSentence(CeSentence pSen) {
		setActionOutcomeAsStructuredResult(actionDeleteSentence(pSen));
	}

	private void textDeleteSentence(CeSentence pSen) {
		String summaryResult = actionDeleteSentence(pSen);

		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteSentence(CeSentence pSen) {
		String result = "";
		getModelBuilder().deleteSingleSentence(this.wc, pSen.formattedId());

		result = "Sentence '" + pSen.formattedId() + "' has been deleted";

		return result;
	}

	private void handleGetSentenceRationale(CeSentence pSen) {
		if (isJsonRequest()) {
			jsonGetSentenceRationale(pSen);
		} else if (isTextRequest()) {
			textGetSentenceRationale(pSen);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetSentenceRationale(CeSentence pSen) {
		ArrayList<CeRationaleReasoningStep> ratList = getModelBuilder().getReasoningStepsForSentence(pSen);
		setRationaleListAsStructuredResult(ratList);
	}

	private void textGetSentenceRationale(CeSentence pSen) {
		//TODO: Implement this
		reportNotYetImplementedError("get sentence rationale for sentence '" + pSen.getId() + "'");
	}

	public static void generateTextForSentence(WebActionContext pWc, StringBuilder pSb, CeSentence pSen, boolean pFullStyle) {
		if (pFullStyle) {
			appendToSb(pSb, "-- Sentence: " + pSen.formattedId());
		}

		appendToSb(pSb, pSen.getCeText(pWc));
	}

	private void reportUnexpectedQualifierError(Set<String> invalidSentenceTypes, Set<String> invalidSentenceValidities) {
		String typeMessage = "";
		for (String qualifier : invalidSentenceTypes) {
			typeMessage += ", " + PARM_TYPE + "=" + qualifier;
		}
		
		String validityMessage = "";
		for (String qualifier : invalidSentenceValidities) {
			validityMessage += ", " + PARM_VALIDITY + "=" + qualifier;
		}

		String message = "Unexpected sentence qualifier(s)" + typeMessage + validityMessage;		
		reportError(message, this.wc);
	}

	private void reportNotFoundError(String pSenId) {
		reportNotFoundError(JSONTYPE_SEN, pSenId);
	}

	private void setSentenceDetailsAsStructuredResult(CeSentence pSen) {
		CeWebSentence senWeb = new CeWebSentence(this.wc);

		if (isDefaultStyle() || isFullStyle()) {
			getWebActionResponse().setStructuredResult(senWeb.generateFullJson(pSen, null));
		} else {
			getWebActionResponse().setStructuredResult(senWeb.generateSummaryJson(pSen, null));
		}
	}

}
