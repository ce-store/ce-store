package com.ibm.ets.ita.ce.store.uid;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_UIDEND;
import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_UIDSTART;
import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_UIDPREFIX;
import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_UIDPADFORMAT;
import static com.ibm.ets.ita.ce.store.names.MiscNames.UID_USEPADDING;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class UidManagerDefault extends UidManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

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
		this.uidPrefix = DEFAULT_UIDPREFIX;
		this.uidPadFormat = DEFAULT_UIDPADFORMAT;

		this.uidStart = DEFAULT_UIDSTART;
		this.uidEnd = DEFAULT_UIDEND;
		this.currentUid = new AtomicLong(this.uidStart);
		this.usePadding = UID_USEPADDING;
		
		reportMicroDebug("UID prefix: '" + this.uidPrefix + "'");
		reportMicroDebug("UID start: '" + Long.toString(this.uidStart) + "'");
		reportMicroDebug("UID end: '" + Long.toString(this.uidEnd) + "'");
	}

}
