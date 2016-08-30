package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.hudson.helper.SpCollection;
import com.ibm.ets.ita.ce.store.hudson.helper.SpEnumeratedConcept;
import com.ibm.ets.ita.ce.store.hudson.helper.SpLinkedInstance;
import com.ibm.ets.ita.ce.store.hudson.helper.SpMatchedTriple;
import com.ibm.ets.ita.ce.store.hudson.helper.SpNumber;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeModelEntity;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class QuestionInterpreterHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_Q_TEXT = "question_text";
	private static final String JSON_Q_CONF = "confidence";
	private static final String JSON_Q_CONFEXP = "confidence_explanation";
	private static final String JSON_WORDS = "words";
	private static final String JSON_CONS = "concepts";
	private static final String JSON_PROPS = "properties";
	private static final String JSON_INSTS = "instances";
	private static final String JSON_SPECS = "specials";

	private static final String CON_NUMWORD = "number word";
	private static final String CON_LINKEDPROP = "linked property";
	
	private static final int TYPE_ALL = 0;
	private static final int TYPE_BEFORE = 1;
	private static final int TYPE_AFTER = 2;

	private HashMap<ProcessedWord, SpNumber> numbers = new HashMap<ProcessedWord, SpNumber>();
	private HashMap<ProcessedWord, SpEnumeratedConcept> enumeratedConcepts = new HashMap<ProcessedWord, SpEnumeratedConcept>();
	private HashMap<String, SpCollection> collections = new HashMap<String, SpCollection>();
	private HashMap<ProcessedWord, SpLinkedInstance> linkedInstances = new HashMap<ProcessedWord, SpLinkedInstance>();
	private HashMap<ProcessedWord, SpMatchedTriple> matchedTriples = new HashMap<ProcessedWord, SpMatchedTriple>();

	public QuestionInterpreterHandler(ActionContext pAc, boolean pDebug, String pQt, long pStartTime) {
		super(pAc, pDebug, Question.create(pQt), pStartTime);		
	}

	public static CeStoreJsonArray jsonFor(ActionContext pAc, ArrayList<CeInstance> pInstList) {
		CeStoreJsonArray result = new CeStoreJsonArray();

		for (CeInstance thisInst : pInstList) {
			result.add(QuestionInterpreterHandler.jsonFor(pAc, thisInst));
		}

		return result;
	}

	public static CeStoreJsonObject jsonFor(ActionContext pAc, CeInstance pInst) {
		CeWebInstance webInst = new CeWebInstance(pAc);
		CeStoreJsonObject result = webInst.generateNormalisedDetailsJsonFor(pInst, null, 0, false, false, null);

		return result;
	}

	public static CeStoreJsonObject jsonFor(ActionContext pAc, CeConcept pCon) {
		CeStoreJsonObject result = null;
		CeInstance mmInst = pCon.retrieveMetaModelInstance(pAc);

		if (mmInst != null) {
			result = jsonFor(pAc, mmInst);
		}

		return result;
	}

	public static CeStoreJsonObject jsonFor(ActionContext pAc, CeProperty pProp) {
		CeStoreJsonObject result = null;
		CeInstance mmInst = pProp.getMetaModelInstance(pAc);

		if (mmInst != null) {
			result = jsonFor(pAc, mmInst);
		}

		return result;
	}

	@Override
	public CeStoreJsonObject handleQuestion() {
		CeStoreJsonObject result = null;

		interpretQuestion();
		refineMatches();
		analyseSpecialCases();
		calculateConfidence();

		result = createResult();

		return result;
	}
	
	private void refineMatches() {
		//TODO: Should this be done with instances and concepts too?

		for (ProcessedWord thisWord : this.allWords) {
			ProcessedWord prevWord = thisWord.getPreviousProcessedWord();
			
			if (prevWord != null) {
				TreeMap<String, CeProperty> propMap1 = thisWord.listGroundedPropertiesAndKeys();
				TreeMap<String, CeProperty> propMap2 = prevWord.listGroundedPropertiesAndKeys();
				
				if (!propMap1.isEmpty() && !propMap2.isEmpty()) {
					for (String wordKey1 : propMap1.keySet()) {
						CeProperty prop1 = propMap1.get(wordKey1);
						
						for (String wordKey2 : propMap2.keySet()) {
							CeProperty prop2 = propMap2.get(wordKey2);
							
							if (prop1.equals(prop2)) {
								if (wordKey2.contains(wordKey1)) {
									thisWord.removeReferredRelation(wordKey1);
								}
							}
						}
					}
				}
			}
		}
	}

	private void analyseSpecialCases() {
		analyseNumbers();
		analyseEnumeratedConcepts();
		analyseCollections();
		analyseLinkedConcepts();
		analyseMatchedTriples();
	}

	private void analyseMatchedTriples() {
		for (ProcessedWord thisPw : this.allWords) {
			TreeMap<String, CeProperty> propList = thisPw.listGroundedPropertiesAndKeys();
			for (String thisKey : propList.keySet()) {
				CeProperty thisProp = propList.get(thisKey);

				ArrayList<CeInstance> subjectList = null;
				ArrayList<CeInstance> objectList = null;
				
				if (thisProp.isObjectProperty()) {
					subjectList = seekPossibleSubjectsFor(thisProp, thisPw);
					objectList = seekPossibleObjectsFor(thisProp, thisPw);
				} else {
					subjectList = seekPossibleSubjectsFor(thisProp);
					objectList = new ArrayList<CeInstance>();
				}

				if (!subjectList.isEmpty() || !objectList.isEmpty()) {
					//This is a triple (2 or 3 of the triple are matched)
					SpMatchedTriple mt = new SpMatchedTriple(thisKey, thisProp);
					mt.setSubjects(subjectList);
					mt.setObjects(objectList);

					this.matchedTriples.put(thisPw, mt);
				}
			}
		}
	}

	private ArrayList<CeInstance> seekPossibleSubjectsFor(CeProperty pProp, ProcessedWord pWord) {
		return seekPossibleInstanceMatchesForConcept(pProp.getDomainConcept(), pWord, TYPE_BEFORE);
	}

	private ArrayList<CeInstance> seekPossibleSubjectsFor(CeProperty pProp) {
		return seekPossibleInstanceMatchesForConcept(pProp.getDomainConcept(), null, TYPE_ALL);
	}

	private ArrayList<CeInstance> seekPossibleObjectsFor(CeProperty pProp, ProcessedWord pWord) {
		ArrayList<CeInstance> result = null;

		if (pProp.isObjectProperty()) {
			result = seekPossibleInstanceMatchesForConcept(pProp.getRangeConcept(), pWord, TYPE_AFTER);
		} else {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	private ArrayList<CeInstance> seekPossibleInstanceMatchesForConcept(CeConcept pCon, ProcessedWord pWord, int pType) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		for (ProcessedWord thisWord : this.allWords) {
			int thisPos = thisWord.getWordPos();
			boolean doTest = false;

			if (pType == TYPE_ALL) {
				doTest = true;
			} else {
				int matchPos = pWord.getWordPos();
				if (thisPos < matchPos) {
					if (pType == TYPE_BEFORE) {
						doTest = true;
					}
				} else if (thisPos > matchPos) {
					if (pType == TYPE_AFTER) {
						doTest = true;
					}
				}
			}

			if (doTest) {
				for (CeInstance thisInst : thisWord.listGroundedInstances()) {
					if (thisInst.isConceptNamed(this.ac, pCon.getConceptName())) {
						result.add(thisInst);
					}
				}
			}
		}

		return result;
	}

	private void calculateConfidence() {
		int computedConfidence = 0;
		float groundedCount = 0;
		float wordCount = this.allWords.size();
		String expText = "";

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isGrounded() || thisWord.isLaterPartOfPartial() || thisWord.isNumberWord()) {
				++groundedCount;
			} else {
				if (!expText.isEmpty()) {
					expText += ", ";
				}
				expText += "'" + thisWord.getWordText() +"'";
			}
		}

		float ratio = groundedCount / wordCount;
		computedConfidence = new Float(ratio * 100).intValue();

		this.question.setInterpretationConfidence(computedConfidence);
		
		if (!expText.isEmpty()) {
			if ((wordCount - groundedCount) == 1) {
				expText = "The word " + expText;
				expText += " was not matched";
			} else {
				expText = "The words " + expText;
				expText += " were not matched";
			}
			this.question.setInterpretationConfidenceExplanation(expText);
		}
	}

	private void analyseNumbers() {
		for (ProcessedWord thisWord : this.allWords) {
			if (isNumberWord(thisWord)) {
				SpNumber spNum = new SpNumber(thisWord.getWordText());

				this.numbers.put(thisWord, spNum);
			}
		}
	}

	private void analyseEnumeratedConcepts() {
		for (ProcessedWord thisWord : this.allWords) {
			if (isNumberWord(thisWord)) {
				ProcessedWord nextWord = thisWord.getNextProcessedWord();

				if (nextWord != null) {
					if (nextWord.isGroundedOnConcept()) {
						String label = thisWord.getWordText() + " " + nextWord.getWordText();
						SpEnumeratedConcept enCon = new SpEnumeratedConcept(thisWord.getWordText(), nextWord.getWordText(), nextWord.listGroundedConcepts(), label);

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
			ArrayList<CeModelEntity> matches = null;

			if (isConnector(thisWord)) {
				ProcessedWord prevWord = thisWord.getPreviousProcessedWord();
				ProcessedWord nextWord = thisWord.getNextProcessedWord();

				if (spColl == null) {
					spColl = new SpCollection(prevWord.getWordText());
				}

				if (prevWord != null) {
					matches = new ArrayList<CeModelEntity>();

					if (prevWord.isGroundedOnConcept()) {
						matches.addAll(prevWord.listGroundedConcepts());
					}

					if (prevWord.isGroundedOnProperty()) {
						matches.addAll(prevWord.listGroundedProperties());
					}

					if (prevWord.isGroundedOnInstance()) {
						matches.addAll(prevWord.listGroundedInstances());
					}

					spColl.addItem(prevWord.getWordText(), matches);
				}

				if (nextWord != null) {
					matches = new ArrayList<CeModelEntity>();

					if (nextWord.isGroundedOnConcept()) {
						matches.addAll(nextWord.listGroundedConcepts());
					}

					if (nextWord.isGroundedOnProperty()) {
						matches.addAll(nextWord.listGroundedProperties());
					}

					if (nextWord.isGroundedOnInstance()) {
						matches.addAll(nextWord.listGroundedInstances());
					}

					spColl.addItem(nextWord.getWordText(), matches);
				}

				spColl.setConnectorWordText(thisWord.getWordText());

//				this.collections.put(prevWord, spColl);
			}
		}

		if (spColl != null) {
			this.collections.put(spColl.getFirstWordText(), spColl);
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
		result.put(JSON_Q_CONF, getInterpretationConfidence());
		result.put(JSON_Q_CONFEXP, getInterpretationConfidenceExplanation());
		result.put(JSON_WORDS, createJsonForWords());
		result.put(JSON_CONS, createJsonForConcepts());
		result.put(JSON_PROPS, createJsonForProperties());
		result.put(JSON_INSTS, createJsonForInstances());
		result.put(JSON_SPECS, createJsonForSpecials());

		return result;
	}

	private int getInterpretationConfidence() {
		return this.question.getInterpretationConfidence();
	}

	private String getInterpretationConfidenceExplanation() {
		return this.question.getInterpretationConfidenceExplanation();
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
					jCon.put("instance", jsonFor(this.ac, thisCon));

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
					jProp.put("instance", jsonFor(this.ac, thisProp));

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

					if (!isAlreadyMatchedToConceptOrProperty(thisWord, thisInst)) {
						CeStoreJsonObject jInst = new CeStoreJsonObject();
	
						jInst.put("type", "instance");
						jInst.put("name", thisInst.getInstanceName());
						jInst.put("position", ctr);
						jInst.put("instance", jsonFor(this.ac, thisInst));
	
						result.put(thisKey, jInst);
					}
				}
			}
			++ctr;
		}

		return result;
	}

	private boolean isAlreadyMatchedToConceptOrProperty(ProcessedWord pWord, CeInstance pInst) {
		boolean result = false;
		String instName = pInst.getInstanceName();

		for (CeConcept thisCon : pWord.listGroundedConcepts()) {
			if (thisCon.getConceptName().equals(instName)) {
				result = true;
				break;
			}
		}
		
		if (!result) {
			for (CeProperty thisProp : pWord.listGroundedProperties()) {
				if (thisProp.formattedFullPropertyName().equals(instName)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}

	private CeStoreJsonObject createJsonForSpecials() {
		CeStoreJsonObject result = new CeStoreJsonObject();

		createJsonForNumberSpecials(result);
		createJsonForCollectionSpecials(result);
		createJsonForEnumeratedConceptSpecials(result);
		createJsonForLinkedInstanceSpecials(result);
		createJsonForMatchedTripleSpecials(result);

		return result;
	}

	private void createJsonForNumberSpecials(CeStoreJsonObject pResult) {
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			for (ProcessedWord numWord : this.numbers.keySet()) {
				if (thisWord.equals(numWord)) {
					SpNumber num = this.numbers.get(numWord);
					CeStoreJsonObject jSpecial = num.toJson(this.ac, ctr);

					pResult.put(num.getLabel(), jSpecial);
				}
			}

			++ctr;
		}
	}

	private void createJsonForCollectionSpecials(CeStoreJsonObject pResult) {
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			for (String wordText : this.collections.keySet()) {
				if (thisWord.getWordText().equals(wordText)) {
					SpCollection col = this.collections.get(wordText);
					CeStoreJsonObject jSpecial = col.toJson(this.ac, ctr);

					pResult.put(col.computeLabel(), jSpecial);
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
					SpEnumeratedConcept ec = this.enumeratedConcepts.get(ecWord);
					String thisKey = ec.getLabel();
					CeStoreJsonObject jSpecial = ec.toJson(this.ac, ctr);

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
					SpLinkedInstance li = this.linkedInstances.get(liWord);
					String thisKey = li.getLabel();
					CeStoreJsonObject jSpecial = li.toJson(this.ac, ctr);

					pResult.put(thisKey, jSpecial);
				}
			}

			++ctr;
		}
	}

	private void createJsonForMatchedTripleSpecials(CeStoreJsonObject pResult) {
		int ctr = 0;

		for (ProcessedWord thisWord : this.allWords) {
			for (ProcessedWord mtWord : this.matchedTriples.keySet()) {
				if (thisWord.equals(mtWord)) {
					SpMatchedTriple mt = this.matchedTriples.get(mtWord);
					String thisKey = mt.getLabel();
					CeStoreJsonObject jSpecial = mt.toJson(this.ac, ctr);

					pResult.put(thisKey, jSpecial);
				}
			}

			++ctr;
		}
	}

}
