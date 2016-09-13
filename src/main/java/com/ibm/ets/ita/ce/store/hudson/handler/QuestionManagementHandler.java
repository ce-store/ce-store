package com.ibm.ets.ita.ce.store.hudson.handler;

import static com.ibm.ets.ita.ce.store.names.CeNames.SRC_HUDSON;
import static com.ibm.ets.ita.ce.store.names.MiscNames.URL_HUDSON;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ET;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SM;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.CESTORENAME_DEFAULT;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.hudson.helper.HudsonManager;
import com.ibm.ets.ita.ce.store.hudson.model.ConvConfig;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.utilities.GeneralUtilities;

public class QuestionManagementHandler extends GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public QuestionManagementHandler(ActionContext pAc, long pStartTime) {
		super(pAc, pStartTime);
	}

	public CeStoreJsonObject handleReset() {
		CeStoreJsonObject result = new CeStoreJsonObject();

		//Just reset the store and clear the config
		ServletStateManager ssm = ServletStateManager.getServletStateManager();
		HudsonManager hm = ServletStateManager.getHudsonManager(this.ac);

		ssm.deleteModelBuilder(this.ac, this.ac.getModelBuilder());
		hm.deleteConvConfig();
		hm.refreshQuestionLog(this.ac);
		hm.clearCaches(this.ac);
		this.ac.clearIndexedEntityAccessor();

		setupCeStore(this.ac, ssm, hm);

		result.put(JSON_SM, "The CE Store has been reset");
		result.put(JSON_ET, System.currentTimeMillis() - this.startTime);

		return result;
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

	public static String ceLoad(ActionContext pAc, String pCeUrl) {
		String ceText = null;

		ceText = "perform load sentences from url '";
		ceText += pCeUrl;
		ceText += "'.";

		return ceSave(pAc, ceText);
	}

	public static String ceSave(ActionContext pAc, String pCeToSave) {
		String resultText = "";

		if (!pCeToSave.isEmpty()) {
			reportDebug("CE to be saved: " + pCeToSave, pAc);

			ContainerSentenceLoadResult result = GenericHandler.saveCeText(pAc, pCeToSave, null);

			int valCount = result.getValidSentenceCount();
			int invCount = result.getInvalidSentenceCount();

			resultText += "CE sentences saved:";
			resultText += "\n  " + valCount + " were valid";

			if (invCount > 0) {
				resultText += "\n  " + invCount + " were invalid";
			}
		}

		return resultText;
	}

	protected static void setupCeStore(ActionContext pAc, ServletStateManager pSsm, HudsonManager pHm) {
		String storeName = CESTORENAME_DEFAULT;
		ModelBuilder mb = pSsm.getModelBuilder(storeName);
		boolean storeWasLoaded = false;

		if (mb == null) {
			//The CE Store is not yet set up, so initialise it
			mb = loadCeStore(pAc, storeName);
			storeWasLoaded = true;
		}

		//The CE store does not need to be created but it does still need to
		//be added to the action context
		pAc.setModelBuilderAndCeStoreName(mb);

		//For this application the CE Store is always case-insenstive
		pAc.getCeConfig().setCaseInsensitive();

		//DSB 13/10/2015 - See whether rules should be auto executed
		ConvConfig cc = pHm.getConvConfig(pAc);

		if (cc != null) {
			if (cc.isRunningRules()) {
				pAc.markAsAutoExecuteRules(true);

				if (storeWasLoaded) {
					//Run all the rules since the store was loaded in this call
					//and the flag to auto-run the rules has only just been set
					ArrayList<CeRule> allRules = new ArrayList<CeRule>();
					allRules.addAll(pAc.getModelBuilder().getAllRules().values());

					pAc.getModelBuilder().executeTheseRules(pAc, allRules, 10, null);
				}
			}
		}
		
	}

	private static synchronized ModelBuilder loadCeStore(ActionContext pAc, String pStoreName) {
		ServletStateManager ssm = ServletStateManager.getServletStateManager();
		ModelBuilder mb = ssm.getModelBuilder(pStoreName);

		//Do the null test again since this method is synchronized and other requests
		//may have been blocked and are now flowing through but the CE store has now
		//been set up whilst they have been waiting
		if (mb == null) {
			mb = ssm.createModelBuilder(pAc, pStoreName);
			reportDebug("Successfully set up CE Store named " + pStoreName, pAc);

			StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
			String tgtUrl = URL_HUDSON;
			String tgtSrc = SRC_HUDSON;

			sa.loadSentencesFromUrl(tgtUrl, tgtSrc);

			reportDebug("Successfully loaded CE from url " + tgtUrl, pAc);
		}

		return mb;
	}

}
