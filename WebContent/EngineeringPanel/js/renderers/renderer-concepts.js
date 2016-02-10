/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.concepts = new RendererConcepts();

function RendererConcepts() {
	var TYPENAME = 'concept';

	this.initialise = function() {
		gCe.msg.debug('RendererConcepts', 'initialise');
		//Nothing needed
	};

	this.renderConceptList = function(pConList) {
		var html = '';

		if (!gCe.utils.isNullOrEmpty(pConList)) {
			var list = calculateBasicConceptListFor(pConList);

			html += htmlHeaderFor(pConList);
			html += gEp.ui.htmlUnorderedListFor(list);
		} else {
			html += htmlHeaderFor(pConList);
			html += 'No concepts were returned';
		}

		gEp.ui.pane.concept.updateWith(html, false);
	};

	this.renderShadowConceptList = function(pConList) {
		var html = '';

		if (!gCe.utils.isNullOrEmpty(pConList)) {
			var list = [];

			for (var key in pConList) {
				var thisCon = pConList[key];

				list.push(gEp.ui.links.conceptDetails(thisCon._id));
			}
			
			html += 'The following ' + pConList.length + ' shadow concepts exist:';
			html += gEp.ui.htmlUnorderedListFor(list);
		} else {
			html += 'There are no shadow concepts';
		}

		gEp.ui.pane.general.updateWith(html, true);
	};

	this.renderConceptDetails = function(pCon) {
		if (!gCe.utils.isNullOrEmpty(pCon)) {
			//TODO: Complete this - annotations need to be added, plus others?
			var conLink = gEp.ui.links.conceptDetails(pCon._id);
			var html = 'Showing concept details for ' + conLink + ':';
			html += '<br/><br/>';
			var hdrs = [ 'name', 'models', 'parents', 'children', 'properties', 'instances', 'primary sentences', 'secondary sentences', 'all sentences' ];

			var instLink = null;
			
			if (pCon.instance_count === 0) {
				instLink = '0';
			} else {
				instLink = pCon.instance_count;
				instLink += '<br/>';
				instLink += 'list: ';
				instLink += gEp.ui.links.conceptInstanceList(pCon._id, 'standard');
				instLink += ' or ';
				instLink += gEp.ui.links.conceptInstanceListExact(pCon._id, 'exact');
			}
			
			var priSenLink = null;
			if (pCon.primary_sentence_count === 0) {
				priSenLink = 0;
			} else {
				priSenLink = gEp.ui.links.conceptPrimarySentences(pCon._id, pCon.primary_sentence_count, null, false);
			}

			var secSenLink = null;
			if (pCon.secondary_sentence_count === 0) {
				secSenLink = 0;
			} else {
				secSenLink = gEp.ui.links.conceptSecondarySentences(pCon._id, pCon.secondary_sentence_count, null, false);
			}
			
			var allSenCount = pCon.primary_sentence_count + pCon.secondary_sentence_count;
			var allSenLink = gEp.ui.links.conceptAllSentences(pCon._id, allSenCount, null, false);

			var rows = [
				conLink,
				htmlForConceptualModels(pCon),
				htmlForParents(pCon),
				htmlForChildren(pCon),
				htmlForProperties(pCon),
				instLink,
				priSenLink,
				secSenLink,
				allSenLink
			];

			insertActions(pCon, hdrs, rows);

			html += gEp.ui.htmlVerticalTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);
			gEp.ui.pane.entity.updateWith(html, true);
		} else {
			gCe.msg.error('Unable to get concept details');
		}
	};
	
	function insertActions(pCon, pHdrs, pRows) {
		if (gEp.hasDetailActionsFor(TYPENAME)) {
			var actList = gEp.getDetailActionsListFor(TYPENAME, pCon);
			pRows.push(actList);
			pHdrs.push('Actions');
		}
	}

	function htmlForConceptualModels(pCon) {
		var linkList = [];

		for (var key in pCon.conceptual_models) {
			var thisCm = pCon.conceptual_models[key];
			linkList.push(gEp.ui.links.modelDetails(thisCm._id));
		}

		return gEp.ui.htmlUnorderedListFor(linkList);
	}

	function htmlForParents(pCon) {
		var linkList = [];

		for (var key in pCon.all_parent_names) {
			var thisParName = pCon.all_parent_names[key];
			linkList.push(gEp.ui.links.conceptDetails(thisParName));
		}

		return gEp.ui.htmlUnorderedListFor(linkList);
	}

	function htmlForChildren(pCon) {
		var linkList = [];

		for (var key in pCon.all_child_names) {
			var thisChiName = pCon.all_child_names[key];
			linkList.push(gEp.ui.links.conceptDetails(thisChiName, thisChiName));
		}

		return gEp.ui.htmlUnorderedListFor(linkList);
	}

	function htmlForProperties(pCon) {
		var linkList = [];

		for (var key in pCon.direct_properties) {
			var thisProp = pCon.direct_properties[key];
			linkList.push(gEp.ui.links.propertyDetails(thisProp.property_name, thisProp.domain_name, thisProp.range_name));
		}
		for (var key in pCon.inherited_properties) {
			var thisProp = pCon.inherited_properties[key];
			linkList.push(gEp.ui.links.propertyDetails(thisProp.property_name, thisProp.domain_name, thisProp.range_name));
		}

		return gEp.ui.htmlUnorderedListFor(linkList);
	}

	function htmlHeaderFor(pConList, pQualifier) {
		var html = '';
		var hdrText = null;
		
		if (!gCe.utils.isNullOrEmpty(pConList)) {
			hdrText = gEp.ui.htmlEmphasise(textForHeader(), pConList.length);
		} else {
			hdrText = gEp.ui.htmlEmphasise(textForHeader(), 0);
		}

		html += 'Filters:';
		html += ' [' + hyperlinkZeroOrNonZero() + ']';
		html += ' [' + gEp.ui.links.conceptList('refresh') + ']';
		html += '<br/>';
		html += '<hr/>';
		html += gEp.ui.htmlPaneHeaderFor(hdrText);

		return html;
	}
	
	function textForHeader() {
		var nzText = null;

		if (gEp.handler.concepts.isNonZeroOnly()) {
			nzText = ' except those with no instances';
		} else {
			nzText = '';
		}

		return 'Showing all %01 concepts' + nzText + ':';
	}

	function hyperlinkZeroOrNonZero() {
		var result = null;

		if (gEp.handler.concepts.isNonZeroOnly()) {
			result = gEp.ui.links.conceptSpecialShowNonZero();
		} else {
			result = gEp.ui.links.conceptSpecialShowZero();
		}

		return result;
	}

	function calculateBasicConceptListFor(pConList) {
		var rows = [];

		for (var key in pConList) {
			rows.push(listTextFor(pConList[key]));
		}

		return rows;
	}

	function listTextFor(pCon) {
		var listText = '';

		listText += gEp.ui.links.conceptDetails(pCon._id);
		listText += ' (';
		listText += gEp.ui.links.conceptInstanceList(pCon._id, pCon.instance_count);
		listText += ')';

		return listText;
	}
}