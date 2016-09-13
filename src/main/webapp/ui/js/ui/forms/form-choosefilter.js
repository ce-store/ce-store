/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.form.form_choosefilter = new FormChooseFilter();

function FormChooseFilter() {
	var iDomName = 'ceqbForm_ChooseFilter';

	this.initialise = function() {
		gCe.msg.debug('FormChooseFilter', 'initialise');

		gEp.ui.registerForm(iDomName, initialHtml());
	};

	function initialHtml() {
		var html = '';

		html += '<div data-dojo-type="dijit.Dialog" id="formFilterDetails" title="Filter details" style="display:none">';
		html += ' <form data-dojo-type="dijit.form.Form">';
		html += '  <div class="dijitDialogPaneContentArea">';
		html += '   <table style="width:500px;">';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgFilterVariableId">Variable:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.TextBox" id="dlgFilterVariableId" data-dojo-props="required:true" style="width:auto;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgFilterPropery">Attribute:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.FilteringSelect" data-dojo-props="searchattr:\'name\', required:true" id="dlgFilterProperty" onChange="' + gEp.dlg.ceqb.links.jsTextForChangedFilterProp() + '" style="width:100%;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgFilterOperator">Operator:</label></td>';
		html += '     <td>';
		html += '      <select data-dojo-type="dijit.form.ComboBox" id="dlgFilterOperator" data-dojo-props="required:true" name="operator">';
		html += '       <option selected>(none)</option>';
		html += '       <option>=</option>';
		html += '       <option>contains</option>';
		html += '       <option>matches</option>';
		html += '       <option>starts-with</option>';
		html += '       <option>ends-with</option>';
		html += '       <option>!=</option>';
		html += '       <option>&gt;</option>';
		html += '       <option>&gt;=</option>';
		html += '       <option>&lt;</option>';
		html += '       <option>&lt;=</option>';
		html += '      </select>';
		html += '     </td>';
		html += '    </tr>';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgFilterValue">Filter value:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.TextBox" id="dlgFilterValue" data-dojo-props="required:false" style="width:auto;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '    <tr><td colspan="2"><hr/></td></tr>';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgFilterSource">Attribute source:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.TextBox" id="dlgFilterSource" data-dojo-props="readOnly:true" style="width:auto;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgFilterPropertyDetails">Attribute details:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.TextBox" id="dlgFilterPropertyDetails" data-dojo-props="readOnly:true" style="width:100%;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '   </table>';
		html += '  </div>';
		html += '  <div class="dijitDialogPaneActionBar">';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.dlg.ceqb.links.jsTextForSaveDialogFilterDetails() + '">OK</button>';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.dlg.ceqb.links.jsTextForCancelDialogFilterDetails() + '">Cancel</button>';
		html += '  </div>';
		html += ' </form>';
		html += '</div>';

		return html;
	}

}
