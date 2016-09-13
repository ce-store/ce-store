/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.sources = new RendererSources();

function RendererSources() {
	var TYPENAME = 'source';

	var SRCTYPE_URL = 'Url';
	var SRCTYPE_CMD = 'Command';
	var SRCTYPE_AGENT = 'Agent';
	var SRCTYPE_RULE = 'Rule';
	var SRCTYPE_QUERY = 'Query';

	this.initialise = function() {
		gCe.msg.debug('RendererSources', 'initialise');
		//Nothing needed
	};

	this.renderSourceList = function(pSrcList, pUserParms) {
		var html = '';

		if (!gCe.utils.isNullOrEmpty(pSrcList)) {
			var hdrs = [ 'Id', 'Type', '# Sens', 'Name', 'User', 'Creation', 'Delta (ms)' ];
			var hasActions = gEp.hasListItemActionsFor(TYPENAME);
			
			if (hasActions) {
				hdrs.push('Actions');
			}
			var rows = calculateRowsFor(pSrcList, hasActions);

			html += htmlPaneHeader(pSrcList.length);
			html += gEp.ui.htmlTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);
		} else {
			html += gEp.ui.links.sourceList('List all sources');
		}

		gEp.ui.pane.source.updateWith(html, (pUserParms.showPane === true));
	};

	this.renderSourceDetails = function(pSrc) {
		if (!gCe.utils.isNullOrEmpty(pSrc)) {
			var srcLink = gEp.ui.links.sourceDetails(pSrc._id);
			var parSrcLink = null;
			var childSrcLinks = null;
			var html = 'Showing source details for ' + srcLink + '<br/><br/>';
			var hdrs = [ 'Id', 'Created', 'Type', 'Detail', 'User', 'Parent', 'Children', '# Sens', '# Models', 'Annotations' ];

			if (pSrc.parent_id != null) {
				parSrcLink = gEp.ui.links.sourceDetails(pSrc.parent_id);
			} else {
				parSrcLink = '';
			}
			
			if (pSrc.child_ids != null) {
				childSrcLinks = '<ul>';
				for (var idx in pSrc.child_ids) {
					var child_id = pSrc.child_ids[idx];
					childSrcLinks += '<li>' + gEp.ui.links.sourceDetails(child_id) + '</li>';
				}
				childSrcLinks += '</ul>';
			} else {
				childSrcLinks = '';
			}

			var rows = [
				srcLink,
				gEp.ui.htmlCreationDate(pSrc._created),
				pSrc.source_type,
				formattedSourceDetailFor(pSrc),
				formattedUserIdFor(pSrc),
				parSrcLink,
				childSrcLinks,
				hyperlinkSentenceCountFor(pSrc),
				pSrc.model_count,
				htmlListForAnnotations(pSrc.annotations)
			];

			var hasActions = gEp.hasDetailActionsFor(TYPENAME);
			
			if (hasActions) {
				hdrs.push('Actions');
				rows.push(htmlDetailActionsFor(pSrc));
			}

			html += gEp.ui.htmlVerticalTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);

			gEp.ui.pane.entity.updateWith(html, true);
		} else {
			gCe.msg.error('Unable to get source details');
		}
	};

	function htmlDetailActionsFor(pSrc) {
		var actionsList = gEp.getDetailActionsListFor(TYPENAME, pSrc);

		return gEp.ui.htmlUnorderedListFor(actionsList);
	}

	function htmlPaneHeader(pLen) {
		var refreshLink = gEp.ui.links.sourceList('(refresh)');
		var hdrText = gEp.ui.htmlEmphasise('Showing %01 sources ' + refreshLink, pLen);

		return gEp.ui.htmlPaneHeaderFor(hdrText);
	};

	function htmlListForAnnotations(pRawAnnos) {
		var annoLinks = [];

		for ( var thisKey in pRawAnnos) {
			var thisVal = pRawAnnos[thisKey];

			if (thisKey === 'Model') {
				thisVal = gEp.ui.links.modelDetails(thisVal);
			}
			annoLinks.push(thisKey + ': ' + thisVal);
		}

		return gEp.ui.htmlUnorderedListFor(annoLinks);
	}

	function calculateRowsFor(pSrcList, pHasActions) {
		var rows = [];
		var lastSrc = null;

		for (var key in pSrcList) {
			var thisSrc = pSrcList[key];

			rows.push(createRowFor(thisSrc, lastSrc, pHasActions));
			lastSrc = thisSrc;
		}

		return rows;
	}

	function createRowFor(pSrc, pLastSrc, pHasActions) {
		var row = [];

		row.push(gEp.ui.links.sourceDetails(pSrc._id));
		row.push(pSrc.source_type);
		row.push(hyperlinkSentenceCountFor(pSrc));
		row.push(formattedSourceDetailFor(pSrc));
		row.push(formattedUserIdFor(pSrc));
		row.push(gEp.ui.htmlCreationDate(pSrc._created));
		row.push(formattedDeltaTimeFor(pSrc, pLastSrc));
		
		if (pHasActions) {
			var actList = gEp.getListItemActionsListFor(TYPENAME, pSrc);
			
			if (actList.length === 1) {
				actList = actList[0];
			}
			
			row.push(actList);
		}

		return row;
	}

	function hyperlinkSentenceCountFor(pSrc) {
		var result = null;

		if ((pSrc.sentence_count != null) && (pSrc.sentence_count > 0)) {
			result = gEp.ui.links.sourceSentences(pSrc._id, pSrc.sentence_count, false);
		} else {
			result = '0';
		}

		return result;
	}

	function formattedSourceDetailFor(pSrc) {
		var result = '';

		if ((pSrc.source_type === SRCTYPE_URL) || (pSrc.source_type === SRCTYPE_CMD)) {
			result = gEp.ui.links.plainUrl(pSrc.detail, true);
		} else if (pSrc.source_type === SRCTYPE_AGENT) {
			if (pSrc.agent_instname === 'autoRunRules') {
				result = pSrc.detail;
			} else {
				result = formatAgentDetail(pSrc.detail);
				result += '<br/>';
				result += 'AgentId: ' + gEp.ui.links.instanceDetails(pSrc.agent_instname);
			}
		} else if (pSrc.source_type === SRCTYPE_RULE) {
			result = 'Rule: ' + gEp.ui.links.hyperlinkRuleOrQueryDetails(pSrc.detail, pSrc.detail);
		} else if (pSrc.source_type === SRCTYPE_QUERY) {
			result = 'Query: ' + gEp.ui.links.hyperlinkRuleOrQueryDetails(pSrc.detail, pSrc.detail);
		} else {
			result = pSrc.detail;
		}

		return result;
	}

	function formatAgentDetail(pAgentDetail) {
		var wordArray = pAgentDetail.split(' ');
		var agentName = wordArray[0];
		var className = wordArray[1];
		var version = wordArray[2];
		var hoverText = null;
		
		if (className != null) {
			hoverText = className;
			
			if (version != null) {
				hoverText += ' ' + version;
			}
		} else {
			hoverText = '(unknown agent)';
		}

		return agentName + ' <a title="' + hoverText + '">(...)</a>';
	}

	function formattedDeltaTimeFor(pSrc, pLastSrc) {
		var deltaTime = '';

		if (!gCe.utils.isNullOrEmpty(pLastSrc)) {
			deltaTime = (parseInt(pSrc._created) - parseInt(pLastSrc._created)).toString();
		}

		return deltaTime;
	}

	function formattedUserIdFor(pSrc) {
		var result = '';

		if (!gCe.utils.isNullOrEmpty(pSrc.user_instname)) {
			result = gEp.ui.links.instanceDetails(pSrc.user_instname);
		}

		return result;
	}

}
