package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleConclusion;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationalePart;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationalePremise;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

public class CeWebRationaleReasoningStep extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//JSON Response Keys
	private static final String KEY_RAT_ID = "rat_id";
	private static final String KEY_RAT_RULENAME = "rule_name";
	private static final String KEY_RAT_SENID = "source_senid";
	private static final String KEY_RAT_CON = "concept";
	private static final String KEY_RAT_NEGCON = "negated_concept";
	private static final String KEY_RAT_PROP = "property";
	private static final String KEY_RAT_NEGPROP = "negated_property";
	private static final String KEY_RAT_INST = "instance";
	private static final String KEY_RAT_RANGE = "range";
	private static final String KEY_RAT_VAL = "value";
	private static final String KEY_RAT_RATCE = "rationale_ce";
	private static final String KEY_RAT_PREMS = "premises";
	private static final String KEY_RAT_CONCS = "conclusions";
	private static final String KEY_RAT_VALSENIDS = "value_senids";
	
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
				
		putStringValueIn(jObj, KEY_RAT_ID, pRs.getId());
		putStringValueIn(jObj, KEY_RAT_RULENAME, pRs.getRuleName());
		putStringValueIn(jObj, KEY_RAT_SENID, pRs.getSourceSentence().formattedId());
		putStringValueIn(jObj, KEY_RAT_RATCE, pRs.getRationaleCe());			
		putArrayValueIn(jObj, KEY_RAT_PREMS, processPremises(pRs));
		putArrayValueIn(jObj, KEY_RAT_CONCS, processConclusions(pRs));

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

			putArrayValueIn(jPrem, KEY_RAT_VALSENIDS, jArrSen);
					
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
		putStringValueIn(jObj, KEY_RAT_INST, pPart.getInstanceName());
		putStringValueIn(jObj, calculatePropertyKey(pPart), pPart.getPropertyName());
		putStringValueIn(jObj, KEY_RAT_RANGE, pPart.getRangeName());
		putStringValueIn(jObj, KEY_RAT_VAL, pPart.getValue());

		return jObj;
	}

	private static String calculateConceptKey(CeRationalePart pPart) {
		String result = "";

		if (pPart.isConceptNegated()) {
			result = KEY_RAT_NEGCON;
		} else {
			result = KEY_RAT_CON;
		}
		
		return result;
	}
	
	private static String calculateConceptName(CeRationalePart pPart) {
		String result = pPart.getConceptName();
		
		if (result.isEmpty()) {
			result = CeProperty.RANGE_VALUE;
		}

		return result;		
	}

	private static String calculatePropertyKey(CeRationalePart pPart) {
		String result = "";

		if (pPart.isPropertyNegated()) {
			result = KEY_RAT_NEGPROP;
		} else {
			result = KEY_RAT_PROP;
		}

		return result;
	}

}