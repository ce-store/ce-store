package com.ibm.ets.ita.ce.store.client.web;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STRUCTURED;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_TS;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.urlDecode;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonProcessor;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ActionResponse;

public class WebActionResponse extends ActionResponse {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CLASS_NAME = WebActionResponse.class.getName();
	private static final String PACKAGE_NAME = WebActionResponse.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private CeStoreJsonObject jsonPayload = null;
	private StringBuilder sbPayload = null;
	private CeStoreJsonProcessor structuredResult = null;

	private int httpErrorCode = -1;
	private String httpErrorMessage = null;

	public int getHttpErrorCode() {
		return this.httpErrorCode;
	}

	public void setHttpErrorCode(int pErrCode) {
		this.httpErrorCode = pErrCode;
	}

	public boolean hasHttpError() {
		return this.httpErrorCode != -1;
	}

	public String getHttpErrorMessage() {
		return this.httpErrorMessage;
	}

	public void setHttpErrorMessage(String pErrMsg) {
		this.httpErrorMessage = pErrMsg;
	}

	private void initialiseJsonPayload(ActionContext pAc, String pKey) {
		if (this.jsonPayload == null) {
			this.jsonPayload = new CeStoreJsonObject();
		}

		if (this.jsonPayload.get(pAc, pKey) != null) {
			reportWarning("Payload (JSON) has already been populated with data for key '" + pKey + "'", pAc);
		}
	}

	private void initialiseSbPayload(ActionContext pAc) {
		if (this.sbPayload != null) {
			reportWarning("Payload (plain) has already been initialised", pAc);
		}

		if (this.sbPayload == null) {
			this.sbPayload = new StringBuilder();
		}
	}

	public void setStructuredResult(CeStoreJsonProcessor pJson) {
		if (pJson != null) {
			this.structuredResult = pJson;
		}
	}

	@Override
	public void saveStructuredResult(ActionContext pAc) {
		addToPayload(pAc, JSON_STRUCTURED, this.structuredResult);
	}

	public void setPayloadTo(CeStoreJsonObject pJsonObj) {
		this.jsonPayload = pJsonObj;
	}

	private void addToPayload(ActionContext pAc, String pKey, CeStoreJsonProcessor pJson) {
		initialiseJsonPayload(pAc, pKey);
		this.jsonPayload.put(pKey, pJson);
	}

	@Override
	public void setPlainTextPayload(ActionContext pAc, String pText) {
		pAc.getActionResponse().setIsPlainTextResponse(true);
		initialiseSbPayload(pAc);
		this.sbPayload.append(pText);
	}

	@Override
	public void plainTextResponse(PrintWriter pOut) {
		// This is the special response type which is plain text
		pOut.append(this.sbPayload.toString());
	}
	
	@Override
	public void gzipResponse(ActionContext pAc, HttpServletResponse pResponse) {
		// This is the special response type which is a gzip archive file
		GZIPOutputStream gz;
		ObjectOutputStream oos;
		StringBuffer fileName = new StringBuffer(pAc.getModelBuilder().getCeStoreName().toLowerCase()) ;
		String appTS = urlDecode(pAc, ((WebActionContext) pAc).getRequest().getParameter(PARM_TS));
		if (appTS != null && "true".equalsIgnoreCase(appTS)) {
			fileName.append("_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())) ;
		}
		pResponse.setHeader("Content-Disposition", "attachment; filename="+ fileName + ".gz;");
		try {
			
			gz = new GZIPOutputStream(pResponse.getOutputStream());
			oos = new ObjectOutputStream(gz);
			oos.writeObject(pAc.getModelBuilder());
			oos.flush();
			oos.close();
			gz.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void convertAndRespond(ActionContext pAc, PrintWriter pOut, boolean pWrapResponse) {
		final String METHOD_NAME = "convertAndRespond";

		if (pWrapResponse) {
			populateStandardFields(pAc);
		}

		// Since this is a web action response the conversion is to json
		try {
			StringBuilder jsonSb = null;

			if (pWrapResponse) {
				if (this.jsonPayload != null) {
					jsonSb = this.jsonPayload.serializeToSb(pAc);
				}
			} else {
				if (this.structuredResult != null) {
					jsonSb = this.structuredResult.serializeToSb(pAc);
				} else {
					if (this.jsonPayload != null) {
						jsonSb = this.jsonPayload.serializeToSb(pAc);
					}
				}
			}

			if (jsonSb != null) {
				pOut.append(jsonSb);
			}
		} catch (IOException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	@Override
	protected void addMessagesToPayload(ActionContext pAc, String pKey, ArrayList<String> pMessages) {
		addToPayload(pAc, pKey, CeWebObject.generateStandardMessagesFrom(pMessages));
	}

	@Override
	protected void addStatsToPayload(ActionContext pAc, String pKey, LinkedHashMap<String, String> pStats) {
		addToPayload(pAc, pKey, CeWebObject.generateStandardStatsFrom(pStats));
	}

	@Override
	protected void addAlertsToPayload(ActionContext pAc, String pKey,
			LinkedHashMap<String, ArrayList<String>> pAlerts) {
		addToPayload(pAc, pKey, CeWebObject.generateStandardAlertsFrom(pAlerts));
	}

}
