/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.map.projections = new DialogMapProjections();

function DialogMapProjections() {
	this.map = gEp.dlg.map;

	this.initialise = function() {
		gCe.msg.debug('DialogMapProjections', 'initialise');

		//Define the BNG projection - taken from http://spatialreference.org/ref/epsg/27700/proj4js/
		if (this.map.isMapConversionLibraryLoaded()) {
			proj4.defs['EPSG:27700'] = '+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +datum=OSGB36 +units=m +no_defs';
		} else {
			gCe.msg.warning('Unable to define extra map projections as the coordinate conversion library is not loaded (proj4)');
		}

		//Add further projection definitions here if you wish to support them
	};

}
