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

		cqs.push([ '001', 'list people' ]);
		cqs.push([ '002', 'list persons' ]);
		cqs.push([ '003', 'list person' ]);
		cqs.push([ '004', 'list people and lumps' ]);
		cqs.push([ '005', 'list people, lumps and patients' ]);
		cqs.push([ '006', 'who is bill' ]);
		cqs.push([ '007', 'what is jean' ]);
		cqs.push([ '008', 'who are bill and jean' ]);
		cqs.push([ '009', 'what are bill, jean and john' ]);
		cqs.push([ '010', 'who is bill and what is jean' ]);
		cqs.push([ '011', 'expand bill' ]);
		cqs.push([ '012', 'expand bill and jean' ]);
		cqs.push([ '013', 'where is Andover War Memorial Hospital' ]);
		cqs.push([ '014', 'count people' ]);
		cqs.push([ '015', 'what is person' ]);
		cqs.push([ '016', 'expand person' ]);
		cqs.push([ '017', 'list concepts' ]);
		cqs.push([ '018', 'what is Andover War Memorial Hospital picture' ]);
		cqs.push([ '019', 'what does Andover War Memorial Hospital look like' ]);
		cqs.push([ '020', 'what age is jean' ]);
		cqs.push([ '021', 'who is closely related to jean' ]);

		return cqs;
	}

}
