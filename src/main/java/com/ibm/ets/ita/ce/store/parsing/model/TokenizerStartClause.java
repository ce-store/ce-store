package com.ibm.ets.ita.ce.store.parsing.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THERE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NO;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_A;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AN;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_VALUE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_IS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NAMED;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.handleSpecialMarkersAndDecode;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public class TokenizerStartClause extends TokenizerNormalClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String PREAMBLE_A = "there is a";
	private static final String PREAMBLE_AN = "there is an";

	private static int TYPE_THE = 1;
	private static int TYPE_THEREISA = 2;
	private static int TYPE_THEREISAN = 3;
	private static int TYPE_NO = 4;
	
	private int preambleType = -1;
	protected String conceptName = null;
	protected String instanceName = null;
	private String quoteType = null;

	public TokenizerStartClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, boolean pIsPatternClause) {
		super(pParent, pRawTokens, pDelimiter, pLastClause, null, pIsPatternClause);
	}

	public CeConcept getDomainConcept() {
		return this.domainCon;
	}

	@Override
	public void processClauseChain() {
		//Start clauses have their processing done in processStartClause as they
		//need to be processed before all other clauses are		
		testForCompleteness();

		super.processNextInChain();
	}
	
	public void processStartClause() {
		recordPreamble();
		extractConcept();
		extractInstance();
		dealWithAnyErrors();
	}

	protected void dealWithAnyErrors() {
		if (this.domainCon != null) {
			//Sometimes the start clause also contains the tokens of a trailing property clause
			if (!reachedEndOfTokens()) {
				splitRemainderIntoNewClause();
			}
		}
	}

	public boolean checkForConceptNameOverflowWith(String pDelimiter, ArrayList<String> pRawTokens) {
		boolean result = false;
		ArrayList<String> originalRawTokens = this.rawTokens;

		this.rawTokens.add(pDelimiter);

		for (int i = 0; i < pRawTokens.size(); i++) {
			this.rawTokens.add(pRawTokens.get(i));
		}

		this.processStartClause();
		this.testForCompleteness();

		if (this.complete) {
			result = true;
		} else {
			result = false;
			//Also revert the raw tokens back to the original state
			this.rawTokens = originalRawTokens;
			this.processStartClause();
			this.testForCompleteness();
		}

		return result;
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
			}
		}
	}

	@Override
	public String explainIncompleteness() {
		String result = null;

		result = explainIfNotAllTokensProcessed(result);

		if (this.conceptName == null) {
			result = appendReason(result, "No concept name was identified");
		} else {
			if (this.domainCon == null) {
				result = appendReason(result, "No concept found for concept name '" + this.conceptName + "'");
			} else {
				if (this.instanceName == null) {
					result = appendReason(result, "No instance name was identified");
				} else {
					//TODO: Eventually remove this test
					if (this.instanceName.equals(TOKEN_NAMED)) {
						result = appendReason(result, "Instance name of 'named' was detected");
					}
				}
			}
		}

		return result;
	}

	@Override
	public void save(boolean pTokensOnly) {
		if (!pTokensOnly) {
			//Save the main information
			if (isNegatedDomain()) {
				this.sen.setTargetConceptNegated(this.domainCon);
			} else {
				this.sen.setTargetConceptNormal(this.domainCon);
			}

			this.sen.setInstanceName(this.instanceName);
			
			if (this.quoteType != null) {
				this.sen.markInstanceNameAsQuoted();
			}
		}

		//Save the CE tokens
		this.parent.addNormalCeToken(formattedPreamble());
		
		if (this.conceptName != null) {
			if (!this.isPatternClause) {
				this.parent.addConceptCeToken(this.conceptName);
			} else {
				if (this.domainCon == null) {
					this.parent.addNormalCeToken(this.conceptName);
				} else {
					this.parent.addConceptCeToken(this.conceptName);
				}
			}
		} else {
			reportError("Unexpected null concept name");
		}
		
		if ((this.preambleType == TYPE_THEREISA) || (this.preambleType == TYPE_THEREISAN)) {
			this.parent.addNormalCeToken(TOKEN_NAMED);
		}
		
		if (this.isPatternClause) {
			if (this.quoteType == null) {
				//No quotes, so treat this as a value
				this.parent.addNormalCeToken(this.instanceName);
			} else {
				if (this.domainCon != null) {
					//There were quotes so treat this as an instance
					this.parent.addInstanceCeToken(this.instanceName, this.quoteType);
				} else {
					//This is a special pattern "value" clause
					this.parent.addValueCeToken(this.instanceName, this.quoteType);
				}
			}
		} else {
			this.parent.addInstanceCeToken(this.instanceName, this.quoteType);
		}
	}

	private boolean isNegatedDomain() {
		return this.preambleType == TYPE_NO;
	}

	private void recordPreamble() {
		if (this.rawTokens.get(0).equals(TOKEN_THE)) {
			this.preambleType = TYPE_THE;
			this.finalTokenPos = 1;
		} else if (this.rawTokens.get(0).equals(TOKEN_NO)) {
			this.preambleType = TYPE_NO;
			this.finalTokenPos = 1;
		} else {
			if (this.rawTokens.size() >= 3) {
				if ((this.rawTokens.get(0).equals(TOKEN_THERE)) && (this.rawTokens.get(1).equals(TOKEN_IS))) {
					if (this.rawTokens.get(2).equals(TOKEN_A)) {
						this.preambleType = TYPE_THEREISA;
						this.finalTokenPos = 3;
					} else if (this.rawTokens.get(2).equals(TOKEN_AN)) {
						this.preambleType = TYPE_THEREISAN;
						this.finalTokenPos = 3;
					} else {
						reportError("Unexpected preamble");
					}
				} else {
					reportError("Unexpected preamble");
				}
			} else {
				reportError("Fact sentence is too short (" + this.rawTokens.size() + " words)");
			}
		}
		
		//If there was an error ensure that the finalTokenPos is set to 0 (the start of the tokens)
		if (this.finalTokenPos == -1) {
			this.finalTokenPos = 0;
		}
	}

	protected void extractConcept() {
		//First get the concept name
		this.conceptName = extractConceptName();

		//The concept matching this name (if any) will be found in the lastMatchedConcept
		//property.  This is used for performance reasons since it is calculated when
		//testing for a valid concept name, and therefore saves making the call to retrieve
		//the concept twice.
		this.domainCon = this.lastMatchedConcept;
	}

	private void extractInstance() {
		if (!reachedEndOfTokens()) {
			String thisWord = getCurrentToken();

			if ((this.preambleType == TYPE_THEREISA) || (this.preambleType == TYPE_THEREISAN)) {
				if (thisWord.equals(TOKEN_NAMED)) {
					thisWord = getCurrentToken();
				} else {
					reportError("Expected word 'named' between domain concept and instance name was missing");
				}
			}
			
			String strippedInst = stripQuotesFrom(thisWord);

			//If the length changed then it did have delimiters so store which character was used
			if ((thisWord.length() != strippedInst.length())) {
				this.quoteType = thisWord.substring(0, 1);
			}

			this.instanceName = handleSpecialMarkersAndDecode(this.ac, strippedInst);
		}
	}
	
	protected void splitRemainderIntoNewClause() {
		ArrayList<String> confirmedTokens = new ArrayList<String>();
		ArrayList<String> extraTokens = new ArrayList<String>();
		String lastConfToken = null;
		
		for (int i = 0; i < this.finalTokenPos; i++) {
			lastConfToken = this.rawTokens.get(i);
			confirmedTokens.add(lastConfToken);
		}

		for (int i = this.finalTokenPos; i < this.rawTokens.size(); i++) {
			extraTokens.add(this.rawTokens.get(i));
		}
		
		//TODO: Is there a better way?
		//If the last confirmed token is the word "is" the remove it and add it back to extraTokens
		if (lastConfToken.equals("is")) {
			this.finalTokenPos--;
			confirmedTokens.remove(lastConfToken);
			extraTokens.add(0, lastConfToken);
		}

		if (extraTokens.size() > 1) {	
			//Take a copy of the next clause (it will get overwritten when the new clause gets created)
			TokenizerClause oldNextClause = this.nextClause;
			TokenizerNormalClause newPropClause = null;

			//Create the new property clause to contain the extra tokens
			if (this.hasValueDomain()) {
				newPropClause = TokenizerNormalClause.identifyAndCreateCorrectClause(this.parent, extraTokens, null, this, null, this.isPatternClause);
			} else {
				newPropClause = TokenizerNormalClause.identifyAndCreateCorrectClause(this.parent, extraTokens, null, this, this.domainCon, this.isPatternClause);
			}

			if (newPropClause != null) {
				//Fix the chain:
				// 1. Take the clause that is the current next clause and make it the next clause for the new clause
				newPropClause.nextClause = oldNextClause;
		
				// 2. Make the new clause the next clause
				this.nextClause = newPropClause;
		
				//Finally, reduce the tokens for this clause to reflect the split
				this.rawTokens = confirmedTokens;
			} else {
				reportError("No valid clause created during start clause split");
			}
		} else {
			this.sen.hasError(this.ac, "Unexpected trailing word (" + extraTokens.get(0) + ") in starter clause");
		}
	}
	
	protected boolean hasValueDomain() {
		return (this.conceptName == null) || this.conceptName.equals(TOKEN_VALUE);
	}

	@Override
	protected String formattedQualifier() {
		return "(StartClause)";
	}
	
	protected String formattedPreamble() {
		if (this.preambleType == TYPE_THE) {
			return TOKEN_THE;
		} else if (this.preambleType == TYPE_THEREISA) {
			return PREAMBLE_A;
		} else if (this.preambleType == TYPE_THEREISAN) {
			return PREAMBLE_AN;
		} else if (this.preambleType == TYPE_NO) {
			return TOKEN_NO;
		} else {
			return "???";	//TODO: Abstract this
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(formattedQualifier());

		sb.append(" conceptName=" + this.conceptName);
		sb.append(", instanceName=" + this.instanceName);
		sb.append(", delimiter=" + this.delimiter);
		sb.append(", rawTokens=" + this.rawTokens.toString());

		return sb.toString();
	}

}
