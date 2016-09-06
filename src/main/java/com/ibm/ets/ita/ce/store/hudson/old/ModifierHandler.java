package com.ibm.ets.ita.ce.store.hudson.old;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class ModifierHandler {
//	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";
//
//	private static final String ANSKEY_STATS = "stats";
//	private static final String ANSKEY_RESET = "reset";
//
//	private static final String MOD_EXPAND = "general:expand";
//	private static final String MOD_RESET = "general:reset";
//	private static final String MOD_CLEARCACHE = "general:clearcache";
//	private static final String MOD_STATS = "general:stats";
//	public static final String MOD_LOCATE = "general:locate";
//	public static final String MOD_COUNT = "general:count";
//	public static final String MOD_LIST = "general:list";
//	public static final String MOD_LINKSFROM = "general:linksfrom";
//	public static final String MOD_LINKSTO = "general:linksto";
//	public static final String MOD_SHOW = "general:show";
//	//	private static final String MOD_GET = "search:get";
//	//	private static final String MOD_STARTS = "search:starts";
//	//	private static final String MOD_ENDS = "search:ends";
//	//	private static final String MOD_LIKE = "search:like";
//	private static final String MOD_SAME = "search:same";
//	private static final String MOD_EQUALS = "filter:equals";
//	private static final String MOD_GT = "filter:greaterthan";
//	private static final String MOD_LT = "filter:lessthan";
//	private static final String MOD_GTX = "filter:greatest";
//	private static final String MOD_LTX = "filter:least";
//	private static final String MOD_CELOAD = "ce:load";
//	private static final String MOD_CESAVE = "ce:save";
//
//	private static final String MODE_LT = "LESSTHAN";
//	private static final String MODE_GT = "GREATERTHAN";
//	private static final String MODE_LTX = "LEAST";
//	private static final String MODE_GTX = "GREATEST";
//	private static final String MODE_EQ = "EQUALS";
//
//	private ActionContext ac = null;
////	private ConvConfig cc = null;
//	private ArrayList<ProcessedWord> allWords = null;
//	private String filterText = "";
//	private String resultTitleSingle = "";
//	private String resultTitlePlural = "";
//
////	private static final String CASE_UPPER = "UPPER";
//
//	public ModifierHandler(ActionContext pAc, ConvConfig pCc, ArrayList<ProcessedWord> pWords) {
//		this.ac = pAc;
////		this.cc = pCc;
//		this.allWords = pWords;
//	}
//
//	public String getResultTitleSingle() {
//		return this.resultTitleSingle;
//	}
//
//	public String getResultTitlePlural() {
//		return this.resultTitlePlural;
//	}
//
//	public String getFilterText() {
//		return this.filterText;
//	}
//
////	public ArrayList<CeInstance> executeSearchQuestion(AnswerReply pReply) {
////		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
////
////		reportDebug("I am a search question", this.ac);
////
////		if (isSameQuestion(this.ac, this.allWords)) {
////			result = processSameQuestion(pReply);
////		} else {
////			result = processSearchQuestion(pReply);
////		}
////
////		return result;
////	}
//
//	public void executeFilterQuestion(AnswerReply pReply, ArrayList<CeInstance> pInsts) {
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//
//		reportDebug("I am a filter question", this.ac);
//
//		if (isEqualsQuestion(this.ac, this.allWords)) {
//			reportDebug("I am an equals question", this.ac);
//			result = processBinaryComputation(pReply, pInsts, MODE_EQ);
//		} else if (isLessThanQuestion(this.ac, this.allWords)) {
//			reportDebug("I am a less than question", this.ac);
//			result = processBinaryComputation(pReply, pInsts, MODE_LT);
//		} else if (isGreaterThanQuestion(this.ac, this.allWords)) {
//			reportDebug("I am a greater than question", this.ac);
//			result = processUnaryComputation(pReply, pInsts, MODE_GT);
//		} else if (isLeastQuestion(this.ac, this.allWords)) {
//			reportDebug("I am a least question", this.ac);
//			result = processUnaryComputation(pReply, pInsts, MODE_LTX);
//		} else if (isGreatestQuestion(this.ac, this.allWords)) {
//			reportDebug("I am a greatest question", this.ac);
//			result = processUnaryComputation(pReply, pInsts, MODE_GTX);
//		} else {
//			reportDebug("Unknown filter question", this.ac);
//		}
//
//		pReply.trimAnswersUsing(result);
//	}
//
//	public ArrayList<CeInstance> executeNonSearchQuestion(AnswerReply pReply) {
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//
//		if (isExpandQuestion(this.ac, this.allWords)) {
//			//Nothing is needed here - expands are handled downstream
//		} else if (isResetQuestion(this.ac, this.allWords)) {
//			processResetQuestion(pReply);
//		} else if (isClearCacheQuestion(this.ac, this.allWords)) {
//			processClearCacheQuestion(pReply);
//		} else if (isStatsQuestion(this.ac, this.allWords)) {
//			processStatsQuestion(pReply);
//		} else if (isCeLoadStatement(this.ac, this.allWords)) {
//			processCeLoadStatement(pReply);
//		} else if (isCeSaveStatement(this.ac, this.allWords)) {
//			processCeSaveStatement(pReply);
//		}
//
//		return result;
//	}
//
//	public static boolean isExpandQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_EXPAND);
//	}
//
//	private static boolean isResetQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_RESET);
//	}
//
//	private static boolean isClearCacheQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_CLEARCACHE);
//	}
//
//	private static boolean isStatsQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_STATS);
//	}
//
//	public static boolean isCeLoadStatement(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_CELOAD);
//	}
//
//	public static boolean isCeSaveStatement(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_CESAVE);
//	}
//
//	public static boolean isCeModifier(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_CELOAD) ||
//				questionMatchesFunctionNamed(pAc, pWords, MOD_CESAVE);
//	}
//
//	public static boolean isSameQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_SAME);
//	}
//
//	private static boolean isGreaterThanQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_GT);
//	}
//
//	private static boolean isLessThanQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_LT);
//	}
//
//	private static boolean isGreatestQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_GTX);
//	}
//
//	private static boolean isLeastQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_LTX);
//	}
//
//	private static boolean isEqualsQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		return questionMatchesFunctionNamed(pAc, pWords, MOD_EQUALS);
//	}
//
//	private static boolean questionMatchesFunctionNamed(ActionContext pAc, ArrayList<ProcessedWord> pWords,
//			String pFuncName) {
//		boolean result = false;
//
//		for (ProcessedWord thisWord : pWords) {
//			if (thisWord.isModifier(pAc)) {
//				for (CeInstance thisInst : thisWord.listGroundedInstances()) {
//					for (CeInstance corrInst : thisInst.getInstanceListFromPropertyNamed(pAc,
//							GenericHandler.PROP_CORRTO)) {
//						if (corrInst.getInstanceName().equals(pFuncName)) {
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
////	private ArrayList<CeInstance> processSameQuestion(AnswerReply pReply) {
////		reportDebug("I am a 'same' question", this.ac);
////
////		return doSameProcessing(pReply);
////	}
//
//	private ArrayList<CeInstance> processBinaryComputation(AnswerReply pReply, ArrayList<CeInstance> pInsts,
//			String pMode) {
//		String numWord = null;
//		CeConcept rangeCon = null;
//
//		for (ProcessedWord thisPw : this.allWords) {
//			if (thisPw.isFilterModifier(this.ac)) {
//				for (CeInstance filtInst : thisPw.listGroundedInstances()) {
//					CeInstance rangeInst = filtInst.getSingleInstanceFromPropertyNamed(this.ac,
//							GenericHandler.PROP_INVOLVES);
//
//					if (rangeInst != null) {
//						rangeCon = this.ac.getModelBuilder().getConceptNamed(this.ac, rangeInst.getInstanceName());
//					}
//				}
//			}
//		}
//
//		if (rangeCon == null) {
//			//Look for a range concept in the properties
//			for (CeProperty thisProp : listMentionedProperties()) {
//				if (thisProp.isObjectProperty()) {
//					//TODO: This should handle multiple properties
//					rangeCon = thisProp.getRangeConcept();
//					break;
//				}
//			}
//		}
//
//		for (ProcessedWord thisWord : this.allWords) {
//			if (thisWord.isNumberWord()) {
//				numWord = thisWord.getWordText();
//				break;
//			}
//		}
//
//		return doBinaryComputation(pReply, pInsts, rangeCon, numWord, pMode);
//	}
//
//	private ArrayList<CeInstance> processUnaryComputation(AnswerReply pReply, ArrayList<CeInstance> pInsts,
//			String pMode) {
//		CeConcept rangeCon = null;
//
//		for (ProcessedWord thisPw : this.allWords) {
//			if (thisPw.isFilterModifier(this.ac)) {
//				for (CeInstance filtInst : thisPw.listGroundedInstances()) {
//					CeInstance rangeInst = filtInst.getSingleInstanceFromPropertyNamed(this.ac,
//							GenericHandler.PROP_INVOLVES);
//
//					if (rangeInst != null) {
//						rangeCon = this.ac.getModelBuilder().getConceptNamed(this.ac, rangeInst.getInstanceName());
//					}
//				}
//			}
//		}
//
//		if (rangeCon == null) {
//			//Look for a range concept in the properties
//			for (CeProperty thisProp : listMentionedProperties()) {
//				if (thisProp.isObjectProperty()) {
//					//TODO: This should handle multiple properties
//					rangeCon = thisProp.getRangeConcept();
//					break;
//				}
//			}
//		}
//
//		return doUnaryComputation(pReply, pInsts, rangeCon, pMode);
//	}
//
//	private void processResetQuestion(AnswerReply pReply) {
//		reportDebug("I am a 'reset' question", this.ac);
//
//		QuestionManagementHandler qh = new QuestionManagementHandler(this.ac, false, System.currentTimeMillis());
//		qh.handleReset();
//
//		pReply.addAnswer(this.ac, Answer.create(ANSKEY_RESET, null, "The environment has been reset", 0));
//	}
//
//	private void processClearCacheQuestion(AnswerReply pReply) {
//		reportDebug("I am a 'clear cache' question", this.ac);
//		GenericHandler.clearCaches(this.ac);
//		pReply.addAnswer(this.ac, Answer.create(ANSKEY_RESET, null, "The caches have been cleared", 0));
//	}
//
//	private void processStatsQuestion(AnswerReply pReply) {
//		reportDebug("I am a 'stats' question", this.ac);
//		String statsText = QuestionManagementHandler.calculateStatusString(this.ac);
//
//		pReply.addAnswer(this.ac, Answer.create(ANSKEY_STATS, null, statsText, 0));
//	}
//
//	private void processCeLoadStatement(AnswerReply pReply) {
//		reportDebug("I am a 'CE load' statement", this.ac);
//
//		String tgtUrl = extractCeUrl();
//		String summaryText = null;
//
//		if (!tgtUrl.isEmpty()) {
//			summaryText = QuestionManagementHandler.ceLoad(this.ac, tgtUrl);
//		} else {
//			summaryText = "ERROR: No location to load from was found";
//		}
//
//		pReply.addAnswer(this.ac, Answer.create(ANSKEY_STATS, null, summaryText, 0));
//	}
//
//	private void processCeSaveStatement(AnswerReply pReply) {
//		reportDebug("I am a 'CE save' statement", this.ac);
//
//		String ceToSave = extractCeText();
//		String summaryText = QuestionManagementHandler.ceSave(this.ac, ceToSave);
//
//		pReply.addAnswer(this.ac, Answer.create(ANSKEY_STATS, null, summaryText, 0));
//	}
//
//	private String extractCeUrl() {
//		String result = "";
//
//		for (ProcessedWord thisWord : this.allWords) {
//			if (thisWord.isModifier(this.ac)) {
//				for (CeInstance thisInst : thisWord.listGroundedInstances()) {
//					if (thisInst.isConceptNamed(this.ac, GenericHandler.CON_CEMOD)) {
//						result = thisInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_CEURL);
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
//	private String extractCeText() {
//		String result = "";
//
//		for (ProcessedWord thisWord : this.allWords) {
//			boolean useThisWord = true;
//
//			if (thisWord.isModifier(this.ac)) {
//				for (CeInstance thisInst : thisWord.listGroundedInstances()) {
//					if (thisInst.isConceptNamed(this.ac, GenericHandler.CON_CEMOD)) {
//						useThisWord = false;
//					}
//				}
//			}
//
//			if (useThisWord) {
//				if (!result.isEmpty()) {
//					result += " ";
//				}
//
//				result += thisWord.getWordText();
//			}
//		}
//
//		if (!result.endsWith(".")) {
//			result += ".";
//		}
//
//		return result;
//	}
//
////	private ArrayList<CeInstance> processSearchQuestion(AnswerReply pReply) {
////		reportDebug("I am a 'search' question", this.ac);
////
////		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
////		ArrayList<String> newInstIds = new ArrayList<String>();
////
////		for (CeInstance ssInst : listSeekableSources()) {
////			String tableName = ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_TNAME);
////			String columnName = ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_CNAME);
////
////			if (!columnName.isEmpty()) {
////				boolean caseSen = new Boolean(ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_CASESEN))
////						.booleanValue();
////				String forcedCase = ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_FCASE);
////				CeInstance tgtPropInst = ssInst.getSingleInstanceFromPropertyNamed(this.ac, GenericHandler.PROP_CORRTO);
////
////				boolean goUpper = false;
////
////				if (forcedCase.equalsIgnoreCase(CASE_UPPER)) {
////					goUpper = true;
////				}
////
////				String searchType = getDisplayTextFor(this.ac, this.allWords);
////
////				if (searchType.isEmpty()) {
////					searchType = "unknown";
////				}
////
////				String tgtVal = extractSeekableName(caseSen, goUpper, tgtPropInst);
////
////				computeResultTitleFromSearch(searchType, tgtVal, tgtPropInst);
////
//////				ArrayList<String> theseNewIds = makeDatabaseSearchCall(this.ac, this.cc, pReply, this.allWords,
//////						tableName, columnName, caseSen, forcedCase, tgtVal);
//////
//////				if (!theseNewIds.isEmpty()) {
//////					newInstIds.addAll(theseNewIds);
//////				}
////			}
////		}
////
////		if (newInstIds != null) {
////			for (String thisInstId : newInstIds) {
////				CeInstance newInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, thisInstId);
////
////				if (newInst != null) {
////					result.add(newInst);
////				}
////			}
////		}
////
////		return result;
////	}
//
////	private void computeResultTitleFromSearch(String pSearchType, String pTgtVal, CeInstance pPropInst) {
////		CeProperty tgtProp = this.ac.getModelBuilder().getPropertyFullyNamed(pPropInst.getInstanceName());
////		CeConcept domCon = tgtProp.getAssertedDomainConcept();
////		String propName = "";
////		String conName = domCon.getConceptName();
////		String conNamePlural = domCon.pluralFormName(this.ac);
////
////		if (tgtProp != null) {
////			propName = tgtProp.getPropertyName();
////		}
////
////		this.resultTitleSingle = "there is 1 " + conName + " with " + propName + " " + pSearchType + " '" + pTgtVal
////				+ "'";
////		this.resultTitlePlural = "there are %COUNT% " + conNamePlural + " with " + propName + " " + pSearchType + " '"
////				+ pTgtVal + "'";
////		this.filterText = " with " + propName + " " + pSearchType + " '" + pTgtVal + "'";
////	}
//
////	private void computeResultTitleFromSame(ArrayList<CeInstance> pTgtInsts, ArrayList<CeProperty> pSameProps) {
////		String conName = "";
////		String conNamePlural = "";
////		String propName = "";
////		String instNameText = null;
////		CeProperty sameProp = null;
////
////		for (CeInstance tgtInst : pTgtInsts) {
////			if (instNameText == null) {
////				instNameText = "";
////			} else {
////				instNameText += " or ";
////			}
////
////			instNameText += tgtInst.getFirstInstanceIdentifier(this.ac);
////		}
////
////		if (!pSameProps.isEmpty()) {
////			sameProp = pSameProps.get(0);
////
////			if (pSameProps.size() > 1) {
////				String propText = null;
////
////				for (CeProperty thisProp : pSameProps) {
////					if (propText == null) {
////						propText = "";
////					} else {
////						propText += ", ";
////					}
////
////					propText += thisProp.getPropertyName();
////				}
////
////				reportWarning(
////						"More than one matched property detected during 'same' processing.  Only the first will be used. ("
////								+ propText + ")",
////						this.ac);
////			}
////		}
////
////		if (sameProp != null) {
////			propName = sameProp.getPropertyName();
////			conName = sameProp.getDomainConcept().getConceptName();
////			conNamePlural = sameProp.getDomainConcept().pluralFormName(this.ac);
////		} else {
////			propName = "?";
////		}
////
////		this.filterText = " with the same " + propName + " as " + instNameText;
////		this.resultTitleSingle = "there is 1 " + conName + " with " + propName + this.filterText;
////		this.resultTitlePlural = "there are %COUNT% " + conNamePlural + this.filterText;
////	}
//
////	private String extractSeekableName(boolean pCaseSen, boolean pGoUpper, CeInstance pPropInst) {
////		String tgtVal = null;
////		boolean foundSeekWord = false;
////		String tgtPropName = pPropInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_PROPNAME);
////
////		for (ProcessedWord thisWord : this.allWords) {
////			if (foundSeekWord) {
////				if (!thisWord.getLcWordText().equals(tgtPropName)) {
////					if (tgtVal == null) {
////						tgtVal = "";
////					} else {
////						tgtVal += " ";
////					}
////
////					if (pCaseSen) {
////						tgtVal += thisWord.getWordText();
////					} else {
////						if (pGoUpper) {
////							tgtVal += thisWord.getWordText().toUpperCase();
////						} else {
////							tgtVal += thisWord.getLcWordText();
////						}
////					}
////				}
////			}
////
////			if (thisWord.isSearchModifier(this.ac)) {
////				foundSeekWord = true;
////			}
////		}
////
////		return tgtVal;
////	}
//
////	private ArrayList<CeInstance> doSameProcessing(AnswerReply pReply) {
////		ArrayList<CeInstance> result = null;
////
////		ArrayList<CeInstance> instList = listMentionedInstances();
////		ArrayList<CeProperty> propList = listMentionedProperties();
////		ArrayList<CeConcept> conList = listMentionedConcepts();
////
////		SameProcessor sp = new SameProcessor(this.ac, this.cc, this.allWords, instList, propList, conList);
////
////		computeResultTitleFromSame(instList, propList);
////
////		result = sp.doSameProcessing(pReply);
////
////		return result;
////	}
//
//	private ArrayList<CeInstance> doBinaryComputation(AnswerReply pReply, ArrayList<CeInstance> pInsts,
//			CeConcept pRangeCon, String pTgtVal, String pMode) {
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//		int tgtInt = new Integer(pTgtVal).intValue();
//		String propNames = "";
//
//		ArrayList<CeProperty> matchedPropList = new ArrayList<CeProperty>();
//
//		boolean foundPropMatch = false;
//
//		for (CeInstance thisInst : pInsts) {
//			for (CeProperty thisProp : thisInst.retrieveAllProperties(this.ac)) {
//				if (thisProp.isObjectProperty()) {
//					if (thisProp.getRangeConcept().equals(pRangeCon)) {
//						for (String thisVal : thisInst.getValueListFromPropertyNamed(thisProp.getPropertyName())) {
//							int thisInt = new Integer(thisVal).intValue();
//
//							if (pMode.equals(MODE_LT)) {
//								if (thisInt < tgtInt) {
//									if (!result.contains(thisInst)) {
//										result.add(thisInst);
//									}
//									foundPropMatch = true;
//								}
//							} else if (pMode.equals(MODE_GT)) {
//								if (thisInt > tgtInt) {
//									if (!result.contains(thisInst)) {
//										result.add(thisInst);
//									}
//									foundPropMatch = true;
//								}
//							} else if (pMode.equals(MODE_EQ)) {
//								if (thisInt == tgtInt) {
//									if (!result.contains(thisInst)) {
//										result.add(thisInst);
//									}
//									foundPropMatch = true;
//								}
//							}
//						}
//					}
//
//					if (foundPropMatch) {
//						CeProperty statedProp = null;
//
//						if (thisProp.isStatedProperty()) {
//							statedProp = thisProp;
//						} else {
//							statedProp = thisProp.getStatedSourceProperty();
//						}
//
//						if (!matchedPropList.contains(statedProp)) {
//							matchedPropList.add(statedProp);
//						}
//					}
//					foundPropMatch = false;
//				}
//			}
//		}
//
//		for (CeProperty thisProp : matchedPropList) {
//			if (!propNames.isEmpty()) {
//				propNames += ", ";
//			}
//
//			propNames += thisProp.getPropertyName();
//		}
//
//		if (propNames.isEmpty()) {
//			if (pRangeCon != null) {
//				propNames = pRangeCon.getConceptName();
//			}
//		}
//
//		String modeName = "";
//
//		if (pMode.equals(MODE_LT)) {
//			modeName = "less than";
//		} else if (pMode.equals(MODE_GT)) {
//			modeName = "greater than";
//		} else if (pMode.equals(MODE_EQ)) {
//			modeName = "equals";
//		}
//
//		if (!propNames.isEmpty()) {
//			this.filterText = " with " + propNames + " " + modeName + " '" + pTgtVal + "'";
//		}
//
//		return result;
//	}
//
//	private ArrayList<CeInstance> doUnaryComputation(AnswerReply pReply, ArrayList<CeInstance> pInsts,
//			CeConcept pRangeCon, String pMode) {
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//		ArrayList<CeInstance> possInsts = new ArrayList<CeInstance>();
//		String propNames = "";
//		TreeMap<CeProperty, Integer> tgtVals = new TreeMap<CeProperty, Integer>();
//
//		//TODO: Handle properties by name too.  Currently this only does named range concepts
//		for (CeInstance thisInst : pInsts) {
//			for (CeProperty thisProp : thisInst.retrieveAllProperties(this.ac)) {
//				CeProperty statedProp = null;
//
//				if (thisProp.isStatedProperty()) {
//					statedProp = thisProp;
//				} else {
//					statedProp = thisProp.getStatedSourceProperty();
//				}
//
//				if (statedProp.isObjectProperty()) {
//					if (statedProp.getRangeConcept().equals(pRangeCon)) {
//						for (String thisVal : thisInst.getValueListFromPropertyNamed(statedProp.getPropertyName())) {
//							int thisInt = new Integer(thisVal).intValue();
//							Integer existingVal = tgtVals.get(statedProp);
//
//							if (existingVal == null) {
//								tgtVals.put(statedProp, new Integer(thisInt));
//							} else {
//								if (pMode.equals(MODE_LTX)) {
//									if (thisInt < existingVal.intValue()) {
//										tgtVals.put(statedProp, new Integer(thisInt));
//										possInsts.add(thisInst);
//									}
//								} else if (pMode.equals(MODE_GTX)) {
//									if (thisInt > existingVal.intValue()) {
//										tgtVals.put(statedProp, new Integer(thisInt));
//										possInsts.add(thisInst);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//		for (CeProperty thisProp : tgtVals.keySet()) {
//			String tgtVal = tgtVals.get(thisProp).toString();
//
//			if (!propNames.isEmpty()) {
//				propNames += ", ";
//			}
//
//			propNames += thisProp.getPropertyName();
//
//			for (CeInstance thisInst : possInsts) {
//				ArrayList<String> allVals = thisInst.getValueListFromPropertyNamed(thisProp.getPropertyName());
//
//				if (allVals.contains(tgtVal)) {
//					if (!result.contains(thisInst)) {
//						result.add(thisInst);
//					}
//				}
//			}
//
//		}
//
//		String modeName = "";
//
//		if (pMode.equals(MODE_LTX)) {
//			modeName = "least";
//		} else if (pMode.equals(MODE_GTX)) {
//			modeName = "greatest";
//		}
//
//		if (!propNames.isEmpty()) {
//			this.filterText = " with " + modeName + " " + propNames;
//		}
//
//		return result;
//	}
//
////	private ArrayList<CeInstance> listMentionedInstances() {
////		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
////
////		for (ProcessedWord thisWord : this.allWords) {
////			if (thisWord.isGroundedOnInstance()) {
////				for (CeInstance thisInst : thisWord.listGroundedInstances()) {
////					if (!thisInst.isOnlyConceptNamed(this.ac, GenericHandler.CON_CONFCON)) {
////						if (!thisInst.isOnlyConceptNamed(this.ac, GenericHandler.CON_MODIFIER)) {
////							if (!result.contains(thisInst)) {
////								result.add(thisInst);
////							}
////						}
////					}
////				}
////			}
////		}
////
////		return result;
////	}
//
////	private ArrayList<CeConcept> listMentionedConcepts() {
////		ArrayList<CeConcept> result = new ArrayList<CeConcept>();
////
////		for (ProcessedWord thisWord : this.allWords) {
////			if (thisWord.isGroundedOnConcept()) {
////				for (CeConcept thisCon : thisWord.listGroundedConcepts()) {
////					if (!result.contains(thisCon)) {
////						result.add(thisCon);
////					}
////				}
////			}
////		}
////
////		return result;
////	}
//
//	private ArrayList<CeProperty> listMentionedProperties() {
//		ArrayList<CeProperty> result = new ArrayList<CeProperty>();
//
//		for (ProcessedWord thisWord : this.allWords) {
//			if (thisWord.isGroundedOnProperty()) {
//				for (CeProperty thisProp : thisWord.listGroundedProperties()) {
//					if (!result.contains(thisProp)) {
//						result.add(thisProp);
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
////	public static ArrayList<String> makeDatabaseSearchCall(ActionContext pAc, ConvConfig pCc, AnswerReply pReply,
////			ArrayList<ProcessedWord> pAllWords, String pTableName, String pColName, boolean pCaseSen,
////			String pForcedCase, String pTgtVal) {
////		ArrayList<String> newInstIds = new ArrayList<String>();
////		String sqlText = null;
////		String filterText = null;
////		String extraFilterText = getFilterTextFor(pAc, pAllWords);
////
////		if (pCaseSen) {
////			//It is case sensitive so just a straight filter
////			filterText = pColName;
////		} else {
////			//It is not case sensitive
////			if ((pForcedCase == null) || (pForcedCase.isEmpty())) {
////				//There is no forced case so we need to use the lower() function
////				filterText = "lower (" + pColName + ")";
////			} else {
////				filterText = pColName;
////			}
////		}
////
////		if (!extraFilterText.isEmpty()) {
////			sqlText = "select * from " + pTableName + " where " + filterText + " " + extraFilterText;
////		} else {
////			reportError("Unable to determine question type", pAc);
////		}
////
////		if (sqlText != null) {
////			if (pTgtVal != null) {
////				sqlText = sqlText.replaceAll("%01", pTgtVal);
////				reportDebug("Executing SQL: " + sqlText, pAc);
////				long start = System.currentTimeMillis();
////
////				boolean hadError = DatabaseConnection.executeRawSqlQuery(pAc, pCc, sqlText, pReply, newInstIds);
////
////				reportDebug("SQL execution complete, duration was " + (System.currentTimeMillis() - start) + "ms", pAc);
////
////				if (hadError) {
////					//There was an error - an answer object has already been created containing the details...
////				}
////			} else {
////				reportError("No target name was detected", pAc);
////			}
////		}
////
////		return newInstIds;
////	}
//
////	public static ArrayList<String> makeDatabaseFilterCall(ActionContext pAc, ConvConfig pCc, AnswerReply pReply,
////			ArrayList<ProcessedWord> pAllWords, String pTableName, ArrayList<ArrayList<String>> pFilterInfo,
////			boolean pCaseSen, String pForcedCase) {
////		ArrayList<String> newInstIds = new ArrayList<String>();
////		String sqlText = null;
////		String filterText = null;
////
////		for (ArrayList<String> thisFilter : pFilterInfo) {
////			String thisCol = thisFilter.get(0);
////			String thisVal = thisFilter.get(1);
////
////			if (filterText == null) {
////				filterText = "";
////			} else {
////				filterText += " AND ";
////			}
////
////			if (pCaseSen) {
////				//It is case sensitive so just a straight filter
////				filterText += thisCol;
////			} else {
////				//It is not case sensitive
////				if ((pForcedCase == null) || (pForcedCase.isEmpty())) {
////					//There is no forced case so we need to use the lower() function
////					filterText += "lower (" + thisCol + ")";
////				} else {
////					filterText += thisCol;
////				}
////			}
////
////			filterText += "='" + thisVal + "'";
////		}
////
////		sqlText = "select * from " + pTableName + " where " + filterText;
////
////		if (sqlText != null) {
////			reportDebug("Executing SQL: " + sqlText, pAc);
////			long start = System.currentTimeMillis();
////
////			boolean hadError = DatabaseConnection.executeRawSqlQuery(pAc, pCc, sqlText, pReply, newInstIds);
////
////			reportDebug("SQL execution complete, duration was " + (System.currentTimeMillis() - start) + "ms", pAc);
////
////			if (hadError) {
////				//There was an error - an answer object has already been created containing the details...
////			}
////		}
////
////		return newInstIds;
////	}
//
////	private static String getFilterTextFor(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
////		return getRelatedTextFor(pAc, pWords, GenericHandler.PROP_FILTTEXT);
////	}
////
////	private static String getDisplayTextFor(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
////		return getRelatedTextFor(pAc, pWords, GenericHandler.PROP_DISPTEXT);
////	}
////
////	private static String getRelatedTextFor(ActionContext pAc, ArrayList<ProcessedWord> pWords, String pPropName) {
////		String result = "";
////
////		for (ProcessedWord thisPw : pWords) {
////			if (thisPw.isSearchModifier(pAc)) {
////				for (CeInstance thisInst : thisPw.listGroundedInstances()) {
////					CeInstance sfInst = thisInst.getSingleInstanceFromPropertyNamed(pAc, GenericHandler.PROP_CORRTO);
////
////					if (sfInst != null) {
////						result = sfInst.getSingleValueFromPropertyNamed(pPropName);
////
////						if (!result.isEmpty()) {
////							break;
////						}
////					}
////				}
////			}
////		}
////
////		return result;
////	}
//
////	private ArrayList<CeInstance> listSeekableSources() {
////		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
////		ArrayList<CeInstance> rawList = this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac,
////				GenericHandler.CON_SEEK);
////		ArrayList<CeProperty> propList = listMentionedProperties();
////
////		if (propList.isEmpty()) {
////			//There are no properties so seek default SQL sources
////			for (CeInstance thisInst : rawList) {
////				for (CeInstance srcInst : thisInst.getInstanceListFromPropertyNamed(this.ac,
////						GenericHandler.PROP_POTSRC)) {
////					if (srcInst.isConceptNamed(this.ac, GenericHandler.CON_DEFSS)) {
////						result.add(srcInst);
////					}
////				}
////			}
////		} else {
////			//There are properties, so seek matching SQL sources
////			for (CeProperty thisProp : propList) {
////				CeInstance mmInst = thisProp.getMetaModelInstance(this.ac);
////
////				for (CeInstance thisInst : rawList) {
////					for (CeInstance srcInst : thisInst.getInstanceListFromPropertyNamed(this.ac,
////							GenericHandler.PROP_POTSRC)) {
////						for (CeInstance corrInst : srcInst.getInstanceListFromPropertyNamed(this.ac,
////								GenericHandler.PROP_CORRTO)) {
////							if (mmInst.equals(corrInst)) {
////								result.add(srcInst);
////							}
////						}
////					}
////				}
////			}
////		}
////
////		return result;
////	}
//
//	public static boolean isModifierQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		boolean result = false;
//
//		for (ProcessedWord thisPw : pWords) {
//			if (thisPw.isModifier(pAc)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	public static boolean isSearchQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		boolean result = false;
//
//		for (ProcessedWord thisPw : pWords) {
//			if (thisPw.isSearchModifier(pAc)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	public static boolean isFilterQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		boolean result = false;
//
//		for (ProcessedWord thisPw : pWords) {
//			if (thisPw.isFilterModifier(pAc)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	public static boolean isFunctionQuestion(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
//		boolean result = false;
//
//		for (ProcessedWord thisPw : pWords) {
//			if (thisPw.isFunctionModifier(pAc)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}
//
}
