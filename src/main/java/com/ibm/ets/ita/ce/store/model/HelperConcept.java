package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_ATTCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONMOD;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_ENTCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_PROPCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_RELCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_THING;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ICOFN;
import static com.ibm.ets.ita.ce.store.names.CeNames.RANGE_VALUE;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public abstract class HelperConcept {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static boolean isReservedName(String pName) {
		return pName.equals(RANGE_VALUE);
	}

	public static CeConcept thingConcept(ActionContext pAc) {
		return CeConcept.createOrRetrieveConceptNamed(pAc, CON_THING);
	}

	public static boolean hasReservedName(CeConcept pConcept) {
		return isReservedName(pConcept.getConceptName());
	}

	public static ArrayList<String> metaModelConceptNames() {
		//TODO: Replace this method with a better (more dynamic) approach
		ArrayList<String> result = new ArrayList<String>();

		result.add(CON_CON);
		result.add(CON_ENTCON);
		result.add(CON_PROPCON);
		result.add(CON_ATTCON);
		result.add(CON_RELCON);
		result.add(CON_CONMOD);

		return result;
	}

	public static boolean isThing(CeConcept pCon) {
		return (pCon.getConceptName().equals(CON_THING));
	}

	public static boolean hasDefaultIcon(ActionContext pAc, CeConcept pCon) {
		return (generateIconName(pAc, pCon) == null);
	}

	public static String generateIconName(ActionContext pAc, CeConcept pCon) {
		String result = null;

		CeInstance mmInst = pCon.retrieveMetaModelInstance(pAc);

		if (mmInst != null) {
			CePropertyInstance ifnPi = mmInst.getPropertyInstanceNamed(PROP_ICOFN);

			if (ifnPi != null) {
				//Use the value specified for this concept
				result = ifnPi.getSingleOrFirstValue();
			} else {
				//Check the parent concept(s)
				for (CeConcept parentCon : pCon.getDirectParents()) {
					if (result == null) {
						result = HelperConcept.generateIconName(pAc, parentCon);
					}
				}
			}
		}

		return result;
	}

	public static ArrayList<String> listConceptualModelNames(CeConcept pCon) {
		ArrayList<String> result = new ArrayList<String>();

		for (CeConceptualModel thisCm : pCon.getConceptualModels()) {
			result.add(thisCm.getModelName());
		}

		return result;
	}

}
