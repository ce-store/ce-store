package com.ibm.ets.ita.ce.store.names;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

public class CeNames {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	//Concepts: Meta model concept names
	public static final String CON_CON = "concept";
	public static final String CON_ENTCON = "entity concept";
	public static final String CON_PROPCON = "property concept";
	public static final String CON_ATTCON = "attribute concept";
	public static final String CON_RELCON = "relation concept";
	public static final String CON_CONMOD = "conceptual model";
	public static final String CON_DTPROP = "datatype property";
	public static final String CON_OBPROP = "object property";

	//Concepts: Other concept names
	public static final String CON_THING = "thing";
	public static final String CON_IDPROPCON = "identification property concept";
	public static final String CON_SEPIDCON = "separately identified concept";
	public static final String CON_SPATIAL = "spatial thing";
	public static final String CON_SEQUENCE = "sequence";
	public static final String CON_CTE= "CE triggered event";
	public static final String CON_SVP = "single value property";
	public static final String CON_INT = "integer";

	//Concepts: Conversation
	public static final String CON_CARD = "card";
	public static final String CON_TELLCARD = "tell card";
	public static final String CON_NLCARD = "NL card";
	public static final String CON_CONFCARD = "confirm card";
	public static final String CON_EXPANDCARD = "expand card";
	public static final String CON_WHYCARD = "why card";
	public static final String CON_GISTCARD = "gist card";
	public static final String CON_GISTCONFCARD = "gist-confirm card";
	public static final String CON_SERVICE = "service";
	public static final String CON_CEUSER = "CE user";
	public static final String CON_USER = "user";
	public static final String CON_AUTHUSER = "authorised user";
	public static final String CON_CONVTHING = "conv thing";
	public static final String CON_INTERESTING = "interesting thing";
	public static final String CON_PERSON = "person";

	//Concepts: Hudson
	public static final String CON_QPHRASE = "question phrase";
	public static final String CON_QWORD = "question word";
	public static final String CON_COMWORD = "common word";
	public static final String CON_CONNWORD = "connector word";
	public static final String CON_MODIFIER = "modifier";
	public static final String CON_QUAL = "qualifier";
	public static final String CON_SRCHMOD = "search modifier";
	public static final String CON_FILTMOD = "filter modifier";
	public static final String CON_FUNCMOD = "function modifier";
	public static final String CON_ENDMOD = "end modifier";
	public static final String CON_CEMOD = "CE modifier";
	public static final String CON_CC = "conversation config";
	public static final String CON_SUPPCON = "suppressed concept";
	public static final String CON_CONFCON = "configuration concept";
	public static final String CON_UNINTCON = "uninteresting concept";
	public static final String CON_SOURCE = "source";
	public static final String CON_LOCATION = "location";
	public static final String CON_MEDIA = "media";
	public static final String CON_NUMWORD = "number word";
	public static final String CON_LINKEDPROP = "linked property";	
	public static final String CON_MULTIMATCH = "multimatch thing";
	public static final String CON_CONVPHRASE = "conv phrase";
	public static final String CON_CONVCLAUSE = "conv clause";
	public static final String CON_CONVSEN = "conv sentence";
	public static final String CON_CONVWORD = "conv word";
	public static final String[] CONLIST_HUDSON = { CON_QPHRASE, CON_QWORD, CON_COMWORD, CON_MODIFIER, CON_QUAL, CON_CONNWORD };

	//Properties: general property names
	public static final String PROP_LABPN = "label property name";
	public static final String PROP_ICOPN = "icon property name";
	public static final String PROP_ICOFN = "icon file name";
	public static final String PROP_PROPNAME = "property name";
	public static final String PROP_DESC = "description";

	//Properties: CE agent property names
	public static final String PROP_CLASSNAME = "class name";
	public static final String PROP_DOESNOTGENERATECE = "does not generate CE";
	public static final String PROP_SENDTOSTORE = "send CE to store";
	public static final String PROP_SAVETOFILE = "save CE to file";
	public static final String PROP_RESTATE = "restate existing sentences";
	public static final String PROP_SAVEINDIVIDUAL = "save CE individually";
	public static final String PROP_CEFILENAME = "CE filename";
	public static final String PROP_MAXSENS = "maximum CE sentences";
	public static final String PROP_MAXITS = "maximum iterations";
	public static final String PROP_SRC_CONCEPT = "source concept";
	public static final String PROP_SRC_PROP = "source property";
	public static final String PROP_SRC_RANGE = "source range";
	public static final String PROP_TGT_CONCEPT = "target concept";
	public static final String PROP_TGT_PROP = "target property";	
	public static final String PROP_TGT_RANGE = "target range";
	public static final String PROP_TGT_SOURCE = "target source";
	public static final String PROP_GENRAT = "generate rationale";
	public static final String PROP_DUBRAT = "double rationale sentences";
	public static final String PROP_DEBUG = "debug";

	//Properties: Rule executor properties
	public static final String PROP_ITER = "iterate";
	public static final String PROP_RULENAME = "rule name";

	//Properties: Duration calculator properties
	public static final String PROP_DATAFMT = "data format";
	public static final String PROP_YEAR = "year";
	public static final String PROP_MONTH = "month";
	public static final String PROP_DAY = "day";
	public static final String PROP_STARTPROP = "start property";
	public static final String PROP_ENDPROP = "end property";
	public static final String PROP_DURPROP = "duration property";
	public static final String PROP_UNENDDURPROP = "unended duration property";
	public static final String PROP_CURRPROP = "current property";
	public static final String PROP_GENCURR = "generate current";
	public static final String PROP_CURRTIMEOV = "current time override";

