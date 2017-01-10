package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_SEN;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CREATED;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_FRAGTYPE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_IDX;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_PRIORSEC;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_RATSTEPS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_SRC;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_SRC_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_STRUCTUREDTEXT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_TEXT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_TYPE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_VAL;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STYLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_TYPE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ANNO_TOKEN_MODEL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SPACE;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_FULL;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_SUMMARY;

import java.util.Collection;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeSentence;

public class CeWebSentence extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public CeWebSentence(ActionContext pAc) {
		super(pAc);
	}

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

	public CeStoreJsonArray generateNormalisedListFrom(Collection<CeSentence> pSenList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		generateNormalisedListUsing(pSenList, null, jArr);

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

	public void generateNormalisedListUsing(Collection<CeSentence> pSenList, String pPriOrSecInd, CeStoreJsonArray pJa) {
		if (pSenList != null) {
			for (CeSentence thisSen : pSenList) {
				addObjectValueTo(pJa, generateNormalisedJson(thisSen, pPriOrSecInd));
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

		return jObj;
	}

	public CeStoreJsonObject generateMinimalSummaryJson(CeSentence pSen, String pPriOrSecInd) {
		//TODO: Implement minimal form
		CeStoreJsonObject jObj = generateSummaryNormalJson(pSen, pPriOrSecInd);

		return jObj;
	}

	public CeStoreJsonObject generateNormalisedSummaryJson(CeSentence pSen, String pPriOrSecInd) {
		//TODO: Implement normalised form
		CeStoreJsonObject jObj = generateSummaryNormalJson(pSen, pPriOrSecInd);

		return jObj;
	}

	public CeStoreJsonObject generateMinimalJson(CeSentence pSen, String pPriOrSecInd) {
		return generateMinimalSummaryJson(pSen, pPriOrSecInd);
	}

	public CeStoreJsonObject generateNormalisedJson(CeSentence pSen, String pPriOrSecInd) {
		return generateNormalisedSummaryJson(pSen, pPriOrSecInd);
	}

	public CeStoreJsonObject generateFullJson(CeSentence pSen, String pPriOrSecInd) {
		CeStoreJsonObject jObj = generateFullNormalJson(pSen, pPriOrSecInd);

		return jObj;
	}

	CeStoreJsonObject generateSummaryNormalJson(CeSentence pSen, String pPriOrSecInd) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_SEN);
		putStringValueIn(jObj, JSON_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, JSON_ID, pSen.formattedId());
		putLongValueIn(jObj, JSON_CREATED, pSen.getCreationDate());
		putStringValueIn(jObj, JSON_SEN_TYPE, pSen.formattedSentenceType());
		putStringValueIn(jObj, JSON_SEN_VAL, pSen.formattedValidity());
		putStringValueIn(jObj, JSON_SEN_TEXT, pSen.getCeText(this.ac));
		putArrayValueIn(jObj, JSON_SEN_STRUCTUREDTEXT, generateStructuredCeTextListFor(pSen));
		putStringValueIn(jObj, JSON_SEN_SRC_ID, pSen.getSource().getId());

		if (pPriOrSecInd != null) {
			putStringValueIn(jObj, JSON_SEN_PRIORSEC, pPriOrSecInd);
		}
		
		CeStoreJsonArray jaR = generateRationaleJsonFor(pSen);
		if (!jaR.isEmpty()) {
			putArrayValueIn(jObj, JSON_SEN_RATSTEPS, jaR);			
		}
	
		return jObj;
	}

	private CeStoreJsonObject generateFullNormalJson(CeSentence pSen, String pPriOrSecInd) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_SEN);
		putStringValueIn(jObj, JSON_STYLE, STYLE_FULL);
		putStringValueIn(jObj, JSON_ID, pSen.formattedId());
		putLongValueIn(jObj, JSON_CREATED, pSen.getCreationDate());
		putStringValueIn(jObj, JSON_SEN_TYPE, pSen.formattedSentenceType());
		putStringValueIn(jObj, JSON_SEN_VAL, pSen.formattedValidity());
		putStringValueIn(jObj, JSON_SEN_TEXT, pSen.getCeText(this.ac));
		putArrayValueIn(jObj, JSON_SEN_STRUCTUREDTEXT, generateStructuredCeTextListFor(pSen));
		putObjectValueIn(jObj, JSON_SEN_SRC, CeWebSource.generateSummaryDetailsJsonFor(pSen.getSource()));

		if (pPriOrSecInd != null) {
			putStringValueIn(jObj, JSON_SEN_PRIORSEC, pPriOrSecInd);
		}

		CeStoreJsonArray jaR = generateRationaleJsonFor(pSen);
		if (!jaR.isEmpty()) {
			putArrayValueIn(jObj, JSON_SEN_RATSTEPS, jaR);
		}

		return jObj;
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

						//Special case for '.' (terminator)
						if ((propType.equals("normal")) && (thisFrag.equals(TOKEN_DOT))) {
							propType = "terminator";
						}

						if (propType.equals("normal")) {
							if (!concatNormal.isEmpty()) {
								concatNormal += " ";
							}
							concatNormal += thisFrag;
						} else {
							i = handleConcatenatedNormal(concatNormal, thisFrag, propType, i, result);
							concatNormal = "";

							if (propType.equals("pattern_start")) {
								//Output a separator before the next fragment
								CeStoreJsonObject thisObj = new CeStoreJsonObject();
								thisObj.put(JSON_FRAGTYPE, "separator_patternname");
								thisObj.put(JSON_IDX, i++);
								result.add(thisObj);
								propType = "normal";
								if (!concatNormal.isEmpty()) {
									if (!thisFrag.equals(TOKEN_DOT)) {
										concatNormal += TOKEN_SPACE;
									}
								}
								concatNormal += thisFrag;
							} else {
								CeStoreJsonObject thisObj = new CeStoreJsonObject();

								if (!propType.equals("connector")) {
									//Now output the required value
									thisObj.put(JSON_FRAGTYPE, propType);
									thisObj.put(JSON_IDX, i++);
									thisObj.put(textKey, thisFrag);	
									result.add(thisObj);
								}
							}
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

		thisObj.put(JSON_FRAGTYPE, "property");
		thisObj.put(JSON_IDX, myIndex++);
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
		
		thisObj.put(JSON_FRAGTYPE, "annotation");
		thisObj.put(JSON_IDX, myIndex++);
		thisObj.put("annotation_name", pAnnoName);	//TODO: Abstract these
		thisObj.put("annotation_value", pAnnoVal);
		
		if (pAnnoName.equals(ANNO_TOKEN_MODEL)) {
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
				if (!pThisText.equals(TOKEN_DOT)) {
					tgtVal += " ";
				}
			}
			tgtVal += pThisText;

			CeStoreJsonObject thisObj = new CeStoreJsonObject();
			thisObj.put(JSON_FRAGTYPE, "normal");
			thisObj.put(JSON_IDX, myIndex++);
			thisObj.put("text", tgtVal);	
			pResult.add(thisObj);

			//Also output a separator
			thisObj = new CeStoreJsonObject();
			thisObj.put(JSON_FRAGTYPE, "separator");
			thisObj.put(JSON_IDX, myIndex++);
			pResult.add(thisObj);
		} else {
			//This is a different type so output separately
			if (!pOutputText.isEmpty()) {
				//First add the previously concatenated values
				CeStoreJsonObject thisObj = new CeStoreJsonObject();
				thisObj.put(JSON_FRAGTYPE, "normal");
				thisObj.put(JSON_IDX, myIndex++);
				thisObj.put("text", pOutputText);	
				pResult.add(thisObj);
			}
		}		

		return myIndex;
	}

}
