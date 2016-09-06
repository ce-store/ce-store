package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.hudson.model.Question;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpCollection;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpEnumeratedConcept;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpLinkedInstance;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpMatchedTriple;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpMultiMatch;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpNumber;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class QuestionInterpreterHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_QUES = "question";
	private static final String JSON_TEXT = "text";
	private static final String JSON_WORDS = "words";

	private static final String JSON_INTS = "interpretations";
	private static final String JSON_CONF = "confidence";
	private static final String JSON_EXP = "explanation";
	private static final String JSON_RES = "result";

	public static final String JSON_CONS = "concepts";
	public static final String JSON_PROPS = "properties";
	public static final String JSON_INSTS = "instances";
	private static final String JSON_SPECS = "specials";

	private static final String JSON_ENTS = "entities";
	private static final String JSON_PHRASE = "phrase";
	private static final String JSON_STARTPOS = "start position";
	private static final String JSON_ENDPOS = "end position";

	private static final String CON_NUMWORD = "number word";
	private static final String CON_LINKEDPROP = "linked property";
	
	private static final String CON_QPHRASE = "question phrase";
	private static final String CON_QWORD = "question word";
	private static final String CON_COMWORD = "common word";
	private static final String CON_CONNWORD = "connector word";
	private static final String CON_MULTIMATCH = "multimatch thing";

	private static final String[] CONLIST_OTHERS = { CON_QPHRASE, CON_QWORD, CON_COMWORD, CON_MODIFIER, CON_CONNWORD };

	private static final int TYPE_ALL = 0;
	private static final int TYPE_BEFORE = 1;
	private static final int TYPE_AFTER = 2;

	private ArrayList<SpNumber> numbers = new ArrayList<SpNumber>();
	private ArrayList<SpEnumeratedConcept> enumeratedConcepts = new ArrayList<SpEnumeratedConcept>();
	private ArrayList<SpCollection> collections = new ArrayList<SpCollection>();
	private ArrayList<SpLinkedInstance> linkedInstances = new ArrayList<SpLinkedInstance>();
	private ArrayList<SpMatchedTriple> matchedTriples = new ArrayList<SpMatchedTriple>();
	private ArrayList<SpMultiMatch> multiMatches = new ArrayList<SpMultiMatch>();

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

	public static CeStoreJsonArray jsonForMatchedItemInstances(ActionContext pAc, ArrayList<MatchedItem> pInstList) {
		CeStoreJsonArray result = new CeStoreJsonArray();

		for (MatchedItem thisMi : pInstList) {
			if (thisMi.hasInstance()) {
				ProcessedWord thisWord = thisMi.getFirstWord();

				TreeMap<String, ArrayList<MatchedItem>> instMap = thisWord.getMatchedItemInstanceMap();

				for (String thisKey : instMap.keySet()) {
					ArrayList<MatchedItem> instList = instMap.get(thisKey);
					CeStoreJsonObject jInst = new CeStoreJsonObject();
					CeStoreJsonArray eArr = new CeStoreJsonArray();
					MatchedItem firstItem = instList.get(0);

					for (MatchedItem mi : instList) {
						eArr.add(jsonFor(pAc, mi.getInstance()));
					}

					jInst = jsonForMatchedItem(firstItem, eArr);

					result.add(jInst);
				}
			}
		}

		return result;
	}

	public static CeStoreJsonObject jsonForMatchedItemInstance(ActionContext pAc, MatchedItem pMi) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		if (pMi.hasInstance()) {
			ProcessedWord thisWord = pMi.getFirstWord();

			TreeMap<String, ArrayList<MatchedItem>> instMap = thisWord.getMatchedItemInstanceMap();

			for (String thisKey : instMap.keySet()) {
				ArrayList<MatchedItem> instList = instMap.get(thisKey);
				CeStoreJsonArray eArr = new CeStoreJsonArray();
				MatchedItem firstItem = instList.get(0);

				for (MatchedItem mi : instList) {
					eArr.add(jsonFor(pAc, mi.getInstance()));
				}

				result = jsonForMatchedItem(firstItem, eArr);
			}
		}

		return result;
	}

	public static CeStoreJsonObject jsonForMatchedItemProperty(ActionContext pAc, MatchedItem pMiProp) {
		CeStoreJsonObject jProp = new CeStoreJsonObject();
		CeStoreJsonArray eArr = new CeStoreJsonArray();

		eArr.add(jsonFor(pAc, pMiProp.getProperty()));

		jProp = jsonForMatchedItem(pMiProp, eArr);

		return jProp;
	}

	public static CeStoreJsonObject jsonFor(ActionContext pAc, CeInstance pInst) {
		CeWebInstance webInst = new CeWebInstance(pAc);
		CeStoreJsonObject result = webInst.generateNormalisedDetailsJsonFor(pInst, null, 0, false, false, null);

		return result;
	}

	public static CeStoreJsonObject jsonFor(ActionContext pAc, CeInstance pInst, MatchedItem pMi) {
		CeStoreJsonObject result = null;
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		CeWebInstance webInst = new CeWebInstance(pAc);
		CeStoreJsonObject jInst = webInst.generateNormalisedDetailsJsonFor(pInst, null, 0, false, false, null);
		
		jArr.add(jInst);
		
		result = jsonForMatchedItem(pMi, jArr);
		
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

	public static CeStoreJsonObject jsonForMatchedItem(MatchedItem pMi, CeStoreJsonArray pArr) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		jObj.put(JSON_PHRASE, pMi.getPhraseText());
		jObj.put(JSON_STARTPOS, pMi.getStartPos());
		jObj.put(JSON_ENDPOS, pMi.getEndPos());
		jObj.put(JSON_ENTS, pArr);

		return jObj;
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
		refinePropertyMatches();
		refineConceptMatches();
		refineInstanceMatches();
	}

	private void refinePropertyMatches() {
		for (ProcessedWord thisWord : this.allWords) {
			ProcessedWord prevWord = thisWord.getPreviousProcessedWord();

			if (prevWord != null) {
				TreeMap<String, ArrayList<CeProperty>> propMap1 = thisWord.listGroundedPropertiesAndKeys();
				TreeMap<String, ArrayList<CeProperty>> propMap2 = prevWord.listGroundedPropertiesAndKeys();

				if (!propMap1.isEmpty() && !propMap2.isEmpty()) {
					for (String wordKey1 : propMap1.keySet()) {
						for (CeProperty prop1 : propMap1.get(wordKey1)) {
							for (String wordKey2 : propMap2.keySet()) {
								for (CeProperty prop2 : propMap2.get(wordKey2)) {
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
		}
	}

	private void refineConceptMatches() {
		for (ProcessedWord thisWord : this.allWords) {
			ProcessedWord prevWord = thisWord.getPreviousProcessedWord();
			
			if (prevWord != null) {
				TreeMap<String, ArrayList<CeConcept>> conMap1 = thisWord.listGroundedConceptsAndKeys();
				TreeMap<String, ArrayList<CeConcept>> conMap2 = prevWord.listGroundedConceptsAndKeys();
				
				if (!conMap1.isEmpty() && !conMap2.isEmpty()) {
					for (String wordKey1 : conMap1.keySet()) {
						for (CeConcept con1 : conMap1.get(wordKey1)) {
							for (String wordKey2 : conMap2.keySet()) {
								for (CeConcept con2 : conMap2.get(wordKey2)) {

									if (con1.equals(con2)) {
										if (wordKey2.contains(wordKey1)) {
											thisWord.removeReferredConcept(wordKey1);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void refineInstanceMatches() {
		for (ProcessedWord thisWord : this.allWords) {
			ProcessedWord prevWord = thisWord.getPreviousProcessedWord();
			
			if (prevWord != null) {
				TreeMap<String, ArrayList<CeInstance>> instMap1 = thisWord.listGroundedInstancesAndKeys();
				TreeMap<String, ArrayList<CeInstance>> instMap2 = prevWord.listGroundedInstancesAndKeys();
				
				if (!instMap1.isEmpty() && !instMap2.isEmpty()) {
					for (String wordKey1 : instMap1.keySet()) {
						for (CeInstance inst1 : instMap1.get(wordKey1)) {
							for (String wordKey2 : instMap2.keySet()) {
								for (CeInstance inst2 : instMap2.get(wordKey2)) {
									if (inst1.equals(inst2)) {
										if (wordKey2.contains(wordKey1)) {
											thisWord.removeReferredInstance(wordKey1);
										}
									}
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
		analyseMultiMatches();
	}

	private void analyseMatchedTriples() {
		for (ProcessedWord thisPw : this.allWords) {
			TreeMap<String, ArrayList<MatchedItem>> propList = thisPw.getMatchedItemPropertyMap();

			for (String thisKey : propList.keySet()) {
				for (MatchedItem thisMi : propList.get(thisKey)) {
					ArrayList<MatchedItem> subjectList = null;
					ArrayList<MatchedItem> objectList = null;
					CeProperty thisProp = thisMi.getProperty();

					if (thisProp.isObjectProperty()) {
						subjectList = seekPossibleSubjectsFor(thisProp, thisPw);
						objectList = seekPossibleObjectsFor(thisProp, thisPw);
					} else {
						subjectList = seekPossibleSubjectsFor(thisProp);
						objectList = new ArrayList<MatchedItem>();
					}

					if (!subjectList.isEmpty() || !objectList.isEmpty()) {
						String phraseText = null;
						int startPos = -1;
						int endPos = -1;
	
						if (!subjectList.isEmpty() && !objectList.isEmpty()) {
							//Subject, object and predicate
							MatchedItem firstSubj = subjectList.get(0);
							MatchedItem firstObj = objectList.get(0);
							phraseText = firstSubj.getPhraseText() + " " + thisMi.getPhraseText() + " " + firstObj.getPhraseText();
							startPos = firstSubj.getStartPos();
							endPos = firstObj.getEndPos();
						} else if (!subjectList.isEmpty()) {
							//Subject and predicate only
							MatchedItem firstSubj = subjectList.get(0);

							if (thisMi.getStartPos() > firstSubj.getStartPos()) {
								phraseText = firstSubj.getPhraseText() + " " + intermediatePhraseText(firstSubj, thisMi) + thisMi.getPhraseText();
								startPos = firstSubj.getStartPos();
								endPos = thisMi.getEndPos();
							} else {
								phraseText = thisMi.getPhraseText() + " "  + intermediatePhraseText(thisMi, firstSubj) + firstSubj.getPhraseText();
								startPos = thisMi.getStartPos();
								endPos = firstSubj.getEndPos();
							}
						} else if (!objectList.isEmpty()) {
							//Object and predicate only
							MatchedItem firstObj = objectList.get(0);
							
							phraseText = thisMi.getPhraseText() + " " + firstObj.getPhraseText();
							startPos = thisMi.getStartPos();
							endPos = firstObj.getEndPos();
						}

						SpMatchedTriple mt = new SpMatchedTriple(phraseText, startPos, endPos, thisMi, subjectList, objectList);
						this.matchedTriples.add(mt);
					}
				}
			}
		}
	}

	private void analyseMultiMatches() {
		ArrayList<MatchedItem> allInstMis = new ArrayList<MatchedItem>();

		for (ProcessedWord thisWord : this.allWords) {
			for (MatchedItem thisMi : thisWord.getMatchedItems()) {
				if (thisMi.hasInstance()) {
					if (!allInstMis.contains(thisMi)) {
						allInstMis.add(thisMi);
					}
				}
			}
		}

		if (allInstMis.size() > 1) {
			for (MatchedItem mi1 : allInstMis) {
				CeInstance inst1 = mi1.getInstance();

				for (CePropertyInstance thisPi : inst1.getReferringPropertyInstances()) {
					CeInstance relInst = thisPi.getRelatedInstance();

					if (relInst != null) {
						if (relInst.isConceptNamed(this.ac, CON_MULTIMATCH)) {
							for (CeInstance inst2 : relInst.getAllRelatedInstances(this.ac)) {
								if (!inst2.equals(inst1)) {
									for (MatchedItem mi2 : allInstMis) {
										if (mi2.getInstance().equals(inst2)) {
											int pos1 = mi1.getStartPos();
											int pos2 = mi2.getEndPos();
											
											if (pos2 > pos1) {
												String phraseText = mi1.getPhraseText() + " " + mi2.getPhraseText();
												SpMultiMatch smm = new SpMultiMatch(phraseText, pos1, pos2, mi1, mi2, relInst);

												this.multiMatches.add(smm);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private String intermediatePhraseText(MatchedItem pItem1, MatchedItem pItem2) {
		String result = "";

		int pos1 = pItem1.getEndPos() + 1;
		int pos2 = pItem2.getStartPos();

		if (pos1 < pos2) {
			for (int i = pos1; i < pos2; i++) {
				result += phraseAtPos(i) + " ";
			}
		}

		return result;
	}

	private String phraseAtPos(int pPos) {
		String result = "";

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.getWordPos() == pPos) {
				result = thisWord.getLcWordText();
			}
		}

		return result;
	}

	private ArrayList<MatchedItem> seekPossibleSubjectsFor(CeProperty pProp, ProcessedWord pWord) {
		return seekPossibleInstanceMatchesForConcept(pProp.getDomainConcept(), pWord, TYPE_BEFORE);
	}

	private ArrayList<MatchedItem> seekPossibleSubjectsFor(CeProperty pProp) {
		return seekPossibleInstanceMatchesForConcept(pProp.getDomainConcept(), null, TYPE_ALL);
	}

	private ArrayList<MatchedItem> seekPossibleObjectsFor(CeProperty pProp, ProcessedWord pWord) {
		ArrayList<MatchedItem> result = null;

		if (pProp.isObjectProperty()) {
			result = seekPossibleInstanceMatchesForConcept(pProp.getRangeConcept(), pWord, TYPE_AFTER);
		} else {
			result = new ArrayList<MatchedItem>();
		}

		return result;
	}

	private ArrayList<MatchedItem> seekPossibleInstanceMatchesForConcept(CeConcept pCon, ProcessedWord pWord, int pType) {
		ArrayList<MatchedItem> result = new ArrayList<MatchedItem>();

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
				for (MatchedItem thisMi : thisWord.getMatchedItems()) {
					if (thisMi.hasInstance()) {
						CeInstance thisInst = thisMi.getInstance();

						if (thisInst.isConceptNamed(this.ac, pCon.getConceptName())) {
							result.add(thisMi);
						}
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
			if (thisWord.isGrounded() || thisWord.hasOtherMatchedItems() || thisWord.isNumberWord()) {
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
				SpNumber spNum = new SpNumber(thisWord.getWordText(), thisWord.getWordPos());

				this.numbers.add(spNum);
			}
		}
	}

	private void analyseEnumeratedConcepts() {
		for (ProcessedWord thisWord : this.allWords) {
			if (isNumberWord(thisWord)) {
				ProcessedWord nextWord = thisWord.getNextProcessedWord();

				if (nextWord != null) {
					if (nextWord.isGroundedOnConcept()) {
						TreeMap<String, ArrayList<MatchedItem>> conMap = nextWord.getMatchedItemConceptMap();

						for (String phraseText : conMap.keySet()) {
							ArrayList<MatchedItem> conList = conMap.get(phraseText);
							String wholePhrase = thisWord.getWordText() + " " + phraseText;
							int endPos = conList.get(0).getLastWord().getWordPos();

							SpEnumeratedConcept enCon = new SpEnumeratedConcept(wholePhrase, endPos, thisWord, conList);

							this.enumeratedConcepts.add(enCon);
						}
					}
				}
			}
		}
	}

	private void analyseLinkedConcepts() {
		for (ProcessedWord thisWord : this.allWords) {
			TreeMap<String, ArrayList<MatchedItem>> instMap = thisWord.getMatchedItemInstanceMap();

			for (String phraseText : instMap.keySet()) {
				for (MatchedItem thisMi : instMap.get(phraseText)) {
					CeInstance thisInst = thisMi.getInstance();

					for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
						CeProperty thisProp = thisPi.getRelatedProperty();
						CeInstance mmInst = thisProp.getMetaModelInstance(this.ac);

						if (mmInst.isConceptNamed(this.ac, CON_LINKEDPROP)) {
							ArrayList<CeInstance> relInsts = new ArrayList<CeInstance>();

							for (CeInstance relInst : thisPi.getValueInstanceList(this.ac)) {
								relInsts.add(relInst);
							}

							SpLinkedInstance linCon = new SpLinkedInstance(phraseText, thisMi, thisProp, relInsts);
							this.linkedInstances.add(linCon);
						}
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

	private boolean isDomain(ProcessedWord pWord) {
		boolean result = false;
		
		if (!pWord.listGroundedConcepts().isEmpty()) {
			result = true;
		} else if (!pWord.listGroundedProperties().isEmpty()) {
			result = true;
		} else {
			for (CeInstance thisInst : pWord.listGroundedInstances()) {
				if (!thisInst.isOnlyConceptNamed(this.ac, CON_CONFCON)) {
					if (!thisInst.isConceptNamed(this.ac, CONLIST_OTHERS)) {
						result = true;
					}
				}
			}
		}

		return result;
	}
	
	private void analyseCollections() {
		for (ProcessedWord thisWord : this.allWords) {
			if (isConnector(thisWord)) {
				ProcessedWord prevWord = thisWord.getPreviousProcessedWord();
				ProcessedWord nextWord = thisWord.getNextProcessedWord();

				if ((nextWord != null) && (prevWord != null)) {
					if (isDomain(prevWord) && isDomain(nextWord)) {
						ArrayList<MatchedItem> connItems = thisWord.getMatchedItems();
						ArrayList<MatchedItem> firstItems = prevWord.getMatchedItems();
						ArrayList<MatchedItem> lastItems = nextWord.getMatchedItems();
						ArrayList<MatchedItem> allItems = new ArrayList<MatchedItem>();
						MatchedItem firstItem = firstItems.get(0);
						MatchedItem connItem = connItems.get(0);
						MatchedItem lastItem = lastItems.get(0);
						String phraseText = firstItem.getPhraseText() + " " + connItem.getPhraseText() + " " + lastItem.getPhraseText();
						int startPos = firstItem.getStartPos();
						int endPos = lastItem.getEndPos();

						for (MatchedItem mi : firstItems) {
							if (!isAlreadyMatchedToConceptOrProperty(prevWord, mi)) {
								if (!allItems.contains(mi)) {
									allItems.add(mi);
								}
							}
						}

						for (MatchedItem mi : lastItems) {
							if (!isAlreadyMatchedToConceptOrProperty(nextWord, mi)) {
								if (!allItems.contains(mi)) {
									allItems.add(mi);
								}
							}
						}

						SpCollection spColl = new SpCollection(phraseText, startPos, endPos, connItems, allItems);
						this.collections.add(spColl);
					}
				}
			}
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
		CeStoreJsonObject qObj = new CeStoreJsonObject();
		CeStoreJsonObject iObj = new CeStoreJsonObject();
		CeStoreJsonObject rObj = new CeStoreJsonObject();
		CeStoreJsonArray iArr = new CeStoreJsonArray();
		String expText = getInterpretationConfidenceExplanation();
		CeStoreJsonArray jCons = createJsonForConcepts();
		CeStoreJsonArray jProps = createJsonForProperties();
		CeStoreJsonArray jInsts = createJsonForInstances();
		CeStoreJsonArray jSpecs = createJsonForSpecials();

		//question
		qObj.put(JSON_TEXT, getQuestionText());
		qObj.put(JSON_WORDS, createJsonForWords());

		//interpretation
		iObj.put(JSON_CONF, getInterpretationConfidence());

		if ((expText != null) && (!expText.isEmpty())) {
			iObj.put(JSON_EXP, expText);
		}
		
		//Note - there is only ever a single interpretation returned at the moment
		iArr.add(iObj);

		//interpretation - result
		if (!jCons.isEmpty()) {
			rObj.put(JSON_CONS, jCons);
		}

		if (!jProps.isEmpty()) {
			rObj.put(JSON_PROPS, jProps);
		}

		if (!jInsts.isEmpty()) {
			rObj.put(JSON_INSTS, jInsts);
		}
		
		if (!jSpecs.isEmpty()) {
			rObj.put(JSON_SPECS, jSpecs);
		}

		iObj.put(JSON_RES, rObj);

		result.put(JSON_QUES, qObj);
		result.put(JSON_INTS, iArr);

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

//	private CeStoreJsonArray createJsonForConcepts() {
//		CeStoreJsonArray result = new CeStoreJsonArray();
//		int ctr = 0;
//
//		for (ProcessedWord thisWord : this.allWords) {
//			if (thisWord.isGroundedOnConcept()) {
//				TreeMap<String, ArrayList<CeConcept>> conMap = thisWord.listGroundedConceptsAndKeys();
//
//				for (String thisKey : conMap.keySet()) {
//					ArrayList<CeConcept> conList = conMap.get(thisKey);
//					CeStoreJsonObject jCon = new CeStoreJsonObject();
//					CeStoreJsonArray eArr = new CeStoreJsonArray();
//
//					jCon.put("phrase", thisKey);
//					jCon.put("start position", ctr);
////					jCon.put("end position", ctr);
//					jCon.put("entities", eArr);
//
//					for (CeConcept thisCon : conList) {
//						eArr.add(jsonFor(this.ac, thisCon));
//					}
//
//					result.add(jCon);
//				}
//			}
//			++ctr;
//		}
//
//		return result;
//	}

	private CeStoreJsonArray createJsonForConcepts() {
		CeStoreJsonArray result = new CeStoreJsonArray();

		for (ProcessedWord thisWord : this.allWords) {
			TreeMap<String, ArrayList<MatchedItem>> conMap = thisWord.getMatchedItemConceptMap();

			for (String thisKey : conMap.keySet()) {
				ArrayList<MatchedItem> conList = conMap.get(thisKey);
				CeStoreJsonObject jCon = new CeStoreJsonObject();
				CeStoreJsonArray eArr = new CeStoreJsonArray();
				MatchedItem firstItem = conList.get(0);

				for (MatchedItem mi : conList) {
					eArr.add(jsonFor(this.ac, mi.getConcept()));
				}

				jCon = jsonForMatchedItem(firstItem, eArr);

				result.add(jCon);
			}
		}

		return result;
	}

	private CeStoreJsonArray createJsonForProperties() {
		CeStoreJsonArray result = new CeStoreJsonArray();

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isGroundedOnProperty()) {
				TreeMap<String, ArrayList<MatchedItem>> propMap = thisWord.getMatchedItemPropertyMap();

				for (String thisKey : propMap.keySet()) {
					ArrayList<MatchedItem> propList = propMap.get(thisKey);
					CeStoreJsonObject jProp = new CeStoreJsonObject();
					CeStoreJsonArray eArr = new CeStoreJsonArray();
					MatchedItem firstItem = propList.get(0);

					jProp.put("phrase", firstItem.getPhraseText());
					jProp.put("start position", firstItem.getStartPos());
					jProp.put("end position", firstItem.getEndPos());
					jProp.put("entities", eArr);

					for (MatchedItem thisMi : propList) {
						eArr.add(jsonFor(this.ac, thisMi.getProperty()));
					}

					result.add(jProp);
				}
			}
		}

		return result;
	}

	private CeStoreJsonArray createJsonForInstances() {
		CeStoreJsonArray result = new CeStoreJsonArray();

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isGroundedOnInstance()) {
				TreeMap<String, ArrayList<MatchedItem>> instMap = thisWord.getMatchedItemInstanceMap();

				for (String thisKey : instMap.keySet()) {
					ArrayList<MatchedItem> instList = instMap.get(thisKey);

					CeStoreJsonObject jInst = new CeStoreJsonObject();
					CeStoreJsonArray eArr = new CeStoreJsonArray();
					MatchedItem firstItem = instList.get(0);

					jInst.put("phrase", firstItem.getPhraseText());
					jInst.put("start position", firstItem.getStartPos());
					jInst.put("end position", firstItem.getEndPos());
					jInst.put("entities", eArr);

					for (MatchedItem thisMi : instList) {
						if (!isAlreadyMatchedToConceptOrProperty(thisWord, thisMi)) {
							eArr.add(jsonFor(this.ac, thisMi.getInstance()));
						}
					}

					if (!eArr.isEmpty()) {
						result.add(jInst);
					}
				}
			}
		}

		return result;
	}

	private boolean isAlreadyMatchedToConceptOrProperty(ProcessedWord pWord, MatchedItem pMi) {
		boolean result = false;
		
		if (pMi.hasInstance()) {
			String instName = pMi.getInstance().getInstanceName();

			for (MatchedItem mi : pWord.getMatchedItems()) {
				if (!mi.equals(pMi)) {
					if (mi.hasConcept()) {
						if (mi.getConcept().getConceptName().equals(instName)) {
							result = true;
							break;
						}
					} else if (mi.hasProperty()) {
						if (mi.getProperty().getPropertyName().equals(instName)) {
							result = true;
							break;
						}
					}
				}
			}
		}

		return result;
	}

	private CeStoreJsonArray createJsonForSpecials() {
		CeStoreJsonArray result = new CeStoreJsonArray();

		createJsonForNumberSpecials(result);
		createJsonForCollectionSpecials(result);
		createJsonForEnumeratedConceptSpecials(result);
		createJsonForLinkedInstanceSpecials(result);
		createJsonForMatchedTripleSpecials(result);
		createJsonForMultiMatchSpecials(result);

		return result;
	}

	private void createJsonForNumberSpecials(CeStoreJsonArray pResult) {
		for (ProcessedWord thisWord : this.allWords) {
			for (SpNumber sn : this.numbers) {
				if (thisWord.getWordPos() == sn.getStartPos()) {
					pResult.add(sn.toJson(this.ac));
				}
			}
		}
	}

	private void createJsonForCollectionSpecials(CeStoreJsonArray pResult) {
		for (ProcessedWord thisWord : this.allWords) {
			for (SpCollection sc : this.collections) {
				if (thisWord.getWordPos() == sc.getStartPos()) {
					pResult.add(sc.toJson(this.ac));
				}
			}
		}
	}

	private void createJsonForEnumeratedConceptSpecials(CeStoreJsonArray pResult) {
		for (ProcessedWord thisWord : this.allWords) {
			for (SpEnumeratedConcept sec : this.enumeratedConcepts) {
				if (thisWord.getWordPos() == sec.getStartPos()) {
					pResult.add(sec.toJson(this.ac));
				}
			}
		}
	}

	private void createJsonForLinkedInstanceSpecials(CeStoreJsonArray pResult) {
		for (ProcessedWord thisWord : this.allWords) {
			for (SpLinkedInstance sli : this.linkedInstances) {
				if (thisWord.getWordPos() == sli.getStartPos()) {
					pResult.add(sli.toJson(this.ac));
				}
			}
		}
	}

	private void createJsonForMatchedTripleSpecials(CeStoreJsonArray pResult) {
		for (ProcessedWord thisWord : this.allWords) {
			for (SpMatchedTriple smt : this.matchedTriples) {
				if (thisWord.getWordPos() == smt.getStartPos()) {
					pResult.add(smt.toJson(this.ac));
				}
			}
		}
	}

	private void createJsonForMultiMatchSpecials(CeStoreJsonArray pResult) {
		for (ProcessedWord thisWord : this.allWords) {
			for (SpMultiMatch smm : this.multiMatches) {
				if (thisWord.getWordPos() == smm.getStartPos()) {
					pResult.add(smm.toJson(this.ac));
				}
			}
		}
	}

}
