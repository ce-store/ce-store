/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.rationale = new HandlerRationale();

function HandlerRationale() {
	var ren = null;		//This is set during initialise

	this.initialise = function() {
		gCe.msg.debug('HandlerRationale', 'initialise');

		ren = gEp.renderer.rationale;
	};

	this.listAllRationale = function(pCbf, pUserParms) {
		var cbf = null;

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.rationale.processAllRationale(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.listAllRationale(gEp.stdHttpParms(), cbf, userParms);
	};

	this.listRationaleForConcept = function(pConName, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { concept_name: pConName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.rationale.processConceptRationale(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.listRationaleForConcept(gEp.stdHttpParms(), cbf, pConName, userParms);
	};

	this.listRationaleForInstance = function(pInstName, pCbf, pUserParms) {
		var localUserParms = { instance_name: pInstName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.rationale.processInstanceRationale(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.listRationaleForInstance(gEp.stdHttpParms(), cbf, pInstName, userParms);
	};

	this.listRationaleForProperty = function(pInstName, pPropName, pCbf, pUserParms) {
		var localUserParms = { instance_name: pInstName, property_name: pPropName };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.rationale.processPropertyRationale(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.listRationaleForProperty(gEp.stdHttpParms(), cbf, pInstName, pPropName, userParms);
	};

	this.listRationaleForPropertyValue = function(pInstName, pPropName, pPropVal, pCbf, pUserParms) {
		var localUserParms = { instance_name: pInstName, property_name: pPropName, property_value: pPropVal };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.rationale.processPropertyValueRationale(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.listRationaleForPropertyValue(gEp.stdHttpParms(), cbf, pInstName, pPropName, pPropVal, userParms);
	};

	this.processAllRationale = function(pResponseObject, pUserParms) {
		var ratList = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderAllRationaleList(ratList, pUserParms);
	};

	this.processRationaleForConcept = function(pResponseObject, pUserParms) {
		var ratList = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderConceptRationaleList(ratList, pUserParms);
	};

	this.processRationaleForInstance = function(pResponseObject, pUserParms) {
		var ratList = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderInstanceRationaleList(ratList, pUserParms);
	};

	this.processRationaleForProperty = function(pResponseObject, pUserParms) {
		var ratList = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderPropertyRationaleList(ratList, pUserParms);
	};

	this.processRationaleForPropertyValue = function(pResponseObject, pUserParms) {
		var ratList = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderPropertyValueRationaleList(ratList, pUserParms);
	};

}
