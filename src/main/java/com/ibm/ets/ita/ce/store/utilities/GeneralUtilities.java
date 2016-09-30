package com.ibm.ets.ita.ce.store.utilities;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ESC_BS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ESC_DQ;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ESC_DQ1;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ESC_DQ2;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ESC_SQ;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ESC_SQ1;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ESC_SQ2;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_BS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DQ;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DQ1;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DQ2;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_FS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SQ;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SQ1;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SQ2;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NEWUID;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_LOGGEDINUSER;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NOW;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_PROTOCOL_FILE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_PROTOCOL_HTTP;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_PROTOCOL_HTTPS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.REGEX_NEWUID;
import static com.ibm.ets.ita.ce.store.names.ParseNames.REGEX_LOGGEDINUSER;
import static com.ibm.ets.ita.ce.store.names.ParseNames.REGEX_NOW;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.getFolderValueFor;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportTiming;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportInfo;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportTiming;

import java.text.DecimalFormat;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public abstract class GeneralUtilities {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = GeneralUtilities.class.getName();
	private static final String PACKAGE_NAME = GeneralUtilities.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	public static String handleSpecialMarkersAndDecode(ActionContext pAc, String pVal) {
		String result = pVal;

		result = GeneralUtilities.handleNowMarker(result);
		result = GeneralUtilities.handleLoggedInUserMarker(pAc, result);
		result = GeneralUtilities.handleUidMarker(pAc, result);

		result = decodeForCe(result);

		return result;
	}

	private static String handleNowMarker(String pVal) {
		String result = null;

		if (pVal.contains(TOKEN_NOW)) {
			result = pVal.replaceAll(REGEX_NOW, new Long(timestampNow()).toString());
		} else {
			result = pVal;
		}

		return result;
	}

	private static String handleUidMarker(ActionContext pAc, String pVal) {
		String result = null;

		if (pVal.contains(TOKEN_NEWUID)) {
			result = pVal.replaceAll(REGEX_NEWUID, pAc.getModelBuilder().getNextUid(pAc, null));
		} else {
			result = pVal;
		}

		return result;
	}

	private static String handleLoggedInUserMarker(ActionContext pAc, String pVal) {
		String userName = pAc.getUserName();

		if ((userName == null) || (userName.isEmpty())) {
			userName = "no one";	//TODO: Abstract this
		}

		String result = null;

		if (pVal.contains(TOKEN_LOGGEDINUSER)) {
			result = pVal.replaceAll(REGEX_LOGGEDINUSER, userName);
		} else {
			result = pVal;
		}

		return result;
	}

	public static String formattedDuration(long pStartTime) {
		DecimalFormat df = new DecimalFormat("#,###,###,##0.000");

		return df.format((System.currentTimeMillis() - pStartTime) / 1000.00);
	}

	public static void reportExecutionTiming(ActionContext pAc, long pStartTime, String pLabel, String className, String methodName) {
		if (isReportTiming()) {
			long eTime = System.currentTimeMillis();
			DecimalFormat df = new DecimalFormat("#,###,###,##0.000");
			double duration = (eTime - pStartTime) / 1000.00;
			String message = "TIME: " + pLabel + " = " + df.format(duration) + " secs";
			reportTiming(message, pAc, className, methodName);
		}
	}

	public static String formattedDecimal(double pValue) {
		DecimalFormat df = new DecimalFormat("#,###,###,###.##");

		return df.format(pValue);
	}

	public static String encodeForCe(String pValue) {
		String result = pValue;

		if (pValue != null) {
			//Escape backslashes as double backslashes and escape single/double quotes with a preceding backslash
			result = result.replace(TOKEN_BS, ESC_BS);
			result = result.replace(TOKEN_SQ, ESC_SQ);
			result = result.replace(TOKEN_DQ, ESC_DQ);
			result = result.replace(TOKEN_SQ1, ESC_SQ1);
			result = result.replace(TOKEN_SQ2, ESC_SQ2);
			result = result.replace(TOKEN_DQ1, ESC_DQ1);
			result = result.replace(TOKEN_DQ2, ESC_DQ2);
		}

		return result;
	}

	public static String decodeForCe(String pValue) {
		String result = pValue;

		if (pValue != null) {
			//Replace escaped quotes and escaped backslashes
			result = result.replace(ESC_SQ, TOKEN_SQ);
			result = result.replace(ESC_DQ, TOKEN_DQ);
			result = result.replace(ESC_SQ1, TOKEN_SQ1);
			result = result.replace(ESC_SQ2, TOKEN_SQ2);
			result = result.replace(ESC_DQ1, TOKEN_DQ1);
			result = result.replace(ESC_DQ2, TOKEN_DQ2);
			result = result.replace(ESC_BS, TOKEN_BS);
		}

		return result;
	}

	public static String stripDelimitingQuotesFrom(String pValue) {
		String result = pValue;

		if (isQuoteDelimited(pValue)) {
			if (pValue.length() <= 2) {
				result = ES;
			} else {
				result = result.substring(1, pValue.length() - 1);
			}
		}

		return result;
	}

	public static long timestampNow() {
		return System.currentTimeMillis();
	}

	public static String substituteCeParameters(String pTemplate, TreeMap<String, String> pValues) {
		String result = pTemplate;

		for (String thisKey : pValues.keySet()) {
			String thisVal = pValues.get(thisKey);
			if (thisVal == null) {
				thisVal = ES;
			}

			result = result.replace(thisKey, GeneralUtilities.encodeForCe(thisVal));
		}

		return result;
	}

	public static String substituteNormalParameters(String pTemplate, TreeMap<String, String> pValues) {
		String result = pTemplate;

		for (String thisKey : pValues.keySet()) {
			String thisVal = pValues.get(thisKey);
			result = result.replace(thisKey, thisVal);
		}

		return result;
	}

	public static int getIntValueFrom(String pValue) {
		return new Integer(pValue).intValue();
	}

	public static long getLongValueFrom(String pValue) {
		return new Long(pValue).longValue();
	}

	public static boolean getBooleanValueFrom(String pValue) {
		return new Boolean(pValue).booleanValue();
	}

	public static boolean isQuoteDelimited(String pString) {
		boolean result = false;

		if (pString != null) {
			result = (pString.startsWith(TOKEN_SQ)) || (pString.startsWith(TOKEN_DQ)) || (pString.startsWith(TOKEN_SQ1)) || (pString.startsWith(TOKEN_SQ2)) || (pString.startsWith(TOKEN_DQ1)) || (pString.startsWith(TOKEN_DQ2));
		}

		return result;
	}

	public static String formattedStats(String pExecTime, int pTxns) {
		return Integer.toString(pTxns) + " txns, in " + pExecTime + " secs.";
	}

	public static void reportLatestUidValue(ActionContext pAc) {
		String latestUid = pAc.getModelBuilder().showNextUidWithoutIncrementing(pAc);

		reportInfo("Latest UID value = " + latestUid, pAc);
	}

	public static void reportMemoryUsage(ActionContext pAc) {
		long mbMult = 1048576;

		double freeMem = Runtime.getRuntime().freeMemory();
		freeMem = freeMem / mbMult;

		double maxMem = Runtime.getRuntime().maxMemory();
		maxMem = maxMem / mbMult;

		double totalMem = Runtime.getRuntime().totalMemory();
		totalMem = totalMem / mbMult;

		reportInfo("Memory (MB): Free=" + GeneralUtilities.formattedDecimal(freeMem) + ", Max=" + GeneralUtilities.formattedDecimal(maxMem) + ", Total=" + GeneralUtilities.formattedDecimal(totalMem) + ", Used=" + GeneralUtilities.formattedDecimal(totalMem - freeMem), pAc);

		long cachedStringCount = pAc.getModelBuilder().getCachedStringCount();

		reportInfo("Total cached strings = " + new Long(cachedStringCount).toString(), pAc);

		reportDetailedInstanceMemoryUsage(pAc);
	}

	public static void reportGeneralCountInformation(ActionContext pAc) {
		ModelBuilder mb = pAc.getModelBuilder();

		long srcCount = mb.getAllSources().size();
		long conCount = mb.listAllConcepts().size();
		long propCount = mb.countAllProperties();
		long valSenCount = mb.countAllValidSentences();
		long invSenCount = mb.countAllInvalidSentences();
		reportInfo("All sources = " + new Long(srcCount).toString(), pAc);
		reportInfo("All concepts = " + new Long(conCount).toString(), pAc);
		reportInfo("All properties = " + new Long(propCount).toString(), pAc);
		reportInfo("Valid sentences = " + new Long(valSenCount).toString(), pAc);
		reportInfo("Invalid sentences = " + new Long(invSenCount).toString(), pAc);
	}

	public static void doGarbageCollection(ActionContext pAc) {
		final String METHOD_NAME = "doGarbageCollection";

		try {
			System.gc();
			Thread.sleep(100);
			System.runFinalization();
			Thread.sleep(100);
			System.gc();
		} catch (InterruptedException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	private static void reportDetailedInstanceMemoryUsage(ActionContext pAc) {
		long queriedInstCount = pAc.getModelBuilder().getInstanceRepository().getTotalInstanceCount();
		long actualInstCount = 0;
		long priSenCount = 0;
		long secSenCount = 0;
		long dirConCount = 0;
		long inhConCount = 0;
		long piCount = 0;
		long pvCount = 0;
		long maxPri = 0;
		long maxSec = 0;
		long maxDirCon = 0;
		long maxInhCon = 0;
		long maxPi = 0;
		long maxPv = 0;

		for (CeInstance thisInst : pAc.getModelBuilder().getInstanceRepository().getAllInstances().values()) {
			++actualInstCount;
			long thisPri = thisInst.countPrimarySentences();
			long thisSec = thisInst.countSecondarySentences();
			long thisDirCon = thisInst.countDirectConcepts();
			long thisInhCon = thisInst.countInheritedConcepts();
			long thisPi = thisInst.countPropertyInstances();
			long thisPv = 0;

			for (CePropertyInstance thisPropInst : thisInst.getPropertyInstances()) {
				thisPv = thisPropInst.countPropertyValues();
				pvCount += thisPv;

				if (thisPv > maxPv) {
					maxPv = thisPv;
				}
			}

			priSenCount += thisPri;
			secSenCount += thisSec;
			dirConCount += thisDirCon;
			inhConCount += thisInhCon;
			piCount += thisPi;

			if (thisPri > maxPri) {
				maxPri = thisPri;
			}
			if (thisSec > maxSec) {
				maxSec = thisSec;
			}

			if (thisDirCon > maxDirCon) {
				maxDirCon = thisDirCon;
			}

			if (thisInhCon > maxInhCon) {
				maxInhCon = thisInhCon;
			}

			if (thisPi > maxPi) {
				maxPi = thisPi;
			}
		}

		if (actualInstCount > 0) {
			double avgPri = priSenCount;
			double avgSec = secSenCount;
			avgPri = avgPri / actualInstCount;
			avgSec = avgSec / actualInstCount;

			double avgDirCon = dirConCount;
			double avgInhCon = inhConCount;
			avgDirCon = avgDirCon / actualInstCount;
			avgInhCon = avgInhCon / actualInstCount;

			double avgPi = piCount / actualInstCount;
			double avgPv = pvCount / piCount;

			reportInfo("Total instances (actual) = " + new Long(actualInstCount).toString(), pAc);
			reportInfo("Total instances (queried) = " + new Long(queriedInstCount).toString(), pAc);
			reportInfo("Primary sentence links = " + new Long(priSenCount).toString() + " (average = " + formattedDecimal(avgPri) + ", max = " + new Long(maxPri).toString() + ")", pAc);
			reportInfo("Secondary sentence links = " + new Long(secSenCount).toString() + " (average = " + formattedDecimal(avgSec) + ", max = " + new Long(maxSec).toString() + ")", pAc);
			reportInfo("Direct concept links = " + new Long(dirConCount).toString() + " (average = " + formattedDecimal(avgDirCon) + ", max = " + new Long(maxDirCon).toString() + ")", pAc);
			reportInfo("Inherited concept links = " + new Long(inhConCount).toString() + " (average = " + formattedDecimal(avgInhCon) + ", max = " + new Long(maxInhCon).toString() + ")", pAc);
			reportInfo("PropertyInstance links = " + new Long(piCount).toString() + " (average = " + formattedDecimal(avgPi) + ", max = " + new Long(maxPi).toString() + ")", pAc);
			reportInfo("PropertyValue links = " + new Long(pvCount).toString() + " (average = " + formattedDecimal(avgPv) + ", max = " + new Long(maxPv).toString() + ")", pAc);
		}
	}

	public static boolean isRelativePath(String pUrl) {
		return !(pUrl.startsWith(TOKEN_PROTOCOL_HTTP) || pUrl.startsWith(TOKEN_PROTOCOL_HTTPS) || pUrl.startsWith(TOKEN_PROTOCOL_FILE));
	}

	public static String createFullUrlForRelativePath(ActionContext pAc, String pUrlPath) {
		String result = pUrlPath;

		//Remove the first slash if there is one
		if (result.startsWith(TOKEN_FS)) {
			String rootUrl = pAc.getCeConfig().getDefaultCeRootUrl();

			if ((pAc.getCeRoot() != null) && (!pAc.getCeRoot().isEmpty())) {
				rootUrl = getFolderValueFor(pAc, pAc.getCeRoot());
			}

			result = result.substring(1, result.length());
			result = rootUrl + result;
		} else if (result.startsWith(TOKEN_DOT)) {
			String currentUrl = pAc.getCeConfig().getDefaultCeCurrentUrl();

			if ((pAc.getCeRoot() != null) && (!pAc.getCeRoot().isEmpty())) {
				currentUrl = getFolderValueFor(pAc, pAc.getCeRoot());
			}

			result = result.substring(2, result.length());
			result = currentUrl + result;
		}

		return result;
	}

}
