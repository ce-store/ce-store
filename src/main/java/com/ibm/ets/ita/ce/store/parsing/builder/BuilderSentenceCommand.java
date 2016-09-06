package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.decodeForCe;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.stripDelimitingQuotesFrom;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.persistence.PersistenceManagerFactory;

public class BuilderSentenceCommand extends BuilderSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String VARNAME_ARR = "autorun rules";
	private static final String VARNAME_MDU = "model directory url";
	private static final String VARVAL_TRUE = "true";

	private static final String CMD_RESET = "reset";
	private static final String CMD_RELOAD = "reload";
	private static final String CMD_STORE = "store";
	private static final String CMD_STARTING = "starting";
	private static final String CMD_UID = "uid";
	private static final String CMD_BUILD = "build";
	private static final String CMD_SCHEMA = "schema";
	private static final String CMD_EMPTY = "empty";
	private static final String CMD_INSTANCES = "instances";
	private static final String CMD_LOAD = "load";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_SENTENCES = "sentences";
	private static final String CMD_FROM = "from";
	private static final String CMD_URL = "url";
	private static final String CMD_FILE = "file";
	private static final String CMD_QUERY = "query";
	private static final String CMD_USING = "using";
	private static final String CMD_INTO = "into";
	private static final String CMD_RUN = "run";
	private static final String CMD_THE = "the";
	private static final String CMD_NAMED = "named";
	private static final String CMD_SOURCE = "source";
	private static final String CMD_SOURCES = "sources";
	private static final String CMD_WITH = "with";
	private static final String CMD_NAME = "name";
	private static final String CMD_AGENT = "agent";
	private static final String CMD_ID = "id";
	private static final String CMD_SHOW = "show";
//	private static final String CMD_SET = "set";
	private static final String CMD_NEXT = "next";
	private static final String CMD_VALUE = "value";
	private static final String CMD_TO = "to";
