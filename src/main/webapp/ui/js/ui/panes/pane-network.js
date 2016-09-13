/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.network = new PaneNetwork();

function PaneNetwork() {
	var iDomParentName = 'mainContainer';
	var iDomName = 'networkPane';
	var iTitle = 'Network';
	var iTabPos = 60;

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

		html += 'back links? <input id="backLinks" name="backLinks" data-dojo-type="dijit.form.CheckBox" value="true" onChange="' + gEp.dlg.network.links.jsTextForChangedBackLinks() + '" title="Whether referring instances will have their (back) links shown">  |   ';
		html += 'cross links? <input id="crossLinks" name="crossLinks" data-dojo-type="dijit.form.CheckBox" value="true" onChange="' + gEp.dlg.network.links.jsTextForChangedCrossLinks() + '" title="Whether links from entities one step away will be traversed">  |   ';
		html += 'steps: <div data-dojo-type="dijit.form.TextBox" id="numSteps" name="numSteps" style="width:20px;" value="3"></div>  |   ';
		html += gEp.dlg.network.links.redraw() + '  |   ';
		html += gEp.dlg.network.links.showDebugValues();
		html += '<br/>';
		html += '<svg xmlns="http://www.w3.org/2000/svg" id="svgNetwork" width="100%" height="100%"></svg>';
		html += '<marker id="triangle"';
		html += ' viewBox="0 0 10 10" refX="0" refY="5" ';
		html += ' markerUnits="strokeWidth"';
		html += ' markerWidth="4" markerHeight="3"';
		html += ' fill="#000000"';
		html += ' orient="auto">';
		html += ' <path d="M 0 0 L 10 5 L 0 10 z" />';
		html += '</marker>';

		return html;
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
