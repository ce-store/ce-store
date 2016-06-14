package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeConceptualModel extends CeModelEntity {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = CeConceptualModel.class.getName();
	private static final String PACKAGE_NAME = CeConceptualModel.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	//TODO: Need to populate primary sentences for this when creating (via meta-model)

	private CeConcept[] definedConcepts = new CeConcept[0];
	private CeProperty[] definedProperties = new CeProperty[0];
	private CeSource[] sources = new CeSource[0];

	public static CeConceptualModel createConceptualModel(ActionContext pAc, String pModelName, CeSource pSrc) {
		CeConceptualModel thisCm = new CeConceptualModel();

		thisCm.name = pModelName;
		thisCm.addSource(pSrc);
		pSrc.addDefinedModel(thisCm);

		pAc.getModelBuilder().saveConceptualModel(thisCm);

		return thisCm;
	}

	public void addDefinedConceptByName(ActionContext pAc, String pConName) {
		CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc,  pConName);

		if (tgtCon != null) {
			addDefinedConcept(tgtCon);

			//Also add the conceptual model to the concept
			tgtCon.addConceptualModel(this);
		} else {
			reportWarning("Unable to locate concept named '' during meta-model (conceptual model) generation", pAc);
		}
	}

	@Override
	public HashSet<CeSentence> listAllSentences() {
		HashSet<CeSentence> result = new HashSet<CeSentence>();

		for (CeSentence priSen : getPrimarySentences()) {
			result.add(priSen);
		}

		return result;
	}

	@Override
	public ArrayList<CeSentence> listSecondarySentences() {
		return new ArrayList<CeSentence>();
	}

	@Override
	public ArrayList<ArrayList<CeSentence>> listAllSentencesAsPair() {
		ArrayList<ArrayList<CeSentence>> senListPair = new ArrayList<ArrayList<CeSentence>>();

		senListPair.add(listPrimarySentences());
		senListPair.add(listSecondarySentences());

		return senListPair;
	}

	public String getModelName() {
		return this.name;
	}

	public CeConcept[] getDefinedConcepts() {
		return this.definedConcepts;
	}

	public ArrayList<CeConcept> listDefinedConcepts() {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();
		
		for (CeConcept thisCon : getDefinedConcepts()) {
			result.add(thisCon);
		}
		return result;
	}

	public void addDefinedConcept(CeConcept pCon) {
	  final String METHOD_NAME = "addDefinedConcept";
	  
		if (!hasDefinedConcept(pCon)) {
			int currLen = this.definedConcepts.length;
			CeConcept[] newArray = new CeConcept[currLen + 1];
			System.arraycopy(this.definedConcepts, 0, newArray, 0, currLen);
			newArray[currLen] = pCon;
			this.definedConcepts = newArray;
			logger.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, this + " -> " + pCon);
		}
	}
	
	public boolean hasDefinedConcept(CeConcept pCon) {
		boolean result = false;
		
		for (CeConcept defCon : this.definedConcepts) {
			if (!result) {
				if (defCon == pCon) {
					result = true;
				}
			}
		}
		
		return result;
	}

	public ArrayList<String> getDefinedConceptNames() {
		ArrayList<String> result = new ArrayList<String>();
		
		for (CeConcept thisCon : getDefinedConcepts()) {
			result.add(thisCon.getConceptName());
		}
		
		return result;
	}

	public CeProperty[] getDefinedProperties() {
		return this.definedProperties;
	}

	public ArrayList<CeProperty> listDefinedProperties() {
		ArrayList<CeProperty> result = new ArrayList<CeProperty>();
		
		for (CeProperty thisProp : getDefinedProperties()) {
			result.add(thisProp);
		}
		return result;
	}

	public void addDefinedProperty(CeProperty pProperty) {
		if (!hasDefinedProperty(pProperty)) {
			int currLen = 0;
	
			currLen = this.definedProperties.length;
			CeProperty[] newArray = new CeProperty[currLen + 1];
			System.arraycopy(this.definedProperties, 0, newArray, 0, currLen);
			this.definedProperties = newArray;
	
			this.definedProperties[currLen] = pProperty;
		}
	}

	public boolean hasDefinedProperty(CeProperty pProp) {
		boolean result = false;
	
		for (CeProperty defProp : this.definedProperties) {
			if (!result) {
				if (defProp == pProp) {
					result = true;
				}
			}
		}

		return result;
	}

	public CeSource[] getSources() {
		return this.sources;
	}

	public ArrayList<CeSource> listSources() {
		ArrayList<CeSource> result = new ArrayList<CeSource>();

		for (CeSource thisSrc : getSources()) {
			result.add(thisSrc);
		}

		return result;
	}

	public void addSource(CeSource pSrc) {
		if (!hasSource(pSrc)) {
			int currLen = 0;

			currLen = this.sources.length;
			CeSource[] newArray = new CeSource[currLen + 1];
			System.arraycopy(this.sources, 0, newArray, 0, currLen);
			this.sources = newArray;
	
			this.sources[currLen] = pSrc;
		}
	}

	public boolean hasSource(CeSource pSrc) {
		boolean result = false;

		for (CeSource thisSrc : this.sources) {
			if (!result) {
				if (thisSrc == pSrc) {
					result = true;
				}
			}
		}

		return result;
	}

	public ArrayList<String> getSourceIds() {
		ArrayList<String> result = new ArrayList<String>();

		for (CeSource thisSrc : getSources()) {
			result.add(thisSrc.getId());
		}

		return result;
	}

	public int countSentences() {
		int result = 0;

		for (CeSource thisSrc : getSources()) {
			result += thisSrc.countPrimarySentences();
		}

		return result;
	}

	public ArrayList<CeSentence> getSentences() {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();

		for (CeSource thisSrc : getSources()) {
			for (CeSentence thisSen : thisSrc.getPrimarySentences()) {
				result.add(thisSen);
			}
		}

		return result;
	}

	public CeInstance retrieveMetaModelInstance(ActionContext pAc){
		return pAc.getModelBuilder().getInstanceNamed(pAc, getModelName());
	}

}