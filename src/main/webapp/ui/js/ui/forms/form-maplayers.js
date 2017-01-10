/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.form.form_maplayers = new FormMapLayers();

function FormMapLayers() {
	var iDomName = 'mapForm_MapLayers';

	this.initialise = function() {
		gCe.msg.debug('FormMapLayers', 'initialise');

		gEp.ui.registerForm(iDomName, initialHtml());
	};

	function initialHtml() {
		var html = '';

		html += '<div data-dojo-type="dijit.Dialog" id="formMapLayers" title="Manage map layers" style="display: none">';
		html += ' <form data-dojo-type="dijit.form.Form">';
		html += '  <div class="dijitDialogPaneContentArea">';
		html += '   <table style="width:500px;">';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgLayerList">Layer:</label></td>';
		html += '     <td>';
		html += '      <select id="dlgLayerList" name="dlgLayerList" data-dojo-type="dijit.form.MultiSelect" onChange="' + gEp.dlg.map.links.jsTextForChangedSelectedLayer() + '" size="5"></select>';
		html += '     </td>';
		html += '     <td>';
		html += '      <input id="showPoints" name="showPoints" data-dojo-type="dijit.form.CheckBox" value="false" onChange="' + gEp.dlg.map.links.jsTextForShowPoints() + '">Show points<br>';
		html += '      <input id="showHeatmap" name="showHeatmap" data-dojo-type="dijit.form.CheckBox" value="false" onChange="' + gEp.dlg.map.links.jsTextForShowHeatmap() + '">Show heatmap<br>';
		html += '      ' + gEp.dlg.map.links.deleteLayer() + '<br>';
		html += '      ' + gEp.dlg.map.links.zoomToLayer();
		html += '     </td>';
		html += '    </tr>';
		html += '   </table>';
		html += '  </div>';
		html += '  <div class="dijitDialogPaneActionBar">';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.dlg.map.links.jsTextForCloseMapLayersForm() + '">OK</button>';
		html += '  </div>';
		html += ' </form>';
		html += '</div>';

		return html;
	}

}
