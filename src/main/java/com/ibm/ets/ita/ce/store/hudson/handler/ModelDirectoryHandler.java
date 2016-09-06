package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonParser;
import com.ibm.ets.ita.ce.store.hudson.helper.HudsonManager;
import com.ibm.ets.ita.ce.store.hudson.model.ConvConfig;
import com.ibm.ets.ita.ce.store.model.CeRule;

public class ModelDirectoryHandler extends QuestionHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";


	private static final String CE_HUDSON_SRC = "HUDSON";
	private static final String JSON_SM = "system_message";

	public ModelDirectoryHandler(ActionContext pAc, boolean pDebug, long pStartTime) {
		super(pAc, pDebug, null, pStartTime);
	}
	
	@Override
	public CeStoreJsonObject handleQuestion() {
		//Nothing is needed here - no question is ever asked

		return null;
	}
	
	public CeStoreJsonObject handleList(){
		CeStoreJsonObject result = new CeStoreJsonObject();	
		StringBuilder stringBuilder = new StringBuilder();
		try{
		    URL url = new URL(this.ac.getModelBuilder().getModelDirectoryUrl());
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
	 
	    result.put("models", parser.getRootJsonArray());
	    
		return result;
	}
	
	public CeStoreJsonObject handleLoad(String modelName){
		
		CeStoreJsonObject result = new CeStoreJsonObject();	

		//Just reset the store and clear the config
		ServletStateManager ssm = ServletStateManager.getServletStateManager();
		HudsonManager hm = ServletStateManager.getHudsonManager(this.ac);

		ssm.deleteModelBuilder(this.ac, this.ac.getModelBuilder());
		hm.deleteConvConfig();
		hm.refreshQuestionLog(this.ac);
		hm.clearCaches(this.ac);
		this.ac.clearIndexedEntityAccessor();

		setupCeStore(this.ac, ssm, hm, modelName);

		result.put(JSON_SM, "The CE Store has been reset");
		result.put(JSON_ET, System.currentTimeMillis() - this.startTime);

		
	    
		return result;
	}
	
	
	
	protected static void setupCeStore(ActionContext pAc, ServletStateManager pSsm, HudsonManager pHm, String modelName) {
		String storeName = ModelBuilder.CESTORENAME_DEFAULT;
		ModelBuilder mb = pSsm.getModelBuilder(storeName);
		boolean storeWasLoaded = false;

		if (mb == null) {
			//The CE Store is not yet set up, so initialise it
			mb = loadCeStore(pAc, storeName, modelName, true);
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

	private static synchronized ModelBuilder loadCeStore(ActionContext pAc, String pStoreName, String modelName, boolean loadCore) {
		System.out.println("loading model from dir: " + modelName);
		ServletStateManager ssm = ServletStateManager.getServletStateManager();
		ModelBuilder mb = ssm.getModelBuilder(pStoreName);

		//Do the null test again since this method is synchronized and other requests
		//may have been blocked and are now flowing through but the CE store has now
		//been set up whilst they have been waiting
		if (mb == null) {
			mb = ssm.createModelBuilder(pAc, pStoreName);
			reportDebug("Successfully set up CE Store named " + pStoreName, pAc);

			StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
			String tgtSrc = CE_HUDSON_SRC;
			
			//load core
			if(loadCore && !modelName.equalsIgnoreCase("core")){
				String coreUrl = "http://ce-models.eu-gb.mybluemix.net/core";
				sa.loadSentencesFromUrl(coreUrl, tgtSrc);
			}
			
			// now load the specific model
			String tgtUrl = "http://ce-models.eu-gb.mybluemix.net/"+modelName;
			sa.loadSentencesFromUrl(tgtUrl, tgtSrc);

			reportDebug("Successfully loaded CE from url " + tgtUrl, pAc);
		}

		return mb;
	}


}
