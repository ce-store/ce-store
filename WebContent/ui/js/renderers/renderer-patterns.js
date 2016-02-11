/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.patterns = new RendererPatterns();

function RendererPatterns() {

	this.initialise = function() {
		gCe.msg.debug('RendererPatterns', 'initialise');
		//Nothing needed
	};

	this.renderQueryAndRuleList = function(pQueryList, pRuleList, pParms) {
		var html = '';

		html += htmlForQueryList(pQueryList);
		html += htmlForRuleList(pRuleList);

		gEp.ui.pane.pattern.updateWith(html);
	};

	this.renderQueryList = function(pQueryList, pParms) {
		var html = '';

		html += htmlForQueryList(pQueryList);

		gEp.ui.pane.pattern.updateWith(html);
	};

	this.renderQueryDetails = function(pQuery) {
		gCe.msg.notYetImplemented('renderQueryDetails', [pQuery]);

		if (!gCe.utils.isNullOrEmpty(pQuery)) {
			//TODO: Complete this
			var html = '';

			html += pQuery._id;
			html += '<br/><br/>';
			html += gCe.utils.htmlFormat(pQuery.ce_text);

			gEp.ui.pane.entity.updateWith(html);
		} else {
			gCe.msg.error('Unable to get query details');
		}
	};

	this.renderPatternExecutionResult = function(pResult, pUserParms) {
		var html = '';
		var patternType = null;
		var extraBit = '';
		
		if (pUserParms.executed_as_query === true) {
			extraBit = '(as a query) ';
		}
		
		if (pUserParms.query_name !== undefined) {
			html += '<h3>Results of query execution for: ' + pUserParms.query_name + '</h3>';
			patternType = 'query';
		} else {
			html += '<h3>Results of rule execution for: ' + pUserParms.rule_name + '</h3>';
			patternType = 'rule';
		}
		
		html += 'The ' + patternType + ' that was executed ' + extraBit + 'is:<br/><br/>';
		html += '<hr/>';
		html += gCe.utils.htmlFormat(pResult.query);
		html += '<hr/>';
		html += '<br/><br/>';
		html += pResult.results.length + ' rows returned (execution time was ' + pResult.query_time + 'ms)<br/><br/>';
		
		var formattedResults = formatResults(pResult);
		
		if (pResult.results.length !== 0) {
			html += gEp.ui.htmlTableFor(pResult.headers, formattedResults, gEp.ui.DEFAULT_STYLE);
		}

		gEp.ui.pane.general.updateWith(html, true);
	};

	this.renderRuleList = function(pRuleList, pParms) {
		var html = '';

		html += htmlForRuleList(pRuleList);

		gEp.ui.pane.pattern.updateWith(html);
	};

	this.renderRuleDetails = function(pRule) {
		gCe.msg.notYetImplemented('renderRuleDetails', [pRule]);

		if (!gCe.utils.isNullOrEmpty(pRule)) {
			//TODO: Complete this
			var html = '';

			html += pRule._id;
			html += '<br/><br/>';
			html += gCe.utils.htmlFormat(pRule.ce_text);

			gEp.ui.pane.entity.updateWith(html);
		} else {
			gCe.msg.error('Unable to get rule details');
		}
	};

	function formatResults(pResponse) {
		var result = [];

		for (var idx in pResponse.results) {
			var oldRow = pResponse.results[idx];
			var newRow = [];
			
			for (var i = 0; i < oldRow.length; i++) {
				var thisType = pResponse.types[i];
				var thisVal = oldRow[i];
				var formattedVal = null;
				
				if (thisType === 'O') {
					//This is an instance, so create a hyperlink
					formattedVal = gEp.ui.links.instanceDetails(thisVal);
				} else {
					//This is a value (or CE) so format for html rendering
					formattedVal = gCe.utils.htmlFormat(thisVal);
				}

				newRow.push(formattedVal);
			}

			result.push(newRow);
		}
		
		return result;
	}
	
	function htmlForQueryList(pQueryList) {
		var html = '';

		html += gEp.ui.links.patternList();
		html += '<br/><br/>';
		html += gEp.ui.htmlEmphasise('Queries:');
		html += gEp.ui.htmlUnorderedListFor(calculateQueryListItems(pQueryList));

		return html;
	}

	function htmlForRuleList(pRuleList) {
		var html = '';

		html += gEp.ui.htmlEmphasise('Rules:');
		html += gEp.ui.htmlUnorderedListFor(calculateRuleListItems(pRuleList));

		return html;
	}

	function calculateQueryListItems(pQueries) {
		var list = [];

		for (var key in pQueries) {
			var thisQuery = pQueries[key];
			var listText = '';
			listText += thisQuery.query_name;
			listText += htmlLinksForQuery(thisQuery);

			list.push(listText);
		}

		return list;
	}

	function htmlLinksForQuery(pQuery) {
		var html = '';
		html += '<br/>';
		html += '(';
		html += gEp.ui.links.queryLoad(pQuery.query_name);
		html += ', ';
		html += gEp.ui.links.queryDetails(pQuery.query_name);
		html += ', ';
		html += gEp.ui.links.queryExecute(pQuery.query_name, 'execute');
		html += ')';

		return html;
	}

	function calculateRuleListItems(pRules) {
		var list = [];

		for (var key in pRules) {
			var thisRule = pRules[key];

			var listText = '';
			listText += thisRule.rule_name;
			listText += htmlLinksForRule(thisRule);

			list.push(listText);
		}

		return list;
	}

	function htmlLinksForRule(pRule) {
		var html = '';

		html += '<br/>';
		html += '(';
		html += gEp.ui.links.ruleLoad(pRule.rule_name);
		html += ', ';
		html += gEp.ui.links.ruleDetails(pRule.rule_name);
		html += ', ';
		html += gEp.ui.links.ruleExecuteAsQuery(pRule.rule_name, 'execute as query');
		html += ', ';
		html += gEp.ui.links.ruleExecute(pRule.rule_name, 'execute as rule');
		html += ')';

		return html;
	}

}