/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.form.form_chooserel = new FormChooseRelationship();

function FormChooseRelationship() {
	var iDomName = 'ceqbForm_ChooseRelationship';

	this.initialise = function() {
		gCe.msg.debug('FormChooseRelationahip', 'initialise');

		gEp.ui.registerForm(iDomName, initialHtml());
	};

	function initialHtml() {
		var html = '';

		html += '<div data-dojo-type="dijit.Dialog" id="formRelationshipDetails" title="Relationship details" style="display: none">';
		html += ' <form data-dojo-type="dijit.form.Form">';
		html += '  <div class="dijitDialogPaneContentArea">';
		html += '   <table style="width:500px;">';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgRelProperty">Relationship:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.FilteringSelect" data-dojo-props="searchattr:\'name\', required:true" id="dlgRelProperty"  onChange="' + gEp.dlg.ceqb.links.jsTextForChangedRelProp() + '" style="width:100%;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '    <tr><td colspan="2"><hr/></td></tr>';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgRelSource">Relationship source:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.TextBox" id="dlgRelSource" data-dojo-props="readOnly:true" style="width:auto;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgRelTarget">Relationship target:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.TextBox" id="dlgRelTarget" data-dojo-props="readOnly:true" style="width:auto;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgRelPropertyDetails">Property details:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.TextBox" id="dlgRelPropertyDetails" data-dojo-props="readOnly:true" style="width:100%;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '   </table>';
		html += '  </div>';
		html += '  <div class="dijitDialogPaneActionBar">';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.dlg.ceqb.links.jsTextForSaveDialogRelDetails() + '">OK</button>';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.dlg.ceqb.links.jsTextForCancelDialogRelDetails() + '">Cancel</button>';
		html += '  </div>';
		html += ' </form>';
		html += '</div>';

		return html;
	}

}
