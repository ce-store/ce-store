package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ET;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SM;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.utilities.GeneralUtilities;

public class QuestionManagementHandler extends GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public QuestionManagementHandler(ActionContext pAc, long pStartTime) {
		super(pAc, pStartTime);
	}

	public CeStoreJsonObject handleStatus() {
		CeStoreJsonObject result = new CeStoreJsonObject();
		String statusMsg = calculateStatusString(this.ac);

		result.put(JSON_SM, statusMsg);
		result.put(JSON_ET, System.currentTimeMillis() - this.startTime);

		return result;
	}

	public static String calculateStatusString(ActionContext pAc) {
		String result = null;
		int conCount = pAc.getModelBuilder().listAllConcepts().size();
		long instCount = pAc.getModelBuilder().getTotalInstanceCount();
		long senCount = pAc.getModelBuilder().countSentences();

		GeneralUtilities.doGarbageCollection(pAc);

		result = reportMemoryUsage(pAc);		
		result += "\n" + conCount + " concepts, " + instCount + " instances and " + senCount + " CE sentences loaded";

		if (pAc.getModelBuilder() == null) {
			result += "\nNote: The CE Store is not yet initialised";
		}

		return result;
	}

	private static String reportMemoryUsage(ActionContext pAc) {
		String result = "";
		long mbMult = 1048576;

		double freeMem = Runtime.getRuntime().freeMemory();
		freeMem = freeMem / mbMult;

		double maxMem = Runtime.getRuntime().maxMemory();
		maxMem = maxMem / mbMult;

		double totalMem = Runtime.getRuntime().totalMemory();
		totalMem = totalMem / mbMult;

		result += "Memory (MB): Free=" + GeneralUtilities.formattedDecimal(freeMem) + ", Max=" + GeneralUtilities.formattedDecimal(maxMem) + ", Total=" + GeneralUtilities.formattedDecimal(totalMem) + ", Used=" + GeneralUtilities.formattedDecimal(totalMem - freeMem);

		if (pAc.getModelBuilder() != null) {
			long cachedStringCount = pAc.getModelBuilder().getCachedStringCount();

			result += "\nTotal cached strings = " + new Long(cachedStringCount).toString();
		}

		return result;
	}

//	public static String ceLoad(ActionContext pAc, String pCeUrl) {
//		String ceText = null;
//
//		ceText = "perform load sentences from url '";
//		ceText += pCeUrl;
//		ceText += "'.";
//
//		return ceSave(pAc, ceText);
//	}
//
//	public static String ceSave(ActionContext pAc, String pCeToSave) {
//		String resultText = "";
//
//		if (!pCeToSave.isEmpty()) {
//			reportDebug("CE to be saved: " + pCeToSave, pAc);
//
//			ContainerSentenceLoadResult result = GenericHandler.saveCeText(pAc, pCeToSave, null);
//
//			int valCount = result.getValidSentenceCount();
//			int invCount = result.getInvalidSentenceCount();
//
//			resultText += "CE sentences saved:";
//			resultText += "\n  " + valCount + " were valid";
//
//			if (invCount > 0) {
//				resultText += "\n  " + invCount + " were invalid";
//			}
//		}
//
//		return resultText;
//	}

}
