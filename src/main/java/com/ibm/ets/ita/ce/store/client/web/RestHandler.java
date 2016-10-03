package com.ibm.ets.ita.ce.store.client.web;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.ENCODING;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COMMA;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_TRUE;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_AC_AC;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_AC_AH;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_AC_AM;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_AC_AO;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_AC_RH;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_AUTH;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_CEUSER;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_ORIGIN;
import static com.ibm.ets.ita.ce.store.names.RestNames.RESPONSE_JSON;
import static com.ibm.ets.ita.ce.store.names.RestNames.RESPONSE_TEXT;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.ets.ita.ce.store.client.rest.CeStoreRestApi;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;

/**
 * Servlet implementation class RestHandler
 */
@MultipartConfig
public class RestHandler extends HttpServlet {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = RestHandler.class.getName();
	private static final String PACKAGE_NAME = RestHandler.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final String ALL_METHODS = "GET,POST,HEAD,OPTIONS,PUT,DELETE";

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse)
			throws ServletException, IOException {
		doStandardProcessing(pRequest, pResponse);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse)
			throws ServletException, IOException {
		doStandardProcessing(pRequest, pResponse);
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest pRequest, HttpServletResponse pResponse)
			throws ServletException, IOException {
		doStandardProcessing(pRequest, pResponse);
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest pRequest, HttpServletResponse pResponse)
			throws ServletException, IOException {
		doStandardProcessing(pRequest, pResponse);
	}

	/**
	 * @see HttpServlet#doOptions(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doOptions(HttpServletRequest pRequest, HttpServletResponse pResponse)
			throws ServletException, IOException {
		// For an options request just set the standard CORS response headers
		setCorsResponseHeaders(pRequest, pResponse);
	}

	private static void doStandardProcessing(HttpServletRequest pRequest, HttpServletResponse pResponse) {
		final String METHOD_NAME = "doStandardProcessing";
		WebActionContext wc = null;
		boolean statsInResponse = false;

		try {
			ServletStateManager.retainDefaultUrl(pRequest);
			wc = createWebActionContext(pRequest);
			extractCredentials(wc, pRequest);
			initialiseHttpRequest(pRequest, wc);
			statsInResponse = CeStoreRestApi.processRestRequest(wc, pRequest);
		} catch (Exception e) {
			reportException(e, wc, logger, CLASS_NAME, METHOD_NAME);
		}

		wrapUpAndReturn(wc, pRequest, pResponse, statsInResponse);
	}

	private static synchronized WebActionContext createWebActionContext(HttpServletRequest pRequest) {
		WebActionResponse wr = new WebActionResponse();
		WebActionContext wc = new WebActionContext(pRequest.getHeader(HDR_CEUSER), wr);
		ModelBuilder mb = ServletStateManager.getServletStateManager().getDefaultModelBuilder(wc);
		wc.setModelBuilderAndCeStoreName(mb);

		return wc;
	}

	private static void extractCredentials(WebActionContext pWc, HttpServletRequest pRequest) {
		String creds = pRequest.getHeader(HDR_AUTH);

		if ((creds != null) && (!creds.isEmpty())) {
			pWc.setCredentials(creds);
			reportDebug("Setting request credentials (" + creds + ")", pWc);
		}
	}

	private static void initialiseHttpRequest(HttpServletRequest pRequest, WebActionContext pWc) {
		final String METHOD_NAME = "initialiseRequest";

		// TODO: Why is this even needed?
		// TODO: The encoding should come from the container, not be hard-coded
		try {
			pRequest.setCharacterEncoding(ENCODING);
		} catch (UnsupportedEncodingException e) {
			reportException(e, pWc, logger, CLASS_NAME, METHOD_NAME);
		}

	}

	private static void setCorsResponseHeaders(HttpServletRequest pRequest, HttpServletResponse pResponse) {
		// TODO: Abstract these
		// Set headers to allow cross domain responses
		pResponse.setHeader(HDR_AC_AO, pRequest.getHeader(HDR_ORIGIN));
		pResponse.setHeader(HDR_AC_AC, TOKEN_TRUE);
		pResponse.setHeader(HDR_AC_AM, ALL_METHODS);
		pResponse.setHeader(HDR_AC_AH, HDR_AUTH + TOKEN_COMMA + pRequest.getHeader(HDR_AC_RH));

		// IMPORTANT:
		// If you enable http authentication in Tomcat (i.e. in web.xml) then
		// you must also add a corresponding CorsFilter
		// in web.xml also. The values specified in there must match the above
		// methods/headers and origins.
		// So, if you need to add a new header (for example) then don't forget
		// to review and update web.xml also (if http authentication is on).
	}

	private static void wrapUpAndReturn(WebActionContext pWc, HttpServletRequest pRequest,
			HttpServletResponse pResponse, boolean pWrapResponse) {
		updateResponse(pWc, pRequest, pResponse);
		updateCeStore(pWc);
		createResponseAndReturn(pWc, pResponse, pWrapResponse);
	}

	private static void updateResponse(WebActionContext pWc, HttpServletRequest pRequest,
			HttpServletResponse pResponse) {
		setCorsResponseHeaders(pRequest, pResponse);

		pResponse.setCharacterEncoding(ENCODING);

		if (pWc.getActionResponse().isPlainTextResponse()) {
			pResponse.setContentType(RESPONSE_TEXT);
		} else {
			pResponse.setContentType(RESPONSE_JSON);
		}
	}

	private static void updateCeStore(WebActionContext pWc) {
		if (pWc.hasModelBuilder()) {
			pWc.getModelBuilder().updateCreatedThingsFrom(pWc);
		}
	}

	private static void createResponseAndReturn(WebActionContext pWc, HttpServletResponse pResponse,
			boolean pWrapResponse) {
		if (pWc.getWebActionResponse().hasHttpError()) {
			returnErrorDetails(pWc, pResponse);
		} else {
			returnNormalResponse(pWc, pResponse, pWrapResponse);
		}
	}

	private static void returnErrorDetails(WebActionContext pWc, HttpServletResponse pResponse) {
		final String METHOD_NAME = "returnErrorDetails";
		String errorText = pWc.getWebActionResponse().getHttpErrorMessage();

		if (errorText == null) {
			errorText = pWc.getActionResponse().convertErrorsAndWarningsToHtml();
		}

		try {
			pResponse.sendError(pWc.getWebActionResponse().getHttpErrorCode(), errorText);
		} catch (IOException e) {
			reportException(e, pWc, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	private static void returnNormalResponse(WebActionContext pWc, HttpServletResponse pResponse,
			boolean pWrapResponse) {
		final String METHOD_NAME = "returnNormalResponse";

		try {
			pWc.getActionResponse().returnInResponse(pWc, pResponse.getWriter(), pWrapResponse);
		} catch (IOException e) {
			reportException(e, pWc, logger, CLASS_NAME, METHOD_NAME);
		}
	}

}
