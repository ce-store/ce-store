package com.ibm.ets.ita.ce.store.parsing.tokenizer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFactNormal;

public abstract class TokenizerSentence {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public static final String SCELABEL_NORMAL = "";
	public static final String SCELABEL_CONCEPT = "{Concept}:";
	public static final String SCELABEL_PROP = "{Property}:";
	public static final String SCELABEL_CONNECTOR = "{Connector}:";
	public static final String SCELABEL_RQNAME = "{Name}:";
	public static final String SCELABEL_RQSTART = "{RqStart}:";

	public static final String TOKEN_A = "a";
	public static final String TOKEN_AN = "an";
	public static final String TOKEN_THE = "the";
	public static final String TOKEN_VALUE = "value";
	public static final String TOKEN_THERE = "there";
	public static final String TOKEN_IS = "is";
	public static final String TOKEN_NAMED = "named";	
	public static final String TOKEN_HAS = "has";
	public static final String TOKEN_AS = "as";
	public static final String TOKEN_AND = "and";
	public static final String TOKEN_THAT = "that";
	public static final String TOKEN_IS_A = "is a";
	public static final String TOKEN_IS_AN = "is an";

	protected ActionContext ac = null;
	protected ModelBuilder mb = null;
	private BuilderSentence targetSentence = null;
	protected String conQualifier = "";

	protected abstract BuilderSentence getTargetSentence();
	protected abstract void doTokenizing();
	protected abstract String tokenizerType();

	public static void doTokenizingFor(ActionContext pAc, BuilderSentence pSentence, boolean pIsInClause) {
		ModelBuilder mb = pAc.getModelBuilder();

		if (pSentence.isFactSentenceNormal()) {
			new TokenizerFactSentence().tokenizeNormalSentence(pAc, (BuilderSentenceFactNormal)pSentence, pIsInClause);
		} else if (pSentence.isFactSentenceQualified()) {
			new TokenizerFactSentenceQualified().tokenizeSentence(pAc, mb, pSentence);
		} else if (pSentence.isModelSentence()) {
			new TokenizerModelSentence().tokenizeSentence(pAc, mb, pSentence);
		} else if (pSentence.isRuleSentence()) {
			new TokenizerRuleSentence().tokenizeSentence(pAc, mb, pSentence);
		} else if (pSentence.isQuerySentence()) {
			new TokenizerQuerySentence().tokenizeSentence(pAc, mb, pSentence);
		} else if (pSentence.isAnnotationSentence()) {
			new TokenizerAnnotationSentence().tokenizeSentence(pAc, mb, pSentence);
		} else if (pSentence.isCommandSentence()) {
			new TokenizerCommandSentence().tokenizeSentence(pAc, mb, pSentence);
		} else {
			reportErrorForUnknownSentenceType(pAc, pSentence);
		}
	}

	private static void reportErrorForUnknownSentenceType(ActionContext pAc, BuilderSentence pSentence) {
		pSentence.hasError(pAc, "100: Cannot tokenize sentence as sentence type cannot be detected");
	}

	protected BuilderSentence superTargetSentence() {
		return this.targetSentence;
	}

	public void tokenizeSentence(ActionContext pAc, ModelBuilder pMb, BuilderSentence pSentence) {
		this.ac = pAc;
		this.mb = pMb;
		this.targetSentence = pSentence;

		doTokenizing();
	}

	protected void addErrorToSentence(String pErrorText) {
		this.targetSentence.hasError(this.ac, pErrorText);
	}

	@Override
	public String toString() {
		String result = "";

		if (this.targetSentence != null) {
			result += tokenizerType() + " tokenizer for sentence : " + this.targetSentence.getSentenceText();
		}

		return result;
	}

	protected void checkForConceptQualifierMismatch(CeConcept pConcept, String pContext) {
		if (isReportDebug()) {
			//If the qualifier is missing (which means it was "the") then the test cannot be carried out as the qualifier is ambiguous
			if ((!this.conQualifier.isEmpty()) && (!this.conQualifier.equals(TOKEN_THE))) {
				if (this.conQualifier.equals(TOKEN_AN)) {
					if (!pConcept.isQualifiedWithAn()) {
						reportDebug("Inconsistent use of concept qualifier (" + this.conQualifier + ") between conceptualise and " + pContext + " sentence, for concept '" + pConcept.getConceptName() + "' in sentence: " + this.getTargetSentence().getSentenceText(), this.ac);
					}
				} else {
					if (pConcept.isQualifiedWithAn()) {
						reportDebug("Inconsistent use of (" + this.conQualifier + ") between conceptualise and " + pContext + " sentence, for concept '" + pConcept.getConceptName() + "' in sentence: " + this.getTargetSentence().getSentenceText(), this.ac);
					}
				}
			}
		}
	}

	public static String SCELABEL_PROPDEF(String pDomain, String pRange) {
		StringBuffer b = new StringBuffer();
		b.append(CeSentence.PROPDEF_PREFIX);
		b.append(pDomain);
		b.append(",");
		b.append(pRange);
		b.append(CeSentence.PROPDEF_SUFFIX);
		return b.toString();
	}

}