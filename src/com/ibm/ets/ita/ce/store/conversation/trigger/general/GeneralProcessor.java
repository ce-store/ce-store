package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public class GeneralProcessor {

    protected ActionContext ac = null;
    protected CardGenerator cg = null;

    // Test text for valid CE
    protected boolean isValidCe(String text) {
        StoreActions sa = StoreActions.createUsingDefaultConfig(ac);

        ContainerSentenceLoadResult result = sa.validateCeSentence(text);

        return (result.getInvalidSentenceCount() == 0) && (result.getValidSentenceCount() > 0);
    }
}
