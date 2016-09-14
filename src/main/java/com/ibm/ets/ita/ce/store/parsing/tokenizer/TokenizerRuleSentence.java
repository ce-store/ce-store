package com.ibm.ets.ita.ce.store.parsing.tokenizer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_CONNECTOR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_NORMAL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_RQNAME;
import static com.ibm.ets.ita.ce.store.names.ParseNames.SCELABEL_RQSTART;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AND;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CLOSEPAR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CLOSESQBR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_IF;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_OPENPAR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_OPENSQBR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THEN;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_UNDERSCORE;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import com.ibm.ets.ita.ce.store.model.CeClause;
import com.ibm.ets.ita.ce.store.model.CeConcatenatedValue;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFact;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceRuleOrQuery;

public class TokenizerRuleSentence extends TokenizerSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private boolean insideSqBr = false;
	private boolean insidePar = false;
	private boolean ignoreThisToken = false;

	private boolean inPremise = false;

	private String ruleName = "";
	private CeRule targetRule = null;
	private CeClause currentClause = null;

	private static void doClauseProcessingForExistanceStatement(CeClause pClause) {
		String cloneSeqId = pClause.getSeqId() + TOKEN_UNDERSCORE + Integer.toString(1);
		CeClause thisClause = pClause.cloneFromStemUsing(cloneSeqId);
		pClause.addChildClause(thisClause);
		thisClause = null;
	}

	private static void doNormalClauseProcessingOn(BuilderSentenceFact pSenBuilder, CeClause pClause) {
		CeClause thisClause = null;
		String origSeqId = pClause.getSeqId();
		int seqCounter = 0;

		for (CeConcept thisSecCon : pSenBuilder.getSecondaryConceptsNormal()) {
			String cloneSeqId = origSeqId + TOKEN_UNDERSCORE + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addSecondaryConceptNormal(thisSecCon);
			pClause.addSecondaryConceptNormal(thisSecCon);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (CeConcept thisSecCon : pSenBuilder.getSecondaryConceptsNegated()) {
			String cloneSeqId = origSeqId + TOKEN_UNDERSCORE + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addSecondaryConceptNegated(thisSecCon);
			pClause.addSecondaryConceptNegated(thisSecCon);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (CePropertyInstance thisProp : pSenBuilder.getDatatypeProperties()) {
			thisProp.setClauseVariableId(pSenBuilder.getInstanceName());
			String cloneSeqId = origSeqId + TOKEN_UNDERSCORE + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addDatatypeProperty(thisProp);
			pClause.addDatatypeProperty(thisProp);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (CePropertyInstance thisProp : pSenBuilder.getObjectProperties()) {
			thisProp.setClauseVariableId(pSenBuilder.getInstanceName());
			String cloneSeqId = origSeqId + TOKEN_UNDERSCORE + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addObjectProperty(thisProp);
			pClause.addObjectProperty(thisProp);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (String thisCvt : pSenBuilder.getConceptVariableTokens()) {
			String cloneSeqId = origSeqId + TOKEN_UNDERSCORE + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addConceptVariableToken(thisCvt);
			pClause.addConceptVariableToken(thisCvt);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}

		for (CeConcatenatedValue thisCc : pSenBuilder.getConcatenatedValues()) {
			String cloneSeqId = origSeqId + TOKEN_UNDERSCORE + Integer.toString(++seqCounter);
			thisClause = pClause.cloneFromStemUsing(cloneSeqId);
			thisClause.addConcatenatedValue(thisCc);
			pClause.addConcatenatedValue(thisCc);
			pClause.addChildClause(thisClause);
			thisClause = null;
		}
	}

	@Override
	protected String tokenizerType() {
		return "Rule";
	}

	@Override
	protected BuilderSentenceRuleOrQuery getTargetSentence() {
		return (BuilderSentenceRuleOrQuery)superTargetSentence();
	}

	@Override
	public void doTokenizing() {
		initialiseBuffers();
		createFinalTokensForRuleSentence();

		this.targetRule.calculateAllVariableIds();
		this.targetRule.testIntegrity(this.ac);

		//Only save the rule if it is being created, not executed
		if (!this.ac.isExecutingQueryOrRule()) {
			this.mb.addRule(this.targetRule);
		}

		getTargetSentence().setRule(this.targetRule);
	}

	private void initialiseBuffers() {
		this.insideSqBr = false;
		this.insidePar = false;
		this.ignoreThisToken = false;

		this.inPremise = false;

		this.ruleName = "";

		this.targetRule = null;
		this.currentClause = null;
	}

	private void createFinalTokensForRuleSentence() {
		for (String thisRawToken : getTargetSentence().getRawTokens()) {
			this.ignoreThisToken = false;

			if (thisRawToken.equals(TOKEN_OPENSQBR)) {
				handleOpenSquareBracket();
			} else if (thisRawToken.equals(TOKEN_CLOSESQBR)) {
				handleCloseSquareBracket();
			} else if (thisRawToken.equals(TOKEN_IF)) {
				handleTokenIf();
			} else if (thisRawToken.equals(TOKEN_THEN)) {
				handleTokenThen();
			} else if (thisRawToken.equals(TOKEN_AND)) {
				handleTokenAnd();
			} else if (thisRawToken.equals(TOKEN_OPENPAR)) {
				handleOpenParenthesis();
			} else if (thisRawToken.equals(TOKEN_CLOSEPAR)) {
				handleCloseParenthesis();
			}

			if (!this.ignoreThisToken) {
				processToken(thisRawToken);
			}
		}

		tokenizingComplete();
	}

	private void handleOpenSquareBracket() {
		if (this.insideSqBr) {
			reportError("Attempt to open parenthesis ([) when already open during processing of rule sentence:" + getTargetSentence().getSentenceText(), this.ac);
		}

		this.insideSqBr = true;
		this.ignoreThisToken = true;
		getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_OPENSQBR);
	}

	private void handleCloseSquareBracket() {
		if (!this.insideSqBr) {
			reportError("Attempt to close parenthesis (]) when it has not yet been opened during processing of rule sentence:" + getTargetSentence().getSentenceText(), this.ac);
		}

		//The rule name is complete so store as a final token
		getTargetSentence().addFinalToken(SCELABEL_RQNAME, "", this.ac.getModelBuilder().getCachedStringValueLevel3(this.ruleName));

		this.insideSqBr = false;
		this.ignoreThisToken = true;
		getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_CLOSESQBR);
	}

	private void handleTokenIf() {
		if (!this.insideSqBr) {
			//The rule can now be created
			createNewRule();

			this.inPremise = true;
			this.ignoreThisToken = true;
		}
	}

	private void handleTokenThen() {
		if (!this.insideSqBr) {
			this.inPremise = false;
			this.ignoreThisToken = true;
		}
	}

	private void handleTokenAnd() {
		if (!this.insideSqBr) {
			if (this.insidePar) {
				//If it is inside the parenthesis then it is part of a (multi-term) clause and must be retained
				this.currentClause.addRawToken(TOKEN_AND);
			}

			this.ignoreThisToken = true;
		}
	}

	private void handleOpenParenthesis() {
		if (this.inPremise) {
			addNewPremiseClause();
		} else {
			addNewConclusionClause();
		}

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

	private void processToken(String pToken) {
		if (this.insideSqBr) {
			//Inside square brackets any tokens are names
			processNameToken(pToken);
		} else {
			if (this.insidePar) {
				//If inside a parenthesis then this is a clause 
				processClauseToken(pToken);
			} else {
				//No unexpected tokens should occur outside brackets
				processUnexpectedToken(pToken);
			}
		}
	}

	private void processNameToken(String pToken) {
		if (this.ruleName.isEmpty()) {
			this.ruleName = pToken;
		} else {
			this.ruleName += " " + pToken;
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
		reportError("Unexpected token '" + pToken + "' found outside square brackets and parentheses during rule processing, for sentence:" + getTargetSentence().getSentenceText(), this.ac);
	}

	private void processClauses() {
		if (this.targetRule != null) {
			getTargetSentence().addFinalToken(SCELABEL_RQSTART, "", TOKEN_IF);
			boolean firstClause = true;
			for (CeClause thisClause : this.targetRule.getDirectPremiseClauses()) {
				processClause(thisClause, firstClause);
				firstClause = false;
			}

			getTargetSentence().addFinalToken(SCELABEL_RQSTART, "", TOKEN_THEN);

			firstClause = true;
			for (CeClause thisClause : this.targetRule.getDirectConclusionClauses()) {
				processClause(thisClause, firstClause);
				firstClause = false;
			}
		} else {
			reportError("No targetRule identified during rule tokenizing, for sentence: " + getTargetSentence().getSentenceText(), this.ac);
		}
	}

	private void processClause(CeClause pClause, boolean pFirstClause) {
		String clauseText = pClause.terminatedClauseText();
		if (!clauseText.isEmpty()) {
			BuilderSentenceFact bsF = (BuilderSentenceFact)BuilderSentence.createForSentenceText(this.ac, clauseText, pClause.getRawTokens());

			if (bsF != null) {
				bsF.markAsClause();
				bsF.categoriseSentence();

				new TokenizerFactSentence().tokenizePatternClause(this.ac, bsF, true);
				
				if (!pFirstClause) {
					getTargetSentence().addFinalToken(SCELABEL_CONNECTOR, "", TOKEN_AND);
				}

				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_OPENPAR);

				if (!bsF.hasErrors()) {
					getTargetSentence().addSceTokens(bsF.getStructuredCeTokens());
					pClause.setTargetConcept(bsF.getTargetConcept());
					pClause.setTargetVariable(bsF.getInstanceName());
					pClause.setTargetConceptNegated(bsF.isTargetConceptNegated());
					if (bsF.hadQuotedInstanceName()) {
						pClause.markTargetVariableAsQuoted();
					}
					int dpCount = bsF.getDatatypeProperties().size();
					int opCount = bsF.getObjectProperties().size();
					int stCount = bsF.getAllSecondaryConcepts().size();
					int cvtCount = bsF.getConceptVariableTokens().size();
					int covCount = bsF.getConcatenatedValues().size();
					int totalCount = dpCount + opCount + stCount + cvtCount + covCount;

					if (totalCount == 0) {
						//Plain statement of existence
						doClauseProcessingForExistanceStatement(pClause);
					} else {
						doNormalClauseProcessingOn(bsF, pClause);
					}
				} else {
					for (String thisError : bsF.errors) {
						addErrorToSentence(thisError);
					}
					reportWarning("Rule clause '" + clauseText + "' contains errors, in sentence: '" + getTargetSentence().getSentenceText() + "'", this.ac);
				}

				getTargetSentence().addFinalToken(SCELABEL_NORMAL, "", TOKEN_CLOSEPAR);
			}
		} else {
			reportWarning("Unexpected empty clause for rule, in sentence: '" + getTargetSentence().getSentenceText() + "'", this.ac);
		}
	}

	private void createNewRule() {
		if (this.ruleName.isEmpty()) {
			this.ruleName = generateDefaultRuleName();
			if (isReportDebug()) {
				reportDebug("No rule name specified, so '" + this.ruleName + "' has been generated for rule in sentence: " + getTargetSentence().getSentenceText(), this.ac);
			}
		}

		this.targetRule = CeRule.createNew(getTargetSentence().getSentenceText(), this.ruleName);

		CeSource currSrc = this.ac.getCurrentSource();
		
		if (currSrc != null) {
			currSrc.addAffectedRule(this.targetRule);
		} else {
			reportDebug("Cannot save rule as there is no current source", this.ac);
		}
	}

	private void addNewPremiseClause() {
		if (this.targetRule != null) {
			this.currentClause = CeClause.createNewPremiseClauseFor(this.targetRule);
			this.targetRule.addDirectPremiseClause(this.currentClause);
		} else {
			reportError("Null targetRule encountered when trying to add new premise clause to rule, for sentence: " + getTargetSentence().getSentenceText(), this.ac);
		}
	}

	private void addNewConclusionClause() {
		if (this.targetRule != null) {
			this.currentClause = CeClause.createNewConclusionClauseFor(this.targetRule);
			this.targetRule.addDirectConclusionClause(this.currentClause);
		} else {
			reportError("Null targetRule encountered when trying to add new conclusion clause to rule, for sentence: " + getTargetSentence().getSentenceText(), this.ac);
		}
	}

	private static String generateDefaultRuleName() {
		//This is used when a rule name has not been specified
		return "rule_" + CeQuery.nextPatternId();
	}

	private void saveCurrentClause() {
		if (this.currentClause == null) {
			reportError("Unxepected null currentClause whilst processing sentence: " + getTargetSentence().getSentenceText(), this.ac);
		}
	}

}
