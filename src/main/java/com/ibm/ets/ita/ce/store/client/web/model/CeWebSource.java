package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSONTYPE_SOURCE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_TYPE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STYLE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CREATED;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_FULL;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_SUMMARY;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PARENT_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CHILD_IDS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SEN_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_MOD_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SOURCE_TYPE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SOURCE_DETAIL;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_USER_INSTNAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_AGENT_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SENS;

import java.util.ArrayList;
import java.util.Collection;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;

public class CeWebSource extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public CeWebSource(ActionContext pAc) {
		super(pAc);
	}

	public static CeStoreJsonArray generateSummaryListFrom(Collection<CeSource> pSrcList) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		if (pSrcList != null) {
			for (CeSource thisSrc : pSrcList) {
				jInsts.add(generateSummaryDetailsJsonFor(thisSrc));
			}
		}

		return jInsts;
	}

	public static CeStoreJsonArray generateMinimalListFrom(Collection<CeSource> pSrcList) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		if (pSrcList != null) {
			for (CeSource thisSrc : pSrcList) {
				jInsts.add(generateMinimalDetailsJsonFor(thisSrc));
			}
		}

		return jInsts;
	}

	public static CeStoreJsonArray generateNormalisedListFrom(Collection<CeSource> pSrcList) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();

		if (pSrcList != null) {
			for (CeSource thisSrc : pSrcList) {
				jInsts.add(generateNormalisedDetailsJsonFor(thisSrc));
			}
		}

		return jInsts;
	}

	public CeStoreJsonArray generateFullListFrom(Collection<CeSource> pSrcList) {
		CeStoreJsonArray jInsts = new CeStoreJsonArray();
		
		if (pSrcList != null) {
			for (CeSource thisSrc : pSrcList) {
				jInsts.add(generateFullDetailsJsonFor(thisSrc));
			}
		}
		
		return jInsts;
	}

	public static CeStoreJsonObject generateSummaryDetailsJsonFor(CeSource pSrc) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_SOURCE);
		putStringValueIn(jObj, JSON_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, JSON_ID, pSrc.getId());
		putLongValueIn(jObj, JSON_CREATED, pSrc.getCreationDate());

		processAnnotations(pSrc, jObj);

		putStringValueIn(jObj, JSON_SOURCE_TYPE, pSrc.formattedType());
		putStringValueIn(jObj, JSON_SOURCE_DETAIL, pSrc.getDetail());
		putStringValueIn(jObj, JSON_USER_INSTNAME, pSrc.getUserInstanceName());
		putStringValueIn(jObj, JSON_AGENT_NAME, pSrc.getAgentInstanceName());

		CeSource parSrc = pSrc.getParentSource();
		if (parSrc != null) {
			putStringValueIn(jObj, JSON_PARENT_ID, parSrc.getId());
		}

		if (pSrc.hasChildSources()) {
			ArrayList<String> childIds = new ArrayList<String>();
			
			for (CeSource childSrc : pSrc.getChildSources()) {
				childIds.add(childSrc.getId());
			}

			putAllStringValuesIn(jObj, JSON_CHILD_IDS, childIds);
		}

		putIntValueIn(jObj, JSON_SEN_COUNT, pSrc.countPrimarySentences());
		putIntValueIn(jObj, JSON_MOD_COUNT, pSrc.getDefinedModels().length);
		//Sentences are not returned in summary mode
		
		return jObj;
	}

	public static CeStoreJsonObject generateMinimalDetailsJsonFor(CeSource pSrc) {
		//TODO: Replace this with the actual minimal version
		return generateSummaryDetailsJsonFor(pSrc);
	}

	public static CeStoreJsonObject generateNormalisedDetailsJsonFor(CeSource pSrc) {
		//TODO: Replace this with the actual normalised version
		return generateSummaryDetailsJsonFor(pSrc);
	}

	public CeStoreJsonObject generateFullDetailsJsonFor(CeSource pSrc) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		CeWebSentence webSen = new CeWebSentence(this.ac);

		putStringValueIn(jObj, JSON_TYPE, JSONTYPE_SOURCE);
		putStringValueIn(jObj, JSON_STYLE, STYLE_FULL);
		putStringValueIn(jObj, JSON_ID, pSrc.getId());
	    putLongValueIn(jObj, JSON_CREATED, pSrc.getCreationDate());

		processAnnotations(pSrc, jObj);

		putStringValueIn(jObj, JSON_SOURCE_TYPE, pSrc.formattedType());
		putStringValueIn(jObj, JSON_SOURCE_DETAIL, pSrc.getDetail());
		putStringValueIn(jObj, JSON_USER_INSTNAME, pSrc.getUserInstanceName());
		putStringValueIn(jObj, JSON_AGENT_NAME, pSrc.getAgentInstanceName());
		
		CeSource parSrc = pSrc.getParentSource();
		if (parSrc != null) {
			putStringValueIn(jObj, JSON_PARENT_ID, parSrc.getId());
		}

		if (pSrc.hasChildSources()) {
			ArrayList<String> childIds = new ArrayList<String>();
			
			for (CeSource childSrc : pSrc.getChildSources()) {
				childIds.add(childSrc.getId());
			}

			putAllStringValuesIn(jObj, JSON_CHILD_IDS, childIds);
		}

		putIntValueIn(jObj, JSON_SEN_COUNT, pSrc.countPrimarySentences());
		putIntValueIn(jObj, JSON_MOD_COUNT, pSrc.getDefinedModels().length);
		putArrayValueIn(jObj, JSON_SENS, webSen.generateSummaryListFrom(new ArrayList<CeSentence>(pSrc.listAllSentences())));

		return jObj;
	}

}
