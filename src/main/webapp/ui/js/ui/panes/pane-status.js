/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.status = new PaneStatus();

function PaneStatus() {
	var iDomName = 'statusPane';

	this.initialise = function() {
		gCe.msg.debug(iDomName, 'initialise');

		gEp.ui.updateAndParsePaneWith(initialHtml(), '', iDomName);
	};

	this.resetContents = function() {
		gCe.msg.debug(iDomName, 'resetContents');

		this.updateWith(initialHtml());
	};

	function initialHtml() {
		var html ='';

		return html;
	}

	this.updateWith = function(pHtml) {
		gEp.ui.updatePaneWith(pHtml, '', iDomName);
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

}
