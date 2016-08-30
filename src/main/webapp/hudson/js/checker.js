/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gChecker = new Checker();

function Checker() {

	this.compareExecuteReplies = function(pAr1, pAr2, pMaxDepth) {
		//Execute replies are the same as answer replies
		return this.compareAnswerReplies(pAr1, pAr2, pMaxDepth);
	};

	this.compareAnswerReplies = function(pAr1, pAr2, pMaxDepth) {
		var result = '';

		if ((pAr1 != null) && (pAr2 != null)) {
			//question
			result += compareQuestion(pAr1.question, pAr2.question);

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
			gHudson.reportLog('Difference in execute result detected (cached, live):');
			gHudson.reportLog(JSON.stringify(pAr1));
			gHudson.reportLog(JSON.stringify(pAr2));
		}

		return result;
	};

	this.compareInterpretReplies = function(pAr1, pAr2, pMaxDepth) {
		var result = '';

		if ((pAr1 != null) && (pAr2 != null)) {
			//question_text
			result += compareSimpleValues(pAr1.question_text, pAr2.question_text, 'I.QT');

			//confidence
			result += compareSimpleValues(pAr1.confidence, pAr2.confidence, 'I.QC');

			//confidence_explanation
			result += compareSimpleValues(pAr1.confidence_explanation, pAr2.confidence_explanation, 'I.QE');

			//words
			result += compareArrayValues(pAr1.words, pAr2.words, 'I.W');

			//concepts
			result += compareConcepts(pAr1.concepts, pAr2.concepts, 'C');

			//properties
			result += compareProperties(pAr1.properties, pAr2.properties, 'P');

			//instances
			result += compareInstances(pAr1.instances, pAr2.instances, 'I');

			//specials
			result += compareSpecials(pAr1.specials, pAr2.specials, 'S');
		} else {
			result += '!R|';	//One or both of the replies is null
		}

		if (result != '') {
			gHudson.reportLog('Difference in interpret result detected (cached, live):');
			gHudson.reportLog(JSON.stringify(pAr1));
			gHudson.reportLog(JSON.stringify(pAr2));
		}

		return result;
	};

	this.compareAnalyseReplies = function(pAr1, pAr2, pMaxDepth) {
		//TODO: Complete this
		return 'Not yet implemented';
	};

	function compareConcepts(pConList1, pConList2, pErrCode) {
		var result = '';
		var conList = processEntityLists(pConList1, pConList2);

		if (conList.length > 0) {
			for (var i = 0; i < conList.length; i++) {
				var thisItem = conList[i];
				
				result += compareTheseConcepts(thisItem[1], thisItem[2], i, pErrCode);
			}
		}
		
		return result;
	}
	
	function compareProperties(pPropList1, pPropList2, pErrCode) {
		var result = '';
		var propList = processEntityLists(pPropList1, pPropList2);

		if (propList.length > 0) {
			for (var i = 0; i < propList.length; i++) {
				var thisItem = propList[i];
				
				result += compareTheseProperties(thisItem[1], thisItem[2], i, pErrCode);
			}
		}

		return result;
	}

	function compareInstances(pInstList1, pInstList2, pErrCode) {
		var result = '';
		var instList = processEntityLists(pInstList1, pInstList2);

		if (instList.length > 0) {
			for (var i = 0; i < instList.length; i++) {
				var thisItem = instList[i];

				result += compareTheseInstances(thisItem[1], thisItem[2], i, pErrCode);
			}
		}

		return result;
	}

	function compareSpecials(pSpecList1, pSpecList2, pErrCode) {
		var result = '';
		var specList = processEntityLists(pSpecList1, pSpecList2);

		if (specList.length > 0) {
			for (var i = 0; i < specList.length; i++) {
				var thisItem = specList[i];
				
				result += compareTheseSpecials(thisItem[1], thisItem[2], i, pErrCode);
			}
		}

		return result;
	}
	
	function compareTheseConcepts(pCon1, pCon2, pPos, pErrCode) {
		return compareStandardEntity(pCon1, pCon2, pPos, pErrCode);
	}

	function compareTheseProperties(pProp1, pProp2, pPos, pErrCode) {
		return compareStandardEntity(pProp1, pProp2, pPos, pErrCode);
	}

	function compareTheseInstances(pInst1, pInst2, pPos, pErrCode) {
		return compareStandardEntity(pInst1, pInst2, pPos, pErrCode);
	}
	
	function compareStandardEntity(pEnt1, pEnt2, pPos, pErrCode) {
		var result = '';
		var errText = pErrCode + '.' + pPos;

		if (pEnt1 != null || pEnt2 != null) {
			if (pEnt1 == null) {
				result += errText + ".!1";
			} else if (pEnt2 == null) {
				result += errText + ".!2";
			} else {
				result += compareSimpleValues(pEnt1.name, pEnt2.name, errText + '.N')
				result += compareSimpleValues(pEnt1.position, pEnt2.position, errText + '.P')
				result += compareSimpleValues(pEnt1.type, pEnt2.type, errText + '.T')
				result += compareMmInstance(pEnt1.instance, pEnt2.instance, pPos, errText + '.I-')
			}
		} else {
			//Both are null - nothing needed
		}

		return result;
	}

	function compareMmInstance(pInst1, pInst2, pPos, pResultRoot) {
		var result = '';
		var keyList = [];

		for ( var i in pInst1) {
			if (keyList.indexOf(i) == -1) {
				keyList.push(i);
			}
		}

		for ( var i in pInst2) {
			if (keyList.indexOf(i) == -1) {
				keyList.push(i);
			}
		}

		for (var i in keyList) {
			var thisKey = keyList[i];
			var vals1 = pInst1[thisKey];
			var vals2 = pInst2[thisKey];
			var errCode = pResultRoot + '.' + thisKey;
			
			if (typeof vals1 == 'string') {
				result += compareSimpleValues(vals1, vals2, errCode)
			} else {
				result += compareArrayValues(vals1, vals2, true, errCode);
			}
		}

		return result;
	}

	function compareTheseSpecials(pSpec1, pSpec2, pPos, pErrCode) {
		var result = '';
		var errCode = pErrCode + pPos;
		
		if ((pSpec1 != null) || (pSpec2 !=null)) {
			if (pSpec1 == null) {
				result += errCode + "!1";
			} else if (pSpec2 == null) {
				result += errCode + "!2";
			} else {
				result += compareSimpleValues(pSpec1.type, pSpec2.type, errCode + '.T')
				result += compareSimpleValues(pSpec1.name, pSpec2.name, errCode + '.N')
				result += compareSimpleValues(pSpec1.position, pSpec2.position, errCode + '.P')

				if (result == '') {
					if (pSpec1.type == 'collection') {
						result += compareThisSpecialCollection(pSpec1, pSpec2, errCode);
					} else if (pSpec1.type == 'number') {
						result += compareThisSpecialNumber(pSpec1, pSpec2, errCode);
					} else if (pSpec1.type == 'enumerated-concept') {
						result += compareThisSpecialEnumeratedConcept(pSpec1, pSpec2, errCode);
					} else {
						result += 'Implement special:' + pSpec1.type;
					}
				}
			}
		} else {
			//Both are null - nothing needed
		}

		return result;
	}

	function compareThisSpecialNumber(pSpec1, pSpec2, pErrCode) {
		var result = '';
		
		result += compareSimpleValues(pSpec1.type, pSpec2.type, pErrCode + '.T')
		result += compareSimpleValues(pSpec1.name, pSpec2.name, pErrCode + '.N')
		result += compareSimpleValues(pSpec1.position, pSpec2.position, pErrCode + '.P')
		
		return result;
	}

	function compareThisSpecialEnumeratedConcept(pSpec1, pSpec2, pErrCode) {
		var result = '';
		
		result += compareSimpleValues(pSpec1.type, pSpec2.type, pErrCode + '.T')
		result += compareSimpleValues(pSpec1.name, pSpec2.name, pErrCode + '.Na')
		result += compareSimpleValues(pSpec1.position, pSpec2.position, pErrCode + '.P')
		result += compareSimpleValues(pSpec1.number, pSpec2.number, pErrCode + '.Nu')
		result += compareConcepts(pSpec1.concepts, pSpec2.concepts, pErrCode);
		
		return result;
	}

	function compareThisSpecialCollection(pSpec1, pSpec2, pErrCode) {
		var result = '';
		
		result += compareSimpleValues(pSpec1.connector, pSpec2.connector, pErrCode + '.Conn')
		result += compareSpecialCollectionContents(pSpec1.contents, pSpec2.contents, pErrCode + ".Cont");
		
		return result;
	}

	function compareSpecialCollectionContents(pColl1, pColl2, pErrCode) {
		var result = '';

		pColl1.sort(sortByText);
		pColl2.sort(sortByText);

		if ((pColl1 != null) && (pColl2 != null)) {
			if (pColl1.length === pColl2.length) {
				for (var i = 0; i < pColl1.length; i++) {
					var coll1 = pColl1[i];
					var coll2 = pColl2[i];

					if ((coll1 != null) && (coll2 != null)) {
						//individual item
						result += compareSpecialCollectionContentItem(coll1, coll2, pErrCode + "." + i);
					} else {
						result += pErrCode + '~|'; //One or both of the items is null
					}
				}
			} else {
				result += pErrCode + '#|'; //The number of items don't match
			}
		} else {
			result += pErrCode + '!|'; //One or both item lists is null
		}

		return result;
	}

	function sortByText(a, b) {
		var result = null;

		if (a.text < b.text) {
			result = -1;
		} else if (a.text > b.text) {
			result = 1;
		} else {
			result = 0;
		}

		return result;
	}

	function compareSpecialCollectionContentItem(pItem1, pItem2, pErrCode) {
		var result = '';

		result += compareSimpleValues(pItem1.text, pItem2.text, pErrCode + ".T");
		result += compareInstances(pItem1.items, pItem2.items, pErrCode + ".I");

		return result;
	}

	function processEntityLists(pList1, pList2) {
		var entList = [];

		if ((pList1 != null) && (pList2 != null)) {
			for ( var i in pList1) {
				var thisEnt1 = pList1[i];
				var thisEnt2 = pList2[i];

				entList.push( [i, thisEnt1, thisEnt2 ] );
			}

			for ( var i in pList2) {
				var thisEnt1 = pList1[i];
				var thisEnt2 = pList2[i];
				var foundMatch = false;

				for ( var j in entList) {
					var wordName = entList[j][0];
					
					if (i == wordName) {
						foundMatch = true;
					}
				}
				
				if (!foundMatch) {
					entList.push( [i, thisEnt1, thisEnt2 ] );
				}
			}
		}

		return entList;
	}

	function compareQuestion(pQuest1, pQuest2) {
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
			
			if (pMaxDepth == -1) {
				tgtRows = pRs1.rows;
			} else {
				if (pRs1.rows.length > pMaxDepth) {
					tgtRows = [];
					
					for (var i = 0; i < pMaxDepth; i++) {
						tgtRows.push(pRs1.rows[i]);
					}
				} else {
					tgtRows = pRs1.rows;
				}
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