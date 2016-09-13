package com.ibm.ets.ita.ce.store.conversation.processor;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ConvPhrase;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ConvSentence;

public class RawLexicalProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private SentenceProcessor sp = null;
	private ActionContext ac = null;
	private ConvPhrase convPhrase = null;

	public RawLexicalProcessor(ActionContext pAc) {
		this.ac = pAc;
	}

	public ResultOfAnalysis processConversationText(String pConvText) {
		ResultOfAnalysis result = null;

		result = handleAsNaturalLanguageText(pConvText);

		// If no CE text or no info messages were generated then nothing was
		// understood from the processing
		if (this.convPhrase.isAssertion()) {
			if (result.getInfoMessage() == null) {
				if ((result.getCeText() == null) || (result.getCeText().isEmpty())) {
					result = ResultOfAnalysis.msgNotUnderstood();
				}
			}
		}

		return result;
	}

	private ResultOfAnalysis handleAsNaturalLanguageText(String pConvText) {
		ResultOfAnalysis overallResult = new ResultOfAnalysis();

		this.convPhrase = ConvPhrase.createNewPhrase(this.ac, pConvText);

		for (ConvSentence thisSen : this.convPhrase.getChildSentences()) {
			this.sp = new SentenceProcessor(this.ac, thisSen);
			ResultOfAnalysis thisResult = sp.processSentence();

			if (thisResult != null) {
				if ((overallResult.getCeText() == null) && (overallResult.getGistText() == null)) {
					// No CE or gist yet, so just replace the result
					overallResult = thisResult;
				} else {
					overallResult.incorporate(thisResult);
				}
			}
		}

		return overallResult;
	}

}
