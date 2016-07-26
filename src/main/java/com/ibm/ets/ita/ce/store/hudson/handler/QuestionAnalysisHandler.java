package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebConcept;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebProperty;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class QuestionAnalysisHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_QT = "question_text";
	private static final String JSON_CO = "concepts";
	private static final String JSON_PR = "properties";
	private static final String JSON_IN = "instances";
	private static final String JSON_ANI = "answer_instances";
	private static final String JSON_ANV = "answer_values";

	private TreeMap<String, ArrayList<CeConcept>> concepts = new TreeMap<String, ArrayList<CeConcept>>();
	private TreeMap<String, ArrayList<CeProperty>> properties = new TreeMap<String, ArrayList<CeProperty>>();
	private TreeMap<String, ArrayList<CeInstance>> instances = new TreeMap<String, ArrayList<CeInstance>>();
	
	private ArrayList<CeInstance> resultInsts = new ArrayList<CeInstance>();
	private ArrayList<String> resultVals = new ArrayList<String>();

	public QuestionAnalysisHandler(WebActionContext pWc, boolean pDebug, String pQt, long pStartTime) {
		super(pWc, pDebug, Question.create(pQt), pStartTime);		
	}

	@Override
	public CeStoreJsonObject handleQuestion() {
		long st = System.currentTimeMillis();
		CeStoreJsonObject result = null;

		interpretQuestion();
		populateItems();

		winnowPropertiesBasedOnInstances();

		newExecuteQuestion();

		result = createResult();

		reportDebug("handleQuestion=" + new Long(System.currentTimeMillis() - st).toString(), this.ac);
		return result;
	}
	
	private void winnowPropertiesBasedOnInstances() {
		//TODO: This may be too simplistic.  For now it is fine but there may be more subtle
		//questions in the future where winnowing such as this is too aggressive.
		
		ArrayList<CeConcept> conList = new ArrayList<CeConcept>();
		
		for (CeInstance thisInst : listDomainInstances()) {
			for (CeConcept thisCon : thisInst.getAllLeafConcepts()) {
				if (!conList.contains(thisCon)) {
					conList.add(thisCon);
				}
			}
		}
		
		//conList now contains a list of all possible valid domain concepts for any properties

		ArrayList<CeProperty> propsToBeRemoved = new ArrayList<CeProperty>();

		for (ArrayList<CeProperty> propList : this.properties.values()) {
			for (CeProperty thisProp : propList) {
				boolean foundMatch = false;

				for (CeConcept thisCon : conList) {
					if (thisCon.equalsOrHasParent(thisProp.getDomainConcept())) {
						foundMatch = true;
					}
				}

				if (!foundMatch) {
					if (!propsToBeRemoved.contains(thisProp)) {
						propsToBeRemoved.add(thisProp);
					}
				}
			}
		}
		
		//propsToBeRemoved now contains all the properties that need to be removed
		ArrayList<String> keysToBeRemoved = new ArrayList<String>();

		for (String key : this.properties.keySet()) {
			ArrayList<CeProperty> propList = this.properties.get(key);
			ArrayList<CeProperty> copyList = new ArrayList<CeProperty>(propList);
			
			for (CeProperty thisProp : copyList) {
				if (propsToBeRemoved.contains(thisProp)) {
					propList.remove(thisProp);
				}
			}
			//If there are no properties left remove the whole entry
			if (propList.isEmpty()) {
				keysToBeRemoved.add(key);
			}
		}
		
		for (String key : keysToBeRemoved) {
			this.properties.remove(key);
		}
	}

	private void populateItems() {
		//This is a very simple method and should be updated with an full implementation

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isGroundedOnConcept()) {
				for (CeConcept thisCon : thisWord.listGroundedConcepts()) {
//					if (!isOnlyConfigCon(thisCon)) {
						String key = thisWord.getWholePhraseText();
						ArrayList<CeConcept> conList = this.concepts.get(key);
						
						if (conList == null) {
							conList = new ArrayList<CeConcept>();
							this.concepts.put(key,conList);
						}
					
						conList.add(thisCon);
//					}
				}
			}

			if (thisWord.isGroundedOnProperty()) {
				for (CeProperty thisProp : thisWord.listGroundedProperties()) {
					String key = thisWord.getWholePhraseText();
					ArrayList<CeProperty> propList = this.properties.get(key);
					
					if (propList == null) {
						propList = new ArrayList<CeProperty>();
						this.properties.put(key,propList);
					}
				
					propList.add(thisProp);
				}
			}

			if (thisWord.isGroundedOnInstance()) {
				for (CeInstance thisInst : thisWord.listGroundedInstances()) {
//					if (!isOnlyConfigCon(thisInst)) {
					String key = thisWord.getWholePhraseText();
					ArrayList<CeInstance> instList = this.instances.get(key);
					
					if (instList == null) {
						instList = new ArrayList<CeInstance>();
						this.instances.put(key,instList);
					}
				
					instList.add(thisInst);
//					}
				}
			}
		}
	}

	private void newExecuteQuestion() {
		//This recreates (and simplifies) functionality already elsewhere
		//This version is very naive and should be replaced by a more robust
		//implementation that handles more cases (when required)

		//TODO: Don't forget to handle the modifiers
		
		if (questionTypeSimpleInstances()) {
			handleSimpleInstanceQuestion();
		} else if (questionTypeSingleConcept()) {
			handleSingleConceptQuestion();
		} else if (questionTypeSingleInstanceWithProperty()) {
			handleSingleInstanceWithPropertyQuestion();
		} else {
			reportWarning("Unhandled question type", this.ac);
		}
	}

	private boolean questionTypeSimpleInstances() {
		boolean result = false;
		
		//The question just contains instances (no relations or concepts)
		
		if (this.concepts.isEmpty() && this.properties.isEmpty()) {
			result = (!listDomainInstances().isEmpty());
		}
		
		return result;
	}
	
	private boolean questionTypeSingleInstanceWithProperty() {
		boolean result = false;
	
		//DB - No longer test if concepts is empty...
//		if (this.concepts.isEmpty()) {
			result = (listDomainInstances().size() == 1);
			
			if (result) {
				result = (listUniqueProperties().size() == 1);
			}
//		}

		return result;
	}

	private boolean questionTypeSingleConcept() {
		boolean result = false;

		if (this.properties.isEmpty() && listDomainInstances().isEmpty()) {
			result = (listUniqueConcepts().size() == 1);
		}

		return result;
	}
	
	private ArrayList<CeConcept> listUniqueConcepts() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

		for (ArrayList<CeConcept> conList : this.concepts.values()) {
			for (CeConcept thisCon : conList) {
				if (!result.contains(thisCon)) {
					result.add(thisCon);
				}
			}
		}

		return result;
	}
	
	private ArrayList<CeProperty> listUniqueProperties() {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();

		for (ArrayList<CeProperty> propList : this.properties.values()) {
			for (CeProperty thisProp : propList) {
				if (!result.contains(thisProp)) {
					result.add(thisProp);
				}
			}
		}

		return result;
	}