	//Properties: Conversation
	public static final String PROP_ISTO = "is to";
	public static final String PROP_ISINREPLYTO = "is in reply to";
	public static final String PROP_ISFROM = "is from";
	public static final String PROP_FROMCONCONCEPT = "from concept";
	public static final String PROP_FROMINSTANCE = "from instance";
	public static final String PROP_MILLISECONDSDELAY = "milliseconds delay";
	public static final String PROP_AFFILIATION = "affiliation";
	public static final String PROP_CHECKNATIONALITIES = "check nationalities";
	public static final String PROP_CHECKAUTHORISEDUSERS = "check authorised users";
	public static final String PROP_RUNRULESONSAVE = "run rules on save";
	public static final String PROP_SUBJECT = "subject";
	public static final String PROP_PREDICATE = "predicate";
	public static final String PROP_ISABOUT = "is about";
	public static final String PROP_IGNORECON = "ignore concept name";
	public static final String PROP_TOCON = "to concept";
	public static final String PROP_TOINST = "to instance";
	public static final String PROP_CONTENT = "content";
	public static final String PROP_SECCON = "secondary content";
	public static final String PROP_CANCONF = "can confirm";
	public static final String PROP_CANASK = "can ask";
	public static final String PROP_CANTELL = "can tell";
	public static final String PROP_CANWHY = "can why";
	public static final String PROP_CANEXP = "can expand";

	//Properties: Conversation config
	public static final String PROP_SPLITPHRASES = "split phrases";
	public static final String PROP_RUNRULES = "run rules";
	public static final String PROP_SINGANS = "single answers";
	public static final String PROP_PHRASEDELIMS = "phrase delimiters";
	public static final String PROP_SENDELIMS = "sentence delimiters";
	public static final String PROP_CLAUSEDELIMS = "clause delimiters";
	public static final String PROP_CLAUSEPUNCS = "clause punctuation";
	public static final String PROP_QSMS = "question start markers";
	public static final String PROP_QEMS = "question end markers";
	public static final String PROP_COMWORDS = "common words";
	public static final String PROP_NEGWORDS = "negation words";
	public static final String PROP_MARKER = "marker";
	public static final String PROP_MAXSUGGS = "max suggestions";
	public static final String PROP_MAXRESULTS = "max answer result rows";
	public static final String PROP_MAXDBROWS = "max database to ce rows";
	public static final String PROP_DEFANSCONF = "default answer confidence";
	public static final String PROP_DEFINTCONF = "default interpretation confidence";
	public static final String PROP_DEFABANSCONF = "default ability to answer confidence";
	public static final String PROP_COMPANSCONF = "compute answer confidence";
	public static final String PROP_COMPINTCONF = "compute interpretation confidence";
	public static final String PROP_COMPABANSCONF = "compute ability to answer confidence";

	//Properties: Hudson
	public static final String PROP_LAT = "latitude";
	public static final String PROP_LON = "longitude";
	public static final String PROP_URL = "url";
	public static final String PROP_CREDIT = "credit";
	public static final String PROP_CORRTO = "corresponds to";
	public static final String PROP_MAPSTO = "maps to";
	public static final String PROP_ISEXPBY = "is expressed by";
	public static final String PROP_PASTTENSE = "past tense";
	public static final String PROP_PLURAL = "plural form";
	public static final String[] PROPS_LING = {PROP_ISEXPBY, PROP_PLURAL, PROP_PASTTENSE};

	//Values: Hudson
	public static final String MOD_EXPAND = "general:expand";
	public static final String MOD_LINKSFROM = "general:linksfrom";
	public static final String MOD_LINKSTO = "general:linksto";
	public static final String MOD_LINKSBET = "general:linksbetween";
	public static final String MOD_LOCATE = "general:locate";
	public static final String MOD_COUNT = "general:count";
	public static final String MOD_LIST = "general:list";
	public static final String MOD_SHOW = "general:show";
	public static final String ABS_ASC = "a:ascending";
	public static final String ABS_DESC = "a:descending";
	public static final String ABS_MERGE = "a:merge";

	//Properties: "Special" property names (operators applied to values)
	public static final String SPECIALNAME_CONTAINS = "contains";
	public static final String SPECIALNAME_NOTCONTAINS = "not-contains";
	public static final String SPECIALNAME_MATCHES = "matches";
	public static final String SPECIALNAME_STARTSWITH = "starts-with";
	public static final String SPECIALNAME_NOTSTARTSWITH = "not-starts-with";
	public static final String SPECIALNAME_ENDSWITH = "ends-with";
	public static final String SPECIALNAME_EQUALS = "=";
	public static final String SPECIALNAME_NOTEQUALS = "!=";
	public static final String SPECIALNAME_GREATER = ">";
	public static final String SPECIALNAME_LESS = "<";
	public static final String SPECIALNAME_GREATEROREQUAL = ">=";
	public static final String SPECIALNAME_LESSOREQUAL = "<=";

	//Ranges
	public static final String RANGE_VALUE = "value";
	public static final String RANGE_INSTANCE = "instance";

	//Sources
	public static final String SRC_HUDSON = "HUDSON";
	public static final String SRC_INF = "conv_inf";
	public static final String SRC_CONV_PREFIX = "conv_";

}
