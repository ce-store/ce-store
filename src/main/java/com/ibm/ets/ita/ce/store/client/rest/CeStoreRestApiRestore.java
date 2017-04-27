package com.ibm.ets.ita.ce.store.client.rest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import static com.ibm.ets.ita.ce.store.names.RestNames.PARM_NEWNAME;
//import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportInfo;

import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;

public class CeStoreRestApiRestore extends CeStoreRestApi {

	public CeStoreRestApiRestore(WebActionContext pWc, ArrayList<String> pRestParts, HttpServletRequest pRequest) {
		super(pWc, pRestParts, pRequest);
		// TODO Auto-generated constructor stub
	}

	/*
	 * Supported requests: /restore
	 */

	public boolean processRequest() {
		if (isPut()) {
			try {
				//reportInfo("Received model to restore",this.wc);
				GZIPInputStream gz = new GZIPInputStream(this.request.getInputStream());
				ObjectInputStream oin = new ObjectInputStream(gz) ;
				ModelBuilder tgtMb = (ModelBuilder) oin.readObject() ;
				tgtMb.setCeStoreName(this.wc.getModelBuilder().getCeStoreName());
				ServletStateManager.getServletStateManager().getModelBuilderMap().put(tgtMb.getCeStoreName(), tgtMb);
				oin.close();
				gz.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			reportUnsupportedMethodError();
		}
		return false;

	}

}
