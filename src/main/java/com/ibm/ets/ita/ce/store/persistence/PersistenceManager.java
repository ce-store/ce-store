package com.ibm.ets.ita.ce.store.persistence;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;

public interface PersistenceManager
{
  public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

  void save(ActionContext actionContext);
  void load(ActionContext actionContext);
}
