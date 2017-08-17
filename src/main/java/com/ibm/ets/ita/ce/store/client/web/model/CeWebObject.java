package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ANNOTATIONS;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_INT;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_FLT;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_DBL;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_BLN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeAnnotation;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeModelEntity;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public abstract class CeWebObject {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CLASS_NAME = CeWebObject.class.getName();
	private static final String PACKAGE_NAME = CeWebObject.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	protected ActionContext ac = null;

	// TODO: Review this when complete - e.g. move methods from static to
	// instance

	public CeWebObject(ActionContext pAc) {
		this.ac = pAc;
	}

	protected static void addStringValueTo(CeStoreJsonArray pArr, String pVal) {
		if ((pVal != null) && (!pVal.isEmpty())) {
			pArr.add(pVal);
		}
	}

	protected static void addObjectValueTo(CeStoreJsonArray pArr, CeStoreJsonObject pVal) {
		if (pVal != null) {
			pArr.add(pVal);
		}
	}

	protected static void addAllStringValuesTo(CeStoreJsonArray pArr, ArrayList<String> pVals) {
		if (pVals != null) {
			pArr.addAll(pVals);
		}
	}

	protected static void putStringValueIn(CeStoreJsonObject pObj, String pKey, String pVal) {
		if ((pVal != null) && (!pVal.isEmpty())) {
			pObj.put(pKey, pVal);
		}
	}

	protected static void putAppropriateValueIn(ActionContext pAc, CePropertyInstance pPi, CeStoreJsonObject pObj, String pKey, String pVal, boolean pIsSmartMode) {
		final String METHOD_NAME = "putAppropriateValueIn";

		if (pIsSmartMode) {
			if (isInteger(pAc, pPi)) {
				try {
					int intVal = new Integer(pVal).intValue();

					putIntValueIn(pObj, pKey, intVal);
				} catch (NumberFormatException e) {
					reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
				}
			} else if (isFloat(pAc, pPi)) {
				try {
					float floatVal = new Float(pVal).floatValue();

					putFloatValueIn(pObj, pKey, floatVal);
				} catch (NumberFormatException e) {
					reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
				}
			} else if (isDouble(pAc, pPi)) {
				try {
					double doubleVal = new Double(pVal).doubleValue();

					putDoubleValueIn(pObj, pKey, doubleVal);
				} catch (NumberFormatException e) {
					reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
				}
			} else if (isBoolean(pAc, pPi)) {
				try {
					boolean boolVal = new Boolean(pVal).booleanValue();

					putBooleanValueIn(pObj, pKey, boolVal);
				} catch (NumberFormatException e) {
					reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
				}
			} else {
				putStringValueIn(pObj, pKey, pVal);
			}
		} else {
			putStringValueIn(pObj, pKey, pVal);
		}
	}

	protected static void putObjectValueIn(CeStoreJsonObject pObj, String pKey, Object pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putBooleanValueIn(CeStoreJsonObject pObj, String pKey, boolean pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putIntValueIn(CeStoreJsonObject pObj, String pKey, int pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putLongValueIn(CeStoreJsonObject pObj, String pKey, long pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putFloatValueIn(CeStoreJsonObject pObj, String pKey, float pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putDoubleValueIn(CeStoreJsonObject pObj, String pKey, double pVal) {
		pObj.put(pKey, pVal);
	}

	protected static void putArrayValueIn(CeStoreJsonObject pObj, String pKey, CeStoreJsonArray pVal) {
		if ((pVal != null)) {
			pObj.put(pKey, pVal);
		}
	}

	protected static void putObjectValueIn(CeStoreJsonObject pObj, String pKey, CeStoreJsonObject pVal) {
		if ((pVal != null)) {
			pObj.put(pKey, pVal);
		}
	}

	protected static void putAllStringValuesIn(CeStoreJsonObject pObj, String pKey, Collection<String> pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (String thisVal : pVals) {
				jArr.add(thisVal);
			}
			pObj.put(pKey, jArr);
		}
	}

	protected static void putAllIntValuesIn(CeStoreJsonObject pObj, String pKey, Collection<Integer> pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (Integer thisVal : pVals) {
				jArr.add(thisVal.intValue());
			}
			pObj.put(pKey, jArr);
		}
	}

	protected static void putAllFloatValuesIn(CeStoreJsonObject pObj, String pKey, Collection<Float> pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (Float thisVal : pVals) {
				jArr.add(thisVal.floatValue());
			}
			pObj.put(pKey, jArr);
		}
	}

	protected static void putAllDoubleValuesIn(CeStoreJsonObject pObj, String pKey, Collection<Double> pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (Double thisVal : pVals) {
				jArr.add(thisVal.doubleValue());
			}
			pObj.put(pKey, jArr);
		}
	}

	protected static void putAllBooleanValuesIn(CeStoreJsonObject pObj, String pKey, Collection<Boolean> pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (Boolean thisVal : pVals) {
				jArr.add(thisVal.booleanValue());
			}
			pObj.put(pKey, jArr);
		}
	}

	protected static void putAllStringValuesIn(CeStoreJsonObject pObj, String pKey, String[] pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jArr = new CeStoreJsonArray();

			for (String thisVal : pVals) {
				jArr.add(thisVal);
			}
			pObj.put(pKey, jArr);
		}
	}

	private static boolean isInteger(ActionContext pAc, CePropertyInstance pPi) {
		boolean result = false;
		CeConcept rangeCon = pPi.getRelatedProperty().getRangeConcept();

		if (rangeCon != null) {
			if (rangeCon.equalsOrHasParentNamed(pAc, CON_INT)) {
				result = true;
			}
		}

		return result;
	}

	private static boolean isFloat(ActionContext pAc, CePropertyInstance pPi) {
		boolean result = false;
		CeConcept rangeCon = pPi.getRelatedProperty().getRangeConcept();

		if (rangeCon != null) {
			if (rangeCon.equalsOrHasParentNamed(pAc, CON_FLT)) {
				result = true;
			}
		}

		return result;
	}

	private static boolean isDouble(ActionContext pAc, CePropertyInstance pPi) {
		boolean result = false;
		CeConcept rangeCon = pPi.getRelatedProperty().getRangeConcept();

		if (rangeCon != null) {
			if (rangeCon.equalsOrHasParentNamed(pAc, CON_DBL)) {
				result = true;
			}
		}

		return result;
	}

	private static boolean isBoolean(ActionContext pAc, CePropertyInstance pPi) {
		boolean result = false;
		CeConcept rangeCon = pPi.getRelatedProperty().getRangeConcept();

		if (rangeCon != null) {
			if (rangeCon.equalsOrHasParentNamed(pAc, CON_BLN)) {
				result = true;
			}
		}

		return result;
	}

	protected static void putAllAppropriateValuesIn(ActionContext pAc, CePropertyInstance pPi, CeStoreJsonObject pObj, String pKey, String[] pVals, boolean pIsSmartMode) {
		final String METHOD_NAME = "putAllAppropriateValuesIn";

		if (pIsSmartMode) {
			if(isInteger(pAc, pPi)) {
				ArrayList<Integer> intVals = new ArrayList<Integer>();

				for (String thisVal : pVals) {
					try {
						Integer intVal = new Integer(thisVal);

						intVals.add(intVal);
					} catch (NumberFormatException e) {
						reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}

				putAllIntValuesIn(pObj, pKey, intVals);
			} else if(isFloat(pAc, pPi)) {
				ArrayList<Float> floatVals = new ArrayList<Float>();

				for (String thisVal : pVals) {
					try {
						float floatVal = new Float(thisVal);

						floatVals.add(floatVal);
					} catch (NumberFormatException e) {
						reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}

				putAllFloatValuesIn(pObj, pKey, floatVals);
			} else if(isDouble(pAc, pPi)) {
				ArrayList<Double> doubleVals = new ArrayList<Double>();

				for (String thisVal : pVals) {
					try {
						double doubleVal = new Double(thisVal);

						doubleVals.add(doubleVal);
					} catch (NumberFormatException e) {
						reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}

				putAllDoubleValuesIn(pObj, pKey, doubleVals);
			} else if(isBoolean(pAc, pPi)) {
				ArrayList<Boolean> boolVals = new ArrayList<Boolean>();

				for (String thisVal : pVals) {
					try {
						boolean boolVal = new Boolean(thisVal);

						boolVals.add(boolVal);
					} catch (NumberFormatException e) {
						reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}

				putAllBooleanValuesIn(pObj, pKey, boolVals);
			} else {
				putAllStringValuesIn(pObj, pKey, pVals);
			}
		} else {
			putAllStringValuesIn(pObj, pKey, pVals);
		}
	}

	protected static void putAllAppropriateValuesIn(ActionContext pAc, CePropertyInstance pPi, CeStoreJsonObject pObj, String pKey, HashSet<String> pVals, boolean pIsSmartMode) {
		final String METHOD_NAME = "putAllAppropriateValuesIn";

		if (pIsSmartMode) {
			if (isInteger(pAc, pPi)) {
				ArrayList<Integer> intVals = new ArrayList<Integer>();

				for (String thisVal : pVals) {
					try {
						Integer intVal = new Integer(thisVal);

						intVals.add(intVal);
					} catch (NumberFormatException e) {
						reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}

				putAllIntValuesIn(pObj, pKey, intVals);
			} else if (isFloat(pAc, pPi)) {
				ArrayList<Float> floatVals = new ArrayList<Float>();

				for (String thisVal : pVals) {
					try {
						float floatVal = new Float(thisVal);

						floatVals.add(floatVal);
					} catch (NumberFormatException e) {
						reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}

				putAllFloatValuesIn(pObj, pKey, floatVals);
			} else if (isDouble(pAc, pPi)) {
				ArrayList<Double> doubleVals = new ArrayList<Double>();

				for (String thisVal : pVals) {
					try {
						double doubleVal = new Double(thisVal);

						doubleVals.add(doubleVal);
					} catch (NumberFormatException e) {
						reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}

				putAllDoubleValuesIn(pObj, pKey, doubleVals);
			} else if (isBoolean(pAc, pPi)) {
				ArrayList<Boolean> boolVals = new ArrayList<Boolean>();

				for (String thisVal : pVals) {
					try {
						boolean boolVal = new Boolean(thisVal);

						boolVals.add(boolVal);
					} catch (NumberFormatException e) {
						reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}

				putAllBooleanValuesIn(pObj, pKey, boolVals);
			} else {
				putAllStringValuesIn(pObj, pKey, pVals);
			}
		} else {
			putAllStringValuesIn(pObj, pKey, pVals);
		}
	}

	protected static void putAllArrayStringValuesIn(CeStoreJsonObject pObj, String pKey,
			ArrayList<ArrayList<String>> pVals) {
		if ((pVals != null)) {
			CeStoreJsonArray jOuterArr = new CeStoreJsonArray();

			for (ArrayList<String> thisArr : pVals) {
				CeStoreJsonArray jInnerArr = new CeStoreJsonArray();
				for (String thisVal : thisArr) {
					jInnerArr.add(thisVal);
				}
				jOuterArr.add(jInnerArr);
			}
			pObj.put(pKey, jOuterArr);
		}
	}

	protected static CeStoreJsonObject createJsonObjectsFor(LinkedHashMap<String, String> pLhm) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		for (String thisKey : pLhm.keySet()) {
			putStringValueIn(jObj, thisKey, pLhm.get(thisKey));
		}

		return jObj;
	}

	public static CeStoreJsonObject generateStandardAlertsFrom(LinkedHashMap<String, ArrayList<String>> pAlerts) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (!pAlerts.isEmpty()) {
			for (String thisKey : pAlerts.keySet()) {
				putAllStringValuesIn(jObj, thisKey, pAlerts.get(thisKey));
			}
		}

		return jObj;
	}

	public static CeStoreJsonArray generateStandardMessagesFrom(ArrayList<String> pMessages) {
		CeStoreJsonArray jArr = new CeStoreJsonArray();

		if (!pMessages.isEmpty()) {
			addAllStringValuesTo(jArr, pMessages);
		}

		return jArr;
	}

	public static CeStoreJsonObject generateStandardStatsFrom(LinkedHashMap<String, String> pStats) {
		CeStoreJsonObject jObj = new CeStoreJsonObject();

		if (!pStats.isEmpty()) {
			for (String thisKey : pStats.keySet()) {
				putStringValueIn(jObj, thisKey, pStats.get(thisKey));
			}
		}

		return jObj;
	}

	protected static void processAnnotations(CeModelEntity pObj, CeStoreJsonObject pJsonObj) {
		CeStoreJsonObject annoObj = new CeStoreJsonObject();

		for (CeAnnotation thisAnno : pObj.getAnnotations()) {
			putStringValueIn(annoObj, thisAnno.trimmedLabel(), thisAnno.getText());
		}

		if (!annoObj.isEmpty()) {
			putObjectValueIn(pJsonObj, JSON_ANNOTATIONS, annoObj);
		}
	}

}
