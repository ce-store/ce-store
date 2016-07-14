package com.ibm.ets.ita.ce.store.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.removeCrs;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.concurrent.atomic.AtomicInteger;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public abstract class DataHandler extends RequestHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static AtomicInteger debugCtr = new AtomicInteger(0);
	private CeSource tgtSrc = null;
	protected Long startTime = null;

	public abstract ContainerSentenceLoadResult processData(String pUserName, String pHint, boolean pGenModel, String pJsonText);
	protected abstract String getHandlerName();

	public DataHandler(ActionContext pAc, Long pStartTime) {
		super(pAc);
		debugCtr = new AtomicInteger(0);
		this.startTime = pStartTime;
	}

	private void getSource(String pHint) {
		this.tgtSrc = this.ac.getModelBuilder().getOrCreateSourceByDetail(this.ac, calculateSourceName(pHint));
	}

	protected String calculateSourceName(String pHint) {
		return getHandlerName() + ":" + pHint;
	}

	protected ContainerSentenceLoadResult dealWithResult(String pCeText, String pHint, boolean pGenModel) {
		ContainerSentenceLoadResult result = ContainerSentenceLoadResult.createWithZeroValues(calculateSourceName(pHint));

		if (!pCeText.isEmpty()) {
			if (pGenModel) {
				//Model CE gets returned to the user (not saved to the store)
				this.ac.getActionResponse().addLineToMessage(pCeText);

			} else {
				//Normal CE gets saved to the store
				result = saveGeneratedCe(pCeText, pHint);
			}
		} else {
			reportError("OBO conversion did not generate any CE text", this.ac);
		}

		return result;
	}

	protected ContainerSentenceLoadResult saveGeneratedCe(String pCeText, String pHint) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);

		getSource(pHint);

		return sa.saveCeText(pCeText, this.tgtSrc);
	}

	protected void debug(String pMsgText) {
		String debugText = "[Inv " + debugCtr.incrementAndGet() + "] " + pMsgText;
		reportDebug(debugText, this.ac);
	}

	protected void debugSummary(String pCeText) {
		String msgText = "";

		if (this.tgtSrc != null) {
			msgText += "tgtSrc=" + this.tgtSrc.getId() + ", ";
		}

		if (pCeText != null) {
			msgText += "ceText=" + removeCrs(pCeText);
		}

		debug(msgText);
	}
}