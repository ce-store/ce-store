/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gHudson = new Hudson(true);

function Hudson(pJsDebug) {
	var LOCAL_SERVER = '..';
	var REMOTE_SERVER = 'http://blah.blah.blah:8080/ce-store/';
	var DOM_QT = 'questionText';
	var DOM_QP = 'questionPos';
	var DOM_AT = 'answer';
	var DOM_PQ = 'parsedQuestion';
	var DOM_EP = 'endpoint';
	var DOM_DB = 'debug';
	var DOM_AN = 'answers_normal';
	var DOM_AC = 'answers_chatty';
	var DOM_UN = 'username';
	var DOM_LR = 'log_results';
	var URL_QH = '/special/hudson/helper';
	var URL_QE = '/special/hudson/executor';
	var URL_QA = '/special/hudson/analyser';
	var URL_QB = '/special/hudson/answerer';
	var URL_QI = '/special/hudson/interpreter';
	var URL_QMR = '/special/hudson/reset';
	var URL_QMS = '/special/hudson/status';
	var HDR_CEUSER = 'CE_User';

	this.jsDebug = pJsDebug;
	this.chattyAnswers = false;
	this.cachedHelp = null;
	this.cachedAnswer = null;
	this.lastInterpretation = null;

	this.isDebug = function() {
		return getCheckboxValueFrom(DOM_DB);
	};

	this.isLoggingResults = function() {
		return getCheckboxValueFrom(DOM_LR);
	};

	this.getCurrentCannedQuestion = function() {
		return gQuestions.getCannedQuestions()[getCqPos()];
	};
	
	this.getNextCannedQuestion = function() {
		var cqPos = getCqPos();

		if (++cqPos >= gQuestions.getCannedQuestions().length) {
			cqPos = 0;
		}
		setTextIn(DOM_QP, cqPos);
		
		return gQuestions.getCannedQuestions()[cqPos];
	};

	this.getPreviousCannedQuestion = function() {
		var cqPos = getCqPos();

		if (--cqPos < 0) {
			cqPos = gQuestions.getCannedQuestions().length - 1;
		}
		setTextIn(DOM_QP, cqPos);
		
		return gQuestions.getCannedQuestions()[cqPos];
	};

	this.loadCurrentCannedQuestion = function() {
		var cqPair = this.getCurrentCannedQuestion();

		this.clearAnswerText();

		setTextIn(DOM_QT, cqPair[1]);
		this.parseQuestionText();
	};

	this.loadPreviousCannedQuestion = function() {
		var cqPair = this.getPreviousCannedQuestion();

		this.clearAnswerText();

		setTextIn(DOM_QT, cqPair[1]);
		this.parseQuestionText();
	};

	this.loadNextCannedQuestion = function() {
		var cqPair = this.getNextCannedQuestion();

		this.clearAnswerText();

		setTextIn(DOM_QT, cqPair[1]);
		this.parseQuestionText();
	};

	this.resetCeStore = function() {
		var cbf = function(pResponse) {gHudson.updateAnswer(pResponse);};
		
		sendResetRequest(cbf, false);
	};

	this.getCeStoreStatus = function() {
		var cbf = function(pResponse) {gHudson.updateAnswer(pResponse);};
		
		sendStatusRequest(cbf, false);
	};

	this.keyUpQuestionText = function() {
		this.parseQuestionText();
	};
	
	this.keyUpQuestionPos = function() {
		var rawText = getTextFrom(DOM_QP);

		if (rawText != '') {
			var qpos = parseInt(rawText);
			
			if (isNaN(qpos)) {
				qpos = 0;
				setTextIn(DOM_QP, '0');
			} else {
				if (qpos < 0) {
					qpos = 0;
					setTextIn(DOM_QP, qpos);
				} else {
					if (qpos >= gQuestions.getCannedQuestions().length) {
						qpos = gQuestions.getCannedQuestions().length - 1;
						setTextIn(DOM_QP, qpos);
					} else {
						setTextIn(DOM_QP, qpos);
					}
				}
			}

			this.loadCurrentCannedQuestion();
		}
	};

	this.changeAnswerCheckbox = function(pDomId) {
		var isChecked = getCheckboxValueFrom(pDomId);

		if (pDomId === DOM_AN) {
			//The "normal" checkbox has changed
			if (isChecked) {
				setCheckboxValueTo(DOM_AC, false);
				this.chattyAnswers = false;
			} else {
				setCheckboxValueTo(DOM_AC, true);
				this.chattyAnswers = true;
			}
		} else {
			//The "chatty" checkbox has changed
			if (isChecked) {
				setCheckboxValueTo(DOM_AN, false);
				this.chattyAnswers = true;
			} else {
				setCheckboxValueTo(DOM_AN, true);
				this.chattyAnswers = false;
			}
		}

		this.switchAnswerStyle(this.chattyAnswers);
	};

	this.switchAnswerStyle = function(pIsChatty) {
		this.renderAnswers(this.cachedAnswer);
	};

	this.parseQuestionText = function() {
		var qText = getTextFrom(DOM_QT);
		var cbf = function(pResponse) {gHudson.updateParsedQuestion(pResponse);};
		
		sendHelpRequest(qText, cbf, this.isDebug());
	};

	this.getUserName = function() {
		return getTextFrom(DOM_UN);
	};

	this.executeQuestion = function() {
		var qText = getTextFrom(DOM_QT);
		var cbf = function(pResponse) {gHudson.updateAnswer(pResponse);};

		this.clearAnswerText();

		this.executeSpecificQuestion(qText, cbf);
	};

	this.clearAnswerText = function() {
		setTextIn(DOM_AT, '');
	};

	this.executeSpecificQuestion = function(pQuestionText, pCbf) {
		sendExecuteRequest(pQuestionText, pCbf, this.isDebug());
	};

	this.analyseQuestion = function() {
		var qText = getTextFrom(DOM_QT);
		var cbf = function(pResponse) {gHudson.updateAnswer(pResponse);};

		setTextIn(DOM_AT, '');

		this.analyseSpecificQuestion(qText, cbf);
	};

	this.interpretQuestion = function() {
		var qText = getTextFrom(DOM_QT);
		var cbf = function(pResponse) {gHudson.updateInterpretation(pResponse);};

		setTextIn(DOM_AT, '');

		this.interpretSpecificQuestion(qText, cbf);
	};

	this.answerInterpretation = function() {
		var qText = JSON.stringify(this.lastInterpretation);
		var cbf = function(pResponse) {gHudson.updateRawAnswer(pResponse);};

		setTextIn(DOM_AT, '');

		this.answerSpecificInterpretation(qText, cbf);
	};

	this.analyseSpecificQuestion = function(pQuestionText, pCbf) {
		sendAnalyseRequest(pQuestionText, pCbf, this.isDebug());
	};

	this.interpretSpecificQuestion = function(pQuestionText, pCbf) {
		sendInterpretRequest(pQuestionText, pCbf, this.isDebug());
	};

	this.answerSpecificInterpretation = function(pQuestionText, pCbf) {
		sendAnswerInterpretationRequest(pQuestionText, pCbf, this.isDebug());
	};

	this.showInterpretations = function() {
		var intText = '';
		
		if (this.cachedHelp != null) {
			if (this.cachedHelp.debug != null) {
				var rawInts = this.cachedHelp.debug.interpretations;

				if (rawInts != null) {
					intText += '<ol>';
					for (var i = 0; i < rawInts.length; i++) {
						var thisInt = rawInts[i];
						intText += '<li>' + thisInt + '</li>';
					}
					intText += '</ol>';
				}
			}
		}

		if (intText == '') {
			if (this.isDebug()) {
				intText = 'No interpretations were returned<br/><br/>';
			} else {
				intText = 'You need to switch on debug mode before interpretations are returned<br/><br/>';
			}
		}

		setTextIn(DOM_PQ, intText);
	};

	this.updateInterpretation = function(pResponse) {
		this.lastInterpretation = pResponse;

		if (pResponse != null) {
			this.renderInterpretation(pResponse);
		}
	};

	this.renderInterpretation = function(pResponse) {
		var result = null;

		result = "<table border='1'>";
		result += "<tr><td><b>Word</b></td><td><b>Matches</b></td></tr>";

		for (var i in pResponse.words) {
			result += "<tr>";
			result += "<td>" + pResponse.words[i] + "</td>";
			result += "<td>" + seekMatches(i, pResponse) + "</td>";
			result += "</tr>";
		}

		result += "</table>";

		result += "<br><br>";
		
		result += "<a href=\"javascript:gHudson.answerInterpretation();\">Send this interpretation for answer</a>";
		
		result += "<br><br>";

		result += JSON.stringify(pResponse);

		setTextIn(DOM_AT, result);
	};

	function seekMatches(pIdx, pResponse) {
		var result = "";

		for (var i in pResponse.concepts) {
			var thisCon = pResponse.concepts[i];

			if (thisCon.position == pIdx) {
				result += "\"" + i + "\" = " + htmlForConcept(thisCon);
			}
		}

		for (var i in pResponse.properties) {
			var thisProp = pResponse.properties[i];

			if (thisProp.position == pIdx) {
				result += "\"" + i + "\" = " + htmlForProperty(thisProp);
			}
		}

		for (var i in pResponse.instances) {
			var thisInst = pResponse.instances[i];

			if (thisInst.position == pIdx) {
				result += "\"" + i + "\" = " + htmlForInstance(thisInst);
			}
		}

		for (var i in pResponse.specials) {
			var thisSpec = pResponse.specials[i];

			if (thisSpec.position == pIdx) {
				result += "\"" + i + "\" = " + htmlForSpecial(thisSpec);
			}
		}

		return result;
	}

	function htmlForConcept(pCon) {
		var result = null;

		result = "<a title='" + JSON.stringify(pCon) + "'><font color='blue'>" + pCon.instance._id + "</font></a> (";
		result += htmlConceptSummary(pCon.instance) + ")<br>";

		return result;
	}

	function htmlForProperty(pProp) {
		var result = null;

		result = "<a title='" + JSON.stringify(pProp) + "'><font color='blue'>" + pProp.instance._id + "</font></a> (";
		result += htmlPropertySummary(pProp.instance) + ")<br>";

		return result;
	}

	function htmlForInstance(pInst) {
		var result = null;

		result = "<a title='" + JSON.stringify(pInst) + "'><font color='blue'>" + pInst.instance._id + "</font></a> (";
		result += htmlInstanceSummary(pInst.instance) + ")<br>";

		return result;
	}

	function htmlForSpecial(pSpec) {
		var result = null;

		result = "<a title='" + JSON.stringify(pSpec) + "'><font color='blue'>" + pSpec.name + "</font></a> (" + pSpec.type + ")<br>";

		return result;
	}

	function htmlInstanceSummary(pInst) {
		var result = null;
		var conList = removeMetaConceptsFrom(pInst._concept);

		result = "instance [" + conList + "]";

		return result;
	}

	function htmlPropertySummary(pInst) {
		var result = null;
		var conList = removeMetaConceptsFrom(pInst._concept);

		result = "property [" + conList + "]";

		return result;
	}

	function htmlConceptSummary(pInst) {
		var result = null;
		var conList = removeMetaConceptsFrom(pInst._concept);

		result = "concept [" + conList + "]";

		return result;
	}

	function removeMetaConceptsFrom(pList) {
		var metaList = ["configuration concept", "linguistic thing", "suppressed concept", "local concept", "meaning", "concept", "property concept" ];
		var result = [];

		for (var i in pList) {
			var thisItem = pList[i];

			if (metaList.indexOf(thisItem) == -1) {
				result.push(thisItem);
			}
		}

		return result;
	}

	this.updateAnswer = function(pResponse) {
		this.cachedAnswer = pResponse;

		if (this.isLoggingResults()) {
			console.log(JSON.stringify(pResponse));
		}

		if (pResponse != null) {
			this.renderAnswers(pResponse);
		}
	};

	this.updateRawAnswer = function(pResponse) {
		if (pResponse != null) {
			setTextIn(DOM_AT, htmlFormat(pResponse));
		}
	};

	this.renderAnswers = function(pResponse) {
		var answerText = '';

		if (pResponse != null) {
			var question = pResponse.question;
			var debug = pResponse.debug;
			var alerts = pResponse.alerts;

			if (debug != null) {
				if (debug.execution_time_ms != null) {
					answerText += 'Execution time (ms): <font color="red">' + debug.execution_time_ms + '</font>';
				}
			} 

			if (pResponse.answers != null) {
				for (var idx in pResponse.answers) {
					var answer = pResponse.answers[idx];
					var bullet = '';
					
					if (pResponse.answers.length > 1) {
						bullet = (parseInt(idx) + 1) + ') ';
					}

					if (answerText !== '') {
						answerText += '<br/><br/>';
					}

					var answered = false;
					var confText = '<i>confidence for this answer=<font color="green">' + answer.answer_confidence + '</font></i>';

					var intText = ', <i>interpretation=<b>' + answer.question_interpretation + '</b></i>'; 

					if (this.chattyAnswers) {
						if ((answer.chatty_text != null) && (answer.chatty_text != '')) {
							answerText += bullet + '<b>' + htmlFormat(answer.chatty_text) + '</b>';
							answerText += '<br/>[' + confText + intText + ']';
							answered = true;
						}
					}
					
					if (!answered) {
						if (answer.result_text != null) {
							answerText += bullet + '<b>' + htmlFormat(answer.result_text) + '</b>';
						} else if (answer.result_set != null) {
							answerText += bullet + createHtmlForResultSet(answer.result_set);
						} else if (answer.result_media != null) {
							answerText += bullet + createHtmlForMedia(answer.result_media);
						} else if (answer.result_coords != null) {
							answerText += bullet + createHtmlForCoords(answer.result_coords);
						} else if (answer.result_code != null) {
							answerText += bullet + '<b>' + answer.result_code + ': ' + answer.chatty_text + '</b>';
						} else {
							answerText += bullet + '<b>NO ANSWER FOUND!</b>';
						}

						if (answer.source != null) {
							answerText += '<br/>[<i>source:<a href="' + answer.source.url + '">' + answer.source.name + '</a>, ' + confText + intText + '</i>]';
						} else {
							answerText += '<br/>[' + confText + intText + ']';
						}
					}
				}
			} else {
				if (pResponse.system_message != null) {
					answerText = '<font color="green">' + pResponse.system_message + '</font>';
					//Note that for management responses the execution time comes back in the root, not in the debug
					answerText += '<br/>Execution time (ms): <font color="red">' + pResponse.execution_time_ms + '</font>';
				}
			}
			
			if (question != null) {
				answerText = '[<i>interpretation confidence=<font color="green">' + question.interpretation_confidence + '</font></i>, <i>ability to answer confidence=<font color="green">' + question.ability_to_answer_confidence + '</font></i>]<br/><br/>' + answerText;

				if (debug != null) {
					if (debug.sql_query != null) {
//						answerText += '<br/><br/>SQL:<br/><font color="green">' + sqlFormat(debug.sql_query) + '</font>';
						answerText += '<br/><br/>SQL:<br/><font color="green">' + htmlFormat(debug.sql_query) + '</font>';
					}
				} 
			}

			if (alerts != null) {
				if (alerts.errors != null) {
					answerText += '<br/><br/><hr/><u>Errors:</u><ul>';
					for (var i = 0; i < alerts.errors.length; i++) {
						var thisError = alerts.errors[i];
						
						answerText += '<li>' + htmlFormat(thisError) + '</li>';
					}
					
					answerText += '</ul>';
				}

				if (alerts.warnings != null) {
					answerText += '<br/><br/><hr/><u>Warnings:</u><ul>';
					for (var i = 0; i < alerts.warnings.length; i++) {
						var thisWarning = alerts.warnings[i];
						
						answerText += '<li>' + htmlFormat(thisWarning) + '</li>';
					}
					
					answerText += '</ul>';
				}				
			}

			if (debug != null) {
				if (debug.debugs != null) {
					answerText += '<br/><br/><hr/><u>Debugs:</u><ul>';
					for (var i = 0; i < debug.debugs.length; i++) {
						var thisDebug = debug.debugs[i];
						
						answerText += '<li>' + htmlFormat(thisDebug) + '</li>';
					}
					
					answerText += '</ul>';
				}
			}

			setTextIn(DOM_AT, answerText);
		}
	};

	this.updateParsedQuestion = function(pResponse) {
		this.cachedHelp = pResponse;

		var pqText = '';

		if (pResponse != null) {
			var debug = pResponse.debug;

			if (debug != null) {
				if (debug.execution_time_ms != null) {
					pqText += 'Execution time (ms): <font color="red">' + debug.execution_time_ms + '</font>';
				}
			} 

			for (var idx in pResponse.suggestions) {
				var thisSugg = pResponse.suggestions[idx];
				var qt = thisSugg.question_text;
				var wholeSugg = '';

				if (pqText !== '') {
					pqText += '<br>';
				}

				var bt = thisSugg.before_text;
				var at = thisSugg.after_text;

				if (bt != null) {
					pqText += '<b>' + bt + '</b>';
					wholeSugg += bt;
				}

				pqText += qt;
				wholeSugg += qt;

				if (at != null) {
					pqText += '<b>' + at + '</b>';
					wholeSugg += at;
				}

				pqText += ' [<a href="javascript:gHudson.useSuggestion(\'' + wholeSugg + '\');">use</a>]';
			}
		} else {
			pqText = JSON.stringify(pResponse);
		}

		setTextIn(DOM_PQ, pqText);
	};
	
	this.useSuggestion = function(pText) {
		setTextIn(DOM_QT, pText);
		this.keyUpQuestionText();
	};

	this.swapEndpoint = function() {
		var currentEp = getTextFrom(DOM_EP);

		if (currentEp === LOCAL_SERVER) {
			newEp = REMOTE_SERVER;
		} else {
			newEp = LOCAL_SERVER;
		}

		setTextIn(DOM_EP, newEp);
	};

	function sendHelpRequest(pQuestionText, pCbf, pDebug) {
		var url = getTextFrom(DOM_EP) + URL_QH;
		sendAjaxPost(url, pQuestionText, pCbf, pDebug);
	}
	
	function sendExecuteRequest(pQuestionJson, pCbf, pDebug) {
		var url = getTextFrom(DOM_EP) + URL_QE;
		sendAjaxPost(url, pQuestionJson, pCbf, pDebug);
	}

	function sendAnalyseRequest(pQuestionJson, pCbf, pDebug) {
		var url = getTextFrom(DOM_EP) + URL_QA;
		sendAjaxPost(url, pQuestionJson, pCbf, pDebug);
	}

	function sendInterpretRequest(pQuestionJson, pCbf, pDebug) {
		var url = getTextFrom(DOM_EP) + URL_QI;
		sendAjaxPost(url, pQuestionJson, pCbf, pDebug);
	}

	function sendAnswerInterpretationRequest(pQuestionJson, pCbf, pDebug) {
		var url = getTextFrom(DOM_EP) + URL_QB;
		sendAjaxPost(url, pQuestionJson, pCbf, pDebug);
	}

	function sendResetRequest(pCbf, pDebug) {
		var url = getTextFrom(DOM_EP) + URL_QMR;
		sendAjaxGet(url, pCbf, pDebug, false);
	}

	function sendStatusRequest(pCbf, pDebug) {
		var url = getTextFrom(DOM_EP) + URL_QMS;
		sendAjaxGet(url, pCbf, pDebug, false);
	}

	function getTextFrom(pDomId) {
		var elem = window.document.getElementById(pDomId);
		var contents = null;

		if (elem != null) {
			contents = elem.value;
		}

		return contents;
	}

	function getCheckboxValueFrom(pDomId) {
		var elem = window.document.getElementById(pDomId);
		var result = null;
		
		if (elem != null) {
			result = elem.checked;
		}

		return result;
	}
	
	function setCheckboxValueTo(pDomId, pFlag) {
		var elem = window.document.getElementById(pDomId);

		if (elem != null) {
			elem.checked = pFlag;
		}
	}

	function getCqPos() {
		return parseInt(getTextFrom(DOM_QP));
	}
	
	function setTextIn(pDomId, pText) {
		var elem = window.document.getElementById(pDomId);

		if (elem != null) {
			//It's nasty setting both of these but is means one function for divs and textareas
			elem.value = pText;
			elem.innerHTML = pText;
		}
	}

	function sendAjaxGet(pUrl, pCbf, pDebug) {
		sendAjaxRequest('GET', pUrl, null, pCbf, pDebug);
	}

	function sendAjaxPost(pUrl, pParms, pCbf, pDebug) {
		sendAjaxRequest('POST', pUrl, pParms, pCbf, pDebug);
	}

	function sendAjaxRequest(pType, pUrl, pPostParms, pCbf, pDebug) {
		var xhr = new XMLHttpRequest();

		if (pDebug) {
			pUrl += '?';

			if (pDebug) {
				pUrl += 'debug=true';
			}
		}

		var userName = gHudson.getUserName();

		xhr.open(pType, pUrl, true);

		if (userName != null) {
			xhr.setRequestHeader(HDR_CEUSER, userName);
		}

		xhr.withCredentials = true;
		xhr.setRequestHeader('Accept', 'application/json');
		xhr.setRequestHeader('Content-type', 'text/plain; charset=utf-8');

		xhr.onload = function(e) {
			if (this.status === 200) {
				try {
					pCbf(JSON.parse(xhr.response));
				} catch(e) {
					pCbf(xhr.response);
				}
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
		console.log(pResponseText);
		console.log(pCode);
		reportError('Unknown server error [' + pError + '] for url ' + pUrl);
	}
	
	function reportError(pErrorText) {
		setTextIn('answer', '<font color="red">' + pErrorText + '</font>');
	}
	
	function createHtmlForResultSet(pAnswerSet) {
		var result = '';
		var hdrs = pAnswerSet.headers;
		var rows = pAnswerSet.rows;

		if (pAnswerSet.title != null) {
			result = 'Title: ' + pAnswerSet.title;
		}

		result += renderTable(hdrs, rows);

		return result;
	}

	function createHtmlForMedia(pMedia) {
		var result = '';
		var hdrs = [ 'id', 'url', 'credit' ];
		var rows = [ [ pMedia.id, pMedia.url, pMedia.credit ] ];

		result = renderTable(hdrs, rows);

		return result;
	}

	function createHtmlForCoords(pCoords) {
		var result = '';
		var hdrs = null;
		var rows = null;
		
		if (pCoords.lat != null) {
			if (pCoords.postcode != null) {
				//lat lon and postcode
				hdrs = [ 'id', 'lat', 'lon' , 'postcode', 'address line 1'];
				rows = [ [ pCoords.id, pCoords.lat, pCoords.lon, pCoords.postcode, , pCoords.address_line_1 ] ];
			} else {
				//lat lon only
				hdrs = [ 'id', 'lat', 'lon' ];
				rows = [ [ pCoords.id, pCoords.lat, pCoords.lon ] ];
			}
		} else {
			//No lat lon
			hdrs = [ 'id', 'postcode', 'address line 1' ];
			rows = [ [ pCoords.id, pCoords.postcode, pCoords.address_line_1 ] ];
		}
		
		result = renderTable(hdrs, rows);

		return result;
	}

	function renderTable(pHdrs, pRows) {
		var result = '';
		
		result += '<table border="1">';

		result += '<tr>';
		for (var i = 0; i < pHdrs.length; i++) {
			var thisHdr = pHdrs[i];
			result += '<td><b>' + thisHdr + '</b></td>';
		}
		result += '</tr>';

		for (var i = 0; i < pRows.length; i++) {
			result += '<tr>';
			var thisRow = pRows[i];
			for (var j = 0; j < thisRow.length; j++) {
			var thisVal = thisRow[j];
				result += '<td>' + htmlFormat(thisVal) + '</td>';
			}
			result += '</tr>';
		}
		
		result += '</table>';

		return result;
	}
	
	function htmlFormat(pText) {
		var result = pText;

		if (result != null) {
			result = local_escape(result);
			result = replaceAll(result, '\n', '<BR/>');
			result = replaceAll(result, ' ', '&nbsp;');
			result = replaceAll(result, '	', '&nbsp;&nbsp;&nbsp;');
		}

		return result;
	}

	function sqlFormat(pText) {
		var result = htmlFormat(pText);

		if (result != null) {
			result = replaceAll(result, '&nbsp;', '+');
			result = replaceAll(result, '<BR/>', '|<BR/>');
		}

		return result;
	}

	function local_escape(pVal) {
		return pVal.replace(new RegExp('<', 'g'), '&lt;').replace(new RegExp('>', 'g'), '&gt;');
	}

	function replaceAll(pOrig, pSource, pTarget) {
		//Replace all occurrences of pSource with pTarget in pOrig, returning the updated result
		var result = pOrig;

		if (pOrig !== null) {
			result = pOrig.replace(new RegExp(pSource, 'g'), pTarget);
		}

		return result;
	}

}