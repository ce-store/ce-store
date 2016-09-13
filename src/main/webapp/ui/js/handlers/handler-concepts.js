/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.concepts = new HandlerConcepts();

function HandlerConcepts() {
	var ren = null;		//This is set during initialise

	var nonZeroOnly = false;

	this.initialise = function() {
		gCe.msg.debug('HandlerConcepts', 'initialise');

		ren = gEp.renderer.concepts;
	};

	this.isNonZeroOnly = function() {
		return nonZeroOnly;
	};

	this.listAllConcepts = function(pCbf, pUserParms) {
		var cbf = null;

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.concepts.processAllConceptList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.concepts.listAll(gEp.stdHttpParms(), cbf, userParms);
	};

	this.getConceptDetails = function(pConName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { concept_name: pConName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.concepts.processConceptDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.getDetails(gEp.stdHttpParms(), cbf, pConName, userParms);
	};

	this.listObjectProperties = function(pDomainName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { domain_name: pDomainName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.concepts.processConceptList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.listObjectProperties(gEp.stdHttpParms(), cbf, pDomainName, userParms);
	};

	this.listObjectPropertiesWithRange = function(pDomainName, pRangeName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { domain_name: pDomainName, range_name: pRangeName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.concepts.processConceptList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.listObjectPropertiesWithRange(gEp.stdHttpParms(), cbf, pDomainName, pRangeName, userParms);
	};

	this.listDatatypeProperties = function(pDomainName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { domain_name: pDomainName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.concepts.processConceptList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.listDatatypeProperties(gEp.stdHttpParms(), cbf, pDomainName, userParms);
	};

	this.listPrimarySentencesForConcept = function(pConName, pFullDetail, pCbf, pUserParms) {
		var localUserParms = {con_name: pConName, context: 'primary', full_detail: pFullDetail };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.listPrimarySentencesFor(gEp.stdHttpParms(), cbf, pConName, userParms);
	};

	this.listSecondarySentencesForConcept = function(pConName, pFullDetail, pCbf, pUserParms) {
		var localUserParms = {con_name: pConName, context: 'secondary', full_detail: pFullDetail };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.listSecondarySentencesFor(gEp.stdHttpParms(), cbf, pConName, userParms);
	};

	this.listAllSentencesForConcept = function(pConName, pFullDetail, pCbf, pUserParms) {
		var localUserParms = {con_name: pConName, context: 'all', full_detail: pFullDetail };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.concepts.listAllSentencesFor(gEp.stdHttpParms(), cbf, pConName, userParms);
	};

	this.showAllConcepts = function() {
		this.listAllConcepts();
	};

	this.showZeroConcepts = function() {
		nonZeroOnly = true;
		this.listAllConcepts();
	};

	this.showNonZeroConcepts = function() {
		nonZeroOnly = false;
		this.listAllConcepts();
	};

	this.processAllConceptList = function(pResponse, pUserParms) {
		var conList = gCe.utils.getStructuredResponseFrom(pResponse);

		if (!gCe.utils.isNullOrEmpty(conList)) {
			filterSortAndRenderConcepts(conList, pUserParms);
		}

		//Since this was a request for all concepts they should also be saved
		gEp.storeConcepts(conList);
	};

	this.processConceptList = function(pResponse, pUserParms) {
		var conList = gCe.utils.getStructuredResponseFrom(pResponse);

		filterSortAndRenderConcepts(conList, pUserParms);
	};

	this.processShadowConceptList = function(pResponse, pUserParms) {
		var conList = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderShadowConceptList(conList, pUserParms);
	};

	function filterSortAndRenderConcepts(pConList, pUserParms) {
		var sortedCons = null;

		if (!gCe.utils.isNullOrEmpty(pConList)) {
			var filtConList = filterConceptList(pConList);
			sortedCons = filtConList.sort(gEp.sortById);

		} else {
			sortedCons = [];
		}

		ren.renderConceptList(sortedCons, pUserParms);
	};

	this.processConceptDetails = function(pResponse, pUserParms) {
		var thisCon = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderConceptDetails(thisCon, pUserParms);
	};

	function filterConceptList(pConList) {
		var result = null;

		result = filterZeroOrNonZeros(pConList);

		return result;
	}

	function getNamedConceptFrom(pConName, pConList) {
		var result = null;

		for (var key in pConList) {
			if (pConList[key]._id === pConName) {
				result = pConList[key];
				break;
			}
		}

		return result;
	}

	function filterZeroOrNonZeros(pConList) {
		var result = null;

		if (nonZeroOnly) {
			result = [];

			for (var key in pConList) {
				var thisCon = pConList[key];

				if (thisCon.instance_count > 0) {
					result.push(thisCon);
				}
			}
		} else {
			result = pConList;
		}

		return result;
	}

}
