package com.ibm.ets.ita.ce.store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

public class SessionCreations {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private HashSet<CeInstance> newInstances = null;
	private HashSet<CeInstance> totalInstances = null;
	private List<CeSentence> validSentencesCreated = null;
	private List<CeSentence> invalidSentencesCreated = null;
	private List<CeSentence> allValidSessionSentences = null;

	public List<CeSentence> getAllValidSessionSentences() {
		return this.allValidSessionSentences;
	}

	public HashSet<CeInstance> getNewInstances() {
		return this.newInstances;
	}

	public HashSet<CeInstance> getTotalInstances() {
		return this.totalInstances;
	}

	public void setNewInstances(HashSet<CeInstance> pNewInsts) {
		this.newInstances = pNewInsts;

		if (this.totalInstances == null) {
			this.totalInstances = new HashSet<CeInstance>();
		}
		this.totalInstances.addAll(pNewInsts);
	}

	public void recordNewInstance(CeInstance pInst) {
		if (this.newInstances == null) {
			this.newInstances = new HashSet<CeInstance>();
		}
		this.newInstances.add(pInst);

		if (this.totalInstances == null) {
			this.totalInstances = new HashSet<CeInstance>();
		}
		this.totalInstances.add(pInst);

	}

	public List<CeSentence> getValidSentencesCreated() {
		return this.validSentencesCreated;
	}

	public void recordValidSentence(CeSentence pSen) {
		if (this.validSentencesCreated == null) {
			this.validSentencesCreated = new ArrayList<CeSentence>();
		}

		this.validSentencesCreated.add(pSen);
	}

	public List<CeSentence> getInvalidSentencesCreated() {
		return this.invalidSentencesCreated;
	}

	public void recordInvalidSentence(CeSentence pSen) {
		if (this.invalidSentencesCreated == null) {
			this.invalidSentencesCreated = new ArrayList<CeSentence>();
		}

		this.invalidSentencesCreated.add(pSen);
	}

	public void clearSessionSentences(ActionContext pAc) {
		if (pAc.isKeepingSentences()) {
			if (this.validSentencesCreated != null) {
				if (this.allValidSessionSentences == null) {
					this.allValidSessionSentences = new ArrayList<CeSentence>();
				}
				this.allValidSessionSentences.addAll(this.validSentencesCreated);
			}
		}

		this.validSentencesCreated = null;
		this.invalidSentencesCreated = null;
	}

	public void clearSessionInstances() {
		this.newInstances = null;
	}

}
