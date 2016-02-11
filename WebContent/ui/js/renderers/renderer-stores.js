/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.stores = new RendererStores();

function RendererStores() {
	var TYPENAME = 'store';

	this.initialise = function() {
		gCe.msg.debug('RendererStores', 'initialise');
		//Nothing needed
	};

	this.renderStoresList = function(pList, pUserParms) {
		if (!gCe.utils.isNullOrEmpty(pList)) {
			var showTab = true;
			
			if (pUserParms !== undefined) {
				if (pUserParms.show_tab !== undefined) {
					showTab = pUserParms.showTab;
				}
			}

			var html = '';

			html += 'The following ' + pList.length + ' CE Stores are in use';
			html += ' (' + gEp.ui.links.listStores('refresh') + ')';
			html += '<br/><br/>';
			html += htmlTableFor(pList.sort(gEp.sortById));
			html += '<br/><br/>';
			html += 'Note: The current CE Store row is highlighted';
			
			if (gEp.hasListFooterActionsFor(TYPENAME)) {
				html += '<br/><br/>';
				html += htmlForListFooterActions();
			}

			gEp.ui.pane.general.updateWith(html, showTab);
		} else {
			gCe.msg.error('Unable to get stores list');
		}
	};

	this.renderSearchResult = function(pHdrs, pRows, pTerms) {
		if (!gCe.utils.isNullOrEmpty(pRows)) {
			var html = '';

			html += 'There are ' + pRows.length + ' matches for the search term \'' + pTerms + '\':';
			html += '<br/><br/>';

			html += gEp.ui.htmlTableFor(pHdrs, pRows, gEp.ui.DEFAULT_STYLE, null);
			
			gEp.ui.pane.general.updateWith(html, true);
		} else {
			gCe.msg.error('No occurrences of the term \'' + pTerms + '\' were found.');
		}
	};

	this.renderStoreDetails = function(pStore) {
		if (!gCe.utils.isNullOrEmpty(pStore)) {
			var storeLink = gEp.ui.links.storeDetails(pStore._id);
			var html = 'Showing store details for ' + storeLink + ':';
			html += '<br/><br/>';
			var hdrs = [ 'Store name', 'Created', '# sentences', '# models', '# concepts', '# instances' ];

			var hasActions = gEp.hasDetailActionsFor(TYPENAME);

			if (hasActions) {
				hdrs.push('Actions');
			}
			
			html += gEp.ui.htmlVerticalTableFor(hdrs, createRowFor(pStore, hasActions), gEp.ui.DEFAULT_STYLE);
			gEp.ui.pane.entity.updateWith(html, true);
		} else {
			gCe.msg.error('Unable to get store details');
		}
	};

	function htmlTableFor(pList) {
		var hasActions = gEp.hasListItemActionsFor(TYPENAME);
		var hdrs = [ 'Store name', 'Created', '# sentences', '# models', '# concepts', '# instances' ];
		var rows = calculateRowsFor(pList, hasActions);
		var classes = calculateClassesFor(pList);
		
		if (hasActions) {
			hdrs.push('Actions');
		}
		
		return gEp.ui.htmlTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE, classes);
	}

	function calculateRowsFor(pList, pHasActions) {
		var rows = [];
		
		for (var key in pList) {
			rows.push(createRowFor(pList[key], pHasActions));
		}

		return rows;
	}

	function createRowFor(pStore, pHasActions) {
		var rowVals = [];
		var isCurrentStore = (pStore._id === gEp.currentCeStore);
		var senLink = null;
		var modLink = null;
		var conLink = null;
		var instLink = null;

		if (isCurrentStore) {
			if (pStore.sentence_count > 0) {
				senLink = gEp.ui.links.listAllSentences(pStore.sentence_count);
			} else {
				senLink = '0';
			}
			
			if (pStore.model_count > 0) {
				modLink = gEp.ui.links.listAllModels(pStore.model_count);
			} else {
				modLink = '0';
			}

			if (pStore.concept_count > 0) {
				conLink = gEp.ui.links.conceptList(null, pStore.concept_count);
			} else {
				conLink = '0';
			}
			
			if (pStore.instance_count > 0) {
				instLink = gEp.ui.links.listAllInstances(pStore.instance_count);
			} else {
				instLink = '0';
			}
		} else {
			//If it's not the current CE Store then only numbers (not links) should be shown
			senLink = pStore.sentence_count;
			modLink = pStore.model_count;
			conLink = pStore.concept_count;
			instLink = pStore.instance_count;
		}

		rowVals.push(gEp.ui.links.storeDetails(pStore._id));
		rowVals.push(gEp.ui.formattedDateTimeStringFor(pStore._created));
		rowVals.push(senLink);
		rowVals.push(modLink);
		rowVals.push(conLink);
		rowVals.push(instLink);
		
		if (pHasActions) {
			rowVals.push(htmlListActionsFor(pStore));
		}

		return rowVals;
	}

	function calculateClassesFor(pList) {
		var rows = [];

		for (var key in pList) {
			if (gEp.isThisCeStoreCurrent(pList[key]._id)) {
				rows.push('selected');
			} else {
				rows.push(null);
			}
		}

		return rows;
	}

	function htmlListActionsFor(pStore) {
		var actionList = gEp.getDetailActionsListFor(TYPENAME, pStore);

		return gEp.ui.htmlUnorderedListFor(actionList);
	}

	function htmlForListFooterActions() {
		var html = '';

		html += 'List actions:';
		html += gEp.ui.htmlUnorderedListFor(gEp.getListFooterActionsListFor(TYPENAME));

		return html;
	}

}
