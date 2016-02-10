package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collection;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.HelperConcept;

public class CeWebConcept extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//JSON Response Keys
	private static final String KEY_COUNT = "instance_count";
	private static final String KEY_ICON = "icon";
	private static final String KEY_MODELS = "conceptual_models";
	private static final String KEY_MODEL_NAMES = "conceptual_model_names";
	private static final String KEY_DIRPARENT_NAMES = "direct_parent_names";
	private static final String KEY_ALLPARENT_NAMES = "all_parent_names";
	private static final String KEY_ALLCHILD_NAMES = "all_child_names";
	private static final String KEY_CHILD_NAMES = "direct_child_names";		
	private static final String KEY_DIR_PROPERTY_NAMES = "direct_property_names";		
	private static final String KEY_INH_PROPERTY_NAMES = "inherited_property_names";		
	private static final String KEY_DIR_PARENTS = "direct_parents";
	private static final String KEY_DIR_CHILDREN = "direct_children";
	private static final String KEY_PRISEN_COUNT = "primary_sentence_count";
	private static final String KEY_SECSEN_COUNT = "secondary_sentence_count";
	private static final String KEY_PRI_SENS = "primary_sentences";
	private static final String KEY_SEC_SENS = "secondary_sentences";
	private static final String KEY_DIR_PROPERTIES = "direct_properties";
	private static final String KEY_INH_PROPERTIES = "inherited_properties";

	private static final String TYPE_CON = "concept";

	public CeWebConcept(ActionContext pAc) {
		super(pAc);
	}

	//Concept full response structure:
	//	KEY_CONCEPT_NAME (String)
	//	KEY_CONCEPT_CRDATE (Long)
	//	KEY_COUNT (Int)
	//	KEY_ICON (String)
	//	KEY_PRISEN_COUNT (Int)
	//	KEY_SECSEN_COUNT (Int)
	//	KEY_ANNOTATIONS (Array<String>)
	//	KEY_PROPERTIES (Array) -> [CeWebProperty:generateEmbeddedJsonFor]
	public CeStoreJsonObject generateFullDetailsJsonFor(CeConcept pConcept) {
		CeStoreJsonObject jObj = null;
//		CeWebProperty webProp = new CeWebProperty(this.ac);

		jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, KEY_TYPE, TYPE_CON);
		putStringValueIn(jObj, KEY_STYLE, STYLE_FULL);
		putStringValueIn(jObj, KEY_ID, pConcept.getConceptName());
		putLongValueIn(jObj, KEY_CREATED, pConcept.getCreationDate());
		putBooleanValueIn(jObj, KEY_SHADOW, pConcept.isShadowEntity());

		processAnnotations(pConcept, jObj);

		putIntValueIn(jObj, KEY_COUNT, this.ac.getModelBuilder().getInstanceCountForConcept(pConcept));
		putIntValueIn(jObj, KEY_PRISEN_COUNT, pConcept.countPrimarySentences());
		putIntValueIn(jObj, KEY_SECSEN_COUNT, pConcept.countSecondarySentences());

		if (!HelperConcept.hasDefaultIcon(this.ac, pConcept)) {
			putStringValueIn(jObj, KEY_ICON, HelperConcept.generateIconName(this.ac, pConcept));
		}

		CeStoreJsonArray parentArray = processDirectParentsFor(pConcept);
		if (!parentArray.isEmpty()) {
			putArrayValueIn(jObj, KEY_DIR_PARENTS, parentArray);
		}

		if (pConcept.hasDirectParents()) {
			putAllStringValuesIn(jObj, KEY_DIRPARENT_NAMES, pConcept.calculateDirectParentNames());
		}

		if (pConcept.hasAnyParents()) {
			putAllStringValuesIn(jObj, KEY_ALLPARENT_NAMES, pConcept.calculateAllParentNames());
		}

		CeStoreJsonArray childArray = processDirectChildrenFor(pConcept);
		if (!childArray.isEmpty()) {
			putArrayValueIn(jObj, KEY_DIR_CHILDREN, childArray);
		}

		if (pConcept.hasDirectChildren()) {
			putAllStringValuesIn(jObj, KEY_CHILD_NAMES, pConcept.calculateDirectChildNames());
		}

		if (pConcept.hasAnyChildren()) {
			putAllStringValuesIn(jObj, KEY_ALLCHILD_NAMES, pConcept.calculateAllChildNames());
		}

		CeStoreJsonArray dPropArray = processDirectPropertiesFor(pConcept);
		if (!dPropArray.isEmpty()) {
			putArrayValueIn(jObj, KEY_DIR_PROPERTIES, dPropArray);
		}

		CeStoreJsonArray iPropArray = processInheritedPropertiesFor(pConcept);
		if (!iPropArray.isEmpty()) {
			putArrayValueIn(jObj, KEY_INH_PROPERTIES, iPropArray);
		}

        putArrayValueIn(jObj, KEY_MODELS, processConceptualModelsFor(pConcept));

        CeWebSentence webSen = new CeWebSentence(this.ac);
        putArrayValueIn(jObj, KEY_PRI_SENS, webSen.generateSummaryListFrom(getPrimarySentenceList(pConcept)));
        putArrayValueIn(jObj, KEY_SEC_SENS, webSen.generateSummaryListFrom(pConcept.listSecondarySentences()));

		// add meta-model details
		addMetamodelInstanceFor(pConcept, jObj);

		return jObj;
	}

	//Concept Summary response structure:
	//	KEY_CONCEPT_NAME
	//	KEY_CONCEPT_CRDATE
	//	KEY_COUNT
	//	KEY_ICON
	//	KEY_MODELS[]
	//	KEY_PARENT_NAMES[]
	//	KEY_CHILD_NAMES[]
	public CeStoreJsonObject generateSummaryDetailsJsonFor(CeConcept pConcept) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, KEY_TYPE, TYPE_CON);
		putStringValueIn(jObj, KEY_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, KEY_ID, pConcept.getConceptName());
		putLongValueIn(jObj, KEY_CREATED, pConcept.getCreationDate());
		putBooleanValueIn(jObj, KEY_SHADOW, pConcept.isShadowEntity());

		processAnnotations(pConcept, jObj);

		putIntValueIn(jObj, KEY_COUNT, this.ac.getModelBuilder().getInstanceCountForConcept(pConcept));
		putIntValueIn(jObj, KEY_PRISEN_COUNT, pConcept.countPrimarySentences());
		putIntValueIn(jObj, KEY_SECSEN_COUNT, pConcept.countSecondarySentences());

		if (!HelperConcept.hasDefaultIcon(this.ac, pConcept)) {
			putStringValueIn(jObj, KEY_ICON, HelperConcept.generateIconName(this.ac, pConcept));
		}

		if (pConcept.hasDirectParents()) {
			putAllStringValuesIn(jObj, KEY_DIRPARENT_NAMES, pConcept.calculateDirectParentNames());
		}

		if (pConcept.hasAnyParents()) {
			putAllStringValuesIn(jObj, KEY_ALLPARENT_NAMES, pConcept.calculateAllParentNames());
		}

		if (pConcept.hasDirectChildren()) {
			putAllStringValuesIn(jObj, KEY_CHILD_NAMES, pConcept.calculateDirectChildNames());
		}

		if (pConcept.hasAnyChildren()) {
			putAllStringValuesIn(jObj, KEY_ALLCHILD_NAMES, pConcept.calculateAllChildNames());
		}

		if (pConcept.hasDirectProperties()) {
			putAllStringValuesIn(jObj, KEY_DIR_PROPERTY_NAMES, pConcept.calculateDirectPropertyNames());
		}

		if (pConcept.hasInferredProperties()) {
			putAllStringValuesIn(jObj, KEY_INH_PROPERTY_NAMES, pConcept.calculateInheritedPropertyNames());
		}

		putAllStringValuesIn(jObj, KEY_MODEL_NAMES, HelperConcept.listConceptualModelNames(pConcept));

		// add meta-model details
		addMetamodelInstanceFor(pConcept, jObj);

		return jObj;
	}
	
	private void addMetamodelInstanceFor(CeConcept pCon, CeStoreJsonObject pJsonObj) {
		CeInstance mmInst = pCon.retrieveMetaModelInstance(this.ac);
		
		if (mmInst != null) {
			CeWebInstance webInst = new CeWebInstance(this.ac);
			CeStoreJsonObject metaModelInstanceJSON = webInst.generateSummaryDetailsJsonFor(mmInst, 0, false, false, null);
			putObjectValueIn(pJsonObj, KEY_META_INSTANCE, metaModelInstanceJSON);
		} else {
			reportWarning("No meta-model instance was found for concept named '" + pCon.getConceptName() + "' - this might be a shadow concept", this.ac);
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