package com.ibm.ets.ita.ce.store;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class IndexedEntityAccessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_IPC = "identification property concept";
	
	private ModelBuilder mb = null;
	
	private HashMap<String, ArrayList<CeConcept>> cachedConceptsStartingWith = new HashMap<String, ArrayList<CeConcept>>();
	private HashMap<String, ArrayList<CeInstance>> cachedInstancesStartingWith = new HashMap<String, ArrayList<CeInstance>>();
	private HashMap<String, HashMap<String, ArrayList<CeProperty>>> cachedPropertiesStartingWith = new HashMap<String, HashMap<String,ArrayList<CeProperty>>>();
	private HashMap<String, HashMap<String, ArrayList<CeProperty>>> cachedPropertiesNamed = new HashMap<String, HashMap<String,ArrayList<CeProperty>>>();

	public IndexedEntityAccessor(ModelBuilder pMb) {
		this.mb = pMb;
	}

	public ArrayList<CeConcept> calculateConceptsWithNameStarting(String pName) {
		ArrayList<CeConcept> result = this.cachedConceptsStartingWith.get(pName);
		
		if (result == null) {
			//There is nothing in the cached so calculate the values
			result = new ArrayList<CeConcept>();

			for (CeConcept thisCon : this.mb.listAllConcepts()) {
				if (thisCon.getConceptName().startsWith(pName)) {
					result.add(thisCon);
				}
			}

			this.cachedConceptsStartingWith.put(pName, result);
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

			//First check all the identification property values
			for (CeProperty thisProp : this.mb.getAllProperties()) {
				if (thisProp.isStatedProperty()) {
					CeInstance mm = thisProp.getMetaModelInstance(pAc);
					
					if (mm != null) {
						if (mm.isConceptNamed(pAc, CON_IPC)) {
							idProps.add(thisProp);
						}
					}
				}
			}

			//There is nothing in the cached so calculate the values
			result = new ArrayList<CeInstance>();

			//First check all the main instances
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

		//First check all the identification property values
		for (CeProperty thisProp : this.mb.getAllProperties()) {
			if (thisProp.isStatedProperty()) {
				CeInstance mm = thisProp.getMetaModelInstance(pAc);
				
				if (mm != null) {
					if (mm.isConceptNamed(pAc, CON_IPC)) {
						for (CeInstance possInst : this.mb.listAllInstances()) {
							CePropertyInstance thisPi = possInst.getPropertyInstanceForProperty(thisProp);
							
							if (thisPi != null) {
								for (String idVal : thisPi.getValueList()) {
									String lcIdVal = idVal.toLowerCase();
									
									if (lcIdVal.equals(lcTgtName)) {
										if (result != null) {
											reportWarning("More than one instance with identifier '" + pName + "' was found", pAc);
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

		//Finally check for an instance actually named what was specified
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

		//First check all the identification property values
		for (CeProperty thisProp : this.mb.getAllProperties()) {
			if (thisProp.isStatedProperty()) {
				CeInstance mm = thisProp.getMetaModelInstance(pAc);
				
				if (mm != null) {
					if (mm.isConceptNamed(pAc, CON_IPC)) {
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

		//Finally check for an instance actually named what was specified
		CeInstance actualInst = this.mb.getInstanceNamed(pAc, pName);

		if (actualInst != null) {
			result.add(actualInst);
		}

		return result;
	}

	public void clearPropertyNameCaches() {
		this.cachedPropertiesNamed = new HashMap<String, HashMap<String,ArrayList<CeProperty>>>();
		this.cachedPropertiesStartingWith = new HashMap<String, HashMap<String,ArrayList<CeProperty>>>();
	}

	public ArrayList<CeProperty> calculatePropertiesStartingWith(CeConcept pDomainCon, String pPropName) {
		ArrayList<CeProperty> result = null;

		if (pDomainCon != null) {
			//This is a normal domain concept so check the "real" properties in the model
			HashMap<String, ArrayList<CeProperty>> propMap = this.cachedPropertiesStartingWith.get(pDomainCon.getConceptName());

			if (propMap != null) {
				result = propMap.get(pPropName);
			} else {
				//Create a new prop map in the cache
				propMap = new HashMap<String, ArrayList<CeProperty>>();
				this.cachedPropertiesStartingWith.put(pDomainCon.getConceptName(), propMap);
				result = propMap.get(pPropName);
			}
			
			if (result == null) {
				//There is nothing in the cached so calculate the values
				result = new ArrayList<CeProperty>();

				for (CeProperty thisProp : pDomainCon.retrieveAllNamedProperties()) {
					if (thisProp.getPropertyName().startsWith(pPropName)) {
						result.add(thisProp);
					}
				}
				
				propMap.put(pPropName, result);
			}
		} else {
			//The domain is null which means that the properties to be checked are the
			//"special" query/rule operator properties ("=", "contains" etc).
			
			//TODO: Complete this
			result = new ArrayList<CeProperty>();
		}

		return result;
	}
	
	public ArrayList<CeProperty> calculatePropertiesNamed(CeConcept pDomainCon, String pPropName) {
		ArrayList<CeProperty> result = null;

		if (pDomainCon != null) {
			//This is a normal domain concept so check the "real" properties in the model
			HashMap<String, ArrayList<CeProperty>> propMap = this.cachedPropertiesNamed.get(pDomainCon.getConceptName());
			
			if (propMap != null) {
				result = propMap.get(pPropName);
			} else {
				//Create a new prop map in the cache
				propMap = new HashMap<String, ArrayList<CeProperty>>();
				this.cachedPropertiesNamed.put(pDomainCon.getConceptName(), propMap);
				result = propMap.get(pPropName);
			}
			
			if (result == null) {
				//There is nothing in the cached so calculate the values
				result = new ArrayList<CeProperty>();
		
				for (CeProperty thisProp : pDomainCon.retrieveAllNamedProperties()) {
					if (thisProp.getPropertyName().equals(pPropName)) {
						result.add(thisProp);
					}
				}
				
				propMap.put(pPropName, result);
			}
		} else {
			//The domain is null which means that the properties to be checked are the
			//"special" query/rule operator properties ("=", "contains" etc).
			
			//TODO: Complete this
			result = new ArrayList<CeProperty>();
		}

		return result;
	}

}