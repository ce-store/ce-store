/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.handler.sentences = new HandlerSentences();

function HandlerSentences() {
	var ren = null;		//This is set during initialise

	this.QUAL_MODEL = 'model';
	this.QUAL_FACT = 'fact';
	this.QUAL_PATTERN = 'pattern';
	this.QUAL_RULE = 'rule';
	this.QUAL_QUERY = 'query';
	this.QUAL_ANNOTATION = 'annotation';
	this.QUAL_COMMAND = 'command';
	this.QUAL_VALID = 'valid';
	this.QUAL_INVALID = 'invalid';
	
	var QUAL_ALL = 'all';

	var ACTION_SAVE = 'save';
	var ACTION_VALIDATE = 'validate';
	var ACTION_EXECUTE_Q = 'execute_as_query';
	var ACTION_EXECUTE_R = 'execute_as_rule';
	
	var MARKER_MODEL = 'conceptualise';		//TODO: Have the server advise about model changes instead

	var HTML_INVALID_CE = '<font color="red">CE is invalid!</font>';
	
	var DLG_CETEXT = 'ceText';

	var MS_CEWAIT = 1000;		//How many milliseconds to wait before validating the CE

	var timeEvent = null;
	var ceIsValid = true;

	this.initialise = function() {
		gCe.msg.debug('HandlerSentences', 'initialise');

		ren = gEp.renderer.sentences;
	};

	this.addSentences = function(pFormName, pCeText, pLabel) {
		var ceText = dojo.byId(DLG_CETEXT).value;

		gEp.handler.sentences.add(ceText, pLabel, pFormName, null);
	};

	this.addSentencesFromForm = function(pFormName) {
		var ceText = dojo.byId(DLG_CETEXT).value;

		this.addSentences(pFormName, ceText, 'Load sentences from form');
	};

	this.uploadSentencesFromForm = function(pForm) {
		gEp.handler.sentences.upload(pForm);
	};

	this.updateAddCeFieldWith = function(pCeText) {
		var domCeField = dijit.byId(DLG_CETEXT);
		domCeField.set('value', pCeText);

		gEp.ui.pane.addce.activateTab();
	};

	this.insertTextIntoAddCeField = function(pText) {
		var domCeField = dojo.byId(DLG_CETEXT);
		var cursorStart = domCeField.selectionStart;
		var cursorEnd = domCeField.selectionEnd;
		var currentText = domCeField.value;

		var newText = null;

		if (cursorStart >= 0) {
			newText = currentText.substring(0, cursorStart);
			newText += pText;
			newText += currentText.substring(cursorEnd, currentText.length);
		} else {
			newText = currentText + pText;
		}

		this.updateAddCeFieldWith(newText);
	};

	this.add = function(pCeText, pReqName, pSourceId, pCbf, pUserParms) {
		if (ceIsValid) {
			if (!gCe.utils.isNullOrEmpty(pCeText)) {
				var arr = gEp.autoRunRules;
				var rc = gEp.returnCe;
				var cbf = null;
				var localUserParms = {ce_text: pCeText, source_id: pSourceId, form_name: pReqName, auto_run_rule: arr, mode: ACTION_SAVE};

				if (pCbf == null) {
					if (containsModelChanges(pCeText)) {
						//The submitted text contains a model change so refresh the concept list on response
						cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportLoadResultsAndRefresh(pResponseObject, pUserParms); };
					} else {
						cbf = function(pResponseObject,pUserParms) { gEp.handler.sentences.reportLoadResults(pResponseObject, pUserParms); };
					}
				} else {
					cbf = pCbf;
				}

				var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);

				if (!gCe.utils.isNullOrEmpty(pSourceId)) {
					gCe.api.sentences.addToSource(gEp.stdHttpParms(), cbf, pCeText, pSourceId, arr, rc, userParms);
				} else {
					gCe.api.sentences.add(gEp.stdHttpParms(), cbf, pCeText, arr, rc, userParms);
				}
			} else {
				gCe.msg.error('No CE text was specified');
			}
		} else {
			alert('The CE is not valid.  Please correct it before saving');
		}
	};

	this.upload = function(pForm) {
	    var formName = pForm.id;
		var arr = gEp.autoRunRules;
		var rc = gEp.returnCe;
		var localUserParms = {source_id: formName, form_name: formName, auto_run_rule: arr, mode: ACTION_SAVE};
		// TODO unlike this.add above, here we're going to assume model changes
		// are present, ideally the server should indicate whether this is the
		// case and both this function and the one above be altered accordingly.
		var cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportLoadResultsAndRefresh(pResponseObject, pUserParms); };
		gCe.api.sentences.uploadToSource(gEp.stdHttpParms(), cbf, pForm, arr, rc, localUserParms);
	};

	this.autoValidateCe = function(pDlgName, pValidText, pInvalidText, pCbf) {
		if (timeEvent === null) {
			var cbf = function() { gEp.handler.sentences.validateCe(pDlgName, pValidText, pInvalidText, pCbf); };
			timeEvent = window.setInterval(cbf, MS_CEWAIT);	
		}
	};

	this.validateCe = function(pDlgName, pValidText, pInvalidText, pCbf) {
		//First remove the timed event
		if (timeEvent != null) {
			window.clearInterval(timeEvent);
			timeEvent = null;
		}

		if (pDlgName != null) {
			var dlgCeField = dojo.byId(pDlgName);
			
			if (dlgCeField != null) {
				var ceText = dlgCeField.value;
				if (!gCe.utils.isNullOrEmpty(ceText)) {					
					var userParms = { dlgName: pDlgName };
					
					if (pValidText != null) {
						userParms.valid_text = pValidText;
					}
					
					if (pInvalidText != null) {
						userParms.invalid_text = pInvalidText;
					}
					
					if (pCbf != null) {
						userParms.notify = pCbf;
					}

					var cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportAutoValidateResults(pResponseObject, pUserParms); };
					this.validate(ceText, cbf, userParms);
				}
			} else {
				gCe.msg.debug('Unable to auto-validate CE as field name "' + pDlgName + '" could not be found');
			}
		} else {
			gCe.msg.debug('Unable to auto-validate CE as no field name was specified');
		}
	};

	this.validate = function(pCeText, pCbf, pUserParms) {
		if (!gCe.utils.isNullOrEmpty(pCeText)) {
			var localUserParms = {ce_text: pCeText, mode: ACTION_VALIDATE};

			if (pCbf == null) {
				cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportValidateResults(pResponseObject, pUserParms); };
			} else {
				cbf = pCbf;
			}

			var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
			gCe.api.sentences.validate(gEp.stdHttpParms(), cbf, pCeText, userParms);
		} else {
			gCe.msg.error('No CE text was specified');
		}
	};

	this.executeAsQuery = function(pCeText, pCbf, pUserParms) {
		if (!gCe.utils.isNullOrEmpty(pCeText)) {
			var localUserParms = {ce_text: pCeText, mode: ACTION_EXECUTE_Q};

			if (pCbf == null) {
				cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportExecuteResults(pResponseObject, pUserParms); };
			} else {
				cbf = pCbf;
			}

			var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
			gCe.api.sentences.executeAsQuery(gEp.stdHttpParms(), cbf, pCeText, userParms);
		} else {
			gCe.msg.error('No CE text was specified');
		}
	};

	this.executeAsRule = function(pCeText, pCbf, pUserParms) {
		if (!gCe.utils.isNullOrEmpty(pCeText)) {
			var localUserParms = {ce_text: pCeText, mode: ACTION_EXECUTE_R};

			if (pCbf == null) {
				cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.reportExecuteResults(pResponseObject, pUserParms); };
			} else {
				cbf = pCbf;
			}

			var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
			gCe.api.sentences.executeAsRule(gEp.stdHttpParms(), cbf, pCeText, userParms);
		} else {
			gCe.msg.error('No CE text was specified');
		}
	};

	this.getSentenceDetails = function(pSenId, pCbf, pUserParms) {
		var localUserParms = {sentence_id: pSenId};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceDetails(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.sentences.getDetailsFor(gEp.stdHttpParms(), cbf, pSenId, userParms);
	};

	this.listAllSentences = function(pFullDetail, pCbf, pUserParms) {
		var localUserParms = {type_qualifier: QUAL_ALL, full_detail: pFullDetail};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.sentences.listAll(gEp.stdHttpParms(), cbf, userParms);
	};

	this.listAllSentencesQualifiedAs = function(pTypeQual, pValQual, pFullDetail, pCbf, pUserParms) {
		var localUserParms = {type_qualifier: pTypeQual, validity_qualifier: pValQual, full_detail: pFullDetail};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.sentences.listQualifiedWith(gEp.stdHttpParms(), cbf, pTypeQual, pValQual, userParms);
	};

	this.listSentencesForSource = function(pSrcId, pFullDetail, pCbf, pUserParms) {
		var localUserParms = {source_id: pSrcId, full_detail: pFullDetail};

		if (pCbf == null) {
			cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processSentenceList(pResponseObject, pUserParms); };
		} else {
			cbf = pCbf;
		}

		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
		gCe.api.sources.listSentencesFor(gEp.stdHttpParms(), cbf, pSrcId, userParms);
	};

	this.deleteSentence = function(pSenId, pCbf, pUserParms) {
		var answer = confirm('Are you sure you want to delete sentence \'' + pSenId + '\'?');

		if (answer) {
			var localUserParms = {sentence_id: pSenId};

			if (pCbf == null) {
				cbf = function(pResponseObject, pUserParms) { gEp.handler.sentences.processDeletedSentence(pResponseObject, pUserParms); };
			} else {
				cbf = pCbf;
			}

			var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);
			gCe.api.sentences.deleteSentence(gEp.stdHttpParms(), cbf, pSenId, userParms);
		}
	};

	this.processSentenceList = function(pResponseObject, pUserParms) {
		if (pUserParms.full_detail) {
			this.processFullSentenceList(pResponseObject, pUserParms);
		} else {
			this.processSimpleSentenceList(pResponseObject, pUserParms);
		}
	};

	this.processSimpleSentenceList = function(pResponseObject, pUserParms) {
		var senList = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderSimpleSentenceList(senList, pUserParms);
	};

	this.processFullSentenceList = function(pResponseObject, pUserParms) {
		var senList = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderFullSentenceList(senList, pUserParms);
	};

	this.processSentenceDetails = function(pResponseObject, pUserParms) {
		var thisSen = gCe.utils.getStructuredResponseFrom(pResponseObject);

		ren.renderSentenceDetails(thisSen, pUserParms);
	};

	this.processDeletedSentence = function(pResponseObject, pUserParms) {
		var result = gCe.utils.getStructuredResponseFrom(pResponseObject);
		gCe.msg.notYetImplemented('processDeletedSentence', result);
	};

	function containsModelChanges(pCeText) {
		return pCeText.indexOf(MARKER_MODEL) !== -1;
	}

	this.reportLoadResultsAndRefresh = function(pResponse, pUserParms) {
		this.reportLoadResults(pResponse, pUserParms);

		gEp.listAllCoreItems();
	};

	this.reportValidateResults = function(pResponse, pUserParms) {
		var result = gCe.utils.getStructuredResponseFrom(pResponse);

		var genMsg = "Result of CE sentence validation:" + summariseSentenceCounts(result);

		gCe.msg.alert(genMsg);
	};

	this.reportAutoValidateResults = function(pResponse, pUserParms) {
		if (pResponse.alerts != null) {
			if ((pResponse.alerts.errors != null) && (pResponse.alerts.errors.length != 0)) {
				//failure
				ceIsValid = false;
			} else {
				ceIsValid = true;
			}
		} else {
			ceIsValid = true;
		}

		if ((pUserParms != null) && (pUserParms.dlgName != null)) {
			var statusFieldName = pUserParms.dlgName + '_Status';

			var dlgCeStatus = dojo.byId(statusFieldName);

			if (pUserParms.notify != null) {
				pUserParms.notify(ceIsValid);
			}
			
			if (ceIsValid) {
				if (dlgCeStatus != null) {
					var validText = 'CE is valid';
					
					if (!gCe.utils.isNullOrEmpty(pUserParms)) {
						if (pUserParms.valid_text != null) {
							validText = pUserParms.valid_text;
						}
					}
					
					dlgCeStatus.innerHTML = validText;
				}
			} else {
				if (dlgCeStatus != null) {
					var invalidText = HTML_INVALID_CE;
					
					if (!gCe.utils.isNullOrEmpty(pUserParms)) {
						if (pUserParms.invalid_text != null) {
							invalidText = pUserParms.invalid_text;
						}
					}

					dlgCeStatus.innerHTML = invalidText;
				}
			}
		}
	};

	this.reportLoadResults = function(pResponse, pUserParms) {
		var result = gCe.utils.getStructuredResponseFrom(pResponse);
		var genMsg = 'Result of CE sentence loading:\n';

		genMsg += '   ' + result.command_count + ' commands executed' + summariseSentenceCounts(result);

		var errs = gEp.handler.messages.hasErrors(pResponse);
		var warns = gEp.handler.messages.hasWarnings(pResponse);
		var extraText = '';

		if (errs && warns) {
			extraText = '\nErrors and warnings were reported (see error/warning pane below)';
		} else {
			if (errs) {
				extraText = '\nErrors were reported (see error pane below)';
			}
			if (warns) {
				extraText = '\nWarnings were reported (see warning pane below)';
			}
		}

		gCe.msg.alert(genMsg + extraText);
	};

	function summariseSentenceCounts(pResult) {
		var result = '';

		if ((!gCe.utils.isNullOrEmpty(pResult.valid_sentences)) && (pResult.valid_sentences > 0)) {
			result += '\n   ' + pResult.valid_sentences + ' valid sentences';
		}

		if ((!gCe.utils.isNullOrEmpty(pResult.invalid_sentences)) && (pResult.invalid_sentences > 0)) {
			result += '\n   ' + pResult.invalid_sentences + ' invalid sentences';
		}

		return result;
	}

}
