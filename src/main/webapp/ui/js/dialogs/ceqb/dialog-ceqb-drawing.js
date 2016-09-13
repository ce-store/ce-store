/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.ceqb.drawing = new DialogCeqbDrawing();

function DialogCeqbDrawing() {
	var ceqb = gEp.dlg.ceqb;

	//  Drawing model
//		VisCon - the drawn representation of a concept
//		VisRel - the drawn representation of a relationship
//		VisFil - the drawn representation of a filter

	//  Miscellaneous prefixes
//		"dom" - indicates a variable that contains a dojo dom element
//		"gfx" - indicates a variable that contains a dojo GFX element
//		"pos" - indicates a variable that contains location (X, Y) information
//		"size" - indicates a variable that contains size (width, height, radius) information

	//Javascript structures
	//  Concept (on query canvas)
//		id
//		shape_id
//		concept_name
//		count
//		inward_rels
//		outward_rels
//		filters
//		gfx_icon
//		dom_label
//		dom_countlabel
	//
	//  Relationship (on query canvas)
//		id
//		shape_id
//		prop_name
//		prop_format
//		prop_domain
//		prop_range
//		source_concept
//		target_concept
//		gfx_group
//		gfx_line
//		gfx_label
//		gfx_countlabel
	//
	//  Filter (on query canvas)
//		id
//		shape_id
//		operator
//		prop_name
//		prop_format
//		value
//		concept
//		dom_label
//		gfx_icon
//		gfx_line
//		gfx_countlabel

	var COLOUR_CONCEPT_NORMAL = '#F5F5DC';				//beige
	var COLOUR_CONCEPT_EMPTY = '#D3D3D3';				//lightgrey
	var COLOUR_CONCEPT_SELECTED = '#4169E1';			//royalblue
	var COLOUR_CONCEPT_OUTLINE_PREMISE = 'red';
	var COLOUR_CONCEPT_OUTLINE_CONCLUSION = '#4169E1';	//royalblue
	var COLOUR_FILTER_SELECTED = '#4169E1';				//royalblue
	var COLOUR_FILTER_OUTLINE_PREMISE = 'red';
	var COLOUR_FILTER_OUTLINE_CONCLUSION = '#4169E1';	//royalblue
	var COLOUR_REL_LINE_PREMISE_NORMAL = 'red';
	var COLOUR_REL_LINE_PREMISE_EMPTY = '#D3D3D3';		//lightgrey
	var COLOUR_REL_LINE_CONCLUSION = '#4169E1';			//royalblue
	var COLOUR_LF_LINE_PREMISE_NORMAL = 'red';
	var COLOUR_LF_LINE_PREMISE_EMPTY = '#D3D3D3';		//lightgrey
	var COLOUR_LF_LINE_CONCLUSION = '#4169E1';			//royalblue
	var COLOUR_FILTER_NORMAL = '#F5F5DC';				//beige
	var COLOUR_FILTER_EMPTY = '#D3D3D3';				//lightgrey
//	var COLOUR_FILTER_LINE_NORMAL = 'red';
//	var COLOUR_FILTER_LINE_EMPTY = '#D3D3D3';			//lightgrey

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbDrawing', 'initialise');
		//Nothing needed
	};

	this.drawConceptIcon = function(pConcept, pPosX, pPosY) {
		var outlineColour = outlineColourForConcept(pConcept);
		var conceptIcon = ceqb.gfxSurface.createCircle({cx: pPosX, cy: pPosY, r: ceqb.sizeVisCon}).setStroke(outlineColour);

		conceptIcon.setFill(colourForConcept(pConcept));
		conceptIcon.rawNode.id = pConcept.shape_id;
		pConcept.gfx_icon = conceptIcon;
	};

	this.drawConceptLabel = function(pConcept, pPosX, pPosY) {
		//Create the label
		var conceptLabel = dojo.doc.createElement('div');
		conceptLabel.id = ceqb.model.calculateVisLabelIdFor(pConcept.shape_id);
		conceptLabel.innerHTML = ceqb.model.labelTextForConceptOnCanvas(pConcept);

		dojo.style(conceptLabel, {
			textAlign: 'center',
			verticalAlign: 'middle',
			position: 'absolute'
		});

		//First place the new text on the domContainer to calculate the offset (actual) width and height
		dojo.place(conceptLabel, ceqb.domContainer);

		var sizeHeight = conceptLabel.offsetHeight;
		var sizeWidth = conceptLabel.offsetWidth;

		var newPosX = ceqb.posMinorOffsetX + pPosX - (sizeWidth / 2);
		var newPosY = ceqb.posMinorOffsetY + pPosY - (sizeHeight / 2);

		//Now relocate the concept label element so that it is centralised correctly
		dojo.style(conceptLabel, {
			left: newPosX + 'px',
			top: newPosY + 'px'
		});

		//Store the label
		pConcept.dom_label = conceptLabel;
	};

	this.drawConceptCountLabel = function(pConcept, pPosX, pPosY) {
		//Create the count label
		var countLabel = dojo.doc.createElement('div');
		countLabel.id = ceqb.model.calculateVisCountIdFor(pConcept.shape_id);
		countLabel.innerHTML = '[0]';

		//Calculate the position
		var newPos = posConceptCountLabel(pPosX, pPosY);

		dojo.style(countLabel, {
			textAlign: 'left',
			verticalAlign: 'top',
			position: 'absolute',
			left: newPos.left + 'px',
			top: newPos.top + 'px'
		});

		//Place the label on the container
		dojo.place(countLabel, ceqb.domContainer);

		//Store the label
		pConcept.dom_countlabel = countLabel;
	};

	this.drawUpdatedConceptCount = function(pConcept) {
		pConcept.dom_countlabel.innerHTML = '[' + pConcept.count + ']';

		pConcept.gfx_icon.setFill(colourForConcept(pConcept));
		pConcept.gfx_icon.setStroke(outlineColourForConcept(pConcept));
	};

	this.drawSelectedConcept = function(pConcept) {
		//Store the concept as the currently selected source
		ceqb.srcConcept = pConcept;

		//Update the colour of the corresponding icon
		pConcept.gfx_icon.setFill(colourForConcept(pConcept));
	};

	this.drawUnselectedSourceConcept = function() {
		var oldSelConcept = ceqb.srcConcept;
		ceqb.srcConcept = null;

		if (!gCe.utils.isNullOrEmpty(oldSelConcept)) {
			//Call the function to change the concept colour
			oldSelConcept.gfx_icon.setFill(colourForConcept(oldSelConcept));
		}
	};

	this.drawRelationshipGroup = function(pRel) {
		//Create the group
		var newGroup = ceqb.gfxSurface.createGroup();
		newGroup.moveToBack();

		//Store the group
		pRel.dom_group = newGroup;
	};

	this.drawRelationshipLine = function(pRel, pEdgeCoords) {
		//Create the line
		var thisLine = ceqb.gfxSurface.createLine({x1: pEdgeCoords.x1, y1: pEdgeCoords.y1, x2: pEdgeCoords.x2, y2: pEdgeCoords.y2}).setStroke(colourForRel(pRel));
		thisLine.moveToBack();

		//Store the line
		pRel.gfx_line = thisLine;

		//Create the arrow head
		this.drawRelationshipLineArrowHead(pRel, pEdgeCoords);
	};

	this.drawRelationshipLabel = function(pRel, pCoords) {
		//On the next lines -50 (x) and -10 (y) are currently hardcoded estimates of half the length and height of a typical label
		var xPos = pCoords.x1 + ((pCoords.x2 - pCoords.x1) / 2) - 50;
		var yPos = pCoords.y1 + ((pCoords.y2 - pCoords.y1) / 2) - 10;

		//Create the label
		var relLabel = pRel.dom_group.createText({x: xPos, y: yPos, align: 'start', width: 'auto', height: 'auto'});
		relLabel.setFont({ family:'Arial', size:'12pt', weight:'normal' });
		relLabel.setFill('black');
		relLabel.rawNode.id = ceqb.model.calculateVisLabelIdFor(pRel.shape_id);

		var labelText = ceqb.model.labelTextForRelationship(pRel);
		relLabel.rawNode.textContent = labelText;
		relLabel.getShape().text = labelText;

		//Store the label
		pRel.gfx_label = relLabel;
	};

	this.drawRelationshipCountLabel = function(pRel, pCoords) {
		//Calculate the position
		var newPos = posRelCountLabel(pCoords);

		//Create the label
		var countLabel = pRel.dom_group.createText({x: newPos.x, y: newPos.y, text: '[0]', align: 'start'});
		countLabel.setFont({ family:'Arial', size:'12pt', weight:'normal' });
		countLabel.setFill('black');
		countLabel.rawNode.id = ceqb.model.calculateVisCountIdFor(pRel.shape_id);

		//Store the label
		pRel.gfx_countlabel = countLabel;
	};

	this.drawUpdatedRelationshipLabel = function(pRel) {
		//Strange behavior!  I have to set both of these... one is used when static, and the other when dragging...
		var labelText = ceqb.model.labelTextForRelationship(pRel);
		pRel.gfx_label.rawNode.textContent = labelText;
		pRel.gfx_label.getShape().text = labelText;
	};

	this.drawUpdatedRelationshipCount = function(pRel) {
		//Strange behavior!  I have to set both of these... one is used when static, and the other when dragging...
		pRel.gfx_countlabel.rawNode.textContent = '[' + pRel.count + ']';
		pRel.gfx_countlabel.getShape().text = '[' + pRel.count + ']';

		//Update the line to reflect the count
		pRel.gfx_line.setStroke(colourForRel(pRel));
		pRel.gfx_line_ah1.setStroke(colourForRel(pRel));
		pRel.gfx_line_ah2.setStroke(colourForRel(pRel));
	};

	this.calculateInitialRelCoordsFor = function(pRel) {
		var pos1 = dojo.position(pRel.source_concept.gfx_icon.rawNode);
		var pos2 = dojo.position(pRel.target_concept.gfx_icon.rawNode);

		return this.calculateRelLineEdgeCoords(pos1.x, pos1.y, pos2.x, pos2.y);
	};

	this.calculateRelLineEdgeCoords = function(pX1, pY1, pX2, pY2) {
		var circCenX1 = (pX1 - ceqb.posMainOffsetX) + ceqb.sizeVisCon;
		var circCenY1 = (pY1 - ceqb.posMainOffsetY) + ceqb.sizeVisCon;

		var circCenX2 = (pX2 - ceqb.posMainOffsetX) + ceqb.sizeVisCon;
		var circCenY2 = (pY2 - ceqb.posMainOffsetY) + ceqb.sizeVisCon;

		var edgeCoords = calculateTrigDistances(circCenX1, circCenY1, circCenX2, circCenY2, ceqb.sizeVisCon);

		return edgeCoords;
	};

	this.calculateRelLineArrowHeadCoords = function(pCoords, pInwardOrOutward) {
		var result = {};
		var arrowAngle = 30;		//In degrees

		//Note: 1 degree = PI/180 radians
		var angleDelta1 = pCoords.angle + (arrowAngle * (Math.PI / 180));
		var angleDelta2 = pCoords.angle - (arrowAngle * (Math.PI / 180));

		var line1Coords = calculateTrigDistances(pCoords.x1, pCoords.y1, pCoords.x2, pCoords.y2, 10, angleDelta1);
		var line2Coords = calculateTrigDistances(pCoords.x1, pCoords.y1, pCoords.x2, pCoords.y2, 10, angleDelta2);

		if (pInwardOrOutward == 'inward') {
			result.ah1x1 = pCoords.x2;
			result.ah1y1 = pCoords.y2;
			result.ah1x2 = line1Coords.x2;
			result.ah1y2 = line1Coords.y2;
			result.ah1ad = angleDelta1;

			result.ah2x1 = pCoords.x2;
			result.ah2y1 = pCoords.y2;
			result.ah2x2 = line2Coords.x2;
			result.ah2y2 = line2Coords.y2;
			result.ah2ad = angleDelta2;
		} else {
			result.ah1x1 = pCoords.x1;
			result.ah1y1 = pCoords.y1;
			result.ah1x2 = line1Coords.x1;
			result.ah1y2 = line1Coords.y1;
			result.ah1ad = angleDelta1;

			result.ah2x1 = pCoords.x1;
			result.ah2y1 = pCoords.y1;
			result.ah2x2 = line2Coords.x1;
			result.ah2y2 = line2Coords.y1;
			result.ah2ad = angleDelta2;
		}

		return result;
	};

	this.drawFilterIcon = function(pFilter, pPosX, pPosY) {
		//Create the icon
		var gfxFilterIcon = ceqb.gfxSurface.createRect({x: pPosX - ceqb.sizeVisCon, y: pPosY, width: (ceqb.sizeVisCon * 2), height: ceqb.sizeVisCon}).setStroke(outlineColourForFilter(pFilter));
		gfxFilterIcon.setFill(colourForFilter(pFilter));
		gfxFilterIcon.rawNode.id = pFilter.shape_id;

		//Store the icon
		pFilter.gfx_icon = gfxFilterIcon;
	};

	this.drawFilterLabel = function(pFilter, pPosX, pPosY) {
		var targetText = ceqb.model.labelTextForFilterValue(pFilter);
		var domFilterLabel = dojo.doc.createElement('div');
		domFilterLabel.id = ceqb.model.calculateVisLabelIdFor(pFilter.shape_id);
		domFilterLabel.innerHTML = targetText;

		dojo.style(domFilterLabel, {
			textAlign: 'center',
			verticalAlign: 'middle',
			position: 'absolute'
		});

		//First place the new text on the domContainer (to calculate the offset width and height)
		dojo.place(domFilterLabel, ceqb.domContainer);

		var dh = domFilterLabel.offsetHeight;
		var dw = domFilterLabel.offsetWidth;

		//Now relocate the new text so that it is centralised correctly
		dojo.style(domFilterLabel, {
			left: pPosX - (dw / 2) + 'px',
			top: (ceqb.sizeVisCon / 2) + pPosY - (dh / 2) + 'px'
		});

		pFilter.dom_label = domFilterLabel;
	};

	this.drawFilterCountLabel = function(pFilter, pCoords) {
		//Calculate the position
		var newPos = posFilterCountLabel(pCoords);

		//Create the label
		var countLabel = ceqb.gfxSurface.createText({x: newPos.x, y: newPos.y, text: '[0]', align: 'start'});
		countLabel.setFont({ family:'Arial', size:'12pt', weight:'normal' });
		countLabel.setFill('black');
		countLabel.rawNode.id = ceqb.model.calculateVisCountIdFor(pFilter.shape_id);

		//Store the label
		pFilter.gfx_countlabel = countLabel;
	};

	this.drawFilterLine = function(pFilter) {
		var posSrc = dojo.position(pFilter.concept.gfx_icon.rawNode);
		var posTgt = dojo.position(pFilter.gfx_icon.rawNode);
		var lineCoords = this.calculateFilterLineEdgeCoords(posSrc.x, posSrc.y, posTgt.x, posTgt.y);
		var gfxLine = ceqb.gfxSurface.createLine({x1: lineCoords.cx, y1: lineCoords.cy, x2: lineCoords.fx, y2: lineCoords.fy}).setStroke(outlineColourForFilter(pFilter));

		gfxLine.moveToBack();
		pFilter.gfx_line = gfxLine;
	};

	this.drawFilterLineLabel = function(pFilter, pCoords) {
		//On the next lines -50 (x) and -10 (y) are currently hardcoded estimates of half the length and height of a typical label
		var xPos = pCoords.x1 + ((pCoords.x2 - pCoords.x1) / 2) - 50;
		var yPos = pCoords.y1 + ((pCoords.y2 - pCoords.y1) / 2) - 10;

		//Create the label
		var filterLabel = ceqb.gfxSurface.createText({x: xPos, y: yPos, align: 'start'});
		filterLabel.setFont({ family:'Arial', size:'12pt', weight:'normal' });
		filterLabel.setFill('black');
		filterLabel.rawNode.id = ceqb.model.calculateVisLineLabelIdFor(pFilter.shape_id);

		var labelText = ceqb.model.labelTextForRelationship(pFilter);
		filterLabel.rawNode.textContent = labelText;
		filterLabel.getShape().text = labelText;

		//Store the label
		pFilter.gfx_linelabel = filterLabel;
	};

	this.drawUpdatedFilterLabel = function(pFilter) {
		var labelText = ceqb.model.labelTextForFilterValue(pFilter);
		var lineLabelText = ceqb.model.labelTextForRelationship(pFilter);
		pFilter.dom_label.innerHTML = labelText;

		pFilter.gfx_linelabel.rawNode.textContent = lineLabelText;
		pFilter.gfx_linelabel.getShape().text = lineLabelText;
	};

	this.drawUpdatedFilterCount = function(pFilter) {
		//Strange behavior!  I have to set both of these... one is used when static, and the other when dragging...
		pFilter.gfx_countlabel.rawNode.textContent = '[' + pFilter.count + ']';
		pFilter.gfx_countlabel.getShape().text = '[' + pFilter.count + ']';

		//Update the line and icon to reflect the count
		pFilter.gfx_icon.setFill(colourForFilter(pFilter));
		pFilter.gfx_line.setStroke(outlineColourForFilter(pFilter));
	};

	this.drawSelectedFilter = function(pFilter) {
		//Store the filter as the currently selected source
		ceqb.srcFilter = pFilter;

		//Update the colour of the corresponding icon
		pFilter.gfx_icon.setFill(colourForFilter(pFilter));
	};

	this.drawUnselectedSourceFilter = function() {
		var oldSelFilter = ceqb.srcFilter;
		ceqb.srcFilter = null;

		if (!gCe.utils.isNullOrEmpty(oldSelFilter)) {
			//Call the function to change the filter colour
			oldSelFilter.gfx_icon.setFill(colourForFilter(oldSelFilter));
		}
	};

	this.calculateInitialFilterCoordsFor = function(pFilter) {
		var pos1 = dojo.position(pFilter.concept.gfx_icon.rawNode);
		var pos2 = dojo.position(pFilter.gfx_icon.rawNode);

		return this.calculateRelLineEdgeCoords(pos1.x, pos1.y, pos2.x, pos2.y);
	};

	this.calculateFilterLineEdgeCoords = function(pCx, pCy, pFx, pFy) {
		var circCenX = (pCx - ceqb.posMainOffsetX) + ceqb.sizeVisCon;
		var circCenY = (pCy - ceqb.posMainOffsetY) + ceqb.sizeVisCon;

		var filtEdgeX = pFx - ceqb.posMainOffsetX + ceqb.sizeVisCon;
		var filtEdgeY = pFy - ceqb.posMainOffsetY + (ceqb.sizeVisCon / 2);

		var lenX = filtEdgeX - circCenX;
		var lenY = filtEdgeY - circCenY;

		var theta2 = Math.atan2(lenY, lenX);
		var cosX = Math.cos(theta2);
		var sinY = Math.sin(theta2);

		var edgeCoords = {};

		edgeCoords.cx = circCenX + (ceqb.sizeVisCon * cosX);
		edgeCoords.cy = circCenY + (ceqb.sizeVisCon * sinY);
		edgeCoords.fx = filtEdgeX;
		edgeCoords.fy = filtEdgeY;

		return edgeCoords;
	};

	function posRelCountLabel(pCoords) {
		var result = {};

		//On the next lines -20 (x) and -10 (y) are currently hardcoded estimates of half the length and height of a typical label
		result.x = pCoords.x1 + ((pCoords.x2 - pCoords.x1) / 2) - 20;
		result.y = pCoords.y1 + ((pCoords.y2 - pCoords.y1) / 2) + 10;

		return result;
	}

	this.drawLinkedFilterGroup = function(pLf) {
		//Create the group
		var newGroup = ceqb.gfxSurface.createGroup();
		newGroup.moveToBack();

		//Store the group
		pLf.dom_group = newGroup;
	};

	this.drawLinkedFilterLine = function(pLf, pEdgeCoords) {
		//Create the line
		var thisLine = ceqb.gfxSurface.createLine({x1: pEdgeCoords.x1, y1: pEdgeCoords.y1, x2: pEdgeCoords.x2, y2: pEdgeCoords.y2}).setStroke(colourForLinkedFilter(pLf));
		thisLine.moveToBack();

		//Store the line
	 	pLf.gfx_line = thisLine;
	};

	this.drawLinkedFilterLabel = function(pLf, pCoords) {
		//On the next lines -50 (x) and -10 (y) are currently hardcoded estimates of half the length and height of a typical label
		var xPos = pCoords.x1 + ((pCoords.x2 - pCoords.x1) / 2) - 50;
		var yPos = pCoords.y1 + ((pCoords.y2 - pCoords.y1) / 2) - 10;

		//Create the label
		var lfLabel = pLf.dom_group.createText({x: xPos, y: yPos, align: 'start', width: 'auto', height: 'auto'});
		lfLabel.setFont({ family:'Arial', size:'12pt', weight:'normal' });
		lfLabel.setFill('black');
		lfLabel.rawNode.id = ceqb.model.calculateVisLabelIdFor(pLf.shape_id);

		var labelText = ceqb.model.labelTextForLinkedFilter(pLf);
		lfLabel.rawNode.textContent = labelText;
		lfLabel.getShape().text = labelText;

		//Store the label
		pLf.gfx_label = lfLabel;
	};

	function colourForLinkedFilter(pLf) {
		var colour = null;

		if (ceqb.model.isPremiseLinkedFilter(pLf)) {
			if (pLf.count <= 0) {
				colour = COLOUR_LF_LINE_PREMISE_EMPTY;
			} else {
				colour = COLOUR_LF_LINE_PREMISE_NORMAL;
			}
		} else {
			colour = COLOUR_LF_LINE_CONCLUSION;
		}

		return colour;
	}

	this.calculateInitialLinkedFilterCoordsFor = function(pLf) {
		var pos1 = dojo.position(pLf.source_filter.gfx_icon.rawNode);
		var pos2 = dojo.position(pLf.target_filter.gfx_icon.rawNode);

		return this.calculateLinkedFilterLineEdgeCoords(pos1.x, (pos1.y - (ceqb.sizeVisCon / 2)), pos2.x, (pos2.y - (ceqb.sizeVisCon / 2)));
	};

	this.calculateLinkedFilterLineEdgeCoords = function(pX1, pY1, pX2, pY2) {
		var edgeCoords = {};

		edgeCoords.x1 = (pX1 - ceqb.posMainOffsetX) + ceqb.sizeVisCon;
		edgeCoords.y1 = (pY1 - ceqb.posMainOffsetY) + (ceqb.sizeVisCon / 2);
		edgeCoords.x2 = (pX2 - ceqb.posMainOffsetX) + ceqb.sizeVisCon;
		edgeCoords.y2 = (pY2 - ceqb.posMainOffsetY) + (ceqb.sizeVisCon / 2);

		return edgeCoords;
	};

	this.calculatePosOffsets = function() {
		var canvPosX = dojo.position(ceqb.domContainer).x;
		var canvPosY = dojo.position(ceqb.domContainer).y;

		if (canvPosX + canvPosY > 0) {
			var deltaX = ceqb.posMainOffsetX - (canvPosX + ceqb.posMinorOffsetX);
			var deltaY = ceqb.posMainOffsetY - (canvPosY + ceqb.posMinorOffsetY);

			ceqb.posMainOffsetX = canvPosX + ceqb.posMinorOffsetX;
			ceqb.posMainOffsetY = canvPosY + ceqb.posMinorOffsetY;

			//Alter the current mouse pos by the delta
			ceqb.posCurrentX = ceqb.posCurrentX + deltaX;
			ceqb.posCurrentY = ceqb.posCurrentY + deltaY;
		}
	};

	this.drawRelationshipLineArrowHead = function(pRel, pCoords) {
		var ahCoords = this.calculateRelLineArrowHeadCoords(pCoords, 'inward');

		var line1 = ceqb.gfxSurface.createLine({x1: ahCoords.ah1x1, y1: ahCoords.ah1y1, x2: ahCoords.ah1x2, y2: ahCoords.ah1y2}).setStroke(colourForRel(pRel));
		pRel.gfx_line_ah1 = line1;
		line1.moveToBack();

		var line2 = ceqb.gfxSurface.createLine({x1: ahCoords.ah2x1, y1: ahCoords.ah2y1, x2: ahCoords.ah2x2, y2: ahCoords.ah2y2}).setStroke(colourForRel(pRel));
		pRel.gfx_line_ah2 = line2;
		line2.moveToBack();
	};

	function colourForConcept(pConcept) {
		var colour = null;

		if (pConcept == ceqb.srcConcept) {
			colour = COLOUR_CONCEPT_SELECTED;
		} else {
			if (parseInt(pConcept.count) == 0) {
				colour = COLOUR_CONCEPT_EMPTY;
			} else {
				colour = COLOUR_CONCEPT_NORMAL;
			}
		}

		return colour;
	}

	function outlineColourForConcept(pConcept) {
		var colour = null;

		if (ceqb.model.isPremiseConcept(pConcept)) {
			colour = COLOUR_CONCEPT_OUTLINE_PREMISE;
		} else {
			colour = COLOUR_CONCEPT_OUTLINE_CONCLUSION;
		}

		return colour;
	}

	function posConceptCountLabel(pPosX, pPosY) {
		var result = {};

		result.left = pPosX + ceqb.sizeVisCon;
		result.top = pPosY - ceqb.sizeVisCon;

		return result;
	}

	function colourForRel(pRel) {
		var colour = '';

		if (ceqb.model.isPremiseRel(pRel)) {
			if (pRel.count <= 0) {
				colour = COLOUR_REL_LINE_PREMISE_EMPTY;
			} else {
				colour = COLOUR_REL_LINE_PREMISE_NORMAL;
			}
		} else {
			colour = COLOUR_REL_LINE_CONCLUSION;
		}

		return colour;
	}

	function colourForFilter(pFilter) {
		var colour = null;

		if (pFilter == ceqb.srcFilter) {
			colour = COLOUR_FILTER_SELECTED;
		} else {
			if (pFilter.count <= 0) {
				colour = COLOUR_FILTER_EMPTY;
			} else {
				colour = COLOUR_FILTER_NORMAL;
			}
		}

		return colour;
	}

	function outlineColourForFilter(pFilter) {
		var colour = null;

		if (ceqb.model.isPremiseFilter(pFilter)) {
			colour = COLOUR_FILTER_OUTLINE_PREMISE;
		} else {
			colour = COLOUR_FILTER_OUTLINE_CONCLUSION;
		}

		return colour;
	}

	function posFilterCountLabel(pPosX, pPosY) {
		var result = {};

		result.x = pPosX;
		result.y = pPosY;

		return result;
	}

	function calculateTrigDistances(pX1, pY1, pX2, pY2, pWidth, pAngle) {
		var angle = null;

		if (pAngle != null) {
			//Use the specified angle
			angle = pAngle;
		} else {
			//Calculate the angle from the supplied coordinates
			var lenX = pX2 - pX1;
			var lenY = pY2 - pY1;
			angle = Math.atan2(lenY, lenX);
		}

		var cosX = Math.cos(angle);
		var sinY = Math.sin(angle);

		var edgeCoords = {};

		edgeCoords.x1 = pX1 + (pWidth * cosX);
		edgeCoords.y1 = pY1 + (pWidth * sinY);
		edgeCoords.x2 = pX2 - (pWidth * cosX);
		edgeCoords.y2 = pY2 - (pWidth * sinY);
		edgeCoords.angle = angle;

		return edgeCoords;
	}

}
