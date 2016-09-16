/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gTester = new Tester(true);

gTester.loadRequests();

function Tester(pJsDebug) {
	var LOCAL_SERVER = '..';
	var REMOTE_SERVER = 'http://blah.blah.blah:8080/ce-store/';
	var URL_REQUESTS_LIST = [ './json/requests_core.json', './json/requests_extra.json' ];

	var URL_RESPONSES_LIST = [
		{ name: 'core',
		  url: './json/responses_core.json',
		},
		{ name: 'extra',
		  url: './json/responses_extra.json'
		}
	];

	var DOM_RQL = 'request_list';
	var DOM_RR = 'request_range';
	var DOM_DE = 'delay';
	var DOM_IT = 'iterations';
	var DOM_RSL = 'response_list';
	var DOM_NR = 'number_of_requests';
	var DOM_XL = 'loaded';
	var DOM_TR = 'test_results';
	var DOM_RD = 'response_depth';
	var DOM_CJ = 'json';
	var DOM_CC = 'ce';
	var DOM_SD = 'show_details';
	var DOM_EP = 'endpoint';
	var DOM_UN = 'username';
	var HDR_CEUSER = 'CE_User';

	var TYPE_JSON = 'json';
	var TYPE_CE = 'ce';

	var MAX_INITIAL_REQUESTS = -1;

	this.allRequests = null;
	this.jsDebug = pJsDebug;
	this.currentPos = 0;
	this.currentIteration = 0;
	this.currentType = null;
	this.selectedRequests = null;
	this.definitiveResponses = {};
	this.actualResponses = {};
	this.testErrors = 0;
	this.testUnknowns = 0;
	this.testCount = 0;
	this.testTime = 0;
	this.totalRequests = 0;

	this.initialiseTestPage = function(pResponse) {
		if (gTester.allRequests == null) {
			gTester.allRequests = [];
		}

		for (var i = 0; i < pResponse.length; i++) {
			var thisReq = pResponse[i];
			gTester.allRequests.push(thisReq);
		}

		gTester.sortRequests(gTester.allRequests);
		gTester.renderRequestList();
	};

	this.getCurrentPos = function() {
		return this.currentPos;
	};

	this.getCurrentIteration = function() {
		return this.currentIteration;
	};

	this.loadDefinitiveResponses = function() {
		var cbf = function(pResponse) { gTester.handleDefinitiveResponsesResponse(pResponse); };

		this.definitiveResponses = {};

		for (var i = 0; i < URL_RESPONSES_LIST.length; i++) {
			var thisUrl = URL_RESPONSES_LIST[i].url;

			gTester.sendJsonFileRequest(thisUrl, cbf);
		}
	};

	this.handleDefinitiveResponsesResponse = function(pResponse) {
		for (var i = 0; i < pResponse.length; i++) {
			var thisResponse = pResponse[i];

			if (this.definitiveResponses[thisResponse.id] != null) {
				gTester.reportLog("Warning - overwriting answer " + thisResponse.id);
			}

			this.definitiveResponses[thisResponse.id] = thisResponse;
		}

		var countUrl = hyperlinksForNewWindow(URL_RESPONSES_LIST, definitiveCountTextFor(this.definitiveResponses));

		setTextIn(DOM_XL, countUrl);
	};

	function hyperlinksForNewWindow(pUrls, pLinkText) {
		var result = pLinkText + ': ';
		var firstTime = true;

		for (var i = 0; i < pUrls.length; i++) {
			var thisUrl = pUrls[i];

			if (!firstTime) {
				result += ', ';
			}

			result += '<a href="' + thisUrl.url + '"target="_blank">' + thisUrl.name + '</a> ';
			firstTime = false;
		}

		return result;
	}

	this.updateUiWithRequests = function() {
		var maxLen = gTester.allRequests.length - 1;

		if (MAX_INITIAL_REQUESTS != -1) {
			if (maxLen > MAX_INITIAL_REQUESTS) {
				maxLen = MAX_INITIAL_REQUESTS;
			}
		}

		this.setDefaultRequestRange(maxLen);

		this.renderRequestList(0, maxLen);
	};
	
	function debugList(pList) {
		for (var i = 0; i < pList.length; i++) {
			var thisQ = pList[i];
			gTester.reportLog(thisQ);
		}
	}

	function definitiveCountTextFor(pObj) {
		var result = null;
		var colour = null;
		var numItems = Object.keys(pObj).length;
		
		if (numItems === 0) {
			colour = 'grey';
		} else {
			colour = 'green';
		}
		
		result = '<font color="' + colour + '">(' + numItems + ' loaded)</font>';
		
		return result;
	}

	this.getDefinitiveResponseFor = function(pKey) {
		var result = null;

		result = this.definitiveResponses[pKey];

		return result;
	};
	
	this.isJson = function() {
		return gTester.getCheckboxValueFrom(DOM_CJ);
	};

	this.isCe = function() {
		return gTester.getCheckboxValueFrom(DOM_CC);
	};

	this.isShowingDetails = function() {
		return gTester.getCheckboxValueFrom(DOM_SD);
	};

	this.renderRequestList = function(pStartIdx, pEndIdx) {
		var htmlText = '';
		var startIdx = 0;
		var endIdx = 0;
		this.selectedRequests = [];

		if (pStartIdx == null) {
			startIdx = 0;
		} else {
			startIdx = pStartIdx - 1;
		}

		if ((pEndIdx == null) || (pEndIdx >= this.allRequests.length)) {
			endIdx = this.allRequests.length -1;
		} else {
			endIdx = pEndIdx - 1;
		}

		for (var i = startIdx; i <= endIdx; i++) {
			this.selectedRequests.push(this.allRequests[i]);
		}

		this.sortRequests(this.selectedRequests);

		htmlText += '<ol>';

		for (var i = 0; i < this.selectedRequests.length; i++) {
			var thisRequest = this.selectedRequests[i];
			var extras = '';
			
			if (thisRequest.parameters != null) {
				extras += " [" + thisRequest.parameters + "]";
			}

			if (thisRequest.delay != null) {
				extras += ' (delay=' + thisRequest.delay + 'ms)';
			}
			
			htmlText += '<li>[' + thisRequest.id + ']: ' + thisRequest.url + extras + '</li>';
		}

		htmlText += '</ol>';

		setTextIn(DOM_RQL, htmlText);
	};

	this.executeTestRequests = function() {
		this.testTime = 0;
		this.testCount = 0;
		this.testErrors = 0;
		this.testUnknowns = 0;
		this.currentPos = 0;
		this.currentIteration = 1;
		this.responseText = '';
		var numIts = parseInt(getTextFrom(DOM_IT));

		setTextIn(DOM_TR, "");

		if (isNaN(numIts) || (numIts < 1)) {
			numIts = 1;
		}

		this.totalRequests = (numIts * this.selectedRequests.length) * this.countCheckboxesSelected();

		updateResponseText(this.responseText);

		if (this.isJson()) {
			this.currentType = TYPE_JSON;
			this.processNextRequest();
		} else if (this.isCe()) {
			this.currentType = TYPE_CE;
			this.processNextRequest();
		} else {
			alert("Nothing to do - please select one of the checkboxes");
		}
	};

	this.countCheckboxesSelected = function() {
		var result = 0;

		if (this.isJson()) {
			++result;
		}

		if (this.isCe()) {
			++result;
		}

		return result;
	}

	this.takeNextRequest = function() {
		var result = null;
		var numIts = parseInt(getTextFrom(DOM_IT));

		if (isNaN(numIts) || (numIts < 1)) {
			numIts = 1;
		}

		if (this.currentPos < this.selectedRequests.length) {
			result = this.selectedRequests[this.currentPos++];
		} else {
			var nextType = this.getNextType();
			
			if (nextType != null) {
				this.currentType = nextType;
				this.currentPos = 0;
				result = this.selectedRequests[this.currentPos++];
			} else {
				//Iterate
				if (this.currentIteration++ < numIts) {
					this.currentPos = 0;
					result = this.selectedRequests[this.currentPos++];
				}
			}
		}

		return result;
	};

	this.getNextType = function() {
		var result = null;

		if (this.currentType == TYPE_JSON) {
			if (this.isCe()) {
				result = TYPE_CE;
			}
		}

		return result;
	}

	this.processNextRequest = function() {
		var msDelay = parseInt(getTextFrom(DOM_DE)) + this.nextRequestDelay;
		var nextReq = gTester.takeNextRequest();

		if (nextReq != null) {
			var addedDelay = nextReq.delay;
			if ((addedDelay != undefined) && (!isNaN(addedDelay))) {
				this.nextRequestDelay = addedDelay;
			} else {
				this.nextRequestDelay = 0;
			}

			if (this.currentType == TYPE_JSON) {
				this.setTimeoutForJson(nextReq, msDelay);
			} else if (this.currentType == TYPE_CE) {
				this.setTimeoutForCe(nextQ, msDelay);
			}
		}
	};

	this.setTimeoutForJson = function(pReq, pMsDelay) {
		setTimeout(function () {
			var qIdx = gTester.getCurrentPos();
			var qIt = gTester.getCurrentIteration();
			var cbf = function(pResponse) { gTester.handleResponse(pReq, pResponse, qIdx, qIt, pReq.id, TYPE_JSON); };

			sendSpecificRequest(pReq, cbf);
			gTester.processNextRequest(TYPE_JSON);
		}, pMsDelay);
	};

	this.setTimeoutForCe = function(pReq, pMsDelay) {
		setTimeout(function () {
			var qIdx = gTester.getCurrentPos();
			var qIt = gTester.getCurrentIteration();
			var cbf = function(pResponse) { gTester.handleResponse(pReq, pResponse, qIdx, qIt, pReq.id, TYPE_CE); };

			sendSpecificRequest(pReq, cbf);
			gTester.processNextRequest(TYPE_CE);
		}, pMsDelay);
	};

	function sendSpecificRequest(pRequest, pCbf) {
		var url = getTextFrom(DOM_EP) + pRequest.url;
		var parms = pRequest.parameters;

		if (parms != null) {
			url += parms;
		}

		if (pRequest.method == "GET") {
			sendAjaxGet(url, pCbf);
		} else {
			sendAjaxPost(url, null, pCbf);
		}
	}

	function sendAjaxGet(pUrl, pCbf) {
		sendAjaxRequest('GET', pUrl, null, pCbf);
	}

	function sendAjaxPost(pUrl, pParms, pCbf) {
		sendAjaxRequest('POST', pUrl, pParms, pCbf);
	}

	this.getUserName = function() {
		return getTextFrom(DOM_UN);
	};

	function sendAjaxRequest(pType, pUrl, pPostParms, pCbf) {
		var xhr = new XMLHttpRequest();

		var userName = gTester.getUserName();

		xhr.open(pType, pUrl, true);

		if (userName != null) {
			xhr.setRequestHeader(HDR_CEUSER, userName);
		}

		xhr.withCredentials = true;
		xhr.setRequestHeader('Accept', 'application/json');
		xhr.setRequestHeader('Content-type', 'text/plain; charset=utf-8');

		xhr.onload = function(e) {
			if (this.status === 200) {
				var jResponse = null;

				try {
					jResponse = JSON.parse(xhr.response);
				} catch(e) {
					jResponse = xhr.response;
				}

				pCbf(jResponse);
			} else if (this.status === 404) {
				ajaxError404(pUrl);
			} else if (this.status === 405) {
				ajaxError405(pUrl);
			} else if (this.status === 500) {
				ajaxError500(pUrl);
			} else {
				ajaxErrorOther(xhr.response, e, this.status, pUrl);
			}
		};

		xhr.onerror = function(e) {
			ajaxErrorOther(xhr.response, e, this.status, pUrl);
		};

		if ((pType === 'POST') && (pPostParms != null)) {
			xhr.send(pPostParms);
		} else {
			xhr.send();
		}
	}

	function ajaxError404(pUrl) {
		reportError('Server error "not found" (404) for url ' + pUrl);
	}

	function ajaxError405(pUrl) {
		reportError('Server error "not allowed" (405) for url ' + pUrl);
	}

	function ajaxError500(pUrl) {
		reportError('Server error (500) for url ' + pUrl);
	}

	function ajaxErrorOther(pResponseText, pError, pCode, pUrl) {
		gTester.reportLog(pResponseText);
		gTester.reportLog(pCode);
		reportError('Unknown server error [' + pError + '] for url ' + pUrl);
	}

	function reportError(pErrorText) {
		setTextIn('answer', '<font color="red">' + pErrorText + '</font>');
	}

	this.reportResults = function() {
		var resultText = '';

		resultText += this.testCount + ' of ' + this.totalRequests + ' tests completed';

		if (this.testTime !== 0) {
			var avgText = ' (' + Math.round(parseInt(this.testTime) / parseInt(this.testCount)) + ' ms per test)';

			resultText += ' in ' + parseInt(this.testTime) / 1000 + ' seconds ' + avgText + '. ';
		} else {
			resultText += '. ';
		}

		if (this.testErrors === 0) {
			if (this.testUnknowns === 0) {
				resultText += '<font color="green">No issues detected</font>';
			} else {
				resultText += '<font color="green">No errors detected</font>, but <font color="orange">' + this.testUnknowns + ' tests could not be checked</font>';
			}
		} else {
			resultText += '<font color="red">' + this.testErrors + ' errors detected</font>';

			if (this.testUnknowns !== 0) {
				resultText += ', and <font color="orange">' + this.testUnknowns + ' tests could not be checked</font>';
			}
		}

		setTextIn(DOM_TR, resultText);
	};

	this.handleUnsupported = function(pMsg, pIdx, pIteration, pKey, pType) {
		var markerText = '<font color="red">Bad (' + pMsg + ')</font> ';
		this.testErrors++;

		this.answerText += this.answerTextFrom(null, pIdx, pIteration, markerText);
		this.answerText += '<br><br>';
		updateAnswerText(this.answerText);

		++this.testCount;
		
		this.reportResults();
	}

	this.handleResponse = function(pRequest, pResponse, pIdx, pIteration, pKey, pType) {
		var dr = null;
		var matchError = 0;
		var markerText = null;
		var expected = null;

		this.actualResponses[pKey] = pResponse;
		dr = this.getDefinitiveResponseFor(pKey);

		if (dr != null) {
			var maxDepth = getMaxDepth();
			expected = dr[pType];

			if (expected != null) {
				if (pType == TYPE_JSON) {
					matchError = gChecker.compareJson(expected, pResponse, maxDepth);
				} else if (pType == TYPE_CE) {
					matchError = gChecker.compareCe(expected, pResponse, maxDepth);
				}

				if (matchError === '') {
					markerText = ' - <font color="green">Good</font> ';
				} else {
					markerText = ' - <font color="red">Bad (' + matchError + ')</font> ';
					this.testErrors++;
				}
			} else {
				markerText = ' - <font color="grey">???</font> ';
				this.testUnknowns++;
			}
		} else {
			markerText = ' - <font color="grey">???</font> ';
			this.testUnknowns++;
		}
		
		if (pResponse.debug != null) {
			var et = pResponse.debug.execution_time_ms;
			
			if (et !== null) {
				this.testTime += et;
			}
		}

		this.responseText += pType;
		this.responseText += " (<a href=\"javascript:gTester.showActualJson('" + pKey + "');\" title=\"actual live JSON\">a</a>";
		
		if (dr != null) {
			this.responseText += ",<a href=\"javascript:gTester.showDefinitiveJson('" + pKey + "');\" title=\"expected JSON\">e</a>)";
		} else {
			this.responseText += ")";
		}
		this.responseText += this.responseTextFrom(pRequest, pResponse, expected, pIdx, pIteration, markerText);
		
		updateResponseText(this.responseText);

		++this.testCount;
		
		this.reportResults();
	};
	
	this.responseTextFrom = function(pRequest, pResponse, pExpected, pIdx, pIteration, pMarker) {
		var result = pMarker;
		
		result += '[' + pIteration + ':' + pIdx + '] ';

		if (pResponse != null) {
			result += pRequest.id + ': "' + pRequest.description + '"';
			
			if (pRequest.parameters != null) {
				result += " [" + pRequest.parameters + "]";
			}

			if (this.isShowingDetails()) {
				var actLength = JSON.stringify(pResponse).length;
				result += '<ul>';

				result += '<li>';
				result += actLength + ' characters';

				if (pMarker != "json - <font color=\"green\">Good</font> ") {
					if (pExpected != null) {
						var expLength = JSON.stringify(pExpected).length;
						
						if (expLength != actLength) {
							result += " vs " + expLength + ' expected characters';
						}
					}
				}
				result += '</li>';
				result += '</ul>';
			} else {
				result += '<br><br>';
			}
		}

		if (result === '') {
			result = 'Unexpected null response<br/>';
		}

		return result;
	}

	this.showDefinitiveJson = function(pKey) {
		var thisJson = this.definitiveResponses[pKey];

		this.reportLog("Expected (definitive) JSON for '" + pKey + "':");
		this.reportLog(JSON.stringify(thisJson));
	};
	
	this.showActualJson = function(pKey) {
		var thisJson = this.actualResponses[pKey];
		var jsonText = "\n";
		
		jsonText += "	{\n";
		jsonText += "		\"id\": \"" + pKey + "\",\n";
		jsonText += "		\"json\": " + JSON.stringify(thisJson) + "\n";
		jsonText += "	},\n";

		this.reportLog("Actual JSON for '" + pKey + "':");
		this.reportLog(jsonText);
	};

	function getMaxDepth() {
		var result = parseInt(getTextFrom(DOM_RD));

		if (isNaN(result)) {
			result = -1;
		} else if (result < 0) {
			result = -1;
		}

		return result;
	}

	this.keyUpRequestRange = function() {
		var rrText = getTextFrom(DOM_RR);
		var parts = rrText.split('-');
		var failedParse = false;
		var firstNum = null;
		var secondNum = null;
		var maxNum = gTester.allRequests.length;
		
		if (parts.length === 1) {
			//A single number
			firstNum = parseInt(parts[0]);
		} else if (parts.length === 2) {
			//A pair of numbers
			firstNum = parseInt(parts[0]);
			secondNum = parseInt(parts[1]);
		} else {
			//Some other situation - an error
			failedParse = true;
		}
		
		if (!isNaN(firstNum)) {
			if (!isNaN(secondNum)) {
				//Both numbers are valid
				if (firstNum < 0) {
					firstNum = 0;
				}

				if (firstNum > maxNum) {
					firstNum = maxNum;
				}

				if (secondNum < 0) {
					secondNum = 0;
				}

				if (secondNum > maxNum) {
					secondNum = maxNum;
				}

				if (secondNum >= firstNum) {
					this.renderRequestList(firstNum, secondNum);
				} else {
					//The numbers are back to front, so switch them
					this.renderRequestList(secondNum, firstNum);
				}
			} else {
				//just the first number is valid
				
				if (firstNum < 0) {
					firstNum = 0;
				}
				
				if (firstNum > maxNum) {
					firstNum = maxNum;
				}

				this.renderRequestList(firstNum, firstNum);
			}
		} else {
			//Neither number is valid
			failedParse = true;
		}

		if (failedParse) {
			this.setDefaultRequestRange(MAX_INITIAL_REQUESTS);
		}
	};

	this.requestTextFrom = function(pResponse, pIdx, pIteration, pMarker) {
		var result = pMarker;
		
		result += '[' + pIteration + ':' + pIdx + '] ';

		if (pResponse != null) {
			result += "response";

			if (this.isShowingDetails()) {
				result += JSON.stringify(pResponse);
			} else {
				result += '<br><br>';
			}
		}

		if (result === '') {
			result = 'Unexpected null response<br/>';
		}

		return result;
	}
	
	function formattedTableLineFor(pRs) {
		var result = '';
		var hdrText = JSON.stringify(pRs.headers);
		var rowText = JSON.stringify(pRs.rows);
		
		result += hdrText + '->' + rowText;
		
		return result;
	}

	function updateResponseText(pText) {
		setTextIn(DOM_RSL, pText);
	}

	this.setDefaultRequestRange = function(pMaxLen) {
		var rangeText = '0-' + pMaxLen;

		setTextIn(DOM_RR, rangeText);
		setTextIn(DOM_NR, ' - there are ' + gTester.allRequests.length + ' predefined requests available');
	}

	function getTextFrom(pDomId) {
		var elem = window.document.getElementById(pDomId);
		var contents = null;

		if (elem != null) {
			contents = elem.value;
		}

		return contents;
	}

	function setTextIn(pDomId, pText) {
		var elem = window.document.getElementById(pDomId);

		if (elem != null) {
			//It's nasty setting both of these but is means one function for divs and textareas
			elem.value = pText;
			elem.innerHTML = pText;
		}
	}

	this.loadRequests = function() {
		for (var i = 0; i < URL_REQUESTS_LIST.length; i++) {
			var thisUrl = URL_REQUESTS_LIST[i];

			this.sendJsonFileRequest(thisUrl, gTester.initialiseTestPage);
		}
	};

	this.sendJsonFileRequest = function(pUrl, pCbf) {
		var xhr = new XMLHttpRequest();

		xhr.open('GET', pUrl, true);

		xhr.onload = function(e) {
			if (this.status === 200) {
				pCbf(JSON.parse(xhr.response));
			} else {
				ajaxErrorOther(xhr.response, e, this.status, pUrl);
			}
		};

		xhr.onerror = function(e) {
			ajaxErrorOther(xhr.response, e, this.status, pUrl);
		};

		xhr.send();
	};

	this.sortRequests = function(pList) {
		pList.sort(sortById);
	}

	function sortById(a, b) {
		var result = null;

		if (a.id < b.id) {
			result = -1;
		} else if (a.id > b.id) {
			result = 1;
		} else {
			result = 0;
		}

		return result;
	}

	this.loadCurrentRequest = function() {
		var cr = this.getCurrentRequest();

		if (cr != null) {
			var rText = cr.url;

			this.clearResponseText();

			setTextIn(DOM_RT, rText);
		}
	};

	this.getCheckboxValueFrom = function(pDomId) {
		var elem = window.document.getElementById(pDomId);
		var result = null;

		if (elem != null) {
			result = elem.checked;
		}

		return result;
	};

	this.reportLog = function(pText) {
		console.log(pText);
	}

}
