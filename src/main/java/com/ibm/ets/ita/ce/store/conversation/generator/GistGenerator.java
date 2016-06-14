package com.ibm.ets.ita.ce.store.conversation.generator;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;

import java.util.HashSet;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.MatchedTriple;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.conversation.processor.SentenceProcessor;
import com.ibm.ets.ita.ce.store.generation.GeneralGenerator;
import com.ibm.ets.ita.ce.store.model.CeConcept;

public class GistGenerator extends GeneralGenerator {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_SUMTHING = "summarisable thing";
	private static final String CON_CUSTSUMTHING = "custom summarisable thing";
	private static final String CON_GRAPHSUMTHING = "graphical summarisable thing";

	private SentenceProcessor sp = null;

	public GistGenerator(ActionContext pAc, SentenceProcessor pSp) {
		super(pAc);
		
		this.sp = pSp;
	}

	private static boolean conceptHasGistForm(CeConcept pCon) {
		return (pCon != null) && pCon.hasParentNamed(CON_SUMTHING);
	}

	private static boolean conceptHasCustomGistForm(CeConcept pCon) {
		return pCon.hasParentNamed(CON_CUSTSUMTHING);
	}

	private static boolean conceptHasGraphicalGistForm(CeConcept pCon) {
		return pCon.hasParentNamed(CON_GRAPHSUMTHING);
	}

	public static String generateGistFor(ActionContext pAc, SentenceProcessor pSp) {
		GistGenerator gg = new GistGenerator(pAc, pSp);

		return gg.calculateGistText();
	}

	private String calculateGistText() {
		StringBuilder sb = new StringBuilder();
		CeConcept subCon = this.sp.getSubjectConcept();

		if (conceptHasGistForm(subCon)) {
			//Gist processing
			if (conceptHasCustomGistForm(subCon)) {
				//Custom Gist
				createGistCustom(sb);
			} else if (conceptHasGraphicalGistForm(subCon)) {
				//Graphical Gist
				createGistGraphical(sb);
			} else {
				//Standard Gist
				createGistStandard(sb);
			}
		}

		return sb.toString();
	}

	private void createGistCustom(StringBuilder pSb) {
		appendToSb(pSb, "(custom gist not yet implemented)");
		appendToSb(pSb, "");

		createGistStandard(pSb);
	}

	private void createGistGraphical(StringBuilder pSb) {
		appendToSb(pSb, "(graphical gist not yet implemented)");
		appendToSb(pSb, "");

		createGistStandard(pSb);
	}

	private void createGistStandard(StringBuilder pSb) {
		gistForConLedTriples(pSb);
	}

	private void gistForConLedTriples(StringBuilder pSb) {
		gistForTriplesMatchingSubjectConcept(pSb, this.sp.computeUniqueConcepts());

		gistForUnprocessedWords(pSb);
	}

	private void gistForTriplesMatchingSubjectConcept(StringBuilder pSb, HashSet<CeConcept> pAllCons) {
		boolean firstTime = true;
		
		for (CeConcept thisCon : pAllCons) {
			if (firstTime) {
				appendToSb(pSb, "You said?");
				firstTime = false;
			} else {
				appendToSbNoNl(pSb, ", ");
			}

			appendToSbNoNl(pSb, thisCon.getConceptName());
		}

		appendToSb(pSb, ":");

		gistForTriplesMatchingConcept(pSb);
	}

	private void gistForTriplesMatchingConcept(StringBuilder pSb) {
		boolean firstTime = true;

		for (MatchedTriple thisMt : this.sp.getMatchedTriples()) {
			String predName = thisMt.getPredicateProperty().getPropertyName();
			String objVal = "";

			if (thisMt.isObjectInstance()) {
				objVal = thisMt.getObjectInstanceId();
			} else {
				objVal = thisMt.getObjectValue();
			}

			if (!firstTime) {
				appendToSb(pSb, ",");
			}

			appendToSbNoNl(pSb, predName + " is " + objVal);
			firstTime = false;
		}
	}

	private void gistForUnprocessedWords(StringBuilder pSb) {
		boolean firstTime = true;

		for (ProcessedWord thisWord : this.sp.getAllProcessedWords()) {
			if (thisWord.isUnmatchedWord()) {
				if (firstTime) {
					appendToSb(pSb, ".");
					appendToSbNoNl(pSb, "Unhandled words: ");
					firstTime = false;
				} else {
					appendToSbNoNl(pSb, ", ");
				}
				
				appendToSbNoNl(pSb, thisWord.getWordText());
			}
		}
	}

}
