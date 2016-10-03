/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

function CeStore(pJsDebug) {
	this.DEFAULT_CESTORE = 'DEFAULT';

	this.FORMAT_JSON = 'application/json';
	this.FORMAT_TEXT = 'text/plain';
	
	this.STYLE_ID = 'ID';
	this.STYLE_VS = 'verb singular';
	this.STYLE_FN = 'functional noun';

	this.autoRunRules = true;
	this.showStats = true;

	this.msg = new CeStoreMessage(pJsDebug);
	this.utils = null;		//instantiated when /core/utils.js is loaded
	this.api = null;		//instantiated when /api/api.js is loaded

	this.initialise = function() {
		this.msg.debug('CeStore', 'initialise');

		this.api = new CeStoreApi(this);
		this.api.concepts = new CeStoreApiConcept(this);
		this.api.instances = new CeStoreApiInstance(this);
		this.api.models = new CeStoreApiModel(this);
		this.api.patterns = new CeStoreApiPattern(this);
		this.api.properties = new CeStoreApiProperty(this);
		this.api.rationale = new CeStoreApiRationale(this);
		this.api.sentences = new CeStoreApiSentence(this);
		this.api.sources = new CeStoreApiSource(this);
		this.api.special = new CeStoreApiSpecial(this);
		this.api.stores = new CeStoreApiStore(this);

		this.utils = new CeStoreUtils(this.msg);

		this.msg.debug('CE initialisation complete', 'initialise', [ this ]);
	};

	this.isDebug = function() {
		return this.msg.jsDebug;
	};

}

