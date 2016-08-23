package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonParser;
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.hudson.helper.SpThing;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class QuestionAnswererHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String SPTYPE_ENCCON = "enumerated-concept";
	private static final String SPTYPE_COLL = "collection";
	private static final String CON_QPHRASE = "question phrase";
	private static final String MOD_EXPAND = "general:expand";
	private static final String MOD_LINKSFROM = "general:linksFrom";
	private static final String MOD_LINKSTO = "general:linksTo";
	private static final String MOD_LOCATE = "general:locate";
	private static final String MOD_COUNT = "general:count";
	private static final String MOD_LIST = "general:list";
	private static final String MOD_SHOW = "general:show";

	//TODO: All of these need to be handled by the interpreter function
//	private static final String MOD_RESET = "general:reset";
//	private static final String MOD_CLEARCACHE = "general:clearcache";
//	private static final String MOD_STATS = "general:stats";
//	private static final String MOD_CESAVE = "ce:save";

	private String interpretationJson = null;
	private String answerText = "";

	ArrayList<CeConcept> allConcepts = null;
	ArrayList<CeProperty> allProperties = null;
	ArrayList<CeInstance> allInstances = null;
	ArrayList<SpThing> allSpecials = null;

	HashSet<CeInstance> domainInstances = null;
	HashSet<CeInstance> modifierInstances = null;
	HashSet<CeInstance> questionInstances = null;

	public QuestionAnswererHandler(WebActionContext pWc, boolean pDebug, String pQt, long pStartTime) {
		super(pWc, pDebug, Question.create(pQt), pStartTime);
	}

	@Override
	public CeStoreJsonObject handleQuestion() {
		CeStoreJsonObject result = null;

		//TODO: Move this code away from using this.question as it is not relevant
		this.interpretationJson = this.question.getQuestionText();

		processInterpretation();
		answerQuestion();

		result = createResult();

		return result;
	}

	private void processInterpretation() {
		CeStoreJsonObject jObj = null;

		CeStoreJsonParser sjp = new CeStoreJsonParser(this.ac, this.interpretationJson);
		sjp.parse();

		if (sjp.hasRootJsonObject()) {
			jObj = sjp.getRootJsonObject();
		} else {
			reportError("No JSON Object with interpretations found", this.ac);
		}

		if (jObj != null) {
			CeStoreJsonObject jCons = (CeStoreJsonObject)jObj.get(this.ac, "concepts");
			CeStoreJsonObject jProps = (CeStoreJsonObject)jObj.get(this.ac, "properties");
			CeStoreJsonObject jInsts = (CeStoreJsonObject)jObj.get(this.ac, "instances");
			CeStoreJsonObject jSpecs = (CeStoreJsonObject)jObj.get(this.ac, "specials");

			this.allConcepts = extractConceptsFrom(jCons);
			this.allProperties = extractPropertiesFrom(jProps);
			this.allInstances = extractInstancesFrom(jInsts);
			this.allSpecials = extractSpecialsFrom(jSpecs);

			filterInstances();
		}
	}

	private void answerQuestion() {
		debugInstances();

		if (this.allConcepts.isEmpty()) {
			if (this.allProperties.isEmpty()) {
				if (this.domainInstances.isEmpty()) {
					//no concepts, no properties, no domain instances
					handleEverythingEmpty();
				} else {
					//no concepts, no properties, some domain instances
					handleJustInstances();
				}
			} else {
				if (this.domainInstances.isEmpty()) {
					//no concepts, some properties, no domain instances
					handleJustProperties();
				} else {
					//no concepts, some properties, some domain instances
					handleSomePropertiesAndInstances();
				}
			}
		} else {
			if (this.allProperties.isEmpty()) {
				if (this.domainInstances.isEmpty()) {
					//some concepts, no properties, no domain instances
					handleJustConcepts();
				} else {
					//some concepts, no properties, some domain instances
					handleSomeConceptsAndInstances();
				}
			} else {
				if (this.domainInstances.isEmpty()) {
					//some concepts, some properties, no domain instances
					handleSomeConceptsAndProperties();
				} else {
					//some concepts, some properties, some domain instances
					handleSomeConceptsPropertiesAndInstances();
				}
			}
		}
	}

	private void handleJustInstances() {
		for (CeInstance thisInst : this.domainInstances) {
			answerInstanceQuestionFor(thisInst);
		}
	}

	private void appendToAnswer(String pText) {
		if (!this.answerText.isEmpty()) {
			this.answerText += "\n";
		}

		this.answerText += pText;
	}

	private void handleEverythingEmpty() {
		appendToAnswer("handleEverythingEmpty");
	}

	private void handleSomePropertiesAndInstances() {
		appendToAnswer("handleSomePropertiesAndInstances");
	}

	private void handleJustProperties() {
		appendToAnswer("handleJustProperties");
	}

	private void handleJustConcepts() {
		appendToAnswer("handleJustConcepts");
	}

	private void handleSomeConceptsAndInstances() {
		appendToAnswer("handleSomeConceptsAndInstances");
	}

	private void handleSomeConceptsAndProperties() {
		appendToAnswer("handleSomeConceptsAndProperties");
	}

	private void handleSomeConceptsPropertiesAndInstances() {
		appendToAnswer("handleSomeConceptsPropertiesAndInstances");
	}

	private void answerInstanceQuestionFor(CeInstance pInst) {
		if (isExpand()) {
			answerExpandFor(pInst);
		} else if (isLinksFrom()) {
			answerLinksFromFor(pInst);
		} else if (isLinksTo()) {
			answerLinksToFor(pInst);
		} else if (isLocate()) {
			answerLocateFor(pInst);
		} else if (isCount()) {
			answerCountFor(pInst);
		} else if (isList()) {
			answerListFor(pInst);
		} else if (isShow()) {
			answerShowFor(pInst);
		} else {
			answerSummaryFor(pInst);
		}
	}

	private void answerExpandFor(CeInstance pInst) {
		appendToAnswer("answerExpandFor: " + pInst.getInstanceName());
	}

	private void answerLinksFromFor(CeInstance pInst) {
		appendToAnswer("answerLinksFromFor: " + pInst.getInstanceName());
	}

	private void answerLinksToFor(CeInstance pInst) {
		appendToAnswer("answerLinksToFor: " + pInst.getInstanceName());
	}

	private void answerLocateFor(CeInstance pInst) {
		appendToAnswer("answerLocateFor: " + pInst.getInstanceName());
	}

	private void answerCountFor(CeInstance pInst) {
		//Does not make sense for a count of one instance
		appendToAnswer("answerCountFor: " + pInst.getInstanceName());
	}

	private void answerListFor(CeInstance pInst) {
		appendToAnswer("answerListFor: " + pInst.getInstanceName());
	}

	private void answerShowFor(CeInstance pInst) {
		appendToAnswer("answerShowFor: " + pInst.getInstanceName());
	}

	private void answerSummaryFor(CeInstance pInst) {
		//TODO: It would be nice to use the original phrase specified in the question
		String questionText = pInst.getInstanceName();
		String instName = pInst.getInstanceName();
		String answerText = "";

		if (questionText.equalsIgnoreCase(instName)) {
			answerText = instName;
		} else {
			answerText = questionText + " (" + instName + ")";
		}

		answerText += textForConcepts(pInst);

System.out.println("answerSummaryFor: " + answerText);

		appendToAnswer(answerText);
	}

	private String textForConcepts(CeInstance pInst) {
		String result = "";

		for (CeConcept thisCon : pInst.getAllLeafConcepts()) {
			if (isDomainConcept(thisCon)) {
				if (result.isEmpty()) {
					result += " is a ";
				} else {
					result += ", ";
				}
	
				result += thisCon.getConceptName();
			}
		}

		return result;
	}

	private boolean isDomainConcept(CeConcept pCon) {
		return !pCon.equalsOrHasParentNamed(this.ac, CON_CONFCON);
	}

	private boolean isExpand() {
		return isModifierNamed(MOD_EXPAND);
	}

	private boolean isLinksFrom() {
		return isModifierNamed(MOD_LINKSFROM);
	}

	private boolean isLinksTo() {
		return isModifierNamed(MOD_LINKSTO);
	}

	private boolean isLocate() {
		return isModifierNamed(MOD_LOCATE);
	}

	private boolean isCount() {
		return isModifierNamed(MOD_COUNT);
	}

	private boolean isList() {
		return isModifierNamed(MOD_LIST);
	}

	private boolean isShow() {
		return isModifierNamed(MOD_SHOW);
	}

	private boolean isModifierNamed(String pTgtName) {
		boolean result = false;

		for (CeInstance thisInst : this.modifierInstances) {
			CePropertyInstance thisPi = thisInst.getPropertyInstanceNamed(PROP_CORRTO);

			for (CeInstance relInst : thisPi.getValueInstanceList(this.ac)) {
				if (relInst.getInstanceName().equals(pTgtName)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	private void debugInstances() {
System.out.println("Domain instances");

		for (CeInstance thisInst : this.domainInstances) {
System.out.println("  " + thisInst.getInstanceName());
		}

System.out.println("Modifier instances");

		for (CeInstance thisInst : this.modifierInstances) {
System.out.println("  " + thisInst.getInstanceName());
		}

System.out.println("Question instances");

		for (CeInstance thisInst : this.questionInstances) {
System.out.println("  " + thisInst.getInstanceName());
		}
	}

	private void filterInstances() {
		this.domainInstances = new HashSet<CeInstance>();
		this.modifierInstances = new HashSet<CeInstance>();
		this.questionInstances = new HashSet<CeInstance>();

		for (CeInstance thisInst : this.allInstances) {
			boolean alreadyProcessed = false;

			if (!thisInst.isOnlyConceptNamed(this.ac, CON_CONFCON)) {
				if (thisInst.isConceptNamed(this.ac, CON_MODIFIER)) {
					this.modifierInstances.add(thisInst);
					alreadyProcessed = true;
				}

				if (thisInst.isConceptNamed(this.ac, CON_QPHRASE)) {
					this.questionInstances.add(thisInst);
					alreadyProcessed = true;
				}

				if (!alreadyProcessed) {
					this.domainInstances.add(thisInst);
				}
			}
		}
	}

	private ArrayList<CeConcept> extractConceptsFrom(CeStoreJsonObject pJsonObj) {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

System.out.println("concepts:");

		for (String thisKey : pJsonObj.keySet()) {
			String name = pJsonObj.getJsonObject(thisKey).getString("name");
			int pos = pJsonObj.getJsonObject(thisKey).getInt("position");
			CeConcept thisCon = this.ac.getModelBuilder().getConceptNamed(this.ac, name);

System.out.println("  " + thisKey + " -> " + name + " ["+ pos +"]");

			if (thisCon != null) {
				result.add(thisCon);
			}
		}

		return result;
	}

	private ArrayList<CeProperty> extractPropertiesFrom(CeStoreJsonObject pJsonObj) {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

System.out.println("properties:");

		for (String thisKey : pJsonObj.keySet()) {
			CeStoreJsonObject jObj = null;

			jObj = pJsonObj.getJsonObject(thisKey);
			String name = jObj.getString("name");

			jObj = pJsonObj.getJsonObject(thisKey);
			int pos = jObj.getInt("position");

			CeProperty thisProp = this.ac.getModelBuilder().getPropertyFullyNamed(name);

System.out.println("  " + thisKey + " -> " + name + " ["+ pos +"]");

			if (thisProp != null) {
				result.add(thisProp);
			}
		}

		return result;
	}

	private ArrayList<CeInstance> extractInstancesFrom(CeStoreJsonObject pJsonObj) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

System.out.println("instances:");

		for (String thisKey : pJsonObj.keySet()) {
			String name = pJsonObj.getJsonObject(thisKey).getString("name");
			int pos = pJsonObj.getJsonObject(thisKey).getInt("position");
			CeInstance thisInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, name);

System.out.println("  " + thisKey + " -> " + name + " ["+ pos +"]");

			if (thisInst != null) {
				result.add(thisInst);
			}
		}

		return result;
	}

	private ArrayList<SpThing> extractSpecialsFrom(CeStoreJsonObject pJsonObj) {
		ArrayList<SpThing> result = new ArrayList<SpThing>();

System.out.println("specials:");

		for (String thisKey : pJsonObj.keySet()) {
			String name = pJsonObj.getJsonObject(thisKey).getString("name");
			String type = pJsonObj.getJsonObject(thisKey).getString("type");
			int pos = pJsonObj.getJsonObject(thisKey).getInt("position");

System.out.println("  " + thisKey + " -> " + name + " (" + type + ") [" + pos + "]");

			if (type.equals(SPTYPE_ENCCON)) {
				//Create SpEncodedConcept instance
			} else if (type.equals(SPTYPE_COLL)) {
				//Create SpCollection instance
			} else {
				System.out.println("Unexpected special type: " + type);
			}
		}

		return result;
	}

	protected CeStoreJsonObject createResult() {
		long st = System.currentTimeMillis();
		CeStoreJsonObject result = null;

		result = createJsonResponse();
		reportDebug("createResult=" + new Long(System.currentTimeMillis() - st).toString(), this.ac);

		return result;
	}

	protected CeStoreJsonObject createJsonResponse() {
		CeStoreJsonObject result = new CeStoreJsonObject();

		result.put("answer", this.answerText);

		return result;
	}

}
