/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.map = new PaneMap();

function PaneMap() {
	var iDomParentName = 'mainContainer';
	var iDomName = 'mapPane';
	var iTitle = 'Map';
	var iTabPos = 30;

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
		var html ='';

		html += '<div data-dojo-type="dijit.layout.BorderContainer" style="width: 100%; height: 100%">';
		html += ' <div data-dojo-type="dijit.layout.ContentPane" id="mapDetails" region="leading" style="width: 100%; height: 100%;"></div>';
		html += ' <div data-dojo-type="dijit.layout.ContentPane" id="mapControls" region="bottom" style="width: 100%; height: 40px;">';
		html += '  <table border="1" style="width: 100%">';
		html += '   <tr>';
		html += '    <td>';
		html += '     ' + gEp.dlg.map.links.redrawMap() + ' | ';
		html += '     ' + gEp.dlg.map.links.manageLayers() + ' | ';
		html += '     ' + gEp.dlg.map.links.calculateActiveBoundingBox();
		html += '     (auto <input id="autoCalc" name="autoCalc" data-dojo-type="dijit.form.CheckBox" value="true" checked onChange="' + gEp.dlg.map.links.jsTextForChangedAutoCalc() + '">) | ';
		html += '     ' + gEp.dlg.map.links.generateSpatialViewCe() +' | ';
		html += '     ' + gEp.dlg.map.links.debugMap() +' | ';
		html += '     <label for="dlgSpatialViews">Spatial views:</label>';
		html += '     <div data-dojo-type="dijit.form.FilteringSelect" searchattr="name" id="dlgSpatialViews" onChange="' + gEp.dlg.map.links.jsTextForResizeMap() + '" required="false"></div> | ';
		html += '     ' + gEp.dlg.map.links.listSpatialViews();
		html += '    </td>';
		html += '    <td><div id="mapCoords">&nbsp;</div></td>';
		html += '   </tr>';
		html += '  </table>';
		html += ' </div>';
		html += '</div>';

		return html;
	}

	function tabSwitchFunction() {
		gEp.dlg.map.drawMapPane();
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
