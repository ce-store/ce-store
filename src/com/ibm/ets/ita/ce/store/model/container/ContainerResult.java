package com.ibm.ets.ita.ce.store.model.container;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.List;

import com.ibm.ets.ita.ce.store.model.CeSentence;

public abstract class ContainerResult {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private List<CeSentence> createdSentences = null;		//TODO: This should be located elsewhere (lower in the hierarchy)...
	private long executionTime = -1;
	public abstract long getCreationDate();	
	public abstract boolean isStatistics();
	public abstract boolean isCeResult();

	public List<CeSentence> getCreatedSentences() {
		return this.createdSentences;
	}

	public void setCreatedSentences(List<CeSentence> pSens) {
		this.createdSentences = pSens;
	}

	public long getExecutionTime() {
		return this.executionTime;
	}

	public void setExecutionTime(long pVal) {
		this.executionTime = pVal;
	}

}