package com.ibm.ets.ita.ce.store.parsing.tokenizer;
/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFactNormal;
import com.ibm.ets.ita.ce.store.parsing.model.TokenizerClause;
import com.ibm.ets.ita.ce.store.parsing.model.TokenizerNormalClause;
import com.ibm.ets.ita.ce.store.parsing.model.TokenizerRationaleClause;
import com.ibm.ets.ita.ce.store.parsing.model.TokenizerStartClause;

public class TokenizerFactSentence {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String SEPARATOR_THAT = "that";
	private static final String SEPARATOR_AND = "and";
	private static final String SEPARATOR_BECAUSE = "because";

	private static final String SCELABEL_NORMAL = "";
	private static final String SCELABEL_CONCEPT = "{Concept}:";
	private static final String SCELABEL_PROP = "{Property}:";
	private static final String SCELABEL_INSTVAL = "{Instance}:";
	private static final String SCELABEL_DELIMITER = "{Connector}:";
	private static final String SCELABEL_BECAUSE = "{Because}:";
	public static final String SCELABEL_QUOTE = "{Quote}:";

	private ActionContext ac = null;
	private ModelBuilder mb = null;
	private BuilderSentenceFactNormal sen = null;
	private ArrayList<String> tokens = null;
	
	private int posCtr = 0;
	private TokenizerStartClause startClause = null;
	private boolean isPatternClause = false;
	private boolean hasRationale = false;

	public ActionContext getActionContext() {
		return this.ac;
	}

	public BuilderSentenceFactNormal getSentence() {
		return this.sen;
	}

	public void tokenizeNormalSentence(ActionContext pAc, BuilderSentenceFactNormal pSen, boolean pIsPatternClause) {
		initialise(pAc, pSen, pIsPatternClause);

		//Create the clauses by iterating over all the rawTokens
		//The created clauses are not listed anywhere but are stored in the form of a
		//linked list from the startClause via nextClause/previousClause properties
		createClauses();
		
		//If a start clause was successfully extracted process and save all clauses
		if (this.startClause != null) {
			this.startClause.processClauseChain();
			saveOrReportErrors();
		} else {
			reportError("Unable to process clauses as no start clause was found");
		}
	}

	public void tokenizePatternClause(ActionContext pAc, BuilderSentenceFactNormal pSen, boolean pIsPatternClause) {
		initialise(pAc, pSen, pIsPatternClause);

		//Create the clauses by iterating over all the rawTokens
		//The created clauses are not listed anywhere but are stored in the form of a
		//linked list from the startClause via nextClause/previousClause properties
		createClauses();
		
		//If a start clause was successfully extracted process and save all clauses
		if (this.startClause != null) {
			this.startClause.processClauseChain();
			saveOrReportErrors();
		} else {
			reportError("Unable to process clauses as no start clause was found");
		}
	}

	private void initialise(ActionContext pAc, BuilderSentenceFactNormal pSen, boolean pIsPatternClause) {
		this.ac = pAc;
		this.mb = this.ac.getModelBuilder();
		this.sen = pSen;
		this.tokens = this.sen.getRawTokens();
		this.isPatternClause = pIsPatternClause;
	}

	public TokenizerStartClause getStartClause() {
		return this.startClause;
	}

	public void setStartClause(TokenizerStartClause pSc) {
		this.startClause = pSc;
	}

	public void reportError(String pErrorMsg) {
		//Convenience method
		this.sen.hasError(this.ac, pErrorMsg);
	}

	private String cachedValueFor(String pText) {
		//Convenience method
		return this.mb.getCachedStringValueLevel1(pText);
	}
	
	private boolean tokensRemain() {
		return this.tokens.size() > this.posCtr;
	}
	
	private String getNextToken() {
		return this.tokens.get(this.posCtr++);
	}
	
	private void createClauses() {
		ArrayList<String> currentClauseTokens = new ArrayList<String>();
		TokenizerClause lastClause = null;
		String lastDelimiter = null;
		boolean rationaleMode = false;

		while (tokensRemain()) {
			String thisToken = getNextToken();

			//Split the raw tokens into clauses at any delimiter tokens
			if ((thisToken.equals(SEPARATOR_AND)) || (thisToken.equals(SEPARATOR_THAT))) {
				//This is a delimiter token, so create a new clause
				if (!currentClauseTokens.isEmpty()) {
					lastClause = createClauseUsing(currentClauseTokens, lastDelimiter, lastClause, rationaleMode);
					currentClauseTokens = new ArrayList<String>();
				}

				lastDelimiter = thisToken;
			} else if (thisToken.equals(SEPARATOR_BECAUSE)) {
				//This is the rationale start token ("because"), so the remainder of the sentence is now rationale
				//Create the clause with rationaleMode=false because this is the token that indicates
				//rationale follows, so this clause which precedes it should be created as a normal clause
				lastClause = createClauseUsing(currentClauseTokens, lastDelimiter, lastClause, false);

				//All subsequent clauses will be saved with the rationaleMode flag set to true
				rationaleMode = true;
				currentClauseTokens = new ArrayList<String>();
				lastDelimiter = thisToken;
			} else {
				//This is a normal token so just add it to the current list
				currentClauseTokens.add(thisToken);
			}
		}

		//All the tokens have been processed, but the last clause may still need to be created
		if (!currentClauseTokens.isEmpty()) {
			createClauseUsing(currentClauseTokens, lastDelimiter, lastClause, rationaleMode);
		}
	}

