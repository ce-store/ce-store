/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.action = new PaneAction();

function PaneAction() {
	var iDomParentName = 'menuContainer';
	var iDomName = 'actionPane';
	var iTitle = 'Actions';
	var iTabPos = 20;

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

		html += htmlForSentenceSets();
		html += htmlForStoreLinks();
        html += htmlForSentenceUpload();

		return html;
	}

	function htmlForStoreLinks() {
		var html = '';
		var storeLinks = getGeneralCeStoreLinks();
		var linkList = [];

		for (var key in storeLinks) {
			var thisLink = storeLinks[key];

			var jsMethod = thisLink[0];
			var hrefText = thisLink[1];

			var jsText = gEp.ui.links.jsTextFor(jsMethod);
			var thisLink = gEp.ui.links.hyperlinkFor(jsText, hrefText);

			linkList.push(thisLink);
		}

		html += '<h3>CE Store links:</h3>';
		html += gEp.ui.htmlUnorderedListFor(linkList);

		return html;
	}

	function htmlForSentenceSets() {
		var html = '';

		for (var key in gEp.sentenceSets) {
			var thisSet = gEp.sentenceSets[key];
			var linkList = [];

			html += '<h3>' + thisSet.title + ':' + '</h3>';

			for (var lKey in thisSet.links) {
				var thisSenSet = thisSet.links[lKey];
				var jsText = null;

				if (gCe.utils.isNullOrEmpty(thisSenSet.url)) {
					jsText = gEp.ui.links.jsTextFor('gEp.dlg.sentence.processCommandsAbsolute');
				} else {
					jsText = gEp.ui.links.jsTextFor('gEp.dlg.sentence.loadNewSentenceSet', [ thisSenSet.url ]);
				}

				linkList.push(gEp.ui.links.hyperlinkFor(jsText, thisSenSet.name));
			}

			html += gEp.ui.htmlUnorderedListFor(linkList);
		}

		return html;
	}

	// Calls handler-sentences.js/uploadSentencesFromForm.
	// Does not use dojo widgets, believe that would require switching to
	// dojox.form.Uploader, but haven't yet worked out how to plug that into the
	// existing ce js upload code.
	function htmlForSentenceUpload() {
		var html = '';
        html += '<h3>Upload CE from file:</h3>';
        html += '<form id="generalCeUploadForm" method="post" enctype="multipart/form-data" action="/">';
        html += '  <div style="display:flex; flex-wrap:wrap;">';
        html += '    <input style="border:1px solid #DDDDDD;" id="upload-form-input-id" name="file" type="file" />';
		html += '    <button type="button" onclick="' + 'gEp.handler.sentences.uploadSentencesFromForm(this.parentNode.parentNode)' + '">Upload</button>';
        html += '  </div>';
        html += '</form>';

        return html;
    }
    
	function getGeneralCeStoreLinks() {
		var list = [];

		list.push([ 'gEp.handler.stores.listAllStores', 'List CE Stores' ]);
		list.push([ 'gEp.handler.actions.resetStore', 'Reset (drop) store' ]);
		list.push([ 'gEp.handler.actions.reloadStore', 'Reload store' ]);
		list.push([ 'gEp.handler.actions.emptyInstanceData', 'Empty instance data' ]);
		list.push([ 'gEp.handler.actions.showStoreStatistics', 'Show store stats' ]);
		list.push([ 'gEp.handler.actions.showNextUid', 'Show next UID' ]);
		list.push([ 'gEp.handler.actions.getNextUid', 'Get next UID' ]);
		list.push([ 'gEp.handler.actions.resetUids', 'Reset UIDs' ]);
		list.push([ 'gEp.handler.actions.getUidBatch', 'Get UID batch' ]);
		list.push([ 'gEp.handler.actions.listAllRationale', 'Show all rationale' ]);
		list.push([ 'gEp.handler.actions.listShadowConcepts', 'List shadow concepts' ]);
		list.push([ 'gEp.handler.actions.listShadowInstances', 'List shadow instances' ]);
		list.push([ 'gEp.handler.actions.listUnreferencedInstances', 'List unreferenced instances' ]);
		list.push([ 'gEp.handler.actions.listUnreferencedInstancesNoMetaModel', 'List unreferenced non-metamodel instances' ]);
		list.push([ 'gEp.handler.actions.listDiverseConceptInstances', 'List diverse concept instances' ]);

		return list;
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