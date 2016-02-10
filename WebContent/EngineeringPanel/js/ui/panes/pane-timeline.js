/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

gEp.ui.pane.timeline = new PaneTimeline();

function PaneTimeline() {
	var iDomParentName = 'mainContainer';
	var iDomName = 'timelinePane';
	var iTitle = 'Timeline';
	var iTabPos = 75;
	
	this.lastRequest = null;

	this.initialise = function() {
		gCe.msg.debug(iDomName, 'initialise');

		gEp.ui.registerTab(iDomName, iDomParentName, iTabPos, iTitle, initialHtml());
		gEp.ui.registerTabSwitchFunction(iDomParentName, iDomName, tabSwitchFunction);
	};

	this.resetContents = function() {
		gCe.msg.debug(iDomName, 'resetContents');

		this.updateWith(initialHtml());
//		generateTimeline();
	};
	
	this.generateTimeline = function() {
		var margin = {top: 20, right: 20, bottom: 200, left: 40},
	    width = 960 - margin.left - margin.right,
	    height = 500 - margin.top - margin.bottom;

		var x = d3.scale.ordinal()
		    .rangeRoundBands([0, width], .1);
	
		var y = d3.scale.linear()
		    .range([height, 0]);
	
		var xAxis = d3.svg.axis()
		    .scale(x)
		    .orient("bottom");
	
		var yAxis = d3.svg.axis()
		    .scale(y)
		    .orient("left");
	
		d3.select("svg").remove();
		
		var svg = d3.select("#timeline")
		  .append("svg")
		    .attr("width", width + margin.left + margin.right)
		    .attr("height", height + margin.top + margin.bottom)
		  .append("g")
		    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

		var concept = document.getElementById("concept").value;
		var timeProperty = document.getElementById("timeProperty").value;
		var buckets = parseInt(document.getElementById("buckets").value);
		
		var url = "http://localhost:8080/CeStore/concepts/" + concept + "/instances/frequency";
		
		if (timeProperty) {
			url += "?property=" + timeProperty;
			
			if (buckets) {
				url += "&buckets=" + buckets;
			}
		} else if (buckets) {
			url += "?buckets=" + buckets;
		}
		
		
		d3.json(url, function(data) {
			if (data) {
				x.domain(data.map(function(d) { return d.range; }));
				y.domain([0, d3.max(data, function(d) { return d.frequency; })]);
		
				svg.append("g")
				    .attr("class", "x axis")
				    .attr("transform", "translate(0," + height + ")")
				    .call(xAxis)
				  .selectAll("text")
				    .attr("y", 0)
				    .attr("x", 9)
				    .attr("dy", ".35em")
				    .attr("transform", "rotate(90)")
				    .style("text-anchor", "start");
		
				svg.append("g")
				    .attr("class", "y axis")
				    .call(yAxis)
				  .append("text")
				    .attr("transform", "rotate(-90)")
				    .attr("y", 6)
				    .attr("dy", ".71em")
				    .style("text-anchor", "end")
				    .text("Frequency");
		
				svg.selectAll(".bar")
				    .data(data)
				  .enter().append("rect")
				    .attr("class", "bar")
				    .attr("x", function(d) { return x(d.range); })
				    .attr("width", x.rangeBand())
				    .attr("y", function(d) { return y(d.frequency); })
				    .attr("height", function(d) { return height - y(d.frequency); });
			}
		});
	
		function type(d) {
			d.frequency = +d.frequency;
			return d;
		}
	}

	function initialHtml() {
		var html = "";

		html += "<label>Concept:</label> <input name='concept' placeholder='Concept' type='text' id='concept' /> ";
		html += "<label>Time Property:</label> <input name='timeProperty' placeholder='Time Property' type='text' id='timeProperty' /> ";
		html += "<label>Buckets:</label> <input name='buckets' placeholder='Buckets' type='text' id='buckets' /> ";
		html += "<button onclick='gEp.ui.pane.timeline.generateTimeline()'>Create Timeline</button>";
		html += "<div id='timeline'></div>";

		return html;
	}
	
	function tabSwitchFunction() {
		registerDropEvents();
	}
	
	function registerDropEvents() {
		document.getElementById('timeline').addEventListener('dragover', function(e) {
		    e.preventDefault();
			e.stopPropagation();
			e.dataTransfer.dropEffect = 'copy';
		}, false);
		document.getElementById('timeline').addEventListener('drop', function(e) {
			e.preventDefault();
			e.stopPropagation();
			
			var rawTgtObj = e.dataTransfer.getData(gCe.FORMAT_JSON);
			
			if (!gCe.utils.isNullOrEmpty(e)) {
				//There is an object
				var tgtObj = JSON.parse(rawTgtObj);
				handleDroppedObject(tgtObj);
			} else {
				gCe.msg.warning('Nothing detected in drop', 'doDropHandling', [e]);
			}
		}, false);
	}
	
	function handleDroppedObject(tgtObj) {
		if (tgtObj.type === 'concept') {
			document.getElementById("concept").value = tgtObj.conceptName;
			gEp.ui.pane.timeline.generateTimeline();
		} else if (tgtObj.type === 'property') {
			document.getElementById("timeProperty").value = tgtObj.propertyName;
			gEp.ui.pane.timeline.generateTimeline();
		} else {
			gCe.msg.warning('Ignored dropped item', 'handleDroppedObject', [tgtObj]);
		}
	}

	this.updateWith = function(pHtml, pShow, pTitle) {
		if (pShow) {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName, iDomParentName);
		} else {
			gEp.ui.updatePaneWith(pHtml, pTitle, iDomName);
		}
	};

	this.updateAndParseWith = function(pHtml, pShow, pTitle) {
		if (pShow) {
			gEp.ui.updateAndParsePaneWith(pHtml, pTitle, iDomName, iDomParentName);
		} else {
			gEp.ui.updateAndParsePaneWith(pHtml, pTitle, iDomName);
		}
	};

	this.activateTab = function() {
		gEp.ui.activateTab(iDomName, iDomParentName);
	};

}