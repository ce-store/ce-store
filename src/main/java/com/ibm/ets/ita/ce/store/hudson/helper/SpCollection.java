package com.ibm.ets.ita.ce.store.hudson.helper;

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.hudson.handler.QuestionInterpreterHandler;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeModelEntity;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class SpCollection extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String TYPE_NAME = "collection";
	private static final String JSON_CONNECTOR = "connector";
	private static final String JSON_CONTENTS = "contents";
	private static final String JSON_E_TEXT = "text";
	private static final String JSON_E_ITEMS = "items";

	private String firstWordText = null;
	private String connectorWordText = null;
	private HashMap<String, ArrayList<CeModelEntity>> items = new HashMap<String, ArrayList<CeModelEntity>>();

	public SpCollection(String pFirstWordText) {
		this.firstWordText = pFirstWordText;
	}

	public static SpCollection createFromJson(ActionContext pAc, CeStoreJsonObject pJo) {
		SpCollection result = new SpCollection("");

		result.extractStandardFieldsFromJson(pJo);

		//TODO: Complete this
		return result;
	}
	
	public boolean isCollection() {
		return true;
	}
	
	public String getFirstWordText() {
		return this.firstWordText;
	}

	public HashMap<String, ArrayList<CeModelEntity>> getItems() {
		return this.items;
	}

	public void addItem(String pWordText, ArrayList<CeModelEntity> pMe) {
		this.items.put(pWordText, pMe);
	}

	public String getConnectorWordText() {
		return this.connectorWordText;
	}

	public void setConnectorWordText(String pWordText) {
		this.connectorWordText = pWordText;
	}
	
	public String computeLabel() {
		String result = "";

		result += this.firstWordText;

		for (String thisWordText : this.items.keySet()) {
			if (!thisWordText.equals(this.firstWordText)) {
				result += " " + this.connectorWordText + " ";
				result += thisWordText;
			}
		}

		return result;
	}
	
	public CeStoreJsonObject toJson(ActionContext pAc, int pCtr) {
		CeStoreJsonObject jResult = new CeStoreJsonObject();
		String thisKey = computeLabel();
		CeStoreJsonArray jEntList = new CeStoreJsonArray();

		for (String innerWordText : getItems().keySet()) {
			ArrayList<CeModelEntity> entList = getItems().get(innerWordText);
			CeStoreJsonObject jEntry = new CeStoreJsonObject();
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			jEntry.put(JSON_E_TEXT, innerWordText);
			jEntry.put(JSON_E_ITEMS, jArr);

			for (CeModelEntity thisEnt : entList) {
				if (thisEnt.isConcept()) {
					jArr.add(QuestionInterpreterHandler.jsonFor(pAc, (CeConcept)thisEnt));	
				} else if (thisEnt.isProperty()) {
					jArr.add(QuestionInterpreterHandler.jsonFor(pAc, (CeProperty)thisEnt));	
				} else if (thisEnt.isInstance()) {
					jArr.add(QuestionInterpreterHandler.jsonFor(pAc, (CeInstance)thisEnt));	
				} else {
					String className = thisEnt.getClass().getSimpleName();

					reportError("Unexpected class (" + className + ") during SpCollection processing", pAc);
				}
			}

			jEntList.add(jEntry);
		}

		jResult.put(JSON_TYPE, TYPE_NAME);
		jResult.put(JSON_NAME, thisKey);
		jResult.put(JSON_POS, pCtr);
		jResult.put(JSON_CONNECTOR, getConnectorWordText());
		jResult.put(JSON_CONTENTS, jEntList);

		return jResult;
	}
}
