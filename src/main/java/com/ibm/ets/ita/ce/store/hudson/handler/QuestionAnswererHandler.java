package com.ibm.ets.ita.ce.store.hudson.handler;

import static com.ibm.ets.ita.ce.store.names.CeNames.ABS_DESC;
import static com.ibm.ets.ita.ce.store.names.CeNames.ABS_MERGE;
import static com.ibm.ets.ita.ce.store.names.CeNames.CONLIST_HUDSON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONFCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_MEDIA;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_SPATIAL;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CORRTO;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CREDIT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DESC;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_LAT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_LON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_MAPSTO;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_URL;
import static com.ibm.ets.ita.ce.store.names.CeNames.MOD_COUNT;
import static com.ibm.ets.ita.ce.store.names.CeNames.MOD_EXPAND;
import static com.ibm.ets.ita.ce.store.names.CeNames.MOD_LINKSFROM;
import static com.ibm.ets.ita.ce.store.names.CeNames.MOD_LINKSTO;
import static com.ibm.ets.ita.ce.store.names.CeNames.MOD_LIST;
import static com.ibm.ets.ita.ce.store.names.CeNames.MOD_LOCATE;
import static com.ibm.ets.ita.ce.store.names.CeNames.MOD_SHOW;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ANSWERS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_QUESTION;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_Q_TEXT;
import static com.ibm.ets.ita.ce.store.names.HudsonCodes.AC_NOVAL;
import static com.ibm.ets.ita.ce.store.names.HudsonCodes.AT_NOVAL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.sortInstancesByProperty;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonParser;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.model.ConceptPhrase;
import com.ibm.ets.ita.ce.store.hudson.model.InstancePhrase;
import com.ibm.ets.ita.ce.store.hudson.model.Interpretation;
import com.ibm.ets.ita.ce.store.hudson.model.PropertyPhrase;
import com.ibm.ets.ita.ce.store.hudson.model.SpecialPhrase;
import com.ibm.ets.ita.ce.store.hudson.model.answer.Answer;
import com.ibm.ets.ita.ce.store.hudson.model.answer.AnswerCode;
import com.ibm.ets.ita.ce.store.hudson.model.answer.AnswerCoords;
import com.ibm.ets.ita.ce.store.hudson.model.answer.AnswerMedia;
import com.ibm.ets.ita.ce.store.hudson.model.answer.AnswerResultSet;
import com.ibm.ets.ita.ce.store.hudson.model.answer.AnswerText;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpMatchedTriple;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpMultiMatch;
import com.ibm.ets.ita.ce.store.hudson.model.special.SpThing;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class QuestionAnswererHandler extends GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private CeStoreJsonObject interpretationJson = null;
	private Interpretation interpretation = null;
	private HashSet<CeInstance> domainInstances = null;
	private HashSet<CeInstance> otherInstances = null;
	private ArrayList<Answer> answers = null;
	private boolean returnInterpretation = false;
	private boolean returnInstances = false;

	public QuestionAnswererHandler(ActionContext pAc, String pIntJson, boolean pRetInt, boolean pRetInsts, long pStartTime) {
		super(pAc, pStartTime);

		this.answers = new ArrayList<Answer>();
		this.returnInterpretation = pRetInt;
		this.returnInstances = pRetInsts;

		CeStoreJsonParser sjp = new CeStoreJsonParser(pAc, pIntJson);
		sjp.parse();

		if (sjp.hasRootJsonObject()) {
			this.interpretationJson = sjp.getRootJsonObject();
		} else {
			reportError("No JSON Object with interpretations found", this.ac);
		}
	}

	public CeStoreJsonObject processInterpretation() {
		CeStoreJsonObject result = null;

		doInterpretationProcessing();
		answerQuestion();

		result = createResult();

		return result;
	}

	private ArrayList<ConceptPhrase> listConceptPhrases() {
		return this.interpretation.getConceptPhrases();
	}

	private ArrayList<InstancePhrase> listInstancePhrases() {
		return this.interpretation.getBestInstancePhrases();
	}

	private ArrayList<PropertyPhrase> listPropertyPhrases() {
		return this.interpretation.getPropertyPhrases();
	}

	private ArrayList<PropertyPhrase> listObjectPropertyPhrases() {
		ArrayList<PropertyPhrase> result = new ArrayList<PropertyPhrase>();

		for (PropertyPhrase thisPp : listPropertyPhrases()) {
			if (thisPp.getFirstProperty().isObjectProperty()) {
				result.add(thisPp);
			}
		}

		return result;
	}

	private ArrayList<SpecialPhrase> listSpecialPhrases() {
		return this.interpretation.getSpecialPhrases();
	}

	private void doInterpretationProcessing() {
		if (this.interpretationJson != null) {
			CeStoreJsonArray jInts = (CeStoreJsonArray) this.interpretationJson.get(this.ac, JSON_INTS);

			if (!jInts.isEmpty()) {
				CeStoreJsonObject jFirstInt = (CeStoreJsonObject) jInts.get(0);

				if (jFirstInt != null) {
					this.interpretation = new Interpretation(this.ac, jFirstInt);
				}

				// TODO: Need to handle additional interpretations when
				// implemented
			}

			filterInstances();
		}
	}

	private void answerQuestion() {
		// TODO: This needs to be improved. For now it just attempts various
		// special matches first
		if (hasMultiMatches()) {
			tryMultiMatchAnswer();
		} else if (hasMatchedTriples()) {
			tryMatchedTripleAnswer();
		} else {
			tryNormalAnswer();
		}
	}

	private boolean hasMatchedTriples() {
		boolean result = false;

		for (SpecialPhrase thisSpecPhrase : listSpecialPhrases()) {
			SpThing thisSp = thisSpecPhrase.getSpecial();

			if (thisSp != null) {
				if (thisSp.isMatchedTriple()) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	private boolean hasMultiMatches() {
		boolean result = false;

		for (SpecialPhrase thisSpecPhrase : listSpecialPhrases()) {
			SpThing thisSp = thisSpecPhrase.getSpecial();

			if (thisSp != null) {
				if (thisSp.isMultiMatch()) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	private void tryMatchedTripleAnswer() {
		for (SpecialPhrase thisSpecPhrase : listSpecialPhrases()) {
			SpThing thisSp = thisSpecPhrase.getSpecial();

			if (thisSp != null) {
				if (thisSp.isMatchedTriple()) {
					matchedTripleAnswerFor((SpMatchedTriple) thisSp);
				}
			}
		}
	}

	private void tryMultiMatchAnswer() {
		TreeMap<String, ArrayList<SpMultiMatch>> tm = new TreeMap<String, ArrayList<SpMultiMatch>>();

		for (SpecialPhrase thisSp : listSpecialPhrases()) {
			if (thisSp.isMultiMatch()) {
				String key = thisSp.getPhraseText();
				ArrayList<SpMultiMatch> values = tm.get(key);

				if (values == null) {
					values = new ArrayList<SpMultiMatch>();
					tm.put(key, values);
				}
				
				if (!values.contains(tm)) {
					SpThing spt = thisSp.getSpecial();
					values.add((SpMultiMatch)spt);
				}
			}
		}

		for (String key : tm.keySet()) {
			ArrayList<SpMultiMatch> smList = tm.get(key);

			createMultiMatchDetailedAnswerFor(smList);
		}
	}

	private void matchedTripleAnswerFor(SpMatchedTriple pMt) {
		if (pMt.isFullTriple()) {
			matchedTripleFullAnswerFor(pMt);
		} else if (pMt.isPartialSubjectTriple()) {
			matchedTripleSubjectAnswerFor(pMt);
		} else {
			matchedTripleObjectAnswerFor(pMt);
		}
	}

	private void matchedTripleFullAnswerFor(SpMatchedTriple pMt) {
		// TODO: Complete this
		createTbcAnswerWith("matchedTripleAnswerFor (full)");
	}

	private void matchedTripleObjectAnswerFor(SpMatchedTriple pMt) {
		textMatchedTripleObjectAnswerFor(pMt);
	}

	private void matchedTripleSubjectAnswerFor(SpMatchedTriple pMt) {
		PropertyPhrase propPhrase = pMt.getPredicate();
		CeProperty mProp = propPhrase.getFirstProperty();

		if (mProp != null) {
			if (mProp.isObjectProperty()) {
				if (mProp.getRangeConcept().equalsOrHasParentNamed(this.ac, CON_MEDIA)) {
					mediaMatchedTripleSubjectAnswerFor(pMt);
				} else if (mProp.getRangeConcept().equalsOrHasParentNamed(this.ac, CON_SPATIAL)) {
					spatialMatchedTripleSubjectAnswerFor(pMt);
				} else {
					textMatchedTripleSubjectAnswerFor(pMt);
				}
			} else {
				//Datatype property
				textMatchedTripleSubjectAnswerFor(pMt);
			}
		}
	}
	
	private void textMatchedTripleSubjectAnswerFor(SpMatchedTriple pMt) {
		PropertyPhrase propPhrase = pMt.getPredicate();
		CeProperty mProp = propPhrase.getFirstProperty();
		ArrayList<String> answerValues = new ArrayList<String>();
		ArrayList<CeInstance> matchedInsts = new ArrayList<CeInstance>();

		for (InstancePhrase thisIp : pMt.getSubjects()) {
			CeInstance mInst = thisIp.getFirstInstance();

			if (mProp.isObjectProperty()) {
				for (CeInstance thisInst : mInst.getInstanceListFromPropertyNamed(this.ac,
						mProp.getPropertyName())) {
					answerValues.add(thisInst.getInstanceName());
					matchedInsts.add(thisInst);
				}
			} else {
				for (String thisVal : mInst.getValueListFromPropertyNamed(mProp.getPropertyName())) {
					answerValues.add(thisVal);
				}
			}
		}

		if (answerValues.isEmpty()) {
			noValueSubjectAnswerFor(pMt);
		} else if (answerValues.size() == 1) {
			singleValueSubjectAnswerFor(pMt, answerValues, matchedInsts);
		} else {
			multipleValueAnswerFor(pMt, answerValues, matchedInsts);
		}
	}
	
	private void textMatchedTripleObjectAnswerFor(SpMatchedTriple pMt) {
		PropertyPhrase propPhrase = pMt.getPredicate();
		CeProperty mProp = propPhrase.getFirstProperty();
		CeConcept domCon = mProp.getDomainConcept();
		ArrayList<String> answerValues = new ArrayList<String>();
		ArrayList<CeInstance> matchedInsts = new ArrayList<CeInstance>();

		if (mProp != null) {
			for (InstancePhrase thisIp : pMt.getObjects()) {
				CeInstance mInst = thisIp.getFirstInstance();

				for (CeInstance subInst : this.ac.getModelBuilder().listAllInstancesForConcept(domCon)) {
					for (CeInstance objInst : subInst.getInstanceListFromPropertyNamed(this.ac,
							mProp.getPropertyName())) {
						if (objInst.equals(mInst)) {
							answerValues.add(subInst.getInstanceName());
							matchedInsts.add(subInst);
						}
					}
				}
			}
		}

		if (answerValues.isEmpty()) {
			noValueObjectAnswerFor(pMt);
		} else if (answerValues.size() == 1) {
			singleValueObjectAnswerFor(pMt, answerValues, matchedInsts);
		} else {
			multipleValueAnswerFor(pMt, answerValues, matchedInsts);
		}
	}

	private void noValueSubjectAnswerFor(SpMatchedTriple pMt) {
		createResultCodeAnswerWith(AC_NOVAL, AT_NOVAL);
	}

	private void noValueObjectAnswerFor(SpMatchedTriple pMt) {
		createResultCodeAnswerWith(AC_NOVAL, AT_NOVAL);
	}

	private void singleValueSubjectAnswerFor(SpMatchedTriple pMt, ArrayList<String> pAnswerValues, ArrayList<CeInstance> pMatchedInsts) {
		StringBuilder sb = new StringBuilder();
		String answerVal = pAnswerValues.get(0);

		for (InstancePhrase subPhrase : pMt.getSubjects()) {
			String phraseText = subPhrase.getPhraseText();

			if (subPhrase.isExactMatch()) {
				phraseText += "'s";
			}

			sb.append(phraseText);
			sb.append(" ");
		}

		sb.append(pMt.getPredicate().getPhraseText());
		sb.append(" is ");
		sb.append(answerVal);

		createStandardAnswerWith(answerVal, sb.toString(), pMatchedInsts);
	}
	
	private void singleValueObjectAnswerFor(SpMatchedTriple pMt, ArrayList<String> pAnswerValues, ArrayList<CeInstance> pMatchedInsts) {
		StringBuilder sb = new StringBuilder();
		String answerVal = pAnswerValues.get(0);

		sb.append(answerVal);

		sb.append(" ");
		sb.append(pMt.getPredicate().getPhraseText());
		sb.append(" ");

		for (InstancePhrase objPhrase : pMt.getObjects()) {
			sb.append(objPhrase.getPhraseText());
		}

		createStandardAnswerWith(answerVal, sb.toString(), pMatchedInsts);
	}

	private void multipleValueAnswerFor(SpMatchedTriple pMt, ArrayList<String> pAnswerValues, ArrayList<CeInstance> pMatchedInsts) {
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();

		titles.add(pMt.getPhraseText());
		
		Collections.sort(pAnswerValues);

		for (String thisVal : pAnswerValues) {
			ArrayList<String> thisRow = new ArrayList<String>();
			thisRow.add(thisVal);
			rows.add(thisRow);
		}

		createResultSetAnswerWith(titles, rows, pMatchedInsts);
	}

	private void mediaMatchedTripleSubjectAnswerFor(SpMatchedTriple pMt) {
		PropertyPhrase propPhrase = pMt.getPredicate();
		CeProperty mProp = propPhrase.getFirstProperty();

		for (InstancePhrase thisIp : pMt.getSubjects()) {
			CeInstance mInst = thisIp.getFirstInstance();

			if (mProp.isObjectProperty()) {
				for (CeInstance thisInst : mInst.getInstanceListFromPropertyNamed(this.ac,
						mProp.getPropertyName())) {
					createMediaAnswerFor(thisInst);
				}
			}
		}
	}

	private void spatialMatchedTripleSubjectAnswerFor(SpMatchedTriple pMt) {
		PropertyPhrase propPhrase = pMt.getPredicate();
		CeProperty mProp = propPhrase.getFirstProperty();

		for (InstancePhrase thisIp : pMt.getSubjects()) {
			CeInstance mInst = thisIp.getFirstInstance();

			if (mProp.isObjectProperty()) {
				for (CeInstance thisInst : mInst.getInstanceListFromPropertyNamed(this.ac,
						mProp.getPropertyName())) {
					createCoordsAnswerFor(thisInst);
				}
			}
		}
	}
	
//	private void multiMatchAnswerFor(SpMultiMatch pSm) {
//		CeInstance matchedInst = pSm.getMatchedInstance();
//
//		if (matchedInst != null) {
//			String desc = matchedInst.getSingleValueFromPropertyNamed(PROP_DESC);
//
//			if ((desc != null) && (!desc.isEmpty())) {
//				createStandardAnswerWith(desc, null, matchedInst);
//			} else {
//				ArrayList<SpMultiMatch> smList = new ArrayList<SpMultiMatch>();
//				smList.add(pSm);
//				createMultiMatchDetailedAnswerFor(smList);
//			}
//		} else {
//			createTbcAnswerWith("multiMatchAnswerFor (no matched instance)", matchedInst);
//		}
//	}

	private void createMultiMatchDetailedAnswerFor(ArrayList<SpMultiMatch> pSms) {
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		ArrayList<CeInstance> instList = new ArrayList<CeInstance>();

		titles.add("matched");
		titles.add("property 1");
		titles.add("instance 1");
		titles.add("property 2");
		titles.add("instance 2");
		
		for (SpMultiMatch thisSm : pSms) {
			ArrayList<String> thisRow = new ArrayList<String>();
			CeInstance matchedInst = thisSm.getMatchedInstance();

			if (matchedInst != null) {
				thisRow.add(matchedInst.getInstanceName());

				if (!instList.contains(matchedInst)) {
					instList.add(matchedInst);
				}

				thisRow.add(thisSm.getPropertyName1());
				
				for (CeInstance thisInst : thisSm.getInstPhrase1().getInstances()) {
					thisRow.add(thisInst.getInstanceName());

					if (!instList.contains(thisInst)) {
						instList.add(thisInst);
					}				
				}

				thisRow.add(thisSm.getPropertyName2());

				for (CeInstance thisInst : thisSm.getInstPhrase2().getInstances()) {
					thisRow.add(thisInst.getInstanceName());

					if (!instList.contains(thisInst)) {
						instList.add(thisInst);
					}				
				}
			}

			rows.add(thisRow);
		}

		createResultSetAnswerWith(titles, rows, instList);
	}

	private void tryNormalAnswer() {
		if (listConceptPhrases().isEmpty()) {
			if (listPropertyPhrases().isEmpty()) {
				if (this.domainInstances.isEmpty()) {
					// no concepts, no properties, no domain instances
					handleEverythingEmpty();
				} else {
					// no concepts, no properties, some domain instances
					handleJustInstances();
				}
			} else {
				if (this.domainInstances.isEmpty()) {
					// no concepts, some properties, no domain instances
					handleJustProperties();
				} else {
					// no concepts, some properties, some domain instances
					handleSomePropertiesAndInstances();
				}
			}
		} else {
			if (listPropertyPhrases().isEmpty()) {
				if (this.domainInstances.isEmpty()) {
					// some concepts, no properties, no domain instances
					handleJustConcepts();
				} else {
					// some concepts, no properties, some domain instances
					handleSomeConceptsAndInstances();
				}
			} else {
				if (this.domainInstances.isEmpty()) {
					if (!listObjectPropertyPhrases().isEmpty()) {
						//TODO: This may be an over-simplification
						//If the concepts and properties were related they would be matched triples
						//Since they are not and we can just treat as concepts
						handleJustConcepts();
					} else {
						// some concepts, some properties, no domain instances
						handleSomeConceptsAndProperties();
					}
				} else {
					// some concepts, some properties, some domain instances
					handleSomeConceptsPropertiesAndInstances();
				}
			}
		}
	}

	private void createStandardAnswerWith(String pAnswerText, String pChattyText) {
		Answer ans = new AnswerText(pAnswerText, pChattyText, computeAnswerConfidence());

		this.answers.add(ans);
	}

	private void createStandardAnswerWith(String pAnswerText, String pChattyText, CeInstance pInst) {
		Answer ans = new AnswerText(pAnswerText, pChattyText, computeAnswerConfidence());
		ans.addInstance(pInst);

		this.answers.add(ans);
	}

	private void createStandardAnswerWith(String pAnswerText, String pChattyText, ArrayList<CeInstance> pInstList) {
		Answer ans = new AnswerText(pAnswerText, pChattyText, computeAnswerConfidence(), pInstList);

		this.answers.add(ans);
	}

	private void createResultSetAnswerWith(ArrayList<String> pTitles, ArrayList<ArrayList<String>> pRows, ArrayList<CeInstance> pInsts) {
		Answer ans = new AnswerResultSet(pTitles, pRows, pInsts, computeAnswerConfidence());

		this.answers.add(ans);
	}

	private void createResultCodeAnswerWith(String pCode, String pText) {
		Answer ans = new AnswerCode(pCode, pText, computeAnswerConfidence());

		this.answers.add(ans);
	}

	private void createTbcAnswerWith(String pAnswerText) {
		Answer ans = new AnswerText("TBC - " + pAnswerText, null, 0);

		this.answers.add(ans);
	}

	private void createTbcAnswerWith(String pAnswerText, CeInstance pInst) {
		Answer ans = new AnswerText("TBC - " + pAnswerText, null, 0);
		ans.addInstance(pInst);

		this.answers.add(ans);
	}

	private void createCoordsAnswerFor(CeInstance pInst) {
		String name = pInst.getInstanceName();
		String lat = pInst.getSingleValueFromPropertyNamed(PROP_LAT);
		String lon = pInst.getSingleValueFromPropertyNamed(PROP_LON);

		if ((!lat.isEmpty()) && (!lon.isEmpty())) {
			Answer ans = new AnswerCoords(name, lat, lon, computeAnswerConfidence());
			ans.addInstance(pInst);

			this.answers.add(ans);
		}
	}

	private void createMediaAnswerFor(CeInstance pInst) {
		String name = pInst.getInstanceName();
		String url = pInst.getSingleValueFromPropertyNamed(PROP_URL);
		String credit = pInst.getSingleValueFromPropertyNamed(PROP_CREDIT);

		if (!url.isEmpty()) {
			Answer ans = new AnswerMedia(name, url, credit, computeAnswerConfidence());
			ans.addInstance(pInst);

			this.answers.add(ans);
		}
	}

	private int computeAnswerConfidence() {
		//TODO: Improve this
		return this.interpretation.getConfidence();
	}

	private void handleJustInstances() {
		for (CeInstance thisInst : this.domainInstances) {
			answerInstanceQuestionFor(thisInst);
		}
	}

	private void handleEverythingEmpty() {
		// TODO: Complete this
		createTbcAnswerWith("handleEverythingEmpty");
	}

	private void handleSomePropertiesAndInstances() {
		for (CeInstance thisInst : this.domainInstances) {
			handlePropertiesFor(thisInst);
		}
	}

	private void handlePropertiesFor(CeInstance pInst) {
		StringBuilder sb = new StringBuilder();
		HashSet<CeInstance> mediaInsts = null;
		HashSet<CeInstance> spatialInsts = null;
		String instName = null;
		String propName = null;
		String propVals = null;

		instName = pInst.getInstanceName();

		for (PropertyPhrase thisPp : listPropertyPhrases()) {
			for (CeProperty thisProp : thisPp.getProperties()) {
				for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
					CeProperty innerProp = thisPi.getRelatedProperty();

					if (!innerProp.isStatedProperty()) {
						innerProp = innerProp.getStatedSourceProperty();
					}

					if (innerProp.equals(thisProp)) {
						if (innerProp.getRangeConcept().equalsOrHasParentNamed(this.ac, CON_MEDIA)) {
							mediaInsts = thisPi.getValueInstanceList(this.ac);
						} else if (innerProp.getRangeConcept().equalsOrHasParentNamed(this.ac, CON_SPATIAL)) {
							spatialInsts = thisPi.getValueInstanceList(this.ac);
						} else {
							propName = thisPi.getPropertyName();

							for (String thisVal : thisPi.getValueList()) {
								if (propVals == null) {
									propVals = "";
								} else {
									propVals += ", ";
								}

								propVals += thisVal;
							}
						}
					}
				}

				if (thisProp.isFunctionalNoun()) {
					sb.append(instName + " has " + propVals + " as " + propName);
				} else {
					sb.append(instName + " " + propName + " " + propVals);
				}

				if (mediaInsts != null) {
					for (CeInstance relInst : mediaInsts) {
						createMediaAnswerFor(relInst);
					}
				} else if (spatialInsts != null) {
					for (CeInstance relInst : spatialInsts) {
						createCoordsAnswerFor(relInst);
					}
				} else {
					//Standard answer
					createStandardAnswerWith(sb.toString(), null, pInst);
				}
			}
		}
	}

	private void handleJustProperties() {
		// TODO: Complete this
		createTbcAnswerWith("handleJustProperties");
	}

	private void handleJustConcepts() {
		if (isList()) {
			answerListFor(listConceptPhrases());
		} else {
			for (ConceptPhrase thisCp : listConceptPhrases()) {
				for (CeConcept thisCon : thisCp.getConcepts()) {
					answerConceptQuestionFor(thisCon);
				}
			}
		}
	}

	private void handleSomeConceptsAndInstances() {
		if (isLocate()) {
			ArrayList<CeInstance> instList = filterInstancesByConcepts();

			for (CeInstance thisInst : instList) {
				answerLocateFor(thisInst);
			}
		} else {
			// TODO: Complete this
			createTbcAnswerWith("handleSomeConceptsAndInstances");
		}
	}

	private ArrayList<CeInstance> filterInstancesByConcepts() {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		for (CeInstance thisInst : this.domainInstances) {
			for (ConceptPhrase thisCp : listConceptPhrases()) {
				for (CeConcept thisCon : thisCp.getConcepts()) {
					if (thisInst.isConcept(thisCon)) {
						result.add(thisInst);
					}
				}
			}
		}

		return result;
	}

	private void handleSomeConceptsAndProperties() {
		if (isList()) {
			answerListFor(listConceptPhrases(), listPropertyPhrases());
		} else {
			for (ConceptPhrase thisCp : listConceptPhrases()) {
				for (CeConcept thisCon : thisCp.getConcepts()) {
					handleSomeConceptsAndProperties(thisCon);
				}
			}
		}
	}

	private void handleSomeConceptsAndProperties(CeConcept pCon) {
		// TODO: Complete this
		createTbcAnswerWith("handleSomeConceptsAndProperties");
	}

	private void handleSomeConceptsPropertiesAndInstances() {
		// TODO: Complete this
		createTbcAnswerWith("handleSomeConceptsPropertiesAndInstances");
	}

	private void answerInstanceQuestionFor(CeInstance pInst) {
		if (isExpand()) {
			answerExpandFor(pInst);
		} else if (isLinksFrom()) {
			answerLinksFromFor(pInst);
		} else if (isLinksTo()) {
			answerLinksToFor(pInst);
		} else if (isLocate()) {
			answerLocateFor(pInst);
		} else if (isCount()) {
			answerCountFor(pInst);
		} else if (isList()) {
			answerListFor(pInst);
		} else if (isShow()) {
			answerShowFor(pInst);
		} else {
			createStandardAnswerWith(answerSummaryFor(pInst), null, pInst);
		}
	}

	private void answerConceptQuestionFor(CeConcept pCon) {
		if (isExpand()) {
			// TODO: Consider other properties like properties, parent concepts,
			// conceptual models etc
			answerExpandFor(pCon.retrieveMetaModelInstance(this.ac));
		} else if (isLinksFrom()) {
			answerLinksFromFor(pCon);
		} else if (isLinksTo()) {
			answerLinksToFor(pCon);
		} else if (isLocate()) {
			answerLocateFor(pCon);
		} else if (isCount()) {
			answerCountFor(pCon);
		} else if (isShow()) {
			answerShowFor(pCon);
		} else {
			createStandardAnswerWith(answerSummaryFor(pCon), null);
		}
	}

	private void answerExpandFor(CeInstance pInst) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(answerSummaryFor(pInst));

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			sb.append(NL);
			sb.append("  ");
			sb.append(thisPi.getPropertyName());
			sb.append(" -> ");

			boolean firstTime = true;

			for (String thisVal : thisPi.getValueList()) {
				if (!firstTime) {
					sb.append(", ");
				}

				sb.append(thisVal);
				firstTime = false;
			}
		}

		createStandardAnswerWith(sb.toString(), null, pInst);
	}

	private void answerLinksFromFor(CeInstance pInst) {
		// TODO: Complete this
		createTbcAnswerWith("answerLinksFromForInstance: " + pInst.getInstanceName(), pInst);
	}

	private void answerLinksFromFor(CeConcept pCon) {
		// TODO: Complete this
		createTbcAnswerWith("answerLinksFromForConcept: " + pCon.getConceptName());
	}

	private void answerLinksToFor(CeInstance pInst) {
		// TODO: Complete this
		createTbcAnswerWith("answerLinksToForInstance: " + pInst.getInstanceName(), pInst);
	}

	private void answerLinksToFor(CeConcept pCon) {
		// TODO: Complete this
		createTbcAnswerWith("answerLinksToForConcept: " + pCon.getConceptName());
	}

	private void answerLocateFor(CeInstance pInst) {
		// TODO: Also check for related instances that are spatial things
		if (pInst.isConceptNamed(this.ac, CON_SPATIAL)) {
			createCoordsAnswerFor(pInst);
		}
	}

	private void answerLocateFor(CeConcept pCon) {
		for (CeInstance thisInst : this.ac.getModelBuilder().getAllInstancesForConceptNamed(this.ac, pCon.getConceptName())) {
			answerLocateFor(thisInst);
		}
	}

	private void answerCountFor(CeInstance pInst) {
		// TODO: Complete this
		createTbcAnswerWith("answerCountForInstance: " + pInst.getInstanceName(), pInst);
	}

	private void answerCountFor(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();
		String pluralName = pCon.pluralFormName(this.ac);
		int count = this.ac.getModelBuilder().countAllInstancesForConcept(pCon);

		if (count == 0) {
			sb.append("there are no ");
			sb.append(pluralName);
		} else if (count == 1) {
			sb.append("there is 1 ");
			sb.append(pCon.getConceptName());
		} else {
			sb.append("there are ");
			sb.append(count);
			sb.append(" ");
			sb.append(pluralName);
		}

		sb.append(" defined");

		createStandardAnswerWith(new Long(count).toString(), sb.toString());
	}

	private void answerListFor(CeInstance pInst) {
		// TODO: Complete this
		createTbcAnswerWith("answerListForInstance: " + pInst.getInstanceName(), pInst);
	}

	private void answerListFor(ArrayList<ConceptPhrase> pConList) {
		if (isMerge()) {
			mergedAnswerListFor(pConList);
		} else {
			for (ConceptPhrase thisCp : pConList) {
				for (CeConcept thisCon : thisCp.getConcepts()) {
					answerListFor(thisCon);
				}
			}
		}
	}

	private void answerListFor(ArrayList<ConceptPhrase> pConList, ArrayList<PropertyPhrase> pPropList) {
		for (ConceptPhrase thisCp : pConList) {
			for (CeConcept thisCon : thisCp.getConcepts()) {
				answerListFor(thisCon, pPropList);
			}
		}
	}

	private void answerListFor(CeConcept pCon) {
		ArrayList<CeInstance> allInstList = this.ac.getModelBuilder().listAllInstancesForConcept(pCon);
		ArrayList<CeInstance> mediaList = new ArrayList<CeInstance>();
		ArrayList<CeInstance> spatialList = new ArrayList<CeInstance>();
		ArrayList<CeInstance> normalList = new ArrayList<CeInstance>();

		for (CeInstance thisInst : allInstList) {
			if (thisInst.isConceptNamed(this.ac, CON_SPATIAL)) {
				spatialList.add(thisInst);
			} else if (thisInst.isConceptNamed(this.ac, CON_MEDIA)) {
				mediaList.add(thisInst);
			} else {
				normalList.add(thisInst);
			}
		}

		if (isSortAscending()) {
			Collections.sort(mediaList);
			Collections.sort(spatialList);
			Collections.sort(normalList);
		} else {
			Collections.reverse(mediaList);
			Collections.reverse(spatialList);
			Collections.reverse(normalList);
		}

		for (CeInstance medInst : mediaList) {
			createMediaAnswerFor(medInst);
		}

		for (CeInstance spInst : spatialList) {
			createCoordsAnswerFor(spInst);
		}

		if (!normalList.isEmpty()) {
			ArrayList<String> titles = new ArrayList<String>();
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
			titles.add(pCon.getConceptName());

			for (CeInstance normInst : normalList) {
				ArrayList<String> thisRow = new ArrayList<String>();

				thisRow.add(normInst.getInstanceName());
				rows.add(thisRow);
			}

			createResultSetAnswerWith(titles, rows, normalList);
		}
	}

	private void mergedAnswerListFor(ArrayList<ConceptPhrase> pConPhrases) {
		ArrayList<CeInstance> instList = new ArrayList<CeInstance>();
		String conNames = "";

		for (ConceptPhrase thisCp : pConPhrases) {
			for (CeConcept thisCon : thisCp.getConcepts()) {
				if (!conNames.isEmpty()) {
					conNames += " ";
				}
				conNames += thisCon.pluralFormName(this.ac);

				for (CeInstance thisInst : this.ac.getModelBuilder().listAllInstancesForConcept(thisCon)) {
					if (!instList.contains(thisInst)) {
						instList.add(thisInst);
					}
				}
			}
		}

		if (isSortAscending()) {
			Collections.sort(instList);
		} else {
			Collections.reverse(instList);
		}

		if (!instList.isEmpty()) {
			ArrayList<String> titles = new ArrayList<String>();
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
			titles.add(conNames);

			for (CeInstance thisInst : instList) {
				ArrayList<String> thisRow = new ArrayList<String>();

				thisRow.add(thisInst.getInstanceName());
				rows.add(thisRow);
			}

			createResultSetAnswerWith(titles, rows, instList);
		} else {
			//TODO: Handle empty list here
		}
	}

	private void answerListFor(CeConcept pCon, CeProperty pProp) {
		ArrayList<CeInstance> conList = this.ac.getModelBuilder().listAllInstancesForConcept(pCon);

		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();

		titles.add(pCon.getConceptName());
		titles.add(pProp.getPropertyName());
		
		sortInstancesByProperty(conList, pProp, isSortAscending()); 

		for (CeInstance thisInst : conList) {
			CePropertyInstance thisPi = thisInst.getPropertyInstanceForProperty(pProp);
			String propVal = null;
		
			if (thisPi != null) {
				propVal = thisPi.getSingleOrFirstValue();
			}
			
			ArrayList<String> thisRow = new ArrayList<String>();
			thisRow.add(thisInst.getInstanceName());
			thisRow.add(propVal);

			rows.add(thisRow);
		}

		createResultSetAnswerWith(titles, rows, conList);
	}

	private boolean isSortAscending() {
		boolean result = true;
		
		for (CeInstance thisInst : this.otherInstances) {
			String mtVal = thisInst.getSingleValueFromPropertyNamed(PROP_MAPSTO);
			
			if (mtVal.equals(ABS_DESC)) {
				result = false;
				break;
			}
		}

		return result;
	}

	private boolean isMerge() {
		boolean result = false;
		
		for (CeInstance thisInst : this.otherInstances) {
			String mtVal = thisInst.getSingleValueFromPropertyNamed(PROP_MAPSTO);
			
			if (mtVal.equals(ABS_MERGE)) {
				result = true;
				break;
			}
		}

		return result;
	}

	private void answerListFor(CeConcept pCon, ArrayList<PropertyPhrase> pProps) {
		for (PropertyPhrase thisPhrase : pProps) {
			CeProperty thisProp = thisPhrase.getFirstProperty();
			answerListFor(pCon, thisProp);
		}
	}

	private void answerShowFor(CeInstance pInst) {
		// TODO: Complete this
		createTbcAnswerWith("answerShowForInstance: " + pInst.getInstanceName(), pInst);
	}

	private void answerShowFor(CeConcept pCon) {
		// TODO: Complete this
		createTbcAnswerWith("answerShowForConcept: " + pCon.getConceptName());
	}

	private String answerSummaryFor(CeConcept pCon) {
		return pCon.getConceptName() + textForConcepts(pCon.retrieveMetaModelInstance(this.ac));
	}

	private String answerSummaryFor(CeInstance pInst) {
		String result = null;
		String descText = pInst.getSingleValueFromPropertyNamed(PROP_DESC);

		if ((descText != null) && (!descText.isEmpty())) {
			result = descText;
		} else {
			result = pInst.getInstanceName() + textForConcepts(pInst);
		}

		return result;
	}

	private String textForConcepts(CeInstance pInst) {
		String result = "";

		for (CeConcept thisCon : pInst.getAllLeafConcepts()) {
			if (isDomainConcept(thisCon)) {
				if (result.isEmpty()) {
					result += " is ";
					result += thisCon.conceptQualifier();
					result += " ";
				} else {
					result += ", ";
				}

				result += thisCon.getConceptName();
			}
		}

		return result;
	}

	private boolean isDomainConcept(CeConcept pCon) {
		return !pCon.equalsOrHasParentNamed(this.ac, CON_CONFCON);
	}

	private boolean isExpand() {
		return isModifierNamed(MOD_EXPAND);
	}

	private boolean isLinksFrom() {
		return isModifierNamed(MOD_LINKSFROM);
	}

	private boolean isLinksTo() {
		return isModifierNamed(MOD_LINKSTO);
	}

	private boolean isLocate() {
		return isModifierNamed(MOD_LOCATE);
	}

	private boolean isCount() {
		return isModifierNamed(MOD_COUNT);
	}

	private boolean isList() {
		return isModifierNamed(MOD_LIST);
	}

	private boolean isShow() {
		return isModifierNamed(MOD_SHOW);
	}

	private boolean isModifierNamed(String pTgtName) {
		boolean result = false;

		for (CeInstance thisInst : this.otherInstances) {
			CePropertyInstance thisPi = thisInst.getPropertyInstanceNamed(PROP_CORRTO);

			if (thisPi != null) {
				for (CeInstance relInst : thisPi.getValueInstanceList(this.ac)) {
					if (relInst.getInstanceName().equals(pTgtName)) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}

	private void filterInstances() {
		this.domainInstances = new HashSet<CeInstance>();
		this.otherInstances = new HashSet<CeInstance>();

		for (InstancePhrase thisIp : listInstancePhrases()) {
			for (CeInstance thisInst : thisIp.getInstances()) {
				boolean alreadyProcessed = false;

				if (!thisInst.isOnlyConceptNamed(this.ac, CON_CONFCON)) {
					if (thisInst.isConceptNamed(this.ac, CONLIST_HUDSON)) {
						this.otherInstances.add(thisInst);
						alreadyProcessed = true;
					}

					if (!alreadyProcessed) {
						this.domainInstances.add(thisInst);
					}
				}
			}
		}
	}

	protected CeStoreJsonObject createResult() {
		long st = System.currentTimeMillis();
		CeStoreJsonObject result = null;

		result = createJsonResponse();
		reportDebug("createResult=" + new Long(System.currentTimeMillis() - st).toString(), this.ac);

		return result;
	}

	protected CeStoreJsonObject createJsonResponse() {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonArray jAnswers = new CeStoreJsonArray();
		CeStoreJsonObject jQuestion = new CeStoreJsonObject();

		CeStoreJsonObject jQues = (CeStoreJsonObject) this.interpretationJson.get(this.ac, JSON_QUESTION);

		if (jQues != null) {
			String qText = jQues.getString(JSON_Q_TEXT);

			if ((qText != null) && (!qText.isEmpty())) {
				jQuestion.put(JSON_Q_TEXT, qText);
			}
		}

		jResult.put(JSON_QUESTION, jQuestion);

		if (this.returnInterpretation) {
			jResult.put(JSON_INT, this.interpretationJson);
		}

		for (Answer thisAns : this.answers) {
			CeStoreJsonObject jAnswer = thisAns.toJson(this.ac, this.returnInstances);
			jAnswers.add(jAnswer);			
		}
		
		jResult.put(JSON_ANSWERS, jAnswers);

		return jResult;
	}

}
