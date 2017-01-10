/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.conv = new DialogConv();

function DialogConv() {

	var DOM_CONVTEXT = 'convText';
	var DOM_LOCNAME = 'locName';
	var DOM_CONVBC1 = 'convBc';
	var DOM_CONVENABLE1 = 'convEnable';

	var INTERVAL_AUTOUPDATE = 5000;

	var iTimeEvent = null;
	var isTextCe = false;
	this.handler = null;
	
	this.initialise = function() {
		gCe.msg.debug('DialogConv', 'initialise');

		this.actions.initialise();
		this.handler.initialise();
		this.links.initialise();
		
		this.handler = gEp.dlg.conv.handler;
	};

	this.initialiseConvEnvironment = function() {
		this.showOrHideConversationDetails();
	};

	this.isTextCe = function() {
		return isTextCe;
	};

	this.setIsTextCe = function(pBoolean) {
		isTextCe = pBoolean;
	};
	
	this.autoValidateCe = function() {
		var cbf = function(pValid) { gEp.dlg.conv.setIsTextCe(pValid); };
		gEp.handler.sentences.autoValidateCe(DOM_CONVTEXT, '<font color="red">Will be submitted as valid Controlled English...</font>', '', cbf);
	};

	this.showOrHideConversationDetails = function(pOverride) {
		var domConvEnable1 = document.getElementById(DOM_CONVENABLE1);
		var domConvBc1 = document.getElementById(DOM_CONVBC1);

		if (gEp.isCeStoreEmpty()) {
			//The CE Store is empty
			domConvEnable1.style.display = 'block';
			domConvBc1.style.display = 'none';
		} else {
			if (this.handler.isConversationModelLoaded() || pOverride) {
				//The conversation model is already loaded
				domConvEnable1.style.display = 'none';
				domConvBc1.style.display = 'block';
			} else {
				//The conversation model is not loaded
				domConvEnable1.style.display = 'block';
				domConvBc1.style.display = 'none';
			}
		}

		var bc1 = dijit.byId(DOM_CONVBC1);
		if (bc1 != null) {
			bc1.resize();
		}
	};

	this.domConversationFieldContents = function() {
		var domConvText = dijit.byId(DOM_CONVTEXT);

		return domConvText.get('value');
	};

	this.domLocationNameContents = function() {
		var domLocName = document.getElementById(DOM_LOCNAME);

		if (domLocName != null) {
			return domLocName.value;
		} else {
			return null;
		}
	};

	this.clearConversationField = function() {
		var domConvText = dijit.byId(DOM_CONVTEXT);
		domConvText.set('value', '');
		this.handler.currentMessage = '';
	};

	this.updateConversationFieldWithPlainText = function(pText) {
		var domConvText = dijit.byId(DOM_CONVTEXT);
		domConvText.set('value', pText);
		this.handler.currentMessage = pText;
	};

	this.appendToConversationField = function(pText) {
		var domConvText = dijit.byId(DOM_CONVTEXT);
		domConvText.set('value', domConvText.get('value') + pText);	
	};

	this.updateConversationFieldWithCe = function(pText) {	
		var domConvText = dijit.byId(DOM_CONVTEXT);
		domConvText.set('value', pText);
	};

	this.toggleAutoUpdate = function() {
		if (this.handler.getAutoUpdate()) {
			iTimeEvent = window.setInterval(gEp.dlg.conv.actions.showNewConversation, INTERVAL_AUTOUPDATE);	
		} else {
			if (iTimeEvent != null) {
				window.clearInterval(iTimeEvent);
				iTimeEvent = null;
			}
		}
	};

}
