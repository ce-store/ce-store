/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gChecker = new Checker();

function Checker() {

	this.compareAnswerReplies = function(pAr1, pAr2, pMaxDepth) {
		var result = '';

		if ((pAr1 != null) && (pAr2 != null)) {
			//question
			result += compareQuestions(pAr1.question, pAr2.question);
			
			//answers
			result += compareAnswerLists(pAr1.answers, pAr2.answers, pMaxDepth);

			//alerts
			result += compareAlerts(pAr1.alerts, pAr2.alerts);		
			
			//debug
			result += compareDebugs(pAr1.debug, pAr2.debug);
		} else {
			result += '!R|';	//One or both of the replies is null
		}
		
		if (result != '') {
			console.log('Difference detected:');
			console.log(JSON.stringify(pAr1));
			console.log(JSON.stringify(pAr2));
		}

		return result;
	};

	function compareQuestions(pQuest1, pQuest2) {
		var result = '';

		if ((pQuest1 != null) && (pQuest2 != null)) {
			//question.text
			result += compareSimpleValues(pQuest1.text, pQuest2.text, 'Q.T');

			//question.interpretation_confidence
			result += compareSimpleValues(pQuest1.interpretation_confidence, pQuest2.interpretation_confidence, 'Q.IC');

			//question.ability_to_answer_confidence
			result += compareSimpleValues(pQuest1.ability_to_answer_confidence, pQuest2.ability_to_answer_confidence, 'Q.AC');
		} else {
			result += '!Q|';	//One or both of the questions is null
		}

		return result;
	}
	
	function compareAnswerLists(pAnsList1, pAnsList2, pMaxDepth) {
		var result = '';

		if ((pAnsList1 != null) && (pAnsList2 != null)) {
			if (pAnsList1.length === pAnsList2.length) {
				for (var i = 0; i < pAnsList1.length; i++) {
					var ans1 = pAnsList1[i];
					var ans2 = pAnsList2[i];
					
					if ((ans1 != null) && (ans2 != null)) {
						//individual answer
						result += compareAnswers(ans1, ans2, pMaxDepth);
					} else {
						result += 'A.~A|'; //One or both of the answers is null
					}
				}
			} else {
				result += 'A.#A|'; //The number of answers don't match
			}
		} else {
			result += '!A|'; //One or both answer lists is null
		}

		return result;
	}
	
	function compareAnswers(pAns1, pAns2, pMaxDepth) {
		var result = '';

		//answer.answer_confidence
		result += compareSimpleValues(pAns1.answer_confidence, pAns2.answer_confidence, 'A.AC');
		
		//answer.answer_chatty_text
		result += compareSimpleValues(pAns1.chatty_text, pAns2.chatty_text, 'A.CT');

		//answer.source
		//result += compareSources(pAns1.source, pAns2.source, 'A');

		//answer.question_interpretation
		result += compareSimpleValues(pAns1.question_interpretation, pAns2.question_interpretation, 'A.QI');

		//check the various answer types
		result += checkAnswerTypes(pAns1, pAns2, pMaxDepth);

		return result;
	}
	
	function checkAnswerTypes(pAns1, pAns2, pMaxDepth) {
		var result = '';

		//answer.result_text
		result += compareSimpleValues(pAns1.result_text, pAns2.result_text, 'A.RT');

		//answer.result_code
		result += compareSimpleValues(pAns1.result_code, pAns2.result_code, 'A.RC');

		//answer.result_media
		result += compareResultMedia(pAns1.result_media, pAns2.result_media);

		//answer.result_coords
		result += compareResultCoords(pAns1.result_coords, pAns2.result_coords);

		//answer.result_set
		result += compareResultSet(pAns1.result_set, pAns2.result_set, pMaxDepth);
		
		return result;
	}
	
	function compareResultMedia(pRm1, pRm2) {
		var result = '';

		if ((pRm1 != null) && (pRm2 != null)) {
			//result_media.id
			result += compareSimpleValues(pRm1.id, pRm2.id, 'A.RMI');

			//result_media.url
			result += compareSimpleValues(pRm1.url, pRm2.url, 'A.RMU');

			//result_media.credit
			result += compareSimpleValues(pRm1.credit, pRm2.credit, 'A.RMC');
		}
		
		return result;
	}

	function compareResultCoords(pRc1, pRc2) {
		var result = '';

		if ((pRc1 != null) && (pRc2 != null)) {
			//result_coords.id
			result += compareSimpleValues(pRc1.id, pRc2.id, 'A.RCI');

			//result_coords.lat
			result += compareSimpleValues(pRc1.lat, pRc2.lat, 'A.RCLa');

			//result_coords.lon
			result += compareSimpleValues(pRc1.lon, pRc2.lon, 'A.RCLo');

			//result_coords.postcode
			result += compareSimpleValues(pRc1.lon, pRc2.lon, 'A.RCPo');

			//result_coords.address_line_1
			result += compareSimpleValues(pRc1.lon, pRc2.lon, 'A.RCAl1');
		}

		return result;
	}

	function compareResultSet(pRs1, pRs2, pMaxDepth) {
		var result = '';

		if ((pRs1 != null) && (pRs2 != null)) {
			//result_set.title
			result += compareSimpleValues(pRs1.title, pRs2.title, false, 'A.RST');

			//result_set.headers
			result += compareArrayValues(pRs1.headers, pRs2.headers, false, 'A.RSH');

			//result_set.rows
			var tgtRows = null;
			if (pRs1.rows.length > pMaxDepth) {
				tgtRows = [];
				
				for (var i = 0; i < pMaxDepth; i++) {
					tgtRows.push(pRs1.rows[i]);
				}
			} else {
				tgtRows = pRs1.rows;
			}
			
			result += compareArrayOfArrayValues(tgtRows, pRs2.rows, false, 'A.RSR');
		}

		return result;
	}

	function compareArrayValues(pArr1, pArr2, pAllowEmpty, pErrorCode) {
		var result = '';
		var matching = true;

		if ((pArr1 != null) && (pArr2 != null)) {
			if ((pArr1.length) === (pArr2.length)) {
				for (var i = 0; i < pArr1.length; i++) {
					if (matching) {
						var val1 = pArr1[i];
						var val2 = pArr2[i];
						matching = (val1 === val2);
	
						if (!matching) {
							result += pErrorCode + '|';
						}
					}					
				}
			} else {
				//The arrays have different lengths
				result += '#' + pErrorCode + '|';
			}
		} else {
			if (pAllowEmpty) {
				//They are both allowed to be empty so check
				if ((pArr1 == null) && (pArr2 == null)) {
					//They are both empty so ignore
				} else {
					//Only one is empty - this is an error
					result += '!' + pErrorCode + '|';
				}
			} else {
				//One or both is empty and that is not allowed
				result += '!' + pErrorCode + '|';
			}
		}

		return result;
	}
	
	function compareArrayOfArrayValues(pArr1, pArr2, pAllowEmpty, pErrorCode) {
		var result = '';

		if ((pArr1 != null) && (pArr2 != null)) {
			if ((pArr1.length) === (pArr2.length)) {
				for (var i = 0; i < pArr1.length; i++) {
					if (result == '') {
						var innerArr1 = pArr1[i];
						var innerArr2 = pArr2[i];

						result += compareArrayValues(innerArr1, innerArr2, pAllowEmpty, pErrorCode + '[' + i + ']');
					}					
				}
			} else {
				//The arrays have different lengths
				result += '#' + pErrorCode + '|';
			}
		} else {
			if (pAllowEmpty) {
				//They are both allowed to be empty so check
				if ((pArr1 == null) && (pArr2 == null)) {
					//They are both empty so ignore
				} else {
					//Only one is empty - this is an error
					result += '!' + pErrorCode + '|';
				}
			} else {
				//One or both is empty and that is not allowed
				result += '!' + pErrorCode + '|';
			}
		}

		return result;
	}

	function compareDebugs(pDebug1, pDebug2) {
		var result = '';

		if ((pDebug1 != null) && (pDebug2 != null)) {
			//debug.sql_query
			result += compareSimpleValues(pDebug1.sql_query, pDebug2.sql_query, 'D.SQ');

			//These are no longer checked because full debug info is returned
//			//debug.debugs
//			result += compareArrayValues(pDebug1.debugs, pDebug2.debugs, true, 'D.D');

			//debug.execution_time_ms does not need to be checked as it will vary
		}

		return result;
	}

	function compareAlerts(pAl1, pAl2) {
		var result = '';

		if ((pAl1 != null) && (pAl2 != null)) {
			var trimmedErrors = null;

			if (pAl2.errors != null) {
				//Manually remove an 'Maximum rows exceeded' errors as these can safely be ignored
				//since the user can specify an arbitrary maximum value of results to test against
				//in the text UI.
				trimmedErrors = [];

				for (var i = 0; i < pAl2.errors.length; i++) {
					var thisError = pAl2.errors[i];

					if (thisError != 'Maximum rows exceeded') {
						trimmedErrors.push(thisError);
					}
				}
				
				if (trimmedErrors.length == 0) {
					trimmedErrors = null;
				}
			}
			
			//alerts.errors
			result += compareArrayValues(pAl1.errors, trimmedErrors, true, 'A.E');

			//alerts.warnings
			result += compareArrayValues(pAl1.warnings, pAl2.warnings, true, 'A.W');			
		}
		
		return result;
	}

	function compareSources(pSrc1, pSrc2, pErrorContext) {
		var result = '';

		if ((pSrc1 != null) && (pSrc2 != null)) {
			//source.name
			result += compareSimpleValues(pSrc1.name, pSrc2.name, pErrorContext + '.SN');

			//source.url
			result += compareSimpleValues(pSrc1.url, pSrc2.url, pErrorContext + '.SU');
		} else {
			if ((pSrc1 == null) && (pSrc2 == null)) {
				//They are both null - can be ignored
			} else {
				result += pErrorContext + '.!S|';
			}
		}

		return result;
	}
	
	function compareSimpleValues(pAc1, pAc2, pErrorCode) {
		var result = '';

		if (pAc1 !== pAc2) {
			//They don't match so use the error code for the result
			result += pErrorCode + '|';
		}

		return result;
	}

}