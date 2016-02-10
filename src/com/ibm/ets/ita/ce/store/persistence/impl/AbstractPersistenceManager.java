package com.ibm.ets.ita.ce.store.persistence.impl;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.persistence.PersistableStore;
import com.ibm.ets.ita.ce.store.persistence.PersistenceManager;

public abstract class AbstractPersistenceManager
  implements PersistenceManager
{
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CLASS_NAME = AbstractPersistenceManager.class.getName();
	private static final String PACKAGE_NAME = AbstractPersistenceManager.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	// TODO this shouldn't really be a member variable, as it's passed-in to
	// save and load ?
	// store doesn't have access to config so use the action context to get it.
	ActionContext actionContext = null;

	// store stats. TODO move into a map of store name to stats object ?
	String storeName            = null;
	int storeResetEventId       = -1;
	int storeAddEventId         = -1;
	int storeRemoveEventId      = -1;


    /*
	abstract int getLastSavedSourceId(String storeName);
	abstract int writeSources(String storeName, List<CeSource> newSources);
	abstract List<CeSource> readSources(String storeName);
	abstract void deleteSources(String storeName);
    */
	abstract int getLastSavedSentenceId(String storeName);
	abstract int writeSentences(String storeName, List<CeSentence> newSentences);
	abstract List<String> readSentences(String storeName);
	abstract void deleteSentences(String storeName);


	String storeStatsToString() {
		return "(" + storeName + ", " + storeResetEventId + ", " + storeAddEventId + ", " + storeRemoveEventId + ")";
	}
	
	
	String saveNumbersToString(int lastSavedSentenceId, int numberOfNewSentences) {
		return storeName + ", last saved sentence id = " + lastSavedSentenceId + ", number of new sentences = " + numberOfNewSentences;
	}
	
	
	String loadNumbersToString(int numberOfSentences) {
		return storeName + ", number of sentences = " + numberOfSentences;
	}
	
	
	void updateStoreStats(PersistableStore store) {
		storeName          = store.getName();
		storeResetEventId  = store.getLastSentenceResetEventId();
		storeAddEventId    = store.getLastSentenceAddEventId();
		storeRemoveEventId = store.getLastSentenceRemoveEventId();
	}


	void resetStoreStats(String newStoreName) {
		storeName          = newStoreName;
		storeResetEventId  = -1;
		storeAddEventId    = -1;
		storeRemoveEventId = -1;
	}


	@Override
	public void save(ActionContext actionContext) {
		final String METHOD_NAME = "save";
		logger.entering(CLASS_NAME, METHOD_NAME, storeStatsToString());
		
		this.actionContext = actionContext;
		PersistableStore store = actionContext.getModelBuilder();
		if (!store.getName().equals(storeName)) {
			resetStoreStats(store.getName());
		}
		boolean saveRequired = false;
		if ( storeResetEventId != store.getLastSentenceResetEventId() ||
			storeRemoveEventId != store.getLastSentenceRemoveEventId())
		{
			//deleteSources(storeName); // TODO something less extreme than deleting all if remove event occurred ?
			deleteSentences(storeName);
			saveRequired = true;
		}
		if (storeAddEventId != store.getLastSentenceAddEventId()) {
			saveRequired = true;
		}
		if (saveRequired) {
			//int lastSavedSourceId = getLastSavedSourceId(storeName);
			//List<CeSource> newSources = store.getSources(storeName, lastSavedSourceId + 1);
			//writeSources(storeName, newSources);
			int lastSavedSentenceId = getLastSavedSentenceId(storeName);
			List<CeSentence> newSentences = store.getSentences(lastSavedSentenceId + 1);
			logger.logp(Level.FINER, CLASS_NAME, METHOD_NAME, saveNumbersToString(lastSavedSentenceId, newSentences.size()));
			writeSentences(storeName, newSentences);
		}
		updateStoreStats(store);
		this.actionContext = null;
		
		logger.exiting(CLASS_NAME, METHOD_NAME, storeStatsToString());
	}


	@Override
	public void load(ActionContext actionContext) {
		final String METHOD_NAME = "load";
		logger.entering(CLASS_NAME, METHOD_NAME, storeStatsToString());
		
		this.actionContext = actionContext;
		PersistableStore store = actionContext.getModelBuilder();
		store.reset(actionContext);
		resetStoreStats(store.getName());
		List<String> sentences = readSentences(storeName);
		logger.logp(Level.FINER, CLASS_NAME, METHOD_NAME, loadNumbersToString(sentences.size()));
		store.loadSentences(sentences, actionContext);
		updateStoreStats(store);
		this.actionContext = null;
		
		logger.exiting(CLASS_NAME, METHOD_NAME, storeStatsToString());
	}

}