//	private ArrayList<CeInstance> listUniqueInstances() {
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//
//		for (ArrayList<CeInstance> instList : this.instances.values()) {
//			for (CeInstance thisInst : instList) {
//				if (!result.contains(thisInst)) {
//					result.add(thisInst);
//				}
//			}
//		}
//
//		return result;
//	}

	private ArrayList<CeInstance> listDomainInstances() {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		//TODO: Other concepts in addition to modifier may need to be tested here
		
		for (ArrayList<CeInstance> instList : this.instances.values()) {
			for (CeInstance thisInst : instList) {
				if (!thisInst.isConceptNamed(this.ac, CON_MODIFIER)) {
					result.add(thisInst);
				}
			}
		}

		return result;
	}

//	private ArrayList<CeInstance> listModifierInstances() {
//		//This method may be useful when you implement modifier checking
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//
//		for (ArrayList<CeInstance> instList : this.instances.values()) {
//			for (CeInstance thisInst : instList) {
//				if (thisInst.isConceptNamed(this.ac, CON_MODIFIER)) {
//					result.add(thisInst);
//				}
//			}
//		}
//
//		return result;
//	}

	private void handleSimpleInstanceQuestion() {
		//just return the instance(s)
		this.resultInsts = listDomainInstances();
	}
	
	private void handleSingleInstanceWithPropertyQuestion() {
		//assume this is a filter question... For the stated instance list all values or
		//instances for the name property
		
		//TODO: Should really check that the property and instance are compatible (e.g. the domain of the property matches
		//to at least one concept of the instance)
		
		CeInstance tgtInst = listDomainInstances().get(0);
		ArrayList<CeProperty> tgtProps = this.properties.firstEntry().getValue();
		
		if (!tgtProps.isEmpty()) {
			CeProperty tgtProp = tgtProps.get(0);
			CePropertyInstance pi = tgtInst.getPropertyInstanceNamed(tgtProp.getPropertyName());

			if (pi != null) {
				if (tgtProp.isObjectProperty()) {
					this.resultInsts = new ArrayList<CeInstance>(pi.getValueInstanceList(this.ac));
				} else {
					this.resultVals = new ArrayList<String>(pi.getValueList());
				}		
			} else {
				reportDebug("No property instance was found", this.ac);
			}
		}
	}

	private void handleSingleConceptQuestion() {
		//assume it is a list and list all instances of that concept
		ArrayList<CeConcept> conList = this.concepts.firstEntry().getValue();
		
		if (!conList.isEmpty()) {
			CeConcept tgtCon = conList.get(0);

			this.resultInsts = this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, tgtCon.getConceptName());
		}
		
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

		result.put(JSON_QT, this.question.getQuestionText());

		if (!this.concepts.isEmpty()) {
			result.put(JSON_CO, createJsonForConcepts());
		}
		
		if (!this.properties.isEmpty()) {
			result.put(JSON_PR, createJsonForProperties());
		}
		
		if (!this.instances.isEmpty()) {
			result.put(JSON_IN, createJsonForInstances());
		}

		if (!this.resultInsts.isEmpty()) {
			result.put(JSON_ANI, createJsonForAnswerInstances());
		}

		if (!this.resultVals.isEmpty()) {
			result.put(JSON_ANV, createJsonForAnswerValues());
		}

		if (this.debug) {
			CeStoreJsonObject jDebug = new CeStoreJsonObject();
			jDebug.put(JSON_ET, System.currentTimeMillis() - this.startTime);
			createJsonAlerts(jDebug);

			result.put(JSON_DEBUG, jDebug);
		}

		return result;
	}

	private CeStoreJsonObject createJsonForConcepts() {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		for (String thisKey : this.concepts.keySet()) {
			ArrayList<CeConcept> conList = this.concepts.get(thisKey);
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (CeConcept thisCon : conList) {
				CeWebConcept wCon = new CeWebConcept(this.ac);
				CeStoreJsonObject jCon = wCon.generateSummaryDetailsJsonFor(thisCon);
				
				jArr.add(jCon);
			}

			jObj.put(thisKey, jArr);
		}

		return jObj;
	}

	private CeStoreJsonObject createJsonForProperties() {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		
		for (String thisKey : this.properties.keySet()) {
			ArrayList<CeProperty> propList = this.properties.get(thisKey);
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (CeProperty thisProp : propList) {
				CeWebProperty wProp = new CeWebProperty(this.ac);
				CeStoreJsonObject jProp = wProp.generateSummaryDetailsJsonFor(thisProp);
				
				jArr.add(jProp);
			}

			jObj.put(thisKey, jArr);
		}
		
		return jObj;
	}

	private CeStoreJsonObject createJsonForInstances() {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		for (String thisKey : this.instances.keySet()) {
			ArrayList<CeInstance> instList = this.instances.get(thisKey);
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (CeInstance thisInst : instList) {
				CeWebInstance wInst = new CeWebInstance(this.ac);
				CeStoreJsonObject jInst = wInst.generateSummaryDetailsJsonFor(thisInst, null, 0, false, false, null, false);

				jArr.add(jInst);
			}

			jObj.put(thisKey, jArr);
		}
		
		return jObj;
	}
	
	private CeStoreJsonObject createJsonForAnswerInstances() {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		
		for (CeInstance thisInst : this.resultInsts) {			
			CeWebInstance wInst = new CeWebInstance(this.ac);
			CeStoreJsonObject jInst = wInst.generateSummaryDetailsJsonFor(thisInst, null, 0, false, false, null, false);
			jObj.put(thisInst.getInstanceName(), jInst);
		}
		
		return jObj;
	}

	private CeStoreJsonArray createJsonForAnswerValues() {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		jArr.addAll(this.resultVals);

		return jArr;
	}

//	//This method may be useful later - configuration concepts should be excluded but right
//	//now we are in the midst of changing how these work...
//	private boolean isOnlyConfigCon(CeConcept pCon) {
//		boolean result = true;
//
//		CeInstance mmInst = pCon.retrieveMetaModelInstance(this.ac);
//
//		if (mmInst != null) {
//			for (CeConcept thisCon : mmInst.getAllLeafConcepts()) {
//				if (!thisCon.equalsOrHasParentNamed(this.ac, CON_CONFCON)) {
//					result = false;
//					break;
//				}
//			}
//		}
//
//		return result;
//	}

//	//This method may be useful later - configuration concepts should be excluded but right
//	//now we are in the midst of changing how these work...
//	private boolean isOnlyConfigCon(CeInstance pInst) {
//		boolean result = true;
//
//		for (CeConcept thisCon : pInst.getAllLeafConcepts()) {
//			result = isOnlyConfigCon(thisCon);
//
//			if (result == false) {
//				break;
//			}
//		}
//
//		return result;
//	}

}