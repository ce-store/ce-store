package com.ibm.ets.ita.ce.store.model.rationale;

import java.io.Serializable;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

public class CeRationaleConclusion extends CeRationalePart implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public static CeRationaleConclusion createNew(boolean pConIsNegated, String pConName, String pInstName, boolean pPropIsNegated, String pPropName, String pRangeName, String pVal) {
		CeRationaleConclusion newInst = new CeRationaleConclusion();

//		newInst.id = "conc_" + nextRationaleId();
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
		return "Conclusion";
	}

}
