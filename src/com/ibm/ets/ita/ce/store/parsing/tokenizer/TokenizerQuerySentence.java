package com.ibm.ets.ita.ce.store.parsing.tokenizer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import com.ibm.ets.ita.ce.store.model.CeClause;
import com.ibm.ets.ita.ce.store.model.CeConcatenatedValue;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFactNormal;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceRuleOrQuery;

public class TokenizerQuerySentence extends TokenizerSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String TOKEN_OPENSQBR = "[";
	private static final String TOKEN_CLOSESQBR = "]";
	private static final String TOKEN_OPENPAR = "(";
	private static final String TOKEN_CLOSEPAR = ")";
	private static final String TOKEN_COMMA = ",";
	private static final String TOKEN_FOR = "for";
	private static final String TOKEN_HOW = "how";
	private static final String TOKEN_MANY = "many";
	private static final String TOKEN_WHICH = "which";
	private static final String TOKEN_IT = "it";
	private static final String TOKEN_TRUE = "true";

	//DSB 01/05/2015 #1095
	private static final String TOKEN_OR = "or";
	private static final String TOKEN_ORDER = "order";	
	private static final String TOKEN_BY = "by";
	private static final String TOKEN_ASCENDING = "ascending";
	private static final String TOKEN_DESCENDING = "descending";

	private boolean insideSqBr = false;
	private boolean insidePar = false;
	private boolean ignoreThisToken = false;
	private boolean inPreamble = false;
	private boolean inVariableList = false;
	private boolean inOrderBy = false;	//DSB 01/05/2015 #1095

	private String queryName = "";
	private CeQuery targetQuery = null;
	private CeClause currentClause = null;

	private static void doClauseProcessingForExistenceStatement(CeClause pClause) {
		String cloneSeqId = pClause.getSeqId() + "_" + Integer.toString(1);
		CeClause thisClause = pClause.cloneFromStemUsing(cloneSeqId);
		pClause.addChildClause(thisClause);
		thisClause = null;
	}

	private static void doNormalClauseProcessingOn(BuilderSentenceFactNormal pSenBuilder, CeClause pClause) {
		CeClause thisClause = null;
		String origSeqId = pClause.getSeqId();
		int seqCounter = 0;

		for (CeConcept thisSecCon : pSenBuilder.getSecondaryConceptsNormal()) {
			String cloneSeqId = origSeqId + "_" + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addSecondaryConceptNormal(thisSecCon);
			pClause.addSecondaryConceptNormal(thisSecCon);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (CeConcept thisSecCon : pSenBuilder.getSecondaryConceptsNegated()) {
			String cloneSeqId = origSeqId + "_" + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addSecondaryConceptNegated(thisSecCon);
			pClause.addSecondaryConceptNegated(thisSecCon);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (CePropertyInstance thisProp : pSenBuilder.getDatatypeProperties()) {
			thisProp.setClauseVariableId(pSenBuilder.getInstanceName());
			String cloneSeqId = origSeqId + "_" + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addDatatypeProperty(thisProp);
			pClause.addDatatypeProperty(thisProp);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (CePropertyInstance thisProp : pSenBuilder.getObjectProperties()) {
			thisProp.setClauseVariableId(pSenBuilder.getInstanceName());
			String cloneSeqId = origSeqId + "_" + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addObjectProperty(thisProp);
			pClause.addObjectProperty(thisProp);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (CeConcatenatedValue thisCc : pSenBuilder.getConcatenatedValues()) {
			String cloneSeqId = origSeqId + "_" + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addConcatenatedValue(thisCc);
			pClause.addConcatenatedValue(thisCc);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}
	}

	@Override
	protected String tokenizerType() {
		return "Query";
	}

	@Override
	protected BuilderSentenceRuleOrQuery getTargetSentence() {
		return (BuilderSentenceRuleOrQuery)superTargetSentence();
	}

	@Override
	public void doTokenizing() {
		initialiseBuffers();
		createFinalTokensForQuerySentence();

		this.targetQuery.calculateAllVariableIds();
		this.targetQuery.testIntegrity(this.ac);

		//Only save the query if it is being created, not executed
		if (!this.ac.isExecutingQueryOrRule()) {
			this.mb.addQuery(this.targetQuery);
		}

		getTargetSentence().setQuery(this.targetQuery);
	}

	private void initialiseBuffers() {
		this.insideSqBr = false;
		this.insidePar = false;
		this.ignoreThisToken = false;
		this.inPreamble = false;
		this.inVariableList = false;

		this.queryName = "";

		this.targetQuery = null;
		this.currentClause = null;
	}

	private void createFinalTokensForQuerySentence() {
		int tokenCounter = 0;
		for (String thisRawToken : getTargetSentence().getRawTokens()) {
			this.ignoreThisToken = false;

			if (thisRawToken.equals(TOKEN_OPENSQBR)) {
				handleOpenSquareBracket();
			} else if (thisRawToken.equals(TOKEN_CLOSESQBR)) {
				handleCloseSquareBracket();
			} else if (thisRawToken.equals(TOKEN_FOR)) {
				tokenCounter = handleTokenFor(tokenCounter);
			} else if (thisRawToken.equals(TOKEN_HOW)) {
				tokenCounter = handleTokenHow(tokenCounter);
			} else if (thisRawToken.equals(TOKEN_MANY)) {
				tokenCounter = handleTokenMany(tokenCounter);
			} else if (thisRawToken.equals(TOKEN_WHICH)) {
				tokenCounter = handleTokenWhich(tokenCounter);
			} else if (thisRawToken.equals(TOKEN_IS)) {
				tokenCounter = handleTokenIs(tokenCounter);
			} else if (thisRawToken.equals(TOKEN_IT)) {
				tokenCounter = handleTokenIt(tokenCounter);
			} else if (thisRawToken.equals(TOKEN_TRUE)) {
				tokenCounter = handleTokenTrue(tokenCounter);
			} else if (thisRawToken.equals(TOKEN_THAT)) {
				tokenCounter = handleTokenThat(tokenCounter);
			} else if (thisRawToken.equals(TOKEN_AND)) {
				handleTokenAnd();
			} else if (thisRawToken.equals(TOKEN_COMMA)) {
				handleTokenComma();
			} else if (thisRawToken.equals(TOKEN_OPENPAR)) {
				handleOpenParenthesis();
			} else if (thisRawToken.equals(TOKEN_CLOSEPAR)) {
				handleCloseParenthesis();
			}

			if (!this.ignoreThisToken) {
				tokenCounter = processToken(thisRawToken, tokenCounter);
			}
		}

		tokenizingComplete();
	}

	private void handleOpenSquareBracket() {
		if (this.insideSqBr) {
			reportError("Attempt to open parenthesis ([) when already open during processing of query sentence:" + getTargetSentence().getSentenceText(), this.ac);
		}

		this.insideSqBr = true;
		this.ignoreThisToken = true;
		getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_OPENSQBR);
	}

	private void handleCloseSquareBracket() {
		if (!this.insideSqBr) {
			reportError("Attempt to close parenthesis (]) when it has not yet been opened during processing of query sentence:" + getTargetSentence().getSentenceText(), this.ac);
		}

		//The query name is complete so store as a final token
		getTargetSentence().addFinalToken(SCELABEL_RQNAME, "", this.ac.getModelBuilder().getCachedStringValueLevel3(this.queryName));
		this.insideSqBr = false;
		this.ignoreThisToken = true;
		getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_CLOSESQBR);
	}

	private int handleTokenFor(int pCounter) {
		int result = pCounter;

		if (!this.insideSqBr) {
			//Only treat "for" as a special case if it is the first token
			if (pCounter == 0) {
				//The query can now be created
				createNewQuery();
	
				this.ignoreThisToken = true;
				this.inPreamble = true;
				++result;
				getTargetSentence().addFinalToken(SCELABEL_RQSTART, "", TOKEN_FOR);
			}
		}

		return result;
	}

	private int handleTokenHow(int pCounter) {
		int result = pCounter;

		if (!this.insideSqBr) {
			//Only treat "how" as a special case if it is the second token and we are in the preamble
			if ((pCounter == 1) && (this.inPreamble)) {
				this.targetQuery.markAsCountQuery();
				this.ignoreThisToken = true;
				++result;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_HOW);
			}
		}

		return result;
	}

	private int handleTokenMany(int pCounter) {
		int result = pCounter;

		if (!this.insideSqBr) {
			//Only treat "many" as a special case if it is the third token and we are in the preamble
			if ((pCounter == 2) && (this.inPreamble)) {
				this.targetQuery.markAsCountQuery();
				this.ignoreThisToken = true;
				this.inPreamble = false;
				this.inVariableList = true;
				++result;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_MANY);
			}
		}

		return result;
	}

	private int handleTokenWhich(int pCounter) {
		int result = pCounter;

		if (!this.insideSqBr) {
			//Only treat "which" as a special case if it is the second token and we are in the preamble
			if ((pCounter == 1) && (this.inPreamble)) {
				this.targetQuery.markAsNormalQuery();
				this.ignoreThisToken = true;
				this.inPreamble = false;
				this.inVariableList = true;
				++result;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_WHICH);
			}
		}

		return result;
	}

	private int handleTokenIs(int pCounter) {
		int result = pCounter;

		if (!this.insideSqBr) {
			//Only treat "is" as a special case if it is the third or fourth token and we are in the preamble
			if (((pCounter == 2) || (pCounter == 3)) && (this.inVariableList)) {
				this.ignoreThisToken = true;
				this.inVariableList = false;
				this.inPreamble = true;
				++result;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_IS);
			}
		}

		return result;
	}

	private int handleTokenIt(int pCounter) {
		int result = pCounter;

		if (!this.insideSqBr) {
			//Only treat "it" as a special case if it is the fourth or fifth token and we are in the preamble
			if (((pCounter == 3) || (pCounter == 4)) && (this.inPreamble)) {
				this.ignoreThisToken = true;
				this.inPreamble = true;
				++result;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_IT);
			}
		}

		return result;
	}

	private int handleTokenTrue(int pCounter) {
		int result = pCounter;

		if (!this.insideSqBr) {
			//Only treat "true" as a special case if it is the fifth or sixth token and we are in the preamble
			if (((pCounter == 4) || (pCounter == 5)) && (this.inPreamble)) {
				this.ignoreThisToken = true;
				this.inPreamble = true;
				++result;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_TRUE);
			}
		}

		return result;
	}

	private int handleTokenThat(int pCounter) {
		int result = pCounter;

		if (!this.insideSqBr) {
			//Only treat "that" as a special case if it is the sixth or seventh token and we are in the preamble
			if (((pCounter == 5) || (pCounter == 6)) && (this.inPreamble)) {
				this.ignoreThisToken = true;
				this.inPreamble = false;
				++result;
				getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, "", TOKEN_THAT);
			}
		}

		return result;
	}

	private void handleTokenAnd() {
		if (!this.insideSqBr) {
			if (this.inVariableList) {
				//If it is inside the list of variable ids then it is a separator
				this.ignoreThisToken = true;
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_AND);
			}
	
			if (this.insidePar) {
				//If it is inside the parenthesis then it is part of a (multi-term) clause and must be retained
				this.currentClause.addRawToken(TOKEN_AND);
				this.ignoreThisToken = true;
			}
		}
	}

	private void handleTokenComma() {
		if (this.inVariableList) {
			//If it is inside the list of variable ids then it is a separator
			this.ignoreThisToken = true;
			getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_COMMA);
		}
	}

	private void handleOpenParenthesis() {
		addNewPremiseClause();

		this.insidePar = true;
		this.ignoreThisToken = true;
	}

	private void handleCloseParenthesis() {
		saveCurrentClause();

		this.insidePar = false;
		this.ignoreThisToken = true;
	}

	private void tokenizingComplete() {
		//The tokenizing is complete so now the clauses can be processed
		processClauses();

		//Finished, so notify the sentence that validation is complete
		getTargetSentence().validationComplete();
	}

	private int processToken(String pToken, int pCounter) {
		int result = pCounter;

		if (this.insideSqBr) {
			//Inside square brackets any tokens are names
			processNameToken(pToken);
		} else {
			if (this.inVariableList) {
				//We are in the variable list so this is a variable id
				this.targetQuery.addResponseVariableId(pToken);
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", this.ac.getModelBuilder().getCachedStringValueLevel3(pToken));
			} else if (this.inOrderBy) {
				//DSB 01/05/2015 #1095
				//We are in an "order by" suffix
				if (pToken.equals(TOKEN_BY)) {
					//Ignore the "by token"
				} else if (pToken.equals(TOKEN_COMMA)) {
					//Ignore any commas
				} else {
					//This must be a variable name
					if (pToken.equals(TOKEN_ASCENDING)) {
						//Ascending is the default order so just ignore this token
					} else if (pToken.equals(TOKEN_DESCENDING)) {
						this.targetQuery.setSortOrderDescending();
					} else {
						this.targetQuery.addOrderToken(pToken);
					}
				}
			} else {
				//Outside square brackets and the variable list the token counter must be incremented
				++result;
				if (this.insidePar) {
					//If inside a parenthesis then this is a clause 
					processClauseToken(pToken);
				} else {
					//Only the "and" token or a suffix token acceptable outside of all the query sections
					if (pToken.equals(TOKEN_AND)) {
						//Just ignore the "and"
					} else if (pToken.equals(TOKEN_OR)) {
						//DSB 01/05/2015 #1095
						processUnsupportedOrToken();
					} else if (pToken.equals(TOKEN_ORDER)) {
						//DSB 01/05/2015 #1095
						processOrderToken();
					} else {
						//No unexpected tokens should occur outside brackets
						processUnexpectedToken(pToken);
					}
				}
			}
		}

		return result;
	}

	private void processNameToken(String pToken) {
		if (this.queryName.isEmpty()) {
			this.queryName = pToken;
		} else {
			this.queryName += " " + pToken;
		}
	}

	private void processClauseToken(String pToken) {
		if (this.currentClause != null) {
			this.currentClause.addRawToken(pToken);
		} else {
			reportError("Null clause encountered when trying to add raw token (" + pToken + ") to clause", this.ac);
		}
	}

	private void processUnexpectedToken(String pToken) {
		reportError("Unexpected token '" + pToken + "' found outside square brackets and parentheses during query processing, for sentence:" + getTargetSentence().getSentenceText(), this.ac);
	}

	//DSB 01/05/2015 #1095
	private void processUnsupportedOrToken() {
		reportError("The 'or' operator is not yet supported in CE Queries and will be treated as an 'and', for sentence:" + getTargetSentence().getSentenceText(), this.ac);
	}

	//DSB 01/05/2015 #1095
	private void processOrderToken() {
		this.inOrderBy = true;
	}

	private void processClauses() {
		if (this.targetQuery != null) {
			boolean firstClause = true;
			for (CeClause thisClause : this.targetQuery.getDirectPremiseClauses()) {
				processClause(thisClause, firstClause);
				firstClause = false;
			}
		} else {
			reportError("No targetQuery identified during query tokenizing, for sentence: " + getTargetSentence().getSentenceText(), this.ac);
		}
	}

	private void processClause(CeClause pClause, boolean pFirstClause) {
		String clauseText = pClause.terminatedClauseText();
		if (!clauseText.isEmpty()) {
			BuilderSentenceFactNormal bsF = (BuilderSentenceFactNormal)BuilderSentence.createForSentenceText(this.ac, clauseText, pClause.getRawTokens());

			if (bsF != null) {
				if (!pFirstClause) {
					getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, "", TOKEN_AND);
				}
				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_OPENPAR);
				bsF.markAsClause();
				bsF.categoriseSentence();

				TokenizerSentence.doTokenizingFor(this.ac, bsF, true);

				if (!bsF.hasErrors()) {
					getTargetSentence().addSceTokens(bsF.getStructuredCeTokens());
					pClause.setTargetConcept(bsF.getTargetConcept());
					pClause.setTargetVariable(bsF.getInstanceName());
					if (bsF.hadQuotedInstanceName()) {
						pClause.markTargetVariableAsQuoted();
					}
					pClause.setTargetConceptNegated(bsF.isTargetConceptNegated());
					int dpCount = bsF.getDatatypeProperties().size();
					int opCount = bsF.getObjectProperties().size();
					int stCount = bsF.getAllSecondaryConcepts().size();
					int cvCount = bsF.getConcatenatedValues().size();
					int totalCount = dpCount + opCount + stCount + cvCount;

					if (totalCount == 0) {
						//Plain statement of existence
						doClauseProcessingForExistenceStatement(pClause);
					} else {
						doNormalClauseProcessingOn(bsF, pClause);
					}
				} else {
					for (String thisError : bsF.errors) {
						addErrorToSentence(thisError);
					}
					reportWarning("Query clause '" + clauseText + "' contains errors, in sentence: '" + getTargetSentence().getSentenceText() + "'", this.ac);
				}
			}
			getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_CLOSEPAR);
		} else {
			reportWarning("Unexpected empty clause for query, in sentence: '" + getTargetSentence().getSentenceText() + "'", this.ac);
		}
	}

	private void createNewQuery() {
		if (this.queryName.isEmpty()) {
			this.queryName = generateDefaultQueryName();
			if (isReportDebug()) {
				reportDebug("No query name specified, so '" + this.queryName + "' has been generated for query in sentence: " + getTargetSentence().getSentenceText(), this.ac);
			}
		}

		this.targetQuery = CeQuery.createNew(getTargetSentence().getSentenceText(), this.queryName);
	}

	private void addNewPremiseClause() {
		if (this.targetQuery != null) {
			this.currentClause = CeClause.createNewPremiseClauseFor(this.targetQuery);
			this.targetQuery.addDirectPremiseClause(this.currentClause);
		} else {
			reportError("Null targetQuery encountered when trying to add new premise clause to query, for sentence: " + getTargetSentence().getSentenceText(), this.ac);
		}
	}

	private static String generateDefaultQueryName() {
		//This is used when a query name has not been specified
		return "query_" + CeQuery.nextPatternId();
	}

	private void saveCurrentClause() {
		if (this.currentClause == null) {
			reportError("Unxepected null currentClause whilst processing sentence: " + getTargetSentence().getSentenceText(), this.ac);
		}
	}

}