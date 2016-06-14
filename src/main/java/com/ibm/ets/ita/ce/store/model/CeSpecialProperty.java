package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeSpecialProperty extends CeProperty {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public static final String SPECIALNAME_CONTAINS = "contains";
	public static final String SPECIALNAME_NOTCONTAINS = "not-contains";
	public static final String SPECIALNAME_MATCHES = "matches";
	public static final String SPECIALNAME_STARTSWITH = "starts-with";
	public static final String SPECIALNAME_NOTSTARTSWITH = "not-starts-with";
	public static final String SPECIALNAME_ENDSWITH = "ends-with";
	public static final String SPECIALNAME_EQUALS = "=";
	public static final String SPECIALNAME_NOTEQUALS = "!=";
	public static final String SPECIALNAME_GREATER = ">";
	public static final String SPECIALNAME_LESS = "<";
	public static final String SPECIALNAME_GREATEROREQUAL = ">=";
	public static final String SPECIALNAME_LESSOREQUAL = "<=";

	private CeSpecialProperty() {
		// This is private to ensure that new instances can only be created via
		// the various static methods
	}

	private static CeSpecialProperty createNewSpecialOperatorPropertyNamed(String pName) {
		CeSpecialProperty newProp = new CeSpecialProperty();

		newProp.name = pName;
		newProp.ceStyle = STYLE_VS;
		newProp.ceCardinality = CARDINALITY_SINGLE;

		return newProp;
	}

	public static CeSpecialProperty getSpecialOperatorPropertyNamed(ActionContext pAc, String pPropName) {
		CeSpecialProperty result = null;

		//TODO: Improve this code, make it more dynamic
		if (pPropName.equals(SPECIALNAME_CONTAINS)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_NOTCONTAINS)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_MATCHES)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_STARTSWITH)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_NOTSTARTSWITH)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_ENDSWITH)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_EQUALS)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_NOTEQUALS)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_GREATER)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_LESS)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_GREATEROREQUAL)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else if (pPropName.equals(SPECIALNAME_LESSOREQUAL)) {
			result = CeSpecialProperty.createNewSpecialOperatorPropertyNamed(pPropName);
		} else {
			reportError("Unknown special operator name '" + pPropName + "'", pAc);
		}
		
		return result;
	}
	
	public static boolean isSpecialValueOperator(String pPropName) {
		return (isSpecialUniversalOperator(pPropName) ||
				(pPropName.equals(SPECIALNAME_CONTAINS)) ||
				(pPropName.equals(SPECIALNAME_NOTCONTAINS)) ||
				(pPropName.equals(SPECIALNAME_MATCHES)) ||
				(pPropName.equals(SPECIALNAME_STARTSWITH)) ||
				(pPropName.equals(SPECIALNAME_NOTSTARTSWITH)) ||
				pPropName.equals(SPECIALNAME_ENDSWITH));
	}

	public static boolean isSpecialUniversalOperator(String pPropName) {
		return (pPropName.equals(SPECIALNAME_EQUALS)) ||
				(pPropName.equals(SPECIALNAME_NOTEQUALS)) ||
				(pPropName.equals(SPECIALNAME_GREATER)) ||
				(pPropName.equals(SPECIALNAME_LESS)) ||
				(pPropName.equals(SPECIALNAME_GREATEROREQUAL)) ||
				(pPropName.equals(SPECIALNAME_LESSOREQUAL));
	}
	
	@Override
	public boolean isSpecialOperatorProperty() {
		return true;
	}
	
}
