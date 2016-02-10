/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.currentProjects = new PaneCurrentProjects();

function PaneCurrentProjects() {
	var iDomParentName = 'menuContainer';
	var iDomName = 'cpPane';
	var iTitle = 'Current Projects';
	var iTabPos = 1;

	this.initialise = function() {
		gCe.msg.debug(iDomName, 'initialise');

		gEp.ui.registerTab(iDomName, iDomParentName, iTabPos, iTitle, initialHtml(this));
	};

	this.resetContents = function() {
		gCe.msg.debug(iDomName, 'resetContents');

		this.updateWith(initialHtml(this));
	};

	function initialHtml(pPane) {
		var html = '';

		html += htmlListFor(pPane.listSentenceSets());

		return html;
	}

	this.updateWith = function(pHtml, pShow, pTitle) {
		if (pShow) {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName, iDomParentName);
		} else {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName);
		}
	};

	this.listSentenceSets = function() {
		return {
			basic: {
				title: 'Basic sentence sets',
				links: [
					{ url: 'CeStore/ce/medicine/cmd/med_load.cecmd', name: 'Medicine' }
					]
			}
		};
	};

	function htmlListFor(pSenListArray) {
		var jsMethod = 'gEp.dlg.sentence.loadNewSentenceSet';
		var html = '';
		
		for (var key in pSenListArray) {
			var thisSenSet = pSenListArray[key];
			var linkList = [];
			
			for (var linkKey in thisSenSet.links) {
				var thisLink = thisSenSet.links[linkKey];
				var jsText = gEp.ui.links.jsTextFor(jsMethod, [ thisLink.url ]);
				var linkText = gEp.ui.links.hyperlinkFor(jsText, thisLink.name);
				linkList.push(linkText);
			}

			html += '<h3>' + thisSenSet.title + ':</h3>';
			html += gEp.ui.htmlUnorderedListFor(linkList);
		}

		return html;
	}
}