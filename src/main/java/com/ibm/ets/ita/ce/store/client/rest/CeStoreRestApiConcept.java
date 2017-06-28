package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_LAT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_LON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_CON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.VAL_UNDEFINED;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_BUCKETS;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_DISTANCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_INST;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_LAT;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_LON;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_PROPERTY;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_RANGE;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_SINCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_UNITS;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_CHILDREN;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_COUNT;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_DATATYPE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_DIRECT;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_EXACT;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_FREQUENCY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_INSTANCE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_NEARBY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_OBJECT;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_PARENTS;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_PRIMARY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_PROPERTY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_RATIONALE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SECONDARY;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_SENTENCE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.UNIT_KMS;
import static com.ibm.ets.ita.ce.store.names.MiscNames.UNIT_MILES;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendNewLineToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebConcept;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSpecial;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeStoreRestApiConcept extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CLASS_NAME = CeStoreRestApiConcept.class.getName();
	private static final String PACKAGE_NAME = CeStoreRestApiConcept.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	public CeStoreRestApiConcept(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 * 		/concepts/
	 * 		/concepts/{name}
	 * 		/concepts/{name}/children
	 * 		/concepts/{name}/children/direct
	 * 		/concepts/{name}/parents
	 * 		/concepts/{name}/parents/direct
	 * 		/concepts/{name}/properties
	 * 		/concepts/{name}/properties/datatype
	 * 		/concepts/{name}/properties/object (?range=)
	 * 		/concepts/{name}/instances (?since=)
	 * 		/concepts/{name}/instances/frequency (?property=) (?buckets=)
	 * 		/concepts/{name}/instances/count
	 * 		/concepts/{name}/instances/exact
	 * 		/concepts/{name}/sentences
	 * 		/concepts/{name}/sentences/primary
	 * 		/concepts/{name}/sentences/secondary
	 * 		/concepts/{name}/rationale
	 */
	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 1) {
			processOneElementRequest();
		} else {
			String conceptName = this.restParts.get(1);

			CeConcept tgtCon = getModelBuilder().getConceptNamed(this.wc, conceptName);

			if (tgtCon != null) {
				if (this.restParts.size() == 2) {
					statsInResponse = processTwoElementRequest(tgtCon);
				} else {
					String qualifier = this.restParts.get(2);

					if (qualifier.equals(REST_CHILDREN)) {
						if (this.restParts.size() == 3) {
							processThreeElementChildrenRequest(tgtCon);
						} else {
							if (this.restParts.size() == 4) {
								processFourElementChildrenRequest(tgtCon);
							} else {
								reportUnhandledUrl();
							}
						}
					} else if (qualifier.equals(REST_PARENTS)) {
						if (this.restParts.size() == 3) {
							processThreeElementParentRequest(tgtCon);
						} else {
							if (this.restParts.size() == 4) {
								processFourElementParentRequest(tgtCon);
							} else {
								reportUnhandledUrl();
							}
						}
					} else if (qualifier.equals(REST_PROPERTY)) {
						if (this.restParts.size() == 3) {
							processThreeElementPropertyRequest(tgtCon);
						} else {
							if (this.restParts.size() == 4) {
								processFourElementPropertyRequest(tgtCon);
							} else {
								reportUnhandledUrl();
							}
						}
					} else if (qualifier.equals(REST_INSTANCE)) {
						if (this.restParts.size() == 3) {
							statsInResponse = processThreeElementInstanceRequest(tgtCon);
						} else {
							if (this.restParts.size() == 4) {
								processFourElementInstanceRequest(tgtCon);
							} else {
								reportUnhandledUrl();
							}
						}
					} else if (qualifier.equals(REST_SENTENCE)) {
						if (this.restParts.size() == 3) {
							processThreeElementSentenceRequest(tgtCon);
						} else {
							if (this.restParts.size() == 4) {
								processFourElementSentenceRequest(tgtCon);
							} else {
								reportUnhandledUrl();
							}
						}
					} else if (qualifier.equals(REST_RATIONALE)) {
						if (this.restParts.size() == 3) {
							processThreeElementRationaleRequest(tgtCon);
						} else {
							reportUnhandledUrl();
						}
					} else {
						reportUnhandledUrl();
					}
				}
			} else {
				reportNotFoundError(conceptName);
			}
		}

		return statsInResponse;
	}

	public static ArrayList<CeInstance> makeInstanceListRequestFor(ActionContext pAc, CeConcept pCon, String pSince) {
		final String METHOD_NAME = "makeInstanceListRequestFor";

		ArrayList<CeInstance> instList = new ArrayList<CeInstance>();

		if ((pSince != null) && (!pSince.isEmpty()) && (!pSince.toLowerCase().equals(VAL_UNDEFINED))) {
			try {
				long sinceTs = new Long(pSince).longValue();
				instList = pAc.getModelBuilder().retrieveInstancesForConceptCreatedSince(pCon, sinceTs);
			} catch (NumberFormatException e) {
				reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
				//Since there was a number format exception, ignore the 'since' value and return all instances instead
				instList = pAc.getModelBuilder().retrieveAllInstancesForConcept(pCon);
			}
		} else {
			instList = pAc.getModelBuilder().retrieveAllInstancesForConcept(pCon);
		}

		return instList;
	}

	private void processOneElementRequest() {
		if (isGet()) {
			//URL = /concepts
			//List all concepts
			handleListAllConcepts();
		} else {
			reportUnsupportedMethodError();
		}
	}

	private boolean processTwoElementRequest(CeConcept pCon) {
		//URL = /concepts/{name}
		boolean statsInResponse = false;

		if (isGet()) {
			//GET concept details
			handleGetConceptDetails(pCon);
			statsInResponse = false;
		} else if (isDelete()) {
			if (!this.wc.getModelBuilder().isLocked()) {
				//DELETE concept
				handleDeleteConcept(pCon);
				statsInResponse = true;
			} else {
				reportError("ce-store is locked.  The delete request was ignored", this.wc);
			}
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private void processThreeElementChildrenRequest(CeConcept pCon) {
		if (isGet()) {
			//URL = /concepts/{name}/children
			//List all child concepts for concept
			handleListAllChildConceptsForConcept(pCon);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processThreeElementParentRequest(CeConcept pCon) {
		if (isGet()) {
			//URL = /concepts/{name}/parents
			//List all parent concepts for concept
			handleListAllParentConceptsForConcept(pCon);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processThreeElementPropertyRequest(CeConcept pCon) {
		if (isGet()) {
			//URL = /concepts/{name}/properties
			//List all properties for concept
			handleListAllPropertiesForConcept(pCon);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private boolean processThreeElementInstanceRequest(CeConcept pCon) {
		boolean statsInResponse = false;

		if (isGet()) {
			//URL = /concepts/{name}/instances
			//List all instances for concept
			handleListAllInstancesForConcept(pCon);
		} else if (isDelete()) {
			if (!this.wc.getModelBuilder().isLocked()) {
				//URL = /concepts/{name}/instances
				//List all instances for concept
				handleDeleteAllInstancesForConcept(pCon);
				statsInResponse = true;
			} else {
				reportError("ce-store is locked.  The delete request was ignored", this.wc);
			}
		} else {
			reportUnsupportedMethodError();
		}

		return statsInResponse;
	}

	private void processThreeElementSentenceRequest(CeConcept pCon) {
		if (isGet()) {
			//URL = /concepts/{name}/sentences
			//List all sentences for concept
			handleListAllSentencesForConcept(pCon);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processThreeElementRationaleRequest(CeConcept pCon) {
		if (isGet()) {
			//URL = /concepts/{name}/rationale
			//List all rationale for concept
			handleListAllRationaleForConcept(pCon, false);
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processFourElementChildrenRequest(CeConcept pCon) {
		String qualifier = this.restParts.get(3);

		if (qualifier.equals(REST_DIRECT)) {
			if (isGet()) {
				//URL = /concepts/{name}/children/direct
				//List direct child concepts for concept
				handleListDirectChildConceptsForConcept(pCon);
			} else {
				reportUnsupportedMethodError();
			}
		} else {
			reportUnhandledUrl();
		}
	}

	private void processFourElementParentRequest(CeConcept pCon) {
		String qualifier = this.restParts.get(3);

		if (qualifier.equals(REST_DIRECT)) {
			if (isGet()) {
				//URL = /concepts/{name}/parents/direct
				//List direct parent concepts for concept
				handleListDirectParentConceptsForConcept(pCon);
			} else {
				reportUnsupportedMethodError();
			}
		} else {
			reportUnhandledUrl();
		}
	}

	private void processFourElementPropertyRequest(CeConcept pCon) {
		String qualifier = this.restParts.get(3);

		if (isGet()) {
			if (qualifier.equals(REST_DATATYPE)) {
				//List all datatype properties for concept
				//URL = /concepts/{name}/properties/datatype
				handleListDatatypePropertiesForConcept(pCon);
			} else if (qualifier.equals(REST_OBJECT)) {
				//List all object properties for concept
				//URL = /concepts/{name}/properties/object
				String valueParm = getUrlParameterValueNamed(PARM_RANGE);

				if ((valueParm == null) || (valueParm.isEmpty())) {
					handleListObjectPropertiesForConcept(pCon);
				} else {
					handleListObjectPropertiesWithRangeForConcept(pCon, valueParm);
				}
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void processFourElementInstanceRequest(CeConcept pCon) {
		String qualifier = this.restParts.get(3);

		if (qualifier.equals(REST_COUNT)) {
			if (isGet()) {
				//Count instances for concept
				//URL = /concepts/{name}/instances/count
				handleCountInstancesForConcept(pCon);
			} else {
				reportUnsupportedMethodError();
			}
		} else if (qualifier.equals(REST_FREQUENCY)) {
			if (isGet()) {
				//Get frequency of instances for concept
				//URL = /concepts/{name}/instances/frequency
				handleFrequencyInstancesForConcept(pCon);
			} else {
				reportUnsupportedMethodError();
			}
		} else if (qualifier.equals(REST_NEARBY)) {
			if (isGet()) {
				//Get proximity of instances for concept
				//URL = /concepts/{name}/instances/nearby
				handleNearbyInstancesForConcept(pCon);
			} else {
				reportUnsupportedMethodError();
			}
		} else if (qualifier.equals(REST_EXACT)) {
			if (isGet()) {
				//List exact instances for concept
				//URL = /concepts/{name}/instances/exact
				handleListExactInstancesForConcept(pCon);
			} else if (isDelete()) {
				if (!this.wc.getModelBuilder().isLocked()) {
					//Delete exact instances for concept
					//URL = /concepts/{name}/instances/exact
					handleDeleteExactInstancesForConcept(pCon);
				} else {
					reportError("ce-store is locked.  The delete request was ignored", this.wc);
				}
			} else {
				reportUnsupportedMethodError();
			}
		} else {
			reportUnhandledUrl();
		}
	}

	private void processFourElementSentenceRequest(CeConcept pCon) {
		String qualifier = this.restParts.get(3);

		if (isGet()) {
			if (qualifier.equals(REST_PRIMARY)) {
				//URL = /concepts/{name}/sentences/secondary
				//List primary sentences for concept
				handleListPrimarySentencesForConcept(pCon);
			} else if (qualifier.equals(REST_SECONDARY)) {
				//URL = /concepts/{name}/sentences/secondary
				//List secondary sentences for concept
				handleListSecondarySentencesForConcept(pCon);
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}
	}

	private void handleListAllConcepts() {
		if (isJsonRequest()) {
			jsonListAllConcepts();
		} else if (isTextRequest()) {
			textListAllConcepts();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllConcepts() {
		ArrayList<CeConcept> conList = new ArrayList<CeConcept>(getModelBuilder().listAllConcepts());
		setConceptListAsStructuredResult(conList);
	}

	private void textListAllConcepts() {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "All concept details");	//TODO: Abstract this

		for (CeConcept thisCon : getModelBuilder().listAllConcepts()) {
			generateTextForConcept(this.wc, sb, thisCon, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleGetConceptDetails(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonGetConceptDetails(pCon);
		} else if (isTextRequest()) {
			textGetConceptDetails(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetConceptDetails(CeConcept pCon) {
		setConceptDetailsAsStructuredResult(pCon);
	}

	private void textGetConceptDetails(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();
		generateTextForConcept(this.wc, sb, pCon, isFullStyle());

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleDeleteConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonDeleteConcept(pCon);
		} else if (isTextRequest()) {
			textDeleteConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteConcept(CeConcept pCon) {
		setActionOutcomeAsStructuredResult(actionDeleteConcept(pCon));
	}

	private void textDeleteConcept(CeConcept pCon) {
		String summaryResult = actionDeleteConcept(pCon);
		
		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteConcept(CeConcept pCon) {
		String result = null;
		ModelBuilder.deleteConcept(this.wc, pCon);

		result = "Concept '" + pCon.getConceptName() + "' has been deleted";	//TODO: Abstract this

		return result;
	}

	private void handleListAllChildConceptsForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListAllChildConcepts(pCon);
		} else if (isTextRequest()) {
			textListAllChildConcepts(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllChildConcepts(CeConcept pCon) {
		ArrayList<CeConcept> childConcepts = pCon.retrieveAllChildren(false);
		setConceptListAsStructuredResult(childConcepts);
	}

	private void textListAllChildConcepts(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "Concept details for all children of " + pCon.getConceptName());	//TODO: Abstract this

		for (CeConcept thisCon : pCon.retrieveAllChildren(false)) {
			generateTextForConcept(this.wc, sb, thisCon, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListDirectChildConceptsForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListDirectChildConceptsForConcept(pCon);
		} else if (isTextRequest()) {
			textListDirectChildConceptsForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListDirectChildConceptsForConcept(CeConcept pCon) {
		ArrayList<CeConcept> childConcepts = pCon.listDirectChildren();
		setConceptListAsStructuredResult(childConcepts);
	}

	private void textListDirectChildConceptsForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "Concept details for direct children of " + pCon.getConceptName());	//TODO: Abstract this

		for (CeConcept thisCon : pCon.getDirectChildren()) {
			generateTextForConcept(this.wc, sb, thisCon, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListAllParentConceptsForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListAllParentConceptsForConcept(pCon);
		} else if (isTextRequest()) {
			textListAllParentConceptsForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllParentConceptsForConcept(CeConcept pCon) {
		ArrayList<CeConcept> parentConcepts = pCon.retrieveAllParents(false);
		setConceptListAsStructuredResult(parentConcepts);
	}

	private void textListAllParentConceptsForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "Concept details for all parents of " + pCon.getConceptName());	//TODO: Abstract this

		for (CeConcept thisCon : pCon.retrieveAllParents(false)) {
			generateTextForConcept(this.wc, sb, thisCon, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListDirectParentConceptsForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListDirectParentConceptsForConcept(pCon);
		} else if (isTextRequest()) {
			textListDirectParentConceptsForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListDirectParentConceptsForConcept(CeConcept pCon) {
		ArrayList<CeConcept> parentConcepts = pCon.listDirectParents();
		setConceptListAsStructuredResult(parentConcepts);
	}

	private void textListDirectParentConceptsForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "Concept details for direct parents of " + pCon.getConceptName());

		for (CeConcept thisCon : pCon.getDirectParents()) {
			generateTextForConcept(this.wc, sb, thisCon, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListAllPropertiesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListAllPropertiesForConcept(pCon);
		} else if (isTextRequest()) {
			textListAllPropertiesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllPropertiesForConcept(CeConcept pCon) {
		TreeMap<String, CeProperty> propList = null;

		propList = pCon.calculateAllProperties();

		setPropertyListAsStructuredResult(propList.values());
	}

	private void textListAllPropertiesForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "All properties for concept " + pCon.getConceptName());

		for (CeProperty thisProp : pCon.calculateAllProperties().values()) {
			CeStoreRestApiProperty.generateTextForProperty(this.wc, sb, thisProp, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListDatatypePropertiesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListDatatypePropertiesForConcept(pCon);
		} else if (isTextRequest()) {
			textListDatatypePropertiesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListDatatypePropertiesForConcept(CeConcept pCon) {
		TreeMap<String, CeProperty> allProps = null;
		ArrayList<CeProperty> filteredProps = new ArrayList<CeProperty>();

		allProps = pCon.calculateAllProperties();

		for (CeProperty thisProp : allProps.values()) {
			if (thisProp.isDatatypeProperty()) {
				filteredProps.add(thisProp);
			}
		}

		setPropertyListAsStructuredResult(filteredProps);
	}

	private void textListDatatypePropertiesForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "All datatype properties for concept " + pCon.getConceptName());

		for (CeProperty thisProp : pCon.calculateAllProperties().values()) {
			if (thisProp.isDatatypeProperty()) {
				CeStoreRestApiProperty.generateTextForProperty(this.wc, sb, thisProp, isFullStyle());
				appendNewLineToSb(sb);
			}
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListObjectPropertiesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListObjectPropertiesForConcept(pCon);
		} else if (isTextRequest()) {
			textListObjectPropertiesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListObjectPropertiesForConcept(CeConcept pCon) {
		TreeMap<String, CeProperty> allProps = null;
		ArrayList<CeProperty> filteredProps = new ArrayList<CeProperty>();

		allProps = pCon.calculateAllProperties();

		for (CeProperty thisProp : allProps.values()) {
			if (thisProp.isObjectProperty()) {
				filteredProps.add(thisProp);
			}
		}

		setPropertyListAsStructuredResult(filteredProps);
	}

	private void textListObjectPropertiesForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "All object properties for concept " + pCon.getConceptName());

		for (CeProperty thisProp : pCon.calculateAllProperties().values()) {
			if (thisProp.isObjectProperty()) {
				CeStoreRestApiProperty.generateTextForProperty(this.wc, sb, thisProp, isFullStyle());
				appendNewLineToSb(sb);
			}
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListObjectPropertiesWithRangeForConcept(CeConcept pCon, String pRangeName) {
		if (isJsonRequest()) {
			jsonListObjectPropertiesWithRangeForConcept(pCon, pRangeName);
		} else if (isTextRequest()) {
			textListObjectPropertiesWithRangeForConcept(pCon, pRangeName);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListObjectPropertiesWithRangeForConcept(CeConcept pCon, String pRangeName) {
		TreeMap<String, CeProperty> allProps = null;
		ArrayList<CeProperty> filteredProps = new ArrayList<CeProperty>();
		CeConcept tgtRangeCon = this.wc.getModelBuilder().getConceptNamed(this.wc, pRangeName);

		if (tgtRangeCon != null) {
			allProps = pCon.calculateAllProperties();

			for (CeProperty thisProp : allProps.values()) {
				if (thisProp.isObjectProperty()) {
					CeConcept propRangeCon = thisProp.getRangeConcept();
					
					if (tgtRangeCon.equalsOrHasParent(propRangeCon)) {
						filteredProps.add(thisProp);
					}
				}
			}
		}

		setPropertyListAsStructuredResult(filteredProps);
	}

	private void textListObjectPropertiesWithRangeForConcept(CeConcept pCon, String pRangeName) {
		StringBuilder sb = new StringBuilder();
		CeConcept tgtRangeCon = this.wc.getModelBuilder().getConceptNamed(this.wc, pRangeName);

		appendCeMainHeader(sb, "All object properties with range '" + pRangeName + "' for concept " + pCon.getConceptName());

		if (tgtRangeCon != null) {
			for (CeProperty thisProp : pCon.calculateAllProperties().values()) {
				if (thisProp.isObjectProperty()) {
					CeConcept propRangeCon = thisProp.getRangeConcept();
					
					if (tgtRangeCon.equalsOrHasParent(propRangeCon)) {
						CeStoreRestApiProperty.generateTextForProperty(this.wc, sb, thisProp, isFullStyle());
						appendNewLineToSb(sb);
					}
				}
			}
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListAllInstancesForConcept(CeConcept pCon) {
		String sinceParm = getUrlParameterValueNamed(PARM_SINCE);

		if (isJsonRequest()) {
			jsonListAllInstancesForConcept(pCon, sinceParm);
		} else if (isTextRequest()) {
			textListAllInstancesForConcept(pCon, sinceParm);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllInstancesForConcept(CeConcept pCon, String pSince) {
		ArrayList<CeInstance> instList = makeInstanceListRequestFor(this.wc, pCon, pSince);

		setInstanceListAsStructuredResult(instList, pCon);
	}

	private void textListAllInstancesForConcept(CeConcept pCon, String pSince) {
		StringBuilder sb = new StringBuilder();

		if (!pSince.isEmpty()) {
			long sinceTs = new Long(pSince).longValue();

			appendCeMainHeader(sb, "All instances for concept " + pCon.getConceptName() + " created since " + pSince);

			for (CeInstance thisInst : getModelBuilder().retrieveInstancesForConceptCreatedSince(pCon, sinceTs)) {
				CeStoreRestApiInstance.generateTextForInstance(this.wc, sb, thisInst, isFullStyle());
				appendNewLineToSb(sb);
			}
		} else {
			appendCeMainHeader(sb, "All instances for concept " + pCon.getConceptName());

			for (CeInstance thisInst : getModelBuilder().retrieveAllInstancesForConcept(pCon)) {
				CeStoreRestApiInstance.generateTextForInstance(this.wc, sb, thisInst, isFullStyle());
				appendNewLineToSb(sb);
			}
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleDeleteAllInstancesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonDeleteAllInstancesForConcept(pCon);
		} else if (isTextRequest()) {
			textDeleteAllInstancesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteAllInstancesForConcept(CeConcept pCon) {
		setActionOutcomeAsStructuredResult(actionDeleteAllInstancesForConcept(pCon));
	}

	private void textDeleteAllInstancesForConcept(CeConcept pCon) {
		String summaryResult = actionDeleteAllInstancesForConcept(pCon);

		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteAllInstancesForConcept(CeConcept pCon) {
		getModelBuilder().deleteAllInstancesForConcept(this.wc, pCon);

		return "All instances for concept '" + pCon.getConceptName() + "' have been deleted";
	}
	
	private void handleFrequencyInstancesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonGetFrequencyOfInstancesForConcept(pCon);
		} else if (isTextRequest()) {
			textGetFrequencyOfInstancesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void handleNearbyInstancesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonGetNearbyInstancesForConcept(pCon);
		} else if (isTextRequest()) {
			textGetNearbyInstancesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetFrequencyOfInstancesForConcept(CeConcept pCon) {
		ArrayList<CeInstance> instList = makeInstanceListRequestFor(this.wc, pCon, null);

		String propertyParm = getUrlParameterValueNamed(PARM_PROPERTY);
		String bucketsParm = getUrlParameterValueNamed(PARM_BUCKETS);

		CeStoreJsonArray jArr = CeWebSpecial.generateFrequencyArrayFrom(this.wc, pCon, instList, propertyParm, bucketsParm);
		
		getWebActionResponse().setStructuredResult(jArr);
	}

	private void textGetFrequencyOfInstancesForConcept(CeConcept pCon) {
		//TODO: Implement this
		reportNotYetImplementedError("Get frequency of instances for concept '" + pCon.getConceptName() + "'");
	}

	private void jsonGetNearbyInstancesForConcept(CeConcept pCon) {
		ArrayList<CeInstance> instList = makeInstanceListRequestFor(this.wc, pCon, null);
		CeStoreJsonObject jRoot = new CeStoreJsonObject();
		CeStoreJsonObject jCent = new CeStoreJsonObject();
		CeStoreJsonObject jDist = new CeStoreJsonObject();
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		String distanceParm = getUrlParameterValueNamed(PARM_DISTANCE);
		String unitsParm = getUrlParameterValueNamed(PARM_UNITS);
		String instParm = getUrlParameterValueNamed(PARM_INST);
		String latParm = null;
		String lonParm = null;

		if ((instParm != null) && (!instParm.isEmpty())) {
			CeInstance thisInst = this.wc.getModelBuilder().getInstanceNamed(this.wc, instParm);

			latParm = thisInst.getSingleValueFromPropertyNamed(PROP_LAT);
			lonParm = thisInst.getSingleValueFromPropertyNamed(PROP_LON);
		} else {
			latParm = getUrlParameterValueNamed(PARM_LAT);
			lonParm = getUrlParameterValueNamed(PARM_LON);
		}

		jRoot.put("centre", jCent);
		jRoot.put("distance", jDist);

		if ((distanceParm != null) && (!distanceParm.isEmpty())) {
			if ((latParm != null) && (!latParm.isEmpty())) {
				if ((lonParm != null) && (!lonParm.isEmpty())) {
					String units = null;
					double distance = -1;
					double centreLat = -1;
					double centreLon = -1;

					if ((unitsParm != null) && (!unitsParm.isEmpty())) {
						units = unitsParm.trim().toLowerCase();
					} else {
						units = UNIT_KMS;
					}

					if (units.equals(UNIT_KMS) || units.equals(UNIT_MILES)) {
						try {
							distance = new Double(distanceParm).doubleValue();

							jDist.put("value", distance);
							jDist.put("unit", units);

							if (distance <= 0) {
								reportError("Invalid distance '" + distanceParm + "' specified.  Must be a value positive number", this.wc);
							} else {
								try {
									centreLat = new Double(latParm).doubleValue();

									try {
										Double highest = null;
										Double lowest = null;
										centreLon = new Double(lonParm).doubleValue();

										jCent.put("latitude", centreLat);
										jCent.put("longitude", centreLon);

										TreeMap<Double, ArrayList<CeInstance>> nearbyInsts = CeWebSpecial.generateNearbyInstancesFrom(this.wc, pCon, instList, distance, units, centreLat, centreLon);
										SortedSet<Double> keys = new TreeSet<Double>(nearbyInsts.keySet());

										for (Double thisDist : keys) {
											if ((highest == null) || (thisDist > highest)) {
												highest = thisDist;
											}

											if ((lowest == null) || (thisDist < lowest)) {
												lowest = thisDist;
											}

											for (CeInstance thisInst : nearbyInsts.get(thisDist)) {
						                        CeWebInstance instWeb = new CeWebInstance(this.wc);
						                        CeStoreJsonObject jWrap = new CeStoreJsonObject();
						                        CeStoreJsonObject jInst = null;

						                        if ((isDefaultStyle()) || (isSummaryStyle())) {
						                            jInst = instWeb.generateSummaryDetailsJsonFor(thisInst, null, 0, false, false, null, false, false);
						                        } else if (isFullStyle()) {
						                            jInst = instWeb.generateFullDetailsJsonFor(thisInst, null, 0, false, false, null, false, false);
						                        } else {
						                            jInst = instWeb.generateMinimalDetailsJsonFor(thisInst, null, 0, false, false, null, false);
						                        }

						                        jWrap.put("distance", thisDist.doubleValue());
						                        jWrap.put("instance", jInst);

						                        jArr.add(jWrap);
											}
										}

										jRoot.put("nearest", lowest);
										jRoot.put("farthest", highest);
										jRoot.put("count", jArr.length());
										jRoot.put("result", jArr);
									} catch (NumberFormatException e) {
										reportError("Invalid longitude '" + lonParm + "' specified.  Must be a value longitude value", this.wc);
									}
								} catch (NumberFormatException e) {
									reportError("Invalid latitude '" + latParm + "' specified.  Must be a value latitude value", this.wc);
								}
							}
						} catch (NumberFormatException e) {
							reportError("Invalid distance '" + distanceParm + "' specified.  Must be a value positive number", this.wc);
						}
					} else {
						reportError("Invalid distance unit '" + units + "' specified.  Must be either '" + UNIT_KMS + "' (default) or '" + UNIT_MILES + "'", this.wc);
					}
				} else {
					reportError("Longitude parameter (" + PARM_LON + ") must be specified", this.wc);
				}
			} else {
				reportError("Latitude parameter (" + PARM_LAT + ") must be specified", this.wc);
			}
		} else {
			reportError("Distance parameter (" + PARM_DISTANCE + ") must be specified", this.wc);
		}

		getWebActionResponse().setStructuredResult(jRoot);
	}

	private void textGetNearbyInstancesForConcept(CeConcept pCon) {
		//TODO: Implement this
		reportNotYetImplementedError("Get nearby instances for concept '" + pCon.getConceptName() + "'");
	}

	private void handleCountInstancesForConcept(CeConcept pCon) {
		//TODO: Should the since parameter apply here too?

		if (isJsonRequest()) {
			jsonCountInstancesForConcept(pCon);
		} else if (isTextRequest()) {
			textCountInstancesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonCountInstancesForConcept(CeConcept pCon) {
		int instCount = actionCountAllInstancesForConcept(pCon);
		CeStoreJsonObject jCount = CeWebSpecial.generateCountSingleFrom(instCount);

		getWebActionResponse().setStructuredResult(jCount);
	}

	private void textCountInstancesForConcept(CeConcept pCon) {
		//TODO: Implement this
		reportNotYetImplementedError("Count instances for concept '" + pCon.getConceptName() + "'");
	}

	private int actionCountAllInstancesForConcept(CeConcept pCon) {
		return getModelBuilder().countAllInstancesForConcept(pCon);
	}

	private void handleListExactInstancesForConcept(CeConcept pCon) {
		//TODO: Should the since parameter apply here too?

		if (isJsonRequest()) {
			jsonListExactInstancesForConcept(pCon);
		} else if (isTextRequest()) {
			textListExactInstancesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListExactInstancesForConcept(CeConcept pCon) {
		ArrayList<CeInstance> instList = getModelBuilder().retrieveAllExactInstancesForConcept(pCon);
		setInstanceListAsStructuredResult(instList, pCon);
	}

	private void textListExactInstancesForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "Exact instances for concept " + pCon.getConceptName());

		for (CeInstance thisInst : getModelBuilder().retrieveAllExactInstancesForConcept(pCon)) {
			CeStoreRestApiInstance.generateTextForInstance(this.wc, sb, thisInst, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleDeleteExactInstancesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonDeleteExactInstancesForConcept(pCon);
		} else if (isTextRequest()) {
			textDeleteExactInstancesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonDeleteExactInstancesForConcept(CeConcept pCon) {
		setActionOutcomeAsStructuredResult(actionDeleteExactInstancesForConcept(pCon));
	}

	private void textDeleteExactInstancesForConcept(CeConcept pCon) {
		String summaryResult = actionDeleteExactInstancesForConcept(pCon);

		getWebActionResponse().setPlainTextPayload(this.wc, summaryResult);
	}

	private String actionDeleteExactInstancesForConcept(CeConcept pCon) {
		ModelBuilder.deleteExactInstancesForConcept(this.wc, pCon);

		return "Exact instances for concept '" + pCon.getConceptName() + "' have been deleted";
	}

	private void handleListAllSentencesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListAllSentencesForConcept(pCon);
		} else if (isTextRequest()) {
			textListAllSentencesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListAllSentencesForConcept(CeConcept pCon) {
		setSentenceListPairAsStructuredResult(pCon.listAllSentencesAsPair());
	}

	private void textListAllSentencesForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "All sentences for concept " + pCon.getConceptName());

		for (CeSentence thisSen : pCon.listAllSentences()) {
			CeStoreRestApiSentence.generateTextForSentence(this.wc, sb, thisSen, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListPrimarySentencesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListPrimarySentencesForConcept(pCon);
		} else if (isTextRequest()) {
			textListPrimarySentencesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListPrimarySentencesForConcept(CeConcept pCon) {
		setSentenceListAsStructuredResult(pCon.listPrimarySentences());
	}

	private void textListPrimarySentencesForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "All primary sentences for concept " + pCon.getConceptName());

		for (CeSentence thisSen : pCon.getPrimarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(this.wc, sb, thisSen, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListSecondarySentencesForConcept(CeConcept pCon) {
		if (isJsonRequest()) {
			jsonListSecondarySentencesForConcept(pCon);
		} else if (isTextRequest()) {
			textListSecondarySentencesForConcept(pCon);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonListSecondarySentencesForConcept(CeConcept pCon) {
		setSentenceListAsStructuredResult(pCon.listSecondarySentences());
	}

	private void textListSecondarySentencesForConcept(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();

		appendCeMainHeader(sb, "All secondary sentences for concept " + pCon.getConceptName());

		for (CeSentence thisSen : pCon.listSecondarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(this.wc, sb, thisSen, isFullStyle());
			appendNewLineToSb(sb);
		}

		getWebActionResponse().setPlainTextPayload(this.wc, sb.toString());
	}

	private void handleListAllRationaleForConcept(CeConcept pCon, boolean pCheckPremise) {
		if (isJsonRequest()) {
			jsonGetConceptRationale(pCon, pCheckPremise);
		} else if (isTextRequest()) {
			textGetConceptRationale(pCon, pCheckPremise);
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetConceptRationale(CeConcept pCon, boolean pCheckPremise) {
		ArrayList<CeRationaleReasoningStep> ratList = getModelBuilder().getReasoningStepsForConcept(pCon.getConceptName(), pCheckPremise);
		setRationaleListAsStructuredResult(ratList);
	}

	private void textGetConceptRationale(CeConcept pCon, boolean pCheckPremise) {
		//TODO: Implement this
		reportNotYetImplementedError("get concept rationale for '" + pCon.getConceptName() + "' (" + pCheckPremise + ")");
	}

	public static String generateTextForConcept(WebActionContext pWc, StringBuilder pSb, CeConcept pCon, boolean pFullStyle) {
		appendCeMainHeader(pSb, "Concept: " + pCon.getConceptName() + " (all sentences)");

		appendCeSubHeader(pSb, "Primary sentences");
		for (CeSentence thisSen : pCon.listPrimarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
			appendToSb(pSb, "");
		}

		appendCeSubHeader(pSb, "Secondary sentences");
		for (CeSentence thisSen : pCon.getSecondarySentences()) {
			CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
			appendToSb(pSb, "");
		}

		CeInstance mmInst = pWc.getModelBuilder().getInstanceNamed(pWc, pCon.getConceptName());
		if (mmInst != null) {
			appendCeSubHeader(pSb, "Meta-model sentences");

			for (CeSentence thisSen : mmInst.listAllSentences()) {
				CeStoreRestApiSentence.generateTextForSentence(pWc, pSb, thisSen, pFullStyle);
				appendToSb(pSb, "");
			}
		}

		return pSb.toString();
	}

	private void reportNotFoundError(String pConName) {
		reportNotFoundError(JSONTYPE_CON, pConName);
	}

	private void setConceptDetailsAsStructuredResult(CeConcept pConcept) {
		CeWebConcept conWeb = new CeWebConcept(this.wc);

		if (isDefaultStyle() || isFullStyle()) {
			getWebActionResponse().setStructuredResult(conWeb.generateFullDetailsJsonFor(pConcept));
		} else {
			getWebActionResponse().setStructuredResult(conWeb.generateSummaryDetailsJsonFor(pConcept));
		}
	}

}
