package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

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
import com.ibm.ets.ita.ce.store.hudson.helper.Question;
import com.ibm.ets.ita.ce.store.hudson.helper.Source;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class QuestionExecutionHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

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

		if (this.cc.computeAnswerConfidence()) {
			result = pAnswer.getAnswerConfidence();
		} else {
			result = this.cc.defaultAnswerConfidence();
		}

		return result;
	}

	private int interpretationConfidenceFor(Question pQuestion) {
		int result = -1;

		if (this.cc != null) {
			if (this.cc.computeInterpretationConfidence()) {
				result = pQuestion.getInterpretationConfidence();
			} else {
				result = this.cc.defaultInterpretationConfidence();
			}
		}

		return result;
	}

	private int abilityToAnswerConfidenceFor(Question pQuestion) {
		int result = -1;

		if (this.cc != null) {
			if (this.cc.computeAbilityToAnswerConfidence()) {
				result = pQuestion.getAbilityToAnswerConfidence();
			} else {
				result = this.cc.defaultAbilityToAnswerConfidence();
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

//	private boolean isDatabaseQuestion() {
//		boolean result = false;
//
//		for (ProcessedWord thisPw : this.allWords) {
//			if (isDatabaseBacked(thisPw)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}

//	private boolean isDatabaseBacked(ProcessedWord pWord) {
//		boolean result = false;
//
//		if (pWord.isGroundedOnConcept()) {
//			for (CeConcept thisCon : pWord.listGroundedConcepts()) {
//				CeInstance mmInst = thisCon.retrieveMetaModelInstance(this.ac);
//
//				if (mmInst != null) {
//					if (mmInst.isConceptNamed(this.ac, CON_DBCON)) {
//						result = true;
//						break;
//					}
//				}
//			}
//		}
//
//		return result;
//	}

	protected void executeQuestion() {
		boolean needToTerminate = doResponseListProcessing();
		String filterText = "";

		//needToTerminate will be true if the response list processing found words
		//that can't be handled (like negation etc)
		if (!needToTerminate) {
			ModifierHandler mh = new ModifierHandler(this.ac, this.cc, this.allWords);
			ArrayList<CeInstance> newInsts = null;

//			if (isDatabaseQuestion()) {
//				//This is the old style 'leave the data where it is and query it directly' database question
//				executeDatabaseQuestion();
//			} else {
//				if (isApiQuestion()) {
////					newInsts = executeApiQuestion();
//					System.out.println("executeApiQuestion() is disabled");
//				}

//				if (ModifierHandler.isSearchQuestion(this.ac, this.allWords)) {
//					newInsts = mh.executeSearchQuestion(this.reply);
//
//					String rts = mh.getResultTitleSingle();
//					String rtp = mh.getResultTitlePlural();
//
//					if (rts != null) {
//						this.resultTitleSingle = rts;
//					}
//
//					if (rtp != null) {
//						this.resultTitlePlural = rtp;
//					}
//				}

				CeQuestionExecutor cqe = new CeQuestionExecutor(this.ac, this.debug, this.startTime, this.reply,
						this.allWords, this.chosenWords, this.question.getInterpretationConfidence(), newInsts);
				cqe.executeQuestion();

				if (ModifierHandler.isFilterQuestion(this.ac, this.allWords)) {
					mh.executeFilterQuestion(this.reply, newInsts);

					filterText = mh.getFilterText();
				} else {
					mh.executeNonSearchQuestion(this.reply);
				}
			}
//		}

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

//	private boolean shouldApiBeInvoked(CeInstance pApiConInst, CeInstance pTgtInst, CeConcept pTgtCon) {
//		boolean result = true;
//
//		String outputPropFullName = pApiConInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_OUTPROP);
//
//		if (!outputPropFullName.isEmpty()) {
//			CeProperty outputProp = this.ac.getModelBuilder().getPropertyFullyNamed(outputPropFullName);
//
//			if (outputProp != null) {
//				if (!getExistingInstancesFor(pTgtCon, pApiConInst, pTgtInst).isEmpty()) {
//					result = false;
//				}
//			}
//		}
//
//		return result;
//	}

//	private ArrayList<CeInstance> getExistingInstancesFor(CeConcept pCon, CeInstance pApiConInst, CeInstance pTgtInst) {
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//		String outputPropFullName = pApiConInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_OUTPROP);
//
//		if (!outputPropFullName.isEmpty()) {
//			CeProperty outputProp = this.ac.getModelBuilder().getPropertyFullyNamed(outputPropFullName);
//
//			if (outputProp != null) {
//				for (CeInstance thisInst : pTgtInst.getInstanceListFromPropertyNamed(this.ac,
//						outputProp.getPropertyName())) {
//					if (thisInst.isConcept(pCon)) {
//						result.add(thisInst);
//					}
//				}
//			}
//		}
//
//		return result;
//	}

//	private ArrayList<CeInstance> executeApiQuestion() {
//		ArrayList<String> newInstIds = new ArrayList<String>();
//		ArrayList<CeInstance> newInsts = new ArrayList<CeInstance>();
//		String singleConNames = "";
//		String pluralConNames = "";
//		String instNames = "";
//
//		reportDebug("This is an API question.", this.ac);
//
//		// list of the API concepts found in the question
//		TreeMap<String, APIConcept> APIConcepts = new TreeMap<String, APIConcept>();
//
//		// loop over all words in the question looking for all the API concepts
//		for (ProcessedWord thisPw : this.allWords) {
//
//			// we're only interested in words grounded on a concept
//			if (thisPw.isGroundedOnConcept()) {
//				CeConcept thisApiConcept = null;
//				CeInstance thisApiInstance = null;
//				boolean isApiConcept = false;
//
//				// test if the word is grounded on an API concept
//				for (CeConcept thisGc : thisPw.listGroundedConcepts()) {
//					for (CeInstance mmInst : thisGc.retrieveMetaModelInstances(this.ac, CON_API)) {
//						thisApiInstance = mmInst;
//						thisApiConcept = thisGc;
//
//						String conName = thisGc.getConceptName();
//						String pfName = thisGc.pluralFormName(this.ac);
//
//						if (!singleConNames.contains(conName)) {
//							if (!singleConNames.isEmpty()) {
//								singleConNames += ", ";
//							}
//
//							singleConNames += conName;
//						}
//
//						if (!pluralConNames.contains(pfName)) {
//							if (!pluralConNames.isEmpty()) {
//								pluralConNames += ", ";
//							}
//
//							pluralConNames += pfName;
//						}
//
//						isApiConcept = true;
//						break;
//					}
//				}
//
//				// don't process words that aren't API concepts
//				if (!isApiConcept || thisApiInstance == null) {
//					continue;
//				}
//
//				// do some work to find out the target property
//				String targetProperty = "";
//				CeInstance targetConceptPropertyInstance = thisApiInstance.getSingleInstanceFromPropertyNamed(this.ac,
//						PROP_TARGET_RELATION);
//				if (targetConceptPropertyInstance != null) {
//					targetProperty = targetConceptPropertyInstance.getSingleValueFromPropertyNamed("property name");
//				}
//
//				// create a new API Concept
//				APIConcept thisConcept = new APIConcept(thisApiInstance.getInstanceName(),
//						thisApiInstance.getLatestValueFromPropertyNamed(PROP_TARGET_CONCEPT), targetProperty,
//						thisApiInstance.getLatestValueFromPropertyNamed(PROP_URL_TEMPLATE));
//				thisConcept.setApiInstance(thisApiInstance);
//				thisConcept.setConcept(thisApiConcept);
//				thisConcept.setUsername(thisApiInstance.getLatestValueFromPropertyNamed(PROP_USERNAME));
//				thisConcept.setPassword(thisApiInstance.getLatestValueFromPropertyNamed(PROP_PASSWORD));
//				thisConcept.setRootJsonElementName(thisApiInstance.getLatestValueFromPropertyNamed(PROP_ROOT_JSON));
//				thisConcept.setIdElement(thisApiInstance.getLatestValueFromPropertyNamed(PROP_ID_ELEMENT));
//
//				// test the new API Concept for usability
//				if (thisConcept.hasRequiredValues()) {
//					APIConcepts.put(thisConcept.getConceptName(), thisConcept);
//				} else {
//					StringBuilder message = new StringBuilder();
//					message.append("The api concept " + thisApiInstance.getInstanceName());
//					message.append(" has one or more required values missing (");
//					message.append("\"" + PROP_TARGET_CONCEPT + "\", ");
//					message.append("\"" + PROP_TARGET_RELATION + "\", ");
//					message.append("\"" + PROP_URL_TEMPLATE + "\")");
//					reportError(message.toString(), this.ac);
//				}
//
//			}
//		}
//
//		// we haven't found any API concepts that will actually work so just return here
//		if (APIConcepts.isEmpty()) {
//			reportError("No API concepts were found from the question text", this.ac);
//			return newInsts;
//		}
//
//		// loop over all words in the question again
//		// this time looking for each of the API Concepts' target concept
//		for (ProcessedWord thisPw : this.allWords) {
//
//			// loop over the API concepts found previously
//			for (APIConcept thisConcept : APIConcepts.values()) {
//
//				// we're only interested in words grounded on a concept instance
//				if (thisPw.isGroundedOnInstance()) {
//
//					ArrayList<CeInstance> giList = thisPw.listGroundedInstances();
//					boolean foundInst = false;
//
//					//DSB 20/10/2015 - Ensure that only the correct matched instances are used.
//					//This filters out instances that are partial matches for the whole phrase if longer matches are present.
//					GenericHandler.winnowShorterMatchesFrom(this.ac, giList);
//
//					// loop over grounded instances to find the target concept
//					for (CeInstance thisGi : giList) {
//
//						// test if we've found a target concept (e.g. company)
//						if (thisGi.isConceptNamed(this.ac, thisConcept.getTargetConceptName())) {
//							String thisInstName = thisGi.getFirstInstanceIdentifier(this.ac);
//							foundInst = true;
//
//							if (!instNames.contains(thisInstName)) {
//								if (!instNames.isEmpty()) {
//									instNames += ", ";
//								}
//
//								instNames += thisInstName;
//							}
//
//							if (shouldApiBeInvoked(thisConcept.getApiInstance(), thisGi, thisConcept.getConcept())) {
//								try {
//									// get the CE sentences from the API
//									URL url = parseUrlTemplate(thisGi, thisConcept.getUrlTemplate());
//
//									reportDebug("About to execute API call: " + url.toString(), this.ac);
//
//									String ceSentences = getCeFromUrl(url, thisConcept, thisGi, newInstIds);
//
//									if ((ceSentences != null) && (!ceSentences.isEmpty())) {
//										// commit the CE to the store
//										CeSource jsonApiCeSource = getCeSourceForApiUrl(url);
//										GenericHandler.saveCeText(this.ac, ceSentences, jsonApiCeSource);
//									} else {
//										reportWarning("No CE sentences created from API call: " + url.toString(),
//												this.ac);
//									}
//								} catch (MalformedURLException e) {
//									reportError("Malformed URL for the api concept '" + thisConcept.getConceptName()
//											+ "' from template " + thisConcept.getUrlTemplate(), this.ac);
//								} catch (IOException e) {
//									reportError("An I/O error occured when talking to the API for the api concept '"
//											+ thisConcept.getConceptName() + "'", this.ac);
//								}
//
//								//Now create the list of new instances
//								for (String thisId : newInstIds) {
//									CeInstance newInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, thisId);
//
//									if (newInst != null) {
//										newInsts.add(newInst);
//									}
//								}
//							} else {
//								for (CeInstance thisInst : getExistingInstancesFor(thisConcept.getConcept(),
//										thisConcept.getApiInstance(), thisGi)) {
//									if (!newInsts.contains(thisInst)) {
//										newInsts.add(thisInst);
//									}
//								}
//
//								reportDebug(
//										"Instance '" + thisGi
//												+ "' already has some values for output property so API was not invoked",
//										this.ac);
//							}
//						}
//					}
//
//					if (!foundInst) {
//						//There are no instances so just look in all the local instances
//						for (CeInstance thisInst : this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac,
//								thisConcept.getConcept().getConceptName())) {
//							if (!newInsts.contains(thisInst)) {
//								newInsts.add(thisInst);
//							}
//						}
//					}
//				}
//			}
//		}
//
//		String instBit = null;
//
//		if (!instNames.isEmpty()) {
//			instBit = " for " + instNames;
//		} else {
//			instBit = "";
//		}
//
//		this.resultTitleSingle = "there is 1 " + singleConNames + " listed" + instBit + "%FILTER%";
//		this.resultTitlePlural = "there are %COUNT% " + pluralConNames + " listed" + instBit + "%FILTER%";
//
//		Collections.sort(newInsts);
//
//		return newInsts;
//	}

//	private CeSource getCeSourceForApiUrl(URL url) {
//
//		CeSource thisSource;
//
//		if (ceSources.containsKey(url)) {
//			thisSource = ceSources.get(url);
//		} else {
//			thisSource = CeSource.createNewUrlSource(this.ac, url.toString(), null);
//			ceSources.put(url, thisSource);
//		}
//
//		return thisSource;
//
//	}

//	private URL parseUrlTemplate(CeInstance groundedInstance, String urlTemplate) throws MalformedURLException {
//		Pattern pattern = Pattern.compile("\\{.*?\\}");
//		Matcher matcher = pattern.matcher(urlTemplate);
//
//		StringBuilder builder = new StringBuilder();
//		int i = 0;
//
//		while (matcher.find()) {
//			String foundTag = matcher.group();
//			String foundProperty = foundTag.substring(1, foundTag.length() - 1);
//			String replacement = groundedInstance.getSingleValueFromPropertyNamed(foundProperty);
//			if (replacement.isEmpty()) {
//				return null;
//			}
//
//			builder.append(urlTemplate.substring(i, matcher.start()));
//			builder.append(replacement);
//			i = matcher.end();
//		}
//
//		builder.append(urlTemplate.substring(i, urlTemplate.length()));
//
//		return new URL(builder.toString());
//	}

//	private String getCeFromUrl(URL url, APIConcept thisConcept, CeInstance targetInstance,
//			ArrayList<String> pNewInstIds) throws IOException {
//
//		String instancePrefix = targetInstance.getSingleValueFromPropertyNamed(thisConcept.getTargetConceptProperty());
//
//		// these can be toggled for testing without connecting to the API
//		String jsonResponseString = APIHandler.callApi(url, thisConcept.getUsername(), thisConcept.getPassword());
//		//		String jsonResponseString = "{\"start_index\":0,\"resigned_count\":1,\"etag\":\"35c683789403a7737667f40ba869096d04e225e4\",\"items\":[{\"address\":{\"address_line_1\":\"Red Lion Street\",\"premises\":\"Lion House\",\"postal_code\":\"WC1R 4GB\",\"locality\":\"London\",\"country\":\"United Kingdom\"},\"name\":\"SUMNER, Sarah Louise\",\"officer_role\":\"secretary\",\"links\":{\"officer\":{\"appointments\":\"\\/officers\\/sGVK9-mD3LA0CK-MOLea-3yg7eE\\/appointments\"}},\"appointed_on\":\"1991-05-10\"},{\"occupation\":\"Musician\",\"nationality\":\"British\",\"address\":{\"address_line_1\":\"Red Lion Street\",\"premises\":\"Lion House\",\"postal_code\":\"WC1R 4GB\",\"locality\":\"London\",\"country\":\"United Kingdom\"},\"country_of_residence\":\"United Kingdom\",\"name\":\"SUMNER, Bernard\",\"date_of_birth\":{\"month\":1,\"year\":1956},\"officer_role\":\"director\",\"links\":{\"officer\":{\"appointments\":\"\\/officers\\/-bTlY6FNsy3HbXaHaf-FdXoCrqY\\/appointments\"}},\"appointed_on\":\"1990-05-10\"},{\"address\":{\"address_line_1\":\"6 Lansdowne Mews\",\"address_line_2\":\"Holland Park\",\"postal_code\":\"W11 3BH\",\"locality\":\"London\"},\"name\":\"CHATEL REGISTRARS LIMITED\",\"resigned_on\":\"2002-07-04\",\"officer_role\":\"corporate-secretary\",\"links\":{\"officer\":{\"appointments\":\"\\/officers\\/bR766leL6nCm8vtMID8jk3G-soU\\/appointments\"}},\"appointed_on\":\"1993-05-27\"}],\"active_count\":2,\"links\":{\"self\":\"\\/company\\/02488775\\/appointments\"},\"items_per_page\":35,\"kind\":\"officer-list\",\"total_results\":3}";
//
//		if (jsonResponseString == null || jsonResponseString.equals("null")) {
//			return null;
//		}
//
//		JSONObject jsonResponse = JSONHandler.getJSONObject(jsonResponseString);
//
//		Object jsonRootElement;
//		if (thisConcept.getRootJsonElementName() == null) {
//			jsonRootElement = jsonResponse;
//		} else {
//			jsonRootElement = jsonResponse.get(thisConcept.getRootJsonElementName());
//		}
//
//		StringBuilder ceSentences = new StringBuilder();
//
//		if (jsonRootElement instanceof JSONArray) {
//			JSONArray jsonArray = (JSONArray) jsonRootElement;
//			for (Object jsonArrayObject : jsonArray) {
//				JSONObject jsonObject = (JSONObject) jsonArrayObject;
//				String id = thisConcept.getIdElement();
//				if (id == null) {
//					// we don't insist on an ID element so if it's not specified then generate one here
//					id = new BigInteger(130, random).toString(32);
//				} else {
//					id = (String) jsonObject.get(id);
//				}
//
//				populateCeSentencesFromJson(ceSentences, targetInstance, thisConcept.getConceptName(), instancePrefix,
//						id, jsonObject, pNewInstIds);
//				generateUnmappedJsonWarnings(jsonObject, thisConcept.getConceptName());
//			}
//		} else {
//			JSONObject jsonObject = (JSONObject) jsonRootElement;
//			String id = thisConcept.getIdElement();
//			if (id == null) {
//				// we don't insist on an ID element so if it's not specified then generate one here
//				id = new BigInteger(130, random).toString(32);
//			} else {
//				id = (String) jsonObject.get(id);
//			}
//			populateCeSentencesFromJson(ceSentences, targetInstance, thisConcept.getConceptName(), instancePrefix, id,
//					jsonObject, pNewInstIds);
//			generateUnmappedJsonWarnings(jsonObject, thisConcept.getConceptName());
//		}
//
//		return ceSentences.toString();
//	}

//	private void populateCeSentencesFromJson(StringBuilder ceSentences, CeInstance targetInstance,
//			String targetConceptName, String instancePrefix, String id, JSONObject json,
//			ArrayList<String> pNewInstIds) {
//		ArrayList<String> ceSentence = new ArrayList<>();
//
//		String fullId = instancePrefix + CE_ID_SEP + id;
//		String encodedFullId = GeneralUtilities.encodeForCe(fullId);
//
//		if (!pNewInstIds.contains(encodedFullId)) {
//			pNewInstIds.add(encodedFullId);
//		}
//
//		ceSentence.add("the " + targetConceptName + " '" + encodedFullId + "'");
//
//		// Get the CeConcept instance for the target concept
//		CeConcept targetConcept = this.ac.getModelBuilder().getConceptNamed(this.ac, targetConceptName);
//
//		if (targetConcept == null) {
//			reportError("The concept " + targetConceptName
//					+ " has not been defined in CE and was found while attempting to create a sentence for the "
//					+ fullId, this.ac);
//			return;
//		}
//
//		TreeMap<String, CeProperty> targetConceptProperties = targetConcept.calculateAllProperties();
//
//		// Iterate through all properties
//		for (String targetConceptPropertyKey : targetConceptProperties.keySet()) {
//
//			CeProperty targetConceptPropertyValue = targetConceptProperties.get(targetConceptPropertyKey);
//
//			// get the meta-model instance
//			CeInstance mmInst = targetConceptPropertyValue.getMetaModelInstance(this.ac);
//
//			if (mmInst == null) {
//				continue;
//			}
//
//			// write CE to link this instance to its target concept
//			if (!mmInst.getSingleValueFromPropertyNamed("identifier").isEmpty()) {
//				String identifier = mmInst.getSingleValueFromPropertyNamed("identifier");
//				String identifierValue = targetInstance.getSingleValueFromPropertyNamed(identifier);
//				String encodedIdentifierValue = GeneralUtilities.encodeForCe(identifierValue);
//				ceSentence.add("  " + targetConceptPropertyValue.getPropertyName() + " the "
//						+ targetConceptPropertyValue.getRangeConceptName() + " '" + encodedIdentifierValue + "'");
//				continue;
//			}
//
//			// see whether the meta-model instance has an 'element name’ property value
//			String elemName = mmInst.getSingleValueFromPropertyNamed("element name");
//
//			Object elemObject = JSONHandler.get(json, elemName);
//
//			if (elemName.isEmpty() || elemObject == null) {
//				continue;
//			}
//
//			if (elemObject instanceof JSONObject) {
//				String newFullId = fullId + CE_ID_SEP + targetConceptPropertyValue.getPropertyName();
//				String newEncodedFullId = GeneralUtilities.encodeForCe(newFullId);
//				ceSentence.add("  has the " + targetConceptPropertyValue.getRangeConceptName() + " '" + newEncodedFullId
//						+ "' as " + targetConceptPropertyValue.getPropertyName());
//				populateCeSentencesFromJson(ceSentences, targetInstance,
//						targetConceptPropertyValue.getRangeConceptName(), fullId,
//						targetConceptPropertyValue.getPropertyName(), (JSONObject) elemObject, pNewInstIds);
//				continue;
//			} else if (elemObject instanceof JSONArray) {
//				String newFullId = fullId + CE_ID_SEP + targetConceptPropertyValue.getPropertyName();
//				String newEncodedFullId = GeneralUtilities.encodeForCe(newFullId);
//
//				Iterator<?> it = ((JSONArray) elemObject).iterator();
//				int i = 1;
//				while (it.hasNext()) {
//					ceSentence.add(
//							"  has the " + targetConceptPropertyValue.getRangeConceptName() + " '" + newEncodedFullId
//									+ Integer.toString(i) + "' as " + targetConceptPropertyValue.getPropertyName());
//					populateCeSentencesFromJson(ceSentences, targetInstance,
//							targetConceptPropertyValue.getRangeConceptName(), fullId,
//							targetConceptPropertyValue.getPropertyName() + Integer.toString(i), (JSONObject) it.next(),
//							pNewInstIds);
//					i++;
//				}
//
//				continue;
//			}
//
//			// generate CE (different styles for verbSingular and FunctionalNoun)
//			if (targetConceptPropertyValue.isVerbSingular()) {
//
//				ArrayList<String> elemValues = JSONHandler.searchJsonForString(json, elemName);
//
//				for (String elemValue : elemValues) {
//					String encodedElemValue = GeneralUtilities.encodeForCe(elemValue);
//					if (targetConceptPropertyValue.isDatatypeProperty()) {
//						// If the property is a datatype property then the value is just a string
//						ceSentence.add(
//								"  has '" + encodedElemValue + "' as " + targetConceptPropertyValue.getPropertyName());
//					} else {
//						ceSentence.add("  " + targetConceptPropertyValue.getPropertyName() + " the "
//								+ targetConceptPropertyValue.getRangeConceptName() + " '" + encodedElemValue + "'");
//					}
//				}
//
//			} else if (targetConceptPropertyValue.isFunctionalNoun()) {
//
//				ArrayList<String> elemValues = JSONHandler.searchJsonForString(json, elemName);
//
//				for (String elemValue : elemValues) {
//					String encodedElemValue = GeneralUtilities.encodeForCe(elemValue);
//					if (targetConceptPropertyValue.isDatatypeProperty()) {
//						ceSentence.add(
//								"  has '" + encodedElemValue + "' as " + targetConceptPropertyValue.getPropertyName());
//					} else {
//						ceSentence.add("  has the " + targetConceptPropertyValue.getRangeConceptName() + " '"
//								+ encodedElemValue + "' as " + targetConceptPropertyValue.getPropertyName());
//					}
//				}
//
//			} else {
//
//				// we should probably never get here
//				reportError("Found something that isn't Verb Singular or Functional Noun", this.ac);
//
//			}
//
//		}
//
//		// build the list of strings into a full CE sentence using "and" and a full stop a the end
//		StringBuilder ceString = new StringBuilder();
//		Iterator<String> it = ceSentence.iterator();
//
//		// process the first line separately so as not to get an "and"
//		if (it.hasNext()) {
//			ceString.append(it.next() + NEWLINE);
//		}
//
//		while (it.hasNext()) {
//			ceString.append(it.next());
//			if (it.hasNext()) {
//				ceString.append(" and" + NEWLINE);
//			} else {
//				ceString.append("." + NEWLINE + NEWLINE);
//			}
//		}
//
//		ceSentences.append(ceString);
//
//		return;
//	}

//	private void generateUnmappedJsonWarnings(JSONObject json, String targetConceptName) {
//
//		ArrayList<CeInstance> instances = this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac,
//				"JSON mapped property");
//		ArrayList<String> jsonKeys = new ArrayList<>();
//
//		for (CeInstance instance : instances) {
//
//			String innerConcept = targetConceptName;
//			if (targetConceptName.indexOf('.') >= 0) {
//				innerConcept = targetConceptName.substring(targetConceptName.lastIndexOf('.') + 1,
//						targetConceptName.length());
//				if (targetConceptName.startsWith(innerConcept)) {
//					continue;
//				}
//			}
//
//			if (instance.getInstanceName().substring(0, instance.getInstanceName().indexOf(':')).equals(innerConcept)) {
//				String jsonKey = targetConceptName + "." + instance.getSingleValueFromPropertyNamed("element name");
//				jsonKeys.add(jsonKey);
//			}
//		}
//
//		if (jsonKeys.isEmpty()) {
//			return;
//		}
//
//		for (Object keyObject : json.keySet()) {
//			String key = (String) keyObject;
//			Object val = json.get(key);
//			String fullKey = targetConceptName + "." + key;
//
//			if (val instanceof JSONArray) {
//				Iterator<?> it = ((JSONArray) val).iterator();
//				while (it.hasNext()) {
//					generateUnmappedJsonWarnings((JSONObject) it.next(), fullKey);
//				}
//			} else if (val instanceof JSONObject) {
//				generateUnmappedJsonWarnings((JSONObject) val, fullKey);
//			} else if (!jsonKeys.contains(fullKey)) {
//				reportError("Found JSON key '" + fullKey + "' that has not been mapped in CE as 'JSON mapped property'",
//						this.ac);
//			}
//		}
//	}

//	private boolean isApiQuestion() {
//		boolean result = false;
//
//		for (ProcessedWord thisPw : this.allWords) {
//			if (isApiBacked(thisPw)) {
//				result = true;
//				break;
//			}
//		}
//
//		return result;
//	}

//	private boolean isApiBacked(ProcessedWord pWord) {
//		boolean result = false;
//
//		if (pWord.isGroundedOnConcept()) {
//			for (CeConcept thisCon : pWord.listGroundedConcepts()) {
//				ArrayList<CeInstance> mmInsts = thisCon.retrieveMetaModelInstances(this.ac, CON_API);
//
//				if (!mmInsts.isEmpty()) {
//					result = true;
//					break;
//				}
//			}
//		}
//
//		return result;
//	}

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

//	private void executeDatabaseQuestion() {
//		generateSqlQuery();
//
//		this.chosenWords = this.qi.getChosenWords();
//
//		if (this.qi.hasSqlText()) {
//			DatabaseConnection.executeSqlQuery(this.ac, this.qi.getSqlText(), this.chosenWords,
//					this.qi.getResultColumns(), this.reply);
//			substituteColumnTranslations();
//		} else {
//			handleSomethingWentWrongError(this.ac, this.reply);
//			this.question.setInterpretationConfidence(0);
//			this.question.setAbilityToAnswerConfidence(0);
//		}
//
//		convertFromSetToTextIfNeeded();
//	}

//	private void convertFromSetToTextIfNeeded() {
//		TreeMap<String, Answer> allAnswers = this.reply.getAnswers();
//
//		for (String ansKey : allAnswers.keySet()) {
//			Answer ans = allAnswers.get(ansKey);
//
//			if (ans.hasAnswerSet()) {
//				AnswerResultSet ansSet = ans.getAnswerSet();
//
//				if (ansSet.hasSingleValue()) {
//					ans.replaceAnswerSetWithAnswerText(this.unitName);
//				}
//			}
//		}
//	}

//	private void substituteColumnTranslations() {
//		for (Answer ans : this.reply.getAnswers().values()) {
//			if (ans.hasAnswerSet()) {
//				substituteColumnTranslationsFor(ans);
//			}
//		}
//	}

//	private void substituteColumnTranslationsFor(Answer pAns) {
//		ArrayList<CeInstance> cts = this.qi.getColumnTranslations();
//
//		for (CeInstance dbConInst : cts) {
//			for (CeInstance corrInst : dbConInst.getInstanceListFromPropertyNamed(this.ac, PROP_CORRTO)) {
//				if (corrInst.isConceptNamed(this.ac, CON_DBCOL)) {
//					CeInstance dbColInst = corrInst;
//
//					if (dbColInst != null) {
//						String colName = dbColInst.getSingleValueFromPropertyNamed(PROP_COLNAME);
//
//						substituteUsing(pAns, dbConInst, colName);
//					}
//				}
//			}
//		}
//	}

//	private void substituteUsing(Answer pAns, CeInstance pDbConInst, String pColName) {
//		ServletStateManager ssm = ServletStateManager.getServletStateManager();
//
//		AnswerResultSet ars = pAns.getAnswerSet();
//		CeInstance usedInst = pDbConInst.getSingleInstanceFromPropertyNamed(this.ac, PROP_USES);
//
//		if (usedInst != null) {
//			String propName = usedInst.getSingleValueFromPropertyNamed(PROP_PROPNAME);
//
//			TreeMap<String, String> inverseLookup = ssm.getInverseLookupFor(this.ac, pDbConInst.getInstanceName(),
//					propName);
//
//			for (int i = 0; i < ars.getHeaders().size(); i++) {
//				String thisHdr = ars.getHeaders().get(i);
//
//				if (thisHdr.equals(pColName)) {
//					for (ArrayList<String> thisRow : ars.getRows()) {
//						String tgtVal = thisRow.get(i);
//
//						if (tgtVal != null) {
//							String replacementVal = inverseLookup.get(tgtVal);
//
//							if (replacementVal == null) {
//								replacementVal = "ERROR: " + tgtVal;
//							}
//
//							thisRow.set(i, replacementVal);
//						}
//					}
//				}
//			}
//		}
//	}

//	protected void generateSqlQuery() {
//		this.qi = new QuestionInterpreter(this.ac, this.allWords);
//
//		this.qi.computeSqlForInterpretation();
//	}

}
