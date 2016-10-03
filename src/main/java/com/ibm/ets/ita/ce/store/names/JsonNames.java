package com.ibm.ets.ita.ce.store.names;

/*******************************************************************************
 * (C) Copyright IBM Corporation 2011, 2016 All Rights Reserved
 *******************************************************************************/

public class JsonNames {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	// JSON type values (returned in "type" element to signify the type of JSON
	// object)
	public static final String JSONTYPE_CON = "concept";
	public static final String JSONTYPE_CONMOD = "conceptual model";
	public static final String JSONTYPE_INST = "instance";
	public static final String JSONTYPE_PROP = "property";
	public static final String JSONTYPE_QUERY = "query";
	public static final String JSONTYPE_RULE = "rule";
	public static final String JSONTYPE_SEN = "sentence";
	public static final String JSONTYPE_SOURCE = "source";
	public static final String JSONTYPE_STORE = "store";

	// JSON element names
	public static final String JSON_TYPE = "_type";
	public static final String JSON_STYLE = "_style";
	public static final String JSON_ID = "_id";
	public static final String JSON_SHADOW = "_shadow";
	public static final String JSON_CREATED = "_created";
	public static final String JSON_LABEL = "_label";
	public static final String JSON_NORM_CONCEPTS = "_concept";

	public static final String JSON_INSTANCE_NAME = "instance_name";
	public static final String JSON_INSTANCE_LABEL = "instance_label";

	public static final String JSON_PROP_NAME = "property_name";
	public static final String JSON_PROP_VAL = "property_value";
	public static final String JSON_PROP_TYPE = "property_type";

	public static final String JSON_HEADERS = "headers";
	public static final String JSON_RESULTS = "results";
	public static final String JSON_ROWS = "rows";
	public static final String JSON_INSTANCES = "instances";
	public static final String JSON_TYPES = "types";
	public static final String JSON_NUMROWS = "number_of_rows";

	public static final String JSON_QUERY_TEXT = "query";
	public static final String JSON_QUERY_TIME = "query_time";

	public static final String JSON_SEARCHTERMS = "search_terms";
	public static final String JSON_SEARCHCONS = "search_concepts";
	public static final String JSON_SEARCHPROPS = "search_properties";
	public static final String JSON_SEARCHRESULTS = "search_results";
	public static final String JSON_COUNT = "count";

	public static final String JSON_SRC_IDS = "source_ids";
	public static final String JSON_CONCEPT_NAMES = "concept_names";
	public static final String JSON_SOURCES = "sources";
	public static final String JSON_CONCEPTS = "concepts";
	public static final String JSON_PROPERTIES = "properties";
	public static final String JSON_SENTENCES = "sentences";

	public static final String JSON_META_INSTANCE = "meta_instance";
	public static final String JSON_ANNOTATIONS = "annotations";
	public static final String JSON_SEN_TEXT = "ce_text";
	public static final String JSON_INSTCOUNT = "instance_count";
	public static final String JSON_ICON = "icon";
	public static final String JSON_CONMODELS = "conceptual_models";
	public static final String JSON_MODEL_NAMES = "conceptual_model_names";
	public static final String JSON_DIRPARENT_NAMES = "direct_parent_names";
	public static final String JSON_ALLPARENT_NAMES = "all_parent_names";
	public static final String JSON_ALLCHILD_NAMES = "all_child_names";
	public static final String JSON_CHILD_NAMES = "direct_child_names";
	public static final String JSON_DIR_PROPERTY_NAMES = "direct_property_names";
	public static final String JSON_INH_PROPERTY_NAMES = "inherited_property_names";
	public static final String JSON_DIR_PARENTS = "direct_parents";
	public static final String JSON_DIR_CHILDREN = "direct_children";
	public static final String JSON_PRISEN_COUNT = "primary_sentence_count";
	public static final String JSON_SECSEN_COUNT = "secondary_sentence_count";
	public static final String JSON_PRI_SENS = "primary_sentences";
	public static final String JSON_SEC_SENS = "secondary_sentences";
	public static final String JSON_DIR_PROPERTIES = "direct_properties";
	public static final String JSON_INH_PROPERTIES = "inherited_properties";

