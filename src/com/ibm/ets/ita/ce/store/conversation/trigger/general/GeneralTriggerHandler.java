package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.agent.CeNotifyHandler;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public abstract class GeneralTriggerHandler extends CeNotifyHandler {

    protected static final String TYPE_PROP = "PROPERTY";

    private static final String CON_SERVICE = "service";
    protected static final String CON_NL_CARD = "nl card";
    protected static final String CON_TELL_CARD = "tell card";

    private static final String PROP_IRT = "is in reply to";
    private static final String PROP_FROM = "is from";
    private static final String PROP_TO = "is to";
    private static final String PROP_FROM_CON = "from concept";
    private static final String PROP_FROM_INST = "from instance";
    private static final String PROP_TELL_SERV = "tell service";

    protected ActionContext ac = null;
    protected CeInstance trigInst = null;
    protected CeInstance fromInst = null;
    protected String fromConName = null;
    protected String fromInstName = null;
    protected String tellService = null;

    protected void initialise(ActionContext ac) {
        this.ac = ac;
    }

    // Extract trigger details from property values in CE instance
    protected void extractTriggerDetailsUsing(String triggerName) {
        trigInst = ac.getModelBuilder().getInstanceNamed(ac, triggerName);

        if (this.trigInst != null) {
            fromConName = trigInst.getSingleValueFromPropertyNamed(PROP_FROM_CON);
            fromInst = trigInst.getSingleInstanceFromPropertyNamed(ac, PROP_FROM_INST);
            tellService = trigInst.getSingleValueFromPropertyNamed(PROP_TELL_SERV);

            if (fromConName != null) {
                fromConName = CON_SERVICE;
            }

            if (fromInst != null) {
                fromInstName = fromInst.getInstanceName();
            }

            System.out.println("From: " + fromInstName);
            System.out.println("Tell Service: " + tellService);
        } else {
            reportError("Unable to get trigger details for: " + triggerName, ac);
        }
    }

    protected boolean isCardAlreadyProcessed(CeInstance cardInst) {
        // The passed card instance is already processed if it has another card linked via
        // the "is in reply to" property, and that card has the user name of this agent
        // in the "is from" property.

        // More succinctly:
        // This card has already been processed if any other card from this agent is already
        // in reply to it.
        boolean result = false;

        CePropertyInstance irtPI = cardInst.getReferringPropertyInstanceNamed(PROP_IRT);

        if (irtPI != null) {
            CeInstance replier = irtPI.getRelatedInstance().getSingleInstanceFromPropertyNamed(ac, PROP_FROM);

            if (replier != null) {
                result = replier.equals(fromInst);
            }
        }

        return result;
    }

    protected boolean isThisCardForMe(CeInstance cardInst) {
        //This card is for "me" (this agent) if the card has the name of this
        //agent in the "is to" property.
        CePropertyInstance tgtPI = cardInst.getPropertyInstanceNamed(PROP_TO);
        boolean result = false;

        if (tgtPI != null) {
            result = tgtPI.hasValue(fromInstName);
        }

        return result;
    }

    public String getFromInstName() {
        return fromInstName;
    }

    public String getTellServiceName() {
        return tellService;
    }
}
