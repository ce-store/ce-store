package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_PROP;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_RANGE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_COMMON;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_DATATYPE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_OBJECT;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_RATIONALE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SENTENCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_PRIMARY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SECONDARY;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendNewLineToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebContainerResult;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebProperty;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.container.ContainerCommonValues;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeStoreRestApiProperty extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public CeStoreRestApiProperty(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/properties
	 * 		/properties/datatype
	 * 		/properties/object
	 * 		/properties/{name}
	 * 		/properties/{name}/sentences
	 * 		/properties/{name}/sentences/primary
	 * 		/properties/{name}/sentences/secondary
	 * 		/properties/{name}/rationale
	 * 		/properties/{name}/common
	 */
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			processOneElementRequest();
		} else {
			if (this.restParts.size() == 2) {
				statsInResponse = processTwoElementRequest();
			} else {
				String propName = this.restParts.get(1);
//				String propName = StaticFunctionsGeneral.decodeFromUrl(this.wc, rawPropName);

				CeProperty tgtProp = getModelBuilder().getPropertyFullyNamed(propName);

				if (tgtProp != null) {
					if (this.restParts.size() == 3) {
						processThreeElementRequest(tgtProp);
					} else if (this.restParts.size() == 4) {
						processFourElementRequest(tgtProp);
					} else {
						reportUnhandledUrl();
					}
				} else {
					reportNotFoundError(propName);
				}
			}
		}

		return statsInResponse;
	}

	private void processOneElementRequest() {
		//URL = /properties
		//List all properties
		handleListAllProperties();
	}

	private boolean processTwoElementRequest() {
		String qualifier = this.restParts.get(1);
		boolean statsInResponse = false;

		if (qualifier.equals(REST_DATATYPE)) {
			//List all datatype properties
			//URL = /properties/datatype
			handleListAllDatatypeProperties();
		} else if (qualifier.equals(REST_OBJECT)) {
			//List all object properties
			//URL = /properties/object
			String rangeParm = getUrlParameterValueNamed(PARM_RANGE);

			if ((rangeParm != null) && (!rangeParm.isEmpty())) {
				//All object properties with specific range
				handleListAllObjectPropertiesWithRange(rangeParm);
			} else {
				//All object properties
				handleListAllObjectProperties();
			}
		} else {
			//Get property details
			//URL = /properties/{name}
			CeProperty tgtProp = getModelBuilder().getPropertyFullyNamed(qualifier);

			if (tgtProp != null) {
				statsInResponse = handlePropertyDetailsRequest(tgtProp);
			} else {
				reportNotFoundError(qualifier);
			}
		}

		return statsInResponse;
	}

	private void processThreeElementRequest(CeProperty pTgtProp) {
		String qualifier = this.restParts.get(2);

		if (qualifier.equals(REST_SENTENCE)) {
			processThreeElementSentenceRequest(pTgtProp);
		} else if (qualifier.equals(REST_RATIONALE)) {
			processThreeElementRationaleRequest(pTgtProp);
		} else if (qualifier.equals(REST_COMMON)) {
			processThreeElementCommonRequest(pTgtProp);
		} else {
			reportUnhandledUrl();
		}
	}

	private void processThreeElementSentenceRequest(CeProperty pTgtProp) {
		if (isGet()) {
			//URL = /properties/{name}/sentences
			//List all sentences for a property
			handleListAllSentencesForProperty(pTgtProp);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processThreeElementRationaleRequest(CeProperty pTgtProp) {
		if (isGet()) {
			//URL = /properties/{name}/rationale
			//List all rationale for property
			handleListAllRationaleForProperty(pTgtProp, false);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processThreeElementCommonRequest(CeProperty pTgtProp) {
		if (isGet()) {
			//URL = /properties/{name}/common
			//List common values for property
			handleListCommonValuesForProperty(pTgtProp);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processFourElementRequest(CeProperty pTgtProp) {
		String qualifier = this.restParts.get(2);

			if (qualifier.equals(REST_SENTENCE)) {
				processFourElementSentenceRequest(pTgtProp);
			} else {
				reportUnhandledUrl();
			}
	}

	private void processFourElementSentenceRequest(CeProperty pTgtProp) {
		String qualifier = this.restParts.get(3);

		if (isGet()) {
			if (qualifier.equals(REST_PRIMARY)) {
				//URL = /properties/{name}/sentences/secondary
				//List primary sentences for property
				handleListPrimarySentencesForProperty(pTgtProp);
			} else if (qualifier.equals(REST_SECONDARY)) {
				//URL = /properties/{name}/sentences/secondary
				//List secondary sentences for property
				handleListSecondarySentencesForProperty(pTgtProp);
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void handleListAllProperties() {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonListAllProperties();
			} else if (isTextRequest()) {
				textListAllProperties();
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonListAllProperties() {
		HashSet<CeProperty> propList = getModelBuilder().getAllProperties();
		setPropertyListAsStructuredResult(propList);
	}

	private void textListAllProperties() {
		//TODO: Implement this
		reportNotYetImplementedError();
	}

	private boolean handlePropertyDetailsRequest(CeProperty pTgtProp) {
		boolean result = false;

		if (isGet()) {
			handleGetPropertyDetails(pTgtProp);
		} else if (isDelete()) {
			if (!this.wc.getModelBuilder().isLocked()) {
				handleDeleteProperty(pTgtProp);
				result = true;
			} else {
				reportError("ce-store is locked.  The delete request was ignored", this.wc);
			}
		} else {
			reportUnsupportedMethodError();
		}

		return result;
	}

	private void handleGetPropertyDetails(CeProperty pTgtProp) {
		if (isJsonRequest()) {
			jsonGetPropertyDetails(pTgtProp);
		} else if (isTextRequest()) {
			textGetPropertyDetails(pTgtProp);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void handleDeleteProperty(CeProperty pProp) {
		//TODO: Implement this (ModelBuilder.deleteProperty() not yet implemented)
		reportNotYetImplementedError("delete property for '" + pProp.formattedFullPropertyName() + "'");
	}
//		if (isJsonRequest()) {
//			jsonDeleteProperty(pProp);
//		} else if (isTextRequest()) {
//			textDeleteConcept(pProp);
//		} else {
//			reportUnsupportedFormatError();
//		}
//	}
//
//	private void jsonDeleteProperty(CeProperty pProp) {
//		getWebActionResponse().setActionOutcomeAsStructuredResult(actionDeleteProperty(pProp));
//	}
//
//	private void textDeleteConcept(CeProperty pProp) {
//		String summaryResult = actionDeleteProperty(pProp);
//
//		getWebActionResponse().setPlainTextPayload(summaryResult);
//	}
//
//	private String actionDeleteProperty(CeProperty pProp) {
//		ModelBuilder.deleteProperty(this.wc, pProp);
//
//		return "Property '" + pProp.formattedFullPropertyName() + "' has been deleted";
//	}

	private void jsonGetPropertyDetails(CeProperty pTgtProp) {
		setPropertyDetailsAsStructuredResult(pTgtProp);
	}

	private void textGetPropertyDetails(CeProperty pTgtProp) {
		//TODO: Implement this
		reportNotYetImplementedError("get property details for '" + pTgtProp.formattedFullPropertyName() + "'");
	}

	private void handleListAllDatatypeProperties() {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonListAllDatatypeProperties();
			} else if (isTextRequest()) {
				textListAllDatatypeProperties();
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonListAllDatatypeProperties() {
		ArrayList<CeProperty> propList = new ArrayList<CeProperty>();
		
		for (CeProperty thisProp : getModelBuilder().getAllProperties()) {
			if (thisProp.isDatatypeProperty()) {
				propList.add(thisProp);
			}
		}

		setPropertyListAsStructuredResult(propList);
	}

	private void textListAllDatatypeProperties() {
		//TODO: Implement this
		reportNotYetImplementedError();
	}

	private void handleListAllObjectProperties() {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonListAllObjectProperties();
			} else if (isTextRequest()) {
				textListAllObjectProperties();
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonListAllObjectProperties() {
		ArrayList<CeProperty> propList = new ArrayList<CeProperty>();
		
		for (CeProperty thisProp : getModelBuilder().getAllProperties()) {
			if (thisProp.isObjectProperty()) {
				propList.add(thisProp);
			}
		}

		setPropertyListAsStructuredResult(propList);
	}

	private void textListAllObjectProperties() {
		//TODO: Implement this
		reportNotYetImplementedError();
	}

	private void handleListAllObjectPropertiesWithRange(String pRangeName) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonListAllObjectPropertiesWithRange(pRangeName);
			} else if (isTextRequest()) {
				textListAllObjectPropertiesWithRange(pRangeName);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonListAllObjectPropertiesWithRange(String pRangeName) {
		ArrayList<CeProperty> propList = new ArrayList<CeProperty>();
		CeConcept tgtRangeCon = getModelBuilder().getConceptNamed(this.wc, pRangeName);
		
		if (tgtRangeCon != null) {
			for (CeProperty thisProp : getModelBuilder().getAllProperties()) {
				if (thisProp.isObjectProperty()) {
					if (thisProp.getRangeConcept().equalsOrHasParent(tgtRangeCon)) {
						propList.add(thisProp);
					}
				}
			}

			setPropertyListAsStructuredResult(propList);
		} else {
			reportError("Specified range concept named '" + pRangeName + "' could not be found when listing all object properties with that range", this.wc);
		}

	}

	private void textListAllObjectPropertiesWithRange(String pRangeName) {
		//TODO: Implement this
		reportNotYetImplementedError("list all object properties with range '" + pRangeName + "'");
	}

	private void handleListAllSentencesForProperty(CeProperty pTgtProp) {
		if (isJsonRequest()) {
			jsonListAllSentencesForProperty(pTgtProp);
		} else if (isTextRequest()) {
			textListAllSentencesForProperty(pTgtProp);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllSentencesForProperty(CeProperty pTgtProp) {
		setSentenceListPairAsStructuredResult(pTgtProp.listAllSentencesAsPair());
	}

	private void textListAllSentencesForProperty(CeProperty pTgtProp) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All sentences for property " + pTgtProp.formattedFullPropertyName());
		appendNewLineToSb(sb);

		for (CeSentence thisSen : pTgtProp.listAllSentences()) {
			CeStoreRestApiSentence.generateTextForSentence(this.wc, sb, thisSen, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListPrimarySentencesForProperty(CeProperty pTgtProp) {
		if (isJsonRequest()) {
			jsonListPrimarySentencesForProperty(pTgtProp);
		} else if (isTextRequest()) {
			textListPrimarySentencesForProperty(pTgtProp);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListPrimarySentencesForProperty(CeProperty pTgtProp) {
		setSentenceListAsStructuredResult(pTgtProp.listPrimarySentences());
	}

	private void textListPrimarySentencesForProperty(CeProperty pTgtProp) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All primary sentences for property " + pTgtProp.formattedFullPropertyName());
		appendNewLineToSb(sb);

		for (CeSentence thisSen : pTgtProp.getPrimarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(this.wc, sb, thisSen, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListSecondarySentencesForProperty(CeProperty pTgtProp) {
		if (isJsonRequest()) {
			jsonListSecondarySentencesForProperty(pTgtProp);
		} else if (isTextRequest()) {
			textListSecondarySentencesForProperty(pTgtProp);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListSecondarySentencesForProperty(CeProperty pTgtProp) {
		setSentenceListAsStructuredResult(pTgtProp.listSecondarySentences());
	}

	private void textListSecondarySentencesForProperty(CeProperty pTgtProp) {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- All secondary sentences for property " + pTgtProp.formattedFullPropertyName());

		for (CeSentence thisSen : pTgtProp.listSecondarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(this.wc, sb, thisSen, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListAllRationaleForProperty(CeProperty pProp, boolean pCheckPremise) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonGetPropertyRationale(pProp, pCheckPremise);
			} else if (isTextRequest()) {
				textGetPropertyRationale(pProp, pCheckPremise);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonGetPropertyRationale(CeProperty pProp, boolean pCheckPremise) {
		ArrayList<CeRationaleReasoningStep> ratList = getModelBuilder().getReasoningStepsForProperty(pProp.getPropertyName(), pCheckPremise);
		setRationaleListAsStructuredResult(ratList);
	}

	private void textGetPropertyRationale(CeProperty pProp, boolean pCheckPremise) {
		//TODO: Implement this
		reportNotYetImplementedError("get property rationale for '" + pProp.formattedFullPropertyName() + "' (" + pCheckPremise + ")");
	}

	private void handleListCommonValuesForProperty(CeProperty pProp) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonListCommonValues(pProp);
			} else if (isTextRequest()) {
				textListCommonValues(pProp);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonListCommonValues(CeProperty pProp) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);

		setCommonValuesListAsStructuredResult(sa.commonPropertyValues(pProp));
	}

	private void textListCommonValues(CeProperty pProp) {
		//TODO: Implement this
		reportNotYetImplementedError("list common values for '" + pProp.formattedFullPropertyName() + "'");
	}

	public static void generateTextForProperty(WebActionContext pWc, StringBuilder pSb, CeProperty pProp, boolean pFullStyle) {
		appendToSb(pSb, "-- Property: " + pProp.formattedFullPropertyName() + " (all sentences)");
		appendToSb(pSb, "");

		appendToSb(pSb, "-- Primary sentences");
		for (CeSentence thisSen : pProp.listPrimarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
			appendToSb(pSb, "");
		}

		appendToSb(pSb, "-- Secondary sentences\n");
		for (CeSentence thisSen : pProp.listSecondarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
			appendToSb(pSb, "");
		}

		CeInstance mmInst = pWc.getModelBuilder().getInstanceNamed(pWc, pProp.getPropertyName());
		if (mmInst != null) {

			appendToSb(pSb, "-- Meta-model sentences");

			for (CeSentence thisSen : mmInst.listAllSentences()) {
				CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
				appendToSb(pSb, "");
			}
		}
	}

	private void reportNotFoundError(String pPropName) {
		reportNotFoundError(JSONTYPE_PROP, pPropName);
	}

	private void setPropertyDetailsAsStructuredResult(CeProperty pProperty) {
		CeWebProperty propWeb = new CeWebProperty(this.wc);

		if (isDefaultStyle() || isFullStyle()) {
			getWebActionResponse().setStructuredResult(propWeb.generateFullDetailsJsonFor(pProperty));
		} else {
			getWebActionResponse().setStructuredResult(propWeb.generateSummaryDetailsJsonFor(pProperty));
		}
	}

	private void setCommonValuesListAsStructuredResult(ArrayList<ContainerCommonValues> pCommonVals) {
		getWebActionResponse().setStructuredResult(CeWebContainerResult.generateCommonValuesResultFrom(this.wc, pCommonVals));		
	}

}
