/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gHudson = new Hudson(false);
var gTester = null;

//gHudson.loadQuestions();
gHudson.listDirectoryModels();

window.onload = function(){

	document.getElementById("directory_load").onclick = function(e){
		var directoryModlel = document.getElementById("directory_model").options[document.getElementById("directory_model").selectedIndex].value;
		gHudson.loadDirectoryModel(directoryModlel);
	}
};

function Hudson(pJsDebug) {
	var LOCAL_SERVER = '..';
	var REMOTE_SERVER = 'http://blah.blah.blah:8080/ce-store/';
	var URL_QUESTIONS_LIST = [ './test_json/questions_core.json', './test_json/questions_extra.json' ];

	var DOM_QT = 'questionText';
	var DOM_QP = 'questionPos';
	var DOM_AT = 'answer';
	var DOM_JT = 'json';
	var DOM_PQ = 'parsedQuestion';
	var DOM_EP = 'endpoint';
	var DOM_AN = 'answers_normal';
	var DOM_AC = 'answers_chatty';
	var DOM_UN = 'username';
	var DOM_LR = 'log_results';
	var URL_QH = '/special/hudson/helper';
	var URL_QE = '/special/hudson/executor';
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
	this.allQuestions = [];
	this.questionCount = 0;

//	this.sendJsonFileRequest = function(pUrl, pCbf) {
//		var xhr = new XMLHttpRequest();
//
//		xhr.open('GET', pUrl, true);
//
//		xhr.onload = function(e) {
//			if (this.status === 200) {
//				pCbf(JSON.parse(xhr.response));
//			} else {
//				ajaxErrorOther(xhr.response, e, this.status, pUrl);
//			}
//		};
//
//		xhr.onerror = function(e) {
//			ajaxErrorOther(xhr.response, e, this.status, pUrl);
//		};
//
//		xhr.send();
//	};

	this.listDirectoryModels = function(){
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
		  if (this.readyState == 4 && this.status == 200) {
				var html = '<option>Default</option>';
				if(this.responseText){
					var directory = JSON.parse(this.responseText);
					if(directory && directory.models){
						for(var i=0; i <directory.models.length; i++){
							html = html + "<option value='"+directory.models[i]+"'>"+directory.models[i]+'</option>';
						}
					}
					document.getElementById("directory_model").innerHTML = html;

				}

		  }
		};

		xhr.open("GET", '../special/hudson/directory_list', true);
		xhr.send();
	};

	this.loadDirectoryModel = function(model){
		this.doLoadDirectoryModel(model);
		this.doGetDirectoryQuestions(model);
		this.doGetDirectoryAnswers(model);
	};

	this.doLoadDirectoryModel = function(model){
		document.getElementById('directory_load_message').innerHTML = "Loading " + model + ".";

		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function(pResponse) {
		  if(this.readyState == 4 && this.status == 200) {
				document.getElementById('directory_load_message').innerHTML = "Finished loading.";
		  }
			else if(this.readyState == 4 && this.status != 200) {
				document.getElementById('directory_load_message').innerHTML = "Error loading. " + this.responseText;
			}
		};

		xhr.open("POST", '../special/hudson/directory_load', true);
		xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		xhr.send("model="+model);
	};

	this.doGetDirectoryQuestions = function(model){
		document.getElementById('directory_questions_message').innerHTML = "Getting questions for " + model + ".";

		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
		  if(this.readyState == 4 && this.status == 200) {
			  gHudson.handleDirectoryQuestions(this.response);
		  } else if(this.readyState == 4 && this.status != 200) {
			  document.getElementById('directory_questions_message').innerHTML = "Error getting questions. " + this.responseText;
		  }
		};

		xhr.open("POST", '../special/hudson/directory_get_questions', true);
		xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		xhr.send("model="+model);
	};

	this.doGetDirectoryAnswers = function(model){
		document.getElementById('directory_answers_message').innerHTML = "Getting answers for " + model + ".";

		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
		  if(this.readyState == 4 && this.status == 200) {
			  gHudson.handleDirectoryAnswers(this.response);
		  }
			else if(this.readyState == 4 && this.status != 200) {
				document.getElementById('directory_answers_message').innerHTML = "Error getting answers. " + this.responseText;
			}
		};

		xhr.open("POST", '../special/hudson/directory_get_answers', true);
		xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		xhr.send("model="+model);
	};

	this.handleDirectoryQuestions = function(pResponse) {
		var trimmed = pResponse.trim();
		var msgText = null;
		
		if (trimmed != "") {
			var questions = JSON.parse(trimmed);
			this.handleLoadQuestions(questions);
			msgText = this.questionCount + " questions retrieved.";
		} else {
			msgText = "0 questions retrieved.";
		}

		document.getElementById('directory_questions_message').innerHTML = msgText;
	};
	
	this.handleDirectoryAnswers = function(pResponse) {
		var trimmed = pResponse.trim();
		var msgText = null;
		
		if (trimmed != "") {
			var answers = JSON.parse(trimmed);

			if (gTester) {
				gTester.handleDefinitiveAnswersResponse(answers);
				msgText = gTester.answerCount + " answers retrieved.";
			} else {
				msgText = answers.length + " answers retrieved.";
			}
		} else {
			msgText = "0 answers retrieved.";
		}

		document.getElementById('directory_answers_message').innerHTML = msgText;
	};
	
