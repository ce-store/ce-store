package com.ibm.ets.ita.ce.store.conversation.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

public class RawLexicalProcessor {
//	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";
//
//	private static final String SRC_CONVDEBUG = "conv_debug";
//
//	private ActionContext ac = null;
//	private ConvPhrase convPhrase = null;
//	private CeInstance cardInstance = null;
//	private SentenceProcessor sp = null;
//
//	private boolean isGeneratingConversationCe = false;
//	private boolean useDefaultScoring = false;
//
//	public RawLexicalProcessor(ActionContext pAc, CeInstance pCardInst, boolean pGenConvCe, boolean pUseDefaultScoring) {
//		this.ac = pAc;
//		this.cardInstance = pCardInst;
//		this.isGeneratingConversationCe = pGenConvCe;
//		this.useDefaultScoring = pUseDefaultScoring;
//	}
//
//	public SentenceProcessor getSentenceProcessor() {
//		return this.sp;
//	}
//
//	public ResultOfAnalysis processConversationText(String pConvText) {
//		ResultOfAnalysis result = null;
//
//		result = handleAsNaturalLanguageText(pConvText);
//
//		//If no CE text or no info messages were generated then nothing was understood from the processing
//		if (this.convPhrase.isAssertion()) {
//			if (result.getInfoMessage() == null) {
//				if ((result.getCeText() == null) || (result.getCeText().isEmpty())) {
//					result = ResultOfAnalysis.msgNotUnderstood();
//				}
//			}
//		}
//
//		return result;
//	}
//	
//	private ResultOfAnalysis handleAsNaturalLanguageText(String pConvText) {
//		ResultOfAnalysis overallResult = new ResultOfAnalysis();
//
//		this.convPhrase = ConvPhrase.createNewPhrase(this.ac, pConvText);
//
//		for (ConvSentence thisSen : this.convPhrase.getChildSentences()) {
//			this.sp = new SentenceProcessor(this.ac, this.cardInstance, thisSen, this.useDefaultScoring);
//			ResultOfAnalysis thisResult = sp.processSentence();
//
//			if (thisResult != null) {
//				if ((overallResult.getCeText() == null) && (overallResult.getGistText() == null)) {
//					//No CE or gist yet, so just replace the result
//					overallResult = thisResult;
//				} else {
//					overallResult.incorporate(thisResult);
//				}
//			}
//
//			//Generate the CE that describes the conversation fragments (words, sentences etc)
//			if (this.isGeneratingConversationCe) {
//				generateConvCe(sp);
//			}
//		}
//
//		return overallResult;
//	}
//
//	private void generateConvCe(SentenceProcessor pSp) {
//		StringBuilder sb = new StringBuilder();
//
//		pSp.generateConvCe(sb, this.convPhrase);
//		saveAsCe(sb);
//	}
//
//	private void saveAsCe(StringBuilder pSb) {
//		if (pSb.length() > 0) {
//			StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);
//			CeSource tgtSrc = CeSource.createNewFormSource(this.ac, SRC_CONVDEBUG, SRC_CONVDEBUG);
//			sa.saveCeText(pSb.toString(), tgtSrc);
//		} else {
//			reportWarning("No debug sentences generated from conversation text:" + this.convPhrase.getPhraseText(), this.ac);
//		}
//	}
//
}