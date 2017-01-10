/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/
gEp.dlg.ceqb.dialog = new DialogCeqbDialog();

function DialogCeqbDialog() {
	var ceqb = gEp.dlg.ceqb;

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbDialog', 'initialise');
		//Nothing needed
	};

	this.saveManualCe = function() {
		var thisForm = dijit.byId('formManualEditCe');

		if (!thisForm.isValid()) {
			gCe.msg.error('You must provide all of the required values');
		} else {
			var domCeText = dijit.byId('dlgCeText');
			var thisCeText = domCeText.get('value');

			//Hide the form
			thisForm.hide();

			//Overwrite the CE query text
			ceqb.actions.overwriteCeQueryText(thisCeText);
		}
	};

	this.cancelManualCe = function() {
		//Hide the form
		dijit.byId('formManualEditCe').hide();
	};

	this.openDialogRelDetails = function(pRel) {
		//Store the current relationship
		ceqb.currentRel = pRel;

		var formDlg = dijit.byId('formRelationshipDetails');
		var domPropList = dijit.byId('dlgRelProperty');
		var domSource = dijit.byId('dlgRelSource');
		var domTarget = dijit.byId('dlgRelTarget');

		var rpData = {
				identifier: 'id',
				label: 'name',
				items: []
		};

		var rpStore = new dojo.data.ItemFileWriteStore({
			data: rpData  
		});	

		if ((!gCe.utils.isNullOrEmpty(ceqb.latestPropList)) && (ceqb.latestPropList.length > 0)) {
			for (var i = 0; i < ceqb.latestPropList.length; i++) {
				var thisProp = ceqb.latestPropList[i];
				var qualName = thisProp._id + ' (' + thisProp.domain_name + ':' + thisProp.range_name + ')';

				rpStore.newItem( { id: thisProp._id,   name: qualName, domain: thisProp.domain_name,   range: thisProp.range_name,   style: thisProp.property_style } );
			}
		}

		domPropList.set('store', rpStore);

		if (!gCe.utils.isNullOrEmpty(pRel)) {
			//An existing relationship is being edited
			formDlg.attr('title', 'Edit existing relationship');

			//Select the correct value in the property list field
			domPropList.set('value', pRel.fullName);

			//Set the source field to a summary of the selected concept
			domSource.set('value', ceqb.model.calculateConceptSummary(pRel.source_concept));

			//Set the target field to a summary of the selected concept
			domTarget.set('value', ceqb.model.calculateConceptSummary(pRel.target_concept));
		} else {
			//A new relationship is being created
			formDlg.attr('title', 'Create new relationship');

			//Clear the property list field
			domPropList.reset();

			//Set the source field to a summary of the selected concept
			domSource.set('value', ceqb.model.calculateConceptSummary(ceqb.srcConcept));

			//Set the target field to a summary of the selected concept
			domTarget.set('value', ceqb.model.calculateConceptSummary(ceqb.tgtConcept));
		}

		formDlg.show();
	};

	this.saveDialogRelDetails = function() {
		var thisForm = dijit.byId('formRelationshipDetails');

		if (!thisForm.isValid()) {
			gCe.msg.error('You must provide all of the required values');
		} else {
			var domProperty = dijit.byId('dlgRelProperty');

			var selPropFullName = domProperty.get('value');
			var selectedProp = ceqb.model.getLatestPropertyByFullName(selPropFullName);

			if (!gCe.utils.isNullOrEmpty(selectedProp)) {
				ceqb.render.createTheRelationship(selectedProp);
			} else {
				gCe.msg.error('No selected property was detected');
			}

			//Clear the current relationship
			ceqb.currentRel = null;

			//Hide the form
			thisForm.hide();
		}
	};

	this.cancelDialogRelDetails = function() {
		//Hide the form
		dijit.byId('formRelationshipDetails').hide();

		ceqb.actions.clearSourceAndTargetConcepts();
	};

	this.dlgChangeRelProp = function() {
		var domPropDetails = dijit.byId('dlgRelPropertyDetails');
		var domProperty = dijit.byId('dlgRelProperty');
		var selPropFullName = domProperty.get('value');	
		var selectedProp = ceqb.model.getLatestPropertyByFullName(selPropFullName);
		var labelFilter = '';

		if (!gCe.utils.isNullOrEmpty(selectedProp)) {
			labelFilter = ceqb.model.labelTextForPropertyDetails(selectedProp);
		} else {
			labelFilter = '';
		}

		domPropDetails.set('value', labelFilter);
	};

	this.openDialogFilterDetails = function(pFilter) {
		ceqb.currentFilter = pFilter;

		var formDlg = dijit.byId('formFilterDetails');
		var domFilterList = dijit.byId('dlgFilterProperty');
		var domSource = dijit.byId('dlgFilterSource');
		var domVarId = dijit.byId('dlgFilterVariableId');
		var domOperator = dijit.byId('dlgFilterOperator');
		var domValue = dijit.byId('dlgFilterValue');

		var fpData = {
				identifier: 'id',
				label: 'name',
				items: []
		};

		var fpStore = new dojo.data.ItemFileWriteStore({  
			data: fpData  
		});

		domFilterList.set('store', fpStore);

		if ((!gCe.utils.isNullOrEmpty(ceqb.latestPropList)) && (ceqb.latestPropList.length > 0)) {
			for (var i = 0; i < ceqb.latestPropList.length; i++) {
				var thisProp = ceqb.latestPropList[i];
				var qualName = thisProp.property_name + ' (' + thisProp.domain_name + ')';
				fpStore.newItem( { id: thisProp._id,   name: qualName, domain: thisProp.domain_name,   range: 'value',   style: thisProp.property_style } );
			}
		}

		if (!gCe.utils.isNullOrEmpty(ceqb.currentFilter)) {
			//An existing filter is being edited
			formDlg.attr('title', 'Edit existing attribute');

			//Select the correct variable id
			domVarId.set('value', ceqb.currentFilter.variableId);

			//Select the correct value in the property list field
			domFilterList.set('value', ceqb.currentFilter.fullName);

			//Set the source field to a summary of the selected concept
			domSource.set('value', ceqb.model.calculateConceptSummary(ceqb.currentFilter.concept));

			//Set the operator and value fields
			domOperator.set('value', ceqb.currentFilter.operator);

			domValue.set('value', ceqb.currentFilter.value);
		} else {
			//A new filter is being created
			formDlg.attr('title', 'Create new attribute');

			//Clear the filter list field
			domFilterList.reset();

			//Empty the operator and value fields
			domOperator.set('value', ceqb.VAL_FILTEROP_NONE);
			domValue.set('value', '');

			//Set the source field to a summary of the selected concept
			domSource.set('value', ceqb.model.calculateConceptSummary(ceqb.currentConcept));

			//Set the variable id field to the next filter value
			domVarId.set('value', ceqb.model.calculateNextFilterId());
		}

		formDlg.show();
	};

	this.saveDialogFilterDetails = function() {
		var thisForm = dijit.byId('formFilterDetails');

		if (!thisForm.isValid()) {
			gCe.msg.error('You must provide all of the required values');
		} else {
			var domVarId = dijit.byId('dlgFilterVariableId');
			var domProperty = dijit.byId('dlgFilterProperty');
			var domOperator = dijit.byId('dlgFilterOperator');
			var domValue = dijit.byId('dlgFilterValue');

			var selPropFullName = domProperty.get('value');
			var selectedProp = ceqb.model.getLatestPropertyByFullName(selPropFullName);

			if (gCe.utils.isNullOrEmpty(ceqb.currentFilter)) {
				//A new filter is being created
				var filterDetails = {};
				filterDetails.variableId = domVarId.get('value');
				filterDetails.fullName = selPropFullName;
				filterDetails.propName = selectedProp.property_name;
				filterDetails.propFormat = selectedProp.property_style;
				filterDetails.operator = domOperator.get('value');
				filterDetails.value = domValue.get('value');

				//Continue the processing (to add the filter)
				var newFilter = ceqb.model.createFilterOnConcept(ceqb.currentConcept, filterDetails, ceqb.VAL_TYPE_PREMISE);
				ceqb.render.addFilterToQueryCanvas(newFilter);	
			} else {
				//An existing filter is being updated
				var newId = domVarId.get('value');

				if (newId != ceqb.currentFilter.variableId) {
					//The variable id has changed
					ceqb.model.renameFilterVariable(ceqb.currentFilter, newId);
				}

				ceqb.currentFilter.fullName = selPropFullName;
				ceqb.currentFilter.prop_name = selectedProp.property_name;
				ceqb.currentFilter.prop_format = selectedProp.property_style;
				ceqb.currentFilter.operator = domOperator.get('value');
				ceqb.currentFilter.value = domValue.get('value');
				ceqb.drawing.drawUpdatedFilterLabel(ceqb.currentFilter);
				ceqb.actions.filterCountRequest(ceqb.currentFilter);
			}

			//Clear the current concept and filter
			ceqb.currentConcept = null;
			ceqb.currentFilter = null;

			//Hide the form
			thisForm.hide();

			//Recalculate the CE query text
			ceqb.actions.recalculateCeQueryText(ceqb.GENMODE_NORMAL);
		}
	};

	this.cancelDialogFilterDetails = function() {
		//Hide the form
		dijit.byId('formFilterDetails').hide();

		//Clear the current concept
		ceqb.currentConcept = null;
	};

	this.dlgChangeFilterProp = function() {
		var domPropDetails = dijit.byId('dlgFilterPropertyDetails');
		var domProperty = dijit.byId('dlgFilterProperty');
		var selPropFullName = domProperty.get('value');	
		var selectedProp = ceqb.model.getLatestPropertyByFullName(selPropFullName);
		var labelFilter = '';
		
		if (!gCe.utils.isNullOrEmpty(selectedProp)) {
			labelFilter = ceqb.model.labelTextForPropertyDetails(selectedProp);
		} else {
			labelFilter = '';
		}

		domPropDetails.set('value', labelFilter);
	};

	this.openManualCeForm = function() {
		var formDlg = dijit.byId('formManualEditCe');
		var domCeText = dijit.byId('dlgCeText');

		//Set the CE text field
		domCeText.set('value', ceqb.ceQueryText);

		formDlg.show();
	};

}
