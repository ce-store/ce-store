package com.ibm.ets.ita.ce.store.parsing.model;
/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public class PatternTokenizerSecondaryConceptClause extends TokenizerSecondaryConceptClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String MARKER_VARIABLE = "#";

	private String secConVariable = null;

	public PatternTokenizerSecondaryConceptClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, CeConcept pDomainCon, String pQualType) {
		super(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pQualType, true);
	}

	private static boolean isVariableConceptName(String pToken) {
		return pToken.startsWith(MARKER_VARIABLE);
	}

	@Override
	protected void extractSecondaryConcept() {
		//Skip the first two tokens.  They must be "is a" or "is an" for this to be a secondary concept clause
		this.finalTokenPos = 2;

		String currentToken = getCurrentToken();
		if (isVariableConceptName(currentToken)) {
			//This is a token variable (e.g. #C) so store it accordingly
			this.secConVariable = currentToken;
		} else {
			//This is a normal concept name so defer to the normal parent processing
			super.extractSecondaryConcept();
		}
	}

	@Override
	protected void dealWithAnyErrors() {
		//Nothing special to be done here
		if (this.secConVariable == null) {
			//There is no secondary concept variable specified so do the standard error handling
			super.dealWithAnyErrors();
		}
	}

	@Override
	protected void testForCompleteness() {
		super.testForCompleteness();

		//Additional test - if the secondary concept name variable is set then that's ok
		if (!this.complete) {
			this.complete = this.secConVariable != null;
		}
	}

	@Override
	public String explainIncompleteness() {
		String result = null;

		//Need to bypass the explainCompleteness on the parent as it will incorrectly
		//report a missing secondary concept if a secondary concept variable is specified
		result = explainIfNotAllTokensProcessed("");

		if ((this.secConVariable == null) && (this.secCon == null)) {
			//Both the secondary concept variable and the secondary concept are null
			result = appendReason(result, "No concept or concept variable was specified");
		}

		return result;
	}

	@Override
	protected String formattedQualifier() {
		return "(" + this.qualType + ":pattern)";		
	}

	@Override
	public void save(boolean pTokensOnly) {
		if (this.secConVariable != null) {
			if (!pTokensOnly) {
				//Save the main information
				this.sen.addConceptVariableToken(this.secConVariable);
			}

			//Save the CE tokens
			if (this.delimiter != null) {
				this.parent.addNormalCeToken(this.delimiter);
			}

			this.parent.addNormalCeToken(this.qualType);
			this.parent.addNormalCeToken(this.secConVariable);
		} else {
			super.save(pTokensOnly);
		}
	}
}