package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.StaticCeRepository.NAME_CMGLOBAL;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeSource extends CeModelEntity {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//External types
	private static final int SRCTYPE_ID_UNKNOWN = -1;
	public static final int SRCTYPE_ID_URL = 1;
	public static final int SRCTYPE_ID_FILE = 2;
	public static final int SRCTYPE_ID_FORM = 3;
	public static final int SRCTYPE_ID_AGENT = 4;
	public static final int SRCTYPE_ID_INTERNAL = 5;
	public static final int SRCTYPE_ID_RULE = 6;
	public static final int SRCTYPE_ID_QUERY = 7;

	//Internal - readable names
	private static final String SRCTYPE_NAME_URL = "Url";
	private static final String SRCTYPE_NAME_FILE = "File";
	private static final String SRCTYPE_NAME_FORM = "Form";
	private static final String SRCTYPE_NAME_AGENT = "Agent";
	private static final String SRCTYPE_NAME_INTERNAL = "Internal";
	private static final String SRCTYPE_NAME_RULE = "Rule";
	private static final String SRCTYPE_NAME_QUERY = "Query";

	private static final String SRC_ID_PREFIX = "src_";

	private static AtomicLong sourceIdVal = new AtomicLong(0);

	private int type = SRCTYPE_ID_UNKNOWN;
	private String detail = null;
	private String userInstanceName = null;
	private String agentInstanceName = null;
	private CeSentence lastSentence = null;
	private CeSource parentSource = null;
	private CeSource[] childSources = new CeSource[0];

	private CeSentence[] annotationSentences = new CeSentence[0];

	private CeConceptualModel[] definedModels = new CeConceptualModel[0];

	//DSB 22/07/2013 - Relocated from ActionContext
	private HashSet<CeConcept> affectedConcepts = new HashSet<CeConcept>();
	private HashSet<CeProperty> affectedProperties = new HashSet<CeProperty>();
	private HashSet<CeRule> affectedRules = new HashSet<CeRule>();

	private CeSource() {
		//This is private to ensure that new instances can only be created via the various static methods		
	}

	public static void resetCounter() {
		sourceIdVal = new AtomicLong(0);
	}

	public static CeSource createNewUrlSource(ActionContext pAc, String pUrl, String pOptionalSrcId) {
		return createNewSourceUsing(pAc, SRCTYPE_ID_URL, pUrl, null, pOptionalSrcId);
	}

	public static CeSource createNewFileSource(ActionContext pAc, String pFilename, String pOptionalSrcId) {
		return createNewSourceUsing(pAc, SRCTYPE_ID_FILE, pFilename, null, pOptionalSrcId);
	}

	public static CeSource createNewFormSource(ActionContext pAc, String pFormName, String pOptionalSrcId) {
		return createNewSourceUsing(pAc, SRCTYPE_ID_FORM, pFormName, null, pOptionalSrcId);
	}

	public static CeSource createNewAgentSource(ActionContext pAc, String pAgentName, String pAgentInstanceName, String pOptionalSrcId) {
		return createNewSourceUsing(pAc, SRCTYPE_ID_AGENT, pAgentName, pAgentInstanceName, pOptionalSrcId);
	}	

	public static CeSource createNewInternalSource(ActionContext pAc, String pInternalName, String pOptionalSrcId) {
		return createNewSourceUsing(pAc, SRCTYPE_ID_INTERNAL, pInternalName, null, pOptionalSrcId);
	}

	public static CeSource createNewRuleSource(ActionContext pAc, String pRuleName, String pOptionalSrcId) {
		return createNewSourceUsing(pAc, SRCTYPE_ID_RULE, pRuleName, null, pOptionalSrcId);
	}

	public static CeSource createNewQuerySource(ActionContext pAc, String pQueryName, String pOptionalSrcId) {
		return createNewSourceUsing(pAc, SRCTYPE_ID_QUERY, pQueryName, null, pOptionalSrcId);
	}

	private static CeSource createNewSourceUsing(ActionContext pAc, int pType, String pDetail, String pAgentInstanceName, String pOptionalSrcId) {
		CeSource newSource = null;
		String newSrcId = null;
		
		if (pOptionalSrcId != null) {
			newSrcId = pOptionalSrcId;
		} else {
			newSrcId = SRC_ID_PREFIX + nextSourceIdValue();
		}

		//Try to get an existing source with this id
		newSource = pAc.getModelBuilder().getSourceById(newSrcId);

		if (newSource == null) {
			//The source does not exist already so it needs to be created
			newSource = new CeSource();
			newSource.name = pAc.getModelBuilder().getCachedStringValueLevel3(newSrcId);
			newSource.type = pType;
			newSource.detail = pAc.getModelBuilder().getCachedStringValueLevel3(pDetail);

			newSource.userInstanceName = pAc.getModelBuilder().getCachedStringValueLevel3(pAc.getUserName());

			if ((pAgentInstanceName == null) || (pAgentInstanceName.isEmpty())) {
				newSource.agentInstanceName = null;
			} else {
				newSource.agentInstanceName = pAc.getModelBuilder().getCachedStringValueLevel3(pAgentInstanceName);
			}

			pAc.getModelBuilder().saveSource(newSource);
		}

		pAc.setCurrentSource(newSource);
		
		return newSource;
	}

	public static void renumberThisSource(CeSource pSrc) {
		String nextSrcId = nextSourceIdValue();
		
		pSrc.name = SRC_ID_PREFIX + nextSrcId;
	}

	private static String nextSourceIdValue() {
		return String.format("%03d", new Long(sourceIdVal.incrementAndGet()));
	}

	public String getId() {
		return this.name;
	}

	public CeSource getParentSource() {
		return this.parentSource;
	}

	public void setParentSource(ActionContext pAc, CeSource pSrc) {
		if (pSrc != null) {
			if (this.equals(pSrc)) {
				reportError("Cannot set source as it's own parent: " + this.name, pAc);
			} else {
				this.parentSource = pSrc;
				pSrc.addChildSource(this);
			}
		}
	}

	public CeSource[] getChildSources() {
		return this.childSources;
	}

	public int countChildSources() {
		return this.childSources.length;
	}

	public void addChildSource(CeSource pSrc) {
		if (!hasChildSource(pSrc)) {
			int currLen = 0;

			currLen = this.childSources.length;
			CeSource[] newArray = new CeSource[currLen + 1];
			System.arraycopy(this.childSources, 0, newArray, 0, currLen);

			this.childSources = newArray;
			this.childSources[currLen] = pSrc;
		}
	}

	public void removeChildSource(CeSource pSrc) {
		if (hasChildSource(pSrc)) {
			CeSource[] newArray = new CeSource[this.childSources.length - 1];

			int ctr = 0;
			for (CeSource thisSrc : this.childSources) {
				if (thisSrc != pSrc) {
					newArray[ctr++] = thisSrc;
				}
			}

			this.childSources = newArray;
		}
	}

	public boolean hasChildSource(CeSource pSrc) {
		boolean result = false;

		for (CeSource thisSrc : this.childSources) {
			if (!result) {
				if (thisSrc == pSrc) {
					result = true;
				}
			}
		}

		return result;
	}

	public boolean hasChildSources() {
		return (getChildSources().length > 0);
	}

	public CeSentence[] getAnnotationSentences() {
		return this.annotationSentences;
	}

	@Override
	public int countAnnotationSentences() {
		return this.annotationSentences.length;
	}

	public ArrayList<CeSentence> listAnnotationSentences() {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();

		for (CeSentence thisSen : this.annotationSentences) {
			result.add(thisSen);
		}

		return result;
	}

	public void addAnnotationSentence(CeSentence pSen) {
		if (!hasAnnotationSentence(pSen)) {
			int currLen = 0;

			currLen = this.annotationSentences.length;
			CeSentence[] newArray = new CeSentence[currLen + 1];
			System.arraycopy(this.annotationSentences, 0, newArray, 0, currLen);

			this.annotationSentences = newArray;
			this.annotationSentences[currLen] = pSen;
		}
	}

	public void removeAnnotationSentence(CeSentence pSen) {
		if (hasAnnotationSentence(pSen)) {
			CeSentence[] newArray = new CeSentence[this.annotationSentences.length - 1];

			int ctr = 0;
			for (CeSentence annoSen : this.annotationSentences) {
				if (annoSen != pSen) {
					newArray[ctr++] = annoSen;
				}
			}

			this.annotationSentences = newArray;
		}
	}

	public boolean hasAnnotationSentence(CeSentence pSen) {
		boolean result = false;

		for (CeSentence annoSen : this.annotationSentences) {
			if (!result) {
				if (annoSen == pSen) {
					result = true;
				}
			}
		}

		return result;
	}

	public boolean hasAnnotationSentences() {
		return (getAnnotationSentences().length > 0);
	}

	public int getType() {
		return this.type;
	}

	public boolean isInternalSource() {
		return (this.type == SRCTYPE_ID_INTERNAL);
	}

	public String getDetail() {
		return this.detail;
	}

	public String getUserInstanceName() {
		return this.userInstanceName;
	}

	public String getAgentInstanceName() {
		return this.agentInstanceName;
	}

	public CeSentence getLastSentence() {
		return this.lastSentence;
	}

	@Override
	public synchronized void addPrimarySentence(CeSentence pSentence) {
		super.addPrimarySentence(pSentence);
		this.lastSentence = pSentence;
	}

	public CeConceptualModel[] getDefinedModels() {
		return this.definedModels;
	}

	public void addDefinedModel(CeConceptualModel pModel) {
		if (!hasDefinedModel(pModel)) {
			int currLen = 0;

			currLen = this.definedModels.length;
			CeConceptualModel[] newArray = new CeConceptualModel[currLen + 1];
			System.arraycopy(this.definedModels, 0, newArray, 0, currLen);
			this.definedModels = newArray;

			this.definedModels[currLen] = pModel;
		}
	}

	private boolean hasDefinedModel(CeConceptualModel pModel) {
		boolean result = false;

		for (CeConceptualModel defModel : this.definedModels) {
			if (!result) {
				if (defModel == pModel) {
					result = true;
				}
			}
		}

		return result;
	}

	public boolean hasAnyDefinedModel() {
		return (this.definedModels.length > 0);
	}
	
	public CeConceptualModel addDefaultConceptualModel(ActionContext pAc) {
		String cmName = NAME_CMGLOBAL;
		
		return CeConceptualModel.createConceptualModel(pAc, cmName, this);
	}

	public String formattedType() {
		String result = "";

		switch (this.type) {
		case SRCTYPE_ID_URL:
			result = SRCTYPE_NAME_URL;
			break;
		case SRCTYPE_ID_FILE:
			result = SRCTYPE_NAME_FILE;
			break;
		case SRCTYPE_ID_FORM:
			result = SRCTYPE_NAME_FORM;
			break;
		case SRCTYPE_ID_AGENT:
			result = SRCTYPE_NAME_AGENT;
			break;
		case SRCTYPE_ID_INTERNAL:
			result = SRCTYPE_NAME_INTERNAL;
			break;
		case SRCTYPE_ID_RULE:
			result = SRCTYPE_NAME_RULE;
			break;
		case SRCTYPE_ID_QUERY:
			result = SRCTYPE_NAME_QUERY;
			break;
		default:
			result = "(unknown type: " + Integer.toString(this.type) + ")";
			break;
		}

		return result;
	}

	@Override
	public String toString() {
		return "CeSource: " + formattedType() + " " + this.detail + " (" + this.name + ")";
	}

	@Override
	public HashSet<CeSentence> listAllSentences() {
		HashSet<CeSentence> result = new HashSet<CeSentence>();

		for (CeSentence thisSen : getPrimarySentences()) {
			result.add(thisSen);
		}

		for (CeSentence thisSen : getAnnotationSentences()) {
			result.add(thisSen);
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

	public HashSet<CeConcept> getAffectedConcepts() {
		return this.affectedConcepts;
	}

	public HashSet<CeProperty> getAffectedProperties() {
		return this.affectedProperties;
	}

	public HashSet<CeRule> getAffectedRules() {
		return this.affectedRules;
	}

	public void addAffectedConcept(CeConcept pConcept) {
		this.affectedConcepts.add(pConcept);
	}

	public void addAffectedProperty(CeProperty pProperty) {
		this.affectedProperties.add(pProperty);
	}

	public void addAffectedRule(CeRule pRule) {
		this.affectedRules.add(pRule);
	}

	public void clearNotifications() {
		this.affectedConcepts = new HashSet<CeConcept>();
		this.affectedProperties = new HashSet<CeProperty>();
		this.affectedRules = new HashSet<CeRule>();
	}

	public void debugAffectedConceptsAndProperties(ActionContext pAc) {
//		if (isReportDebug()) {
//			for (CeConcept affCon : this.affectedConcepts) {
//				reportDebug("Affected concept: " + affCon.getConceptName(), pAc);
//			}
//
//			for (CeProperty affProp : this.affectedProperties) {
//				reportDebug("Affected property: " + affProp.formattedFullPropertyName(), pAc);
//			}
//		}
	}

}