/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.network.actions = new DialogNetworkActions();

function DialogNetworkActions() {

	this.initialise = function() {
		gCe.msg.debug('DialogNetworkActions', 'initialise');
	};

	this.changedBackLinks = function() {
		var domBackLinks = dijit.byId('backLinks');

		gEp.dlg.network.setBackRefs(domBackLinks.get('value') === 'true');
		this.redraw();
	};

	this.changedCrossLinks = function() {
		var domCrossLinks = dijit.byId('crossLinks');

		gEp.dlg.network.setCrossLinks(domCrossLinks.get('value') === 'true');
		this.redraw();
	};

	this.redraw = function() {
		var domNumSteps = dijit.byId('numSteps');
		gEp.dlg.network.setNumSteps(domNumSteps.get('value'));

		gCe.msg.debug('Redrawing network');
		
		gEp.dlg.network.getInstanceDetailsForNetwork();
	};

	this.showDebugValues = function(pForce) {
		gEp.dlg.network.showDebugValues(pForce);
	};

}
