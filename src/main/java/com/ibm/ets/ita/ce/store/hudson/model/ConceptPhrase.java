package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;

public class ConceptPhrase extends InterpretationPhrase {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ArrayList<CeConcept> concepts = new ArrayList<CeConcept>();

	public ConceptPhrase(ActionContext pAc, CeStoreJsonObject pJo) {
		super(pJo);

		CeStoreJsonArray jEnts = pJo.getJsonArray("entities");

		if (jEnts != null) {
			for (Object thisObj : jEnts.items()) {
				CeStoreJsonObject jEnt = (CeStoreJsonObject)thisObj;
				
				String conName = jEnt.getString("_id");
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

}
