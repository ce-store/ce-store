package com.ibm.ets.ita.ce.store.conversation.processor;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionExecutionHandler;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ConvSentence;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ProcessedWord;

public class SentenceProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ActionContext ac = null;
	private ConvSentence convSentence = null;

	private ArrayList<ProcessedWord> allProcessedWords = null;

	public SentenceProcessor(ActionContext pAc, ConvSentence pConvSen) {
		this.ac = pAc;
		this.convSentence = pConvSen;
		this.allProcessedWords = new ArrayList<ProcessedWord>();
	}

	public ResultOfAnalysis processSentence() {
		QuestionExecutionHandler qe = new QuestionExecutionHandler(this.ac, this.convSentence.getSentenceText(),
				false, false, System.currentTimeMillis());
		String answerText = qe.processQuestionAndReturnAnswerText();

		return ResultOfAnalysis.createQuestionResponseWithGistOnly(answerText);
	}

	public ConvSentence getConvSentence() {
		return this.convSentence;
	}

	public ArrayList<ProcessedWord> getAllProcessedWords() {
		return this.allProcessedWords;
	}

	public void addProcessedWord(ProcessedWord pWord) {
		this.allProcessedWords.add(pWord);
	}

	public String getSentenceText() {
		return this.convSentence.getSentenceText();
	}

}
