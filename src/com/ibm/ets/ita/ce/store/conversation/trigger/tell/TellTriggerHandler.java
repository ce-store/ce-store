package com.ibm.ets.ita.ce.store.conversation.trigger.tell;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.GeneralTriggerHandler;

public class TellTriggerHandler extends GeneralTriggerHandler {

	@Override
	public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId,
			String pRuleOrQuery, String pRuleOrQueryName) {
		System.out.println("Tell trigger notification received");
	}

}
