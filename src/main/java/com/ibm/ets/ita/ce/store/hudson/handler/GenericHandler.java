package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.client.web.ServletStateManager;
import com.ibm.ets.ita.ce.store.hudson.helper.Answer;
import com.ibm.ets.ita.ce.store.hudson.helper.AnswerReply;
import com.ibm.ets.ita.ce.store.hudson.helper.ChosenWord;
import com.ibm.ets.ita.ce.store.hudson.helper.ConvConfig;
import com.ibm.ets.ita.ce.store.hudson.helper.HudsonManager;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public abstract class GenericHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected static final int DEFAULT_CONF = 100;
	protected static final int ERR_CONF = 0;

	public static final String CON_SPATIAL = "spatial thing";
	public static final String CON_TEMPTHING = "templated thing";
	public static final String CON_CC = "conversation config";
	public static final String CON_SUPPCON = "suppressed concept";
	public static final String CON_CONFCON = "configuration concept";
	public static final String CON_UNINTCON = "uninteresting concept";
	public static final String CON_QUALCON = "qualified concept";
	public static final String CON_MODIFIER = "modifier";
	public static final String CON_ENDMOD = "end modifier";
	public static final String CON_SRCHMOD = "search modifier";
	public static final String CON_FILTMOD = "filter modifier";
	public static final String CON_FUNCMOD = "function modifier";
	public static final String CON_CEMOD = "CE modifier";
	public static final String CON_GROUPER = "grouper";
	public static final String CON_ORDERER = "orderer";
	public static final String CON_APICON = "api concept";
	public static final String CON_DBCON = "database concept";
	public static final String CON_DBTABLE = "database table";
	public static final String CON_DBCOL = "database column";
	public static final String CON_SUMMCOL = "summable column";
	public static final String CON_FKDC = "foreign key database column";
	public static final String CON_DVDC = "duplicated value database column";
	public static final String CON_LOCCON = "local concept";
	public static final String CON_SOURCE = "source";
	public static final String CON_PERSON = "person";
	public static final String CON_LOCATION = "location";
	public static final String CON_MEDIA = "media";
	public static final String CON_RESLIST = "response list";
	public static final String CON_SEEK = "seekable thing";
	public static final String CON_DEFSS = "default SQL source";
	public static final String CON_SQLSRC = "SQL source";

	public static final String PROP_LAT = "latitude";
	public static final String PROP_LON = "longitude";
	public static final String PROP_AL1 = "line 1";
	public static final String PROP_PC = "postcode";
	public static final String PROP_DEFBY = "is defined by";
	public static final String PROP_ISFROM = "is from";
	public static final String PROP_CRED = "credibility";

	public static final String PROP_ENTTEMP = "entity template";
	public static final String PROP_TPLSTR = "template string";
	public static final String PROP_CORRTO = "corresponds to";
	public static final String PROP_APPWHEN = "applies when domain concept is";
	public static final String PROP_PROPTPL = "property template";
	public static final String PROP_MVS = "multi-value separator";
	public static final String PROP_PROPSEP = "property separator";
	public static final String PROP_WAMP = "warn about missed properties";
	public static final String PROP_PRETEXT = "prefix text";
	public static final String PROP_SUFFTEXT = "suffix text";
	public static final String PROP_FILTOP = "filter operator";
	public static final String PROP_FILTVAL = "filter value";
	public static final String PROP_DATAFORMAT = "data format";
	public static final String PROP_INVOLVES = "involves";
	public static final String PROP_CONSTRAINT = "constraint";
	public static final String PROP_PRICOL = "primary column";
	public static final String PROP_MAINFORM = "main form";
	public static final String PROP_FKCOL = "foreign key column";
	public static final String PROP_TGTCON = "target concept";
	public static final String PROP_ORDCOL = "order column name";
	public static final String PROP_TABLENAME = "table name";
	public static final String PROP_SHORTNAME = "short name";
	public static final String PROP_COLNAME = "column name";
	public static final String PROP_ISDEFON = "is defined on";
	public static final String PROP_DESC = "description";
	public static final String PROP_SN = "short name";
	public static final String PROP_SOURCE = "source";
	public static final String PROP_USES = "uses";
	public static final String PROP_OPTUSES = "optionally uses";
	public static final String PROP_PROPNAME = "property name";
	public static final String PROP_PROPQUAL = "property qualifier";
	public static final String PROP_IFKT = "is foreign key to";
	public static final String PROP_MAPSTO = "maps to";
	public static final String PROP_URL = "url";
	public static final String PROP_CREDIT = "credit";
	public static final String PROP_CONSQL = "constraint sql";
	public static final String PROP_DEFJOIN = "default join";
	public static final String PROP_JOININST = "join instance";
	public static final String PROP_WORD = "word";
	public static final String PROP_CAUSES = "causes";
	public static final String PROP_RESPCODE = "response code";
	public static final String PROP_RESPTEXT = "response text";
	public static final String PROP_IQB = "is qualified by";
	public static final String PROP_UNITNAME = "unit name";
	public static final String PROP_POTSRC = "potential source";
	public static final String PROP_TNAME = "table name";
	public static final String PROP_CNAME = "column name";
	public static final String PROP_CASESEN = "case insensitive";
	public static final String PROP_FCASE = "forced case";
	public static final String PROP_FILTTEXT = "filter text";
	public static final String PROP_DISPTEXT = "display text";
	public static final String PROP_MUO = "makes use of";
	public static final String PROP_SAMEAS = "is the same as";
	public static final String PROP_OUTPROP = "output property";
	public static final String PROP_CEURL = "CE URL";

	private static final String ANSKEY_ERR = "error";
	protected static final String ANSCODE_GENERROR = "GENERAL";

	protected static final String ANSCODE_SQLERROR = "ERROR_SQL";
	protected static final String CHATTY_ERROR = "Something went wrong: ";

	protected static final String ANSCODE_NEGATION = "NEGATION";
	protected static final String CHATTY_NEGATION = "I can't handle negations yet";

	protected static final String ANSCODE_NOTUNDERSTOOD = "NOT_UNDERSTOOD";
	protected static final String CHATTY_NOTUNDERSTOOD = "I'm sorry, I didn't understand the question";

	protected static final String ANSCODE_NOCOORDS = "NO_COORDS";
	protected static final String CHATTY_NOCOORDS = "I know that is a location but I don\'t have any coordinates for it";

	protected static final String ANSCODE_CANNOTLOC = "CANNOT_LOCATE";
	protected static final String CHATTY_CANNOTLOC = "I don't know how to locate %1, sorry";

	protected static final String ANSCODE_NOMEDIA = "NO_MEDIA";

	protected ActionContext ac = null;
	private ConvConfig cc = null;
	protected boolean debug = false;
	protected long startTime = -1;

	public GenericHandler(ActionContext pAc, boolean pDebug, long pStartTime) {
		this.ac = pAc;
		this.debug = pDebug;
		this.startTime = pStartTime;
	}

	protected ConvConfig getConvConfig() {
		if (this.cc == null) {
			ModelBuilder mb = this.ac.getModelBuilder();

			if (mb != null) {
				for (CeInstance ccInst : mb.getAllInstancesForConceptNamed(this.ac, CON_CC)) {
					if (this.cc == null) {
						this.cc = ConvConfig.createUsing(this.ac, ccInst);
					} else {
						reportError("Error: more than one instance of " + CON_CC + " defined: " + ccInst.getInstanceName(), this.ac);
					}
				}
			}
		}

		if (this.cc == null) {
			reportDebug("No " + CON_CC + " instance found", this.ac);
		}

		return this.cc;
	}

	public static Answer handleExceptionAsAnswer(ActionContext pAc, Exception pE, String pErrCode, AnswerReply pReply) {
		Answer err = Answer.createError(ANSKEY_ERR + "_" + pErrCode, ERR_CONF);
		err.setAnswerCode(pErrCode);
		err.setChattyAnswerText(CHATTY_ERROR + pE.getMessage());
		pReply.addAnswer(pAc, err);

		pE.printStackTrace();

		return err;
	}

	public static void handleGeneralErrorAsAnswer(ActionContext pAc, String pErrorMsg, AnswerReply pReply) {
		Answer err = Answer.createError(ANSKEY_ERR + "_" + ANSCODE_GENERROR, ERR_CONF);
		err.setAnswerCode(ANSCODE_GENERROR);
		err.setChattyAnswerText(pErrorMsg);
		pReply.addAnswer(pAc, err);
	}
	
	public static void handleNotUnderstoodError(ActionContext pAc, AnswerReply pReply) {
		Answer err = Answer.createError(ANSKEY_ERR + "_" + ANSCODE_NOTUNDERSTOOD, ERR_CONF);
		err.setAnswerCode(ANSCODE_NOTUNDERSTOOD);
		err.setChattyAnswerText(CHATTY_NOTUNDERSTOOD);
		pReply.addAnswer(pAc, err);
	}

	public static void handleSomethingWentWrongError(ActionContext pAc, AnswerReply pReply) {
		Answer err = Answer.createError(ANSKEY_ERR + "_" + ANSCODE_SQLERROR, ERR_CONF);
		err.setAnswerCode(ANSCODE_SQLERROR);
		err.setChattyAnswerText(CHATTY_ERROR);
		pReply.addAnswer(pAc, err);
	}

	public static void handleNoCoordinatesError(ActionContext pAc, AnswerReply pReply, ArrayList<ChosenWord> pWords) {
		Answer err = Answer.create(ANSKEY_ERR + "_" + ANSCODE_NOCOORDS, pWords, ERR_CONF);
		err.setAnswerCode(ANSCODE_NOCOORDS);
		err.setChattyAnswerText(CHATTY_NOCOORDS);
		pReply.addAnswer(pAc, err);
	}

	public static void handleCannotLocateError(ActionContext pAc, AnswerReply pReply, ArrayList<ChosenWord> pWords) {
		String chattyText = CHATTY_CANNOTLOC;
		String repText = "";

		for (ChosenWord thisCw : pWords) {
			repText += thisCw.interpretationText(pAc, false);
		}

		chattyText = chattyText.replace("%1", repText);

		Answer err = Answer.create(ANSKEY_ERR + "_" + ANSCODE_CANNOTLOC, pWords, ERR_CONF);
		err.setAnswerCode(ANSCODE_CANNOTLOC);
		err.setChattyAnswerText(chattyText);
		err.markAsWhere();
		pReply.addAnswer(pAc, err);
	}

	public static void handleNoMediaError(ActionContext pAc, AnswerReply pReply, String pChattyText, ArrayList<ChosenWord> pWords) {
		Answer err = Answer.create(ANSKEY_ERR + "_" + ANSCODE_NOMEDIA, pWords, ERR_CONF);
		err.setAnswerCode(ANSCODE_NOMEDIA);
		err.setChattyAnswerText(pChattyText);
		pReply.addAnswer(pAc, err);
	}

	public static String columnNameFor(ActionContext pAc, CeInstance pInst) {
		CeInstance tableInst = null;
		String shortName = null;
		String colName = null;
		String result = null;
		
		if (pInst != null) {
			colName = unqualifiedColumnNameFor(pInst);
			tableInst = pInst.getSingleInstanceFromPropertyNamed(pAc, PROP_ISDEFON);
			
			if (tableInst != null) {
				shortName = shortNameFor(tableInst);
				result = shortName + "." + colName;
			} else {
				shortName = "";
				result = colName;
			}
		} else {
			result = "";
		}

		return result;
	}

	public static String unqualifiedColumnNameFor(CeInstance pInst) {
		String colName = pInst.getSingleValueFromPropertyNamed(PROP_COLNAME);

		if (colName.isEmpty()) {
			colName = pInst.getInstanceName();
		}

		return colName;
	}

	public static String primaryColumnNameFor(ActionContext pAc, CeInstance pInst) {
		CeInstance mcInst = pInst.getSingleInstanceFromPropertyNamed(pAc, PROP_PRICOL);

		return columnNameFor(pAc, mcInst);
	}

	public static String unqualifiedPrimaryColumnNameFor(ActionContext pAc, CeInstance pInst) {
		CeInstance mcInst = pInst.getSingleInstanceFromPropertyNamed(pAc, PROP_PRICOL);

		return unqualifiedColumnNameFor(mcInst);
	}

	public static String tableNameFor(CeInstance pInst) {
		String tableName = null;
		
		if (pInst != null) {
			tableName = pInst.getSingleValueFromPropertyNamed(PROP_TABLENAME);

			if (tableName.isEmpty()) {
				tableName = pInst.getInstanceName();
			}
		} else {
			tableName = "";
		}

		return tableName;
	}
	
	public static String shortNameFor(CeInstance pInst) {
		String shortName = null;
		
		if (pInst != null) {
			shortName = pInst.getSingleValueFromPropertyNamed(PROP_SHORTNAME);

			if (shortName.isEmpty()) {
				shortName = tableNameFor(pInst);
			}
		} else {
			shortName = "";
		}

		return shortName;
	}
	
	public static CeInstance joinedTableFor(ActionContext pAc, CeInstance pInst) {
		return pInst.getSingleInstanceFromPropertyNamed(pAc, PROP_IFKT);
	}

	public static CeInstance definedTableFor(ActionContext pAc, CeInstance pInst) {
		return pInst.getSingleInstanceFromPropertyNamed(pAc, PROP_ISDEFON);
	}

	public static void winnowShorterMatchesFrom(ActionContext pAc, ArrayList<CeInstance> pInsts) {
		ArrayList<CeInstance> copyList = new ArrayList<CeInstance>();
		copyList.addAll(pInsts);
		
		for (CeInstance thisInst : copyList) {
			if (hasLongerVariant(pAc, thisInst, copyList)) {
				pInsts.remove(thisInst);
			}
		}
	}

	private static boolean hasLongerVariant(ActionContext pAc, CeInstance pInst, ArrayList<CeInstance> pAllInsts) {
		boolean result = false;
		ArrayList<String> tgtInstIds = pInst.getInstanceIdentifiers(pAc);

		for (CeInstance possInst : pAllInsts) {
			if (possInst != pInst) {
				for (String possId : possInst.getInstanceIdentifiers(pAc)) {
					String lcPossId = possId.toLowerCase();

					for (String tgtId : tgtInstIds) {
						String lcTgtId = tgtId.toLowerCase();

						//TODO: Should this be contains or startsWith? (deeper fix needed)
						if (lcPossId.startsWith(lcTgtId) && !lcPossId.equals(lcTgtId)) {
							result = true;
							break;
						}
					}
				}
			}
		}

		return result;
	}

	public static ContainerSentenceLoadResult saveCeText(ActionContext pAc, String pCeText, CeSource pSrc) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
		ContainerSentenceLoadResult result = sa.saveCeText(pCeText, null);

		//Clear the various caches
		clearCaches(pAc);

		return result;
	}

	public static void clearCaches(ActionContext pAc) {
		HudsonManager hm = ServletStateManager.getHudsonManager(pAc);

		hm.clearInverseLists(pAc);
		hm.clearWordCheckerCache(pAc);
		hm.clearIndexedEntityAccessor(pAc);
	}

}