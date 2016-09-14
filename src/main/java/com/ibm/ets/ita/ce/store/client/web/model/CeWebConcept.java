package com.ibm.ets.ita.ce.store.client.web.model;

//ALL DONE (not messages)

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_CON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ALLCHILD_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ALLPARENT_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CHILD_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CONMODELS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CREATED;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_DIRPARENT_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_DIR_CHILDREN;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_DIR_PARENTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_DIR_PROPERTIES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_DIR_PROPERTY_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ICON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INH_PROPERTIES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INH_PROPERTY_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTCOUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_META_INSTANCE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_MODEL_NAMES;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PRISEN_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PRI_SENS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SECSEN_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEC_SENS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SHADOW;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STYLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_TYPE;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_FULL;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_SUMMARY;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collection;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.HelperConcept;

public class CeWebConcept extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public CeWebConcept(ActionContext pAc) {
		super(pAc);
	}

	public CeStoreJsonObject generateFullDetailsJsonFor(CeConcept pConcept) {
		CeStoreJsonObject jObj = null;

		jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_CON);
		putStringValueIn(jObj, JSON_STYLE, STYLE_FULL);
		putStringValueIn(jObj, JSON_ID, pConcept.getConceptName());
		putLongValueIn(jObj, JSON_CREATED, pConcept.getCreationDate());
		putBooleanValueIn(jObj, JSON_SHADOW, pConcept.isShadowEntity());

		processAnnotations(pConcept, jObj);

		putIntValueIn(jObj, JSON_INSTCOUNT, this.ac.getModelBuilder().getInstanceCountForConcept(pConcept));
		putIntValueIn(jObj, JSON_PRISEN_COUNT, pConcept.countPrimarySentences());
		putIntValueIn(jObj, JSON_SECSEN_COUNT, pConcept.countSecondarySentences());

		if (!HelperConcept.hasDefaultIcon(this.ac, pConcept)) {
			putStringValueIn(jObj, JSON_ICON, HelperConcept.generateIconName(this.ac, pConcept));
		}

		CeStoreJsonArray parentArray = processDirectParentsFor(pConcept);
		if (!parentArray.isEmpty()) {
			putArrayValueIn(jObj, JSON_DIR_PARENTS, parentArray);
		}

		if (pConcept.hasDirectParents()) {
			putAllStringValuesIn(jObj, JSON_DIRPARENT_NAMES, pConcept.calculateDirectParentNames());
		}

		if (pConcept.hasAnyParents()) {
			putAllStringValuesIn(jObj, JSON_ALLPARENT_NAMES, pConcept.calculateAllParentNames());
		}

		CeStoreJsonArray childArray = processDirectChildrenFor(pConcept);
		if (!childArray.isEmpty()) {
			putArrayValueIn(jObj, JSON_DIR_CHILDREN, childArray);
		}

		if (pConcept.hasDirectChildren()) {
			putAllStringValuesIn(jObj, JSON_CHILD_NAMES, pConcept.calculateDirectChildNames());
		}

		if (pConcept.hasAnyChildren()) {
			putAllStringValuesIn(jObj, JSON_ALLCHILD_NAMES, pConcept.calculateAllChildNames());
		}

		CeStoreJsonArray dPropArray = processDirectPropertiesFor(pConcept);
		if (!dPropArray.isEmpty()) {
			putArrayValueIn(jObj, JSON_DIR_PROPERTIES, dPropArray);
		}

		CeStoreJsonArray iPropArray = processInheritedPropertiesFor(pConcept);
		if (!iPropArray.isEmpty()) {
			putArrayValueIn(jObj, JSON_INH_PROPERTIES, iPropArray);
		}

		putArrayValueIn(jObj, JSON_CONMODELS, processConceptualModelsFor(pConcept));

		CeWebSentence webSen = new CeWebSentence(this.ac);
		putArrayValueIn(jObj, JSON_PRI_SENS, webSen.generateSummaryListFrom(getPrimarySentenceList(pConcept)));
		putArrayValueIn(jObj, JSON_SEC_SENS, webSen.generateSummaryListFrom(pConcept.listSecondarySentences()));

		// add meta-model details
		addMetamodelInstanceFor(pConcept, jObj);

		return jObj;
	}

	public CeStoreJsonObject generateSummaryDetailsJsonFor(CeConcept pConcept) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_CON);
		putStringValueIn(jObj, JSON_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, JSON_ID, pConcept.getConceptName());
		putLongValueIn(jObj, JSON_CREATED, pConcept.getCreationDate());
		putBooleanValueIn(jObj, JSON_SHADOW, pConcept.isShadowEntity());

		processAnnotations(pConcept, jObj);

		putIntValueIn(jObj, JSON_INSTCOUNT, this.ac.getModelBuilder().getInstanceCountForConcept(pConcept));
		putIntValueIn(jObj, JSON_PRISEN_COUNT, pConcept.countPrimarySentences());
		putIntValueIn(jObj, JSON_SECSEN_COUNT, pConcept.countSecondarySentences());

		if (!HelperConcept.hasDefaultIcon(this.ac, pConcept)) {
			putStringValueIn(jObj, JSON_ICON, HelperConcept.generateIconName(this.ac, pConcept));
		}

		if (pConcept.hasDirectParents()) {
			putAllStringValuesIn(jObj, JSON_DIRPARENT_NAMES, pConcept.calculateDirectParentNames());
		}

		if (pConcept.hasAnyParents()) {
			putAllStringValuesIn(jObj, JSON_ALLPARENT_NAMES, pConcept.calculateAllParentNames());
		}

		if (pConcept.hasDirectChildren()) {
			putAllStringValuesIn(jObj, JSON_CHILD_NAMES, pConcept.calculateDirectChildNames());
		}

		if (pConcept.hasAnyChildren()) {
			putAllStringValuesIn(jObj, JSON_ALLCHILD_NAMES, pConcept.calculateAllChildNames());
		}

		if (pConcept.hasDirectProperties()) {
			putAllStringValuesIn(jObj, JSON_DIR_PROPERTY_NAMES, pConcept.calculateDirectPropertyNames());
		}

		if (pConcept.hasInferredProperties()) {
			putAllStringValuesIn(jObj, JSON_INH_PROPERTY_NAMES, pConcept.calculateInheritedPropertyNames());
		}

		putAllStringValuesIn(jObj, JSON_MODEL_NAMES, HelperConcept.listConceptualModelNames(pConcept));

		// add meta-model details
		addMetamodelInstanceFor(pConcept, jObj);

		return jObj;
	}

	public CeStoreJsonObject generateMinimalDetailsJsonFor(CeConcept pConcept) {
		// TODO: Replace this with the actual minimal version
		return generateSummaryDetailsJsonFor(pConcept);
	}

	public CeStoreJsonObject generateNormalisedDetailsJsonFor(CeConcept pConcept) {
		// TODO: Replace this with the actual normalised version
		return generateSummaryDetailsJsonFor(pConcept);
	}

	private void addMetamodelInstanceFor(CeConcept pCon, CeStoreJsonObject pJsonObj) {
		CeInstance mmInst = pCon.retrieveMetaModelInstance(this.ac);

		if (mmInst != null) {
			CeWebInstance webInst = new CeWebInstance(this.ac);
			CeStoreJsonObject metaModelInstanceJSON = webInst.generateSummaryDetailsJsonFor(mmInst, null, 0, false,
					false, null, false);
			putObjectValueIn(pJsonObj, JSON_META_INSTANCE, metaModelInstanceJSON);
		} else {
			reportWarning("No meta-model instance was found for concept named '" + pCon.getConceptName()
					+ "' - this might be a shadow concept", this.ac);
		}
	}

	public CeStoreJsonArray generateFullListJsonFor(Collection<CeConcept> pConList) {
		CeStoreJsonArray jConcepts = new CeStoreJsonArray();

		if (pConList != null) {
			for (CeConcept thisConcept : pConList) {
				jConcepts.add(generateFullDetailsJsonFor(thisConcept));
			}
		}

		return jConcepts;
	}

	public CeStoreJsonArray generateSummaryListJsonFor(Collection<CeConcept> pConList) {
		CeStoreJsonArray jConcepts = new CeStoreJsonArray();

		if (pConList != null) {
			for (CeConcept thisConcept : pConList) {
				jConcepts.add(generateSummaryDetailsJsonFor(thisConcept));
			}
		}

		return jConcepts;
	}

	public CeStoreJsonArray generateMinimalListJsonFor(Collection<CeConcept> pConList) {
		CeStoreJsonArray jConcepts = new CeStoreJsonArray();

		if (pConList != null) {
			for (CeConcept thisConcept : pConList) {
				jConcepts.add(generateMinimalDetailsJsonFor(thisConcept));
			}
		}

		return jConcepts;
	}

	public CeStoreJsonArray generateNormalisedListJsonFor(Collection<CeConcept> pConList) {
		CeStoreJsonArray jConcepts = new CeStoreJsonArray();

		if (pConList != null) {
			for (CeConcept thisConcept : pConList) {
				jConcepts.add(generateNormalisedDetailsJsonFor(thisConcept));
			}
		}

		return jConcepts;
	}

	private static ArrayList<CeSentence> getPrimarySentenceList(CeConcept pConcept) {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();

		for (CeSentence thisSen : pConcept.getPrimarySentences()) {
			result.add(thisSen);
		}

		return result;
	}

	private CeStoreJsonArray processDirectParentsFor(CeConcept pConcept) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeConcept thisParent : pConcept.getDirectParents()) {
			jArr.add(generateSummaryDetailsJsonFor(thisParent));
		}

		return jArr;
	}

	private CeStoreJsonArray processDirectChildrenFor(CeConcept pConcept) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeConcept thisChild : pConcept.getDirectChildren()) {
			jArr.add(generateSummaryDetailsJsonFor(thisChild));
		}

		return jArr;
	}

	private CeStoreJsonArray processDirectPropertiesFor(CeConcept pConcept) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeProperty thisProp : pConcept.getDirectProperties()) {
			CeWebProperty propWeb = new CeWebProperty(this.ac);
			jArr.add(propWeb.generateSummaryDetailsJsonFor(thisProp));
		}

		return jArr;
	}

	private CeStoreJsonArray processInheritedPropertiesFor(CeConcept pConcept) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeProperty thisProp : pConcept.retrieveInferredProperties()) {
			CeWebProperty propWeb = new CeWebProperty(this.ac);
			jArr.add(propWeb.generateSummaryDetailsJsonFor(thisProp));
		}

		return jArr;
	}

	private CeStoreJsonArray processConceptualModelsFor(CeConcept pConcept) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		for (CeConceptualModel thisCm : pConcept.getConceptualModels()) {
			CeWebConceptualModel cmWeb = new CeWebConceptualModel(this.ac);
			jArr.add(cmWeb.generateSummaryDetailsJsonFor(thisCm));
		}

		return jArr;
	}

}
