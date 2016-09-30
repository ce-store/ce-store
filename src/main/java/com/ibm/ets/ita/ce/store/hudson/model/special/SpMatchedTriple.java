package com.ibm.ets.ita.ce.store.hudson.model.special;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.SPEC_MATCHTRIP;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.model.InstancePhrase;
import com.ibm.ets.ita.ce.store.hudson.model.PropertyPhrase;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.MatchedItem;

public class SpMatchedTriple extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String JSON_PRED = "predicate";
	private static final String JSON_SUBS = "subject instances";
	private static final String JSON_OBJS = "object instances";

	private ArrayList<InstancePhrase> subjects = null;
	private PropertyPhrase predicate = null;
	private ArrayList<InstancePhrase> objects = null;

	public SpMatchedTriple(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		this.subjects = new ArrayList<InstancePhrase>();
		this.objects = new ArrayList<InstancePhrase>();

		CeStoreJsonObject jPred = pJo.getJsonObject(JSON_PRED);
		this.predicate = new PropertyPhrase(pAc, jPred);

		CeStoreJsonArray jSubs = pJo.getJsonArray(JSON_SUBS);

		if (jSubs != null) {
			for (Object thisItem : jSubs.items()) {
				CeStoreJsonObject jSub = (CeStoreJsonObject) thisItem;

				this.subjects.add(new InstancePhrase(pAc, jSub));
			}
		}

		CeStoreJsonArray jObjs = pJo.getJsonArray(JSON_OBJS);

		if (jObjs != null) {
			for (Object thisItem : jObjs.items()) {
				CeStoreJsonObject jObj = (CeStoreJsonObject) thisItem;

				this.objects.add(new InstancePhrase(pAc, jObj));
			}
		}
	}

	public SpMatchedTriple(String pPhraseText, int pStartPos, int pEndPos, MatchedItem pPredicate,
			ArrayList<MatchedItem> pSubjects, ArrayList<MatchedItem> pObjects) {
		super(pPhraseText, pStartPos, pEndPos);

		this.predicate = new PropertyPhrase(pPredicate);

		this.subjects = InstancePhrase.createListFrom(pSubjects);
		this.objects = InstancePhrase.createListFrom(pObjects);
	}

	public boolean isMatchedTriple() {
		return true;
	}

	public boolean isFullTriple() {
		return hasSubjects() && hasObjects();
	}

	public boolean isPartialTriple() {
		return !isFullTriple();
	}

	public boolean isPartialSubjectTriple() {
		return hasSubjects() && !hasObjects();
	}

	public boolean isPartialObjectTriple() {
		return !hasSubjects() && hasObjects();
	}

	public ArrayList<InstancePhrase> getSubjects() {
		return this.subjects;
	}

	public boolean hasSubjects() {
		return ((this.subjects != null) && !this.subjects.isEmpty());
	}

	public PropertyPhrase getPredicate() {
		return this.predicate;
	}

	public ArrayList<InstancePhrase> getObjects() {
		return this.objects;
	}

	public boolean hasObjects() {
		return ((this.objects != null) && !this.objects.isEmpty());
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		CeStoreJsonObject jPred = this.predicate.toJson(pAc);
		CeStoreJsonArray jSubs = new CeStoreJsonArray();
		CeStoreJsonArray jObjs = new CeStoreJsonArray();

		addStandardFields(jResult, SPEC_MATCHTRIP);

		jResult.put(JSON_PRED, jPred);

		for (InstancePhrase subjIp : this.subjects) {
			jSubs.add(subjIp.toJson(pAc));
		}

		if (!jSubs.isEmpty()) {
			jResult.put(JSON_SUBS, jSubs);
		}

		for (InstancePhrase objIp : this.objects) {
			jObjs.add(objIp.toJson(pAc));
		}

		if (!jObjs.isEmpty()) {
			jResult.put(JSON_OBJS, jObjs);
		}

		return jResult;
	}

}
