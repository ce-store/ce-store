package com.ibm.ets.ita.ce.store.names;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class RestNames {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	//Misc
	public static final String METHOD_GET = "GET";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_DELETE = "DELETE";

	public static final String REQTYPE_ANY = "*/*";
	public static final String REQTYPE_JSON = "application/json";
	public static final String REQTYPE_TEXT = "text/plain";
	public static final String REQTYPE_WEAKTEXT = "text";

	public static final String RESPONSE_JSON = "application/json";
	public static final String RESPONSE_TEXT = "application/text";

	public static final String HDR_ACCEPT = "Accept";
	public static final String HDR_CEUSER = "CE_User";
	public static final String HDR_AUTH = "Authorization";
	public static final String HDR_ORIGIN = "Origin";
	public static final String HDR_AC_AO = "Access-Control-Allow-Origin";
	public static final String HDR_AC_AC = "Access-Control-Allow-Credentials";
	public static final String HDR_AC_AM = "Access-Control-Allow-Methods";
	public static final String HDR_AC_AH = "Access-Control-Allow-Headers";
	public static final String HDR_AC_RH = "Access-Control-Request-Headers";

	public static final String CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";

	//REST request names
	public static final String REST_STORE = "stores";
	public static final String REST_SOURCE = "sources";
	public static final String REST_CONCEPT = "concepts";
	public static final String REST_INSTANCE = "instances";
	public static final String REST_SENTENCE = "sentences";
	public static final String REST_QUERY = "queries";
	public static final String REST_RULE = "rules";
	public static final String REST_PRIMARY = "primary";
	public static final String REST_SECONDARY = "secondary";
	public static final String REST_CHILDREN = "children";
	public static final String REST_PARENTS = "parents";
	public static final String REST_DIRECT = "direct";
	public static final String REST_FREQUENCY = "frequency";
	public static final String REST_COUNT = "count";
	public static final String REST_EXACT = "exact";
	public static final String REST_PROPERTY = "properties";
	public static final String REST_DATATYPE = "datatype";
	public static final String REST_OBJECT = "object";
	public static final String REST_RATIONALE = "rationale";
	public static final String REST_EXECUTE = "execute";
	public static final String REST_SPECIAL = "special";
	public static final String REST_CONMODEL = "models";
	public static final String REST_HEADLINE = "headline";
	public static final String REST_REFERENCE = "references";
	public static final String REST_COMMON = "common";
	public static final String REST_STATISTICS = "statistics";
	public static final String REST_PATTERN = "patterns";
	public static final String REST_CONFIG = "config";
	public static final String REST_UID = "uid";
	public static final String REST_RESET = "reset";
	public static final String REST_SEARCH = "keyword-search";
	public static final String REST_SHADCON = "shadow-concepts";
	public static final String REST_SHADINST = "shadow-instances";
	public static final String REST_UNREFINST = "unreferenced-instances";
	public static final String REST_DIVCONINST = "diverse-concept-instances";
	public static final String REST_MULTINSTS = "instances-for-multiple-concepts";
	public static final String REST_HUDSON = "hudson";
	public static final String REST_BATCH = "batch";
	public static final String REST_SEN_MODEL = "model";
	public static final String REST_SEN_FACT = "fact";
	public static final String REST_SEN_RULE = "rule";
	public static final String REST_SEN_QUERY = "query";
	public static final String REST_SEN_PATTERN = "pattern";
	public static final String REST_SEN_ANNOTATION = "annotation";
	public static final String REST_SEN_COMMAND = "command";
	public static final String REST_SEN_VALID = "valid";
	public static final String REST_SEN_INVALID = "invalid";

	public static final String[] REST_SEN_ALL_TYPES = { REST_SEN_MODEL, REST_SEN_FACT, REST_SEN_RULE, REST_SEN_QUERY, REST_SEN_PATTERN, REST_SEN_ANNOTATION, REST_SEN_COMMAND };
	public static final String[] REST_SEN_ALL_VALIDITIES = { REST_SEN_VALID, REST_SEN_INVALID };

	//Hudson
	public static final String REST_HELPER = "helper";
	public static final String REST_EXECUTOR = "executor";
	public static final String REST_INTERPRETER = "interpreter";
	public static final String REST_ANSWERER = "answerer";
	public static final String REST_STATUS = "status";	
	public static final String REST_DIR_LIST = "directory_list";
	public static final String REST_DIR_LOAD = "directory_load";
	public static final String REST_DIR_GETQS = "directory_get_questions";
	public static final String REST_DIR_GETAS = "directory_get_answers";

	//Parameter names
	public static final String PARM_PROPNAME = "propName";
	public static final String PARM_PROPVAL = "propVal";
	public static final String PARM_STEPS = "steps";
	public static final String PARM_RELINSTS = "relatedInstances";
	public static final String PARM_REFINSTS = "referringInstances";
	public static final String PARM_LIMRELS = "limitRelationships";
	public static final String PARM_ONLYPROPS = "onlyProperties";
	public static final String PARM_SPTS = "suppressPropertyTypes";
	public static final String PARM_STARTTS = "startTimestamp";
	public static final String PARM_ENDTS = "endTimestamp";
	public static final String PARM_RETCE = "returnCe";
	public static final String PARM_RETINSTS = "returnInstances";
	public static final String PARM_CETEXT = "ceText";
	public static final String PARM_SHOWSTATS = "showStats";
	public static final String PARM_STYLE = "style";
	public static final String PARM_RANGE = "range";
	public static final String PARM_SINCE = "since";
	public static final String PARM_PROPERTY = "property";
	public static final String PARM_BUCKETS = "buckets";
	public static final String PARM_SUPPCE = "suppressCe";
	public static final String PARM_SUPPRES = "suppressResult";
	public static final String PARM_ACTION = "action";
	public static final String PARM_RUNRULES = "runRules";
	public static final String PARM_TYPE = "type";
	public static final String PARM_VALIDITY = "validity";
	public static final String PARM_AGENTINSTNAME = "filterByAgentInstanceName";
	public static final String PARM_DETAIL = "filterByDetail";
	public static final String PARM_SIZE = "size";
	public static final String PARM_MODEL = "model";
	public static final String PARM_RETINTER = "returnInterpretation";

	public static final String PARM_SEARCHTERMS = "keywords";
	public static final String PARM_CASESEN = "caseSensitive";
	public static final String PARM_RESTRICTCONNAMES = "restrictToConcepts";
	public static final String PARM_RESTRICTPROPNAMES = "restrictToProperties";
	public static final String PARM_CONNAMES = "conceptNames";
	public static final String PARM_IGMETMOD = "ignoreMetaModel";
	public static final String PARM_VALUE = "value";

	//Parameter values
	public static final String STYLE_FULL = "full";
	public static final String STYLE_SUMMARY = "summary";
	public static final String STYLE_MINIMAL = "minimal";
	public static final String STYLE_NORMALISED = "normalised";

	public static final String ACTION_SAVE = "save";
	public static final String ACTION_VALIDATE = "validate";
	public static final String ACTION_PARSE = "parse";
	public static final String ACTION_EXEC_Q = "execute_as_query";
	public static final String ACTION_EXEC_R = "execute_as_rule";

}
