package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_BLANK;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CONCEPTUALISE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CONCEPTUALIZE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DEFINE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THERE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NO;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_FOR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_IF;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_PERFORM;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_OPENSQBR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CLOSESQBR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COLON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;

public abstract class BuilderSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final int SENTYPE_UNCATEGORISED = 0;
	public static final int SENTYPE_FACT = 10;
	public static final int SENTYPE_MODEL = 20;
	public static final int SENTYPE_RULE = 30;
	public static final int SENTYPE_QUERY = 40;
	public static final int SENTYPE_ANNO = 50;
	public static final int SENTYPE_CMD = 60;

	private static final String SENNAME_FACT = "Fact";
	private static final String SENNAME_MODEL = "Model";
	private static final String SENNAME_QUERY = "Query";
	private static final String SENNAME_RULE = "Rule";
	private static final String SENNAME_ANNO = "Annotation";
	private static final String SENNAME_CMD = "Command";
	private static final String SENTYPE_VALID = "Valid";
	private static final String SENTYPE_INVALID = "Invalid";

	public static final int SENVAL_VALID = 1;
	public static final int SENVAL_INVALID = 2;

	public static final String TEMP_PROP = "***TEMP_PROP***";	//TODO: Refactor this out

	protected String sentenceText = null;
	protected ArrayList<String> rawTokens = null;
	protected ArrayList<String> structuredCeTokens = null;
	public ArrayList<String> errors = null;
	protected CeSentence convertedSentence = null;

	private String firstToken = null;
	private String firstNonNameToken = null;

	protected int type = SENTYPE_UNCATEGORISED;
	protected int validity = -1;

	protected CeConcept targetConceptNormal = null;
	protected CeConcept targetConceptNegated = null;

	public abstract boolean hasRationale();
	protected abstract void propogateRationaleValues();

	public BuilderSentence(String pSenText) {
		this.sentenceText = pSenText;
		this.rawTokens = new ArrayList<String>();
		this.structuredCeTokens = new ArrayList<String>();
		this.errors = new ArrayList<String>();
		this.firstToken = "";
		this.firstNonNameToken = "";
	}

	public static BuilderSentence createForSentenceText(ActionContext pAc, String pSentenceText, ArrayList<String> pTokens) {
		BuilderSentence bs = null;

		if ((pSentenceText != null) && (!pSentenceText.isEmpty())) {
			if ((pTokens != null) && (!pTokens.isEmpty())) {
				String firstWord = pTokens.get(0);
				if (firstWord.equals(TOKEN_CONCEPTUALISE) || firstWord.equals(TOKEN_CONCEPTUALIZE) || firstWord.equals(TOKEN_DEFINE)) {
					bs = new BuilderSentenceModel(pSentenceText);
				} else if ((firstWord.equals(TOKEN_THE)) || (firstWord.equals(TOKEN_THERE)) || (firstWord.equals(TOKEN_NO))) {
					bs = new BuilderSentenceFact(pSentenceText);
				} else if (firstWord.equals(TOKEN_PERFORM)) {
					bs = new BuilderSentenceCommand(pSentenceText);
				} else if ((firstWord.equals(TOKEN_IF)) || (firstWord.equals(TOKEN_FOR)) || (firstWord.equals(TOKEN_OPENSQBR))) {
					bs = new BuilderSentenceRuleOrQuery(pSentenceText);
				} else if (firstWord.endsWith(TOKEN_COLON)) {
					bs = new BuilderSentenceAnnotation(pSentenceText);
				} else {
					if (!pAc.isValidatingOnly()) {
						reportError("Unable to detect sentence type when creating sentence builder for sentence:" + pSentenceText, pAc);
					}
				}
			} else {
				if (!pAc.isValidatingOnly()) {
					reportError("Unable to create sentence builder due to empty token list", pAc);
				}
			}
		} else {
			if (!pAc.isValidatingOnly()) {
				reportError("Unable to create sentence builder for empty sentence", pAc);	
			}
		}

		if (bs != null) {
			bs.processTokens(pTokens);
		}

		return bs;
	}

	public static String formattedSentenceType(int pType) {
		String result = null;

		switch (pType) {
		case SENTYPE_FACT:
			result = SENNAME_FACT;
			break;
		case SENTYPE_MODEL:
			result = SENNAME_MODEL;
			break;
		case SENTYPE_QUERY:
			result = SENNAME_QUERY;
			break;
		case SENTYPE_RULE:
			result = SENNAME_RULE;
			break;
		case SENTYPE_ANNO:
			result = SENNAME_ANNO;
			break;
		case SENTYPE_CMD:
			result = SENNAME_CMD;
			break;
		default:
			result = "UNKNOWN TYPE (" + Integer.toString(pType) + ")";
			break;
		}

		return result;
	}

	public static String formattedSentenceValidity(int pValidity) {
		String result = "";

		switch (pValidity) {
			case SENVAL_VALID:
				result = SENTYPE_VALID;
				break;
			case SENVAL_INVALID:
				result = SENTYPE_INVALID;
				break;
			default:
				result = "UNKNOWN VALIDITY (" + Integer.toString(pValidity)+ ")";
				break;
		}

		return result;
	}

	public String getSentenceText() {
		return this.sentenceText;
	}

	public ArrayList<String> getRawTokens() {
		return this.rawTokens;
	}

	public ArrayList<String> getStructuredCeTokens() {
		return this.structuredCeTokens;
	}

	public CeConcept getTargetConcept() {
		CeConcept result = null;

		//TODO: Need to add a check/error that these are both not null

		if (this.targetConceptNormal != null) {
			result = this.targetConceptNormal;
		} else {
			result = this.targetConceptNegated;
		}

		return result;
	}

	public CeConcept getTargetConceptNormal() {
		return this.targetConceptNormal;
	}

	public CeConcept getTargetConceptNegated() {
		return this.targetConceptNegated;
	}

	public boolean isTargetConceptNegated() {
		return this.targetConceptNegated != null;
	}

	public void setRawTokens(ArrayList<String> pTokens) {
		this.rawTokens = pTokens;
	}

	public boolean hasTargetConcept() {
		return hasTargetConceptNormal() || hasTargetConceptNegated();
	}

	public boolean hasTargetConceptNormal() {
		return (this.targetConceptNormal != null);
	}

	public boolean hasTargetConceptNegated() {
		return (this.targetConceptNegated != null);
	}

	public void setTargetConceptNormal(CeConcept pConcept) {
		this.targetConceptNormal = pConcept;
	}

	public void setTargetConceptNegated(CeConcept pConcept) {
		this.targetConceptNegated = pConcept;
	}

	public void addFinalToken(String pStructuredCeLabel, String pStructuredCeDetails, String pToken) {
		if (pToken != null) {
			if (!pToken.trim().isEmpty()) {
				if ((pStructuredCeLabel != null) && (!pStructuredCeLabel.isEmpty())) {
					this.structuredCeTokens.add(pStructuredCeLabel);
				}

				if ((pStructuredCeDetails != null) && (!pStructuredCeDetails.isEmpty())) {
					this.structuredCeTokens.add(pStructuredCeDetails);
				}

				this.structuredCeTokens.add(pToken);
			}
		}
	}

	public void replacePropertyToken(String pRepText) {
		//TODO: Come up with a better way... this is very bad performance-wise
		int tokPos = this.structuredCeTokens.indexOf(TEMP_PROP);

		if (tokPos != -1) {
			this.structuredCeTokens.remove(tokPos);
			this.structuredCeTokens.add(tokPos, pRepText);
		}
	}

	public void addSceTokens(ArrayList<String> pSceTokens) {
		this.structuredCeTokens.addAll(pSceTokens);
	}

	public void removeLastFinalToken() {
		//Remove the last structured CE token
		this.structuredCeTokens.remove(this.structuredCeTokens.size() - 1);
	}

	public CeSentence getConvertedSentence() {
		return this.convertedSentence;
	}

	public int categoriseSentence() {
		if (this.rawTokens.isEmpty()) {
			this.type = SENTYPE_UNCATEGORISED;
		} else {
			if (this.firstToken.equals(TOKEN_CONCEPTUALISE) || this.firstToken.equals(TOKEN_CONCEPTUALIZE) || this.firstToken.equals(TOKEN_DEFINE)) {
				this.type = SENTYPE_MODEL;
			} else if ((this.firstToken.equals(TOKEN_THE)) || (this.firstToken.equals(TOKEN_THERE)) || (this.firstToken.equals(TOKEN_NO))) {
				this.type = SENTYPE_FACT;
			} else if (this.firstToken.equals(TOKEN_FOR)) {
				this.type = SENTYPE_QUERY;
			} else if (this.firstToken.equals(TOKEN_IF)) {
				this.type = SENTYPE_RULE;
			} else if (this.firstToken.equals(TOKEN_OPENSQBR)) {
				if (this.firstNonNameToken.equals(TOKEN_FOR)) {
					this.type = SENTYPE_QUERY;
				} else if (this.firstNonNameToken.equals(TOKEN_IF)) {
					this.type = SENTYPE_RULE;
				} else {
					this.type = SENTYPE_UNCATEGORISED;
				}
			} else if (this.firstToken.endsWith(TOKEN_COLON)) {
				this.type = SENTYPE_ANNO;
			} else if (this.firstToken.equals(TOKEN_PERFORM)) {
				this.type = SENTYPE_CMD;
			} else {
				this.type = SENTYPE_UNCATEGORISED;
			}
		}

		return this.type;
	}

	public boolean isFactSentence() {
		return (this.type == SENTYPE_FACT);
	}

	public boolean isModelSentence() {
		return (this.type == SENTYPE_MODEL);
	}

	public boolean isQuerySentence() {
		return (this.type == SENTYPE_QUERY);
	}

	public boolean isRuleSentence() {
		return (this.type == SENTYPE_RULE);
	}

	public boolean isAnnotationSentence() {
		return (this.type == SENTYPE_ANNO);
	}

	public boolean isCommandSentence() {
		return (this.type == SENTYPE_CMD);
	}

	public boolean isUncategorised() {
		return (this.type == SENTYPE_UNCATEGORISED);
	}

	public boolean isValid() {
		return (this.validity == SENVAL_VALID);
	}

	@SuppressWarnings("static-method")
	public boolean isRationaleProcessing() {
		return false;
	}

	public void markAsRationaleProcessing() {
		//Do nothing - must be implemented by subclass if rationale is possible
	}

	public void hasError(ActionContext pAc, String pErrorText) {
		this.errors.add(pErrorText);
		this.validity = SENVAL_INVALID;

		if (!pAc.isValidatingOnly()) {
			//Report the error
			reportError(pErrorText + ", for sentence: " + this.sentenceText, pAc);
		}
	}

	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	public void validationComplete() {
		// Validation is complete, so if there are no errors reported then the sentence is valid
		if (this.errors.isEmpty()) {
			this.validity = SENVAL_VALID;
		} else {
			this.validity = SENVAL_INVALID;
		}
	}

	public CeSentence convertToSentence(ActionContext pAc) {
		CeSource lastSrc = pAc.getLastSource();

		if (lastSrc == null) {
			lastSrc = CeSource.createNewInternalSource(pAc, "Error", null);
		}

		if (this.convertedSentence == null) {
			if (!isAnnotationSentence()) {
				//Don't add the terminating dot for sentences with rationale - it is added later, after the rationale clauses are inserted
				if (!hasRationale()) {
					addFinalToken(TOKEN_BLANK, TOKEN_BLANK, TOKEN_DOT);	//Terminate the sentence with a full stop
				}
			}
			//this.convertedSentence = CeSentence.createNewSentence(pAc, this.type, this.validity, getSentenceText(), getStructuredCeTokens(), lastSrc);
			this.convertedSentence = CeSentence.createNewSentence(pAc, this.type, this.validity, getSentenceText(), getStructuredCeTokens(), lastSrc, this.targetConceptNormal);
		}

		propogateRationaleValues();

		return this.convertedSentence;
	}

	private void processTokens(ArrayList<String> pTokens) {
		//Store the raw tokens
		setRawTokens(pTokens);

		//Store the first token
		if (pTokens.size() > 0) {
			this.firstToken = pTokens.get(0);
		}

		//Store the first non-name token
		boolean foundEndMarker = false;

		break_position:
		for (String thisToken : this.rawTokens) {
			if (foundEndMarker) {
				this.firstNonNameToken = thisToken;
				break break_position;
			}

			if (thisToken.equals(TOKEN_CLOSESQBR)) {
				foundEndMarker = true;
			}
		}
	}

	public void addAnnotation(ActionContext pAc, CeSentence pAnnoSen) {
		if (getConvertedSentence() != null) {
			getConvertedSentence().addAnnotation(pAnnoSen);
		} else {
			reportWarning("Unable to add annotation due to null converted sentence", pAc);
		}
	}

	@Override
	public String toString() {
		String errorText = "";

		if (hasErrors()) {
			errorText = ", has " + Integer.toString(this.errors.size()) + " errors";
		}

		return "BuilderSentence (" + BuilderSentence.formattedSentenceType(this.type) + ", " + BuilderSentence.formattedSentenceValidity(this.validity) + errorText + "): " + this.sentenceText;
	}

}
