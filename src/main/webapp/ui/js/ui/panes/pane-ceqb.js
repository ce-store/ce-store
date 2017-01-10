/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.ceqb = new PaneCeqb();

function PaneCeqb() {
	var iDomParentName = 'mainContainer';
	var iDomName = 'ceqbPane';
	var iTitle = 'Ce Query Builder (CEQB)';
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
		html += ' <div data-dojo-type ="dijit.layout.ContentPane" id="queryCanvas" region="leading" style="width: 100%; height: 100%; background-color:beige;"></div>';
		html += ' <div data-dojo-type ="dijit.layout.ContentPane" id="ceqbFooter" region="bottom" style="width: 100%; height: 130px;">';
		html += '  <div data-dojo-type="dijit.layout.BorderContainer" style="width: 100%; height: 100%">';
		html += '   <div data-dojo-type ="dijit.layout.ContentPane" id="ceQueryText" region="leading" style="width: 75%; height: 100%;">(CE will appear here)</div>';
		html += '   <div data-dojo-type ="dijit.layout.ContentPane" id="ceButtons" region="right" style="width: 25%; height: 100%;">';
		html += '    <div data-dojo-type ="dijit.layout.ContentPane" id="ceqbName" style="width:auto;">';
		html += '     Query/rule name: <div data-dojo-type="dijit.form.TextBox" id="ceqbQueryName" style="width:auto;" onKeyUp="' + gEp.dlg.ceqb.links.jsTextForEditedQueryName() + '"></div>';
		html += '    </div>';
		html += '    <ul>';
		html += '     <li>' + gEp.dlg.ceqb.links.clearCanvas() + '</li>';
		html += '     <li>' +  gEp.dlg.ceqb.links.validateQueryOrRule() + '</li>';
		html += '     <li>';
		html += '      Execute as ' +  gEp.dlg.ceqb.links.executeAsQuery() + ' or ' + gEp.dlg.ceqb.links.executeAsRule() + '<br/>';
		html += '      No CE? <input id="no_ce" type="checkbox"/>';
		html += '     </li>';
		html += '     <li>' + gEp.dlg.ceqb.links.saveQueryOrRule() + '</li>';
		html += '    <ul/>';
		html += '   <div/>';
		html += '  <div/>';
		html += ' <div/>';
		html += '<div/>';

		return html;
	}

	function tabSwitchFunction() {
		gEp.dlg.ceqb.initialiseCeqbEnvironment();
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
