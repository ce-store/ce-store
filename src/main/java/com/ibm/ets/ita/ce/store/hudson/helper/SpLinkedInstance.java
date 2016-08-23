package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class SpLinkedInstance extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String label = null;
	private CeInstance matchedInstance = null;
	private CeProperty linkingProperty = null;
	private ArrayList<CeInstance> linkedInstances = null;

	public SpLinkedInstance(CeInstance pMatchedInstance, CeProperty pLinkingProperty, ArrayList<CeInstance> pLinkedInstances, String pLabel) {
		this.matchedInstance = pMatchedInstance;
		this.linkingProperty = pLinkingProperty;
		this.linkedInstances = pLinkedInstances;
		this.label = pLabel;
	}

	public CeInstance getMatchedInstance() {
		return this.matchedInstance;
	}

	public CeProperty getLinkingProperty() {
		return this.linkingProperty;
	}

	public ArrayList<CeInstance> getLinkedInstances() {
		return this.linkedInstances;
	}

	public String getLabel() {
		return this.label;
	}

}
