/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.conv.handler = new DialogConvHandler();

function DialogConvHandler() {
	var DOM_CONVHISTORY = 'convHistory';

	var CON_CARD = 'card';
	this.CON_CARD = CON_CARD;

	var CON_CECARD = 'CE card';
	var CON_NLCARD = 'NL card';
	var CON_ASKCARD = 'ask card';
	var CON_CONFCARD = 'confirm card';
	var CON_EXPCARD = 'expand card';
	var CON_TELLCARD = 'tell card';
	var CON_WHYCARD = 'why card';
	var CON_GISTCARD = 'gist card';
	var CON_GCCARD = 'gist-confirm card';
	var CON_SCORE = 'score';
	var CON_SCOREPOT = 'potential score';
	var CON_SCOREACT = 'actual score';

	var DEFAULT_CESTORE = 'DEFAULT';

	var PROP_ISFROM = 'is from';
	var PROP_ISTO = 'is to';
	var PROP_IRT = 'is in reply to';
	var PROP_TS = 'timestamp';
	var PROP_CONTENT = 'content';
	var PROP_SECCON = 'secondary content';
	var PROP_ISAWARDED = 'is awarded';
	var PROP_SCOREVAL = 'score value';
	var PROP_SCOREEXP = 'score explanation';

	var MODE_SENTBYME = 0;
	var MODE_SENTTOME = 1;
	var MODE_NOTMINE = 2;

	var SRC_CONVERSATION = 'conv_cestoreweb';

	var DEFAULT_PARTNER = 'Moira';

	var MSG_WIDTH = '400px';
	var HTML_CONV_PREAMBLE = '<table border="0" width="100%">';
	var HTML_CONV_POSTAMBLE = '</table>';
	var HTML_MSG = '<tr><td align="%ALIGN%"><a id="%ANCHOR%"/><font style="font-size:10px"><b>%NAME%:</b></font><table><tr><td align="right">%TYPE%%SCORE%</td><td><table border="1" cellpadding="2" cellspacing="2"><tr><td bgcolor="%COLOUR%" style="max-width:' + MSG_WIDTH + '; word-wrap:break-word;"><font color="%FONT_COLOUR%">%MESSAGE%</font></td></tr></table><td>%REPLY%</td></tr></table>%DATE%</td></tr>';
	var HTML_IRT_LINK = '<a href="#%IRT%" title="is in reply to %IRT%"><img src="./icons/link.png" width="10" height="10"></a>';
	var HTML_MSG_LINK = '<a href="javascript:gEp.handler.instances.getInstanceDetails(\'%ID%\');" title="%TYPE% %ID%">%TYPE%</a>';
	var HTML_MSG_SCORE = '<font color="%SCORE_COLOUR%"><BR/><a title="%SCORE_EXP%">*%SCORE_VAL% pts*</a></font>';

	var NL = '\n';

	var autoUpdate = false;
	var autoSend = true;
	var noseyMode = true;
	var selectedCeStore = null;		// The name of the currently selected CE stores

	this.currentMessage = null;
	this.ceStoreList = [];			// List of all active ce store names
	this.totalPotScore = 0;
	this.totalActScore = 0;

	//Flags (linked to checkboxes)
	var messageLists = {};			// All the message details for each of the CE Stores

	this.initialise = function() {
		gCe.msg.debug('DialogConvHandler', 'initialise');
	};

	this.getAutoUpdate = function() {
		return autoUpdate;
	};

	this.setAutoUpdate = function(pVal) {
		autoUpdate = pVal;
	};

	this.getNoseyMode = function() {
		return noseyMode;
	};

	this.setNoseyMode = function(pVal) {
		noseyMode = pVal;
	};

	this.getAutoSend = function() {
		return autoSend;
	};

	this.setAutoSend = function(pVal) {
		autoSend = pVal;
	};

	this.getSelectedCeStore = function() {
		var result = null;

		if (selectedCeStore === null) {
			result = DEFAULT_CESTORE;
		} else {
			result = selectedCeStore;
		}
		
		return result;
	};

	this.setCeStoreList = function(pList) {
		this.ceStoreList = pList;
	};

	this.getLastRequestTime = function(pCeStoreName) {
		return getCeStoreMessageDetails(pCeStoreName).lastRequestTime;
	};

	this.isConversationModelLoaded = function() {
		return gEp.isConceptLoaded(CON_CECARD);
	};

	this.computeCeFrom = function(pText, pCeStoreName, pCurrentUserName) {
		var result = '';
		var typeToken = calculateDefaultTypeToken(pText);
		var idToken = '';
		var irtToken = getDefaultIrtToken(pCeStoreName);
		var fromToken = null;
		var toTokens = [];
		var messageText = '';
		var userSpecifiedType = false;
		var lines = pText.split('\n');
		var locToken = gEp.dlg.conv.domLocationNameContents();
//		var isConfirm = false;

		if (pCurrentUserName != null) {
			fromToken = pCurrentUserName;	//the current user is the default
		} else {
			fromToken = '';
		}

		for (var idx in lines) {
			var thisLine = lines[idx];

//			if (thisLine === 'confirm') {
//				isConfirm = true;
//			}

			if (gCe.utils.startsWith(thisLine, '!')) {
				var payload = removeFirstCharacter(thisLine);
				var words = payload.split(' ');
				var typeWord = '';

				typeWord = words[0].trim();
				if (words.length > 1) {
					idToken = words[1].trim();
				}

				typeToken = validateTypeToken(typeWord, typeToken);
				userSpecifiedType = true;
			} else if (gCe.utils.startsWith(thisLine, '[')) {
				var payload = removeFirstAndLastCharacters(thisLine);
				irtToken = payload.trim();
			} else if (gCe.utils.startsWith(thisLine, '@')) {
				var payload = removeFirstCharacter(thisLine);
				var words = payload.split(',');

				for (idx in words) {
					var thisWord = words[idx];
					toTokens.push(thisWord.trim());
				}
			} else if (gCe.utils.startsWith(thisLine, '*')) {
				var payload = removeFirstCharacter(thisLine);
				fromToken = payload.trim();
			} else {
				if (messageText.length > 0) {
					messageText += NL;
				}
				messageText += thisLine;
			}
		}

		if (idToken == '') {
			idToken = calculateNewId();
		}

		if (!userSpecifiedType) {
			typeToken = calculateTypeTokenFromMessage(messageText, typeToken);
		}

		if ((gEp.dlg.conv.isTextCe()) || (typeToken == CON_TELLCARD)) {
//			if (isConfirm) {
//				var irtMsg = getConvMessageFor(irtToken, pCeStoreName);
//
//				var possMsg = getCeContentFrom(irtMsg);
//				
//				if ((possMsg !== null) && (possMsg.trim() !== '')) {
//					messageText = possMsg;
//				} else {
//					typeToken = CON_TELLCARD;
//				}
//			} else {
				typeToken = CON_TELLCARD;
//			}
		}

		//If no target try to get the target from the message that this is in reply to
		if (toTokens.length == 0) {
			if (irtToken != '') {
				var irtMsg = getConvMessageFor(irtToken, pCeStoreName);
				
				if (irtMsg != null) {
					toTokens.push[gCe.utils.getFirstOrSinglePropertyValueFor(irtMsg, PROP_ISFROM)];
				}
			}
		}

		//If still no target then use the default
		if (toTokens.length == 0) {
			toTokens.push(DEFAULT_PARTNER);
		}

		if ((messageText != null) && (messageText != '')) {
			result = buildCeUsing(typeToken, idToken, irtToken, fromToken, toTokens, messageText, pCeStoreName, locToken);
		} else {
			gCe.msg.alert('Cannot send empty message');
		}

		return result;
	};
	
	this.showNewConversation = function() {
		this.setRequestCounter(this.getSelectedCeStore(), 2);

		var parms1 = {};
		parms1.CeStoreName = this.getSelectedCeStore();
		parms1.RequestedConcept = CON_CARD;
		var cbf = function(pResponseObject, pParms) { gEp.dlg.conv.handler.appendToConversationRendering(pResponseObject, pParms); };
		gEp.handler.instances.listInstancesForConcept(CON_CARD, null, cbf, parms1);

		var parms2 = {};
		parms2.CeStoreName = this.getSelectedCeStore();
		parms2.RequestedConcept = CON_SCORE;
		var cbf2 = function(pResponseObject, pParms) { gEp.dlg.conv.handler.appendToConversationRendering(pResponseObject, pParms); };
		gEp.handler.instances.listInstancesForConcept(CON_SCORE, null, cbf2, parms2);
	};

	this.showExistingConversation = function() {
		this.setRequestCounter(this.getSelectedCeStore(), 2);

		this.restartConversationRendering();

		var parms1 = { CeStoreName: this.getSelectedCeStore(), RequestedConcept: CON_CARD };
		var cbf1 = function(pResponseObject, pParms) { gEp.dlg.conv.handler.appendToConversationRendering(pResponseObject, pParms); };

		gEp.handler.instances.listInstancesForConcept(CON_CARD, null, cbf1, parms1);


		var parms2 = { CeStoreName: this.getSelectedCeStore(), RequestedConcept: CON_SCORE };
		var cbf2 = function(pResponseObject, pParms) { gEp.dlg.conv.handler.appendToConversationRendering(pResponseObject, pParms); };
		gEp.handler.instances.listInstancesForConcept(CON_SCORE, null, cbf2, parms2);
	};

	this.setRequestCounter = function(pCeStoreName, pVal) {
//gCe.msg.debug('setRequestCounter(' + pCeStoreName + '): ' + pVal);
		getCeStoreMessageDetails(pCeStoreName).outstandingRequests = pVal;
//gCe.msg.debug(getCeStoreMessageDetails(pCeStoreName).outstandingRequests);
	};

	this.countReturnedRequest = function(pCeStoreName) {
//gCe.msg.debug('countReturnedRequest(' + pCeStoreName + ')');
		getCeStoreMessageDetails(pCeStoreName).outstandingRequests = (getCeStoreMessageDetails(pCeStoreName).outstandingRequests - 1);
//gCe.msg.debug(getCeStoreMessageDetails(pCeStoreName).outstandingRequests);
	};

	this.areAllRequestsReturned = function(pCeStoreName) {
//gCe.msg.debug('areAllRequestsReturned(' + pCeStoreName + ')');
		var result = getCeStoreMessageDetails(pCeStoreName).outstandingRequests === 0;
//gCe.msg.debug(result);
		return result;
	};

	this.confirmMessage = function(pCardInst) {
//		console.log(pCardInst);
		var respText = '[' + pCardInst._id + ']\nconfirm';
//		console.log(respText);

		this.sendConversationCe(respText, true, true);
	};

	this.sendConversationCe = function(pFieldContents, pNoUiUpdates, pNoAlerts) {
		var ceText = '';

		this.currentMessage = pFieldContents;
		ceText = this.computeCeFrom(this.currentMessage, this.getSelectedCeStore(), gCe.utils.getLoggedInUserId());

		if ((ceText != null) && (ceText != '')) {
			var cbf = null;
			
			if (pNoUiUpdates) {
				if (!pNoAlerts) {
					cbf = function() { gCe.msg.alert('Your message has been sent'); };
				}
			} else {
				cbf = function(pResponseObject) { gEp.dlg.conv.actions.acknowledgeMessageSent(); };
			}
			var src = null;

			if (gCe.utils.getLoggedInUserId() !== null) {
				src = 'conv_' + gCe.utils.getLoggedInUserId();
			}
			
			if (src === null) {
				src = SRC_CONVERSATION;
			}

			var arr = true;		//auto run rules
			var rc = false;		//return ce 
			gCe.api.sentences.addToSource(gEp.stdHttpParms(), cbf, ceText, src, arr, rc, {});
		}
	};

	function setLastRequestTime(pCeStoreName, pVal) {
		return getCeStoreMessageDetails(pCeStoreName).lastRequestTime = pVal;
	}

	function clearLastRequestTime(pCeStoreName) {
		getCeStoreMessageDetails(pCeStoreName).lastRequestTime = -1;
	}

	function getAllConvMessages(pCeStoreName) {
		return getCeStoreMessageDetails(pCeStoreName).convMessages;
	}

	function getConvMessageFor(pMsgId, pCeStoreName) {
		var result = null;

		if (pMsgId != null) {
			var csmd = getCeStoreMessageDetails(pCeStoreName);
			if (csmd != null) {
				if (csmd.convMessages != null) {
					result = csmd.convMessages[pMsgId];
				}
			}
		}

		return result;
	}

	function addConvMessage(pCeStoreName, pMsg) {
//gCe.msg.debug('addConvMessage');
		if (getCeStoreMessageDetails(pCeStoreName).convMessages == null) {
			getCeStoreMessageDetails(pCeStoreName).convMessages = {};
		}

		getCeStoreMessageDetails(pCeStoreName).convMessages[pMsg._id] = pMsg;
	}

	function addScoreInst(pCeStoreName, pScoreInst) {
		if (getCeStoreMessageDetails(pCeStoreName).scoreInstances == null) {
			getCeStoreMessageDetails(pCeStoreName).scoreInstances = {};
		}

		getCeStoreMessageDetails(pCeStoreName).scoreInstances[pScoreInst._id] = pScoreInst;
	}

	function clearConvMessagesAndScores(pCeStoreName) {
//gCe.msg.debug('clearConvMessages');
		getCeStoreMessageDetails(pCeStoreName).convMessages = {};
		getCeStoreMessageDetails(pCeStoreName).scoreInstances = {};
	}

	function getCeContentFrom(pCardInst) {
		var result = '';

		if (pCardInst != null) {
			if (gCe.utils.isDirectOrInheritedConcept(pCardInst, CON_GISTCARD)) {
				//Gist cards have their CE content in the secondary content property
				result = gCe.utils.getFirstOrSinglePropertyValueFor(pCardInst, PROP_SECCON);
			} else {
				//CE cards have their CE content in content property
				result = gCe.utils.getFirstOrSinglePropertyValueFor(pCardInst, PROP_CONTENT);
			}
		}
		
		return result;
	}

	function calculateTypeTokenFromMessage(pMsgText, pExistingToken) {
		var result = pExistingToken;
		var lcMsgText = pMsgText;

		lcMsgText = gCe.utils.replaceAll(lcMsgText, '\\.', '');
		lcMsgText = gCe.utils.replaceAll(lcMsgText, ',', '');
		lcMsgText = gCe.utils.replaceAll(lcMsgText, '!', '');

		//Need to do the ? test inline for some reason instead of calling the gCe.utils.replaceAll function
		lcMsgText = lcMsgText.replace(new RegExp('\\?', 'g'), '');

		lcMsgText = lcMsgText.toLowerCase().trim();

		if (lcMsgText == 'why') {
			result = CON_WHYCARD;
//		} else if ((lcMsgText == 'confirm') || (lcMsgText == 'ok') || (lcMsgText == 'yes')) {
//			result = CON_TELLCARD;
//		} else if ((lcMsgText == 'expand') || (lcMsgText == 'explain')) {
//			result = CON_EXPCARD;
		}

		return result;
	}

	function getDefaultIrtToken(pCeStoreName) {
		var result = '';
		var lmr = getLastMessageReceived(pCeStoreName);
		if (lmr != null) {
			result = lmr._id;
		}

		return result;
	}

	function getLastMessageReceived(pCeStoreName) {
//gCe.msg.debug('getLastMessageReceived');
//gCe.msg.debug(pCeStoreName);
//gCe.msg.debug(getCeStoreMessageDetails(pCeStoreName));
		return getCeStoreMessageDetails(pCeStoreName).lastMessageReceived;
	}

	function setLastMessageReceived(pCeStoreName, pMsg) {
//gCe.msg.debug('setLastMessageReceived');
		getCeStoreMessageDetails(pCeStoreName).lastMessageReceived = pMsg;
	}

	function getCeStoreMessageDetails(pCeStoreName) {
		var cestoreMsgDetails = messageLists[pCeStoreName];
		
		if (cestoreMsgDetails == null) {
			messageLists[pCeStoreName] = {};
			cestoreMsgDetails = messageLists[pCeStoreName];
		}

//gCe.msg.debug('ceStoreMessageDetails=');
//gCe.msg.debug(cestoreMsgDetails);
		return cestoreMsgDetails;
	}

	function clearLastMessageReceived(pCeStoreName) {
		getCeStoreMessageDetails(pCeStoreName).lastMessageReceived = null;
	}

	function removeFirstCharacter(pText) {
		return pText.substring(1, pText.length);
	}

	function removeFirstAndLastCharacters(pText) {
		return pText.substring(1, (pText.length - 1));
	}

	function calculateNewId() {
		return 'msg_{uid}';
//		return 'msg_' + new Date().getTime();
	}

	function validateTypeToken(pToken, pDefault) {
		var result = null;
		
		if ((pToken == 'ask') ||
//				(pToken == 'confirm') ||
//				(pToken == 'expand') ||
				(pToken == 'tell') ||
				(pToken == 'why') ||
				(pToken == 'NL') ||
				(pToken == 'gist') ||
				(pToken == 'gist-confirm')) {
			result = pToken + ' card';
		} else {
			result = pDefault;
			gCe.msg.alert('Invalid type \'' + pToken + '\' specified');
		}
				
		return result;
	}

	function calculateDefaultTypeToken(pText) {
		return CON_NLCARD;
	}

	function calculateDeterminerFor(pTgtConName) {
		var result = 'a';
		
		if (startsWithVowel(pTgtConName)) {
			result = 'an';
		}
		
		return result;
	}

	function startsWithVowel(pText) {
		var lcText = pText.toLowerCase();
		
		return gCe.utils.startsWith(lcText, 'a') ||
				gCe.utils.startsWith(lcText, 'e') ||
				gCe.utils.startsWith(lcText, 'i') ||
				gCe.utils.startsWith(lcText, 'o') ||
				gCe.utils.startsWith(lcText, 'u');
	}

	function buildCeUsing(pTypeToken, pIdToken, pIrtToken, pFromToken, pToTokens, pMessageText, pCeStoreName, pLocToken) {
		var ceText = '';
		var tgtConName = '';
		var tgtDeterminer = '';
		var sepText = ' and' + NL;
		var indent = '  ';
		var irtCon = calculateConceptForCard(pIrtToken, pCeStoreName);

		if (pTypeToken.length > 1) {
			tgtConName = pTypeToken;
		} else {
			tgtConName = CON_NLCARD;
		}

		tgtDeterminer = calculateDeterminerFor(tgtConName);

		ceText += 'there is ' + tgtDeterminer + ' ' + tgtConName + ' named \'' + gCe.utils.encodeForCe(pIdToken) + '\' that';
		ceText += indent + 'has the timestamp \'{now}\' as timestamp';
//		ceText += indent + 'has \'' + new Date().getTime() + '\' as timestamp';

		if (pIrtToken.length > 1) {
			ceText += sepText + indent + 'is in reply to the ' + irtCon + ' \'' + gCe.utils.encodeForCe(pIrtToken) + '\'';
		}

		if (pFromToken.length > 1) {
			ceText += sepText + indent + 'is from the individual \'' + gCe.utils.encodeForCe(pFromToken) + '\'';
		}

		for (var idx in pToTokens) {
			var thisToToken = pToTokens[idx];
			
			ceText += sepText + indent + 'is to the agent \'' + gCe.utils.encodeForCe(thisToToken) + '\'';
		}

		if (pMessageText.length > 1) {
			ceText += sepText + indent + 'has \'' + gCe.utils.encodeForCe(pMessageText) + '\' as content';
		}

		if ((pLocToken != null) && (pLocToken != "")) {
			ceText += sepText + indent + 'has the spatial thing \'' + pLocToken + '\' as location';
		}

		ceText += '.';

		return ceText;
	}

	function calculateConceptForCard(pCardId, pCeStoreName) {
		var result = CON_CARD;
		var ceCard = getConvMessageFor(pCardId, pCeStoreName);
		
		if (ceCard != null) {
			result = gCe.utils.getFirstDirectConceptNameFor(ceCard);
		}
		
		return result;
	}

	this.restartConversationRendering = function() {
//gCe.msg.debug('restartConversationRendering');
		clearConvMessagesAndScores(this.getSelectedCeStore());
		clearLastRequestTime(this.getSelectedCeStore());
		clearLastMessageReceived(this.getSelectedCeStore());
		this.totalPotScore = 0;
		this.totalActScore = 0;
	};

	this.appendToConversationRendering = function(pResponseObject, pParms) {
		this.countReturnedRequest(pParms.CeStoreName);
//gCe.msg.debug('appendToConversationRendering');
//gCe.msg.debug(pResponseObject);
//gCe.msg.debug(pParms);
		if (pParms.RequestedConcept === CON_CARD) {
			storeCardInstances(pResponseObject, true, pParms.CeStoreName);
		} else {
			storeScoreInstances(pResponseObject, true, pParms.CeStoreName);
		}

		if (this.areAllRequestsReturned(pParms.CeStoreName)) {
			this.renderConversation();
		}

		var domTs = document.getElementById('totalScore');
		
		if (domTs != null) {
			domTs.innerHTML = this.totalActScore + '(' + this.totalPotScore + ')';
		}
	};

	function storeCardInstances(pResponseObject, pForceClear, pCeStoreName) {
		var instArray = gCe.utils.getStructuredResponseFrom(pResponseObject);
		instArray.sort(gCe.utils.sortByCreated);

		if (instArray != null) {
			instArray.sort(gCe.utils.sortByPropertyNumber('timestamp'));

			for (var idx in instArray) {
				var ceCard = instArray[idx];

				if (!gCe.utils.isShadowInstance(ceCard)) {
					var thisConvMsg = getConvMessageFor(ceCard._id, pCeStoreName);

					if (thisConvMsg == null) {
						addConvMessage(pCeStoreName, ceCard);
						setLastRequestTime(pCeStoreName, ceCard._created);

						if (!messageIsFromMe(ceCard) && !isNlCard(ceCard)) {
							setLastMessageReceived(pCeStoreName, ceCard);
						}
					}
				}
			}
		}
	}

	function storeScoreInstances(pResponseObject, pForceClear, pCeStoreName) {
		var instArray = gCe.utils.getStructuredResponseFrom(pResponseObject);
		instArray.sort(gCe.utils.sortByCreated);

		if (instArray != null) {
			for (var idx in instArray) {
				var scoreInst = instArray[idx];
				addScoreInst(pCeStoreName, scoreInst);
			}
		}
	}

	function isInUiMode() {
		return  (document.getElementById(DOM_CONVHISTORY) != null);
	}

	function isNlCard(pCeCard) {
		return gCe.utils.isDirectOrInheritedConcept(pCeCard, CON_NLCARD);
	}

	function messageIsFromMe(pCeCard) {
		var myName = gCe.utils.getLoggedInUserId();
		var fromName = gCe.utils.getFirstOrSinglePropertyValueFor(pCeCard, PROP_ISFROM);

		return myName == fromName;
	}

	this.renderConversation = function() {
		var myName = gCe.utils.getLoggedInUserId();
		var lcMyName = null;
		var htmlText = '';
		var lastMessage = null;

		if (myName == null) {
			myName = '';
		}
		
		lcMyName = myName.toLowerCase();
		this.totalActScore = 0;
		this.totalPotScore = 0;
		
		for (var idx in getAllConvMessages(this.getSelectedCeStore())) {
			var ceCard = getConvMessageFor(idx, this.getSelectedCeStore());
			
			var isFrom = gCe.utils.getFirstOrSinglePropertyValueFor(ceCard, PROP_ISFROM);
			var lcIsFrom = null;
			var isToList = gCe.utils.getAllPropertyValuesFor(ceCard, PROP_ISTO);

			if (isFrom != null) {
				lcIsFrom = isFrom.toLowerCase();
			}
			
			if (lcMyName == lcIsFrom) {
				htmlText += this.htmlForMessage(ceCard, isFrom, isToList, MODE_SENTBYME);
			} else {
				if (gCe.utils.arrayContainsValue(isToList, myName, true)) {
					htmlText += this.htmlForMessage(ceCard, isFrom, isToList, MODE_SENTTOME);
				} else {
					if (noseyMode) {
						htmlText += this.htmlForMessage(ceCard, isFrom, isToList, MODE_NOTMINE);
					}
				}
			}
		}

		updateConversationHistoryWith(htmlText);

		var domTs = document.getElementById('totalScore');
		
		if (domTs != null) {
			domTs.innerHTML = this.totalActScore + '(' + this.totalPotScore + ')';
		}
	};

	this.htmlForMessage = function(pCeCard, pFrom, pToList, pMode) {
		var html = HTML_MSG;
		var msgType = this.htmlForMessageType(pCeCard);
		var msgScore = this.htmlForMessageScore(pCeCard);
		var convText = gCe.utils.getFirstOrSinglePropertyValueFor(pCeCard, PROP_CONTENT);
		var msgTime = gCe.utils.getFirstOrSinglePropertyValueFor(pCeCard, PROP_TS);
		var inReplyTo = this.htmlForInReplyTo(gCe.utils.getFirstOrSinglePropertyValueFor(pCeCard, PROP_IRT));
		var senderText = pFrom;
		
		if (pMode == MODE_SENTBYME) {
			html = gCe.utils.replaceAll(html, '%ALIGN%', 'left');
			html = gCe.utils.replaceAll(html, '%COLOUR%', 'lightgreen');
		} else if (pMode == MODE_SENTTOME) {
			html = gCe.utils.replaceAll(html, '%ALIGN%', 'right');
			html = gCe.utils.replaceAll(html, '%COLOUR%', 'lightblue');
		} else {
			html = gCe.utils.replaceAll(html, '%ALIGN%', 'right');
			html = gCe.utils.replaceAll(html, '%COLOUR%', 'lightgrey');
			
			senderText = pFrom + ' (to ' + pToList + ')';
		}
		
		var fontColour = calculateFontColourFor(pCeCard);
		var formattedDate = '<font style="font-size:10px" color="grey"><b>' + newFormattedDateTimeStringFor(msgTime) + '</b></font><br/><br/>';
		var replyLinkWithDate = '<a href="javascript:gEp.dlg.conv.actions.replyTo(\'' + pCeCard._id + '\');" title="Click to reply to this message">' + formattedDate + '</a>';
		
		html = gCe.utils.replaceAll(html, '%FONT_COLOUR%', fontColour);

		html = gCe.utils.replaceAll(html, '%ANCHOR%', pCeCard._id);
		html = gCe.utils.replaceAll(html, '%TYPE%', msgType);
		html = gCe.utils.replaceAll(html, '%SCORE%', msgScore);
		html = gCe.utils.replaceAll(html, '%NAME%', gCe.utils.htmlFormat(senderText));
		html = gCe.utils.replaceAll(html, '%MESSAGE%', gCe.utils.htmlFormat(convText));
		html = gCe.utils.replaceAll(html, '%REPLY%', inReplyTo);
		html = gCe.utils.replaceAll(html, '%DATE%', replyLinkWithDate);

		return html;
	};

	function calculateFontColourFor(pCeCard) {
		var result = '';
		
		if (gCe.utils.isDirectConcept(pCeCard, CON_CONFCARD) || gCe.utils.isDirectConcept(pCeCard, CON_TELLCARD)) {
			result = 'blue';
		} else {
			result = 'black';
		}
		
		return result;
	}

	this.htmlForInReplyTo = function(pIrtText) {
		var result = '';

		if (pIrtText !== null) {
			result = HTML_IRT_LINK;
			result = gCe.utils.replaceAll(result, '%IRT%', pIrtText);
		}

		return result;
	};

	this.htmlForMessageType = function(pCeCard) {
		var result = '';
		
		result = HTML_MSG_LINK;
		result = gCe.utils.replaceAll(result, '%TYPE%', formattedTypeFor(pCeCard));
		result = gCe.utils.replaceAll(result, '%ID%', pCeCard._id);
		
		return result;
	};

	this.htmlForMessageScore = function(pCeCard) {		
		var result = '';
		var scoreInst = this.getScoreInstanceFor(pCeCard);

		if (scoreInst != null) {
			var scoreVal = this.getScoreValFor(scoreInst);
			var scoreExp = this.getScoreExplanationFor(scoreInst);
			var scoreColour = 'red';
			
			if (gCe.utils.isDirectConcept(scoreInst, CON_SCOREPOT)) {
				scoreColour = 'grey';
				scoreExp = 'Potential score: ' + scoreExp;
				this.totalPotScore += parseInt(scoreVal);
			} else if (gCe.utils.isDirectConcept(scoreInst, CON_SCOREACT)) {
				scoreColour = 'green';
				scoreExp = 'Awarded score: ' + scoreExp;
				this.totalActScore += parseInt(scoreVal);
			}

			if (scoreVal !== '') {
				result = HTML_MSG_SCORE;
				result = gCe.utils.replaceAll(result, '%SCORE_COLOUR%', scoreColour);
				result = gCe.utils.replaceAll(result, '%SCORE_VAL%', scoreVal);
				result = gCe.utils.replaceAll(result, '%SCORE_EXP%', scoreExp);
			}
		}

		return result;
	};

//	this.getTotalScore = function() {
//		var result = 0;
//		var myName = gCe.utils.getLoggedInUserId();
//		var allScores = getCeStoreMessageDetails(this.getSelectedCeStore()).scoreInstances;
//
//		for (var idx in allScores) {
//			var thisScore = allScores[idx];
//			
//			if (gCe.utils.isDirectConcept(thisScore, CON_SCOREACT)) {
//				if (noseyMode) {
//					result += parseInt(gCe.utils.getFirstOrSinglePropertyValueFor(thisScore, PROP_SCOREVAL));
//				}
//			}
//		}
//
//		return result;
//	};
	
	this.getMessagesForSelectedCeStore = function() {
		return getCeStoreMessageDetails(this.getSelectedCeStore());
	};

	this.getScoreInstanceFor = function(pCeCard) {
		var tgtScoreId = gCe.utils.getFirstOrSinglePropertyValueFor(pCeCard, PROP_ISAWARDED);
		var allScores = getCeStoreMessageDetails(this.getSelectedCeStore()).scoreInstances;

		for (var idx in allScores) {
			var thisScore = allScores[idx];

			if (thisScore._id === tgtScoreId) {
				return thisScore;
			}
		}

		return null;
	};

	this.getScoreValFor = function(pScoreInst) {
		return gCe.utils.getFirstOrSinglePropertyValueFor(pScoreInst, PROP_SCOREVAL);
	};

	this.getScoreExplanationFor = function(pScoreInst) {
		return gCe.utils.getFirstOrSinglePropertyValueFor(pScoreInst, PROP_SCOREEXP);
	};

	function newFormattedDateTimeStringFor(pTs) {
		var result = '';

		if (pTs == '{now}') {
			result = formattedCurrentDateTime();
		} else {
			var tsVal = parseInt(pTs);
			var dt = new Date(tsVal);

			if (dt != null) {	
				result = formatDate(dt);
			} else {
				result = pTs;
			}
		}

		return result;
	}

	function formattedCurrentDateTime() {
	    var dt = new Date();
	    
	    return formatDate(dt);
	}

	function formatDate(pDt) {
		return padTo2(pDt.getDate()) + "/" + padTo2(pDt.getMonth() + 1) + "/" + pDt.getFullYear() + " " + padTo2(pDt.getHours()) + ":" + padTo2(pDt.getMinutes()) + ":" + padTo2(pDt.getSeconds());
	}

	function formatTime(pDt) {
		return padTo2(pDt.getHours()) + ":" + padTo2(pDt.getMinutes()) + ":" + padTo2(pDt.getSeconds()) + ":" + padTo3(pDt.getMilliseconds());
	}

	function padTo2(pNum) {
	    return (pNum < 10 ? '0' : '') + pNum;
	}

	function padTo3(pNum) {
		var result = "";
		
		if (pNum < 100) {
			result = "0" + padTo2(pNum);
		} else {
			result = pNum;
		}
		
	    return result;
	}

	function padTo4(pNum) {
		var result = "";
		
		if (pNum < 1000) {
			result = "0" + padTo3(pNum);
		} else {
			result = pNum;
		}
		
	    return result;
	}

	function updateConversationHistoryWith(pHtmlText) {
		var domConvHistory = dojo.byId(DOM_CONVHISTORY);

		domConvHistory.innerHTML = HTML_CONV_PREAMBLE + pHtmlText + HTML_CONV_POSTAMBLE;
		domConvHistory.scrollTop = domConvHistory.scrollHeight;
	}

	function formattedTypeFor(pCeCard) {
		var result = null;
		
		if (gCe.utils.isDirectConcept(pCeCard, CON_ASKCARD)) {
			result = 'ask';
		} else if (gCe.utils.isDirectConcept(pCeCard, CON_CONFCARD)) {
			result = 'confirm';
		} else if (gCe.utils.isDirectConcept(pCeCard, CON_EXPCARD)) {
			result = 'expand';
		} else if (gCe.utils.isDirectConcept(pCeCard, CON_TELLCARD)) {
			result = 'tell';
		} else if (gCe.utils.isDirectConcept(pCeCard, CON_WHYCARD)) {
			result = 'why';
		} else if (gCe.utils.isDirectConcept(pCeCard, CON_GISTCARD)) {
			result = 'gist';
		} else if (gCe.utils.isDirectConcept(pCeCard, CON_GCCARD)) {
			result = 'gist-confirm';
		}
		
		//If still null then check for the higher level concepts instead
		if (result === null) {
			if (gCe.utils.isDirectConcept(pCeCard, CON_CECARD)) {
				result = 'CE';
			} else if (gCe.utils.isDirectConcept(pCeCard, CON_NLCARD)) {
				result = 'NL';
			}
		}
		
		//If still null then unknown
		if (result === null) {
			result = '?';
			gCe.msg.debug('Unable to detect card concept name for:');
			gCe.msg.debug(pCeCard);
		}
		
		return result;
	}

}
