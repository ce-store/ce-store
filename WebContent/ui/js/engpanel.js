/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

function EngineeringPanel() {
	this.DEFAULT_CESTORE = 'DEFAULT';
	this.DEFAULT_SERVERADDRESS = '/';
	this.DEFAULT_APPNAME = 'ce-store/';

	this.currentCeStore = this.DEFAULT_CESTORE;
	this.currentServerAddress = this.DEFAULT_SERVERADDRESS;
	this.currentAppName = this.DEFAULT_APPNAME;
	
	var jsLibraries = null;
	var extraActions = null;
	var actionsList = null;
	var ce = gCe;

	this.showStats = true;
	this.autoRunRules = false;
	this.returnCe = false;
	this.sentenceSets = {};
	this.serverAddresses = {};
	this.allStores = {};
	this.allConcepts = {};

	this.handler = {};		//specific handlers are created when each .js in /handers/ is loaded
	this.renderer = {};		//specific renderers are created when each .js in /renderers/ is loaded
	this.dlg = {};			//specific dialogs are created when each .js in /dialogs/ is loaded
	this.ui = {};

	this.initialise = function(pExtraLibraries, pExtraActions) {
		jsLibraries = initialJsLibraries(pExtraLibraries);
		extraActions = pExtraActions;

		loadJsLibraries();
	};
	
	this.startup = function() {
		ce.msg.debug('EngineeringPanel', 'initialise');

		reportAnyLibrariesNotLoaded();

		var cbfSuccess = function(pResponseObject, pUserParms) { gEp.handler.messages.showAllStats(pResponseObject, pUserParms); };
		var cbfFailure = function(pErrorList) { gEp.handler.messages.showApiErrors(pErrorList); };

		gCe.api.setAjaxCallbacks(cbfSuccess, cbfFailure);
		this.sentenceSets = this.initialSentenceSets();
		this.serverAddresses = this.initialServerAddresses();

		this.ui.initialise();

		for (var key in this.handler) { this.handler[key].initialise(); }
		for (var key in this.renderer) { this.renderer[key].initialise(); }
		for (var key in this.dlg) { this.dlg[key].initialise(); }

		for (var key in this.ui.pane) { this.ui.pane[key].initialise(); }
		for (var key in this.ui.form) { this.ui.form[key].initialise(); }

		actionsList = initialiseActionsList();
		addAnyExtraActions();

		this.ui.renderUiComponents();
		this.listAllCoreItems();

		ce.msg.debug('EP initialisation complete', 'initialise', [ gEp ]);

		document.title = window.location.hostname + ' - CE Engineering Panel';
	};

	function loadJsLibraries() {
		for (var key in jsLibraries) {
			var thisJsLib = jsLibraries[key];

			ce.msg.debug('Attempting load of: ' + thisJsLib.url + ' (' + key + ')');
			require({"async":false});
			require( [ thisJsLib.url ], function() { handleJsLoadSuccess(thisJsLib); } );
		}
	}

	function initialJsLibraries(pExtraLibraries) {
		var jsLibs = {
			dojo_01: { url:'dojo/has', loaded: false },
			dojo_02: { url:'dojox/gfx', loaded: false },
			dojo_03: { url:'dojox/gfx/move', loaded: false },
			dojo_04: { url:'dojo/data/ItemFileWriteStore', loaded: false },
			dojo_05: { url:'dojo/parser', loaded: false },
			dojo_06: { url:'dijit/Menu', loaded: false },
			dojo_07: { url:'dijit/layout/BorderContainer', loaded: false },
			dojo_08: { url:'dijit/layout/ContentPane', loaded: false },
			dojo_09: { url:'dijit/layout/AccordionContainer', loaded: false },
			dojo_10: { url:'dijit/layout/TabContainer', loaded: false },
			dojo_11: { url:'dijit/Dialog', loaded: false },
			dojo_12: { url:'dijit/form/Form', loaded: false },
			dojo_13: { url:'dijit/form/Button', loaded: false },
			dojo_14: { url:'dijit/form/ComboBox', loaded: false },
			dojo_15: { url:'dijit/form/CheckBox', loaded: false },
			dojo_16: { url:'dijit/form/SimpleTextarea', loaded: false },
			dojo_17: { url:'dijit/form/MultiSelect', loaded: false },
			dojo_18: { url:'dijit/form/FilteringSelect', loaded: false },

			cestore_01: { url:'./js/renderers/renderer-actions.js', loaded: false },
			cestore_02: { url:'./js/renderers/renderer-concepts.js', loaded: false },
			cestore_03: { url:'./js/renderers/renderer-instances.js', loaded: false },
			cestore_04: { url:'./js/renderers/renderer-messages.js', loaded: false },
			cestore_05: { url:'./js/renderers/renderer-models.js', loaded: false },
			cestore_06: { url:'./js/renderers/renderer-patterns.js', loaded: false },
			cestore_07: { url:'./js/renderers/renderer-properties.js', loaded: false },
			cestore_08: { url:'./js/renderers/renderer-rationale.js', loaded: false },
			cestore_09: { url:'./js/renderers/renderer-sentences.js', loaded: false },
			cestore_10: { url:'./js/renderers/renderer-sources.js', loaded: false },
			cestore_11: { url:'./js/renderers/renderer-stores.js', loaded: false },

			cestore_12: { url:'./js/handlers/handler-actions.js', loaded: false },
			cestore_13: { url:'./js/handlers/handler-concepts.js', loaded: false },
			cestore_14: { url:'./js/handlers/handler-instances.js', loaded: false },
			cestore_15: { url:'./js/handlers/handler-messages.js', loaded: false },
			cestore_16: { url:'./js/handlers/handler-models.js', loaded: false },
			cestore_17: { url:'./js/handlers/handler-patterns.js', loaded: false },
			cestore_18: { url:'./js/handlers/handler-properties.js', loaded: false },
			cestore_19: { url:'./js/handlers/handler-rationale.js', loaded: false },
			cestore_20: { url:'./js/handlers/handler-sentences.js', loaded: false },
			cestore_21: { url:'./js/handlers/handler-sources.js', loaded: false },
			cestore_22: { url:'./js/handlers/handler-stores.js', loaded: false },

			dialog_01: { url:'./js/dialogs/ceqb/dialog-ceqb.js', loaded: false },
			dialog_02: { url:'./js/dialogs/ceqb/dialog-ceqb-actions.js', loaded: false },
			dialog_03: { url:'./js/dialogs/ceqb/dialog-ceqb-dialog.js', loaded: false },
			dialog_04: { url:'./js/dialogs/ceqb/dialog-ceqb-drawing.js', loaded: false },
			dialog_05: { url:'./js/dialogs/ceqb/dialog-ceqb-generation.js', loaded: false },
			dialog_06: { url:'./js/dialogs/ceqb/dialog-ceqb-links.js', loaded: false },
			dialog_07: { url:'./js/dialogs/ceqb/dialog-ceqb-menu.js', loaded: false },
			dialog_08: { url:'./js/dialogs/ceqb/dialog-ceqb-model.js', loaded: false },
			dialog_09: { url:'./js/dialogs/ceqb/dialog-ceqb-move.js', loaded: false },
			dialog_10: { url:'./js/dialogs/ceqb/dialog-ceqb-render.js', loaded: false },
			dialog_11: { url:'./js/dialogs/ceqb/dialog-ceqb-response.js', loaded: false },

			dialog_12: { url:'./js/dialogs/dnd/dialog-dnd.js', loaded: false },

			dialog_15: { url:'./js/dialogs/conv/dialog-conv.js', loaded: false },
			dialog_16: { url:'./js/dialogs/conv/dialog-conv-actions.js', loaded: false },
			dialog_17: { url:'./js/dialogs/conv/dialog-conv-handler.js', loaded: false },
			dialog_18: { url:'./js/dialogs/conv/dialog-conv-links.js', loaded: false },

			dialog_20: { url:'./js/dialogs/dialog-config.js', loaded: false },
			dialog_21: { url:'./js/dialogs/dialog-sentence.js', loaded: false },
			dialog_22: { url:'./js/dialogs/dialog-user.js', loaded: false },

			ui_01: { url:'./js/ui/ui.js', loaded: false },
			ui_02: { url:'./js/ui/links.js', loaded: false },
			ui_03: { url:'./js/ui/panes/pane-action.js', loaded: false },
			ui_04: { url:'./js/ui/panes/pane-addce.js', loaded: false },
			ui_05: { url:'./js/ui/panes/pane-ceqb.js', loaded: false },
			ui_06: { url:'./js/ui/panes/pane-concept.js', loaded: false },
			ui_07: { url:'./js/ui/panes/pane-config.js', loaded: false },
			ui_08: { url:'./js/ui/panes/pane-conversation.js', loaded: false },
			ui_10: { url:'./js/ui/panes/pane-debug.js', loaded: false },
			ui_12: { url:'./js/ui/panes/pane-entity.js', loaded: false },
			ui_13: { url:'./js/ui/panes/pane-error.js', loaded: false },
			ui_14: { url:'./js/ui/panes/pane-general.js', loaded: false },
			ui_15: { url:'./js/ui/panes/pane-pattern.js', loaded: false },
			ui_16: { url:'./js/ui/panes/pane-search.js', loaded: false },
			ui_17: { url:'./js/ui/panes/pane-sentence.js', loaded: false },
			ui_18: { url:'./js/ui/panes/pane-source.js', loaded: false },
			ui_19: { url:'./js/ui/panes/pane-status.js', loaded: false },
			ui_20: { url:'./js/ui/panes/pane-timeline.js', loaded: false },
			ui_21: { url:'./js/ui/panes/pane-warning.js', loaded: false },
			ui_22: { url:'./js/ui/panes/pane-info.js', loaded: false },

			ui_23: { url:'./js/ui/forms/form-choosefilter.js', loaded: false },
			ui_24: { url:'./js/ui/forms/form-chooserel.js', loaded: false },
			ui_25: { url:'./js/ui/forms/form-editce.js', loaded: false },
			ui_26: { url:'./js/ui/forms/form-login.js', loaded: false }
		};

		//Now add in any extra libraries
		for (var key in pExtraLibraries) {
			if (jsLibs[key] != null) {
				gEp.reportError('Overwriting existing js library at ' + key);
			}

			jsLibs[key] = pExtraLibraries[key];
		}

		return jsLibs;
	}

	this.initialSentenceSets = function() {
		return {
			main: { 
				title: 'Add CE from URL',
				links: [ 
					{ url: './ce/medicine/cmd/med_load.cecmd', name: 'Medicine' },
					{ url: '', name: 'Specify URL ...' }
				]
			}
		};
	};

	this.initialServerAddresses = function() {
		return [
				this.DEFAULT_SERVERADDRESS,
				'http://localhost:8080/'
			];
	};

	this.reportError = function(pErrorMsg) {
		console.error(pErrorMsg);
	};

	this.reportWarning = function(pErrorMsg) {
		console.warn(pErrorMsg);
	};

	function handleJsLoadSuccess(pJsLib) {
		pJsLib.loaded = true;
		ce.msg.debug('Successfully loaded: ' + pJsLib.url);
	}

	function reportAnyLibrariesNotLoaded() {
		for (var key in gEp.jsLibraries) {
			var thisJsLib = gEp.jsLibraries[key];

			if (!thisJsLib.loaded) {
				ce.msg.warning('JS library \'' + thisJsLib.url + '\' has not been loaded');
			}
		}
	}

	this.getDomainAndApp = function() {
		return this.currentServerAddress + this.currentAppName;
	};

	this.listAllCoreItems = function() {
		var userParms = { clearAlerts: false, showPane: false };

		//Request all concepts and sources
		gEp.handler.concepts.listAllConcepts(null, userParms);
		gEp.handler.sources.listAllSources(null, userParms);
		gEp.refreshGeneralPaneIfNeeded();
	};
	
	function initialiseActionsList() {
		var actList = {};

		actList.store = [];
		initialiseStoreActions(actList.store);

		actList.model = [];
		initialiseModelActions(actList.model);

		actList.source = [];
		initialiseSourceActions(actList.source);		

		actList.instance = [];
		initialiseInstanceActions(actList.instance);		

		actList.concept = [];
		initialiseConceptActions(actList.concept);		

		return actList;
	}

	function addAnyExtraActions() {
		for (var key in extraActions) {
			for (var actIdx in extraActions[key]) {
				var thisAction = extraActions[key][actIdx];
				actionsList[key].push(thisAction);
			}
		}
	}

	function initialiseStoreActions(pActList) {
		var newAction = null;

		//Store: create store
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_LISTFOOTER ];
		newAction.linkText = 'Create new CE Store';
		newAction.hoverText = 'Create new CE Store';
		newAction.jsMethodName = 'gEp.dlg.config.createStore';
		newAction.jsParms = [];
		pActList.push(newAction);

		//Store: create and switch to store
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_LISTFOOTER ];
		newAction.linkText = 'Create (and switch to) new CE Store';
		newAction.hoverText = 'Create a new CE Store and switch to it';
		newAction.jsMethodName = 'gEp.dlg.config.createStoreAndSwitchTo';
		newAction.jsParms = [];
		pActList.push(newAction);

		//Store: delete store
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_LISTITEM, gEp.ui.MODE_DETAILS ];
		newAction.linkText = 'Delete this CE Store';
		newAction.hoverText = 'Delete the CE Store \'{item}._id\'';
		newAction.jsMethodName = 'gEp.dlg.config.deleteStore';
		newAction.jsParms = [ '{item}._id' ];
		pActList.push(newAction);

		//Store: switch to store
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_LISTITEM, gEp.ui.MODE_DETAILS ];
		newAction.linkText = 'Switch to this CE Store';
		newAction.hoverText = 'Switch to the CE Store \'{item}._id\'';
		newAction.jsMethodName = 'gEp.handler.stores.switchToStore';
		newAction.jsParms = [ '{item}._id' ];
		pActList.push(newAction);
	}
	
	function initialiseModelActions(pActList) {
		//Nothing to do here
	}

	function initialiseSourceActions(pActList) {
		var newAction = null;

		//Source: Delete (in list mode)
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_LISTITEM ];
		newAction.linkText = '[d]';
		newAction.hoverText = 'Delete this source';
		newAction.jsMethodName = 'gEp.handler.sources.deleteSource';
		newAction.jsParms = [ '{item}._id' ];		
		pActList.push(newAction);

		//Source: Delete (in details mode)
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_DETAILS ];
		newAction.linkText = 'Delete this source';
		newAction.hoverText = 'Delete this source';
		newAction.jsMethodName = 'gEp.handler.sources.deleteSource';
		newAction.jsParms = [ '{item}._id' ];		
		pActList.push(newAction);

		//Source: Get CE text from
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_DETAILS ];
		newAction.linkText = 'Get CE text from';
		newAction.hoverText = 'Get all of the CE sentences defined in this source';
		newAction.jsMethodName = 'gEp.handler.sources.getSentenceTextFor';
		newAction.jsParms = [ '{item}._id' ];		
		pActList.push(newAction);
	}
	
	function initialiseInstanceActions(pActList) {
		var newAction = null;

		//Instance: Use CE
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_DETAILS ];
		newAction.linkText = 'Use CE';
		newAction.hoverText = 'Create a CE fragment ready to be completed and saved by the user';
		newAction.jsMethodName = 'gEp.dlg.sentence.useCeForInstance';
		newAction.jsParms = [ '{item}._id', '{item}.direct_concept_names' ];
		pActList.push(newAction);
	}

	function initialiseConceptActions(pActList) {
		var newAction = null;

		//Concept: Show meta-model instance
		newAction = {};
		newAction.filters = [ gEp.ui.MODE_DETAILS ];
		newAction.linkText = 'Show meta-model instance';
		newAction.hoverText = 'Show the meta-model instance details for this concept';
		newAction.jsMethodName = 'gEp.handler.instances.getInstanceDetails';
		newAction.jsParms = [ '{item}._id' ];
		pActList.push(newAction);
	}

	this.storeConcepts = function(pConList) {
		if (!ce.utils.isNullOrEmpty(pConList)) {
			for (var key in pConList) {
				var thisCon = pConList[key];
				
				this.allConcepts[thisCon._id] = thisCon;
			}
		}
	};

	this.storeStores = function(pStoreList) {
		if (!ce.utils.isNullOrEmpty(pStoreList)) {
			for (var key in pStoreList) {
				var thisStore = pStoreList[key];

				this.allStores[thisStore._id] = thisStore;
			}
		}
	};
	
	this.showCeInPopupWindow = function(pTitle, pCeText) {
		var myWindow = window.open('', pTitle, 'width=800, height=1000');
		myWindow.document.write(pCeText);	};

	this.isDefaultCeStore = function() {
		return (this.currentCeStore === DEFAULT_CESTORE);
	};

	this.isThisCeStoreCurrent = function(pCeStoreName) {
		return (this.currentCeStore === pCeStoreName);
	};

	this.isCeStoreEmpty = function() {
		return gCe.utils.isNullOrEmpty(this.allConcepts);
	};

	this.isConceptLoaded = function(pConName) {
		return (this.allConcepts[pConName] != null);
	};

	this.clearLoggedInUser = function() {
		gCe.utils.clearLoggedInUser();

		this.dlg.user.clearUserName();
		gCe.msg.alert('You have been logged out');
	};

	this.stdHttpParms = function() {
		return {
			address: this.getDomainAndApp(),
			store: this.currentCeStore,
			showStats: this.showStats,
			accept: gCe.FORMAT_JSON
		};
	};

	this.sortById = function(a, b) {
		return a._id.localeCompare(b._id);

	};

	this.refreshGeneralPaneIfNeeded = function() {
		if (!gCe.utils.isNullOrEmpty(gEp.ui.pane.general.lastRequest)) {
			//Execute the function
			gEp.ui.pane.general.lastRequest();
		}
	};
	
	this.hasListItemActionsFor = function(pType) {
		return hasActionsFor(actionsList, pType, gEp.ui.MODE_LISTITEM);
	};

	this.hasListFooterActionsFor = function(pType) {
		return hasActionsFor(actionsList, pType, gEp.ui.MODE_LISTFOOTER);
	};

	this.hasDetailActionsFor = function(pType) {
		return hasActionsFor(actionsList, pType, gEp.ui.MODE_DETAILS);
	};

	function hasActionsFor(pActions, pType, pMode) {
		var result = false;
		
		if (pMode === null) {
			result = !gCe.utils.isNullOrEmpty(pActions[pType]);
		} else {
			var typeActions = pActions[pType];

			if (!gCe.utils.isNullOrEmpty(typeActions)) {
				for (var i = 0; i < typeActions.length; i++) {
					var thisAction = typeActions[i];
					
					for (var j = 0; j < thisAction.filters.length; j++) {
						var thisFilter = thisAction.filters[j];
						
						if (thisFilter === pMode) {
							result = true;
						}
					}
				}
			}
		}
		
		return result;
	}

	this.getListItemActionsListFor = function(pType, pInst) {
		return getActionsListFor(actionsList, pType, pInst, gEp.ui.MODE_LISTITEM);
	};
	
	this.getListFooterActionsListFor = function(pType) {
		return getActionsListFor(actionsList, pType, null, gEp.ui.MODE_LISTFOOTER);
	};

	this.getDetailActionsListFor = function(pType, pInst) {
		return getActionsListFor(actionsList, pType, pInst, gEp.ui.MODE_DETAILS);
	};

	function getActionsListFor(pActions, pType, pInst, pMode) {
		var result = [];
		var typeActions = pActions[pType];
		
		if (!gCe.utils.isNullOrEmpty(typeActions)) {
			for (var i = 0; i < typeActions.length; i++) {
				var thisAction = typeActions[i];
				var useThisAction = false;

				for (var j = 0; j < thisAction.filters.length; j++) {
					var thisFilter = thisAction.filters[j];

					if (thisFilter === pMode) {
						useThisAction = true;
					}
				}
				
				if (useThisAction) {
					var linkText = gEp.ui.links.createActionLinkUsing(thisAction, pInst);
					result.push(linkText);
				}
			}
		}
		
		return result;
	};

}