/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

var gQuestions = new Questions();

function Questions() {
	this.cannedQuestions = populateCannnedQuestions();

	this.getCannedQuestions =function() {
		return this.cannedQuestions;
	};

	function populateCannnedQuestions() {
		var cqs = [];

		cqs.push([ '000', '(type your question here)' ]);
		cqs.push([ 'TEST_001', 'who is Fred?' ]);

		return cqs;
	}

}
