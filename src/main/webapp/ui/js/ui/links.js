/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/
gEp.ui.links = new CeStoreLinks();

function CeStoreLinks() {
	var JS_PATT_LIST = 'gEp.handler.patterns.listAllPatterns';
	var JS_RULE_DETAILS = 'gEp.handler.patterns.getRuleDetails';
	var JS_RULE_LOAD = 'gEp.dlg.ceqb.actions.loadRule';
	var JS_RULE_EXECUTE = 'gEp.handler.patterns.executeRuleByName';
	var JS_RULE_EXECUTEASQUERY = 'gEp.handler.patterns.executeRuleAsQueryByName';
	var JS_QUERY_DETAILS = 'gEp.handler.patterns.getQueryDetails';
	var JS_QUERY_LOAD = 'gEp.dlg.ceqb.actions.loadQuery';
	var JS_QUERY_EXECUTE = 'gEp.handler.patterns.executeQueryByName';
	var JS_SPEC_CON_SHOWALL = 'gEp.handler.concepts.showAllConcepts';
	var JS_SPEC_CON_SHOWZERO = 'gEp.handler.concepts.showZeroConcepts';
	var JS_SPEC_CON_SHOWNONZERO = 'gEp.handler.concepts.showNonZeroConcepts';
	var JS_USER_LOGIN = 'gEp.handler.actions.login';
	var JS_USER_LOGOUT = 'gEp.handler.actions.logout';
	var JS_USER_SAVELOGIN = 'gEp.handler.actions.saveLoggedInUser';
	var JS_USER_CANCELLOGIN = 'gEp.handler.actions.cancelLoginDialog';
	var JS_CONF_DEBUG = 'gEp.handler.actions.setDebug';
	var JS_CONF_JSD = 'gEp.dlg.config.changedJavascriptDebug';
	var JS_CONF_ARR = 'gEp.dlg.config.changedAutoRunRules';
	var JS_CONF_SS = 'gEp.dlg.config.changedShowStats';
	var JS_CONF_RC = 'gEp.dlg.config.changedReturnCe';
	var JS_CONF_EDIT = 'gEp.dlg.config.editConfigValue';
	var JS_GEN_SEARCH = 'gEp.handler.actions.keywordSearch';
	var JS_GEN_SHOWLIBS = 'gEp.handler.actions.showLibraryVersions';
	var JS_GEN_CLEARDROP = 'clearDropPane';
	var JS_GEN_STORECONF = 'gEp.handler.actions.showStoreConfiguration';

	//Server links
	var JS_SERVER_REFRESH = 'gEp.dlg.config.refreshServerAddressList';
	var JS_SERVER_ADD = 'gEp.dlg.config.createNewServerAddress';
	var JS_SERVER_CHANGED = 'gEp.dlg.config.changedServer';

	//Store links
	this.JS_STORE_LIST = 'gEp.handler.stores.listAllStores';
	this.JS_STORE_DETAILS = 'gEp.handler.stores.getStoreDetails';
	this.JS_STORE_ADD = 'gEp.dlg.config.createStore';
	this.JS_STORE_ADD_AND_SWITCH = 'gEp.dlg.config.createStoreAndSwitchTo';
	this.JS_STORE_DELETE = 'gEp.dlg.config.deleteStore';
	this.JS_STORE_SWITCHED = 'gEp.handler.stores.switchToStore';
	var JS_STORE_REFRESH = 'gEp.handler.stores.refreshStoreList';
	var JS_STORE_CHANGED = 'gEp.dlg.config.changedCeStore';

	//Source links
	this.JS_SOURCE_LIST = 'gEp.handler.sources.listAllSources';
	this.JS_SOURCE_DETAILS = 'gEp.handler.sources.getSourceDetails';
	this.JS_SOURCE_DELETE = 'gEp.handler.sources.deleteSource';
	this.JS_SOURCE_SENS = 'gEp.handler.sentences.listSentencesForSource';

	//Sentence links
	this.JS_SEN_ADD = 'gEp.handler.sentences.addSentences';
	this.JS_SEN_ADDFORM = 'gEp.handler.sentences.addSentencesFromForm';
	this.JS_SEN_EXQ = 'gEp.handler.patterns.executeQueryText';
	this.JS_SEN_EXR = 'gEp.handler.patterns.executeRuleText';
	this.JS_SEN_EXQR = 'gEp.handler.patterns.executeRuleText';
	this.JS_SEN_ALLLIST = 'gEp.handler.sentences.listAllSentences';
	this.JS_SEN_QUALLIST = 'gEp.handler.sentences.listAllSentencesQualifiedAs';
	this.JS_SEN_DETAILS = 'gEp.handler.sentences.getSentenceDetails';
	this.JS_SEN_DELETE = 'gEp.handler.sentences.deleteSentence';

	//Model links
	this.JS_MOD_LIST = 'gEp.handler.models.listAllModels';
	this.JS_MOD_DETAILS = 'gEp.handler.models.getModelDetails';
	this.JS_MOD_DELETE = 'gEp.dlg.general.deleteModel';
	this.JS_MOD_LISTSRC = 'gEp.handler.models.listSourcesForModel';
	this.JS_MOD_LISTSEN = 'gEp.handler.models.listSentencesForModel';
	this.JS_MOD_LISTCON = 'gEp.handler.models.listConceptsForModel';

	//Concept links
	this.JS_CON_LIST = 'gEp.handler.concepts.listAllConcepts';
	this.JS_CON_DETAILS = 'gEp.handler.concepts.getConceptDetails';
	this.JS_CON_DELETE = 'gEp.dlg.general.deleteConcept';
	this.JS_CON_COUNTINSTS = 'gEp.handler.concepts.countInstancesForConcept';
	this.JS_CON_INSTLIST = 'gEp.handler.instances.listInstancesForConcept';
	this.JS_CON_INSTLISTEXACT = 'gEp.handler.instances.listExactInstancesForConcept';
	this.JS_CON_PRISENS = 'gEp.handler.concepts.listPrimarySentencesForConcept';
	this.JS_CON_SECSENS = 'gEp.handler.concepts.listSecondarySentencesForConcept';
	this.JS_CON_ALLSENS = 'gEp.handler.concepts.listAllSentencesForConcept';

	//Property links
	this.JS_PROP_LIST = 'gEp.handler.properties.listAllProperties';
	this.JS_PROP_DETAILS = 'gEp.handler.properties.getPropertyDetails';
	
	//Instance links
	this.JS_INST_LIST = 'gEp.handler.instances.listAllInstances';
	var JS_INST_DETAILS = 'gEp.handler.instances.getInstanceDetails';
	this.JS_INST_PRISENS = 'gEp.handler.instances.listPrimarySentencesForInstance';
	this.JS_INST_SECSENS = 'gEp.handler.instances.listSecondarySentencesForInstance';
	this.JS_INST_ALLSENS = 'gEp.handler.instances.listAllSentencesForInstance';

	var TARGET_POPUP = '_blank';
	var TARGET_HUDSON = '_hudson';
	var DEFAULT_DEL = '[d]';

	this.initialise = function() {
		gCe.msg.debug('CeStoreLinks', 'initialise');
		//Nothing needed
	};

	this.hyperlinkFor = function(pHref, pLinkText, pHoverText, pTarget) {
		var hoverPart = '';
		var targetPart = '';

		if (pHoverText != null) {
			hoverPart = ' title="' + gCe.utils.encodeForHtml(pHoverText) + '"';
		}

		if (pTarget != null) {
			targetPart = ' target="' + gCe.utils.encodeForHtml(pTarget) + '"';
		}

		return '<a href="' + pHref + '" draggable="false"' + hoverPart + targetPart + '>' + pLinkText + '</a>';
	};

	this.draggableHyperlinkFor = function(pHref, pLinkText, pDragType, pDragId, pHoverText, pTarget) {
		var hoverPart = '';
		var targetPart = '';
		var dragPart = ' draggable="true" ondragstart="gEp.dlg.dnd.dragStart' + pDragType + '(event, \'' + pDragId + '\')"';

		if (pHoverText != null) {
			hoverPart = ' title="' + gCe.utils.encodeForHtml(pHoverText) + '"';
		}

		if (pTarget != null) {
			targetPart = ' target="' + gCe.utils.encodeForHtml(pTarget) + '"';
		}

		return '<a href="' + pHref + '"' + dragPart + hoverPart + targetPart + '>' + pLinkText + '</a>';
	};

	this.externalHyperlinkFor = function(pUrl) {
		return this.hyperlinkFor(pUrl, '<img src="./icons/link.png" height="15"/>', 'Open this link in a new window', '_blank');
	};

	//Stores
	this.jsTextForAddCeStore = function() {
		return this.jsTextFor(this.JS_STORE_ADD);
	};

	this.jsTextForAddAndSwitchCeStore = function() {
		return this.jsTextFor(this.JS_STORE_ADD_AND_SWITCH);
	};

	this.addCeStore = function() {
		return this.hyperlinkFor(this.jsTextForAddAndSwitchCeStore(), 'add', 'Create a new CE Store');
	};

	this.createCeStore = function() {
		return this.hyperlinkFor(this.jsTextForAddCeStore(), 'Create new CE Store', 'Create a new CE Store');
	};

	this.createAndSwitchToCeStore = function() {
		return this.hyperlinkFor(this.jsTextForAddAndSwitchCeStore(), 'Create (and switch to) new CE Store', 'Create a new CE Store and switch to it');
	};

	this.jsTextForDeleteCeStore = function(pStoreId) {
		return this.jsTextFor(this.JS_STORE_DELETE, [ pStoreId ]);
	};

	this.deleteCeStore = function() {
		return this.hyperlinkFor(this.jsTextForDeleteCeStore(), 'delete', 'Delete the selected CE Store');
	};

	this.deleteCeStoreNamed = function(pStoreId) {
		return this.hyperlinkFor(this.jsTextForDeleteCeStore(pStoreId), 'Delete CE Store \'' + pStoreId + '\'', 'Delete the CE Store named \'' + pStoreId + '\'');
	};

	this.jsTextForSwitchToCeStore = function(pStoreId) {
		return this.jsTextFor(this.JS_STORE_SWITCHED, [ pStoreId ]);
	};

	this.switchToCeStore = function (pStoreId) {
		return this.hyperlinkFor(this.jsTextForSwitchToCeStore(pStoreId), 'Switch to CE Store \'' + pStoreId + '\'', 'Switch to the CE Store named \'' + pStoreId + '\'');
	};

	this.storeDetails = function(pStoreId, pStoreName, pHover) {
		var jsText = null;
		var storeName = null;
		var hoverText = null;

		if (gCe.utils.isNullOrEmpty(pStoreName)) {
			storeName = pStoreId;
		} else {
			storeName = pStoreName;
		}

		if (!gCe.utils.isNullOrEmpty(pHover)) {
			hoverText = pHover;
		} else {
			hoverText = 'Show store details';
		}

		jsText = this.jsTextFor(this.JS_STORE_DETAILS, [ pStoreId ]);

		return this.draggableHyperlinkFor(jsText, storeName, 'Store', pStoreId, hoverText);
	};

	this.jsTextForListStores = function() {
		return this.jsTextFor(this.JS_STORE_LIST);
	};

	this.listStores = function(pLinkText) {
		var linkText = null;

		if (!gCe.utils.isNullOrEmpty(pLinkText)) {
			linkText = pLinkText;
		} else {
			linkText = 'List all stores';
		}

		return this.hyperlinkFor(this.jsTextForListStores(), linkText);
	};

	this.jsTextForRefreshStores = function() {
		return this.jsTextFor(JS_STORE_REFRESH);
	};

	this.refreshStoreList = function() {
		return this.hyperlinkFor(this.jsTextForRefreshStores(), 'refresh');
	};

	this.jsTextForChangedCeStore = function() {
		return this.jsTextFor(JS_STORE_CHANGED);
	};


	//Sources
	this.sourceList = function(pText) {
		var jsText = this.jsTextFor(this.JS_SOURCE_LIST);
		var hoverText = 'Show all sources';

		return this.hyperlinkFor(jsText, pText, hoverText);
	};

	this.sourceDetails = function(pSrcId) {
		var jsText = this.jsTextFor(this.JS_SOURCE_DETAILS, [ pSrcId ]);

		return this.hyperlinkFor(jsText, pSrcId, 'Show source details');
	};

	this.sourceDelete = function(pSrc, pLinkText) {
		var jsText = this.jsTextFor(this.JS_SOURCE_DELETE, [ pSrc._id ]);

		return this.hyperlinkFor(jsText, linkTextForDelete(pLinkText), 'Delete this source');
	};

	this.sourceSentences = function(pSrcId, pText, pFlag) {
		var jsText = this.jsTextFor(this.JS_SOURCE_SENS, [ pSrcId, pFlag ]);
		var hoverText = 'Show all sentences created from source ' + pSrcId;

		return this.hyperlinkFor(jsText, pText, hoverText);
	};

	//Sentences
	this.listAllSentences = function(pNumSens) {
		var linkText = null;
		var hoverText = null;
		var jsText = null;

		if (gCe.utils.isNullOrEmpty(pNumSens)) {
			linkText = 'List all sentences';
			hoverText = 'List all sentences';
		} else {
			linkText = pNumSens;
			hoverText = 'List all ' + pNumSens + ' sentences';
		}

		jsText = this.jsTextFor(this.JS_SEN_ALLLIST);

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.listQualifiedSentences = function(pTypeQual, pValQual, pFullFlag, pText) {
		var typeQual = null;
		
		if (pTypeQual !== 'all') {
			typeQual = pTypeQual;
		}
		
		var jsText = this.jsTextFor(this.JS_SEN_QUALLIST, [ typeQual, pValQual, pFullFlag ]);
		var linkText = null;

		if (gCe.utils.isNullOrEmpty(pText)) {
			var qualText = '';
			
			if (pTypeQual != null) {
				qualText = pTypeQual;
			}
			
			if (pValQual != null) {
				if (qualText != '') {
					qualText += ' ';
				}
				
				qualText += pValQual;
			}
			linkText = 'List ' + qualText + ' sentences';
		} else {
			linkText = pText;
		}

		return this.hyperlinkFor(jsText, linkText, 'List sentences qualified as \'' + qualText + '\'');
	};

	this.sentenceDetails = function(pSenId) {
		var jsText = this.jsTextFor(this.JS_SEN_DETAILS, [ pSenId ]);

		return this.hyperlinkFor(jsText, pSenId, 'Show sentence details');
	};

	this.sentenceDelete = function(pSen, pLinkText) {
		var jsText = this.jsTextFor(this.JS_SEN_DELETE, [ pSen._id ]);

		return this.hyperlinkFor(jsText, linkTextForDelete(pLinkText), 'Delete this sentence');
	};

	this.addSentences = function(pFormName, pCeText, pLabel) {
		var jsText = this.jsTextFor(this.JS_SEN_ADD, [pFormName, pCeText, pLabel]);

		return this.hyperlinkFor(jsText, 'Save CE sentence(s)');
	};

	this.addSentencesFromForm = function(pFormName) {
		var jsText = this.jsTextFor(this.JS_SEN_ADDFORM, [pFormName]);

		return this.hyperlinkFor(jsText, 'Save CE sentence(s)');
	};

	this.executeSentenceAsQuery = function(pFormName) {
		var jsText = this.jsTextFor(this.JS_SEN_EXQ, [pFormName]);

		return this.hyperlinkFor(jsText, 'Execute CE sentence as query');
	};

	this.executeSentenceAsRule = function(pFormName) {
		var jsText = this.jsTextFor(this.JS_SEN_EXQ, [pFormName]);

		return this.hyperlinkFor(jsText, 'Execute CE sentence as query');
	};


	//Models
	this.listAllModels = function(pNumMods) {
		var linkText = null;
		var hoverText = null;
		var jsText = null;
		
		if (gCe.utils.isNullOrEmpty(pNumMods)) {
			linkText = 'List all conceptual models';
			hoverText = 'List all conceptual models';
		} else {
			linkText = pNumMods;
			hoverText = 'List all ' + pNumMods + ' conceptual models';
		}
		
		jsText = this.jsTextFor(this.JS_MOD_LIST);

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.modelDetails = function(pCmName) {
		var jsText = this.jsTextFor(this.JS_MOD_DETAILS, [ pCmName ]);

		return this.hyperlinkFor(jsText, pCmName, 'Show conceptual model details');
	};

	this.modelSentences = function(pCmName, pText, pFlag) {
		var jsText = this.jsTextFor(this.JS_MOD_LISTSEN, [ pCmName, pFlag ]);
		var hoverText = 'Show all sentences for model ' + pCmName;

		return this.hyperlinkFor(jsText, pText, hoverText);
	};

	//Concepts
	this.conceptList = function(pText, pNumCons) {
		var jsText = this.jsTextFor(this.JS_CON_LIST);
		var linkText = null;
		var hoverText = null;
		
		if (!gCe.utils.isNullOrEmpty(pText)) {
			linkText = pText;
			hoverText = 'Show all concepts';
		} else {
			linkText = pNumCons;
			hoverText = 'Show all ' + pNumCons+ ' concepts'; 
		}

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.conceptDetails = function(pConName) {
		var jsText = this.jsTextFor(this.JS_CON_DETAILS, [ pConName ] );
		var hoverText = 'Show concept details for \'' + pConName + '\'';

		return this.draggableHyperlinkFor(jsText, pConName, 'Concept', pConName, hoverText);
	};

	this.conceptInstanceList = function(pConName, pLabel) {
		var jsText = this.jsTextFor(this.JS_CON_INSTLIST, [ pConName ]);
		var hoverText = 'Show all instances for the concept \'' + pConName + '\'';

		return this.hyperlinkFor(jsText, pLabel, hoverText);
	};

	this.conceptInstanceListExact = function(pConName, pLabel) {
		var jsText = this.jsTextFor(this.JS_CON_INSTLISTEXACT, [ pConName ]);
		var hoverText = 'Show all exact instances for the concept \'' + pConName + '\' (instances which are defined eaxctly as that concept and no other)';

		return this.hyperlinkFor(jsText, pLabel, hoverText);
	};

	this.conceptPrimarySentences = function(pConName, pCount, pLinkText, pFullDetails) {
		var linkText = null;
		if (!gCe.utils.isNullOrEmpty(pLinkText)) {
			linkText = pLinkText;
		} else {
			linkText = pCount;
		}

		var jsText = this.jsTextFor(this.JS_CON_PRISENS, [ pConName, pFullDetails ]);
		var hoverText = 'Show all primary sentences for the concept \'' + pConName + '\'';

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.conceptSecondarySentences = function(pConName, pCount, pLinkText, pFullDetails) {
		var linkText = null;
		if (!gCe.utils.isNullOrEmpty(pLinkText)) {
			linkText = pLinkText;
		} else {
			linkText = pCount;
		}

		var jsText = this.jsTextFor(this.JS_CON_SECSENS, [ pConName, pFullDetails ]);
		var hoverText = 'Show all secondary sentences for the concept \'' + pConName + '\'';

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.conceptAllSentences = function(pConName, pCount, pLinkText, pFullDetails) {
		var linkText = null;
		if (!gCe.utils.isNullOrEmpty(pLinkText)) {
			linkText = pLinkText;
		} else {
			linkText = pCount;
		}

		var jsText = this.jsTextFor(this.JS_CON_ALLSENS, [ pConName, pFullDetails ]);
		var hoverText = 'Show all sentences for the concept \'' + pConName + '\'';

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.conceptSpecialShowZero = function() {
		var jsText = this.jsTextFor(JS_SPEC_CON_SHOWZERO);

		return this.hyperlinkFor(jsText, 'nz', 'The list is showing all concepts including those with zero instances - click to filter out the zero instance concepts');
	};

	this.conceptSpecialShowNonZero = function() {
		var jsText = this.jsTextFor(JS_SPEC_CON_SHOWNONZERO);

		return this.hyperlinkFor(jsText, 'z', 'The list is showing only concepts with non-zero instance counts - click to remove this filter');
	};

	
	//Properties
	this.propertyDetails = function(pPropName, pPropDomain, pPropRange) {
		var jsText = this.jsTextFor(this.JS_PROP_DETAILS, [ pPropName, pPropDomain, pPropRange ]);
		var hoverText = 'Show property details for \'' + pPropDomain + ':' + pPropName + ':' + pPropRange + '\'';

		return this.draggableHyperlinkFor(jsText, pPropName, 'Property', pPropName, hoverText);
	};

	this.propertyDetailsFromFullName = function(pFullName) {
		var propParts = pFullName.split(':');

		return this.propertyDetails(propParts[1], propParts[0], propParts[2]);
	};

	
	//Instances
	this.listAllInstances = function(pNumInsts) {
		var linkText = null;
		var hoverText = null;
		var jsText = null;
		
		if (gCe.utils.isNullOrEmpty(pNumInsts)) {
			linkText = 'List all instances';
			hoverText = 'List all instances';
		} else {
			linkText = pNumInsts;
			hoverText = 'List all ' + pNumInsts + ' instances';
		}
		
		jsText = this.jsTextFor(this.JS_INST_LIST);

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.instancePrimarySentences = function(pInstName, pCount, pLinkText, pFullDetails) {
		var linkText = null;
		if (!gCe.utils.isNullOrEmpty(pLinkText)) {
			linkText = pLinkText;
		} else {
			linkText = pCount;
		}

		var jsText = this.jsTextFor(this.JS_INST_PRISENS, [ pInstName, pFullDetails ]);
		var hoverText = 'Show all primary sentences for the instance \'' + pInstName + '\'';

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.instanceSecondarySentences = function(pInstName, pCount, pLinkText, pFullDetails) {
		var linkText = null;
		if (!gCe.utils.isNullOrEmpty(pLinkText)) {
			linkText = pLinkText;
		} else {
			linkText = pCount;
		}

		var jsText = this.jsTextFor(this.JS_INST_SECSENS, [ pInstName, pFullDetails ]);
		var hoverText = 'Show all secondary sentences for the instance \'' + pInstName + '\'';

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.instanceAllSentences = function(pInstName, pCount, pLinkText, pFullDetails) {
		var linkText = null;
		if (!gCe.utils.isNullOrEmpty(pLinkText)) {
			linkText = pLinkText;
		} else {
			linkText = pCount;
		}

		var jsText = this.jsTextFor(this.JS_INST_ALLSENS, [ pInstName, pFullDetails ]);
		var hoverText = 'Show all sentences for the instance \'' + pInstName + '\'';

		return this.hyperlinkFor(jsText, linkText, hoverText);
	};


	
	this.plainUrl = function(pFullUrl, pShortenFlag) {
		var result = null;

		if (!gCe.utils.isNullOrEmpty(pFullUrl)) {
			var hoverText = pFullUrl + ' (click to open in new window)';
			var urlParts = pFullUrl.split('/');
			var shortUrl = urlParts[urlParts.length - 1];  //Get the last part of the url (after the last /)

			result = this.hyperlinkFor(pFullUrl, shortUrl, hoverText, TARGET_POPUP);
		} else {
			result = '';
		}
		
		return result;
	};

	this.instanceDetails = function(pInstId, pInstName, pHover) {
		var jsText = null;
		var instName = null;
		var hoverText = null;

		if (gCe.utils.isNullOrEmpty(pInstName)) {
			instName = pInstId;
		} else {
			instName = pInstName;
		}

		if (!gCe.utils.isNullOrEmpty(pHover)) {
			hoverText = pHover;
		} else {
			if (pInstId != pInstName) {
				hoverText = 'Show instance details for ' + pInstId;
			} else {
				hoverText = 'Show instance details';
			}
		}

		jsText = this.jsTextFor(JS_INST_DETAILS, [ pInstId ]);

		return this.draggableHyperlinkFor(jsText, instName, 'Instance', pInstId, hoverText);
	};

	this.patternList = function() {
		var jsText = gEp.ui.links.jsTextFor(JS_PATT_LIST);

		return this.hyperlinkFor(jsText, 'Refresh saved query/rule list');
	};

	this.queryDetails = function(pQueryName) {
		var jsText = this.jsTextFor(JS_QUERY_DETAILS, [ pQueryName ]);
		var hoverText = 'Show query details for \'' + pQueryName + '\'';

		return this.draggableHyperlinkFor(jsText, pQueryName, 'Query', pQueryName, hoverText);
	};

	this.queryLoad = function(pQueryName) {
		var jsText = this.jsTextFor(JS_QUERY_LOAD, [ pQueryName ]);

		return this.hyperlinkFor(jsText, 'load', 'Load this query in the CE Query Builder pane');
	};

	this.queryExecute = function(pQueryName, pLinkText) {
		var jsText = this.jsTextFor(JS_QUERY_EXECUTE, [ pQueryName ]);

		return this.hyperlinkFor(jsText, pLinkText);
	};

	this.ruleDetails = function(pRuleName) {
		var jsText = this.jsTextFor(JS_RULE_DETAILS, [ pRuleName ]);
		var hoverText = 'Show rule details for \'' + pRuleName + '\'';

		return this.draggableHyperlinkFor(jsText, pRuleName, 'Rule', pRuleName, hoverText);
	};

	this.editConfigValue = function(pKey, pValue) {
		var jsText = gEp.ui.links.jsTextForEditConfigValue(pKey, gCe.utils.encodeForJs(pValue));
		var hoverText = 'Edit config value for \'' + pKey + '\'';

		return this.hyperlinkFor(jsText, 'edit', hoverText);
	};

	this.ruleLoad = function(pRuleName) {
		var jsText = this.jsTextFor(JS_RULE_LOAD, [ pRuleName ]);

		return this.hyperlinkFor(jsText, 'load', 'Load this rule in the CE Query Builder pane');
	};

	this.ruleExecute = function(pRuleName, pLinkText) {
		var jsText = this.jsTextFor(JS_RULE_EXECUTE, [ pRuleName ]);

		return this.hyperlinkFor(jsText, pLinkText);
	};

	this.ruleExecuteAsQuery = function(pRuleName, pLinkText) {
		var jsText = this.jsTextFor(JS_RULE_EXECUTEASQUERY, [ pRuleName ]);

		return this.hyperlinkFor(jsText, pLinkText);
	};

	this.jsTextForRefreshServerList = function() {
		return this.jsTextFor(JS_SERVER_REFRESH);
	};

	this.refreshServerList = function() {
		return this.hyperlinkFor(this.jsTextForRefreshServerList(), 'refresh');
	};

	this.jsTextForAddServer = function() {
		return this.jsTextFor(JS_SERVER_ADD);
	};

	this.addServer = function() {
		return this.hyperlinkFor(this.jsTextForAddServer(), 'add', 'Create a new CE Server entry');
	};

	this.jsTextForChangedServer = function() {
		return this.jsTextFor(JS_SERVER_CHANGED);
	};


	this.login = function() {
		var jsText = this.jsTextFor(JS_USER_LOGIN);

		return this.hyperlinkFor(jsText, 'login');
	};

	this.logout = function() {
		var jsLogoutText = this.jsTextFor(JS_USER_LOGOUT);
	
		return this.hyperlinkFor(jsLogoutText, 'logout');
	};

	this.jsTextForSaveLoginForm = function() {
		return this.jsTextFor(JS_USER_SAVELOGIN);
	};

	this.jsTextForCancelLoginForm = function() {
		return this.jsTextFor(JS_USER_CANCELLOGIN);
	};

	this.search = function() {
		return this.hyperlinkFor(this.jsTextFor(JS_GEN_SEARCH), 'Search');
	};

	this.jsTextFor = function(pMethodName, pParms) {
		var result = '';

		result += 'javascript:' + pMethodName + '(';

		if (pParms != null) {
			var sepText = '';

			for (var key in pParms) {
				var thisParm = pParms[key];

				if (gCe.utils.isString(thisParm)) {
					thisParm = '\'' + gCe.utils.encodeForJs(thisParm) + '\'';
				}

				result += sepText + thisParm;
				sepText = ', ';
			}
		}

		result += ');';

		return result;
	};

	this.setDebug = function(pFlagVal, pLinkText) {
		var jsMethodText = jsMethodForFlagSwitch(JS_CONF_DEBUG, pFlagVal);
		var hoverText = 'Click here to switch debug mode ' + pLinkText;

		return this.hyperlinkFor(jsMethodText, pLinkText, hoverText);
	};

	this.showVersions = function(pLinkText) {
		var jsMethodText = this.jsTextFor(JS_GEN_SHOWLIBS);
		var hoverText = 'Click to see details of the third party library versions';

		return this.hyperlinkFor(jsMethodText, pLinkText, hoverText);
	};

	this.refreshPage = function() {
		var jsMethodText = this.jsTextFor('location.reload', [true]);
		var hoverText = 'Click to see details of the third party library versions';

		return this.hyperlinkFor(jsMethodText, 'refresh the page', hoverText);
	};

	this.help = function() {
		return this.hyperlinkFor('../doc/index.html', 'help', 'Open the help page', TARGET_POPUP);
	};

	this.hudson = function() {
		return this.hyperlinkFor('../hudson/', 'Hudson', 'Open Hudson', TARGET_HUDSON);
	};

	this.clearDropPane = function() {
		return this.hyperlinkFor(this.jsTextFor(JS_GEN_CLEARDROP), 'clear');
	};

	this.jsTextForChangedJsDebug = function() {
		return this.jsTextFor(JS_CONF_JSD);
	};

	this.jsTextForChangedAutoRunRules = function() {
		return this.jsTextFor(JS_CONF_ARR);
	};

	this.jsTextForChangedShowStats = function() {
		return this.jsTextFor(JS_CONF_SS);
	};

	this.jsTextForChangedReturnCe = function() {
		return this.jsTextFor(JS_CONF_RC);
	};

	this.jsTextForEditConfigValue = function(pKey, pVal) {
		return this.jsTextFor(JS_CONF_EDIT, [ pKey, pVal ] );
	};

	this.showStoreConfiguration = function() {
		var jsText = this.jsTextFor(JS_GEN_STORECONF);
		
		return this.hyperlinkFor(jsText, 'Show store configuration');
	};

	this.createActionLinkUsing = function(pAction, pItem) {
		var processedParms = [];
		
		//Iterate through all the parms and replace them with the live values from the instance
		for (var i = 0; i < pAction.jsParms.length; i++) {
			var thisRawParm = pAction.jsParms[i];

			if (gCe.utils.startsWith(thisRawParm, '{item}.')) {
				if (pItem !== null) {
					var propName = gCe.utils.replaceAll(thisRawParm, '{item}.', '');
					var parmVal = pItem[propName];
					
					if (gCe.utils.isArray(parmVal)) {
						parmVal = '' + parmVal;
					}

					processedParms.push(parmVal);
				} else {
					gCe.msg.debug('createActionLinkUsing: cannot instantiate parameters as pTem is null');
				}
			} else {
				//This is a plain parameter so just add it
				processedParms.push(thisRawParm);
			}
		}
		
		var jsText = this.jsTextFor(pAction.jsMethodName, processedParms);
		var lt = null;
		var ht = null;

		//TODO: Improve this to be more generic (rather than just handling the _id property)
		if (pItem !== null) {
			lt = gCe.utils.replaceAll(pAction.linkText, '{item}._id', pItem._id);
			ht = gCe.utils.replaceAll(pAction.hoverText, '{item}._id', pItem._id);
		} else {
			lt = pAction.linkText;
			ht = pAction.hoverText;
		}
		
		return this.hyperlinkFor(jsText, lt, ht);
	};

	function linkTextForDelete(pLinkText) {
		var linkText = null;

		if (pLinkText == null) {
			linkText = DEFAULT_DEL;
		} else {
			linkText = pLinkText;
		}

		return linkText;
	}

	function jsMethodForFlagSwitch(pJsMethodName, pFlagVal, pConceptName) {
		var result = null;

		if (gCe.utils.isNullOrEmpty(pConceptName)) {
			result = gEp.ui.links.jsTextFor(pJsMethodName, [ pFlagVal ]);
		} else {
			result = gEp.ui.links.jsTextFor(pJsMethodName, [ pFlagVal, pConceptName ]);
		}

		return result;
	}

}