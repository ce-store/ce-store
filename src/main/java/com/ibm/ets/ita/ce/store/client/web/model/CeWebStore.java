package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_STORE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_TYPE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STYLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CREATED;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SERVER_TIME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STORE_VERSION;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_MOD_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CON_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INST_COUNT;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_FULL;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_SUMMARY;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_MINIMAL;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_NORMALISED;
import static com.ibm.ets.ita.ce.store.names.MiscNames.VERSION;

import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;

public class CeWebStore extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

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

	public static CeStoreJsonArray generateNormalisedListFrom(TreeMap<String, ModelBuilder> pMbs) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		if (pMbs != null) {
			for (String storeName : pMbs.keySet()) {
				ModelBuilder thisMb = pMbs.get(storeName);
				jInsts.add(generateNormalisedDetailsJsonFor(storeName, thisMb));
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
		//TODO: Implement minimal version
		CeStoreJsonObject jObj = generateDetailsJsonFor(pStoreName, pMb, STYLE_MINIMAL);
		return jObj;
	}

	public static CeStoreJsonObject generateNormalisedDetailsJsonFor(String pStoreName, ModelBuilder pMb) {
		//TODO: Implement normalised version
		CeStoreJsonObject jObj = generateDetailsJsonFor(pStoreName, pMb, STYLE_NORMALISED);
		return jObj;
	}

	public static CeStoreJsonObject generateFullDetailsJsonFor(String pStoreName, ModelBuilder pMb) {
		CeStoreJsonObject jObj = generateDetailsJsonFor(pStoreName, pMb, STYLE_FULL);
	return jObj;
	}

	private static CeStoreJsonObject generateDetailsJsonFor(String pStoreName, ModelBuilder pMb, String style) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_STORE);
		putStringValueIn(jObj, JSON_STYLE, style);
		putStringValueIn(jObj, JSON_ID, pStoreName);
		putLongValueIn(jObj, JSON_CREATED, pMb.getCreationTime());

		putIntValueIn(jObj, JSON_SEN_COUNT, pMb.countAllValidSentences());
		putIntValueIn(jObj, JSON_MOD_COUNT, pMb.getAllConceptualModels().size());
		putIntValueIn(jObj, JSON_CON_COUNT, pMb.listAllConcepts().size());
		putIntValueIn(jObj, JSON_INST_COUNT, pMb.listAllInstances().size());
		putLongValueIn(jObj, JSON_SERVER_TIME, System.currentTimeMillis());
		putStringValueIn(jObj, JSON_STORE_VERSION, VERSION);

		return jObj;
	}

}
