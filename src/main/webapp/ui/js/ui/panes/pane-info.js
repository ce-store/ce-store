/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.info = new PaneInfo();

function PaneInfo() {
	var iDomParentName = 'feedbackContainer';
	var iDomName = 'infoPane';
	var iTitle = 'Infos (0)';
	var iTabPos = 30;

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

		return html;
	}

	this.updateWith = function(pHtml, pTitle) {
		gEp.ui.updatePaneWith(pHtml, pTitle, iDomName);
	};

	this.updateWith = function(pHtml, pShow, pTitle) {
		if (pShow) {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName, iDomParentName);
		} else {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName);
		}
	};

	this.updateAndParseWith = function(pHtml, pShow, pTitle) {
		if (pShow) {
			gEp.ui.updateAndParsePaneWith(pHtml, pTitle, iDomName, iDomParentName);
		} else {
			gEp.ui.updateAndParsePaneWith(pHtml, pTitle, iDomName);
		}
	};

	this.activateTab = function() {
		gEp.ui.activateTab(iDomName, iDomParentName);
	};

}
