package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_PROP;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ASS_DOMAIN_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CREATED;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_DOMAIN_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_META_INSTANCE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROP_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROP_STYLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROP_TYPE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RANGE_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STYLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_TYPE;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_FULL;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_SUMMARY;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.Collection;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class CeWebProperty extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public CeWebProperty(ActionContext pAc) {
		super(pAc);
	}

	public CeStoreJsonObject generateFullDetailsJsonFor(CeProperty pProp) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_PROP);
		putStringValueIn(jObj, JSON_STYLE, STYLE_FULL);
		putStringValueIn(jObj, JSON_ID, pProp.formattedFullPropertyName());
		putLongValueIn(jObj, JSON_CREATED, pProp.getCreationDate());
		putStringValueIn(jObj, JSON_PROP_NAME, pProp.getPropertyName());
		putStringValueIn(jObj, JSON_ASS_DOMAIN_NAME, pProp.calculateAssertedDomainConceptName());
		putStringValueIn(jObj, JSON_DOMAIN_NAME, pProp.calculateDomainConceptName());
		putStringValueIn(jObj, JSON_RANGE_NAME, pProp.getRangeConceptName());
		putStringValueIn(jObj, JSON_PROP_STYLE, pProp.formattedCeStyle());
		putStringValueIn(jObj, JSON_PROP_TYPE, pProp.formattedCeType());

		// add meta-model details
		addMetamodelInstanceFor(pProp, jObj);

		return jObj;
	}

	public CeStoreJsonObject generateSummaryDetailsJsonFor(CeProperty pProp) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_PROP);
		putStringValueIn(jObj, JSON_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, JSON_ID, pProp.formattedFullPropertyName());
		putLongValueIn(jObj, JSON_CREATED, pProp.getCreationDate());
		putStringValueIn(jObj, JSON_PROP_NAME, pProp.getPropertyName());
		putStringValueIn(jObj, JSON_ASS_DOMAIN_NAME, pProp.calculateAssertedDomainConceptName());
		putStringValueIn(jObj, JSON_DOMAIN_NAME, pProp.calculateDomainConceptName());
		putStringValueIn(jObj, JSON_RANGE_NAME, pProp.getRangeConceptName());
		putStringValueIn(jObj, JSON_PROP_STYLE, pProp.formattedCeStyle());
		putStringValueIn(jObj, JSON_PROP_TYPE, pProp.formattedCeType());

		// add meta-model details
		addMetamodelInstanceFor(pProp, jObj);

		return jObj;
	}

	public CeStoreJsonObject generateMinimalDetailsJsonFor(CeProperty pProp) {
		// TODO: Replace this with the actual minimal version
		return generateSummaryDetailsJsonFor(pProp);
	}

	public CeStoreJsonObject generateNormalisedDetailsJsonFor(CeProperty pProp) {
		// TODO: Replace this with the actual normalised version
		return generateSummaryDetailsJsonFor(pProp);
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

	public CeStoreJsonArray generateMinimalListJsonFor(Collection<CeProperty> pPropList) {
		CeStoreJsonArray jProps = new CeStoreJsonArray();

		if (pPropList != null) {
			for (CeProperty thisProp : pPropList) {
				CeStoreJsonObject jObj = generateMinimalDetailsJsonFor(thisProp);
				addObjectValueTo(jProps, jObj);
			}
		}

		return jProps;
	}

	public CeStoreJsonArray generateNormalisedListJsonFor(Collection<CeProperty> pPropList) {
		CeStoreJsonArray jProps = new CeStoreJsonArray();

		if (pPropList != null) {
			for (CeProperty thisProp : pPropList) {
				CeStoreJsonObject jObj = generateNormalisedDetailsJsonFor(thisProp);
				addObjectValueTo(jProps, jObj);
			}
		}

		return jProps;
	}

	private void addMetamodelInstanceFor(CeProperty pProp, CeStoreJsonObject pJsonObj) {
		CeInstance mmInst = pProp.getMetaModelInstance(this.ac);

		if (mmInst != null) {
			CeWebInstance webInst = new CeWebInstance(this.ac);
			CeStoreJsonObject metaModelInstanceJSON = webInst.generateSummaryDetailsJsonFor(mmInst, null, 0, false,
					false, null, false);
			putObjectValueIn(pJsonObj, JSON_META_INSTANCE, metaModelInstanceJSON);
		} else {
			reportWarning(
					"No meta-model instance was found for property named '" + pProp.formattedFullPropertyName() + "'",
					this.ac);
		}
	}

}
