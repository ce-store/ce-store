package com.ibm.ets.ita.ce.store.conversation.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class MatchedTriple extends GeneralItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public static final String CON_DOM_MATCHED = "domain-matched triple";
	public static final String CON_DOM_UNMATCHED = "domain-unmatched triple";
	public static final String CON_DIRECT = "direct matched triple";
	private static final String UID_PREFIX = "mt";
	
	private ExtractedItem extractedItem = null;
	private CeConcept subjectConcept = null;
	private CeInstance subjectInstance = null;

	private CeProperty predicateProperty = null;

	private String objectInstanceId = null;
	private String objectConceptName = null;
	private String objectValue = null;
	private String originalDescription = null;
	private String context = "";
	private boolean isNewInstance = false;
	private boolean isProcessed = false;

	public static MatchedTriple createOpTriple(ActionContext pAc, ExtractedItem pEi, CeConcept pSubCon, CeProperty pPredProp, String pObjInstId, String pObjConName, String pOrigDesc, String pContext, boolean pIsNewInst, CeInstance pSubInst) {
		MatchedTriple thisMt = new MatchedTriple();

		thisMt.id = pAc.getModelBuilder().getNextUid(pAc, UID_PREFIX);
		thisMt.extractedItem = pEi;
		thisMt.subjectConcept = pSubCon;
		thisMt.subjectInstance = pSubInst;
		thisMt.predicateProperty = pPredProp;
		thisMt.objectInstanceId = pObjInstId;
		thisMt.objectConceptName = pObjConName;
		thisMt.originalDescription = pOrigDesc;
		thisMt.context = pContext;
		thisMt.isNewInstance = pIsNewInst;

		pEi.addMatchedTriple(thisMt);

		return thisMt;
	}

	public static MatchedTriple createDpTriple(ActionContext pAc, ExtractedItem pEi, CeConcept pSubCon, CeProperty pPredProp, String pObjVal, String pContext) {
		MatchedTriple thisMt = new MatchedTriple();

		thisMt.id = pAc.getModelBuilder().getNextUid(pAc, UID_PREFIX);
		thisMt.extractedItem = pEi;
		thisMt.subjectConcept = pSubCon;
		thisMt.predicateProperty = pPredProp;
		thisMt.objectValue = pObjVal;
		thisMt.context = pContext;

		pEi.addMatchedTriple(thisMt);

		return thisMt;
	}
	
	public boolean isProcessed() {
		return this.isProcessed;
	}

	public void markAsProcessed() {
		this.isProcessed = true;
	}

	public ExtractedItem getExtractedItem() {
		return this.extractedItem;
	}

	public CeConcept getSubjectConcept() {
		return this.subjectConcept;
	}

	public CeInstance getSubjectInstance() {
		return this.subjectInstance;
	}

	public CeProperty getPredicateProperty() {
		return this.predicateProperty;
	}

	public String getObjectInstanceId() {
		return this.objectInstanceId;
	}

	public String getObjectConceptName() {
		return this.objectConceptName;
	}

	public String getObjectValue() {
		return this.objectValue;
	}

	public String getOriginalDescription() {
		return this.originalDescription;
	}

	public boolean isSubjectConcept() {
		return this.subjectConcept != null;
	}

	public boolean isSubjectInstance() {
		return this.subjectInstance != null;
	}

	public boolean isObjectInstance() {
		return this.objectInstanceId != null;
	}

	public boolean isObjectValue() {
		return this.objectValue != null;
	}

	public boolean isNewInstance() {
		return this.isNewInstance;
	}

	public boolean needsDescription() {
		return isObjectInstance() && (!this.originalDescription.equals(this.objectConceptName));
	}

	public String calculateCeConceptName() {
		return this.context;
	}
	
	public String getDeterminerForObjectConcept(ActionContext pAc) {
		String result = null;
		CeConcept objCon = pAc.getModelBuilder().getConceptNamed(pAc, getObjectConceptName());

		if (objCon != null) {
			result = objCon.conceptQualifier();
		} else {
			//TODO: Should not be hardcoded
			result = "a";
		}

		return result;
	}

	@Override
	public String toString() {
		String result = "";
		String subjLabel = "";
		String subjName = "";
		String objLabel = "";
		String objName = "";
		String predName = this.predicateProperty.getPropertyName();
		
		if (isSubjectConcept()) {
			subjLabel = "con";
			subjName = this.subjectConcept.getConceptName();
		} else {
			subjLabel = "inst";
			subjName = this.subjectInstance.getInstanceName();
		}

		if (isObjectInstance()) {
			objLabel = "inst";
			objName = this.objectInstanceId;
		} else {
			objLabel = "val";
			objName = this.objectValue;
		}

		result = "[MatchedTriple(" + subjLabel + ":" + objLabel + ")] " + subjName + "->" + predName + "->" + objName + " (" + this.context + ")";
		
		return result;
	}

}