function CeStoreUtils(pMsg) {
	var COOKIE_USERNAME = 'logged_in_user';
	var COOKIE_USERID = 'logged_in_userid';
	var MS_IN_A_DAY = 86400000;		// Number of milliseconds in a day: 24 x 60 x 60 x 1000
	var TYPE_ARRAY = '[object Array]';
	var TYPE_STRING = 'string';
	var ATT_DIRCONNAMES = 'direct_concept_names';
	var ATT_INHCONNAMES = 'inherited_concept_names';
	var ATT_SR = 'structured_response';

	this.msg = pMsg;

	this.errorsInResponse = function(pResponse) {
		return !this.isNullOrEmpty(pResponse) && !this.isNullOrEmpty(pResponse.alerts) && !this.isNullOrEmpty(pResponse.alerts.errors);
	};

	this.extractSingleValueFromCeQueryResponse = function(pResponse) {
		var result = null;

		if (!this.isNullOrEmpty(pResponse)) {
			if (!this.isNullOrEmpty(pResponse.structured_response)) {
				if (!this.isNullOrEmpty(pResponse.structured_response.results)) {
					result = pResponse.structured_response.results[0];
				}
			}
		}

		if (result === null) {
			this.msg.warning('Could not locate value', 'extractSingleValueFromCeQueryResponse', [pResponse]);
		}

		return result;
	};

	this.startsWith = function(pSourceText, pTargetText) {
		//Return true if pSourceText starts with pTargetText
		var result = false;

		if (pSourceText !== null) {
			result = pSourceText.indexOf(pTargetText) === 0;
		}

		return result;
	};

	this.htmlFormat = function(pText) {
		var result = pText;

		if (!this.isNullOrEmpty(result)) {
			result = local_escape(result);
			result = this.replaceAll(result, '\n', '<BR/>');
			result = this.replaceAll(result, ' ', '&nbsp;');
			result = this.replaceAll(result, '	', '&nbsp;&nbsp;&nbsp;');
		}

		return result;
	};

	function local_escape(pVal) {
		return pVal.replace(new RegExp('<', 'g'), '&lt;').replace(new RegExp('>', 'g'), '&gt;');
	}

	this.replaceAll = function(pOrig, pSource, pTarget) {
		//Replace all occurrences of pSource with pTarget in pOrig, returning the updated result
		var result = pOrig;

		if (pOrig !== null) {
			result = pOrig.replace(new RegExp(pSource, 'g'), pTarget);
		}

		return result;
	};

	this.replaceAllCaseInsensitive = function(pOrig, pSource, pTarget) {
		//Replace all occurrences of pSource with pTarget in pOrig, returning the updated result
		var result = pOrig;

		if (pOrig !== null) {
			result = pOrig.replace(new RegExp(pSource, 'ig'), pTarget);
		}

		return result;
	};

	this.isArray = function(pVal) {
		return Object.prototype.toString.call(pVal) === '[object Array]';
	};

	this.isObject = function(pVal) {
		var typeName = Object.prototype.toString.call(pVal);

		return typeName === '[object Object]';
	};

	this.isString = function(pVal) {
		return (typeof pVal  === 'string');
	};

	this.isNullOrEmpty = function(pVal) {
		var result = null;

		if (this.isArray(pVal)) {
			result = (pVal.length === 0);
		} else {
			result = (pVal === null) || (pVal === '') || (pVal === undefined);
		}

		return result;
	};

	this.arrayContainsValue = function(pArray, pValue, pCaseInsensitive) {
		var result = false;

		//Only do the test if the array is not empty and the value is not null
		if ((!this.isNullOrEmpty(pArray)) && (pValue !== null)) {
			if (pCaseInsensitive === true) {
				var lcValue = pValue.toLowerCase();
				
				for (var i = 0; i < pArray.length; i++) {
					var lcItem = pArray[i].toLowerCase();
					
					if (lcValue === lcItem) {
						result = true;
						break;
					}
				}
			} else {
				//A simple case sensitive test
				result = (pArray.indexOf(pValue) != -1);
			}
		}

		return result;
	};

	this.isArray = function(pVal) {
		var typeName = Object.prototype.toString.call(pVal);

		return typeName === TYPE_ARRAY;
	};

	this.isString = function(pVal) {
		return (typeof pVal === TYPE_STRING);
	};

	this.encodeForCe = function(pValue) {
		var result = null;

		if (pValue !== null) {
			result = pValue;
			result = result.replace(/\\/g, '\\\\');
			result = this.replaceAll(result, '\'', '\\\'');
			result = this.replaceAll(result, '‘', '\‘');
			result = this.replaceAll(result, '’', '\’');
			result = this.replaceAll(result, '“', '\“');
			result = this.replaceAll(result, '”', '\”');
		} else {
			result = '';
		}

		return result;
	};

	this.encodeForJs = function(pValue) {
		//This is currently the same as encodeForCe
		var result = null;

		if (pValue !== null) {
			result = pValue;
			result = result.replace(/\\/g, '\\\\');
			result = this.replaceAll(result, '\'', '\\\'');
		} else {
			result = '';
		}

		return result;
	};

	this.encodeForHtml = function(pValue) {
		var result = null;

		if (pValue !== null) {
			result = pValue;
			result = this.replaceAll(result, '\"', '&quot;');
		} else {
			result = '';
		}

		return result;
	};

	this.formatForHtml = function(pValue) {
		var result = null;

		if (pValue !== null) {
			result = pValue;
			result = this.replaceAll(result, ' ', '&nbsp;');
			result = this.replaceAll(result, '\n', '<BR/>');
			result = this.replaceAll(result, '	', '&nbsp;&nbsp;');
		} else {
			result = '';
		}

		return result;
	};

	this.sortByCreated = function(pA, pB) {
		var aVal = null;
		var bVal = null;

		if (pA !== null) {
			aVal = pA._created;
		}

		if (pB !== null) {
			bVal = pB._created;
		}

		return aVal - bVal;
	};

	this.sortById = function(pA, pB) {
		var aVal = null;
		var bVal = null;

		if (pA !== null) {
			aVal = pA._id;
		}

		if (pB !== null) {
			bVal = pB._id;
		}

		return aVal - bVal;
	};

	this.sortByPropertyText = function(pPropName) {
		return function(a, b) {
			var aVal = gCe.utils.getFirstOrSinglePropertyValueFor(a, pPropName);
			var bVal = gCe.utils.getFirstOrSinglePropertyValueFor(a, pPropName);
			
			return aVal - bVal;
		};
	};

	this.sortByPropertyNumber = function(pPropName) {
		return function(a, b) {
			var aVal = gCe.utils.getFirstOrSinglePropertyValueFor(a, pPropName);
			var bVal = gCe.utils.getFirstOrSinglePropertyValueFor(b, pPropName);
			
			return parseInt(aVal) - parseInt(bVal);
		};
	};

	this.getFirstOrSinglePropertyValueFor = function(pInst, pPropName) {
		var propValOrVals = null;
		var result = null;

		if ((pInst != null) && (pInst.property_values != null)) {
			propValOrVals = pInst.property_values[pPropName];

			if (propValOrVals != null) {
				if (this.isArray(propValOrVals)) {
					if (propValOrVals.length > 0) {
						result = propValOrVals[0];
					}
				} else if (this.isString(propValOrVals)){
					result = propValOrVals;
				} else {
					this.msg.warning('Unexpected property value type for: ' + pPropValOrVals, 'getFirstOrSinglePropertyValueFor');
				}
			}
		}

		return result;
	};

	this.getAllPropertyValuesFor = function(pInst, pPropName) {
		var propValOrVals = null;
		var result = null;

		if ((pInst != null) && (pInst.property_values != null)) {
			propValOrVals = pInst.property_values[pPropName];

			if (propValOrVals != null) {
				if (this.isArray(propValOrVals)) {
					result = propValOrVals;
				} else if (this.isString(propValOrVals)){
					result = [ propValOrVals ];
				} else {
					this.msg.warning('Unexpected property value type for: ' + pPropValOrVals, 'getFirstOrSinglePropertyValueFor');
				}
			}
		}

		return result;
	};

	this.isTextSelectedInAddCeBox = function() {
		var domCeField = dojo.byId('ceText');
		var cursorStart = domCeField.selectionStart;
		var cursorEnd = domCeField.selectionEnd;

		return cursorEnd != cursorStart;
	};

	this.hasStats = function(pResponseObject) {
		return (pResponseObject !== null) && (pResponseObject[ATT_SR] !== undefined);
	};

	this.getStructuredResponseFrom = function(pResponseObject) {
		var result = null;

		if (pResponseObject != null) {
			if (pResponseObject[ATT_SR] != null) {
				result = pResponseObject[ATT_SR];
			} else {
				result = pResponseObject;
			}
		}

		return result;
	};

	this.isDirectConcept = function(pCeInstance, pConName) {
		return this.arrayContainsValue(pCeInstance[ATT_DIRCONNAMES], pConName);
	};

	this.isInheritedConcept = function(pCeInstance, pConName) {
		return this.arrayContainsValue(pCeInstance[ATT_INHCONNAMES], pConName);
	};

	this.isDirectOrInheritedConcept = function(pCeInstance, pConName) {
		return this.isDirectConcept(pCeInstance, pConName) || this.isInheritedConcept(pCeInstance, pConName);
	};

	this.isShadowInstance = function(pInst) {
		return (pInst._shadow == true);
	};

	this.getFirstDirectConceptNameFor = function(pInst) {
		return pInst.direct_concept_names[0];
	};

	this.mergeArrays = function(pArr1, pArr2) {
		var result = null;

		if (this.isArray(pArr1)) {
			if (this.isArray(pArr2)) {
				result = pArr1.concat(pArr2);
			} else {
				result = pArr1;
			}
		} else {
			if (this.isArray(pArr2)) {
				result = pArr2;
			} else {
				result = [];
			}
		}

		return result;
	};

	this.mergeObjects = function(pObj1, pObj2) {
		var result = null;

		if (this.isNullOrEmpty(pObj1)) {
			if (this.isNullOrEmpty(pObj2)) {
				result = {};
			} else {
				result = pObj2;
			}
		} else {
			if (this.isNullOrEmpty(pObj2)) {
				result = pObj1;
			} else {
				if (this.isObject(pObj1)) {
					if (this.isObject(pObj2)) {
						result = {};

						for (var key in pObj1) {
							result[key] = pObj1[key];
						}
						for (var key in pObj2) {
							result[key] = pObj2[key];
						}
					} else {
						this.msg.warning('Second argument not an object', 'mergeObjects', [ pObj2 ]);
						result = pObj2;
					}
				} else {
					this.msg.warning('First argument not an object', 'mergeObjects', [ pObj1 ]);
					result = pObj2;
				}
			}
		}

		return result;
	};

	this.getLoggedInUserId = function() {
		return getCookieValueFor(COOKIE_USERID);
	};

	this.getLoggedInUserName = function() {
		return getCookieValueFor(COOKIE_USERNAME);
	};

	this.isLoggedIn = function() {
		return (this.getLoggedInUserName() != null);
	};

	this.setLoggedInUser = function(pUserId, pUserName) {
		setCookie(COOKIE_USERID, pUserId, 365);
		setCookie(COOKIE_USERNAME, pUserName, 365);
	};

	this.clearLoggedInUser = function() {
		setCookie(COOKIE_USERID, '', -1);
		setCookie(COOKIE_USERNAME, '', -1);
	};

	function getCookieValueFor(pKeyName) {
		var kpe = pKeyName + '=';	//Key plus equals sign
		var nvps = document.cookie.split(';');	//Split cookie text into name-value pairs

		for (var key in nvps) {
			var thisNvp = nvps[key];
			thisNvp = thisNvp.trim();
			var possVal = thisNvp.replace(kpe, '');

			if (possVal !== thisNvp) {
				return decodeURIComponent(possVal);
			}
		}

		return null;
	}

	function setCookie(pName, pValue, pDays) {
		var expiresText = null;

		if (pDays != null) {
			var nowDate = new Date();
			nowDate.setTime(nowDate.getTime() + (pDays * MS_IN_A_DAY));
			expiresText = '; expires=' + nowDate.toGMTString();
		} else {
			expiresText = "";
		}

		document.cookie = pName + '=' + pValue + expiresText + '; path=/';
	}

	this.isAttribute = function(pProp) {
		return (pProp.range_name == 'value');
	};

	this.isSequence = function(pProp) {
		return (pProp.range_name == 'sequence');
	};

	this.isRelationship = function(pProp) {
		return !this.isAttribute(pProp) && !this.isSequence(pProp);
	};

	this.isSelfReferentialRelationship = function(pRel) {
		return pRel.domain.name == pRel.range.name;
	};

}

