package com.ibm.ets.ita.ce.store.conversation.generator;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSbNoNl;

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;
import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.MatchedTriple;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.conversation.processor.SentenceProcessor;
import com.ibm.ets.ita.ce.store.generation.CeGenerator;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class DomainCeGenerator extends CeGenerator {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_GROUP = "group";
	private static final String CON_QUANTITY = "quantity";
	private static final String CON_QUALIFIER = "qualifier";

	private static final String PROP_DESC = "description";
	private static final String PROP_MEMBER = "member";
	private static final String PROP_QUAL = "qualifier";
	private static final String PROP_NUMVAL = "numeric value";
	private static final String PROP_SIZE = "size";

	private static final String UID_PREFIX_GROUP = "g";
	
	private SentenceProcessor sp = null;
	private HashSet<String> processedUids = new HashSet<String>();

	private DomainCeGenerator(ActionContext pAc, SentenceProcessor pSp, StringBuilder pSb) {
		super(pAc, pSb);
		
		this.sp = pSp;
	}

	public static String generateCeFor(ActionContext pAc, SentenceProcessor pSp) {
		StringBuilder sb = new StringBuilder();

		DomainCeGenerator cg = new DomainCeGenerator(pAc, pSp, sb);
		cg.calculateCeText();

		return sb.toString();
	}

	private void calculateCeText() {
		calculateConceptLedTriples();
		calculateOtherConceptsCe();

		ceForUnprocessedWords();

		testCeForValidity();
	}

	private boolean matchesSubjectConcept(CeConcept pCon) {
		return pCon.equals(this.sp.getSubjectConcept());
	}

	private String calculateUidBasedOnSubjectConcept() {
		String newUid = null;

		if (getSubjectEi().isPluralConceptMatch()) {
			newUid = UID_PREFIX_GROUP + getNewUidFor(this.ac, null);
		} else {
			newUid = getNewUidFor(this.ac, this.sp.getSubjectConcept());
		}

		return newUid;
	}

	private void calculateConceptLedTriples() {
		HashSet<CeConcept> tgtCons = computeUniqueLeafConcepts();
		String newUid = null;

		for (CeConcept thisCon : tgtCons) {
			newUid = conceptLedProcessingFor(thisCon, tgtCons, newUid);
		}
	}
	
	private String conceptLedProcessingFor(CeConcept pCon, HashSet<CeConcept> pAllCons, String pUid) {
		String thisUid = pUid;
		boolean newInst = false;

		if (thisUid == null) {
			if (this.sp.getSubjectInstance() != null) {
				//There is a subject instance so use that instance name
				thisUid = this.sp.getSubjectInstance().getInstanceName();
			} else {
				//Compute a new instance name based on the subject concept
				thisUid = calculateUidBasedOnSubjectConcept();
				newInst = true;
			}
		}

		if (matchesSubjectConcept(pCon)) {
			boolean suppressDescription = shouldDescriptionBeIgnored(pCon);
			ceForTriplesMatchingConcept(pCon, thisUid, suppressDescription, newInst);
			doOtherConProcessing(pAllCons);
			ceEndSentence();

			processTriplesForExtraInstanceDefinitions(pCon);			
		} else {
			ceForTriplesMatchingConcept(pCon, thisUid, false, newInst);
			ceEndSentence();
		}

		this.processedUids.add(thisUid);

		return thisUid;
	}

	private ArrayList<ExtractedItem> getSubjectEiList() {
		return this.sp.getSubjectWord().getExtractedItems();
	}

	private ExtractedItem getSubjectEi() {
		ExtractedItem result = null;
		ArrayList<ExtractedItem> eiList = getSubjectEiList();
		
		//TODO: Is it sufficient to just get the first?
		if ((eiList != null) && (!eiList.isEmpty())) {
			result = eiList.get(0);
		}
		
		return result;
	}

	private void calculateOtherConceptsCe() {
		for (ExtractedItem thisEi : this.sp.getOtherConcepts()) {
			if (thisEi.isConceptItem()) {
				CeConcept thisCon = thisEi.getConcept();

				if (thisCon != null) {
					if (noTripleHasRange(thisCon)) {
						ceDeclarationLong(thisCon.conceptQualifier(), thisCon.getConceptName(), getNewUidFor(this.ac, thisCon));

						String wordsUsed = thisEi.formattedItemName();
						if (!thisCon.getConceptName().equals(wordsUsed)) {
							ceAddFnProperty(PROP_DESC, null, wordsUsed);
						}

						ceEndSentence();
					}
				}
			}
		}
	}

	protected HashSet<CeConcept> computeUniqueLeafConcepts() {
		HashSet<CeConcept> uniqueCons = this.sp.computeUniqueConcepts();
		HashSet<CeConcept> filteredCons = filterOutParentsFrom(uniqueCons);

		return filteredCons;
	}

	protected HashSet<CeInstance> computeUniqueInstances() {
		HashSet<CeInstance> uniqueInsts = new HashSet<CeInstance>();

		for (ProcessedWord thisPw : this.sp.getAllProcessedWords()) {
			if (thisPw.getChosenInstance() != null) {
				uniqueInsts.add(thisPw.getChosenInstance());
			} else {
				for (CeInstance thisInst : thisPw.listGroundedInstances()) {
					uniqueInsts.add(thisInst);
				}
			}
		}

		for (MatchedTriple thisMt : this.sp.getMatchedTriples()) {
			if (thisMt.isSubjectInstance()) {
				uniqueInsts.add(thisMt.getSubjectInstance());
			}
		}

		return uniqueInsts;
	}

	protected static HashSet<CeConcept> filterOutParentsFrom(HashSet<CeConcept> pCons) {
		HashSet<CeConcept> result = new HashSet<CeConcept>();

		for (CeConcept outerCon : pCons) {
			boolean foundChild = false;

			for (CeConcept innerCon : pCons) {
				if (!innerCon.equals(outerCon)) {
					if (innerCon.hasParent(outerCon)) {
						foundChild = true;
					}
				}
			}

			if (!foundChild) {
				result.add(outerCon);
			}
		}

		return result;
	}

	protected boolean noTripleHasRange(CeConcept pCon) {
		for (MatchedTriple thisMt : this.sp.getMatchedTriples()) {
			if (thisMt.isObjectInstance()) {
				String ocName = thisMt.getObjectConceptName();
				if (ocName != null) {
					CeConcept ocCon = this.ac.getModelBuilder().getConceptNamed(this.ac, ocName);

					if (ocCon != null) {
						if (pCon.equalsOrHasParent(ocCon)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	private void ceForTriplesMatchingConcept(CeConcept pTgtCon, String pUid, boolean pNoDesc, boolean pNewInst) {
		computeStartingTextFor(pUid, pTgtCon);
		restOfStartingText(pTgtCon, pNoDesc, pNewInst);

		processTriplesForMainSentence(pTgtCon);
	}

	private void ceForUnprocessedWords() {
		boolean firstTime = true;

		for (ProcessedWord thisPw : this.sp.getAllProcessedWords()) {
			if (thisPw.isUnmatchedWord()) {
				if (firstTime) {
					appendToSbNoNl(this.sb, "Note: The following words were not matched:");
					firstTime = false;
				}

				appendToSbNoNl(this.sb, " ");
				appendToSbNoNl(this.sb, thisPw.getWordText());
			}
		}

		if (!firstTime) {
			ceEndSentence();
		}
	}

	private void processTriplesForMainSentence(CeConcept pTgtCon) {
		for (MatchedTriple thisMt : this.sp.getMatchedTriples()) {
			if (!thisMt.isProcessed()) {
				boolean matched = testForMatch(pTgtCon, thisMt);

				if (matched) {
					String predName = thisMt.getPredicateProperty().getPropertyName();
					String objVal = "";

					if (thisMt.isObjectInstance()) {
						objVal = thisMt.getObjectInstanceId();
					} else {
						objVal = thisMt.getObjectValue();
					}

					if (thisMt.getPredicateProperty().isFunctionalNoun()) {
						ceAddFnProperty(predName, thisMt.getObjectConceptName(), objVal);
					} else {
						ceAddVsProperty(predName, thisMt.getObjectConceptName(), objVal);
					}

					thisMt.markAsProcessed();
				}
			}
		}
	}

	private void processTriplesForExtraInstanceDefinitions(CeConcept pTgtCon) {
		for (MatchedTriple thisMt : this.sp.getMatchedTriples()) {
			boolean matched = testForMatch(pTgtCon, thisMt);

			if (matched) {
				if (thisMt.isNewInstance()) {
					generateInstanceDefinitionCeFor(thisMt);
				}
			}
		}
	}

	private boolean testForMatch(CeConcept pTgtCon, MatchedTriple pMt) {
		boolean result = pTgtCon.equalsOrHasParent(pMt.getSubjectConcept());

		if (!result) {
			result = getSubjectEi().isPluralOrGroup();
		}

		return result;
	}

	private boolean shouldDescriptionBeIgnored(CeConcept pTgtCon) {
		boolean result = false;

		for (MatchedTriple thisMt : this.sp.getMatchedTriples()) {
			if (!thisMt.isProcessed()) {
				boolean matched = testForMatch(pTgtCon, thisMt);

				if (matched) {
					if (thisMt.getSubjectInstance() != null) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	private void computeStartingTextFor(String pTgtUid, CeConcept pTgtCon) {
		String tgtConName = getSubjectConceptNameFor(pTgtCon);
		boolean useShortDec = useShortDeclaration(pTgtUid);
		
		if (useShortDec) {
			ceDeclarationShort(tgtConName, pTgtUid);
		} else {
			ceDeclarationLong(pTgtCon.conceptQualifier(), tgtConName, pTgtUid);
		}
	}

	private boolean useShortDeclaration(String pTgtUid) {
		boolean result = false;

		//First check if an existing instance has already been matched
		result = hasTripleMatchedToExistingInstance();

		if (!result) {
			//Next check if this instance has already had CE generated in this unit of work
			result = this.processedUids.contains(pTgtUid);
		}

		return result;
	}
	
	private String getSubjectConceptNameFor(CeConcept pTgtCon) {
		String result = null;

		if (getSubjectEi().isPluralOrGroup()) {
			result = CON_GROUP;
		} else {
			result = pTgtCon.getConceptName();
		}

		return result;
	}

	private void restOfStartingText(CeConcept pTgtCon, boolean pSuppressDescription, boolean pNewInst) {
		if (!pSuppressDescription) {
			ExtractedItem ei = getSubjectEi();

			if (pNewInst) {
				//A new instance so the description should be the words that were provided that describe that instance
				if (ei.hasMeaningfulDescription(this.ac)) {
					ceAddFnProperty(PROP_DESC, null, ei.getOriginalDescription());
				}
			} else {
				//Not a new instance - get any description from the extracted item
				if (ei.hasMeaningfulDescription(this.ac)) {
					ceAddFnProperty(PROP_DESC, null, ei.getOriginalDescription());
				}
			}
		}

		if (getSubjectEi().isPluralOrGroup()) {
			postProcessingForGroup(pTgtCon);
		}
	}

	private boolean hasTripleMatchedToExistingInstance() {
		MatchedTriple firstMt = this.sp.getFirstMatchedTriple();

		//TODO: Is it sufficient to just test the first triple?
		return (firstMt != null) && (firstMt.getSubjectInstance() != null);
	}

	private void postProcessingForGroup(CeConcept pCon) {
		String conName = pCon.getConceptName();

		ceAddFnProperty(PROP_MEMBER, CON_ENTCON, conName);

		tryToAddGroupSize();
		tryToAddGroupQualifier();
	}

	private void doOtherConProcessing(HashSet<CeConcept> pOtherCons) {
		if (pOtherCons != null) {
			for (CeConcept oCon : pOtherCons) {
				if (!this.sp.getSubjectConcept().equalsOrHasParent(oCon)) {
					if (!getSubjectEi().isPluralOrGroup()) {
						ceSecondaryConcept(oCon.conceptQualifier(), oCon.getConceptName());
					}
				}
			}
		}
	}
	
	private void tryToAddGroupSize() {
		for (ProcessedWord thisPw : this.sp.getAllProcessedWords()) {
			//Try for instances of number
			if (thisPw.refersToInstanceOfConceptNamed(this.ac, CON_QUANTITY)) {
				CeInstance tgtInst = thisPw.getMatchingInstance();

				ceAddFnProperty(PROP_SIZE, null, tgtInst.getSingleValueFromPropertyNamed(PROP_NUMVAL));
			}

			//Try for numeric values
			if (thisPw.isNumberWord()) {
				ceAddFnProperty(PROP_SIZE, null, thisPw.getLcWordText());
			}
		}
	}

	private void tryToAddGroupQualifier() {
		for (ProcessedWord thisPw : this.sp.getAllProcessedWords()) {
			if (thisPw.refersToInstanceOfConceptNamed(this.ac, CON_QUALIFIER)) {
				CeInstance tgtInst = thisPw.getMatchingInstance();

				ceAddFnProperty(PROP_QUAL, tgtInst.getFirstLeafConceptName(), tgtInst.getInstanceName());
			}
		}
	}

	private void testCeForValidity() {
		String ceText = this.sb.toString();

		if (!ceText.trim().isEmpty()) {
			if (!ModelBuilder.isThisCeValid(this.ac, ceText)) {
				ceAnnotationForInvalidCe();
			}
		}
	}

	private void generateInstanceDefinitionCeFor(MatchedTriple thisMt) {
		ceDeclarationLong(thisMt.getDeterminerForObjectConcept(this.ac), thisMt.getObjectConceptName(), thisMt.getObjectInstanceId());

		ExtractedItem thisEi = thisMt.getExtractedItem();

		if (thisEi != null) {
			//TODO: Should this be next or previous item?  It's probably contextual
			ceAddFnProperty(PROP_DESC, null, thisEi.getOriginalDescriptionForNextItem());
		}

		ceEndSentence();
	}

}