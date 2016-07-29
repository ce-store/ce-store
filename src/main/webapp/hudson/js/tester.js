/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gTester = new Tester(true);

function Tester(pJsDebug) {
	var URL_DEBUG = './test_json/debug_local.json';
	var URL_NORMAL = './test_json/normal_local.json';

	var DOM_QL = 'question_list';
	var DOM_QR = 'question_range';
	var DOM_DE = 'delay';
	var DOM_IT = 'iterations';
	var DOM_AL = 'answer_list';
	var DOM_NQ = 'number_of_questions';
	var DOM_NL = 'normal_loaded';
	var DOM_DL = 'debug_loaded';
	var DOM_TR = 'test_results';
	var DOM_RSD = 'result_set_depth';
	
	var MAX_INITIAL_QUESTIONS = 55;

	this.jsDebug = pJsDebug;
	this.currentPos = 0;
	this.currentIteration = 0;
	this.selectedQuestions = null;
	this.definitiveNormalResponses = null;
	this.definitiveDebugResponses = null;
	this.testErrors = 0;
	this.testUnknowns = 0;
	this.testCount = 0;
	this.testTime = 0;
	this.totalQuestions = 0;

	this.initialiseTestPage = function() {
		var maxLen = gQuestions.getCannedQuestions().length - 1;
		
		if (maxLen > MAX_INITIAL_QUESTIONS) {
			maxLen = MAX_INITIAL_QUESTIONS;
		}
		
		this.renderQuestionList(1, maxLen);

		setDefaultQuestionRange(gQuestions.getCannedQuestions(), maxLen);
	};
	
	this.getCurrentPos = function() {
		return this.currentPos;
	};

	this.getCurrentIteration = function() {
		return this.currentIteration;
	};

	this.loadDefinitiveNormalResponses = function() {
		var cbf = function(pResponse) { gTester.handleStaticNormalJsonResponses(pResponse); };

		sendJsonFileRequest(URL_NORMAL, cbf);
	};

	this.loadDefinitiveDebugResponses = function() {
		var cbf = function(pResponse) { gTester.handleStaticDebugJsonResponses(pResponse); };

		sendJsonFileRequest(URL_DEBUG, cbf);
	};

	this.handleStaticNormalJsonResponses = function(pResponse) {
		this.definitiveNormalResponses = pResponse;
		
		var countUrl = hyperlinkForNewWindow(URL_NORMAL, definitiveCountTextFor(this.definitiveNormalResponses));
		
		setTextIn(DOM_NL, countUrl);
	};
	
	function hyperlinkForNewWindow(pUrl, pLinkText) {
		return '<a href="' + pUrl + '"target="_blank">' + pLinkText + '</a>';
	}

	this.handleStaticDebugJsonResponses = function(pResponse) {
		this.definitiveDebugResponses = pResponse;

		var countUrl = hyperlinkForNewWindow(URL_DEBUG, definitiveCountTextFor(this.definitiveDebugResponses));

		setTextIn(DOM_DL, countUrl);
	};

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
		var dr = null;

		if (gHudson.isDebug()) {
			if (this.definitiveDebugResponses != null) {
				dr = this.definitiveDebugResponses[pKey];
			}
		} else {
			if (this.definitiveNormalResponses != null) {
				dr = this.definitiveNormalResponses[pKey];
			}
		}

		return dr;
	};
	
	this.renderQuestionList = function(pStartIdx, pEndIdx) {
		var htmlText = '';

		this.selectedQuestions = [];

		if (pEndIdx >= gQuestions.getCannedQuestions().length) {
			pEndIdx = gQuestions.getCannedQuestions().length -1;
		}
		
		htmlText += '<ol>';

		for (var i = pStartIdx; i <= pEndIdx; i++) {
			var thisCqPair = gQuestions.getCannedQuestions()[i];
			
			if (thisCqPair != null) {
				this.selectedQuestions.push(thisCqPair);

				htmlText += '<li>' + thisCqPair[0] + ': ' + thisCqPair[1] + '</li>';
			}
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

		if (isNaN(numIts) || (numIts < 1)) {
			numIts = 1;
		}

		this.totalQuestions = (numIts * this.selectedQuestions.length);

		updateAnswerText(this.answerText);

		this.executeNextQuestion();
	};
	
	this.takeNextQuestion = function() {
		var result = null;
		var numIts = parseInt(getTextFrom(DOM_IT));

		if (isNaN(numIts) || (numIts < 1)) {
			numIts = 1;
		}

		if (this.currentPos < this.selectedQuestions.length) {
			result = this.selectedQuestions[this.currentPos++];
		} else {
			if (this.currentIteration++ < numIts) {
				this.currentPos = 0;
				result = this.selectedQuestions[this.currentPos++];
			}
		}

		return result;
	};

	this.executeNextQuestion = function() {
		var msDelay = parseInt(getTextFrom(DOM_DE)) + this.nextQuestionDelay;
		var nextQ = gTester.takeNextQuestion();

		if (nextQ != null) {
			var addedDelay = nextQ[2];
			if ((addedDelay != undefined) && (!isNaN(addedDelay))) {
				this.nextQuestionDelay = addedDelay;
			} else {
				this.nextQuestionDelay = 0;
			}

			setTimeout(function () {
				var qIdx = gTester.getCurrentPos();
				var qIt = gTester.getCurrentIteration();
				var cbf = function(pResponse) { gTester.handleAnswer(pResponse, qIdx, qIt, nextQ[0]); };

					gHudson.executeSpecificQuestion(nextQ[1], cbf);
	
					gTester.executeNextQuestion();
			}, msDelay);
		}
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

	this.handleAnswer = function(pResponse, pIdx, pIteration, pKey) {
		var dr = null;
		var matchError = 0;
		var markerText = null;

		dr = this.getDefinitiveResponseFor(pKey);
		
		if (dr != null) {
			var maxDepth = getTextFrom(DOM_RSD);

			matchError = gChecker.compareAnswerReplies(dr, pResponse, maxDepth);

			if (matchError === '') {
				markerText = '<font color="green">Good</font> ';
			} else {
				markerText = '<font color="red">Bad (' + matchError + ')</font> ';
				this.testErrors++;
			}
		} else {
			markerText = '<font color="grey">???</font> ';
			this.testUnknowns++;
		}
		
		if (pResponse.debug != null) {
			var et = pResponse.debug.execution_time_ms;
			
			if (et !== null) {
				this.testTime += et;
			}
		}

		this.answerText += answerTextFrom(pResponse, pIdx, pIteration, markerText);

		updateAnswerText(this.answerText);

		++this.testCount;
		
		this.reportResults();
	};

	this.keyUpQuestionRange = function() {
		var qrText = getTextFrom(DOM_QR);
		var parts = qrText.split('-');
		var failedParse = false;
		var firstNum = null;
		var secondNum = null;
		var maxNum = gQuestions.getCannedQuestions().length;
		
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
			setDefaultQuestionRange(gQuestions.getCannedQuestions(), MAX_INITIAL_QUESTIONS);
		}
	};

	function answerTextFrom(pResponse, pIdx, pIteration, pMarker) {
		var result = pMarker;
		
		result += '[' + pIteration + ':' + pIdx + '] ';

		if (pResponse != null) {
			var q = pResponse.question;
			var a = pResponse.answers;
			var d = pResponse.debug;

			if (q != null) {
				result += '"' + q.text + '"';
				result += ' (' + q.interpretation_confidence + ', ' + q.ability_to_answer_confidence + ')';

			} else {
				result += '(unknown question)<br/>';
			}
			
			if (d != null) {
				result += ' - ' + d.execution_time_ms + 'ms';
			}
			
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
						
						console.log('Unhandled result type:');
						console.log(a);
					}
					
					result += ' (' + thisA.answer_confidence + ')';
					result += '</li>';
				}
			}

			result += '</ul>';
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

	function setDefaultQuestionRange(pCqs, pMaxLen) {
		var rangeText = '1-' + pMaxLen;

		setTextIn(DOM_QR, rangeText);
		setTextIn(DOM_NQ, ' - there are ' + pCqs.length + ' predefined questions available');
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

	function sendJsonFileRequest(pUrl, pCbf) {
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
	}

	function ajaxErrorOther(pResponseText, pError, pCode, pUrl) {
		console.log(pResponseText);
		console.log(pCode);
		console.log('Unknown server error [' + pError + '] for url ' + pUrl);
	}

}