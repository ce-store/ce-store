/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.models = new RendererModels();

function RendererModels() {
	var TYPENAME = 'model';

	this.initialise = function() {
		gCe.msg.debug('RendererModels', 'initialise');
		//Nothing needed
	};

	this.renderModelDetails = function(pModel) {
		if (!gCe.utils.isNullOrEmpty(pModel)) {
			var modLink = gEp.ui.links.modelDetails(pModel._id);
			var html = 'Showing model details for ' + modLink + '<br/><br/>';
			var hdrs = [ 'Model name', 'Created', '# Sens', 'Concepts', 'Sources' ];
			var hasActions = gEp.hasDetailActionsFor(TYPENAME);
			
			var rows = [
				modLink,
				gEp.ui.htmlCreationDate(pModel._created),
				hyperlinkSentenceCountFor(pModel),
				htmlListForConcepts(pModel.concepts),
				htmlListForSources(pModel.sources)
			];

			if (hasActions) {
				hdrs.push('Actions');
				rows.push(htmlDetailActionsFor(pModel));
			}
			
			html += gEp.ui.htmlVerticalTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);

			gEp.ui.pane.entity.updateWith(html, true);
		} else {
			gCe.msg.error('Unable to get model details');
		}
	};

	this.renderModelsList = function(pList) {
		if (!gCe.utils.isNullOrEmpty(pList)) {
			var html = '';

			html += 'The following ' + pList.length + ' models are defined:';
			html += '<br/><br/>';
			html += htmlTableFor(pList.sort(gEp.sortById));

			gEp.ui.pane.general.updateWith(html, true);
		} else {
			gCe.msg.error('Unable to get models list');
		}
	};

	function hyperlinkSentenceCountFor(pModel) {
		var result = null;

		if ((pModel.sentence_count != null) && (pModel.sentence_count > 0)) {
			result = gEp.ui.links.modelSentences(pModel._id, pModel.sentence_count, false);
		} else {
			result = '0';
		}

		return result;
	}

	function htmlTableFor(pList) {
		var hdrs = [ 'Model name', 'Created', '# Sens', 'Sources', 'Concepts' ];
		var hasActions = gEp.hasListItemActionsFor(TYPENAME);

		if (hasActions) {
			hdrs.push('Actions');
		}
		
		var rows = calculateRowsFor(pList, hasActions);

		return gEp.ui.htmlTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);
	}

	function calculateRowsFor(pList, pHasActions) {
		var rows = [];
		
		for (var key in pList) {
			rows.push(createRowFor(pList[key], pHasActions));
		}

		return rows;
	}

	function createRowFor(pModel, pShowActions) {
		var rowVals = [];

		rowVals.push(gEp.ui.links.modelDetails(pModel._id));
		rowVals.push(gEp.ui.formattedDateTimeStringFor(pModel._created));
		rowVals.push(hyperlinkSentenceCountFor(pModel));
		rowVals.push(htmlListForSourceIds(pModel.source_ids));
		rowVals.push(htmlListForConceptNames(pModel.concept_names));

		if (pShowActions) {
			rowVals.push(htmlListActionsFor(pModel));
		}

		return rowVals;
	}

	function htmlListActionsFor(pModel) {
		var actionsList = gEp.getListItemActionsListFor(TYPENAME, pModel);

		return gEp.ui.htmlUnorderedListFor(actionsList);
	}

	function htmlDetailActionsFor(pModel) {
		var actionsList = gEp.getDetailActionsListFor(TYPENAME, pModel);
		
		return gEp.ui.htmlUnorderedListFor(actionsList);
	}

	function htmlListForConcepts(pConList) {
		var conLinks = [];

		for (var thisKey in pConList) {
			var thisCon = pConList[thisKey];

			if (!gCe.utils.isNullOrEmpty(thisCon._id)) {
				conLinks.push(gEp.ui.links.conceptDetails(thisCon._id));
			}
		}

		return gEp.ui.htmlUnorderedListFor(conLinks);
	}

	function htmlListForConceptNames(pList) {
		var conLinks = [];

		for (var thisKey in pList) {
			var thisName = pList[thisKey];

			conLinks.push(gEp.ui.links.conceptDetails(thisName));
		}

		return gEp.ui.htmlUnorderedListFor(conLinks);
	}

	function htmlListForSources(pSrcList) {
		var srcLinks = [];

		for (var thisKey in pSrcList) {
			var thisSrc = pSrcList[thisKey];

			if (!gCe.utils.isNullOrEmpty(thisSrc._id)) {
				srcLinks.push(gEp.ui.links.sourceDetails(thisSrc._id));
			}
		}

		return gEp.ui.htmlUnorderedListFor(srcLinks);
	}

	function htmlListForSourceIds(pList) {
		var srcLinks = [];

		for (var thisKey in pList) {
			var thisId = pList[thisKey];

			srcLinks.push(gEp.ui.links.sourceDetails(thisId));
		}

		return gEp.ui.htmlUnorderedListFor(srcLinks);
	}

}