//	this.loadQuestions = function(pCbf) {
//		var cbf = null;
//
//		if (pCbf == null) {
//			cbf = function(pResponse) { gHudson.handleLoadQuestions(pResponse); };
//		} else {
//			cbf = pCbf;
//		}
//
//		for (var i = 0; i < URL_QUESTIONS_LIST.length; i++) {
//			var thisUrl = URL_QUESTIONS_LIST[i];
//
//			this.sendJsonFileRequest(thisUrl, cbf);
//		}
//	};

	this.sortQuestions = function(pList) {
		pList.sort(sortById);
	};

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

	this.handleLoadQuestions = function(pResponse) {
		this.allQuestions = [];

		for (var i = 0; i < pResponse.length; i++) {
			var thisQ = pResponse[i];
			this.allQuestions.push(thisQ);
		}

		this.questionCount = pResponse.length;

		this.sortQuestions(this.allQuestions);
		this.loadCurrentQuestion();
		
		if (gTester) {
			gTester.renderQuestionList();
		}
	};

	this.isLoggingResults = function() {
		return this.getCheckboxValueFrom(DOM_LR);
	};

	this.getCurrentQuestion = function() {
		return this.allQuestions[getCqPos()];
	};

	this.getNextQuestion = function() {
		var cqPos = getCqPos();

		if (++cqPos >= this.allQuestions.length) {
			cqPos = 0;
		}

		setTextIn(DOM_QP, cqPos);

		return this.allQuestions[cqPos].question;
	};

	this.getPreviousQuestion = function() {
		var cqPos = getCqPos();

		if (--cqPos < 0) {
			cqPos = this.allQuestions.length - 1;
		}
		setTextIn(DOM_QP, cqPos);

		return this.allQuestions[cqPos].question;
	};

	this.loadCurrentQuestion = function() {
		var cq = this.getCurrentQuestion();

		if (cq != null) {
			var qText = cq.question;

			this.clearAnswerText();

			setTextIn(DOM_QT, qText);
			this.clearHelpText();
//			this.parseQuestionText();
		}
	};

	this.loadPreviousQuestion = function() {
		var qText = this.getPreviousQuestion();

		this.clearAnswerText();

		setTextIn(DOM_QT, qText);
		this.clearHelpText();
//		this.parseQuestionText();
	};

	this.loadNextQuestion = function() {
		var qText = this.getNextQuestion();

		this.clearAnswerText();

		setTextIn(DOM_QT, qText);
		this.clearHelpText();
//		this.parseQuestionText();
	};

