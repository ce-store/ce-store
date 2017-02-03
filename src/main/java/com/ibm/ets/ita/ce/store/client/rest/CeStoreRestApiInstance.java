package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_INST;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_PROPNAME;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_PROPVAL;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_STEPS;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_RELINSTS;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_REFINSTS;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_ONLYPROPS;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_SPTS;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_LIMRELS;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_REFERENCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_PRIMARY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SECONDARY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SENTENCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_RATIONALE;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSpecial;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeStoreRestApiInstance extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public CeStoreRestApiInstance(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/instances
	 * 		/instances/{name}
	 * 		/instances/{name}/references
	 * 		/instances/{name}/sentences
	 * 		/instances/{name}/sentences/primary
	 * 		/instances/{name}/sentences/secondary
	 * 		/instances/{name}/rationale
	 */
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			statsInResponse = processOneElementRequest();
		} else {
			String instName = this.restParts.get(1);

			CeInstance tgtInst = getModelBuilder().getInstanceNamed(this.wc, instName);

			if (tgtInst != null) {
				if (this.restParts.size() == 2) {
					statsInResponse = processTwoElementRequest(tgtInst);
				} else if (this.restParts.size() == 3) {
					processThreeElementRequest(tgtInst);
				} else if (this.restParts.size() == 4) {
					processFourElementRequest(tgtInst);
				} else {
					reportUnhandledUrl();
				}
			} else {
				reportNotFoundError(instName);
			}
		}

		return statsInResponse;
	}

	private boolean processOneElementRequest() {
		boolean statsInResponse = false;

		if (isGet()) {
			//URL = /instances
			//List all instances
			handleListAllInstances();
		} else if (isDelete()) {
			if (!this.wc.getModelBuilder().isLocked()) {
				//URL = /instances
				//DELETE all instances
				handleDeleteAllInstances();
				statsInResponse = true;
			} else {
				reportError("ce-store is locked.  The delete request was ignored", this.wc);
			}
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private boolean processTwoElementRequest(CeInstance pTgtInst) {
		boolean statsInResponse = false;

		if (isGet()) {
			handleGetInstanceDetails(pTgtInst);
		} else if (isDelete()) {
			if (!this.wc.getModelBuilder().isLocked()) {
				//URL = /instances/{name}
				//DELETE this instances
				handleDeleteInstance(pTgtInst);
				statsInResponse = true;
			} else {
				reportError("ce-store is locked.  The delete request was ignored", this.wc);
			}
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private void processThreeElementRequest(CeInstance pTgtInst) {
		String qualifier = this.restParts.get(2);

		if (qualifier.equals(REST_SENTENCE)) {
			processThreeElementSentenceRequest(pTgtInst);
		} else if (qualifier.equals(REST_REFERENCE)) {
			processThreeElementReferenceRequest(pTgtInst);
		} else if (qualifier.equals(REST_RATIONALE)) {
			processThreeElementRationaleRequest(pTgtInst);
		} else {
			reportUnhandledUrl();
		}
	}

	private void processThreeElementSentenceRequest(CeInstance pTgtInst) {
		//URL = /instances/{name}/sentences
		//List all sentences for instance
		handleListAllSentencesForInstance(pTgtInst);
	}

	private void processThreeElementReferenceRequest(CeInstance pTgtInst) {
		if (isGet()) {
			//URL = /instances/{name}/references
			//List references to instance
			handleListReferencesToInstance(pTgtInst);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processThreeElementRationaleRequest(CeInstance pTgtInst) {
		//URL = /instances/{name}/rationale
		//List all rationale for instance
		String propName = getUrlParameterValueNamed(PARM_PROPNAME);
		String propVal = getUrlParameterValueNamed(PARM_PROPVAL);

		if ((propName.isEmpty()) || (propVal.isEmpty())) {
			handleListAllRationaleForInstance(pTgtInst, false);
		} else {
			handleListAllRationaleForInstanceWithPropertyValue(pTgtInst, propName, propVal, false);
		}
	}

	private void processFourElementRequest(CeInstance pTgtInst) {
		String qualifier = this.restParts.get(2);

		if (qualifier.equals(REST_SENTENCE)) {
			processFourElementSentenceRequest(pTgtInst);
		} else {
			reportUnhandledUrl();
		}
	}

	private void processFourElementSentenceRequest(CeInstance pTgtInst) {
		String subQualifier = this.restParts.get(3);

		if (subQualifier.equals(REST_PRIMARY)) {
			//URL = /instances/{name}/sentences/secondary
			//List primary sentences for instance
			handleListPrimarySentencesForInstance(pTgtInst);
		} else if (subQualifier.equals(REST_SECONDARY)) {
			//URL = /instances/{name}/sentences/secondary
			//List secondary sentences for instance
			handleListSecondarySentencesForInstance(pTgtInst);
		} else {
			reportUnhandledUrl();
		}
	}

	private void handleListAllInstances() {
		if (isJsonRequest()) {
			jsonListAllInstances();
		} else if (isTextRequest()) {
			textListAllInstances();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllInstances() {
		setInstanceListAsStructuredResult(getModelBuilder().listAllInstances());
	}

	private void textListAllInstances() {
		StringBuilder sb = new StringBuilder();

		for (CeInstance thisInst : getModelBuilder().listAllInstances()) {
			sb.append("-- Primary sentences for ");
			sb.append(thisInst.getInstanceName());
			sb.append("\n");
			for (CeSentence priSen : thisInst.getPrimarySentences()) {
				sb.append(priSen.getCeText(this.wc));
				sb.append("\n\n");
			}

			if (isFullStyle()) {
				//In full style also add secondary sentences
				sb.append("-- Secondary sentences for ");
				sb.append(thisInst.getInstanceName());
				sb.append("\n");
				sb.append("-- Secondary sentences\n");
				for (CeSentence secSen : thisInst.getSecondarySentences()) {
					sb.append(secSen.getCeText(this.wc));
					sb.append("\n\n");
				}
			}
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleDeleteAllInstances() {
		if (isJsonRequest()) {
			jsonDeleteAllInstances();
		} else if (isTextRequest()) {
			textDeleteAllInstances();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteAllInstances() {
		setActionOutcomeAsStructuredResult(actionDeleteAllInstances());
	}

	private void textDeleteAllInstances() {
		String summaryResult = actionDeleteAllInstances();

		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteAllInstances() {
		String result = "";
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
//		ContainerSentenceLoadResult slRes = sa.emptyInstances();
		sa.emptyInstances();

		//TODO: What to do with the sentence load result?
		result = "All instances have been deleted";

		return result;
	}

	private void handleGetInstanceDetails(CeInstance pTgtInst) {
		//URL = /instances/{name}
		//Get instance details
		if (isJsonRequest()) {
			jsonGetInstanceDetails(pTgtInst);
		} else if (isTextRequest()) {
			textGetInstanceDetails(pTgtInst);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetInstanceDetails(CeInstance pTgtInst) {
		setInstanceDetailsAsStructuredResult(pTgtInst);
	}

	private void textGetInstanceDetails(CeInstance pTgtInst) {
		StringBuilder sb = new StringBuilder();

		sb.append("-- Primary sentences for ");
		sb.append(pTgtInst.getInstanceName());
		sb.append("\n");
		for (CeSentence priSen : pTgtInst.getPrimarySentences()) {
			sb.append(priSen.getCeText(this.wc));
			sb.append("\n\n");
		}

		if (isFullStyle()) {
			//In full style also add secondary sentences
			sb.append("-- Secondary sentences for ");
			sb.append(pTgtInst.getInstanceName());
			sb.append("\n");
			for (CeSentence secSen : pTgtInst.getSecondarySentences()) {
				sb.append(secSen.getCeText(this.wc));
				sb.append("\n\n");
			}
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleDeleteInstance(CeInstance pTgtInst) {
		if (isJsonRequest()) {
			jsonDeleteInstance(pTgtInst);
		} else if (isTextRequest()) {
			textDeleteInstance(pTgtInst);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteInstance(CeInstance pTgtInst) {
		setActionOutcomeAsStructuredResult(actionDeleteInstance(pTgtInst));
	}

	private void textDeleteInstance(CeInstance pTgtInst) {
		String summaryResult = actionDeleteInstance(pTgtInst);

		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteInstance(CeInstance pTgtInst) {
		getModelBuilder().deleteInstance(this.wc, pTgtInst);

		return "The instance '" + pTgtInst.getInstanceName() + "' been deleted";
	}

	private void handleListReferencesToInstance(CeInstance pTgtInst) {
		if (isJsonRequest()) {
			jsonListReferencesToInstance(pTgtInst);
		} else if (isTextRequest()) {
			textListReferencesToInstance(pTgtInst);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListReferencesToInstance(CeInstance pTgtInst) {
		String tgtInstName = pTgtInst.getInstanceName();
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		ArrayList<ArrayList<String>> refList = sa.listReferences(tgtInstName);

		setReferenceListAsStructuredResult(tgtInstName, refList);
	}

	private void textListReferencesToInstance(CeInstance pTgtInst) {
		//TODO: Implement this
		reportNotYetImplementedError("list references to instance named '" + pTgtInst.getInstanceName() + "'");
	}

	private void handleListAllSentencesForInstance(CeInstance pTgtInst) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonListAllSentencesForInstance(pTgtInst);
			} else if (isTextRequest()) {
				textListAllSentencesForInstance(pTgtInst);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonListAllSentencesForInstance(CeInstance pTgtInst) {
		setSentenceListPairAsStructuredResult(pTgtInst.listAllSentencesAsPair());
	}

	private void textListAllSentencesForInstance(CeInstance pTgtInst) {
		StringBuilder sb = new StringBuilder();

		sb.append("-- Primary sentences for ");
		sb.append(pTgtInst.getInstanceName());
		sb.append("\n");
		for (CeSentence priSen : pTgtInst.getPrimarySentences()) {
			sb.append(priSen.getCeText(this.wc));
			sb.append("\n\n");
		}

		sb.append("-- Secondary sentences for ");
		sb.append(pTgtInst.getInstanceName());
		sb.append("\n");
		for (CeSentence secSen : pTgtInst.getSecondarySentences()) {
			sb.append(secSen.getCeText(this.wc));
			sb.append("\n\n");
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListPrimarySentencesForInstance(CeInstance pTgtInst) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonListPrimarySentencesForInstance(pTgtInst);
			} else if (isTextRequest()) {
				textListPrimarySentencesForInstance(pTgtInst);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonListPrimarySentencesForInstance(CeInstance pTgtInst) {
		setSentenceListAsStructuredResult(pTgtInst.listPrimarySentences());
	}

	private void textListPrimarySentencesForInstance(CeInstance pTgtInst) {
		StringBuilder sb = new StringBuilder();

		sb.append("-- Primary sentences for ");
		sb.append(pTgtInst.getInstanceName());
		sb.append("\n");
		for (CeSentence priSen : pTgtInst.getPrimarySentences()) {
			sb.append(priSen.getCeText(this.wc));
			sb.append("\n\n");
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListSecondarySentencesForInstance(CeInstance pTgtInst) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonListSecondarySentencesForInstance(pTgtInst);
			} else if (isTextRequest()) {
				textListSecondarySentencesForInstance(pTgtInst);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonListSecondarySentencesForInstance(CeInstance pTgtInst) {
		setSentenceListAsStructuredResult(pTgtInst.listSecondarySentences());
	}

	private void textListSecondarySentencesForInstance(CeInstance pTgtInst) {
		StringBuilder sb = new StringBuilder();

		//In full style also add secondary sentences
		sb.append("-- Secondary sentences for ");
		sb.append(pTgtInst.getInstanceName());
		sb.append("\n");
		for (CeSentence secSen : pTgtInst.getSecondarySentences()) {
			sb.append(secSen.getCeText(this.wc));
			sb.append("\n\n");
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListAllRationaleForInstance(CeInstance pInst, boolean pCheckPremise) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonGetInstanceRationale(pInst, pCheckPremise);
			} else if (isTextRequest()) {
				textGetInstanceRationale(pInst, pCheckPremise);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonGetInstanceRationale(CeInstance pInst, boolean pCheckPremise) {
		ArrayList<CeRationaleReasoningStep> ratList = getModelBuilder().getReasoningStepsForInstance(pInst.getInstanceName(), pCheckPremise);
		setRationaleListAsStructuredResult(ratList);
	}

	private void textGetInstanceRationale(CeInstance pInst, boolean pCheckPremise) {
		//TODO: Implement this
		reportNotYetImplementedError("get instance rationale for '" + pInst.getInstanceName() + "' (" + pCheckPremise + ")");
	}

	//TODO: Need to include these!
	private void handleListAllRationaleForInstanceWithPropertyValue(CeInstance pInst, String pPropName, String pPropVal, boolean pCheckPremise) {
		if (isGet()) {
			if (isJsonRequest()) {
				jsonGetPropertyWithValueRationale(pInst, pPropName, pPropVal, pCheckPremise);
			} else if (isTextRequest()) {
				textGetPropertyWithValueRationale(pInst, pPropName, pPropVal, pCheckPremise);
			} else {
				reportUnsupportedFormatError();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void jsonGetPropertyWithValueRationale(CeInstance pInst, String pPropName, String pPropVal, boolean pCheckPremise) {
		ArrayList<CeRationaleReasoningStep> ratList = getModelBuilder().getReasoningStepsForPropertyValue(pInst.getInstanceName(), pPropName, pPropVal, pCheckPremise);
		setRationaleListAsStructuredResult(ratList);
	}

	private void textGetPropertyWithValueRationale(CeInstance pInst, String pPropName, String pPropVal, boolean pCheckPremise) {
		//TODO: Implement this
		reportNotYetImplementedError("get property with value rationale for instance '" + pInst.getInstanceName() + "' and property '" + pPropName + "' with value '" + pPropVal + "' (" + pCheckPremise + ")");
	}

	public static void generateTextForInstance(WebActionContext pWc, StringBuilder pSb, CeInstance pInst, boolean pFullStyle) {
		appendToSb(pSb, "-- " + pInst.getInstanceName());
		for (CeSentence thisSen : pInst.listPrimarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
			appendToSb(pSb, "");
		}

		for (CeSentence thisSen : pInst.listSecondarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
			appendToSb(pSb, "");
		}
	}

	private void reportNotFoundError(String pInstName) {
		reportNotFoundError(JSONTYPE_INST, pInstName);
	}

	private void setInstanceDetailsAsStructuredResult(CeInstance pInstance) {
		CeWebInstance instWeb = new CeWebInstance(this.wc);
		int numSteps = getNumericUrlParameterValueNamed(PARM_STEPS, -1);
		boolean relInsts = getBooleanParameterNamed(PARM_RELINSTS, true);
		boolean refInsts = getBooleanParameterNamed(PARM_REFINSTS, true);
		boolean suppPropTypes = getBooleanParameterNamed(PARM_SPTS, false);
		String[] limRels = getListParameterNamed(PARM_LIMRELS);
		String[] onlyProps = getListParameterNamed(PARM_ONLYPROPS);

		onlyProps = mergedPropertyRestrictionsFor(onlyProps, limRels);

		if (isDefaultStyle() || isFullStyle()) {
			getWebActionResponse().setStructuredResult(instWeb.generateFullDetailsJsonFor(pInstance, onlyProps, numSteps, relInsts, refInsts, limRels, suppPropTypes, isSmartMode()));
		} else if (isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(instWeb.generateSummaryDetailsJsonFor(pInstance, onlyProps, numSteps, relInsts, refInsts, limRels, suppPropTypes, isSmartMode()));
		} else if (isMinimalStyle()){
			getWebActionResponse().setStructuredResult(instWeb.generateMinimalDetailsJsonFor(pInstance, onlyProps, numSteps, relInsts, refInsts, limRels, isSmartMode()));
		} else {
			getWebActionResponse().setStructuredResult(instWeb.generateNormalisedDetailsJsonFor(pInstance, onlyProps, numSteps, relInsts, refInsts, limRels, isSmartMode()));
		}
	}

	private void setReferenceListAsStructuredResult(String pInstName, ArrayList<ArrayList<String>> pRefList) {
		getWebActionResponse().setStructuredResult(CeWebSpecial.generateReferenceListFrom(pInstName, pRefList));
	}

}
