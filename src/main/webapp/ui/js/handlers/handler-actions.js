/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.actions = new HandlerActions();

function HandlerActions() {
	var FORMNAME_PREFIX = 'processCeCommands - ';

	var ren = null;		//This is set during initialise

	var iUserList = null;

	this.initialise = function() {
		gCe.msg.debug('HandlerActions', 'initialise');

		ren = gEp.renderer.actions;
	};

	this.processCommandsRelative = function(pUrl, pFormName, pCbf, pUserParms) {
		var arr = gEp.autoRunRules;
		var rc = gEp.returnCe;
		var fullUrl = null;

		if (gCe.utils.startsWith(pUrl, '.')) {
			var trimmedUrl = pUrl.substring(2, pUrl.length);
			fullUrl = gEp.currentServerAddress + gEp.currentAppName + trimmedUrl;
		} else if (gCe.utils.startsWith(pUrl, '/')){
			var trimmedUrl = pUrl.substring(1, pUrl.length);
			fullUrl = gEp.currentServerAddress + trimmedUrl;
		} else {
			fullUrl = gEp.currentServerAddress + pUrl;
		}

		var ceLoadText = 'perform load sentences from url \'' + fullUrl + '\'.';

		var cbf = null;
		var localUserParms = { url: pUrl, full_url: fullUrl, form_name: FORMNAME_PREFIX + pFormName, ce_text: ceLoadText, auto_run_rules: arr, return_ce: rc };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportLoadResultsAndRefresh(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.sentences.add(gEp.stdHttpParms(), cbf, ceLoadText, arr, rc, userParms);
	};

	this.processCommandsAbsolute = function(pUrl, pSrcName, pCbf, pUserParms) {
		var arr = gEp.autoRunRules;
		var rc = gEp.returnCe;
		var ceLoadText = 'perform load sentences from url \'' + pUrl + '\'';
		var cbf = null;
		var localUserParms = { url: pUrl, ce_text: ceLoadText, auto_run_rules: arr };

		if (pSrcName != null) {
			ceLoadText += ' into source \'' + pSrcName + '\'';			
		}

		ceLoadText += '.';
		
		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportLoadResultsAndRefresh(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.sentences.add(gEp.stdHttpParms(), cbf, ceLoadText, arr, rc, userParms);
	};

	this.listMultipleConceptInstances = function(pConNames, pSince, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { concept_names: pConNames, since: pSince };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.instances.processMultiConceptInstanceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.special.listInstancesForMultipleConcepts(gEp.stdHttpParms(), cbf, pConNames, pSince, userParms);
	};

	this.login = function(pCbf, pUserParms) {
		var cbf = null;

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.actions.openLoginDialog(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		this.listUsers(cbf, userParms);
	};

	this.openLoginDialog = function(pResponse, pUserParms) {
		iUserList = processUserList(pResponse);

		gEp.dlg.user.openLoginList(iUserList, pUserParms);
	};

	this.showLibraryVersions = function() {
		gCe.msg.alert(ren.renderLibraryVersionText());
	};

	this.logout = function() {
		gEp.clearLoggedInUser();
	};

	this.saveLoggedInUser = function() {
		gEp.dlg.user.saveSelectedUser(iUserList);
	};

	this.cancelLoginDialog = function() {
		var formDlg = dijit.byId('formUserLogin');
		formDlg.hide();
	};

	this.listUsers = function(pCbf, pUserParms) {
		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.special.listUsers(gEp.stdHttpParms(), pCbf, userParms);
	};

	this.setDebug = function(pDebug, pCbf, pUserParms) {
		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.special.updateConfigValue(gEp.stdHttpParms(), pCbf, userParms, 'debug', pDebug);
	};

	this.keywordSearch = function() {
		var keywords = document.getElementById('searchTerms').value;

		var userParms = { terms: keywords };
		var cbf = function(pResponseObject, userParms) { gEp.handler.stores.processKeywordSearch(pResponseObject, userParms); };
		gCe.api.special.keywordSearch(gEp.stdHttpParms(), cbf, userParms, keywords);
	};

	this.resetStore = function() {
		if (confirm('This will empty the current CE Store.  Are you sure?')) {
			var userParms = { store_name: gEp.currentCeStore };
			var cbf = function(pResponseObject, userParms) { gEp.handler.stores.processResetCeStore(pResponseObject, userParms); };

			var ceText = 'perform reset store.';
			var arr = gEp.autoRunRules;
			var rc = gEp.returnCe;

			gCe.api.sentences.add(gEp.stdHttpParms(), cbf, ceText, arr, rc, userParms);
		}
	};

	this.reloadStore = function() {
		if (confirm('This will reload the current CE Store.  Are you sure?')) {
			var userParms = { store_name: gEp.currentCeStore };
			var cbf = function(pResponseObject, userParms) { gEp.handler.stores.processReloadCeStore(pResponseObject, userParms); };

			var ceText = 'perform reload store.';
			var arr = gEp.autoRunRules;
			var rc = gEp.returnCe;

			gCe.api.sentences.add(gEp.stdHttpParms(), cbf, ceText, arr, rc, userParms);
		}
	};

	this.loadStore = function() {
		if (confirm('This will empty the current CE Store and reload from the the last saved state.  Are you sure?')) {
			var userParms = { store_name: gEp.currentCeStore };
			var cbf = function(pResponseObject, userParms) { gEp.handler.stores.processLoadCeStore(pResponseObject, userParms); };

			var ceText = 'perform load store.';
			var arr = gEp.autoRunRules;
			var rc = gEp.returnCe;

			gCe.api.sentences.add(gEp.stdHttpParms(), cbf, ceText, arr, rc, userParms);
		}
	};

	this.saveStore = function() {
		if (confirm('This will save the current state of the CE Store, removing any previously saved state.  Are you sure?')) {
			var userParms = { store_name: gEp.currentCeStore };
			var cbf = function(pResponseObject, userParms) { gEp.handler.stores.processSaveCeStore(pResponseObject, userParms); };

			var ceText = 'perform save store.';
			var arr = gEp.autoRunRules;
			var rc = gEp.returnCe;

			gCe.api.sentences.add(gEp.stdHttpParms(), cbf, ceText, arr, rc, userParms);
		}
	};

	this.emptyInstanceData = function() {
		if (confirm('This will empty the instances (but leave the model) for the current CE Store.  Are you sure?')) {
			var userParms = { store_name: gEp.currentCeStore };
			var cbf = function(pResponseObject, userParms) { gEp.handler.stores.processEmptyCeStore(pResponseObject, userParms); };

			var ceText = 'perform empty instances.';
			var arr = gEp.autoRunRules;
			var rc = gEp.returnCe;

			gCe.api.sentences.add(gEp.stdHttpParms(), cbf, ceText, arr, rc, userParms);
		}
	};

	this.showStoreStatistics = function(pCbf, pUserParms) {
		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.special.showStoreStatistics(gEp.stdHttpParms(), pCbf, userParms);
	};

	this.showNextUid = function() {
		var cbf = function(pResponseObject, userParms) { gEp.handler.stores.showUidResponse(pResponseObject, userParms); };

		gCe.api.special.showNextUid(gEp.stdHttpParms(), cbf, null);
	};

	this.getNextUid = function() {
		var cbf = function(pResponseObject, userParms) { gEp.handler.stores.showUidResponse(pResponseObject, userParms); };

		gCe.api.special.getNextUid(gEp.stdHttpParms(), cbf, null);
	};

	this.resetUids = function() {
		var cbf = function(pResponseObject, userParms) { gEp.handler.stores.showUidResponse(pResponseObject, userParms); };

		gCe.api.special.resetUids(gEp.stdHttpParms(), cbf, null);
	};

	this.getUidBatch = function() {
		var batchSize = prompt('How many UIDs do you want to get?', '100');
		
		if (!gCe.utils.isNullOrEmpty(batchSize)) {
			var cbf = function(pResponseObject, userParms) { gEp.handler.stores.showUidResponse(pResponseObject, userParms); };

			gCe.api.special.getUidBatch(gEp.stdHttpParms(), cbf, null, batchSize);
		}
	};

	this.listShadowConcepts = function() {
		var cbf = function(pResponseObject, userParms) { gEp.handler.concepts.processShadowConceptList(pResponseObject, userParms); };

		gCe.api.special.listShadowConcepts(gEp.stdHttpParms(), cbf, {});
	};

	this.listShadowInstances = function() {
		var cbf = function(pResponseObject, userParms) { gEp.handler.instances.processShadowInstanceList(pResponseObject, userParms); };

		gCe.api.special.listShadowInstances(gEp.stdHttpParms(), cbf, {});
	};

	this.listUnreferencedInstances = function() {
		var cbf = function(pResponseObject, userParms) { gEp.handler.instances.processUnreferencedInstanceList(pResponseObject, userParms); };

		gCe.api.special.listUnreferencedInstances(gEp.stdHttpParms(), cbf, {});
	};

	this.listUnreferencedInstancesNoMetaModel = function() {
		var cbf = function(pResponseObject, userParms) { gEp.handler.instances.processUnreferencedInstanceList(pResponseObject, userParms); };

		gCe.api.special.listUnreferencedInstances(gEp.stdHttpParms(), cbf, { ignoreMetaModel: true });
	};

	this.listDiverseConceptInstances = function() {
		var cbf = function(pResponseObject, userParms) { gEp.handler.instances.processDiverseConceptInstanceList(pResponseObject, userParms); };

		gCe.api.special.listDiverseConceptInstances(gEp.stdHttpParms(), cbf, {});
	};

	this.listAllRationale = function() {
		gCe.msg.error('listAllRationale not yet implemented');
	};

	this.showStoreConfiguration = function(pUserParms) {
		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		var cbf = function(pResponse, pUserParms) { gEp.renderer.actions.renderStoreConfigList(pResponse, pUserParms); };
		gCe.api.special.showStoreConfig(gEp.stdHttpParms(), cbf, userParms);
	};

	function processUserList(pResponse, pUserParms) {
		var rows = gCe.utils.getStructuredResponseFrom(pResponse, pUserParms);
		var userList = [];

		if (!gCe.utils.isNullOrEmpty(rows)) {
			for (var key in rows) {
				var respFrag = rows[key];
				userList.push(extractUserFrom(respFrag));
			}
		}

		return userList;
	}

	function extractUserFrom(pFrag) {
		var user = {};

		if (!gCe.utils.isNullOrEmpty(pFrag)) {
			user.userName = pFrag._id;
			user.screenName = pFrag.property_values.screen_name;
		}

		return user;
	}

}
