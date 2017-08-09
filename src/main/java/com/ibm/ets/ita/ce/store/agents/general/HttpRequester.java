package com.ibm.ets.ita.ce.store.agents.general;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.agents.CeNotifyHandler;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.utilities.FileUtilities;

import static com.ibm.ets.ita.ce.store.names.MiscNames.TRIGTYPE_CON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

public class HttpRequester extends CeNotifyHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";
	
	private static final String CLASS_NAME = HttpRequester.class.getName();
	private static final String PACKAGE_NAME = HttpRequester.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final String PROP_CONNAME = "concept name";
	private static final String PROP_TGTURL = "target url";

    @Override
    public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId, String pRuleOrQuery, String pRuleOrQueryName) {
    	
    	if (pThingType.equals(TRIGTYPE_CON)) {
            CeInstance trigger = pAc.getModelBuilder().getInstanceNamed(pAc, pTriggerName);
            String conName = trigger.getSingleValueFromPropertyNamed(PROP_CONNAME);

            if (conName.equalsIgnoreCase(pThingName)) {
//	            System.out.println(trigger.getInstanceName() + "(" + pSourceId + ") " + pThingName);

	            doHttpRequestProcessing(pAc, trigger);
            }
    	}
    }

    private void doHttpRequestProcessing(ActionContext pAc, CeInstance pTrigger) {
        for (CeInstance thisInst : pAc.getSessionCreations().getNewInstances()) {
        	sendHttpRequestFor(pAc, pTrigger, thisInst);
        }
    }

    private void sendHttpRequestFor(ActionContext pAc, CeInstance pTriggerInst, CeInstance pTargetInst) {
		final String METHOD_NAME = "sendHttpRequestFor";
    	String tgtUrl = pTriggerInst.getSingleValueFromPropertyNamed(PROP_TGTURL);

    	if ((tgtUrl == null) || (tgtUrl.isEmpty())) {
    		reportError("Cannot send triggered HTTP request - no target url is specified", pAc);
    	} else {
    		CeWebInstance instWeb = new CeWebInstance(pAc);
    		CeStoreJsonObject jObj = instWeb.generateFullDetailsJsonFor(pTargetInst, null, 0, false, false, null, false, false);
        	TreeMap<String, String> urlParms = new TreeMap<String, String>();

    		try {
    			urlParms.put("instance", jObj.serialize(pAc));
    		} catch (IOException e) {
    			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
    		}

    		FileUtilities.sendHttpPostRequest(pAc, tgtUrl, urlParms, false);
    	}

    }

}
