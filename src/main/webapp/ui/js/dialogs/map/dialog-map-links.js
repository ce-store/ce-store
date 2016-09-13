/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.map.links = new DialogMapLinks();

function DialogMapLinks() {

	this.links = null;

	this.initialise = function() {
		gCe.msg.debug('DialogMapLinks', 'initialise');

		this.links = gEp.ui.links;
	};

	this.jsTextForRedrawMap = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.redrawMap');
	};

	this.redrawMap = function() {
		return this.links.hyperlinkFor(this.jsTextForRedrawMap(), 'Redraw the map');
	};

	this.jsTextForManageLayers = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.manageLayers');
	};

	this.manageLayers = function() {
		return this.links.hyperlinkFor(this.jsTextForManageLayers(), 'Manage layers');
	};

	this.jsTextForCalculateActiveBoundingBox = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.calculateActiveBoundingBox');
	};

	this.calculateActiveBoundingBox = function() {
		return this.links.hyperlinkFor(this.jsTextForCalculateActiveBoundingBox(), 'Calculate active bounding box');
	};

	this.jsTextForGenerateCeUsingLatLon = function(pLat, pLon) {
		return this.links.jsTextFor('gEp.dlg.map.actions.generateCeUsingLatLon', [ pLat, pLon ]);
	};

	this.generateCeUsingLatLon = function(pLat, pLon) {
		return this.links.hyperlinkFor(this.jsTextForGenerateCeUsingLatLon(pLat, pLon), 'use', 'Generate CE using lat lon');
	};

	this.jsTextForGenerateSpatialViewCe = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.generateSpatialViewCe');
	};

	this.generateSpatialViewCe = function() {
		return this.links.hyperlinkFor(this.jsTextForGenerateSpatialViewCe(), 'Generate spatial view CE');
	};

	this.jsTextForDebugMap = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.debugMap');
	};

	this.debugMap = function() {
		return this.links.hyperlinkFor(this.jsTextForDebugMap(), 'Debug map');
	};

	this.jsTextForListSpatialViews = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.listSpatialViews');
	};

	this.listSpatialViews = function() {
		return this.links.hyperlinkFor(this.jsTextForListSpatialViews(), 'List spatial views');
	};

	this.jsTextForDeleteLayer = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.deleteLayer');
	};

	this.deleteLayer = function() {
		return this.links.hyperlinkFor(this.jsTextForDeleteLayer(), 'Delete layer');
	};

	this.jsTextForZoomToLayer = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.zoomToLayer');
	};

	this.zoomToLayer = function() {
		return this.links.hyperlinkFor(this.jsTextForZoomToLayer(), 'Zoom to layer');
	};


	//jsText... only (these are triggered by events)
	this.jsTextForChangedAutoCalc = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.changedAutoCalc');
	};

	this.jsTextForResizeMap = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.resizeMap');
	};

	this.jsTextForChangedSelectedLayer = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.changedSelectedLayer');
	};

	this.jsTextForShowPoints = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.showPoints');
	};

	this.jsTextForShowHeatmap = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.showHeatmap');
	};

	this.jsTextForCloseMapLayersForm = function() {
		return this.links.jsTextFor('gEp.dlg.map.actions.closeMapLayersForm');
	};

}
