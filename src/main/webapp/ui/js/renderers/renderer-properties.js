/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.properties = new RendererProperties();

function RendererProperties() {

	this.initialise = function() {
		gCe.msg.debug('RendererProperties', 'initialise');
		//Nothing needed
	};

	this.renderPropertyDetails = function(pProp) {
		gCe.msg.notYetImplemented('renderPropertyDetails', [pProp]);

		if (!gCe.utils.isNullOrEmpty(pProp)) {
			//TODO: Complete this
			var html = 'Property (not yet implemented) - ' + pProp._id;
			gEp.ui.pane.entity.updateWith(html, true);
		} else {
			gCe.msg.error('Unable to get property details');
		}
	};

}
