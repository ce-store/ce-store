package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ENTITIES;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class ConceptPhrase extends InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<CeConcept> concepts = new ArrayList<CeConcept>();

	public ConceptPhrase(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonArray jEnts = pJo.getJsonArray(JSON_ENTITIES);

		if (jEnts != null) {
			for (Object thisObj : jEnts.items()) {
				CeStoreJsonObject jEnt = (CeStoreJsonObject) thisObj;

				String conName = jEnt.getString(JSON_ID);
				CeConcept thisCon = pAc.getModelBuilder().getConceptNamed(pAc, conName);

				if (thisCon != null) {
					this.concepts.add(thisCon);
				}
			}
		}
	}

	public ArrayList<CeConcept> getConcepts() {
		return this.concepts;
	}

	public CeStoreJsonObject toJson(ActionContext pAc) {
		CeStoreJsonObject result = new CeStoreJsonObject();

		super.toJsonUsing(result);

		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeConcept thisCon : this.concepts) {
			CeInstance mmInst = thisCon.retrieveMetaModelInstance(pAc);

			if (mmInst != null) {
				CeWebInstance webInst = new CeWebInstance(pAc);
				CeStoreJsonObject jInst = webInst.generateNormalisedDetailsJsonFor(mmInst, null, 0, false, false, null);

				jArr.add(jInst);
			}
		}

		return result;
	}

}
