package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_STORE;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendNewLineToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebStore;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;

public class CeStoreRestApiStore extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private boolean storeWasCreated = false;

	public CeStoreRestApiStore(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/stores/
	 */
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			processOneElementRequest();
		} else if (this.restParts.size() >= 2) {
			String storeName = this.restParts.get(1);
			ModelBuilder tgtMb = getNamedModelBuilder(this.wc, storeName);

			if ((tgtMb == null) && (isPost()) && (this.restParts.size() == 2)) {
				//This is a 2 element POST request, so a CE Store should be created
				tgtMb = ServletStateManager.getServletStateManager().createModelBuilder(this.wc, storeName);
				this.storeWasCreated = true;
			}

			if (tgtMb != null) {
				this.wc.setModelBuilderAndCeStoreName(tgtMb);

				if (this.restParts.size() == 2) {
					processTwoElementRequest(storeName, tgtMb);
				} else {
					//Strip the first two rest parameters and send the request again, using the target store
					ArrayList<String> modifiedRestParts = new ArrayList<String>();
					modifiedRestParts.addAll(this.restParts);
					modifiedRestParts.remove(1);
					modifiedRestParts.remove(0);
					statsInResponse = CeStoreRestApi.processModifiedRestRequest(this.wc, this.request, modifiedRestParts);
				}
			} else {
				reportNotFoundError(storeName);
			}
		} else {
			reportUnhandledUrl();
		}

		return statsInResponse;
	}

	private void processOneElementRequest() {
		if (isGet()) {
			//URL = /stores
			//List all stores
			handleListAllStores();
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processTwoElementRequest(String pStoreName, ModelBuilder pMb) {
		if (isGet()) {
			//URL = /stores/{name}
			//Get store details
			handleGetStoreDetails(pStoreName, pMb);
		} else if (isPost()) {
			//Create new store
			handleCreateNewCeStore(pStoreName, pMb);
		} else if (isDelete()) {
			//Create new store
			handleDeleteCeStore(pStoreName, pMb);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void handleListAllStores() {
		if (isJsonRequest()) {
			jsonListAllStores();
		} else if (isTextRequest()) {
			textListAllStores();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllStores() {
		setStoreListAsStructuredResult(getAllModelBuilders());
	}

	private void textListAllStores() {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All stores");
		appendNewLineToSb(sb);

		for (String storeName : getAllModelBuilders().keySet()) {
			appendToSb(sb, generateTextForStore(storeName));
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleGetStoreDetails(String pStoreName, ModelBuilder pMb) {
		if (isJsonRequest()) {
			jsonGetStoreDetails(pStoreName, pMb);
		} else if (isTextRequest()) {
			textGetStoreDetails(pStoreName);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void handleCreateNewCeStore(String pStoreName, ModelBuilder pMb) {
		if (isJsonRequest()) {
			jsonCreateNewCeStore(pStoreName, pMb);
		} else if (isTextRequest()) {
			textCreateNewCeStore(pStoreName);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void handleDeleteCeStore(String pStoreName, ModelBuilder pMb) {
		if (isJsonRequest()) {
			jsonDeleteCeStore(pStoreName, pMb);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetStoreDetails(String pStoreName, ModelBuilder pMb) {
		setStoreDetailsAsStructuredResult(pStoreName, pMb);
	}

	private void jsonCreateNewCeStore(String pStoreName, ModelBuilder pMb) {
		//Just return the model builder... it had to be created earlier (in processRequest())
		setStoreDetailsAsStructuredResult(pStoreName, pMb);
	}

	private void textCreateNewCeStore(String pStoreName) {
		//Return text to say whether this store was created
		String msgText = null;

		if (this.storeWasCreated) {
			msgText = "The cestore " + pStoreName + " has been created";
		} else {
			msgText = "The cestore " + pStoreName + " existed already, so did not need to be created";
		}

		getWebActionResponse().setPlainTextPayload(this.wc, msgText);
	}

	private void jsonDeleteCeStore(String pStoreName, ModelBuilder pMb) {
		ServletStateManager.getServletStateManager().deleteModelBuilder(this.wc, pMb);
		setActionOutcomeAsStructuredResult("CE Store " + pStoreName + " has been deleted");
	}

	private void textGetStoreDetails(String pStoreName) {
		getWebActionResponse().setPlainTextPayload(this.wc, generateTextForStore(pStoreName));
	}

	private static String generateTextForStore(String pStoreName) {
		//TODO: Complete this
		return "-- Not yet implemented (" + pStoreName + ")\n\n";
	}

	private void reportNotFoundError(String pStoreName) {
		reportNotFoundError(JSONTYPE_STORE, pStoreName);
	}

	private void setStoreListAsStructuredResult(TreeMap<String, ModelBuilder> pMbs) {
		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(CeWebStore.generateSummaryListFrom(pMbs));
		} else if (isMinimalStyle()) {
			getWebActionResponse().setStructuredResult(CeWebStore.generateMinimalListFrom(pMbs));
		} else if (isNormalisedStyle()) {
			getWebActionResponse().setStructuredResult(CeWebStore.generateNormalisedListFrom(pMbs));
		} else {
			getWebActionResponse().setStructuredResult(CeWebStore.generateFullListFrom(pMbs));
		}
	}

	private void setStoreDetailsAsStructuredResult(String pStoreName, ModelBuilder pMb) {
		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(CeWebStore.generateSummaryDetailsJsonFor(pStoreName, pMb));
		} else if (isMinimalStyle()) {
			getWebActionResponse().setStructuredResult(CeWebStore.generateMinimalDetailsJsonFor(pStoreName, pMb));
		} else if (isNormalisedStyle()) {
			getWebActionResponse().setStructuredResult(CeWebStore.generateNormalisedDetailsJsonFor(pStoreName, pMb));
		} else {
			getWebActionResponse().setStructuredResult(CeWebStore.generateFullDetailsJsonFor(pStoreName, pMb));
		}
	}

}
