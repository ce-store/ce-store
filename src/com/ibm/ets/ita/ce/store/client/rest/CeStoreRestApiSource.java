package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebContainerResult;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSource;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public class CeStoreRestApiSource extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String TYPE_SOURCE = "source";

	private static final String PARM_RUNRULES = "runRules";
	private static final String PARM_AGENTINSTNAME = "filterByAgentInstanceName";
	private static final String PARM_DETAIL = "filterByDetail";

	public CeStoreRestApiSource(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/sources
	 * 		/sources/{id}
	 * 		/sources/{id}/sentences
	*/
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			processOneElementRequest();
		} else {
			String srcId = this.restParts.get(1);

			CeSource tgtSrc = getModelBuilder().getSourceById(srcId);

			if (this.restParts.size() == 2) {
				statsInResponse = processTwoElementRequest(tgtSrc, srcId);
			} else if (this.restParts.size() == 3) {
				if (tgtSrc != null) {
					processThreeElementRequest(tgtSrc);
				} else {
					reportNotFoundError(srcId);
				}
			} else {
				reportUnhandledUrl();
			}
		}

		return statsInResponse;
	}

	private void processOneElementRequest() {
		if (isGet()) {
			//URL = /sources
			//List all sources
			handleListSources();
		} else {
			reportUnsupportedMethodError();
		}
	}

	private boolean processTwoElementRequest(CeSource pTgtSrc, String pSrcId) {
		boolean statsInResponse = false;

		//URL = /sources/{id}
		if (isGet()) {
			if (pTgtSrc != null) {
				//Get source details
				handleGetSourceDetails(pTgtSrc);
			} else {
				reportNotFoundError(pSrcId);
			}
		} else if (isPost()) {
			CeSource tgtSrc = null;
			if (pTgtSrc == null) {
				tgtSrc = CeSource.createNewFormSource(this.wc, "add sentences to source", pSrcId);
			} else {
				tgtSrc = pTgtSrc;
			}

			//POST source details
			handleCreateOrAddToSource(tgtSrc);
		} else if (isDelete()) {
			if (pTgtSrc != null) {
				//DELETE source details
				handleDeleteSource(pTgtSrc);
				statsInResponse = true;
			} else {
				reportNotFoundError(pSrcId);
			}
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private void processThreeElementRequest(CeSource pTgtSrc) {
		String qualifier = this.restParts.get(2);

		if (isGet()) {
			if (qualifier.equals(REST_SENTENCE)) {
				//URL = /sources/{id}/sentences
				//List all sentences for source
				handleListAllSentencesForSource(pTgtSrc);
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void handleListSources() {
		if (isJsonRequest()) {
			jsonListSources();
		} else if (isTextRequest()) {
			textListSources();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListSources() {
		String agentInstName = this.getUrlParameterValueNamed(PARM_AGENTINSTNAME);
		String detail = this.getUrlParameterValueNamed(PARM_DETAIL);

		Collection<CeSource> srcList = null;
		
		if (!agentInstName.isEmpty()) {
			srcList = getModelBuilder().getSourcesByAgentInstanceName(agentInstName);
		} else if (!detail.isEmpty()) {
			srcList = getModelBuilder().getSourcesByDetail(detail);
		} else {
			srcList = getModelBuilder().getAllSources().values();
		}
		
		setSourceListAsStructuredResult(this, srcList);
	}

	private void textListSources() {
		StringBuilder sb = new StringBuilder();
		
		appendToSb(sb, "-- All sentences for all sources\n\n");

		for (CeSource thisSrc : getModelBuilder().getAllSources().values()) {
			generateTextForSource(this.wc, sb, thisSrc, isFullStyle());
			appendToSb(sb, "");
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleGetSourceDetails(CeSource pTgtSrc) {
		if (isJsonRequest()) {
			jsonGetSourceDetails(pTgtSrc);
		} else if (isTextRequest()) {
			textGetSourceDetails(pTgtSrc);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetSourceDetails(CeSource pTgtSrc) {
		setSourceDetailsAsStructuredResult(pTgtSrc);
	}

	//List all sentences and all meta-data sentences
	private void textGetSourceDetails(CeSource pTgtSrc) {
		StringBuilder sb = new StringBuilder();

		generateTextForSource(this.wc, sb, pTgtSrc, isFullStyle());

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleCreateOrAddToSource(CeSource pTgtSrc) {
		if (isJsonRequest()) {
			jsonAddSentences(pTgtSrc);
		} else if (isTextRequest()) {
			textAddSentences(pTgtSrc);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonAddSentences(CeSource pTgtSrc) {
		ContainerResult result = actionAddSentences(pTgtSrc);

		if (result.isCeResult()) {
			boolean returnInstances = getBooleanUrlParameterValueNamed(PARM_RETINSTS, false);

			setCeResultAsStructuredResult((ContainerCeResult)result, returnInstances);
		} else if (result.isStatistics()) {
			setSentenceLoadResults((ContainerSentenceLoadResult)result);
		} else {
			reportGeneralError("Unexpected result type encountered");
		}
	}

	private void textAddSentences(CeSource pTgtSrc) {
		ContainerResult result = actionAddSentences(pTgtSrc);
		String textResult = "";

		ArrayList<String> msgLines = getWebActionResponse().getMessageLines();

		if ((msgLines != null) && (!msgLines.isEmpty())) {
			//There were messages so return them as plain text
			StringBuilder sb = new StringBuilder();

			for (String thisMsg : msgLines) {
				sb.append(thisMsg);
				sb.append("\n");
			}

			textResult = sb.toString();
		} else {
			if (result.isStatistics()) {
				textResult = ((ContainerSentenceLoadResult)result).convertToText();
			} else if (result.isCeResult()) {
				textResult = CeWebContainerResult.generateCeOnlyCeQueryResultFrom(this.wc, (ContainerCeResult)result);
			} else {
				reportGeneralError("Unexpected result type encountered");
			}
		}

		if (getWebActionResponse().hasErrors()) {
			textResult += "\n\n";
			textResult += "-- Errors:\n";

			for (String thisError : getWebActionResponse().getErrors()) {
				textResult += "-- " + thisError + "\n";
			}
		}
		
		if (getWebActionResponse().hasWarnings()) {
			textResult += "\n\n";
			textResult += "-- Warnings:\n";

			for (String thisWarning : getWebActionResponse().getWarnings()) {
				textResult += "-- " + thisWarning + "\n";
			}
		}

		getWebActionResponse().setPlainTextPayload(this.wc, textResult);
	}

	private ContainerResult actionAddSentences(CeSource pTgtSrc) {
		ContainerResult result = null;
		String ceText = getCeTextFromRequest();
		String returnCe = this.getUrlParameterValueNamed(PARM_RETCE);

		//Save sentences
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);

		boolean runRules = this.getBooleanUrlParameterValueNamed(PARM_RUNRULES, false);
		this.wc.markAsAutoExecuteRules(runRules);

		result = sa.saveCeText(ceText, pTgtSrc);

		if (returnCe.equals("true")) {
			result.setCreatedSentences(this.wc.getSessionCreations().getValidSentencesCreated());
		}

		return result;
	}

	private void handleDeleteSource(CeSource pTgtSrc) {
		if (isJsonRequest()) {
			jsonDeleteSource(pTgtSrc);
		} else if (isTextRequest()) {
			textDeleteSource(pTgtSrc);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteSource(CeSource pTgtSrc) {
		setActionOutcomeAsStructuredResult(actionDeleteSource(pTgtSrc));
	}

	private void textDeleteSource(CeSource pTgtSrc) {
		String summaryResult = actionDeleteSource(pTgtSrc);

		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteSource(CeSource pTgtSrc) {
		String srcId = pTgtSrc.getId();

		getModelBuilder().deleteSource(this.wc, srcId);		

		return "Source '" + srcId + "' has been deleted";
	}

	private void handleListAllSentencesForSource(CeSource pTgtSrc) {
		if (isJsonRequest()) {
			jsonListAllSentencesForSource(pTgtSrc);
		} else if (isTextRequest()) {
			textListAllSentencesForSource(pTgtSrc);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllSentencesForSource(CeSource pTgtSrc) {
		setSentenceListAsStructuredResult(pTgtSrc.listAllSentences());
	}

	//List all sentences defined in this source
	private void textListAllSentencesForSource(CeSource pTgtSrc) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences for source " + pTgtSrc.getId());
		appendToSb(sb, "");

		for (CeSentence thisSen : pTgtSrc.listAllSentences()) {
			CeStoreRestApiSentence.generateTextForSentence(this.wc, sb, thisSen, isFullStyle());
			appendToSb(sb, "");
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	public static void generateTextForSource(WebActionContext pWc, StringBuilder pSb, CeSource pSrc, boolean pFullStyle) {
		//TODO: Need to implement meta-model for sources to convey the annotated details below		
		appendToSb(pSb, "-- Source: " + pSrc.getId() + " (all sentences)");
		appendToSb(pSb, "-- Source type: " + pSrc.formattedType());
		appendToSb(pSb, "-- Creation date: " + pSrc.getCreationDate());
		appendToSb(pSb, "-- Detail: " + pSrc.getDetail());
		appendToSb(pSb, "");

		if (pSrc.getUserInstanceName() != null) {
			appendToSb(pSb, "-- User instance name: " + pSrc.getUserInstanceName());
		}

		if (pSrc.getAgentInstanceName() != null) {
			appendToSb(pSb, "-- Agent instance name: " + pSrc.getAgentInstanceName());
		}

		for (CeSentence thisSen : pSrc.listAllSentences()) {
			CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
			appendToSb(pSb, "");
		}

		CeInstance mmInst = pWc.getModelBuilder().getInstanceNamed(pWc, pSrc.getId());
		if (mmInst != null) {

			appendToSb(pSb, "-- Meta-model sentences");

			for (CeSentence thisSen : mmInst.listAllSentences()) {
				CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
				appendToSb(pSb, "");
			}
		}
	}

	private void reportNotFoundError(String pSrcId) {
		reportNotFoundError(TYPE_SOURCE, pSrcId);
	}

	private void setSourceDetailsAsStructuredResult(CeSource pSrc) {
		if (isDefaultStyle() || isFullStyle()) {
			CeWebSource srcWeb = new CeWebSource(this.wc);
			getWebActionResponse().setStructuredResult(srcWeb.generateFullDetailsJsonFor(pSrc));
		} else {
			getWebActionResponse().setStructuredResult(CeWebSource.generateSummaryDetailsJsonFor(pSrc));
		}
	}

}