/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.ceqb.menu = new DialogCeqbMenu();

function DialogCeqbMenu() {
	var ceqb = gEp.dlg.ceqb;

	this.initialise = function() {
		gCe.msg.debug('DialogCeqbMenu', 'initialise');
		//Nothing needed
	};

	this.doubleclickConcept = function(pConcept) {
		dojo.connect(pConcept.gfx_icon.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.addFilterToConcept(pConcept); });
		dojo.connect(pConcept.dom_label, 'ondblclick', function() { gEp.dlg.ceqb.actions.addFilterToConcept(pConcept); });
		dojo.connect(pConcept.dom_countlabel, 'ondblclick', function() { gEp.dlg.ceqb.actions.addFilterToConcept(pConcept); });
	};

	this.menuConcept = function(pConcept) {
		var visConId = pConcept.shape_id;
		var visConLabelId = ceqb.model.calculateVisLabelIdFor(pConcept.shape_id);
		var newMenu = new dijit.Menu({
			targetNodeIds:[visConId, visConLabelId]
		});

		newMenu.connect(newMenu, 'onOpen', function(e) {menuConceptEnableOrDisable(e, newMenu, pConcept); });

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_showdef',
			label:'Show concept definition',
			onClick: function(){ gEp.dlg.ceqb.actions.showConceptDefinition(pConcept); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_renamevar',
			label:'Rename variable',
			onClick: function(){ gEp.dlg.ceqb.actions.renameVariable(pConcept, 'concept'); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_incresults',
			label:'Include in results',
			onClick: function(){ gEp.dlg.ceqb.actions.includeConceptInResults(pConcept); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_excresults',
			label:'Exclude from results',
			onClick: function(){ gEp.dlg.ceqb.actions.excludeConceptFromResults(pConcept); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_del',
			label:'Delete concept',
			onClick: function(){ gEp.dlg.ceqb.actions.deleteConcept(pConcept, false, true); }
		}));

		newMenu.addChild(new dijit.MenuSeparator());

		//Only shown if this concept is a premise 
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_switchconc',
			label:'Switch to conclusion',
			onClick: function(){ gEp.dlg.ceqb.actions.switchConceptToConclusion(pConcept); }
		}));

		//Only shown if this concept is a conclusion
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_switchprem',
			label:'Switch to premise',
			onClick: function(){ gEp.dlg.ceqb.actions.switchConceptToPremise(pConcept); }
		}));

		//Only shown if this concept is selected as sources
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_remrelstart',
			label:'Remove relationship start',
			onClick: function(){ gEp.dlg.ceqb.actions.startRelationshipAtConcept(pConcept, false); }
		}));

		//Only shown if this concept is not selected as source
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_startrel',
			label:'Start relationship',
			onClick: function(){ gEp.dlg.ceqb.actions.startRelationshipAtConcept(pConcept, true); }
		}));

		//Only enabled if a concept is selected as source
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_endrel',
			label:'End relationship',
			onClick: function(){ gEp.dlg.ceqb.actions.endRelationshipAtConcept(pConcept); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_addfilt',
			label:'Add attribute',
			onClick: function(){ gEp.dlg.ceqb.actions.addFilterToConcept(pConcept); }
		}));

		newMenu.addChild(new dijit.MenuSeparator());

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_refcount',
			label:'Refresh count',
			onClick: function(){ gEp.dlg.ceqb.actions.refreshConceptCount(pConcept); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_showcountce',
			label:'Show concept count CE',
			onClick: function(){ gEp.dlg.ceqb.actions.showConceptCountCE(pConcept); }
		}));

		newMenu.addChild(new dijit.MenuSeparator());

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_concept_inspect',
			label:'Inspect',
			onClick: function() { ceqb.actions.inspectConcept(pConcept); }
		}));

		newMenu.startup();
	};

	this.doubleclickRelationship = function(pRel) {
		dojo.connect(pRel.gfx_label.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editRelationship(pRel); });
		dojo.connect(pRel.gfx_line.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editRelationship(pRel); });
		dojo.connect(pRel.gfx_countlabel.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editRelationship(pRel); });
	};

	this.menuRelationship = function(pRel) {
		var visRelLabelId = ceqb.model.calculateVisLabelIdFor(pRel.shape_id);
		var newMenu = new dijit.Menu({
			targetNodeIds:[visRelLabelId]
		});

		newMenu.connect(newMenu, 'onOpen', function(e) {menuRelEnableOrDisable(e, newMenu, pRel); });

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_rel_edit',
			label:'Edit relationship',
			onClick: function(){ gEp.dlg.ceqb.actions.editRelationship(pRel); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_rel_del',
			label:'Delete relationship',
			onClick: function(){ gEp.dlg.ceqb.actions.deleteRelationship(pRel, false, true); }
		}));

		//Only shown if this relationship is a premise 
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_rel_switchconc',
			label:'Switch to conclusion',
			onClick: function(){ gEp.dlg.ceqb.actions.switchRelationshipToConclusion(pRel); }
		}));

		//Only shown if this relationship is a conclusion
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_rel_switchprem',
			label:'Switch to premise',
			onClick: function(){ gEp.dlg.ceqb.actions.switchRelationshipToPremise(pRel); }
		}));

		newMenu.addChild(new dijit.MenuSeparator());

		//Only shown if this relationship is a premise 
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_rel_refcount',
			label:'Refresh count',
			onClick: function(){ gEp.dlg.ceqb.actions.refreshRelationshipCount(pRel); }
		}));

		//Only shown if this relationship is a premise 
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_rel_relres',
			label:'Show relationship results',
			onClick: function(){ gEp.dlg.ceqb.actions.showRelationshipResults(pRel); }
		}));

		//Only shown if this relationship is a premise 
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_rel_showcountce',
			label:'Show relationship count CE',
			onClick: function(){ gEp.dlg.ceqb.actions.showRelationshipCountCE(pRel); }
		}));

		newMenu.addChild(new dijit.MenuSeparator());

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_rel_inspect',
			label:'Inspect',
			onClick: function() { ceqb.actions.inspectRelationship(pRel); }
		}));

		newMenu.startup();
	};

	this.doubleclickFilter = function(pFilter) {
		dojo.connect(pFilter.dom_label, 'ondblclick', function() { gEp.dlg.ceqb.actions.editFilter(pFilter); });
		dojo.connect(pFilter.gfx_icon.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editFilter(pFilter); });
		dojo.connect(pFilter.gfx_line.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editFilter(pFilter); });
		dojo.connect(pFilter.gfx_countlabel.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editFilter(pFilter); });
		dojo.connect(pFilter.gfx_linelabel.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editFilter(pFilter); });
	};

	this.menuFilter = function(pFilter) {
		var visFilId = pFilter.shape_id;
		var visFilLabelId = ceqb.model.calculateVisLabelIdFor(pFilter.shape_id);
		var visFilLineLabelId = ceqb.model.calculateVisLineLabelIdFor(pFilter.shape_id);
		var newMenu = new dijit.Menu({
			targetNodeIds:[visFilId, visFilLabelId, visFilLineLabelId]
		});

		newMenu.connect(newMenu, 'onOpen', function(e) {menuFilterEnableOrDisable(e, newMenu, pFilter); });

		newMenu.addChild(new dijit.MenuItem({
			label:'Rename variable',
			onClick: function(){ gEp.dlg.ceqb.actions.renameVariable(pFilter, 'filter'); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_incresults',
			label:'Include in results',
			onClick: function(){ gEp.dlg.ceqb.actions.includeFilterInResults(pFilter); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_excresults',
			label:'Exclude from results',
			onClick: function(){ gEp.dlg.ceqb.actions.excludeFilterFromResults(pFilter); }
		}));

		//Only shown if this filter is a premise 
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_switchconc',
			label:'Switch to conclusion',
			onClick: function(){ gEp.dlg.ceqb.actions.switchFilterToConclusion(pFilter); }
		}));

		//Only shown if this filter is a conclusion
		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_switchprem',
			label:'Switch to premise',
			onClick: function(){ gEp.dlg.ceqb.actions.switchFilterToPremise(pFilter); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_delete',
			label:'Delete attribute',
			onClick: function(){ gEp.dlg.ceqb.actions.deleteFilter(pFilter, false, true); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_edit',
			label:'Edit attribute',
			onClick: function(){ gEp.dlg.ceqb.actions.editFilter(pFilter); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_linkstart',
			label:'Link attribute (start)',
			onClick: function(){ gEp.dlg.ceqb.actions.startLinkFilter(pFilter, true); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_linkremove',
			label:'Link attribute (remove)',
			onClick: function(){ gEp.dlg.ceqb.actions.startLinkFilter(pFilter, false); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_linkend',
			label:'Link attribute (end)',
			onClick: function(){ gEp.dlg.ceqb.actions.endLinkFilter(pFilter); }
		}));

		newMenu.addChild(new dijit.MenuSeparator());

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_refreshcount',
			label:'Refresh count',
			onClick: function(){ gEp.dlg.ceqb.actions.refreshFilterCount(pFilter); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_filter_showresults',
			label:'Show attribute results',
			onClick: function(){ gEp.dlg.ceqb.actions.showFilterResults(pFilter); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			label:'Show attribute count CE',
			name: 'mnu_filter_showcountce',
			onClick: function(){ gEp.dlg.ceqb.actions.showFilterCountCE(pFilter); }
		}));

		newMenu.addChild(new dijit.MenuSeparator());

		newMenu.addChild(new dijit.MenuItem({
			label:'Inspect',
			name: 'mnu_filter_inspect',
			onClick: function() { ceqb.actions.inspectFilter(pFilter); }
		}));

		newMenu.startup();
	};

	this.doubleclickLinkedFilter = function(pLf) {
		dojo.connect(pLf.gfx_line.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editLinkedFilter(pLf); });
		dojo.connect(pLf.gfx_label.rawNode, 'ondblclick', function() { gEp.dlg.ceqb.actions.editLinkedFilter(pLf); });
	};

	this.menuLinkedFilter = function(pLf) {
		var visRelLabelId = ceqb.model.calculateVisLabelIdFor(pLf.shape_id);
		var newMenu = new dijit.Menu({
			targetNodeIds:[visRelLabelId]
		});

		newMenu.connect(newMenu, 'onOpen', function(e) {menuLinkedFilterEnableOrDisable(e, newMenu, pLf); });

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_lf_edit',
			label:'Edit link',
			onClick: function(){ gEp.dlg.ceqb.actions.editLinkedFilter(pLf); }
		}));

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_lf_del',
			label:'Delete link',
			onClick: function(){ gEp.dlg.ceqb.actions.deleteLinkedFilter(pLf, false, true); }
		}));

		newMenu.addChild(new dijit.MenuSeparator());

		newMenu.addChild(new dijit.MenuItem({
			name: 'mnu_lf_inspect',
			label:'Inspect',
			onClick: function() { ceqb.actions.inspectLinkedFilter(pLf); }
		}));

		newMenu.startup();
	};

	function menuConceptEnableOrDisable(pE, pMenu, pConcept) {
		var mi1 = getMenuItemByName(pMenu, 'mnu_concept_remrelstart');
		var mi2 = getMenuItemByName(pMenu, 'mnu_concept_startrel');
		var mi3 = getMenuItemByName(pMenu, 'mnu_concept_endrel');
		var mi4 = getMenuItemByName(pMenu, 'mnu_concept_incresults');
		var mi5 = getMenuItemByName(pMenu, 'mnu_concept_excresults');
		var mi6 = getMenuItemByName(pMenu, 'mnu_concept_switchconc');
		var mi7 = getMenuItemByName(pMenu, 'mnu_concept_switchprem');

		if (pConcept == ceqb.srcConcept) {
			showMenuItem(mi1);
			hideMenuItem(mi2);
		} else {
			showMenuItem(mi2);
			hideMenuItem(mi1);
		}

		if (!gCe.utils.isNullOrEmpty(ceqb.srcConcept)) {
			enableMenuItem(mi3);
		} else {
			disableMenuItem(mi3);
		}

		if (pConcept.incInResults) {
			disableMenuItem(mi4);
			enableMenuItem(mi5);
		} else {
			enableMenuItem(mi4);
			disableMenuItem(mi5);
		}

		if (ceqb.model.isPremiseConcept(pConcept)) {
			showMenuItem(mi6);
			hideMenuItem(mi7);
		} else {
			hideMenuItem(mi6);
			showMenuItem(mi7);
		}
	}

	function menuRelEnableOrDisable(pE, pMenu, pRel) {
		var mi1 = getMenuItemByName(pMenu, 'mnu_rel_switchconc');
		var mi2 = getMenuItemByName(pMenu, 'mnu_rel_switchprem');
		var mi3 = getMenuItemByName(pMenu, 'mnu_rel_refcount');
		var mi4 = getMenuItemByName(pMenu, 'mnu_rel_relres');
		var mi5 = getMenuItemByName(pMenu, 'mnu_rel_showcountce');

		if (ceqb.model.isPremiseRel(pRel)) {
			showMenuItem(mi1);
			hideMenuItem(mi2);
			enableMenuItem(mi3);
			enableMenuItem(mi4);
			enableMenuItem(mi5);
		} else {
			hideMenuItem(mi1);
			showMenuItem(mi2);
			disableMenuItem(mi3);
			disableMenuItem(mi4);
			disableMenuItem(mi5);
		}	
	}

	function menuFilterEnableOrDisable(pE, pMenu, pFilter) {
		var mi1 = getMenuItemByName(pMenu, 'mnu_filter_incresults');
		var mi2 = getMenuItemByName(pMenu, 'mnu_filter_excresults');
		var mi3 = getMenuItemByName(pMenu, 'mnu_filter_switchconc');
		var mi4 = getMenuItemByName(pMenu, 'mnu_filter_switchprem');
		var mi5 = getMenuItemByName(pMenu, 'mnu_filter_linkstart');
		var mi6 = getMenuItemByName(pMenu, 'mnu_filter_linkend');
		var mi7 = getMenuItemByName(pMenu, 'mnu_filter_linkremove');

		if (pFilter.incInResults) {
			disableMenuItem(mi1);
			enableMenuItem(mi2);
		} else {
			enableMenuItem(mi1);
			disableMenuItem(mi2);
		}

		if (ceqb.model.isPremiseFilter(pFilter)) {
			showMenuItem(mi3);
			hideMenuItem(mi4);
		} else {
			showMenuItem(mi4);
			hideMenuItem(mi3);
		}

		if (gCe.utils.isNullOrEmpty(ceqb.srcFilter)) {
			showMenuItem(mi5);
			hideMenuItem(mi6);
			hideMenuItem(mi7);
		} else {
			showMenuItem(mi6);

			if (ceqb.srcFilter == pFilter) {
				hideMenuItem(mi5);
				showMenuItem(mi7);
			} else {
				hideMenuItem(mi5);
				hideMenuItem(mi7);
			}
		}
	}

	function menuLinkedFilterEnableOrDisable(pE, pMenu, pLf) {
		//TODO: Complete this
	}

	function getMenuItemByName(pMenu, pItemName) {
		var result = null;
		var domMenuItems = pMenu.getChildren();
		
		for (var thisIdx in domMenuItems) {
			var thisMi = domMenuItems[thisIdx];
			
			if (thisMi.name == pItemName) {
				result = thisMi;
			}
		}
		
		return result;
	}

	function showMenuItem(pMenuItem) {
		enableMenuItem(pMenuItem);
		pMenuItem.set('style', 'display:table-row');
	}

	function hideMenuItem(pMenuItem) {
		disableMenuItem(pMenuItem);
		pMenuItem.set('style', 'display:none');
	}

	function enableMenuItem(pMenuItem) {
		pMenuItem.set('disabled', false);
	}

	function disableMenuItem(pMenuItem) {
		pMenuItem.set('disabled', true);
	}

}
