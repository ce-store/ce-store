package com.ibm.ets.ita.ce.store.utilities;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.BR;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportTiming;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportInfo;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportTiming;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

	private static final String RAW_DOT = ".";
	private static final String RAW_BS = "\\";
	private static final String RAW_FS = "/";
	private static final String RAW_SQ = "'";
	private static final String RAW_DQ = "\"";

	private static final String ESC_BS = "\\\\";
	private static final String ESC_SQ = "\\'";
	private static final String ESC_DQ = "\\\"";

	//DSB 30/07/2014 - Added handling for special quote characters
	private static final String RAW_SQ1 = "‘";
	private static final String RAW_SQ2 = "’";
	private static final String RAW_DQ1 = "“";
	private static final String RAW_DQ2 = "”";
	private static final String ESC_SQ1 = "\\‘";
	private static final String ESC_SQ2 = "\\’";
	private static final String ESC_DQ1 = "\\“";
	private static final String ESC_DQ2 = "\\”";

	private static final String TOKEN_HTTP = "http://";
	private static final String TOKEN_HTTPS = "https://";
	private static final String TOKEN_FILE = "file://";

	private static final String MARKER_UID = "{uid}";
	private static final String REGEX_UID = "\\{uid\\}";
	private static final String MARKER_NOW = "{now}";
	private static final String REGEX_NOW = "\\{now\\}";
	private static final String MARKER_LOGGEDINUSER = "{logged in user}";
	private static final String REGEX_LOGGEDINUSER = "\\{logged in user\\}";

	private static ArrayList<String> dateFormatList = null;

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

		if (pVal.contains(MARKER_NOW)) {
			result = pVal.replaceAll(REGEX_NOW, new Long(timestampNow()).toString());
		} else {
			result = pVal;
		}

		return result;
	}

	private static String handleUidMarker(ActionContext pAc, String pVal) {
		String result = null;

		if (pVal.contains(MARKER_UID)) {
			result = pVal.replaceAll(REGEX_UID, pAc.getModelBuilder().getNextUid(pAc, null));
		} else {
			result = pVal;
		}

		return result;
	}

	private static String handleLoggedInUserMarker(ActionContext pAc, String pVal) {
		String userName = pAc.getUserName();

		if ((userName == null) || (userName.isEmpty())) {
			userName = "no one";
		}

		String result = null;

		if (pVal.contains(MARKER_LOGGEDINUSER)) {
			result = pVal.replaceAll(REGEX_LOGGEDINUSER, userName);
		} else {
			result = pVal;
		}

		return result;
	}

	public static String formatToEightPlaces(double pValue) {
		DecimalFormat df = new DecimalFormat("#,###,###,##0.00000000");

		return df.format(pValue);
	}

	public static String formattedUnixTime() {
		return Long.toString(System.currentTimeMillis());
	}

	public static String formattedDuration(long pStartTime) {
		DecimalFormat df = new DecimalFormat("#,###,###,##0.000");

		return df.format((System.currentTimeMillis() - pStartTime) / 1000.00);
	}

	public static String formattedTimeForFilenames() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

		return sdf.format(new Date());
	}

	public static String formattedSimpleDatetime(Date pDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

		return sdf.format(pDate);
	}

	public static String formattedIso8601DateString(Calendar pCal) {
		Date date = pCal.getTime();
		String result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);

		return result + "Z";
	}

	private static ArrayList<String> getDateFormatList() {
		if (dateFormatList == null) {
			dateFormatList = new ArrayList<String>();
			dateFormatList.add("yyyy-MM-dd'T'HH:mm:ssZ");
			dateFormatList.add("yyyy-MM-dd'T'HH:mmZ");
			dateFormatList.add("yyyy-MM-dd'T'HHZ");
			dateFormatList.add("yyyy-MM-ddZ");
			dateFormatList.add("yyyy-MMZ");
			dateFormatList.add("yyyyZ");
			dateFormatList.add("yyyyMMdd'T'HHmmssZ");
			dateFormatList.add("yyyyMMdd'T'HHmmZ");
			dateFormatList.add("yyyyMMdd'T'HHZ");
			dateFormatList.add("yyyyMMddZ");
			dateFormatList.add("yyyyMMZ");
			dateFormatList.add("yyyyMMddHHmmssZ");
			dateFormatList.add("yyyyMMddHHmmZ");
			dateFormatList.add("yyyyMMddHHZ");
		}

		return dateFormatList;
	}

	public static Calendar extractTimestampFrom(String pValue, ActionContext pAc) {
		Calendar calendar = Calendar.getInstance();
		String targetString = pValue.replace("Z", "GMT-00:00");
		Date date = null;

		boolean success = false;
		for (String thisFormat : getDateFormatList()) {
			if (!success) {
				try {
					date = new SimpleDateFormat(thisFormat).parse(targetString);
					success = true;
				} catch (ParseException e) {
					//Nothing to do here
				}
			}
		}

		if (success) {
			calendar.setTime(date);
		} else {
			reportError("No valid parses found for timestamp '" + pValue + "'", pAc);
		}

		return calendar;
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
			result = result.replace(RAW_BS, ESC_BS);
			result = result.replace(RAW_SQ, ESC_SQ);
			result = result.replace(RAW_DQ, ESC_DQ);

			//DSB 30/07/2014 - Added handling for special quote characters
			result = result.replace(RAW_SQ1, ESC_SQ1);
			result = result.replace(RAW_SQ2, ESC_SQ2);
			result = result.replace(RAW_DQ1, ESC_DQ1);
			result = result.replace(RAW_DQ2, ESC_DQ2);
		}

		return result;
	}

	public static String decodeForCe(String pValue) {
		String result = pValue;

		if (pValue != null) {
			//Replace escaped quotes and escaped backslashes
			result = result.replace(ESC_SQ, RAW_SQ);
			result = result.replace(ESC_DQ, RAW_DQ);
			result = result.replace(ESC_BS, RAW_BS);
		}

		return result;
	}

	public static String encodeForHtml(String pValue) {
		return pValue.replace(NL, BR);
	}

	public static String stripDelimitingQuotesFrom(String pValue) {
		String result = pValue;

		if (isQuoteDelimited(pValue)) {
			if (pValue.length() <= 2) {
				result = "";
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
				thisVal = "";
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
			//DSB 30/07/2014 - Added handling for special quote characters
//			result = (pString.startsWith(RAW_SQ)) || (pString.startsWith(RAW_DQ));
			result = (pString.startsWith(RAW_SQ)) || (pString.startsWith(RAW_DQ)) || (pString.startsWith(RAW_SQ1)) || (pString.startsWith(RAW_SQ2)) || (pString.startsWith(RAW_DQ1)) || (pString.startsWith(RAW_DQ2));
		}

		return result;
	}

	public static String formattedStats(String pExecTime, int pTxns) {
		String result = "";

		result = Integer.toString(pTxns) + " txns, in " + pExecTime + " secs.";

		return result;
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
		return !(pUrl.startsWith(TOKEN_HTTP) || pUrl.startsWith(TOKEN_HTTPS) || pUrl.startsWith(TOKEN_FILE));
	}

	public static String createFullUrlForRelativePath(ActionContext pAc, String pUrlPath) {
		String result = pUrlPath;

		//Remove the first slash if there is one
		if (result.startsWith(RAW_FS)) {
			result = result.substring(1, result.length());
			result = pAc.getCeConfig().getDefaultCeRootUrl() + result;
		} else if (result.startsWith(RAW_DOT)) {
			result = result.substring(2, result.length());
			result = pAc.getCeConfig().getDefaultCeCurrentUrl() + result;
		}

		return result;
	}

	public static String removeCrs(String pText) {
		return pText.replaceAll(NL, "");
	}

	public static String encodeAndEncloseInQuotesIfNeeded(String pVal) {
		String result = encodeForCe(pVal);

		if ((pVal.indexOf(" ") == -1) && (result.length() == pVal.length())) {
			//No spaces and no change in length (so no special characters)
			String lcVal = pVal.toLowerCase();

			//TODO: This should not be a hardcoded list (and I should fix the parser so that these don't break it)
			if (lcVal.equals("the") || lcVal.equals("and") || lcVal.equals("that")) {
				//A word that currently breaks the parser if not quoted, so quote
				result = "'" + result + "'";
			} else {
				//No special words - No quotes needed
			}
		} else {
			result = "'" + result + "'";
		}

		return result;
	}

}
