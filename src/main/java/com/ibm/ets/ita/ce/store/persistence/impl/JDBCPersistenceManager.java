package com.ibm.ets.ita.ce.store.persistence.impl;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.List;

import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.persistence.PersistenceManager;

public class JDBCPersistenceManager extends AbstractPersistenceManager implements PersistenceManager
{
	public static final String copyrightNotice = "(C) Copyright IBM Corporation	2011, 2016";

	@Override
	int getLastSavedSentenceId(String storeName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	int writeSentences(String storeName, List<CeSentence> newSentences) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	List<String> readSentences(String storeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void deleteSentences(String storeName) {
		// TODO Auto-generated method stub		
	}

}
