/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.sources = new HandlerSources();

function HandlerSources() {
	var ren = null;		//This is set during initialise

	this.initialise = function() {
		gCe.msg.debug('HandlerSources', 'initialise');

		ren = gEp.renderer.sources;
	};

	this.listAllSources = function(pCbf, pUserParms) {
		var cbf = null;

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sources.processSourceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.sources.listAll(gEp.stdHttpParms(), cbf, userParms);
	};

	this.getSourceDetails = function(pSrcId, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { source_id: pSrcId };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sources.processSourceDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.sources.getDetailsFor(gEp.stdHttpParms(), cbf, pSrcId, userParms);
	};

	this.deleteSource = function(pSrcId, pCbf, pUserParms) {
		var answer = confirm('Are you sure you want to delete source \'' + pSrcId + '\'?');

		if (answer) {
			var localUserParms = { source_id: pSrcId };

			if (pCbf == null) {
				cbf = function(pResponseObject) { gEp.handler.sources.processDeletedSource(pResponseObject); };
			} else {
				cbf = pCbf;
			}

			var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
			gCe.api.sources.deleteSource(gEp.stdHttpParms(), cbf, pSrcId, userParms);
		}
	};

	this.getSentenceTextFor = function(pSrcId) {
		var userParms = { title: 'CE sentences for source ' + pSrcId };
		var cbf = function(response, userParms) { gEp.showCeInPopupWindow(userParms.title, gCe.utils.formatForHtml(response)); };

		var httpParms = gEp.stdHttpParms();
		httpParms.accept = gCe.FORMAT_TEXT;

		gCe.api.sources.listSentencesFor(httpParms, cbf, pSrcId, userParms);
	};

	this.processSourceList = function(pResponseObject, pUserParms) {
		var allSources = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderSourceList(allSources, pUserParms);
	};

	this.processSourceDetails = function(pResponseObject, pUserParms) {
		var thisSrc = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderSourceDetails(thisSrc, pUserParms);
	};

	this.processDeletedSource = function(pResponseObject, pUserParms) {
		//The source has been deleted so refresh the list of sources
		gEp.handler.sources.listAllSources();
	};

}