	private TokenizerClause createClauseUsing(ArrayList<String> pRawTokens, String pDelimiter, TokenizerClause pLastClause, boolean pRatMode) {
		TokenizerClause tc = null;

		if (!pRatMode) {
			//This is a normal clause
			tc = TokenizerNormalClause.createUsing(this, pRawTokens, pDelimiter, pLastClause, this.isPatternClause, pRatMode);
		} else {
			//This is a rationale clause
			this.hasRationale = true;
			tc = TokenizerRationaleClause.createUsing(this, pRawTokens, pDelimiter, pLastClause);
		}

		if (tc != null) {
			//This is important because the act of processing the clause may have discovered that the
			//tokens proposed for it needed to be split into two clauses in the chain (in rare cases).
			//In either case it is the clause in the chain that should be returned from this method
			tc = tc.getClauseAtEndOfChain();
		}

		return tc;
	}

	private void saveOrReportErrors() {
		if (this.startClause != null) {
			ArrayList<TokenizerClause> allClauses = this.startClause.listAllClauses();
			String incompleteClauseText = "";

			//Test all the clauses and compile descriptions for any that are incomplete
			for (TokenizerClause thisClause : allClauses) {
				if (!thisClause.isComplete()) {
					incompleteClauseText += "\n" + thisClause.explainIncompleteness() + " [" + thisClause + "]";
				}
			}

			//Report error(s) or save the clauses
			if (!incompleteClauseText.isEmpty()) {
				//Some clauses were incomplete.  This means there are errors in the CE sentence.
				//Report the details and do not save the clauses
				if (!this.ac.isValidatingOnly()) {
					reportWarning(this.toString() + incompleteClauseText, this.ac);
				}
			} else {
				//All clauses were complete, so save them all
				saveClauses(allClauses);

				if (this.hasRationale) {
					saveRationale(allClauses);
				}
			}
		}
	}

	private void saveClauses(ArrayList<TokenizerClause> pAllClauses) {
		for (TokenizerClause thisClause : pAllClauses) {
			//Save the clause and specify that everything should be saved (not just the tokens)
			thisClause.save(false);
		}

		//This records that this chain of clauses (and therefore the sentence) has been validated and saved
		this.sen.validationComplete();
	}
	
	private void saveRationale(ArrayList<TokenizerClause> pAllClauses) {
		String ruleName = null;
		StringBuilder sb = new StringBuilder();

		for (TokenizerClause thisClause : pAllClauses) {
			if (thisClause instanceof TokenizerRationaleClause) {
				//The clause has already been saved in the standard way, but here
				//we extract the rationale specific information
				TokenizerRationaleClause ratClause = (TokenizerRationaleClause)thisClause;

				ruleName = ratClause.getRuleName();
				
				if (sb.length() != 0) {
					sb.append(" and ");
					this.sen.addRationaleToken("and");
				}
				sb.append(ratClause.getRationaleText());
				
				for (String ratToken : ratClause.getRationaleTokens()) {
					this.sen.addRationaleToken(ratToken);
				}
			}
		}
		
		//Now save the rule name and rationale text
		this.sen.setRationaleRuleName(ruleName);
		this.sen.setRationaleText(sb.toString());
	}

	public void addNormalCeToken(String pToken) {
		this.sen.addFinalToken(SCELABEL_NORMAL, "", cachedValueFor(pToken));
	}

	public void addDelimiterCeToken(String pToken) {
		this.sen.addFinalToken(SCELABEL_DELIMITER, "", cachedValueFor(pToken));
	}

	public void addBecauseCeToken() {
		this.sen.addFinalToken(SCELABEL_BECAUSE, "", SEPARATOR_BECAUSE);
	}

	public void addConceptCeToken(String pToken) {
		this.sen.addFinalToken(SCELABEL_CONCEPT, "", cachedValueFor(pToken));
	}

	public void addInstanceCeToken(String pToken, String pQuoteType) {
		String qText = null;
		
		if (pQuoteType != null) {
			qText = cachedValueFor(pQuoteType);
		}

		if (qText != null) {
			this.sen.addFinalToken(SCELABEL_QUOTE, "", qText);
		}

		this.sen.addFinalToken(SCELABEL_INSTVAL, "", cachedValueFor(pToken));

		if (qText != null) {
			this.sen.addFinalToken(SCELABEL_QUOTE, "", qText);
		}
	}

	public void addValueCeToken(String pToken, String pQuoteType) {
		String qText = null;
		
		if (pQuoteType != null) {
			qText = cachedValueFor(pQuoteType);
		}

		if (qText != null) {
			this.sen.addFinalToken(SCELABEL_QUOTE, "", qText);
		}

		this.sen.addFinalToken(SCELABEL_NORMAL, "", cachedValueFor(pToken));

		if (qText != null) {
			this.sen.addFinalToken(SCELABEL_QUOTE, "", qText);
		}
	}

	public void addPropetyCeTokens(CeProperty pProp) {
		StringBuilder sb = new StringBuilder();
		sb.append(CeSentence.PROPDEF_PREFIX);
		sb.append(pProp.calculateDomainConceptName());
		sb.append(",");
		sb.append(pProp.getRangeConceptName());
		sb.append(CeSentence.PROPDEF_SUFFIX);
		
		this.sen.addFinalToken(SCELABEL_PROP, cachedValueFor(sb.toString()), cachedValueFor(pProp.getPropertyName()));
	}
	
	@Override
	public String toString() {
		return "(NewTokenizerFactSentence) " + this.sen.getSentenceText();
	}

}