/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

gEp.dlg.network = new DialogNetwork();

function DialogNetwork() {

	var WIDTH = 500;
	var HEIGHT = 400;
	var DISTANCE = 150;
	var GRAVITY = .03;
	var CHARGE = -100;
	var DEFAULT_ICON = './icons/network_default.png';
	var ICON_WIDTH = 32;
	var ICON_HEIGHT = 32;
	var LABEL_OFFSET = 15;
	var LABEL_SIZE = '.35em';
	var MODE_ADD = false;
	var DEFAULT_STEPS = 3;

	var iNumSteps = DEFAULT_STEPS;
	var iD3Json = {};
	iD3Json.nodes = [];
	iD3Json.links = [];

	var iLastInstName = null;
	var iBackRefs = false;		//Whether referring instances will have their links shown
	var iCrossLinks = true;		//Whether links from entities one step away will be traversed
	var iAllPropNames = {};
	var iFilteredProperties = {};

	this.initialise = function() {
		gCe.msg.debug('DialogNetwork', 'initialise');
		
		this.actions.initialise();
		this.links.initialise();

		if (gEp.ui.isD3Loaded()) {
			iColor = d3.scale.category20();

			iFilteredProperties.is_closely_related_to = true;
			iFilteredProperties.is_the_sibling_of = true;
			iFilteredProperties.target_identity = true;
			iFilteredProperties.word_text = true;
			iFilteredProperties.previous_word = true;
		}

	};
	
	this.setBackRefs = function(pVal) {
		iBackRefs = pVal;
	};

	this.setCrossLinks = function(pVal) {
		iCrossLinks = pVal;
	};

	this.setNumSteps = function(pVal) {
		iNumSteps = pVal;
	};

	this.getInstanceDetailsForNetwork = function(pInstName) {
		if (gEp.ui.isD3Loaded()) {
			var tgtInstName = null;

			if (pInstName == null) {
				tgtInstName = iLastInstName;
			} else {
				tgtInstName = pInstName;
				iLastInstName = pInstName;
			}

			if (tgtInstName != null) {
				var cbf = function(pResponse) { gEp.dlg.network.renderInstanceInNetwork(pResponse); };
				var userParms = { steps: iNumSteps };
				gEp.handler.instances.getInstanceDetails(tgtInstName, cbf, userParms );
			}
		} else {
			gCe.msg.alert('The D3JS library is not loaded so network rendering cannot be done');
		}
	};

	this.renderInstanceInNetwork = function(pResponse) {
		successInstanceDetails(gCe.utils.getStructuredResponseFrom(pResponse));

		gEp.ui.pane.network.activateTab();
	};

	function renderDiagramUsing(pJson) {
		var domSvg = document.getElementById('svgNetwork');
		var svg = d3.select(domSvg);

		var force = setupSvgAndForce(svg, pJson);

		var link = setupLinks(svg, pJson);
		var linkText = setupLinkTexts(svg, pJson);
		var node = setupNodes(svg, pJson, force);

		setupTick(force, node, link, linkText);
	}

	function setupSvgAndForce(pSvg, pJson) {
		pSvg
			.attr('viewBox', '0 0 ' + WIDTH + ' ' + HEIGHT )
			.attr('preserveAspectRatio', 'xMidYMid meet')
			.attr('pointer-events', 'all');

		var force = d3.layout.force()
			.gravity(GRAVITY)
			.distance(DISTANCE)
			.charge(CHARGE)
			.size([WIDTH, HEIGHT])
			.nodes(pJson.nodes)
			.links(pJson.links)
			.start();
		
		return force;
	}

	function setupLinks(pSvg, pJson) {
		//Delete existing links
		pSvg.selectAll('.link').remove();
		
		//Create new links
		var link = pSvg.selectAll('.link')
					.data(pJson.links)
					.enter().append('line')
					.attr('class', 'link')
					.attr('marker-end', 'url(#triangle)')
					.attr('x1', function(d) { return d.source.x; })
					.attr('y1', function(d) { return d.source.y; })
					.attr('x2', function(d) { return d.target.x; })
					.attr('y2', function(d) { return d.target.y; })
		      .on('click', function(d, i) { leftLinkClick(d, i); })
					.style('stroke-width', function(d) { return Math.sqrt(d.value); });
		
		return link;
	}

	function setupLinkTexts(pSvg, pJson) {
		//Delete existing linkTexts
		pSvg.selectAll('.linkText').remove();
		
		//Create new linkTexts
		var linkText = pSvg.selectAll('.linkText')
		      .data(pJson.links)
		      .enter().append('text')
		      .attr('class', 'linkText')
		      .attr('text-anchor', 'middle')
		      .attr('title', function(d) { 'hello'; })
		      .attr('dx', function(d) { return calculateDxFor(d); })
		      .attr('dy', function(d) { return calculateDyFor(d); })
		      .text(function(d) { return d.text; });

		return linkText;
	}

	function setupNodes(pSvg, pJson, pForce) {
		//Delete existing nodes
		pSvg.selectAll('.node').remove();

		//Create new nodes
		var node = pSvg.selectAll('.node')
			.data(pJson.nodes)
			.enter().append('g')
			.attr('class', 'node')
			.call(pForce.drag);
		
		//Add node icon
		node.append('image')
			.attr('xlink:href', function(d) {return getIconUrl(d); })
			.attr('x', -(ICON_WIDTH / 2))
			.attr('y', -(ICON_HEIGHT / 2))
			.attr('width', ICON_WIDTH)
			.attr('height', ICON_HEIGHT)
			.on('click', function(d, i) { leftNodeClick(d, i); })
			.on('dblclick', function(d, i) { leftNodeDoubleClick(d, i); })
			.on('contextmenu', function(d, i) { rightNodeClick(d, i); });

		//Add node label
		node.append('text')
			.attr('dx', LABEL_OFFSET)
			.attr('dy', LABEL_SIZE)
			.text(function(d) { return d.name; });

		return node;
	}

	function setupTick(pForce, pNode, pLink, pLinkText) {
		pForce.on('tick', function() {
			pLink
				.attr('x1', function(d) { return d.source.x; })
				.attr('y1', function(d) { return d.source.y; })
				.attr('x2', function(d) { return d.target.x; })
				.attr('y2', function(d) { return d.target.y; });

			pLinkText
				.attr('dx', function(d) { return calculateDxFor(d); })
				.attr('dy', function(d) { return calculateDyFor(d); });

			pNode.attr('transform', function(d) { return 'translate(' + d.x + ',' + d.y + ')'; });
		});
	}

	function calculateDxFor(d) {
		var mid = (d.target.x - d.source.x) / 2;

		return d.source.x + mid;
	}

	function calculateDyFor(d) {
		var mid = (d.target.y - d.source.y) / 2;

		return d.source.y + mid;
	}

	function leftNodeClick(d, i) {
		gEp.handler.instances.getInstanceDetails(d.ce_id);
	}

	function leftNodeDoubleClick(d, i) {
		gEp.dlg.network.getInstanceDetailsForNetwork(d.ce_id);
	}

	function rightNodeClick(d, i) {
	    //handle right click

	    //stop showing browser menu
	    d3.event.preventDefault();
	    gCe.msg.alert('right click');
	}

	function leftLinkClick(d, i) {
		var propText = 'These properties are:\n';
		
		for (var i = 0; i < d.names.length; i++) {
			var thisPropName = d.names[i];
			propText += '   ' + thisPropName + '\n';
		}
			
		gCe.msg.alert(propText);
	}

	function successInstanceDetails(pInstJson) {
		if (!MODE_ADD) {
			iD3Json = {};
			iD3Json.nodes = [];
			iD3Json.links = [];

			iAllPropNames = {};
		}

		drawWithInstJson(pInstJson);
	}

	function drawWithInstJson(pInstJson) {
		var d3Json = convertToD3Json(pInstJson);

		if (MODE_ADD) {
			iD3Json = d3Json;
		}

		renderDiagramUsing(d3Json);
	}

	function convertToD3Json(pInstJson) {
		var result = iD3Json;

		gCe.msg.debug(pInstJson);
		
		if (pInstJson.main_instance != null) {
			var mainIdx = getNodeIndexForValue(result.nodes, pInstJson.main_instance._id);
			
			if (mainIdx == -1) {
				addNode(pInstJson.main_instance, result);
			}
			mainIdx = getNodeIndexForValue(result.nodes, pInstJson.main_instance._id);
		
			for (var idx in pInstJson.related_instances) {
				var relInst = pInstJson.related_instances[idx];
				
				addNode(relInst, result);
			}
			
			for (var propName in pInstJson.main_instance.property_values) {
				var propVal = pInstJson.main_instance.property_values[propName];
				gCe.msg.debug('[main] ' + propName + ' -> ' + propVal);
				
				makeLinks(result.nodes, result.links, propName, propVal, mainIdx);
			}
			
			for (var i in pInstJson.related_instances) {
				var relInst = pInstJson.related_instances[i];
				var srcIdx = getNodeIndexForValue(result.nodes, relInst._id);
		
				gCe.msg.debug('[rel x] ' + relInst._id + ' -> ' + srcIdx);
				if (srcIdx == -1) {
					addNode(relInst, result);
				}
				srcIdx = getNodeIndexForValue(result.nodes, relInst._id);
				
				if (iCrossLinks) {
					for (var propName in relInst.property_values) {
						var propVal = relInst.property_values[propName];
						
						gCe.msg.debug('[rel] ' + propName + ' -> ' + propVal);
						makeLinks(result.nodes, result.links, propName, propVal, srcIdx);
					}
				}
			}

			if (iBackRefs) {
				for (var propName in pInstJson.referring_instances) {
					var refInsts = pInstJson.referring_instances[propName];
					
					for (var i in refInsts) {
						var refInst = refInsts[i];
						var srcIdx = getNodeIndexForValue(result.nodes, refInst._id);
				
						gCe.msg.debug('[ref x] ' + refInst._id + ' -> ' + srcIdx);
						if (srcIdx == -1) {
							addNode(refInst, result);
						}
						srcIdx = getNodeIndexForValue(result.nodes, refInst._id);
						
						for (var propName in refInst.property_values) {
							var propVal = refInst.property_values[propName];
							
							gCe.msg.debug('[ref] ' + propName + ' -> ' + propVal);
							makeLinks(result.nodes, result.links, propName, propVal, srcIdx);
						}
					}			
				}
			}
		} else {
			gCe.msg.alert('Nothing was returned from the server');
		}

		gEp.dlg.network.showDebugValues();
		
		return result;
	}

	this.showDebugValues = function(pForce) {
		if (gCe.isDebug() || pForce) {
			gCe.msg.debug('Variables:');
			gCe.msg.debug(' numSteps=' + iNumSteps);
			gCe.msg.debug(' backRefs=' + iBackRefs);
			gCe.msg.debug(' crossLinks=' + iCrossLinks);

			gCe.msg.debug('Nodes:');
			for ( var i in iD3Json.nodes) {
				var thisNode = iD3Json.nodes[i];
				gCe.msg.debug(thisNode);
			}

			gCe.msg.debug('Links:');
			for ( var i in iD3Json.links) {
				var thisLink = iD3Json.links[i];
				gCe.msg.debug(thisLink);
			}
			
			gCe.msg.debug('All properties:');
			gCe.msg.debug(iAllPropNames);
		}
	};

	function makeLinks(pNodes, pLinks, pPropName, pTgtNameOrNames, pSrcIdx) {
		gCe.msg.debug('makeLinks: ' + pPropName + ', ' + pTgtNameOrNames + ', ' + pSrcIdx);
		if (gCe.utils.isArray(pNodes)) {
			for (var idx in pTgtNameOrNames) {
				var thisName = pTgtNameOrNames[idx];
				
				var tgtIndex = getNodeIndexForValue(pNodes, thisName);

				if (tgtIndex != -1) {
					addLink(pLinks, pSrcIdx, tgtIndex, 1, pPropName);
				} else {
					gCe.msg.debug('[b] Cannot find node: ' + thisName);
				}
			}
		} else {
			var tgtIndex = getNodeIndexForValue(pNodes, pTgtNameOrNames);

			if (tgtIndex != -1) {
				addLink(pLinks, pSrcIdx, tgtIndex, 1, pPropName);
			} else {
				gCe.msg.debug('[c] Cannot find node: ' + pTgtNameOrNames);
			}
		}	
	}

	function addLink(pLinks, pSrcIdx, pTgtIdx, pVal, pPropName) {
	gCe.msg.debug('addLink: ' + pSrcIdx + ', ' + pTgtIdx + ', ' + pPropName + '=' + pVal);
		if (iFilteredProperties[pPropName] != true) {
			var foundExisting = false;
			
			for (var idx in pLinks) {
				var thisLink = pLinks[idx];
				
				if (((thisLink.source == pSrcIdx) || (thisLink.source == pTgtIdx)) && ((thisLink.target == pTgtIdx) || (thisLink.target == pSrcIdx))) {
					foundExisting = true;
					if (!gCe.utils.arrayContainsValue(thisLink.names, pPropName)) {
						//There is an existing link, so update it
						thisLink.names.push(pPropName);
						thisLink.value++;
						thisLink.text = '(' + thisLink.value + ' properties)';
//						thisLink.text += '<tspan>' + cePropName + '</tspan>';
						
						if (iAllPropNames[pPropName] == null) {
							iAllPropNames[pPropName] = 1;
						} else {
							++iAllPropNames[pPropName];
						}					
					}
				}
			}	
			
			if (!foundExisting) {
				//There is no existing link so just add a new one
				var newLink = {};
				newLink.source = pSrcIdx;
				newLink.target = pTgtIdx;
				newLink.value = pVal;
				newLink.text = pPropName;
				newLink.names = [ pPropName ];

				if (iAllPropNames[pPropName] == null) {
					iAllPropNames[pPropName] = 1;
				} else {
					++iAllPropNames[pPropName];
				}

				pLinks.push(newLink);
			}
		}
	}

	function getNodeIndexForValue(pNodes, pTgtName) {
		var result = -1;
		
		for (var i = 0; i < pNodes.length; i++) {
			var thisNode = pNodes[i];
			
			if (thisNode.ce_id == pTgtName) {
				result = i;
				//TODO: Add a break here
			}
		}

		return result;
	}

	function getIconUrl(pNode) {
		if (pNode.icon == null) {
			return DEFAULT_ICON;
		} else {
			return pNode.icon;
		}
	}

	function addNode(pInst, pResult) {
		var currIdx = getNodeIndexForValue(pResult.nodes, pInst._id);
		
		if (currIdx == -1) {
			var node = {};
			node.ce_id = pInst._id;
			node.name = pInst._label;
			node.group = 1;

			node.icon = pInst.icon;
			
			gCe.msg.debug('Adding node:');
			gCe.msg.debug(node);
			
			pResult.nodes.push(node);
		} else {
			gCe.msg.debug('Ignoring node (already present):' + pInst._id);
		}
	}

	function isConceptNamed(pInst, pConName) {
		if (pInst != null) {
			var allCons = pInst.direct_concept_names.concat(pInst.inherited_concept_names);

			for (var i in allCons) {
				var conName = allCons[i];
				
				if (conName == pConName) {
					return true;
				}
			}
		}
		
		return false;
	}

	function failureInstanceDetails(pStatus, pInstName) {
		gCe.msg.alert('Instance details failure');
		gCe.msg.debug(pStatus);
		gCe.msg.debug(pInstName);
	}

}
