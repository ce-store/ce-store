package com.ibm.ets.ita.ce.store.hudson.model.answer;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class AnswerCoords {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String id = null;
	private String lat = null;
	private String lon = null;
	private String postcode = null;
	private String addressLine1 = null;

	public AnswerCoords(String pId, String pLat, String pLon, String pPostcode, String pAddressLine1) {
		this.id = pId;
		this.lat = pLat;
		this.lon = pLon;
		this.postcode = pPostcode;
		this.addressLine1 = pAddressLine1;
	}

	public String getId() {
		return this.id;
	}

	public String getLat() {
		return this.lat;
	}

	public String getLon() {
		return this.lon;
	}

	public String getPostcode() {
		return this.postcode;
	}

	public String getAddressLine1() {
		return this.addressLine1;
	}

	@Override
	public String toString() {
		String result = null;

		if (this.lat != null) {			
			result = this.lat + ", " + this.lon + " (" + this.id + ")";
		} else {
			result = this.addressLine1 + ", " + this.postcode + " (" + this.id + ")";
		}
		
		return result;
	}

}