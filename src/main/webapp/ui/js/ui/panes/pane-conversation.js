/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.conversation = new PaneConversation();

function PaneConversation() {
	var iDomParentName = 'mainContainer';
	var iDomName = 'convPane';
	var iTitle = 'Conversation';
	var iTabPos = 40;

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

		html += '<div data-dojo-type="dijit.layout.ContentPane" id="convEnable" style="display:block;">';
		html += ' <table border="1" style="width:100%">';
		html += '  <tr>';
		html += '   <td align="center">' + gEp.dlg.conv.links.enableConversation() + '</td>';
		html += '  </tr>';
		html += ' </table>';
		html += '</div>';
		html += '<div data-dojo-type="dijit.layout.BorderContainer" id="convBc" style="width: 100%; height: 100%" splitter="true" style="display:none;">';
		html += ' <div data-dojo-type="dijit.layout.ContentPane" id="convHistory" region="leading" style="height: 100%"></div>';
		html += ' <div data-dojo-type="dijit.layout.ContentPane" id="convControls" region="bottom">';
		html += '  <table border="1" style="width:100%;">';
		html += '   <tr>';
		html += '    <td colspan="2">';
		html += '     Total score: <span id="totalScore">0</span>';
		html += '    </td>';
		html += '   </tr>';
		html += '   <tr>';
		html += '    <td colspan="2">';
		html += '     ' + gEp.dlg.conv.links.showExistingConversation() + ' |';
		html += '     ' + gEp.dlg.conv.links.showNewConversation() + ' |';
		html += '     auto update <input id="autoUpdate" name="autoUpdate" data-dojo-type="dijit.form.CheckBox" value="true" onChange="' + gEp.dlg.conv.links.jsTextForChangedAutoUpdate() + '" title="If checked this will automatically request and append new messages every 5 seconds"> |';
		html += '     nosey <input id="noseyMode" name="noseyMode" data-dojo-type="dijit.form.CheckBox" value="true" checked onChange="' + gEp.dlg.conv.links.jsTextForChangedNoseyMode() + '" title="If checked this will show all conversation messages regardless of sender and recipient, otherwise only messages relating to the logged in user will be shown">';
		html += '    </td>';
		html += '   </tr>';
		html += '   <tr>';
		html += '    <td valign="top">';
		html += '     <span id="convText_Status"></span><br/>';
		html += '     <form data-dojo-type="dijit.form.Form">';
		html += '      <b>What do you want?</b><br>';
		html += '      <textarea id="convText" name="convText" data-dojo-type="dijit.form.SimpleTextarea" rows="8" cols="50" onKeyUp="gEp.dlg.conv.autoValidateCe();"></textarea>';
		html += '      <br/>';
		html += '      <button data-dojo-type="dijit.form.Button" type="button" onClick="' + gEp.dlg.conv.links.jsTextForSendConversationCe() + '">send message</button>';
		html += '     </form>';
		html += '    </td>';
		html += '    <td>';
		
		html += '      auto send <input id="autoSend" name="autoSend" data-dojo-type="dijit.form.CheckBox" value="true" checked onChange="' + gEp.dlg.conv.links.jsTextForChangedAutoSend() + '">';
		html += '     </form>';
		html += '     <br>';
		html += '     <b>Location:</b><br>';
		html += '     <input type="text" id="locName" width="20" value=""></input>';
		html += '     <br>';
		html += '    </td>';
		html += '   </tr>';
		html += '  </table>';
		html += ' </div>';
		html += '</div>';

		return html;
	}

	function tabSwitchFunction() {
		gEp.dlg.conv.initialiseConvEnvironment();
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
