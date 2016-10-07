package com.ibm.ets.ita.ce.store.hudson.model.answer;

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_C_ID;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_C_LAT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_C_LON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_A_RESCOORDS;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class AnswerCoords extends Answer {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String id = null;
	private String lat = null;
	private String lon = null;

	public AnswerCoords(String pId, String pLat, String pLon, int pConf) {
		super(pConf);

		this.id = pId;
		this.lat = pLat;
		this.lon = pLon;
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

	@Override
	public String toString() {
		String result = null;

		result = this.lat + ", " + this.lon + " (" + this.id + ")";
		
		return result;
	}

	@Override
	public CeStoreJsonObject specificJson() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		CeStoreJsonObject jRes = new CeStoreJsonObject();

		jRes.put(JSON_A_C_ID, this.id);
		jRes.put(JSON_A_C_LAT, this.lat);
		jRes.put(JSON_A_C_LON, this.lon);

		result.put(JSON_A_RESCOORDS, jRes);

		return result;
	}

}
