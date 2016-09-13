package com.ibm.ets.ita.ce.store.parsing.saver;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFact;

public abstract class SentenceSaver {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected ActionContext ac = null;

	protected BuilderSentence targetSentence = null;
	protected CeConcept targetConcept = null;
	protected CeSentence sentenceInstance = null;
	protected String sentenceText = null;

	protected SentenceSaver(ActionContext pAc, BuilderSentence pSentence) {
		this.ac = pAc;
		initialise(pSentence);
	}

	private void initialise(BuilderSentence pSentence) {
		this.targetSentence = pSentence;

		if (this.targetSentence != null) {
			this.sentenceInstance = this.targetSentence.convertToSentence(this.ac);
			this.targetConcept = this.targetSentence.getTargetConcept();
			this.sentenceText = this.targetSentence.getSentenceText();
		}
	}

	protected ModelBuilder getModelBuilder() {
		return this.ac.getModelBuilder();
	}

	public static void saveValidSentence(ActionContext pAc, BuilderSentence pSentence, BuilderSentence pLastSentence) {		
		if (pSentence.isFactSentence()) {
			BuilderSentenceFact factSentence = (BuilderSentenceFact)pSentence;
			SentenceSaverFact ssF = new SentenceSaverFact(pAc, factSentence);
			ssF.saveFactSentence();
		} else if (pSentence.isModelSentence()) {
			SentenceSaverModel ssM = new SentenceSaverModel(pAc, pSentence);
			ssM.saveModelSentence();
		} else if (pSentence.isAnnotationSentence()) {
			SentenceSaverAnnotation ssA = new SentenceSaverAnnotation(pAc, pSentence, pLastSentence);
			ssA.saveAnnotationSentence();
		} else if ((pSentence.isRuleSentence()) || (pSentence.isQuerySentence()) || (pSentence.isCommandSentence())) {
			SentenceSaverGeneral ssG = new SentenceSaverGeneral(pAc, pSentence);
			ssG.storeValidSentence();
		} else {
			reportError("Cannot save result of processing sentence as type cannot be detected, for sentence: " + pSentence.getSentenceText(), pAc);
		}
	}

	public static void saveInvalidSentence(ActionContext pAc, BuilderSentence pSentence) {
		SentenceSaverGeneral ssG = new SentenceSaverGeneral(pAc, pSentence);
		ssG.storeInvalidSentence();
	}

	protected void storeValidSentence() {
		if (this.targetSentence.isValid()) {
			this.ac.getSessionCreations().recordValidSentence(this.sentenceInstance);
		} else {
			reportError("Unexpected invalid sentence encountered during storeSentence(), " + this.sentenceText, this.ac);
		}
	}

	protected void storeInvalidSentence() {
		if (!this.targetSentence.isValid()) {
			this.ac.getSessionCreations().recordInvalidSentence(this.sentenceInstance);
		} else {
			reportError("Unexpected valid sentence encountered during storeInvalidSentence(), " + this.sentenceText, this.ac);
		}
	}

}
