/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.dnd = new DialogDnd();

function DialogDnd() {
	var DNDTYPE_CON = 'concept';
	var DNDTYPE_PROP = 'property';
	var DNDTYPE_INST = 'instance';
	var DNDTYPE_QUERY = 'query';
	var DNDTYPE_RULE = 'rule';

	this.initialise = function() {
		gCe.msg.debug('DialogDnd', 'initialise');
		//Nothing needed
	};

	this.dragStartConcept = function(pEvt, pConName) {
		gCe.msg.debug(pConName, 'dragStartConcept', [pEvt]);

		if (!gCe.utils.isNullOrEmpty(pConName)) {
			var conCarrier = {type: DNDTYPE_CON, conceptName: pConName};
			pEvt.dataTransfer.setData(gCe.FORMAT_JSON, JSON.stringify(conCarrier));
		} else {
			//TODO: Also cancel the drag event
			gCe.msg.warning('No concept name specified for drag event', 'dragStartConcept');
		}
	};

	this.dragStartProperty = function(pEvt, pPropName) {
		gCe.msg.debug(pPropName, 'dragStartProperty', [pEvt]);

		if (!gCe.utils.isNullOrEmpty(pPropName)) {
			var propCarrier = {type: DNDTYPE_PROP, propertyName: pPropName};
			pEvt.dataTransfer.setData(gCe.FORMAT_JSON, JSON.stringify(propCarrier));
		} else {
			//TODO: Also cancel the drag event
			gCe.msg.warning('No property name specified for drag event', 'dragStartProperty');
		}
	};

	this.dragStartInstance = function(pEvt, pInstName) {
		gCe.msg.debug(pInstName, 'dragStartInstance', [pEvt]);

		if (!gCe.utils.isNullOrEmpty(pInstName)) {
			var instCarrier = {type: DNDTYPE_INST, instanceName: pInstName};
			pEvt.dataTransfer.setData(gCe.FORMAT_JSON, JSON.stringify(instCarrier));
		} else {
			//TODO: Also cancel the drag event
			gCe.msg.warning('No instance name specified for drag event', 'dragStartInstance');
		}
	};

	this.dragStartQuery = function(pEvt, pQueryName) {
		gCe.msg.debug(pQueryName, 'dragStartQuery', [pEvt]);

		if (!gCe.utils.isNullOrEmpty(pQueryName)) {
			var queryCarrier = {type: DNDTYPE_QUERY, queryName: pQueryName};
			pEvt.dataTransfer.setData(gCe.FORMAT_JSON, JSON.stringify(queryCarrier));
		} else {
			//TODO: Also cancel the drag event
			gCe.msg.warning('No query name specified for drag event', 'dragStartQuery');
		}
	};

	this.dragStartRule = function(pEvt, pRuleName) {
		gCe.msg.debug(pRuleName, 'dragStartRule', [pEvt]);

		if (!gCe.utils.isNullOrEmpty(pRuleName)) {
			var ruleCarrier = {type: DNDTYPE_RULE, ruleName: pRuleName};
			pEvt.dataTransfer.setData(gCe.FORMAT_JSON, JSON.stringify(ruleCarrier));
		} else {
			//TODO: Also cancel the drag event
			gCe.msg.warning('No rule name specified for drag event', 'dragStartRule');
		}
	};

	function isDroppedInstance(pPayload) {
		return pPayload.type == DNDTYPE_INST;
	}

	function isDroppedProperty(pPayload) {
		return pPayload.type == DNDTYPE_PROP;
	}

	function isDroppedConcept(pPayload) {
		return pPayload.type == DNDTYPE_CON;
	}

}