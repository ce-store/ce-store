package com.ibm.ets.ita.ce.store.model.container;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

public class ContainerSearchResult implements Comparable<ContainerSearchResult> {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String idKey = null;
	private ArrayList<String> conceptNames = new ArrayList<String>();
	private String instanceName = "";
	private String propertyName = "";
	private String propertyValue = "";
	private String propertyType = "";

	public String identityKey() {
		if (this.idKey == null) {
			this.idKey = firstConceptName() + ":" + this.instanceName + ":" + this.propertyName + ":" + this.propertyValue;
		}

		return this.idKey;
	}

	public ArrayList<String> getConceptNames() {
		return this.conceptNames;
	}

	public void setConceptNames(ArrayList<String> pConceptNames) {
		this.conceptNames = pConceptNames;
	}

	public String firstConceptName() {
		String result = null;

		if (!this.conceptNames.isEmpty()) {
			result = this.conceptNames.get(0);
		}

		return result;
	}

	public String getInstanceName() {
		return this.instanceName;
	}

	public void setInstanceName(String pInstanceName) {
		this.instanceName = pInstanceName;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	public void setPropertyName(String pPropertyName) {
		this.propertyName = pPropertyName;
	}

	public String getPropertyValue() {
		return this.propertyValue;
	}

	public void setPropertyValue(String pPropertyValue) {
		this.propertyValue = pPropertyValue;
	}

	public String getPropertyType() {
		return this.propertyType;
	}

	public void setPropertyType(String pPropertyType) {
		this.propertyType = pPropertyType;
	}

	@Override
	public boolean equals(Object pObj) {
		boolean result = false;

		if (pObj.hashCode() == hashCode()) {
			//The hash codes match so these objects MAY be the same.  Now check on the strings
			ContainerSearchResult pSr = (ContainerSearchResult)pObj;
			result = identityKey().equals(pSr.identityKey());
		}

		return result;
	}

	@Override
	public int compareTo(ContainerSearchResult pOther) {
		return identityKey().compareTo(pOther.identityKey());
	}

	@Override
	public int hashCode() {
		return identityKey().hashCode();
	}

}
