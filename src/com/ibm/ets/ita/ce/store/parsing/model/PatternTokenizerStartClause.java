package com.ibm.ets.ita.ce.store.parsing.model;
/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public class PatternTokenizerStartClause extends TokenizerStartClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public PatternTokenizerStartClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause) {
		super(pParent, pRawTokens, pDelimiter, pLastClause, true);
	}

	@Override
	protected void testForCompleteness() {
		if (reachedEndOfTokens()) {
			if (this.domainCon != null) {
				if (this.instanceName != null) {
					if (!this.instanceName.equals(TOKEN_NAMED)) {
						this.complete = true;
					}
				}
			} else {
				//It is ok for domainCon to be null if domainName is value since this is a pattern clause
				this.complete = isValueDomain();
			}
		}
	}

	@Override
	public String explainIncompleteness() {
		String result = null;

		if (isValueDomain()) {
			//This concept name is value so do special processing
			result = explainIfNotAllTokensProcessed("");

			if (this.instanceName == null) {
				result = appendReason(result, "No instance name was identified");
			} else {
				//TODO: Eventually remove this test
				if (this.instanceName.equals(TOKEN_NAMED)) {
					result = appendReason(result, "Instance name of 'named' was detected");
				}
			}
		} else {
			//Do normal processing
			result = super.explainIncompleteness();
		}

		return result;
	}

	@Override
	protected void dealWithAnyErrors() {
		if (isValueDomain()) {
			//Sometimes the start clause also contains the tokens of a trailing property clause
			if (!reachedEndOfTokens()) {
				splitRemainderIntoNewClause();
			}
		} else {
			super.dealWithAnyErrors();
		}
	}

	@Override
	protected String formattedQualifier() {
		return "(StartClause:Pattern)";
	}

	private boolean isValueDomain() {
		return (this.conceptName == null) || (this.conceptName.equals(TOKEN_VALUE));
	}

}