package com.ibm.ets.ita.ce.store.hudson.handler;

import static com.ibm.ets.ita.ce.store.names.CeNames.ABS_ASC;
import static com.ibm.ets.ita.ce.store.names.CeNames.CONLIST_HUDSON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONFCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CORRTO;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_MAPSTO;
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
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_SPATIAL;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DESC;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.sortInstancesByProperty;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.HashSet;

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
//	private String answerText = null;
	private Answer answer = null;
//	private ArrayList<CeInstance> answerInstances = new ArrayList<CeInstance>();
	private boolean returnInterpretation = false;
	private boolean returnInstances = false;

	public QuestionAnswererHandler(ActionContext pAc, String pIntJson, boolean pRetInt, boolean pRetInsts, long pStartTime) {
		super(pAc, pStartTime);

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
		for (SpecialPhrase thisSpecPhrase : listSpecialPhrases()) {
			SpThing thisSp = thisSpecPhrase.getSpecial();

			if (thisSp != null) {
				if (thisSp.isMultiMatch()) {
					multiMatchAnswerFor((SpMultiMatch) thisSp);
				}
			}
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
		PropertyPhrase propPhrase = pMt.getPredicate();
		CeProperty mProp = propPhrase.getFirstProperty();
		CeConcept domCon = mProp.getDomainConcept();
		ArrayList<String> answerValues = new ArrayList<String>();

		if (mProp != null) {
			for (InstancePhrase thisIp : pMt.getObjects()) {
				CeInstance mInst = thisIp.getFirstInstance();

				for (CeInstance subInst : this.ac.getModelBuilder().listAllInstancesForConcept(domCon)) {
					for (CeInstance objInst : subInst.getInstanceListFromPropertyNamed(this.ac,
							mProp.getPropertyName())) {
						if (objInst.equals(mInst)) {
							answerValues.add(subInst.getInstanceName());
							this.answer.addInstance(subInst);
						}
					}
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();

		String sepVal = "";
		for (String thisVal : answerValues) {
			sb.append(sepVal);
			sb.append(thisVal);
			sepVal = ", ";
		}

		sb.append(" ");
		sb.append(pMt.getPredicate().getPhraseText());
		sb.append(" ");

		for (InstancePhrase objPhrase : pMt.getObjects()) {
			sb.append(objPhrase.getPhraseText());
		}

		createStandardAnswerWith(sb.toString(), null);
	}

	private void matchedTripleSubjectAnswerFor(SpMatchedTriple pMt) {
		PropertyPhrase propPhrase = pMt.getPredicate();
		CeProperty mProp = propPhrase.getFirstProperty();
		ArrayList<String> answerValues = new ArrayList<String>();

		if (mProp != null) {
			for (InstancePhrase thisIp : pMt.getSubjects()) {
				CeInstance mInst = thisIp.getFirstInstance();

				if (mProp.isObjectProperty()) {
					for (CeInstance thisInst : mInst.getInstanceListFromPropertyNamed(this.ac,
							mProp.getPropertyName())) {
						answerValues.add(thisInst.getInstanceName());
						this.answer.addInstance(thisInst);
					}
				} else {
					for (String thisVal : mInst.getValueListFromPropertyNamed(mProp.getPropertyName())) {
						answerValues.add(thisVal);
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		
		for (InstancePhrase subPhrase : pMt.getSubjects()) {
			sb.append(subPhrase.getPhraseText());
			sb.append("'s ");
		}

		sb.append(pMt.getPredicate().getPhraseText());
		sb.append(" is ");

		String sepVal = "";
		for (String thisVal : answerValues) {
			sb.append(sepVal);
			sb.append(thisVal);
			sepVal = ", ";
		}

		createStandardAnswerWith(sb.toString(), null);
	}

	private void multiMatchAnswerFor(SpMultiMatch pMm) {
		CeInstance matchedInst = pMm.getMatchedInstance();

		this.answer.addInstance(matchedInst);

		if (matchedInst != null) {
			String desc = matchedInst.getSingleValueFromPropertyNamed(PROP_DESC);

			if ((desc != null) && (!desc.isEmpty())) {
				createStandardAnswerWith(desc, null);
			} else {
				createTbcAnswerWith("multiMatchAnswerFor (no description)");
			}
		} else {
			createTbcAnswerWith("multiMatchAnswerFor (no matched instance)");
		}
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
					// some concepts, some properties, no domain instances
					handleSomeConceptsAndProperties();
				} else {
					// some concepts, some properties, some domain instances
					handleSomeConceptsPropertiesAndInstances();
				}
			}
		}
	}

	private void createStandardAnswerWith(String pAnswerText, String pChattyText) {
		this.answer = new AnswerText(pAnswerText, pChattyText, computeAnswerConfidence());
	}

//	private void createStandardAnswerWith(String pAnswerText, String pChattyText, ArrayList<CeInstance> pInstList) {
//		this.answer = new AnswerText(pAnswerText, pChattyText, computeAnswerConfidence(), pInstList);
//	}

	private void createResultSetAnswerWith(ArrayList<String> pTitles, ArrayList<ArrayList<String>> pRows) {
		this.answer = new AnswerResultSet(pTitles, pRows, computeAnswerConfidence());
	}

	private void createTbcAnswerWith(String pAnswerText) {
		this.answer = new AnswerText("TBC - " + pAnswerText, null, 0);
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
		String instName = null;
		String propName = null;
		String propVals = null;

		this.answer.addInstance(pInst);

		instName = pInst.getInstanceName();

		for (PropertyPhrase thisPp : listPropertyPhrases()) {
			for (CeProperty thisProp : thisPp.getProperties()) {
				for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
					CeProperty innerProp = thisPi.getRelatedProperty();

					if (!innerProp.isStatedProperty()) {
						innerProp = innerProp.getStatedSourceProperty();
					}

					if (innerProp.equals(thisProp)) {
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

				if (thisProp.isFunctionalNoun()) {
					sb.append(instName + " has " + propVals + " as " + propName);
				} else {
					sb.append(instName + " " + propName + " " + propVals);
				}

				createStandardAnswerWith(sb.toString(), null);
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
			StringBuilder sb = new StringBuilder();

			for (CeInstance thisInst : instList) {
				sb.append(answerLocateFor(thisInst));
				this.answer.addInstance(thisInst);
			}

			createStandardAnswerWith(sb.toString(), null);
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
					// TODO: Complete this
					createTbcAnswerWith("handleSomeConceptsAndProperties");
				}
			}
		}
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
			createStandardAnswerWith(answerLocateFor(pInst), null);
		} else if (isCount()) {
			answerCountFor(pInst);
		} else if (isList()) {
			answerListFor(pInst);
		} else if (isShow()) {
			answerShowFor(pInst);
		} else {
			createStandardAnswerWith(answerSummaryFor(pInst), null);
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

		this.answer.addInstance(pInst);

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

		createStandardAnswerWith(sb.toString(), null);
	}

	private void answerLinksFromFor(CeInstance pInst) {
		// TODO: Complete this
		this.answer.addInstance(pInst);
		createTbcAnswerWith("answerLinksFromForInstance: " + pInst.getInstanceName());
	}

	private void answerLinksFromFor(CeConcept pCon) {
		// TODO: Complete this
		createTbcAnswerWith("answerLinksFromForConcept: " + pCon.getConceptName());
	}

	private void answerLinksToFor(CeInstance pInst) {
		// TODO: Complete this
		this.answer.addInstance(pInst);
		createTbcAnswerWith("answerLinksToForInstance: " + pInst.getInstanceName());
	}

	private void answerLinksToFor(CeConcept pCon) {
		// TODO: Complete this
		createTbcAnswerWith("answerLinksToForConcept: " + pCon.getConceptName());
	}

	private String answerLocateFor(CeInstance pInst) {
		StringBuilder sb = new StringBuilder();
		this.answer.addInstance(pInst);

		// TODO: Also check for related instances that are spatial things
		if (pInst.isConceptNamed(this.ac, CON_SPATIAL)) {
			sb.append(pInst.getInstanceName());
		}

		return sb.toString();
	}

	private void answerLocateFor(CeConcept pCon) {
		// TODO: Complete this
		createTbcAnswerWith("answerLocateForConcept: " + pCon.getConceptName());
	}

	private void answerCountFor(CeInstance pInst) {
		// TODO: Complete this
		this.answer.addInstance(pInst);
		createTbcAnswerWith("answerCountForInstance: " + pInst.getInstanceName());
	}

	private void answerCountFor(CeConcept pCon) {
		StringBuilder sb = new StringBuilder();
		String pluralName = pCon.pluralFormName(this.ac);
		int count = this.ac.getModelBuilder().countAllInstancesForConcept(pCon);

		if (count == 0) {
			sb.append("there are no ");
			sb.append(pluralName);
			sb.append(" defined");
		} else if (count == 1) {
			sb.append("there is 1 ");
			sb.append(pCon.getConceptName());
			sb.append(" defined");
		} else {
			sb.append("there are ");
			sb.append(count);
			sb.append(" ");
			sb.append(pluralName);
			sb.append(" defined");
		}

		createStandardAnswerWith(sb.toString(), null);
	}

	private void answerListFor(CeInstance pInst) {
		// TODO: Complete this
		this.answer.addInstance(pInst);
		createTbcAnswerWith("answerListForInstance: " + pInst.getInstanceName());
	}

	private void answerListFor(ArrayList<ConceptPhrase> pConList) {
		for (ConceptPhrase thisCp : pConList) {
			for (CeConcept thisCon : thisCp.getConcepts()) {
				answerListFor(thisCon);
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
		StringBuilder sb = new StringBuilder();
		ArrayList<CeInstance> conList = this.ac.getModelBuilder().listAllInstancesForConcept(pCon);

		sb.append("There are ");
		sb.append(conList.size());
		sb.append(" ");
		sb.append(pCon.pluralFormName(this.ac));
		sb.append(":");
		sb.append(NL);

		for (CeInstance thisInst : conList) {
			sb.append(thisInst.getInstanceName());
			sb.append(NL);
		}

		createStandardAnswerWith(sb.toString(), null);
	}

	private void answerListFor(CeConcept pCon, CeProperty pProp) {
//		StringBuilder sb = new StringBuilder();
//		ArrayList<CeInstance> valList = new ArrayList<CeInstance>();
//		ArrayList<CeInstance> emptyList = new ArrayList<CeInstance>();
		ArrayList<CeInstance> conList = this.ac.getModelBuilder().listAllInstancesForConcept(pCon);

		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();

		titles.add(pCon.getConceptName());
		titles.add(pProp.getPropertyName());
		
		sortInstancesByProperty(conList, pProp, isSortDescending()); 

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

		createResultSetAnswerWith(titles, rows);

//		for (CeInstance thisInst : conList) {
//			CePropertyInstance thisPi = thisInst.getPropertyInstanceForProperty(pProp);
//
//			if (thisPi != null) {
//				valList.add(thisInst);
//			} else {
//				emptyList.add(thisInst);
//			}
//		}
//
//		sortInstancesByProperty(valList, pProp, isSortDescending()); 
//
//		sb.append("There are ");
//		sb.append(valList.size());
//		sb.append(" ");
//		sb.append(pCon.pluralFormName(this.ac));
//		sb.append(" with a stated ");
//		sb.append(pProp.getPropertyName());
//		sb.append(" (and ");
//		sb.append(emptyList.size());
//		sb.append(" with no stated ");
//		sb.append(pProp.getPropertyName());
//		sb.append("):");
//
//		sb.append(NL);
//
//		for (CeInstance thisInst : valList) {
//			CePropertyInstance thisPi = thisInst.getPropertyInstanceForProperty(pProp);
//			
//			if (thisPi != null) {
//				String propVal = thisPi.getSingleOrFirstValue();
//
//				sb.append(thisInst.getInstanceName());
//				sb.append(", ");
//				sb.append(propVal);
//				sb.append(" ");
//				sb.append(pProp.getPropertyName());
//				sb.append(NL);
//			}
//		}
//
//		createStandardAnswerWith(sb.toString(), null, conList);
	}

	private boolean isSortDescending() {
		boolean result = true;
		
		for (CeInstance thisInst : this.otherInstances) {
			String mtVal = thisInst.getSingleValueFromPropertyNamed(PROP_MAPSTO);
			
			if (mtVal.equals(ABS_ASC)) {
				result = false;
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
		this.answer.addInstance(pInst);
		createTbcAnswerWith("answerShowForInstance: " + pInst.getInstanceName());
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

		this.answer.addInstance(pInst);

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
		CeStoreJsonObject jFirstAnswer = this.answer.toJson(this.ac, this.returnInstances);
		CeStoreJsonObject jQuestion = new CeStoreJsonObject();

		CeStoreJsonObject jQues = (CeStoreJsonObject) this.interpretationJson.get(this.ac, JSON_QUESTION);

		if (jQues != null) {
			String qText = jQues.getString(JSON_Q_TEXT);

			if ((qText != null) && (!qText.isEmpty())) {
				jQuestion.put(JSON_Q_TEXT, qText);
			}
		}

		jAnswers.add(jFirstAnswer);

		jResult.put(JSON_QUESTION, jQuestion);
		
		if (this.returnInterpretation) {
			jResult.put(JSON_INT, this.interpretationJson);
		}

		jResult.put(JSON_ANSWERS, jAnswers);

		return jResult;
	}

}
