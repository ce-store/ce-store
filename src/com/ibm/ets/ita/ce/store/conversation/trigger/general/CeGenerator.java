package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
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

    public String generateInterestingConcept(CeConcept concept, String user) {
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "conceptualise a ~ %CONCEPT_NAME% ~ C that");
        appendToSb(sb, "  is an interesting thing.");

        ceParms.put("%CONCEPT_NAME%", concept.getConceptName());

        ArrayList<CeInstance> instances = ac.getModelBuilder().getAllInstancesForConceptNamed(ac, concept.getConceptName());

        for (CeInstance instance : instances) {
            appendToSb(sb, generateInterestingInstance(instance, user));
        }

        String ceSentence = substituteCeParameters(sb.toString(), ceParms);
        System.out.println("Sentence: " + ceSentence);
        return ceSentence;
    }

    public String generateInterestingInstance(CeInstance instance, String user) {
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> ceParms = new TreeMap<String, String>();

        appendToSb(sb, "the %CONCEPT_NAME% '%INSTANCE_NAME%'");
        appendToSb(sb, "  is an interesting thing.");
        appendToSb(sb, "the interesting thing '%INSTANCE_NAME%'");
        appendToSb(sb, "  has the CE user %USER% as interested party.");

        ceParms.put("%CONCEPT_NAME%", instance.getFirstLeafConceptName());
        ceParms.put("%INSTANCE_NAME%", instance.getInstanceName());
        ceParms.put("%USER%", user);

        String ceSentence = substituteCeParameters(sb.toString(), ceParms);
        return ceSentence;
    }
}
