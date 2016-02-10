/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.instances = new HandlerInstances();

function HandlerInstances() {
	var ren = null;		//This is set during initialise

	this.initialise = function() {
		gCe.msg.debug('HandlerInstances', 'initialise');

		ren = gEp.renderer.instances;
	};

	this.listAllInstances = function(pSince, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { since: pSince };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.instances.processInstanceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.instances.listAll(gEp.stdHttpParms(), cbf, pSince, userParms);
	};

	this.listReferencesToInstance = function(pInstName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { in_reference_to: pInstName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.instances.processReferencesList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.instances.listReferencesTo(gEp.stdHttpParms(), cbf, pInstName, userParms);
	};

	this.getInstanceDetails = function(pInstName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { instance_name: pInstName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.instances.processInstanceDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.instances.getDetailsFor(gEp.stdHttpParms(), cbf, pInstName, userParms);
	};

	this.listInstancesForConcept = function(pConName, pSince, pCbf, pUserParms) {
		var localUserParms = { concept_name: pConName, since: pSince };
		var cbf = null;

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.instances.processInstanceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.listInstancesFor(gEp.stdHttpParms(), cbf, pConName, pSince, userParms);
	};

	this.listExactInstancesForConcept = function(pConName, pSince, pCbf, pUserParms) {
		var localUserParms = { concept_name: pConName, since: pSince, exact: true };
		var cbf = null;

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.instances.processInstanceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.listExactInstancesFor(gEp.stdHttpParms(), cbf, pConName, pSince, userParms);
	};

	this.countInstancesForConcept = function(pConName, pSince, pCbf, pUserParms) {
		var localUserParms = { concept_name: pConName, since: pSince };
		var cbf = null;

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.instances.processInstanceCount(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.countInstancesFor(gEp.stdHttpParms(), cbf, pConName, pSince, userParms);
	};

	this.listPrimarySentencesForInstance = function(pInstId, pFullDetail, pCbf, pUserParms) {
		var localUserParms = {inst_id: pInstId, context: 'primary', full_detail: pFullDetail };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.instances.listPrimarySentencesFor(gEp.stdHttpParms(), cbf, pInstId, userParms);
	};

	this.listSecondarySentencesForInstance = function(pInstId, pFullDetail, pCbf, pUserParms) {
		var localUserParms = {inst_id: pInstId, context: 'secondary', full_detail: pFullDetail };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.instances.listSecondarySentencesFor(gEp.stdHttpParms(), cbf, pInstId, userParms);
	};

	this.listAllSentencesForInstance = function(pInstId, pFullDetail, pCbf, pUserParms) {
		var localUserParms = {inst_id: pInstId, context: 'all', full_detail: pFullDetail };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.instances.listAllSentencesFor(gEp.stdHttpParms(), cbf, pInstId, userParms);
	};

	this.processInstanceList = function(pResponse, pUserParms) {
		var instList = gCe.utils.getStructuredResponseFrom(pResponse);
		var sortedInsts = instList.sort(sortInstancesById);

		ren.renderInstanceList(sortedInsts, pUserParms);
	};

	this.processReferencesList = function(pResponse, pUserParms) {
		var instList = gCe.utils.getStructuredResponseFrom(pResponse).results;

		ren.renderReferenceList(instList, pUserParms);
	};

	this.processDiverseConceptInstanceList = function(pResponse, pUserParms) {
		var dciList = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderDiverseConceptInstanceList(dciList, pUserParms);
	};

	this.processShadowInstanceList = function(pResponse, pUserParms) {
		var instList = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderShadowInstanceList(instList, pUserParms);
	};

	this.processMultiConceptInstanceList = function(pResponse, pUserParms) {
		var mcInstList = gCe.utils.getStructuredResponseFrom(pResponse);
		var html = '';

		for ( var conName in mcInstList) {
			var instList = mcInstList[conName];
			var parms = { concept_name: conName };

			html += ren.htmlForInstanceList(instList, parms);

			html += '<br/><hr/><br/>';
		}

		gEp.ui.pane.general.updateWith(html, true);
	};

	this.processInstanceDetails = function(pResponse, pUserParms) {
		var thisInst = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderInstanceDetails(thisInst, pUserParms);
	};

	function sortInstancesById(a, b) {
		return a._id.localeCompare(b._id);
	};

}