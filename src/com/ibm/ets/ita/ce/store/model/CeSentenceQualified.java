package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;

public class CeSentenceQualified extends CeSentence {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final int TIMEFRAME_CURRENTLY = 1;
	private static final int TIMEFRAME_HISTORICALLY = 2;
	private static final int TIMEFRAME_INFUTURE = 3;
	private static final int TIMEFRAME_ALWAYS = 4;

	private static final int TRUTHVALUE_TRUE = 1;
	private static final int TRUTHVALUE_FALSE = 2;
	private static final int TRUTHVALUE_POSSIBLE = 3;
	private static final int TRUTHVALUE_IMPOSSIBLE = 4;

	private static final int STATEMENTTYPE_HYPOTHESISED = 1;
	private static final int STATEMENTTYPE_ASSUMED = 2;
	private static final int STATEMENTTYPE_ASSERTED = 3;
	private static final int STATEMENTTYPE_DECLARED = 4;
	private static final int STATEMENTTYPE_INFERRED = 5;

	//TODO: Check all defaults are correct
	private static final String TIMESTAMP_DEFAULT = "";
	private static final int TIMEFRAME_DEFAULT = TIMEFRAME_CURRENTLY;
	private static final int TRUTHVALUE_DEFAULT = TRUTHVALUE_TRUE;
	private static final int STATEMENTTYPE_DEFAULT = STATEMENTTYPE_ASSERTED;

	private static final String TIMEFRAME_NAME_CURRENTLY = "currently";
	private static final String TIMEFRAME_NAME_HISTORICALLY = "historically";
	private static final String TIMEFRAME_NAME_INFUTURE = "in-future";
	private static final String TIMEFRAME_NAME_ALWAYS = "always";

	private static final String TRUTHVALUE_NAME_TRUE = "true";
	private static final String TRUTHVALUE_NAME_FALSE = "false";
	private static final String TRUTHVALUE_NAME_POSSIBLE = "possible";
	private static final String TRUTHVALUE_NAME_IMPOSSIBLE = "impossible";

	private static final String STATEMENTTYPE_NAME_HYPOTHESISED = "hypothesised";
	private static final String STATEMENTTYPE_NAME_ASSUMED = "assumed";
	private static final String STATEMENTTYPE_NAME_ASSERTED = "asserted";
	private static final String STATEMENTTYPE_NAME_DECLARED = "declared";
	private static final String STATEMENTTYPE_NAME_INFERRED = "inferred";

//	private String innerCeText = "";

	private String qualifiedTimestamp = TIMESTAMP_DEFAULT;
	private int qualifiedTimeframe = TIMEFRAME_DEFAULT;
	private int qualifiedTruthValue = TRUTHVALUE_DEFAULT;
	private int qualifiedStatementType = STATEMENTTYPE_DEFAULT;
	private String qualifiedAuthorCon = null;
	private String qualifiedAuthorName = null;
	private String qualifiedContextCon = null;
	private String qualifiedContextName = null;
	private String generalQualificationCon = null;
	private String generalQualificationName = null;

	private CeSentence innerSentence = null;

	private CeSentenceQualified(ActionContext pAc, CeSource pSource, int pType, int pVal) {
		super(pAc, pSource, pType, pVal);
	}

	public static CeSentenceQualified createValidQualifiedFactSentence(ActionContext pAc, String pSenText, ArrayList<String> pStructuredCeList, CeSource pSource) {
		CeSentenceQualified newSen = new CeSentenceQualified(pAc, pSource, BuilderSentence.SENTYPE_FACT_QUALIFIED, BuilderSentence.SENVAL_VALID);

		newSen.ceText = pSenText;
		newSen.createStructuredCeTextFrom(pStructuredCeList);
		
		return newSen;
	}

	public CeSentence getInnerSentence() {
		return this.innerSentence;
	}
	
	public void setInnerSentence(CeSentence pSen) {
		this.innerSentence = pSen;
		pSen.addQualifiedSentence(this);
	}
	
//	public String getInnerCeText(ActionContext pAc) {
//		return this.innerSentence.getCeText(pAc);
//	}

	public String getGeneralQualificationConceptName() {
		return this.generalQualificationCon;
	}

	public String getGeneralQualificationName() {
		return this.generalQualificationName;
	}

	public String getQualifiedTimestamp() {
		return this.qualifiedTimestamp;
	}
	
	public int getQualifiedTimeframe() {
		return this.qualifiedTimeframe;
	}

	public int getQualifiedTruthValue() {
		return this.qualifiedTruthValue;
	}

	public int getQualifiedStatementType() {
		return this.qualifiedStatementType;
	}

	public String getQualifiedAuthorConceptName() {
		return this.qualifiedAuthorCon;
	}

	public String getQualifiedAuthorName() {
		return this.qualifiedAuthorName;
	}
	
	public String getQualifiedContextConceptName() {
		return this.qualifiedContextCon;
	}

	public String getQualifiedContextName() {
		return this.qualifiedContextName;
	}
	
	public String formattedQualifiedTimeframe() {
		String result = "";
		
		switch (this.qualifiedTimeframe) {
		case TIMEFRAME_CURRENTLY:
			result = TIMEFRAME_NAME_CURRENTLY;
			break;
		case TIMEFRAME_HISTORICALLY:
			result = TIMEFRAME_NAME_HISTORICALLY;
			break;
		case TIMEFRAME_INFUTURE:
			result = TIMEFRAME_NAME_INFUTURE;
			break;
		case TIMEFRAME_ALWAYS:
			result = TIMEFRAME_NAME_ALWAYS;
			break;
		default:
			result = "UNKNOWN TIMEFRAME (" + Integer.toString(this.qualifiedTimeframe) + ")";
			break;
		}
		
		return result;
	}

