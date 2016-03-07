package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;

import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeConcept;
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
        System.out.println("Save CE");
        StoreActions sa = StoreActions.createUsingDefaultConfig(ac);

        System.out.println("Source: " + source);
        CeSource tgtSrc = CeSource.createNewFormSource(ac, source, source);
        System.out.println("Created source");

        sa.saveCeText(ceSentence, tgtSrc);
    }

    public String generateNewUid() {
        return ac.getModelBuilder().getNextUid(ac, UID_PREFIX);
    }

    public String generateSrcName(String sender) {
        return SRC_PREFIX + sender;
    }

    public String generateInterestingThing(CeConcept concept) {
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "conceptualise a ~ %CONCEPT_NAME% ~ C that");
        appendToSb(sb, "  is an interesting thing.");

        ceParms.put("%CONCEPT_NAME%", concept.getConceptName());

        String ceSentence = substituteCeParameters(sb.toString(), ceParms);
        return ceSentence;
    }
}
