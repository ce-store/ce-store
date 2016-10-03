package com.ibm.ets.ita.ce.store.hudson.helper;

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CC;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.IndexedEntityAccessor;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.hudson.model.ConvConfig;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class HudsonManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ConvConfig cc = null;
	private TreeMap<String, TreeMap<String, String>> inverseLists = null;
	private WordCheckerCache wcc = null;
	private IndexedEntityAccessor iea = null;

	public HudsonManager(ActionContext pAc) {
		reportDebug("Initialising HudsonManager", pAc);

		this.inverseLists = new TreeMap<String, TreeMap<String, String>>();
		this.wcc = new WordCheckerCache();
	}

	public WordCheckerCache getWordCheckerCache() {
		return this.wcc;
	}

	public IndexedEntityAccessor getIndexedEntityAccessor(ModelBuilder pMb) {
		if (this.iea == null) {
			this.iea = new IndexedEntityAccessor(pMb);
		}

		return this.iea;
	}

	public ConvConfig getConvConfig(ActionContext pAc) {
		if (this.cc == null) {
			ModelBuilder mb = pAc.getModelBuilder();

			if (mb != null) {
				for (CeInstance ccInst : mb.getAllInstancesForConceptNamed(pAc, CON_CC)) {
					if (this.cc == null) {
						this.cc = ConvConfig.createUsing(pAc, ccInst);
					} else {
						reportError("Error: more than one instance of " + CON_CC + " defined: " + ccInst.getInstanceName(), pAc);
					}
				}
			}
		}

		if (this.cc == null) {
			reportDebug("No " + CON_CC + " instance found", pAc);
		}

		return this.cc;
	}

	private void deleteConvConfig() {
		this.cc = null;
	}

	private void clearInverseLists(ActionContext pAc) {
		reportDebug("Clearing inverseLists", pAc);

		this.inverseLists = new TreeMap<String, TreeMap<String, String>>();
	}

	private void clearWordCheckerCache(ActionContext pAc) {
		reportDebug("Clearing wordCheckerCache", pAc);

		this.wcc = new WordCheckerCache();
	}
	
	private void clearIndexedEntityAccessor(ActionContext pAc) {
		reportDebug("Clearing indexedEntityAccessor", pAc);

		this.iea = null;
	}

	public void clearCaches(ActionContext pAc) {
		clearInverseLists(pAc);
		clearWordCheckerCache(pAc);
		clearIndexedEntityAccessor(pAc);
		deleteConvConfig();
	}

	public synchronized TreeMap<String, String> getInverseLookupFor(ActionContext pAc, String pConName, String pPropName) {
		TreeMap<String, String> result = null;
		String keyName = pConName + ":" + pPropName;
		
		result = this.inverseLists.get(keyName);

		if (result == null) {
			reportDebug("Creating new inverseList for: " + keyName, pAc);

			result = new TreeMap<String, String>();

			for (CeInstance thisInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, pConName)) {
				String instName = thisInst.getInstanceName();
				
				for (String rsValue : thisInst.getValueListFromPropertyNamed(pPropName)) {
					result.put(rsValue, instName);
				}
			}

			this.inverseLists.put(keyName, result);
		}

		return result;
	}

}
