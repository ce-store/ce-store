/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.test = new PaneTest();

//This file is deliberately located outside of the main engineering panel structure
//to demonstrate how additional panes (and other items like sentence sets) can be
//added to the engineering panel dynamically in order to be rendered in the browser.
//The line above that locates a new instance of the PaneTest class in the ui.panes
//array of the engineering panel is all that is needed to achieve this.

function PaneTest() {
	var iDomParentName = 'menuContainer';
	var iDomName = 'testPane';
	var iTitle = 'Tests';
	var iTabPos = 0;

	this.initialise = function() {
		gCe.msg.debug(iDomName, 'initialise');

		gEp.ui.registerTab(iDomName, iDomParentName, iTabPos, iTitle, initialHtml());
	};

	this.resetContents = function() {
		gCe.msg.debug(iDomName, 'resetContents');

		this.updateWith(initialHtml());
	};

	function initialHtml() {
		var html = '';

		html += htmlListFor('Store', getStoreLinks);
		html += htmlListFor('Source', getSourceLinks);
		html += htmlListFor('Sentence', getSentenceLinks);
		html += htmlListFor('Model', getModelLinks);
		html += htmlListFor('Concept', getConceptLinks);
		html += htmlListFor('Property', getPropertyLinks);
		html += htmlListFor('Instance', getInstanceLinks);
		html += htmlListFor('Rule', getRuleLinks);
		html += htmlListFor('Query', getQueryLinks);
		html += htmlListFor('Special', getSpecialLinks);

		return html;
	}

	this.updateWith = function(pHtml, pShow, pTitle) {
		if (pShow) {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName, iDomParentName);
		} else {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName);
		}
	};

	function getStoreLinks() {
		var list = [];

		list.push([ 'List CE Stores', gEp.ui.links.JS_STORE_LIST ]);
		list.push([ 'Create store \'Dave\'', gEp.ui.links.JS_STORE_ADD, [ 'Dave' ] ] );
		list.push([ 'Get store details \'Dave\'', gEp.ui.links.JS_STORE_DETAILS, [ 'Dave' ] ] );
		list.push([ 'Delete store \'Dave\'', gEp.ui.links.JS_STORE_DELETE, [ 'Dave' ] ] );

		return list;
	}

	function getSourceLinks() {
		var list = [];

		list.push([ 'List sources', gEp.ui.links.JS_SOURCE_LIST ]);
		list.push([ 'Get source details \'src_010\'', gEp.ui.links.JS_SOURCE_DETAILS, [ 'src_010' ] ] );
		list.push([ 'Source sentences \'src_010\'', gEp.ui.links.JS_SOURCE_SENS, [ 'src_010' ] ] );
		list.push([ 'Delete source \'src_010\'', gEp.ui.links.JS_SOURCE_DELETE, [ 'src_010' ] ] );

		return list;
	}

	function getSentenceLinks() {
		var list = [];
		
		var ceFact = 'there is a person named \'Fred Smith\'.';
		var ceQuery = 'for which P1 and P2 is it true that ( the person P1 is closely related to the person P2).';
		var ceRule = 'if (the person P1 is closely related to the person P2) then (the person P2 is closely related to the person P1).';
		
		list.push([ 'Add sentences', gEp.ui.links.JS_SEN_ADD, [ 'Test', ceFact, 'Tester' ] ]);
		list.push([ 'Validate sentences', gEp.ui.links.JS_SEN_VAL, [ ceFact ] ]);
		list.push([ 'List all sentences', gEp.ui.links.JS_SEN_ALLLIST ]);
		list.push([ 'List model sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'model' ] ]);
		list.push([ 'List fact sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'fact' ] ]);
		list.push([ 'List fact (normal) sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'fact-normal' ] ]);
		list.push([ 'List fact (qualified) sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'fact-qualified' ] ]);
		list.push([ 'List pattern sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'pattern' ] ]);
		list.push([ 'List rule sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'rule' ] ]);
		list.push([ 'List query sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'query' ] ]);
		list.push([ 'List annotation sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'annotation' ] ]);
		list.push([ 'List command sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'command' ] ]);
		list.push([ 'List valid sentences', gEp.ui.links.JS_SEN_QUALLIST, [ null, 'valid' ] ]);
		list.push([ 'List invalid sentences', gEp.ui.links.JS_SEN_QUALLIST, [ null, 'invalid' ] ]);
		list.push([ 'List valid model sentences', gEp.ui.links.JS_SEN_QUALLIST, [ 'model', valid ] ]);
		list.push([ 'Get sentence details \'x\'', gEp.ui.links.JS_SEN_DETAILS, [ 'x' ] ] );
		list.push([ 'Delete sentence \'x\'', gEp.ui.links.JS_SEN_DELETE, [ 'x' ] ] );
		list.push([ 'Execute (rule) sentence', gEp.ui.links.JS_SEN_EXR, [ ceRule ] ]);
		list.push([ 'Execute (query) sentence', gEp.ui.links.JS_SEN_EXR, [ ceQuery ] ]);
		list.push([ 'Execute (rule as query) sentence', gEp.ui.links.JS_SEN_EXQR, [ ceRule ] ]);
		list.push([ '*Rationale for sentence \'x\'', 'tbc', [ 'x' ] ]);

		return list;
	}

	function getModelLinks() {
		var list = [];

		list.push([ 'List models', gEp.ui.links.JS_MOD_LIST ]);
		list.push([ 'Get model details \'Medicine\'', gEp.ui.links.JS_MOD_DETAILS, [ 'Medicine' ] ] );
		list.push([ '*Delete model \'Medicine\'', gEp.ui.links.JS_MOD_DELETE, [ 'Medicine' ] ] );
		list.push([ '*Model sources', gEp.ui.links.JS_MOD_LISTSRC ]);
		list.push([ '*Model sentences', gEp.ui.links.JS_MOD_LISTSEN ]);
		list.push([ '*Model concepts', gEp.ui.links.JS_MOD_LISTCON ]);

		return list;
	}

	function getConceptLinks() {
		var list = [];

		list.push([ 'List concepts', gEp.ui.links.JS_CON_LIST ]);
		list.push([ 'Get concept details \'person\'', gEp.ui.links.JS_CON_DETAILS, [ 'person' ] ] );
		list.push([ '*Delete concept \'person\'', gEp.ui.links.JS_CON_DELETE, [ 'person' ] ] );
		list.push([ '*Concept children (all)', 'gEp.handler.concepts.childrenForConcept' ]);
		list.push([ '*Concept children (direct)', 'gEp.handler.concepts.directChildrenForConcept' ]);
		list.push([ '*Concept parents (all)', 'gEp.handler.concepts.parentsForConcept' ]);
		list.push([ '*Concept parents (direct)', 'gEp.handler.concepts.directParentsForConcept' ]);
		list.push([ '*Concept properties (all)', 'gEp.handler.concepts.allPropertiesForConcept' ]);
		list.push([ '*Concept properties (dat)', 'gEp.handler.concepts.datatypePropertiesForConcept' ]);
		list.push([ '*Concept properties (obj)', 'gEp.handler.concepts.objectPropertiesForConcept' ]);
		list.push([ '*Concept properties (obj-wr)', 'gEp.handler.concepts.objectPropertiesWithRangeForConcept' ]);
		list.push([ 'Concept instance (all)', gEp.ui.links.JS_CON_INSTLIST, ['person'] ]);
		list.push([ 'Concept instance (exact)', gEp.ui.links.JS_CON_INSTLISTEXACT, ['person'] ]);
		list.push([ '*Count instances', gEp.ui.links.JS_CON_COUNTINSTS, ['person'] ]);
		list.push([ '*Concept primary sentences', 'gEp.handler.concepts.primarySentencesForConcept' ]);
		list.push([ '*Concept secondary sentences', 'gEp.handler.concepts.secondarySentencesForConcept' ]);
		list.push([ '*Concept all sentences', 'gEp.handler.concepts.allSentencesForConcept' ]);
		list.push([ '*Rationale for concept \'x\'', 'tbc', [ 'x' ] ]);

		return list;
	}

	function getPropertyLinks() {
		var list = [];

		list.push([ 'List properties (all)', 'gEp.handler.properties.listAllProperties' ]);
		list.push([ 'List properties (dat)', 'gEp.handler.properties.listDatatypeProperties' ]);
		list.push([ 'List properties (obj)', 'gEp.handler.properties.listObjectProperties' ]);
		list.push([ 'List properties (obj-wr)', 'gEp.handler.properties.listObjectPropertiesWithRange' ]);
		list.push([ 'Get property details \'x\'', 'gEp.handler.properties.getPropertyDetails', [ 'x' ] ] );
		list.push([ 'Delete property \'x\'', 'gEp.dlg.general.deleteProperty', [ 'x' ] ] );
		list.push([ 'Property primary sentences', 'gEp.handler.properties.primarySentencesForProperty' ]);
		list.push([ 'Property secondary sentences', 'gEp.handler.properties.secondarySentencesForProperty' ]);
		list.push([ 'Property all sentences', 'gEp.handler.properties.allSentencesForProperty' ]);
		list.push([ 'Rationale for property \'x\'', 'tbc', [ 'x' ] ]);
		list.push([ 'Rationale for property value \'x\', \'y\'', 'tbc', [ 'x', 'y' ] ]);
		list.push([ 'Common values for property \'x\'', 'tbc', [ 'x' ] ]);

		return list;
	}

	function getInstanceLinks() {
		var list = [];

		list.push([ 'List instances', 'gEp.handler.instances.listAllInstances' ]);
		list.push([ 'Delete all instances', 'gEp.handler.instances.deleteAllInstances' ]);
		list.push([ 'Get instance details \'Jean\'', 'gEp.handler.instances.getInstanceDetails', [ 'Jean' ] ] );
		list.push([ 'Delete instance \'Jean\'', 'gEp.dlg.general.deleteInstance', [ 'Jean' ] ] );
		list.push([ 'Instance references \'Jean\'', 'gEp.handler.instances.referencesToInstance', [ 'Jean' ] ]);
		list.push([ 'Instance primary sentences \'Jean\'', 'gEp.handler.instances.primarySentencesForInstance', [ 'Jean' ] ]);
		list.push([ 'Instance secondary sentences \'Jean\'', 'gEp.handler.instances.secondarySentencesForInstance', [ 'Jean' ] ]);
		list.push([ 'Instance all sentences \'Jean\'', 'gEp.handler.instances.allSentencesForInstance', [ 'Jean' ] ]);
		list.push([ 'Rationale for instance \'Jean\'', 'tbc', [ 'Jean' ] ]);

		return list;
	}

	function getQueryLinks() {
		var list = [];

		list.push([ 'List queries', 'gEp.handler.patterns.listAllQueries' ]);
		list.push([ 'Get query details \'x\'', 'gEp.handler.pattern.getPatternDetails', [ 'x' ] ] );
		list.push([ 'Execute query \'x\'', 'gEp.handler.pattern.executeQuery', [ 'x' ] ] );
		list.push([ 'Delete query \'x\'', 'gEp.dlg.general.deleteQuery', [ 'x' ] ] );

		return list;
	}

	function getRuleLinks() {
		var list = [];

		list.push([ 'List rules', 'gEp.handler.patterns.listAllRules' ]);
		list.push([ 'Get rule details \'x\'', 'gEp.handler.pattern.getPatternDetails', [ 'x' ] ] );
		list.push([ 'Execute rule \'x\'', 'gEp.handler.pattern.executeRule', [ 'x' ] ] );
		list.push([ 'Execute rule as query \'x\'', 'gEp.handler.pattern.executeRuleAsQuery', [ 'x' ] ] );
		list.push([ 'Delete rule \'x\'', 'gEp.dlg.general.deleteRule', [ 'x' ] ] );
		list.push([ 'Rationale for rule \'x\'', 'tbc', [ 'x' ] ]);

		return list;
	}

	function getSpecialLinks() {
		var list = [];

		list.push([ 'Show stats', 'tbc' ]);
		list.push([ 'Clean and show stats', 'tbc' ]);
		list.push([ 'List patterns', 'gEp.handler.patterns.listAllPatterns' ]);
		list.push([ 'List all config', 'tbc' ]);
		list.push([ 'Show config value', 'tbc' ]);
		list.push([ 'Update config value', 'tbc' ]);
		list.push([ 'Get next UID', 'getNextUid' ]);
		list.push([ 'Get UID batch', 'tbc' ]);
		list.push([ 'Reset UIDs', 'resetUids' ]);
		list.push([ 'Search', 'keywordSearch' ]);
		list.push([ 'Show all rationale', 'listAllRationale' ]);
		list.push([ 'List shadow concepts', 'listShadowConcepts' ]);
		list.push([ 'List shadow instances', 'listShadowInstances' ]);
		list.push([ 'List diverse concept instances', 'listDiverseConceptInstances' ]);
		list.push([ 'List multiple concept instances', 'gEp.handler.actions.listMultipleConceptInstances', [ 'entity concept,conceptual model' ] ]);

		return list;
	}

	function htmlListFor(pTypeName, pMethodName) {
		var html = '';
		var rawLinks = pMethodName();
		var linkList = [];

		for (var key in rawLinks) {
			var thisLink = rawLinks[key];

			var hrefText = thisLink[0];
			var jsMethod = thisLink[1];
			var jsParms = thisLink[2];

			if (jsParms == null) {
				jsParms = [];
			}

			var jsText = gEp.ui.links.jsTextFor(jsMethod, jsParms);
			var thisLink = gEp.ui.links.hyperlinkFor(jsText, hrefText);

			linkList.push(thisLink);
		}

		html += '<h3>' + pTypeName + ':</h3>';
		html += gEp.ui.htmlUnorderedListFor(linkList);

		return html;
	}

}