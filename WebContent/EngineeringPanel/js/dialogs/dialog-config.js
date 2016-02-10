/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.config = new DialogConfig();

function DialogConfig() {
	this.pane = null;

	this.initialise = function() {
		gCe.msg.debug('DialogConfig', 'initialise');

		this.pane = gEp.ui.pane.config;
	};

	this.changedJavascriptDebug = function() {
		var domJsDebug = dijit.byId('jsDebug');
		var jsdValue = domJsDebug.get('value');

		gCe.msg.jsDebug = (jsdValue === 'true');
	};

	this.changedAutoRunRules = function() {
		var domAutoRunRules = dijit.byId('autoRunRules');
		var arrValue = domAutoRunRules.get('value');

		gEp.autoRunRules = (arrValue === 'true');
	};

	this.changedShowStats = function() {
		var domShowStats = dijit.byId('showStats');
		var showStats = domShowStats.get('value');

		gEp.showStats = (showStats === 'true');
	};

	this.changedReturnCe = function() {
		var domReturnCe = dijit.byId('returnCe');
		var retCe = domReturnCe.get('value');

		gEp.returnCe = (retCe === 'true');
	};

	this.updateCeStoreList = function(pResponseObject, pUserParms) {
		var domCeStoreList = dijit.byId(this.pane.DOM_CESTORELIST);
		var fpData = {
				identifier: 'id',
				label: 'name',
				items: []
		};

		var fpStore = new dojo.data.ItemFileWriteStore({
			data: fpData
		});

		domCeStoreList.set('store', fpStore);

		if (!gCe.utils.isNullOrEmpty(gEp.allStores)) {
			for (var key in gEp.allStores) {
				var thisCeStore = gEp.allStores[key];
				fpStore.newItem( { id: thisCeStore._id, name: thisCeStore._id } );
			}
		}

		domCeStoreList.set('value', gEp.currentCeStore);
	};

	this.changedCeStore = function() {
		var domCeStoreList = dijit.byId(this.pane.DOM_CESTORELIST);

		gEp.handlers.stores.switchToStore(domCeStoreList.value);
	};

	this.createStore = function(pStoreName) {
		createNewStore(pStoreName, false);
	};

	this.createStoreAndSwitchTo = function(pStoreName) {
		createNewStore(pStoreName, true);
	};

	function createNewStore(pStoreName, pSwitch) {
		var tgtName = '';
		
		if (!gCe.utils.isNullOrEmpty(pStoreName)) {
			tgtName = pStoreName;
		} else {
			tgtName = prompt('Please enter the name for this new CE Store', 'DEFAULT');
		}

		if (!gCe.utils.isNullOrEmpty(tgtName)) {
			gEp.handler.stores.createStore(tgtName);

			if (pSwitch) {
				gEp.dlg.config.setCurrentCeStore(tgtName);
			}

			//Also refresh the list of all items
			gEp.listAllCoreItems();
		}
	};

	this.deleteStore = function(pStoreName) {
		var storeName = '';

		if (!gCe.utils.isNullOrEmpty(pStoreName)) {
			storeName = pStoreName;
		} else {
			storeName = document.getElementById(this.pane.DOM_CESTORELIST).value;
		}

		var answer = confirm('Are you sure you want to delete the CE Store "' + storeName + '"');

		if (answer) {
			gEp.handler.stores.deleteStore(storeName);

			if (gEp.isThisCeStoreCurrent(storeName)) {
				this.setCurrentCeStore(gCe.DEFAULT_CESTORE);
			}

			//Also refresh the list of all items
			gEp.listAllCoreItems();
		}
	};

	this.resetStore = function(pStoreName) {
		var storeName = '';

		if (!gCe.utils.isNullOrEmpty(pStoreName)) {
			storeName = pStoreName;
		} else {
			//No store name is specified so use the current one
			storeName = gEp.currentCeStore;
		}

		var answer = confirm('Are you sure you want to reset the CE Store "' + storeName + '"');

		if (answer) {
			gEp.handler.stores.resetStore(storeName);

			//Also refresh the list of all items
			gEp.listAllCoreItems();
		}
	};

	this.refreshServerAddressList = function() {
		var domServerList = dijit.byId(this.pane.DOM_SERVERLIST);
		var fpData = {
				identifier: 'id',
				label: 'name',
				items: []
		};

		var fpStore = new dojo.data.ItemFileWriteStore({
			data: fpData
		});

		domServerList.set('store', fpStore);

		if (!gCe.utils.isNullOrEmpty(gEp.serverAddresses)) {
			for (var key in gEp.serverAddresses) {
				var thisAddr = gEp.serverAddresses[key];
				fpStore.newItem( { id: thisAddr, name: thisAddr } );
			}
		}

		domServerList.set('value', gEp.currentServerAddress);
	};

	this.changedServer = function() {
		var domServerList = dijit.byId(this.pane.DOM_SERVERLIST);

		this.setCurrentServer(domServerList.value);
	};

	this.createNewServerAddress = function() {
		var newAddress = prompt('Please enter the url for a CE Store', 'http://localhost:8080/');

		if (!gCe.utils.isNullOrEmpty(newAddress)) {
			var lastChar = newAddress.slice(-1);
			
			//Ensure that a trailing slash is always present
			if (lastChar !== '/') {
				newAddress += '/';
			}
			
			gEp.serverAddresses.push(newAddress);			
			this.refreshServerAddressList();
		}
	};

	this.setCurrentCeStore = function(pStoreName) {
		gEp.handler.stores.switchToStore(pStoreName);
	};

	this.setCurrentServer = function(pServerName) {
		gEp.currentServerAddress = pServerName;
		
		gCe.msg.debug(pServerName);

		//Also refresh the list of all items
		gEp.listAllCoreItems();
	};

	this.editConfigValue = function(pKey, pValue) {
		var newVal = prompt('Please specify a new value for the \'' + pKey + '\' property', pValue);

		if ((newVal !== null) && (newVal !== pValue)) {
			var cbf = function() { gEp.handler.actions.showStoreConfiguration(); };
			gCe.api.special.updateConfigValue(gEp.stdHttpParms(), cbf, null, pKey, newVal);
		}
	};

}