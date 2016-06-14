/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/
gEp.ui = new CeStoreUi();

function CeStoreUi() {
	var CONTAINER_DETAILS = 'detailsContainer';
	var CONTAINER_MAIN = 'mainContainer';
	var CONTAINER_MENU = 'menuContainer';
	var CONTAINER_MSGS = 'feedbackContainer';
	var CONTAINER_FORM = 'formContainer';

	this.DEFAULT_STYLE = 'ce_plain';

	this.pane = {};		//specific panes are created when each .js in /ui/panes/ is loaded
	this.form = {};		//specific forms are created when each .js in /ui/forms/ is loaded

	var tabGroups = initialiseTabGroups();
	var formGroup = [];

	var tabFuncs = {};

	this.MODE_DETAILS = 'details';
	this.MODE_LISTITEM = 'list_item';
	this.MODE_LISTFOOTER = 'list_footer';
	
	this.initialise = function() {
		this.links.initialise();

		initialiseTabs();
	};

	this.renderUiComponents = function() {
		for (var key in tabGroups) {
			this.renderTabGroup(key);
		}

		for (var key in formGroup) {
			this.renderForm(formGroup[key]);
		}
	};

	this.registerTab = function(pTabId, pContainerId, pTabIndex, pTitle, pHtml) {
		if (pContainerId != null) {
			if (tabGroups[pContainerId] !== undefined) {
				tabGroups[pContainerId].tabs.push({
					id: pTabId,
					position: pTabIndex,
					title: pTitle,
					content: pHtml
				});
			} else {
				gCe.msg.warning('Unknown containerId', 'RegisterTab', [ pTabId ]);
			}
		} else {
			gCe.msg.warning('No containerId specified when registering tab', 'registerTab');
		}
	};

	this.registerForm = function(pFormId, pHtml) {
		formGroup.push({ id: pFormId, content: pHtml });
	};

	this.renderTabGroup = function(pContainerId) {
		var matched = false;

		for (var key in tabGroups) {
			if (key === pContainerId) {
				var tgtContainer = dijit.byId(pContainerId);
				var thisTg = tabGroups[key];
				var sortedTabs = thisTg.tabs.sort(sortTabsByPosition);
				for (var tKey in sortedTabs) {
					var thisTab = thisTg.tabs[tKey];
					matched = true;

					require(['dijit/layout/ContentPane'],
						function(ContentPane) {
							tgtContainer.addChild(new ContentPane({
								id: thisTab.id,
								title: thisTab.title,
								content: thisTab.content
							}));
					});
				}
			}
		}

		if (!matched) {
			gCe.msg.warning('No tab found for container', 'renderTabGroup', [ pContainerId ]);
		}
	};

	this.renderForm = function(pForm) {
		require(['dijit/layout/ContentPane'],
			function(ContentPane) {
				new ContentPane({
					id: pForm.id,
					content: pForm.content
				}).placeAt(CONTAINER_FORM);
		});
	};

	function sortTabsByPosition(a, b) {
		return (a.position - b.position);
	}

	function initialiseTabGroups() {
		return {
			menuContainer: {
				tabs: []
			},
			mainContainer: {
				tabs: []
			},
			detailsContainer: {
				tabs: []
			},
			feedbackContainer: {
				tabs: []
			}
		};
	}

	function initialiseTabs() {
		//Register for menu container
		var menuContainer = dijit.byId(CONTAINER_MENU);
		dojo.connect(menuContainer, '_transition', function(newPage, oldPage) {
			gEp.ui.switchedTabs(menuContainer, newPage, oldPage);
		});

		//Register for main container
		var mainContainer = dijit.byId(CONTAINER_MAIN);
		dojo.connect(mainContainer, '_transition', function(newPage, oldPage) {
			gEp.ui.switchedTabs(mainContainer, newPage, oldPage);
		});

		//Register for details container
		var detContainer = dijit.byId(CONTAINER_DETAILS);
		dojo.connect(detContainer, '_transition', function(newPage, oldPage) {
			gEp.ui.switchedTabs(detContainer, newPage, oldPage);
		});

		//Register for messages container
		var msgContainer = dijit.byId(CONTAINER_MSGS);
		dojo.connect(msgContainer, '_transition', function(newPage, oldPage) {
			gEp.ui.switchedTabs(msgContainer, newPage, oldPage);
		});
	}

	this.registerTabSwitchFunction = function(pContainerName, pTabName, pFunction) {
		gCe.msg.debug(tabFuncs, 'registerTabSwitchFunction', [ pContainerName, pTabName, pFunction ] );

		if (tabFuncs[pContainerName] === undefined) {
			tabFuncs[pContainerName] = {};
		}

		tabFuncs[pContainerName][pTabName] = pFunction;
	};

	this.switchedTabs = function(pContainer, pNewPage, pOldPage, pExtra) {
		//This is called whenever a tab is shown
		var tgtFunc = null;

		if (tabFuncs[pContainer.id] !== undefined) {
			tgtFunc = tabFuncs[pContainer.id][pNewPage.id];
		}

		gCe.msg.debug('tabName=' + pNewPage.id + ', containerName=' + pContainer.id, 'switchedTabs', [ tgtFunc, tabFuncs ] );

		if (tgtFunc != null) {
			//Run the required function
			tgtFunc();
		}
	};

	this.updatePaneWith = function(pHtml, pTitle, pPaneId, pContainerId, pDojoParse) {
		if (pPaneId != null) {
			var tgt = document.getElementById(pPaneId);

			if (tgt != null) {
				tgt.innerHTML = pHtml;

				if (pDojoParse) {
					dojo.parser.parse(tgt);		//Have dojo re-parse the page (to process any dojo elements)
				}

				if (pTitle != null) {
					//Use the dijit accessor to set the title
					dijit.byId(pPaneId).set('title', pTitle);
				}

				if (pContainerId != null) {
					this.activateTab(pPaneId, pContainerId);
				}
			} else {
				gCe.msg.warning('Could not locate pane named \'' + pPaneId + '\'', 'updatePaneWith');
			}
		} else {
			gCe.msg.warning('No paneId specified', 'updatePaneWith');
		}
	};

	this.updateAndParsePaneWith = function(pHtml, pTitle, pPaneId, pContainerId) {
		this.updatePaneWith(pHtml, pTitle, pPaneId, pContainerId, true);
	};

	this.updateFormWith = function(pHtml, pFormId) {
		if (pFormId != null) {
			var tgtForm = document.getElementById(pFormId);

			if (tgtForm != null) {
				tgtForm.innerHTML = pHtml;
				dojo.parser.parse(tgtForm);		//Have dojo re-parse the html (to process any dojo elements)
			} else {
				gCe.msg.warning('Could not locate form named \'' + pFormId + '\'', 'drawForm');
			}
		} else {
			gCe.msg.warning('No formId specified', 'updateFormWith');
		}
	};

	this.activateTab = function(pTargetId, pParentId) {
		//If a parent is specified ensure that the target (child) is selected so that it is brought to the front
		if (!gCe.utils.isNullOrEmpty(pParentId)) {
			var tabCtr = dijit.byId(pParentId);

			if (!gCe.utils.isNullOrEmpty(tabCtr)) {
				tabCtr.selectChild(pTargetId);
			} else {
				gCe.msg.warning('Could not locate tab named \'' + pParentId + '\'', 'activateTab');
			}
		}
	};

	this.activeMainTab = function() {
		return dijit.byId(CONTAINER_MAIN).selectedChildWidget;
	};

	this.isDojoLoaded = function() {
		return (typeof dojo !== 'undefined');
	};

	this.isD3Loaded = function() {
		return (typeof d3 !== 'undefined');
	};

	this.htmlCreationDate = function(pVal) {
		return '<a title="Original value: ' + pVal + '">' + this.formattedDateTimeStringFor(pVal) + '</a>';
	};

	this.htmlLoggedInUserText = function() {
		var userText = null;
		var currentUserId = gCe.utils.getLoggedInUserId();
		var currentUserName = gCe.utils.getLoggedInUserName();

		if (gCe.utils.isNullOrEmpty(currentUserName)) {
			userText = '<span id="' + gEp.dlg.user.DLG_USERNAME + '">' + gEp.ui.links.login() + '</span>';
		} else {
			userText = '<span id="' + gEp.dlg.user.DLG_USERNAME + '">' + this.htmlForUserNamed(currentUserId, currentUserName) + '</span>';
		}

		return userText;
	};

	this.htmlCurrentCeStore = function() {
		var prefix = '';
		
		if (gEp.getDomainAndApp().indexOf('http') === 0) {
			prefix = 'remote:';
		}
		
		var hoverText = 'Full CE store address is ' + gEp.getDomainAndApp() + gEp.currentCeStore;
		return '<span id="storeName">Store ' + prefix + gEp.ui.links.storeDetails(gEp.currentCeStore, gEp.currentCeStore, hoverText) + '</span>';
	};

	this.htmlForUserNamed = function(pUserId, pUserName) {
		var userLink = gEp.ui.links.instanceDetails(pUserId, pUserName);
		var logoutLink = gEp.ui.links.logout();

		return 'Logged in as ' + userLink + ' (' + logoutLink + ')';
	};

	this.htmlEmphasise = function(pTemplate, pTarget) {
		return gCe.utils.replaceAll(pTemplate, '%01', '<strong>' + pTarget + '</strong>');
	};

	this.htmlEmphasise = function(pTemplate, pTgt1, pTgt2) {
		var result = pTemplate;
		result = gCe.utils.replaceAll(result, '%01', '<strong>' + pTgt1 + '</strong>');
		result = gCe.utils.replaceAll(result, '%02', pTgt2);

		return result;
	};

	this.htmlTableFor = function(pHdrs, pRows, pClassName, pClassRows) {
		var result = '';

		result += htmlTableOpen(pClassName);
		result += htmlHeaderRow(pHdrs, pClassName);
		result += htmlForAllTableRows(pRows, pClassName, pClassRows);
		result += htmlTableClose();

		return result;
	};

	this.htmlVerticalTableFor = function(pHdrs, pRows, pClassName) {
		var result = '';
		var vertRows = [];

		for (var key in pHdrs) {
			var thisHdr = pHdrs[key];
			var thisVal = pRows[key];

			vertRows.push( [thisHdr, thisVal ] );

		}

		result += htmlTableOpen(pClassName);
		result += htmlHeaderRow([], pClassName);
		result += htmlForAllTableRows(vertRows, pClassName);
		result += htmlTableClose();

		return result;
	};

	this.htmlUnorderedListFor = function(pList, pClassName) {
		var result = '';

		if (!gCe.utils.isNullOrEmpty(pList)) {
			result += htmlUlOpen(pClassName);
			result += htmlForAllListItems(pList, pClassName);
			result += htmlUlClose();
		}

		return result;
	};

	this.htmlHdrNotComplete = function(pHdrText) {
		return '<h3>Not yet completed: ' + pHdrText + '</h3>';
	};

	this.htmlPaneHeaderFor = function(pHdrText, pSubText) {
		var result = '';

		result += pHdrText + '<br/><br/>';

		if (pSubText != null) {
			result += pSubText + '<br/><br/>';
		}

		return result;
	};

	this.formattedDateTimeStringFor = function(pTs) {
		var result = null;
		var tsVal = parseInt(pTs);
		var dt = new Date(tsVal);

		if (dt != null) {
			result = this.formatDate(dt);
		} else {
			result = pTs;
		}

		return result;
	};

	this.formatDate = function(pDt) {
		return padTo2(pDt.getDate()) + '/' + padTo2(pDt.getMonth() + 1) + '/' + pDt.getFullYear() + ' ' + padTo2(pDt.getHours()) + ':' + padTo2(pDt.getMinutes()) + ':' + padTo2(pDt.getSeconds());
	};

	this.formatTime = function(pDt) {
		return padTo2(pDt.getHours()) + ':' + padTo2(pDt.getMinutes()) + ':' + padTo2(pDt.getSeconds()) + ':' + padTo3(pDt.getMilliseconds());
	};
	
	this.renderDatatypeValue = function(pValue, pPropName, pInst) {
		var result = pValue;
	
		if (pInst != null) {
			//image:url - render as image
			if (gCe.utils.isDirectOrInheritedConcept(pInst, 'image')) {
				if (pPropName === 'url') {
					result = htmlForImageThumbnail(pValue);
				}
			}

			//pictoral thing:image url - render as image
			if (gCe.utils.isDirectOrInheritedConcept(pInst, 'pictoral thing')) {
				if (pPropName === 'image url') {
					result = htmlForImageThumbnail(pValue);
				}
			}

			//thing:picture - render as image
			if (gCe.utils.isDirectOrInheritedConcept(pInst, 'thing')) {
				if (pPropName === 'picture') {
					result = htmlForImageThumbnail(pValue);
				}
			}
		}		

		return result;
	};
	
	function htmlForImageThumbnail(pValue) {
		return '<img src="' + pValue + '" height="80"></img><br/>' + pValue + ' ' + gEp.ui.links.externalHyperlinkFor(pValue);
	}

	function padTo2(pNum) {
		return (pNum < 10 ? '0' : '') + pNum;
	}

	function padTo3(pNum) {
		var result = null;

		if (pNum < 100) {
			result = '0' + padTo2(pNum);
		} else {
			result = pNum;
		}

		return result;
	}

	function padTo4(pNum) {
		var result = null;

		if (pNum < 1000) {
			result = '0' + padTo3(pNum);
		} else {
			result = pNum;
		}

		return result;
	}

	function htmlTableOpen(pClassName) {
		return '<table' + classTextFor(pClassName) + '>';
	};

	function htmlTableClose() {
		return '</table>';
	};

	function htmlHeaderRow(pHdrs, pClassName) {
		var result = '';

		if (!gCe.utils.isNullOrEmpty(pHdrs)) {
			result += '<thead' + classTextFor(pClassName) + '>';
			result += '<tr>';

			for (var key in pHdrs) {
				result += '<th>' + pHdrs[key] + '</th>';
			}

			result += '</tr>';
			result += '</thead>';
		}

		return result;
	};

	function htmlForAllTableRows(pRows, pClassName, pClassRows) {
		var result = '';

		if (!gCe.utils.isNullOrEmpty(pRows)) {
			result += '<tbody' + classTextFor(pClassName) + '>';

			for (var key in pRows) {
				var overrideClassName = null;

				if (pClassRows != null) {
					overrideClassName = pClassRows[key];
				}

				result += htmlForTableRow(pRows[key], pClassName, overrideClassName);
			}
	
			result += '</tbody>';
		}

		return result;
	};

	function htmlForTableRow(pRowVals, pClassName, pClassRowVal) {
		var result = '';
		var classText = classTextFor(pClassName, pClassRowVal);

		result += '<tr' + classText + '>';

		for (var key in pRowVals) {
			var thisVal = pRowVals[key];
			var valText = null;

			if (!gCe.utils.isNullOrEmpty(thisVal)) {
				if (gCe.utils.isArray(thisVal)) {
					valText = gEp.ui.htmlUnorderedListFor(thisVal);
				} else {
					valText = thisVal;
				}
			} else {
				valText = '';
			}

			result += '<td' + classText + '>' + valText + '</td>';
		}

		result += '</tr>';

		return result;
	};

	function htmlUlOpen(pClassName) {
		return '<ul' + classTextFor(pClassName) + '>';
	};

	function htmlUlClose() {
		return '</ul>';
	};

	function htmlForAllListItems(pList, pClassName) {
		var result = '';

		for (var key in pList) {
			result += '<li' + classTextFor(pClassName) + '>' + pList[key] + '</li>';
		}

		return result;
	};

	function classTextFor(pClassNameDefault, pClassNameExtra) {
		var classText = null;
		var extraClassText = '';

		if (pClassNameExtra != null) {
			extraClassText = pClassNameExtra;
		}

		if (pClassNameDefault != null) {
			classText = ' class="' + pClassNameDefault + ' ' + extraClassText + '"';
		} else {
			if (extraClassText !== '') {
				classText = ' class="' + extraClassText + '"';
			} else {
				classText = '';
			}
		}

		return classText;
	}

}