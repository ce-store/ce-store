package com.ibm.ets.ita.ce.store.parsing.model;
/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public class TokenizerSecondaryConceptClause extends TokenizerNormalClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	protected static final String TYPE_ISA = "is a";
	protected static final String TYPE_ISAN = "is an";

	protected String qualType = null;
	private String secConName = null;
	protected CeConcept secCon = null;
	
	public TokenizerSecondaryConceptClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, CeConcept pDomainCon, String pQualType, boolean pIsPatternClause) {
		super(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);

		this.qualType = pQualType;
	}

	@Override
	public void processClauseChain() {
		extractSecondaryConcept();
		dealWithAnyErrors();
		testForCompleteness();
		
		super.processNextInChain();
	}

	protected void dealWithAnyErrors() {
		if (this.secCon == null) {
			//The detection of the secondary concept failed.
			//This is either an error or possibly an incorrectly split or classified secondary concept
			
			//First test whether incorporating the nextClause fixes the issue
			//(i.e. this is due to an incorrect delimiter split due to the concept name containing the delimiter word)
			if (!testForIncorrectDelimiterSplit()) {
				//Second test whether this is an incorrectly classified secondary concept clause when it is
				//in fact a property with the same start
				testForIncorrectlyClassifiedClause();
			}
		}
	}
	
	private boolean testForIncorrectDelimiterSplit() {
		if (this.nextClause != null) {
			ArrayList<String> originalRawTokens = this.rawTokens;
			this.rawTokens = new ArrayList<String>(this.rawTokens);

			this.rawTokens.add(this.nextClause.delimiter);
			this.rawTokens.addAll(this.nextClause.rawTokens);
			
			this.extractSecondaryConcept();
			this.testForCompleteness();

			if (isComplete()) {
				//Fixed: Including the delimiter and tokens from the following clause resolves this issue
				//Make the change permanent by adopting the required additional tokens, retesting this
				//clause and removing the following clause from the chain
				TokenizerClause newNextClause = this.nextClause.nextClause;
				
				this.nextClause = newNextClause;
				if (newNextClause != null) {
					newNextClause.previousClause = this;
				}
			} else {
				//The fix failed so revert this clause back to normal
				this.rawTokens = originalRawTokens;
				this.extractSecondaryConcept();
				this.testForCompleteness();
			}
		}
		
		return isComplete();
	}

	private boolean testForIncorrectlyClassifiedClause() {
		TokenizerVsPropertyClause test = new TokenizerVsPropertyClause(this.parent, this.rawTokens, this.delimiter, this.previousClause, this.domainCon, this.isPatternClause);
		test.processClauseChain();
		boolean result = test.isComplete();

		if (result) {
			//Success - it was a misclassified object property so re-link and remove the current clause from the chain
			//When the object property clause was created in correctly had the previousLink and the corresponding nextLink
			//(on the previous clause) set up so we only need to manually link in the nextClause
			if (this.nextClause != null) {
				test.nextClause = this.nextClause;
				this.nextClause.previousClause = test;
			}
		}

		return result;
	}
	
	@Override
	protected void testForCompleteness() {
		if (reachedEndOfTokens()) {
			this.complete = (this.secCon != null);
		}
	}

	@Override
	public String explainIncompleteness() {
		String result = null;

		result = explainIfNotAllTokensProcessed(result);

		if (this.secConName == null) {
			result = appendReason(result, "No concept name was identified");
		} else {
			if (this.secCon == null) {
				result = appendReason(result, "No concept found for concept name '" + this.secConName + "'");
			}
		}

		return result;
	}
	
	@Override
	public void save(boolean pTokensOnly) {
		if (!pTokensOnly) {
			//Save the main information
			this.sen.addSecondaryConceptNormal(this.secCon);
		}

		//Save the CE tokens
		if (this.delimiter != null) {
			this.parent.addDelimiterCeToken(this.delimiter);
		}
		this.parent.addNormalCeToken(this.qualType);
		this.parent.addConceptCeToken(this.secConName);
	}

	protected void extractSecondaryConcept() {
		//Skip the first two tokens.  They must be "is a" or "is an" for this to be a secondary concept clause
		this.finalTokenPos = 2;

		this.secConName = extractConceptName();
		this.secCon = this.lastMatchedConcept;
	}

	@Override
	protected String formattedQualifier() {
		return "(" + this.qualType + ")";		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(formattedQualifier());

		sb.append(" conceptName=" + this.secConName);
		sb.append(", concept=" + this.secCon);
		sb.append(", delimiter=" + this.delimiter);
		sb.append(", rawTokens=" + this.rawTokens.toString());

		return sb.toString();
	}

}