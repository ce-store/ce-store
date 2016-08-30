package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.helper.Answer;
import com.ibm.ets.ita.ce.store.hudson.helper.AnswerCoords;
import com.ibm.ets.ita.ce.store.hudson.helper.AnswerMedia;
import com.ibm.ets.ita.ce.store.hudson.helper.AnswerReply;
import com.ibm.ets.ita.ce.store.hudson.helper.AnswerResultSet;
import com.ibm.ets.ita.ce.store.hudson.helper.ChosenWord;
import com.ibm.ets.ita.ce.store.hudson.helper.ConvConfig;
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.hudson.helper.Source;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class QuestionExecutionHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = QuestionHandler.class.getName();
	private static final String PACKAGE_NAME = QuestionHandler.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final String JSON_QUES = "question";
	private static final String JSON_ANS = "answers";
	private static final String JSON_Q_TEXT = "text";
	private static final String JSON_Q_INTCONF = "interpretation_confidence";
	private static final String JSON_Q_ANSCONF = "ability_to_answer_confidence";
//	private static final String JSON_D_SQL = "sql_query";
	private static final String JSON_A_QUESINT = "question_interpretation";
	private static final String JSON_A_RESULT_TEXT = "result_text";
	private static final String JSON_A_RESULT_SET = "result_set";
	private static final String JSON_A_MEDIA = "result_media";
	private static final String JSON_A_MEDIA_ID = "id";
	private static final String JSON_A_MEDIA_URL = "url";
	private static final String JSON_A_MEDIA_CREDIT = "credit";
	private static final String JSON_A_COORDS = "result_coords";
	private static final String JSON_A_COORDS_ID = "id";
	private static final String JSON_A_COORDS_LAT = "lat";
	private static final String JSON_A_COORDS_LON = "lon";
	private static final String JSON_A_COORDS_AL1 = "address_line_1";
	private static final String JSON_A_COORDS_PC = "postcode";
	private static final String JSON_A_RESULT_CODE = "result_code";
	private static final String JSON_A_SET_TITLE = "title";
	private static final String JSON_A_SET_HDRS = "headers";
	private static final String JSON_A_SET_ROWS = "rows";
	private static final String JSON_A_CHATTY = "chatty_text";
	private static final String JSON_A_ANSCONF = "answer_confidence";
	private static final String JSON_A_SOURCE = "source";
	private static final String JSON_S_NAME = "name";
	private static final String JSON_S_URL = "url";

//	private static final String CON_API = "api concept";
	private static final String PROP_ACTION = "action";
//	private static final String PROP_TARGET_CONCEPT = "target concept";
//	private static final String PROP_TARGET_RELATION = "target property";
//	private static final String PROP_URL_TEMPLATE = "url template";
//	private static final String PROP_USERNAME = "api user";
//	private static final String PROP_PASSWORD = "api password";
//	private static final String PROP_ROOT_JSON = "root JSON element name";
//	private static final String PROP_ID_ELEMENT = "JSON identifier element";
	private static final String ACTION_TERMINATE = "terminate";

//	private static final String CE_ID_SEP = "_";
//	private static final String NEWLINE = System.getProperty("line.separator");

//	private static final SecureRandom random = new SecureRandom();

//	private QuestionInterpreter qi = null;
	private AnswerReply reply = null;
	protected ArrayList<ChosenWord> chosenWords = null;
	String resultTitleSingle = null;
	String resultTitlePlural = null;

