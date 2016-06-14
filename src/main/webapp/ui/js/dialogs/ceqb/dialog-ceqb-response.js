/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.ceqb.response = new DialogCeqbResponse();

function DialogCeqbResponse() {
	var ceqb = gEp.dlg.ceqb;

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbResponse', 'initialise');
		//Nothing needed
	};

	this.updateConceptCount = function(pResponse, pUserParms) {
		var conceptCount = '';
		var targetConcept = ceqb.model.getConceptById(pUserParms.variableId);

		if (!gCe.utils.isNullOrEmpty(targetConcept)) {
			if (gCe.utils.errorsInResponse(pResponse)) {
				conceptCount = '-1';
			} else {
				conceptCount = gCe.utils.extractSingleValueFromCeQueryResponse(pResponse);
			}

			targetConcept.count = conceptCount;

			ceqb.drawing.drawUpdatedConceptCount(targetConcept);
		} else {
			gCe.msg.error('Concept \'' + pUserParms.variableId + '\' could not be found');
		}
	};

	this.updateRelationshipPropertiesList = function(pResponse, pRel) {
		ceqb.latestPropList = gCe.utils.getStructuredResponseFrom(pResponse);

		if ((!gCe.utils.isNullOrEmpty(ceqb.latestPropList)) && (ceqb.latestPropList.length > 0)) {
			if (ceqb.latestPropList.length > 1) {
				//More that one possible relationship - open the dialog
				ceqb.dialog.openDialogRelDetails(pRel.tgtRel);
			} else {
				//Exactly one possible relationship - just use it
				var thisProp = ceqb.latestPropList[0];
				if (!gCe.utils.isNullOrEmpty(thisProp)) {
					ceqb.render.createTheRelationship(thisProp);
				} else {
					gCe.msg.error('Unexpected error selecting single property');
				}
			}
		} else {
			//No valid relationships - report and error
			gCe.msg.error('There are no possible properties between the two selected concepts');
			ceqb.actions.clearSourceAndTargetConcepts();
		}
	};

	this.updateRelationshipCount = function(pResponse, pRelId) {
		var relCount = '';
		var targetRel = ceqb.model.getRelationshipById(pRelId.tgtRel.variableId);

		if (gCe.utils.errorsInResponse(pResponse)) {
			relCount = '-1';
		} else {
			relCount = gCe.utils.extractSingleValueFromCeQueryResponse(pResponse);
		}

		targetRel.count = relCount;
		ceqb.drawing.drawUpdatedRelationshipCount(targetRel);
	};

	this.updateFilterPropertiesList = function(pResponse, pFilter) {
		ceqb.latestPropList = gCe.utils.getStructuredResponseFrom(pResponse);

		//The response has come back so the dialog can be opened
		ceqb.dialog.openDialogFilterDetails(pFilter.tgtFilter);
	};

	this.updateFilterCount = function(pResponse, pParms) {
		var filterCount = '';
		var targetFilter = ceqb.model.getFilterById(pParms.tgtFilter.variableId);

		if (gCe.utils.errorsInResponse(pResponse)) {
			filterCount = '-1';
		} else {
			filterCount = gCe.utils.extractSingleValueFromCeQueryResponse(pResponse);
		}

		targetFilter.count = filterCount;

		ceqb.drawing.drawUpdatedFilterCount(targetFilter);
	};

}