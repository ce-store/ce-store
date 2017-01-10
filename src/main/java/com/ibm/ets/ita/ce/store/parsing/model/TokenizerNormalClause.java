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
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_IS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_HAS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AS;
import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public abstract class TokenizerNormalClause extends TokenizerClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	protected CeConcept domainCon = null;
	protected boolean isPatternClause = false;
	
	protected abstract String formattedQualifier();

	public TokenizerNormalClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, CeConcept pDomainCon, boolean pIsPatternClause) {
		super(pParent, pRawTokens, pDelimiter, pLastClause);

		this.domainCon = pDomainCon;
		this.isPatternClause = pIsPatternClause;
	}
	
	public static TokenizerNormalClause createUsing(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, boolean pIsPatternClause, boolean pRatMode) {
		TokenizerNormalClause result = null;
		TokenizerStartClause startClause = null;
		String firstToken = pRawTokens.get(0);

		if (isStartToken(firstToken, pIsPatternClause)) {
			if (pIsPatternClause) {
				//This is a starter clause inside a rule or query
				startClause = new PatternTokenizerStartClause(pParent, pRawTokens, pDelimiter, pLastClause);
			} else {
				//This is a normal starter clause
				startClause = new TokenizerStartClause(pParent, pRawTokens, pDelimiter, pLastClause, pIsPatternClause);
			}
			startClause.processStartClause();
			
			if (!pRatMode) {
				//Only update the parent start clause if this is not a rationale start clause
				pParent.setStartClause(startClause);
			}
			result = startClause;
		} else {
			startClause = pParent.getStartClause();
			if (startClause != null) {
				//This is a property clause
				if (startClause.hasValueDomain()) {
					//This clause has a value domain
					result = TokenizerNormalClause.identifyAndCreateCorrectClause(pParent, pRawTokens, pDelimiter, pLastClause, null, pIsPatternClause);
				} else {
					//This is a normal (non-value) clause
					CeConcept domCon = startClause.getDomainConcept();
					
					if (domCon == null) {
						//Try to use these raw tokens to fix the broken start clause
						if (!startClause.checkForConceptNameOverflowWith(pDelimiter, pRawTokens)) {
							pParent.reportError("Unable to create property clause as domain concept was not found");
						} else {
							result = startClause;
						}
					} else {
						result = TokenizerNormalClause.identifyAndCreateCorrectClause(pParent, pRawTokens, pDelimiter, pLastClause, domCon, pIsPatternClause);
					}
				}
			} else {
				pParent.reportError("Unable to create property clause as no start clause was found");
			}
		}

		return result;
	}
	
	private static boolean isStartToken(String pToken, boolean pIsPatternClause) {
		return (pToken.equals(TOKEN_THE)) ||
				(pToken.equals(TOKEN_THERE)) ||
				(pToken.equals(TOKEN_NO) && pIsPatternClause);
	}

	protected static TokenizerSecondaryConceptClause createSecondaryConceptClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, CeConcept pDomainCon, String pPreamble, boolean pIsPatternClause) {
		TokenizerSecondaryConceptClause result = null;

		if (pIsPatternClause) {
			//This is a secondary concept clause inside a rule or query
			result = new PatternTokenizerSecondaryConceptClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pPreamble);
		} else {
			//This is a normal secondary concept clause
			result = new TokenizerSecondaryConceptClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pPreamble, pIsPatternClause);
		}
		
		return result;
	}

	public static TokenizerNormalClause identifyAndCreateCorrectClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, CeConcept pDomainCon, boolean pIsPatternClause) {
		TokenizerNormalClause result = null;
		String firstWord = pRawTokens.get(0);

		if (pRawTokens.size() > 1) {
			String secondWord = pRawTokens.get(1);
		
			if (firstWord.equals(TOKEN_IS)) {
				if (secondWord.equals(TOKEN_A)) {
					result = createSecondaryConceptClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, TokenizerSecondaryConceptClause.TYPE_ISA, pIsPatternClause);
				} else if (secondWord.equals(TOKEN_AN)) {
					result = createSecondaryConceptClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, TokenizerSecondaryConceptClause.TYPE_ISAN, pIsPatternClause);
				} else {
					result = new TokenizerVsPropertyClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);
				}
			} else if (firstWord.equals(TOKEN_HAS)) {
				if (secondWord.equals(TOKEN_AS)) {
					//Sometimes verb singular properties begin "has as"
					result = new TokenizerVsPropertyClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);
				} else {
					result = new TokenizerFnPropertyClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);
				}
			} else {
				result = new TokenizerVsPropertyClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);
			}
		} else {
			result = new TokenizerVsPropertyClause(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);
		}

		return result;
	}

}