function CeStoreMessage(pJsDebug) {

	this.jsDebug = pJsDebug;  // This flag determines whether debug messages are reported or not

	this.alert = function(pMsg) {
		alert(pMsg);
	};

	this.error = function(pMsg) {
		alert(pMsg);
	};

	this.warning = function(pMsg, pMethod, pExtras) {
		var msgText = '';

		if (pMethod != null) {
			msgText += pMethod + ': ';
		}

		if (typeof pMsg === 'string') {
			msgText += pMsg;
			console.warn(msgText);
		} else {
			console.warn(msgText);
			console.warn(pMsg);
		}

		if (pExtras != null) {
			for (var key in pExtras) {
				console.debug(pExtras[key]);
			}
		}
	};

	this.debug = function(pMsg, pMethod, pExtras) {
		if (this.jsDebug) {
			var msgText = '';

			if (pMethod != null) {
				msgText += pMethod + ': ';
			}

			if (typeof pMsg === 'string') {
				msgText += pMsg;
				console.debug(msgText);
			} else {
				console.debug(msgText);
				console.debug(pMsg);
			}

			if (pExtras != null) {
				for (var key in pExtras) {
					console.debug(pExtras[key]);
				}
			}
		}
	};

	this.notYetImplemented = function(pMethod, pExtras) {
		this.warning('Not yet implemented', pMethod, pExtras);
	};

}

