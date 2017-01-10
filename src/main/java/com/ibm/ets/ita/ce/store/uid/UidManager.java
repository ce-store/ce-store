package com.ibm.ets.ita.ce.store.uid;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_UID_PREFIX;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_UID_BATCHSTART;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_UID_BATCHEND;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public abstract class UidManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final long DEFAULT_UIDSTART = 0;

	private boolean reportDebug = false;
	private boolean reportMicroDebug = false;
	private boolean allowUserSpecifiedUidPrefixes = false;
	protected String uidPropFn = "";
	protected String uidPrefix = "";

	protected long uidBatchSize = -1;
	protected long uidStart = -1;
	protected String uidPadFormat = "";
	protected long uidEnd = -1;
	protected AtomicLong currentUid = new AtomicLong(-1);
	protected boolean usePadding = true;

	private ArrayList<String> errors = new ArrayList<String>();
	private ArrayList<String> warnings = new ArrayList<String>();
	private ArrayList<String> debugs = new ArrayList<String>();
	private ArrayList<String> microDebugs = new ArrayList<String>();

	public abstract void initialiseUidManager(boolean pDebug, boolean pMicroDebug, String pPropFn, boolean pAllowPrefixes, long pUidBatchSize);
	public abstract Properties getDefaultUidVals(String pUidVals);
	protected abstract void resetUidManager();
	protected abstract Properties getProperties();
	public abstract void extractValuesFrom(Properties pVals);
	public abstract void initialiseUids(long pBatchSize);

	public String getNextUid(String pOptionalPrefix) {
		String userPrefix = "";

		if (this.allowUserSpecifiedUidPrefixes) {
			if ((pOptionalPrefix != null) && (!pOptionalPrefix.isEmpty())) {
				userPrefix = pOptionalPrefix;
			}
		}

		if (this.currentUid.get() >= this.uidEnd) {
			reportDebug("Maximum UID size exceeded (" + Long.toString(this.currentUid.get()) + ") - requesting new batch");
			initialiseUidsWithDefaultBatchSize(DEFAULT_UIDSTART);
		}

		String uidNumber = "";
		if (this.usePadding) {
			uidNumber = String.format(Locale.getDefault(), this.uidPadFormat, new Long(this.currentUid.incrementAndGet()));
		} else {
			uidNumber = Long.toString(this.currentUid.incrementAndGet());
		}

		return userPrefix + this.uidPrefix + uidNumber;
	}

	public String showNextUid() {
		return Long.toString(this.currentUid.get() + 1);
	}

	public void setNextUidTo(long pNextUid) {
		this.currentUid = new AtomicLong(pNextUid);
	}

	public Properties getBatchOfUids(long pBatchSize) {
		Properties result = null;

		if ((this.currentUid.get() + pBatchSize) >= this.uidEnd) {
			reportDebug("Maximum UID size exceeded in batch request for '" + Long.toString(pBatchSize) + "' UIDs (" + Long.toString(this.currentUid.get()) + ") - requesting new batch");
			initialiseUids(pBatchSize);
		}

		long startUid = this.currentUid.get();
		this.currentUid = new AtomicLong(this.currentUid.get() + pBatchSize);

		result = new Properties();
		result.put(JSON_UID_PREFIX, this.uidPrefix);
		result.put(JSON_UID_BATCHSTART, Long.toString(startUid));
		result.put(JSON_UID_BATCHEND, Long.toString(this.currentUid.getAndIncrement()));

		return result;
	}

	public void resetUidCounter(String pUidStart) {
		long tgtUidStart = DEFAULT_UIDSTART;

		//Reset the local values
		if ((pUidStart != null) && (!pUidStart.isEmpty())) {
			//Subtract 1 from the specified value so that when the next UID is requested
			//it is the value that was specified as the starting value
			tgtUidStart = new Long(pUidStart).longValue() - 1;
		}

		//Now reset the specific implementation
		resetUidManager();
		initialiseUidsWithDefaultBatchSize(tgtUidStart);
	}

	protected void setValues(boolean pReportDebug, boolean pReportMicroDebug, String pPropFn, boolean pAllowPrefixes, long pUidBatchSize) {
		this.reportDebug = pReportDebug;
		this.reportMicroDebug = pReportMicroDebug;
		this.uidPropFn = pPropFn;
		this.allowUserSpecifiedUidPrefixes = pAllowPrefixes;
		this.uidBatchSize = pUidBatchSize;
	}

	public ArrayList<String> getDebugs() {
		return this.debugs;
	}

	public ArrayList<String> getMicroDebugs() {
		return this.microDebugs;
	}

	public ArrayList<String> getWarnings() {
		return this.warnings;
	}

	public ArrayList<String> getErrors() {
		return this.errors;
	}

	public void clearMessages() {
		this.microDebugs = new ArrayList<String>();
		this.debugs = new ArrayList<String>();
		this.warnings = new ArrayList<String>();
		this.errors = new ArrayList<String>();
	}

	protected void reportDebug(String pText) {
		//Only log the message if debug is on
		if (this.reportDebug) { 
			this.debugs.add(pText);
		}
	}

	protected void reportMicroDebug(String pText) {
		//Only log the message if debug is on
		if (this.reportMicroDebug) { 
			this.microDebugs.add(pText);
		}
	}

	private void initialiseUidsWithDefaultBatchSize(long pStartUid) {
		initialiseUids(this.uidBatchSize);
		this.uidStart = pStartUid;
		this.currentUid = new AtomicLong(this.uidStart);
	}

}
