package com.ibm.ets.ita.ce.store.parsing.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NO;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_VALUE;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public class TokenizerVsPropertyClause extends TokenizerPropertyClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private boolean ignoreRangeName = false;
	
	public TokenizerVsPropertyClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, CeConcept pDomainCon, boolean pIsPatternClause) {
		super(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);
	}

	@Override
	public void processClauseChain() {
		extractVsPropertyDetails();
		testForCompleteness();

		super.processNextInChain();
	}
	
	/*
	 * Verb singular (VS) properties take the form:
	 *    {property name} {range name} {range value}
	 * e.g.
	 *    "is married to the person Fred"
	 * So seek the features of this property clause in that way 
	 */
	private void extractVsPropertyDetails() {
		this.finalTokenPos = 0;

		//Only the property name can be extracted at this stage
		this.propName = extractPropertyName();
		
		if (isNormalDeterminerToken()) {
			this.finalTokenPos++;

			this.rangeName = extractConceptName();
			this.rangeCon = this.lastMatchedConcept;
		} else if (isNegatedDeterminerToken()) {
			this.finalTokenPos++;

			this.negatedRange = true;
			this.rangeName = extractConceptName();
			this.rangeCon = this.lastMatchedConcept;
		} else {
			this.rangeName = TOKEN_VALUE;
			this.ignoreRangeName = true;		//TODO: Is there a cleaner way?
		}
		
		if ((this.rangeName == null) || (this.rangeName.equals(TOKEN_VALUE))) {
			this.style = STYLE_DAT;
		} else {
			this.style = STYLE_OBJ;
		}
		
		handleRangeValue();

		//Now that the property name and the range have been extracted
		//the exact matching property can be determined
		this.matchedProp = extractExactProperty();
	}

	@Override
	protected void saveCeTokens() {
		if (!this.matchedProp.isSpecialOperatorProperty()) {
			this.parent.addPropetyCeTokens(this.matchedProp);
		} else {
			this.parent.addNormalCeToken(this.matchedProp.getPropertyName());
		}

		if ((this.rangeName != null) && (!this.ignoreRangeName)) {
			if (this.negatedRange) {
				this.parent.addNormalCeToken(TOKEN_NO);
			} else {
				this.parent.addNormalCeToken(TOKEN_THE);
			}

			if (this.rangeName.equals(TOKEN_VALUE)) {
				this.parent.addNormalCeToken(TOKEN_VALUE);
				this.parent.addValueCeToken(this.rangeValue, this.quoteType);
			} else {
				this.parent.addConceptCeToken(this.rangeName);
				
				if (this.isPatternClause) {
					if (this.quoteType == null) {
						if (this.rangeValue != null) {
							this.parent.addNormalCeToken(this.rangeValue);
						}
					} else {
						this.parent.addInstanceCeToken(this.rangeValue, this.quoteType);
					}
				} else {
					this.parent.addInstanceCeToken(this.rangeValue, this.quoteType);
				}
			}
		} else {
			this.parent.addValueCeToken(this.rangeValue, this.quoteType);
		}

	}

	@Override
	protected String formattedQualifier() {
		String stylePart = null;

		if (this.style != null) {
			stylePart = " " + this.style;
		} else {
			stylePart = "";
		}

		return "(VS" + stylePart + ")";		
	}

}
