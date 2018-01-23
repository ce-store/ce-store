package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CC;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_IDPROPCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_SEPIDCON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.hudson.model.ConvConfig;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public abstract class GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

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
		ContainerSentenceLoadResult result = sa.saveCeText(pCeText, null, false);

		//Clear the various caches
		ServletStateManager.getHudsonManager().clearCaches(pAc);

		return result;
	}

	public ArrayList<String> getInstanceIdentifiersFor(CeInstance pInst, ActionContext pAc) {
		ArrayList<String> result = new ArrayList<String>();

		//Only add the instance name if no other identifiers found
		if (!isSeparatelyIdentified(pInst, pAc)) {
			result.add(pInst.getInstanceName());
		}

		for (CePropertyInstance thisPi : pInst.getPropertyInstances()) {
			CeProperty relProp = thisPi.getRelatedProperty();
			CeInstance mmInst = relProp.getMetaModelInstance(pAc);

			if (mmInst != null) {
				if (mmInst.isConceptNamed(pAc, CON_IDPROPCON)) {
					for (String thisVal : thisPi.getValueList()) {
						result.add(thisVal);
					}
				}
			}
		}

		//If there are no identifiers found then add the instance name
		if (result.isEmpty()) {
			result.add(pInst.getInstanceName());
		}

		return result;
	}

	private boolean isSeparatelyIdentified(CeInstance pInst, ActionContext pAc) {
		boolean result = false;

		for (CeConcept dirCon : pInst.getDirectConcepts()) {
			ArrayList<CeInstance> mmList = dirCon.retrieveMetaModelInstances(pAc, null);

			if (mmList != null) {
				for (CeInstance mmInst : mmList) {
					if (mmInst.isConceptNamed(pAc, CON_SEPIDCON)) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}

}
