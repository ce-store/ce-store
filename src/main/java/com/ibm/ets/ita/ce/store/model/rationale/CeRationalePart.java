package com.ibm.ets.ita.ce.store.model.rationale;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

public abstract class CeRationalePart {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

//	private static AtomicLong rationaleIdVal = new AtomicLong(0);
//	protected String id = null;
	protected boolean isConceptNegated = false;
	protected boolean isPropertyNegated = false;
	protected String conceptName = "";
	protected String instanceName = "";
	protected String propertyName = "";
	protected String rangeName = "";
	protected String value = "";

	protected abstract String formattedType();

	protected CeRationalePart() {
		// This is private to ensure that new instances can only be created via
		// the various static methods
	}

//	public static void resetCounter() {
//		rationaleIdVal = new AtomicLong(0);
//	}

//	protected static long nextRationaleId() {
//		return rationaleIdVal.incrementAndGet();
//	}

//	public String getId() {
//		return this.id;
//	}

	public boolean isConceptNegated() {
		return this.isConceptNegated;
	}

	public boolean isPropertyNegated() {
		return this.isPropertyNegated;
	}

	public boolean isNegated() {
		return isConceptNegated() || isPropertyNegated();
	}

	public boolean isConceptOnly() {
		return !hasPropertyDetails();
	}

	public String getConceptName() {
		return this.conceptName;
	}

	public String getInstanceName() {
		return this.instanceName;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	public String getRangeName() {
		return this.rangeName;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		String result = "";

		result += formattedType() + ": ";

		if (hasInstanceDetails()) {
			result += formattedInstanceDetails() + ", ";
		} else {
			if (hasConceptDetails()) {
				result += formattedConceptDetails() + ", ";
			}
		}

		if (hasPropertyDetails()) {
			result += formattedPropertyDetails();
		}

		return result;
	}

	public boolean hasConceptDetails() {
		return (this.conceptName != null) && (!this.conceptName.isEmpty());
	}

	public boolean hasInstanceDetails() {
		return this.hasConceptDetails() && (this.instanceName != null) && (!this.instanceName.isEmpty());
	}

	public boolean hasPropertyDetails() {
		return this.hasInstanceDetails() && (this.propertyName != null) && (!this.propertyName.isEmpty()) && !hasClassMembershipProperty();
	}

	private boolean hasClassMembershipProperty() {
		boolean result = false;

		if ((this.propertyName != null) && (!this.propertyName.isEmpty())) {
			//TODO: Remove the dash delimited versions when that syntax is deprecated
			result = this.propertyName.equals("[is a]") || this.propertyName.equals("[is an]") || this.propertyName.equals("[is-a]") || this.propertyName.equals("[is-an]");
		}

		return result;
	}

	private String formattedConceptDetails() {
		String negText = "";

		if (this.isConceptNegated) {
			negText = "[negated]";
		}

		return this.conceptName + negText;
	}

	private String formattedInstanceDetails() {
		return "(" + formattedConceptDetails() + ") '" + this.instanceName + "'";
	}

	private String formattedPropertyDetails() {
		String negText = "";
		String rangeText = "";
		String valText = "";

		if (this.rangeName != null) {
			rangeText = " (" + this.rangeName + ")";
		}

		if (this.value != null) {
			valText = " '" + this.value + "'";
		}

		if (this.isPropertyNegated) {
			negText = "[negated]";
		}

		return this.propertyName + rangeText + valText + negText;		
	}

}
