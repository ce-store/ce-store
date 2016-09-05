package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonParser;
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.hudson.special.SpCollection;
import com.ibm.ets.ita.ce.store.hudson.special.SpEnumeratedConcept;
import com.ibm.ets.ita.ce.store.hudson.special.SpLinkedInstance;
import com.ibm.ets.ita.ce.store.hudson.special.SpMatchedTriple;
import com.ibm.ets.ita.ce.store.hudson.special.SpThing;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class QuestionAnswererHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String SPTYPE_ENCCON = "enumerated-concept";
	private static final String SPTYPE_COLL = "collection";
	private static final String SPTYPE_MT = "matched-triple";
	private static final String SPTYPE_LI = "linked-instance";

	private static final String MOD_EXPAND = "general:expand";
	private static final String MOD_LINKSFROM = "general:linksFrom";
	private static final String MOD_LINKSTO = "general:linksTo";
	private static final String MOD_LOCATE = "general:locate";
	private static final String MOD_COUNT = "general:count";
	private static final String MOD_LIST = "general:list";
	private static final String MOD_SHOW = "general:show";

	private static final String CON_QPHRASE = "question phrase";
	private static final String CON_QWORD = "question word";
	private static final String CON_COMWORD = "common word";
	private static final String CON_CONNWORD = "connector word";

	private static final String[] CONLIST_OTHERS = { CON_QPHRASE, CON_QWORD, CON_COMWORD, CON_MODIFIER, CON_CONNWORD };

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
	HashSet<CeInstance> otherInstances = null;

	public QuestionAnswererHandler(ActionContext pAc, boolean pDebug, String pQt, long pStartTime) {
		super(pAc, pDebug, Question.create(pQt), pStartTime);
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

			if (jCons != null) {
				this.allConcepts = extractConceptsFrom(jCons);
			} else {
				this.allConcepts = new ArrayList<CeConcept>();
			}

			if (jProps != null) {
				this.allProperties = extractPropertiesFrom(jProps);
			} else {
				this.allProperties = new ArrayList<CeProperty>();
			}

			if (jInsts != null) {
				this.allInstances = extractInstancesFrom(jInsts);
			} else {
				this.allInstances = new ArrayList<CeInstance>();
			}

			if (jSpecs != null) {
				this.allSpecials = extractSpecialsFrom(jSpecs);
			} else {
				this.allSpecials = new ArrayList<SpThing>();
			}

			filterInstances();
		}
	}

	private void answerQuestion() {
//		debugInstances();

		if (hasMatchedTriples()) {
			tryMatchedTripleAnswer();
		} else {
			tryNormalAnswer();
		}
	}

	private boolean hasMatchedTriples() {
		boolean result = false;
		
		if (!this.allSpecials.isEmpty()) {
			for (SpThing thisSp : this.allSpecials) {
				if (thisSp.isMatchedTriple()) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	private void tryMatchedTripleAnswer() {
		for (SpThing thisSp : this.allSpecials) {
			if (thisSp.isMatchedTriple()) {
				matchedTripleAnswerFor((SpMatchedTriple)thisSp);
			}
		}
	}
	
	private void matchedTripleAnswerFor(SpMatchedTriple pMt) {
		//TODO: Complete this
		if (pMt.isFullTriple()) {
			appendToAnswer("TBC - matchedTripleAnswerFor (full)");			
		} else if (pMt.isPartialSubjectTriple()) {
			appendToAnswer("TBC - matchedTripleAnswerFor (subject-based)");			
		} else {
			appendToAnswer("TBC - matchedTripleAnswerFor (object-based)");			
		}
	}
	
	private void tryNormalAnswer() {
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
		//TODO: Complete this
		appendToAnswer("TBC - handleEverythingEmpty");
	}

	private void handleSomePropertiesAndInstances() {
		for (CeInstance thisInst : this.domainInstances) {
			handlePropertiesFor(thisInst);
		}
	}
	
	private void handlePropertiesFor(CeInstance pInst) {
		String result = null;
		String instName = null;
		String propName = null;
		String propVals = null;
		
		instName = pInst.getInstanceName();
		
		for (CeProperty thisProp : this.allProperties) {
			for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
				CeProperty innerProp = thisPi.getRelatedProperty();
				
				if (!innerProp.isStatedProperty()) {
					innerProp = innerProp.getStatedSourceProperty();
				}

				if (innerProp.equals(thisProp)) {
					propName = thisPi.getPropertyName();
					
					for (String thisVal : thisPi.getValueList()) {
						if (propVals == null) {
							propVals = "";
						} else {
							propVals += ", ";
						}

						propVals += thisVal;
					}
				}
			}
			
			if (thisProp.isFunctionalNoun()) {
				result = instName + " has " + propVals + " as " + propName;
			} else {
				result = instName + " " + propName + " " + propVals;
			}

			appendToAnswer(result);
		}
	}

	private void handleJustProperties() {
		//TODO: Complete this
		appendToAnswer("TBC - handleJustProperties");
	}

	private void handleJustConcepts() {
		if (isList()) {
			answerListFor(this.allConcepts);
		} else {
			for (CeConcept thisCon : this.allConcepts) {
				answerConceptQuestionFor(thisCon);
			}
		}
	}

	private void handleSomeConceptsAndInstances() {
		if (isLocate()) {
			ArrayList<CeInstance> instList = filterInstancesByConcepts();
			String result = "";
			
			for (CeInstance thisInst : instList) {
				result += answerLocateFor(thisInst);
			}
			
			appendToAnswer(result);
		} else {
			//TODO: Complete this
			appendToAnswer("TBC - handleSomeConceptsAndInstances");
		}
	}
	
	private ArrayList<CeInstance> filterInstancesByConcepts() {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		for (CeInstance thisInst : this.domainInstances) {
			for (CeConcept thisCon : this.allConcepts) {
				if (thisInst.isConcept(thisCon)) {
					result.add(thisInst);
				}
			}
		}

		return result;
	}

	private void handleSomeConceptsAndProperties() {
		//TODO: Complete this
		appendToAnswer("TBC - handleSomeConceptsAndProperties");
	}

	private void handleSomeConceptsPropertiesAndInstances() {
		//TODO: Complete this
		appendToAnswer("TBC - handleSomeConceptsPropertiesAndInstances");
	}

	private void answerInstanceQuestionFor(CeInstance pInst) {
		if (isExpand()) {
			answerExpandFor(pInst);
		} else if (isLinksFrom()) {
			answerLinksFromFor(pInst);
		} else if (isLinksTo()) {
			answerLinksToFor(pInst);
		} else if (isLocate()) {
			appendToAnswer(answerLocateFor(pInst));
		} else if (isCount()) {
			answerCountFor(pInst);
		} else if (isList()) {
			answerListFor(pInst);
		} else if (isShow()) {
			answerShowFor(pInst);
		} else {
			String answer = answerSummaryFor(pInst);
			appendToAnswer(answer);
		}
	}

	private void answerConceptQuestionFor(CeConcept pCon) {
		if (isExpand()) {
			//TODO: Consider other properties like properties, parent concepts, conceptual models etc
			answerExpandFor(pCon.retrieveMetaModelInstance(this.ac));
		} else if (isLinksFrom()) {
			answerLinksFromFor(pCon);
		} else if (isLinksTo()) {
			answerLinksToFor(pCon);
		} else if (isLocate()) {
			answerLocateFor(pCon);
		} else if (isCount()) {
			answerCountFor(pCon);
		} else if (isShow()) {
			answerShowFor(pCon);
		} else {
			appendToAnswer(answerSummaryFor(pCon));
		}
	}

	private void answerExpandFor(CeInstance pInst) {
		String result = answerSummaryFor(pInst);
		
		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			result += "\n";
			result += "  ";
			result += thisPi.getPropertyName();
			result += " -> ";

			boolean firstTime = true;

			for (String thisVal : thisPi.getValueList()) {
				if (!firstTime) {
					result += ", ";
				}

				result += thisVal;
				firstTime = false;
			}
		}

		appendToAnswer(result);
	}

	private void answerLinksFromFor(CeInstance pInst) {
		//TODO: Complete this
		appendToAnswer("TBC - answerLinksFromForInstance: " + pInst.getInstanceName());
	}

	private void answerLinksFromFor(CeConcept pCon) {
		//TODO: Complete this
		appendToAnswer("TBC - answerLinksFromForConcept: " + pCon.getConceptName());
	}

	private void answerLinksToFor(CeInstance pInst) {
		//TODO: Complete this
		appendToAnswer("TBC - answerLinksToForInstance: " + pInst.getInstanceName());
	}

	private void answerLinksToFor(CeConcept pCon) {
		//TODO: Complete this
		appendToAnswer("TBC - answerLinksToForConcept: " + pCon.getConceptName());
	}

	private String answerLocateFor(CeInstance pInst) {
		String result = "";

		//TODO: Also check for related instances that are spatial things
		if (pInst.isConceptNamed(this.ac, CON_SPATIAL)) {
			result += pInst.getInstanceName();
		}

		return result;
	}

	private void answerLocateFor(CeConcept pCon) {
		//TODO: Complete this
		appendToAnswer("TBC - answerLocateForConcept: " + pCon.getConceptName());
	}

	private void answerCountFor(CeInstance pInst) {
		//TODO: Complete this
		appendToAnswer("TBC - answerCountForInstance: " + pInst.getInstanceName());
	}

	private void answerCountFor(CeConcept pCon) {
		String result = null;
		String pluralName = pCon.pluralFormName(this.ac);
		int count = this.ac.getModelBuilder().countAllInstancesForConcept(pCon);
		
		if (count == 0) {
			result = "there are no ";
			result += pluralName;
			result += " defined";
		} else if (count == 1) {
			result = "there is 1 ";
			result += pCon.getConceptName();
			result += " defined";
		} else {
			result = "there are ";
			result += count;
			result += " ";
			result += pluralName;
			result += " defined";
		}

		appendToAnswer(result);
	}

	private void answerListFor(CeInstance pInst) {
		//TODO: Complete this
		appendToAnswer("TBC - answerListForInstance: " + pInst.getInstanceName());
	}

	private void answerListFor(ArrayList<CeConcept> pConList) {
		HashSet<String> result = new HashSet<String>();

		for (CeConcept thisCon : pConList) {
			for (CeInstance thisInst : this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, thisCon.getConceptName())) {
				String answer = answerSummaryFor(thisInst);

				result.add(answer);
			}			
		}

		ArrayList<String> sortedList = new ArrayList<String>(result);
		Collections.sort(sortedList);
		
		for (String thisAnswer : sortedList) {
			appendToAnswer(thisAnswer);
		}
	}

	private void answerShowFor(CeInstance pInst) {
		//TODO: Complete this
		appendToAnswer("TBC - answerShowForInstance: " + pInst.getInstanceName());
	}

	private void answerShowFor(CeConcept pCon) {
		//TODO: Complete this
		appendToAnswer("TBC - answerShowForConcept: " + pCon.getConceptName());
	}

	private String answerSummaryFor(CeConcept pCon) {
		return pCon.getConceptName() + textForConcepts(pCon.retrieveMetaModelInstance(this.ac));
	}

	private String answerSummaryFor(CeInstance pInst) {
		return pInst.getInstanceName() + textForConcepts(pInst);
	}

	private String textForConcepts(CeInstance pInst) {
		String result = "";

		for (CeConcept thisCon : pInst.getAllLeafConcepts()) {
			if (isDomainConcept(thisCon)) {
				if (result.isEmpty()) {
					result += " is ";
					result += thisCon.conceptQualifier();
					result += " ";
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

		for (CeInstance thisInst : this.otherInstances) {
			CePropertyInstance thisPi = thisInst.getPropertyInstanceNamed(PROP_CORRTO);

			if (thisPi != null) {
				for (CeInstance relInst : thisPi.getValueInstanceList(this.ac)) {
					if (relInst.getInstanceName().equals(pTgtName)) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}

//	private void debugInstances() {
//System.out.println("Domain instances");
//
//		for (CeInstance thisInst : this.domainInstances) {
//System.out.println("  " + thisInst.getInstanceName());
//		}
//
//System.out.println("Other instances");
//
//		for (CeInstance thisInst : this.otherInstances) {
//System.out.println("  " + thisInst.getInstanceName());
//		}
//	}

	private void filterInstances() {
		this.domainInstances = new HashSet<CeInstance>();
		this.otherInstances = new HashSet<CeInstance>();

		for (CeInstance thisInst : this.allInstances) {
			boolean alreadyProcessed = false;

			if (!thisInst.isOnlyConceptNamed(this.ac, CON_CONFCON)) {
				if (thisInst.isConceptNamed(this.ac, CONLIST_OTHERS)) {
					this.otherInstances.add(thisInst);
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
			CeStoreJsonObject jObj = pJsonObj.getJsonObject(thisKey);
			String type = jObj.getString("type");

			if (type.equals(SPTYPE_ENCCON)) {
				SpEnumeratedConcept spEc = new SpEnumeratedConcept(jObj);
				result.add(spEc);
			} else if (type.equals(SPTYPE_COLL)) {
				SpCollection spCo = new SpCollection(jObj);
				result.add(spCo);
			} else if (type.equals(SPTYPE_MT)) {
				SpMatchedTriple spMt = new SpMatchedTriple(jObj);
				result.add(spMt);
			} else if (type.equals(SPTYPE_LI)) {
				SpLinkedInstance spLi = new SpLinkedInstance(jObj);
				result.add(spLi);
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
