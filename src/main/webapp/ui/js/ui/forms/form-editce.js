/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.form.form_editce = new FormEditCe();

function FormEditCe() {
	var iDomName = 'ceqbForm_ManualEditCe';

	this.initialise = function() {
		gCe.msg.debug('FormChooseFilter', 'initialise');

		gEp.ui.registerForm(iDomName, initialHtml());
	};

	function initialHtml() {
		var html = '';

		html += '<div data-dojo-type="dijit.Dialog" id="formManualEditCe" title="Manually edit CE" style="display: none">';
		html += ' <form data-dojo-type="dijit.form.Form">';
		html += '  <div class="dijitDialogPaneContentArea">';
		html += '   <table style="width:500px;">';
		html += '    <tr>';
		html += '     <td align="right" valign="top"><label for="dlgCeText">CE text:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.SimpleTextarea" id="dlgCeText" data-dojo-props="required:true, rows:10, cols:50" style="width:auto"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '   </table>';
		html += '  </div>';
		html += '  <div class="dijitDialogPaneActionBar">';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.dlg.ceqb.links.jsTextForSaveManualCe() + '">OK</button>';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.dlg.ceqb.links.jsTextForCancelManualCe() + '">Cancel</button>';
		html += '  </div>';
		html += ' </form>';
		html += '</div>';

		return html;
	}

}
