/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.messages = new RendererMessages();

function RendererMessages() {
	var NBSP = '&nbsp;';
	var TAB = NBSP + NBSP + NBSP;

	this.initialise = function() {
		gCe.msg.debug('RendererMessages', 'initialise');
		//Nothing needed
	};

	this.updateDebugsPane = function(pDebugList) {
		var html = null;
		var title = null;
		var showPane = false;

		if ((pDebugList != null) && (pDebugList.length > 0)) {
			title = 'Debugs (' + pDebugList.length + ')';
			html = htmlForList(pDebugList);
			showPane = true;
		} else {
			title = 'Debugs (0)';
			html = 'No debugs were returned in the last request.';
		}

		html += htmlDebugLinks();

		gEp.ui.pane.debug.updateWith(html, showPane, title);
	};

	this.updateInfosPane = function(pInfoList) {
		var html = null;
		var title = null;
		var showPane = false;

		if ((pInfoList != null) && (pInfoList.length > 0)) {
			html = htmlForList(pInfoList);
			title = 'Infos (' + pInfoList.length + ')';
			showPane = true;
		} else {
			html = 'No infos were returned in the last request.';
			title = 'Infos (0)';
		}

		gEp.ui.pane.info.updateWith(html, showPane, title);
	};

	this.updateWarningsPane = function(pWarningList) {
		var html = null;
		var title = null;
		var showPane = false;

		if ((pWarningList != null) && (pWarningList.length > 0)) {
			html = htmlForList(pWarningList);
			title = 'Warnings (' + pWarningList.length + ')';
			showPane = true;
		} else {
			html = 'No warnings were returned in the last request.';
			title = 'Warnings (0)';
		}

		gEp.ui.pane.warning.updateWith(html, showPane, title);
	};

	this.updateErrorsPane = function(pErrorList) {
		var html = null;
		var title = null;
		var showPane = false;

		if ((pErrorList != null) && (pErrorList.length > 0)) {
			html = htmlForList(pErrorList);
			title = 'Errors (' + pErrorList.length + ')';
			showPane = true;
		} else {
			html = 'No errors were returned in the last request.';
			title = 'Errors (0)';
		}

		gEp.ui.pane.error.updateWith(html, showPane, title);
	};

	this.htmlStatusMessage = function(pStats) {
		var result = '';
		var txnText = '';

		if (pStats.total_txns > 0) {
			txnText =  ' for <b>' + pStats.total_txns + '</b>, ';
			txnText += '<b>' + pStats.successful_txns + '</b> transactions';
		}

		result += 'Last transaction';
		result += ' took <b>' + pStats.duration + '</b> seconds';
		result += txnText + '. ';
		result += gEp.ui.htmlCurrentCeStore() + ' contains <b>' + pStats.instance_count + '</b> instances and ';
		result += '<b>' + pStats.sentence_count + '</b> sentences. ';
		result += 'Code version=';
		result += gEp.ui.links.showVersions(pStats.code_version);

		result += standardStatusText();

		gEp.ui.pane.status.updateWith(result);
	};

	this.htmlDefaultHeaderMessage = function() {
		var result = '';

		result += 'No stats returned in last request... ';
		result += standardStatusText();

		gEp.ui.pane.status.updateWith(result);
	};

	function htmlForList(pList) {
		var rows = [];
		var rowCounter = 0;

		for (var key in pList) {
			var thisRow = [];

			thisRow.push(++rowCounter);
			thisRow.push(gCe.utils.htmlFormat(pList[key]));

			rows.push(thisRow);
		}

		return gEp.ui.htmlTableFor(null, rows, gEp.ui.DEFAULT_STYLE);
	}

	function standardStatusText() {
		var result = '';

		result += TAB + '[' + gEp.ui.links.refreshPage() + ']';
		result += NBSP + '[' + gEp.ui.links.help() + ']';
		result += NBSP + '[' + gEp.ui.links.test() + ']';
		result += NBSP + '[' + gEp.ui.links.hudson() + ']';
		result += TAB + TAB + gEp.ui.htmlLoggedInUserText();

		return result;
	}

	function htmlDebugLinks() {
		var hlDebugOn = gEp.ui.links.setDebug('true', 'on');
		var hlDebugOff = gEp.ui.links.setDebug('false', 'off');

		return '<br/><br/>Switch debug ' + hlDebugOn + ' or ' + hlDebugOff + '.';
	}

}
