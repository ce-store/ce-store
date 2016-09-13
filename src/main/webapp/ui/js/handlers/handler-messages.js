/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.messages = new HandlerMessages();

function HandlerMessages() {
	var ren = null;		//This is set during initialise

	var iAlerts = {};

	this.initialise = function() {
		gCe.msg.debug('HandlerMessages', 'initialise');

		ren = gEp.renderer.messages;
	};

	function clearAllAlertLists() {
		iAlerts.allDebugs = [];
		iAlerts.allInfos = [];
		iAlerts.allWarnings = [];
		iAlerts.allErrors = [];
	}

	this.showAllStats = function(pResponse, pUserParms) {
		//Only process if the format is JSON
		if (pUserParms.accept === gCe.FORMAT_JSON) {
			if (gCe.utils.hasStats(pResponse)) {
				showPopupMessages(pResponse);
				updateStatusBar(pResponse);
				showAlerts(pResponse, pUserParms);
			} else {
				ren.htmlDefaultHeaderMessage();
			}
		}
	};

	this.showApiErrors = function(pErrorList) {
		iAlerts.allErrors = gCe.utils.mergeArrays(iAlerts.allErrors, pErrorList);
		ren.updateErrorsPane(iAlerts.allErrors);
	};

	this.hasErrors = function(pResponse) {
		return (pResponse != null) && (pResponse.alerts != null) && (!gCe.utils.isNullOrEmpty(pResponse.alerts.errors));
	};

	this.hasWarnings = function(pResponse) {
		return (pResponse != null) && (pResponse.alerts != null) && (!gCe.utils.isNullOrEmpty(pResponse.alerts.warnings));
	};

	function processAlertsFrom(pResponse) {
		if (!gCe.utils.isNullOrEmpty(pResponse.alerts)) {
			iAlerts.allErrors = gCe.utils.mergeArrays(iAlerts.allErrors, pResponse.alerts.errors);
			iAlerts.allWarnings = gCe.utils.mergeArrays(iAlerts.allWarnings, pResponse.alerts.warnings);
			iAlerts.allInfos = gCe.utils.mergeArrays(iAlerts.allInfos, pResponse.alerts.infos);
			iAlerts.allDebugs = gCe.utils.mergeArrays(iAlerts.allDebugs, pResponse.alerts.debugs);
		} else {
			gCe.msg.error('Standard alerts not detected in response');
		}
	};

	function showPopupMessages(pResponse) {
		//This is a message that is intended to be directly reported to the user
		var thisMsgText = '';

		if (!gCe.utils.isNullOrEmpty(pResponse.message)) {
			for (var key in pResponse.message) {
				thisMsgText += pResponse.message[key] + '\n';
			}

			gCe.msg.alert(thisMsgText);
		}
	};

	function updateStatusBar(pResponse) {
		if (gCe.utils.isNullOrEmpty(pResponse.stats)) {
			gCe.msg.error('Standard statistics not detected in response');
		} else {
			ren.htmlStatusMessage(pResponse.stats);
		}
	};

	function showAlerts(pResponse, pUserParms) {
		if ((pUserParms != null) && (pUserParms.clearAlerts !== false)) {
			clearAllAlertLists();
		}

		processAlertsFrom(pResponse);

		ren.updateDebugsPane(iAlerts.allDebugs);
		ren.updateInfosPane(iAlerts.allInfos);
		ren.updateWarningsPane(iAlerts.allWarnings);
		ren.updateErrorsPane(iAlerts.allErrors);
	};

}
