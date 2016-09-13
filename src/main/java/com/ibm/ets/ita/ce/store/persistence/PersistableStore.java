package com.ibm.ets.ita.ce.store.persistence;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.List;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeSentence;

/*
 * Things a PersistenceManager needs from CEStore to be able to persist it.
 */
public interface PersistableStore {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	// store methods
	String getName();
	void reset(ActionContext pAc);

	// store source methods
	/*
	int getLastSourceId();
	List<CeSentence> getSources(int startId); // in order
	void loadSources(List<CeSentence> sentences);
	*/

	// store sentence methods
	int getLastSentenceResetEventId();
	int getLastSentenceAddEventId();
	int getLastSentenceRemoveEventId();
	List<CeSentence> getSentences(int startId); // in order
	void loadSentences(List<String> sentences, ActionContext actionContext);

}
