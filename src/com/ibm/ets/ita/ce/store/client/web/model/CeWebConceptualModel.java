package com.ibm.ets.ita.ce.store.client.web.model;

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.Collection;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSource;

public class CeWebConceptualModel extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String TYPE_CM = "conceptual model";

	private static final String KEY_SEN_COUNT = "sentence_count";
	private static final String KEY_SRC_IDS = "source_ids";
	private static final String KEY_CONCEPT_NAMES = "concept_names";
	private static final String KEY_SOURCES = "sources";
	private static final String KEY_CONCEPTS = "concepts";
	private static final String KEY_PROPERTIES = "properties";
	private static final String KEY_SENTENCES = "sentences";

	public CeWebConceptualModel(ActionContext pAc) {
		super(pAc);
	}

	public CeStoreJsonObject generateFullDetailsJsonFor(CeConceptualModel pCm) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		CeWebSentence webSen = new CeWebSentence(this.ac);
		
		putStringValueIn(jObj, KEY_TYPE, TYPE_CM);
		putStringValueIn(jObj, KEY_STYLE, STYLE_FULL);
		putStringValueIn(jObj, KEY_ID, pCm.getModelName());
		putLongValueIn(jObj, KEY_CREATED, pCm.getCreationDate());
		putIntValueIn(jObj, KEY_SEN_COUNT, pCm.countSentences());
		putArrayValueIn(jObj, KEY_SOURCES, processSourcesFor(pCm));
		putArrayValueIn(jObj, KEY_CONCEPTS, processConceptsFor(pCm));
		putArrayValueIn(jObj, KEY_PROPERTIES, processPropertiesFor(pCm));
		
		// add meta-model details
		addMetamodelInstanceFor(pCm, jObj);

		putArrayValueIn(jObj, KEY_SENTENCES, webSen.generateSummaryListFrom(pCm.getSentences()));			

		return jObj;
	}

	public CeStoreJsonObject generateSummaryDetailsJsonFor(CeConceptualModel pCm) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, KEY_TYPE, TYPE_CM);
		putStringValueIn(jObj, KEY_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, KEY_ID, pCm.getModelName());
		putLongValueIn(jObj, KEY_CREATED, pCm.getCreationDate());
		putIntValueIn(jObj, KEY_SEN_COUNT, pCm.countSentences());
		putAllStringValuesIn(jObj, KEY_SRC_IDS, pCm.getSourceIds());
		putAllStringValuesIn(jObj, KEY_CONCEPT_NAMES, pCm.getDefinedConceptNames());

		// add meta-model details
		addMetamodelInstanceFor(pCm, jObj);

		return jObj;
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
			CeStoreJsonObject metaModelInstanceJSON = webInst.generateSummaryDetailsJsonFor(mmInst, 0, false, false, null, false);
			putObjectValueIn(pJsonObj, KEY_META_INSTANCE, metaModelInstanceJSON);
		} else {
			reportWarning("No meta-model instance was found for conceptual model named '" + pCm.getModelName(), this.ac);
		}
	}

}