//	this.resetCeStore = function() {
//		var cbf = function(pResponse) {gHudson.updateAnswer(pResponse);};
//
//		sendResetRequest(cbf, false);
//	};

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
					if (qpos >= this.allQuestions.length) {
						qpos = this.allQuestions.length - 1;
						setTextIn(DOM_QP, qpos);
					} else {
						setTextIn(DOM_QP, qpos);
					}
				}
			}

			this.loadCurrentQuestion();
		}
	};

	this.changeAnswerCheckbox = function(pDomId) {
		var isChecked = this.getCheckboxValueFrom(pDomId);

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

		sendHelpRequest(qText, cbf);
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

	this.clearHelpText = function() {
		setTextIn(DOM_PQ, '');
	};

	this.clearAnswerText = function() {
		setTextIn(DOM_AT, '');
		setTextIn(DOM_JT, '');
	};

	this.executeSpecificQuestion = function(pQuestionText, pCbf) {
		sendExecuteRequest(pQuestionText, pCbf);
	};

	this.interpretSpecificQuestion = function(pQuestionText, pCbf) {
		sendInterpretRequest(pQuestionText, pCbf);
	};

	this.answerSpecificQuestion = function(pQuestionText, pCbf) {
		pCbf();  // Temporary as not yet supported
//		sendAnswerRequest(pQuestionText, pCbf);
	};

	this.interpretQuestion = function() {
		var qText = getTextFrom(DOM_QT);
		var cbf = function(pResponse) {gHudson.updateInterpretation(pResponse);};

		this.clearAnswerText();

		this.interpretSpecificQuestion(qText, cbf);
	};

	this.answerInterpretation = function() {
		var qText = JSON.stringify(this.lastInterpretation);
		var cbf = function(pResponse) {gHudson.updateRawAnswer(pResponse);};

		this.clearAnswerText();

		this.answerSpecificInterpretation(qText, cbf);
	};

	this.answerSpecificInterpretation = function(pQuestionText, pCbf) {
		sendAnswerInterpretationRequest(pQuestionText, pCbf);
	};

	this.updateInterpretation = function(pResponse) {
		this.lastInterpretation = pResponse;

		if (this.isLoggingResults()) {
			this.reportLog(JSON.stringify(pResponse));
		}

		if (pResponse != null) {
			this.renderInterpretation(pResponse);
		}
	};

	this.renderInterpretation = function(pResponse) {
		var result = null;
		var confExp = null;
		var question = pResponse.question;
		var interpretation = pResponse.interpretations[0];

		result = "<table border='1'>";
		result += "<tr><td><b>Word</b></td><td><b>Matches</b></td></tr>";

		for (var i in question.words) {
			result += "<tr>";
			result += "<td>" + question.words[i] + "</td>";
			result += "<td>" + seekMatches(i, interpretation.result) + "</td>";
			result += "</tr>";
		}

		result += "</table>";

		if (interpretation.explanation) {
			confExp = " (" + interpretation.explanation + ")";
		} else {
			confExp = '';
		}
		
		result += "Confidence = " + interpretation.confidence + "%" + confExp;
		result += "<br><br>";

		result += "<a href=\"javascript:gHudson.answerInterpretation();\">Send this interpretation for answer</a>";

		result += "<br><br>";

		setTextIn(DOM_AT, result);
		setTextIn(DOM_JT, JSON.stringify(pResponse, null, 4));
	};

	function seekMatches(pIdx, pResponse) {
		var result = "";

		for (var i in pResponse.concepts) {
			var thisCon = pResponse.concepts[i];

			if (thisCon["start position"] == pIdx) {
				result += "\"" + thisCon.phrase + "\" = " + htmlForConcept(thisCon);
			}
		}

		for (var i in pResponse.properties) {
			var thisProp = pResponse.properties[i];

			if (thisProp["start position"] == pIdx) {
				result += "\"" + thisProp.phrase + "\" = " + htmlForProperty(thisProp);
			}
		}

		for (var i in pResponse.instances) {
			var thisInst = pResponse.instances[i];

			if (thisInst["start position"] == pIdx) {
				result += "\"" + thisInst.phrase + "\" = " + htmlForInstance(thisInst);
			}
		}

		for (var i in pResponse.specials) {
			var thisSpec = pResponse.specials[i];

			if (thisSpec["start position"] == pIdx) {
				result += "\"" + thisSpec.phrase + "\" = " + htmlForSpecial(thisSpec);
			}
		}

		return result;
	}

	function htmlForConcept(pCon) {
		var result = "";

		for (var i in pCon.entities) {
			var entity = pCon.entities[i];
			
			if (result != "") {
				result += ", ";
			}

			result += "<a title='" + JSON.stringify(entity) + "'><font color='blue'>" + entity._id + "</font></a> (";
			result += htmlConceptSummary(entity) + ")<br>";
		}

		return result;
	}

	function htmlForProperty(pProp) {
		var result = "";

		for (var i in pProp.entities) {
			var entity = pProp.entities[i];
			
			if (result != "") {
				result += ", ";
			}

			result = "<a title='" + JSON.stringify(entity) + "'><font color='blue'>" + entity._id + "</font></a> (";
			result += htmlPropertySummary(entity) + ")<br>";
		}

		return result;
	}

	function htmlForInstance(pInst) {
		var result = "";

		for (var i in pInst.entities) {
			var entity = pInst.entities[i];
			
			if (result != "") {
				result += ", ";
			}

			result = "<a title='" + JSON.stringify(entity) + "'><font color='blue'>" + entity._id + "</font></a> (";
			result += htmlInstanceSummary(entity) + ")<br>";
		}

		return result;
	}

	function htmlForSpecial(pSpec) {
		var result = null;

		result = "<a title='" + JSON.stringify(pSpec) + "'><font color='blue'>" + pSpec.phrase + "</font></a> (" + pSpec.type + ")<br>";

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

		this.clearAnswerText();

		if (this.isLoggingResults()) {
			this.reportLog(JSON.stringify(pResponse));
		}

		if (pResponse != null) {
			this.renderAnswers(pResponse);
		}
	};

	this.updateRawAnswer = function(pResponse) {
		if (this.isLoggingResults()) {
			this.reportLog(JSON.stringify(pResponse));
		}

		if (pResponse != null) {
			setTextIn(DOM_AT, htmlFormat(pResponse));
			setTextIn(DOM_JT, pResponse);
		}
	};

	this.renderAnswers = function(pResponse) {
		var answerText = '';

		if (pResponse != null) {
			if (pResponse.answer != null) {
				//Remove this temporary code when temporary simple answers are removed
				answerText = htmlFormat(pResponse.answer);
			} else {
				var question = pResponse.question;
				var alerts = pResponse.alerts;

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
						var confText = '<i>confidence for this answer=<font color="green">' + answer.confidence + '</font></i>';

						if (this.chattyAnswers) {
							if ((answer.chatty_text != null) && (answer.chatty_text != '')) {
								answerText += bullet + '<b>' + htmlFormat(answer.chatty_text) + '</b>';
								answerText += '<br/>[' + confText  + ']';
								answered = true;
							}
						}

						if (!answered) {
							if (answer["result text"] != null) {
								answerText += bullet + '<b>' + htmlFormat(answer["result text"]) + '</b>';
							} else if (answer["result set"] != null) {
								answerText += bullet + createHtmlForResultSet(answer["result set"]);
							} else if (answer["result media"] != null) {
								answerText += bullet + createHtmlForMedia(answer["result media"]);
							} else if (answer["result coords"] != null) {
								answerText += bullet + createHtmlForCoords(answer["result coords"]);
							} else if (answer["result code"] != null) {
								answerText += bullet + '<b>' + answer["result code"] + ': ' + answer["chatty text"] + '</b>';
							} else {
								answerText += bullet + '<b>NO ANSWER FOUND!</b>';
							}

							if (answer.source != null) {
								answerText += '<br/>[<i>source:<a href="' + answer.source.url + '">' + answer.source.name + '</a>, ' + confText + '</i>]';
							} else {
								answerText += '<br/>[' + confText + ']';
							}
						}
					}
				} else {
					if (pResponse["system message"] != null) {
						answerText = '<font color="green">' + pResponse["system message"] + '</font>';
						//Note that for management responses the execution time comes back in the root, not in the debug
						answerText += '<br/>Execution time (ms): <font color="red">' + pResponse["execution time"] + '</font>';
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
			}

			setTextIn(DOM_AT, answerText);
			setTextIn(DOM_JT, JSON.stringify(pResponse, null, 4));
		}
	};

	this.updateParsedQuestion = function(pResponse) {
		this.cachedHelp = pResponse;

		var pqText = '';

		if (pResponse != null) {
			for (var idx in pResponse.suggestions) {
				var thisSugg = pResponse.suggestions[idx];
				var qt = thisSugg["question text"];
				var wholeSugg = '';

				if (pqText !== '') {
					pqText += '<br>';
				}

				var bt = thisSugg["before text"];
				var at = thisSugg["after text"];

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

	function sendHelpRequest(pQuestionText, pCbf) {
		var url = getTextFrom(DOM_EP) + URL_QH;
		sendAjaxPost(url, pQuestionText, pCbf);
	}

	function sendExecuteRequest(pQuestionJson, pCbf) {
		var url = getTextFrom(DOM_EP) + URL_QE;
		sendAjaxPost(url, pQuestionJson, pCbf);
	}

	function sendInterpretRequest(pQuestionJson, pCbf) {
		var url = getTextFrom(DOM_EP) + URL_QI;
		sendAjaxPost(url, pQuestionJson, pCbf);
	}

	function sendAnswerInterpretationRequest(pQuestionJson, pCbf) {
		var url = getTextFrom(DOM_EP) + URL_QB;
		sendAjaxPost(url, pQuestionJson, pCbf);
	}

//	function sendResetRequest(pCbf) {
//		var url = getTextFrom(DOM_EP) + URL_QMR;
//		sendAjaxGet(url, pCbf);
//	}

	function sendStatusRequest(pCbf) {
		var url = getTextFrom(DOM_EP) + URL_QMS;
		sendAjaxGet(url, pCbf);
	}

	function getTextFrom(pDomId) {
		var elem = window.document.getElementById(pDomId);
		var contents = null;

		if (elem != null) {
			contents = elem.value;
		}

		return contents;
	}

	this.getCheckboxValueFrom = function(pDomId) {
		var elem = window.document.getElementById(pDomId);
		var result = null;

		if (elem != null) {
			result = elem.checked;
		}

		return result;
	};

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

	function sendAjaxGet(pUrl, pCbf) {
		sendAjaxRequest('GET', pUrl, null, pCbf);
	}

	function sendAjaxPost(pUrl, pParms, pCbf) {
		sendAjaxRequest('POST', pUrl, pParms, pCbf);
	}

	function sendAjaxRequest(pType, pUrl, pPostParms, pCbf) {
		var xhr = new XMLHttpRequest();

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
				var jResponse = null;

				try {
					jResponse = JSON.parse(xhr.response);
				} catch(e) {
//					gHudson.reportLog('Error parsing response:');
//					gHudson.reportLog(e);
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
		gHudson.reportLog(pResponseText);
		gHudson.reportLog(pCode);
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
				rows = [ [ pCoords.id, pCoords.lat, pCoords.lon, pCoords.postcode, , pCoords["address line 1"] ] ];
			} else {
				//lat lon only
				hdrs = [ 'id', 'lat', 'lon' ];
				rows = [ [ pCoords.id, pCoords.lat, pCoords.lon ] ];
			}
		} else {
			//No lat lon
			hdrs = [ 'id', 'postcode', 'address line 1' ];
			rows = [ [ pCoords.id, pCoords.postcode, pCoords["address line 1"] ] ];
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

	this.reportLog = function(pText) {
		console.log(pText);
	}

}
