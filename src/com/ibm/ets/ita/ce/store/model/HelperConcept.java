package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;

public abstract class HelperConcept {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	//TODO: Try to make this private
	public static final String NAME_THING = "thing";

	protected static final String PROP_LAPN = "label property name";
	protected static final String PROP_IPN = "icon property name";
	protected static final String PROP_IFN = "icon file name";

	private static final String MMCON_CON = "concept";
	private static final String MMCON_ENTCON = "entity concept";
	private static final String MMCON_PROPCON = "property concept";
	private static final String MMCON_ATTCON = "attribute concept";
	private static final String MMCON_RELCON = "relation concept";
	private static final String MMCON_CONMOD = "conceptual model";
	private static final String NAME_MM8 = "renderable concept";

	//These can now be removed from the server code as they are dealt with on the client
	private static final String ICON_SUFFIX = ".png";
	private static final String DEFAULT_ICONNAME = "marker" + ICON_SUFFIX;

//	private static final ArrayList<String> resConNames = new ArrayList<String>();
//
//	static {
////		resConNames.add("ce *");
////		resConNames.add("cex *");
////		resConNames.add("sup *");
//		resConNames.add("value");
////		resConNames.add("value *");
////		resConNames.add("* value");
////		resConNames.add("* props");
//	}

	private static boolean isReservedName(String pName) {
//		boolean result = false;
//
//		for (String thisResName : resConNames) {
//			if (thisResName.endsWith("*")) {
//				String thisRootForm = thisResName.replace("*", "");
//				if (pName.startsWith(thisRootForm)) {
//					result = true;
//				}
//			} else if (thisResName.startsWith("*")) {
//					String thisRootForm = thisResName.replace("*", "");
//					if (pName.endsWith(thisRootForm)) {
//						result = true;
//					}
//			} else {
//				if (pName.equals(thisResName)) {
//					result = true;
//				}
//			}
//		}

		//DSB 15/10/2014 - Significantly simplified.  Now only "value" is a reserved name
		return pName.equals("value");
	}

	public static CeConcept thingConcept(ActionContext pAc) {
		return CeConcept.createOrRetrieveConceptNamed(pAc, NAME_THING);
	}

	public static boolean hasReservedName(CeConcept pConcept) {
		return isReservedName(pConcept.getConceptName());
	}

	public static ArrayList<String> metaModelConceptNames() {
		//TODO: Replace this method with a better (more dynamic) approach
		ArrayList<String> result = new ArrayList<String>();

		result.add(MMCON_CON);
		result.add(MMCON_ENTCON);
		result.add(MMCON_PROPCON);
		result.add(MMCON_ATTCON);
		result.add(MMCON_RELCON);
		result.add(MMCON_CONMOD);
		result.add(NAME_MM8);

		return result;
	}

	public static boolean isCoreConcept(CeConcept pCon) {
		return (pCon.getConceptName().equals(NAME_THING));
	}

	public static boolean hasDefaultIcon(ActionContext pAc, CeConcept pCon) {
		//TODO: Make this more efficient than just calling the name generator and testing the result
		return (generateIconName(pAc, pCon) == DEFAULT_ICONNAME);
	}

	public static String generateIconName(ActionContext pAc, CeConcept pCon) {
		String result = DEFAULT_ICONNAME;

		CeInstance rcInst = pAc.getModelBuilder().getInstanceNamed(pAc, pCon.getConceptName());

		if (rcInst != null) {
			CePropertyInstance ifnPi = rcInst.getPropertyInstanceNamed(PROP_IFN);

			if (ifnPi != null) {
				//Use the value specified for this concept
				result = ifnPi.getSingleOrFirstValue();
			} else {
				//Check the parent concept(s)
				for (CeConcept parentCon : pCon.getDirectParents()) {
					if (result.equals(DEFAULT_ICONNAME)) {
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
