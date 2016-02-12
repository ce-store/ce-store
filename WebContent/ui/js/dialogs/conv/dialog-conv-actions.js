/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.conv.actions = new DialogConvActions();

function DialogConvActions() {

	var DOM_AUTOSEND = 'autoSend';
	var DOM_AUTOUPDATE = 'autoUpdate';
	var DOM_NOSEY = 'noseyMode';

	this.handler = null;

	this.initialise = function() {
		gCe.msg.debug('DialogConvActions', 'initialise');

		this.handler = gEp.dlg.conv.handler;
	};

	this.enableConversation = function() {
		if (gEp.isCeStoreEmpty()) {
			gCe.msg.alert('The CE Store is empty.  Please load a sentence set before enabling the conversation pane.');
		} else {
			var senUrl = 'ce-store/ce/conversation/cmd/load_conv_and_card_models.cecmd';
			var cbf = function(pResponseObject, pUserParms) { 
							gEp.handler.sentences.reportLoadResultsAndRefresh(pResponseObject, pUserParms);
							gEp.dlg.conv.actions.updateAfterLoad(true);
						};

			gEp.handler.actions.processCommandsRelative(senUrl, 'conversation pane', cbf);
		}
	};

	//Invoked when the 'send message' button on the conversation tab is pressed
	this.sendConversationCe = function() {
		this.handler.sendConversationCe(gEp.dlg.conv.domConversationFieldContents());
	};

	//Invoked as callback when addSentences REST call to save Conversation CE is completed
	this.acknowledgeMessageSent = function() {
		gEp.dlg.conv.clearConversationField();
		this.showNewConversation();
	};

	this.showExistingConversation = function() {
		this.handler.showExistingConversation();
	};

	this.showNewConversation = function() {
		if (gEp.dlg.conv.handler.getLastRequestTime(gEp.dlg.conv.handler.getSelectedCeStore()) === -1) {
			this.showExistingConversation();
		} else {
			gEp.dlg.conv.handler.showNewConversation();
		}
	};

	this.changedAutoUpdate = function() {
		var domAutoUpdate = dijit.byId(DOM_AUTOUPDATE);
		var autoUpdate = domAutoUpdate.get('value');

		this.handler.setAutoUpdate(autoUpdate === 'true');

		gEp.dlg.conv.toggleAutoUpdate();
	};

	this.changedAutoSend = function() {
		var domAutoSend = dijit.byId(DOM_AUTOSEND);
		var autoSend = domAutoSend.get('value');
		
		this.handler.setAutoSend(autoSend === 'true');
	};

	this.changedNoseyMode = function() {
		var domNoseyMode = dijit.byId(DOM_NOSEY);
		var noseyMode = domNoseyMode.get('value');

		this.handler.setNoseyMode(noseyMode === 'true');

		this.handler.renderConversation();
	};

	this.replyTo = function(pMsgId) {
		gEp.dlg.conv.appendToConversationField('[' + pMsgId + ']\n');
	};

	this.updateAfterLoad = function(pOverride) {
		gEp.dlg.conv.showOrHideConversationDetails(pOverride);
	};

}