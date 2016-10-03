/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gHudson = new Hudson(false);
var gTester = new Tester(false);

function Tester(pJsDebug) {
	var URL_ANSWERS_LIST = [
		{ name: 'core',
		  url: './test_json/answers_core.json',
		},
		{ name: 'extra',
		  url: './test_json/answers_extra.json'
		}
	];

	var DOM_QL = 'question_list';
	var DOM_QR = 'question_range';
	var DOM_DE = 'delay';
	var DOM_IT = 'iterations';
	var DOM_AL = 'answer_list';
	var DOM_NQ = 'number_of_questions';
	var DOM_XL = 'loaded';
	var DOM_TR = 'test_results';
	var DOM_RSD = 'result_set_depth';
	var DOM_CIN = 'interpret';
	var DOM_CANS = 'answer';
	var DOM_CEX = 'execute';
	var DOM_CANA = 'analyse';
	var DOM_SD = 'show_details';

	var TYPE_INTERPRET = 'interpret';
	var TYPE_ANSWER = 'answer';
	var TYPE_EXECUTE = 'execute';
	var TYPE_ANALYSE = 'analyse';

	var MAX_INITIAL_QUESTIONS = -1;

	this.jsDebug = pJsDebug;
	this.currentPos = 0;
	this.currentIteration = 0;
	this.currentType = null;
	this.selectedQuestions = null;
	this.definitiveNormalResponses = null;
	this.definitiveDebugResponses = null;
	this.answers = {};
	this.answerCount = 0;
	this.testErrors = 0;
	this.testUnknowns = 0;
	this.testCount = 0;
	this.testTime = 0;
	this.totalQuestions = 0;

	this.getCurrentPos = function() {
		return this.currentPos;
	};

	this.getCurrentIteration = function() {
		return this.currentIteration;
	};

//	this.loadDefinitiveAnswers = function() {
//		var cbf = function(pResponse) { gTester.handleDefinitiveAnswersResponse(pResponse); };
//
//		this.answers = {};
//
//		for (var i = 0; i < URL_ANSWERS_LIST.length; i++) {
//			var thisUrl = URL_ANSWERS_LIST[i].url;
//
//			gHudson.sendJsonFileRequest(thisUrl, cbf);
//		}
//	};

	this.handleDefinitiveAnswersResponse = function(pResponse) {
		this.answers = {};

		for (var i = 0; i < pResponse.length; i++) {
			var thisAnswer = pResponse[i];

			if (this.answers[thisAnswer.id] != null) {
				gHudson.reportLog("Warning - overwriting answer " + thisAnswer.id);
			}

			this.answers[thisAnswer.id] = thisAnswer;
		}
		
		this.answerCount = pResponse.length;

		var countUrl = definitiveCountTextFor(this.answers);

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

	this.updateUiWithQuestions = function() {
		var maxLen = gHudson.allQuestions.length - 1;

		if (MAX_INITIAL_QUESTIONS != -1) {
			if (maxLen > MAX_INITIAL_QUESTIONS) {
				maxLen = MAX_INITIAL_QUESTIONS;
			}
		}

		this.setDefaultQuestionRange(maxLen);

		this.renderQuestionList(0, maxLen);
	};
	
	function debugList(pList) {
		for (var i = 0; i < pList.length; i++) {
			var thisQ = pList[i];
			gHudson.reportLog(thisQ);
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

		result = this.answers[pKey];

		return result;
	};
	
	this.isInterpreting = function() {
		return gHudson.getCheckboxValueFrom(DOM_CIN);
	};

	this.isAnswering = function() {
		return gHudson.getCheckboxValueFrom(DOM_CANS);
	};

	this.isExecuting = function() {
		return gHudson.getCheckboxValueFrom(DOM_CEX);
	};

	this.isShowingDetails = function() {
		return gHudson.getCheckboxValueFrom(DOM_SD);
	};

	this.isAnalysing = function() {
		return gHudson.getCheckboxValueFrom(DOM_CANA);
	};

	this.renderQuestionList = function(pStartIdx, pEndIdx) {
		var htmlText = '';
		var startIdx = 0;
		var endIdx = 0;
		this.selectedQuestions = [];

		if (pStartIdx == null) {
			startIdx = 0;
		} else {
			startIdx = pStartIdx - 1;
		}

		if ((pEndIdx == null) || (pEndIdx >= gHudson.allQuestions.length)) {
			endIdx = gHudson.allQuestions.length -1;
		} else {
			endIdx = pEndIdx - 1;
		}

		for (var i = startIdx; i <= endIdx; i++) {
			this.selectedQuestions.push(gHudson.allQuestions[i]);
		}

		gHudson.sortQuestions(this.selectedQuestions);

		htmlText += '<ol>';

		for (var i = 0; i < this.selectedQuestions.length; i++) {
			var thisQuestion = this.selectedQuestions[i];
			var extras = '';
			
			if (thisQuestion.delay != null) {
				extras = ' (delay=' + thisQuestion.delay + 'ms)';
			}
			
			htmlText += '<li>[' + thisQuestion.id + ']: ' + thisQuestion.question + extras + '</li>';
		}

		htmlText += '</ol>';

		setTextIn(DOM_QL, htmlText);
	};

	this.executeTestQuestions = function() {
		this.testTime = 0;
		this.testCount = 0;
		this.testErrors = 0;
		this.testUnknowns = 0;
		this.currentPos = 0;
		this.currentIteration = 1;
		this.answerText = '';
		var numIts = parseInt(getTextFrom(DOM_IT));

		setTextIn(DOM_TR, "");

		if (isNaN(numIts) || (numIts < 1)) {
			numIts = 1;
		}

		this.totalQuestions = (numIts * this.selectedQuestions.length) * this.countCheckboxesSelected();

		updateAnswerText(this.answerText);

		if (this.isInterpreting()) {
			this.currentType = TYPE_INTERPRET;
			this.processNextQuestion();
		} else if (this.isAnswering()) {
			this.currentType = TYPE_ANSWER;
			this.processNextQuestion();
		} else if (this.isExecuting()) {
			this.currentType = TYPE_EXECUTE;
			this.processNextQuestion();
		} else if (this.isAnalysing()) {
			this.currentType = TYPE_ANALYSE;
			this.processNextQuestion();
		} else {
			alert("Nothing to do - please select one of the checkboxes");
		}
	};

	this.countCheckboxesSelected = function() {
		var result = 0;

		if (this.isInterpreting()) {
			++result;
		}

		if (this.isAnswering()) {
			++result;
		}

		if (this.isExecuting()) {
			++result;
		}

		if (this.isAnalysing()) {
			++result;
		}

		return result;
	}

	this.takeNextQuestion = function() {
		var result = null;
		var numIts = parseInt(getTextFrom(DOM_IT));

		if (isNaN(numIts) || (numIts < 1)) {
			numIts = 1;
		}

		if (this.currentPos < this.selectedQuestions.length) {
			result = this.selectedQuestions[this.currentPos++];
		} else {
			var nextType = this.getNextType();
			
			if (nextType != null) {
				this.currentType = nextType;
				this.currentPos = 0;
				result = this.selectedQuestions[this.currentPos++];
			} else {
				//Iterate
				if (this.currentIteration++ < numIts) {
					this.currentPos = 0;
					result = this.selectedQuestions[this.currentPos++];
				}
			}
		}

		return result;
	};

	this.getNextType = function() {
		var result = null;

		if (this.currentType == TYPE_INTERPRET) {
			if (this.isAnswering()) {
				result = TYPE_ANSWER;
			} else if (this.isExecuting()) {
				result = TYPE_EXECUTE;
			} else if (this.isAnalysing()) {
				result = TYPE_ANALYSE;
			}
		} else if (this.currentType == TYPE_ANSWER) {
			if (this.isExecuting()) {
				result = TYPE_EXECUTE;
			} else if (this.isAnalysing()) {
				result = TYPE_ANALYSE;
			}
		} else if (this.currentType == TYPE_EXECUTE) {
			if (this.isAnalysing()) {
				result = TYPE_ANALYSE;
			}
		}

		return result;
	}

	this.processNextQuestion = function() {
		var msDelay = parseInt(getTextFrom(DOM_DE)) + this.nextQuestionDelay;
		var nextQ = gTester.takeNextQuestion();

		if (nextQ != null) {
			var addedDelay = nextQ.delay;
			if ((addedDelay != undefined) && (!isNaN(addedDelay))) {
				this.nextQuestionDelay = addedDelay;
			} else {
				this.nextQuestionDelay = 0;
			}

			if (this.currentType == TYPE_INTERPRET) {
				this.setTimeoutForInterpreting(nextQ, msDelay);
			} else if (this.currentType == TYPE_ANSWER) {
				this.setTimeoutForAnswering(nextQ, msDelay);
			} else if (this.currentType == TYPE_EXECUTE) {
				this.setTimeoutForExecuting(nextQ, msDelay);
			} else if (this.currentType == TYPE_ANALYSE) {
				this.setTimeoutForAnalysing(nextQ, msDelay);
			}
		}
	};

	this.setTimeoutForExecuting = function(pQ, pMsDelay) {
		setTimeout(function () {
			var qIdx = gTester.getCurrentPos();
			var qIt = gTester.getCurrentIteration();
			var cbf = function(pResponse) { gTester.handleAnswer(pResponse, qIdx, qIt, pQ.id, TYPE_EXECUTE); };

			gHudson.executeSpecificQuestion(pQ.question, cbf);
			gTester.processNextQuestion(TYPE_EXECUTE);
		}, pMsDelay);
	};

	this.setTimeoutForAnswering = function(pQ, pMsDelay) {
		setTimeout(function () {
			var qIdx = gTester.getCurrentPos();
			var qIt = gTester.getCurrentIteration();
			var cbf = function(pResponse) { gTester.handleUnsupported('answering not yet supported', qIdx, qIt, pQ.id, TYPE_ANSWER); };

			gHudson.answerSpecificQuestion(pQ.question, cbf);
			gTester.processNextQuestion(TYPE_ANSWER);
		}, pMsDelay);
	};

	this.setTimeoutForInterpreting = function(pQ, pMsDelay) {
		setTimeout(function () {
			var qIdx = gTester.getCurrentPos();
			var qIt = gTester.getCurrentIteration();
			var cbf = function(pResponse) { gTester.handleAnswer(pResponse, qIdx, qIt, pQ.id, TYPE_INTERPRET); };

			gHudson.interpretSpecificQuestion(pQ.question, cbf);
			gTester.processNextQuestion(TYPE_INTERPRET);
		}, pMsDelay);
	};

	this.setTimeoutForAnalysing = function(pQ, pMsDelay) {
		setTimeout(function () {
			var qIdx = gTester.getCurrentPos();
			var qIt = gTester.getCurrentIteration();
			var cbf = function(pResponse) { gTester.handleUnsupported('analysing not yet supported', qIdx, qIt, pQ.id, TYPE_ANALYSE); };

			gHudson.analyseSpecificQuestion(pQ.question, cbf);
			gTester.processNextQuestion(TYPE_ANALYSE);
		}, pMsDelay);
	};

	this.reportResults = function() {
		var resultText = '';

		resultText += this.testCount + ' of ' + this.totalQuestions + ' tests completed';

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

	this.handleAnswer = function(pResponse, pIdx, pIteration, pKey, pType) {
		var dr = null;
		var matchError = 0;
		var markerText = null;

		dr = this.getDefinitiveResponseFor(pKey);

		if (dr != null) {
			var maxDepth = getMaxDepth();

			if (dr.answer != null) {
				if (pType == TYPE_EXECUTE) {
					matchError = gChecker.compareExecuteReplies(dr[pType], pResponse, maxDepth);
				} else if (pType == TYPE_INTERPRET) {
					matchError = gChecker.compareInterpretReplies(dr[pType], pResponse, maxDepth);
				} else if (pType == TYPE_ANSWER) {
					matchError = gChecker.compareAnswerReplies(dr[pType], pResponse, maxDepth);
				} else if (pType == TYPE_ANALYSE) {
					matchError = gChecker.compareAnalyseReplies(dr[pType], pResponse, maxDepth);
				}

				if (matchError === '') {
					markerText = pType + ' - <font color="green">Good</font> ';
				} else {
					markerText = pType + ' - <font color="red">Bad (' + matchError + ')</font> ';
					this.testErrors++;
				}
			} else {
				markerText = pType + ' - <font color="grey">???</font> ';
				this.testUnknowns++;
			}
		} else {
			markerText = pType + ' - <font color="grey">???</font> ';
			this.testUnknowns++;
		}
		
		if (pResponse.debug != null) {
			var et = pResponse.debug.execution_time_ms;
			
			if (et !== null) {
				this.testTime += et;
			}
		}

		this.answerText += this.answerTextFrom(pResponse, pIdx, pIteration, markerText);

		updateAnswerText(this.answerText);

		++this.testCount;
		
		this.reportResults();
	};
	
	function getMaxDepth() {
		var result = parseInt(getTextFrom(DOM_RSD));

		if (isNaN(result)) {
			result = -1;
		} else if (result < 0) {
			result = -1;
		}

		return result;
	}

	this.keyUpQuestionRange = function() {
		var qrText = getTextFrom(DOM_QR);
		var parts = qrText.split('-');
		var failedParse = false;
		var firstNum = null;
		var secondNum = null;
		var maxNum = gHudson.allQuestions.length;
		
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
					this.renderQuestionList(firstNum, secondNum);
				} else {
					//The numbers are back to front, so switch them
					this.renderQuestionList(secondNum, firstNum);
				}
			} else {
				//just the first number is valid
				
				if (firstNum < 0) {
					firstNum = 0;
				}
				
				if (firstNum > maxNum) {
					firstNum = maxNum;
				}

				this.renderQuestionList(firstNum, firstNum);
			}
		} else {
			//Neither number is valid
			failedParse = true;
		}

		if (failedParse) {
			this.setDefaultQuestionRange(MAX_INITIAL_QUESTIONS);
		}
	};

	this.answerTextFrom = function(pResponse, pIdx, pIteration, pMarker) {
		var result = pMarker;
		
		result += '[' + pIteration + ':' + pIdx + '] ';

		if (pResponse != null) {
			var q = null;
			var i = null;
			var a = null;
			var qtext = null;
			var conf = null;
			var expText = null;

			q = pResponse.question;
			a = pResponse.answers;

			if (pResponse.interpretations != null) {
				i = pResponse.interpretations[0];
			}
			
			if (q != null) {
				qText = q.text;
			} else {
				qText = "???";
			}

			result += '"' + qText + '"';

			if (i != null) {
				conf = i.confidence;
				expText = i.explanation;
			}
			
			if (expText != '') {
				result += ' (<a title="' + expText + '">' + conf + '</a>)';
			} else {
				result += ' (' + conf + ')';
			}

			if (this.isShowingDetails()) {
				result += '<ul>';

				if (a != null) {
					for (var i = 0; i < a.length; i++) {
						var thisA = a[i];
						
						result += '<li>';
						
						if (thisA.result_text != null) {
							result += '"' + thisA.result_text + '"';
						} else if (thisA.result_code != null) {
							result += '[Code:' + thisA.result_code + ']: "' + thisA.chatty_text + '"';
						} else if (thisA.result_media != null) {
							var m = thisA.result_media;

							result += '[Media]: id="' + m.id + '", url="' + m.url + '", credit="' + m.credit + '"';
						} else if (thisA.result_coords != null) {
							var c = thisA.result_coords;

							var latBit = '';
							if (c.lat != null) {
								latBit = ', lat="' + c.lat + '"';
							}

							var lonBit = '';
							if (c.lon != null) {
								lonBit = ', lon="' + c.lon + '"';
							}

							var al1Bit = '';
							if (c.address_line_1 != null) {
								al1Bit = ', address line 1="' + c.address_line_1 + '"';
							}

							var pcBit = '';
							if (c.postcode != null) {
								pcBit = ', postcode="' + c.postcode + '"';
							}

							result += '[Coords]: id="' + c.id + '"' + latBit + lonBit + al1Bit + pcBit;
						} else if (thisA.result_set != null) {
							result += '[Table]: ' + formattedTableLineFor(thisA.result_set);
						} else {
							result += '<font color="red">MISSING</font> ';
							
							gHudson.reportLog('Unhandled result type:');
							gHudson.reportLog(a);
						}
						
						result += ' (' + thisA.answer_confidence + ')';
						result += '</li>';
					}
				}

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
	
	function formattedTableLineFor(pRs) {
		var result = '';
		var hdrText = JSON.stringify(pRs.headers);
		var rowText = JSON.stringify(pRs.rows);
		
		result += hdrText + '->' + rowText;
		
		return result;
	}

	function updateAnswerText(pText) {
		setTextIn(DOM_AL, pText);
	}

	this.setDefaultQuestionRange = function(pMaxLen) {
		var rangeText = '0-' + pMaxLen;

		setTextIn(DOM_QR, rangeText);
		setTextIn(DOM_NQ, ' - there are ' + gHudson.allQuestions.length + ' predefined questions available');
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

	function ajaxErrorOther(pResponseText, pError, pCode, pUrl) {
		gHudson.reportLog(pResponseText);
		gHudson.reportLog(pCode);
		gHudson.reportLog('Unknown server error [' + pError + '] for url ' + pUrl);
	}
	
}
