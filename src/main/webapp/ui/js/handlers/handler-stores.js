/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.stores = new HandlerStores();

function HandlerStores() {
	var ren = null;		//This is set during initialise

	this.initialise = function() {
		gCe.msg.debug('HandlerStores', 'initialise');

		ren = gEp.renderer.stores;
	};

	this.showUidResponse = function(pResponse, pUserParms) {
		var sr = gCe.utils.getStructuredResponseFrom(pResponse);

		if (pUserParms.action === 'reset UIDs') {
			//Ignore this response - it has been reported already (it is just an alert message)
		} else if (pUserParms.action === 'get UID batch') {
			alert('The result of the \'' + pUserParms.action + '\' request is:\n  batch size=\'' + sr.batch_size + '\',\n  first uid=\'' + sr.batch_start + '\',\n  last uid=\'' + sr.batch_end + '\'');
		} else {
			alert('The result of the \'' + pUserParms.action + '\' request is \'' + sr.value + '\'');
		}
	};

	this.listAllStores = function(pCbf, pUserParms) {
		var cbf = null;

		if (pCbf == null ) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.stores.processStoresList(pResponseObject, pUserParms, true); };
		} else {
			//We still need to save the stores so create a new callback that does both
			cbf = function(pResponseObject, pUserParms) {
				gEp.handler.stores.processStoresList(pResponseObject, pUserParms, false);
				pCbf(pResponseObject, pUserParms);
			};
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.stores.listAll(gEp.stdHttpParms(), cbf, userParms);

		gEp.ui.pane.general.setLastRequest(function() {gEp.handler.stores.listAllStores();});		
	};

	this.refreshStoreList = function() {
		var cbf = function(pResponseObject, pParms) { gEp.dlg.config.updateCeStoreList(pResponseObject, pParms); };
		var userParms = { show_tab: false };
		gEp.handler.stores.listAllStores(cbf, userParms);
	};

	this.getStoreDetails = function(pStoreName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { store_name: pStoreName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.stores.processStoreDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.stores.getStoreFor(gEp.stdHttpParms(), cbf, pStoreName, userParms);
	};

	this.createStore = function(pStoreName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { store_name: pStoreName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.stores.processCreatedCeStore(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.stores.createNew(gEp.stdHttpParms(), cbf, pStoreName, userParms);
	};

	this.deleteStore = function(pStoreName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { store_name: pStoreName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.stores.processDeletedCeStore(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.stores.deleteStore(gEp.stdHttpParms(), cbf, pStoreName, userParms);
	};

	this.backupStore = function(pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = {};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms, pXhr) { gEp.handler.stores.processBackedUpCeStore(pResponseObject, pUserParms, pXhr); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.stores.backup(gEp.stdHttpParms(), cbf, gEp.currentCeStore, userParms);
	};

	this.restoreStoreFromForm = function(pForm) {
	    var formName = pForm.id;
		var localUserParms = {};

		var cbf = function(pResponseObject, pUserParms) { gEp.handler.stores.processRestoredCeStore(pResponseObject, pUserParms); };
		gCe.api.stores.restore(gEp.stdHttpParms(), cbf, gEp.currentCeStore, pForm, localUserParms);
	};

	this.switchToStore = function(pStoreId) {
		gEp.currentCeStore = pStoreId;

		//Also refresh the list of all items
		gEp.listAllCoreItems();
	};

	this.processStoresList = function(pResponse, pUserParms, pRender) {
		var storesList = gCe.utils.getStructuredResponseFrom(pResponse);

		//Since this was a request for all stores they should also be saved
		gEp.storeStores(storesList);

		if (pRender) {
			ren.renderStoresList(storesList, pUserParms);
		}
	};

	this.processKeywordSearch = function(pResponse, pUserParms) {
		var sr = gCe.utils.getStructuredResponseFrom(pResponse);

		var hdrs = [ '#', 'Instance', 'Property', 'Value' ];
		var rows = [];
		var rawTerms = null;
		var highlightedTerms = '<font style="background-color:lightgreen">' + rawTerms + '</font>';

		if (!gCe.utils.isNullOrEmpty(sr) && !gCe.utils.isNullOrEmpty(sr.search_results)) {
			var ctr = 0;
			var finalTerms = [];
			rawTerms = sr.search_terms;

			for (var key in rawTerms) {
				var thisTerm = rawTerms[key];
				var trimmedTerm = null;

				if ((thisTerm != 'OR') && (thisTerm != 'AND') && (thisTerm != 'NOT')) {
					finalTerms.push(thisTerm);
				}
			}

			for (var idx in sr.search_results) {
				var thisRow = sr.search_results[idx];

				var instLink = gEp.ui.links.instanceDetails(thisRow.instance_name, thisRow.instance_label);
				var conLinks = "";
				var sep = "";

				for (var key in thisRow.concept_names) {
					var thisConName = thisRow.concept_names[key];
					conLinks += sep + gEp.ui.links.conceptDetails(thisConName);
					sep = ", ";
				}

				var instDetails = instLink + '<br>(' + conLinks + ')';
				var propDetails = gEp.ui.links.propertyDetails(thisRow.property_name);
				var valDetails = thisRow.property_value;

				for (var key in finalTerms) {
					var thisTerm = finalTerms[key];
					valDetails = gCe.utils.replaceAllCaseInsensitive(valDetails, thisTerm, '<span class="searchterm">' + thisTerm + '</span>')
				}

				if (thisRow.property_type !== 'value') {
					var linkDetails = gEp.ui.links.instanceDetails(thisRow.property_value, 'link');
					valDetails += ' [' + linkDetails + ']';
				}

				var thisResult = [ ++ctr, instDetails, propDetails, valDetails ];

				rows.push(thisResult);
			}
		}

		ren.renderSearchResult(hdrs, rows, rawTerms);
	};

	this.processStoreDetails = function(pResponse, pUserParms) {
		var thisStore = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderStoreDetails(thisStore, pUserParms);
	};

	this.processCreatedCeStore = function(pResponse, pUserParms) {
		//Nothing needs to be done here
	};

	this.processBackedUpCeStore = function(pResponse, pUserParms, pXhr) {
		var cd = pXhr.getResponseHeader('content-disposition');
		var ct = pXhr.getResponseHeader('content-type');
		var regex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
		var match = regex.exec(cd);
		var fn = match[1];

		try {
			var blob = new Blob([pResponse], {type: ct});

			if (window.navigator && window.navigator.msSaveOrOpenBlob) {
				window.navigator.msSaveOrOpenBlob(blob, fn);
			} else {
				// TODO Find some way of setting the filename
				var objectUrl = URL.createObjectURL(blob);
				window.open(objectUrl);
			}
		} catch (e) {
			console.error('Error saving backed up ce-store');
			console.error(e);
		}

	};

	this.processRestoredCeStore = function(pResponse, pUserParms, pXhr) {
		//Just do a standard refresh
		gEp.listAllCoreItems();

		gCe.msg.alert('ce-store has been restored from backup');
	};

	this.processDeletedCeStore = function(pResponse, pUserParms) {
		var storeName = pUserParms.store_name;

		//Delete the local reference to the CE store
		delete gEp.allStores[storeName];
	};

	this.processResetCeStore = function(pResponseObject, pUserParms) {
		alert('The store has been reset');
	};

	this.processReloadCeStore = function(pResponseObject, pUserParms) {
		alert('The store has been reloaded');
	};

	this.processEmptyCeStore = function(pResponseObject, pUserParms) {
		alert('The store has been emptied');
	};

	this.processLoadCeStore = function(pResponseObject, pUserParms) {
		alert('The store has been loaded from the last saved state');
	};

	this.processSaveCeStore = function(pResponseObject, pUserParms) {
		alert('The current store state has been saved');
	};

}
