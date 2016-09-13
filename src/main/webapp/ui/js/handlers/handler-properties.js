/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.properties = new HandlerProperties();

function HandlerProperties() {
	var ren = null;		//This is set during initialise

	this.initialise = function() {
		gCe.msg.debug('HandlerProperties', 'initialise');

		ren = gEp.renderer.properties;
	};

	this.listAllProperties = function(pCbf, pUserParms) {
		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.properties.processPropertyList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.properties.listAll(gEp.stdHttpParms(), cbf, userParms);
	};

	this.listDatatypeProperties = function(pCbf, pUserParms) {
		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.properties.processPropertyList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.properties.listDatatype(gEp.stdHttpParms(), cbf, userParms);
	};

	this.listObjectProperties = function(pCbf, pUserParms) {
		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.properties.processPropertyList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, {});
		gCe.api.properties.listObject(gEp.stdHttpParms(), cbf, userParms);
	};

	this.getPropertyDetails = function(pPropName, pPropDomain, pPropRange, pCbf, pUserParms) {
		var cbf = null;
		var localUserParms = { property_name: pPropName, property_domain: pPropDomain, property_range: pPropRange };

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.properties.processPropertyDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.properties.getDetailsFor(gEp.stdHttpParms(), cbf, pPropName, pPropDomain, pPropRange, userParms);
	};

	this.processPropertyList = function(pResponseObject, pUserParms) {
		var propList = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderPropertyList(propList, pUserParms);
	};

	this.processPropertyDetails = function(pResponseObject, pUserParms) {
		var thisProp = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderPropertyDetails(thisProp, pUserParms);
	};

}
