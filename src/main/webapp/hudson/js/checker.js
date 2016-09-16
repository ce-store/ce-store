/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gChecker = new Checker();

function Checker() {
	
	this.compareExecuteReplies = function(pAr1, pAr2, pMaxDepth) {
		return compareJson(pAr1, pAr2, pMaxDepth, "execute");
	};

	this.compareAnswerReplies = function(pAr1, pAr2, pMaxDepth) {
		return compareJson(pAr1, pAr2, pMaxDepth, "answer");
	};

	this.compareInterpretReplies = function(pAr1, pAr2, pMaxDepth) {
		return compareJson(pAr1, pAr2, pMaxDepth, "interpret");
	};

	function compareJson(pAr1, pAr2, pMaxDepth, pRootName) {
		var result = "";
		
		result += compareObjects(pAr1, pAr2, pMaxDepth, pRootName);

		if (result != '') {
			gHudson.reportLog('Difference in ' + pRootName + ' result detected (cached, live):');
			gHudson.reportLog(JSON.stringify(pAr1));
			gHudson.reportLog(JSON.stringify(pAr2));
		}

		return result;
	}

	function compareObjects(pObj1, pObj2, pMaxDepth, pErrCode) {
		var result = "";

		if ((pObj1 != null) && (pObj2 != null)) {
			var elemNames = computeElementNames(pObj1, pObj2);

			for (var i = 0; i < elemNames.length; i++) {
				var elemName = elemNames[i];

				if (result == "") {
					var val1 = pObj1[elemName];
					var val2 = pObj2[elemName];

					if ((val1 != null) && (val2 != null)) {
						if (typeof val1 == "object") {
							result += compareObjects(val1, val2, pMaxDepth, pErrCode + "." + elemName);
						} else {
							if (val1 != val2) {
								result = pErrCode + "." + elemName;
								console.log("val1=" + val1);
								console.log("val2=" + val2);
							}
						}
					} else {
						result += pErrCode + ".!" + elemName;
					}
				}
			}			
		} else {
			result += '!' + pErrCode;	//One or both of the replies is null
		}

		return result;
	};

	function computeElementNames(pAr1, pAr2) {
		var result = [];

		for (var i in pAr1) {
			if (result.indexOf(i) == -1) {
				result.push(i);
			}
		}

		for (var i in pAr2) {
			if (result.indexOf(i) == -1) {
				result.push(i);
			}
		}

		return result;
	}

}
