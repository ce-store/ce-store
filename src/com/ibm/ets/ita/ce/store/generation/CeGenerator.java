package com.ibm.ets.ita.ce.store.generation;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.encodeAndEncloseInQuotesIfNeeded;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;

public class CeGenerator extends GeneralGenerator {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CONN_THAT = " that";	//Used after long declaration
	private static final String CONN_BLANK = "";		//Used after short declaration
	private static final String CONN_AND = " and";		//All other cases
	private static final String INDENT = "  ";

	protected static final String CON_THING = "thing";
	protected static final String CON_ENTCON = "entity concept";
	protected static final String CON_RELCON = "relation concept";

	private String connector = null;
	protected StringBuilder sb = null;

	protected CeGenerator(ActionContext pAc, StringBuilder pSb) {
		super(pAc);
		
		this.sb = pSb;
	}

	public static String getNewUidFor(ActionContext pAc, CeConcept pCon) {
		String uidPrefix = null;

		if (pCon != null) {
			//Get the lowercase first character of the concept name
			uidPrefix = pCon.getConceptName().substring(0, 1).toLowerCase();
		} else {
			uidPrefix = "";
		}

		return pAc.getModelBuilder().getNextUid(pAc, uidPrefix);
	}

	protected void ceDeclarationLong(String pDeterminer, String pConName, String pRawInstName) {
		String encInstName = encodeAndEncloseInQuotesIfNeeded(pRawInstName);

		appendToSbNoNl(this.sb, "there is ");
		appendToSbNoNl(this.sb, pDeterminer);
		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, pConName);
		appendToSbNoNl(this.sb, " named ");
		appendToSbNoNl(this.sb, encInstName);

		this.connector = CONN_THAT;
	}
	
	protected void ceDeclarationShort(String pConName, String pRawInstName) {
		String encInstName = encodeAndEncloseInQuotesIfNeeded(pRawInstName);

		appendToSbNoNl(this.sb, "the ");
		appendToSbNoNl(this.sb, pConName);
		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, encInstName);

		this.connector = CONN_BLANK;
	}

	protected void ceAddFnProperty(String pPropName, String pRange, String pRawVal) {
		String encVal = encodeAndEncloseInQuotesIfNeeded(pRawVal);

		appendConnector();
		appendToSbNoNl(this.sb, INDENT);
		appendToSbNoNl(this.sb, "has ");
		appendRangeTextFor(pRange);
		appendToSbNoNl(this.sb, encVal);
		appendToSbNoNl(this.sb, " as ");
		appendToSbNoNl(this.sb, pPropName);
	}

	protected void ceAddVsProperty(String pPropName, String pRange, String pRawVal) {
		String encVal = encodeAndEncloseInQuotesIfNeeded(pRawVal);

		appendConnector();
		appendToSbNoNl(this.sb, INDENT);
		appendToSbNoNl(this.sb, pPropName);
		appendToSbNoNl(this.sb, " ");
		appendRangeTextFor(pRange);
		appendToSbNoNl(this.sb, encVal);
	}
	
	protected void ceSecondaryConcept(String pDeterminer, String pConName) {
		appendConnector();
		appendToSbNoNl(this.sb, INDENT);
		appendToSbNoNl(this.sb, "is ");
		appendToSbNoNl(this.sb, pDeterminer);
		appendToSbNoNl(this.sb, " ");
		appendToSbNoNl(this.sb, pConName);
	}

	protected void ceEndSentence() {
		appendToSb(this.sb, ".");
		appendToSb(this.sb, "");	//An extra blank line to separate the next sentence
	}

	protected void ceAnnotationForInvalidCe() {
		appendToSb(this.sb, "Note: This CE is invalid.");
	}

	private void appendConnector() {
		if (this.connector != null) {
			appendToSb(this.sb, this.connector);
			this.connector = null;
		} else {
			appendToSb(this.sb, CONN_AND);
		}
	}

	private void appendRangeTextFor(String pRange) {
		if (pRange != null) {
			appendToSbNoNl(this.sb, "the ");
			appendToSbNoNl(this.sb, pRange);
			appendToSbNoNl(this.sb, " ");
		}
	}

}