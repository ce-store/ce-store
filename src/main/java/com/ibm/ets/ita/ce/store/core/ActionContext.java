package com.ibm.ets.ita.ce.store.core;

//ALL DONE

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.model.CeSource;

public abstract class ActionContext {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private long startTime = -1;
	private String ceStoreName = null;
	private String userName = null;
	private String credentials = null;
	private String ceRoot = null;

	private ModelBuilder mb = null;
	private StoreConfig conf = null;
	protected ActionResponse ar = null;
	private IndexedEntityAccessor iea = null; // Lazy initialised
	private SessionCreations sc = null; // Lazy initialised

	private boolean executingQueryOrRule = false;
	private boolean validatingOnly = false;
	private boolean autoExecuteRules = false;
	private boolean keepSentences = false;

	private CeSource currentSource = null;
	private CeSource lastSource = null; // TODO: Can we get rid of this?

	public abstract void switchToStoreNamed(String pStoreName);

	public ActionContext(StoreConfig pConf, String pUserName, ActionResponse pAr) {
		this.startTime = System.currentTimeMillis();
		this.userName = pUserName;
		this.conf = pConf;
		this.ar = pAr;
		this.autoExecuteRules = pConf.getAutoRunRules();
	}

	public long getStartTime() {
		return this.startTime;
	}

	public String getCeStoreName() {
		return this.ceStoreName;
	}

	public String getCeRoot() {
		return this.ceRoot;
	}

	public void setCeRoot(String pVal) {
		this.ceRoot = pVal;
	}

	public String getUserName() {
		return this.userName;
	}

	public String getCredentials() {
		return this.credentials;
	}

	public void setCredentials(String pCreds) {
		this.credentials = pCreds;
	}

	public boolean hasCredentials() {
		return this.credentials != null;
	}

	public ModelBuilder getModelBuilder() {
		return this.mb;
	}

	public void setModelBuilderAndCeStoreName(ModelBuilder pMb) {
		this.mb = pMb;
		this.ceStoreName = pMb.getCeStoreName();
	}

	public boolean hasModelBuilder() {
		return this.mb != null;
	}

	public void removeModelBuilder() {
		this.mb = null;
	}

	public StoreConfig getCeConfig() {
		return this.conf;
	}

	public ActionResponse getActionResponse() {
		return this.ar;
	}

	public IndexedEntityAccessor getIndexedEntityAccessor() {
		if (this.iea == null) {
			this.iea = new IndexedEntityAccessor(this.mb);
		}

		return this.iea;
	}

	public void clearIndexedEntityAccessor() {
		this.iea = null;
	}

	public SessionCreations getSessionCreations() {
		if (this.sc == null) {
			this.sc = new SessionCreations();
		}

		return this.sc;
	}

	public boolean isExecutingQueryOrRule() {
		return this.executingQueryOrRule;
	}

	public void markAsExecutingQueryOrRule(boolean pVal) {
		this.executingQueryOrRule = pVal;
	}

	public boolean isValidatingOnly() {
		return this.validatingOnly;
	}

	public void markAsValidatingOnly() {
		this.validatingOnly = true;
	}

	public void markAsNotValidating() {
		this.validatingOnly = false;
	}

	public boolean isAutoExecutingRules() {
		return this.autoExecuteRules;
	}

	public void markAsAutoExecuteRules(boolean pVal) {
		this.autoExecuteRules = pVal;
	}

	public boolean isKeepingSentences() {
		return this.keepSentences;
	}

	public void markAsKeepingSentences() {
		this.keepSentences = true;
	}

	public CeSource getCurrentSource() {
		return this.currentSource;
	}

	public void setCurrentSource(CeSource pSource) {
		this.currentSource = pSource;
	}

	public CeSource getLastSource() {
		return this.lastSource;
	}

	public void setLastSource(CeSource pSource) {
		this.lastSource = pSource;
	}

}
