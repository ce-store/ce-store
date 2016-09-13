/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.ceqb.render = new DialogCeqbRender();

function DialogCeqbRender() {
	var ceqb = gEp.dlg.ceqb;

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbRender', 'initialise');
		//Nothing needed
	};

	this.renderConceptOnQueryCanvas = function(pDndCon) {
		if (pDndCon != null){
			var concept = {name: pDndCon.conceptName};
			addConceptToQueryCanvas(concept);
		}
	};

	this.updateCePaneWith = function(pCE, pManualInd) {
		var domCeTextPane = dojo.byId('ceQueryText');
		domCeTextPane.innerHTML = gCe.utils.htmlFormat(pCE);

		ceqb.ceQueryText = pCE;	

		//If this has been manually edited then set the colour accordingly
		if (pManualInd) {
			dojo.style('ceQueryText', 'backgroundColor', '#FF6347');
		} else {
			dojo.style('ceQueryText', 'backgroundColor', 'white');
		}
	};

	this.refreshQueryName = function() {
		if (!ceqb.isManualEditing) {
			ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
		} else {
			ceqb.actions.recalculateCeQueryNameOnly(ceqb.ueryName);
		}
	};

	this.clearCanvas = function() {
		ceqb.isManualEditing = false;

		for (var thisConceptKey in ceqb.allConcepts) {
			var thisConcept = ceqb.allConcepts[thisConceptKey];
			if (!gCe.utils.isNullOrEmpty(thisConcept)) {
				ceqb.actions.deleteConcept(thisConcept, true, false);
			} else {
				gCe.msg.warning('Could not find concept \'' + thisConceptKey + '\' for clearing from canvas', 'clearCanvas');
			}
		}

		//Empty the query name field
		var domQueryName = dijit.byId('ceqbQueryName');
		domQueryName.set('value', '');
		ceqb.queryName = '';

		//Reset the ID counters
		ceqb.latestVarId = 1;
		ceqb.latestFiltId = 1;
		ceqb.latestLinkId = 1;
		ceqb.latestShapeId = 1;

		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	function addConceptToQueryCanvas(pConceptPi) {
		ceqb.drawing.calculatePosOffsets();

		//Take a copy of the current X and Y pos (to keep it fixed in case the mouse moves during the rendering)
		var posX = ceqb.posCurrentX;
		var posY = ceqb.posCurrentY;

		//Create the new concept
		var newConcept = ceqb.model.createNewConcept(pConceptPi);

		ceqb.drawing.drawConceptLabel(newConcept, posX, posY);
		ceqb.drawing.drawConceptCountLabel(newConcept, posX, posY);
		ceqb.drawing.drawConceptIcon(newConcept, posX, posY);
		ceqb.move.moveableConcept(newConcept);
		ceqb.menu.menuConcept(newConcept);
		ceqb.menu.doubleclickConcept(newConcept);

		ceqb.actions.refreshConceptCount(newConcept);
		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);

		return newConcept;
	}

	function addConceptToQueryCanvasFromLoad(pVarId, pName, pPremOrConc, pIncInResults, pPosX, pPosY) {
		//Create the new concept
		var newConcept = ceqb.model.createNewConceptFromLoad(pVarId, pName, pIncInResults, pPremOrConc);

		ceqb.drawing.drawConceptLabel(newConcept, pPosX, pPosY);
		ceqb.drawing.drawConceptCountLabel(newConcept, pPosX, pPosY);
		ceqb.drawing.drawConceptIcon(newConcept, pPosX, pPosY);
		ceqb.move.moveableConcept(newConcept);
		ceqb.menu.menuConcept(newConcept);
		ceqb.menu.doubleclickConcept(newConcept);

		ceqb.actions.refreshConceptCount(newConcept);
		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);

		return newConcept;
	}

	this.createTheRelationship = function(pRel) {
		var chosenRel = {};
		var targetRel = null;

		chosenRel.propName = pRel.property_name;
		chosenRel.propFormat = pRel.property_style;
		chosenRel.propDomain = pRel.domain_name;
		chosenRel.propRange = pRel.range_name;

		if (gCe.utils.isNullOrEmpty(ceqb.currentRel)) {
			//A new relationship is being created
			var newRel = ceqb.model.createNewRelationship(chosenRel.propName, chosenRel.propFormat, chosenRel.propDomain, chosenRel.propRange);

			//Calculate the new bounding box of this relationship
			var relCoords = ceqb.drawing.calculateInitialRelCoordsFor(newRel);

			ceqb.drawing.drawRelationshipGroup(newRel);
			ceqb.drawing.drawRelationshipLine(newRel, relCoords);
			ceqb.drawing.drawRelationshipLabel(newRel, relCoords);	
			ceqb.drawing.drawRelationshipCountLabel(newRel, relCoords);

			ceqb.move.moveableRelationship(newRel);
			ceqb.menu.menuRelationship(newRel);
			ceqb.menu.doubleclickRelationship(newRel);

			//Unselect the source and target concepts
			ceqb.actions.clearSourceAndTargetConcepts();

			targetRel = newRel;
		} else {
			//An existing relationship is being updated
			ceqb.model.updateExistingRelationship(ceqb.currentRel, chosenRel.propName, chosenRel.propFormat, chosenRel.propDomain, chosenRel.propRange);
			ceqb.drawing.drawUpdatedRelationshipLabel(ceqb.currentRel);
			ceqb.actions.relationshipCountRequest(ceqb.currentRel);
			targetRel = ceqb.currentRel;
		}

		ceqb.actions.relationshipCountRequest(targetRel);
		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	function addRelationshipToQueryCanvasFromLoad(pRelName, pStyle, pRelType, pDomainConcept, pRangeConcept, pNegDom, pNegRange) {
		ceqb.drawing.calculatePosOffsets();

		//Create the new relationship
		var newRel = ceqb.model.createNewRelationshipFromLoad(pRelName, pStyle, pRelType, pDomainConcept, pRangeConcept, pNegDom, pNegRange);

		//Calculate the new bounding box of this relationship
		var relCoords = ceqb.drawing.calculateInitialRelCoordsFor(newRel);

		ceqb.drawing.drawRelationshipGroup(newRel);
		ceqb.drawing.drawRelationshipLine(newRel, relCoords);
		ceqb.drawing.drawRelationshipLabel(newRel, relCoords);	
		ceqb.drawing.drawRelationshipCountLabel(newRel, relCoords);

		ceqb.move.moveableRelationship(newRel);
		ceqb.menu.menuRelationship(newRel);
		ceqb.menu.doubleclickRelationship(newRel);

		if (newRel.type == ceqb.VAL_TYPE_PREMISE) {
			//This is a premise, so count the relationship instances
			ceqb.actions.relationshipCountRequest(newRel);
		} else {
			//This is a conclusion so no count is needed
			newRel.count = ceqb.VAL_TYPE_CONCLUSION;
			ceqb.drawing.drawUpdatedRelationshipCount(newRel);
		}

		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	}

	this.addFilterToQueryCanvas = function(pFilter) {
		ceqb.drawing.calculatePosOffsets();

		var pos = dojo.position(pFilter.concept.gfx_icon.rawNode);

		ceqb.drawing.drawFilterLabel(pFilter, pos.x, pos.y);
		ceqb.drawing.drawFilterIcon(pFilter, pos.x, pos.y);
		ceqb.drawing.drawFilterLine(pFilter);

		var coords = ceqb.drawing.calculateInitialFilterCoordsFor(pFilter);
		ceqb.drawing.drawFilterLineLabel(pFilter, coords);
		ceqb.drawing.drawFilterCountLabel(pFilter, coords);

		ceqb.move.moveableFilter(pFilter);
		ceqb.menu.menuFilter(pFilter);
		ceqb.menu.doubleclickFilter(pFilter);

		if (pFilter.type == ceqb.VAL_TYPE_PREMISE) {
			//This is a premise, so count the filter instances
			ceqb.actions.filterCountRequest(pFilter);
		} else {
			//This is a conclusion so no count is needed
			pFilter.count = ceqb.VAL_TYPE_CONCLUSION;
			ceqb.drawing.drawUpdatedFilterCount(pFilter);
		}

		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	function addFilterToQueryCanvasFromLoad(pConcept, pVarId, pPropName, pPropFormat, pPremOrConc, pOperator, pValue, pIncInResults, pPosX, pPosY) {
		if (!gCe.utils.isNullOrEmpty(pConcept)) {
			//Only attempt any processing if the concept is not null

			var filterDetails = {};
			filterDetails.variableId = pVarId;
			filterDetails.propName = pPropName;
			filterDetails.propFormat = pPropFormat;

			if (!gCe.utils.isNullOrEmpty(pOperator)) {
				filterDetails.operator = pOperator;
			}
			if (!gCe.utils.isNullOrEmpty(pValue)) {
				filterDetails.value = pValue;
			}

			//Continue the processing (to add the filter)
			var newFilter = ceqb.model.createFilterOnConcept(pConcept, filterDetails, pPremOrConc);

			newFilter.incInResults = pIncInResults;

			ceqb.drawing.drawFilterLabel(newFilter, pPosX, pPosY);
			ceqb.drawing.drawFilterIcon(newFilter, pPosX, pPosY);
			ceqb.drawing.drawFilterLine(newFilter);

			var coords = ceqb.drawing.calculateInitialFilterCoordsFor(newFilter);
			ceqb.drawing.drawFilterLineLabel(newFilter, coords);
			ceqb.drawing.drawFilterCountLabel(newFilter, coords);

			ceqb.move.moveableFilter(newFilter);
			ceqb.menu.menuFilter(newFilter);
			ceqb.menu.doubleclickFilter(newFilter);

			if (newFilter.type == ceqb.VAL_TYPE_PREMISE) {
				//This is a premise, so count the filter instances
				ceqb.actions.filterCountRequest(newFilter);
			} else {
				//This is a conclusion so no count is needed
				newFilter.count = ceqb.VAL_TYPE_CONCLUSION;
				ceqb.drawing.drawUpdatedFilterCount(newFilter);
			}

			ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
		}
	}

	this.displayRuleOrQueryUsing = function(pQueryOrRule) {

		var queryOrRule = gCe.utils.getStructuredResponseFrom(pQueryOrRule);
		gCe.msg.debug('main', 'displayRuleOrQueryUsing', [ queryOrRule ]);

		if (!gCe.utils.isNullOrEmpty(queryOrRule)) {
			ceqb.drawing.calculatePosOffsets();

			var currentX = 100;
			var currentY = 50;
			var xDelta = 50;
			var yDelta = 20;
			var allConcepts = {};
			ceqb.render.clearCanvas();

			this.setQueryNameFromLoad(queryOrRule._id);

			for (var i = 0; i < queryOrRule.concepts.length; i++) {
				var thisQc = queryOrRule.concepts[i];
				gCe.msg.debug('con', 'displayRuleOrQueryUsing', [ thisQc ]);

				var xPos = thisQc.x_pos;
				var yPos = thisQc.y_pos;

				if (gCe.utils.isNullOrEmpty(xPos)) {
					xPos = currentX;
					currentX += xDelta;
				}

				if (gCe.utils.isNullOrEmpty(yPos)) {
					yPos = currentY;
					currentY += yDelta;
				}

				var newCon = addConceptToQueryCanvasFromLoad(thisQc.variable_id, thisQc.concept_name, thisQc.premise_or_conclusion, thisQc.included, xPos, yPos);

				allConcepts[thisQc.variable_id] = newCon;
			}

			for (var i = 0; i < queryOrRule.relationships.length; i++) {
				var thisQr = queryOrRule.relationships[i];
				gCe.msg.debug('rel', 'displayRuleOrQueryUsing', [ thisQr ]);

				var startCon = allConcepts[thisQr.source_variable];
				var endCon = allConcepts[thisQr.target_variable];

				if ((!gCe.utils.isNullOrEmpty(startCon)) && (!gCe.utils.isNullOrEmpty(endCon))) {
					addRelationshipToQueryCanvasFromLoad(thisQr.property_name, thisQr.property_format, thisQr.premise_or_conclusion, startCon, endCon, thisQr.negated_domain, thisQr.negated_range);
				} else {
					gCe.msg.alert('Error rendering query/rule.  Concepts could not be identified for variables \'' + thisQr.sourceVariable + '\' and \'' + thisQr.targetVariable + '\' when processing a relationship (' + thisQr.propName + ')');
				}
			}

			for (var i = 0; i < queryOrRule.attributes.length; i++) {
				var thisQa = queryOrRule.attributes[i];
				gCe.msg.debug('att', 'displayRuleOrQueryUsing', [ thisQa ]);

				var xPos = thisQa.x_pos;
				var yPos = thisQa.y_pos;

				if (gCe.utils.isNullOrEmpty(xPos)) {
					xPos = currentX;
					currentX += xDelta;
				}

				if (gCe.utils.isNullOrEmpty(yPos)) {
					yPos = currentY;
					currentY += yDelta;
				}

				var startCon = allConcepts[thisQa.source_variable];

				var thisOp = thisQa.operator;
				var thisVal = thisQa.value;

				if (gCe.utils.isNullOrEmpty(thisOp)) {
					thisOp = ceqb.VAL_FILTEROP_NONE;
				}

				if (gCe.utils.isNullOrEmpty(thisVal)) {
					thisVal = '';
				}

				addFilterToQueryCanvasFromLoad(startCon, thisQa.variable_id, thisQa.property_name, thisQa.property_format, thisQa.premise_or_conclusion, thisOp, thisVal, thisQa.included, xPos, yPos);
			}
		} else {
			gCe.msg.error('Could not extract rule or query from response');
		}
	};

	this.setQueryNameFromLoad = function(pQueryName) {
		ceqb.queryName = pQueryName;

		var domQueryName = dijit.byId('ceqbQueryName');
		domQueryName.set('value', pQueryName);

		this.refreshQueryName();
	};

}
