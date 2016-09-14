package com.ibm.ets.ita.ce.store.parsing.tokenizer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_SEQUENCE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.MiscNames.PROPDEF_PREFIX;
import static com.ibm.ets.ita.ce.store.names.MiscNames.PROPDEF_SUFFIX;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_CONCEPT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_CONNECTOR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_NORMAL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_PROP;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_1;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_A;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AN;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AND;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CLOSEPAR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COMMA;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CONCEPTUALISE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CONCEPTUALIZE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DEFINE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_EXACTLY;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_HAS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_IDENTIFIER;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_IS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_MOST;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_ONE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_OPENPAR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SPACE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THAT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_TILDE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_VALUE;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceModel;

public class TokenizerModelSentence extends TokenizerSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final int TEST_SKIP = 0;
	private static final int TEST_SKIP_TORANGE = 1;
	private static final int TEST_RETRY = -1;
	private static final int TEST_SEEKCONCEPTSTART = -2;
	private static final int TEST_SEEKCONCEPT = -3;
	private static final int TEST_SEEKVARNAME_CONCEPT = -4;
	private static final int TEST_SEEKVARNAME_PROPERTY = -5;
	private static final int TEST_SEEKPROPERTYSTART = -6;
	private static final int TEST_SEEKPROPERTY = -7;
	private static final int TEST_SEEKRANGE = -8;
	private static final int TEST_SEEKPHRASE1_A = -10;
	private static final int TEST_SEEKPHRASE1_B = -11;
	private static final int TEST_SEEKPHRASE2 = -20;
	private static final int TEST_SEEKPHRASE2A_A = -21;
	private static final int TEST_SEEKPHRASE2A_B = -22;
	private static final int TEST_SEEKPHRASE2A_C = -23;
	private static final int TEST_SEEKPHRASE2A_D = -24;
	private static final int TEST_SEEKPHRASE2B_A = -25;
	private static final int TEST_SEEKPHRASE2B_B = -26;
	private static final int TEST_SEEKPHRASE3 = -30;
	private static final int TEST_IDENTIFIERHACK = -40;
	private static final int TEST_END = -100;

	private static final String MARKER_BLANK_PROPVAR = "(none)";	//TODO: Abstract this

	private static final int MODE_UNKNOWN = 0;
	private static final int MODE_FN = 1;
	private static final int MODE_VS = 2;

	private int sentenceMode = MODE_UNKNOWN;
	private String lastToken = ES;
	private String phraseToken = ES;
	private String conceptToken = ES;
	private String propertyToken = ES;
	private String rangeToken = ES;
	private String propertyCeName = ES;
	private String propertyVarName = ES;		//No longer used
	private String propertyRangeName = ES;
	private boolean insideTilde = false;
	private boolean singleCardinality = false;
	private boolean exactCardinality = false;
	private boolean secondaryConceptStored = false;
	private String potentialSecondaryConcept = ES;
	private boolean lastConNameSequence = false;
	private boolean inSequence = false;

	@Override
	protected String tokenizerType() {
		return "Model";
	}

	@Override
	protected BuilderSentenceModel getTargetSentence() {
		return (BuilderSentenceModel)superTargetSentence();
	}

	@Override
	public void doTokenizing() {
		initialiseBuffers();
		createFinalTokensForModelSentence();
		if (!this.secondaryConceptStored) {
			//No explicit parent (secondary concept) was specified
			createParentRelationship(null);
		}
		if (!getTargetSentence().hasTargetConcept()) {
			getTargetSentence().hasError(this.ac, "Unable to identify target concept for: " + getTargetSentence().getSentenceText());
		}
	}

	private void initialiseBuffers() {
		this.sentenceMode = MODE_UNKNOWN;
		this.lastToken = ES;
		this.phraseToken = ES;
		this.conceptToken = ES;
		this.propertyToken = ES;
		this.rangeToken = ES;
		this.propertyCeName = ES;
		this.propertyVarName = ES;
		this.propertyRangeName = ES;
		this.potentialSecondaryConcept = ES;
		this.lastConNameSequence = false;
		this.inSequence = false;
		this.conQualifier = ES;
	}

	private void createFinalTokensForModelSentence() {
		int testResult = TEST_SEEKPHRASE1_A;
		this.insideTilde = false;

		ArrayList<String> rawTokens = getTargetSentence().getRawTokens();
		for (int i = 0; i < rawTokens.size(); i++) {
			String thisRawToken = rawTokens.get(i);

			// If the previous execution was set to skip, this must be reset to retry here
			if (testResult == TEST_SKIP) {
				testResult = TEST_RETRY;
			} else if (testResult == TEST_SKIP_TORANGE) {
				testResult = TEST_SEEKRANGE;
			}

			if ((testResult == TEST_SEEKCONCEPT) || (testResult == TEST_SEEKCONCEPTSTART)) {
				testResult = processConcept(thisRawToken, testResult);
			} else if ((testResult == TEST_SEEKPROPERTY) || (testResult == TEST_SEEKPROPERTYSTART)) {
				testResult = processProperty(thisRawToken, testResult);
			} else if ((testResult == TEST_SEEKVARNAME_CONCEPT) || (testResult == TEST_SEEKVARNAME_PROPERTY)) {
				testResult = processVariableName(thisRawToken, testResult);
			} else if (testResult == TEST_SEEKRANGE) {
				testResult = processRange(thisRawToken, testResult, i);
			} else if (testResult == TEST_IDENTIFIERHACK) {
				testResult = processIdentifierHack(thisRawToken, testResult);
			}

			// Test 1: Look for phrase 1 ('conceptualise a|an')
			if ((testResult == TEST_SEEKPHRASE1_A) || (testResult == TEST_SEEKPHRASE1_B)) {
				testResult = doTest1(thisRawToken, testResult);
			}
			// Test 2a: Look for phrase 2a ('that|and has the|at most one|1')
			if ((testResult == TEST_RETRY) || (testResult == TEST_SEEKPHRASE2) || (testResult == TEST_SEEKPHRASE2A_A) || (testResult == TEST_SEEKPHRASE2A_B) || (testResult == TEST_SEEKPHRASE2A_C)  || (testResult == TEST_SEEKPHRASE2A_D)) {
				testResult = doTest2a(thisRawToken, testResult);
			}
			// Test 2b: Look for phrase 2b ('that|and is a')
			if ((testResult == TEST_RETRY) || (testResult == TEST_SEEKPHRASE2) || (testResult == TEST_SEEKPHRASE2B_A) || (testResult == TEST_SEEKPHRASE2B_B)) {
				testResult = doTest2b(thisRawToken, testResult);
			}
			// Test 3: Look for phrase 3 ('as')
			if ((testResult == TEST_RETRY) || (testResult == TEST_SEEKPHRASE3)) {
				testResult = doTest3(thisRawToken, testResult);
			}
			// Test 4: Look for phrase 4 ('and ~')
			if (testResult == TEST_RETRY) {
				testResult = doTest4(thisRawToken, testResult);
			}

			if (testResult == TEST_RETRY) {
				if (!this.potentialSecondaryConcept.isEmpty()) {
					if ((thisRawToken.equals(TOKEN_IS)) || (thisRawToken.equals(TOKEN_HAS)) || (thisRawToken.equals(TOKEN_TILDE))) {
						this.conceptToken = this.potentialSecondaryConcept;
						this.potentialSecondaryConcept = ES;
						storeSecondaryConcept();
						addToPhraseToken(TOKEN_AND);
						getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, ES, TOKEN_AND);
						if (thisRawToken.equals(TOKEN_IS)) {
							addToPhraseToken(thisRawToken);
							getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_IS);
							testResult = TEST_SEEKPHRASE2B_B;
						} else if (thisRawToken.equals(TOKEN_HAS)) {
							addToPhraseToken(thisRawToken);
							getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_HAS);
							testResult = TEST_SEEKPHRASE2A_B;
						} else if (thisRawToken.equals(TOKEN_TILDE)) {
							this.phraseToken = ES;
							this.sentenceMode = MODE_VS;
							testResult = TEST_SEEKPROPERTY;
							getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
						} else {
							//Not possible
						}
					} else {
						//An unexpected token was encountered, so if the secondary concept is incomplete try adding in with an 'and'
						//Hack to handle 'and' in concept names
						this.conceptToken = this.potentialSecondaryConcept + TOKEN_SPACE + TOKEN_AND + TOKEN_SPACE + thisRawToken;
						this.potentialSecondaryConcept = ES;
						testResult = TEST_SEEKCONCEPT;
					}
				} else {
					getTargetSentence().hasError(this.ac, "100: Unhandled token '" + thisRawToken + "' for sentence: " + getTargetSentence().getSentenceText());
				}
			}

			this.lastToken = thisRawToken;
		}

		if (!this.conceptToken.isEmpty()) {
			processConcept(ES, TEST_END);
		}

		if (!this.propertyToken.isEmpty()) {
			getTargetSentence().hasError(this.ac, "111: Unknown property encountered on model sentence '" + this.propertyToken + "'");			
		}

		if (!this.rangeToken.isEmpty()) {
			processRange(ES, TEST_END, -1);
		}

		if (!this.phraseToken.isEmpty()) {
			getTargetSentence().hasError(this.ac, "112: Unknown phrase encountered '" + this.phraseToken + "'");			
		}
		
		if (this.insideTilde) {
			getTargetSentence().hasError(this.ac, "Unclosed tilde");
		}

		//Finished, so notify the sentence that validation is complete
		getTargetSentence().validationComplete();
	}

	private int processConcept(String pRawToken, int pMarker) {		
		int result = TEST_RETRY;

		if (isInFnMode()) {
			result = processFnConcept(pRawToken, pMarker);
		} else if (isInVsMode()) {
			result = processVsConcept(pRawToken, pMarker);
		} else {
			getTargetSentence().hasError(this.ac, "101: Unknown sentence mode (" + Integer.toString(this.sentenceMode) + ") encountered during concept tokenizing");
			result = TEST_RETRY;
		}

		return result;
	}

	private boolean isInVsMode() {
		return (this.sentenceMode == MODE_VS);
	}

	private boolean isInFnMode() {
		return (this.sentenceMode == MODE_FN);
	}

	private int processFnConcept(String pRawToken, int pMarker) {
		int result = TEST_RETRY;

		if (pMarker == TEST_SEEKCONCEPTSTART) {
			//Looking for a starting delimiter
			if (pRawToken.equals(TOKEN_TILDE)) {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
				this.insideTilde = true;
				result = TEST_SEEKCONCEPT;
			} else {
				getTargetSentence().hasError(this.ac, "102: Error detecting concept.  No starting ~ character found.");
				result = TEST_RETRY;
			}
		} else if (pMarker == TEST_SEEKCONCEPT) {
			if (pRawToken.equals(TOKEN_TILDE)) {
				result = TEST_SEEKVARNAME_CONCEPT;
				this.insideTilde = false;
				//This denotes the end of the concept name so store it
				if (getTargetSentence().hasTargetConcept()) {
					storeSecondaryConcept();		//Store the secondary concept
				} else {
					storeTargetConcept();		//Store the target concept
				}
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
			} else if (pRawToken.equals(TOKEN_AND)) {
				if (!this.insideTilde) {
					this.potentialSecondaryConcept = this.conceptToken;		//Store the secondary concept
					this.conceptToken = ES;
					result = TEST_SKIP;
				} else {
					result = TEST_SEEKCONCEPT;
					addToConceptToken(pRawToken);
				}
			} else {
				result = TEST_SEEKCONCEPT;
				addToConceptToken(pRawToken);
			}
		} else if (pMarker == TEST_END) {
			storeSecondaryConcept();
		} else {
			getTargetSentence().hasError(this.ac, "103: Unknown seek mode (" + Integer.toString(pMarker) + ") encountered during FN concept tokenizing");
		}

		return result;
	}

	private int processVsConcept(String pRawToken, int pMarker) {
		int result = TEST_RETRY;

		if (pMarker == TEST_SEEKCONCEPTSTART) {
			//Don't need to process the token here as we are one step behind for VS processing
			result = TEST_SEEKCONCEPT;
		} else if (pMarker == TEST_SEEKCONCEPT) {
			if (pRawToken.equals(TOKEN_TILDE)) {
				//This denotes the end of the concept name so store it
				storeTargetConcept();		//Store the target concept
				result = processVariableName(this.lastToken, TEST_SEEKVARNAME_CONCEPT);
			} else {
				result = TEST_SEEKCONCEPT;
				addToConceptToken(this.lastToken);		//Add the last token as we are one step behind for VS processing
			}
		} else {
			getTargetSentence().hasError(this.ac, "104: Unknown mode (" + Integer.toString(pMarker) + ") encountered during VS concept tokenizing");
		}

		return result;
	}

	private int processVariableName(String pRawToken, int pMarker) {
		int result = TEST_RETRY;

		//Token name is easy - it can only be a single token
		if (pMarker == TEST_SEEKVARNAME_CONCEPT) {
			storeConceptVariableName(pRawToken);		//Store the concept variable name
			if (isInFnMode()) {
				result = TEST_SKIP;
			} else {
				result = TEST_SEEKPROPERTYSTART;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
			}
		} else if (pMarker == TEST_SEEKVARNAME_PROPERTY) {
			storePropertyVariableName(pRawToken);		//Store the property variable name
			result = TEST_SKIP;
		} else {
			getTargetSentence().hasError(this.ac, "105: Unknown mode (" + Integer.toString(pMarker) + ") encountered during variable name tokenizing");
		}

		return result;
	}

	private int processProperty(String pRawToken, int pMarker) {
		int result = TEST_RETRY;

		if (isInFnMode()) {
			result = processFnProperty(pRawToken, pMarker);
		} else if (isInVsMode()) {
			result = processVsProperty(pRawToken, pMarker);
		} else {
			getTargetSentence().hasError(this.ac, "106: Unknown mode (" + Integer.toString(pMarker) + ") encountered during property tokenizing");
			result = TEST_RETRY;
		}

		return result;
	}

	private int processFnProperty(String pRawToken, int pMarker) {
		int result = pMarker;

		if (pMarker == TEST_SEEKPROPERTYSTART) {
			//Looking for a starting delimiter
			if(pRawToken.equals(TOKEN_TILDE)) {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
				result = TEST_SEEKPROPERTY;
			} else {
				result = TEST_RETRY;
				getTargetSentence().hasError(this.ac, "107: Error detecting property.  No starting ~ character found.");
			}
		} else if (pMarker == TEST_SEEKPROPERTY) {
			if (pRawToken.equals(TOKEN_TILDE)) {
				storeProperty();		//Store the property
				doPropertyProcessing();
				result = TEST_SKIP;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
			} else {
				result = TEST_SEEKPROPERTY;
				addToPropertyToken(pRawToken);
			}
		} else {
			getTargetSentence().hasError(this.ac, "108: Unknown mode (" + Integer.toString(pMarker) + ") encountered during FN property tokenizing");
		}

		return result;
	}

	private int processVsProperty(String pRawToken, int pMarker) {
		int result = TEST_SKIP;

		if (pRawToken.equals(TOKEN_TILDE)) {
			//end of property
			storeProperty();
			getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
			result = TEST_SKIP_TORANGE;
		} else {
			addToPropertyToken(pRawToken);
			result = pMarker;
		}

		return result;
	}

	private int processRange(String pRawToken, int pMarker, int pPos) {
		int result = pMarker;

		if (this.inSequence) {
			if (pRawToken.equals(TOKEN_CLOSEPAR)) {
				this.inSequence = false;
				storePropertyVariableName(MARKER_BLANK_PROPVAR);
				result = TEST_IDENTIFIERHACK;
			}
			getTargetSentence().addFinalToken(SCELABEL_CONCEPT, ES, this.ac.getModelBuilder().getCachedStringValueLevel1(pRawToken));
		} else {
			if (this.lastConNameSequence) {
				if (pRawToken.equals(TOKEN_OPENPAR)) {
					getTargetSentence().addFinalToken(SCELABEL_CONCEPT, ES, CON_SEQUENCE);
					this.propertyRangeName = CON_SEQUENCE;
					this.inSequence = true;
				}
			}
			this.lastConNameSequence = false;

			if (this.inSequence) {
				//In a sequence, so ignore everything until out of it
				//TODO: Implement sequence processing properly
				getTargetSentence().addFinalToken(SCELABEL_CONCEPT, ES, this.ac.getModelBuilder().getCachedStringValueLevel1(pRawToken));
			} else {
				//Normal processing
				if (isInFnMode()) {
					result = processFnRange(pRawToken);
				} else {
					result = processVsRange(pRawToken, pMarker, pPos);
				}

				if (pRawToken.equals(CON_SEQUENCE)) {
					this.lastConNameSequence = true;
				}
			}
		}

		return result;
	}

	private int processFnRange(String pRawToken) {
		int result = TEST_RETRY;

		if (pRawToken.equals(TOKEN_VALUE)) {
			storeRangeValue(pRawToken);		//Store the range (value)
			result = TEST_SEEKVARNAME_PROPERTY;
		} else {
			if (!pRawToken.equals(TOKEN_AS)) {
				if (this.rangeToken.isEmpty()) {
					//There is no rangeToken yet so check for special tokens
					if ((!this.lastToken.equals(TOKEN_THE)) && (!this.lastToken.equals(TOKEN_ONE)) && (!this.lastToken.equals(TOKEN_1))) {
						addToRangeToken(this.lastToken);
					}
				} else {
					//There is already a rangeToken which means we are part-way through a concept name so always add the token
					addToRangeToken(this.lastToken);
				}
				result = TEST_SEEKRANGE;
			} else {
				storeRangeConcept();		//Store the range concept
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_AS);
				result = TEST_SEEKPROPERTYSTART;		
			}
		}

		return result;
	}

	private int processVsRange(String pRawToken, int pMarker, int pPos) {
		int result = TEST_RETRY;

		if ((pRawToken.equals(TOKEN_THE)) || (pRawToken.equals(TOKEN_AT)) || (pRawToken.equals(TOKEN_EXACTLY))) {
			if (pRawToken.equals(TOKEN_THE)) {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_THE);
			} else if (pRawToken.equals(TOKEN_AT)) {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_AT);
			} else if (pRawToken.equals(TOKEN_EXACTLY)) {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_EXACTLY);
			}
			this.exactCardinality = false;
			this.singleCardinality = false;
			result = TEST_SEEKRANGE;
		} else if ((pRawToken.equals(TOKEN_MOST)) && (this.lastToken.equals(TOKEN_AT))) {
			getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_MOST);
			this.exactCardinality = false;
			this.singleCardinality = false;
			result = TEST_SEEKRANGE;
		} else if (((pRawToken.equals(TOKEN_ONE)) || (pRawToken.equals(TOKEN_1))) && (this.lastToken.equals(TOKEN_MOST))) {
			if (pRawToken.equals(TOKEN_ONE)) {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_ONE);
			} else {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_1);
			}
			this.exactCardinality = false;
			this.singleCardinality = true;
			result = TEST_SEEKRANGE;
		} else if (((pRawToken.equals(TOKEN_ONE)) || (pRawToken.equals(TOKEN_1))) && (this.lastToken.equals(TOKEN_EXACTLY))) {
			if (pRawToken.equals(TOKEN_ONE)) {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_ONE);
			} else {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_1);
			}
			this.exactCardinality = true;
			this.singleCardinality = true;
			result = TEST_SEEKRANGE;
		} else if ((pRawToken.equals(TOKEN_AND)) || (pMarker == TEST_END)) {
			if ((pPos != -1) && (getTargetSentence().getRawTokens().size() > (pPos + 1))) {
				String nextWord = getTargetSentence().getRawTokens().get(pPos + 1);

				//Added by Dave Braines - if the next word is not a tilde or has then this delimiter is actually part of a concept name
				if (nextWord.equals(TOKEN_TILDE) || nextWord.equals(TOKEN_HAS)) {
					storeRangeConcept();
					doPropertyProcessing();
					result = TEST_RETRY;
				} else {
					result = pMarker;
				}
			} else {
				storeRangeConcept();
				doPropertyProcessing();
				result = TEST_RETRY;
			}
		} else {
			if ((!this.lastToken.equals(TOKEN_THE)) && (!this.lastToken.equals(TOKEN_ONE)) && (!this.lastToken.equals(TOKEN_1))) {
				addToRangeToken(this.lastToken);
			}
			result = TEST_SEEKRANGE;
		}

		return result;
	}

	private int processIdentifierHack(String pRawToken, int pMarker) {
		//TODO: Can this be removed at some point?
		//There is a special case when using 'sequence' definitions in conceptualise sentences:
		//They will always be followed by the special property name 'identifier', and since this is a pre-existing
		//property the name is not enclosed in tilde characters.
		//This syntax is required for the prolog parser, but not by the CE store
		int result = pMarker;

		if (pRawToken.equals(TOKEN_AS)) {
			//Store the 'as' and continue
			getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_AS);
		} else if (pRawToken.equals(TOKEN_IDENTIFIER)) {
			this.propertyToken = pRawToken;
			storeProperty();
			doPropertyProcessing();
			result = TEST_SKIP;
		} else {
			reportError("Unexpected failure in identifier name processing (" + pRawToken + ")... the word 'identifier' was not found after the sequence, for:" + getTargetSentence().getSentenceText(), this.ac);
		}

		return result;
	}

	private int doTest1(String pRawToken, int pMarker) {		
		//Seeking phrase 'conceptualise {a|an}|the'
		int result = pMarker;

		if (pMarker == TEST_SEEKPHRASE1_A) {
			if (pRawToken.equals(TOKEN_CONCEPTUALISE) || pRawToken.equals(TOKEN_CONCEPTUALIZE) || pRawToken.equals(TOKEN_DEFINE)) {
				this.phraseToken = pRawToken;
				if (pRawToken.equals(TOKEN_CONCEPTUALISE)) {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_CONCEPTUALISE);
				} else if (pRawToken.equals(TOKEN_CONCEPTUALIZE)) {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_CONCEPTUALIZE);
				} else {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_DEFINE);
				}
				result = TEST_SEEKPHRASE1_B;		// Continue the test (looking for a|an)
			} else {
				getTargetSentence().hasError(this.ac, "113: Failed to process model sentence - first word not conceptualise");
				result = TEST_RETRY;	// Fail the test)
				this.phraseToken = ES;
			}
		} else if (pMarker == TEST_SEEKPHRASE1_B) { 
			if ((pRawToken.equals(TOKEN_A) || pRawToken.equals(TOKEN_AN))) {
				this.conQualifier = pRawToken;
				this.sentenceMode = MODE_FN;
				addToPhraseToken(pRawToken);
				if (pRawToken.equals(TOKEN_A)) {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_A);
				} else {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_AN);
				}
				result = TEST_SEEKCONCEPTSTART;	// Complete the test (looking for a functional noun style concept)
				this.phraseToken = ES;
			} else if (pRawToken.equals(TOKEN_THE)) {
				this.sentenceMode = MODE_VS;
				addToPhraseToken(pRawToken);
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_THE);
				result = TEST_SEEKCONCEPTSTART;	// Complete the test (looking for a verb singular style concept)
				this.phraseToken = ES;
			} else {
				getTargetSentence().hasError(this.ac, "114: Failed tokenizing multi-part token (conceptualise a|an) with '" + this.phraseToken + "'");
				result = TEST_RETRY;	// Fail the test)
				this.phraseToken = ES;
			}
		} else {
			getTargetSentence().hasError(this.ac, "115: Failed tokenizing multi-part token (conceptualise a|an) with '" + this.phraseToken + "'");
			result = TEST_RETRY;	// Fail the test)
			this.phraseToken = ES;
		}
 
		return result;
	}

	private int doTest2a(String pRawToken, int pMarker) {
		//Seeking phrase 'that | and has the | at most one | exactly one'
		int result = pMarker;
		if ((pMarker == TEST_RETRY) || (pMarker == TEST_SEEKPHRASE2)) {
			if ((pRawToken.equals(TOKEN_THAT)) || (pRawToken.equals(TOKEN_AND))) {
				addToPhraseToken(pRawToken);
				if (pRawToken.equals(TOKEN_THAT)) {
					getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, ES, TOKEN_THAT);
				} else {
					getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, ES, TOKEN_AND);
				}
				result = TEST_SEEKPHRASE2A_A;
			} else {
				//First token, so don't report an error
				result = TEST_RETRY;
			}
		} else if (pMarker == TEST_SEEKPHRASE2A_A) {
			if (pRawToken.equals(TOKEN_HAS)) {
				addToPhraseToken(pRawToken);
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_HAS);
				result = TEST_SEEKPHRASE2A_B;
			} else if (pRawToken.equals(TOKEN_TILDE)) {
				this.phraseToken = ES;
				this.sentenceMode = MODE_VS;
				result = TEST_SEEKPROPERTY;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
			} else {
				//No error to report (yet) - there are two fragments that begin with 'that'
				result = TEST_SEEKPHRASE2B_A;
			}
		} else if (pMarker == TEST_SEEKPHRASE2A_B) {
			if (pRawToken.equals(TOKEN_THE)) {
				addToPhraseToken(pRawToken);
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_THE);
				this.phraseToken = ES;
				this.exactCardinality = false;
				this.singleCardinality = false;
				result = TEST_SEEKRANGE;
			} else if (pRawToken.equals(TOKEN_AT)) {
				addToPhraseToken(pRawToken);
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_AT);
				result = TEST_SEEKPHRASE2A_C;
			} else if (pRawToken.equals(TOKEN_EXACTLY)) {
				this.exactCardinality = true;
				addToPhraseToken(pRawToken);
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_EXACTLY);
				result = TEST_SEEKPHRASE2A_D;
			} else {
				getTargetSentence().hasError(this.ac, "116: Failed tokenizing multi-part token (that | and has the | at most one | exactly one) with '" + this.phraseToken + "'");
				result = TEST_RETRY;
			}
		} else if (pMarker == TEST_SEEKPHRASE2A_C) {
			if (pRawToken.equals(TOKEN_MOST)) {
				addToPhraseToken(pRawToken);
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_MOST);
				result = TEST_SEEKPHRASE2A_D;
			} else {
				getTargetSentence().hasError(this.ac, "116: Failed tokenizing multi-part token (that | and has the | at most one | exactly one) with '" + this.phraseToken + "'");
				result = TEST_RETRY;
			}
		} else if (pMarker == TEST_SEEKPHRASE2A_D) {
			if ((pRawToken.equals(TOKEN_ONE)) || (pRawToken.equals(TOKEN_1))) {
				addToPhraseToken(pRawToken);
				if (pRawToken.equals(TOKEN_ONE)) {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_ONE);
				} else {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_1);
				}
				this.phraseToken = ES;
				this.singleCardinality = true;
				result = TEST_SEEKRANGE;
			} else {
				getTargetSentence().hasError(this.ac, "116: Failed tokenizing multi-part token (that|and has the|at most one) with '" + this.phraseToken + "'");
				result = TEST_RETRY;
			}
		} else {
			getTargetSentence().hasError(this.ac, "109: Unknown mode (" + Integer.toString(pMarker) + ") encountered during multi-word phrase tokenizing");
			result = TEST_RETRY;
		}

		return result;
	}

	private int doTest2b(String pRawToken, int pMarker) {
		//Seeking phrase 'that|and is a|an'
		int result = pMarker;
		if ((pMarker == TEST_RETRY) || (pMarker == TEST_SEEKPHRASE2)) {
			if ((pRawToken.equals(TOKEN_THAT)) || (pRawToken.equals(TOKEN_AND))) {
				addToPhraseToken(pRawToken);
				if (pRawToken.equals(TOKEN_THAT)) {
					getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, ES, TOKEN_THAT);
				} else {
					getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, ES, TOKEN_AND);
				}
				result = TEST_SEEKPHRASE2B_A;
			} else {
				//First token so don't report an error
				result = TEST_RETRY;
			}
		} else if (pMarker == TEST_SEEKPHRASE2B_A) {
			if (pRawToken.equals(TOKEN_IS)) {
				addToPhraseToken(pRawToken);
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_IS);
				result = TEST_SEEKPHRASE2B_B;
			} else {
				//No error to report here
				result = TEST_RETRY;
			}
		} else if (pMarker == TEST_SEEKPHRASE2B_B) {
			if ((pRawToken.equals(TOKEN_A)) || (pRawToken.equals(TOKEN_AN))) {
				this.conQualifier = pRawToken;
				addToPhraseToken(pRawToken);
				if (pRawToken.equals(TOKEN_A)) {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_A);
				} else {
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_AN);
				}
				this.phraseToken = ES;
				result = TEST_SEEKCONCEPT;
			} else {
				getTargetSentence().hasError(this.ac, "118: Failed tokenizing multi-part token (that|and is a) with '" + this.phraseToken + "'");
				result = TEST_RETRY;
			}
		} else {
			getTargetSentence().hasError(this.ac, "110: Unknown mode (" + Integer.toString(pMarker) + ") encountered during multi-word phrase tokenizing");
			result = TEST_RETRY;
		}

		return result;
	}

	private int doTest3(String pRawToken, int pMarker) {
		//Seeking phrase 'as'
		int result = pMarker;

		if (pRawToken.equals(TOKEN_AS)) {
			getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_AS);
			this.sentenceMode = MODE_FN;
			result = TEST_SEEKPROPERTYSTART;
		} else {
			//First token so don't report an error
			result = TEST_RETRY;
		}

		return result;
	}

	private int doTest4(String pRawToken, int pMarker) {
		//Seeking phrase 'that|and is a|an'
		int result = pMarker;
		if (isInVsMode()) {
			if (this.lastToken.equals(TOKEN_AND)) {
				if (pRawToken.equals(TOKEN_TILDE)) {
					this.phraseToken = ES;
					result = TEST_SEEKPROPERTY;
					this.sentenceMode = MODE_VS;
					getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_TILDE);
				}
			}
		}

		return result;
	}

	private String storeConceptToken() {
		String result = this.conceptToken;
		getTargetSentence().addFinalToken(SCELABEL_CONCEPT, ES, this.ac.getModelBuilder().getCachedStringValueLevel1(this.conceptToken));
		this.conceptToken = ES;

		return result;
	}

	private void storeTargetConcept() {
		boolean conceptExistsAlready = false;

		//Store the token
		String conceptName = storeConceptToken();

		if (this.mb.getConceptNamed(this.ac, conceptName) != null) {
			conceptExistsAlready = true;
		}
		//Store the target concept on the sentence
		CeConcept thisConcept = CeConcept.createOrRetrieveConceptNamed(this.ac, conceptName);

		if (thisConcept != null) {
			//Set the qualifiedWithAn status
			if(this.conQualifier.equals(TOKEN_AN)) {
				thisConcept.markAsQualifiedWithAn();
			}

			getTargetSentence().setTargetConceptNormal(thisConcept);

			if (!conceptExistsAlready) {
				//Only store the concept in the 'new' concept property if it did not exist already
				getTargetSentence().setNewConcept(thisConcept);
			}
		}
	}

	private void storeSecondaryConcept() {
		//Store the token
		String conceptName = storeConceptToken();
		this.secondaryConceptStored = true;

		CeConcept tgtCon = CeConcept.createOrRetrieveConceptNamed(this.ac, conceptName);

		checkForConceptQualifierMismatch(tgtCon, "secondary concept");	//TODO: Abstract this

		//Store the target concept on the sentence
		createParentRelationship(tgtCon);
	}

	private void createParentRelationship(CeConcept pParentConcept) {
		CeConcept targetConcept = getTargetSentence().getTargetConcept();

		if (targetConcept != null) {
			if (pParentConcept != null) {
				getTargetSentence().addNewParent(this.ac, pParentConcept);
			}
		} else {
			getTargetSentence().hasError(this.ac, "Unexpected null targetConcept when storing secondary concept in model sentence tokenization, for sentence:" + getTargetSentence().getSentenceText());
		}
	}

	private void storeConceptVariableName(String pRawToken) {
		if (pRawToken.equals(TOKEN_THAT)) {
			getTargetSentence().hasError(this.ac, "Concept variable not specified; the token 'that' has been incorrectly identified.");
		} else {
			//Store the token (not actually used any more)
			if (isInFnMode()) {
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, this.ac.getModelBuilder().getCachedStringValueLevel1(pRawToken));
			} else {
				getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, ES, this.ac.getModelBuilder().getCachedStringValueLevel1(pRawToken));
			}
		}
	}

	private void storePropertyVariableName(String pRawToken) {
		//Store the token
		this.propertyVarName = pRawToken;

		if (!this.propertyVarName.equals(MARKER_BLANK_PROPVAR)) {
			getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, this.ac.getModelBuilder().getCachedStringValueLevel1(this.propertyVarName));		
		}
	}

	private void storeProperty() {
		//Store the token
		this.propertyCeName = this.propertyToken;

		if (isInVsMode()) {
			storeTemporaryPropertyLabel();	//This will be updated later when all the required info is available (in doPropertyProcessing())
		}

		this.propertyToken = ES;
	}

	private void doPropertyProcessing() {
		int colType = CeProperty.RANGETYPE_DATATYPE;
		CeConcept rangeConcept = null;
		CeConcept targetConcept = getTargetSentence().getTargetConcept();

		if (isInFnMode()) {
			storeRealPropertyLabel();
		} else {
			updateTemporaryPropertyLabel();
		}

		if (!this.propertyRangeName.equals(TOKEN_VALUE)) {
			colType = CeProperty.RANGETYPE_OBJECT;
			rangeConcept = CeConcept.createOrRetrieveConceptNamed(this.ac, this.propertyRangeName);
			if (rangeConcept == null) {
				//If the range concept is not found check whether it is the same as the range concept.
				//If not then it may be defined later, but for now a shadow concept will be created
				if (this.propertyRangeName.equals(targetConcept.getConceptName())) {
					rangeConcept = targetConcept;
				} else {
					rangeConcept = CeConcept.createOrRetrieveConceptNamed(this.ac, this.propertyRangeName);
				}
			}
		} else {
			colType = CeProperty.RANGETYPE_DATATYPE;
		}

		int cardinality = -1;
		if (this.singleCardinality) {
			if (this.exactCardinality) {
				cardinality = CeProperty.CARDINALITY_SINGLE_EXACT;
			} else {
				cardinality = CeProperty.CARDINALITY_SINGLE;
			}
		} else {
			cardinality = CeProperty.CARDINALITY_MULTI;
		}

		if (targetConcept != null) {
			CeProperty thisProperty = null;
			if (isInFnMode()) {
				if (colType == CeProperty.RANGETYPE_DATATYPE) {
					thisProperty = CeProperty.createOrRetrieveFnDatatypeProperty(this.ac, targetConcept, this.propertyCeName, cardinality);
				} else {
					thisProperty = CeProperty.createOrRetrieveFnObjectProperty(this.ac, targetConcept, this.propertyCeName, cardinality, rangeConcept);
				}
			} else {
				if (colType == CeProperty.RANGETYPE_DATATYPE) {
					thisProperty = CeProperty.createOrRetrieveVsDatatypeProperty(this.ac, targetConcept, this.propertyCeName, cardinality);
				} else {
					thisProperty = CeProperty.createOrRetrieveVsObjectProperty(this.ac, targetConcept, this.propertyCeName, cardinality, rangeConcept);
				}
			}
			if (thisProperty != null) {
				getTargetSentence().addNewProperty(thisProperty);
			} else {
				reportError("Problem creating property '" + this.propertyCeName + "' for concept named '" + targetConcept.getConceptName() + "' from sentence: " + getTargetSentence().getSentenceText(), this.ac);
			}
		} else {
			getTargetSentence().hasError(this.ac, "Target concept has not been identified for property '" + this.propertyCeName + "', for sentence: " + getTargetSentence().getSentenceText());
		}
		
		this.sentenceMode = MODE_FN;
	}

	private void storeRealPropertyLabel() {
		storePropertyLabelNamed(calcPropLabel());
	}

	private void storeTemporaryPropertyLabel() {
		storePropertyLabelNamed(BuilderSentence.TEMP_PROP);
	}

	private String calcPropLabel() {
		StringBuilder sb = new StringBuilder();
		String domConName = ES;

		if (getTargetSentence() != null) {
			CeConcept domCon = getTargetSentence().getTargetConcept();

			if (domCon != null) {
				domConName = domCon.getConceptName();
			}
		}

		sb.append(PROPDEF_PREFIX);
		sb.append(domConName);
		sb.append(TOKEN_COMMA);
		sb.append(this.propertyRangeName);
		sb.append(PROPDEF_SUFFIX);

		return sb.toString();
	}

	private void storePropertyLabelNamed(String pPropLabel) {
		getTargetSentence().addFinalToken(SCELABEL_PROP, pPropLabel, this.ac.getModelBuilder().getCachedStringValueLevel1(this.propertyCeName));
	}

	private void updateTemporaryPropertyLabel() {
		getTargetSentence().replacePropertyToken(calcPropLabel());
	}

	private void storeRangeValue(String pRawToken) {
		//Store the token
		this.propertyRangeName = pRawToken;
		getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, this.ac.getModelBuilder().getCachedStringValueLevel1(this.propertyRangeName));		
	}

	private void storeRangeConcept() {
		//Store the token
		this.propertyRangeName = this.rangeToken;

		if (this.propertyRangeName.equals(TOKEN_VALUE)) {
			getTargetSentence().addFinalToken(SCELABEL_NORMAL, ES, TOKEN_VALUE);
		} else {
			getTargetSentence().addFinalToken(SCELABEL_CONCEPT, ES, this.ac.getModelBuilder().getCachedStringValueLevel1(this.propertyRangeName));
		}
		this.rangeToken = ES;

		storePropertyVariableName(this.lastToken);
	}

	private void addToPhraseToken(String pRawToken) {
		if (this.phraseToken.isEmpty()) {
			this.phraseToken = pRawToken;
		} else {
			this.phraseToken += TOKEN_SPACE + pRawToken;
		}
	}

	private void addToConceptToken(String pRawToken) {
		if (this.conceptToken.isEmpty()) {
			this.conceptToken = pRawToken;
		} else {
			this.conceptToken += TOKEN_SPACE + pRawToken;
		}
	}

	private void addToPropertyToken(String pRawToken) {
		if (this.propertyToken.isEmpty()) {
			this.propertyToken = pRawToken;	
		} else {
			this.propertyToken += TOKEN_SPACE + pRawToken;	
		}
	}

	private void addToRangeToken(String pRawToken) {
		if (this.rangeToken.isEmpty()) {
			this.rangeToken = pRawToken;
		} else {
			this.rangeToken += TOKEN_SPACE + pRawToken;	
		}
	}

}
