package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.MODELNAME_CORE;
import static com.ibm.ets.ita.ce.store.names.MiscNames.FOLDER_JSON;
import static com.ibm.ets.ita.ce.store.names.MiscNames.FOLDER_MODELS;
import static com.ibm.ets.ita.ce.store.names.MiscNames.JSONFILE_QUESTIONS;
import static com.ibm.ets.ita.ce.store.names.MiscNames.JSONFILE_ANSWERS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_ET;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_MODELS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_SM;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.sendHttpGetRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonParser;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.StoreActions;

public class ModelDirectoryHandler extends GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public ModelDirectoryHandler(ActionContext pAc, long pStartTime) {
		super(pAc, pStartTime);
	}
	
	public CeStoreJsonObject handleList(){
		CeStoreJsonObject result = new CeStoreJsonObject();	
		StringBuilder stringBuilder = new StringBuilder();

		try{
		    URL url = new URL(this.ac.getCeConfig().getModelDirectoryUrl());
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setRequestMethod("GET");
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    while ((line = rd.readLine()) != null) {
		    	stringBuilder.append(line);
		    }
		    rd.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	    
	    CeStoreJsonParser parser = new CeStoreJsonParser(this.ac, stringBuilder.toString());
	    parser.parse();
	 
	    result.put(JSON_MODELS, parser.getRootJsonArray());
	    
		return result;
	}

	public CeStoreJsonObject handleLoad(String modelName){
		CeStoreJsonObject result = new CeStoreJsonObject();	
		String ceText = "";
		String mdu = this.ac.getCeConfig().getModelDirectoryUrl();

		String coreUrl = mdu + "/" + MODELNAME_CORE;

		ceText += "perform reset store with starting uid '1'.\n\n";
		ceText += "perform load sentences from url '" + coreUrl + "'.\n";

		if (!modelName.equals(MODELNAME_CORE)) {
			String modelUrl = mdu + "/" + modelName;
			ceText += "perform load sentences from url '" + modelUrl + "'.";
		}

		StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);
		sa.saveCeText(ceText, null);

//		//Just reset the store and clear the config
//		ServletStateManager ssm = ServletStateManager.getServletStateManager();
//		HudsonManager hm = ServletStateManager.getHudsonManager(this.ac);
//
//		ssm.deleteModelBuilder(this.ac, this.ac.getModelBuilder());
//		hm.clearCaches(this.ac);
//		this.ac.clearIndexedEntityAccessor();
//
//		setupCeStore(this.ac, ssm, hm, modelName);

		result.put(JSON_SM, "The CE Store has been reset");
		result.put(JSON_ET, System.currentTimeMillis() - this.startTime);

		return result;
	}
	
	public String handleGetQuestions(ActionContext pAc, String pModelName){
		String modelName = pModelName;

		if ((modelName == null) || (modelName.isEmpty())) {
			modelName = MODELNAME_CORE;
		}

		String questionUrl = pAc.getCeConfig().getModelDirectoryUrl() + "/" + FOLDER_MODELS + modelName + "/"+ FOLDER_JSON + JSONFILE_QUESTIONS;

		return sendHttpGetRequest(pAc, questionUrl, null, false);
	}

	public String handleGetAnswers(ActionContext pAc, String pModelName){
		String modelName = pModelName;

		if ((modelName == null) || (modelName.isEmpty())) {
			modelName = MODELNAME_CORE;
		}

		String answerUrl = pAc.getCeConfig().getModelDirectoryUrl() + "/" + FOLDER_MODELS + modelName + "/"+ FOLDER_JSON + JSONFILE_ANSWERS;

		return sendHttpGetRequest(pAc, answerUrl, null, false);
	}

//	protected static void setupCeStore(ActionContext pAc, ServletStateManager pSsm, HudsonManager pHm, String modelName) {
//		String storeName = CESTORENAME_DEFAULT;
//		ModelBuilder mb = pSsm.getModelBuilder(storeName);
//		boolean storeWasLoaded = false;
//
//		if (mb == null) {
//			//The CE Store is not yet set up, so initialise it
//			mb = loadCeStore(pAc, storeName, modelName, true);
//			storeWasLoaded = true;
//		}
//
//		//The CE store does not need to be created but it does still need to
//		//be added to the action context
//		pAc.setModelBuilderAndCeStoreName(mb);
//
//		ConvConfig cc = pHm.getConvConfig(pAc);
//
//		if (cc != null) {
//			if (cc.isRunningRules()) {
//				pAc.markAsAutoExecuteRules(true);
//
//				if (storeWasLoaded) {
//					//Run all the rules since the store was loaded in this call
//					//and the flag to auto-run the rules has only just been set
//					ArrayList<CeRule> allRules = new ArrayList<CeRule>();
//					allRules.addAll(pAc.getModelBuilder().getAllRules().values());
//
//					pAc.getModelBuilder().executeTheseRules(pAc, allRules, 10, null);
//				}
//			}
//		}		
//	}

//	private static synchronized ModelBuilder loadCeStore(ActionContext pAc, String pStoreName, String modelName, boolean loadCore) {
//		System.out.println("loading model from dir: " + modelName);
//		ServletStateManager ssm = ServletStateManager.getServletStateManager();
//		ModelBuilder mb = ssm.getModelBuilder(pStoreName);
//
//		//Do the null test again since this method is synchronized and other requests
//		//may have been blocked and are now flowing through but the CE store has now
//		//been set up whilst they have been waiting
//		if (mb == null) {
//			mb = ssm.createModelBuilder(pAc, pStoreName);
//			reportDebug("Successfully set up CE Store named " + pStoreName, pAc);
//
//			StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
//			String tgtSrc = SRC_HUDSON;
//			
//			//load core
//			if(loadCore && !modelName.equalsIgnoreCase(MODELNAME_CORE)){
//				String coreUrl = pAc.getCeConfig().getModelDirectoryUrl() + "/" + MODELNAME_CORE;
//				sa.loadSentencesFromUrl(coreUrl, tgtSrc);
//			}
//			
//			// now load the specific model
//			String tgtUrl = pAc.getCeConfig().getModelDirectoryUrl() + "/" + modelName;
//			sa.loadSentencesFromUrl(tgtUrl, tgtSrc);
//
//			reportDebug("Successfully loaded CE from url " + tgtUrl, pAc);
//		}
//
//		return mb;
//	}


}