	public static final String JSON_PROPVALS = "property_values";
	public static final String JSON_PROPTYPES = "property_types";
	public static final String JSON_PROPRAT = "property_rationale";

	public static final String JSON_DIR_CONCEPT_NAMES = "direct_concept_names";
	public static final String JSON_INH_CONCEPT_NAMES = "inherited_concept_names";

	public static final String JSON_DOMAIN_NAME = "domain_name";
	public static final String JSON_RANGE_NAME = "range_name";
	public static final String JSON_PROP_STYLE = "property_style";
	public static final String JSON_ASS_DOMAIN_NAME = "asserted_domain_name";

	public static final String JSON_A_QUERIES = "queries";
	public static final String JSON_A_RULES = "rules";

	public static final String JSON_S_CETEXT = "ce_text";
	public static final String JSON_A_CONCEPTS = "concepts";
	public static final String JSON_A_ATTRIBUTES = "attributes";
	public static final String JSON_A_RELATIONSHIPS = "relationships";

	public static final String JSON_S_CE = "ce";
	public static final String JSON_S_QR_TYPE = "qr_type";
	public static final String JSON_S_QUERY_NAME = "query_name";
	public static final String JSON_L_QUERY_TIME = "query_time";
	public static final String JSON_S_RULE_NAME = "rule_name";
	public static final String JSON_L_RULE_TIME = "rule_time";

	public static final String JSON_S_PROPNAME = "property_name";
	public static final String JSON_S_PROPFORMAT = "property_format";
	public static final String JSON_S_PREM_OR_CONC = "premise_or_conclusion";
	public static final String JSON_S_SRCVAR = "source_variable";
	public static final String JSON_S_TGTVAR = "target_variable";
	public static final String JSON_S_CONNAME = "concept_name";

	public static final String JSON_S_VALUE = "value";
	public static final String JSON_S_OPERATOR = "operator";
	public static final String JSON_S_VARID = "variable_id";
	public static final String JSON_B_INCLUDED = "included";
	public static final String JSON_B_NEGATED_DOM = "negated_domain";
	public static final String JSON_B_NEGATED_RNG = "negated_range";

	public static final String JSON_RAT_ID = "rat_id";
	public static final String JSON_RAT_RULENAME = "rule_name";
	public static final String JSON_RAT_SENID = "source_senid";
	public static final String JSON_RAT_CON = "concept";
	public static final String JSON_RAT_NEGCON = "negated_concept";
	public static final String JSON_RAT_PROP = "property";
	public static final String JSON_RAT_NEGPROP = "negated_property";
	public static final String JSON_RAT_INST = "instance";
	public static final String JSON_RAT_RANGE = "range";
	public static final String JSON_RAT_VAL = "value";
	public static final String JSON_RAT_RATCE = "rationale_ce";
	public static final String JSON_RAT_PREMS = "premises";
	public static final String JSON_RAT_CONCS = "conclusions";
	public static final String JSON_RAT_VALSENIDS = "value_senids";

	public static final String JSON_PARENT_ID = "parent_id";
	public static final String JSON_CHILD_IDS = "child_ids";
	public static final String JSON_SEN_COUNT = "sentence_count";
	public static final String JSON_MOD_COUNT = "model_count";
	public static final String JSON_SOURCE_TYPE = "source_type";
	public static final String JSON_SOURCE_DETAIL = "detail";
	public static final String JSON_USER_INSTNAME = "user_instname";
	public static final String JSON_AGENT_NAME = "agent_instname";
	public static final String JSON_SENS = "sentences";

	public static final String JSON_SEN_TYPE = "sen_type";
	public static final String JSON_SEN_PRIORSEC = "pri_or_sec";
	public static final String JSON_SEN_VAL = "validity";
	public static final String JSON_SEN_SRC = "source";
	public static final String JSON_SEN_SRC_ID = "source_id";
	public static final String JSON_SEN_STRUCTUREDTEXT = "ce_structured_text";

	public static final String JSON_SEN_RATSTEPS = "rationale";

	public static final String JSON_IDX = "index";
	public static final String JSON_FRAGTYPE = "type";

