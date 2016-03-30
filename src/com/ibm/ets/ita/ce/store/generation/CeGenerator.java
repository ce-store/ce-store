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

    protected CeGenerator(ActionContext pAc) {
        super(pAc);
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

	protected String ceDeclarationLong(String pDeterminer, String pConName, String pRawInstName) {
	    StringBuilder sb = new StringBuilder();
		String encInstName = encodeAndEncloseInQuotesIfNeeded(pRawInstName);

		appendToSbNoNl(sb, "there is ");
		appendToSbNoNl(sb, pDeterminer);
		appendToSbNoNl(sb, " ");
		appendToSbNoNl(sb, pConName);
		appendToSbNoNl(sb, " named ");
		appendToSbNoNl(sb, encInstName);

		this.connector = CONN_THAT;
		return sb.toString();
	}

	protected String ceDeclarationShort(String pConName, String pRawInstName) {
        StringBuilder sb = new StringBuilder();
		String encInstName = encodeAndEncloseInQuotesIfNeeded(pRawInstName);

		appendToSbNoNl(sb, "the ");
		appendToSbNoNl(sb, pConName);
		appendToSbNoNl(sb, " ");
		appendToSbNoNl(sb, encInstName);

		this.connector = CONN_BLANK;
        return sb.toString();
	}

	protected String ceAddFnProperty(String pPropName, String pRange, String pRawVal) {
        StringBuilder sb = new StringBuilder();
		String encVal = encodeAndEncloseInQuotesIfNeeded(pRawVal);

		if (!encVal.isEmpty()) {
			appendConnector();
			appendToSbNoNl(sb, INDENT);
			appendToSbNoNl(sb, "has ");
			appendToSbNoNl(sb, appendRangeTextFor(pRange));
			appendToSbNoNl(sb, encVal);
			appendToSbNoNl(sb, " as ");
			appendToSbNoNl(sb, pPropName);
		}
        return sb.toString();
	}

	protected String ceAddVsProperty(String pPropName, String pRange, String pRawVal) {
        StringBuilder sb = new StringBuilder();
		String encVal = encodeAndEncloseInQuotesIfNeeded(pRawVal);

		if (!encVal.isEmpty()) {
			appendConnector();
			appendToSbNoNl(sb, INDENT);
			appendToSbNoNl(sb, pPropName);
			appendToSbNoNl(sb, " ");
            appendToSbNoNl(sb, appendRangeTextFor(pRange));
			appendToSbNoNl(sb, encVal);
		}
        return sb.toString();
	}

	protected String ceSecondaryConcept(String pDeterminer, String pConName) {
        StringBuilder sb = new StringBuilder();
		appendConnector();
		appendToSbNoNl(sb, INDENT);
		appendToSbNoNl(sb, "is ");
		appendToSbNoNl(sb, pDeterminer);
		appendToSbNoNl(sb, " ");
		appendToSbNoNl(sb, pConName);
        return sb.toString();
	}

    protected String ceEndFinalSentence() {
        StringBuilder sb = new StringBuilder();
        appendToSb(sb, ".");
        return sb.toString();
    }

    protected String ceEndSentence() {
        StringBuilder sb = new StringBuilder();
        appendToSb(sb, ".");
        appendToSb(sb, "");    //An extra blank line to separate the next sentence
        return sb.toString();
    }

	protected String ceAnnotationForInvalidCe() {
        StringBuilder sb = new StringBuilder();
		appendToSb(sb, "Note: This CE is invalid.");
        return sb.toString();
	}

	private String appendConnector() {
        StringBuilder sb = new StringBuilder();
		if (this.connector != null) {
			appendToSb(sb, this.connector);
			this.connector = null;
		} else {
			appendToSb(sb, CONN_AND);
		}
        return sb.toString();
	}

	private String appendRangeTextFor(String pRange) {
        StringBuilder sb = new StringBuilder();
		if (pRange != null) {
			appendToSbNoNl(sb, "the ");
			appendToSbNoNl(sb, pRange);
			appendToSbNoNl(sb, " ");
		}
        return sb.toString();
	}

}