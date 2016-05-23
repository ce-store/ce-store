package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.Collection;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSentenceQualified;

public class CeWebSentence extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public CeWebSentence(ActionContext pAc) {
		super(pAc);
	}

	//JSON Response Keys
	private static final String KEY_SEN_TYPE = "sen_type";
	private static final String KEY_SEN_PRIORSEC = "pri_or_sec";
	private static final String KEY_SEN_VAL = "validity";
	private static final String KEY_SEN_SRC = "source";
	private static final String KEY_SEN_SRC_ID = "source_id";
	private static final String KEY_SEN_STRUCTUREDTEXT = "ce_structured_text";

	private static final String TYPE_SEN = "sentence";
	
	private static final String KEY_SEN_QUALSENS = "qualified_sentences";
	private static final String KEY_SEN_RATSTEPS = "rationale";
	private static final String KEY_SENQUAL = "qualifications";
	private static final String KEY_SENINNER = "inner_sentence";
	private static final String KEY_SENINNER_ID = "inner_sentence_id";

	private static final String KEY_SENQUAL_AN = "author_name";
	private static final String KEY_SENQUAL_AC = "author_concept";
	private static final String KEY_SENQUAL_QN = "qualified_name";
	private static final String KEY_SENQUAL_QC = "qualified_concept";
	private static final String KEY_SENQUAL_CN = "context_name";
	private static final String KEY_SENQUAL_CC = "context_concept";
	private static final String KEY_SENQUAL_ST = "statement_type";
	private static final String KEY_SENQUAL_TF = "timeframe";
	private static final String KEY_SENQUAL_TS = "timestamp";
	private static final String KEY_SENQUAL_TV = "truth_value";

	private static final String KEY_IDX = "index";
	private static final String KEY_FRAGTYPE = "type";

	public CeStoreJsonArray generateSummaryListFrom(Collection<CeSentence> pSenList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		generateSummaryListUsing(pSenList, null, jArr);

		return jArr;
	}

	public CeStoreJsonArray generateMinimalListFrom(Collection<CeSentence> pSenList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		generateMinimalListUsing(pSenList, null, jArr);

		return jArr;
	}

	public CeStoreJsonArray generateFullListFrom(Collection<CeSentence> pSenList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		generateFullListUsing(pSenList, null, jArr);
		
		return jArr;
	}

	public void generateSummaryListUsing(Collection<CeSentence> pSenList, String pPriOrSecInd, CeStoreJsonArray pJa) {
		if (pSenList != null) {
			for (CeSentence thisSen : pSenList) {
				addObjectValueTo(pJa, generateSummaryJson(thisSen, pPriOrSecInd));
			}
		}
	}

	public void generateMinimalListUsing(Collection<CeSentence> pSenList, String pPriOrSecInd, CeStoreJsonArray pJa) {
		if (pSenList != null) {
			for (CeSentence thisSen : pSenList) {
				addObjectValueTo(pJa, generateMinimalJson(thisSen, pPriOrSecInd));
			}
		}
	}

	public void generateFullListUsing(Collection<CeSentence> pSenList, String pPriOrSecInd, CeStoreJsonArray pJa) {
		if (pSenList != null) {
			for (CeSentence thisSen : pSenList) {
				addObjectValueTo(pJa, generateFullJson(thisSen, pPriOrSecInd));
			}
		}
	}

	public CeStoreJsonObject generateSummaryJson(CeSentence pSen, String pPriOrSecInd) {
		CeStoreJsonObject jObj = generateSummaryNormalJson(pSen, pPriOrSecInd);

		if (pSen instanceof CeSentenceQualified) {
			CeSentenceQualified qSen = (CeSentenceQualified)pSen;
			generateQualifiedJsonInto(jObj, qSen);
		}
		
		return jObj;
	}

	public CeStoreJsonObject generateMinimalJson(CeSentence pSen, String pPriOrSecInd) {
		//TODO: Replace this with the actual minimal version
		return generateSummaryJson(pSen, pPriOrSecInd);
	}

	public CeStoreJsonObject generateFullJson(CeSentence pSen, String pPriOrSecInd) {
		CeStoreJsonObject jObj = generateFullNormalJson(pSen, pPriOrSecInd);

		if (pSen instanceof CeSentenceQualified) {
			CeSentenceQualified qSen = (CeSentenceQualified)pSen;
			generateQualifiedJsonInto(jObj, qSen);
		}
		
		return jObj;
	}

	CeStoreJsonObject generateSummaryNormalJson(CeSentence pSen, String pPriOrSecInd) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, KEY_TYPE, TYPE_SEN);
		putStringValueIn(jObj, KEY_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, KEY_ID, pSen.formattedId());
		putLongValueIn(jObj, KEY_CREATED, pSen.getCreationDate());
		putStringValueIn(jObj, KEY_SEN_TYPE, pSen.formattedSentenceType());
		putStringValueIn(jObj, KEY_SEN_VAL, pSen.formattedValidity());
		putStringValueIn(jObj, KEY_SEN_TEXT, pSen.getCeText(this.ac));
		putArrayValueIn(jObj, KEY_SEN_STRUCTUREDTEXT, generateStructuredCeTextListFor(pSen));
		putStringValueIn(jObj, KEY_SEN_SRC_ID, pSen.getSource().getId());

		if (pPriOrSecInd != null) {
			putStringValueIn(jObj, KEY_SEN_PRIORSEC, pPriOrSecInd);
		}
		
		CeStoreJsonArray jaQs = generateQualifiedSentencesJsonFor(pSen);
		if (!jaQs.isEmpty()) {
			putArrayValueIn(jObj, KEY_SEN_QUALSENS, jaQs);
		}
		
		CeStoreJsonArray jaR = generateRationaleJsonFor(pSen);
		if (!jaR.isEmpty()) {
			putArrayValueIn(jObj, KEY_SEN_RATSTEPS, jaR);			
		}
	
		if (pSen.isQualified()) {
			CeSentenceQualified qSen = (CeSentenceQualified)pSen;
			CeSentence iSen = qSen.getInnerSentence();
			
			if (iSen != null) {
				putStringValueIn(jObj, KEY_SENINNER_ID, iSen.formattedId());
			}
		}
		
		return jObj;		
	}

	private CeStoreJsonObject generateFullNormalJson(CeSentence pSen, String pPriOrSecInd) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, KEY_TYPE, TYPE_SEN);
		putStringValueIn(jObj, KEY_STYLE, STYLE_FULL);
		putStringValueIn(jObj, KEY_ID, pSen.formattedId());
		putLongValueIn(jObj, KEY_CREATED, pSen.getCreationDate());
		putStringValueIn(jObj, KEY_SEN_TYPE, pSen.formattedSentenceType());
		putStringValueIn(jObj, KEY_SEN_VAL, pSen.formattedValidity());
		putStringValueIn(jObj, KEY_SEN_TEXT, pSen.getCeText(this.ac));
		putArrayValueIn(jObj, KEY_SEN_STRUCTUREDTEXT, generateStructuredCeTextListFor(pSen));
		putObjectValueIn(jObj, KEY_SEN_SRC, CeWebSource.generateSummaryDetailsJsonFor(pSen.getSource()));

		if (pPriOrSecInd != null) {
			putStringValueIn(jObj, KEY_SEN_PRIORSEC, pPriOrSecInd);
		}

		CeStoreJsonArray jaQs = generateQualifiedSentencesJsonFor(pSen);
		if (!jaQs.isEmpty()) {
			putArrayValueIn(jObj, KEY_SEN_QUALSENS, jaQs);
		}

		CeStoreJsonArray jaR = generateRationaleJsonFor(pSen);
		if (!jaR.isEmpty()) {
			putArrayValueIn(jObj, KEY_SEN_RATSTEPS, jaR);
		}

		if (pSen.isQualified()) {
			CeSentenceQualified qSen = (CeSentenceQualified)pSen;
			CeSentence iSen = qSen.getInnerSentence();

			if (iSen != null) {
				putObjectValueIn(jObj, KEY_SENINNER, generateSummaryJson(iSen, ""));
			}
		}

		return jObj;
	}

	private CeStoreJsonArray generateQualifiedSentencesJsonFor(CeSentence pSen) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		for (CeSentence qSen : pSen.getQualifiedSentences()) {
			addObjectValueTo(jArr, generateFullJson(qSen, null));
		}
		
		return jArr;
	}

	private static CeStoreJsonArray generateStructuredCeTextListFor(CeSentence pSen) {
		CeStoreJsonArray result = new CeStoreJsonArray();

		//TODO: Improve this by changing the core generated format to a more processable form
		String propType = "";
		String textKey = "";
		String propDomain = "";
		String propRange = "";
		String annoName = "";
		String annoVal = "";
		String lastFrag = "";
		String concatNormal = "";
		boolean gotPropDetails = false;
		boolean gotAnnoDetails = false;
		boolean isOpen = true;
		int i = 1;

		for (String thisFrag : pSen.getStructuredCeTextList()) {
			if ((propType.equals("property")) && !gotPropDetails) {
				//Special case for property
				gotPropDetails = true;
				propType = "";
				String parts[] = extractDomainAndRangeFrom(thisFrag);

				if (parts.length == 2) {
					propDomain = parts[0];
					propRange = parts[1];
				} else {
					propDomain = "(unknown)";
					propRange = "(unknown)";
				}
			} else if (propType.equals("annotation_name")) {
				annoName = thisFrag;
				propType = "";
			} else if (propType.equals("annotation_value")) {
				annoVal = thisFrag;
				propType = "";
				gotAnnoDetails = true;
			} else {
				//Normal (non-property) cases
				if (thisFrag.equals("{Concept}:")) {
					propType = "concept";
					textKey = "concept_name";
				} else if (thisFrag.equals("{Property}:")) {
					propType = "property";
					textKey = "";
				} else if (thisFrag.equals("{Connector}:")) {
					propType = "connector";
					textKey = "";
				} else if (thisFrag.equals("{AnnoName}:")) {
					propType = "annotation_name";
					textKey = "";
				} else if (thisFrag.equals("{AnnoVal}:")) {
					propType = "annotation_value";
					textKey = "";
				} else if (thisFrag.equals("{Quote}:")) {
					if (isOpen) {
						propType = "open quote";
					} else {
						propType = "close quote";
					}
					isOpen = !isOpen;
					textKey = "text";
				} else if (thisFrag.equals("{Instance}:")) {
					propType = "instance";
					textKey = "instance_name";
				} else if (thisFrag.equals("{Name}:")) {
					propType = "pattern_name";
					textKey = "pattern_name";
				} else if (thisFrag.equals("{RqStart}:")) {
					propType = "pattern_start";
					textKey = "text";
				} else if (thisFrag.equals("{Because}:")) {
					propType = "rationale_start";
					textKey = "text";
				} else {
					if (gotPropDetails) {
						//Special property case
						i = handleProperty(concatNormal, thisFrag, propRange, propDomain, i, result);

						gotPropDetails = false;
						propRange = "";
						propDomain = "";
						concatNormal = "";
					} else if (gotAnnoDetails) {
						//Special property case
						i = handleAnnotation(concatNormal, annoName, annoVal, i, result);

						gotAnnoDetails = false;
						annoName = "";
						annoVal = "";
						concatNormal = "";
					} else {
						//Normal case
						if (propType.isEmpty()) {
							propType = "normal";
							textKey = "text";
						}

						//Special case for "." (terminator)
						if ((propType.equals("normal")) && (thisFrag.equals("."))) {
							propType = "terminator";
						}

						if (propType.equals("normal")) {
//						if ((propType.equals("normal")) || (propType.equals("connector"))) {
							if (!concatNormal.isEmpty()) {
//								if (!thisFrag.equals(".")) {
									concatNormal += " ";
//								}
							}
							concatNormal += thisFrag;
						} else {
//							if (!concatNormal.isEmpty()) {
								i = handleConcatenatedNormal(concatNormal, thisFrag, propType, i, result);
								concatNormal = "";
//							}

//							if ((!propType.equals("normal")) && (!propType.equals("connector"))) {
//							if (!propType.equals("normal")) {
								if (propType.equals("pattern_start")) {
									//Output a separator before the next fragment
									CeStoreJsonObject thisObj = new CeStoreJsonObject();
									thisObj.put(KEY_FRAGTYPE, "separator_patternname");
									thisObj.put(KEY_IDX, i++);
									result.add(thisObj);
									propType = "normal";
									if (!concatNormal.isEmpty()) {
										if (!thisFrag.equals(".")) {
											concatNormal += " ";
										}
									}
									concatNormal += thisFrag;
								} else {
									CeStoreJsonObject thisObj = new CeStoreJsonObject();

//									if (propType.equals("connector")) {
//										textKey = "text";
//										thisObj.put(KEY_FRAGTYPE, "separator");
//									} else {
//										thisObj.put(KEY_FRAGTYPE, propType);
//									}
//									
//									//Now output the required value
//									thisObj.put(KEY_IDX, i++);
//									thisObj.put(textKey, thisFrag);	
//									result.add(thisObj);

									if (!propType.equals("connector")) {
										//Now output the required value
										thisObj.put(KEY_FRAGTYPE, propType);
										thisObj.put(KEY_IDX, i++);
										thisObj.put(textKey, thisFrag);	
										result.add(thisObj);
									}
								}
//							}
						}
					}
					
					propType = "";
				}
				
				lastFrag = thisFrag;
			}
		}
		
		//Final check for last cases
		if (gotPropDetails) {
			//Special property case
			i = handleProperty(concatNormal, lastFrag, propRange, propDomain, i, result);
		} else if (gotAnnoDetails) {
			//Special property case
			i = handleAnnotation(concatNormal, annoName, annoVal, i, result);
		} else if (!concatNormal.isEmpty()) {
			i = handleConcatenatedNormal(concatNormal, "", "normal", i, result);
		}
		
		return result;
	}
	
	private static void generateQualifiedJsonInto(CeStoreJsonObject pJo, CeSentenceQualified pQualSen) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, KEY_SENQUAL_TS, pQualSen.getQualifiedTimestamp());
		putStringValueIn(jObj, KEY_SENQUAL_AN, pQualSen.getQualifiedAuthorName());
		putStringValueIn(jObj, KEY_SENQUAL_AC, pQualSen.getQualifiedAuthorConceptName());
		putStringValueIn(jObj, KEY_SENQUAL_QN, pQualSen.getGeneralQualificationName());
		putStringValueIn(jObj, KEY_SENQUAL_QC, pQualSen.getGeneralQualificationConceptName());
		putStringValueIn(jObj, KEY_SENQUAL_CN, pQualSen.getQualifiedContextName());
		putStringValueIn(jObj, KEY_SENQUAL_CC, pQualSen.getQualifiedContextConceptName());
		putStringValueIn(jObj, KEY_SENQUAL_ST, pQualSen.formattedQualifiedStatementType());
		putStringValueIn(jObj, KEY_SENQUAL_TF, pQualSen.formattedQualifiedTimeframe());
		putStringValueIn(jObj, KEY_SENQUAL_TV, pQualSen.formattedQualifiedTruthValue());
		
		putObjectValueIn(pJo, KEY_SENQUAL, jObj);
	}

	private CeStoreJsonArray generateRationaleJsonFor(CeSentence pSen) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		if (pSen.getRationaleReasoningStep() != null) {
			CeWebRationaleReasoningStep webRrs = new CeWebRationaleReasoningStep(this.ac);
			jArr.add(webRrs.generateFor(pSen.getRationaleReasoningStep()));
		}
		
		return jArr;
	}

	private static String[] extractDomainAndRangeFrom(String pText) {
		String trimmedText = pText.replace("[", "").replace("]", "").replace(":", "");
		
		return trimmedText.split(",");
	}

	private static int handleProperty(String pConcatNormal, String pPropName, String pPropRange, String pPropDomain, int pIndex, CeStoreJsonArray pResult) {
		int myIndex = pIndex;

		if (!pConcatNormal.isEmpty()) {
			myIndex = handleConcatenatedNormal(pConcatNormal, "", "normal", myIndex++, pResult);
		}

		CeStoreJsonObject thisObj = new CeStoreJsonObject();

		thisObj.put(KEY_FRAGTYPE, "property");
		thisObj.put(KEY_IDX, myIndex++);
		thisObj.put("property_name", pPropName);
		thisObj.put("property_domain", pPropDomain);
		thisObj.put("property_range", pPropRange);

		pResult.add(thisObj);

		return myIndex;
	}

	private static int handleAnnotation(String pConcatNormal, String pAnnoName, String pAnnoVal, int pIndex, CeStoreJsonArray pResult) {
		int myIndex = pIndex;
		
		if (!pConcatNormal.isEmpty()) {
			myIndex = handleConcatenatedNormal(pConcatNormal, "", "normal", myIndex++, pResult);
		}

		CeStoreJsonObject thisObj = new CeStoreJsonObject();
		
		thisObj.put(KEY_FRAGTYPE, "annotation");
		thisObj.put(KEY_IDX, myIndex++);
		thisObj.put("annotation_name", pAnnoName);
		thisObj.put("annotation_value", pAnnoVal);
		
		if (pAnnoName.equals("Model:")) {
			thisObj.put("annotation_value_type", "conceptual model");
		}
		
		pResult.add(thisObj);
		
		return myIndex;
	}
	
	private static int handleConcatenatedNormal(String pOutputText, String pThisText, String pPropType, int pIndex, CeStoreJsonArray pResult) {
		int myIndex = pIndex;

		String tgtVal = pOutputText;

		if (pPropType.equals("connector")) {
			//This is a connector so concatenate to the previous value
			if (!tgtVal.isEmpty()) {
				if (!pThisText.equals(".")) {
					tgtVal += " ";
				}
			}
			tgtVal += pThisText;

			CeStoreJsonObject thisObj = new CeStoreJsonObject();
			thisObj.put(KEY_FRAGTYPE, "normal");
			thisObj.put(KEY_IDX, myIndex++);
			thisObj.put("text", tgtVal);	
			pResult.add(thisObj);

			//Also output a separator
			thisObj = new CeStoreJsonObject();
			thisObj.put(KEY_FRAGTYPE, "separator");
			thisObj.put(KEY_IDX, myIndex++);
			pResult.add(thisObj);
		} else {
			//This is a different type so output separately
			if (!pOutputText.isEmpty()) {
				//First add the previously concatenated values
				CeStoreJsonObject thisObj = new CeStoreJsonObject();
				thisObj.put(KEY_FRAGTYPE, "normal");
				thisObj.put(KEY_IDX, myIndex++);
				thisObj.put("text", pOutputText);	
				pResult.add(thisObj);
			}
		}		

		return myIndex;
	}

}