	public String formattedQualifiedTruthValue() {
		String result = "";
		
		switch (this.qualifiedTruthValue) {
		case TRUTHVALUE_TRUE:
			result = TRUTHVALUE_NAME_TRUE;
			break;
		case TRUTHVALUE_FALSE:
			result = TRUTHVALUE_NAME_FALSE;
			break;
		case TRUTHVALUE_POSSIBLE:
			result = TRUTHVALUE_NAME_POSSIBLE;
			break;
		case TRUTHVALUE_IMPOSSIBLE:
			result = TRUTHVALUE_NAME_IMPOSSIBLE;
			break;
		default:
			result = "UNKNOWN TRUTHVALUE (" + Integer.toString(this.qualifiedTruthValue) + ")";
			break;
		}
		
		return result;
	}
	
	public String formattedQualifiedStatementType() {
		String result = "";
		
		switch (this.qualifiedStatementType) {
		case STATEMENTTYPE_HYPOTHESISED:
			result = STATEMENTTYPE_NAME_HYPOTHESISED;
			break;
		case STATEMENTTYPE_ASSUMED:
			result = STATEMENTTYPE_NAME_ASSUMED;
			break;
		case STATEMENTTYPE_ASSERTED:
			result = STATEMENTTYPE_NAME_ASSERTED;
			break;
		case STATEMENTTYPE_DECLARED:
			result = STATEMENTTYPE_NAME_DECLARED;
			break;
		default:
			result = "UNKNOWN STATEMENTTYPE (" + Integer.toString(this.qualifiedStatementType) + ")";
			break;
		}
		
		return result;
	}

	public void setGeneralQualification(CeConcept pCon, String pInstName) {
		if (pCon != null) {
			this.generalQualificationCon = pCon.getConceptName();
		}
		
		this.generalQualificationName = pInstName;
	}
	
	public void setQualifiedAuthor(CeConcept pCon, String pInstName) {
		if (pCon != null) {
			this.qualifiedAuthorCon = pCon.getConceptName();
		}
		
		this.qualifiedAuthorName = pInstName;
	}
	
	public void setQualifiedContext(CeConcept pCon, String pInstName) {
		if (pCon != null) {
			this.qualifiedContextCon = pCon.getConceptName();
		}
		
		this.qualifiedContextName = pInstName;
	}
	
	public void setStatementType(String pVal) {
		int stVal = STATEMENTTYPE_DEFAULT;
		
		if (pVal.equals(STATEMENTTYPE_NAME_ASSERTED)) {
			stVal = STATEMENTTYPE_ASSERTED;
		} else if (pVal.equals(STATEMENTTYPE_NAME_ASSUMED)) {
			stVal = STATEMENTTYPE_ASSUMED;
		} else if (pVal.equals(STATEMENTTYPE_NAME_DECLARED)) {
			stVal = STATEMENTTYPE_DECLARED;
		} else if (pVal.equals(STATEMENTTYPE_NAME_HYPOTHESISED)) {
			stVal = STATEMENTTYPE_HYPOTHESISED;
		} else if (pVal.equals(STATEMENTTYPE_NAME_INFERRED)) {
			stVal = STATEMENTTYPE_INFERRED;
		} else {
			//Unexpected statement type
		}
		
		this.qualifiedStatementType = stVal;
	}

	public void setTruthValue(String pVal) {
		int tVal = TRUTHVALUE_DEFAULT;
		
		if (pVal.equals(TRUTHVALUE_NAME_FALSE)) {
			tVal = TRUTHVALUE_FALSE;
		} else if (pVal.equals(TRUTHVALUE_NAME_IMPOSSIBLE)) {
			tVal = TRUTHVALUE_IMPOSSIBLE;
		} else if (pVal.equals(TRUTHVALUE_NAME_POSSIBLE)) {
			tVal = TRUTHVALUE_POSSIBLE;
		} else if (pVal.equals(TRUTHVALUE_NAME_TRUE)) {
			tVal = TRUTHVALUE_TRUE;
		} else {
			//Unexpected truth value
		}
		
		this.qualifiedStatementType = tVal;
	}

	public void setTimeframe(String pVal) {
		int tfVal = TIMEFRAME_DEFAULT;
		
		if (pVal.equals(TIMEFRAME_NAME_ALWAYS)) {
			tfVal = TIMEFRAME_ALWAYS;
		} else if (pVal.equals(TIMEFRAME_NAME_CURRENTLY)) {
			tfVal = TIMEFRAME_CURRENTLY;
		} else if (pVal.equals(TIMEFRAME_NAME_HISTORICALLY)) {
			tfVal = TIMEFRAME_HISTORICALLY;
		} else if (pVal.equals(TIMEFRAME_NAME_INFUTURE)) {
			tfVal = TIMEFRAME_INFUTURE;
		} else {
			//Unexpected timeframe
		}
		
		this.qualifiedTimeframe = tfVal;
	}
	
	public void setTimestamp(String pVal) {
		this.qualifiedTimestamp = pVal;
	}
	
	@Override
	public boolean isQualified() {
		return true;
	}

}