package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.encodeForCe;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public class CeSentence implements Comparable<CeSentence> {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public static String PREFIX_SEN = "sen_";
	private static final String LABEL_PREFIX = "{";
	private static final String LABEL_SUFFIX = "}:";

	public static final String PROPDEF_PREFIX = "[";
	public static final String PROPDEF_SUFFIX = "]:";

	private static AtomicLong sentenceIdVal = new AtomicLong(0);

	private int id = -1;
	private long creationDate = 0;
	private int sentenceType = -1;
	private int validity = -1;
	protected String ceText = null;
	private String[] structuredCeTextList = new String[0];
	private CeSource source = null;
	private String rationaleText = null;
	private String rationaleRuleName = null;

	private CeConcept targetConcept = null;

	private CeSentence[] annotationSentences = new CeSentence[0];
	private CeSentenceQualified[] qualifiedSentences = new CeSentenceQualified[0];
	private CeRationaleReasoningStep rationaleReasoningStep = null;

	private CeSentence() {
		//This is private to ensure that new instances can only be created via the various static methods
		this.id = nextSentenceId();
		this.creationDate = timestampNow();
	}

	public static void resetCounter() {
		sentenceIdVal = new AtomicLong(0);
	}

	protected CeSentence(ActionContext pAc, CeSource pSource, int pType, int pVal) {
		this.id = nextSentenceId();
		this.creationDate = timestampNow();
		this.sentenceType = pType;
		this.validity = pVal;

		this.source = pSource;
		if (pAc.getCeConfig().isSavingCeSentences()) {
			if (isAnnotationSentence()) {
				pSource.addAnnotationSentence(this);
			} else {
				pSource.addPrimarySentence(this);
			}
		}
	}

	private static int nextSentenceId() {
		return (int)sentenceIdVal.incrementAndGet();
	}

	/**
	 * SL - added to support events
	 *      TODO - work out how to get property and instance details in
	 *      
	 * @param pAc
	 * @param pType
	 * @param pVal
	 * @param pSenText
	 * @param pCeList
	 * @param pSource
	 * @param targetConcept
	 * @return
	 */
	public static CeSentence createNewSentence(ActionContext pAc, int pType, int pVal, String pSenText, ArrayList<String> pCeList, CeSource pSource, CeConcept targetConcept) {
		CeSentence sentence = createNewSentence(pAc, pType, pVal, pSenText, pCeList, pSource);
		sentence.targetConcept = targetConcept;
		return sentence;
	}

	public static CeSentence createNewSentence(ActionContext pAc, int pType, int pVal, String pSenText, ArrayList<String> pCeList, CeSource pSource) {
		CeSentence newSen = new CeSentence(pAc, pSource, pType, pVal);

		newSen.createStructuredCeTextFrom(pCeList);

		if (pAc.getCeConfig().cacheCeText()) {
			newSen.ceText = pAc.getModelBuilder().getCachedStringValueLevel2(pSenText);
		}

		return newSen;
	}

	public static CeSentence createNewRuleOrQuerySentenceWithoutSaving(ActionContext pAc, int pType, int pVal, String pSenText) {
		CeSentence newSen = new CeSentence();

		//Used for rule/query execution... the sentence must be parsed but should not be saved
		
		newSen.sentenceType = pType;
		newSen.validity = pVal;
		newSen.structuredCeTextList = new String[0];		//This is not needed since the sentence will not be saved
		
		if (pAc.getCeConfig().cacheCeText()) {
			newSen.ceText = pAc.getModelBuilder().getCachedStringValueLevel2(pSenText);
		}

		return newSen;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String formattedId() {
		return PREFIX_SEN + Integer.toString(this.id);
	}

	public int getSentenceType() {
		return this.sentenceType;
	}
	
	public boolean isFactSentence() {
		return isFactSentenceNormal() || isFactSentenceQualified();
	}

	public boolean isFactSentenceNormal() {
		return (this.sentenceType == BuilderSentence.SENTYPE_FACT_NORMAL);
	}
	
	public boolean isFactSentenceQualified() {
		return (this.sentenceType == BuilderSentence.SENTYPE_FACT_QUALIFIED);
	}
	
	public boolean isModelSentence() {
		return (this.sentenceType == BuilderSentence.SENTYPE_MODEL);
	}

	public boolean isRuleSentence() {
		return (this.sentenceType == BuilderSentence.SENTYPE_RULE);
	}

	public boolean isQuerySentence() {
		return (this.sentenceType == BuilderSentence.SENTYPE_QUERY);
	}

	public boolean isPatternSentence() {
		return isRuleSentence() || isQuerySentence();
	}

	public boolean isAnnotationSentence() {
		return (this.sentenceType == BuilderSentence.SENTYPE_ANNO);
	}
	
	public boolean isCommandSentence() {
		return (this.sentenceType == BuilderSentence.SENTYPE_CMD);
	}

	public String formattedSentenceType() {
		return BuilderSentence.formattedSentenceType(this.sentenceType);		
	}
	
	public long getCreationDate() {
		return this.creationDate;
	}

	public int getValidity() {
		return this.validity;
	}

	public boolean isValid() {
		return (this.validity == BuilderSentence.SENVAL_VALID);
	}

	public String formattedValidity() {
		return BuilderSentence.formattedSentenceValidity(this.validity);
	}

	public String getCeText(ActionContext pAc) {
		String result = null;
		
		if (pAc.getCeConfig().cacheCeText()) {
			result = this.ceText;
		} else {
			result = calculateCeText();
		}
		
		return result;
	}

	public String getActualCeTextWithTokenSubstitutions() {
		//TODO: This should happen by default and not require a secondary method like this
		return calculateCeText();
	}

	public String getCeTextWithoutRationale(ActionContext pAc) {
		String result = getCeText(pAc);

		//TODO: Is there a cleaner way?
		String ratWords[] = result.split("\nbecause\n");
		
		if (ratWords.length > 1) {
			result = ratWords[0] + ".";
		}

		return result;
	}

	private String calculateCeText() {
		String result = "";
		String sepChar = "";
		boolean lastLabelQuote = false;
		boolean quoteStart = false;
		
		for (String thisPart : this.structuredCeTextList) {
			if (!isLabelToken(thisPart)) {
				if (lastLabelQuote) {
					if (quoteStart) {
						//This is the starting quote so we need a proceeding space, but none after
						result += " " + thisPart;
						sepChar = "";
					} else {
						//This is an ending quote so we need no space before but one after
						result += thisPart;
						sepChar = " ";
					}
					lastLabelQuote = false;
				} else {
					//This is a normal token so just append it
					
					if (isDotToken(thisPart)) {
						sepChar = "";
					}
					
					if (tokenStartsWithQuote(thisPart)) {
						//Don't encode as this starts with a quote already (rationale fragments are not broken down)
						result += sepChar + thisPart;
					} else {
						//A normal token so make sure it is encoded
						result += sepChar + encodeForCe(thisPart);
					}
					
					sepChar = " ";
				}
			} else {
				if (isQuoteLabelToken(thisPart)) {
					lastLabelQuote = true;
					quoteStart = !quoteStart;
				} else {
					lastLabelQuote = false;
				}
			}
		}
		
		return result;
	}

	private static boolean tokenStartsWithQuote(String pToken) {
		return pToken.startsWith("'") || pToken.startsWith("\"");
	}
	
	public static boolean isLabelToken(String pToken) {
		return 
				(pToken.startsWith(LABEL_PREFIX) && pToken.endsWith(LABEL_SUFFIX)) ||
				(pToken.startsWith(PROPDEF_PREFIX) && pToken.endsWith(PROPDEF_SUFFIX));
	}
	
	private static boolean isQuoteLabelToken(String pToken) {
		return pToken.equals(TokenizerFactSentence.SCELABEL_QUOTE);
	}
	
	private static boolean isDotToken(String pToken) {
		return pToken.equals(BuilderSentence.TOKEN_DOT);
	}

	public String getCeTextWithoutFullStop(ActionContext pAc) {
		String result = getCeText(pAc);
		
		if ((result != null) && (!result.isEmpty())) {
			result = result.substring(0, (result.length() - 1));
		}
		
		return result;
	}

	public String[] getStructuredCeTextList() {
		return this.structuredCeTextList;
	}
	
	public String calculateCeTextWithoutRationale() {
		String result = null;

		if ((this.rationaleText == null) || (this.rationaleText.isEmpty())) {
			//There is no rationale - just return the ceText
			result = this.ceText;
		} else {
			//Rebuild the CE until the point where rationale starts and return only that
			StringBuilder sb = new StringBuilder();
			boolean nextTokenIsQuote = false;
			boolean inQuote = false;
			String spaceBefore = "";
			String spaceAfter = "";

			for (String thisToken : this.structuredCeTextList) {
				if (nextTokenIsQuote) {
					nextTokenIsQuote = false;
					inQuote = !inQuote;
					
					if (inQuote) {
						sb.append(" ");
						sb.append(thisToken);
						spaceBefore = "";
						spaceAfter = "";
					} else {
						sb.append(thisToken);
						spaceBefore = " ";
					}
				} else {
					if (thisToken.equals("{Because}:")) {
						//This token signifies the beginning of the rationale, so stop here
						sb.append(".");
						break;
					} else if (thisToken.equals("{Connector}:")) {
						//These indicate a new clause
						sb.append("\n  ");
					} else if (thisToken.equals("{Property}:")) {
						//Remember that an extra space is needed after the next word
						spaceAfter = " ";
					} else if (thisToken.equals("{Quote}:")) {
						//Special processing for quotes
						nextTokenIsQuote = true;
					} else if (thisToken.startsWith("{")) {
						//Anything else can be ignored and replaced with a space (unless in a quote)
						if (!inQuote) {
							sb.append(" ");
						}
					} else if (thisToken.startsWith("[")) {
						//Just ignore these - they define property domain and range
					} else {
						sb.append(spaceBefore);
						sb.append(thisToken);
						sb.append(spaceAfter);

						spaceBefore = "";
						spaceAfter = "";
					}
				}
			}

			result = sb.toString();
		}

		return result;
	}

	protected void createStructuredCeTextFrom(ArrayList<String> pCeTextList) {
		if (pCeTextList != null) {
			this.structuredCeTextList = new String[pCeTextList.size()];

			int ctr = 0;
			for (String thisToken : pCeTextList) {
				this.structuredCeTextList[ctr++] = thisToken;
			}
		}
	}
	
	public void addStructuredRationaleTokens(ArrayList<String> pCeTextList) {
		if (pCeTextList != null) {
			int existingSize = this.structuredCeTextList.length;
			ArrayList<String> existingTokens = null;
			
			if (existingSize > 0) {
				existingTokens = new ArrayList<String>(Arrays.asList(this.structuredCeTextList));
			} else {
				existingTokens = new ArrayList<String>();
			}
			
			existingTokens.addAll(pCeTextList);
			
			this.structuredCeTextList = new String[existingTokens.size()];

			int ctr = 0;
			for (String thisToken : existingTokens) {
				this.structuredCeTextList[ctr++] = thisToken;
			}
		}
	}

	public CeRationaleReasoningStep getRationaleReasoningStep() {
		return this.rationaleReasoningStep;
	}
	
	public void setRationaleReasoningStep(CeRationaleReasoningStep pRs) {
		this.rationaleReasoningStep = pRs;
	}
	
	public String getRationaleText() {
		return this.rationaleText;
	}
	
	public void setRationaleText(String pRationaleText) {
		this.rationaleText = pRationaleText;
	}
	
	public String getRationaleRuleName() {
		return this.rationaleRuleName;
	}
	
	public void setRationaleRuleName(String pRationaleRuleName) {
		this.rationaleRuleName = pRationaleRuleName;
	}
	
	public CeSource getSource() {
		return this.source;
	}
	
	public CeSentence[] getAnnotationSentences() {
		return this.annotationSentences;
	}
	
	public void addAnnotation(CeSentence pAnnoSen) {
		if (!hasAnnotationSentence(pAnnoSen)) {
			int currLen = 0;

			currLen = this.annotationSentences.length;
			CeSentence[] newArray = new CeSentence[currLen + 1];
			System.arraycopy(this.annotationSentences, 0, newArray, 0, currLen);
			this.annotationSentences = newArray;
	
			this.annotationSentences[currLen] = pAnnoSen;
		}		
	}
	
	public boolean hasAnnotationSentence(CeSentence pAnnoSen) {
		boolean result = false;
		
		for (CeSentence thisSen : this.annotationSentences) {
			if (!result) {
				if (thisSen == pAnnoSen) {
					result = true;
				}
			}
		}
		
		return result;
	}

	public boolean hasAnnotationSentences() {
		return (getAnnotationSentences().length > 0);
	}

	public CeSentenceQualified[] getQualifiedSentences() {
		return this.qualifiedSentences;
	}
	
	public void addQualifiedSentence(CeSentenceQualified pQualSen) {
		if (!hasQualifiedSentence(pQualSen)) {
			int currLen = 0;

			currLen = this.qualifiedSentences.length;
			CeSentenceQualified[] newArray = new CeSentenceQualified[currLen + 1];
			System.arraycopy(this.qualifiedSentences, 0, newArray, 0, currLen);
			this.qualifiedSentences = newArray;
	
			this.qualifiedSentences[currLen] = pQualSen;			
		}
	}
	
	public boolean hasQualifiedSentence(CeSentenceQualified pQualSen) {
		boolean result = false;
		
		for (CeSentenceQualified thisSen : this.qualifiedSentences) {
			if (!result) {
				if (thisSen == pQualSen) {
					result = true;
				}
			}
		}
		
		return result;
	}

	public boolean hasQualifiedSentences() {
		return (this.qualifiedSentences.length > 0);
	}
	
	public boolean hasRationale() {
		return (!this.rationaleText.isEmpty());
	}
	
	@Override
	public boolean equals(Object pObj) {
	  if (pObj==this)
	    return true;
	  if (!(pObj instanceof CeSentence))
	    return false;
		return pObj.hashCode() == hashCode();
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public int compareTo(CeSentence pOtherSen) {
		return this.id - pOtherSen.getId();
	}

	public String extractAnnotationText(ActionContext pAc) {
		//everything to the left of the first colon character should be removed to get the annotation text
		String result = "";
		int markerPos = getCeText(pAc).indexOf(":");
		
		if (markerPos > 0) {
			result = getCeText(pAc).substring(markerPos + 1, getCeText(pAc).length());
			result = result.trim();
		} else {
			result = getCeText(pAc);
		}
		
		return result;
	}
	
	@SuppressWarnings("static-method")
	public boolean isQualified() {
		return false;
	}
	
	public CeConcept getTargetConcept() {
		return this.targetConcept;
	}
	
	@Override
	public String toString() {
		String result = "";
		
		result = "CeSentence '" + getId() + "' (" + formattedValidity() + "," + formattedSentenceType() + ") : " + calculateCeText();
		
		return result;
	}
	
}