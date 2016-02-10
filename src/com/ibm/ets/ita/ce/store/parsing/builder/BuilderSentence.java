package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;

public abstract class BuilderSentence {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String TOKEN_BLANK = "";
	public static final String TOKEN_DOT = ".";

	private static final int SENTYPE_UNCATEGORISED = 0;
	public static final int SENTYPE_FACT_NORMAL = 10;
	public static final int SENTYPE_FACT_QUALIFIED = 11;
	public static final int SENTYPE_MODEL = 20;
	public static final int SENTYPE_RULE = 30;
	public static final int SENTYPE_QUERY = 40;
	public static final int SENTYPE_ANNO = 50;
	public static final int SENTYPE_CMD = 60;

	private static final String SENNAME_FACT_NORMAL = "Fact";
	private static final String SENNAME_FACT_QUALIFIED = "Fact (qualified)";
	private static final String SENNAME_MODEL = "Model";
	private static final String SENNAME_QUERY = "Query";
	private static final String SENNAME_RULE = "Rule";
	private static final String SENNAME_ANNO = "Annotation";
	private static final String SENNAME_CMD = "Command";

	public static final int SENVAL_VALID = 1;
	public static final int SENVAL_INVALID = 2;

	public static final String SENSOURCE_PRIMARY = "primary";
	public static final String SENSOURCE_SECONDARY = "secondary";

	private static final String MODEL_MARKER1 = "conceptualise";
	private static final String MODEL_MARKER2 = "conceptualize";
	private static final String MODEL_MARKER3 = "define";
	private static final String FACT_NORMAL_MARKER1 = "the";
	private static final String FACT_NORMAL_MARKER2 = "there";
	private static final String FACT_NORMAL_MARKER3 = "no";
	private static final String FACT_QUALIFIED_MARKER = "statement";
	private static final String FACT_TEMP_MARKER1 = "it";
	private static final String QUERY_MARKER = "for";
	private static final String RULE_MARKER = "if";
	private static final String NAME_STARTMARKER = "[";
	private static final String NAME_ENDMARKER = "]";
	private static final String CMD_MARKER = "perform";
	private static final String ANNO_ENDMARKER = ":";

	public static final String TEMP_PROP = "***TEMP_PROP***";

	protected String sentenceText = "";
	protected ArrayList<String> rawTokens = null;
	protected ArrayList<String> structuredCeTokens = null;
	public ArrayList<String> errors = null;
	protected CeSentence convertedSentence = null;

	private String firstToken = "";
	private String firstNonNameToken = "";

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
				if (firstWord.equals(MODEL_MARKER1) || firstWord.equals(MODEL_MARKER2) || firstWord.equals(MODEL_MARKER3)) {
					bs = new BuilderSentenceModel(pSentenceText);
				} else if (firstWord.equals(FACT_NORMAL_MARKER1)) {
					String secondWord = pTokens.get(1);
					if (secondWord.equals(FACT_QUALIFIED_MARKER)) {
						bs = new BuilderSentenceFactQualified(pSentenceText);
					} else {
						bs = new BuilderSentenceFactNormal(pSentenceText);
					}
				} else if (firstWord.equals(FACT_TEMP_MARKER1)) {
					//TODO: Remove this test when this temporary syntax "it is true|false that" is removed
					bs = new BuilderSentenceFactTemporary(pSentenceText);
				} else if ((firstWord.equals(FACT_NORMAL_MARKER2)) || (firstWord.equals(FACT_NORMAL_MARKER3))) {
					bs = new BuilderSentenceFactNormal(pSentenceText);
				} else if (firstWord.equals(FACT_QUALIFIED_MARKER)) {
					bs = new BuilderSentenceFactQualified(pSentenceText);
				} else if (firstWord.equals(CMD_MARKER)) {
					bs = new BuilderSentenceCommand(pSentenceText);
				} else if ((firstWord.equals(RULE_MARKER)) || (firstWord.equals(QUERY_MARKER)) || (firstWord.equals(NAME_STARTMARKER))) {
					bs = new BuilderSentenceRuleOrQuery(pSentenceText);
				} else if (firstWord.endsWith(ANNO_ENDMARKER)) {
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

	@SuppressWarnings("static-method")
	public boolean isQualified() {
		return false;
	}

	public static String formattedSentenceType(int pType) {
		String result = "";

		switch (pType) {
		case SENTYPE_FACT_NORMAL:
			result = SENNAME_FACT_NORMAL;
			break;
		case SENTYPE_FACT_QUALIFIED:
			result = SENNAME_FACT_QUALIFIED;
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
				result = "Valid";
				break;
			case SENVAL_INVALID:
				result = "Invalid";
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
		//DSB 27/10/2015 - Added null pointer test after fixing rationale generation bug
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
			if (this.firstToken.equals(MODEL_MARKER1) || this.firstToken.equals(MODEL_MARKER2) || this.firstToken.equals(MODEL_MARKER3)) {
				this.type = SENTYPE_MODEL;
			} else if (this.firstToken.equals(FACT_NORMAL_MARKER1)) {
				String secondToken = this.getRawTokens().get(1);
				if (secondToken.equals(FACT_QUALIFIED_MARKER)) {
					this.type = SENTYPE_FACT_QUALIFIED;
				} else {
					this.type = SENTYPE_FACT_NORMAL;
				}
			} else if (this.firstToken.equals(FACT_TEMP_MARKER1)) {
				//TODO: Remove this test when this temporary syntax "it is true|false that" is removed
				this.type = SENTYPE_FACT_NORMAL;
			} else if ((this.firstToken.equals(FACT_NORMAL_MARKER2)) || (this.firstToken.equals(FACT_NORMAL_MARKER3))) {
				this.type = SENTYPE_FACT_NORMAL;
			} else if (this.firstToken.equals(QUERY_MARKER)) {
				this.type = SENTYPE_QUERY;
			} else if (this.firstToken.equals(RULE_MARKER)) {
				this.type = SENTYPE_RULE;
			} else if (this.firstToken.equals(NAME_STARTMARKER)) {
				if (this.firstNonNameToken.equals(QUERY_MARKER)) {
					this.type = SENTYPE_QUERY;
				} else if (this.firstNonNameToken.equals(RULE_MARKER)) {
					this.type = SENTYPE_RULE;
				} else {
					this.type = SENTYPE_UNCATEGORISED;
				}
			} else if (this.firstToken.endsWith(ANNO_ENDMARKER)) {
				this.type = SENTYPE_ANNO;
			} else if (this.firstToken.equals(CMD_MARKER)) {
				this.type = SENTYPE_CMD;
			} else {
				this.type = SENTYPE_UNCATEGORISED;
			}
		}

		return this.type;
	}

	public boolean isFactSentence() {
		return isFactSentenceNormal() || isFactSentenceQualified();
	}

	public boolean isFactSentenceNormal() {
		return (this.type == SENTYPE_FACT_NORMAL);
	}

	public boolean isFactSentenceQualified() {
		return (this.type == SENTYPE_FACT_QUALIFIED);
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

			if (thisToken.equals(NAME_ENDMARKER)) {
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