//	private static final String CMD_AVAIL = "available";
	private static final String CMD_PREPARE = "prepare";
	private static final String CMD_FOR = "for";
	private static final String CMD_CACHED = "cached";
	private static final String CMD_SAVE = "save";
	private static final String CMD_CE = "CE";
	private static final String CMD_SWITCH = "switch";
	private static final String CMD_SET = "set";

	private static final String UID_NEXTAVAIL = "(next available)";

	private boolean isValid = false;
	private String cmdTargetUrlOrFile = "";
	private String cmdTargetQuery = "";
	private String cmdEmbeddedCe = "";
	private String cmdAgentInstName = "";
	private String cmdAgentConceptName = "";
	private String cmdTargetSourceName = "";
	private String cmdTargetSourceId = "";
	private String cmdTargetSourceAgentId = "";
	private String cmdStartingUid = "";
	private String cmdTargetStore = "";

	public BuilderSentenceCommand(String pSenText) {
		super(pSenText);
	}

	@Override
	protected void propogateRationaleValues() {
		//Do nothing - rationale does not apply to command sentences
	}

	@Override
	public boolean hasRationale() {
		//Rationale does not apply to command sentences
		return false;
	}

	public boolean isCmdSaveStore() {
		boolean result = false;

		if (this.rawTokens.size() == 3) {
			// No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_SAVE)) && (this.rawTokens.get(2).equals(CMD_STORE));
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdLoadStore() {
		boolean result = false;

		if (this.rawTokens.size() == 3) {
			// No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_LOAD)) && (this.rawTokens.get(2).equals(CMD_STORE));
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdSetValue() {
		boolean result = false;

		if (this.rawTokens.size() == 5) {
			// No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_SET)) && (this.rawTokens.get(3).equals(CMD_TO));
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdResetStore() {
		return isCmdResetStoreSimple() || isCmdResetStoreWithUidStart();
	}

	public boolean isCmdResetStoreSimple() {
		boolean result = false;

		if (this.rawTokens.size() == 3) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_RESET)) && (this.rawTokens.get(2).equals(CMD_STORE));
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdResetStoreWithUidStart() {
		boolean result = false;

		if (this.rawTokens.size() == 7) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_RESET)) && (this.rawTokens.get(2).equals(CMD_STORE)) && (this.rawTokens.get(3).equals(CMD_WITH)) && (this.rawTokens.get(4).equals(CMD_STARTING)) && (this.rawTokens.get(5).equals(CMD_UID));

			if (result) {
				this.isValid = true;
				this.cmdStartingUid = stripDelimitingQuotesFrom(this.rawTokens.get(6));
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdReloadStore() {
		boolean result = false;

		if (this.rawTokens.size() == 3) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_RELOAD)) && (this.rawTokens.get(2).equals(CMD_STORE));
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdSwitchStore() {
		boolean result = false;

		if (this.rawTokens.size() == 5) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_SWITCH)) && (this.rawTokens.get(2).equals(CMD_STORE)) && (this.rawTokens.get(3).equals(CMD_TO));

			if (result) {
				this.isValid = true;
				this.cmdTargetStore = stripDelimitingQuotesFrom(this.rawTokens.get(4));
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdBuildSchema() {
		boolean result = false;

		if (this.rawTokens.size() == 4) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_BUILD)) && (this.rawTokens.get(2).equals(CMD_STORE)) && (this.rawTokens.get(3).equals(CMD_SCHEMA));
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdEmptyInstances() {
		boolean result = false;

		if (this.rawTokens.size() == 3) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_EMPTY)) && (this.rawTokens.get(2).equals(CMD_INSTANCES));
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdLoadFromUrl() {
		boolean result = false;

		if (this.rawTokens.size() == 6) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_LOAD)) && (this.rawTokens.get(2).equals(CMD_SENTENCES)) && (this.rawTokens.get(3).equals(CMD_FROM)) && (this.rawTokens.get(4).equals(CMD_URL));
			this.cmdTargetUrlOrFile = stripDelimitingQuotesFrom(this.rawTokens.get(5));	//TODO: Make this better; this property should not be initialised in this "is" method
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdLoadFromUrlIntoSource() {
		boolean result = false;

		if (this.rawTokens.size() == 9) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_LOAD)) && (this.rawTokens.get(2).equals(CMD_SENTENCES)) && (this.rawTokens.get(3).equals(CMD_FROM)) && (this.rawTokens.get(4).equals(CMD_URL)) && (this.rawTokens.get(6).equals(CMD_INTO)) && (this.rawTokens.get(7).equals(CMD_SOURCE));
			this.cmdTargetUrlOrFile = stripDelimitingQuotesFrom(this.rawTokens.get(5));
			this.cmdTargetSourceId = stripDelimitingQuotesFrom(this.rawTokens.get(8));
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdLoadFromFile() {
		boolean result = false;

		if (this.rawTokens.size() == 6) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_LOAD)) && (this.rawTokens.get(2).equals(CMD_SENTENCES)) && (this.rawTokens.get(3).equals(CMD_FROM)) && (this.rawTokens.get(4).equals(CMD_FILE));
			this.cmdTargetUrlOrFile = stripDelimitingQuotesFrom(this.rawTokens.get(5));	//TODO: Make this better; this property should not be initialised in this "is" method
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdLoadFromQuery() {
		boolean result = false;

		if (this.rawTokens.size() == 6) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_LOAD)) && (this.rawTokens.get(2).equals(CMD_SENTENCES)) && (this.rawTokens.get(3).equals(CMD_FROM)) && (this.rawTokens.get(4).equals(CMD_QUERY));
			this.cmdTargetQuery = stripDelimitingQuotesFrom(this.rawTokens.get(5));	//TODO: Make this better; this property should not be initialised in this "is" method
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdDeleteSourceByName() {
		boolean result = false;

		if (this.rawTokens.size() == 8) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_DELETE)) &&
						(this.rawTokens.get(2).equals(CMD_SENTENCES)) &&
						(this.rawTokens.get(3).equals(CMD_FROM)) &&
						(this.rawTokens.get(4).equals(CMD_SOURCES)) &&
						(this.rawTokens.get(5).equals(CMD_WITH)) &&
						(this.rawTokens.get(6).equals(CMD_NAME));
			this.cmdTargetSourceName = stripDelimitingQuotesFrom(this.rawTokens.get(7));	//TODO: Make this better; this property should not be initialised in this "is" method
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdDeleteSourceById() {
		boolean result = false;

		if (this.rawTokens.size() == 8) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_DELETE)) &&
						(this.rawTokens.get(2).equals(CMD_SENTENCES)) &&
						(this.rawTokens.get(3).equals(CMD_FROM)) &&
						(this.rawTokens.get(4).equals(CMD_SOURCE)) &&
						(this.rawTokens.get(5).equals(CMD_WITH)) &&
						(this.rawTokens.get(6).equals(CMD_ID));
			this.cmdTargetSourceId = stripDelimitingQuotesFrom(this.rawTokens.get(7));	//TODO: Make this better; this property should not be initialised in this "is" method
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdDeleteSourceByAgentId() {
		boolean result = false;

		if (this.rawTokens.size() == 9) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_DELETE)) &&
						(this.rawTokens.get(2).equals(CMD_SENTENCES)) &&
						(this.rawTokens.get(3).equals(CMD_FROM)) &&
						(this.rawTokens.get(4).equals(CMD_SOURCES)) &&
						(this.rawTokens.get(5).equals(CMD_WITH)) &&
						(this.rawTokens.get(6).equals(CMD_AGENT)) &&
						(this.rawTokens.get(7).equals(CMD_ID));
			this.cmdTargetSourceAgentId = stripDelimitingQuotesFrom(this.rawTokens.get(8));	//TODO: Make this better; this property should not be initialised in this "is" method
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdLoadUsing() {
		boolean result = false;

		if (this.rawTokens.size() == 5) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(CMD_LOAD)) && (this.rawTokens.get(2).equals(CMD_SENTENCES)) && (this.rawTokens.get(3).equals(CMD_USING));
			this.cmdEmbeddedCe = stripDelimitingQuotesFrom(decodeForCe(this.rawTokens.get(4)));	//TODO: Make this better; this property should not be initialised in this "is" method
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdRunAgent() {
		boolean result = false;

		int tokLen = this.rawTokens.size();
		if (tokLen >= 4) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = ((this.rawTokens.get(1).equals(CMD_RUN)) && (this.rawTokens.get(2).equals(CMD_THE)) && (this.rawTokens.get(tokLen - 2).equals(CMD_NAMED)));
			this.cmdAgentInstName = stripDelimitingQuotesFrom(this.rawTokens.get(tokLen - 1));
			this.cmdAgentConceptName = "";
			String sepChar = "";

			for (int i = 3; i < (tokLen - 2); i++) {
				this.cmdAgentConceptName += sepChar + this.rawTokens.get(i);
				sepChar = " ";
			}
			if (result) {
				this.isValid = true;
			}
		} else {
			result = false;
		}

		return result;
	}

	public boolean isCmdShowNextUidValue() {
		boolean result = false;

		int tokLen = this.rawTokens.size();
		if (tokLen == 5) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = ((this.rawTokens.get(1).equals(CMD_SHOW)) && (this.rawTokens.get(2).equals(CMD_NEXT)) && (this.rawTokens.get(3).equals(CMD_UID)) && (this.rawTokens.get(4).equals(CMD_VALUE)));
		} else {
			result = false;
		}

		return result;
	}

