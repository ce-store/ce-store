package com.ibm.ets.ita.ce.store.model.container;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;

public class ContainerSentenceLoadResult extends ContainerResult {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static int uidCtr = 1;

	private int validSentenceCount = 0;
	private int invalidSentenceCount = 0;
	private int commandCount = 0;
	private int uid = -1;
	private String context = null;
	private ArrayList<BuilderSentence> validatedSentences = null;

	//TODO: Replace with a better solution (and account for concepts and properties too!)
	private ArrayList<CeInstance> newInstances = new ArrayList<CeInstance>();

	private ContainerSentenceLoadResult(String pContext) {
		//private to force use of constructor
		this.uid = ContainerSentenceLoadResult.uidCtr++;
		this.context = pContext;
	}

	public static ContainerSentenceLoadResult createWithZeroValues(String pContext) {
		return new ContainerSentenceLoadResult(pContext);
	}

	public int getUid() {
		return this.uid;
	}
	
	@Override
	public boolean isStatistics() {
		return true;
	}

	@Override
	public boolean isCeResult() {
		return false;
	}

	@Override
	public long getCreationDate() {
		return timestampNow();
	}

	public int getValidSentenceCount() {
		return this.validSentenceCount;
	}

	public void setValidSentenceCount(int pVal) {
		this.validSentenceCount = pVal;
	}

	public int getInvalidSentenceCount() {
		return this.invalidSentenceCount;
	}

	public void setInvalidSentenceCount(int pVal) {
		this.invalidSentenceCount = pVal;
	}

	public void incrementInvalidSentenceCount(int pVal) {
		this.invalidSentenceCount += pVal;
	}

	public int getCommandCount() {
		return this.commandCount;
	}

	public ArrayList<CeInstance> getNewInstances() {
		return this.newInstances;
	}

	public void setNewInstances(HashSet<CeInstance> pInstList) {
		this.newInstances = new ArrayList<CeInstance>(pInstList);
	}

	public ArrayList<BuilderSentence> getValidatedSentences() {
		return this.validatedSentences;
	}

	public void addValidatedSentence(BuilderSentence pSen) {
		if (this.validatedSentences == null) {
			this.validatedSentences = new ArrayList<BuilderSentence>();
		}

		this.validatedSentences.add(pSen);
		
		this.validSentenceCount++;
	}

	public void updateFrom(ContainerSentenceLoadResult pSenStats) {
		updateFrom(pSenStats, false);
	}
	
	public void updateFrom(ContainerSentenceLoadResult pSenStats, boolean pIncrementCommandCount) {
		int incVal = 1;

		if (pSenStats.getCommandCount() > incVal) {
			incVal = pSenStats.getCommandCount();
		}

		this.validSentenceCount += pSenStats.getValidSentenceCount();
		this.invalidSentenceCount += pSenStats.getInvalidSentenceCount();

		if (pIncrementCommandCount) {
			this.commandCount += incVal;
		}
		
		this.validatedSentences = pSenStats.getValidatedSentences();
	}

	public String convertToText() {
		StringBuilder sb = new StringBuilder();

		appendToSb(sb, "-- The following sentences were processed in this action:");
		appendToSb(sb, "--   Valid sentences: " + this.validSentenceCount);
		appendToSb(sb, "--   Invalid sentences: " + this.invalidSentenceCount);
		appendToSb(sb, "--   Command files processed: " + this.commandCount);

		return sb.toString();
	}

	@Override
	public String toString() {
		String result = "";

		result = "SenStats (" + this.context + "): ";
		result += this.commandCount + ", ";
		result += this.validSentenceCount + ", ";
		result += this.invalidSentenceCount + ", ";
		result += "{" + this.newInstances.size() + "}";
		result += " [" + this.uid + "]";

		return result;
	}

}
