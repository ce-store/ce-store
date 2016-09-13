/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.map = new DialogMap();

function DialogMap() {
	var HM_RADIUS = 40;
	var HM_OPACITY = 0.3;
	var HM_MAX = 5;

	var DEFAULT_PROJECTIONNAME = 'EPSG:4326';	// WGS84 (Plate Carree) - default lat/lon
	var BNG_PROJECTIONNAME = 'EPSG:27700';		// British National Grid
	var DEFAULT_MAPICON = './icons/marker.png';

	var MAPICON_WIDTH = 32;
	var MAPICON_HEIGHT = 37;

	var LINE_DISTANCE = 10;

	var PROP_LAT = 'latitude';
	var PROP_LON = 'longitude';
	var PROP_BEARANG = 'bearing angle';

	var PROP_EAST = 'easting';
	var PROP_NORTH = 'northing';
	var PROP_MGRS = 'mgrs raw';
//	var PROP_ZOOM = 'zoom factor';
	var PROP_DESC = 'description';
//	var PROP_CTRYCODE = 'country code';

	this.map = null;
	var iBaseLayer = null;
	var iSvList = [];
	this.allMarkers = {};
	this.maxLat = null;
	this.maxLon = null;
	this.minLat = null;
	this.minLon = null;
	var iAutoCalc = true;
	var iConversionError = false;
	var iMissingName = '';

	this.actions = null;
	this.links = null;
	this.projections = null;

	this.initialise = function() {
		gCe.msg.debug('DialogMap', 'initialise');

		this.actions.initialise();
		this.links.initialise();
		this.projections.initialise();
	};
	
	this.getVersionDetails = function() {
		var msgText = '';

		if (gEp.dlg.map.isOpenLayersLoaded()) {
			msgText += '\n\nOpenLayers = ' + OpenLayers.VERSION_NUMBER;
		}

		if (gEp.dlg.map.isMapConversionLibraryLoaded()) {
			msgText += '\n\nMap conversion = ' + Proj4js.scriptName;

			if (gEp.dlg.map.isMgrsProjectionLoaded()) {
				msgText += '\n(MGRS projection is loaded)';
			} else {
				msgText += '\n(MGRS projection NOT loaded)';
			}
		}

		if (gEp.dlg.map.isGoogleMapsLoaded()) {
			msgText += '\n\nGoogle maps = ' + google.maps.version;
		}

		return msgText;
	};

	this.isOpenLayersLoaded = function() {
		return (typeof OpenLayers !== 'undefined');	
	};

	this.isGoogleMapsLoaded = function() {
		return (typeof google !== 'undefined');
	};

	this.isMapConversionLibraryLoaded = function() {
		return (typeof proj4 !== 'undefined');
	};

	this.isMgrsProjectionLoaded = function() {
		var result = true;
		var preReq = this.isMapConversionLibraryLoaded();

		if (preReq) {
			result = (typeof Proj4js.Point.fromMGRS != 'undefined');
		} else {
			result = false;
		}

		return result;
	};

	this.isHeatmapLibraryLoaded = function() {
		var result = false;

		if (this.isOpenLayersLoaded()) {
			result = !gCe.utils.isNullOrEmpty(OpenLayers.Layer.Heatmap);
		}

		return result;
	};

	this.drawMapPane = function() {
		if (gCe.utils.isNullOrEmpty(gEp.dlg.map.map)) {
			drawMap();
			registerDropEvents();
		}
	};

	function drawMap() {
		gEp.dlg.map.actions.listSpatialViews();

		//Only create the map if it doesn't exist already (maybe a refresh)
		if (gCe.utils.isNullOrEmpty(gEp.dlg.map.map)) {
			if (gEp.dlg.map.isOpenLayersLoaded()) {
				//Create the map and add the base layer
				gEp.dlg.map.map = new OpenLayers.Map('mapDetails');		//Default projection is used
				iBaseLayer = new OpenLayers.Layer.OSM();
				gEp.dlg.map.map.addLayer(iBaseLayer);
//DSB 12/02/2016 - Temporarily removed as not compatible with latest OpenLayers
//Needs to be resurrected
//				gEp.dlg.map.map.addControl(new OpenLayers.Control.LayerSwitcher());
//				gEp.dlg.map.map.addControl(new OpenLayers.Control.MousePosition());

				if (gEp.dlg.map.isGoogleMapsLoaded()) {
					var layerGoogleSat = new OpenLayers.Layer.Google(
							'Google Satellite',
							{type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22}
							// used to be {type: G_SATELLITE_MAP, numZoomLevels: 22}
						);

					if (layerGoogleSat != null) {
						gEp.dlg.map.map.addLayer(layerGoogleSat);
					}

					var layerGoogleRoad = new OpenLayers.Layer.Google(
							'Google Road Map',
							{type: google.maps.MapTypeId.ROADMAP, numZoomLevels: 22}
						);

					if (layerGoogleRoad != null) {
						gEp.dlg.map.map.addLayer(layerGoogleRoad);
					}

					var layerGoogleHyb = new OpenLayers.Layer.Google(
							'Google Hybrid',
							{type: google.maps.MapTypeId.HYBRID, numZoomLevels: 22}
							// used to be {type: G_HYBRID_MAP, numZoomLevels: 22}
						);

					if (layerGoogleHyb != null) {
						gEp.dlg.map.map.addLayer(layerGoogleHyb);
					}
				}

				registerMapEvents();

				this.allMarkers = {};

				gEp.dlg.map.actions.redrawMap();
			} else {
				var domMapPanel = dojo.byId('mapPane');
				//TODO: Would be nice to have the failed URL to show here
				domMapPanel.innerHTML = 'The map cannot be displayed because the OpenLayers javascript library cannot be located.<BR/><BR/>Please check that the URL to load this library is correctly specified (look for "OpenLayers" in index.html)<BR/><BR/>OpenLayers can be downloaded from <a href="http://openlayers.org" target="_blank">here</a>, or can be referenced remotely via http://www.openlayers.org/api/OpenLayers.js<BR/><BR/>You will need to refresh this page to show the map once you have fixed the problem';
			}

		}
	}
	
	function registerDropEvents() {
		var domMapDetails = document.getElementById('mapDetails');

		if (domMapDetails) {
			domMapDetails.addEventListener('dragover', function(e) {
				doMapDragOverHandling(e);
			}, false);
			domMapDetails.addEventListener('drop', function(e) {
				doMapDropHandling(e);
			}, false);
		}
	}

	function doMapDragOverHandling(pE) {
		pE.preventDefault();
		pE.stopPropagation();
		pE.dataTransfer.dropEffect = 'copy';
	};

	function doMapDropHandling(pE) {
		pE.preventDefault();
		pE.stopPropagation();

		var rawTgtObj = pE.dataTransfer.getData(gCe.FORMAT_JSON);

		if (!gCe.utils.isNullOrEmpty(pE)) {
			//There is an object
			var tgtObj = JSON.parse(rawTgtObj);
			gEp.dlg.map.handleDroppedObject(tgtObj);
		} else {
			gCe.msg.warning('Nothing detected in drop', 'doDropHandling', [pE]);
		}
	};

	this.handleDroppedObject = function(pObj) {
		gCe.msg.debug(pObj, 'handleDroppedObject');

		if (pObj.type === 'concept') {
			this.renderConceptOnMap(pObj.conceptName);
		} else {
			gCe.msg.warning('Ignored dropped item', 'handleDroppedObject', [pObj]);
		}
	};

	function registerMapEvents() {
		gEp.dlg.map.map.events.register('click', gEp.dlg.map.map, function(e) { gEp.dlg.map.actions.basemapClick(e);});
	}

	this.refreshLayerList = function() {
		var domLayerList = dojo.byId('dlgLayerList');

		dojo.empty(domLayerList);

		//MultiSelect is not data-aware so I cannot use a store to populate it
		if (!gCe.utils.isNullOrEmpty(this.allMarkers)) {
			for (var key in this.allMarkers) {
				var thisLayer = this.allMarkers[key];

				var newOpt = dojo.doc.createElement('option');
				newOpt.innerHTML = thisLayer.name;
				newOpt.value = thisLayer.name;
				domLayerList.appendChild(newOpt);
			}
		}

		var dijLayerList = dijit.byId('dlgLayerList');
		dijLayerList.set('value', []);

		configureFormFor(null);
	};

	this.getSelectedMapLayer = function() {
		var domLayerList = dijit.byId('dlgLayerList');
		var selName = domLayerList.get('value');
		var result = null;

		if (!gCe.utils.isNullOrEmpty(selName)) {
			result = this.allMarkers[selName];
		}

		return result;
	};

	function configureFormFor(pLayer) {
		var domOptPoints = dijit.byId('showPoints');
		var domOptHeatmap = dijit.byId('showHeatmap');

		if (!gCe.utils.isNullOrEmpty(pLayer)) {
			//A layer is selected
			domOptPoints.attr('disabled', false);
			domOptHeatmap.attr('disabled', false);

			domOptPoints.attr('checked', pLayer.pointsVisible);
			domOptHeatmap.attr('checked', pLayer.heatmapVisible);
		} else {
			//No layer is selected
			domOptPoints.attr('disabled', true);
			domOptHeatmap.attr('disabled', true);
			domOptPoints.attr('checked', false);
			domOptHeatmap.attr('checked', false);
		}
	}

	this.resizeMapTo = function(pLat, pLon, pZoom) {
		//Transform from default projection to map projection (to allow 'normal' lat/lon values to be used)
		var lonLat = new OpenLayers.LonLat(pLon, pLat).transform(gEp.dlg.map.getDefaultProjection(), gEp.dlg.map.getMapProjection());
		gEp.dlg.map.map.setCenter(lonLat, pZoom);	
	};

	this.calculateZoom = function() {
		//Transform from default projection to map projection (to allow 'normal' lat/lon values to be used)
		var bounds = new OpenLayers.Bounds(gEp.dlg.map.minLon, gEp.dlg.map.minLat, gEp.dlg.map.maxLon, gEp.dlg.map.maxLat).transform(gEp.dlg.map.getDefaultProjection(), gEp.dlg.map.getMapProjection());

		return gEp.dlg.map.map.getZoomForExtent(bounds);
	};

	this.renderConceptOnMap = function(pConName) {
		if (gCe.utils.isNullOrEmpty(this.allMarkers[pConName])) {
			listAllDetailsForMap(pConName);
		} else {
			gCe.msg.alert('This layer already exists on the map.  if you wish to refresh it simply delete (via manage layers) and then recreate it');
		}
	};

	function listAllDetailsForMap(pConceptName) {
		var cbf = function(pResponseObject, pUserParms) { gEp.dlg.map.renderInstancesOnMap(pResponseObject, pUserParms);};	
		gEp.handler.instances.listInstancesForConcept(pConceptName, null, cbf, null);
	}

	this.renderInstancesOnMap = function(pResponse, pConceptName) {
		gCe.msg.debug(pResponse, 'renderInstancesOnMap', [ pConceptName ]);
		var latLonPairs = this.extractLatLonsFromResponse(pResponse);

		this.renderLatLonsOnMap(latLonPairs, pConceptName);
	};

	this.showSingleInstanceOnMap = function(pResponse, pInstName) {
		activateTab(PANE_GEO, CONTAINER_MAIN);

		var thisInst = processInstanceDetails(pResponse);

		this.renderSingleInstanceOnMap(thisInst);
	};

	this.renderSingleInstanceOnMap = function(pInst) {
		var latLonPairs = this.extractLatLonsFromInstance(pInst);

		this.renderLatLonsOnMap(latLonPairs, pInst._id);	
	};

	this.renderLatLonsOnMap = function(pLatLonPairs, pLayerTitle) {
		var addedMarker = false;
		var ceMarker = {};
		var markerLayer = new OpenLayers.Layer.Markers(pLayerTitle);
		var lineLayer = new OpenLayers.Layer.Vector('Line_' + pLayerTitle);

		ceMarker.name = pLayerTitle;
		ceMarker.olMarkers = markerLayer;
		ceMarker.olLines = lineLayer;
		ceMarker.latLons = [];
		ceMarker.pointsVisible = true;
		ceMarker.heatmapVisible = false;
		this.allMarkers[pLayerTitle] = ceMarker;

		iConversionError = false;
		iMissingName = '';

		for (var i = 0; i < pLatLonPairs.length; i++) {
			var thisLatLon = pLatLonPairs[i];

			//Transform from current projection to map projection (to allow whatever input lat/lon values to be used)
			var lonLat = new OpenLayers.LonLat(thisLatLon.lonVal, thisLatLon.latVal).transform(getProjectionFor(thisLatLon.projectionName), gEp.dlg.map.getMapProjection());
			updateExtentExtremes(lonLat);
			ceMarker.latLons.push(thisLatLon);

			var destLatLon = calculateDestinationLatLon(thisLatLon, lonLat);
			var size = new OpenLayers.Size(MAPICON_WIDTH, MAPICON_HEIGHT);
			var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);

			var iconName = getIconNameFromGeo(thisLatLon);
			var thisIcon = new OpenLayers.Icon(iconName, size, offset);
			thisIcon.imageDiv.title = thisLatLon._id;
			//Transform to map projection (for plotting on map) - not sure why this is needed!
			var mapLonLat = lonLat.transform(gEp.dlg.map.getDefaultProjection(), gEp.dlg.map.getMapProjection());

			//Render a single marker or a marker plus line (if a bearing / destination is specified)
			var thisMarker = new OpenLayers.Marker(mapLonLat, thisIcon);
			thisMarker.events.register('click', thisMarker, function(pClickEvent) {gEp.dlg.map.actions.clickedMarker(pClickEvent);});

			markerLayer.addMarker(thisMarker);
			addedMarker = true;

			if (!gCe.utils.isNullOrEmpty(destLatLon)) {
				var points = new Array(
					new OpenLayers.Geometry.Point(thisLatLon.lonVal, thisLatLon.latVal).transform(gEp.dlg.map.getDefaultProjection(), gEp.dlg.map.getMapProjection()),
					new OpenLayers.Geometry.Point(destLatLon.lonVal, destLatLon.latVal).transform(gEp.dlg.map.getDefaultProjection(), gEp.dlg.map.getMapProjection())
				);

				var line = new OpenLayers.Geometry.LineString(points);

				var style = { 
					strokeColor: '#0000ff', 
					strokeOpacity: 0.5,
					strokeWidth: 5
				};

				var lineFeature = new OpenLayers.Feature.Vector(line, null, style);
				lineLayer.addFeatures([lineFeature]);
			}
		}

		if (addedMarker) {
			gEp.dlg.map.map.addLayer(lineLayer);
			gEp.dlg.map.map.addLayer(markerLayer);
			gEp.dlg.map.map.addControl(new OpenLayers.Control.DrawFeature(lineLayer, OpenLayers.Handler.Path));

			if (iAutoCalc) {
				gEp.dlg.map.actions.calculateActiveBoundingBox();
			}
		}

		if (iConversionError) {
			gCe.msg.error('The locations could not be rendered because a conversion library was not available for the location information (' + iMissingName + ')');
		}
	};

	function calculateDestinationLatLon(pSrcLatLon, pLonLat) {
		var destResult = null;

		if (!gCe.utils.isNullOrEmpty(pSrcLatLon.bearingVal)) {
			var destLonLat = OpenLayers.Util.destinationVincenty(pLonLat, pSrcLatLon.bearingVal, LINE_DISTANCE);

			destResult = {};
			destResult.sourceLatLon = pSrcLatLon;
			destResult.name = pSrcLatLon._d + ' destination';
			destResult.projectionName = pSrcLatLon.projectionName;
			destResult.latVal = destLonLat.lat;
			destResult.lonVal = destLonLat.lon;
		}

		return destResult;
	}

	function updateExtentExtremes(pLonLat) {
		//Transform from map projection to default projection (to get 'normal' lat/lon value)
		var trLonLat = pLonLat.transform(gEp.dlg.map.getMapProjection(), gEp.dlg.map.getDefaultProjection());

		var thisLat = trLonLat.lat;
		var thisLon = trLonLat.lon;

		if ((gEp.dlg.map.maxLat == null) || (thisLat > gEp.dlg.map.maxLat)) {
			gEp.dlg.map.maxLat = thisLat;
		}
		if ((gEp.dlg.map.minLat == null) || (thisLat < gEp.dlg.map.minLat)) {
			gEp.dlg.map.minLat = thisLat;
		}
		if ((gEp.dlg.map.maxLon == null) || (thisLon > gEp.dlg.map.maxLon)) {
			gEp.dlg.map.maxLon = thisLon;
		}
		if ((gEp.dlg.map.minLon == null) || (thisLon < gEp.dlg.map.minLon)) {
			gEp.dlg.map.minLon = thisLon;
		}
	}

	this.extractLatLonsFromResponse = function(pResponse) {
//		var hdrs = processFullInstanceListHeaders(pResponse);
//		var instList = processFullInstanceListRows(pResponse, hdrs);

		var instList = gCe.utils.getStructuredResponseFrom(pResponse);

		return this.extractLatLonsFromInstances(instList);
	};

	this.extractLatLonsFromInstance = function(pInst) {
		var instList = [];
		instList.push(pInst);

		return extractLatLonsFromInstances(instList);
	};

	this.extractLatLonsFromInstances = function(pInstList) {
		var result = [];

		for (var iInst = 0; iInst < pInstList.length; iInst++) {
			var thisInst = pInstList[iInst];
			var resPair = null;

			//First try the standard coordinate properties (latitude and longitude)
			resPair = this.extractStandardCoordinatesFrom(thisInst);

			//If needed, try the BNG properties (easting and northing) 
			if (gCe.utils.isNullOrEmpty(resPair)) {
				resPair = this.extractBngCoordinatesFrom(thisInst);
			}

			//If needed, try the MGRS property (mgrs raw)
			if (gCe.utils.isNullOrEmpty(resPair)) {
				resPair = this.extractMgrsCoordinatesFrom(thisInst);
			}

			//Only add to the result if coordinate extraction was successful
			if (!gCe.utils.isNullOrEmpty(resPair)) {
				result.push(resPair);
			}
		}

		return result;
	};

	this.extractStandardCoordinatesFrom = function(pInst) {
		var locResult = null;
		var latPropVal = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_LAT);
		var lonPropVal = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_LON);
		var bearingVal = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_BEARANG);

		if (!gCe.utils.isNullOrEmpty(latPropVal) && !gCe.utils.isNullOrEmpty(lonPropVal)) {
			locResult = {};
			locResult.instance = pInst;
			locResult._id = pInst._id;
			locResult.description = getDescriptionFor(pInst);
			locResult.projectionName = DEFAULT_PROJECTIONNAME;
			locResult.latVal = latPropVal;
			locResult.lonVal = lonPropVal;
			locResult.bearingVal = bearingVal;
		}

		return locResult;
	};

	this.extractBngCoordinatesFrom = function(pInst) {
		var locResult = null;
		var northingPropVal = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_NORTH);
		var eastingPropVal = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_EAST);
		var bearingVal = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_BEARANG);

		if (!gCe.utils.isNullOrEmpty(northingPropVal) && !gCe.utils.isNullOrEmpty(eastingPropVal)) {
			if (this.isMapConversionLibraryLoaded()) {
				if (this.isProjectionLoaded(BNG_PROJECTIONNAME)) {
					locResult = {};
					locResult.instance = pInst;
					locResult._id = pInst._id;
					locResult.description = getDescriptionFor(pInst);
					locResult.projectionName = BNG_PROJECTIONNAME;
					locResult.latVal = northingPropVal;
					locResult.lonVal = eastingPropVal;
					locResult.bearingVal = bearingVal;
				} else {
					iMissingName = BNG_PROJECTIONNAME;
					iConversionError = true;
				}
			} else {
				iMissingName = 'Entire Proj4js library';
				iConversionError = true;
			}
		}

		return locResult;
	};

	this.extractMgrsCoordinatesFrom = function(pInst) {
		var locResult = null;
		var mgrsPropVal = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_MGRS);
		var bearingVal = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_BEARANG);

		if (!gCe.utils.isNullOrEmpty(mgrsPropVal)) {
			if (this.isMapConversionLibraryLoaded()) {
				if (this.isMgrsProjectionLoaded()) {
					var mgrsLatLon = calculateLatLonFromMgrs(mgrsPropVal);

					if (!gCe.utils.isNullOrEmpty(mgrsLatLon)) {
						locResult = {};
						locResult.instance = pInst;
						locResult._id = pInst._id;
						locResult.description = getDescriptionFor(pInst);
						locResult.projectionName = DEFAULT_PROJECTIONNAME;
						locResult.latVal = mgrsLatLon[0];
						locResult.lonVal = mgrsLatLon[1];
						locResult.mgrs = mgrsPropVal;
						locResult.bearingVal = bearingVal;
					}
				} else {
					iMissingName = 'MGRS';
					iConversionError = true;
				}
			} else {
				iMissingName = 'Entire Proj4js library';
				iConversionError = true;
			}
		}

		return locResult;
	};

	function getDescriptionFor(pInst) {
		var result = gCe.utils.getFirstOrSinglePropertyValueFor(pInst, PROP_DESC);

		//If there is no description use the name instead
		if (gCe.utils.isNullOrEmpty(result)) {
			result = pInst._id;
		}

		return result;
	}

	function calculateLatLonFromMgrs(pMgrsVal) {
		var result = [];
		var squashedVal = replaceAll(pMgrsVal, ' ', '');
		var calcMgrsPoint = Proj4js.Point.fromMGRS(squashedVal);
		var mgrsLat = calcMgrsPoint.x;
		var mgrsLon = calcMgrsPoint.y;

		result.push(mgrsLon);
		result.push(mgrsLat);

		return result;
	}

	function getIconNameFromGeo(pGeo) {
		var result = DEFAULT_MAPICON;
		
		if (!gCe.utils.isNullOrEmpty(pGeo)) {
			if (!gCe.utils.isNullOrEmpty(pGeo.instance)) {
				if (!gCe.utils.isNullOrEmpty(pGeo.instance.icon)) {
					//A map icon has been specified, so use it instead of the default
					result = pGeo.instance.icon;
				}
			}
		}

		return result;
	}

	this.getSpatialView = function(pName) {
		var result = null;

		for (var iSv = 0; iSv < iSvList.length; iSv++) {
			if (gCe.utils.isNullOrEmpty(result)) {
				var thisSv = iSvList[iSv];

				if (thisSv._id == pName) {
					result = thisSv;
				}
			}
		}

		return result;
	};

	this.processSpatialViewList = function(pResponse, pUserParms) {
		iSvList = [];

//		var hdrs = processFullInstanceListHeaders(pResponse);
//		var instList = processFullInstanceListRows(pResponse, hdrs);

		iSvList = gCe.utils.getStructuredResponseFrom(pResponse);

		populateSpatialViewList();
	};

	function populateSpatialViewList() {
		var domSvList = dijit.byId('dlgSpatialViews');

		var svData = {
				identifier: 'id',
				label: 'name',
				items: []
		};

		var svStore = new dojo.data.ItemFileWriteStore({  
			data: svData  
		});	

		if ((!gCe.utils.isNullOrEmpty(iSvList)) && (iSvList.length > 0)) {
			for (var i = 0; i < iSvList.length; i++){
				var thisSv = iSvList[i];

				svStore.newItem( {
					id: thisSv._id,
					name: thisSv._id,
					lat: thisSv.property_values.latitude,
					lon: thisSv.property_values.longitude,
					zoom: thisSv.property_values.zoom_factor } );
			}
		}

		domSvList.set('store', svStore);
	}

	this.calculateDistanceInKm = function(pLat1, pLon1, pLat2, pLon2) {
		//Calculate the distance (in km) between two lat/lon pairs using Haversine
		//Assumes the earth is spherical (but is accurate enough
		var mult = 6371; // km
		var deltaLat = degToRad(pLat2 - pLat1);
		var deltaLon = degToRad(pLon2 - pLon1); 
		var arc = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
				Math.cos(degToRad(pLat1)) * Math.cos(degToRad(pLat2)) * 
				Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2); 
		var circ = 2 * Math.atan2(Math.sqrt(arc), Math.sqrt(1 - arc)); 
		var dist = mult * circ;

		return dist;
	};

	function degToRad(pDegVal) {
		return pDegVal * Math.PI / 180;
	}

	this.getMapProjection = function() {
		return gEp.dlg.map.map.getProjectionObject();
	};

	function getProjectionFor(pProjName) {
		return new OpenLayers.Projection(pProjName);
	}

	this.getDefaultProjection = function() {
		return new OpenLayers.Projection(DEFAULT_PROJECTIONNAME);
	};

	this.isProjectionLoaded = function(pProjName) {
		var result = false;

		if (this.isMapConversionLibraryLoaded()) {
			result = !gCe.utils.isNullOrEmpty(Proj4js.defs[pProjName]);
		}

		return result;
	};

	function createHeatmapLayerFor(pLayer) {
		if (!gCe.utils.isNullOrEmpty(pLayer)) {
			if (isHeatmapLibraryLoaded()) {
				if (gCe.utils.isNullOrEmpty(pLayer.heatmapLayer)) {
					var mapData = [];

					for (var key in pLayer.latLons) {
						var thisLatLon = pLayer.latLons[key];
						mapData.push({
							lonlat: new OpenLayers.LonLat(thisLatLon.lonVal, thisLatLon.latVal).transform(getProjectionFor(thisLatLon.projectionName), gEp.dlg.map.getDefaultProjection()),
							count: 1				//TODO: Currently this is always one, but I could compute it
						});
					}

					//TODO: HM_MAX should probably be dynamically generated
					var hmData = { max: HM_MAX, data: mapData };
				
					// Create the heatmap layer
					pLayer.heatmapLayer = new OpenLayers.Layer.Heatmap( 'Heatmap Layer', gEp.dlg.map.map, iBaseLayer, {visible: true, radius: calculateHmRadius()}, {isBaseLayer: false, opacity: HM_OPACITY, projection: gEp.dlg.map.getDefaultProjection()});
					pLayer.heatmapLayer.setDataSet(hmData);

//					gEp.dlg.map.map.addLayers([iBaseLayer, pLayer.heatmapLayer]);
					gEp.dlg.map.map.addLayer(pLayer.heatmapLayer);
				}
			} else {
				gCe.msg.alert('Heatmap cannot be created as the heatmap library is not loaded');
			}
		}
	}

	function calculateHmRadius() {
		var hmRadius = 0;

		//TODO: Make this dynamic
		hmRadius = HM_RADIUS;

		return hmRadius;
	}

}
