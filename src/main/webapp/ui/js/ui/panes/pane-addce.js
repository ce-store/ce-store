/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.addce = new PaneAddCe();

function PaneAddCe() {
	var iDomParentName = 'menuContainer';
	var iDomName = 'addCePane';
	var iTitle = 'Add CE';
	var iTabPos = 30;

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

		html += '<b>Add CE sentence:</b>&nbsp;&nbsp;&nbsp;<span id="ceText_Status"></span><br>';
		html += '<textarea id="ceText" name="ceText" data-dojo-type="dijit.form.SimpleTextarea" rows="12" cols="25" onKeyUp="gEp.handler.sentences.autoValidateCe(\'ceText\');">';
		html += 'there is a person named \'Fred Smith\'.';
		html += '</textarea>';
		html += '<br>';
		html += gEp.ui.links.addSentencesFromForm('generalCeForm') + '<br>';

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