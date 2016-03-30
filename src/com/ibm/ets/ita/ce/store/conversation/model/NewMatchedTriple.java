package com.ibm.ets.ita.ce.store.conversation.model;

import java.util.ArrayList;

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
    public NewMatchedTriple (CeConcept domainConcept, String domainName, CeProperty property, CeInstance rangeInstance) {
        this.domain = domainConcept;
        this.domainName = domainName;
        this.property = property;
        this.range = property.getRangeConcept();
        this.rangeInstance = rangeInstance;
        this.rangeName = rangeInstance.getInstanceName();
    }

    // <instance:property:concept>
    public NewMatchedTriple (CeInstance domainInstance, CeProperty property, CeConcept rangeConcept, String rangeName) {
        this.domain = property.getDomainConcept();
        this.domainInstance = domainInstance;
        this.domainName = domainInstance.getInstanceName();
        this.property = property;
        this.range = rangeConcept;
        this.rangeName = rangeName;
    }

    public CeInstance getDomainInstance() {
        return domainInstance;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getPropertyName() {
    	return property.getPropertyName();
    }

    public CeInstance getRangeInstance() {
        return rangeInstance;
    }

    public String getRangeName() {
        return rangeName;
    }

    public String getSentence() {
    	return "the " + domain.getConceptName() + " '" + getDomainName() + "' " + getPropertyName() + " the " + range.getConceptName() + " '" + getRangeName() + "'.";
    }

    public ArrayList<String> getReferencedInstances() {
    	ArrayList<String> referencedInsts = new ArrayList<String>();
    	referencedInsts.add(getDomainName());
    	referencedInsts.add(getRangeName());
    	return referencedInsts;
    }

    @Override
    public String toString() {
        return "[MatchedTriple] (" + domain + ") " + getDomainName() + ":" + getPropertyName() + ":" + "(" + range + ") " + getRangeName();
    }
}
