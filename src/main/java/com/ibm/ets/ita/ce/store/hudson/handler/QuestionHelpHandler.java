package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.helper.Interpretation;
import com.ibm.ets.ita.ce.store.hudson.helper.InterpretationSummary;
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.hudson.helper.Suggestion;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class QuestionHelpHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_QT = "question_text";
	private static final String JSON_SUGGS = "suggestions";
	private static final String JSON_D_INTS = "interpretations";
	private static final String JSON_BT = "before_text";
	private static final String JSON_AT = "after_text";

	protected ArrayList<Suggestion> suggestions = null;
	protected ArrayList<Interpretation> interpretations = null;
	private int maxSuggs = 10;	//TODO: Abstract this

	public QuestionHelpHandler(WebActionContext pWc, boolean pDebug, String pQt, long pStartTime) {
		super(pWc, pDebug, Question.create(pQt), pStartTime);

		this.interpretations = new ArrayList<Interpretation>();

		if (getConvConfig() != null) {
			this.maxSuggs = getConvConfig().getMaxSuggestions();
		}
	}

	protected CeStoreJsonObject createJsonResponse() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		CeStoreJsonArray jInts = new CeStoreJsonArray();

		result.put(JSON_SUGGS, jsonForSuggestions());

		if (this.debug) {
			CeStoreJsonObject jDebug = new CeStoreJsonObject();
			jDebug.put(JSON_ET, System.currentTimeMillis() - this.startTime);
			createJsonAlerts(jDebug);

			if (!this.interpretations.isEmpty()) {
				for (Interpretation thisInt : this.interpretations) {
					jInts.add(thisInt.textSummary());
				}
				jDebug.put(JSON_D_INTS, jInts);
			}

			result.put(JSON_DEBUG, jDebug);
		}

		return result;
	}

	private CeStoreJsonArray jsonForSuggestions() {
		CeStoreJsonArray result = new CeStoreJsonArray();

		if (this.suggestions != null) {
			for (Suggestion thisSugg : this.suggestions) {
				result.add(jsonForSuggestion(thisSugg));
			}
		}

		return result;
	}

	private static CeStoreJsonObject jsonForSuggestion(Suggestion pSugg) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		if (pSugg.hasBeforeText()) {
			result.put(JSON_BT, pSugg.getBeforeText());
		}

		result.put(JSON_QT, pSugg.getOriginalText());

		if (pSugg.hasAfterText()) {
			result.put(JSON_AT, pSugg.getAfterText());
		}

		return result;
	}

	@Override
	public CeStoreJsonObject handleQuestion() {
		long st = System.currentTimeMillis();
		CeStoreJsonObject result = null;

		interpretQuestion();

		if (!this.question.endsWithPunctuation()) {
			if (!isCeText()) {
				ArrayList<Suggestion> theseSuggs = new ArrayList<Suggestion>();
				computeSuggestions(0, theseSuggs);
				Collections.sort(theseSuggs);
		
				this.suggestions = theseSuggs;
			}
		}

		result = createResult();

		reportDebug("handleQuestion=" + new Long(System.currentTimeMillis() - st).toString(), this.ac);
		return result;
	}

	private boolean isCeText() {
		return ModifierHandler.isCeModifier(this.ac, this.allWords);
	}

	private void computeSuggestions(int pStartIndex, ArrayList<Suggestion> pSuggs) {
		String lcSeekPhrase = getPartialText(pStartIndex);

		if ((lcSeekPhrase != null) && !lcSeekPhrase.isEmpty()) {
			seekMatchesUsing(lcSeekPhrase, pSuggs);

			if (pSuggs.isEmpty() || (pSuggs.size() <= this.maxSuggs)) {
				if (this.allWords.size() > (pStartIndex + 1)) {
					computeSuggestions(pStartIndex + 1, pSuggs);
				}
			}
		}
	}

	private String getPartialText(int pStartIndex) {
		String result = null;
		int wordCount = this.allWords.size() - 1;

		for (int i = pStartIndex; i <= wordCount; i++) {
			ProcessedWord thisWord = this.allWords.get(i);

			if (result == null) {
				result = thisWord.getLcWordText();
			} else {
				result += " " + thisWord.getLcWordText();
			}
		}

		return result;
	}

	private void seekMatchesUsing(String pSeekText, ArrayList<Suggestion> pSuggs) {
		suggestInstanceNames(pSeekText, pSuggs);
		suggestConceptNames(pSeekText, pSuggs);
		suggestPropertyNames(pSeekText, pSuggs);
	}

	private void suggestInstanceNames(String pFragment, ArrayList<Suggestion> pSuggs) {
		ArrayList<CeInstance> instList = this.ac.getIndexedEntityAccessor().calculateInstancesWithNameStarting(this.ac, pFragment);

		if (!instList.isEmpty()) {
			ArrayList<CeInstance> trimmedList = new ArrayList<CeInstance>();
	
			for (CeInstance thisInst : instList) {
				if (!thisInst.isOnlyConceptNamed(this.ac, GenericHandler.CON_CONFCON)) {
					trimmedList.add(thisInst);
				}
			}

			suggestInstanceNamesFromList(pFragment, trimmedList, pSuggs);
		}
	}

	private void suggestInstanceNamesFromList(String pFragment, ArrayList<CeInstance> pTgtInsts, ArrayList<Suggestion> pSuggestions) {
		TreeMap<String, ArrayList<String>> finalPairs = new TreeMap<String, ArrayList<String>>();

		for (CeInstance thisInst : pTgtInsts) {
			if (finalPairs.size() >= this.maxSuggs) {
				break;
			}

			processInstance(thisInst, pFragment, finalPairs);
		}

		for (String thisKey : finalPairs.keySet()) {
			ArrayList<String> thisPair = finalPairs.get(thisKey);
			String qText = getQuestionText().trim();
			String beforePart = thisPair.get(0);
			String afterPart = thisPair.get(1);
			int bpLen = beforePart.length();

			if (beforePart.isEmpty()) {
				beforePart = " ";
			}

			qText = qText.substring(0, (qText.length() - bpLen));
			qText += beforePart;

			if (pSuggestions.size() >= this.maxSuggs) {
				break;
			}

			pSuggestions.add(Suggestion.create(qText, null, afterPart));
		}
	}

	private int processInstance(CeInstance pInst, String pFragment, TreeMap<String, ArrayList<String>> pFinalPairs) {
		int result = 0;
		String lcFrag = pFragment.toLowerCase();

		if (!lcFrag.isEmpty()) {
			for (String possId : pInst.getInstanceIdentifiers(this.ac)) {
				String lcId = possId.toLowerCase();

				if (lcId.contains(lcFrag)) {
					String beforePart = possId.substring(0, lcFrag.length());
					String afterPart = possId.substring(lcFrag.length());

					ArrayList<String> thisPair = new ArrayList<String>();
					thisPair.add(beforePart);
					thisPair.add(afterPart);

					pFinalPairs.put(afterPart, thisPair);
					result = 1;
					break;
				}
			}
		}

		return result;
	}

	private void suggestConceptNames(String pFragment, ArrayList<Suggestion> pSuggs) {
		ArrayList<String> conNames = new ArrayList<String>();
		ArrayList<String> finalVals = new ArrayList<String>();

		conNames.add(CON_SUPPCON);

		for (CeConcept thisCon : this.ac.getModelBuilder().listAllConcepts()) {
			if (!thisCon.isThing()) {
				if (!thisCon.equalsOrHasParentNamed(this.ac, conNames)) {
					String lcId = thisCon.pluralFormName(this.ac).toLowerCase();

					if (pFragment.isEmpty() || lcId.startsWith(pFragment)) {
						String afterBit = thisCon.pluralFormName(this.ac).substring(pFragment.length());

						finalVals.add(afterBit);
					}

					if (pSuggs.size() >= maxSuggs) {
						break;
					}
				}
			}
		}

		for (String afterBit : finalVals) {
			if (pSuggs.size() >= this.maxSuggs) {
				break;
			}

			pSuggs.add(Suggestion.create(getQuestionText(), null, afterBit));
		}
	}

	private void suggestPropertyNames(String pFragment, ArrayList<Suggestion> pSuggs) {
		HashSet<String> propNames = new HashSet<String>();
		ArrayList<String> finalVals = new ArrayList<String>();

		for (CeProperty thisProp : this.ac.getModelBuilder().getAllProperties()) {
			String propName = thisProp.getPropertyName();
			
			propNames.add(propName);
		}

		for (String propName : propNames) {
			if (propName.startsWith(pFragment)) {
				String afterBit = propName.substring(pFragment.length(), propName.length());
				finalVals.add(afterBit);
			}
		}

		for (String afterBit : finalVals) {
			if (pSuggs.size() >= this.maxSuggs) {
				break;
			}

			pSuggs.add(Suggestion.create(getQuestionText(), null, afterBit));
		}

	}

	protected CeStoreJsonObject createResult() {
		long st = System.currentTimeMillis();
		CeStoreJsonObject result = null;

		if (this.suggestions != null) {
			Collections.sort(this.suggestions);
		}

		if (this.debug) {
			this.interpretations = InterpretationSummary.generateInterpretations(this.allWords);
		}

		result = createJsonResponse();
		reportDebug("createResult=" + new Long(System.currentTimeMillis() - st).toString(), this.ac);

		return result;
	}

}
