package com.ibm.ets.ita.ce.store.model.rationale;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

public class CeRationalePremise extends CeRationalePart {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public static CeRationalePremise createNew(boolean pConIsNegated, String pConName, String pInstName, boolean pPropIsNegated, String pPropName, String pRangeName, String pVal) {
		CeRationalePremise newInst = new CeRationalePremise();

		newInst.id = "prem_" + nextRationaleId();
		newInst.isConceptNegated = pConIsNegated;
		newInst.isPropertyNegated = pPropIsNegated;
		newInst.conceptName = pConName;
		newInst.instanceName = pInstName;
		newInst.propertyName = pPropName;
		newInst.rangeName = pRangeName;
		newInst.value = pVal;

		return newInst;
	}

	@Override
	protected String formattedType() {
		return "Premise";
	}

}