package com.ibm.ets.ita.ce.store.client.web.model;

//ALL DONE (not messages)

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_CONMOD;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CONCEPTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CONCEPT_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CREATED;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_META_INSTANCE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROPERTIES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SENTENCES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SOURCES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SRC_IDS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STYLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_TYPE;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_FULL;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_SUMMARY;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.Collection;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSource;

public class CeWebConceptualModel extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public CeWebConceptualModel(ActionContext pAc) {
		super(pAc);
	}

	public CeStoreJsonObject generateFullDetailsJsonFor(CeConceptualModel pCm) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		CeWebSentence webSen = new CeWebSentence(this.ac);

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_CONMOD);
		putStringValueIn(jObj, JSON_STYLE, STYLE_FULL);
		putStringValueIn(jObj, JSON_ID, pCm.getModelName());
		putLongValueIn(jObj, JSON_CREATED, pCm.getCreationDate());
		putIntValueIn(jObj, JSON_SEN_COUNT, pCm.countSentences());
		putArrayValueIn(jObj, JSON_SOURCES, processSourcesFor(pCm));
		putArrayValueIn(jObj, JSON_CONCEPTS, processConceptsFor(pCm));
		putArrayValueIn(jObj, JSON_PROPERTIES, processPropertiesFor(pCm));

		// add meta-model details
		addMetamodelInstanceFor(pCm, jObj);

		putArrayValueIn(jObj, JSON_SENTENCES, webSen.generateSummaryListFrom(pCm.getSentences()));

		return jObj;
	}

	public CeStoreJsonObject generateSummaryDetailsJsonFor(CeConceptualModel pCm) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_CONMOD);
		putStringValueIn(jObj, JSON_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, JSON_ID, pCm.getModelName());
		putLongValueIn(jObj, JSON_CREATED, pCm.getCreationDate());
		putIntValueIn(jObj, JSON_SEN_COUNT, pCm.countSentences());
		putAllStringValuesIn(jObj, JSON_SRC_IDS, pCm.getSourceIds());
		putAllStringValuesIn(jObj, JSON_CONCEPT_NAMES, pCm.getDefinedConceptNames());

		// add meta-model details
		addMetamodelInstanceFor(pCm, jObj);

		return jObj;
	}

	public CeStoreJsonObject generateMinimalDetailsJsonFor(CeConceptualModel pCm) {
		// TODO: Replace this with the actual minimal version
		return generateSummaryDetailsJsonFor(pCm);
	}

	public CeStoreJsonObject generateNormalisedDetailsJsonFor(CeConceptualModel pCm) {
		// TODO: Replace this with the actual normalised version
		return generateSummaryDetailsJsonFor(pCm);
	}

	public CeStoreJsonArray generateFullListJsonFor(Collection<CeConceptualModel> pCmList) {
		CeStoreJsonArray jCms = new CeStoreJsonArray();

		if (pCmList != null) {
			for (CeConceptualModel thisCm : pCmList) {
				jCms.add(generateFullDetailsJsonFor(thisCm));
			}
		}

		return jCms;
	}

	public CeStoreJsonArray generateSummaryListJsonFor(Collection<CeConceptualModel> pCmList) {
		CeStoreJsonArray jCms = new CeStoreJsonArray();

		if (pCmList != null) {
			for (CeConceptualModel thisCm : pCmList) {
				jCms.add(generateSummaryDetailsJsonFor(thisCm));
			}
		}

		return jCms;
	}

	public CeStoreJsonArray generateMinimalListJsonFor(Collection<CeConceptualModel> pCmList) {
		CeStoreJsonArray jCms = new CeStoreJsonArray();

		if (pCmList != null) {
			for (CeConceptualModel thisCm : pCmList) {
				jCms.add(generateMinimalDetailsJsonFor(thisCm));
			}
		}

		return jCms;
	}

	public CeStoreJsonArray generateNormalisedListJsonFor(Collection<CeConceptualModel> pCmList) {
		CeStoreJsonArray jCms = new CeStoreJsonArray();

		if (pCmList != null) {
			for (CeConceptualModel thisCm : pCmList) {
				jCms.add(generateNormalisedDetailsJsonFor(thisCm));
			}
		}

		return jCms;
	}

	private static CeStoreJsonArray processSourcesFor(CeConceptualModel pCm) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeSource thisSrc : pCm.getSources()) {
			jArr.add(CeWebSource.generateSummaryDetailsJsonFor(thisSrc));
		}

		return jArr;
	}

	private CeStoreJsonArray processConceptsFor(CeConceptualModel pCm) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		CeWebConcept webCon = new CeWebConcept(this.ac);

		for (CeConcept thisCon : pCm.getDefinedConcepts()) {
			jArr.add(webCon.generateSummaryDetailsJsonFor(thisCon));
		}

		return jArr;
	}

	private CeStoreJsonArray processPropertiesFor(CeConceptualModel pCm) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeProperty thisProp : pCm.getDefinedProperties()) {
			CeWebProperty propWeb = new CeWebProperty(this.ac);
			jArr.add(propWeb.generateSummaryDetailsJsonFor(thisProp));
		}

		return jArr;
	}

	private void addMetamodelInstanceFor(CeConceptualModel pCm, CeStoreJsonObject pJsonObj) {
		CeInstance mmInst = pCm.retrieveMetaModelInstance(this.ac);

		if (mmInst != null) {
			CeWebInstance webInst = new CeWebInstance(this.ac);
			CeStoreJsonObject metaModelInstanceJSON = webInst.generateSummaryDetailsJsonFor(mmInst, null, 0, false,
					false, null, false);
			putObjectValueIn(pJsonObj, JSON_META_INSTANCE, metaModelInstanceJSON);
		} else {
			reportWarning("No meta-model instance was found for conceptual model named '" + pCm.getModelName(),
					this.ac);
		}
	}

}
