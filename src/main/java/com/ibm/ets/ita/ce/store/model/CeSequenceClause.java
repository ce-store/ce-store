package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class CeSequenceClause {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

//	private static AtomicLong sequenceIdVal = new AtomicLong(0);

//	private String id = null;
	private String conceptName = null;
	private String instanceName = null;

	private CeSequenceClause() {
		//This is private to ensure that new sequence clauses can only be created via the static methods
	}

//	public static void resetCounter() {
//		sequenceIdVal = new AtomicLong(0);
//	}

	public static CeSequenceClause createSequenceClause(String pConName, String pInstName) {
		CeSequenceClause newSc = new CeSequenceClause();
		
//		newSc.id = "sq_" + nextSequenceId();
		newSc.conceptName = pConName;
		newSc.instanceName = pInstName;

		return newSc;
	}

//	private static long nextSequenceId() {
//		return sequenceIdVal.incrementAndGet();
//	}

//	public String getId() {
//		return this.id;
//	}

	public String getConceptName() {
		return this.conceptName;
	}

	public String getInstanceName() {
		return this.instanceName;
	}

	@Override
	public String toString() {
		return getConceptName() + " -> " + getInstanceName();
	}

}
