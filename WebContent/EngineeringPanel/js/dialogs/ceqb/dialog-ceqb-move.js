/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.ceqb.move = new DialogCeqbMove();

function DialogCeqbMove() {
	var ceqb = gEp.dlg.ceqb;

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbMove', 'initialise');
		//Nothing needed
	};

	this.moveableConcept = function(pConcept) {
		var m1 = new dojox.gfx.Moveable(pConcept.gfx_icon);
		dojo.connect(m1, 'onMoved', function(mover, shift) { moveConcept(mover, shift, pConcept); });
	};

	this.moveableRelationship = function(pRel) {
		//Make the group moveable as the automatic location algorithm is a bit weak at the moment
		new dojox.gfx.Moveable(pRel.dom_group);	
	};

	this.moveableFilter = function(pFilter) {
		var m1 = new dojox.gfx.Moveable(pFilter.gfx_icon);
		dojo.connect(m1, 'onMoved', function(mover, shift) { moveFilter(mover, shift, pFilter);});
	};

	this.moveableLinkedFilter = function(pLf) {
		//Make the group moveable as the automatic location algorithm is a bit weak at the moment
		new dojox.gfx.Moveable(pLf.dom_group);	
	};

	function moveConcept(pMover, pShift, pConcept) {
		moveConceptLabel(pShift, pConcept);
		moveConceptCountLabel(pShift, pConcept);
		moveConceptOutwardRels(pShift, pConcept);
		moveConceptInwardRels(pShift, pConcept);
		moveConceptFilters(pShift, pConcept);
	}

	function moveConceptLabel(pShift, pConcept) {
		var conceptLabel = pConcept.dom_label;
		dojo.style(conceptLabel, {
			position: 'absolute',
			left: parseInt(conceptLabel.style.left) + pShift.dx + 'px',
			top: parseInt(conceptLabel.style.top) + pShift.dy + 'px'
		});
	}

	function moveConceptCountLabel(pShift, pConcept) {
		var countLabel = pConcept.dom_countlabel;
		dojo.style(countLabel, {
			position: 'absolute',
			left: parseInt(countLabel.style.left) + pShift.dx + 'px',
			top: parseInt(countLabel.style.top) + pShift.dy + 'px'
		});
	}

	function moveConceptOutwardRels(pShift, pConcept) {
		moveConceptRelsFor(pConcept.outward_rels, pShift, pConcept, 'outward');
	}

	function moveConceptInwardRels(pShift, pConcept) {
		moveConceptRelsFor(pConcept.inward_rels, pShift, pConcept, 'inward');
	}

	function moveConceptRelsFor(pRels, pShift, pConcept, pInwardOrOutward) {
		for (var i = 0; i < pRels.length; i++) {
			var thisRel = pRels[i];
			
			moveConceptRelLine(pShift, thisRel, pConcept, pInwardOrOutward);
			moveConceptRelLabel(thisRel);
			moveConceptRelCountLabel(thisRel);
		}
	}

	function moveConceptRelLine(pShift, pRel, pConcept, pInwardOrOutward) {
		var relLine = pRel.gfx_line;
		var concept1 = null;
		var concept2 = null;
		
		if (pInwardOrOutward == 'inward') {
			concept1 = pRel.source_concept;
			concept2 = pRel.target_concept;
		} else {
			concept1 = pRel.target_concept;
			concept2 = pRel.source_concept;
		}
		var pos1 = dojo.position(concept1.gfx_icon.rawNode);
		var pos2 = dojo.position(concept2.gfx_icon.rawNode);
		var newCoords = ceqb.drawing.calculateRelLineEdgeCoords((pos1.x + pShift.dx), (pos1.y + pShift.dy), pos2.x, pos2.y);

		relLine.setShape({x1: newCoords.x1, y1: newCoords.y1, x2: newCoords.x2, y2: newCoords.y2});
		
		moveConceptRelLineArrowHead(pRel, newCoords, pInwardOrOutward);
	}

	function moveConceptRelLineArrowHead(pRel, pCoords, pInwardOrOutward) {
		var ahCoords = ceqb.drawing.calculateRelLineArrowHeadCoords(pCoords, pInwardOrOutward);

		var ahLine1 = pRel.gfx_line_ah1;
		ahLine1.setShape({x1: ahCoords.ah1x1, y1: ahCoords.ah1y1, x2: ahCoords.ah1x2, y2: ahCoords.ah1y2});
		
		var ahLine2 = pRel.gfx_line_ah2;
		ahLine2.setShape({x1: ahCoords.ah2x1, y1: ahCoords.ah2y1, x2: ahCoords.ah2x2, y2: ahCoords.ah2y2});
	}

	function moveConceptRelLabel(pRel) {
		var sh1 = pRel.gfx_line.getShape();
		var relLabel = pRel.gfx_label;
		var lw = dojo.position(relLabel.rawNode).w;
		var lh = dojo.position(relLabel.rawNode).h;
		//TODO: Need to improve these algorithms
		var newXPos = sh1.x1 + ((sh1.x2 - sh1.x1) / 2) - (lw / 2);
		var newYPos = sh1.y1 + ((sh1.y2 - sh1.y1) / 2) + (lh / 2);
		
		relLabel.setShape({x: newXPos, y: newYPos});
	}

	function moveConceptRelCountLabel(pRel) {
		var sh1 = pRel.gfx_line.getShape();
		var countLabel = pRel.gfx_countlabel;
		var lw = dojo.position(countLabel.rawNode).w;
		var lh = dojo.position(countLabel.rawNode).h;
		//TODO: Need to improve these algorithms
		var newXPos = sh1.x1 + ((sh1.x2 - sh1.x1) / 2) - (lw / 2) + 20;
		var newYPos = sh1.y1 + ((sh1.y2 - sh1.y1) / 2) + (lh / 2) - 20;
		
		countLabel.setShape({x: newXPos, y: newYPos});
	}

	function moveConceptFilters(pShift, pConcept) {
		for (var i = 0; i < pConcept.filters.length; i++) {
			var thisFilter = pConcept.filters[i];
			var pos1 = dojo.position(thisFilter.concept.gfx_icon.rawNode);
			var pos2 = dojo.position(thisFilter.gfx_icon.rawNode);
			var newCoords = ceqb.drawing.calculateFilterLineEdgeCoords((pos1.x + pShift.dx), pos1.y + (pShift.dy), pos2.x, pos2.y);
			var filtLine = thisFilter.gfx_line;
			filtLine.setShape({x1: newCoords.cx, y1: newCoords.cy, x2: newCoords.fx, y2: newCoords.fy});
		}
	}

	function moveFilter(pMover, pShift, pFilter) {
		moveFilterLabel(pShift, pFilter);
		moveFilterLine(pShift, pFilter);
		moveFilterLineLabel(pFilter);
		moveFilterCountLabel(pFilter);
		moveFilterLinkedFilters(pShift, pFilter);
	}

	function moveFilterLabel(pShift, pFilter) {
		var thisLabel = pFilter.dom_label;
		dojo.style(thisLabel, {
			position: 'absolute',
			left: parseInt(thisLabel.style.left) + pShift.dx + 'px',
			top: parseInt(thisLabel.style.top) + pShift.dy + 'px'
		});
	}

	function moveFilterLine(pShift, pFilter) {
		var pos1 = dojo.position(pFilter.concept.gfx_icon.rawNode);
		var pos2 = dojo.position(pFilter.gfx_icon.rawNode);
		var newCoords = ceqb.drawing.calculateFilterLineEdgeCoords((pos1.x + pShift.dx), (pos1.y + pShift.dy), pos2.x, pos2.y);

		var filtLine = pFilter.gfx_line;
		filtLine.setShape({x1: newCoords.cx, y1: newCoords.cy, x2: newCoords.fx, y2: newCoords.fy});			
	}

	function moveFilterLineLabel(pFilter) {
		var sh1 = pFilter.gfx_line.getShape();
		var filterLineLabel = pFilter.gfx_linelabel;
		var lw = dojo.position(filterLineLabel.rawNode).w;
		var lh = dojo.position(filterLineLabel.rawNode).h;
		//TODO: Need to improve these algorithms
		var newXPos = sh1.x1 + ((sh1.x2 - sh1.x1) / 2) - (lw / 2);
		var newYPos = sh1.y1 + ((sh1.y2 - sh1.y1) / 2) + (lh / 2);
		
		filterLineLabel.setShape({x: newXPos, y: newYPos});
	}

	function moveFilterCountLabel(pFilter) {
		var sh1 = pFilter.gfx_line.getShape();
		var countLabel = pFilter.gfx_countlabel;
		var lw = dojo.position(countLabel.rawNode).w;
		var lh = dojo.position(countLabel.rawNode).h;
		//TODO: Need to improve these algorithms
		var newXPos = sh1.x1 + ((sh1.x2 - sh1.x1) / 2) - (lw / 2) + 50;
		var newYPos = sh1.y1 + ((sh1.y2 - sh1.y1) / 2) + (lh / 2) - 20;
		countLabel.setShape({x: newXPos, y: newYPos});
	}

	function moveFilterLinkedFilters(pShift, pFilter) {
		for (var i = 0; i < pFilter.linked_filters.length; i++) {
			var thisLf = pFilter.linked_filters[i];
			moveFilterLinkedFilterLine(pShift, thisLf);
			moveFilterLinkedFilterLabel(thisLf);
		}
	}

	function moveFilterLinkedFilterLine(pShift, pLf) {
		var pos1 = dojo.position(pLf.source_filter.gfx_icon.rawNode);
		var pos2 = dojo.position(pLf.target_filter.gfx_icon.rawNode);
		var newCoords = ceqb.drawing.calculateLinkedFilterLineEdgeCoords((pos1.x + pShift.dx), pos1.y + (pShift.dy), pos2.x, pos2.y);
		var lfLine = pLf.gfx_line;
		lfLine.setShape({x1: newCoords.x1, y1: newCoords.y1, x2: newCoords.x2, y2: newCoords.y2});
	}

	function moveFilterLinkedFilterLabel(pLf) {
		var sh1 = pLf.gfx_line.getShape();
		var lfLabel = pLf.gfx_label;
		var lw = dojo.position(lfLabel.rawNode).w;
		var lh = dojo.position(lfLabel.rawNode).h;
		//TODO: Need to improve these algorithms
		var newXPos = sh1.x1 + ((sh1.x2 - sh1.x1) / 2) - (lw / 2);
		var newYPos = sh1.y1 + ((sh1.y2 - sh1.y1) / 2) + (lh / 2);
		
		lfLabel.setShape({x: newXPos, y: newYPos});
	}

}