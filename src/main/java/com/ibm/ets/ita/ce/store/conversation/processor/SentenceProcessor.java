package com.ibm.ets.ita.ce.store.conversation.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class SentenceProcessor {
//	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";
//
////	private static final String CON_CWL = "common word list";
////	private static final String CON_NWL = "negation word list";
//	private static final String CON_ATTTHING = "attributional thing";
//	private static final String PROP_LOC = "location";
//	private static final String PROP_NEWINSTSCORE = "new instance score";
//	private static final String PROP_EXINSTSCORE = "existing instance score";
//	private static final String PROP_NEWRELSCORE = "new relationship between existing things score";
//
//	private ActionContext ac = null;
//	private CeInstance cardInstance = null;
//	private ConvSentence convSentence = null;
//
//	private ArrayList<ProcessedWord> allProcessedWords = null;
//	private ProcessedWord subjectWord = null;
//	private CeConcept subjectConcept = null;
//	private CeProperty subjectProperty = null;
//	private CeInstance subjectInstance = null;
//
//	private HashSet<ExtractedItem> otherConcepts = new HashSet<ExtractedItem>();
//	private HashSet<ExtractedItem> otherProperties = new HashSet<ExtractedItem>();
//	private HashSet<ExtractedItem> otherInstances = new HashSet<ExtractedItem>();
//	private ArrayList<ProcessedWord> ungroundedWords = new ArrayList<ProcessedWord>();
//
//	private ArrayList<MatchedTriple> matchedTriples = new ArrayList<MatchedTriple>();
//	
//	private ExtractedItem lastExtItem = null;
//	private boolean useDefaultScoring = false;
//	
//	public ProcessedWord getSubjectWord() {
//		return this.subjectWord;
//	}
//
//	public CeConcept getSubjectConcept() {
//		return this.subjectConcept;
//	}
//
//	public CeInstance getSubjectInstance() {
//		return this.subjectInstance;
//	}
//
//	public HashSet<ExtractedItem> getOtherConcepts() {
//		return this.otherConcepts;
//	}
//
//	public ArrayList<MatchedTriple> getMatchedTriples() {
//		return this.matchedTriples;
//	}
//	
//	public MatchedTriple getFirstMatchedTriple() {
//		MatchedTriple result = null;
//
//		if (this.matchedTriples != null) {
//			if (!this.matchedTriples.isEmpty()) {
//				result = this.matchedTriples.get(0);
//			}
//		}
//
//		return result;
//	}
//
//	public ResultOfAnalysis processSentence() {
//		initialise();
//		prepareAndClassifyWords();
//
//		return doPostProcessing();
//	}
//
//	public SentenceProcessor(ActionContext pAc, CeInstance pCardInst, ConvSentence pConvSen, boolean pUseDefaultScoring) {
//		this.ac = pAc;
//		this.cardInstance = pCardInst;
//		this.convSentence = pConvSen;
//		this.useDefaultScoring = pUseDefaultScoring;
//	}
//
//	protected void initialise() {
//		this.allProcessedWords = new ArrayList<ProcessedWord>();
//	}
//
//	public ConvSentence getConvSentence() {
//		return this.convSentence;
//	}
//
//	public ArrayList<ProcessedWord> getAllProcessedWords() {
//		return this.allProcessedWords;
//	}
//
//	public void addProcessedWord(ProcessedWord pWord) {
//		this.allProcessedWords.add(pWord);
//	}
//
//	private void addMatchedTriple(MatchedTriple pMatchedTriple) {
//		this.matchedTriples.add(pMatchedTriple);
//	}
//
//	public String getSentenceText() {
//		return this.convSentence.getSentenceText();
//	}
//
//	public ResultOfAnalysis doPostProcessing() {
//		ResultOfAnalysis result = null;
//
//		if (this.convSentence.getParentPhrase().isQuestion()) {
//			doBasicWordMatching();
//
//			QuestionProcessor qp = new QuestionProcessor(this.ac, this.allProcessedWords, this.convSentence, this.cardInstance);
//			result = qp.processQuestion();
//		} else {
//			if (!containsNegations()) {
//				if (!hasBeenSaidBefore()) {
//					result = doAssertionPostProcessing();
//
//					if (result != null) {
//						augmentWithMatchedInstances(result);
//					}
//				} else {
//					result = ResultOfAnalysis.msgBeenSaidAlready();
//				}
//			} else {
//				result = ResultOfAnalysis.msgCantHandleNegations();
//			}
//		}
//
//		return result;
//	}
//
//	private boolean hasBeenSaidBefore() {
//		boolean result = false;
//		String lcText = this.cardInstance.getSingleValueFromPropertyNamed("content").toLowerCase().trim();
//		CeInstance currFrom = this.cardInstance.getSingleInstanceFromPropertyNamed(this.ac, "is from");
//
//		//TODO: Need to handle anaphors like "here" and "I" too
//		//TODO: Abstract these values
//		for (CeInstance prevInst : this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, "NL card")) {
//			CeInstance prevFrom = prevInst.getSingleInstanceFromPropertyNamed(this.ac, "is from");
//			
//			if (prevFrom != null) {
//				if (prevFrom.equals(currFrom)) {
//					String lcContent = prevInst.getSingleValueFromPropertyNamed("content").toLowerCase().trim();
//					if (lcContent.equals(lcText)) {
//						if (locationsMatch(this.cardInstance, prevInst)) {
//							if (hasBeenConfirmed(prevInst)) {
//								result = true;
//								break;
//							}
//						}
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
//	private boolean containsNegations() {
//		boolean result = false;
//
//		for (ProcessedWord thisPw : this.allProcessedWords) {
//			if (thisPw.isPureNegation()) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	private boolean locationsMatch(CeInstance pFirstCard, CeInstance pSecondCard) {
//		//TODO: Abstract these values
//		//TODO: Should we restrict this to only cases where the location was used?
//		boolean result = false;
//		CeInstance firstLoc = pFirstCard.getSingleInstanceFromPropertyNamed(this.ac, "location");
//		CeInstance secondLoc = pSecondCard.getSingleInstanceFromPropertyNamed(this.ac, "location");
//
//		if (firstLoc == null) {
//			result = (secondLoc == null);
//		} else {
//			if (secondLoc == null) {
//				result = false;
//			} else {
//				result = firstLoc.equals(secondLoc);
//			}
//		}
//
//		return result;
//	}
//
//	private boolean hasBeenConfirmed(CeInstance pCardInst) {
//		boolean result = false;
//		
//		//TODO: Abstract these values
//		
//		//There should be a confirm card is reply to the matched card (sent by moira)
//		CePropertyInstance pi = pCardInst.getReferringPropertyInstanceNamed("is in reply to");
//		
//		if (pi != null) {
//			CeInstance confCard = pi.getRelatedInstance();
//			
//			if (confCard.isConceptNamed(this.ac, "confirm card")) {
//				//It is a confirm card - if this has a tell card in response then this passed card has been confirmed
//				CePropertyInstance npi = confCard.getReferringPropertyInstanceNamed("is in reply to");
//
//				if (npi != null) {
//					CeInstance nextCard = npi.getRelatedInstance();
//
//					if (isConfirmationCard(nextCard)) {
//						result = true;
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
//	private boolean isConfirmationCard(CeInstance pCard) {
//		boolean result = false;
//
//		if (pCard.isConceptNamed(this.ac, "tell card")) {
//			result = true;
//		} else {
//			String lcCon = pCard.getSingleValueFromPropertyNamed("content").toLowerCase().trim();
//
//			//TODO: Need to check all the other words too
//			if (lcCon.equals("confirm")) {
//				result = true;
//			}
//		}
//
//		return result;
//	}
//
//	private void augmentWithMatchedInstances(ResultOfAnalysis pResult) {
//		for (ProcessedWord thisPw : this.allProcessedWords) {
//			for (CeInstance thisInst : thisPw.listGroundedInstances()) {
//				pResult.addMatchedInstance(thisInst);
//			}
//		}
//	}
//
//	private void doBasicWordMatching() {
//		//Seek subject
//		seekSubject();
//
//		//Seek others
//		seekOthers();
//	}
//	
//	private ResultOfAnalysis doAssertionPostProcessing() {
//		ResultOfAnalysis result = null;
//
//		doBasicWordMatching();
//
//		boolean madeSense = tryToMakeSenseOfSentence();
//
//		if (madeSense) {
//			result = generateCeForMatchedTriples();
//		}
//
//		return result;
//	}
//
//
//	private void seekSubject() {
//		this.subjectWord = seekSubjectWord();
//
//		if (this.subjectWord != null) {
//			seekSubjectEntity();
//		} else {
//			reportDebug("No subject word found for sentence: " + getSentenceText(), this.ac);
//		}
//	}
//	
//	private ProcessedWord seekSubjectWord() {
//		ProcessedWord foundWord = null;
//
//		if (foundWord == null) {
//			//Now try everything
//			for (ProcessedWord thisWord : this.allProcessedWords) {
//				if (foundWord == null) {
//					if (thisWord.isGrounded()) {
//						if (thisWord.isValidSubjectWord(this.ac)) {
//							foundWord = thisWord;
//						}
//					}
//				}
//			}
//		}
//
//		return foundWord;
//	}
//
//	private void seekSubjectEntity() {
//		if (this.subjectWord.isGroundedOnConcept()) {
//			//Concept
//			ArrayList<CeConcept> subCons = this.subjectWord.listGroundedConcepts();
//			this.subjectConcept = retrieveSingleConceptFrom(subCons, this.subjectWord, "subject");
//			this.lastExtItem = new ExtractedItem(this.subjectWord, this.subjectConcept);
//		} else if (this.subjectWord.isGroundedOnProperty()) {
//			//Property
//			ArrayList<CeProperty> subProps = this.subjectWord.listGroundedProperties();
////			this.subjectProperty = retrieveSinglePropertyFrom(subProps, this.subjectWord, "subject");
//			this.lastExtItem = new ExtractedItem(this.subjectWord, subProps);
//		} else if (this.subjectWord.isGroundedOnInstance()) {
//			//Instance
//			ArrayList<CeInstance> subInsts = this.subjectWord.listGroundedInstances();
//			this.subjectInstance = retrieveSingleInstanceFrom(subInsts, this.subjectWord, "subject");
//			if (this.subjectInstance != null) {
//				this.subjectWord.setChosenInstance(this.subjectInstance);
//			}
//			this.lastExtItem = new ExtractedItem(this.subjectWord, this.subjectInstance);
//		} else {
//			//Unknown
//			reportWarning("Unable to detect grounding type for subject word: " + this.subjectWord.toString(), this.ac);
//		}
//
//		if (this.lastExtItem != null) {
//			this.subjectWord.addConnectedWordsTo(this.lastExtItem);
//		}
//	}
//	
//	private CeConcept retrieveSingleConceptFrom(ArrayList<CeConcept> pCons, ProcessedWord pWord, String pContext) {
//		CeConcept result = null;
//		
//		if (pCons.isEmpty()) {
//			reportError("No grounded concept found for " + pContext + " word: " + pWord.toString(), this.ac);
//		} else if (pCons.size() > 1) {
//			reportError("Too many (" + pCons.size() + ") grounded concepts found for " + pContext + " word: "  + pWord.toString(), this.ac);
//		} else {
//			result = pCons.get(0);
//		}
//		
//		return result;
//	}
//
////	private CeProperty retrieveSinglePropertyFrom(ArrayList<CeProperty> pProps, ProcessedWord pWord, String pContext) {
////		if (pProps.isEmpty()) {
////			reportError("No grounded property found for " + pContext + " word: " + pWord.toString());
////		} else if (pProps.size() > 1) {
////			reportError("Too many (" + pProps.size() + ") grounded properties found for " + pContext + " word: "  + pWord.toString());
////
////			ProcessedWord prevWord = pWord.getPreviousProcessedWord();
////			if (prevWord != null) {
////				ArrayList<CeConcept> conList = prevWord.listGroundedConceptsNoPlural();
////
////				for (CeConcept thisCon : conList) {
////					for (CeProperty thisProp : pProps) {
////						CeConcept domCon = thisProp.getDomainConcept();
////						
////						if (domCon.equalsOrHasParent(thisCon)) {
////							return thisProp;
////						}
////					}
////				}
////			}
////		}
////
////		return pProps.get(0);
////	}
//
//	private CeInstance retrieveSingleInstanceFrom(ArrayList<CeInstance> pInsts, ProcessedWord pWord, String pContext) {
//		CeInstance result = null;
//		
//		if (pInsts.isEmpty()) {
//			reportError("No grounded instance found for " + pContext + " word: " + pWord.toString(), this.ac);
//		} else if (pInsts.size() > 1) {
//			reportDebug("Too many (" + pInsts.size() + ") grounded instances found for " + pContext + " word: "  + pWord.toString(), this.ac);
//			result = chooseLongestNameFrom(pInsts);
//		} else {
//			result = pInsts.get(0);
//		}
//		
//		return result;
//	}
//
//	private static CeInstance chooseLongestNameFrom(ArrayList<CeInstance> pInsts) {
//		CeInstance result = null;
//
//		for (CeInstance thisInst : pInsts) {
//			int currLen = -1;
//
//			if (result != null) {
//				currLen = result.getInstanceName().length();
//			}
//
//			if (thisInst.getInstanceName().length() > currLen) {
//				result = thisInst;
//			}
//		}
//
//		return result;
//	}
//
//	private void seekOthers() {
//		for (ProcessedWord thisWord : this.allProcessedWords) {
//			if (thisWord != this.subjectWord) {
//				if (thisWord.isGroundedOnConcept()) {
//					if (!thisWord.isLaterPartOfPartial()) {
//						CeConcept tgtCon = retrieveSingleConceptFrom(thisWord.listGroundedConcepts(), thisWord, "object");
//
//						if (tgtCon != null) {
//							reportDebug("I got an object concept (" + tgtCon.getConceptName()  + "), from word: " + thisWord.toString(), this.ac);
//							ExtractedItem extCon = new ExtractedItem(thisWord, tgtCon);
//							if (this.lastExtItem != null) {
//								extCon.setPreviousItem(this.lastExtItem);
//							}
//							thisWord.addConnectedWordsTo(extCon);
//							this.otherConcepts.add(extCon);
//
//							this.lastExtItem = extCon;
//						} else {
//							reportDebug("No object concept found for word: " + thisWord.toString(), this.ac);
//						}
//					} else {
//						reportDebug("Ignoring later partial for concept: " + thisWord.getWordText(), this.ac);
//					}
//				} else if (thisWord.isGroundedOnProperty()) {
//					if (!thisWord.isLaterPartOfPartial()) {
////						CeProperty tgtProp = retrieveSinglePropertyFrom(thisWord.listGroundedProperties(), thisWord, "object");
//	
//						ArrayList<CeProperty> propList = thisWord.listGroundedProperties();
//						if (!propList.isEmpty()) {
////							reportDebug("I got an object property (" + tgtProp.formattedFullPropertyName()  + "), from word: " + thisWord.toString());
//							ExtractedItem extProp = new ExtractedItem(thisWord, propList);
//							if (this.lastExtItem != null) {
//								extProp.setPreviousItem(this.lastExtItem);
//							}
//							thisWord.addConnectedWordsTo(extProp);
//							this.otherProperties.add(extProp);
//	
//							this.lastExtItem = extProp;
//						} else {
//							reportWarning("No object property found for word: " + thisWord.toString(), this.ac);
//						}
//					} else {
//						reportDebug("Ignoring later partial for property: " + thisWord.getWordText(), this.ac);
//					}
//				} else if (thisWord.isGroundedOnInstance()) {
//					CeInstance tgtInst = retrieveSingleInstanceFrom(thisWord.listGroundedInstances(), thisWord, "object");
//
//					if (tgtInst != null) {
//						thisWord.setChosenInstance(tgtInst);
//						reportDebug("I got an object instance (" + tgtInst.getInstanceName()  + "), from word: " + thisWord.toString(), this.ac);
//						ExtractedItem extInst = new ExtractedItem(thisWord, tgtInst);
//						if (this.lastExtItem != null) {
//							extInst.setPreviousItem(this.lastExtItem);
//						}
//						thisWord.addConnectedWordsTo(extInst);
//						this.otherInstances.add(extInst);
//
//						this.lastExtItem = extInst;
//					} else {
//						reportWarning("No object instance found for word: " + thisWord.toString(), this.ac);
//					}
//				} else {
//					this.ungroundedWords.add(thisWord);
//				}
//			}
//		}
//	}
//
//	private void prepareAndClassifyWords() {
//		int wordPos = 0;
//		ConvConfig cc = ServletStateManager.getHudsonManager(this.ac).getConvConfig(this.ac);
//
//		//First create all the processed word instances
//		for (ConvWord thisWord : this.convSentence.getAllWords()) {
//			ProcessedWord newPw = ProcessedWord.createFrom(thisWord, wordPos++);
//			this.allProcessedWords.add(newPw);
//		}
//
//		markQuestionWords(this.allProcessedWords);
//
////		ArrayList<CeInstance> cwls = this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, CON_CWL);
////		ArrayList<CeInstance> nwls = this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, CON_NWL);
//
//		//Then classify the processed words
//		for (ProcessedWord thisWord : this.allProcessedWords) {
//			thisWord.classify(this.ac, cc);
//		}
//	}
//	
//	public CeInstance getLocationInstance() {
//		CeInstance locInst = null;
//
//		if (this.cardInstance != null) {
//			locInst = this.cardInstance.getSingleInstanceFromPropertyNamed(this.ac, PROP_LOC);
//		}
//
//		return locInst;
//	}
//
//	private static void markQuestionWords(ArrayList<ProcessedWord> pWords) {
//		//TODO: This should not be hardcoded
//		for (ProcessedWord thisPw : pWords) {
//			if ((thisPw.getLcWordText().equals("who")) ||
//				(thisPw.getLcWordText().equals("where")) ||
//				(thisPw.getLcWordText().equals("when")) ||
//				(thisPw.getLcWordText().equals("what")) ||
//				(thisPw.getLcWordText().equals("which")) ||
//				(thisPw.getLcWordText().equals("why")) ||
//				(thisPw.getLcWordText().equals("list")) ||
//				(thisPw.getLcWordText().equals("summarise")) ||
//				(thisPw.getLcWordText().equals("summarize")) ||
//				(thisPw.getLcWordText().equals("count"))) {
//				thisPw.markAsQuestionWord();
//			}
//			}
//	}
//
//	private ResultOfAnalysis generateCeForMatchedTriples() {
//		String gistText = GistGenerator.generateGistFor(this.ac, this);
//		String ceText = DomainCeGenerator.generateCeFor(this.ac, this);
//
//		ResultOfAnalysis result = ResultOfAnalysis.createWithGistAndCe(gistText, ceText);
//
//		calculateScoreInformation(result);
//
//		return result;
//	}
//
//	private void calculateScoreInformation(ResultOfAnalysis pResult) {
//		if (this.useDefaultScoring) {
//			calculateDefaultScore(pResult);
//		} else {
//			calculateSpecialScore(pResult);
//		}
//	}
//	
//	private void calculateDefaultScore(ResultOfAnalysis pResult) {
//		long score = 0;
//		String scoreExplanation = "";
//		String scoreConnector = "";
//
//		if ((this.subjectConcept != null) && (this.subjectInstance == null)) {
//			++score;
//			scoreExplanation += scoreConnector + "matched concept '" + this.subjectConcept.getConceptName() + "'";
//			scoreConnector = ", ";
//		}
//
//		if (this.subjectInstance != null) {
//			++score;
//			scoreExplanation += scoreConnector + "matched instance '" + this.subjectInstance.getInstanceName() + "'";
//			scoreConnector = ", ";
//		}
//
//		if (!this.matchedTriples.isEmpty()) {
//			long numProps = this.matchedTriples.size();
//			score += numProps;
//			
//			String propWord = null;
//			if (numProps == 1) {
//				propWord = "property";
//			} else {
//				propWord = "properties";
//			}
//			
//			String propList = " (";
//			String sepText = "";
//			for (MatchedTriple thisMt : this.matchedTriples) {
//				propList += sepText + thisMt.getPredicateProperty().getPropertyName();
//				sepText = ", ";
//				
//				String objId = thisMt.getObjectInstanceId();
//				if ((objId != null) && (!objId.isEmpty())) {
//					if (this.ac.getModelBuilder().getInstanceNamed(this.ac, objId) != null) {
//						++score;
//						scoreExplanation += scoreConnector + "matched instance '" + objId + "'";
//						scoreConnector = ", ";
//					}
//				}
//			}
//			propList += ")";
//
//			scoreExplanation += scoreConnector + " identified " + new Long(numProps).toString() + " " + propWord + propList;
//			scoreConnector = ", ";
//		}
//
//		pResult.addScoreDetails(score, scoreExplanation, "potential");
//	}
//
//	private void calculateSpecialScore(ResultOfAnalysis pResult) {
//		long score = 0;
//		String scoreExplanation = "";
//		String scoreConnector = "";
//
//		if ((this.subjectConcept != null) && (this.subjectInstance == null)) {
//			//A new instance
//			int scoreVal = extractScoreValueFor(this.subjectConcept, PROP_NEWINSTSCORE);
//			score += scoreVal;
//			
//			for (ExtractedItem otherCon : this.otherConcepts) {
//				scoreVal = extractScoreValueFor(otherCon.getConcept(), PROP_NEWINSTSCORE);
//				score += scoreVal;
//			}
//		}
//
//		if (this.subjectInstance != null) {
//			//An existing instance
//			int scoreVal = extractScoreValueFor(this.subjectConcept, PROP_EXINSTSCORE);
//			score += scoreVal;
//		}
//
//		if (!this.matchedTriples.isEmpty()) {
//			int scoreVal = extractScoreValueFor(this.subjectConcept, PROP_NEWRELSCORE);
//			ArrayList<MatchedTriple> filteredMts = new ArrayList<MatchedTriple>();
//
//			for (MatchedTriple thisMt : this.matchedTriples) {
//				if (!thisMt.isNewInstance()) {
//					String objId = thisMt.getObjectInstanceId();
//					if ((objId != null) && (!objId.isEmpty())) {
//						if (this.ac.getModelBuilder().getInstanceNamed(this.ac, objId) != null) {
//							filteredMts.add(thisMt);
//						}
//					}
//				}
//			}
//
//			long numProps = filteredMts.size();
//
//			String propWord = null;
//			if (numProps == 1) {
//				propWord = "property";
//			} else {
//				propWord = "properties";
//			}
//			
//			String propList = " (";
//			String sepText = "";
//			for (MatchedTriple thisMt : filteredMts) {
//				propList += sepText + thisMt.getPredicateProperty().getPropertyName();
//				sepText = ", ";
//				score += scoreVal;
//			}
//			propList += ")";
//
//			scoreExplanation += scoreConnector + " identified " + new Long(numProps).toString() + " " + propWord + propList;
//			scoreConnector = ", ";
//		}
//
//		pResult.addScoreDetails(score, scoreExplanation, "potential");
//	}
//
//	private int extractScoreValueFor(CeConcept pCon, String pPropName) {
//		int score = 0;
//		CeInstance mm = pCon.retrieveMetadataInstance(this.ac);
//		
//		if (mm != null) {
//			if (mm.hasPropertyInstanceForPropertyNamed(pPropName)) {
//				int scoreVal = new Integer(mm.getSingleValueFromPropertyNamed(pPropName)).intValue();
//				score = score + scoreVal;
//			} else {
//				//Check parents
//				for (CeConcept parCon : pCon.getDirectParents()) {
//					score = extractScoreValueFor(parCon, pPropName);
//					
//					if (score > 0) {
//						break;
//					}
//				}
//			}
//		}
//
//		return score;
//	}
//
//	private boolean tryToMakeSenseOfSentence() {
//		boolean result = false;
//
//		if (this.subjectConcept != null) {
//			result = makeSenseOfConceptLedSentence();
//		} else if (this.subjectProperty != null) {
//			result = makeSenseOfPropertyLedSentence();
//		} else if (this.subjectInstance != null) {
//			result = makeSenseOfInstanceLedSentence();
//		} else {
//			reportDebug("Cannot make sense as sentence has no subject: " + getSentenceText(), this.ac);
//		}
//
//		return result;
//	}
//
//	private boolean makeSenseOfConceptLedSentence() {
//		//First process the properties
//		HashSet<String> processedInstIds = processProperties();
//
//		//Now process the instances that were not matched in the property processing
//		HashSet<ExtractedItem> unprocessedInsts = new HashSet<ExtractedItem>();
//		for (ExtractedItem extInst : this.otherInstances) {
//			CeInstance thisInst = extInst.getInstance();
//
//			if (thisInst != null) {
//				if (!processedInstIds.contains(thisInst.getInstanceName())) {
//					unprocessedInsts.add(extInst);
//				}
//			} 
//		}
//
//		processRemainingInstances(unprocessedInsts);
//		
//		return true;
//	}
//
//	private HashSet<String> processProperties() {
//		HashSet<String> result = new HashSet<String>();
//		
//		HashSet<ExtractedItem> matchSet = new HashSet<ExtractedItem>();
//		HashSet<ExtractedItem> unmatchSet = null;
//
//		seekPropertyMatchesFor(this.subjectConcept, "direct", matchSet);
//
//		for (CeConcept parentCon : this.subjectConcept.retrieveAllParents(false)) {
//			seekPropertyMatchesFor(parentCon, "parent", matchSet);
//		}
//
//		unmatchSet = new HashSet<ExtractedItem>();
//		for (ExtractedItem extProp : this.otherProperties) {
//			if (extProp.isPropertyItem()) {
//				unmatchSet.add(extProp);
//			} else {
//				reportWarning("Unexpected extracted item type when creating unmatched list: " + extProp.toString(), this.ac);
//			}
//		}
//		
//		unmatchSet.removeAll(matchSet);
//		
//		for (ExtractedItem matchedProp : matchSet) {
//			result.addAll(createMatchedTripleFor(this.subjectConcept, matchedProp, MatchedTriple.CON_DOM_MATCHED));
//		}
//
//		for (ExtractedItem ump : unmatchSet) {
//			reportDebug("Unmatched property - using domain of property itself " + ump.getFirstProperty().formattedFullPropertyName(), this.ac);
//			result.addAll(createMatchedTripleFor(ump.getFirstProperty().getDomainConcept(), ump, MatchedTriple.CON_DOM_UNMATCHED));
//		}
//		
//		return result;
//	}
//	
//	private void processRemainingInstances(HashSet<ExtractedItem> pExtInsts) {
//		for (ExtractedItem extInst : pExtInsts) {
//			if (extInst.isDominantInterpretation()) {
//				CeInstance tgtInst = extInst.getInstance();
//				
//				for (CeConcept dirCon : tgtInst.getDirectConcepts()) {
//					for (CeProperty possProp : this.subjectConcept.calculateAllProperties().values()) {
//						if (possProp.isObjectProperty()) {
//							reportDebug("Checking '" + tgtInst.getInstanceName() + "' for possible match (range=" + dirCon.getConceptName() + " vs " + possProp.getRangeConcept().getConceptName() + ") for property: " + possProp.formattedFullPropertyName(), this.ac);
//							if (possProp.getRangeConcept().equals(dirCon)) {
//								if (!dirCon.isThing()) {
//									String objConName = conceptNameMatching(tgtInst, dirCon);
//
//									if (objConName != null) {
//										reportDebug("Instance '" + tgtInst.getInstanceName() + "' has possible match (range=" + dirCon.getConceptName() + ") for property: " + possProp.formattedFullPropertyName(), this.ac);
//										MatchedTriple mt = MatchedTriple.createOpTriple(this.ac, extInst, this.subjectConcept, possProp, tgtInst.getInstanceName(), objConName, extInst.getOriginalDescription(), MatchedTriple.CON_DIRECT, false, this.subjectInstance);
//										addMatchedTriple(mt);
//									}
//
////									extInst.markPreceedingWordAsLinker();
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//
//	private static String conceptNameMatching(CeInstance pInst, CeConcept pTgtCon) {
//		String result = null;
//		
//		for (CeConcept possCon : pInst.getAllLeafConcepts()) {
//			if (possCon.equalsOrHasParent(pTgtCon)) {
//				result = possCon.getConceptName();
//				break;
//			}
//		}
//		
//		return result;
//	}
//
//	private HashSet<String> createMatchedTripleFor(CeConcept pDomCon, ExtractedItem pEiProp, String pContext) {
//		HashSet<String> result = new HashSet<String>();
//		CeProperty tgtProp = pEiProp.getPropertyWithDomain(this.ac, pDomCon);
//		CeConcept rangeCon = tgtProp.getRangeConcept();
//		String seekText = "";
//
//		if (rangeCon == null) {
//			seekText = "Seeking value";
//			//TODO: It is not always the next word that will contain the value
//			ProcessedWord lastWord = pEiProp.getLastWord();
//
//			if (lastWord != null) {
//				ArrayList<ProcessedWord> valWords = lastWord.getFollowingUnprocessedWords();
//				String valText = "";
//
//				for (ProcessedWord valWord : valWords) {
//					if (!valText.isEmpty()) {
//						valText += " ";
//					}
//					valText += valWord.getWordText();
//					valWord.markWordAsValue();
//				}
//
//				if (!valText.isEmpty()) {
//					reportDebug("Found value '" + valText + "' for property: " + tgtProp.getPropertyName(), this.ac);
//					MatchedTriple mt = MatchedTriple.createDpTriple(this.ac, pEiProp, pDomCon, pEiProp.getFirstProperty(), valText, pContext);
//					addMatchedTriple(mt);
//				}
//
//			} else {
//				reportWarning("No last word for: " + pEiProp.toString(), this.ac);
//			}
//		} else {
//			seekText = "Seeking instance of " + rangeCon.getConceptName();
//			ArrayList<ExtractedItem> matchedInsts = filterOtherInstancesByRange(rangeCon);
//
//			if (matchedInsts.isEmpty()) {
//				reportDebug("No matching instance with range '" + rangeCon.getConceptName() + "' was detected", this.ac);
//				String newInstId = CeGenerator.getNewUidFor(this.ac, rangeCon);
//				pEiProp.setNewInstanceId(newInstId);
//
//				//Create matched triple with a new instance
//				MatchedTriple mt = MatchedTriple.createOpTriple(this.ac, pEiProp, tgtProp.getDomainConcept(), tgtProp, newInstId, pEiProp.getFirstProperty().getRangeConceptName(), pEiProp.getOriginalDescription(), pContext, true, this.subjectInstance);
//				addMatchedTriple(mt);
//				result.add(newInstId);
//			} else {
//				for (ExtractedItem instEi : matchedInsts) {
//					if (instEi.isDominantInterpretation()) {
//						String objConName = null;
//						CeInstance matchedInst = instEi.getInstance();
//						objConName = conceptNameMatching(matchedInst, rangeCon);					
//						if (objConName != null) {
//							reportDebug("Instance '" + matchedInst.getInstanceName() + "' matches range '" + rangeCon.getConceptName() + "'", this.ac);
//							MatchedTriple mt = MatchedTriple.createOpTriple(this.ac, pEiProp, tgtProp.getDomainConcept(), tgtProp, matchedInst.getInstanceName(), objConName, pEiProp.getOriginalDescriptionForNextItem(), pContext, false, this.subjectInstance);
//							addMatchedTriple(mt);
//							result.add(matchedInst.getInstanceName());
//						}
//					}
//				}
//			}
//		}
//
//		reportDebug("Matched property with this text: " + tgtProp.formattedFullPropertyName() + " (" + seekText + ")", this.ac);
//
//		return result;
//	}
//
//	private ArrayList<ExtractedItem> filterOtherInstancesByRange(CeConcept pRangeCon) {
//		ArrayList<ExtractedItem> result = new ArrayList<ExtractedItem>();
//
//		for (ExtractedItem extInst : this.otherInstances) {
//			if (extInst.isInstanceItem()) {
//				CeInstance thisInst = extInst.getInstance();
//				if (thisInst.isConcept(pRangeCon)) {
//					result.add(extInst);
//				}
//			} else {
//				reportWarning("Unexpected extracted item type filtering instances: " + extInst.toString(), this.ac);
//			}
//		}
//
//		return result;
//	}
//
//	private void seekPropertyMatchesFor(CeConcept pCon, String pContext, HashSet<ExtractedItem> pMatchSet) {
//		ArrayList<CeProperty> allProps = pCon.calculateAllDirectProperties(false);
//		
//		for (CeProperty thisProp : allProps) {
//			reportDebug("Possible property (" + pContext + "): " + thisProp.formattedFullPropertyName(), this.ac);
//			
//			for (ExtractedItem extProp : this.otherProperties) {
//				for (CeProperty tgtProp : extProp.getPropertyList()) {
//					if (thisProp.equals(tgtProp)) {
//						pMatchSet.add(extProp);
//					}
//				}
//			}
//		}		
//	}
//	
//	private boolean makeSenseOfPropertyLedSentence() {
//		//TODO: Improve this.  For now simply provide a suitable subject concept and then carry out the rest of the processing
//		CeConcept tgtCon = this.ac.getModelBuilder().getConceptNamed(this.ac, this.subjectProperty.getDomainConcept().getConceptName());
//		this.subjectConcept = tgtCon;
//		
//		if ((this.allProcessedWords != null) && (!this.allProcessedWords.isEmpty())) {
//			ProcessedWord firstWord = this.allProcessedWords.get(0);
//			
//			if (firstWord.isUnmatchedWord()) {
//				CeInstance tgtInst = null;
//				ExtractedItem instEi = new ExtractedItem(firstWord, tgtInst);
//				firstWord.addExtractedItem(instEi);
//			}
//		}
//
//		return makeSenseOfConceptLedSentence();
//	}
//
//	private boolean makeSenseOfInstanceLedSentence() {
//		//TODO: Improve this.  For now simply provide a suitable subject concept and then carry out the rest of the processing
//		CeConcept conExc = this.ac.getModelBuilder().getConceptNamed(this.ac, CON_ATTTHING);
//		CeConcept tgtCon = this.subjectInstance.getFirstLeafConceptExcludingConcept(conExc);
//		this.subjectConcept = tgtCon;
//		
//		return makeSenseOfConceptLedSentence();
//	}
//
//	public void generateConvCe(StringBuilder pSb, ConvPhrase pCp) {
//		ConvCeGenerator.generateConvCeFor(this.ac, this, pCp, pSb);
//	}
//
//	public HashSet<CeConcept> computeUniqueConcepts() {
//		HashSet<CeConcept> uniqueCons = new HashSet<CeConcept>();
//
//		if (getSubjectConcept() != null) {
//			uniqueCons.add(getSubjectConcept());
//		}
//
//		for (MatchedTriple thisMt : getMatchedTriples()) {
//			if (thisMt.isSubjectConcept()) {
//				uniqueCons.add(thisMt.getSubjectConcept());
//			}
//		}
//
//		return uniqueCons;
//	}
//
}
