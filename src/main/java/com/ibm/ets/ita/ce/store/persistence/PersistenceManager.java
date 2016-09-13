package com.ibm.ets.ita.ce.store.persistence;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public interface PersistenceManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	void save(ActionContext actionContext);
	void load(ActionContext actionContext);

}
