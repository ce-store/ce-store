package com.ibm.ets.ita.ce.store.parsing.tokenizer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AN;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THE;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFact;

public abstract class TokenizerSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected ActionContext ac = null;
	protected ModelBuilder mb = null;
	private BuilderSentence targetSentence = null;
	protected String conQualifier = ES;

	protected abstract BuilderSentence getTargetSentence();

	protected abstract void doTokenizing();

	protected abstract String tokenizerType();

	public static void doTokenizingFor(ActionContext pAc, BuilderSentence pSentence, boolean pIsInClause) {
		ModelBuilder mb = pAc.getModelBuilder();

		if (pSentence.isFactSentence()) {
			new TokenizerFactSentence().tokenizeNormalSentence(pAc, (BuilderSentenceFact) pSentence, pIsInClause);
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
		String result = ES;

		if (this.targetSentence != null) {
			result += tokenizerType() + " tokenizer for sentence : " + this.targetSentence.getSentenceText();
		}

		return result;
	}

	protected void checkForConceptQualifierMismatch(CeConcept pConcept, String pContext) {
		if (isReportDebug()) {
			// If the qualifier is missing (which means it was 'the') then the
			// test cannot be carried out as the qualifier is ambiguous
			if ((!this.conQualifier.isEmpty()) && (!this.conQualifier.equals(TOKEN_THE))) {
				if (this.conQualifier.equals(TOKEN_AN)) {
					if (!pConcept.isQualifiedWithAn()) {
						reportDebug("Inconsistent use of concept qualifier (" + this.conQualifier
								+ ") between conceptualise and " + pContext + " sentence, for concept '"
								+ pConcept.getConceptName() + "' in sentence: " + getTargetSentence().getSentenceText(),
								this.ac);
					}
				} else {
					if (pConcept.isQualifiedWithAn()) {
						reportDebug("Inconsistent use of (" + this.conQualifier + ") between conceptualise and "
								+ pContext + " sentence, for concept '" + pConcept.getConceptName() + "' in sentence: "
								+ getTargetSentence().getSentenceText(), this.ac);
					}
				}
			}
		}
	}

}
