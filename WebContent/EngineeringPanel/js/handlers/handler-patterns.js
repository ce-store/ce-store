/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.patterns = new HandlerPatterns();

function HandlerPatterns() {
	var ren = null;		//This is set during initialise

	this.initialise = function() {
		gCe.msg.debug('HandlerPatterns', 'initialise');

		ren = gEp.renderer.patterns;
	};

	this.listAllPatterns = function(pCbf, pUserParms) {
		var localUserParms = {type: 'pattern'};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.patterns.listPatterns(gEp.stdHttpParms(), cbf, userParms);
	};

	this.listAllQueries = function(pCbf, pUserParms) {
		var localUserParms = {type: 'query'};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.patterns.listQueries(gEp.stdHttpParms(), cbf, userParms);
	};

	this.getQueryDetails = function(pQueryName, pCbf, pUserParms) {
		var localUserParms = {type: 'query'};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processQueryDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.patterns.getQueryDetails(gEp.stdHttpParms(), cbf, pQueryName, userParms);
	};

	this.executeQueryByName = function(pQueryName, pCbf, pUserParms) {
		var localUserParms = {query_name: pQueryName};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processPatternExecutionResult(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.patterns.executeQuery(gEp.stdHttpParms(), cbf, pQueryName, userParms);
	};

	this.listAllRules = function(pCbf, pUserParms) {
		var localUserParms = {type: 'rule'};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.patterns.listPatterns(gEp.stdHttpParms(), cbf, userParms);
	};

	this.getRuleDetails = function(pRuleName, pCbf, pUserParms) {
		var localUserParms = {rule_name: pRuleName};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processRuleDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.patterns.getRuleDetails(gEp.stdHttpParms(), cbf, pRuleName, userParms);
	};

	this.executeRuleByName = function(pRuleName, pCbf, pUserParms) {
		var localUserParms = {rule_name: pRuleName, executed_as_query: false};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processPatternExecutionResult(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.patterns.executeRule(gEp.stdHttpParms(), cbf, pRuleName, userParms);
	};

	this.executeRuleAsQueryByName = function(pRuleName, pCbf, pUserParms) {
		var localUserParms = {rule_name: pRuleName, executed_as_query: true};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processPatternExecutionResult(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.patterns.executeRuleAsQuery(gEp.stdHttpParms(), cbf, pRuleName, userParms);
	};

	this.executeQueryText = function(pQueryText, pQueryName, pCbf, pUserParms) {
		var localUserParms = {type: 'query', query_name: pQueryName};
		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);

		executeTextAsQuery(pQueryText, pQueryName, pCbf, userParms);
	};

	this.executeRuleText = function(pRuleText, pRuleName, pCbf, pUserParms) {
		var localUserParms = {type: 'rule', rule_name: pRuleName, executed_as_query: false};
		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);

		executeTextAsRule(pRuleText, pRuleName, pCbf, userParms);
	};

	this.executeRuleTextAsQuery = function(pRuleText, pRuleName, pCbf, pUserParms) {
		var localUserParms = {type: 'rule', rule_name: pRuleName, executed_as_query: true};
		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);

		executeTextAsQuery(pRuleText, pRuleName, pCbf, userParms);
	};

	function executeTextAsQuery(pText, pQueryName, pCbf, pUserParms) {
		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processPatternExecutionResult(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		gCe.api.sentences.executeAsQuery(gEp.stdHttpParms(), cbf, pText, pUserParms);
	}

	function executeTextAsRule(pText, pQueryName, pCbf, pUserParms) {
		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.patterns.processPatternExecutionResult(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		gCe.api.sentences.executeAsRule(gEp.stdHttpParms(), cbf, pText, pUserParms);
	}

	this.processList = function(pResponse, pUserParms) {
		var list = gCe.utils.getStructuredResponseFrom(pResponse);

		if (!gCe.utils.isNullOrEmpty(list)) {
			ren.renderQueryAndRuleList(sortedQueries(list.queries), sortedRules(list.rules), pUserParms);
		} else {
			gCe.msg.error('Unable to get rule and query list');
		}

	};

	this.processQueryDetails = function(pResponse, pUserParms) {
		var query = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderQueryDetails(query, pUserParms);
	};

	this.processPatternExecutionResult = function(pResponse, pUserParms) {
		var result = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderPatternExecutionResult(result, pUserParms);
	};

	this.processRuleDetails = function(pResponse, pUserParms) {
		var rule = gCe.utils.getStructuredResponseFrom(pResponse);

		ren.renderRuleDetails(rule, pUserParms);
	};

	function sortedQueries(pQueryList) {
		var sortedQueries = null;

		if (pQueryList != null) {
			sortedQueries = pQueryList.sort(sortQueriesByName);
		} else {
			sortedQueries = [];
		}

		return sortedQueries;
	}

	function sortedRules(pRuleList) {
		var sortedRules = null;

		if (pRuleList != null) {
			sortedRules = pRuleList.sort(sortRulesByName);
		} else {
			sortedRules = [];
		}

		return sortedRules;
	}

	function sortQueriesByName(a, b) {
		return a.query_name.localeCompare(b.query_name);
	};

	function sortRulesByName(a, b) {
		return a.rule_name.localeCompare(b.rule_name);
	};

}