/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.config = new PaneConfig();

function PaneConfig() {
	var iDomParentName = 'mainContainer';
	var iDomName = 'configPane';
	var iTitle = 'Config';
	var iTabPos = 70;

	this.DOM_CESTORELIST = 'dlgCeStoreList';
	this.DOM_SERVERLIST = 'dlgServerList';

	this.initialise = function() {
		gCe.msg.debug(iDomName, 'initialise');

		gEp.ui.registerTab(iDomName, iDomParentName, iTabPos, iTitle, this.initialHtml());
	};

	this.resetContents = function() {
		gCe.msg.debug(iDomName, 'resetContents');

		this.updateAndParseWith(this.initialHtml());
	};

	this.initialHtml = function() {
		var html = '';

		html += '<div data-data-dojo-type="dijit.layout.ContentPane" id="configControls" style="width: 100%;">';
		html += ' <h3>HTTP parameters</h3>';
		html += ' <table border="1" cellpadding="5">';
		html += '  <tr>';
		html += '   <td>';
		html += '    <label for="' + this.DOM_SERVERLIST + '">Server:</label>';
		html += '    <div data-dojo-type="dijit.form.FilteringSelect" searchattr="name" id="' + this.DOM_SERVERLIST + '" onChange="' + gEp.ui.links.jsTextForChangedServer() + '" required="false" style="width:200px;"></div>';
		html += '    ' + gEp.ui.links.refreshServerList() + ' |';
		html += '    ' + gEp.ui.links.addServer();
		html += '   </td>';
		html += '   <td rowspan="3">';
		html += '    JS debug?';
		html += '    <input';
		html += '     id="jsDebug" name="jsDebug"';
		html += '     data-dojo-type="dijit.form.CheckBox"';
		html += '     value="true"';
		html += '     onChange="' + gEp.ui.links.jsTextForChangedJsDebug() + '"';
		html += '     title="This switches javascript debug on or off (debug messages are logged to the browser console)">';
		html += '   <br/>';
		html += '    Auto-run rules?';
		html += '    <input';
		html += '     id="autoRunRules" name="autoRunRules"';
		html += '     data-dojo-type="dijit.form.CheckBox"';
		html += '     value="true"';
		html += '     onChange="' + gEp.ui.links.jsTextForChangedAutoRunRules() + '"';
		html += '     title="If checked this will automatically run rules whenever new sentences are loaded">';
		html += '   <br/>';
		html += '    Show stats?';
		html += '    <input';
		html += '     id="showStats" name="showStats"';
		html += '     data-dojo-type="dijit.form.CheckBox"';
		html += '     value="true" checked';
		html += '     onChange="' + gEp.ui.links.jsTextForChangedShowStats() + '"';
		html += '     title="If checked this will append showStats=true to all new style HTTP requests">';
		html += '   <br/>';
		html += '    Return CE?';
		html += '    <input';
		html += '     id="returnCe" name="returnCe"';
		html += '     data-dojo-type="dijit.form.CheckBox"';
		html += '     value="true"';
		html += '     onChange="' + gEp.ui.links.jsTextForChangedReturnCe() + '"';
		html += '     title="If checked this will append returnCe=true to all new style HTTP requests">';
		html += '   </td>';
		html += '  </tr>';
		html += '  <tr>';
		html += '   <td>';
		html += '    App name: <input value="ce-store/">';
		html += '   </td>';
		html += '  </tr>';
		html += '  <tr>';
		html += '   <td>';
		html += '    <label for="' + this.DOM_CESTORELIST + '">CE Store:</label>';
		html += '    <div data-dojo-type="dijit.form.FilteringSelect" searchattr="name" id="' + this.DOM_CESTORELIST + '" onChange="' + gEp.ui.links.jsTextForChangedCeStore() + '" required="false" style="width:100px;"></div>';
		html += '    ' + gEp.ui.links.refreshStoreList() + ' |';
		html += '    ' + gEp.ui.links.addCeStore() + ' |';
		html += '    ' + gEp.ui.links.deleteCeStore();
		html += '   </td>';
		html += '  </tr>';
		html += ' </table>';
		html += ' <hr/>';
		html += '</div>';
		html += '<h3>CE Store properties</h3>';
		html += gEp.ui.links.showStoreConfiguration() + '<br>';
		html += '<div data-data-dojo-type="dijit.layout.ContentPane" id="configValuesPane" style="width: 100%; height: 100%;">';
		html += '</div>';
	
		return html;
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