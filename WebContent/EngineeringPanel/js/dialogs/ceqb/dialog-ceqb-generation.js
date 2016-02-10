/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.ceqb.generation = new DialogCeqbGeneration();

function DialogCeqbGeneration() {
	var ceqb = gEp.dlg.ceqb;

	var INDENT = '    ';

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbGeneration', 'initialise');
		//Nothing needed
	};

	this.generateCeQueryTextForAllConcepts = function(pMode) {
		var queryPreamble = '';
		var queryClausesText = '';
		var existingClauses = false;
		var result = '';

		//Generate the actual query clauses
		for (var thisConceptKey in ceqb.allConcepts) {
			var thisConcept = ceqb.allConcepts[thisConceptKey];

			if (!gCe.utils.isNullOrEmpty(thisConcept)) {
				queryClausesText += generateCeQueryTextForConcept(thisConcept, existingClauses);
				if (queryClausesText.length > 0) {
					existingClauses = true;
				}
			}
		}

		if (!gCe.utils.isNullOrEmpty(queryClausesText)) {
			//The varIdList is calculated automatically if a null value is passed in
			queryPreamble = generateCePreambleFor(pMode, null);

			result = queryPreamble + '\n' + queryClausesText + '\n' + '.';
		}

		return result;
	};

	this.generateCeRuleTextForAllConcepts = function(pMode) {
		var concPart = '';
		var premPart = '';
		var concSep = '';
		var premSep = '';
		var result = '';

		//First iterate through all concepts (for conclusion processing - these come at the beginning)
		if (!gCe.utils.isNullOrEmpty(ceqb.allConcepts)) {
			for (var thisKey in ceqb.allConcepts) {
				var thisConcept = ceqb.allConcepts[thisKey];

				if (ceqb.model.isConclusionConcept(thisConcept)) {
					//Concepts only need to be added to the conclusion if they have no premise based relationships or filters
					if (ceqb.model.isIsolatedConcept(thisConcept)) {
						concPart += concSep + generateCeQueryTextForSimpleConcept(thisConcept);
						concSep = ' and' + '\n' + INDENT;
					}
				}
			}
		}

		//Iterate through all relationships
		if (!gCe.utils.isNullOrEmpty(ceqb.allRelationships)) {
			for (var thisRelKey in ceqb.allRelationships) {
				var thisRel = ceqb.allRelationships[thisRelKey];
		
				if (ceqb.model.isConclusionRel(thisRel)) {
					concPart += concSep + generateCeQueryTextForRelationship(thisRel);
					concSep = ' and' + '\n' + INDENT;
				} else {
					premPart += premSep + generateCeQueryTextForRelationship(thisRel);
					premSep = ' and' + '\n' + INDENT;
				}
			}
		}

		//Now iterate through all filters
		if (!gCe.utils.isNullOrEmpty(ceqb.allFilters)) {
			for (var thisFiltKey in ceqb.allFilters) {
				var thisFilter = ceqb.allFilters[thisFiltKey];
		
				if (ceqb.model.isConclusionRel(thisFilter)) {
					concPart += concSep + generateCeQueryTextForFilter(thisFilter);
					concSep = ' and' + '\n' + INDENT;
				} else {
					premPart += premSep + generateCeQueryTextForFilter(thisFilter);
					premSep = ' and' + '\n' + INDENT;
				}
			}
		}

		//Finally iterate through all concepts (for premise processing - these come at the end)
		if (!gCe.utils.isNullOrEmpty(ceqb.allConcepts)) {
			for (var thisKey in ceqb.allConcepts) {
				var thisConcept = ceqb.allConcepts[thisKey];

				if (ceqb.model.isPremiseConcept(thisConcept)) {
					//Concepts only need to be added to the premise if they have no premise based relationships or filters
					if (ceqb.model.isIsolatedConcept(thisConcept)) {
						premPart += premSep + generateCeQueryTextForSimpleConcept(thisConcept);
						premSep = ' and' + '\n' + INDENT;
					}
				}
			}
		}

		result = 'if' + '\n' + INDENT + premPart + '\n' + 'then' + '\n' + INDENT + concPart + '\n' + '.';
		return result;
	};

	this.generateCeCountQueryForConcept = function(pConcept) {
		var result = '';
		var conceptArray = [];

		conceptArray.push(pConcept.variableId);

		result = generateCePreambleFor(ceqb.GENMODE_COUNT, conceptArray) + '\n';
		result += INDENT + generateCeQueryTextForSimpleConcept(pConcept) + '\n';
		result += '.';

		return result;
	};

	this.generateCeCountQueryForRelationship = function(pRel) {
		var result = '';
		var conceptArray = [];

		conceptArray.push(pRel.source_concept.variableId);
		conceptArray.push(pRel.target_concept.variableId);

		result = generateCePreambleFor(ceqb.GENMODE_COUNT, conceptArray) + '\n';
		result += INDENT + generateCeQueryTextForRelationship(pRel) + '\n';
		result += '.';

		return result;
	};

	this.generateCeCountQueryForFilter = function(pFilter) {
		var result = '';
		var conceptArray = [];

		conceptArray.push(pFilter.concept.variableId);

		result = generateCePreambleFor(ceqb.GENMODE_COUNT, conceptArray) + '\n';
		result += INDENT + generateCeQueryTextForFilter(pFilter) + '\n';
		result += '.';

		return result;
	};

	this.generateCeQueryForSingleFilter = function(pFilter) {
		var result = '';
		var conceptArray = [];

		conceptArray.push(pFilter.concept.variableId);

		result = generateCePreambleFor(ceqb.GENMODE_NORMAL, conceptArray) + '\n';
		result += INDENT + generateCeQueryTextForFilter(pFilter) + '\n';
		result += '.';

		return result;
	};

	this.generateCeQueryForSingleRelationship = function(pRel) {
		var result = '';
		var conceptArray = [];

		conceptArray.push(pRel.source_concept.variableId);
		conceptArray.push(pRel.target_concept.variableId);

		result = generateCePreambleFor(ceqb.GENMODE_NORMAL, conceptArray) + '\n';
		result += INDENT + generateCeQueryTextForRelationship(pRel) + '\n';
		result += '.';

		return result;
	};

	function generateCePreambleFor(pMode, pIdArray) {
		var preambleIntro = '';
		var idListText = '';
		var idArray = null;

		if (pMode == ceqb.GENMODE_COUNT) {
			preambleIntro = 'for how many ';
		} else {
			preambleIntro = 'for which ';
		}

		if (gCe.utils.isNullOrEmpty(pIdArray)) {
			//No list of ids provided so get ids for all concepts
			idArray = generateCeQueryIdArray();
		} else {
			//Use the passed list of ids
			idArray = pIdArray;
		}

		idListText = generateCeQueryIdListText(idArray);

		return preambleIntro + idListText + ' is it true that ';
	}

	function generateCeQueryIdArray() {
		var totalList = [];

		//Build a combined collection of concept and filter ids
		for (var thisConceptKey in ceqb.allConcepts) {		
			var thisConcept = ceqb.allConcepts[thisConceptKey];
			if (!gCe.utils.isNullOrEmpty(thisConcept)) {
				if (thisConcept.incInResults) {
					if (!gCe.utils.startsWith(thisConcept.variableId, '\'')) {
						totalList.push(thisConcept.variableId);
					}
				}
			}
		}

		for (var thisFilterKey in ceqb.allFilters) {
			var thisFilter = ceqb.allFilters[thisFilterKey];
			if (thisFilter.incInResults) {
				if (!ceqb.model.isSimpleFilter(thisFilter)) {
					//Non simple filter ids should be added to the list of ids (simple filters use a different syntax without the filter id)
					if (!gCe.utils.startsWith(thisFilter.variableId, '\'')) {
						totalList.push(thisFilter.variableId);
					}
				}
			}
		}

		return totalList;
	}

	function generateCeQueryIdListText(pArray) {
		var result = '';
		var commaSep = '';
		var lastId = '';

		//Now generate the list of ids
		for (var i = 0; i < pArray.length; i++) {
			var thisId = pArray[i];

			if (!gCe.utils.isNullOrEmpty(thisId)) {
				if (lastId.length > 0) {
					result += commaSep + lastId;
					commaSep = ', ';
				}
				lastId = thisId;
			}
		}

		//If the comma separator has already been set change it to "and" for the last value
		if (commaSep.length > 0) {
			commaSep = ' and ';
		}
		result += commaSep + lastId;

		return result;
	}

	function generateCeQueryTextForConcept(pConcept, pExistingClauses) {
		var result = '';
		var clauseSep = '';

		if (pExistingClauses) {
			clauseSep = ' and' + '\n';
		}

		//Process the outward relationships
		//Inward relationships are not processed as they will be covered by processing all the outward ones
		for (var i = 0; i < pConcept.outward_rels.length; i++) {
			var thisRel = pConcept.outward_rels[i];

			if (!gCe.utils.isNullOrEmpty(thisRel)) {
				result += clauseSep + INDENT + generateCeQueryTextForRelationship(thisRel);
				clauseSep = ' and' + '\n';
			}
		}

		//Process the filters
		for (var i = 0; i < pConcept.filters.length; i++) {
			var thisFilter = pConcept.filters[i];

			if (!gCe.utils.isNullOrEmpty(thisFilter)) {
				result += clauseSep + INDENT + generateCeQueryTextForFilter(thisFilter);
				clauseSep = ' and' + '\n';
			}
		}

		if (result.length == 0) {
			if (pConcept.inward_rels.length == 0) {
				//There are no relationships or filters and there are no inward relations so this is a plain single node query
				result = clauseSep + INDENT + generateCeQueryTextForSimpleConcept(pConcept);
			}
		}

		return result;
	}

	function generateCeQueryTextForSimpleConcept(pConcept) {
		return '( there is a ' + pConcept.concept_name + ' named ' + pConcept.variableId + ' )';
	}

	function generateCeQueryTextForRelationship(pRel) {
		var result = '';

		var propName = pRel.prop_name;
		var propFormat = pRel.prop_format;
		var dConcept = pRel.source_concept.concept_name;
		var dVarId = pRel.source_concept.variableId;
		var rConcept = pRel.target_concept.concept_name;
		var rVarId = pRel.target_concept.variableId;

		var theOrNoDom = '';
		var theOrNoRange = '';

		if (pRel.negated_domain) {
			theOrNoDom = 'no';
		} else {
			theOrNoDom = 'the';
		}

		if (pRel.negated_range) {
			theOrNoRange = 'no';
		} else {
			theOrNoRange = 'the';
		}

		if (propFormat == gCe.STYLE_VS) {
			//Verb Singular (VS) format
			result += clauseTextRelVs(propName, dConcept, dVarId, rConcept, rVarId, theOrNoDom, theOrNoRange);
		} else {
			//Functional noun (FN) format
			result += clauseTextRelFn(propName, dConcept, dVarId, rConcept, rVarId, theOrNoDom, theOrNoRange);
		}

		return result;
	}

	function generateCeQueryTextForFilter(pFilter) {
		var result = '';

		var propName = pFilter.prop_name;
		var propFormat = pFilter.prop_format;
		var dConcept = pFilter.concept.concept_name;
		var dVarId = pFilter.concept.variableId;
		var fVarId = pFilter.variableId;
		var fOperator = pFilter.operator;
		var fValue = pFilter.value;	

		if (propFormat == gCe.STYLE_VS) {
			//Verb Singular (VS) format
			if (ceqb.model.isComplexFilter(pFilter)) {
				//More complex syntax for non equality operators
				result += clauseTextFilterComplexVs(propName, dConcept, dVarId, fVarId, fValue, fOperator);
			} else if (ceqb.model.isUnqualifiedFilter(pFilter)) {
				//Unqualified syntax
				result += clauseTextFilterUnqualifiedVs(propName, dConcept, dVarId, fVarId, fValue);
			} else {
				//Simple syntax for equality - currently not used in premise
				if (ceqb.model.isConclusionFilter(pFilter)) {
					result += clauseTextFilterSimpleVs(propName, dConcept, dVarId, fValue);
				} else {
					result += clauseTextFilterComplexVs(propName, dConcept, dVarId, fVarId, fValue, fOperator);
				}
			}
		} else {
			//Functional noun (FN) format
			if (ceqb.model.isComplexFilter(pFilter)) {
				//More complex syntax for non equality operators
				result += clauseTextFilterComplexFn(propName, dConcept, dVarId, fVarId, fValue, fOperator);
			} else if (ceqb.model.isUnqualifiedFilter(pFilter)) {
				//Unqualified syntax
				result += clauseTextFilterUnqualifiedFn(propName, dConcept, dVarId, fVarId, fValue);
			} else {
				//Simple syntax for equality - currently not used in premise
				if (ceqb.model.isConclusionFilter(pFilter)) {
					result += clauseTextFilterSimpleFn(propName, dConcept, dVarId, fValue);
				} else {
					result += clauseTextFilterComplexFn(propName, dConcept, dVarId, fVarId, fValue, fOperator);
				}
			}
		}

		return result;
	}

	function clauseTextRelVs(pPropName, pDomConcept, pDomVarId, pRangeConcept, pRangeVarId, pTheOrNoDom, pTheOrNoRange) {
		//Return the clause text for a relationship in verb singular (VS) format 

		return '( ' + pTheOrNoDom + ' ' + pDomConcept + ' ' + pDomVarId + ' ' + pPropName + ' ' + pTheOrNoRange + ' ' + pRangeConcept + ' ' + pRangeVarId + ' )';
	}

	function clauseTextRelFn(pPropName, pDomConcept, pDomVarId, pRangeConcept, pRangeVarId, pTheOrNoDom, pTheOrNoRange) {
		//Return the clause text for a relationship in functional noun (FN) format 

		return '( ' + pTheOrNoDom + ' ' + pDomConcept + ' ' + pDomVarId + ' has ' + pTheOrNoRange + ' ' + pRangeConcept + ' ' + pRangeVarId + ' as ' + pPropName + ' )';
	}

	function clauseTextFilterSimpleVs(pPropName, pDomConcept, pDomVarId, pFilterValue) {
		//Return the clause text for a simple filter in verb singular (VS) format 
		return '( the ' + pDomConcept + ' ' + pDomVarId + ' ' + pPropName + ' \'' + pFilterValue + '\' )';
	}

	function clauseTextFilterUnqualifiedVs(pPropName, pDomConcept, pDomVarId, pFilterVarId, pFilterValue) {
		//Return the clause text for an unqualified filter in verb singular (VS) format 
		return '( the ' + pDomConcept + ' ' + pDomVarId + ' ' + pPropName + ' the value ' + pFilterVarId + ' )';
	}

	function clauseTextFilterComplexVs(pPropName, pDomConcept, pDomVarId, pFilterVarId, pFilterValue, pFilterOperator) {
		//Return the clause text for a complex filter in verb singular (VS) format 
		return '( the ' + pDomConcept + ' ' + pDomVarId + ' ' + pPropName + ' the value ' + pFilterVarId + ') and (the value ' + pFilterVarId + ' ' + pFilterOperator + ' \'' + pFilterValue + '\' )';
	}

	function clauseTextFilterSimpleFn(pPropName, pDomConcept, pDomVarId, pFilterValue) {
		//Return the clause text for a simple filter in functional noun (FN) format 
		return '( the ' + pDomConcept + ' ' + pDomVarId + ' has \'' + pFilterValue + '\' as ' + pPropName + ' )';
	}

	function clauseTextFilterUnqualifiedFn(pPropName, pDomConcept, pDomVarId, pFilterVarId, pFilterValue) {
		//Return the clause text for an unqualified filter in functional noun (FN) format 
		return '( the ' + pDomConcept + ' ' + pDomVarId + ' has the value ' + pFilterVarId + ' as ' + pPropName + ' )';
	}

	function clauseTextFilterComplexFn(pPropName, pDomConcept, pDomVarId, pFilterVarId, pFilterValue, pFilterOperator) {
		//Return the clause text for a complex filter in functional noun (FN) format 
		return '( the ' + pDomConcept + ' ' + pDomVarId + ' has the value ' + pFilterVarId + ' as ' + pPropName + ' ) and ( the value ' + pFilterVarId + ' ' + pFilterOperator + ' \'' + pFilterValue + '\' )';
	}

}