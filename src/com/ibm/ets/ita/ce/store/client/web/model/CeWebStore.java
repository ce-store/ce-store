package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.StoreConfig;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

public class CeWebStore extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//JSON Response Keys
	private static final String KEY_SERVER_TIME = "server_time";
	private static final String KEY_STORE_VERSION = "store_version";
	private static final String KEY_SEN_COUNT = "sentence_count";
	private static final String KEY_MOD_COUNT = "model_count";
	private static final String KEY_CON_COUNT = "concept_count";
	private static final String KEY_INST_COUNT = "instance_count";

	private static final String TYPE_STORE = "store";

	public CeWebStore(ActionContext pAc) {
		super(pAc);
	}

	public static CeStoreJsonArray generateSummaryListFrom(TreeMap<String, ModelBuilder> pMbs) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		if (pMbs != null) {
			for (String storeName : pMbs.keySet()) {
				ModelBuilder thisMb = pMbs.get(storeName);
				jInsts.add(generateSummaryDetailsJsonFor(storeName, thisMb));
			}
		}

		return jInsts;
	}

	public static CeStoreJsonArray generateMinimalListFrom(TreeMap<String, ModelBuilder> pMbs) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		if (pMbs != null) {
			for (String storeName : pMbs.keySet()) {
				ModelBuilder thisMb = pMbs.get(storeName);
				jInsts.add(generateMinimalDetailsJsonFor(storeName, thisMb));
			}
		}

		return jInsts;
	}

	public static CeStoreJsonArray generateFullListFrom(TreeMap<String, ModelBuilder> pMbs) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		if (pMbs != null) {
			for (String storeName : pMbs.keySet()) {
				ModelBuilder thisMb = pMbs.get(storeName);
				jInsts.add(generateFullDetailsJsonFor(storeName, thisMb));
			}
		}

		return jInsts;
	}

	public static CeStoreJsonObject generateSummaryDetailsJsonFor(String pStoreName, ModelBuilder pMb) {
		CeStoreJsonObject jObj = generateDetailsJsonFor(pStoreName, pMb, STYLE_SUMMARY);
		return jObj;
	}

	public static CeStoreJsonObject generateMinimalDetailsJsonFor(String pStoreName, ModelBuilder pMb) {
		CeStoreJsonObject jObj = generateDetailsJsonFor(pStoreName, pMb, STYLE_MINIMAL);
		return jObj;
	}

	public static CeStoreJsonObject generateFullDetailsJsonFor(String pStoreName, ModelBuilder pMb) {
		CeStoreJsonObject jObj = generateDetailsJsonFor(pStoreName, pMb, STYLE_FULL);
	return jObj;
	}

	private static CeStoreJsonObject generateDetailsJsonFor(String pStoreName, ModelBuilder pMb, String style) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, KEY_TYPE, TYPE_STORE);
		putStringValueIn(jObj, KEY_STYLE, style);
		putStringValueIn(jObj, KEY_ID, pStoreName);
		putLongValueIn(jObj, KEY_CREATED, pMb.getCreationTime());

		putIntValueIn(jObj, KEY_SEN_COUNT, pMb.countAllValidSentences());
		putIntValueIn(jObj, KEY_MOD_COUNT, pMb.getAllConceptualModels().size());
		putIntValueIn(jObj, KEY_CON_COUNT, pMb.listAllConcepts().size());
		putIntValueIn(jObj, KEY_INST_COUNT, pMb.listAllInstances().size());
		putLongValueIn(jObj, KEY_SERVER_TIME, System.currentTimeMillis());
		putStringValueIn(jObj, KEY_STORE_VERSION, StoreConfig.VERSION);

		return jObj;
	}

}