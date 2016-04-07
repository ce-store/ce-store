package com.ibm.ets.ita.ce.store.conversation.generator;
/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ConvClause;
import com.ibm.ets.ita.ce.store.conversation.model.ConvItem;
import com.ibm.ets.ita.ce.store.conversation.model.ConvSentence;
import com.ibm.ets.ita.ce.store.conversation.model.ConvText;
import com.ibm.ets.ita.ce.store.conversation.model.ConvWord;
import com.ibm.ets.ita.ce.store.conversation.model.ExtractedItem;
import com.ibm.ets.ita.ce.store.conversation.model.MatchedTriple;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.conversation.processor.SentenceProcessor;
import com.ibm.ets.ita.ce.store.generation.CeGenerator;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class ConvCeGenerator extends CeGenerator {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String CON_MT = "matched triple";
	private static final String CON_MDT = "matched datatype triple";
	private static final String CON_MOT = "matched object triple";
	private static final String CON_CONVEXI = "conv extracted item";
//	private static final String CON_COMWORD = "common word";
	private static final String CON_QUESWORD = "question word";
	private static final String CON_STDWORD = "standard word";
	private static final String CON_PVWORD = "possible value word";
	private static final String CON_NUMWORD = "number word";

	protected ConvCeGenerator(ActionContext pAc, StringBuilder pSb) {
		super(pAc);
	}

	public static void generateConvCeFor(ActionContext pAc, SentenceProcessor pSp, ConvText pCt, StringBuilder pSb) {
		ConvCeGenerator ccg = new ConvCeGenerator(pAc, pSb);

		ccg.generateConvCeForText(pCt);

		for (ProcessedWord thisWord : pSp.getAllProcessedWords()) {
			ccg.generateConvCeForProcessedWord(thisWord);
		}
	}

	private void generateConvCeForProcessedWord(ProcessedWord pPw) {
		ceDeclarationLong(pPw.getDeterminer(), pPw.getConceptName(), pPw.getId());

		wordCeForAdditionalTypes(pPw);

		wordCeForMatchingConcept(pPw);
		wordCeForMatchingRelation(pPw);
		wordCeForMatchingInstance(pPw);

		wordCeForReferringConcepts(pPw);
		wordCeForReferringConceptsPlural(pPw);
		wordCeForReferringRelations(pPw);
		wordCeForReferringInstances(pPw);
		wordCeForReferringInstancesPlural(pPw);

		if (pPw.getExtractedItems() != null) {
			for (ExtractedItem thisEi : pPw.getExtractedItems()) {
				wordCeForExtractedItem(thisEi);
			}
		}

		ceEndSentence();

		if (pPw.getExtractedItems() != null) {
			//TODO: Suppress duplicate generation if possible
			//(extracted items may have already been generated if linked to other processed words)
			for (ExtractedItem thisEi : pPw.getExtractedItems()) {
				if (thisEi != null) {
					generateConvCeForExtractedItem(thisEi);
				}
			}
		}
	}

	private void generateConvCeForExtractedItem(ExtractedItem pEi) {
		ProcessedWord sw = pEi.getStartWord();

		ceDeclarationLong(ConvItem.getDeterminer(), CON_CONVEXI, pEi.getId());
		ceAddFnProperty("item text", null, pEi.calculateItemText());
		ceAddFnProperty("start word", sw.getConceptName(), sw.getId());

		for (MatchedTriple mt : pEi.getMatchedTriples()) {
			ceAddFnProperty("matched triple", CON_MT, mt.getId());
		}

		ProcessedWord dw = pEi.getDeterminerWord();
		if (dw != null) {
			ceAddFnProperty("determiner word", dw.getConceptName(), dw.getId());
		}

		for (ProcessedWord ow : pEi.getOtherWords()) {
			ceAddFnProperty("other word", ow.getConceptName(), ow.getId());
		}

		CeConcept eiCon = pEi.getConcept();
		if (eiCon != null) {
			ceAddFnProperty("identified concept", CON_ENTCON, eiCon.getConceptName());
		}

		CeProperty eiProp = pEi.getFirstProperty();
		if (eiProp != null) {
			ceAddFnProperty("identified property", CON_RELCON, eiProp.formattedFullPropertyName());
		}

		CeInstance eiInst = pEi.getInstance();
		if (eiInst != null) {
			ceAddFnProperty("identified existing instance", CON_THING, eiInst.getInstanceName());
		}

		String newInstId = pEi.getNewInstanceId();
		if (newInstId != null) {
			ceAddFnProperty("new instance name", null, newInstId);
		}

		ceEndSentence();

		//Now generate the conv CE for the related matched triples
		for (MatchedTriple mt : pEi.getMatchedTriples()) {
			generateConvCeForMatchedTriple(mt);
		}
	}

	private void generateConvCeForMatchedTriple(MatchedTriple pMt) {
		ceDeclarationLong(ConvItem.getDeterminer(), pMt.calculateCeConceptName(), pMt.getId());

		if (pMt.isObjectValue()) {
			ceSecondaryConcept(ConvItem.getDeterminer(), CON_MDT);
		} else {
			ceSecondaryConcept(ConvItem.getDeterminer(), CON_MOT);
		}

		ExtractedItem ei = pMt.getExtractedItem();
		if (ei != null) {
			ceAddVsProperty("comes from", CON_CONVEXI, ei.getId());
		}

		CeConcept subCon = pMt.getSubjectConcept();
		if (subCon != null) {
			ceAddFnProperty("subject concept", CON_ENTCON, subCon.getConceptName());
		}

		CeInstance subInst = pMt.getSubjectInstance();
		if (subInst != null) {
			ceAddFnProperty("subject instance", CON_THING, subInst.getInstanceName());
		}

		CeProperty predProp = pMt.getPredicateProperty();
		if (predProp != null) {
			ceAddFnProperty("predicate property", CON_RELCON, predProp.formattedFullPropertyName());
		}

		String objInstId = pMt.getObjectInstanceId();
		if (objInstId != null) {
			if (pMt.isNewInstance()) {
				ceAddFnProperty("object new instance id", null, objInstId);
			} else {
				ceAddFnProperty("object existing instance", CON_THING, objInstId);
			}
		}

		String objVal = pMt.getObjectValue();
		if (objVal != null) {
			ceAddFnProperty("object value", null, objVal);
		}

		String objConName = pMt.getObjectConceptName();
		if (objConName != null) {
			ceAddFnProperty("object concept", CON_ENTCON, objConName);
		}

		String origDesc = pMt.getOriginalDescription();
		if (origDesc != null) {
			ceAddFnProperty("original description", null, origDesc);
		}

		ceEndSentence();
	}

	private void generateConvCeForText(ConvText pCt) {
		ceDeclarationLong(ConvItem.getDeterminer(), pCt.getConceptName(), pCt.getId());
		ceAddFnProperty("text", null, pCt.getItemText());

		for (ConvSentence cs : pCt.getChildSentences()) {
			ceAddFnProperty("sentence", ConvSentence.getConceptName(), cs.getId());
		}

		ceEndSentence();

		//Now generate the conv CE for the child sentences
		for (ConvSentence cs : pCt.getChildSentences()) {
			generateConvCeForSentence(cs);
		}
	}

	private void generateConvCeForSentence(ConvSentence pCs) {
		String anText = pCs.getAnalysisText();

		ceDeclarationLong(ConvItem.getDeterminer(), ConvSentence.getConceptName(), pCs.getId());
		ceAddFnProperty("sentence text", null, pCs.getItemText());

		if (anText != null) {
			ceAddFnProperty("analysis text", null, anText);
		}

		for (ConvClause cc : pCs.getChildClauses()) {
			ceAddFnProperty("clause", ConvClause.getConceptName(), cc.getId());
		}

		ceEndSentence();

		//Now generate the conv CE for the child clauses
		for (ConvClause cc : pCs.getChildClauses()) {
			generateConvCeForClause(cc);
		}
	}

	private void generateConvCeForClause(ConvClause pCc) {
		ceDeclarationLong(ConvItem.getDeterminer(), ConvClause.getConceptName(), pCc.getId());
		ceAddFnProperty("clause text", null, pCc.getItemText());

		for (ConvWord cw : pCc.getChildWords()) {
			ceAddFnProperty("word", ConvWord.getConceptName(), cw.getId());
		}

		ceEndSentence();

		//Now generate the conv CE for the child words
		for (ConvWord cw : pCc.getChildWords()) {
			generateConvCeForWord(cw);
		}
	}

	private void generateConvCeForWord(ConvWord pCw) {
		ceDeclarationLong(ConvItem.getDeterminer(), ConvWord.getConceptName(), pCw.getId());
		ceAddFnProperty("word text", null, pCw.getItemText());
		ceAddFnProperty("word position", null, new Integer(pCw.getWordIndex()).toString());

		ceEndSentence();
	}

	private void wordCeForAdditionalTypes(ProcessedWord pPw) {
//		if (pPw.isCommonWord()) {
//			ceSecondaryConcept(ConvItem.getDeterminer(), CON_COMWORD);
//		}

		if (pPw.isQuestionWord()) {
			ceSecondaryConcept(ConvItem.getDeterminer(), CON_QUESWORD);
		}

		if (pPw.isStandardWord()) {
			ceSecondaryConcept(ConvItem.getDeterminer(), CON_STDWORD);
		}

		if (pPw.isValueWord()) {
			ceSecondaryConcept(ConvItem.getDeterminer(), CON_PVWORD);
		}

		if (pPw.isNumberWord()) {
			ceSecondaryConcept(ConvItem.getDeterminer(), CON_NUMWORD);
		}
	}

	private void wordCeForMatchingConcept(ProcessedWord pPw) {
		CeConcept matchCon = pPw.getMatchingConcept();

		if (matchCon != null) {
			ceAddVsProperty("corresponds to concept", CON_ENTCON, matchCon.getConceptName());
		}
	}

	private void wordCeForMatchingRelation(ProcessedWord pPw) {
		for (CeProperty thisProp : pPw.listMatchingRelations()) {
			ceAddVsProperty("corresponds to relation", CON_RELCON, thisProp.formattedFullPropertyName());
		}
	}

	private void wordCeForMatchingInstance(ProcessedWord pPw) {
		CeInstance matchInst = pPw.getMatchingInstance();

		if (matchInst != null) {
			String conName = matchInst.getFirstLeafConceptName();

			if (!conName.isEmpty()) {
				ceAddVsProperty("corresponds to instance", conName, matchInst.getInstanceName());
			}
		}
	}

	private void wordCeForReferringConcepts(ProcessedWord pPw) {
		for (CeConcept refCon : pPw.listReferredExactConcepts()) {
			ceAddVsProperty("refers to concept", CON_ENTCON, refCon.getConceptName());
		}
	}

	private void wordCeForReferringConceptsPlural(ProcessedWord pPw) {
		for (CeConcept refCon : pPw.listReferredExactConceptsPlural()) {
			ceAddVsProperty("refers in plural to concept", CON_ENTCON, refCon.getConceptName());
		}
	}

	private void wordCeForReferringRelations(ProcessedWord pPw) {
		for (CeProperty refProp : pPw.listReferredExactRelations()) {
			ceAddVsProperty("refers to relation", CON_RELCON, refProp.formattedFullPropertyName());
		}
	}

	private void wordCeForReferringInstances(ProcessedWord pPw) {
		for (CeInstance refInst : pPw.listReferredExactInstances()) {
			String conName = refInst.getFirstLeafConceptName();

			ceAddVsProperty("refers to instance", conName, refInst.getInstanceName());
		}
	}

	private void wordCeForReferringInstancesPlural(ProcessedWord pPw) {
		for (CeInstance refInst : pPw.listReferredExactInstancesPlural()) {
			ceAddVsProperty("refers in plural to instance", CON_THING, refInst.getInstanceName());
		}
	}

	private void wordCeForExtractedItem(ExtractedItem pEi) {
		if (pEi != null) {
			ceAddVsProperty("is linked to", CON_CONVEXI, pEi.getId());
		}
	}

}