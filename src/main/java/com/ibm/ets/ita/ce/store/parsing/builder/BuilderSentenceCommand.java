package com.ibm.ets.ita.ce.store.parsing.builder;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.SENMODE_NORMAL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_AGENT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DELETE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_EMPTY;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_FILE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_FROM;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_ID;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_INSTANCES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_INTO;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_LOAD;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NAME;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_NAMED;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_QUERY;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_RESET;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_RUN;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SENTENCES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SET;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SOURCE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SOURCES;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_STARTING;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_STORE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_SWITCH;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_THE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_TO;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_UID;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_URL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_USING;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_WITH;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_ARR;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_CER;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_MDU;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_CACHECE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_CASESEN;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_DEBUG;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_DEFCECURRENT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_DEFCEROOT;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_GENPATH;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_ROOTFOLDER;
import static com.ibm.ets.ita.ce.store.names.ParseNames.VARNAME_SAVESENS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_TRUE;
import static com.ibm.ets.ita.ce.store.names.ParseNames.UID_NEXTAVAIL;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.decodeForCe;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.stripDelimitingQuotesFrom;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public class BuilderSentenceCommand extends BuilderSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private boolean isValid = false;
	private String cmdTargetUrlOrFile = null;
	private String cmdTargetQuery = null;
	private String cmdEmbeddedCe = null;
	private String cmdAgentInstName = null;
	private String cmdAgentConceptName = null;
	private String cmdTargetSourceName = null;
	private String cmdTargetSourceId = null;
	private String cmdTargetSourceAgentId = null;
	private String cmdStartingUid = null;
	private String cmdTargetStore = null;

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

	public boolean isCmdSetValue() {
		boolean result = false;

		if (this.rawTokens.size() == 5) {
			// No need to check first token as it must be 'perform' for this to be a command sentence
			result = (this.rawTokens.get(1).equals(TOKEN_SET)) && (this.rawTokens.get(3).equals(TOKEN_TO));
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
			result = (this.rawTokens.get(1).equals(TOKEN_RESET)) && (this.rawTokens.get(2).equals(TOKEN_STORE));
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
			result = (this.rawTokens.get(1).equals(TOKEN_RESET)) && (this.rawTokens.get(2).equals(TOKEN_STORE)) && (this.rawTokens.get(3).equals(TOKEN_WITH)) && (this.rawTokens.get(4).equals(TOKEN_STARTING)) && (this.rawTokens.get(5).equals(TOKEN_UID));

			if (result) {
				this.isValid = true;
				this.cmdStartingUid = stripDelimitingQuotesFrom(this.rawTokens.get(6));
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
			result = (this.rawTokens.get(1).equals(TOKEN_SWITCH)) && (this.rawTokens.get(2).equals(TOKEN_STORE)) && (this.rawTokens.get(3).equals(TOKEN_TO));

			if (result) {
				this.isValid = true;
				this.cmdTargetStore = stripDelimitingQuotesFrom(this.rawTokens.get(4));
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
			result = (this.rawTokens.get(1).equals(TOKEN_EMPTY)) && (this.rawTokens.get(2).equals(TOKEN_INSTANCES));
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
			result = (this.rawTokens.get(1).equals(TOKEN_LOAD)) && (this.rawTokens.get(2).equals(TOKEN_SENTENCES)) && (this.rawTokens.get(3).equals(TOKEN_FROM)) && (this.rawTokens.get(4).equals(TOKEN_URL));
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
			result = (this.rawTokens.get(1).equals(TOKEN_LOAD)) && (this.rawTokens.get(2).equals(TOKEN_SENTENCES)) && (this.rawTokens.get(3).equals(TOKEN_FROM)) && (this.rawTokens.get(4).equals(TOKEN_URL)) && (this.rawTokens.get(6).equals(TOKEN_INTO)) && (this.rawTokens.get(7).equals(TOKEN_SOURCE));
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
			result = (this.rawTokens.get(1).equals(TOKEN_LOAD)) && (this.rawTokens.get(2).equals(TOKEN_SENTENCES)) && (this.rawTokens.get(3).equals(TOKEN_FROM)) && (this.rawTokens.get(4).equals(TOKEN_FILE));
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
			result = (this.rawTokens.get(1).equals(TOKEN_LOAD)) && (this.rawTokens.get(2).equals(TOKEN_SENTENCES)) && (this.rawTokens.get(3).equals(TOKEN_FROM)) && (this.rawTokens.get(4).equals(TOKEN_QUERY));
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
			result = (this.rawTokens.get(1).equals(TOKEN_DELETE)) &&
						(this.rawTokens.get(2).equals(TOKEN_SENTENCES)) &&
						(this.rawTokens.get(3).equals(TOKEN_FROM)) &&
						(this.rawTokens.get(4).equals(TOKEN_SOURCES)) &&
						(this.rawTokens.get(5).equals(TOKEN_WITH)) &&
						(this.rawTokens.get(6).equals(TOKEN_NAME));
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
			result = (this.rawTokens.get(1).equals(TOKEN_DELETE)) &&
						(this.rawTokens.get(2).equals(TOKEN_SENTENCES)) &&
						(this.rawTokens.get(3).equals(TOKEN_FROM)) &&
						(this.rawTokens.get(4).equals(TOKEN_SOURCE)) &&
						(this.rawTokens.get(5).equals(TOKEN_WITH)) &&
						(this.rawTokens.get(6).equals(TOKEN_ID));
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
			result = (this.rawTokens.get(1).equals(TOKEN_DELETE)) &&
						(this.rawTokens.get(2).equals(TOKEN_SENTENCES)) &&
						(this.rawTokens.get(3).equals(TOKEN_FROM)) &&
						(this.rawTokens.get(4).equals(TOKEN_SOURCES)) &&
						(this.rawTokens.get(5).equals(TOKEN_WITH)) &&
						(this.rawTokens.get(6).equals(TOKEN_AGENT)) &&
						(this.rawTokens.get(7).equals(TOKEN_ID));
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
			result = (this.rawTokens.get(1).equals(TOKEN_LOAD)) && (this.rawTokens.get(2).equals(TOKEN_SENTENCES)) && (this.rawTokens.get(3).equals(TOKEN_USING));
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
			result = ((this.rawTokens.get(1).equals(TOKEN_RUN)) && (this.rawTokens.get(2).equals(TOKEN_THE)) && (this.rawTokens.get(tokLen - 2).equals(TOKEN_NAMED)));
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
			pSenStats.updateFrom(sa.loadSentencesFromForm(getCmdEmbeddedCe(), "cmd", SENMODE_NORMAL), true);
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
		isCmdRunAgent();
		isCmdSetValue();

		return this.isValid;
	}

	private void setUserSpecifiedValue(ActionContext pAc) {
		String varName = stripDelimitingQuotesFrom(this.rawTokens.get(2)).toLowerCase().trim();
		String varVal = stripDelimitingQuotesFrom(this.rawTokens.get(4)).toLowerCase().trim();

		if (varName.equals(VARNAME_ARR)) {
			if (varVal.equals(TOKEN_TRUE)) {
				pAc.getCeConfig().setAutoRunRules(true);
				pAc.markAsAutoExecuteRules(true);
			} else {
				pAc.getCeConfig().setAutoRunRules(false);
				pAc.markAsAutoExecuteRules(false);
			}
		} else if (varName.equals(VARNAME_MDU)) {
			pAc.getCeConfig().setModelDirectoryUrl(varVal);
		} else if (varName.equals(VARNAME_CER)) {
			pAc.setCeRoot(varVal);
		} else if (varName.equals(VARNAME_DEBUG)) {
			if (varVal.equals(TOKEN_TRUE)) {
				pAc.getCeConfig().setDebug(true);
			} else {
				pAc.getCeConfig().setDebug(false);
			}
		} else if (varName.equals(VARNAME_CACHECE)) {
			if (varVal.equals(TOKEN_TRUE)) {
				pAc.getCeConfig().setCacheCeText(true);
			} else {
				pAc.getCeConfig().setCacheCeText(false);
			}
		} else if (varName.equals(VARNAME_CASESEN)) {
			if (varVal.equals(TOKEN_TRUE)) {
				pAc.getCeConfig().setCaseSensitive(true);
			} else {
				pAc.getCeConfig().setCaseSensitive(false);
			}
		} else if (varName.equals(VARNAME_DEFCEROOT)) {
			pAc.getCeConfig().setDefaultCeRootUrl(varVal);
		} else if (varName.equals(VARNAME_DEFCECURRENT)) {
			pAc.getCeConfig().setDefaultCeRootUrl(varVal);
		} else if (varName.equals(VARNAME_SAVESENS)) {
			if (varVal.equals(TOKEN_TRUE)) {
				pAc.getCeConfig().setSaveCeSentences(true);
			} else {
				pAc.getCeConfig().setSaveCeSentences(false);
			}
		} else if (varName.equals(VARNAME_ROOTFOLDER)) {
			pAc.getCeConfig().setRootFolder(varVal);
		} else if (varName.equals(VARNAME_GENPATH)) {
			pAc.getCeConfig().setGenPath(varVal);
		}
	}

}
