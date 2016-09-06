package com.ibm.ets.ita.ce.store.agents;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportInfo;

import com.ibm.ets.ita.ce.store.ActionContext;

public class ExampleNotifyHandler extends CeNotifyHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	@Override
	public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId, String pRuleOrQuery, String pRuleOrQueryName) {
		reportInfo("Trigger notification received (by example Notify Handler):", pAc);
		reportInfo("  Thing type = '" + pThingType + "'", pAc);
		reportInfo("  Thing name = '" + pThingName + "'", pAc);
		reportInfo("  Trigger name = '" + pTriggerName + "'", pAc);
		reportInfo("  Source id = '" + pSourceId + "'", pAc);
		reportInfo("  Rule or query = '" + pRuleOrQuery + "'", pAc);
		reportInfo("  Rule or query name = '" + pRuleOrQueryName + "'\n", pAc);
	}
}