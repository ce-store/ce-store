package com.ibm.ets.ita.ce.store;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import com.ibm.ets.ita.ce.store.uid.UidManager;

public class UidManagerDefault extends UidManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String DEFAULT_PREFIX = "";
	private static final long DEFAULT_START = 0;
	private static final long DEFAULT_END = 999999;
	
	private static final boolean USE_PADDING = false;
	
	@Override
	public void initialiseUidManager(boolean pReportDebug, boolean pReportMicroDebug, String pPropFn, boolean pAllowPrefixes, long pUidBatchSize) {
		setValues(pReportDebug, pReportMicroDebug, pPropFn, pAllowPrefixes, pUidBatchSize);
	}

	@Override
	protected void resetUidManager() {
		//Nothing needs to be done here as all values are hardcoded in this default implementation anyway
	}

	@Override
	protected Properties getProperties() {
		//In the default implementation there are no variable properties, so just return an empty Properties instance
		return new Properties();
	}

	@Override
	public void extractValuesFrom(Properties pVals) {
		//Not relevant for the default implementation - do nothing
	}
	
	@Override
	public Properties getDefaultUidVals(String pUidVals) {
		//In the default implementation there are no variable properties, so just return an empty Properties instance
		return new Properties();
	}

	@Override
	public void initialiseUids(long pBatchSize) {
		this.uidPrefix = DEFAULT_PREFIX;
		this.uidPadFormat = "%01d";

		this.uidStart = DEFAULT_START;
		this.uidEnd = DEFAULT_END;
		this.currentUid = new AtomicLong(this.uidStart);
		this.usePadding = USE_PADDING;
		
		reportMicroDebug("UID prefix: '" + this.uidPrefix + "'");
		reportMicroDebug("UID start: '" + Long.toString(this.uidStart) + "'");
		reportMicroDebug("UID end: '" + Long.toString(this.uidEnd) + "'");
	}

}
