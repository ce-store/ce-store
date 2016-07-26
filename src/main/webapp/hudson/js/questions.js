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

		cqs.push([ 'ITA_001', 'list people' ]);
		cqs.push([ 'ITA_002', 'who is Dave Braines' ]);
		cqs.push([ 'ITA_003', 'who is Dave Braines and Mark Nixon' ]);
		cqs.push([ 'ITA_004', 'who are Mark Nixon co-author' ]);
		cqs.push([ 'ITA_005', 'what is the abstract Formal Properties of Distributed Database Networks' ]);

		// Anna's test questions for ITA Papers Demo

		// Redirect questions
		cqs.push(['ITA_006', 'What is Verifiable Delegation of Computation over Large Datasets']); // paper
		cqs.push(['ITA_007', 'Who is Don Towsley']); // author
		cqs.push(['ITA_008', 'Show Fusion']); // venue
		cqs.push(['ITA_009', 'View IBM-UK']); // organisation
		cqs.push(['ITA_010', 'Go to Efficient Security Techniques for Information Flows in Coalition Environments']); // project

		// Highlight questions
		// Paper
		cqs.push(['ITA_011', 'Who wrote Verifiable Delegation of Computation over Large Datasets']); // authors
		cqs.push(['ITA_012', 'When was Verifiable Delegation of Computation over Large Datasets published']); // date
		cqs.push(['ITA_013', 'What is the abstract for Verifiable Delegation of Computation over Large Datasets']); // abstract
		cqs.push(['ITA_014', 'How many citations does Verifiable Delegation of Computation over Large Datasets have']); // citation
		cqs.push(['ITA_015', 'Where was Verifiable Delegation of Computation over Large Datasets published']); // venue
		cqs.push(['ITA_016', 'Does Verifiable Delegation of Computation over Large Datasets have any variants']); // variants
		cqs.push(['ITA_017', 'Can I download Verifiable Delegation of Computation over Large Datasets']); // download
		cqs.push(['ITA_018', 'What project was Verifiable Delegation of Computation over Large Datasets in']); // project

		// Author
		cqs.push(['ITA_019', 'Where was Don Towsley employed']); // employer
		cqs.push(['ITA_020', 'Who collaborated with Don Towsley']); // collaboration graph
		cqs.push(['ITA_021', 'What papers did Don Towsley write']); // papers list
		cqs.push(['ITA_022', 'List Don Towsley\'s co-workers']); // co-workers list
		cqs.push(['ITA_023', 'How many publications did Don Towsley have']); // total publications
		cqs.push(['ITA_024', 'How many technical reports does Don Towsley have']); // technical papers
		cqs.push(['ITA_025', 'How many full collaboration papers does Don Towsley have']); // full collaboration
		cqs.push(['ITA_026', 'How many internal papers does Don Towsley have']); // internal papers
		cqs.push(['ITA_027', 'How many external papers has Don Towsley written']); // external papers
		cqs.push(['ITA_028', 'How many journal papers has Don Towsley written']); // journal papers
		cqs.push(['ITA_029', 'What is Don Towsley\'s citation count']); // citation count
		cqs.push(['ITA_030', 'What is Don Towsley\'s h-index']); // h-index

		// Venue
		cqs.push(['ITA_031', 'How many years has Fusion ran for']); // years
		cqs.push(['ITA_032', 'What papers were published at Fusion 2013']); // papers
		cqs.push(['ITA_033', 'What organisations published at Fusion 2009']); // organisations
		cqs.push(['ITA_034', 'What authors wrote papers for Fusion 2012']); // authors
		cqs.push(['ITA_038', 'Where was Fusion 2012 held']); // map

		// Organisation
		cqs.push(['ITA_039', 'Who works for University of Aberdeen']); // employees
		cqs.push(['ITA_040', 'What industry is the University of Aberdeen']); // industry
		cqs.push(['ITA_041', 'Where is University of Aberdeen']);

		// Project
		cqs.push(['ITA_042', 'What technical area is Efficient Security Techniques for Information Flows in Coalition Environments in']); // technical area
		cqs.push(['ITA_043', 'What papers are in Efficient Security Techniques for Information Flows in Coalition Environments']); // publications

		// List/result questions
		cqs.push(['ITA_035', 'How many papers were published at Fusion']);
		cqs.push(['ITA_036', 'How many organisations participated in Fusion']);
		cqs.push(['ITA_037', 'How many authors were there at Fusion 2008']);
		cqs.push(['ITA_044', 'How many papers in Efficient Security Techniques for Information Flows in Coalition Environments']);
		cqs.push(['ITA_045', 'List the papers Don Towsley has worked on with Zubair Shafiq']);
		cqs.push(['ITA_046', 'List Don Towsley\'s technical reports']);
		cqs.push(['ITA_047', 'List papers that have come out of IBM UK']);
		cqs.push(['ITA_048', 'List all papers from Fusion']);
		cqs.push(['ITA_049', 'List authors with citation count over 1000']);

		// Extra tests from Dave
		cqs.push(['ITA_050', 'what is On Structuring Controlled English Documents' ]);

		// Dave's test questions, designed specifically for HUDSON
		cqs.push([ 'DB_001', 'what is underspin?' ]);
		cqs.push([ 'DB_002', 'what is love?' ]);
		cqs.push([ 'DB_003', 'how many matches?' ]);
		cqs.push([ 'DB_004', 'how many players?' ]);
		cqs.push([ 'DB_005', 'how many Belgian players are there?' ]);
		cqs.push([ 'DB_006', 'how many British players in last 16 in 2014' ]);
		cqs.push([ 'DB_007', 'what British players in last 16 in 2014' ]);
		cqs.push([ 'DB_008', 'how many matches were there in 2012?' ]);
		cqs.push([ 'DB_009', 'how many matches were there last year?' ]);
		cqs.push([ 'DB_010', 'how many matches have there been this year?' ]);
		cqs.push([ 'DB_011', 'how many mens single\'s matches were there in 2012?' ]);
		cqs.push([ 'DB_012', 'how many men\'s singles\' semi-final matches were there in 2010?' ]);
		cqs.push([ 'DB_013', 'how many british players in last four in 2014' ]);
		cqs.push([ 'DB_014', 'how many british players in third-round in 2008' ]);
		cqs.push([ 'DB_015', 'what french players in third round in 2008' ]);
		cqs.push([ 'DB_016', 'where is centre court?' ]);
		cqs.push([ 'DB_017', 'what is centre court?' ]);
		cqs.push([ 'DB_018', 'what is Henman Hill?' ]);
		cqs.push([ 'DB_019', 'how many aces from deuce by year?' ]);
		cqs.push([ 'DB_020', 'how many aces from advantage by year?' ]);
		cqs.push([ 'DB_021', 'how many players by nationality in size order' ]);
		cqs.push([ 'DB_022', 'what does Roger Federer look like?' ]);
		cqs.push([ 'DB_023', 'what does Ada Bakker look like?' ]);
		cqs.push([ 'DB_024', 'what do Roger Federer and Andy Roddick look like?']);
		cqs.push([ 'DB_025', 'what is centre court and henman hill?' ]);
		cqs.push([ 'DB_026', 'what British players won in last 16 in 2014?' ]);
		cqs.push([ 'DB_027', 'what British players lost in mens quarter finals in 2014?' ]);
		cqs.push([ 'DB_028', 'who won the mens singles wimbledon final 3 years ago?' ]);
		cqs.push([ 'DB_029', 'fastest 1st serve last year by Swiss player?' ]);
		cqs.push([ 'DB_030', 'fastest serve by a Brit?' ]);
		cqs.push([ 'DB_031', 'fastest second serve by a Norwegian?' ]);
		cqs.push([ 'DB_032', 'how many british players through to mens singles quarter finals in 2012, 2013 and 2014']);
		cqs.push([ 'DB_033', 'what american players through to final 16 in ladies singles in 2012, 2013 and 2014']);
		cqs.push([ 'DB_034', 'how many American players have won the mens singles final between 1995 and 2013']);
		cqs.push([ 'DB_035', 'what British players have won the mens singles final between 2015 and 1995']);
		cqs.push([ 'DB_036', 'how many players by nationality']);
		cqs.push([ 'DB_037', 'what players by nationality']);
		cqs.push([ 'DB_038', 'list players by nationality']);
		cqs.push([ 'DB_039', 'all players by nationality']);
		cqs.push([ 'DB_040', 'what is Wimbledon?']);
		cqs.push([ 'DB_041', 'Who won Wimbledon in 2013?']);
		cqs.push([ 'DB_042', 'Who won the men\'s singles in 2012?']);
		cqs.push([ 'DB_043', 'who is Cheryl Cole?']);
		cqs.push([ 'DB_044', 'Who won mens singles\' each year']);
		cqs.push([ 'DB_045', 'Who won mens singles\' by year']);
		cqs.push([ 'DB_046', 'Hello']);
		cqs.push([ 'DB_047', 'Hi']);
		cqs.push([ 'DB_048', 'Hey']);
		cqs.push([ 'DB_049', 'who will win tomorrow?']);
		cqs.push([ 'DB_050', 'why didn\'t Pete Sampras keep winning?']);
		cqs.push([ 'DB_051', 'how many British players played in Third Round and played in 2014']);
		cqs.push([ 'DB_052', 'how many matches were played in 2012?']);
		cqs.push([ 'DB_053', 'Where is Tim Henman?']);
		cqs.push([ 'DB_054', 'Who won wimbledon?']);
		cqs.push([ 'DB_055', 'Who won Wimbledon mens singles by year in year order?']);

		cqs.push([ 'WL_001', 'starts X CARGO', 5000]);
		cqs.push([ 'WL_002', 'officers for X CARGO LIMITED']);
		cqs.push([ 'WL_003', 'address for X CARGO LIMITED']);
		cqs.push([ 'WL_004', 'number of outstanding mortgage charges for X CARGO LIMITED']);
		cqs.push([ 'WL_005', 'current officers for X CARGO LIMITED']);
		cqs.push([ 'WL_006', 'resigned officers for X CARGO LIMITED']);
		cqs.push([ 'WL_007', 'directors for X CARGO LIMITED']);
		cqs.push([ 'WL_008', 'secretarys for X CARGO LIMITED']);
		cqs.push([ 'WL_009', 'secretaries for X CARGO LIMITED']);
		cqs.push([ 'WL_010', 'get X LIMITED', 5000]);
		cqs.push([ 'WL_011', 'officers for X LIMITED']);
		cqs.push([ 'WL_012', 'get ? LTD', 5000]);
		cqs.push([ 'WL_013', 'officers for ? LTD']);
		cqs.push([ 'WL_014', 'company name starts WHICH?', 5000]);
		cqs.push([ 'WL_015', 'officers for WHICH? LIMITED and WHICH? INTERNATIONAL LIMITED']);
		cqs.push([ 'WL_016', 'what is X CARGO LIMITED\'s registered office address']);
		cqs.push([ 'WL_017', 'postcode starts SO21 2JN']);
		cqs.push([ 'WL_018', 'company number like 233233']);
		cqs.push([ 'WL_019', 'same address as X CARGO LIMITED']);
		cqs.push([ 'WL_020', 'What is X Limited\'s registered office address']);
		cqs.push([ 'WL_021', 'What companies have the same registered office address as X CARGO LIMITED']);
		cqs.push([ 'WL_022', 'Who are the directors of X Limited']);
		cqs.push([ 'WL_023', 'What is the address of SUMNER, Bernard']);
		cqs.push([ 'WL_024', 'What are the addresses for the directors of X Limited']);
		cqs.push([ 'WL_025', 'When have directors of X CARGO LIMITED resigned']);
		cqs.push([ 'WL_026', 'How many of X Limited\'s directors are 50 years of age or older']);
		cqs.push([ 'WL_027', 'How many of X Limited\'s directors are younger than 50']);
		cqs.push([ 'WL_028', 'What is the addresses of the secretary of X Limited']);
		cqs.push([ 'WL_029', 'Has the secretary of X Limited resigned and if so on which date']);
		cqs.push([ 'WL_030', 'What is X Limited\'s principal business activity']);
		cqs.push([ 'WL_031', 'Has X Limited changed (or proposed to change) its registered name']);
		cqs.push([ 'WL_032', 'Are there mortgages or charges registered against X Limited']);
		cqs.push([ 'WL_033', 'Is X Limited an investment company']);
		cqs.push([ 'WL_034', 'get The Spud Fund', 5000]);
		cqs.push([ 'WL_035', 'Is The Spud Fund an investment company']);
		cqs.push([ 'WL_036', 'Has a receiver or manager been appointed to X Limited']);
		cqs.push([ 'WL_037', 'Has a receiver or manager been appointed to X CARGO Limited']);
		cqs.push([ 'WL_038', 'Is X Limited subject to a corporate voluntary arrangement']);
		cqs.push([ 'WL_039', 'get RCS PLC', 5000]);
		cqs.push([ 'WL_040', 'Is RCS PLC Limited subject to a corporate voluntary arrangement']);
		cqs.push([ 'WL_041', 'Is RCS PLC Limited under CVA']);
		cqs.push([ 'WL_042', 'Is X Limited in administration']);
		cqs.push([ 'WL_043', 'get VORIO LIMITED', 5000]);
		cqs.push([ 'WL_044', 'Is VORIO LIMITED Limited in administration']);
		cqs.push([ 'WL_045', 'Is X Limited in liquidation']);
		cqs.push([ 'WL_046', 'Is X CARGO Limited in liquidation']);
		cqs.push([ 'WL_047', 'What is X Limited\'s accounting reference date']);
		cqs.push([ 'WL_048', 'get Dodd group limited', 5000]);
		cqs.push([ 'WL_049', 'where is x limited']);
		cqs.push([ 'WL_050', 'where is x limited and x cargo limited']);
		cqs.push([ 'WL_051', 'same postcode as X CARGO LIMITED']);
		cqs.push([ 'WL_052', 'What is X Limited\'s Registered Office Address']);
		cqs.push([ 'WL_053', 'get ZERVO PLC', 5000]);
		cqs.push([ 'WL_054', 'get SHIRO HOUSING CO-OPERATION LIMITED', 5000]);
		cqs.push([ 'WL_055', 'get DEF INVESTMENTS LIMITED', 5000]);
		cqs.push([ 'WL_056', 'officers for DEF INVESTMENTS LIMITED']);
		cqs.push([ 'WL_057', 'get STARTCO LIMITED', 5000]);
		cqs.push([ 'WL_058', 'get NEWCO LIMITED', 5000]);
		cqs.push([ 'WL_059', 'officers for NEWCO LIMITED and STARTCO LIMITED', 5000]);
		cqs.push([ 'WL_060', 'what is STARTCO LIMITED']);
		cqs.push([ 'WL_061', 'insolvency cases x cargo limited', 5000]);
		cqs.push([ 'WL_062', 'what is x cargo limited']);
		cqs.push([ 'WL_063', 'expand X CARGO LIMITED']);
		cqs.push([ 'WL_064', 'expand STARTCO LIMITED']);
		cqs.push([ 'WL_065', 'expand SUMNER, Bernard']);
		cqs.push([ 'WL_066', 'expand COX, Amanda']);
		cqs.push([ 'WL_067', 'starts WRAGGE']);
		cqs.push([ 'WL_068', 'directors for WRAGGE & CO LIMITED']);
		cqs.push([ 'WL_069', 'Has HENWOOD, Julian Richard resigned']);

		return cqs;
	}

}
