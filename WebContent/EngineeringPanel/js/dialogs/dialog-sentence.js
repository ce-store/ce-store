/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.sentence = new DialogSentence();

function DialogSentence() {
	var DEFAULT_CMDURL = 'CeStore/ce/medicine/cmd/med_load.cecmd';

	var iLastUserCmdUrl = null;

	this.initialise = function() {
		gCe.msg.debug('DialogSentence', 'initialise');
		//Nothing needed
	};

	this.loadNewSentenceSet = function(pUrl, pName) {
		var response = confirm('This will empty the contents of the CE store and all current data will be lost.  Are you sure?');

		if (response) {
			this.processCommandsRelative(pUrl, pName);
		}
	};

	this.useCeForInstance = function(pInstId, pConName) {
		var tgtConName = null;
		
		if (pConName != null) {
			var conParts = pConName.split(',');
			tgtConName = conParts[0];
		} else {
			tgtConName = 'thing';
		}

		var ceText = 'the ' + tgtConName + ' \'' + pInstId + '\' is...';

		gEp.handler.sentences.updateAddCeFieldWith(ceText);
	};

	this.processCommandsRelative = function(pUrl, pName) {
		if (gCe.utils.isNullOrEmpty(pUrl)) {
			gCe.msg.error('You must specify a relative URL');
		} else {
			gEp.handler.actions.processCommandsRelative(pUrl, pName);
		}
	};

	this.processCommandsAbsolute = function(pUrl, pSrcName) {
		var actualUrl = null;

		if (gCe.utils.isNullOrEmpty(pUrl)) {
			initialiseLastUserCmdUrl();
			actualUrl = prompt ('Please specify the url which contains your CE command statements', iLastUserCmdUrl);
		} else {
			actualUrl = pUrl;
		}

		if (!gCe.utils.isNullOrEmpty(actualUrl)) {
			iLastUserCmdUrl = actualUrl;
			gEp.handler.actions.processCommandsAbsolute(actualUrl, pSrcName);
		}
	};

	function initialiseLastUserCmdUrl() {
		if (iLastUserCmdUrl === null) {
			iLastUserCmdUrl = gEp.currentServerAddress + DEFAULT_CMDURL;
		}
	}

}