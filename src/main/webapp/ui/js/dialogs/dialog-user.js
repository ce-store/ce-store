/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.user = new DialogUser();

function DialogUser() {
	var DLG_FORM_LOGIN = 'formUserLogin';
	var DLG_USERNAME = 'dlgUserName';

	this.DLG_USERNAME = 'userName';

	this.initialise = function() {
		gCe.msg.debug('DialogUser', 'initialise');
		//Nothing needed
	};

	this.clearUserName = function() {
		var dlgUserName = document.getElementById(this.DLG_USERNAME);

		if (dlgUserName != null) {
			dlgUserName.innerHTML = gEp.ui.links.login();
		}
	};

	this.openLoginList = function(pUserList) {
		if (!gCe.utils.isNullOrEmpty(pUserList)) {
			var formDlg = dijit.byId(DLG_FORM_LOGIN);
			var domUserList = dijit.byId(DLG_USERNAME);

			var fpUserData = {
					identifier: 'id',
					label: 'name',
					items: []
			};

			var fpUserStore = new dojo.data.ItemFileWriteStore({
				data: fpUserData  
			});

			domUserList.set('store', fpUserStore);

			for (var key in pUserList) {
				var thisUser = pUserList[key];
				var screenName = thisUser.screenName;
				
				if (screenName == null) {
					screenName = thisUser.userName;
				}

				fpUserStore.newItem( { id: thisUser.userName, name: screenName } );
			}

			//Ensure that the list of users is empty (since this dialog can only be opened when no one is logged in)
			domUserList.reset();

			formDlg.show();
		} else {
			gCe.msg.error('No users are defined, so login is not possible.  Load some CE sentences to resolve this.');
		}
	};

	this.saveSelectedUser = function(pUserList) {
		var formDlg = dijit.byId(DLG_FORM_LOGIN);
		formDlg.hide();

		//Upate the currentUser with the selected value
		var domUserList = dijit.byId(DLG_USERNAME);
		var selUserName = domUserList.get('value');

		if (!gCe.utils.isNullOrEmpty(selUserName)) {
			for (var key in pUserList){
				var thisUser = pUserList[key];

				if (typeof thisUser.screenName == 'undefined') {
					thisUser.screenName = thisUser.userName;
				}

				if (thisUser.userName === selUserName) {
					gCe.utils.setLoggedInUser(thisUser.userName, thisUser.screenName);
					this.setUserName(thisUser.userName, thisUser.screenName);
				}
			}
		}
	};

	this.setUserName = function(pUserId, pUserName) {
		var dlgUserName = document.getElementById(this.DLG_USERNAME);

		if (dlgUserName != null) {
			dlgUserName.innerHTML = gEp.ui.htmlForUserNamed(pUserId, pUserName);
		}
	};

}