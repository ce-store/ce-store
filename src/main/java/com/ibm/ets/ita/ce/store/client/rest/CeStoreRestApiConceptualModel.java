package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_CONMOD;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_CONCEPT;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SOURCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SENTENCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_PROPERTY;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendNewLineToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebConceptualModel;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;

public class CeStoreRestApiConceptualModel extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public CeStoreRestApiConceptualModel(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/models
	 * 		/models/{name}
	 * 		/models/{name}/sources
	 * 		/models/{name}/sentences
	 * 		/models/{name}/concepts
	 * 		/models/{name}/properties
	*/
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			processOneElementRequest();
		} else {
			String modelName = this.restParts.get(1);
//			String modelName = StaticFunctionsGeneral.decodeFromUrl(this.wc, rawModelName);

			CeConceptualModel tgtCm = getModelBuilder().getConceptualModel(modelName);

			if (tgtCm != null) {
				if (this.restParts.size() == 2) {
					statsInResponse = processTwoElementRequest(tgtCm);
				} else if (this.restParts.size() == 3) {
					processThreeElementRequest(tgtCm);
				} else {
					reportUnhandledUrl();
				}
			} else {
				reportNotFoundError(modelName);
			}
		}

		return statsInResponse;
	}

	private void processOneElementRequest() {
		//URL = /models
		//List all conceptual models
		if (isGet()) {
			handleListConceptualModels();
		} else {
			reportUnsupportedMethodError();
		}
	}

	private boolean processTwoElementRequest(CeConceptualModel pCm) {
		//URL = /models/{name}
		boolean statsInResponse = false;

		if (isGet()) {
			//Get conceptual model details
			handleGetConceptualModelDetails(pCm);
		} else if (isDelete()) {
			if (!this.wc.getModelBuilder().isLocked()) {
				//DELETE conceptual model
				handleDeleteConceptualModel(pCm);
				statsInResponse = true;
			} else {
				reportError("ce-store is locked.  The delete request was ignored", this.wc);
			}
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private void processThreeElementRequest(CeConceptualModel pCm) {
		String qualifier = this.restParts.get(2);

		if (isGet()) {
			if (qualifier.equals(REST_SOURCE)) {
				//URL = /models/{name}/sources
				//List all sources for conceptual model
				handleListSourcesForConceptualModel(pCm);
			} else if (qualifier.equals(REST_SENTENCE)) {
				//URL = /models/{name}/sentences
				//List all sentences for conceptual model
				handleListSentencesForConceptualModel(pCm);
			} else if (qualifier.equals(REST_CONCEPT)) {
				//URL = /models/{name}/concepts
				//List all concepts for conceptual model
				handleListConceptsForConceptualModel(pCm);
			} else if (qualifier.equals(REST_PROPERTY)) {
				//URL = /models/{name}/properties
				//List all properties for conceptual model
				handleListPropertiesForConceptualModel(pCm);
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void handleListConceptualModels() {
		if (isJsonRequest()) {
			jsonListConceptualModels();
		} else if (isTextRequest()) {
			textListConceptualModels();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListConceptualModels() {
		ArrayList<CeConceptualModel> cmList = getModelBuilder().listAllConceptualModels();
		
		setConceptualModelListAsStructuredResult(cmList);
	}

	private void textListConceptualModels() {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences for all conceptual models");
		appendNewLineToSb(sb);

		for (CeConceptualModel thisCm : getModelBuilder().getAllConceptualModels().values()) {
			generateTextForConceptualModel(this.wc, sb, thisCm, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());		
	}

	private void handleGetConceptualModelDetails(CeConceptualModel pCm) {
		if (isJsonRequest()) {
			jsonGetConceptualModelDetails(pCm);
		} else if (isTextRequest()) {
			textGetConceptualModelDetails(pCm);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetConceptualModelDetails(CeConceptualModel pCm) {
		setConceptualModelDetailsAsStructuredResult(pCm);
	}

	private void textGetConceptualModelDetails(CeConceptualModel pCm) {
		StringBuilder sb = new StringBuilder();

		generateTextForConceptualModel(this.wc, sb, pCm, isFullStyle());

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleDeleteConceptualModel(CeConceptualModel pCm) {
		if (isJsonRequest()) {
			jsonDeleteConceptualModel(pCm);
		} else if (isTextRequest()) {
			textDeleteConceptualModel(pCm);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteConceptualModel(CeConceptualModel pCm) {
		setActionOutcomeAsStructuredResult(actionDeleteConceptualModel(pCm));
	}

	private void textDeleteConceptualModel(CeConceptualModel pCm) {
		String summaryResult = actionDeleteConceptualModel(pCm);
		
		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteConceptualModel(CeConceptualModel pCm) {
		ModelBuilder.deleteConceptualModel(this.wc, pCm);

		return "Conceptual model '" + pCm.getModelName() + "' has been deleted";
	}

	private void handleListSourcesForConceptualModel(CeConceptualModel pCm) {
		if (isJsonRequest()) {
			jsonListSourcesForConceptualModel(pCm);
		} else if (isTextRequest()) {
			textListSourcesForConceptualModel(pCm);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListSourcesForConceptualModel(CeConceptualModel pCm) {
		setSourceListAsStructuredResult(this, pCm.listSources());
	}

	private void textListSourcesForConceptualModel(CeConceptualModel pCm) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences for all sources for conceptual model " + pCm.getModelName());
		appendNewLineToSb(sb);

		for (CeSource thisSrc : pCm.listSources()) {
			CeStoreRestApiSource.generateTextForSource(this.wc, sb, thisSrc, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());		
	}

	private void handleListSentencesForConceptualModel(CeConceptualModel pCm) {
		if (isJsonRequest()) {
			jsonListSentencesForConceptualModel(pCm);
		} else if (isTextRequest()) {
			textListSentencesForConceptualModel(pCm);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListSentencesForConceptualModel(CeConceptualModel pCm) {
		setSentenceListAsStructuredResult(pCm.getSentences());
	}

	private void textListSentencesForConceptualModel(CeConceptualModel pCm) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences for conceptual model " + pCm.getModelName());
		appendNewLineToSb(sb);

		for (CeSource thisSrc : pCm.getSources()) {
			for (CeSentence thisSen : thisSrc.listPrimarySentences()) {
				CeStoreRestApiSentence.generateTextForSentence(this.wc, sb, thisSen, isFullStyle());
				appendNewLineToSb(sb);
			}
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListConceptsForConceptualModel(CeConceptualModel pCm) {
		if (isJsonRequest()) {
			jsonListConceptsForConceptualModel(pCm);
		} else if (isTextRequest()) {
			textListConceptsForConceptualModel(pCm);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListConceptsForConceptualModel(CeConceptualModel pCm) {
		setConceptListAsStructuredResult(pCm.listDefinedConcepts());
	}

	private void textListConceptsForConceptualModel(CeConceptualModel pCm) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences for all concepts for conceptual model " + pCm.getModelName());
		appendNewLineToSb(sb);

		for (CeConcept thisCon : pCm.getDefinedConcepts()) {
			CeStoreRestApiConcept.generateTextForConcept(this.wc, sb, thisCon, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());		
	}

	private void handleListPropertiesForConceptualModel(CeConceptualModel pCm) {
		if (isJsonRequest()) {
			jsonListPropertiesForConceptualModel(pCm);
		} else if (isTextRequest()) {
			textListPropertiesForConceptualModel(pCm);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListPropertiesForConceptualModel(CeConceptualModel pCm) {
		setPropertyListAsStructuredResult(pCm.listDefinedProperties());
	}

	private void textListPropertiesForConceptualModel(CeConceptualModel pCm) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences for all properties for conceptual model " + pCm.getModelName());
		appendNewLineToSb(sb);

		for (CeProperty thisProp : pCm.getDefinedProperties()) {
			CeStoreRestApiProperty.generateTextForProperty(this.wc, sb, thisProp, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());		
	}

	private void generateTextForConceptualModel(WebActionContext pWc, StringBuilder pSb, CeConceptualModel pCm, boolean pFullStyle) {
		appendToSb(pSb, "-- Conceptual model: " + pCm.getModelName() + " (all sentences)");
		appendNewLineToSb(pSb);

		//TODO: Link sentences directly to conceptual model.  For now get them via the source(s)
		for (CeSource thisSrc : pCm.getSources()) {
			for (CeSentence thisSen : thisSrc.listPrimarySentences()) {
				CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
				appendNewLineToSb(pSb);
			}
		}

		CeInstance mmInst = getModelBuilder().getInstanceNamed(pWc, pCm.getModelName());
		if (mmInst != null) {

			appendToSb(pSb, "-- Meta-model sentences");

			for (CeSentence thisSen : mmInst.listAllSentences()) {
				CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
				appendToSb(pSb, "");
			}
		}
	}

	private void reportNotFoundError(String pModelName) {
		reportNotFoundError(JSONTYPE_CONMOD, pModelName);
	}

	private void setConceptualModelListAsStructuredResult(Collection<CeConceptualModel> pCmList) {
		CeWebConceptualModel cmWeb = new CeWebConceptualModel(this.wc);

		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(cmWeb.generateSummaryListJsonFor(pCmList));
		} else if (isMinimalStyle()) {
			getWebActionResponse().setStructuredResult(cmWeb.generateMinimalListJsonFor(pCmList));
		} else if (isNormalisedStyle()) {
			getWebActionResponse().setStructuredResult(cmWeb.generateNormalisedListJsonFor(pCmList));
		} else {
			getWebActionResponse().setStructuredResult(cmWeb.generateFullListJsonFor(pCmList));
		}
	}

	private void setConceptualModelDetailsAsStructuredResult(CeConceptualModel pCm) {
		CeWebConceptualModel cmWeb = new CeWebConceptualModel(this.wc);

		if (isDefaultStyle() || isFullStyle()) {
			getWebActionResponse().setStructuredResult(cmWeb.generateFullDetailsJsonFor(pCm));
		} else {
			getWebActionResponse().setStructuredResult(cmWeb.generateSummaryDetailsJsonFor(pCm));
		}
	}

}