	public static final String QR_TYPE_RULE = "RULE";
	public static final String QR_TYPE_QUERY = "QUERY";

	public static final String PREFIX_QUOTED_OPERATOR = "T_";

	public static final String VAL_UNDEFINED = "undefined";

	public static final String SENSOURCE_PRIMARY = "primary";
	public static final String SENSOURCE_SECONDARY = "secondary";

	public static final String JSON_MESSAGE = "message";
	public static final String JSON_ALERTS = "alerts";
	public static final String JSON_DEBUG = "debugs";
	public static final String JSON_INFOS = "infos";
	public static final String JSON_WARNINGS = "warnings";
	public static final String JSON_ERRORS = "errors";
	public static final String JSON_STATS = "stats";
	public static final String JSON_DURATION = "duration";
	public static final String JSON_TXN_CODEVERSION = "code_version";
	public static final String JSON_SERVER_TIME = "server_time";
	public static final String JSON_INST_COUNT = "instance_count";
	public static final String JSON_STRUCTURED = "structured_response";

	public static final String JSON_STOREPROPS = "store_properties";

	public static final String JSON_VALUE = "value";
	public static final String JSON_RANGE = "range";
	public static final String JSON_FREQUENCY = "frequency";
	public static final String JSON_UID_PREFIX = "prefix";
	public static final String JSON_UID_BATCHSTART = "batch_start";
	public static final String JSON_UID_BATCHEND = "batch_end";
	public static final String JSON_UID_BATCHSIZE = "batch_size";

	public static final String JSON_CRESENS = "created_sentences";
	public static final String JSON_EXECTIME = "execution_time";
	public static final String JSON_CMDCOUNT = "command_count";
	public static final String JSON_VALCOUNT = "valid_sentences";
	public static final String JSON_INVCOUNT = "invalid_sentences";

	public static final String JSON_NEWINSTS = "new_instances";
	public static final String JSON_STORE_VERSION = "store_version";
	public static final String JSON_CON_COUNT = "concept_count";

	public static final String JSON_MAININST = "main_instance";
	public static final String JSON_RELINSTS = "related_instances";
	public static final String JSON_REFINSTS = "referring_instances";

	public static final String TYPE_STORE = "store";

	// Hudson
	public static final String JSON_QUESTION = "question";
	public static final String JSON_Q_TEXT = "text";
	public static final String JSON_Q_WORDS = "words";
	public static final String JSON_ANSWERS = "answers";
	public static final String JSON_ANSWER = "answer";
	public static final String JSON_A_RESTEXT = "result text";
	public static final String JSON_A_CONF = "confidence";
	public static final String JSON_INT = "interpretation";
	public static final String JSON_INTS = "interpretations";
	public static final String JSON_AL_ERRORS = "errors";
	public static final String JSON_AL_WARNINGS = "warnings";

	public static final String JSON_CONF = "confidence";
	public static final String JSON_EXP = "explanation";
	public static final String JSON_RES = "result";

	public static final String JSON_CONS = "concepts";
	public static final String JSON_PROPS = "properties";
	public static final String JSON_INSTS = "instances";
	public static final String JSON_SPECS = "specials";

	public static final String JSON_ENTS = "entities";
	public static final String JSON_PHRASE = "phrase";
	public static final String JSON_STARTPOS = "start position";
	public static final String JSON_ENDPOS = "end position";

	// Help handler
	public static final String JSON_QT = "question text";
	public static final String JSON_SUGGS = "suggestions";
	public static final String JSON_BT = "before text";
	public static final String JSON_AT = "after text";

	public static final String JSON_ENTITIES = "entities";

	// Model directory handler
	public static final String JSON_MODELS = "models";

	// General management
	public static final String JSON_ET = "execution time";
	public static final String JSON_SM = "system message";

	public static final String SPEC_COLLECTION = "collection";
	public static final String SPEC_ENUMCON = "enumerated-concept";
	public static final String SPEC_LINKEDINST = "linked-instance";
	public static final String SPEC_MATCHTRIP = "matched-triple";
	public static final String SPEC_MULTIMATCH = "multi-match";
	public static final String SPEC_NUMBER = "number";

}
