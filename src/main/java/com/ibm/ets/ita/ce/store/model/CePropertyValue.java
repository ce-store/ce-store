package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;

import java.util.concurrent.atomic.AtomicLong;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;

public class CePropertyValue {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static AtomicLong propvalIdVal = new AtomicLong(0);

	private String id = null;
	private String value = null;
	private CeInstance valueInst = null;
	private String rangeName = null;
	private long creationDate = 0;
	private boolean hadQuotesOriginally = false;
	private boolean isNegated = false;
	private CeSentence sentence = null;	//TODO: Make use of sentence

	private CePropertyValue() {
		// This is private to ensure that new instances can only be created via the various static methods
	}

	public static CePropertyValue createUsing(ActionContext pAc, String pValue, String pRangeName, boolean pHadQuotesOriginally, boolean pIsNegated, CeSentence pSen) {
		CePropertyValue thisPv = new CePropertyValue();
		thisPv.id = "pv_" + nextPropvalId();

		thisPv.setValue(pAc, pValue);
		thisPv.rangeName = pAc.getModelBuilder().getCachedStringValueLevel1(pRangeName);
		thisPv.hadQuotesOriginally = pHadQuotesOriginally;
		thisPv.isNegated = pIsNegated;
		
		if (pSen != null) {
			thisPv.creationDate = pSen.getCreationDate();
			if (pAc.getCeConfig().isSavingCeSentences()) {
				thisPv.sentence = pSen;		//The sentence is updated later in some cases
			}
		} else {
			thisPv.creationDate = timestampNow();
		}

		return thisPv;
	}
	
	public static void resetCounter() {
		propvalIdVal = new AtomicLong(0);
	}

	private static long nextPropvalId() {
		return propvalIdVal.incrementAndGet();
	}

	public String getId() {
		return this.id;
	}

	public String getValue() {
		return this.value;
	}
	
	public CeInstance getValueInstance(ActionContext pAc) {
		//Lazy initialisation
		if (this.valueInst == null) {
			this.valueInst = pAc.getModelBuilder().getInstanceNamed(pAc, this.value);
		}

		return this.valueInst;
	}

	public void setValue(ActionContext pAc, String pValue) {
		this.value = pAc.getModelBuilder().getCachedStringValueLevel1(pValue);
	}
	
	public String getRangeName() {
		return this.rangeName;
	}
	
	public long getCreationDate() {
		return this.creationDate;
	}
	
	public boolean hadQuotesOriginally() {
		return this.hadQuotesOriginally;
	}

	public boolean isNegated() {
		return this.isNegated;
	}

	public CeSentence getSentence() {
		return this.sentence;
	}

	public void setSentence(CeSentence pSen) {		
		this.sentence = pSen;
	}

	public boolean isInTimestampRange(long pStartTs, long pEndTs) {
		boolean result = true;

		if (pStartTs != ModelBuilder.NO_TS) {
			result = getCreationDate() >= pStartTs;
		}

		if (result) {
			if (pEndTs != ModelBuilder.NO_TS) {
				result = getCreationDate() <= pEndTs;
			}
		}

		return result;
	}

	@Override
	public String toString() {
		String result = "";
		
		result = "CePropertyValue: ";
		result += "'" + this.value + "'";
		result += " (" + this.rangeName + ")";
		
		return result;
	}

}