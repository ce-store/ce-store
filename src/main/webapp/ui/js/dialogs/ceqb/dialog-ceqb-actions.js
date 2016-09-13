/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/
gEp.dlg.ceqb.actions = new DialogCeqbActions();

function DialogCeqbActions() {
	var ceqb = gEp.dlg.ceqb;

	var SRC_CEQB = 'ceqb';

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbActions', 'initialise');
		//Nothing needed
	};

	this.overwriteCeQueryText = function(pCeText) {
		ceqb.render.updateCePaneWith(pCeText, true);
	};

	this.editedQueryName = function() {
		var domQueryName = dijit.byId('ceqbQueryName');

		ceqb.queryName = domQueryName.get('value');
		ceqb.render.refreshQueryName();
	};

	this.recalculateCeQueryNameOnly = function(pNewName) {
		var startPos = ceqb.ceQueryText.indexOf(']');

		var tempString = null;

		if (startPos < 0) {
			tempString = '[ ' + pNewName + ' ]\n' + ceqb.ceQueryText;
		} else {
			tempString = ceqb.ceQueryText.substring(startPos, ceqb.ceQueryText.length);
		}

		tempString = '[ ' + pNewName + ' ' + tempString;

		var domCeText = dojo.byId('ceQueryText');
		ceqb.ceQueryText = tempString;
		domCeText.innerHTML = gCe.utils.htmlFormat(tempString);
	};

	this.recalculateCeQueryText = function(pMode) {
		var ceText = '';

		if (ceqb.model.isRule()) {
			ceText = ceqb.generation.generateCeRuleTextForAllConcepts(pMode);
		} else {
			ceText = ceqb.generation.generateCeQueryTextForAllConcepts(pMode);
		}

		if (!gCe.utils.isNullOrEmpty(ceqb.queryName)) {
			if (!gCe.utils.isNullOrEmpty(ceText)) {
				ceText = '[' + ceqb.queryName + ']\n' + ceText;
			} else {
				ceText = ceqb.DEFAULT_CETEXT;
			}
		} else {
			if (gCe.utils.isNullOrEmpty(ceText)) {
				ceText = ceqb.DEFAULT_CETEXT;
			}
		}

		ceqb.render.updateCePaneWith(ceText, false);
	};

	this.clearCanvas = function() {
		var response = confirm('Are you sure you want to clear the query canvas?  Everything will be deleted');

		if (response) {
			ceqb.render.clearCanvas();
		}
	};

	this.showConceptDefinition = function(pConcept) {
		gEp.handler.concepts.getConceptDetails(pConcept.concept_name);
	};

	this.showConceptCountCE = function(pConcept) {
		ceqb.render.updateCePaneWith(ceqb.generation.generateCeCountQueryForConcept(pConcept), false);
	};

	this.refreshConceptCount = function(pConcept) {
		var conceptCountQuery = ceqb.generation.generateCeCountQueryForConcept(pConcept);
		var cbf = function(pResponseObject, pParm1) { gEp.dlg.ceqb.response.updateConceptCount(pResponseObject, pParm1); };
		gEp.handler.patterns.executeQueryText(conceptCountQuery, '(count for concept)', cbf, {variableId: pConcept.variableId} );
	};

	this.includeConceptInResults = function(pConcept) {
		pConcept.incInResults = true;
		pConcept.dom_label.innerHTML = ceqb.model.labelTextForConceptOnCanvas(pConcept);
		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.excludeConceptFromResults = function(pConcept) {
		pConcept.incInResults = false;
		pConcept.dom_label.innerHTML = ceqb.model.labelTextForConceptOnCanvas(pConcept);
		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.clearSourceAndTargetConcepts = function() {
		//Clear the selected source and target concepts
		ceqb.tgtConcept = null;
		ceqb.drawing.drawUnselectedSourceConcept();
	};

	this.deleteConcept = function(pConcept, pQuiet, pAutoDelete) {
		var response = true;

		if (!pQuiet) {
			response = confirm('Are you sure you want to delete this concept? (All connected relationships and attributes will be deleted too)');
		}

		if (response) {
			ceqb.gfxSurface.remove(pConcept.gfx_icon);
			dojo.destroy(pConcept.dom_label);
			dojo.destroy(pConcept.dom_countlabel);

			for (var thisFilterKey in pConcept.filters) {
				var thisFilter = pConcept.filters[thisFilterKey];
				if (thisFilter != null) {
					this.deleteFilter(thisFilter, true, pAutoDelete);
				}
			}
			pConcept.filters = [];

			for (var thisRelKey in pConcept.inward_rels) {
				var thisRel = pConcept.inward_rels[thisRelKey];
				if (thisRel != null) {
					this.deleteRelationship(thisRel, true, pAutoDelete);
				}
			}
			pConcept.inward_rels = [];

			for (var thisRelKey in pConcept.outward_rels) {
				var thisRel = pConcept.outward_rels[thisRelKey];
				if (thisRel != null) {
					this.deleteRelationship(thisRel, true, pAutoDelete);
				}
			}
			pConcept.outward_rels = [];

			delete ceqb.allConcepts[pConcept.variableId];

			this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
		}
	};

	this.switchConceptToConclusion = function(pConcept) {
		pConcept.type = ceqb.VAL_TYPE_CONCLUSION;

		//The count is not applicable for conclusions
		pConcept.count = ceqb.VAL_TYPE_CONCLUSION;
		ceqb.drawing.drawUpdatedConceptCount(pConcept);

		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.switchConceptToPremise = function(pConcept) {
		pConcept.type = ceqb.VAL_TYPE_PREMISE;

		//Update the count
		ceqb.actions.refreshConceptCount(pConcept);

		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.editRelationship = function(pRel) {
		this.listRelationshipProperties(pRel.source_concept, pRel.target_concept, pRel);
	};

	this.showRelationshipResults = function(pRel) {
		ceqb.render.updateCePaneWith(ceqb.generation.generateCeQueryForSingleRelationship(pRel), false);
		gEp.handler.patterns.executeQueryText(ceqb.ceQueryText, '(relationships)');
	};

	this.showRelationshipCountCE = function(pRel) {
		ceqb.render.updateCePaneWith(ceqb.generation.generateCeCountQueryForRelationship(pRel), false);
	};

	this.refreshRelationshipCount = function(pRel) {
		this.relationshipCountRequest(pRel);
	};

	this.startRelationshipAtConcept = function(pConcept, pMode) {
		if (pMode) {
			//pMode = true : This concept should be selected
			if (ceqb.srcConcept == null) {
				//There is no existing start concept so simply select this one
				ceqb.drawing.drawSelectedConcept(pConcept);
			} else {
				//There is an existing start concept, so deselect it before selecting the new one
				ceqb.drawing.drawUnselectedSourceConcept();
				ceqb.drawing.drawSelectedConcept(pConcept);
			}
		} else {
			//pMode = false : This concept should be deselected
			ceqb.drawing.drawUnselectedSourceConcept();
		}
	};

	this.endRelationshipAtConcept = function(pConcept) {
		if (ceqb.srcConcept == null) {
			//There is no existing start concept (report error)
			gCe.msg.error('Error - No starting concept for this relationship is specified');
		} else {
			//There is an existing start concept
			if (ceqb.srcConcept.variableId != pConcept.variableId) {
				//The end concept is different to the start concept (create relationship)
				ceqb.tgtConcept = pConcept;

				this.listRelationshipProperties(ceqb.srcConcept, ceqb.tgtConcept, null);
			} else {
				//The end concept is the same as the start concept (report error)
				gCe.msg.error('Error - The start concept cannot be the same as the end concept when creating a relationship');
			}
		}
	};

	this.switchRelationshipToConclusion = function(pRel) {
		pRel.type = ceqb.VAL_TYPE_CONCLUSION;
		ceqb.drawing.drawUpdatedRelationshipLabel(pRel);

		//The count is not applicable for conclusions
		pRel.count = ceqb.VAL_TYPE_CONCLUSION;
		ceqb.drawing.drawUpdatedRelationshipCount(pRel);

		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.switchRelationshipToPremise = function(pRel) {
		pRel.type = ceqb.VAL_TYPE_PREMISE;
		ceqb.drawing.drawUpdatedRelationshipLabel(pRel);

		//Update the count
		this.relationshipCountRequest(pRel);

		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.deleteRelationship = function(pRel, pQuiet, pAutoDelete) {
		var response = true;

		if (!pQuiet) {
			response = confirm('Are you sure you want to delete this relationship?');
		}

		if (response) {
			ceqb.gfxSurface.remove(pRel.gfx_line);
			ceqb.gfxSurface.remove(pRel.gfx_line_ah1);
			ceqb.gfxSurface.remove(pRel.gfx_line_ah2);
			pRel.dom_group.remove(pRel.gfx_label);
			pRel.dom_group.remove(pRel.gfx_countlabel);
			dojo.destroy(pRel.dom_group);

			//Only delete from the parent concept if auto delete is specified
			//(if not then they will be deleted all in one go at the end by the calling function)
			if (pAutoDelete) {
				var inwardIdx = pRel.target_concept.inward_rels.indexOf(pRel);
				if (inwardIdx > -1) {
					pRel.target_concept.inward_rels.splice(inwardIdx, 1);
				}

				var outwardIdx = pRel.source_concept.outward_rels.indexOf(pRel);
				if (outwardIdx > -1) {
					pRel.source_concept.outward_rels.splice(outwardIdx, 1);
				}
			}

			delete ceqb.allRelationships[pRel.variableId];

			this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
		}
	};

	this.addFilterToConcept = function(pConcept) {
		ceqb.currentConcept = pConcept;

		listFilterPropertiesRequest(pConcept);
	};

	this.showFilterResults = function(pFilter) {
		ceqb.render.updateCePaneWith(ceqb.generation.generateCeQueryForSingleFilter(pFilter), false);
		gEp.handler.patterns.executeQueryText(ceqb.ceQueryText, '(filters)');
	};

	this.showFilterCountCE = function(pFilter) {
		ceqb.render.updateCePaneWith(ceqb.generation.generateCeCountQueryForFilter(pFilter), false);
	};

	this.refreshFilterCount = function(pFilter) {
		this.filterCountRequest(pFilter);
	};

	this.includeFilterInResults = function(pFilter) {
		pFilter.incInResults = true;
		pFilter.dom_label.innerHTML = ceqb.model.labelTextForFilterValue(pFilter);
		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.excludeFilterFromResults = function(pFilter) {
		pFilter.incInResults = false;
		pFilter.dom_label.innerHTML = ceqb.model.labelTextForFilterValue(pFilter);
		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.switchFilterToConclusion = function(pFilter) {
		pFilter.type = ceqb.VAL_TYPE_CONCLUSION;
		ceqb.drawing.drawUpdatedFilterLabel(pFilter);

		//The count is not applicable for conclusions
		pFilter.count = ceqb.VAL_TYPE_CONCLUSION;
		ceqb.drawing.drawUpdatedFilterCount(pFilter);

		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.switchFilterToPremise = function(pFilter) {
		pFilter.type = ceqb.VAL_TYPE_PREMISE;
		ceqb.drawing.drawUpdatedFilterLabel(pFilter);

		//Update the count
		this.filterCountRequest(pFilter);

		this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.editFilter = function(pFilter) {
		listFilterPropertiesRequest(pFilter.concept, pFilter);
	};

	this.deleteFilter = function(pFilter, pQuiet, pAutoDelete) {
		var response = true;

		if (!pQuiet) {
			response = confirm('Are you sure you want to delete this attribute?');
		}

		if (response) {
			ceqb.gfxSurface.remove(pFilter.gfx_icon);
			ceqb.gfxSurface.remove(pFilter.gfx_line);
			ceqb.gfxSurface.remove(pFilter.gfx_linelabel);
			ceqb.gfxSurface.remove(pFilter.gfx_countlabel);
			dojo.destroy(pFilter.dom_label);

			//Only delete from the parent concept if auto delete is specified
			//(if not then they will be deleted all in one go at the end by the calling function)
			if (pAutoDelete) {
				var conceptIndex = pFilter.concept.filters.indexOf(pFilter);
				if (conceptIndex > -1) {
					pFilter.concept.filters.splice(conceptIndex, 1);
				}

				for (var lfKey in pFilter.linked_filters) {
					var thisLf = pFilter.linked_filters[lfKey];
					var thisLfId = thisLf.varId;

					ceqb.gfxSurface.remove(thisLf.dom_group);
					ceqb.gfxSurface.remove(thisLf.gfx_line);
					ceqb.gfxSurface.remove(thisLf.gfx_label);

					var srcIdx = thisLf.source_filter.linked_filters.indexOf(thisLfId);
					if (srcIdx > -1) {
						thisLf.source_filter.filters.splice(srcIdx, 1);
					}

					var tgtIdx = thisLf.target_filter.linked_filters.indexOf(thisLfId);
					if (tgtIdx > -1) {
						thisLf.target_filter.filters.splice(srcIdx, 1);
					}

					delete ceqb.allLinkedFilters[thisLfId];
				}

				delete ceqb.allFilters[pFilter.variableId];
			}
			
			delete ceqb.allFilters[pFilter.variableId];

			this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
		}
	};

	this.editLinkedFilter = function(pLf) {
		gCe.msg.alert('Editing of links not yet implemented');
	};

	this.startLinkFilter = function(pFilter, pMode) {
		if (pMode) {
			//pMode = true : This filter should be selected
			if (ceqb.srcFilter == null) {
				//There is no existing start filter so simply select this one
				ceqb.drawing.drawSelectedFilter(pFilter);
			} else {
				//There is an existing start filter, so deselect it before selecting the new one
				ceqb.drawing.drawUnselectedSourceFilter();
				ceqb.drawing.drawSelectedFilter(pFilter);
			}
		} else {
			//pMode = false : This filter should be deselected
			ceqb.drawing.drawUnselectedSourceFilter();
		}
	};

	this.endLinkFilter = function(pFilter) {
		if (ceqb.srcFilter == null) {
			//There is no existing start filter (report error)
			gCe.msg.error('Error - No starting filter for this link is specified');
		} else {
			//There is an existing start filter
			if (ceqb.srcFilter.variableId != pFilter.variableId) {
				ceqb.drawing.calculatePosOffsets();
				//The end filter is different to the start filter (create link)
				ceqb.tgtFilter = pFilter;

				var newFilterLink = ceqb.model.createNewLinkedFilter(ceqb.srcFilter, ceqb.tgtFilter);

				//Calculate the new bounding box of this filter link
				var lfCoords = ceqb.drawing.calculateInitialLinkedFilterCoordsFor(newFilterLink);

				ceqb.drawing.drawLinkedFilterGroup(newFilterLink);
				ceqb.drawing.drawLinkedFilterLine(newFilterLink, lfCoords);
				ceqb.drawing.drawLinkedFilterLabel(newFilterLink, lfCoords);	

				ceqb.move.moveableLinkedFilter(newFilterLink);
				ceqb.menu.menuLinkedFilter(newFilterLink);
				ceqb.menu.doubleclickLinkedFilter(newFilterLink);

				//Unselect the source and target filters
				ceqb.actions.clearSourceAndTargetFilters();

				ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
			} else {
				//The end filter is the same as the start filter (report error)
				gCe.msg.error('Error - The start filter cannot be the same as the end filter when creating a link');
			}
		}
	};

	this.deleteLinkedFilter = function(pLf, pQuiet, pAutoDelete) {
		var response = true;

		if (!pQuiet) {
			response = confirm('Are you sure you want to delete this link?');
		}

		if (response) {
			ceqb.gfxSurface.remove(pLf.gfx_line);
			ceqb.gfxSurface.remove(pLf.dom_group);
			pLf.dom_group.remove(pLf.gfx_label);
			dojo.destroy(pLf.dom_group);

			//Only delete from the parent filters if auto delete is specified
			//(if not then they will be deleted all in one go at the end by the calling function)
			if (pAutoDelete) {
				var srcIdx = pLf.source_filter.filters.indexOf(pLf);
				if (srcIdx > -1) {
					pLf.source_filter.filters.splice(srcIdx, 1);
				}

				var tgtIdx = pLf.target_filter.filters.indexOf(pLf);
				if (tgtIdx > -1) {
					pLf.target_filter.filters.splice(tgtIdx, 1);
				}
			}

			delete ceqb.allLinkedFilters[pLf.variableId];

			this.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
		}
	};

	this.loadQuery = function(pQueryName) {
		gEp.ui.pane.ceqb.activateTab();

		var cbf = function(pResponseObject, pUserParms) { gEp.dlg.ceqb.render.displayRuleOrQueryUsing(pResponseObject, pUserParms); };
		gEp.handler.patterns.getQueryDetails(pQueryName, cbf);
	};

	this.loadRule = function(pRuleName) {
		gEp.ui.pane.ceqb.activateTab();

		var cbf = function(pResponseObject, pUserParms) { gEp.dlg.ceqb.render.displayRuleOrQueryUsing(pResponseObject, pUserParms); };
		gEp.handler.patterns.getRuleDetails(pRuleName, cbf);
	};

	this.validateQueryOrRule = function() {
		var cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportValidateResults(pResponseObject, pUserParms); };
		gEp.handler.sentences.validate(ceqb.ceQueryText, cbf);
	};

	this.executeDrawnQuery = function() {
		var qName = ceqb.queryName;
		var suppressCe = document.getElementById('no_ce').checked;
		var parms = { 'suppressCe': suppressCe };
		
		if (gCe.utils.isNullOrEmpty(qName)) {
			qName = '(no name)';
		}

		if (gCe.utils.startsWith(ceqb.ceQueryText, 'if')) {
			gEp.handler.patterns.executeRuleTextAsQuery(ceqb.ceQueryText, qName, null, parms);
		} else {
			gEp.handler.patterns.executeQueryText(ceqb.ceQueryText, qName, null, parms);
		}
	};

	this.executeDrawnRule = function() {
		var qName = ceqb.queryName;
		
		if (gCe.utils.isNullOrEmpty(qName)) {
			qName = '(no name)';
		}

		if (!gCe.utils.isNullOrEmpty(ceqb.ceQueryText)) {
			var answer = confirm('Are you sure you want to execute this rule.  This will create new CE sentences in your CE Store... you can execute the rule as a query instead if you just want to see the CE that would be generated');

			if (answer) {
				gEp.handler.patterns.executeRuleText(ceqb.ceQueryText, qName);
			}
		} else {
			gCe.msg.error('Cannot execute CE rule as no rule text is specified');
		}
	};

	this.renameVariable = function(pConceptOrFilter, pType) {
		var newVarId = prompt('Please enter the new variable name', pConceptOrFilter.variableId);

		if ((newVarId == null) || (newVarId.trim().length = 0) || (newVarId == pConceptOrFilter.variableId)) {
			gCe.msg.alert('The variable was not changed or was not specified. No change made.');
		} else {
			newVarId = newVarId.trim();
			if (ceqb.model.isVariableNameValid(newVarId)) {
				if (ceqb.model.isVariableNameStillAvailable(newVarId)) {
					//This is a valid new name, so make the changes
					if (pType == 'concept') {
						ceqb.model.renameConceptVariable(pConceptOrFilter, newVarId);
					} else {
						ceqb.model.renameFilterVariable(pConceptOrFilter, newVarId);
					}
				} else {
					gCe.msg.alert('The variable \'' + newVarId + '\' is already in use.  No change made.');
				}
			} else{
				gCe.msg.alert('The variable \'' + newVarId + '\' is invalid - must contain alpha-numeric characters only.  No change made.');
			}
		}
	};

	this.inspectConcept = function(pConcept) {
		inspect(ceqb.model.getConceptById(pConcept.variableId), 'inspectConcept');
	};

	this.inspectRelationship = function(pRel) {
		inspect(ceqb.model.getRelationshipById(pRel.variableId), 'inspectRelationship');
	};

	this.inspectFilter = function(pFilter) {
		inspect(ceqb.model.getFilterById(pFilter.variableId), 'inspectFilter');
	};

	this.inspectLinkedFilter = function(pLf) {
		inspect(ceqb.model.getLinkedFilterById(pLf.variableId), 'inspectLinkedFilter');
	};

	this.inspectGlobals = function() {
		gCe.msg.debug(ceqb.allConcepts, 'ceqb.allConcepts');
		gCe.msg.debug(ceqb.allRelationships, 'ceqb.allRealtionships');
		gCe.msg.debug(ceqb.allFilters, 'ceqb.allFilters');
		gCe.msg.debug(ceqb.allLinkedFilters, 'ceqb.allLinkedFilters');
		gCe.msg.debug(ceqb.latestPropList, 'ceqb.latestPropList');
		gCe.msg.debug(ceqb.latestVarId, 'ceqb.latestVarId');
		gCe.msg.debug(ceqb.latestFiltId, 'ceqb.latestFiltId');
		gCe.msg.debug(ceqb.latestShapeId, 'ceqb.latestShapeId');
		gCe.msg.debug(ceqb.currentConcept, 'ceqb.currentConcept');
		gCe.msg.debug(ceqb.srcConcept, 'ceqb.srcConcept');
		gCe.msg.debug(ceqb.tgtConcept, 'ceqb.tgtConcept');
		gCe.msg.debug(ceqb.posMainOffsetX, 'ceqb.posMainOffsetX');
		gCe.msg.debug(ceqb.posMainOffsetY, 'ceqb.posMainOffsetY');
		gCe.msg.debug(ceqb.posMinorOffsetX, 'ceqb.posMinorOffsetX');
		gCe.msg.debug(ceqb.posMinorOffsetY, 'ceqb.posMinorOffsetY');
	};

	function inspect(pThing, pMethod) {
		gCe.msg.debug(pThing, pMethod);
	}

	this.clearSourceAndTargetFilters = function() {
		//Clear the selected source and target filters
		ceqb.tgtFilter = null;
		ceqb.drawing.drawUnselectedSourceFilter();
	};

	this.saveQueryOrRule = function() {
		var ceText = ceqb.ceQueryText;
		
		if (!gCe.utils.isNullOrEmpty(ceText)) {
			var cbf = function(pResponseObject, pUserParms) { actionRefreshSavedQueries(pResponseObject, pUserParms); };
			gEp.handler.sentences.add(ceText, 'CEQB', SRC_CEQB, cbf);
		} else {
			alert('There is nothing to save!');
		}
	};

	this.relationshipCountRequest = function(pRel) {
		if (ceqb.model.isPremiseRel(pRel)) {
			var userParms = {tgtRel: pRel};
			var relCountQuery = ceqb.generation.generateCeCountQueryForRelationship(pRel);
			var cbf = function(pResponseObject, pUserParms) { gEp.dlg.ceqb.response.updateRelationshipCount(pResponseObject, pUserParms); };
			gEp.handler.patterns.executeQueryText(relCountQuery, '(count for relationship)', cbf, userParms);
		} else {
			//No need to send count request for conclusion
		}
	};

	this.filterCountRequest = function(pFilter) {
		var userParms = {tgtFilter: pFilter};
		var filterCountQuery = ceqb.generation.generateCeCountQueryForFilter(pFilter);
		var cbf = function(pResponseObject, pUserParms) { gEp.dlg.ceqb.response.updateFilterCount(pResponseObject, pUserParms); };
		gEp.handler.patterns.executeQueryText(filterCountQuery, '(count for filter)', cbf, userParms);
	};

	this.listRelationshipProperties = function(pDomain, pRange, pRel) {
		var userParms = {tgtRel: pRel};
		var cbf = function(pResponseObject, pUserParms) { gEp.dlg.ceqb.response.updateRelationshipPropertiesList(pResponseObject, pUserParms); };
		gEp.handler.concepts.listObjectPropertiesWithRange(pDomain.concept_name, pRange.concept_name, cbf, userParms);
	};

	function listFilterPropertiesRequest(pConcept, pFilter) {
		var userParms = {tgtFilter: pFilter};
		var cbf = function(pResponseObject, pUserParms) { gEp.dlg.ceqb.response.updateFilterPropertiesList(pResponseObject, pUserParms); };
		gEp.handler.concepts.listDatatypeProperties(pConcept.concept_name, cbf, userParms);
	}

}
