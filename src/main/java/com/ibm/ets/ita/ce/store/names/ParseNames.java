package com.ibm.ets.ita.ce.store.names;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class ParseNames {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	public static final char CHAR_BELL = '\b';
	public static final char CHAR_CR = '\r';
	public static final char CHAR_NL = '\n';
	public static final char CHAR_F = '\f';
	public static final char CHAR_SPACE = ' ';
	public static final char CHAR_COMMA = ',';
	public static final char CHAR_OPBR = '(';
	public static final char CHAR_CLBR = ')';
	public static final char CHAR_OPPAR = '[';
	public static final char CHAR_CLPAR = ']';
	public static final char CHAR_TAB = '	';
	public static final char CHAR_TAB2 = '\t';
	public static final char CHAR_DOT = '.';
	public static final char CHAR_SQ = '\'';
	public static final char CHAR_DQ = '\"';
	public static final char CHAR_BS = '\\';
	public static final char CHAR_FQ = '/';
	public static final char CHAR_BOM = (char)65279;	//This is the Byte Order Marker Character inserted when editing in notepad
	public static final char CHAR_SQ1 = '‘';
	public static final char CHAR_SQ2 = '’';
	public static final char CHAR_DQ1 = '“';
	public static final char CHAR_DQ2 = '”';
	public static final char CHAR_DASH = '-';
	public static final char CHAR_COLON = ':';
	public static final char CHAR_OPENBRA = '{';
	public static final char CHAR_CLOSEBRA = '}';

	public static final String TOKEN_NEW = "{{NEW}}";
	public static final String TOKEN_CEFOLDER = "{CeStore_folder}";
	public static final String TOKEN_VARIABLE = "$";
	public static final String TOKEN_CONSTANT = "#";
	public static final String TOKEN_CONCAT = "<>";
	public static final String TOKEN_COUNT = "#";
	public static final String TOKEN_SUM = "@";

	public static final String TOKEN_BLANK = "";
	public static final String TOKEN_DOT = ".";
	public static final String TOKEN_TILDE = "~";
	public static final String TOKEN_COLON = ":";
	public static final String TOKEN_OPENSQBR = "[";
	public static final String TOKEN_CLOSESQBR = "]";
	public static final String TOKEN_OPENPAR = "(";
	public static final String TOKEN_CLOSEPAR = ")";
	public static final String TOKEN_OPENBRA = "{";
	public static final String TOKEN_CLOSEBRA = "}";
	public static final String TOKEN_COMMA = ",";
	public static final String TOKEN_SPACE = " ";
	public static final String TOKEN_DQ = "\"";

	public static final String TOKEN_BECAUSE = "because";
	public static final String TOKEN_NULL = "null";

	public static final String TOKEN_A = "a";
	public static final String TOKEN_AN = "an";
	public static final String TOKEN_THE = "the";
	public static final String TOKEN_VALUE = "value";
	public static final String TOKEN_THERE = "there";
	public static final String TOKEN_IS = "is";
	public static final String TOKEN_NAMED = "named";	
	public static final String TOKEN_HAS = "has";
	public static final String TOKEN_AS = "as";
	public static final String TOKEN_AND = "and";
	public static final String TOKEN_THAT = "that";
	public static final String TOKEN_NO = "no";
	public static final String TOKEN_PERFORM = "perform";
	public static final String TOKEN_STATEMENT = "statement";

	public static final String TOKEN_CONCEPTUALISE = "conceptualise";
	public static final String TOKEN_CONCEPTUALIZE = "conceptualize";
	public static final String TOKEN_DEFINE = "define";
	public static final String TOKEN_EXACTLY = "exactly";
	public static final String TOKEN_AT = "at";
	public static final String TOKEN_MOST = "most";
	public static final String TOKEN_ONE = "one";
	public static final String TOKEN_1 = "1";

	public static final String TOKEN_FOR = "for";
	public static final String TOKEN_HOW = "how";
	public static final String TOKEN_MANY = "many";
	public static final String TOKEN_WHICH = "which";
	public static final String TOKEN_IT = "it";
	public static final String TOKEN_TRUE = "true";
	public static final String TOKEN_FALSE = "false";

	public static final String TOKEN_OR = "or";
	public static final String TOKEN_ORDER = "order";	
	public static final String TOKEN_LIMIT = "limit";	
	public static final String TOKEN_BY = "by";
	public static final String TOKEN_ASCENDING = "ascending";
	public static final String TOKEN_DESCENDING = "descending";

	public static final String TOKEN_IF = "if";
	public static final String TOKEN_THEN = "then";

	//Qualified sentence tokens
	public static final String TOKEN_TIMESTAMPED = "time-stamped";
	public static final String TOKEN_IN = "in";
	public static final String TOKEN_QUALIFIED = "qualified";
	public static final String TOKEN_WITH = "with";
	public static final String TOKEN_TRUST = "trust";
	public static final String TOKEN_LEVEL = "level";
	public static final String TOKEN_CURRENTLY = "currently";
	public static final String TOKEN_HISTORICALLY = "historically";
	public static final String TOKEN_INFUTURE = "in-future";
	public static final String TOKEN_ALWAYS = "always";
	public static final String TOKEN_POSSIBLE = "possible";
	public static final String TOKEN_IMPOSSIBLE = "impossible";
	public static final String TOKEN_HYPOTHESISED = "hypothesised";
	public static final String TOKEN_ASSUMED = "assumed";
	public static final String TOKEN_ASSERTED = "asserted";
	public static final String TOKEN_DECLARED = "declared";
	public static final String TOKEN_INFERRED = "inferred";

	public static final String TOKEN_RESET = "reset";
	public static final String TOKEN_RELOAD = "reload";
	public static final String TOKEN_STORE = "store";
	public static final String TOKEN_STARTING = "starting";
	public static final String TOKEN_UID = "uid";
	public static final String TOKEN_BUILD = "build";
	public static final String TOKEN_SCHEMA = "schema";
	public static final String TOKEN_EMPTY = "empty";
	public static final String TOKEN_INSTANCES = "instances";
	public static final String TOKEN_LOAD = "load";
	public static final String TOKEN_DELETE = "delete";
	public static final String TOKEN_SENTENCES = "sentences";
	public static final String TOKEN_FROM = "from";
	public static final String TOKEN_URL = "url";
	public static final String TOKEN_FILE = "file";
	public static final String TOKEN_QUERY = "query";
	public static final String TOKEN_USING = "using";
	public static final String TOKEN_INTO = "into";
	public static final String TOKEN_RUN = "run";
	public static final String TOKEN_SOURCE = "source";
	public static final String TOKEN_SOURCES = "sources";
	public static final String TOKEN_NAME = "name";
	public static final String TOKEN_AGENT = "agent";
	public static final String TOKEN_ID = "id";
	public static final String TOKEN_SHOW = "show";
	public static final String TOKEN_NEXT = "next";
	public static final String TOKEN_TO = "to";
	public static final String TOKEN_PREPARE = "prepare";
	public static final String TOKEN_CACHED = "cached";
	public static final String TOKEN_SAVE = "save";
	public static final String TOKEN_CE = "CE";
	public static final String TOKEN_SWITCH = "switch";
	public static final String TOKEN_SET = "set";

	public static final String ANNO_TOKEN_NOTE = "Note:";
	public static final String ANNO_TOKEN_MODEL = "Model:";

	public static final String PREAMBLE_THEREISA = "there is a";
	public static final String PREAMBLE_THEREISAN = "there is an";
	public static final String PREAMBLE_THEVALUE = "the value";
	public static final String PREAMBLE_NOVALUE = "no value";

	public static final String VARNAME_ARR = "autorun rules";
	public static final String VARNAME_MDU = "model directory url";
	public static final String VARVAL_TRUE = "true";

	public static final String UID_NEXTAVAIL = "(next available)";

	public static final String SEARCH_AND = "AND";
	public static final String SEARCH_OR = "OR";
	public static final String SEARCH_NOT = "NOT";

	//TODO: Replace this nasty approach with a proper JSON structure
	public static final String SCELABEL_NORMAL = "";
	public static final String SCELABEL_ANNONAME = "{AnnoName}:";
	public static final String SCELABEL_ANNOVAL = "{AnnoVal}:";
	public static final String SCELABEL_CONCEPT = "{Concept}:";
	public static final String SCELABEL_PROP = "{Property}:";
	public static final String SCELABEL_INSTVAL = "{Instance}:";
	public static final String SCELABEL_BECAUSE = "{Because}:";
	public static final String SCELABEL_QUOTE = "{Quote}:";
	public static final String SCELABEL_CONNECTOR = "{Connector}:";
	public static final String SCELABEL_RQNAME = "{Name}:";
	public static final String SCELABEL_RQSTART = "{RqStart}:";

}
