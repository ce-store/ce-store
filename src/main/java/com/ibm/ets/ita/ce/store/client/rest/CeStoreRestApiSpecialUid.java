package com.ibm.ets.ita.ce.store.client.rest;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_SIZE;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_BATCH;
import static com.ibm.ets.ita.ce.store.names.RestNames.REST_RESET;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.client.web.model.CeWebSpecial;
import com.ibm.ets.ita.ce.store.core.StoreActions;

public class CeStoreRestApiSpecialUid extends CeStoreRestApi {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public CeStoreRestApiSpecialUid(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
	}

	public boolean processRequest() {
		boolean statsInResponse = false;

		if (this.restParts.size() == 2) {
			statsInResponse = processTwoElementUidRequest();
		} else if (this.restParts.size() == 3) {
			statsInResponse = processThreeElementUidRequest();
		} else {
			reportUnhandledUrl();
		}

		return statsInResponse;
	}

	private boolean processTwoElementUidRequest() {
		if (isGet()) {
			//URL = special/uid
			//Get last UID value
			handleShowLastUidValue();
		} else if (isPost()) {
			//URL = special/uid
			//Get next UID value (incrementing counter)
			handleGetNextUidValue();
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private boolean processThreeElementUidRequest() {
		String actionName = this.restParts.get(2);

		if (isPost()) {
			if (actionName.equals(REST_RESET)) {
				//URL = special/uid/reset
				//Reset the UIDs
				handleResetUids();
			} else if (actionName.equals(REST_BATCH)) {
				//URL = special/uid/batch
				//Get a batch of UIDs
				handleGetUidBatch();
			} else {
				reportUnhandledUrl();
			}
		} else {
			reportUnsupportedMethodError();
		}

		return false;
	}

	private void handleShowLastUidValue() {
		if (isJsonRequest()) {
			jsonShowLastUidValue();
		} else if (isTextRequest()) {
			textShowLastUidValue();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonShowLastUidValue() {
		String lastUid = getModelBuilder().showNextUidWithoutIncrementing(this.wc);
		setSingleValueAsStructuredResult(lastUid);
	}

	private void textShowLastUidValue() {
		//TODO: Implement this
		String lastUid = getModelBuilder().showNextUidWithoutIncrementing(this.wc);
		reportNotYetImplementedError("uid value is '" + lastUid + "'");
	}

	private void handleGetNextUidValue() {
		if (isJsonRequest()) {
			jsonGetNextUidValue();
		} else if (isTextRequest()) {
			textGetNextUidValue();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonGetNextUidValue() {
		String nextUid = getModelBuilder().getNextUid(this.wc, null);

		setSingleValueAsStructuredResult(nextUid);
	}

	private void textGetNextUidValue() {
		//TODO: Implement this
		reportNotYetImplementedError();
	}

	private void handleResetUids() {
		if (isJsonRequest()) {
			jsonResetUids();
		} else if (isTextRequest()) {
			textResetUids();
		} else {
			reportUnsupportedFormatError();
		}
	}

	private void jsonResetUids() {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		sa.resetUids();

		getWebActionResponse().addLineToMessage("UIDs have been reset");
	}

	private void textResetUids() {
		//TODO: Implement this
		reportNotYetImplementedError();
	}

	private void handleGetUidBatch() {
		String sizeVal = getUrlParameterValueNamed(PARM_SIZE);

		if ((sizeVal != null) && (!sizeVal.isEmpty())) {
			int size = new Integer(sizeVal).intValue();

			if (size > 0) {
				if (isJsonRequest()) {
					jsonGetUidBatch(size);
				} else if (isTextRequest()) {
					textGetUidBatch(size);
				} else {
					reportUnsupportedFormatError();
				}
			} else {
				reportGeneralError("Non-numeric value (" + sizeVal + ") for size parameter in batch request");
			}
		} else {
			reportMissingUrlParameterError(PARM_SIZE);
		}
	}

	private void jsonGetUidBatch(int pBatchSize) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.wc);
		Properties result = sa.getUidBatch(pBatchSize);

		setUidBatchAsStructuredResult(result, pBatchSize);
	}

	private void textGetUidBatch(int pBatchSize) {
		//TODO: Implement this
		reportNotYetImplementedError("get uid batch using '" + pBatchSize + "'");
	}

	private void setUidBatchAsStructuredResult(Properties pUidProps, long pBatchSize) {
		getWebActionResponse().setStructuredResult(CeWebSpecial.generateUidBatchFrom(pUidProps, pBatchSize));
	}

}
