/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.form.form_login = new FormLogin();

function FormLogin() {
	var iDomName = 'userForm_Login';

	this.initialise = function() {
		gCe.msg.debug('FormLogin', 'initialise');

		gEp.ui.registerForm(iDomName, initialHtml());
	};

	function initialHtml() {
		var html = '';

		html += '<div data-dojo-type="dijit.Dialog" id="formUserLogin" title="User login" style="display: none">';
		html += ' <form data-dojo-type="dijit.form.Form">';
		html += '  <div class="dijitDialogPaneContentArea">';
		html += '   <table style="width:500px;">';
		html += '    <tr>';
		html += '     <td align="right"><label for="dlgUserName">User:</label></td>';
		html += '     <td>';
		html += '      <div data-dojo-type="dijit.form.FilteringSelect" data-dojo-props="searchattr:\'name\', required:true" id="dlgUserName" style="width:100%;"></div>';
		html += '     </td>';
		html += '    </tr>';
		html += '   </table>';
		html += '  </div>';
		html += '  <div class="dijitDialogPaneActionBar">';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.ui.links.jsTextForSaveLoginForm() + '">OK</button>';
		html += '   <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.ui.links.jsTextForCancelLoginForm() + '">Cancel</button>';
		html += '  </div>';
		html += ' </form>';
		html += '</div>';
		html += '';
		html += '';

		return html;
	}

}