package com.ibm.ets.ita.ce.store.names;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class MiscNames {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	//Main
	public static final String VERSION = "1.3.0002";
	public static final String CESTORENAME_DEFAULT = "DEFAULT";
	public static final String CM_GLOBAL = "global";
	
	//URL related
	public static final String URL_DEFAULTLOAD = "./ce/autoload.cecmd";
	public static final String URL_MODELDIR = "http://ce-models.eu-gb.mybluemix.net";
	public static final String URL_HUDSON = "./hudson/ce/cmd/load_hudson.cecmd";
	public static final String URL_CONV_INITIALISE = "/ce-store/ce/conversation/cmd/load_conv.cecmd";

	//File related
	public static final String DEFAULT_ROOT = "/opt/ibm/ce-store/";
	public static final String SUB_FOLDER_GENERATED = "generated/";
	public static final String SUFFIX_CE = ".ce";
	public static final String SUFFIX_DEFAULT = ".default";
	public static final String SUFFIX_TIMING = ".timing";

	//UID related
	public static final String DEFAULT_UIDPREFIX = "";
	public static final String DEFAULT_UIDPADFORMAT = "%01d";
	public static final long DEFAULT_UIDSTART = 0;
	public static final long DEFAULT_UIDEND = 999999;
	public static final boolean UID_USEPADDING = false;
	
	//Default values
	public static final long NO_TS = -1;
	public static final String DEFAULT_MAXITS = "10";
	public static final String DEFAULT_DATEFMT = "yyyy-MM-dd";
	public static final String DEFAULT_NOVAL = "-1";
	public static final int DEFAULT_MAXSUGGS = 10;
	public static final int CEVALUE_UNLIMITED = -1;

	//Characters
	public static final String ES = "";
	public static final String NL = "\n";
	public static final String BR = "<br>";
	public static final String BS = "\\";
	public static final String ENCODING = "UTF-8";
	public static final String URL_SEP = "/";
	public static final String URL_EQUALS = "=";
	public static final String URL_AMPERSAND = "&";
	public static final String FILE_SEP = "/";

	//Misc
	public static final String SRCID_CONCMETAMODEL = "src_cm";
	public static final String STEPNAME_CONCMETAMODEL = "conceptualiseMetamodel";
	public static final String SRCID_POPMODEL = "src_pm";
	public static final String STEPNAME_POPMODEL = "populateMetamodel";
	public static final int SENMODE_NORMAL = 1;
	public static final int SENMODE_VALIDATE = 2;
	public static final String TRIGTYPE_CON = "CONCEPT";
	public static final String HDR_CE = "CE";
	public static final String HDR_COUNT = "count";
	public static final String PREFIX_ANNO = "anno_";
	public static final String PREFIX_SEN = "sen_";
	public static final String PREFIX_SRC = "src_";
	public static final String PREFIX_CLAUSE = "clause_";
	public static final String PREFIX_CONCAT = "ccv_";
	public static final String PREFIX_PROPVAL = "pv_";
	public static final String PREFIX_QUERY = "query_";
	public static final String PREFIX_TEMP = "temp_";
	public static final String FORMAT_SRC = "%03d";
	public static final String REGEX_KEYWORDLIST = "([^\"]\\S*|\".+?\")\\s*";

	public static final String MODELNAME_CORE = "core";
	public static final String CESRC_NAME = "my knowledgebase";

	//Conversation
	public static final String TYPE_CON = "CONCEPT";
	public static final String TYPE_PROP = "PROPERTY";
	public static final String FORM_CONVFACT = "conv_facts";
	public static final String UID_PREFIX = "msg_";
	public static final long DEFAULT_DELAY = 0;
	public static final String CMD_CONFIRM = "confirm";
	public static final String CMD_OK = "ok";
	public static final String CMD_YES = "yes";
	public static final String CMD_EXPAND = "expand";
	public static final String CMD_EXPLAIN = "explain";
	public static final String ACT_TELL = "tell";
	public static final String ACT_CONFIRM = "confirm";
	public static final String ACT_EXPAND = "expand";
	public static final String FORM_CONVINIT = "Conversation initialisation";
	public static final String UNKNOWN_USER = "(unknown)";
	public static final String PROPTYPE_OBJECT = "O";
	public static final String PROPTYPE_DATATYPE = "D";
	
	public static final String FOLDER_JSON = "json/";
	public static final String FOLDER_MODELS = "models/";
	public static final String JSONFILE_QUESTIONS = "questions_core.json";
	public static final String JSONFILE_ANSWERS = "answers_core.json";

	//TO BE REMOVED
	public static final String CESEN_SEPARATOR = "{|}";
	public static final String LABEL_PREFIX = "{";
	public static final String LABEL_SUFFIX = "}:";
	public static final String PROPDEF_PREFIX = "[";
	public static final String PROPDEF_SUFFIX = "]:";

}