//	private HashMap<URL, CeSource> ceSources = new HashMap<URL, CeSource>();

	public QuestionExecutionHandler(WebActionContext pWc, boolean pDebug, String pQt, long pStartTime) {
		super(pWc, pDebug, Question.create(pQt), pStartTime);

		this.chosenWords = new ArrayList<ChosenWord>();
	}

	protected CeStoreJsonObject createJsonResponse() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		CeStoreJsonObject jDebug = new CeStoreJsonObject();
		CeStoreJsonObject jAlerts = new CeStoreJsonObject();

		result.put(JSON_QUES, createQuestionJson(jDebug));
		result.put(JSON_ANS, createAnswersJson());

		createJsonAlerts(jAlerts);
		result.put(JSON_ALERTS, jAlerts);

		if (this.debug) {
			jDebug.put(JSON_ET, System.currentTimeMillis() - this.startTime);
			createJsonDebugs(jDebug);
			result.put(JSON_DEBUG, jDebug);
		}

		return result;
	}

	private CeStoreJsonObject createQuestionJson(CeStoreJsonObject pJsonDebug) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		result.put(JSON_Q_TEXT, getQuestionText());
		result.put(JSON_Q_INTCONF, interpretationConfidenceFor(this.question));
		result.put(JSON_Q_ANSCONF, abilityToAnswerConfidenceFor(this.question));

