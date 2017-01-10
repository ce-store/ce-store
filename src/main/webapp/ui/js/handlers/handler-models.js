/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.models = new HandlerModels();

function HandlerModels() {
	var ren = null;		//This is set during initialise

	this.initialise = function() {
		gCe.msg.debug('HandlerModels', 'initialise');

		ren = gEp.renderer.models;
	};

	this.listAllModels = function(pCbf, pUserParms) {
		var cbf = null;

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.models.processModelList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.models.listAll(gEp.stdHttpParms(), cbf, userParms);
	};

	this.getModelDetails = function(pModelName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { model_name: pModelName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.models.processModelDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.models.getDetailsFor(gEp.stdHttpParms(), cbf, pModelName, userParms);
	};

	this.listSentencesForModel = function(pModelName, pFullDetails, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { model_name: pModelName, full_detail: pFullDetails };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.models.listSentencesFor(gEp.stdHttpParms(), cbf, pModelName, userParms);
	};

	this.processModelList = function(pResponse, pUserParms) {
		var modList = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderModelsList(modList, pUserParms);
	};

	this.processModelDetails = function(pResponse, pUserParms) {
		var thisModel = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderModelDetails(thisModel, pUserParms);
	};

}
