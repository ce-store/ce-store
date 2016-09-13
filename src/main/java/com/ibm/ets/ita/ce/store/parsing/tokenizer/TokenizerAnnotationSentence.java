package com.ibm.ets.ita.ce.store.parsing.tokenizer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_ANNONAME;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_ANNOVAL;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;

public class TokenizerAnnotationSentence extends TokenizerSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	@Override
	protected String tokenizerType() {
		return "Annotation";
	}

	@Override
	protected BuilderSentence getTargetSentence() {
		return superTargetSentence();
	}

	@Override
	public void doTokenizing() {
		//No tokenizing is required for annotation sentences
		getTargetSentence().validationComplete();

		int rtCount = getTargetSentence().getRawTokens().size();
		if (rtCount == 2) {
			getTargetSentence().addFinalToken(SCELABEL_ANNONAME, "", getTargetSentence().getRawTokens().get(0).trim());
			getTargetSentence().addFinalToken(SCELABEL_ANNOVAL, "", getTargetSentence().getRawTokens().get(1).trim());
		} else {
			reportWarning("Unexpected number of tokens (" + rtCount + ") encountered for annotation sentence: " + getTargetSentence().getSentenceText(), this.ac);
		}
	}

}
