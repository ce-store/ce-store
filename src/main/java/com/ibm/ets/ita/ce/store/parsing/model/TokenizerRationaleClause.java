package com.ibm.ets.ita.ce.store.parsing.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public class TokenizerRationaleClause extends TokenizerClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TOKEN_RQ_START = "[";
	private static final String TOKEN_RQ_END = "]";
	private static final String TOKEN_BECAUSE = "because";

	private String ruleName = null;
	private String rationaleText = null;
	private TokenizerNormalClause innerClause = null;
	
	public TokenizerRationaleClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause) {
		super(pParent, pRawTokens, pDelimiter, pLastClause);
	}
	
	public static TokenizerRationaleClause createUsing(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause) {
		return new TokenizerRationaleClause(pParent, pRawTokens, pDelimiter, pLastClause);
	}

	public String getRuleName() {
		return this.ruleName;
	}
	
	public String getRationaleText() {
		return this.rationaleText;
	}
	
	public ArrayList<String> getRationaleTokens() {
		return this.rawTokens;
	}
	
	@Override
	public void processClauseChain() {
		createInnerClauses();
		testForCompleteness();

		super.processNextInChain();
	}

	private void createInnerClauses() {
		//Extract the rule name
		extractRuleNameAndRationaleText();

		//Save the main information
		this.innerClause = TokenizerNormalClause.createUsing(this.parent, this.rawTokens, null, null, true, true);

		//If an inner clause was successfully extracted process and save all clauses
		if (this.innerClause != null) {
			this.innerClause.processClauseChain();
		} else {
			reportError("Unable to create inner clauses as no start clause was found");
		}
	}

	@Override
	protected void testForCompleteness() {
		this.finalTokenPos = 0;

		//Account for all the tokens in each of the inner clauses
		if (this.innerClause != null) {
			for (TokenizerClause thisClause : this.innerClause.listAllClauses()) {
				this.finalTokenPos += thisClause.finalTokenPos;
			}
		}

		this.complete = this.reachedEndOfTokens();
	}

	@Override
	public String explainIncompleteness() {
		//No special explanations needed for rationale clauses
		return super.explainIfNotAllTokensProcessed("");
	}

	@Override
	public void save(boolean pTokensOnly) {
		//Save the delimiter
		if ((this.delimiter != null) && (!this.delimiter.isEmpty())) {
			if (this.delimiter.equals(TOKEN_BECAUSE)) {
				this.parent.addBecauseCeToken();
			} else {
				this.parent.addDelimiterCeToken(this.delimiter);
			}
		}

		//If an inner clause was successfully extracted process and save all clauses
		if (this.innerClause != null) {
			saveTokensOrReportErrors();
		}
	}
	
	private void extractRuleNameAndRationaleText() {
		//The rule name is the tokens between the [ and ] tokens
		boolean inRuleName = false;
		int numRuleTokens = 0;
		StringBuilder sb = new StringBuilder();

		//Iterate through all the tokens, extracting the rule name
		for (int i = 0; i < this.rawTokens.size(); i++) {
			String thisToken = this.rawTokens.get(i);
			
			if (!inRuleName) {
				//Not yet in rule name
				if (thisToken.equals(TOKEN_RQ_START)) {
					inRuleName = true;
					++numRuleTokens;
				} else {
					//This is still part of the rationale text so append it
					if (sb.length() != 0) {
						sb.append(" ");
					}
					sb.append(thisToken);
				}
			} else {
				//In rule name
				++numRuleTokens;

				if (!thisToken.equals(TOKEN_RQ_END)) {
					if (this.ruleName == null) {
						this.ruleName = thisToken;
					} else {
						this.ruleName += " " + thisToken;
					}
				}
			}
		}

		this.rationaleText = sb.toString();
		
		//Now ensure that all the rule name tokens are removed
		for (int i = 0; i < numRuleTokens; i++) {
			this.rawTokens.remove(this.rawTokens.size() - 1);
		}
	}

	private void saveTokensOrReportErrors() {
		if (this.innerClause != null) {
			ArrayList<TokenizerClause> allClauses = this.innerClause.listAllClauses();
			String incompleteClauseText = "";

			//Test all the clauses and compile descriptions for any that are incomplete
			for (TokenizerClause thisClause : allClauses) {
				if (!thisClause.isComplete()) {
					incompleteClauseText += "\n" + thisClause.explainIncompleteness() + " [" + thisClause + "]";
				}
			}

			//Report error(s) or save the clauses
			if (!incompleteClauseText.isEmpty()) {
				//Some clauses were incomplete.  This means there are errors in the CE sentence.
				//Report the details and do not save the clauses
				reportWarning(this.toString() + incompleteClauseText, this.ac);
			} else {
				//All clauses were complete, so save them all
				for (TokenizerClause thisClause : allClauses) {
					//These are rationale clauses so ask for only the tokens to be saved
					thisClause.save(true);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "(TokenizerRationaleClause) delimiter=" + this.delimiter + ", rawTokens=" + this.rawTokens;
	}

}
