package com.ibm.ets.ita.ce.store.parsing.model;
/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.names.ParseNames.PREAMBLE_THEVALUE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.PREAMBLE_NOVALUE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NO;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_VALUE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CONCAT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_HAS;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public class TokenizerFnPropertyClause extends TokenizerPropertyClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String datatypePreamble = null;

	public TokenizerFnPropertyClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, CeConcept pDomainCon, boolean pIsPatternClause) {
		super(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);
	}

	@Override
	public void processClauseChain() {
		extractFnPropertyDetails();

		testForCompleteness();

		super.processNextInChain();
	}

	private void extractFnPropertyDetails() {
		this.finalTokenPos = 1;

		if (isNormalDeterminerToken()) {
			++this.finalTokenPos;

			if (this.rawTokens.get(this.finalTokenPos).equals(TOKEN_VALUE)) {
				++this.finalTokenPos;
				this.datatypePreamble = PREAMBLE_THEVALUE;
				handleDatatypeProperty();
			} else {
				this.datatypePreamble = TOKEN_THE;
				handleObjectProperty();
			}
		} else if (isNegatedDeterminerToken()) {
			++this.finalTokenPos;

			this.negatedRange = true;
			if (this.rawTokens.get(this.finalTokenPos).equals(TOKEN_VALUE)) {
				++this.finalTokenPos;
				this.datatypePreamble = PREAMBLE_NOVALUE;
				handleDatatypeProperty();
			} else {
				this.datatypePreamble = TOKEN_NO;
				handleObjectProperty();
			}

		} else {
			this.datatypePreamble = null;
			handleDatatypeProperty();
		}
	}
	
	private void handleDatatypeProperty() {
		this.style = STYLE_DAT;
		this.rangeName = TOKEN_VALUE;
		
		handleGenericProperty();
	}
	
	private void handleObjectProperty() {
		this.style = STYLE_OBJ;
		
		this.rangeName = extractConceptName();
		this.rangeCon = this.lastMatchedConcept;
		
		handleGenericProperty();
	}
	
	private void handleGenericProperty() {
		handleRangeValue();

		if (this.rawTokens.size() > this.finalTokenPos) {
			String thisToken = this.rawTokens.get(this.finalTokenPos);

			if (thisToken.equals(TOKEN_AS)) {
				++this.finalTokenPos;

				this.propName = extractPropertyName();
				this.matchedProp = extractExactProperty();
			} else {
				if (thisToken.equals(TOKEN_CONCAT)) {
					handleConcatenation();
				} else {
					reportError("No 'as' after instance name in property");
				}
			}
		} else {
			reportError("Maximum tokens exceeded");
		}
	}
	
	private void handleConcatenation() {
		String nextToken = getCurrentToken();

		this.isConcatenation = true;

		//If the original value was quoted, stick the quotes back on as the concatenation value must have quotes preserved
		if (this.quoteType != null) {
			this.rangeValue = this.quoteType + this.rangeValue + this.quoteType;
		}

		while (!nextToken.equals(TOKEN_AS)) {
			this.rangeValue += " " + nextToken;

			if (reachedEndOfTokens()) {
				break;
			}

			nextToken = getCurrentToken();
		}

		if (isReportDebug()) {
			reportDebug("Concatenation clause detected, with value '" + this.rangeValue + "'", this.ac);
		}
		
		//Now get the property name and matched property in the usual way
		this.propName = extractPropertyName();
		this.matchedProp = extractExactProperty();
	}
	
	@Override
	protected void saveCeTokens() {
		this.parent.addNormalCeToken(TOKEN_HAS);

		if (this.datatypePreamble != null) {
			this.parent.addNormalCeToken(this.datatypePreamble);
		}
		
		if (this.rangeName.equals(TOKEN_VALUE)) {
			if (this.isConcatenation) {
				//Currently this is identical for concatenation tokens and normal values
				this.parent.addValueCeToken(this.rangeValue, this.quoteType);
			} else {
				this.parent.addValueCeToken(this.rangeValue, this.quoteType);				
			}
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
				if (this.rangeValue != null) {
					this.parent.addInstanceCeToken(this.rangeValue, this.quoteType);
				}
			}
		}

		this.parent.addNormalCeToken(TOKEN_AS);
		
		if (!this.matchedProp.isSpecialOperatorProperty()) {
			this.parent.addPropetyCeTokens(this.matchedProp);
		} else {
			this.parent.addNormalCeToken(this.matchedProp.getPropertyName());
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

		return "(FN" + stylePart + ")";		
	}

}
