package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.Collection;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class CeWebProperty extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//JSON Response Keys
	private static final String KEY_DOMAIN_NAME = "domain_name";
	private static final String KEY_RANGE_NAME = "range_name";
	private static final String KEY_PROP_NAME = "property_name";
	private static final String KEY_PROP_STYLE = "property_style";
	private static final String KEY_PROP_TYPE = "property_type";
	private static final String KEY_ASS_DOMAIN_NAME = "asserted_domain_name";

	//Fixed values for returned properties
	private static final String TYPE_PROPERTY = "property";
	
	public CeWebProperty(ActionContext pAc) {
		super(pAc);
	}

	public CeStoreJsonObject generateFullDetailsJsonFor(CeProperty pProp) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

        putStringValueIn(jObj, KEY_TYPE, TYPE_PROPERTY);
        putStringValueIn(jObj, KEY_STYLE, STYLE_FULL);
		putStringValueIn(jObj, KEY_ID, pProp.formattedFullPropertyName());
		putLongValueIn(jObj, KEY_CREATED, pProp.getCreationDate());
        putStringValueIn(jObj, KEY_PROP_NAME, pProp.getPropertyName());
		putStringValueIn(jObj, KEY_ASS_DOMAIN_NAME, pProp.calculateAssertedDomainConceptName());
		putStringValueIn(jObj, KEY_DOMAIN_NAME, pProp.calculateDomainConceptName());
		putStringValueIn(jObj, KEY_RANGE_NAME, pProp.getRangeConceptName());
		putStringValueIn(jObj, KEY_PROP_STYLE, pProp.formattedCeStyle());
		putStringValueIn(jObj, KEY_PROP_TYPE, pProp.formattedCeType());

		// add meta-model details
		addMetamodelInstanceFor(pProp, jObj);

		return jObj;
	}

	public CeStoreJsonObject generateSummaryDetailsJsonFor(CeProperty pProp) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

        putStringValueIn(jObj, KEY_TYPE, TYPE_PROPERTY);
        putStringValueIn(jObj, KEY_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, KEY_ID, pProp.formattedFullPropertyName());
        putLongValueIn(jObj, KEY_CREATED, pProp.getCreationDate());
		putStringValueIn(jObj, KEY_PROP_NAME, pProp.getPropertyName());
		putStringValueIn(jObj, KEY_ASS_DOMAIN_NAME, pProp.calculateAssertedDomainConceptName());
		putStringValueIn(jObj, KEY_DOMAIN_NAME, pProp.calculateDomainConceptName());
		putStringValueIn(jObj, KEY_RANGE_NAME, pProp.getRangeConceptName());
		putStringValueIn(jObj, KEY_PROP_STYLE, pProp.formattedCeStyle());
		putStringValueIn(jObj, KEY_PROP_TYPE, pProp.formattedCeType());

		// add meta-model details
		addMetamodelInstanceFor(pProp, jObj);

		return jObj;
	}

	public CeStoreJsonArray generateFullListJsonFor(Collection<CeProperty> pPropList) {
		CeStoreJsonArray jProps = new CeStoreJsonArray();
		
		if (pPropList != null) {
			for (CeProperty thisProp : pPropList) {
				CeStoreJsonObject jObj = generateFullDetailsJsonFor(thisProp);
				addObjectValueTo(jProps, jObj);
			}
		}
		
		return jProps;
	}

	public CeStoreJsonArray generateSummaryListJsonFor(Collection<CeProperty> pPropList) {
		CeStoreJsonArray jProps = new CeStoreJsonArray();

		if (pPropList != null) {
			for (CeProperty thisProp : pPropList) {
				CeStoreJsonObject jObj = generateSummaryDetailsJsonFor(thisProp);
				addObjectValueTo(jProps, jObj);
			}
		}

		return jProps;
	}

	private void addMetamodelInstanceFor(CeProperty pProp, CeStoreJsonObject pJsonObj) {
		CeInstance mmInst = pProp.getMetaModelInstance(this.ac);

		if (mmInst != null) {
			CeWebInstance webInst = new CeWebInstance(this.ac);
			CeStoreJsonObject metaModelInstanceJSON = webInst.generateSummaryDetailsJsonFor(mmInst, null, 0, false, false, null, false);
			putObjectValueIn(pJsonObj, KEY_META_INSTANCE, metaModelInstanceJSON);
		} else {
			reportWarning("No meta-model instance was found for property named '" + pProp.formattedFullPropertyName() + "'", this.ac);
		}
	}

}