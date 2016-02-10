package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeAnnotation;
import com.ibm.ets.ita.ce.store.model.CeModelEntity;

public abstract class CeWebObject {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	protected static final String KEY_TYPE = "_type";
	protected static final String KEY_STYLE = "_style";
	protected static final String KEY_ID = "_id";
	protected static final String KEY_SHADOW = "_shadow";
	protected static final String KEY_CREATED = "_created";
	protected static final String KEY_LABEL = "_label";
	protected static final String KEY_META_INSTANCE = "meta_instance";

	protected static final String STYLE_FULL = "full";
	protected static final String STYLE_SUMMARY = "summary";

	private static final String KEY_ANNOTATIONS = "annotations";
	protected static final String KEY_SEN_TEXT = "ce_text";

	protected ActionContext ac = null;

	//TODO: Review this when complete - e.g. move methods from static to instance

	public CeWebObject(ActionContext pAc) {
		this.ac = pAc;
	}

	protected static void addStringValueTo(CeStoreJsonArray pArr, String pVal) {
		if ((pVal != null) && (!pVal.isEmpty())) {
			pArr.add(pVal);
		}
	}

	protected static void addObjectValueTo(CeStoreJsonArray pArr, CeStoreJsonObject pVal) {
		if (pVal != null) {
			pArr.add(pVal);
		}
	}

	protected static void addAllStringValuesTo(CeStoreJsonArray pArr, ArrayList<String> pVals) {
		if (pVals != null) {
			pArr.addAll(pVals);
		}
	}

	protected static void putStringValueIn(CeStoreJsonObject pObj, String pKey, String pVal) {
		if ((pVal != null) && (!pVal.isEmpty())) {
			pObj.put(pKey, pVal);
		}
	}

	protected static void putObjectValueIn(CeStoreJsonObject pObj, String pKey, Object pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putBooleanValueIn(CeStoreJsonObject pObj, String pKey, boolean pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putIntValueIn(CeStoreJsonObject pObj, String pKey, int pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putLongValueIn(CeStoreJsonObject pObj, String pKey, long pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putDoubleValueIn(CeStoreJsonObject pObj, String pKey, double pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putArrayValueIn(CeStoreJsonObject pObj, String pKey, CeStoreJsonArray pVal) {
		if ((pVal != null)) {
			pObj.put(pKey, pVal);
		}
	}

	protected static void putObjectValueIn(CeStoreJsonObject pObj, String pKey, CeStoreJsonObject pVal) {
		if ((pVal != null)) {
			pObj.put(pKey, pVal);
		}
	}

	protected static void putAllStringValuesIn(CeStoreJsonObject pObj, String pKey, Collection<String> pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (String thisVal : pVals) {
				jArr.add(thisVal);
			}
			pObj.put(pKey, jArr);
		}
	}

	protected static void putAllStringValuesIn(CeStoreJsonObject pObj, String pKey, String[] pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (String thisVal : pVals) {
				jArr.add(thisVal);
			}
			pObj.put(pKey, jArr);
		}
	}

	protected static void putAllArrayStringValuesIn(CeStoreJsonObject pObj, String pKey, ArrayList<ArrayList<String>> pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jOuterArr = new CeStoreJsonArray();

			for (ArrayList<String> thisArr : pVals) {
				CeStoreJsonArray jInnerArr = new CeStoreJsonArray();
				for (String thisVal : thisArr) {
					jInnerArr.add(thisVal);
				}	
				jOuterArr.add(jInnerArr);
			}
			pObj.put(pKey, jOuterArr);
		}
	}

	protected static CeStoreJsonObject createJsonObjectsFor(LinkedHashMap<String, String> pLhm) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		for (String thisKey : pLhm.keySet()) {
			putStringValueIn(jObj, thisKey, pLhm.get(thisKey));
		}

		return jObj;
	}

	public static CeStoreJsonObject generateStandardAlertsFrom(LinkedHashMap<String, ArrayList<String>> pAlerts) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (!pAlerts.isEmpty()) {
			for (String thisKey : pAlerts.keySet()) {
				putAllStringValuesIn(jObj, thisKey, pAlerts.get(thisKey));
			}
		}

		return jObj;
	}

	public static CeStoreJsonArray generateStandardMessagesFrom(ArrayList<String> pMessages) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		if (!pMessages.isEmpty()) {
			addAllStringValuesTo(jArr, pMessages);
		}

		return jArr;
	}

	public static CeStoreJsonObject generateStandardStatsFrom(LinkedHashMap<String, String> pStats) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (!pStats.isEmpty()) {
			for (String thisKey : pStats.keySet()) {
				putStringValueIn(jObj, thisKey, pStats.get(thisKey));
			}
		}

		return jObj;
	}

	protected static void processAnnotations(CeModelEntity pObj, CeStoreJsonObject pJsonObj) {
		CeStoreJsonObject annoObj = new CeStoreJsonObject();

		for (CeAnnotation thisAnno : pObj.getAnnotations()) {
			putStringValueIn(annoObj, thisAnno.trimmedLabel(), thisAnno.getText());
		}

		if (!annoObj.isEmpty()) {
			putObjectValueIn(pJsonObj, KEY_ANNOTATIONS, annoObj);
		}
	}

}