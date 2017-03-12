package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;

public class Interpretation {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	int confidence = -1;
	ArrayList<InstancePhrase> instancePhrases = null;
	ArrayList<ConceptPhrase> conceptPhrases = null;
	ArrayList<PropertyPhrase> propertyPhrases = null;
	ArrayList<SpecialPhrase> specialPhrases = null;

	public Interpretation(ActionContext pAc, CeStoreJsonObject pJo) {
		CeStoreJsonObject jResult = pJo.getJsonObject("result");

		this.confidence = pJo.getNumber("confidence").intValue();

		if (jResult != null) {
			this.instancePhrases = populateInstancePhrases(pAc, jResult.getJsonArray("instances"));
			this.conceptPhrases = populateConceptPhrases(pAc, jResult.getJsonArray("concepts"));
			this.propertyPhrases = populatePropertyPhrases(pAc, jResult.getJsonArray("properties"));
			this.specialPhrases = populateSpecialPhrases(pAc, jResult.getJsonArray("specials"));
		}
	}

	public int getConfidence() {
		return this.confidence;
	}

	public ArrayList<ConceptPhrase> getConceptPhrases() {
		return this.conceptPhrases;
	}

	public ArrayList<InstancePhrase> getInstancePhrases() {
		return this.instancePhrases;
	}

	public ArrayList<PropertyPhrase> getPropertyPhrases() {
		return this.propertyPhrases;
	}

	public ArrayList<SpecialPhrase> getSpecialPhrases() {
		return this.specialPhrases;
	}

	private static ArrayList<InstancePhrase> populateInstancePhrases(ActionContext pAc, CeStoreJsonArray pJsonArray) {
		ArrayList<InstancePhrase> result = new ArrayList<InstancePhrase>();

		if (pJsonArray != null) {
			for (Object thisObj : pJsonArray.items()) {
				CeStoreJsonObject jObj = (CeStoreJsonObject)thisObj;
				result.add(new InstancePhrase(pAc, jObj));
			}
		}

		return result;
	}

	private static ArrayList<ConceptPhrase> populateConceptPhrases(ActionContext pAc, CeStoreJsonArray pJsonArray) {
		ArrayList<ConceptPhrase> result = new ArrayList<ConceptPhrase>();

		if (pJsonArray != null) {
			for (Object thisObj : pJsonArray.items()) {
				CeStoreJsonObject jObj = (CeStoreJsonObject)thisObj;
				result.add(new ConceptPhrase(pAc, jObj));
			}
		}

		return result;
	}

	private static ArrayList<PropertyPhrase> populatePropertyPhrases(ActionContext pAc, CeStoreJsonArray pJsonArray) {
		ArrayList<PropertyPhrase> result = new ArrayList<PropertyPhrase>();

		if (pJsonArray != null) {
			for (Object thisObj : pJsonArray.items()) {
				CeStoreJsonObject jObj = (CeStoreJsonObject)thisObj;
				result.add(new PropertyPhrase(pAc, jObj));
			}
		}

		return result;
	}

	private static ArrayList<SpecialPhrase> populateSpecialPhrases(ActionContext pAc, CeStoreJsonArray pJsonArray) {
		ArrayList<SpecialPhrase> result = new ArrayList<SpecialPhrase>();

		if (pJsonArray != null) {
			for (Object thisObj : pJsonArray.items()) {
				CeStoreJsonObject jObj = (CeStoreJsonObject)thisObj;
				result.add(new SpecialPhrase(pAc, jObj));
			}
		}

		return result;
	}

	public ArrayList<InstancePhrase> getBestInstancePhrases() {
		ArrayList<InstancePhrase> result = new ArrayList<InstancePhrase>();

		for (InstancePhrase thisIp : this.instancePhrases) {
			boolean ignore = false;
			
			for (InstancePhrase thisIp2 : this.instancePhrases) {
				if (!thisIp.equals(thisIp2)) {
					if (isInRange(thisIp, thisIp2)) {
						ignore = true;
					}
				}
			}

			if (!ignore) {
				result.add(thisIp);
			}
		}

		return result;
	}

	public boolean isInRange(InstancePhrase pIp1, InstancePhrase pIp2) {
		boolean result = false;

		if (pIp1.getStartPos() >= pIp2.getStartPos()) {
			if (pIp1.getEndPos() <= pIp2.getEndPos()) {
				result = true;
			}
		}

		return result;
	}

}
