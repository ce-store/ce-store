package com.ibm.ets.ita.ce.store.core;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class InstanceRepository implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private ConcurrentHashMap<String, CeInstance> allInstances = null;
	private ConcurrentHashMap<CeConcept, CopyOnWriteArrayList<CeInstance>> instancesByConcept = null;

	protected InstanceRepository() {
		this.allInstances = new ConcurrentHashMap<String, CeInstance>();
		this.instancesByConcept = new ConcurrentHashMap<CeConcept, CopyOnWriteArrayList<CeInstance>>();
	}

	public ConcurrentHashMap<String, CeInstance> getAllInstances() {
		return this.allInstances;
	}

	public ArrayList<CeInstance> listAllInstances() {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>(this.allInstances.values());
		Collections.sort(result);

		return result;
	}

	public CeInstance getInstanceNamed(ActionContext pAc, String pInstName) {
		CeInstance result = null;

		if (pInstName != null) {
			if (this.allInstances != null) {
				result = this.allInstances.get(getKeyNameFor(pAc, pInstName));
			}
		}

		return result;
	}

	public ArrayList<CeInstance> getAllInstancesForConcept(CeConcept pConcept) {
		ArrayList<CeInstance> result = null;

		if (pConcept != null) {
			if (pConcept.isThing()) {
				result = new ArrayList<CeInstance>(this.allInstances.values());
			} else {
				if (this.instancesByConcept != null) {
					CopyOnWriteArrayList<CeInstance> instList = this.instancesByConcept.get(pConcept);

					if (instList != null) {
						result = new ArrayList<CeInstance>(instList);
					}
				}
			}

			if (result != null) {
				Collections.sort(result);
			}
		}

		// Ensure that an empty list rather than null is returned
		if (result == null) {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	public ArrayList<CeInstance> getAllInstancesForConceptCreatedSince(CeConcept pConcept, long pSinceTs) {
		ArrayList<CeInstance> result = null;

		if (pConcept != null) {
			result = new ArrayList<CeInstance>();

			for (CeInstance thisInst : getAllInstancesForConcept(pConcept)) {
				if (thisInst.getCreationDate() > pSinceTs) {
					result.add(thisInst);
				}
			}

			Collections.sort(result);
		}

		// Ensure that an empty list rather than null is returned
		if (result == null) {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	public ArrayList<CeInstance> getAllExactInstancesForConcept(CeConcept pConcept) {
		ArrayList<CeInstance> result = null;

		if (pConcept != null) {
			result = new ArrayList<CeInstance>();

			for (CeInstance thisInst : getAllInstancesForConcept(pConcept)) {
				if (thisInst.countDirectConcepts() == 1) {
					CeConcept dirCon = thisInst.getDirectConcepts()[0];

					if (dirCon == pConcept) {
						result.add(thisInst);
					}
				}
			}

			Collections.sort(result);
		}

		// Ensure that an empty list rather than null is returned
		if (result == null) {
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	public int getTotalInstanceCount() {
		int result = 0;

		if (this.allInstances != null) {
			result = this.allInstances.size();
		}

		return result;
	}

	public int getInstanceCountForConcept(CeConcept pConcept) {
		int result = 0;
		ArrayList<CeInstance> conInsts = getAllInstancesForConcept(pConcept);

		if (conInsts != null) {
			result = conInsts.size();
		}

		return result;
	}

	public void saveInstance(ActionContext pAc, CeInstance pInst) {
		String keyName = getKeyNameFor(pAc, pInst);

		// Save the instance into the overall allInstances collection
		this.allInstances.put(keyName, pInst);

		// This method now only saves to the allInstances collection
		// For efficiency reasons the saving to the concept based collections
		// comes later (at the end of a unit of work)
	}

	private static String getKeyNameFor(ActionContext pAc, CeInstance pInst) {
		String keyName = null;

		if (pAc.getCeConfig().isCaseSensitive()) {
			keyName = pInst.getInstanceName();
		} else {
			keyName = pInst.getInstanceName().toLowerCase();
		}

		return keyName;
	}

	private static String getKeyNameFor(ActionContext pAc, String pInstName) {
		String keyName = null;

		if (pAc.getCeConfig().isCaseSensitive()) {
			keyName = pInstName;
		} else {
			keyName = pInstName.toLowerCase();
		}

		return keyName;
	}

	public void updateConceptInstanceListsFor(Collection<CeInstance> pInstList) {
		if (pInstList != null) {
			HashMap<CeConcept, HashSet<CeInstance>> tempList = new HashMap<CeConcept, HashSet<CeInstance>>();

			for (CeInstance thisInst : pInstList) {
				// Save this instance into each of the collections for the
				// relevant concepts
				for (CeConcept thisConcept : thisInst.getDirectConcepts()) {
					HashSet<CeInstance> thisHash = tempList.get(thisConcept);
					if (thisHash == null) {
						thisHash = new HashSet<CeInstance>();
						tempList.put(thisConcept, thisHash);
					}
					thisHash.add(thisInst);

					// Add to all parents too
					// TODO: A big performance improvement if we don't save this
					// but instead calculate
					// the instances for children when requested instead
					ArrayList<CeConcept> allParents = thisConcept.retrieveAllParents(false);
					for (CeConcept thisParent : allParents) {
						if (!thisParent.isThing()) {
							// Instances of thing do not need to be stored in
							// this structure (they are available from the
							// allInstances list)
							HashSet<CeInstance> innerHash = tempList.get(thisParent);
							if (innerHash == null) {
								innerHash = new HashSet<CeInstance>();
								tempList.put(thisParent, innerHash);
							}
							innerHash.add(thisInst);
						}
					}
				}
			}

			if (this.instancesByConcept != null) {
				for (CeConcept thisCon : tempList.keySet()) {
					HashSet<CeInstance> instList = tempList.get(thisCon);
					CopyOnWriteArrayList<CeInstance> conList = this.instancesByConcept.get(thisCon);

					if (conList == null) {
						conList = new CopyOnWriteArrayList<CeInstance>();
						this.instancesByConcept.put(thisCon, conList);
					}

					for (CeInstance thisInst : instList) {
						conList.addIfAbsent(thisInst);
					}
				}
			}
		}
	}

	public void removeInstance(CeInstance pInstance) {
		// First remove the instance from the overall allInstances collection
		this.allInstances.remove(pInstance.getInstanceName());

		// Next remove the instance from each of the collections for the
		// relevant concepts
		for (CeConcept thisConcept : pInstance.listAllConcepts()) {
			CopyOnWriteArrayList<CeInstance> instList = this.instancesByConcept.get(thisConcept);

			if (instList != null) {
				instList.remove(pInstance);
			}
		}
	}

	public void removeAllNonMetamodelInstances(ActionContext pAc) {
		ArrayList<CeInstance> allMetamodelInstances = new ArrayList<CeInstance>();
		// First reset the allInstances collection - metamodel instances will be
		// added back in during the final stage
		this.allInstances = new ConcurrentHashMap<String, CeInstance>();

		// Next iterate through all of the concepts and store a list of the
		// instances of any meta-model concepts
		for (CeConcept thisConcept : pAc.getModelBuilder().listAllConcepts()) {
			if (thisConcept.isMetaModelConcept()) {
				CopyOnWriteArrayList<CeInstance> theseInstances = this.instancesByConcept.get(thisConcept);

				if (theseInstances != null) {
					for (CeInstance thisInst : theseInstances) {
						allMetamodelInstances.add(thisInst);
					}
				}
			}
		}

		// Now reset the allInstancesByConcept collection - metamodel instances
		// will be added back in during the final stage
		this.instancesByConcept = new ConcurrentHashMap<CeConcept, CopyOnWriteArrayList<CeInstance>>();

		// Finally iterate through each of the metamodel instances and save them
		// back
		// into the instance repository
		for (CeInstance thisInst : allMetamodelInstances) {
			saveInstance(pAc, thisInst);
		}

		updateConceptInstanceListsFor(allMetamodelInstances);
	}

	// TODO: Add deleteSource

	protected void deleteAllInstancesForConcept(ActionContext pAc, CeConcept pConcept) {
		// Note that instances are not referenced by any other model entity, so
		// they can be safely deleted here
		// without leaving any references in place.
		// If there are textual references to the instance (i.e. in a property
		// that refers to it by name) then
		// these references must be manually cleaned up separately by the code
		// that calls this method.

		// First remove the instances individually from the list of all
		// instances
		ArrayList<CeInstance> allInsts = getAllInstancesForConcept(pConcept);
		for (CeInstance thisInst : allInsts) {
			deleteInstanceNoRefs(thisInst);
		}

		// Then remove all of the instances for the specified concept from the
		// other list
		this.instancesByConcept.remove(pConcept);

		// Then remove the unused sentences and sources that may be left as a
		// result
		pAc.getModelBuilder().removeUnusedSentencesAndSources(pAc);
	}

	protected static void deleteExactInstancesForConcept(ActionContext pAc, CeConcept pConcept) {
		// TODO: Implement this
		reportError(
				"Deleting of all exact instances (for concept '" + pConcept.getConceptName() + "') not yet implemented",
				pAc);
	}

	protected void deleteInstance(CeInstance pInst) {
		// Delete the instance
		deleteInstanceNoRefs(pInst);

		// Then delete from the all instances list
		this.allInstances.remove(pInst.getInstanceName());
	}

	private void deleteInstanceNoRefs(CeInstance pInst) {
		String instName = pInst.getInstanceName();

		// First delete from each of the concept level lists for each of the
		// concepts
		for (CeConcept thisCon : pInst.listAllConcepts()) {
			CopyOnWriteArrayList<CeInstance> instList = this.instancesByConcept.get(thisCon);

			if (instList != null) {
				instList.remove(pInst);
			}
		}

		// Then delete from the all instances list
		this.allInstances.remove(instName);
	}

}
