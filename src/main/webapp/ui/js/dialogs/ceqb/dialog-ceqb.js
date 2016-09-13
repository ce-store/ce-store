/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/
gEp.dlg.ceqb = new DialogCeqb();

function DialogCeqb() {
	this.actions = null;		//These are all set when initialise is called
	this.dialog = null;
	this.drawing = null;
	this.generation = null;
	this.menu = null;
	this.model = null;
	this.move = null;
	this.render = null;
	this.response = null;

	this.GENMODE_NORMAL = 1;
	this.GENMODE_COUNT = 2;
	this.DEFAULT_CETEXT = '(CE will appear here)';

	this.VAL_FILTEROP_EQUALS = '=';
	this.VAL_FILTEROP_NONE = '(none)';
	this.VAL_TYPE_PREMISE = 'premise';
	this.VAL_TYPE_CONCLUSION = 'conclusion';

	this.latestVarId = 1;					//The highest variable id number used so far (incremented with each use)
	this.latestFiltId = 1;					//The highest filter id number used so far (incremented with each use)
	this.latestLinkId = 1;					//The highest link id number used so far (incremented with each use)
	this.latestShapeId = 1;					//The highest shape id number used so far (incremented with each use)

	this.isManualEditing = false;

	this.domContainer = null;				//The GFX gDomContainer
	this.gfxSurface = null;					//The GFX surface

	//The last (i.e. current) X and Y position of the mouse - 0,0 on browser
	this.posCurrentX = 0;
	this.posCurrentY = 0;

	//The offset X and Y of the query canvas from the 0,0 browser origin
	this.posMainOffsetX = 0;
	this.posMainOffsetY = 0;

	//The offset X and Y of the query canvas from the containing pane
	this.posMinorOffsetX = 6;
	this.posMinorOffsetY = 6;

	//Various size values for the canvas and concepts
	this.sizeVisCon = 50;

	this.ceQueryText = "";	//The CE Query text that corresponds to the drawn query
	this.queryName = "";	//The name of the current query/rule

	this.allConcepts = {};					//The collection of all query concepts
	this.allRelationships = {};				//The collection of all query relationships
	this.allFilters = {};					//The collection of all query filters
	this.allLinkedFilters = {};

	this.srcConcept = null;					//The selected source concept (or null if none selected)
	this.tgtConcept = null;					//The selected target concept (or null if none selected)
	this.srcFilter = null;					//The selected source filter (or null if none selected)
	this.tTgtFilter = null;					//The selected target filter (or null if none selected)
	this.currentConcept = null;				//A temporary field used for remembering the selected concept when opening dialog windows
	this.currentRel = null;					//A temporary field used for remembering the selected relationship when opening dialog windows
	this.currentFilter = null;				//A temporary field used for remembering the selected filter when opening dialog windows
	this.latestPropList = null;				//The list of properties most recently returned from the server
	this.ruleList = [];
	this.queryList = [];

	this.initialise = function() {
		gCe.msg.debug('DialogCeqb', 'initialise');

		this.actions.initialise();
		this.dialog.initialise();
		this.drawing.initialise();
		this.generation.initialise();
		this.links.initialise();
		this.menu.initialise();
		this.model.initialise();
		this.move.initialise();
		this.render.initialise();
		this.response.initialise();
	};

	this.initialiseCeqbEnvironment = function() {
		if (gCe.utils.isNullOrEmpty(this.gfxSurface)) {
			this.initialiseQueryCanvas();
			this.initialiseCeQueryTextPane();
		}
	};

	this.initialiseQueryCanvas = function() {
		//Register function on tab change
		var domTabContainer = dijit.byId('mainContainer');
		domTabContainer.watch('selectedChildWidget', function() { gEp.dlg.ceqb.drawing.calculatePosOffsets(); });

		//Connect the calculatePosOffsets function to the splitter resize (on mouse up)
//		var domSplitter = dijit.byId('outerContainer').getSplitter('left');
		var domSplitter = dijit.byId('outerContainer');
		dojo.connect(domSplitter.domNode, 'onmouseup', function() { gEp.dlg.ceqb.drawing.calculatePosOffsets();}); 

		//Create the GFX container and surface
		this.domContainer = dojo.byId('queryCanvas');
		this.gfxSurface = dojox.gfx.createSurface(this.domContainer, '100%', '100%');

		document.getElementById('queryCanvas').addEventListener('dragover', function(e) {
			doCeqbDragOverHandling(e);
		}, false);
		document.getElementById('queryCanvas').addEventListener('drop', function(e) {
			doCeqbDropHandling(e);
		}, false);

		//Register the drag over event to ensure that the current mouse coordinates are always being stored
		this.domContainer.ondragover = eventDragOver;
	};

	this.initialiseCeQueryTextPane = function() {
		//Register the double click event
		document.getElementById('ceQueryText').ondblclick = eventDoubleClickQueryText;
	};

	this.handleDroppedObject = function(pObj) {
		gCe.msg.debug(pObj, 'handleDroppedObject');

		if (pObj.type === 'concept') {
			this.render.renderConceptOnQueryCanvas(pObj);
		} else {
			gCe.msg.warning('Ignored dropped item', 'handleDroppedObject', [pObj]);
		}
	};

	function eventDragOver(evt) {
		//When the mouse moves simply store the X and Y coordinates for later use
		//(when dropping - to render the dropped item in the correct location)
		//Subtract the offset of the canvas to ensure that the X,Y has a 0,0 origin
		//at the left top corner of the canvas (not the browser page)
		gEp.dlg.ceqb.posCurrentX = evt.clientX - gEp.dlg.ceqb.posMainOffsetX;
		gEp.dlg.ceqb.posCurrentY = evt.clientY - gEp.dlg.ceqb.posMainOffsetY;
	};

	function eventDoubleClickQueryText(evt) {
		gEp.dlg.ceqb.dialog.openManualCeForm();
	};

	function doCeqbDragOverHandling(pE) {
		pE.preventDefault();
		pE.stopPropagation();
		pE.dataTransfer.dropEffect = 'copy';
	};

	function doCeqbDropHandling(pE) {
		pE.preventDefault();
		pE.stopPropagation();

		var rawTgtObj = pE.dataTransfer.getData(gCe.FORMAT_JSON);

		if (!gCe.utils.isNullOrEmpty(pE)) {
			//There is an object
			var tgtObj = JSON.parse(rawTgtObj);
			gEp.dlg.ceqb.handleDroppedObject(tgtObj);
		} else {
			gCe.msg.warning('Nothing detected in drop', 'doDropHandling', [pE]);
		}
	};

}
