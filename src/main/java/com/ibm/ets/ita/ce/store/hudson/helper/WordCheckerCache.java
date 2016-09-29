package com.ibm.ets.ita.ce.store.hudson.helper;

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CONFCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_UNINTCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_PLURAL;
import static com.ibm.ets.ita.ce.store.names.CeNames.CON_PROPCON;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_PROPNAME;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROPS_LING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.hudson.model.ConvConfig;
import com.ibm.ets.ita.ce.store.hudson.model.conversation.ProcessedWord;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class WordCheckerCache {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private HashMap<String, CeConcept> matchingConcepts = new HashMap<String, CeConcept>();
	private HashMap<String, TreeMap<String, CeProperty>> matchingRelations = new HashMap<String, TreeMap<String, CeProperty>>();
	private HashMap<String, ArrayList<CeInstance>> matchingInstances = new HashMap<String, ArrayList<CeInstance>>();

	private ArrayList<String> commonWords = null;
	private ArrayList<String> negationWords = null;
	private ArrayList<CeInstance> lingThingInsts = null;
	private HashMap<String, ArrayList<CeInstance>> lingThingPluralFormInsts = new HashMap<String, ArrayList<CeInstance>>();

	public synchronized void checkForMatchingConcept(ActionContext pAc, ProcessedWord pWord) {
		checkForMatchingConceptUsing(pAc, pWord, pWord.getDeclutteredText());

		String depText = pWord.depluralise(pWord.getDeclutteredText());

		if (depText != null) {
			checkForMatchingConceptUsing(pAc, pWord, depText);
		}

		depText = pWord.pluralise(pWord.getDeclutteredText());

		if (depText != null) {
			checkForMatchingConceptUsing(pAc, pWord, depText);
		}
	}

	public synchronized void checkForMatchingConceptUsing(ActionContext pAc, ProcessedWord pWord, String pText) {
		String cacheKey = pText;
		CeConcept tgtCon = null;

		if (!this.matchingConcepts.containsKey(cacheKey)) {
			tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, cacheKey);
			this.matchingConcepts.put(cacheKey, tgtCon);
		} else {
			tgtCon = this.matchingConcepts.get(cacheKey);
		}

		if (tgtCon != null) {
			pWord.setMatchingConcept(tgtCon);
		}
	}

	public synchronized void checkForMatchingRelation(ActionContext pAc, ProcessedWord pWord) {
		checkForMatchingRelationUsing(pAc, pWord, pWord.getDeclutteredText());

		String depText = pWord.depluralise(pWord.getDeclutteredText());

		if (depText != null) {
			checkForMatchingRelationUsing(pAc, pWord, depText);
		}

		depText = pWord.pluralise(pWord.getDeclutteredText());

		if (depText != null) {
			checkForMatchingRelationUsing(pAc, pWord, depText);
		}
	}

	public synchronized void checkForMatchingRelationUsing(ActionContext pAc, ProcessedWord pWord, String pText) {
		TreeMap<String, CeProperty> tgtProps = null;

		if (!this.matchingRelations.containsKey(pText)) {
			tgtProps = new TreeMap<String, CeProperty>();

			for (CeInstance thisInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, CON_PROPCON)) {
				String propFullName = thisInst.getInstanceName();

				for (String expVal : thisInst.getValueListFromPropertyNamed(PROP_PROPNAME)) {
					if (expVal.equals(pText)) {
						CeProperty tgtProp = pAc.getModelBuilder().getPropertyFullyNamed(propFullName);
						tgtProps.put(expVal, tgtProp);
					}
				}
			}

			this.matchingRelations.put(pText, tgtProps);
		} else {
			tgtProps = this.matchingRelations.get(pText);
		}

		if (!tgtProps.isEmpty()) {
			pWord.setMatchingRelations(tgtProps);
		}
	}

	public synchronized void checkForMatchingInstances(ActionContext pAc, ProcessedWord pWord) {
		checkForMatchingInstancesUsing(pAc, pWord, pWord.getDeclutteredText());

		String depText = pWord.depluralise(pWord.getDeclutteredText());

		if (depText != null) {
			checkForMatchingInstancesUsing(pAc, pWord, depText);
		}

		depText = pWord.pluralise(pWord.getDeclutteredText());

		if (depText != null) {
			checkForMatchingInstancesUsing(pAc, pWord, depText);
		}
	}

	public synchronized void checkForMatchingInstancesUsing(ActionContext pAc, ProcessedWord pWord, String pText) {
		ArrayList<CeInstance> tgtInsts = null;
		HudsonManager hm = ServletStateManager.getHudsonManager(pAc);

		if (!this.matchingInstances.containsKey(pText)) {
			tgtInsts = new ArrayList<CeInstance>();
			ArrayList<CeInstance> possInsts = hm.getIndexedEntityAccessor(pAc.getModelBuilder())
					.getInstancesNamedOrIdentifiedAs(pAc, pText);

			for (CeInstance possInst : possInsts) {
				if (isValidMatchingInstance(pAc, possInst)) {
					tgtInsts.add(possInst);
				}
			}

			this.matchingInstances.put(pText, tgtInsts);
		} else {
			tgtInsts = this.matchingInstances.get(pText);
		}

		if (!tgtInsts.isEmpty()) {
			pWord.setMatchingInstances(tgtInsts);
		}
	}

	public synchronized ArrayList<CeInstance> checkForMatchingInstances(ActionContext pAc, String pText) {
		ArrayList<CeInstance> tgtInsts = null;
		HudsonManager hm = ServletStateManager.getHudsonManager(pAc);

		if (!this.matchingInstances.containsKey(pText)) {
			tgtInsts = new ArrayList<CeInstance>();
			ArrayList<CeInstance> possInsts = hm.getIndexedEntityAccessor(pAc.getModelBuilder())
					.getInstancesNamedOrIdentifiedAs(pAc, pText);

			for (CeInstance possInst : possInsts) {
				if (isValidMatchingInstance(pAc, possInst)) {
					tgtInsts.add(possInst);
				}
			}

			this.matchingInstances.put(pText, tgtInsts);
		} else {
			tgtInsts = this.matchingInstances.get(pText);
		}

		return tgtInsts;
	}

	private static boolean isValidMatchingInstance(ActionContext pAc, CeInstance pInst) {
		boolean result = false;

		if (pInst != null) {
			result = !pInst.isOnlyMetaModelInstance() && !isUninterestingInstance(pAc, pInst)
					&& !isOnlyConfigCon(pAc, pInst);
		}

		return result;
	}

	public static boolean isOnlyConfigCon(ActionContext pAc, CeInstance pTgtInst) {
		boolean result = true;

		for (CeConcept thisCon : pTgtInst.getDirectConcepts()) {
			if (!thisCon.hasParentNamed(CON_CONFCON)) {
				result = false;
				break;
			}
		}

		return result;
	}

	private static boolean isUninterestingInstance(ActionContext pAc, CeInstance pInst) {
		return pInst.isConceptNamed(pAc, CON_UNINTCON);
	}

	public synchronized ArrayList<String> getCommonWords(ConvConfig pCc, ActionContext pAc) {
		if (pCc != null) {
			if (this.commonWords == null) {
				this.commonWords = new ArrayList<String>();

				for (String thisCw : pCc.getCommonWords()) {
					String cwText = thisCw.trim().toLowerCase();
					this.commonWords.add(cwText);
				}
			}
		}

		return this.commonWords;
	}

	public synchronized ArrayList<String> getNegationWords(ConvConfig pCc, ActionContext pAc) {
		if (pCc != null) {
			if (this.negationWords == null) {
				this.negationWords = new ArrayList<String>();

				for (String thisNw : pCc.getNegationWords()) {
					String nwText = thisNw.trim().toLowerCase();

					this.negationWords.add(nwText);
				}
			}
		}

		return this.negationWords;
	}

	public synchronized ArrayList<CeInstance> getLingThingInstances(ActionContext pAc) {
		if (this.lingThingInsts == null) {
			this.lingThingInsts = new ArrayList<CeInstance>();

			for (CeInstance thisInst : pAc.getModelBuilder().listAllInstances()) {
				if (thisInst.hasPropertyInstanceForPropertyNamed(PROPS_LING)) {
					this.lingThingInsts.add(thisInst);
				}
			}
		}

		return this.lingThingInsts;
	}

	public synchronized ArrayList<CeInstance> getLingThingPluralForms(ActionContext pAc, String pTgtText) {
		if (!this.lingThingPluralFormInsts.containsKey(pTgtText)) {
			ArrayList<CeInstance> matchedInsts = new ArrayList<CeInstance>();

			for (CeInstance thisInst : getLingThingInstances(pAc)) {
				for (String thisPf : thisInst.getValueListFromPropertyNamed(PROP_PLURAL)) {
					if (thisPf.equals(pTgtText)) {
						matchedInsts.add(thisInst);
					}
				}
			}

			this.lingThingPluralFormInsts.put(pTgtText, matchedInsts);
		}

		return this.lingThingPluralFormInsts.get(pTgtText);
	}

}
