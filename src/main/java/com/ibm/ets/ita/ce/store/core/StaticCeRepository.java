package com.ibm.ets.ita.ce.store.core;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.encodeForCe;

public abstract class StaticCeRepository {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	protected static void ceMetamodel(StringBuilder pSb) {
		appendToSb(pSb, "Model:   Meta model");
		appendToSb(pSb, "Version: 1.4");
		appendToSb(pSb, "Date:    6th February 2015");
		appendToSb(pSb, "Author:  IBM");

		appendToSb(pSb, "conceptualise a ~ thing ~ T that");
		appendToSb(pSb, "  has the value D as ~ description ~ and");
		appendToSb(pSb, "  ~ is related to ~ the thing T2 and");
		appendToSb(pSb, "  ~ is the same as ~ the thing T3.");

		appendToSb(pSb, "conceptualise a ~ meaning ~ M that");
		appendToSb(pSb, "  ~ conceptualises ~ the thing T and");
		appendToSb(pSb, "  ~ means the same as ~ the meaning M2.");

		appendToSb(pSb, "conceptualise a ~ symbol ~ S that");
		appendToSb(pSb, "  ~ expresses ~ the meaning M and");
		appendToSb(pSb, "  ~ stands for ~ the thing T.");

		appendToSb(pSb, "conceptualise a ~ statement ~ S.");

		appendToSb(pSb, "conceptualise a ~ sequence ~ S.");

		appendToSb(pSb, "conceptualise a ~ concept ~ C that");
		appendToSb(pSb, "  is a meaning and");
		appendToSb(pSb, "  has the value A as ~ annotation ~.");

		appendToSb(pSb, "conceptualise an ~ entity concept ~ E that");
		appendToSb(pSb, "  is a concept and");
		appendToSb(pSb, "  has the entity concept S as ~ sub-concept ~.");

		appendToSb(pSb, "conceptualise a ~ property concept ~ P that");
		appendToSb(pSb, "  is a concept and");
		appendToSb(pSb, "  has the entity concept D as ~ domain ~ and");
		appendToSb(pSb, "  has the entity concept R as ~ range ~ and");
		appendToSb(pSb, "  has the value N as ~ property name ~.");

		appendToSb(pSb, "conceptualise an ~ attribute concept ~ A that");
		appendToSb(pSb, "  is a property concept.");

		appendToSb(pSb, "conceptualise a ~ relation concept ~ R that");
		appendToSb(pSb, "  is a property concept.");

		appendToSb(pSb, "conceptualise a ~ datatype property ~ D that");
		appendToSb(pSb, "  is a property concept.");

		appendToSb(pSb, "conceptualise an ~ object property ~ O that");
		appendToSb(pSb, "  is a property concept.");

		appendToSb(pSb, "conceptualise a ~ conceptual model ~ M that");
		appendToSb(pSb, "  ~ defines ~ the concept C.");

		appendToSb(pSb, "Note: When referring to instances created within the CE environment (e.g. particular people or places) please use the term 'instance'");
	}

	/*
	 *  Define a new property concept, using the appropriate subclasses depending on whether the property was declared using
	 *  functional-noun or verb-singular CE, and whether the range is a value or thing, using CE of the form :
	 *   there is a property concept named 'propertyInstance' that is (an attribute concept | a relation concept) and is
	 *   (a datatype property | an object property) and has the entity concept 'domainInstance' as domain and has the entity concept
	 *   'rangeInstance' as range and has 'propertyName' as property name.
	 */
	public static void ceMetamodelProperty(StringBuilder pSb, boolean pIsObjProp, String pParm1, String pParm2, String pParm3, String pParm4, String pParm5, String pParm6) {
		appendToSbNoNl(pSb, "there is a property concept named '");
		appendToSbNoNl(pSb, pParm1);	//Property names do not need to be encoded
		appendToSb(pSb,  "' that");

		appendToSbNoNl(pSb, "  is ");
		appendToSbNoNl(pSb, pParm2);	//Does not need to be encoded
		appendToSb(pSb, " and");
		
		appendToSbNoNl(pSb, "  is ");
		appendToSbNoNl(pSb, pParm3);	//Does not need to be encoded
		appendToSb(pSb, " and");
		
		appendToSbNoNl(pSb, "  has the entity concept '");
		appendToSbNoNl(pSb, pParm4);	//Concept names do not need to be encoded
		appendToSb(pSb, "' as domain and");

		if (pIsObjProp) {
			appendToSbNoNl(pSb, "  has the entity concept '");
			appendToSbNoNl(pSb, pParm5);	//Concept names do not need to be encoded
			appendToSb(pSb, "' as range and");
		}

		appendToSbNoNl(pSb, "  has '");
		appendToSbNoNl(pSb, pParm6);	//Property names do not need to be encoded
		appendToSb(pSb, "' as property name.");
	}

	public static void ceMetamodelEntityConceptMain(StringBuilder pSb, String pParm1) {
		appendToSbNoNl(pSb, "there is an entity concept named '");
		appendToSbNoNl(pSb, pParm1);	//Concept names do not need to be encoded
		appendToSb(pSb, "'.");
	}

	public static void ceMetamodelEntityConceptChild(StringBuilder pSb, String pParm1, String pParm2) {
		appendToSbNoNl(pSb, "the entity concept '");
		appendToSbNoNl(pSb, pParm1);	//Concept names do not need to be encoded
		appendToSb(pSb, "'");

		appendToSbNoNl(pSb, "  has the entity concept '");
		appendToSbNoNl(pSb, pParm2);	//Concept names do not need to be encoded
		appendToSb(pSb, "' as sub-concept.");
	}

	public static void ceMetamodelConceptualModel(StringBuilder pSb, String pParm1) {
		appendToSbNoNl(pSb, "there is a conceptual model named '");
		appendToSbNoNl(pSb, pParm1);	//Conceptual model names do not need to be encoded
		appendToSb(pSb, "'.");
	}

	public static void ceMetamodelEntityConceptAnnotation(StringBuilder pSb, String pParm1, String pParm2) {
		appendToSbNoNl(pSb, "the entity concept '");
		appendToSbNoNl(pSb, pParm1);	//Concept names do not need to be encoded
		appendToSb(pSb, "'");

		appendToSbNoNl(pSb, "  has '");
		appendToSbNoNl(pSb, encodeForCe(pParm2));
		appendToSb(pSb, "' as annotation.");
	}

	public static void ceMetamodelConceptualModelDefinesConcept(StringBuilder pSb, String pParm1, String pParm2) {
		appendToSbNoNl(pSb, "the conceptual model '");
		appendToSbNoNl(pSb, pParm1);	//Conceptual model names do not need to be encoded
		appendToSb(pSb, "'");

		appendToSbNoNl(pSb, "  defines the concept '");
		appendToSbNoNl(pSb, pParm2);	//Concept names do not need to be encoded
		appendToSb(pSb, "'.");
	}

}
