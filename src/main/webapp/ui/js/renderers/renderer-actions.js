/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.renderer.actions = new RendererActions();

function RendererActions() {

	var DOM_CONFLIST = 'configValuesPane';

	this.initialise = function() {
		gCe.msg.debug('RendererActions', 'initialise');
		//Nothing needed
	};

	this.renderLibraryVersionText = function() {
		var msgText = '';

		if (gEp.ui.isDojoLoaded()) {
			msgText += 'dojo = ' + dojo.version.toString();
		}

		if (gEp.dlg.map !== undefined) {
			msgText += gEp.dlg.map.getVersionDetails();
		}

		return msgText;
	};

	this.renderStoreConfigList = function(pResponse, pUserParms) {
		var html = '';
	
		if (pResponse != null) {
			var sr = gCe.utils.getStructuredResponseFrom(pResponse);
			
			if (sr != null) {
//				var genProps = sr.general_properties;
//				
//				if (genProps != null) {
//					var hdrs = ['Key', 'Value'];
//					var rows = [];
//
//					for (var key in genProps) {
//						rows.push( [ key, genProps[key] ] );
//					}
//
//					html += '<h2>General properties (not editable)</h2>';
//					html += gEp.ui.htmlTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);
//				}

				var envProps = sr.store_properties;
				
				if (envProps != null) {
					var hdrs = ['Key', 'Value', 'Action'];
					var rows = [];

					for (var key in envProps) {
						var thisVal = envProps[key];
						rows.push( [ key, thisVal, gEp.ui.links.editConfigValue(key, thisVal) ] );
					}

					html += '<h2>Store properties</h2>';
					html += gEp.ui.htmlTableFor(hdrs, rows, gEp.ui.DEFAULT_STYLE);
				}
			}
		}
		
		var domConfList = dojo.byId(DOM_CONFLIST);
		
		if (domConfList != null) {
			domConfList.innerHTML = html;
		} else {
			gEp.reportError('Can render config list as element \'' + DOM_CONFLIST + '\' cannot be found');
		}
	};

}
