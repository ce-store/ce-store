package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class SpMatchedTriple extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TYPE_NAME = "matched-triple";
	private static final String JSON_PRED = "predicate";
	private static final String JSON_SUBS = "subjects";
	private static final String JSON_OBJS = "objects";

	private ArrayList<CeInstance> subjects = null;
	private CeProperty predicate = null;
	private ArrayList<CeInstance> objects = null;

	public SpMatchedTriple(String pLabel, CeProperty pProp) {
		this.label = pLabel;
		this.predicate = pProp;
	}

	public static SpMatchedTriple createFromJson(ActionContext pAc, CeStoreJsonObject pJo) {
		SpMatchedTriple result = new SpMatchedTriple("", null);
		ArrayList<CeInstance> subInsts = new ArrayList<CeInstance>();
		ArrayList<CeInstance> objInsts = new ArrayList<CeInstance>();

		result.extractStandardFieldsFromJson(pJo);

		CeStoreJsonObject jPred = pJo.getJsonObject(JSON_PRED);
		if (!jPred.isEmpty()) {
			String propId = jPred.getString(JSON_CEID);
			
			result.predicate = pAc.getModelBuilder().getPropertyFullyNamed(propId);
		}

		CeStoreJsonArray jSubs = pJo.getJsonArray(JSON_SUBS);
		if (!jSubs.isEmpty()) {
			for (Object oSub : jSubs.items()) {
				CeStoreJsonObject jSub = (CeStoreJsonObject)oSub;
				CeInstance subInst = pAc.getModelBuilder().getInstanceNamed(pAc, jSub.getString(JSON_CEID));
				subInsts.add(subInst);
			}
		}

		CeStoreJsonArray jObjs = pJo.getJsonArray(JSON_OBJS);
		if (!jObjs.isEmpty()) {
			for (Object oObj : jSubs.items()) {
				CeStoreJsonObject jObj = (CeStoreJsonObject)oObj;
				CeInstance subInst = pAc.getModelBuilder().getInstanceNamed(pAc, jObj.getString(JSON_CEID));
				objInsts.add(subInst);
			}
		}

		result.subjects = subInsts;
		result.objects = objInsts;

		return result;
	}
	
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

	public ArrayList<CeInstance> getSubjects() {
		return this.subjects;
	}

	public void setSubjects(ArrayList<CeInstance> pList) {
		this.subjects = pList;
	}
	
	public CeProperty getPredicate() {
		return this.predicate;
	}

	public ArrayList<CeInstance> getObjects() {
		return this.objects;
	}

	public void setObjects(ArrayList<CeInstance> pList) {
		this.objects = pList;
	}
	
	public CeStoreJsonObject toJson(ActionContext pAc, int pCtr) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();

		jResult.put(JSON_TYPE, TYPE_NAME);
		jResult.put(JSON_NAME, getLabel());
		jResult.put(JSON_POS, pCtr);
		jResult.put(JSON_PRED, QuestionInterpreterHandler.jsonFor(pAc, getPredicate()));
		jResult.put(JSON_SUBS, QuestionInterpreterHandler.jsonFor(pAc, getSubjects()));
		jResult.put(JSON_OBJS, QuestionInterpreterHandler.jsonFor(pAc, getObjects()));

		return jResult;
	}

}
