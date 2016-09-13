package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.RANGE_VALUE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_RULENAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_SENID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_CON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_NEGCON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_PROP;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_NEGPROP;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_INST;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_RANGE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_VAL;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_RATCE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_PREMS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_CONCS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RAT_VALSENIDS;

import java.util.ArrayList;
import java.util.Collection;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleConclusion;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationalePart;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationalePremise;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeWebRationaleReasoningStep extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public CeWebRationaleReasoningStep(ActionContext pAc) {
		super(pAc);
	}

	public CeStoreJsonArray generateListFrom(Collection<CeRationaleReasoningStep> pRsList) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		if (pRsList != null) {
			for (CeRationaleReasoningStep thisRs : pRsList) {
				jArr.add(generateFor(thisRs));
			}
		}
		
		return jArr;
	}
	
	public CeStoreJsonObject generateFor(CeRationaleReasoningStep pRs) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
				
		putStringValueIn(jObj, JSON_RAT_ID, pRs.getId());
		putStringValueIn(jObj, JSON_RAT_RULENAME, pRs.getRuleName());
		putStringValueIn(jObj, JSON_RAT_SENID, pRs.getSourceSentence().formattedId());
		putStringValueIn(jObj, JSON_RAT_RATCE, pRs.getRationaleCe());			
		putArrayValueIn(jObj, JSON_RAT_PREMS, processPremises(pRs));
		putArrayValueIn(jObj, JSON_RAT_CONCS, processConclusions(pRs));

		return jObj;
	}
	
	private CeStoreJsonArray processPremises(CeRationaleReasoningStep pRs) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		
		for (CeRationalePremise thisPrem : pRs.getPremises()) {
			CeStoreJsonObject jPrem = extractRationalePartJsonFrom(thisPrem);
			CeStoreJsonArray jArrSen = new CeStoreJsonArray();
			ArrayList<CePropertyValue> ppvList = this.ac.getModelBuilder().getPropertyValuesFromPremise(this.ac, thisPrem);
			
			for (CePropertyValue thisPpv : ppvList) {
				CeSentence premSen = thisPpv.getSentence();
				if (premSen != null) {
					addStringValueTo(jArrSen, premSen.formattedId());
				}
			}

			putArrayValueIn(jPrem, JSON_RAT_VALSENIDS, jArrSen);
					
			jArr.add(jPrem);
		}
		
		return jArr;
	}

	private static CeStoreJsonArray processConclusions(CeRationaleReasoningStep pRs) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeRationaleConclusion thisConc : pRs.getConclusions()) {
			CeStoreJsonObject jConc = extractRationalePartJsonFrom(thisConc);
			jArr.add(jConc);
		}

		return jArr;
	}

	private static CeStoreJsonObject extractRationalePartJsonFrom(CeRationalePart pPart) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, calculateConceptKey(pPart), calculateConceptName(pPart));
		putStringValueIn(jObj, JSON_RAT_INST, pPart.getInstanceName());
		putStringValueIn(jObj, calculatePropertyKey(pPart), pPart.getPropertyName());
		putStringValueIn(jObj, JSON_RAT_RANGE, pPart.getRangeName());
		putStringValueIn(jObj, JSON_RAT_VAL, pPart.getValue());

		return jObj;
	}

	private static String calculateConceptKey(CeRationalePart pPart) {
		String result = "";

		if (pPart.isConceptNegated()) {
			result = JSON_RAT_NEGCON;
		} else {
			result = JSON_RAT_CON;
		}
		
		return result;
	}
	
	private static String calculateConceptName(CeRationalePart pPart) {
		String result = pPart.getConceptName();
		
		if (result.isEmpty()) {
			result = RANGE_VALUE;
		}

		return result;		
	}

	private static String calculatePropertyKey(CeRationalePart pPart) {
		String result = "";

		if (pPart.isPropertyNegated()) {
			result = JSON_RAT_NEGPROP;
		} else {
			result = JSON_RAT_PROP;
		}

		return result;
	}

}
