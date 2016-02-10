package com.ibm.ets.ita.ce.store.client.web.json;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeStoreJsonObject extends CeStoreJsonProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String MAP_STRING = "stringMap";
	private static final String MAP_OBJECT = "objectMap";
	private static final String MAP_JSON = "jsonMap";

	private LinkedHashMap<String, String> keyMap = new LinkedHashMap<String, String>();

	private LinkedHashMap<String, String> stringMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, Object> objectMap = new LinkedHashMap<String, Object>();
	private LinkedHashMap<String, CeStoreJsonProcessor> jsonMap = new LinkedHashMap<String, CeStoreJsonProcessor>();

	public Object get(ActionContext pAc, String pKey) {
		String tgtMap = this.keyMap.get(pKey);
		Object result = null;

		if (tgtMap != null) {
			if (tgtMap.equals(MAP_STRING)) {
				result = this.stringMap.get(pKey);
			} else if (tgtMap.equals(MAP_OBJECT)) {
				result = this.objectMap.get(pKey);
			} else if (tgtMap.equals(MAP_JSON)) {
				result = this.jsonMap.get(pKey);
			} else {
				reportError("Unknown map type '" + tgtMap + "' encountered during JSON processing for key '" + pKey + "'", pAc);
			}
		}

		return result;
	}

	public void put(String pKey, String pValue) {
		this.keyMap.put(pKey, MAP_STRING);
		this.stringMap.put(pKey, pValue);
	}

	public void put(String pKey, Object pValue) {
		this.keyMap.put(pKey, MAP_OBJECT);
		this.objectMap.put(pKey, pValue);
	}

	public void put(String pKey, int pValue) {
		this.keyMap.put(pKey, MAP_OBJECT);
		this.objectMap.put(pKey, new Integer(pValue));
	}

	public void put(String pKey, long pValue) {
		this.keyMap.put(pKey, MAP_OBJECT);
		this.objectMap.put(pKey, new Long(pValue));
	}

	public void put(String pKey, double pValue) {
		this.keyMap.put(pKey, MAP_OBJECT);
		this.objectMap.put(pKey, new Double(pValue));
	}

	public void put(String pKey, boolean pValue) {
		this.keyMap.put(pKey, MAP_OBJECT);
		this.objectMap.put(pKey, new Boolean(pValue));
	}

	public void put(String pKey, CeStoreJsonProcessor pValue) {
		this.keyMap.put(pKey, MAP_JSON);
		this.jsonMap.put(pKey, pValue);
	}
	
	public Integer getInt(String pKey) {
		return (Integer) this.objectMap.get(pKey);
	}

	public String getString(String pKey) {
		return this.stringMap.get(pKey);
	}

	public boolean isEmpty() {
		return this.keyMap.isEmpty();
	}

	@Override
	public StringBuilder serializeToSb(ActionContext pAc) throws IOException {
		StringBuilder sb = new StringBuilder();

		if (this.jsonText == null) {
			String commaVal = "";
			sb.append("{");

			for (String thisKey : this.keyMap.keySet()) {
				sb.append(commaVal);
				String tgtMap = this.keyMap.get(thisKey);

				if (tgtMap.equals(MAP_STRING)) {
					sb.append(encodeJsonKey(thisKey));
					sb.append(encodeJsonValue(this.stringMap.get(thisKey)));
				} else if (tgtMap.equals(MAP_OBJECT)) {
					sb.append(encodeJsonKey(thisKey));
					sb.append(this.objectMap.get(thisKey));
				} else if (tgtMap.equals(MAP_JSON)) {
					sb.append(encodeJsonKey(thisKey));
					CeStoreJsonProcessor thisJp = this.jsonMap.get(thisKey);

					if (thisJp != null) {
						sb.append(thisJp.serialize(pAc));
					} else {
						sb.append("null");
					}
				} else {
					reportError("Unknown map type '" + tgtMap + "' encountered during JSON serializing for key '" + thisKey + "'", pAc);
				}

				commaVal = ",";
			}

			sb.append("}");
		} else {
			sb.append(this.jsonText);
		}

		return sb;
	}

	@Override
	public String serialize(ActionContext pAc) throws IOException {
		return serializeToSb(pAc).toString();
	}

	@Override
	public String toString() {
		String result = "(CeStoreJsonObject): ";
		String commaSep = "";

		for (String thisKey : this.keyMap.keySet()) {
			String thisVal = "";

			String tgtMap = this.keyMap.get(thisKey);
			Object tgtObj = null;

			if (tgtMap != null) {
				if (tgtMap.equals(MAP_STRING)) {
					tgtObj = this.stringMap.get(thisKey);
				} else if (tgtMap.equals(MAP_OBJECT)) {
					tgtObj = this.objectMap.get(thisKey);
				} else if (tgtMap.equals(MAP_JSON)) {
					tgtObj = this.jsonMap.get(thisKey);
				} else {
					thisVal = "<error>";
				}
			} else {
				thisVal = "<null>";
			}

			if (tgtObj != null) {
				thisVal = tgtObj.toString();
			}

			result += commaSep + thisKey + "='" + thisVal + "'";
			commaSep = ", ";
		}

		return result;
	}

}