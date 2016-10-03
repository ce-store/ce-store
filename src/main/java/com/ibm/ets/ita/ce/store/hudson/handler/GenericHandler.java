package com.ibm.ets.ita.ce.store.hudson.handler;

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CC;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.hudson.model.ConvConfig;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public abstract class GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected static final int DEFAULT_CONF = 100;
	protected static final int ERR_CONF = 0;

	protected ActionContext ac = null;
	private ConvConfig cc = null;
	protected long startTime = -1;

	public GenericHandler(ActionContext pAc, long pStartTime) {
		this.ac = pAc;
		this.startTime = pStartTime;
	}

	protected ConvConfig getConvConfig() {
		if (this.cc == null) {
			ModelBuilder mb = this.ac.getModelBuilder();

			if (mb != null) {
				for (CeInstance ccInst : mb.getAllInstancesForConceptNamed(this.ac, CON_CC)) {
					if (this.cc == null) {
						this.cc = ConvConfig.createUsing(this.ac, ccInst);
					} else {
						reportError("Error: more than one instance of " + CON_CC + " defined: " + ccInst.getInstanceName(), this.ac);
					}
				}
			}
		}

		if (this.cc == null) {
			reportDebug("No " + CON_CC + " instance found", this.ac);
		}

		return this.cc;
	}

	public static ContainerSentenceLoadResult saveCeText(ActionContext pAc, String pCeText, CeSource pSrc) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
		ContainerSentenceLoadResult result = sa.saveCeText(pCeText, null);

		//Clear the various caches
		ServletStateManager.getHudsonManager(pAc).clearCaches(pAc);

		return result;
	}

}
