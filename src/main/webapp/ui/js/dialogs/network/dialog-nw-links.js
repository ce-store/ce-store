/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.network.links = new DialogNetworkLinks();

function DialogNetworkLinks() {

	this.initialise = function() {
		gCe.msg.debug('DialogNetworkLinks', 'initialise');
	};

	this.jsTextForChangedBackLinks = function() {
		return gEp.ui.links.jsTextFor('gEp.dlg.network.actions.changedBackLinks');
	};

	this.jsTextForChangedCrossLinks = function() {
		return gEp.ui.links.jsTextFor('gEp.dlg.network.actions.changedCrossLinks');
	};

	this.redraw = function() {
		return gEp.ui.links.hyperlinkFor(gEp.ui.links.jsTextFor('gEp.dlg.network.actions.redraw'), 'Redraw');
	};

	this.showDebugValues = function() {
		return gEp.ui.links.hyperlinkFor(gEp.ui.links.jsTextFor('gEp.dlg.network.actions.showDebugValues'), 'Show debug values');
	};

}
