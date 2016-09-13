/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.rationale = new RendererRationale();

function RendererRationale() {

	this.initialise = function() {
		gCe.msg.debug('RendererRationale', 'initialise');
		//Nothing needed
	};

	this.renderAllRationaleList = function(pRatList) {
		gCe.msg.notYetImplemented('renderAllRationaleList', [pRatList]);
	};

	this.renderRationaleListForConcept = function(pRatList, pParms) {
		gCe.msg.notYetImplemented('renderRationaleListForConcept', [pRatList, pParms]);
	};

	this.renderRationaleListForInstance = function(pRatList, pParms) {
		gCe.msg.notYetImplemented('renderRationaleListForInstance', [pRatList, pParms]);
	};

	this.renderRationaleListForProperty = function(pRatList, pParms) {
		gCe.msg.notYetImplemented('renderRationaleListForProperty', [pRatList, pParms]);
	};

	this.renderRationaleListForPropertyValue = function(pRatList, pParms) {
		gCe.msg.notYetImplemented('renderRationaleListForPropertyValue', [pRatList, pParms]);
	};

}