//	public boolean isCmdSetNextUidValue() {
//		boolean result = false;
//
//		int tokLen = this.rawTokens.size();
//		if (tokLen == 7) {
//			//No need to check first token as it must be 'perform' for this to be a command sentence
//			result = ((this.rawTokens.get(1).equals(CMD_SET)) && (this.rawTokens.get(2).equals(CMD_NEXT)) && (this.rawTokens.get(3).equals(CMD_UID)) && (this.rawTokens.get(4).equals(CMD_VALUE)) && (this.rawTokens.get(5).equals(CMD_TO)));
//			this.cmdStartingUid = stripDelimitingQuotesFrom(this.rawTokens.get(6));
//		} else if (tokLen == 8) {
//			//No need to check first token as it must be 'perform' for this to be a command sentence
//			result = ((this.rawTokens.get(1).equals(CMD_SET)) && (this.rawTokens.get(2).equals(CMD_NEXT)) && (this.rawTokens.get(3).equals(CMD_UID)) && (this.rawTokens.get(4).equals(CMD_VALUE)) && (this.rawTokens.get(5).equals(CMD_TO)) && (this.rawTokens.get(6).equals(CMD_NEXT)) && (this.rawTokens.get(7).equals(CMD_AVAIL)));
//			this.cmdStartingUid = UID_NEXTAVAIL;
//		} else {
//			result = false;
//		}
//
//		return result;
//	}

	public boolean isCmdPrepareForCachedCeLoad() {
		boolean result = false;

		int tokLen = this.rawTokens.size();
		if (tokLen == 6) {
			//No need to check first token as it must be 'perform' for this to be a command sentence
			result = ((this.rawTokens.get(1).equals(CMD_PREPARE)) && (this.rawTokens.get(2).equals(CMD_FOR)) && (this.rawTokens.get(3).equals(CMD_CACHED)) && (this.rawTokens.get(4).equals(CMD_CE)) && (this.rawTokens.get(5).equals(CMD_LOAD)));
		} else {
			result = false;
		}

		return result;
	}

	public String getCmdTargetUrl() {
		return this.cmdTargetUrlOrFile;
	}

	public String getCmdTargetFile() {
		return this.cmdTargetUrlOrFile;
	}

	public String getCmdTargetQuery() {
		return this.cmdTargetQuery;
	}

	public String getCmdTargetSourceName() {
		return this.cmdTargetSourceName;
	}

	public String getCmdTargetSourceId() {
		return this.cmdTargetSourceId;
	}

	public String getCmdTargetSourceAgentId() {
		return this.cmdTargetSourceAgentId;
	}

	public String getCmdEmbeddedCe() {
		return this.cmdEmbeddedCe;
	}

	public String getCmdAgentInstName() {
		return this.cmdAgentInstName;
	}

	public String getCmdAgentConceptName() {
		return this.cmdAgentConceptName;
	}

	public String getCmdStartingUid() {
		return this.cmdStartingUid;
	}

	public String getCmdTargetStore() {
		return this.cmdTargetStore;
	}

	public long getCmdNextUid() {
		long result = -1;

		if (!this.cmdStartingUid.equals(UID_NEXTAVAIL)) {
			result = new Long(this.cmdStartingUid).longValue();
		}

		// -1 is returned if "next available" was specified
		return result;
	}
	
	public void executeCommand(ActionContext pAc, ContainerSentenceLoadResult pSenStats) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(pAc);
		
		if (isCmdResetStoreSimple()) {
			pSenStats.updateFrom(sa.resetStore("1", true), true);
		} else if (isCmdResetStoreWithUidStart()) {
			pSenStats.updateFrom(sa.resetStore(getCmdStartingUid(), true), true);
		} else if (isCmdReloadStore()) {
			pSenStats.updateFrom(sa.reloadStore(), true);
		} else if (isCmdSwitchStore()) {
			pAc.switchToStoreNamed(getCmdTargetStore());
		} else if (isCmdEmptyInstances()) {
			pSenStats.updateFrom(sa.emptyInstances(), true);
		} else if (isCmdLoadFromUrl()) {
			pSenStats.updateFrom(sa.loadSentencesFromUrl(getCmdTargetUrl(), null), true);
		} else if (isCmdLoadFromUrlIntoSource()) {
			pSenStats.updateFrom(sa.loadSentencesFromUrl(getCmdTargetUrl(), getCmdTargetSourceId()), true);
		} else if (isCmdLoadFromFile()) {
			pSenStats.updateFrom(sa.loadSentencesFromFile(getCmdTargetFile()), true);
		} else if (isCmdLoadFromQuery()) {
			pSenStats.updateFrom(sa.loadSentencesFromQuery(getCmdTargetQuery()), true);
		} else if (isCmdLoadUsing()) {
			pSenStats.updateFrom(sa.loadSentencesFromForm(getCmdEmbeddedCe(), "cmd", StoreActions.MODE_NORMAL), true);
		} else if (isCmdDeleteSourceByName()) {
			pSenStats.updateFrom(sa.deleteSentencesFromSourceByName(getCmdTargetSourceName()), true);
		} else if (isCmdDeleteSourceById()) {
			pSenStats.updateFrom(sa.deleteSentencesFromSourceById(getCmdTargetSourceId()), true);
		} else if (isCmdDeleteSourceByAgentId()) {
			pSenStats.updateFrom(sa.deleteSentencesFromSourceByAgentId(getCmdTargetSourceAgentId()), true);
		} else if (isCmdRunAgent()) {
			String agentInstName = getCmdAgentInstName();
			String agentConName = getCmdAgentConceptName();
			pSenStats.updateFrom(sa.runAgent(agentInstName, agentConName), true);
		} else if (isCmdShowNextUidValue()) {
			String nextUidValue = sa.showNextUidWithoutIncrementing();
			reportWarning("Next UID value is '" + nextUidValue + "'", pAc);
//		} else if (isCmdSetNextUidValue()) {
//			long nextUidValue = getCmdNextUid();
//
//			if (nextUidValue == -1) {
//				reportWarning("Calculation of next available UID is no longer supported.  Please remove this call", pAc);
//				nextUidValue = pAc.getModelBuilder().calculateNextAvailableUid();
//				if (isReportMicroDebug()) {
//					reportMicroDebug("The next available UID has been calculated as " + nextUidValue, pAc);
//				}
//			}
//			pAc.getModelBuilder().setNextUidValueTo(pAc, nextUidValue);
//			if (isReportMicroDebug()) {
//				reportMicroDebug("Next UID value has been set to '" + nextUidValue + "'", pAc);
//			}
		} else if (isCmdPrepareForCachedCeLoad()) {
			pAc.markAsCachedCeLoading();
			sa.resetStore("1", true);
		} else if (isCmdSaveStore()) {
			PersistenceManagerFactory.get().save(pAc);
		} else if (isCmdLoadStore()) {
			pAc.markAsAutoExecuteRules(false);
			pAc.markAsCachedCeLoading();
			PersistenceManagerFactory.get().load(pAc);
		} else if (isCmdSetValue()) {
			setUserSpecifiedValue(pAc);
		} else {
			reportError("Unknown command encountered during command sentence processing (" + getSentenceText() + ")", pAc);
		}

		//After each command ensure the created sentences and instances are saved
		if (isReportMicroDebug()) {
			reportMicroDebug("Updating created things following CE command completion (" + getSentenceText() + ")", pAc);
		}

		pAc.getModelBuilder().updateCreatedThingsFrom(pAc);		
	}

	@Override
	public boolean isValid() {
		//TODO: Is there a better way? - each of the "is" methods will set the validity, so call each here
		isCmdEmptyInstances();
		isCmdLoadFromFile();
		isCmdLoadFromUrl();
		isCmdLoadFromUrlIntoSource();
		isCmdLoadFromQuery();
		isCmdLoadUsing();
		isCmdDeleteSourceByName();
		isCmdDeleteSourceById();
		isCmdResetStore();
		isCmdReloadStore();
		isCmdRunAgent();
		isCmdShowNextUidValue();
//		isCmdSetNextUidValue();
		isCmdPrepareForCachedCeLoad();
		isCmdSaveStore();
		isCmdLoadStore();
		isCmdSetValue();

		return this.isValid;
	}

	private void setUserSpecifiedValue(ActionContext pAc) {
		String varName = stripDelimitingQuotesFrom(this.rawTokens.get(2)).toLowerCase().trim();
		String varVal = stripDelimitingQuotesFrom(this.rawTokens.get(4)).toLowerCase().trim();

		if (varName.equals(VARNAME_ARR)) {
			if (varVal.equals(VARVAL_TRUE)) {
				pAc.getCeConfig().setAutoRunRules(true);
				pAc.markAsAutoExecuteRules(true);
			} else {
				pAc.getCeConfig().setAutoRunRules(false);
				pAc.markAsAutoExecuteRules(false);
			}
		} else if (varName.equals(VARNAME_MDU)) {
			pAc.getModelBuilder().setModelDirectoryUrl(varVal);
		}
	}

}