function CeStoreApi(pCe) {
	var AJAX_GET = 'GET';
	var AJAX_POST = 'POST';
	var AJAX_PUT = 'PUT';
	var AJAX_DELETE = 'DELETE';
	var HDR_ACCEPT = 'Accept';
	var HDR_CEUSER = 'CE_User';
	var HDR_CONTYPE = 'Content-type';
	var VAL_CONTYPE = 'text/plain; charset=utf-8';
	var PARM_SHOWSTATS = 'showStats';
	var PARM_STYLE = 'style';
	var REST_STORES = 'stores';

	var cbfAjaxSuccess = null;
	var cbfAjaxFailure = null;
	var ce = pCe;

	this.setAjaxCallbacks = function(pCbfSuccess, pCbfFailure) {
		cbfAjaxSuccess = pCbfSuccess;
		cbfAjaxFailure = pCbfFailure;
	};

	this.defaultHttpParms = function(pUserParms) {
		var httpParms = {};

		if (pUserParms.showStats) {
			httpParms[PARM_SHOWSTATS] = pUserParms.showStats;
		}

		if (pUserParms.style != null) {
			httpParms[PARM_STYLE] = pUserParms.style;
		}

		return httpParms;
	};

	this.constructUrlFrom = function(pDomain, pCeStoreName, pRestPart, pParms) {
		var result = null;

		if (pDomain !== null) {
			result = pDomain;
		} else {
			result = '';
		}

		//If a CE Store name is specified the append the correct prefix
		if (ce.utils.isNullOrEmpty(pCeStoreName)) {
			result += '';
		} else {
			result += REST_STORES + '/' + pCeStoreName + '/';
		}

		//Next append the REST part of the URL
		result += pRestPart;

		//Finally append any HTTP parameters (post parameters are handled separately)
		if (pParms != null) {
			var firstTime = true;

			for (var parmName in pParms) {
				var parmVal = pParms[parmName];
				result += constructParameter(parmName, parmVal, firstTime);
				firstTime = false;
			}
		}

		return result;
	};

	this.mergeUserParms = function(pParms1, pParms2) {
		return ce.utils.mergeObjects(pParms1, pParms2);
	};

	function constructParameter(pParmName, pParmVal, pFirstTime) {
		var encVal = encodeURIComponent(pParmVal);
		var connector = null;

		if (pFirstTime) {
			connector = '?';
		} else {
			connector = '&';
		}

		return connector + pParmName + '=' + encVal;
	}

	this.sendAjaxGet = function(pUrl, pStdVals, pCallbackFunction, pUserParms) {
		sendAjaxRequest(AJAX_GET, pUrl, pStdVals, pCallbackFunction, null, pUserParms);
	};

	this.sendAjaxDelete = function(pUrl, pStdVals, pCallbackFunction, pUserParms) {
		sendAjaxRequest(AJAX_DELETE, pUrl, pStdVals, pCallbackFunction, null, pUserParms);
	};

	this.sendAjaxPost = function(pUrl, pStdVals, pCallbackFunction, pPostParms, pUserParms) {
		sendAjaxRequest(AJAX_POST, pUrl, pStdVals, pCallbackFunction, pPostParms, pUserParms);
	};

	this.sendAjaxPut = function(pUrl, pStdVals, pCallbackFunction, pUserParms) {
		sendAjaxRequest(AJAX_PUT, pUrl, pStdVals, pCallbackFunction, null, pUserParms);
	};

	// common code invoked by sendAjaxRequest and sendAjaxFormPost.
	function setUpAjaxRequest(pType, pUrl, pStdVals, pCallbackFunction, pUserParms) {
		var xhr = new XMLHttpRequest();

		if ((pUserParms === undefined) || (pUserParms === null)) {
			console.warn('pUserParms is missing in setUpAjaxRequest (' + pType + '), for url:' + pUrl);
			pUserParms = {};
		}

		pUserParms.accept = pStdVals.accept;

		xhr.open(pType, pUrl, true);
		xhr.withCredentials = true;
		xhr.setRequestHeader(HDR_ACCEPT, pStdVals.accept);
		xhr.setRequestHeader(HDR_CEUSER, ce.utils.getLoggedInUserId());
		// HDR_CONTYPE must be set-up by caller if desired.

		xhr.onload = function(e) {
			if (this.status === 200) {
				var processedResponse = null;

				try {
					if (pUserParms.accept === gCe.FORMAT_JSON) {
						processedResponse = JSON.parse(xhr.response);
					} else {
						processedResponse = xhr.response;
					}
				} catch (e) {
					ajaxErrorOther(xhr.response, e, pUrl, pUserParms);
				}

				if (processedResponse != null) {
					ajaxSuccess(processedResponse, pCallbackFunction, pUserParms);
				}
			} else if (this.status === 404) {
				ajaxError404(pUrl, pUserParms);
			} else if (this.status === 405) {
				ajaxError405(pUrl, pUserParms);
			} else if (this.status === 500) {
				ajaxError500(pUrl, pUserParms);
			} else {
				ajaxErrorOther(xhr.response, e, this.status, pUrl, pUserParms);
			}
		};

		return xhr;
	}
	
	function sendAjaxRequest(pType, pUrl, pStdVals, pCallbackFunction, pPostParms, pUserParms) {
		var xhr = setUpAjaxRequest(pType, pUrl, pStdVals, pCallbackFunction, pUserParms);
		xhr.setRequestHeader(HDR_CONTYPE, VAL_CONTYPE);

		if (pPostParms != null) {
			var encText = '';

			//Encode the post parameters as encoded name/value pairs
			if (gCe.utils.isString(pPostParms)) {
				encText = pPostParms;
			} else {
				for (var parmName in pPostParms) {
					var parmValue = pPostParms[parmName];
					encText += encodeURIComponent(parmName) + '=';
					encText += encodeURIComponent(parmValue);
				}
			}

			xhr.send(encText);
		} else {
			xhr.send();
		}
	}

	this.sendAjaxFormPost = function(pUrl, pStdVals, pCallbackFunction, pForm, pUserParms) {
		var xhr = setUpAjaxRequest(AJAX_POST, pUrl, pStdVals, pCallbackFunction, pUserParms);
		// HDR_CONTYPE will be automatically set to multipart/form-data
		var formData = new FormData(pForm);
		xhr.send(formData);
	};

	function ajaxSuccess(pResponseObject, pCallbackFunction, pUserParms) {
		if (pResponseObject != null) {
			cbfAjaxSuccess(pResponseObject, pUserParms);

			if (pCallbackFunction != null) {
				if (typeof(pCallbackFunction) === 'function') {
					pCallbackFunction(pResponseObject, pUserParms);
				} else {
					console.error('Callback function cannot be run as it is not a function:');
					console.error(pCallbackFunction);
				}
			}
		} else {
			ce.msg.error('Unexpected null response for transaction');
		}
	}

	function ajaxError404(pUrl, pUserParms) {
		var errorList = [];
		errorList.push('Server error "not found" (404) for url ' + pUrl);

		cbfAjaxFailure(errorList, pUserParms);
	}

	function ajaxError405(pUrl, pUserParms) {
		var errorList = [];
		errorList.push('Server error "not allowed" (405) for url ' + pUrl);

		cbfAjaxFailure(errorList, pUserParms);
	}

	function ajaxError500(pUrl, pUserParms) {
		var errorList = [];
		errorList.push('Server error (500) for url ' + pUrl);

		cbfAjaxFailure(errorList, pUserParms);
	}

	function ajaxErrorOther(pResponseText, pError, pCode, pUrl, pUserParms) {
		// This is called when an unexpected error occurs, such as a bad HTTP response from the server
		ce.msg.warning('An error occurred (ajaxError)', 'ajaxErrorOther', [ pUrl, pError]);
		ce.msg.debug(pResponseText, 'ajaxErrorOther');

		var errorList = [];
		var errText = 'Error: "' + pError + '" for targetUrl: "' + pUrl + '"';
		errorList.push('Unhandled server error (' + pCode + ') for url ' + pUrl);
		errorList.push(errText);

		if (!ce.utils.isNullOrEmpty(pResponseText)) {
			errorList.push('Stacktrace:' + pResponseText);
		}

		cbfAjaxFailure(errorList, pUserParms);
	}
}

