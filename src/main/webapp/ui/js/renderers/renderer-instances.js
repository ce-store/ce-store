/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.instances = new RendererInstances();

function RendererInstances() {
	var TYPENAME = 'instance';
	
	this.initialise = function() {
		gCe.msg.debug('RendererInstances', 'initialise');
		//Nothing needed
	};

	this.renderShadowInstanceList = function(pInstList) {
		var html = '';

		if (!gCe.utils.isNullOrEmpty(pInstList)) {
			var list = [];

			for (var key in pInstList) {
				var thisInst = pInstList[key];

				list.push(gEp.ui.links.instanceDetails(thisInst._id));
			}
			
			html += 'The following ' + pInstList.length + ' shadow instances exist:';
			html += gEp.ui.htmlUnorderedListFor(list);
		} else {
			html += 'There are no shadow instances';
		}

		gEp.ui.pane.general.updateWith(html, true);
	};

	this.renderUnreferencedInstanceList = function(pInstList) {
		var html = '';

		if (!gCe.utils.isNullOrEmpty(pInstList)) {
			var list = [];

			for (var key in pInstList) {
				var thisInst = pInstList[key];

				list.push(gEp.ui.links.instanceDetails(thisInst._id));
			}

			html += 'The following ' + pInstList.length + ' unreferenced instances exist:';
			html += gEp.ui.htmlUnorderedListFor(list);
		} else {
			html += 'There are no shadow instances';
		}

		gEp.ui.pane.general.updateWith(html, true);
	};

	this.renderDiverseConceptInstanceList = function(pDciList) {
		var html = '';

		if (!gCe.utils.isNullOrEmpty(pDciList)) {
			var list = [];

			for (var idxDci in pDciList) {
				var thisDci = pDciList[idxDci];
				var linkText = gEp.ui.links.instanceDetails(thisDci.instance_name);
				
				linkText += ' (';
				var sep = '';
				for (var idxCon in thisDci.concept_names) {
					var thisConName = thisDci.concept_names[idxCon];
					
					linkText += sep + gEp.ui.links.conceptDetails(thisConName);
					sep = ', ';
				}
				linkText += ' )';

				list.push(linkText);
			}
			
			html += 'The following ' + pDciList.length + ' diverse concept instances exist:';
			html += gEp.ui.htmlUnorderedListFor(list);
		} else {
			html += 'There are no diverse concept instances';
		}

		gEp.ui.pane.general.updateWith(html, true);
	};

	this.renderInstanceList = function(pInstList, pParms) {
		var html = this.htmlForInstanceList(pInstList, pParms);
		gEp.ui.pane.general.updateWith(html, true);
	};

	this.renderReferenceList = function(pRefList, pParms) {
		var html = '';
		var hdrs = [ '#', 'instance name', 'property name' ];
		var rows = [];
		var ctr = 0;
		
		for (var idx in pRefList) {
			var thisRef = pRefList[idx];
			
			var thisRow = [];
			thisRow.push(++ctr);
			thisRow.push(gEp.ui.links.instanceDetails(thisRef.instance_name));
			thisRow.push(gEp.ui.links.propertyDetails(thisRef.property_name));

			rows.push( thisRow );
		}

		html += htmlForReferencesPaneHeader(pParms, rows.length);
		html += gEp.ui.htmlTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);

		gEp.ui.pane.general.updateWith(html, true);
	};

	this.htmlForInstanceList = function(pInstList, pParms) {
		var html = '';
		var hasActions = gEp.hasListItemActionsFor(TYPENAME);
		var stdHdrs = standardHeaders();
		var propHdrs = computeHeadersFor(pInstList);
		var allHdrs = mergeHeaders(stdHdrs, propHdrs);
		var rows = computeRowsFor(pInstList, propHdrs, hasActions);

		if (hasActions) {
			allHdrs.push('Actions');
		}

		html += htmlForStandardPaneHeader(pParms, rows.length);
		html += gEp.ui.htmlTableFor(allHdrs, rows, gEp.ui.DEFAULT_STYLE);

		return html;
	};
	
	function htmlForRefreshLink(pParms) {
		var result = null;
		var linkText = null;

		if (pParms.exact) {
			linkText = gEp.ui.links.conceptInstanceListExact(pParms.concept_name, 'refresh');
		} else {
			linkText = gEp.ui.links.conceptInstanceList(pParms.concept_name, 'refresh');
		}
		
		if (pParms.since != null) {
			gCe.msg.warning('Since mode in instance list refresh is not yet supported.  All instances will be shown.');
		}

		result = ' (' + linkText + ')';

		return result;
	}

	this.renderInstanceDetails = function(pInst) {
		//TODO: Complete this
		var html = 'Showing instance details for ' + gEp.ui.links.instanceDetails(pInst._id) + ':<br/><br/>';

		var hdrs = [ 'name', 'direct concepts', 'inherited concepts' ];

		var rows = [
			gEp.ui.links.instanceDetails(pInst._id),
			htmlForConceptNames(pInst.direct_concept_names),
			htmlForConceptNames(pInst.inherited_concept_names)
		];

		htmlForProperties(pInst, hdrs, rows);

		if (pInst.primary_sentence_count === 0) {
			rows.push('0');
		} else {
			rows.push(gEp.ui.links.instancePrimarySentences(pInst._id, pInst.primary_sentence_count, null, false));
		}

		if (pInst.secondary_sentence_count === 0) {
			rows.push('0');
		} else {
			rows.push(gEp.ui.links.instanceSecondarySentences(pInst._id, pInst.secondary_sentence_count, null, false));
		}
		
		var totalSenCount = pInst.primary_sentence_count + pInst.secondary_sentence_count;
		rows.push(gEp.ui.links.instanceAllSentences(pInst._id, totalSenCount, null, false));

		hdrs.push('primary sentences');
		hdrs.push('secondary sentences');
		hdrs.push('all sentences');
		
		if (gEp.hasDetailActionsFor(TYPENAME)) {
			var actList = gEp.getDetailActionsListFor(TYPENAME, pInst);
			rows.push(actList);
			hdrs.push('Actions');
		}

		html += gEp.ui.htmlVerticalTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);
		gEp.ui.pane.entity.updateWith(html, true);
	};

	function htmlForConceptNames(pConList) {
		var linkList = [];

		for (var key in pConList) {
			var thisConName = pConList[key];
			linkList.push(gEp.ui.links.conceptDetails(thisConName));
		}

		return gEp.ui.htmlUnorderedListFor(linkList);
	}

	function htmlForProperties(pInst, pHdrs, pRows) {
		for (var key in pInst.property_values) {
			var thisPropValList = pInst.property_values[key];
			var isObjectProp = pInst.property_types[key] === 'O';
			var isSingleValue = false;
			var actualSingleValue = null;
			var cePropName = key;
			
			pHdrs.push(cePropName);

			if (gCe.utils.isArray(thisPropValList)) {
				//This is an array, so may have more than one value
				isSingleValue = (thisPropValList.length === 1);
				
				if (isSingleValue) {
					actualSingleValue = thisPropValList[0];
				}
			} else {
				//This is not an array so is single value
				isSingleValue = true;
				actualSingleValue = thisPropValList;
			}

			if (isSingleValue) {
				//Single value for this property
				if (isObjectProp) {
					//This is an object property so a link is needed
					var linkedVal = gEp.ui.links.instanceDetails(actualSingleValue);
					pRows.push(linkedVal);
				} else {
					//This is a datatype property so no link is needed
					pRows.push(gEp.ui.renderDatatypeValue(actualSingleValue, cePropName, pInst));
				}
			} else {
				//Multiple values for this property
				var valList = [];

				for (var i = 0; i < thisPropValList.length; i++) {
					var thisVal = thisPropValList[i];

					if (isObjectProp) {
						//This is an object property so a link is needed
						var linkedVal = gEp.ui.links.instanceDetails(thisVal);
						valList.push(linkedVal);
					} else {
						//This is a datatype property so no link is needed
						valList.push(gEp.ui.renderDatatypeValue(thisVal, cePropName, pInst));
					}
				}
				pRows.push(valList);
			}
		}
	}

	function htmlForStandardPaneHeader(pParms, pLen) {
		var conLink = gEp.ui.links.conceptDetails(pParms.concept_name);
		var hdrText = gEp.ui.htmlEmphasise('The following %01 ' + conLink + ' instances are listed.', pLen);
		var subText = '';

		if (pParms.exact) {
			subText = 'Showing exact instances only (no children)';
		} else {
			subText = 'Showing all instances, including children';
		}

		if (!gCe.utils.isNullOrEmpty(pParms.since)) {
			subText += ', created since ' + gEp.ui.formattedDateTimeStringFor(pParms.since);
		}

		subText += htmlForRefreshLink(pParms);

		return gEp.ui.htmlPaneHeaderFor(hdrText, subText);
	}

	function htmlForReferencesPaneHeader(pParms, pLen) {
		var tgtInstLink = gEp.ui.links.instanceDetails(pParms.in_reference_to);
		var hdrText = gEp.ui.htmlEmphasise('The following %01 instances refer to the instance %02.', pLen, tgtInstLink);

		return gEp.ui.htmlPaneHeaderFor(hdrText, null);
	}

	function standardHeaders() {
		var hdrList = [];

		hdrList.push('#');
		hdrList.push('instance name');
		hdrList.push('direct concept names');
		hdrList.push('creation date');

		return hdrList;
	}

	function computeHeadersFor(pInstList) {
		var hdrObj = {};
		var hdrList = [];

		for (var iKey in pInstList) {
			var thisInst = pInstList[iKey];

			for (var pKey in thisInst.property_values) {
				hdrObj[pKey] = null;
			}
		}

		//Compute all the other headers
		for (var hKey in hdrObj) {
			hdrList.push(hKey);
		}

		return hdrList;
	}

	function mergeHeaders(pStdHdrs, pPropHdrs) {
		var result = [];

		for (var key in pStdHdrs) {
			result.push(pStdHdrs[key]);
		}

		for (var key in pPropHdrs) {
			result.push(pPropHdrs[key]);
		}

		return result;
	}

	function computeRowsFor(pInstList, pHdrs, pHasActions) {
		var rows = [];
		var ctr = 0;

		for (var iKey in pInstList) {
			var thisRow = [];
			var thisInst = pInstList[iKey];
			var propTypes = thisInst.property_types;

			//Add the standard values
			thisRow.push(++ctr);
			thisRow.push(gEp.ui.links.instanceDetails(thisInst._id));
			thisRow.push(htmlForDirectConceptNames(thisInst));
			thisRow.push(gEp.ui.formattedDateTimeStringFor(thisInst._created));

			for (var hKey in pHdrs) {
				var thisPropName = pHdrs[hKey];
				var thisPropVals = thisInst.property_values[thisPropName];
				var thisPropText = null;

				if (!gCe.utils.isNullOrEmpty(thisPropVals)) {
					if (gCe.utils.isArray(thisPropVals)) {
						if (thisPropVals.length === 1) {
							if (isObjectProperty(propTypes, thisPropName)) {
								thisPropText = gEp.ui.links.instanceDetails(thisPropVals[0]);
							} else {
								thisPropText = gEp.ui.renderDatatypeValue(thisPropVals[0], thisPropName, thisInst);
							}
						} else {
							var linkList = [];

							for (var vKey in thisPropVals) {
								var thisPropVal = thisPropVals[vKey];
								if (isObjectProperty(propTypes, thisPropName)) {
									linkList.push(gEp.ui.links.instanceDetails(thisPropVal));
								} else {
									linkList.push(gEp.ui.renderDatatypeValue(thisPropVal, thisPropName, thisInst));
								}
							}

							thisPropText = gEp.ui.htmlUnorderedListFor(linkList);
						}
					} else {
						if (isObjectProperty(propTypes, thisPropName)) {
							thisPropText = gEp.ui.links.instanceDetails(thisPropVals);
						} else {
							thisPropText = gEp.ui.renderDatatypeValue(thisPropVals, thisPropName, thisInst);
						}
					}
				} else {
					thisPropText = '';
				}

				thisRow.push(thisPropText);				
			}

			if (pHasActions) {
				var actList = gEp.getListItemActionsListFor(TYPENAME, thisInst);
				thisRow.push(actList);
			}

			rows.push(thisRow);
		}

		return rows;
	}
	
	function isObjectProperty(pPropTypes, pPropName) {
		return (pPropTypes[pPropName] === 'O');
	}

	function htmlForDirectConceptNames(pInst) {
		var html = null;

		if (pInst.direct_concept_names.length > 1) {
			var linkList = [];

			for (var key in pInst.direct_concept_names) {
				var thisDcn = pInst.direct_concept_names[key];
				linkList.push(gEp.ui.links.conceptDetails(thisDcn));
			}

			html = gEp.ui.htmlUnorderedListFor(linkList);
		} else {
			html = gEp.ui.links.conceptDetails(pInst.direct_concept_names[0]);
		}

		return html;

	}

}
