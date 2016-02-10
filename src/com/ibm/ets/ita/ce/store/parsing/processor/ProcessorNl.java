package com.ibm.ets.ita.ce.store.parsing.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.bufferedReaderFromString;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;

public class ProcessorNl {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = ProcessorNl.class.getName();
	private static final String PACKAGE_NAME = ProcessorNl.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private ActionContext ac = null;
	private SentenceParserNl sp = null;
	private NlSentence lastNlSentence = null;
	private ArrayList<NlSentence> allNlSentences = new ArrayList<NlSentence>();

	public ProcessorNl(ActionContext pAc, ArrayList<String> pAcronymList, char[] pDelimList, char[] pSpecQuoteList, boolean pIgnoreCrs, boolean pTrackSingle, boolean pTrackDouble, boolean pTrackSpecial) {
		this.ac = pAc;
		this.sp = new SentenceParserNl(this.ac, this, pAcronymList, pDelimList, pSpecQuoteList, pIgnoreCrs, pTrackSingle, pTrackDouble, pTrackSpecial);
	}

	protected SentenceParserNl getSentenceParser() {
		return this.sp;
	}

	public ArrayList<NlSentence> getAllNlSentences() {
		return this.allNlSentences;
	}

	public int getSentenceCount() {
		return this.allNlSentences.size();
	}

	public void extractNlSentencesFromString(String pText) {
		final String METHOD_NAME = "extractNlSentencesFromString";

		BufferedReader br = bufferedReaderFromString(pText);
		getSentenceParser().doParsing(br, StoreActions.MODE_NORMAL);

		try {
			br.close();
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	protected void processNlSentence(NlSentence pSentence) {
		if (pSentence != null) {
			this.allNlSentences.add(pSentence);
			this.lastNlSentence = pSentence;
		}
	}

	protected void recordUnevenQuotes() {
		if (this.lastNlSentence != null) {
			this.lastNlSentence.setHasUnevenQuotes(true);
		}
	}

}