//		if ((this.debug) && (this.qi != null)) {
//			String sqlText = this.qi.getSqlText();
//
//			if ((sqlText != null) && (!sqlText.isEmpty())) {
//				pJsonDebug.put(JSON_D_SQL, sqlText);
//			}
//		}

		return result;
	}

	private CeStoreJsonArray createAnswersJson() {
		CeStoreJsonArray result = new CeStoreJsonArray();

		for (Answer thisAnswer : this.reply.sortedAnswers()) {
			result.add(createIndividualAnswerJson(thisAnswer, false));
		}

		return result;
	}

	private CeStoreJsonObject createIndividualAnswerJson(Answer pAnswer, boolean pError) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		try {
			result.put(JSON_A_QUESINT, pAnswer.computeInterpretation(this.ac, getQuestionWords()));

			if (pAnswer.hasStandardAnswerText()) {
				result.put(JSON_A_RESULT_TEXT, pAnswer.getStandardAnswerText());
			}

			if (pAnswer.hasAnswerSet()) {
				result.put(JSON_A_RESULT_SET, createAnswerSetJson(pAnswer.getAnswerSet()));
			}

			if (pAnswer.hasAnswerMedia()) {
				result.put(JSON_A_MEDIA, createMediaJson(pAnswer.getAnswerMedia()));
			}

			if (pAnswer.hasAnswerCoords()) {
				result.put(JSON_A_COORDS, createCoordsJson(pAnswer.getAnswerCoords()));
			}

			if (pAnswer.hasAnswerCode()) {
				result.put(JSON_A_RESULT_CODE, pAnswer.getAnswerCode());
			}

			result.put(JSON_A_ANSCONF, answerConfidenceFor(pAnswer));

			if (pAnswer.hasChattyAnswerText()) {
				result.put(JSON_A_CHATTY, pAnswer.getChattyAnswerText());
			}

			if (pAnswer.hasSource()) {
				result.put(JSON_A_SOURCE, createSourceJson(pAnswer.getSource()));
			}
		} catch (Exception e) {
			if (!pError) {
				//Don't report exceptions as errors if an error has already been raised (to prevent recursion)
				Answer errAnswer = handleExceptionAsAnswer(this.ac, e, ANSCODE_GENERROR, this.reply);
				result = createIndividualAnswerJson(errAnswer, true);
			} else {
				System.out.println("Error not reported due to recursion risk");
				e.printStackTrace();
			}
		}

		return result;
	}

	private int answerConfidenceFor(Answer pAnswer) {
		int result = -1;
		ConvConfig thisCc = getConvConfig();

		if (thisCc != null) {
			if (thisCc.computeAnswerConfidence()) {
				result = pAnswer.getAnswerConfidence();
			} else {
				result = thisCc.defaultAnswerConfidence();
			}
		}

		return result;
	}

	private int interpretationConfidenceFor(Question pQuestion) {
		int result = -1;
		ConvConfig thisCc = getConvConfig();

		if (thisCc != null) {
			if (thisCc.computeInterpretationConfidence()) {
				result = pQuestion.getInterpretationConfidence();
			} else {
				result = thisCc.defaultInterpretationConfidence();
			}
		}

		return result;
	}

	private int abilityToAnswerConfidenceFor(Question pQuestion) {
		int result = -1;
		ConvConfig thisCc = getConvConfig();

		if (thisCc != null) {
			if (thisCc.computeAbilityToAnswerConfidence()) {
				result = pQuestion.getAbilityToAnswerConfidence();
			} else {
				result = thisCc.defaultAbilityToAnswerConfidence();
			}
		}

		return result;
	}

	private ArrayList<ProcessedWord> getQuestionWords() {
		ArrayList<ProcessedWord> result = new ArrayList<ProcessedWord>();

		for (ProcessedWord thisWord : this.allWords) {
			if (thisWord.isQuestionWord()) {
				result.add(thisWord);
			}
		}

		return result;
	}

	private static CeStoreJsonObject createAnswerSetJson(AnswerResultSet pAnswerSet) {
		CeStoreJsonObject result = new CeStoreJsonObject();
		CeStoreJsonArray hdrs = new CeStoreJsonArray();
		CeStoreJsonArray rows = new CeStoreJsonArray();

		for (String thisHdr : pAnswerSet.getHeaders()) {
			hdrs.add(thisHdr);
		}

		for (ArrayList<String> thisRow : pAnswerSet.getRows()) {
			CeStoreJsonArray jRow = new CeStoreJsonArray();

			for (String thisVal : thisRow) {
				jRow.add(thisVal);
			}

			rows.add(jRow);
		}

		if (pAnswerSet.getTitle() != null) {
			result.put(JSON_A_SET_TITLE, pAnswerSet.getTitle());
		}
		result.put(JSON_A_SET_HDRS, hdrs);
		result.put(JSON_A_SET_ROWS, rows);

		return result;
	}

	private static CeStoreJsonObject createMediaJson(AnswerMedia pAnswerMedia) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		result.put(JSON_A_MEDIA_ID, pAnswerMedia.getId());
		result.put(JSON_A_MEDIA_URL, pAnswerMedia.getUrl());
		result.put(JSON_A_MEDIA_CREDIT, pAnswerMedia.getCredit());

		return result;
	}

	private static CeStoreJsonObject createCoordsJson(AnswerCoords pAnswerCoords) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		String lat = pAnswerCoords.getLat();
		String lon = pAnswerCoords.getLon();
		String al1 = pAnswerCoords.getAddressLine1();
		String pc = pAnswerCoords.getPostcode();

		result.put(JSON_A_COORDS_ID, pAnswerCoords.getId());

		if (lat != null) {
			result.put(JSON_A_COORDS_LAT, lat);
		}

		if (lon != null) {
			result.put(JSON_A_COORDS_LON, lon);
		}

		if (al1 != null) {
			result.put(JSON_A_COORDS_AL1, al1);
		}

		if (pc != null) {
			result.put(JSON_A_COORDS_PC, pc);
		}

		return result;
	}

	private static CeStoreJsonObject createSourceJson(Source pSource) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		result.put(JSON_S_NAME, pSource.getName());
		result.put(JSON_S_URL, pSource.getUrl());

		return result;
	}

	@Override
	public CeStoreJsonObject handleQuestion() {
		String METHOD_NAME = "handleQuestion";

		QuestionInterpreterHandler qih = new QuestionInterpreterHandler(this.ac, this.debug, this.question.getQuestionText(), System.currentTimeMillis());
		CeStoreJsonObject intResult = qih.handleQuestion();
		CeStoreJsonObject ansResult = null;
		String jsonText = null;

		try {
			jsonText = intResult.serialize(this.ac);
		} catch (IOException e) {
			reportException(e, this.ac, logger, CLASS_NAME, METHOD_NAME);
		}

		if (jsonText != null) {
			QuestionAnswererHandler qah = new QuestionAnswererHandler(this.ac, this.debug, jsonText, System.currentTimeMillis());
			ansResult = qah.handleQuestion();
		}

		return ansResult;
	}

	public CeStoreJsonObject oldHandleQuestion() {
		this.reply = AnswerReply.create(this.question);

		interpretQuestion();

		computeInterpretationConfidence();
		computeAbilityToAnswerConfidence();

		executeQuestion();

		return createJsonResponse();
	}

	private void computeInterpretationConfidence() {
		float missedCount = 0;
		float matchedCount = 0;
		float confidence = 0;

		for (ProcessedWord thisPw : this.allWords) {
			if (!thisPw.isStandardWord()) {
				if (!thisPw.isQuestionWord()) {
					if (!thisPw.isGrounded()) {
						if (!thisPw.isLaterPartOfPartial()) {
							++missedCount;
						} else {
							++matchedCount;
						}
					} else {
						++matchedCount;
					}
				}
			}
		}

		if (missedCount == 0) {
			confidence = 100;
		} else {
			//TODO: Improve the algorithm here
			confidence = (matchedCount / (matchedCount + (missedCount * 3))) * 100;
		}

		this.question.setInterpretationConfidence(new Float(confidence).intValue());
	}

	private void computeAbilityToAnswerConfidence() {
		//TODO: Complete this properly
		this.question.setAbilityToAnswerConfidence(DEFAULT_CONF);
	}

	protected void executeQuestion() {
		boolean needToTerminate = doResponseListProcessing();
		String filterText = "";

		//needToTerminate will be true if the response list processing found words
		//that can't be handled (like negation etc)
		if (!needToTerminate) {
			ModifierHandler mh = new ModifierHandler(this.ac, getConvConfig(), this.allWords);
			ArrayList<CeInstance> newInsts = null;

			CeQuestionExecutor cqe = new CeQuestionExecutor(this.ac, this.debug, this.startTime, this.reply, this.allWords, this.chosenWords, this.question.getInterpretationConfidence(), newInsts);
			cqe.executeQuestion();

			if (ModifierHandler.isFilterQuestion(this.ac, this.allWords)) {
				mh.executeFilterQuestion(this.reply, newInsts);

				filterText = mh.getFilterText();
			} else {
				mh.executeNonSearchQuestion(this.reply);
			}
		}

		//TODO: Find a better way of doing this
		cleanUpAnswer();

		Answer setAnswer = this.reply.getAnswers().get("cons");

		if (setAnswer != null && setAnswer.hasAnswerSet()) {
			AnswerResultSet rsAns = setAnswer.getAnswerSet();
			rsAns.applyTitle(this.resultTitleSingle, this.resultTitlePlural, filterText);
		}
	}

	private void cleanUpAnswer() {
		this.reply.trimAnswersUsing(null);
	}

	private boolean doResponseListProcessing() {
		boolean terminateProcessing = false;

		for (CeInstance rl : this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, CON_RESLIST)) {
			boolean matched = false;

			for (String rw : rl.getValueListFromPropertyNamed(PROP_WORD)) {
				String respWord = rw;

				//TODO: This should be the same code as for the processing of words
				//which removes these characters
				//DSB 21/09/2015 - Uncommented these
				respWord = respWord.replace("'", "");
				//				respWord = respWord.replace("-", "");

				for (ProcessedWord pw : this.allWords) {
					if (pw.getLcWordText().equalsIgnoreCase(respWord)) {
						matched = true;
						break;
					}
				}
			}

			if (matched) {
				CeInstance rc = rl.getSingleInstanceFromPropertyNamed(this.ac, PROP_CAUSES);

				if (rc != null) {
					String ansCode = rc.getSingleValueFromPropertyNamed(PROP_RESPCODE);
					String ansText = rc.getSingleValueFromPropertyNamed(PROP_RESPTEXT);

					Answer ans = Answer.create(rl.getInstanceName(), this.chosenWords, DEFAULT_CONF);
					ans.setAnswerCode(ansCode);
					ans.setChattyAnswerText(ansText);

					this.reply.addAnswer(this.ac, ans);
				} else {
					reportError("no response found for matched word in response list: " + rl.getInstanceName(),
							this.ac);
				}

				String action = rl.getSingleValueFromPropertyNamed(PROP_ACTION);

				if (action.equals(ACTION_TERMINATE)) {
					terminateProcessing = true;
					break;
				}
			}
		}

		return terminateProcessing;
	}

}
