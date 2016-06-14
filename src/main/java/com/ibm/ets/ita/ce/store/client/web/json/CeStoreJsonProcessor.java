package com.ibm.ets.ita.ce.store.client.web.json;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.LOCALE;

import java.io.IOException;

import com.ibm.ets.ita.ce.store.ActionContext;

public abstract class CeStoreJsonProcessor {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	protected String jsonText = null;

	public abstract StringBuilder serializeToSb(ActionContext pAc) throws IOException;
	public abstract String serialize(ActionContext pAc) throws IOException;

	public void setJsonText(String pJsonText) {
		this.jsonText = pJsonText;
	}

	protected static String encodeJsonKey(String pKey) {
		return encodeJsonValue(pKey) + ":";
	}

	protected static String encodeJsonValue(String pValue) {
		return "\"" + jsonSubstitutions(pValue) + "\"";
	}

	private static String jsonSubstitutions(String pValue) {
		StringBuilder sb = new StringBuilder();

		if (pValue != null) {
			for (int i = 0; i < pValue.length(); i++) {
				char c = pValue.charAt(i);

				switch(c) {
					case '\\':
						sb.append("\\\\");
						break;
					case '"':
						sb.append("\\\"");
						break;
					case '/':
						sb.append("\\/");
						break;
					case '\b':
						sb.append("\\b");
						break;
					case '\f':
						sb.append("\\f");
						break;
					case '\n':
						sb.append("\\n");
						break;
					case '\r':
						sb.append("\\r");
						break;
					case '\t':
						sb.append("\\t");
						break;
					default:
						if ((c >= '\u0000' && c <= '\u001F') || (c >= '\u007F' && c <= '\u009F') || (c >= '\u2000' && c <= '\u20FF')) {
							String hexText = "\\u" + String.format(LOCALE, "%04X", new Integer(c)).toUpperCase();

							sb.append(hexText);
						} else{
							sb.append(c);
						}
				}
			}
		}

		return sb.toString();
	}

}