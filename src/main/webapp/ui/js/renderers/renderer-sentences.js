/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.sentences = new RendererSentences();

function RendererSentences() {

	this.initialise = function() {
		gCe.msg.debug('RendererSentences', 'initialise');
		//Nothing needed
	};

	this.renderSimpleSentenceList = function(pSenList, pUserParms) {
		var html = '';

		html += calculateSimpleHeaderUsing(pUserParms, pSenList.length);
		html += htmlSimpleTableFor(pSenList.sort(sortSentencesById));

		gEp.ui.pane.sentence.updateWith(html, true);
	};

	this.renderFullSentenceList = function(pSenList, pUserParms) {
		var html = '';

		html += calculateFullHeaderUsing(pUserParms, pSenList.length);
		html += htmlFullTableFor(pSenList.sort(sortSentencesById));

		gEp.ui.pane.general.updateWith(html, true);
	};

	this.renderSentenceDetails = function(pSen, pUserParms) {
		var senLink = gEp.ui.links.sentenceDetails(pSen._id);
		var html = 'Showing sentence details for ' + senLink + ':';
		html += '<br/><br/>';
		var hdrs = [ 'id', 'type', 'validity', 'created', 'source', 'CE text', 'qualifiers' ];

		var rows = [
			senLink,
			pSen.sen_type,
			pSen.validity,
			gEp.ui.formattedDateTimeStringFor(pSen._created),
			gEp.ui.links.sourceDetails(pSen.source._id),
			formattedCeText(pSen),
			''  //TODO: Handle qualifiers properly here
		];

		html += gEp.ui.htmlVerticalTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);

		html += '<br/><br/>';
		html += '<b>Actions:</b>';
		html += gEp.ui.htmlUnorderedListFor(getSentenceActionsFor(pSen));

		gEp.ui.pane.entity.updateWith(html, true);
	};

	function getSentenceActionsFor(pSen) {
		var actList = [];

		actList.push(gEp.ui.links.sentenceDelete(pSen, 'Delete this sentence'));

		return actList;
	}

	function htmlSimpleTableFor(pSortedSens) {
		var hdrs = [ 'Id', 'Type', 'CE text' ];
		var rows = calculateSimpleSentenceRowsFor(pSortedSens);

		return gEp.ui.htmlTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);
	}

	function htmlFullTableFor(pSortedSens) {
		var hdrs = [ 'Id', 'Type', 'Validity', 'Created', 'Source', 'CE text', 'Qualifications' ];
		var rows = calculateFullSentenceRowsFor(pSortedSens);

		return gEp.ui.htmlTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);
	}

	function calculateSimpleSentenceRowsFor(pSenList) {
		var rows = [];
	
		for (var key in pSenList) {
			rows.push(createSimpleRowFor(pSenList[key]));
		}

		return rows;
	}

	function calculateFullSentenceRowsFor(pSenList) {
		var rows = [];
	
		for (var key in pSenList) {
			rows.push(createFullRowFor(pSenList[key]));
		}

		return rows;
	}

	function calculateSimpleHeaderUsing(pUserParms, pSenListSize) {
		var html = calculateHeaderUsing(pUserParms, pSenListSize);

		html += '<br/><br/>';
		html += 'This is the summary view. ';
		html += calculateDetailsLinkUsing(pUserParms);
		html += ' to see full details for this list of sentences';
		html += '<br/><br/>';

		return html;
	}

	function calculateFullHeaderUsing(pUserParms, pSenListSize) {
		var html = calculateHeaderUsing(pUserParms, pSenListSize);

		html += '<br/><br/>';

		return html;
	}

	function calculateHeaderUsing(pUserParms, pSenListSize) {
		var html = '';

		if (!gCe.utils.isNullOrEmpty(pUserParms.source_id)) {
			//Source sentences
			html += 'The following ' + pSenListSize + ' sentences are defined in the source ';
			html += gEp.ui.links.sourceDetails(pUserParms.source_id);
			html += ':';
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.type_qualifier)) {
			//Qualified (type) sentences (e.g. model, fact, rule etc)
			html += 'The following ' + pSenListSize + ' ' + pUserParms.type_qualifier + ' sentences are loaded:';
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.validity_qualifier)) {
			//Qualified (validity) sentences (e.g. valid, invalid)
			html += 'The following ' + pSenListSize + ' ' + pUserParms.validity_qualifier + ' sentences are loaded:';
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.inst_id)) {
			//Instance sentences
			var instLink = gEp.ui.links.instanceDetails(pUserParms.inst_id);
			html += 'The following ' + pSenListSize + ' (' + pUserParms.context + ') sentences relate to the instance ' + instLink +  ':';
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.con_name)) {
			//Concept sentences
			var conLink = gEp.ui.links.conceptDetails(pUserParms.con_name);
			html += 'The following ' + pSenListSize + ' (' + pUserParms.context + ') sentences relate to the concept ' + conLink +  ':';
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.model_name)) {
			//Model sentences
			var modelLink = gEp.ui.links.modelDetails(pUserParms.model_name);
			html += 'The following ' + pSenListSize + ' sentences relate to the model ' + modelLink +  ':';
		} else {
			//All sentences
			html += 'The following ' + pSenListSize + ' sentences are all the sentences loaded in the CE Store';
		}

		return html;
	}

	function calculateDetailsLinkUsing(pUserParms) {
		var result = null;

		if (!gCe.utils.isNullOrEmpty(pUserParms.source_id)) {
			//Source sentences
			result = gEp.ui.links.sourceSentences(pUserParms.source_id, 'Click here', true);
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.type_qualifier)) {
			//Qualified (type) sentences (e.g. model, fact, rule etc)
			result = gEp.ui.links.listQualifiedSentences(pUserParms.type_qualifier, null, true, 'Click here', true);
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.validity_qualifier)) {
			//Qualified (validity) sentences (e.g. valid, invalid)
			result = gEp.ui.links.listQualifiedSentences(null, pUserParms.validity_qualifier, true, 'Click here', true);
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.inst_id)) {
			//Instance sentences
			if (pUserParms.context === 'primary') {
				result = gEp.ui.links.instancePrimarySentences(pUserParms.inst_id, null, 'Click here', true);
			} else if (pUserParms.context === 'secondary') {
				result = gEp.ui.links.instanceSecondarySentences(pUserParms.inst_id, null, 'Click here', true);
			} else {
				result = gEp.ui.links.instanceAllSentences(pUserParms.inst_id, null, 'Click here', true);
			}
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.con_name)) {
			//Concept sentences
			if (pUserParms.context === 'primary') {
				result = gEp.ui.links.conceptPrimarySentences(pUserParms.con_name, null, 'Click here', true);
			} else if (pUserParms.context === 'secondary') {
				result = gEp.ui.links.conceptSecondarySentences(pUserParms.con_name, null, 'Click here', true);
			} else {
				result = gEp.ui.links.conceptAllSentences(pUserParms.con_name, null, 'Click here', true);
			}
		} else if (!gCe.utils.isNullOrEmpty(pUserParms.model_name)) {
			//Model sentences
			result = gEp.ui.links.modelSentences(pUserParms.model_name, 'Click here', true);
		} else {
			result = '???';
		}

		return result;
	}

	function createSimpleRowFor(pSen) {
		var rowVals = [];

		rowVals.push(gEp.ui.links.sentenceDetails(pSen._id, pSen.ce_text));
		rowVals.push(pSen.sen_type);
		rowVals.push(formattedCeText(pSen) + ' ' + gEp.ui.links.sentenceDelete(pSen));

		return rowVals;
	}

	function createFullRowFor(pSen) {
		var rowVals = [];

		rowVals.push(gEp.ui.links.sentenceDetails(pSen._id, pSen.ce_text));
		rowVals.push(pSen.sen_type);
		rowVals.push(pSen.validity);
		rowVals.push(gEp.ui.formattedDateTimeStringFor(pSen._created));
		rowVals.push(gEp.ui.links.sourceDetails(pSen.source_id));
		rowVals.push(formattedCeText(pSen) + ' ' + gEp.ui.links.sentenceDelete(pSen));
		rowVals.push('');	//TODO: Handle qualifiers properly here

		return rowVals;
	}

	function sortSentencesById(a, b) {
		var aVal = parseInt(a._id.replace('sen_', ''));
		var bVal = parseInt(b._id.replace('sen_', ''));
		return (aVal - bVal);
	};

	function formattedCeText(pSen) {
		var fragList = pSen.ce_structured_text;
		var result = '';
		var spaceChar = ' ';

		if (!gCe.utils.isNullOrEmpty(fragList)) {
			var justHadQuote = false;
			
			for (key in fragList) {
				var thisFrag = fragList[key];

				if (thisFrag.type === 'normal') {
					result += gCe.utils.htmlFormat(thisFrag.text);
				} else if (thisFrag.type === 'open quote') {
					result += spaceChar + thisFrag.text;
				} else if (thisFrag.type === 'close quote') {
					result += thisFrag.text + spaceChar;
				} else if (thisFrag.type === 'annotation') {
					result += gCe.utils.htmlFormat(thisFrag.annotation_name);
					result += spaceChar;

					if (thisFrag.annotation_value_type === 'conceptual model') {
						result += gEp.ui.links.modelDetails(thisFrag.annotation_value);
					} else {
						result += gCe.utils.htmlFormat(thisFrag.annotation_value);
					}
				} else if (thisFrag.type === 'instance') {
					var instLink = gEp.ui.links.instanceDetails(thisFrag.instance_name);

					if (justHadQuote) {
						result += instLink;
					} else {
						result += spaceChar + instLink + spaceChar;
					}
				} else if (thisFrag.type === 'concept') {
					result += spaceChar;
					result += gEp.ui.links.conceptDetails(thisFrag.concept_name);
					result += spaceChar;
				} else if (thisFrag.type === 'property') {
					result += spaceChar;
					result += gEp.ui.links.propertyDetails(thisFrag.property_name, thisFrag.property_domain, thisFrag.property_range);
					result += spaceChar;
				} else if (thisFrag.type === 'pattern_name') {
					result += gEp.ui.links.ruleDetails(thisFrag.pattern_name);
				} else if (thisFrag.type === 'separator') {
					if (thisFrag.text != null) {
						result += spaceChar;
						result += thisFrag.text;
					}
					result += '<br/>&nbsp;&nbsp;&nbsp;';
				} else if (thisFrag.type === 'separator_patternname') {
					result += '<br/>';
				} else if (thisFrag.type === 'rationale_start') {
					result += '<br/>';
					result += thisFrag.text;
					result += '<br/>&nbsp;&nbsp;&nbsp;';
				} else if (thisFrag.type === 'terminator') {
					result = result.trim() + thisFrag.text;
				} else {
					gCe.msg.debug('Unhandled CE fragment', 'formattedCeText', [thisFrag]);

					result += '*' + thisFrag.type + '*';
				}

				justHadQuote = (thisFrag.type === 'open quote');
			}
		} else {
			result = '***UNFORMATTED***<BR/>' + pSen.ce_text;
		}

		return result;
	}

}
