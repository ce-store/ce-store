package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_CONFIG;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_JSON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonParser;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.importer.JsonImporter;

public class CeStoreRestApiSpecialImport extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CONF_DPN = "depluralisePropertyNames";
	private static final String CONF_FLC = "forceLowerCase";
	private static final String CONF_IBV = "ignoreBlankValues";
	private static final String CONF_SCC = "splitCamelCase";
	private static final String CONF_RS = "replaceSeparators";
	private static final String CONF_CONS = "concepts";
	private static final String CONF_PROPS = "properties";
	private static final String CONF_TP = "targetProperty";
	private static final String CONF_PC = "parentConcept";
	private static final String CONF_AANP = "arrayAsNamedProperties";
	private static final String CONF_IC = "ignoreConcept";
	private static final String CONF_IP = "ignoreProperty";
	private static final String CONF_SP = "suppressProperty";
	private static final String CONF_MTP = "moveToParent";
	private static final String CONF_SI = "startingId";
	private static final String CONF_GM = "generateModel";
	private static final String CONF_GI = "generateInstances";
	private static final String CONF_MI = "maxInstances";
	private static final String CONF_CS = "ceStyle";
	private static final String CONF_TyP = "typeProperty";
	private static final String CONF_IdP = "idProperty";
	private static final String CONF_SaC = "saveCE";
	private static final String CONF_ShC = "showCE";
	private static final String CONF_RC = "rangeConcept";

	private static final String VAL_TRUE = "true";
	private static final String VAL_FALSE = "false";

	private static final String CONNECTOR_THAT = "that\n";
	private static final String CONNECTOR_AND = "and\n";
	private static final String CONNECTOR_FINISH = ".\n\n";

	private static final String TYPE_VALUE = "value";
	private static final String DEFAULT_ID_PREFIX = "imported_";
	private static final String DEFAULT_CESTYLE = "normalised";
	
	private static final String CLASS_STRING = "java.lang.String";
	private static final String CLASS_INTEGER = "java.lang.Integer";
	private static final String CLASS_FLOAT = "java.lang.Float";
	private static final String CLASS_BOOLEAN = "java.lang.Boolean";
	private static final String CLASS_JSONARRAY = "com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray";
	private static final String CLASS_JSONOBJECT = "com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject";

	private CeStoreJsonObject config = null;
	private StringBuilder sbModel = new StringBuilder();
	private StringBuilder sbFacts = new StringBuilder();

	private TreeMap<String, String> configGeneral = new TreeMap<String, String>();
	private TreeMap<String, TreeMap<String, String>> configConcepts = new TreeMap<String, TreeMap<String, String>>();
	private TreeMap<String, TreeMap<String, String>> configProperties = new TreeMap<String, TreeMap<String, String>>();
	private ArrayList<ArrayList<String>> conceptInheritance = new ArrayList<ArrayList<String>>();

	private ArrayList<JsonImporter> jis = new ArrayList<JsonImporter>();
	private int instanceCounter = 0;
	private int variableCounter = 0;

	public CeStoreRestApiSpecialImport(WebActionContext pWc, ArrayList<String> pRestParts,
			HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	/*
	 * Supported requests:
	 *   /special/import/json
	 */
	public boolean processRequest() {
		if (this.restParts.size() == 3) {
			processThreeElementImportRequest();
		} else {
			reportUnhandledUrl();
		}

		return false;
	}

	private void processThreeElementImportRequest() {
		String strResult = null;
		long st = System.currentTimeMillis();
		String command = this.restParts.get(2);
		String payload = getTextFromRequest();
		String parmConfig = getParameterNamed(PARM_CONFIG);
//		boolean plainText = true;

		if (isPost()) {
			if (command.equals(REST_JSON)) {
				processJsonImport(payload, st, parmConfig);

				strResult = this.sbModel.toString();
				strResult += this.sbFacts.toString();
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}

//		if (plainText) {
			getWebActionResponse().setIsPlainTextResponse(true);
			getWebActionResponse().setPlainTextPayload(this.wc, strResult);
//		} else {
//			getWebActionResponse().setPayloadTo(result);
//		}
	}

	public String generateNewInstanceId() {
		return DEFAULT_ID_PREFIX + ++this.instanceCounter;
	}

	private void processJsonImport(String pPayload, long pStartTime, String pParmConfig) {		
		CeStoreJsonParser sjp = new CeStoreJsonParser(this.wc, pPayload);
		sjp.parse();

		extractConfig(pParmConfig);

		if (sjp.hasRootJsonObject()) {
			CeStoreJsonObject jo = sjp.getRootJsonObject();
			JsonImporter ji = createNewJsonImporter();

			processJsonObject(ji, jo, null);
		} else {
			CeStoreJsonArray ja = sjp.getRootJsonArray();
			processJsonArray(null, ja, null);
		}

		calculateAllCeText();
	}
	
	private void extractConfig(String pParmConfig) {
		if (pParmConfig != null) {
			CeStoreJsonParser sjpConfig = new CeStoreJsonParser(this.wc, pParmConfig);
			sjpConfig.parse();
			this.config = sjpConfig.getRootJsonObject();

			populateGeneralConfig();
			populateConcepts();
			populateProperties();
		}
	}
	
	private void populateGeneralConfig() {
		if (this.config != null) {
			populateDepluralisePropertyNames();
			populateForceLowerCase();
			populateSplitCamelCase();
			populateReplaceSeparators();
			populateStartingId();
			populateGenerateModel();
			populateGenerateInstances();
			populateIgnoreBlankValues();
			populateMaxInstances();
			populateSaveCE();
			populateShowCE();
		}
	}

	private void populateDepluralisePropertyNames() {
		populateFromBooleanParameter(CONF_DPN);
	}

	private void populateForceLowerCase() {
		populateFromBooleanParameter(CONF_FLC);
	}

	private void populateIgnoreBlankValues() {
		populateFromBooleanParameter(CONF_IBV);
	}

	private void populateSplitCamelCase() {
		populateFromBooleanParameter(CONF_SCC);
	}

	private void populateReplaceSeparators() {
		populateFromBooleanParameter(CONF_RS);
	}

	private void populateGenerateModel() {
		populateFromBooleanParameter(CONF_GM);
	}

	private void populateSaveCE() {
		populateFromBooleanParameter(CONF_SaC);
	}

	private void populateShowCE() {
		populateFromBooleanParameter(CONF_ShC);
	}

	private void populateGenerateInstances() {
		populateFromBooleanParameter(CONF_GI);
	}

	private void populateMaxInstances() {
		populateFromNumberParameter(CONF_MI);
	}

	private void populateStartingId() {
		Float parmVal = this.config.getNumber(CONF_SI);

		if (parmVal != null) {
			int intVal = parmVal.intValue();
			
			if (intVal >= 0) {
				this.instanceCounter = intVal;
			}
		}
	}

	private void populateFromBooleanParameter(String pParmName) {
		try {
			Boolean parmVal = (Boolean)this.config.get(this.wc, pParmName);

			if (parmVal != null) {
				if (parmVal) {
					this.configGeneral.put(pParmName, VAL_TRUE);
				} else {
					this.configGeneral.put(pParmName, VAL_FALSE);
				}
			}
		} catch (ClassCastException e) {
			reportError("Boolean was expected but '" + this.config.get(this.wc, pParmName).getClass().getName() + "' found in property " + pParmName, this.wc);
		}
	}

	private void populateFromNumberParameter(String pParmName) {
		try {
			Float parmVal = (Float)this.config.get(this.wc, pParmName);

			if (parmVal != null) {
				this.configGeneral.put(pParmName, parmVal.toString());
			}
		} catch (ClassCastException e) {
			reportError("Number was expected but '" + this.config.get(this.wc, pParmName).getClass().getName() + "' found in property " + pParmName, this.wc);
		}
	}

	private void populateConcepts() {
		if (this.config != null) {
			CeStoreJsonObject jProps = this.config.getJsonObject(CONF_CONS);

			if (jProps != null) {
				for (String thisKey : jProps.keySet()) {
					TreeMap<String, String> thisEntry = new TreeMap<String, String>();
					CeStoreJsonObject jObj = jProps.getJsonObject(thisKey);
					
					if (jObj != null) {
						for (String key : jObj.keySet()) {
							Object thisVal = jObj.get(this.wc, key);
							
							if (isString(thisVal)) {
								thisEntry.put(key, (String)thisVal);
							} else if (isBoolean(thisVal)) {
								thisEntry.put(key, getBooleanTextFrom(jObj, key));							
							} else {
								reportError("Unhandled type for '" + key + "' in concept config", this.wc);
							}
						}
					}
	
					this.configConcepts.put(thisKey, thisEntry);
				}
			}
		}
	}

	private void populateProperties() {
		if (this.config != null) {
			CeStoreJsonObject jProps = this.config.getJsonObject(CONF_PROPS);

			if (jProps != null) {
				for (String thisKey : jProps.keySet()) {
					TreeMap<String, String> thisEntry = new TreeMap<String, String>();
					CeStoreJsonObject jObj = jProps.getJsonObject(thisKey);
					if (jObj != null) {
						for (String key : jObj.keySet()) {
							Object thisVal = jObj.get(this.wc, key);
							
							if (isString(thisVal)) {
								thisEntry.put(key, (String)thisVal);
							} else if (isBoolean(thisVal)) {
								thisEntry.put(key, getBooleanTextFrom(jObj, key));							
							} else {
								reportError("Unhandled type for '" + key + "' in property config", this.wc);
							}
						}
					}

					this.configProperties.put(thisKey, thisEntry);
				}
			}
		}
	}
	
	private String getBooleanTextFrom(CeStoreJsonObject pObj, String pPropName) {
		String result = "";

		Object rawVal = pObj.get(this.wc, pPropName);

		if (rawVal != null) {
			if ((boolean)rawVal) {
				result = VAL_TRUE;
			} else {
				result = VAL_FALSE;
			}
		}

		return result;
	}

	private JsonImporter createNewJsonImporter() {
		JsonImporter ji = new JsonImporter(this, null, null, null, false);
		this.jis.add(ji);

		return ji;
	}

	private JsonImporter createNewJsonImporterFor(JsonImporter pParentJi, String pPropName) {
		boolean isSuppressed = suppressProperty(pPropName);
		String tgtPropName = normalisePropertyName(pPropName);
		JsonImporter ji = JsonImporter.createWithParent(this, null, pParentJi, tgtPropName, isSuppressed, null, null);
		this.jis.add(ji);

		return ji;
	}

	private void calculateAllCeText() {
		if (isGeneratingModel()) {
			sbModel.append(buildModel());
		}

		if (isGeneratingInstances()) {
			Float maxInsts = maxInstances();
			int maxVal = -1;
			int thisCtr = 0;

			if (maxInsts != null) {
				maxVal = maxInsts.intValue();
			}

			for (JsonImporter thisJi : this.jis) {
				if (maxVal > -1) {
					if (thisCtr >= maxVal) {
						break;
					}
				}
				if (!ignoreConcept(thisJi.getConceptName())) {
					if (!thisJi.isEmpty()) {
						++thisCtr;
						sbFacts.append(thisJi.calculateCeText(ceStyle(), isIgnoringBlankValues()));
					}
				}
			}
		}

		if (isSavingCe()) {
			StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);

			if ((sbModel != null) && (sbModel.length() > 0)) {
				sa.saveCeText(this.sbModel.toString(), null, false);
			}

			if ((sbFacts != null) && (sbFacts.length() > 0)) {
				sa.saveCeText(this.sbFacts.toString(), null, false);
			}
		}

		if (isShowingCe()) {
			System.out.println(this.sbModel.toString());
			System.out.println(this.sbFacts.toString());
		}
	}
	
	private String buildModel() {
		TreeMap<String, ArrayList<ArrayList<String>>> conNames = new TreeMap<String, ArrayList<ArrayList<String>>>();
		ArrayList<String> rangeCons = new ArrayList<String>();

		for (JsonImporter thisJi : this.jis) {
			if (!thisJi.isEmpty()) {
				String conName = parentConceptNameFor(thisJi.getConceptName());
				
				if (!conName.equals(thisJi.getConceptName())) {
					ArrayList<String> inhPair = new ArrayList<String>();
					inhPair.add(thisJi.getConceptName());
					inhPair.add(conName);
	
					if (!this.conceptInheritance.contains(inhPair)) {
						this.conceptInheritance.add(inhPair);
					}
				}
	
				ArrayList<ArrayList<String>> propList = conNames.get(conName);
	
				if (propList == null) {
					propList = new ArrayList<ArrayList<String>>();
					conNames.put(conName, propList);
				}

				for (String attName : thisJi.getAttributeNames()) {
					ArrayList<String> newAtt = new ArrayList<String>();
					String rangeName = obtainRangeConceptFor(attName);

					if (rangeName == null) {
						rangeName = TYPE_VALUE;
					} else {
						if (!rangeCons.contains(rangeName)) {
							rangeCons.add(rangeName);
						}
					}

					newAtt.add(attName);
					newAtt.add(rangeName);
					newAtt.add(VAL_FALSE);
	
					if (!propList.contains(newAtt)) {
						propList.add(newAtt);
					}	
				}
	
				for (String relName : thisJi.getChildren().keySet()) {
					for (JsonImporter childJi : thisJi.getChildren().get(relName)) {
						ArrayList<String> newRel = new ArrayList<String>();
						String suppText = null;
						
						if (childJi.isSuppressed()) {
							suppText = VAL_TRUE;
						} else {
							suppText = VAL_FALSE;
						}
	
						newRel.add(relName);
						newRel.add(childJi.getConceptName());
						newRel.add(suppText);
	
						if (!propList.contains(newRel)) {
							propList.add(newRel);
						}	
					}
				}
			}
		}

		return ceModelTextFor(conNames, rangeCons);
	}

	private String ceModelTextFor(TreeMap<String, ArrayList<ArrayList<String>>> pModel, ArrayList<String> pRangeCons) {
		StringBuilder sb = new StringBuilder();
		String connector = CONNECTOR_THAT;

		for (String conName : pModel.keySet()) {
			if (!ignoreConcept(conName)) {
				this.variableCounter = 0;
				sb.append(ceConceptualiseFor(conName));

				String inhText = ceInheritanceFor(conName);

				if (inhText != null) {
					sb.append(inhText);
					connector = CONNECTOR_AND;
				}

				for (ArrayList<String> attDetails : pModel.get(conName)) {
					String propName = attDetails.get(0);
					String propRange = attDetails.get(1);
					String isSuppressed = attDetails.get(2);

					if (!isSuppressed.equals(VAL_TRUE)) {
						sb.append(cePropertyFor(propName, propRange, connector));
						connector = CONNECTOR_AND;
					}
				}
	
				sb.append(CONNECTOR_FINISH);
				connector = CONNECTOR_THAT;
			}
		}
		
		for (ArrayList<String> inhPair : this.conceptInheritance) {
			String childCon = inhPair.get(0);

			if (!pModel.keySet().contains(childCon)) {
				String ceText = ceConceptualiseFor(childCon);
				String inhText = ceInheritanceFor(childCon);

				sb.append(ceText);

				if (inhText != null) {
					sb.append(inhText);
				}

				sb.append(CONNECTOR_FINISH);
			}
		}

		for (String thisCon : pRangeCons) {
			String ceText = ceConceptualiseFor(thisCon);

			sb.append(ceText);
			sb.append(CONNECTOR_FINISH);
		}

		return sb.toString();
	}

	private String ceInheritanceFor(String pConName) {
		String result = null;

		for (ArrayList<String> thisPair : this.conceptInheritance) {
			String childCon = thisPair.get(0);
			String parentCon = thisPair.get(1);
			
			if (childCon.equals(pConName)) {
				result = " " + CONNECTOR_THAT + "  is " + determinerFor(parentCon) + " " + parentCon;
			}
		}

		return result;
	}

	private boolean ignoreConcept(String pConName) {
		boolean result = false;

		if (this.configConcepts != null) {
			TreeMap<String, String> conMap = this.configConcepts.get(pConName);

			if (conMap != null) {
				result = booleanFrom(conMap, CONF_IC);
			}
		}

		return result;
	}

	private boolean ignoreProperty(String pPropName) {
		boolean result = false;

		if (this.configProperties != null) {
			if (pPropName != null) {
				TreeMap<String, String> propMap = this.configProperties.get(pPropName);
	
				if (propMap != null) {
					result = booleanFrom(propMap, CONF_IP);
				}
			}
		}

		return result;
	}

	private boolean suppressProperty(String pPropName) {
		boolean result = false;

		if (this.configProperties != null) {
			if (pPropName != null) {
				TreeMap<String, String> propMap = this.configProperties.get(pPropName);
	
				if (propMap != null) {
					result = booleanFrom(propMap, CONF_SP);
				}
			}
		}

		return result;
	}

	private boolean booleanFrom(TreeMap<String, String> pMap, String pKey) {
		boolean result = false;
		String rawVal = pMap.get(pKey);

		if (rawVal != null) {
			if (rawVal.equals(VAL_TRUE)) {
				result = true;
			} else {
				result = false;
			}
		}

		return result;
	}

	private String ceConceptualiseFor(String pConName) {
		return "conceptualise " + determinerFor(pConName) + " ~ " + pConName + " ~ " + generateVariableFor(pConName);
	}

	private String determinerFor(String pConName) {
		String result = null;
		String lcConName = pConName.toLowerCase();

		if (lcConName.startsWith("a") ||
				lcConName.startsWith("e") ||
				lcConName.startsWith("i") ||
				lcConName.startsWith("o") ||
				lcConName.startsWith("u")) {
			result = "an";
		} else {
			result = "a";
		}

		return result;
	}

	private String cePropertyFor(String pPropName, String pPropRange, String pConn) {
		return " " + pConn + "  has the " + pPropRange + " " + generateVariableFor(pPropName) + " as ~ " + pPropName + " ~";
	}

	private String generateVariableFor(String pPropName) {
		return pPropName.substring(0, 1).toUpperCase() + ++this.variableCounter;
	}

	private void processJsonObject(JsonImporter pJi, CeStoreJsonObject pJo, String pPropName) {
		for (String thisKey : pJo.keySet()) {
			Object thisVal = pJo.get(this.wc, thisKey);

//			String wholeKey = null;
//
//			if (pPropName != null) {
//				wholeKey = pPropName + ":" +  thisKey;
//			} else {
//				wholeKey = thisKey;
//			}

			processObject(pJi, thisVal, thisKey);
		}
	}

	private void processJsonArray(JsonImporter pJi, CeStoreJsonArray pJa, String pPropName) {
		int i = 0;
		boolean splitName = treatArrayAsNamedProperties(pPropName);

		for (Object thisObj : pJa.items()) {
//			String newStack = pPropStack + ":" + i++;
			String propName = null;

			if (splitName) {
				propName = pPropName + " " + i++;
			} else {
				propName = pPropName;
			}

			processObject(pJi, thisObj, propName);
		}
	}

	private boolean treatArrayAsNamedProperties(String pPropName) {
		boolean result = false;

		if (this.configProperties != null) {
			if (pPropName != null) {
				TreeMap<String, String> propMap = this.configProperties.get(pPropName);
				
				if (propMap != null) {
					result = booleanFrom(propMap, CONF_AANP);
				}
			}
		}

		return result;
	}

	private String obtainPropertyNameFor(String pPropName) {
		String result = pPropName;

		if (this.configProperties != null) {
			if (pPropName != null) {
				TreeMap<String, String> propMap = this.configProperties.get(pPropName);
				
				if (propMap != null) {
					String tgtName = propMap.get(CONF_TP);
					
					if (tgtName != null) {
						result = tgtName;
					}
				}
			}
		}

		return result;
	}

	private String obtainRangeConceptFor(String pPropName) {
		String result = null;

		if (this.configProperties != null) {
			if (pPropName != null) {
				TreeMap<String, String> propMap = this.configProperties.get(pPropName);
				
				if (propMap != null) {
					String tgtName = propMap.get(CONF_RC);
					
					if (tgtName != null) {
						result = tgtName;
					}
				}
			}
		}

		return result;
	}

	private String parentConceptNameFor(String pRawName) {
		String result = null;

		if (pRawName != null) {
			for (String thisKey : this.configConcepts.keySet()) {
				TreeMap<String, String> conMap = this.configConcepts.get(thisKey);
	
				if (conMap != null) {
					String thisParent = conMap.get(CONF_PC);
	
					if (thisParent != null) {
						if (thisKey.equals(pRawName)) {
							result = parentConceptNameFor(thisParent);
						}
					}
				}
			}
	
			if (result == null) {
				result = pRawName;
			}
		}

		return result;
	}

	private void addAttribute(JsonImporter pJi, String pVal, String pPropName) {
		String tgtPropName = normalisePropertyName(pPropName);

		if (!ignoreProperty(pPropName)) {
			String rangeCon = obtainRangeConceptFor(tgtPropName);

			if (moveToParent(pPropName)) {
				pJi.getParent().addAttribute(tgtPropName, pVal, rangeCon);
			} else {
				pJi.addAttribute(tgtPropName, pVal, rangeCon);
			}
		}
	}

	private boolean moveToParent(String pPropName) {
		boolean result = false;

		if (this.configProperties != null) {
			if (pPropName != null) {
				TreeMap<String, String> propMap = this.configProperties.get(pPropName);
	
				if (propMap != null) {
					result = booleanFrom(propMap, CONF_MTP);
				}
			}
		}

		return result;
	}

	private boolean isTypeProperty(String pPropName) {
		boolean result = false;

		if (this.configProperties != null) {
			if (pPropName != null) {
				TreeMap<String, String> propMap = this.configProperties.get(pPropName);
	
				if (propMap != null) {
					result = booleanFrom(propMap, CONF_TyP);
				}
			}
		}

		return result;
	}

	private boolean isIdProperty(String pPropName) {
		boolean result = false;

		if (this.configProperties != null) {
			if (pPropName != null) {
				TreeMap<String, String> propMap = this.configProperties.get(pPropName);
	
				if (propMap != null) {
					result = booleanFrom(propMap, CONF_IdP);
				}
			}
		}

		return result;
	}

	private void processInteger(JsonImporter pJi, Integer pVal, String pPropName) {
		addAttribute(pJi, pVal.toString(), pPropName);
	}

	private void processFloat(JsonImporter pJi, Float pVal, String pPropName) {
		addAttribute(pJi, pVal.toString(), pPropName);
	}

	private void processBoolean(JsonImporter pJi, Boolean pVal, String pPropName) {
		addAttribute(pJi, pVal.toString(), pPropName);
	}

	private void processString(JsonImporter pJi, String pVal, String pPropName) {
		if (isTypeProperty(pPropName)) {
			String tgtConName = normaliseConceptName(pVal);
			pJi.setConceptName(tgtConName, determinerFor(tgtConName));
			pJi.setOriginalConceptName(pVal);
		} else if (isIdProperty(pPropName)) {
			pJi.setId(pVal);
		} else {
			addAttribute(pJi, pVal, pPropName);
		}
	}

	private void processObject(JsonImporter pJi, Object pVal, String pPropName) {
		if (isString(pVal)) {
			processString(pJi, (String)pVal, pPropName);
		} else if (isInteger(pVal)) {
			processInteger(pJi, (Integer)pVal, pPropName);
		} else if (isFloat(pVal)) {
			processFloat(pJi, (Float)pVal, pPropName);
		} else if (isBoolean(pVal)) {
			processBoolean(pJi, (Boolean)pVal, pPropName);
		} else if (isJsonArray(pVal)) {
			processJsonArray(pJi, (CeStoreJsonArray)pVal, pPropName);
		} else if (isJsonObject(pVal)) {
			if (!ignoreProperty(pPropName)) {
				if (moveToParent(pPropName)) {
					processJsonObject(pJi, (CeStoreJsonObject)pVal, pPropName);
				} else {
					JsonImporter newJi = null;
					
					if (pJi == null) {
						newJi = createNewJsonImporter();
					} else {
						newJi = createNewJsonImporterFor(pJi, pPropName);
					}
	
					processJsonObject(newJi, (CeStoreJsonObject)pVal, pPropName);
				}
			}
		} else {
			reportError("Unhandled  type: " + pPropName + " -> " + pVal.getClass().getName(), this.wc);
		}
	}
	
	private String normalisePropertyName(String pPropName) {
		String propName = obtainPropertyNameFor(pPropName);

		return normaliseName(propName);
	}

	private String normaliseConceptName(String pConName) {
		return normaliseName(pConName);
	}

	private String normaliseName(String pRawName) {
		String result = null;

		if (pRawName != null) {
			result = pRawName.trim();
			boolean isAllUpperCase = result.equals(result.toUpperCase());
	
			if (getBooleanFromGeneralConfig(CONF_DPN, false)) {
				if (result.endsWith("ies")) {
					result = result.substring(0, result.length() - 3) + "y";
				} else if (result.endsWith("s")) {
					result = result.substring(0, result.length() - 1);
				}
			}

			if (getBooleanFromGeneralConfig(CONF_RS, false)) {
				result = result.replaceAll(Pattern.quote("_"), " ");
				result = result.replaceAll(Pattern.quote("-"), " ");
				result = result.replaceAll(Pattern.quote("."), " ");
			}

			if (!isAllUpperCase) {
				if (getBooleanFromGeneralConfig(CONF_SCC, false)) {
					//Only do this if it is not an all upper-case word.
					result = result.replaceAll("([a-z])(\\p{Upper})", "$1 $2");
				}
			}

			if (getBooleanFromGeneralConfig(CONF_FLC, false)) {
				result = result.toLowerCase();
			}
		}

		return result;
	}

	private boolean getBooleanFromGeneralConfig(String pParmName, boolean pDefault) {
		boolean result = pDefault;

		String boolText = this.configGeneral.get(pParmName);

		if (boolText != null) {
			if (boolText.equals(VAL_TRUE)) {
				result = true;
			} else if (boolText.equals(VAL_FALSE)) {
				result = false;
			}
		}

		return result;
	}

	private Float getNumberFromGeneralConfig(String pParmName) {
		Float result = null;

		String numText = this.configGeneral.get(pParmName);

		if (numText != null) {
			result = new Float(numText);
		}

		return result;
	}

	private String getStringFromGeneralConfig(String pParmName, String pDefault) {
		String result = this.configGeneral.get(pParmName);

		if (result == null) {
			result = pDefault;
		}

		return result;
	}

	private boolean isGeneratingModel() {
		return getBooleanFromGeneralConfig(CONF_GM, true);
	}

	private boolean isGeneratingInstances() {
		return getBooleanFromGeneralConfig(CONF_GI, true);
	}

	private Float maxInstances() {
		return getNumberFromGeneralConfig(CONF_MI);
	}

	private boolean isSavingCe() {
		return getBooleanFromGeneralConfig(CONF_SaC, false);
	}

	private boolean isShowingCe() {
		return getBooleanFromGeneralConfig(CONF_ShC, false);
	}

	private boolean isIgnoringBlankValues() {
		return getBooleanFromGeneralConfig(CONF_IBV, true);
	}

	private String ceStyle() {
		return getStringFromGeneralConfig(CONF_CS, DEFAULT_CESTYLE);
	}

	private boolean isString(Object pObj) {
		return pObj.getClass().getName() == CLASS_STRING;
	}

	private boolean isInteger(Object pObj) {
		return pObj.getClass().getName() == CLASS_INTEGER;
	}

	private boolean isFloat(Object pObj) {
		return pObj.getClass().getName() == CLASS_FLOAT;
	}

	private boolean isBoolean(Object pObj) {
		return pObj.getClass().getName() == CLASS_BOOLEAN;
	}

	private boolean isJsonArray(Object pObj) {
		return pObj.getClass().getName() == CLASS_JSONARRAY;
	}

	private boolean isJsonObject(Object pObj) {
		return pObj.getClass().getName() == CLASS_JSONOBJECT;
	}

}
