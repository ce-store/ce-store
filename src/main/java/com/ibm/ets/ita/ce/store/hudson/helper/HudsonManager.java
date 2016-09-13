package com.ibm.ets.ita.ce.store.hudson.helper;

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_CC;
import static com.ibm.ets.ita.ce.store.names.MiscNames.VERSION;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.io.PrintWriter;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.IndexedEntityAccessor;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.hudson.model.ConvConfig;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class HudsonManager {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ConvConfig cc = null;
	private PrintWriter qWriter = null;
	private TreeMap<String, TreeMap<String, String>> inverseLists = null;
	private WordCheckerCache wcc = null;
	private IndexedEntityAccessor iea = null;

	public HudsonManager(ActionContext pAc) {
		reportDebug("Initialising HudsonManager", pAc);

		initialiseQuestionLog(pAc);

		this.inverseLists = new TreeMap<String, TreeMap<String, String>>();
		this.wcc = new WordCheckerCache();
	}

	public WordCheckerCache getWordCheckerCache() {
		return this.wcc;
	}

	public IndexedEntityAccessor getIndexedEntityAccessor(ModelBuilder pMb) {
		if (this.iea == null) {
			this.iea = new IndexedEntityAccessor(pMb);
		}

		return this.iea;
	}

	@Override
	protected void finalize() {
		//This must be reported to System.out as there is no action context against which to log the message
		System.out.println("ServletStateManager is being destroyed so I am closing the question log file");
		
		//TODO: Is there a cleaner way to ensure the file is closed?
		if (this.qWriter != null) {
			this.qWriter.close();
		}
	}

	private void initialiseQuestionLog(ActionContext pAc) {
		//TODO: Reimplement this using CE instead of JNDI

//		String METHOD_NAME = "initialiseQuestionLog";
//		InitialContext ctx = null;
//
//		try {
//			ctx = new InitialContext();
//
//			String qdn = (String) ctx.lookup(JNDI_QDN);
//
//			if (qdn != null) {
//				String filename = qdn + FILENAME_QUESTIONS;
//
//				try {
//					this.qWriter = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
//				} catch (IOException e) {
//					reportException(e, "Unhandled error when appending to file", pAc, logger, CLASS_NAME, METHOD_NAME);
//				}
//			}
//		} catch (NamingException e) {
//			reportDebug("No question log name was specified so questions will not be logged to file", pAc);
//		}
	}

	public synchronized void refreshQuestionLog(ActionContext pAc) {
		if (this.qWriter != null) {
			this.qWriter.close();
		}

		initialiseQuestionLog(pAc);
	}

	public synchronized void logQuestionText(ActionContext pAc, String pQt) {
		if (this.qWriter != null) {
			String userName = pAc.getUserName();
			String thisLine = null;
	
			if (userName == null) {
				userName = "(unknown user)";
			}
	
			thisLine = System.currentTimeMillis() + "	" + userName + "	" + VERSION + "	" + pQt;
	
			this.qWriter.println(thisLine);
			this.qWriter.flush();
		}
	}

	public ConvConfig getConvConfig(ActionContext pAc) {
		if (this.cc == null) {
			ModelBuilder mb = pAc.getModelBuilder();

			if (mb != null) {
				for (CeInstance ccInst : mb.getAllInstancesForConceptNamed(pAc, CON_CC)) {
					if (this.cc == null) {
						this.cc = ConvConfig.createUsing(pAc, ccInst);
					} else {
						reportError("Error: more than one instance of " + CON_CC + " defined: " + ccInst.getInstanceName(), pAc);
					}
				}
			}
		}

		if (this.cc == null) {
			reportDebug("No " + CON_CC + " instance found", pAc);
		}

		return this.cc;
	}

	public void deleteConvConfig() {
		this.cc = null;
	}

	public void clearInverseLists(ActionContext pAc) {
		reportDebug("Clearing inverseLists", pAc);

		this.inverseLists = new TreeMap<String, TreeMap<String, String>>();
	}

	public void clearWordCheckerCache(ActionContext pAc) {
		reportDebug("Clearing wordCheckerCache", pAc);

		this.wcc = new WordCheckerCache();
	}
	
	public void clearIndexedEntityAccessor(ActionContext pAc) {
		reportDebug("Clearing indexedEntityAccessor", pAc);

		this.iea = null;
	}

	public void clearCaches(ActionContext pAc) {
		clearInverseLists(pAc);
		clearWordCheckerCache(pAc);
		clearIndexedEntityAccessor(pAc);
	}

	public synchronized TreeMap<String, String> getInverseLookupFor(ActionContext pAc, String pConName, String pPropName) {
		TreeMap<String, String> result = null;
		String keyName = pConName + ":" + pPropName;
		
		result = this.inverseLists.get(keyName);

		if (result == null) {
			reportDebug("Creating new inverseList for: " + keyName, pAc);

			result = new TreeMap<String, String>();

			for (CeInstance thisInst : pAc.getModelBuilder().getAllInstancesForConceptNamed(pAc, pConName)) {
				String instName = thisInst.getInstanceName();
				
				for (String rsValue : thisInst.getValueListFromPropertyNamed(pPropName)) {
					result.put(rsValue, instName);
				}
			}

			this.inverseLists.put(keyName, result);
		}

		return result;
	}

}
