package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeSource;

public class CeGenerator {

    private static final String UID_PREFIX = "msg_";
    private static final String SRC_PREFIX = "conv_";

    private ActionContext ac;

    public CeGenerator(ActionContext ac) {
        this.ac = ac;
    }

    // Save CE to store
    public void save(String ceSentence, String source) {
        StoreActions sa = StoreActions.createUsingDefaultConfig(ac);

        CeSource tgtSrc = CeSource.createNewFormSource(ac, source, source);
        sa.saveCeText(ceSentence, tgtSrc);
    }

    public String generateNewUid() {
        return ac.getModelBuilder().getNextUid(ac, UID_PREFIX);
    }

    public String generateSrcName(String sender) {
        return SRC_PREFIX + sender;
    }
}
