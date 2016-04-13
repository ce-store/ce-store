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
					{ url: 'ce-store/ce/medicine/cmd/med_load.cecmd', name: 'Medicine' },
					{ url: 'ce-store/ce/road-to-war/cmd/load.cecmd', name: 'Road To War' },
	                { url: 'ce-store/ce/road-to-war/cmd/loadbackground.cecmd', name: 'Road To War Background' },
	                { url: 'ce-store/ce/road-to-war/cmd/loadserials.cecmd', name: 'Road To War all Serials' },
	                { url: 'ce-store/ce/road-to-war/cmd/loadserial1-2.cecmd', name: 'Road To War Serial 1-2' },
	                { url: 'ce-store/ce/road-to-war/cmd/loadserial3.cecmd', name: 'Road To War Serial 3' },
	                { url: 'ce-store/ce/road-to-war/cmd/loadserial4.cecmd', name: 'Road To War Serial 4' },
	                { url: 'ce-store/ce/road-to-war/cmd/loadserial5-12.cecmd', name: 'Road To War Serial 5-12' }
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