function CeStoreApiConcept(pCe) {
	var ce = pCe;

	var REST_CONCEPTS = 'concepts';
	var REST_SENTENCES = 'sentences';
	var REST_INSTANCES = 'instances';
	var REST_PROPERTIES = 'properties';
	var REST_DATATYPE = 'datatype';
	var REST_OBJECT = 'object';
	var REST_PRIMARY = 'primary';
	var REST_SECONDARY = 'secondary';
	var REST_EXACT = 'exact';
	var REST_COUNT = 'count';
	var PARM_RANGE = 'range';

	this.initialise = function() {
		ce.msg.debug('CeStoreApiConcept', 'initialise');
		//Nothing needed
	};

	this.listAll = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_CONCEPTS + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.getDetails = function(pStdVals, pCallbackFunction, pConName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pConName);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listProperties = function(pStdVals, pCallbackFunction, pConceptName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pConceptName) + '/' + REST_PROPERTIES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listDatatypeProperties = function(pStdVals, pCallbackFunction, pConceptName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pConceptName) + '/' + REST_PROPERTIES + '/' + REST_DATATYPE;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listObjectProperties = function(pStdVals, pCallbackFunction, pConceptName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pConceptName) + '/' + REST_PROPERTIES + '/' + REST_OBJECT;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listObjectPropertiesWithRange = function(pStdVals, pCallbackFunction, pDomainName, pRangeName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		httpParms[PARM_RANGE] = pRangeName;

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pDomainName) + '/' + REST_PROPERTIES + '/' + REST_OBJECT;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listInstancesFor = function(pStdVals, pCallbackFunction, pConceptName, pSinceTs, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (!ce.utils.isNullOrEmpty(pSinceTs)) {
			httpParms[ce.api.instances.PARM_SINCE] = pSinceTs;
		}

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pConceptName) + '/' + REST_INSTANCES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listExactInstancesFor = function(pStdVals, pCallbackFunction, pConceptName, pSinceTs, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (!ce.utils.isNullOrEmpty(pSinceTs)) {
			httpParms[ce.api.instances.PARM_SINCE] = pSinceTs;
		}

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pConceptName) + '/' + REST_INSTANCES + '/' + REST_EXACT;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.countInstancesFor = function(pStdVals, pCallbackFunction, pConceptName, pSinceTs, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (!ce.utils.isNullOrEmpty(pSinceTs)) {
			httpParms[ce.api.instances.PARM_SINCE] = pSinceTs;
		}

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pConceptName) + '/' + REST_INSTANCES + '/' + REST_COUNT;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listPrimarySentencesFor = function(pStdVals, pCallbackFunction, pConName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		//TODO: Correct this - primary/secondary has switched from REST to filter
		var restPart =  REST_CONCEPTS + '/' + encodeURIComponent(pConName) + '/' + REST_SENTENCES + '/' + REST_PRIMARY;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listSecondarySentencesFor = function(pStdVals, pCallbackFunction, pConName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		//TODO: Correct this - primary/secondary has switched from REST to filter
		var restPart =  REST_CONCEPTS + '/' + encodeURIComponent(pConName) + '/' + REST_SENTENCES + '/' + REST_SECONDARY;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listAllSentencesFor = function(pStdVals, pCallbackFunction, pConName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart =  REST_CONCEPTS + '/' + encodeURIComponent(pConName) + '/' + REST_SENTENCES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

}

function CeStoreApiInstance(pCe) {
	var REST_INSTANCES = 'instances';
	var REST_REFERENCES = 'references';
	var REST_SENTENCES = 'sentences';
	var REST_PRIMARY = 'primary';
	var REST_SECONDARY = 'secondary';
	var PARM_SINCE = 'since';
	var PARM_STEPS = 'steps';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiInstance', 'initialise');
		//Nothing needed
	};

	this.listAll = function(pStdVals, pCallbackFunction, pSinceTs, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (!ce.utils.isNullOrEmpty(pSinceTs)) {
			httpParms[PARM_SINCE] = pSinceTs;
		}

		var restPart =  REST_INSTANCES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listPrimarySentencesFor = function(pStdVals, pCallbackFunction, pInstName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		//TODO: Correct this - primary/secondary has switched from REST to filter
		var restPart =  REST_INSTANCES + '/' + encodeURIComponent(pInstName) + '/' + REST_SENTENCES + '/' + REST_PRIMARY;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listSecondarySentencesFor = function(pStdVals, pCallbackFunction, pInstName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		//TODO: Correct this - primary/secondary has switched from REST to filter
		var restPart =  REST_INSTANCES + '/' + encodeURIComponent(pInstName) + '/' + REST_SENTENCES + '/' + REST_SECONDARY;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listAllSentencesFor = function(pStdVals, pCallbackFunction, pInstName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart =  REST_INSTANCES + '/' + encodeURIComponent(pInstName) + '/' + REST_SENTENCES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.getDetailsFor = function(pStdVals, pCallbackFunction, pInstName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if ((pUserParms != null) && (pUserParms[PARM_STEPS] != null)) {
			httpParms[PARM_STEPS] = pUserParms[PARM_STEPS];
		}

		var restPart = REST_INSTANCES + '/' + encodeURIComponent(pInstName);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listReferencesTo = function(pStdVals, pCallbackFunction, pInstName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if ((pUserParms != null) && (pUserParms[PARM_STEPS] != null)) {
			httpParms[PARM_STEPS] = pUserParms[PARM_STEPS];
		}

		var restPart = REST_INSTANCES + '/' + encodeURIComponent(pInstName) + '/' + REST_REFERENCES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

}

function CeStoreApiModel(pCe) {
	var REST_MODELS = 'models';
	var REST_SENTENCES = 'sentences';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiModel', 'initialise');
		//Nothing needed
	};

	this.listAll = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_MODELS + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.getDetailsFor = function(pStdVals, pCallbackFunction, pModelName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_MODELS + '/' + encodeURIComponent(pModelName);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listSentencesFor = function(pStdVals, pCallbackFunction, pModelName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var restPart = REST_MODELS + '/' + encodeURIComponent(pModelName) + '/' + REST_SENTENCES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

}

function CeStoreApiPattern(pCe) {
	var REST_SPECIAL = 'special';
	var REST_PATTERNS = 'patterns';
	var REST_QUERIES = 'queries';
	var REST_RULES = 'rules';
	var REST_EXECUTE = 'execute';
	var PARM_SUPPCE = 'suppressCe';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiPattern', 'initialise');
		//Nothing needed
	};

	this.listPatterns = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SPECIAL + '/' + REST_PATTERNS + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listQueries = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart =  REST_QUERIES + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.getQueryDetails = function(pStdVals, pCallbackFunction, pQueryName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_QUERIES + '/' + encodeURIComponent(pQueryName);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.executeQuery = function(pStdVals, pCallbackFunction, pQueryName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (pUserParms.suppressCe != null) {
			httpParms[PARM_SUPPCE] = pUserParms.suppressCe;
		}

		var restPart = REST_QUERIES + '/' + encodeURIComponent(pQueryName) + '/' + REST_EXECUTE;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listRules = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_RULES + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.getRuleDetails = function(pStdVals, pCallbackFunction, pRuleName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_RULES + '/' + encodeURIComponent(pRuleName);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.executeRule = function(pStdVals, pCallbackFunction, pRuleName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_RULES + '/' + encodeURIComponent(pRuleName) + '/' + REST_EXECUTE;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, null, pUserParms);
	};

	this.executeRuleAsQuery = function(pStdVals, pCallbackFunction, pRuleName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (pUserParms.suppressCe != null) {
			httpParms[PARM_SUPPCE] = pUserParms.suppressCe;
		}

		var restPart = REST_RULES + '/' + encodeURIComponent(pRuleName) + '/' + REST_EXECUTE;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

}

function CeStoreApiProperty(pCe) {
	var REST_PROPERTIES = 'properties';
	var REST_DATATYPE = 'datatype';
	var REST_OBJECT = 'object';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiProperty', 'initialise');
		//Nothing needed
	};

	this.listAll = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_PROPERTIES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listDatatype = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_PROPERTIES + '/' + REST_DATATYPE;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listObject = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_PROPERTIES + '/' + REST_OBJECT;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.getDetailsFor = function(pStdVals, pCallbackFunction, pPropName, pPropDomain, pPropRange, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_PROPERTIES + '/' + encodeURIComponent(pPropDomain + ':' + pPropName + ':' + pPropRange);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

}

function CeStoreApiRationale(pCe) {
	var REST_SPECIAL = 'special';
	var REST_RATIONALE = 'rationale';
	var REST_CONCEPTS = 'concepts';
	var REST_INSTANCES = 'instances';
	var REST_RULES = 'rules';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiRationale', 'initialise');
		//Nothing needed
	};

	this.listAll = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SPECIAL + '/' + REST_RATIONALE + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listForConcept = function(pStdVals, pCallbackFunction, pConName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_CONCEPTS + '/' + encodeURIComponent(pConName) + '/' + REST_RATIONALE + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listForInstance = function(pStdVals, pCallbackFunction, pInstName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_INSTANCES + '/' + encodeURIComponent(pInstName) + '/' + REST_RATIONALE + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listForProperty = function(pStdVals, pCallbackFunction, pInstName, pPropName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_INSTANCES + '/' + encodeURIComponent(pInstName) + '/' + REST_RATIONALE + '/' + encodeURIComponent(pPropName);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listForPropertyValue = function(pStdVals, pCallbackFunction, pInstName, pPropName, pPropVal, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_INSTANCES + '/' + encodeURIComponent(pInstName) + '/' + REST_RATIONALE + '/' + encodeURIComponent(pPropName) + '/' + encodeURIComponent(pPropVal);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listForRule = function(pStdVals, pCallbackFunction, pRuleName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_RULES + '/' + encodeURIComponent(pRuleName) + '/' + REST_RATIONALE;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

}

function CeStoreApiSentence(pCe) {
	var REST_SENTENCES = 'sentences';
	var REST_SOURCES = 'sources';
	var PARM_ACTION = 'action';
	var PARM_RUNRULES = 'runRules';
	var PARM_RETCE = 'returnCe';
	var PARM_SUPPCE = 'suppressCe';
	var ACTION_SAVE = 'save';
	var ACTION_VALIDATE = 'validate';
	var ACTION_EXECUTE_Q = 'execute_as_query';
	var ACTION_EXECUTE_R = 'execute_as_rule';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiSentence', 'initialise');
		//Nothing needed
	};

	this.listAll = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var restPart = REST_SENTENCES;

		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listQualifiedWith = function(pStdVals, pCallbackFunction, pTypeQual, pValQual, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var restPart = REST_SENTENCES;

		if (!gCe.utils.isNullOrEmpty(pTypeQual)) {
			httpParms.type = pTypeQual;
		}
		
		if (!gCe.utils.isNullOrEmpty(pValQual)) {
			httpParms.validity = pValQual;
		}

		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.add = function(pStdVals, pCallbackFunction, pText, pRunRules, pRetCe, pUserParms) {
		var restPart = REST_SENTENCES;
		performAddSentences(pStdVals, pCallbackFunction, pText, pRunRules, pRetCe, pUserParms, restPart);
	};

	this.addToSource = function(pStdVals, pCallbackFunction, pText, pSourceName, pRunRules, pRetCe, pUserParms) {
		var restPart = REST_SOURCES + '/' + encodeURIComponent(pSourceName);
		performAddSentences(pStdVals, pCallbackFunction, pText, pRunRules, pRetCe, pUserParms, restPart);
	};

	this.uploadToSource = function(pStdVals, pCallbackFunction, pForm, pRunRules, pRetCe, pUserParms) {
		var restPart = REST_SOURCES + '/' + encodeURIComponent(pForm.id);
		performUploadSentences(pStdVals, pCallbackFunction, pForm, pRunRules, pRetCe, pUserParms, restPart);		
	};
	
	this.validate = function(pStdVals, pCallbackFunction, pText, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		httpParms[PARM_ACTION] = ACTION_VALIDATE;

//		var postParms = {};
//		postParms[PARM_CETEXT] = encodeURIComponent(pText);
		var postParms = encodeURIComponent(pText);

		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, REST_SENTENCES, httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, postParms, pUserParms);
	};

	this.executeAsQuery = function(pStdVals, pCallbackFunction, pText, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		httpParms[PARM_ACTION] = ACTION_EXECUTE_Q;
		
		if (pUserParms.suppressCe != null) {
			httpParms[PARM_SUPPCE] = pUserParms.suppressCe;
		}

//		var postParms = {};
//		postParms[PARM_CETEXT] = encodeURIComponent(pText);
		var postParms = encodeURIComponent(pText);

		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, REST_SENTENCES, httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, postParms, pUserParms);
	};

	this.executeAsRule = function(pStdVals, pCallbackFunction, pText, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		httpParms[PARM_ACTION] = ACTION_EXECUTE_R;

//		var postParms = {};
//		postParms[PARM_CETEXT] = encodeURIComponent(pText);
		var postParms = encodeURIComponent(pText);

		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, REST_SENTENCES, httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, postParms, pUserParms);
	};

	this.getDetailsFor = function(pStdVals, pCallbackFunction, pSenId, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SENTENCES + '/' + encodeURIComponent(pSenId);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.deleteSentence = function(pStdVals, pCallbackFunction, pSenId, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SENTENCES + '/' + encodeURIComponent(pSenId);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxDelete(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	function performAddSentences(pStdVals, pCallbackFunction, pText, pRunRules, pRetCe, pUserParms, pRestPart) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (pRunRules) {
			httpParms[PARM_RUNRULES] = pRunRules;
		}

		if (pRetCe) {
			httpParms[PARM_RETCE] = pRetCe;
		}

		httpParms[PARM_ACTION] = ACTION_SAVE;

//		var postParms = {};
//		postParms[PARM_CETEXT] = encodeURIComponent(pText);
		var postParms = encodeURIComponent(pText);

		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, pRestPart, httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, postParms, pUserParms);
	};

	function performUploadSentences(pStdVals, pCallbackFunction, pForm, pRunRules, pRetCe, pUserParms, pRestPart) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (pRunRules) {
			httpParms[PARM_RUNRULES] = pRunRules;
		}

		if (pRetCe) {
			httpParms[PARM_RETCE] = pRetCe;
		}

		httpParms[PARM_ACTION] = ACTION_SAVE;

		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, pRestPart, httpParms);
		ce.api.sendAjaxFormPost(targetUrl, pStdVals, pCallbackFunction, pForm, pUserParms);
	}

}

function CeStoreApiSource(pCe) {
	var REST_SOURCES = 'sources';
	var REST_SENTENCES = 'sentences';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiSource', 'initialise');
		//Nothing needed
	};

	this.listAll = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SOURCES + '/';
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.getDetailsFor = function(pStdVals, pCallbackFunction, pSrcId, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SOURCES + '/' + encodeURIComponent(pSrcId);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.deleteSource = function(pStdVals, pCallbackFunction, pSrcId, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SOURCES + '/' + encodeURIComponent(pSrcId);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxDelete(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listSentencesFor = function(pStdVals, pCallbackFunction, pSrcId, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SOURCES + '/' + encodeURIComponent(pSrcId) + '/' + REST_SENTENCES;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

}

function CeStoreApiSpecial(pCe) {
	var REST_SPECIAL = 'special';
	var REST_CONFIG = 'config';
	var REST_STATS = 'statistics';
	var REST_UID = 'uid';
	var REST_BATCH = 'batch';
	var REST_RESET = 'reset';
	var REST_MULTINSTS = 'instances-for-multiple-concepts';
	var REST_SHADOW_CONS = 'shadow-concepts';
	var REST_SHADOW_INSTS = 'shadow-instances';
	var REST_UNREF_INSTS = 'unreferenced-instances';
	var REST_DCIS = 'diverse-concept-instances';
	var REST_SEARCH = 'keyword-search';
	var PARM_CONS = 'conceptNames';
	var CON_USER = 'CE user';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiSpecial', 'initialise');
		//Nothing needed
	};

	this.listUsers = function(pStdVals, pCallbackFunction, pUserParms) {
		ce.api.concepts.listInstancesFor(pStdVals, pCallbackFunction, CON_USER, null, pUserParms);
	};

	this.showStoreConfig = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SPECIAL + '/' + REST_CONFIG;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};	

	this.showStoreStatistics = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SPECIAL + '/' + REST_STATS;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);

		//Make the call:
		//  GET will simply show the stats
		//  POST will force a garbage collection and then show the stats
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, null, pUserParms);
	};	

	this.updateConfigValue = function(pStdVals, pCallbackFunction, pUserParms, pKeyName, pValue) {
		var ceText = 'perform set \'' + pKeyName + '\' to \'' + gCe.utils.encodeForCe(pValue) + '\'.';
		ce.api.sentences.add(pStdVals, pCallbackFunction, ceText, false, false, pUserParms);
	};	

	this.listInstancesForMultipleConcepts = function(pStdVals, pCallbackFunction, pConNames, pSinceTs, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		httpParms[PARM_CONS] = pConNames;

		if (!ce.utils.isNullOrEmpty(pSinceTs)) {
			httpParms[ce.api.instances.PARM_SINCE] = pSinceTs;
		}

		var restPart = REST_SPECIAL + '/' + REST_MULTINSTS;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listShadowConcepts = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SPECIAL + '/' + REST_SHADOW_CONS;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listShadowInstances = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SPECIAL + '/' + REST_SHADOW_INSTS;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listUnreferencedInstances = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		if (pUserParms.ignoreMetaModel) {
			httpParms.ignoreMetaModel = true;
		}

		var restPart = REST_SPECIAL + '/' + REST_UNREF_INSTS;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.listDiverseConceptInstances = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);

		var restPart = REST_SPECIAL + '/' + REST_DCIS;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.keywordSearch = function(pStdVals, pCallbackFunction, pUserParms, pKeywords) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		httpParms.keywords = pKeywords;

		var restPart = REST_SPECIAL + '/' + REST_SEARCH;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.showNextUid = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var localUserParms = { action: 'show next UID' };
		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);

		var restPart = REST_SPECIAL + '/' + REST_UID;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, userParms);
	};

	this.getNextUid = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var localUserParms = { action: 'get next UID' };
		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);

		var restPart = REST_SPECIAL + '/' + REST_UID;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, null, userParms);
	};

	this.getUidBatch = function(pStdVals, pCallbackFunction, pUserParms, pBatchSize) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		httpParms.size = pBatchSize;
		var localUserParms = { action: 'get UID batch' };
		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);

		var restPart = REST_SPECIAL + '/' + REST_UID + '/' + REST_BATCH;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, null, userParms);
	};

	this.resetUids = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var localUserParms = { action: 'reset UIDs' };
		var userParms = gCe.api.mergeUserParms(pUserParms, localUserParms);

		var restPart = REST_SPECIAL + '/' + REST_UID + '/' + REST_RESET;
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStdVals.store, restPart, httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, null, userParms);
	};

}

