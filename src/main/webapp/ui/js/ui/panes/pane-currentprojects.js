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
	var jsonUrls = [ './json/local_projects.json',
			'./json/public_projects.json' ];

	this.initialise = function() {
		gCe.msg.debug(iDomName, 'initialise');

		gEp.ui.registerTab(iDomName, iDomParentName, iTabPos, iTitle, '');

		this.requestContents();
	};

	this.requestContents = function() {
		for ( var i in jsonUrls) {
			var thisUrl = jsonUrls[i];

			requestContentsFor(thisUrl);
		}
	};

	this.setContents = function(pProjects) {
		gCe.msg.debug(iDomName, 'setContents');

		this.updateWith(initialHtml(iDomName, pProjects));
	};

	function requestContentsFor(pUrl) {
		var cbf = function(json) { gEp.ui.pane.currentProjects.setContents(json);};
		var parms = gEp.stdHttpParms();

		gCe.api.sendAjaxGet(pUrl, parms, cbf, {});
	}

	initialHtml = function(pDomName, pProjects) {
		var html = '';
		var tgt = document.getElementById(pDomName);

		if (tgt != null) {
			html = tgt.innerHTML;
		}

		html += htmlListFor(pProjects);

		return html;
	}

	this.updateWith = function(pHtml, pShow, pTitle) {
		if (pShow) {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName, iDomParentName);
		} else {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName);
		}
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
