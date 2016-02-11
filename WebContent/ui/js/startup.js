/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

var gCe = new CeStore(false);	//The flag indicates whether javascript debug mode is enabled
gCe.initialise();

var gEp = new EngineeringPanel();

if (typeof dojo !== 'undefined') {
	var extraLibs = {
		dialog_extra_01: { url:'/ce-store/EngineeringPanel/js/dialogs/map/dialog-map.js', loaded: false },
		dialog_extra_02: { url:'/ce-store/EngineeringPanel/js/dialogs/map/dialog-map-actions.js', loaded: false },
		dialog_extra_03: { url:'/ce-store/EngineeringPanel/js/dialogs/map/dialog-map-links.js', loaded: false },
		dialog_extra_04: { url:'/ce-store/EngineeringPanel/js/dialogs/map/dialog-map-projections.js', loaded: false },

		dialog_extra_12: { url:'/ce-store/EngineeringPanel/js/dialogs/network/dialog-nw.js', loaded: false },
		dialog_extra_13: { url:'/ce-store/EngineeringPanel/js/dialogs/network/dialog-nw-actions.js', loaded: false },
		dialog_extra_14: { url:'/ce-store/EngineeringPanel/js/dialogs/network/dialog-nw-links.js', loaded: false },

		ui_extra_02: { url:'/ce-store/EngineeringPanel/js/ui/panes/pane-map.js', loaded: false },
		ui_extra_03: { url:'/ce-store/EngineeringPanel/js/ui/panes/pane-network.js', loaded: false },
		ui_extra_04: { url:'/ce-store/EngineeringPanel/js/ui/panes/pane-currentprojects.js', loaded: false },

		ui_extra_05: { url:'/ce-store/EngineeringPanel/js/ui/forms/form-maplayers.js', loaded: false }
	};

	var extraActs = {
			store: [],
			model: [],
			source: [],
			instance: [
			    {
		        	filters: [ 'details' ],
					linkText: 'List references',
					hoverText: 'List all references to this instance',
					jsMethodName: 'gEp.handler.instances.listReferencesToInstance',
					jsParms: [ '{item}._id' ]
			    },
			    {
		        	filters: [ 'details' ],
					linkText: 'Show in network',
					hoverText: 'Render this instance in a dynamic network graph',
					jsMethodName: 'gEp.dlg.network.getInstanceDetailsForNetwork',
					jsParms: [ '{item}._id' ]
			    }
            ],
			concept: []
	};

	//Create the CE Store and load all the required JS files (DOJO and CE Store)
	gEp.initialise(extraLibs, extraActs);

	//Register callback for when the page is loaded
	dojo.addOnLoad(
		function() {
			//Everything is loaded so now start up the engineering panel
			gEp.startup();
		}
	);
} else {
	alert('Warning - the DOJO library cannot be located.  The CE Store cannot be used until this is resolved.');
}