function CeStoreApiStore(pCe) {
	var REST_STORES = 'stores';

	var ce = pCe;

	this.initialise = function() {
		ce.msg.debug('CeStoreApiStore', 'initialise');
		//Nothing needed
	};

	this.listAll = function(pStdVals, pCallbackFunction, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, null, REST_STORES, httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.getStoreFor = function(pStdVals, pCallbackFunction, pStoreName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pStoreName, '', httpParms);
		ce.api.sendAjaxGet(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

	this.showStats = function(pStdVals, pCallbackFunction, pUserParms) {
		this.getStoreFor(pStdVals, pCallbackFunction, pUserParms);
	};

	this.createNew = function(pStdVals, pCallbackFunction, pCeStoreName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pCeStoreName, '', httpParms);
		ce.api.sendAjaxPost(targetUrl, pStdVals, pCallbackFunction, true, pUserParms);
	};

	this.deleteStore = function(pStdVals, pCallbackFunction, pCeStoreName, pUserParms) {
		var httpParms = ce.api.defaultHttpParms(pStdVals);
		var targetUrl = ce.api.constructUrlFrom(pStdVals.address, pCeStoreName, '', httpParms);
		ce.api.sendAjaxDelete(targetUrl, pStdVals, pCallbackFunction, pUserParms);
	};

}
