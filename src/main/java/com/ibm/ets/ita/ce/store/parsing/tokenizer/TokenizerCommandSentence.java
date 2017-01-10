package com.ibm.ets.ita.ce.store.parsing.tokenizer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceCommand;

public class TokenizerCommandSentence extends TokenizerSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	@Override
	protected BuilderSentenceCommand getTargetSentence() {
		return (BuilderSentenceCommand)superTargetSentence();
	}

	@Override
	protected String tokenizerType() {
		return "Command";
	}

	@Override
	public void doTokenizing() {
		reportWarning("401: Command sentence processing not yet implemented", this.ac);
	}

}
