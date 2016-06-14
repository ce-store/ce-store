package com.ibm.ets.ita.ce.store.parsing.tokenizer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.extractTimestampFrom;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.formattedIso8601DateString;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.stripDelimitingQuotesFrom;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeSentenceQualified;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.processor.ProcessorCe;

public class TokenizerFactSentenceQualified extends TokenizerSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = TokenizerFactSentenceQualified.class.getName();
	private static final String PACKAGE_NAME = TokenizerFactSentenceQualified.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);


	//Start and end tokens
	private static final String TOKEN_START_1 = "the";
	private static final String TOKEN_START_2 = "statement";
	private static final String TOKEN_START_3 = "that";
	private static final String TOKEN_START_4 = "(";
	private static final String TOKEN_END = ")";

	//General tokens
	private static final String TOKEN_TIMESTAMPED = "time-stamped";
	private static final String TOKEN_BY = "by";
	private static final String TOKEN_IN = "in";
	private static final String TOKEN_QUALIFIED = "qualified";
	private static final String TOKEN_WITH = "with";
	private static final String TOKEN_TRUST = "trust";
	private static final String TOKEN_LEVEL = "level";

	//Timeframe tokens
	private static final String TOKEN_TF_CURRENTLY = "currently";
	private static final String TOKEN_TF_HISTORICALLY = "historically";
	private static final String TOKEN_TF_INFUTURE = "in-future";
	private static final String TOKEN_TF_ALWAYS = "always";

	//Truth value tokens
	private static final String TOKEN_TV_TRUE = "true";
	private static final String TOKEN_TV_FALSE = "false";
	private static final String TOKEN_TV_POSSIBLE = "possible";
	private static final String TOKEN_TV_IMPOSSIBLE = "impossible";

	//Statement type tokens
	private static final String TOKEN_ST_HYPOTHESISED = "hypothesised";
	private static final String TOKEN_ST_ASSUMED = "assumed";
	private static final String TOKEN_ST_ASSERTED = "asserted";
	private static final String TOKEN_ST_DECLARED = "declared";
	private static final String TOKEN_ST_INFERRED = "inferred";

	//Default values
	private static final String DEFAULT_TF = TOKEN_TF_ALWAYS;
	private static final String DEFAULT_TV = TOKEN_TV_TRUE;
	private static final String DEFAULT_ST = TOKEN_ST_ASSERTED;

	private static final String CON_TRUSTLEVEL = "trust level";

	//Modes
	private static final int MODE_NORMAL = 0;
	private static final int MODE_CONNECT = 1;
	private static final int MODE_SEEK = 2;
	private static final int MODE_GET_TS = 3;
	private static final int MODE_GET_TV = 4;
	private static final int MODE_GET_AUTHOR1 = 5;
	private static final int MODE_GET_AUTHOR2 = 6;
	private static final int MODE_GET_AUTHOR3 = 7;
	private static final int MODE_GET_AUTHOR4 = 8;
	private static final int MODE_GET_CONTEXT1 = 9;
	private static final int MODE_GET_CONTEXT2 = 10;
	private static final int MODE_GET_CONTEXT3 = 11;
	private static final int MODE_GET_QUALIFIED1 = 12;
	private static final int MODE_GET_QUALIFIED2 = 13;
	private static final int MODE_GET_QUALIFIED3 = 14;
	private static final int MODE_GET_QUALIFIED4 = 15;
	private static final int MODE_GET_QUALIFIED5 = 16;

	private Calendar tsCal = null;
	private String tsText = "";
	private String timeframe = "";
	private String truthValue = "";
	private String statementType = "";
	private CeConcept authorCon = null;
	private String authorName = "";
	private CeConcept contextCon = null;
	private String contextName = "";
	private CeConcept qualCon = null;
	private String qualName = "";

	private BuilderSentence innerSentence = null;

	private static boolean isTimestampedToken(String pToken) {
		return pToken.equals(TOKEN_TIMESTAMPED);
	}

	private static boolean isQualifiedToken(String pToken) {
		return pToken.equals(TOKEN_QUALIFIED);
	}

	private static boolean isTimeframeToken(String pToken) {
		return pToken.equals(TOKEN_TF_CURRENTLY) || pToken.equals(TOKEN_TF_HISTORICALLY) || pToken.equals(TOKEN_TF_INFUTURE) || pToken.equals(TOKEN_TF_ALWAYS);
	}

	private static boolean isTruthValueToken(String pToken) {
		return pToken.equals(TOKEN_TV_TRUE) || pToken.equals(TOKEN_TV_FALSE) || pToken.equals(TOKEN_TV_POSSIBLE) || pToken.equals(TOKEN_TV_IMPOSSIBLE);
	}

	private static boolean isStatementTypeToken(String pToken) {
		return pToken.equals(TOKEN_ST_HYPOTHESISED) || pToken.equals(TOKEN_ST_ASSUMED) || pToken.equals(TOKEN_ST_ASSERTED) || pToken.equals(TOKEN_ST_DECLARED) || pToken.equals(TOKEN_ST_INFERRED);
	}

	@Override
	protected String tokenizerType() {
		return "Fact (qualified)";
	}

	@Override
	protected BuilderSentence getTargetSentence() {
		return superTargetSentence();
	}

	@Override
	protected void doTokenizing() {
		initialiseBuffers();
		createFinalTokensForQualifiedFactSentence();
		updateQualifiedSentence();
	}

	private void updateQualifiedSentence() {
		CeSentenceQualified qs = convertToSentence();

		if (this.innerSentence != null) {
			qs.setInnerSentence(this.innerSentence.getConvertedSentence());
		} else {
			reportError("Unexpected null inner sentence during qualified information processing, for: " + this.getTargetSentence().getSentenceText(), this.ac);
		}

		qs.setGeneralQualification(this.qualCon, this.qualName);
		qs.setQualifiedAuthor(this.authorCon, this.authorName);
		qs.setQualifiedContext(this.contextCon, this.contextName);
		qs.setStatementType(this.statementType);
		qs.setTimeframe(this.timeframe);
		qs.setTruthValue(this.truthValue);
		qs.setTimestamp(this.tsText);
	}

	private CeSentenceQualified convertToSentence() {
		CeSource lastSrc = this.ac.getLastSource();

		return CeSentenceQualified.createValidQualifiedFactSentence(this.ac, getTargetSentence().getSentenceText(), getTargetSentence().getStructuredCeTokens(), lastSrc);
	}

	private void initialiseBuffers() {
		//TODO: Complete this
	}

	private void createFinalTokensForQualifiedFactSentence() {
		ArrayList<String> originalTokens = getTargetSentence().getRawTokens();
		ArrayList<String> innerTokens = new ArrayList<String>();
		ArrayList<String> outerTokens = new ArrayList<String>();

		if (originalTokens.size() >= 4) {
			String firstToken = originalTokens.get(0);

			if (firstToken.equals(TOKEN_START_1)) {
				String secondToken = getTargetSentence().getRawTokens().get(1);
				if (secondToken.equals(TOKEN_START_2)) {
					String thirdToken = getTargetSentence().getRawTokens().get(2);
					if (thirdToken.equals(TOKEN_START_3)) {
						String fourthToken = getTargetSentence().getRawTokens().get(3);
						if (fourthToken.equals(TOKEN_START_4)) {
							boolean endOfInnerTokens = false;

							for (int i = 4; i < originalTokens.size(); i++) {
								String thisToken = originalTokens.get(i);
								if (thisToken.equals(TOKEN_END)) {
									endOfInnerTokens = true;
								} else {
									if (endOfInnerTokens) {
										outerTokens.add(thisToken);
									} else {
										innerTokens.add(thisToken);
									}
								}
							}

							processSentenceAndQualifiers(innerTokens, outerTokens);
						} else {
							reportError("Qualified fact sentence processing failed on token 4, for sentence: " + getTargetSentence().toString(), this.ac);
						}
					} else {
						reportError("Qualified fact sentence processing failed on token 3, for sentence: " + getTargetSentence().toString(), this.ac);
					}
				} else {
					reportError("Qualified fact sentence processing failed on token 2, for sentence: " + getTargetSentence().toString(), this.ac);
				}
			} else {
				reportError("Qualified fact sentence processing failed on token 1, for sentence: " + getTargetSentence().toString(), this.ac);
			}
		} else {
			reportError("Unable to parse qualified fact sentence as there are not enough tokens (" + new Integer(originalTokens.size()).toString() + ") for sentence: " + getTargetSentence().toString(), this.ac);
		}

		getTargetSentence().validationComplete();
	}

	private void processSentenceAndQualifiers(ArrayList<String> pInnerTokens, ArrayList<String> pOuterTokens) {
		processOuterSentence(pOuterTokens);
		processInnerSentence(pInnerTokens);
	}

	private void processInnerSentence(ArrayList<String> pInnerTokens) {
		String innerCe = rebuildCeFromTokens(pInnerTokens);

		this.innerSentence = BuilderSentence.createForSentenceText(this.ac, innerCe, pInnerTokens);
		this.innerSentence.categoriseSentence();

		ContainerSentenceLoadResult senStats = ContainerSentenceLoadResult.createWithZeroValues("processInnerSentence");
		ProcessorCe.tokenizeSentence(this.ac, this.innerSentence, null, false, senStats);
	}

	private static String rebuildCeFromTokens(ArrayList<String> pTokens) {
		StringBuilder sb = new StringBuilder();
		String tokenSep = "";

		for (String thisToken : pTokens) {
			sb.append(tokenSep);
			sb.append(thisToken);
			tokenSep = " ";
		}

		sb.append(".");

		return sb.toString();
	}

	private void processOuterSentence(ArrayList<String> pOuterTokens) {
		int mode = MODE_NORMAL;
		boolean hasErrors = false;

		for (String thisToken : pOuterTokens) {
			switch (mode) {
			case MODE_NORMAL:
				if (thisToken.equals(TOKEN_IS)) {
					mode = MODE_SEEK;
				} else {
					reportUnexpectedToken(thisToken, mode, "");
					hasErrors = true;
					mode = MODE_CONNECT;
				}
				break;
			case MODE_CONNECT:
				if (thisToken.equals(TOKEN_AND)) {
					mode = MODE_NORMAL;
				} else {
					reportUnexpectedToken(thisToken, mode, "");
					hasErrors = true;
					mode = MODE_CONNECT;
				}
				break;
			case MODE_SEEK:
				if (isTimestampedToken(thisToken)) {
					mode = MODE_GET_TS;
				} else if (isTimeframeToken(thisToken)) {
					saveTimeframe(thisToken);
					mode = MODE_GET_TV;
				} else if (isTruthValueToken(thisToken)) {
					saveTruthValue(thisToken);
					mode = MODE_CONNECT;
				} else if (isStatementTypeToken(thisToken)) {
					saveStatementType(thisToken);
					mode = MODE_GET_AUTHOR1;
				} else if (isQualifiedToken(thisToken)) {
					mode = MODE_GET_QUALIFIED1;
				} else if (thisToken.equals(TOKEN_IN)) {
					mode = MODE_GET_CONTEXT1;
				} else {
					if (isAuthorToken(thisToken, mode)) {
						if (mode == MODE_GET_AUTHOR4) {
							mode = MODE_CONNECT;
						} else {
							++mode;
						}
					} else if (isContextToken(thisToken, mode)) {
						if (mode == MODE_GET_CONTEXT3) {
							mode = MODE_CONNECT;
						} else {
							++mode;
						}
					} else if (isQualifiedToken(thisToken, mode)) {
						if (mode == MODE_GET_QUALIFIED5) {
							mode = MODE_CONNECT;
						} else {
							++mode;
						}
					} else {
						reportUnexpectedToken(thisToken, mode, "");
						hasErrors = true;
						mode = MODE_CONNECT;
					}
				}
				break;
			case MODE_GET_TS:
				hasErrors = saveTimestamp(thisToken);
				mode = MODE_CONNECT;
				break;
			case MODE_GET_TV:
				if (isTruthValueToken(thisToken)) {
					saveTruthValue(thisToken);
					mode = MODE_CONNECT;
				} else if (thisToken.equals(TOKEN_AND)) {
					mode = MODE_NORMAL;
				} else {
					reportUnexpectedToken(thisToken, mode, "truth value");
					mode = MODE_CONNECT;
				}
				break;
			default:
				//Another mode - assumed to be one of the author or context modes
				if (isAuthorToken(thisToken, mode)) {
					if (mode == MODE_GET_AUTHOR4) {
						mode = MODE_CONNECT;
					} else {
						++mode;
					}
				} else if (isContextToken(thisToken, mode)) {
					if (mode == MODE_GET_CONTEXT3) {
						mode = MODE_CONNECT;
					} else {
						++mode;
					}
				} else if (isQualifiedToken(thisToken, mode)) {
					if (mode == MODE_GET_QUALIFIED5) {
						mode = MODE_CONNECT;
					} else {
						++mode;
					}
				} else {
					reportUnexpectedToken(thisToken, mode, "");
					hasErrors = true;
					mode = MODE_CONNECT;
				}
				break;
			}
		}

		if (!hasErrors) {
			calculateDefaultValues();
			hasErrors = testForInconsistencies();
		}

		if (hasErrors) {
			reportError("No further processing for qualified sentence as errors were encountered", this.ac);
		}
	}

	private void calculateDefaultValues() {
		if (this.tsText.isEmpty()) {
			//TODO: Timestamps are not always required, so this could be simplified
			this.tsCal = tsNow();
			this.tsText = formattedIso8601DateString(this.tsCal);
		}
		if (this.timeframe.isEmpty()) {
			this.timeframe = DEFAULT_TF;
		}
		if (this.truthValue.isEmpty()) {
			this.truthValue = DEFAULT_TV;
		}
		if (this.statementType.isEmpty()) {
			this.statementType = DEFAULT_ST;
		}
	}

	private boolean testForInconsistencies() {
		boolean result = false;

		//If author information is provided then both concept and instance name are needed
		if (this.authorCon != null) {
			if (this.authorName.isEmpty()) {
				//Inconsistency - author concept, but no name
				result = true;
			}
		} else {
			if (!this.authorName.isEmpty()) {
				//Inconsistency - author name but no concept
				result = true;
			}
		}

		return result;
	}

	private static Calendar tsNow() {
		return new GregorianCalendar();
	}

	private boolean saveTimestamp(String pValue) {
		final String METHOD_NAME = "saveTimestamp";

		boolean result = false;

		//TODO: Implement this properly
		if (this.tsText.isEmpty()) {
			this.tsText = stripDelimitingQuotesFrom(pValue);
			try {
				this.tsCal = extractTimestampFrom(this.tsText, this.ac);
			} catch (Exception e) {
				reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
			}
			if (this.tsCal == null) {
				reportError("Invalid timestamp (" + this.tsText + ") encountered during qualified sentence processing", this.ac);
				result = true;
			}
		} else {
			reportDuplicateValue("timestamp");
			result = true;
		}

		return result;
	}

	private void saveTimeframe(String pValue) {
		if (this.timeframe.isEmpty()) {
			this.timeframe = pValue;
		} else {
			reportDuplicateValue("timeframe");
		}
	}

	private void saveTruthValue(String pValue) {
		if (this.truthValue.isEmpty()) {
			this.truthValue = pValue;
		} else {
			reportDuplicateValue("truth value");
		}
	}

	private void saveStatementType(String pValue) {
		if (this.statementType.isEmpty()) {
			this.statementType = pValue;
		} else {
			reportDuplicateValue("statement type");
		}
	}

	private void saveAuthorConcept(CeConcept pCon) {
		if (this.authorCon == null) {
			this.authorCon = pCon;
		} else {
			reportDuplicateValue("author concept");
		}
	}

	private void saveAuthorName(String pValue) {
		if (this.authorName.isEmpty()) {
			this.authorName = pValue;
		} else {
			reportDuplicateValue("author name");
		}
	}

	private void saveGeneralQualification(CeConcept pConcept, String pInstName) {
		if (this.qualCon == null) {
			this.qualCon = pConcept;
		} else {
			reportDuplicateValue("qualification concept");
		}

		if (this.qualName.isEmpty()) {
			this.qualName = pInstName;
		} else {
			reportDuplicateValue("qualification name");
		}
	}

	private void saveContextConcept(CeConcept pCon) {
		if (this.contextCon == null) {
			this.contextCon = pCon;
		} else {
			reportDuplicateValue("context concept");
		}
	}

	private void saveContextName(String pValue) {
		if (this.contextName.isEmpty()) {
			this.contextName = pValue;
		} else {
			reportDuplicateValue("context name");
		}
	}

	private boolean isAuthorToken(String pToken, int pMode) {
		boolean result = false;

		switch (pMode) {
		case MODE_GET_AUTHOR1:
			if (pToken.equals(TOKEN_BY)) {
				result = true;
			}
			break;
		case MODE_GET_AUTHOR2:
			if (pToken.equals(TOKEN_THE)) {
				result = true;
			}
			break;
		case MODE_GET_AUTHOR3:
			//TODO: Need to handle multi-word concept names here
			CeConcept refCon = this.ac.getModelBuilder().getConceptNamed(this.ac, pToken);
			if (refCon != null) {
				saveAuthorConcept(refCon);
				result = true;
			} else {
				reportError("Unable to locate concept (" + pToken + ") used in author declaration for qualified sentence: " + getTargetSentence().getSentenceText(), this.ac);
			}
			break;
		case MODE_GET_AUTHOR4:
			saveAuthorName(pToken);
			result = true;
			break;
		default:
			break;
		}

		return result;
	}

	private boolean isQualifiedToken(String pToken, int pMode) {
		boolean result = false;

		switch (pMode) {
		case MODE_GET_QUALIFIED1:
			if (pToken.equals(TOKEN_WITH)) {
				result = true;
			}
			break;
		case MODE_GET_QUALIFIED2:
			if (pToken.equals(TOKEN_THE)) {
				result = true;
			}
			break;
		case MODE_GET_QUALIFIED3:
			if (pToken.equals(TOKEN_TRUST)) {
				result = true;
			}
			break;
		case MODE_GET_QUALIFIED4:
			if (pToken.equals(TOKEN_LEVEL)) {
				result = true;
			}
			break;
		case MODE_GET_QUALIFIED5:
			CeConcept tlCon = this.mb.getConceptNamed(this.ac, CON_TRUSTLEVEL);		//TODO:  Currently hardcoded... remove this
			saveGeneralQualification(tlCon, pToken);
			result = true;
			break;
		default:
			break;
		}

		return result;
	}

	private boolean isContextToken(String pToken, int pMode) {
		boolean result = false;

		switch (pMode) {
		case MODE_GET_CONTEXT1:
			if (pToken.equals(TOKEN_THE)) {
				result = true;
			}
			break;
		case MODE_GET_CONTEXT2:
			//TODO: Need to handle multi-word concept names here
			CeConcept refCon = this.ac.getModelBuilder().getConceptNamed(this.ac, pToken);
			if (refCon != null) {
				saveContextConcept(refCon);
				result = true;
			} else {
				reportError("Unable to locate concept (" + pToken + ") used in context declaration for qualified sentence: " + getTargetSentence().getSentenceText(), this.ac);
			}
			break;
		case MODE_GET_CONTEXT3:
			saveContextName(pToken);
			result = true;
			break;
		default:
			break;
		}

		return result;
	}

	private void reportUnexpectedToken(String pToken, int pMode, String pExtraText) {
		String formattedMode = new Integer(pMode).toString();
		
		reportError("Unexpected token '" + pToken + "' encountered during qualified sentence (outer) processing (mode='" + formattedMode + "')" + pExtraText, this.ac);
	}

	private void reportDuplicateValue(String pPropName) {
		reportError("Attempt to store more than one '" + pPropName + "' value, in sentence: " + getTargetSentence().getSentenceText(), this.ac);
	}

}