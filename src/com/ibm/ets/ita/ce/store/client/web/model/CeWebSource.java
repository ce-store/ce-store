package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;

public class CeWebSource extends CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//JSON Response Keys
	private static final String KEY_PARENT_ID = "parent_id";
	private static final String KEY_CHILD_IDS = "child_ids";
	private static final String KEY_SEN_COUNT = "sentence_count";
	private static final String KEY_MOD_COUNT = "model_count";
	private static final String KEY_SOURCE_TYPE = "source_type";
	private static final String KEY_SOURCE_DETAIL = "detail";
	private static final String KEY_USER_INSTNAME = "user_instname";
	private static final String KEY_AGENT_NAME = "agent_instname";
	private static final String KEY_SENS = "sentences";
	
	private static final String TYPE_SRC = "source";

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

		putStringValueIn(jObj, KEY_TYPE, TYPE_SRC);
		putStringValueIn(jObj, KEY_STYLE, STYLE_SUMMARY);
		putStringValueIn(jObj, KEY_ID, pSrc.getId());
	    putLongValueIn(jObj, KEY_CREATED, pSrc.getCreationDate());

		processAnnotations(pSrc, jObj);

		putStringValueIn(jObj, KEY_SOURCE_TYPE, pSrc.formattedType());
		putStringValueIn(jObj, KEY_SOURCE_DETAIL, pSrc.getDetail());
		putStringValueIn(jObj, KEY_USER_INSTNAME, pSrc.getUserInstanceName());
		putStringValueIn(jObj, KEY_AGENT_NAME, pSrc.getAgentInstanceName());

		CeSource parSrc = pSrc.getParentSource();
		if (parSrc != null) {
			putStringValueIn(jObj, KEY_PARENT_ID, parSrc.getId());
		}

		if (pSrc.hasChildSources()) {
			ArrayList<String> childIds = new ArrayList<String>();
			
			for (CeSource childSrc : pSrc.getChildSources()) {
				childIds.add(childSrc.getId());
			}

			putAllStringValuesIn(jObj, KEY_CHILD_IDS, childIds);
		}

		putIntValueIn(jObj, KEY_SEN_COUNT, pSrc.countPrimarySentences());
		putIntValueIn(jObj, KEY_MOD_COUNT, pSrc.getDefinedModels().length);
		//Sentences are not returned in summary mode
		
		return jObj;
	}

	public CeStoreJsonObject generateFullDetailsJsonFor(CeSource pSrc) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();
		CeWebSentence webSen = new CeWebSentence(this.ac);

		putStringValueIn(jObj, KEY_TYPE, TYPE_SRC);
		putStringValueIn(jObj, KEY_STYLE, STYLE_FULL);
		putStringValueIn(jObj, KEY_ID, pSrc.getId());
	    putLongValueIn(jObj, KEY_CREATED, pSrc.getCreationDate());

		processAnnotations(pSrc, jObj);

		putStringValueIn(jObj, KEY_SOURCE_TYPE, pSrc.formattedType());
		putStringValueIn(jObj, KEY_SOURCE_DETAIL, pSrc.getDetail());
		putStringValueIn(jObj, KEY_USER_INSTNAME, pSrc.getUserInstanceName());
		putStringValueIn(jObj, KEY_AGENT_NAME, pSrc.getAgentInstanceName());
		
		CeSource parSrc = pSrc.getParentSource();
		if (parSrc != null) {
			putStringValueIn(jObj, KEY_PARENT_ID, parSrc.getId());
		}

		if (pSrc.hasChildSources()) {
			ArrayList<String> childIds = new ArrayList<String>();
			
			for (CeSource childSrc : pSrc.getChildSources()) {
				childIds.add(childSrc.getId());
			}

			putAllStringValuesIn(jObj, KEY_CHILD_IDS, childIds);
		}

		putIntValueIn(jObj, KEY_SEN_COUNT, pSrc.countPrimarySentences());
		putIntValueIn(jObj, KEY_MOD_COUNT, pSrc.getDefinedModels().length);
		putArrayValueIn(jObj, KEY_SENS, webSen.generateSummaryListFrom(new ArrayList<CeSentence>(pSrc.listAllSentences())));

		return jObj;
	}

}