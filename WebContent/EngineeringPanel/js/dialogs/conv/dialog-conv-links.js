/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.conv.links = new DialogConvLinks();

function DialogConvLinks() {

	this.links = null;

	this.initialise = function() {
		gCe.msg.debug('DialogConvLinks', 'initialise');

		this.links = gEp.ui.links;
	};

	this.jsTextForEnableConversation = function() {
		return this.links.jsTextFor('gEp.dlg.conv.actions.enableConversation');
	};

	this.enableConversation = function() {
		return this.links.hyperlinkFor(this.jsTextForEnableConversation(), 'enable conversation');
	};

	this.jsTextForShowExistingConversation = function() {
		return this.links.jsTextFor('gEp.dlg.conv.actions.showExistingConversation');
	};

	this.showExistingConversation = function() {
		var jsText = this.jsTextForShowExistingConversation();
		var linkText = 'Show all conversation';
		var hoverText = 'Request and list all the existing messages in the conversation';

		return this.links.hyperlinkFor(jsText, linkText, hoverText);
	};

	this.jsTextForShowNewConversation = function() {
		return this.links.jsTextFor('gEp.dlg.conv.actions.showNewConversation');
	};

	this.showNewConversation = function() {
		var jsText = this.jsTextForShowExistingConversation();
		var linkText = 'Show new conversation';
		var hoverText = 'Request and append the new conversation messages (since the last request)';

		return this.links.hyperlinkFor(jsText, linkText, hoverText);
	};

	//jsText... only (these are triggered by events)
	this.jsTextForChangedAutoUpdate = function() {
		return this.links.jsTextFor('gEp.dlg.conv.actions.changedAutoUpdate');
	};

	this.jsTextForChangedNoseyMode = function() {
		return this.links.jsTextFor('gEp.dlg.conv.actions.changedNoseyMode');
	};

	this.jsTextForSendConversationCe = function() {
		return this.links.jsTextFor('gEp.dlg.conv.actions.sendConversationCe');
	};

	this.jsTextForChangedAutoSend = function() {
		return this.links.jsTextFor('gEp.dlg.conv.actions.changedAutoSend');
	};

}