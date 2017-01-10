/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.ceqb.model = new DialogCeqbModel();

function DialogCeqbModel() {
	var ceqb = gEp.dlg.ceqb;

//Various names for the conceptual and drawn model are defined here to avoid confusion with the dojo drawing terms:
//  Conceptual model
//	    Concept - a type or class from the CE model (e.g. "person")
//	              ConceptPi (Pi = PaletteItem) is a sub-classification used to distinguish a "concept" (on the query canvas)
//	              from a "concept" shown in the concept palette 
//	    Relationship - a link between two concepts within the CE model (e.g. "is married to")
//	                   Links are directional.
//	                   The default relationship which indicates any valid relationship is "is related to"
//	    Filter - a restriction on a specific value for a property

//	    Source Concept - The concept at the start of a relationship
//	    Target Concept - The concept at the end of a relationship

	//Javascript structures
	//  ConceptPi (Palette Item - on Concept Palette)
//	    name - The CE name for this concept
//	    image - The unqualified filename of the image for this concept (not currently used)
//	    type - Always set to "standard".  May be removed
//

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbModel', 'initialise');
		//Nothing needed
	};

	this.isRule = function() {
		var result = false;

		//If any concept or relationship is a conclusion then this is a rule
		for (var conceptKey in ceqb.allConcepts) {
			var thisConcept = ceqb.allConcepts[conceptKey];

			if (this.isConclusionConcept(thisConcept)) {
				result = true;
			}
		}

		for (var relKey in ceqb.allRelationships) {
			var thisRel = ceqb.allRelationships[relKey];

			if (this.isConclusionRel(thisRel)) {
				result = true;
			}
		}

		return result;
	};

	this.getConceptById = function(pConceptName) {
		return ceqb.allConcepts[pConceptName];
	};

	this.getRelationshipById = function(pRelId) {
		return ceqb.allRelationships[pRelId];
	};

	this.getFilterById = function(pFilterId) {
		return ceqb.allFilters[pFilterId];
	};

	this.getLinkedFilterById = function(pLfId) {
		return ceqb.allLinkedFilters[pLfId];
	};

	this.getRuleOrQueryNamed = function(pName) {
		var result = null;

		for (var i = 0; i < ceqb.ruleList.length; i++) {
			var thisRuleOrQuery = ceqb.ruleList[i];

			if (thisRuleOrQuery._id == pName) {
				result = thisRuleOrQuery;
			}
		}

		for (var i = 0; i < ceqb.queryList.length; i++) {
			var thisRuleOrQuery = ceqb.queryList[i];

			if (thisRuleOrQuery._id == pName) {
				result = thisRuleOrQuery;
			}
		}

		return result;
	};

	this.getLatestPropertyByFullName = function(pFullName) {
		var result = null;

		for (var i = 0; i < ceqb.latestPropList.length; i++){
			var thisProp = ceqb.latestPropList[i];
			
			if (thisProp._id === pFullName) {
				result = thisProp;
			}
		}

		return result;
	};

	this.isComplexFilter = function(pFilter) {
		return (pFilter.operator != ceqb.VAL_FILTEROP_EQUALS) && ((pFilter.operator != ceqb.VAL_FILTEROP_NONE));
	};

	this.isUnqualifiedFilter = function(pFilter) {
		return (pFilter.operator == ceqb.VAL_FILTEROP_NONE);
	};

	this.isSimpleFilter = function(pFilter) {
		return (pFilter.operator == ceqb.VAL_FILTEROP_EQUALS);
	};

	this.isPremiseRel = function(pRel) {
		return (pRel.type == ceqb.VAL_TYPE_PREMISE);
	};

	this.isConclusionRel = function(pRel) {
		return !this.isPremiseRel(pRel);
	};

	this.isPremiseLinkedFilter = function(pLf) {
		return (pLf.type == ceqb.VAL_TYPE_PREMISE);
	};

	this.isConclusionLinkedFilter = function(pLf) {
		return (pLf.type == ceqb.VAL_TYPE_CONCLUSION);
	};

	this.isPremiseConcept = function(pConcept) {
		return (pConcept.type == ceqb.VAL_TYPE_PREMISE);
	};

	this.isConclusionConcept = function(pConcept) {
		return !this.isPremiseConcept(pConcept);
	};

	this.isPremiseFilter = function(pFilter) {
		return (pFilter.type == ceqb.VAL_TYPE_PREMISE);
	};

	this.isConclusionFilter = function(pFilter) {
		return !this.isPremiseFilter(pFilter);
	};

	this.isIsolatedConcept = function(pConcept) {
		var result = true;

		//First check inward relationships
		for (var i = 0; i < pConcept.inward_rels.length; i++){
			var thisRel = pConcept.inward_rels[i];
			
			if (this.isPremiseRel(thisRel)) {
				result = false;
			}
		}

		//Next check outward relationships
		for (var i = 0; i < pConcept.outward_rels.length; i++){
			var thisRel = pConcept.outward_rels[i];
			
			if (this.isPremiseRel(thisRel)) {
				result = false;
			}
		}

		//Finally check filters
		for (var i = 0; i < pConcept.filters.length; i++){
			var thisFilter = pConcept.filters[i];
			
			if (this.isPremiseFilter(thisFilter)) {
				result = false;
			}
		}

		return result;
	};

	this.createNewConcept = function(pConceptPi) {
		var newConcept = {};

		newConcept.type = ceqb.VAL_TYPE_PREMISE;
		newConcept.variableId = this.calculateNextVariableId();
		newConcept.shape_id = calculateNextShapeId();
		newConcept.concept_name = pConceptPi.name;
		newConcept.count = pConceptPi.count;
		newConcept.inward_rels = [];
		newConcept.outward_rels = [];
		newConcept.filters = [];
		newConcept.incInResults = true;

		ceqb.allConcepts[newConcept.variableId] = newConcept;

		return newConcept;
	};

	this.createNewConceptFromLoad = function(pVarId, pName, pIncInResults, pPremOrConc) {
		var newConcept = {};

		newConcept.type = pPremOrConc;
		newConcept.variableId = pVarId;
		newConcept.shape_id = calculateNextShapeId();
		newConcept.concept_name = pName;
		newConcept.count = 0;
		newConcept.inward_rels = [];
		newConcept.outward_rels = [];
		newConcept.filters = [];
		newConcept.incInResults = pIncInResults;

		ceqb.allConcepts[newConcept.variableId] = newConcept;

		return newConcept;
	};

	this.renameConceptVariable = function(pConcept, pNewVarId) {
		var oldVarId = pConcept.variableId;

		pConcept.variableId = pNewVarId;
		pConcept.dom_label.innerHTML = this.labelTextForConceptOnCanvas(pConcept);
		ceqb.allConcepts[pConcept.variableId] = pConcept;
		delete ceqb.allConcepts[oldVarId];

		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.calculateConceptSummary = function(pConcept) {
		return pConcept.concept_name + ' (' + pConcept.variableId + ')';
	};

	this.createNewRelationship = function(pPropName, pPropFormat, pPropDomain, pPropRange) {
		var newRel = {};

		newRel.variableId = calculateRelNameUsing(ceqb.srcConcept, pPropName, ceqb.tgtConcept);
		newRel.type = ceqb.VAL_TYPE_PREMISE;

		newRel.shape_id = calculateNextShapeId();
		newRel.fullName = pPropDomain + ':' + pPropName + ':' + pPropRange;
		newRel.prop_name = pPropName;
		newRel.prop_format = pPropFormat;
		newRel.prop_domain = pPropDomain;
		newRel.prop_range = pPropRange;

		newRel.source_concept = ceqb.srcConcept;
		newRel.source_concept.outward_rels.push(newRel);
		newRel.target_concept = ceqb.tgtConcept;
		newRel.target_concept.inward_rels.push(newRel);

		ceqb.allRelationships[newRel.variableId] = newRel;

		return newRel;
	};

	this.createNewRelationshipFromLoad = function(pPropName, pPropFormat, pRelType, pDomainConcept, pRangeConcept, pNegDomain, pNegRange) {
		var newRel = {};

		newRel.variableId = calculateRelNameUsing(pDomainConcept, pPropName, pRangeConcept);
		newRel.type = pRelType;

		newRel.shape_id = calculateNextShapeId();
		newRel.fullName = pDomainConcept.concept_name + ':' + pPropName + ':' + pRangeConcept.concept_name;
		newRel.prop_name = pPropName;
		newRel.prop_format = pPropFormat;
		newRel.prop_domain = pDomainConcept.concept_name;
		newRel.prop_range = pRangeConcept.concept_name;
		newRel.negated_domain = pNegDomain;
		newRel.negated_range = pNegRange;

		newRel.source_concept = pDomainConcept;
		newRel.source_concept.outward_rels.push(newRel);
		newRel.target_concept = pRangeConcept;
		newRel.target_concept.inward_rels.push(newRel);

		ceqb.allRelationships[newRel.variableId] = newRel;

		return newRel;
	};

	this.updateExistingRelationship = function(pRel, pPropName, pPropFormat, pPropDomain, pPropRange) {
		var oldRelVarId = pRel.variableId;
		pRel.variableId = calculateRelNameUsing(pRel.source_concept, pPropName, pRel.target_concept);

		pRel.fullName = pPropDomain + ':' + pPropName + ':' + pPropRange;
		pRel.prop_name = pPropName;
		pRel.prop_format = pPropFormat;
		pRel.prop_domain = pPropDomain;
		pRel.prop_range = pPropRange;

		delete ceqb.allRelationships[oldRelVarId];
		ceqb.allRelationships[pRel.variableId] = pRel;

		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	function calculateRelNameUsing(pSrcConcept, pPropName, pTargetConcept) {
		return pSrcConcept.variableId + ':' + pPropName + ':' + pTargetConcept.variableId;
	}

	this.createFilterOnConcept = function(pConcept, pFilterDetails, pPremOrConc) {
		var newFilter = {};

		newFilter.variableId = pFilterDetails.variableId;
		newFilter.type = pPremOrConc;
		newFilter.shape_id = calculateNextShapeId();
		newFilter.fullName = pFilterDetails.fullName;
		newFilter.prop_name = pFilterDetails.propName;
		newFilter.prop_format = pFilterDetails.propFormat;
		newFilter.operator = pFilterDetails.operator;
		newFilter.value = pFilterDetails.value;
		newFilter.incInResults = true;
		newFilter.linked_filters = [];

		newFilter.concept = pConcept;
		pConcept.filters.push(newFilter);

		ceqb.allFilters[newFilter.variableId] = newFilter;

		return newFilter;
	};

	this.createNewLinkedFilter = function(pSrcFilter, pTgtFilter) {
		var newLinkedFilter = {};

		newLinkedFilter.type = ceqb.VAL_TYPE_PREMISE;
		newLinkedFilter.variableId = this.calculateNextLinkId();
		newLinkedFilter.shape_id = calculateNextShapeId();
		newLinkedFilter.source_filter = pSrcFilter;
		newLinkedFilter.target_filter = pTgtFilter;
		newLinkedFilter.operator = '=';

		pSrcFilter.linked_filters.push(newLinkedFilter);
		pTgtFilter.linked_filters.push(newLinkedFilter);

		ceqb.allLinkedFilters[newLinkedFilter.variableId] = newLinkedFilter;

		return newLinkedFilter;
	};

	this.renameFilterVariable = function(pFilter, pNewVarId) {
		var oldVarId = pFilter.variableId;

		pFilter.variableId = pNewVarId;
		pFilter.dom_label.innerHTML = this.labelTextForFilterValue(pFilter);
		ceqb.allFilters[pFilter.variableId] = pFilter;
		delete ceqb.allFilters[oldVarId];

		ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
	};

	this.labelTextForConceptOnCanvas = function(pConcept) {
		var formattedExcText = '';

		if (!pConcept.incInResults) {
			formattedExcText = '<br/>' + '<i>(excluded)</i>';
		}

		return 'the' + '<br/>' + pConcept.concept_name + '<br/>' + pConcept.variableId + formattedExcText;
	};

	this.labelTextForRelationship = function(pRel) {
		var labelText = '';

		if (isVerbSingular(pRel)) {
			labelText = pRel.prop_name;
		} else {
			labelText = 'has as ' + pRel.prop_name;
		}

		return labelText;
	};

	this.labelTextForPropertyDetails = function(pProp) {
		var labelText = pProp.property_name + ' (' + pProp.domain_name + ':' + pProp.range_name + ') [' + pProp.property_style + ']';

		return labelText;
	};

	this.labelTextForFilterValue = function(pFilter) {
		var labelText = '';
		var formattedExcText = '';

		labelText = 'the value ' + pFilter.variableId;

		if (pFilter.operator != '(none)') {
			labelText += ' ' + pFilter.operator + ' ' + '\'' + pFilter.value +  '\'';
		}

		if (!pFilter.incInResults) {
			formattedExcText = '<br/>' + '<i>(excluded)</i>';
		}

		return labelText + formattedExcText;
	};

	this.labelTextForLinkedFilter = function(pLf) {
		return pLf.operator;
	};

	this.isVariableNameValid = function(pVarId) {
		var result = false;

		if (gCe.utils.startsWith(pVarId, '\'') || gCe.utils.startsWith(pVarId, '"')) {
			//A quoted value - anything is allowed
			result = true;
		} else {
			//A non-quoted value - must be checked
			var regx = /^([a-zA-Z0-9_-]+)$/;
			var trimmedVarId = gCe.utils.replaceAll(pVarId, '\'', '');

			result = regx.test(trimmedVarId); 
		}

		return result;
	};

	this.isVariableNameStillAvailable = function(pVarId) {
		return (ceqb.allConcepts[pVarId] == null) && ((ceqb.allFilters[pVarId] == null));
	};

	this.calculateNextFilterId = function() {
		var available = false;
		var thisId = '';

		//Because the user can choose their own variable ids we may get the odd clash
		//so iterate through until the next available one is found
		while (available == false) {
			thisId = '_F' + ceqb.latestFiltId++;
			available = this.isVariableNameStillAvailable(thisId);
		}

		return thisId;
	};

	this.calculateVisLabelIdFor = function(pVisId) {
		return pVisId + '_lab';
	};

	this.calculateVisLineLabelIdFor = function(pVisId) {
		return pVisId + '_linelab';
	};

	this.calculateVisCountIdFor = function(pVisId) {
		return pVisId + '_count';
	};

	this.calculateNextVariableId = function() {
		var available = false;
		var thisId = '';

		//Because the user can choose their own variable ids we may get the odd clash
		//so iterate through until the next available one is found
		while (available == false) {
			thisId = 'V' + ceqb.latestVarId++;
			available = this.isVariableNameStillAvailable(thisId);
		}

		return thisId;
	};

	this.calculateNextLinkId = function() {
		var available = false;
		var thisId = '';

		//Iterate through until the first available one is found
		while (available == false) {
			thisId = 'L' + ceqb.latestLinkId++;
			available = this.isVariableNameStillAvailable(thisId);
		}

		return thisId;
	};

	function calculateNextShapeId() {
		return 'S' + ceqb.latestShapeId++;
	}

	function isFunctionalNoun(pRel) {
		return (pRel.prop_format == gCe.STYLE_FN);
	}

	function isVerbSingular(pRel) {
		return !isFunctionalNoun(pRel);
	}

}
