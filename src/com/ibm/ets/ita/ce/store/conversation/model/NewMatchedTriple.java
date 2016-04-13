package com.ibm.ets.ita.ce.store.conversation.model;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class NewMatchedTriple {

    private CeConcept domain;
    private CeInstance domainInstance;
    private String domainName;
    private CeProperty property;
    private CeConcept range;
    private CeInstance rangeInstance;
    private String rangeName;

    // <instance:property:instance>
    public NewMatchedTriple (CeInstance domainInstance, CeProperty property, CeInstance rangeInstance) {
        this.domain = property.getDomainConcept();
        this.domainInstance = domainInstance;
        this.domainName = domainInstance.getInstanceName();
        this.property = property;
        this.range = property.getRangeConcept();
        this.rangeInstance = rangeInstance;
        this.rangeName = rangeInstance.getInstanceName();
    }

    // <concept:property:instance>
    public NewMatchedTriple (CeConcept domainConcept, CeProperty property, CeInstance rangeInstance, ActionContext ac) {
        this.domain = domainConcept;
        this.domainName = ac.getModelBuilder().getNextUid(ac, domain.getConceptName().substring(0, 1).toLowerCase());
        this.property = property;
        this.range = property.getRangeConcept();
        this.rangeInstance = rangeInstance;
        this.rangeName = rangeInstance.getInstanceName();
    }

    // <instance:property:concept>
    public NewMatchedTriple (CeInstance domainInstance, CeProperty property, CeConcept rangeConcept, ActionContext ac) {
        this.domain = property.getDomainConcept();
        this.domainInstance = domainInstance;
        this.domainName = domainInstance.getInstanceName();
        this.property = property;
        this.range = rangeConcept;
        this.rangeName = ac.getModelBuilder().getNextUid(ac, range.getConceptName().substring(0, 1).toLowerCase());
    }

    // <instance:property:value>
    public NewMatchedTriple (CeInstance domainInstance, CeProperty property, String rangeValue) {
        this.domain = property.getDomainConcept();
        this.domainInstance = domainInstance;
        this.domainName = domainInstance.getInstanceName();
        this.property = property;
        this.rangeName = rangeValue;
    }

    // <concept:MISSING:instance>
    public NewMatchedTriple (CeConcept domainConcept, CeInstance domainInstance) {
        this.domain = domainConcept;
        this.domainInstance = domainInstance;
        this.domainName = domainInstance.getInstanceName();
    }

    public CeConcept getDomain() {
        return domain;
    }

    public CeInstance getDomainInstance() {
        return domainInstance;
    }

    public String getDomainName() {
        return domainName;
    }

    public CeProperty getProperty() {
        return property;
    }

    public String getPropertyName() {
        if (property != null) {
            return property.getPropertyName();
        } else {
            return null;
        }
    }

    public CeConcept getRange() {
        return range;
    }

    public CeInstance getRangeInstance() {
        return rangeInstance;
    }

    public String getRangeName() {
        return rangeName;
    }

//    public String getSentence() {
//        StringBuilder sb = new StringBuilder();
//
//        appendToSbNoNl(sb, "the ");
//        appendToSbNoNl(sb, domain.getConceptName());
//        appendToSbNoNl(sb, " '");
//        appendToSbNoNl(sb, getDomainName());
//        appendToSbNoNl(sb, "' ");
//
//        if (property.isFunctionalNoun()) {
//            appendToSbNoNl(sb, "has the ");
//            appendToSbNoNl(sb, range.getConceptName());
//            appendToSbNoNl(sb, getRangeName());
//            appendToSbNoNl(sb, " as ");
//            appendToSbNoNl(sb, getPropertyName());
//            appendToSbNoNl(sb, ".");
//        } else {
//            appendToSbNoNl(sb, getPropertyName());
//            appendToSbNoNl(sb, "the ");
//            appendToSbNoNl(sb, range.getConceptName());
//            appendToSbNoNl(sb, " '");
//            appendToSbNoNl(sb, getRangeName());
//            appendToSbNoNl(sb, "'.");
//        }
//
////    	return "the " + domain.getConceptName() + " '" + getDomainName() + "' " + getPropertyName() + " the " + range.getConceptName() + " '" + getRangeName() + "'.";
//        return sb.toString();
//    }

    public ArrayList<String> getReferencedInstances() {
    	ArrayList<String> referencedInsts = new ArrayList<String>();
    	referencedInsts.add(getDomainName());
    	referencedInsts.add(getRangeName());
    	return referencedInsts;
    }

    @Override
    public String toString() {
        return "[MatchedTriple] (" + getDomain() + ") " + getDomainName() + ":" + getPropertyName() + ":" + "(" + getRange() + ") " + getRangeName();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof NewMatchedTriple) {
            NewMatchedTriple triple = (NewMatchedTriple) obj;
            result = true;

            if (domainInstance != null && triple.getDomainInstance() != null) {
                result = result && domainInstance.equals(triple.getDomainInstance());
            }
            if (domain != null && triple.getDomain() != null) {
                result = result && domain.equals(triple.getDomain());
            }
            if (property != null && triple.getProperty() != null) {
                result = result && property.equals(triple.getProperty());
            }
            if (range != null && triple.getRange() != null) {
                result = result && range.equals(triple.getRange());
            }
            if (rangeInstance != null && triple.getRangeInstance() != null) {
                result = result && rangeInstance.equals(triple.getRangeInstance());
            }
            if (rangeName != null && triple.getRangeName() != null) {
                result = result && rangeName.equals(triple.getRangeName());
            }
        }

        return result;
    }
}
