/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.sentence = new PaneSentence();

function PaneSentence() {
	var iDomParentName = 'detailsContainer';
	var iDomName = 'sentencePane';
	var iTitle = 'Sentences';
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
		var senLinks = getSentenceLinks();
		var linkList = [];
		var htmlText = '';

		for (var key in senLinks) {
			var thisLink = senLinks[key];
			var jsMethod = thisLink.jsParts[0];
			var jsParms = [];

			for (var i = 1; i < thisLink.jsParts.length; i++) {
				jsParms.push(thisLink.jsParts[i]);
			}

			var jsText = gEp.ui.links.jsTextFor(jsMethod, jsParms);
			var linkText = gEp.ui.links.hyperlinkFor(jsText, thisLink.name);

			linkList.push(linkText);
		}

		htmlText += gEp.ui.htmlPaneHeaderFor('List various types of sentences:');
		htmlText += gEp.ui.htmlUnorderedListFor(linkList);

		return htmlText;
	}

	function getSentenceLinks() {
		var list = [];

		list.push({ name: 'All sentences', jsParts: [ gEp.ui.links.JS_SEN_ALLLIST ]});
		list.push({ name: 'Model sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, 'model' ]});
		list.push({ name: 'Fact sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, 'fact' ]});
		list.push({ name: 'Pattern sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, 'pattern' ]});
		list.push({ name: 'Rule sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, 'rule' ]});
		list.push({ name: 'Query sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, 'query' ]});
		list.push({ name: 'Annotation sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, 'annotation' ]});
		list.push({ name: 'Command sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, 'command' ]});
		list.push({ name: 'Valid sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, '', 'valid' ]});
		list.push({ name: 'Invalid sentences', jsParts: [ gEp.ui.links.JS_SEN_QUALLIST, '', 'invalid' ]});

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
