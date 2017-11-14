package com.ibm.ets.ita.ce.store.core;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_IDPROPCON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class IndexedEntityAccessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private ModelBuilder mb = null;

	private HashMap<String, ArrayList<CeConcept>> cachedConceptsStartingWith = null;
	private HashMap<String, ArrayList<CeInstance>> cachedInstancesStartingWith = null;
	private HashMap<String, HashMap<String, ArrayList<CeProperty>>> cachedPropertiesStartingWith = null;
	private HashMap<String, HashMap<String, ArrayList<CeProperty>>> cachedPropertiesNamed = null;

	public IndexedEntityAccessor(ModelBuilder pMb) {
		this.mb = pMb;

		this.cachedConceptsStartingWith = new HashMap<String, ArrayList<CeConcept>>();
		this.cachedInstancesStartingWith = new HashMap<String, ArrayList<CeInstance>>();
		this.cachedPropertiesStartingWith = new HashMap<String, HashMap<String, ArrayList<CeProperty>>>();
		this.cachedPropertiesNamed = new HashMap<String, HashMap<String, ArrayList<CeProperty>>>();
	}

	public ArrayList<CeConcept> calculateConceptsWithNameStarting(ActionContext pAc, String pName) {
		ArrayList<CeConcept> result = null;
		String tgtName = null;

		if (pAc.getCeConfig().isCaseSensitive()) {
			tgtName = pName;
		} else {
			tgtName = pName.toLowerCase();
		}

		result = this.cachedConceptsStartingWith.get(tgtName);

		if (result == null) {
			// There is nothing in the cached so calculate the values
			result = new ArrayList<CeConcept>();

			for (CeConcept thisCon : this.mb.listAllConcepts()) {
				String conName = null;

				if (pAc.getCeConfig().isCaseSensitive()) {
					conName = thisCon.getConceptName();
				} else {
					conName = thisCon.getConceptName().toLowerCase();
				}

				if (conName.startsWith(tgtName)) {
					result.add(thisCon);
				}
			}

			this.cachedConceptsStartingWith.put(tgtName, result);
		}

		return result;
	}

	public void clearConceptNameCache() {
		this.cachedConceptsStartingWith = new HashMap<String, ArrayList<CeConcept>>();
	}

	public ArrayList<CeInstance> calculateInstancesWithNameStarting(ActionContext pAc, String pName) {
		String lcTgtName = pName.toLowerCase();
		ArrayList<CeInstance> result = null;

		if (this.cachedInstancesStartingWith.containsKey(lcTgtName)) {
			result = this.cachedInstancesStartingWith.get(lcTgtName);
		} else {
			result = new ArrayList<CeInstance>();

			HashSet<CeProperty> idProps = new HashSet<CeProperty>();

			// First check all the identification property values
			for (CeProperty thisProp : this.mb.getAllProperties()) {
				if (thisProp.isStatedProperty()) {
					CeInstance mm = thisProp.getMetaModelInstance(pAc);

					if (mm != null) {
						if (mm.isConceptNamed(pAc, CON_IDPROPCON)) {
							idProps.add(thisProp);
						}
					}
				}
			}

			// There is nothing in the cached so calculate the values
			result = new ArrayList<CeInstance>();

			// First check all the main instances
			for (CeInstance thisInst : this.mb.listAllInstances()) {
				String lcInstName = thisInst.getInstanceName().toLowerCase();
				if (lcInstName.startsWith(lcTgtName)) {
					result.add(thisInst);
				} else {
					for (CeProperty idProp : idProps) {
						CePropertyInstance idPi = thisInst.getPropertyInstanceForProperty(idProp);

						if (idPi != null) {
							for (String idVal : idPi.getValueList()) {
								String lcVal = idVal.toLowerCase();

								if (lcVal.startsWith(lcTgtName)) {
									result.add(thisInst);
								}
							}
						}
					}
				}
			}

			this.cachedInstancesStartingWith.put(lcTgtName, result);
		}

		return result;
	}

	public void clearInstanceNameCache() {
		this.cachedInstancesStartingWith = new HashMap<String, ArrayList<CeInstance>>();
	}

	public CeInstance getInstanceNamedOrIdentifiedAs(ActionContext pAc, String pName) {
		CeInstance result = null;
		String lcTgtName = pName.toLowerCase();

		// First check all the identification property values
		for (CeProperty thisProp : this.mb.getAllProperties()) {
			if (thisProp.isStatedProperty()) {
				CeInstance mm = thisProp.getMetaModelInstance(pAc);

				if (mm != null) {
					if (mm.isConceptNamed(pAc, CON_IDPROPCON)) {
						for (CeInstance possInst : this.mb.listAllInstances()) {
							CePropertyInstance thisPi = possInst.getPropertyInstanceForProperty(thisProp);

							if (thisPi != null) {
								for (String idVal : thisPi.getValueList()) {
									String lcIdVal = idVal.toLowerCase();

									if (lcIdVal.equals(lcTgtName)) {
										if (result != null) {
											reportWarning(
													"More than one instance with identifier '" + pName + "' was found",
													pAc);
										}
										result = possInst;
									}
								}
							}
						}
					}
				}
			}
		}

		// Finally check for an instance actually named what was specified
		CeInstance actualInst = this.mb.getInstanceNamed(pAc, pName);

		if (actualInst != null) {
			if (result != null) {
				reportWarning("More than one instance with identifier '" + pName + "' was found", pAc);
			}

			result = actualInst;
		}

		return result;
	}

	public ArrayList<CeInstance> getInstancesNamedOrIdentifiedAs(ActionContext pAc, String pName) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		String lcTgtName = pName.toLowerCase();

		// First check all the identification property values
		for (CeProperty thisProp : this.mb.getAllProperties()) {
			if (thisProp.isStatedProperty()) {
				CeInstance mm = thisProp.getMetaModelInstance(pAc);

				if (mm != null) {
					if (mm.isConceptNamed(pAc, CON_IDPROPCON)) {
						for (CeInstance possInst : this.mb.listAllInstances()) {
							CePropertyInstance thisPi = possInst.getPropertyInstanceForProperty(thisProp);

							if (thisPi != null) {
								for (String idVal : thisPi.getValueList()) {
									String lcIdVal = idVal.toLowerCase();

									if (lcIdVal.equals(lcTgtName)) {
										result.add(possInst);
									}
								}
							}
						}
					}
				}
			}
		}

		// Finally check for an instance actually named what was specified
		CeInstance actualInst = this.mb.getInstanceNamed(pAc, pName);

		if (actualInst != null) {
			if (!result.contains(actualInst)) {
				result.add(actualInst);
			}
		}

		return result;
	}

	public void clearPropertyNameCaches() {
		this.cachedPropertiesNamed = new HashMap<String, HashMap<String, ArrayList<CeProperty>>>();
		this.cachedPropertiesStartingWith = new HashMap<String, HashMap<String, ArrayList<CeProperty>>>();
	}

	public ArrayList<CeProperty> calculatePropertiesStartingWith(CeConcept pDomainCon, String pPropName) {
		ArrayList<CeProperty> result = null;

		if (pDomainCon != null) {
			// This is a normal domain concept so check the 'real' properties in
			// the model
			HashMap<String, ArrayList<CeProperty>> propMap = this.cachedPropertiesStartingWith
					.get(pDomainCon.getConceptName());

			if (propMap != null) {
				result = propMap.get(pPropName);
			} else {
				// Create a new prop map in the cache
				propMap = new HashMap<String, ArrayList<CeProperty>>();
				this.cachedPropertiesStartingWith.put(pDomainCon.getConceptName(), propMap);
				result = propMap.get(pPropName);
			}

			if (result == null) {
				// There is nothing in the cached so calculate the values
				result = new ArrayList<CeProperty>();

				for (CeProperty thisProp : pDomainCon.retrieveAllNamedProperties()) {
					if (thisProp.getPropertyName().startsWith(pPropName)) {
						result.add(thisProp);
					}
				}

				propMap.put(pPropName, result);
			}
		} else {
			// The domain is null which means that the properties to be checked
			// are the
			// 'special' query/rule operator properties ('=', 'contains' etc).

			// TODO: Complete this
			result = new ArrayList<CeProperty>();
		}

		return result;
	}

	public ArrayList<CeProperty> calculatePropertiesNamed(CeConcept pDomainCon, String pPropName) {
		ArrayList<CeProperty> result = null;

		if (pDomainCon != null) {
			// This is a normal domain concept so check the 'real' properties in
			// the model
			HashMap<String, ArrayList<CeProperty>> propMap = this.cachedPropertiesNamed
					.get(pDomainCon.getConceptName());

			if (propMap != null) {
				result = propMap.get(pPropName);
			} else {
				// Create a new prop map in the cache
				propMap = new HashMap<String, ArrayList<CeProperty>>();
				this.cachedPropertiesNamed.put(pDomainCon.getConceptName(), propMap);
				result = propMap.get(pPropName);
			}

			if (result == null) {
				// There is nothing in the cached so calculate the values
				result = new ArrayList<CeProperty>();

				for (CeProperty thisProp : pDomainCon.retrieveAllNamedProperties()) {
					if (thisProp.getPropertyName().equals(pPropName)) {
						result.add(thisProp);
					}
				}

				propMap.put(pPropName, result);
			}
		} else {
			// The domain is null which means that the properties to be checked
			// are the
			// 'special' query/rule operator properties ('=', 'contains' etc).

			// TODO: Complete this
			result = new ArrayList<CeProperty>();
		}

		return result;
	}

}
