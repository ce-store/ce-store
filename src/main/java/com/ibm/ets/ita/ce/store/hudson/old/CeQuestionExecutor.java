package com.ibm.ets.ita.ce.store.hudson.old;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.handler.GenericHandler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class CeQuestionExecutor extends GenericHandler {
	public CeQuestionExecutor(ActionContext pAc, boolean pDebug, long pStartTime) {
		super(pAc, pDebug, pStartTime);
	}

//	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";
//
//	private static final String ANSKEY_CONS = "cons";
//	private static final String HDR_TYPE = "type";
//	private static final String HDR_COUNT = "count";
//
//	private static final int DEFAULT_COUNT_CONF = 100;
//	private static final int CONF_OFFSET_COMPUTEDDESC = -50;
//
//	private AnswerReply reply = null;
//	private ArrayList<ProcessedWord> allWords = null;
//	private ArrayList<ChosenWord> chosenWords = null;
//	private int qConf = -1;
//	private boolean isCount = false;
//	private boolean isList = false;
//	private boolean isShow = false;
//	private boolean isLinksFrom = false;
//	private boolean isLinksTo = false;
//	private boolean isLocate = false;
//	private ArrayList<CeInstance> newInstances = null;
//	private TemplateProcessor tp = null;
//	private AnswerResultSet newInstRs = null;
//	private ArrayList<CeConcept> extraCons = null;
//
//	public CeQuestionExecutor(ActionContext pAc, boolean pDebug, long pStartTime, AnswerReply pReply,
//			ArrayList<ProcessedWord> pAllWords, ArrayList<ChosenWord> pChosenWords, int pQuestionConf,
//			ArrayList<CeInstance> pNewInsts) {
//		super(pAc, pDebug, pStartTime);
//		this.reply = pReply;
//		this.allWords = pAllWords;
//		this.chosenWords = pChosenWords;
//		this.qConf = pQuestionConf;
//		this.newInstances = pNewInsts;
//		this.extraCons = new ArrayList<CeConcept>();
//		this.tp = new TemplateProcessor(this.ac, this.allWords);
//	}
//
//	public void executeQuestion() {
//		chooseModifiers();
//
//		if ((this.newInstances == null) || (this.newInstances.isEmpty())) {
//			if (this.isCount) {
//				executeCountQuestion();
//			} else if (this.isList) {
//				executeListQuestion();
//			} else if (this.isShow) {
//				executeShowQuestion();
//			} else if (this.isLinksFrom) {
//				executeLinksFromQuestion();
//			} else if (this.isLinksTo) {
//				executeLinksToQuestion();
//			} else if (this.isLocate) {
//				executeLocationQuestion();
//			} else {
//				executeStandardQuestion();
//			}
//		} else {
//			//Just process newly created instances
//			processNewInstances();
//		}
//	}
//
//	private void chooseModifiers() {
//		for (ProcessedWord thisPw : this.allWords) {
//			if (!thisPw.isLaterPartOfPartial()) {
//				if (thisPw.isGroundedOnInstance()) {
//					for (CeInstance thisInst : thisPw.listGroundedInstances()) {
//						if (thisInst.isConceptNamed(this.ac, CON_MODIFIER)) {
//							CeInstance ctInst = thisInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_CORRTO);
//							CeInstance ubdInst = thisInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_CORRTO);
//
//							if (ctInst != null) {
//								if (isCountModifier(ctInst)) {
//									this.isCount = true;
//								} else if (isListModifier(ctInst)) {
//									this.isList = true;
//								} else if (isShowModifier(ctInst)) {
//									this.isShow = true;
//								} else if (isLinksFromModifier(ctInst)) {
//									this.isLinksFrom = true;
//								} else if (isLinksToModifier(ctInst)) {
//									this.isLinksTo = true;
//								} else if (isLocationModifier(ctInst)) {
//									this.isLocate = true;
//								}
//
//								if (thisInst.isConceptNamed(this.ac, CON_ENDMOD)) {
//									addChosenWord(ChosenWord.createAsEndModifier(thisPw, ctInst));
//								} else {
//									addChosenWord(ChosenWord.createAsStartModifier(thisPw, ctInst));
//								}
//
//								if (ubdInst != null) {
//									CeConcept ubdCon = this.ac.getModelBuilder().getConceptNamed(this.ac,
//											ubdInst.getInstanceName());
//
//									if (ubdCon != null) {
//										this.extraCons.add(ubdCon);
//									}
//								}
//							} else {
//								reportDebug("Modifier instance (" + thisInst.getInstanceName()
//										+ ") that does not correspond to anything", this.ac);
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//
//	private static boolean isCountModifier(CeInstance pModInst) {
//		return pModInst.getInstanceName().equals(ModifierHandler.MOD_COUNT);
//	}
//
//	private static boolean isListModifier(CeInstance pModInst) {
//		return pModInst.getInstanceName().equals(ModifierHandler.MOD_LIST);
//	}
//
//	private boolean hasLinksFromModifier(CeInstance pInst) {
//		boolean result = false;
//
//		if (pInst.isConceptNamed(this.ac, CON_MODIFIER)) {
//			CeInstance ctInst = pInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_CORRTO);
//
//			if (ctInst != null) {
//				result = isLinksFromModifier(ctInst);
//			}
//		}
//
//		return result;
//	}
//
//	private static boolean isLinksFromModifier(CeInstance pModInst) {
//		return pModInst.getInstanceName().equals(ModifierHandler.MOD_LINKSFROM);
//	}
//
//	private boolean hasLinksToModifier(CeInstance pInst) {
//		boolean result = false;
//
//		if (pInst.isConceptNamed(this.ac, CON_MODIFIER)) {
//			CeInstance ctInst = pInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_CORRTO);
//
//			if (ctInst != null) {
//				result = isLinksToModifier(ctInst);
//			}
//		}
//
//		return result;
//	}
//
//	private static boolean isLinksToModifier(CeInstance pModInst) {
//		return pModInst.getInstanceName().equals(ModifierHandler.MOD_LINKSTO);
//	}
//
//	private static boolean isShowModifier(CeInstance pModInst) {
//		return pModInst.getInstanceName().equals(ModifierHandler.MOD_SHOW);
//	}
//
//	private static boolean isLocationModifier(CeInstance pModInst) {
//		return pModInst.getInstanceName().equals(ModifierHandler.MOD_LOCATE);
//	}
//
//	private boolean isSpatialThing(CeInstance pInst) {
//		return pInst.isConceptNamed(this.ac, GenericHandler.CON_SPATIAL);
//	}
//
//	private void executeCountQuestion() {
//		ArrayList<CeConcept> tgtCons = new ArrayList<CeConcept>();
//		ArrayList<ChosenWord> tgtChosenWords = new ArrayList<ChosenWord>();
//
//		for (ProcessedWord thisPw : this.allWords) {
//			if (thisPw.isGroundedOnConcept()) {
//				for (CeConcept thisCon : thisPw.listGroundedConcepts()) {
//					if (!tgtCons.contains(thisCon)) {
//						tgtCons.add(thisCon);
//						tgtChosenWords.add(ChosenWord.createAsCeConcept(this.ac, thisPw, thisCon));
//					}
//				}
//			}
//		}
//
//		createLocalCountAnswerFor(tgtCons, tgtChosenWords);
//	}
//
//	private void executeListQuestion() {
//		ArrayList<CeInstance> chosenInsts = new ArrayList<CeInstance>();
//		ArrayList<CeConcept> processedCons = new ArrayList<CeConcept>();
//		String conNames = "";
//		String conNamesPlural = "";
//
//		for (ProcessedWord thisWord : this.allWords) {
//			if (thisWord.isGroundedOnConcept()) {
//				for (CeConcept thisCon : thisWord.listGroundedConcepts()) {
//					if (!processedCons.contains(thisCon)) {
//						processedCons.add(thisCon);
//						ArrayList<CeInstance> conInsts = this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, thisCon.getConceptName());
//
//						for (CeInstance thisInst : conInsts) {
//							if (!chosenInsts.contains(thisInst)) {
//								chosenInsts.add(thisInst);
//							}
//						}
//
//						if (!conNames.isEmpty()) {
//							conNames += " or ";
//						}
//
//						if (!conNamesPlural.isEmpty()) {
//							conNamesPlural += " or ";
//						}
//
//						conNames += thisCon.getConceptName();
//						conNamesPlural += thisCon.pluralFormName(this.ac);
//					}
//				}
//			}
//		}
//
//		if (!chosenInsts.isEmpty()) {
//			processChosenInstances(chosenInsts);
//
//			String titleTextSingle = "there is 1 " + conNames + " defined:";
//			String titleTextPlural = "there are " + chosenInsts.size() + " " + conNamesPlural + " defined:";
//
//			this.newInstRs.applyTitle(titleTextSingle, titleTextPlural, "");		
//		} else {
//			reportWarning("No concepts found for any words in the phrase '" + this.reply.getOriginalQuestion().getQuestionText() + "'", this.ac);
//		}
//	}
//
//	private void executeLinksFromQuestion() {
//		for (ProcessedWord thisWord : this.allWords) {
//			for (CeInstance thisInst : thisWord.listGroundedInstances()) {
//				if (!hasLinksFromModifier(thisInst)) {
//					System.out.println("Matching instance: " + thisInst.toString());
//
//					for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
//						if (thisPi.getRelatedProperty().isObjectProperty()) {
//							String propName = thisPi.getPropertyName();
//	
//							for (String thisVal : thisPi.getValueList()) {
//								System.out.println("  " + propName + thisVal);
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//
//	private void executeLinksToQuestion() {
//		for (ProcessedWord thisWord : this.allWords) {
//			for (CeInstance thisInst : thisWord.listGroundedInstances()) {
//				if (!hasLinksToModifier(thisInst)) {
//					System.out.println("Matching instance: " + thisInst.toString());
//				}
//			}
//		}
//	}
//
//	private void executeShowQuestion() {
////		ArrayList<CeConcept> tgtCons = new ArrayList<CeConcept>();
//		ArrayList<CeInstance> tgtInsts = new ArrayList<CeInstance>();
//
//		// Initialise result set answer
//		ArrayList<String> hdrs = new ArrayList<String>();
//		hdrs.add("label");
//		hdrs.add("start");
//		hdrs.add("end");
//		this.newInstRs = new AnswerResultSet(hdrs);
//		Answer thisAnswer = Answer.create(ANSKEY_CONS, null, this.newInstRs, DEFAULT_COUNT_CONF, null);
//		saveAnswer(thisAnswer, null);
//
//		// Generate title text
//		String titleTextSingle = "there is 1 instance defined:";
//		String titleTextPlural = "there are " + tgtInsts.size() + " durations defined:";
//
//		this.newInstRs.applyTitle(titleTextSingle, titleTextPlural, "");
//	}
//
//	private void executeLocationQuestion() {
//		ArrayList<ArrayList<CeInstance>> tgtInstances = new ArrayList<ArrayList<CeInstance>>();
//		ArrayList<ChosenWord> tgtChosenWords = new ArrayList<ChosenWord>();
//		ArrayList<CeInstance> nonLocInstances = new ArrayList<CeInstance>();
//		ArrayList<ChosenWord> nonLocChosenWords = new ArrayList<ChosenWord>();
//
//		for (ProcessedWord thisPw : this.allWords) {
//			if (thisPw.isGroundedOnInstance()) {
//				for (CeInstance thisInst : thisPw.listGroundedInstances()) {
//					if (isSpatialThing(thisInst)) {
//						if (!tgtInstances.contains(thisInst)) {
//							ArrayList<CeInstance> newRow = new ArrayList<CeInstance>();
//							newRow.add(thisInst);
//							newRow.add(thisInst);
//							tgtInstances.add(newRow);
//							tgtChosenWords.add(ChosenWord.createAsCeInstance(thisPw, thisInst));
//						}
//					} else {
//						if (!thisInst.isConceptNamed(this.ac, GenericHandler.CON_MODIFIER)) {
//							boolean foundInst = false;
//							//Check related instances to see if they are spatial
//							for (CeInstance relInst : thisInst.getAllRelatedInstances(this.ac)) {
//								if (isSpatialThing(relInst)) {
//									if (!tgtInstances.contains(relInst)) {
//										ArrayList<CeInstance> newRow = new ArrayList<CeInstance>();
//										newRow.add(thisInst);
//										newRow.add(relInst);
//										tgtInstances.add(newRow);
//										tgtChosenWords.add(ChosenWord.createAsCeInstance(thisPw, relInst));
//										foundInst = true;
//									}
//								}
//							}
//
//							if (!foundInst) {
//								if (!nonLocInstances.contains(thisInst)) {
//									nonLocInstances.add(thisInst);
//									nonLocChosenWords.add(ChosenWord.createAsCeInstance(thisPw, thisInst));
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//		if (!tgtInstances.isEmpty()) {
//			for (int i = 0; i < tgtInstances.size(); i++) {
//				ArrayList<CeInstance> instPair = tgtInstances.get(i);
//				CeInstance mainInst = instPair.get(0);
//				CeInstance locInst = instPair.get(1);
//				ChosenWord thisWord = tgtChosenWords.get(i);
//
//				createLocalLocateAnswerFor(mainInst, locInst, thisWord);
//			}
//		} else {
//			handleCannotLocateError(this.ac, this.reply, nonLocChosenWords);
//		}
//	}
//
//	private void executeStandardQuestion() {
//		ArrayList<CeInstance> tgtInstances = new ArrayList<CeInstance>();
//		ArrayList<ChosenWord> tgtChosenWords = new ArrayList<ChosenWord>();
//		TreeMap<String, CeProperty> tgtProps = new TreeMap<String, CeProperty>();
//
//		for (ProcessedWord thisPw : this.allWords) {
//			if (!thisPw.isLaterPartOfPartial()) {
//				if (thisPw.isGroundedOnInstance()) {
//					for (CeInstance thisInst : thisPw.listGroundedInstances()) {
////						if (!thisInst.isOnlyConceptNamed(this.ac, CON_CONFCON)) {
//							if (!thisInst.isOnlyConceptNamed(this.ac, CON_MODIFIER)) {
//								if (!tgtInstances.contains(thisInst)) {
//									tgtInstances.add(thisInst);
//									tgtChosenWords.add(ChosenWord.createAsCeInstance(thisPw, thisInst));
//								}
//							}
////						}
//					}
//				}
//
//				//DSB 20/10/2015 - Ensure that instances that match on part of a longer name are removed
//				GenericHandler.winnowShorterMatchesFrom(this.ac, tgtInstances);
//
//				if (thisPw.isGroundedOnProperty()) {
//					for (CeProperty thisProp : thisPw.listGroundedProperties()) {
//						CeInstance mmInst = thisProp.getMetaModelInstance(this.ac);
//						String keyName = mmInst.getInstanceName();
//
//						if (!tgtProps.containsKey(keyName)) {
//							CeProperty actualProp = this.ac.getModelBuilder().getPropertyFullyNamed(keyName);
//							tgtProps.put(keyName, actualProp);
//							addChosenWord(ChosenWord.createAsCeProperty(this.ac, thisPw, actualProp));
//						}
//					}
//				}
//
//				//TODO: Handle more types of words
//			}
//		}
//
//		ArrayList<ArrayList<CeInstance>> clusters = createClustersFrom(tgtInstances);
//
//		for (int i = 0; i < clusters.size(); i++) {
//			ArrayList<CeInstance> thisRow = clusters.get(i);
//			CeInstance thisInst = thisRow.get(0);
//			ChosenWord thisWord = tgtChosenWords.get(i); //TODO: This is no longer safe - the index may not match the chosen word... needs to be refactored
//
//			if (isSpatialThing(thisInst)) {
//				//If it is a spatial thing then create a location answer instead
//				createLocalLocateAnswerFor(thisInst, thisInst, thisWord);
//			}
//
//			//Augment the property list
//			//Any property with a range concept named in the question...
//			for (ProcessedWord thisPw : this.allWords) {
//				if (thisPw.isGroundedOnConcept()) {
//					for (CeConcept thisCon : thisPw.listGroundedConcepts()) {
//						for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
//							CeProperty relProp = thisPi.getRelatedProperty();
//
//							if (relProp.isObjectProperty()) {
//								CeConcept rangeCon = relProp.getRangeConcept();
//
//								if (rangeCon.equalsOrHasParent(thisCon)) {
//									CeProperty actProp = null;
//
//									if (relProp.isInferredProperty()) {
//										actProp = relProp.getStatedSourceProperty();
//									} else {
//										actProp = relProp;
//									}
//
//									String keyName = actProp.formattedFullPropertyName();
//									if (!tgtProps.containsKey(keyName)) {
//										CeProperty actualProp = this.ac.getModelBuilder()
//												.getPropertyFullyNamed(keyName);
//										tgtProps.put(keyName, actualProp);
//										addChosenWord(ChosenWord.createAsCeProperty(this.ac, thisPw, actualProp));
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//
//			ArrayList<CeProperty> propList = new ArrayList<CeProperty>(tgtProps.values());
//
//			if (isBoundToAnyConcept()) {
//				if (isMentionedConcept(thisInst)) {
//					//Handle as a standard answer
//					exploreStandardAnswerFor(thisRow, propList, thisWord);
//				} else if (hasRelatedProperty(thisInst, propList)) {
//					exploreStandardAnswerFor(thisRow, propList, thisWord);
//				}
//			} else {
////				if (!this.reply.hasAnswers()) {
//					//Handle as a standard answer
//					exploreStandardAnswerFor(thisRow, new ArrayList<CeProperty>(tgtProps.values()), thisWord);
////				}
//			}
//		}
//	}
//
//	private boolean hasRelatedProperty(CeInstance pInst, ArrayList<CeProperty> pProps) {
//		boolean result = false;
//
//		for (CeProperty thisProp : pProps) {
//			for (CeConcept thisCon : pInst.getDirectConcepts()) {
//				if (thisProp.getDomainConcept().equalsOrHasParent(thisCon)) {
//					result = true;
//					break;
//				}
//			}
//		}
//
//		return result;
//	}
//
//	private boolean isBoundToAnyConcept() {
//		boolean result = false;
//
//		for (ProcessedWord thisPw : this.allWords) {
//			if (thisPw.isGroundedOnConcept()) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	private void processNewInstances() {
//		ArrayList<CeInstance> chosenInsts = new ArrayList<CeInstance>();
//
//		for (CeInstance thisInst : this.newInstances) {
//			if (!isAlreadyInReply(thisInst)) {
//				if (isMentionedConcept(thisInst)) {
//					chosenInsts.add(thisInst);
//				}
//			}
//		}
//
//		if (chosenInsts.isEmpty()) {
//			for (CeInstance thisInst : this.newInstances) {
//				if (isSeekable(thisInst)) {
//					chosenInsts.add(thisInst);
//				}
//			}
//		}
//
//		processChosenInstances(chosenInsts);
//
//		this.newInstances = chosenInsts;
//	}
//
//	private void processChosenInstances(ArrayList<CeInstance> pInsts) {
//		ArrayList<ArrayList<CeInstance>> clusters = createClustersFrom(pInsts);
//
//		for (ArrayList<CeInstance> thisRow : clusters) {
//			createNewInstanceAnswerFor(thisRow, null);
//		}
//	}
//
//	private ArrayList<ArrayList<CeInstance>> createClustersFrom(ArrayList<CeInstance> pInstList) {
//		ArrayList<ArrayList<CeInstance>> result = new ArrayList<ArrayList<CeInstance>>();
//
//		if (pInstList != null) {
//			for (CeInstance thisInst : pInstList) {
//				ArrayList<CeInstance> thisRow = new ArrayList<CeInstance>();
//
//				thisRow.add(thisInst);
//				for (CeInstance relInst : thisInst.getInstanceListFromPropertyNamed(this.ac,
//						GenericHandler.PROP_SAMEAS)) {
//					thisRow.add(relInst);
//				}
//
//				if (!alreadyPresent(thisRow, result)) {
//					result.add(thisRow);
//				}
//			}
//		}
//
//		return result;
//	}
//
//	private boolean alreadyPresent(ArrayList<CeInstance> pRow, ArrayList<ArrayList<CeInstance>> pResultSet) {
//		boolean result = false;
//
//		for (CeInstance thisInst : pRow) {
//			for (ArrayList<CeInstance> rsRow : pResultSet) {
//				for (CeInstance rsInst : rsRow) {
//					if (thisInst.equals(rsInst)) {
//						result = true;
//						break;
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
//	private boolean isAlreadyInReply(CeInstance pInst) {
//		return this.reply.hasAnswer(pInst.getInstanceName());
//	}
//
//	private boolean isSeekable(CeInstance pInst) {
//		boolean result = false;
//
//		for (CeConcept thisCon : pInst.listAllConcepts()) {
//			CeInstance mmInst = thisCon.retrieveMetaModelInstance(this.ac);
//
//			if (mmInst.isConceptNamed(this.ac, CON_SEEK)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	private boolean isMentionedConcept(CeInstance pInst) {
//		boolean result = false;
//
//		if (ModifierHandler.isSameQuestion(this.ac, this.allWords)) {
//			//Same question
//			for (ProcessedWord thisPw : this.allWords) {
//				if (thisPw.isGroundedOnInstance()) {
//					for (CeInstance gInst : thisPw.listGroundedInstances()) {
//						for (CeConcept thisCon : gInst.getDirectConcepts()) {
//							if (pInst.isConcept(thisCon)) {
//								result = true;
//								break;
//							}
//						}
//					}
//				}
//			}
//		} else {
//			//Search question
//			for (ProcessedWord thisPw : this.allWords) {
//				if (thisPw.isGroundedOnConcept()) {
//					for (CeConcept gCon : thisPw.listGroundedConcepts()) {
//
//						if (pInst.isConcept(gCon)) {
//							result = true;
//							break;
//						}
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
//	private void createLocalLocateAnswerFor(CeInstance pMainInst, CeInstance pLocInst, ChosenWord pWord) {
//		if (isSpatialThing(pLocInst)) {
//			//This is a spatial thing - get the lat and lon and add to the answer
//			String lat = pLocInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_LAT);
//			String lon = pLocInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_LON);
//
//			if (lat.isEmpty() || lon.isEmpty()) {
//				//Next try ere  and address line 1
//				//TODO: These should not be hardcoded
//				String al1 = pLocInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_AL1);
//				String pc = pLocInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_PC);
//
//				if (pc.isEmpty()) {
//					ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//
//					handleNoCoordinatesError(this.ac, this.reply, cws);
//				} else {
//					String refId = pMainInst.getFirstInstanceIdentifier(this.ac);
//					AnswerCoords ac = new AnswerCoords(refId, null, null, pc, al1);
//
//					ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//					CeInstance srcInst = pLocInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_SOURCE);
//
//					createCoordsAnswer(pLocInst.getInstanceName(), cws, ac, srcInst, pLocInst);
//				}
//			} else {
//				String refId = pMainInst.getFirstInstanceIdentifier(this.ac);
//				AnswerCoords ac = new AnswerCoords(refId, lat, lon, null, null);
//
//				ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//				CeInstance srcInst = pLocInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_SOURCE);
//
//				createCoordsAnswer(pLocInst.getInstanceName(), cws, ac, srcInst, pLocInst);
//			}
//		}
//	}
//
//	private void exploreStandardAnswerFor(ArrayList<CeInstance> pInstCluster, ArrayList<CeProperty> pProps,
//			ChosenWord pWord) {
//		CeInstance tgtInst = pInstCluster.get(0);
//
//		if (pProps.isEmpty()) {
//			createLocalStandardAnswerFor(pInstCluster, pWord);
//		} else {
//			boolean haveAnswered = false;
//
//			//TODO: Implement this properly
//			boolean foundProp = false;
//			CeProperty mediaProp = null;
//
//			for (CeProperty thisProp : pProps) {
//				CeConcept rangeCon = thisProp.getRangeConcept();
//				if (rangeCon != null) {
//					if (rangeCon.equalsOrHasParentNamed(this.ac, CON_MEDIA)) {
//						//TODO: Remove this - it should not be hardcoded like this
//						mediaProp = thisProp;
//
//						for (CeInstance propInst : tgtInst.getInstanceListFromPropertyNamed(this.ac,
//								thisProp.getPropertyName())) {
//							if (!foundProp) {
//								if (propInst.isConceptNamed(this.ac, CON_MEDIA)) {
//									createLocalMediaAnswerFor(propInst, tgtInst, pWord);
//									foundProp = true;
//								}
//							}
//						}
//
//						haveAnswered = true;
//					} else {
//						//TODO: Complete this
//						if (tgtInst.isConcept(rangeCon)) {
//							for (CeInstance possInst : this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, thisProp.getDomainConcept().getConceptName())) {
//								//TODO: This will only work for object properties at the moment
//								for (CeInstance rangeInst : possInst.getInstanceListFromPropertyNamed(this.ac, thisProp.getPropertyName())) {
//									if (rangeInst.equals(tgtInst)) {
//										System.out.println("This is a match!!! - " + possInst.getInstanceName());
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//
//			if ((!foundProp) && (mediaProp != null)) {
//				createLocalNoMediaFoundAnswer(tgtInst, mediaProp, pWord);
//			}
//
//			if (!haveAnswered) {
//				createLocalStandardAnswerFor(pInstCluster, pWord);
//			}
//		}
//	}
//
//	private void createLocalCountAnswerFor(ArrayList<CeConcept> pConList, ArrayList<ChosenWord> pWords) {
//		if (!pWords.isEmpty()) {
//			if (pConList.size() == 1) {
//				createLocalSingleCountAnswerFor(pConList.get(0), pWords.get(0));
//			} else {
//				createLocalMultipleCountAnswerFor(pConList, pWords.get(0));
//			}
//		}
//	}
//
//	private void createLocalSingleCountAnswerFor(CeConcept pCon, ChosenWord pWord) {
//		Source ceSrc = Source.ceSource();
//		String rawAnswer = new Integer(this.ac.getModelBuilder().getInstanceCountForConcept(pCon)).toString();
//		String chattyAnswer = "there are " + rawAnswer + " " + pCon.pluralFormName(this.ac) + " defined";
//
//		ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//
//		createCountAnswer(rawAnswer, cws, chattyAnswer, ceSrc);
//	}
//
//	private void createLocalMultipleCountAnswerFor(ArrayList<CeConcept> pConList, ChosenWord pWord) {
//		AnswerResultSet ars = null;
//		Source ceSrc = Source.ceSource();
//		String chattyAnswer = null;
//
//		ArrayList<String> hdrs = new ArrayList<String>();
//		hdrs.add(HDR_TYPE);
//		hdrs.add(HDR_COUNT);
//
//		ars = new AnswerResultSet(hdrs);
//
//		for (int i = 0; i < pConList.size(); i++) {
//			CeConcept thisCon = pConList.get(i);
//			String rawAnswer = new Integer(this.ac.getModelBuilder().getInstanceCountForConcept(thisCon)).toString();
//			ArrayList<String> thisRow = new ArrayList<String>();
//			thisRow.add(thisCon.getConceptName());
//			thisRow.add(rawAnswer);
//
//			ars.addRow(thisRow);
//
//			if (chattyAnswer == null) {
//				chattyAnswer = "there are ";
//			} else {
//				if (i == (pConList.size() - 1)) {
//					chattyAnswer += " and ";
//				} else {
//					chattyAnswer += ", ";
//				}
//			}
//
//			chattyAnswer += rawAnswer + " " + thisCon.pluralFormName(this.ac);
//		}
//
//		chattyAnswer += " defined";
//
//		ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//
//		createResultSetAnswer(ANSKEY_CONS, cws, ars, chattyAnswer, ceSrc);
//	}
//
//	private int calculateLocateModifier() {
//		int modifier = 0;
//		boolean foundWhere = false;
//
//		for (ProcessedWord procWord : this.allWords) {
//			if (procWord.isQuestionWord()) {
//				if (procWord.isWhere()) {
//					foundWhere = true;
//				}
//			}
//		}
//
//		if (foundWhere) {
//			modifier = 0;
//		} else {
//			//This was not a "where" question so reduce the confidence
//			modifier = -50;
//		}
//
//		return modifier;
//	}
//
//	private ArrayList<ChosenWord> chosenWordsPlus(ChosenWord pWord) {
//		ArrayList<ChosenWord> result = new ArrayList<ChosenWord>();
//
//		result.addAll(this.chosenWords);
//
//		if (pWord != null) {
//			result.add(pWord);
//		}
//
//		return result;
//	}
//
//	private void createLocalStandardAnswerFor(ArrayList<CeInstance> pInstCluster, ChosenWord pWord) {
//		CeInstance tgtInst = pInstCluster.get(0);
//
//		if (hasDescription(tgtInst)) {
//			//Simple case - this instance has a description property
//			createNormalAnswerFor(tgtInst, pWord);
//		} else {
//			CeInstance defInst = tgtInst.getSingleInstanceFromPropertyNamed(this.ac, GenericHandler.PROP_DEFBY);
//
//			if (defInst != null) {
//				String lcMf = defInst.getSingleValueFromPropertyNamed(PROP_MAINFORM).toLowerCase();
//				String lcInst = tgtInst.getInstanceName().toLowerCase();
//
//				if (!lcMf.equals(lcInst)) {
//					//Get the description from the glossary term that defines this instance
//					createNormalAnswerFor(defInst, pWord);
//				} else {
//					//This can be ignored as the main form is the same as the property instance name and therefore
//					//it will get picked up anyway.  Suppressing it here prevents duplicates.
//				}
//			} else {
//				//There is no description so we need to compute some text from the instance
//				String desc = createSummaryTextFor(pInstCluster);
//
//				if (desc != null) {
//					ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//
//					CeInstance srcInst = tgtInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_SOURCE);
//					if (getConvConfig() != null) {
//						if (getConvConfig().isReturningSingleAnswers()) {
//							createNormalSingleAnswer(tgtInst, cws, desc, srcInst, true);
//						} else {
//							createNormalAnswer(tgtInst.getInstanceName(), cws, desc, srcInst, tgtInst, true);
//						}
//					} else {
//						createNormalAnswer(tgtInst.getInstanceName(), cws, desc, srcInst, tgtInst, true);
//					}
//				}
//			}
//		}
//	}
//
//	private void createNewInstanceAnswerFor(ArrayList<CeInstance> pInstCluster, ChosenWord pWord) {
//		//There is no description so we need to compute some text from the instance
//		String desc = createSummaryTextFor(pInstCluster);
//
//		if (desc != null) {
//			CeInstance tgtInst = pInstCluster.get(0);
//			ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//
//			Source ceSrc = Source.ceSource();
//			createNewInstanceAnswer(tgtInst, cws, desc, ceSrc);
//		}
//	}
//
//	private void createLocalMediaAnswerFor(CeInstance pImgInst, CeInstance pRefInst, ChosenWord pWord) {
//		AnswerMedia am = null;
//
//		String refId = pRefInst.getInstanceName(); //TODO: This should be the identifier, not the name
//		String imgUrl = pImgInst.getSingleValueFromPropertyNamed(PROP_URL);
//		String imgCredit = pImgInst.getSingleValueFromPropertyNamed(PROP_CREDIT);
//
//		am = new AnswerMedia(refId, imgUrl, imgCredit);
//		ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//
//		CeInstance srcInst = pImgInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_SOURCE);
//
//		createMediaAnswer(imgUrl, cws, am, srcInst);
//	}
//
//	private void createMediaAnswer(String pKey, ArrayList<ChosenWord> pCws, AnswerMedia pAm, CeInstance pSrcInst) {
//		Answer thisAnswer = Answer.create(pKey, pCws, pAm, DEFAULT_CONF, null);
//
//		saveAnswer(thisAnswer, pSrcInst);
//	}
//
//	private void createCoordsAnswer(String pKey, ArrayList<ChosenWord> pCws, AnswerCoords pAnsCoord,
//			CeInstance pSrcInst, CeInstance pInst) {
//		int modifier = calculateLocateModifier();
//
//		Answer thisAnswer = Answer.create(pKey, pCws, pAnsCoord, answerConfidenceFor(pInst, modifier), null);
//
//		saveAnswer(thisAnswer, pSrcInst);
//	}
//
//	private void createResultSetAnswer(String pKey, ArrayList<ChosenWord> pCws, AnswerResultSet pAnsRs,
//			String pChattyAnswer, Source pCeSrc) {
//		Answer thisAnswer = Answer.create(ANSKEY_CONS, pCws, pAnsRs, DEFAULT_COUNT_CONF, pCeSrc);
//		createChattySimpleAnswer(thisAnswer, pChattyAnswer, pCeSrc.getName());
//
//		saveAnswer(thisAnswer, null);
//	}
//
//	private void createNewInstanceAnswer(CeInstance pInst, ArrayList<ChosenWord> pCws, String pDesc, Source pCeSrc) {
//		initialiseResultSetAnswer(pCws, pCeSrc);
//
//		if (!existingRow(pDesc)) {
//			ArrayList<String> newRow = new ArrayList<String>();
//			newRow.add(pInst.getInstanceName());
//			newRow.add(pDesc);
//
//			this.newInstRs.addRow(newRow);
//		}
//	}
//
//	private boolean existingRow(String pDescription) {
//		boolean result = false;
//
//		for (ArrayList<String> thisRow : this.newInstRs.getRows()) {
//			String thisVal = thisRow.get(0);
//
//			if (pDescription.equals(thisVal)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	private void initialiseResultSetAnswer(ArrayList<ChosenWord> pCws, Source pCeSrc) {
//		if (this.newInstRs == null) {
//			ArrayList<String> hdrs = new ArrayList<String>();
//			hdrs.add("item");
//			this.newInstRs = new AnswerResultSet(hdrs);
//			Answer thisAnswer = Answer.create(ANSKEY_CONS, pCws, this.newInstRs, DEFAULT_COUNT_CONF, pCeSrc);
//			saveAnswer(thisAnswer, null);
//		}
//	}
//
//	private void createNormalAnswer(String pKey, ArrayList<ChosenWord> pCws, String pDesc, CeInstance pSrcInst,
//			CeInstance pInst, boolean pChattyFlag) {
//		int conf = answerConfidenceFor(pInst);
//		String srcName = null;
//
//		if (pChattyFlag) {
//			conf = answerConfidenceFor(pInst, CONF_OFFSET_COMPUTEDDESC);
//		}
//
//		Answer thisAnswer = Answer.create(pKey, pCws, pDesc, conf);
//
//		if (pSrcInst != null) {
//			srcName = pSrcInst.getSingleValueFromPropertyNamed(PROP_SHORTNAME);
//		} else {
//			srcName = "";
//		}
//
//		if (pChattyFlag) {
//			createChattySimpleAnswer(thisAnswer, pDesc, srcName);
//		} else {
//			createChattyAnswer(thisAnswer, pInst.getFirstInstanceIdentifier(this.ac), pDesc, srcName);
//		}
//
//		saveAnswer(thisAnswer, pSrcInst);
//	}
//
//	private void createNormalSingleAnswer(CeInstance pInst, ArrayList<ChosenWord> pCws, String pDesc,
//			CeInstance pSrcInst, boolean pChattyFlag) {
//		Source ceSrc = Source.ceSource();
//		createNewInstanceAnswer(pInst, pCws, pDesc, ceSrc);
//	}
//
//	private void createCountAnswer(String pKey, ArrayList<ChosenWord> pCws, String pChattyAnswer, Source pCeSrc) {
//		Answer thisAnswer = Answer.create(pKey, pCws, pKey, DEFAULT_COUNT_CONF, pCeSrc);
//		thisAnswer.markAsCountAnswer();
//
//		createChattySimpleAnswer(thisAnswer, pChattyAnswer, pCeSrc.getName());
//
//		saveAnswer(thisAnswer, null);
//	}
//
//	private void saveAnswer(Answer pAnswer, CeInstance pSrcInst) {
//		if (pSrcInst != null) {
//			pAnswer.addSourceUsing(pSrcInst);
//		}
//
//		if (pAnswer != null) {
//			this.reply.addAnswer(this.ac, pAnswer);
//		}
//	}
//
//	private void createLocalNoMediaFoundAnswer(CeInstance pInst, CeProperty pProp, ChosenWord pWord) {
//		String chattyText = "I couldn't find any " + pProp.getPropertyName() + " for " + pInst.getInstanceName();
//		ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//
//		handleNoMediaError(this.ac, this.reply, chattyText, cws);
//	}
//
//	private void createNormalAnswerFor(CeInstance pInst, ChosenWord pWord) {
//		String desc = pInst.getSingleValueFromPropertyNamed(PROP_DESC);
//		ArrayList<ChosenWord> cws = chosenWordsPlus(pWord);
//		CeInstance srcInst = pInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_SOURCE);
//
//		createNormalAnswer(pInst.getInstanceName(), cws, desc, srcInst, pInst, false);
//	}
//
//	private static boolean hasDescription(CeInstance pInst) {
//		String desc = pInst.getSingleValueFromPropertyNamed(PROP_DESC);
//
//		return (!desc.isEmpty());
//	}
//
//	private String createSummaryTextFor(ArrayList<CeInstance> pInstCluster) {
//		String result = null;
//
//		if (isPropertyQuestion()) {
//			result = createSpecificAnswerFor(pInstCluster);
//		} else {
//			result = this.tp.createAnswerFor(pInstCluster);
//		}
//
//		return result;
//	}
//
//	private boolean isPropertyQuestion() {
//		boolean result = false;
//
//		for (ChosenWord thisCw : this.chosenWords) {
//			if (thisCw.isCeProperty()) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	private String createSpecificAnswerFor(ArrayList<CeInstance> pInstCluster) {
//		String result = "";
//		CeInstance tgtInst = pInstCluster.get(0);
//
//		for (ChosenWord thisCw : this.chosenWords) {
//			if (thisCw.isCeProperty()) {
//				CeInstance propInst = thisCw.ceInstance();
//
//				if (propInst != null) {
//					CeProperty tgtProp = this.ac.getModelBuilder().getPropertyFullyNamed(propInst.getInstanceName());
//					String propName = propInst.getSingleValueFromPropertyNamed(PROP_PROPNAME);
//
//					if (tgtProp != null) {
//						if (tgtProp.isObjectProperty()) {
//							if (propMatches(tgtProp, tgtInst)) {
//								ArrayList<CeInstance> relList = tgtInst.getInstanceListFromPropertyNamed(this.ac,
//										propName);
//
//								if (!relList.isEmpty()) {
//									result += "the " + propName + " for " + tgtInst.getFirstInstanceIdentifier(this.ac)
//											+ " is: ";
//
//									for (CeInstance relInst : relList) {
//										ArrayList<CeInstance> wrappedInst = new ArrayList<CeInstance>();
//										wrappedInst.add(relInst);
//										wrappedInst.addAll(relInst.getInstanceListFromPropertyNamed(this.ac,
//												GenericHandler.PROP_SAMEAS));
//
//										result += this.tp.createAnswerFor(wrappedInst);
//										result += "\n";
//									}
//								}
//							}
//						} else {
//							ArrayList<String> valList = tgtInst.getValueListFromPropertyNamed(propName);
//
//							if (!valList.isEmpty()) {
//								result += "the " + propName + " for " + tgtInst.getFirstInstanceIdentifier(this.ac)
//										+ " is ";
//
//								boolean firstTime = true;
//								for (String propVal : valList) {
//									if (!firstTime) {
//										result += ", ";
//									}
//
//									result += propVal;
//									firstTime = false;
//								}
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
//	private boolean propMatches(CeProperty pProp, CeInstance pInst) {
//		boolean result = false;
//
//		//TODO: Check whether it is safe to use this getAllLeafConcepts() method - it may not work how we want
//		for (CeConcept thisCon : pInst.getAllLeafConcepts()) {
//			CeConcept domCon = pProp.getDomainConcept();
//
//			if (thisCon.equalsOrHasParent(domCon)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	private static void createChattyAnswer(Answer pAnswer, String pTerm, String pDef, String pSrcName) {
//		//TODO: This needs to be more dynamic
//		String chattyAnswer = null;
//
//		if (pSrcName != null) {
//			chattyAnswer = "According to $SRC '$TERM' means: $DEF";
//			chattyAnswer = chattyAnswer.replace("$SRC", pSrcName);
//		} else {
//			chattyAnswer = "'$TERM' means: $DEF";
//		}
//
//		chattyAnswer = chattyAnswer.replace("$TERM", pTerm);
//		chattyAnswer = chattyAnswer.replace("$DEF", pDef);
//
//		pAnswer.setChattyAnswerText(chattyAnswer);
//	}
//
//	private static void createChattySimpleAnswer(Answer pAnswer, String pDef, String pSrcName) {
//		//TODO: This needs to be more dynamic
//		String chattyAnswer = null;
//
//		if ((pSrcName != null) && (!pSrcName.isEmpty())) {
//			chattyAnswer = "According to $SRC $DEF";
//			chattyAnswer = chattyAnswer.replace("$SRC", pSrcName);
//		} else {
//			chattyAnswer = "$DEF";
//		}
//
//		chattyAnswer = chattyAnswer.replace("$DEF", pDef);
//
//		pAnswer.setChattyAnswerText(chattyAnswer);
//	}
//
//	private int answerConfidenceFor(CeInstance pInst) {
//		return answerConfidenceFor(pInst, 0);
//	}
//
//	private int answerConfidenceFor(CeInstance pInst, int pModifier) {
//		int confidence = 0;
//
//		CeInstance srcInst = pInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_SOURCE);
//
//		if (srcInst != null) {
//			CeInstance authInst = srcInst.getSingleInstanceFromPropertyNamed(this.ac, GenericHandler.PROP_ISFROM);
//
//			if (authInst != null) {
//				String credVal = authInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_CRED);
//
//				if (!credVal.isEmpty()) {
//					confidence = new Float((new Float(credVal).floatValue() / 100) * this.qConf).intValue();
//				} else {
//					confidence = this.qConf;
//				}
//			} else {
//				confidence = this.qConf;
//			}
//		} else {
//			confidence = this.qConf;
//		}
//
//		//Now apply the modifier
//		confidence = confidence + pModifier;
//
//		//An ensure still in bounds
//		if (confidence > 100) {
//			confidence = 100;
//		}
//
//		if (confidence < 0) {
//			confidence = 0;
//		}
//
//		return confidence;
//	}
//
//	private void addChosenWord(ChosenWord pWord) {
//		if (!this.chosenWords.contains(pWord)) {
//			this.chosenWords.add(pWord);
//		}
//	}
//
}