package com.ibm.ets.ita.ce.store.parsing.model;
/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.handleSpecialMarkersAndDecode;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSequence;
import com.ibm.ets.ita.ce.store.model.CeSpecialProperty;
import com.ibm.ets.ita.ce.store.parsing.tokenizer.TokenizerFactSentence;

public abstract class TokenizerPropertyClause extends TokenizerNormalClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static String TOKEN_BRACKET_OPEN = "(";
	private static String TOKEN_BRACKET_CLOSE = ")";
	protected static final String TOKEN_CONCAT = "<>";

	protected static final String STYLE_OBJ = "OBJ";
	protected static final String STYLE_DAT = "DAT";

	protected String style = null;
	protected ArrayList<CeProperty> lastCandidateProperties = null;
	protected String rangeName = null;
	protected CeConcept rangeCon = null;
	protected String propName = null;
	protected CeProperty matchedProp = null;
	protected String rangeValue = null;
	protected CeSequence rangeSequence = null;
	protected String quoteType = null;
	protected boolean negatedRange = false;
	protected boolean isConcatenation = false;

	protected abstract void saveCeTokens();

	public TokenizerPropertyClause(TokenizerFactSentence pParent, ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, CeConcept pDomainCon, boolean pIsPatternClause) {
		super(pParent, pRawTokens, pDelimiter, pLastClause, pDomainCon, pIsPatternClause);
	}

	@Override
	protected void testForCompleteness() {
		if (reachedEndOfTokens()) {
			if ((this.domainCon != null) || this.isPatternClause) {
				if (this.matchedProp != null) {
					if (this.rangeName != null) {
						if ((this.rangeName.equals(TOKEN_VALUE)) || (this.rangeCon != null)) {
							if ((this.rangeValue != null) || (this.rangeSequence != null)) {
								this.complete = true;
							} else {
								//A clause can be valid without a range value if it is negated
								this.complete = this.negatedRange;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String explainIncompleteness() {
		String result = null;

		result = explainIfNotAllTokensProcessed(result);

		if ((this.domainCon == null) && (!this.isPatternClause)) {
			result = appendReason(result, "No domain concept specified");
		} else {
			if (this.propName == null) {
				result = appendReason(result, "No property name detected");
			} else {
				if (this.matchedProp == null) {
					result = appendReason(result, "No property found named '" + this.propName + "'");
				} else {
					if (this.rangeName == null) {
						result = appendReason(result, "No range name detected");
					} else {
						if ((!this.rangeName.equals(TOKEN_VALUE)) && (this.rangeCon == null)) {
							result = appendReason(result, "No concept found for range name '" + this.rangeName + "'");
						} else {
							if ((this.rangeSequence == null) && (this.rangeValue == null)) {
								if (!this.negatedRange) {
									result = appendReason(result, "No range value or sequence detected and range is not negated");
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	protected String extractPropertyName() {
		String result = null;
		boolean carryOn = true;

		while (carryOn) {
			String currToken = this.getCurrentToken();
			
			if (currToken != null) {
				if (result == null) {
					result = currToken;
				} else {
					result += " ";
					result += currToken;
				}
				
				ArrayList<CeProperty> possProps = calculatePropertiesStartingWith(result);
				ArrayList<CeProperty> actualProps = calculatePropertiesNamed(result);

				if (!actualProps.isEmpty()) {
					//One or more properties are matched
					if (possProps.isEmpty()) {
						//Simple - One or more properties are matched and there are no other possibilities.
						//Return the matched properties
						this.lastCandidateProperties = actualProps;
						carryOn = false;
					} else {
						//There are other possible properties that start in this way.
						//Have a look to see if later matches is possible
						if (!isPropertyMatchedLater(this.rawTokens, this.finalTokenPos, result)) {
							//No longer property name match is possible, so go with this one
							this.lastCandidateProperties = actualProps;
							carryOn = false;
						}
					}
				} else {
					//No properties are matched
					if (possProps.isEmpty()) {
						//Simple - No properties are matched and there are no other possibilities.
						//As a final resort check the special properties
						CeSpecialProperty specProp = CeSpecialProperty.getSpecialOperatorPropertyNamed(this.ac, result);
						
						if (specProp != null) {
							//Use the special property
							this.lastCandidateProperties = new ArrayList<CeProperty>();
							this.lastCandidateProperties.add(specProp);
						} else {
							//Return a failure
							this.lastCandidateProperties = null;
						}
						carryOn = false;
					} else {
						//No properties are matched but there are other possible properties to continue.
						//Carry on as normal
					}
				}
			} else {
				carryOn = false;
			}
		}

		return result;
	}

	private boolean isPropertyMatchedLater(ArrayList<String> pTokens, int pPos, String pPropNameSoFar) {
		boolean result = false;

		if (pTokens.size() > pPos) {
			String newPropName = pPropNameSoFar + " " + pTokens.get(pPos);
			result = (!calculatePropertiesNamed(newPropName).isEmpty());

			if (!result) {
				result = isPropertyMatchedLater(pTokens, (pPos + 1), newPropName);
			}
		}

		return result;
	}

	protected ArrayList<CeProperty> calculatePropertiesStartingWith(String pPropName) {
		return this.ac.getIndexedEntityAccessor().calculatePropertiesStartingWith(this.domainCon, pPropName + " ");
	}

	protected ArrayList<CeProperty> calculatePropertiesNamed(String pPropName) {
		return this.ac.getIndexedEntityAccessor().calculatePropertiesNamed(this.domainCon, pPropName);
	}

	protected CeProperty extractExactProperty() {
		CeProperty result = null;

		if (this.lastCandidateProperties != null) {	
			for (CeProperty thisProp : this.lastCandidateProperties) {
				if (this.rangeCon != null) {
					if (thisProp.isSpecialOperatorProperty()) {
						result = thisProp;
					} else {
						if (thisProp.isObjectProperty()) {
							if (this.rangeCon.equalsOrHasParent(thisProp.getRangeConcept())) {
								result = thisProp;
							}
						}
					}
				} else {
					if (thisProp.isDatatypeProperty()) {
						result = thisProp;
					}
				}
			}
		}

		return result;
	}
	
	protected void handleRangeValue() {
		String rawInst = null;
		
		if (this.finalTokenPos < this.rawTokens.size()) {
			rawInst = this.getCurrentToken();

			if ((!this.negatedRange) || (this.negatedRange && (!rawInst.equals(TOKEN_AS)))) {
				if (rawInst.equals(TOKEN_BRACKET_OPEN)) {
					//TODO: Implement sequences properly
					String bracketedPhrase = getWholeBracketedPhrase();
					this.rangeSequence = CeSequence.createFrom(bracketedPhrase);
				} else {
					String strippedInst = stripQuotesFrom(rawInst);

					//If the length changed then it did have delimiters so store which character was used
					if (rawInst.length() != strippedInst.length()) {
						this.quoteType = rawInst.substring(0, 1);
					}

					//Finally decode the instance name as it may contain escaped characters
					this.rangeValue = replaceMarkersAndDecode(strippedInst);
				}
			} else {
				//This is a negated range so no "id" is expected so therefore we need to go back one position so the
				//"as" will be processed in the correct position.
				//This occurs in rationale for negated clauses, e.g.
				//   ...
				// because
				//   has, no, date, as, publish, date
				this.finalTokenPos--;
			}
		} else {
			if (!this.negatedRange) {
				reportError("Missing property value for non-negated property");
			}
		}
	}
	
	private String replaceMarkersAndDecode(String pRawVal) {
		String result = pRawVal;
		
		result = handleSpecialMarkersAndDecode(this.ac, result);

		return result;
	}

	private String getWholeBracketedPhrase() {
		StringBuilder sb = new StringBuilder();
		sb.append(TOKEN_BRACKET_OPEN);
		sb.append(" ");

		boolean carryOn = true;
		
		while (carryOn) {
			String thisToken = this.getCurrentToken();
			
			if (!thisToken.equals(TOKEN_BRACKET_CLOSE)) {
				sb.append(thisToken);
				sb.append(" ");
				carryOn = this.rawTokens.size() > this.finalTokenPos;
			} else {
				sb.append(thisToken);
				carryOn = false;
			}
		}
		
		return sb.toString();
	}

	@Override
	public void save(boolean pTokensOnly) {
		if (this.rangeSequence == null) {
			if (!pTokensOnly) {
				//Save the main information
				if (this.isConcatenation) {
					this.sen.addConcatenatedValue(this.propName, this.rangeValue);
				} else {
					//This is a normal property
					if (this.style == STYLE_DAT) {
						this.sen.addDatatypeProperty(this.ac, this.propName, this.rangeValue, this.matchedProp, (this.quoteType != null), this.negatedRange);
					} else {
						this.sen.addObjectProperty(this.ac, this.propName, this.rangeName, this.rangeValue, (this.quoteType != null), this.negatedRange);
					}
				}
			}

			//Save the CE tokens
			if ((this.delimiter != null) && (!this.delimiter.isEmpty())) {
				this.parent.addDelimiterCeToken(this.delimiter);
			}

			saveCeTokens();
		} else {
			//TODO: Implement sequences properly
		}
	}

	protected boolean isNormalDeterminerToken() {
		boolean result = false;

		if (this.finalTokenPos < this.rawTokens.size()) {
			result = this.rawTokens.get(this.finalTokenPos).equals(TOKEN_THE);
		}

		return result;
	}
	
	protected boolean isNegatedDeterminerToken() {
		boolean result = false;

		if (this.finalTokenPos < this.rawTokens.size()) {
			result = (this.isPatternClause) && (this.rawTokens.get(this.finalTokenPos).equals(TOKEN_NO));
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(formattedQualifier());

		sb.append(" rangeName=" + this.rangeName);
		sb.append(", rangeCon=" + this.rangeCon);
		sb.append(", propName=" + this.propName);
		sb.append(", matchedProp=" + this.matchedProp);
		sb.append(", rangeValue=" + this.rangeValue);
		sb.append(", delimiter=" + this.delimiter);
		sb.append(", rawTokens=" + this.rawTokens.toString());

		return sb.toString();
	}

}