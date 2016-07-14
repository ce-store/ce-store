package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.ibm.ets.ita.ce.store.ActionContext;

public class CeSequence {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String SEQ_PREFIX = "seq_";

	private static AtomicLong sequenceIdVal = new AtomicLong(0);

	private String sequenceId = null;
	private long creationDate = 0;
	private String propertyName = null;
	private String rawText = null;
	private ArrayList<CeSequenceClause> allClauses = new ArrayList<CeSequenceClause>();

	private CeSequence() {
		//This is private to ensure that new sequences can only be created via the static methods
	}

	public static void resetCounter() {
		sequenceIdVal = new AtomicLong(0);
	}

	public static CeSequence createSequence() {
		CeSequence newSeq = new CeSequence();

		newSeq.sequenceId = SEQ_PREFIX + nextSequenceId();
		newSeq.creationDate = timestampNow();

		return newSeq;
	}

	public static CeSequence createFrom(String pBracketedPhrase) {
		//TODO: Implement sequence properly
		CeSequence newSeq = new CeSequence();

		newSeq.sequenceId = SEQ_PREFIX + nextSequenceId();
		newSeq.creationDate = timestampNow();
		newSeq.rawText = pBracketedPhrase;

		return newSeq;
	}

	private static long nextSequenceId() {
		return sequenceIdVal.incrementAndGet();
	}

	public String getSequenceId() {
		return this.sequenceId;
	}

	public long getCreationDate() {
		return this.creationDate;
	}
	
	public String getRawText() {
		return this.rawText;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	public void setPropertyName(String pPropName) {
		this.propertyName = pPropName;
	}

	public ArrayList<CeSequenceClause> getAllClauses() {
		return this.allClauses;
	}

	public void addClause(String pConName, String pInstName) {
		CeSequenceClause newClause = CeSequenceClause.createSequenceClause(pConName, pInstName);

		this.allClauses.add(newClause);
	}

	@Override
	public String toString() {
		return "CeSequence: " + getSequenceId() + " (" + new Integer(getAllClauses().size()).toString() + " clauses, rawText=" + this.rawText + ")";
	}

	public void tempDebug(ActionContext pAc) {
		if (isReportDebug()) {
			//TODO: Remove this when implemented
			reportDebug("Reminder... Sequence processing is not yet fully implemented", pAc);
			reportDebug("Sequence " + this.sequenceId + ":", pAc);
			for (CeSequenceClause thisSc : this.allClauses) {
				reportDebug("    " + thisSc.toString(), pAc);
			}
		}
	}
}