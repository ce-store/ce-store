package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.hudson.helper.SpCollection;
import com.ibm.ets.ita.ce.store.hudson.helper.SpEnumeratedConcept;
import com.ibm.ets.ita.ce.store.hudson.helper.SpLinkedInstance;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeModelEntity;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class QuestionInterpreterHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_Q_TEXT = "question_text";
	private static final String JSON_WORDS = "words";
	private static final String JSON_CONS = "concepts";
	private static final String JSON_PROPS = "properties";
	private static final String JSON_INSTS = "instances";
	private static final String JSON_SPECS = "specials";

	private static final String CON_NUMWORD = "number word";
	private static final String CON_LINKEDPROP = "linked property";

	private HashMap<ProcessedWord, SpEnumeratedConcept> enumeratedConcepts = new HashMap<ProcessedWord, SpEnumeratedConcept>();
	private HashMap<ProcessedWord, SpCollection> collections = new HashMap<ProcessedWord, SpCollection>();
	private HashMap<ProcessedWord, SpLinkedInstance> linkedInstances = new HashMap<ProcessedWord, SpLinkedInstance>();

	public QuestionInterpreterHandler(WebActionContext pWc, boolean pDebug, String pQt, long pStartTime) {
		super(pWc, pDebug, Question.create(pQt), pStartTime);		
	}

	@Override
	public CeStoreJsonObject handleQuestion() {
		CeStoreJsonObject result = null;

		interpretQuestion();
		analyseSpecialCases();

		result = createResult();

		return result;
	}

	private void analyseSpecialCases() {
		analyseEnumeratedConcepts();
		analyseCollections();
		analyseLinkedConcepts();
	}

	private void analyseEnumeratedConcepts() {
		for (ProcessedWord thisWord : this.allWords) {
			if (isNumberWord(thisWord)) {
				ProcessedWord nextWord = thisWord.getNextProcessedWord();

				if (nextWord != null) {
					if (nextWord.isGroundedOnConcept()) {
						String label = thisWord.getWordText() + " " + nextWord.getWordText();
						SpEnumeratedConcept enCon = new SpEnumeratedConcept(thisWord, nextWord, label);

						this.enumeratedConcepts.put(thisWord, enCon);
					}
				}
			}
		}
	}

	private void analyseLinkedConcepts() {
		for (ProcessedWord thisWord : this.allWords) {
			for (CeInstance thisInst : thisWord.listGroundedInstances()) {
				for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
					CeProperty thisProp = thisPi.getRelatedProperty();
					CeInstance mmInst = thisProp.getMetaModelInstance(this.ac);

					if (mmInst.isConceptNamed(this.ac, CON_LINKEDPROP)) {
						ArrayList<CeInstance> relInsts = new ArrayList<CeInstance>();

						for (CeInstance relInst : thisPi.getValueInstanceList(this.ac)) {
							relInsts.add(relInst);
						}

						//TODO: The label should be different to this (could be longer)
						String label = thisWord.getWordText();
						SpLinkedInstance linCon = new SpLinkedInstance(thisInst, thisProp, relInsts, label);
						this.linkedInstances.put(thisWord, linCon);
					}
				}
			}
		}
	}

	private boolean isNumberWord(ProcessedWord pWord) {
		boolean result = false;

		result = pWord.isNumberWord();

		if (!result) {
			for (CeInstance thisInst : pWord.listGroundedInstances()) {
				if (thisInst.isConceptNamed(this.ac, CON_NUMWORD)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	private void analyseCollections() {
		SpCollection spColl = null;

		for (ProcessedWord thisWord : this.allWords) {
			if (isConnector(thisWord)) {
				ProcessedWord prevWord = thisWord.getPreviousProcessedWord();
				ProcessedWord nextWord = thisWord.getNextProcessedWord();

				if (spColl == null) {
					spColl = new SpCollection(prevWord);
				}

				ArrayList<CeModelEntity> matches = new ArrayList<CeModelEntity>();

				if (prevWord != null) {
					if (prevWord.isGroundedOnConcept()) {
						matches.addAll(prevWord.listGroundedConcepts());
					}

					if (prevWord.isGroundedOnProperty()) {
						matches.addAll(prevWord.listGroundedProperties());
					}

					if (prevWord.isGroundedOnInstance()) {
						matches.addAll(prevWord.listGroundedInstances());
					}

					spColl.addItem(prevWord, matches);
				}

				if (nextWord != null) {
					if (nextWord.isGroundedOnConcept()) {
						matches.addAll(nextWord.listGroundedConcepts());
					}

					if (nextWord.isGroundedOnProperty()) {
						matches.addAll(nextWord.listGroundedProperties());
					}

					if (nextWord.isGroundedOnInstance()) {
						matches.addAll(nextWord.listGroundedInstances());
					}

					spColl.addItem(nextWord, matches);
				}

				spColl.setConnectorWord(thisWord);

				this.collections.put(prevWord, spColl);
			}
		}

		if (spColl != null) {
			this.collections.put(spColl.getFirstWord(), spColl);
		}
	}

	private boolean isConnector(ProcessedWord pWord) {
		boolean result = false;

		for (CeInstance thisInst : pWord.listGroundedInstances()) {
			if (thisInst.isConceptNamed(this.ac, "connector word")) {
				result = true;
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

		result.put(JSON_Q_TEXT, getQuestionText());
		result.put(JSON_WORDS, createJsonForWords());
		result.put(JSON_CONS, createJsonForConcepts());
		result.put(JSON_PROPS, createJsonForProperties());
		result.put(JSON_INSTS, createJsonForInstances());
		result.put(JSON_SPECS, createJsonForSpecials());

		return result;
	}

	private CeStoreJsonArray createJsonForWords() {
		CeStoreJsonArray result = new CeStoreJsonArray();

		for (ProcessedWord thisPw : this.allWords) {
			result.add(thisPw.getWordText());
		}

		return result;
	}

	private CeStoreJsonObject createJsonForConcepts() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isGroundedOnConcept()) {
				TreeMap<String, CeConcept> conMap = thisWord.listGroundedConceptsAndKeys();

				for (String thisKey : conMap.keySet()) {
					CeConcept thisCon = conMap.get(thisKey);
					CeStoreJsonObject jCon = new CeStoreJsonObject();
					String keyText = thisWord.getWordText();

					jCon.put("type", "concept");
					jCon.put("name", thisCon.getConceptName());
					jCon.put("position", ctr);
					jCon.put("instance", jsonFor(thisCon));

					result.put(keyText, jCon);
				}
			}
			++ctr;
		}

		return result;
	}

	private CeStoreJsonObject createJsonForProperties() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isGroundedOnProperty()) {
				TreeMap<String,CeProperty> propMap = thisWord.listGroundedPropertiesAndKeys();

				for (String thisKey : propMap.keySet()) {
					CeProperty thisProp = propMap.get(thisKey);
					CeStoreJsonObject jProp = new CeStoreJsonObject();

					jProp.put("type", "property");
					jProp.put("name", thisProp.formattedFullPropertyName());
					jProp.put("position", ctr);
					jProp.put("instance", jsonFor(thisProp));

					result.put(thisKey, jProp);
				}
			}
			++ctr;
		}

		return result;
	}

	private CeStoreJsonObject createJsonForInstances() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isGroundedOnInstance()) {
				TreeMap<String, CeInstance> instMap = thisWord.listGroundedInstancesAndKeys();

				for (String thisKey : instMap.keySet()) {
					CeInstance thisInst = instMap.get(thisKey);
					CeStoreJsonObject jInst = new CeStoreJsonObject();

					jInst.put("type", "instance");
					jInst.put("name", thisInst.getInstanceName());
					jInst.put("position", ctr);
					jInst.put("instance", jsonFor(thisInst));

					result.put(thisKey, jInst);
				}
			}
			++ctr;
		}

		return result;
	}

	private CeStoreJsonObject createJsonForSpecials() {
		CeStoreJsonObject result = new CeStoreJsonObject();

		createJsonForNumberSpecials(result);
		createJsonForCollectionSpecials(result);
		createJsonForEnumeratedConceptSpecials(result);
		createJsonForLinkedInstanceSpecials(result);

		return result;
	}

	private void createJsonForNumberSpecials(CeStoreJsonObject pResult) {
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isNumberWord()) {
				CeStoreJsonObject jSpecial = new CeStoreJsonObject();
				String thisKey = thisWord.getWordText();

				jSpecial.put("type", "number");
				jSpecial.put("name", thisKey);
				jSpecial.put("position", ctr);

				pResult.put(thisKey, jSpecial);
			}

			++ctr;
		}
	}

	private void createJsonForCollectionSpecials(CeStoreJsonObject pResult) {
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			for (ProcessedWord colWord : this.collections.keySet()) {
				if (thisWord.equals(colWord)) {
					CeStoreJsonObject jSpecial = new CeStoreJsonObject();
					SpCollection col = this.collections.get(colWord);
					String thisKey = col.computeLabel();

					CeStoreJsonArray jEntList = new CeStoreJsonArray();

					for (ProcessedWord innerWord : col.getItems().keySet()) {
						ArrayList<CeModelEntity> entList = col.getItems().get(innerWord);
						CeStoreJsonObject jEntry = new CeStoreJsonObject();
						CeStoreJsonArray jArr = new CeStoreJsonArray();
	
						jEntry.put("text", col.getFirstWord().getWordText());
						jEntry.put("items", jArr);

						for (CeModelEntity thisEnt : entList) {
							String className = thisEnt.getClass().getSimpleName();

							if (className.equals("CeConcept")) {
								jArr.add(jsonFor((CeConcept)thisEnt));	
							} else if (className.equals("CeProperty")) {
								jArr.add(jsonFor((CeProperty)thisEnt));	
							} else if (className.equals("CeInstance")) {
								jArr.add(jsonFor((CeInstance)thisEnt));	
							} else {
								reportError("Unexpected class (" + className + ") during SpCollection processing", this.ac);
							}
						}

						jEntList.add(jEntry);
					}

					jSpecial.put("type", "collection");
					jSpecial.put("name", thisKey);
					jSpecial.put("position", ctr);
					jSpecial.put("connector", col.getConnectorWord().getWordText());
					jSpecial.put("contents", jEntList);

					pResult.put(thisKey, jSpecial);
				}
			}

			++ctr;
		}
	}

	private void createJsonForEnumeratedConceptSpecials(CeStoreJsonObject pResult) {
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			for (ProcessedWord ecWord : this.enumeratedConcepts.keySet()) {
				if (thisWord.equals(ecWord)) {
					CeStoreJsonObject jSpecial = new CeStoreJsonObject();
					SpEnumeratedConcept ec = this.enumeratedConcepts.get(ecWord);
					String thisKey = ec.getLabel();

					CeStoreJsonArray jConList = new CeStoreJsonArray();

					for (CeConcept thisCon : ec.getConceptWord().listGroundedConcepts()) {
						CeInstance mmInst = thisCon.retrieveMetaModelInstance(this.ac);

						jConList.add(jsonFor(mmInst));
					}

					jSpecial.put("type", "enumerated-concept");
					jSpecial.put("name", thisKey);
					jSpecial.put("position", ctr);
					jSpecial.put("number", ec.getNumberWord().getWordText());
					jSpecial.put("concepts", jConList);

					pResult.put(thisKey, jSpecial);
				}
			}

			++ctr;
		}
	}

	private void createJsonForLinkedInstanceSpecials(CeStoreJsonObject pResult) {
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			for (ProcessedWord liWord : this.linkedInstances.keySet()) {
				if (thisWord.equals(liWord)) {
					CeStoreJsonObject jSpecial = new CeStoreJsonObject();
					SpLinkedInstance li = this.linkedInstances.get(liWord);
					String thisKey = li.getLabel();

					jSpecial.put("type", "linked-instance");
					jSpecial.put("name", thisKey);
					jSpecial.put("position", ctr);
					jSpecial.put("matched_instance", jsonFor(li.getMatchedInstance()));
					jSpecial.put("linked_instance", jsonFor(li.getLinkedInstances()));
					jSpecial.put("property", jsonFor(li.getLinkingProperty()));

					pResult.put(thisKey, jSpecial);
				}
			}

			++ctr;
		}
	}

	private CeStoreJsonObject jsonFor(CeInstance pInst) {
		CeWebInstance webInst = new CeWebInstance(this.ac);
		CeStoreJsonObject result = webInst.generateNormalisedDetailsJsonFor(pInst, null, 0, false, false, null);

		return result;
	}

	private CeStoreJsonArray jsonFor(ArrayList<CeInstance> pInstList) {
		CeStoreJsonArray result = new CeStoreJsonArray();

		for (CeInstance thisInst : pInstList) {
			result.add(jsonFor(thisInst));
		}

		return result;
	}

	private CeStoreJsonObject jsonFor(CeConcept pCon) {
		CeStoreJsonObject result = null;
		CeInstance mmInst = pCon.retrieveMetaModelInstance(this.ac);

		if (mmInst != null) {
			result = jsonFor(mmInst);
		}

		return result;
	}

	private CeStoreJsonObject jsonFor(CeProperty pProp) {
		CeStoreJsonObject result = null;
		CeInstance mmInst = pProp.getMetaModelInstance(this.ac);

		if (mmInst != null) {
			result = jsonFor(mmInst);
		}

		return result;
	}

}
