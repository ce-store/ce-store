/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.ceqb.links = new DialogCeqbLinks();

function DialogCeqbLinks() {
	this.links = null;

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbLinks', 'initialise');

		this.links = gEp.ui.links;
	};

	this.clearCanvas = function() {
		return this.links.hyperlinkFor(this.jsTextForClearCanvas(), 'Clear query canvas');
	};

	this.jsTextForClearCanvas = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.actions.clearCanvas');
	};

	this.validateQueryOrRule = function() {
		return this.links.hyperlinkFor(this.jsTextForValidateQueryOrRule(), 'Validate query/rule');
	};

	this.jsTextForValidateQueryOrRule = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.actions.validateQueryOrRule');
	};

	this.executeAsQuery = function() {
		return this.links.hyperlinkFor(this.jsTextForExecuteAsQuery(), 'query');
	};

	this.jsTextForExecuteAsQuery = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.actions.executeDrawnQuery');
	};

	this.executeAsRule = function() {
		return this.links.hyperlinkFor(this.jsTextForExecuteAsRule(), 'rule');
	};

	this.jsTextForExecuteAsRule = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.actions.executeDrawnRule');
	};

	this.saveQueryOrRule = function() {
		return this.links.hyperlinkFor(this.jsTextForSaveQueryOrRule(), 'Save query/rule');
	};

	this.jsTextForSaveQueryOrRule = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.actions.saveQueryOrRule');
	};



	//jsText... only (these are triggered by events)
	this.jsTextForChangedFilterProp = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.dlgChangeFilterProp');
	};

	this.jsTextForSaveDialogFilterDetails = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.saveDialogFilterDetails');
	};

	this.jsTextForCancelDialogFilterDetails = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.cancelDialogFilterDetails');
	};

	this.jsTextForChangedRelProp = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.dlgChangeRelProp');
	};

	this.jsTextForChangedRelProp = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.dlgChangeRelProp');
	};

	this.jsTextForSaveDialogRelDetails = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.saveDialogRelDetails');
	};

	this.jsTextForCancelDialogRelDetails = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.cancelDialogRelDetails');
	};

	this.jsTextForSaveManualCe = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.saveManualCe');
	};

	this.jsTextForCancelManualCe = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.dialog.cancelManualCe');
	};

	this.jsTextForEditedQueryName = function() {
		return this.links.jsTextFor('gEp.dlg.ceqb.actions.editedQueryName');
	};

}