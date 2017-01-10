/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.modelgraph = new PaneModelGraph();

function PaneModelGraph() {
	var iDomParentName = 'mainContainer';
	var iDomName = 'modelGraphPane';
	var iTitle = 'Model Graph';
	var iTabPos = 20;

	this.initialise = function() {
		gCe.msg.debug(iDomName, 'initialise');

		gEp.ui.registerTab(iDomName, iDomParentName, iTabPos, iTitle, initialHtml());
		gEp.ui.registerTabSwitchFunction(iDomParentName, iDomName, tabSwitchFunction );
	};

	this.resetContents = function() {
		gCe.msg.debug(iDomName, 'resetContents');

		this.updateWith(initialHtml());
	};

	function initialHtml() {
		var html = '';

		html += '<div data-dojo-type="dijit.layout.BorderContainer" style="width: 100%; height: 100%">';
		html += '<iframe style="width:100%; height:100%; border:none; margin:0px; padding:0px;" src="/ce-store/ui/modelgraph/"></iframe>';
		html += '<div/>';

		return html;
	}

	function tabSwitchFunction() {
		
	}

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