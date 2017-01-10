/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.map.actions = new DialogMapActions();

function DialogMapActions() {

	var DEFAULT_LAT = 54.406143;
	var DEFAULT_LON = -3.47168;
	var DEFAULT_ZOOM = 5;

	this.initialise = function() {
		gCe.msg.debug('DialogMapActions', 'initialise');
	};

	this.basemapClick = function(pEvent) {
		var position = gEp.dlg.map.map.getLonLatFromPixel(pEvent.xy).transform(gEp.dlg.map.getMapProjection(), gEp.dlg.map.getDefaultProjection());

		var domMapCoords = dojo.byId('mapCoords');
		var latVal = position.lat.toFixed(8);
		var lonVal = position.lon.toFixed(8);
		var msgText = 'Lat/lon: ' + latVal + ', ' + lonVal + ' [' + gEp.dlg.map.links.generateCeUsingLatLon(latVal, lonVal) + ']';

		if (!gCe.utils.isNullOrEmpty(domMapCoords)) {
			domMapCoords.innerHTML = msgText;
		} else {
			gCe.msg.alert(msgText);
		}
	};

	this.generateCeUsingLatLon = function(pLatVal, pLonVal) {
		if (gCe.utils.isTextSelectedInAddCeBox()) {
			gCe.handler.sentences.insertTextIntoAddCeField(pLatVal + ',' + pLonVal);
		} else {
			var ceText = 'there is a spatial thing named \'x\' that\n' +
				'  has \'' + pLatVal + '\' as latitude and\n' +
				'  has \'' + pLonVal + '\' as longitude.';

			gEp.handler.sentences.updateAddCeFieldWith(ceText);
		}
	};

	this.debugMap = function() {
		var midLat = calculateMidLat();
		var midLon = calculateMidLon();
		var calcZoom = gEp.dlg.map.calculateZoom();

		gCe.msg.debug(gEp.dlg.map.map, 'debugMap');
		gCe.msg.debug('Max lat = ' + gEp.dlg.map.maxLat, 'debugMap');
		gCe.msg.debug('Max lon = ' + gEp.dlg.map.maxLon, 'debugMap');
		gCe.msg.debug('Min lat = ' + gEp.dlg.map.minLat, 'debugMap');
		gCe.msg.debug('Min lon = ' + gEp.dlg.map.minLon, 'debugMap');
		gCe.msg.debug('Avg lat = ' + midLat, 'debugMap');
		gCe.msg.debug('Avg lon = ' + midLon, 'debugMap');
		gCe.msg.debug('Calc zoom = ' + calcZoom, 'debugMap');

		gCe.msg.debug('width=' + parseFloat(gEp.dlg.map.calculateDistanceInKm(gEp.dlg.map.minLat, midLon, gEp.dlg.map.maxLat, midLon)).toFixed(2) + 'km', 'debugMap');
		gCe.msg.debug('height=' + parseFloat(gEp.dlg.map.calculateDistanceInKm(midLat, gEp.dlg.map.minLon, midLat, gEp.dlg.map.maxLon)).toFixed(2) + 'km', 'debugMap');
		gCe.msg.debug('zoom = ' + gEp.dlg.map.map.zoom, 'debugMap');

		gCe.msg.debug(gEp.dlg.map.allMarkers, 'debugMap');
	};

	this.generateSpatialViewCe = function() {
		//Transform from map projection to default projection (to get 'normal' lat/lon value)
		var ctrLonLat = gEp.dlg.map.map.center.transform(gEp.dlg.map.getMapProjection(), gEp.dlg.map.getDefaultProjection());
		var zoomVal = gEp.dlg.map.map.zoom;
		var latVal = parseFloat(ctrLonLat.lat).toFixed(8);
		var lonVal = parseFloat(ctrLonLat.lon).toFixed(8);

		var ceText = 'there is a spatial view named \'x\' that\n  has \'' + latVal +
						'\' as latitude and\n  has \'' + lonVal +
						'\' as longitude and\n  has \'' + zoomVal +
						'\' as zoom factor.';

		gEp.handler.sentences.updateAddCeFieldWith(ceText);
	};

	this.redrawMap = function() {
		//Transform from default projection to map projection (to allow 'normal' lat/lon values to be specified)
		var lonLat = new OpenLayers.LonLat(DEFAULT_LON, DEFAULT_LAT).transform(gEp.dlg.map.getDefaultProjection(), gEp.dlg.map.getMapProjection());

		//TODO: An error is thrown when redrawing the map if heatmap layers have been deleted... for now just ignore it
		try {
			gEp.dlg.map.map.setCenter(lonLat, DEFAULT_ZOOM);
		} catch (e) {
			// TODO: handle exception
		}
	};

	this.manageLayers = function() {
		gEp.dlg.map.refreshLayerList();

		var formDlg = dijit.byId('formMapLayers');
		formDlg.show();
	};

	this.changedSelectedLayer = function(pThing) {
		var domLayerList = dijit.byId('dlgLayerList');
		var selName = domLayerList.get('value');

		if (!gCe.utils.isNullOrEmpty(selName)) {
			var selLayer = gEp.dlg.map.getSelectedMapLayer();
			configureFormFor(selLayer);
		}
	};

	this.showPoints = function() {
		var domOpt = dijit.byId('showPoints');
		var selVal = domOpt.get('checked');
		var selLayer = gEp.dlg.map.getSelectedMapLayer();

		if (!gCe.utils.isNullOrEmpty(selLayer)) {
			selLayer.pointsVisible = selVal;
			selLayer.olMarkers.setVisibility(selVal);
		}
	};

	this.showHeatmap = function() {
		var domOpt = dijit.byId('showHeatmap');
		var selVal = domOpt.get('checked');
		var selLayer = gEp.dlg.map.getSelectedMapLayer();

		if (!gCe.utils.isNullOrEmpty(selLayer)) {
			selLayer.heatmapVisible = selVal;

			if (selVal) {
				createHeatmapLayerFor(selLayer);
				selLayer.heatmapLayer.setVisibility(selVal);
			} else {
				//Only hide the heatmap layer if it has been created
				if (!gCe.utils.isNullOrEmpty(selLayer.heatmapLayer)) {
					selLayer.heatmapLayer.setVisibility(selVal);
					gCe.msg.alert('Due to a bug the heatmap layer cannot be made visible again.  Please refresh the map and recreate the heatmap if you wish to see it');
				}
			}
		}
	};

	this.deleteLayer = function() {
		var selLayer = gEp.dlg.map.getSelectedMapLayer();

		if (!gCe.utils.isNullOrEmpty(selLayer)) {
			var olMarkers = selLayer.olMarkers;
			var olLines = selLayer.olLines;

			//Remove any line layer
			if (!gCe.utils.isNullOrEmpty(olLines)) {
				gEp.dlg.map.map.removeLayer(olLines);
				olLines.destroy();
			}

			//Remove the marker layer
			gEp.dlg.map.map.removeLayer(olMarkers);
			olMarkers.destroy();
			if (!gCe.utils.isNullOrEmpty(selLayer.heatmapLayer)) {
				gEp.dlg.map.map.removeLayer(selLayer.heatmapLayer);
				//TODO: An error is thrown when destroying the heatmap layer... for now just catch it
				try {
					selLayer.heatmapLayer.destroy();
				} catch (e) {
					// TODO: handle exception
				}
			}

			delete gEp.dlg.map.allMarkers[selLayer.name];
			gEp.dlg.map.refreshLayerList();
		} else {
			gCe.msg.alert('No layer is selected');
		}
	};

	this.zoomToLayer = function() {
		var selLayer = gEp.dlg.map.getSelectedMapLayer();

		if (!gCe.utils.isNullOrEmpty(selLayer)) {
			gCe.msg.alert('Not yet implemented');
		} else {
			gCe.msg.alert('No layer is selected');
		}
	};

	this.clickedMarker = function(pClickEvent) {
		var instId = pClickEvent.currentTarget.title;

		if (!gCe.utils.isNullOrEmpty(instId)) {
			gEp.handler.instances.getInstanceDetails(instId);
		}
	};

	this.calculateActiveBoundingBox = function() {
		var midLat = calculateMidLat();
		var midLon = calculateMidLon();
		var calcZoom = gEp.dlg.map.calculateZoom();

		gEp.dlg.map.resizeMapTo(midLat, midLon, calcZoom);
	};

	this.changedAutoCalc = function() {
		var domAutoCalc = dijit.byId('autoCalc');
		var calcVal = domAutoCalc.get('value');

		iAutoCalc = (calcVal == 'true');
	};

	this.resizeMap = function() {
		var domSvList = dijit.byId('dlgSpatialViews');
		var selName = domSvList.get('value');
		var selSv = gEp.dlg.map.getSpatialView(selName);
		var thisLat = selSv.property_values.latitude;
		var thisLon = selSv.property_values.longitude;
		var thisZoom = selSv.property_values.zoom_factor;

		gEp.dlg.map.resizeMapTo(thisLat, thisLon, thisZoom);
	};

	this.listSpatialViews = function() {
		var cbf = function(pResponseObject, pUserParms) {gEp.dlg.map.processSpatialViewList(pResponseObject, pUserParms);};	
		gEp.handler.instances.listInstancesForConcept('spatial view', null, cbf, null);
	};

	this.closeMapLayersForm = function() {
		var form = dijit.byId('formMapLayers');
		form.hide();
	};

	function calculateMidLat() {
		return parseFloat(gEp.dlg.map.minLat) + parseFloat((gEp.dlg.map.maxLat - gEp.dlg.map.minLat) / 2);
	}

	function calculateMidLon() {
		return parseFloat(gEp.dlg.map.minLon) + parseFloat((gEp.dlg.map.maxLon - gEp.dlg.map.minLon) / 2);
	}

}
