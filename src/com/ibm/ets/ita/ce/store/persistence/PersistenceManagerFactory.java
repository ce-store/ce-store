package com.ibm.ets.ita.ce.store.persistence;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.persistence.impl.FilePersistenceManager;

public class PersistenceManagerFactory
{
  public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

  private static PersistenceManager persistenceManager = null;

  public synchronized static PersistenceManager get() {
    if (persistenceManager==null) {
      persistenceManager = new FilePersistenceManager();
    }
    return persistenceManager;
  }
}
