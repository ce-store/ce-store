package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.convertToString;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.urlDecode;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.ibm.ets.ita.ce.store.ApiHandler;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.WebActionResponse;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebConcept;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebContainerResult;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebInstance;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebProperty;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebRationaleReasoningStep;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSentence;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSource;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSpecial;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;

public abstract class CeStoreRestApi extends ApiHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = CeStoreRestApi.class.getName();
	private static final String PACKAGE_NAME = CeStoreRestApi.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final String CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";

	private static final String REQTYPE_ANY = "*/*";
	private static final String REQTYPE_JSON = "application/json";
	private static final String REQTYPE_TEXT = "text/plain";
	private static final String REQTYPE_WEAKTEXT = "text";

	private static final String METHOD_GET = "GET";
	private static final String METHOD_PUT = "PUT";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_DELETE = "DELETE";

	private static final String STYLE_FULL = "full";
	private static final String STYLE_SUMMARY = "summary";

	private static final String REST_STORE = "stores";
	protected static final String REST_SOURCE = "sources";
	protected static final String REST_CONCEPT = "concepts";
	protected static final String REST_INSTANCE = "instances";
	protected static final String REST_SENTENCE = "sentences";
	private static final String REST_QUERY = "queries";
	private static final String REST_RULE = "rules";
	protected static final String REST_PRIMARY = "primary";
	protected static final String REST_SECONDARY = "secondary";
	protected static final String REST_CHILDREN = "children";
	protected static final String REST_PARENTS = "parents";
	protected static final String REST_DIRECT = "direct";
	protected static final String REST_FREQUENCY = "frequency";
	protected static final String REST_COUNT = "count";
	protected static final String REST_EXACT = "exact";
	protected static final String REST_PROPERTY = "properties";
	protected static final String REST_DATATYPE = "datatype";
	protected static final String REST_OBJECT = "object";
	protected static final String REST_RATIONALE = "rationale";
	protected static final String REST_EXECUTE = "execute";

	private static final String REST_SPECIAL = "special";
	private static final String REST_CONMODEL = "models";
	private static final String REST_HEADLINE = "headline";

	protected static final String PARM_STARTTS = "startTimestamp";
	protected static final String PARM_ENDTS = "endTimestamp";
	protected static final String PARM_RETCE = "returnCe";
	protected static final String PARM_RETINSTS = "returnInstances";
	private static final String PARM_CETEXT = "ceText";
	private static final String PARM_SHOWSTATS = "showStats";
	private static final String PARM_STYLE = "style";
	
	private static final String HDR_ACCEPT = "Accept";

	protected static final String VAL_UNDEFINED = "undefined";

	protected WebActionContext wc = null;
	private ModelBuilder mb = null;
	protected ArrayList<String> restParts = null;
	protected HttpServletRequest request = null;
	private String reqType = null;
	private String reqMethod = null;

	protected String parmStyle = null;

	public CeStoreRestApi(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		this.wc = pWc;
		this.mb = pWc.getModelBuilder();
		this.restParts = pRestParts;
		this.request = pRequest;
		this.reqType = this.request.getHeader(HDR_ACCEPT);
		this.reqMethod = this.request.getMethod();

		this.parmStyle = pRequest.getParameter(PARM_STYLE);
	}
	
	public static String getCeStoreNameFrom(ArrayList<String> pRestParts) {
		String result = null;

		//The CE store name is found in the second element only if the first element is the "store" indicator, otherwise
		//the default store name is used.
		if ((pRestParts == null) || (pRestParts.size() <= 1)) {
			result = ModelBuilder.CESTORENAME_DEFAULT;
		} else {
			if (pRestParts.get(0).equals(REST_STORE)) {
				result = pRestParts.get(1);
			}
		}

		return result;
	}

	protected static void appendCeMainHeader(StringBuilder pSb, String pHdrText) {
		appendToSb(pSb, "-- " + pHdrText);
		appendToSb(pSb, "");
	}

	protected static void appendCeSubHeader(StringBuilder pSb, String pHdrText) {
		appendToSb(pSb, "-- " + pHdrText);
	}

	public static boolean processRestRequest(WebActionContext pWc, HttpServletRequest pRequest) {
		ArrayList<String> pRestParts = extractRestParts(pWc, pRequest);
		
		return doRestRequestProcessing(pWc, pRequest, pRestParts);
	}
	
	public static boolean processModifiedRestRequest(WebActionContext pWc, HttpServletRequest pRequest, ArrayList<String> ppRestParts) {
		return doRestRequestProcessing(pWc, pRequest, ppRestParts);
	}

	private static boolean doRestRequestProcessing(WebActionContext pWc, HttpServletRequest pRequest, ArrayList<String> pRestParts) {
		boolean statsInResponse = false;

		if (!pRestParts.isEmpty()) {
			String firstPart = pRestParts.get(0);

			if (firstPart.equals(REST_STORE)) {
				//Process the request, and if it is "/stores/<storename>/...",
				//the correct ModelBuilder will be set-up and this method called
				//again with the "/stores/<storename>" removed to process the 
				//remaining parts request using the ModelBuilder. 
				CeStoreRestApiStore restHandler = new CeStoreRestApiStore(pWc, pRestParts, pRequest);
				statsInResponse = restHandler.processRequest();
			} else {
				if (pWc.getModelBuilder()==null) {
					ModelBuilder tgtMb = getNamedModelBuilder(pWc, ModelBuilder.CESTORENAME_DEFAULT);
					pWc.setModelBuilderAndCeStoreName(tgtMb);
				}
				
				if(firstPart.equals(REST_SOURCE)) {
					CeStoreRestApiSource restHandler = new CeStoreRestApiSource(pWc, pRestParts, pRequest);
					statsInResponse = restHandler.processRequest();
				} else if(firstPart.equals(REST_SENTENCE)) {
					CeStoreRestApiSentence restHandler = new CeStoreRestApiSentence(pWc, pRestParts, pRequest);
					statsInResponse = restHandler.processRequest();
				} else if(firstPart.equals(REST_CONMODEL)) {
					CeStoreRestApiConceptualModel restHandler = new CeStoreRestApiConceptualModel(pWc, pRestParts, pRequest);
					statsInResponse = restHandler.processRequest();
				} else if (firstPart.equals(REST_CONCEPT)) {
					CeStoreRestApiConcept restHandler = new CeStoreRestApiConcept(pWc, pRestParts, pRequest);
					statsInResponse = restHandler.processRequest();
				} else if(firstPart.equals(REST_PROPERTY)) {
					CeStoreRestApiProperty restHandler = new CeStoreRestApiProperty(pWc, pRestParts, pRequest);
					statsInResponse = restHandler.processRequest();
				} else if(firstPart.equals(REST_INSTANCE)) {
					CeStoreRestApiInstance restHandler = new CeStoreRestApiInstance(pWc, pRestParts, pRequest);
					statsInResponse = restHandler.processRequest();
				} else if(firstPart.equals(REST_QUERY)) {
					CeStoreRestApiQuery restHandler = new CeStoreRestApiQuery(pWc, pRestParts, pRequest);
					statsInResponse = restHandler.processRequest();
				} else if(firstPart.equals(REST_RULE)) {
					CeStoreRestApiRule restHandler = new CeStoreRestApiRule(pWc, pRestParts, pRequest);
					statsInResponse = restHandler.processRequest();
				} else if(firstPart.equals(REST_SPECIAL)) {
					statsInResponse = CeStoreRestApiSpecial.processRequest(pWc, pRestParts, pRequest);
				} else {
					reportUnsupportedPrimaryError(pWc, firstPart);
				}
			}
		} else {
			reportUnsupportedPrimaryError(pWc, "");
		}

		//If the user has specified showStats=true as a HTTP parameter then this will override the statsInResponse value
		String overrideStats = urlDecode(pWc, pRequest.getParameter(PARM_SHOWSTATS));
		if (overrideStats.equals("true")) {
			statsInResponse = true;
		}

		return statsInResponse;
	}

	private static ArrayList<String> extractRestParts(WebActionContext pWc, HttpServletRequest pRequest) {
		ArrayList<String> result = new ArrayList<String>();

		String urlRoot = pRequest.getContextPath();
		String fullUrl = pRequest.getRequestURI();
		String restUrl = fullUrl.replace(urlRoot, "");
		String[] rawpRestParts = restUrl.split("/");

		//Remove any empty parts (typically the first if the url starts with \)
		for (String thisRawPart : rawpRestParts) {
			if (!thisRawPart.isEmpty()) {
				result.add(urlDecode(pWc, thisRawPart));
			}
		}

		return result;
	}

	protected WebActionResponse getWebActionResponse() {
		return this.wc.getWebActionResponse();
	}

	protected ModelBuilder getModelBuilder() {
		return this.mb;
	}
	
	protected static ModelBuilder getNamedModelBuilder(WebActionContext pWc, String pMbName) {
		ModelBuilder result = ServletStateManager.getServletStateManager().getModelBuilder(pMbName);
		
		if (result == null && pMbName.toUpperCase().trim().equals(ModelBuilder.CESTORENAME_DEFAULT)) {
			result = ServletStateManager.getServletStateManager().createModelBuilder(pWc, pMbName);
		}
		
		return result;
	}

	protected static TreeMap<String, ModelBuilder> getAllModelBuilders() {
		return ServletStateManager.getServletStateManager().getAllModelBuilders();
	}

	protected boolean isJsonRequest() {
		//The default is to return json (hence the null and "any" tests)
		return (this.reqType == null) || (this.reqType.contains(REQTYPE_ANY)) || (this.reqType.contains(REQTYPE_JSON));
	}

	protected boolean isTextRequest() {
		boolean result = (this.reqType != null) && (this.reqType.contains(REQTYPE_TEXT));

		if (!result) {
			//Try the "weak" text test as some browsers and agents use different forms
			result = (this.reqType != null) && (this.reqType.contains(REQTYPE_WEAKTEXT));
		}

		return result;
	}

	protected boolean isGet() {
		return this.reqMethod.equals(METHOD_GET);
	}

	protected boolean isPut() {
		return this.reqMethod.equals(METHOD_PUT);
	}

	protected boolean isPost() {
		return this.reqMethod.equals(METHOD_POST);
	}

	protected boolean isDelete() {
		return this.reqMethod.equals(METHOD_DELETE);
	}

	@Override
	public boolean isDefaultStyle() {
		return (this.parmStyle == null) || ((!this.parmStyle.equals(STYLE_FULL)) && (!this.parmStyle.equals(STYLE_SUMMARY)));
	}

	@Override
	public boolean isFullStyle() {
		return (this.parmStyle != null) && (this.parmStyle.equals(STYLE_FULL));
	}

	@Override
	public boolean isSummaryStyle() {
		return (this.parmStyle != null) && (this.parmStyle.equals(STYLE_SUMMARY));
	}

	protected String getParameterNamed(String pParmName) {
		//The parameter may be passed as a url parameter or a http request header
		//Both alternatives have the same name
		//The http url takes precedence in cases where both are specified
		String result = getUrlParameterValueNamed(pParmName);

		if ((result == null) || (result.isEmpty())) {
			result = this.request.getHeader(pParmName);
		}

		return result;
	}

	protected String[] getListParameterNamed(String pParmName) {
		String rawVal = getParameterNamed(pParmName);
		String[] listVals = null;

		if (rawVal != null) {
			listVals = rawVal.split(",");
		}

		return listVals;
	}

	protected boolean getBooleanParameterNamed(String pParmName) {
		String parmVal = getParameterNamed(pParmName);
		return new Boolean(parmVal).booleanValue();
	}

	protected boolean getBooleanParameterNamed(String pParmName, boolean pDefaultValue) {
		boolean result = pDefaultValue;
		String parmVal = getParameterNamed(pParmName);

		if ((parmVal != null) && (!parmVal.isEmpty())) {
			result = new Boolean(parmVal).booleanValue();
		}

		return result;
	}

	protected String getUrlParameterValueNamed(String pKeyName) {
		return urlDecode(this.wc, this.request.getParameter(pKeyName));
	}

	protected Set<String> getUrlParameterValuesNamed(String pKeyName) {
		Set<String> valueSet;
		String[] valueArray = this.request.getParameterValues(pKeyName);
		if (valueArray != null) {
			valueSet = new HashSet<String>(valueArray.length);
			for(String value : valueArray) {
				valueSet.add(urlDecode(this.wc, value));
			}
		} else {
			valueSet = Collections.emptySet();
		}
		return valueSet;
	}

	public boolean getBooleanUrlParameterValueNamed(String pKeyName, boolean pDefaultValue) {
		boolean result = pDefaultValue;
		String parmText = getUrlParameterValueNamed(pKeyName);

		if (parmText != null) {
			result = new Boolean(parmText).booleanValue();
		}

		return result;
	}

	public int getNumericUrlParameterValueNamed(String pKeyName, int pDefaultValue) {
		int result = pDefaultValue;
		String parmText = getUrlParameterValueNamed(pKeyName);

		if ((parmText != null) && (!parmText.isEmpty())) {
			result = new Integer(parmText).intValue();
		}

		return result;
	}

	protected String getTextFromRequest() {
		final String METHOD_NAME = "getTextFromRequest";

		String result = null;
		BufferedReader reader = null;
		
		try {
			if (this.request.getContentType().startsWith(CONTENT_TYPE_MULTIPART_FORM)) {
				// multipart form, i.e. file upload request, currently single file only.
				Collection<Part> parts = this.request.getParts();
				Iterator<Part> partsIterator = parts.iterator();
				if (partsIterator.hasNext()) {
					Part part = partsIterator.next();
					InputStream partInputStream = part.getInputStream();
					InputStreamReader partInputStreamReader = new InputStreamReader(partInputStream); 
					reader = new BufferedReader(partInputStreamReader);
				}
			} else {
				// normal request.
				reader = this.request.getReader();
			}
			if (reader!=null) {
				result = convertToString(this.wc, reader);

				try {
					result = urlDecode(this.wc, result);
				} catch (IllegalArgumentException e) {
					reportWarning("Error encountered url decoding CE text. Continuing on the assumption it was not url encoded", this.wc);
				}
			}
		} catch (IOException e) {
			reportException(e, this.wc, logger, CLASS_NAME, METHOD_NAME);
		} catch (ServletException e) {
			reportException(e, this.wc, logger, CLASS_NAME, METHOD_NAME);		  
		}
		
		return result;
	}
	
	protected String getCeTextFromRequest() {
		String result = null;

		//First try the named parameter
		result = this.getUrlParameterValueNamed(PARM_CETEXT);

		if ((result == null) || (result.isEmpty())) {
			//If that is not specified try the post body
			result = getTextFromRequest();
		}

		return result;
	}

	protected void reportNotFoundError(String pType, String pId) {
		this.wc.getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_NOT_FOUND);
		this.wc.getWebActionResponse().setHttpErrorMessage(pType + " with identifier '" + pId + "' not found");
	}

	protected void reportUnsupportedMethodError() {
		this.wc.getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	protected void reportUnsupportedFormatError() {
		this.wc.getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
	}

	private static void reportUnsupportedPrimaryError(WebActionContext pWc, String pPrimaryName) {
		pWc.getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		pWc.getWebActionResponse().setHttpErrorMessage("Unsupported primary REST term (" + pPrimaryName + ")");
	}

	protected void reportNotYetImplementedError() {
		reportNotYetImplementedError("");
	}

	protected void reportNotYetImplementedError(String pText) {
		getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		getWebActionResponse().setHttpErrorMessage("Not yet implemented " + pText);
	}

	protected void reportUnhandledUrl() {
		getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		getWebActionResponse().setHttpErrorMessage("Unhandled URL fragment in '" + rebuildUrlFrom(this.restParts) + "'");
	}

	protected void reportUnhandledUrl(ArrayList<String> pRestParts) {
		getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		getWebActionResponse().setHttpErrorMessage("Unhandled URL fragment in '" + rebuildUrlFrom(pRestParts) + "'");
	}

	protected void reportMissingUrlParameterError(String pParmName) {
		getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		getWebActionResponse().setHttpErrorMessage("The mandatory url parameter '" + pParmName + "' is not specified");
	}

	protected void reportGeneralError(String pErrorMsg) {
		getWebActionResponse().setHttpErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		getWebActionResponse().setHttpErrorMessage(pErrorMsg);
	}

	private static String rebuildUrlFrom(ArrayList<String> pRestParts) {
		String result = "";

		for (String thisPart : pRestParts) {
			result += "/" + thisPart;
		}

		return result;
	}
	
	protected void setActionOutcomeAsStructuredResult(String pHeadline) {
		CeStoreJsonObject jsonHeadline = new CeStoreJsonObject();
		jsonHeadline.put(REST_HEADLINE, pHeadline);

		getWebActionResponse().setStructuredResult(jsonHeadline);
		
		//Also add this as a standard message
		getWebActionResponse().addLineToMessage(pHeadline);
	}

	protected void setSingleValueAsStructuredResult(Object pVal) {
		getWebActionResponse().setStructuredResult(CeWebSpecial.generateSingleValueFrom(pVal));
	}

	protected void setSourceListAsStructuredResult(ApiHandler pApiHandler, Collection<CeSource> pSrcList) {
		if ((pApiHandler.isDefaultStyle()) || (pApiHandler.isSummaryStyle())) {
			getWebActionResponse().setStructuredResult(CeWebSource.generateSummaryListFrom(pSrcList));
		} else {
			CeWebSource srcWeb = new CeWebSource(this.wc);
			getWebActionResponse().setStructuredResult(srcWeb.generateFullListFrom(pSrcList));
		}
	}

	protected void setSentenceListAsStructuredResult(Collection<CeSentence> pSenList) {
		CeWebSentence senWeb = new CeWebSentence(this.wc);
		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(senWeb.generateSummaryListFrom(pSenList));
		} else {
			getWebActionResponse().setStructuredResult(senWeb.generateFullListFrom(pSenList));
		}
	}

	protected void setSentenceListPairAsStructuredResult(ArrayList<ArrayList<CeSentence>> pSentencesPair) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();
		CeWebSentence senWeb = new CeWebSentence(this.wc);

		//Add each set of sentences with a different primary/secondary flag
		if (isDefaultStyle() || isSummaryStyle()) {
			senWeb.generateSummaryListUsing(pSentencesPair.get(0), BuilderSentence.SENSOURCE_PRIMARY, jArr);
			senWeb.generateSummaryListUsing(pSentencesPair.get(1), BuilderSentence.SENSOURCE_SECONDARY, jArr);
		} else {
			senWeb.generateFullListUsing(pSentencesPair.get(0), BuilderSentence.SENSOURCE_PRIMARY, jArr);
			senWeb.generateFullListUsing(pSentencesPair.get(1), BuilderSentence.SENSOURCE_SECONDARY, jArr);
		}

		getWebActionResponse().setStructuredResult(jArr);
	}

	protected void setConceptListAsStructuredResult(Collection<CeConcept> pConcepts) {
		CeWebConcept conWeb = new CeWebConcept(this.wc);

		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(conWeb.generateSummaryListJsonFor(pConcepts));
		} else {
			getWebActionResponse().setStructuredResult(conWeb.generateFullListJsonFor(pConcepts));
		}
	}

	protected void setPropertyListAsStructuredResult(Collection<CeProperty> pPropList) {
		CeWebProperty propWeb = new CeWebProperty(this.wc);

		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(propWeb.generateSummaryListJsonFor(pPropList));
		} else {
			getWebActionResponse().setStructuredResult(propWeb.generateFullListJsonFor(pPropList));
		}
	}

	protected void setInstanceListAsStructuredResult(Collection<CeInstance> pInstList) {
		CeWebInstance instWeb = new CeWebInstance(this.wc);
		boolean suppPropTypes = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_SPTS, false);

		if (isDefaultStyle() || isSummaryStyle()) {
			getWebActionResponse().setStructuredResult(instWeb.generateSummaryListJsonFor(pInstList, suppPropTypes));
		} else {
			getWebActionResponse().setStructuredResult(instWeb.generateFullListJsonFor(pInstList, suppPropTypes));
		}
	}

	protected void setRationaleListAsStructuredResult(Collection<CeRationaleReasoningStep> pRsList) {
		CeWebRationaleReasoningStep webRrs = new CeWebRationaleReasoningStep(this.wc);
		getWebActionResponse().setStructuredResult(webRrs.generateListFrom(pRsList));
	}

	protected void setSentenceLoadResults(ContainerSentenceLoadResult pSenStats) {
		boolean suppPropTypes = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_SPTS, false);

		CeWebSpecial webSpec = new CeWebSpecial(this.wc);
		getWebActionResponse().setStructuredResult(webSpec.generateSentenceLoadResultsFrom(pSenStats, suppPropTypes));
	}

	protected void setCeResultAsStructuredResult(ContainerCeResult pCeResult, boolean pReturnInstances) {
		int numSteps = getNumericUrlParameterValueNamed(CeStoreRestApiInstance.PARM_STEPS, -1);
		boolean relInsts = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_RELINSTS, true);
		boolean refInsts = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_REFINSTS, true);
		boolean suppPropTypes = getBooleanParameterNamed(CeStoreRestApiInstance.PARM_SPTS, false);
		String limRelsText = getParameterNamed(CeStoreRestApiInstance.PARM_LIMRELS);
		String[] limRels = null;
		
		if (limRelsText != null) {
			limRels = limRelsText.split(",");
		}

		getWebActionResponse().setStructuredResult(CeWebContainerResult.generateNormalCeQueryResultFrom(this.wc, pCeResult, pReturnInstances, numSteps, relInsts, refInsts, limRels, suppPropTypes));
	}

}