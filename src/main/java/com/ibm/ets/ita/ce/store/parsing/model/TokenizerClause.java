package com.ibm.ets.ita.ce.store.parsing.model;
/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_VALUE;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.stripDelimitingQuotesFrom;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFact;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public abstract class TokenizerClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected TokenizerFactSentence parent = null;
	protected ActionContext ac = null;
	protected BuilderSentenceFact sen = null;
	protected String delimiter = null;
	protected ArrayList<String> rawTokens = null;
	protected TokenizerClause previousClause = null;
	protected TokenizerClause nextClause = null;
	protected int finalTokenPos = -1;
	protected boolean complete = false;
	protected CeConcept lastMatchedConcept = null;

	public abstract void processClauseChain();
	public abstract String explainIncompleteness();
	public abstract void save(boolean pTokensOnly);
	protected abstract void testForCompleteness();
	
	public TokenizerClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pPreceedingClause) {
		this.parent = pParent;
		this.ac = pParent.getActionContext();
		this.sen = pParent.getSentence();
		this.delimiter = pDelimiter;
		this.rawTokens = pRawTokens;

		linkClauses(pPreceedingClause, this);
	}

	protected static String stripQuotesFrom(String pWord) {
		//Convenience method
		return stripDelimitingQuotesFrom(pWord);
	}

	private static void linkClauses(TokenizerClause pFirstClause, TokenizerClause pSecondClause) {
		if (pFirstClause != null) {
			pFirstClause.nextClause = pSecondClause;
		}

		if (pSecondClause != null) {
			pSecondClause.previousClause = pFirstClause;
		}
	}
	
	public boolean isComplete() {
		return this.complete;
	}

	public ArrayList<TokenizerClause> listAllClauses() {
		ArrayList<TokenizerClause> allClauses = new ArrayList<TokenizerClause>();
		this.calculateAllClauses(allClauses);

		return allClauses;
	}
	
	protected void calculateAllClauses(ArrayList<TokenizerClause> pList) {
		pList.add(this);

		if (this.nextClause != null) {
			this.nextClause.calculateAllClauses(pList);
		}
	}

	protected void reportError(String pErrorMsg) {
		//Convenience method
		this.parent.reportError(pErrorMsg);
	}

	protected CeConcept getConceptNamed(String pConName) {
		//Convenience method
		return this.ac.getModelBuilder().getConceptNamed(this.ac, pConName);
	}

	protected ArrayList<CeConcept> getConceptsWithNameStarting(String pConName) {
		//Convenience method
		return this.ac.getIndexedEntityAccessor().calculateConceptsWithNameStarting(this.ac, pConName + " ");
	}

	protected String getCurrentToken() {
		String result = null;

		if (this.finalTokenPos < this.rawTokens.size()) {
			result = this.rawTokens.get(this.finalTokenPos++);
		}

		return result;
	}

	protected String extractConceptName() {
		this.lastMatchedConcept = null;
		String conName = null;
		CeConcept matchedCon = null;
		ArrayList<CeConcept> conList = null;
		int rts = this.rawTokens.size();
		boolean carryOn = true;

		while (carryOn) {
			if (rts > this.finalTokenPos) {
				String thisWord = getCurrentToken();

				if (conName == null) {
					conName = thisWord;
				} else {
					conName += " ";
					conName += thisWord;
				}

				matchedCon = getConceptNamed(conName);
				conList = getConceptsWithNameStarting(conName);

				if (matchedCon != null) {
					//A concept is matched
					if (conList.isEmpty()) {
						//Simple - A concept is matched and there are no other possibilities.  Return the matched concept
						this.lastMatchedConcept = matchedCon;
						carryOn = false;
					} else {
						//There are other possible concepts that start in this way. Have a look to see if a later match is possible
						if (!isConceptMatchedLater(this.rawTokens, this.finalTokenPos, conName)) {
							//No longer concept name match is possible, so go with this one
							this.lastMatchedConcept = matchedCon;
							carryOn = false;
						}
					}
				} else {
					//No concepts is matched
					if (conList.isEmpty()) {
						//Simple - No concept is matched and there are no other possibilities.  Return a failure
						if (!conName.equals(TOKEN_VALUE)) {
							conName = null;
						}
						this.lastMatchedConcept = null;
						carryOn = false;
					} else {
						//No concept is matched but there are other possible concepts to continue. Carry on as normal
					}
				}
			} else {
				//There are no more tokens left
				this.lastMatchedConcept = matchedCon;
				carryOn = false;
			}
		}

		return conName;
	}

	private boolean isConceptMatchedLater(ArrayList<String> pTokens, int pPos, String pConNameSoFar) {
		boolean result = false;

		if (pTokens.size() > pPos) {
			String newConName = pConNameSoFar + " " + pTokens.get(pPos);
			result = (getConceptNamed(newConName) != null);

			if (!result) {
				result = isConceptMatchedLater(pTokens, (pPos + 1), newConName);
			}
		} else {
			//The end of the tokens list, but if matches are still possible then return true
			//as this is likely an incorrectly split clause
			return !getConceptsWithNameStarting(pConNameSoFar).isEmpty();
		}

		return result;
	}

	protected static String appendReason(String pExisting, String pNew) {
		String result = pExisting;

		if (result == null) {
			result = pNew;
		} else {
			result += ", " + pNew;
		}

		return result;
	}
	
	protected String explainIfNotAllTokensProcessed(String pResult) {
		String result = null;

		if (!reachedEndOfTokens()) {
			result = appendReason(pResult, "Not all tokens processed.  " + this.rawTokens.size() + " tokens in clause but only " + this.finalTokenPos + " processed");
		}

		return result;
	}

	public TokenizerClause getClauseAtEndOfChain() {
		TokenizerClause result = null;

		if (this.nextClause == null) {
			result = this;
		} else {
			result = this.nextClause.getClauseAtEndOfChain();
		}

		return result;
	}
	
	protected boolean reachedEndOfTokens() {
		return this.rawTokens.size() == this.finalTokenPos;
	}
	
	protected void processNextInChain() {
		if (this.nextClause != null) {
			this.nextClause.processClauseChain();
		}
	}
	
	@Override
	public String toString() {
		return "(TokenizerClause) delimiter=" + this.delimiter + ", rawTokens=" + this.rawTokens;
	}

}
