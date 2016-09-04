package com.ibm.ets.ita.ce.store.hudson.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.conversation.model.MatchedItem;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;

public class SpMatchedTriple extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TYPE_NAME = "matched-triple";
	private static final String JSON_PRED = "predicate";
	private static final String JSON_SUBS = "subject instances";
	private static final String JSON_OBJS = "object instances";

	private ArrayList<MatchedItem> subjects = null;
	private MatchedItem predicate = null;
	private ArrayList<MatchedItem> objects = null;

	public SpMatchedTriple(CeStoreJsonObject pJo) {
		super(pJo);
	}

	public SpMatchedTriple(String pPhraseText, int pStartPos, int pEndPos, MatchedItem pPredicate, ArrayList<MatchedItem> pSubjects, ArrayList<MatchedItem> pObjects) {
		super(pPhraseText, pStartPos, pEndPos);
		
		this.predicate = pPredicate;
		this.subjects = pSubjects;
		this.objects = pObjects;
	}

//	public static SpMatchedTriple createFromJson(ActionContext pAc, CeStoreJsonObject pJo) {
//		SpMatchedTriple result = new SpMatchedTriple(pJo);
//		ArrayList<CeInstance> subInsts = new ArrayList<CeInstance>();
//		ArrayList<CeInstance> objInsts = new ArrayList<CeInstance>();
//
//		CeStoreJsonObject jPred = pJo.getJsonObject(JSON_PRED);
//		if (!jPred.isEmpty()) {
//			String propId = jPred.getString(JSON_CEID);
//			
//			result.predicate = pAc.getModelBuilder().getPropertyFullyNamed(propId);
//		}
//
//		CeStoreJsonArray jSubs = pJo.getJsonArray(JSON_SUBS);
//		if (!jSubs.isEmpty()) {
//			for (Object oSub : jSubs.items()) {
//				CeStoreJsonObject jSub = (CeStoreJsonObject)oSub;
//				CeInstance subInst = pAc.getModelBuilder().getInstanceNamed(pAc, jSub.getString(JSON_CEID));
//				subInsts.add(subInst);
//			}
//		}
//
//		CeStoreJsonArray jObjs = pJo.getJsonArray(JSON_OBJS);
//		if (!jObjs.isEmpty()) {
//			for (Object oObj : jSubs.items()) {
//				CeStoreJsonObject jObj = (CeStoreJsonObject)oObj;
//				CeInstance subInst = pAc.getModelBuilder().getInstanceNamed(pAc, jObj.getString(JSON_CEID));
//				objInsts.add(subInst);
//			}
//		}
//
//		result.subjects = subInsts;
//		result.objects = objInsts;
//
//		return result;
//	}
	
	public boolean isMatchedTriple() {
		return true;
	}

	public boolean isFullTriple() {
		return !this.subjects.isEmpty() && !this.objects.isEmpty();
	}

	public boolean isPartialTriple() {
		return this.subjects.isEmpty() || this.objects.isEmpty();
	}

	public boolean isPartialSubjectTriple() {
		return !this.subjects.isEmpty() && this.objects.isEmpty();
	}

	public boolean isPartialObjectTriple() {
		return this.subjects.isEmpty() && !this.objects.isEmpty();
	}

	public ArrayList<MatchedItem> getSubjects() {
		return this.subjects;
	}

	public MatchedItem getPredicate() {
		return this.predicate;
	}

	public ArrayList<MatchedItem> getObjects() {
		return this.objects;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonObject jPred = QuestionInterpreterHandler.jsonForMatchedItemProperty(pAc, getPredicate());
		CeStoreJsonArray jSubs = QuestionInterpreterHandler.jsonForMatchedItemInstances(pAc, getSubjects());
		CeStoreJsonArray jObjs = QuestionInterpreterHandler.jsonForMatchedItemInstances(pAc, getObjects());

		addStandardFields(jResult, TYPE_NAME);

		jResult.put(JSON_PRED, jPred);

		if (!jSubs.isEmpty()) {
			jResult.put(JSON_SUBS, jSubs);
		}

		if (!jObjs.isEmpty()) {
			jResult.put(JSON_OBJS, jObjs);
		}

		return jResult;
	}

}
